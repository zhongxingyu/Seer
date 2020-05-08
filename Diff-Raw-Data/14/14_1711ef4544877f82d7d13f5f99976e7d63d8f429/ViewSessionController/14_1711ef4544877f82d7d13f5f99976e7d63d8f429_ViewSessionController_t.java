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
 package org.jasig.portlet.blackboardvcportlet.mvc.sessionmngr;
 
 import java.util.Set;
 
 import javax.portlet.PortletRequest;
import javax.portlet.WindowState;
 
 import org.jasig.portlet.blackboardvcportlet.dao.SessionDao;
 import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
 import org.jasig.portlet.blackboardvcportlet.data.Session;
 import org.jasig.portlet.blackboardvcportlet.data.SessionRecording;
 import org.jasig.portlet.blackboardvcportlet.security.ConferenceUserService;
 import org.jasig.portlet.blackboardvcportlet.service.SessionService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 
 import com.google.common.collect.ImmutableSortedSet;
 
 /**
  * Controller for handling Portlet view mode
  *
  * @author Richard Good
  */
 @Controller
 @RequestMapping("VIEW")
 public class ViewSessionController
 {
 	protected final Logger logger = LoggerFactory.getLogger(getClass());
 
 	private ConferenceUserService conferenceUserService;
 	private SessionService sessionService;
 	private SessionDao sessionDao;
 	
 	
 	@Autowired
 	public void setSessionService(SessionService sessionService) {
         this.sessionService = sessionService;
     }
	
	@Autowired
	private ViewSessionListController viewController;
 
 	@Autowired
     public void setConferenceUserService(ConferenceUserService conferenceUserService) {
         this.conferenceUserService = conferenceUserService;
     }
 
 	@Autowired
 	public void setSessionDao(SessionDao sessionDao) {
         this.sessionDao = sessionDao;
     }
 
     @RenderMapping(params = "action=viewSession")
 	public String viewSession(PortletRequest request, @RequestParam long sessionId, ModelMap model)	{
    	
    	if(WindowState.NORMAL.equals(request.getWindowState())) {
	    	return viewController.view(request, model, null, null);
	    }
    	
         final Session session = this.sessionService.getSession(sessionId);
         //TODO what if session is null?
         model.addAttribute("session", session);
         
         final Set<ConferenceUser> sessionChairs = this.sessionService.getSessionChairs(session);
         model.addAttribute("sessionChairs", ImmutableSortedSet.copyOf(ConferenceUserDisplayComparator.INSTANCE, sessionChairs));
         
         final Set<ConferenceUser> sessionNonChairs = this.sessionService.getSessionNonChairs(session);
         model.addAttribute("sessionNonChairs", ImmutableSortedSet.copyOf(ConferenceUserDisplayComparator.INSTANCE, sessionNonChairs));
         
         final Set<SessionRecording> sessionRecordings = this.sessionDao.getSessionRecordings(session);
         model.addAttribute("recordings", ImmutableSortedSet.copyOf(SessionRecordingDisplayComparator.INSTANCE, sessionRecordings));
         
         //Session hasn't completed yet, show session launch URL
         //TODO should we check if we are within the "boundary time"?
         if (session.getEndTime().isAfterNow()) {
             final ConferenceUser conferenceUser = this.conferenceUserService.getCurrentConferenceUser();
             sessionService.populateLaunchUrl(conferenceUser, session);
             model.addAttribute("launchUrl",session.getLaunchUrl());
         }
 
        return "viewSession";
 	}
 }
