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
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.*;
 import com.pace.base.app.*;
 import com.pace.base.comm.ApplicationState;
 import com.pace.base.comm.CustomMenuDef;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.db.membertags.MemberTagDef;
 import com.pace.base.mdb.PafBaseTree;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree.LevelGenType;
 import com.pace.base.project.ProjectElementId;
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
 
 	private static Map<String, PafApplicationDef> applicationDefs = new HashMap<String, PafApplicationDef>();
 	private static Map<String, ApplicationState> applicationStates = new HashMap<String, ApplicationState>();
         
 	//handles to the other main services
 	PafViewService viewService = PafViewService.getInstance();
 	PafDataService dataService = PafDataService.getInstance();
 	
 	
 	private static Logger logger = Logger.getLogger(PafAppService.class);
 	private static Logger logPerf = Logger.getLogger("pace.performance");
 
 	private PafAppService() { }
 
 	public static PafAppService getInstance() {
 		if (_instance == null)
 			_instance = new PafAppService();
 		return _instance;
 	}
     
     /**
      * Load key meta-data to each pace application
      */
     // TODO make measure and version loads driven by application
     // currently just loads all apps with the current measure/version defs
     public synchronized void loadApplicationConfigurations() {
 
 
         applicationDefs.clear();
         
 		// update the pace project object
 		PafMetaData.updateApplicationConfig();
 				
         List<PafApplicationDef> appDefs = PafMetaData.getPaceProject().getApplicationDefinitions();
                         
         for (PafApplicationDef app : appDefs) {
         	
             // Initialize an application state objected (defaults to stopped)
             ApplicationState as = new ApplicationState(app.getAppId());
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
 			String s = String.format("Failed to start application [%s]", id);
 			throw new PafException(s, PafErrSeverity.Error, e);
 		}
     	
 		String stepDesc = String.format("Application [%s] Loaded", id);
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
     		String s = String.format("No application with ID [%s] defined", id);
     		throw new IllegalArgumentException(s);
     	} else {
     		applicationStates.get(id).setCurrentRunState(state);
     	}
     }
     
     public synchronized RunningState getAppRunState(String id) {
     	if ( ! applicationStates.containsKey(id) ) {
     		String s = String.format("No application with ID [%s] defined", id);
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
  
 
 	/**
 	 * Create a map of elapsed periods by year
 	 * 
 	 * @param clientState Client State
 	 * @return Map of elapsed periods by year
 	 */
 	public Map<String, Set<String>> getLockedPeriodMap(PafClientState clientState) {
 		
 		Map<String, Set<String>> lockedPeriodMap = new HashMap<String, Set<String>>();
 		UnitOfWork uow = clientState.getUnitOfWork();
 		boolean isCalcElapsedPeriods = clientState.getPlannerConfig().isCalcElapsedPeriods();
 		PafApplicationDef pafApp = clientState.getApp(); 
 		String elapsedYear = pafApp.getCurrentYear();
 		String timeDim = pafApp.getMdbDef().getTimeDim();
 		String yearDim = pafApp.getMdbDef().getYearDim();
 		
 		
 		// Initialize map
 		String[] years = uow.getDimMembers(yearDim);
 			for (String year : years) {
 			lockedPeriodMap.put(year, new HashSet<String>());
 		}
 		
 		// Set the locked periods by year. This step will be skipped if
 		// elapsed periods are to be calculated.
 		if (!isCalcElapsedPeriods) {
 			
 			String currentVersion = clientState.getPlanningVersion().getName();
 			Map<String, VersionType> versionsTypeMap = PafMetaData.getVersionTypeMap();
 			VersionType versionType = versionsTypeMap.get(currentVersion);
 			boolean isYearElapsed = false;
 			
 			// Processing years in backwards order makes it easier to determine 
 			// which years are elapsed.
 			for (int i = years.length -1; i >=0; i--) {
 				String year = years[i];
 				if (year.equalsIgnoreCase(elapsedYear)) {
 					isYearElapsed = true;
 				}
 				if (versionType.equals(VersionType.ForwardPlannable)) {
 					if (isYearElapsed) {
 						Set<String> lockedPeriods = new HashSet<String>();
 						if (year.equalsIgnoreCase(elapsedYear)) {
 							// Current year - add only elapsed periods
 							lockedPeriods.addAll(PafAppService.getInstance().getLockedList(clientState));
 						} else {
 							// Historical year - mark all periods as locked
 							lockedPeriods.addAll(Arrays.asList(uow.getDimMembers(timeDim)));
 						}
 						lockedPeriodMap.put(year, lockedPeriods);
 					}
 				
 				}
 			}
 		}
 		return lockedPeriodMap;
 	}
     
     public Set<String> getLockedList(PafClientState clientState) {
 
 		Set<String> lockedPeriods = clientState.getLockedPeriods();
 
 		if (lockedPeriods == null) {
 			
 			lockedPeriods = new HashSet<String>();
 
 			PafApplicationDef pafApp = clientState.getApp();
 
 			String elapsedTimeMember = pafApp.getLastPeriod();
 
 			PafBaseTree periodTree = PafDataService.getInstance().getBaseTree(pafApp.getMdbDef().getTimeDim());
 
 			List<PafDimMember> members = periodTree.getDescendants(periodTree
 					.getRootNode().getKey(), LevelGenType.LEVEL, 0);
 
 			PafDimMember elapsedMember = null;
 
 			// add all members to the Set until a match is found on Elapsed
 			// Period
 			for (PafDimMember member : members) {
 
 				logger.info(Messages.getString("PafAppService.2") + member.getKey() + Messages.getString("PafAppService.3") //$NON-NLS-1$ //$NON-NLS-2$
 						+ member.getParent().getKey());
 
 				lockedPeriods.add(member.getKey());
 
 				// if elapsed time is found, break out of loop
 				if (member.getKey().equals(elapsedTimeMember)) {
 					elapsedMember = member;
 					break;
 				}
 			}
 
 			resolveLockedMember(elapsedMember, lockedPeriods);
 
 			clientState.setLockedPeriods(lockedPeriods);
 
 			logger.info(Messages.getString("PafAppService.4") + lockedPeriods); //$NON-NLS-1$
 			
 		}
 		
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
 	private void resolveLockedMember(PafDimMember member,
 			Set<String> lockedPeriods) {
 
 		PafDimMember parent = member.getParent();
 
 		if (parent.getParent() != null) {
 
 			List<PafDimMember> children = parent.getChildren();
 
 			if (children != null) {
 				for (PafDimMember child : children) {
 
 					if (!lockedPeriods.contains(child.getKey())) {
 						lockedPeriods.remove(parent.getKey());
 						break;
 					}
 				}
 			}
 
 			resolveLockedMember(parent, lockedPeriods);
 
 		}
 
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
 	 *  Get all or specified member tag defintions
 	 *
 	 * @param appId Application Id
 	 * @param memberTagNames Optional member tag filter
 	 * 
 	 * @return MemberTagDef
 	 */
 	public MemberTagDef[] getMemberTagDefs(String appId, String[] memberTagNames) {
 
 		MemberTagDef[] memberTagDefs = null;
 		PafApplicationDef appDef = getApplication(appId);
 		
 		
 		// Get member tag defintions
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
 		
 		// Return member tag defintions
 		return memberTagDefs;
 	}      
 
 }
