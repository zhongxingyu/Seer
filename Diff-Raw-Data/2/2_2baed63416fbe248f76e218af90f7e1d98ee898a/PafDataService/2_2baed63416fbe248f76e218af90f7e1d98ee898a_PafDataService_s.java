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
 
 import org.apache.commons.math3.stat.clustering.EuclideanIntegerPoint;
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.springframework.util.StopWatch;
 
 import cern.colt.list.IntArrayList;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrHandler;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.AllocType;
 import com.pace.base.app.AppSettings;
 import com.pace.base.app.DimType;
 import com.pace.base.app.MdbDef;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.PafDimSpec;
 import com.pace.base.app.UnitOfWork;
 import com.pace.base.app.VersionDef;
 import com.pace.base.comm.EvaluateViewRequest;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.comm.SimpleCoordList;
 import com.pace.base.data.Coordinates;
 import com.pace.base.data.ExpOpCode;
 import com.pace.base.data.ExpOperation;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.IntersectionUtil;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.PafDataSlice;
 import com.pace.base.data.PafMemberList;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.data.UserMemberLists;
 import com.pace.base.db.Application;
 import com.pace.base.db.SecurityGroup;
 import com.pace.base.mdb.AttributeUtil;
 import com.pace.base.mdb.DcTrackChangeOpt;
 import com.pace.base.mdb.IMdbClassLoader;
 import com.pace.base.mdb.IMdbData;
 import com.pace.base.mdb.IMdbMetaData;
 import com.pace.base.mdb.IPafConnectionProps;
 import com.pace.base.mdb.PafAttributeMember;
 import com.pace.base.mdb.PafAttributeMemberProps;
 import com.pace.base.mdb.PafAttributeTree;
 import com.pace.base.mdb.PafBaseMember;
 import com.pace.base.mdb.PafBaseMemberProps;
 import com.pace.base.mdb.PafBaseTree;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDataCacheCalc;
 import com.pace.base.mdb.PafDataSliceParms;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimMemberProps;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.mdb.PafMdbProps;
 import com.pace.base.mdb.PafSimpleDimTree;
 import com.pace.base.mdb.TreeTraversalOrder;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.state.EvalState;
 import com.pace.base.state.PafClientState;
 import com.pace.base.state.SliceState;
 import com.pace.base.utility.LevelGenParamUtil;
 import com.pace.base.utility.LogUtil;
 import com.pace.base.utility.StringOdometer;
 import com.pace.base.utility.StringUtils;
 import com.pace.base.view.PafMVS;
 import com.pace.base.view.PafView;
 import com.pace.base.view.PafViewSection;
 import com.pace.base.view.PageTuple;
 import com.pace.base.view.ViewTuple;
 import com.pace.server.assortment.AsstSet;
 import com.pace.server.eval.ES_ProcessReplication;
 import com.pace.server.eval.IEvalStrategy;
 import com.pace.server.eval.MathOp;
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
 	private static Logger uowPerfLogger = Logger.getLogger(PafBaseConstants.PERFORMANCE_LOGGER_UOW_LOAD);
 	private static Logger auditLogger = Logger.getLogger(PafBaseConstants.LOGGER_AUDIT_EVAL);
 
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
 		long dcLoadStart = System.currentTimeMillis();
 		
 		logger.info("Loading uow cache for client: " + clientId);
 		logger.info("Unit of Work: " + uow.toString() );
 
 		// at this point save the connection props for this application id into the client state
 		String dsId = appDef.getMdbDef().getDataSourceId();
 		IPafConnectionProps connProps = (IPafConnectionProps) PafMetaData.getMdbProp(dsId);
 		clientState.getDataSources().put(dsId, connProps);
 
 //		TTN-1406
 //		IMdbClassLoader mdbClassLoader = this.getMdbClassLoader(connProps);
 //		IMdbData mdbData = mdbClassLoader.getMdbDataProvider();
 		IMdbData mdbData = getMdbDataProvider(connProps);
 		
 		// Determine which data intersections to extract for each version
 		Map<String, Map<Integer, List<String>>> mdbDataSpecByVersion = buildUowLoadDataSpec(uow, clientState);
 		
 		// Load data cache
 		logger.info("Building the unit of work..."); //$NON-NLS-1$
 		PafDataCache dataCache = new PafDataCache(clientState);
 		Map<String, Map<Integer, List<String>>>loadedMdbDataSpec = mdbData.updateDataCache(dataCache, mdbDataSpecByVersion);
 		logger.info("UOW intialized with version(s): " + StringUtils.setToString(loadedMdbDataSpec.keySet()));
 		uowCache.put(clientId, dataCache);           
 
 		// Calculate synthetic member intersections
 		PafDataCacheCalc.calculateSyntheticMembers(clientState, dataCache, loadedMdbDataSpec);
 		
 		String stats = dataCache.getStatsString();
 		logger.info(stats);
 		uowPerfLogger.info(stats);
 
 		logger.info("Data cache loaded, cached object count: " + uowCache.size());
 		uowPerfLogger.info(LogUtil.timedStep("UOW build", dcLoadStart));
 
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
 		String versionDim = clientState.getMdbDef().getVersionDim(), yearDim = clientState.getMdbDef().getYearDim();
 		int versionAxis = expandedUowSpec.getDimIndex(versionDim);
 		List<String> uowVersions = Arrays.asList(expandedUowSpec.getDimMembers(versionDim));
 		List<String> uowYears = Arrays.asList(expandedUowSpec.getDimMembers(yearDim));
 		List<String> extractedVersions = new ArrayList<String>();
 		Set<String> nonPlanYears = clientState.getLockedYears();
 		UnitOfWork filteredUowSpec = null;
 
 
 		// Only plannable years will be loaded upon the initial load (TTN-1860).
 		List<String> planYears = new ArrayList<String>(uowYears);
 		planYears.removeAll(nonPlanYears);
 		if (planYears.isEmpty()) {
 			// No plannable years - just return empty data spec
 			return mdbDataSpecByVersion;
 		} else {
 			// Else - update the uow spec to filter on just the plan years
 			filteredUowSpec = expandedUowSpec.clone();
 			filteredUowSpec.setDimMembers(yearDim, planYears.toArray(new String[0]));
 		}
 		
 		
 		
 		// Next, check each version to see if they should be extracted from the mdb. Optimally, only the active planning
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
     	// intersections for that version
 		for (String version : extractedVersions) {
 			Map<Integer, List<String>> mdbDataSpec = filteredUowSpec.buildUowMap();
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
 	 * @return Map describing which intersections were actually retrieved.
 	 * @throws PafException 
 	 */
 	public Map<String, Map<Integer, List<String>>> refreshUowCache(PafClientState clientState,PafApplicationDef appDef, UnitOfWork expandedUow, 
 			List <String> versionFilter) throws PafException {
 	
 		List<String> validatedVersionFilter = new ArrayList<String>();
 		Map<String, Map<Integer, List<String>>> loadedMdbDataSpec = null;
 	
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
 		loadedMdbDataSpec = mdbData.refreshDataCache(cache, mdbDataSpec, validatedVersionFilter);
 		logger.info("Data cache updated for versions: " + StringUtils.setToString(loadedMdbDataSpec.keySet()));
 		logger.info("Cached object count: " + uowCache.size());
 
 		// Calculate synthetic member intersections
 		PafDataCacheCalc.calculateSyntheticMembers(clientState, cache, loadedMdbDataSpec);
 	
 		// Log data cache statistics
 		String stats = cache.getStatsString();
 		logger.info(stats);
 		uowPerfLogger.info(stats);
 
 		return loadedMdbDataSpec;
 	}
 
 	/**
 	 *	Update the uow cache from the multi-dimensional database for the specified versions, without
 	 *  first refreshing these versions.
 	 *
 	 * @param clientState Client State	
 	 * @param dataCache Data cache
 	 * @param versionFilter List of versions to update
 	 * 
 	 * @throws PafException 
 	 */
 	public void updateUowCache(PafClientState clientState, PafDataCache dataCache, List<String> versionFilter) throws PafException {
 
 		// Get mdb data provider corresponding to application data source id
 		String dsId = clientState.getApp().getMdbDef().getDataSourceId();
 		IPafConnectionProps connProps =	clientState.getDataSources().get(dsId);
 		IMdbData mdbData = getMdbDataProvider(connProps);
 
 		// Refresh filtered versions
 		Map<String, Map<Integer, List<String>>> updatedMdbDataSpec = mdbData.updateDataCache(dataCache, clientState.getUnitOfWork(), versionFilter);
 		logger.info("Data cache updated for versions: " + StringUtils.setToString(updatedMdbDataSpec.keySet()));
 		
 		// Calculate synthetic member intersections
 		PafDataCacheCalc.calculateSyntheticMembers(clientState, dataCache, updatedMdbDataSpec);
 			
 	}
 
 	
 
 	/**
 	 *	Expand out the members in a unit of work using the full mdb trees
 	 *
 	 * @param uow Unit of work object
 	 * @param clientState Client state object
 	 * @param discontigMbrGrps Discrete lists of expanded member groups by discontiguous dimension
 	 * @param bUseClientTrees If set to true, the expansion process will use the client trees instead of the full trees 
 	 * 
 	 * @return Hash Map containing the expanded members for each dimension
 	 * @throws PafException 
 	 */
 	private List<String> expandUowDim(String dim, String[] terms, PafClientState clientState, 
 			List<List<String>> discontigMbrGrps) throws PafException {
 		
 		return expandUowDim(dim, terms, clientState, discontigMbrGrps, false);
 	}
 
 
 	/**
 	 *	Expand out the members in a unit of work using the full mdb trees
 	 *
 	 * @param uow Unit of work object
 	 * @param clientState Client state object
 	 * @param discontigMbrGrps Discrete lists of expanded member groups by discontiguous dimension
 	 * @param bUseClientTrees If set to true, the expansion process will alternately use the current client trees
 	 * 
 	 * @return Hash Map containing the expanded members for each dimension
 	 * @throws PafException 
 	 */
 	private List<String> expandUowDim(String dim, String[] terms, PafClientState clientState, 
 			List<List<String>> discontigMbrGrps, boolean bUseClientTrees) throws PafException {
 
 		String measureDim = clientState.getApp().getMdbDef().getMeasureDim();
 		String versionDim = clientState.getApp().getMdbDef().getVersionDim();
 		String yearDim = clientState.getApp().getMdbDef().getYearDim();
 
 
 		// Check for discontiguous hierarchies. A hierarchy is considered discontiguous
 		// if it consists of more than one member specification. Measures and Version
 		// dimensions are ignored, since these hierarchies do not aggregate and have 
 		// special handling elsewhere (TTN-1644).
 		boolean isDiscontig = false;
 		if (terms.length > 1) {
 			if (!dim.equals(measureDim) && !dim.equals(versionDim)) {
 				isDiscontig = true;
 			}			
 		}
 
 		// Expand each member specification - always use full mdb trees, unless the option to 
 		// use the client trees has been set.
 		List<String> dimMemberList = new ArrayList<String>();
 		PafClientState expansionCS = bUseClientTrees ? clientState : null;
 		for (String term : terms) {
 
 			List<String> expandedMbrs = new ArrayList<String>(Arrays.asList(expandExpression(term, true, dim, expansionCS)));
 			dimMemberList.addAll(expandedMbrs);
 
 			// Additional processing for discontiguous dimension hierarchy (TTN-1644)
 			if (isDiscontig) {
 				// Add in synthetic root to beginning of member list, if this is the first
 				// term. Skip this operation for the year dimension, since the year 
 				// dimension already comes in expanded with the root as the first term.
 				if (discontigMbrGrps.size() == 0 && !dim.equals(yearDim)) {
 					dimMemberList.add(0, dim);
 					discontigMbrGrps.add(0,new ArrayList<String>(Arrays.asList(new String[]{dim})));
 				}
 				discontigMbrGrps.add(expandedMbrs);
 			}
 		}
 
 		// Special logic for version dimension - filter out version dimension root
 		if (dim.equalsIgnoreCase(versionDim)) {
 			dimMemberList.remove(versionDim);
 		}
 
 		
 		// Check for duplicate members
 		Set<String> dupMembers = new HashSet<String>();
 		Set<String> uniqueMembers = new HashSet<String>();
 		for (String member : dimMemberList) {
 			if (!uniqueMembers.contains(member)) {
 				uniqueMembers.add(member);
 			} else {
 				dupMembers.add(member);
 			}
 		}
 		
 		int dupMbrCount = dimMemberList.size() - uniqueMembers.size();
 		if (dupMbrCount != 0) {
 			String errMsg = String.format("%d duplicate member(s) found in UOW definition for dimension: [%s]. These members are: %s. User security or underlying dimensional hierarchies need to be adjusted.", //$NON-NLS-1$
 				dupMbrCount, dim, StringUtils.setToString(dupMembers));
 			logger.error(errMsg);
 		}
 
 		return dimMemberList;
 	}
 
 
 	/**
 	 *	Expand out the members in a unit of work
 	 *
 	 * @param uow Unit of work object
 	 * @param clientState Client state object
 	 * @param discontigMbrGrpsByDim Discrete lists of expanded member groups by discontiguous dimension
 	 * 
 	 * @return Hash Map containing the expanded members for each dimension
 	 * @throws PafException 
 	 */
 	private Map<Integer, List<String>> expandUowMembers(UnitOfWork uow, PafClientState clientState, 
 			Map<String, List<List<String>>> discontigMbrGrpsByDim) throws PafException {
 
 		int axis = 0;
 		String[] terms = null;
 		Map<Integer, List<String>> expandedUow = new HashMap<Integer, List<String>>();
 
 
 		// Get the list of expanded members for each dimension
 		for (String dim : uow.getDimensions() ) {
 
 			// Get the list of uow member specifications for current dimension
 			terms = uow.getDimMembers(dim);
 			
 			// Expand the members for each dimension
 			List<List<String>> discontigMbrGrps = new ArrayList<List<String>>();
 			List<String> dimMemberList = expandUowDim(dim, terms, clientState, discontigMbrGrps);
 
 			// Add expanded member list to uow definition
 			expandedUow.put(axis++, dimMemberList);
 
 			// Add discontiguous member groups to master collection
 			if (!discontigMbrGrps.isEmpty()) {
 				discontigMbrGrpsByDim.put(dim, discontigMbrGrps);
 			}
 			
 		}
 		
 		return expandedUow;
 	}
 	
 	/**
 	 *	Expand out the members in a unit of work using the base trees
 	 *
 	 * @param uow Unit of work object
 	 * @param clientState Client state object
 	 * 
 	 * @return UnitOfWork containing the expanded members for each dimension
 	 * @throws PafException 
 	 */
 	public UnitOfWork expandUOW(UnitOfWork uow, PafClientState clientState) throws PafException{
 
 		UnitOfWork expandedUow = null;
 		Map<Integer, List<String>> expandedUowMap = null;
 		Map<String, List<List<String>>> discontigMemberGroupsByDim = new HashMap<String, List<List<String>>>();
 
 		
 		// Expand uow member specifications
 		expandedUowMap = expandUowMembers(uow, clientState, discontigMemberGroupsByDim);
 		
 		// Create new expanded uow
 		String[] dimensions = new String[expandedUowMap.size()];
 		String[][] dimensionMembers = new String[expandedUowMap.size()][];
 		for(Integer axisIndex : expandedUowMap.keySet()){
 			dimensions[axisIndex] = uow.getAxisIndices().get(axisIndex);
 			
 			dimensionMembers[axisIndex] = expandedUowMap.get(axisIndex).toArray(new String[0]);
 		}
 		expandedUow = new UnitOfWork(dimensions, dimensionMembers);
 		expandedUow.setDiscontigMemberGroups(discontigMemberGroupsByDim);
 		
 		return expandedUow;
 	}
 
 
 	/**
 	 * Split a list of dimension members into separate member lists, one for each tree branch. The first member
 	 * in the sortedMemberList must be the root member.
 	 * 	
 	 * @param sortedMemberList List of dimension members whose are grouped together by branch, each branch's members organized in a post-order sort
 	 * @param uowTree UOW dimension tree that will be used to organize the members by branch
 	 * 
 	 * @return Lists of member branches
 	 */
 	public List<List<String>> getBranchLists(List<String> sortedMemberList, PafDimTree uowTree) {
 		
 		
 		List<List<String>> branchLists = new ArrayList<List<String>>();
 
 		if (sortedMemberList.size() > 0) {//Check if all dimension members were suppressed due to data filtering.
 			// Put the root into it's own branch
 			String rootMember = sortedMemberList.get(0);
 			branchLists.add(new ArrayList<String>(Arrays.asList(new String[]{rootMember})));
 		
 			if (sortedMemberList.size() > 1) {
 				List<String> filteredMembers = new ArrayList<String>(sortedMemberList.subList(1,sortedMemberList.size()));
 				List<String> branchList = new ArrayList<String>(),  fullTreeBranch = new ArrayList<String>();
 				for (String member : filteredMembers) {
 	
 					// Add member to branch member list
 					if (fullTreeBranch.contains(member)) {
 						branchList.add(member);
 						continue;
 					}
 	
 					// New branch - add branch member list to 
 					// collection and create new branch member list.
 					if (!branchList.isEmpty()) {
 						branchLists.add(branchList);
 						branchList = new ArrayList<String>();
 					}
 	
 					// Start new branch
 					if (branchList.isEmpty()) {
 						branchList.add(member);
 						fullTreeBranch = PafDimTree.getMemberNames(uowTree.getDescendants(member));
 					}
 	
 				}
 				
 				// Add any remaining members to branch Lists
 				branchLists.add(branchList);
 				
 			}
 		}
 		
 		return branchLists;
 	}
 
 
 	
 	/**
 	 * @param clientState
 	 * @param dim
 	 * @param uow
 	 * @return
 	 * @deprecated
 	 */
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
 		PafBaseMember treeRoot = dimTree.getRootNode();
 		
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
 			
 			// Determine which dimension member is the intended root of the UOW branch. It's
 			// also possible that this is a set of peer members with no root.
 			String branchRootName = null;
 			if (dimType != DimType.Measure) {
 
 				// If the member list contains the new branch root, our candidate root will be
 				// the first name in the member list
 				String candidateRoot = dimMembers[0];
 				if (treeRoot.getKey().equals(candidateRoot)) {
 					// If the candidate root is the top of the dimension tree, then we're found
 					// our branch root.
 					branchRootName = candidateRoot;
 				} else {
 					// Else, the candidate root is the branch root if it is the ancestor of all
 					// the other members in the branch
 					List<String> nonDescendants = new ArrayList<String>(Arrays.asList(dimMembers));
 					List<String> descendants = PafDimTree.getMemberNames(dimTree.getIDescendants(candidateRoot));
 					nonDescendants.removeAll(descendants);
 					if (nonDescendants.size() == 0) {
 						branchRootName = candidateRoot;
 					}
 				}
 		
 			} else {
 				// Measure dimension - use the measure root
 				branchRootName = mdbDef.getMeasureRoot();
 			}
 
 			// Prune tree (if branch root was found and it's not the root of the tree)
 			if (branchRootName != null && !branchRootName.equals(treeRoot.getKey())) {
 				dimTree = dimTree.getSubTreeCopy(branchRootName);
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
 			PafBaseMember versionRoot = treeRoot;
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
 		String[] uowMbrNames = null;
 		UnitOfWork expandedUow = null;
 		
 		
 		//Get the dimension members.  Use the optional workUnit parameter if it is not null
 		if(optionalWorkUnit != null){
 			expandedUow = optionalWorkUnit;
 		}
 		else{
 			expandedUow = clientState.getUnitOfWork();
 		}
 		uowMbrNames = expandedUow.getDimMembers(dim);
 
 		// Version dimension special logic - Prune out any shared members and versions
 		// not contained in the version filter. 
 		baseTree = baseTrees.get(dim);
 		if (dim.equalsIgnoreCase(versionDim)) {
 			
 			//get version filters from client state
 			String[] versionFilters = clientState.getPlannerConfig().getVersionFilter();
 
 			//get tree
 			TreeMap<Integer, List<PafBaseMember>> treeMap = getMembersByGen(dim, uowMbrNames, mdbDef);
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
 	
 		// All other dimensions - Start out by making a tree copy
 		SortedMap<Integer, List<PafBaseMember>> treeMap = getMembersByGen(dim, uowMbrNames, mdbDef);
 		PafBaseMember root = treeMap.get(treeMap.firstKey()).get(0);
 		if (expandedUow.isDiscontigDim(dim)) {
 			String rootAlias = null;
 			if (!dim.equals(yearDim)) {
 				rootAlias = PafBaseConstants.SYNTHETIC_ROOT_ALIAS_PREFIX + root.getKey() + PafBaseConstants.SYNTHETIC_ROOT_ALIAS_SUFFIX;
 			} else {
 				rootAlias = PafBaseConstants.SYNTHETIC_YEAR_ROOT_ALIAS;			
 			}
 			copy = baseTree.getDiscSubTreeCopy(expandedUow.getDiscontigMemberGroups(dim), root.getKey(), rootAlias);
 		} else if (baseTree.hasSharedMembers()) {
 			// Shared members exist, get whole branch since generations on 
 			// shared members may be higher than original member
 			copy = baseTree.getSubTreeCopy(root.getKey());	
 		} else {
 			// No shared members, safely pull down to highest generation 
 			// in the UOW
 			copy = baseTree.getSubTreeCopyByGen(root.getKey(), treeMap.lastKey());			
 		}
 
 		
 		// Housekeeping for non-plannable year support (TTN-1860)
 		Set<String> plannableYears = new HashSet<String>();
 		boolean areNoPlannableYears = false;
 		if (dim.equals(yearDim)) {
 			String[] plannableYearArray = clientState.getPlanSeason().getPlannableYears();
 			if (plannableYearArray != null) {
 				plannableYears.addAll(Arrays.asList(clientState.getPlanSeason().getPlannableYears()));
 			}
 			areNoPlannableYears = (plannableYears.size() == 0 ? true : false);
 		}
 
 		
 		// build list of members in the cache, use hash set for quick find
 		List<String>cacheMbrs = new ArrayList<String>(); 
 		cacheMbrs.addAll(Arrays.asList(uowMbrNames));
 
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
 						errMsg += Messages.getString("PafDataService.19"); //$NON-NLS-1$
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
 				
 				//TTN-1413: Read Only Measures
 				//if measures dimension
 				if ( dim.equalsIgnoreCase(measureDim) ) {					
 					if ( clientState.getReadOnlyMeasures().contains(memberName) && member.getMemberProps() != null ) {						
 						member.getMemberProps().setReadOnly(true);					
 					}
 					
 				}
 				
 				// TTN-1860: Non-Plannable Year Support
 				if (dim.equals(yearDim)) {
 					// Mark all non-plannable years as read-only. If there are no plannable years then
 					// mark the synthetic root as read-only as well.
 					if (!plannableYears.contains(member.getKey())) {
 						PafDimMemberProps memberProps = member.getMemberProps();
 						if (!memberProps.isSynthetic() || areNoPlannableYears) {
 							member.getMemberProps().setReadOnly(true);
 						}
 					}
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
 		treeSet.addTree(PafBaseConstants.TIME_HORIZON_DIM, buildTimeHorizonTree(timeTree, yearTree, clientState));
 		
 		return treeSet;
 	}
 
 	/**
 	 * Build the virtual time horizon tree
 	 * 
 	 * @param timeTree UOW time tree
 	 * @param yearTree UOW year tree
 	 * @param clientState Client State
 	 * 
 	 * @return Virtual time horizon tree
 	 * @throws PafException 
 	 */
 	protected PafDimTree buildTimeHorizonTree(PafDimTree timeTree, PafDimTree yearTree, PafClientState clientState) throws PafException {
 		
 		PafDimMember timeRoot = timeTree.getRootNode(), yearRoot = yearTree.getRootNode();
 		List<PafDimMember> yearMembers = yearTree.getLowestLevelMembers();
 		List<PafDimMember> timeChildMbrs = null;
 		AppSettings appSettings = clientState.getApp().getAppSettings();
 
 
 		// The virtual time tree is formed by combining the UOW Time and Year trees. It is used 
 		// in aggregation and any time-based rule function call, instead of the separate Time 
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
 		rootProps.setSynthetic(timeRoot.getMemberProps().isSynthetic() || yearRoot.getMemberProps().isSynthetic());
 		
 		// The is some differing logic depending on whether the is just a single year in the UOW
 		// or multiple years, represented by a year tree with a virtual node whose children are 
 		// the individual years.
 		if (yearMembers.size() == 1) {
 			// There is a one-to-one mapping between the members in the time tree and the
 			// members in the time horizon tree.
 			rootProps.setLevelNumber(timeTree.getHighestAbsLevelInTree()); 
 			timeChildMbrs = timeRoot.getChildren();
 		} else {
 			// Otherwise, the time horizon tree contains a node for each combination of year and time 
 			// period, plus the root node which is comprised of the root of the year tree 
 			// and the root of the time tree. 
 			rootProps.setLevelNumber(timeTree.getHighestAbsLevelInTree() + 1); 
 			rootProps.setSynthetic(true);
 			timeChildMbrs = new ArrayList<PafDimMember>(Arrays.asList(new PafDimMember[]{timeRoot}));
 		}
 		PafDimTree timeHorizonTree = new PafBaseTree(root, new String[]{PafBaseConstants.ESS_DEF_ALIAS_TABLE});
 		
 		
 		// Get week 53 settings (TTN-1858)
 		Set<String> week53Mbrs = new HashSet<String>(), week53Years = new HashSet<String>();
 		if (appSettings.getWeek53Members() != null) {
 			week53Mbrs.addAll(appSettings.getWeek53Members());
 		}
 		if (appSettings.getWeek53Years() != null) {
 			week53Years.addAll(appSettings.getWeek53Years());
 		}
 		
 		// Ensure that only one "week 53 member" exists in current time tree (TTN-1858)
 		List<String> allPeriods = new ArrayList<String>(Arrays.asList(timeTree.getMemberKeys()));
 		allPeriods.retainAll(week53Mbrs);
 		if (allPeriods.size() > 1) {
 			String errMsg = "Unable to bulid client trees. More than one 'Week 53 member' has been identified in the client time hiearchy."
 					+ " This can indicate an issue with the application Week 53 properties or with the current season definition";
 			logger.fatal(errMsg);
 			throw new PafException(errMsg, PafErrSeverity.Fatal);
 		}
 		
 		
 		// Build out the rest of the time horizon tree
 		for (PafDimMember yearMember : yearMembers) {
 			// Add child members
 			addTimeHorizonChildren(timeHorizonTree, root, yearMember, timeChildMbrs, week53Mbrs, week53Years);
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
 	 * @param week53Mbrs Week 53 members
 	 * @param week53Years Week 53 years
 	 * 
 	 * @throws PafException 
 	 */
 	private void addTimeHorizonChildren(PafDimTree timeHorizonTree, PafDimMember parentNode, PafDimMember yearMember, List<PafDimMember> timeMembers, Set<String> week53Mbrs, Set<String> week53Years) throws PafException {
 		
 		for (PafDimMember timeMember : timeMembers) {
 			
 			// Skip any invalid week 53 members (TTN-1858)
 			String period = timeMember.getKey(), year = yearMember.getKey();
 			if (week53Mbrs.contains(period) && !week53Years.contains(year)) 
 				continue;
 
 			// Create child member
 			PafBaseMemberProps memberProps = new PafBaseMemberProps();
 			String memberName = TimeSlice.buildTimeHorizonCoord(period, year);
 			PafDimMember timeHorizonChild = new PafBaseMember(memberName, memberProps);
 			memberProps.addMemberAlias(PafBaseConstants.ESS_DEF_ALIAS_TABLE, timeHorizonChild.getKey());
 			memberProps.setLevelNumber(timeMember.getMemberProps().getLevelNumber()); 				// Use level number of time member
 			memberProps.setGenerationNumber(parentNode.getMemberProps().getGenerationNumber() + 1);	// Gen number is parent gen + 1
 
 			// Add child member to time horizon tree 
 			timeHorizonTree.addChild(parentNode, timeHorizonChild);
 			
 			// Recursively add a child member to time horizon tree for each child member of the regular time tree
 			addTimeHorizonChildren(timeHorizonTree, timeHorizonChild, yearMember, timeMember.getChildren(), week53Mbrs,week53Years);
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
 
 			// Compute impacted attribute intersections on view
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
 
 		// Display data cache statistics
 		logger.info(dataCache.getCurrentUsageStatsString());
 		
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
 		String planVersion = dataCache.getPlanVersion();
 		int versionAxis = dataCache.getVersionAxis();
 		UnitOfWork refDataSpec = null;
 		Map<String, Map<Integer, List<String>>> dataSpecByVersion = new HashMap<String, Map<Integer, List<String>>>();
 		MemberTreeSet uowTrees = clientState.getUowTrees();
 
 		
 		// As a safeguard, filter out any versions not defined to the data cache. This
 		// prevents the attempted loading of a version the user doesn't have security 
 		// rights to, in the case of any unauthorized versions specified in the view.
 		List<String> viewVersions = new ArrayList<String>(Arrays.asList(viewMemberSpec.getDimMembers(versionDim)));
 		List<String> viewRefVersions = dataCache.getReferenceVersions();
 		viewRefVersions.retainAll(viewVersions);
 		Set<String> requiredRefVersions = null;
 		List<String> viewContribPctVersions = dataCache.getContribPctVersions();
 		viewContribPctVersions.retainAll(viewVersions);
 		List<String> viewVarVersions = dataCache.getVarianceVersions();
 		viewVarVersions.retainAll(viewVersions);
 
 		
 		// Exit if no data to update
 		if (viewRefVersions.isEmpty() && viewContribPctVersions.isEmpty() && viewVarVersions.isEmpty()) {
 			return;
 		}
 		
 		
 		// First process reference and variance versions. Determine which reference 
 		// data intersections are needed to support view and any variance version
 		// calculations. 
 		requiredRefVersions = new HashSet<String>(dataCache.getComponentVersions(viewVarVersions));
 		requiredRefVersions.remove(planVersion);
 		requiredRefVersions.addAll(viewRefVersions);
 
 		
 		// Store the view's member specification in the format of a uow specification
 		// and clone for each reference version.
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
 					refDataSpec.setDimMembers(baseDim, PafDimTree.getMemberNames(baseMembers).toArray(new String[0]));
 				} else {
 					// No associated attribute - just select members on the view
 					refDataSpec.setDimMembers(baseDim, viewMemberSpec.getDimMembers(baseDim));
 				}
 			}
 		}
 
 	
 		// Take data specification, convert to a map, and clone it across each required 
 		// reference version.
 		for (String version : requiredRefVersions) {
 			
 			// Clone data specification for current version
 			Map <Integer, List<String>> dataSpecAsMap = refDataSpec.buildUowMap();
 			dataSpecAsMap.put(versionAxis, new ArrayList<String>(Arrays.asList(new String[]{version})));
 			
 			// Add filtered version-specific data specification to master map
 			dataSpecByVersion.put(version, dataSpecAsMap);
 		}
 		
 		
 		
 		// Contribution % versions - Populate additional UOW intersections for any base
 		// reference versions (on or off the view) that meets one of the following criteria:
 		//
 		// 	1. Base reference version is used in the compare intersection spec of any 
 		//	   contribution % version formula on the view. 
 		// 			OR 
 		//	2. Base reference version is used as the base version in a contribution % 
 		//	   formula. (TTN-1765)
 		//
 		//
 		// Some data specifications, that were previously generated earlier in this method,
 		// may be updated in the following logic. 
 		//
 		List<String> refVersions = dataCache.getReferenceVersions();	// TTN-1765
 		for (String contribPctVersion : viewContribPctVersions) {
 			
 			// This collection will keep track of additional members to be added by dimension
 			Map<String, List<String>> compareVersDepMbrMap = new HashMap<String, List<String>>();
 
 			// Get the formula's base version and optional comparison version;
 			// and determine the additional UOW members that are needed to support
 			// the comparison version (if any) in this contribution % formula.
 			VersionDef versionDef = pafApp.getVersionDef(contribPctVersion);
 			String baseVersion = versionDef.getVersionFormula().getBaseVersion();
 			String compareVersion = null;
 			PafDimSpec[] compareIsSpec = versionDef.getVersionFormula().getCompareIsSpec();
 			for (PafDimSpec crossDimSpec : compareIsSpec) {
 				String dim = crossDimSpec.getDimension();
 				String memberSpec = crossDimSpec.getExpressionList()[0];
 				if (!dim.equals(versionDim)) {
 					// Since this dimension was referenced in the contribution percent
 					// formula, additional members need to be retrieved (TTN-1781).
 					List<String> dependantMbrs = null;
 					PafDimTree dimTree = uowTrees.getTree(dim);
 					if (memberSpec.equalsIgnoreCase(PafBaseConstants.VF_TOKEN_PARENT)) {
 						// Parent token - add all non-leaf dimension members
 						int lowestParentLvl = dimTree.getLowestAbsLevelInTree() + 1;
 						dependantMbrs = dimTree.getMemberNames(TreeTraversalOrder.POST_ORDER, lowestParentLvl);
 					} else if (memberSpec.equalsIgnoreCase(PafBaseConstants.VF_TOKEN_UOWROOT)) {
 						// UOW Root Token - add Root Node
 						dependantMbrs = new ArrayList<String>();
 						dependantMbrs.add(dimTree.getRootNode().getKey());			
 					} else {
 						// Regular Member - just add member
 						dependantMbrs = new ArrayList<String>();
 						dependantMbrs.add(memberSpec);
 					}
 
 					// Keep track of needed additional members by dimension
 					compareVersDepMbrMap.put(dim, dependantMbrs);
 
 				} else {
 					compareVersion = memberSpec;				
 				}
 			}
 
 			// Select any reference version that is specified in either the 
 			// base or comparison versions.
 			Set<String> selectedVersions = new HashSet<String>();
 			if (refVersions.contains(compareVersion)) {
 				selectedVersions.add(compareVersion);
 			}
 			if (refVersions.contains(baseVersion)) {
 				selectedVersions.add(baseVersion);
 			}
 			
 			// For each reference version specified in the current contribution percent 
 			// formula, select all UOW intersections needed to support the view, as well
 			// as the formula (TTN-1781). 
 			// 
 			// Since the same reference version can support multiple contribution percent
 			// formulas, this logic is additive. 
 			//
 			// The selection logic, as applied to reference versions, is:
 			//
 			// 	Formula base version - add all members referenced on the view
 			//	Formula compare version - add specific members required by each dimension 
 			// 							  to support formula calculation
 			// 
 			for (String version : selectedVersions) {
 				
 				// Get previous defined data specification for selected version. If
 				// none exists, then pull the default data specification for the view
 				Map <Integer, List<String>> dataSpecAsMap = null;
 				if (dataSpecByVersion.containsKey(version)) {
 					dataSpecAsMap = dataSpecByVersion.get(version);
 				} else {
 					dataSpecAsMap = refDataSpec.buildUowMap();
 				}
 
 				// Update data specification based on version type: base version / compare version
 				Map <Integer, List<String>> refDataSpecAsMap = refDataSpec.buildUowMap();				
 				for (int axis : dataSpecAsMap.keySet())  {
 					Set<String> dataSpecMbrSet = new HashSet<String>(dataSpecAsMap.get(axis));
 					if (version.equals(baseVersion)) { 
 						// Base version logic 
 						dataSpecMbrSet.addAll(refDataSpecAsMap.get(axis));
 					} else {
 						// Compare version logic
 						String dim = dataCache.getDimension(axis);
 						dataSpecMbrSet.addAll(compareVersDepMbrMap.get(dim));
 					}
 					dataSpecAsMap.put(axis, new ArrayList<String>(dataSpecMbrSet));
 				}
 
 				// Add filtered version-specific data specification to master map
 				dataSpecAsMap.put(versionAxis, new ArrayList<String>(Arrays.asList(new String[]{version})));
 				dataSpecByVersion.put(version, dataSpecAsMap);
 			}			
 				
 		}
 		
 				
 		// Update view reference data using data specification map
 		String dsId = mdbDef.getDataSourceId();
 		IPafConnectionProps connProps = clientState.getDataSources().get(dsId);
 		IMdbData mdbData = this.getMdbDataProvider(connProps);
 		Map<String, Map<Integer, List<String>>> loadedMdbDataSpec = mdbData.updateDataCache(dataCache, dataSpecByVersion);
 
 		// Calculate any synthetic member intersections
 		PafDataCacheCalc.calculateSyntheticMembers(clientState, dataCache, loadedMdbDataSpec);
 		
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
 		
 		logger.debug("Building Data Slice Parms ...");
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
 		logger.debug("Returning Completed Data Slice Parms");    
 		return sliceParms;
 	}
 
 
 	/**
 	 *	Initialize member tree cache
 	 *
 	 */
 	protected void clearDimTreeCache() {
 
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
 	public void initDimTreeStore(PafApplicationDef pafApp) {
 				
 		IMdbMetaData metaData = null;
 		MdbDef mdbDef = pafApp.getMdbDef();
 		IPafConnectionProps connProps = (IPafConnectionProps) PafMetaData.getMdbProp(mdbDef.getDataSourceId());
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
 		List<String> skippedAttrTrees = new ArrayList<String>();
 
 
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
 				// attribute mapped to a base member. Remove any attribute trees that don't.
 				if (attrBaseMemberLevel == null) {
 					String errMsg = "Error loading associated attributes - no base member mappings were found for attribute dimension: ["
 						+ attrDimName;
 					errMsg += "]. This attribute dimension will not be loaded.";
 					logger.error(errMsg);
 					skippedAttrTrees.add(attrDimName);
 					continue;
 //					IllegalArgumentException iae = new IllegalArgumentException(errMsg);
 //					throw (iae);
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
 
 		
 		// Remove any skipped attribute trees
 		for (String attrTree : skippedAttrTrees) {
 			this.attributeTrees.remove(attrTree);
 		}
 		
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
 		
 		IPafConnectionProps connProps = (IPafConnectionProps) PafMetaData.getMdbProp(appDef.getMdbDef().getDataSourceId());
 				
 		try{
 //			TTN-1406
 //			IMdbClassLoader mdbClassLoader = this.getMdbClassLoader(connProps);
 //			metaData = mdbClassLoader.getMetaDataProvider();
 			metaData = this.getMetaDataProvider(connProps);
 			
 			//get the mdb props from Essbase.
 			mdbProps = metaData.getMdbProps();
 			
 			boolean varyingAttributesExist = metaData.varyingAttributesExist(dims, mdbProps, appDef.getEssNetTimeOut());
 			
 			if(varyingAttributesExist == true){
 				logger.error(Messages.getString("PafDataService.75")); //$NON-NLS-1$
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
 	 * Deletes a PafAttributeTree from the Hibernate data store.
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
 
 		@SuppressWarnings("rawtypes")
 		List list;
 
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
 			PafViewService.getInstance().processInvalidTuples(viewSection, clientState);
 			if (viewSection.getRowTuples().length > 0){
 				viewSection.setRowTuples(generateHeaderGroupNo(viewSection.getRowTuples(),viewSection.getRowAxisDims(), viewSection.isRowHeaderRepeated()));
 			} else {
 				viewSection.setEmpty(true);
 			}
 			if (viewSection.getColTuples().length > 0)
 			{
 				viewSection.setColTuples(generateHeaderGroupNo(viewSection.getColTuples(), viewSection.getColAxisDims(), viewSection.isColHeaderRepeated()));
 			} else {
 				viewSection.setEmpty(true);
 			}
 		}
 		logger.debug("Completed exapanding tuples for view: " + view.getName());        
 		return view;
 	}
 
 	/**
 	 *	Generate the header group number used in the presentation layer to properly span member headers across rows or columns
 	 *
 	 * @param viewTuples View tuples
 	 * @param axisDims	Axis dimension names
 	 * @param isHeaderRepeated Indicates is headers should be repeated across rows or columns
 	 * 
 	 * @return ViewTuple[] 
 	 */
 	private ViewTuple[] generateHeaderGroupNo(ViewTuple[] viewTuples, String[] axisDims, boolean isHeaderRepeated) {
 
 		int dimCount = axisDims.length;
 		
 		//TTN-1041 - client starts tuple index at 1, not 0
 		short tupleInx = 1;
 		
 		Integer groupNo[] = new Integer[dimCount];
 		String[] prevHeader = new String[dimCount];
 		ViewTuple[] updatedViewTuples = viewTuples.clone();
 
 		logger.debug(Messages.getString("PafDataService.85")); //$NON-NLS-1$
 
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
 
 		// Loop through each view tuple to be updated
 		for (ViewTuple viewTuple:updatedViewTuples) {
 
 			// If headers are to be repeated, simply increment group# on first (outer) axis (TTN-1865).
 			if (isHeaderRepeated) {
 				groupNo[0]++;
 			} else {
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
 	 *	Resolve and expand the member definitions on a set of view tuples
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
 		MdbDef mdbDef = clientState.getMdbDef();
 		String timeDim = mdbDef.getTimeDim(), yearDim = mdbDef.getYearDim();
 		String axisList = "";
 		Set<String> invalidTimeHorizonPeriods = clientState.getInvalidTimeHorizonPeriods();
 		List <ViewTuple>expandedTuples = new ArrayList<ViewTuple>();
 
 		
 		// Initialization
 		for (String a : axes) {
 			axisList += a + " "; 
 		}
 		logger.debug("Expanding tuples for axis: " + axisList);  
 
 		// Expand inner axis
 		for (ViewTuple vt:origViewTuples) {   
 			if( ! vt.getMemberDefs()[innerAxisIndex].isEmpty() ) {
 				expandedTuples.addAll(expandTuple(vt, innerAxisIndex, axes[innerAxisIndex], clientState));   
 			}
 		}
 
 		// Compile a list of attribute dimensions used in this tuple or the page tuple. This
 		// information will be used to in an initial pass at filtering out invalid member 
 		// intersections. (TTN-1469)
 		Set<String> tupleAttrDims = new HashSet<String>();
 		List<String> axisDimList = Arrays.asList(axes);
 		List<String> pageDimList = Arrays.asList(pageAxisDims);
 		if (axes.length > 0 && attributeDims != null && attributeDims.length > 1) {
 			// Attribute View
 			// Check for attributes on the tuple (page or current axis)
 			for (String attrDim : attributeDims) {
 				if (axisDimList.contains(attrDim) || pageDimList.contains(attrDim)) {
 					tupleAttrDims.add(attrDim);
 				}
 			}
 		}
 		
 		// Run the expanded tuples through some final editing and filtering
 		logger.debug("Editing & Filtering Expanded View Tuples");
 
 		// -- Determine if time horizon validation will be performed. This validation will be performed
 		// if the tuple, by itself, or in combination with the page tuple contains both the time and
 		// year dimensions. No time horizon validation will be performed, however,  if the tuple doesn't 
 		// contain at least one of these dimensions, unless if they are both specified as page dimensions.
 		//
 		// Validating the case where the time and year are on different axis, but are either a row or column
 		// dimension, cannot be done here, but will be done in some later logic (TTN-1858 / TTN-1886)
 		List<ViewTuple> tuplesToRemove = new ArrayList<ViewTuple>();
 		List<String> invalidCoordList = new ArrayList<String>(); 
 		boolean bDoTimeHorizValidation = true, bTimeOnPage = false, bYearOnPage = false;
 		int timeAxis = 0, yearAxis = 0;
 		String period = null, year = null;
 		do {
 			// Search for time dimension
 			if (axisDimList.contains(timeDim)) {
 				timeAxis = axisDimList.indexOf(timeDim);
 			} else if (pageDimList.contains(timeDim)) {
 				timeAxis = pageDimList.indexOf(timeDim);
 				period = pageAxisMembers[timeAxis];
 				bTimeOnPage = true;
 			} else {
 				// Time dimension was not found - skip validation
 				bDoTimeHorizValidation = false;	
 				break;
 			}
 			
 			// Search for year dimension
 			if (axisDimList.contains(yearDim)) {
 				yearAxis = axisDimList.indexOf(yearDim);
 			} else if (pageDimList.contains(yearDim)) {
 				yearAxis = pageDimList.indexOf(yearDim);
 				year = pageAxisMembers[yearAxis];
 				bYearOnPage = true;
 			} else {
 				// Year dimension was not found - skip validation
 				bDoTimeHorizValidation = false;	
 				break;
 			}
 			
 			// Test for invalid time horizon coordinate when time and year
 			// are both page dimensions
 			if (bTimeOnPage && bYearOnPage) {
 				String timeHorizCoord = TimeSlice.buildTimeHorizonCoord(period, year);
 				if (invalidTimeHorizonPeriods.contains(timeHorizCoord)) {
 					String invalidCoords = PafBaseConstants.SYNTHETIC_YEAR_ROOT_ALIAS + " / " + period;
 					String errMsg = invalidCoords + " is an invalid Year/Time combination. Please select a different Year/Time combination.";
 					throw new PafException(errMsg, PafErrSeverity.Error);
 				}				
 			}
 		 } while (false);
 		
 
 		// Expand each symmetric tuple group (for tuples comprised of multiple dimensions)
 		for (int axisIndex = innerAxisIndex - 1; axisIndex >= 0; axisIndex--) {
 			expandedTuples = expandSymetricTupleGroups(axisIndex, expandedTuples.toArray(new ViewTuple[0]), axes, tupleAttrDims, pageAxisDims, pageAxisMembers, clientState);
 		}
 
 		// -- Now edit/validate each expanded view tuple
 		for (ViewTuple viewTuple:expandedTuples) {
 
 			// Initialization
 			String[] memberArray = viewTuple.getMemberDefs();
 
 			// If any tuple member is set to PAFBLANK, set the remaining members to PAFBLANK as well
 			if (isBlankViewTuple(viewTuple)) {
 				for (int i = 0; i < memberArray.length; i++) {
 					memberArray[i] = PafBaseConstants.PAF_BLANK;
 				}
 				viewTuple.setMemberDefs(memberArray);
 				continue;
 			}
 						
 			// Compile a list of any tuples with invalid time / year combinations (TTN-1858 / TTN-1886).
 			if (bDoTimeHorizValidation) {
 				if (!bTimeOnPage)
 					period = memberArray[timeAxis];
 				if (!bYearOnPage)
 					year = memberArray[yearAxis];
 				String timeHorizCoord = TimeSlice.buildTimeHorizonCoord(period, year);
 				if (invalidTimeHorizonPeriods.contains(timeHorizCoord)) {
 					invalidCoordList.add(PafBaseConstants.SYNTHETIC_YEAR_ROOT_ALIAS + " / " + period);
 					tuplesToRemove.add(viewTuple);
 				}
 			}			
 			
 		}
 		
 		// -- Lastly, remove any filtered tuples
 		if( expandedTuples.size() != 0 && tuplesToRemove.size() == expandedTuples.size() ) {
 			String errMsg = "The view can not be displayed since all the selected Year/Time combinations: "
 					+ StringUtils.arrayListToString(invalidCoordList) + " are invalid. Please select different Year/Time combination(s).";
 			throw new PafException(errMsg, PafErrSeverity.Error);
 		}
 		expandedTuples.removeAll(tuplesToRemove);
 		
 
 		logger.debug("Completed expanding tuples.");
 		return expandedTuples.toArray(new ViewTuple[0]);
 	}
 
 	/**
 	 *  Expand symmetric view tuple groups
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
 	public List<ViewTuple> expandSymetricTupleGroups(int axisIndex, ViewTuple[] origViewTuples, String[] axes, Set<String> tupleAttrDims, String[] pageAxisDims, String[] pageAxisMembers, PafClientState clientState) throws PafException {
 
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
 			String[] expandedGroupTerms = expandExpression(groupTerm, groupParentFirst, dimToExpand , clientState, true);
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
 
 	public boolean isBlankViewTuple(ViewTuple viewTuple) {
 
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
 	public List<ViewTuple> expandTuple(ViewTuple viewTuple, int axisIndex, String dim, PafClientState clientState) throws PafException {
 		ArrayList<ViewTuple> expTuples = new ArrayList<ViewTuple>();
 		String term = viewTuple.getMemberDefs()[axisIndex];
 		if (term.contains("@")) {
 			ExpOperation expOp = new ExpOperation(term);
 			String [] expTerms = resolveExpOperation(expOp, viewTuple.getParentFirst(), dim, clientState, true);
 			for (String expTerm : expTerms) {
 				if( expTerm != null && ! expTerm.isEmpty() ) {
 					ViewTuple vt = viewTuple.clone();
 					vt.getMemberDefs()[axisIndex] = expTerm;
 					expTuples.add(vt);
 				}
 			}
 		}
 		else {
 			//added code to handle terms that contain multiple members
 			if( term.contains(",") ) {
 				List<String> expTermList = StringUtils.stringToList(term, ",");
 				for (String expTerm : expTermList) {
 					if( expTerm != null && ! expTerm.isEmpty() ) {
 						ViewTuple vt = viewTuple.clone();
 						vt.getMemberDefs()[axisIndex] = expTerm;
 						expTuples.add(vt);
 					}
 				}
 			}
 			else {
 				expTuples.add(viewTuple);
 			}
 		}
 		return expTuples;        
 	}
 
 
 
 	/**
 	 * Expand member expansion expression along it dimension hierarchy
 	 * 
 	 * @param term Expression to expand
 	 * @param parentFirst Indicates that parents should appear before children in return set
 	 * @param dim Member dimension
 	 * @param clientState Client state
 	 * 
 	 * @return String[]
 	 * @throws PafException
 	 */
 	private String[] expandExpression(String term, boolean parentFirst, String dim, PafClientState clientState) throws PafException {
 		return expandExpression(term, parentFirst, dim, clientState, false);
 	}
 
 	/**
 	 * Expand member expansion expression along it dimension hierarchy
 	 * 
 	 * @param term Expression to expand
 	 * @param parentFirst Indicates that parents should appear before children in return set
 	 * @param dim Member dimension
 	 * @param clientState Client state
 	 * @param bIgnoreErrors If set to true, then any resolution/expansion errors will be ignored
 	 * 
 	 * @return String[]
 	 * @throws PafException
 	 */
 	private String[] expandExpression(String term, boolean parentFirst, String dim, PafClientState clientState, boolean bIgnoreErrors) throws PafException {
 		String [] expTerms;
 		if (term.contains("@")) {
 			ExpOperation expOp = new ExpOperation(term);
 			expTerms = resolveExpOperation(expOp, parentFirst, dim, clientState, bIgnoreErrors);
 		}
 		else {
 			expTerms = new String[] {term};
 		}
 		return expTerms;
 	}
 
 
 
 	/**
 	 *	Expands a multidimensional operator for the specified dimension and returns corresponding members
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
 		return resolveExpOperation(expOp, parentFirst, dim, clientState, false);
 	}
 
 	/**
 	 *	Expands a multidimensional operator for the specified dimension and returns corresponding members
 	 *
 	 * @param expOp
 	 * @param parentFirst Indicates whether the parent member should appear before or after it's children
 	 * @param dim
 	 * @param clientState If this is null than the full tree for the specified dimension is used. Otherwise
 	 * the subtree appropriate for that client will be used.
 	 * @param bIgnoreErrors If set to true, then any resolution/expansion errors will be ignored
 	 * 
 	 * @return String[]
 	 * @throws PafException 
 	 */
 	private String[] resolveExpOperation(ExpOperation expOp, Boolean parentFirst, String dim, PafClientState clientState, boolean bIgnoreErrors ) throws PafException {
 
 		PafDimTree tree; 
 		if (clientState == null || clientState.getUowTrees() == null)
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
 
 		String parmKey = "", parmVal = "";
 		Properties tokenCatalog = null;
 		if( clientState != null ) {
 			tokenCatalog = clientState.getTokenCatalog();
 		}
 		
 		List<PafDimMember> memberList = null;
 		PafDimMember newPafDimMember = null;
 		
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
 			if (expOp.getParms().length == 1) {
				memberList = tree.getLowestLevelMembers();
 			} else {
 
 				memberList = tree.getMembersAtLevel(expOp.getParms()[0], Short.parseShort(expOp.getParms()[1]));
 			}
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
 					
 					if( ! m.isEmpty() ) {
 						//get dim member from tree
 						newPafDimMember = tree.getMember(m);
 						
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
 			}
 			break;
 			
 		case OFFSET_MEMBERS:
 			memberList = new ArrayList<PafDimMember>();
 			if (expOp.getParms().length == 0) {
 				
 			}
 			String baseMember = expOp.getParms()[0];
 			int offsetStart = Short.parseShort(expOp.getParms()[1]);
 			int offsetEnd = Short.parseShort(expOp.getParms()[2]);
 			try {
 				List<PafDimMember> peers = tree.getPeersByOffsets(dim, baseMember, offsetStart, offsetEnd);
 				memberList.addAll(peers);
 			} catch( RuntimeException re ) {
 				throw re;
 			}
 			break;
 			
 		case PLAN_YEARS:
 			parmKey = PafBaseConstants.VIEW_TOKEN_PLAN_YEARS;
 			parmVal = tokenCatalog.getProperty(parmKey);
 			if ( parmVal == null ) {
 				String errMsgDtl = "Unable to resolve the [" + parmKey + "] property";
 				logger.error(errMsgDtl);
 				throw new IllegalArgumentException(errMsgDtl);
 			}
 			List<String> planYearList = StringUtils.stringToList(parmVal, ",");
 			if( planYearList != null && planYearList.size() > 0 ) {
 				memberList = new ArrayList<PafDimMember>();
 				for( String nonPlanYear : planYearList ) {
 					newPafDimMember = tree.getMember(nonPlanYear);
 					memberList.add(newPafDimMember);
 				}
 			}
 			break;
 
 		case NONPLAN_YEARS:
 			parmKey = PafBaseConstants.VIEW_TOKEN_NONPLAN_YEARS;
 			parmVal = tokenCatalog.getProperty(parmKey);
 			if ( parmVal == null ) {
 				String errMsgDtl = "Unable to resolve the [" + parmKey + "] property";
 				logger.error(errMsgDtl);
 				throw new IllegalArgumentException(errMsgDtl);
 			}
 			List<String> nonPlanYearList = StringUtils.stringToList(parmVal, ",");
 			if( nonPlanYearList != null && nonPlanYearList.size() > 0 ) {
 				memberList = new ArrayList<PafDimMember>();
 				for( String nonPlanYear : nonPlanYearList ) {
 					newPafDimMember = tree.getMember(nonPlanYear);
 					memberList.add(newPafDimMember);
 				}
 			}
 			break;
 
 		case FIRST_PLAN_YEAR:
 			parmKey = PafBaseConstants.VIEW_TOKEN_FIRST_PLAN_YEAR;
 			parmVal = tokenCatalog.getProperty(parmKey);
 			if ( parmVal == null ) {
 				String errMsgDtl = "Unable to resolve the [" + parmKey + "] property";
 				logger.error(errMsgDtl);
 				throw new IllegalArgumentException(errMsgDtl);
 			}
 			memberList = new ArrayList<PafDimMember>();
 			newPafDimMember = tree.getMember(parmVal);
 			memberList.add(newPafDimMember);
 			break;
 
 		case FIRST_NONPLAN_YEAR:
 			parmKey = PafBaseConstants.VIEW_TOKEN_FIRST_NONPLAN_YEAR;
 			parmVal = tokenCatalog.getProperty(parmKey);
 			if ( parmVal == null ) {
 				String errMsgDtl = "Unable to resolve the [" + parmKey + "] property";
 				logger.error(errMsgDtl);
 				throw new IllegalArgumentException(errMsgDtl);
 			}
 			memberList = new ArrayList<PafDimMember>();
 			newPafDimMember = tree.getMember(parmVal);
 			memberList.add(newPafDimMember);
 			break;
 
 		}
 
 		// check if member list is empty. depending on method parm, either throw
 		// a fatal error or issue warning and return original member (TTN-1886).
 		String firstTerm = expOp.getParms()[0];
 		if (memberList.size() == 0) {
 			if (bIgnoreErrors) {
 				String errMsg = "Unable to expand the member [" + firstTerm + "] in the dimension [" + dim
 						+ "] using the operation [" + expOp.toString() + "]";
 				logger.warn(errMsg);
 			} else {
 				throw new PafException("No members found for dimension [" + dim + "]. Please check view and security settings.", PafErrSeverity.Error);
 			}
 		}
 
 
 		// return member names, if none the return original member (TTN-1886)
 		String[] memberNames = new String[memberList.size()];
 		if( ! firstTerm.isEmpty() && expOp.getOpCode() != ExpOpCode.OFFSET_MEMBERS ) {
 			int i=0;
 			for (PafDimMember m : memberList)
 				memberNames[i++] = m.getKey();
 			if (memberNames.length == 0) {
 				memberNames = new String[]{firstTerm};
 			}
 		}
 		// return member names
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
 			// Don't overwrite any existing member entries. Shared hierarchies
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
 		logger.info(Messages.getString("PafDataService.98"));  //$NON-NLS-1$
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
 			baseDimNames.add(baseDimName);			
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
 		RuleSet measureRuleSet = clientState.getDefaultMsrRuleset();				// TTN-1792
 		evalState.setMeasureRuleSet(measureRuleSet); 								// TTN-1792
 		AppSettings appSettings = clientState.getApp().getAppSettings();			// TTN-1792
 		measureRuleSet = resolveRuleSetSettings(appSettings, measureRuleSet);		// TTN-1792
 		
 		// Perform default evaluation
 		evalState.setDefaultEvalStep(true);
 		evalStrategy.executeDefaultStrategy(evalState);   
 		evalState.setDefaultEvalStep(false);
 
 		// Push updated data into multi-dimensional database for consistency 
 		// in case of aggregate changes, only if corresponding planner config 
 		// option is set.
 		if (plannerConfig.isMdbSaveWorkingVersionOnUowLoad()) {
 			String dsId = clientState.getApp().getMdbDef().getDataSourceId();
 			logger.info(String.format("Saving datacache to data provider: [%s]", dsId));           
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
 		String s = String.format("Evaluating view [%s] for client [%s] using measure set [%s]", currentView.getName(), clientState.getUserName(), evalRequest.getRuleSetName() );
 		auditLogger.info(s);
 		logger.info(s);
 		PafDataSlice newSlice = evalRequest.getDataSlice();
 		PafMVS pafMVS = dataCache.getPafMVS();
 		PafApplicationDef appDef = clientState.getApp();
 		AppSettings appSettings = appDef.getAppSettings();
 		MdbDef mdbDef = appDef.getMdbDef();
 		PafViewSection currentViewSection = pafMVS.getViewSection();
 		String measureDim = mdbDef.getMeasureDim(), versionDim = mdbDef.getVersionDim(), timeDim = mdbDef.getTimeDim(), yearDim = mdbDef.getYearDim();
 		boolean hasAttributes = currentViewSection.hasAttributes();
 
 		if (newSlice.isCompressed()) {
 			logger.info("Uncompressing data slice" );
 			newSlice.uncompressData();
 		}
 		
 		
 		// 	Non-existent attribute intersections are now calculated as needed. Therefore
 		// 	the following block of code is no longer needed.
 		//
 		
 //		// Calculate attribute intersections for off-screen measures. This 
 //		// step is only needed during the first evaluation pass for a each
 //		// view, and after the data cache has been refreshed.
 //		if (hasAttributes && !pafMVS.isInitializedForAttrEval()) {
 //
 //			// Create member filter containing list of off-screen measures
 //			Map<String, List<String>> memberFilter = new HashMap<String, List<String>>();
 //			List<String> measureList = new ArrayList<String>(Arrays.asList(dataCache.getDimMembers(measureDim)));
 //			measureList.removeAll(Arrays.asList(sliceParms.getMembers(measureDim)));
 //			memberFilter.put(measureDim, measureList);
 //
 //			// Calculate attribute intersections
 //			long attrInitStart = System.currentTimeMillis();
 //			PafDataCacheCalc.calcAttributeIntersections(dataCache, clientState, sliceParms,
 //					memberFilter, DcTrackChangeOpt.NONE);
 //			pafMVS.setInitializedForAttrEval(true);
 //			String logMsg = LogUtil.timedStep("Attribute Eval Initialization", attrInitStart);
 //			evalPerfLogger.info(logMsg);
 //		}
 		
 		// Set the measure rule set. If a measure rule set name is specified,
 		// use that rule set, else just use the default rule set.
 		RuleSet measureRuleset;
 		if (evalRequest.getRuleSetName() == null || evalRequest.getRuleSetName().trim().equals("")) {
 			measureRuleset = clientState.getDefaultMsrRuleset();
 		}
 		else {
 			measureRuleset = clientState.getMsrRulsetByName(evalRequest.getRuleSetName());
 		}
 		measureRuleset = resolveRuleSetSettings(appSettings, measureRuleset);				// TTN-1792
 		
 		// Take a back up of plannable data. Lift allocation requires that the original
 		// cell values are recorded (TTN-1793).
 		if ( (evalRequest.getLiftAllCells() != null && evalRequest.getLiftAllCells().getCoordCount() > 0)
 				|| (evalRequest.getLiftExistingCells() != null && evalRequest.getLiftExistingCells().getCoordCount() > 0) 
 					|| (measureRuleset.getLiftAllMeasureList() != null && measureRuleset.getLiftAllMeasureList().length > 0)
 						|| (measureRuleset.getLiftExistingMeasureList() != null && measureRuleset.getLiftExistingMeasureList().length > 0) ) {
 			logger.info("Taking a snapshot of plannable data for lift allocation(s)");
 			dataCache.snapshotPlannableData();
 		}
 		
 		// Update the data cache with updated client data. 
 		logger.info("Updating data cache with client data: " + sliceParms.toString() );
 		dataCache.updateDataCache(newSlice, sliceParms);
 
 		IEvalStrategy evalStrategy = new RuleBasedEvalStrategy();
 
 		// Create slice state object (holds info about evaluation request sent over from the client)
 		SliceState sliceState = new SliceState(evalRequest);
 		sliceState.setDataSliceParms(sliceParms);
 				
 		// Convert user changes that correspond to lift allocation measures to lift allocation
 		// changes. This involves taking these user change intersections and moving them into 
 		// the appropriate lift collections. (TTN-1793)
 		ES_ProcessReplication.convertLiftAllocChanges(sliceState, measureRuleset, dataCache);
 
 		// Create evaluation state object (holds and tracks information that
 		// is key to the evaluation process)
 		EvalState evalState = new EvalState(sliceState, clientState, dataCache);
 		evalState.setAxisSortPriority(currentViewSection.getDimensionCalcSequence());
 		evalState.setDimSequence(currentViewSection.getDimensionsPriority());
 		evalState.setAttributeEval(hasAttributes);
 		evalState.setMeasureRuleSet(measureRuleset);
 
 		
 		// Set the axisAllocPriority property on eval state. The axisAlocPriority
 		// is equal to the axisSortPriority, except that the time and year 
 		// dimensions are replaced with time horizon dimension (TTN-1595).
 		List<String> axisPriorityList = new ArrayList<String>(Arrays.asList(evalState.getAxisSortPriority()));
 		axisPriorityList.remove(yearDim);
 		int timeDimIndex = axisPriorityList.indexOf(timeDim);
 		axisPriorityList.remove(timeDim);
 		axisPriorityList.add(timeDimIndex, dataCache.getTimeHorizonDim());
 		evalState.setAxisAllocPriority(axisPriorityList.toArray(new String[0]));
 		
 		// Check for contribution percent formulas on view section
 		List<String> contribPctVersions = appDef.getContribPctVersions();
 		String[] viewSectionVersions = currentViewSection.getDimensionMembers(versionDim); 
 		for (String viewSectionVersion : viewSectionVersions) {
 			if (contribPctVersions.contains(viewSectionVersion)) {
 				evalState.setContribPctFormulas(true);
 				break;
 			}
 		}
 		
 		// log user change request, performed here as request objects have been converted to richer intersection objects at this point
 		auditLogger.info("Changes: " + evalState.getOrigChangedCells().toString() );
 		auditLogger.info("Locks: " +  evalState.getOrigLockedCells().toString() );
 
 		
 		
 		// Perform evaluation strategy
 		logger.info("Executing Strategy");
 		dataCache = evalStrategy.executeStrategy(evalState);
 
 		logger.info("Evaluation Complete");
 		
 	}
 	
 
 	/**
 	 * Resolve rule set options 
 	 * 
 	 * @param appSettings Application settings
 	 * @param rule S Rule set
      *
 	 * @return Updated rule set
 	 */
 	private RuleSet resolveRuleSetSettings(AppSettings appSettings, RuleSet ruleSet) {
 		
 		// Resolve allocType setting - rule set setting takes precedence over global setting
 		AllocType rsAllocType = ruleSet.getAllocType();
 		if (rsAllocType == null) {
 			AllocType globalAllocType = appSettings.getGlobalAllocType();
 			if (globalAllocType != null) {
 				rsAllocType = globalAllocType;
 			} else {
 				// Not rule set or global setting - use default allocation type
 				rsAllocType = PafAppService.getDefaultAllocType();
 			}
 		}
 //		// For testing purposes - look for alloc type in rule set comment
 //		
 //		String comment = ruleSet.getComment();
 //		if (comment != null && comment.contains(AllocType.AbsAlloc.toString())) {
 //			rsAllocType = AllocType.AbsAlloc;
 //		}
 		ruleSet.setAllocType(rsAllocType);
 			
 		return ruleSet;
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
 	
 	@SuppressWarnings("unchecked")
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
 		@SuppressWarnings("unchecked")
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
 	public void loadApplicationData() throws PafException {
 
 		String appId = "[Unspecified]";
 		String dataSourceId = "[Unspecified]";
 		String errMsg = null;
 		initDataMaps();
 		
 		
 		// assumes a single application at this point
 		// loads all dimensions into the tree hash map, so at the very least
 		// multiple apps couldn't share dimensions
 
 		// Attribute trees must be loaded first, since attribute info is needed 
 		// to populate the base trees
 		try {
 
 			List<PafApplicationDef> pafApps = PafMetaData.getPaceProject().getApplicationDefinitions();
 			
 			for (PafApplicationDef pafApp : pafApps ) {
 				
 				
 				appId = pafApp.getAppId();
 				String appString = "for application [" + appId + "]";
 				dataSourceId = pafApp.getMdbDef().getDataSourceId();
 				IPafConnectionProps connProps = (IPafConnectionProps) PafMetaData.getMdbProp(dataSourceId);
 				if (connProps == null) {
 					errMsg = String.format(". The data source id [%s] is undefined.", dataSourceId);
 					throw new IllegalArgumentException(errMsg);
 				}
 				
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
 				this.initDimTreeStore(pafApp);
 																
 				this.initCellNotes(pafApp);
 			}
 		} catch (Exception ex) {
 			errMsg = String.format("Error loading application [%s]", appId) + errMsg;
 			logger.fatal(errMsg);
 			throw new PafException(errMsg, ex, PafErrSeverity.Error);
 		}
 	}
 
 	private void initDataMaps() {
 		
 		//clear existing maps
 		baseTrees.clear();
 		attributeTrees.clear();
 		uowCache.clear();
 		systemLockedIntersections.clear();		
 		
 	}
 
 
 	/**
 	 * 
 	 * Initializes cell note data. 
 	 *
 	 * @param pafApp The application to initialize.
 	 * @throws PafException
 	 */
 	private void initCellNotes(PafApplicationDef pafApp) throws PafException {
 		
 		//if clear all is true, delete all cell notes for every app and every datasource
 		if ( PafMetaData.getServerSettings().isClearAllCellNotes() ) {
 			
 			PafCellNoteManager.getInstance().deleteCellNotes();
 		
 		//if just clear cell notes is ture, delete cell notes for current app and current datasource.
 		} else if ( PafMetaData.getServerSettings().isClearCellNotes() ) {
 			
 			PafCellNoteManager.getInstance().deleteCellNotes(pafApp.getAppId(), pafApp.getMdbDef().getDataSourceId());
 			
 		}
 		
 		IPafConnectionProps connProps = (IPafConnectionProps) PafMetaData.getMdbProp(pafApp.getMdbDef().getDataSourceId());
 		
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
 		PafMdbProps mdbProps = null;
 		
 		IMdbMetaData metaData  = getMetaDataProvider(connProps);
 		if( metaData != null ) {
 			 mdbProps = metaData.getMdbProps();
 
 			if(attributeTrees != null){
 				mdbProps.setCachedAttributeDims(attributeTrees.keySet().toArray(new String[0]));
 			}
 		}
 		return mdbProps;
 	}    
 
 	public void cacheUow(String clientId) {
 
 		uowCache.get(clientId);
 	}
 		
 	/**
 	 *	Returns a List with an expanded expressionList.
 	 *  The Filtering is done against the current UOW
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
 		
 		//Save the connection props for this application id into the client state
 		String dsId = appDef.getMdbDef().getDataSourceId();
 		IPafConnectionProps connProps = (IPafConnectionProps) PafMetaData.getMdbProp(dsId);
 		clientState.getDataSources().put(dsId, connProps);
 
 //		TTN-1406		
 //		IMdbClassLoader mdbClassLoader = this.getMdbClassLoader(connProps);
 //		IMdbData mdbData = mdbClassLoader.getMdbDataProvider();
 		IMdbData mdbData = getMdbDataProvider(connProps);
 		
 		//Filter the data using an Essbase MDX query with the Non Empty flag
 		PafDimSpec[] filteredMetadata = mdbData.getFilteredMetadata(expandedUOW, appDef);
 		
 		return filteredMetadata;
 	}
 
 
 	/**
 	 * Create a UOW specification that matches the client role filter user selections
 	 * 
 	 * @param clientState Client state
 	 * @param userSelections Role filter member selections
 	 * 
 	 * @return UnitOfWork
 	 * @throws PafException 
 	 */
 	protected UnitOfWork createUserFilteredWorkSpec(PafClientState clientState, PafDimSpec[] userSelections) throws PafException {
 
 		/*
 		 * This method was built to hold code that was originally part of PafServerProvider.getfilteredUowSize(). 
 		 * This method was created to make the PafService layer more manageable (TTN-1644).
 		 */
 		
 		UnitOfWork workUnit = clientState.getUnitOfWork().clone();
 		MemberTreeSet uowTrees = clientState.getUowTrees();
 		
 		
 		//Get all possible hierarchical base dimension with attributes
 		String[] hierDims = clientState.getApp().getMdbDef().getHierDims();
 		Map<String,Set<String>> hierDimsMap = new HashMap<String,Set<String>>();
 		for(String baseDim : hierDims){
 			//Get all possible attributes for the hierarchical base dimension
 			hierDimsMap.put(baseDim, getBaseTree(baseDim).getAttributeDimNames());
 		}
 		
 		// Get the role filter user selections. Each selection is the root 
 		// of an individual branch to include in the UOW.
 		PafDimSpec[] pafDimSpecs = userSelections;
 		//Convert to Map
 		Map<String, List<String>> userSelectionsMap = new HashMap<String, List<String>>();
 		for(PafDimSpec dimSpec : pafDimSpecs){
 			String dim = dimSpec.getDimension();
 			if(dim != null && dimSpec.getExpressionList() != null){
 				
 				// Apply a post-order sort (default reporting order) to members in expression
 				// since the user selections are sorted in the ordered in selection order,
 				// not tree order.  (TTN-1644)
 				ArrayList<String> expressionList =  new ArrayList<String>(Arrays.asList(dimSpec.getExpressionList()));
 				PafDimTree dimTree = uowTrees.getTree(dim);
 				dimTree.sortMemberList(expressionList, TreeTraversalOrder.POST_ORDER);
 				userSelectionsMap.put(dim, expressionList);
 
 				// Place any orphan members at the end of the expression list. This is 
 				// purely a presentation issue. This will help to prevent it appearing
 				// as if these orphan members are contained in one of the other 
 				// branches, and make it clearer the they roll up directly under the 
 				// synthetic root (TTN-1644).
 				List<String> orphanMembers = new ArrayList<String>();
 				String root = dimTree.getRootNode().getKey();
 				for (String member : expressionList) {
 					// An orphan is any member who's parent is not the root of the tree
 					PafDimMember parentNode = dimTree.getMember(member).getParent();
 					if (parentNode != null && parentNode.getKey() != root) {
 						orphanMembers.add(member);
 					}
 				}
 				for (String orphanMember : orphanMembers) {
 					expressionList.remove(orphanMember);
 					expressionList.add(orphanMember);
 				}
 
 			}
 		}
 		
 		// Add the role filter user selections to client state (TTN-1472)
 		clientState.setRoleFilterSelections(userSelectionsMap);
 		
 		// Process the selection on each role filter dimension
 		for(String baseDim : hierDimsMap.keySet()){
 			
 			List<List<String>> discontigMbrGrps; 
 			if (userSelectionsMap.containsKey(baseDim)){
 
 				// Get this list of members in the filtered dimension
 				List<String> expressionList = userSelectionsMap.get(baseDim);
 				PafDimTree dimTree = uowTrees.getTree(baseDim);
 				String root = dimTree.getRootNode().getKey();
 				if (workUnit.isDiscontigDim(baseDim) && expressionList.get(0).equals(root)) {					
 					// If this dimension is discontiguous and the UOWRoot is selected,
 					// the re-select all of the discontiguous member lists. (TTN-1748)
 					expressionList = new ArrayList<String>();
 					discontigMbrGrps = workUnit.getDiscontigMemberGroups().get(baseDim);
 					for (int i = 0; i < discontigMbrGrps.size(); i++) {
 						expressionList.addAll(discontigMbrGrps.get(i));
 					}
 					
 				} else {
 					// ELSE, wrap each selected member inside the expression - 
 					// '@IDESC([member name], 0)'. This  will force all descendants of
 					// each selected member to be included in the UOW. This section of
 					// code has been modified to handle multiple selections per base 
 					// dimension (TTN-1644).
 					for (int i = 0; i < expressionList.size(); i++) {
 						expressionList.set(i, ExpOperation.I_DESC_TAG + "(" +  expressionList.get(i) + ", 0)");
 					}
 
 					// Next expand the base dimension expression list and process discontiguous
 					// hierarchy, if one exists (TTN-1644)
 					discontigMbrGrps = new ArrayList<List<String>>();
 					expressionList = expandUowDim(baseDim, expressionList.toArray(new String[0]), clientState, discontigMbrGrps, true); 
 					
 				}
 				
 				
 				// Update discontinuous member properties for current dimension. We also
 				// need to address the situation in which a dimension that was comprised
 				// of discontiguous branches, has been filtered down to a single branch
 				// and therefore is no longer discontiguous. (TTN-1644)
 				Map<String, List<List<String>>> uowDiscMbrGrpsMap = workUnit.getDiscontigMemberGroups();
 				if (!discontigMbrGrps.isEmpty()) {
 					// Dimension is discontiguous
 					uowDiscMbrGrpsMap.put(baseDim, discontigMbrGrps);
 				} else {
 					// Dimension is not discontiguous
 					if (uowDiscMbrGrpsMap.containsKey(baseDim)) {
 						uowDiscMbrGrpsMap.remove(baseDim);
 					}
 				}
 				
 				//Get a list of attribute dimensions and a list of attribute member lists
 				List<String> attrDimLists = new ArrayList<String>();
 				List<List<String>> attrMemberLists = new ArrayList<List<String>>();
 				for(String hierDim : hierDimsMap.get(baseDim)){
 					if (userSelectionsMap.containsKey(hierDim)){
 						attrDimLists.add(hierDim);
 						attrMemberLists.add(userSelectionsMap.get(hierDim));
 					}
 				}
 				String[] attrDims = attrDimLists.toArray(new String[0]);
 
 				//If there are no attribute dimensions, then do not filter the base dimension
 				List<String> validBaseMemberList = new ArrayList<String>();
 				List<String> filteredBaseMemberList = new ArrayList<String>();		// TTN-1786
 				if(attrDimLists.size() ==0){
 					//Build a map of valid members for each base dimension
 					for(String baseMember : expressionList){
 						validBaseMemberList.add(baseMember);
 					}
 				}
 				else{
 					//Get a list of all possible attribute intersection lists
 					StringOdometer isIterator = new StringOdometer(attrMemberLists.toArray(new List[0]));
 					List<Intersection> selAttrCombos = new ArrayList<Intersection>();
 					while (isIterator.hasNext()) {
 						@SuppressWarnings("unchecked")
 						Intersection intersection = new Intersection(attrDims, isIterator.nextValue());		// TTN-1851
 						selAttrCombos.add(intersection);
 					}					        
 
 					//Build a map of base dimension members that are valid for the selected attributes
 					for(String baseMember : expressionList){
 						Set<Intersection> validAttrCombos = AttributeUtil.getValidAttributeCombos(baseDim, baseMember, attrDims, getAllDimTrees());
 						validAttrCombos.retainAll(selAttrCombos);
 						if(!validAttrCombos.isEmpty()){
 							validBaseMemberList.add(baseMember);
 						} else {
 							filteredBaseMemberList.add(baseMember);
 						}
 					}
 					
 					// Filter out any upper level rollups that are ancestors of the filtered members
 					// with the exception of UOW Root (this could be a little better optimized - TTN-1786)
 					String uowRoot = expressionList.get(0); 
 					for (String filteredBaseMbr : filteredBaseMemberList) {
 						List<String> ancestors = PafDimTree.getMemberNames(dimTree.getAncestors(filteredBaseMbr));
 						ancestors.remove(uowRoot);
 						validBaseMemberList.removeAll(ancestors);
 					}
 					
 					// Since this dimension is filtered, the discontiguous member group collection needs to be
 					// populated for this dimension, so that the corresponding uow tree is built properly as
 					// a discontiguous tree. The root member must appear first, its own list, followed
 					// by the remaining base members, grouped by branch, each in their own list (TTN-1644).
 					discontigMbrGrps = getBranchLists(validBaseMemberList, uowTrees.getTree(baseDim));
 					workUnit.getDiscontigMemberGroups().put(baseDim, discontigMbrGrps);					
 				}
 				
 				// Update the work unit and client tree for this filtered dimension
 				workUnit.setDimMembers(baseDim, validBaseMemberList.toArray(new String[0]));
 			}
 
 		}
 		
 		return workUnit;
 	}
 
 
 	/**
 	 * Validate user security specifications
 	 * 
 	 * @param securitySpecs User security specifications, one for each security dimension
 	 * @param validationErrors A list to contain any found validation errors
 	 * @param clientState Client state
 	 * 
 	 * @return Success status
 	 * @throws PafException 
 	 */
 	public boolean validateUserSecurity(PafDimSpec[] securitySpecs, List<String> validationErrors, PafClientState clientState) throws PafException {
 		
 		Set<String>securityDims = new HashSet<String>();
 		Set<String> expressionSet = new HashSet<String>(), dupExpressionSet = new HashSet<String>();
 		Map<String, List<String>> memberMap = new HashMap<String, List<String>>();			// Tracks all the expression lists that a member is referenced in
 
 		String errMsg = null;
 		boolean isValid = true;
 		
 		// Validate each security specification. There will be one for each security dimension.
 		for (PafDimSpec securitySpec : securitySpecs) {
 			
 			// Ensure that there's only one security specification per dimension
 			String dim = securitySpec.getDimension();
 			if (!securityDims.contains(dim)) {
 				securityDims.add(dim);
 			} else {
 				errMsg = "More than one security specification for dimension: [" 
 						+ dim + "] was found.";
 				validationErrors.add(errMsg);
 				isValid = false;
 			}
 
 			// Validate each expression and check for duplicate expressions and expanded
 			// members.
 			String [] expressions = securitySpec.getExpressionList();
 			for (String exp : expressions) {
 
 				// Check for duplicate expression
 				if (!expressionSet.contains(exp)) {
 					expressionSet.add(exp);
 				} else {
 					isValid = false;
 					dupExpressionSet.add(exp);
 				}
 				
 				// Expand expression
 				List<String> expressionAsList = new ArrayList<String>();
 				expressionAsList.add(exp);
 				List<String> members = this.expandExpressionList(dim, expressionAsList, null);
 				
 				// Check each expanded member
 				for (String member : members) {
 					
 					// Add current expression to map
 					if (memberMap.containsKey(member)) {
 						memberMap.get(member).add(exp);
 					} else {
 						List<String> expressionList = new ArrayList<String>();
 						expressionList.add(exp);
 						memberMap.put(member, expressionList);
 					}
 				}
 			}	
 			
 			
 			// Generate error messages for any duplicate expressions
 			for (String exp : dupExpressionSet) {
 				errMsg = "Member specification: [" + exp + "] appears multiple times in the security specification for security dimension: ["
 						+ dim + "].";
 				validationErrors.add(errMsg);
 			}
 			
 			// Generate error messages for any duplicate members
 			for (String member : memberMap.keySet()) {
 				List<String> expressionList = memberMap.get(member);
 				if (expressionList.size() > 1) {
 					errMsg = "Member: [" + member + "] is duplicated across the following member specifications: "
 							+ StringUtils.arrayListToString(expressionList);
 					validationErrors.add(errMsg);
 					isValid = false;
 				}
 			}
 		}
 		
 		
 		return isValid;
 	}
 
 
 	public PafMemberList getUserMemberList(String umlKey) {
 		UserMemberLists uml = PafMetaData.getPaceProject().getUserMemberLists();
 		umlKey = umlKey.trim();
 		if (uml==null || uml.getMemberList(umlKey)==null) {
 			String s = String.format("No user member list found by the label [%s]", umlKey);
 			throw new IllegalArgumentException(s);
 		} else {
 			return uml.getMemberList(umlKey);
 		}
 	}
 
 	public PaceClusteredDataSet clusterDataset(PaceDataSet inData) {
 		PaceClusteredDataSet dataSet = new PaceClusteredDataSet();
 		List<EuclideanIntegerPoint> points = new ArrayList<EuclideanIntegerPoint>();
 		IntArrayList iPoint = new IntArrayList( inData.getColCount() );
 		for (int i=0 ; i < inData.getRowCount(); i++) {
 			for (double d : inData.getRow(i) ) {
 				// build row of data points
 				iPoint.add( (int) Math.round(d) );
 			}
 			// add row as euclidean point
 			points.add(new EuclideanIntegerPoint(iPoint.elements().clone()));	
 			iPoint.clear();
 		}
 		
 		dataSet.setClusters(MathOp.clusterData(points));
 		return dataSet;
 	}
 
 
 	public PaceDataSet buildAsstDataSet(AsstSet asst) throws PafException {
 		
 		// Look at each location to cluster and convert the measures in question into metrics in an array.
 		// For each product filtered to, treat it as another metric for that location. In effect this produces
 		// a data point for each unique product / measure combination at a given location.
 		PafDataCache dc = this.getDataCache(asst.getClientId());
 
 		String[] baseDims = dc.getBaseDimensions();	
 		
 		// Initialize arrays
 		List<Double> row = new ArrayList<Double>(asst.getNumCols());
 		double[][] data = new double[asst.getNumRows()][asst.getNumCols()];
 		
 
 		Map<String, List<String>> memberFilter = new HashMap<String, List<String>>();
 		memberFilter.put("Product", Arrays.asList(asst.getDimToMeasure().getExpressionList() ) );
 		memberFilter.put("Time", Arrays.asList(asst.getTimePeriods().getExpressionList() ) );			
 		memberFilter.put(dc.getMeasureDim(), Arrays.asList(asst.getMeasures().getExpressionList() ) );
 		memberFilter.put(dc.getVersionDim(), Arrays.asList(dc.getPlanVersion()));
 		memberFilter.put(dc.getYearDim(), Arrays.asList(dc.getYears()[0]));
 		
 		
 		
 		StringOdometer so;
 		int iRow = 0; int iCol = 0;
 		
 		for (String loc : asst.getDimToCluster().getExpressionList() ) {
 			memberFilter.put("Location", Arrays.asList(loc));
 			iCol = 0;
 			so = dc.getCellIterator(baseDims, memberFilter);
 			
 			while (so.hasNext()) {
 				data[iRow][iCol++] = dc.getCellValue(baseDims, so.nextValue());
 			}
 			iRow++;
 		}
 		
 		PaceDataSet dataSet = new PaceDataSet(data);		
 		return dataSet;
 	}
 	
 	/**
 	 * Get all the descendant intersections for the SimpleCoordList parent array.
 	 * 
 	 * Any ancestor intersections, whose descendants are all represented by the parent
 	 * array, will be included in the last array element of the returned array.
 	 * 
 	 * @param ancestorCells Array of ancestor SimpleCoodList cells
 	 * @param clientState User's PafClientState
 	 * 
 	 * @return A SimpleCoordList containing all the descendant intersections (in a SimpleCoordList) format.
 	 * @throws PafException
 	 */
 	public SimpleCoordList[] getDescendants(SimpleCoordList[] ancestorCells, PafClientState clientState) throws PafException {
 		
 		final StopWatch sw = new StopWatch("getDescendants");
 		final PafDataCache dataCache = getDataCache(clientState.getClientId());
 		final String[] baseDims = dataCache.getBaseDimensions();
 		final Set<String> lockedTimeHorizPeriods = clientState.getLockedTimeHorizonPeriods();
 		Set<Coordinates> floorCoordsSet = new HashSet<Coordinates>(10000), ancestorCoordsSet = new HashSet<Coordinates>(ancestorCells.length);
 		
 
 		// Initialize result object - add an additional element to hold the intermediate parent cells
 		SimpleCoordList[] resultCoordLists = new SimpleCoordList[ancestorCells.length + 1];
 		
 		// Cycle through each ancestor cell and explode to its floor descendants. Intermediate parent cells 
 		// are added in a later step.
 		for (int i = 0; i <  ancestorCells.length; i++) {
 		 
 			//Check for null items in the request, if so set the response and return.
 			SimpleCoordList ancestorCell = ancestorCells[i];
 			if(ancestorCell == null){
 				resultCoordLists[i] = null;
 				continue;
 			}
 			
 			// Uncompress parent cell
 			if(ancestorCell.isCompressed())
 				ancestorCell.uncompressData();
 		
 			// Explode the parent cell to its floor descendants (filtering out any parents with invalid 
 			// or locked time horizon periods) 
 			Intersection ancestorIs = new Intersection(ancestorCell.getAxis(), ancestorCell.getCoordinates());
 			String timeHorizPeriod = TimeSlice.buildTimeHorizonCoord(ancestorIs, dataCache.getMdbDef());
 			if (lockedTimeHorizPeriods.contains(timeHorizPeriod))
 				continue;
 			List<Coordinates> floorCoords= IntersectionUtil.buildFloorCoordinates(ancestorIs, dataCache);
 
 			// Convert list of intersections to SimpleCoordList and place in result array
 			sw.start("CreateSimpleCoordList" );
 			SimpleCoordList resultCoordList = IntersectionUtil.convertCoordinatesToSimpleCoordList(baseDims, floorCoords);	
 			resultCoordLists[i] = resultCoordList;
 		    sw.stop();
 		    
 		    // Compress SimpleCoordList
 		    sw.start("CompressSimpleCoordList" );
 		    try {
 		    	resultCoordList.compressData();
 			} catch (IOException e) {
 				System.out.println(e.getMessage());
 			}
 		    uowPerfLogger.info("coord list length: " + Integer.toString(resultCoordList.getCoordCount()));
 		    sw.stop();
 		    uowPerfLogger.info(sw.prettyPrint());
 		    
 			// Need to also maintain an accumulated set of all floor intersections, and each ancestor intersection
 		    // for quick lookup later on.
 			floorCoordsSet.addAll(floorCoords);
 			ancestorCoordsSet.add(new Coordinates(ancestorCell.getCoordinates()));
 		}
 
 		// Get the coordinates of any parent intersections whose children have all been included
 		// in the set of exploded floor intersections
 	    sw.start("ComputeParentCells" );
 		Set<Coordinates>parentCoordsSet = IntersectionUtil.getLockedBaseParentCoords(floorCoordsSet, dataCache, floorCoordsSet);
 				 
 		// Remove any ancestor intersections that are already included in the original set 
 		// of ancestor cells.
 		parentCoordsSet.removeAll(ancestorCoordsSet);
 	    sw.stop();
 	    uowPerfLogger.info(sw.prettyPrint());
 		
 		
 		// Place the parent cells onto the last element of the results array
 		if (!parentCoordsSet.isEmpty()) {
 			SimpleCoordList ancestorCoordList = IntersectionUtil.convertCoordinatesToSimpleCoordList(baseDims, parentCoordsSet);	
 		    sw.start("CompressSimpleCoordList-Parents" );
 		    try {
 		    	ancestorCoordList.compressData();
 			} catch (IOException e) {
 				System.out.println(e.getMessage());
 			}
 		    uowPerfLogger.info("coord list length: " + Integer.toString(ancestorCoordList.getCoordCount()));
 		    sw.stop();
 		    uowPerfLogger.info(sw.prettyPrint());
 			resultCoordLists[resultCoordLists.length - 1] = ancestorCoordList;
 		}
 		
 		// Return results
 		return resultCoordLists;
 	}
  
 }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
