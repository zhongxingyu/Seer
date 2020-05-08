 /*
  *	File: @(#)PafAppService.java 	Package: com.pace.base.server 	Project: PafServer
  *	Created: Dec 5, 2005  		By: JWatkins
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
 package com.pace.server;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.CustomCommandResult;
 import com.pace.base.IPafCustomCommand;
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrHandler;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.RunningState;
 import com.pace.base.app.AllocType;
 import com.pace.base.app.AppSettings;
 import com.pace.base.app.CustomActionDef;
 import com.pace.base.app.MdbDef;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.SuppressZeroSettings;
 import com.pace.base.app.UnitOfWork;
 import com.pace.base.app.VersionType;
 import com.pace.base.comm.ApplicationState;
 import com.pace.base.comm.CustomMenuDef;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.db.membertags.MemberTagDef;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.state.IPafClientState;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.LogUtil;
 
 /**
  * Class_description_goes_here
  * 
  * @version x.xx
  * @author JWatkins
  * 
  */
 public class PafAppService {
 
 	private static PafAppService _instance = null;
 
 	private static Map<String, PafApplicationDef> applicationDefs =  Collections.synchronizedMap(new HashMap<String, PafApplicationDef>());
 	private static Map<String, ApplicationState> applicationStates = Collections.synchronizedMap(new HashMap<String, ApplicationState>());
         
 	//handles to the other main services
 	PafViewService viewService = PafViewService.getInstance();
 	PafDataService dataService = PafDataService.getInstance();
 	
     // default allocation type (TTN-1792)
     private static final AllocType DEFAULT_ALLOC_TYPE = AllocType.SignedAlloc;
     
 	private static Logger logger = Logger.getLogger(PafAppService.class);
 	private static Logger logPerf = Logger.getLogger("pace.performance"); //$NON-NLS-1$
 
 	private PafAppService() { }
 
 	public static PafAppService getInstance() {
 		if (_instance == null)
 			_instance = new PafAppService();
 		return _instance;
 	}
     
 	
 	/**
 	 * @return the defaultAllocType 
 	 */
 	protected static AllocType getDefaultAllocType() {
 		return DEFAULT_ALLOC_TYPE;
 	}
 
 
 	/**
      * Load key meta-data to each pace application
      */
     // TODO make measure and version loads driven by application
     // currently just loads all apps with the current measure/version defs
     public synchronized void loadApplicationConfigurations() {
 
     	Map<String, ApplicationState> previousAppStateMap = new HashMap<String, ApplicationState>();
     	
     	if ( applicationStates != null ) {
     		previousAppStateMap.putAll(applicationStates);
     	}
     	
         applicationDefs.clear();
         applicationStates.clear();
         
 		// update the pace project object
 		PafMetaData.updateApplicationConfig();
 				
         List<PafApplicationDef> appDefs = PafMetaData.getPaceProject().getApplicationDefinitions();
                         
         for (PafApplicationDef app : appDefs) {
         	
             // Initialize an application state objected (defaults to stopped)
             ApplicationState as = new ApplicationState(app.getAppId());
             
             as.setCurrentRunState(RunningState.LOADING);
             
             applicationStates.put(as.getApplicationId(), as);
         	
             app.initMeasures(PafMetaData.getPaceProject().getMeasures());
             app.initVersions(PafMetaData.getPaceProject().getVersions());
             app.initMemberTags(PafMetaData.getPaceProject().getMemberTags());
             app.initFunctionFactory(PafMetaData.getPaceProject().getCustomFunctions());
             app.initCustomMenus(PafMetaData.getPaceProject().getCustomMenus()); 
             applicationDefs.put(app.getAppId(), app);
             
             
             
             // might as well load the view cache as well, it's part of the application definition
             // and requires no external dependencies
             
             viewService.loadViewCache();
             
 			// initialize the user list
 			PafSecurityService.initUsers();
 			PafSecurityService.initPlannerRoles();
 			
 			if ( previousAppStateMap.containsKey(as.getApplicationId()) && previousAppStateMap.get(as.getApplicationId()).getCurrentRunState().equals(RunningState.RUNNING) ) {
 				
 				as.setCurrentRunState(RunningState.RUNNING);
 				
 			} else {
 				
 				as.setCurrentRunState(RunningState.STOPPED);
 				
 			}
 				
 			
 			applicationStates.put(as.getApplicationId(), as);
             
         }
     }
     
     public synchronized void loadApplicationMetaData(String id) throws PafException {
 		long startTime = System.currentTimeMillis();
 		
 		
 		if (id == null) { // just use the id of the currently deployed app config
 			id = getApplications().get(0).getAppId();
 		}
     	
     	try {
 			updateAppRunState(id, RunningState.STARTING);
 			PafMetaData.clearDataCache();
 			dataService.loadApplicationData();
 			
 			updateAppRunState(id, RunningState.RUNNING);
 			
 		} catch (Exception e) {
 			updateAppRunState(id, RunningState.FAILED);
 			String s = String.format(Messages.getString("PafAppService.21"), id); //$NON-NLS-1$
 			throw new PafException(s, PafErrSeverity.Error, e);
 		}
     	
 		String stepDesc = String.format(Messages.getString("PafAppService.24"), id); //$NON-NLS-1$
 		logPerf.info(LogUtil.timedStep(stepDesc, startTime));		
     }
     
     public synchronized void autoloadApplications() throws PafException {
     	// for now just check jndi/env variable for behavior.
     	// will migrate to application specific setting when multi app hits
 
     	loadApplicationConfigurations();
 		
     	if ( PafMetaData.isAutoLoad() ) {
     		loadApplicationMetaData(null);
     	}
     	
     	
     	
     }
     
     
     
 //    public synchronized void startApplication(String id, boolean reloadMetadata) throws PafException {
 //
 //		long startTime = System.currentTimeMillis();
 //    	
 //    	try {
 //			updateAppRunState(id, RunningState.STARTING);
 //			PafMetaData.clearDataCache();
 //			dataService.loadApplicationData();
 //			
 //			// initialize the user list
 //			PafSecurityService.initUsers();
 //			updateAppRunState(id, RunningState.RUNNING);
 //			
 //		} catch (Exception e) {
 //			updateAppRunState(id, RunningState.FAILED);
 //			String s = String.format("Failed to start application [%s]", id);
 //			throw new PafException(s, PafErrSeverity.Error, e);
 //		}
 //    	
 //		String stepDesc = String.format("Application [%s] Loaded", id);
 //		logPerf.info(LogUtil.timedStep(stepDesc, startTime));		
 //		
 //    }
     
     
     public synchronized void updateAppRunState(String id, RunningState state) {
     	if ( ! applicationStates.containsKey(id) ) {
     		String s = String.format(Messages.getString("PafAppService.25"), id); //$NON-NLS-1$
     		throw new IllegalArgumentException(s);
     	} else {
     		applicationStates.get(id).setCurrentRunState(state);
     	}
     }
     
     public synchronized RunningState getAppRunState(String id) {
     	if ( ! applicationStates.containsKey(id) ) {
     		String s = String.format(Messages.getString("PafAppService.26"), id); //$NON-NLS-1$
     		throw new IllegalArgumentException(s);
     	} else {
     		return applicationStates.get(id).getCurrentRunState();
     	}    	
     }
 
 	/**
 	 * @return Returns the applications.
 	 */
 	public List<PafApplicationDef> getApplications() {
 		return new ArrayList<PafApplicationDef>(applicationDefs.values());
 	}
 
 	public PafApplicationDef getApplication(String id) {
 		if (applicationDefs.containsKey(id))
 			return (applicationDefs.get(id));
 		else
 			throw new IllegalArgumentException(Messages.getString("PafAppService.0") //$NON-NLS-1$
 				+ id + Messages.getString("PafAppService.1")); //$NON-NLS-1$
 	}
 	
 	public ApplicationState getApplicationState(String id) {
 		ApplicationState appState = applicationStates.get(id);
 		if (appState == null) {
 			throw new IllegalArgumentException(Messages.getString("PafAppService.0") //$NON-NLS-1$
 				+ id + Messages.getString("PafAppService.1")); //$NON-NLS-1$
 		}
 		
 		return appState;
 	}
 	
 	public void setApplicationState(String id, ApplicationState state) {
 		applicationStates.put(id, state);
 	}
 	
 	public List<ApplicationState> getAllApplicationStates() {
 		return new ArrayList<ApplicationState>(applicationStates.values());		
 	}
  
 
     public CustomCommandResult[] runCustomCommand(String menuId, String[] parmKeys, String[] parmValues, IPafClientState clientState) throws PafException {
         
         IPafCustomCommand customCmd = null;
         List<CustomCommandResult> results = new ArrayList<CustomCommandResult>();
         
         // get implementing class name from menu id
         for (CustomActionDef actionDef : clientState.getApp().getCustomMenuDef(menuId).getCustomActionDefs()) {
             
             try {
                 customCmd = (IPafCustomCommand) Class.forName(actionDef.getActionClassName()).newInstance();
             }
             catch (Exception e) {
                 PafErrHandler.handleException (Messages.getString("PafAppService.5") + actionDef.getActionClassName() + Messages.getString("PafAppService.6"), PafErrSeverity.Error, e, clientState); //$NON-NLS-1$ //$NON-NLS-2$
             } 
             
             // TODO Consolidate/error check parm processing code
             Properties props = new Properties();
             int i = 0;
             String[] kvPair;
             
             // 1st add menudef parms
             for (String namedParms : actionDef.getActionNamedParameters()) {
             	kvPair = namedParms.split(Messages.getString("PafAppService.7")); //$NON-NLS-1$
             	props.put(PafBaseConstants.CC_TOKEN_PREFIX_ACTION_PARM + kvPair[0].toUpperCase(), kvPair[1]);
             }
             
             // 2nd add client keys parms, can override embedded parms
             if (parmKeys != null) {
             	for (String key: parmKeys) {
             		props.put(key.toUpperCase(), parmValues[i++]);
             	}
             }
             
     		// Generate token catalog
     		Properties tokenCatalog = clientState.generateTokenCatalog(props);
 
             // TODO spin up thread for timeout control
             try {
                 if (customCmd != null)
                     results.add(customCmd.execute(tokenCatalog, clientState));
             }
             catch (PafException pex) {
                 pex.setClientState(clientState);
                 pex.addMessageDetail(Messages.getString("PafAppService.8") + actionDef.getActionClassName() + Messages.getString("PafAppService.9")); //$NON-NLS-1$ //$NON-NLS-2$
                 throw pex;
             }
         }
         
         return results.toArray(new CustomCommandResult[0]);
     }
     /**
      * This method maps the String custom menu name with the actual custom menu def object.
      * 
      * @param customMenuDefNames
      *            array of custom menu def names
      * @param app 
      * @return CustomMenuDef[]
      */
     
     public CustomMenuDef[] generateCustomMenuDefsFromCustomMenuDefNames(String[] customMenuDefNames, PafApplicationDef app) {
         
         //create an empty array list
         ArrayList<CustomMenuDef> customMenuDefList = new ArrayList<CustomMenuDef>();
         
         //if the custom menu def names array is null, return null array
         if ( customMenuDefNames == null )
             return null;        
                 
         //for each custom menu name, check to see if in map and if so, map name to object
         for (String customMenuDefName : customMenuDefNames ) {
             
             //if in map, add to list otherwise log as error
             if ( app.getCustomMenuDef(customMenuDefName) != null) {
                 customMenuDefList.add(app.getCustomMenuDef(customMenuDefName));
             } else {
                 logger.error(Messages.getString("PafAppService.10") + customMenuDefName + Messages.getString("PafAppService.11") + PafBaseConstants.FN_CustomMenus + Messages.getString("PafAppService.12")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
             }
         }       
         
         //convert array list to array
         return customMenuDefList.toArray(new CustomMenuDef[0]);
     }
 
 
 	/**
 	 *  This method applies the Global Settings, if appropriate, to the Suppress Zero Settings
 	 * 
 	 * @param suppressZeroSettings
 	 * @param appId
 	 */
     public SuppressZeroSettings resolveSuppressZeroSettings(SuppressZeroSettings suppressZeroSettings, String appId){
     	
     	AppSettings appSet = this.getApplication(appId).getAppSettings();
     	
     	boolean nullFlag = false;
     	
     	if (suppressZeroSettings == null){
     		suppressZeroSettings = new SuppressZeroSettings();
     		nullFlag = true;
     	}
     				
     	if (nullFlag == true || suppressZeroSettings.getColumnsSuppressed() == null){
     		suppressZeroSettings.setColumnsSuppressed(appSet.getGlobalSuppressZeroSettings().getColumnsSuppressed());
     	}
     	
     	if (nullFlag == true || suppressZeroSettings.getRowsSuppressed() == null){
     		suppressZeroSettings.setRowsSuppressed(appSet.getGlobalSuppressZeroSettings().getRowsSuppressed());
     	}
     	
     	if (nullFlag == true || suppressZeroSettings.getEnabled() == null){
     		suppressZeroSettings.setEnabled(appSet.getGlobalSuppressZeroSettings().getEnabled());
     	}
     	
     	if (nullFlag == true || suppressZeroSettings.getVisible() == null){
     		suppressZeroSettings.setVisible(appSet.getGlobalSuppressZeroSettings().getVisible());
     	}
     	
     	return suppressZeroSettings;
     }
     
     
     
 	/**
 	 *  This method resolves the planner variious planner overrides and defaults between
 	 *  an applicaitons global settings and a specific planner config.
 	 *  it updated the planner config object
 	 * 
 	 * @param plannerConfig
 	 * @param appId
 	 */
 	public PafPlannerConfig resolvePlannerOverrides(PafPlannerConfig plannerConfig, String appId) {
 		
 		AppSettings appSet = this.getApplication(appId).getAppSettings();
 		
 		if (plannerConfig.getIsDataFilteredUow() == null) {
 			// then try to use a global setting
 			if (appSet != null && appSet.isGlobalDataFilteredUow() ) {
 				plannerConfig.setIsDataFilteredUow(true);
 			}
 			// default behavior is false if null and no global setting
 			else plannerConfig.setIsDataFilteredUow(false);
 			
 			// also try to use a global setting
 			if (appSet != null && appSet.getGlobalDataFilterSpec() != null ) {
 				plannerConfig.setDataFilterSpec(appSet.getGlobalDataFilterSpec());		
 			}
 		}
 		
 		
 		
 		if (plannerConfig.getIsUserFilteredUow() == null) {
 			// then try to use a global setting
 			if (appSet != null && appSet.isGlobalUserFilteredUow() ) {
 				plannerConfig.setIsUserFilteredUow(true);
 			}
 			// default behavior is false if null and no global setting
 			else plannerConfig.setIsUserFilteredUow(false);			
 			
 			// also try to use a global setting
 			if (appSet != null && appSet.getGlobalUserFilterSpec() != null ) {
 				plannerConfig.setUserFilterSpec(appSet.getGlobalUserFilterSpec());	
 			}
 			
 		}
 		
 		//TTN 1733 - Role Filter Single Select
 		//if isUserFilteredMultiSelect is null, retrieve global setting from appset
 		if (plannerConfig.getIsUserFilteredMultiSelect() == null) {
 			// then try to use a global setting
 			if (appSet != null ) {
 				if( appSet.isGlobalUserFilteredMultiSelect() ) {
 					plannerConfig.setIsUserFilteredMultiSelect(true);
 				}
 			}
 			// default behavior is false if null and no global setting
 			else {
 				plannerConfig.setIsUserFilteredMultiSelect(false);
 			}
 		}
 		
 		return plannerConfig;
 		
 	}
 
 	public boolean isValidClient(String clientVersion, String clientType) {
 
 		boolean bValid = true;
 		
 		// Just return true any client other than Excel client
 		if (!clientType.toUpperCase().contains(Messages.getString("PafAppService.14"))) return true; //$NON-NLS-1$
 
 		try {
 			
 //			 not set, return true
 			if (PafMetaData.getServerSettings().getClientMinVersion() == null || PafMetaData.getServerSettings().getClientUpdateUrl() == null){
 				
 				if(PafMetaData.getServerSettings().getClientMinVersion() != null){
 					logger.error(Messages.getString(Messages.getString("PafAppService.15"))); //$NON-NLS-1$
 				}else if(PafMetaData.getServerSettings().getClientUpdateUrl() != null){
 					logger.error(Messages.getString(Messages.getString("PafAppService.16"))); //$NON-NLS-1$
 				}
 				return true;
 			}
 
 			String minVers[] = PafMetaData.getServerSettings().getClientMinVersion().split(Messages.getString("PafAppService.17")); //$NON-NLS-1$
 
 
 			// not set, return true (probably not needed)
 			if (minVers == null)
 				return true;
 
 			// set to 0, don't check
 			if (Integer.valueOf(minVers[0]) == 0)
 				return true;
 
 			// fine grained compare
 			String clientVers[] = clientVersion.split(Messages.getString("PafAppService.18")); //$NON-NLS-1$
 			
 			//Check to see if the minimum server version is in the same format as the client version
 //			if (clientVers.length != minVers.length){
 //				logger.error(Messages.getString("The minimum client version in the serverConfig is invalid."));
 //				return true;
 //			}
 			
 			for (int i = 0; i < 4; i++) {
 				
 				//if (minVers[i].)
 				try{
 					if (Integer.valueOf(clientVers[i]) > Integer.valueOf(minVers[i])) {
 						bValid = true;
 						break;
 					}
 					else if (Integer.valueOf(clientVers[i]) < Integer.valueOf(minVers[i])) {
 						bValid = false;
 						break;
 					}
 				}
 				catch (NumberFormatException nfe){
 					logger.error(Messages.getString(Messages.getString("PafAppService.19"))); //$NON-NLS-1$
 					return true;
 				}
 
 			}
 
 		// if anything goes wrong, log the exception, but allow the client to try.
 		} catch (Exception e) {
 			PafErrHandler.handleException(Messages.getString("PafAppService.20"), PafErrSeverity.Warning, e, null); //$NON-NLS-1$
 			bValid = true;
 		}
 
 		return bValid;
 	}
 
 	/**
 	 *  Get all or specified member tag definitions
 	 *
 	 * @param appId Application Id
 	 * @param memberTagNames Optional member tag filter
 	 * 
 	 * @return MemberTagDef
 	 */
 	public MemberTagDef[] getMemberTagDefs(String appId, String[] memberTagNames) {
 
 		MemberTagDef[] memberTagDefs = null;
 		PafApplicationDef appDef = getApplication(appId);
 		
 		
 		// Get member tag definitions
 		if (memberTagNames == null || memberTagNames.length == 0) {
 			// If no member tag names were provided then get all member tag defs
 			memberTagDefs = appDef.getMemberTagDefs().values().toArray(new MemberTagDef[0]);
 		} else {
 			// Else, only get the definitions for the specified member tag names
 			memberTagDefs = new MemberTagDef[memberTagNames.length];
 			for (int i = 0; i < memberTagNames.length; i++) {
 				memberTagDefs[i] = appDef.getMemberTagDef(memberTagNames[i]);
 			}
 		}
 		
 		// Return member tag definitions
 		return memberTagDefs;
 	}      
 
 
 	/**
 	 * Populate the client state's locked and invalid period collections corresponding 
 	 * to the current uow.
 	 * 
 	 * @param clientState Client state
 	 * @param lockedTimeSlices Locked time slice coordinates
 	 * @param invalidTimeSlices Invalid time slice coordinates
 	 * @param lockedPeriodMap Locked time periods by year
 	 * 
 	 * @throws PafException 
 	 */
 	public void populateLockedPeriodCollections(PafClientState clientState) throws PafException {
 
 		final PafApplicationDef pafApp = clientState.getApp(); 
 		final MdbDef mdbDef = pafApp.getMdbDef();
 		final MemberTreeSet uowTrees = clientState.getUowTrees();
 		final String timeDim = mdbDef.getTimeDim(), yearDim = mdbDef.getYearDim();
 		final PafDimTree timeTree =  uowTrees.getTree(timeDim), yearTree =  uowTrees.getTree(yearDim);
 		final PafDimTree timeHorizTree = clientState.getTimeHorizonTree();	
 		final UnitOfWork uow = clientState.getUnitOfWork();
 		final boolean isCalcElapsedPeriods = clientState.getPlannerConfig().isCalcElapsedPeriods();
 		Set<TimeSlice> lockedTimeSlices = new HashSet<TimeSlice>(), invalidTimeSlices = new HashSet<TimeSlice>();
 		Set<String> lockedTimeHorizPeriods = new HashSet<String>(), invalidTimeHorizPeriods = new HashSet<String>();
 		Map<String, Set<String>> lockedPeriodMap = new HashMap<String, Set<String>>(), invalidPeriodMap = new HashMap<String, Set<String>>();	
 		
 		
 		// Initialize locked & invalid period maps
 		String[] years = uow.getDimMembers(yearDim);
 		for (String year : years) {
 			lockedPeriodMap.put(year, new HashSet<String>());
 			invalidPeriodMap.put(year, new HashSet<String>());		// TTN-1858 - week 53 support
 		}
 		
 		// Set the locked time horizon periods that correspond to an individual year,
 		// omitting those periods that are associated with the aggregate year
 		// (in the case of a multi-year uow).
 		//
 		// This step will be skipped if elapsed periods are to be calculated or 
 		// the plan version is not forward plannable.
 		if (!isCalcElapsedPeriods) {
 
 			String currentVersion = clientState.getPlanningVersion().getName();
 			Map<String, VersionType> versionsTypeMap = PafMetaData.getVersionTypeMap();
 			VersionType versionType = versionsTypeMap.get(currentVersion);
 			if (versionType.equals(VersionType.ForwardPlannable)) {
 				Set<String> lockedTimeHorizFloorPeriods = this.getLockedList(clientState);
 				for (String lockedTimeHorizPeriod : lockedTimeHorizFloorPeriods) {
 					lockedTimeHorizPeriods.add(lockedTimeHorizPeriod);
 					TimeSlice timeSlice = new TimeSlice(lockedTimeHorizPeriod);
 					lockedTimeSlices.add(timeSlice);
 					lockedPeriodMap.get(timeSlice.getYear()).add(timeSlice.getPeriod());
 				}
 			}
 			
 		}
 
 		// Lock any invalid time / year coordinates - those that are not defined
 		// in the time horizon tree - OR those in which the year is read-only (TTN-1860)
 		String[] uowPeriods = timeTree.getMemberKeys();
 		String[] uowYears = yearTree.getMemberKeys();
 		Set<String> readOnlyYears = yearTree.getReadOnlyMemberNames();		// TTN-1860 - non-plannable year support
 		for (String year : uowYears) {
 			for (String period : uowPeriods) {
 				String timeHorizCoord = TimeSlice.buildTimeHorizonCoord(period, year);
 				TimeSlice timeSlice = new TimeSlice(timeHorizCoord);
 				if (!timeHorizTree.hasMember(timeHorizCoord)) {
 					invalidTimeHorizPeriods.add(timeHorizCoord);
 					invalidTimeSlices.add(timeSlice);
 					lockedPeriodMap.get(year).add(period);
 					invalidPeriodMap.get(year).add(period);					// TTN-1858 - week 53 support
 				} else 	if (readOnlyYears.contains(year)) {	 
 					// Lock any period where the year is read-only (TTN-1860)
 					lockedPeriodMap.get(year).add(period);
 					lockedTimeHorizPeriods.add(timeHorizCoord);
 				}
 			}
 		}
 		lockedTimeHorizPeriods.addAll(invalidTimeHorizPeriods);
 		lockedTimeSlices.addAll(invalidTimeSlices);
 	
 		// Store client state collections
 		clientState.setLockedTimeSlices(lockedTimeSlices);
 		clientState.setInvalidTimeSlices(invalidTimeSlices);
 		clientState.setLockedPeriodMap(lockedPeriodMap);
 		clientState.setInvalidPeriodMap(invalidPeriodMap); 					// TTN-1858 - week 53 support
 		clientState.setLockedTimeHorizonPeriods(lockedTimeHorizPeriods);
 		clientState.setLockedYears(readOnlyYears);							// TTN-1860 - non-plannable year support
 		clientState.setInvalidTimeHorizonPeriods(invalidTimeHorizPeriods);
 
 		
 		// Populate first plan period properties. Find the first unlocked floor
 		// member of the time horizon tree. (TTN-1882)
 		String firstPlanPeriod = "", firstTimeHorizPlanPeriod = "";
 		List<String> timeHorizPeriods = timeHorizTree.getLowestMemberNames();
 		timeHorizPeriods.removeAll(lockedTimeHorizPeriods);
 		if (!timeHorizPeriods.isEmpty()) {
 			firstTimeHorizPlanPeriod = timeHorizPeriods.get(0);
 			TimeSlice ts = new TimeSlice(firstTimeHorizPlanPeriod);
 			firstPlanPeriod = ts.getPeriod();		
 		}
 		clientState.setFirstPlanPeriod(firstPlanPeriod);
		clientState.setFirstPlanTimeHorizonPeriod(firstTimeHorizPlanPeriod);
 	}
 
 
 	/**
      * Returns the set of locked level 0 and upper-level periods in the 
      * time tree.
      * 
      * @param clientState Client state
      * 
      * @return The set of locked level 0 and upper-level periods in the time
 	 * @throws PafException 
      */
     public Set<String> getLockedList(PafClientState clientState) throws PafException {
 
     	final PafApplicationDef pafApp = clientState.getApp();
     	final String lastPeriodParm = pafApp.getLastPeriod();
     	final String currentYearParm = pafApp.getCurrentYear();
 		final String timeDim = pafApp.getMdbDef().getTimeDim(), yearDim = pafApp.getMdbDef().getYearDim();
     	final PafDimTree timeHorizonTree = clientState.getTimeHorizonTree();
        	final PafDimTree timeTree = clientState.getUowTrees().getTree(timeDim);
        	final PafDimTree yearTree = clientState.getUowTrees().getTree(yearDim);
        	final PafDimMember timeRootNode = timeTree.getRootNode();
        	final PafDimTree mdbTimeTree = dataService.getDimTree(timeDim);
        	final PafDimTree mdbYearTree = dataService.getDimTree(yearDim);
        	final PafDimMember lastTimeHorizonPeriod = timeHorizonTree.getLastFloorMbr();
 
     	Set<String> lockedPeriods = new HashSet<String>();
     	String elapsedPeriod = null;
 
 
     	// Resolve the lastPeriod & currentYear parameters, preferably, against
     	// the current time horizon tree. 
     	// 
     	// This code has been updated to support alternate time hierarchies and
     	// a last period setting that is above or below the uow floor. (TTN-1686)
     	PafDimMember elapsedPeriodMember;
     	ExitDo:
     	do {
 			elapsedPeriod = TimeSlice.buildTimeHorizonCoord(lastPeriodParm, currentYearParm);
 			elapsedPeriodMember = null;
 			if (timeHorizonTree.hasMember(elapsedPeriod)) {
 				// The derived elapsed period does exist in the uow. Ensure that it
 				// corresponds to a member at the time horizon floor, by using its
 				// last descendant.
 				elapsedPeriodMember = timeHorizonTree.getLastDescendant(elapsedPeriod, (short) 0);
 				break;
 			}
 			
 			// The derived elapsed period is outside uow. At a minimum, the 
 			// current year and the last period need to exist in their
 			// respective mdb trees. If not, then we  can't resolve the 
 			// elapsed period parameters.
 			if (!mdbYearTree.hasMember(currentYearParm) || !mdbTimeTree.hasMember(lastPeriodParm)) break;
 			
 
 			// Now comes the hard part. There are a few scenarios that need to
 			// be tested, complicated by the need to support shared hierarchies.
 			
 			// Scenario 1 - the current year falls outside the UOW. (TTN-1709)
 			if (!yearTree.hasMember(currentYearParm)) {
 				String firstUowYear = yearTree.getFirstFloorMbr().getKey();
 				int firstYearInx = mdbYearTree.getMemberSortIndex(firstUowYear);
 				int currentYearInx = mdbYearTree.getMemberSortIndex(currentYearParm);
 				if (currentYearInx < firstYearInx) {
 					// Current year occurs before the first year in the UOW -
 					// don't lock any periods.
 					return lockedPeriods;
 				} else {
 					// Current year points to a future year - the entire uow should
 					// be locked.
 					elapsedPeriodMember = lastTimeHorizonPeriod;
 					break;
 				}				
 			}
 
 			// Scenario 2 - in the mdb time tree, the last period is a 
 			// descendant of one of the uow time tree floor members.
 			List<String> timeFloorMbrs = PafDimTree.getMemberNames(timeTree.getLowestMembers(timeTree.getRootNode().getKey()));
 			for (String timeFloorMbr : timeFloorMbrs) {
 				List<String> floorDescendants = PafDimTree.getMemberNames(mdbTimeTree.getDescendants(timeFloorMbr));
 				if (floorDescendants.contains(lastPeriodParm)) {
 					// The last period was found in one of the descendants of the
 					// current time floor member. Validate that its valid with
 					// the current year.
 					elapsedPeriod = TimeSlice.buildTimeHorizonCoord(timeFloorMbr, currentYearParm);
 					if (timeHorizonTree.hasMember(elapsedPeriod)) {
 						// Valid time horizon member
 						elapsedPeriodMember = timeHorizonTree.getMember(elapsedPeriod);
 						break ExitDo;
 					} else {
 						// Can't be resolved
 						break ExitDo;
 					}
 				}
 			}
 
 			// Scenario 3 - the last period is part of a sibling branch, outside the uow,
 			// and corresponds to a time period that comes before or after the current
 			// time horizon. The trick will be to find the sibling time hierarchy branch
 			// that is related to this uow. Because of alternate time hierarchies, this
 			// is not a trivial task.
 			
 			// Attempt to build a tree that contains the current uow time members plus all
 			// the related time members outside this uow. If this tree doesn't contain a
 			// member corresponding to the last period parm, then the elapsed time period
 			// can't be resolved.
 			PafDimTree fullYearTree = mdbTimeTree.getHighestUniqueSubTreeCopy(timeRootNode.getKey());
 			if (!fullYearTree.hasMember(lastPeriodParm)) break;
 			
 			// The last period member has finally been found. Now determine 
 			// if it comes before or after the current uow's time horizon.
 			PafDimMember firstUowTimeMbr = timeTree.getFirstFloorMbr();
 			int firstUowTimeMbrInx = fullYearTree.getMemberSortIndex(firstUowTimeMbr.getKey());
 			int lastPeriodMbrInx = fullYearTree.getMemberSortIndex(lastPeriodParm);
 			if (lastPeriodMbrInx < firstUowTimeMbrInx) {
 				// Last period points to a period that occurs before the start of
 				// the uow - don't lock any periods.
 				return lockedPeriods;
 			} else {
 				// Last period points to a future period - this entire uow should
 				// be locked.
 				elapsedPeriodMember = lastTimeHorizonPeriod;
 				break;
 			}
 
 		} while (false);
     	
  
     	// Throw error if elapsed period can't be resolved (TTN-1686)
     	if (elapsedPeriodMember == null){
     		String errMsg = String.format(Messages.getString("PafAppService.27"), //$NON-NLS-1$
     				currentYearParm, lastPeriodParm); 
     		logger.fatal(errMsg);
     		throw new IllegalArgumentException(errMsg);
     	}
 
 
     	// Traverse all time tree descendant members (parent first) until the elapsed
     	// period member is reached. All members up through the current elapsed period
     	// will initially be considered locked.
     	List<PafDimMember> members = timeHorizonTree.getIDescendants();
     	for (PafDimMember member : members) {
 
     		logger.debug(Messages.getString("PafAppService.2") + member.getKey() + Messages.getString("PafAppService.3") //$NON-NLS-1$ //$NON-NLS-2$
     				+ (member.getParent() != null ? member.getParent().getKey() : "[NONE]"));
 
     		lockedPeriods.add(member.getKey());
 
     		// if elapsed period is found, break out of loop
     		if (member.getKey().equals(elapsedPeriodMember.getKey())) {
     			break;
     		}
     	}
 
 
     	// Resolve locked status of upper level locked time periods
     	resolveLockedMember(elapsedPeriodMember, lockedPeriods);
 
 		return lockedPeriods;
 	}
 
 	/**
 	 * Get parent of member, check to see if all children of parent are listed
 	 * in lockedTimes, if not, remove parent member from Set. Recursively call
 	 * until top of tree (parent == null ).
 	 * 
 	 * @param current
 	 *            tree member
 	 */
 	private void resolveLockedMember(PafDimMember member, Set<String> lockedPeriods) {
 
 		PafDimMember parent = member.getParent();
 		List<PafDimMember> children = parent.getChildren();
 
 		if (children != null) {
 			for (PafDimMember child : children) {
 
 				if (!lockedPeriods.contains(child.getKey())) {
 					lockedPeriods.remove(parent.getKey());
 					break;
 				}
 			}
 		}
 
 		if (parent.getParent() != null) {
 			resolveLockedMember(parent, lockedPeriods);
 		}
 
 	}
 
 }
