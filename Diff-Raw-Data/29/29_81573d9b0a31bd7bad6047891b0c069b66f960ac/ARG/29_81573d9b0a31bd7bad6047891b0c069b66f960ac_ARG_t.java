 package arg;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import arg.ARGChangeEvent.ChangeType;
 import arg.argIO.ARGParser;
 import arg.argIO.ARGWriter;
 
 import logging.StringUtils;
 import math.RandomSource;
 import mcmc.MCMC;
 import modifier.ARGModifier;
 import modifier.IllegalModificationException;
 import modifier.ModificationImpossibleException;
 
 import cern.jet.random.Exponential;
 import dlCalculation.computeCore.ComputeCore;
 import parameter.AbstractParameter;
 import parameter.InvalidParameterValueException;
 import parameter.Parameter;
 import parameter.ParameterListener;
 import sequence.Alignment;
 import sequence.CharacterColumn;
 import sequence.DNAUtils;
 import sequence.DataMatrix;
 
 /**
  * A collection of nodes joined by edges. Nodes are of 3 types, CoalNodes, RecombNodes, and TipNodes.  
  * @author brendan
  *
  */
 public class ARG extends AbstractParameter<ARG> implements ParameterListener {
 
 	public static final String XML_COLLECT = "collectPatterns";
 	
 	private List<CoalNode> coalNodes = new ArrayList<CoalNode>(200);
 	private List<RecombNode> recombNodes = new ArrayList<RecombNode>(200);
 	private List<TipNode> tips = new ArrayList<TipNode>(200);
 	
 	private ARGNode[] currentSortedNodes = null;
 	private ARGNode[] proposedSortedNodes = null;
 	private ARGNode[] activeSortedNodes = null;
 	
 	//Stores nodes that have been proposed to be added. Note that these nodes are also added to 
 	//their respective list (coalNodes, recombNodes..), so that when we calculate something like ARGIntervals
 	//the proposed state is all there. The list below is just a handy reference so we know what 
 	//nodes to remove if the state is rejected. 
 	private List<ARGNode> proposedNodeAdds = new ArrayList<ARGNode>(4);
 	
 	//Stores nodes that have been proposed to be removed. Similar to list above, but stores nodes
 	//that are candidates for removal. If state is rejected we add these all back to the right lists. 
 	private List<ARGNode> proposedNodeRemoves = new ArrayList<ARGNode>(4);
 			
 	//The number of sites associated with this ARG. There can't be an ARG without a number of sites. 
 	protected int siteCount;
 	
 	//List of changes that have happened to this arg since the last call to processChanges
 	private List<ARGChangeEvent> changeEvents = new ArrayList<ARGChangeEvent>();
 	
 	//Summary of intervals for this arg. We maintain an active/proposed scheme similar to
 	//changes for nodes, where activeIntervals points to either current or proposed. Certain
 	//types of node changes (but not all of them) alter the intervals state, which causes
 	//a new intervals set to be generated and active to point to proposed. Accepting means
 	//swapping the proposed into the current state. Rejection means simply setting the active
 	//back to the current state. 
 	private ARGIntervals currentIntervals = new ARGIntervals();
 	private ARGIntervals proposedIntervals = new ARGIntervals();
 	private ARGIntervals activeIntervals = currentIntervals;
 		
 	private ComputeCore computeCore = null;
 	
 	//A flag that indicated whether a 'structural' change has occurred since the last call to
 	// accept or reject. Not used, currently. 
 	private boolean structureHasChanged = true;
 	
 	//A flag indicating whether or not any nodes with coalescing sites have been altered since the last call to accept or reject
 	private boolean requiresDLRecalc = true;
 	
 	//Counter for calls to accept / reject
 	private int calls = 0;
 	
 	//Used to assign unique numbers to nodes
 	private int nextNodeNumber = 0;
 	
 	//Various properties that we can log
 	private String[] logKeys = new String[]{/*"root.height",*/ "visible.height", "visible.bps", "total.bps", "num.patterns", "num.nodes"}; 

 	
 	final int rangePoolSize = 1000;
 	private Stack<SiteRangeList> rangePool = new Stack<SiteRangeList>();
 	private Stack<CoalRangeList> coalRangePool = new Stack<CoalRangeList>();
 	
 	private NodeHeightComparator heightComparator = new NodeHeightComparator();
 
 	//Stores some information about the alignment that we need to give to other objects
 	private DataMatrix dataMatrix = null;
 	
 	public ARG() {
 		super(new HashMap<String, String>());
 		//Create an empty arg. 
 	}
 	
 	
 	/**
 	 * Constructor for building a tree with no data or modifiers. We look for the attributes "theta", "tips" or "file", specifying
 	 * the initial theta and number of tips, or the file to read the tree from. 
 	 * @param attrMap
 	 */
 	public ARG(Map<String, String> attrMap) {
 		super(attrMap);
 		generateInitialTree(attrMap, null);
 	}
 	
 	/**
 	 * Constructs a random tree, requires supplying a tips="X" to yield number of tips
 	 * @param attrMap
 	 * @param mods
 	 */
 	public ARG(Map<String, String> attrMap, List<Object> mods) {
 		this(attrMap);
 		
 		for(int i=0; i<mods.size(); i++) {
 			this.addModifier( (ARGModifier)mods.get(i));
 		}	
 	}
 	
 	/**
 	 * No modifier list constructor
 	 * @param attrMap
 	 * @param alignment
 	 * @param data
 	 */
 	public ARG(Map<String, String> attrMap, Alignment alignment) {
 		this(attrMap, alignment, new ArrayList<Object>());
 	}
 	
 	public ARG(Map<String, String> attrMap, Newick newick, Alignment alignment) {
 		this(attrMap, newick, alignment, new ArrayList<Object>());
 	}
 
 	/**
 	 * Create a tree with the given newick string, but no data. 
 	 * @param attrMap
 	 * @param newick
 	 * @param mods
 	 */
 	public ARG(Map<String, String> attrMap, Newick newick) {
 		this(attrMap, newick, new ArrayList<Object>());
 	}
 	
 	/**
 	 * Create a tree with the given newick string and modifiers, but no data. 
 	 * @param attrMap
 	 * @param newick
 	 * @param mods
 	 */
 	public ARG(Map<String, String> attrMap, Newick newick, List<Object> mods) {
 		super(attrMap); 
 		CoalNode root = TreeUtils.buildTreeFromNewick(newick.getNewick(), this);
 		if (!attrMap.containsKey(XML_PARAM_FREQUENCY)) {
 			frequency = 20.0;
 		}
 		
 		addNodesFromTree(root);
 		siteCount = 1;
 		initializeRanges();
 		acceptValue();
 		if (mods != null) {
 			for(int i=0; i<mods.size(); i++) {
 				this.addModifier( (ARGModifier)mods.get(i));
 			}
 		}
 	}
 	
 	/**
 	 * Constructor that generates a tree by examining the contents of a Newick object...this way the tree can 
 	 * be directly specified in the data file
 	 * 
 	 * @param attrMap
 	 * @param newick
 	 * @param alignment
 	 * @param data
 	 * @param mods
 	 */
 	public ARG(Map<String, String> attrMap, Newick newick, Alignment alignment, List<Object> mods) {
 		super(attrMap); 
 		CoalNode root = TreeUtils.buildTreeFromNewick(newick.getNewick(), this);
 		addNodesFromTree(root);
 		initializeNodes(alignment);
 		this.siteCount = alignment.getSiteCount();
 		
 		//Set default frequency to 20, unless specified otherwise
 		if (!attrMap.containsKey(XML_PARAM_FREQUENCY)) {
 			frequency = 20.0;
 		}
 		
 		String collect = attrMap.get(XML_COLLECT);
 		if (collect != null) {
 			if (collect.equalsIgnoreCase("true")) {
 				throw new IllegalArgumentException("Pattern collecting has been disabled for now.");
 			}
 
 		}
 		
 		for(int i=0; i<mods.size(); i++) {
 			this.addModifier( (ARGModifier)mods.get(i));
 		}
 	}
 	
 
 	
 	public ARG(Map<String, String> attrMap, Alignment alignment, List<Object> mods) {
 		super(attrMap);
 		generateInitialTree(attrMap, alignment);
 		this.siteCount = alignment.getSiteCount();
 				
 		for(int i=0; i<mods.size(); i++) {
 			this.addModifier( (ARGModifier)mods.get(i));
 		}
 	}
 	
 	/**
 	 * Builds a random tree and associates the data given in the alignment with the tips of the tree. 
 	 * Right now, this just gives it a theta of 1.0
 	 * @param alignment
 	 * @param data
 	 */
 	public ARG(Alignment alignment) {
 		this(new HashMap<String, String>(), alignment);
 		this.siteCount = alignment.getSiteCount();
 	}
 	
 	
 	
 	public ARG(String newick) {
 		super(new HashMap<String, String>());
 		CoalNode root = TreeUtils.buildTreeFromNewick(newick, this);
 		addNodesFromTree(root);
 	}
 	
 	/**
 	 *  Constructor for when all you have is a list of nodes. 
 	 * @param nodes
 	 */
 	public ARG(int siteCount, List<ARGNode> nodes) {
 		super(new HashMap<String, String>());
 		initializeFromNodeList(nodes, siteCount);
 	}
 
 	
 	/**
 	 * Initialize this ARG from the given list of nodes 
 	 * @param nodes Nodes to form the ARG
 	 * @param sites Number of sites in sequence
 	 * @param jiggle Jiggle node height so no two nodes have exactly the same height
 	 */
 	public void initializeFromNodeList(List<ARGNode> nodes, int sites) {
 		initializeFromNodeList(nodes, sites, true);
 	}
 	
 	public void initializeFromNodeList(List<ARGNode> nodes, int sites, boolean jiggle) {
 		boolean foundRoot = false;
 		this.siteCount = sites;
 		
 		for(ARGNode node : nodes) {
 			if (node.getARG() != this)
 				throw new IllegalArgumentException("Cannot add ARGNode that does not belong to this arg");
 			if (node instanceof CoalNode) {
 				coalNodes.add((CoalNode)node);
 				if (node.getParent(0)==null) {
 					if (foundRoot) {
 						throw new IllegalArgumentException("Found multiple nodes with no parent (including " + node.getLabel() + " )");
 					}
 					else {
 						foundRoot = true;
 					}
 				}
 			}
 			
 			
 			if (node instanceof RecombNode)
 				recombNodes.add((RecombNode)node);
 			if (node instanceof TipNode) 
 				tips.add( (TipNode)node);
 			
 		}
 		
 		proposedIntervals.assignIntervals(this);
 		activeIntervals = proposedIntervals;
 		initializeRanges();
 		acceptValue();
 		if (jiggle)
 			jiggleNodeHeights();
 	}
 	
 	/**
 	 * Generates an initial tree by examining the given attributes. If alignment is non-null, a 
 	 * random tree is generated with a number of tips equal to the number of sequences in the alignment. 
 	 * If alignment is null, we look for a 'tips=X' attribute in the map, and build a tree with the
 	 * given number of tips.
 	 * If theta is given, that value is used to for the tree generation. If theta is omitted, theta=1.0  
 	 * 
 	 * If a file attribute is supplied, we ignore all other data and read the tree from the file. 
 	 * @param attrs
 	 * @param alignment
 	 */
 	private void generateInitialTree(Map<String, String> attrs, Alignment alignment) {
 		boolean hasTheta = false;
 		boolean hasTips = false;
 		
 		String thetaStr = attrs.get("theta");
 		double theta = 0.001; //Initial theta used to create tree
 		if (thetaStr != null) {
 			hasTheta = true;
 			try {
 				theta = Double.parseDouble(thetaStr);
 			}
 			catch (NumberFormatException ex) {
 				System.out.println("Could not parse a number for initial theta value from : " + thetaStr);
 			}
 			
 		}
 		
 		String tipsStr = attrs.get("tips");
 		double tipNumber = 0;
 		if (tipsStr != null) {
 			hasTips = true;
 			if (alignment != null) {
 				System.out.println("Warning : 'tips' attribute was supplied to tree along with sequence data, tip attribute ignored.");
 			}
 			
 			try {
 				if (alignment == null)
 					tipNumber = Double.parseDouble(tipsStr);
 				else {
 					tipNumber = alignment.getSequenceCount();
 				}
 			}
 			catch (NumberFormatException ex) {
 				System.out.println("Could not parse a number for number of tree tips from : " + tipsStr);
 			}
 		}
 		else {
 			if (alignment != null)
 				tipNumber = alignment.getSequenceCount();
 		}
 		
 		
 		String fileStr = attrs.get("filename");
 		CoalNode root = null;
 		if (fileStr != null ) {
 			
 			if (hasTheta) {
 				System.err.println("Both an initial theta and an initial tree have been specified, please supply one or the other.");
 				System.exit(0);
 			}
 			if (hasTips) {
 				System.err.println("Both an initial number of tips and an initial tree have been specified, please supply one or the other.");
 				System.exit(0);	
 			}
 			
 			try {
 				File file = new File(fileStr);
 				BufferedReader buf = new BufferedReader(new FileReader(file));
 				if (fileStr.endsWith("tre") || fileStr.endsWith("nwk")) {
 					System.out.println("Attempting to read newick-formatted tree from " + file.getName());
 					String treeString = buf.readLine();
 					root = TreeUtils.buildTreeFromNewick(treeString.trim());
 				}
 				if (fileStr.endsWith("xml")) {
 					System.out.println("Attempting to read xml-formatted ARG from " + file.getAbsolutePath());
 					ARGParser parser = new ARGParser();
 					List<ARGNode> nodes = parser.readARGNodes(file, this);
 					int sites = parser.getSiteCountFromXML(file);
 					if (alignment != null && sites != alignment.getSiteCount()) {
 						throw new IllegalArgumentException("ARG and sequence file disagree as to number of sites, arg reports " + sites + ", but sequences reports " + alignment.getSiteCount() );
 					}
 					
 					initializeFromNodeList(nodes, sites, false);
 					
 //					Integer[] bps = this.collectBreakPoints();
 //					System.out.println("Initial arg has " + bps.length + " breakpoints at ... ");
 //					for(int i=0; i<bps.length; i++)
 //						System.out.println(bps[i]);
 				}
 				
 
 			}
 			catch (Exception ex) {
 				System.err.println("Could not read tree from file : " + fileStr);
 				System.err.println("Reason : " + ex);
 				System.exit(0);
 			}
 		}
 		else {
 
 			if (!hasTips && alignment == null) {
 				System.err.println("No tree file, newick string, or number of tips has been specified, please supply at least one to build an initial tree.");
 				System.exit(0);
 			}
 			
 			String sitesStr = attrs.get("sites");
 			
 			if (sitesStr != null) {
 				
 				try {
 					siteCount = Integer.parseInt(sitesStr);
 					if (alignment != null && siteCount != alignment.getSiteCount()) {
 						System.err.println("Alignment and specified site count dot not agree on total number of sites (alignment thinks " + alignment.getSiteCount() + ", but sites was given as "  + siteCount);
 					}
 				}
 				catch (NumberFormatException ex) {
 					System.out.println("Could not parse a number for the number of sites in ARG from : " + sitesStr + "\n please supply sites=X");
 					System.exit(1);
 				}
 				
 			}
 			else {
 				if (alignment != null) {
 					siteCount = alignment.getSiteCount();
 				}
 				else {
 					System.out.println("You must specify a number of sites to generate an initial ARG");
 					System.exit(1);
 				}
 			}
 			
 			for(int i=0; i<tipNumber; i++) {
 				TipNode tip = new TipNode(this);
 				if (alignment == null)
 					tip.setLabel("tip #" + i);
 				else
 					tip.setLabel( alignment.getSequenceLabel(i));
 				tips.add(tip);
 			}
 			
 			Boolean useUPGMA = true;
 			String upgmaStr = attrs.get("upgma");
 			if (upgmaStr != null) {
 				useUPGMA = Boolean.parseBoolean(upgmaStr);
 			}
 			
 			if (useUPGMA) {
 				//Default is now to build a upgma tree to start searching. This really seems to help reduce
 				//convergence time
 
 				System.out.println("Generating initial tree from UPGMA tree");
 				UPGMABuilder upgma = new UPGMABuilder(alignment);
 				root = upgma.getRoot(this); 
 			}
 			else {
 				//An alternative is to generate a random tree
 				root = TreeUtils.generateRandomTree(tips, theta, this); //Sets the current state of the tree
 			}
 		}
 		
 		if (root != null)
 			addNodesFromTree(root);
 		
 		if (alignment != null) {
 			initializeNodes(alignment);
 			siteCount = alignment.getSiteCount();
 		}
 		
 		proposedIntervals.assignIntervals(this);
 		activeIntervals = proposedIntervals;
 		
 		//Initialize all range info
 		initializeRanges();
 		
 		//Accept the first state
 		acceptValue();
 		structureHasChanged = true; //Must be true on first call
 	}
 	
 	public SiteRangeList getSiteRangeList() {
 		if (rangePool.isEmpty()) {
 			//System.out.println("ARG is returning new site range list");
 			return new SiteRangeList();
 		}
 		else {
 			SiteRangeList list = rangePool.pop();
 			list.clear();
 			//System.out.println("ARG is popping list from pool, pool size: " + rangePool.size());
 			return list;
 		}
 	}
 	
 	public CoalRangeList getCoalRangeList() {
 		if (coalRangePool.isEmpty()) {
 			//System.out.println("ARG is returning new site range list");
 			return new CoalRangeList();
 		}
 		else {
 			CoalRangeList list = coalRangePool.pop();
 			list.clear();
 			//System.out.println("ARG is popping list from pool, pool size: " + rangePool.size());
 			return list;
 		}
 	}
 	
 	/**
 	 * Traverse all coal nodes in increasing-height order and initialize the SiteRanges for all of them. This
 	 * causes each coal node to know exactly what sites coalesce and descend from it. 
 	 */
 	public void initializeRanges() {
 		for(TipNode tip : tips)
 			tip.initializeRanges(siteCount);
 		
 		List<ARGNode> interiorNodes = getInternalNodes();
 		Collections.sort(interiorNodes, getNodeHeightComparator());
 		
 		for(ARGNode node : interiorNodes) {
 			node.computeProposedRanges(true);
 		}
 		
 		//Do some check to ensure validity? 
 	}
 	
 	public void verifySiteRanges() {
 		//Right now we turn this off if we're not computing on any data, but in principle this should
 		//always be left on
 		if (computeCore == null)
 			return;
 
 		for(CoalNode node : coalNodes) {
 			node.getActiveRanges().checkValidity();
 			node.getCoalescingSites().checkValidity();
 		}
 		
 		for(RecombNode node : recombNodes) {
 			node.getActiveRanges().checkValidity();
 		}
 		
 		Integer[] bps = collectBreakPoints();
 	
 		for(int i=0; i<bps.length+1; i++) {
 			int sumCoals = 0;
 			int bp = i==0 ? 0 : bps[i-1];
 			for(CoalNode cNode : coalNodes) {
 				if (cNode.siteCoalesces(bp))
 					sumCoals++;
 			}
 			if (sumCoals != tips.size()-1) {
 				System.out.println("Site range error, site " + bp + " coalesces " + sumCoals + " times");
 				System.out.println("Site coalesces at the following nodes: ");
 				for(CoalNode cNode : coalNodes) {
 					if (cNode.siteCoalesces(bp))
 						System.out.println(cNode);
 				}
 				System.out.println("Random seed: " + RandomSource.getSeed());
 				throw new IllegalStateException("Site " + bp + " coalesces an incorrect number of times, " + sumCoals);
 			}
 
 		}
 	}
 
 	/**
 	 * Set all visited flags for all nodes to false. Generally a good idea to invoke this 
 	 * before attempting some traversal procedure that uses these. 
 	 */
 	public void clearVisitedFlags() {
 		for(TipNode tip : tips) {
 			tip.setVisited(false);
 		}
 
 		for(CoalNode node : coalNodes) {
 			node.setVisited(false);
 		}
 
 
 		for(RecombNode node : recombNodes) {
 			node.setVisited(false);
 		}
 	}
 
 	/**
 	 * Add a new event to the list of arg change events to be handled
 	 * @param event
 	 */
 	public void queueChangeEvent(ARGChangeEvent event) {
 		changeEvents.add(event);
 	}
 
 	/**
 	 * Returns true if the arg has been modified since the last change event was processed
 	 * @return
 	 */
 	public boolean modified() {
 		return changeEvents.size() > 0;
 	}
 
 
 	public ComputeCore getComputeCore() {
 		return computeCore;
 	}
 
 
 	public boolean hasComputeCore() {
 		return computeCore != null;
 	}
 
 	/**
 	 * Clears events list, but does not process any events
 	 */
 	public void forgetEvents() {
 		changeEvents.clear();
 	}
 	
 	
 	/**
 	 * Handle all events queued. Right now we just set some flags specifying whether
 	 * various quantities are known.
 	 * 
 	 * Right now the strategy is that this gets called after all modifications, but
 	 * before any likelihood calculations for a given mcmc step. That way all likelihood
 	 * components will have a chance to    
 	 */
 	public void processEvents() {
 		boolean intervalsKnown = true;
 		if (modified()) {
 			for(ARGChangeEvent evt : changeEvents) {
 				if (evt.getType() == ChangeType.NodeAdded) {
 					intervalsKnown = false;
 					structureHasChanged = true;
 				}
 				if (evt.getType() == ChangeType.NodeRemoved) {
 					intervalsKnown = false;
 					structureHasChanged = true;
 				}
 				if (evt.getType() == ChangeType.HeightChanged) {
 					intervalsKnown = false;
 				}
 				if (evt.getType() == ChangeType.StructureChanged) {
 					structureHasChanged = true;
 				}
 			}
 			
 			try {
 				fireParameterChange();
 			} catch (ModificationImpossibleException e) {
 				//I don't think this should ever happen....
 				throw new IllegalArgumentException("Modification impossible for ARG structure?");
 			}
 		}
 		
 		if (!intervalsKnown) {
 			proposedIntervals.assignIntervals(this);
 			activeIntervals = proposedIntervals;
 		}
 		
 		changeEvents.clear();
 	}
 	
 	/**
 	 * Adds or subtracts a very small amount from all internal node heights, which helps to ensure
 	 * that no two nodes have exactly the same height, which can occur when we read ARGs in from 
 	 * simulation programs. 
 	 */
 	public void jiggleNodeHeights() {
 		List<ARGNode> nodes = this.getInternalNodes();
 		for(ARGNode node : nodes) {
 			double oldHeight = node.getHeight();
 			double dif = (RandomSource.getNextUniform()-0.5)*0.00000001;
 			node.proposeHeight( oldHeight + dif );
 			
 			for(int i=0; i<node.getNumParents(); i++) {
 				if (node.getParent(i) != null && node.getHeight() >= node.getParent(i).getHeight()) {
 					node.proposeHeight( node.getParent(i).getHeight()*0.99999 );
 				}
 			}
 			
 		}
 		this.acceptValue();
 	}
 	
 	/**
 	 * Creates the partial- and pattern arrays for all nodes
 	 * @param alignment
 	 * @param data
 	 */
 	private void initializeNodes(Alignment alignment) {
 		DNAUtils dna = new DNAUtils();
 		this.dataMatrix = alignment.getDataMatrix();
 		
 		for(int j=0; j<alignment.getSequenceCount(); j++) {
 			TipNode tip = (TipNode)getNodeForLabel( alignment.getSequenceLabel(j) );
 			if (tip == null ) {
 				throw new IllegalArgumentException("Could not find tip with label : " + alignment.getSequenceLabel(j));
 			}
 			int[] stateVec = dataMatrix.getStateVector(alignment.getSequenceLabel(j), dna);
 			if (stateVec == null) {
 				throw new IllegalArgumentException("Could not find state vector for tip #" + j + " with label: " + alignment.getSequenceLabel(j));
 			}
 			tip.addTipState(stateVec);
 
 		}
 
 	}
 	
 	public int getSiteCount() {
 		return siteCount;
 	}
 	
 	/**
 	 * Collect an unsorted list of all interior breakpoints. Each recombNode contributes exactly one. Note that this
 	 * may contain redundant entries if multiple recomb nodes have the same breakpoint. 
 	 * @return
 	 */
 	public Integer[] collectBreakPoints() {
 		Integer[] bps = new Integer[recombNodes.size()];
 		int count = 0;
 		for(RecombNode node : recombNodes) {
 			bps[count] = node.getInteriorBP();
 			count++;
 		}
 		return bps;
 	}
 	
 	/**
 	 * Return the height of the node with the greatest height. This is always from a coal node. 
 	 * @return
 	 */
 	public double getMaxHeight() {
 		double max = Double.NEGATIVE_INFINITY;
 		for(CoalNode node : coalNodes) {
 			if (node.getHeight() > max)
 				max = node.getHeight();
 		}
 		return max;
 	}
 	
 	/**
 	 * Returns a list of nodes that have a height < time, but that have at least one parent with height > time.
 	 * Current implementation ADDS RECOMBINATION NODES TWICE IF THEY HAVE TWO PARENTS THAT CROSS THE TIME. This
 	 * is to facilitate random selection of a 'branch' that crosses the time (so just picking a node at random
 	 * from the list is a uniform draw from 'branches' that cross).  
 	 * @param time
 	 * @return
 	 */
 	public List<ARGNode> getBranchesCrossingTime(double time) {
 		List<ARGNode> nodes = new ArrayList<ARGNode>();
 		for(CoalNode node : coalNodes) {
 			if (node.getHeight()<time && node.getParent()!=null && node.getParent().getHeight()>time)
 				nodes.add(node);
 		}
 		for(TipNode tip : tips) {
 			if (tip.getHeight()<time && tip.getParent(0).getHeight()>time)
 				nodes.add(tip);
 		}
 		for(RecombNode node : recombNodes) {
 			if (node.getHeight()<time) {
 				if (node.getParent(0).getHeight()>time) 
 					nodes.add(node);
 				if (node.getParent(1).getHeight()>time) 
 				nodes.add(node);
 			}
 		}
 		
 		return nodes;
 	}
 	
 
 	
 	/**
 	 * Returns true if we can modify the tree. This is true if all nodes are not in the activeProposed state
 	 */
 	public boolean isProposeable() {
 		
 		if (activeIntervals == proposedIntervals) {
 			System.out.println("Intervals are still active proposed, cannot modify ");
 			return false;
 		}
 		
 		if (proposedNodeAdds.size()>0) {
 			System.out.println("There are still nodes in proposedNodeAdds, cannot modify");
 			return false;
 		}
 
 		if (proposedNodeRemoves.size()>0) {
 			System.out.println("There are still nodes in proposedNodeRemoves, cannot modify");
 			return false;
 		}
 		
 		for(ARGNode node : coalNodes) {
 			if (node.isActiveProposed()) {
 				System.out.println(node + " is active proposed, tree is not valid.");
 				return false;
 			}
 		}
 		
 		for(ARGNode node : recombNodes) {
 			if (node.isActiveProposed()) {
 				System.out.println(node + " is active proposed, tree is not valid.");
 				return false;
 			}
 		}
 		
 		for(TipNode node : tips) {
 			if (node.isActiveProposed()) {
 				System.out.println(node + " is active proposed, tree is not valid.");
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	public DataMatrix getDataMatrix() {
 		return dataMatrix;
 	}
 	
 	/**
 	 * Returns a single list containing all nodes in this ARG
 	 * @return
 	 */
 	public List<ARGNode> getAllNodes() {
 		List<ARGNode> nodes = new ArrayList<ARGNode>();
 		nodes.addAll(coalNodes);
 		nodes.addAll(recombNodes);
 		nodes.addAll(tips);
 		return nodes;
 	}
 	
 	/**
 	 * Returns a single list containing all non-tip nodes in this arg
 	 * @return
 	 */
 	public List<ARGNode> getInternalNodes() {
 		List<ARGNode> nodes = new ArrayList<ARGNode>();
 		nodes.addAll(coalNodes);
 		nodes.addAll(recombNodes);
 		return nodes;
 	}
 	
 	/**
 	 * Returns a list of all internal nodes (coals and recombs) that have at least
 	 * one site that is ancestral to the data
 	 * @return
 	 */
 	public List<ARGNode> getInternalDLNodes() {
 		List<ARGNode> nodes = new ArrayList<ARGNode>();
 		for(CoalNode cNode : coalNodes) {
 			if (cNode.getActiveRanges().size()>0 || cNode.getCoalescingSites().size()>0)
 				nodes.add(cNode);
 		}
 		
 		for(RecombNode rNode : recombNodes) {
 			if (rNode.getActiveRanges().size()>0) {
 				nodes.add(rNode);
 			}
 		}
 		
 		return nodes;
 	}
 	
 	/**
 	 * Returns the total number of internal nodes (coal. nodes + recomb. nodes);
 	 * @return
 	 */
 	public int getInternalNodeCount() {
 		return coalNodes.size() + recombNodes.size();
 	}
 
 	
 	/**
 	 * Increments and returns the next number used to assign numbers to nodes
 	 */
 	public synchronized int nextNodeNumber() {
 		int val = nextNodeNumber;
 		nextNodeNumber++;
 		return val;
 	}
 	
 	/**
 	 * Calls rejectProposal on all nodes, and reverts the state of the ARG to that before any call to
 	 * proposeNodeRemove or proposeNodeAdd was called. This is done by 'putting back' all nodes in the
 	 * proposedNodeRemoves into the right list, and removing from the lists all nodes in 
 	 * proposedNodeAdds
 	 */
 	public void revertValue() {
 		calls++;
 		activeIntervals = currentIntervals;
 		activeSortedNodes = currentSortedNodes;
 		
 		//We're rejecting proposed node removals, so put the nodes back in to the right lists
 		for(ARGNode node : proposedNodeRemoves) {
 			if (node instanceof CoalNode) 
 				coalNodes.add((CoalNode)node);
 			if (node instanceof RecombNode) 
 				recombNodes.add( (RecombNode)node);	
 		}
 		proposedNodeRemoves.clear();
 		
 		for(ARGNode node : coalNodes) {
 			node.rejectProposal();
 		}
 		
 		for(ARGNode node : recombNodes) {
 			node.rejectProposal();
 		}
 		
 		for(ARGNode tip : tips) {
 			tip.rejectProposal();
 		}
 		
 		//We're rejecting an added state here, so the nodes get yanked out of their
 		//respective list. 
 		for(ARGNode node : proposedNodeAdds) {
 			if (node instanceof CoalNode) {
 				boolean in = coalNodes.remove(node);
 				CoalNode cNode = (CoalNode)node;
 
 				for(int i=0; i<cNode.proposedState.coalescingSites.rangeCount(); i++) {
 					computeCore.releaseNode(cNode.proposedState.coalescingSites.getRefID(i));
 				}
 				
 				if (rangePool.size() < rangePoolSize) {
 					rangePool.push(cNode.currentState.descendingSites);
 					rangePool.push(cNode.proposedState.descendingSites);				
 					cNode.currentState.descendingSites = null;
 					cNode.proposedState.descendingSites = null;
 				}
 				
 				if (coalRangePool.size() < rangePoolSize) {
 					coalRangePool.push(cNode.currentState.coalescingSites);
 					coalRangePool.push(cNode.proposedState.coalescingSites);
 					cNode.currentState.coalescingSites = null;
 					cNode.proposedState.coalescingSites = null;
 				}
 				
 				
 				if (!in) 
 					throw new IllegalStateException("Could not remove proposed add from coal nodes list!");
 			}
 			
 			if (node instanceof RecombNode) {
 				boolean in = recombNodes.remove(node);
 				RecombNode rNode = (RecombNode)node;
 				if (rangePool.size() < rangePoolSize) {
 					rangePool.push(rNode.currentState.siteRanges);
 					rangePool.push(rNode.proposedState.siteRanges);
 				}
 				rNode.currentState.siteRanges = null;
 				rNode.proposedState.siteRanges = null;
 				if (!in) 
 					throw new IllegalStateException("Could not remove proposed add from recomb nodes list!");
 			}
 		}
 		proposedNodeAdds.clear();
 		
 		rejectedValuesCount++;
 		
 
 		requiresDLRecalc = false;
 
 		//This flag gets reset with every call to accept or reject
 		structureHasChanged = false;
 	}
 
 	
 	/**
 	 * Called when the state proposed by the mcmc has been accepted. We notify all nodes to accept the proposed state.
 	 */
 	public void acceptValue() {
 		calls++;
 		proposedNodeAdds.clear();
 		
 		for(ARGNode node : proposedNodeRemoves) {
 			if (node instanceof CoalNode) {
 				CoalNode cNode = (CoalNode)node;
 				
 				for(int i=0; i<cNode.currentState.coalescingSites.rangeCount(); i++) {
 					computeCore.releaseNode(cNode.currentState.coalescingSites.getRefID(i));
 				}
 				
 				if (rangePool.size() < rangePoolSize) {
 					rangePool.push(cNode.currentState.descendingSites);
 					rangePool.push(cNode.proposedState.descendingSites);
 					
 					cNode.currentState.descendingSites = null;
 					cNode.proposedState.descendingSites = null;
 				}
 				
 				if (coalRangePool.size() < rangePoolSize) {
 					rangePool.push(cNode.currentState.coalescingSites);
 					rangePool.push(cNode.proposedState.coalescingSites);
 					cNode.currentState.coalescingSites = null;
 					cNode.proposedState.coalescingSites = null;
 				}
 			}
 			
 			if (node instanceof RecombNode) {
 				RecombNode rNode = (RecombNode)node;
 				if (rangePool.size() < rangePoolSize) {
 					rangePool.push(rNode.currentState.siteRanges);
 					rangePool.push(rNode.proposedState.siteRanges);
 				}
 				rNode.currentState.siteRanges = null;
 				rNode.proposedState.siteRanges = null;
 			}
 		}
 		proposedNodeRemoves.clear();
 		
 		//swap current and proposed intervals
 		if (activeIntervals == proposedIntervals) {
 			ARGIntervals tmp = proposedIntervals;
 			proposedIntervals = currentIntervals;
 			currentIntervals = tmp;
 			activeIntervals = currentIntervals;
 		}
 		
 		if (activeSortedNodes == proposedSortedNodes) {
 			ARGNode[] tmp = proposedSortedNodes;
 			proposedSortedNodes = currentSortedNodes;
 			currentSortedNodes = tmp;
 			activeSortedNodes = currentSortedNodes;
 		}
 		
 		for(ARGNode node : coalNodes) {
 			node.acceptProposal();
 		}
 
 		for(ARGNode node : recombNodes) {
 			node.acceptProposal();
 		}
 		
 		for(ARGNode tip : tips) {
 			tip.acceptProposal();
 		}
 			
 		//We do a bit of error checking here... not sure if this is the right place for this. Ideally, we'd do it before
 		//we acceptState for everything so we could reject a bad state... but it's probably best to just crash rather
 		//than proceed. Plus, the error checking code assumes we're in a non-modified state.  
 //		if (calls < 1000 || calls % 1000 == 0) {
 //			try {
 //				verifyComputeNodes();
 //				verifyReferences();
 //				verifyHeights();
 //				verifySiteRanges();
 //			} catch (InvalidParameterValueException e) {
 //				System.out.flush();
 //				e.printStackTrace();
 //				System.exit(1);
 //			}
 //
 //		}
 		
 		requiresDLRecalc = false;
 		
 		//This flag gets reset with every call to accept or reject
 		structureHasChanged = false;
 	}
 	
 	/**
 	 * Indicate that the arg (at least, some parts of it) require data likelihood calculation. This
 	 * causes the data likelihood object to recompute root ranges and partials, etc. for nodes
 	 * that have been altered. 
 	 */
 	public void setDLRecalc() {
 		requiresDLRecalc = true;
 	}
 	
 	/**
 	 * Returns whether or not any node with coalescencing sites at it has been changed. 
 	 * @return
 	 */
 	public boolean getRequiredDLRecalc() {
 		return requiresDLRecalc;
 	}
 	
 	
 	/**
 	 * Obtain an object that compares two nodes for height
 	 * @return
 	 */
 	public Comparator<ARGNode> getNodeHeightComparator() {
 		return heightComparator; 
 	}
 	
 	
 	/**
 	 * Associates a data likelihood ComputeCore with the nodes in the tree
 	 * @param computeCore
 	 */
 	public void setComputeCore(ComputeCore computeCore) {
 		this.computeCore = computeCore;
 		
 		for(TipNode tip : tips) {
 			tip.setComputeID(computeCore.nextTipID());
 			if (tip.getTipStates() == null)
 				throw new IllegalArgumentException("Tip state vector is null for tip " + tip);
 
 			computeCore.initializeTipState(tip.getComputeID(), tip.getTipStates());
 			tip.initializeRanges(tip.getTipStates().length);
 		}
 		
 		for(CoalNode node : coalNodes) {
 			node.setComputeCore(computeCore);
 		}
 		
 	}
 	
 	
 	/**
 	 * Reconstruct the list of nodes using 
 	 * @param root
 	 */
 	private void addNodesFromTree(CoalNode root) {
 		coalNodes.clear();
 		recombNodes.clear();
 		tips.clear();
 		Stack<ARGNode> stack = new Stack<ARGNode>();
 		stack.push(root);
 		while(!stack.isEmpty()) {
 			ARGNode node = stack.pop();
 			if (node instanceof CoalNode) {
 				if (! coalNodes.contains(node))
 					coalNodes.add((CoalNode)node);
 			}
 			if (node instanceof TipNode) {
 				if (!tips.contains(node)) 
 					tips.add( (TipNode)node);
 			}
 			if (node instanceof RecombNode) {
 				if (! recombNodes.contains(node)) {
 					recombNodes.add( (RecombNode)node);
 				}
 			}
 			
 			for(int i=0; i<node.getNumOffspring(); i++) {
 				stack.push(node.getOffspring(i));	
 			}
 			
 		}//while nodes remain in stack
 		proposedIntervals = new ARGIntervals();
 		proposedIntervals.assignIntervals(this);
 		activeIntervals = proposedIntervals;
 	}
 	
 	private void makeProposedSortedNodes() {
 		final int arraySize = coalNodes.size() + recombNodes.size();
 		if (proposedSortedNodes == null || proposedSortedNodes.length != arraySize) {
 			proposedSortedNodes = new ARGNode[arraySize];	
 		}
 		
 		//Dump all nodes into an array and sort it
 		int i;
 		for(i=0; i<coalNodes.size(); i++)
 			proposedSortedNodes[i] = coalNodes.get(i);
 		for(int j=0; j<recombNodes.size(); j++)
 			proposedSortedNodes[i+j] = recombNodes.get(j);
 		
 		Arrays.sort(proposedSortedNodes, getNodeHeightComparator());
 		
 	}
 	
 	/**
 	 * Return the node whose label matches the given label. Returns tips first, if there's
 	 * a match there, then searches internal nodes if not. 
 	 * @param label
 	 * @return
 	 */
 	public ARGNode getNodeForLabel(String label) {
 		for(TipNode tip : tips) {
 			if (tip.getLabel().equals(label))
 				return tip;
 		}
 
 		for(ARGNode node : coalNodes) {
 			if (node.getLabel().equals(label))
 				return node;
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Returns the node with the maximum height. This performs a little error checking which can probably
 	 * be turned off at some point. 
 	 * @return A CoalNode whose height is greater than all other coal nodes
 	 */
 	public CoalNode getMaxHeightNode() {
 		double maxHeight = Double.NEGATIVE_INFINITY;
 		CoalNode root = null;
 		for(CoalNode node : coalNodes) {
 			if (node.getHeight() > maxHeight) {
 				maxHeight = node.getHeight();
 				root = node;
 			}
 		}
 		if (root.getParent()!=null) {
 			throw new IllegalStateException("Root node has a non-null parent!");
 		}
 		return root;
 	}
 
 	/**
 	 * Called to propose an addition to the nodes in this ARG. These are added to a temporary list 
 	 * in addition to the coalNodes or recombNodes list so that if the state is rejected we know which
 	 * items to yank back out.  
 	 */
 	public void proposeNodeAdd(ARGNode node) {
 		if (node instanceof RecombNode)
 			recombNodes.add((RecombNode)node);
 		if (node instanceof CoalNode) {
 			CoalNode cNode = (CoalNode)node; 
 			coalNodes.add( cNode );
 			if (computeCore != null)
 				cNode.setComputeCore( computeCore );
 		}
 
 		
 		//System.out.println("Proposing to add node: " + node);
 		proposedNodeAdds.add(node);
 		//node.setARG(this);
 		this.queueChangeEvent(new ARGChangeEvent(ChangeType.NodeAdded, new ARGNode[]{node}));
 	}
 	
 	/**
 	 * Propose to remove the given node from this ARG. We remove the node from the appropriate node list
 	 * but add it to proposedNodeRemoves. If the state is rejected, the nodes will be added back to the
 	 * correct list, if accepted the proposedNodeRemoves list is simple cleared and the nodes are lost. 
 	 * @param node
 	 */
 	public void proposeNodeRemove(ARGNode node) {
 		if (node instanceof RecombNode) {
 			boolean contained = recombNodes.remove(node);
 			if (! contained) 
 				throw new IllegalStateException("Recomb nodes doesn't contain node to remove!");
 		}
 		if (node instanceof CoalNode) {
 			boolean contained = coalNodes.remove(node);
 			if (! contained) 
 				throw new IllegalStateException("Coal nodes doesn't contain node to remove!");
 		}
 
 		proposedNodeRemoves.add(node);
 		this.queueChangeEvent(new ARGChangeEvent(ChangeType.NodeRemoved, new ARGNode[]{node}));
 	}
 
 	public String getName() {
 		return "ARG";
 	}
 	
 	public List<CoalNode> getCoalescentNodes() {
 		return coalNodes;
 	}
 	
 	/**
 	 * Returns a list of only those coalescent nodes at which sites coalesce.
 	 * @return
 	 */
 	public List<CoalNode> getDLCoalNodes() {
 		List<CoalNode> dlNodes = new ArrayList<CoalNode>(coalNodes.size());
 		for(CoalNode node : coalNodes) {
 			if (node.getCoalescingSites() != null && node.getCoalescingSites().size()>0)
 				dlNodes.add(node);
 		}
 		
 		return dlNodes;
 	}
 
 	/**
 	 * Returns a list of all recomb nodes that contain a breakpoint that is ancestral to the sequences
 	 * @return
 	 */
 	public List<RecombNode> getDLRecombNodes() {
 		List<RecombNode> dlNodes = new ArrayList<RecombNode>(recombNodes.size());
 		for(RecombNode node : recombNodes) {
 			if (node.getActiveRanges() != null && node.getActiveRanges().contains( node.getInteriorBP() )) 
 				dlNodes.add(node);
 		}
 		
 		return dlNodes;
 	}
 	
 	
 	public List<RecombNode> getRecombNodes() {
 		return recombNodes;
 	}
 	
 	public List<TipNode> getTips() {
 		return tips;
 	}
 	
 	/**
 	 * Returns the number of nodes in the tree that have zero offspring
 	 */
 	public int getNumTips() {
 		return tips.size();
 	}
 
 
 	
 	/**
 	 * Return the total number of nodes in the tree
 	 */
 	public int getNodeTotal() {
 		return coalNodes.size() + tips.size() + recombNodes.size();
 	}
 
 
 	public ARGNode getNode(int which) {
 		if (which < coalNodes.size())
 			return coalNodes.get(which);
 		else if (which < (coalNodes.size() + recombNodes.size()))
 				return recombNodes.get(which - coalNodes.size());
 			else 
 				return tips.get(which-coalNodes.size()-recombNodes.size());
 	}
 	
 	/**
 	 * This returns true if there has been a structural change since the last call to accept or reject (revert)
 	 * DL calculation uses this to see if we need to re-sort the nodes
 	 * @return
 	 */
 	public boolean getStructureHasChanged() {
 		return structureHasChanged;
 	}
 	
 	/**
 	 * Return the height of the highest node which is ancestral to the data
 	 * @return
 	 */
 	public double getMaxDLHeight() {
 		List<CoalNode> cNodes = getDLCoalNodes();
 		Collections.sort(cNodes, getNodeHeightComparator());
 		if (cNodes.size()==0)
 			return 0;
 		
 		return cNodes.get( cNodes.size()-1).getHeight();
 	}
 	
 	public String getLogString() {
 		StringBuffer header = new StringBuffer();
 		for(int i=0; i<logKeys.length-1; i++) {
 			header.append(getLogItem(logKeys[i]) + "\t");
 		}
 		header.append(getLogItem(logKeys[logKeys.length-1]));
 		return header.toString();
 		
 		
 //		if (internalNodes.size()>1) {
 //			double rootHeight = calcProposedRootHeight();
 //			return	String.valueOf( Math.max(rootHeight-root.getLeftOffspring().getDistToParent(), rootHeight-root.getRightOffspring().getDistToParent() )) + "\t" + StringUtils.format(rootHeight, 6);
 //		}
 //		else
 //			return	"0.0 \t" + StringUtils.format(calcProposedRootHeight(), 6);
 		//return String.valueOf(tips.get(0).getDistToRoot()-internalNodes.get(1).getDistToParent() + "\t" + StringUtils.format(calcProposedRootHeight(), 6));
 		//return String.valueOf( StringUtils.format(this.getMaxDLHeight(), 6) + "\t" + recombNodes.size() + "\t" + getDLRecombNodes().size() );
 	}
 	
 	@Override
 	public String getLogHeader() {
 		StringBuffer header = new StringBuffer();
 		for(int i=0; i<logKeys.length-1; i++)
 			header.append(logKeys[i] + "\t");
 		header.append(logKeys[logKeys.length-1]);
 		return header.toString();
 	}
 	
 	/**
 	 * Conducts a couple of tree sanity checks to ensure all links are valid and parent/child relationships are reciprocal, and
 	 * that all nodes are equidistant from the root. 
 	 */
 	public void checkSanity() {
 		try {
 			verifyReferences();
 			verifyHeights();
 			if (computeCore != null) {
 				verifyComputeNodes();
 				verifySiteRanges();
 			}
 		}
 		catch (Exception ex) {
 			System.err.println("Tree did not pass sanity check: " + ex.getMessage());
 			System.exit(0);
 		}
 	}
 	
 	/**
 	 * A debugging / sanity check function that checks to make sure all parent / offspring relationships are consistent (no null parents,
 	 * all parents kid's think the correct parent is their parent, etc..)
 	 * @throws InvalidParameterValueException 
 	 */
 	public void verifyReferences() throws InvalidParameterValueException {
         ARGNode mrca = this.getMaxHeightNode();
 		try {
 			
 			for(ARGNode node : this.getAllNodes()) {
 				if (node != mrca)
 					node.checkReferences();
 				else {
 					for(int i=0; i<node.getNumOffspring(); i++) {
 						if (node.getOffspring(i)==null) {
 							throw new NodeReferenceException(node, "Child #" + i + " of node " + node.getLabel() + " is null");
 						}
 						if (! node.getOffspring(i).hasThisParent(node)) {
 							StringBuilder message = new StringBuilder();
 							message.append("Child #" + i + "  (" + node.getOffspring(i) + ")  of node " + node.getLabel() + " does not think node " + node + " is a parent \n");
 							throw new NodeReferenceException(node,  message.toString());
 						}
 					}
 				}
 			}
 		}
 		catch (NodeReferenceException nfe) {
 			throw new InvalidParameterValueException(this, "ARG did not pass node references check, cause: \n " + nfe.getMessage());	
 		}
 		
 	}
 	
 	
 	public void verifyComputeNodes() {
 		if (computeCore != null) {
 			for(CoalNode node : coalNodes) {
 				node.verifyComputeNodes();
 			}
 		}
 	}
 	/**
 	 * A debugging function that checks to make sure all tips are the same distance from the root, and emits an error
 	 * message to System.out if not. 
 	 * @throws InvalidParameterValueException 
 	 */
 	public void verifyHeights() throws InvalidParameterValueException {
 		boolean OK = true;
 		
 		for(CoalNode node : coalNodes) {
 			if (node.getLeftOffspring().getHeight() >= node.getHeight()) {
 				System.out.println("Coal Node with label " + node.getLeftOffspring() + " has height greater than its parent, " + node);
 				OK = false;
 			}
 			if (node.getRightOffspring().getHeight() >= node.getHeight()) {
 				System.out.println("Coal Node with label " + node.getRightOffspring() + " has height greater than its parent, " + node);
 				OK = false;
 			}
 		}
 		
 		for(RecombNode node : recombNodes) {
 			if (node.getOffspring(0).getHeight() >= node.getHeight()) {
 				System.out.println("Node " + node.getOffspring(0) + " has height greater than its parent, " + node);
 				OK = false;
 			}
 			if (node.getParent(0).getHeight() <= node.getHeight()) {
 				System.out.println("Recomb Node with label " + node + " has parent 0 with height less than itself");
 				OK = false;
 			}
 			if (node.getParent(1).getHeight() <= node.getHeight()) {
 				System.out.println("Recomb Node with label " + node + " has parent 1 with height less than itself");
 				OK = false;
 			}
 		}
 		
 		for(TipNode node : tips) {
 			if (node.getHeight()!=0.0) {
 				System.out.println("Tip node " + node.getLabel() + " has non-zero height of " + node.getHeight());
 				OK = false;
 			}
 		}
 		
 		if (! OK) {
 			throw new InvalidParameterValueException(this, "Tree did not pass tip height verification");
 		}
 	}
 	
 
 	/**
 	 * Obtain an ARG intervals object represnting the intervals in this arg
 	 * @return
 	 */
 	public ARGIntervals getIntervals() {
 		return activeIntervals;
 	}
 	
 	/**
 	 * If any of the parameters we depend on changed, we need to tell whoever listens to us (for 
 	 * instance, coal likelihood, data likelihood), that we have changed. 
 	 * @throws ModificationImpossibleException 
 	 */
 	@Override public void parameterChanged(Parameter<?> source) throws ModificationImpossibleException {
 		//Propagate parameter change to other listeners, such as the DL calculator
 		fireParameterChange();
 	}
 
 	
 	
 	/**
 	 * Obtain number of loggable items provided by this parameter.
 	 * Default implementation is a single item.
 	 */
 	public int getKeyCount() {
 		return logKeys.length;
 	}
 	
 	/**
 	 * Returns a list of keys associated with the loggable items of this param. 
 	 * Default implementation is a single item whose key is 'getName()'
 	 */
 	public String[] getLogKeys() {
 		return logKeys;
 	}
 	
 	/**
 	 * Return the current value of the log item associated with the given key.
 	 */
 	public Object getLogItem(String key) {
 		if (key.equals("root.height"))
 			return getMaxHeight();
 		if (key.equals("num.nodes"))
 			return computeCore != null ? computeCore.getNodeCount() : 0;
 		if (key.equals("visible.height")) 
 			return getMaxDLHeight();
		if (key.equals("visible.recombs")) {
 			return getDLRecombNodes().size(); 
 		}
 		if (key.equals("total.bps")) {
 			return getRecombNodes().size();
 		}
 		if (key.equals("num.patterns")) {
 			return computeCore != null ? computeCore.countPatterns() : 0;
 		}
 			
 		return null;
 	}
 	
 	class NodeHeightComparator implements Comparator<ARGNode> {
 
 		@Override
 		public int compare(ARGNode nodeA, ARGNode nodeB) {
 			if (nodeA.getHeight()>nodeB.getHeight())
 				return 1;
 			else
 				return -1;
 		}
 		
 		
 	}
 
 
 }
