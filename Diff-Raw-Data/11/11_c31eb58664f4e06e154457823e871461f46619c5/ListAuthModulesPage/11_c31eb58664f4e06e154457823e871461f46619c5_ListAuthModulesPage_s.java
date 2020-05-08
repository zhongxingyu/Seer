 /**********************************************************************************
  *
  * $URL$
  *
  ***********************************************************************************
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
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIData;
 import javax.faces.component.UIInput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.ModuleObjService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionBeanService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.component.app.melete.ModuleDateBean;
 import org.etudes.component.app.melete.Section;
 import org.etudes.component.app.melete.SectionBean;
 import org.sakaiproject.util.ResourceLoader;
 
 /**
  * Gets the module beans for author
  *
  */
 
 class SecModObj implements Comparable
 {
 	Integer modIndex, secIndex;
 
 	public SecModObj(int modInd, int secInd)
 	{
 		modIndex = new Integer(modInd);
 		secIndex = new Integer(secInd);
 	}
 
 	public Integer getModObj()
 	{
 		return modIndex;
 	}
 
 	public Integer getSecObj()
 	{
 		return secIndex;
 	}
 
 	public int compareTo(Object smObj) throws ClassCastException
 	{
 		if (!(smObj instanceof SecModObj)) throw new ClassCastException("SecModObj class expected.");
 		int compModIndex = ((SecModObj) smObj).getModObj().intValue();
 		return this.getModObj().intValue() - compModIndex;
 	}
 
 }
 
 public class ListAuthModulesPage implements Serializable
 {
 
 	/** Dependency: The logging service. */
 	protected Log logger = LogFactory.getLog(ListAuthModulesPage.class);
 
 	private List moduleDateBeans = null;
 
 	private List errModuleIds = null;
 
 	/** identifier field */
 	private int showModuleId;
     private int showInvalidModuleId;
 
 	private String formName;
 
 	private Date currentDate;
 
 	private boolean selectedSection;
 
 	private Boolean nomodsFlag;
 
 	private boolean expandAllFlag;
 
 	private boolean autonumber;
 	
 	// This needs to be set later using Utils.getBinding
 	String courseId;
 
 	String userId;
 
 	// rashmi added
 	int count;
 
 	int selectedModIndex;
 
 	private List selectedModIndices = null;
 
 	private List selectedSecModIndices = null;
 
 	boolean moduleSelected;
 
 	int selectedSecIndex;
 
 	boolean sectionSelected;
 	
 	boolean selectAllFlag;
 
 	private ModuleService moduleService;
 
 	private boolean trueFlag = true;
 
 	private List nullList = null;
 
 	private Integer printModuleId;
 	
 	int listSize;
 
 	// added by rashmi on apr 8
 	private String isNull = null;
 
 	private UIData table;
 	private UIData secTable;
 
 	/**
 	 * @return value of datatable (in which modules are rendered)
 	 */
 	public UIData getTable()
 	{
 		return table;
 	}
 
 	/**
 	 * @param table module datatable to set
 	 */
 	public void setTable(UIData table)
 	{
 		this.table = table;
 	}
 
 	public ListAuthModulesPage()
 	{
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
     	MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
     	
 		courseId = mPage.getCurrentSiteId();
 		userId = mPage.getCurrentUser().getId();
 		nomodsFlag = null;
 		setShowModuleId(-1);
 		count = 0;
 		selectedModIndex = -1;
 		moduleSelected = false;
 		selectedModIndices = null;
 		selectedSecModIndices = null;
 
 		selectedSecIndex = -1;
 		sectionSelected = false;
 		binding = Util.getBinding("#{authorPreferences}");
 		AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(context);
 		String expFlag = preferencePage.getUserView();
 		if (expFlag.equals("true"))
 		{
 			expandAllFlag = true;
 		}
 		else
 		{
 			expandAllFlag = false;
 		}
 		selectAllFlag = false;
 		listSize = 0;
 	}
 
 	/**
 	 * Reset all flags(expand,collapse,auto numbering) and set all indexes to -1 and lists to null
 	 * 
 	 */
 	public void resetValues()
 	{
 		setShowModuleId(-1);
 		errModuleIds = null;
 		nomodsFlag = null;
 		count = 0;
 		selectedModIndex = -1;
 		moduleSelected = false;
 		selectedModIndices = null;
 		selectedSecModIndices = null;
 
 		selectedSecIndex = -1;
 		sectionSelected = false;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{authorPreferences}");
 		AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(ctx);
 		String expFlag = preferencePage.getUserView();
 		if (expFlag.equals("true"))
 		{
 			expandAllFlag = true;
 		}
 		else
 		{
 			expandAllFlag = false;
 		}
                 String autonum = preferencePage.getMaterialAutonumber();
 		if (autonum.equals("true"))
 		{  autonumber = true;
 		} else {
 		   autonumber = false;
 		}
 		selectAllFlag = false;
 		listSize = 0;
 		moduleDateBeans = null;
 		setShowInvalidModuleId(-1);
 	}
 
 	/**
 	 * @return autoNumbering preference value
 	 */
 	public boolean isAutonumber()
 	{
 		return autonumber;
 	};
 	
 	/** Set date flag of each module to false
 	 * 
 	 */
 	public void resetDateFlags()
 	{
 		resetSelectedLists();
 		for (ListIterator i = moduleDateBeans.listIterator(); i.hasNext();)
 		{
 			ModuleDateBean mdbean = (ModuleDateBean) i.next();
 			mdbean.setDateFlag(false);
 		}
 
 	}
 
 	/**
 	 * @return Returns the ModuleService.
 	 */
 	public ModuleService getModuleService()
 	{
 		return moduleService;
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
 	 * @return boolean value of trueFlag
 	 */
 	public boolean getTrueFlag()
 	{
 		return trueFlag;
 	}
 
 	/**
 	 * @param trueFlag boolean value of trueFlag
 	 */
 	public void setTrueFlag(boolean trueFlag)
 	{
 		this.trueFlag = trueFlag;
 	}
 
 	/**
 	 * @return value of nullList
 	 */
 	public List getNullList()
 	{
 		return nullList;
 	}
 
 	/**
 	 * @param nullList value of nullList
 	 */
 	public void setNullList(List nullList)
 	{
 		this.nullList = nullList;
 	}
 
 	
 	/**Method that triggers when a module is selected
 	 * @param event ValueChangeEvent object
 	 * @throws AbortProcessingException
 	 */
 	public void selectedModuleSection(ValueChangeEvent event) throws AbortProcessingException
 	{
 		if (selectAllFlag == false)
 		{	
 		
 		FacesContext context = FacesContext.getCurrentInstance();
 		UIInput mod_Selected = (UIInput) event.getComponent();
 		if (((Boolean) mod_Selected.getValue()).booleanValue() == true)
 			count++;
 		else
 			count--;
 
 		String selclientId = mod_Selected.getClientId(context);
 		if (logger.isDebugEnabled()) logger.debug("Sel client ID is " + selclientId);
 		selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 		selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 		String modId = selclientId.substring(0, selclientId.indexOf(':'));
 
 		selectedModIndex = Integer.parseInt(modId);
 		if (selectedModIndices == null)
 		{
 			selectedModIndices = new ArrayList();
 		}
 		selectedModIndices.add(new Integer(selectedModIndex));
 		moduleSelected = true;
 		}
 		return;
 	}
 
 	/**Method that triggers when a section is selected
 	 * @param event ValueChangeEvent object
 	 * @throws AbortProcessingException
 	 */
 	public void selectedSection(ValueChangeEvent event) throws AbortProcessingException
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		UIInput sec_Selected = (UIInput) event.getComponent();
 		if (((Boolean) sec_Selected.getValue()).booleanValue() == true)
 			count++;
 		else
 			count--;
 
 		String selclientId = sec_Selected.getClientId(context);
 		if (logger.isDebugEnabled()) logger.debug("Sel client ID is " + selclientId);
 		selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 		selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 		String modId = selclientId.substring(0, selclientId.indexOf(':'));
 		selectedModIndex = Integer.parseInt(modId);
 		selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 		selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 		String sectionindex = selclientId.substring(0, selclientId.indexOf(':'));
 		selectedSecIndex = Integer.parseInt(sectionindex);
 		if (selectedSecModIndices == null)
 		{
 			selectedSecModIndices = new ArrayList();
 		}
 		selectedSecModIndices.add(new SecModObj(selectedModIndex, selectedSecIndex));
 		sectionSelected = true;
 
 		return;
 	}
 	
 	/**Method that triggers when all modules are selected
 	 * @param event ValueChangeEvent object
 	 * @throws AbortProcessingException
 	 */
 	public void selectAllModules(ValueChangeEvent event) throws AbortProcessingException
 	{
 		selectAllFlag= true;
 		int k = 0;
 		if (selectedModIndices == null)
 		{
 			selectedModIndices = new ArrayList();
 		}
 		for (ListIterator i = moduleDateBeans.listIterator(); i.hasNext();)
 		{
 			ModuleDateBean mdbean = (ModuleDateBean) i.next();
 			mdbean.setSelected(true);
 			selectedModIndices.add(new Integer(k));
 			k++;
 		}
 		count = moduleDateBeans.size();
 		if (count == 1) selectedModIndex = 0;
 		moduleSelected = true;
 		return;
 	}	
 
 	/** Reset selected module lists and selectAllFlag to false
 	 * 
 	 */
 	public void resetSelectedLists()
 	{
 		selectedModIndices = null;
 		selectedSecModIndices = null;
 		selectAllFlag = false;
 	}
 	
 	 /** Method to show invalid module
 	 * @return list_auth_modules
 	 */
 	public String showHideInvalid()
 	 {
 		 ModuleDateBean mdbean = null;
 		 FacesContext ctx = FacesContext.getCurrentInstance();
 		 UIViewRoot root = ctx.getViewRoot();
 		 UIData table = null;
 		 table = (UIData)
 		 root.findComponent("listauthmodulesform").findComponent("table");
 
 		 mdbean = (ModuleDateBean) table.getRowData();
 		 if (getShowInvalidModuleId() != mdbean.getModuleId())
 		 {
 			 setShowInvalidModuleId(mdbean.getModuleId());
 		 }
 
 		 return "list_auth_modules";
 	 }	
 
 
 	  /** Method to hide invalid modules
 	 * @return list_auth_modules
 	 */
 	public String hideInvalid()
 	  {
 		  setShowInvalidModuleId(-1);
 		  return "list_auth_modules";
 	  }
 	  	
 
 	/** Get list of modules, flag modules with invalid dates
 	 * @return list of ModuleDateBean objects
 	 */
 	public List getModuleDateBeans()
 	{
 		resetSelectedLists();
 		setCurrentDate(Calendar.getInstance().getTime());
 		FacesContext context = FacesContext.getCurrentInstance();
 		// reset courseid. Its getting lost when edit_module called from coursemap calls TOC
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
     	MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
     	courseId = mPage.getCurrentSiteId();
 
     	boolean flagsReset = false;
 		try
 		{
 			ModuleService modServ = getModuleService();
 			// fetch beans
 			if (nomodsFlag == null || moduleDateBeans == null)
 				moduleDateBeans = modServ.getModuleDateBeans(userId, courseId);
 			
 			// for bug reports
 			if (moduleDateBeans == null || moduleDateBeans.size() == 0) 
 			{
 				listSize = 0;
 				nomodsFlag = true;
 				return moduleDateBeans;
 			}
 			// end
 			nomodsFlag = false;
 			listSize = moduleDateBeans.size();
 			Iterator itr = context.getMessages();
 			while (itr.hasNext())
 			{
 				String msg = ((FacesMessage) itr.next()).getDetail();
 				if (msg.equals("Input data is not in the correct format."))
 				{
 					resetDateFlags();
 					flagsReset = true;
 				}
 				else
 				{
 					break;
 				}
 			}
 			// selectedModIndices = new ArrayList();
 			for (ListIterator i = moduleDateBeans.listIterator(); i.hasNext();)
 			{
 				ModuleDateBean mdbean = (ModuleDateBean) i.next();
 				// If there is an invalid format message, don't set lollipop
 				if (flagsReset == false)
 				{
 					if (errModuleIds != null)
 					{
 						if (errModuleIds.size() > 0)
 						{
 							for (ListIterator l = errModuleIds.listIterator(); l.hasNext();)
 							{
 								ModuleDateBean errmdbean = (ModuleDateBean) l.next();
 								if (errmdbean.getModuleId() == mdbean.getModuleId())
 								{
 									mdbean.setDateFlag(true);
 									mdbean.getModuleShdate().setStartDate(errmdbean.getModuleShdate().getStartDate());
 									mdbean.getModuleShdate().setEndDate(errmdbean.getModuleShdate().getEndDate());
 								}
 							}
 
 						}
 					}
 				}
 
 
 			}
 		}
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 		}
 
 		return moduleDateBeans;
 	}
 
 	/**
 	 * @return value of currentDate
 	 */
 	public Date getCurrentDate()
 	{
 		return currentDate;
 	}
 
 	/**
 	 * @param currentDate date value
 	 */
 	public void setCurrentDate(Date currentDate)
 	{
 		this.currentDate = currentDate;
 	}
 
 	/**
 	 * @param moduleDateBeansList list of ModuleDateBean objects
 	 */
 	public void setModuleDateBeans(List moduleDateBeansList)
 	{
 		moduleDateBeans = moduleDateBeansList;
 	}
 
 	/**
 	 * @return value of showModuleId
 	 */
 	public int getShowModuleId()
 	{
 		return this.showModuleId;
 	}
 
 	/**
 	 * @param moduleId integer value of moduleId
 	 */
 	public void setShowModuleId(int moduleId)
 	{
 		this.showModuleId = moduleId;
 	}
 
 	/**
 	 * @return boolean value of selectedSection
 	 */
 	public boolean getSelectedSection()
 	{
 		return selectedSection;
 	}
 
 	/**
 	 * @param selectedSection boolean value of selectedSection
 	 */
 	public void setSelectedSection(boolean selectedSection)
 	{
 		this.selectedSection = selectedSection;
 	}
 
 	/** Invokes getModuleDateBeans method to set nomodsFlag value
 	 * @return boolean value of nomodsFlag
 	 */
 	public boolean getNomodsFlag()
 	{
 		if(nomodsFlag == null) getModuleDateBeans();
 		return nomodsFlag;
 	}
 
 	/**
 	 * @param nomodsFlag boolean value
 	 */
 	public void setNomodsFlag(boolean nomodsFlag)
 	{
 		this.nomodsFlag = nomodsFlag;
 	}
 
 	/**
 	 * @return boolean value of expandAllFlag
 	 */
 	public boolean getExpandAllFlag()
 	{
 		return expandAllFlag;
 	}
 
 	/**
 	 * @param expandAllFlag boolean value
 	 */
 	public void setExpandAllFlag(boolean expandAllFlag)
 	{
 		this.expandAllFlag = expandAllFlag;
 	}
 	
 	/**
 	 * @return boolean value of selectAllFlag
 	 */
 	public boolean getSelectAllFlag()
 	{
 		return selectAllFlag;
 	}
 
 	/**
 	 * @param selectAllFlag boolean value
 	 */
 	public void setSelectAllFlag(boolean selectAllFlag)
 	{
 		this.selectAllFlag = selectAllFlag;
 	}	
 
 	/**
 	 * @return integer value of listSize
 	 */
 	public int getListSize()
 	{
 		return listSize;
 	}
 	
 	/**
 	 * @param listSize integer value
 	 */
 	public void setListSize(int listSize)
 	{
 		this.listSize = listSize;
 	}
 	
 	/** Method to expand or collapse individual modules' sections
 	 * @return list_auth_modules
 	 */
 	public String showHideSections()
 	{
 		resetSelectedLists();
 		if (getExpandAllFlag() == true)
 		{
 			setShowModuleId(-1);
 			setExpandAllFlag(false);
 		}
 		else
 		{
 			FacesContext ctx = FacesContext.getCurrentInstance();
 			UIViewRoot root = ctx.getViewRoot();
 			UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 			ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 			if (getShowModuleId() != mdbean.getModuleId())
 			{	
 			   setShowModuleId(mdbean.getModuleId());
 			}
 			else
 			{
 				setShowModuleId(-1);
 				setExpandAllFlag(false);
 			}
 		}
 		return "list_auth_modules";
 	}
 	
 	/** Method to expand or collapse all modules' sections
 	 * @return list_auth_modules
 	 */
 	public String expandCollapseAction()
 	{
 		resetSelectedLists();
 	
 		if (getExpandAllFlag() == false)
 		{		
 		  setExpandAllFlag(true);
 		}
 		else
 		{	
 		  setExpandAllFlag(false);
 		  setShowModuleId(-1);
 		}  
 		return "list_auth_modules";		
 	}
 	
 	/**Reset lists and navigate to edit module or section page depending on what is selected
 	 * @return list_auth_modules or edit_module or editmodulesections
 	 */
 	public String editAction()
 	{
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 
 		if (count >= 2)
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_error");
 			addMessage(ctx, "Select  ERROR", msg, FacesMessage.SEVERITY_ERROR);
 			count = 0;
 			moduleSelected = false;
 			sectionSelected = false;
 			return "list_auth_modules";
 		}
 		count = 0;
 
 		// module selected
 		if (moduleSelected)
 		{
 			if (moduleDateBeans != null && selectedModIndex > -1)
 			{
 			ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selectedModIndex);
 			ValueBinding binding = Util.getBinding("#{editModulePage}");
 			EditModulePage emPage = (EditModulePage) binding.getValue(ctx);
 			emPage.setEditInfo(mdbean);
 			// added by rashmi to show correct module number
 			emPage.resetModuleNumber();
 
 			count = 0;
 			moduleSelected = false;
 			// Mallika -3/24/05
 			sectionSelected = false;
 			return "edit_module";
 			}
 		}
 		if (sectionSelected)
 		{
 			if (moduleDateBeans != null && selectedModIndex > -1 && selectedSecIndex > -1)
 			{
 			ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selectedModIndex);
 			SectionBeanService secBean = (SectionBeanService) mdbean.getSectionBeans().get(selectedSecIndex);
 			ValueBinding binding = Util.getBinding("#{editSectionPage}");
 			EditSectionPage esPage = (EditSectionPage) binding.getValue(ctx);
 			esPage.setEditInfo((Section) secBean.getSection());
 
 			sectionSelected = false;
 			// Mallika - 3/24/05
 			moduleSelected = false;
 			return "editmodulesections";
 			}
 		}
 		if ((moduleSelected == false) && (sectionSelected == false))
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_one_edit");
 			addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		moduleSelected = false;
 		sectionSelected = false;
 		return "list_auth_modules";
 	}
 
 	
 	/**Reset selected lists and navigate to add module page
 	 * @return add_module
 	 */
 	public String AddModuleAction()
 	{
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{addModulePage}");
 
 		AddModulePage amPage = (AddModulePage) binding.getValue(ctx);
 		amPage.setModuleNull();
 
 		return "add_module";
 	}
 
 	/** Reset selected lists and navigate to add section page
 	 * @return editmodulesections or list_auth_modules
 	 */
 	public String AddContentAction()
 	{
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 
 		if (count >= 2)
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_error");
 			addMessage(ctx, "Select  ERROR", msg, FacesMessage.SEVERITY_ERROR);
 			count = 0;
 
 			moduleSelected = false;
 			sectionSelected = false;
 			return "list_auth_modules";
 		}
 		count = 0;
 		// module selected
 		if (moduleSelected || sectionSelected)
 		{
 			if(selectedModIndex <= -1) selectedModIndex = 0;
 			ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selectedModIndex);
 
 			ValueBinding binding = Util.getBinding("#{editSectionPage}");
 			FacesContext context = FacesContext.getCurrentInstance();
 			EditSectionPage editPage = (EditSectionPage) binding.getValue(context);
 			editPage.setSection(null);
 			editPage.resetSectionValues();
 			editPage.setModule(mdbean.getModule());
 			
 			Map sessionMap = context.getExternalContext().getSessionMap();
 			sessionMap.put("currModule", mdbean.getModule());
 			
 			editPage.addBlankSection();
 
 			count = 0;
 			moduleSelected = false;
 			// Mallika -3/24/05
 			sectionSelected = false;
 			return "editmodulesections";
 		}
 		if ((moduleSelected == false) && (sectionSelected == false))
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_one_add");
 			addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		moduleSelected = false;
 		sectionSelected = false;
 		return "list_auth_modules";
 	}
 
 	/** Inactivate modules and display a message about modules that have been inactivated
 	 * @return list_auth_modules
 	 */
 	public String InactivateAction()
 	{
 
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		List selModBeans = null;
 		ModuleDateBean mdbean = null;
 		if (sectionSelected)
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_module");
 			addMessage(ctx, "Select Module", msg, FacesMessage.SEVERITY_ERROR);
			sectionSelected = false;
			moduleSelected = false;
			count = 0;
			selectedSecModIndices = null;
 			return "list_auth_modules";
 		}
 
 		// module selected
 		if (moduleSelected && selectedModIndices != null)
 		{
 
 			if (selModBeans == null)
 			{
 				selModBeans = new ArrayList();
 			}
 			for (ListIterator i = selectedModIndices.listIterator(); i.hasNext();)
 			{
 				mdbean = (ModuleDateBean) moduleDateBeans.get(((Integer) i.next()).intValue());
 				selModBeans.add(mdbean);
 			}
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			try
 			{
 				int origSeqNo = mdbean.getCmod().getSeqNo();
 				getModuleService().archiveModules(selModBeans,moduleDateBeans,courseId);
 				StringBuffer modTitles = new StringBuffer();
 				mdbean = null;
 				for (ListIterator i = selModBeans.listIterator(); i.hasNext();)
 				{
 					mdbean = (ModuleDateBean) i.next();
 					modTitles.append(mdbean.getModule().getTitle());
 					modTitles.append(", ");
 				}
 				modTitles.delete(modTitles.toString().length() - 2, modTitles.toString().length());
 				String msg1 = bundle.getString("inactivate_message1");
 				String msg2 = bundle.getString("inactivate_message2");
 				addMessage(ctx, "Inactivate Message", msg1 + modTitles.toString() + msg2, FacesMessage.SEVERITY_INFO);
 			}
 			catch (Exception ex)
 			{
 				logger.debug(ex.toString());
 				String errmsg = bundle.getString("archive_fail");
 				addMessage(ctx, "Error Message", errmsg, FacesMessage.SEVERITY_ERROR);
 				return "list_auth_modules";
 			}
 			count = 0;
 			// Mallika - 3/24/05 added this to prevent selected value from being stored
 			moduleSelected = false;
 			sectionSelected = false;
 			return "list_auth_modules";
 		}
 
 		if ((moduleSelected == false) && (sectionSelected == false))
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_one_module");
 			addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		count = 0;
 		moduleSelected = false;
 		sectionSelected = false;
 		return "list_auth_modules";
 	}
 
 	/**Redirect to edit module page
 	 * @return edit_module
 	 */
 	public String redirectToEditModule()
 	{
 		return "edit_module";
 	}
 
 	/** Redirect to edit module page with selected module id
 	 * @param evt ActionEvent object
 	 */
 	public void editModule(ActionEvent evt)
 	{
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		Map params = ctx.getExternalContext().getRequestParameterMap();
 		int selModIndex = Integer.parseInt((String) params.get("modidx"));
 
 		ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selModIndex);
 		ValueBinding binding = Util.getBinding("#{editModulePage}");
 		EditModulePage emPage = (EditModulePage) binding.getValue(ctx);
 		emPage.setEditInfo(mdbean);
 		emPage.resetModuleNumber();
 	}
 
 	/** Redirect to edit section page
 	 * @return editmodulesections
 	 */
 	public String redirectToEditSection()
 	{
 		return "editmodulesections";
 	}
 
 	/** Redirect to edit section page with selected section id
 	 * @param evt ActionEvent object
 	 */
 	public void editSection(ActionEvent evt)
 	{
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		Map params = ctx.getExternalContext().getRequestParameterMap();
 	  	int selModIndex = Integer.parseInt((String) params.get("modidx"));
 	  	int selSecIndex = Integer.parseInt((String) params.get("secidx"));
 
 		ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selModIndex);
 		SectionBeanService secBean = (SectionBeanService) mdbean.getSectionBeans().get(selSecIndex);
 
 		ValueBinding binding = Util.getBinding("#{editSectionPage}");
 		EditSectionPage esPage = (EditSectionPage) binding.getValue(ctx);
 		Map sessionMap = ctx.getExternalContext().getSessionMap();
 		sessionMap.put("currModule", ((Section) secBean.getSection()).getModule());
 		esPage.setEditInfo((Section) secBean.getSection());
 	}
 
 	/** Redirect to delete confirmation page with selected modules or sections
 	 * @return delete_module or list_auth_modules
 	 */
 	public String deleteAction()
 	{
 
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		List delMods = null;
 		List delSecBeans = null;
 
 		count = 0;
 
 		// added by rashmi
 		if (!moduleSelected && !sectionSelected)
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_one_delete");
 			addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		// add end
 		// module selected
 		if (moduleSelected)
 		{
 			ModuleDateBean mdbean = null;
 			if (delMods == null)
 			{
 				delMods = new ArrayList();
 			}
 			if (selectedModIndices != null)
 			{
 			  for (ListIterator i = selectedModIndices.listIterator(); i.hasNext();)
 			  {
 				mdbean = (ModuleDateBean) moduleDateBeans.get(((Integer) i.next()).intValue());
 				delMods.add(mdbean.getModule());
 			  }
 			}
 			ValueBinding binding = Util.getBinding("#{deleteModulePage}");
 			DeleteModulePage dmPage = (DeleteModulePage) binding.getValue(ctx);
 			// dmPage.setMdbean(mdbean);
 			dmPage.setModules(delMods);
 		//	List <> allActivenArchvModules = moduleService.getAllActivenArchvModules();
 			dmPage.setModuleSelected(true);
 			count = 0;
 			moduleSelected = false;
 			selectedModIndices = null;
 		//	delModBeans = null;
 			// We do not want to bypass processing of section if sections and modules are selected
 			if (sectionSelected == false)
 			{
 				return "delete_module";
 			}
 		}
 		if (sectionSelected)
 		{
 			ModuleDateBean mdbean = null;
 			SectionBeanService secBean = null;
 			if (delSecBeans == null)
 			{
 				delSecBeans = new ArrayList();
 			}
 			if(selectedSecModIndices == null)
 			{
 				ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 				String msg = bundle.getString("select_one_delete");
 				addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 				moduleSelected = false;
 				sectionSelected = false;
 				resetSelectedLists();
 				return "list_auth_modules";
 			}
 
 			for (ListIterator i = selectedSecModIndices.listIterator(); i.hasNext();)
 			{
 				SecModObj smObj = (SecModObj) i.next();
 				mdbean = (ModuleDateBean) moduleDateBeans.get((((Integer) smObj.getModObj())).intValue());
 				secBean = (SectionBeanService) mdbean.getSectionBeans().get((((Integer) smObj.getSecObj())).intValue());
 				delSecBeans.add(secBean);
 			}
 
 			ValueBinding binding = Util.getBinding("#{deleteModulePage}");
 			DeleteModulePage dmPage = (DeleteModulePage) binding.getValue(ctx);
 			dmPage.setSection((Section) secBean.getSection());
 			dmPage.setSectionBeans(delSecBeans);
 			dmPage.setSectionSelected(true);
 
 			count = 0;
 			sectionSelected = false;
 			selectedSecModIndices = null;
 			// Mallika - 3/24/05
 
 			return "delete_module";
 		}
 
 		moduleSelected = false;
 		sectionSelected = false;
 		resetSelectedLists();
 		return "list_auth_modules";
 	}
 
 	/** Validate dates and save changes
 	 * @return list_auth_modules
 	 */
 	public String saveChanges()
 	{
 		resetSelectedLists();
 		FacesContext ctx = null;
 		ResourceLoader bundle = null;
 		boolean dateErrFlag = false;
 		boolean yearTooBigFlag = false;
 		errModuleIds = new ArrayList();
 		try
 		{
 			Iterator moduleIter = moduleDateBeans.iterator();
 			ctx = FacesContext.getCurrentInstance();
 			bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			Calendar stCal = null;
 			Calendar enCal = null;
 			while (moduleIter.hasNext())
 			{
 				ModuleDateBean mdbean = (ModuleDateBean) moduleIter.next();
 				mdbean.setDateFlag(false);
 				if (mdbean.getModuleShdate().getStartDate() != null)
 				{
 					stCal = Calendar.getInstance();
 					stCal.setTime(mdbean.getModuleShdate().getStartDate());
 					if (stCal.get(Calendar.YEAR) > 9999)
 					{
 					  yearTooBigFlag = true;
 					  mdbean.setDateFlag(true);
 					}
 				}
 				if (mdbean.getModuleShdate().getEndDate() != null)
 				{
 					enCal = Calendar.getInstance();
 					enCal.setTime(mdbean.getModuleShdate().getEndDate());
 					if (enCal.get(Calendar.YEAR) > 9999)
 					{
 					  yearTooBigFlag = true;
 					  mdbean.setDateFlag(true);
 					}
 				}
 				if ((mdbean.getModuleShdate().getStartDate() != null)&&(mdbean.getModuleShdate().getEndDate() != null))
 				{
 				  if (mdbean.getModuleShdate().getStartDate().compareTo(mdbean.getModuleShdate().getEndDate()) >= 0)
 				  {
 					dateErrFlag = true;
 					mdbean.setDateFlag(true);
 					/*
 					 * addDateErrorMessage(ctx); return "list_auth_modules";
 					 */
 				  }
 
 			     }
 				if (mdbean.isDateFlag() == true)
 				  {
 					  errModuleIds.add(mdbean);
 				  }
 			}
 			  getModuleService().updateProperties(moduleDateBeans, courseId);
 
 
 			if ((yearTooBigFlag == true)||(dateErrFlag == true))
 			{
 			  if (yearTooBigFlag == true)
 			  {
 			  String msg = bundle.getString("year_toobig_error");
 			  addMessage(ctx, "Year Error", msg, FacesMessage.SEVERITY_ERROR);
 			  }
 			  if (dateErrFlag == true)
 			  {
 				String msg = bundle.getString("date_error");
 				addMessage(ctx, "Date Error", msg, FacesMessage.SEVERITY_ERROR);
 			  }
 			}
 			else
 			{
 				String msg = bundle.getString("changes_saved");
 				addMessage(ctx, "Changes Saved", msg, FacesMessage.SEVERITY_INFO);
 			}
 			}
 
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 			String msg = bundle.getString("list_auth_modules_fail");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 			return "list_auth_modules";
 		}
 		return "list_auth_modules";
 	}
 
 	/** Redirect to list auth page
 	 * @return list_auth_modules
 	 */
 	public String cancelChanges()
 	{
 		return "list_auth_modules";
 	}
 
 	/** Redirect to view module page
 	 * @return view_module
 	 */
 	public String viewModule()
 	{
 		return "view_module";
 	}
 
 	/** Redirect to view section page
 	 * @return view_section
 	 */
 	public String viewSection()
 	{
 		return "view_section";
 	}
 
 	/** Redirect to list auth page
 	 * @return list_auth_modules
 	 */
 	public String viewPrereqs()
 	{
 		return "list_auth_modules";
 	}
 
 	
 	/** Reset selected lists and redirect to module_post_steps page
 	 * @return module_post_steps
 	 */
 	public String viewNextsteps()
 	{
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UIViewRoot root = ctx.getViewRoot();
 		UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 		ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 		ValueBinding binding = Util.getBinding("#{moduleNextStepsPage}");
 		ModuleNextStepsPage nextPage = (ModuleNextStepsPage) binding.getValue(ctx);
 		nextPage.setMdBean(mdbean);
 		return "module_post_steps";
 	}
 	
 	/** Reset selected lists and redirect to list of special accesses page
 	 * @return list_special_access
 	 */
 	public String specialAccessAction()
 	{
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UIViewRoot root = ctx.getViewRoot();
 		UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 		ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 		ValueBinding binding = Util.getBinding("#{specialAccessPage}");
 		SpecialAccessPage specialAccessPage = (SpecialAccessPage) binding.getValue(ctx);
 		specialAccessPage.setModuleId(mdbean.getModule().getModuleId().intValue());
 		specialAccessPage.setSaList(null);
 		return "list_special_access";
 	}	
 
 	
 	/**
 	 * @return String value of isNull
 	 */
 	public String getIsNull()
 	{
 		return isNull;
 	}
 
 	/**Reset values, set selected flags to false, count to 0
 	 * and selectedSecModIndices to null
 	 */
 	private void resetSubSectionValues()
 	{
 		sectionSelected = false;
 		moduleSelected = false;
 		count = 0;
 		selectedSecModIndices = null;
 	}
 
 	/** Get indentation level of a section
 	 * @param indentBean SectionBeanService object
 	 * @param secBeans list of section bean objects
 	 * @return true if indentation is less than 10 levels, false otherwise
 	 */
 	private boolean findIndent(SectionBeanService indentBean, List secBeans)
 	{
 		String pattern = "\\.";
 		int occurs = indentBean.getDisplaySequence().split(pattern).length - 1;
 
 		if (occurs >= 10)
 		{
 			return false;
 		}
 		else
 		{
 			String indDispSeq = indentBean.getDisplaySequence();
 			for (ListIterator i = secBeans.listIterator(); i.hasNext();)
 			{
 				SectionBeanService secBean = (SectionBeanService) i.next();
 				String sbDispSeq = secBean.getDisplaySequence();
 				if (sbDispSeq.startsWith(indDispSeq))
 				{
 					occurs = 0;
 					occurs = sbDispSeq.split(pattern).length - 1;
 					if (occurs >= 10) return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	
 	/** Indent this section by first checking if indentation is less than 10 levels
 	 * @return list_auth_modules
 	 */
 	public String CreateSubSectionAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		// if module is selected then throw message
 		if (moduleSelected)
 		{
 			String msg = bundle.getString("list_select_indent");
 			addMessage(ctx, "Select  Indent", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 
 		if ((moduleSelected == false) && (sectionSelected == false))
 		{
 			String msg = bundle.getString("list_select_one_indent");
 			addMessage(ctx, "Select Indent", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 		if (sectionSelected == true)
 		{
 			SecModObj smObj = null;
 			ModuleDateBean mdbean = null;
 			SectionBeanService secBean = null;
 			List indentSecBeans = null;
 			if (indentSecBeans == null)
 			{
 				indentSecBeans = new ArrayList();
 			}
 
 			if (selectedSecModIndices == null) 
 			{
 				resetSubSectionValues();
 				return "list_auth_modules";
 			}
 			// If one section is selected, we check if its the first section
 			// or if it is too deep to indent
 			// If multiple sections are selected, we indent those that we can
 			// and leave the others alone
 			if (selectedSecModIndices.size() == 1)
 			{
 				smObj = (SecModObj) selectedSecModIndices.get(0);
 				mdbean = (ModuleDateBean) moduleDateBeans.get((((Integer) smObj.getModObj())).intValue());
 				int selIndex = ((Integer) smObj.getSecObj()).intValue();
 				// If user tries to indent first section, return
 				if (mdbean.getSectionBeans().size() < 2 || selIndex == 0)
 				{
 					logger.debug("First section can't be change to subsection");
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 				else
 				{
 					secBean = (SectionBeanService) mdbean.getSectionBeans().get(selIndex);
 					boolean indentOk = findIndent(secBean, mdbean.getSectionBeans());
 
 					// Only allow indent upto 10 levels
 					if (indentOk)
 					{
 						try
 						{
 							indentSecBeans.add(secBean);
 							moduleService.createSubSection(mdbean.getModule(), indentSecBeans);
 						}
 						catch (MeleteException me)
 						{
 							logger.debug(me.toString());
 							String msg = bundle.getString("indent_right_fail");
 							addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 						}
 					}
 					else
 					{
 						String msg = bundle.getString("indent_right_toodeep");
 						addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 					}
 				}
 			}
 			else
 			{
 				// Multiple indent code
 				boolean res = checkDifModules(selectedSecModIndices);
 				if (res == true)
 				{
 					String msg = bundle.getString("list_select_in_onemodule");
 					addMessage(ctx, "Select Indent", msg, FacesMessage.SEVERITY_ERROR);
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 				else
 				{
 					mdbean = null;
 					secBean = null;
 
 					for (ListIterator i = selectedSecModIndices.listIterator(); i.hasNext();)
 					{
 						smObj = (SecModObj) i.next();
 						mdbean = (ModuleDateBean) moduleDateBeans.get((((Integer) smObj.getModObj())).intValue());
 						secBean = (SectionBeanService) mdbean.getSectionBeans().get((((Integer) smObj.getSecObj())).intValue());
 						boolean indentOk = findIndent(secBean, mdbean.getSectionBeans());
 
 						if (indentOk)
 						{
 							indentSecBeans.add(secBean);
 						}
 					}
 					try
 					{
 						moduleService.createSubSection(mdbean.getModule(), indentSecBeans);
 					}
 					catch (MeleteException me)
 					{
 						logger.debug(me.toString());
 						String msg = bundle.getString("indent_right_fail");
 						addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 					}
 				}
 			}
 		}
 		resetSubSectionValues();
 		int saveShowId = showModuleId;
 		resetValues();
 		setShowModuleId(saveShowId);
 		return "list_auth_modules";
 	}
 
 	/** Bring section one level up in indentation
 	 * @return list_auth_modules
 	 */
 	public String BringSubSectionLevelUpAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		if (count == 0)
 		{
 			String msg = bundle.getString("list_select_one_indent");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 
 		// if module is selected then throw message
 		if (moduleSelected)
 		{
 			String msg = bundle.getString("list_select_indent");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 
 		// section selected
 		/*
 		 * if(sectionSelected && count != 1) { String msg = bundle.getString("list_select_one_indent"); addMessage(ctx,"Error
 		 * Message",msg,FacesMessage.SEVERITY_ERROR); resetSubSectionValues(); return "list_auth_modules"; }
 		 */
 
 		if (sectionSelected == true)
 		{
 			SecModObj smObj = null;
 			ModuleDateBean mdbean = null;
 			SectionBeanService secBean = null;
 			List indentSecBeans = null;
 			if (indentSecBeans == null)
 			{
 				indentSecBeans = new ArrayList();
 			}
 			
 			if (selectedSecModIndices == null) 
 			{
 				resetSubSectionValues();
 				return "list_auth_modules";
 			}
 			// If one section is selected, we check if its the top level section
 			// If multiple sections are selected, we indent those that we can
 			// and leave the others alone
 			if (selectedSecModIndices.size() == 1)
 			{
 				smObj = (SecModObj) selectedSecModIndices.get(0);
 				mdbean = (ModuleDateBean) moduleDateBeans.get((((Integer) smObj.getModObj())).intValue());
 				secBean = (SectionBeanService) mdbean.getSectionBeans().get((((Integer) smObj.getSecObj())).intValue());
 				if (checkTopLevelSection(secBean.getDisplaySequence()))
 				{
 					logger.debug("Top level section can't indent left more");
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 				try
 				{
 					indentSecBeans.add(secBean);
 					moduleService.bringOneLevelUp(mdbean.getModule(), indentSecBeans);
 				}
 				catch (MeleteException me)
 				{
 					logger.debug(me.toString());
 					String msg = bundle.getString("indent_left_fail");
 					addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 				}
 			}
 			else
 			{
 				// Multiple indent code
 				boolean res = checkDifModules(selectedSecModIndices);
 				if (res == true)
 				{
 					String msg = bundle.getString("list_select_in_onemodule");
 					addMessage(ctx, "Select Indent", msg, FacesMessage.SEVERITY_ERROR);
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 				else
 				{
 					mdbean = null;
 					secBean = null;
 
 					for (ListIterator i = selectedSecModIndices.listIterator(); i.hasNext();)
 					{
 						smObj = (SecModObj) i.next();
 						mdbean = (ModuleDateBean) moduleDateBeans.get((((Integer) smObj.getModObj())).intValue());
 						secBean = (SectionBeanService) mdbean.getSectionBeans().get((((Integer) smObj.getSecObj())).intValue());
 						indentSecBeans.add(secBean);
 					}
 					try
 					{
 						moduleService.bringOneLevelUp(mdbean.getModule(), indentSecBeans);
 					}
 					catch (MeleteException me)
 					{
 						logger.debug(me.toString());
 						String msg = bundle.getString("indent_left_fail");
 						addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 					}
 				}
 			}
 		}
 		resetSubSectionValues();
 		int saveShowId = showModuleId;
 		resetValues();
 		setShowModuleId(saveShowId);
 		return "list_auth_modules";
 	}
 
 	/** Check if the current section is at the top level
 	 * @param dispSeq Display sequence of section being checked
 	 * @return true if its a top level section, false otherwise
 	 */
 	private boolean checkTopLevelSection(String dispSeq)
 	{
 		String pattern = "\\.";
 		int occurs = dispSeq.split(pattern).length - 1;
 		if (occurs == 1) return true;
 		return false;
 	}
 
 	/** Check if selected sections are in different modules
 	 * @param selectedSecModIndices list of SecMod objects
 	 * @return true if sections are in different modules, false otherwise
 	 */
 	private boolean checkDifModules(List selectedSecModIndices)
 	{
 		Collections.sort(selectedSecModIndices);
 		Integer firstMod = ((SecModObj) selectedSecModIndices.get(0)).getModObj();
 		Integer lastMod = ((SecModObj) selectedSecModIndices.get(selectedSecModIndices.size() - 1)).getModObj();
 		if (!(firstMod.equals(lastMod)))
 			return true;
 		else
 			return false;
 	}
 
 	
 	/** Move module or section one up in the list
 	 * @return list_auth_modules
 	 */
 	public String MoveItemUpAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		// if module is selected then throw message
 		logger.debug("values" + moduleSelected + count + selectedModIndices + selectedSecModIndices + sectionSelected);
 
 		if (count != -1)
 		{
 			String msg = bundle.getString("select_one_move");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 
 		try
 		{
 			if (moduleSelected)
 			{
 				int selIndex = ((Integer) selectedModIndices.get(0)).intValue();
 
 				if (moduleDateBeans.size() < 2 || selIndex == 0)
 				{
 					logger.debug("first module selected to move up");
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 				ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selIndex);
 				logger.debug("calling sort for " + mdbean.getModule().getTitle());
 				moduleService.sortModule(mdbean.getModule(), courseId, "up");
 			}
 			if (sectionSelected)
 			{
 				SecModObj smObj = (SecModObj) selectedSecModIndices.get(0);
 				ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get((((Integer) smObj.getModObj())).intValue());
 				int selIndex = ((Integer) smObj.getSecObj()).intValue();
 				if (mdbean.getSectionBeans().size() < 2 || selIndex == 0)
 				{
 					logger.debug("one item in the list or first section is selected to move up");
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 				SectionBeanService secBean = (SectionBeanService) mdbean.getSectionBeans().get(selIndex);
 				moduleService.sortSectionItem(mdbean.getModule(), secBean.getSection().getSectionId().toString(), "up");
 			}
 		}
 		catch (MeleteException me)
 		{
 			logger.debug(me.toString());
 			me.printStackTrace();
 			String msg = bundle.getString("sort_fail");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		resetSubSectionValues();
 		return "list_auth_modules";
 	}
 
 	/** Move module or section one down in the list
 	 * @return list_auth_modules
 	 */
 	public String MoveItemDownAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		// if module is selected then throw message
 		if (count != 1)
 		{
 			String msg = bundle.getString("select_one_move");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 			resetSubSectionValues();
 			return "list_auth_modules";
 		}
 
 		try
 		{
 			if (moduleSelected)
 			{
 				int selIndex = ((Integer) selectedModIndices.get(0)).intValue();
 
 				if (moduleDateBeans.size() < 2 || (selIndex == moduleDateBeans.size() - 1))
 				{
 					logger.debug("last module selected to move down");
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 				ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selIndex);
 				logger.debug("calling sort for " + mdbean.getModule().getTitle());
 				moduleService.sortModule((ModuleObjService) mdbean.getModule(), courseId, "down");
 			}
 			if (sectionSelected)
 			{
 				SecModObj smObj = (SecModObj) selectedSecModIndices.get(0);
 				ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get((((Integer) smObj.getModObj())).intValue());
 				int selIndex = ((Integer) smObj.getSecObj()).intValue();
 
 				if (mdbean.getSectionBeans().size() < 2 || (selIndex == mdbean.getSectionBeans().size() - 1))
 				{
 					logger.debug("one item in the list or last section is selected to move down");
 					resetSubSectionValues();
 					return "list_auth_modules";
 				}
 				SectionBeanService secBean = (SectionBeanService) mdbean.getSectionBeans().get(selIndex);
 				moduleService.sortSectionItem((ModuleObjService) mdbean.getModule(), secBean.getSection().getSectionId().toString(), "down");
 			}
 		}
 		catch (MeleteException me)
 		{
 			logger.debug(me.toString());
 			String msg = bundle.getString("sort_fail");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		resetSubSectionValues();
 		return "list_auth_modules";
 	}
 
 	/** Make a copy of the selected module
 	 * @return list_auth_modules
 	 */
 	public String duplicateAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		try
 		{
 			resetSelectedLists();
 			UIViewRoot root = ctx.getViewRoot();
 			UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 			ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 			logger.debug("calling copy for " + mdbean.getModule().getTitle());
 			moduleService.copyModule((ModuleObjService) mdbean.getModule(), courseId, userId);
 		}
 		catch (MeleteException me)
 		{
 			logger.debug(me.toString());
 			String msg = bundle.getString("copy_fail");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		resetValues();
 		return "list_auth_modules";
 	}
 	
 	/** Move sections from one module to another
 	 * @return list_auth_modules or move_section
 	 */
 	public String MoveSectionAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");

 		try
 		{
 			if (count == 0 || moduleSelected)
 			{
 				String msg = bundle.getString("select_mv_section");
 				addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 				resetSubSectionValues();
 				return "list_auth_modules";
 			}
 
 			if (sectionSelected && selectedSecModIndices != null)
 			{
 				ModuleDateBean mdbean = null;
 				SectionBean secBean = null;
 				ArrayList<SectionBean> moveSectionBeans = new ArrayList<SectionBean>(0);
 
 				for (ListIterator<SecModObj> i = selectedSecModIndices.listIterator(); i.hasNext();)
 				{
 					SecModObj smObj = i.next();
 					mdbean = (ModuleDateBean) moduleDateBeans.get((((Integer) smObj.getModObj())).intValue());
 					secBean = (SectionBean) mdbean.getSectionBeans().get((((Integer) smObj.getSecObj())).intValue());
 					moveSectionBeans.add(secBean);
 				}
 				ValueBinding binding = Util.getBinding("#{moveSectionsPage}");
 				MoveSectionsPage mvPage = (MoveSectionsPage) binding.getValue(ctx);
 				mvPage.resetValues();
 				mvPage.setSectionBeans(moveSectionBeans);
 
 				count = 0;
 				sectionSelected = false;
 				selectedSecModIndices = null;
 				return "move_section";
 			}
 
 		}
 		catch (Exception me)
 		{
 			logger.debug(me.toString());
 			String msg = bundle.getString("copy_fail");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 		}
 
 		return "list_auth_modules";
 	}
 	
 	/** Get module id to print
 	 * @return module id to print
 	 */
 	public Integer getPrintModuleId()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		try
 		{
 			resetSelectedLists();
 			UIViewRoot root = ctx.getViewRoot();
 			UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 			ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 			printModuleId = mdbean.getModule().getModuleId();
 			return printModuleId;
 		}
 		catch (Exception me)
 		{
 			logger.error(me.toString());
 			String msg = bundle.getString("print_module_fail");
 			addMessage(ctx, "Error Message", msg, FacesMessage.SEVERITY_ERROR);
 		}
 		return 0;
 	}
 
 /** Add message to context
 	 * @param ctx FacesContext object
 	 * @param msgName Message name
 	 * @param msgDetail Message detail
 	 * @param severity Severity of message
 	 */
 	private void addMessage(FacesContext ctx, String msgName, String msgDetail, FacesMessage.Severity severity)
 	{
 		FacesMessage msg = new FacesMessage(msgName, msgDetail);
 		msg.setSeverity(severity);
 		ctx.addMessage(null, msg);
 	}
 	
 	/**
 	 * @return showInvalidModuleId module id of invalid module(used for popup)
 	 */
 	public int getShowInvalidModuleId() {
 		return showInvalidModuleId;
 	}
 
 	/**
 	 * @param showInvalidModuleId module id of invalid module
 	 */
 	public void setShowInvalidModuleId(int showInvalidModuleId) {
 		this.showInvalidModuleId = showInvalidModuleId;
 	}
 
 	/**
 	 * @return value of sec table(datatable in which sections are rendered)
 	 */
 	public UIData getSecTable()
 	{
 		return this.secTable;
 	}
 
 	/**
 	 * @param secTable
 	 *        section datatable to set
 	 */
 	public void setSecTable(UIData secTable)
 	{
 		this.secTable = secTable;
 	}
 
 }
