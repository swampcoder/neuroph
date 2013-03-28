package org.neuroph.netbeans.explorer;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.api.settings.ConvertAsProperties;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.DataSet;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Explorer Top component which displays neural network nodees. See
 * http://platform.netbeans.org/tutorials/nbm-selection-1.html
 * http://platform.netbeans.org/tutorials/nbm-selection-2.html
 * http://platform.netbeans.org/tutorials/nbm-nodesapi3.html
 * http://wiki.netbeans.org/BasicUnderstandingOfTheNetBeansNodesAPI
 *
 *
 * http://netbeans-org.1045718.n5.nabble.com/TopComponent-associateLookup-is-incompatible-with-setActivatedNodes-is-it-a-bug-td3261230.html
 */
@ConvertAsProperties(dtd = "-//org.neuroph.netbeans.ide.navigator//Explorer//EN",
autostore = false)
public final class ExplorerTopComponent extends TopComponent implements LookupListener, ExplorerManager.Provider {

    private static ExplorerTopComponent instance;
    private static final String PREFERRED_ID = "ExplorerTopComponent";
    private final ExplorerManager explorerManager = new ExplorerManager();

    //  private InstanceContent content;
    public ExplorerTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ExplorerTopComponent.class, "CTL_ExplorerTopComponent"));
        setToolTipText(NbBundle.getMessage(ExplorerTopComponent.class, "HINT_ExplorerTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        // associate explorer manager lookup as lookup of this top componnet
        associateLookup(ExplorerUtils.createLookup(explorerManager, getActionMap()));      
//           
        ((BeanTreeView) jScrollPane1).setRootVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = jScrollPane1 = new BeanTreeView();

        // Code of sub-components and layout - not shown here

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized ExplorerTopComponent getDefault() {
        if (instance == null) {
            instance = new ExplorerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the NavigatorClassTopComponent instance. Never call
     * {@link #getDefault} directly!
     */
    public static synchronized ExplorerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ExplorerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ExplorerTopComponent) {
            return (ExplorerTopComponent) win;
        }
        Logger.getLogger(ExplorerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");

        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
        
    }

    @Override
    public void componentOpened() {
        // listen for neural network selection in global lookup
   
        resultNN = Utilities.actionsGlobalContext().lookupResult(NeuralNetwork.class);
        resultNN.addLookupListener(this);
        resultChanged(new LookupEvent(resultNN));

        // listen for data set selection in global lookup
        resultDS = Utilities.actionsGlobalContext().lookupResult(DataSet.class);
        resultDS.addLookupListener(this);
        resultChanged(new LookupEvent(resultDS));        
        
        // listen for folder selection in global lookup (when user clicks nn, trainingor test set folder
        resultDF = Utilities.actionsGlobalContext().lookupResult(DataFolder.class);
        resultDF.addLookupListener(this);
        resultChanged(new LookupEvent(resultDF));          
        
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    Result<NeuralNetwork> resultNN;
    Result<DataSet> resultDS;
    Result<DataFolder> resultDF;
    private boolean recursiveCall = false;

    @Override
    public void resultChanged(LookupEvent le) {               
        Lookup.Result localResult = (Result) le.getSource();
        Collection<Object> coll = localResult.allInstances();
        if (!coll.isEmpty()) {
            
            for (Object selectedItem : coll) {
                if (selectedItem instanceof NeuralNetwork) {    // if neural network is selected
                    NeuralNetwork selectedNNet = (NeuralNetwork) selectedItem;
                    this.setName(selectedNNet.getLabel() + " -  Explorer");
                     ((BeanTreeView) jScrollPane1).setRootVisible(true);
                    ExplorerNeuralNetworkNode nnNode = new ExplorerNeuralNetworkNode(selectedNNet);
                    
                    recursiveCall = true;
                    explorerManager.setRootContext (nnNode); //this one calls resultChanged recursivly, since global lookup is changed
                    try {
                        explorerManager.setExploredContextAndSelection(nnNode, new Node[]{nnNode});
                    } catch (PropertyVetoException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (selectedItem instanceof DataSet) { // if data set is selected
                    DataSet selectedDataSet = (DataSet) selectedItem;
                    ExplorerDataSetNode dataSetNode = new ExplorerDataSetNode(selectedDataSet);

                    ((BeanTreeView) jScrollPane1).setRootVisible(true);
                    recursiveCall = true;
                    explorerManager.setRootContext(dataSetNode); //this one calls resultChanged recursivly, since global lookup is changed
                    try {
                        explorerManager.setExploredContextAndSelection(dataSetNode, new Node[]{dataSetNode});
                    } catch (PropertyVetoException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (selectedItem instanceof DataFolder) {
                    explorerManager.setRootContext(Node.EMPTY);
                    BeanTreeView btw = (BeanTreeView) jScrollPane1;
                    btw.setRootVisible(false);
                    this.setName("Explorer");                    
                } 
            }
        } else { // if nothing is selected...
            if (!recursiveCall) {
                explorerManager.setRootContext(Node.EMPTY);
                BeanTreeView btw = (BeanTreeView) jScrollPane1;
                btw.setRootVisible(false);
                this.setName("Explorer");
                
            } else {
                recursiveCall = false;
            }
       }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
}
