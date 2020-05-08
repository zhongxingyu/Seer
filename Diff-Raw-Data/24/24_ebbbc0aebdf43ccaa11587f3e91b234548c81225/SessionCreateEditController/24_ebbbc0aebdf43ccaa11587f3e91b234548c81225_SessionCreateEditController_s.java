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

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;

 import org.apache.commons.lang.StringUtils;
import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
import org.jasig.portlet.blackboardvcportlet.data.Multimedia;
import org.jasig.portlet.blackboardvcportlet.data.RecordingMode;
import org.jasig.portlet.blackboardvcportlet.data.ServerConfiguration;
import org.jasig.portlet.blackboardvcportlet.data.Session;
import com.google.common.collect.ImmutableSortedSet;
 import org.jasig.portlet.blackboardvcportlet.data.*;
 import org.jasig.portlet.blackboardvcportlet.security.ConferenceUserService;
 import org.jasig.portlet.blackboardvcportlet.service.ServerConfigurationService;
 import org.jasig.portlet.blackboardvcportlet.service.SessionForm;
 import org.jasig.portlet.blackboardvcportlet.service.SessionService;
 import org.joda.time.DateMidnight;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.portlet.bind.annotation.ActionMapping;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletMode;
 import javax.portlet.PortletModeException;
 import javax.validation.Valid;
 import java.util.Set;
 
