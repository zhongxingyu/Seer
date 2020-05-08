 package org.cytoscape.coreplugin.cpath2.task;
 
 import cytoscape.*;
 import cytoscape.util.CyNetworkNaming;
 import cytoscape.ding.DingNetworkView;
 import cytoscape.ding.CyGraphLOD;
 import cytoscape.layout.CyLayoutAlgorithm;
 import cytoscape.layout.CyLayouts;
 import cytoscape.logger.CyLogger;
 import cytoscape.view.CyNetworkView;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.readers.GraphReader;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 import cytoscape.visual.VisualStyle;
 import cytoscape.visual.VisualMappingManager;
 
 import org.biopax.paxtools.io.SimpleIOHandler;
 import org.biopax.paxtools.model.BioPAXElement;
 import org.biopax.paxtools.model.Model;
 import org.biopax.paxtools.model.level2.physicalEntity;
 import org.biopax.paxtools.model.level3.PhysicalEntity;
 import org.cytoscape.coreplugin.cpath2.cytoscape.BinarySifVisualStyleUtil;
 import org.cytoscape.coreplugin.cpath2.web_service.*;
 //import org.mskcc.biopax_plugin.mapping.MapNodeAttributes;
 import cytoscape.coreplugins.biopax.MapBioPaxToCytoscape;
 import cytoscape.coreplugins.biopax.util.BioPaxUtil;
 import cytoscape.coreplugins.biopax.util.CytoscapeWrapper;
 import cytoscape.coreplugins.biopax.action.NetworkListener;
 import cytoscape.coreplugins.biopax.view.BioPaxContainer;
 import cytoscape.coreplugins.biopax.util.BioPaxVisualStyleUtil;
 
 import javax.swing.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Controller for Executing a Get Record(s) by CPath ID(s) command.
  *
  * @author Ethan Cerami.
  */
 public class ExecuteGetRecordByCPathId implements Task {
     private CPathWebService webApi;
     private TaskMonitor taskMonitor;
     private long ids[];
     private String networkTitle;
     private boolean haltFlag = false;
     private CyNetwork mergedNetwork;
     private CPathResponseFormat format;
     private final static String CPATH_SERVER_NAME_ATTRIBUTE = "CPATH_SERVER_NAME";
     private final static String CPATH_SERVER_DETAILS_URL = "CPATH_SERVER_DETAILS_URL";
 		private CyLogger logger = CyLogger.getLogger(ExecuteGetRecordByCPathId.class);
 
     /**
      * Constructor.
      *
      * @param webApi        cPath Web API.
      * @param ids           Array of cPath IDs.
      * @param format        CPathResponseFormat Object.
      * @param networkTitle  Tentative Network Title.
      */
     public ExecuteGetRecordByCPathId(CPathWebService webApi, long ids[], CPathResponseFormat format,
             String networkTitle) {
         this.webApi = webApi;
         this.ids = ids;
         this.format = format;
         this.networkTitle = networkTitle;
     }
 
     /**
      * Constructor.
      *
      * @param webApi        cPath Web API.
      * @param ids           Array of cPath IDs.
      * @param format        CPathResponseFormat Object.
      * @param networkTitle  Tentative Network Title.
      * @param mergedNetwork Network to merge into.
      */
     public ExecuteGetRecordByCPathId(CPathWebService webApi, long ids[], CPathResponseFormat format,
             String networkTitle, CyNetwork mergedNetwork) {
         this.webApi = webApi;
         this.ids = ids;
         this.format = format;
         this.networkTitle = networkTitle;
         this.mergedNetwork = mergedNetwork;
     }
 
     /**
      * Our implementation of Task.abort()
      */
     public void halt() {
         webApi.abort();
         haltFlag = true;
     }
 
     /**
      * Our implementation of Task.setTaskMonitor().
      *
      * @param taskMonitor TaskMonitor
      */
     public void setTaskMonitor(TaskMonitor taskMonitor) throws IllegalThreadStateException {
         this.taskMonitor = taskMonitor;
     }
 
     /**
      * Our implementation of Task.getTitle.
      *
      * @return Task Title.
      */
     public String getTitle() {
         return "Retrieving " + networkTitle + " from "
                 + CPathProperties.getInstance().getCPathServerName() + "...";
     }
 
     /**
      * Our implementation of Task.run().
      */
     public void run() {
         try {
             // read the network from cpath instance
             if (taskMonitor != null) {
                 taskMonitor.setPercentCompleted(-1);
                 taskMonitor.setStatus("Retrieving " + networkTitle + ".");
             }
 
             //  Store BioPAX to Temp File
             String tmpDir = System.getProperty("java.io.tmpdir");
             //  Branch based on download mode setting.
             File tmpFile;
             if (format == CPathResponseFormat.BIOPAX) {
                 tmpFile = File.createTempFile("temp", ".xml", new File(tmpDir));
             } else {
                 tmpFile = File.createTempFile("temp", ".sif", new File(tmpDir));
             }
             tmpFile.deleteOnExit();
 
             //  Get Data, and write to temp file.
             String data = webApi.getRecordsByIds(ids, format, taskMonitor);
             FileWriter writer = new FileWriter(tmpFile);
             writer.write(data);
             writer.close();
 
             //  Load up File via ImportHandler Framework
             //  the biopax graph reader is going to be called
             //  it will look for the network view title
             //  via system properties, so lets set it now
             if (networkTitle != null && networkTitle.length() > 0) {
                 System.setProperty("biopax.network_view_title", networkTitle);
             }
             GraphReader reader = Cytoscape.getImportHandler().getReader(tmpFile.getAbsolutePath());
             if (taskMonitor != null) {
                 taskMonitor.setStatus("Creating Cytoscape Network...");
                 taskMonitor.setPercentCompleted(-1);
             }
 
             CyNetwork cyNetwork = null;
             // Branch, based on download mode.
             if (format == CPathResponseFormat.BINARY_SIF) {
                 // create network, without the view.
                 cyNetwork = Cytoscape.createNetwork(reader, false, null);
                 postProcessingBinarySif(cyNetwork);
             } else {
                 //  create network, without the view.
                 cyNetwork = Cytoscape.createNetwork(reader, false, null);
                 postProcessingBioPAX(cyNetwork);             
             }
 
             // Fire appropriate network event.
             if (mergedNetwork == null) {
                 //  Fire a Network Loaded Event
                 Object[] ret_val = new Object[2];
                 ret_val[0] = cyNetwork;
                 ret_val[1] = networkTitle;
                 Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, ret_val);
             } else {
                 //  Fire a Network Modified Event;  causes Quick Find to Re-Index.
                 Object[] ret_val = new Object[2];
                 ret_val[0] = mergedNetwork;
                 ret_val[1] = networkTitle;
                 Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null,
                     ret_val);
             }
 
             //  Add Links Back to cPath Instance
             addLinksToCPathInstance (cyNetwork);
 
             if (taskMonitor != null) {
                 taskMonitor.setStatus("Done");
                 taskMonitor.setPercentCompleted(100);
             }
         } catch (IOException e) {
             taskMonitor.setException(e, "Failed to retrieve records.",
                     "Please try again.");
         } catch (EmptySetException e) {
             taskMonitor.setException(e, "No matches found for your request.  ",
                     "Please try again.");
         } catch (CPathException e) {
             if (e.getErrorCode() != CPathException.ERROR_CANCELED_BY_USER) {
                 taskMonitor.setException(e, e.getMessage(), e.getRecoveryTip());
             }
         }
     }
 
     /**
      * Add Node Links Back to cPath Instance.
      * @param cyNetwork CyNetwork.
      */
     private void addLinksToCPathInstance(CyNetwork cyNetwork) {
         CyAttributes networkAttributes = Cytoscape.getNetworkAttributes();
         CPathProperties props = CPathProperties.getInstance();
         String serverName = props.getCPathServerName();
         String serverURL = props.getCPathUrl();
         String cPathServerDetailsUrl = networkAttributes.getStringAttribute
                 (cyNetwork.getIdentifier(), ExecuteGetRecordByCPathId.CPATH_SERVER_DETAILS_URL);
         if (cPathServerDetailsUrl == null) {
             networkAttributes.setAttribute(cyNetwork.getIdentifier(),
                     ExecuteGetRecordByCPathId.CPATH_SERVER_NAME_ATTRIBUTE,
                     serverName);
             String url = serverURL.replaceFirst("webservice.do", "record2.do?id=");
             networkAttributes.setAttribute(cyNetwork.getIdentifier(),
                     ExecuteGetRecordByCPathId.CPATH_SERVER_DETAILS_URL,
                     url);
         }
     }
 
     /**
      * Execute Post-Processing on BINARY SIF Network.
      *
      * @param cyNetwork Cytoscape Network Object.
      */
     private void postProcessingBinarySif(final CyNetwork cyNetwork) {
         CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
 
         //  Init the node attribute meta data, e.g. description, visibility, etc.
         MapBioPaxToCytoscape.initAttributes(nodeAttributes);
 
         //  Set the Quick Find Default Index
         Cytoscape.getNetworkAttributes().setAttribute(cyNetwork.getIdentifier(),
                 "quickfind.default_index", "biopax.node_label");
 
         //  Specify that this is a BINARY_NETWORK
         Cytoscape.getNetworkAttributes().setAttribute(cyNetwork.getIdentifier(),
                 BinarySifVisualStyleUtil.BINARY_NETWORK, Boolean.TRUE);
 
         //  Get all node details.
         getNodeDetails(cyNetwork, nodeAttributes);
 
         if (haltFlag == false) {
             if (mergedNetwork != null) {
                 mergeNetworks(cyNetwork);
             } else if (cyNetwork.getNodeCount() < Integer.parseInt(CytoscapeInit.getProperties()
                     .getProperty("viewThreshold"))) {
                 if (taskMonitor != null) {
                     taskMonitor.setStatus("Creating Network View...");
                     taskMonitor.setPercentCompleted(-1);
                 }
 
                 //  Set up the right visual style
                 VisualStyle visualStyle = BinarySifVisualStyleUtil.getVisualStyle();
 
                 //  Set up the right layout algorithm.
                 CyLayoutAlgorithm layoutAlgorithm = CyLayouts.getLayout("force-directed");
 
                 //  Now, create the view.
                 //  Use local create view option, so that we don't mess up the visual style.
                 CyNetworkView view = createNetworkView
                         (cyNetwork, cyNetwork.getTitle(), layoutAlgorithm, null);
                 
                 //  Now apply the visual style;
                 //  Doing this as a separate step ensures that the visual style appears
                 //  in the visual style drop-down menu.
                 view.applyVizmapper(visualStyle);
 
                 // Set up clickable node details.
                 CytoscapeWrapper.initBioPaxPlugInUI();
                 final BioPaxContainer bpContainer = BioPaxContainer.getInstance();
                 NetworkListener networkListener = bpContainer.getNetworkListener();
                 networkListener.registerNetwork(cyNetwork);
 
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         CytoscapeWrapper.activateBioPaxPlugInTab(bpContainer);
                         bpContainer.showLegend();
                         Cytoscape.getCurrentNetworkView().fitContent();
                         String networkTitleWithUnderscores = networkTitle.replaceAll(": ", "");
                         networkTitleWithUnderscores = networkTitleWithUnderscores.replaceAll(" ", "_");
                         networkTitleWithUnderscores = CyNetworkNaming.getSuggestedNetworkTitle
                                 (networkTitleWithUnderscores);
                         cyNetwork.setTitle(networkTitleWithUnderscores);
                     }
                 });
             }
         } else {
             //  If we have requested a halt, and we have a network, destroy it.
             if (cyNetwork != null) {
                 Cytoscape.destroyNetwork(cyNetwork);
             }
         }
     }
 
     /**
      * Execute Post-Processing on BioPAX Network.
      *
      * @param cyNetwork Cytoscape Network Object.
      */
     private void postProcessingBioPAX(final CyNetwork cyNetwork) {
         if (haltFlag == false) {
             if (mergedNetwork != null) {
                 mergeNetworks(cyNetwork);
             } else if (cyNetwork.getNodeCount() < Integer.parseInt(CytoscapeInit.getProperties()
                     .getProperty("viewThreshold"))) {
                 if (taskMonitor != null) {
                     taskMonitor.setStatus("Creating Network View...");
                     taskMonitor.setPercentCompleted(-1);
                 }
 
                 //  Set up the right visual style
                 VisualStyle visualStyle = BioPaxVisualStyleUtil.getBioPaxVisualStyle();
 
                 //  Set up the right layout algorithm.
                 CyLayoutAlgorithm layoutAlgorithm = CyLayouts.getLayout("force-directed");
 
                 //  Now, create the view.
                 //  Use local create view option, so that we don't mess up the visual style.
                 CyNetworkView view = createNetworkView(cyNetwork,
                         cyNetwork.getTitle(), layoutAlgorithm, null);
 
                 //  Now apply the visual style;
                 //  Doing this as a separate step ensures that the visual style appears
                 //  in the visual style drop-down menu.
                 view.applyVizmapper(visualStyle);
             }
         } else {
             //  If we have requested a halt, and we have a network, destroy it.
             if (cyNetwork != null) {
                 Cytoscape.destroyNetwork(cyNetwork);
             }
         }
     }
 
 
     private void mergeNetworks(CyNetwork cyNetwork) {
         taskMonitor.setStatus("Merging Network...");
         List nodeList = cyNetwork.nodesList();
         for (int i = 0; i < nodeList.size(); i++) {
             CyNode node = (CyNode) nodeList.get(i);
             mergedNetwork.addNode(node);
         }
         List edgeList = cyNetwork.edgesList();
         for (int i = 0; i < edgeList.size(); i++) {
             CyEdge edge = (CyEdge) edgeList.get(i);
             mergedNetwork.addEdge(edge);
         }
 
         //  Select this view
         final CyNetworkView networkView = Cytoscape.getNetworkView
                 (mergedNetwork.getIdentifier());
         Cytoscape.setCurrentNetwork(mergedNetwork.getIdentifier());
         Cytoscape.setCurrentNetworkView(mergedNetwork.getIdentifier());
 
         final BioPaxContainer bpContainer = BioPaxContainer.getInstance();
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 CytoscapeWrapper.activateBioPaxPlugInTab(bpContainer);
                 bpContainer.showLegend();
                 VisualMappingManager vizmapper = Cytoscape.getVisualMappingManager();
                 vizmapper.applyAppearances();
             }
         });
 
         //  Select only the new nodes
         mergedNetwork.unselectAllEdges();
         mergedNetwork.unselectAllNodes();
         mergedNetwork.setSelectedNodeState(nodeList, true);
         mergedNetwork.setSelectedEdgeState(edgeList, true);
 
         //  Delete the temp network.
         Cytoscape.destroyNetwork(cyNetwork);
 
         //  Apply Layout
         Object[] options = {"Yes", "No"};
         int n = JOptionPane.showOptionDialog(Cytoscape.getDesktop(),
             "Would you like to layout the modified network?",
             "Adjust Layout?",
             JOptionPane.YES_NO_CANCEL_OPTION,
             JOptionPane.QUESTION_MESSAGE,
             null,
             options, options[0]);
         if (n==0) {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                 		CyLayoutAlgorithm layoutAlgorithm = CyLayouts.getLayout("force-directed");
                     networkView.applyLayout(layoutAlgorithm);
                     Cytoscape.getCurrentNetworkView().fitContent();
                 }
             });
         }
     }
 
     /**
      * Gets Details for Each Node from Web Service API.
      */
     private void getNodeDetails (CyNetwork cyNetwork,  CyAttributes nodeAttributes) {
         if (taskMonitor != null) {
             taskMonitor.setStatus("Retrieving node details...");
             taskMonitor.setPercentCompleted(0);
         }
         ArrayList batchList = createBatchArray(cyNetwork);
         if (batchList.size()==0) {
 						logger.info ("Skipping node details.  Already have all the details new need.");
         }
         for (int i=0; i<batchList.size(); i++) {
             if (haltFlag == true) {
                 break;
             }
             ArrayList currentList = (ArrayList) batchList.get(i);
             logger.debug ("Getting node details, batch:  " + i);
             long ids[] = new long [currentList.size()];
             for (int j=0; j<currentList.size(); j++) {
                 CyNode node = (CyNode) currentList.get(j);
                 ids[j] = Long.valueOf(node.getIdentifier());
             }
             try {
                 String xml = webApi.getRecordsByIds(ids, CPathResponseFormat.BIOPAX,
                         new NullTaskMonitor());
                 //StringReader reader = new StringReader(xml);
                 //BioPaxUtil bpUtil = new BioPaxUtil(reader, new NullTaskMonitor());
                 Model model = (new SimpleIOHandler())
                 	.convertFromOWL(new ByteArrayInputStream(xml.getBytes()));
                //ArrayList peList = bpUtil.getPhysicalEntityList();
                //Namespace ns = Namespace.getNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                //for (int j=0; j<peList.size(); j++) {
                 for(BioPAXElement pe: BioPaxUtil.getObjects(model, physicalEntity.class, PhysicalEntity.class)) {
                    //Element element = (Element) peList.get(j);
                    //String id = element.getAttributeValue("ID", ns);
                	String id = BioPaxUtil.getLocalPartRdfId(pe);
                    if (id != null) {
                        id = id.replaceAll("CPATH-", "");
                        MapBioPaxToCytoscape.mapNodeAttribute(pe, model, id);
                    }
                 }
                 int percentComplete = (int) (100.0 * (i / (double) batchList.size()));
                 if (taskMonitor != null) {
                     taskMonitor.setPercentCompleted(percentComplete);
                 }
             } catch (EmptySetException e) {
                 e.printStackTrace();
             } catch (CPathException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private ArrayList createBatchArray(CyNetwork cyNetwork) {
         CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
         int max_ids_per_request = 50;
         ArrayList masterList = new ArrayList();
         ArrayList currentList = new ArrayList();
         Iterator nodeIterator = cyNetwork.nodesIterator();
         int counter = 0;
         while (nodeIterator.hasNext()) {
             CyNode node = (CyNode) nodeIterator.next();
             String label = nodeAttributes.getStringAttribute(node.getIdentifier(),
                     BioPaxVisualStyleUtil.BIOPAX_NODE_LABEL);
 
             //  If we already have details on this node, skip it.
             if (label == null) {
                 currentList.add(node);
                 counter++;
             }
             if (counter > max_ids_per_request) {
                 masterList.add(currentList);
                 currentList = new ArrayList();
                 counter = 0;
             }
         }
         if (currentList.size() > 0) {
             masterList.add(currentList);
         }
         return masterList;
     }
 
     private CyNetworkView createNetworkView (CyNetwork network, String title, CyLayoutAlgorithm
             layout, VisualStyle vs) {
 
 		if (Cytoscape.viewExists(network.getIdentifier())) {
 			return Cytoscape.getNetworkView(network.getIdentifier());
 		}
 
 		final DingNetworkView view = new DingNetworkView(network, title);
 		view.setGraphLOD(new CyGraphLOD());
 		view.setIdentifier(network.getIdentifier());
 		view.setTitle(network.getTitle());
 		Cytoscape.getNetworkViewMap().put(network.getIdentifier(), view);
 		Cytoscape.setSelectionMode(Cytoscape.getSelectionMode(), view);
 
         VisualMappingManager VMM = Cytoscape.getVisualMappingManager();
         if (vs != null) {
 			view.setVisualStyle(vs.getName());
             VMM.setVisualStyle(vs);
             VMM.setNetworkView(view);
         }
 
 		if (layout == null) {
 			layout = CyLayouts.getDefaultLayout();
 		}
 
 		Cytoscape.firePropertyChange(cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED,
                 null, view);
 		layout.doLayout(view);
 		view.fitContent();
 		view.redrawGraph(false, true);
 		return view;
     }
 }
 
 class NullTaskMonitor implements TaskMonitor {
 
     public void setPercentCompleted(int i) throws IllegalArgumentException {
     }
 
     public void setEstimatedTimeRemaining(long l) throws IllegalThreadStateException {
     }
 
     public void setException(Throwable throwable, String string)
             throws IllegalThreadStateException {
     }
 
     public void setException(Throwable throwable, String string, String string1)
             throws IllegalThreadStateException {
     }
 
     public void setStatus(String string) throws IllegalThreadStateException, NullPointerException {
     }
 }
