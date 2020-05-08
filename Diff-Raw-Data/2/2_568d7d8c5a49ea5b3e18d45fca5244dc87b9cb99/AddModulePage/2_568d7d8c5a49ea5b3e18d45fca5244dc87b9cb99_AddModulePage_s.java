 /**********************************************************************************
  *
  * $URL$
  * $Id$  
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
  **********************************************************************************/
 package org.etudes.tool.melete;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.ActionEvent;
 
 import org.sakaiproject.event.cover.EventTrackingService;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.util.ResourceLoader;
 
 public class AddModulePage extends ModulePage implements Serializable
 {
 
 	public AddModulePage()
 	{
 		this.module = null;
 		setModuleShdates(null);
 		setModuleDateBean(null);
 		setFormName("AddModuleForm");
 	}
 
 	/**
 	 * Set module to null to reset the values.
 	 */
 	public void setModuleNull()
 	{
 		this.module = null;
 		setModuleShdates(null);
 		resetModuleValues();
 	}
 
 	private void actualSave()
 	{
 		if (moduleService == null) moduleService = getModuleService();
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		Map sessionMap = context.getExternalContext().getSessionMap();
 
 		// validation
 		module.setTitle(module.getTitle().trim());
 
 		// get course info
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 		MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 		String courseId = mPage.getCurrentSiteId();
 		String userId = mPage.getCurrentUser().getId();
 
 		// actual insert
 		try
 		{
 			if (module.getKeywords() != null)
 			{
 				module.setKeywords(module.getKeywords().trim());
 			}
 			if (module.getKeywords() == null || (module.getKeywords().length() == 0))
 			{
 				module.setKeywords(module.getTitle());
 			}
 
 			moduleService.insertProperties(getModule(), getModuleShdates(), userId, courseId);
 			// add module to session
 			sessionMap.put("currModule", module);
 			// Track the event
 			EventTrackingService.post(EventTrackingService.newEvent("melete.module.new", ToolManager.getCurrentPlacement().getContext(), true));
 			setSuccess(true);
 		}
 		catch (Exception ex)
 		{
 			// logger.error("mbusiness insert module failed:" + ex.toString());
 			String errMsg = bundle.getString("add_module_fail");
 			addMessage(context, "Error Message", errMsg, FacesMessage.SEVERITY_ERROR);
 			return;
 		}
 	}
 	/**
 	 * saves the module into database. Validates the dates provided by the user. If keyword is not provided then keyword = title. Tracks add module event
 	 */
 
 	public String save()
 	{
 		setSuccess(false);
 		actualSave();	
 		FacesContext context = FacesContext.getCurrentInstance();
 		if (context.getMessages().hasNext()) return "failure";
 		return "list_auth_modules";
 	}
 
 	/**
 	 * Adds the blank section and resets and initializes objects.
 	 */
 	public void addContentSections(ActionEvent evt)
 	{
 		actualSave();
 		FacesContext context = FacesContext.getCurrentInstance();
 		if (context.getMessages().hasNext()) return;
 		ValueBinding binding = Util.getBinding("#{editSectionPage}");
 		EditSectionPage editPage = (EditSectionPage) binding.getValue(context);
 		editPage.setModule(module);
 		Integer newSecId = editPage.addBlankSection();
 		try
 		{
 			if (newSecId != null) context.getExternalContext().redirect("editmodulesections.jsf?sectionId=" + newSecId.toString());
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 
 	/**
 	 * For top mode bar clicks, auto save add module Returns # if save is success else stay on same page to correct error
 	 */
 	public String autoSave()
 	{
		if (save().equals("confirm_addmodule")) return "#";
 		return "add_module";
 	}
 }
