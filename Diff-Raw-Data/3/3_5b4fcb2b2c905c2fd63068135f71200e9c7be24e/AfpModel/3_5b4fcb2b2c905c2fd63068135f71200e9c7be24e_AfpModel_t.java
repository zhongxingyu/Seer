 package org.amanzi.awe.afp.models;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import org.amanzi.awe.afp.ControlFileProperties;
 import org.amanzi.awe.afp.executors.AfpProcessExecutor;
 import org.amanzi.awe.afp.executors.AfpProcessProgress;
 import org.amanzi.awe.afp.filters.AfpFilter;
 import org.amanzi.awe.afp.filters.AfpRowFilter;
 import org.amanzi.awe.afp.wizards.AfpLoadNetworkPage;
 import org.amanzi.awe.afp.wizards.AfpWizardUtils;
 import org.amanzi.awe.console.AweConsolePlugin;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.amanzi.neo.services.utils.Pair;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 
 public class AfpModel {
 
 	protected Node datasetNode;
 	protected Node afpNode;
 	private final GraphDatabaseService service = NeoServiceProviderUi.getProvider().getService();
 	private static final String AFP_NODE_NAME = "afp-dataset";
 
 	private AfpProcessExecutor afpJob;
 	private HashMap<String, Node> networkNodes;
 	private HashMap<String, Node> afpNodes;
 
 	boolean optimizeFrequency = true;
 	boolean optimizeBSIC = true;
 	boolean optimizeHSN = true;
 	boolean optimizeMAIO = true;
 	
 	public static final String[] DOMAIN_TYPES = {"frequency", "mal", "sector_separations", "site_separations"};
 	public static final String[] BAND_NAMES = { "900", "1800", "850", "1900" };
 	public static final String[] CHANNEL_NAMES = {"BCCH", "TCH Non/BB Hopping", "TCH SY Hopping"};
 	public static final String GOALS_SUMMARY_ROW_HEADERS[] = {"Selected Sectors: ",
 			   "Selected TRXs: ",
 			   "BCCH TRXs: ",
 			   "TCH Non/BB Hopping TRXs: ",
 			   "TCH SY HoppingTRXs: "};
 	
 	public static final int BAND_900=0;
 	public static final int BAND_1800=1;
 	public static final int BAND_850=2;
 	public static final int BAND_1900=3;
 	
 	public static final String[][] SCALING_PAGE_ROW_HEADERS = {
 			{"BCCH", "BCCH"},
 			{"BCCH", "Non/BB TCH"},
 			{"BCCH", "SY TCH"},
 			{"Non/BB TCH", "BCCH"},
 			{"Non/BB TCH", "Non/BB TCH"},
 			{"Non/BB TCH", "SY TCH"},
 			{"SY TCH", "BCCH"},
 			{"SY TCH", "Non/BB TCH"},
 			{"SY TCH", "SY TCH"}
 			};
 	public static final String DEFAULT_MAL_NAME = "Default MAL";
 	public static final String DEFAULT_SECTOR_SEP_NAME = "Default Separations";
 	public static final String DEFAULT_SITE_SEP_NAME = "Default Separations";
 	private int totalTRX;
 	private int totalRemainingTRX;
 //	private int totalRemainingMalTRX;
 	private int totalSites;
 	private int totalSectors;
 	
 	/**
 	 * 0- 900
 	 * 1- 1800
 	 * 2- 850
 	 * 3- 1900
 	 */
 	boolean[] frequencyBands = new boolean[] {true, true, true, true};
 	
 	/**
 	 * 0- BCCH
 	 * 1- TCH Non/BB Hopping
 	 * 2- TCH SY Hopping
 	 */
 	boolean[] channelTypes = new boolean[] {true, true, true};
 	boolean analyzeCurrentFreqAllocation = true;
 
 	//Page 2 params
 	String availableFreq[] = new String[4];
 	int availableNCCs = 0xff;
 	int availableBCCs = 0xff;
 	
 	//Page 3 params
 	HashMap<String,AfpFrequencyDomainModel> freqDomains = new HashMap<String,AfpFrequencyDomainModel>();
 	Set TRXDomain;
 	
 	//Page 4 params
 	HashMap<String,AfpHoppingMALDomainModel> malDomains= new HashMap<String,AfpHoppingMALDomainModel>();
 	
 	//Page 5 params
 	HashMap<String,AfpSeparationDomainModel> siteSeparationDomains= new HashMap<String,AfpSeparationDomainModel>();
 	HashMap<String,AfpSeparationDomainModel> sectorSeparationDomains= new HashMap<String,AfpSeparationDomainModel>();
 	
 	//Page 6 params
 	//Constants defining the index of Serving-Interfering pair in the scaling rules arrays
 	//NHBB: Non/BB TCH
 	//SFH: SY TCH
 	public final static int BCCHBCCH = 0;
 	public final static int BCCHNHBB = 1;
 	public final static int BCCHSFH = 2;
 	public final static int NHBBBCCH = 3;
 	public final static int NHBBNHBB = 4;
 	public final static int NHBBSFH = 5;
 	public final static int SFHBCCH = 6;
 	public final static int SFHNHBB = 7;
 	public final static int SFHSFH = 8;
 	
 	//scaling rules arrays with default values
 	float[] sectorSeparation = new float[]{100, 100, 100, 100, 100, 100, 100, 100, 100};
 	float[] siteSeparation = new float[]{100, 70, 50, 70, 50, 30, 70, 50, 20};
 	float[] coInterference = new float[]{1, 0.7f, 0.5f, 0.7f, 0.5f, 0.3f, 0.7f, 0.3f, 0.2f};
 	float[] adjInterference = new float[]{1, 0.7f, 0.5f, 0.7f, 0.5f, 0.3f, 0.7f, 0.3f, 0.2f};
 	float[] coNeighbor = new float[]{1, 0.3f, 0.2f, 0, 0, 0, 0, 0, 0};
 	float[] adjNeighbor = new float[]{1, 0.1f, 0, 0, 0, 0, 0, 0, 0};
 	float[] coTriangulation = new float[]{1, 0, 0, 0, 0, 0, 0, 0, 0};
 	float[] adjTriangulation = new float[]{1, 0, 0, 0, 0, 0, 0, 0, 0};
 	float[] coShadowing = new float[]{1, 0, 0, 0, 0, 0, 0, 0, 0};
 	float[] adjShadowing = new float[]{1, 0, 0, 0, 0, 0, 0, 0, 0};
 	
 	//Progress page params
 	String[] tableItems = new String[6];
 	
     protected HashMap<String, String> parameters;
     protected HashMap<String, String> filters;
     
     public static final String[] sitePropertiesName = new String[] {
     	INeoConstants.PROPERTY_NAME_NAME
     };
     public static final String[] sectorPropertiesName = new String[] {
     	INeoConstants.PROPERTY_NAME_NAME
     };
     public static final String[] trxPropertiesName = new String[] {
     	INeoConstants.PROPERTY_NAME_NAME, "Layer" , "Subcell", "trx_id", 
 		"band", "Extended", "hopping_type", INeoConstants.PROPERTY_BCCH_NAME,
     };
 //    protected HashMap<String, String> equalFilters = new HashMap<String, String>();
 
 	
 	public AfpModel() {
 	}
 
 	/**
 	 * Array rows: 
 	 * 0-Selected sectors
 	 * 1-Selected TRXs
 	 * 2- BCCH TRXs
 	 * 3- TCH Non/BB Hopping TRXs
 	 * 4- TCH SY Hopping TRXs
 	 * 
 	 * Array columns:
 	 * 0- 900
 	 * 1- 1800
 	 * 2- 850
 	 * 3- 1900
 	 * @return return an array
 	 */
 	public int[][] getSelectedCount(){
 		totalTRX = 0;
 		totalSites = 0;
 		totalSectors = 0;
 		
 		int[] bandTRXs = new int[4];
 		
 		Traverser traverser = datasetNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
 
 			@Override
 			public boolean isReturnableNode(TraversalPosition currentPos) {
 				if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.SECTOR.getId()) ||
 						currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.SITE.getId()) ||
 						currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.TRX.getId())){
 					return true;
 				}
 				return false;
 			}
     	}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
 		
 		for (Node node: traverser){
 			// add to unique properties
 			
 			
 			if (node.getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.SITE.getId())){
 				totalSites++;
 			}
 			else if (node.getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.SECTOR.getId())){
 				totalSectors++;
 			}
 			else if(node.getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.TRX.getId())){
 				totalTRX++;
 				String band = (String)node.getProperty("band", "");
 				if (band.contains("900"))
 					bandTRXs[BAND_900]++;
 				if (band.contains("1800"))
 					bandTRXs[BAND_1800]++;
 				if (band.contains("850"))
 					bandTRXs[BAND_850]++;
 				if (band.contains("1900"))
 					bandTRXs[BAND_1900]++;
 			}
 			
 //			totalRemainingTRX = totalTRX;
 //			totalRemainingMalTRX = totalTRX;
 //			for (AfpFrequencyDomainModel dm : getFreqDomains(false)){
 //				totalRemainingTRX -= dm.getNumTRX();
 //			}
 //			for (AfpHoppingMALDomainModel dm : getMalDomains(false)){
 //				totalRemainingMalTRX -= dm.getNumTRX();
 //			}
 		}
 
 		
 		//TODO: get the count from database based on the selected node and also update frequency band values based on that
 		int[][] selectedArray = {bandTRXs, bandTRXs, bandTRXs, {0,0,0,0},{0,0,0,0}};
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
 
 //	public int getTotalRemainingMalTRX() {
 //		return totalRemainingMalTRX;
 //	}
 //
 //	public void setTotalRemainingMalTRX(int totalRemainingMalTRX) {
 //		this.totalRemainingMalTRX = totalRemainingMalTRX;
 //	}
 
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
 
 	/**
 	 * @return the afpNode
 	 */
 	public Node getAfpNode() {
 		return afpNode;
 	}
 
 
 	public void setSelectNetworkDataSetName(String datasetName) {
     	datasetNode = networkNodes.get(datasetName);
     	if(datasetNode != null) {
     		loadAfpDataSet();
     	}
 	}
 
     public String[] getNetworkDatasets() {
         networkNodes = new HashMap<String, Node>();
         for (Node root : NeoUtils.getAllRootTraverser(service, null)) {
         	
             if (NodeTypes.NETWORK.checkNode(root)) {
                 networkNodes.put(NeoUtils.getNodeName(root, service), root);
             }
         }
         return networkNodes.keySet().toArray(new String[0]);
     }
 	/**
      * Gets the networ datasets.
      * 
      * @return the network datasets
      */
     
     public String[] getAfpDatasets(Node networkNode) {
         afpNodes = new HashMap<String, Node>();
         Transaction tx = service.beginTx();
         try {
         	Traverser traverser = networkNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 				@Override
 				public boolean isReturnableNode(TraversalPosition currentPos) {
 					if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.AFP.getId()))
 						return true;
 					return false;
 				}
         		
         	}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
             for (Node afpNode : traverser) {
             	
                 if (NodeTypes.AFP.checkNode(afpNode)) {
                     afpNodes.put(NeoUtils.getNodeName(afpNode, service), afpNode);
                 }
             }
         } finally {
             tx.finish();
         }
         return afpNodes.keySet().toArray(new String[0]);
     }
     public boolean hasValidNetworkDataset() {
 	    if (datasetNode == null) {
 	            return false;
 	    }
 	    return  true;
     }
 
 	/**
 	 * @param afpNode the afpNode to set
 	 */
 	public void loadAfpDataSet() {
 		getAfpDatasets(datasetNode);
 
         if(afpNodes != null) {
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
 	 * Array: 
 	 * 0- Frequencies
 	 * 1- BSIC
 	 * 2- HSN
 	 * 3- MAIO
 	 * @return boolean array containing all optimization params
 	 */
 	public boolean[] getOptimizationParameters(){
 		return new boolean[]{isOptimizeFrequency(), isOptimizeBSIC(), isOptimizeHSN(), isOptimizeMAIO()}; 
 	}
 
 
 
 	/**
 	 * @return the frequencyBands
 	 */
 	public boolean[] getFrequencyBands() {
 		return frequencyBands;
 	}
 
 	public int[] getAvailableFrequencyBandsIndexs() {
 		int cnt =0;
 		for(int i=0;i<this.frequencyBands.length;i++) 
 			if(this.frequencyBands[i]) cnt++;
 		
 		int ret[] = new int[cnt];
 		for(int i=0,j=0;i<this.frequencyBands.length;i++) {
 			if(this.frequencyBands[i]) {
 				ret[j] =i;
 				j++;
 			}
 		}
 		return ret;
 	}
 
 	public boolean isFrequencyBandAvaliable(int band) {
 		if(band >=0 && band <=4) {
 			return frequencyBands[band];
 		}		
 		return false;
 	}
 
 
 
 	/**
 	 * @param frequencyBands the frequencyBands to set
 	 */
 	public void setFrequencyBands(boolean[] frequencyBands) {
 		this.frequencyBands = frequencyBands;
 		for(int i=0;i<4;i++) {
 			if(!this.frequencyBands[i]) {
 				this.availableFreq[i] ="";
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
 		if(band >=0 && band <=4) {
 			if(availableFreq[band] != null) {
 				return availableFreq[band];
 			}
 		}
 		return "";
 	}
 
 	public String getAvailableFreq(String bandName) {
 		for(int i=0;i<BAND_NAMES.length;i++) {
 			if(BAND_NAMES[i].compareTo(bandName) ==0) {
 				if(availableFreq[i] != null) {
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
 		if(band >=0 && band <=4) {
 			availableFreq[band] = freq;
 		}
 	}
 
 
 	/**
 	 * @return the availableNCCs
 	 */
 	public boolean[] getAvailableNCCs() {
 		boolean ret[] = new boolean[] { true,true,true,true,true,true,true,true};
 		for(int i=0; i<8;i++) {
 			ret[i] = ((availableNCCs & (1 << i)) >0);
 		}
 		return ret;
 	}
 
 
 
 
 
 	/**
 	 * @param availableNCCs the availableNCCs to set
 	 */
 	public void setAvailableNCCs(boolean[] availableNCCs) {
 		int n =0;
 		for(int i=0; i< availableNCCs.length;i++) {
 			if(availableNCCs[i]) {
 				n = n | (1 << i);
 			}
 		}
 		this.availableNCCs = n;
 	}
 
 
 
 
 
 	/**
 	 * @return the availableBCCs
 	 */
 	public boolean[] getAvailableBCCs() {
 		boolean ret[] = new boolean[] { true,true,true,true,true,true,true,true};
 		for(int i=0; i<8;i++) {
 			ret[i] = ((availableBCCs & (1 << i)) >0);
 		}
 		return ret;
 	}
 
 
 
 
 
 	/**
 	 * @param availableBCCs the availableBCCs to set
 	 */
 	public void setAvailableBCCs(boolean[] availableBCCs) {
 		int n =0;
 		for(int i=0; i< availableBCCs.length;i++) {
 			if(availableBCCs[i]) {
 				n = n | (1 << i);
 			}
 		}
 		this.availableBCCs = n;
 	}
 
 	private void addRemoveFreeFrequencyDomain(boolean addFree) {
 		for(int i=0; i< frequencyBands.length;i++) {
 			if(frequencyBands[i]) {
 				// add free domains
 				if(addFree) {
 					AfpFrequencyDomainModel d = new AfpFrequencyDomainModel();
 					d.setName("Free " + BAND_NAMES[i]);
 					d.setBand(BAND_NAMES[i]);
 					d.setNumTRX(totalRemainingTRX);
 					d.setFree(true);
 					String f[] = new String[1];
 					f[0] = this.availableFreq[i];
 					d.setFrequencies(f);
 					freqDomains.put(d.getName(), d);
 				} else {
 					freqDomains.remove("Free " + BAND_NAMES[i]);
 				}
 			}
 		}
 	}
 	
 	/*private int getTotalSelectedTRXCount(){
 		int totalSelected = 0;
 		for (AfpFrequencyDomainModel dm: freqDomains.values()){
 			totalSelected += dm.getNumTRX();
 		}
 		
 		return totalSelected;
 	}*/
 	
 	public AfpDomainModel findDomainByName(String type, String name){
 		AfpDomainModel model = null;
 		if (type.equals(DOMAIN_TYPES[0])){
 			for(AfpFrequencyDomainModel freqModel : freqDomains.values()){
 				if(freqModel.getName().equals(name)){
 					model = freqModel;
 					break;
 				}
 			}
 		}
 		
 		if (type.equals(DOMAIN_TYPES[1])){
 			for(AfpHoppingMALDomainModel malModel : malDomains.values()){
 				if(malModel.getName().equals(name)){
 					model = malModel;
 					break;
 				}
 			}
 		}
 		if (type.equals(DOMAIN_TYPES[2])){
 			for(AfpSeparationDomainModel malModel : this.sectorSeparationDomains.values()){
 				if(malModel.getName().equals(name)){
 					model = malModel;
 					break;
 				}
 			}
 		}
 		if (type.equals(DOMAIN_TYPES[3])){
 			for(AfpSeparationDomainModel malModel : this.siteSeparationDomains.values()){
 				if(malModel.getName().equals(name)){
 					model = malModel;
 					break;
 				}
 			}
 		}
 		
 		return model;
 	}
 
 	/**
 	 * @return the freqDomains
 	 */
 	public Collection<AfpFrequencyDomainModel> getFreqDomains(boolean addFree) {
 		addRemoveFreeFrequencyDomain(addFree);
 		ArrayList<AfpFrequencyDomainModel> l = new ArrayList<AfpFrequencyDomainModel>();
 		for(AfpFrequencyDomainModel d:freqDomains.values()) {
 			l.add(new AfpFrequencyDomainModel(d));
 		}
 		return l;
 	}
 	/*public int[] getFreqDomainsTrxCount(boolean addFree) {
 		Collection<AfpFrequencyDomainModel> domains = getFreqDomains(true);
 		int counters[] = new int[domains.size()];
 		
 		int cnt=0;
 		for(AfpFrequencyDomainModel d: domains) {
 			String filterStr =d.getFilters();
 			if( filterStr != null) {
 				filters.add(AfpFilter.getFilter(filterStr));
 			} else {
 				filters.add(null);
 			}
 			counters[cnt] =0;
 			cnt++;
 		}
 
 		return counters;
 	}*/
 
 
 
 
 
 	/**
 	 * @param freqDomains the freqDomains to set
 	 */
 	private void setFreqDomains(HashMap<String,AfpFrequencyDomainModel> freqDomains) {
 		this.freqDomains = freqDomains;
 	}
 	
 	private void addDefaultMalDomains() {
 		// add free domains
 		AfpHoppingMALDomainModel d = new AfpHoppingMALDomainModel();
 		d.setName(DEFAULT_MAL_NAME);
 		d.setFree(true);
 //		d.setNumTRX(totalRemainingMalTRX);
 		malDomains.put(d.getName(),d);
 	}
 	
 	/**
 	 * @return the malDomains
 	 */
 	public Collection<AfpHoppingMALDomainModel> getMalDomains(boolean getFree) {
 		addDefaultMalDomains();
 		ArrayList<AfpHoppingMALDomainModel> l = new ArrayList<AfpHoppingMALDomainModel>();
 		for(AfpHoppingMALDomainModel d:this.malDomains.values()) {
 			if (!getFree)
 				if(d.getName().equals(DEFAULT_MAL_NAME))
 					continue;
 			l.add(new AfpHoppingMALDomainModel(d));
 		}
 		
 		return l;
 	}
 
 
 	private void addDefaultSiteSeparationDomains() {
 		// add free domains
 		AfpSeparationDomainModel d = new AfpSeparationDomainModel();
 		d.setName(DEFAULT_SITE_SEP_NAME);
 		d.setFree(true);
 		siteSeparationDomains.put(d.getName(),d);
 	}
 
 
 	/**
 	 * @return the siteSeparationDomains
 	 */
 	public Collection<AfpSeparationDomainModel> getSiteSeparationDomains(boolean getFree) {
 		addDefaultSiteSeparationDomains();
 		ArrayList<AfpSeparationDomainModel> l = new ArrayList<AfpSeparationDomainModel>();
 		for(AfpSeparationDomainModel d:this.siteSeparationDomains.values()) {
 			if(!getFree) {
 				if(d.getName().equals(DEFAULT_SECTOR_SEP_NAME))
 					continue;
 			}
 			l.add(new AfpSeparationDomainModel(d));
 		}
 		
 		return l;
 	}
 
 
 	/**
 	 * @param siteSeparationDomains the siteSeparationDomains to set
 	 */
 	private void setSiteSeparationDomains(HashMap<String,AfpSeparationDomainModel> siteSeparationDomains) {
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
 		for(AfpSeparationDomainModel d:this.sectorSeparationDomains.values()) {
 			if(!getFree)
 				if(d.getName().equals(DEFAULT_SITE_SEP_NAME))
 					continue;
 
 			l.add(new AfpSeparationDomainModel(d));
 		}
 		
 		return l;
 	}
 
 
 	/**
 	 * @param sectorSeparationDomains the sectorSeparationDomains to set
 	 */
 	private void setSectorSeparationDomains(
 			HashMap<String,AfpSeparationDomainModel> sectorSeparationDomains) {
 		this.sectorSeparationDomains = sectorSeparationDomains;
 	}
 	
 	
 	public void updateFreqDomain(AfpDomainModel model){
 		for(AfpDomainModel dm : freqDomains.values()){
 			if (dm.getName().equals(model.getName())){
 				dm = model;
 				break;
 			}
 		}
 	}
 	
 	public void updateMalDomain(AfpDomainModel model){
 		for(AfpDomainModel dm : malDomains.values()){
 			if (dm.getName().equals(model.getName())){
 				dm = model;
 				break;
 			}
 		}
 	}
 	
 	public void updateSectorSepDomain(AfpDomainModel model){
 		for(AfpDomainModel dm : sectorSeparationDomains.values()){
 			if (dm.getName().equals(model.getName())){
 				dm = model;
 				break;
 			}
 		}
 	}
 	
 	public void updateSiteDomain(AfpDomainModel model){
 		for(AfpDomainModel dm : siteSeparationDomains.values()){
 			if (dm.getName().equals(model.getName())){
 				dm = model;
 				break;
 			}
 		}
 	}
 
 
 
 
 
 	/**
 	 * @param malDomains the malDomains to set
 	 */
 	private void setMalDomains(HashMap<String,AfpHoppingMALDomainModel> malDomains) {
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
 	public float[] getCoInterference() {
 		return coInterference;
 	}
 
 
 
 
 
 	/**
 	 * @param coInterference the coInterference to set
 	 */
 	public void setCoInterference(float[] coInterference) {
 		this.coInterference = coInterference;
 	}
 
 
 
 
 
 	/**
 	 * @return the adjInterference
 	 */
 	public float[] getAdjInterference() {
 		return adjInterference;
 	}
 
 
 
 
 
 	/**
 	 * @param adjInterference the adjInterference to set
 	 */
 	public void setAdjInterference(float[] adjInterference) {
 		this.adjInterference = adjInterference;
 	}
 
 
 
 
 
 	/**
 	 * @return the coNeighbor
 	 */
 	public float[] getCoNeighbor() {
 		return coNeighbor;
 	}
 
 
 
 
 
 	/**
 	 * @param coNeighbor the coNeighbor to set
 	 */
 	public void setCoNeighbor(float[] coNeighbor) {
 		this.coNeighbor = coNeighbor;
 	}
 
 
 
 
 
 	/**
 	 * @return the adjNeighbor
 	 */
 	public float[] getAdjNeighbor() {
 		return adjNeighbor;
 	}
 
 
 
 
 
 	/**
 	 * @param adjNeighbor the adjNeighbor to set
 	 */
 	public void setAdjNeighbor(float[] adjNeighbor) {
 		this.adjNeighbor = adjNeighbor;
 	}
 
 
 
 
 
 	/**
 	 * @return the coTriangulation
 	 */
 	public float[] getCoTriangulation() {
 		return coTriangulation;
 	}
 
 
 
 
 
 	/**
 	 * @param coTriangulation the coTriangulation to set
 	 */
 	public void setCoTriangulation(float[] coTriangulation) {
 		this.coTriangulation = coTriangulation;
 	}
 
 
 
 
 
 	/**
 	 * @return the adjTriangulation
 	 */
 	public float[] getAdjTriangulation() {
 		return adjTriangulation;
 	}
 
 
 
 
 
 	/**
 	 * @param adjTriangulation the adjTriangulation to set
 	 */
 	public void setAdjTriangulation(float[] adjTriangulation) {
 		this.adjTriangulation = adjTriangulation;
 	}
 
 
 
 
 
 	/**
 	 * @return the coShadowing
 	 */
 	public float[] getCoShadowing() {
 		return coShadowing;
 	}
 
 
 
 
 
 	/**
 	 * @param coShadowing the coShadowing to set
 	 */
 	public void setCoShadowing(float[] coShadowing) {
 		this.coShadowing = coShadowing;
 	}
 
 
 
 
 
 	/**
 	 * @return the adjShadowing
 	 */
 	public float[] getAdjShadowing() {
 		return adjShadowing;
 	}
 
 
 
 
 
 	/**
 	 * @param adjShadowing the adjShadowing to set
 	 */
 	public void setAdjShadowing(float[] adjShadowing) {
 		this.adjShadowing = adjShadowing;
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
 
 	public void addFreqDomain(AfpFrequencyDomainModel freqDomain){
 		if(freqDomains.containsKey(freqDomain.getName())) {
 			freqDomain.setName(freqDomain.getName() + "-1");
 		}
 		freqDomains.put(freqDomain.getName(),freqDomain);
 	}
 	public void editFreqDomain(AfpFrequencyDomainModel freqDomain){
 		freqDomains.put(freqDomain.getName(),freqDomain);
 	}
 	
 	public void deleteFreqDomain(String name){
 		if (freqDomains == null){
 			return;
 		}
 		if(freqDomains.containsKey(name))
 			freqDomains.remove(name);
 	}
 	
 	public AfpFrequencyDomainModel findFreqDomain(String domainName){
 		return freqDomains.get(domainName);
 	}
 	
 	public String[] getAllFrequencyDomainNames(){
 		String[] names = new String[freqDomains.size()];
 		int i = 0;
 		for(AfpFrequencyDomainModel freqDomain : this.getFreqDomains(false)){
 			if(!freqDomain.isFree()) {
 				names[i] = freqDomain.getName();
 				i++;
 			}
 		}
 		String[] ret = new String[i];
 		for(int j=0;j< i;j++) {
 			ret[j]  = names[j];
 		}
 		return ret;
 	}
 	
 	public String[] getAvailableBands(){
 		int length = 0;
 		for (boolean isEnabled : frequencyBands){
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
 	
 	public void addMALDomain(AfpHoppingMALDomainModel malDomain){
 		if(malDomains.containsKey(malDomain.getName())) {
 			malDomain.setName(malDomain.getName() + "-1");
 		}
 		malDomains.put(malDomain.getName(),malDomain);
 	}
 	
 	public void editMALDomain(AfpHoppingMALDomainModel malDomain){
 		malDomains.put(malDomain.getName(),malDomain);
 	}
 
 	public void deleteMALDomain(AfpHoppingMALDomainModel malDomain){
 		if (malDomains == null){
 			//TODO error handling
 			return;
 		}
 		if(malDomain.isFree())
 			return;
 		
 		if(malDomains.containsKey(malDomain.getName()))
 			malDomains.remove(malDomain.getName());
 	}
 	
 	public AfpHoppingMALDomainModel findMALDomain(String domainName){
 		for(AfpHoppingMALDomainModel malDomain : malDomains.values()){
 			if (malDomain.getName().equals(domainName))
 				return malDomain;
 		}
 		return null;
 	}
 	
 	public String[] getAllMALDomainNames(){
 		String[] names = new String[malDomains.size()];
 		int i = 0;
 		for(AfpHoppingMALDomainModel malDomain : getMalDomains(true)){
 			names[i] = malDomain.getName();
 			i++;	
 		}
 		return names;
 	}
 	
 	public void addSiteSeparationDomain(AfpSeparationDomainModel separationDomain){
 		if(siteSeparationDomains.containsKey(separationDomain.getName())) {
 			separationDomain.setName(separationDomain.getName() + "-1");
 		}
 		siteSeparationDomains.put(separationDomain.getName(),separationDomain);
 	}
 	
 	public void editSiteSeparationDomain(AfpSeparationDomainModel domain){
 		siteSeparationDomains.put(domain.getName(),domain);
 	}
 
 	public void deleteSiteSeparationDomain(AfpSeparationDomainModel separationDomain){
 		if (separationDomain == null){
 			//TODO error handling
 		}
 		if(siteSeparationDomains.containsKey(separationDomain.getName()))
 			siteSeparationDomains.remove(separationDomain.getName());
 	}
 	
 	public AfpSeparationDomainModel findSiteSeparationDomain(String domainName){
 		if(siteSeparationDomains.containsKey(domainName))
 			return siteSeparationDomains.get(domainName);
 		return null;
 	}
 	
 	public String[] getAllSiteSeparationDomainNames(){
 		String[] names = new String[siteSeparationDomains.size()];
 		int i = 0;
 		for(AfpSeparationDomainModel separationDomain : siteSeparationDomains.values()){
 			names[i] = separationDomain.getName();
 			i++;	
 		}
 		return names;
 	}
 	
 	
 	public void addSectorSeparationDomain(AfpSeparationDomainModel separationDomain){
 		if(sectorSeparationDomains.containsKey(separationDomain.getName())) {
 			separationDomain.setName(separationDomain.getName() + "-1");
 		}
 		sectorSeparationDomains.put(separationDomain.getName(), separationDomain);
 	}
 	public void editSectorSeparationDomain(AfpSeparationDomainModel domain){
 		sectorSeparationDomains.put(domain.getName(),domain);
 	}
 	
 	public void deleteSectorSeparationDomain(AfpSeparationDomainModel domain){
 		if (domain == null){
 			//TODO error handling
 		}
 		if(sectorSeparationDomains.containsKey(domain.getName()))
 			sectorSeparationDomains.remove(domain.getName());
 	}
 	
 	public AfpSeparationDomainModel findSectorSeparationDomain(String domainName){
 		if(sectorSeparationDomains.containsKey(domainName)) {
 			return sectorSeparationDomains.get(domainName);
 		}
 		return null;
 	}
 	
 	public String[] getAllSectorSeparationDomainNames(){
 		String[] names = new String[sectorSeparationDomains.size()];
 		int i = 0;
 		for(AfpSeparationDomainModel separationDomain : sectorSeparationDomains.values()){
 			names[i] = separationDomain.getName();
 			i++;	
 		}
 		return names;
 	}
 	
 	public void setInterferenceMatrixArrays(float[][] array){
 		coInterference = array[0];
 		adjInterference = array[1];
 		coNeighbor = array[2];
 		adjNeighbor = array[3];
 		coTriangulation = array[4];
 		adjTriangulation = array[5];
 		coShadowing = array[6];
 		adjShadowing = array[7];
 	}
 	
 	
 	/**
 	 * Write all user selected data to database
 	 */
 	public void saveUserData() {
 		
 		Transaction tx = this.service.beginTx();
 		try {
 			if (afpNode == null) {
 				afpNode = service.createNode();
 				NodeTypes.AFP.setNodeType(afpNode, service);
 				NeoUtils.setNodeName(afpNode, AFP_NODE_NAME , service);
 				datasetNode.createRelationshipTo(afpNode, NetworkRelationshipTypes.CHILD);
 			}
 			afpNode.setProperty(INeoConstants.AFP_OPTIMIZATION_PARAMETERS, getOptimizationParameters());
 			afpNode.setProperty(INeoConstants.AFP_FREQUENCY_BAND, getFrequencyBands());
 			afpNode.setProperty(INeoConstants.AFP_CHANNEL_TYPE, getChanneltypes());
 			afpNode.setProperty(INeoConstants.AFP_ANALYZE_CURRENT, isAnalyzeCurrentFreqAllocation());
 			if (getAvailableFreq(BAND_900) != null)
 				afpNode.setProperty( INeoConstants.AFP_AVAILABLE_FREQUENCIES_900, getAvailableFreq(BAND_900));
 			if (getAvailableFreq(BAND_1800) != null)
 				afpNode.setProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_1800, getAvailableFreq(BAND_1800));
 			
 			if (getAvailableFreq(BAND_850) != null)
 				afpNode.setProperty( INeoConstants.AFP_AVAILABLE_FREQUENCIES_850, getAvailableFreq(BAND_850));
 			if (getAvailableFreq(BAND_1900) != null)
 				afpNode.setProperty( INeoConstants.AFP_AVAILABLE_FREQUENCIES_1900, getAvailableFreq(BAND_1900));
 			afpNode.setProperty(INeoConstants.AFP_AVAILABLE_BCCS, this.availableBCCs);
 			afpNode.setProperty(INeoConstants.AFP_AVAILABLE_NCCS, this.availableNCCs);
 
 			afpNode.setProperty(INeoConstants.AFP_SECTOR_SCALING_RULES, getSectorSeparation());
 			afpNode.setProperty(INeoConstants.AFP_SITE_SCALING_RULES, getSiteSeparation());
 			afpNode.setProperty(INeoConstants.AFP_CO_INTERFERENCE_VALUES, getCoInterference());
 			afpNode.setProperty(INeoConstants.AFP_ADJ_INTERFERENCE_VALUES,getAdjInterference());
 			afpNode.setProperty(INeoConstants.AFP_CO_NEIGHBOR_VALUES, getCoNeighbor());
 			afpNode.setProperty(INeoConstants.AFP_ADJ_NEIGHBOR_VALUES, getAdjNeighbor());
 			afpNode.setProperty(INeoConstants.AFP_CO_TRIANGULATION_VALUES, getCoTriangulation());
 			afpNode.setProperty(INeoConstants.AFP_ADJ_TRIANGULATION_VALUES, getAdjTriangulation());
 			afpNode.setProperty(INeoConstants.AFP_CO_SHADOWING_VALUES, getCoShadowing());
 			afpNode.setProperty(INeoConstants.AFP_ADJ_SHADOWING_VALUES, getAdjShadowing());
 			
 			// remove all chid nodes before storing new ones
 			Traverser traverser = afpNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 				@Override
 				public boolean isReturnableNode(TraversalPosition currentPos) {
 					if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.AFP_DOMAIN.getId())) 
 						return true;
 					return false;
 				}
 	    		
 	    	}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
 			
 
 			for (Node n : traverser) {
 				Iterable<Relationship> relationsI = n.getRelationships();
 				if(relationsI != null) {
 					Iterator<Relationship> reations = relationsI.iterator();
 					while(reations.hasNext()) {
 						Relationship r = reations.next();
 						r.delete();
 					}
 				}
 				n.delete();
 			}
 
 			for (AfpFrequencyDomainModel frequencyModel : getFreqDomains(false)) {
 				if(!frequencyModel.isFree())
 					createFrequencyDomainNode(afpNode, frequencyModel, service);
 			}
 
 			for (AfpHoppingMALDomainModel malModel : getMalDomains(true)) {
 				createHoppingMALDomainNode(afpNode, malModel,
 						service);
 			}
 
 			for (AfpSeparationDomainModel separationsModel : getSectorSeparationDomains(true)) {
 				createSectorSeparationDomainNode(afpNode,
 						separationsModel, service);
 			}
 
 			for (AfpSeparationDomainModel separationsModel : getSiteSeparationDomains(true)) {
 				createSiteSeparationDomainNode(afpNode,
 						separationsModel, service);
 			}
 
 		} catch (Exception e) {
 			AweConsolePlugin.exception(e);
 		} finally {
 			tx.finish();
 		}
 	}
 	
 	private void loadUserData() {
 		
 		try {
 			if (afpNode == null) {
 				return;
 			}
 			try {
 				boolean opt[] = (boolean[])afpNode.getProperty(INeoConstants.AFP_OPTIMIZATION_PARAMETERS);
 				if(opt != null) {
 					if(opt.length >=4) {
 						this.optimizeFrequency = opt[0];
 						this.optimizeBSIC = opt[1];
 						this.optimizeHSN = opt[2];
 						this.optimizeMAIO = opt[3];
 					}
 				}
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setFrequencyBands((boolean[])afpNode.getProperty(INeoConstants.AFP_FREQUENCY_BAND));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setChanneltypes((boolean[])afpNode.getProperty(INeoConstants.AFP_CHANNEL_TYPE));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setAnalyzeCurrentFreqAllocation((Boolean)afpNode.getProperty(INeoConstants.AFP_ANALYZE_CURRENT));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setAvailableFreq(BAND_900, (String)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_900));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setAvailableFreq(BAND_1800,(String)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_1800));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setAvailableFreq(BAND_850,(String)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_850));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setAvailableFreq(BAND_1900,(String)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_FREQUENCIES_1900));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				Integer n = (Integer)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_BCCS);
 				this.availableBCCs =n.intValue();				
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				Integer n = (Integer)afpNode.getProperty(INeoConstants.AFP_AVAILABLE_NCCS);
 				this.availableNCCs =n.intValue();				
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setSectorSeparation((float[])afpNode.getProperty(INeoConstants.AFP_SECTOR_SCALING_RULES));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setSiteSeparation((float[])afpNode.getProperty(INeoConstants.AFP_SITE_SCALING_RULES));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setCoInterference((float[])afpNode.getProperty(INeoConstants.AFP_CO_INTERFERENCE_VALUES));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setAdjInterference((float[])afpNode.getProperty(INeoConstants.AFP_ADJ_INTERFERENCE_VALUES));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setCoNeighbor((float[])afpNode.getProperty(INeoConstants.AFP_CO_NEIGHBOR_VALUES));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setAdjNeighbor((float[])afpNode.getProperty(INeoConstants.AFP_ADJ_NEIGHBOR_VALUES));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setCoTriangulation((float[])afpNode.getProperty(INeoConstants.AFP_CO_TRIANGULATION_VALUES));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setAdjTriangulation((float[])afpNode.getProperty(INeoConstants.AFP_ADJ_TRIANGULATION_VALUES));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setCoShadowing((float[])afpNode.getProperty(INeoConstants.AFP_CO_SHADOWING_VALUES));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			try {
 				setAdjShadowing((float[])afpNode.getProperty(INeoConstants.AFP_ADJ_SHADOWING_VALUES));
 			} catch(Exception e) {
 				// no property sent 
 			}
 			
 			loadDomainNode(afpNode);
 /*
 			for (AfpHoppingMALDomainModel malModel : getMalDomains()) {
 				createHoppingMALDomainNode(afpNode, malModel,
 						service);
 			}
 
 			for (AfpSeparationDomainModel separationsModel : getSectorSeparationDomains()) {
 				createSectorSeparationDomainNode(afpNode,
 						separationsModel, service);
 			}
 
 			for (AfpSeparationDomainModel separationsModel : getSiteSeparationDomains()) {
 				createSiteSeparationDomainNode(afpNode,
 						separationsModel, service);
 			}*/
 
 		} catch (Exception e) {
 			AweConsolePlugin.exception(e);
 		} finally {
 		}
 	}
 	
 	public void createFrequencyDomainNode(Node afpNode, AfpFrequencyDomainModel domainModel, GraphDatabaseService service){
 		Node frequencyNode = findOrCreateDomainNode(afpNode, INeoConstants.AFP_DOMAIN_NAME_FREQUENCY, domainModel.getName(), service);
         
 		if(domainModel.getFilters() != null) {
 			frequencyNode.setProperty(INeoConstants.AFP_PROPERTY_FILTERS_NAME, domainModel.getFilters());
 		}
         frequencyNode.setProperty(INeoConstants.AFP_PROPERTY_FREQUENCY_BAND_NAME, domainModel.getBand());
         frequencyNode.setProperty(INeoConstants.AFP_PROPERTY_FREQUENCIES_NAME, domainModel.getFrequencies());
         frequencyNode.setProperty(INeoConstants.AFP_PROPERTY_TRX_COUNT_NAME, domainModel.getNumTRX());
 	}
 	
 	public void createHoppingMALDomainNode(Node afpNode, AfpHoppingMALDomainModel domainModel, GraphDatabaseService service){
 		Node malNode = findOrCreateDomainNode(afpNode, INeoConstants.AFP_DOMAIN_NAME_MAL, domainModel.getName(), service);
 
 		if(domainModel.getFilters() != null) {
 			malNode.setProperty(INeoConstants.AFP_PROPERTY_FILTERS_NAME, domainModel.getFilters());
 		}
 		malNode.setProperty(INeoConstants.AFP_PROPERTY_MAL_SIZE_NAME, domainModel.getMALSize());
 		malNode.setProperty(INeoConstants.AFP_PROPERTY_TRX_COUNT_NAME, domainModel.getNumTRX());
 	}
 	
 	public void createSectorSeparationDomainNode(Node afpNode, AfpSeparationDomainModel domainModel, GraphDatabaseService service){
 		Node separationNode = findOrCreateDomainNode(afpNode, INeoConstants.AFP_DOMAIN_NAME_SECTOR_SEPARATION, domainModel.getName(), service);
 
 		if(domainModel.getFilters() != null) {
 			separationNode.setProperty(INeoConstants.AFP_PROPERTY_FILTERS_NAME, domainModel.getFilters());
 		}
 		separationNode.setProperty(INeoConstants.AFP_PROPERTY_SEPARATIONS_NAME, domainModel.getSeparations());
 	}
 	
 	public void createSiteSeparationDomainNode(Node afpNode, AfpSeparationDomainModel domainModel, GraphDatabaseService service){
 		Node separationNode = findOrCreateDomainNode(afpNode, INeoConstants.AFP_DOMAIN_NAME_SITE_SEPARATION, domainModel.getName(), service);
 
 		if(domainModel.getFilters() != null) {
 			separationNode.setProperty(INeoConstants.AFP_PROPERTY_FILTERS_NAME, domainModel.getFilters());
 		}
 		separationNode.setProperty(INeoConstants.AFP_PROPERTY_SEPARATIONS_NAME, domainModel.getSeparations());
 	}
 	
 	public void deleteDomainNode(Node afpNode, String domain, String name, GraphDatabaseService service){
 		//TODO implement this method
 	}
 
 	public Node findOrCreateDomainNode(Node afpNode, String domain, String name, GraphDatabaseService service){
 		Node domainNode = null;
 		
 		Traverser traverser = afpNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 			@Override
 			public boolean isReturnableNode(TraversalPosition currentPos) {
 				if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.AFP_DOMAIN.getId()))
 					return true;
 				return false;
 			}
     		
     	}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
 		
 		for (Node node : traverser) {
         	if (node.getProperty(INeoConstants.PROPERTY_NAME_NAME).equals(name) &&
         			node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME).equals(domain))
         		domainNode = node;
         }
 		
 		if (domainNode == null){
 			domainNode = service.createNode();
     		NodeTypes.AFP_DOMAIN.setNodeType(domainNode, service);
             NeoUtils.setNodeName(domainNode, name, service);
             domainNode.setProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME, domain);
             afpNode.createRelationshipTo(domainNode, NetworkRelationshipTypes.CHILD);
 		}
 		
 		return domainNode;
 
 	}
 	
 	public void executeAfpEngine(AfpProcessProgress progress){
 		if (afpNode != null ){
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
 	    	int cnt =0;
			boolean first = true;
 	    	for(int i=0; i< frequencyBands.length;i++) {
 	    		if(frequencyBands[i]) {
 	    			String freq = this.availableFreq[i];
 	    			String[] franges = freq.split(",");
 	    			
 	    			String[] freqList = rangeArraytoArray(franges);
 	    			for(String f: freqList) {
 	    				if(!first) {
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
 	    	parameters.put(ControlFileProperties.RECALCULATE_ALL, "1" );
 	    	parameters.put(ControlFileProperties.USE_TRAFFIC, "1");
 	    	parameters.put(ControlFileProperties.USE_SO_NEIGHBOURS, "1");
 	    	parameters.put(ControlFileProperties.DECOMPOSE_CLIQUES, "0");
 			afpJob = new AfpProcessExecutor("Execute Afp Process", datasetNode,this.afpNode, service, parameters);
 			afpJob.setProgress(progress);
 			//afpJob.schedule();
     	}
 	}
 
 	private void loadDomainNode(Node afpNode){
 		Node domainNode = null;
 		
 		Traverser traverser = afpNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 			@Override
 			public boolean isReturnableNode(TraversalPosition currentPos) {
 				if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.AFP_DOMAIN.getId())) 
 					return true;
 				return false;
 			}
     		
     	}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
 		
 
 		for (Node node : traverser) {
         	if (node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME,"").equals(INeoConstants.AFP_DOMAIN_NAME_FREQUENCY)) {
         		// frequency type domain
         		AfpFrequencyDomainModel m = AfpFrequencyDomainModel.getModel(node);
         		if(m != null) {
         			this.addFreqDomain(m);
         		}
         	} else  if (node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME,"").equals(INeoConstants.AFP_DOMAIN_NAME_MAL)) {
         		// frequency type domain
         		AfpHoppingMALDomainModel m = AfpHoppingMALDomainModel.getModel(node);
         		if(m != null) {
         			addMALDomain(m);
         		}
         	} else  if (node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME,"").equals(INeoConstants.AFP_DOMAIN_NAME_SECTOR_SEPARATION)) {
         		// frequency type domain
         		AfpSeparationDomainModel m = AfpSeparationDomainModel.getModel(node, INeoConstants.AFP_PROPERTY_SEPARATIONS_NAME);
         		if(m != null) {
         			this.addSectorSeparationDomain(m);
         		}
         	} else  if (node.getProperty(INeoConstants.AFP_PROPERTY_DOMAIN_NAME,"").equals(INeoConstants.AFP_DOMAIN_NAME_SITE_SEPARATION)) {
         		// frequency type domain
         		AfpSeparationDomainModel m = AfpSeparationDomainModel.getModel(node,INeoConstants.AFP_PROPERTY_SEPARATIONS_NAME);
         		if(m != null) {
         			this.addSiteSeparationDomain(m);
         		}
         	}  
         }
 	}
 
 
 	public AfpProcessExecutor getExecutor() {
 		return afpJob;
 	}
 
 	public Traverser getTRXList(final HashMap<String, String> filters) {
     	Traverser traverser = datasetNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
 
 			@Override
 			public boolean isReturnableNode(TraversalPosition currentPos) {
 				boolean ret = false;
 				if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.SECTOR.getId())){
 					if (filters != null){
 						for (String key: filters.keySet()){
 							if (key.equals("band")){
 								for (String band : filters.get(key).split(",")){
 									if (((String)currentPos.currentNode().getProperty("band", "")).contains(band))
 										ret = ret || true;
 								}
 							}
 						}
 					}
 					else 
 						ret = true;
 				}
 					
 				return ret;
 			}
     		
     	}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
     	
         return traverser;
 	}
 	
 	@Override
 	public String toString(){
 		StringBuilder sb = new StringBuilder();
 		sb.append("Optimization Goals:\n" );
 		if (isOptimizeFrequency()) sb.append("Frequencies, ");
 		if (isOptimizeBSIC()) sb.append("BSIC, ");
 		if (isOptimizeHSN()) sb.append("HSN, ");
 		if (isOptimizeMAIO()) sb.append("MAIO");
 		
 		sb.append("\n\nFrequency Bands:\n");
 		for (int i = 0; i < BAND_NAMES.length; i++){
 			if (frequencyBands[i]){
 				sb.append("\t" + BAND_NAMES[i] + " : " + availableFreq[i] + "\n");
 			}
 		}
 		
 		sb.append("\nChannel Types: \n\t");
 		for (int i = 0; i < CHANNEL_NAMES.length; i++){
 			if (channelTypes[i]){
 				sb.append(CHANNEL_NAMES[i] + ", ");
 			}
 		}
 		 
 		sb.append("\n\nSummary: \n");
 		
 		int[][] selectedCount = getSelectedCount();
 		for (int i = 0; i < GOALS_SUMMARY_ROW_HEADERS.length; i++){
 	    	int total = 0;
 	    	for (int j = 0; j < selectedCount[i].length; j++){
 	    		total += selectedCount[i][j];
 	    	}
 	    	String s = String.format("\n\tTotal-%d \n\tBand 900-%d \n\tBand 1800-%d \n\tBand 850-%d \n\tBand 1900-%d", total, selectedCount[i][0], selectedCount[i][1], selectedCount[i][2], selectedCount[i][3]);
 	    	sb.append(GOALS_SUMMARY_ROW_HEADERS[i] + "  "+ s + "\n");
 	    }
 		
 		sb.append("\nAvailable BSIC: \n");
 		sb.append("\tNCCs: ");
 		for (int i  = 0; i < 8; i++){
 			if((availableNCCs & (0x01 << i)) > 0) sb.append(Integer.toString(i) + ",");
 		}
 		
 		sb.append("\n\tBCCs: ");
 		for (int i  = 0; i < 8; i++){
 			if((availableBCCs & (0x01 << i)) > 0) sb.append(Integer.toString(i) + ",");
 		}
 		
 		sb.append("\n\nFrequency Type Domains: \n");
 		for(AfpFrequencyDomainModel domainModel: this.freqDomains.values()) {
 			if(domainModel != null) {
 				sb. append(domainModel.getName() + " : \n\tBand-" + domainModel.getBand() + "\n\tAssigned Frequencies-"+ domainModel.getCount() + "\n");
 				sb.append("\tAssignedTRXs-" + domainModel.getNumTRX()  + "\n\tFilters: " + domainModel.getFilters() + "\n");
 			}
 		}
 		
 		sb.append("\nMAL Domains: \n");
 		for(AfpHoppingMALDomainModel malDomainModel :getMalDomains(true)) {
 			if(malDomainModel != null) {
 				sb. append(malDomainModel.getName() + ":  AssignedTRXs-" + malDomainModel.getNumTRX() + "\n\tFilters: " + malDomainModel.getFilters() + "\n");
 			}
 		}
 		
 		sb.append("\nSeparation Rules: \n");
 		sb.append("Sector Domains: \n");
 		for(AfpSeparationDomainModel sectorDomainModel :sectorSeparationDomains.values()){
 			if(sectorDomainModel != null){
 				sb.append("\t" + sectorDomainModel.getName() + " : AssignedSectors- 0" + "\n");
 			}
 		}
 		
 		sb.append("Site Domains: \n");
 		for(AfpSeparationDomainModel siteDomainModel :siteSeparationDomains.values()){
 			if(siteDomainModel != null){
 				sb.append("\t" + siteDomainModel.getName() + " : AssignedSectors- 0" + "\n");
 			}
 		}
 		
 		sb.append("\nScaling Rules: \n");
 		sb.append("Separations: \n");
 		for (int i = 0; i < SCALING_PAGE_ROW_HEADERS.length; i++){
 			String s = String.format("\n\tSector-%3.1f \n\tSite-%3.1f",sectorSeparation[i], siteSeparation[i]);
 			sb.append(SCALING_PAGE_ROW_HEADERS[i][0] + "-" + SCALING_PAGE_ROW_HEADERS[i][1] + " : "+ s + "\n");
 		}
 		
 		sb.append("\nInterference Matrices: \n");
 		for (int i = 0; i < SCALING_PAGE_ROW_HEADERS.length; i++){
 			String s = String.format("\n\tInterference: Co-%3.1f, Adj-%3.1f \n\tNeighbor: Co-%3.1f, Adj-%3.1f " +
 					"\n\tTriangulation: Co-%3.1f, Adj-%3.1f \n\tShadowing: Co-%3.1f, Adj-%3.1f", 
 					coInterference[i], adjInterference[i], coNeighbor[i], adjNeighbor[i], 
 					coTriangulation[i], adjTriangulation[i], coShadowing[i], adjShadowing[i]);
 			sb.append(SCALING_PAGE_ROW_HEADERS[i][0] + "-" + SCALING_PAGE_ROW_HEADERS[i][1] + " : "+ s + "\n");
 		}
 		
 		
 		
 		return sb.toString();
 	}
 	
 	public static Pair<String[],String[]> convertFreqString2Array(String frequenciesText, String frequencies[]) {
 		int numSelected = 0;
 		String[] frequenciesLeft = null;
 		String[] selectedRanges = new String[]{};
 
 		if(frequenciesText != null) {
 			if (!frequenciesText.trim().equals(""))
 				selectedRanges = frequenciesText.split(",");
 		}
 		
 		if (selectedRanges.length > 0 && selectedRanges[0] != null && !selectedRanges[0].trim().equals("")){
 			String[] selected = rangeArraytoArray(selectedRanges);
 			numSelected = selected.length;
 			frequenciesLeft = new String[frequencies.length - selected.length];
 			
 			Arrays.sort(selected);
 			int i = 0;
 			for (String item: frequencies){
 				if(i >= frequenciesLeft.length)
 					break;
 				if (Arrays.binarySearch(selected, item) < 0){
 					frequenciesLeft[i] = item;
 					i++;
 				}		
 			}
 		}
 		else {
 			frequenciesLeft = frequencies;
 		}
 		frequencies = frequenciesLeft;
 		return new Pair<String[], String[]>(selectedRanges,frequencies);
 	}
 	/**
 	 * Converts string array containing integer values and ranges to string array containing int values only
 	 * For example {"0","2","4","8-10","12","13","15-20", "22"} is converted to {"0","2","4","8","9","10","12","13","15","16","17","18","19","20", "22"} 
 	 * @param rangeArray string array containing string representations of int and/or ranges (eg. 9-12 implies 9,10,11,12) 
 	 * @return sorted string array containing only string representations of int and no ranges.
 	 */
 	public static String[] rangeArraytoArray(String[] rangeArray){
 		ArrayList<String> list = new ArrayList<String>();
 		for (String item : rangeArray){
 			int index = item.indexOf("-");
 			if (index == -1){
 				list.add(item);
 			}
 			else{
 				int start = Integer.parseInt(item.substring(0,index).trim());
 				int end = Integer.parseInt(item.substring(index + 1).trim());
 				for (int i = start; i<= end; i++){
 					list.add(Integer.toString(i));
 				}
 			}
 		}
 		
 		String[] stringArray = new String[list.size()];
 		int[] intArray = new int[list.size()];
 		list.toArray(stringArray);
 		for (int i = 0; i < stringArray.length; i++){
 			intArray[i] = Integer.parseInt(stringArray[i].trim());
 		}
 		
 		Arrays.sort(intArray);
 		for (int i = 0; i < intArray.length; i++){
 			stringArray[i] = Integer.toString(intArray[i]);
 		}
 		
 		return stringArray;
 	}
 
 	public String[] getFrequencyArray(int band){
 		String frequencies[] = null;
 		if (band == AfpModel.BAND_900) {
 			frequencies= new String[(124 - 0 + 1) + (1023 - 955 + 1)];
 			for (int i = 0; i < frequencies.length; i++) {
 				if (i <= 124)
 					frequencies[i] = Integer.toString(i);
 				else
 					frequencies[i] = Integer.toString(i + 955 - 124 + 1);
 			}
 		} else if(band == AfpModel.BAND_1800) {
     			frequencies = new String[885-512+1];
     			for (int i = 0; i < frequencies.length; i++){
     				frequencies[i] = Integer.toString(512 + i); 
     			}
 		}else if(band == AfpModel.BAND_850) {
     			frequencies = new String[251-128+1];
     			for (int i = 0; i < frequencies.length; i++){
     				frequencies[i] = Integer.toString(251 + i); 
     			}
 		} else if(band == AfpModel.BAND_1900) {
     			frequencies = new String[810-512+1];
     			for (int i = 0; i < frequencies.length; i++){
     				frequencies[i] = Integer.toString(512 + i); 
     			}
     	}
 		return frequencies;
 	}
 	
 	
 	/**
 	 * Converts string array containing integer values to string array containing int values and/or ranges (wherever applicable)
 	 * For example {"0","2","4","8","9","10","12","13","15","16","17","18","19","20", "22"} is converted to {"0","2","4","8-10","12","13","15-20", "22"}
 	 * @param array An string array containing string representations of int values (no ranges)
 	 * @return
 	 */
 	public static String[] arrayToRangeArray(String array[]){
 
 		int lastItem = -1;
 		int rangeFirstItem = -1;
 		String range = null;
 		boolean isRange = false;
 		int[] rangeArray = new int[array.length];
 		
 		for (int i = 0; i < array.length; i++){
 			rangeArray[i] = Integer.parseInt(array[i].trim());
 		}
 		
 		Arrays.sort(rangeArray);
 		
 		ArrayList<String> list = new ArrayList<String>();
 		for (int currItem : rangeArray){
 			if (lastItem >= 0 && currItem == lastItem + 1){
 				range = "" + rangeFirstItem + "-" +  currItem;
 				isRange = true;
 				lastItem = currItem;
 			}
 			else {
 				rangeFirstItem = currItem;
 				if (isRange){
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
 		else list.add(Integer.toString(lastItem));
 		
 	
 		return list.toArray(new String[0]);
 	}
 	
 
 }