import com.google.common.collect.Ordering;

 /**
  * Controller class for Portlet EDIT related actions and render
  *
  * @author Richard Good
  */
 @Controller
 @RequestMapping("EDIT")
 public class SessionCreateEditController
 {
 	protected final Logger logger = LoggerFactory.getLogger(getClass());
 
 	private ConferenceUserService conferenceUserService;
 	private ServerConfigurationService serverConfigurationService;
 	private SessionService sessionService;
 
 	@Autowired
 	public void setConferenceUserService(ConferenceUserService conferenceUserService) {
         this.conferenceUserService = conferenceUserService;
     }
 
 	@Autowired
 	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
 	{
 		this.serverConfigurationService = serverConfigurationService;
 	}
 
 	@Autowired
 	public void setSessionService(SessionService sessionService)
 	{
 		this.sessionService = sessionService;
 	}
 
 	@InitBinder
     public void initBinder(WebDataBinder binder) {
 	    final DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("MM/dd/yyyy").toFormatter();
         binder.registerCustomEditor(DateMidnight.class, new CustomDateMidnightEditor(formatter, false));
     }
 	
 	@ModelAttribute("recordingModes")
 	public RecordingMode[] getRecordingModes() {
 	    return RecordingMode.values();
 	}
 
 	@RenderMapping
 	public String displayNewSessionForm(ModelMap model) throws PortletModeException {
 	    final ServerConfiguration serverConfiguration = this.serverConfigurationService.getServerConfiguration();
         model.put("serverConfiguration", serverConfiguration);
 	    
         final SessionForm sessionForm = new SessionForm(serverConfiguration);
        model.put("session", sessionForm);
 	    
 	    return "BlackboardVCPortlet_edit";
 	}
 
     @RenderMapping(params="sessionId")
     public String displayEditSessionForm(ModelMap model, @RequestParam long sessionId) throws PortletModeException
 	{
         final ServerConfiguration serverConfiguration = this.serverConfigurationService.getServerConfiguration();
         model.put("serverConfiguration", serverConfiguration);
 
 		final Session session = this.sessionService.getSession(sessionId);
 		//TODO if session is null
 
		// Update model to match pattern that jsp is looking for
		// ie, sessionForm -> session AND BindingResult.sessionForm -> BindingResult.session
 		if (!model.containsKey("sessionForm"))
 		{
 			SessionForm sessionForm = new SessionForm(session);
 			model.addAttribute("sessionForm", sessionForm);
 		}
 
         final Set<ConferenceUser> sessionChairs = this.sessionService.getSessionChairs(session);
         model.addAttribute("sessionChairs", Ordering.from(ConferenceUserDisplayNameComparator.INSTANCE).sortedCopy(sessionChairs));
         
         final Set<ConferenceUser> sessionNonChairs = this.sessionService.getSessionNonChairs(session);
         model.addAttribute("sessionNonChairs", Ordering.from(ConferenceUserDisplayNameComparator.INSTANCE).sortedCopy(sessionNonChairs));
         
         final Set<Multimedia> sessionMultimedia = this.sessionService.getSessionMultimedia(session);
         model.addAttribute("sessionMultimedia", Ordering.from(MultimediaNameComparator.INSTANCE).sortedCopy(sessionMultimedia));
         
         model.addAttribute("presentation", session.getPresentation()); 
         
         return "BlackboardVCPortlet_edit";
     }
 	
 	@ActionMapping(params = "action=saveSession")
 	public void saveSession(ActionResponse response, @Valid SessionForm session, BindingResult bindingResult) throws PortletModeException
 	{
 		if (bindingResult.hasErrors())
 		{
 			response.setPortletMode(PortletMode.EDIT);
 			response.setRenderParameter("sessionId", Long.toString(session.getSessionId()));
 		}
 		else
 		{
 			final ConferenceUser conferenceUser = this.conferenceUserService.getCurrentConferenceUser();
 			this.sessionService.createOrUpdateSession(conferenceUser, session);
 			response.setPortletMode(PortletMode.VIEW);
 		}
 	}
     
     @ActionMapping(params = "action=deleteSessions")
     public void deleteSession(ActionResponse response, @RequestParam long[] deleteSession) throws PortletModeException {
         //TODO do in try/catch?
         for (final long sessionId : deleteSession) {
             this.sessionService.removeSession(sessionId);
         }
         
         response.setPortletMode(PortletMode.VIEW);
     }
 
 	//TODO @Valid on name/email
     @ActionMapping(params = "action=Add Moderator")
     public void addSessionChair(ActionResponse response, @RequestParam long sessionId, @RequestParam String displayName, @RequestParam String email) throws PortletModeException {
         displayName = StringUtils.trimToNull(displayName);
         email = StringUtils.trimToNull(email);
         
         this.sessionService.addSessionChair(sessionId, displayName, email);
 
         response.setPortletMode(PortletMode.EDIT);
         response.setRenderParameter("sessionId", Long.toString(sessionId));
     }
 
     //TODO @Valid on deleteChair (must be email)
     @ActionMapping(params = "action=Delete Moderator(s)")
     public void deleteSessionChairs(ActionResponse response, @RequestParam long sessionId, @RequestParam long[] chairId) throws PortletModeException {
         this.sessionService.removeSessionChairs(sessionId, chairId);
 
         response.setPortletMode(PortletMode.EDIT);
         response.setRenderParameter("sessionId", Long.toString(sessionId));
     }
 
     //TODO @Valid on name/email
     @ActionMapping(params = "action=Add Participant")
     public void addSessionNonChair(ActionResponse response, @RequestParam long sessionId, @RequestParam String displayName, @RequestParam String email) throws PortletModeException {
         displayName = StringUtils.trimToNull(displayName);
         email = StringUtils.trimToNull(email);
         
         this.sessionService.addSessionNonChair(sessionId, displayName, email);
 
         response.setPortletMode(PortletMode.EDIT);
         response.setRenderParameter("sessionId", Long.toString(sessionId));
     }
 
     //TODO @Valid on deleteNonChair (must be email)
     @ActionMapping(params = "action=Delete Participant(s)")
     public void deleteSessionNonChairs(ActionResponse response, @RequestParam long sessionId, @RequestParam long[] nonChairId) throws PortletModeException {
         this.sessionService.removeSessionNonChairs(sessionId, nonChairId);
 
         response.setPortletMode(PortletMode.EDIT);
         response.setRenderParameter("sessionId", Long.toString(sessionId));
     }
     
     //TODO @Valid on multimediaUpload file types ".mpeg, .mpg, .mpe, .mov, .qt, .swf, .m4v, .mp3, .mp4, .mpeg, .wmv"
     @ActionMapping(params = "action=Upload Multimedia")
     public void uploadMultimedia(ActionResponse response, @RequestParam long sessionId, @RequestParam MultipartFile multimediaUpload) throws PortletModeException {
         this.sessionService.addMultimedia(sessionId, multimediaUpload);
 
         response.setPortletMode(PortletMode.EDIT);
         response.setRenderParameter("sessionId", Long.toString(sessionId));
     }
 
     @ActionMapping(params = "action=Delete Multimedia Item(s)")
     public void deleteMultimedia(ActionResponse response, @RequestParam long sessionId, @RequestParam long[] deleteMultimedia) throws PortletModeException {
         this.sessionService.deleteMultimedia(sessionId, deleteMultimedia);
 
         response.setPortletMode(PortletMode.EDIT);
         response.setRenderParameter("sessionId", Long.toString(sessionId));
     }
     
     //TODO @Valid on presentationUpload file types ".wbd, .wbp, .elp, .elpx"
     @ActionMapping(params = "action=Upload Presentation")
     public void uploadPresentation(ActionResponse response, @RequestParam long sessionId, @RequestParam MultipartFile presentationUpload) throws PortletModeException {
         this.sessionService.addPresentation(sessionId, presentationUpload);
 
         response.setPortletMode(PortletMode.EDIT);
         response.setRenderParameter("sessionId", Long.toString(sessionId));
     }
 
     @ActionMapping(params = "action=Delete Presentation")
     public void deletePresentation(ActionResponse response, @RequestParam long sessionId) throws PortletModeException {
         this.sessionService.deletePresentation(sessionId);
 
         response.setPortletMode(PortletMode.EDIT);
         response.setRenderParameter("sessionId", Long.toString(sessionId));
     }
 }
