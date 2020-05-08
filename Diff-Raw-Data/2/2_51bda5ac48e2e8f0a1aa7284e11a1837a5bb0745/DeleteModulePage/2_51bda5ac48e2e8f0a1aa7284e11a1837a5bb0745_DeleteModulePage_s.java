 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009 Etudes, Inc.
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
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import org.sakaiproject.util.ResourceLoader;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.component.app.melete.Module;
 import org.etudes.component.app.melete.SectionBean;
 import org.etudes.api.app.melete.ModuleDateBeanService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionService;
 import org.etudes.api.app.melete.SectionObjService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.component.app.melete.ModuleDateBean;
 
 import org.sakaiproject.event.cover.EventTrackingService;
 import org.sakaiproject.tool.cover.ToolManager;
 /**
  * @author Mallika
  *
  * Delete Module Page is the backing bean for the page delete_module.jsp.
  * It also connects to other jsp pages like confirm_delete.jsp and
  * list_auth_modules.jsp
  *
  * Rashmi - 10/24/06 - clean up comments and change logger.info to debug
  */
 
 public class DeleteModulePage implements Serializable/*,ToolBean*/{
 	protected ModuleDateBeanService mdbean;
 	protected SectionObjService section;
 	 /** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(DeleteModulePage.class);
 	protected ModuleService moduleService;
 	protected SectionService sectionService;
 	private boolean success;
 	private boolean moduleSelected;
 	private boolean sectionSelected;
 	private List modules = null;
 	private List sectionBeans = null;
 	String courseId;
 	String userId;
 	private boolean sameModuleSectionSelected;
 
     public DeleteModulePage(){
        	this.mdbean = null;
     	this.section = null;
     	this.modules = null;
     	this.sectionBeans = null;
     	sameModuleSectionSelected = false;
     	courseId = null;
     	userId = null;
     }
 
   	/*
 	 * setting module
 	 */
 	public void setMdbean(ModuleDateBeanService mdbean)
 	{
 		this.mdbean = mdbean;
 	}
 
 	public ModuleDateBeanService getMdbean()
 	{
 		return this.mdbean;
 	}
 
 	public void setModules(List modules) {
 		    this.modules = modules;
 
 	}
 
 	public List getModules() {
 		 return this.modules;
 	  }
   	/*
 	 * setting section
 	 */
 	public void setSection(SectionObjService section)
 	{
 		this.section = section;
 	}
 	public SectionObjService getSection()
 	{
 		return this.section;
 	}
 	public void setSectionBeans(List sectionBeansList) {
 	    this.sectionBeans = sectionBeansList;
     }
 
    public List getSectionBeans() {
     return this.sectionBeans;
    }
 
 	public void setSuccess(boolean success)
 	{
 		this.success = success;
 	}
 	public boolean getSuccess()
 	{
 		return success;
 	}
 	public void setModuleSelected(boolean moduleSelected)
 	{
 		this.moduleSelected = moduleSelected;
 	}
 	public boolean getModuleSelected()
 	{
 		return moduleSelected;
 	}
 	public void setSectionSelected(boolean sectionSelected)
 	{
 		this.sectionSelected = sectionSelected;
 	}
 	public boolean getSectionSelected()
 	{
 		return sectionSelected;
 	}
 
 	private void CheckSectionsSelected() throws Exception
 	{
 		HashMap modulekeys = new HashMap();
 		for (int i=0; i < modules.size(); i++)
 		{
 			Module mod = (Module)modules.get(i);
 			Integer mkey = new Integer(mod.getModuleId());
 			modulekeys.put(mkey,mkey);
 		}
 
 		for(int i=0; i < sectionBeans.size();i++)
 		{
 			SectionBean secbean = (SectionBean)sectionBeans.get(i);
 			Integer checkModId = new Integer(secbean.getSection().getModuleId());
 			if (modulekeys.containsKey(checkModId)) throw new MeleteException("same_module_section_selected");
 		}
 	}
 
 	private void removeSectionsSelectedToModule() throws Exception
 	{
 		HashMap modulekeys = new HashMap();
 		for (int i=0; i < modules.size(); i++)
 		{
 			Module mod = (Module)modules.get(i);
 			Integer mkey = new Integer(mod.getModuleId());
 			modulekeys.put(mkey,mkey);
 		}
 		Iterator iter = sectionBeans.iterator();
 		while(iter != null && iter.hasNext())
 		{
 			SectionBean secbean = (SectionBean)iter.next();
 			Integer checkModId = new Integer(secbean.getSection().getModuleId());
 			if (modulekeys.containsKey(checkModId)) iter.remove();
 		}
 	}
     /*
      * Called by the jsp page to delete the module or section and redirect to the confirmation page.
      */
     public String deleteAction()
     {
           if(moduleService == null)
         	moduleService = getModuleService();
         FacesContext context = FacesContext.getCurrentInstance();
         ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
         // actual delete
 		try{
 
 			if (getModuleSelected() == true)
 			{
 				 // check if sections of selected module are selected too
 				if(sectionBeans != null)CheckSectionsSelected();
 				moduleService.deleteModules(this.modules,getCourseId(), getUserId());
 
 				Iterator it = this.modules.iterator();
 				while (it.hasNext()){
 					  Module obj = (Module) it.next();
 					  //Track the event
 					  EventTrackingService.post(EventTrackingService.newEvent("melete.module.delete", ToolManager.getCurrentPlacement().getContext(), true));
 				}
 
 			}
 			if (getSectionSelected() == true)
 			{
 				sectionService.deleteSections(this.sectionBeans,getCourseId(), getUserId());
 
 				Iterator it = this.sectionBeans.iterator();
 				while (it.hasNext()){
 					  SectionBean obj = (SectionBean) it.next();
 					  //Track the event
 					  EventTrackingService.post(EventTrackingService.newEvent("melete.section.delete", ToolManager.getCurrentPlacement().getContext(), true));
 				}
 
 			}
 		}
 		catch(MeleteException me)
 		{
 			String errMsg = bundle.getString(me.getMessage());
 	     	context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,me.getMessage(),errMsg));
 			sameModuleSectionSelected = true;
 			return "delete_module";
 		}
 		catch(Exception ex)
 		{
 			String errMsg = bundle.getString("delete_module_fail");
 	     	context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"delete_module_fail",errMsg));
 			return "delete_module";
 		}
 		setSuccess(true);
 		String deleteMsg = bundle.getString("confirm_delete_module_msg");
 		FacesMessage msg = new FacesMessage("Delete Confirmation",deleteMsg);
 		msg.setSeverity(FacesMessage.SEVERITY_INFO);
 		context.addMessage(null,msg);
 		 return "list_auth_modules";
     }
 
     /*
      * doubleConfirm module and sections belonging to it are selected
      */
     public String reConfirmedDeleteAction()
     {
           if(moduleService == null)
         	moduleService = getModuleService();
         FacesContext context = FacesContext.getCurrentInstance();
         ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
         // actual delete
 		try{
 
 			if (getModuleSelected() == true)
 			{
 				 // check if sections of selected module are selected too
 		        removeSectionsSelectedToModule();
 				moduleService.deleteModules(this.modules,getCourseId(), getUserId());
 				sameModuleSectionSelected = false;
 			}
 			if (getSectionSelected() == true)
 			{
 			   sectionService.deleteSections(this.sectionBeans,getCourseId(), getUserId());
 			}
 		}
 		catch(Exception ex)
 		{
 			String errMsg = bundle.getString("delete_module_fail");
 	     	context.addMessage (null, new FacesMessage(errMsg));
 			return "delete_module";
 		}
 		setSuccess(true);
       return "confirm_delete_module";
     }
 
 	public String backToModules()
 	{
 		setMdbean(null);
 		setModuleSelected(false);
 		setSection(null);
 		setSectionSelected(false);
 		setModules(null);
 		setSectionBeans(null);
 		sameModuleSectionSelected = false;
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{listModulesPage}");
 		ListModulesPage listPage = (ListModulesPage)
 	        binding.getValue(context);
 		listPage.setViewModuleBeans(null);
 		return "list_auth_modules";
 	}
 
 	/**
 	 * @return naviagtion rule on click of cancel button
 	 */
 	public String cancel()
 	{
 		setMdbean(null);
 		setModuleSelected(false);
 		setSection(null);
 		setSectionSelected(false);
 		setModules(null);
 		setSectionBeans(null);
 		sameModuleSectionSelected = false;
 
 		return "list_auth_modules";
 	}
 	private String getCourseId()
 	{
 		if (courseId == null)
 		{
 		FacesContext context = FacesContext.getCurrentInstance();
 	  	Map sessionMap = context.getExternalContext().getSessionMap();
 		courseId = (String)sessionMap.get("courseId");
 		}
 		return courseId;
 	}
 	private String getUserId()
 	{
 		if (userId == null)
 		{
 		FacesContext context = FacesContext.getCurrentInstance();
 	  	Map sessionMap = context.getExternalContext().getSessionMap();
 		userId = (String)sessionMap.get("userId");
 		}
 		return userId;
 	}
 	/**
 	 * @return Returns the ModuleService.
 	 */
 	public ModuleService getModuleService() {
 		return moduleService;
 	}
 	/**
 	 * @param ModuleService The ModuleService to set.
 	 */
 	public void setModuleService(ModuleService moduleService) {
 		this.moduleService = moduleService;
 	}
 	/**
 	 * @return Returns the SectionService.
 	 */
 	public SectionService getSectionService() {
 		return sectionService;
 	}
 	/**
 	 * @param SectionService The SectionService to set.
 	 */
 	public void setSectionService(SectionService sectionService) {
 		this.sectionService = sectionService;
 	}
 
 	/**
 	 * @return Returns the sameModuleSectionSelected.
 	 */
 	public boolean isSameModuleSectionSelected() {
 		return sameModuleSectionSelected;
 	}
 
 	
  }
