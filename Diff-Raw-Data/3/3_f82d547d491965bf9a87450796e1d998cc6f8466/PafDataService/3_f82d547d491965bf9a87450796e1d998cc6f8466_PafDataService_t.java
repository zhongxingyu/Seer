 /*
  *  File: @(#)PafDataService.java   Package: com.pace.base.server  Project: PafServer
  *  Created: Jun 26, 2005        By: JWatkins
  *  Version: x.xx
  *
  *  Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *  This software is the confidential and proprietary information of Palladium Group, Inc.
  *  ("Confidential Information"). You shall not disclose such Confidential Information and 
  *  should use it only in accordance with the terms of the license agreement you entered into
  *  with Palladium Group, Inc.
  *
  *
  *
  Date            Author          Version         Changes
  xx/xx/xx        xxxxxxxx        x.xx            ..............
  * 
  */
 package com.pace.server;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrHandler;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.*;
 import com.pace.base.comm.EvaluateViewRequest;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.data.ExpOperation;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.PafDataSlice;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.db.Application;
 import com.pace.base.db.SecurityGroup;
 import com.pace.base.mdb.*;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.state.EvalState;
 import com.pace.base.state.PafClientState;
 import com.pace.base.state.SliceState;
 import com.pace.base.utility.*;
 import com.pace.base.view.*;
 import com.pace.server.eval.IEvalStrategy;
 import com.pace.server.eval.RuleBasedEvalStrategy;
 
 
 /**
  * This class is the gate keeper to all data services. It holds a 
  * hash table of data caches by client id.
  *
  * @version x.xx
  * @author JWatkins
  *
  */
 
 public class PafDataService {
 	private static Logger logger = Logger.getLogger(PafDataService.class);
 	private static Logger evalPerfLogger = Logger.getLogger(PafBaseConstants.PERFORMANCE_LOGGER_EVAL);
 
 	private static PafDataService _instance = null;
 	private ConcurrentHashMap <String, PafDataCache> uowCache = new ConcurrentHashMap <String, PafDataCache>();
 	private Map <String, PafBaseTree> baseTrees = new HashMap<String, PafBaseTree>();
 	private Map <String, PafAttributeTree> attributeTrees = new HashMap<String, PafAttributeTree>();
 //	private HashMap<String, HashMap<String, Integer>> memberIndexLists = new HashMap<String, HashMap<String, Integer>>();
 //	private Map <String, HashMap<String,PafBaseTree>> uowCacheSubTrees = new HashMap<String, HashMap<String,PafBaseTree>>();
 	private Map <String, Set<Intersection>> systemLockedIntersections = new HashMap<String, Set<Intersection>> ();
 
 	/**
 	 *	Load uow cache for selected client state
 	 *
 	 * @param clientState Client state
 	 * 
 	 * @throws PafException
 	 */
 	public void loadUowCache(PafClientState clientState) throws PafException {
 		
 		String clientId = clientState.getClientId();
 		PafApplicationDef appDef = clientState.getApp();
 		UnitOfWork uow = clientState.getUnitOfWork();
 		
 		logger.info("Loading uow cache for client: " + clientId);
 		logger.info("Unit of Work: " + uow.toString() );
 
 		// at this point save the connection props for this application id into the client state
 		String dsId = appDef.getMdbDef().getDataSourceId();
 		IPafConnectionProps connProps = (IPafConnectionProps) PafMetaData.getAppContext().getBean(dsId);
 		clientState.getDataSources().put(dsId, connProps);
 
 //		TTN-1406
 //		IMdbClassLoader mdbClassLoader = this.getMdbClassLoader(connProps);
 //		IMdbData mdbData = mdbClassLoader.getMdbDataProvider();
 		IMdbData mdbData = getMdbDataProvider(connProps);
 		
 		// Determine which data intersections to extract for each version
 		Map<String, Map<Integer, List<String>>> mdbDataSpecByVersion = buildUowLoadDataSpec(uow, clientState);
 		
 		// Load data cache
 		logger.info("Building the unit of work...");
 		Map<String, Set<String>> lockedPeriodMap = PafAppService.getInstance().getLockedPeriodMap(clientState);
 //		clientState.setLockedPeriodMap();
 		PafDataCache dataCache = new PafDataCache(clientState, lockedPeriodMap);
 		List<String>loadedVersions = mdbData.updateDataCache(dataCache, mdbDataSpecByVersion);
 		logger.info("UOW intialized with version(s): " + StringUtils.arrayListToString(loadedVersions));
 		uowCache.put(clientId, dataCache);           
 
 		// Intialize attribute eval initialization property
 		for (PafMVS pafMVS : clientState.getAllMVS()) {
 			pafMVS.setInitializedForAttrEval(false);
 		}
 		
 		
 		logger.info("Data cache loaded, cached object count: " + uowCache.size());
 
 	}
 
 
 	/**
 	 * 	Determine which data intersections need to be extracted from the multidimensional
 	 * 	database when initially loading the unit of work.
 	 * 
 	 * @param expandedUowSpec Expanded UOW specification
 	 * @param clientState Client State
 	 * 
 	 * @return Map<String, Map<Integer, List<String>>>
 	 */
 	private Map<String, Map<Integer, List<String>>> buildUowLoadDataSpec(UnitOfWork expandedUowSpec, PafClientState clientState) {
 
 		Map<String, Map<Integer, List<String>>> mdbDataSpecByVersion = new HashMap<String, Map<Integer, List<String>>>();
 		String versionDim = clientState.getMdbDef().getVersionDim();
 		int versionAxis = expandedUowSpec.getDimIndex(versionDim); 
 		List<String> uowVersions = Arrays.asList(expandedUowSpec.getDimMembers(versionDim));
 		List<String> extractedVersions = new ArrayList<String>();
 
 
 		// Check each version to see if they should be extracted from the mdb. Optimally, only the active planning
 		// version needs to be selected during the initial UOW load. 
 		
 
 		// First select the active planning version
 		String planVersion = clientState.getPlanningVersion().getName();
 		extractedVersions.add(planVersion);
 		
 		// Add any reference versions used in default evaluation
 		PafPlannerConfig plannerConfig = clientState.getPlannerConfig();
 		String[] evalRefVersions = plannerConfig.getDefaultEvalRefVersions();
     	if (evalRefVersions != null && evalRefVersions.length> 0) {
     		List<String> evalRefVersionList = new ArrayList<String>(Arrays.asList(plannerConfig.getDefaultEvalRefVersions()));
      		extractedVersions.addAll(evalRefVersionList);
     	}
 		
     	
     	// Filter out any versions not in the uow (to be safe)
     	extractedVersions.retainAll(uowVersions);
     	
     	// Add a data spec entry for each version being extracted that selects all uow
     	// intersectons for that version
 		for (String version : extractedVersions) {
 			Map<Integer, List<String>> mdbDataSpec = expandedUowSpec.buildUowMap();
 			mdbDataSpec.put(versionAxis, new ArrayList<String>(Arrays.asList(new String[]{version})));
 			mdbDataSpecByVersion.put(version, mdbDataSpec);
 		}
 		return mdbDataSpecByVersion;
 	}
 
 
 
 	/**
 	 * 	Refresh selected versions of the client's current uow cache. If a null 
 	 * 	or empty version filter is specified, then all versions will be updated.
 	 * 
 	 * @param clientState Client state
 	 * @param appDef Application definition
 	 * @param expandedUow Expanded unit of work
 	 * @param versionFilter Versions to update
 	 *
 	 * @return List of updated versions
 	 * @throws PafException 
 	 */
 	public List<String> refreshUowCache(PafClientState clientState,PafApplicationDef appDef, UnitOfWork expandedUow, 
 			List <String> versionFilter) throws PafException {
 	
 		List<String> validatedVersionFilter = new ArrayList<String>();
 		List<String> updatedVersions = null;
 	
 		// Get client id
 		String clientId = clientState.getClientId();
 
 		// Get uow cache for current client session
 		logger.info("Updating uow cache for client: " + clientId);
 		PafDataCache cache = uowCache.get(clientId);         
 		if (cache == null) {
 			throw new IllegalArgumentException("Uow cache not found for client: [" + clientId + "]");
 		}
 		
 		// Validate version filter, resolving tokens and case differences
 		if (versionFilter != null && versionFilter.size() > 0) {
 			String planVersion = cache.getPlanVersions()[0];
 			String versionDim = cache.getVersionDim();
 			StringBuffer foundVersion = new StringBuffer(); 
 			for (String version : versionFilter) {
 				// Check for plan version token and resolve it
 				if (version.equalsIgnoreCase(PafBaseConstants.PLAN_VERSION)) {
 					validatedVersionFilter.add(planVersion);
 				} else {
 					// Validate filter version, ignoring case
 					if (cache.isMemberIgnoresCase(versionDim, version, foundVersion)) {
 						validatedVersionFilter.add(foundVersion.toString());
 					} else {
 						throw new IllegalArgumentException("Illegal filter version: [" + version + "] is not contained in the unit of work.");
 					}
 				}
 			}
 		} else {
 			// Empty filter - retrieve all non-derived versions (plan version + reference versions)
 			versionFilter = cache.getBaseVersions();
 		}
 		
 		// Get mdb data provider corresponding to application data source id
 		String dsId = appDef.getMdbDef().getDataSourceId();
 		IPafConnectionProps connProps =	clientState.getDataSources().get(dsId);
 		IMdbData mdbData = getMdbDataProvider(connProps);
 // 		TTN-1406
 //		IMdbClassLoader mdbClassLoader = this.getMdbClassLoader(connProps);
 //		IMdbData mdbData = mdbClassLoader.getMdbDataProvider();
 		
 
 		// Determine which data intersections need to extracted for the versions being refreshed. Start
 		// out with the data spec that covers all data needed for the initial UOW load and filter down
 		// to just the versions being refreshed.
 		Map<String, Map<Integer, List<String>>> mdbDataSpec = buildUowLoadDataSpec(clientState.getUnitOfWork(), clientState);
 		Set<String> dataSpecVersions = mdbDataSpec.keySet();
 		for (String dataSpecVersion : dataSpecVersions) {
 			if (!validatedVersionFilter.contains(dataSpecVersion)) {
 				mdbDataSpec.remove(dataSpecVersion);
 			}
 		}
 		
 		// Load mdb data for the required intersections. Any refreshed versions not in the data spec
 		// will be loaded as needed during view rendering or evaluation.
 		updatedVersions = mdbData.refreshDataCache(cache, mdbDataSpec, validatedVersionFilter);
 		logger.info("Data cache updated for versions: " + StringUtils.arrayListToString(updatedVersions));
 		logger.info("Cached object count: " + uowCache.size());
 
 		return updatedVersions;
 	}
 
 	/**
 	 *	Update the uow cache from the mdb for the specified versions
 	 *
 	 * @param clientState Client State	
 	 * @param dataCache Data cache
 	 * @param versionFilter List of versions to update
 	 * 
 	 * @throws PafException 
 	 */
 	public void updateDataCacheFromMdb(PafClientState clientState, PafDataCache dataCache, List<String> versionFilter) throws PafException {
 
 		// Get mdb data provider corresponding to application data source id
 		String dsId = clientState.getApp().getMdbDef().getDataSourceId();
 		IPafConnectionProps connProps =	clientState.getDataSources().get(dsId);
 		IMdbData mdbData = getMdbDataProvider(connProps);
 
 		// Refresh filtered versions
 		List<String> updatedVersions = mdbData.updateDataCache(dataCache, clientState.getUnitOfWork(), versionFilter);
 		logger.info("Data cache updated for versions: " + StringUtils.arrayListToString(updatedVersions));
 		
 	}
 
 	
 	/**
 	 * 	Update the data cache with the contents of the data slice
 	 * 
 	 * @param dataCache Data Cache
 	 * @param pafDataSlice Paf Data Slice
 	 * @param parms Object containing required PafDataSlice parameters
 	 * @param dimSequence Dimension sequence for data cache intersections associated with the corresponding view section
 	 * 
 	 * @throws PafException
 	 */
 	public void updateDataCacheFromSlice(PafDataCache dataCache, PafDataSlice pafDataSlice, PafDataSliceParms parms) throws PafException {
 
 		boolean hasPageDimensions = false;
 		int cols = 0, rows = 0;
 		double[] dataSlice = null;
 		String[] rowDims = parms.getRowDimensions(), colDims = parms.getColDimensions();
 		String[][] rowTuples = parms.getRowTuples(), colTuples = parms.getColTuples();
 		String [] attributeDims = parms.getAttributeDims();
 		boolean hasAttributes = false;
 
 		// Had to move this method over from the PafDataCache object so I could get access
 		// to the "isValidAttributeIntersection()" method (AF - 8/23/2011)
 		//TODO If possible, move this this method and all the attribute validation methods to PafDataCache
 		// --- Might not be possible because of data filtering calls in PafServiceProvider.startPlanSession()
 		
 		try {
 			// Validate data slice parms
 			logger.info("Validating PafDataSlice parameters...");
 			hasPageDimensions = dataCache.validateDataSliceParms(parms);
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
 					// invalid attribute intersections
 					if (!hasAttributes || dataCache.isValidAttributeIntersection(cellIs, attributeDims)) {
 						dataCache.setCellValue(cellIs, dataSlice[sliceIndex]);
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
 	 *	Expand out the members in a unit of work using the base trees
 	 *
 	 * @param uow Unit of work object
 	 * @param clientState Client state object
 	 * 
 	 * @return Hash Map containing the expanded members for each dimension
 	 * @throws PafException 
 	 */
 	private Map<Integer, List<String>> expandUow(UnitOfWork uow, PafClientState clientState) throws PafException {
 
 		int axis = 0;
 		String versionDim = clientState.getApp().getMdbDef().getVersionDim();
 		String[] terms = null;
 		Map<Integer, List<String>> expandedUow = new HashMap<Integer, List<String>>();
 
 		// Get the list of expanded members for each dimension
 		for (String dim : uow.getDimensions() ) {
 			terms = uow.getDimMembers(dim);
 			List<String> members = new ArrayList<String>();
 			for (String term : terms) {
 				members.addAll(Arrays.asList(expandExpression(term, true, dim, null)));
 			}
 			// Special logic for version dimension - filter out version dimension root
 			if (dim.equalsIgnoreCase(versionDim)) {
 				members.remove(versionDim);
 			}
 			expandedUow.put(axis++, members);
 		}
 		return expandedUow;
 	}
 	
 	
 	/**
 	 *	Expand out the members in a unit of work uing the base trees
 	 *
 	 * @param uow Unit of work object
 	 * @param clientState Client state object
 	 * 
 	 * @return UnitOfWork containing the expanded members for each dimension
 	 * @throws PafException 
 	 */
 	public UnitOfWork expandUOW(UnitOfWork uow, PafClientState clientState) throws PafException{
 		//Call expandUOW
 		Map<Integer, List<String>> expandedUow = this.expandUow(uow, clientState);
 		
 		String[] dimensions = new String[expandedUow.size()];
 		String[][] dimensionMembers = new String[expandedUow.size()][];
 
 		for(Integer axisIndex : expandedUow.keySet()){
 			dimensions[axisIndex] = uow.getAxisIndices().get(axisIndex);
 			
 			dimensionMembers[axisIndex] = expandedUow.get(axisIndex).toArray(new String[0]);
 		}
 
 		return new UnitOfWork(dimensions, dimensionMembers);
 	}
 
 	public PafBaseMember getUowRoot(PafClientState clientState, String dim, UnitOfWork uow) {
 		TreeMap<Integer, ArrayList<PafBaseMember>> treeMap = getMembersByLevel(clientState, dim);
 		PafBaseMember root = treeMap.get(treeMap.lastKey()).get(0);
 		return root;
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param clientId 
 	 * @param dim 
 	 * 
 	 * @return TreeMap<Integer, ArrayList<PafBaseMember>>
 	 */
 	public TreeMap<Integer, ArrayList<PafBaseMember>> getMembersByLevel(PafClientState clientState, String dim) {
 
 		TreeMap<Integer, ArrayList<PafBaseMember>> membersByLevel = new TreeMap<Integer, ArrayList<PafBaseMember>>();
 		PafBaseMember memberDtl;
 		int lvl;
 
 		//PafUowCache cache = uowCache.get(clientId);
 		PafBaseTree dimTree = baseTrees.get(dim);
 
 		//String[] dimMembers = cache.getAxisMembers(cache.getAxisIndex(dimTree.getRootNode().getKey()));
 		String[] dimMembers = clientState.getUnitOfWork().getDimMembers(dimTree.getRootNode().getKey());
 
 		for (String member : dimMembers) {
 			memberDtl = dimTree.getMember(member);
 			lvl =  memberDtl.getMemberProps().getLevelNumber();
 			if (!membersByLevel.containsKey(lvl)) {
 				membersByLevel.put(lvl, new ArrayList<PafBaseMember>());
 			}
 			membersByLevel.get(lvl).add(memberDtl);  
 		}       
 
 		return membersByLevel;
 	}
 
 	/**
 	 *  Return a tree map of members organized by generation
 	 *  
 	 * @param dim UOW Dimension
 	 * @param dimMembers UOW dimension members
 	 * @param isVersionDim Indicates if selected dimension is the "Version" dimension.
 	 *
 	 * @return TreeMap<Integer, ArrayList<PafBaseMember>>
 	 * @throws PafException 
 	 */
 	private TreeMap<Integer, List<PafBaseMember>> getMembersByGen(String dim, String[] dimMembers, MdbDef mdbDef ) throws PafException {
 
 		TreeMap<Integer, List<PafBaseMember>> membersByGen = new TreeMap<Integer, List<PafBaseMember>>();
 		PafBaseTree dimTree = null;
 		PafBaseMember memberDtl = null;
 		DimType dimType = null;
 		int gen = 0;
 
 		// Determine the dimension type (TTN-1347)
 		dimType = mdbDef.getDimType(dim);
 
 		// Get base tree for selected dimension in order to look up generation
 		// property by member. 
 		dimTree = baseTrees.get(dim);
 		
 		// To support shared hierarchies, we need to get a subtree that only contains
 		// the branch that is part of the selected UOW. We need to weed out multiple
 		// occurrences of the same member, so that we can get the correct generation
 		// properties of the members that make up the UOW. (TTN-1347)
 		//
 		// This logic only applies to hierarchy, measure, or time dimensions that
 		// contain shared members.
 		//
 		if (dimTree.hasSharedMembers() &&
 				(dimType == DimType.Hier || dimType == DimType.Measure || dimType == DimType.Time) ) {
 			
 			// Determine which dimension member is the intended root of the UOW branch
 			String root = null;
 			if (dimType != DimType.Measure) {
 				// Look for candidate root by first discounting any shared members
 				Set<String> candidateRoots = new HashSet<String>(Arrays.asList(dimMembers));
 				Set<String> sharedMemberNames = dimTree.getSharedMemberNames();
 				candidateRoots.removeAll(sharedMemberNames);
 
 				// Root is the non-shared member with lowest generation (if all UOW members
 				// in dimension are shared then no pruning is needed).
 				int lowestGen = 9999;
 				for (String candidateRoot : candidateRoots) {
 					PafDimMember candidateRootMember = dimTree.getMember(candidateRoot);
 					int candidateGen = candidateRootMember.getMemberProps().getGenerationNumber();
 					if (candidateGen  < lowestGen) {
 						lowestGen = candidateGen;
 						root = candidateRoot;
 					}
 				}
 
 			} else {
 				// Measure dimension - use the measure root
 				root = mdbDef.getMeasureRoot();
 			}
 
 			// Prune tree (if root was found)
 			if (root != null) {
 				dimTree = dimTree.getSubTreeCopy(root);
 			}
 		}
 		
 		// Build tree map
 		for (String member : dimMembers) {
 			memberDtl = dimTree.getMember(member);
 			gen =  memberDtl.getMemberProps().getGenerationNumber();
 			if (!membersByGen.containsKey(gen)) {
 				membersByGen.put(gen, new ArrayList<PafBaseMember>());
 			}
 			membersByGen.get(gen).add(memberDtl);  
 		}       
 
 		// "Special" logic for version dimension. Add in version dimension root 
 		//  as it is not contained in the data cache
 		if (dimType == DimType.Version) {
 			PafBaseMember versionRoot = dimTree.getRootNode();
 			gen = versionRoot.getMemberProps().getGenerationNumber();
 			membersByGen.put(gen, new ArrayList<PafBaseMember>());
 			membersByGen.get(gen).add(versionRoot);  
 		}
 
 		// Return generation map
 		return membersByGen;
 	}    
 
 
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param clientId
 	 * @return Set<Intersection>
 	 */
 	public Set<Intersection> getSystemLockedIntersections(String clientId) {
 		return systemLockedIntersections.get(clientId);
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param clientId
 	 * @param isSet
 	 */
 	public void setSystemLockedIntersections(String clientId, Set<Intersection> isSet) {
 		systemLockedIntersections.put(clientId, isSet); 
 	}
 
 
 	/**
 	 *	Creates a copy of a tree based upon the UOW specification
 	 *
 	 * @param clientId
 	 * @param dim
 	 * @param optionalWorkUnit
 	 * @return PafBaseTree
 	 * @throws PafException 
 	 */
 	private PafBaseTree calculateDataCacheSubTree(PafClientState clientState, String dim, UnitOfWork optionalWorkUnit) throws PafException {
 
 		PafBaseTree baseTree, copy;
 		MdbDef mdbDef = clientState.getMdbDef();
 		String measureDim = mdbDef.getMeasureDim();
 		String versionDim = mdbDef.getVersionDim();
 		String yearDim = mdbDef.getYearDim();
 		String[] mbrNames = null;
 		
 		//Get the dimension members.  Use the optional workUnit parameter if it is not null
 		if(optionalWorkUnit != null){
 			mbrNames = optionalWorkUnit.getDimMembers(dim);
 		}
 		else{
 			mbrNames = clientState.getUnitOfWork().getDimMembers(dim);
 		}
 
 		// Version dimension special logic - Prune out any shared members and versions
 		// not contained in the version filter. 
 		baseTree = baseTrees.get(dim);
 		if (dim.equalsIgnoreCase(versionDim)) {
 			
 			//get version filters from client state
 			String[] versionFilters = clientState.getPlannerConfig().getVersionFilter();
 
 			//get tree
 			TreeMap<Integer, List<PafBaseMember>> treeMap = getMembersByGen(dim, mbrNames, mdbDef);
 			PafBaseMember root = treeMap.get(treeMap.firstKey()).get(0);
 			PafBaseTree tree = baseTree.getSubTreeCopyByGen(root.getKey(), root.getMemberProps().getLevelNumber() + 1);
 			
 			//remove any shared members / branches
 			List<PafDimMember> members = tree.getMembers(TreeTraversalOrder.PRE_ORDER);
 			for (PafDimMember member : members) {
 				if (member.isShared()) {
 					tree.removeBranch(member);
 				}
 			}
 			
 			//if version filters is not null and versions exists
 			if ( versionFilters != null && versionFilters.length > 0) {
 				
 				//create version Filter set
 				Set<String> versionFilterSet = new HashSet<String>(Arrays.asList(versionFilters));
 	
 				//get all descendants of tree from level0 -> level #
 				ArrayList<PafDimMember> descendantsOfTree = tree.getIDescendants(root.getKey(), false);
 							
 				//for each member, see if in version filter set, if not and doesn't have children remove it
 				for ( PafDimMember branchMember : descendantsOfTree ) {
 					String branchMemberName = branchMember.getKey();					
 					if ( ! versionFilterSet.contains(branchMemberName)) {					
 						if ( ! branchMember.hasChildren() ) {						
 							tree.removeBranch(branchMemberName);				
 						}						
 					}				
 						}
 					}
 					
 			// Return version tree
 			return tree;
 		}
 		
 		// Special year dimension logic for multiple year UOW (TTN-1595).
 		if (dim.equals(yearDim) && mbrNames.length > 1) {
 
 			// Create virtual root
 			PafBaseMember root = baseTree.getRootNode().getShallowDiscCopy();
 			PafBaseMemberProps rootProps = root.getMemberProps();
 			String rootDesc = "**" + root.getKey() + "**";
 			for (String aliasTableName : baseTree.getAliasTableNames()) {
 				rootProps.addMemberAlias(aliasTableName, rootDesc);
 			}
 			rootProps.setVirtual(true);
 			
 			// Create year sub tree and add in UOW years
 			List<String> yearList = new ArrayList<String>(Arrays.asList(mbrNames));
 			yearList.remove(root.getKey());
 			PafBaseTree yearTree = new PafBaseTree(root, baseTree.getAliasTableNames());
 			for (String year : yearList) {
 				PafBaseMember yearMember = baseTree.getMember(year).getShallowDiscCopy();
 				PafBaseMemberProps memberProps = yearMember.getMemberProps();
 				memberProps.setGenerationNumber(2);
 				yearTree.addChild(root, yearMember);
 			}
 						
 			return yearTree;
 		}
 
 		// All other dimensions - Start out by making a tree copy
 		SortedMap<Integer, List<PafBaseMember>> treeMap = getMembersByGen(dim, mbrNames, mdbDef);
 		PafBaseMember root = treeMap.get(treeMap.firstKey()).get(0);
 		if (baseTree.hasSharedMembers()) {
 			// Shared members exist, get whole branch since generations on 
 			// shared members may be higher than original member
 			copy = baseTree.getSubTreeCopy(root.getKey());	
 		} else {
 			// No shared members, safely pull down to highest generation 
 			// in the UOW
 			copy = baseTree.getSubTreeCopyByGen(root.getKey(), treeMap.lastKey());			
 		}
 
 		// build list of members in the cache, use hash set for quick find
 		List<String>cacheMbrs = new ArrayList<String>(); 
 		cacheMbrs.addAll(Arrays.asList(mbrNames));
 
 //		// build copy of tree members for traversal, to allow removal
 //		// get copy in generation order to prune from top to bottom
 //		// resolves certain issues with shared members and is more efficient.
 //		List<PafDimMember> treeMbrs = new ArrayList<PafDimMember>();
 //		SortedMap<Integer, List<PafDimMember>>treeGen = copy.getMembersByGen();
 //		
 //		for (int gen : treeGen.keySet() ) {
 //			for (PafDimMember m : treeGen.get(gen)) {
 //				treeMbrs.add(m);
 //			}
 //		}
 
 		// Prune invalid and duplicate members from tree. The member
 		// search list must be initialized via a tree traversal since 
 		// shared members aren't contained in the members hash map or 
 		// generation & level collections. A pre-order traversal is used 
 		// to ensure that we process the nodes in top-down, left-right, order.
 		List<PafDimMember> treeMembers = copy.getMembers(TreeTraversalOrder.PRE_ORDER);
 		Set<String> uniqueMembers = new HashSet<String>();
 		PafDimMember parent;
 		String memberName;
 		for (PafDimMember member : treeMembers ) {
 			// If this is a valid member, leave the member in the tree,
 			// unless it is a duplicate occurrence.
 			memberName = member.getKey();
 			if (cacheMbrs.contains(memberName)) {
 				
 				// Check for discontiguous tree error
 				if (!copy.hasMember(memberName)) {
 					String errMsg = "Error encountered generating the unit of work tree: [" + dim
 						+ "]. The required member: [" + memberName + "] is missing from the uow tree. "
 						+ " One possibility is that a discontiguous [" + dim
 						+ "] tree was defined";
 					if (dim.equalsIgnoreCase(measureDim)) {
 						errMsg += " via Rule Set Measure Filters.";
 					} else {
 						errMsg += ".";
 					}
 					logger.error(errMsg);
 //					throw new PafException(errMsg, PafErrSeverity.Error);
 				}
 				
 				// Check if member is duplicate
 				if (!uniqueMembers.contains(memberName)) {
 					// First occurrence
 					uniqueMembers.add(memberName);
 				} else {
 					// Remove duplicate member / branch
 					parent = member.getParent();
 					copy.removeBranch(member);
 					logger.warn("Duplicate member [" + memberName + "] removed from unit of work tree: " + dim);
 					// Remove any branches that were comprised solely
 					// of duplicates and their ancestors.
 					copy.pruneAncestors(parent);
 				}
 				
 			} else {
 				// Remove invalid member / branch
 				copy.removeBranch(member);
 			}
 			
 		}
 
 				
 		// TODO: Document/rework issues around measures as a tree.
 		// nastiness: the measures dimension allows for a "filtered subset of members in the tree".
 		// this is not normally allowed (for instance you can't have weeks 1,2 and 4 along with January
 		// Fix: if measures dimension prune back off all members that aren't in the current datacache
 //		if (dim.trim().equals(measureDim)) {
 //		String[] cacheMsrs = uowCache.get(clientId).getDimMembers(measureDim);
 //		List<String> treeMsrs = new ArrayList<String>();  
 //		treeMsrs.addAll(Arrays.asList(copy.getMemberKeys()));
 //		List<String> cacheMsrList = Arrays.asList(cacheMsrs);
 //		for (String msrName : treeMsrs) {
 //		if (!cacheMsrList.contains(msrName))
 //		copy.removeBranch(msrName);
 //		}
 //		}        
 
 
 		return copy;
 	}
 
 
 
 	/**
 	 *	Get data cache for select client state id                                                                          
 	 *
 	 * @param clientId Client state id                                                                                                                                
 	 * @return PafDataCache
 	 */
 	public PafDataCache getDataCache(String clientId) {
 		logger.info("Getting data cache for client: " + clientId);    
 		logger.info("Current cached object count: " + uowCache.size());
 		return uowCache.get(clientId);
 	}
 	
 	/**
 	 *	Get trees associated with the UOW                                                                                         
 	 *
 	 * @param clientState Client state id
 	 * @return MemberTreeSet Collection of member trees
 	 * 
 	 * @throws PafException 
 	 */
 	public MemberTreeSet getUowCacheTrees(PafClientState clientState) throws PafException {
 		return getUowCacheTrees(clientState, null);
 	}
 
 	/**
 	 *	Get trees associated with the UOW                                                                                         
 	 *
 	 * @param clientState Client state id
 	 * @param optionalWorkUnit Unit of Work
 	 * 
 	 * @return MemberTreeSet Collection of member trees
 	 * @throws PafException 
 	 */
 	public MemberTreeSet getUowCacheTrees(PafClientState clientState, UnitOfWork optionalWorkUnit) throws PafException {
 
 		//PafUowCache cache  = uowCache.get(clientState.getClientId());
 		MemberTreeSet treeSet = new MemberTreeSet();
 	
 		// Base Dimensions
 		for (String dim : clientState.getUnitOfWork().getDimensions() ) {
 			treeSet.addTree(dim, calculateDataCacheSubTree(clientState, dim, optionalWorkUnit));
 		}
 
 		// Attribute Dimensions
 		treeSet.addAllTrees(attributeTrees);
 
 		// Virtual Time Horizon Dimension
 		PafDimTree timeTree = treeSet.getTree(clientState.getMdbDef().getTimeDim());
 		PafDimTree yearTree = treeSet.getTree(clientState.getMdbDef().getYearDim());
 		treeSet.addTree(PafBaseConstants.TIME_HORIZON_DIM, buildTimeHorizonTree(timeTree, yearTree));
 		
 		return treeSet;
 	}
 
 	/**
 	 * Build the virtual time horizon tree
 	 * 
 	 * @param timeTree UOW time tree
 	 * @param yearTree UOW year tree
 	 * 
 	 * @return Virtual time horizon tree
 	 * @throws PafException 
 	 */
 	protected PafDimTree buildTimeHorizonTree(PafDimTree timeTree, PafDimTree yearTree) throws PafException {
 		
 		PafDimMember timeRoot = timeTree.getRootNode(), yearRoot = yearTree.getRootNode();
 		List<PafDimMember> yearMembers = yearTree.getLowestLevelMembers();
 		List<PafDimMember> timeChildMbrs = null;
 
	if (yearMembers.size() == 1) return	timeTree;  // Don't incur overhead of building tree if single-year UOW
	
 		// The virtual time tree is formed by combining the UOW Time and Year trees. It is used 
 		// in aggregation and any time-based rule function call, instead of the seperate Time 
 		// and Year trees loaded directly from the multidimensional database. The main advantage 
 		// of the virtual time tree is that it makes it easy to index and accumulate time periods
 		// that cross year boundaries.
 		
 		// Create the time horizon tree. The name of the root node will be a combination of
 		// the root of the year tree and the root of the time tree. Mark the new root as
 		// synthetic if either the time root or the year root are synthetic.
 		PafBaseMemberProps rootProps = new PafBaseMemberProps();
 		String rootName = TimeSlice.buildTimeHorizonCoord(timeRoot.getKey(), yearRoot.getKey());
 		PafBaseMember root = new PafBaseMember(rootName, rootProps);		
 		rootProps.addMemberAlias(PafBaseConstants.ESS_DEF_ALIAS_TABLE, root.getKey());
 		rootProps.setGenerationNumber(1);
 		rootProps.setVirtual(yearRoot.getMemberProps().isVirtual() || yearRoot.getMemberProps().isVirtual());
 		
 		// The is some differing logic depending on whether the is just a single year in the UOW
 		// or multiple years, represented by a year tree with a virtual node whose children are 
 		// the individual years.
 		if (yearMembers.size() == 1) {
 			// There is a one-to-one mapping between the members in the time tree and the
 			// members in the time horizon tree.
 			rootProps.setLevelNumber(timeTree.getHighestAbsLevelInTree()); 
 			timeChildMbrs = timeRoot.getChildren();
 		} else {
 			// There time horizon tree contains a node for each combination of year and time 
 			// period, plus the root node which is comprised of the root of the year tree 
 			// and the root of the time tree. 
 			// Since the root of the year tree is synthetic
 			rootProps.setLevelNumber(timeTree.getHighestAbsLevelInTree() + 1); 
 			rootProps.setVirtual(true);
 			timeChildMbrs = new ArrayList<PafDimMember>(Arrays.asList(new PafDimMember[]{timeRoot}));
 		}
 		PafDimTree timeHorizonTree = new PafBaseTree(root, new String[]{PafBaseConstants.ESS_DEF_ALIAS_TABLE});
 		
 		
 		// Build out the rest of the time horizon tree
 		for (PafDimMember yearMember : yearMembers) {
 			// Add child members
 			addTimeHorizonChildren(timeHorizonTree, root, yearMember, timeChildMbrs);
 		}
 		return timeHorizonTree;
 	}
 
 
 	/**
 	 * Add child to virtual time tree
 	 * 
 	 * @param timeHorizonTree Virtual time tree
 	 * @param parentNode Parent node
 	 * @param yearMember Name of year member
 	 * @param timeMembers Time tree node
 	 * 
 	 * @throws PafException 
 	 */
 	private void addTimeHorizonChildren(PafDimTree timeHorizonTree, PafDimMember parentNode, PafDimMember yearMember, List<PafDimMember> timeMembers) throws PafException {
 		
 		for (PafDimMember timeMember : timeMembers) {
 			
 			// Create child member
 			PafBaseMemberProps memberProps = new PafBaseMemberProps();
 			String memberName = TimeSlice.buildTimeHorizonCoord(timeMember.getKey(), yearMember.getKey());
 			PafDimMember timeHorizonChild = new PafBaseMember(memberName, memberProps);
 			memberProps.addMemberAlias(PafBaseConstants.ESS_DEF_ALIAS_TABLE, timeHorizonChild.getKey());
 			memberProps.setLevelNumber(timeMember.getMemberProps().getLevelNumber()); 				// Use level number of time member
 			memberProps.setGenerationNumber(parentNode.getMemberProps().getGenerationNumber() + 1);	// Gen number is parent gen + 1
 
 			// Add child member to time horizon tree 
 			timeHorizonTree.addChild(parentNode, timeHorizonChild);
 
 			// Recursively add a child member to time horizon tree for each child member of the regular time tree
 			addTimeHorizonChildren(timeHorizonTree, timeHorizonChild, yearMember, timeMember.getChildren());
 		}
 	}
 
 
 	/**
 	 *	Get the memberIndexLists for the specified member tree set
 	 *
 	 * @param treeSet Member tree set
 	 * @return Map<String, HashMap<String, Integer>>
 	 */
 	protected Map<String, HashMap<String, Integer>> getUowMemberIndexLists(MemberTreeSet treeSet) {
 		
 		// Create member index lists on each dimension - used to sort allocation
 		// intersections in evaluation processing (TTN-1391)
 		Map<String, HashMap<String, Integer>> memberIndexLists = new HashMap<String, HashMap<String, Integer>>();
 		for (String dim : treeSet.getTreeDimensions()) {
 			PafDimTree uowTree = treeSet.getTree(dim);
 			memberIndexLists.put(dim, createMemberIndexList(uowTree, TreeTraversalOrder.PRE_ORDER));
 		}
 		return memberIndexLists;
 	}
 
 	/**
 	 *	Return the data slice corresponding to the specified view section
 	 *
 	 * @param view Client view
 	 * @param viewSection Client view section
 	 * @param clientState Client state object
 	 * 
 	 * @return Paf Data Slice
 	 * @throws PafException
 	 */
 	public PafDataSlice getDataSlice(PafView view, PafViewSection viewSection, PafClientState clientState, boolean compressData) throws PafException {
 
 		String stepDesc = null;
 		long startTime = 0;
 		PafDataSlice dataSlice = null;
 		PafDataSliceParms sliceParms = null;
 		PafMVS pafMVS = null;
 
 		// If view section is empty, then send back dummy data slice
 		if (viewSection.isEmpty()) {
 			dataSlice = new PafDataSlice(new double[0], 1);
 			return dataSlice;
 		}
 
 		// Get data cache
 		String clientId = clientState.getClientId();
 		PafDataCache dataCache = getDataCache(clientId);
 
 		// Check if data cache is initialized
 		if (dataCache == null) {
 			String errMsg = "No uow cache initialized for client: " + clientId;
 			logger.fatal(errMsg);
 			throw new PafException(errMsg, PafErrSeverity.Fatal);
 		}
 
 		// Get uow trees
 		MemberTreeSet memberTrees = clientState.getUowTrees();
 		if (memberTrees == null) {
 			String errMsg = "No member trees initialized for client: " + clientId;
 			logger.fatal(errMsg);
 			throw new PafException(errMsg, PafErrSeverity.Fatal);
 		}
 
 		// Get "Materialized View Section" catalog
 		pafMVS = clientState.getMVS(PafMVS.generateKey(view, viewSection));
 		if (pafMVS == null) { 
 			String errMsg = "Missing materialized view section for client: [" 
 				+ clientId + "] - view: [" + view.getName() + "] - view section: [" 
 				+ viewSection.getName() + "]";
 			throw new PafException(errMsg, PafErrSeverity.Fatal);
 		}
 		
 		// Add data cache to MVS & add MVS to Data Cache
 		pafMVS.setDataCache(dataCache);
 		dataCache.setPafMVS(pafMVS);
 		
 		// Build data slice parameters
 		sliceParms = buildDataSliceParms(viewSection);
 		pafMVS.setDataSliceParms(sliceParms);
 
 		// Populate data cache with supporting reference data
 		UnitOfWork uowSpec = sliceParms.buildUowSpec(viewSection.getDimensionsPriority());
 		loadMdbViewData(dataCache, uowSpec, clientState, viewSection.hasAttributes());
 		
 		
 		// Calculate base version intersections on attribute view
 		if (viewSection.hasAttributes()) {
 
 			// Ensure that all selected attribute dimensions for a given
 			// base dimension are mapped to the same level
 			Set<String> viewAttributes = new HashSet<String>(Arrays.asList(viewSection.getAttributeDims()));
 			for (String baseDimName:getBaseDimNames()) {
 
 				// Get attributes for current base dimension that appear in view section
 				PafBaseTree baseTree = getBaseTree(baseDimName);
 				Set<String> associatedAttrDims = new HashSet<String> (baseTree.getAttributeDimNames());
 				associatedAttrDims.retainAll(viewAttributes);
 
 				// Check attribute mapping levels for each attribute dimension.
 				// They all need to be at the same level.
 				Set<Integer> mappingLevels = new HashSet<Integer>(); 
 				for (String attributeDim:associatedAttrDims) {
 					mappingLevels.add(baseTree.getAttributeMappingLevel(attributeDim));
 				}
 				if (mappingLevels.size() > 1) {
 					String errMsg = "Unable to populate view section: [" + viewSection.getName()
 					+ "] - all attribute dimensions of [" + baseDimName 
 					+ "] are not mapped to the same base member level";
 					logger.error(errMsg);
 					PafException pfe = new PafException(errMsg, PafErrSeverity.Error);
 					throw(pfe);
 				}
 
 			}
 
 			// Compute attribute intersections on non-derived versions only. Also, 
 			// include any off-screen versions that are needed to calculate any
 			// derived version on the view. 
 			//TODO Optimize this by only calculating reference versions when the view is initially displayed, unless reference data has been refreshed. 
 			stepDesc = "Attribute aggregation and recalc";
 			startTime = System.currentTimeMillis();
 			dataCache = PafDataCacheCalc.calcAttributeIntersections(dataCache, clientState, sliceParms, null, DcTrackChangeOpt.NONE);
 			logger.info(LogUtil.timedStep(stepDesc, startTime));				
 		}
 
 		// Calculate any derived versions on the view
 		stepDesc = "Version dim calculation";
 		startTime = System.currentTimeMillis();
 		dataCache = PafDataCacheCalc.calcVersionDim(dataCache, uowSpec.buildMemberFilter(), memberTrees);
 		logger.info(LogUtil.timedStep(stepDesc, startTime));
 
 		// Get data slice corresponding to this view section
 		dataSlice = dataCache.getDataSlice(sliceParms);
 
 		if (compressData == true){
 			// compress slice for return.
 			try {
 				dataSlice.compressData();
 			}
 			catch (IOException iox) {
 				throw new PafException(iox.getLocalizedMessage(), PafErrSeverity.Error);
 			}
 		}
 
 		// Return dataSlice
 		return dataSlice;
 	}
 
 	/**
 	 * 	Load mdb data intersections required to support the rendering of 
 	 *  the current view
 	 * 
 	 * @param dataCache Data cache
 	 * @param viewMemberSpec View member specification per dimension
 	 * @param clientState Client state object
 	 * @param isAttributeView If true, indicates the current view is an attribute view
 	 * 
 	 * @throws PafException 
 	 */
 	private void loadMdbViewData(PafDataCache dataCache, UnitOfWork viewMemberSpec, PafClientState clientState, Boolean isAttributeView) throws PafException {
 	
 		PafApplicationDef pafApp = clientState.getApp();
 		MdbDef mdbDef = pafApp.getMdbDef();
 		String measureDim = mdbDef.getMeasureDim(), timeDim = mdbDef.getTimeDim(), versionDim = mdbDef.getVersionDim();
 		int versionAxis = dataCache.getVersionAxis();
 		UnitOfWork refDataSpec = null;
 		Map<String, Map<Integer, List<String>>> dataSpecByVersion = new HashMap<String, Map<Integer, List<String>>>();
 
 		// As a safeguard, filter out any versions not defined to the data cache. This
 		// prevents the attempted loading of a version the user doesn't have security 
 		// rights to, in the case of any unauthorized versions specified in the view.
 		List<String> viewVersions = new ArrayList<String>(Arrays.asList(viewMemberSpec.getDimMembers(versionDim)));
 		List<String> referenceVersions = new ArrayList<String>(dataCache.getReferenceVersions());
 		referenceVersions.retainAll(viewVersions);
 		List<String> contribPctVersions = new ArrayList<String>(pafApp.getContribPctVersions());
 		contribPctVersions.retainAll(viewVersions);
 
 		
 		// Exit if no data to update
 		if (referenceVersions.size() == 0 && contribPctVersions.size() == 0) {
 			return;
 		}
 		
 		
 		// First process base (non-dynamic) reference versions - determine which reference 
 		// data intersections to update. Store this data specification in the format of a 
 		// uow specification and clone for each base version.
 		if (!isAttributeView) {
 
 			// Regular view - select all members on view, plus all uow measures 
 			// and time periods
 			refDataSpec = viewMemberSpec.clone();
 			refDataSpec.setDimMembers(measureDim, dataCache.getDimMembers(measureDim));
 			refDataSpec.setDimMembers(timeDim, dataCache.getDimMembers(timeDim));
 			
 		} else {
 			
 			// Attribute view - select the supporting base dimension intersections. All uow
 			// measures and time periods will be selected, regardless of which members are
 			// specified on view. Only the members on the view will be selected for the 
 			// remaining base dimensions. 
 			//
 			// The one exception to this is for any base dimension with an associated
 			// attribute dimension on the view. Since all upper level attribute intersections
 			// are calculated in Pace, only reference data for base members at the attribute
 			// mapping level will be selected for these base dimensions with attributes
 			// on the view.
 			String[]dcBaseDims = dataCache.getBaseDimensions();
 			refDataSpec = new UnitOfWork(dcBaseDims);
 			Set<String> viewDims = new HashSet<String>(Arrays.asList(viewMemberSpec.getDimensions()));
 			for (String baseDim : dcBaseDims) {
 				
 				// All uow measures and periods will be selected
 				if (baseDim.equals(measureDim) || baseDim.equals(timeDim)) {
 					refDataSpec.setDimMembers(baseDim, dataCache.getDimMembers(baseDim));
 					continue;
 				}
 				
 				// Check remaining base dimensions for associated attributes
 				PafBaseTree baseTree = (PafBaseTree) clientState.getUowTrees().getTree(baseDim);
 				Set<String> associatedAttrDims = new HashSet<String> (baseTree.getAttributeDimNames());
 				associatedAttrDims.retainAll(viewDims);
 				if (associatedAttrDims.size() > 0) {
 					// Base dimension has attributes - select all uow members at the attribute
 					// mapping level.
 					int level = baseTree.getAttributeMappingLevel(associatedAttrDims.toArray(new String[0])[0]);
 					List<PafDimMember> baseMembers = baseTree.getMembersAtLevel(baseTree.getRootNode().getKey(), level);
 					refDataSpec.setDimMembers(baseDim, baseTree.getMemberNames(baseMembers).toArray(new String[0]));
 				} else {
 					// No associated attribute - just select members on the view
 					refDataSpec.setDimMembers(baseDim, viewMemberSpec.getDimMembers(baseDim));
 				}
 			}
 		}
 
 		// Take data specification, convert to a map, and clone it across each reference version on the view
 		for (String version : referenceVersions) {
 			
 			// Clone data specification for current version
 			Map <Integer, List<String>> dataSpecAsMap = refDataSpec.buildUowMap();
 			dataSpecAsMap.put(versionAxis, new ArrayList<String>(Arrays.asList(new String[]{version})));
 			
 			// Add filtered version-specific data spec to master map
 			dataSpecByVersion.put(version, dataSpecAsMap);
 		}
 		
 		
 	
 		// Contribution % versions - Populate all UOW intersections for any base reference
 		// versions (on or off the view) that meets one of the following criteria:
 		//
 		// 	1. Base reference version is used in the compare intersection spec of any 
 		//	   contribution % version formula on the view. 
 		// 			OR 
 		//	2. Base reference version is used as the base version in a contribution % 
 		//	   formula and no other version is specified in the cross dim spec.
 		//
 		//
 		// Some data specifications that were previously generated earlier in this methods
 		// may be updated in this logic.
 		//
 		//
 		// While this logic could be better optimized to pull in only the required 
 		// intersections, it doesn't seem worth the effort at this point, due to the 
 		// complex logic required and the chance of introducing calculation errors. However,
 		// this may have to be revisited in the future if additional data load optimizations 
 		// are required.
 		for (String contribPctVersion : contribPctVersions) {
 			
 			// Get the formula's base version and optional comparison version
 			VersionDef versionDef = pafApp.getVersionDef(contribPctVersion);
 			String baseVersion = versionDef.getVersionFormula().getBaseVersion();
 			String compareVersion = null;
 			PafDimSpec[] compareIsSpec = versionDef.getVersionFormula().getCompareIsSpec();
 			for (PafDimSpec crossDimSpec : compareIsSpec) {
 				if (crossDimSpec.getDimension().equals(versionDim)) {
 					compareVersion = crossDimSpec.getExpressionList()[0];
 					break;
 				}
 			}
 			
 			// Select any reference version that is specified in either the 
 			// base or comparison version.
 			Set<String> selectedVersions = new HashSet<String>();
 			if (referenceVersions.contains(compareVersion)) {
 				selectedVersions.add(compareVersion);
 			}
 //			if (referenceVersions.contains(baseVersion) && !baseVersion.equals(compareVersion)) {
 			if (referenceVersions.contains(baseVersion)) {
 				selectedVersions.add(baseVersion);
 			}
 			
 			// Select all UOW intersections for any of the selected reference versions
 			refDataSpec = dataCache.getUowSpec();
 			for (String version : selectedVersions) {
 				
 				// Clone data specification for current version
 				Map <Integer, List<String>> dataSpecAsMap = refDataSpec.buildUowMap();
 				dataSpecAsMap.put(versionAxis, new ArrayList<String>(Arrays.asList(new String[]{version})));
 				
 				// Add filtered version-specific data spec to master map
 				dataSpecByVersion.put(version, dataSpecAsMap);
 			}
 				
 		}
 		
 		
 		
 		// Update view reference data using data specification map
 		String dsId = mdbDef.getDataSourceId();
 		IPafConnectionProps connProps = clientState.getDataSources().get(dsId);
 		IMdbData mdbData = this.getMdbDataProvider(connProps);
 		mdbData.updateDataCache(dataCache, dataSpecByVersion);
 
 		
 	}
 
 
 	/**
 	 * Build the necessary parms for generating a data slice
 	 *
 	 * @param section View sections
 	 * @return PafDataSliceParms
 	 */
 	protected PafDataSliceParms buildDataSliceParms(final PafViewSection section) {
 		
 		/*
 		 * Omit member tags and paf blanks from data slice parms
 		 */
 		
 		logger.info("Building Data Slice Parms ...");
 		// Build data slice parms
 		PafDataSliceParms sliceParms = new PafDataSliceParms();
 		logger.debug("Building Data Slice Parms - Setting Column Dimensions");
 		sliceParms.setColDimensions(section.getColAxisDims());
 
 		logger.debug("Building Data Slice Parms - Setting Row Dimensions");
 		sliceParms.setRowDimensions(section.getRowAxisDims());
 
 		ArrayList<String[]> memberLists = null;
 		int i = 0;
 		logger.debug("Building Data Slice Parms - Setting Row Members");        
 		memberLists = new ArrayList<String[]>();
 		for (ViewTuple tuple : section.getRowTuples() ) {
 //			if (!tuple.getMemberDefs()[0].toUpperCase().equals(PafBaseConstants.PAF_BLANK))
 			if (!tuple.getMemberDefs()[0].toUpperCase().equals(PafBaseConstants.PAF_BLANK) && !tuple.isMemberTag())
 				memberLists.add(tuple.getMemberDefs());
 		}
 		sliceParms.setRowTuples(memberLists.toArray(new String[0][0]));
 
 		logger.debug("Building Data Slice Parms - Setting Column Members");
 		i = 0;
 		memberLists = new ArrayList<String[]>();
 		for (ViewTuple tuple : section.getColTuples() ) {
 //if (!tuple.getMemberDefs()[0].toUpperCase().equals(PafBaseConstants.PAF_BLANK))            
 			if (!tuple.getMemberDefs()[0].toUpperCase().equals(PafBaseConstants.PAF_BLANK)&& !tuple.isMemberTag())            
 				memberLists.add(tuple.getMemberDefs());
 		}        
 		sliceParms.setColTuples(memberLists.toArray(new String[0][0]));
 
 		logger.debug("Building Data Slice Parms - Setting Page Axis & Members");          
 		i = 0;
 		String [] pageAxis = new String[section.getPageTuples().length];      
 		String [] pageMembers = new String[section.getPageTuples().length];
 		for (PageTuple tuple : section.getPageTuples() ) {
 			pageAxis[i] = tuple.getAxis();          
 			pageMembers[i++] = tuple.getMember();
 		}     
 
 		sliceParms.setPageDimensions( pageAxis );      
 		sliceParms.setPageMembers( pageMembers );
 
 		sliceParms.setAttributeDims(section.getAttributeDims());
 		sliceParms.setDimSequence(section.getDimensionsPriority());
 		
 		// Return data slice parms
 		logger.info("Returning Completed Data Slice Parms");    
 		return sliceParms;
 	}
 
 
 	/**
 	 *	Initialize member tree cache
 	 *
 	 */
 	protected void clearMemberTreeCache() {
 
 		logger.info("Clearing dimension tree cache.");
 
 		Session session = PafMetaData.currentPafDBSession();
 		
 		Transaction tx = null;
 
 		try {
 
 			tx = session.beginTransaction();
 
 			@SuppressWarnings("unchecked")
 			List<PafBaseTree> trees = session.createQuery("from PafBaseTree").list();
 
 			logger.info("Trees enumerated");
 
 			for (PafBaseTree tree : trees) {  
 
 				if (tree.getRootNode() != null) {
 
 					delChildrenFromCache(tree.getRootNode(), session);
 
 				}
 
 				session.delete(tree);
 
 				logger.info(tree.getRootNode().getKey() + " tree deleted" );
 
 			}
 
 			tx.commit();
 
 		} catch (RuntimeException ex) {
 
 			try {
 				tx.rollback();
 			} catch (RuntimeException re2) {
 				//do nothing
 			}
 
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 
 		}
 
 	}
 
 	private void delChildrenFromCache(PafDimMember member, Session session) {
 
 		for (PafDimMember child: member.getChildren() ) {
 
 			delChildrenFromCache(child, session);
 
 		}
 
 		session.delete(member);
 	}
 
 	/**
 	 *	Initialize member tree store
 	 *
 	 * @param pafApp Application definition
 	 */
 	public void initMemberTreeStore(PafApplicationDef pafApp) {
 
 		IMdbMetaData metaData = null;
 		MdbDef mdbDef = pafApp.getMdbDef();
 		IPafConnectionProps connProps = (IPafConnectionProps) PafMetaData.getAppContext().getBean(mdbDef.getDataSourceId());
 		String[] dims = mdbDef.getAllDims();
 		String measureDim = mdbDef.getMeasureDim(), versionDim = mdbDef.getVersionDim();
 		Set<String> validMeasures = pafApp.getMeasureDefs().keySet();
 		Set<String> validVersions = pafApp.getVersionDefs().keySet();
 		Set<String> validMembers = null;
 		boolean isRollupFiltered = true;
 
 
 		
 		try {           
 			
 			// Connect to Essbase
 			
 // 			TTN-1406
 //			IMdbClassLoader mdbClassLoader = this.getMdbClassLoader(connProps);
 //			metaData = mdbClassLoader.getMetaDataProvider();
 			metaData = this.getMetaDataProvider(connProps);
 			
 			// Load and/or cache each base dimension tree
 			for (String dim : dims) {
 				
 
 				// Pre-processing for validation of measure and version trees
 				if (dim.equals(measureDim) || dim.equals(versionDim)) {
 					if (dim.equals(measureDim)) {
 						// Measures - all measures in tree should have an entry in the measures
 						// definition file.
 						validMembers = validMeasures;
 						isRollupFiltered = true;
 					} else {
 						// Versions - only level 0 versions should be pruned from versions tree. 
 						// Version rollups are used for display purposes only and wouldn't have   
 						// an entry in the version definition file.  
 						validMembers = validVersions;
 						isRollupFiltered = false;
 					}
 				}
 
 				if (isDimCached(dim)) {
 					
 					//get cached tree
 					PafBaseTree cachedTree = getCachedDim(dim);
 					
 					//TTN-1015: This if handles the scenario when the root node of the measure tree
 					//is a different name than the dimension name.  This resolves the error when the root node
 					//gets set to null because hibernate can't map the root member back from the original
 					//root member set on the tree before being cached.
 					if (dim.equals(measureDim) && cachedTree.getRootNode() == null) {
 																		
 						//check for null measure root
 						if ( mdbDef.getMeasureRoot() == null ) {							
 							throw new IllegalArgumentException("Measure root is null.");
 						}
 												
 						//find measure root member and set as new root member
 						cachedTree.setRootNode(cachedTree.getMember(mdbDef.getMeasureRoot()));	
 						
 					}
 					
 					baseTrees.put(dim, cachedTree);
 					
 					// Check measure and version trees for any undefined members
 					if (dim.equals(measureDim) || dim.equals(versionDim)) {
 						logger.info("Validating cached dimension tree: [" + dim + "] ...");
 						Set<String> undefinedMembers = cachedTree.findInvalidMembers(validMembers, isRollupFiltered);
 						for (String member : undefinedMembers) {
 							String errMsg = "Undefined member: [" + member + "] found in cached dimension tree: " + dim;
 							logger.warn(errMsg);
 						}
 					}
 					
 				}
 				else
 				{
 					// If measures dimension, pull root name from "measure root" property
 					String root = dim;
 					if (dim.equals(mdbDef.getMeasureDim())) {
 						root = mdbDef.getMeasureRoot();
 					}
 
 					// Import base dimension tree from Essbase outline
 					PafBaseTree tree = metaData.getBaseDimension(pafApp.getEssNetTimeOut(), root);
 					
 					// Reset tree id to dimension name (necessary if the root of the tree is not the dimension name)
 					tree.setId(dim);
 
 					// Filter measure and version trees against undefined members
 					if (dim.equals(measureDim) || dim.equals(versionDim)) {
 						Set<String> prunedMembers = tree.filterTree(validMembers, isRollupFiltered);
 						for (String member : prunedMembers) {
 							String errMsg = "Undefined member: [" + member + "] " ;
 							if (isRollupFiltered){
 								errMsg += "(and its descendants) ";
 							} 
 							errMsg += "pruned from dimension tree: "+ dim;
 							logger.warn(errMsg);
 						}
 					}
 					
 					// Set associated attributes properties on base tree members
 					tree = setAssociatedAttributes(tree, dim);
 
 					// Add base tree to collection and cache it to relational store
 					baseTrees.put(dim, tree);
 					cacheDim(baseTrees.get(dim));
 				}
 // Migrated creation of memberIndexLists to client state during plan session start (TTN-1391)
 //				memberIndexLists.put(dim, createMemberIndexList(baseTrees.get(dim), TreeTraversalOrder.PRE_ORDER));
 			}
 			
 		} catch (Exception ex) {
 			// Handle any found exception (TTN-1355)
 			logger.error("Error encountered loading dimension trees - one or more trees have failed to load!!!");
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 		} finally {
 			try {
 				metaData.disconnect();
 			} catch (Exception ex) {
 				//doesn't matter.
 			}
 		}
 	}
 
 	/**
 	 *	Set associated attributes on paf base tree
 	 *
 	 * @param baseTree Paf base tree
 	 * @param baseDimName Name of base tree
 	 * 
 	 * @return PafBaseTree
 	 */
 	public PafBaseTree setAssociatedAttributes(PafBaseTree baseTree, String baseDimName) {
 
 		Map <String, PafAttributeTree> attrTrees = getAttributeTrees(); 
 		Map<String, Integer> attributeDimInfo = new HashMap<String, Integer>();
 
 
 		// Iterate through all attribute trees and identify the ones 
 		// mapped to specified base dimension
 		for (String attrDimName:attrTrees.keySet()) {
 			PafAttributeTree attrTree = attrTrees.get(attrDimName);
 
 			// If attribute dimension is mapped to specified base dimension
 			if (baseDimName.equalsIgnoreCase(attrTree.getBaseDimName())) {
 
 				// Track level of associated base members for this attribute dimension
 				Integer attrBaseMemberLevel = null;
 
 				// Get all level 0 attribute members for current attribute tree
 				// as this is where the base members are assigned
 				List<PafDimMember> attrMbrs = attrTree.getMembersByLevel().get(0);
 
 				// Iterate through attribute members and retrieve associated base members
 				for (PafDimMember attrMbr: attrMbrs) {
 
 					// Retrieve associated base members
 					PafDimMemberProps dimMemberProps = attrMbr.getMemberProps();
 					PafAttributeMemberProps attrMemberProps = (PafAttributeMemberProps) dimMemberProps;
 					Set<String> baseMemberNames = attrMemberProps.getBaseMembers();
 
 					// Generate a warning message if attribute does not have any mapped base members
 					if (baseMemberNames == null || baseMemberNames.isEmpty()) {
 						String errMsg = "Warning: loading associated attributes - no base member mappings were found for"
 							+ " attribute value: " + attrMbr.getKey() + " - in attribute dimension: " + attrDimName;
 						logger.warn(errMsg);
 					}
 
 					// Iterate through base members and assign attributes
 					for (String baseMemberName:baseMemberNames) {
 
 						// Find base member in base tree
 						PafBaseMember baseMember = baseTree.getMember(baseMemberName);
 
 						// Check base member level number
 						PafBaseMemberProps baseMemberProps = baseMember.getMemberProps();
 						int memberLevel = baseMemberProps.getLevelNumber();
 						if (attrBaseMemberLevel == null) {
 							// First associated base member?
 							attrBaseMemberLevel = memberLevel;
 						} else {
 							// Else, ensure that every subsequent base member has the same level as 
 							// the first base member associated with this attribute dimension.
 							if (attrBaseMemberLevel != memberLevel) {
 								String errMsg = "Error loading associated attributes for Attribute Dimension: ["
 									+ attrDimName + "] - not all associated base members are at level: "
 									+ attrBaseMemberLevel + " - " + baseMemberName + " is at level: " + memberLevel;
 								logger.warn(errMsg);
 							}
 						}
 
 						// Add attribute to base member's associated attributes property
 						Map <String, String> attributes = baseMemberProps.getAssociatedAttributes();
 						if (attributes == null) {
 							attributes = new HashMap <String, String>();
 						}
 						attributes.put(attrDimName, attrMbr.getKey());
 						baseMemberProps.setAssociatedAttributes(attributes);
 					}
 				}
 
 				// Ensure that the attribute current attribute dimension has at least one 
 				// attribute mapped to a base member
 				if (attrBaseMemberLevel == null) {
 					String errMsg = "Error loading associated attributes - no base member mappings were found for attribute dimension: "
 						+ attrDimName;
 					logger.error(errMsg);
 					IllegalArgumentException iae = new IllegalArgumentException(errMsg);
 					throw (iae);
 				}
 
 				
 				// Capture each attribute dimension and corresponding base dimension mapping level 
 				attributeDimInfo.put(attrDimName, attrBaseMemberLevel);
 
 			}
 		}
 
 		// Copy attribute information from stored members to shared members
 		for (PafDimMember sharedMember : baseTree.getSharedMembers()) {
 
 			// Find corresponding base member in base tree
 			PafBaseMember baseMember = baseTree.getMember(sharedMember.getKey());
 			
 			// Copy attribute information
 			PafBaseMember sharedBaseMember = (PafBaseMember) sharedMember; 
 			sharedBaseMember.getMemberProps().setAssociatedAttributes(baseMember.getMemberProps().getAssociatedAttributes());
 		}
 
 		// Store the names of any associated attribute dimensions and their corresponding
 		// base dimension mapping level, at the root of base tree
 		baseTree.setAttributeDimInfo(attributeDimInfo);
 
 		// Return update base trees
 		return baseTree;
 	}
 
 
 	/**
 	 * Builds the attributeTrees map using the internal and external hibernate layer.
 	 */
 	public void initAttributeMemberTreeStore() {
 		List<PafAttributeTree> pafAttributeTree = null;
 		List<PafAttributeTree> pafAttributeTreeExt = null;
 
 		try {           
 			//get all PafAttributeTrees for our datastore.
 			pafAttributeTree = this.getCachedDims(PafMetaData.currentPafDBSession());
 			for(PafAttributeTree tree : pafAttributeTree){
 				logger.info("Starting caching attribute dimension tree: " + tree.getId());
 				
 				/*
 				logger.info("Begining search for parent node for tree: " + tree.getId());
 				for(PafAttributeMember member : tree.getMemberValues()){
 					if(member.getParent() == null){
 						logger.info("Found parent node: " + member.getKey() + ", for tree: " + tree.getId());
 						//set the root node.
 						tree.setRootNode(member);
 						//set the members by generation.
 						tree.getMembersByGen();
 						//set the members by level.
 						tree.getMembersByLevel();
 						break;
 					}
 				}
 				*/
 				
 				//find root node using attribute dimension name.
 				PafAttributeMember rootNode = tree.getMember(tree.getId());
 				
 				//set the root node.
 				tree.setRootNode(rootNode);
 				
 				//set the members by generation.
 				tree.getMembersByGen();
 				
 				//set the members by level.
 				tree.getMembersByLevel();
 				
 				//put the tree in the dictionary.
 				attributeTrees.put(tree.getId(), tree);
 				
 				logger.info("Completed caching attribute dimension tree: " + tree.getId());
 			}
 
 			//get all PafAttributeTrees for external data store.
 			//if a duplicate tree name if found it is ignored.
 			pafAttributeTreeExt = this.getCachedDims(PafMetaData.currentPafExtAttrDBSession());
 			for(PafAttributeTree tree : pafAttributeTreeExt){
 				if(! attributeTrees.containsKey(tree.getId())){
 					logger.info("Starting caching attribute dimension tree: " + tree.getId());
 					logger.info("Begining search for parent node for tree: " + tree.getId());
 					for(PafAttributeMember member : tree.getMemberValues()){
 						if(member.getParent() == null){
 							logger.info("Found parent node: " + member.getKey() + ", for tree: " + tree.getId());
 							//set the root node.
 							tree.setRootNode(member);
 							//set the members by generation.
 							tree.getMembersByGen();
 							//set the members by level.
 							tree.getMembersByLevel();
 							break;
 						}
 					}
 					attributeTrees.put(tree.getId(), tree);
 					logger.info("Completed caching attribute dimension tree: " + tree.getId());
 				} else {
 					logger.warn("Duplicate attribute dimension tree found: " + tree.getId() + " program will ignore.");
 				}
 			}
 
 // Migrated creation of memberIndexLists to client state during plan session start (TTN-1391)
 //			// Generate memberIndexList for each attribute dimension, for use in allocation processing
 //			for (String attrDim:attributeTrees.keySet()) {
 //				//for (PafDimTree attrTree:attributeTrees.keySet()) {
 //				memberIndexLists.put(attrDim, createMemberIndexList(attributeTrees.get(attrDim), TreeTraversalOrder.PRE_ORDER));
 //			}
 
 		} catch (Exception ex) {
 			logger.warn(ex.getMessage());
 		} 
 
 
 	}
 
 	/**
 	 * Cache an array of attribute dimensions into the data store.
 	 * @param dims Array of attribute dimensions to cache.
 	 * @param appDef The application def for the user.
 	 * @throws PafException 
 	 */
 	public void cacheAttributeDims(String[] dims, PafApplicationDef appDef) throws PafException {
 		if(dims == null || dims.length == 0){
 			return;
 		}
 
 		IMdbMetaData metaData = null;
 		PafMdbProps mdbProps = null;
 		
 		IPafConnectionProps connProps = (IPafConnectionProps) 
 		PafMetaData.getAppContext().getBean(appDef.getMdbDef().getDataSourceId());
 				
 		try{
 //			TTN-1406
 //			IMdbClassLoader mdbClassLoader = this.getMdbClassLoader(connProps);
 //			metaData = mdbClassLoader.getMetaDataProvider();
 			metaData = this.getMetaDataProvider(connProps);
 			
 			//get the mdb props from Essbase.
 			mdbProps = metaData.getMdbProps();
 			
 			boolean varyingAttributesExist = metaData.varyingAttributesExist(dims, mdbProps, appDef.getEssNetTimeOut());
 			
 			if(varyingAttributesExist == true){
 				logger.error("Varying Attributes exist in the Essbase outline - no attributes will be loaded");
 			}
 			else{
 				for(String dim : dims){
 					//get the attribute tree from the essbase outline.
 //					PafAttributeTree pafAttributeTree = metaData.getAttributeDimension(dim, mdbProps, appDef.getEssNetTimeOut());
 					PafAttributeTree pafAttributeTree = metaData.getAttributeDimension(appDef.getEssNetTimeOut(), dim, mdbProps);
 					//delete the cached dim.
 					deleteCacheAttrDim(dim);
 					//recache the dim.
 					cacheDim(pafAttributeTree);
 					//check the DICTIONARY to see if the tree already exists.  If
 					//so then delete it and re add it.
 					if(attributeTrees.containsKey(dim)){
 						attributeTrees.remove(dim);
 					}
 					attributeTrees.put(dim, pafAttributeTree);
 				}
 			}
 		} catch(PafException e){
 			logger.error(e);
 			throw e;
 		} finally {
 			try {
 				metaData.disconnect();
 			} catch (Exception ex) {
 				//doesn't matter.
 			}
 		}
 	}
 
 	/**
 	 * Deletes a PafAttributeTree from the Hibernate datastore.
 	 * 
 	 * @param dim The dimension to be deleted.
 	 */
 	public void deleteCacheAttrDim(String dim) {
 		Session session = PafMetaData.currentPafDBSession();
 		Transaction tx = null;
 
 		try {
 			if(dim != null && dim.length() > 0){
 				//begin a new transaction
 				tx = session.beginTransaction();   
 				//send the delete query.
 				session.createQuery("Delete from PafAttributeTree where MEMBER_TREE_NAME = ?")
 				.setParameter(0, dim).executeUpdate();
 				//Commit the transaction
 				tx.commit();
 				//remove the item from the tree
 				attributeTrees.remove(dim);
 			}
 
 		} catch (RuntimeException ex) {
 			try {
 				tx.rollback();
 			} catch (RuntimeException re2) {
 				//do nothing
 			}
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 		}
 	}
 
 	public void deleteAllCacheAttrDim() {
 		Session session = PafMetaData.currentPafDBSession();
 		Transaction tx = null;
 		List<PafAttributeTree> pafAttributeTree = null;
 		List<String> dimsToRemove = new ArrayList<String>();
 
 		try{
 			//get all PafAttributeTrees for our datastore.
 			pafAttributeTree = this.getCachedDims(session);
 			//store the names of the dims.
 			for(PafAttributeTree tree : pafAttributeTree){
 				dimsToRemove.add(tree.getId());
 			}
 		}catch (Exception ex) {
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 		}
 
 		tx = null;
 
 		try {
 			//begin a new transaction
 			tx = session.beginTransaction();   
 			//send the delete query.
 			session.createQuery("Delete from PafAttributeTree").executeUpdate();
 			//Commit the transaction
 			tx.commit();
 			//remove all attributes from the map.
 			for(String dim : dimsToRemove){
 				attributeTrees.remove(dim);
 			}
 
 		} catch (RuntimeException ex) {
 			try {
 				tx.rollback();
 			} catch (RuntimeException re2) {
 				//do nothing
 			}
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 		}
 	}
 
 
 	/**
 	 * Cache a PafAttributeTree into the Hibernate datastore.
 	 * 
 	 * @param tree The PafAttributeTree to be cached.
 	 */
 	private void cacheDim(PafAttributeTree tree) {
 		Session session = PafMetaData.currentPafDBSession();
 		Transaction tx = null;
 
 		try {
 			//begin a new transaction
 			tx = session.beginTransaction();
 			//save the new one
 			session.save(tree);
 			//commit the transaction
 			tx.commit(); 
 		} catch (RuntimeException ex) {
 			try {
 				tx.rollback();
 			} catch (RuntimeException re2) {
 				//do nothing
 			}
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 		}
 	}
 
 	/**
 	 * Gets a list of cached dimension from the Hibernate datastore.
 	 * 
 	 * @param session The hibernate session to query.
 	 * @return A list of PafAttributeTree
 	 */
 	@SuppressWarnings("unchecked")
 	private List<PafAttributeTree> getCachedDims(Session session) {
 		Transaction tx = null;
 		List<PafAttributeTree> pafAttributeTrees = null;
 		try{
 			//start a transaction.
 			tx = session.beginTransaction();
 			//load the trees
 			pafAttributeTrees = session.createQuery("from PafAttributeTree").list();
 			//end the transaction.
 			tx.commit();
 		} catch (RuntimeException ex) {
 			try {
 				tx.rollback();
 			} catch (RuntimeException re2) {
 				//do nothing
 			}
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 		}
 		return pafAttributeTrees;
 	}
 
 	private void cacheDim(PafBaseTree tree) {
 
 		Session session = PafMetaData.currentPafDBSession();
 
 		Transaction tx = null;
 
 		try {
 
 			tx = session.beginTransaction();
 
 			session.saveOrUpdate(tree);
 
 			tx.commit();  
 
 		} catch (RuntimeException ex) {
 
 			try {
 				tx.rollback();
 			} catch (RuntimeException re2) {
 				//do nothing
 			}
 
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 		}       
 
 	}
 
 	private PafBaseTree getCachedDim(String string) {
 
 		Session session = PafMetaData.currentPafDBSession();
 		Transaction tx = null;
 		PafBaseTree tree = null;
 
 		try {
 
 			tx = session.beginTransaction();
 
 			tree = (PafBaseTree) session.load(PafBaseTree.class, string );
 
 			tx.commit();
 
 		} catch (RuntimeException ex) {
 
 			try {
 				tx.rollback();
 			} catch (RuntimeException re2) {
 				//do nothing
 			}
 
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 		}
 
 		return tree;
 	}
 
 	private boolean isDimCached(String string) {
 
 		Session session = PafMetaData.currentPafDBSession();
 
 		Transaction tx = null;
 
 		java.util.List list;
 
 		try {
 
 			tx = session.beginTransaction();
 
 			list = session.createQuery("from PafBaseTree where id='" + string + "'").list();
 
 			tx.commit();
 
 		} catch (RuntimeException ex) {
 
 			try {
 				tx.rollback();
 			} catch (RuntimeException re2) {
 				//do nothing
 			}
 
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 
 			return false;
 		}
 
 		if (list.size() > 0) {
 			return true;
 		}
 		return false;
 
 	}
 
 	public void clearMemberTreeStore() {
 		baseTrees.clear();
 //		memberIndexLists.clear();
 	}
 
 
 	public IMdbMetaData getMetaDataProvider(IPafConnectionProps connectionProps) {
 
 		IMdbMetaData mdbProvider = null;
 
 		try {
 			@SuppressWarnings("rawtypes")
 			Constructor C = Class.forName(connectionProps.getMetaDataServiceProvider()).getConstructor(new Class[] {Properties.class} );
 
 			mdbProvider = (IMdbMetaData) C.newInstance(new Object[] { connectionProps.getProperties() } );
 
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.getTargetException().printStackTrace();
 			e.printStackTrace();
 		}
 
 		return mdbProvider;
 	}
 
 	public IMdbData getMdbDataProvider(IPafConnectionProps connectionProps) {  
 
 		IMdbData dataProvider = null;
 
 		try {
 
 			Constructor<?> C = Class.forName(connectionProps.getDataServiceProvider()).getConstructor(new Class[] {Properties.class} );
 
 			dataProvider = (IMdbData) C.newInstance(new Object[] { connectionProps.getProperties() } );
 
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return dataProvider;        
 
 	}
 
 	/**
 	 *  Get the specified mdb provider class loader
 	 *
 	 * @param connectionProps Connection properties
 	 * 
 	 * @return IMdbClassLoader
 	 */
 	public IMdbClassLoader getMdbClassLoader(IPafConnectionProps connectionProps) {
 
 		IMdbClassLoader mdbClassLoader = null;
 
 		try {
 
 			String mdbClassLoaderProp = connectionProps.getMdbClassLoader();
 			if (mdbClassLoaderProp == null) {
 				mdbClassLoaderProp = PafBaseConstants.DEFAULT_MDB_CLASS_LOADER;
 				logger.warn("Using default MDB class loader: [" + mdbClassLoaderProp + "]");
 			}
 			
 			Constructor<?> C = Class.forName(mdbClassLoaderProp).getConstructor(new Class[] {Properties.class} );
 
 			mdbClassLoader = (IMdbClassLoader) C.newInstance(new Object[] { connectionProps.getProperties() } );
 
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return mdbClassLoader;
 	}
 
 
 
 	/**
 	 *	Resolve and expand the dynamic row and column member definitions on the view
 	 *
 	 * @param view Application view
 	 * @param clientState Client state object
 	 * 
 	 * @return Updated application view
 	 * @throws PafException 
 	 */
 	public PafView expandView(PafView view, PafClientState clientState) throws PafException {
 
 		// Expand all of the row and column tuples on each view section
 		logger.info("Expanding tuples for view: " + view.getName());
 		for (PafViewSection viewSection: view.getViewSections()) {
 			viewSection.setRowTuples(expandTuples(viewSection.getRowTuples(), viewSection.getRowAxisDims(), viewSection.getPageAxisDims(), viewSection.getPageAxisMembers(), viewSection.getAttributeDims(), clientState));
 			viewSection.setColTuples(expandTuples(viewSection.getColTuples(), viewSection.getColAxisDims(), viewSection.getPageAxisDims(), viewSection.getPageAxisMembers(), viewSection.getAttributeDims(), clientState));
 
 			// Remove invalid tuples from view section
 			PafViewService.getInstance().ProcessInvalidTuples(viewSection, clientState);
 
 			if (viewSection.getRowTuples().length > 0){
 				viewSection.setRowTuples(generateHeaderGroupNo(viewSection.getRowTuples(),viewSection.getRowAxisDims()));
 			} else {
 				viewSection.setEmpty(true);
 			}
 			if (viewSection.getColTuples().length > 0)
 			{
 				viewSection.setColTuples(generateHeaderGroupNo(viewSection.getColTuples(), viewSection.getColAxisDims()));
 			} else {
 				viewSection.setEmpty(true);
 			}
 			
 		}
 		logger.info("Completed exapanding tuples for view: " + view.getName());        
 		return view;
 	}
 
 	/**
 	 *	Generate the header group number used in the presentation layer to properly span member headers across rows or columns
 	 *
 	 * @param viewTuples View tuples
 	 * @param axisDims	Axis dimension names
 	 * @return ViewTuple[] 
 	 */
 	private ViewTuple[] generateHeaderGroupNo(ViewTuple[] viewTuples, String[] axisDims) {
 
 		int dimCount = axisDims.length;
 		
 		//TTN-1041 - client starts tuple index at 1, not 0
 		short tupleInx = 1;
 		
 		Integer groupNo[] = new Integer[dimCount];
 		String[] prevHeader = new String[dimCount];
 		ViewTuple[] updatedViewTuples = viewTuples.clone();
 
 		logger.info("Generating Header Group Numbers");
 
 		//    	Sample View Tuples        	Header Group Info	Order
 		//    	-----------------------------------------------------------
 		//    	Sales$	DPT210	WP			0,0,0					0
 		//    	Sales$	DPT210	LY			0,0,1					1
 		//    	Sales$	DPT210	OP			0,0,2					2
 		//    	Sales$	DPT211	WP			0,1,0					3
 		//    	Sales$	DPT211	LY			0,1,1					4
 		//    	Sales$	DPT211	OP			0,1,2					5
 		//    	GM$		DPT210	WP			1,0,0					6
 		//    	GM$		DPT210	LY			1,0,1					7
 		//    	GM$		DPT210	OP			1,0,2					8
 		//    	GM$		DPT211	WP			1,1,0					9
 		//    	GM$		DPT211	LY			1,1,1				   10
 		//    	GM$		DPT211	OP			1,1,2				   11
 
 
 		// Initialization
 		for (int i = 0; i < dimCount; i++) {
 			// Get get header values on first view tuple
 			prevHeader[i] = viewTuples[0].getMemberDefs()[i];
 			// Initialize group number array
 			groupNo[i] = 0;
 		}
 
 		// Loop through each view tuple to be update
 		for (ViewTuple viewTuple:updatedViewTuples) {
 			// Compare headers on current view tuple to previous view tuple
 			String headers[] = viewTuple.getMemberDefs();
 			for (int axis = 0; axis < dimCount; axis++) {
 				// If header on current axis has changed
 				if (!headers[axis].equals(prevHeader[axis])) {
 					// Increment group number for current axis
 					groupNo[axis]++;
 					prevHeader[axis] = headers[axis];
 					// Initialize group numbers for remaining axis
 					for (int remainingAxis = axis + 1; remainingAxis < dimCount; remainingAxis++) {
 						groupNo[remainingAxis] = 0;
 						prevHeader[remainingAxis] = headers[remainingAxis]; 
 					}
 					// Break out of comparison loop
 					break;
 				}
 			}
 			// Set header group number
 			viewTuple.setDerivedHeaderGroupNo(groupNo.clone());
 			// Set order property
 			viewTuple.setOrder(tupleInx++);
 		}
 
 		// Return update view tuples
 		return updatedViewTuples;
 	}
 
 
 	/**
 	 *	Resolve and expand the member definitions on a set of  view tuples
 	 *
 	 * @param origViewTuples Array of view tuples
 	 * @param axes Array of dimensional axes corresponding to the dimensions in each view tuple
 	 * @param pageAxisDims 
 	 * @param pageAxisMembers 
 	 * @param attributeDims Array of any attribute dimensions associated with the view section
 	 * @param clientState Client state object
 	 * 
 	 * @return Array of view tuples
 	 * @throws PafException 
 	 */
 	public ViewTuple[] expandTuples(ViewTuple[] origViewTuples, String[] axes, String[] pageAxisDims, String[] pageAxisMembers, String[] attributeDims, PafClientState clientState) throws PafException {
 
 		int dimCount = axes.length;
 		int innerAxisIndex = dimCount -1;
 		String axisList = "";
 		List <ViewTuple>expandedTuples = new ArrayList<ViewTuple>();
 
 		
 		// Initialization
 		for (String a : axes) {axisList += a + " "; }
 		logger.info("Expanding tuples for axis: " + axisList);  
 
 		// Expand inner axis
 		for (ViewTuple vt:origViewTuples) {               
 			expandedTuples.addAll(expandTuple(vt, innerAxisIndex, axes[innerAxisIndex], clientState));         
 		}
 
 		// Compile a list of attribute dimensions used in this tuple or the page tuple. This
 		// information will be used to in an initial pass at filtering out invalid member 
 		// intersections. (TTN-1469)
 		Set<String> tupleAttrDims = new HashSet<String>();
 		if (axes.length > 0 && attributeDims != null && attributeDims.length > 1) {
 			// Attribute View
 			// Check for attributes on the tuple (page or current axis)
 			List<String> axisDimList = Arrays.asList(axes);
 			List<String> pageDimList = Arrays.asList(pageAxisDims);
 			for (String attrDim : attributeDims) {
 				if (axisDimList.contains(attrDim) || pageDimList.contains(attrDim)) {
 					tupleAttrDims.add(attrDim);
 				}
 			}
 		}
 		
 		// Expand each symetric tuple group (for tuples comprised of multiple dimensions)
 		for (int axisIndex = innerAxisIndex - 1; axisIndex >= 0; axisIndex--) {
 			expandedTuples = expandSymetricTupleGroups(axisIndex, expandedTuples.toArray(new ViewTuple[0]), axes, tupleAttrDims, pageAxisDims, pageAxisMembers, clientState);
 		}
 
 		//If any tuple member is set to PAFBLANK, set the remaining members to PAFBLANK as well
 		logger.info("Converting Paf Blank Tuples");
 		for (ViewTuple viewTuple:expandedTuples) {
 			if (isBlankViewTuple(viewTuple)) {
 				String[] memberAr = viewTuple.getMemberDefs();
 				for (int i = 0; i < memberAr.length; i++) {
 					memberAr[i] = PafBaseConstants.PAF_BLANK;
 				}
 				viewTuple.setMemberDefs(memberAr);
 			}
 		}
 	
 		logger.info("Completed expanding tuples.");
 		return expandedTuples.toArray(new ViewTuple[0]);
 	}
 
 	/**
 	 *  Expand symteric view tuple groups
 	 *
 	 * @param axisIndex
 	 * @param origViewTuples
 	 * @param axes
 	 * @param tupleAttrDims Set containing any attribute dimensions contained in the view tuples
 	 * @param pageAxisDims 
 	 * @param pageAxisMembers 
 	 * @param clientState
 	 * 
 	 * @return List<ViewTuple>
 	 * @throws PafException 
 	 */
 	private List<ViewTuple> expandSymetricTupleGroups(int axisIndex, ViewTuple[] origViewTuples, String[] axes, Set<String> tupleAttrDims, String[] pageAxisDims, String[] pageAxisMembers, PafClientState clientState) throws PafException {
 
 		int tupleCount = origViewTuples.length;
 		int tupleInx = 0;
 		int groupNoPrefixSize = axisIndex + 1;
 		Integer[] prevGroupNoPrefix = new Integer[groupNoPrefixSize];
 		String dimToExpand = axes[axisIndex];
 		List <ViewTuple>expandedTuples = new ArrayList<ViewTuple>();
 		List <ViewTuple>tupleGroup = new ArrayList<ViewTuple>();
 
 		logger.debug("Expanding tuples along axis: " + axes[axisIndex]);
 		
 	    // This method expands the supplied tuples from the second most inner axis
 		// dimension out to the first tuple axis dimension. Within each axis, the tuples
 		// are expanded in groups based on the setting of the 'symetricGroupNo' property.
         //
         // The example, below, shows a set of tuples coming into this method. The inner
 		// axis members have already been resolved:
         //
 		// Symetric Group#		Members
 		// ---------------		------------------------------
 		// [0][0]				@IDESC(DPT110),	@MEMBERS(BOP_RTL, SLS_RTL, SLS_AUR), WP 	
 		// [0][0]				@IDESC(DPT110),	@MEMBERS(BOP_RTL, SLS_RTL, SLS_AUR), LY 	
 		// [0][0]				@IDESC(DPT110),	@MEMBERS(BOP_RTL, SLS_RTL, SLS_AUR), WP.vs.LY 	
 		// [0][1]				@IDESC(DPT110),	@MEMBERS(EOP_RTL, WOS), WP 					
 		// [0][1]				@IDESC(DPT110),	@MEMBERS(EOP_RTL, WOS), OP 					
 		//
 		//
 		// After the first pass through this set of tuples we have:
 		//
 		// Symetric Group#		Members
 		// ---------------		------------------------------
 		// [0][0]				@IDESC(DPT110),	BOP_RTL, WP 								
 		// [0][0]				@IDESC(DPT110),	BOP_RTL, LY 					
 		// [0][0]				@IDESC(DPT110),	BOP_RTL, WP.vs.LY 						
 		// [0][0]				@IDESC(DPT110),	SLS_RTL, WP 					
 		// [0][0]				@IDESC(DPT110),	SLS_RTL, LY 							
 		// [0][0]				@IDESC(DPT110),	SLS_RTL, WP.vs.LY 			
 		// [0][0]				@IDESC(DPT110),	SLS_AUR, WP 					
 		// [0][0]				@IDESC(DPT110),	SLS_AUR, LY 
 		// [0][0]				@IDESC(DPT110),	SLS_AUR, WP.vs.LY 							
 		// [0][1]				@IDESC(DPT110),	EOP_RTL, WP 						
 		// [0][1]				@IDESC(DPT110),	EOP_RTL, OP 				
 		// [0][1]				@IDESC(DPT110),	WOS, WP 					
 		// [0][1]				@IDESC(DPT110),	WOS, OP 					
         //
 		//
 		// Ultimately, we end up with the following after fully expanding this set of tuples,
 		// making sure to iterate through the inner axis dimensions first.
 		//
 		// Symetric Group#		Members
 		// ---------------		------------------------------
 		// [0][0]				DPT110,	BOP_RTL, WP 								
 		// [0][0]				DPT110,	BOP_RTL, LY 					
 		// [0][0]				DPT110,	BOP_RTL, WP.vs.LY 						
 		// [0][0]				DPT110,	SLS_RTL, WP 					
 		// [0][0]				DPT110,	SLS_RTL, LY 							
 		// [0][0]				DPT110,	SLS_RTL, WP.vs.LY 			
 		// [0][0]				DPT110,	SLS_AUR, WP 					
 		// [0][0]				DPT110,	SLS_AUR, LY 
 		// [0][0]				DPT110,	SLS_AUR, WP.vs.LY 							
 		// [0][1]				DPT110,	EOP_RTL, WP 										
 		// [0][1]				DPT110,	EOP_RTL, OP 										
 		// [0][1]				DPT110,	WOS, WP 												
 		// [0][1]				DPT110,	WOS, OP 												
 		// [0][1]				DPT110,	EOP_RTL, WP 										
 		// [0][1]				DPT110,	EOP_RTL, OP 										
 		// [0][1]				DPT110,	WOS, WP 												
 		// [0][1]				DPT110,	WOS, OP 												
 		// [0][0]				CLS110-00, BOP_RTL, WP 								
 		// [0][0]				CLS110-00, BOP_RTL, LY 					
 		// [0][0]				CLS110-00, BOP_RTL, WP.vs.LY 						
 		// [0][0]				CLS110-00, SLS_RTL, WP 					
 		// [0][0]				CLS110-00, SLS_RTL, LY 							
 		// [0][0]				CLS110-00, SLS_RTL, WP.vs.LY 			
 		// [0][0]				CLS110-00, SLS_AUR, WP 					
 		// [0][0]				CLS110-00, SLS_AUR, LY 
 		// [0][0]				CLS110-00, SLS_AUR, WP.vs.LY 							
 		// [0][1]				CLS110-00, EOP_RTL, WP 										
 		// [0][1]				CLS110-00, EOP_RTL, OP 										
 		// [0][1]				CLS110-00, WOS, WP 												
 		// [0][1]				CLS110-00, WOS, OP 												
 		// [0][1]				CLS110-00,	EOP_RTL, OP 								
 		// [0][1]				CLS110-00,	WOS, WP 										
 		// [0][1]				CLS110-00,	WOS, OP 										
 		// [0][0]				CLS110-10, BOP_RTL, WP 								
 		// [0][0]				CLS110-10, BOP_RTL, LY 					
 		// [0][0]				CLS110-10, BOP_RTL, WP.vs.LY 						
 		// [0][0]				CLS110-10, SLS_RTL, WP 					
 		// [0][0]				CLS110-10, SLS_RTL, LY 							
 		// [0][0]				CLS110-10, SLS_RTL, WP.vs.LY 			
 		// [0][0]				CLS110-10, SLS_AUR, WP 					
 		// [0][0]				CLS110-10, SLS_AUR, LY 
 		// [0][0]				CLS110-10, SLS_AUR, WP.vs.LY 							
 		// [0][1]				CLS110-00,	EOP_RTL, WP 										
 		// [0][1]				CLS110-10,	EOP_RTL, WP 								
 		// [0][1]				CLS110-10,	EOP_RTL, OP 										
 		// [0][1]				CLS110-10,	WOS, WP 									
 		// [0][1]				CLS110-10,	WOS, OP 									
         //
 
 		// Perform pre-procssing for filtering of invalid attribute member intersections (TTN-1469)
 		boolean hasAttributes = false;
 		List<String> expandedAxisDims = new ArrayList<String>();
 		Set<String> attrBaseDims = null;
 		Map<String, List<String>> attrByBaseDim = new HashMap<String, List<String>>();
 		if (tupleAttrDims.size() > 0) {
 
 			// Get the pending list of expanded axis dimensions (include page dimensions). This
 			// list determines which dimensions will be validated. Only dimension axes that
 			// have been expanded (resolved) can be filtered on.
 			for (int i = axes.length -1; i >= axisIndex; i--) {
 				expandedAxisDims.add(axes[i]);
 			}
 			expandedAxisDims.addAll(Arrays.asList(pageAxisDims));
 
 			// Build a collection of tuple attribute dimensions by base dimension
 			attrBaseDims = getBaseDimNamesWithAttributes();
 			attrBaseDims.retainAll(expandedAxisDims);
 			attrByBaseDim = new HashMap<String, List<String>>();
 			for (String baseDim : attrBaseDims) {
 
 				// Select attribute dimension if it is contained on the page or expanded axis tuple
 				Set<String> attrDims = getBaseTree(baseDim).getAttributeDimNames();
 				for (String attrDim : attrDims) {
 					if (expandedAxisDims.contains(attrDim)) {
 						if (attrByBaseDim.get(baseDim) == null) {
 							attrByBaseDim.put(baseDim, new ArrayList<String>());
 						}
 						attrByBaseDim.get(baseDim).add(attrDim);
 					}
 				}
 			}
 
 			// Final housekeeping
 			if (attrByBaseDim.size() > 0) {
 				hasAttributes = true;
 			}
 			
 		}
 
 		
         // Loop through each row tuple, preserving the original formatting 
 		// and original tuple order.
  		while (tupleInx < tupleCount) {
  			
 			// Group all tuples containing the same groupNo prefix corresponding
  			// to the selected axis
 			Integer[] symetricGroupNo = origViewTuples[tupleInx].getSymetricGroupNo();
 			System.arraycopy(symetricGroupNo, 0, prevGroupNoPrefix, 0, groupNoPrefixSize);
 			tupleGroup = new ArrayList<ViewTuple>();
 			Integer[] groupNoPrefix = new Integer[groupNoPrefixSize];
 			while (tupleInx < tupleCount) {
 				symetricGroupNo = origViewTuples[tupleInx].getSymetricGroupNo();
 				System.arraycopy(symetricGroupNo, 0, groupNoPrefix, 0, groupNoPrefixSize);
 				if (!Arrays.deepEquals(groupNoPrefix, prevGroupNoPrefix)) {
 					break;
 				}
 				tupleGroup.add(origViewTuples[tupleInx++]);
 			}				
 
 			// Expand the tuple group by cloning the current set of tuples for each
 			// term in the set of expanded members for this axis.
 			List<ViewTuple> expandedTupleGroup = new ArrayList<ViewTuple>();
 			ViewTuple firstTuple = tupleGroup.get(0);
 			String groupTerm = firstTuple.getMemberDefs()[axisIndex];
 			boolean groupParentFirst = firstTuple.getParentFirst();
 			String[] expandedGroupTerms = expandExpression(groupTerm, groupParentFirst, dimToExpand , clientState);
 			for (String expandedTerm:expandedGroupTerms) {
 				for (ViewTuple viewTuple:tupleGroup) {
 				
 					// Take a first pass at filtering out invalid attribute member intersections among this row/col
 					// tuple and the page tuple. Any remaining invalid tuples are filtered out in 
 					// ViewService.ProcessInvalidTuples(). Tuples containing PafBlank or Member Tags are exempt 
 					// from the filtering process. (TTN-1469)
 					boolean validTupleExpansion = true;
 					if (hasAttributes && !viewTuple.containsPafBlank() && !viewTuple.isMemberTag()){
 						
 						// Cycle through each base dimension that has at least one attribute dimension
 						// on the current row/col tuple or page tuple. Then search the current axis, any
 						// previously expanded axis, and the page tuple for the base member and 
 						// corresponding attribute member intersections being validated.
 						for (String baseDim : attrByBaseDim.keySet()) {
 							
 							// Initialization
 							String baseMember = null;
 							List<String> attrDimList = attrByBaseDim.get(baseDim);
 							String[] attrDims = new String[attrDimList.size()];
 							String[] attrIs = new String[attrDimList.size()];
 							int attrIsIndex = 0;
 							
 							// Check current tuple axis
 							if (dimToExpand.equals(baseDim)) {
 								baseMember = expandedTerm;
 							} else if (attrDimList.contains(dimToExpand)) {
 								attrDims[attrIsIndex] = dimToExpand;
 								attrIs[attrIsIndex++] = expandedTerm;
 							}
 							
 							// Check previously expanded tuple axes
 							for (int i = axes.length -1; i > axisIndex; i--) {
 								String dim = axes[i];
 								if (dim.equals(baseDim)) {
 									baseMember = viewTuple.getMemberDefs()[i];
 								} else if (attrDimList.contains(dim)) {
 									attrDims[attrIsIndex] = dim;
 									attrIs[attrIsIndex++] = viewTuple.getMemberDefs()[i];
 								}
 							}
 							
 							// Check page dimensions
 							for (int i = 0; i < pageAxisDims.length; i++) {
 								String dim = pageAxisDims[i];
 								if (dim.equals(baseDim)) {
 									baseMember = pageAxisDims[i];
 								} else if (attrDimList.contains(dim)) {
 									attrDims[attrIsIndex] = dim;
 									attrIs[attrIsIndex++] = pageAxisDims[i];
 								}
 							}
 							
 							// Skip any invalid attribute intersections
 							if (AttributeUtil.isInvalidAttributeCombo(baseDim, baseMember, attrDims, attrIs, getAllDimTrees())) {
 								validTupleExpansion = false;
 								break;
 							}
 
 						}
 					}
 
 					// Tuple expansion
 					if (validTupleExpansion) {
 						ViewTuple newViewTuple = viewTuple.clone();
 						newViewTuple.getMemberDefs()[axisIndex] = expandedTerm;
 						expandedTupleGroup.add(newViewTuple);
 					}
 				}
 			}	
 			expandedTuples.addAll(expandedTupleGroup);
 		}		
 
  		// Return expanded tuples
 		return expandedTuples;
 	}
 
 	private boolean isBlankViewTuple(ViewTuple viewTuple) {
 
 		String[] members = viewTuple.getMemberDefs();
 		if (members != null) {
 			for (String member : members) {
 				if ( member.equals(PafBaseConstants.PAF_BLANK)) {
 					return true;
 				}
 			}
 		}  		
 		return false;    	
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param viewTuple
 	 * @param axisIndex
 	 * @param dim
 	 * @param clientId
 	 * 
 	 * @return List<ViewTuple>
 	 * @throws PafException 
 	 */
 	private List<ViewTuple> expandTuple(ViewTuple viewTuple, int axisIndex, String dim, PafClientState clientState) throws PafException {
 		ArrayList<ViewTuple> expTuples = new ArrayList<ViewTuple>();
 		String term = viewTuple.getMemberDefs()[axisIndex];
 		if (term.contains("@")) {
 			ExpOperation expOp = new ExpOperation(term);
 			String [] expTerms = resolveExpOperation(expOp, viewTuple.getParentFirst(), dim, clientState);
 			// Special year dimension logic for view rendering - if no children are found then 
 			// use specified member. This will allow @CHILD(@UOWROOT(Year)) to work in both a
 			// single year and multiple-year UOW. (TTN-1595)
 			if (expTerms.length == 0 && dim.equals(clientState.getApp().getMdbDef().getYearDim())) {
 				expTerms = new String[]{expOp.getParms()[0]};
 			}
 			for (String expTerm : expTerms) {
 				ViewTuple vt = viewTuple.clone();
 				vt.getMemberDefs()[axisIndex] = expTerm;
 				expTuples.add(vt);
 			}
 		}
 		else {
 			expTuples.add(viewTuple);
 		}
 		return expTuples;        
 	}
 
 
 
 	private String[] expandExpression(String term, boolean parentFirst, String dim, PafClientState clientState) throws PafException {
 		String [] expTerms;
 		if (term.contains("@")) {
 			ExpOperation expOp = new ExpOperation(term);
 			expTerms = resolveExpOperation(expOp, parentFirst, dim, clientState);
 		}
 		else {
 			expTerms = new String[] {term};
 		}
 		return expTerms;
 	}
 
 
 
 	/**
 	 *	Expands a multidemnsional operator for the specified dimension and returns corresponding members
 	 *
 	 * @param expOp
 	 * @param parentFirst Indicates whether the parent member should appear before or after it's children
 	 * @param dim
 	 * @param clientId If this is null than the full tree for the specified dimension is used. Otherwise
 	 * the subtree appropriate for that client will be used.
 	 * 
 	 * @return String[]
 	 * @throws PafException 
 	 */
 	private String[] resolveExpOperation(ExpOperation expOp, Boolean parentFirst, String dim, PafClientState clientState) throws PafException {
 
 		PafDimTree tree; 
 		if (clientState == null)
 			tree = baseTrees.get(dim);
 		else
 			tree = clientState.getUowTrees().getTree(dim);
 
 		// Check for null member Tree   
 		if (tree == null) {
 			String errMsg = "Dimension tree: [" + dim + "] does not exist";
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error );
 			throw pfe;
 		}
 
 		List<PafDimMember> memberList = null;
 
 		switch (expOp.getOpCode()) {
 		case CHILDREN: 
 			memberList = tree.getChildren(expOp.getParms()[0]);
 			break;
 
 		case ICHILDREN: 
 			memberList = tree.getIChildren(expOp.getParms()[0], parentFirst);
 			break;
 
 		case DESC: 
 
 			if (expOp.getParms().length == 1) {
 				memberList = tree.getDescendants(expOp.getParms()[0], parentFirst);
 			} else {
 
 				memberList = tree
 				.getDescendants(expOp.getParms()[0], LevelGenParamUtil.getLevelGenType(expOp.getParms()[1]),
 						LevelGenParamUtil.getLevelGenNumber(expOp.getParms()[1]), parentFirst);
 
 			}
 
 			break;
 
 		case IDESC:
 
 			if (expOp.getParms().length == 1) {
 				memberList = tree.getIDescendants(expOp.getParms()[0], parentFirst);
 			} else {
 
 				memberList = tree
 				.getIDescendants(expOp.getParms()[0], LevelGenParamUtil.getLevelGenType(
 						expOp.getParms()[1]), LevelGenParamUtil.getLevelGenNumber(expOp.getParms()[1]), parentFirst);
 
 			}
 
 			break;
 
 		case LEVEL:
 			memberList = tree.getMembersAtLevel(expOp.getParms()[0], Short.parseShort(expOp.getParms()[1]));
 			break;
 
 		case GEN:
 			memberList = tree.getMembersAtGen(expOp.getParms()[0], Short.parseShort(expOp.getParms()[1]));
 			break;
 
 		case MEMBERS: 
 			memberList = new ArrayList<PafDimMember>();
 			
 			for (String m : expOp.getParms()) {
 
 				if ( m.equals(PafBaseConstants.PAF_BLANK)) {
 					memberList.add(new PafBaseMember(m));
 				} else {
 
 					//get dim member from tree
 					PafDimMember newPafDimMember = tree.getMember(m);
 					
 					//if memberList has members in it, get last member in tree and see
 					//if last and current members are same, if so..don't add dup to list.
 					if ( memberList.size() > 0 ) {
 						
 						//get last dim member
 						PafDimMember lastPafDimMember = memberList.get(memberList.size() -1);
 						
 						//if last = new, continue to next member
 						if ( lastPafDimMember.equals(newPafDimMember)) {
 							continue;
 						}
 					}
 					
 					//add new paf dim member to list
 					memberList.add(newPafDimMember);
 					
 				}
 			}
 			break;
 		}
 
 		//if member list doesn't have any members, throw exception.
 		if ( memberList.size() == 0 ) {
 			
 			throw new PafException("No members found for dimension " + dim + ". Please check view and security settings.", PafErrSeverity.Error);
 			
 		}
 		
 		int i=0;
 		String[] memberNames = new String[memberList.size()];
 		for (PafDimMember m : memberList)
 			memberNames[i++] = m.getKey();
 
 		return memberNames;
 	}
 
 
 	/**
 	 *	Create a collection of member indexes for specified member tree
 	 *
 	 * @param memberTree
 	 * @param order
 	 * @return HashMap<String, Integer>
 	 */
 	private HashMap<String, Integer> createMemberIndexList(PafDimTree memberTree, TreeTraversalOrder order) {
 
 		HashMap<String, Integer> memberList = new HashMap<String, Integer>();
 		String[] memberNames;
 		memberNames = memberTree.getMemberNames(order).toArray(new String[0]);
 
 		int i = 0;
 		for (String member : memberNames) {
 			// Don't overwrite any existing member entries. Shared hiearchies
 			// cause inconsistencies with the index numbers.
 			if (memberList.get(member) == null) {
 				memberList.put(member, i++);
 			}
 		}
 
 		return memberList;
 	}
 
 //	/**
 //	 *	Method_description_goes_here
 //	 *
 //	 * @param dimName
 //	 * @return Map<String, Integer>
 //	 */
 //	public Map<String, Integer> getMemberIndexList(String dimName) {
 //		if (!memberIndexLists.containsKey(dimName)) throw new IllegalArgumentException("Dimension name not found in Dimension Index List structure. Dimension: " + dimName);
 //		return memberIndexLists.get(dimName);
 //	}
 //
 //	/**
 //	 *	Method_description_goes_here
 //	 *
 //	 * @return Map<String, HashMap<String, Integer>>
 //	 */
 //	public HashMap<String, HashMap<String, Integer>> getMemberIndexLists() {
 //		return memberIndexLists;
 //	}
 
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @return PafDataService
 	 */
 	public static PafDataService getInstance() {
 
 		if (_instance == null) {
 
 			_instance = new PafDataService();
 		}
 		return _instance;
 	}
 
 
 	/**
 	 *	Get all dimension trees - attribute and base
 	 *
 	 * @return Map<String,PafDimTree>
 	 */
 	public Map<String,PafDimTree> getAllDimTrees() {
 
 		Map<String,PafDimTree> allTrees = new HashMap<String,PafDimTree>();
 
 		// Add attribute trees to collection
 		allTrees.putAll(getAttributeTrees());
 
 		// Add base trees to collection
 		allTrees.putAll(getBaseTrees());
 
 		return allTrees;
 	}
 
 	/**
 	 *	Return dimension tree for specified dimension
 	 *
 	 * @param dimension Specified dimension name
 	 * @return PafDimTree
 	 */
 	public PafDimTree getDimTree(String dimension) {
 		return getAllDimTrees().get(dimension);
 	}
 
 
 	/**
 	 *	Get simple versions of all dimension trees - attribute and base
 	 *
 	 * @return Set<PafSimpleDimTree>
 	 * @throws PafException 
 	 */
 	public Set<PafSimpleDimTree> getAllSimpleDimTrees() throws PafException {
 
 		Set<PafSimpleDimTree> allSimpleTrees = new HashSet<PafSimpleDimTree>();
 
 		// Get simple version of each dimension tree
 		for (String dimension:getAllDimTrees().keySet()) {
 			allSimpleTrees.add(getSimpleTree(dimension));
 		}
 
 		// Return simple trees
 		return allSimpleTrees;
 	}
 
 	/**
 	 *	Return simple dimension tree for specified dimension
 	 *
 	 * @param dimension Specified dimension name
 	 * 
 	 * @return PafSimpleTree
 	 * @throws PafException 
 	 */
 	public PafSimpleDimTree getSimpleTree(String dimension) throws PafException {
 
 		PafDimTree dimTree = null;
 		PafSimpleDimTree simpleTree = null;
 
 		// Get selected dimension tree
 		dimTree = getAllDimTrees().get(dimension);
 
 		// Get simple version of dimension tree. 
 		simpleTree = dimTree.getSimpleVersion();     		
 
 		// Return simple tree
 		return simpleTree;
 	}
 
 
 	/**
 	 * Determine if a dimension is an attribute dimension
 	 * pmack
 	 * @param dimName 
 	 * @return boolean
 	 */
 	public boolean isAttributeDimension(String dimName){
 
 		boolean isAttributeDim = false;
 
 		if (getAttributeDimNames().contains(dimName)){
 			isAttributeDim = true;
 		}
 
 		return isAttributeDim;
 	}
 
 	/**
 	 *	Return set of attribute dimension names
 	 *
 	 * @return Set<String>
 	 */
 	public Set<String> getAttributeDimNames() {
 
 		// Return empy set if there are no attribute trees
 		Set<String> dimNames = null;
 		if (attributeTrees != null) {
 			dimNames = attributeTrees.keySet();
 		} else {
 			dimNames = new HashSet<String>();
 		}
 		return dimNames;
 	}
 
 	/**
 	 *	Return base tree associated with specified attribute dimension
 	 *
 	 * @param attrDimName Specified attribute dimension name
 	 * @return PafBaseTree
 	 */
 	public PafBaseTree getAssociatedBaseTree(String attrDimName) {
 
 		// Validate attribute dimension name 
 		if (!getAttributeTrees().containsKey(attrDimName)) {
 			String errMsg = "Unable to retrieve associated attribute dimension - invalid attribute dimension specified.";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          						
 		}
 
 		// Get associated base tree
 		String baseDimName = getAttributeTree(attrDimName).getBaseDimName();
 		PafBaseTree baseTree = getBaseTree(baseDimName);
 
 		// Return associated base tree
 		return baseTree;
 	}
 
 
 	/**
 	 *	Return the invalid attribute member combinations for the specified
 	 *  base tree, base member, and attribute dimension(s)
 	 * 
 	 * @param baseDimName Base dimension name
 	 * @param baseMemberName Base member name
 	 * @param attrDimNames Array of attribute dimension name(s)
 	 *
 	 * @return Set<Intersection>
 	 */
 	@SuppressWarnings("unchecked")
 	public Set<Intersection> getInvalidAttributeCombos(String baseDimName, String baseMemberName, String[] attrDimNames)  {
 
 		int attrIndex = 0;
 		Set<Intersection> invalidAttrCombos = null;
 		Set<Intersection> validAttrCombos = null;
 		List<String>[] allAttrMembers = null;
 		PafBaseTree baseTree = null;
 
 		// Get baseMemberTree and baseMember
 		baseTree = getBaseTree(baseDimName);
 
 		// Get all possible attribute member intersections
 		allAttrMembers = new List[attrDimNames.length];
 		for (String attrDimName:attrDimNames) {
 
 			// Verify attribute dimension name
 			PafDimTree attrTree = null;
 			if (baseTree.getAttributeDimNames().contains(attrDimName)) {
 				attrTree = getAttributeTree(attrDimName);
 			} else {
 				String errMsg = "Unable to get invalid attribute intersections - [" + attrDimName
 				+ "] is not an attribute dimension mapped to the base dimension: " + baseDimName;
 				logger.error(errMsg);
 				IllegalArgumentException iae = new IllegalArgumentException(errMsg);
 				throw(iae);
 			}
 
 			// Add all members of current attribute dimension
 			List<String> dimMembers = attrTree.getMemberNames(TreeTraversalOrder.POST_ORDER);
 			allAttrMembers[attrIndex] = dimMembers;
 			attrIndex++;
 		}
 
 		// Create cross product of all valid attribute members across
 		// each attribute dimension
 		int intersectionCount = 1;
 		for (List<String> members:allAttrMembers) {
 			intersectionCount *= members.size();
 		}
 		invalidAttrCombos = new HashSet<Intersection>(intersectionCount);
 		Odometer isIterator = new Odometer(allAttrMembers);
 		List<String> isList = null;
 		while (isIterator.hasNext()) {
 
 			// Get next intersection (in list form)
 			isList = isIterator.nextValue();
 
 			// Convert list into custom intersection object
 			Intersection is = new Intersection(attrDimNames, isList.toArray(new String[0]));
 
 			// Add intersection to intersection collection
 			invalidAttrCombos.add(is);
 		}
 
 		// Remove valid intersections
 		validAttrCombos = AttributeUtil.getValidAttributeCombos(baseDimName, baseMemberName, attrDimNames, getAllDimTrees());
 		invalidAttrCombos.removeAll(validAttrCombos);
 
 		// Return invalid intersections
 		return invalidAttrCombos;
 	}
 
 
 	/**
 	 *	Return attribute tree for specified dimension
 	 *
 	 * @param dimension Specified dimension name
 	 * @return PafAttributeTree
 	 */
 	public PafAttributeTree getAttributeTree(String dimension) {
 		return attributeTrees.get(dimension);
 	}
 
 	/**
 	 * @return the attributeTrees
 	 */
 	public Map<String, PafAttributeTree> getAttributeTrees() {
 		return attributeTrees;
 	}
 
 	/**
 	 * @param attributeTrees the attributeTrees to set
 	 */
 	public void setAttributeTrees(Map<String, PafAttributeTree> attributeTrees) {
 		this.attributeTrees = attributeTrees;
 	}
 
 	/**
 	 *	Return set of names for any attribute dimensions corresponding to specified base dimension
 	 *
 	 * @param baseDimName Base dimension name
 	 * @return Map<String, PafAttributeTree>
 	 */
 	public Map<String, PafAttributeTree> getAttributeTrees(String baseDimName) {
 
 		Set<String> attrDimNames = null;
 		Map<String, PafAttributeTree> attrTrees = new HashMap <String, PafAttributeTree>();
 
 		// Validate base dimension name
 		if (!getBaseTrees().containsKey(baseDimName)) {
 			String errMsg = "Unable to retrieve attribute trees - invalid base dimension: " + baseDimName
 			+ " specified.";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);
 			throw iae;
 		}
 
 		// Get names of attribute dimensions corresponding to base dimension
 		attrDimNames = getBaseTree(baseDimName).getAttributeDimNames();
 
 		// Add all selected trees to collection
 		for (String attrDimName:attrDimNames) {
 			attrTrees.put(attrDimName, getAttributeTree(attrDimName));			
 		}
 
 		// Return trees, or empty set if no match is found
 		return attrTrees;
 	}
 
 
 	/**
 	 *	Return set of base dimension names
 	 *
 	 * @return Set<String>
 	 */
 	public Set<String> getBaseDimNames() {
 		return baseTrees.keySet();
 	}
 
 	/**
 	 *	Return set of names of any base dimensions that have been
 	 *  assigned one or more attribute dimensions
 	 *
 	 * @return Set<String>
 	 */
 	public Set<String> getBaseDimNamesWithAttributes() {
 
 		Set<String> baseDimNames = new HashSet<String>();
 		Map<String, PafAttributeTree> attrTrees = getAttributeTrees();
 
 		// Return empty set if no attribute dimensions exist
 		if (attrTrees == null) {
 			return baseDimNames;
 		}
 
 		// Iterate through all attribute dimensions and append all associated
 		// base dimensions to base dimension name set
 		for (PafAttributeTree attrTree:attrTrees.values()) {
 			String baseDimName = attrTree.getBaseDimName();
 			if (!baseDimNames.contains(baseDimName)) {
 				baseDimNames.add(baseDimName);
 			}			
 		}
 
 		// Return all base dimensions that have been assigned one or more attribute dimensions
 		return baseDimNames;
 	}
 
 	/**
 	 *	Return set of names of any base dimensions that have not been
 	 *  assigned any attribute dimensions
 	 *
 	 * @return Set<String>
 	 */
 	public Set<String> getBaseDimNamesWithoutAttributes() {
 		
 		Set<String> baseDimsWithoutAttrs = new HashSet<String>(getBaseDimNames());
 		baseDimsWithoutAttrs.removeAll(getBaseDimNamesWithAttributes());
 		return baseDimsWithoutAttrs;
 	}
 
 	/**
 	 *	Return set of base members corresponding to specified attribute dimension and member
 	 *
 	 * @param attrDimName Attribute dimension name
 	 * @param attrMemberName Attribute member
 	 *
 	 * @return Set<PafBaseMember>
 	 */
 	public Set<PafBaseMember> getBaseMembers(String attrDimName, String attrMemberName) {
 
 		Set<String> baseMemberNames = null;
 		Set<PafBaseMember> baseMembers = new HashSet<PafBaseMember>();
 		PafAttributeTree attrTree = null;
 		PafBaseTree baseTree = null;
 
 		// Validate attribute dimension name 
 		if (getAttributeTrees().containsKey(attrDimName)) {
 			attrTree = getAttributeTrees().get(attrDimName);
 		} else {
 			String errMsg = "Unable to retrieve base members - invalid attribute dimension specified.";
 			logger.error(errMsg);
 			IllegalArgumentException iae = new IllegalArgumentException(errMsg);    
 			throw iae;          						
 		}
 
 		// Get corresponding base member names
 		baseMemberNames = attrTree.getBaseMemberNames(attrMemberName);
 
 		// Convert all base member names to their corresponding base member objects
 		baseTree = getBaseTree(attrTree.getBaseDimName());
 		for (String baseMemberName:baseMemberNames) {
 			baseMembers.add(baseTree.getMember(baseMemberName));
 		}
 
 		// Return base members, or empty set if no match is found
 		return baseMembers;
 	}
 
 	/**
 	 * @return the baseTrees
 	 */
 	public Map<String, PafBaseTree> getBaseTrees() {
 		return baseTrees;
 	}    
 	/**
 	 * @param baseTrees the baseTrees to set
 	 */
 	public void setBaseTrees(Map<String, PafBaseTree> baseTrees) {
 		this.baseTrees = baseTrees;
 	}
 
 	/**
 	 *	Return base tree for specified dimension
 	 *
 	 * @param dimension Specified dimension name
 	 * @return PafBaseTree
 	 */
 	public PafBaseTree getBaseTree(String dimension) {
 		return getBaseTrees().get(dimension);
 	}
 
 
 	/**
 	 *	Return collection of base dimension trees that have been 
 	 *  assigned one or more attribute dimensions
 	 *  
 	 * @return Map<String, PafBaseTree>
 	 */
 	public Map<String, PafBaseTree> getBaseTreesWithAttributes() {
 
 		Set<String> baseDimNames = null;
 		Map<String, PafBaseTree> baseDimTrees = new HashMap<String, PafBaseTree>();
 
 		// Get list of base dimensions containing one or more attribute dimensions
 		baseDimNames = getBaseDimNamesWithAttributes();
 
 		// Iterate through list of base dimension names, get associated tree, 
 		// and add to base tree collection
 		for (String baseDimName:baseDimNames) {
 			baseDimTrees.put(baseDimName, getBaseTree(baseDimName));
 		}
 
 		// Return selected base trees
 		return baseDimTrees;
 	}
 
 	/**
 	 *	Evaluate the default rule set if warranted by the client state paf
 	 *  planner configuration settings.
 	 *
 	 * @param clientState Client state object
 	 * @throws PafException
 	 */
 	public void evaluateDefaultRuleset(PafClientState clientState) throws PafException {
 
 		PafPlannerConfig plannerConfig = clientState.getPlannerConfig();
 
 		// Determine if a default evaluation is warranted. Only execute the default
 		// evaluation process if at least one of the following conditions have been met:
 		//
 		// 1. The default eval enabled working version flag is set to true
 		// 2. At least one version has been specified in the "defaultEvalRevVersion" tag
 		//
 		if (!plannerConfig.isDefaultEvalEnabledWorkingVersion() &&
 				(plannerConfig.getDefaultEvalRefVersions() == null  || plannerConfig.getDefaultEvalRefVersions().length == 0)) {
 			return;
 		}
 
 		
 		// Initialization
 		logger.info("Executing Default Strategy");
 		RuleBasedEvalStrategy evalStrategy = new RuleBasedEvalStrategy();
 		PafDataCache cache = getDataCache(clientState.getClientId());
 		EvalState evalState = new EvalState(clientState, cache);
 		evalState.setMeasureRuleSet(clientState.getDefaultMsrRuleset());
 
 		// Perform default evaluation
 		evalState.setDefaultEvalStep(true);
 		evalStrategy.executeDefaultStrategy(evalState);   
 		evalState.setDefaultEvalStep(false);
 
 		// Push updated data into multi-dimensional database for consistency 
 		// in case of aggregate changes, only if corresponding planner config 
 		// option is set.
 		if (plannerConfig.isMdbSaveWorkingVersionOnUowLoad()) {
 			String dsId = clientState.getApp().getMdbDef().getDataSourceId();
 			logger.info("Saving datacache to data provider: [" + dsId + "]" );           
 			this.saveDataCache(clientState);
 		}
 	}
 	
 	/**
 	 *	Evaluate view cell changes
 	 *
 	 * @param evalRequest Evaluation request object
 	 * @param clientState Client state object
 	 * @param dataCache
 	 * @param sliceParms
 	 * @return void
 	 * @throws PafException
 	 */
 	public void evaluateView(EvaluateViewRequest evalRequest, PafClientState clientState, PafDataCache dataCache, PafDataSliceParms sliceParms) throws PafException {
 
 		PafView currentView = clientState.getView(evalRequest.getViewName());
 		logger.info("Evaluating view: " + currentView.getName() + " for client " + evalRequest.getClientId() + " using measure set " + evalRequest.getMeasureSetKey());
 		PafDataSlice newSlice = evalRequest.getDataSlice();
 		PafMVS pafMVS = dataCache.getPafMVS();
 		PafApplicationDef appDef = clientState.getApp();
 		PafViewSection currentViewSection = pafMVS.getViewSection();
 		String measureDim = appDef.getMdbDef().getMeasureDim();
 		String versionDim = appDef.getMdbDef().getVersionDim();
 		boolean hasAttributes = currentViewSection.hasAttributes();
 
 		if (newSlice.isCompressed()) {
 			logger.info("Uncompressing data slice\n" );
 			newSlice.uncompressData();
 		}
 
 		// Calculate attribute intersections for off-screen measures. This 
 		// step is only needed during the first evaluation pass for a each
 		// view, and after the data cache has been refreshed.
 		if (hasAttributes && !pafMVS.isInitializedForAttrEval()) {
 
 			// Create member filter containing list of off-screen measures
 			Map<String, List<String>> memberFilter = new HashMap<String, List<String>>();
 			List<String> measureList = new ArrayList<String>(Arrays.asList(dataCache.getDimMembers(measureDim)));
 			measureList.removeAll(Arrays.asList(sliceParms.getMembers(measureDim)));
 			memberFilter.put(measureDim, measureList);
 
 			// Calculate attribute intersections
 			long attrInitStart = System.currentTimeMillis();
 			PafDataCacheCalc.calcAttributeIntersections(dataCache, clientState, sliceParms,
 					memberFilter, DcTrackChangeOpt.NONE);
 			pafMVS.setInitializedForAttrEval(true);
 			String logMsg = LogUtil.timedStep("Attribute Eval Initialization", attrInitStart);
 			evalPerfLogger.info(logMsg);
 		}
 		
 		logger.info("Updating data cache with client data\n" + sliceParms.toString() );
 		updateDataCacheFromSlice(dataCache, newSlice, sliceParms);
 
 		IEvalStrategy evalStrategy = new RuleBasedEvalStrategy();
 
 		// Create evaluation state object (holds and tracks information that
 		// is key to the evaluation process)
 		SliceState sliceState = new SliceState(evalRequest);
 		sliceState.setDataSliceParms(sliceParms);
 		EvalState evalState = new EvalState(sliceState, clientState, dataCache);
 		evalState.setAxisPriority(currentViewSection.getDimensionCalcSequence());
 		evalState.setDimSequence(currentViewSection.getDimensionsPriority());
 		evalState.setAttributeEval(hasAttributes);
 
 		// Set the measure rule set. If a measure rule set name is specified,
 		// use that rule set, else just use the default rule set.
 		RuleSet measureRuleset;
 		if (evalRequest.getRuleSetName() == null || evalRequest.getRuleSetName().trim().equals("")) {
 			measureRuleset = clientState.getDefaultMsrRuleset();
 		}
 		else {
 			measureRuleset = clientState.getMsrRulsetByName(evalRequest.getRuleSetName());
 		}
 		evalState.setMeasureRuleSet(measureRuleset);
 
 		// Check for contribution percent formulas on view section
 		List<String> contribPctVersions = appDef.getContribPctVersions();
 		String[] viewSectionVersions = currentViewSection.getDimensionMembers(versionDim); 
 		for (String viewSectionVersion : viewSectionVersions) {
 			if (contribPctVersions.contains(viewSectionVersion)) {
 				evalState.setContribPctFormulas(true);
 				break;
 			}
 		}
 		
 		
 		// Perform evaluation strategy
 		logger.info("Executing Strategy");
 		dataCache = evalStrategy.executeStrategy(evalState);
 
 		logger.info("Evaluation Complete");
 		
 	}
 	
 
 	/**
 	 *	Save updated uow cache to mdb
 	 *
 	 * @param clientState Client state object
 	 * @throws PafException
 	 */
 	public void saveDataCache(PafClientState clientState) throws PafException { 
 
 //		TTN-1406		
 //		IMdbClassLoader mdbClassLoader = this.getMdbClassLoader(connProps);
 //		IMdbData mdbData = mdbClassLoader.getMdbDataProvider();
 //		mdbData.sendData(this.uowCache.get(clientId));
 
 		String dsId = clientState.getApp().getMdbDef().getDataSourceId();
 		IPafConnectionProps connProps = clientState.getDataSources().get(dsId);
 		String clientId = clientState.getClientId();
 		this.getMdbDataProvider(connProps).sendData(this.uowCache.get(clientId), clientState);
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param memberName
 	 * @return String
 	 */
 	public String findMemberAxis(String memberName) {
 		String axis = "";
 		for (PafBaseTree tree: this.baseTrees.values()) {
 			if (tree.hasMember(memberName)) axis = tree.getRootNode().getKey();
 		}
 		// TODO Auto-generated method stub
 		return axis;
 	}
 	
 	public List<SecurityGroup> getGroups(String app){
 		Session session = PafMetaData.currentPafClientCacheDBSession();
 		Transaction tx = null;
 		List<SecurityGroup> securityGroups = null;
 		
 		//Add administrator check here
 
 		
 		try {
 			
 			tx = session.beginTransaction();
 						
 			securityGroups = session.createQuery("from SecurityGroup where application.name = '" + app + "'").list();	
 			
 			
 		} catch (RuntimeException ex) {
 
 			try {
 				if ( tx != null ) {
 					//roll back if runtime exception occurred
 					tx.rollback();
 				}
 				
 			} catch (RuntimeException rbEx) {
 				
 				//log exception
 				PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 			}
 			
 			//log exception
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 		}  
 		
 		return securityGroups;
 	}
 	
 	
 	public boolean setGroups(List<SecurityGroup> securityGroups, String app) {
 		boolean isSuccess = true;
 		Session session = PafMetaData.currentPafClientCacheDBSession();
 		Transaction tx = null;
 		
 		//Add administrator check here
 				
 		//get application
 		Map<String, Application> applicationMap = getApplicationMap(session);
 		
 		try {
 			
 			tx = session.beginTransaction();
 		
 			Query deleteQuery = session.createQuery("delete from SecurityGroup sg where sg in (from SecurityGroup where application.name = ?)").setParameter(0, app);
 			
 			int recordsUpdated = deleteQuery.executeUpdate();
 			
 			logger.info("Cleared " + recordsUpdated + " Security Group records.");
 			
 			tx.commit();
 			
 		} catch (RuntimeException ex) {
 
 			try {
 				if ( tx != null ) {
 					//rollback if runtime exception occurred
 					tx.rollback();
 				}
 				
 			} catch (RuntimeException rbEx) {
 				
 				//log exception
 				PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 			}
 			
 			//log exception
 			isSuccess = false;
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 			return isSuccess;
 		} 
 		
 		if (securityGroups != null){
 			//begin a new transaction
 			tx = session.beginTransaction();
 
 			for (SecurityGroup securityGroup : securityGroups){
 
 				//get app id from cell note
 				String appName = securityGroup.getApplication().getName();
 
 				//if in application map, use, otherwise add to map as new.
 				if( appName != null && applicationMap.containsKey(appName)) {
 
 					securityGroup.setApplication(applicationMap.get(appName));
 				}else{
 
 					applicationMap.put(appName, securityGroup.getApplication());
 				}
 
 				try {
 					//save the new one
 					session.save(securityGroup);
 				} catch (RuntimeException ex) {
 					try {
 
 						if ( tx != null ) {
 							//rollback if runtime exception occurred
 							tx.rollback();
 						}
 
 					} catch (RuntimeException rbEx) {
 
 						//log exception
 						PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 					}
 
 					//log exception
 					isSuccess = false;
 					PafErrHandler.handleException(ex, PafErrSeverity.Error);
 					return isSuccess;
 				}
 			}
 
 			//commit the transaction
 			tx.commit(); 
 		}
 		
 		logger.debug("Set Security Groups - " + new Date());
 		return isSuccess;
 	}
 	
 	/**
 	 * 
 	 *  Returns a map<application name, application>.
 	 *
 	 * @param s Session to use
 	 * @return map of application names and apps
 	 */
 	private static Map<String, Application> getApplicationMap(Session s) {
 		
 		logger.info("DEBUG - Start getApplicationMap(Session) - " + new Date());
 		
 		Map<String, Application> ApplicationMap = new HashMap<String, Application>();
 		
 		//get list of apps
 		List<Application> ApplicationList = s.createQuery("from Application").list();
 		
 		if ( ApplicationList != null ) {
 		
 			//loop over apps and add to map
 			for (Application Application : ApplicationList ) {
 				
 				ApplicationMap.put(Application.getName(), Application);
 				
 			}
 			
 		}		
 		
 		logger.debug("DEBUG - End getApplicationMap(Session) - " + new Date());
 		
 		return ApplicationMap;
 		
 	}
 
 
 	/**
 	 * 
 	 */
 	private PafDataService() {	}
 
 
 	/**
 	 *	This constructor is used for UNIT TESTING only, bypassing the normal
 	 *  construction processing for the data service (ie. loading+ new trees, etc.)
 	 *
 	 * @param isTest Can be set to TRUE or FALSE
 	 */
 	private PafDataService(boolean isTest) {
 
 	}
 
 	/**
 	 *	Loads data for defined applications
 	 *  Primarily loads pafBaseMember trees for a particular application
 	 *
 	 */
 	public void loadApplicationData() {
 
 		// assumes a single application at this point
 		// loads all dimensions into the tree hashmap, so at the very least
 		// multipls apps couldn't share dimensions
 
 		// Attribute trees must be loaded first, since attribute info is needed 
 		// to populate the base trees
 		try {
 
 			List<PafApplicationDef> pafApps = PafAppService.getInstance().getApplications();
 			
 			for (PafApplicationDef pafApp : pafApps ) {
 				
 				String appId = pafApp.getAppId();
 				String appString = "for application [" + appId + "]";
 				IPafConnectionProps connProps = (IPafConnectionProps) 
 					PafMetaData.getAppContext().getBean(pafApp.getMdbDef().getDataSourceId());
 
 // 	TTN-1406 Commented out this code for the time being until we figure out a way to get custom class loader to work.	
 				
 //				// Add multi-dimensional database libraries to classpath
 //				logger.info("Loading multidimensional database class libraries " + appString);
 //				IMdbClassLoader mdbClassLoader = null;
 //				mdbClassLoader = this.getMdbClassLoader(connProps);
 //				mdbClassLoader.load();
 
 				if ( PafMetaData.getServerSettings().isAutoLoadAttributes() ) {
 					logger.info("Clearing existing attribute cache " + appString);
 					deleteAllCacheAttrDim();
 
 					IMdbMetaData metaData = null;
 					PafMdbProps mdbProps = null;
 					
 					logger.info("Retrieving attribute dimension information from Metadata provider " + appString);
 //					TTN-1406
 //					metaData = mdbClassLoader.getMetaDataProvider();
 					metaData = this.getMetaDataProvider(connProps);
 					mdbProps = metaData.getMdbProps();
 
 					logger.info("Caching attribute dimension information " + appString); 
 					String[] attrDimFilter = pafApp.getEssAttrDimFilter();
 					String[] attrDims = null;
 					if (attrDimFilter == null || attrDimFilter.length == 0) {
 						attrDims = mdbProps.getAttributeDims();
 					} else {
 						attrDims = attrDimFilter;
 						logger.info("Using attribute dimension filter: " + StringUtils.arrayToString(attrDims));
 					}
 					cacheAttributeDims(attrDims, pafApp);
 					
 					// Disconnect
 					metaData.disconnect();
 				}
 
 				logger.info("Loading attribute trees " + appString);
 				this.initAttributeMemberTreeStore();
 
 				logger.info("Loading base trees " + appString);				
 				
 				this.initMemberTreeStore(pafApp);
 																
 				this.initCellNotes(pafApp);
 			}
 		} catch (Exception ex) {
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 		}
 	}
 
 	/**
 	 * 
 	 * Initializes cell note data. 
 	 *
 	 * @param pafApp The application to initialize.
 	 * @throws PafException
 	 */
 	@SuppressWarnings("unchecked")
 	private void initCellNotes(PafApplicationDef pafApp) throws PafException {
 		
 		//if clear all is true, delete all cell notes for every app and every datasource
 		if ( PafMetaData.getServerSettings().isClearAllCellNotes() ) {
 			
 			PafCellNoteManager.getInstance().deleteCellNotes();
 		
 		//if just clear cell notes is ture, delete cell notes for current app and current datasource.
 		} else if ( PafMetaData.getServerSettings().isClearCellNotes() ) {
 			
 			PafCellNoteManager.getInstance().deleteCellNotes(pafApp.getAppId(), pafApp.getMdbDef().getDataSourceId());
 			
 		}
 		
 		IPafConnectionProps connProps = (IPafConnectionProps) 
 					PafMetaData.getAppContext().getBean(pafApp.getMdbDef().getDataSourceId());
 		
 		PafMdbProps mdbProps = this.getMdbProps(connProps);
 		
 		if ( mdbProps != null && mdbProps.getBaseDims() != null) {
 		
 			//get list of base dims
 			List<String> dimensions = new ArrayList<String>(Arrays.asList(mdbProps.getBaseDims()));
 			
 			//if cached attributes dims exists, add them to dim list
 			if ( mdbProps.getCachedAttributeDims() != null ) {
 				
 				dimensions.addAll(Arrays.asList(mdbProps.getCachedAttributeDims()));
 				
 			}
 									
 			//set current cell note dimensions
 			PafCellNoteManager.getInstance().setCurrentDimensions(dimensions.toArray(new String[0]));
 			
 		}
 		
 	}
 
 
 	/**
 	 *	Remove uow cache for selected client id
 	 *
 	 * @param clientId Client state id
 	 */
 	public void removeUowCache(String clientId) {
 		
 		if ( clientId != null ) {
 			uowCache.remove(clientId);
 			systemLockedIntersections.remove(clientId);
 		}
 	}
 
 	/**
 	 *	Return count of uow caches
 	 *
 	 * @return int
 	 */
 	public int getUowCacheCnt() {
 		return uowCache.size();
 	}
 
 	/**
 	 *	Get List of Properties from MDB
 	 *
 	 * @param connProps Connection properties
 	 *
 	 * @return MdbProps Basic multidimensional properties
 	 * @throws PafException 
 	 */
 	public PafMdbProps getMdbProps(IPafConnectionProps connProps) throws PafException {
 
 //		TTN-1406		
 //		IMdbClassLoader mdbClassLoader = this.getMdbClassLoader(connProps);
 //		IMdbMetaData metaData = mdbClassLoader.getMetaDataProvider();
 //		PafMdbProps mdbProps = metaData.getMdbProps();
 		PafMdbProps mdbProps = getMetaDataProvider(connProps).getMdbProps();
 
 		if(attributeTrees != null){
 			mdbProps.setCachedAttributeDims(attributeTrees.keySet().toArray(new String[0]));
 		}
 
 		return mdbProps;
 	}    
 
 	public void cacheUow(String clientId) {
 
 		uowCache.get(clientId);
 	}
 		
 	/**
 	 *	Returns a List with an expanded expressionList.
 	 *  The Filtering is done within the current UOW
 	 *
 	 * @param PafDimSpec 
 	 *
 	 * @return A List<String> An expanded list of members
 	 */
 	public List<String> expandExpressionList(String dim, List<String> expressionList, PafClientState clientState) throws PafException{
 		
 		//Expand expression if needed
 		List<String> expandedExpressionList = new ArrayList<String>();
 		for (String term : expressionList) {
 			expandedExpressionList.addAll(Arrays.asList(expandExpression(term, true, dim, clientState)));
 		}
 
 		return expandedExpressionList;
 	}
 	
     /**
      *	Get Filtered meta-data from Essbase
      *
      * @param clientId Client state id
      * @param appDef Paf Application Definition
 	 * @param expandedUow Fully expanded unit of work
 	 * @throws PafException
      */
 	public PafDimSpec[] getFilteredMetadata(PafClientState clientState, PafApplicationDef appDef, PafDimSpec[] uow) throws PafException {
 		
 		//Convert PafDimSpec[] to Map
 		Map<Integer, List<String>> expandedUOW = new HashMap<Integer, List<String>>();
 		Integer uowDimCount = 0;
 		for (PafDimSpec dimSpec : uow ) {
 			List<String> members = new ArrayList<String>();
 			for ( String member : dimSpec.getExpressionList()){
 				members.add(member);
 			}
 			expandedUOW.put(uowDimCount++, members);
 		}
 		
 		//Save the connection props for this application id into the clientstate
 		String dsId = appDef.getMdbDef().getDataSourceId();
 		IPafConnectionProps connProps = (IPafConnectionProps) PafMetaData.getAppContext().getBean(dsId);
 		clientState.getDataSources().put(dsId, connProps);
 
 //		TTN-1406		
 //		IMdbClassLoader mdbClassLoader = this.getMdbClassLoader(connProps);
 //		IMdbData mdbData = mdbClassLoader.getMdbDataProvider();
 		IMdbData mdbData = getMdbDataProvider(connProps);
 		
 		//Filter the data using an Essbase MDX query with the Non Empty flag
 		PafDimSpec[] filteredMetadata = mdbData.getFilteredMetadata(expandedUOW, appDef);
 		
 		return filteredMetadata;
 	}
 
 
 
 
 
 }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
