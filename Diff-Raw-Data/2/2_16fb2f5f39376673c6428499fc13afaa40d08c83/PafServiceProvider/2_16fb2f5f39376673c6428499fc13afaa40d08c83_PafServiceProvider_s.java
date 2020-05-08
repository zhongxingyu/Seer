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
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.activation.DataHandler;
 import javax.annotation.Resource;
 import javax.jws.WebService;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 
 import org.apache.commons.math3.stat.clustering.Cluster;
 import org.apache.commons.math3.stat.clustering.EuclideanIntegerPoint;
 import org.apache.log4j.Logger;
 import org.apache.log4j.NDC;
 import org.springframework.util.StopWatch;
 
 import com.pace.base.AuthMode;
 import com.pace.base.InvalidPasswordException;
 import com.pace.base.InvalidUserNameException;
 import com.pace.base.NoEmailAddressException;
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrHandler;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.PafInvalidLogonInformation;
 import com.pace.base.PafNotAbletoGetLDAPContext;
 import com.pace.base.PafNotAuthenticatedSoapException;
 import com.pace.base.PafNotAuthorizedSoapException;
 import com.pace.base.PafSecurityToken;
 import com.pace.base.PafSoapException;
 import com.pace.base.app.AppSettings;
 import com.pace.base.app.MdbDef;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.PafDimSpec;
 import com.pace.base.app.PafDimSpec2;
 import com.pace.base.app.PafPlannerRole;
 import com.pace.base.app.PafSecurityDomainGroups;
 import com.pace.base.app.PafSecurityDomainUserNames;
 import com.pace.base.app.PafSecurityGroup;
 import com.pace.base.app.PafUserDef;
 import com.pace.base.app.PafUserNamesSecurityGroup;
 import com.pace.base.app.Season;
 import com.pace.base.app.SeasonList;
 import com.pace.base.app.UnitOfWork;
 import com.pace.base.app.VersionDef;
 import com.pace.base.app.VersionType;
 import com.pace.base.comm.ApplicationStateRequest;
 import com.pace.base.comm.ApplicationStateResponse;
 import com.pace.base.comm.ClientInitRequest;
 import com.pace.base.comm.DataFilterSpec;
 import com.pace.base.comm.DownloadAppRequest;
 import com.pace.base.comm.DownloadAppResponse;
 import com.pace.base.comm.EvaluateViewRequest;
 import com.pace.base.comm.LoadApplicationRequest;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.comm.PafRequest;
 import com.pace.base.comm.PafResponse;
 import com.pace.base.comm.PafSuccessResponse;
 import com.pace.base.comm.PafViewTreeItem;
 import com.pace.base.comm.SimpleCoordList;
 import com.pace.base.comm.StartApplicationRequest;
 import com.pace.base.comm.UploadAppRequest;
 import com.pace.base.comm.UploadAppResponse;
 import com.pace.base.comm.UserFilterSpec;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.PafDataSlice;
 import com.pace.base.db.SecurityGroup;
 import com.pace.base.db.cellnotes.CellNote;
 import com.pace.base.db.cellnotes.CellNotesInformation;
 import com.pace.base.db.cellnotes.SimpleCellNote;
 import com.pace.base.db.membertags.MemberTagDef;
 import com.pace.base.db.membertags.SimpleMemberTagData;
 import com.pace.base.mdb.AttributeUtil;
 import com.pace.base.mdb.IPafConnectionProps;
 import com.pace.base.mdb.PafAttributeTree;
 import com.pace.base.mdb.PafBaseTree;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDataSliceParms;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.mdb.PafSimpleDimTree;
 import com.pace.base.mdb.TreeTraversalOrder;
 import com.pace.base.project.InvalidPaceProjectInputException;
 import com.pace.base.project.PaceProject;
 import com.pace.base.project.PaceProjectCreationException;
 import com.pace.base.project.ProjectElementId;
 import com.pace.base.project.ProjectSaveException;
 import com.pace.base.project.XMLPaceProject;
 import com.pace.base.rules.RuleGroup;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.server.PafLDAPSettings;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.AESEncryptionUtil;
 import com.pace.base.utility.CompressionUtil;
 import com.pace.base.utility.DataHandlerPaceProjectUtil;
 import com.pace.base.utility.DomainNameParser;
 import com.pace.base.utility.LogUtil;
 import com.pace.base.view.PafMVS;
 import com.pace.base.view.PafStyle;
 import com.pace.base.view.PafView;
 import com.pace.base.view.PafViewSection;
 import com.pace.db.DataStore;
 import com.pace.server.assortment.AsstSet;
 import com.pace.server.comm.AttributeDimInfo;
 import com.pace.server.comm.ClusterResultSetRequest;
 import com.pace.server.comm.ClusteredResultSetResponse;
 import com.pace.server.comm.ClusteredResultSetSaveRequest;
 import com.pace.server.comm.CreateAsstRequest;
 import com.pace.server.comm.CreateAsstResponse;
 import com.pace.server.comm.PaceDescendantsRequest;
 import com.pace.server.comm.PaceDescendantsResponse;
 import com.pace.server.comm.PaceQueryRequest;
 import com.pace.server.comm.PaceResultSetResponse;
 import com.pace.server.comm.PafAuthRequest;
 import com.pace.server.comm.PafAuthResponse;
 import com.pace.server.comm.PafCellNoteInformationResponse;
 import com.pace.server.comm.PafClearImportedAttrRequest;
 import com.pace.server.comm.PafClearImportedAttrResponse;
 import com.pace.server.comm.PafClientCacheBlock;
 import com.pace.server.comm.PafClientCacheRequest;
 import com.pace.server.comm.PafClientChangePasswordRequest;
 import com.pace.server.comm.PafClientSecurityPasswordResetResponse;
 import com.pace.server.comm.PafClientSecurityRequest;
 import com.pace.server.comm.PafClientSecurityResponse;
 import com.pace.server.comm.PafCommandResponse;
 import com.pace.server.comm.PafCustomCommandRequest;
 import com.pace.server.comm.PafCustomCommandResponse;
 import com.pace.server.comm.PafFilteredMbrTagRequest;
 import com.pace.server.comm.PafGetFilteredUOWSizeRequest;
 import com.pace.server.comm.PafGetFilteredUOWSizeResponse;
 import com.pace.server.comm.PafGetMemberTagDataResponse;
 import com.pace.server.comm.PafGetMemberTagDefsRequest;
 import com.pace.server.comm.PafGetMemberTagDefsResponse;
 import com.pace.server.comm.PafGetMemberTagInfoResponse;
 import com.pace.server.comm.PafGetNotesRequest;
 import com.pace.server.comm.PafGetNotesResponse;
 import com.pace.server.comm.PafGetPaceGroupsRequest;
 import com.pace.server.comm.PafGetPaceGroupsResponse;
 import com.pace.server.comm.PafGroupSecurityRequest;
 import com.pace.server.comm.PafGroupSecurityResponse;
 import com.pace.server.comm.PafImportAttrRequest;
 import com.pace.server.comm.PafImportAttrResponse;
 import com.pace.server.comm.PafImportMemberTagRequest;
 import com.pace.server.comm.PafMdbPropsRequest;
 import com.pace.server.comm.PafMdbPropsResponse;
 import com.pace.server.comm.PafPlanSessionRequest;
 import com.pace.server.comm.PafPlanSessionResponse;
 import com.pace.server.comm.PafPopulateRoleFilterResponse;
 import com.pace.server.comm.PafSaveMbrTagRequest;
 import com.pace.server.comm.PafSaveNotesRequest;
 import com.pace.server.comm.PafSaveNotesResponse;
 import com.pace.server.comm.PafServerAck;
 import com.pace.server.comm.PafSetPaceGroupsRequest;
 import com.pace.server.comm.PafSetPaceGroupsResponse;
 import com.pace.server.comm.PafSimpleCellNoteExportRequest;
 import com.pace.server.comm.PafSimpleCellNoteExportResponse;
 import com.pace.server.comm.PafSimpleCellNoteImportRequest;
 import com.pace.server.comm.PafSimpleCellNoteImportResponse;
 import com.pace.server.comm.PafTreeRequest;
 import com.pace.server.comm.PafTreeResponse;
 import com.pace.server.comm.PafTreesRequest;
 import com.pace.server.comm.PafTreesResponse;
 import com.pace.server.comm.PafUpdateDatacacheRequest;
 import com.pace.server.comm.PafUserNamesforSecurityGroupsRequest;
 import com.pace.server.comm.PafUserNamesforSecurityGroupsResponse;
 import com.pace.server.comm.PafValidAttrRequest;
 import com.pace.server.comm.PafValidAttrResponse;
 import com.pace.server.comm.PafVerifyUsersRequest;
 import com.pace.server.comm.PafVerifyUsersResponse;
 import com.pace.server.comm.PafViewRequest;
 import com.pace.server.comm.SaveWorkRequest;
 import com.pace.server.comm.SimpleMeasureDef;
 import com.pace.server.comm.SimpleVersionDef;
 import com.pace.server.comm.StringRow;
 import com.pace.server.comm.ValidateUserSecurityRequest;
 import com.pace.server.comm.ValidationResponse;
 import com.pace.server.comm.ViewRequest;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class PafServiceProvider.
  *
  * @author Jwatkins
  * @version	x.xx
  */
 
 @WebService
 public class PafServiceProvider implements IPafService {
 
 	// injected handle to the web service context
 	/** The ws ctx. */
 	@Resource
 	WebServiceContext wsCtx;	
 	
 	/** The view service. */
 	private PafViewService viewService = PafViewService.getInstance();
 	
 	/** The data service. */
 	private PafDataService dataService = PafDataService.getInstance();
 	
 	/** The app service. */
 	private PafAppService appService = PafAppService.getInstance();
 	
 	/** The server platform. */
 	private static String serverPlatform = null;
 	
 	/** The logger. */
 	private static Logger logger = Logger.getLogger(PafServiceProvider.class);
 	
 	/** The log audit. */
 	private static Logger logAudit = Logger.getLogger(Messages.getString("PafServiceProvider.12")); //$NON-NLS-1$
 	
 	private static Logger logPerf = Logger.getLogger(Messages.getString("PafServiceProvider.14")); //$NON-NLS-1$
 	
 	/** The clients. */
 	private static ConcurrentHashMap<String, PafClientState> clients = new ConcurrentHashMap<String, PafClientState>();
 	
 	/** Assortment planning objects */
 	private static ConcurrentHashMap<String, Integer> assortments = new ConcurrentHashMap<String, Integer>(); // assortmentLabel, slot
 	private static ConcurrentHashMap<Integer, String> assortSlots = new ConcurrentHashMap<Integer, String>(); // slot, assortmentLabel
 	
 	private DataStore dataStore = new DataStore();
 
 	/**
 	 * Instantiates a new paf service provider.
 	 */
 	public PafServiceProvider() {
 		try {
 			// initilize planform information
 			if (serverPlatform == null) {
 				serverPlatform = System.getProperty(Messages.getString("PafServiceProvider.0")) + Messages.getString("PafServiceProvider.1") //$NON-NLS-1$ //$NON-NLS-2$
 				+ System.getProperty(Messages.getString("PafServiceProvider.2")) + Messages.getString("PafServiceProvider.3") //$NON-NLS-1$ //$NON-NLS-2$
 				+ System.getProperty(Messages.getString("PafServiceProvider.4")) + Messages.getString("PafServiceProvider.5") //$NON-NLS-1$ //$NON-NLS-2$
 				+ System.getProperty(Messages.getString("PafServiceProvider.6")); //$NON-NLS-1$
 			
 				logger.info(Messages.getString("PafServiceProvider.21")); //$NON-NLS-1$
 			}
 			
 			
 			
 		} catch (Throwable t) {
 			// don't do anything as all error handling should have been handled elsewhere
 			// otherwise the app server thinks something went wrong.
 			logger.fatal(Messages.getString("PafServiceProvider.24")); //$NON-NLS-1$
 		}
 	}
 	
 	
 
 	
 	/**
 	 * Export all view definitions to xml.
 	 */
 	public void saveViewCache() {
 		viewService.saveViewCache();
 	}
 
 	/**
 	 * Get paf server version.
 	 *
 	 * @return Version ID
 	 */
 	public String getVersion() {
 		return (PafServerConstants.SERVER_VERSION);
 	}
 
 	/**
 	 * Get Client View.
 	 *
 	 * @param viewRequest View request
 	 * @return PafView
 	 * @throws PafSoapException the paf soap exception
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
 				logger.error(Messages.getString("PafServiceProvider.25") + viewRequest.getClientId()); //$NON-NLS-1$
 				throw new PafSoapException(new PafException(Messages.getString(Messages.getString("PafServiceProvider.31")), PafErrSeverity.Error));			 //$NON-NLS-1$
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
 				String errMsg = Messages.getString("PafServiceProvider.34"); //$NON-NLS-1$
 				logger.error(errMsg);
 				throw new PafException(errMsg, PafErrSeverity.Error);
 			}
 	
 			// Compress each cloned view section
 			if(viewRequest.isCompressResponse() == true){
 				for(PafViewSection viewSection : compressedView.getViewSections()){
 					if( ! viewSection.isEmpty() ) {
 						try {
 							viewSection.compressData();
 						} catch (IOException e) {
 							String errMsg = Messages.getString("PafServiceProvider.35"); //$NON-NLS-1$
 							logger.error(errMsg);
 							throw new PafException(errMsg, PafErrSeverity.Error);
 						}
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
 
 
 	
 	/* (non-Javadoc)
  * @see com.pace.server.IPafService#reinitializeClientState(com.pace.base.comm.PafRequest)
  */
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
 	
 	/**
 	 * Reinitialize client state.
 	 *
 	 * @param clientId the client id
 	 */
 	private void reinitializeClientState(String clientId) {
 
 		ClientInitRequest pcInit = clients.get(clientId).getInitRequest();
 		PafApplicationDef paDef = clients.get(clientId).getApp();
 		PafSecurityToken pToken = clients.get(clientId).getSecurityToken();
 		
 		PafClientState state = new PafClientState(clientId, pcInit, PafMetaData.getPaceHome(), PafMetaData.getTransferDirPath(), PafMetaData.isDebugMode());
 		state.setApp(paDef);
 		state.setMdbBaseTrees(dataService.getBaseTrees()); // TTN-1598
 		state.setSecurityToken(pToken);
 		
 		clients.put(clientId, state);
 	}
 	
 
 	/**
 	 * Begin initial tracking of a client upon receiving this request generates
 	 * a unique id, but is not actually authenticated to perform any operations.
 	 *
 	 * @param pcInit Client init request
 	 * @return PafServerAck
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafServerAck clientInit(ClientInitRequest pcInit)
 			throws RemoteException, PafSoapException {
 
 		PafServerAck ack = null;
 	
 		try {
 
 			String clientId = String.valueOf(Math.random());
 			
 			// Display client initialization message
 			String s = String.format(Messages.getString("PafServiceProvider.15"), pcInit.getIpAddress(), pcInit.getClientType(), pcInit.getClientVersion()); //$NON-NLS-1$
 			logger.info(s);
 						
 			// block to debug load balancer cookies.
 			listCookies(clientId);
 			
 			// check for app service to have started.
 			if (appService == null) {
 				PafException pexFailedAppInit = new PafException(Messages.getString("PafServiceProvider.37"), PafErrSeverity.Fatal); //$NON-NLS-1$
 				PafErrHandler.handleException(pexFailedAppInit);
 				throw pexFailedAppInit.getPafSoapException();
 			}
 
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
 			
 			PafApplicationDef pafApplicationDef = null;
 			
 			if ( appService.getApplications().size() > 0 ) {
 				pafApplicationDef = appService.getApplications().get(0);
 			}
 			
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
 	 * Perform client authorization.
 	 *
 	 * @param authReq Authorization request
 	 * @return PafAuthResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
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
 				logger.error(Messages.getString("PafServiceProvider.39") + clientId); //$NON-NLS-1$
 				throw new PafSoapException(new PafException(Messages.getString(Messages.getString("PafServiceProvider.40")), PafErrSeverity.Error));			 //$NON-NLS-1$
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
 				String appId = PafMetaData.getPaceProject().getApplicationDefinitions().get(0).getAppId();
 				
 				// Set planner configurations and roles properties
 				PafPlannerRole[] plannerRoles = PafSecurityService.getPlannerRoles(token, appId);
 				response.setPlannerRoles(plannerRoles);				
 				response.setPlannerConfigs(findPafPlannerConfig(plannerRoles ));
 							
 				// Re-load application meta-data if debugMode is set to true (TTN-1367)	
 				if (PafMetaData.isDebugMode()) {			
 					appService.loadApplicationConfigurations();
 				}
 				
 				List<PafApplicationDef> pafAppList = PafAppService.getInstance().getApplications();
 				
 				PafApplicationDef currentPafApp = null;
 				
 				for (PafApplicationDef pafAppDef : pafAppList ) {
 					if ( pafAppDef.getAppId().equals(appId)) {
 						currentPafApp = pafAppDef;
 						break;
 					}
 				}
 				
 				// Populate client state appDef property
 				clients.get(authReq.getClientId()).setApp(currentPafApp);
 				
 				// Populate client state baseTrees property (TTN-1598)
 				clients.get(authReq.getClientId()).setMdbBaseTrees(dataService.getBaseTrees());
 				
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
 	 * Start Planning Session.
 	 *
 	 * @param planRequest Plan session response
 	 * @return PafPlanSessionResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	// TODO Refactor startPlanSession to be cleaner, less arrayList to array
 	// behavior, and wrap some operations
 	@SuppressWarnings({"unchecked" }) //$NON-NLS-1$ //$NON-NLS-2$
 	public PafPlanSessionResponse startPlanSession(PafPlanSessionRequest planRequest) throws RemoteException, PafSoapException {
 
 		PafPlanSessionResponse planResponse = new PafPlanSessionResponse();
 		long planSessionStart = System.currentTimeMillis();
 
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
 			
 			// Create client state locked period collections (TTN-1595)
 			appService.populateLockedPeriodCollections(clientState);
 			
 			// Echo locked periods to server log (TTN-1595)
 			Map<String, Set<String>> lockedPeriodMap = clientState.getLockedPeriodMap();
 			for (String year : lockedPeriodMap.keySet()) {
 				Set<String> lockedPeriods = lockedPeriodMap.get(year);
 				logger.info(Messages.getString("PafServiceProvider.48") + year + Messages.getString("PafServiceProvider.49") + lockedPeriods); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 			
 
 			// Create member index lists on each dimension - used to sort allocation
 			// intersections in evaluation processing (TTN-1391)
 			Map<String, Map<String, Integer>> memberIndexLists = dataService.getUowMemberIndexLists(treeSet);
 			clientState.setMemberIndexLists(memberIndexLists);
 			
 			// calculate dynamic rule sets for the client
 			RuleSet[] fullRuleSets = RuleMngr.getInstance().calculateRuleSets(treeSet, app, pafPlannerConfig);
 
 			Map<String, List<RuleSet>> fullRuleSetMap = new HashMap<String, List<RuleSet>>();
 			List<RuleSet> clientRuleSetList = new ArrayList<RuleSet>();
 			List<RuleSet> ruleSetList = new ArrayList<RuleSet>();
 
 			// 1st remove any rule sets that the user doesn't have access too
 			// based upon the planner configuration
 
 			for (RuleSet rs : fullRuleSets) {
 				// prune out protProcSkip rule groups from measure's rule set
 
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
 
 			// Load data cache based on unit of work
 			dataService.loadUowCache(clientState);
 			
 			// need to set id of simple tree to root dimension, not default
 			// behavior
 			// key is normally root node. Then store in response object
 			PafSimpleDimTree simpleDimTree = null;
 			PafSimpleDimTree simpleDimTrees[] = new PafSimpleDimTree[treeSet.getTrees().size()];
 
 			// Get simple versions of each client member trees (Regular member trees
 			// can't be passed across the soap layer).
 			logger.info(Messages.getString("PafServiceProvider.19"));  //$NON-NLS-1$
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
 			clientState.setActiveVersions(getActiveVersions(clientState.getPlanningVersion()));
 			
 			
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
 			
 			//TTN-1957:	Add Lift Allocation configurability to Pace Server
 			//if getLiftEnabled is null, retrieve global setting from appset
 			//if Lift exists on planner config, use it, else if app settings 
 			//exists, use global, otherwise default to true
 			if ( pafPlannerConfig.getLiftEnabled() != null ) {
 				
 				planResponse.setLiftEnabled(pafPlannerConfig.getLiftEnabled());
 				
 			} else if ( clientState.getApp().getAppSettings() != null  ) {
 				
 				planResponse.setLiftEnabled(clientState.getApp().getAppSettings().isGlobalLiftEnabled());
 				
 			}
 
 			//if Lift All exists on planner config, use it, else if app settings 
 			//exists, use global, otherwise default to true
 			if ( pafPlannerConfig.getLiftAllEnabled() != null ) {
 				
 				planResponse.setLiftAllEnabled(pafPlannerConfig.getLiftAllEnabled());
 				
 			} else if ( clientState.getApp().getAppSettings() != null  ) {
 				
 				planResponse.setLiftAllEnabled(clientState.getApp().getAppSettings().isGlobalLiftAllEnabled());
 				
 			}
 
 			//get unit of work
 			UnitOfWork uow = clientState.getUnitOfWork();
 			
 			//only allow one user to create note cache at a time
 			synchronized (PafServiceProvider.class) {
 			
 				//create cell note cache for current clientId
 				CellNoteCacheManager.getInstance().createNoteCache(clientState, uow);
 			
 			}
 						
 			logger.info(LogUtil.timedStep("Plan session start" , planSessionStart));
 
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
 	 * Find the planner configurations for the roles available to a user.
 	 *
 	 * @param plannerRoles the planner roles
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
 	 * Find planner configuration for specified role and planCycle.
 	 *
 	 * @param roleName Role name
 	 * @param planCycle Plan cycle
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
 	 * Get all active planning versions.
 	 *
 	 * @param baseVersionDef Base version definition
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
 	 * object for a collection used by the client to populate the view menu.
 	 * 
 	 * @param viewTreeItemNames
 	 *            name of tree view items, could be view names or view group
 	 *            names
 	 * @return PafViewTreeItem[]
 	 * @throws PafException 
 	 */
 
 	private PafViewTreeItem[] generateViewTreeItemsFromViewTreeItemNames(String[] viewTreeItemNames) throws PafException  {
 
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
 		Map<String, String> invalidViewsMap = viewService
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
 	 * Return client cache block.
 	 *
 	 * @param cacheRequest the cache request
 	 * @return ClientCacheBlock
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
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
 	 * Method_description_goes_here.
 	 *
 	 * @return the simple version defs
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
 	 * Evaluate pending calculations on view section.
 	 *
 	 * @param evalRequest Evaluation request object
 	 * @return PafDataSlice Paf data slice
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafView evaluateView(EvaluateViewRequest evalRequest)
 			throws RemoteException, PafSoapException {
 
 		PafView pView = null;
 		PafView pViewEmpty = null;
 		String clientId = evalRequest.getClientId();
 		String stepDesc = null, logMsg = null;
 		Long startTime;
 
 
 		// Evaluate view
 		startTime = System.currentTimeMillis();
 		try {
 
 			// Set logger client info property to user name
 			pushToNDCStack(evalRequest.getClientId());
 					
 			// Troubleshoot load balancer cookies
 			listCookies(clientId);
 			
 			// Verify client id is good
 			if ( ! clients.containsKey( evalRequest.getClientId() ) ) {
 				logger.error(Messages.getString("PafServiceProvider.50") + evalRequest.getClientId()); //$NON-NLS-1$
 				throw new PafSoapException(new PafException(Messages.getString(Messages.getString("PafServiceProvider.51")), PafErrSeverity.Error));			 //$NON-NLS-1$
 			}		
 			
 			// Get client state
 			PafClientState clientState = clients.get(evalRequest.getClientId());
 
 			// update client state with ruleset used in this calculation
 			clientState.setCurrentMsrRulesetName(evalRequest.getRuleSetName());
 			
 			//dataSlice = dataService.evaluateView(evalRequest, clientState);
 			if (logAudit.isInfoEnabled() && evalRequest.getChangedCells() != null && evalRequest.getChangedCells().getCompressedData() != null) {
 				logAudit.info(
 						Messages.getString("PafServiceProvider.53") + clientState.getClientId() +  //$NON-NLS-1$
 						Messages.getString("PafServiceProvider.56") + evalRequest.getChangedCells().toString()); //$NON-NLS-1$
 			}
 			
 			
 			//Evaluate view
 			PafView currentView = clientState.getView(evalRequest.getViewName());
 			PafMVS pafMVS = clientState.getMVS(PafMVS.generateKey(currentView, currentView.getViewSections()[0]));
 			PafDataCache dataCache = pafMVS.getDataCache();
 			dataCache.setPafMVS(pafMVS);
 			PafDataSliceParms sliceParms = pafMVS.getDataSliceParms();
 			dataService.evaluateView(evalRequest, clientState, dataCache, sliceParms);
 			
 			//Set original user selections
 			PafView view = clientState.getView(evalRequest.getViewName());
 			evalRequest.setUserSelections(view.getUserSelections());
 			evalRequest.setRowsSuppressed(view.getViewSections()[0].getSuppressZeroSettings().getRowsSuppressed());
 			evalRequest.setColumnsSuppressed(view.getViewSections()[0].getSuppressZeroSettings().getColumnsSuppressed());
 			
 			pView = viewService.getView(evalRequest, clientState);
 			
 			// Construct an empty view to return to the client, if no view definition changes
 			// were detected
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
 		
 		// Log processing timings
 		stepDesc = String.format(Messages.getString("PafServiceProvider.85"), pView.getName()); //$NON-NLS-1$
 		logMsg = LogUtil.timedStep(stepDesc, startTime);
 		logger.info(logMsg);				
 		logPerf.info(logMsg);				
 
 		if (pViewEmpty != null){
 			return pViewEmpty;
 		}
 
 		
 		// Return full view
 		return pView;
 	}
 
 	/**
 	 * Method_description_goes_here.
 	 *
 	 * @param saveWorkRequest the save work request
 	 * @return the paf command response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
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
 	 * Method_description_goes_here.
 	 *
 	 * @param reloadRequest the reload request
 	 * @return the paf data slice
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	public PafDataSlice reloadDatacache(PafViewRequest reloadRequest)
 			throws RemoteException, PafSoapException {
 
 		PafDataSlice dataSlices[] = null;
 		long dcLoadStart = System.currentTimeMillis();
 
 		try {
 
 			// Set logger client info property to user name
 			pushToNDCStack(reloadRequest.getClientId());
 
 			PafClientState clientState = clients.get(reloadRequest
 					.getClientId());
 
 			// Reload data cache for specified client state
 			dataService.loadUowCache(clientState);
 
 			// run the default evaluation process 
 			dataService.evaluateDefaultRuleset(clientState);
 
 			// Retrieve the data slice for each view section if a view
 			// is currently open
 			if (reloadRequest.getViewName() == null ||
 					reloadRequest.getViewName().trim().equals("")) { //$NON-NLS-1$
 				logger.info(LogUtil.timedStep("UOW refresh request" , dcLoadStart));
 				return null;
 			}
 			PafView view = clientState.getView(reloadRequest.getViewName());
 						
 			if ( view != null ) {
 									
 				int sectionCount = view.getViewSections().length;
 				
 				dataSlices = new PafDataSlice[sectionCount];
 				for (int i = 0; i < sectionCount; i++) {
 					dataSlices[i] = dataService.getDataSlice(view, view.getViewSections()[i], clientState, true);
 				}
 			} else {
 				
 				throw (new PafException(Messages.getString("PafServiceProvider.36"), PafErrSeverity.Warning)).getPafSoapException(); //$NON-NLS-1$
 				
 			}
 			
 			logger.info(LogUtil.timedStep("UOW refresh request" , dcLoadStart));
 			
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
 	 * Refresh selected versions in data cache from the mdb
 	 * 
 	 * This method is typically called from the Pace Client after a custom action is run
 	 * with the <refreshUow> parameter set to true and the <refreshUowVersionFilter>
 	 * parameter utilized.
 	 *
 	 * @param updateRequest Update data cache request
 	 * @return PafDataSlice
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
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
 			Map<String, Map<Integer, List<String>>> updatedMdbDataSpec = dataService.refreshUowCache(clientState, clientState.getApp(),clientState.getUnitOfWork(), 
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
 
 				int sectionCount = view.getViewSections().length;
 
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
 	 * Return specified dimension tree.
 	 *
 	 * @param pafTreeRequest Tree request object (contains specified dimension name)
 	 * @return PafTreeResponse (contains requested dimension tree)
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
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
 	 * Get all dimension trees.
 	 *
 	 * @param pafTreesRequest Request object
 	 * @return PafTreesResponse (contains dimension trees)
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
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
 	 * Method_description_goes_here.
 	 *
 	 * @param cmdRequest the cmd request
 	 * @return the paf custom command response
 	 * @throws PafSoapException the paf soap exception
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
 	 * Ends the current planning session.  This method cleans up the UOW Cache
 	 * and then removes the client id from the map of clients.
 	 *
 	 * @param endSessionRequest the end session request
 	 * @return the paf response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
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
 			
 			logger.info(Messages.getString(Messages.getString("PafServiceProvider.57"))); //$NON-NLS-1$
 			
 		} else {
 			
 			logger.info(Messages.getString(Messages.getString("PafServiceProvider.60"))); //$NON-NLS-1$
 			
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
 	 * Method_description_goes_here.
 	 *
 	 * @param re the re
 	 * @throws PafSoapException the paf soap exception
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
 	 * @param changePasswordRequest Client Change Password Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException the remote exception
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
 	 * @param clientSecurityRequest Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException the remote exception
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
 	 * @param clientSecurityRequest Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 * @throws PafSoapException the paf soap exception
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
 	 * @param groupSecurityRequest the group security request
 	 * @return the paf groups
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAbletoGetLDAPContext the paf not ableto get ldap context
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
 	 * @param groupSecurityRequest the group security request
 	 * @return the user names for security groups
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAbletoGetLDAPContext the paf not ableto get ldap context
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
 	
 	/* (non-Javadoc)
 	 * @see com.pace.server.IPafService#verifyUsers(com.pace.server.comm.PafVerifyUsersRequest)
 	 */
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
 					invalidUserList.add(user + Messages.getString("PafServiceProvider.63") + domain); //$NON-NLS-1$
 				}
 			}
 
 			String[] invalidUsersArray  = invalidUserList.toArray(new String[0]);
 			InvalidUsers.setUsers(invalidUsersArray);
 
 		}
 		
 		return InvalidUsers;
 	}
 	
 	/**
 	 * Security domain user names.
 	 *
 	 * @param clientID the client id
 	 * @return the paf security domain user names
 	 */
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
 	 * @param clientSecurityRequest Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 * @throws PafNotAbletoGetLDAPContext the paf not ableto get ldap context
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
 					logger.warn(Messages.getString("PafServiceProvider.64")); //$NON-NLS-1$
 				}
 				
 				//encrypt email
 				String email = pafUserDef.getEmail();
 				try{
 					if (email.length() > 0){
 						email = AESEncryptionUtil.encrypt(email, generatedIV);
 					}
 				}
 		  		catch(Exception e){
 		  			email = Messages.getString("PafServiceProvider.65"); //$NON-NLS-1$
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
 		  			firstName = Messages.getString("PafServiceProvider.66"); //$NON-NLS-1$
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
 		  			lastName = Messages.getString("PafServiceProvider.67"); //$NON-NLS-1$
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
 						password = Messages.getString("PafServiceProvider.68"); //$NON-NLS-1$
 					}
 				}
 		  		catch(Exception e){
 		  			password = Messages.getString("PafServiceProvider.69"); //$NON-NLS-1$
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
 		  			userName = Messages.getString("PafServiceProvider.70"); //$NON-NLS-1$
 	    			logger.error(e.getMessage());
 	    		}
 				pafUserDef.setUserName(userName);
 				
 				//Finally, set db user on respose
 				response.setPafUserDef(pafUserDef);
 			}
 		}
 		return response;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.pace.server.IPafService#setGroups(com.pace.server.comm.PafSetPaceGroupsRequest)
 	 */
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
 				
 				String app = Messages.getString("PafServiceProvider.71"); //$NON-NLS-1$
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
 	
 	/* (non-Javadoc)
 	 * @see com.pace.server.IPafService#getGroups(com.pace.server.comm.PafGetPaceGroupsRequest)
 	 */
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
 	 * @param clientSecurityRequest Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException the remote exception
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
 	 * @param clientSecurityRequest Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException the remote exception
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
 	 * @param clientSecurityRequest Client Security Request (contains clientId)
 	 * @return PafClientSecurityPasswordResetResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
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
 	 * @param clientSecurityRequest Client Security Request (contains clientId)
 	 * @return PafClientSecurityResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException Thrown when client id is not valid or session token is not valid
 	 * @throws PafNotAuthorizedSoapException Thrown when client is not authorized
 	 * @throws PafSoapException the paf soap exception
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
 	 * Import attribute dimensions from multi-dimensional database.
 	 *
 	 * @param importAttrRequest the import attr request
 	 * @return PafImportAttrResponse Import attributes response object
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 * @throws PafSoapException the paf soap exception
 	 * @parm importAttrRequest Import attributes request object
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
 
 	/* (non-Javadoc)
 	 * @see com.pace.server.IPafService#clearImportedMdbAttributeDims(com.pace.server.comm.PafClearImportedAttrRequest)
 	 */
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
 	 * Verifiy client id is authenticated and valid.
 	 *
 	 * @param clientId Used to get client state from clients map.
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
 	 * Get list of properties from multi-dimensional database.
 	 *
 	 * @param mdbRequest Mdb props request object
 	 * @return PafMdbPropsResponse Mdb props response object
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafMdbPropsResponse getMdbProps(PafMdbPropsRequest mdbRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException {
 		PafMdbPropsResponse resp = new PafMdbPropsResponse();
 		boolean success = false;
 		try
 		{
 			if(isAuthorized(mdbRequest.getClientId(), false)){
 				
 				//Set logger client info property to user name
 				pushToNDCStack(mdbRequest.getClientId());
 
 				IPafConnectionProps connProps = (IPafConnectionProps) 
 	        		PafMetaData.getMdbProp(PafMetaData.getPaceProject().getApplicationDefinitions().get(0).getMdbDef().getDataSourceId());
 				
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
 	 * Return the valid list of attribute members and rollups
 	 * in light of selections on the related base dimension
 	 * and any other related attribute members.
 	 *
 	 * @param attrRequest Valid attribute request object
 	 * @return PafValidAttrResponse Valid attribute response object
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 * @throws PafSoapException the paf soap exception
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
 				Set<String> validAttrSet = new HashSet<String>();
 				String[] selBaseMembers = attrRequest.getSelBaseMembers();
 				for (int i = 0; i < selBaseMembers.length; i++) {
 					String baseMember = selBaseMembers[i];
 					String[] attrMembers = AttributeUtil.getValidAttributeMembers(attrRequest.getReqAttrDim(),
 							attrRequest.getSelBaseDim(), baseMember, attrRequest.getSelAttrSpecs(),
 							dataService.getAllDimTrees());
 					validAttrSet.addAll(Arrays.asList(attrMembers));
 				}
 				resp.setMembers(validAttrSet.toArray(new String[0]));
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
 	 * Provides, to the client, the PafSimpleTrees that reflect the user's uow
 	 * as defined by their role & security configurations.
 	 *
 	 * This method also updates the client state with the user's uow defintion 
 	 * and client trees.
 	 * 
 	 * @param planRequest the plan request
 	 * @return the paf populate role filter response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
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
 	 * Returns the filtered UOW size to the client. This method also updates the client state with 
 	 * the rolefilter user selections and generates the UOW specifcation corresponding to these 
 	 * selections.
 	 *
 	 * @param filteredUOWSize the filtered uow size
 	 * @return the filtered uow size
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
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
 
 				// Create user-filtered work spec
 				workUnit = dataService.createUserFilteredWorkSpec(clientState, filteredUOWSize.getPafUserSelections());
 
 			} else {
 				workUnit = PafSecurityService.getWorkSpec(filteredUOWSize.getSelectedRole(), filteredUOWSize.getSeasonId(), clientState);
 				workUnit = dataService.expandUOW(workUnit, clientState);
 				clientState.setUnitOfWork(workUnit.clone());
 				clientState.setPlanSeason(planSeason);
 				//to fix TTN 1745 - Getting PafException when role filter is disabled and Suppress Invalid Intersections Enabled
 				MemberTreeSet treeSet = dataService.getUowCacheTrees(clientState);
 				clientState.setUowTrees(treeSet);
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
 
 						// The Data Filter Spec exists
 						if(dataFilterSpec != null && dataFilterSpec.getDimSpec().length > 0){
 							UnitOfWork workUnitDF = workUnit.clone();
 
 							PafDimSpec[] nonHierDimSpecs = pafPlannerConfig.getDataFilterSpec().getDimSpec();
 
 							// De-tokenize the expressions
 							for(PafDimSpec pafDimSpec : nonHierDimSpecs){
 								//Check if all dimension members were suppressed due to data filtering.
 								if (pafDimSpec.getExpressionList() == null || pafDimSpec.getExpressionList().length == 0 ||  pafDimSpec.getExpressionList()[0].length() == 0){
 									throw new PafException(Messages.getString("PafServiceProvider.72") + pafDimSpec.getDimension() + Messages.getString("PafServiceProvider.73"), PafErrSeverity.Error); //$NON-NLS-1$ //$NON-NLS-2$
 								}
 								
 								if (pafDimSpec.getExpressionList()[0].contains(com.pace.base.PafBaseConstants.UOW_ROOT)){
 									pafDimSpec.getExpressionList()[0] = viewService.replaceUserUow(pafDimSpec.getExpressionList()[0], clientState, pafDimSpec.getDimension());
 								}
 								else if(pafDimSpec.getExpressionList()[0].contains(com.pace.base.PafBaseConstants.PLAN_VERSION)){
 									pafDimSpec.getExpressionList()[0] = viewService.replaceUserVers(pafDimSpec.getExpressionList()[0], clientState);
 								}
 							}
 							
 							// Get trees associated with the unit of work
 							MemberTreeSet treeSet = dataService.getUowCacheTrees(clientState, workUnitDF);
 							
 							// Filter the hierarchy dimension members to only include the floor members
 							for(String dim1 : clientState.getApp().getMdbDef().getHierDims())
 							{
 								List<String> lowestLevelMembers = new ArrayList<String>();
 								for(PafDimMember lowestLevelMember : treeSet.getTree(dim1).getLowestMembers(treeSet.getTree(dim1).getRootNode().toString()))
 								{
 									lowestLevelMembers.add(lowestLevelMember.getKey());
 								}
 								workUnitDF.setDimMembers(dim1, lowestLevelMembers.toArray(new String[0]));
 							}
 							
 							// Update the Unit of Work with the base member selections from the data filter spec
 							for(PafDimSpec dimSpec : nonHierDimSpecs){
 								workUnitDF.setDimMembers(dimSpec.getDimension(), dimSpec.getExpressionList());
 							}
 
 							// Filter the data using an Essbase MDX query with the Non Empty flag
 							PafDimSpec[] hierDimSpecs = dataService.getFilteredMetadata(clientState, clientState.getApp(), workUnitDF.getPafDimSpecs());
 
 							// Update the Unit of Work with the data filtered hierachical dim metadata
 							for(PafDimSpec dimSpec : hierDimSpecs){
 								
 								// Expand hierarchy dim members to include ancestors that were in uow
 								List<PafDimMember> ancestors = new ArrayList<PafDimMember>();
 								Set<String> uniqueMembers = new HashSet<String>();
 								String dim = dimSpec.getDimension();
 								PafDimTree dimTree = treeSet.getTree(dim);
 								for(String memberName : dimSpec.getExpressionList()){
 									uniqueMembers.add(memberName);
 									ancestors = dimTree.getAncestors(memberName);
 									for(PafDimMember ancestor : ancestors){
 											uniqueMembers.add(ancestor.getKey());
 									}
 								}
 								
 								// Sort members using a pre-order tree sort
 								List<String> sortedMemberList = new ArrayList<String>(uniqueMembers);
 								dimTree.sortMemberList(sortedMemberList, TreeTraversalOrder.PRE_ORDER);
 								
 								// If this dimension is filtered, the discontiguous member group collection needs to be
 								// populated for this dimension, so that the corresponding uow tree is built properly as
 								// a discontiguous tree. The root member must appear first, its own list, followed
 								// by the remaining base members, grouped by branch, each in their own list (TTN-1644).
 								List<String> uowMembers = new ArrayList<String>(Arrays.asList(workUnit.getDimMembers(dim)));
 								if (sortedMemberList.size() < uowMembers.size()) {
 									List<List<String>> discontigMbrLists = dataService.getBranchLists(sortedMemberList, dimTree);							
 									workUnit.getDiscontigMemberGroups().put(dim, discontigMbrLists);
 								}	
 								
 								// Update unit of work
 								workUnit.setDimMembers(dimSpec.getDimension(), sortedMemberList.toArray(new String[0]));
 
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
 			
 			
 			// Calculate the UOW cell count. For sizing purposes we will ignore derived versions, which
 			// have been added to the unit of work definition to support Attribute Evaluation re-design
 			// (TTN-1506).
 			UnitOfWork sizingWorkUnit = workUnit.clone();
 			String versionDim = clientState.getApp().getMdbDef().getVersionDim();
 			List<String> versions = new ArrayList<String>(Arrays.asList(sizingWorkUnit.getDimMembers(versionDim)));
 			versions.removeAll(clientState.getApp().getDerivedVersions());
 			sizingWorkUnit.setDimMembers(versionDim, versions.toArray(new String[0]));
 			long uowCellCount = sizingWorkUnit.getMemberCount();
 			
 			// If the cell count falls within the bounds, then update the unit of work in the client state
 			if ((sizeMax == null || uowCellCount <= sizeMax) && uowCellCount > 0){
 				clientState.setUnitOfWork(workUnit);
 			}
 			
 			// Fill out remaining response object fields
 			resp.setUowCellCount(uowCellCount);			
 			resp.setEmptyDimensions(workUnit.getEmptyDimensions());
 			
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
 	
 	public PaceResultSetResponse getFilteredResultSetResponse(PaceQueryRequest queryRequest) throws RemoteException, PafSoapException {
 		
 		// for now presume the expression list is simply a list of members/attributes
 		String dim = queryRequest.getMembers().getDimension();
 		String expr[] = queryRequest.getMembers().getExpressionList();
 		int level = queryRequest.getLevel();
 		
 		PafClientState clientState = clients.get(queryRequest.getClientId());
 		List<PafDimMember> dimMembers = new ArrayList<PafDimMember>();
 		
 		PafBaseTree t = clientState.getUowTrees().getBaseTree(dim);
 		
 		//get direct selected members at the specified level
 		
 		if (expr != null && expr.length>0) {
 			for (String e : expr) {
 				//TODO Look at how level is being set in Client GUI
 				dimMembers.addAll(t.getMembersAtLevel(e, level));			
 			}
 		}
 
 		// get members correcsponding to the selected attributes
 		List<String> attribBaseNames = new ArrayList<String>();
 		if (queryRequest.getAttributes() != null) {
 			for (PafDimSpec spec : queryRequest.getAttributes()) {
 				PafAttributeTree at = clientState.getUowTrees().getAttributeTree(spec.getDimension() );
 				for ( String e : spec.getExpressionList() ) {
 					List<String> attribNames = at.getLowestMemberNames(e);
 					for (String aname : attribNames) {
 						attribBaseNames.addAll(at.getBaseMemberNames(aname));
 					}
 				}
 			}
 		}
 		
 		// process member names into basemembers
 		// should have some level checking here as basemembers might not match corresponding level
 		for (String aname : attribBaseNames) {
 			// Attribute tree contains base member mappings for members outside UOW
 			if (t.hasMember(aname)) {
 				dimMembers.add(t.getMember(aname));
 			}
 		}
 
 		// dimMembers should now hold all selected members
 		// pump them into the lists
 		Map<String, String[]> dataset = new HashMap<String, String[]>();
 		// initialize header list
 		ArrayList<String> hdr = new ArrayList<String>();
 		hdr.add(dim);
 		for (String atname : t.getAttributeDimNames()) {
 			hdr.add(atname);
 		}
 
 
 		for (PafDimMember m : dimMembers) {
 			ArrayList<String> row = new ArrayList<String>();
 			// insert member
 			row.add(m.getKey());
 			// set attribute values
 			for (String atname : t.getAttributeDimNames()) {
 				// super sloppy, but presumption this is at a 1-1 level
 				Set<String> atval = t.getAttributeMembers(m.getKey(), atname);
 				if (atval.size() > 0)
 					row.add((String) atval.toArray()[0]);
 				else 
 					row.add("");
 			}
 			dataset.put(m.getKey(), row.toArray(new String[0]));
 		}
 
 		// get assortment set from datastore
 		AsstSet asst = dataStore.getAsstSet(queryRequest.getClientId(), queryRequest.getSessionToken());
 		
 		// save off results for subsequent cluster work so client data doesn't need to be resent
 		// build an assortment set to save
 		PafDimSpec2 dimSpec = dataStore.createPafDimSpec(dim, new ArrayList<String>(dataset.keySet()));
 		
 		// hardcode for demos, will be based upon configuration file
 		if (dim.equals("Location")){
 			asst.setDimToCluster(dimSpec);
 		} else if (dim.equals("Product")) {
 			asst.setDimToMeasure(dimSpec);
 		}
 		
 		// save assortment set back into database
 		dataStore.saveAsst(asst);
 		
 		//return string grid response
 		StringRow srHeader = new StringRow(hdr.toArray(new String[0]));
 		return new PaceResultSetResponse(srHeader, dataset.values().toArray(new String[0][]));
 
 	}
 	
 	public ClusteredResultSetResponse getClusteredResult(ClusterResultSetRequest request) throws RemoteException, PafSoapException {
 		
 		String clientId = request.getClientId();
 		String sessinoId = request.getSessionToken();
 		ClusteredResultSetResponse response = new ClusteredResultSetResponse();
 		
 		try{
 			pushToNDCStack(clientId);
 			
 			AsstSet asst = dataStore.getAsstSet(clientId, sessinoId);
 			
 			asst.setMeasures(dataStore.createPafDimSpec(request.getMeasuresDimSpec().getDimension(), Arrays.asList(request.getMeasuresDimSpec().getExpressionList())));
 			asst.setTimePeriods(dataStore.createPafDimSpec(request.getTimeDimSpec().getDimension(), Arrays.asList(request.getTimeDimSpec().getExpressionList())));
 			asst.setLabel(request.getLabel());
 			
 			dataStore.saveAsst(asst);
 			
 			asst = dataStore.getAsstSet(clientId, sessinoId);
 			
 			PaceDataSet inData;
 			
 			try {
 				inData = dataService.buildAsstDataSet(asst, request.getYearsDimSpec(), request.getVersionDimSpec());
 			} catch (PafException e) {
 				throw e.getPafSoapException();
 			}
 			
 			//This throws an error.  I think it's due to the double[][] data property.
 			//dataStore.storePaceDataSet(clientId, sessinoId, inData);
 			
 			PaceClusteredDataSet clusters = dataService.clusterDataset(inData, request.getNumOfClusters(), request.getMaxIterations());
 			String[] row = null;
 			List<StringRow> rows = new ArrayList<StringRow>();
 			int i;
 			
 			int clusterCount = 0;
 			int rowCount = 0;
 			// cluster
 			for (Cluster<EuclideanIntegerPoint> c : clusters.getClusters() ){
 				// row
 				clusterCount++;
 				for (EuclideanIntegerPoint point : c.getPoints() ) {
 					// value
 					row = new String[point.getPoint().length];
 					i = 0;
 					for (Integer I : point.getPoint() ) {
 						row[i++] = I.toString();
 					}
 					//Get the member name (id)
 					String id = clusters.getClusterRowMap().get(rowCount);
 					//Get the cluster number
 					int clusterNumber = clusters.getClusterKeys().get(id);
 					// add row
 					rows.add(new StringRow(id, clusterNumber, row));
 					rowCount++;
 				}
 			}
 			
 			// generate header
 			StringRow hdr = new StringRow();
 			for (String s : asst.getDimToMeasure().getExpressionList() ) {
 				for (String m : asst.getMeasures().getExpressionList() ) {
 					//for(String l : asst.getDimToCluster().getExpressionList()){
 					for(String t : asst.getTimePeriods().getExpressionList()){
 						//hdr.add(s + ", " + m );
 						hdr.add(s + ", " + m + ", " + t);
 						//hdr.add(s + ", " + m + ", " + l);
 					}
 					//}
 				}
 			}
 
 			response.setClusters(clusters.getClusterKeys());
 			response.setHeader(hdr);
 			response.setData(rows.toArray(new StringRow[0]));
 			response.setMeasures(asst.getMeasures().getExpressionList());
 			response.setDimToCluster(asst.getDimToCluster().getExpressionList());
 			response.setDimToMeasure(asst.getDimToMeasure().getExpressionList());
 			response.setVersion(Arrays.asList(request.getVersionDimSpec().getExpressionList()));
 			response.setYears(Arrays.asList(request.getYearsDimSpec().getExpressionList()));
 			response.setTime(Arrays.asList(request.getTimeDimSpec().getExpressionList()));
 			
 		} catch (RuntimeException re) {
 			handleRuntimeException(re);
 			
 		} finally {
 			popFromNDCStack(clientId);
 		}
 		return response;
 	}
 	
 	public PafResponse saveClusteredResultSet(ClusteredResultSetSaveRequest request) throws RemoteException, PafSoapException {
 		
 		final String clientId = request.getClientId(), sessinoId = request.getSessionToken(), assortmentLabel = request.getAssortment();
 		final String locationDim = request.getLocationDim(), productDim = request.getProductDim(), assortmentDim = "Assort";
 		final List<String> timePeriods = request.getTime(), plannableYears = request.getYears();
 		final List<String> products = request.getDimToMeasure(), locations = request.getDimToCluster();
 		final List<String> measures = request.getMeasures(), version = request.getVersion();
 		final String assortmentRole = "Assortment Planner", assortmentCycle = null;
 		final int clusterLevel = 0, SLOTS = 30;
		final String CLUSTER_PREFIX = "Cluster ", ASSORTMENT_PREFIX = "Assort", ASSORTMENT_ROOT = "AssortmentTotal";
 		SortedMap<String, List<String>> clusterMap = new TreeMap<String, List<String>>();
 		List<PafDimSpec> otherDims = new ArrayList<PafDimSpec>();
 		PafResponse response = new PafResponse();
 
 		
 		try{
 			pushToNDCStack(clientId);
 			
 			// Validate Parameters
 
 			// Get app info
 			PafClientState clientState = clients.get(request.getClientId());
 			PafApplicationDef app = clientState.getApp();
 			MdbDef mdbDef = app.getMdbDef();
 			String timeDim = mdbDef.getTimeDim(), measureDim = mdbDef.getMeasureDim(), 
 					versionDim = mdbDef.getVersionDim(), planTypeDim = mdbDef.getPlanTypeDim();
 			MemberTreeSet clientTrees = clientState.getUowTrees();
 			PafDimTree timeTree = clientTrees.getTree(timeDim);
 
 			// Get current security / role info
 			PafPlannerConfig plannerConfig = clientState.getPlannerConfig();
 			String plannerPlanType = clientState.getPlannerRole().getPlanType();
 			Season season = clientState.getPlanSeason();
 			
 			// Clone season
 			// -- Set Periods (Use otherDims collection)
 			Season clusterSeason = season.clone();
 			clusterSeason.setId(assortmentLabel);
 			clusterSeason.setOpen(true);
 			clusterSeason.setPlannableYears(plannableYears.toArray(new String[0]));
 			clusterSeason.setTimePeriod(null);
 			PafDimSpec timeSpec = new PafDimSpec();
 			timeSpec.setDimension(timeDim);
 			List<String> periodList = new ArrayList<String>();
 			if (timePeriods.size() == 1) {
 				String period = "@IDESCENDENTS(" + timePeriods.get(0) + ")";
 				periodList.add(period);
 			} else {
 				// Get range of peer members
 				String timePeriodStart = timePeriods.get(0);
 				String timePeriodEnd = timePeriods.get(1);
 				List<PafDimMember> peers = timeTree.getIRPeers(timePeriodStart);
 				for (PafDimMember peer : peers) {
 					String memberName = peer.getKey();
 					periodList.add(memberName);
 					if (timePeriodEnd.equals(memberName)) {
 						break;
 					}
 				}
 			}
 			timeSpec.setExpressionList(periodList.toArray(new String[0]));
 			otherDims.add(timeSpec);
 
 //			// -- Set Measures
 //			PafDimSpec measureSpec = new PafDimSpec();
 //			measureSpec.setDimension(measureDim);
 //			measureSpec.setExpressionList(measures.toArray(new String[0]));
 //			otherDims.add(measureSpec);
 			
 			// -- Set Version
 			PafDimSpec versionSpec = new PafDimSpec();
 			versionSpec.setDimension(versionDim);
 			versionSpec.setExpressionList(version.toArray(new String[0]));
 			otherDims.add(versionSpec);
 			
 			// -- Find available slot
 			//TODO Update to make this non case-insensitive
 			boolean slotWasFound = false;
 			int slot = 1;
 			String key = assortmentLabel;
 			if (assortments.containsKey(key)) {
 				slot = assortments.get(key);
 				slotWasFound = true;
 			} else {
 				// Look for an available slot
 				int maxSlots = SLOTS;
 				PafDimTree assortTree = dataService.getDimTree(assortmentDim);
 				if (assortTree.hasMember(ASSORTMENT_ROOT)) {
 					// Dyanmically calculate max available slots
 					maxSlots = assortTree.getMembersAtLevel(ASSORTMENT_ROOT, 0).size();
 				}
 				slot = assortSlots.size() + 1;
 				while (slot <= maxSlots) {
 					String status = assortSlots.putIfAbsent(slot, key);
 					if (status == null) {
 						slotWasFound = true;
 						break;
 					}
 					slot++;
 				}
 			}
 			if (slotWasFound) {
 				assortments.put(key, slot);
 			} else {
 				String errMsg = String.format("Unable to save assortment: [%s] - no available slots", key);
 				throw new IllegalArgumentException(errMsg);
 			}
 
 			// -- Set Plan Type
 			PafDimSpec planTypeSpec = new PafDimSpec();
 			planTypeSpec.setDimension(planTypeDim);
 			String planType = plannerPlanType; 
 			planTypeSpec.setExpressionList(new String[]{planType});
 			otherDims.add(planTypeSpec);
 			
 			// -- Set Products
 			PafDimSpec prodSpec = new PafDimSpec();
 			prodSpec.setDimension(productDim);
 			prodSpec.setExpressionList(products.toArray(new String[0]));
 			otherDims.add(prodSpec);
 			
 			// Organize entities by cluster number
 			Map<String, Integer> clusterSelections = request.getClusters();
 			List<String> locList = new ArrayList<String>();
 			for (String entity : clusterSelections.keySet()) {
 				int clusterNo = clusterSelections.get(entity);
 				String clusterKey = String.format("%s%02d", CLUSTER_PREFIX, clusterNo);
 				List<String> entityList = null;
 				if (clusterMap.containsKey(clusterKey)) {
 					entityList = clusterMap.get(clusterKey);
 				} else {
 					entityList = new ArrayList<String>();
 				}
 				entityList.add(entity);
 				clusterMap.put(clusterKey, entityList);
 			}
 			dataService.addClusterMap(assortmentLabel, clusterMap);
 			for (String clusterKey : clusterMap.keySet()) {
 				locList.add(clusterKey);
 				locList.addAll(clusterMap.get(clusterKey));				
 			}
 
 			// -- Set Locations
 			PafDimSpec locSpec = new PafDimSpec();
 			locSpec.setDimension(locationDim);
 //			locSpec.setExpressionList(locations.toArray(new String[0]));
 			locSpec.setExpressionList(locList.toArray(new String[0]));
 			otherDims.add(locSpec);
 			
 			// -- Set Assortment
 			PafDimSpec assortmentSpec = new PafDimSpec();
 			assortmentSpec.setDimension(assortmentDim);
 			String assortment = String.format("%s%02d", ASSORTMENT_PREFIX, slot); 
 			assortmentSpec.setExpressionList(new String[]{assortment});
 			otherDims.add(assortmentSpec);
 			
 			// -- Store clustered dimension specs
 			clusterSeason.setOtherDims(otherDims.toArray(new PafDimSpec[0]));
 			
 			// Persist season
 			SeasonList seasonList = app.getSeasonList();
 			String seasonId = clusterSeason.getId();
 			seasonList.removeSeasonById(seasonId);
 			seasonList.addSeason(clusterSeason);
 					
 			// Add Season to Assortment Planner Role	
 			//PafPlannerConfig assortmentRoleConfig = findPafPlannerConfig(assortmentRole, assortmentCycle);
 			//PafPlannerRole plannerRole = PafSecurityService.getPlannerRole(assortmentRole);
 			PafSecurityService.addOrReplaceSeason(assortmentRole, clusterSeason);
 						
 			// Save was successful			
 			response.setResponseMsg("Assortment: [" + assortmentLabel + "] was succsessfully created");
 			
 		} catch (RuntimeException re) {
 			response.setResponseMsg(re.getMessage());
 			handleRuntimeException(re);
 			
 		} finally {
 			popFromNDCStack(clientId);
 		}
 		return response;
 	}
 
 	/**
 	 * Logs off the current user.  This method cleans up the UOW Cache
 	 * tied to the client id.
 	 *
 	 * @param logoffRequest 		Logoff request
 	 * @return the paf response
 	 * @throws RemoteException 	blah
 	 * @throws PafSoapException 	blah
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
 	
 	/* (non-Javadoc)
  * @see com.pace.server.IPafService#getCellNotes(com.pace.server.comm.PafGetNotesRequest)
  */
 public PafGetNotesResponse getCellNotes(
 			PafGetNotesRequest getNotesRequest) throws RemoteException,
 			PafSoapException {
 
 		PafGetNotesResponse getNotesResponse = new PafGetNotesResponse();
 		
 		CellNoteCache cnc = CellNoteCacheManager.getInstance().getNoteCache(getNotesRequest.getClientId());
 		
 		getNotesResponse.setNotes(cnc.getNotes(getNotesRequest.getSimpleCoordLists()));
 
 		return getNotesResponse;
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see com.pace.server.IPafService#saveCellNotes(com.pace.server.comm.PafSaveNotesRequest)
 	 */
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
 	 * @param pafRequest the paf request
 	 * @return PafCellNoteInformationResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @parm pafRequest	The request
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
 	 * @param pafSimpleCellNoteExportRequest the paf simple cell note export request
 	 * @return PafSimpleCellNoteExportResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @parm pafSimpleCellNoteExportRequest	The request to export the cell notes.
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
 	 * Import Simple Cell Notes.
 	 *
 	 * @param pafSimpleCellNoteImportRequest the paf simple cell note import request
 	 * @return PafSimpleCellNoteImportResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @parm PafSimpleCellNoteImportRequest	The request to import simple cell notes.
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
 	 * Clear member tag data for the specified application(s) and member tags.
 	 *
 	 * @param filteredMbrTagsRequest Contains optional app/member tag filter
 	 * @return PafSuccessResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
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
 	 * Clear member tag data for the specified application(s) and member tags.
 	 *
 	 * @param filteredMbrTagsRequest Contains optional app/member tag filter
 	 * @return PafSuccessResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
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
 	 * Get all or specified member tag definitions.
 	 *
 	 * @param memberTagDefsRequest Contains an optional member tag filter
 	 * @return PafGetMemberTagDefsResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
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
 	 * Get all member tag defs for the specified application.
 	 *
 	 * @param app Current application definition
 	 * @return MemberTagDef[]
 	 */
 	private MemberTagDef[] getMemberTagDefs(PafApplicationDef app) {
 		
 		Map<String, MemberTagDef> memberTagDefs = app.getMemberTagDefs();
 	
 		return memberTagDefs.values().toArray(new MemberTagDef[0]);
 	}
 	
 
 	/**
 	 * Get member tag statistics.
 	 *
 	 * @param filteredMbrTagsRequest Contains optional app/member tag filter
 	 * @return PafGetMemberTagInfoResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
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
 	 * Import data into the member tag database.
 	 *
 	 * @param importMemberTagRequest Imported member tag data
 	 * @return PafSuccessResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
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
 				logger.info(Messages.getString("PafServiceProvider.74")); //$NON-NLS-1$
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
 	 * Save updates to the member tag database.
 	 *
 	 * @param saveMbrTagsRequest Member tag additions, updates, and deletions
 	 * @return PafSuccessResponse
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
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
 	 * Validate user security configuration.
 	 *
 	 * @param validateUserSecurityReq The user security validation request
 	 * @return ValidationResponse
 	 * 
 	 * @throws RemoteException
 	 * @throws PafNotAuthorizedSoapException 
 	 * @throws PafNotAuthenticatedSoapException 
 	 * @throws PafSoapException 
 	 */
 	public ValidationResponse validateUserSecurity(ValidateUserSecurityRequest validateUserSecurityReq) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException {
 	
 		ValidationResponse resp = new ValidationResponse();
 		List<String> validationErrors = new ArrayList<String>();
 		boolean success = false;
 		try
 		{
 			if(isAuthorized(validateUserSecurityReq.getClientId(), false)){
 				
 				// Set logger client info property to user name
 				pushToNDCStack(validateUserSecurityReq.getClientId());
 				
 				// Validate the user security configuration
 				PafClientState clientState = clients.get(validateUserSecurityReq.getClientId());
 				success =  dataService.validateUserSecurity(validateUserSecurityReq.getSecuritySpecs(), validationErrors, clientState);
 			}
 			
 		}catch (RuntimeException re) {
 			handleRuntimeException(re);
 		}catch (PafException pex) {
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 		}finally{
 			// Pop logger client id from stack and format response object
 			popFromNDCStack(validateUserSecurityReq.getClientId());
 			resp.setValidationErrors(validationErrors.toArray(new String[0]));
 			resp.setSuccess(success);
 		}
 		
 		return resp;
 
 	}	
 	/**
 	 * Closes a clients session by removing Client State from the clients map.
 	 *
 	 * @param pafRequest request
 	 * @return PafSuccessResponse success or not
 	 * @throws RemoteException the remote exception
 	 */
 	public PafSuccessResponse closeClientSession(PafRequest pafRequest) throws RemoteException {
 
 		PafSuccessResponse response = new PafSuccessResponse();
 		
 		if ( pafRequest != null ) {
 			
 			String clientId = pafRequest.getClientId();
 			
 			dataStore.delAsstSet(clientId);
 						
 			if ( clients != null && clients.containsKey(clientId) ) {
 				
 				logger.info(Messages.getString("PafServiceProvider.75")+ clientId +Messages.getString("PafServiceProvider.76")); //$NON-NLS-1$ //$NON-NLS-2$
 				
 				clients.remove(clientId);
 				
 				response.setSuccess(! clients.containsKey(clientId));
 				
 			}
 			
 		}
 		
 		return response;
 	}
 
 
 
 	/**
 	 * Checks to see if a client session is active by using the client id
 	 * TTN-1160.
 	 *
 	 * @param pafRequest request
 	 * @return PafSuccessResponse success or not
 	 * @throws RemoteException the remote exception
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
 	
 	
 	/**
 	 * List cookies.
 	 *
 	 * @param clientId the client id
 	 */
 	private void listCookies(String clientId) {
 		MessageContext mc = wsCtx.getMessageContext();		   
 		//HttpSession session = ((javax.servlet.http.HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST)).getSession();
 		HttpServletRequest req = (HttpServletRequest)mc.get(mc.SERVLET_REQUEST);
 
 	   Cookie cookies[] = req.getCookies();
 	   if (cookies != null) {
 		   logger.debug(Messages.getString("PafServiceProvider.77") + clientId + Messages.getString("PafServiceProvider.78"));		    //$NON-NLS-1$ //$NON-NLS-2$
 		   for (Cookie c : cookies) {
 			   logger.debug( c.getName() + Messages.getString("PafServiceProvider.79") + c.getValue() ); //$NON-NLS-1$
 		   }
 	   }
 	   else {
 		   logger.debug(Messages.getString("PafServiceProvider.80")); //$NON-NLS-1$
 	   }		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see com.pace.server.IPafService#getApplicationState(com.pace.base.comm.ApplicationStateRequest)
 	 */
 	@Override
 	public ApplicationStateResponse getApplicationState(
 			ApplicationStateRequest appReq) throws RemoteException,
 			PafSoapException {
 		
 		ApplicationStateResponse asr = new ApplicationStateResponse();
 		asr.setAppStates(appService.getAllApplicationStates() );
 
 		return asr;
 	}
 		
 
 	/* (non-Javadoc)
 	 * @see com.pace.server.IPafService#loadApplication(com.pace.base.comm.LoadApplicationRequest)
 	 */
 	@Override
 	public PafSuccessResponse loadApplication(LoadApplicationRequest appReq)
 			throws RemoteException, PafSoapException {
 
 		
 		String reqAppId=Messages.getString("PafServiceProvider.81"); //$NON-NLS-1$
 	
 		PafSuccessResponse psr = null;
 		
 		try {
 								
 			// initialize the application service and reload the application metadata			
 			appService.loadApplicationConfigurations();			
 			
 			System.out.println(Messages.getString("PafServiceProvider.7") + logger.getLevel()); //$NON-NLS-1$
 			
 			logger.info(Messages.getString("PafServiceProvider.8")); //$NON-NLS-1$
 						
 
 		} catch (Exception ex) {
 			
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 			psr = new PafSuccessResponse(false);
 			String s = String.format(Messages.getString("PafServiceProvider.82"), reqAppId); //$NON-NLS-1$
 			psr.addException(ex);
 			psr.setResponseMsg( s + ex.getMessage() );
 		}
 				
 		if ( psr == null ) {
 			psr = new PafSuccessResponse(true);
 		}
 		
 		return psr;
 		
 	}
 
 
 	@Override
 	public UploadAppResponse uploadApplication(UploadAppRequest uploadAppReq) 
 				throws RemoteException, PafSoapException {
 		
 		UploadAppResponse uploadAppResp = new UploadAppResponse();
 		
 		if ( uploadAppReq != null ) {
 							
 			boolean reinitClientState = false;
 			
 			boolean isApplicationLoaded = (PafMetaData.getPaceProject() != null);
 			
 			//save product configuration
 			if ( uploadAppReq.getPaceProjectDataHandler() != null ) {		
 			
 				DataHandler dataHandler = uploadAppReq.getPaceProjectDataHandler();
 				
 				try {
 					
 					PaceProject paceProject = DataHandlerPaceProjectUtil.convertDataHandlerToPaceProject(dataHandler, PafMetaData.getTransferDirPath());
 					
 					//if app is currently loaded
 					if ( isApplicationLoaded &&
 							PafMetaData.getPaceProject().getApplicationDefinitions().size() > 0 
 							&& paceProject.getApplicationDefinitions().size() > 0 
 							&& ! paceProject.getApplicationDefinitions().get(0).getAppId().equals(PafMetaData.getPaceProject().getApplicationDefinitions().get(0).getAppId())) {
 						reinitClientState = true;
 					}
 					
 					if ( paceProject != null ) {
 					
 						if ( paceProject instanceof XMLPaceProject ) {
 							((XMLPaceProject) paceProject).setUpdateOnly(uploadAppReq.isPartialDeployment());
 						}
 						
 						PafMetaData.saveApplicationConfig(paceProject, uploadAppReq.getProjectElementIdFilters());
 						
 						uploadAppResp.setSuccess(true);
 					}				
 					
 				} catch (IOException e) {
 					logger.error(e.getMessage());
 					e.printStackTrace();
 				} catch (InvalidPaceProjectInputException e) {
 					logger.error(e.getMessage());
 					e.printStackTrace();
 				} catch (PaceProjectCreationException e) {
 					logger.error(e.getMessage());
 					e.printStackTrace();
 				} catch (ProjectSaveException e) {
 					logger.error(e.getMessage());
 					e.printStackTrace();
 				} catch (PafException e) {
 					logger.error(e.getMessage());
 					e.printStackTrace();
 				}
 				
 			}
 			
 			boolean isApplyCubeUpdate = false;
 			
 			//apply configuration changes
 			if ( uploadAppReq.isApplyConfigurationUpdate() ) {
 				
 				appService.loadApplicationConfigurations();
 				
 				//if app wasn't previously loaded, load essbase.  took out for now per convo with Jim
 				/*if ( ! isApplicationLoaded ) {
 					isApplyCubeUpdate = true;
 				}*/
 				
 				if ( reinitClientState ) {
 					
 					uploadAppResp.setReinitClientState(true);
 					
 					clients.remove(uploadAppReq.getClientId());
 					
 					uploadAppResp.setApplyConfigurationUpdateSuccess(true);
 					
 				}
 								
 			}
 						
 			if ( uploadAppReq.isApplyCubeUpdate() || isApplyCubeUpdate ) {
 				
 				try {
 					appService.loadApplicationMetaData(PafMetaData.getPaceProject().getApplicationDefinitions().get(0).getAppId());
 					uploadAppResp.setApplyCubeUpdateSuccess(true);
 				} catch (PafException e) {
 					logger.error(e.getMessage());
 				}				
 				
 			}
 			
 		}		
 						
 		return uploadAppResp;
 	}
 
 
 	@Override
 	public DownloadAppResponse downloadApplication(
 			DownloadAppRequest downAppReq) throws RemoteException,
 			PafSoapException {
 
 		DownloadAppResponse resp = new DownloadAppResponse();
 		
 		try {
 						
 			Set<ProjectElementId> filterSet = downAppReq.getProjectElementIdFilters();
 			
 			PaceProject pp = null;
 			
 			if ( filterSet != null && filterSet.size() > 0) {
 				pp = new XMLPaceProject(PafMetaData.getConfigDirPath(), filterSet, false);
 				resp.setProjectDataFiltered(true);
 			} else {
 				pp = new XMLPaceProject(PafMetaData.getConfigDirPath(), false);
 			}									
 			
 							
 			DataHandler dh = DataHandlerPaceProjectUtil.convertPaceProjectToDataHandler(pp, PafMetaData.getTransferDirPath());
 			
 			if ( pp.getApplicationDefinitions() != null && pp.getApplicationDefinitions().size() > 0 ) {
 					
 				resp.setAppId(pp.getApplicationDefinitions().get(0).getAppId());
 					
 			}
 				
 			resp.setPaceProjectDataHandler(dh);			
 			
 			resp.setSuccess(true);
 		}
 		catch (Exception ex) {
 			resp.setSuccess(false);
 			resp.addException(ex);
 		}
 			
 		return resp;
 	}
 
 
 
 
 	@Override
 	public PafSuccessResponse startApplication(StartApplicationRequest appReq)
 			throws RemoteException, PafSoapException {
 		
 		String reqAppId=Messages.getString("PafServiceProvider.83"); //$NON-NLS-1$
 	
 		PafSuccessResponse psr = null;
 		
 		try {
 								
 			if (appReq==null) {
 				reqAppId = appService.getApplications().get(0).getAppId();
 			} else {
 				reqAppId = appReq.getAppIds().get(0);
 			}			
 				
 			
 			appService.loadApplicationMetaData(reqAppId);
 					
 
 		} catch (Exception ex) {
 			
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 			psr = new PafSuccessResponse(false);
 			String s = String.format(Messages.getString("PafServiceProvider.84"), reqAppId); //$NON-NLS-1$
 			psr.addException(ex);
 			psr.setResponseMsg( s + ex.getMessage() );
 		}
 				
 		if ( psr == null ) {
 			psr = new PafSuccessResponse(true);
 		}
 		
 		return psr;		
 	
 	}
 
 
 
 
 	@Override
 	public CreateAsstResponse createAssortment(
 			CreateAsstRequest createAsstRequest) throws RemoteException,
 			PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException,
 			PafSoapException {
 		
 		// clear asset sets for now
 		dataStore.clrAsstSets();
 		
 		// get a persisted object to go with session
 		AsstSet asst = dataStore.initAsstSet(createAsstRequest.getClientId(), createAsstRequest.getSessionToken());
 		
 		// stub in measures and time
 		PafDimSpec2 timeDim = dataStore.createPafDimSpec("Time", Arrays.asList(new String[] {"Aug"}));
 		PafDimSpec2 msrDim = dataStore.createPafDimSpec("Measures", Arrays.asList(new String[] {"SLS_DLR", "SLS_U"}));
 		
 		asst.setMeasures(msrDim);
 		asst.setTimePeriods(timeDim);
 		
 		asst.setLabel("Hello");
 		
 		dataStore.saveAsst(asst);
 		
 
 		return new CreateAsstResponse();
 	}
 
 
 
 
 	@Override
 	public PaceDescendantsResponse getDescendants(PaceDescendantsRequest pafDescendantsRequest)
 			throws PafSoapException {
 		//Create a stopwatch for performance logging.
 		final StopWatch sw = new StopWatch("getDescendants");
 		sw.start("run");
 		//Get Client ID
 		String clientId = pafDescendantsRequest.getClientId();
 		//Create a response object.
 		PaceDescendantsResponse response  = new PaceDescendantsResponse();
 		
 		//Check for null items in the request, if so set the response and return.
 		if(pafDescendantsRequest.getSessionCells() == null || pafDescendantsRequest.getSessionCells().length == 0){
 			response.setResponseCode(-1);
 			//TODO externalize these messages...
 			logger.error("No session cells specified." + clientId); 
 			response.setResponseMsg("No session cells specified.");
 			return response;
 		}
 		
 		
 		try
 		{
 			// Set logger client info property to user name
 			pushToNDCStack(clientId);
 						
 			// Troubleshoot load balancer cookies
 			listCookies(clientId);
 			
 			// Verify client id is good
 			if ( ! clients.containsKey(clientId) ) {
 				logger.error(Messages.getString("PafServiceProvider.25") + clientId);
 				throw new PafSoapException(new PafException(Messages.getString(Messages.getString("PafServiceProvider.31")), PafErrSeverity.Error));
 			}		
 			
 			// Get client state
 			PafClientState cs = clients.get(clientId);
 			//if null, throw exception to client.
 			if (cs == null) {
 				throw new PafException(Messages.getString("PafServiceProvider.9"), 	PafErrSeverity.Fatal);
 			}
 			
 			// Get the decendant intersections
 			SimpleCoordList[] sessionIntersections = dataService.getDescendants(pafDescendantsRequest.getSessionCells(), cs);
 			response.setSessionIntersections(sessionIntersections);
 			sw.stop();
 
 		} catch (RuntimeException re) {
 			handleRuntimeException(re);
 			
 		} catch (PafException pex) {
 			PafErrHandler.handleException(pex);
 			throw pex.getPafSoapException();
 			
 		} finally {
 			logPerf.info(sw.prettyPrint());
 			popFromNDCStack(clientId);
 		}	
 		
 		return response;
 	}
 	
 	
 
 }
