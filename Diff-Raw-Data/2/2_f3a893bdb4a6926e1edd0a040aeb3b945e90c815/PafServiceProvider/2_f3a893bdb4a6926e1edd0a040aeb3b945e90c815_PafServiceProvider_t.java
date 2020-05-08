 /*
  *  File: @(#)PafServiceProvider.java   Package: com.pace.base.server   Project: PafServer
  *  Created: Sep 6, 2005        		By: JWatkins
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
 import java.rmi.RemoteException;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.annotation.Resource;
 import javax.jws.WebService;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.NDC;
 
 import com.pace.base.*;
 import com.pace.base.app.*;
 import com.pace.base.comm.*;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.PafDataSlice;
 import com.pace.base.db.SecurityGroup;
 import com.pace.base.db.cellnotes.CellNote;
 import com.pace.base.db.cellnotes.CellNotesInformation;
 import com.pace.base.db.cellnotes.SimpleCellNote;
 import com.pace.base.db.membertags.MemberTagDef;
 import com.pace.base.db.membertags.SimpleMemberTagData;
 import com.pace.base.mdb.*;
 import com.pace.base.rules.RuleGroup;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.AESEncryptionUtil;
 import com.pace.base.utility.CompressionUtil;
 import com.pace.base.utility.DomainNameParser;
 import com.pace.base.utility.Odometer;
 import com.pace.base.view.PafMVS;
 import com.pace.base.view.PafStyle;
 import com.pace.base.view.PafView;
 import com.pace.base.view.PafViewSection;
 import com.pace.server.comm.*;
 
 /**
  * 
  * 
  *
  * @author Jwatkins
  * @version	x.xx
  *
  */
 
 @WebService(endpointInterface="com.pace.server.IPafService")
 
 public class PafServiceProvider implements IPafService {
 
 	// injected handle to the web service context
 	@Resource
 	WebServiceContext wsCtx;	
 	
 	private PafViewService viewService;
 	private PafDataService dataService;
 	private PafAppService appService;
 	private static String serverPlatform = null;
 	private static Logger logger = Logger.getLogger(PafServiceProvider.class);
 	private static Logger logAudit = Logger.getLogger("pace.audit");
 	private static ConcurrentHashMap<String, PafClientState> clients = new ConcurrentHashMap<String, PafClientState>();
 
 	public PafServiceProvider() {
 		// get handles to singleton implementors
 		if (serverPlatform == null) {
 
 			serverPlatform = System.getProperty(Messages.getString("PafServiceProvider.0")) + Messages.getString("PafServiceProvider.1") //$NON-NLS-1$ //$NON-NLS-2$
 			+ System.getProperty(Messages.getString("PafServiceProvider.2")) + Messages.getString("PafServiceProvider.3") //$NON-NLS-1$ //$NON-NLS-2$
 			+ System.getProperty(Messages.getString("PafServiceProvider.4")) + Messages.getString("PafServiceProvider.5") //$NON-NLS-1$ //$NON-NLS-2$
 			+ System.getProperty(Messages.getString("PafServiceProvider.6")); //$NON-NLS-1$
 		}
 
 		try {
 
 			viewService = PafViewService.getInstance();
 			dataService = PafDataService.getInstance();
 			appService = PafAppService.getInstance();
 
 			System.out.println(Messages.getString("PafServiceProvider.7") + logger.getLevel()); //$NON-NLS-1$
 			logger.info(Messages.getString("PafServiceProvider.8")); //$NON-NLS-1$
 
 		} catch (Exception ex) {
 
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 
 		}
 	}
 
 	
 	/**
 	 *	Export all view definitions to xml
 	 *
 	 */
 	public void saveViewCache() {
 		viewService.saveViewCache();
 	}
 
 	/**
 	 *	Get paf server version
 	 *
 	 * @return Version ID
 	 */
 	public String getVersion() {
 		return (PafServerConstants.SERVER_VERSION);
 	}
 
 	/**
 	 *	Get Client View
 	 *
 	 * @param viewRequest View request
 	 * 
 	 * @return PafView
 	 * @throws PafSoapException
 	 */
 	public PafView getView(ViewRequest viewRequest) throws PafSoapException {
 
 		PafView pf = null, compressedView = null;
 		String clientId = viewRequest.getClientId();
 		
 		try {
 
 			// Set logger client info property to user name
 			pushToNDCStack(viewRequest.getClientId());
 			
 			// Troubleshoot load balancer cookies
 			listCookies(clientId);
 			
 			// Verify client id is good
 			if ( ! clients.containsKey( viewRequest.getClientId() ) ) {
 				logger.error("ClientID not found: " + viewRequest.getClientId());
 				throw new PafSoapException(new PafException(Messages.getString("PafServiceProvider.InvalidClientIdReInit"), PafErrSeverity.Error));			
 			}		
 			
 			
 			// Get client state
 			PafClientState cs = clients.get(viewRequest.getClientId());
 			
 			if (cs == null) {
 				throw new PafException(Messages.getString("PafServiceProvider.9"), //$NON-NLS-1$
 						PafErrSeverity.Fatal);
 			}
 
 			logger.info(Messages.getString("PafServiceProvider.10") + viewRequest.getViewName()); //$NON-NLS-1$
 
 			// Get view
 			pf = viewService.getView(viewRequest, cs);
 			
 			// Add user selections to the view
 			pf.setUserSelections(viewRequest.getUserSelections());
 			
 			// Add uncompressed view to client state
 			cs.addView(pf);
 			
 			// Create a clone of the view so that it can be compressed
 			// without compromising the state of the server copy of the 
 			// view.
 			try {
 //				long stepTime = System.currentTimeMillis();
 				compressedView = pf.clone();
 //				logger.info(LogUtil.timedStep("View Cloned...", stepTime));  
 			} catch (CloneNotSupportedException ex) {
 				String errMsg = "Error encountered while cloning view";
 				logger.error(errMsg);
 				throw new PafException(errMsg, PafErrSeverity.Error);
 			}
 	
 			// Compress each cloned view section
 			if(viewRequest.isCompressResponse() == true){
 				for(PafViewSection viewSection : compressedView.getViewSections()){
 					try {
 						viewSection.compressData();
 					} catch (IOException e) {
 						String errMsg = "Error encountered while compressing view section";
 						logger.error(errMsg);
 						throw new PafException(errMsg, PafErrSeverity.Error);
 					}
 				}
 			}
 			
 						
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 			
 			PafErrHandler.handleException(pex);
 			
 			throw pex.getPafSoapException();
 			
 		} finally {
 
 			popFromNDCStack(viewRequest.getClientId());
 
 		}
 
 		// Return compressed view
 		return compressedView;
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 */
 	public PafResponse refreshMetaDataCache() {
 		logger.info(Messages.getString("PafServiceProvider.11")); //$NON-NLS-1$
 		dataService.clearMemberTreeStore();
 		return new PafResponse();
 	}
 
 	
 	public PafResponse reinitializeClientState(PafRequest cmdRequest) throws RemoteException, PafSoapException {
 
 		// Set logger client info property to user name
 		try {
 			pushToNDCStack(cmdRequest.getClientId());
 			reinitializeClientState(cmdRequest.getClientId());
 			//Reset the client state to the state it is in after ClientInit
 			//This ensures that a user can cleanly change roles 
 
 			
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 
 		} finally {
 
 			popFromNDCStack(cmdRequest.getClientId());
 					
 		}	
 		return new PafResponse();	
 		
 	}
 	
 	private void reinitializeClientState(String clientId) {
 
 		ClientInitRequest pcInit = clients.get(clientId).getInitRequest();
 		PafApplicationDef paDef = clients.get(clientId).getApp();
 		PafSecurityToken pToken = clients.get(clientId).getSecurityToken();
 		
 		PafClientState state = new PafClientState(clientId, pcInit, PafMetaData.getPaceHome(), PafMetaData.getTransferDirPath(), PafMetaData.isDebugMode());
 		state.setApp(paDef);
 		state.setSecurityToken(pToken);
 		
 		clients.put(clientId, state);
 	}
 	
 
 	/**
 	 * Begin initial tracking of a client upon receiving this request generates
 	 * a unique id, but is not actually authenticated to perform any operations
 	 * 
 	 * @param pcInit
 	 *            Client init request
 	 * 
 	 * @return PafServerAck
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	public PafServerAck clientInit(ClientInitRequest pcInit)
 			throws RemoteException, PafSoapException {
 
 		PafServerAck ack = null;
 	
 
 		try {
 			
 			String clientId = String.valueOf(Math.random());
 			
 			// Display client initialization message (TTN-
 			char[] banner = new char[90];
 			Arrays.fill (banner, '*');
 			logger.info(String.valueOf(banner));
 			logger.info(Messages.getString("PafServiceProvider.13") //$NON-NLS-1$
 					+ Messages.getString("PafServiceProvider.15") + pcInit.getIpAddress()); //$NON-NLS-1$
 			logger.info(Messages.getString("PafServiceProvider.19") + pcInit.getClientType()); //$NON-NLS-1$
 			logger.info(Messages.getString("PafServiceProvider.20") + pcInit.getClientVersion()); //$NON-NLS-1$
 			logger.info(String.valueOf(banner));
 			logger.info(""); //$NON-NLS-1$
 
 						
 			// block to debug load balancer cookies.
 			listCookies(clientId);
 			
 			// validate client version
 			if (!appService.isValidClient(pcInit.getClientVersion(), pcInit.getClientType())) {
 				// setup response with version mismatch
 				ack = new PafServerAck(null, PafServiceProvider.serverPlatform,
 						this.getVersion());
 				ack.setClientUpgradeRequired(true);
 				ack.setClientUpgradeUrl(PafMetaData.getServerSettings().getClientUpdateUrl());
 				return ack;
 			}
 			
 			// continue normal setup
 			PafClientState state = new PafClientState(clientId, pcInit, PafMetaData.getPaceHome(), PafMetaData.getTransferDirPath(), PafMetaData.isDebugMode());
 
 			clients.put(clientId, state);
 			
 			PafApplicationDef pafApplicationDef = appService.getApplications().get(0);
 			
 			if ( pafApplicationDef == null ) {
 				ack = new PafServerAck(clientId, PafServiceProvider.serverPlatform,
 						this.getVersion());
 			} else {
 				ack = new PafServerAck(clientId, PafServiceProvider.serverPlatform,
 						this.getVersion(), pafApplicationDef.getAppId(), pafApplicationDef.getMdbDef().getDataSourceId());
 			}
 			
 			//get if server password reset is enabled
 			Boolean serverPasswordResetEnabled = PafMetaData.getServerSettings().getEnableServerPasswordReset();
 			
 			if ( serverPasswordResetEnabled != null) {
 				
 				ack.setServerPasswordResetEnabled(serverPasswordResetEnabled);
 				
 			}
 
 			//get if client password reset is enabled
 			Boolean clientPasswordResetEnabled = PafMetaData.getServerSettings().getEnableClientPasswordReset();
 
 			//if server reset is not enabled, don't allow client to be enabled.
 			if ( serverPasswordResetEnabled != null && ! serverPasswordResetEnabled ) {
 				
 				ack.setClientPasswordResetEnabled(false);
 				
 			} else if ( clientPasswordResetEnabled != null) {
 				
 				ack.setClientPasswordResetEnabled(clientPasswordResetEnabled);
 				
 			}
 			
 			//get min password length
 			int minPasswordLength = PafMetaData.getServerSettings().getMinPasswordLength();
 
 			//get max password length			
 			int maxPasswordLength = PafMetaData.getServerSettings().getMaxPasswordLength();
 			
 			//if max password length is less than the min, set defaults
 			if ( maxPasswordLength < minPasswordLength) {
 				
 				logger.error("Max password length can't be less than min password length.  Default lengths will be used.  Configure in " + PafBaseConstants.FN_ServerSettings + "."); //$NON-NLS-1$ //$NON-NLS-2$
 				
 				minPasswordLength = PafBaseConstants.DEFAULT_PASSWORD_MIN_LENGTH;
 				maxPasswordLength = PafBaseConstants.DEFAULT_PASSWORD_MAX_LENGTH;
 				
 			}
 			
 			ack.setMinPassowordLength(minPasswordLength);
 
 			ack.setMaxPassowordLength(maxPasswordLength);
 			
 			ack.setAuthMode(PafMetaData.getServerSettings().getAuthModeAsEnum());
 			
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		}
 
 		return ack;
 	}
 
 	/**
 	 *	Perform client authorization
 	 *
 	 * @param authReq Authorization request
 	 * 
 	 * @return PafAuthResponse
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	public PafAuthResponse clientAuth(PafAuthRequest authReq)
 			throws RemoteException, PafSoapException{
 
 		PafAuthResponse response = null;
 		String clientId = authReq.getClientId();
 
 		try {
 			// Set logger client info property to user name
 			pushToNDCStack(clientId);
 			
 			// block to debug load balancer cookies.
 			listCookies(clientId);
 			
 			// Verify client id is good
 			if ( ! clients.containsKey( clientId ) ) {
 				logger.error("ClientID not found: " + clientId);
 				throw new PafSoapException(new PafException(Messages.getString("PafServiceProvider.InvalidClientIdReInit"), PafErrSeverity.Error));			
 			}				
 
 			//Reset the client state to the state it is in after ClientInit
 			//This ensures that a user can cleanly change roles
 			reinitializeClientState(authReq.getClientId());
 
 			// Added trim to fix //TTN-1373
 			String userName = authReq.getUsername().trim();
 			String password = authReq.getPassword().trim();
 			String sid = authReq.getSid();
 			String domain;
 			
 			PafLDAPSettings ldapSettings = PafMetaData.getServerSettings().getLdapSettings();
 
 			if (ldapSettings.getNetBiosNames().containsKey(authReq.getDomain().toLowerCase())){
 				domain = ldapSettings.getNetBiosNames().get(authReq.getDomain().toLowerCase());
 			}else{
 				domain = authReq.getDomain();
 			}
 			
 			// Validate user credentials
 			response = new PafAuthResponse();
 			PafSecurityToken token = new PafSecurityToken(userName, "", false); //$NON-NLS-1$
 			PafUserDef pafUserDef = null;
 			boolean authenticated = false;
 
 			try {
 				//Try Native auth first
 				if (sid == null){
 					pafUserDef = PafSecurityService.authenticate(userName, password);				
 					if ( pafUserDef != null ) {						
 						authenticated = true;						
 						response.setAdmin(pafUserDef.getAdmin());						
 						response.setChangePassword(pafUserDef.getChangePassword());						
 						token.setAdmin(PafSecurityService.getUser(userName).getAdmin());						
 						clients.get(authReq.getClientId()).setUserDef(pafUserDef);
 					}	
 				}				
 				
 				// Try LDAP authorization - if still not authenticated and mixed mode is enabled
 				if(pafUserDef == null && PafMetaData.getServerSettings().getAuthMode().equalsIgnoreCase(AuthMode.mixedMode.toString())){
 					LDAPAuthentication ldapAuth = new LDAPAuthentication();
 					PafUserDef userDef = null;
 					
 					if( sid != null){
 						userDef = ldapAuth.authenticateUser2(PafMetaData.getServerSettings(), userName, sid);
 						if(userDef != null){
 							userName = userDef.getUserName();
 							token.setUserName(userName);
 							authenticated = true;
 						}
 					}
 					else{
 						userDef = ldapAuth.authenticateUser(PafMetaData.getServerSettings(), userName, password, domain);
 						if(userDef != null){
 							userName = userDef.getUserName();
 							token.setUserName(userName);
 							authenticated = true;	
 						}
 					}
 					
 					if (authenticated == true){
 						response.setAdmin(false);						
 						response.setChangePassword(false);						
 						token.setDomain(userDef.getDomain());
 						token.setAdmin(false);						
 						clients.get(authReq.getClientId()).setUserDef(userDef);
 					}
 				}
 			
 			} catch (PafInvalidLogonInformation invalidLogonExc) {
 				String errorMessage = "Invalid logon information."; //$NON-NLS-1$
 				logger.error(errorMessage);
 				token.setSessionToken(errorMessage);				
 			} catch (InvalidUserNameException userNameExc) {
 				String errorMessage = Messages.getString("PafServiceProvider.16") //$NON-NLS-1$
 						+ userName + Messages.getString("PafServiceProvider.17"); //$NON-NLS-1$
 				logger.error(errorMessage);
 				token.setSessionToken(errorMessage);				
 			} catch (InvalidPasswordException passwordExc) {
 				String errorMessage = Messages.getString("PafServiceProvider.18") //$NON-NLS-1$
 						+ userName + Messages.getString("PafServiceProvider.SQuotePeriod"); //$NON-NLS-1$
 				logger.error(errorMessage);
 				token.setSessionToken(errorMessage);					
 			} catch (PafException pafExc) {				
 				PafErrHandler.handleException(pafExc);
 			} catch (PafNotAbletoGetLDAPContext ldapExc){
 				ldapExc.printStackTrace();
 			}
 			
 			// Create security token - if authenticated
 			if (authenticated) {
 				token.setSessionToken(String.valueOf(Math.random()));
 				token.setValid(true);
 				
 				// Update client state object
 				clients.get(authReq.getClientId()).setSecurityToken(token);
 
 			}
 			response.setSecurityToken(token);
 
 
 			// Valid login - populate response object
 			if (token.isValid()) {
 
 				popFromNDCStack(authReq.getClientId());
 				pushToNDCStack(authReq.getClientId());
 				String appId = PafAppService.getInstance().getApplications().get(0).getAppId();
 				
 				// Set planner configurations and roles properties
 				PafPlannerRole[] plannerRoles = PafSecurityService.getPlannerRoles(token, appId);
 				response.setPlannerRoles(plannerRoles);				
 				response.setPlannerConfigs(findPafPlannerConfig(plannerRoles ));
 							
 				// Re-load application meta-data if debugMode is set to true (TTN-1367)	
 				if (PafMetaData.isDebugMode()) {			
 					appService.loadApplications();
 				}
 				
 				// Populate client state appDef property
 				clients.get(authReq.getClientId()).setApp(PafAppService.getInstance().getApplications().get(0));
 				
 				// Set application settings - if null, create a new instance
 				AppSettings appSettings = clients.get(authReq.getClientId()).getApp().getAppSettings(); 
 				if ( appSettings == null ) {					
 					appSettings = new AppSettings();					
 				}			
 				//TODO: implement default colors
 				//if app colors is null, create a new AppColor.  This will provide the default colors.
 				/*
 				if ( appSettings.getAppColors() == null ) {
 					
 					appSettings.setAppColors(new AppColors());
 					
 				}
 				*/			
 				//TODO: maybe do this: clients.get(authReq.getClientId()).getApp().setAppSettings(appSettings);
 				response.setAppSettings(appSettings);
 				
 				
 				// Set attribute dimension information
 				int attrDimCount = dataService.getAttributeDimNames().size();
 				AttributeDimInfo[] attrDimInfoArray = new AttributeDimInfo[attrDimCount];
 				int i = 0;
 				for (String attrDimName:dataService.getAttributeDimNames()) {
 					
 					AttributeDimInfo attrDimInfo = new AttributeDimInfo();
 					attrDimInfo.setDimName(attrDimName);
 					
 					PafAttributeTree attrTree = dataService.getAttributeTree(attrDimName);
 					String baseDimName = attrTree.getBaseDimName();
 					attrDimInfo.setBaseDimName(baseDimName);
 					
 					PafBaseTree baseTree = dataService.getBaseTree(baseDimName);
 					
 					// Workaround - returning null from cbrands outline, patch 2.0.2.1
 					if ( baseTree != null ) {					
 						attrDimInfo.setBaseDimMappingLevel(baseTree.getAttributeMappingLevel(attrDimName));						
 					}					
 					attrDimInfoArray[i++] = attrDimInfo;	
 				}
 				
 				response.setAttrDimInfo(attrDimInfoArray);	
 			}
 			
 		} catch (RuntimeException re) {						
 			handleRuntimeException(re);
 		} finally {
 			popFromNDCStack(authReq.getClientId());
 		}
 		
 		// Return user authorization response
 		return response;
 	}
 
 	/**
 	 *	Start Planning Session
 	 *
 	 * @param planRequest Plan session response
 	 * 
 	 * @return PafPlanSessionResponse
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	// TODO Refactor startPlanSession to be cleaner, less arrayList to array
 	// behavior, and wrap some operations
 	@SuppressWarnings({"unchecked" }) //$NON-NLS-1$ //$NON-NLS-2$
 	public PafPlanSessionResponse startPlanSession(PafPlanSessionRequest planRequest) throws RemoteException, PafSoapException {
 
 		PafPlanSessionResponse planResponse = new PafPlanSessionResponse();
 
 		// Get client id
 		String clientId = planRequest.getClientId();
 
 		try {
 
 			// Set logger client id field to user name
 			pushToNDCStack(clientId);
 
 			// get client state for manipulation, and conv references
 			PafClientState clientState = clients.get(clientId);
 
 			// TODO: check if client state got wiped if server restarted
 			PafApplicationDef app = clientState.getApp();
 			String msrDim = app.getMdbDef().getMeasureDim();
 
 			String roleName = planRequest.getSelectedRole();
 			String seasonId = planRequest.getSeasonId();
 
 			// get rich role and season objects for clientState
 			PafPlannerRole plannerRole = PafSecurityService.getPlannerRole(roleName);
 			Season planSeason = app.getSeasonList().getSeasonById(seasonId);
 
 			clientState.setPlannerRole(plannerRole);
 			clientState.setPlanSeason(planSeason);
 						
 			//Begin - TTN 660
 			clientState.setLockedForwardPlannableInterMap(null);
 			clientState.setLockedNotPlannableInterMap(null);
 			//End - TTN 660
 			
 			planResponse.setPlanningVersion(clientState.getPlanningVersion()
 					.getName());
 			
 			
 			//The clientState unit of work may have been set in the populateRoleFilters service call.  If so, skip setting both the clientState
 			//planner config and unit of work.  Just pull the existing settings from the client state.
 			UnitOfWork workUnit = null;
 			PafPlannerConfig pafPlannerConfig;
 			if(clientState.getUnitOfWork() == null){
 				// try to find the paf planner config via role and cycle
 				pafPlannerConfig = findPafPlannerConfig(roleName,
 						planSeason.getPlanCycle());
 
 				// if no paf planner configs exists throw exception
 				if (pafPlannerConfig == null) {
 
 					throw new PafException(
 							Messages.getString("PafServiceProvider.22") + roleName //$NON-NLS-1$
 									+ Messages.getString("PafServiceProvider.23") + planSeason.getPlanCycle() //$NON-NLS-1$
 									+ Messages.getString("PafServiceProvider.SQuotePeriod"), PafErrSeverity.Error); //$NON-NLS-1$
 
 				}
 
 				clientState.setPlannerConfig(pafPlannerConfig);
 
 				// Unit of work is built off of applications mdbdef sequence
 				workUnit = PafSecurityService.getWorkSpec(planRequest.getSelectedRole(), planRequest.getSeasonId(),
 						clientState);
 				
 				//Expand the UOW
 				workUnit = dataService.expandUOW(workUnit, clientState);
 
 				clientState.setUnitOfWork(workUnit);
 			}
 			else{
 				workUnit = clientState.getUnitOfWork();
 				pafPlannerConfig = clientState.getPlannerConfig();
 			}
 			
 			//client state property used in evaluation 
 			clientState.setDataFilteredUow(planRequest.getIsInvalidIntersectionSuppressionSelected());
 			
 			//Determine if UserFilteredUOW is true or false
 			boolean isUserFilteredUow = false;
 			if(pafPlannerConfig.getIsUserFilteredUow()){
 				isUserFilteredUow = true;
 			}
 			else if(clientState.getApp().getAppSettings() != null && clientState.getApp().getAppSettings().isGlobalUserFilteredUow()){
 				isUserFilteredUow = true;
 			}
 			//Set the clientState property 
 			clientState.setUserFilteredUow(isUserFilteredUow);
 			
 			// Get subtrees representative of the hierarchy in the data cache
 			MemberTreeSet treeSet = dataService.getUowCacheTrees(clientState);
 			clientState.setUowTrees(treeSet);
 
 			// Create member index lists on each dimension - used to sort allocation
 			// intersections in evaluation processing (TTN-1391)
 			Map<String, HashMap<String, Integer>> memberIndexLists = 
 				dataService.getUowMemberIndexLists(treeSet);
 			clientState.setMemberIndexLists(memberIndexLists);
 			
 			// Load data cache based on unit of work
 			dataService.loadUowCache(clientState, app, clientState.getUnitOfWork());
 			
 			// calculate dynamic rule sets for the client
 			RuleSet[] fullRuleSets = RuleMngr.getInstance().calculateRuleSets(
 					treeSet, app, pafPlannerConfig);
 
 			Map<String, List<RuleSet>> fullRuleSetMap = new HashMap<String, List<RuleSet>>();
 			List<RuleSet> clientRuleSetList = new ArrayList<RuleSet>();
 			List<RuleSet> ruleSetList = new ArrayList<RuleSet>();
 
 			// 1st remove any rulsesets that the user doesn't have access too
 			// based upon the planner configuration
 
 			for (RuleSet rs : fullRuleSets) {
 				// prune out protProcSkip rulegroups frome measure's ruleset
 
 				if (rs.getDimension().equals(app.getMdbDef().getMeasureDim())) {
 					RuleSet msrCopy = (RuleSet) rs.clone();
 
 					List<RuleGroup> skipGroups = new ArrayList<RuleGroup>();
 					for (RuleGroup rg : msrCopy.getRuleGroups()) {
 						if (rg.isSkipProtProc()) {
 							skipGroups.add(rg);
 						}
 					}
 					for (RuleGroup rg : skipGroups) {
 						msrCopy.removeRuleGroup(rg);
 					}
 					clientRuleSetList.add(msrCopy);
 				} else
 					clientRuleSetList.add(rs);
 
 				if (fullRuleSetMap.containsKey(rs.getDimension()))
 					ruleSetList = fullRuleSetMap.get(rs.getDimension());
 				else
 					ruleSetList = new ArrayList<RuleSet>();
 
 				ruleSetList.add(rs);
 				fullRuleSetMap.put(rs.getDimension(), ruleSetList);
 			}
 
 			clientState.setRuleSets(fullRuleSetMap);
 
 			planResponse.getClientCacheBlock().setRuleSets(
 					clientRuleSetList.toArray(new RuleSet[0]));
 
 			// need to set id of simple tree to root dimension, not default
 			// behavior
 			// key is normally root node. Then store in response object
 			PafSimpleDimTree simpleDimTree = null;
 			PafSimpleDimTree simpleDimTrees[] = new PafSimpleDimTree[treeSet.getTrees().size()];
 
 			// Get simple versions of each client member trees (Regular member trees
 			// can't be passed across the soap layer).
 			int i = 0;
 			for (String memberTreeDim : treeSet.getTreeDimensions()) {
 				// Get simple version of current tree
 				simpleDimTree = treeSet.getTree(memberTreeDim).getSimpleVersion();
 				// Override id of subtree with actual dimension name (required by paf client)
 				simpleDimTree.setId(memberTreeDim);
 				// Add simple tree to collection
 				simpleDimTrees[i++] = simpleDimTree;
 			}
 
 			planResponse.setDimTrees(simpleDimTrees);
 			
 			// compress trees if requested by client
 			if (planRequest.isCompressResponse()) {
 				for ( PafSimpleDimTree t : planResponse.getDimTrees() )
 					t.compressData();
 			}
 			
 			// TODO Refactor a lot of this code into viewService
 
 			// generate the view tree items list from the view tree item names
 			planResponse.setViewTreeItems(generateViewTreeItemsFromViewTreeItemNames(pafPlannerConfig.getViewTreeItemNames()));
 
 			// generate the custom menu def list from
 			planResponse.setCustomMenuDefs(appService
 					.generateCustomMenuDefsFromCustomMenuDefNames(
 							pafPlannerConfig.getMenuItemNames(), clientState
 									.getApp()));
 
 			// set response object attributes for rulesets
 			// if defualts and lists not set populate with all and set default
 			// to 1st ruleset
 			if (pafPlannerConfig.getDefaultRulesetName() == null
 					|| pafPlannerConfig.getDefaultRulesetName().trim().equals(
 							"")) //$NON-NLS-1$
 				planResponse.setDefaultRuleSetName(clientState.getRuleSets()
 						.get(msrDim).get(0).getName());
 			else
 				planResponse.setDefaultRuleSetName(pafPlannerConfig
 						.getDefaultRulesetName());
 
 			if (pafPlannerConfig.getRuleSetNames() == null
 					|| pafPlannerConfig.getRuleSetNames().length < 1) {
 				ArrayList<String> rsNames = new ArrayList<String>();
 				for (RuleSet rs : clientState.getRuleSets().get(msrDim)) {
 					rsNames.add(rs.getName());
 				}
 				planResponse.setRuleSetList(rsNames.toArray(new String[0]));
 			} else {
 				planResponse.setRuleSetList(pafPlannerConfig.getRuleSetNames());
 			}
 
 			// a quick validation check to make sure that the default ruleset
 			// actually exists in the rulesets
 			// available to the user.
 			boolean bValidDflt = false;
 			for (String rsName : planResponse.getRuleSetList()) {
 				if (rsName.trim().equals(planResponse.getDefaultRuleSetName())) {
 					bValidDflt = true;
 					break;
 				}
 			}
 
 			if (!bValidDflt) {
 				throw new PafException(
 						Messages.getString("PafServiceProvider.26") //$NON-NLS-1$
 								+ planResponse.getDefaultRuleSetName() + Messages.getString("PafServiceProvider.27"), //$NON-NLS-1$
 						PafErrSeverity.Error);
 			}
 			
 			// Set current ruleset to default ruleset initially
 			clientState.setCurrentMsrRulesetName(clientState.getDefaultMsrRulesetName() );
 
 			// set view list and related meta-data
 			planResponse.getClientCacheBlock().setAxisSequence(
 					workUnit.getDimensions());
 			planResponse.getClientCacheBlock().setMdbDef(app.getMdbDef());
 			planResponse.getClientCacheBlock().setLastPeriod(
 					app.getLastPeriod());
 			planResponse.getClientCacheBlock().setCurrentYear(
 					app.getCurrentYear());
 
 			// import the global styles from meta-data
 			Map<String, PafStyle> globalStyles = PafMetaData.getPaceProject().getGlobalStyles();
 
 			// create a nulled global styles array
 			PafStyle[] globalStylesAr = null;
 
 			// populate the global styles array
 			int index = 0;
 			if (globalStyles != null) {
 				globalStylesAr = new PafStyle[globalStyles.size()];
 				for (Object key : globalStyles.keySet()) {
 					globalStylesAr[index++] = (PafStyle) globalStyles.get(key);
 				}
 			}
 
 			// set the global styles to the client cache block
 			planResponse.getClientCacheBlock().setGlobalStyles(globalStylesAr);
 
 			// get simplified (SOAP) version of measure definitions for client.
 			// filter definitions to just the measures contained in the unit of work (TTN-1274).
 			List<String> uowMeasures = Arrays.asList(workUnit.getDimMembers(msrDim));
 			Map<String, MeasureDef> measureCat = app.getMeasureDefs(uowMeasures);
 			SimpleMeasureDef[] simpleMeasureCat = new SimpleMeasureDef[measureCat.size()];
 			index = 0;
 			for (MeasureDef meas : measureCat.values()) {
 				simpleMeasureCat[index++] = new SimpleMeasureDef(meas);
 			}
 			planResponse.getClientCacheBlock().setMeasureDefs(simpleMeasureCat);
 			
 			// get simplified (SOAP) version of member tag definitions for client
 			planResponse.getClientCacheBlock().setMemberTagDefs(getMemberTagDefs(app));
 				
 			// get simplified (SOAP) version of version definitions for client
 			planResponse.getClientCacheBlock().setVersionDefs(getSimpleVersionDefs());
 
 			// run the default evaluation process 
 			dataService.evaluateDefaultRuleset(clientState);
 
 			// add the active versions to the client state
 			clientState.setActiveVersions(getActiveVersions(clientState
 					.getPlanningVersion()));
 			
 			
 			//if replicate exists on planner config, use it, else if app settings 
 			//exists, use global, otherwise default to true
 			if ( pafPlannerConfig.getReplicateEnabled() != null ) {
 				
 				planResponse.setReplicateEnabled(pafPlannerConfig.getReplicateEnabled());
 				
 			} else if ( clientState.getApp().getAppSettings() != null  ) {
 				
 				planResponse.setReplicateEnabled(clientState.getApp().getAppSettings().isGlobalReplicateEnabled());
 				
 			}
 
 			//if replicate all exists on planner config, use it, else if app settings 
 			//exists, use global, otherwise default to true
 			if ( pafPlannerConfig.getReplicateAllEnabled() != null ) {
 				
 				planResponse.setReplicateAllEnabled(pafPlannerConfig.getReplicateAllEnabled());
 				
 			} else if ( clientState.getApp().getAppSettings() != null  ) {
 				
 				planResponse.setReplicateAllEnabled(clientState.getApp().getAppSettings().isGlobalReplicateAllEnabled());
 				
 			}
 			
 			//get unit of work
 			UnitOfWork uow = clientState.getUnitOfWork();
 			
 			//only allow one user to create note cache at a time
 			synchronized (PafServiceProvider.class) {
 			
 				//create cell note cache for current clientId
 				CellNoteCacheManager.getInstance().createNoteCache(clientState, uow);
 			
 			}
 						
 			
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} catch (PafException pafException) {
 
 			PafErrHandler.handleException(pafException);
 
 			throw pafException.getPafSoapException();
 
 		} finally {
 
 			// Initialize logger client id field
 			popFromNDCStack(clientId);
 
 		}
 
 		// Return plan session response
 		return planResponse;
 	}
 	
 
 	/**
 	 *	Find the planner configurations for the roles available to a user
 	 *
 	 * @param roleName an array of role names
 	 * 
 	 * @return PafPlannerConfig[]
 	 */
 	private PafPlannerConfig[] findPafPlannerConfig(PafPlannerRole[] plannerRoles){
 		
 		List<PafPlannerConfig> plannerConfigs = new ArrayList<PafPlannerConfig>();
 					
 		//iterate through the PafPlannerRoles
 		for (PafPlannerRole plannerRole : plannerRoles){
 			
 			//iterate through the PafPlannerConfigs
 			for (PafPlannerConfig plannerConfig : PafMetaData.getPaceProject().getRoleConfigurations()) {
 				
 				//build a list of planner configs available to the user
 				if(plannerConfig.getRole().equalsIgnoreCase(plannerRole.getRoleName())){
 					plannerConfigs.add(plannerConfig);
 				}
 				
 				// resolve any global overrides to the planner configuration
 				String appId = PafAppService.getInstance().getApplications().get(0).getAppId();
 				PafAppService.getInstance().resolvePlannerOverrides(plannerConfig, appId);
 			}
 		}
 		
 		return plannerConfigs.toArray(new PafPlannerConfig[0]);
 	}
 
 	/**
 	 *	Find planner configuration for specified role and planCycle
 	 *
 	 * @param roleName Role name
 	 * @param planCycle Plan cycle
 	 * 
 	 * @return PafPlannerConfig
 	 */
 	private PafPlannerConfig findPafPlannerConfig(String roleName,
 			String planCycle) {
 
 		//Get Planner Configs
 		List<PafPlannerConfig> pafPlannerConfigs = PafMetaData.getPaceProject().getRoleConfigurations();
 		
 		// if no paf planner configs exist
 		if (pafPlannerConfigs != null) {
 
 			// define temp paf palnner configs
 			PafPlannerConfig pafPlannerRoleNoCycle = null;
 			PafPlannerConfig pafPlannerCycleNoRole = null;
 			PafPlannerConfig pafPlannerNoRoleNoCycle = null;
 
 			// for each paf planner config
 			for (PafPlannerConfig pafPlannerConfig : pafPlannerConfigs) {
 
 				// resolve any global overrides to the planner configuration
 				String appId = PafAppService.getInstance().getApplications().get(0).getAppId();
 				PafAppService.getInstance().resolvePlannerOverrides(pafPlannerConfig, appId);				
 				
 				// get cycle
 				String cycle = pafPlannerConfig.getCycle();
 
 				// if cycle is not null, check to see if cycle is blank, if so,
 				// null cycle
 				if (cycle != null) {
 
 					if (cycle.trim().length() == 0) {
 						cycle = null;
 					}
 				}
 
 				// if role is not null, check to see if role is blank, if so,
 				// null role
 				String role = pafPlannerConfig.getRole();
 
 				if (role != null) {
 					if (role.trim().length() == 0) {
 						role = null;
 					}
 				}
 
 				// if cycle and role equald planCycle and roleName
 				if (cycle != null && cycle.equals(planCycle) && role != null
 						&& role.equals(roleName)) {
 					return pafPlannerConfig;
 				}
 
 				// Role No Cycle
 				if (pafPlannerRoleNoCycle == null && role != null
 						&& role.equals(roleName) && cycle == null) {
 
 					pafPlannerRoleNoCycle = pafPlannerConfig;
 
 				}
 
 				// Cycle No Role
 				if (pafPlannerCycleNoRole == null && cycle != null
 						&& cycle.equals(planCycle) && role == null) {
 
 					pafPlannerCycleNoRole = pafPlannerConfig;
 
 				}
 
 				// No Role No Cycle
 				if (pafPlannerNoRoleNoCycle == null && cycle == null
 						&& role == null) {
 
 					pafPlannerNoRoleNoCycle = pafPlannerConfig;
 
 				}
 				
 			}
 
 			// return paf planner if one exist
 			if (pafPlannerRoleNoCycle != null) {
 				return pafPlannerRoleNoCycle;
 			} else if (pafPlannerCycleNoRole != null) {
 				return pafPlannerCycleNoRole;
 			} else if (pafPlannerNoRoleNoCycle != null) {
 				return pafPlannerNoRoleNoCycle;
 			}
 
 		}
 
 		return null;
 
 	}
 
 	/**
 	 *	Get all active planning versions
 	 *
 	 * @param baseVersionDef Base version definition
 	 * 
 	 * @return Set<String>
 	 */
 	private Set<String> getActiveVersions(VersionDef baseVersionDef) {
 
 		// create an empty set to hold all the active versions. an active
 		// version is an updatable version that is related to the planning 
 		// version set on the paf apps.
 		Set<String> activeVersions = new HashSet<String>();
 
 		// if the base version def is not non plannable, then add to the set
 		if (!baseVersionDef.getType().equals(VersionType.NonPlannable)) {
 			activeVersions.add(baseVersionDef.getName());
 		}
 	
 		// get the current versions
 		List<VersionDef> versionList = PafMetaData.getPaceProject().getVersions();
 
 		// if versions is not null
 		if (versionList != null) {
 
 			// add any related derived versions (contribution percent, variance, etc.)
 			for (VersionDef version : versionList) {
 
 				// skip to next version, if not a derived version
 				VersionType versionType = version.getType();
 				if (!PafBaseConstants.DERIVED_VERSION_TYPE_LIST.contains(versionType)) {
 					continue;
 				}
 
 				// add this version to active versions set if it's base version 
 				// property equals the active base version
 				String 	baseVersionName = version.getVersionFormula().getBaseVersion();
 				if (baseVersionName.equals(baseVersionDef.getName())) {
 					activeVersions.add(version.getName());
 				}
 			}
 		}
 
 		// return the active versions set
 		return activeVersions;
 	}
 
 	/**
 	 * This method maps the String view or view group name with the actual view
 	 * object.
 	 * 
 	 * @param viewTreeItemNames
 	 *            name of tree view items, could be view names or view group
 	 *            names
 	 * @return PafViewTreeItem[]
 	 */
 
 	private PafViewTreeItem[] generateViewTreeItemsFromViewTreeItemNames(
 			String[] viewTreeItemNames) {
 
 		// create an empty list of view tree items
 		ArrayList<PafViewTreeItem> viewTreeItemsList = new ArrayList<PafViewTreeItem>();
 
 		// if view tree item names array is null, return null
 		if (viewTreeItemNames == null) {
 			return null;
 		}
 
 		// a map to hold all the view's. the key is by view name.
 		Map<String, PafViewTreeItem> allPafViewsMap = new HashMap<String, PafViewTreeItem>();
 
 		// get the available view's from the view cache
 		PafViewTreeItem allPafViewTree[] = viewService.getViewTreeItems();
 
 		// if views exist
 		if (allPafViewTree != null) {
 
 			// populate the all paf views map with all the view cache views.
 			for (PafViewTreeItem view : allPafViewTree) {
 				allPafViewsMap.put(view.getLabel(), view);
 			}
 
 		}
 
 		// get the view groups
 		Map<String, PafViewTreeItem> allPafViewGroupsMap = PafMetaData
 				.getViewGroupsAsViewTreeItemMap();
 
 		// for each view name or view group listed, get the view
 		// from view cache or view group from the view group mapping.
 		for (String viewTreeItemName : viewTreeItemNames) {
 
 			// try to get a paf view tree view from the name of
 			// view or view group
 
 			PafViewTreeItem currentViewEntity = createPafViewTreeItemFromItem(
 					viewTreeItemName, allPafViewsMap, allPafViewGroupsMap);
 
 			// add the entry to the list of views
 			viewTreeItemsList.add(currentViewEntity);
 
 		}
 
 		// convert the list to an array of paf view tree items
 		return viewTreeItemsList.toArray(new PafViewTreeItem[0]);
 	}
 
 	/**
 	 * Find the view entity name by searching the view and view group maps. If
 	 * no view or view group is found, throw a paf exception. if a group is
 	 * found, recursivly try to populate the view group with the views and view
 	 * groups listed inside the view group.
 	 * 
 	 * @param viewTreeItemName
 	 *            name of view or view group to be found
 	 * @param pafViewsMap
 	 *            map of views from view cache
 	 * @param pafViewGroupsMap
 	 *            map of view groups from view groups xml
 	 * @return PafViewTreeView
 	 */
 
 	private PafViewTreeItem createPafViewTreeItemFromItem(
 			String viewTreeItemName, Map<String, PafViewTreeItem> pafViewsMap,
 			Map<String, PafViewTreeItem> pafViewGroupsMap) {
 
 		PafViewTreeItem currentViewEntity = null;
 
 		// get invalid views and error messages from paf view service
 		Map<String, String> invalidViewsMap = PafViewService
 				.getInvalidViewsMap();
 
 		if (invalidViewsMap == null) {
 			invalidViewsMap = new HashMap<String, String>();
 		}
 
 		// if view name is found in view map
 		if (pafViewsMap.containsKey(viewTreeItemName)) {
 			currentViewEntity = pafViewsMap.get(viewTreeItemName);
 
 			// if view name is found in view groups map
 		} else if (pafViewGroupsMap.containsKey(viewTreeItemName)) {
 			currentViewEntity = pafViewGroupsMap.get(viewTreeItemName);
 
 			// if flagged as an invalid view
 		} else if (invalidViewsMap.containsKey(viewTreeItemName)) {
 
 			// create new view entity and set lable to view name
 			currentViewEntity = new PafViewTreeItem();
 			currentViewEntity.setLabel(viewTreeItemName);
 
 			// throw new PafException(invalidViewsMap.get(viewTreeItemName),
 			// PafErrSeverity.Warning);
 
 			// log an error stating the view /view group was not found
 		} else {
 
 			String errorMsg = Messages.getString("PafServiceProvider.29") + viewTreeItemName //$NON-NLS-1$
 					+ Messages.getString("PafServiceProvider.30"); //$NON-NLS-1$
 			logger.error(errorMsg);
 
 			currentViewEntity = new PafViewTreeItem();
 			currentViewEntity.setLabel(viewTreeItemName);
 
 		}
 
 		// if found entity is a view group and has view / view group names
 		if (currentViewEntity.isGroup() && currentViewEntity.getItems() != null) {
 
 			// set up a temp array list of view tree views.
 			ArrayList<PafViewTreeItem> tmpTreeViews = new ArrayList<PafViewTreeItem>();
 
 			// for each item in the view group, populate the temp tree views
 			// with the view entities
 			for (PafViewTreeItem treeView : currentViewEntity.getItems()) {
 
 				// try to add the tree view and it's items to the tmp tree views
 				tmpTreeViews.add(createPafViewTreeItemFromItem(treeView
 						.getLabel(), pafViewsMap, pafViewGroupsMap));
 
 			}
 
 			// if the array list size is greater than 0, set the current view
 			// entity to an array of view tree views.
 			if (tmpTreeViews.size() > 0) {
 				currentViewEntity.setItems(tmpTreeViews
 						.toArray(new PafViewTreeItem[0]));
 			}
 
 		}
 
 		// return populated current view entity, should never be null
 		return currentViewEntity;
 
 	}
 
 	/**
 	 *	Return client cache block
 	 *
 	 * @param cacheRequest
 	 * @return ClientCacheBlock
 	 * 
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafClientCacheBlock clientCacheRequest(
 			PafClientCacheRequest cacheRequest) throws RemoteException,
 			PafSoapException {
 
 		PafClientCacheBlock pcb = new PafClientCacheBlock();
 
 		try {
 
 			// Set logger client info property to user name
 			pushToNDCStack(cacheRequest.getClientId());
 
 			// Get client state
 			PafClientState clientState = clients
 					.get(cacheRequest.getClientId());
 
 			// Set meta-data
 			pcb.setRuleSets(clientState.getRuleSetArray());
 			pcb.setMdbDef(clientState.getMdbDef());
 			pcb.setLastPeriod(clientState.getApp().getLastPeriod());
 			pcb.setLastPeriod(clientState.getApp().getCurrentYear());
 
 			// load axis sequence
 			pcb.setAxisSequence(clientState.getMdbDef().getAxisPriority());
 
 			Map<String, PafStyle> globalStyles = PafMetaData.getPaceProject().getGlobalStyles();
 
 			PafStyle[] globalStylesAr = null;
 
 			if (globalStyles != null) {
 				globalStylesAr = new PafStyle[globalStyles.size()];
 				int index = 0;
 				for (Object key : globalStyles.keySet()) {
 					globalStylesAr[index++] = (PafStyle) globalStyles.get(key);
 				}
 			}
 
 			pcb.setGlobalStyles(globalStylesAr);
 
 			pcb.setVersionDefs(getSimpleVersionDefs());
 
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} finally {
 
 			// Initialize logger client info property
 			popFromNDCStack(cacheRequest.getClientId());
 
 		}
 
 		// Return client cache block
 		return pcb;
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @return
 	 */
 	private SimpleVersionDef[] getSimpleVersionDefs() {
 
 		SimpleVersionDef[] versionAr = null;
 		
 		List<VersionDef> versionList = PafMetaData.getPaceProject().getVersions();
 
 		if (versionList != null) {
 
 			int ndx = 0;
 
 			versionAr = new SimpleVersionDef[versionList.size()];
 
 			for (VersionDef version : versionList) {
 
 				versionAr[ndx++] = new SimpleVersionDef(version);
 
 			}
 
 		}
 
 		return versionAr;
 	}
 
 	/**
 	 *	Evaluate pending calculations on view section
 	 *
 	 * @param evalRequest Evlataion reqest object
 	 * @return PafDataSlice Paf data slice
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	public PafView evaluateView(EvaluateViewRequest evalRequest)
 			throws RemoteException, PafSoapException {
 
 		PafDataSlice dataSlice = null;
 		PafView pView = null;
 		PafView pViewEmpty = null;
 		String clientId = evalRequest.getClientId();
 
 		// Evaluate view
 		try {
 
 			// Set logger client info property to user name
 			pushToNDCStack(evalRequest.getClientId());
 					
 			// Troubleshoot load balancer cookies
 			listCookies(clientId);
 			
 			// Verify client id is good
 			if ( ! clients.containsKey( evalRequest.getClientId() ) ) {
 				logger.error("ClientID not found: " + evalRequest.getClientId());
 				throw new PafSoapException(new PafException(Messages.getString("PafServiceProvider.InvalidClientIdReInit"), PafErrSeverity.Error));			
 			}		
 			
 			// Get client state
 			PafClientState clientState = clients.get(evalRequest.getClientId());
 
 			// update client state with ruleset used in this calculation
 			clientState.setCurrentMsrRulesetName(evalRequest.getRuleSetName());
 			
 			//dataSlice = dataService.evaluateView(evalRequest, clientState);
			if (logAudit.isInfoEnabled() && evalRequest.getChangedCells() != null && evalRequest.getChangedCells().getCompressedData() != null) {
 				logAudit.info(
 						"User: " + clientState.getClientId() + 
 						" changed cells " + evalRequest.getChangedCells().toString());
 			}
 			
 			
 			//Evaluate view
 			PafView currentView = clientState.getView(evalRequest.getViewName());
 			PafMVS pafMVS = clientState.getMVS(PafMVS.generateKey(currentView, currentView.getViewSections()[0]));
 			PafDataCache dsCache = pafMVS.getDataSliceCache();
 			PafDataSliceParms sliceParms = pafMVS.getDataSliceParms();
 			dataService.evaluateView(evalRequest, clientState, dsCache, sliceParms);
 			
 			//Set original user selections
 			PafView view = clientState.getView(evalRequest.getViewName());
 			evalRequest.setUserSelections(view.getUserSelections());
 			evalRequest.setRowsSuppressed(view.getViewSections()[0].getSuppressZeroSettings().getRowsSuppressed());
 			evalRequest.setColumnsSuppressed(view.getViewSections()[0].getSuppressZeroSettings().getColumnsSuppressed());
 			
 			pView = viewService.getView(evalRequest, clientState);
 			
 			if (pView.isDirtyFlag() == false){
 				pViewEmpty = new PafView();
 				pViewEmpty.setDirtyFlag(false);
 				PafViewSection[] pViewSections = new PafViewSection[1];
 				PafViewSection pViewSection = new PafViewSection();
 				pViewSections[0] = pViewSection;
 				pViewEmpty.setViewSections(pViewSections);
 				pViewEmpty.getViewSections()[0].setPafDataSlice(pView.getViewSections()[0].getPafDataSlice());
 			}
 			
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 
 		} finally {
 
 			// pop from stack regardless
 			popFromNDCStack(evalRequest.getClientId());
 
 		}
 		
 		if (pViewEmpty != null){
 			return pViewEmpty;
 		}
 
 		// Return full view
 		return pView;
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param saveWorkRequest
 	 * @return
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	public PafCommandResponse saveWork(SaveWorkRequest saveWorkRequest)
 			throws RemoteException, PafSoapException {
 		PafCommandResponse response = new PafCommandResponse();
 
 		// Set logger client info property to user name
 		pushToNDCStack(saveWorkRequest.getClientId());
 
 		try {
 
 			PafClientState clientState = clients.get(saveWorkRequest.getClientId());
 			dataService.saveDataCache(clientState);
 			response.setErrorCode(0);
 			response.setMessage(Messages.getString("PafServiceProvider.32")); //$NON-NLS-1$
 			response.setSuccessful(true);
 
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 			PafErrHandler.handleException(pex);
 			response.setErrorCode(10000);
 			response.setMessage(Messages.getString("PafServiceProvider.33") + pex.getMessage()); //$NON-NLS-1$
 			response.setSuccessful(false);
 		} finally {
 
 			// pop from stack
 			popFromNDCStack(saveWorkRequest.getClientId());
 
 		}
 
 		// Return command response
 		return response;
 	}
 
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param reloadRequest
 	 * @return
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafDataSlice reloadDatacache(PafViewRequest reloadRequest)
 			throws RemoteException, PafSoapException {
 
 		PafDataSlice dataSlices[] = null;
 
 		try {
 
 			// Set logger client info property to user name
 			pushToNDCStack(reloadRequest.getClientId());
 
 			PafClientState clientState = clients.get(reloadRequest
 					.getClientId());
 
 			// Reload data cache for specified client state
 			dataService.loadUowCache(clientState, clientState.getApp(),
 					clientState.getUnitOfWork());
 
 			// run the default evaluation process 
 			dataService.evaluateDefaultRuleset(clientState);
 
 			// Retrieve the data slice for each view section if a view
 			// is currently open
 			if (reloadRequest.getViewName() == null ||
 					reloadRequest.getViewName().trim().equals("")) { //$NON-NLS-1$
 				return null;
 			}
 			PafView view = clientState.getView(reloadRequest.getViewName());
 						
 			if ( view != null ) {
 									
 				int sectionCount = view.getViewSections().length;;
 				
 				dataSlices = new PafDataSlice[sectionCount];
 				for (int i = 0; i < sectionCount; i++) {
 					dataSlices[i] = dataService.getDataSlice(view, view
 							.getViewSections()[i], clientState, true);
 				}
 			} else {
 				
 				throw (new PafException(Messages.getString("PafServiceProvider.36"), PafErrSeverity.Warning)).getPafSoapException(); //$NON-NLS-1$
 				
 			}
 			
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 
 			PafErrHandler.handleException(pex);
 
 		} finally {
 
 			// pop from stack
 			popFromNDCStack(reloadRequest.getClientId());
 
 		}
 
 		// FIXME - Update to return the entire data slice array
 		return dataSlices[0];
 	}
 	
 	/**
 	 *	Updated selected versions in data cache from the mdb
 	 *
 	 * @param updateRequest Update data cache request
 	 * 
 	 * @return PafDataSlice
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	public PafDataSlice updateDatacache(PafUpdateDatacacheRequest updateRequest) throws RemoteException, PafSoapException {
 		PafDataSlice dataSlices[] = null;
 
 		try {
 
 			// Set logger client info property to user name
 			pushToNDCStack(updateRequest.getClientId());
 
 			// Get client state
 			PafClientState clientState = clients.get(updateRequest.getClientId());
 
 			// Update data cache for specified client state and selected versions
 			@SuppressWarnings("unused")
 			List<String> updatedVersionList = dataService.refreshUowCache(clientState, clientState.getApp(),clientState.getUnitOfWork(), 
 					Arrays.asList(updateRequest.getVersionFilter()));
 
 			// Run the default evaluation process 
 			// -----------------------------------------------------------------------------
 			// (Would like to only run the default eval, if the active plan version has been updated. However,
 			// this could cause problems if there are any crossdim formulas referencing the updated versions)
 			// 
 //			if (updatedVersionList.contains(clientState.getActiveVersions())) {
 				dataService.evaluateDefaultRuleset(clientState);
 //			}
 
 			// Retrieve the data slice for each view section if a view is currently open
 			if (updateRequest.getViewName() == null ||
 					updateRequest.getViewName().trim().equals("")) { //$NON-NLS-1$
 				return null;
 			}
 			PafView view = clientState.getView(updateRequest.getViewName());
 						
 			if ( view != null ) {
 
 				int sectionCount = view.getViewSections().length;;
 
 				dataSlices = new PafDataSlice[sectionCount];
 				for (int i = 0; i < sectionCount; i++) {
 					dataSlices[i] = dataService.getDataSlice(view, view.getViewSections()[i], clientState, true);
 				}
 			} else {
 
 				throw (new PafException(Messages.getString("PafServiceProvider.36"), PafErrSeverity.Warning)).getPafSoapException(); //$NON-NLS-1$
 
 			}
 
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 
 			PafErrHandler.handleException(pex);
 
 		} finally {
 
 			// pop from stack
 			popFromNDCStack(updateRequest.getClientId());
 
 		}
 
 		// FIXME - Update to return the entire data slice array
 		return dataSlices[0];
 	}
 
 	/**
 	 *	Return specified dimension tree
 	 *
 	 * @param pafTreeRequest Tree request object (contains specified dimension name)
 	 * 
 	 * @return PafTreeResponse (contains requested dimension tree)
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafTreeResponse getDimensionTree(PafTreeRequest pafTreeRequest)
 			throws RemoteException, PafSoapException  {
 
 		PafTreeResponse pafTreeResponse = new PafTreeResponse();
 		PafSimpleDimTree simpleTree = null;
 
 		//if ( isAuthorized(pafTreeRequest.getClientId(), false)) {
 		
 			try {
 				// TODO Modify this method to conform to standard service request by
 				// adding client state to call
 				// TODO Enable client logging options after client state has been
 				// added
 	
 				pushToNDCStack(pafTreeRequest.getClientId());
 				
 				String dimName = pafTreeRequest.getTreeName();
 				if ( dimName != null ) {			
 					// Get paf simple tree for selected dimension
 					try {
 						simpleTree = dataService.getSimpleTree(dimName);
 						
 						//if client wants tree compressed
 						if ( pafTreeRequest.isCompressResponse() ) {
 							simpleTree.compressData();
 						}
 						
 						pafTreeResponse.setPafSimpleDimTree(simpleTree);
 					} catch (PafException pex) {
 						PafErrHandler.handleException(pex);
 					}					
 				} else {
 					String errMsg = Messages.getString("PafServiceProvider.38"); //$NON-NLS-1$
 					IllegalArgumentException iae = new IllegalArgumentException(errMsg);
 					logger.error(errMsg);
 					handleRuntimeException(iae);
 				}
 			} catch (RuntimeException re) {
 				handleRuntimeException(re);		
 			} finally {
 				popFromNDCStack(pafTreeRequest.getClientId());		
 			}	
 		//}
 
 		// Return tree response
 		return pafTreeResponse;
 
 	}
 
 	/**
 	 *	Get all dimension trees
 	 *
 	 * @param pafTreesRequest Request object
 	 * 
 	 * @return PafTreesResponse (contains dimension trees)
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafTreesResponse getDimensionTrees(@SuppressWarnings("unused") //$NON-NLS-1$
 			PafTreesRequest pafTreesRequest) throws RemoteException, PafSoapException  {
 
 		PafTreesResponse pafTreesResponse = new PafTreesResponse();
 		Map<String, PafDimTree> treeMap = dataService.getAllDimTrees();
 		Set<PafSimpleDimTree> simpleTreeSet = new HashSet<PafSimpleDimTree>(); 
 
 		// TODO Modify this method to conform to standard service request by
 		// adding client state to call
 		// TODO Enable client logging options after client state has been
 		// added
 
 		// Set logger client info property to user name
 		// NDC.push(clientState.getUserName());
 
 		try {
 			simpleTreeSet = dataService.getAllSimpleDimTrees();
 		} catch (RuntimeException re) {
 			handleRuntimeException(re);	
 		} catch (PafException pex) {
 			PafErrHandler.handleException(pex);
 		}
 		
 		//if not null, convert set into array and add to response object
 		if ( simpleTreeSet != null) {
 			
 			PafSimpleDimTree[] pafSimpleDimTreeAr = simpleTreeSet.toArray(new PafSimpleDimTree[0]);  
 			
 			if ( pafTreesRequest.isCompressResponse()) {
 				
 				for (PafSimpleDimTree pafSimpleDimTree : pafSimpleDimTreeAr) {
 					pafSimpleDimTree.compressData();
 				}
 				
 			}
 			
 			pafTreesResponse.setPafSimpleDimTrees(pafSimpleDimTreeAr);			
 		}
 		
 		//return response object;
 		return pafTreesResponse;
 	}
 	
 	
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param cmdRequest
 	 * @return
 	 * @throws PafSoapException
 	 */
 	public PafCustomCommandResponse runCustomCommand(
 			PafCustomCommandRequest cmdRequest) throws PafSoapException {
 
 		PafCustomCommandResponse cmdResponse = new PafCustomCommandResponse();
 
 		// Run custom command
 		try {
 			
 			pushToNDCStack(cmdRequest.getClientId());
 
 			cmdResponse.setCommandResults(appService.runCustomCommand(
 					cmdRequest.getMenuCommandKey(), cmdRequest
 							.getParameterKeys(), cmdRequest
 							.getParameterValues(), clients.get(cmdRequest
 							.getClientId())));
 			
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 			
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 			
 		} finally {
 
 			// pop from stack
 			popFromNDCStack(cmdRequest.getClientId());
 
 		}
 
 		// Return command response
 		return cmdResponse;
 	}
 
 	/**
 	 *	Ends the current planning session.  This method cleans up the UOW Cache
 	 *  and then removes the client id from the map of clients.
 	 *
 	 * @param endSessionRequest
 	 * @throws RemoteException
 	 * @throws PafSoapException
 	 */
 	public PafResponse endPlanningSession(PafRequest endSessionRequest)
 			throws RemoteException, PafSoapException {
 
 		boolean isSuccessful = false;
 		
 		try {
 					
 			pushToNDCStack(endSessionRequest.getClientId());
 	
 			// End planning session - cleanup unneeded objects
 			dataService.removeUowCache(endSessionRequest.getClientId());
 	
 			logger.info(Messages.getString("PafServiceProvider.41") + endSessionRequest.getClientId() //$NON-NLS-1$
 					+ Messages.getString("PafServiceProvider.42")); //$NON-NLS-1$
 			logger.info(Messages.getString("PafServiceProvider.43") //$NON-NLS-1$
 					+ dataService.getUowCacheCnt());
 							
 			isSuccessful = true;
 			
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} finally {
 
 			// Initialize logger client info property
 			popFromNDCStack(endSessionRequest.getClientId());
 
 			try {
 				
 				//remove from client
 				clients.remove(endSessionRequest.getClientId());
 				
 			} catch (RuntimeException re ) {
 				
 				isSuccessful = false;
 				
 			}
 
 		}
 		
 		//if succesfully removed uow and remove client id form clients
 		if ( isSuccessful ) {
 			
 			logger.info(Messages.getString("PafServiceProvider.SuccessEndPlanSession"));
 			
 		} else {
 			
 			logger.info(Messages.getString("PafServiceProvider.FailEndPlanSession"));
 			
 		}
 		
 		return new PafResponse();
 
 	}
 
 	/**
 	 * Adds client information to the stack.
 	 * 
 	 * @param clientId
 	 * 				Client Id to remove from stack.
 	 */
 	private void pushToNDCStack(String clientId) {
 
 		if (clientId != null) {
 
 			//try to get client state from map
 			PafClientState clientState = clients.get(clientId);
 
 			if (clientState != null) {
 
 				//get client ip address
 				String ipAddress = clientState.getClientIpAddress();
 
 				//if user name is not null, push ip and username on stack
 				if (clientState.getUserName() != null) {
 
 					NDC.push(ipAddress + Messages.getString("PafServiceProvider.44") + clientState.getUserName()); //$NON-NLS-1$
 
 				//else just push ip with not auth message
 				} else {
 
 					NDC.push(ipAddress + Messages.getString("PafServiceProvider.45")); //$NON-NLS-1$
 
 				}
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * Removes client information from the stack.
 	 * 
 	 * @param clientId
 	 * 				Client Id to remove from stack.
 	 */
 	private void popFromNDCStack(String clientId) {
 
 		if (clientId != null) {
 
 			//try to get client state
 			PafClientState clientState = clients.get(clientId);
 
 			//if client state exist
 			if (clientState != null) {
 
 				//pop
 				NDC.pop();
 
 			}
 
 		}
 
 	}
 	
 	/**
 	 *	Method_description_goes_here
 	 *
 	 * @param re
 	 * @throws PafSoapException
 	 */
 	private void handleRuntimeException(RuntimeException re) throws PafSoapException {
 		
 		StackTraceElement ste = re.getStackTrace()[0];
 
 		String errorMessage = Messages.getString("PafServiceProvider.46") + re.toString() + Messages.getString("PafServiceProvider.47") + ste.toString().trim();  //$NON-NLS-1$ //$NON-NLS-2$
 				
 		//-- old way PafException pex = new PafException(errorMessage, PafErrSeverity.Error);
 		PafException pex = new PafException(re.getMessage(), PafErrSeverity.Error, re);
 		pex.addMessageDetail(errorMessage);
 				
 		PafErrHandler.handleException(pex);
 
 		throw pex.getPafSoapException();		
 		
 	}
 
 	/**
 	 * Changes a db user's password in the paf security db by calling the PafSecurityService layer.
 	 * 
 	 * @param changePasswordRequest
 	 *            Client Change Password Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized 
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafClientSecurityResponse changePafUserPassword(PafClientChangePasswordRequest changePasswordRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException  {
 
 		PafClientSecurityResponse response = new PafClientSecurityResponse();
 		
 		//if authorized via client id
 		if ( isAuthorized(changePasswordRequest.getClientId(), false) ) {
 		
 			pushToNDCStack(changePasswordRequest.getClientId());
 			
 			boolean passwordWasChanged = PafSecurityService.changePasswordPafSecurityDbUser(changePasswordRequest.getPafUserDef(), changePasswordRequest.getNewPassword());
 		
 			//set if password was successfully changed or not			
 			response.setSuccessful(passwordWasChanged);
 			
 			popFromNDCStack(changePasswordRequest.getClientId());
 		
 		}
 		
 		return response;
 
 	}
 
 	/**
 	 * Creates a db user in the paf security db by calling the PafSecurityService layer.
 	 * 
 	 * @param clientSecurityRequest
 	 *            Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafClientSecurityResponse createPafUser(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 		
 		PafClientSecurityResponse response = new PafClientSecurityResponse();
 		
 		//if authorized via client id		
 		if (isAuthorized(clientSecurityRequest.getClientId(), true)) {
 	
 			pushToNDCStack(clientSecurityRequest.getClientId());
 			
 			boolean successful = PafSecurityService.createPafSecurityDbUser(clientSecurityRequest.getPafUserDef());
 			
 			//set if transaction was successful or not			
 			response.setSuccessful(successful);
 			
 			popFromNDCStack(clientSecurityRequest.getClientId());
 		
 		}
 		
 		return response;
 		
 	}
 
 	/**
 	 * Deletes a db user in the paf security db by calling the PafSecurityService layer.
 	 * 
 	 * @param clientSecurityRequest
 	 *            Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafClientSecurityResponse deletePafUser(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, 
 																PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException {
 		
 		PafClientSecurityResponse response = new PafClientSecurityResponse();
 		
 		//if authorized via client id		
 		if ( isAuthorized(clientSecurityRequest.getClientId(), true)) {
 		
 			pushToNDCStack(clientSecurityRequest.getClientId());
 			
 			boolean successful = false;
 			
 			try {
 				
 				successful = PafSecurityService.deletePafSecurityDbUser(clientSecurityRequest.getPafUserDef());
 				
 			} catch (PafException e) {
 				
 				//handle exception
 				PafErrHandler.handleException(e);
 				
 				//throw paf soap exception
 				throw e.getPafSoapException();
 				
 			}
 
 			//set if transaction was successful or not
 			response.setSuccessful(successful);
 			
 			popFromNDCStack(clientSecurityRequest.getClientId());
 		
 		}
 		
 		return response;
 		
 	}
 
 	/**
 	 * Gets a multidimensional array of domains and security groups in those domains.
 	 * 
 	 */
 	public PafGroupSecurityResponse getPafGroups(PafGroupSecurityRequest groupSecurityRequest)throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException, PafNotAbletoGetLDAPContext{
 		
 		PafGroupSecurityResponse response = new PafGroupSecurityResponse();
 		
 		//if authorized via client id		
 		if ( isAuthorized(groupSecurityRequest.getClientId(), false)) {
 			
 			if (PafMetaData.getServerSettings().getAuthMode().equalsIgnoreCase(AuthMode.mixedMode.toString())){
 				
 				LDAPAuthentication ldapAuth = new LDAPAuthentication();
 
 				Map<String, TreeSet<String>> securityGroups = ldapAuth.getSecurityGroups(PafMetaData.getServerSettings());
 
 				PafSecurityDomainGroups[] domainGroups = new PafSecurityDomainGroups[securityGroups.size()];
 				PafSecurityDomainGroups domainGroup;
 				PafSecurityGroup[] secGroups;
 				int i = 0;
 				int j;
 
 				for( String domain : securityGroups.keySet()){
 					domainGroup = new PafSecurityDomainGroups();
 					domainGroup.setDomain(domain.toString());
 
 					secGroups = new PafSecurityGroup[securityGroups.get(domain).size()];
 					j = 0;
 					for (String groupName : securityGroups.get(domain)){
 						PafSecurityGroup securityGroup = new PafSecurityGroup();
 						securityGroup.setGroupName(groupName);
 						secGroups[j++] = securityGroup;
 					}
 
 					domainGroup.setSecurityGroups(secGroups);
 					domainGroups[i++] = domainGroup;
 				}
 
 				response.setDomainGroups(domainGroups);
 			}
 		}
 
 		return response;
 	}
 	
 	/**
 	 * Gets a multidimensional array of domains and security groups in those domains.
 	 * 
 	 */
 	public PafUserNamesforSecurityGroupsResponse getUserNamesForSecurityGroups(PafUserNamesforSecurityGroupsRequest groupSecurityRequest)throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException, PafNotAbletoGetLDAPContext{
 		
 		PafUserNamesforSecurityGroupsResponse response = new PafUserNamesforSecurityGroupsResponse();
 		
 		List<PafSecurityDomainUserNames> securityDomainUserNamesTreeSet = new ArrayList<PafSecurityDomainUserNames>();
 		
 		//if authorized via client id		
 		if ( isAuthorized(groupSecurityRequest.getClientId(), false)) {
 			
 			if (groupSecurityRequest.getDomainUserNames() == null){
 
 				//Native
 				PafSecurityDomainUserNames securityDomainUserNames = securityDomainUserNames(groupSecurityRequest.getClientId());
 				securityDomainUserNamesTreeSet.add(securityDomainUserNames);
 
 				//LDAP
 				if (PafMetaData.getServerSettings().getAuthMode().equalsIgnoreCase(AuthMode.mixedMode.toString())){
 					
 					List<SecurityGroup> securityGroups = dataService.getGroups(groupSecurityRequest.getApplication());
 					Map<String, List<String>> paceGroups = new HashMap<String, List<String>>();
 					
 					for (SecurityGroup securityGroup : securityGroups){
 						if(! paceGroups.containsKey(securityGroup.getSecurityDomainNameTxt())){
 							paceGroups.put(securityGroup.getSecurityDomainNameTxt(), new ArrayList<String>());
 						}
 						paceGroups.get(securityGroup.getSecurityDomainNameTxt()).add(securityGroup.getSecurityGroupNameTxt());
 					}
 					LDAPAuthentication ldapAuth = new LDAPAuthentication();
 					TreeSet<PafSecurityDomainUserNames> securityDomainUserNamesLDAPTreeSet = ldapAuth.getUserNamesforSecurityGroups(PafMetaData.getServerSettings(), paceGroups );
 					securityDomainUserNamesTreeSet.addAll(securityDomainUserNamesLDAPTreeSet);
 				}
 			}else{
 				//Return user names for requested groups
 				Map<String, List<String>> paceGroups = new HashMap<String, List<String>>();
 
 				for (PafSecurityDomainGroups domainGroup :  groupSecurityRequest.getDomainUserNames()){
 					
 					if(domainGroup.getDomain().equals(PafBaseConstants.Native_Domain_Name)){
 						//Native
 						PafSecurityDomainUserNames securityDomainUserNames = securityDomainUserNames(groupSecurityRequest.getClientId());
 						securityDomainUserNamesTreeSet.add(securityDomainUserNames);
 					}
 					else if (PafMetaData.getServerSettings().getAuthMode().equalsIgnoreCase(AuthMode.mixedMode.toString())){
 						paceGroups.put(domainGroup.getDomain(), Arrays.asList(domainGroup.getgroups()));
 					}	
 				}
 				
 				if (paceGroups.size() > 0){
 					LDAPAuthentication ldapAuth = new LDAPAuthentication();
 					TreeSet<PafSecurityDomainUserNames> securityDomainUserNamesLDAPTreeSet = ldapAuth.getUserNamesforSecurityGroups(PafMetaData.getServerSettings(), paceGroups );
 					securityDomainUserNamesTreeSet.addAll(securityDomainUserNamesLDAPTreeSet);
 				}
 			}
 			
 			//Both
 			response.setDomainUserNames(securityDomainUserNamesTreeSet.toArray(new PafSecurityDomainUserNames[0]));
 		}
 
 		return response;
 	}
 	
 	public PafVerifyUsersResponse verifyUsers(PafVerifyUsersRequest req) throws RemoteException, PafSoapException,
 	PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafNotAbletoGetLDAPContext{
 		PafVerifyUsersResponse InvalidUsers = new PafVerifyUsersResponse();
 		Map<String, List<String>> domainUsers = new HashMap<String, List<String>>();
 		
 		String userName;
 		String domainName;
 
 		if (req != null && req.getUsers() != null){
 			for ( String user : req.getUsers()){
 				if (DomainNameParser.isValidDomainUserName(user, DomainNameParser.AT_TOKEN)){
 					DomainNameParser dp = new DomainNameParser(user, new String[] { DomainNameParser.AT_TOKEN });
 					userName = dp.getUserName();
 					domainName = dp.getDomainName();
 				}
 				else{
 					continue;
 				}
 
 				if (domainUsers.containsKey(domainName)){
 					domainUsers.get(domainName).add(userName);
 				}else{
 					List<String> userList = new ArrayList<String>();
 					userList.add(userName);
 					domainUsers.put(domainName, userList);
 				}
 			}
 			LDAPAuthentication ldapAuth = new LDAPAuthentication();
 
 			Map<String, List<String>> invalidDomainUsers = ldapAuth.validateUsers(PafMetaData.getServerSettings(), domainUsers);
 
 			List<String> invalidUserList = new ArrayList<String>();
 			for (String domain : invalidDomainUsers.keySet()){
 				for(String user : invalidDomainUsers.get(domain)){
 					invalidUserList.add(user + "@" + domain);
 				}
 			}
 
 			String[] invalidUsersArray  = invalidUserList.toArray(new String[0]);
 			InvalidUsers.setUsers(invalidUsersArray);
 
 		}
 		
 		return InvalidUsers;
 	}
 	
 	private PafSecurityDomainUserNames securityDomainUserNames(String clientID){
 		PafSecurityDomainUserNames securityDomainUserNames = new PafSecurityDomainUserNames();
 		
 		pushToNDCStack(clientID);
 
 		//get security db users
 		PafUserDef[] pafUsersDef = PafSecurityService.getPafSecurityDbUsers();
 
 		String[] pafUserNames = null;
 
 		if ( pafUsersDef != null && pafUsersDef.length > 0) {
 
 			pafUserNames = new String[pafUsersDef.length];
 
 			int usrDefNdx = 0;
 
 			for ( PafUserDef pafUserDef : pafUsersDef ) {
 
 				pafUserNames[usrDefNdx++] = pafUserDef.getUserName();
 			}
 		}
 
 		PafUserNamesSecurityGroup userNamesSecurityGroup = new PafUserNamesSecurityGroup();
 		List<PafUserNamesSecurityGroup> userNamesSecurityGroupList = new ArrayList<PafUserNamesSecurityGroup>();
 		userNamesSecurityGroup.setGroupName(PafBaseConstants.Native_Domain_Name);
 		userNamesSecurityGroup.setUserNames(pafUserNames);
 		userNamesSecurityGroupList.add(userNamesSecurityGroup);
 		securityDomainUserNames.setDomainName(PafBaseConstants.Native_Domain_Name);
 		securityDomainUserNames.setUserNamesSecurityGroup(userNamesSecurityGroupList.toArray(new PafUserNamesSecurityGroup[0]));
 
 		popFromNDCStack(clientID);
 		
 		return securityDomainUserNames;
 	}
 	
 	
 	/**
 	 * Gets a single db user in the paf security db by calling the PafSecurityService layer.
 	 * 
 	 * @param clientSecurityRequest
 	 *            Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafClientSecurityResponse getPafUser(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, 
 																PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafNotAbletoGetLDAPContext{
 
 		PafClientSecurityResponse response = new PafClientSecurityResponse();
 		
 		//if authorized via client id		
 		if ( isAuthorized(clientSecurityRequest.getClientId(), true) ) {
 			
 			PafUserDef pafUserDef = null;
 			
 			String user = clientSecurityRequest.getPafUserDef().getUserName();
 			String domain = clientSecurityRequest.getPafUserDef().getDomain();
 			
 			//temporary fix
 			if(domain == null){
 				domain = PafBaseConstants.Native_Domain_Name;
 			}
 			
 			String generatedIV = AESEncryptionUtil.generateIV();
 			response.setIV(generatedIV);
 			
 			if(PafBaseConstants.Native_Domain_Name.equalsIgnoreCase(domain) && (PafMetaData.getServerSettings().getAuthMode().equalsIgnoreCase(AuthMode.nativeMode.toString()) ||
 					PafMetaData.getServerSettings().getAuthMode().equalsIgnoreCase(AuthMode.mixedMode.toString()))){
 				
 				pushToNDCStack(clientSecurityRequest.getClientId());
 				
 				//get db user from security service layer
 				pafUserDef = PafSecurityService.getPafSecurityDbUser(user);
 							
 				popFromNDCStack(clientSecurityRequest.getClientId());
 			
 			}
 //			In mixed mode, try the LDAP server if the user is not in the security db
 			else if(!PafBaseConstants.Native_Domain_Name.equalsIgnoreCase(domain) && PafMetaData.getServerSettings().getAuthMode().equalsIgnoreCase(AuthMode.mixedMode.toString())){
 				LDAPAuthentication ldapAuth = new LDAPAuthentication();
 				
 				pafUserDef = ldapAuth.getUser(PafMetaData.getServerSettings(), user, domain);								
 			}
 			
 			if (pafUserDef != null){
 				
 				//try to clone because of hibernate.  If key field (username) changes, hibernate freaks out
 				try {
 					pafUserDef = (PafUserDef) pafUserDef.clone();
 				} catch (CloneNotSupportedException e1) {
 					logger.warn("Couldn't clone pafUserDef.");
 				}
 				
 				//encrypt email
 				String email = pafUserDef.getEmail();
 				try{
 					if (email.length() > 0){
 						email = AESEncryptionUtil.encrypt(email, generatedIV);
 					}
 				}
 		  		catch(Exception e){
 		  			email = "";
 	    			logger.error(e.getMessage());
 	    		}
 				pafUserDef.setEmail(email);
 				
 				//encrypt first name
 				String firstName = pafUserDef.getFirstName();
 				try{
 					if (firstName.length() > 0){
 						firstName = AESEncryptionUtil.encrypt(firstName, generatedIV);
 					}
 				}
 		  		catch(Exception e){
 		  			firstName = "";
 	    			logger.error(e.getMessage());
 	    		}
 				pafUserDef.setFirstName(firstName);
 				
 				//encrypt last name
 				String lastName = pafUserDef.getLastName();
 				try{
 					if (lastName.length() > 0){
 						lastName = AESEncryptionUtil.encrypt(lastName, generatedIV);
 					}
 				}
 		  		catch(Exception e){
 		  			lastName = "";
 	    			logger.error(e.getMessage());
 	    		}
 				pafUserDef.setLastName(lastName);
 				
 				//encrypt last name
 				String password = pafUserDef.getPassword();
 				try{
 					if (password != null){
 						if (password.length() > 0){
 							password = AESEncryptionUtil.encrypt(password, generatedIV);
 						}
 					}else{
 						password = "";
 					}
 				}
 		  		catch(Exception e){
 		  			password = "";
 	    			logger.error(e.getMessage());
 	    		}
 				pafUserDef.setPassword(password);
 				
 //				encrypt user name
 				String userName = pafUserDef.getUserName();
 				try{
 					if (userName.length() > 0){
 						userName = AESEncryptionUtil.encrypt(userName, generatedIV);
 					}
 				}
 		  		catch(Exception e){
 		  			userName = "";
 	    			logger.error(e.getMessage());
 	    		}
 				pafUserDef.setUserName(userName);
 				
 				//Finally, set db user on respose
 				response.setPafUserDef(pafUserDef);
 			}
 		}
 		return response;
 	}
 	
 	public PafSetPaceGroupsResponse setGroups(PafSetPaceGroupsRequest paceGroupRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 		PafSetPaceGroupsResponse paceGroupResponse = new PafSetPaceGroupsResponse();
 		
 		//if authorized via client id		
 		if ( isAuthorized(paceGroupRequest.getClientId(), true)) {
 			if (PafMetaData.getServerSettings().getAuthMode().equalsIgnoreCase(AuthMode.mixedMode.toString())){
 				PafDataService dataService = PafDataService.getInstance();
 
 				List<SecurityGroup> securityGroups = new ArrayList<SecurityGroup>();
 
 				//if security groups exist
 				if ( paceGroupRequest.getSecurityGroups() != null ) {
 
 					for (SecurityGroup paceGroup : paceGroupRequest.getSecurityGroups()){
 						securityGroups.add(paceGroup);
 					}
 				}
 				
 				String app = "";
 				boolean isSuccess;
 				if (paceGroupRequest.getApplication() != null){
 					app = paceGroupRequest.getApplication();
 					isSuccess = dataService.setGroups(securityGroups, app);
 				}
 				else{
 					isSuccess = false;
 				}
 				
 				paceGroupResponse.setSuccess(isSuccess);
 			}
 		}
 		
 		return paceGroupResponse;
 	}
 	
 	public PafGetPaceGroupsResponse getGroups(PafGetPaceGroupsRequest paceGroupRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 		PafGetPaceGroupsResponse paceGroupsResponse = new PafGetPaceGroupsResponse();
 		
 		//if authorized via client id		
 		if ( isAuthorized(paceGroupRequest.getClientId(), false)) {
 			if (PafMetaData.getServerSettings().getAuthMode().equalsIgnoreCase(AuthMode.mixedMode.toString())){
 				PafDataService dataService = PafDataService.getInstance();
 				
 				List<SecurityGroup> securityGroups = dataService.getGroups(paceGroupRequest.getApplication());
 				
 				SecurityGroup[] securityGroupsArray = new SecurityGroup[securityGroups.size()];
 				
 				paceGroupsResponse.setSecurityGroups(securityGroups.toArray(securityGroupsArray));
 			}
 		}
 		return paceGroupsResponse;
 	}
 	
 
 	/**
 	 * Gets all db users in the paf security db by calling the PafSecurityService layer.
 	 * 
 	 * @param clientSecurityRequest
 	 *            Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafClientSecurityResponse getPafUsers(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 
 		PafClientSecurityResponse response = new PafClientSecurityResponse();
 		
 		//if authorized via client id		
 		if ( isAuthorized(clientSecurityRequest.getClientId(), true)) {
 		
 			pushToNDCStack(clientSecurityRequest.getClientId());
 			
 			//get security db users
 			PafUserDef[] pafUsersDef = PafSecurityService.getPafSecurityDbUsers();
 			
 			//add db users to response
 			response.setPafUserDefs(pafUsersDef);
 					
 			popFromNDCStack(clientSecurityRequest.getClientId());
 			
 		}
 		
 		return response;
 		
 	}
 	
 	
 	/**
 	 * Gets all db users in the paf security db by calling the PafSecurityService layer.
 	 * 
 	 * @param clientSecurityRequest
 	 *            Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafClientSecurityResponse getPafUserNames(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, 
 																	PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 
 		PafClientSecurityResponse response = new PafClientSecurityResponse();
 		
 		//if authorized via client id		
 		if ( isAuthorized(clientSecurityRequest.getClientId(), false)) {
 		
 			pushToNDCStack(clientSecurityRequest.getClientId());
 			
 			//get security db users
 			PafUserDef[] pafUsersDef = PafSecurityService.getPafSecurityDbUsers();
 			
 			String[] pafUserNames = null;
 			
 			if ( pafUsersDef != null && pafUsersDef.length > 0) {
 				
 				pafUserNames = new String[pafUsersDef.length];
 				
 				int usrDefNdx = 0;
 				
 				for ( PafUserDef pafUserDef : pafUsersDef ) {
 					
 					pafUserNames[usrDefNdx++] = pafUserDef.getUserName();
 					
 				}
 				
 			}
 			
 			//add db users to response
 			response.setPafUserNames(pafUserNames);
 					
 			popFromNDCStack(clientSecurityRequest.getClientId());
 			
 		}
 		
 		return response;
 		
 	}
 
 	/**
 	 * Resets a db user's password in the paf security db by calling the PafSecurityService layer.
 	 * 
 	 * @param clientSecurityRequest
 	 *            Client Security Request (contains clientId)
 	 * @return PafClientSecurityPasswordResetResponse
 	 * @throws RemoteException
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 */
 	public PafClientSecurityPasswordResetResponse resetPafUserPassword(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, PafSoapException {
 
 		PafClientSecurityPasswordResetResponse response = new PafClientSecurityPasswordResetResponse();
 				
 		pushToNDCStack(clientSecurityRequest.getClientId());			
 		
 		try {
 			
 			PafUserDef pafUserDef = clientSecurityRequest.getPafUserDef();
 			
 			if ( pafUserDef != null ) {
 				
 				response.setUserName(pafUserDef.getUserName());
 			
 				String emailAddress = PafSecurityService.resetPasswordPafSecurityDbUser(clientSecurityRequest.getPafUserDef());
 							
 				if ( emailAddress != null ) {
 				
 					response.setSuccessful(true);
 					response.setUserEmailAddress(emailAddress);
 				
 				}
 				
 			} else {
 				
 				response.setInvalidUserName(true);
 				
 			}
 			
 		} catch (InvalidUserNameException e) {
 			
 			response.setInvalidUserName(true);
 			
 		} catch (NoEmailAddressException e) {
 		
 			response.setInvalidEmailAddress(true);
 			
 		} catch (PafException e) {
 			
 			throw e.getPafSoapException();
 		}
 					
 		popFromNDCStack(clientSecurityRequest.getClientId());
 				
 		return response;
 	}
 
 	/**
 	 * Updates a db user in the paf security db by calling the PafSecurityService layer.
 	 * 
 	 * @param clientSecurityRequest
 	 *            Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 */	
 	public PafClientSecurityResponse updatePafUser(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, 
 																PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException {
 		
 		PafClientSecurityResponse response = new PafClientSecurityResponse();
 
 		//if authorized via client id
 		if ( isAuthorized(clientSecurityRequest.getClientId(), true)) {
 		
 			pushToNDCStack(clientSecurityRequest.getClientId());
 			
 			boolean successful = false;
 			try {
 				successful = PafSecurityService.updatePafSecurityDbUser(clientSecurityRequest.getPafUserDef());
 			} catch (PafException e) {
 				
 				PafErrHandler.handleException(e, PafErrSeverity.Error);
 				
 				throw e.getPafSoapException();
 				
 			}
 			
 			//set if transaction was successful or not
 			response.setSuccessful(successful);
 			
 			popFromNDCStack(clientSecurityRequest.getClientId());
 		
 		}
 		
 		return response;
 	}
 
 	
 	/**
 	 * Import attribute dimensions from multi-dimensional database
 	 *
 	 * @parm importAttrRequest Import attributes request object
 	 * 
 	 * @return PafImportAttrResponse Import attributes response object
 	 * @throws PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException 
 	 */
 	public PafImportAttrResponse importMdbAttributeDims(PafImportAttrRequest importAttrRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException {
 		PafImportAttrResponse resp = new PafImportAttrResponse();
 		boolean success = false;
 		
 		try
 		{
 			if(isAuthorized(importAttrRequest.getClientId(), false)){
 				
 				// Set logger client info property to user name
 				pushToNDCStack(importAttrRequest.getClientId());
 	
 				// Import attribute dimensions
 				dataService.cacheAttributeDims(
 						importAttrRequest.getImportDimensions(), PafMetaData.getPaceProject().getApplicationDefinitions().get(0));
 				
 				// Set associated attributes properties on base tree members
 				Set<String> baseDimNames = dataService.getBaseDimNames();
 				for (String baseDim:baseDimNames) {
 					PafBaseTree baseTree = dataService.getBaseTree(baseDim);
 					baseTree = dataService.setAssociatedAttributes(baseTree, baseDim);
 				}
 
 			}
 			
 		}catch (RuntimeException re) {
 			handleRuntimeException(re);
 		}catch (PafException pex) {
 			success = false;
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 		}finally{
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(importAttrRequest.getClientId());
 			resp.setSuccess(success);
 		}
 		
 		return resp;
 	}
 
 	public PafClearImportedAttrResponse clearImportedMdbAttributeDims(PafClearImportedAttrRequest clearImportedAttrRequest) 
 		throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException {
 		
 		PafClearImportedAttrResponse resp = new PafClearImportedAttrResponse();
 		boolean success = true;
 		
 		try
 		{
 			if(isAuthorized(clearImportedAttrRequest.getClientId(), false)){
 				
 				// Set logger client info property to user name
 				pushToNDCStack(clearImportedAttrRequest.getClientId());
 	
 				if(! clearImportedAttrRequest.isClearAllDimensions()){
 					for(String dim : clearImportedAttrRequest.getDimensionsToClear()){
 						dataService.deleteCacheAttrDim(dim);
 					}
 				} else{
 					dataService.deleteAllCacheAttrDim();
 				}
 			}
 			
 		}catch (RuntimeException re) {
 			handleRuntimeException(re);
 			success = false;
 		}finally{
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(clearImportedAttrRequest.getClientId());
 			resp.setSuccess(success);
 		}
 		return resp;
 	}
 	
 	/**
 	 * Verifiy client id is authenticated and valid
 	 * 
 	 * @param clientId
 	 *      		Used to get client state from clients map.
 	 *
 	 * @return boolean true if authenticated and false if not
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 */	
 	
 	private boolean isAuthenticated(String clientId) throws PafNotAuthenticatedSoapException {
 		
 		boolean isAuthenticated = false;
 		
 		if ( clientId != null ) {
 			
 			//try to get client state from map
 			PafClientState pafClientState = clients.get(clientId);
 			
 			if ( pafClientState != null ) {
 				
 				//get session token from client state
 				String sessionToken = pafClientState.getSessionToken();
 				
 				//if session token is present, assume client id is authenticated
 				if ( sessionToken != null) {
 				
 					isAuthenticated = true;
 					
 				}
 				
 			} 				
 				
 		}
 
 		
 		if ( ! isAuthenticated ) {
 			throw new PafNotAuthenticatedSoapException(new PafException(Messages.getString("PafServiceProvider.54"), PafErrSeverity.Error)); //$NON-NLS-1$
 		}
 		
 		
 		return isAuthenticated;
 		
 	}
 
 	/**
 	 * When webservice calls need to be secure, they should wrap their biz logic
 	 * in an if statement that calls this method.  This method will return true if
 	 * the client id and session token are valid.
 	 * 
 	 * @param clientId
 	 *      		Used to get client state from clients map.
 	 * @param authenticateAsAdmin
 	 * 				If is admin is true, authorization will ensure client id is of an admin user
 	 *  
 	 * @return boolean true if authorized and false if not
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 */	
 	private boolean isAuthorized(String clientId, boolean authenticateAsAdmin) throws PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 			
 		boolean isAuthorized = false;
 				
 		if ( isAuthenticated(clientId ) ) {
 			
 			PafClientState pafClientState = clients.get(clientId);
 					
 			if ( authenticateAsAdmin ) {
 				
 				String userName = pafClientState.getUserName();
 				
 				if ( userName != null ) {						
 				
 					PafUserDef pafUser = pafClientState.getUserDef();
 				
 					if ( pafUser != null ) {
 					
 						boolean userIsAdmin = pafUser.getAdmin();
 						
 						if ( /*userIsAdmin != null &&*/ userIsAdmin	) { 
 							
 							//set is authorized to true
 							isAuthorized = true;
 						}
 					}
 				
 				}	
 			} else {
 
 				//set is authorized to true
 				isAuthorized = true;
 			}
 		}			
 								
 		//if not authorized, create a paf exception, then throw a not authorized exception
 		if ( ! isAuthorized ) {
 			
 			PafException pafException = new PafException(Messages.getString("PafServiceProvider.55"), PafErrSeverity.Error); //$NON-NLS-1$
 			
 			throw new PafNotAuthorizedSoapException(pafException);
 			
 		}
 				
 		return isAuthorized;	
 		
 	}
 
 	/**
 	 *	Get list of properties from multi-dimensional database
 	 * @param mdbRequest Mdb props request object
 	 * 
 	 * @return PafMdbPropsResponse Mdb props response object
 	 * @throws RemoteException, PafSoapException 
 	 * @throws PafSoapException 
 	 * @throws PafNotAuthenticatedSoapException 
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafMdbPropsResponse getMdbProps(PafMdbPropsRequest mdbRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException {
 		PafMdbPropsResponse resp = new PafMdbPropsResponse();
 		boolean success = false;
 		try
 		{
 			if(isAuthorized(mdbRequest.getClientId(), false)){
 				
 				//Set logger client info property to user name
 				pushToNDCStack(mdbRequest.getClientId());
 
 				IPafConnectionProps connProps = (IPafConnectionProps) 
 	        		PafMetaData.getAppContext().getBean(
 	        				PafMetaData.getPaceProject().getApplicationDefinitions().get(0).getMdbDef().getDataSourceId());
 				
 				// Get mdb properties
 				resp.setMdbProps(dataService.getMdbProps(connProps));
 				success = true;
 			}
 			
 		}catch (RuntimeException re) {
 			handleRuntimeException(re);
 		}catch (PafException pex) {
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 		}finally{
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(mdbRequest.getClientId());
 			resp.setSuccess(success);
 		}
 		
 		return resp;
 	}
 	
 	
 	/**
 	 *	Return the valid list of attribute members and rollups
 	 *  in light of selections on the related base dimension
 	 *  and any other related attribute members
 	 *  
 	 * @param attrRequest Valid attribute request object
 	 * @return PafValidAttrResponse Valid attribute response object
 	 * 
 	 * @throws RemoteException, PafSoapException 
 	 * @throws PafSoapException 
 	 * @throws PafNotAuthenticatedSoapException 
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafValidAttrResponse getValidAttributeMembers(PafValidAttrRequest attrRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException {
 		
 		PafValidAttrResponse resp = new PafValidAttrResponse();
 		boolean success = false;
 		try
 		{
 			if(isAuthorized(attrRequest.getClientId(), false)){
 				
 				//Set logger client info property to user name
 				pushToNDCStack(attrRequest.getClientId());
 
 				// Get valid attributes
 				String[] attrMembers = dataService.getValidAttributeMembers(attrRequest.getReqAttrDim(), attrRequest.getSelBaseDim(), 
 						attrRequest.getSelBaseMember(), attrRequest.getSelAttrSpecs());
 				resp.setMembers(attrMembers);
 				success = true;
 			}
 			
 		}catch (RuntimeException re) {
 			handleRuntimeException(re);
 		}finally{
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(attrRequest.getClientId());
 			resp.setSuccess(success);
 		}
 		
 		return resp;
 	}
 	
 	
 	/**
 	 *	Provides the PafSimpleTrees to the client
 	 */
 	public PafPopulateRoleFilterResponse populateRoleFilters(PafPlanSessionRequest planRequest) throws RemoteException, PafSoapException{
 		PafPopulateRoleFilterResponse resp = new PafPopulateRoleFilterResponse();
 		
 		try {
 //			Set logger client id field to user name
 			pushToNDCStack(planRequest.getClientId());
 			
 			PafClientState clientState = clients.get(planRequest.getClientId());
 			
 			//Add the pafPlannerConfig to the client state
 			//It is needed for the PafSecurityService.getWorkSpec call
 			String roleName = planRequest.getSelectedRole();
 			String seasonId = planRequest.getSeasonId();
 			Season planSeason = clientState.getApp().getSeasonList().getSeasonById(seasonId);
 			
 			// try to find the paf planner config via role and cycle
 			PafPlannerConfig pafPlannerConfig = findPafPlannerConfig(roleName,
 					planSeason.getPlanCycle());
 
 			// if no paf planner configs exists throw exception
 			if (pafPlannerConfig == null) {
 
 				throw new PafException(
 						Messages.getString("PafServiceProvider.58") + roleName //$NON-NLS-1$
 								+ Messages.getString("PafServiceProvider.59") + planSeason.getPlanCycle() //$NON-NLS-1$
 								+ Messages.getString("PafServiceProvider.SQuotePeriod"), PafErrSeverity.Error); //$NON-NLS-1$
 
 			}
 		
 			clientState.setPlannerConfig(pafPlannerConfig);
 		
 			UnitOfWork workUnit = PafSecurityService.getWorkSpec(planRequest.getSelectedRole(), planRequest.getSeasonId(), clientState);
 			
 			workUnit = dataService.expandUOW(workUnit, clientState);
 
 			clientState.setUnitOfWork(workUnit);
 			
 			clientState.setPlanSeason(planSeason);
 			
 			UserFilterSpec userFilterSpec = null;
 			if(pafPlannerConfig.getIsUserFilteredUow()){
 				userFilterSpec = pafPlannerConfig.getUserFilterSpec();
 			}
 			resp.setUserFilterSpec(userFilterSpec);
 			
 			List<String> userFilterSpecList = null;
 			if (userFilterSpec != null){
 				userFilterSpecList = Arrays.asList(userFilterSpec.getAttrDimNames());
 			}
 			
 			List<String> hierDimList = Arrays.asList(clientState.getApp().getMdbDef().getHierDims());
 
 			MemberTreeSet treeSet = dataService.getUowCacheTrees(clientState);
 			
 			clientState.setUowTrees(treeSet);
 			
 			// need to set id of simple tree to root dimension, not default
 			// behavior
 			// key is normally root node. Then store in response object
 			PafSimpleDimTree simpleDimTree = null;
 			List<PafSimpleDimTree> simpleDimTrees = new ArrayList<PafSimpleDimTree>();
 
 			for (String memberTreeDim : treeSet.getTreeDimensions()) {
 				//If a user filter list exists, use it to filter out the dimension trees
 				if(!hierDimList.contains(memberTreeDim)){
 					if(userFilterSpec != null && !userFilterSpecList.contains(memberTreeDim)){
 						continue;
 					}
 				}
 				
 				// Get simple version of current tree
 				simpleDimTree = treeSet.getTree(memberTreeDim).getSimpleVersion();
 				// Override id of subtree with actual dimension name (required by paf client)
 				simpleDimTree.setId(memberTreeDim);
 				// Add simple tree to collection
 				simpleDimTrees.add(simpleDimTree);
 			}
 						
 			resp.setDimTrees(simpleDimTrees.toArray(new PafSimpleDimTree[0]));
 			
 			// compress trees if requested by client
 			if (planRequest.isCompressResponse()) {
 				for (PafSimpleDimTree t : resp.getDimTrees() )
 					t.compressData();
 			}
 			
 			
 			//todo refactor for hier dims
 			resp.setBaseTreeNames(clientState.getApp().getMdbDef().getHierDims());
 			
 			resp.setAttributeTreeNames(dataService.getAttributeDimNames().toArray(new String[0]));
 			
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} catch (PafException pafException) {
 
 			PafErrHandler.handleException(pafException);
 
 			throw pafException.getPafSoapException();
 
 		} finally {
 
 			// Initialize logger client id field
 			popFromNDCStack(planRequest.getClientId());
 		}
 		
 		return resp;
 	}
 	
 	/**
 	 *	Returns the UOW size to the client
 	 */
 	public PafGetFilteredUOWSizeResponse getFilteredUOWSize(PafGetFilteredUOWSizeRequest filteredUOWSize) throws RemoteException, PafSoapException{
 		PafGetFilteredUOWSizeResponse resp = new PafGetFilteredUOWSizeResponse();
 		
 		try {
 //			Set logger client id field to user name
 			pushToNDCStack(filteredUOWSize.getClientId());
 			
 			UnitOfWork workUnit = null;
 			PafPlannerConfig pafPlannerConfig = null;
 			PafClientState clientState = clients.get(filteredUOWSize.getClientId());
 			String roleName = filteredUOWSize.getSelectedRole();
 			String seasonId = filteredUOWSize.getSeasonId();
 			Season planSeason = clientState.getApp().getSeasonList().getSeasonById(seasonId);
 			
 			//client state property used in evaluation 
 			//clientState.setIsDataFilteredUow(filteredUOWSize.getIsInvalidIntersectionSuppressionSelected());
 			
 			//Get the pafPlannerConfig.  Load from the clientState if available.
 			if (clientState.getPlannerConfig() == null){
 				pafPlannerConfig = findPafPlannerConfig(roleName, planSeason.getPlanCycle());
 
 				// if no paf planner configs exists throw exception
 				if (pafPlannerConfig == null) {
 
 					throw new PafException(
 							Messages.getString("PafServiceProvider.61") + roleName //$NON-NLS-1$
 							+ Messages.getString("PafServiceProvider.62") + planSeason.getPlanCycle() //$NON-NLS-1$
 							+ Messages.getString("PafServiceProvider.SQuotePeriod"), PafErrSeverity.Error); //$NON-NLS-1$
 
 				}
 
 				clientState.setPlannerConfig(pafPlannerConfig);
 			}
 			else{
 				pafPlannerConfig = clientState.getPlannerConfig();
 			}
 			
 			
 			//Set the clientState property
 			clientState.setUserFilteredUow(pafPlannerConfig.getIsUserFilteredUow());
 		
 			//User Filtering is selected
 			if(clientState.isUserFilteredUow()){
 				
 				workUnit = clientState.getUnitOfWork().clone();
 				
 				//Get all possible hierarchical base dimension with attributes
 				String[] hierDims = clientState.getApp().getMdbDef().getHierDims();
 				Map<String,Set<String>> hierDimsMap = new HashMap<String,Set<String>>();
 				for(String baseDim : hierDims){
 
 					//Get all possible attributes for the hierarchical base dimension
 					hierDimsMap.put(baseDim, dataService.getBaseTree(baseDim).getAttributeDimNames());
 				}
 					
 				//Get the role filter user selections
 				PafDimSpec[] pafDimSpecs = filteredUOWSize.getPafUserSelections();
 				//Convert to Map
 				Map<String, List<String>> userSelectionsMap = new HashMap<String, List<String>>();
 				for(PafDimSpec dimSpec : pafDimSpecs){
 					if(dimSpec.getDimension() != null && dimSpec.getExpressionList() != null){
 						userSelectionsMap.put(dimSpec.getDimension(),  Arrays.asList(dimSpec.getExpressionList()));
 					}
 				}
 				
 				// Add the role filter user selections to client state (TTN-1472)
 				clientState.setRoleFilterSelections(userSelectionsMap);
 				
 				//
 				Map<String, List<String>> validBaseMembers = new HashMap<String, List<String>>();
 				for(String baseDim : hierDimsMap.keySet()){
 					
 					if (userSelectionsMap.containsKey(baseDim)){
 						
 						List<String> expressionList;
 						expressionList = userSelectionsMap.get(baseDim);
 						//Only a single user selection can be made for hier dims so only looking at the first item
 						//in the expressionList works here.
 						//First, change expression list to IDesc
 						expressionList.set(0, "@IDESC(" + expressionList.get(0) + ", 0)"); //$NON-NLS-1$ //$NON-NLS-2$
 						
 						//Next, expand the base dimension expression list
 						expressionList = dataService.expandExpressionList(baseDim, expressionList, clientState);
 						
 						//Get a list of attribute dimensions and a list of attribute member lists
 						List<String> attrDimLists = new ArrayList<String>();
 						List<List<String>> attrMemberLists = new ArrayList<List<String>>();
 						for(String hierDim : hierDimsMap.get(baseDim)){
 							if (userSelectionsMap.containsKey(hierDim)){
 								attrDimLists.add(hierDim);
 								attrMemberLists.add(userSelectionsMap.get(hierDim));
 							}
 						}
 
 						//If there are no attribute dimensions, then do not filter the base dimension
 						List<String> validBaseMemberList = new ArrayList<String>();
 						if(attrDimLists.size() ==0){
 							//Build a map of valid members for each base dimension
 							for(String baseMember : expressionList){
 								validBaseMemberList.add(baseMember);
 							}
 						}
 						else{
 							//Get a list of all possible attribute intersection lists
 							List<List<String>> attrIntersectionLists = new ArrayList<List<String>>();
 							Odometer isIterator = new Odometer(attrMemberLists.toArray(new List[0]));
 							while (isIterator.hasNext()) {
 								List<String> isList = isIterator.nextValue();
 								attrIntersectionLists.add(isList);
 							}
 
 							//Build a map of valid members for each base dimension
 							for(String baseMember : expressionList){
 								Set<Intersection> validAttrIntersections = dataService.getAttributeIntersections(baseDim, baseMember, attrDimLists.toArray(new String[0]));
 								for(List<String> attrIntersectionList : attrIntersectionLists){
 									if(dataService.isValidAttributeIntersection(baseDim, baseMember, attrDimLists.toArray(new String[0]),
 											attrIntersectionList.toArray(new String[0]), validAttrIntersections)){
 										validBaseMemberList.add(baseMember);
 										break;
 									}
 								}
 							}
 						}
 						validBaseMembers.put(baseDim, validBaseMemberList);
 					}
 					
 				}
 				
 				//Update the work unit with the user filter
 				for(String dim : validBaseMembers.keySet()){
 					workUnit.setDimMembers(dim, validBaseMembers.get(dim).toArray(new String[0]));
 				}				
 			}
 			else{
 				workUnit = PafSecurityService.getWorkSpec(filteredUOWSize.getSelectedRole(), filteredUOWSize.getSeasonId(), clientState);
 				workUnit = dataService.expandUOW(workUnit, clientState);
 				
 				clientState.setUnitOfWork(workUnit.clone());
 				
 				clientState.setPlanSeason(planSeason);
 			}
 			
 			//Do not data filter if one or more dimensions have been filtered to 0 members
 			if(workUnit.getMemberCount() > 0){
 				if(filteredUOWSize.getIsInvalidIntersectionSuppressionSelected() == true){
 					
 					DataFilterSpec dataFilterSpec = null;
 					boolean isDataFiltered = false;
 					if(pafPlannerConfig.getIsDataFilteredUow()){
 						dataFilterSpec = pafPlannerConfig.getDataFilterSpec();
 						isDataFiltered = true;
 					}
 					else if(clientState.getApp().getAppSettings() != null &&
 							clientState.getApp().getAppSettings().isGlobalDataFilteredUow()){
 						dataFilterSpec = clientState.getApp().getAppSettings().getGlobalDataFilterSpec();
 						isDataFiltered = true;
 					}
 
 					//Data Filtering is selected
 					if (isDataFiltered){
 
 						//The Data Filter Spec exists
 						if(dataFilterSpec != null && dataFilterSpec.getDimSpec().length > 0){
 							UnitOfWork workUnitDF = workUnit.clone();
 
 							PafDimSpec[] nonHierDimSpecs = pafPlannerConfig.getDataFilterSpec().getDimSpec();
 
 							//De-tokenize the expressions
 							for(PafDimSpec pafDimSpec : nonHierDimSpecs){
 								if (pafDimSpec.getExpressionList() == null || pafDimSpec.getExpressionList()[0].length() == 0){
 									throw new PafException("Invalid Data Filter Spec Expression for " + pafDimSpec.getDimension() + " .", PafErrSeverity.Error);
 								}
 								
 								if (pafDimSpec.getExpressionList()[0].contains(com.pace.base.PafBaseConstants.UOW_ROOT)){
 									pafDimSpec.getExpressionList()[0] = viewService.replaceUserUow(pafDimSpec.getExpressionList()[0], clientState, pafDimSpec.getDimension());
 								}
 								else if(pafDimSpec.getExpressionList()[0].contains(com.pace.base.PafBaseConstants.PLAN_VERSION)){
 									pafDimSpec.getExpressionList()[0] = viewService.replaceUserVers(pafDimSpec.getExpressionList()[0], clientState);
 								}
 							}
 							
 							//Get trees associated with the unit of work
 							MemberTreeSet treeSet = dataService.getUowCacheTrees(clientState, workUnitDF);
 							
 //							Filter the hier dimension members to only include the L0 members
 							for(String dim1 : clientState.getApp().getMdbDef().getHierDims())
 							{
 								List<String> lowestLevelMembers = new ArrayList<String>();
 								for(PafDimMember lowestLevelMember : treeSet.getTree(dim1).getLowestMembers(treeSet.getTree(dim1).getRootNode().toString()))
 								{
 									lowestLevelMembers.add(lowestLevelMember.getKey());
 								}
 								workUnitDF.setDimMembers(dim1, lowestLevelMembers.toArray(new String[0]));
 							}
 							
 							//Update the Unit of Work with the base member selections from the data filter spec
 							for(PafDimSpec dimSpec : nonHierDimSpecs){
 								workUnitDF.setDimMembers(dimSpec.getDimension(), dimSpec.getExpressionList());
 							}
 
 							//Filter the data using an Essbase MDX query with the Non Empty flag
 							PafDimSpec[] hierDimSpecs = dataService.getFilteredMetadata(clientState, clientState.getApp(), workUnitDF.getPafDimSpecs());
 
 							//Update the Unit of Work with the data filtered hierachical dim metadata
 							//Expand hierarchy dim members to include ancestors
 							for(PafDimSpec dimSpec : hierDimSpecs){
 								List<PafDimMember> ancestors = new ArrayList<PafDimMember>();
 								Set<String> uniqueMembers = new HashSet<String>();
 								for(String memberName : dimSpec.getExpressionList()){
 									uniqueMembers.add(memberName);
 									ancestors = treeSet.getTree(dimSpec.getDimension()).getAncestors(memberName);
 									for(PafDimMember ancestor : ancestors){
 										uniqueMembers.add(ancestor.getKey());
 									}
 								}
 								
 								workUnit.setDimMembers(dimSpec.getDimension(), uniqueMembers.toArray(new String[0]));
 							}
 						}
 					}
 				}
 			}
 			
 			Integer sizeMax = null;
 			if(clientState.getPlannerConfig().getUowSizeMax() != null){
 				sizeMax = clientState.getPlannerConfig().getUowSizeMax();
 			}
 			else{
 				if(clientState.getApp().getAppSettings() != null && clientState.getApp().getAppSettings().getGlobalUowSizeMax() != null){
 					 sizeMax = clientState.getApp().getAppSettings().getGlobalUowSizeMax();
 				}
 			}
 			
 			int uowCellCount = workUnit.getMemberCount();
 			
 			//If the cell count falls within the bounds, then update the unit of work in the client state
 			if ((sizeMax == null || uowCellCount <= sizeMax) && uowCellCount > 0){
 				clientState.setUnitOfWork(workUnit);
 			}
 			
 			resp.setUowCellCount(uowCellCount);
 			
 			resp.setEmptyDimensions(workUnit.getEmptyDimensions());
 			
 			//Update response with the cell count
 			//resp.setUowCellCount()
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} catch (PafException pafException) {
 
 			PafErrHandler.handleException(pafException);
 
 			throw pafException.getPafSoapException();
 
 		} finally {
 
 			// Initialize logger client id field
 			popFromNDCStack(filteredUOWSize.getClientId());
 		}
 		
 		return resp;
 	}
 	
 	/**
 	 *	Logs off the current user.  This method cleans up the UOW Cache
 	 *  tied to the client id.
 	 *
 	 * @param logoffRequest			Logoff request	
 	 * @throws RemoteException		blah	
 	 * @throws PafSoapException		blah
 	 */
 	public PafResponse logoff(PafRequest logoffRequest)
 			throws RemoteException, PafSoapException {
 
 		try {
 					
 			pushToNDCStack(logoffRequest.getClientId());
 	
 			//cleanup unneeded objects
 			dataService.removeUowCache(logoffRequest.getClientId());
 	
 			logger.info(Messages.getString("PafServiceProvider.41") + logoffRequest.getClientId() //$NON-NLS-1$
 					+ Messages.getString("PafServiceProvider.42")); //$NON-NLS-1$
 			logger.info(Messages.getString("PafServiceProvider.43") //$NON-NLS-1$
 					+ dataService.getUowCacheCnt());
 			
 			logger.info(Messages.getString("PafServiceProvider.52")); //$NON-NLS-1$
 			logger.info(Messages.getString("PafServiceProvider.SuccessLogoff")); //$NON-NLS-1$
 			logger.info(Messages.getString("PafServiceProvider.52")); //$NON-NLS-1$
 			logger.info(""); //$NON-NLS-1$
 			
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 			
 		} finally {
 			
 			// Initialize logger client info property
 			popFromNDCStack(logoffRequest.getClientId());
 		}
 		
 		return new PafResponse();
 
 	}
 	
 //	private List<List<String>> getAttributeCombinationLists(List<List<String>> attrMemberLists){
 //		
 //		//Get the size of the cross product
 //		int combinationCount = 1;
 //		for(List<String> attrMemberList : attrMemberLists){
 //			combinationCount *= attrMemberList.size();
 //		}
 //		
 //		List<String>[] attributeCombinations = new List[combinationCount];
 //		int counter;
 //		int repeatCount = 1;
 //		for(List<String> attrMemberList : attrMemberLists){
 //			counter = 0;
 //			repeatCount = combinationCount/attrMemberList.size();
 //			
 //			for(String member : attrMemberList){
 //				for(int i = 0; i < repeatCount; i++){
 //					attributeCombinations[counter++].add(member);
 //				}
 //			}
 //		}
 //		
 //		return attrComboLists;
 //	}
 	
 	public PafGetNotesResponse getCellNotes(
 			PafGetNotesRequest getNotesRequest) throws RemoteException,
 			PafSoapException {
 
 		PafGetNotesResponse getNotesResponse = new PafGetNotesResponse();
 		
 		CellNoteCache cnc = CellNoteCacheManager.getInstance().getNoteCache(getNotesRequest.getClientId());
 		
 		getNotesResponse.setNotes(cnc.getNotes(getNotesRequest.getSimpleCoordLists()));
 
 		return getNotesResponse;
 	}
 	
 	
 	public PafSaveNotesResponse saveCellNotes(
 			PafSaveNotesRequest saveNotesRequest) throws RemoteException,
 			PafSoapException {
 
 		PafSaveNotesResponse saveNotesResponse = new PafSaveNotesResponse();
 
 		saveNotesResponse.setSuccess(false);
 
 		try {
 			
 			//Set logger client id field to user name
 			pushToNDCStack(saveNotesRequest.getClientId());
 
 			synchronized (PafServiceProvider.class) {
 			
 				SimpleCellNote[] newNotes = saveNotesRequest.getAddNotes();
 				SimpleCellNote[] updatedNotes = saveNotesRequest.getUpdateNotes();
 				SimpleCoordList deleteNoteIntersections = saveNotesRequest.getDeleteNoteIntersections();			
 				
 				CellNoteCache cnc = CellNoteCacheManager.getInstance().getNoteCache(saveNotesRequest.getClientId());
 							
 				List<SimpleCellNote> notesToSaveList = new ArrayList<SimpleCellNote>();
 				
 				// retrieve notes from cache to be deleted by simple coord list
 				if (deleteNoteIntersections != null) {
 					
 					deleteNoteIntersections.uncompressData();
 					
 					//cnc.deleteNotes(cnc.getNotes(deleteNoteIntersections));
 				}
 	
 				if ( newNotes != null && newNotes.length > 0 ) {
 									
 					for (int i = 0; i < newNotes.length; i++ ) {
 					
 						newNotes[i].uncompressData();
 						
 						notesToSaveList.add(newNotes[i]);
 						
 					}
 					
 					//cnc.saveNotes(newNotes);
 				}
 				
 				if ( updatedNotes != null && updatedNotes.length > 0 ) {
 					
 					
 					for (int i = 0; i < updatedNotes.length; i++ ) {
 						
 						updatedNotes[i].uncompressData();
 						
 						notesToSaveList.add(updatedNotes[i]);
 						
 					}
 					
 					//cnc.saveNotes(updatedNotes);
 				}
 				
 				SimpleCellNote[] simpleCellNotesToSave = null;
 				
 				if ( notesToSaveList.size() > 0 ) {
 					
 					simpleCellNotesToSave = notesToSaveList.toArray(new SimpleCellNote[0]);
 					
 				}
 				
 				//add/update/delete
 				cnc.processNotes(simpleCellNotesToSave, cnc.getNotes(deleteNoteIntersections));
 				
 				//TTN-1029
 				cnc.refreshCache();
 			
 				saveNotesResponse.setSuccess(true);
 			}			
 						
 		}catch (PafException pex) {
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 		}catch (RuntimeException re) {
 			handleRuntimeException(re);
 		}finally{
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(saveNotesRequest.getClientId());
 		}
 
 		return saveNotesResponse;
 
 	}
 	
 	/**
 	 * Querys the cache db and gets the number of cell notes per app/data source.
 	 * 
 	 * @parm pafRequest	The request
 	 * @return PafCellNoteInformationResponse
 	 */
 	public PafCellNoteInformationResponse getCellNotesInformation(PafRequest pafRequest) 
 		throws RemoteException, PafSoapException {
 		
 		PafCellNoteInformationResponse response = new PafCellNoteInformationResponse();
 		
 		try {
 			
 			//Set logger client id field to user name
 			pushToNDCStack(pafRequest.getClientId());
 			
 			CellNotesInformation[] cellNotesInformationAr = PafCellNoteManager.getInstance().getCellNotesInformation();
 			
 			response.setCellNotesInformationAr(cellNotesInformationAr);	
 		
 		} catch (RuntimeException re) {
 			
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 			
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 			
 		} finally {
 			
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(pafRequest.getClientId());
 			
 		}
 		
 		return response;
 		
 	}	
 	
 	/**
 	 * Gets simple cell notes to export.
 	 * 
 	 * @parm pafSimpleCellNoteExportRequest	The request to export the cell notes.
 	 * @return PafSimpleCellNoteExportResponse
 	 */
 	public PafSimpleCellNoteExportResponse getSimpleCellNotesToExport(PafSimpleCellNoteExportRequest pafSimpleCellNoteExportRequest) throws RemoteException, PafSoapException {
 		
 		PafSimpleCellNoteExportResponse response = new PafSimpleCellNoteExportResponse();
 		
 		try {
 			
 			//Set logger client id field to user name
 			pushToNDCStack(pafSimpleCellNoteExportRequest.getClientId());
 			
 			CellNotesInformation[] cellNotesInformationToExport = pafSimpleCellNoteExportRequest.getCellNoteInformationAr();
 			
 			if ( cellNotesInformationToExport != null && cellNotesInformationToExport[0] != null ) {
 				
 				List<SimpleCellNote> simpleCellNoteList = new ArrayList<SimpleCellNote>();
 				
 				for (CellNotesInformation cellNoteInfoToExport : cellNotesInformationToExport ) {
 					
 					CellNote[] cellNotesToExport = PafCellNoteManager.getInstance().getCellNotes(cellNoteInfoToExport.getApplicationId(), cellNoteInfoToExport.getDataSourceId());
 					
 					if ( cellNotesToExport != null ) {
 					
 						for (CellNote cellNoteToExport : cellNotesToExport ) {
 							
 							//convert cell note to simple cell notes and then add to list
 							
 							SimpleCellNote simpleCellNote = cellNoteToExport.getSimpleCellNote();
 							
 							//clear id's
 							simpleCellNote.setId(-1);
 							
 							simpleCellNoteList.add(simpleCellNote);
 							
 						}
 						
 					}
 					
 				}	
 				
 				//set simple cell notes to export on response
 				response.setSimpleCellNotesToExport(simpleCellNoteList.toArray(new SimpleCellNote[0]));
 				
 			}
 		
 		} catch (RuntimeException re) {
 			
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 			
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 			
 		} finally{
 			
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(pafSimpleCellNoteExportRequest.getClientId());
 			
 		}
 		
 		
 		return response;
 		
 	}
 	
 	/**
 	 * Import Simple Cell Notes
 	 * 
 	 * @parm PafSimpleCellNoteImportRequest	The request to import simple cell notes.
 	 * @return PafSimpleCellNoteImportResponse
 	 */
 	public PafSimpleCellNoteImportResponse importSimpleCellNotes(PafSimpleCellNoteImportRequest pafSimpleCellNoteImportRequest) throws RemoteException, PafSoapException
 	{
 	
 		PafSimpleCellNoteImportResponse response = new PafSimpleCellNoteImportResponse();
 		
 		try {
 			
 			//Set logger client id field to user name
 			pushToNDCStack(pafSimpleCellNoteImportRequest.getClientId());
 			
 			//get simple cell notes to import from request
 			SimpleCellNote[] simpleCellNotesToImport = pafSimpleCellNoteImportRequest.getSimpleCellNotes();
 			
 			//save cell notes
 			PafCellNoteManager.getInstance().saveCellNotes(simpleCellNotesToImport);
 			
 			//made this far, set as success
 			response.setSuccess(true);
 		
 		}catch (RuntimeException re) {
 			
 			handleRuntimeException(re);
 			
 		} catch (PafException e) {
 			
 			logger.error(e.getMessage());
 			
 			throw e.getPafSoapException();
 			
 		}finally{
 			
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(pafSimpleCellNoteImportRequest.getClientId());
 			
 		}
 		
 		return response;
 	}
 
 	/**
 	 *  Clear member tag data for the specified application(s) and member tags
 	 *
 	 * @param filteredMbrTagsRequest Contains optional app/member tag filter
 	 * 
 	 * @return PafSuccessResponse
 	 * @throws RemoteException, PafSoapException, PafNotAuthorizedSoapException, PafNotAuthenticatedSoapException 
 	 */
 	public PafSuccessResponse clearMemberTagData(PafFilteredMbrTagRequest filteredMbrTagsRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 		
 		PafSuccessResponse response = new PafSuccessResponse(false);
 		
 		try {
 			if(isAuthorized(filteredMbrTagsRequest.getClientId(), false)){
 				
 				//Set logger client id field to user name
 				pushToNDCStack(filteredMbrTagsRequest.getClientId());
 				
 				// Get member tag info
 				PafMemberTagManager.getInstance().clearMemberTagData(filteredMbrTagsRequest.getMemberTagFilters());
 				response.setSuccess(true);
 			}			
 
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 
 		} catch (PafException pex) {
 			
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 			
 		} finally {
 
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(filteredMbrTagsRequest.getClientId());
 
 		}
 
 		return response;
 	}
 
 
 	/**
 	 *  Clear member tag data for the specified application(s) and member tags
 	 *
 	 * @param filteredMbrTagsRequest Contains optional app/member tag filter
 	 * 
 	 * @return PafSuccessResponse
 	 * @throws RemoteException, PafSoapException, PafNotAuthorizedSoapException, PafNotAuthenticatedSoapException 
 	 */
 	public PafGetMemberTagDataResponse exportMemberTagData(PafFilteredMbrTagRequest filteredMbrTagsRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 
 	PafGetMemberTagDataResponse response = new PafGetMemberTagDataResponse();
 	SimpleMemberTagData[] memberTagData = null;
 	
 	try {
 			if(isAuthorized(filteredMbrTagsRequest.getClientId(), false)){
 				
 				//Set logger client id field to user name
 				pushToNDCStack(filteredMbrTagsRequest.getClientId());
 				
 				// Get member tag info
 				memberTagData = PafMemberTagManager.getInstance().exportMemberTagData(filteredMbrTagsRequest.getMemberTagFilters());
 				response.setMemberTagData(memberTagData);
 			}			
 
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 
 		} catch (PafException pex) {
 			
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 			
 		} finally {
 
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(filteredMbrTagsRequest.getClientId());
 
 		}
 
 		return response;
 	}
 
 
 	/**
 	 *  Get all or specified member tag definitions
 	 *
 	 * @param memberTagDefsRequest Contains an optional member tag filter
 	 * 
 	 * @return PafGetMemberTagDefsResponse
 	 * @throws RemoteException, PafSoapException, PafNotAuthorizedSoapException, PafNotAuthenticatedSoapException 
 	 */
 	public PafGetMemberTagDefsResponse getMemberTagDefs(PafGetMemberTagDefsRequest memberTagDefsRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 
 		PafGetMemberTagDefsResponse response = new PafGetMemberTagDefsResponse() ;
 		String clientId = null;
 		
 		try {
 			if(isAuthorized(memberTagDefsRequest.getClientId(), false)){
 				
 				// Get client id
 				clientId = memberTagDefsRequest.getClientId();
 				
 				//Set logger client id field to user name
 				pushToNDCStack(clientId);
 				
 				// Get member tag defintions
 				String appId = clients.get(clientId).getApp().getAppId();
 				MemberTagDef[] memberTagDefs = appService.getMemberTagDefs(appId, memberTagDefsRequest.getMemberTagNames());
 				response.setMemberTagDefs(memberTagDefs);
 			}			
 
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 
 		} finally {
 
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(clientId);
 
 		}
 		
 		return response;
 	}
 
 
 	/**
 	 *	Get all member tag defs for the specified application
 	 *
 	 * @param app Current application definition
 	 *
 	 * @return MemberTagDef[]
 	 */
 	private MemberTagDef[] getMemberTagDefs(PafApplicationDef app) {
 		
 		Map<String, MemberTagDef> memberTagDefs = app.getMemberTagDefs();
 	
 		return memberTagDefs.values().toArray(new MemberTagDef[0]);
 	}
 	
 
 	/**
 	 *  Get member tag statistics
 	 *
 	 * @param filteredMbrTagsRequest Contains optional app/member tag filter
 	 * 
 	 * @return PafGetMemberTagInfoResponse
 	 * @throws RemoteException, PafSoapException, PafNotAuthorizedSoapException, PafNotAuthenticatedSoapException
 	 */
 	public PafGetMemberTagInfoResponse getMemberTagInfo(PafFilteredMbrTagRequest filteredMbrTagsRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 		
 		PafGetMemberTagInfoResponse response = null;
 		
 		try {
 			if(isAuthorized(filteredMbrTagsRequest.getClientId(), false)){
 				
 				//Set logger client id field to user name
 				pushToNDCStack(filteredMbrTagsRequest.getClientId());
 				
 				// Get member tag info
 				response = PafMemberTagManager.getInstance().getMemberTagInfo(
 						filteredMbrTagsRequest.getMemberTagFilters());
 			}			
 
 		} catch (RuntimeException re) {
 
 			handleRuntimeException(re);
 
 		} catch (PafException pex) {
 			
 			PafErrHandler.handleException(pex);			
 			throw pex.getPafSoapException();
 			
 		} finally {
 
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(filteredMbrTagsRequest.getClientId());
 
 		}
 
 		return response;
 	}
 
 
 	/**
 	 *  Import data into the member tag database
 	 *
 	 * @param importMemberTagRequest Imported member tag data
 	 * 
 	 * @return PafSuccessResponse
 	 * @throws RemoteException, PafSoapException, PafNotAuthorizedSoapException, PafNotAuthenticatedSoapException 
 	 */
 	public PafSuccessResponse importMemberTagData(PafImportMemberTagRequest importMemberTagRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 
 		PafSuccessResponse response = new PafSuccessResponse(false);
 		
 		try {
 			if(isAuthorized(importMemberTagRequest.getClientId(), false)){
 
 				//Set logger client id field to user name
 				pushToNDCStack(importMemberTagRequest.getClientId());
 				
 				// Uncompress import records
 				SimpleMemberTagData[] importMemberTags = CompressionUtil.tryToUncompress(importMemberTagRequest.getMemberTagData());
 
 				// Import member tag data
 				PafMemberTagManager mtManager = PafMemberTagManager.getInstance();
 				logger.info("Importing member tag data...");
 				mtManager.importMemberTagData(importMemberTags);
 				response.setSuccess(true);
 			}			
 			
 		} catch (RuntimeException re) {
 			
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 			
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 			
 		} finally {
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(importMemberTagRequest.getClientId());
 		}
 
 		return response;
 
 	}
 
 
 	/* (non-Javadoc)
 	 * @see com.pace.base.server.IPafService#renameMemberTagData(com.pace.base.server.comm.PafFilteredMbrTagRequest)
 	 */
 	public PafSuccessResponse renameMemberTagData(PafFilteredMbrTagRequest filteredMbrTagsRequest) throws RemoteException, PafSoapException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	/**
 	 *  Save updates to the member tag database
 	 *
 	 * @param saveMbrTagsRequest Member tag additions, updates, and deletions
 	 * 
 	 * @return PafSuccessResponse
 	 * @throws RemoteException, PafSoapException, PafNotAuthorizedSoapException, PafNotAuthenticatedSoapException 
 	 */
 	public PafSuccessResponse saveMemberTagData(PafSaveMbrTagRequest saveMbrTagsRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException {
 
 		PafSuccessResponse response = new PafSuccessResponse(false);
 		
 		try {
 			if(isAuthorized(saveMbrTagsRequest.getClientId(), false)){
 
 				//Set logger client id field to user name
 				pushToNDCStack(saveMbrTagsRequest.getClientId());
 				
 				// Uncompress update records
 				SimpleMemberTagData[] addMemberTags = CompressionUtil.tryToUncompress(saveMbrTagsRequest.getAddMemberTags());
 				SimpleMemberTagData[] updateMemberTags = CompressionUtil.tryToUncompress(saveMbrTagsRequest.getUpdateMemberTags());
 				SimpleMemberTagData[] deleteMemberTags = CompressionUtil.tryToUncompress(saveMbrTagsRequest.getDeleteMemberTags());
 
 				// Save updates to member tag data
 				PafMemberTagManager mtManager = PafMemberTagManager.getInstance();
 				mtManager.saveMemberTagData(addMemberTags, updateMemberTags, deleteMemberTags);
 				response.setSuccess(true);
 			}			
 			
 		} catch (RuntimeException re) {
 			
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 			
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 			
 		} finally {
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(saveMbrTagsRequest.getClientId());
 		}
 
 		return response;
 	}
 
 
 	/**
 	 * 
 	 * Closes a clients session by removing Client State from the clients map.
 	 * 
 	 * @param pafRequest request
 	 * @return PafSuccessResponse success or not
 	 * @throws RemoteException 
 	 */
 	public PafSuccessResponse closeClientSession(PafRequest pafRequest) throws RemoteException {
 
 		PafSuccessResponse response = new PafSuccessResponse();
 		
 		if ( pafRequest != null ) {
 			
 			String clientId = pafRequest.getClientId();
 						
 			if ( clients != null && clients.containsKey(clientId) ) {
 				
 				logger.info("Closing client connection for client id "+ clientId +".");
 				
 				clients.remove(clientId);
 				
 				response.setSuccess(! clients.containsKey(clientId));
 				
 			}
 			
 		}
 		
 		return response;
 	}
 
 
 
 	/**
 	 * 
 	 * Checks to see if a client session is active by using the client id
 	 * TTN-1160
 	 * 
 	 * @param pafRequest request
 	 * @return PafSuccessResponse success or not
 	 * @throws RemoteException 
 	 */
 	public PafSuccessResponse isSessionActive(PafRequest pafRequest) throws RemoteException {
 
 		PafSuccessResponse response = new PafSuccessResponse();
 		
 		if ( pafRequest != null ) {
 		
 			String clientId = pafRequest.getClientId();
 			
 			if ( clientId != null ) {
 				
 				response.setSuccess(clients !=null && clients.containsKey(clientId));
 				
 			}
 			
 		}
 		
 		return response;
 	}
 	
 	
 	private void listCookies(String clientId) {
 		MessageContext mc = wsCtx.getMessageContext();		   
 		//HttpSession session = ((javax.servlet.http.HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST)).getSession();
 		HttpServletRequest req = (HttpServletRequest)mc.get(mc.SERVLET_REQUEST);
 
 	   Cookie cookies[] = req.getCookies();
 	   if (cookies != null) {
 		   logger.debug("Listing Cookies in Session (" + clientId + ")");		   
 		   for (Cookie c : cookies) {
 			   logger.debug( c.getName() + ":" + c.getValue() );
 		   }
 	   }
 	   else {
 		   logger.debug("Cookies are null in session");
 	   }		
 	}
 	
 	
 
 }
