 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2005, 2006 The Sakai Foundation.
  * 
  * Licensed under the Educational Community License, Version 1.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *      http://www.opensource.org/licenses/ecl1.php
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.tool.su;
 
 import java.util.Vector;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.authz.api.AuthzGroupService;
 import org.sakaiproject.authz.api.SecurityService;
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.sakaiproject.event.api.UsageSessionService;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 import org.sakaiproject.user.api.UserNotDefinedException;
 import org.sakaiproject.util.ResourceLoader;
 
 /**
  * @author zach.thomas@txstate.edu
  */
 public class SuTool
 {
 	private static final long serialVersionUID = 1L;
 
 	/** Our log (commons). */
 	private static Log M_log = LogFactory.getLog(SuTool.class);
 
 	ResourceLoader msgs = new ResourceLoader("tool-tool-su");
 
 	// Service instance variables
 	private AuthzGroupService M_authzGroupService = org.sakaiproject.authz.cover.AuthzGroupService
 			.getInstance();
 
 	private UserDirectoryService M_uds = org.sakaiproject.user.cover.UserDirectoryService.getInstance();
 
 	private SecurityService M_security = org.sakaiproject.authz.cover.SecurityService.getInstance();
 
 	private SessionManager M_session = org.sakaiproject.tool.cover.SessionManager.getInstance();
 
 	private ServerConfigurationService M_config = org.sakaiproject.component.cover.ServerConfigurationService
 			.getInstance();
 
 	// getters for these vars
 	private String username;
 
 	private String validatedUserId;
	
	private String validatedUserEid;
 
 	private User userinfo;
 
 	private boolean allowed = false;
 
 	// internal only vars
 	private String message = "";
 
 	private boolean confirm = false;
 
 	// base constructor
 	public SuTool()
 	{
 	}
 
 	/**
 	 * Functions
 	 */
 	public String su()
 	{
 
 		Session sakaiSession = M_session.getCurrentSession();
 		FacesContext fc = FacesContext.getCurrentInstance();
 		userinfo = null;
 		message = "";
 
 		if (!getAllowed())
 		{
 			confirm = false;
 			return "unauthorized";
 		}
 
 		try
 		{
 			// try with the user id
 			userinfo = M_uds.getUser(username.trim());
 			validatedUserId = userinfo.getId();
			validatedUserEid = userinfo.getEid();
 		}
 		catch (UserNotDefinedException e)
 		{
 			try
 			{
 				// try with the user eid
 				userinfo = M_uds.getUserByEid(username.trim());
 				validatedUserId = userinfo.getId();
				validatedUserEid = userinfo.getEid();
 			}
 			catch (UserNotDefinedException ee)
 			{
 				message = msgs.getString("no_such_user") + ": " + username;
 				fc.addMessage("su", new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message + ":" + ee));
 				M_log.warn("[SuTool] Exception: " + message);
 				confirm = false;
 				return "error";
 			}
 		}
 
 		if (!confirm)
 		{
			message = msgs.getString("displaying_info_for") + ": " + validatedUserEid;
 			fc.addMessage("su", new FacesMessage(FacesMessage.SEVERITY_INFO, message, message + ":" + userinfo.getDisplayName()));
 			return "unconfirmed";
 		}
 
 		// set the session user from the value supplied in the form
		message = "Username " + sakaiSession.getUserId() + " becoming " + validatedUserEid;
 		M_log.info("[SuTool] " + message);
 		fc.addMessage("su", new FacesMessage(FacesMessage.SEVERITY_INFO, message, message + ": Currently="
 				+ userinfo.getDisplayName()));
 		
 		// while keeping the official usage session under the real user id, swicth over everything else to be the SU'ed user
 		// Modeled on UsageSession's logout() and login()
 		
 		// logout - clear, but do not invalidate, preserve the usage session's current session
 		Vector saveAttributes = new Vector();
 		saveAttributes.add(UsageSessionService.USAGE_SESSION_KEY);
 		sakaiSession.clearExcept(saveAttributes);
 		
 		// login - set the user id and eid into session, and refresh this user's authz information
 		sakaiSession.setUserId(validatedUserId);
		sakaiSession.setUserEid(validatedUserEid);
 		M_authzGroupService.refreshUser(validatedUserId);
 
 		return "redirect";
 	}
 
 	// simple way to support 2 buttons that do almost the same thing
 	public String confirm()
 	{
 		confirm = true;
 		return su();
 	}
 
 	/**
 	 * Specialized Getters
 	 */
 	public boolean getAllowed()
 	{
 		Session sakaiSession = M_session.getCurrentSession();
 		FacesContext fc = FacesContext.getCurrentInstance();
 
 		if (!M_security.isSuperUser())
 		{
 			message = "Unauthorized user attempted access: " + sakaiSession.getUserId();
 			M_log.error("[SuTool] Fatal Error: " + message);
 			fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, message + ":" + " unauthorized"));
 			allowed = false;
 		}
 		else
 		{
 			allowed = true;
 		}
 
 		return allowed;
 	}
 
 	/**
 	 * Basic Getters and setters
 	 */
 	public String getUsername()
 	{
 		return username;
 	}
 
 	public String getPortalUrl()
 	{
 		return M_config.getPortalUrl();
 	}
 
 	public void setUsername(String username)
 	{
 		this.username = username;
 	}
 
 	public User getUserinfo()
 	{
 		return userinfo;
 	}
 
 	public void setUserinfo(User userinfo)
 	{
 		this.userinfo = userinfo;
 	}
 
 }
