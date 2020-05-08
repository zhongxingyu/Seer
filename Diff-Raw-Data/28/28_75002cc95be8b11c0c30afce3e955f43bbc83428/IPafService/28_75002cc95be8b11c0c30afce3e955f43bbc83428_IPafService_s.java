 /*
  *  File: IPafService.java  Package: com.pace.server  Project: pace-server
  *  Updated: Oct 7, 2011     By: Jim Watkins
  *
  *  Copyright (c) 2011 Alvarez & Marsal Software, LLC. All rights reserved.
  *
  *  This software is the confidential and proprietary information of Alvarez & Marsal Software, LLC.
  *  ("Confidential Information"). You shall not disclose such Confidential Information and 
  *  should use it only in accordance with the terms of the license agreement you entered into
  *  with Alvarez & Marsal Software, LLC.
  * 
  */
 package com.pace.server;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import javax.jws.WebService;
 
 import com.pace.base.PafNotAbletoGetLDAPContext;
 import com.pace.base.PafNotAuthenticatedSoapException;
 import com.pace.base.PafNotAuthorizedSoapException;
 import com.pace.base.PafSoapException;
 import com.pace.base.comm.ApplicationStateRequest;
 import com.pace.base.comm.ApplicationStateResponse;
 import com.pace.base.comm.ClientInitRequest;
 import com.pace.base.comm.DownloadAppRequest;
 import com.pace.base.comm.DownloadAppResponse;
 import com.pace.base.comm.EvaluateViewRequest;
 import com.pace.base.comm.LoadApplicationRequest;
 import com.pace.base.comm.PafRequest;
 import com.pace.base.comm.PafResponse;
 import com.pace.base.comm.PafSuccessResponse;
 import com.pace.base.comm.StartApplicationRequest;
 import com.pace.base.comm.UploadAppRequest;
 import com.pace.base.comm.UploadAppResponse;
 import com.pace.base.data.PafDataSlice;
 import com.pace.base.view.PafView;
 import com.pace.server.comm.ClusterResultSetRequest;
 import com.pace.server.comm.ClusteredResultSetResponse;
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
 import com.pace.server.comm.ValidateUserSecurityRequest;
 import com.pace.server.comm.ValidationResponse;
 import com.pace.server.comm.ViewRequest;
 // TODO: Auto-generated Javadoc
 
 /**
  * The interface defnition for the PafServer Web Service.
  *
  * @author Jim Watkins
  * @version x.xx
  */
 
 @WebService(name="PafService", targetNamespace="pace.palladium.com")
 public interface IPafService extends Remote {
 	
 	/**
 	 * Gets the version.
 	 *
 	 * @return the version
 	 * @throws RemoteException the remote exception
 	 */
 	public String getVersion() throws RemoteException;
 	
     /**
      * Start plan session.
      *
      * @param planRequest the plan request
      * @return the paf plan session response
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafPlanSessionResponse startPlanSession( PafPlanSessionRequest planRequest)  throws RemoteException, PafSoapException; 
 	
     /**
      * Uploads an application
      * 
      * @param uploadAppReq upload request
      * @return upload response
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public UploadAppResponse uploadApplication(UploadAppRequest uploadAppReq)  throws RemoteException, PafSoapException; 
     
     /**
      * Downloads an application
      * 
      * @param downAppReq download app request
      * @return download response
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public DownloadAppResponse downloadApplication(DownloadAppRequest downAppReq)  throws RemoteException, PafSoapException;     
     
 	/**
 	 * Gets the view.
 	 *
 	 * @param viewRequest the view request
 	 * @return the view
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafView getView(ViewRequest viewRequest) throws RemoteException, PafSoapException;
 	
 	/**
 	 * Gets the application state.
 	 *
 	 * @param appReq the app req
 	 * @return the application state
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public ApplicationStateResponse getApplicationState(ApplicationStateRequest appReq) throws RemoteException, PafSoapException;
 	
 	/**
 	 * Load application.
 	 *
 	 * @param appReq the app req
 	 * @return the paf success response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafSuccessResponse loadApplication(LoadApplicationRequest appReq) throws RemoteException, PafSoapException;
 	
 
 	/**
 	 * Start/Restarts an application
 	 * 
 	 * @param appReq the app req
 	 * @return the paf success response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException hte paf soap exception
 	 */
 	public PafSuccessResponse startApplication(StartApplicationRequest appReq) throws RemoteException, PafSoapException;
 	
 	
 	/**
 	 * Client init.
 	 *
 	 * @param pcInit the pc init
 	 * @return the paf server ack
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafServerAck clientInit(ClientInitRequest pcInit) throws RemoteException, PafSoapException;
     
     /**
      * Client auth.
      *
      * @param authReq the auth req
      * @return the paf auth response
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafAuthResponse clientAuth(PafAuthRequest authReq) throws RemoteException, PafSoapException;
     
     /**
      * Client cache request.
      *
      * @param cacheRequest the cache request
      * @return the paf client cache block
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafClientCacheBlock clientCacheRequest(PafClientCacheRequest cacheRequest) throws RemoteException, PafSoapException;
     
     /**
      * Evaluate view.
      *
      * @param evalRequest the eval request
      * @return the paf view
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafView evaluateView(EvaluateViewRequest evalRequest) throws RemoteException, PafSoapException;
     
     /**
      * Reload datacache.
      *
      * @param reloadRequest the reload request
      * @return the paf data slice
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafDataSlice reloadDatacache(PafViewRequest reloadRequest) throws RemoteException, PafSoapException;    
 	
 	/**
 	 * Update datacache.
 	 *
 	 * @param updateRequest the update request
 	 * @return the paf data slice
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafDataSlice updateDatacache(PafUpdateDatacacheRequest updateRequest) throws RemoteException, PafSoapException;
     
     /**
      * Save work.
      *
      * @param saveWorkRequest the save work request
      * @return the paf command response
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafCommandResponse saveWork(SaveWorkRequest saveWorkRequest) throws RemoteException, PafSoapException;
 	
 	/**
 	 * Gets the valid attribute members.
 	 *
 	 * @param attrRequest the attr request
 	 * @return the valid attribute members
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 */
 	public PafValidAttrResponse getValidAttributeMembers(PafValidAttrRequest attrRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafSoapException, PafNotAuthorizedSoapException;
     
     /**
      * Gets the mdb props.
      *
      * @param mdbPropsRequest the mdb props request
      * @return the mdb props
      * @throws RemoteException the remote exception
      * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
      * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
      * @throws PafSoapException the paf soap exception
      */
     public PafMdbPropsResponse getMdbProps(PafMdbPropsRequest mdbPropsRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException;;
     
     /**
      * Import mdb attribute dims.
      *
      * @param importAttrRequest the import attr request
      * @return the paf import attr response
      * @throws RemoteException the remote exception
      * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
      * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
      * @throws PafSoapException the paf soap exception
      */
     public PafImportAttrResponse importMdbAttributeDims(PafImportAttrRequest importAttrRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException;
     
     /**
      * Clear imported mdb attribute dims.
      *
      * @param clearImportedAttrRequest the clear imported attr request
      * @return the paf clear imported attr response
      * @throws RemoteException the remote exception
      * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
      * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
      * @throws PafSoapException the paf soap exception
      */
     public PafClearImportedAttrResponse clearImportedMdbAttributeDims(PafClearImportedAttrRequest clearImportedAttrRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException;
     
     /**
      * Run custom command.
      *
      * @param cmdRequest the cmd request
      * @return the paf custom command response
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafCustomCommandResponse runCustomCommand(PafCustomCommandRequest cmdRequest) throws RemoteException, PafSoapException;
     
     /**
      * End planning session.
      *
      * @param endSessionRequest the end session request
      * @return the paf response
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafResponse endPlanningSession(PafRequest endSessionRequest) throws RemoteException, PafSoapException;
     
     /**
      * Gets the dimension tree.
      *
      * @param pafTreeRequest the paf tree request
      * @return the dimension tree
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafTreeResponse getDimensionTree(PafTreeRequest pafTreeRequest) throws RemoteException, PafSoapException;
     
     /**
      * Gets the dimension trees.
      *
      * @param pafTreesRequest the paf trees request
      * @return the dimension trees
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafTreesResponse getDimensionTrees(PafTreesRequest pafTreesRequest) throws RemoteException, PafSoapException;
     
     /**
      * Gets the Descendants for an array of Intersections.
      *
      * @param pafTreesRequest the Pace Descendants request
      * @return the pace descendants response
      * @throws RemoteException the remote exception
      * @throws PafSoapException the Pace Soap Exception
      */
     public PaceDescendantsResponse getDescendants(PaceDescendantsRequest pafDescendantsRequest) throws RemoteException, PafSoapException;
     
     /**
      * Gets the paf user.
      *
      * @param clientSecurityRequest the client security request
      * @return the paf user
      * @throws RemoteException the remote exception
      * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
      * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
      * @throws PafNotAbletoGetLDAPContext the paf not ableto get ldap context
      */
     public PafClientSecurityResponse getPafUser(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafNotAbletoGetLDAPContext;
     
     /**
      * Gets the paf users.
      *
      * @param clientSecurityRequest the client security request
      * @return the paf users
      * @throws RemoteException the remote exception
      * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
      * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
      */
     public PafClientSecurityResponse getPafUsers(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
     
     /**
      * Gets the paf user names.
      *
      * @param clientSecurityRequest the client security request
      * @return the paf user names
      * @throws RemoteException the remote exception
      * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
      * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
      */
     public PafClientSecurityResponse getPafUserNames(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;    
     
     /**
      * Creates the paf user.
      *
      * @param clientSecurityRequest the client security request
      * @return the paf client security response
      * @throws RemoteException the remote exception
      * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
      * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
      */
     
     public CreateAsstResponse createAssortment(CreateAsstRequest createAsstRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException;
 
     
     
     public PafClientSecurityResponse createPafUser(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
     
     /**
      * Update paf user.
      *
      * @param clientSecurityRequest the client security request
      * @return the paf client security response
      * @throws RemoteException the remote exception
      * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
      * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
      * @throws PafSoapException the paf soap exception
      */
     public PafClientSecurityResponse updatePafUser(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException;
     
     
     
     /**
      * Delete paf user.
      *
      * @param clientSecurityRequest the client security request
      * @return the paf client security response
      * @throws RemoteException the remote exception
      * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
      * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
      * @throws PafSoapException the paf soap exception
      */
     public PafClientSecurityResponse deletePafUser(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException;
     
     /**
      * Reset paf user password.
      *
      * @param clientSecurityRequest the client security request
      * @return the paf client security password reset response
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafClientSecurityPasswordResetResponse resetPafUserPassword(PafClientSecurityRequest clientSecurityRequest) throws RemoteException, PafSoapException;
     
     /**
      * Change paf user password.
      *
      * @param passwordResetRequest the password reset request
      * @return the paf client security response
      * @throws RemoteException the remote exception
      * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
      * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
      */
     public PafClientSecurityResponse changePafUserPassword(PafClientChangePasswordRequest passwordResetRequest) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
     
     /**
      * Populate role filters.
      *
      * @param planSessionRequest the plan session request
      * @return the paf populate role filter response
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafPopulateRoleFilterResponse populateRoleFilters(PafPlanSessionRequest planSessionRequest) throws RemoteException, PafSoapException;
     
     /**
      * Gets the filtered uow size.
      *
      * @param filteredUOWSize the filtered uow size
      * @return the filtered uow size
      * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
      */
     public PafGetFilteredUOWSizeResponse getFilteredUOWSize(PafGetFilteredUOWSizeRequest filteredUOWSize) throws RemoteException, PafSoapException;
 	
     
     
 	/**
 	 * Returns a table of members and attribute values based upon a set of dimension and attribute specifications
 	 * 
 	 * @param PaceQueryRequest
 	 * @return PaceResultSetResponse
 	 * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
 	 */
 	public PaceResultSetResponse getFilteredResultSetResponse(PaceQueryRequest queryRequest) throws RemoteException, PafSoapException;
 
 
 	/**
 	 * Returns a dataset that has been clustered by an algorithm
 	 * 
 	 * @param PaceQueryRequest
 	 * @return PaceResultSetResponse
 	 * @throws RemoteException the remote exception
      * @throws PafSoapException the paf soap exception
 	 */	
 	public ClusteredResultSetResponse getClusteredResult(ClusterResultSetRequest request) throws RemoteException, PafSoapException;
 	
 	
 	/**
 	 * Reinitialize client state.
 	 *
 	 * @param cmdRequest the cmd request
 	 * @return the paf response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafResponse reinitializeClientState(PafRequest cmdRequest) throws RemoteException, PafSoapException;
 	
 	/**
 	 * Logoff.
 	 *
 	 * @param logoffRequest the logoff request
 	 * @return the paf response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafResponse logoff(PafRequest logoffRequest) throws RemoteException, PafSoapException;
 	
 	/**
 	 * Gets the cell notes.
 	 *
 	 * @param getNotesRequest the get notes request
 	 * @return the cell notes
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafGetNotesResponse getCellNotes(PafGetNotesRequest getNotesRequest) throws RemoteException,	PafSoapException;
 	
 	/**
 	 * Save cell notes.
 	 *
 	 * @param getNotesRequest the get notes request
 	 * @return the paf save notes response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafSaveNotesResponse saveCellNotes(PafSaveNotesRequest getNotesRequest) throws RemoteException, PafSoapException;
 	
 	/**
 	 * Gets the cell notes information.
 	 *
 	 * @param pafRequest the paf request
 	 * @return the cell notes information
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafCellNoteInformationResponse getCellNotesInformation(PafRequest pafRequest) throws RemoteException, PafSoapException;
 	
 	/**
 	 * Gets the simple cell notes to export.
 	 *
 	 * @param pafSimpleCellNoteExportRequest the paf simple cell note export request
 	 * @return the simple cell notes to export
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafSimpleCellNoteExportResponse getSimpleCellNotesToExport(PafSimpleCellNoteExportRequest pafSimpleCellNoteExportRequest) throws RemoteException, PafSoapException;
 	
 	/**
 	 * Import simple cell notes.
 	 *
 	 * @param pafSimpleCellNoteImportRequest the paf simple cell note import request
 	 * @return the paf simple cell note import response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafSimpleCellNoteImportResponse importSimpleCellNotes(PafSimpleCellNoteImportRequest pafSimpleCellNoteImportRequest) throws RemoteException, PafSoapException;
 	
 	/**
 	 * Clear member tag data.
 	 *
 	 * @param filteredMbrTagsRequest the filtered mbr tags request
 	 * @return the paf success response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 */
 	public PafSuccessResponse clearMemberTagData(PafFilteredMbrTagRequest filteredMbrTagsRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
 	
 	/**
 	 * Export member tag data.
 	 *
 	 * @param filteredMbrTagsRequest the filtered mbr tags request
 	 * @return the paf get member tag data response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 */
 	public PafGetMemberTagDataResponse exportMemberTagData(PafFilteredMbrTagRequest filteredMbrTagsRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
 	
 	/**
 	 * Gets the member tag defs.
 	 *
 	 * @param memberTagDefsRequest the member tag defs request
 	 * @return the member tag defs
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 */
 	public PafGetMemberTagDefsResponse getMemberTagDefs(PafGetMemberTagDefsRequest memberTagDefsRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
 	
 	/**
 	 * Gets the member tag info.
 	 *
 	 * @param filteredMbrTagsRequest the filtered mbr tags request
 	 * @return the member tag info
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 */
 	public PafGetMemberTagInfoResponse getMemberTagInfo(PafFilteredMbrTagRequest filteredMbrTagsRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
 	
 	/**
 	 * Import member tag data.
 	 *
 	 * @param importMemberTagRequest the import member tag request
 	 * @return the paf success response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 */
 	public PafSuccessResponse importMemberTagData(PafImportMemberTagRequest importMemberTagRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
 	
 	/**
 	 * Rename member tag data.
 	 *
 	 * @param filteredMbrTagsRequest the filtered mbr tags request
 	 * @return the paf success response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 */
 	public PafSuccessResponse renameMemberTagData(PafFilteredMbrTagRequest filteredMbrTagsRequest) throws RemoteException, PafSoapException;
 	
 	/**
 	 * Save member tag data.
 	 *
 	 * @param saveMbrTagsRequest the save mbr tags request
 	 * @return the paf success response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 */
 	public PafSuccessResponse saveMemberTagData(PafSaveMbrTagRequest saveMbrTagsRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
 	
 	/**
 	 * Gets the user names for security groups.
 	 *
 	 * @param groupSecurityRequest the group security request
 	 * @return the user names for security groups
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAbletoGetLDAPContext the paf not ableto get ldap context
 	 */
 	public PafUserNamesforSecurityGroupsResponse getUserNamesForSecurityGroups(PafUserNamesforSecurityGroupsRequest groupSecurityRequest)throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException, PafNotAbletoGetLDAPContext;
 	
 	/**
 	 * Gets the paf groups.
 	 *
 	 * @param groupSecurityRequest the group security request
 	 * @return the paf groups
 	 * @throws RemoteException the remote exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAbletoGetLDAPContext the paf not ableto get ldap context
 	 */
 	public PafGroupSecurityResponse getPafGroups(PafGroupSecurityRequest groupSecurityRequest ) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException, PafNotAbletoGetLDAPContext;
 	
 	/**
 	 * Sets the groups.
 	 *
 	 * @param paceGroupRequest the pace group request
 	 * @return the paf set pace groups response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 */
 	public PafSetPaceGroupsResponse setGroups(PafSetPaceGroupsRequest paceGroupRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
 	
 	/**
 	 * Gets the groups.
 	 *
 	 * @param paceGroupRequest the pace group request
 	 * @return the groups
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 */
 	public PafGetPaceGroupsResponse getGroups(PafGetPaceGroupsRequest paceGroupRequest) throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException;
 	
 	/**
 	 * Verify users.
 	 *
 	 * @param req the req
 	 * @return the paf verify users response
 	 * @throws RemoteException the remote exception
 	 * @throws PafSoapException the paf soap exception
 	 * @throws PafNotAuthenticatedSoapException the paf not authenticated soap exception
 	 * @throws PafNotAuthorizedSoapException the paf not authorized soap exception
 	 * @throws PafNotAbletoGetLDAPContext the paf not ableto get ldap context
 	 */
 	public PafVerifyUsersResponse verifyUsers(PafVerifyUsersRequest req)throws RemoteException, PafSoapException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafNotAbletoGetLDAPContext;
 	
 	/**
 	 * Close client session.
 	 *
 	 * @param pafRequest the paf request
 	 * @return the paf success response
 	 * @throws RemoteException the remote exception
 	 */
 	public PafSuccessResponse closeClientSession(PafRequest pafRequest) throws RemoteException;
 	
 	/**
 	 * Checks if is session active.
 	 *
 	 * @param pafRequest the paf request
 	 * @return the paf success response
 	 * @throws RemoteException the remote exception
 	 */
 	public PafSuccessResponse isSessionActive(PafRequest pafRequest) throws RemoteException;
 
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
 	public ValidationResponse validateUserSecurity(ValidateUserSecurityRequest validateUserSecurityReq) throws RemoteException, PafNotAuthenticatedSoapException, PafNotAuthorizedSoapException, PafSoapException;
 	
 	
 	
	

 }
