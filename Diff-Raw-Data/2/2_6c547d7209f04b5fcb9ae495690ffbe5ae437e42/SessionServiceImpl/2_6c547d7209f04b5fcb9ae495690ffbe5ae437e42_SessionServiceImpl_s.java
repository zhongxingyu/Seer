 package org.jasig.portlet.blackboardvcportlet.service.impl;
 
 import java.util.Set;
 
 import javax.xml.bind.JAXBElement;
 
 import org.jasig.portlet.blackboardvcportlet.dao.ConferenceUserDao;
 import org.jasig.portlet.blackboardvcportlet.dao.SessionDao;
 import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
 import org.jasig.portlet.blackboardvcportlet.data.RecordingMode;
 import org.jasig.portlet.blackboardvcportlet.data.Session;
 import org.jasig.portlet.blackboardvcportlet.service.MailTemplateService;
 import org.jasig.portlet.blackboardvcportlet.service.RecordingService;
 import org.jasig.portlet.blackboardvcportlet.service.SessionForm;
 import org.jasig.portlet.blackboardvcportlet.service.SessionService;
 import org.jasig.portlet.blackboardvcportlet.service.util.SASWebServiceOperations;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.support.DataAccessUtils;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.elluminate.sas.BlackboardBuildSessionUrl;
 import com.elluminate.sas.BlackboardSessionResponse;
 import com.elluminate.sas.BlackboardSessionResponseCollection;
 import com.elluminate.sas.BlackboardSetSession;
 import com.elluminate.sas.BlackboardUrlResponse;
 
 @Service
 public class SessionServiceImpl implements SessionService
 {
 	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
 
     private SessionDao sessionDao;
     private ConferenceUserDao conferenceUserDao;
 	private MailTemplateService mailTemplateService;
 //	private UserService userService;
 	private RecordingService recordingService;
 //	private SessionPresentationDao sessionPresentationDao;
 //	private SessionMultimediaDao sessionMultimediaDao;
 	private SASWebServiceOperations sasWebServiceOperations;
 //	private ObjectFactory objectFactory;
 	
 	
 	@Autowired
 	public void setBlackboardSessionDao(SessionDao blackboardSessionDao) {
         this.sessionDao = blackboardSessionDao;
     }
 
     @Autowired
     public void setBlackboardUserDao(ConferenceUserDao blackboardUserDao) {
         this.conferenceUserDao = blackboardUserDao;
     }
 
     @Autowired
     public void setMailTemplateService(MailTemplateService mailTemplateService) {
         this.mailTemplateService = mailTemplateService;
     }
 
 //	@Autowired
 //	public void setUserService(UserService userService)
 //	{
 //		this.userService = userService;
 //	}
 
 	@Autowired
 	public void setRecordingService(RecordingService recordingService)
 	{
 		this.recordingService = recordingService;
 	}
 
 //	@Autowired
 //	public void setSessionPresentationDao(SessionPresentationDao sessionPresentationDao)
 //	{
 //		this.sessionPresentationDao = sessionPresentationDao;
 //	}
 //
 //	@Autowired
 //	public void setSessionMultimediaDao(SessionMultimediaDao sessionMultimediaDao)
 //	{
 //		this.sessionMultimediaDao = sessionMultimediaDao;
 //	}
 
 	@Autowired
 	public void setSasWebServiceOperations(SASWebServiceOperations sasWebServiceOperations)
 	{
 		this.sasWebServiceOperations = sasWebServiceOperations;
 	}
 
     @Override
     @Transactional
     public void createOrUpdateSession(ConferenceUser user, SessionForm sessionForm, boolean fullAccess) {
         if (sessionForm.isNewSession()) {
             final BlackboardSetSession setSession = new BlackboardSetSession();
             setSession.setCreatorId(user.getEmail());
             setSession.setSessionName(sessionForm.getSessionName());
             setSession.setStartTime(sessionForm.getStartTime().getMillis());
             setSession.setEndTime(sessionForm.getEndTime().getMillis());
             setSession.setBoundaryTime(sessionForm.getBoundaryTime());
 
             if (fullAccess) {
                 setSession.setMaxTalkers(sessionForm.getMaxTalkers());
                 setSession.setMaxCameras(sessionForm.getMaxCameras());
                 setSession.setMustBeSupervised(sessionForm.isMustBeSupervised());
                 setSession.setPermissionsOn(sessionForm.isPermissionsOn());
                 setSession.setRaiseHandOnEnter(sessionForm.isRaiseHandOnEnter());
                 final RecordingMode recordingMode = sessionForm.getRecordingMode();
                 if (recordingMode != null) {
                     setSession.setRecordingModeType(recordingMode.getBlackboardRecordingMode());
                 }
                 setSession.setHideParticipantNames(sessionForm.isHideParticipantNames());
                 setSession.setAllowInSessionInvites(sessionForm.isAllowInSessionInvites());
             }
 
             final Object objSessionResponse = sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/SetSession", setSession);
             JAXBElement<BlackboardSessionResponseCollection> jaxbSessionResponse = (JAXBElement<BlackboardSessionResponseCollection>) objSessionResponse;
             
             final BlackboardSessionResponseCollection sessionResponses = jaxbSessionResponse.getValue();
             final BlackboardSessionResponse sessionResponse = DataAccessUtils.singleResult(sessionResponses.getSessionResponses());
             
             
             BlackboardBuildSessionUrl buildGuestUrlRequest = new BlackboardBuildSessionUrl();
             buildGuestUrlRequest.setSessionId(sessionResponse.getSessionId());
             buildGuestUrlRequest.setDisplayName("GUEST_PLACEHOLDER");
             final Object objGuestUrlResponse = sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/BuildSessionUrl", buildGuestUrlRequest);
             JAXBElement<BlackboardUrlResponse> jaxbGuestUrlResponse = (JAXBElement<BlackboardUrlResponse>) objGuestUrlResponse;
             final String guestUrl = jaxbGuestUrlResponse.getValue().getUrl();
 
             //Remove guest username so that guest user's are prompted
            this.sessionDao.createSession(sessionResponse, guestUrl.replace("&amp;username=GUEST_PLACEHOLDER", ""));
         }
         else {
             //TODO just verifying access?
             this.getSession(user, sessionForm.getSessionId(), fullAccess);
         }
     }
 
     @Override
     public Session getSession(ConferenceUser user, long sessionId, boolean fullAccess) {
         final Session session = this.sessionDao.getSession(sessionId);
         if (session == null) {
             return null;
         }
         
         if (fullAccess || session.getCreator().equals(user)) {
             return session;
         }
         
         final Set<ConferenceUser> sessionChairs = this.sessionDao.getSessionChairs(session);
         if (sessionChairs.contains(user)) {
             return session;
         }
         
         final Set<ConferenceUser> sessionNonChairs = this.sessionDao.getSessionNonChairs(session);
         if (sessionNonChairs.contains(user)) {
             return session;
         }
         
         throw new RuntimeException("TODO better exception and error msg about illegal access");
     }
 	
 	
 	
 
 //	public Set<BlackboardSession> getSessionsForUser(String uid) {
 //        List<BlackboardSession> sessionList = blackboardSessionDao.getSessionsForUser(uid);
 //        for (BlackboardSession session : sessionList)
 //		{
 //            if ((session.getChairList()!=null && session.getChairList().indexOf(uid+",") != -1) || (session.getCreatorId().equals(uid)) || (session.getChairList() !=null && session.getChairList().endsWith(uid)))
 //            {
 //				session.setCurrUserCanEdit(true);
 //            } 
 //            else 
 //            {
 //				session.setCurrUserCanEdit(false);
 //            }
 //        }
 //        return sessionList;
 //    }
 //
 //    public BlackboardSession getSession(long sessionId) {
 //        logger.debug("getBlackboardSession called");
 //        return blackboardSessionDao.getSession(sessionId);
 //    }
 //
 //    public SessionUrl getSessionUrl(SessionUrlId sessionUrlId) {
 //        // Guest url uses user id set to -1 from the DB
 //        if (sessionUrlId.getUserId() == null) {
 //            sessionUrlId.setUserId("-1");
 //        }
 //        try {
 //            SessionUrl sessionUrl = sessionUrlDao.getSessionUrl(sessionUrlId);
 //            if (sessionUrl != null) {
 //                logger.debug("found session URL in DB");
 //                return sessionUrl;
 //            }
 //        } catch (Exception e) {
 //            logger.error("Error gettingSessionUrl()", e);
 //        }
 //        SessionUrl sessionUrl = new SessionUrlImpl();
 //        sessionUrl.setDisplayName(sessionUrlId.getDisplayName());
 //        sessionUrl.setSessionId(sessionUrlId.getSessionId());
 //        if (!sessionUrlId.getUserId().equals("-1")) {
 //            sessionUrl.setUserId(sessionUrlId.getUserId());
 //        }
 //
 //        try {
 //            logger.debug("getting session url from Collaborate");
 //			BuildSessionUrl buildSessionUrl = objectFactory.createBuildSessionUrl();
 //			buildSessionUrl.setSessionId(sessionUrl.getSessionId());
 //			buildSessionUrl.setDisplayName(sessionUrl.getDisplayName());
 //			buildSessionUrl.setUserId(sessionUrl.getUserId());
 //			JAXBElement<UrlResponse> response = (JAXBElement<UrlResponse>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/BuildSessionUrl", buildSessionUrl);
 //			UrlResponse urlResponse = response.getValue();
 //
 //            sessionUrl.setUrl(urlResponse.getUrl());
 //            sessionUrl.setLastUpdated(new Date());
 //            if (sessionUrl.getUserId() == null) {
 //                sessionUrl.setUserId("-1");
 //            }
 //            sessionUrl.setLastUpdated(new Date());
 //            sessionUrlDao.saveSessionUrl(sessionUrl);
 //
 //        } catch (Exception e) {
 //            logger.error("Error getting/storing session URL", e);
 //        }
 //
 //        return sessionUrl;
 //    }
 //
 //    public void deleteSession(long sessionId) throws Exception {
 //        logger.debug("deleteBlackboardSession called for :" + sessionId);
 //        try {
 //            
 //            BlackboardSession session = blackboardSessionDao.getSession(sessionId);
 //            
 //            // Call Web Service Operation
 //            logger.debug("deleting session multimedia");
 //            deleteSessionMultimedia(sessionId);
 //
 //            SessionPresentation sessionPresentation = getSessionPresentation(Long.toString(sessionId));
 //
 //            if (sessionPresentation != null) {
 //                logger.debug("deleting session presentation");
 //                deleteSessionPresentation(sessionId, sessionPresentation.getPresentationId());
 //            }
 //
 //            logger.debug("Calling removeSession:" + sessionId);
 //            try {
 //				RemoveBlackboardSession removeBlackboardSession = objectFactory.createRemoveSession();
 //				removeSession.setSessionId(sessionId);
 //				JAXBElement<SuccessResponse> response = (JAXBElement<SuccessResponse>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/RemoveSession", removeSession); 
 //				SuccessResponse successResponse = response.getValue();
 //                logger.debug("removeBlackboardSession called, returned:" + successResponse.isSuccess());
 //            } catch (Exception e) {
 //                logger.error("RemoveBlackboardSession Error:", e);
 //            }
 //            
 //            logger.debug("Deleting session urls");
 //            sessionUrlDao.deleteSessionUrls(sessionId);
 //            logger.debug("Finished deleting session urls");
 //
 //            logger.debug("Now deleting session");
 //            blackboardSessionDao.deleteSession(sessionId);
 //            logger.debug("Finished deleting session");
 //            
 //            notifyOfDeletion(session);
 //            
 //            logger.debug("Deleting session ext participants");
 //            sessionExtParticipantDao.deleteAllExtParticipants(sessionId);
 //            logger.debug("Finished deleting ext participants");
 //        } catch (Exception ex) {
 //            logger.error(ex.toString());
 //            throw ex;
 //        }
 //
 //    }
 //
 //    public List<BlackboardSession> getAllSessions() {
 //        logger.debug("getAllSessions called");
 //        List<BlackboardSession> sessions = blackboardSessionDao.getAllSesssions();
 //        for (BlackboardSession session : sessions) {
 //            session.setCurrUserCanEdit(true);
 //        }
 //        return sessions;
 //    }
 //
 //    public void createEditSession(BlackboardSession session, PortletPreferences prefs, List<User> extParticipantList) throws Exception {
 //
 //        try { // Call Web Service Operation
 //            logger.debug("Setup session web service call");
 //            logger.debug("Calling setSession:" + session.getSessionId());
 //			SessionResponseCollection sessionResponseCollection = null;
 //            if (session.getSessionId() > 0) {
 //                logger.debug("Existing session, calling updateSession");
 //				UpdateBlackboardSession updateBlackboardSession = objectFactory.createUpdateSession();
 //				updateSession.setSessionId(session.getSessionId());
 //				updateSession.setStartTime(session.getStartTime().getTime());
 //				updateSession.setEndTime(session.getEndTime().getTime());
 //				updateSession.setSessionName(session.getSessionName());
 //				updateSession.setAccessType(session.getAccessType());
 //				updateSession.setBoundaryTime(session.getBoundaryTime());
 //				updateSession.setChairList(session.getChairList());
 //				updateSession.setChairNotes(session.getChairNotes());
 //				updateSession.setGroupingList(session.getGroupingList());
 //				updateSession.setMaxTalkers(session.getMaxTalkers());
 //				updateSession.setMaxCameras(session.getMaxCameras());
 //				updateSession.setMustBeSupervised(session.isMustBeSupervised());
 //				updateSession.setNonChairList(session.getNonChairList());
 //				updateSession.setNonChairNotes(session.getNonChairNotes());
 //				updateSession.setOpenChair(session.isOpenChair());
 //				updateSession.setPermissionsOn(session.isPermissionsOn());
 //				updateSession.setRaiseHandOnEnter(session.isRaiseHandOnEnter());
 //				updateSession.setRecordingModeType(session.getRecordingModeType());
 //				updateSession.setReserveSeats(session.getReserveSeats());
 //				updateSession.setSecureSignOn(session.isSecureSignOn());
 //				updateSession.setAllowInSessionInvites(session.isAllowInSessionInvites());
 //				updateSession.setHideParticipantNames(session.isHideParticipantNames());
 //				JAXBElement<SessionResponseCollection> response = (JAXBElement<SessionResponseCollection>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/UpdateSession", updateSession);
 //				sessionResponseCollection = response.getValue();
 //            } else {
 //                logger.debug("New session, calling setSession");
 //				SetBlackboardSession setBlackboardSession = objectFactory.createSetSession();
 //				setSession.setCreatorId(session.getCreatorId());
 //				setSession.setStartTime(session.getStartTime().getTime());
 //				setSession.setEndTime(session.getEndTime().getTime());
 //				setSession.setSessionName(session.getSessionName());
 //				setSession.setAccessType(session.getAccessType());
 //				setSession.setBoundaryTime(session.getBoundaryTime());
 //				setSession.setChairList(session.getChairList());
 //				setSession.setChairNotes(session.getChairNotes());
 //				setSession.setMaxTalkers(session.getMaxTalkers());
 //				setSession.setMaxCameras(session.getMaxCameras());
 //				setSession.setMustBeSupervised(session.isMustBeSupervised());
 //				setSession.setNonChairList(session.getNonChairList());
 //				setSession.setNonChairNotes(session.getNonChairNotes());
 //				setSession.setOpenChair(session.isOpenChair());
 //				setSession.setPermissionsOn(session.isPermissionsOn());
 //				setSession.setRaiseHandOnEnter(session.isRaiseHandOnEnter());
 //				setSession.setRecordingModeType(session.getRecordingModeType());
 //				setSession.setReserveSeats(session.getReserveSeats());
 //				setSession.setSecureSignOn(session.isSecureSignOn());
 //				setSession.setAllowInSessionInvites(session.isAllowInSessionInvites());
 //				setSession.setHideParticipantNames(session.isHideParticipantNames());
 //				JAXBElement<SessionResponseCollection> response = (JAXBElement<SessionResponseCollection>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/SetSession", setSession);
 //				sessionResponseCollection = (SessionResponseCollection)response.getValue();
 //                logger.debug("setBlackboardSession called, received response");
 //            }
 //
 //			for (SessionResponse sessionResponse : sessionResponseCollection.getSessionResponses())
 //			{
 //                logger.debug("Setting sessionId");
 //                session.setSessionId(sessionResponse.getSessionId());
 //                session.setLastUpdated(new Date());
 //                logger.debug("Storing session");
 //                this.storeSession(session);
 //                logger.debug("BlackboardSession stored");
 //            }
 //
 //            logger.debug("Update recordings associated with session");
 //            List<RecordingShort> recordings = recordingService.getRecordingsForSession(session.getSessionId());
 //            if (recordings != null) {
 //                boolean changed;
 //                for (int i = 0; i < recordings.size(); i++) {
 //                    changed=false;
 //                    if ((recordings.get(i).getChairList()==null&&session.getChairList()!=null)
 //                            ||(session.getChairList()==null&&recordings.get(i).getChairList()!=null)
 //                            ||(!recordings.get(i).getChairList().equals(session.getChairList()))) 
 //                    {
 //                        changed=true;
 //                        recordings.get(i).setChairList(session.getChairList());
 //                    }
 //                    if ((recordings.get(i).getNonChairList()==null&&session.getNonChairList()!=null)
 //                            ||(session.getNonChairList()==null&&recordings.get(i).getNonChairList()!=null)
 //                            ||(!recordings.get(i).getNonChairList().equals(session.getNonChairList()))) 
 //                    {
 //                        changed=true;
 //                        recordings.get(i).setNonChairList(session.getNonChairList());
 //                    }
 //                    if (changed) 
 //                    {
 //                        logger.debug("Saving updated recording short");
 //                        recordingService.saveRecordingShort(recordings.get(i));
 //                    }
 //
 //                }
 //            }
 //
 //
 //            logger.debug("Finished updating recordings");
 //
 //            this.deleteExtParticipants(session.getSessionId());
 //            for (int i = 0; i < extParticipantList.size(); i++) {
 //                this.addExtParticipant(extParticipantList.get(i), session.getSessionId());
 //            }
 //
 //            String callBackUrl = prefs.getValue("callbackUrl", null);
 //            logger.debug("Setting callback Url to:" + callBackUrl);
 //            if (callBackUrl != null) {
 //				SetApiCallbackUrl setApiCallbackUrl = objectFactory.createSetApiCallbackUrl();
 //				setApiCallbackUrl.setApiCallbackUrl(callBackUrl);
 //				JAXBElement<SuccessResponse> response = (JAXBElement<SuccessResponse>) sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/SetApiCallbackUrl", setApiCallbackUrl);
 //				SuccessResponse successResponse = response.getValue();
 //                logger.debug("callBackUrl response:" + successResponse.isSuccess());
 //            }
 //
 //        } catch (Exception ex) {
 //            logger.error(ex.toString());
 //            throw ex;
 //        }
 //    }
 //
 //    public BlackboardUser getExtParticipant(long sessionId, String email) {
 //        SessionExtParticipantId sessionExtParticipantId = new SessionExtParticipantId();
 //        sessionExtParticipantId.setSessionId(sessionId);
 //        sessionExtParticipantId.setParticipantEmail(email);
 //
 //        SessionExtParticipant sessionExtParticipant = sessionExtParticipantDao.getSessionExtParticipant(sessionExtParticipantId);
 //
 //        BlackboardUser extParticipant = new User();
 //        extParticipant.setUid(email);
 //        extParticipant.setEmail(email);
 //
 //        if (sessionExtParticipant != null) {
 //            extParticipant.setDisplayName(sessionExtParticipant.getDisplay_name());
 //        }
 //
 //        return extParticipant;
 //    }
 //
 //    public void storeSession(BlackboardSession session) {
 //        blackboardSessionDao.saveSession(session);
 //    }
 //
 //    public void notifyModerators(BlackboardUser creator, BlackboardSession session, List<User> users,String launchUrl) throws Exception {
 //        logger.debug("notifyModerators called");
 //        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
 //        String creatorDetails = creator.getDisplayName() + " (" + creator.getEmail() + ")";
 //       
 //        List<String> toList;
 //        for (BlackboardUser user : users)
 //		{
 //            logger.debug("user name:" + user.getDisplayName());
 //            logger.debug("user email:" + user.getDisplayName());
 //
 //			Map<String, String> subs = new HashMap<String, String>();
 //			subs.put("displayName", user.getDisplayName());
 //			subs.put("creatorDetails", creatorDetails);
 //			subs.put("sessionName", session.getSessionName());
 //			subs.put("sessionStartTime", dateFormat.format(session.getStartTime()));
 //			subs.put("sessionEndTime", dateFormat.format(session.getEndTime()));
 //			subs.put("launchURL", launchUrl);
 //
 //			toList = new ArrayList<String>();
 //            toList.add(user.getEmail());
 //			mailTemplateService.sendEmailUsingTemplate(creator.getEmail(), toList, null, subs, MailMessages.MODERATOR);
 //        }
 //        logger.debug("finished");
 //    }
 //    
 //    public void notifyOfDeletion(BlackboardSession session) throws Exception {
 //        logger.debug("notifyOfDeletion called");
 //        String[] substitutions;
 //        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
 //        
 //        BlackboardUser creator = userService.getUserDetails(session.getCreatorId());
 //        String creatorDetails= "Unknown user";
 //        
 //        if (creator!=null)
 //        {
 //            creatorDetails = creator.getDisplayName() + " (" + creator.getEmail() + ")";
 //            logger.debug("creatorDetails set:"+creatorDetails);
 //        }
 //             
 //        List<User> users = new ArrayList<User>();
 //        
 //        BlackboardUser lookupUser;     
 //        
 //        logger.debug("Finished initialisation of creator and variables");
 //        if (session.getChairList()!=null&&!session.getChairList().equals(""))
 //        {
 //                logger.debug("Adding chair list users");
 //                String[] chairList = StringUtil.split(session.getChairList(),',');
 //                
 //                for (int i=0;i<chairList.length;i++)
 //                {
 //                    lookupUser=userService.getUserDetails(chairList[i]);
 //                    if (lookupUser!=null)
 //                    {
 //                        users.add(lookupUser);
 //                    }
 //                   
 //                }
 //              
 //        }
 //            
 //        if (session.getNonChairList()!=null&&!session.getNonChairList().equals(""))
 //        {
 //                logger.debug("Adding nonchair list users");
 //                String[] nonChairList = StringUtil.split(session.getNonChairList(),',');
 //                             
 //                for (int i=0;i<nonChairList.length;i++)
 //                {
 //                    lookupUser=userService.getUserDetails(nonChairList[i]);
 //                    if (lookupUser!=null)
 //                    {
 //                        users.add(lookupUser);
 //                    }
 //                    else
 //                    {
 //                        lookupBlackboardUser = this.getExtParticipant(session.getSessionId(),nonChairList[i]);
 //                        if (lookupUser==null)
 //                        {
 //                            lookupBlackboardUser = new User();
 //                            lookupUser.setEmail(nonChairList[i]);
 //                        }
 //                        
 //                        users.add(lookupUser);
 //                    }
 //                }
 //                          
 //        }
 //        
 //		List<String> toList;
 //		for (BlackboardUser user : users)
 //		{
 //			Map<String, String> subs = new HashMap<String, String>();
 //			subs.put("displayName", user.getDisplayName());
 //			subs.put("creatorDetails", creatorDetails);
 //			subs.put("sessionName", session.getSessionName());
 //			subs.put("sessionStartTime", dateFormat.format(session.getStartTime()));
 //			subs.put("sessionEndTime", dateFormat.format(session.getEndTime()));
 //
 //			toList = new ArrayList<String>();
 //			toList.add(user.getEmail());
 //			mailTemplateService.sendEmailUsingTemplate(creator.getEmail(), toList, null, subs, MailMessages.SESSION_DELETION);
 //		}
 //		logger.debug("finished");
 //    }
 //
 //    public void notifyIntParticipants(BlackboardUser creator, BlackboardSession session, List<User> users, String launchUrl) throws Exception {
 //        logger.debug("notifyIntParticipants called");
 //        String[] substitutions;
 //        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
 //        String creatorDetails = creator.getDisplayName() + " (" + creator.getEmail() + ")";
 //
 //        List<String> toList;
 //        for (BlackboardUser user : users)
 //		{
 //			Map<String, String> subs = new HashMap<String, String>();
 //			subs.put("displayName", user.getDisplayName());
 //			subs.put("creatorDetails", creatorDetails);
 //			subs.put("sessionName", session.getSessionName());
 //			subs.put("sessionStartTime", dateFormat.format(session.getStartTime()));
 //			subs.put("sessionEndTime", dateFormat.format(session.getEndTime()));
 //			subs.put("launchURL", launchUrl);
 //
 //			toList = new ArrayList<String>();
 //			toList.add(user.getEmail());
 //			mailTemplateService.sendEmailUsingTemplate(creator.getEmail(), toList, null, subs, MailMessages.INTERNAL_PARTICIPANT);
 //        }
 //        logger.debug("finished");
 //    }
 //
 //    public void notifyExtParticipants(BlackboardUser creator, BlackboardSession session, List<User> users) throws Exception {
 //        logger.debug("notifyExtParticipants called");
 //        String[] substitutions;
 //        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
 //        String creatorDetails = creator.getDisplayName() + " (" + creator.getEmail() + ")";
 //        SessionUrl sessionUrl;
 //        SessionUrlId sessionUrlId;
 //        List<String> toList;
 //        // Get the guest launch URL
 //        sessionUrlId = new SessionUrlId();
 //        sessionUrlId.setSessionId(session.getSessionId());
 //        sessionUrlId.setDisplayName("Guest");
 //        sessionUrl = getSessionUrl(sessionUrlId);
 //        String extParticipantUrl;
 //		for (BlackboardUser user : users)
 //		{
 //			extParticipantUrl = sessionUrl.getUrl().replaceFirst("Guest", URLEncoder.encode(user.getDisplayName(), "UTF-8"));
 //
 //			Map<String, String> subs = new HashMap<String, String>();
 //			subs.put("displayName", user.getDisplayName());
 //			subs.put("creatorDetails", creatorDetails);
 //			subs.put("sessionName", session.getSessionName());
 //			subs.put("sessionStartTime", dateFormat.format(session.getStartTime()));
 //			subs.put("sessionEndTime", dateFormat.format(session.getEndTime()));
 //			subs.put("extParticipantUrl", extParticipantUrl);
 //
 //			toList = new ArrayList<String>();
 //			toList.add(user.getEmail());
 //			mailTemplateService.sendEmailUsingTemplate(creator.getEmail(), toList, null, subs, MailMessages.EXTERNAL_PARTICIPANT);
 //		}
 //
 //		logger.debug("finished");
 //    }
 //
 //    public void addExtParticipant(BlackboardUser user, long sessionId) {
 //        logger.debug("addExtParticipant called for session,user: (" + sessionId + "," + user.getEmail() + ")");
 //        SessionExtParticipantId sessionExtParticipantId = new SessionExtParticipantId();
 //        SessionExtParticipant sessionExtParticipant = new SessionExtParticipantImpl();
 //
 //        sessionExtParticipantId.setParticipantEmail(user.getEmail());
 //        sessionExtParticipantId.setSessionId(sessionId);
 //
 //        sessionExtParticipant.setSessionExtParticipantId(sessionExtParticipantId);
 //        sessionExtParticipant.setDisplay_name(user.getDisplayName());
 //        sessionExtParticipantDao.storeSessionExtParticipant(sessionExtParticipant);
 //    }
 //
 //    public void deleteExtParticipants(long sessionId) {
 //        logger.debug("deleteExtParticipants called for :" + sessionId);
 //        sessionExtParticipantDao.deleteAllExtParticipants(sessionId);
 //    }
 //
 //    public SessionPresentation getSessionPresentation(String sessionId) {
 //        logger.debug("getSessionPresentation called");
 //        List<SessionPresentation> sessionPresentationList = sessionPresentationDao.getSessionPresentation(sessionId);
 //
 //        if (sessionPresentationList != null && sessionPresentationList.size() > 0) {
 //            return sessionPresentationList.get(0);
 //        } else {
 //            logger.debug("getSessionPresentation is going to return null");
 //            return null;
 //        }
 //    }
 //
 //    public void deleteSessionPresentation(long sessionId, long presentationId) throws Exception {
 //        logger.debug("deleteSessionPresentation called");
 //
 //        try {
 //            logger.debug("Setup session web service call");
 //			RemoveSessionPresentation removeSessionPresentation = objectFactory.createRemoveSessionPresentation();
 //			removeSessionPresentation.setSessionId(sessionId);
 //			removeSessionPresentation.setPresentationId(presentationId);
 //			JAXBElement<SuccessResponse> response = (JAXBElement<SuccessResponse>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/RemoveSessionPresentation", removeSessionPresentation);  
 //			SuccessResponse successResponse = response.getValue();
 //            logger.debug("removeSessionPresentation returned:" + successResponse.isSuccess());
 //			RemoveRepositoryPresentation removeRepositoryPresentation = objectFactory.createRemoveRepositoryPresentation();
 //			removeRepositoryPresentation.setPresentationId(presentationId);
 //			response = (JAXBElement<SuccessResponse>) sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/RemoveRepositoryPresentation", removeRepositoryPresentation);
 //			successResponse = response.getValue();
 //            logger.debug("removeRepositoryPresentation returned:" + successResponse.isSuccess());
 //            sessionPresentationDao.deleteSessionPresentation(Long.toString(presentationId));
 //        } catch (Exception e) {
 //            logger.error("Exception caught deleting session presentation", e);
 //            throw e;
 //        }
 //    }
 //
 //    public void addSessionPresentation(String uid, long sessionId, MultipartFile file) throws Exception {
 //        logger.debug("addSessionPresentation called");
 //
 //        try { // Call Web Service Operation
 //            logger.debug("Setup session web service call");
 //            ByteArrayDataSource rawData = new ByteArrayDataSource(file.getBytes(), file.getContentType());
 //            logger.debug("ByteArrayDataSource created");
 //            DataHandler dataHandler = new DataHandler(rawData);
 //            logger.debug("DataHandler created from ByteArrayDataSource");
 //			UploadRepositoryContent uploadRepositoryContent = objectFactory.createUploadRepositoryContent();
 //			uploadRepositoryContent.setCreatorId(uid);
 //			uploadRepositoryContent.setFilename(file.getOriginalFilename());
 //			uploadRepositoryContent.setContent(dataHandler);
 //			JAXBElement<PresentationResponseCollection> response = (JAXBElement<PresentationResponseCollection>) sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/UploadRepositoryPresentation", uploadRepositoryContent);
 //			PresentationResponseCollection presentationResponseCollection = response.getValue();
 //            logger.debug("uploadRepositoryPresentation called");
 //
 //            if (presentationResponseCollection != null)
 //			{
 //                SessionPresentation sessionPresentation = new SessionPresentationImpl();
 //                sessionPresentation.setCreatorId(uid);
 //                sessionPresentation.setDateUploaded(new Date());
 //                sessionPresentation.setFileName(file.getOriginalFilename());
 //                sessionPresentation.setSessionId(Long.toString(sessionId));
 //				for (PresentationResponse presentationResponse : presentationResponseCollection.getPresentationResponses())
 //				{
 //                 	SetSessionPresentation setSessionPresentation = objectFactory.createSetSessionPresentation();
 //					setSessionPresentation.setSessionId(sessionId);
 //					setSessionPresentation.setPresentationId(presentationResponse.getPresentationId());
 //					JAXBElement<SuccessResponse> response2 = (JAXBElement<SuccessResponse>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/SetSessionPresentation", setSessionPresentation);
 //					SuccessResponse successResponse = response2.getValue();
 //                    if (successResponse.isSuccess()) {
 //                        sessionPresentation.setPresentationId(presentationResponse.getPresentationId());
 //                        sessionPresentationDao.storeSessionPresentation(sessionPresentation);
 //                    }
 //                }
 //            } else {
 //                logger.error("uploadRepositoryPresentation was null");
 //            }
 //        } catch (Exception e) {
 //            throw e;
 //        }
 //    }
 //
 //    public void deleteSessionMultimedia(long sessionId) throws Exception {
 //        logger.debug("deleteSessionMultimediaFiles called");
 //        List<SessionMultimedia> sessionMultimediaList = this.getSessionMultimedia(sessionId);
 //
 //        try { // Call Web Service Operation
 //            logger.debug("Setup session web service call");
 //            for (SessionMultimedia sessionMultimedia : sessionMultimediaList)
 //			{
 //				RemoveSessionMultimedia removeSessionMultimedia = objectFactory.createRemoveSessionMultimedia();
 //				removeSessionMultimedia.setSessionId(sessionId);
 //				removeSessionMultimedia.setMultimediaId(sessionMultimedia.getMultimediaId());
 //				JAXBElement<SuccessResponse> response = (JAXBElement<SuccessResponse>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/RemoveSessionMultimedia", removeSessionMultimedia);
 //				SuccessResponse successResponse = response.getValue();
 //                logger.debug("deleteSessionMultimedia returned:" + successResponse.isSuccess());
 //				RemoveRepositoryMultimedia removeRepositoryMultimedia = objectFactory.createRemoveRepositoryMultimedia();
 //				removeRepositoryMultimedia.setMultimediaId(sessionMultimedia.getMultimediaId());
 //				JAXBElement<SuccessResponse> response2 = (JAXBElement<SuccessResponse>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/RemoveRepositoryMultimedia", removeRepositoryMultimedia);
 //				successResponse = response2.getValue();
 //                logger.debug("delete multimediaId (" + sessionMultimedia.getMultimediaId() + " returned:" + successResponse.isSuccess());
 //                sessionMultimediaDao.deleteSessionMultimedia(sessionMultimedia.getMultimediaId());
 //            }
 //        } catch (Exception e) {
 //            logger.error("Exception caught removing multimedia files", e);
 //            throw e;
 //        }
 //    }
 //
 //    public void deleteSessionMultimediaFiles(long sessionId, String[] multimediaIds) throws Exception {
 //        logger.debug("deleteSessionMultimediaFiles called");
 //
 //        /* Call set session to remove the old ids, then remove them from
 //         the repository */
 //        try { // Call Web Service Operation
 //            logger.debug("Setup session web service call");
 //
 //            for (String multiMediaId : multimediaIds) {
 //                RemoveSessionMultimedia removeSessionMultimedia = objectFactory.createRemoveSessionMultimedia();
 //				removeSessionMultimedia.setSessionId(sessionId);
 //				removeSessionMultimedia.setMultimediaId(Long.valueOf(multiMediaId));
 //				JAXBElement<SuccessResponse> response = (JAXBElement<SuccessResponse>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/RemoveSessionMultimedia", removeSessionMultimedia); 
 //				SuccessResponse successResponse = response.getValue();
 //                if (successResponse.isSuccess()) {
 //					RemoveRepositoryMultimedia removeRepositoryMultimedia = objectFactory.createRemoveRepositoryMultimedia();
 //					removeRepositoryMultimedia.setMultimediaId(Long.valueOf(multiMediaId));
 //					JAXBElement<SuccessResponse> response2 = (JAXBElement<SuccessResponse>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/RemoveRepositoryMultimedia", removeRepositoryMultimedia);
 //					successResponse = response2.getValue();
 //                    logger.debug("delete multimediaId (" + multiMediaId + " returned:" + successResponse.isSuccess());
 //                    sessionMultimediaDao.deleteSessionMultimedia(Long.valueOf(multiMediaId));
 //                } else {
 //                    throw new Exception("Error deleting session multimedia.");
 //                }
 //            }
 //        } catch (Exception e) {
 //            logger.error("Exception caught deleting session multimedia", e);
 //            throw e;
 //        }
 //
 //    }
 //
 //    public List<SessionMultimedia> getSessionMultimedia(long sessionId) {
 //        return sessionMultimediaDao.getSessionMultimedia(Long.toString(sessionId));
 //    }
 //
 //    public void addSessionMultimedia(String uid, long sessionId, MultipartFile file) throws Exception {
 //        logger.debug("addSessionMultimedia called");
 //
 //        try { // Call Web Service Operation
 //            logger.debug("Setup session web service call");
 //            ByteArrayDataSource rawData = new ByteArrayDataSource(file.getBytes(), file.getContentType());
 //            logger.debug("ByteArrayDataSource created");
 //            DataHandler dataHandler = new DataHandler(rawData);
 //            logger.debug("DataHandler created from ByteArrayDataSource");
 //			UploadRepositoryContent uploadRepositoryContent = objectFactory.createUploadRepositoryContent();
 //			uploadRepositoryContent.setCreatorId(uid);
 //			uploadRepositoryContent.setFilename(file.getOriginalFilename());
 //			uploadRepositoryContent.setContent(dataHandler);
 //			JAXBElement<MultimediaResponseCollection> jaxBmrc = (JAXBElement<MultimediaResponseCollection>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/UploadRepositoryMultimedia", uploadRepositoryContent);  
 //            MultimediaResponseCollection multimediaResponseCollection = jaxBmrc.getValue();
 //            logger.debug("uploadRepositoryMultimedia called");
 //
 //            if (multimediaResponseCollection != null) {
 //                SessionMultimedia sessionMultimedia = new SessionMultimediaImpl();
 //                sessionMultimedia.setCreatorId(uid);
 //                sessionMultimedia.setDateUploaded(new Date());
 //                sessionMultimedia.setFileName(file.getOriginalFilename());
 //                sessionMultimedia.setSessionId(Long.toString(sessionId));
 //                List<SessionMultimedia> sessionMultimediaList = sessionMultimediaDao.getSessionMultimedia(Long.toString(sessionId));
 //                String multimediaIds = "";
 //                for (SessionMultimedia sm : sessionMultimediaList) {
 //                    multimediaIds += sm.getMultimediaId();
 //                    multimediaIds += ",";
 //                }
 //
 //                for (MultimediaResponse multimediaResponse : multimediaResponseCollection.getMultimediaResponses()) {
 //                    sessionMultimedia.setMultimediaId(multimediaResponse.getMultimediaId());
 //                    multimediaIds += sessionMultimedia.getMultimediaId();
 //					SetSessionMultimedia setSessionMultimedia = objectFactory.createSetSessionMultimedia();
 //					setSessionMultimedia.setSessionId(sessionId);
 //					setSessionMultimedia.setMultimediaIds(multimediaIds);
 //					JAXBElement<SuccessResponse> jaxBsuccessResponse = (JAXBElement<SuccessResponse>)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/SetSessionMultimedia", setSessionMultimedia);  
 //					SuccessResponse successResponse = jaxBsuccessResponse.getValue();
 //                    if (successResponse.isSuccess()) {
 //                        sessionMultimediaDao.saveSessionMultimedia(sessionMultimedia);
 //                    }
 //                }
 //            } else {
 //                logger.error("uploadRepositoryMultimedia was null");
 //            }
 //        } catch (Exception e) {
 //            throw e;
 //        }
 //    }
 }
