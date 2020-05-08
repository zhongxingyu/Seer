 /**********************************************************************************
  *
  * $URL$
  *
  ***********************************************************************************
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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.component.app.melete.*;
 
 import java.util.*;
 
 import javax.faces.component.*;
 import javax.faces.event.*;
 import java.io.Serializable;
 
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.application.FacesMessage;
 
 // import com.sun.faces.util.Util;
 
 import org.etudes.api.app.melete.ModuleObjService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.sakaiproject.util.ResourceLoader;
 
 /**
  * Gets the module beans for author
  *
  * @version 1.00 08 Mar 2005
  * @author Mallika M Thoppay
  */
 /*
  * @author Murthy Tanniru 08 Mar 2005 Fixed the bug #91 Section bread crumbs are not shown as links on edit module page Mallika - 1/9/07 - Adding
  * addContentAction method Mallika - 1/7/07 - Adding setmodule to addcontentaction Mallika - 2/13/07 - Adding the showTextOnly attributes Rashmi -
  * 3/6/07 - remove section breadcrumbs Mallika - 6/6/07 - Added multiple indent code Mallika - 6/26/07 - Added findIndent method
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
 
 	private String formName;
 
 	private Date currentDate;
 
 	private boolean selectedSection;
 
 	private boolean nomodsFlag;
 
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
 
 	public UIData getTable()
 	{
 		return table;
 	}
 
 	public void setTable(UIData table)
 	{
 		this.table = table;
 	}
 
 	public ListAuthModulesPage()
 	{
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		courseId = (String) sessionMap.get("courseId");
 		userId = (String) sessionMap.get("userId");
 		nomodsFlag = false;
 		setShowModuleId(-1);
 		count = 0;
 		selectedModIndex = -1;
 		moduleSelected = false;
 		selectedModIndices = null;
 		selectedSecModIndices = null;
 
 		selectedSecIndex = -1;
 		sectionSelected = false;
 		ValueBinding binding = Util.getBinding("#{authorPreferences}");
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
 	}
 
 	public void resetValues()
 	{
 		setShowModuleId(-1);
 		errModuleIds = null;
 		nomodsFlag = false;
 		count = 0;
 		selectedModIndex = -1;
 		moduleSelected = false;
 		selectedModIndices = null;
 		selectedSecModIndices = null;
 
 		selectedSecIndex = -1;
 		sectionSelected = false;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{deleteModulePage}");
 		DeleteModulePage dmPage = (DeleteModulePage) binding.getValue(ctx);
 		dmPage.setMdbean(null);
 		dmPage.setModuleSelected(false);
 		dmPage.setSection(null);
 		dmPage.setSectionSelected(false);
 		dmPage.setModules(null);
 		dmPage.setSectionBeans(null);
 
 		binding = Util.getBinding("#{authorPreferences}");
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
 	}
 
 	public boolean isAutonumber()
 	{
 		return autonumber;
 	};
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
 
 	public boolean getTrueFlag()
 	{
 		return trueFlag;
 	}
 
 	public void setTrueFlag(boolean trueFlag)
 	{
 		this.trueFlag = trueFlag;
 	}
 
 	public List getNullList()
 	{
 		return nullList;
 	}
 
 	public void setNullList(List nullList)
 	{
 		this.nullList = nullList;
 	}
 
 	/*
 	 * adding listener
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
 		moduleSelected = true;
 		return;
 	}	
 
 	public void resetSelectedLists()
 	{
 		selectedModIndices = null;
 		selectedSecModIndices = null;
 		selectAllFlag = false;
 	}
 
 	public List getModuleDateBeans()
 	{
 		resetSelectedLists();
 		setCurrentDate(Calendar.getInstance().getTime());
 		FacesContext context = FacesContext.getCurrentInstance();
 
 		boolean flagsReset = false;
 		try
 		{
 			ModuleService modServ = getModuleService();
 			moduleDateBeans = modServ.getModuleDateBeans(userId, courseId);
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
 		if (moduleDateBeans.size() == 0)
 		{
 			nomodsFlag = true;
 
 		}
 		else
 		{
 			nomodsFlag = false;
 		}
 		return moduleDateBeans;
 	}
 
 	public Date getCurrentDate()
 	{
 		return currentDate;
 	}
 
 	public void setCurrentDate(Date currentDate)
 	{
 		this.currentDate = currentDate;
 	}
 
 	public void setModuleDateBeans(List moduleDateBeansList)
 	{
 		moduleDateBeans = moduleDateBeansList;
 	}
 
 	public int getShowModuleId()
 	{
 		return this.showModuleId;
 	}
 
 	public void setShowModuleId(int moduleId)
 	{
 		this.showModuleId = moduleId;
 	}
 
 	public boolean getSelectedSection()
 	{
 		return selectedSection;
 	}
 
 	public void setSelectedSection(boolean selectedSection)
 	{
 		this.selectedSection = selectedSection;
 	}
 
 	public boolean getNomodsFlag()
 	{
 		return nomodsFlag;
 	}
 
 	public void setNomodsFlag(boolean nomodsFlag)
 	{
 		this.nomodsFlag = nomodsFlag;
 	}
 
 	public boolean getExpandAllFlag()
 	{
 		return expandAllFlag;
 	}
 
 	public void setExpandAllFlag(boolean expandAllFlag)
 	{
 		this.expandAllFlag = expandAllFlag;
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
 	
 	public String showAuthSections()
 	{
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UIViewRoot root = ctx.getViewRoot();
 		UIData table = (UIData) root.findComponent("listauthmodulesform").findComponent("table");
 		ModuleDateBean mdbean = (ModuleDateBean) table.getRowData();
 		ValueBinding binding = Util.getBinding("#{listAuthModulesPage}");
 		ListAuthModulesPage lamPage = (ListAuthModulesPage) binding.getValue(ctx);
 
 		lamPage.setShowModuleId(mdbean.getModuleId());
 
 		return "list_auth_modules";
 	}
 
 	public String hideAuthSections()
 	{
 		resetSelectedLists();
 		setShowModuleId(-1);
 		setExpandAllFlag(false);
 		return "list_auth_modules";
 	}
 
 	// Mallika - 6/6/06 - adding this method to expand all modules
 	public String expandAllAction()
 	{
 
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{listAuthModulesPage}");
 		ListAuthModulesPage lamPage = (ListAuthModulesPage) binding.getValue(ctx);
 
 		lamPage.setExpandAllFlag(true);
 
 		return "list_auth_modules";
 	}
 
 	public String collapseAllAction()
 	{
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{listAuthModulesPage}");
 		ListAuthModulesPage lamPage = (ListAuthModulesPage) binding.getValue(ctx);
 		lamPage.setExpandAllFlag(false);
 		lamPage.setShowModuleId(-1);
 
 		return "list_auth_modules";
 	}
 
 	// Mallika - new code end
 
 	/*
 	 * Revised by Rashmi to include module number Revised by Rashmi to point to editmodulesections.jsp page instead of edit_section nav rule.
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
 			if (moduleDateBeans != null)
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
 			if (moduleDateBeans != null)
 			{
 			ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selectedModIndex);
 			SectionBean secBean = (SectionBean) mdbean.getSectionBeans().get(selectedSecIndex);
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
 
 	/*
 	 * Revised by Rashmi on 1/21 to set module number to fix bug#211
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
 			ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selectedModIndex);
 
 			ValueBinding binding = Util.getBinding("#{addSectionPage}");
 			FacesContext context = FacesContext.getCurrentInstance();
 			AddSectionPage addPage = (AddSectionPage) binding.getValue(context);
 			addPage.setSection(null);
 			addPage.resetSectionValues();
 			addPage.setModule(mdbean.getModule());
 
 			Map sessionMap = context.getExternalContext().getSessionMap();
 			sessionMap.put("currModule", mdbean.getModule());
 			count = 0;
 			moduleSelected = false;
 			// Mallika -3/24/05
 			sectionSelected = false;
 			return "addmodulesections";
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
 		if (moduleSelected)
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
 				getModuleService().archiveModules(selModBeans,moduleDateBeans);
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
 
 	public String redirectToEditModule()
 	{
 		return "edit_module";
 	}
 
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
 
 	public String redirectToEditSection()
 	{
 		return "editmodulesections";
 	}
 
 	public void editSection(ActionEvent evt)
 	{
 		resetSelectedLists();
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		Map params = ctx.getExternalContext().getRequestParameterMap();
 	  	int selModIndex = Integer.parseInt((String) params.get("modidx"));
 	  	int selSecIndex = Integer.parseInt((String) params.get("secidx"));
 
 		ModuleDateBean mdbean = (ModuleDateBean) moduleDateBeans.get(selModIndex);
 		SectionBean secBean = (SectionBean) mdbean.getSectionBeans().get(selSecIndex);
 
 		ValueBinding binding = Util.getBinding("#{editSectionPage}");
 		EditSectionPage esPage = (EditSectionPage) binding.getValue(ctx);
 		Map sessionMap = ctx.getExternalContext().getSessionMap();
 		sessionMap.put("currModule", ((Section) secBean.getSection()).getModule());
 		esPage.setEditInfo((Section) secBean.getSection());
 	}
 
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
 			SectionBean secBean = null;
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
 				secBean = (SectionBean) mdbean.getSectionBeans().get((((Integer) smObj.getSecObj())).intValue());
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
 			  getModuleService().updateProperties(moduleDateBeans);
 
 
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
 
 	public String cancelChanges()
 	{
 		return "list_auth_modules";
 	}
 
 	public String viewModule()
 	{
 		return "view_module";
 	}
 
 	public String viewSection()
 	{
 		return "view_section";
 	}
 
 	public String viewPrereqs()
 	{
 		return "list_auth_modules";
 	}
 
 	/*
 	 * what's next revised by rashmi on Apr 7. add functionality for what's next. This method fetches the module and initialize moduleNextStepsPage
 	 * instance and navigate to module post steps page
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
 
 	/*
 	 * added by rashmi on 8 Apr returns a string whose value is null for render comparison on the page for + icon or view icon for next steps
 	 */
 	public String getIsNull()
 	{
 		return isNull;
 	}
 
 	private void resetSubSectionValues()
 	{
 		sectionSelected = false;
 		moduleSelected = false;
 		count = 0;
 		selectedSecModIndices = null;
 	}
 
 	private boolean findIndent(SectionBean indentBean, List secBeans)
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
 				SectionBean secBean = (SectionBean) i.next();
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
 
 	/*
 	 * added by rashmi - 4/10/07 On clicking Indent Right create subsections
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
 			SectionBean secBean = null;
 			List indentSecBeans = null;
 			if (indentSecBeans == null)
 			{
 				indentSecBeans = new ArrayList();
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
 					secBean = (SectionBean) mdbean.getSectionBeans().get(selIndex);
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
 						secBean = (SectionBean) mdbean.getSectionBeans().get((((Integer) smObj.getSecObj())).intValue());
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
 		return "list_auth_modules";
 	}
 
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
 			SectionBean secBean = null;
 			List indentSecBeans = null;
 			if (indentSecBeans == null)
 			{
 				indentSecBeans = new ArrayList();
 			}
 			// If one section is selected, we check if its the top level section
 			// If multiple sections are selected, we indent those that we can
 			// and leave the others alone
 			if (selectedSecModIndices.size() == 1)
 			{
 				smObj = (SecModObj) selectedSecModIndices.get(0);
 				mdbean = (ModuleDateBean) moduleDateBeans.get((((Integer) smObj.getModObj())).intValue());
 				secBean = (SectionBean) mdbean.getSectionBeans().get((((Integer) smObj.getSecObj())).intValue());
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
 						secBean = (SectionBean) mdbean.getSectionBeans().get((((Integer) smObj.getSecObj())).intValue());
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
 		return "list_auth_modules";
 	}
 
 	private boolean checkTopLevelSection(String dispSeq)
 	{
 		String pattern = "\\.";
 		int occurs = dispSeq.split(pattern).length - 1;
 		if (occurs == 1) return true;
 		return false;
 	}
 
 	// This method returns true of the user has selected sections in different modules
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
 
 	// indent code end
 
 	// sort code
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
 				SectionBean secBean = (SectionBean) mdbean.getSectionBeans().get(selIndex);
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
 				SectionBean secBean = (SectionBean) mdbean.getSectionBeans().get(selIndex);
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
 
 	// copy modules and sections
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
 
 		return "list_auth_modules";
 	}
 	// copy code end
 
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
 
 			if (sectionSelected)
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
 	// move sections code end
 
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
 
 
 	private void addMessage(FacesContext ctx, String msgName, String msgDetail, FacesMessage.Severity severity)
 	{
 		FacesMessage msg = new FacesMessage(msgName, msgDetail);
 		msg.setSeverity(severity);
 		ctx.addMessage(null, msg);
 	}
 
 
 
 	public UIData getSecTable()
 	{
 		return this.secTable;
 	}
 
 	public void setSecTable(UIData secTable)
 	{
 		this.secTable = secTable;
 	}
 
 }
