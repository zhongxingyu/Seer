 /*
  *	File: @(#)PafUowCache.java 	Package: com.pace.base.mdb 	Project: Paf Base Libraries
  *	Created: Aug 27, 2005  			By: Alan Farkas
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *	This software is the confidential and proprietary information of Palladium Group, Inc.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with Palladium Group, Inc.
  *
  *
  *
 	Date			Author			Version			Changes
 	xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.base.mdb;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.MdbDef;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.PafDimSpec;
 import com.pace.base.app.UnitOfWork;
 import com.pace.base.app.VersionDef;
 import com.pace.base.app.VersionFormula;
 import com.pace.base.app.VersionType;
 import com.pace.base.comm.SimpleCoordList;
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.IPafDataCache;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.IntersectionUtil;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.PafDataSlice;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.mdb.PafDimTree.LevelGenType;
 import com.pace.base.state.EvalState;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.LogUtil;
 import com.pace.base.utility.StringOdometer;
 import com.pace.base.utility.StringUtils;
 import com.pace.base.view.PafMVS;
 
 /**
  * Container for a subset of multidimensional data cells corresponding
  * to a "unit of work"
  *
  * @version	x.xx
  * @author Alan Farkas
  *
  *-----------------------------------------------------------------------------------------------
  * Notes:
  * 	1) 	By default all cell intersections are comprised of a separate time and year coordinate.
  * 
  *  2) 	For time-based operations and functions, the time coordinate will be temporarily 
  *  	converted to a time horizon tree member format (combined time & year), and the year 
  *  	coordinate will be set to a dummy value (ex: "**YEAR.NA**"). 
  *------------------------------------------------------------------------------------------------  
  */
 /**
  * @author Alan
  *
  */
 /**
  * @author Alan
  *
  */
 /**
  * @author Alan
  *
  */
 public class PafDataCache implements IPafDataCache {
 
 	private Map<Intersection, Integer> dataBlockIndexMap = null; 	// Maps data block key to an item in the data block pool
 	private List<DataBlock> dataBlockPool = null;					// Collection of populated data blocks
 	private Map<Intersection, Integer> snapshotIndexMap = null; 	// Maps data block key to an item in the snapshot pool
 	private List<DataBlock> snapshotPool = null;					// Collection of blocks that represent an earlier snapshot of selected	
 	private LinkedList<Integer> deletedBlockIndexes = null;			// Surrogate keys of deleted blocks (Linked list for performance reasons)
 	private LinkedList<Integer> deletedSnapshotIndexes = null;		// Surrogate keys of deleted snapshot blocks (Linked list for performance reasons)
 	private Map<String, Set<Intersection>> dataBlocksByVersion = null;	// Data block keys organized by version
 	private Map<Intersection, Set<Intersection>> aliasKeyLookup = null; // Identifies any alias keys for a given data block 
 	private Map<Intersection, Intersection> primaryKeyLookup = null; 	// Identifies the primary data block key associated with each alias key
 	private Set<Intersection> emptyMdbBlocks = null;						// Keys of data blocks that were referenced in the Mdb but not loaded because they were empty
 	private Map<String, Set<Intersection>> emptyMdbBlocksByVersion = null;	// Empty mdb block keys by version
 	private int axisCount = 0;						// Total number of defined dimensions (size of allDimensions)
 	private int blockSize = 0;						// Number of cells in a data block
 	private int dataBlockCount = 0;					// Number of existing data blocks
 	private int snapshotBlockCount = 0;				// Number of existing snapshot blocks
 	private int cellPropsBitCount = 0;				// Number of bits comprising each cell property set
 	private int[] axisSizes = null;					// # of members in each axis
 	private int[] coreKeyAxes = null; 				// The core dimension axes that comprise that data block key
 	private int[][] keyIndexesByIsSize = null;		// Optimization tool - an array that will be used to isolate
 													//		the intersection coordinates that comprise the data 
 													//		block key for each potential intersection size
 	private String[] coreKeyDims = null;			// The core dimensions that comprise that data block key
 													//		(all dimensions except Measures and Time)
 	private String[] attributeDims = null;			// Valid attribute dimensions
 	private String[] baseDimensions = null; 		// Valid base dimensions
 	private List<String> allDimensions = null; 		// Valid intersection dimensions
 	private List<String> treeDims = null; 			// All dimension tree entries (include Time Horizon Tree)
 	private Set<String> coreDimensions = null;		// The required cell intersection dimensions (base dimensions)
 	private Set<String> nonCoreDims = null;			// The optional cell intersection dimensions (attribute dimensions)
 	private String[][] memberCatalog = null;		// Defined members by dimension axis
 	private Map<String, Integer> axisIndexMap = null;					// Resolves dimension name to an axis
 	private Map<String, Integer>[] memberIndexMap = null;				// Resolves member name to an index (currently only
 																		// 		used to lookup Measures and Time dimension members)
 	private String[] planVersions = null; 								// Plan version 
 	private Map<String, Set<String>> lockedPeriodMap = null; 			// Locked planning periods by year
 	private Map<String, Set<String>> invalidPeriodMap = null; 			// Invalid planning periods by year
 	private Set<String> lockedTimeHorizonPeriods = null;				// Locked time horizon coordinates
 	private Set<String> invalidTimeHorizonPeriods = null;				// Invalid time horizon coordinates within possible set of uow time/year coordinates
 	private Set<String> lockedYears = null;								// Locked / non-plannable years
 	//	private Set<TimeSlice> lockedTimeSlices = null; 					// Locked time slices (time slice holds both period & year and period/year time coords)
 //	private Set<TimeSlice> invalidTimeSlices = null; 					// Potential time slices that don't exist in current uow
 	private PafDataCacheCells changedCells = new PafDataCacheCells();	// Optionally tracks changed cells
 	private PafApplicationDef appDef = null;
 	private PafMVS pafMVS = null;
 	private MemberTreeSet dimTrees = null;
 	private Map<String, PafBaseTree> mdbBaseTrees = null;
 	private List<String> mdbLeafYears = null;							// List of leaf years in mdb year tree
 	private List<String> leafYears = null;								// List of years in mdb year tree
 	private Integer timeHorizGenOffset = null;							// Gen difference between member in time horizon tree and corresponding member in time tree;
 	private EvalState evalState = null;
 	private PafClientState clientState = null;
 	private boolean isDirty = true;					// Indicates that the data cache has been updated
 	private final static int NON_KEY_DIM_COUNT = 2;	// Measure and Time are dense dimensions
 	private final static int MB = 1048576; 			// 1024*1024
 	private final static int GB = 1073741824; 		// 1024*1024 * 1024
 	private final static int BYTES = 8; 			// Number of bytes allocated to a data cell (double)
 
 
 	// Lazy-loaded collection of component base member names, indexed by base member intersection
 	private Map<Intersection, List<String>> componentBaseMemberMap = new HashMap<Intersection, List<String>>();
 
 	// Loggers
 	private static Logger logger = Logger.getLogger(PafDataCache.class);
 	private static Logger performanceLogger = Logger.getLogger(PafBaseConstants.PERFORMANCE_LOGGER_DC);
 	
 	
 
 	/**
 	 * @param clientState Client State
 	 */
 	public PafDataCache(PafClientState clientState) {
 		initDataCache(clientState);		
 	}
 
 			
 	protected PafDataCache() {
 		// For testing purposes
 	}
 
 
 	/**
 	 *	Initialize data cache
 	 *
 	 * @param clientState Client state
 	 *
 	 */
 	private void initDataCache(PafClientState clientState) {
 
 		logger.debug("Creating instance of PafDataCache");
 		long startTime = System.currentTimeMillis();
 		
 		// Get needed client state properties
 		appDef = clientState.getApp();
 		String versionDim = getVersionDim();
 		planVersions = new String[]{clientState.getPlanningVersion().getName()};
 		UnitOfWork expandedUowSpec = clientState.getUnitOfWork();
 		dimTrees = clientState.getUowTrees();
 		mdbBaseTrees = clientState.getMdbBaseTrees();
 		lockedPeriodMap = clientState.getLockedPeriodMap();
 		invalidPeriodMap = clientState.getInvalidPeriodMap();			// TTN-1858 - week 53 support
 		lockedYears = clientState.getLockedYears();						// TTN-1860 - non-plannable support
 		lockedTimeHorizonPeriods = clientState.getLockedTimeHorizonPeriods();
 		invalidTimeHorizonPeriods = clientState.getInvalidTimeHorizonPeriods();
 		this.clientState = clientState;									// TTN-1561 - session locks
 
 
 		// Set dimension properties
 		String[] uowDims = expandedUowSpec.getDimensions();
 		attributeDims = dimTrees.getAttributeDimensions().toArray(new String[0]);
 		baseDimensions = uowDims;
 		coreDimensions = new HashSet<String>(Arrays.asList(uowDims));
 		nonCoreDims = new HashSet<String>(Arrays.asList(attributeDims));
 		allDimensions = new ArrayList<String>(Arrays.asList(baseDimensions));
 		allDimensions.addAll(Arrays.asList(attributeDims));
 		treeDims = dimTrees.getAllDimensions(); 	// Includes time horizon tree (TTN-1595)
 		axisCount = treeDims.size();				// Pick up time horizon dim too (TTN-1595)
 		
 		// Add all dimensions members to member catalog. For proper member aggregation,
 		// the members will be added in POST ORDER so that all children are calculated
 		// before their parents.
 		axisSizes = new int[axisCount];
 		memberCatalog = new String[(axisCount)][];
 		for (int axis = 0; axis < axisCount; axis++) {
 			String dim = treeDims.get(axis);		// Pull dim name from dim tree collection (TTN-1595)
 			PafDimTree dimTree = dimTrees.getTree(dim);
 			String[] members;
 			if (!dim.equals(versionDim)) {
 				// Pull all dimensions except Version from dim trees
 				members = dimTree.getMemberNames(TreeTraversalOrder.POST_ORDER).toArray(new String[0]);
 			} else {
 				// Version dim is flat, so just pull members from uow definition
 				members = expandedUowSpec.getDimMembers(versionDim);
 			}
 			memberCatalog[axis] = members;
 			axisSizes[axis] = members.length;
 		}
 
 		// Create axisIndexMap
 		axisIndexMap = createAxisIndexMap();
 
 		// Create memberIndexMap
 		memberIndexMap = createMemberIndexMap();
 
 		// Create arrays to facilitate cell addressing
 		createKeyIndexArrays();
 		
 		// Set cell property statistics
 		cellPropsBitCount = CellPropertyType.getTotBitCount();
 		
 		// Initialize data block index and data block pool. To minimize the
 		// the time it takes to initially populate the data cache, the index
 		// and pool are pre-allocated to hold all initially defined blocks.
 		blockSize = getMeasureSize() * getTimeSize();
 		int maxCoreBlockCount = getMaxCoreDataBlockCount();
 		dataBlockIndexMap = new HashMap<Intersection, Integer>(maxCoreBlockCount * 2);  // Allocate additional space for attribute blocks
 		dataBlockPool = new ArrayList<DataBlock>(maxCoreBlockCount *2);  				// Allocate additional space for attribute blocks 
 		
 		// Initialize various data block housekeeping objects
 		dataBlocksByVersion = new HashMap<String, Set<Intersection>>(getVersionSize());
 		aliasKeyLookup = new HashMap<Intersection, Set<Intersection>>(200);
 		primaryKeyLookup = new HashMap<Intersection, Intersection>(200);
 		deletedBlockIndexes = new LinkedList<Integer>();
 		emptyMdbBlocks = new HashSet<Intersection>(200);
 		emptyMdbBlocksByVersion = new HashMap<String, Set<Intersection>>(getVersionSize());
 
 		// Initialize snapshot data blocks
 		initSnapshotBlocks();
 		
 		// Log data cache statistics
 		String logMsg = LogUtil.timedStep("UOW creation and initialization", startTime);
 		performanceLogger.info(logMsg);
 
 	}
 
 
 	/**
 	 * Initialize snapshot data blocks
 	 */
 	private void initSnapshotBlocks() {
 		int maxPlanCoreBlockCount = getMaxPlanCoreDataBlockCount();
 		snapshotIndexMap = new HashMap<Intersection, Integer>(maxPlanCoreBlockCount);
 		snapshotPool = new ArrayList<DataBlock>(maxPlanCoreBlockCount);
 		deletedSnapshotIndexes = new LinkedList<Integer>();
 		snapshotBlockCount = 0;
 	}
 
 
 	/**
 	 *	Return the number of columns in the data cache
 	 *
 	 * @return Returns the number of columns in the data cache.
 	 */
 	public int getColumnCount() {
 		return getAxisSizes()[0];
 	}
 
 
 	/**
 	 * @return the dimTrees
 	 */
 	public MemberTreeSet getDimTrees() {
 		return dimTrees;
 	}
 
 
 	/**
 	 * @return the mdbBaseTrees
 	 */
 	public Map<String, PafBaseTree> getMdbBaseTrees() {
 		return mdbBaseTrees;
 	}
 
 
 	/**
 	 * Return list of leaf years in the data cache 
 	 * 
 	 * @return List of data cache leaf years
 	 */
 	public List<String> getLeafYears() {
 		
 		if (leafYears == null) {
 			// This collection is lazy loaded
 			final PafDimTree yearTree = dimTrees.getTree(this.getYearDim());
 			leafYears = yearTree.getLowestMemberNames(yearTree.getRootNode().getKey());
 		}
 		return leafYears;
 	}
 
 	/**
 	 * Return list of leaf years in the current multi-dimensional database 
 	 * 
 	 * @return List of mdb leaf years
 	 */
 	public List<String> getMdbLeafYears() {
 		
 		if (mdbLeafYears == null) {
 			// This collection is lazy loaded
 			final PafDimTree mdbYearTree = mdbBaseTrees.get(this.getYearDim());
 			mdbLeafYears = mdbYearTree.getLowestMemberNames(mdbYearTree.getRootNode().getKey());
 		}
 		return mdbLeafYears;
 	}
 
 
 	/**
 	 * @return the clientState
 	 */
 	public PafClientState getClientState() {
 		return clientState;
 	}
 
 
 	/**
 	 * @param clientState the clientState to set
 	 */
 	public void setClientState(PafClientState clientState) {
 		this.clientState = clientState;
 	}
 
 
 	/**
 	 * @return the evalState
 	 */
 	public EvalState getEvalState() {
 		return evalState;
 	}
 
 
 	/**
 	 * @param evalState the evalState to set
 	 */
 	public void setEvalState(EvalState evalState) {
 		this.evalState = evalState;
 	}
 
 
 	/**
 	 *	Return the PafMVS
 	 *
 	 * @return Returns the PafMVS
 	 */
 	public PafMVS getPafMVS() {
 		return pafMVS;
 	}
 
 	/**
 	 * @param pafMVS the pafMVS to set
 	 */
 	public void setPafMVS(PafMVS pafMVS) {
 		this.pafMVS = pafMVS;
 	}
 
 
 
 	/**
 	 *	Valid that array is not empty or null
 	 *
 	 * @param array Array to validate
 	 * @param arrayName Array name
 	 * @throws IllegalArgumentException 
 	 */
 	private void validateArrayNotEmpty(Object[] array, String arrayName) throws IllegalArgumentException {
 
 		// Validate that the array contains data and is the proper size
 		if (array == null || array.length == 0) {
 			// Empty or null array
 			String errMsg = "Parms error - empty or null [" + arrayName + "] array";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae; 
 		}
 	}
 
 
 	/**
 	 *	Create PafUowCache axis index map
 	 *
 	 * @return HashMap used for resolving dimension names to a specific axis index value.
 	 */
 	private Map<String, Integer> createAxisIndexMap() {
 
 		Map <String, Integer> axisIndexMap = null;
 
 		// Fill the axisIndexMap, which is used to resolve dimension
 		// names to a specific axis index.
 		logger.debug("Creating axisIndexMap ...");
 		axisIndexMap = new HashMap<String, Integer>();
 		for (int i = 0; i < axisCount; i++) {
 			axisIndexMap.put(treeDims.get(i), i); 	// Include time horizon dim (TTN-1595)
 		}
 		return axisIndexMap;
 	}
 
 
 	/**
 	 *	Create the set of internal arrays that manage the addressing of data blocks and cells intersections
 	 *
 	 * @return An  the set of internal arrays that manage the addressing of data blocks and cells intersections
 	 */
 	private void createKeyIndexArrays() {
 
 		int measureAxis = getMeasureAxis(), timeAxis = getTimeAxis(), yearAxis = getYearAxis();
 
 		// Fill the coreKeyAxes and coreKeyDims, which contains the list of all
 		// UOW dimensions & axes, respectively, that comprise the data block index. 
 		// This is limited to all dimensions except Measure and Time, which 
 		// are not included as they each are represented within the data block itself.
 		logger.debug("Creating coreKeyAxes &  coreKeyDims arrays...");
 		int minIsSize = coreDimensions.size();
 		int maxIsSize = allDimensions.size();
 		keyIndexesByIsSize = new int[maxIsSize + 1][];		// 1-based array
 
 		coreKeyAxes = new int[minIsSize - NON_KEY_DIM_COUNT];
 		coreKeyDims = new String[coreKeyAxes.length];
 		int i = 0;
 		for (int axisIndex = 0; axisIndex < minIsSize; axisIndex++) {
 			if (axisIndex != measureAxis && axisIndex != timeAxis) {
 				coreKeyAxes[i] = axisIndex;
 				coreKeyDims[i] = getDimension(axisIndex); 
 				i++;
 			}
 		}
 
 		// Create an array that will be used to isolate the intersection coordinates
 		// that comprise the data block key for each intersection size. Intersections
 		// can vary in size due the optional inclusion of any number of attribute
 		// member coordinates.
 		//
 		// This array will be used to quickly and efficiently generate the 
 		// corresponding data block key for any given intersection, regardless of 
 		// size.
 		//
 		// The list of indexed axes, for each intersection size, is limited to the
 		// axis for each intersection dimension except Measure and Time. 
 		// 
 		// In following example Measure and Time are at axes 1 and 3, respectively.
 		//
 		// 	Example:  	
 		//	Intersection Size		Intersection Axes				Indexed Axes
 		//			  		7		[0][1][2][3][4][5][6]			[0][2][4][5][6]
 		//			  		8		[0][1][2][3][4][5][6][7]		[0][2][4][5][6][7]
 		//			  		9		[0][1][2][3][4][5][6][7][8]		[0][2][4][5][6][7][8]
 		//
 		logger.debug("Creating keyIndexesBySize array");
 		keyIndexesByIsSize[minIsSize] = coreKeyAxes;
 		for (int element = minIsSize + 1; element <= maxIsSize; element++) {
 			int[] prevIndexedAxes = keyIndexesByIsSize[element - 1];
 			int[] indexedAxes = new int[element - NON_KEY_DIM_COUNT];
 			System.arraycopy(prevIndexedAxes, 0, indexedAxes, 0, prevIndexedAxes.length);
 			indexedAxes[indexedAxes.length - 1] = element - 1;
 			keyIndexesByIsSize[element] = indexedAxes;
 		}
 	}
 
 	/**
 	 *	Create member index map
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	private Map<String, Integer>[] createMemberIndexMap() {
 
 		Map<String, Integer>[] memberIndexMap = null;
 
 		// Fill the memberIndexMap array, which is used to resolve member
 		// names to a specific index.
 		logger.debug("Filling memberIndexMap array...");
 		memberIndexMap = new Map[axisCount];
 
 		for (int i = 0; i < axisCount; i++) {
 			HashMap <String, Integer> memberIndex = new HashMap <String, Integer> (memberCatalog[i].length);
 			for (int j = 0; j < axisSizes[i]; j++) {
 				memberIndex.put(memberCatalog[i][j], j);
 			}
 			memberIndexMap[i] = memberIndex;
 			logger.debug("Member index for Axis [" + i + "] populated");
 		}
 		return memberIndexMap;
 	}
 
 
 
 	/**
 	 *	Returns the existing number of data blocks in the data cache
 	 *
 	 * @return The existing number of data blocks in the data cache
 	 */
 	public int getDataBlockCount() {
 
 		int dataBlockCount = dataBlockPool.size();
 		return dataBlockCount;
 	}
 
 	/**
 	 *	Returns the existing number of data blocks corresponding to the specified version
 	 *
 	 * @param version Version dimension member
 	 * @return The existing number of data blocks corresponding to the specified version
 	 */
 	public int getDataBlockCount(String version) {
 
 		int dataBlockCount = 0;
 		if (dataBlocksByVersion.containsKey(version)) {
 			dataBlockCount = dataBlocksByVersion.get(version).size();
 		}
 		return dataBlockCount;
 	}
 
 	/**
 	 *	Returns the maximum number of addressable data blocks across the core
 	 *	dimensions
 	 *
 	 * @return The maximum number of addressable data blocks across the core dimensions
 	 */
 	public int getMaxCoreDataBlockCount() {
 
 		int memberCombinations = 1;
 
 		// Multiply the number of members in each core key dimension by
 		// each other.
 		for (int axisIndex = 0; axisIndex < coreKeyAxes.length; axisIndex++) {
 			memberCombinations = memberCombinations * getAxisSize(coreKeyAxes[axisIndex]);
 		}
 
 		// 
 		return memberCombinations;
 	}
 
 	/**
 	 *	Returns the maximum number of addressable plannable data blocks across the core
 	 *	dimensions
 	 *
 	 * @return The maximum number of addressable plannable data blocks across the core dimensions
 	 */
 	public int getMaxPlanCoreDataBlockCount() {
 
 		int memberCombinations = 1;
 
 		// Multiply the number of members in each core key dimension by
 		// each other. For the Version and Year dimensions, the number
 		// of plannable members are used.
 		for (int axisIndex = 0; axisIndex < coreKeyAxes.length; axisIndex++) {
 			
 			int axisSize = 0;
 			if (axisIndex == this.getVersionAxis()) {
 				axisSize = 1;
 			} else if (axisIndex == this.getYearAxis()) {
 				axisSize = this.getPlanYearSize();
 			} else {
 				axisSize = getAxisSize(coreKeyAxes[axisIndex]);
 			}
 			memberCombinations = memberCombinations * axisSize;
 		}
 
 		// 
 		return memberCombinations;
 	}
 
 	/**
 	 *	Returns the maximum number of addressable data blocks in the data cache
 	 *
 	 * @return The maximum number of addressable data blocks in the data cache
 	 */
 	public int getMaxDataBlockCount() {
 
 		int memberCombinations = getMaxCoreDataBlockCount();
 
 		// Multiply the number of members in each non-core dimension by
 		// each other and the number of max core data black
 		for (String nonCoreDim : nonCoreDims) {
 			int axisIndex = this.getAxisIndex(nonCoreDim);
 			memberCombinations = memberCombinations * getAxisSize(axisIndex);			
 		}
 
 		return memberCombinations;
 	}
 
 
 	/**
 	 * @return the blockSize
 	 */
 	public int getBlockSize() {
 		return blockSize;
 	}
 
 
 	/**
 	 * Generate a string containing essential data cache statistics
 	 * 
 	 * @return a string containing essential data cache statistics
 	 */
 	public String getStatsString() {
 		
 		long blocks = 0;
 		
 		StringBuffer sb = new StringBuffer("\n\nData Cache Statistics: \n\n");
 	
 		// Block size stats
 		sb.append(String.format("Data Cache Blocksize: %,d cells (%,.1f KB)", blockSize, (float) (blockSize * BYTES / 1024)));
 		
 		// Current usage stats
 		sb.append(this.getCurrentUsageStatsString());
 		sb.append('\n');
 		
 		// Max usage stats
 		if (this.getPlanYearSize() > 0) {
 			blocks = this.getMaxCoreDataBlockCount() / this.getVersionSize();
 		}
 		sb.append(this.getUsageStatsString(blocks, "Max Data Cache Mem Usage [Plannable UOW]:"));
 		blocks = this.getMaxCoreDataBlockCount();
 		sb.append(this.getUsageStatsString(blocks, "Max Data Cache Mem Usage [Entire UOW]:"));
 //		blocks = this.getMaxDataBlockCount();
 //		sb.append(this.getUsageStatsString(blocks, "Max Data Cache Mem Usage [Entire UOW + Attributes]:"));
 
 		//sb.append("\n");
 		
 		return sb.toString();
 	}
 
 	/**
 	 * Generate a string containing current data cache usage statistics
 	 * 
 	 * @return a string containing current data cache usage statistics
 	 */
 	public String getCurrentUsageStatsString() {
 		
 		long blocks = 0L, maxBlocks = 0L;
 		StringBuffer sb = new StringBuffer();
 		String usageText = null;
 
 		// Current memory usage (plannable uow)
 		blocks = this.getDataBlockCount(this.getPlanVersion());
 		if (this.getPlanYearSize() > 0) {
 			maxBlocks = this.getMaxCoreDataBlockCount() / this.getVersionSize();
 		} 
 		usageText = String.format("\n\nCurrent Data Cache Mem Usage [Plannable UOW] (%.1f%% of Max Non-Attribute Cells):", (float) (blocks > 0 ? (100.0 * blocks / maxBlocks) : 0));
 		sb.append(this.getUsageStatsString(blocks, usageText));
 
 		// Current memory usage (entire uow)
 		blocks = this.getDataBlockCount();
 		maxBlocks = this.getMaxCoreDataBlockCount();
 		usageText = String.format("Current Data Cache Mem Usage [Entire UOW] (%.1f%% of Max Non-Attribute Cells):", (float) (100.0 * blocks / maxBlocks));
 		sb.append(this.getUsageStatsString(blocks, usageText));
 
 		return sb.toString();
 	}
 	
 	/**
 	 * Generate a memory usage statistics string for the specified block count
 	 * 
 	 * @param blockCount Block count
 	 * @return a memory usage statistics string for the specified block count
 	 */
 	private String getUsageStatsString(long blockCount, String usageText) {
 		
 		final long cellCount = blockCount * blockSize;
 		float memUsage = (float) cellCount * BYTES / MB;
 		String scale = "MB";
 		
 		// Set memory usage to appropriate scale (MB/GB/TB)
 		if (memUsage > MB) {
 			scale = "TB";
 			memUsage /= (float) MB;
 		} else if (memUsage > 1024) {
 			scale = "GB";
 			memUsage /= (float) 1024;
 		}
 
 		final String usageStats = String.format("%s %.3g block(s) / %.3g cells / %,.1f %s\n", 
 				usageText, (float) blockCount, (float) cellCount, memUsage, scale);
 		
 		return usageStats;
 	}
 
 
 
 	/**
 	 *	Return a list of versions that are components for any of the
 	 *  specified derived or offset versions
 	 *  
 	 * @param complexVersions List of derived or offset versions to inspect
 	 * @return List of component versions
 	 */
 	public List<String> getComponentVersions(List<String> complexVersions) {
 
 		Set<String> componentVersions = new HashSet<String>();
 
 
 		// Cycle through each derived version and look for component members
 		for (String complexVersion : complexVersions) {
 			
 			VersionDef versionDef = getVersionDef(complexVersion);
 			
 				// Skip version if not derived or offset version
 				if (!this.getDerivedVersions().contains(complexVersion) && !this.getOffsetVersions().contains(complexVersions))
 						continue;
 				
 				// Get base version
 				VersionFormula versionFormula = versionDef.getVersionFormula();
 				String baseVersion = versionFormula.getBaseVersion();
 				componentVersions.add(baseVersion);
 				
 				// Determine the comparison version
 				String compareVersion = null;
 				if (versionDef.getType() == VersionType.Variance || versionDef.getType() == VersionType.Offset) {
 					// Variance or Offset version - use compare version property
 					compareVersion = versionFormula.getCompareVersion(); 
 				} else if (versionDef.getType() == VersionType.ContribPct) {
 					// Contribution percent - search for possible compare version
 					for (PafDimSpec compareMemberSpec:versionFormula.getCompareIsSpec()) {
 						String dim = compareMemberSpec.getDimension();
 						if (dim.equalsIgnoreCase(getVersionDim())) {
 							compareVersion = compareMemberSpec.getExpressionList()[0];
 						}
 					}
 				}
 				if (compareVersion != null) {
 					componentVersions.add(compareVersion);
 				}
 			}
 
 	
 		return new ArrayList<String>(componentVersions);
 	}
 
 	/**
 	 *	Return a list of versions for those data cache versions whose formulas 
 	 *  are dependent on any of the base versions passed to this method 
 	 *  
 	 * @param versions List of versions whose dependencies will be checked
 	 * @return List of dependent versions
 	 */
 	public List<String> getDependentVersions(List<String> versions) {
 
 		List<String> dependentVersions = new ArrayList<String>();
 
 		// Get list of versionDef objects for all derived versions
 		List<VersionDef>derivedVersionDefs = getDerivedVersionDefs();
 		
 		// Cycle through each version and check for dependent formulas
 		for (String version : versions) {
 			for (VersionDef versionDef : derivedVersionDefs) {
 
 				// Get formula components
 				String derivedVersion = versionDef.getName();
 				VersionFormula versionFormula = versionDef.getVersionFormula();
 				String baseVersion = versionFormula.getBaseVersion();
 				String compareVersion = null;
 				
 				// Determine the comparison version
 				if (versionDef.getType() == VersionType.Variance) {
 					// Variance version - use compare version property
 					compareVersion = versionFormula.getCompareVersion(); 
 				} else if (versionDef.getType() == VersionType.ContribPct) {
 					// Contribution percent - search for possible compare version
 					for (PafDimSpec compareMemberSpec:versionFormula.getCompareIsSpec()) {
 						String dim = compareMemberSpec.getDimension();
 						if (dim.equalsIgnoreCase(getVersionDim())) {
 							compareVersion = compareMemberSpec.getExpressionList()[0];
 						}
 					}
 				}
 				
 				// Add dependent version if it is a dependent of one of the selected versions 
 				if (version.equalsIgnoreCase(baseVersion) || 
 						(compareVersion != null && version.equalsIgnoreCase(compareVersion))) {
 					dependentVersions.add(derivedVersion);
 				}
 			}
 
 		}
 		return dependentVersions;
 	}
 
 
 	/**
 	 *	Determine if the Version and Year combination are locked
 	 *
 	 * @param version Selected member of version dimension
 	 * @param year Selected member of year dimension
 	 *
 	 * @return true if Version and Year combination contains locked periods
 	 */
 	public boolean hasLockedPeriods(String version, String year) {	
 
 		boolean hasLockedPeriods = false;
 		VersionType versionType = getVersionType(version);
 
 		// A Version and Year combination contains locked periods if one of the following conditions are met:
 		// 
 		//  A - The year is "locked" OR
 		//
 		// 	B - The year matches the "current year" AND
 		//	    The Version is Forward Plannable 
 		//			OR The Version is a Variance Version whose Base Version is Forward Plannable
 		//
 		if (getLockedYears().contains(year)) {			// TTN-1860 non-plannable support
 			hasLockedPeriods = true;
 		} else if (year.equalsIgnoreCase(getCurrentYear())) {
 			if (versionType == VersionType.ForwardPlannable) {
 				hasLockedPeriods = true;
 			} else if (PafBaseConstants.DERIVED_VERSION_TYPE_LIST.contains(versionType)) {
 				String baseVersion = getVersionDef(version).getVersionFormula().getBaseVersion();
 				hasLockedPeriods = hasLockedPeriods(baseVersion, year);
 			} 
 		}
 		return hasLockedPeriods;
 	}
 
 
 	/**
 	 *	Determines if the intersection translates to a valid time horizon
 	 *  coordinate
 	 *
 	 * @param cellIs Cell intersection
 	 * @return true if the intersection translates to a valid time horizon coordinate
 	 */
 	public boolean hasValidTimeHorizonCoord(Intersection cellIs) {
 		
 		return hasValidTimeHorizonCoord(cellIs.getCoordinates());
 	}
 
 	/**
 	 *	Determines if the intersection coordinates translates to a valid 
 	 *  time horizon coordinate
 	 *
 	 * @param coords Cell intersection coordinates
 	 * @return true if the intersection coordinates translates to a valid time horizon coordinate
 	 */
 	public boolean hasValidTimeHorizonCoord(String[] coords) {
 		
 		String period = coords[getTimeAxis()], year = coords[getYearAxis()];
 		String timeHorizonCoord = TimeSlice.buildTimeHorizonCoord(period, year);
 		if (isMember(getTimeHorizonDim(), timeHorizonCoord)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 
 	/**
 	 *	Get the list of Forward Plannable members for the specified year
 	 *
 	 * @param allPeriods All valid members of the time dimension
 	 * @param lockedPeriods List of current locked periods
 	 * 
 	 * @return List of Forward Plannable periods
 	 */
 	public List<String> getForwardPlannablePeriods(String year) {
 
 		List<String> fpPeriods = new ArrayList<String>();
 
 		// Return any periods from the Time dimension that are not locked
 		String[] allPeriods = getDimMembers(getTimeDim());
 		Set<String> lockedPeriods = getLockedPeriods(year);
 		for (String period : allPeriods) {
 			if (!lockedPeriods.contains(period)) {
 				fpPeriods.add(period);
 			}
 		}
 		return fpPeriods;
 	}
 
 
 	/**
 	 *	Get the list of open and valid periods in this data cache based on the 
 	 *  selected version and year
 	 *	 
 	 * @param version Selected member of version dimension
 	 * @param year Selected member of year dimension
 	 * 
 	 * @return List of open periods
 	 */
 	public List<String> getOpenPeriods(String version, String year) {
 
 		List<String> openPeriods = null;
 
 		// @TODO Consolidate this logic on the application service - it's currently being done in at least a couple of places, here and the view service
 		
 		// Does the selected Version Type and Year combination have locked periods?
 		if (hasLockedPeriods(version, year)) {
 			// Yes - Just return list of Forward Plannable members
 			openPeriods = getForwardPlannablePeriods(year);
 		} else {
 			// No locked periods - Return entire list of time members
 			openPeriods = new ArrayList<String>(Arrays.asList(getDimMembers(getTimeDim())));
 			
 			// Remove any periods that aren't valid across the time horizon (TTN-1858)
 			openPeriods.removeAll(getInvalidPeriods(year));
 		}
 		return openPeriods;
 	}
 
 
 	/**
 	 *	Get the list of all open time horizon periods for the current plan 
 	 *  version
 	 *	 
 	 * @return List of open time horizon periods
 	 */
 	public List<String> getOpenTimeHorizonPeriods() {
 		return getOpenTimeHorizonPeriods(getPlanVersion());		
 	}
 
 
 	/**
 	 *	Get the list of all open time horizon periods for the specified version 
 	 *	 
 	 * @return List of open time horizon periods
 	 */
 	public List<String> getOpenTimeHorizonPeriods(String version) {
 
 		List<String> openPeriods = new ArrayList<String>();
 		PafDimTree yearTree = getDimTrees().getTree(getYearDim());
 		
 		// Add in the open time horizon periods for each year
 		List<PafDimMember> yearMbrs = yearTree.getMembers(TreeTraversalOrder.POST_ORDER);
 		for (PafDimMember yearMbr : yearMbrs) {
 			openPeriods.addAll(getOpenTimeHorizonPeriods(version, yearMbr.getKey()));
 		}
 
 		return openPeriods;
 		
 		
 	}
 
 
 	/**
 	 *	Get the list open periods in this data cache based on the selected version and year
 	 *	 
 	 * @param version Selected member of version dimension
 	 * @param year Selected member of year dimension
 	 * 
 	 * @return List of open periods
 	 */
 	public List<String> getOpenTimeHorizonPeriods(String version, String year) {
 
 		// Get "regular" open time periods
 		List<String> openPeriods = getOpenPeriods(version, year);
 		
 		// Convert to time horizon periods
 		PafDimTree timeHorizonTree = getDimTrees().getTree(getTimeHorizonDim());
 		List<String> openTimeHorizonPeriods = new ArrayList<String>(openPeriods.size());
 		for (String period : openPeriods) {
 			String timeHorizonPeriod = TimeSlice.buildTimeHorizonCoord(period, year);
 			// Validate against time horizon tree
 			if (timeHorizonTree.hasMember(timeHorizonPeriod)) {
 				openTimeHorizonPeriods.add(TimeSlice.buildTimeHorizonCoord(period, year));
 			}
 		}
 		
 		return openTimeHorizonPeriods;
 	}
 
 
 	/**
 	 *	Returns the planning versions
 	 *
 	 * @return An array of planning versions
 	 */
 	public String[] getPlanVersions() {
 		return planVersions;
 	}
 
 	/**
 	 * Return the plan version
 	 * 
 	 * @return Plan version
 	 */
 	public String getPlanVersion() {
 		return planVersions[0];
 	}
 
 
 	/**
 	 * Return an array containing the dimensions that are represented in the data cache
 	 * 
 	 * @return Returns the dimension array.
 	 */
 	public String[] getAllDimensions() {
 		return allDimensions.toArray(new String[0]);
 	}
 
 	/**
 	 * @return Returns the appDef.
 	 */
 	public PafApplicationDef getAppDef() {
 		return appDef;
 	}
 
 	/**
 	 * @return Returns the mdbDef.
 	 */
 	public MdbDef getMdbDef() {
 		return appDef.getMdbDef();
 	}
 
 	/**
 	 *	Return the number of axes contained in the data cache
 	 *
 	 * @return Returns the number of axes contained in the data cache.
 	 */
 	public int getAxisCount() {
 		return axisCount;
 	}
 
 	/**
 	 *	Return the axis number that corresponds to the 
 	 *  the supplied dimension name
 	 *
 	 * @param dimName Dimension name
 	 * @return Returns the axis number of the supplied dimension name.
 	 */
 	public int getAxisIndex(String dimName) {
 
 		int index = 0;	
 
 		if (axisIndexMap.containsKey(dimName)) {
 			index = axisIndexMap.get(dimName);
 		} else {
 			// Dimension name not found
 			String errMsg = "Unable to get index for dimension ["
 				+ dimName + "] - this dimension does not exist in the paf data cache";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          
 		}
 
 		return index;
 	}
 
 
 	/**
 	 *	Return an array containing the members in the specified axis
 	 *
 	 * @param axis Index axis
 	 * @return Returns an array containing the members in the specified axis.
 	 */
 	public String[] getAxisMembers(int axis) {
 		return memberCatalog[axis];
 	}
 
 	/**
 	 *	Return the number of members in specified axis.
 	 *
 	 * @param axis Axis index
 	 * @return Returns the number of members in the specified axis.
 	 */
 	public int getAxisSize(int axis) {
 
 		// Validate axis
 		if (axis < 0 || axis > axisCount - 1) {
 			// Invalid axis value
 			String errMsg = "Unable to get size of axis ["
 				+ axis + "] - axis value is out of bounds";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          			
 		}
 
 		return axisSizes[axis];
 	}
 
 	/**
 	 *	Return the array containing the number of members in each axis.
 	 *
 	 * @return Returns the array containing the number of members in each axis.
 	 */
 	public int[] getAxisSizes() {
 		return axisSizes;
 	}
 
 	/**
 	 * Return the attribute dimensions in the specified intersection
 	 * 
 	 * @param intersection Intersection
 	 * @return the attributeDims
 	 */
 	public List<String> getIntersectionAttrDims(Intersection intersection) {
 		
 		// Assume all attribute dimensions appear after all base dimensions
 		List<String> attrDims = new ArrayList<String>();
 		int i = coreDimensions.size();
 		while (i < intersection.getSize()) {
 			attrDims.add(intersection.getCoordinates()[i]);
 			i++;
 		}		
 		return attrDims;
 
 	}
 
 	/**
 	 * @return the attributeDims
 	 */
 	public String[] getAttributeDims() {
 		return attributeDims;
 	}
 
 
 	/**
 	 * @return the baseDimensions
 	 */
 	public String[] getBaseDimensions() {
 		return baseDimensions;
 	}
 
 	/**
 	 * @return the base dimension count
 	 */
 	public int getBaseDimCount() {
 		return baseDimensions.length;
 	}
 
 
 	/**
 	 *	Return set of names of any base dimensions that have been
 	 *  assigned one or more attribute dimensions
 	 *
 	 * @return Set<String>
 	 */
 	public Set<String> getBaseDimNamesWithAttributes() {
 
 		Set<String> baseDimNames = new HashSet<String>();
 
 		// Iterate through all attribute dimensions and append all associated
 		// base dimensions to base dimension name set
 		for (String attrDim : this.attributeDims) {
 			PafAttributeTree attrTree = (PafAttributeTree) dimTrees.getTree(attrDim);
 			String baseDimName = attrTree.getBaseDimName();
 			if (!baseDimNames.contains(baseDimName)) {
 				baseDimNames.add(baseDimName);
 			}			
 		}
 
 		// Return all base dimensions that have been assigned one or more attribute dimensions
 		return baseDimNames;
 	}
 
 	
 	/**
 	 *	Return the data cache cell count
 	 *
 	 * @return Returns the data cache cell count.
 	 */
 	public int getCellCount() {
 		return this.getDataBlockCount() * blockSize;
 	}
 
 	/**
 	 *	Return the maximum core dimension cell count
 	 *
 	 * @return Returns the maximum data cache core dimension cell count.
 	 */
 	public int getMaxCoreCellCount() {
 		return this.getMaxCoreDataBlockCount() * blockSize;
 	}
 
 	/**
 	 *	Return the maximum data cache cell count
 	 *
 	 * @return Returns the maximum data cache cell count.
 	 */
 	public int getMaxCellCount() {
 		return this.getMaxDataBlockCount() * blockSize;
 	}
 
 	
 	/**
 	 * 	Returns an iterator that will iterate through all valid
 	 * 	intersections comprising the specified dimensions. The 
 	 * 	order of the supplied dimensions will determine the 
 	 *  dimension order of the intersection coordinates generated 
 	 *  by the iterator.
 	 *  
 	 * @param dimensions Dimensions to iterate through. 
 	 * @return Odometer
 	 */
 	public StringOdometer getCellIterator(String[] dimensions) {
 		return getCellIterator(dimensions, null);
 	}
 	
 	/**
 	 * 	Returns an iterator that will iterate through all valid
 	 * 	intersections comprising the specified dimensions. The 
 	 * 	order of the supplied dimensions will determine the 
 	 *  dimension order of the intersection coordinates generated 
 	 *  by the iterator.
 	 *  
 	 *  An optional member filter can be used to select specific members
 	 *  to iterate through.
 	 * 
 	 * @param dimensions Dimensions to iterate through. 
 	 * @param memberFilter Map containing a list for each filtered dimension
 	 * 
 	 * @return Odometer
 	 */
 	public StringOdometer getCellIterator(String[] dimensions, Map<String, List<String>> memberFilter) {
 		
 		// The Odometer requires member lists for each iterated dimension. So, a 
 		// member filter will be created if it's not already supplied and any
 		// missing dimensions will be added.
 		if (memberFilter == null) {
 			memberFilter = new HashMap<String, List<String>>();
 		}
 		memberFilter = addMissingDimsToMemberFilter(memberFilter, dimensions);
 		
 		StringOdometer cellIterator = new StringOdometer(memberFilter, dimensions);
 		return cellIterator;
 	}
 	
 	/**
 	 * 	Add any missing dimensions to member filter. All dimension members defined 
 	 *  in the data cache will be added for any missing dimensions. 
 	 * 
 	 * @param memberFilter Map containing member lists by dimension
 	 * @param dimensions List of dimensions that the map should contain. represented in filter
 	 */
 	private Map<String, List<String>> addMissingDimsToMemberFilter(Map<String, List<String>> memberFilter, String[] dimensions) {
 		
 		Map<String, List<String>> updatedMemberFilter =  new HashMap<String, List<String>>(memberFilter);
 		for (String dimension : dimensions) {
 			if (!updatedMemberFilter.containsKey(dimension)) {
 				updatedMemberFilter.put(dimension, new ArrayList<String>(Arrays.asList(getDimMembers(dimension))));
 			}
 		} 
 		
 		return updatedMemberFilter;
 	}
 
 
 	/**
 	 *	Return the cell value for the specified non-attribute intersection coordinates
 	 *
 	 * @param baseCoords Array of dimension members that define a non-attribute intersection
 	 * @return Returns the cell value.
 	 * 
 	 * @throws PafException 
 	 */
 	public double getBaseCellValue(String[] baseCoords) throws PafException {
 
 		double cellValue = 0;
 		
 		// Get the cell address of the specified intersection
 		DataCacheCellAddress cellAddress = generateBaseCellAddress(baseCoords);
 		Intersection dataBlockKey = cellAddress.getDataBlockKey();
 
 		// Look for underlying data block
 		DataBlock dataBlock = getDataBlock(dataBlockKey).getDataBlock();
 
 		// Retrieve the cell value if the data block exists
 		if (dataBlock != null) {
 			
 			// Data block exists
 			cellValue = dataBlock.getCellValue(cellAddress);
 			
 		} else {
 
 			// Data block does not exist - check if intersection is valid
 			if (isValidIntersection(baseCoords)) {
 				// Valid intersection
 				cellValue = 0;
 
 			} else {
 				// Invalid intersection - throw error
 				String errMsg = "Data Cache error - Unable to get data cache cell value for invalid intersection: "
 						+ StringUtils.arrayToString(baseCoords);
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg);
 			}
 		}
 
 		return cellValue;
 
 	}
 
 	/**
 	 *	Return the intersection cell value for the specified intersection coordinates
 	 *
 	 * @param dimensions Array of dimension names that define the cell intersection dimensionality
 	 * @param coords Array of dimension members that define the cell intersection
 	 * 
 	 * @return Returns the cell value.
 	 * @throws PafException 
 	 */
 	public double getCellValue(String[] dimensions, String[] coords) throws PafException {
 
 		Intersection intersection = new Intersection(dimensions, coords);
 		return getCellValue(intersection);
 	}
 
 	/**
 	 *	Return the cell value for the specified intersection. If the requested intersection
 	 *  is valid, but does not exist, then a zero value is returned.
 	 *
 	 * @param intersection Member intersection object that corresponds to data cache cell
 	 * 
 	 * @return Returns the cell value
 	 * @throws PafException 
 	 */
 	public double getCellValue(Intersection intersection) throws PafException  {
 
 		double cellValue = 0;
 		
 		// Convert time horizon based intersection to time-year intersection
 		Intersection translatedIs = translateTimeHorizonIs(intersection);
 		
 		// Get the cell address of the specified intersection
 		DataCacheCellAddress cellAddress = generateCellAddress(translatedIs);
 		Intersection dataBlockKey = cellAddress.getDataBlockKey();
 
 		// Determine if this is an attribute intersection
 		boolean isAttrIs = isAttributeIntersection(intersection);
 		
 		// Look for underlying data block
 		DataBlock dataBlock = getDataBlock(dataBlockKey).getDataBlock();
 
 		// Retrieve the cell value if the data block exists
 		if (dataBlock != null) {
 			
 			// Data block exists
 			if (isAttrIs && isEmptyIntersection(intersection)) {
 				// Un-populated attribute intersection - calculate missing value
 				String measure = intersection.getCoordinate(getMeasureAxis());
 				PafDataCacheCalc.calcAttributeIntersection(this, intersection, getMeasureType(measure), getDimTrees(), DcTrackChangeOpt.NONE);
 			}
 			cellValue = dataBlock.getCellValue(cellAddress);
 			
 		} else {
 
 			// Data block does not exist - check if intersection is valid
 			if (isValidIntersection(translatedIs)) {
 				// Valid intersection
 				if (!isAttrIs || isInvalidAttributeIntersection(translatedIs)) {
 					// Base intersection or invalid attribute intersection - just return 0
 					cellValue = 0;
 				} else {
 					// Valid attribute intersection - calculate missing value
 					String measure = intersection.getCoordinate(getMeasureAxis());
 					PafDataCacheCalc.calcAttributeIntersection(this, intersection, getMeasureType(measure), getDimTrees(), DcTrackChangeOpt.NONE);
 					dataBlock = getDataBlock(dataBlockKey).getDataBlock();
 					
 					// Get value of attribute intersection. If it still doesn't
 					// exist, that would indicate that the underlying base 
 					// intersections don't exist. In that case the returned
 					// value should just be zero.
 					if (dataBlock != null) {
 						cellValue = dataBlock.getCellValue(cellAddress);
 					}
 				}
 
 			} else {
 				// Invalid intersection - throw error
 				String errMsg = "Data Cache error - Unable to get data cache cell value for invalid intersection: "
 						+ StringUtils.arrayToString(translatedIs.getCoordinates());
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg);
 			}
 		}
 
 		return cellValue;
 
 	}
 
 
 	/**
 	 * Return "snapshot" value of specified intersection. If the requested intersection
 	 * is valid, but does not exist, then a zero value is returned.
 	 * 
 	 * @param cellIs Cell intersection
 	 * 
 	 * @return Snapshot value of specified intersection
 	 * @throws PafException 
 	 */
 	public double getSnapshotValue(Intersection cellIs) throws PafException {
 		double cellValue = 0;
 		
 		// Convert time horizon based intersection to time-year intersection
 		Intersection translatedIs = translateTimeHorizonIs(cellIs);
 		
 		// Get the cell address of the specified intersection
 		DataCacheCellAddress cellAddress = generateCellAddress(translatedIs);
 		Intersection dataBlockKey = cellAddress.getDataBlockKey();
 
 		// Look for underlying data block
 		DataBlock dataBlock = getSnapshotDataBlock(dataBlockKey).getDataBlock();
 
 		// Retrieve the cell value if the data block exists
 		if (dataBlock != null) {
 			
 			cellValue = dataBlock.getCellValue(cellAddress);
 			
 		} else {
 
 			// Data block does not exist - check if intersection is valid
 			if (isValidIntersection(translatedIs)) {
 				// Valid intersection - just return zero
 				cellValue = 0;
 			} else {
 				// Invalid intersection - throw error
 				String errMsg = "Data Cache error - Unable to get data cache cell value for invalid intersection: "
 						+ StringUtils.arrayToString(translatedIs.getCoordinates());
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg);
 			}
 		}
 
 		return cellValue;
 	}
 
 
 	/**
 	 * Take a snapshot of plannable data 
 	 * @throws PafException 
 	 */
 	public void snapshotPlannableData() throws PafException {
 		
 		final String versionDim = this.getVersionDim(), yearDim = this.getYearDim();
 		final String planVersion = this.getPlanVersion();
 		final List<String> planYears = new ArrayList<String>(this.getPlanYears());
 		
 		// Create member filter that represents all plannable intersections - the plan version
 		// across all plannable years. Because of limitations in our typical "member filter"
 		// structure, forward-plannable intersections are included as well, even though 
 		// technically they are non-plannable.
 		Map<String, List<String>> memberFilter = new HashMap<String, List<String>>();
 		memberFilter.put(versionDim, Arrays.asList(new String[]{planVersion}));
 		memberFilter.put(yearDim, planYears);
 		
 		// Initialize snapshot values
 		//TODO Only initialize plannable data blocks, not all snapshot blocks
 		clearSnapshotValues();
 
 		// Snapshot plannable data
 		snapshotFilteredData(memberFilter);
 	}
 
 
 
 	/**
 	 * Snapshot plannable data blocks based on the supplied member filter (Version and/or Year dimensions)
 	 * 
 	 * @param memberFilter Member filter comprised of member lists by each filtered dimension
 	 * @throws PafException 
 	 */
 	public void snapshotFilteredData(Map<String, List<String>> memberFilter) throws PafException {
 		
 		// TODO At this point, only filters on the Version and Year dimensions are considered
 		final String versionDim = this.getVersionDim(), yearDim = this.getYearDim();
 		Set<Intersection> dataBlockKeys = null;
 		
 		// Check for missing member filter 
 		if (memberFilter == null || memberFilter.isEmpty()) {
 			String errMsg = "Unable to snapshot filtered data - member filter is null or empty";
 			logger.error(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 	
 		// Get the list of version filtered data blocks
 		if (memberFilter.get(versionDim) != null) {
 			dataBlockKeys = new HashSet<Intersection>();
 			List<String> filteredVersions = memberFilter.get(versionDim);
 			for (String version : filteredVersions) {
 				if (dataBlocksByVersion.get(version) !=null ) {
 					dataBlockKeys.addAll(dataBlocksByVersion.get(version));
 				}
 			}
 		} else {
 			dataBlockKeys = dataBlockIndexMap.keySet();
 		}
 
 
 		// Cycle through data blocks and snapshot the ones that match the year filter
 		List<String> filteredYears = memberFilter.get(yearDim);
 		for (Intersection dataBlockKey : dataBlockKeys) {
 			if (filteredYears != null) {
 				String yearCoord = dataBlockKey.getCoordinate(yearDim);
 				if (filteredYears.contains(yearCoord)) {
 					snapshotDataBlock(dataBlockKey);
 				}
 			} else {
 				snapshotDataBlock(dataBlockKey);
 			}
 		}
 
 	}
 
 
 
 	/**
 	 * Snapshot specified data block (make a stored copy)
 	 * 
 	 * @param key Data block key
 	 * @throws PafException 
 	 */
 	private void snapshotDataBlock(Intersection key) throws PafException {
 
 		int surrogateKey = 0;
 		DataBlock dataBlock = null;
 		DataBlockResponse dataBlockResp = null;
 		
 		
 		// Unlike regular data blocks, a link between alias snapshot blocks
 		// and base snapshot blocks will not be maintained. This was done
 		// mainly for simplification purposes, but also allows alias blocks
 		// to be snapshotted independently of their underlying base blocks.
 		
 		
 		// First verify that data block exists
 		dataBlockResp = getDataBlock(key);
 		dataBlock = dataBlockResp.getDataBlock();
 		if (dataBlock == null) {
 			String msg = "An attempt was made to snapshot a non-existent data block: "
 				+ key.toString() + " .";
 			logger.error(msg);
 			throw new IllegalArgumentException(msg);
 		}
 		
 		// Delete existing snapshot
 		DataBlockResponse snapshotDbResponse = getSnapshotDataBlock(key);
 		DataBlock snapshotDataBlock = snapshotDbResponse.getDataBlock();
 		if (snapshotDataBlock != null) {
 			deleteSnapshotDataBlock(key);
 		}
 		
 		// Add snapshot data block to pool. Attempt to reuse any previously deleted blocks.
 		DataBlock clonedDataBlock = dataBlock.clone();
 		if (deletedSnapshotIndexes.size() == 0) {
 			// Add index entry for new data block (index is auto-incremented)
 			surrogateKey = snapshotBlockCount;
 			snapshotPool.add(clonedDataBlock);
 		} else {
 			// Reuse index of a deleted block
 			surrogateKey = deletedSnapshotIndexes.removeLast();
 			snapshotPool.set(surrogateKey, clonedDataBlock);
 		}
 		snapshotIndexMap.put(key, surrogateKey);
 		snapshotBlockCount++;
 
 		
 	}
 
 
 	/**
 	 *	Set the value for a specific base intersection cell
 	 *
 	 * @param coords Array of dimension members that define a single base intersection
 	 * @param value Value to put into cell
 	 * 
 	 * @throws PafException 
 	 */
 	public void setBaseCellValue(String[] coords, double value) throws PafException {
 
 		Intersection intersection = new Intersection(getBaseDimensions(), coords);
 		setCellValue(intersection, value);
 	}
 
 	/**
 	 *	Return the intersection cell value for the specified intersection coordinates
 	 *
 	 * @param dimensions Array of dimension names that define the cell intersection dimensionality
 	 * @param coords Array of dimension members that define the cell intersection
 	 * @param value Value to put into cell
 	 * 
 	 * @return Returns the cell value.
 	 * @throws PafException 
 	 */
 	public void setCellValue(String[] dimensions, String[] coords, double value) throws PafException {
 
 		Intersection intersection = new Intersection(dimensions, coords);
 		setCellValue(intersection, value);
 	}
 
 
 	
 	/**
 	 *	Set the value for a specific data cache cell and optionally track changes
 	 *
 	 * @param intersection Cell intersection
 	 * @param value Value to put into cell
 	 * @param trackChangeOpt Data cache change tracking option
 	 * 
 	 * @throws PafException 
 	 */
 	public void setCellValue(Intersection intersection, double value, DcTrackChangeOpt trackChangeOpt) throws PafException {
 
         // Update cell value
         if (trackChangeOpt == DcTrackChangeOpt.NONE) {
               setCellValue(intersection, value);
         } else {
               setCellValueAndTrackChanges(intersection, value);
         }     
 
 	}
 		
 	/**
 	 *	Set the value for a specific data cache cell and track any changed cells
 	 *
 	 * @param intersection Cell intersection
 	 * @param value Value to put into cell
 	 * 
 	 * @throws PafException 
 	 */
 	public void setCellValueAndTrackChanges(Intersection intersection, double value) throws PafException {
 
 		// Convert time horizon based intersection to time-year intersection
 		Intersection translatedIs = translateTimeHorizonIs(intersection);
 		
 		double oldCellValue = getCellValue(translatedIs);
 		setCellValue(translatedIs, value);
 		if (Math.abs(value - oldCellValue) > PafBaseConstants.DC_TRACK_CHANGES_THRESHHOLD) {
 			// Clone intersection before adding to collection as the calling method
 			// may reuse this intersection object to point to a different intersection
 			addChangedCell(translatedIs.clone(), value);
 		}
 	}
 
 	/**
 	 *	Set the value for a specific data cache cell
 	 *
 	 * @param intersection Cell intersection
 	 * @param value Value to put into cell
 	 * 
 	 * @throws PafException 
 	 */
 	public void setCellValue(Intersection intersection, double value) throws PafException {
 		
 		DataCacheCellAddress cellAddress = null;
 		
 		
 		// Convert time horizon based intersection to time-year intersection
 		Intersection translatedIs = translateTimeHorizonIs(intersection);
 		
 		// Round updated cell value to a predefined number of digits to mask any potential precision errors
 		long longValue = (long) value;
 		double signedMantissa = value - longValue;
 		double roundedMantissa = Math.round(signedMantissa * PafBaseConstants.DC_ROUNDING_CONSTANT)
 								/ PafBaseConstants.DC_ROUNDING_CONSTANT;
 		double roundedValue = longValue + roundedMantissa;
 		
 		// To avoid the creation of unneeded data blocks, zero values will
 		// not be set if the intersection doesn't already exist.
 		if (roundedValue == 0) {
 			cellAddress = generateCellAddress(intersection);
 			if (!isExistingDataBlock(cellAddress.getDataBlockKey())) {
 				return;
 			}
 		} else {
 			// Add intersection if it doesn't already exist
 			cellAddress = addCell(translatedIs);			
 		}
 		
 		// Update cell value
 		DataBlock dataBlock = getDataBlock(cellAddress.getDataBlockKey()).getDataBlock();
 		dataBlock.setCellValue(cellAddress, roundedValue);
 
 		// Set dirty flag on cell, data block, and data cache, to indicate that the objects
 		// were updated
 		dataBlock.setDirty();
 		dataBlock.setCellProperty(CellPropertyType.Dirty, cellAddress, true);
 		setDirty();
 		
 		// Indicate that cell exists
 		dataBlock.setCellProperty(CellPropertyType.Empty, cellAddress, false);
 	}
 
 
 	/**
 	 * Returns the value of the next cell intersection along the time dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 *
 	 * @return Cell value
 	 * @throws PafException 
 	 */
 	public double getNextCellValue(Intersection cellIs) throws PafException {
 		return getNextCellValue(cellIs, getTimeDim(), 1);
 	}
 
 	/**
 	 * Returns the value of the next cell intersection along the specified
 	 * offset dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * 
 	 * @return Cell value
 	 * @throws PafException 
 	 */
 	public double getNextCellValue(final Intersection cellIs, final String offsetDim, final int offset) throws PafException {
 
 		double result = 0;
 
 		final Intersection nextIs = getNextIntersection(cellIs, offsetDim, offset, false);
 		if (nextIs != null) {
 			result = getCellValue(nextIs);
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the value of the next cell intersection along the specified
 	 * offset dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * @param bWrap Indicates if search along the offset dimension should wrap around to the beginning/end of the tree
 	 * 
 	 * @return Cell value
 	 * @throws PafException 
 	 */
 	public double getNextCellValue(final Intersection cellIs, final String offsetDim, final int offset, final boolean bWrap) throws PafException {
 
 		double result = 0;
 
 		final Intersection nextIs = getNextIntersection(cellIs, offsetDim, offset, bWrap);
 		if (nextIs != null) {
 			result = getCellValue(nextIs);
 		}
 		return result;
 	}
 
 
 	/**
 	 * Returns the next cell intersection along the time dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @return Cell intersection
 	 */
 	public Intersection getNextIntersection(final Intersection cellIs) {
 		return getNextIntersection(cellIs, getTimeDim(), 1);
 	}
 
 	/**
 	 * Returns the next cell intersection along the specified offset dimension. A null value
 	 * will be returned if the offset points to an out of bounds location
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * 
 	 * @return Cell intersection
 	 */
 	public Intersection getNextIntersection(final Intersection cellIs, final String offsetDim, final int offset) {
 		return getNextIntersection(cellIs, offsetDim, offset, false);
 	}
 
 	/**
 	 * Returns the next cell intersection along the specified offset dimension. A null value
 	 * will be returned if the offset points to an out of bounds location and bWrap is 
 	 * set to false.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * @param bWrap Indicates if search along the offset dimension should wrap around to the beginning/end of the tree
 	 * 
 	 * @return Cell intersection
 	 */
 	public Intersection getNextIntersection(final Intersection cellIs, final String offsetDim, final int offset, final boolean bWrap) {
 	
 		Intersection nextIs = cellIs.clone();
 		nextIs = shiftIntersection(nextIs, offsetDim, offset, true, bWrap);
 		return nextIs;
 	}
 
 
 	/**
 	 * Shift the specified intersection to the next cell intersection along the time dimension.
 	 * 
 	 * The intersection will be set to a null value will be returned if 
 	 * the offset points to an out of bounds location.
 	 * 
 	 * @param cellIs Cell intersection
 	 */
 	public Intersection shiftIntersection(Intersection cellIs) {
 		return shiftIntersection(cellIs, getTimeDim(), 1);		
 	}
 
 	/**
 	 * Shift the specified intersection to the next cell intersection along the specified
 	 * offset dimension. A backwards shift will be performed if a negative offset is 
 	 * supplied.
 
 	 * The intersection will be set to a null value will be returned if 
 	 * the offset points to an out of bounds location.
 	 * 
 	 * This function should be used in place of the getNextIntersection() and getPrevInstersection()
 	 * methods if the value of cellIs can be modified without any ill effects.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 */
 	public Intersection shiftIntersection(Intersection cellIs, final String offsetDim, final int offset) {
 		return shiftIntersection(cellIs, offsetDim, offset, true);		
 	}
 
 	/**
 	 * Shift the specified intersection to the next cell intersection along the specified
 	 * offset dimension. A backwards shift will be performed if a negative offset is 
 	 * supplied.
 
 	 * The intersection will be set to a null value will be returned if 
 	 * the offset points to an out of bounds location.
 	 * 
 	 * This function should be used in place of the getNextIntersection() and getPrevInstersection()
 	 * methods if the value of cellIs can be modified without any ill effects.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * @param bCrossYears Indicates that the shift should span years
 	 */
 	public Intersection shiftIntersection(Intersection cellIs, String offsetDim, int offset, boolean bCrossYears) {
 		return shiftIntersection(cellIs, offsetDim, offset, bCrossYears, false);		
 	}
 
 	/**
 	 * Shift the specified intersection to the next cell intersection along the specified
 	 * offset dimension. A backwards shift will be performed if a negative offset is 
 	 * supplied. 
 	 * 
 	 * A null value will be returned if the offset points to an out of bounds location and bWrap is set to false.
 	 * 
 	 * This function should be used in place of the getNextIntersection() and getPrevInstersection()
 	 * methods if the value of cellIs can be modified without any ill effects.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * @param bCrossYears Indicates that the shift should span years
 	 * @param bWrap Indicates if search along the offset dimension should wrap around to the beginning/end of the tree
 	 */
 	public Intersection shiftIntersection(Intersection cellIs, final String offsetDim, final int offset, 
 			final boolean bCrossYears, final boolean bWrap) {
 	
 		PafDimTree offsetTree = null;
 		PafDimMember nextMbr = null;
 		String currMbrName = null; 
 		String currYear = cellIs.getCoordinate(getYearAxis());
 
 		
 		// If time dimension is selected, use time horizon dimension for shift
 		if (offsetDim.equals(getTimeDim())) {
 			offsetTree = getDimTrees().getTree(getTimeHorizonDim());
 			currMbrName = TimeSlice.buildTimeHorizonCoord(cellIs.getCoordinate(getTimeAxis()), currYear);
 			nextMbr = offsetTree.getPeer(currMbrName, offset, bWrap);		
 			if (nextMbr != null) {
 				TimeSlice.applyTimeHorizonCoord(cellIs, nextMbr.getKey(), getAppDef().getMdbDef());
 			} else {
 				cellIs = null;
 			}
 		} else {
 			// Use specified dimension for shift
 			offsetTree = getDimTrees().getTree(offsetDim);
 			currMbrName = cellIs.getCoordinate(axisIndexMap.get(offsetDim));
 			nextMbr = offsetTree.getPeer(currMbrName, offset, bWrap);
 			if (nextMbr != null) {
 				cellIs.setCoordinate(offsetDim, nextMbr.getKey());
 			} else {
 				cellIs = null;
 			}
 		}
 		
 		// Check if we've crossed years, if spanning of years is not allowed (TTN-1597)
 		if (!bCrossYears && cellIs !=null) {
 			String nextYear = cellIs.getCoordinate(getYearAxis());
 			if (!nextYear.equals(currYear)) cellIs = null;
 		}
 
 
 		return cellIs;
 	}
 
 
 	/**
 	 * Returns the value of the previous cell intersection along the time dimension
 	 * @param cellIs Cell intersection
 	 * 
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * 
 	 * @return Cell value
 	 * @throws PafException 
 	 */
 	public double getPrevCellValue(Intersection cellIs) throws PafException {
 		return getPrevCellValue(cellIs, getTimeDim(), 1);
 	}
 
 	/**
 	 * Returns the value of the previous cell intersection along the specified
 	 * offset dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * 
 	 * @return Cell value
 	 * @throws PafException 
 	 */
 	public double getPrevCellValue(Intersection cellIs, String offsetDim, int offset) throws PafException {
 		return getPrevCellValue(cellIs, offsetDim, offset, false);
 	}
 
 	/**
 	 * Returns the value of the previous cell intersection along the specified
 	 * offset dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * @param bWrap Indicates if search along the offset dimension should wrap around to the beginning/end of the tree
 	 * 
 	 * @return Cell value
 	 * @throws PafException 
 	 */
 	public double getPrevCellValue(Intersection cellIs, String offsetDim, int offset, boolean bWrap) throws PafException {
 		return getNextCellValue(cellIs, offsetDim, -offset, false);
 	}
 
 	/**
 	 * Returns the prev cell intersection along the specified offset dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * 
 	 * @return Cell intersection
 	 */
 
 	public Intersection getPrevIntersection(Intersection cellIs, String offsetDim) {
 		return getPrevIntersection(cellIs, offsetDim, 1);
 	}
 
 	/**
 	 * Returns the prev cell intersection along the specified offset dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * 
 	 * @return Cell intersection
 	 */
 
 	public Intersection getPrevIntersection(Intersection cellIs, String offsetDim, int offset) {
 		return getPrevIntersection(cellIs, offsetDim, offset, false);
 	}
 
 	/**
 	 * Returns the prev cell intersection along the specified offset dimension. A null value
 	 * will be returned if the offset points to an out of bounds location and bWrap is 
 	 * set to false.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param offsetDim Offset dimension
 	 * @param offset Specifies a relative position, along the offset dimension, that will be used to retrieve the desired intersection
 	 * @param bWrap Indicates if search along the offset dimension should wrap around to the beginning/end of the dimension tree
 	 * 
 	 * @return Cell intersection
 	 */
 
 	public Intersection getPrevIntersection(Intersection cellIs, String offsetDim, int offset, boolean bWrap) {
 		return getNextIntersection(cellIs, offsetDim, -offset, bWrap);
 	}
 
 
 	/**
 	 * Provides a count of the descendants, at the specified level, of the current intersection,
 	 * and it's left peers along the specified dimension. 
 	 * 
 	 * The time dimension is used by default if no dimension parameter is supplied. 
 	 * The dimension floor level is used by default if no level parameter is supplied. 
 	 * 
  	 * 
 	 * @param cellIs Cell intersection
 	 * 
 	 * @return Cumulative total
 	 */	
 	public double getCumMbrCount(final Intersection cellIs) {
 		return getCumMbrCount(cellIs, getTimeDim());
 	}
 		
 	/**
 	 * Provides a count of the descendants, at the specified level, of  the current intersection,
 	 * and it's left peers along the specified dimension. 
 	 * 
 	 * The dimension floor level is used by default if no level parameter is supplied. 
 	 * 
  	 * 
 	 * @param cellIs Cell intersection
 	 * @param cumDim Dimension being accumulated
 	 * 
 	 * @return Cumulative total
 	 */	
 	public double getCumMbrCount(final Intersection cellIs, final String cumDim) {
 		
 		PafDimTree cumTree = getDimTrees().getTree(cumDim);
 		int level = cumTree.getLowestAbsLevelInTree();
 		
 		return getCumMbrCount(cellIs, cumDim, level);
 	}
 		
 	/**
 	 * Provides a count of the descendants, at the specified level, of  the current intersection,
 	 * and it's left peers along the specified dimension. 
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param cumDim Dimension being accumulated
 	 * @param level The level of the members being accumulated
 	 * 
 	 * @return Cumulative total
 	 */	
 	public double getCumMbrCount(final Intersection cellIs, final String cumDim, final int level) {
 	
       	// Get list of cum members for the specified level
 		String cumMember;
 		PafDimTree cumTree;
 		if (cumDim.equals(getTimeDim())) {
 			// Use time horizon tree whenever the time dimension is specified
 			cumTree = getDimTrees().getTree(getTimeHorizonDim());
 			cumMember = TimeSlice.buildTimeHorizonCoord(cellIs.getCoordinate(getTimeAxis()), cellIs.getCoordinate(getYearAxis()));
 		} else {
 			cumTree = getDimTrees().getTree(cumDim);			
 			cumMember = cellIs.getCoordinate(axisIndexMap.get(cumDim));
 		}	
 		List<PafDimMember> cumMembers = cumTree.getCumMembers(cumMember, level);
 
 		// Return number of cum members
 		return cumMembers.size();
 	}
 	
 	/**
 	 * Provides a count of the descendants, at the specified level, of  the current intersection,
 	 * and it's left peers along the specified dimension. 
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param cumDim Dimension being accumulated
 	 * @param level The level of the members being accumulated
 	 * 
 	 * @return Cumulative total
 	 */	
 	public double getCumMbrCount(Intersection cellIs, String cumDim,LevelGenType levelGenType, int levelGen, String yearMbr) {
 
       	// Get list of cum members for the specified level
 		String cumMember;
 		PafDimTree cumTree;
 		if (cumDim.equals(getTimeDim())) {
 			// Use time horizon tree whenever the time dimension is specified
 			cumTree = getDimTrees().getTree(getTimeHorizonDim());
 			cumMember = TimeSlice.buildTimeHorizonCoord(cellIs.getCoordinate(getTimeAxis()), cellIs.getCoordinate(getYearAxis()));
 		} else {
 			cumTree = getDimTrees().getTree(cumDim);			
 			cumMember = cellIs.getCoordinate(axisIndexMap.get(cumDim));
 		}	
 		List<PafDimMember> cumMembers = cumTree.getCumMembers(cumMember, levelGen);
 
 		// Return number of cum members
 		return cumMembers.size();
 	}
 
 
 		
 	/**
 	 * Returns the cumulative total of specified intersection along the time dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param cumDim Dimension being accumulated
 	 * 
 	 * @return Cell value
 	 * @throws PafException 
 	 */	
 	public double getCumTotal(final Intersection cellIs) throws PafException {
 		return getCumTotal(cellIs, getTimeDim());
 	}
 
 	/**
 	 * Returns the cumulative total of specified intersection, along the specified dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param cumDim Dimension being accumulated
 	 * 
 	 * @return Cell value
 	 * @throws PafException 
 	 */	
 	public double getCumTotal(final Intersection cellIs, final String cumDim) throws PafException {
 		return getCumTotal(cellIs, cumDim, 0);
 	}
 
 	/**
 	 * Returns the cumulative total of specified intersection, along the specified dimension,
 	 * up through the nth previous intersection. An offset of 0 indicates that all intersections
 	 * leading up to, and including, the specified intersection will be accumulated. 
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param cumDim Dimension being accumulated
 	 * @param offset Specifies a relative position, along the cum dimension, that will be used to retrieve the desired intersection
 	 * 
 	 * @return Cumulative total
 	 * @throws PafException 
 	 */	
 	public double getCumTotal(final Intersection cellIs, final String cumDim, final int offset) throws PafException {
 
 		double result = 0;
  		
 		// Determine the last intersection to be accumulated
 		Intersection lastIs = getPrevIntersection(cellIs, cumDim, offset);
 		
 		// Accumulate the values for all intersections up through the specified offset
 		// position.
 		while (lastIs != null) {
 			// To account for any cell changes that haven't yet been aggregated 
 			// (ex. perpetual inventory process), we aggregate at the floor level.
 			result += EvalUtil.sumFloorIntersections(lastIs, evalState);
 			lastIs = shiftIntersection(lastIs, cumDim, -1);
 		}
 		
 		return result;
 	}
 
 
 	/**
 	 * Returns the cumulative total of specified intersection, along the specified dimension,
 	 * up through the nth previous intersection. An offset of 0 indicates that all intersections
 	 * leading up to, and including, the specified intersection will be accumulated. 
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param cumDim Dimension being accumulated
 	 * @param offset Specifies a relative position, along the cum dimension, that will be used to retrieve the desired intersection
 	 * @param genLevelType Generation or Level Optional parameter that specifies the dimension branch to confine search to
 	 * @param genLevel Generation/level Optional parameter that specifies the dimension branch to confine search to
 	 * @param yearMbr Optional parameter that specifies which year to confine search to (ignored if 'dim' is not the Time dimension)
 	 * 
 	 * @return Cumulative total
 	 * @throws PafException 
 	 */	
 	public double getCumTotal(Intersection cellIs, String cumDim, int offset, LevelGenType levelGenType, int levelGen, String yearMbr)
 			throws PafException {
 
 		double result = 0;
  	
 		// Get the list of intersections to be accumulated
 		List<Intersection> cumIsList = getILPeerIntersections(cellIs,  cumDim, levelGenType, levelGen, yearMbr);
 		
 		// Determine the last intersection to be accumulated
 		Intersection lastIs = getPrevIntersection(cellIs, cumDim, offset);
 		
 		// Exit if boundary condition is reached (or if no intersections to accumulate)
 		if (lastIs == null) return result;
 		int lastIndex = cumIsList.lastIndexOf(lastIs);
 		if (lastIndex == -1) return result;
 		
 		// Accumulate the values for all intersections up through the specified offset
 		// position in the specified scope.
 		int index = 0;
 		for (Intersection cumIs : cumIsList) {
 			// To account for any cell changes that haven't yet been aggregated 
 			// (ex. perpetual inventory process), we aggregate at the floor level.
 			result += EvalUtil.sumFloorIntersections(cumIs, evalState);
 			
 			// Check for boundary condition
 			if (index == lastIndex) break;
 			
 			// Set index to next intersection
 			index++;
 		}
 		
 		return result;
 	}
 
 
 
 	/**
 	 * Return a list of left peer intersections within the specified scope along with the queried member
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension
 	 * @param genLevelType Generation or Level Optional parameter that specifies the dimension branch to confine search to
 	 * @param genLevel Generation/level Optional parameter that specifies the dimension branch to confine search to
 	 * @param yearMbr Optional parameter that specifies which year to confine search to (ignored if 'dim' is not the Time dimension)
 	 * 
 	 * @return List of left peer intersections
 	 * @throws PafException 
 	 */
 	public List<Intersection> getILPeerIntersections(Intersection cellIs, String dim, LevelGenType levelGenType, 
 			int levelGen, String yearMbr) throws PafException {
 		
 		List<Intersection> iLPeerIsList = getLPeerIntersections(cellIs, dim, levelGenType, levelGen, yearMbr);
 		if (iLPeerIsList != null) {
 			iLPeerIsList.add(cellIs);
 		} else {
 			// A null value indicates that the cell intersection doesn't match the specified year member
 			iLPeerIsList = new ArrayList<Intersection>();
 		}
 		return iLPeerIsList;
 		
 	}
 
		/**
 	 * Return a list of left peer intersections within the specified scope
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension
 	 * @param genLevelType Generation or Level Optional parameter that specifies the dimension branch to confine search to
 	 * @param genLevel Generation/level Optional parameter that specifies the dimension branch to confine search to
 	 * @param yearMbr Optional parameter that specifies which year to confine search to (ignored if 'dim' is not the Time dimension)
 	 * 
 	 * @return List of left peer intersections
 	 * @throws PafException 
 	 */
 	public List<Intersection> getLPeerIntersections(Intersection cellIs, String dim, LevelGenType levelGenType, 
 			int levelGen, String yearMbr) throws PafException {
 
 		List<Intersection> lPeerIsList = new ArrayList<Intersection>();
 		PafDimTree dimTree = null;
 		PafDimMember timeMbr = null;
 		String timeCoord = null, treeRoot = null;
 		int genOffset = 0;
 
 
 		// If time dimension is selected, substitute time horizon dimension for query
 		if (dim.equals(getTimeDim())) {
 			dimTree = getDimTrees().getTree(getTimeHorizonDim());
 			timeCoord = IntersectionUtil.getIsCoord(cellIs, dim, this);
 
 			// For generation search, we need to match desired gen to time horizon tree
 			if (levelGenType == LevelGenType.GEN) {
 				genOffset = getTimeHorizGenOffset();
 			}
 
 			// Check if year member is specified (only valid if Time dimension is being traversed)
 			if (yearMbr == null) {
 				// No year member specification
 				treeRoot = dimTree.getRootNode().getKey();	
 			} else {
 				// Check if current time coordinate is contained in selected year. If not,
 				// return null
 				TimeSlice timeSlice = new TimeSlice(timeCoord);
 				String yearCoord = timeSlice.getYear();
 				if (!yearCoord.equals(yearMbr)) {
 					return null;
 				}
 				// Year member specified - use a pruned copy of time horizon tree under specified year
 				PafDimTree timeTree = getDimTrees().getTree(getTimeDim());
 				treeRoot = TimeSlice.buildTimeHorizonCoord(timeTree.getRootNode().getKey(), yearMbr);
 				dimTree = dimTree.getSubTreeCopy(treeRoot);
 			}
 
 		} else {
 			dimTree = getDimTrees().getTree(dim);
 			treeRoot = dimTree.getRootNode().getKey();
 			timeCoord = cellIs.getCoordinate(dim);
 		}
 		timeMbr = dimTree.getMember(timeCoord);
 
 
 		// Find the ancestor that matches the specified scope
 		PafDimMember ancestorMbr = null;
 		ancestorMbr = dimTree.getAncestor(timeMbr, levelGenType, levelGen + genOffset);
 
 		// Get the list of descendant peers (all members at same level) within the 
 		// specified scope and use them to generate the peer intersections
 		List<String> lPeers = PafDimTree.getMemberNames(dimTree.getLPeers(timeMbr.getKey()));
 		int level = timeMbr.getMemberProps().getLevelNumber();
 		List<String> descPeers = PafDimTree.getMemberNames(dimTree.getMembersAtLevel(ancestorMbr.getKey(), (short) level));
 		lPeers.retainAll(descPeers);
 		for (String lPeer : lPeers) {
 			Intersection lPeerIs = cellIs.clone();
 			EvalUtil.setIsCoord(lPeerIs, dim, lPeer, evalState);
 			lPeerIsList.add(lPeerIs);
 		}
 		
 		// Return peer intersections
 		return lPeerIsList;
 	}
 
 
 	/**
 	 * Return the first descendant intersection at the specified level in the specified
 	 * dimension or the floor, whichever is greater. 
 	 * 
 	 * The original intersection will be returned if it has no descendants along has no 
 	 * descendants along the specified dimension.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension to traverse
 	 * @param level Member level of requested descendant
 	 * 
 	 * @return First descendant intersection
 	 * @throws PafException 
 	 */	
 	public Intersection getFirstDescendantIs(final Intersection cellIs, final String dim, int level) {		
 
 		final String timeDim = this.getTimeDim(); 
 		String branch = null;
 		PafDimTree dimTree = null;
 		final MdbDef mdbDef = this.getMdbDef();
 
 		
 		// If time dimension is selected, use time horizon dimension for traversal
 		if (dim.equals(getTimeDim())) {
 			dimTree = getDimTrees().getTree(getTimeHorizonDim());
 			branch = TimeSlice.buildTimeHorizonCoord(cellIs, mdbDef);
 		} else {
 			dimTree = getDimTrees().getTree(dim);
 			branch = cellIs.getCoordinate(axisIndexMap.get(dim));
 		}
 
 		// If the ancestor member is a leaf node then just return the original
 		// intersection.
 		if (dimTree.isLeaf(branch)) {
 			return cellIs;
 		}
 
 		// Get the first descendant members at specified level and use it to
 		// clone the descendant intersection.
 		level = Math.max(level, dimTree.getLowestAbsLevelInTree());
 		PafDimMember descMember = dimTree.getFirstDescendant(branch, (short) level);
 		Intersection descIs = cellIs.clone();
 		if (dim.equals(timeDim)) {
 			TimeSlice.applyTimeHorizonCoord(descIs, descMember.getKey(), mdbDef);
 		} else {
 			descIs.setCoordinate(dim, descMember.getKey());
 		}
 
 		return descIs;
 	}
 
 	/**
 	 * Return the last descendant intersection at the specified level in the specified
 	 * dimension or the floor, whichever is greater. 
 	 * 
 	 * The original intersection will be returned if it has no descendants along has no 
 	 * descendants along the specified dimension.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension to traverse
 	 * @param level Member level of requested descendant
 	 * 
 	 * @return First descendant intersection
 	 * @throws PafException 
 	 */	
 	public Intersection getLastDescendantIs(Intersection cellIs, String dim, int level) {		
 
 		String timeDim = this.getTimeDim(), branch = null;
 		PafDimTree dimTree = null;
 		MdbDef mdbDef = this.getMdbDef();
 
 		
 		// If time dimension is selected, use time horizon dimension for traversal
 		if (dim.equals(getTimeDim())) {
 			dimTree = getDimTrees().getTree(getTimeHorizonDim());
 			branch = TimeSlice.buildTimeHorizonCoord(cellIs, mdbDef);
 		} else {
 			dimTree = getDimTrees().getTree(dim);
 			branch = cellIs.getCoordinate(axisIndexMap.get(dim));
 		}
 
 		// If the ancestor member is a leaf node then just return the original
 		// intersection.
 		if (dimTree.isLeaf(branch)) {
 			return cellIs;
 		}
 
 		// Get the last descendant members at specified level and use it to
 		// clone the descendant intersection.
 		PafDimMember descMember = dimTree.getLastDescendant(branch, (short) level);
 		Intersection descIs = cellIs.clone();
 		if (dim.equals(timeDim)) {
 			TimeSlice.applyTimeHorizonCoord(descIs, descMember.getKey(), mdbDef);
 		} else {
 			descIs.setCoordinate(dim, descMember.getKey());
 		}
 
 		return descIs;
  	}
 
 
 	/**
 	 * Return the descendant intersections at the specified level in the specified
 	 * dimension or the floor, whichever is greater. 
 	 * 
 	 * The original intersection will be returned if it has no descendants along 
 	 * the specified dimension.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension to traverse
 	 * @param level Member level of requested descendants
 	 * 
 	 * @return Descendant intersections
 	 * @throws PafException 
 	 */	
 	public List<Intersection> getDescIntersectionsAtLevel(Intersection cellIs, String dim, int level) {
 		
 		List<Intersection> descendants = new ArrayList<Intersection>();
 		String timeDim = this.getTimeDim(), branch = null;
 		PafDimTree dimTree = null;
 		MdbDef mdbDef = this.getMdbDef();
 
 		
 		// If time dimension is selected, use time horizon dimension for traversal
 		if (dim.equals(getTimeDim())) {
 			dimTree = getDimTrees().getTree(getTimeHorizonDim());
 			branch = TimeSlice.buildTimeHorizonCoord(cellIs, mdbDef);
 		} else {
 			dimTree = getDimTrees().getTree(dim);
 			branch = cellIs.getCoordinate(axisIndexMap.get(dim));
 		}
 		
 		// If the ancestor member is a leaf node then just return the original
 		// intersection.
 		if (dimTree.isLeaf(branch)) {
 			descendants.add(cellIs);
 			return descendants;
 		}
 		
 		// Get descendants members at specified level and use to clone all the 
 		// descendant intersections.
 		List<PafDimMember> descMembers = dimTree.getMembersAtLevel(branch, level);
 		for (PafDimMember descMember : descMembers) {
 			Intersection descIs = cellIs.clone();
 			if (dim.equals(timeDim)) {
 				TimeSlice.applyTimeHorizonCoord(descIs, descMember.getKey(), mdbDef);
 			} else {
 				descIs.setCoordinate(dim, descMember.getKey());
 			}
 			descendants.add(descIs);
 		}
 		
 		return descendants;
 	}
 
 
 	/**
 	 * Return the first floor intersection along the specified dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension
 	 * 
 	 * @return First floor intersection
 	 */
 	public Intersection getFirstFloorIs(final Intersection cellIs, final String dim) {
 
 		Intersection firstFloorIs = cellIs.clone();
 		PafDimTree dimTree = null;
 		PafDimMember firstFloorMbr = null;
 		
 		// If time dimension is selected, substitute time horizon dimension for query
 		if (dim.equals(getTimeDim())) {
 			dimTree = getDimTrees().getTree(getTimeHorizonDim());
 			firstFloorMbr = dimTree.getFirstFloorMbr();
 			TimeSlice.applyTimeHorizonCoord(firstFloorIs, firstFloorMbr.getKey(), this.getMdbDef());
 		} else {
 			dimTree = getDimTrees().getTree(dim);
 			firstFloorMbr = dimTree.getFirstFloorMbr();
 			firstFloorIs.setCoordinate(this.getTimeDim(), firstFloorMbr.getKey());
 		}
 		
 		return firstFloorIs;
 	}
 
 
 	/**
 	 * Return the first floor intersection along the specified dimension, 
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension
 	 * @param genLevelType Generation or Level Optional parameter that specifies the dimension branch to confine search to
 	 * @param genLevel Generation/level Optional parameter that specifies the dimension branch to confine search to
 	 * @param yearMbr Optional parameter that specifies which year to confine search to (ignored if 'dim' is not the Time dimension)
 	 * 
 	 * @return First floor intersection
 	 * @throws PafException 
 	 */
 	public Intersection getFirstFloorIs(Intersection cellIs, String dim, LevelGenType levelGenType, int levelGen, String yearMbr) throws PafException {
 
 		Intersection firstFloorIs = cellIs.clone();
 		PafDimTree dimTree = null;
 		PafDimMember firstFloorMbr = null, timeMbr = null;
 		String timeCoord = null, treeRoot = null;
 		int genOffset = 0;
 		
 		
 		// If time dimension is selected, substitute time horizon dimension for query
 		if (dim.equals(getTimeDim())) {
 			dimTree = getDimTrees().getTree(getTimeHorizonDim());
 			timeCoord = IntersectionUtil.getIsCoord(cellIs, dim, this);
 
 			// For generation search, we need to match desired gen to time horizon tree
 			if (levelGenType == LevelGenType.GEN) {
 				genOffset = getTimeHorizGenOffset();
 			}
 
 			// Check if year member is specified (only valid if Time dimension is being traversed)
 			if (yearMbr == null) {
 				// No year member specification
 				treeRoot = dimTree.getRootNode().getKey();	
 			} else {
 				// Check if current time coordinate is contained in selected year. If not,
 				// return null
 				TimeSlice timeSlice = new TimeSlice(timeCoord);
 				String yearCoord = timeSlice.getYear();
 				if (!yearCoord.equals(yearMbr)) {
 					return null;
 				}
 				// Year member specified - use a pruned copy of time horizon tree under specified year
 				PafDimTree timeTree = getDimTrees().getTree(getTimeDim());
 				treeRoot = TimeSlice.buildTimeHorizonCoord(timeTree.getRootNode().getKey(), yearMbr);
 				dimTree = dimTree.getSubTreeCopy(treeRoot);
 			}
 			
 		} else {
 			dimTree = getDimTrees().getTree(dim);
 			treeRoot = dimTree.getRootNode().getKey();
 			timeCoord = cellIs.getCoordinate(dim);
 		}
 		timeMbr = dimTree.getMember(timeCoord);
 
 		
 		// Find the ancestor that matches the specified scope
 		PafDimMember ancestorMbr = null;
 		ancestorMbr = dimTree.getAncestor(timeMbr, levelGenType, levelGen + genOffset);
 		
 		// Get the first floor descendant within specified scope
 		firstFloorMbr = dimTree.getFirstDescendant(ancestorMbr.getKey());
 		
 		// Update floor intersection and return value
 		IntersectionUtil.setIsCoord(firstFloorIs, dim, firstFloorMbr.getKey(), evalState);	
 		return firstFloorIs;
 	}
 
 	/**
 	 * Return the last floor intersection along the specified dimension
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension
 	 * 
 	 * @return Last floor intersection
 	 */
 	public Intersection getLastFloorIs(Intersection cellIs, String dim) {
 
 		Intersection lastFloorIs = cellIs.clone();
 		PafDimTree dimTree = null;
 		PafDimMember lastFloorMbr = null;
 		
 		// If time dimension is selected, substitute time horizon dimension for query
 		if (dim.equals(getTimeDim())) {
 			dimTree = getDimTrees().getTree(getTimeHorizonDim());
 			lastFloorMbr = dimTree.getLastFloorMbr();
 			TimeSlice.applyTimeHorizonCoord(lastFloorIs, lastFloorMbr.getKey(), this.getMdbDef());
 		} else {
 			dimTree = getDimTrees().getTree(dim);
 			lastFloorMbr = dimTree.getLastFloorMbr();
 			lastFloorIs.setCoordinate(this.getTimeDim(), lastFloorMbr.getKey());
 		}
 		
 		return lastFloorIs;
 
 	}
 
 
 	/**
 	 * Returns the value of specified cell intersection property
 	 * 
 	 * @param intersection Cell intersection
 	 * @param propertyType Cell property type
 	 * 
 	 * @return Cell property value
 	 * @throws PafException 
 	 */
 	private Object getCellProperty(Intersection intersection, CellPropertyType propertyType) throws PafException {
 		
 		Object value = null;
 		
 		DataCacheCellAddress cellAddress = generateCellAddress(intersection);
 		if (isExistingDataBlock(cellAddress.getDataBlockKey())) {
 			// Get intersection's property value
 			DataBlock dataBlock = getDataBlock(cellAddress.getDataBlockKey()).getDataBlock();
 			value = dataBlock.getCellProperty(propertyType, cellAddress);
 		} else {
 			if (isValidIntersection(intersection)) {
 				// Intersection doesn't exist - return default property value
 				value = propertyType.getDefaultValue();
 			} else {
 				// Invalid intersection - throw error
 				String errMsg = "Data Cache error - Unable to get data cache property value ["
 					+ propertyType.name() + "]for invalid intersection: "
 					+ StringUtils.arrayToString(intersection.getCoordinates());
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg); 
 			}
 		}
 		return value;
 	}
 
 	/**
 	 *	Set the property value for the specified data cache cell
 	 *
 	 * @param intersection Cell intersection
 	 * @param propertyType Cell property type
 	 * @param value Cell property value
 	 * 
 	 * @throws PafException 
 	 */
 	private void setCellProperty(Intersection intersection, CellPropertyType propertyType, Object value) throws PafException {
 		
 		// Add intersection if it doesn't already exist
 		DataCacheCellAddress cellAddress = addCell(intersection);
 		
 		// Update cell property value
 		DataBlock dataBlock = getDataBlock(cellAddress.getDataBlockKey()).getDataBlock();
 		dataBlock.setCellProperty(CellPropertyType.Empty, cellAddress, value);
 	}
 
 	/**
 	 *	Set the property values for the specified list of data cache cells
 	 *
 	 * @param intersections Cell intersections
 	 * @param propertyType Cell property type
 	 * @param value Cell property values
 	 * 
 	 * @throws PafException 
 	 */
 	private void setCellsProperty(List<Intersection> intersections, CellPropertyType propertyType, List<Object> values) throws PafException {
 		
 		// Verify that intersections and values lists are the same size
 		if (intersections.size() != values.size()) {
 			String errMsg = "Data Cache setCellsProperty error - Intersections and Values lists are not the same size";
 			logger.error(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		// Set the property of each specified cell
 		for (int i = 0; i < intersections.size(); i++) {
 			setCellProperty(intersections.get(i), propertyType, values.get(i));
 		}
 	}
 
 
 	/**
 	 *	Add a new cell intersection to the data cache
 	 *
 	 * @param intersection Cell intersection
 	 * @return DataCacheCellAddress Cell's internal address
 	 * 
 	 * @throws PafException 
 	 */
 	public DataCacheCellAddress addCell(Intersection intersection) throws PafException {
 		
 		// Validate intersection
 		if (!isValidIntersection(intersection)) {
 			// Invalid intersection - throw error
 			String errMsg = "Data Cache error - Unable to set data cache cell value for invalid intersection ["
 				+ StringUtils.arrayToString(intersection.getCoordinates()) + "]";
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		// Get the cell address of the specified intersection
 		DataCacheCellAddress cellAddress = generateCellAddress(intersection);
 		
 		// Add data block if it doesn't already exist
 		addDataBlock(cellAddress.getDataBlockKey());
 		
 		// Return cell address
 		return cellAddress;
 	}
 	
 	
 	/**
 	 *	Add a new data block to the data cache.
 	 *
 	 * @param key Data block key
 	 * 
 	 * @return DataBlockResponse Data block response
 	 * @throws PafException 
 	 */
 	private DataBlockResponse addDataBlock(Intersection key) throws PafException {
 
 		int surrogateKey = 0;
 		DataBlock dataBlock = null;
 		DataBlockResponse dataBlockResp = null;
 		
 		
 		// If the data block already exists, jut return its
 		// information
 		dataBlockResp = getDataBlock(key);
 		dataBlock = dataBlockResp.getDataBlock();
 		if (dataBlock != null) {
 			return dataBlockResp;
 		}
 		
 		
 		// Ensure that data block being added is valid
 		if (!isValidDataBlock(key)) {
 			String msg = "An attempt was made to add the invalid data block: "
 				+ key.toString() + " to the data cache.";
 			logger.error(msg);
 			throw new IllegalArgumentException(msg);
 		}
 		
 		// If alias data block key, add key to index. Also check for
 		// corresponding primary data block. If the primary data
 		// block doesn't already exists, then create it.
 		List<AliasIntersectionType> aliasTypeProps = new ArrayList<AliasIntersectionType>();
 		if (isAliasDataBlockKey(key, aliasTypeProps)) {
 			Intersection primaryKey = generatePrimaryKey(key, aliasTypeProps);
 			DataBlockResponse aliasDataBlockResp =  getDataBlock(primaryKey);
 			if (aliasDataBlockResp.getDataBlock() == null) {
 				aliasDataBlockResp = addDataBlock(primaryKey);
 				surrogateKey = addDataBlock(primaryKey).getSurrogateKey();
 			}
 			surrogateKey = aliasDataBlockResp.getSurrogateKey();
 			dataBlockIndexMap.put(key, surrogateKey);
 			addAliasKey(key, primaryKey); 
 			return aliasDataBlockResp;
 		}
 		
 		// Create new data block
 		dataBlock = new DataBlock(this.getMeasureSize(), this.getTimeSize(), this.cellPropsBitCount);
 
 		// Add data block to pool. Attempt to reuse any previously deleted blocks.
 		if (deletedBlockIndexes.size() == 0) {
 			// Add index entry for new data block (index is auto-incremented)
 			surrogateKey = dataBlockCount;
 			dataBlockPool.add(dataBlock);
 		} else {
 			// Reuse index of a deleted block
 			surrogateKey = deletedBlockIndexes.removeLast();
 			dataBlockPool.set(surrogateKey, dataBlock);
 		}
 		dataBlockIndexMap.put(key, surrogateKey);
 		dataBlockCount++;
 		
 		// Add data block key to lookup collections
 		addDataBlockKey(key);
 		
 		// Return data block response
 		dataBlockResp = new DataBlockResponse();
 		dataBlockResp.setSurrogateKey(surrogateKey);
 		dataBlockResp.setDataBlock(dataBlock);
 		return dataBlockResp;
 	}
 
 
 	/**
 	 * 	Add alias data block key to lookup collections
 	 * 
 	 * @param aliasKey Alias data block key
 	 * @param primaryKey Corresponding primary data block key
 	 */
 	private void addAliasKey(Intersection aliasKey, Intersection primaryKey) {
 		
 		// Add new alias key lookup entry
 		Set<Intersection> aliasKeys = aliasKeyLookup.get(primaryKey);
 		if (aliasKeys == null) {
 			aliasKeys = new HashSet<Intersection>();
 		}
 		aliasKeys.add(aliasKey);
 		aliasKeyLookup.put(primaryKey, aliasKeys);
 
 
 		// Add new primary key lookup entry
 		primaryKeyLookup.put(aliasKey, primaryKey);
 	}
 
 
 	/**
 	 * Convert an alias data block key to its corresponding primary 
 	 * data block key. 
 	 *  
 	 * @param aliasKey Alias data block key
 	 * @param aliasTypeProps Alias intersection type properties
 	 * 
 	 * @return Primary data block key.
 	 * @throws PafException 
 	 */
 	private Intersection generatePrimaryKey(Intersection aliasKey, List<AliasIntersectionType> aliasTypeProps) throws PafException {
 
 		Intersection primaryKey = aliasKey;
 
 		// If the key is an attribute alias, strip off non-core dimension elements. The non-core
 		// dimension elements will always appear after the core dimension elements.
 		if (aliasTypeProps.contains(AliasIntersectionType.BASE_IS)) {
 			primaryKey = aliasKey.createSubIntersection(coreKeyDims.length);
 		}
 
 		return primaryKey;
 	}
 		
 
 	
 	/**
 	 * Builds the primary intersection corresponding to the specified alias 
 	 * intersection
 	 * 
 	 * @param aliasIs Alias intersection
 	 * @return Corresponding primary intersection
 	 * 
 	 * @throws PafException 
 	 */
 	public Intersection generatePrimaryIntersection(Intersection aliasIs, List<AliasIntersectionType> aliasTypeProps) throws PafException {
 		
 		Intersection primaryIs = aliasIs;
 
 		// If the intersection is an attribute alias, strip off non-core dimension elements. The 
 		// non-core dimension elements will always appear after the core dimension elements.
 		if (aliasTypeProps.contains(AliasIntersectionType.BASE_IS)) {
 			primaryIs = aliasIs.createSubIntersection(coreDimensions.size());
 		}
 		
 		return primaryIs;
 	}
 
 	
 	/**
 	 *  Add data block key to lookup collections
 	 *  
 	 * @param key Data block key
 	 */
 	private void addDataBlockKey(Intersection key) {
 	
 		// Add data block key to version collection
 		String version = key.getCoordinate(getVersionDim());
 		Set<Intersection> versionBlocks = dataBlocksByVersion.get(version);
 		if (versionBlocks != null) {
 			versionBlocks.add(key);
 		} else {
 			versionBlocks = new HashSet<Intersection>();
 			versionBlocks.add(key);
 			dataBlocksByVersion.put(version, versionBlocks);
 		}	
 	}
 
 
 	/**
 	 *  Add empty mdb block key to lookup collections
 	 *  
 	 * @param key Data block key
 	 */
 	public void addEmptyMdbBlock(Intersection key) {
 	
 		// Add empty mdb block key to collections
 		emptyMdbBlocks.add(key);
 		String version = key.getCoordinate(getVersionDim());
 		Set<Intersection> versionBlocks = emptyMdbBlocksByVersion.get(version);
 		if (versionBlocks != null) {
 			versionBlocks.add(key);
 		} else {
 			versionBlocks = new HashSet<Intersection>();
 			versionBlocks.add(key);
 			emptyMdbBlocksByVersion.put(version, versionBlocks);
 		}	
 	}
 
 
 	/**
 	 *  Add empty mdb block keys to lookup collections
 	 *  
 	 * @param keys collection of data block keys
 	 */
 	public void addEmptyMdbBlocks(Collection<Intersection> keys) {
 		for (Intersection key : keys) {
 			addEmptyMdbBlock(key);
 		}
 	}
 
 
 	/**
 	 *	Delete a data block from the data cache.
 	 *
 	 * @param key Data block key	
 	 * @throws PafException 
 	 */
 	private void deleteDataBlock(Intersection key) throws PafException {
 		
 		// Retrieve the data block being deleted
 		DataBlockResponse dataBlockResp = getDataBlock(key);
 		DataBlock dataBlock = dataBlockResp.getDataBlock();
 		
 		if (dataBlock == null) {
 			String errMsg = "Data Cache Error - block deletion error - a data block with a key of [" 
 				+ key + "] does not exist";
 			logger.error(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 
 		// If this is an alias data block key, remove key entry
 		// from data block index and exit.
 		if (isAliasDataBlockKey(key)) {
 			dataBlockIndexMap.remove(key);
 			return;
 		}
 		
 		// Remove data block key from index collections
 		deleteDataBlockKey(key);		
 		dataBlockCount--;
 
 		// Get the block's surrogate key against the data block pool
 		int surrogateKey = dataBlockResp.getSurrogateKey();
 
 		// Initialized deleted data block
 		dataBlockPool.set(surrogateKey, null);
 		
 		// Add index of deleted block to collection so that 
 		// the block can be reused for a future data block addition. 
 		deletedBlockIndexes.add(surrogateKey);
 		
 	}
 
 	/**
 	 *	Delete a snapshot data block from the data cache.
 	 *
 	 * @param key Data block key	
 	 * @throws PafException 
 	 */
 	private void deleteSnapshotDataBlock(Intersection key) throws PafException {
 		
 		// Retrieve the data block being deleted
 		DataBlockResponse dataBlockResp = getSnapshotDataBlock(key);
 		DataBlock dataBlock = dataBlockResp.getDataBlock();
 		
 		if (dataBlock == null) {
 			String errMsg = "Data Cache Error - block deletion error - a snapshot data block with a key of [" 
 				+ key + "] does not exist";
 			logger.error(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 
 //		// If this is an alias data block key, remove key entry
 //		// from data block index and exit.
 //		if (isAliasDataBlockKey(key)) {
 //			dataBlockIndexMap.remove(key);
 //			return;
 //		}
 		
 		// Remove data block key from index collections
 //		deleteDataBlockKey(key);		
 		snapshotBlockCount--;
 
 		// Get the block's surrogate key against the data block pool
 		int surrogateKey = dataBlockResp.getSurrogateKey();
 
 		// Initialized deleted data block
 		snapshotPool.set(surrogateKey, null);
 		
 		// Add index of deleted block to collection so that 
 		// the block can be reused for a future data block addition. 
 		deletedSnapshotIndexes.add(surrogateKey);
 		
 	}
 
 
 
 	/**
 	 *	Remove data block key from lookup collections
 	 *
 	 * @param key Data block key
 	 */
 	private void deleteDataBlockKey(Intersection key) {
 
 		// This method is only called on the primary data
 		// block key.
 		
 		
 		// Remove key from main data block lookup
 		dataBlockIndexMap.remove(key);
 
 		// Remove key from data block by version collection
 		Set<Intersection> versionBlocks = dataBlocksByVersion.get(key.getCoordinate(getVersionDim()));
 		if (versionBlocks != null) {
 			versionBlocks.remove(key);
 		}
 
 		// Remove any associated alias key entries
 		Set<Intersection> aliasKeys = aliasKeyLookup.remove(key);
 		if (aliasKeys != null) {
 			for (Intersection aliasKey : aliasKeys) {
 				dataBlockIndexMap.remove(aliasKey);
 				primaryKeyLookup.remove(aliasKey);
 			}
 		}
 				
 	}
 
 
 	/**
 	 *	Remove empty mdb block key from lookup collections
 	 *
 	 * @param key Data block key
 	 */
 	private void deleteEmptyMdbBlock(Intersection key) {
 
 		// Remove key from main data block lookup
 		emptyMdbBlocks.remove(key);
 
 		// Remove key from data block by version collection
 		Set<Intersection> versionBlocks = emptyMdbBlocksByVersion.get(key.getCoordinate(getVersionDim()));
 		if (versionBlocks != null) {
 			versionBlocks.remove(key);
 		}
 
 	}
 		
 	/**
 	 *	Remove empty mdb block keys from lookup collections
 	 *
 	 * @param keys Collection of data block key
 	 */
 	private void deleteEmptyMdbBlocks(Collection<Intersection> keys) {
 		for (Intersection key : keys) {
 			deleteEmptyMdbBlock(key);
 		}
 	}
 		
 
 	/**
 	 * Returns true if the data block contains attribute intersections
 	 * 
 	 * @param key Data block key
 	 * @return True if the data block contains attribute intersections
 	 */
 	public boolean isAttributeDataBlock(Intersection key) {
 		
 		boolean isAttributeDb = false;
 		
 		// Simple check - a data block key is comprised of attribute
 		// components if it contains any non-core key dimensions
 		if (key.getSize() > this.coreKeyDims.length) {
 			isAttributeDb = true;
 		}
 		return isAttributeDb;
 	}
 
 	/**
 	 * Returns true if the intersection in an attribute intersection
 	 * 
 	 * @param intersection Cell intersection
 	 * @return True if the intersection is an attribute intersection 
 	 */
 	public boolean isAttributeIntersection(Intersection intersection) {
 		
 		boolean isAttributeIs = false;
 		
 		// Simple check - an intersection is an attribute intersection,
 		// if it contains any non-core dimensions
 		if (intersection.getSize() > this.coreDimensions.size()) {
 			isAttributeIs = true;
 		}
 		return isAttributeIs;
 	}
 
 	/**
 	 * Returns true if the intersection in an attribute intersection
 	 * 
 	 * @param coords Cell intersection coordinates
 	 * @return True if the intersection is an attribute intersection 
 	 */
 	public boolean isAttributeIntersection(String[] coords) {
 		
 		boolean isAttributeIs = false;
 		
 		// Simple check - an intersection is an attribute intersection,
 		// if it contains any non-core dimensions
 		if (coords.length > this.coreDimensions.size()) {
 			isAttributeIs = true;
 		}
 		return isAttributeIs;
 	}
 
 	/**
 	 * Returns true if the intersection in an attribute intersection
 	 * 
 	 * @param intersection Cell intersection
 	 * @return True if the intersection is an attribute intersection 
 	 */
 	public boolean isAttributeIntersection(SimpleCoordList intersection) {
 		
 		boolean isAttributeIs = false;
 		
 		// Simple check - an intersection is an attribute intersection,
 		// if it contains any non-core dimensions
 		if (intersection.getAxis().length > this.coreDimensions.size()) {
 			isAttributeIs = true;
 		}
 		return isAttributeIs;
 	}
 
 	/**
 	 * Returns true if the intersection in not an attribute intersection
 	 * 
 	 * @param intersection Cell intersection
 	 * @return True if the intersection is not an attribute intersection 
 	 */
 	public boolean isBaseIntersection(Intersection intersection) {
 		return !isAttributeIntersection(intersection);
 	}
 
 	/**
 	 * Returns true if the intersection coordinates do not represent an 
 	 * attribute intersection
 	 * 
 	 * @param coords Cell intersection coordinates
 	 * @return True if the intersection is not an attribute intersection 
 	 */
 	public boolean isBaseIntersection(String[] coords) {
 		return !isAttributeIntersection(coords);
 	}
 
 	/**
 	 * Returns true if the intersection in not an attribute intersection
 	 * 
 	 * @param intersection Cell intersection
 	 * @return True if the intersection is not an attribute intersection 
 	 */
 	public boolean isBaseIntersection(SimpleCoordList intersection) {
 		return !isAttributeIntersection(intersection);
 	}
 
 	
 	/**
 	 * Returns true if the data block key is an alias key. 
 	 * 
 	 * A key is considered to be an alias key (can be
 	 * directly mapped to a primary data block key) if
 	 * it meets at least one of the following conditions:
 	 * 
 	 * 1) Attribute Alias - The key contains one of more 
 	 *    attribute member coordinates, all of which are 
 	 *    at level 0, and all of which are associated with 
 	 *    base members at the attribute mapping level.
 	 * 2) Offset Version Alias - The key's Version dimension
 	 *    coordinate contains an offset version reference 
 	 *    whose source year falls inside the UOW.
 	 * 
 	 * @param key Data block key
 	 * @param aliasTypeProps Alias intersection type properties
 	 *
 	 * @return boolean	  
 	 * @throws PafException 
 	 */
 	private boolean isAliasDataBlockKey(Intersection key) throws PafException {
 		return isAliasDataBlockKey(key, null);
 		
 	}
 
 	/**
 	 * Returns true if the data block key is an alias key. 
 	 * 
 	 * @param key Data block key
 	 * @param aliasTypeProps Alias intersection type properties
 	 *
 	 * @return boolean	  
 	 * @throws PafException 
 	 */
 	private boolean isAliasDataBlockKey(Intersection key, List<AliasIntersectionType> aliasTypeProps) throws PafException {
 		
 		boolean isAliasKey = false;
 
 		// Check if the key is an alias of a base intersection
 		if (primaryKeyLookup.containsKey(key) || isBaseDataBlockKeyAlias(key)) {
 			if (aliasTypeProps != null) {
 				aliasTypeProps.add(AliasIntersectionType.BASE_IS);
 			}
 			isAliasKey = true;	
 		}
 		
 		// Return status
 		return isAliasKey;
 	}
 
 
 	/**
 	 * Returns true if the data block key is a primary key
 	 *
 	 * @param dataBlockKey Data block key
 	 * 
 	 * @return True if the data block key is a primary key
 	 * @throws PafException
 	 */
 	private boolean isPrimaryDataBlockKey(Intersection dataBlockKey) throws PafException {
 		return isPrimaryDataBlockKey(dataBlockKey, null);
 	}
 
 	/**
 	 * Returns true if the data block key is a primary key
 	 *
 	 * @param dataBlockKey Data block key
 	 * @param aliasTypeProps Collection that will pass back any matched data block key alias type properties (optional parameter)
 	 * 
 	 * @return True if the data block key is a primary key
 	 * @throws PafException
 	 */
 	private boolean isPrimaryDataBlockKey(Intersection dataBlockKey, List<AliasIntersectionType> aliasTypeProps) throws PafException {
 		return !isAliasDataBlockKey(dataBlockKey, aliasTypeProps);
 	}
 
 
 	/**
 	 * Returns true if the intersection in an alias of a primary
 	 * data cache intersection
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param aliasTypeProps Alias intersection type properties
 
 	 * @return True if the intersection is an alias of a primary intersection 
 	 * 
 	 * @throws PafException 
 	 */
 	public boolean isAliasIntersection(Intersection cellIs, List<AliasIntersectionType> aliasTypeProps) throws PafException {
 
 		boolean isAliasIs = false;
 
 		// Check if the key is an alias of a base intersection
 		if (this.isBaseIntersectionAlias(cellIs)) {
 			if (aliasTypeProps != null) {
 				aliasTypeProps.add(AliasIntersectionType.BASE_IS);
 			}
 			isAliasIs = true;	
 		}
 		
 		// Return status
 		return isAliasIs;
 		
 	}
 	
 	/**
 	 * Determines if the supplied intersection is an alias
 	 * of a base intersection.
 	 *   
 	 * @param intersection Cell intersection
 	 * @return True if the intersection is an base intersection alias
 	 */
 	protected boolean isBaseIntersectionAlias(Intersection intersection) {
 		// The intersection is an alias it its underlying data block key
 		// is an alias.
 		return isBaseDataBlockKeyAlias(this.generateDataBlockKey(intersection));
 	}
 
 	/**
 	 * Determines if the data block key is an alias of another data 
 	 * block key that points to a data block that holds base 
 	 * intersections, versus one that hold attribute intersections.
 	 * 
 	 * A data block key is considered to be an alias of a base data 
 	 * block key if it meets the following conditions:
 	 * 
 	 * 1) The data block key contains one or more attribute member
 	 * 	  coordinates
 	 * 
 	 * 2) All attribute members are at level 0
 	 * 
 	 * 3) All attribute members are associated with base members
 	 *    at the attribute mapping level.
 	 *    
 	 *    
 	 * @param dataBlockKey Data block key
 	 * @return True if the intersection is an attribute alias intersection
 	 */
 	private boolean isBaseDataBlockKeyAlias(Intersection dataBlockKey) {
 
 			final int coreKeyDimCount = coreKeyDims.length;
 			boolean isBaseDbKeyAlias = false;
 
 
 			if (isAttributeDataBlock(dataBlockKey)) {
 
 				// The intersection contains one or more attribute dimension 
 				// coordinates, which need to be analyzed. These attribute 
 				// dimension coordinates, when they exist, always appear 
 				// after all the core dimension coordinates.
 				isBaseDbKeyAlias = true;
 				validation:
 					for (int index = coreKeyDimCount; index < dataBlockKey.getSize(); index++) {
 
 						String attrDim = dataBlockKey.getDimensions()[index];
 						String attrMember = dataBlockKey.getCoordinate(attrDim);
 
 						// Check level of attribute coordinate member - if not at level 0,
 						// then this key is a not an attribute alias.
 						PafAttributeTree attrDimTree = (PafAttributeTree) dimTrees.getTree(attrDim);
 						int attrMbrLevel = attrDimTree.getMember(attrMember).getMemberProps().getLevelNumber();
 						if (attrMbrLevel > 0) {
 							isBaseDbKeyAlias = false;
 							break validation;
 						}
 
 						// Check level of associated base member - if above attribute mapping level,
 						// then the key is not an attribute alias.
 						String assocBaseDim = attrDimTree.getBaseDimName();
 						PafBaseTree baseDimTree = (PafBaseTree) dimTrees.getTree(assocBaseDim);
 						String baseMember = dataBlockKey.getCoordinate(assocBaseDim);
 						int baseMbrLevel = baseDimTree.getMember(baseMember).getMemberProps().getLevelNumber();
 						if (baseMbrLevel > baseDimTree.getAttributeMappingLevel(attrDim)) {
 							isBaseDbKeyAlias = false;
 							break validation;
 						}
 					}
 			}
 
 		return isBaseDbKeyAlias;
 	}
 
 
 
 	/**
 	 *  Remove data blocks for the specified versions. If the version
 	 *  filter is empty or null then no data will be removed.
 	 *  
 	 * @param versionFilter Specifies the versions to clear
 	 * @throws PafException 
 	 */
 	public int clearVersionData(List<String> versionFilter) throws PafException {
 
 		long startTime = System.currentTimeMillis();
 		int deletedBlockCount = 0;
 		if (versionFilter != null && !versionFilter.isEmpty()) {
 			for (String version : versionFilter) {
 				
 				// Delete data blocks
 				List<Intersection> dataBlockKeys = null;
 				if (dataBlocksByVersion.get(version) !=null ) {
 					dataBlockKeys = new ArrayList<Intersection>(dataBlocksByVersion.get(version));
 					for (Intersection key : dataBlockKeys) {
 						deleteDataBlock(key);
 						deletedBlockCount++;
 					}
 				}
 				
 				// Delete list of empty mdb blocks (TTN-1860)
 				if (emptyMdbBlocksByVersion.get(version) != null) {
 					dataBlockKeys = new ArrayList<Intersection>(emptyMdbBlocksByVersion.get(version));
 					if (dataBlockKeys != null) {
 						deleteEmptyMdbBlocks(dataBlockKeys);
 					}
 				}
 			}
 		}
 		
 		String stepDesc = StringUtils.commaFormat(deletedBlockCount) 
 				+ " data block(s) removed from the data cache while initializing the following version(s): " 
 				+  StringUtils.arrayListToString(versionFilter);
 		logger.info(stepDesc);
 		stepDesc = "Removal of " + StringUtils.commaFormat(deletedBlockCount) + " data block(s)";
 		performanceLogger.info(LogUtil.timedStep(stepDesc, startTime));
 		return deletedBlockCount;
 		
 	}
 
 
 	/**
 	 *	Return the data block corresponding to the specified intersection.
 	 *  Additional information is also returned.
 	 *  
 	 *  If the data block is not found, then the returned data block will
 	 *  be a null value.
 	 *   
 	 * @param intersection Data block key
 	 * 
 	 * @return DataBlockResponse
 	 */
 	private DataBlockResponse getDataBlock(Intersection dataBlockKey) {
 		
 		DataBlock dataBlock = null;
 		
 		// Find data block index entry
 		Integer  surrogateKey = dataBlockIndexMap.get(dataBlockKey);
 		
 		// Get data block (if an index entry exists)
 		if (surrogateKey != null) {
 			dataBlock = dataBlockPool.get(surrogateKey);
 			if (dataBlock == null) {
 				// Index entry was found but data block is missing
 				String errMsg = "Data Cache Error - Missing data block at intersection: " 
 					+ StringUtils.arrayToString(dataBlockKey.getCoordinates());
 				throw new IllegalArgumentException(errMsg);
 			}
 		}
 		
 		// Return data block response
 		DataBlockResponse dataBlockResp = new DataBlockResponse();
 		dataBlockResp.setSurrogateKey(surrogateKey);
 		dataBlockResp.setDataBlock(dataBlock);
 		return dataBlockResp;
 	}
 
 
 	/**
 	 *	Return the "snapshot data block corresponding to the specified intersection.
 	 *  Additional information is also returned.
 	 *  
 	 *  If the data block is not found, then the returned data block will
 	 *  be a null value.
 	 *   
 	 * @param intersection Data block key
 	 * 
 	 * @return DataBlockResponse
 	 */
 	private DataBlockResponse getSnapshotDataBlock(Intersection dataBlockKey) {
 		
 		DataBlock dataBlock = null;
 		
 		// Find data block index entry
 		Integer  surrogateKey = snapshotIndexMap.get(dataBlockKey);
 		
 		// Get data block (if an index entry exists)
 		if (surrogateKey != null) {
 			dataBlock = snapshotPool.get(surrogateKey);
 			if (dataBlock == null) {
 				// Index entry was found but data block is missing
 				String errMsg = "Data Cache Error - Missing snapshot data block at intersection: " 
 					+ StringUtils.arrayToString(dataBlockKey.getCoordinates());
 				throw new IllegalArgumentException(errMsg);
 			}
 		}
 		
 		// Return data block response
 		DataBlockResponse dataBlockResp = new DataBlockResponse();
 		dataBlockResp.setSurrogateKey(surrogateKey);
 		dataBlockResp.setDataBlock(dataBlock);
 		return dataBlockResp;
 	}
 
 
 	/**
 	 * 	Update the contents of the specified data block
 	 * 
 	 * @param dataBlockResp Data Block Response Object
 	 */
 	private void updateDataBlock(DataBlockResponse dataBlockResp) {
 		dataBlockPool.set(dataBlockResp.getSurrogateKey(), dataBlockResp.getDataBlock());
 	}
 
 
 	/**
 	 * 	Update the contents of the specified data block
 	 * 
 	 * @param dataBlockKey Data Block Key
 	 * @param dataBlock Data Block
 	 */
 	private void updateDataBlock(Intersection dataBlockKey, DataBlock dataBlock) {
 		
 		Integer  surrogateKey = dataBlockIndexMap.get(dataBlockKey);	
 		dataBlockPool.set(surrogateKey, dataBlock);
 		
 	}
 
 
 	/**
 	 * 	Update the data cache with the contents of the data slice
 	 * 
 	 * @param pafDataSlice Paf Data Slice
 	 * @param parms Object containing required PafDataSlice parameters
 	 * 
 	 * @throws PafException
 	 */
 	public void updateDataCache(PafDataSlice pafDataSlice, PafDataSliceParms parms) throws PafException {
 
 		boolean hasPageDimensions = false;
 		int cols = 0, rows = 0;
 		double[] dataSlice = null;
 		String[] rowDims = parms.getRowDimensions(), colDims = parms.getColDimensions();
 		String[][] rowTuples = parms.getRowTuples(), colTuples = parms.getColTuples();
 		String [] attributeDims = parms.getAttributeDims();
 		boolean hasAttributes = false;
 		Map<Intersection, Double> origCellValueMap = null;
 
 		
 		try {
 			// Validate data slice parms
 			logger.info("Validating PafDataSlice parameters...");
 			hasPageDimensions = validateDataSliceParms(parms);
 			if (attributeDims != null && attributeDims.length > 0) {
 				hasAttributes = true;
 			}
 
 			// Getting data slice array 
 			logger.info("Getting data slice array");
 			cols = pafDataSlice.getColumnCount();
 			dataSlice = pafDataSlice.getData();
 			rows = pafDataSlice.getRowCount();
 
 			// Create reusable cell intersection that will to access
 			// data cache data. This intersection will get updated as
 			// we iterate through all the tuple members
 			Intersection cellIs = new Intersection(parms.getDimSequence());
 			
 			
 			// Enter page headers into appropriate elements of the data  
 			// cache cell intersection 
 			if (hasPageDimensions) {
 				logger.debug("Entering page headers into cell intersection");
 				for (int i = 0; i < parms.getPageDimensions().length; i++) {
 					cellIs.setCoordinate(parms.getPageDimensions()[i], parms.getPageMembers()[i]);
 				}
 			}
 
 			// Load data slice. Start by cycling through row tuples
 			logger.info("Updating data cache with data slice - rows: " + rows + " columns: " + cols + " cells: " + dataSlice.length);  	
 			int sliceIndex = 0;
 			for (String[] rowTuple:rowTuples) {
 
 				// Updated the cell intersection with the current row header members
 				for (int i = 0; i < rowDims.length; i++) {
 					cellIs.setCoordinate(rowDims[i], rowTuple[i]);
 				}
 
 				// Cycle through column tuples
 				for (String[] colTuple:colTuples) {
 
 					// Update the cell intersection with the current column header
 					// members 
 					for (int i = 0; i < colDims.length; i++) {
 						cellIs.setCoordinate(colDims[i], colTuple[i]);
 					}
 
 					// Copy current data slice cell to data cache, skipping any
 					// elapsed period intersections (TTN-1595)
 					if (!hasAttributes || isValidAttributeIntersection(cellIs, attributeDims)) {
 						if (!this.isElapsedIs(cellIs)) {
 
 							// Update data cache
 							setCellValue(cellIs, dataSlice[sliceIndex]);
 
 						}
 					}
 					sliceIndex++;
 				}
 			}
 
 		} catch (PafException pfe) {
 			// throw Paf Exception
 			throw pfe;
 		} catch (Exception ex) {
 			// throw Paf Exception
 			String errMsg = ex.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, ex);	
 			throw pfe;
 		}
 
 	}
 
 
 	/**
 	 * Returns true if the specified intersection is elapsed
 	 * 
 	 * @param cellIs Cell intersection
 	 * @return True is the specified intersection is elapsed
 	 */
 	public boolean isElapsedIs(Intersection cellIs) {
 
 		// First check if the cell is valid along the time horizon (TTN-1858)
 		String year = cellIs.getCoordinates()[this.getYearAxis()];
 		String period = cellIs.getCoordinates()[this.getTimeAxis()];
 		if (this.getLockedPeriods(year).contains(period)) {
 			return true;
 		}
 
 		// For the plan version, this has to be a forward plannable version 
 		// for elapsed period logic to apply
 		String version = cellIs.getCoordinates()[this.getVersionAxis()];
 		if (this.getPlanVersion().equals(version)) {
 			VersionType versionType = getVersionDef(version).getType();
 			if (versionType != VersionType.ForwardPlannable) {
 				return false;
 			}
 		} else if (this.getBaseVersions().contains(version)) {
 			// Reference version - assume all periods are elapsed
 			return true;
 		}
 	
 		// If we get this far, we'll assume that the period is not elapsed.
 		return false;
 	}
 
 	
 
 
 	/**
 	 *	Return the cell address for the specified intersection
 	 *
 	 * @param cellIs Cell intersection 
 	 * @return DataCacheCellAddress
 	 * 
 	 * @throws PafException 
 	 */
 	private DataCacheCellAddress generateCellAddress(final Intersection cellIs) throws PafException {
 		
 		int measureAxis = this.getMeasureAxis();
 		int timeAxis = this.getTimeAxis();
 //		int timeHorizonAxis = this.getTimeHorizonAxis();
 //		MdbDef mdbDef = this.getAppDef().getMdbDef();
 		DataCacheCellAddress cellAddress = new DataCacheCellAddress();
 
 		
 		// Translate offset version alias intersection to primary
 		// data cache intersection
 		Intersection translatedIs = translateOffsetVersionAliasIs(cellIs);
 		
 		// Generate data block key that corresponds to the cell intersection
 		Intersection dataBlockKey = generateDataBlockKey(translatedIs);
 		cellAddress.setDataBlockKey(dataBlockKey);
 		
 		// Set x coordinate (measure)
 		int measureIndex = getMemberIndex(translatedIs.getCoordinates()[measureAxis], measureAxis);
 		cellAddress.setCoordX(measureIndex);
 
 //		// Set y coordinate (time horizon - year/period) (TTN-1595)
 //		String timeHorizonCoord = TimeSlice.buildTimeHorizonCoord(translatedIs, mdbDef);
 //		int timeIndex = getMemberIndex(timeHorizonCoord, timeHorizonAxis);
 //		cellAddress.setCoordY(timeIndex);
 
 		// Set y coordinate (time horizon - year/period) (TTN-1860)
 		int timeIndex = getMemberIndex(translatedIs.getCoordinates()[timeAxis], timeAxis);
 		cellAddress.setCoordY(timeIndex);
 
 		// Return cell address
 		return cellAddress;
 	}
 
 	/**
 	 *	Return the cell address for the specified base intersection coordinates
 	 *
 	 * @param baseCoords Base cell intersection coordinates
 	 * @return DataCacheCellAddress
 	 * 
 	 * @throws PafException 
 	 */
 	private DataCacheCellAddress generateBaseCellAddress(final String[] baseCoords) throws PafException {
 		
 		int measureAxis = this.getMeasureAxis();
 		int timeAxis = this.getTimeAxis();
 		DataCacheCellAddress cellAddress = new DataCacheCellAddress();
 
 		
 		// Translate offset version alias intersection to primary
 		// data cache intersection
 		String[] translatedCoords = translateOffsetVersionAliasIs(baseCoords);
 		
 		// Generate data block key that corresponds to the cell intersection
 		Intersection dataBlockKey = generateDataBlockKey(translatedCoords);
 		cellAddress.setDataBlockKey(dataBlockKey);
 		
 		// Set x coordinate (measure)
 		int measureIndex = getMemberIndex(translatedCoords[measureAxis], measureAxis);
 		cellAddress.setCoordX(measureIndex);
 
 		// Set y coordinate (time horizon - year/period) (TTN-1860)
 		int timeIndex = getMemberIndex(translatedCoords[timeAxis], timeAxis);
 		cellAddress.setCoordY(timeIndex);
 
 		// Return cell address
 		return cellAddress;
 	}
 
 
 	/**
 	 * Translate offset version alias intersection to its source data cache intersection
 	 * 
 	 * An offset version intersection will be treated as an alias of
 	 * another data cache intersection, if it meets the following 
 	 * condition(s):
 	 * 
 	 * 1) The year coordinate member is a leaf node
 	 * 
 	 * 2) The intersection's version dimension coordinate contains an
 	 *    offset version reference whose calculated source year falls
 	 *    inside the UOW.
 	 * 
 	 * Offset version intersections who don't meet this criteria are 
 	 * sourced directly from the multidimensional database.
 	 * 
 	 * 
 	 * @param cellIs A cell intersection
 	 * 
 	 * @return Translated intersection or original intersection, if no translation is needed.
 	 * @throws PafException 
 	 */
 	private Intersection translateOffsetVersionAliasIs(Intersection cellIs) throws PafException {
 
 		// Translate coordinates
 		String[] isCoords = cellIs.getCoordinates();
 		String[] translatedCoords = translateOffsetVersionAliasIs(isCoords);
 		if (isCoords != translatedCoords) {
 			// Return translated intersection
 			Intersection translatedIs = new Intersection(cellIs.getDimensions(), translatedCoords);
 			return translatedIs;
 		} else {
 			// No translation needed - return original intersection
 			return cellIs;
 		}
 		
 	}
 
 	/**
 	 * Translate offset version alias intersection to its source data cache intersection
 	 * 
 	 * An offset version intersection will be treated as an alias of
 	 * another data cache intersection, if it meets the following 
 	 * condition(s):
 	 * 
 	 * 1) The year coordinate member is a leaf node
 	 * 
 	 * 2) The intersection's version dimension coordinate contains an
 	 *    offset version reference whose calculated source year falls
 	 *    inside the UOW.
 	 *  
 	 * Offset version intersections who don't meet this criteria are 
 	 * sourced directly from the multidimensional database.
 	 * 
 	 * 
 	 * @param cellIs A cell intersection's coordinates
 	 * 
 	 * @return Translated coordinates or original coordinates, if no translation is needed.
 	 * @throws PafException 
 	 */
 	private String[] translateOffsetVersionAliasIs(String[] coords) throws PafException {
 
 		final int versionAxis = this.getVersionAxis(), yearAxis = this.getYearAxis();
 		final String planVersion = this.getPlanVersion();
 		
 		
 
 		// Check for offset version reference. If none found then return original
 		// intersection.
 		String version = coords[versionAxis];
 		VersionDef vd = getVersionDef(version);
 		if (vd.getType() != VersionType.Offset) {
 			return coords;
 		}
 		
 		// Also skip translation if the year coordinate is not a leaf member (TTN-2017)
 		List<String> uowLeafYears = this.getLeafYears();
 		String yearCoord = coords[yearAxis];
 		if (!uowLeafYears.contains(yearCoord)) {
 			return coords;
 		}
 
 		// Calculate the offset version source year	
 		VersionFormula vf = vd.getVersionFormula();
 		String sourceYear = null;
 		final List<String> mdbYearList = this.getMdbLeafYears();
 		try {
 			sourceYear = vf.calcOffsetVersionSourceYear(yearCoord, mdbYearList);
 		} catch (Exception e) {
 			String logMsg = "Unable to access Data Dache intersection containing Offset Version: [" + version + "]. " + e.getMessage();
 			throw new PafException(logMsg, PafErrSeverity.Error);					
 		}
 
 		// If source year falls inside the uow, then this intersection is an alias
 		// intersection and points to another intersection within the uow.
 		if (uowLeafYears.contains(sourceYear)) {
 			// Translate intersection coordinates
 			String[] translatedCoords = coords.clone();
 			String sourceVersion = vf.getBaseVersionValue(planVersion);
 			translatedCoords[versionAxis] = sourceVersion;
 			translatedCoords[yearAxis] = sourceYear;
 			return translatedCoords;			
 		} else {
 			// No translation needed - return original intersection
 			return coords;
 		}
 
 	}
 
 
 	/**
 	 * Translates a cell intersection based on a time horizon coordinate to one
 	 * that is based on time and year as separate dimensions.
 	 * 
 	 * If a time horizon coordinate is not found, then the original intersection 
 	 * is returned.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @return Translated intersection
 	 */
 	protected Intersection translateTimeHorizonIs(final Intersection cellIs) {
 	return cellIs;	
 //		Intersection translatedIs = null;
 //		
 //		// If this is a time horizon intersection, translate it to a time-year
 //		// intersection, else return the original intersection.
 //		if (isTimeHorizonIs(cellIs)) {
 //			TimeSlice timeSlice = new TimeSlice(cellIs.getCoordinate(getTimeDim()));
 //			translatedIs = cellIs.clone();
 //			translatedIs.setCoordinate(getTimeDim(), timeSlice.getPeriod());
 //			translatedIs.setCoordinate(getYearDim(), timeSlice.getYear());
 //		} else {
 //			translatedIs = cellIs;
 //		}
 //		return translatedIs;
 	}
 
 	/**
 	 * Translates a cell intersection based on a time and year coordinate to one
 	 * that is based on a time horizon coordinate.
 	 * 
 	 * If a time horizon coordinate is not found, then the original intersection 
 	 * is returned.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @return Translated intersection
 	 */
 	protected Intersection translateTimeYearIs(final Intersection cellIs) {
 		return cellIs;
 //		Intersection translatedIs = null;
 //		
 //		// If this is a time-year intersection, translate it to a time horizon
 //		// intersection, else return the original intersection.
 //		if (!isTimeHorizonIs(cellIs)) {
 //			TimeSlice timeSlice = new TimeSlice(cellIs.getCoordinate(getTimeDim()), cellIs.getCoordinate(getYearDim()));
 //			translatedIs = cellIs.clone();
 //			translatedIs.setCoordinate(getTimeDim(), timeSlice.getTimeHorizonPeriod());
 //			translatedIs.setCoordinate(getYearDim(), TimeSlice.getTimeHorizonYear());
 //		} else {
 //			translatedIs = cellIs;
 //		}
 //		return translatedIs;
 	}
 
 
 	/**
 	 * Checks if the current intersection contains a time horizon coordinate
 	 * 
 	 * @param cellIs Cell intersection
 	 * @return True is the specified intersection containts a time horizon coordinate
 	 */
 	public boolean isTimeHorizonIs(Intersection cellIs) {
 		
 		// An intersection containing a time horizon coordinate will still contain both
 		// time and years dimensions. However, the time dimension coordinate will be a 
 		// member from the time horizon dimension tree, a combined year-time member. 
 		// And the year dimension coordinate will be set to a unique value that indicates
 		// the this is a time horizon intersection.
 		
 		if (cellIs.getCoordinate(getYearAxis()).equals(PafBaseConstants.TIME_HORIZON_DEFAULT_YEAR)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 
 	/**
 	 * 	Calculate the data block key for the specified cell intersection
 	 * 
 	 * @param intersection Cell Intersection
 	 * @return Intersection
 	 */
 	private Intersection generateDataBlockKey(Intersection intersection) {
 		
 		Intersection dataBlockKey = null;
 		
 		// The data block key is calculated by taking the intersection
 		// and removing the elements that correspond to the dimensions
 		// contained in the data block. 
 		//
 		// To reduce overhead, this method accesses a pre-built array 
 		// that contains the desired intersection elements for each
 		// given intersection size.  
 		int[] keyDimIndexes = keyIndexesByIsSize[intersection.getSize()];
 		dataBlockKey = intersection.createSubIntersection(keyDimIndexes);
 		
 		return dataBlockKey;
 	}
 
 	/**
 	 * 	Calculate the data block key for the specified base cell intersection
 	 *  coordinates.
 	 * 
 	 * @param baseCoords Base (non-attribute) intersection coordinates
 	 * @return Intersection
 	 */
 	private Intersection generateDataBlockKey(String[] baseCoords) {
 		
 		Intersection dataBlockKey = null;
 		
 		// The data block key is calculated by taking the intersection
 		// and removing the elements that correspond to the dimensions
 		// contained in the data block. 
 		//
 		// To reduce overhead, this method accesses a pre-built array 
 		// that contains the desired intersection elements for each
 		// given intersection size.  
 		int[] keyDimIndexes = keyIndexesByIsSize[baseCoords.length];
 		String[] coords = new String[coreKeyDims.length];
 		for (int i = 0; i < coreKeyDims.length; i++) {
 			coords[i] = baseCoords[keyDimIndexes[i]];
 		}
 		dataBlockKey = new Intersection(coreKeyDims, coords);
 		
 		return dataBlockKey;
 	}
 
 
 	/**
 	 * 	Filter out loaded reference data intersections from data specification
 	 *  map
 	 *  
 	 * @param memberSpecByAxis Defines a subset of reference data in the data cache 
 	 * @param dataBlocksToLoad Used to return the list of data block keys that will be loaded
 	 * @param dimTrees Uow dimension trees
 	 * 
 	 * @return
 	 */
 	public Map<Integer, List<String>> getFilteredRefDataSpec(Map<Integer, List<String>> memberSpecByAxis, List<Intersection> dataBlocksToLoad, MemberTreeSet dimTrees) {
 		
 		long filterStartTime = System.currentTimeMillis();
 		String logMsg = null;
 		Map<Integer, List<String>> filteredRefDataSpec = new HashMap<Integer, List<String>>();
 		
 		
 		logMsg = "Filtering data cache reference data specification....";
 		logger.debug(logMsg);
 		
 		// Generate all the data block keys represented by the member specifications. Since
 		// reference data is loaded an entire block at a time, we only need to filter at the
 		// data block level instead of the individual intersection level
 		int indexedAxisCount = coreKeyAxes.length;
 		@SuppressWarnings("unchecked")
 		List<String>[] memberLists = new ArrayList[indexedAxisCount]; 
 		for (int i = 0; i < indexedAxisCount; i++) {
 			
 			// Get the next indexed core dimension axis
 			int axis = coreKeyAxes[i];
 			
 			// Get the list of members for the current axis. If there's
 			// no entry in the member spec then select all defined axis
 			// members.
 			List<String> memberList = memberSpecByAxis.get(axis);
 			if (memberList == null) {
 				memberList = new ArrayList<String>(Arrays.asList(getAxisMembers(axis)));
 			}
 			memberLists[i] = memberList;
 			
 			// Filter out any synthetic members. There is no point in
 			// loading them since they are calculated in Pace. (TTN-1860)
 			String dim = coreKeyDims[i];
 			PafDimTree dimTree = dimTrees.getTree(dim);
 			if (dimTree.hasSyntheticMembers()) {
 				memberList.removeAll(dimTree.getSyntheticMemberNames());
 			}
 			
 		}
 		StringOdometer dataBlockIterator = new StringOdometer(memberLists);
 		//List<Intersection> representedDataBlockKeys = IntersectionUtil.buildIntersections(memberLists, indexedCoreDims);
 
 		// Get list of keys for any requested data blocks that don't yet exist and don't
 		// correspond to empty data blocks in the multi-dimensional database.
 		while (dataBlockIterator.hasNext()) { 
 			String[] coords = dataBlockIterator.nextValue();
 			Intersection dataBlockKey = new Intersection(coreKeyDims, coords);		// TTN-1851
 			if (!isExistingDataBlock(dataBlockKey) && !isEmptyMdbDataBlock(dataBlockKey)) {   //TTN-1860
 				dataBlocksToLoad.add(dataBlockKey);
 			}
 		}
 		
 		
 		// Build a data spec that defines a superset of all non-existing data blocks
 		// that need to be loaded.
 		if (!dataBlocksToLoad.isEmpty()) {
 			
 			// Iterate through the required data block keys and compile a
 			// unique list of required members by dimension. 
 			Map<String, Set<String>> memberSets = new HashMap<String, Set<String>>();
 			for (String dim : coreKeyDims) {
 				memberSets.put(dim, new HashSet<String>());
 			}
 			for (Intersection dataBlockKey : dataBlocksToLoad) {
 				for (String dim : coreKeyDims) {
 					memberSets.get(dim).add(dataBlockKey.getCoordinate(dim));
 				}
 			}
 			// Build a revised member specification map that represents the
 			// minimal superset of all the required data block keys 
 			for (String dim : coreKeyDims) {
 				filteredRefDataSpec.put(this.getAxisIndex(dim),
 						new ArrayList<String>(memberSets.get(dim)));
 			}
 			// Add in all dense block members (all defined measure and time members)
 			//TODO Rolling Forecast - need to combine replace time&year with time horizon coord - might need to reflect this in UOW definition
 			filteredRefDataSpec.put(getMeasureAxis(),
 					Arrays.asList(getAxisMembers(getMeasureAxis())));
 			filteredRefDataSpec.put(getTimeAxis(),
 					Arrays.asList(getAxisMembers(getTimeAxis())));
 		}
 		
 		// Return filtered data spec
 		logMsg = "Filtering of mdb data specification";
 		logger.debug(logMsg);
 		performanceLogger.info(LogUtil.timedStep(logMsg, filterStartTime));
 		return filteredRefDataSpec;
 	}
 
 
 	/**
 	 * Generate a fully expanded unit of work specification
 	 * 
 	 * @return UnitOfWork
 	 */
 	public UnitOfWork getUowSpec() {
 		
 		int baseDimCount = baseDimensions.length;
 		String[][] members = new String[baseDimCount][];
 //		for (int i = 0; i < baseDimCount; i++) {
 //			members[i] = memberCatalog[i];
 //		}
 		System.arraycopy(memberCatalog, 0, members, 0, baseDimCount);
 		
 		UnitOfWork unitOfWork = new UnitOfWork(baseDimensions, members);
 		
 		return unitOfWork;
 	}
 	
 	/**
 	 * @return Returns the current year.
 	 */
 	public String getCurrentYear() {
 		return appDef.getCurrentYear();
 	}
 
 	
 	/**
 	 * @return Returns the lockedPeriodMap
 	 */
 	public Map<String, Set<String>> getLockedPeriodMap() {
 		return lockedPeriodMap;
 	}
 
 
 	/**
 	 * Returns the locked Periods for the specified year
 	 * 
 	 * @param year Year member
 	 * @return Returns the locked Periods
 	 */
 	public Set<String> getLockedPeriods(String year) {
 		
 		Set<String> lockedPeriods = lockedPeriodMap.get(year);
 		return lockedPeriods;
 	}
 
 	/**
 	 * @return the lockedYears
 	 */
 	public Set<String> getLockedYears() {
 		return lockedYears;
 	}
 
 	/**
 	 * Return the plannable years
 	 * 
 	 * @return Plannable years
 	 */
 	public Set<String> getPlanYears() {
 		Set<String> planYears = new HashSet<String>(Arrays.asList(getYears()));
 		planYears.removeAll(lockedYears);
 		return planYears;
 	}
 
 	/**
 	 * Return the number of plannable years
 	 * 
 	 * @return The number of plannable years
 	 */
 	public int getPlanYearSize() {
 		return this.getPlanYears().size();
 	}
 	
 	/**
 	 * @return the lockedTimeHorizonPeriods
 	 */
 	public Set<String> getLockedTimeHorizonPeriods() {
 		return lockedTimeHorizonPeriods;
 	}
 
 
 	/**
 	 * @return the invalidTimeHorizonPeriods
 	 */
 	public Set<String> getInvalidTimeHorizonPeriods() {
 		return invalidTimeHorizonPeriods;
 	}
 
 
 	/**
 	 * @return the invalidPeriodMap
 	 */
 	public Map<String, Set<String>> getInvalidPeriodMap() {
 		return invalidPeriodMap;
 	}
 
 
 	/**
 	 * Returns the invalid Periods for the specified year
 	 * 
 	 * @param year Year member
 	 * @return Returns the invalid Periods
 	 */
 	public Set<String> getInvalidPeriods(String year) {
 		
 		Set<String> invalidPeriods = invalidPeriodMap.get(year);
 		
 		return invalidPeriods;
 	}
 
 
 
 	/**
 	 * Returns true if the data cache contains any locked periods
 	 * 
 	 * @return True if the data cache contains any locked periods
 	 */
 	public boolean hasLockedPeriods() {
 		
 		// Check if at least one year has any locked periods
 		boolean hasLockedPeriods = false;
 		String[] years = getDimMembers(getYearDim());
 		for (String year : years) {
 			if (getLockedPeriods(year).size() > 0) {
 				hasLockedPeriods = true;
 				break;
 			}
 		}
 		
 		return hasLockedPeriods;
 	}
 
 
 	/**
 	 *	Returns the axis number corresponding to the Measure dimension
 	 *
 	 * @return Axis number corresponding to the Measure dimension
 	 */
 	public int getMeasureAxis() {
 		return getAxisIndex(getMeasureDim());
 	}
 
 	/**
 	 *	Returns the measureDef object for the specified measure
 	 *
 	 * @param measure measure dimension member
 	 *
 	 * @return the measureDef object for the specified measure
 	 */
 	public MeasureDef getMeasureDef(String measure) {
 
 		MeasureDef measureDef = getAppDef().getMeasureDef(measure);
 
 		// Check if versionDef was found
 		if (measureDef == null) {
 			// No matching versionDef found - throw IllegalArgumentException
 			String errMsg = "getMeasureDef() error - no matching measureDef for member: ["
 				+ measure + "]";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);	
 			throw iae;
 		}
 
 		return measureDef;
 	}
 
 	/**
 	 *	Returns the name of the Measure dimension
 	 *
 	 * @return Name of the Measure dimension
 	 */
 	public String getMeasureDim() {
 		return appDef.getMdbDef().getMeasureDim();
 	}
 
 	/**
 	 *	Return the number of members in the Measure dimension
 	 *
 	 * @return The number of members in the Measure dimension
 	 */
 	public int getMeasureSize() {
 		return getAxisSize(getMeasureAxis());
 	}
 
 	/**
 	 *	Returns the measure type for the specified measure
 	 *
 	 * @param measure Measure dimension member
 	 *
 	 * @return the measure type for the specified measure
 	 */
 	public MeasureType getMeasureType(String measure) {
 		return getMeasureDef(measure).getType();	
 	}
 
 
 	/**
 	 *	Returns the axis number corresponding to the PlanType dimension
 	 *
 	 * @return Axis number corresponding to the PlanType dimension
 	 */
 	public int getPlanTypeAxis() {
 		return getAxisIndex(getPlanTypeDim());
 	}
 
 	/**
 	 *	Returns the name of the PlanType dimension
 	 *
 	 * @return Name of the PlanType dimension
 	 */
 	public String getPlanTypeDim() {
 		return appDef.getMdbDef().getPlanTypeDim();
 	}
 
 	/**
 	 *	Returns the axis number corresponding to the Time dimension
 	 *
 	 * @return Axis number corresponding to the Time dimension
 	 */
 	public int getTimeAxis() {
 		return getAxisIndex(getTimeDim());
 	}
 
 	/**
 	 *	Returns the name of the Time dimension
 	 *
 	 * @return Name of the Time dimension
 	 */
 	public String getTimeDim() {
 		return appDef.getMdbDef().getTimeDim();
 	}
 
 	/**
 	 *	Return the number of members in the Time dimension
 	 *
 	 * @return The number of members in the Time dimension
 	 */
 	public int getTimeSize() {
 		return getAxisSize(getTimeAxis());
 	}
 
 	/**
 	 *	Returns the axis number corresponding to the Time Horizon dimension
 	 *
 	 * @return Axis number corresponding to the Time Horizon dimension
 	 */
 	public int getTimeHorizonAxis() {
 		return getAxisIndex(getTimeHorizonDim());
 	}
 
 	/**
 	 *	Returns the name of the Time Horizon dimension
 	 *
 	 * @return Name of the Time Horizon dimension
 	 */
 	public String getTimeHorizonDim() {
 		return PafBaseConstants.TIME_HORIZON_DIM;
 	}
 
 	/**
 	 *	Return the number of members in the Time Horizon dimension
 	 *
 	 * @return The number of members in the Time Horizon dimension
 	 */
 	public int getTimeHorizonSize() {
 		return getAxisSize(getTimeHorizonAxis());
 	}
 
 	/**
 	 * Return the member generation offset between members in the time horizon tree and
 	 * their corresponding members in the time dimension tree
 	 * 
 	 * @return
 	 */
 	public int getTimeHorizGenOffset() {
 		
 		if (timeHorizGenOffset == null) {
 			// This collection is lazy loaded
 			PafDimTree timeTree = getDimTrees().getTree(getTimeDim());
 			PafDimMember firstTimeFloorMbr = timeTree.getFirstFloorMbr();
 			int timeFloorGen = firstTimeFloorMbr.getMemberProps().getGenerationNumber();
 			PafDimTree timeHorizTree = getDimTrees().getTree(getTimeHorizonDim());
 			PafDimMember firstTimeHorizFloorMbr = timeHorizTree.getFirstFloorMbr();
 			int timeHorizFloorGen = firstTimeHorizFloorMbr.getMemberProps().getGenerationNumber();
 			timeHorizGenOffset = timeHorizFloorGen - timeFloorGen;
 		}
 		return timeHorizGenOffset;
 	}
 	
 	
 	/**
 	 *	Returns the axis number corresponding to the Version dimension
 	 *
 	 * @return Axis number corresponding to the Version dimension
 	 */
 	public int getVersionAxis() {
 		return getAxisIndex(getVersionDim());
 	}
 
 	/**
 	 *	Returns the versionDef object for the specified version
 	 *
 	 * @param version Version dimension member
 	 *
 	 * @return the versionDef object for the specified version
 	 */
 	public VersionDef getVersionDef(String version) {
 
 		VersionDef versionDef = getAppDef().getVersionDef(version);
 
 		// Check if versionDef was found
 		if (versionDef == null) {
 			// No matching versionDef found - throw IllegalArgumentException
 			String errMsg = "getVersionDef() error - no matching versionDef for member: ["
 				+ version + "]";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);	
 			throw iae;
 		}
 
 		return versionDef;
 	}
 
 	/**
 	 *	Returns the versionDefs catalog from the application definition object
 	 *  for those versions that exist in this data cache
 	 *
 	 * @return the versionDefs catalog from the application definition object
 	 */
 	public Map<String, VersionDef> getVersionDefs() {
 		
 		Map<String, VersionDef> validVersionDefs = new HashMap<String, VersionDef>();
 		String[] validVersions = getVersions();
 		for (String version:validVersions) {
 			validVersionDefs.put(version, getAppDef().getVersionDef(version));
 		}
 
 		return validVersionDefs;
 	}
 
 	/**
 	 *	Returns the list of versionDef objects for the derived versions that exist 
 	 *  in this data cache
 	 *
 	 * @return List of versionDef objects
 	 */
 	public List<VersionDef> getDerivedVersionDefs() {
 		
 		List<VersionDef> allDerivedVersionDefs = getAppDef().getDerivedVersionDefs();		
 		List<VersionDef> derivedVersionDefs = new ArrayList<VersionDef>();
 		for (VersionDef versionDef:allDerivedVersionDefs) {
 			String version = versionDef.getName();
 			if (isMember(getVersionDim(), versionDef.getName())) {
 				derivedVersionDefs.add(getAppDef().getVersionDef(version));
 			}
 		}
 
 		return derivedVersionDefs;
 	}
 
 	/**
 	 *	Returns the list of versionDef objects for the variance versions that exist 
 	 *  in this data cache
 	 *
 	 * @return List of versionDef objects
 	 */
 	public List<VersionDef> getVarianceVersionDefs() {
 		
 		List<VersionDef> allVarianceVersionDefs = getAppDef().getVarianceVersionDefs();		
 		List<VersionDef> varianceVersionDefs = new ArrayList<VersionDef>();
 		for (VersionDef versionDef:allVarianceVersionDefs) {
 			String version = versionDef.getName();
 			if (isMember(getVersionDim(), versionDef.getName())) {
 				varianceVersionDefs.add(getAppDef().getVersionDef(version));
 			}
 		}
 
 		return varianceVersionDefs;
 	}
 
 	/**
 	 *	Returns the name of the Version dimension
 	 *
 	 * @return Name of the Version dimension
 	 */
 	public String getVersionDim() {
 		return appDef.getMdbDef().getVersionDim();
 	}
 
 	/**
 	 *	Returns the entire list of versions in the PafDataCache 
 	 *
 	 * @return The entire list of versions with the PafDataCache
 	 */
 	public String[] getVersions() {
 		return getAxisMembers(getVersionAxis());
 	}
 
 	/**
 	 *	Return list of base versions
 	 *
 	 * @return List<String>
 	 */
 	public List<String> getBaseVersions() {
 
 		List<String> baseVersions = getVersionsByType(PafBaseConstants.BASE_VERSION_TYPE_LIST);
 		return baseVersions;
 	}
 
 	/**
 	 *	Return list of contribution percent versions
 	 *
 	 * @return List<String>
 	 */
 	public List<String> getContribPctVersions() {
 
 		List<String> contribPctVersions = getVersionsByType(VersionType.ContribPct);
 		return contribPctVersions;
 	}
 
 	/**
 	 *	Return list of derived versions
 	 *
 	 * @return List<String>
 	 */
 	public List<String> getDerivedVersions() {
 
 		// Get the list of derived versions that are defined to the data cache
 		List<String> derivedVersions = getVersionsByType(PafBaseConstants.DERIVED_VERSION_TYPE_LIST);
 		return derivedVersions;
 	}
 
 	/**
 	 *	Return list of reference versions - all non-derived versions, except the 
 	 *  active planning versions
 	 *
 	 * @return List<String>
 	 */
 	public List<String> getReferenceVersions() {
 
 		List<String> referenceVersions = getBaseVersions();
 		referenceVersions.remove(getPlanVersion());
 		
 		return referenceVersions;
 	}
 
 	/**
 	 *	Return list of variance percent versions
 	 *
 	 * @return List<String>
 	 */
 	public List<String> getVarianceVersions() {
 
 		List<String> varianceVersions = getVersionsByType(VersionType.Variance);
 		return varianceVersions;
 	}
 
 	/**
 	 *	Return list of offset versions
 	 *
 	 * @return List<String>
 	 */
 	public List<String> getOffsetVersions() {
 
 		List<String> varianceVersions = getVersionsByType(VersionType.Offset);
 		return varianceVersions;
 	}
 
 	/**
 	 *	Returns the list of Versions with the specified VersionType
 	 *
 	 * @param versionType The Version Type to match
 	 * 
 	 * @return The list of Versions with the specified VersionType
 	 */
 	public List<String> getVersionsByType(VersionType versionType) {
 
 		List<String> matchedVersions = new ArrayList<String>();
 		String[] allVersions = getVersions();
 
 		// Cycle through all defined versions and return the ones that match "VersionType"
 		for(String version:allVersions)  {
 			if (getVersionType(version) == versionType) {
 				matchedVersions.add(version);
 			}
 		}
 		return matchedVersions;
 	}
 
 	/**
 	 *	Return list of versions that match the specified version type filter
 	 *
 	 * @param versionTypeFilter List of selected version type
 	 * @return List<String>
 	 */
 	public List<String> getVersionsByType(List<VersionType> versionTypeFilter) {
 
 		List<String> versions = new ArrayList<String>();
 
 		// Cycle through all defined versions and return the ones that
 		// match one of the selected version types.
 		for(String version:getVersions())  {
 			VersionType versionType = getVersionType(version);
 			if (versionTypeFilter.contains(versionType)) {
 				versions.add(version);
 			}
 		}
 		return versions;
 	}
 
 	/**
 	 *	Returns the number of members in the Version dimension
 	 *
 	 * @return The number of members in the Version dimension
 	 */
 	public int getVersionSize() {
 		return getAxisSize(getVersionAxis());
 	}
 
 	/**
 	 *	Returns the version type for the specified version
 	 *
 	 * @param version Version dimension member
 	 *
 	 * @return the version type for the specified version
 	 */
 	public VersionType getVersionType(String version) {
 		return getVersionDef(version).getType();	
 	}
 
 	/**
 	 *	Returns the axis number corresponding to the Year dimension
 	 *
 	 * @return Axis number corresponding to the Year dimension
 	 */
 	public int getYearAxis() {
 		return getAxisIndex(getYearDim());
 	}
 
 	/**
 	 *	Returns the name of the Year dimension
 	 *
 	 * @return Name of the Year dimension
 	 */
 	public String getYearDim() {
 		return appDef.getMdbDef().getYearDim();
 	}
 
 	/**
 	 *	Return the number of members in the Year dimension
 	 *
 	 * @return The number of members in the Year dimension
 	 */
 	public int getYearSize() {
 		return getAxisSize(getYearAxis());
 	}
 
 	/**
 	 *	Returns the entire list of years in the data cache 
 	 *
 	 * @return The entire list of years in the data cache
 	 */
 	public String[] getYears() {
 		return getAxisMembers(getYearAxis());
 	}
 	
 	/**
 	 *	Add data cache cell to the list of changed cells
 	 *
 	 * @param cellIntersection Data cache cell intersection
 	 * @param cellValue Changed cell value
 	 */
 	public void addChangedCell(Intersection cellIntersection, double cellValue) {
 		changedCells.add(cellIntersection, cellValue);		
 	}
 
 	/**
 	 * @return Returns the list of changed data cache cells.
 	 */
 	public List<PafDataCacheCell> getChangedCells() {
 		return changedCells.getCells();
 	}
 
 	/**
 	 * @return Returns the set of changed data cache intersections.
 	 */
 	public Set<Intersection> getChangedIntersections() {
 		return changedCells.getCellIntersections();
 	}
 
 	/**
 	 *	Initialize the list of changed cells
 	 *
 	 */
 	public void initChangedCells() {
 		logger.debug("Initializing changed cells list");
 		this.changedCells = new PafDataCacheCells();
 	}	
 
 	/**
 	 *	Load in cells from another data cache. This is a convenience method for
 	 *  loadCacheCells(sourceCache, memberFilter) where memberFilter is set to null.
 	 *
 	 * @param sourceCache Source data cache
 	 * 
 	 * @return PafDataCache
 	 * @throws PafException 
 	 */
 	public void loadCacheCells(final PafDataCache sourceCache) throws PafException  {
 		loadCacheCells(sourceCache, null);
 	}
 
 	/**
 	 *	Load in cells from another data cache using the supplied member filter
 	 *
 	 * @param sourceCache Source data cache
 	 * @param memberFilter Member filter by dimension
 	 * 
 	 * @return PafDataCache
 	 * @throws PafException 
 	 */
 	public void loadCacheCells(final PafDataCache sourceCache, final Map<String, List<String>> memberFilter) throws PafException  {
 
 		String[] dimensions = getAllDimensions();
 		Map<String, List<String>> generatedMemberFilter = new HashMap<String, List<String>>();
 
 		// By default a filter will be created for each dimension, consisting of the common members
 		// between both data caches. However, any supplied filters for a given dimension will take 
 		// precedence.
 		for (String dimension : dimensions) {
 			// Use dimensionality of all dsCache dimensions, except the version dimension
 			if (memberFilter != null && memberFilter.containsKey(dimension)) {
 				
 				// Validate filter members
 				List<String> memberList = memberFilter.get(dimension);
 				List<String> missingMembers = new ArrayList<String>();
 				for (String member : memberList) {
 					if (!isMember(dimension, member) || !sourceCache.isMember(dimension, member)) {
 						missingMembers.add(member);
 					}
 				}
 				memberList.removeAll(missingMembers);
 				
 				// Exit if there are no valid members for current filtered dimension
 				if (memberList.isEmpty()) {
 					return;
 				}
 				
 				// Add dimension filter
 				generatedMemberFilter.put(dimension, memberList);
 				
 			} else {
 				
 				List<String> destCacheMembers = Arrays.asList((getDimMembers(dimension)));
 				Set<String> commonMembers = new HashSet<String>(destCacheMembers);
 				List<String> sourceCacheMembers = Arrays.asList((sourceCache.getDimMembers(dimension)));
 				commonMembers.retainAll(new HashSet<String>(sourceCacheMembers));
 				//FIX TTN-838 (KRM)
 				if(commonMembers != null && commonMembers.size() == 0){
 					String members = StringUtils.arrayToString(destCacheMembers.toArray(new String[0]), "", "", "", "", ",");
 					String errMsg = "The following dimension: [" + dimension + "], has member(s): [" + members + "] that are defined on the view, but don't exist in the unit of work.";
 					logger.error(errMsg);
 					throw new PafException(errMsg, PafErrSeverity.Error);
 				}
 				generatedMemberFilter.put(dimension, Arrays.asList(commonMembers.toArray(new String[0])));
 			}
 		}
 
 		// Iterate through all cell intersections represented by the member filter
 		StringOdometer cacheIterator = new StringOdometer(generatedMemberFilter, dimensions);
 		while(cacheIterator.hasNext()) {
 
 			// Copy source intersection to this data cache
 			String[] coords = cacheIterator.nextValue();
 			Intersection intersection = new Intersection(dimensions, coords);
 //			try {
 				setCellValue(intersection, sourceCache.getCellValue(intersection));
 //			} catch (PafException pfe) {
 //				// Problem encountered with source cache index
 //				String errMsg = "Unable to load data into DESTINATION data cache - requested member don't exist in SOURCE data cache.";
 //				logger.error(errMsg);
 //				errMsg = pfe.getMessage() + "  --- " + errMsg;
 //				throw new PafException(errMsg, PafErrSeverity.Error);
 //			}
 		}
 	}
 
 	/**
 	 * 	Get "data slice" using dimensional and member specifications
 	 *  defined in supplied parameter object. The "data slice"
 	 *  contains all of the cell values that are needed to populate
 	 *  a client view section. Meta-data is passed down to the client 
 	 *  via other objects and processes outside of this method.
 	 *
 	 *  Intersections with invalid time horizon intersections are 
 	 *  skipped.
 	 *  
 	 * @param parms Object containing required PafDataSlice parameters
 	 * @param invalidTimeHorizPeriods Invalid time horizon periods
 	 * 
 	 * @return Returns "Data Slice" - a subset of cells in the UowCache
 	 * @throws PafException
 	 */
 	public PafDataSlice getDataSlice(PafDataSliceParms parms) throws PafException {
 
 		boolean hasPageDimensions = false;
 		int cellCount = 0, cols = 0, rows = 0, sliceIndex = 0;
 		double[] dataSlice = null;
 		String[] rowDims = parms.getRowDimensions();
 		String[] colDims = parms.getColDimensions();
 		String[][] rowTuples = parms.getRowTuples();
 		String[][] colTuples = parms.getColTuples();
 		String[] dimSequence = parms.getDimSequence();
 		PafDataSlice pafDataSlice = null;
 
 		try {
 			// Validate data slice parms
 			logger.debug("Validating PafDataSlice parameters...");
 			hasPageDimensions = validateDataSliceParms(parms);
 
 			// Creating new data slice array 
 			logger.debug("Creating new data slice array");
 			cols = colTuples.length;
 			rows = rowTuples.length;
 			cellCount = rows * cols;
 			dataSlice = new double[cellCount];
 
 			// Create reusable cell intersection that will to access
 			// data cache data. This intersection will get updated as
 			// we iterate through all the tuple members
 			Intersection cellIs = new Intersection(dimSequence);
 			
 			// Enter page headers into appropriate elements of the data 
 			// cache cell intersection
 			if (hasPageDimensions) {
 				logger.debug("Entering page headers into cell intersection");
 				for (int i = 0; i < parms.getPageDimensions().length; i++) {
 					cellIs.setCoordinate(parms.getPageDimensions()[i], parms.getPageMembers()[i]);
 				}
 			}
 
 			// Load data slice. Start by cycling through row tuples
 			logger.info("Getting data slice - [" + StringUtils.commaFormat(rows)  
 					+ "] rows  X  [" + cols + "] columns  =  [" 
 					+ StringUtils.commaFormat(cellCount) + "] total cells");  
 			for (String[] rowTuple:rowTuples) {
 	
 				// Updated the cell intersection with the current row header members
 				for (int i = 0; i < rowDims.length; i++) {
 					cellIs.setCoordinate(rowDims[i], rowTuple[i]);
 				}
 
 				// Cycle through column tuples
 				for (String[] colTuple:colTuples) {
 
 					// Update the cell intersection with the current column header
 					// members 
 					for (int i = 0; i < colDims.length; i++) {
 						cellIs.setCoordinate(colDims[i], colTuple[i]);
 					}
 	
 					// Skip any intersection containing an invalid time horizon period (TTN-1595)
 					if (!hasValidTimeHorizonCoord(cellIs)) {
 						sliceIndex++;
 						continue;
 					}
 
 					// Copy selected cell to data slice
 					try {
 						dataSlice[sliceIndex++] = getCellValue(cellIs);
 					} catch (IllegalArgumentException iae) {
 						String errMsg = "Data retrieval error - the view references an intersection: "
 							+ StringUtils.arrayToString(cellIs.getCoordinates()) 
 							+ " that is outside the current unit of work";
 						throw new IllegalArgumentException(errMsg);
 					}
 				}
 			}
 
 			// Transfer data slice to PafDataSlice object
 			pafDataSlice = new PafDataSlice(dataSlice, cols);
 
 		} catch (PafException pfe) {
 			// throw Paf Exception
 			throw pfe;
 		} catch (Exception ex) {
 			// throw Paf Exception
 			String errMsg = ex.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, ex);	
 			throw pfe;
 		}
 
 		logger.debug("Returning pafDataSlice");
 		return pafDataSlice;
 	}
 
 
 	/**
 	 *	Validate data slice parameters
 	 *
 	 * @param parms PafDataSlice parameter object
 	 * 
 	 * @return True if page dimensions are found among data slice parameters
 	 * @throws PafException 
 	 */
 	public boolean validateDataSliceParms(PafDataSliceParms parms) throws PafException {
 
 		boolean hasPageDimensions;
 		boolean[] dimensionFlags = null;
 		String[] viewDims = pafMVS.getViewSection().getDimensionsPriority();
 		int coreDimCount = coreDimensions.size(), viewDimCount = viewDims.length;
 		String[] pageDimensions = parms.getPageDimensions();
 		String[] colDimensions = parms.getColDimensions();
 		String[] rowDimensions = parms.getRowDimensions();
 		String[][] colMembers = parms.getColTuples();
 		String[] pageMembers = parms.getPageMembers();
 		String[][] rowMembers = parms.getRowTuples();
 
 
 		// Checking for existence of page dimensions
 		if (pageDimensions != null && pageDimensions.length > 0) {
 			hasPageDimensions = true;
 		} else {
 			hasPageDimensions = false;
 		}
 
 		// Check that all arrays are not empty (page dimensions are optional)
 		logger.debug("Validating that arrays are not empty....");
 		//validateArrayNotEmpty(pageDimensions, "pageDimensions");
 		validateArrayNotEmpty(colDimensions, "colDimensions");
 		validateArrayNotEmpty(rowDimensions, "rowDimensions");
 		if (hasPageDimensions) validateArrayNotEmpty(pageMembers, "pageMembers");
 		validateArrayNotEmpty(colMembers, "colMembers");
 		validateArrayNotEmpty(rowMembers, "rowMembers");
 
 		// Validate that the correct number of dimensions have been specified
 		int parmsDimCount = 0;
 		if (hasPageDimensions) {
 			parmsDimCount = pageDimensions.length + colDimensions.length + rowDimensions.length;
 		} else {
 			parmsDimCount = colDimensions.length + rowDimensions.length;
 		}
 		
 		if (parmsDimCount != viewDimCount) {
 			// Incorrect number of dimensions specified
 			String errMsg;
 			if (parmsDimCount >= viewDimCount) {
 				String missingparmDims = " ";
 
 				List<String> viewDimList = Arrays.asList(viewDims);
 				for(String dim : parms.getAllDimensions()){
 					if (! viewDimList.contains(dim)){
 						missingparmDims += dim + ", ";
 					}
 				}
 				
 				errMsg = "The view definition has more dimensions than the server expects.  The extra view dimensions are: " + missingparmDims;
 			}else{
 				errMsg = parmsDimCount + " dimensions specified in data slice parms, but data cache has "
 				+ viewDimCount + " dimensions specified"; 
 			}
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 			throw pfe; 				
 		}
 
 		// Check that all dimensions are valid and all core dimensions are accounted for 
 		logger.debug("Validating that all dimensions are valid and all core dimensions are accounted for....");
 		dimensionFlags = new boolean[coreDimCount];
 		for (int i = 0; i < coreDimCount; i++) {
 			dimensionFlags[i] = false;
 		}
 		if (hasPageDimensions) {
 			for (String dimension:pageDimensions) {
 				if (axisIndexMap.containsKey(dimension)) {
 					// Indicate that dimension was found (if it's a core dimension)
 					if (coreDimensions.contains(dimension)) {
 						dimensionFlags[axisIndexMap.get(dimension)] = true;
 					}
 				} else {
 					// Unknown page dimension
 					String errMsg = "Unknown page dimension [" + dimension 
 					+ "] found in paf data slice parms";
 					logger.error(errMsg);
 					PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 					throw pfe; 				
 				}
 			}
 		}
 		for (String dimension:colDimensions) {
 			if (axisIndexMap.containsKey(dimension)) {
 				// Indicate that dimension was found (if it's a core dimension)
 				if (coreDimensions.contains(dimension)) {
 					dimensionFlags[axisIndexMap.get(dimension)] = true;
 				}
 			} else {
 				// Unknown column dimension
 				String errMsg = "Unknown column dimension [" + dimension 
 				+ "] found in paf data slice parms";
 				logger.error(errMsg);
 				PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 				throw pfe; 				
 			}
 		}
 		for (String dimension:rowDimensions) {
 			if (axisIndexMap.containsKey(dimension)) {
 				// Indicate that dimension was found (if it's a core dimension)
 				if (coreDimensions.contains(dimension)) {
 					dimensionFlags[axisIndexMap.get(dimension)] = true;
 				}
 			} else {
 				// Unknown row dimension
 				String errMsg = "Unknown row dimension [" + dimension 
 						+ "] found in paf data slice parms";
 				logger.error(errMsg);
 				PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 				throw pfe; 				
 			}
 		}
 		for (int i = 0; i < coreDimCount; i++) {
 			if (!dimensionFlags[i]) {
 				// Unknown row dimension
 				String errMsg = "Dimension [" + allDimensions.get(i) 
 						+ "] was not specified in the data slice parms";
 				logger.error(errMsg);
 				PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 				throw pfe; 								
 			}
 		}
 
 		// Check that corresponding dimension and tuple arrays match
 		if (hasPageDimensions) {
 			if (pageDimensions.length != pageMembers.length) {
 				// Row dimensions array has different number of dimensions than row tuples array
 				String errMsg = "Page dimensions array has different number of dimensions than page tuples array";
 				logger.error(errMsg);
 				PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 				throw pfe; 	
 			}
 		}
 		if (rowDimensions.length != rowMembers[0].length) {
 			// Row dimensions array has different number of dimensions than row tuples array
 			String errMsg = "Row dimensions array has different number of dimensions than row tuples array";
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 			throw pfe; 											
 		}
 		if (colDimensions.length != colMembers[0].length) {
 			// Row dimensions array has different number of dimensions than row tuples array
 			String errMsg = "Column dimensions array has different number of dimensions than column tuples array";
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error);	
 			throw pfe; 											
 		}
 
 		// Indicate presence of page dimensions
 		return hasPageDimensions;
 	}
 
 
 	/**
 	 * Return the dimension for the specified axis
 	 * 
 	 * @param axis Axis index
 	 * @return Returns the dimension name.
 	 */
 	public String getDimension(int axis) {
 
 		// Validate axis
 		if (axis < 0 || axis > axisCount - 1) {
 			// Invalid axis value
 			String errMsg = "Unable to get dimension for axis ["
 				+ axis + "] - axis value is out of bounds";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          			
 		}
 		return allDimensions.get(axis);
 	}
 
 
 	/**
 	 *	Return corresponding data cache axis indexes 
 	 *	for supplied array of dimension names
 	 *
 	 * @param dimNames Array of dimension names
 	 * @return Returns array of data cache axis indexes
 	 */
 	public int[] getDimIndexes(String[] dimNames) {
 
 		int[] dimIndexes = new int[dimNames.length];
 		for (int i = 0; i < dimNames.length; i++) {
 			dimIndexes[i] = getAxisIndex(dimNames[i]);
 		}
 		return dimIndexes;
 	}
 
 
 	/**
 	 *	Return the dimension member for the corresponding axis and member index
 	 *
 	 * @param axis Axis index
 	 * @param memberIndex Dimension member index
 	 * @return Returns the dimension member for the corresponding axis and member index.
 	 */
 	public String getDimMember(int axis, int memberIndex) {
 		return getAxisMembers(axis)[memberIndex];
 	}    
 
 
 	/**
 	 *	Return an array containing the members in the specified dimension name
 	 *
 	 * @param dimName Dimension name
 	 * @return Returns an array containing the members in the specified axis.
 	 */
 	public String[] getDimMembers(String dimName) {
 		return getAxisMembers(getAxisIndex(dimName));
 	}    
 
 	/**
 	 *	Return an array containing the dimension members that meet the 
 	 *  specified selection criteria.
 	 *  
 	 *  A null value in a property selection indicates that property will be ignored.
 	 *
 	 * @param dimName Dimension name
 	 * @param optionalBranch Branch to select members from (if not supplied then the entire dimension is selected)
 	 * @param isReadOnly Determines if read-only members should be selected
 	 * @param isSynthetic Determines if synthetic members should be selected
 	 * 
 	 * @return Returns an array containing the members in the specified axis.
 	 */
 	public String[] getFilteredDimMembers(String dimName, String optionalBranch, Boolean isReadOnly, Boolean isSynthetic) {
 		
 		//TODO Create a more extensible way to filter out members
 		//TODO Migrate this method to the DimTree Class.
 		
 		PafDimTree dimTree = getDimTrees().getTree(dimName);
 		Set<String> filteredMembers = null;
 		
 		// Start out with a set of all members under consideration - either entire dimension or a selected branch
 		if (optionalBranch == null) {
 			filteredMembers = new HashSet<String>(Arrays.asList(getDimMembers(dimName)));
 		} else {
 			PafDimMember branchMember = dimTree.getMember(optionalBranch);
 			filteredMembers = new HashSet<String>(dimTree.getMemberNames(dimTree.getMembers(branchMember, TreeTraversalOrder.POST_ORDER)));
 		}
 
 		// Apply "synthetic" filter
 		if (isReadOnly != null) {
 			Set<String> syntheticMembers = dimTree.getSyntheticMemberNames();
 			if (isSynthetic) {
 				filteredMembers.retainAll(syntheticMembers);
 			} else {
 				filteredMembers.removeAll(syntheticMembers);
 			}
 		}
 		
 		// Apply "readOnly" filter
 		if (isReadOnly != null) {
 			Set<String> readOnlyMembers = dimTree.getReadOnlyMemberNames();
 			if (isReadOnly) {
 				filteredMembers.retainAll(readOnlyMembers);
 			} else {
 				filteredMembers.removeAll(readOnlyMembers);
 			}
 		}
 		
 		return filteredMembers.toArray(new String[0]);
 	}    
 
 
 	/**
 	 * Return all the recalc measures
 	 * 
 	 * @return All the recalc measures
 	 */
 	public List<String> getRecalcMeasures() {
 		return getFilteredMeasures(MeasureType.Recalc);
 	}
 
 
 	/**
 	 * Return the measures matching the selected measure type
 	 * 
 	 * @param selMeasureType Selected measure type
 	 * @return All the measures matching the measure type
 	 */
 	public List<String> getFilteredMeasures(MeasureType selMeasureType) {
 		
 		final String[] measures = getDimMembers(getMeasureDim());
 		List<String> filteredMeasures = new ArrayList<String>();
 		for (String measure : measures) {
 			MeasureDef measureDef = getMeasureDef(measure);
 			if (measureDef != null && measureDef.getType().equals(selMeasureType)) {
 				filteredMeasures.add(measure);
 			}
 		}
 		
 		return filteredMeasures;
 	}
 
 
 	/**
 	 * Return the measures matching one of the selected measure types
 	 * 
 	 * @param selMeasureType Selected measure type
 	 * @return All the measures matching the measure type
 	 */
 	public List<String> getFilteredMeasures(MeasureType[] selMeasureTypes) {
 		
 		final String[] measures = getDimMembers(getMeasureDim());
 		List<String> filteredMeasures = new ArrayList<String>();
 		for (String measure : measures) {
 			MeasureDef measureDef = getMeasureDef(measure);
 			if (measureDef != null) {
 				boolean isValidMeasure = false;
 				for (int i = 0; i < selMeasureTypes.length && !isValidMeasure; i++) {
 					MeasureType selMeasureType = selMeasureTypes[i]; 
 					if (measureDef.getType().equals(selMeasureType)) {
 						filteredMeasures.add(measure);
 						isValidMeasure = true;
 					}
 				}
 			}
 		}
 
 		return filteredMeasures;
 	}
 
 
 	/**
 	 *	Return number of data cache rows
 	 *
 	 * @return Returns the number of data cache rows.
 	 */
 	public int getRowCount() {
 		return getCellCount() / getColumnCount();
 	}
 
 
 	/**
 	 *	Convert a member name to its corresponding index value
 	 *
 	 * @param axis Axis index
 	 * @param member Dimension member
 	 * 
 	 * @return Returns a dimension member index.
 	 */
 	public int getMemberIndex(String member, int axis) {
 
 		int memberIndex = 0;
 
 		// Validate axis
 		if (axis < 0 || axis > axisCount - 1) {
 			// Invalid axis value
 			String errMsg = "Unable to convert member [" 
 				+ member + "] to an index - axis value of [" + axis + "] - axis is out of bounds";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          			
 		}
 
 		// Compute member index
 		if (memberIndexMap[axis].containsKey(member)) {
 			memberIndex = memberIndexMap[axis].get(member);
 		} else { 
 			// Member not found
 			String errMsg = "Invalid cell coordinate reference - member ["
 				+ member + "] not found in axis [" + axis + "] (dimension [" + treeDims.get(axis) 
 				+ "]). This usually indicates that the specified member is not defined in the current Unit of Work.";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          			
 		}
 
 		return memberIndex;
 	}
 
 
 
 	/**
 	 *	Determines if the specified member exists in the specified dimension
 	 *
 	 * @param dimension Dimension name
 	 * @param member Dimension member name
 	 * 
 	 * @return True if the member exists in the specified dimension
 	 */
 	public boolean isMember(String dimension, String member) {
 
 		boolean isFound = false;
 
 		// Get axis for specified dimension
 		int axis = getAxisIndex(dimension);
 
 		// Look for member
 		Map<String, Integer> memberMap = memberIndexMap[axis];
 		if (memberMap.containsKey(member)) {
 			isFound = true;
 		}
 
 		// Return status
 		return isFound;
 	}
 
 	/**
 	 *	Determines if the specified member exists in the specified dimension,
 	 *  ignoring case.
 	 *
 	 * @param dimension Dimension name
 	 * @param searchMember Dimension member name to search for
 	 * @param foundMember Dimension member name found (could differ in case)
 	 * 
 	 * @return True if the member exists in the specified dimension
 	 */
 	public boolean isMemberIgnoresCase(String dimension, String searchMember, StringBuffer foundMember) {
 	
 		// Initialize found member string buffer
 		foundMember.setLength(0);
 		foundMember.trimToSize();
 		
 		// Look for member - ignoring case
 		if (isMember(dimension, searchMember)) {
 			foundMember.append(searchMember);
 			return true;
 		}
 			
 		// Check for alternate case match
 		String[] members = getDimMembers(dimension);
 		for (String member : members) {
 			if (searchMember.equalsIgnoreCase(member)) {
 				foundMember.append(member);
 				return true;
 			}
 		}
 
 		// Member not found
 		return false;
 	}
 
 
 	/**
 	 *	Determines if the specified data cache intersection is dirty
 	 *
 	 * @param intersection Cell intersection
 	 * 
 	 * @return True if the specified intersection is dirty
 	 * @throws PafException 
 	 */
 	public boolean isDirtyIntersection(Intersection intersection) throws PafException {
 
 		boolean isDirty = (Boolean) getCellProperty(intersection, CellPropertyType.Dirty);
 		return isDirty;
 	}
 
 	/**
 	 *	Clears the dirty property on the specified cell intersection 
 	 *
 	 * @param intersection Cell intersection
 	 * @throws PafException 
 	 */
 	public void clearDirtyIntersection(Intersection intersection) throws PafException {
 		setCellProperty(intersection, CellPropertyType.Dirty, false);
 	}
 
 	/**
 	 *	Clears the dirty property on the specified cell intersections 
 	 *
 	 * @param intersections Cell intersections
 	 * @throws PafException 
 	 */
 	public void clearDirtyIntersections(List<Intersection> intersections) throws PafException {
 	
 		for (Intersection intersection : intersections) {
 			clearDirtyIntersection(intersection);
 		}
 	}
 
 	/**
 	 *	Clears all snapshot values from the data cache
 	 */
 	public void clearSnapshotValues() {
 		initSnapshotBlocks();
 	}
 
 	/**
 	 *	Determines if the specified data cache intersection is
 	 *	empty (un-populated)
 	 *
 	 * @param intersection Cell intersection
 	 * @return True if the specified intersection has not been populated
 	 * 
 	 * @throws PafException 
 	 */
 	public boolean isEmptyIntersection(Intersection intersection) throws PafException {
 
 		boolean isEmpty = (Boolean) getCellProperty(intersection, CellPropertyType.Empty);
 		return isEmpty;
 	}
 
 	/**
 	 *	Determines if the specified data cache intersection is populated
 	 *
 	 * @param intersection Cell intersection
 	 * 
 	 * @return True if the specified intersection is populated
 	 * @throws PafException 
 	 */
 	public boolean isPopulatedIntersection(Intersection intersection) throws PafException {
 		return !isEmptyIntersection(intersection);
 	}
 
 	/**
 	 *	Marks the specified cell intersection as empty
 	 *
 	 * @param intersection Cell intersection
 	 * @throws PafException 
 	 */
 	public void setEmptyIntersection(Intersection intersection) throws PafException {
 		
 		setCellProperty(intersection, CellPropertyType.Empty, true);
 		setCellValue(intersection, 0); // Initialize cell value
 	}
 
 	/**
 	 *	Marks the specified cell intersection as empty
 	 *
 	 * @param intersections Cell intersections
 	 * @throws PafException 
 	 */
 	public void setEmptyIntersections(List<Intersection> intersections) throws PafException {
 		
 		for (Intersection intersection : intersections) {
 			setEmptyIntersection(intersection);
 		}
 	}
 
 
 	/**
 	 *	Determines if the specified data block was referenced in the  
 	 *  multi-dimensional database but not loaded because it was empty.
 	 *
 	 * @param key Data block key
 	 * @return True if the specified data block exists in the data cache
 	 */
 	public boolean isEmptyMdbDataBlock(Intersection key) {
 
 		boolean isFound = false;
 
 		// Look for data block
 		if (emptyMdbBlocks.contains(key)) {
 			isFound = true;
 		}
 
 		// Return status
 		return isFound;
 	}
 
 
 	/**
 	 *	Determines if the specified data block exists in the data cache
 	 *
 	 * @param key Data block key
 	 * @return True if the specified data block exists in the data cache
 	 */
 	public boolean isExistingDataBlock(Intersection key) {
 
 		boolean isFound = false;
 
 		// Look for data block
 		if (dataBlockIndexMap.containsKey(key)) {
 			isFound = true;
 		}
 
 		// Return status
 		return isFound;
 	}
 
 
 	/**
 	 *	Determines if the specified intersection exists in the data cache
 	 *
 	 * @param intersection Cell intersection
 	 * @return True if the specified intersection exists in the data cache
 	 * 
 	 * @throws PafException 
 	 */
 	public boolean isExistingIntersection(Intersection intersection) throws PafException {
 
 		boolean isFound = false;
 
 		// An intersection exists if its coordinates are valid and its corresponding
 		// data block exists. 
 
 		// Get the data block key (this step also validates the measure and time members)
 		DataCacheCellAddress cellAddress = generateCellAddress(intersection);
 		
 		// Check if the data block exists
 		if (isExistingDataBlock(cellAddress.getDataBlockKey())) {
 			isFound = true;
 		}
 
 		// Return status
 		return isFound;
 	}
 
 	/**
 	 *	Determines if the specified base intersection exists in the data cache
 	 *
 	 * @param intersection Cell intersection coordinates
 	 * @return True if the specified intersection exists in the data cache
 	 * 
 	 * @throws PafException 
 	 */
 	public boolean isExistingBaseIntersection(String[] baseCoords) throws PafException {
 
 		boolean isFound = false;
 
 		// An intersection exists if its coordinates are valid and its corresponding
 		// data block exists. 
 
 		// Get the data block key (this step also validates the measure and time members)
 		DataCacheCellAddress cellAddress = generateBaseCellAddress(baseCoords);
 		
 		// Check if the data block exists
 		if (isExistingDataBlock(cellAddress.getDataBlockKey())) {
 			isFound = true;
 		}
 
 		// Return status
 		return isFound;
 	}
 
 
 	/**
 	 *	Determines if the specified key represents a valid data block. This 
 	 *  does not necessarily mean that the data block exists in the data cache.
 	 *
 	 * @param key Data block key
 	 * @return True if the specified intersection is a valid data cache intersection
 	 */
 	public boolean isValidDataBlock(Intersection key) {
 
 		int minKeyDims = coreDimensions.size() - NON_KEY_DIM_COUNT;
 		int maxKeyDims = allDimensions.size() - NON_KEY_DIM_COUNT;
 //		String yearDim = this.getYearDim(), defaultTimeHorizonYear = TimeSlice.getTimeHorizonYear();
 
 		
 		// Key can't be null
 		if (key == null) {
 			return false;
 		}
 		
 		// Check data block key size. The data block key must be big enough
 		// all core data cache key dimensions, but can't be bigger than the 
 		// number allowable key dimensions.
 		int keyDimCount = key.getDimensions().length;
 		if (keyDimCount < minKeyDims || keyDimCount > maxKeyDims ) {
 			return false;
 		}
 		
 		// Core key validation (check the required key dimensions and
 		// coordinates)
 		for (int i = 0; i < minKeyDims; i++) {
 			
 			// Lookup the axis number
 			int axis = coreKeyAxes[i];
 
 			// Validate dimension name and dimension order
 			String dim = key.getDimensions()[i];
 			if (!dim.equals(this.getDimension(axis))) {
 				return false;
 			}
 
 			// Validate member name. 
 			String member = key.getCoordinate(i);
 //			if (!dim.equals(yearDim)) {
 				if (!this.isMember(dim, member)) {
 					return false;
 				} 
 //			// Year Dim
 //			} else if (!member.equals(defaultTimeHorizonYear)){
 //				return false;
 //			}
 		}
 
 
 
 		
 		// Check any remaining key dimensions to ensure that they are valid
 		// dimensions and that each corresponding coordinate is mapped to a
 		// valid member
 		for (int i = minKeyDims; i < keyDimCount; i++) {
 
 			// Validate dimension name and dimension order
 			String dim = key.getDimensions()[i];
 			if (!nonCoreDims.contains(dim)) {
 				return false;
 			}
 			
 			// Validate member name
 			String member = key.getCoordinate(i);
 			if (!this.isMember(dim, member)) {
 				return false;
 			}
 						
 		}
 
 		// Return status
 		return true;
 	}
 
 	/**
 	 *	Determines if the specified intersection is a valid data cache intersection. This
 	 *  does not necessarily mean that the intersection exists in the data cache.
 	 *  
 	 *  This method does not check for attribute intersection validity
 	 *
 	 * @param intersection Cell intersection
 	 * @return True if the specified intersection is a valid data cache intersection
 	 */
 	public boolean isValidIntersection(Intersection intersection) {
 
 		// Assume that intersection is not null
 		
 		// Check intersection size. The intersection must be big enough to fit 
 		// all core data cache dimensions, but can't be bigger than the number
 		// of allowable dimensions.
 		int isDimCount = intersection.getDimensions().length;
 		if (isDimCount < coreDimensions.size() || isDimCount > allDimensions.size()) {
 			return false;
 		}
 		
 		// Check core dimensions in sequence by position
 		for (int axis = 0; axis < coreDimensions.size(); axis++) {
 			
 			// Validate dimension name and dimension order
 			String dim = intersection.getDimensions()[axis];
 			if (!dim.equals(this.getDimension(axis))) {
 				return false;
 			}
 			
 			//Validate member name
 			String member = intersection.getCoordinate(axis);
 			if (!this.isMember(dim, member)) {
 				return false;
 			}
 		}
 
 		// Check remaining dimensions to ensure that they are valid 
 		// and that each corresponding coordinate is mapped to a valid
 		// member
 		for (int i = coreDimensions.size(); i < isDimCount; i++) {
 
 			// Validate dimension name and dimension order
 			String dim = intersection.getDimensions()[i];
 			if (!nonCoreDims.contains(dim)) {
 				return false;
 			}
 			
 			// Validate member name
 			String member = intersection.getCoordinate(i);
 			if (!this.isMember(dim, member)) {
 				return false;
 			}
 						
 		}
 
 		
 		// Lastly, check for valid time horizon coordinate (TTN-1595)
 		if (!this.hasValidTimeHorizonCoord(intersection)) {
 			return false;
 		}
 		
 
 		// Passed all checks - must be valid
 		return true;
 	}
 
 	/**
 	 *	Determines if the specified intersection is a valid data cache intersection. This
 	 *  does not necessarily mean that the intersection exists in the data cache.
 	 *  
 	 *  This method does not check for attribute intersection validity
 	 *
 	 * @param baseCoords Non-attribute Cell intersection coordinates
 	 * @return True if the specified intersection is a valid data cache intersection
 	 */
 	public boolean isValidIntersection(String[] baseCoords) {
 
 		// Assume that intersection is not null
 		
 		// Check intersection size 
 		int isDimCount = baseCoords.length;
 		if (isDimCount != coreDimensions.size()) {
 			return false;
 		}
 		
 		// Check core dimension members in sequence by position
 		for (int axis = 0; axis < coreDimensions.size(); axis++) {
 			
 			// Get base dimension
 			String dim = this.getDimension(axis);
 			
 			//Validate member name
 			String member = baseCoords[axis];
 			if (!this.isMember(dim, member)) {
 				return false;
 			}
 		}
 
 		
 		// Lastly, check for valid time horizon coordinate (TTN-1595)
 		if (!this.hasValidTimeHorizonCoord(baseCoords)) {
 			return false;
 		}
 		
 
 		// Passed all checks - must be valid
 		return true;
 	}
 
 	
 	
 	/**
 	 *	Validate member filter used for retrieving filtered data cache data
 	 *
 	 * @param memberFilter Map comprised of member lists for each filtered dimension
 	 * @throws IllegalArgumentException
 	 */
 	public void validateMemberFilter(Map<String, List<String>> memberFilter) {
 		for (String dimension:memberFilter.keySet()) {
 			List<String> members = memberFilter.get(dimension);
 			if (members == null || members.size() == 0) {
 				// Empty member list - throw IllegalArgumentException
 				String errMsg = "Member filter error - null or empty member list specifed on filtered dimension: ["
 					+ dimension + "]";
 				logger.error(errMsg);
 				throw (new IllegalArgumentException(errMsg));
 			}
 		}		
 	}
 
 
 	/**
 	 * @return the isDirty
 	 */
 	public boolean isDirty() {
 		return isDirty;
 	}
 
 
 	/**
 	 * @param isDirty the isDirty to set
 	 */
 	public void setDirty(boolean isDirty) {
 		this.isDirty = isDirty;
 	}
 
 	/**
 	 * Set dirty flag
 	 */
 	public void setDirty() {
 		setDirty(true);
 	}
 	
 	/**
 	 * Clear dirty flag
 	 */
 	public void clearDirty() {
 		setDirty(false);
 	}
 	
 
 	/**
 	 * @return the componentBaseMemberMap
 	 */
 	public Map<Intersection, List<String>> getComponentBaseMemberMap() {
 		return componentBaseMemberMap;
 	}
 
 	/**
 	 *	Get component base members for the specified intersection
 	 *
 	 * @param baseMemberIs Base member intersection
 	 * @return List<String>
 	 */
 	public List<String> getComponentBaseMembers(Intersection baseMemberIs) {
 
 		List<String> componentMembers = null;
 		if (componentBaseMemberMap.containsKey(baseMemberIs)) {
 			componentMembers = componentBaseMemberMap.get(baseMemberIs);
 		} else {
 			componentMembers = new ArrayList<String>();
 		}
 		return componentMembers;
 	}
 
 
 	/**
 	 *	Add component members for the specified base member intersection
 	 *
 	 * @param baseMemberIs Base member intersection
 	 * @param componentMembers Component base members
 	 */
 	public void addComponentBaseMembers(Intersection baseMemberIs, List<String> componentMembers) {
 
 		// Add new collection
 		componentBaseMemberMap.put(baseMemberIs, componentMembers);
 
 	}
 
 	
 	/**
 	 * @param attrIs Attribute intersection
 	 * @return True if the the attribute intersection is invalid
 	 */
 	public boolean isInvalidAttributeIntersection(Intersection attrIs) {
 		return !isValidAttributeIntersection(attrIs);
 	}
 
 
 	/**
 	 * Returns any attribute dimensions in the specified cell intersection
 	 * 
 	 * @param cellIs Cell intersection
 	 * @return Array of attribute dimensions
 	 */
 	private String[] getAttributeDims(Intersection cellIs) {
 		
 		int attrDimCount = cellIs.getSize() - getBaseDimCount();
 		String[] attributeDims = new String[attrDimCount];
 		if (attrDimCount > 0) {
 			System.arraycopy(cellIs.getDimensions(), getBaseDimCount(), attributeDims, 0, attrDimCount);
 		}
 
 		return attributeDims;
 	}
 
 
 	/**
 	 * @param attrIs Attribute intersection
 	 * @param attrDimNames Attribute dimension names
 	 * 
 	 * @return True if the the attribute intersection is invalid
 	 */
 	public boolean isInvalidAttributeIntersection(Intersection attrIs, String[] attrDimNames) {
 		return !isValidAttributeIntersection(attrIs, attrDimNames);
 	}
 
 
 	/**
 	 * Determines if the specified attribute intersection is valid
 	 * 
 	 * @param attrIs Attribute intersection
 	 * @return True if the intersection represents a valid attribute intersection
 	 */
 	public boolean isValidAttributeIntersection(Intersection attrIs) {
 		String[] attrDimNames = getAttributeDims(attrIs);
 		return isValidAttributeIntersection(attrIs, attrDimNames);		
 	}
 
 	
 	/**
 	 * Determines if the specified attribute intersection is valid
 	 * 
 	 * @param attrIs Attribute intersection
 	 * @param attrDimNames Attribute dimension names
 	 * 
 	 * @return True if the intersection represents a valid attribute intersection
 	 */
 	public boolean isValidAttributeIntersection(Intersection attrIs, String[] attrDimNames) {
 		
 		boolean isValid = true;
 		
 		// Select each base dimension with attributes and verify each 
 		// corresponding attribute combination
 		final List<String> attrDimList = Arrays.asList(attrDimNames);
 		final Set<String> baseDimNames = getBaseDimNamesWithAttributes();
 		validation:
 			for (String baseDimName : baseDimNames) {
 	
 				PafBaseTree baseTree = (PafBaseTree) dimTrees.getTree(baseDimName);
 				Set<String> assocAttrDimSet =  new HashSet<String>(baseTree.getAttributeDimNames());
 				assocAttrDimSet.retainAll(attrDimList);
 				if (!assocAttrDimSet.isEmpty()) {
 					String baseMemberName = attrIs.getCoordinate(axisIndexMap.get(baseDimName));
 					String[] assocAttrDims = assocAttrDimSet.toArray(new String[0]);
 					String[] attrCombo = attrIs.getCoordinates(assocAttrDims);
 					if (!AttributeUtil.isValidAttributeCombo(baseDimName, baseMemberName,
 							assocAttrDims, attrCombo, dimTrees)) {
 						isValid = false;
 						break validation;
 					}
 				}
 			}
 	
 		return isValid;
 	}
 
 
 	/*
 	 *	Represent the DataCache as a 2-dimensional array of data cells
 	 *
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 
 		StringBuffer stringBuffer = new StringBuffer("\n");	
 
 		for (DataBlock dataBlock : dataBlockPool) {
 			stringBuffer.append(dataBlock.toString());
 		}
 		return stringBuffer.toString();
 	}
 
 
 }
