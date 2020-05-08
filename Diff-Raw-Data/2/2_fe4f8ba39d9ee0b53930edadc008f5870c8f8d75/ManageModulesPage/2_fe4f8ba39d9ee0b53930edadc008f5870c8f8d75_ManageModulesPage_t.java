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
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import org.sakaiproject.util.ResourceLoader;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIInput;
 import javax.faces.component.html.HtmlPanelGrid;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.component.app.melete.SectionBean;
 import org.etudes.component.app.melete.CourseModule;
 import org.etudes.component.app.melete.Module;
 //import org.sakaiproject.jsf.ToolBean;
 import org.etudes.api.app.melete.ModuleObjService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionService;
 import org.etudes.api.app.melete.exception.MeleteException;
 
 /**
  * @author Rashmi
  * Created on Jan 11, 2005
  * revised on 3/29 by rashmi to make restore button unclickable if no modules are present
  * Mallika - 4/22/05 - Added the association to go to module label
  * Rashmi - 5/23/05 - sort sections went blank when there are no modules
  * Rashmi - 8/7/06 - remove label and seq no from modules dropdown that appear on sort sections page
  * Mallika - 10/26/06 - adding code to set default module to the first one
  */
 public class ManageModulesPage implements Serializable/*,ToolBean*/{
 
 	// attributes
 	// restore modules
 	int count;
 	private List archiveModulesList;
 	private List restoreModulesList;
 	private boolean shouldRenderEmptyList;
 	private boolean falseBool = false;
 
 	boolean selectAllFlag;
 	int listSize;
 
 	/** Dependency:  The logging service. */
 	 protected Log logger = LogFactory.getLog(ManageModulesPage.class);
 	protected ModuleService moduleService;
 	protected SectionService sectionService;
 	/**
 	 * constructor
 	 */
 	public ManageModulesPage() {
 		count=0;
 		restoreModulesList = new ArrayList();
 		shouldRenderEmptyList=false;
 		archiveModulesList = null;
 		selectAllFlag = false;
 	}
 
 	/**
 	 * @return
 	 */
 	public List getArchiveModulesList()
 	{
 		try{
 			if(archiveModulesList == null)
 			{
 			restoreModulesList=null;
 			FacesContext context = FacesContext.getCurrentInstance();
 			Map sessionMap = context.getExternalContext().getSessionMap();
 
 			String courseId = (String)sessionMap.get("courseId");
 
 			archiveModulesList = getModuleService().getArchiveModules(courseId);
 			}
 		} catch(Exception e)
 		{
 			logger.debug("getting archived modules list "+e.toString());
 		}
 		listSize = archiveModulesList.size();
 		return archiveModulesList;
 	}
 
 	/**
 	 * @return
 	 */
 	public List getRestoreModulesList()
 	{
 		return restoreModulesList;
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean getShouldRenderEmptyList()
 	{
 		if (archiveModulesList == null || archiveModulesList.size() == 0)
 		{
 			return true;
 		}else return false;
 	}
 
 	/*
 	 * added by rashmi on 3/29 to not render restore commandlink if modules are not present
 	 */
 	public boolean getFalseBool()
 	{
 		return false;
 	}
 	/*
 	 * adding listener
 	 */
   public void selectedRestoreModule(ValueChangeEvent event)throws AbortProcessingException
 	{
   		FacesContext context = FacesContext.getCurrentInstance();
 		UIInput mod_Selected = (UIInput)event.getComponent();
 
 		if(((Boolean)mod_Selected.getValue()).booleanValue() == true)
 		  {
 			String selclientId = mod_Selected.getClientId(context);
 			selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 			selclientId = selclientId.substring(selclientId.indexOf(':')+1);
 
 			String modId = selclientId.substring(0,selclientId.indexOf(':'));
 			int selectedModIndex=Integer.parseInt(modId);
 
 			if(restoreModulesList == null)
 				restoreModulesList = new ArrayList();
 
 			restoreModulesList.add(archiveModulesList.get(selectedModIndex));
 			count++;
 
 		  }
 		return;
 	}
 
 	  /**
 	 * @return
 	 */
 	public String restoreModules()
 	  {
 		FacesContext context = FacesContext.getCurrentInstance();
 	   ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 	   Map sessionMap = context.getExternalContext().getSessionMap();
 
 		if(count <=0)
 		{
 			String msg = bundle.getString("no_module_selected");
 			addMessage(context, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 			return "restore_modules";
 		}
 		// 1. restore modules
 		try{
 			String courseId = (String)sessionMap.get("courseId");
 			getModuleService().restoreModules(restoreModulesList,courseId);
 			count=0;
 			}
 		catch(Exception me)
 			{
 
 				String errMsg = bundle.getString("restore_module_fail");
 				context.addMessage (null, new FacesMessage(errMsg));
 				return "restore_modules";
 			}
 
 		// 2. clear archivelist
 			archiveModulesList = null;
 
		String confMsg = bundle.getString("restore_modules_msg");
		addMessage(context, "Info", confMsg, FacesMessage.SEVERITY_INFO);
 		return "list_auth_modules";
 	  }
 
 	public String deleteModules()
 	  {
 		List<Module> delModules = new ArrayList<Module>(0);
 
 		FacesContext context = FacesContext.getCurrentInstance();
 	   ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 	   Map sessionMap = context.getExternalContext().getSessionMap();
 
 		if(count <=0)
 		{
 			String msg = bundle.getString("no_module_delete_selected");
 			addMessage(context, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 			return "restore_modules";
 		}
 
         for (ListIterator i = restoreModulesList.listIterator(); i.hasNext(); )
          {
 	       CourseModule cmod  = (CourseModule)i.next();
 	      delModules.add((Module)cmod.getModule());
          }
 
 
 			ValueBinding binding = Util.getBinding("#{deleteModulePage}");
 			DeleteModulePage dmPage = (DeleteModulePage) binding.getValue(context);
 			dmPage.setModules(delModules);
 			dmPage.setModuleSelected(true);
 			count=0;
 
 
 		// 2. clear archivelist
 			archiveModulesList = null;
 
 	  	return "delete_module";
 	  }
 
 
 	/**
 	 * clear lists on cancel
 	 */
 	public String cancelRestoreModules()
 	{
 		archiveModulesList = null;
 		restoreModulesList= null;
 		count=0;
 		return "modules_author_manage";
 	}
 
 	/**
 	 *navigates manage modules page on cancel
 	 */
 	public String cancel()
 	{
 		return "modules_author_manage";
 	}
 
 	/**
 	 * sort in ascending order
 	 */
 	public String sortOnDate()
 	{
 		if(archiveModulesList == null)
 		{
 			return "restore_modules";
 		}
 		Collections.sort(archiveModulesList, new MeleteDateComparator());
 		return "restore_modules";
 	}
 
 	/*
 	 * navigation rule to go to restore page
 	 */
 	public String goToRestoreModules(){
 		return "restore_modules";
 	}
 
 	/*
 	 * navigation rule to go to manage content page
 	 */
 	public String goToManageContent(){
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{manageResourcesPage}");
 		ManageResourcesPage managePage = (ManageResourcesPage) binding.getValue(context);
 		managePage.resetValues();
 		return "manage_content";
 	}
 
 	
 
 	/**
 	 * gets navigation page to import export modules
 	 * @return
 	 */
 	public String importExportModules(){
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{exportMeleteModules}");
 		ExportMeleteModules exportModules = (ExportMeleteModules) binding.getValue(context);
 		exportModules.resetValues();
 		return "importexportmodules";
 	}
 
 	/*
 	 * Reset all values
 	 */
 	public void resetValues()
 	{
 		archiveModulesList = null;
 		restoreModulesList= null;
 		count=0;		
 		selectAllFlag = false;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{deleteModulePage}");
 		DeleteModulePage dmPage = (DeleteModulePage) binding.getValue(ctx);
 		dmPage.setMdbean(null);
 		dmPage.setModuleSelected(false);
 		dmPage.setSection(null);
 		dmPage.setSectionSelected(false);
 		dmPage.setModules(null);
 		dmPage.setSectionBeans(null);
 	}
 	
 	private void addMessage(FacesContext ctx, String msgName, String msgDetail, FacesMessage.Severity severity)
 	{
 		FacesMessage msg = new FacesMessage(msgName, msgDetail);
 		msg.setSeverity(severity);
 		ctx.addMessage(null, msg);
 	}	
 
   public boolean getSelectAllFlag()
 	{
 		return selectAllFlag;
 	}
 
 	public void setSelectAllFlag(boolean selectAllFlag)
 	{
 		this.selectAllFlag = selectAllFlag;
 	}
 
 	public int getListSize()
 	{
 		return listSize;
 	}
 
 	public void setListSize(int listSize)
 	{
 		this.listSize = listSize;
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
 
 
 	public HtmlPanelGrid getSeqTable() {
 		return null;
 	}
 
 	/**
 	 * @return Returns the sectionService.
 	 */
 	public SectionService getSectionService() {
 		return sectionService;
 	}
 	/**
 	 * @param sectionService The sectionService to set.
 	 */
 	public void setSectionService(SectionService sectionService) {
 		this.sectionService = sectionService;
 	}	
 }
