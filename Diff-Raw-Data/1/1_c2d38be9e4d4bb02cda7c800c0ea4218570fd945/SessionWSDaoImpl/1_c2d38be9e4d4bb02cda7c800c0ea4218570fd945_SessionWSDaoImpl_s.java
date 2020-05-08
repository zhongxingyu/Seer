 package org.jasig.portlet.blackboardvcportlet.dao.ws.impl;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.bind.JAXBElement;
 
 import org.jasig.portlet.blackboardvcportlet.dao.ws.SessionWSDao;
 import org.jasig.portlet.blackboardvcportlet.dao.ws.WSDaoUtils;
 import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
 import org.jasig.portlet.blackboardvcportlet.data.RecordingMode;
 import org.jasig.portlet.blackboardvcportlet.security.SecurityExpressionEvaluator;
 import org.jasig.portlet.blackboardvcportlet.service.SessionForm;
 import org.jasig.portlet.blackboardvcportlet.service.util.SASWebServiceOperations;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.support.DataAccessUtils;
 import org.springframework.stereotype.Service;
 
 import com.elluminate.sas.BlackboardBuildSessionUrl;
 import com.elluminate.sas.BlackboardClearSessionUserList;
 import com.elluminate.sas.BlackboardListSession;
 import com.elluminate.sas.BlackboardListSessionAttendance;
 import com.elluminate.sas.BlackboardListSessionAttendanceResponseCollection;
 import com.elluminate.sas.BlackboardRemoveSession;
 import com.elluminate.sas.BlackboardSessionAttendanceResponse;
 import com.elluminate.sas.BlackboardSessionResponse;
 import com.elluminate.sas.BlackboardSessionResponseCollection;
 import com.elluminate.sas.BlackboardSessionTelephony;
 import com.elluminate.sas.BlackboardSessionTelephonyResponse;
 import com.elluminate.sas.BlackboardSessionTelephonyResponseCollection;
 import com.elluminate.sas.BlackboardSetSession;
 import com.elluminate.sas.BlackboardSetSessionTelephony;
 import com.elluminate.sas.BlackboardUpdateSession;
 import com.elluminate.sas.BlackboardUrlResponse;
 import com.elluminate.sas.ObjectFactory;
 
 @Service
 public class SessionWSDaoImpl implements SessionWSDao {
 	private SASWebServiceOperations sasWebServiceOperations;
 	private SecurityExpressionEvaluator securityExpressionEvaluator;
 	
 	@Autowired
 	public void setSasWebServiceOperations(SASWebServiceOperations sasWebServiceOperations)
 	{
 		this.sasWebServiceOperations = sasWebServiceOperations;
 	}
 	
 	@Autowired
     public void setSecurityExpressionEvaluator(SecurityExpressionEvaluator securityExpressionEvaluator) {
         this.securityExpressionEvaluator = securityExpressionEvaluator;
     }
 
     @Override
 	public BlackboardSessionResponse createSession(ConferenceUser user, SessionForm sessionForm) {
 		final BlackboardSetSession setSession = new BlackboardSetSession();
         setSession.setCreatorId(user.getUniqueId());
         setSession.setSessionName(sessionForm.getSessionName());
         setSession.setStartTime(sessionForm.getStartTime().getMillis());
         setSession.setEndTime(sessionForm.getEndTime().getMillis());
         setSession.setBoundaryTime(sessionForm.getBoundaryTime());
         setSession.setChairList(user.getUniqueId());
        setSession.setNonChairList("externalperson@example.com");
         
         if (securityExpressionEvaluator.authorize("hasRole('ROLE_FULL_ACCESS')")) {
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
         @SuppressWarnings("unchecked")
         JAXBElement<BlackboardSessionResponseCollection> jaxbSessionResponse = (JAXBElement<BlackboardSessionResponseCollection>) objSessionResponse;
         
         final BlackboardSessionResponseCollection sessionResponses = jaxbSessionResponse.getValue();
         final BlackboardSessionResponse sessionResponse = DataAccessUtils.singleResult(sessionResponses.getSessionResponses());
         return sessionResponse;
 		
 	}
 	
 	@Override
 	public String buildSessionUrl(long sessionId, ConferenceUser user) {
 		BlackboardBuildSessionUrl buildSessionUrlRequest = new BlackboardBuildSessionUrl();
 		buildSessionUrlRequest.setSessionId(sessionId);
 		buildSessionUrlRequest.setDisplayName(user.getDisplayName());
 		buildSessionUrlRequest.setUserId(user.getBlackboardUniqueId());
         final Object urlResponseObject = sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/BuildSessionUrl", buildSessionUrlRequest);
         @SuppressWarnings("unchecked")
         JAXBElement<BlackboardUrlResponse> jaxbResponse = (JAXBElement<BlackboardUrlResponse>) urlResponseObject;
         return  jaxbResponse.getValue().getUrl();
 	}
 	
 	@Override
     public String buildGuestSessionUrl(long sessionId) {
         BlackboardBuildSessionUrl buildSessionUrlRequest = new BlackboardBuildSessionUrl();
         buildSessionUrlRequest.setSessionId(sessionId);
         buildSessionUrlRequest.setDisplayName("GUEST_PLACEHOLDER");
         final Object urlResponseObject = sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/BuildSessionUrl", buildSessionUrlRequest);
         @SuppressWarnings("unchecked")
         JAXBElement<BlackboardUrlResponse> jaxbResponse = (JAXBElement<BlackboardUrlResponse>) urlResponseObject;
         return  jaxbResponse.getValue().getUrl().replace("&username=GUEST_PLACEHOLDER", "");
     }
 
     @Override
 	public boolean createSessionTelephony(long sessionId, BlackboardSetSessionTelephony telephony) {
 		telephony.setSessionId(sessionId);
 		return WSDaoUtils.isSuccessful(sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/SetSessionTelephony", telephony));
 	}
 
 	@Override
 	public List<BlackboardSessionResponse> getSessions(String userId, String groupingId, Long sessionId,
 			String creatorId, Long startTime, Long endTime, String sessionName) {
 		//build search request
 		BlackboardListSession request = new ObjectFactory().createBlackboardListSession();
 		
 		if(userId == null && groupingId == null && sessionId == null && creatorId == null 
 				&& startTime == null && endTime == null && sessionName == null) {
 			throw new IllegalStateException("You must specify at least 1 piece of criteria");
 		}
 		
 		if(userId != null) {
 			request.setUserId(userId);
 		}
 		if(groupingId != null) {
 			request.setGroupingId(groupingId);
 		}
 		
 		if(sessionId != null) {
 			request.setSessionId(sessionId);
 		}
 		
 		if(creatorId != null) {
 			request.setCreatorId(creatorId);
 		}
 		
 		if(startTime != null) {
 			request.setStartTime(startTime);
 		}
 		
 		if(endTime != null) {
 			request.setEndTime(endTime);
 		}
 		
 		if(sessionName != null) {
 			request.setSessionName(sessionName);
 		}
 		
 		Object obj = sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/ListSession", request); 
 		@SuppressWarnings("unchecked")
 		JAXBElement<BlackboardSessionResponseCollection> responseCollection = (JAXBElement<BlackboardSessionResponseCollection>) obj;
 		return responseCollection.getValue().getSessionResponses();
 	}
 
 	@Override
 	public List<BlackboardSessionAttendanceResponse> getSessionAttendance(long sessionId, Object startTime) {
 		BlackboardListSessionAttendance request = new ObjectFactory().createBlackboardListSessionAttendance();
 		BlackboardListSessionAttendanceResponseCollection responseCollection = (BlackboardListSessionAttendanceResponseCollection) sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/ListSessionAttendance", request);
 		return responseCollection.getSessionAttendanceResponses();
 	}
 
 	@Override
 	public List<BlackboardSessionTelephonyResponse> getSessionTelephony(long sessionId) {
 		BlackboardSessionTelephony request = new ObjectFactory().createBlackboardSessionTelephony();
 		request.setSessionId(sessionId);
 		final BlackboardSessionTelephonyResponseCollection response = (BlackboardSessionTelephonyResponseCollection) sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/ListSessionTelephony", request);
 		return response.getSessionTelephonyResponses();
 	}
 
 	@Override
 	public BlackboardSessionResponse updateSession(long bbSessionId, SessionForm sessionForm) {
 		final BlackboardUpdateSession updateSession = new ObjectFactory().createBlackboardUpdateSession();
 
 		updateSession.setSessionId(bbSessionId);
 		updateSession.setSessionName(sessionForm.getSessionName());
 		updateSession.setStartTime(sessionForm.getStartTime().getMillis());
 		updateSession.setEndTime(sessionForm.getEndTime().getMillis());
 		updateSession.setBoundaryTime(sessionForm.getBoundaryTime());
 
 		if (securityExpressionEvaluator.authorize("hasRole('ROLE_FULL_ACCESS')")) {
         	updateSession.setMaxTalkers(sessionForm.getMaxTalkers());
         	updateSession.setMaxCameras(sessionForm.getMaxCameras());
         	updateSession.setMustBeSupervised(sessionForm.isMustBeSupervised());
         	updateSession.setPermissionsOn(sessionForm.isPermissionsOn());
         	updateSession.setRaiseHandOnEnter(sessionForm.isRaiseHandOnEnter());
             final RecordingMode recordingMode = sessionForm.getRecordingMode();
             if (recordingMode != null) {
             	updateSession.setRecordingModeType(recordingMode.getBlackboardRecordingMode());
             }
             updateSession.setHideParticipantNames(sessionForm.isHideParticipantNames());
             updateSession.setAllowInSessionInvites(sessionForm.isAllowInSessionInvites());
         }
         
         final Object objSessionResponse = sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/UpdateSession", updateSession);
         @SuppressWarnings("unchecked")
 		JAXBElement<BlackboardSessionResponseCollection> response = (JAXBElement<BlackboardSessionResponseCollection>) objSessionResponse;
         return DataAccessUtils.singleResult(response.getValue().getSessionResponses());
 	}
 	
 	@Override
     public BlackboardSessionResponse setSessionChairs(long bbSessionId, Set<ConferenceUser> sessionChairs) {
 	    final BlackboardUpdateSession updateSession = new ObjectFactory().createBlackboardUpdateSession();
 
 	    updateSession.setSessionId(bbSessionId);
 	    
         final String chairList = buildUidList(sessionChairs);
         updateSession.setChairList(chairList);
         
         final Object objSessionResponse = sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/UpdateSession", updateSession);
         @SuppressWarnings("unchecked")
         JAXBElement<BlackboardSessionResponseCollection> response = (JAXBElement<BlackboardSessionResponseCollection>) objSessionResponse;
         return DataAccessUtils.singleResult(response.getValue().getSessionResponses());
     }
     
     @Override
     public BlackboardSessionResponse setSessionNonChairs(long bbSessionId, Set<ConferenceUser> sessionNonChairs) {
         final BlackboardUpdateSession updateSession = new ObjectFactory().createBlackboardUpdateSession();
 
         updateSession.setSessionId(bbSessionId);
         
         final String chairList = buildUidList(sessionNonChairs);
         updateSession.setNonChairList(chairList);
         
         final Object objSessionResponse = sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/UpdateSession", updateSession);
         @SuppressWarnings("unchecked")
         JAXBElement<BlackboardSessionResponseCollection> response = (JAXBElement<BlackboardSessionResponseCollection>) objSessionResponse;
         return DataAccessUtils.singleResult(response.getValue().getSessionResponses());
     }
 
     @Override
 	public boolean deleteSession(long sessionId) {
 		BlackboardRemoveSession request = new ObjectFactory().createBlackboardRemoveSession();
 		request.setSessionId(sessionId);
 		return WSDaoUtils.isSuccessful(sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/RemoveSession", request));
 	}
 
 	@Override
 	public boolean clearSessionChairList(long sessionId) {
 		return clearSessionUserList(sessionId,true);
 	}
 
 	@Override
 	public boolean clearSessionNonChairList(long sessionId) {
 		return clearSessionUserList(sessionId,false);
 	}
 
     private String buildUidList(Set<ConferenceUser> users) {
         final StringBuilder uidBuilder = new StringBuilder();
         for (final Iterator<ConferenceUser> userItr = users.iterator(); userItr.hasNext();) {
             final ConferenceUser user = userItr.next();
             uidBuilder.append(user.getBlackboardUniqueId());
             if (userItr.hasNext()) {
                 uidBuilder.append(',');
             }
         }
         return uidBuilder.toString();
     }
 	
 	private boolean clearSessionUserList(Long sessionId, boolean isChairList) {
 		BlackboardClearSessionUserList vo = new ObjectFactory().createBlackboardClearSessionUserList();
 		vo.setSessionId(sessionId);
 		Object request;
 		if(isChairList) {
 			request = new ObjectFactory().createClearSessionChairList(vo);
 		} else {
 			request = new ObjectFactory().createClearSessionNonChairList(vo);
 		}
 		return WSDaoUtils.isSuccessful(sasWebServiceOperations.marshalSendAndReceiveToSAS("http://sas.elluminate.com/ClearSession"+ (isChairList ? "" : "Non") +"ChairList" , request));
 	}
 }
