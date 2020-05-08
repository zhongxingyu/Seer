 // ActivePaths.java:  a plugin for CytoscapeWindow,
 // which uses VERA & SAM expression data
 // to propose active gene regulatory paths
 //------------------------------------------------------------------------------
 // $Revision: 11526 $
 // $Date: 2007-09-05 14:14:24 -0700 (Wed, 05 Sep 2007) $
 // $Author: rmkelley $
 //------------------------------------------------------------------------------
 package csplugins.jActiveModules;
 //------------------------------------------------------------------------------
 import org.cytoscape.application.CyApplicationManager;
 import org.cytoscape.application.swing.CySwingApplication;
 import org.cytoscape.event.CyEventHelper;
 import org.cytoscape.model.CyNode;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import csplugins.jActiveModules.data.ActivePathFinderParameters;
 import csplugins.jActiveModules.util.Scaler;
 import csplugins.jActiveModules.util.ScalerFactory;
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNetworkFactory;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.property.CyProperty;
 //import cytoscape.data.Semantics;
 import org.cytoscape.task.creation.LoadVisualStyles;
 import org.cytoscape.view.layout.CyLayoutAlgorithm;
 import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.view.model.CyNetworkViewFactory;
 import org.cytoscape.view.model.CyNetworkViewManager;
 import org.cytoscape.view.vizmap.VisualMappingManager;
 import org.cytoscape.view.vizmap.VisualStyle;
 import org.cytoscape.work.TaskManager;
 
 import csplugins.jActiveModules.util.SelectUtil;
 import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
 import org.cytoscape.model.subnetwork.CyRootNetwork;
 import org.cytoscape.model.subnetwork.CySubNetwork;
 import org.cytoscape.model.CyRow;
 import java.util.Collection;
 import java.io.File;
 import org.cytoscape.work.AbstractTask;
 import org.cytoscape.work.TaskMonitor;
 import csplugins.jActiveModules.ServicesUtil;
 
 
 //-----------------------------------------------------------------------------------
 public class ActivePaths extends AbstractTask implements ActivePathViewer {
 
 	private static final Logger logger = LoggerFactory.getLogger(ActivePaths.class);
 
 	protected boolean showTable = true;
 	protected boolean hideOthers = true;
 	protected boolean randomize = false;
 
 	protected JMenuBar menubar;
 	protected JMenu expressionConditionsMenu;
 	protected String currentCondition = "none";
 	protected Component[] activePaths;
 	private Vector<Component> activePathsVect= new Vector<Component>();
 	
 	protected String[] attrNames;
 	protected static boolean activePathsFindingIsAvailable;
 	protected JButton activePathToolbarButton;
 	protected JFrame mainFrame;
 	protected CyNetwork cyNetwork;
 	protected String titleForCurrentSelection;
 	protected ActivePathFinderParameters apfParams;
 	protected static double MIN_SIG = 0.0000000000001;
 	protected static double MAX_SIG = 1 - MIN_SIG;
 
 	protected static int resultsCount = 1;
 	protected ActiveModulesUI parentUI;
 	
 	private static int MAX_NETWORK_VIEWS; // = PropUtil.getInt(CytoscapeInit.getProperties(), "moduleNetworkViewCreationThreshold", 5);
 	private static int runCount = 0;	
 	
 	private static final URL vizmapPropsLocation = ActiveModulesUI.class.getResource("/jActiveModules_VS.props");
 	
 	private static final String VS_OVERVIEW_NAME = "jActiveModules Overview Style";
 	private static final String VS_MODULE_NAME = "jActiveModules Module Style";
 	private static  VisualStyle overviewVS;
 	private static  VisualStyle moduleVS;
 	
 	// This is common prefix for all finders.
 	private static final String MODULE_FINDER_PREFIX = "jActiveModules.";
 	private static final String EDGE_SCORE = MODULE_FINDER_PREFIX + "overlapScore";
 	
 	private static final String NODE_SCORE = MODULE_FINDER_PREFIX + "activepathScore";
 
 //	static {
 //		
 //		// Create visualStyles based on the definition in property files
 //		Cytoscape.firePropertyChange(Cytoscape.VIZMAP_LOADED, null, vizmapPropsLocation);
 //		//overviewVS = Cytoscape.getVisualMappingManager().getCalculatorCatalog().getVisualStyle(VS_OVERVIEW_NAME);
 //		//moduleVS = Cytoscape.getVisualMappingManager().getCalculatorCatalog().getVisualStyle(VS_MODULE_NAME);
 //	}
 	private static boolean eventFired = false;
 	
 	private static CyLayoutAlgorithm layoutAlgorithm; // = CyLayouts.getLayout("force-directed");
 	
 	private static boolean visualStylesLoaded = false;
 	
 	// ----------------------------------------------------------------
 	public ActivePaths(CyNetwork cyNetwork, ActivePathFinderParameters apfParams, ActiveModulesUI parentUI) {
 		this.apfParams = apfParams;
 
 		try {			
 			MAX_NETWORK_VIEWS = new Integer(ServicesUtil.cytoscapePropertiesServiceRef.getProperties().getProperty("moduleNetworkViewCreationThreshold")).intValue();			
 		}
 		catch (Exception e){
 			MAX_NETWORK_VIEWS = 5;
 		}
 		layoutAlgorithm = ServicesUtil.cyLayoutsServiceRef.getLayout("force-directed");
 		//if (!eventFired){
 		//	this.cyEventHelperService.fireEvent();
 		//}
 
 //		Set<VisualStyle> visualStyles = this.visualMappingManager.getAllVisualStyles(); 
 
 		// Load jActiveModules defined Visual styles if not loaded yet
 //		Set<VisualStyle> visualStyles= null;
 //		if (!visualStylesLoaded){			
 //			File f = new File(vizmapPropsLocation.toString());
 //			visualStyles = this.loadVizmapFileTaskFactory.loadStyles(f);
 //			visualStylesLoaded = true;
 //		}
 //
 //		// Get the styles for overview and module
 //		Iterator<VisualStyle> it = visualStyles.iterator();
 //		while (it.hasNext()){
 //			VisualStyle vs = it.next();
 //			if (vs.getTitle().equalsIgnoreCase(VS_OVERVIEW_NAME)){
 //				overviewVS = vs;
 //			}
 //			if (vs.getTitle().equalsIgnoreCase(VS_MODULE_NAME)){
 //				moduleVS = vs;
 //			}
 //		}
 //			
 //		System.out.println("overviewVS = "+ overviewVS);
 //		System.out.println("moduleVS = "+ moduleVS);
 			
 		
 		//
 		if (cyNetwork == null || cyNetwork.getNodeCount() == 0) {
 			throw new IllegalArgumentException("Please select a network");
 		}
 
 		attrNames = (String[])apfParams.getExpressionAttributes().toArray(new String[0]);
 		Arrays.sort(attrNames);
 		if (attrNames.length == 0) {
 			throw new RuntimeException("No expression data selected!");
 		}
 		menubar = ServicesUtil.cySwingApplicationServiceRef.getJMenuBar();
 		mainFrame = ServicesUtil.cySwingApplicationServiceRef.getJFrame(); //Cytoscape.getDesktop();
 		this.cyNetwork = cyNetwork;
 		this.parentUI = parentUI;
 		
 	} // ctor
 
 	// --------------------------------------------------------------
 	protected void setShowTable(boolean showTable) {
 		this.showTable = showTable;
 	}
 	protected void clearActivePaths() {
 		this.activePaths = null;
 	}
 
 	public void run(TaskMonitor taskMonitor) {
 
 //		System.out.println("\nEntering ActivePaths.run()....");
 //
 //		System.out.println("\napfParams.toString()=\n"+apfParams.toString()+"\n");
 
 		
 	    System.gc();
 		//long start = System.currentTimeMillis();
 		HashMap expressionMap = generateExpressionMap();
 
 		// run the path finding algorithm
 		final ActivePathsFinder apf =
 			new ActivePathsFinder(expressionMap, attrNames, cyNetwork, apfParams,
 					      randomize ? null : mainFrame, parentUI, this.activePathsVect);
 		
 		//activePaths = apf.findActivePaths();
 		   
 		ActivePathsTaskFactory factory = new ActivePathsTaskFactory(apf);
 		ServicesUtil.synchronousTaskManagerServiceRef.execute(factory);
 				
 		activePaths = new Component[activePathsVect.size()];
 		for (int i=0; i< activePathsVect.size(); i++){
 			activePaths[i] = activePathsVect.get(i);
 		}		
 		
 		// create nested networks
 		//1 . create subnetwork for each path
 		CyNetwork[] subnetworks = createSubnetworks();
 		
 		for (int i=0; i<subnetworks.length; i++){
 			ServicesUtil.cyNetworkManagerServiceRef.addNetwork(subnetworks[i]);			
 		}
 		
 		//2. create an overview network for all nested network
 		final CyNetwork overview = ServicesUtil.cyNetworkFactoryServiceRef.getInstance();
 		overview.getCyRow().set("name", "jActiveModules Search Result "+ runCount++);
 		
 		Set<CyNode>  path_nodes = new HashSet<CyNode>();
 		for (int i=0; i< subnetworks.length; i++){
 			CyNode newNode = overview.addNode(); //Cytoscape.getCyNode(subnetworks[i].getTitle(), true);
 			newNode.getCyRow().set("name", subnetworks[i].getCyRow().get("name", String.class));
 			path_nodes.add(newNode);
 			newNode.setNetwork(subnetworks[i]);
 			// create an attribute for this new node
 			//Cytoscape.getNodeAttributes().setAttribute(newNode.getIdentifier(), NODE_SCORE, new Double(activePaths[i].getScore()));
 			
 			if (overview.getDefaultNodeTable().getColumn(NODE_SCORE)== null){
 				overview.getDefaultNodeTable().createColumn(NODE_SCORE, Double.class, false);
 			}
 			newNode.getCyRow().set(NODE_SCORE, new Double(activePaths[i].getScore()));
 		}
 
 		ServicesUtil.cyNetworkManagerServiceRef.addNetwork(overview);
 		
 		//Edges indicate that nodes in nested networks exist in both nested networks
 		Set<CyEdge>  path_edges = getPathEdges(overview, path_nodes); //new HashSet<CyEdge>();
 		//final CyNetwork overview = Cytoscape.createNetwork(path_nodes, path_edges, "jActiveModules Search Result "+ runCount++, cyNetwork, false);
 
 		
 		//3. Create an edge attribute "overlapScore", which is defined as NumberOfSharedNodes/min(two network sizes)
 		
 		overview.getDefaultEdgeTable().createColumn("jActiveModules_nodeCount_min_two", Integer.class, false);
 		overview.getDefaultEdgeTable().createColumn("jActiveModules_nodeOverlapCount", Integer.class, false);
 		overview.getDefaultEdgeTable().createColumn(EDGE_SCORE, Double.class, false);
 		
 		CyTable cyEdgeAttrs = this.cyNetwork.getDefaultEdgeTable(); //Cytoscape.getEdgeAttributes();
 		Iterator it = path_edges.iterator();
 		while(it.hasNext()){
 			CyEdge aEdge = (CyEdge) it.next();
 			int NumberOfSharedNodes = getNumberOfSharedNodes((CyNetwork)aEdge.getSource().getNetwork(), 
 					(CyNetwork)aEdge.getTarget().getNetwork());
 			
 			int minNodeCount = Math.min(aEdge.getSource().getNetwork().getNodeCount(), 
 								aEdge.getTarget().getNetwork().getNodeCount());
 
 			//cyEdgeAttrs.setAttribute(aEdge.getIdentifier(), "jActiveModules_nodeCount_min_two", minNodeCount);
 			aEdge.getCyRow().set("jActiveModules_nodeCount_min_two", minNodeCount);
 			
 			//cyEdgeAttrs.setAttribute(aEdge.getIdentifier(), "jActiveModules_nodeOverlapCount", NumberOfSharedNodes);
 			aEdge.getCyRow().set("jActiveModules_nodeOverlapCount", NumberOfSharedNodes);
 			double overlapScore = (double)NumberOfSharedNodes/minNodeCount;
 			//cyEdgeAttrs.setAttribute(aEdge.getIdentifier(), EDGE_SCORE, overlapScore);
 			aEdge.getCyRow().set(EDGE_SCORE, overlapScore);
 		}
 
		//4. Create an view for overview network and apply visual style
 		//Cytoscape.createNetworkView(overview, overview.getIdentifier(), tuning(), null);
 		final CyNetworkView newView = ServicesUtil.cyNetworkViewFactoryServiceRef.getNetworkView(overview);
 		ServicesUtil.cyNetworkViewManagerServiceRef.addNetworkView(newView);
 		
//		this.visualMappingManager.setVisualStyle(overviewVS, newView);

		//newView.updateView();

 		// Apply layout for overview
		//CyLayoutAlgorithm alg = ServicesUtil.cyLayoutsServiceRef.getLayout("force-directed");
 		layoutAlgorithm.setNetworkView(newView);
		ServicesUtil.taskManagerServiceRef.execute(layoutAlgorithm);				
 
		
 		// Create view for top n modules
 		int n = -1;
 		try {
 			n= new Integer(ServicesUtil.cytoscapePropertiesServiceRef.getProperties().getProperty(ActiveModulesUI.JACTIVEMODULES_TOP_N_MODULE)).intValue();			
 		}
 		catch(Exception e){
 			n= 5;
 		}
 
 		 if (n> subnetworks.length){
 			 n = subnetworks.length;
 		 }
 
 		 for (int i=0; i<n; i++){
 				CyNetworkView theView = ServicesUtil.cyNetworkViewFactoryServiceRef.getNetworkView(subnetworks[i]);
 				ServicesUtil.cyNetworkViewManagerServiceRef.addNetworkView(theView);
				
 //				this.visualMappingManager.setVisualStyle(moduleVS, theView);
 //				theView.updateView();
				
//				CyLayoutAlgorithm alg_f = ServicesUtil.cyLayoutsServiceRef.getLayout("force-directed");
				layoutAlgorithm.setNetworkView(theView);
				
				ServicesUtil.taskManagerServiceRef.execute(layoutAlgorithm);				
 		 }
 	}
 	
 
 	private static int getNumberOfSharedNodes(CyNetwork networkA, CyNetwork networkB){
 		
 		Long[] nodeIndicesA = new Long[networkA.getNodeCount()];
 		Long[] nodeIndicesB = new Long[networkB.getNodeCount()];
 		
 		Iterator<CyNode> it = networkA.getNodeList().iterator();
 		int iA=0;
 		while (it.hasNext()){
 			nodeIndicesA[iA] = it.next().getSUID();
 			iA++;
 		}
 		
 		Iterator<CyNode> it2 = networkB.getNodeList().iterator();
 		int iB=0;
 		while (it2.hasNext()){
 			nodeIndicesB[iB] = it2.next().getSUID();
 			iB++;
 		}
 		
 		HashSet<Long> hashSet = new HashSet<Long>();
 		for (int i=0; i< nodeIndicesA.length; i++){
 			hashSet.add( new Long(nodeIndicesA[i]));
 		}
 
 		int sharedNodeCount =0;
 		for (int i=0; i< nodeIndicesB.length; i++){
 			if (hashSet.contains(new Long(nodeIndicesB[i]))){
 				sharedNodeCount++;
 			}
 		}
 		
 		return sharedNodeCount;
 	}
 	
 
 	private Set<CyEdge> getPathEdges(CyNetwork overview, Set path_nodes) {
 		HashSet<CyEdge> edgeSet = new HashSet<CyEdge>();
 		
 		Object[] nodes = path_nodes.toArray();
 		
 		HashSet[] hashSet = new HashSet[nodes.length];
 		for (int i=0; i< nodes.length; i++){
 			hashSet[i] = new HashSet<CyNode>(((CyNode)nodes[i]).getNetwork().getNodeList());
 		}
 		
 		for (int i=0; i< nodes.length-1; i++){
 			for (int j=i+1; j<nodes.length; j++){
 				// determine if there are overlap between nested networks
 				if (hasTwoSetOverlap(hashSet[i], hashSet[j])){
 					//CyEdge edge = Cytoscape.getCyEdge((CyNode)nodes[i], (CyNode)nodes[j], Semantics.INTERACTION, "overlap", true);
 					CyEdge newEdge = overview.addEdge((CyNode)nodes[i], (CyNode)nodes[j], false);
 					edgeSet.add(newEdge);
 				}
 			}
 		}
 		
 		return edgeSet;
 	}
 	
 	
 	private boolean hasTwoSetOverlap(HashSet<CyNode> set1, HashSet<CyNode> set2) {
 		Iterator<CyNode> it = set1.iterator();
 		while (it.hasNext()){
 			if (set2.contains(it.next())){
 				return true;
 			}
 		}		
 		return false;
 	}
 	
 	
 	private CySubNetwork[] createSubnetworks() {
 		//CyNetwork[] subnetworks = new CyNetwork[activePaths.length];
 
 		CySubNetwork[] subnetworks2 = new CySubNetwork[activePaths.length];
 		
 		CyRootNetwork rootNetwork = ServicesUtil.cyRootNetworkFactory.convert(cyNetwork);
 		
 		for (int i = 0; i < activePaths.length; i++) {
 			Component thePath = activePaths[i];
 			String pathName = "Module_" + runCount + "_" + (i + 1);
 			
 			// get nodes for this path
 			Vector nodeVect = (Vector) thePath.getDisplayNodes();
 			Set<CyNode> nodeSet = new HashSet<CyNode>();
 			for (int j = 0; j < nodeVect.size(); j++) {
 				CyNode oneNode = (CyNode) nodeVect.elementAt(j);
 				if (oneNode != null)
 					nodeSet.add(oneNode);
 			}
 			
 			// get edges for this path
 			Set edgeSet = new HashSet();
 			Iterator iterator = cyNetwork.getEdgeList().iterator(); //.edgesIterator();
 			while (iterator.hasNext()) {
 				CyEdge edge = (CyEdge) iterator.next();
 				if (nodeSet.contains(edge.getSource()) && nodeSet.contains(edge.getTarget()))
 					edgeSet.add(edge);
 			}
 
 			//subnetworks[i] = Cytoscape.createNetwork(nodeSet, edgeSet, pathName, cyNetwork, false);
 			subnetworks2[i] = rootNetwork.addSubNetwork(nodeSet, edgeSet);
 			subnetworks2[i].getCyRow().set("name", pathName);
 			
 //			if(i < MAX_NETWORK_VIEWS) {
 //				//final CyNetworkView moduleView = Cytoscape.createNetworkView(subnetworks[i], subnetworks[i].getTitle(), tuning());
 //				final CyNetworkView moduleView = this.cyNetworkViewManager.getNetworkView(subnetworks2[i].getSUID());
 //				//moduleView.setVisualStyle(moduleVS.getTitle()); //.getName());
 //
 ////				moduleView.updateView(); //.redrawGraph(false, true);
 //			} 
 		}
 		
 		return subnetworks2;
 	}
 	
 	/**
 	 * Returns the best scoring path from the last run. This is mostly used by
 	 * the score distribution when calculating the distribution
 	 */
 	protected Component getHighScoringPath() {
 		logger.info("High Scoring Path: " + activePaths[0].toString());
 		logger.info("Score: " + activePaths[0].getScore());
 		int size = activePaths[0].getNodes().size();
 		logger.info("Size: " + size);
 		logger.info("Raw score: " + activePaths[0].calculateSimpleScore());
 		logger.info("Mean: " + Component.pStats.getMean(size));
 		logger.info("Std: " + Component.pStats.getStd(size));
 		return activePaths[0];
 	}
 
 	protected HashMap generateExpressionMap() {
 		// set up the HashMap which is used to map from nodes
 		// to z values. At this point, we are mapping from the
 		// p values for expression to z values
 		logger.info("Processing Expression Data into Hash");
 		HashMap tempHash = new HashMap();
 		logger.info("Do some testing of the ExpressionData object");
 		CyTable nodeAttributes = this.cyNetwork.getDefaultNodeTable();
 
 		// Create two identical lists of genes
 		List<CyNode> geneList = new ArrayList<CyNode>();
 		List<CyNode> shuffledList = new ArrayList<CyNode>();
 		for (Iterator nodeIt = cyNetwork.getNodeList().iterator() /*.nodesIterator()*/; nodeIt.hasNext();) {
 			CyNode n = (CyNode) nodeIt.next();
 			geneList.add(n);
 			shuffledList.add(n);
 		}
 
 		// If randomize, permute the second list of genes
 		if ( randomize ) 
 			Collections.shuffle(shuffledList);
 
 		final Double[][] attribValues = new Double[attrNames.length][geneList.size()];
 		final Map<String, Integer> geneNameToIndexMap = new HashMap<String, Integer>();
 		for (int i = 0; i < geneList.size(); i++) {
 			final String geneName = geneList.get(i).getCyRow().get("name", String.class); //.getIdentifier();
 			
 			geneNameToIndexMap.put(geneName, new Integer(i));
 			for (int j = 0; j < attrNames.length; j++)
 			{
 				//attribValues[j][i] = nodeAttributes.get.getDoubleAttribute(geneName, attrNames[j]);
 				Collection<CyRow> rows = nodeAttributes.getMatchingRows("name", geneName);
 				Object[] objs = rows.toArray();
 				// We assume name is unique, we expect one row returned for each name
 				CyRow row = (CyRow) objs[0];
 				attribValues[j][i] = row.get(attrNames[j], Double.class);
 			}
 		}
 
 		// Perform the scaling:
 		for (int j = 0; j < attrNames.length; j++) {
 			final int index = apfParams.getExpressionAttributes().indexOf(attrNames[j]);
 			final ScalingMethodX scalingMethod = ScalingMethodX.getEnumValue(apfParams.getScalingMethods().get(index));
 			attribValues[j] = scaleInputValues(attribValues[j], scalingMethod);
 		}
 
 		for (int i = 0; i < geneList.size(); i++) {
 		
 			// If not randomizing these will be identical.
 			CyNode current = geneList.get(i); 
 			CyNode shuffle = shuffledList.get(i);
 
 			// If randomizing, you'll get p-values for a different gene. 
 			String canonicalName = shuffle.getCyRow().get("name", String.class); //.getIdentifier();
 
 			double[] tempArray = new double[attrNames.length];
 			for (int j = 0; j < attrNames.length; j++) {
 				final Double d = attribValues[j][geneNameToIndexMap.get(canonicalName)];
 				if (d == null)
 					tempArray[j] = ZStatistics.oneMinusNormalCDFInverse(.5);
 				else {
 					double sigValue = d.doubleValue();
 					if (sigValue < MIN_SIG) {
 						sigValue = MIN_SIG;
 						logger.warn("Warning: value for " + current.getCyRow().get("name", String.class)+ //.getIdentifier() + 
 						                   " (" + canonicalName + ") adjusted to " + MIN_SIG);
 					} 
 					if (sigValue > MAX_SIG) {
 						sigValue = MAX_SIG;
 						logger.warn("Warning: value for " + current.getCyRow().get("name", String.class)+ //.getIdentifier() + 
 						                   " (" + canonicalName + ") adjusted to " + MAX_SIG);
 					} 
 
 					// transform the p-value into a z-value and store it in the
 					// array of z scores for this particular node
 					tempArray[j] = ZStatistics.oneMinusNormalCDFInverse(sigValue);
 				}
 			}
 			tempHash.put(current, tempArray);
 		}
 		logger.info("Done processing into Hash");
 		return tempHash;
 	}
 
 	private Double[] scaleInputValues(final Double[] inputValues, final ScalingMethodX scalingMethod) {
 		if (scalingMethod == ScalingMethodX.NONE)
 			return inputValues;
 
 		int nullCount = 0;
 		for (final Double inputValue : inputValues) {
 			if (inputValue == null)
 				++nullCount;
 		}
 		if (nullCount == inputValues.length)
 			return null;
 
 		final double[] unscaledValues = new double[inputValues.length - nullCount];
 		int i = 0;
 		for (final Double inputValue : inputValues) {
 			if (inputValue != null)
 				unscaledValues[i++] = inputValue;
 		}
 
 		if (scalingMethod == ScalingMethodX.RANK_LOWER || scalingMethod == ScalingMethodX.LINEAR_LOWER) {
 			for (int k = 0; k < unscaledValues.length; ++k)
 				unscaledValues[k] = -unscaledValues[k];
 		}
 
 		final String type;
 		final double from, to;
 		if (scalingMethod == ScalingMethodX.RANK_LOWER || scalingMethod == ScalingMethodX.RANK_UPPER) {
 			from = 0.0;
 			to   = 1.0;
 			type = "rank";
 		} else if (scalingMethod == ScalingMethodX.LINEAR_LOWER || scalingMethod == ScalingMethodX.LINEAR_UPPER) {
 			final double EPS = 0.5 / unscaledValues.length;
 			from = 0.0 + EPS;
 			to   = 1.0 - EPS;
 			type = "linear";
 		} else
 			throw new IllegalArgumentException("unknown scaling method: " + scalingMethod);
 
 		final double[] scaledValues;
 		try {
 			scaledValues = ScalerFactory.getScaler(type).scale(unscaledValues, from, to);
 		} catch (final IllegalArgumentException e) {
 			logger.warn("Scaling failed: " + e.getMessage());
 			return null;
 		}
 
 		final Double[] outputValues = new Double[inputValues.length];
 		int k = 0;
 		i = 0;
 		for (final Double inputValue : inputValues) {
 			if (inputValue == null)
 				outputValues[k++] = null;
 			else
 				outputValues[k++] = scaledValues[i++];
 		}
 
 		return outputValues;
 	}
 
 	/**
 	 * Scores the currently selected nodes in the graph, and pops up a window
 	 * with the result
 	 */
 //	protected void scoreActivePath() {
 //		String callerID = "jActiveModules";
 //		ActivePathsFinder apf = new ActivePathsFinder(generateExpressionMap(),
 //				attrNames, cyNetwork, apfParams, mainFrame, parentUI);
 //
 //		long start = System.currentTimeMillis();
 //		Vector result = new Vector();
 //		Iterator it = SelectUtil.getSelectedNodes(cyNetwork).iterator();
 //		while (it.hasNext()) {
 //			result.add(it.next());
 //		}
 //
 //		double score = apf.scoreList(result);
 //		long duration = System.currentTimeMillis() - start;
 //		logger.info("-------------- back from score: " + duration
 //				+ " msecs");
 //		logger.info("-------------- score: " + score + " \n");
 //		JOptionPane.showMessageDialog(mainFrame, "Score: " + score);
 //	} // scoreActivePath
 
 	/*
 	protected class ActivePathControllerLauncherAction extends AbstractAction {
 		ActivePathControllerLauncherAction() {
 			super("Active Modules");
 		} // ctor
 		public void actionPerformed(ActionEvent e) {
 			//showConditionsVsPathwaysTable();
 		}
 	}
 	*/
 
 	/**
 	 * find all of the unique node names in the full set of active paths. there
 	 * may be duplicates since some nodes may appear in several paths
 	 */
 	protected Vector combinePaths(Component[] activePaths) {
 		HashSet set = new HashSet();
 		for (int i = 0; i < activePaths.length; i++) {
 			set.addAll(activePaths[i].getNodes());
 		} // for i
 		return new Vector(set);
 
 	}
 
 	protected void addActivePathToolbarButton() {
 		// if (activePathToolbarButton != null)
 		// cytoscapeWindow.getCyMenus().getToolBar().remove
 		// (activePathToolbarButton);
 
 		// activePathToolbarButton =
 		// cytoscapeWindow.getCyMenus().getToolBar().add (new
 		// ActivePathControllerLauncherAction ());
 
 	} // addActivePathToolbarButton
 	// ------------------------------------------------------------------------------
 	public void displayPath(Component activePath, boolean clearOthersFirst, String pathTitle) {
 		titleForCurrentSelection = pathTitle;
 		//SelectFilter filter = cyNetwork.getSelectFilter();
 		// cytoscapeWindow.selectNodesByName (activePath.getNodes (),
 		// clearOthersFirst);
 		if (clearOthersFirst) {
 			// cyNetwork.unFlagAllNodes();
 			//filter.unselectAllNodes();
 			Iterator<CyNode> it = this.cyNetwork.getNodeList().iterator();
 			while (it.hasNext()){
 				CyNode node = it.next();
 				node.getCyRow().set(CyNetwork.SELECTED, false);
 			}
 		}
 		//filter.setSelectedNodes(activePath.getDisplayNodesGeneric(), true);
 		Iterator<CyNode> it2 = activePath.getDisplayNodesGeneric().iterator();
 		while (it2.hasNext()){
 			CyNode node2 = it2.next();
 			node2.getCyRow().set(CyNetwork.SELECTED, true);
 		}
 		
 		// cyNetwork.setFlaggedNodes(activePath.getNodes(),true);
 		ServicesUtil.cyNetworkViewManagerServiceRef.getNetworkView(ServicesUtil.cyApplicationManagerServiceRef.getCurrentNetwork().getSUID()).updateView(); 
 		//Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 	}
 	// ------------------------------------------------------------------------------
 	public void displayPath(Component activePath,
 			String pathTitle) {
 		displayPath(activePath, true, pathTitle);
 	}
 	// ------------------------------------------------------------------------------
 	
 	/*
 	private CyLayoutAlgorithm tuning() {
 		final CyLayoutAlgorithm fd = layoutAlgorithm;
 	
 		fd.getSettings().get("defaultSpringLength").setValue("140");
 		fd.getSettings().get("defaultNodeMass").setValue("9");
 		fd.getSettings().updateValues();
 		fd.updateSettings();
 		
 		return fd;
 	}
 */
 } // class ActivePaths (a CytoscapeWindow plugin)
