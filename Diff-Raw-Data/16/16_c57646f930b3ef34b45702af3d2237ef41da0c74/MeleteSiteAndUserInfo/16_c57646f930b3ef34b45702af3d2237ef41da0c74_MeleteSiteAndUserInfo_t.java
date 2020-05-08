 /**********************************************************************************
  *
  * $URL$
  * $Id$  
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2011 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  * 
  *********************************************************************************/
 
 package org.etudes.tool.melete;
 
 import java.io.File;
 import java.util.Map;
 import org.sakaiproject.util.ResourceLoader;
 
 import javax.faces.context.FacesContext;
 import javax.faces.application.FacesMessage;
 import javax.faces.el.ValueBinding;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.MeleteSecurityService;
 import org.etudes.api.app.melete.ModuleService;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.cover.UserDirectoryService;
 
 public class MeleteSiteAndUserInfo
 {
 
 	/*
 	 * course site properties
 	 */
 	// Names of state attributes corresponding to properties of a site - From siteAction.java
 	private final static String PROP_SITE_TERM = "term";
 
 	private String course_id;
 
 	/*******************************************************************************
 	 * Dependencies
 	 *******************************************************************************/
 	/** Dependency: The logging service. */
 	protected Log logger = LogFactory.getLog(MeleteSiteAndUserInfo.class);
 
 	/** Dependency: The Melete Security service. */
 	protected MeleteSecurityService meleteSecurityService;
 
 	protected ModuleService moduleService;
 
 	/**
 	 * constructor added by rashmi
 	 */
 	public MeleteSiteAndUserInfo()
 	{
 	}
 
 	/**
 	 * @return true if user is admin or author, false otherwise
 	 */
 	public boolean checkAuthorization()
 	{
 		try
 		{
 			if (isSuperUser() || isUserAuthor())
 			{
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 		}
 		return false;
 	}
 
 	/**
 	 * @return true if user is admin, had melete author or student permissions, false otherwise
 	 */
 	public boolean checkMeleteRights()
 	{
 		try
 		{
 			if (isSuperUser() || isUserAuthor() || isUserStudent())
 			{
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 		}
 		return false;
 	}
 
 	/**
 	 * @return server url
 	 */
 	public String getAbsoluteTranslationLocation()
 	{
 		String remoteurl = ServerConfigurationService.getServerUrl();
 		return remoteurl;
 	}
 
 	/**
 	 * @return commonfilebrowser location for file browsing
 	 */
 	public String getCommonDirLocation()
 	{
 		String remoteurl = ServerConfigurationService.getServerUrl() + "/portal/tool/" + ToolManager.getCurrentPlacement().getId()
 				+ "/commonfilebrowser#";
 		return remoteurl;
 	}
 
 	/**
 	 * @return Returns the course_id.
 	 */
 	public String getCourse_id()
 	{
 		return course_id;
 	}
 
 	/**
 	 * gets the description of the course
 	 * 
 	 * @return Returns the course description
 	 */
 	public String getCourseDescription() throws Exception
 	{
 		try
 		{
 			return SiteService.getSite(ToolManager.getCurrentPlacement().getContext()).getDescription();
 			// ToolManager.getCurrentPlacement().g
 		}
 		catch (Exception e)
 		{
 			throw new Exception(e);
 		}
 	}
 
 	/**
 	 * gets the assigned term to the course
 	 * 
 	 * @return Returns the course term as Month Year
 	 */
 	public String getCourseTerm() throws Exception
 	{
 		try
 		{
 			return getSiteProperties().getProperty(PROP_SITE_TERM);
 		}
 		catch (Exception e)
 		{
 			throw new Exception(e);
 		}
 	}
 
 	/**
 	 * gets the title of the course
 	 * 
 	 * @return Returns the course title
 	 */
 	public String getCourseTitle() throws Exception
 	{
 		try
 		{
 			return SiteService.getSite(ToolManager.getCurrentPlacement().getContext()).getTitle();
 			// return ToolManager.getCurrentPlacement().getTitle();
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 			throw new Exception(e);
 		}
 	}
 
 	/**
 	 * gets the current site infromation
 	 * 
 	 * @return Returns the site object
 	 */
 	public Site getCurrenSite() throws Exception
 	{
 		try
 		{
 			return SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
 		}
 		catch (Exception e)
 		{
 			throw new Exception(e);
 		}
 	}
 
 	/**
 	 * gets the current site id
 	 * 
 	 * @return the current site id
 	 */
 	public String getCurrentSiteId()
 	{
 		return ToolManager.getCurrentPlacement().getContext();
 	}
 
 	/**
 	 * gets the current user
 	 * 
 	 * @return Returns the user currently logged in to the system
 	 */
 	public User getCurrentUser()
 	{
 		return UserDirectoryService.getCurrentUser();
 	}
 
 	/**
 	 * @return reference to sferyx jar file
 	 */
 	public String getEditorArchiveLocation()
 	{
 		String remoteurl = ServerConfigurationService.getServerUrl() + "/sferyx/sferyx/HTMLEditorAppletEnterprise.jar";
 		return remoteurl;
 	}
 
 	/**
 	 * @return max upload size set in sakai.properties
 	 */
 	public int getMaxUploadSize()
 	{
 		int maxSize = ServerConfigurationService.getInt("content.upload.max", 2);
 		// logger.debug("MAX UPLOAD SIZE IS "+maxSize);
 		return maxSize;
 	}
 
 	/**
 	 * @return meleteDocs uploads location
 	 */
 	public String getMeleteDocsLocation()
 	{
 		// String remoteurl = "/access/meleteDocs/content/private/meleteDocs/"+getCurrentSiteId()+"/uploads/";
 		String remoteurl = ServerConfigurationService.getServerUrl() + "/access/meleteDocs/content/private/meleteDocs/" + getCurrentSiteId()
 				+ "/uploads/";
 
 		return remoteurl;
 	}
 
 	/**
 	 * @return save.jsf reference to invoke save process for sferyx
 	 */
 	public String getMeleteDocsSaveLocation()
 	{
 		String remoteurl = ServerConfigurationService.getServerUrl() + "/etudes-melete-tool/melete/save.jsf";
 		return remoteurl;
 	}
 
 	/**
 	 * @return server location
 	 */
 	public String getRemoteBrowseLocation()
 	{
 		// String remoteurl = serverConfigurationService.getServerUrl() + "/etudes-melete-tool/melete/remotefilebrowser.jsf";
 		String remoteurl = ServerConfigurationService.getServerUrl() + "/portal/tool/" + ToolManager.getCurrentPlacement().getId()
 				+ "/remotefilebrowser#";
 		return remoteurl;
 	}
 
 	/**
 	 * @return link browser location
 	 */
 	public String getRemoteLinkBrowseLocation()
 	{
 		// String remoteurl = serverConfigurationService.getServerUrl() + "/etudes-melete-tool/melete/remotefilebrowser.jsf";
 		String remoteurl = ServerConfigurationService.getServerUrl() + "/portal/tool/" + ToolManager.getCurrentPlacement().getId()
 				+ "/remotelinkbrowser#";
 		return remoteurl;
 	}
 
 	/**
 	 * gets the season and year of the term
 	 * 
 	 * @return season and year
 	 * @throws Exception
 	 */
 	public String[] getTermSeasonYear() throws Exception
 	{
 		String strterm = getCourseTerm();
 		if (strterm != null && strterm.trim().length() > 0) return strterm.split(" ");
 
 		return null;
 	}
 
 	/**
 	 * @return translation bundle file
 	 */
 	public String getTranslationFile()
 	{
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		String translationfile = ServerConfigurationService.getServerUrl() + bundle.getString("translationfile");
 		return translationfile;
 	}
 
 	/**
 	 * gets the window name encoded
 	 * 
 	 * @return window name encoded
 	 */
 	public String getWinEncodeName()
 	{
 		String element = null;
 		String pid = ToolManager.getCurrentPlacement().getId();
 		if (pid != null)
 		{
 			element = escapeJavascript("Main" + pid);
 		}
 
 		return element;
 	}
 
 	/**
 	 * 
 	 * Check if the current user is a super user(admin)
 	 * 
 	 * @return true if the current user is a super user, false if not.
 	 */
 	public boolean isSuperUser()
 	{
 		return meleteSecurityService.isSuperUser(getCurrentUser().getId());
 	}
 
 	/**
 	 * Check if the current user has permission as author.
 	 * 
 	 * @return true if the current user has permission to perform this action, false if not.
 	 */
 	public boolean isUserAuthor() throws Exception
 	{
 
 		try
 		{
 			return meleteSecurityService.allowAuthor(ToolManager.getCurrentPlacement().getContext());
 		}
 		catch (Exception e)
 		{
 			throw e;
 		}
 	}
 
 	/**
 	 * Check if the current user has permission as student.
 	 * 
 	 * @return true if the current user has permission to perform this action, false if not.
 	 */
 	public boolean isUserStudent() throws Exception
 	{
 		try
 		{
 			return meleteSecurityService.allowStudent(ToolManager.getCurrentPlacement().getContext());
 		}
 		catch (Exception e)
 		{
 			throw e;
 		}
 	}
 
 	/**
 	 * Populate sessionMap with current site, user, first name, last name, institute nad max upload size
 	 * 
 	 */
 	public void populateMeleteSession()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		try
 		{
 			sessionMap.put("courseId", getCurrentSiteId());
 			sessionMap.put("userId", getCurrentUser().getId());
 			setCourse_id(getCurrentSiteId());
 			sessionMap.put("firstName", getCurrentUser().getFirstName());
 			sessionMap.put("lastName", getCurrentUser().getLastName());
 			sessionMap.put("institute", "Foothill College");
 			sessionMap.put("maxSize", String.valueOf(getMaxUploadSize()));
 
 			// logger.debug("Is Author is "+ isUserAuthor());
 			logger.debug("tool id: " + ToolManager.getCurrentPlacement().getToolId());
 
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 		}
 	}
 
 	/**
 	 * Navigates to related landing page based on user is either student or author
 	 * 
 	 * @return list_auth_modules or list_modules_student
 	 */
 	public String processNavigate()
 	{
 		// if (logger.isDebugEnabled()) logger.debug("new process navigate is called");
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		try
 		{
 			populateMeleteSession();
 
 			if (isSuperUser() || isUserAuthor())
 			{
 				setPage("INSTRUCTOR");
 				return "list_auth_modules";
 			}
 			if (isUserStudent())
 			{
 				setPage("STUDENT");
 				return "list_modules_student";
 			}
 
 		}
 		catch (Exception e)
 		{
 			String errMsg = bundle.getString("auth_failed");
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "auth_failed", errMsg));
 			logger.warn(e.toString());
 		}
 		return "list_modules_student";
 	}
 
 	/**
 	 * @param course_id
 	 *        The course_id to set.
 	 */
 	public void setCourse_id(String course_id)
 	{
 		this.course_id = course_id;
 	}
 
 	/**
 	 * @param meleteSecurityService
 	 *        The meleteSecurityService to set.
 	 */
 	public void setMeleteSecurityService(MeleteSecurityService meleteSecurityService)
 	{
 		this.meleteSecurityService = meleteSecurityService;
 	}
 
 	/**
 	 * @param moduleService
 	 *        The moduleService to set.
 	 */
 	public void setModuleService(ModuleService moduleService)
 	{
 		this.moduleService = moduleService;
 	}
 
 	/**
 	 * to get site properties like term(PROP_SITE_TERM), contactemail etc
 	 * 
 	 * @return siteProperties ResourceProperties object
 	 */
 	private ResourceProperties getSiteProperties() throws Exception
 	{
 		ResourceProperties siteProperties;
 		try
 		{
 			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
 			siteProperties = site.getProperties();
 
 			return siteProperties;
 
 		}
 		catch (IdUnusedException e)
 		{
 			throw new Exception(e);
 		}
 	}
 
 	/**
 	 * Set and initialize page info depending on user role
 	 * 
 	 * @param role
 	 *        INSTRUCTOR or STUDENT
 	 */
 	private void setPage(String role)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		int sz = moduleService.getCourseModuleSize(getCurrentSiteId());
 
 		if (role.equals("INSTRUCTOR"))
 		{
 			ValueBinding binding = Util.getBinding("#{listAuthModulesPage}");
 			ListAuthModulesPage listPage = (ListAuthModulesPage) binding.getValue(ctx);
 			if (sz <= 0)
 				listPage.setNomodsFlag(true);
 			else
 				listPage.setNomodsFlag(false);
 		}
 		else if (role.equals("STUDENT"))
 		{
 			ValueBinding binding = Util.getBinding("#{listModulesPage}");
 			ListModulesPage listPage = (ListModulesPage) binding.getValue(ctx);
 			if (sz <= 0)
 				listPage.setNomodsFlag(true);
 			else
 				listPage.setNomodsFlag(false);
 		}
 	}
 
 	/**
 	 * Return a string based on value that is safe to place into a javascript / html identifier: anything not alphanumeric change to 'x'. If the first character is not alphabetic, a letter 'i' is prepended.
 	 * 
 	 * @param value
 	 *        The string to escape.
 	 * @return value fully escaped using javascript / html identifier rules.
 	 */
 	protected String escapeJavascript(String value)
 	{
 		if (value == null || value == "") return "";
 		try
 		{
 			StringBuffer buf = new StringBuffer();
 
 			// prepend 'i' if first character is not a letter
 			if (!java.lang.Character.isLetter(value.charAt(0)))
 			{
 				buf.append("i");
 			}
 
 			// change non-alphanumeric characters to 'x'
 			for (int i = 0; i < value.length(); i++)
 			{
 				char c = value.charAt(i);
 				if (!java.lang.Character.isLetterOrDigit(c))
 				{
 					buf.append("x");
 				}
 				else
 				{
 					buf.append(c);
 				}
 			}
 
 			String rv = buf.toString();
 			return rv;
 		}
 		catch (Exception e)
 		{
 			return value;
 		}
 
 	}
 	
 	/**
 	 * Sets the return tool url. If param is null then melete is clearing the address.
 	 * 
 	 * @param back
 	 *        The tool address
 	 * 
 	 */
 	public void setNavigateCM(String back)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		Map m = ctx.getExternalContext().getSessionMap();
 		if (back != null)
 			m.put("backTo", back);
 		else
 			m.remove("backTo");
 	}
 
 	/**
 	 * Get the returning tool address
 	 * 
 	 * @return
 	 */
 	public String getNavigateCM()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		Map m = ctx.getExternalContext().getSessionMap();
		String returnUrl;
 		if (m.containsKey("backTo"))
 		{
			returnUrl = ServerConfigurationService.getServerUrl() + (String) m.get("backTo");
 			return returnUrl;
 		}
		else
		{
			if (ThreadLocalManager.get("MELETE_NAVIGATE_BACK") != null)
			{
				setNavigateCM((String) ThreadLocalManager.get("MELETE_NAVIGATE_BACK"));
				returnUrl = ServerConfigurationService.getServerUrl() + getNavigateCM();
				return returnUrl;
			}
		}
 		return null;
 	}
 
 	public String returnToCM()
 	{
 		if(getNavigateCM() != null)
 		{
 			return "coursemap";
 		}
 		return "#";
 	}
 }
