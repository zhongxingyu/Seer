 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.awe.afp.models;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 
 import org.amanzi.awe.afp.Activator;
 import org.amanzi.awe.afp.ControlFileProperties;
 import org.amanzi.awe.afp.executors.AfpProcessExecutor;
 import org.amanzi.awe.afp.executors.AfpProcessProgress;
 import org.amanzi.awe.afp.exporters.AfpExporter;
 import org.amanzi.awe.afp.filters.AfpColumnFilter;
 import org.amanzi.awe.afp.filters.AfpFilter;
 import org.amanzi.awe.afp.filters.AfpRowFilter;
 import org.amanzi.awe.afp.services.DomainRelations;
 import org.amanzi.awe.afp.services.DomainService;
 import org.amanzi.awe.console.AweConsolePlugin;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.INodeType;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.network.NetworkModel;
 import org.amanzi.neo.services.networkselection.SelectionModel;
 import org.amanzi.neo.services.node2node.NodeToNodeRelationModel;
 import org.amanzi.neo.services.node2node.NodeToNodeTypes;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.amanzi.neo.services.utils.Pair;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 import org.neo4j.graphdb.traversal.Evaluation;
 import org.neo4j.graphdb.traversal.Evaluator;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.kernel.Traversal;
 
 /**
  * <p>
  * AFP model
  * </p>
  * 
  * @since 1.0.0
  */
 public class AfpModel {
     /**
      * <p>
      * Scaling factor wrapper
      * </p>
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public static class ScalingFactors {
 
         private float[] co;
         private float[] adj;
 
         public float[] getCo() {
             return co;
         }
 
         public void setCo(float[] co) {
             this.co = co;
         }
 
         public float[] getAdj() {
             return adj;
         }
 
         public void setAdj(float[] adj) {
             this.adj = adj;
         }
 
     }
 
     protected Node datasetNode;
     protected Node afpNode;
     private static final String AFP_NODE_NAME = "afp-dataset";
 
     private AfpProcessExecutor afpJob;
     private HashMap<String, Node> networkNodes;
     private HashMap<String, Node> afpNodes;
 
     private SelectionModel selectionModel;
 
     /*
      * Selection List nodes
      */
     private HashMap<Node, Map<String, SelectionModel>> networkSelectionNodes = new HashMap<Node, Map<String, SelectionModel>>();
 
     boolean optimizeFrequency = true;
     boolean optimizeBSIC = true;
     boolean optimizeHSN = true;
     boolean optimizeMAIO = true;
 
     public static final String[] DOMAIN_TYPES = {"frequency", "mal", "sector_separations", "site_separations"};
     public static final String[] BAND_NAMES = {"900", "1800", "850", "1900"};
     public static final String[] CHANNEL_NAMES = {"BCCH", "TCH Non/BB Hopping", "TCH SY Hopping"};
     public static final String GOALS_SUMMARY_ROW_HEADERS[] = {"Selected Sectors: ", "Selected TRXs: ", "BCCH TRXs: ",
             "TCH Non/BB Hopping TRXs: ", "TCH SY HoppingTRXs: "};
 
     public static final int CHANNEL_BCCH = 0;
     public static final int CHANNEL_NON_HOPIING = 1;
     public static final int CHANNEL_HOPPING = 2;
 
     public static final int BAND_900 = 0;
     public static final int BAND_1800 = 1;
     public static final int BAND_850 = 2;
     public static final int BAND_1900 = 3;
 
     public static final String[][] SCALING_PAGE_ROW_HEADERS = { {"BCCH", "BCCH"}, {"BCCH", "Non/BB TCH"}, {"BCCH", "SY TCH"},
             {"Non/BB TCH", "BCCH"}, {"Non/BB TCH", "Non/BB TCH"}, {"Non/BB TCH", "SY TCH"}, {"SY TCH", "BCCH"},
             {"SY TCH", "Non/BB TCH"}, {"SY TCH", "SY TCH"}};
     public static final String[] DEFAULT_BAND_NAMES = {"Free 900", "Free 1800", "Free 850", "Free 1900"};
     public static final String DEFAULT_MAL_NAME = "Default MAL";
     public static final String DEFAULT_SECTOR_SEP_NAME = "Default Separations";
     public static final String DEFAULT_SITE_SEP_NAME = "Default Separations";
     private int totalTRX;
     private int totalRemainingTRX;
     // private int totalRemainingMalTRX;
     private int totalSites;
     private int totalSectors;
     private int totalHoppingTRX;
 
     /**
      * 0- 900 1- 1800 2- 850 3- 1900
      */
     boolean[] frequencyBands = new boolean[] {true, true, false, false};
 
     /**
      * 0- BCCH 1- TCH Non/BB Hopping 2- TCH SY Hopping
      */
     boolean[] channelTypes = new boolean[] {true, true, true};
     boolean analyzeCurrentFreqAllocation = true;
 
     // Page 2 params
     String availableFreq[] = new String[4];
     int availableNCCs = 0xff;
     int availableBCCs = 0xff;
 
     // Page 3 params
     HashMap<String, AfpFrequencyDomainModel> freqDomains = new LinkedHashMap<String, AfpFrequencyDomainModel>();
 
     // Page 4 params
     HashMap<String, AfpHoppingMALDomainModel> malDomains = new HashMap<String, AfpHoppingMALDomainModel>();
 
     // Page 5 params
     HashMap<String, AfpSeparationDomainModel> siteSeparationDomains = new HashMap<String, AfpSeparationDomainModel>();
     HashMap<String, AfpSeparationDomainModel> sectorSeparationDomains = new HashMap<String, AfpSeparationDomainModel>();
     
     HashMap<String, AfpFrequencyDomainModel> freeFrequencyDomains = new HashMap<String, AfpFrequencyDomainModel>();
 
     // Page 6 params
     // Constants defining the index of Serving-Interfering pair in the scaling rules arrays
     // NHBB: Non/BB TCH
     // SFH: SY TCH
     public final static int BCCHBCCH = 0;
     public final static int BCCHNHBB = 1;
     public final static int BCCHSFH = 2;
     public final static int NHBBBCCH = 3;
     public final static int NHBBNHBB = 4;
     public final static int NHBBSFH = 5;
     public final static int SFHBCCH = 6;
     public final static int SFHNHBB = 7;
     public final static int SFHSFH = 8;
 
     // scaling rules arrays with default values
     public Map<NodeToNodeTypes, Map<String, ScalingFactors>> scaling = new HashMap<NodeToNodeTypes, Map<String, ScalingFactors>>();
     public float[] sectorSeparation = new float[] {100, 100, 100, 100, 100, 100, 100, 100, 100};
     public float[] siteSeparation = new float[] {100, 70, 50, 70, 50, 30, 70, 50, 20};
     public static float[] coInterferenceDef = new float[] {1, 0.7f, 0.5f, 0.7f, 0.5f, 0.3f, 0.7f, 0.3f, 0.2f};
     public static float[] adjInterferenceDef = new float[] {1, 0.7f, 0.5f, 0.7f, 0.5f, 0.3f, 0.7f, 0.3f, 0.2f};
     public static float[] coNeighborDef = new float[] {1, 0.3f, 0.2f, 0, 0, 0, 0, 0, 0};
     public static float[] adjNeighborDef = new float[] {1, 0.1f, 0, 0, 0, 0, 0, 0, 0};
     public static float[] coTriangulationDef = new float[] {1, 0, 0, 0, 0, 0, 0, 0, 0};
     public static float[] adjTriangulationDef = new float[] {1, 0, 0, 0, 0, 0, 0, 0, 0};
     public static float[] coShadowingDef = new float[] {1, 0, 0, 0, 0, 0, 0, 0, 0};
     public static float[] adjShadowingDef = new float[] {1, 0, 0, 0, 0, 0, 0, 0, 0};
     private final DatasetService ds = NeoServiceFactory.getInstance().getDatasetService();
     // Progress page params
     String[] tableItems = new String[6];
 
     protected HashMap<String, String> parameters;
     protected HashMap<String, String> filters;
 
     public static final String[] sitePropertiesName = new String[] {INeoConstants.PROPERTY_NAME_NAME};
     public static final String[] sectorPropertiesName = new String[] {INeoConstants.PROPERTY_NAME_NAME};
     public static final String[] trxPropertiesName = new String[] {INeoConstants.PROPERTY_NAME_NAME, "Layer", "Subcell", "trx_id",
             "band", "Extended", "hopping_type", INeoConstants.PROPERTY_BCCH_NAME,};
     
     private Queue<AfpFrequencyDomainModel> freqDomainQueue = new LinkedList<AfpFrequencyDomainModel>();
 
     // protected HashMap<String, String> equalFilters = new HashMap<String, String>();
 
     public AfpModel() {
     }
 
     private Iterable<Node> getElementTraverser(Evaluator filter, INodeType... nodeTypes) {
         if (selectionModel == null) {
             // traverse through network
             return new NetworkModel(datasetNode).getAllElementsByType(filter, nodeTypes);
         } else {
             // traverse through selection list
             return selectionModel.getAllElementsByType(filter, nodeTypes);
         }
     }
 
     /**
      * Array rows: 0-Selected sectors 1-Selected TRXs 2- BCCH TRXs 3- TCH Non/BB Hopping TRXs 4- TCH
      * SY Hopping TRXs Array columns: 0- 900 1- 1800 2- 850 3- 1900
      * 
      * @return return an array
      */
     public int[][] getSelectedCount() {
         totalTRX = 0;
         totalSites = 0;
         totalSectors = 0;
         totalHoppingTRX = 0;
 
         int[] bandTRXs = new int[4];
         int[] bandSectors = new int[4];
         int[] bcchTRXs = new int[4];
         int[] hoppingTRXs = new int[4];
         int[] nonHoppingTRXs = new int[4];
 
         for (Node node : getElementTraverser(null, NodeTypes.SECTOR, NodeTypes.SITE, NodeTypes.TRX)) {
             // add to unique properties
 
             if (node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(NodeTypes.SITE.getId())) {
                 totalSites++;
             } else if (node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(NodeTypes.SECTOR.getId())) {
                 totalSectors++;
                 String band = (String)node.getProperty("band", "");
                 String layer = (String)node.getProperty("layer", "");
                 if (band.contains("900") || layer.contains("900")) {
                     bandSectors[BAND_900]++;
                 }
                 if (band.contains("1800") || layer.contains("1800")) {
                     bandSectors[BAND_1800]++;
                 }
                 if (band.contains("850") || layer.contains("850")) {
                     bandSectors[BAND_850]++;
                 }
                 if (band.contains("1900") || layer.contains("1900")) {
                     bandSectors[BAND_1900]++;
                 }
             } else if (node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(NodeTypes.TRX.getId())) {
                 totalTRX++;
                 String band = (String)node.getProperty("band", "");
                 if (band.contains("900")) {
                     bandTRXs[BAND_900]++;
                     if ((Boolean)node.getProperty(INeoConstants.PROPERTY_BCCH_NAME, false))
                         bcchTRXs[BAND_900]++;
                     if ((Integer)node.getProperty(INeoConstants.PROPERTY_HOPPING_TYPE_NAME, 0) < 1)
                         nonHoppingTRXs[BAND_900]++;
                     else {
                         hoppingTRXs[BAND_900]++;
                         totalHoppingTRX++;
                     }
                 }
                 if (band.contains("1800")) {
                     bandTRXs[BAND_1800]++;
                     if ((Boolean)node.getProperty(INeoConstants.PROPERTY_BCCH_NAME, false))
                         bcchTRXs[BAND_1800]++;
                     if ((Integer)node.getProperty(INeoConstants.PROPERTY_HOPPING_TYPE_NAME, 0) < 1)
                         nonHoppingTRXs[BAND_1800]++;
                     else {
                         hoppingTRXs[BAND_1800]++;
                         totalHoppingTRX++;
                     }
                 }
                 if (band.contains("850")) {
                     bandTRXs[BAND_850]++;
                     if ((Boolean)node.getProperty(INeoConstants.PROPERTY_BCCH_NAME, false))
                         bcchTRXs[BAND_850]++;
                     if ((Integer)node.getProperty(INeoConstants.PROPERTY_HOPPING_TYPE_NAME, 0) < 1)
                         nonHoppingTRXs[BAND_850]++;
                     else {
                         hoppingTRXs[BAND_850]++;
                         totalHoppingTRX++;
                     }
                 }
                 if (band.contains("1900")) {
                     bandTRXs[BAND_1900]++;
                     if ((Boolean)node.getProperty(INeoConstants.PROPERTY_BCCH_NAME, false))
                         bcchTRXs[BAND_1900]++;
                     if ((Integer)node.getProperty(INeoConstants.PROPERTY_HOPPING_TYPE_NAME, 0) < 1)
                         nonHoppingTRXs[BAND_1900]++;
                     else {
                         hoppingTRXs[BAND_1900]++;
                         totalHoppingTRX++;
                     }
                 }
             }
 
             // totalRemainingTRX = totalTRX;
             // totalRemainingMalTRX = totalTRX;
             // for (AfpFrequencyDomainModel dm : getFreqDomains(false)){
             // totalRemainingTRX -= dm.getNumTRX();
             // }
             // for (AfpHoppingMALDomainModel dm : getMalDomains(false)){
             // totalRemainingMalTRX -= dm.getNumTRX();
             // }
         }
 
         // TODO: get the count from database based on the selected node and also update frequency
         // band values based on that
         int[][] selectedArray = {bandSectors, bandTRXs, bcchTRXs, nonHoppingTRXs, hoppingTRXs};
         return selectedArray;
     }
 
     public int getTotalTRX() {
         return totalTRX;
     }
 
     public void setTotalTRX(int totalTRX) {
         this.totalTRX = totalTRX;
     }
 
     public int getTotalRemainingTRX() {
         return totalRemainingTRX;
     }
 
     public void setTotalRemainingTRX(int totalSelectedTRX) {
         this.totalRemainingTRX = totalSelectedTRX;
     }
 
     // public int getTotalRemainingMalTRX() {
     // return totalRemainingMalTRX;
     // }
     //
     // public void setTotalRemainingMalTRX(int totalRemainingMalTRX) {
     // this.totalRemainingMalTRX = totalRemainingMalTRX;
     // }
 
     public int getTotalSites() {
         return totalSites;
     }
 
     public void setTotalSites(int totalSites) {
         this.totalSites = totalSites;
     }
 
     public int getTotalSectors() {
         return totalSectors;
     }
 
     public void setTotalSectors(int totalSectors) {
         this.totalSectors = totalSectors;
     }
 
     public int getTotalHoppingTRX() {
         return totalHoppingTRX;
     }
 
     public void setTotalHoppingTRX(int totalHoppingTRX) {
         this.totalHoppingTRX = totalHoppingTRX;
     }
 
     /**
      * @return the afpNode
      */
     public Node getAfpNode() {
         return afpNode;
     }
 
     /*
      * set the network root mode for test purpose
      */
     public void setDatasetNode(Node network) {
         this.datasetNode = network;
     }
 
     public Node getDatasetNode() {
         return datasetNode;
     }
 
     public void setSelectNetworkDataSetName(String datasetName) {
         if (networkNodes == null) {
             getNetworkDatasets();
         }
         datasetNode = networkNodes.get(datasetName);
         if (datasetNode != null) {
             loadAfpDataSet();
         }
     }
 
     public void setNetworkSelectionName(String selectionListName) {
         selectionModel = networkSelectionNodes.get(datasetNode).get(selectionListName);
     }
 
     public String[] getNetworkDatasets() {
         networkNodes = new HashMap<String, Node>();
         for (Node root : NeoUtils.getAllRootTraverser(NeoServiceProviderUi.getProvider().getService(), null)) {
 
             if (NodeTypes.NETWORK.checkNode(root)) {
                 networkNodes.put(NeoUtils.getNodeName(root), root);
             }
         }
         return networkNodes.keySet().toArray(new String[0]);
     }
 
     /**
      * Returns all NetworkSelection Lists for current Network
      * 
      * @return
      */
     public String[] getNetworkSelectionLists(String networkName) {
         Map<String, SelectionModel> networkSelectionLists = networkSelectionNodes.get(datasetNode);
 
         if (networkSelectionLists == null) {
             // need to initialize
             Node networkNode = networkNodes.get(networkName);
             if (networkNode == null) {
                 getNetworkDatasets();
                 networkNode = networkNodes.get(networkName);
             }
 
             Map<String, SelectionModel> modelMap = new NetworkModel(networkNode).getAllSelectionModels();
 
             networkSelectionNodes.put(datasetNode, modelMap);
 
             return modelMap.keySet().toArray(new String[0]);
         }
 
         return networkSelectionLists.keySet().toArray(new String[0]);
     }
 
     /**
      * Gets the networ datasets.
      * 
      * @return the network datasets
      */
 
     public String[] getAfpDatasets(Node networkNode) {
         afpNodes = new HashMap<String, Node>();
         Traverser traverser = networkNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(NodeTypes.AFP.getId()))
                     return true;
                 return false;
             }
 
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
         for (Node afpNode : traverser) {
 
             if (NodeTypes.AFP.checkNode(afpNode)) {
                 afpNodes.put(NeoUtils.getNodeName(afpNode), afpNode);
             }
         }
         return afpNodes.keySet().toArray(new String[0]);
     }
 
     public boolean hasValidNetworkDataset() {
         if (datasetNode == null) {
             return false;
         }
         return true;
     }
 
     /**
      * @param afpNode the afpNode to set
      */
     public void loadAfpDataSet() {
         getAfpDatasets(datasetNode);
 
         if (afpNodes != null) {
             afpNode = afpNodes.get(AFP_NODE_NAME);
         }
         loadUserData();
     }
 
     /**
      * @return the optimizeFrequency
      */
     public boolean isOptimizeFrequency() {
         return optimizeFrequency;
     }
 
     /**
      * @param optimizeFrequency the optimizeFrequency to set
      */
     public void setOptimizeFrequency(boolean optimizeFrequency) {
         this.optimizeFrequency = optimizeFrequency;
     }
 
     /**
      * @return the optimizeBSIC
      */
     public boolean isOptimizeBSIC() {
         return optimizeBSIC;
     }
 
     /**
      * @param optimizeBSIC the optimizeBSIC to set
      */
     public void setOptimizeBSIC(boolean optimizeBSIC) {
         this.optimizeBSIC = optimizeBSIC;
     }
 
     /**
      * @return the optimizeHsn
      */
     public boolean isOptimizeHSN() {
         return optimizeHSN;
     }
 
     /**
      * @param optimizeHsn the optimizeHsn to set
      */
     public void setOptimizeHSN(boolean optimizeHSN) {
         this.optimizeHSN = optimizeHSN;
     }
 
     /**
      * @return the optimizeMaio
      */
     public boolean isOptimizeMAIO() {
         return optimizeMAIO;
     }
 
     /**
      * @param optimizeMaio the optimizeMaio to set
      */
     public void setOptimizeMAIO(boolean optimizeMAIO) {
         this.optimizeMAIO = optimizeMAIO;
     }
 
     /**
      * Array: 0- Frequencies 1- BSIC 2- HSN 3- MAIO
      * 
      * @return boolean array containing all optimization params
      */
     public boolean[] getOptimizationParameters() {
         return new boolean[] {isOptimizeFrequency(), isOptimizeBSIC(), isOptimizeHSN(), isOptimizeMAIO()};
     }
 
     /**
      * @return the frequencyBands
      */
     public boolean[] getFrequencyBands() {
         return frequencyBands;
     }
 
     public int[] getAvailableFrequencyBandsIndexs() {
         int cnt = 0;
         for (int i = 0; i < this.frequencyBands.length; i++)
             if (this.frequencyBands[i])
                 cnt++;
 
         int ret[] = new int[cnt];
         for (int i = 0, j = 0; i < this.frequencyBands.length; i++) {
             if (this.frequencyBands[i]) {
                 ret[j] = i;
                 j++;
             }
         }
         return ret;
     }
 
     public boolean isFrequencyBandAvaliable(int band) {
         if (band >= 0 && band < 4) {
             return frequencyBands[band];
         }
         return false;
     }
 
     /**
      * @param frequencyBands the frequencyBands to set
      */
     public void setFrequencyBands(boolean[] frequencyBands) {
         this.frequencyBands = frequencyBands;
         for (int i = 0; i < 4; i++) {
             if (!this.frequencyBands[i]) {
                 this.availableFreq[i] = "";
             }
         }
     }
 
     /**
      * @return the channeltypes
      */
     public boolean[] getChanneltypes() {
         return channelTypes;
     }
 
     /**
      * @param channeltypes the channeltypes to set
      */
     public void setChanneltypes(boolean[] channeltypes) {
         this.channelTypes = channeltypes;
     }
 
     /**
      * @return the analyzeCurrentFreqAllocation
      */
     public boolean isAnalyzeCurrentFreqAllocation() {
         return analyzeCurrentFreqAllocation;
     }
 
     /**
      * @param analyzeCurrentFreqAllocation the analyzeCurrentFreqAllocation to set
      */
     public void setAnalyzeCurrentFreqAllocation(boolean analyzeCurrentFreqAllocation) {
         this.analyzeCurrentFreqAllocation = analyzeCurrentFreqAllocation;
     }
 
     /**
      * @return the availableFreq900
      */
     public String getAvailableFreq(int band) {
         if (band >= 0 && band <= 4) {
             if (availableFreq[band] != null) {
                 return availableFreq[band];
             }
         }
         return "";
     }
 
     public String getAvailableFreq(String bandName) {
         for (int i = 0; i < BAND_NAMES.length; i++) {
             if (BAND_NAMES[i].compareTo(bandName) == 0) {
                 if (availableFreq[i] != null) {
                     return availableFreq[i];
                 }
             }
         }
         return "";
     }
 
     /**
      * @param availableFreq900 the availableFreq900 to set
      */
     public void setAvailableFreq(int band, String freq) {
         if (band >= 0 && band < 4) {
             availableFreq[band] = freq;
         }
     }
 
     /**
      * @return the availableNCCs
      */
     public boolean[] getAvailableNCCs() {
         boolean ret[] = new boolean[] {true, true, true, true, true, true, true, true};
         for (int i = 0; i < 8; i++) {
             ret[i] = ((availableNCCs & (1 << i)) > 0);
         }
         return ret;
     }
 
     /**
      * @param availableNCCs the availableNCCs to set
      */
     public void setAvailableNCCs(boolean[] availableNCCs) {
         int n = 0;
         for (int i = 0; i < availableNCCs.length; i++) {
             if (availableNCCs[i]) {
                 n = n | (1 << i);
             }
         }
         this.availableNCCs = n;
     }
 
     /**
      * @return the availableBCCs
      */
     public boolean[] getAvailableBCCs() {
         boolean ret[] = new boolean[] {true, true, true, true, true, true, true, true};
         for (int i = 0; i < 8; i++) {
             ret[i] = ((availableBCCs & (1 << i)) > 0);
         }
         return ret;
     }
 
     /**
      * @param availableBCCs the availableBCCs to set
      */
     public void setAvailableBCCs(boolean[] availableBCCs) {
         int n = 0;
         for (int i = 0; i < availableBCCs.length; i++) {
             if (availableBCCs[i]) {
                 n = n | (1 << i);
             }
         }
         this.availableBCCs = n;
     }
     
     private AfpFrequencyDomainModel createFreeFreqDomain(int index) {
         AfpFrequencyDomainModel d = freeFrequencyDomains.get(DEFAULT_BAND_NAMES[index]);
         if (d != null) {
             return d;
         }
         
         d = new AfpFrequencyDomainModel();
         d.setName(DEFAULT_BAND_NAMES[index]);
         d.setBand(BAND_NAMES[index]);
         d.setNumTRX(0);
         d.setFree(true);
         
         //filter for band
         AfpFilter filter = new AfpFilter();
         filter.addFilter("flike", "band", ".*" + d.getBand() + ".*");
         
         d.setFilters(filter.toString());
         
         String f[] = new String[1];
         f[0] = this.availableFreq[index];
         d.setFrequencies(f);
         
         freeFrequencyDomains.put(d.getName(), d);
         
         return d;
     }
 
     private void addRemoveFreeFrequencyDomain(boolean addFree) {
         for (int i = 0; i < frequencyBands.length; i++) {
             if (frequencyBands[i]) {
                 // add free domains
                 if (addFree) {
                     AfpFrequencyDomainModel d = createFreeFreqDomain(i);
                     freqDomains.put(d.getName(), d);
                 } else {
                     freqDomains.remove("Free " + BAND_NAMES[i]);
                 }
             }
         }
     }
     
     public void addFrequencyDomainToQueue(AfpFrequencyDomainModel freqDomain) {
         if (freqDomainQueue.contains(freqDomain)) {
             freqDomainQueue.remove(freqDomain);
         }
         freqDomainQueue.add(freqDomain);
     }
     
     public void removeFrequencyDomainFromQueue(AfpFrequencyDomainModel freqDomain) {
         freqDomainQueue.remove(freqDomain);
     }
     
     public Queue<AfpFrequencyDomainModel> getFrequencyDomainQueue() {
         return freqDomainQueue;
     }
 
     /*
      * private int getTotalSelectedTRXCount(){ int totalSelected = 0; for (AfpFrequencyDomainModel
      * dm: freqDomains.values()){ totalSelected += dm.getNumTRX(); } return totalSelected; }
      */
 
     public AfpDomainModel findDomainByName(String type, String name) {
         AfpDomainModel model = null;
         if (type.equals(DOMAIN_TYPES[0])) {
             for (AfpFrequencyDomainModel freqModel : freqDomains.values()) {
                 if (freqModel.getName().equals(name)) {
                     model = freqModel;
                     break;
                 }
             }
         }
 
         if (type.equals(DOMAIN_TYPES[1])) {
             for (AfpHoppingMALDomainModel malModel : malDomains.values()) {
                 if (malModel.getName().equals(name)) {
                     model = malModel;
                     break;
                 }
             }
         }
         if (type.equals(DOMAIN_TYPES[2])) {
             for (AfpSeparationDomainModel malModel : this.sectorSeparationDomains.values()) {
                 if (malModel.getName().equals(name)) {
                     model = malModel;
                     break;
                 }
             }
         }
         if (type.equals(DOMAIN_TYPES[3])) {
             for (AfpSeparationDomainModel malModel : this.siteSeparationDomains.values()) {
                 if (malModel.getName().equals(name)) {
                     model = malModel;
                     break;
                 }
             }
         }
 
         return model;
     }
     
     private Collection<AfpFrequencyDomainModel> getAllFrequencyDomains() {
         ArrayList<AfpFrequencyDomainModel> domains = new ArrayList<AfpFrequencyDomainModel>();
         
         for (AfpFrequencyDomainModel singleDomain : freqDomainQueue) {
             domains.add(singleDomain);
         }
         
         for (AfpFrequencyDomainModel singleDomain : freqDomains.values()) {
             if (!freqDomainQueue.contains(singleDomain)) {
                 domains.add(singleDomain);
             }
         }
         
         return domains;
     }
     
     public Collection<AfpFrequencyDomainModel> getFreeFreqDomains() {
         ArrayList<AfpFrequencyDomainModel> result = new ArrayList<AfpFrequencyDomainModel>();
         
         for (int i = 0; i < frequencyBands.length; i++) {
             if (frequencyBands[i]) {
                 result.add(createFreeFreqDomain(i));
             }
         }
         
         return result;
     }
 
     /**
      * @return the freqDomains
      */
     public Collection<AfpFrequencyDomainModel> getFreqDomains(boolean addFree) {
         addRemoveFreeFrequencyDomain(addFree);
         ArrayList<AfpFrequencyDomainModel> l = new ArrayList<AfpFrequencyDomainModel>();
         for (AfpFrequencyDomainModel d : freqDomains.values()) {
             l.add(d/*new AfpFrequencyDomainModel(d)*/);
         }
        
         Collections.sort(l, new Comparator<AfpDomainModel>() {
 
             @Override
             public int compare(AfpDomainModel arg0, AfpDomainModel arg1) {
                 for (String name : DEFAULT_BAND_NAMES) {
                     if (arg0.getName().equals(name))
                         return -1;
                     else if (arg1.getName().equals(name))
                         return 1;
                 }
                 return 0;
             }
 
         });
 
 
         return l;
     }
 
     /*
      * public int[] getFreqDomainsTrxCount(boolean addFree) { Collection<AfpFrequencyDomainModel>
      * domains = getFreqDomains(true); int counters[] = new int[domains.size()]; int cnt=0;
      * for(AfpFrequencyDomainModel d: domains) { String filterStr =d.getFilters(); if( filterStr !=
      * null) { filters.add(AfpFilter.getFilter(filterStr)); } else { filters.add(null); }
      * counters[cnt] =0; cnt++; } return counters; }
      */
 
     /**
      * @param freqDomains the freqDomains to set
      */
     public void setFreqDomains(HashMap<String, AfpFrequencyDomainModel> freqDomains) {
         this.freqDomains = freqDomains;
     }
 
     private void addDefaultMalDomains() {
         // add free domains
         AfpHoppingMALDomainModel d = new AfpHoppingMALDomainModel();
         d.setName(DEFAULT_MAL_NAME);
         d.setFree(true);
         // d.setNumTRX(totalRemainingMalTRX);
         malDomains.put(d.getName(), d);
     }
 
     /**
      * @return the malDomains
      */
     public Collection<AfpHoppingMALDomainModel> getMalDomains(boolean getFree) {
         addDefaultMalDomains();
         ArrayList<AfpHoppingMALDomainModel> l = new ArrayList<AfpHoppingMALDomainModel>();
         for (AfpHoppingMALDomainModel d : this.malDomains.values()) {
             if (!getFree)
                 if (d.getName().equals(DEFAULT_MAL_NAME))
                     continue;
             l.add(new AfpHoppingMALDomainModel(d));
         }
 
         Collections.sort(l, new Comparator<AfpDomainModel>() {
 
             @Override
             public int compare(AfpDomainModel arg0, AfpDomainModel arg1) {
                 if (arg0.getName().equals(DEFAULT_MAL_NAME))
                     return -1;
                 else if (arg1.getName().equals(DEFAULT_MAL_NAME))
                     return 1;
                 return arg0.getName().compareTo(arg1.getName());
             }
 
         });
         return l;
     }
 
     private void addDefaultSiteSeparationDomains() {
         // add free domains
         AfpSeparationDomainModel d = new AfpSeparationDomainModel();
         d.setName(DEFAULT_SITE_SEP_NAME);
         d.setFree(true);
         siteSeparationDomains.put(d.getName(), d);
     }
 
     /**
      * @return the siteSeparationDomains
      */
     public Collection<AfpSeparationDomainModel> getSiteSeparationDomains(boolean getFree) {
         addDefaultSiteSeparationDomains();
         ArrayList<AfpSeparationDomainModel> l = new ArrayList<AfpSeparationDomainModel>();
         for (AfpSeparationDomainModel d : this.siteSeparationDomains.values()) {
             if (!getFree) {
                 if (d.getName().equals(DEFAULT_SECTOR_SEP_NAME))
                     continue;
             }
             l.add(new AfpSeparationDomainModel(d));
         }
 
         Collections.sort(l, new Comparator<AfpDomainModel>() {
 
             @Override
             public int compare(AfpDomainModel arg0, AfpDomainModel arg1) {
                 if (arg0.getName().equals(DEFAULT_SITE_SEP_NAME))
                     return -1;
                 else if (arg1.getName().equals(DEFAULT_SITE_SEP_NAME))
                     return 1;
                 return arg0.getName().compareTo(arg1.getName());
             }
 
         });
         return l;
     }
 
     /**
      * @param siteSeparationDomains the siteSeparationDomains to set
      */
     public void setSiteSeparationDomains(HashMap<String, AfpSeparationDomainModel> siteSeparationDomains) {
         this.siteSeparationDomains = siteSeparationDomains;
     }
 
     private void addDefaultSectorSeparationDomains() {
         // add free domains
         AfpSeparationDomainModel d = new AfpSeparationDomainModel();
         d.setName(DEFAULT_SECTOR_SEP_NAME);
         d.setFree(true);
         sectorSeparationDomains.put(d.getName(), d);
     }
 
     /**
      * @return the sectorSeparationDomains
      */
     public Collection<AfpSeparationDomainModel> getSectorSeparationDomains(boolean getFree) {
         addDefaultSectorSeparationDomains();
         ArrayList<AfpSeparationDomainModel> l = new ArrayList<AfpSeparationDomainModel>();
         for (AfpSeparationDomainModel d : this.sectorSeparationDomains.values()) {
             if (!getFree)
                 if (d.getName().equals(DEFAULT_SECTOR_SEP_NAME))
                     continue;
 
             l.add(new AfpSeparationDomainModel(d));
         }
 
         Collections.sort(l, new Comparator<AfpDomainModel>() {
 
             @Override
             public int compare(AfpDomainModel arg0, AfpDomainModel arg1) {
                 if (arg0.getName().equals(DEFAULT_SECTOR_SEP_NAME))
                     return -1;
                 else if (arg1.getName().equals(DEFAULT_SECTOR_SEP_NAME))
                     return 1;
                 return arg0.getName().compareTo(arg1.getName());
             }
 
         });
 
         return l;
     }
 
     /*
      * public Collection<AfpDomainModel> sortDomainModelsByName(ArrayList<AfpDomainModel>
      * modelList){ Collections.sort(modelList, new Comparator<AfpDomainModel>(){
      * @Override public int compare(AfpDomainModel arg0, AfpDomainModel arg1) { return
      * arg0.getName().compareTo(arg1.getName()); } }); return modelList; }
      */
 
     /**
      * @param sectorSeparationDomains the sectorSeparationDomains to set
      */
     public void setSectorSeparationDomains(HashMap<String, AfpSeparationDomainModel> sectorSeparationDomains) {
         this.sectorSeparationDomains = sectorSeparationDomains;
     }
 
     public void updateFreqDomain(AfpDomainModel model) {
         for (AfpDomainModel dm : freqDomains.values()) {
             if (dm.getName().equals(model.getName())) {
                 dm = model;
                 break;
             }
         }
     }
 
     public void updateMalDomain(AfpDomainModel model) {
         for (AfpDomainModel dm : malDomains.values()) {
             if (dm.getName().equals(model.getName())) {
                 dm = model;
                 break;
             }
         }
     }
 
     public void updateSectorSepDomain(AfpDomainModel model) {
         for (AfpDomainModel dm : sectorSeparationDomains.values()) {
             if (dm.getName().equals(model.getName())) {
                 dm = model;
                 break;
             }
         }
     }
 
     public void updateSiteSepDomain(AfpDomainModel model) {
         for (AfpDomainModel dm : siteSeparationDomains.values()) {
             if (dm.getName().equals(model.getName())) {
                 dm = model;
                 break;
             }
         }
     }
 
     /**
      * @param malDomains the malDomains to set
      */
     public void setMalDomains(HashMap<String, AfpHoppingMALDomainModel> malDomains) {
         this.malDomains = malDomains;
     }
 
     /**
      * @return the sectorSeparation
      */
     public float[] getSectorSeparation() {
         return sectorSeparation;
     }
 
     /**
      * @param sectorSeparation the sectorSeparation to set
      */
     public void setSectorSeparation(float[] sectorSeparation) {
         this.sectorSeparation = sectorSeparation;
     }
 
     /**
      * @return the siteSeparation
      */
     public float[] getSiteSeparation() {
         return siteSeparation;
     }
 
     /**
      * @param siteSeparation the siteSeparation to set
      */
     public void setSiteSeparation(float[] siteSeparation) {
         this.siteSeparation = siteSeparation;
     }
 
     /**
      * @return the coInterference
      */
     public float[] getCoInterference(String interferenceList) {
         ScalingFactors result = findScalingFactor(NodeToNodeTypes.INTERFERENCE_MATRIX, interferenceList);
         return result == null || result.getCo() == null ? coInterferenceDef : result.getCo();
     }
 
     public ScalingFactors findScalingFactor(NodeToNodeTypes type, String listName) {
         Map<String, ScalingFactors> map = scaling.get(type);
         return map == null ? null : map.get(listName);
     }
 
     /**
      * @param coInterference the coInterference to set
      */
     public void setCoInterference(String interferenceList, float[] coInterference) {
         ScalingFactors result = getScalingFactor(NodeToNodeTypes.INTERFERENCE_MATRIX, interferenceList);
         result.setCo(coInterference);
     }
 
     private ScalingFactors getScalingFactor(NodeToNodeTypes type, String listName) {
         Map<String, ScalingFactors> map = scaling.get(type);
         if (map == null) {
             map = new HashMap<String, AfpModel.ScalingFactors>();
             scaling.put(type, map);
         }
         ScalingFactors result = map.get(listName);
         if (result == null) {
             result = new ScalingFactors();
             map.put(listName, result);
         }
         return result;
     }
 
     /**
      * @return the adjInterference
      */
     public float[] getAdjInterference(String interferenceList) {
         ScalingFactors result = findScalingFactor(NodeToNodeTypes.INTERFERENCE_MATRIX, interferenceList);
         return result == null || result.getAdj() == null ? adjInterferenceDef : result.getAdj();
     }
 
     /**
      * @param adjInterference the adjInterference to set
      */
     public void setAdjInterference(String interferenceList, float[] adjInterference) {
         ScalingFactors result = getScalingFactor(NodeToNodeTypes.INTERFERENCE_MATRIX, interferenceList);
         result.setAdj(adjInterference);
     }
 
     /**
      * @return the coNeighbor
      */
     public float[] getCoNeighbor(String neighbourList) {
         ScalingFactors result = findScalingFactor(NodeToNodeTypes.NEIGHBOURS, neighbourList);
         return result == null || result.getCo() == null ? coNeighborDef : result.getCo();
     }
 
     /**
      * @param coNeighbor the coNeighbor to set
      */
     public void setCoNeighbor(String neighbourList, float[] coNeighbor) {
         ScalingFactors result = getScalingFactor(NodeToNodeTypes.NEIGHBOURS, neighbourList);
         result.setCo(coNeighbor);
     }
 
     /**
      * @return the adjNeighbor
      */
     public float[] getAdjNeighbor(String neighbourList) {
         ScalingFactors result = findScalingFactor(NodeToNodeTypes.NEIGHBOURS, neighbourList);
         return result == null || result.getAdj() == null ? adjNeighborDef : result.getAdj();
     }
 
     /**
      * @param adjNeighbor the adjNeighbor to set
      */
     public void setAdjNeighbor(String neighbourList, float[] adjNeighbor) {
         ScalingFactors result = getScalingFactor(NodeToNodeTypes.NEIGHBOURS, neighbourList);
         result.setAdj(adjNeighbor);
     }
 
     /**
      * @return the coTriangulation
      */
     public float[] getCoTriangulation(String triangList) {
         ScalingFactors result = findScalingFactor(NodeToNodeTypes.TRIANGULATION, triangList);
         return result == null || result.getCo() == null ? coTriangulationDef : result.getCo();
     }
 
     /**
      * @param coTriangulation the coTriangulation to set
      */
     public void setCoTriangulation(String triangList, float[] coTriangulation) {
         ScalingFactors result = getScalingFactor(NodeToNodeTypes.TRIANGULATION, triangList);
         result.setCo(coTriangulation);
     }
 
     /**
      * @return the adjTriangulation
      */
     public float[] getAdjTriangulation(String triangList) {
         ScalingFactors result = findScalingFactor(NodeToNodeTypes.TRIANGULATION, triangList);
         return result == null || result.getAdj() == null ? adjTriangulationDef : result.getAdj();
     }
 
     /**
      * @param adjTriangulation the adjTriangulation to set
      */
     public void setAdjTriangulation(String triangList, float[] adjTriangulation) {
         ScalingFactors result = getScalingFactor(NodeToNodeTypes.TRIANGULATION, triangList);
         result.setAdj(adjTriangulation);
     }
 
     /**
      * @return the coShadowing
      */
     public float[] getCoShadowing(String shadowList) {
         ScalingFactors result = findScalingFactor(NodeToNodeTypes.SHADOWING, shadowList);
         return result == null || result.getCo() == null ? coShadowingDef : result.getCo();
     }
 
     /**
      * @param coShadowing the coShadowing to set
      */
     public void setCoShadowing(String shadowList, float[] coShadowing) {
         ScalingFactors result = getScalingFactor(NodeToNodeTypes.SHADOWING, shadowList);
         result.setCo(coShadowing);
     }
 
     /**
      * @return the adjShadowing
      */
     public float[] getAdjShadowing(String shadowList) {
         ScalingFactors result = findScalingFactor(NodeToNodeTypes.SHADOWING, shadowList);
         return result == null || result.getAdj() == null ? adjShadowingDef : result.getAdj();
     }
 
     /**
      * @param adjShadowing the adjShadowing to set
      */
     public void setAdjShadowing(String shadowList, float[] adjShadowing) {
         ScalingFactors result = getScalingFactor(NodeToNodeTypes.SHADOWING, shadowList);
         result.setAdj(adjShadowing);
     }
 
     public String[] getTableItems() {
         return tableItems;
     }
 
     public void setTableItems(String[] tableItems) {
         this.tableItems = tableItems;
     }
 
     public HashMap<String, String> getFilters() {
         return filters;
     }
 
     public void setFilters(HashMap<String, String> filters) {
         this.filters = filters;
     }
 
     public void addFreqDomain(AfpFrequencyDomainModel freqDomain) {
         if (freqDomains.containsKey(freqDomain.getName())) {
             freqDomain.setName(freqDomain.getName() + "-1");
         }
         freqDomains.put(freqDomain.getName(), freqDomain);
         
     }
 
     public void editFreqDomain(AfpFrequencyDomainModel freqDomain) {
         freqDomains.put(freqDomain.getName(), freqDomain);
     }
 
     public void deleteFreqDomain(String name) {
         if (freqDomains == null) {
             return;
         }
         
         if (freqDomains.containsKey(name)) {
             removeFrequencyDomainFromQueue(freqDomains.get(name));
             freqDomains.remove(name);
         }
         
     }
 
     public AfpFrequencyDomainModel findFreqDomain(String domainName) {
         return freqDomains.get(domainName);
     }
 
     public String[] getAllFrequencyDomainNames() {
         String[] names = new String[freqDomains.size()];
         int i = 0;
         for (AfpFrequencyDomainModel freqDomain : this.getFreqDomains(false)) {
             if (!freqDomain.isFree()) {
                 names[i] = freqDomain.getName();
                 i++;
             }
         }
         String[] ret = new String[i];
         for (int j = 0; j < i; j++) {
             ret[j] = names[j];
         }
         return ret;
     }
 
     public String[] getAvailableBands() {
         int length = 0;
         for (boolean isEnabled : frequencyBands) {
             if (isEnabled)
                 length++;
         }
         String[] bands = new String[length];
 
         int i = 0;
         if (frequencyBands[0]) {
             bands[i] = "900";
             i++;
         }
         if (frequencyBands[1]) {
             bands[i] = "1800";
             i++;
         }
         if (frequencyBands[2]) {
             bands[i] = "850";
             i++;
         }
         if (frequencyBands[3]) {
             bands[i] = "1900";
         }
 
         return bands;
     }
 
     public void addMALDomain(AfpHoppingMALDomainModel malDomain) {
         if (malDomains.containsKey(malDomain.getName())) {
             malDomain.setName(malDomain.getName() + "-1");
         }
         malDomains.put(malDomain.getName(), malDomain);
     }
 
     public void editMALDomain(AfpHoppingMALDomainModel malDomain) {
         malDomains.put(malDomain.getName(), malDomain);
     }
 
     public void deleteMALDomain(AfpHoppingMALDomainModel malDomain) {
         if (malDomains == null) {
             // TODO error handling
             return;
         }
         if (malDomain.isFree())
             return;
 
         if (malDomains.containsKey(malDomain.getName()))
             malDomains.remove(malDomain.getName());
     }
 
     public AfpHoppingMALDomainModel findMALDomain(String domainName) {
         for (AfpHoppingMALDomainModel malDomain : malDomains.values()) {
             if (malDomain.getName().equals(domainName))
                 return malDomain;
         }
         return null;
     }
 
     public String[] getAllMALDomainNames() {
         String[] names = new String[malDomains.size()];
         int i = 0;
         for (AfpHoppingMALDomainModel malDomain : getMalDomains(true)) {
             names[i] = malDomain.getName();
             i++;
         }
         return names;
     }
 
     public void addSiteSeparationDomain(AfpSeparationDomainModel separationDomain) {
         if (siteSeparationDomains.containsKey(separationDomain.getName())) {
             separationDomain.setName(separationDomain.getName() + "-1");
         }
         siteSeparationDomains.put(separationDomain.getName(), separationDomain);
     }
 
     public void editSiteSeparationDomain(AfpSeparationDomainModel domain) {
         siteSeparationDomains.put(domain.getName(), domain);
     }
 
     public void deleteSiteSeparationDomain(AfpSeparationDomainModel separationDomain) {
         if (separationDomain == null) {
             // TODO error handling
         }
         if (siteSeparationDomains.containsKey(separationDomain.getName()))
             siteSeparationDomains.remove(separationDomain.getName());
     }
 
     public AfpSeparationDomainModel findSiteSeparationDomain(String domainName) {
         if (siteSeparationDomains.containsKey(domainName))
             return siteSeparationDomains.get(domainName);
         return null;
     }
 
     public String[] getAllSiteSeparationDomainNames() {
         String[] names = new String[siteSeparationDomains.size()];
         int i = 0;
         for (AfpSeparationDomainModel separationDomain : siteSeparationDomains.values()) {
             names[i] = separationDomain.getName();
             i++;
         }
         return names;
     }
 
     public void addSectorSeparationDomain(AfpSeparationDomainModel separationDomain) {
         if (sectorSeparationDomains.containsKey(separationDomain.getName())) {
             separationDomain.setName(separationDomain.getName() + "-1");
         }
         sectorSeparationDomains.put(separationDomain.getName(), separationDomain);
     }
 
     public void editSectorSeparationDomain(AfpSeparationDomainModel domain) {
         sectorSeparationDomains.put(domain.getName(), domain);
     }
 
     public void deleteSectorSeparationDomain(AfpSeparationDomainModel domain) {
         if (domain == null) {
             // TODO error handling
         }
         if (sectorSeparationDomains.containsKey(domain.getName()))
             sectorSeparationDomains.remove(domain.getName());
     }
 
     public AfpSeparationDomainModel findSectorSeparationDomain(String domainName) {
         if (sectorSeparationDomains.containsKey(domainName)) {
             return sectorSeparationDomains.get(domainName);
         }
         return null;
     }
 
     public String[] getAllSectorSeparationDomainNames() {
         String[] names = new String[sectorSeparationDomains.size()];
         int i = 0;
         for (AfpSeparationDomainModel separationDomain : sectorSeparationDomains.values()) {
             names[i] = separationDomain.getName();
             i++;
         }
         return names;
     }
 
     /**
      * Write all user selected data to database
      */
     public void saveUserData() {
         Job saveData = new Job("save user data") {
 
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 GraphDatabaseService service = datasetNode.getGraphDatabase();
                 Transaction tx = service.beginTx();
                 try {
                     if (afpNode == null) {
                         afpNode = service.createNode();
                         NodeTypes.AFP.setNodeType(afpNode, service);
                         NeoUtils.setNodeName(afpNode, AFP_NODE_NAME, service);
                         datasetNode.createRelationshipTo(afpNode, NetworkRelationshipTypes.CHILD);
                     }
                     afpNode.setProperty(INeoConstants.AFP_OPTIMIZATION_PARAMETERS, getOptimizationParameters());
                     afpNode.setProperty(INeoConstants.AFP_FREQUENCY_BAND, getFrequencyBands());
                     afpNode.setProperty(INeoConstants.AFP_CHANNEL_TYPE, getChanneltypes());
                     afpNode.setProperty(INeoConstants.AFP_ANALYZE_CURRENT, isAnalyzeCurrentFreqAllocation());
                     if (getAvailableFreq(BAND_900) != null)
                         afpNode.setProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_900, getAvailableFreq(BAND_900));
                     if (getAvailableFreq(BAND_1800) != null)
                         afpNode.setProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_1800, getAvailableFreq(BAND_1800));
 
                     if (getAvailableFreq(BAND_850) != null)
                         afpNode.setProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_850, getAvailableFreq(BAND_850));
                     if (getAvailableFreq(BAND_1900) != null)
                         afpNode.setProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_1900, getAvailableFreq(BAND_1900));
                     afpNode.setProperty(INeoConstants.AFP_AVAILABLE_BCCS, availableBCCs);
                     afpNode.setProperty(INeoConstants.AFP_AVAILABLE_NCCS, availableNCCs);
 
                     afpNode.setProperty(INeoConstants.AFP_SECTOR_SCALING_RULES, getSectorSeparation());
                     afpNode.setProperty(INeoConstants.AFP_SITE_SCALING_RULES, getSiteSeparation());
 
                     storeScalingFactor(datasetNode, afpNode);
 
                     // remove all chid nodes before storing new ones
                     /*
                      * Traverser traverser = afpNode.traverse(Order.DEPTH_FIRST,
                      * StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
                      * @Override public boolean isReturnableNode(TraversalPosition currentPos) { if
                      * (
                      * currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals
                      * (NodeTypes.AFP_DOMAIN.getId())) return true; return false; } },
                      * NetworkRelationshipTypes.CHILD, Direction.OUTGOING); for (Node n : traverser)
                      * { Iterable<Relationship> relationsI = n.getRelationships(); if(relationsI !=
                      * null) { Iterator<Relationship> reations = relationsI.iterator();
                      * while(reations.hasNext()) { Relationship r = reations.next(); r.delete(); } }
                      * n.delete(); }
                      */
                     HashMap<String, ArrayList<String>> domainNames = getDomainNodeNames(afpNode);
                     Integer order = 0;
                     DomainService domainService = new DomainService();
                     for (AfpFrequencyDomainModel frequencyModel : getFreqDomains(false)/*getAllFrequencyDomains()*/) {
                         if (!frequencyModel.isFree()) {
                             ArrayList<String> nameList = domainNames.get(INeoConstants.AFP_DOMAIN_NAME_FREQUENCY);
                             if (nameList != null) {
                                 nameList.remove(frequencyModel.getName());
                                 domainNames.put(INeoConstants.AFP_DOMAIN_NAME_FREQUENCY, nameList);
                             }
                             domainService.createFrequencyDomainNode(domainService.getFrequencyDomainsNode(afpNode), frequencyModel, order++);
                         }
                     }
                     for (AfpFrequencyDomainModel frequencyModel : getFrequencyDomainQueue()){
                         
                         domainService.addAssignRelation(domainService.getFrequencyDomainsNode(afpNode),frequencyModel);
                     }
 
                     for (AfpHoppingMALDomainModel malModel : getMalDomains(true)) {
                         ArrayList<String> nameList = domainNames.get(INeoConstants.AFP_DOMAIN_NAME_MAL);
                         if (nameList != null) {
                             nameList.remove(malModel.getName());
                             domainNames.put(INeoConstants.AFP_DOMAIN_NAME_MAL, nameList);
                         }
                         createHoppingMALDomainNode(afpNode, malModel, service);
                     }
 
                     for (AfpSeparationDomainModel separationsModel : getSectorSeparationDomains(true)) {
                         ArrayList<String> nameList = domainNames.get(INeoConstants.AFP_DOMAIN_NAME_SECTOR_SEPARATION);
                         if (nameList != null) {
                             nameList.remove(separationsModel.getName());
                             domainNames.put(INeoConstants.AFP_DOMAIN_NAME_SECTOR_SEPARATION, nameList);
                         }
                         createSectorSeparationDomainNode(afpNode, separationsModel, service);
                     }
 
                     for (AfpSeparationDomainModel separationsModel : getSiteSeparationDomains(true)) {
                         ArrayList<String> nameList = domainNames.get(INeoConstants.AFP_DOMAIN_NAME_SITE_SEPARATION);
                         if (nameList != null) {
                             nameList.remove(separationsModel.getName());
                             domainNames.put(INeoConstants.AFP_DOMAIN_NAME_SITE_SEPARATION, nameList);
                         }
                         createSiteSeparationDomainNode(afpNode, separationsModel, service);
                     }
 
                     // delete domains which are not present now
                     for (String type : domainNames.keySet()) {
                         for (String name : domainNames.get(type)) {
                             deleteDomainNode(afpNode, type, name, service);
                         }
                     }
                     tx.success();
                 } catch (Exception e) {
                     e.printStackTrace();
                     AweConsolePlugin.exception(e);
                     return new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
                 } finally {
                     tx.finish();
                 }
 
                 return Status.OK_STATUS;
             }
 
         };
         saveData.schedule();
         try {
             saveData.join();
         } catch (InterruptedException e) {
             // TODO Handle InterruptedException
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * @param datasetNode2
      * @param afpNode2
      */
     private void storeScalingFactor(Node datasetNode, Node afpNode) {
         for (NodeToNodeTypes type : scaling.keySet()) {
             final Map<String, ScalingFactors> map = scaling.get(type);
             if (map == null) {
                 continue;
             }
             for (Entry<String, ScalingFactors> entry : map.entrySet()) {
                 saveScalingFactor(afpNode, type, entry.getKey(), entry.getValue());
             }
         }
     }
 
     private void saveScalingFactor(Node afpNode, NodeToNodeTypes type, String listName, ScalingFactors factors) {
         if (type == null || listName == null || factors == null) {
             return;
         }
         Transaction tx = afpNode.getGraphDatabase().beginTx();
         try {
             Node fcNode = getScalingNode(afpNode, listName, type);
             fcNode.setProperty("coArr", factors.getCo());
             fcNode.setProperty("adjArr", factors.getAdj());
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     private void loadUserData() {
         loadScalingFactors(datasetNode, afpNode);
         try {
             if (afpNode == null) {
                 return;
             }
             try {
                 boolean opt[] = (boolean[])afpNode.getProperty(INeoConstants.AFP_OPTIMIZATION_PARAMETERS);
                 if (opt != null) {
                     if (opt.length >= 4) {
                         this.optimizeFrequency = opt[0];
                         this.optimizeBSIC = opt[1];
                         this.optimizeHSN = opt[2];
                         this.optimizeMAIO = opt[3];
                     }
                 }
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 setFrequencyBands((boolean[])afpNode.getProperty(INeoConstants.AFP_FREQUENCY_BAND));
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 setChanneltypes((boolean[])afpNode.getProperty(INeoConstants.AFP_CHANNEL_TYPE));
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 setAnalyzeCurrentFreqAllocation((Boolean)afpNode.getProperty(INeoConstants.AFP_ANALYZE_CURRENT));
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 setAvailableFreq(BAND_900, (String)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_900));
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 setAvailableFreq(BAND_1800, (String)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_1800));
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 setAvailableFreq(BAND_850, (String)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_850));
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 setAvailableFreq(BAND_1900, (String)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_1900));
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 Integer n = (Integer)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_BCCS);
                 this.availableBCCs = n.intValue();
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 Integer n = (Integer)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_NCCS);
                 this.availableNCCs = n.intValue();
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 setSectorSeparation((float[])afpNode.getProperty(INeoConstants.AFP_SECTOR_SCALING_RULES));
             } catch (Exception e) {
                 // no property sent
             }
             try {
                 setSiteSeparation((float[])afpNode.getProperty(INeoConstants.AFP_SITE_SCALING_RULES));
             } catch (Exception e) {
                 // no property sent
             }
 
             loadDomainNode(afpNode);
             /*
              * for (AfpHoppingMALDomainModel malModel : getMalDomains()) {
              * createHoppingMALDomainNode(afpNode, malModel, service); } for
              * (AfpSeparationDomainModel separationsModel : getSectorSeparationDomains()) {
              * createSectorSeparationDomainNode(afpNode, separationsModel, service); } for
              * (AfpSeparationDomainModel separationsModel : getSiteSeparationDomains()) {
              * createSiteSeparationDomainNode(afpNode, separationsModel, service); }
              */
 
         } catch (Exception e) {
             AweConsolePlugin.exception(e);
         } finally {
         }
     }
 
     /**
      * @param datasetNode2
      * @param afpNode2
      */
     private void loadScalingFactors(Node datasetNode, Node afpNode) {
         scaling.clear();
         NetworkModel model = new NetworkModel(datasetNode);
         NodeToNodeTypes[] types = new NodeToNodeTypes[] {NodeToNodeTypes.INTERFERENCE_MATRIX, NodeToNodeTypes.NEIGHBOURS,
                 NodeToNodeTypes.SHADOWING, NodeToNodeTypes.TRIANGULATION};
         for (NodeToNodeTypes type : types) {
             for (NodeToNodeRelationModel md : model.findAllN2nModels(type)) {
                 loadscaling(afpNode, md);
             }
         }
 
     }
 
     /**
      * @param afpNode2
      * @param md
      */
     private void loadscaling(Node afpNode, NodeToNodeRelationModel md) {
         String name = md.getName();
         NodeToNodeTypes type = (NodeToNodeTypes)md.getType();
         Node scalList = findScalingNode(afpNode, name, type);
         float[] co;
         float[] adj;
         if (scalList == null) {
             switch (type) {
             case INTERFERENCE_MATRIX:
                 co = coInterferenceDef.clone();
                 adj = adjInterferenceDef.clone();
                 break;
             case NEIGHBOURS:
                 co = coNeighborDef.clone();
                 adj = adjNeighborDef.clone();
                 break;
             case TRIANGULATION:
                 co = coTriangulationDef.clone();
                 adj = adjTriangulationDef.clone();
                 break;
             case SHADOWING:
                 co = coShadowingDef.clone();
                 adj = adjShadowingDef.clone();
                 break;
             default:
                 throw new IllegalArgumentException("Wrong type " + type);
             }
         } else {
             co = (float[])scalList.getProperty("coArr");
             adj = (float[])scalList.getProperty("adjArr");
         }
         ScalingFactors fc = getScalingFactor(type, name);
         fc.setCo(co);
         fc.setAdj(adj);
     }
 
     /**
      * @param afpNode2
      * @param name
      * @param type
      * @return
      */
     private Node findScalingNode(Node afpNode, final String name, final NodeToNodeTypes type) {
         if (afpNode == null) {
             return null;
         }
         Iterator<Node> it = Traversal.description().depthFirst().relationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)
                 .evaluator(new Evaluator() {
 
                     @Override
                     public Evaluation evaluate(Path arg0) {
                         Node node = arg0.endNode();
                         boolean includes = arg0.length() == 1 && NodeTypes.AFP_SF.checkNode(node)
                                 && name.equals(ds.getNodeName(node)) && type.name().equals(node.getProperty("n2n"));
                         return Evaluation.of(includes, arg0.length() < 1);
                     }
                 }).traverse(afpNode).nodes().iterator();
         return it.hasNext() ? it.next() : null;
     }
 
     private Node getScalingNode(Node afpNode, final String name, final NodeToNodeTypes type) {
         Node result = findScalingNode(afpNode, name, type);
         if (result == null) {
             Transaction tx = afpNode.getGraphDatabase().beginTx();
             try {
                 result = ds.addSimpleChild(afpNode, NodeTypes.AFP_SF, name);
                 result.setProperty("n2n", type.name());
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
         return result;
     }
 
     
     public void createHoppingMALDomainNode(Node afpNode, AfpHoppingMALDomainModel domainModel, GraphDatabaseService service) {
         Node malNode = findOrCreateDomainNode(afpNode, INeoConstants.AFP_DOMAIN_NAME_MAL, domainModel.getName(), 0, service);
 
         if (domainModel.getFilters() != null) {
             malNode.setProperty(INeoConstants.AFP_PROPERTY_FILTERS_NAME, domainModel.getFilters());
         }
         malNode.setProperty(INeoConstants.AFP_PROPERTY_MAL_SIZE_NAME, domainModel.getMALSize());
         malNode.setProperty(INeoConstants.AFP_PROPERTY_TRX_COUNT_NAME, domainModel.getNumTRX());
     }
 
     public void createSectorSeparationDomainNode(Node afpNode, AfpSeparationDomainModel domainModel, GraphDatabaseService service) {
         Node separationNode = findOrCreateDomainNode(afpNode, INeoConstants.AFP_DOMAIN_NAME_SECTOR_SEPARATION,
                 domainModel.getName(), 0, service);
 
         if (domainModel.getFilters() != null) {
             separationNode.setProperty(INeoConstants.AFP_PROPERTY_FILTERS_NAME, domainModel.getFilters());
         }
         separationNode.setProperty(INeoConstants.AFP_PROPERTY_SEPARATIONS_NAME, domainModel.getSeparations());
     }
 
     public void createSiteSeparationDomainNode(Node afpNode, AfpSeparationDomainModel domainModel, GraphDatabaseService service) {
         Node separationNode = findOrCreateDomainNode(afpNode, INeoConstants.AFP_DOMAIN_NAME_SITE_SEPARATION, domainModel.getName(), 0,
                 service);
 
         if (domainModel.getFilters() != null) {
             separationNode.setProperty(INeoConstants.AFP_PROPERTY_FILTERS_NAME, domainModel.getFilters());
         }
         separationNode.setProperty(INeoConstants.AFP_PROPERTY_SEPARATIONS_NAME, domainModel.getSeparations());
     }
 
     public void deleteDomainNode(Node afpNode, final String domain, final String name, GraphDatabaseService service) {
         Traverser traverser = afpNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(NodeTypes.AFP_DOMAIN.getId())
                         && currentPos.currentNode().getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME).equals(domain)
                         && currentPos.currentNode().getProperty(INeoConstants.PROPERTY_NAME_NAME).equals(name))
                     return true;
                 return false;
             }
 
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
         for (Node n : traverser) {
             Iterable<Relationship> relationsI = n.getRelationships();
             if (relationsI != null) {
                 Iterator<Relationship> relations = relationsI.iterator();
                 while (relations.hasNext()) {
                     Relationship r = relations.next();
                     r.delete();
                 }
             }
             n.delete();
         }
     }
 
     
 
     
     
     public HashMap<String, ArrayList<String>> getDomainNodeNames(Node afpNode) {
         HashMap<String, ArrayList<String>> domainNames = new HashMap<String, ArrayList<String>>();
         Traverser traverser = afpNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(NodeTypes.AFP_DOMAIN.getId()))
                     return true;
                 return false;
             }
 
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
 
         for (Node node : traverser) {
             ArrayList<String> list = domainNames.get(node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME));
             if (list == null)
                 list = new ArrayList<String>();
             list.add((String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, ""));
             domainNames.put((String)node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME), list);
         }
         return domainNames;
 
     }
 
     public void setParameters() {
         parameters = new HashMap<String, String>();
         parameters.put(ControlFileProperties.SITE_SPACING, "2");
         parameters.put(ControlFileProperties.CELL_SPACING, "0");
         parameters.put(ControlFileProperties.REG_NBR_SPACING, "1");
         parameters.put(ControlFileProperties.MIN_NEIGBOUR_SPACING, "0");
         parameters.put(ControlFileProperties.SECOND_NEIGHBOUR_SPACING, "1");
         parameters.put(ControlFileProperties.QUALITY, "100");
         parameters.put(ControlFileProperties.G_MAX_RT_PER_CELL, "5");
         parameters.put(ControlFileProperties.G_MAX_RT_PER_SITE, "5");
         parameters.put(ControlFileProperties.HOPPING_TYPE, "1");
         parameters.put(ControlFileProperties.NUM_GROUPS, "6");
         parameters.put(ControlFileProperties.CELL_CARDINALITY, "61");
         StringBuffer carriers = new StringBuffer();
         int cnt = 0;
         boolean first = true;
         for (int i = 0; i < frequencyBands.length; i++) {
             if (frequencyBands[i]) {
                 String freq = this.availableFreq[i];
                 String[] franges = freq.split(",");
 
                 String[] freqList = rangeArraytoArray(franges);
                 for (String f : freqList) {
                     if (!first) {
                         carriers.append(",");
                     }
                     carriers.append(f);
                     cnt++;
                     first = false;
                 }
             }
         }
 
         parameters.put(ControlFileProperties.CARRIERS, carriers.toString());
         parameters.put(ControlFileProperties.USE_GROUPING, "1");
         parameters.put(ControlFileProperties.EXIST_CLIQUES, "0");
         parameters.put(ControlFileProperties.RECALCULATE_ALL, "1");
         parameters.put(ControlFileProperties.USE_TRAFFIC, "1");
         parameters.put(ControlFileProperties.USE_SO_NEIGHBOURS, "1");
         parameters.put(ControlFileProperties.DECOMPOSE_CLIQUES, "0");
     }
 
     public HashMap<String, String> getParameters() {
         if (parameters == null)
             setParameters();
         return parameters;
     }
 
     public AfpExporter getExporter() {
         AfpExporter exportJob = new AfpExporter(datasetNode, this.afpNode, this);
         return exportJob;
     }
 
     // public void getLoader(){
     // String outFileName = exportJob.domainDirPaths[0] + exportJob.outputFileName;
     // AfpOutputFileLoaderJob loadJob = new AfpOutputFileLoaderJob("Load generated Network",
     // datasetNode, null, afpNode);
     //
     // }
 
     public void executeAfpEngine(AfpProcessProgress progress, AfpExporter exportJob) {
         if (afpNode != null) {
             if (parameters == null) {
                 setParameters();
             }
             afpJob = new AfpProcessExecutor("Optimize plan for ", datasetNode, this.afpNode, parameters, this, exportJob);
             afpJob.setProgress(progress);
             // afpJob.schedule();
         }
     }
 
     private void loadDomainNode(Node afpNode) {
         Collection<Node> traverser = afpNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(NodeTypes.AFP_DOMAIN.getId()))
                     return true;
                 return false;
             }
 
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).getAllNodes();
         
         ArrayList<Node> nodes = new ArrayList<Node>();
         for (Node node : traverser) {
             nodes.add(node);
         }
        traverser = afpNode.getSingleRelationship(DomainRelations.DOMAINS, Direction.OUTGOING).getEndNode()
        .traverse(Order.BREADTH_FIRST,
                StopEvaluator.END_OF_GRAPH,
                ReturnableEvaluator.ALL_BUT_START_NODE,
                DomainRelations.NEXT,
                Direction.OUTGOING).getAllNodes();
        for (Node node : traverser) {
            nodes.add(node);
         }
        
         Collections.sort(nodes, new Comparator<Node>() {
 
             @Override
             public int compare(Node o1, Node o2) {
                 Integer order1 = (Integer)o1.getProperty("order", Integer.MAX_VALUE);
                 Integer order2 = (Integer)o2.getProperty("order", Integer.MAX_VALUE);
                 return order1.compareTo(order2);
             }
             
         });
         
         for (Node node : nodes) {
             if (node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME, "").equals(INeoConstants.AFP_DOMAIN_NAME_FREQUENCY)) {
                 // frequency type domain
                 AfpFrequencyDomainModel m = AfpFrequencyDomainModel.getModel(node);
                 if (m != null) {
                     this.addFreqDomain(m);
                     if ((m.getFilters() != null) && 
                         !m.getFilters().isEmpty()) {
                         addFrequencyDomainToQueue(m);
                     }
                 }
             } else if (node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME, "").equals(INeoConstants.AFP_DOMAIN_NAME_MAL)) {
                 // frequency type domain
                 AfpHoppingMALDomainModel m = AfpHoppingMALDomainModel.getModel(node);
                 if (m != null) {
                     addMALDomain(m);
                 }
             } else if (node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME, "").equals(
                     INeoConstants.AFP_DOMAIN_NAME_SECTOR_SEPARATION)) {
                 // frequency type domain
                 AfpSeparationDomainModel m = AfpSeparationDomainModel.getModel(node, INeoConstants.AFP_PROPERTY_SEPARATIONS_NAME);
                 if (m != null) {
                     this.addSectorSeparationDomain(m);
                 }
             } else if (node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME, "").equals(
                     INeoConstants.AFP_DOMAIN_NAME_SITE_SEPARATION)) {
                 // frequency type domain
                 AfpSeparationDomainModel m = AfpSeparationDomainModel.getModel(node, INeoConstants.AFP_PROPERTY_SEPARATIONS_NAME);
                 if (m != null) {
                     this.addSiteSeparationDomain(m);
                 }
             }
         }
     }
 
     public AfpProcessExecutor getExecutor() {
         return afpJob;
     }
 
     public Iterable<Node> getTRXList(final HashMap<String, String> filters) {
 
         Evaluator bandFilter = new Evaluator() {
 
             @Override
             public Evaluation evaluate(Path arg0) {
                 boolean include = false;
                 if (filters != null) {
                     for (String key : filters.keySet()) {
                         if (key.equals("band")) {
                             // TODO check correct
                             for (String band : filters.get(key).split(",")) {
                                 String bandStr = (String)arg0.endNode().getProperty("band", "");
                                 String layerStr = (String)arg0.endNode().getProperty("layer", "");
                                 if (bandStr.contains(band) || (bandStr.isEmpty() && layerStr.isEmpty()) || layerStr.contains(band))
                                     include = include || true;
                             }
                         }
                     }
                 } else {
                     include = true;
                 }
 
                 return Evaluation.of(include, true);
             }
         };
 
         return getElementTraverser(bandFilter, NodeTypes.SECTOR);
 
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("Optimization Goals:\n");
         if (isOptimizeFrequency())
             sb.append("Frequencies, ");
         if (isOptimizeBSIC())
             sb.append("BSIC, ");
         if (isOptimizeHSN())
             sb.append("HSN, ");
         if (isOptimizeMAIO())
             sb.append("MAIO");
 
         sb.append("\n\nFrequency Bands:\n");
         for (int i = 0; i < BAND_NAMES.length; i++) {
             if (frequencyBands[i]) {
                 sb.append("\t" + BAND_NAMES[i] + " : " + availableFreq[i] + "\n");
             }
         }
 
         sb.append("\nChannel Types: \n\t");
         for (int i = 0; i < CHANNEL_NAMES.length; i++) {
             if (channelTypes[i]) {
                 sb.append(CHANNEL_NAMES[i] + ", ");
             }
         }
 
         sb.append("\n\nSummary: \n");
 
         int[][] selectedCount = getSelectedCount();
         for (int i = 0; i < GOALS_SUMMARY_ROW_HEADERS.length; i++) {
             int total = 0;
             for (int j = 0; j < selectedCount[i].length; j++) {
                 total += selectedCount[i][j];
             }
             String s = String.format("\n\tTotal-%d \n\tBand 900-%d \n\tBand 1800-%d \n\tBand 850-%d \n\tBand 1900-%d", total,
                     selectedCount[i][0], selectedCount[i][1], selectedCount[i][2], selectedCount[i][3]);
             sb.append(GOALS_SUMMARY_ROW_HEADERS[i] + "  " + s + "\n");
         }
 
         sb.append("\nAvailable BSIC: \n");
         sb.append("\tNCCs: ");
         for (int i = 0; i < 8; i++) {
             if ((availableNCCs & (0x01 << i)) > 0)
                 sb.append(Integer.toString(i) + ",");
         }
 
         sb.append("\n\tBCCs: ");
         for (int i = 0; i < 8; i++) {
             if ((availableBCCs & (0x01 << i)) > 0)
                 sb.append(Integer.toString(i) + ",");
         }
 
         sb.append("\n\nFrequency Type Domains: \n");
         for (AfpFrequencyDomainModel domainModel : this.freqDomains.values()) {
             if (domainModel != null) {
                 sb.append(domainModel.getName() + " : \n\tBand-" + domainModel.getBand() + "\n\tAssigned Frequencies-"
                         + domainModel.getCount() + "\n");
                 sb.append("\tAssignedTRXs-" + domainModel.getNumTRX() + "\n\tFilters: " + domainModel.getFilters() + "\n");
             }
         }
 
         sb.append("\nMAL Domains: \n");
         for (AfpHoppingMALDomainModel malDomainModel : getMalDomains(true)) {
             if (malDomainModel != null) {
                 sb.append(malDomainModel.getName() + ":  AssignedTRXs-" + malDomainModel.getNumTRX() + "\n\tFilters: "
                         + malDomainModel.getFilters() + "\n");
             }
         }
 
         sb.append("\nSeparation Rules: \n");
         sb.append("Sector Domains: \n");
         for (AfpSeparationDomainModel sectorDomainModel : sectorSeparationDomains.values()) {
             if (sectorDomainModel != null) {
                 sb.append("\t" + sectorDomainModel.getName() + " : AssignedSectors- 0" + "\n");
             }
         }
 
         sb.append("Site Domains: \n");
         for (AfpSeparationDomainModel siteDomainModel : siteSeparationDomains.values()) {
             if (siteDomainModel != null) {
                 sb.append("\t" + siteDomainModel.getName() + " : AssignedSectors- 0" + "\n");
             }
         }
 
         sb.append("\nScaling Rules: \n");
         sb.append("Separations: \n");
         for (int i = 0; i < SCALING_PAGE_ROW_HEADERS.length; i++) {
             String s = String.format("\n\tSector-%3.1f \n\tSite-%3.1f", sectorSeparation[i], siteSeparation[i]);
             sb.append(SCALING_PAGE_ROW_HEADERS[i][0] + "-" + SCALING_PAGE_ROW_HEADERS[i][1] + " : " + s + "\n");
         }
 
         sb.append("\nInterference Matrices: \n");
         for (Entry<NodeToNodeTypes, Map<String, ScalingFactors>> entry : scaling.entrySet()) {
             if (entry.getValue() == null || entry.getValue().isEmpty()) {
                 continue;
             }
             sb.append("Type ").append(entry.getKey()).append('\n');
             for (Entry<String, ScalingFactors> scentry : entry.getValue().entrySet()) {
                 sb.append("List name: ").append(scentry.getKey()).append('\n');
                 for (int i = 0; i < SCALING_PAGE_ROW_HEADERS.length; i++) {
                     String s = String.format("\n\tCo-%3.1f, Adj-%3.1f", scentry.getValue().getCo()[i],
                             scentry.getValue().getAdj()[i]);
                     sb.append(SCALING_PAGE_ROW_HEADERS[i][0] + "-" + SCALING_PAGE_ROW_HEADERS[i][1] + " : " + s + "\n");
                 }
             }
         }
 
         return sb.toString();
     }
 
     public static Pair<String[], String[]> convertFreqString2Array(String frequenciesText, String frequencies[]) {
         int numSelected = 0;
         String[] frequenciesLeft = null;
         String[] selectedRanges = new String[] {};
 
         if (frequenciesText != null) {
             if (!frequenciesText.trim().equals(""))
                 selectedRanges = frequenciesText.split(",");
         }
 
         if (selectedRanges.length > 0 && selectedRanges[0] != null && !selectedRanges[0].trim().equals("")) {
             String[] selected = rangeArraytoArray(selectedRanges);
             numSelected = selected.length;
             frequenciesLeft = new String[frequencies.length - selected.length];
 
             Arrays.sort(selected);
             int i = 0;
             for (String item : frequencies) {
                 if (i >= frequenciesLeft.length)
                     break;
                 if (Arrays.binarySearch(selected, item) < 0) {
                     frequenciesLeft[i] = item;
                     i++;
                 }
             }
         } else {
             frequenciesLeft = frequencies;
         }
         frequencies = frequenciesLeft;
         return new Pair<String[], String[]>(selectedRanges, frequencies);
     }
 
     /**
      * Converts string array containing integer values and ranges to string array containing int
      * values only For example {"0","2","4","8-10","12","13","15-20", "22"} is converted to
      * {"0","2","4","8","9","10","12","13","15","16","17","18","19","20", "22"}
      * 
      * @param rangeArray string array containing string representations of int and/or ranges (eg.
      *        9-12 implies 9,10,11,12)
      * @return sorted string array containing only string representations of int and no ranges.
      */
     public static String[] rangeArraytoArray(String[] rangeArray) {
         ArrayList<String> list = new ArrayList<String>();
         for (String item : rangeArray) {
             int index = item.indexOf("-");
             if (index == -1) {
                 list.add(item);
             } else {
                 int start = Integer.parseInt(item.substring(0, index).trim());
                 int end = Integer.parseInt(item.substring(index + 1).trim());
                 for (int i = start; i <= end; i++) {
                     list.add(Integer.toString(i));
                 }
             }
         }
 
         String[] stringArray = new String[list.size()];
         int[] intArray = new int[list.size()];
         list.toArray(stringArray);
         for (int i = 0; i < stringArray.length; i++) {
             intArray[i] = Integer.parseInt(stringArray[i].trim());
         }
 
         Arrays.sort(intArray);
         for (int i = 0; i < intArray.length; i++) {
             stringArray[i] = Integer.toString(intArray[i]);
         }
 
         return stringArray;
     }
 
     public String[] getFrequencyArray(int band) {
         String frequencies[] = null;
         if (band == AfpModel.BAND_900) {
             frequencies = new String[(124 - 0 + 1) + (1023 - 955 + 1)];
             for (int i = 0; i < frequencies.length; i++) {
                 if (i <= 124)
                     frequencies[i] = Integer.toString(i);
                 else
                     frequencies[i] = Integer.toString(i + 955 - (124 + 1));
             }
         } else if (band == AfpModel.BAND_1800) {
             frequencies = new String[885 - 512 + 1];
             for (int i = 0; i < frequencies.length; i++) {
                 frequencies[i] = Integer.toString(512 + i);
             }
         } else if (band == AfpModel.BAND_850) {
             frequencies = new String[251 - 128 + 1];
             for (int i = 0; i < frequencies.length; i++) {
                 frequencies[i] = Integer.toString(128 + i);
             }
         } else if (band == AfpModel.BAND_1900) {
             frequencies = new String[810 - 512 + 1];
             for (int i = 0; i < frequencies.length; i++) {
                 frequencies[i] = Integer.toString(512 + i);
             }
         }
         return frequencies;
     }
 
     /**
      * Converts string array containing integer values to string array containing int values and/or
      * ranges (wherever applicable) For example
      * {"0","2","4","8","9","10","12","13","15","16","17","18","19","20", "22"} is converted to
      * {"0","2","4","8-10","12","13","15-20", "22"}
      * 
      * @param array An string array containing string representations of int values (no ranges)
      * @return
      */
     public static String[] arrayToRangeArray(String array[]) {
 
         int lastItem = -1;
         int rangeFirstItem = -1;
         String range = null;
         boolean isRange = false;
         int[] rangeArray = new int[array.length];
 
         for (int i = 0; i < array.length; i++) {
             rangeArray[i] = Integer.parseInt(array[i].trim());
         }
 
         Arrays.sort(rangeArray);
 
         ArrayList<String> list = new ArrayList<String>();
         for (int currItem : rangeArray) {
             if (lastItem >= 0 && currItem == lastItem + 1) {
                 range = "" + rangeFirstItem + "-" + currItem;
                 isRange = true;
                 lastItem = currItem;
             } else {
                 rangeFirstItem = currItem;
                 if (isRange) {
                     list.add(range);
                     isRange = false;
                 }
 
                 else if (lastItem >= 0)
                     list.add(Integer.toString(lastItem));
                 lastItem = currItem;
             }
         }
         if (isRange)
             list.add(range);
         else
             list.add(Integer.toString(lastItem));
 
         return list.toArray(new String[0]);
     }
 
     /**
      * @param interferenceMatrix
      * @return
      */
     public Set<String> getLists(NodeToNodeTypes types) {
         Map<String, ScalingFactors> map = scaling.get(types);
         return map == null ? Collections.<String> emptySet() : map.keySet();
     }
 
     public Node findOrCreateDomainNode(Node afpNode, String domain, String name, Integer order, GraphDatabaseService service) {
         Node domainNode = null;
 
         Traverser traverser = afpNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(NodeTypes.AFP_DOMAIN.getId()))
                     return true;
                 return false;
             }
 
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
 
         for (Node node : traverser) {
             if (node.getProperty(INeoConstants.PROPERTY_NAME_NAME).equals(name)
                     && node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME).equals(domain))
                 domainNode = node;
         }
 
         if (domainNode == null) {
             domainNode = service.createNode();
             NodeTypes.AFP_DOMAIN.setNodeType(domainNode, service);
             NeoUtils.setNodeName(domainNode, name, service);
             domainNode.setProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME, domain);
             domainNode.setProperty("order", order);
             afpNode.createRelationshipTo(domainNode, NetworkRelationshipTypes.CHILD);
         }
 
         return domainNode;
 
     }
 }
