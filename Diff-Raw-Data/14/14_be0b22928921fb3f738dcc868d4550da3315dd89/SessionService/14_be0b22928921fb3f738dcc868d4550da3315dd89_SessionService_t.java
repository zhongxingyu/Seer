 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.jasig.portlet.blackboardvcportlet.service;
 
 import com.elluminate.sas.*;
 import freemarker.template.utility.StringUtil;
 import org.jasig.portlet.blackboardvcportlet.dao.*;
 import org.jasig.portlet.blackboardvcportlet.data.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.ws.client.core.WebServiceTemplate;
 import javax.activation.DataHandler;
 import javax.mail.util.ByteArrayDataSource;
 import javax.portlet.PortletPreferences;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Service class for manipulating Collaborate sessions and their persistent
  * Entities
  *
  * @author rgood
  */
 @Service
 public class SessionService
 {
 	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
     private boolean isInit = false;
     private BasicAuth user;
     @Autowired
     SessionDao sessionDao;
     @Autowired
     SessionUrlDao sessionUrlDao;
     @Autowired
    MailTemplateService jasigMailTemplateService;
     @Autowired
     UserService userService;
     @Autowired
     RecordingService recordingService;
     @Autowired
     SessionExtParticipantDao sessionExtParticipantDao;
     @Autowired
     SessionPresentationDao sessionPresentationDao;
     @Autowired
     SessionMultimediaDao sessionMultimediaDao;
 	@Autowired
 	private WebServiceTemplate webServiceTemplate;
 
 	public List<Session> getSessionsForUser(String uid) {
         List<Session> sessionList = sessionDao.getSessionsForUser(uid);
         for (int i = 0; i < sessionList.size(); i++) {
             
             if ((sessionList.get(i).getChairList()!=null&&sessionList.get(i).getChairList().indexOf(uid+",") != -1)||(sessionList.get(i).getCreatorId().equals(uid))||(sessionList.get(i).getChairList()!=null&&sessionList.get(i).getChairList().endsWith(uid)))
             {
                 sessionList.get(i).setCurrUserCanEdit(true);
             } 
             else 
             {
                 sessionList.get(i).setCurrUserCanEdit(false);
             }
         }
         return sessionList;
     }
 
     public Session getSession(long sessionId) {
         logger.debug("getSession called");
         return sessionDao.getSession(sessionId);
     }
 
     public SessionUrl getSessionUrl(SessionUrlId sessionUrlId, PortletPreferences prefs) {
         // Guest url uses user id set to -1 from the DB
         if (sessionUrlId.getUserId() == null) {
             sessionUrlId.setUserId("-1");
         }
         try {
             SessionUrl sessionUrl = sessionUrlDao.getSessionUrl(sessionUrlId);
             if (sessionUrl != null) {
                 logger.debug("found session URL in DB");
                 return sessionUrl;
             }
         } catch (Exception e) {
             logger.error("Error gettingSessionUrl()", e);
         }
         SessionUrl sessionUrl = new SessionUrl();
         sessionUrl.setDisplayName(sessionUrlId.getDisplayName());
         sessionUrl.setSessionId(sessionUrlId.getSessionId());
         if (!sessionUrlId.getUserId().equals("-1")) {
             sessionUrl.setUserId(sessionUrlId.getUserId());
         }
 
         if (!this.isInit()) {
             doInit(prefs);
         }
 
         try {
             logger.debug("getting session url from Collaborate");
 			BuildSessionUrl buildSessionUrl = new BuildSessionUrl();
 			buildSessionUrl.setSessionId(sessionUrl.getSessionId());
 			buildSessionUrl.setDisplayName(sessionUrl.getDisplayName());
 			buildSessionUrl.setUserId(sessionUrl.getUserId());
 			UrlResponse urlResponse = (UrlResponse)webServiceTemplate.marshalSendAndReceive(buildSessionUrl);
 
             sessionUrl.setUrl(urlResponse.getUrl());
             sessionUrl.setLastUpdated(new Date());
             if (sessionUrl.getUserId() == null) {
                 sessionUrl.setUserId("-1");
             }
             sessionUrl.setLastUpdated(new Date());
             sessionUrlDao.saveSessionUrl(sessionUrl);
 
         } catch (Exception e) {
             logger.error("Error getting/storing session URL", e);
         }
 
         return sessionUrl;
     }
 
     public void deleteSession(long sessionId, PortletPreferences prefs) throws Exception {
         logger.debug("deleteSession called for :" + sessionId);
         if (!this.isInit()) {
             doInit(prefs);
         }     
 
         try { 
             
             Session session = sessionDao.getSession(sessionId);
             
             // Call Web Service Operation
             logger.debug("deleting session multimedia");
             deleteSessionMultimedia(prefs, sessionId);
 
             SessionPresentation sessionPresentation = getSessionPresentation(Long.toString(sessionId));
 
             if (sessionPresentation != null) {
                 logger.debug("deleting session presentation");
                 deleteSessionPresentation(prefs, sessionId, sessionPresentation.getPresentationId());
             }
 
             logger.debug("Calling removeSession:" + sessionId);
             try {
 				RemoveSession removeSession = new RemoveSession();
 				removeSession.setSessionId(sessionId);
 				SuccessResponse successResponse = (SuccessResponse)webServiceTemplate.marshalSendAndReceive(removeSession);
                 logger.debug("removeSession called, returned:" + successResponse.isSuccess());
             } catch (Exception e) {
                 logger.error("RemoveSession Error:", e);
             }
             
             logger.debug("Deleting session urls");
             sessionUrlDao.deleteSessionUrls(sessionId);
             logger.debug("Finished deleting session urls");
 
             logger.debug("Now deleting session");
             sessionDao.deleteSession(sessionId);
             logger.debug("Finished deleting session");
             
             notifyOfDeletion(session);
             
             logger.debug("Deleting session ext participants");
             sessionExtParticipantDao.deleteAllExtParticipants(sessionId);
             logger.debug("Finished deleting ext participants");
             
 
         } catch (Exception ex) {
             logger.error(ex.toString());
             throw ex;
         }
 
     }
 
     public List<Session> getAllSessions() {
         logger.debug("getAllSessions called");
         List<Session> sessions = sessionDao.getAllSesssions();
         for (int i = 0; i < sessions.size(); i++) {
             sessions.get(i).setCurrUserCanEdit(true);
         }
         return sessions;
     }
 
     public void createEditSession(Session session, PortletPreferences prefs, List<User> extParticipantList) throws Exception {
         if (!this.isInit()) {
             doInit(prefs);
         }
 
         try { // Call Web Service Operation
             logger.debug("Setup session web service call");
             logger.debug("Calling setSession:" + session.getSessionId());
 			SessionResponseCollection sessionResponseCollection = null;
             if (session.getSessionId() > 0) {
                 logger.debug("Existing session, calling updateSession");
 				UpdateSession updateSession = new UpdateSession();
 				updateSession.setSessionId(session.getSessionId());
 				updateSession.setStartTime(session.getStartTime().getTime());
 				updateSession.setEndTime(session.getEndTime().getTime());
 				updateSession.setSessionName(session.getSessionName());
 				updateSession.setAccessType(session.getAccessType());
 				updateSession.setBoundaryTime(session.getBoundaryTime());
 				updateSession.setChairList(session.getChairList());
 				updateSession.setChairNotes(session.getChairNotes());
 				updateSession.setGroupingList(session.getGroupingList());
 				updateSession.setMaxTalkers(session.getMaxTalkers());
 				updateSession.setMaxCameras(session.getMaxCameras());
 				updateSession.setMustBeSupervised(session.isMustBeSupervised());
 				updateSession.setNonChairList(session.getNonChairList());
 				updateSession.setNonChairNotes(session.getNonChairNotes());
 				updateSession.setOpenChair(session.isOpenChair());
 				updateSession.setPermissionsOn(session.isPermissionsOn());
 				updateSession.setRaiseHandOnEnter(session.isRaiseHandOnEnter());
 				updateSession.setRecordingModeType(session.getRecordingModeType());
 				updateSession.setReserveSeats(session.getReserveSeats());
 				updateSession.setSecureSignOn(session.isSecureSignOn());
 				updateSession.setAllowInSessionInvites(session.isAllowInSessionInvites());
 				updateSession.setHideParticipantNames(session.isHideParticipantNames());
 				sessionResponseCollection = (SessionResponseCollection)webServiceTemplate.marshalSendAndReceive(updateSession);
             } else {
                 logger.debug("New session, calling setSession");
 				SetSession setSession = new SetSession();
 				setSession.setCreatorId(session.getCreatorId());
 				setSession.setStartTime(session.getStartTime().getTime());
 				setSession.setEndTime(session.getEndTime().getTime());
 				setSession.setSessionName(session.getSessionName());
 				setSession.setAccessType(session.getAccessType());
 				setSession.setBoundaryTime(session.getBoundaryTime());
 				setSession.setChairList(session.getChairList());
 				setSession.setChairNotes(session.getChairNotes());
 				setSession.setMaxTalkers(session.getMaxTalkers());
 				setSession.setMaxCameras(session.getMaxCameras());
 				setSession.setMustBeSupervised(session.isMustBeSupervised());
 				setSession.setNonChairList(session.getNonChairList());
 				setSession.setNonChairNotes(session.getNonChairNotes());
 				setSession.setOpenChair(session.isOpenChair());
 				setSession.setPermissionsOn(session.isPermissionsOn());
 				setSession.setRaiseHandOnEnter(session.isRaiseHandOnEnter());
 				setSession.setRecordingModeType(session.getRecordingModeType());
 				setSession.setReserveSeats(session.getReserveSeats());
 				setSession.setSecureSignOn(session.isSecureSignOn());
 				setSession.setAllowInSessionInvites(session.isAllowInSessionInvites());
 				setSession.setHideParticipantNames(session.isHideParticipantNames());
 
 				sessionResponseCollection = (SessionResponseCollection)webServiceTemplate.marshalSendAndReceive(setSession);
                 logger.debug("setSession called, recieved response");
             }
 
 			for (SessionResponse sessionResponse : sessionResponseCollection.getSessionResponses())
 			{
                 logger.debug("Setting sessionId");
                 session.setSessionId(sessionResponse.getSessionId());
                 session.setLastUpdated(new Date());
                 logger.debug("Storing session");
                 this.storeSession(session);
                 logger.debug("Session stored");
             }
 
             logger.debug("Update recordings associated with session");
             List<RecordingShort> recordings = recordingService.getRecordingsForSession(session.getSessionId());
             if (recordings != null) {
                 boolean changed;
                 for (int i = 0; i < recordings.size(); i++) {
                     changed=false;
                     if ((recordings.get(i).getChairList()==null&&session.getChairList()!=null)
                             ||(session.getChairList()==null&&recordings.get(i).getChairList()!=null)
                             ||(!recordings.get(i).getChairList().equals(session.getChairList()))) 
                     {
                         changed=true;
                         recordings.get(i).setChairList(session.getChairList());
                     }
                     if ((recordings.get(i).getNonChairList()==null&&session.getNonChairList()!=null)
                             ||(session.getNonChairList()==null&&recordings.get(i).getNonChairList()!=null)
                             ||(!recordings.get(i).getNonChairList().equals(session.getNonChairList()))) 
                     {
                         changed=true;
                         recordings.get(i).setNonChairList(session.getNonChairList());
                     }
                     if (changed) 
                     {
                         logger.debug("Saving updated recording short");
                         recordingService.saveRecordingShort(recordings.get(i));
                     }
 
                 }
             }
 
 
             logger.debug("Finished updating recordings");
 
             this.deleteExtParticipants(session.getSessionId());
             for (int i = 0; i < extParticipantList.size(); i++) {
                 this.addExtParticipant(extParticipantList.get(i), session.getSessionId());
             }
 
             String callBackUrl = prefs.getValue("callbackUrl", null);
             logger.debug("Setting callback Url to:" + callBackUrl);
             if (callBackUrl != null) {
 				SetApiCallbackUrl setApiCallbackUrl = new SetApiCallbackUrl();
 				setApiCallbackUrl.setApiCallbackUrl(callBackUrl);
 				SuccessResponse successResponse = (SuccessResponse)webServiceTemplate.marshalSendAndReceive(setApiCallbackUrl);
                 logger.debug("callBackUrl response:" + successResponse.isSuccess());
             }
 
         } catch (Exception ex) {
             logger.error(ex.toString());
             throw ex;
         }
     }
 
     public User getExtParticipant(long sessionId, String email) {
         SessionExtParticipantId sessionExtParticipantId = new SessionExtParticipantId();
         sessionExtParticipantId.setSessionId(sessionId);
         sessionExtParticipantId.setParticipantEmail(email);
 
         SessionExtParticipant sessionExtParticipant = sessionExtParticipantDao.getSessionExtParticipant(sessionExtParticipantId);
 
         User extParticipant = new User();
         extParticipant.setUid(email);
         extParticipant.setEmail(email);
 
         if (sessionExtParticipant != null) {
             extParticipant.setDisplayName(sessionExtParticipant.getDisplay_name());
         }
 
         return extParticipant;
     }
 
     public boolean isInit() {
         return this.isInit;
     }
 
     public void doInit(PortletPreferences prefs) {
         logger.debug("doInit called");
         user = new BasicAuth();
         user.setName(prefs.getValue("wsusername", null));
         user.setPassword(prefs.getValue("wspassword", null));
         isInit = true;
     }
 
     public void storeSession(Session session) {
         sessionDao.saveSession(session);
     }
 
     public void notifyModerators(PortletPreferences prefs, User creator, Session session, List<User> users,String launchUrl) throws Exception {
         logger.debug("notifyModerators called");
         String[] substitutions;
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
         String creatorDetails = creator.getDisplayName() + " (" + creator.getEmail() + ")";
        
         List<String> toList;
         for (int i = 0; i < users.size(); i++) {
             logger.debug("user name:" + users.get(i).getDisplayName());
             logger.debug("user email:" + users.get(i).getDisplayName());
 
             toList = new ArrayList<String>();
             toList.add(users.get(i).getEmail());
 
             substitutions = new String[]{users.get(i).getDisplayName(), creatorDetails, session.getSessionName(), dateFormat.format(session.getStartTime()), dateFormat.format(session.getEndTime()), launchUrl, creatorDetails};
			jasigMailTemplateService.sendEmailUsingTemplate(creator.getEmail(), toList, null, substitutions, "moderatorMailMessage");
         }
         logger.debug("finished");
     }
     
     public void notifyOfDeletion(Session session) throws Exception {
         logger.debug("notifyOfDeletion called");
         String[] substitutions;
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
         
         User creator = userService.getUserDetails(session.getCreatorId());
         String creatorDetails= "Unknown user";
         
         if (creator!=null)
         {
             creatorDetails = creator.getDisplayName() + " (" + creator.getEmail() + ")";
             logger.debug("creatorDetails set:"+creatorDetails);
         }
              
         List<User> users = new ArrayList<User>();
         
         User lookupUser;     
         
         logger.debug("Finished initialisation of creator and variables");
         if (session.getChairList()!=null&&!session.getChairList().equals(""))
         {
                 logger.debug("Adding chair list users");
                 String[] chairList = StringUtil.split(session.getChairList(),',');
                 
                 for (int i=0;i<chairList.length;i++)
                 {
                     lookupUser=userService.getUserDetails(chairList[i]);
                     if (lookupUser!=null)
                     {
                         users.add(lookupUser);
                     }
                    
                 }
               
         }
             
         if (session.getNonChairList()!=null&&!session.getNonChairList().equals(""))
         {
                 logger.debug("Adding nonchair list users");
                 String[] nonChairList = StringUtil.split(session.getNonChairList(),',');
                              
                 for (int i=0;i<nonChairList.length;i++)
                 {
                     lookupUser=userService.getUserDetails(nonChairList[i]);
                     if (lookupUser!=null)
                     {
                         users.add(lookupUser);
                     }
                     else
                     {
                         lookupUser = this.getExtParticipant(session.getSessionId(),nonChairList[i]);
                         if (lookupUser==null)
                         {
                             lookupUser = new User();
                             lookupUser.setEmail(nonChairList[i]);
                         }
                         
                         users.add(lookupUser);
                     }
                 }
                           
         }
         
         List<String> toList;
         for (int i = 0; i < users.size(); i++) {
             logger.debug("user name:" + users.get(i).getDisplayName());
             logger.debug("user email:" + users.get(i).getDisplayName());
 
             toList = new ArrayList<String>();
             toList.add(users.get(i).getEmail());
 
             substitutions = new String[]{users.get(i).getDisplayName(), creatorDetails, session.getSessionName(), dateFormat.format(session.getStartTime()), dateFormat.format(session.getEndTime()), creatorDetails};
			jasigMailTemplateService.sendEmailUsingTemplate(creator.getEmail(), toList, null, substitutions, "sessionDeletionMessage");
         }
         logger.debug("finished");
     }
 
     public void notifyIntParticipants(PortletPreferences prefs, User creator, Session session, List<User> users, String launchUrl) throws Exception {
         logger.debug("notifyIntParticipants called");
         String[] substitutions;
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
         String creatorDetails = creator.getDisplayName() + " (" + creator.getEmail() + ")";
 
         List<String> toList;
         for (int i = 0; i < users.size(); i++) {
             toList = new ArrayList<String>();
             toList.add(users.get(i).getEmail());
             substitutions = new String[]{users.get(i).getDisplayName(), creatorDetails, session.getSessionName(), dateFormat.format(session.getStartTime()), dateFormat.format(session.getEndTime()), launchUrl, creatorDetails};
			jasigMailTemplateService.sendEmailUsingTemplate(creator.getEmail(), toList, null, substitutions, "intParticipantMailMessage");
         }
         logger.debug("finished");
     }
 
     public void notifyExtParticipants(PortletPreferences prefs, User creator, Session session, List<User> users) throws Exception {
         logger.debug("notifyExtParticipants called");
         String[] substitutions;
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
         String creatorDetails = creator.getDisplayName() + " (" + creator.getEmail() + ")";
         SessionUrl sessionUrl;
         SessionUrlId sessionUrlId;
         List<String> toList;
         // Get the guest launch URL
         sessionUrlId = new SessionUrlId();
         sessionUrlId.setSessionId(session.getSessionId());
         sessionUrlId.setDisplayName("Guest");
         sessionUrl = this.getSessionUrl(sessionUrlId, prefs);       
         String extParticipantUrl;
         for (int i = 0; i < users.size(); i++) {
             toList = new ArrayList<String>();
             toList.add(users.get(i).getEmail());
             extParticipantUrl = sessionUrl.getUrl().replaceFirst("Guest",URLEncoder.encode(users.get(i).getDisplayName(), "UTF-8"));
             substitutions = new String[]{users.get(i).getDisplayName(), creatorDetails, session.getSessionName(), dateFormat.format(session.getStartTime()), dateFormat.format(session.getEndTime()), extParticipantUrl, creatorDetails};
			jasigMailTemplateService.sendEmailUsingTemplate(creator.getEmail(), toList, null, substitutions, "extParticipantMailMessage");
         }
         logger.debug("finished");
     }
 
     public void addExtParticipant(User user, long sessionId) {
         logger.debug("addExtParticipant called for session,user: (" + sessionId + "," + user.getEmail() + ")");
         SessionExtParticipantId sessionExtParticipantId = new SessionExtParticipantId();
         SessionExtParticipant sessionExtParticipant = new SessionExtParticipant();
 
         sessionExtParticipantId.setParticipantEmail(user.getEmail());
         sessionExtParticipantId.setSessionId(sessionId);
 
         sessionExtParticipant.setSessionExtParticipantId(sessionExtParticipantId);
         sessionExtParticipant.setDisplay_name(user.getDisplayName());
         sessionExtParticipantDao.storeSessionExtParticipant(sessionExtParticipant);
     }
 
     public void deleteExtParticipants(long sessionId) {
         logger.debug("deleteExtParticipants called for :" + sessionId);
         sessionExtParticipantDao.deleteAllExtParticipants(sessionId);
     }
 
     public SessionPresentation getSessionPresentation(String sessionId) {
         logger.debug("getSessionPresentation called");
         List<SessionPresentation> sessionPresentationList = sessionPresentationDao.getSessionPresentation(sessionId);
 
         if (sessionPresentationList != null && sessionPresentationList.size() > 0) {
             return sessionPresentationList.get(0);
         } else {
             logger.debug("getSessionPresentation is going to return null");
             return null;
         }
     }
 
     public void deleteSessionPresentation(PortletPreferences prefs, long sessionId, long presentationId) throws Exception {
         logger.debug("deleteSessionPresentation called");
         if (!this.isInit()) {
             doInit(prefs);
         }
 
         try {
             logger.debug("Setup session web service call");
 			RemoveSessionPresentation removeSessionPresentation = new RemoveSessionPresentation();
 			removeSessionPresentation.setSessionId(sessionId);
 			removeSessionPresentation.setPresentationId(presentationId);
 			SuccessResponse successResponse = (SuccessResponse)webServiceTemplate.marshalSendAndReceive(removeSessionPresentation);
             logger.debug("removeSessionPresentation returned:" + successResponse.isSuccess());
 			RemoveRepositoryPresentation removeRepositoryPresentation = new RemoveRepositoryPresentation();
 			removeRepositoryPresentation.setPresentationId(presentationId);
 			successResponse = (SuccessResponse)webServiceTemplate.marshalSendAndReceive(removeRepositoryPresentation);
             logger.debug("removeRepositoryPresentation returned:" + successResponse.isSuccess());
             sessionPresentationDao.deleteSessionPresentation(Long.toString(presentationId));
         } catch (Exception e) {
             logger.error("Exception caught deleting session presentation", e);
             throw e;
         }
     }
 
     public void addSessionPresentation(String uid, PortletPreferences prefs, long sessionId, MultipartFile file) throws Exception {
         logger.debug("addSessionPresentation called");
         if (!this.isInit()) {
             doInit(prefs);
         }
 
         try { // Call Web Service Operation
             logger.debug("Setup session web service call");
             ByteArrayDataSource rawData = new ByteArrayDataSource(file.getBytes(), file.getContentType());
             logger.debug("ByteArrayDataSource created");
             DataHandler dataHandler = new DataHandler(rawData);
             logger.debug("DataHandler created from ByteArrayDataSource");
 			UploadRepositoryContent uploadRepositoryContent = new UploadRepositoryContent();
 			uploadRepositoryContent.setCreatorId(uid);
 			uploadRepositoryContent.setFilename(file.getOriginalFilename());
 			uploadRepositoryContent.setContent(dataHandler);
 			PresentationResponseCollection presentationResponseCollection = (PresentationResponseCollection)webServiceTemplate.marshalSendAndReceive(uploadRepositoryContent);
             logger.debug("uploadRepositoryPresentation called");
 
             if (presentationResponseCollection != null) {
                 SessionPresentation sessionPresentation = new SessionPresentation();
                 sessionPresentation.setCreatorId(uid);
                 sessionPresentation.setDateUploaded(new Date());
                 sessionPresentation.setFileName(file.getOriginalFilename());
                 sessionPresentation.setSessionId(Long.toString(sessionId));
 				for (PresentationResponse presentationResponse : presentationResponseCollection.getPresentationResponses())
 				{
                  	SetSessionPresentation setSessionPresentation = new SetSessionPresentation();
 					setSessionPresentation.setSessionId(sessionId);
 					setSessionPresentation.setPresentationId(presentationResponse.getPresentationId());
 					SuccessResponse successResponse = (SuccessResponse)webServiceTemplate.marshalSendAndReceive(setSessionPresentation);
                     if (successResponse.isSuccess()) {
                         sessionPresentation.setPresentationId(presentationResponse.getPresentationId());
                         sessionPresentationDao.storeSessionPresentation(sessionPresentation);
                     }
                 }
             } else {
                 logger.error("uploadRepositoryPresentation was null");
             }
         } catch (Exception e) {
             throw e;
         }
     }
 
     public void deleteSessionMultimedia(PortletPreferences prefs, long sessionId) throws Exception {
         logger.debug("deleteSessionMultimediaFiles called");
         List<SessionMultimedia> sessionMultimediaList = this.getSessionMultimedia(sessionId);
         if (!this.isInit()) {
             doInit(prefs);
         }
 
         try { // Call Web Service Operation
             logger.debug("Setup session web service call");
             for (SessionMultimedia sessionMultimedia : sessionMultimediaList) {
 				RemoveSessionMultimedia removeSessionMultimedia = new RemoveSessionMultimedia();
 				removeSessionMultimedia.setSessionId(sessionId);
 				removeSessionMultimedia.setMultimediaId(sessionMultimedia.getMultimediaId());
 				SuccessResponse successResponse = (SuccessResponse)webServiceTemplate.marshalSendAndReceive(removeSessionMultimedia);
                 logger.debug("deleteSessionMultimedia returned:" + successResponse.isSuccess());
 				RemoveRepositoryMultimedia removeRepositoryMultimedia = new RemoveRepositoryMultimedia();
 				removeRepositoryMultimedia.setMultimediaId(sessionMultimedia.getMultimediaId());
 				successResponse = (SuccessResponse)webServiceTemplate.marshalSendAndReceive(removeRepositoryMultimedia);
                 logger.debug("delete multimediaId (" + sessionMultimedia.getMultimediaId() + " returned:" + successResponse.isSuccess());
                 sessionMultimediaDao.deleteSessionMultimedia(sessionMultimedia.getMultimediaId());
             }
         } catch (Exception e) {
             logger.error("Exception caught removing multimedia files", e);
             throw e;
         }
     }
 
     public void deleteSessionMultimediaFiles(PortletPreferences prefs, long sessionId, String[] multimediaIds) throws Exception {
         logger.debug("deleteSessionMultimediaFiles called");
         List<SessionMultimedia> sessionMultimediaList = this.getSessionMultimedia(sessionId);
 
         if (!this.isInit()) {
             doInit(prefs);
         }
 
         /* Call set session to remove the old ids, then remove them from
          the repository */
         try { // Call Web Service Operation
             logger.debug("Setup session web service call");
 
             for (String multiMediaId : multimediaIds) {
                 RemoveSessionMultimedia removeSessionMultimedia = new RemoveSessionMultimedia();
 				removeSessionMultimedia.setSessionId(sessionId);
 				removeSessionMultimedia.setMultimediaId(Long.valueOf(multiMediaId));
 				SuccessResponse successResponse = (SuccessResponse)webServiceTemplate.marshalSendAndReceive(removeSessionMultimedia);
                 if (successResponse.isSuccess()) {
 					RemoveRepositoryMultimedia removeRepositoryMultimedia = new RemoveRepositoryMultimedia();
 					removeRepositoryMultimedia.setMultimediaId(Long.valueOf(multiMediaId));
 					SuccessResponse response = (SuccessResponse)webServiceTemplate.marshalSendAndReceive(removeRepositoryMultimedia);
                     logger.debug("delete multimediaId (" + multiMediaId + " returned:" + response.isSuccess());
                     sessionMultimediaDao.deleteSessionMultimedia(Long.valueOf(multiMediaId));
                 } else {
                     throw new Exception("Error deleting session multimedia.");
                 }
             }
         } catch (Exception e) {
             logger.error("Exception caught deleting session multimedia", e);
             throw e;
         }
 
     }
 
     public List<SessionMultimedia> getSessionMultimedia(long sessionId) {
         return sessionMultimediaDao.getSessionMultimedia(Long.toString(sessionId));
     }
 
     public void addSessionMultimedia(String uid, PortletPreferences prefs, long sessionId, MultipartFile file) throws Exception {
         logger.debug("addSessionMultimedia called");
         if (!this.isInit()) {
             doInit(prefs);
         }
 
         try { // Call Web Service Operation
             logger.debug("Setup session web service call");
             ByteArrayDataSource rawData = new ByteArrayDataSource(file.getBytes(), file.getContentType());
             logger.debug("ByteArrayDataSource created");
             DataHandler dataHandler = new DataHandler(rawData);
             logger.debug("DataHandler created from ByteArrayDataSource");
 			UploadRepositoryContent uploadRepositoryContent = new UploadRepositoryContent();
 			uploadRepositoryContent.setCreatorId(uid);
 			uploadRepositoryContent.setFilename(file.getOriginalFilename());
 			uploadRepositoryContent.setContent(dataHandler);
             MultimediaResponseCollection multimediaResponseCollection = (MultimediaResponseCollection)webServiceTemplate.marshalSendAndReceive(uploadRepositoryContent);
             logger.debug("uploadRepositoryMultimedia called");
 
             if (multimediaResponseCollection != null) {
                 SessionMultimedia sessionMultimedia = new SessionMultimedia();
                 sessionMultimedia.setCreatorId(uid);
                 sessionMultimedia.setDateUploaded(new Date());
                 sessionMultimedia.setFileName(file.getOriginalFilename());
                 sessionMultimedia.setSessionId(Long.toString(sessionId));
                 List<SessionMultimedia> sessionMultimediaList = sessionMultimediaDao.getSessionMultimedia(Long.toString(sessionId));
                 String multimediaIds = "";
                 for (SessionMultimedia sm : sessionMultimediaList) {
                     multimediaIds += sm.getMultimediaId();
                     multimediaIds += ",";
                 }
 
                 for (MultimediaResponse multimediaResponse : multimediaResponseCollection.getMultimediaResponses()) {
                     sessionMultimedia.setMultimediaId(multimediaResponse.getMultimediaId());
                     multimediaIds += sessionMultimedia.getMultimediaId();
 					SetSessionMultimedia setSessionMultimedia = new SetSessionMultimedia();
 					setSessionMultimedia.setSessionId(sessionId);
 					setSessionMultimedia.setMultimediaIds(multimediaIds);
 					SuccessResponse successResponse = (SuccessResponse)webServiceTemplate.marshalSendAndReceive(setSessionMultimedia);
                     if (successResponse.isSuccess()) {
                         sessionMultimediaDao.saveSessionMultimedia(sessionMultimedia);
                     }
                 }
             } else {
                 logger.error("uploadRepositoryMultimedia was null");
             }
         } catch (Exception e) {
             throw e;
         }
     }
 }
