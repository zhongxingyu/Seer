 /**********************************************************************************
  *
  * $URL: https://source.sakaiproject.org/contrib/etudes/melete/trunk/melete-app/src/java/org/etudes/tool/melete/SpecialAccessPage.java $
  * $Id: SpecialAccessPage.java 56408 2008-12-19 21:16:52Z mallika@etudes.org $
  ***********************************************************************************
  * Copyright (c) 2010, 2011 Etudes, Inc.
  *
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
 import org.etudes.api.app.melete.*;
 import org.sakaiproject.util.ResourceLoader;
 
 import javax.faces.application.Application;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.html.*;
 import javax.faces.component.*;
 
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.Collections;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.Date;
 import java.io.*;
 
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.event.ActionEvent;
 import javax.faces.model.SelectItem;
 
 import javax.faces.context.ExternalContext;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 
 import org.etudes.api.app.melete.SpecialAccessService;
 import org.etudes.api.app.melete.SpecialAccessObjService;
 import org.etudes.api.app.melete.MeleteSecurityService;
 import org.etudes.util.SqlHelper;
 import org.sakaiproject.user.api.UserDirectoryService;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserNotDefinedException;
 
 public class SpecialAccessPage implements Serializable
 {
 	private List deleteAccessIds;
 
 	private String deleteAccessTitles;
 
 	private ModuleObjService module;
 	private int moduleId;
 	private boolean noAccFlag;
 	private List saList;
 
 	private List selectedAccIndices = null;
 	private int showInvalidAccessId;
 	/** identifier field */
 	private SpecialAccessObjService specialAccess;
 	private SpecialAccessService specialAccessService;
 	private Date startDate, endDate;
 	private UIData table;
 
 	/** Dependency: The logging service. */
 	protected Log logger = LogFactory.getLog(SpecialAccessPage.class);
 	/** Dependency: The Melete Security service. */
 	protected MeleteSecurityService meleteSecurityService;
 	protected ModuleService moduleService;
 
 	protected UserDirectoryService userDirectoryService = null;
 
 	boolean accessSelected;
 
 	int count;
 
 	int listSize;
 	boolean selectAllFlag;
 
 	int selectedAccIndex;
 
 	public SpecialAccessPage()
 	{
 		noAccFlag = true;
 	}
 
 	/**
 	 * @return add_special_access
 	 */
 	public String addAccessAction()
 	{
 		this.specialAccess = null;
 		return "add_special_access";
 	}
 
 	/**
 	 * Add special access entry after validating dates
 	 * 
 	 * @return if operation fails "failure", otherwise "list_special_access"
 	 */
 	public String addSpecialAccess()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		// SpecialAccess specialAccess = new SpecialAccess();
 
 		if (specialAccessService == null) specialAccessService = getSpecialAccessService();
 
 		Date st = this.specialAccess.getStartDate();
 		Date end = this.specialAccess.getEndDate();
 
 		boolean dateResult = validateDates(context, bundle, st, end);
 		if (dateResult == false) return "failure";
 
 		if (getUsers() != null)
 		{
 			try
 			{
 				specialAccessService.insertSpecialAccess(this.saList, this.specialAccess, getModule());
 			}
 			catch (Exception ex)
 			{
 				String errMsg = bundle.getString(ex.getMessage());
 				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), errMsg));
 				return "failure";
 			}
 		}
 		resetValues();
 		return "list_special_access";
 
 	}
 
 	/**
 	 * @return list_special_access page
 	 */
 	public String cancel()
 	{
 		resetValues();
 		return "list_special_access";
 	}
 
 	/**
 	 * @return list_special_access page
 	 */
 	public String cancelDeleteAccess()
 	{
 		resetValues();
 		return "list_special_access";
 	}
 
 	/**
 	 * Delete one or more special access entries(goes to confirm page)
 	 * 
 	 * @return delete_special_access page
 	 */
 	public String deleteAction()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		List delAccs = null;
 		count = 0;
 
 		// added by rashmi
 		if (!accessSelected)
 		{
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String msg = bundle.getString("select_one_sa_delete");
 			addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
 			count = 0;
 			resetSelectedLists();
 			return "list_special_access";
 		}
 		// add end
 		// access selected
 		if (accessSelected)
 		{
 			SpecialAccess sa = null;
 			if (delAccs == null)
 			{
 				delAccs = new ArrayList();
 			}
 			if (selectedAccIndices != null)
 			{
 				StringBuffer accTitlesBuf = new StringBuffer();
 				for (ListIterator i = selectedAccIndices.listIterator(); i.hasNext();)
 				{
 					int saId = ((Integer) i.next()).intValue();
 					sa = (SpecialAccess) saList.get(saId);
 					delAccs.add(sa.getAccessId());
 					accTitlesBuf.append(generateUserNames(sa.getUsers()));
 				}
 				setDeleteAccessIds(delAccs);
				accTitlesBuf.delete(accTitlesBuf.toString().length() - 2, accTitlesBuf.toString().length());
 				setDeleteAccessTitles(accTitlesBuf.toString());
 			}
 		}
 
 		count = 0;
 		resetSelectedLists();
 		return "delete_special_access";
 	}
 
 	/**
 	 * Deletes special access entries from database
 	 * 
 	 * @return list_special_access page
 	 */
 	public String deleteSpecialAccess()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		try
 		{
 			specialAccessService.deleteSpecialAccess(this.deleteAccessIds);
 		}
 		catch (Exception ex)
 		{
 			String errMsg = bundle.getString(ex.getMessage());
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), errMsg));
 			return "failure";
 		}
 		resetValues();
 		return "list_special_access";
 	}
 
 	/**
 	 * Set edit special access page to entry that has been selected
 	 * 
 	 * @param evt
 	 *        ActionEvent
 	 */
 	public void editSpecialAccess(ActionEvent evt)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		Map params = ctx.getExternalContext().getRequestParameterMap();
 		int selAccIndex = Integer.parseInt((String) params.get("accidx"));
 		setSpecialAccess((SpecialAccess) this.saList.get(selAccIndex));
 
 	}
 
 	/**
 	 * Takes in a colon delimited string of user Ids, Returns a formatted string of user names and eids
 	 * 
 	 * @param users
 	 *        colon delimited string of user ids
 	 * @return Formatted string of user names and eids
 	 */
 	public String generateUserNames(String users)
 	{
 		StringBuffer userNameBuf = new StringBuffer();
 		String[] userIds = SqlHelper.decodeStringArray(users);
 		String label;
 		User user;
 		for (String userId : userIds)
 		{
 			try
 			{
 				user = org.sakaiproject.user.cover.UserDirectoryService.getUser(userId);
 				label = user.getSortName() + " (" + user.getEid() + ")<br>";
 				userNameBuf.append(label);
 			}
 			catch (UserNotDefinedException e)
 			{
 				logger.warn("User not found while listing special access for " + userId);
 			}
 		}
 		return userNameBuf.toString();
 	}
 
 	/**
 	 * @return list of special access ids to delete
 	 */
 	public List getDeleteAccessIds()
 	{
 		return deleteAccessIds;
 	}
 
 	/**
 	 * @return titles of special access entries to delete
 	 */
 	public String getDeleteAccessTitles()
 	{
 		return deleteAccessTitles;
 	}
 
 	/**
 	 * @return end date of current module
 	 */
 	public Date getEndDate()
 	{
 		return getModule().getModuleshdate().getEndDate();
 	}
 
 	/**
 	 * @return size of list
 	 */
 	public int getListSize()
 	{
 		return listSize;
 	}
 
 	/**
 	 * @return moduleId
 	 */
 	public int getModuleId()
 	{
 		return moduleId;
 	}
 
 	/**
 	 * @return current module object
 	 */
 	public ModuleObjService getModule()
 	{
 		return this.moduleService.getModule(this.moduleId);
 	}
 
 	/**
 	 * @return Returns the ModuleService.
 	 */
 	public ModuleService getModuleService()
 	{
 		return moduleService;
 	}
 
 	/**
 	 * Returns special access list with the user names formatted
 	 * 
 	 * @return special access list
 	 */
 	public List getSaList()
 	{
 		if (saList == null)
 		{
 			saList = specialAccessService.getSpecialAccess(this.moduleId);
 			listSize = saList.size();
 
 			StringBuffer userNameBuf = new StringBuffer();
 			if (saList.size() > 0)
 			{
 				noAccFlag = false;
 				for (ListIterator i = saList.listIterator(); i.hasNext();)
 				{
 					SpecialAccess saObj = (SpecialAccess) i.next();
 					saObj.setUserNames(generateUserNames(saObj.getUsers()));
 				}
 			}
 			else
 			{
 				noAccFlag = true;
 			}
 		}
 		return saList;
 	}
 
 	/**
 	 * @return value of selectAllFlag
 	 */
 	public boolean getSelectAllFlag()
 	{
 		return selectAllFlag;
 	}
 
 	/**
 	 * @return invalid access id
 	 */
 	public int getShowInvalidAccessId()
 	{
 		return showInvalidAccessId;
 	}
 
 	/**
 	 * Create new special access entry with the current module id, dates and override flags set to false
 	 * 
 	 * @return specialAccess specialAccessObjService object
 	 */
 	public SpecialAccessObjService getSpecialAccess()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		if (specialAccess == null)
 		{
 			specialAccess = new SpecialAccess();
 			specialAccess.setAccessId(0);
 			specialAccess.setModuleId(this.moduleId);
 			specialAccess.setStartDate(getModule().getModuleshdate().getStartDate());
 			specialAccess.setEndDate(getModule().getModuleshdate().getEndDate());
 			specialAccess.setOverrideStart(false);
 			specialAccess.setOverrideEnd(false);
 		}
 		return specialAccess;
 	}
 
 	/**
 	 * @return Returns the SpecialAccessService.
 	 */
 	public SpecialAccessService getSpecialAccessService()
 	{
 		return specialAccessService;
 	}
 
 	/**
 	 * @return start date of current module
 	 */
 	public Date getStartDate()
 	{
 		return getModule().getModuleshdate().getStartDate();
 	}
 
 	/**
 	 * @return table special access table on the page
 	 */
 	public UIData getTable()
 	{
 		return table;
 	}
 
 	/**
 	 * Returns special access users as a list
 	 * 
 	 * @return list of users
 	 */
 	public List<String> getUsers()
 	{
 		if (this.specialAccess != null && this.specialAccess.getUsers() != null)
 			return Arrays.asList(SqlHelper.decodeStringArray(this.specialAccess.getUsers()));
 		else
 			return null;
 	}
 
 	/**
 	 * Processes special access user list and returns in a select list
 	 * 
 	 * @return select list of users
 	 */
 	public List<String> getUsersList()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 		MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 		String courseId = mPage.getCurrentSiteId();
 
 		// get the ids
 		Set<String> ids = this.meleteSecurityService.getUsersIsAllowed(courseId);
 
 		// turn into users
 		List<User> users = this.userDirectoryService.getUsers(ids);
 
 		// sort - by user sort name
 		Collections.sort(users, new Comparator()
 		{
 			public int compare(Object arg0, Object arg1)
 			{
 				int rv = ((User) arg0).getSortName().compareTo(((User) arg1).getSortName());
 				return rv;
 			}
 		});
 
 		return forSelectItemsList(users);
 	}
 
 	/**
 	 * @return list_special_access
 	 */
 	public String hideInvalid()
 	{
 		setShowInvalidAccessId(-1);
 		return "list_special_access";
 	}
 
 	/**
 	 * @return the noAccFlag
 	 */
 	public boolean isNoAccFlag()
 	{
 		return noAccFlag;
 	}
 
 	/**
 	 * @return add_special_access
 	 */
 	public String redirectToEditSpecialAccess()
 	{
 		return "add_special_access";
 	}
 
 	/**
 	 * Reset selected lists
 	 * 
 	 */
 	public void resetSelectedLists()
 	{
 		selectedAccIndices = null;
 		selectAllFlag = false;
 	}
 
 	/**
 	 * Reset special access page values
 	 * 
 	 */
 	public void resetValues()
 	{
 		saList = null;
 		noAccFlag = true;
 		accessSelected = false;
 		deleteAccessIds = null;
 		deleteAccessTitles = null;
 		setShowInvalidAccessId(-1);
 	}
 
 	/**
 	 * Reset values on list auth page and redirect user
 	 * 
 	 * @return list_auth_modules page
 	 */
 	public String returnAction()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{listAuthModulesPage}");
 		ListAuthModulesPage lamPage = (ListAuthModulesPage) binding.getValue(context);
 		lamPage.resetValues();
 		return "list_auth_modules";
 	}
 
 	/**
 	 * Select all access entries
 	 * 
 	 * @param event
 	 *        ValueChangeEvent object
 	 * @throws AbortProcessingException
 	 */
 	public void selectAllAccess(ValueChangeEvent event) throws AbortProcessingException
 	{
 		selectAllFlag = true;
 		int k = 0;
 		if (selectedAccIndices == null)
 		{
 			selectedAccIndices = new ArrayList();
 		}
 		for (ListIterator i = saList.listIterator(); i.hasNext();)
 		{
 			SpecialAccess sa = (SpecialAccess) i.next();
 			sa.setSelected(true);
 			selectedAccIndices.add(new Integer(k));
 			k++;
 		}
 		count = saList.size();
 		if (count == 1) selectedAccIndex = 0;
 		accessSelected = true;
 		return;
 	}
 
 	/**
 	 * Sets selectedAccIndex and adds to selectedAddIndices when a special access entry is selected
 	 * 
 	 * @param event
 	 *        ValueChangeEvent object
 	 * @throws AbortProcessingException
 	 */
 	public void selectedAccess(ValueChangeEvent event) throws AbortProcessingException
 	{
 		if (selectAllFlag == false)
 		{
 
 			FacesContext context = FacesContext.getCurrentInstance();
 			UIInput acc_Selected = (UIInput) event.getComponent();
 			if (((Boolean) acc_Selected.getValue()).booleanValue() == true)
 				count++;
 			else
 				count--;
 
 			String selclientId = acc_Selected.getClientId(context);
 			if (logger.isDebugEnabled()) logger.debug("Sel client ID is " + selclientId);
 			selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 			selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 			String accessId = selclientId.substring(0, selclientId.indexOf(':'));
 
 			selectedAccIndex = Integer.parseInt(accessId);
 			if (selectedAccIndices == null)
 			{
 				selectedAccIndices = new ArrayList();
 			}
 			selectedAccIndices.add(new Integer(selectedAccIndex));
 			accessSelected = true;
 		}
 		return;
 	}
 
 	/**
 	 * @param deleteAccessIds
 	 *        special access ids to delete
 	 */
 	public void setDeleteAccessIds(List deleteAccessIds)
 	{
 		this.deleteAccessIds = deleteAccessIds;
 	}
 
 	/**
 	 * @param deleteAccessTitles
 	 *        titles of special access entries to delete
 	 */
 	public void setDeleteAccessTitles(String deleteAccessTitles)
 	{
 		this.deleteAccessTitles = deleteAccessTitles;
 	}
 
 	/**
 	 * @param endDate
 	 *        end date of current module
 	 */
 	public void setEndDate(Date endDate)
 	{
 		this.endDate = endDate;
 	}
 
 	/**
 	 * @param listSize
 	 *        size of list
 	 */
 	public void setListSize(int listSize)
 	{
 		this.listSize = listSize;
 	}
 
 	/**
 	 * @param moduleId
 	 *        module id
 	 */
 	public void setModuleId(int moduleId)
 	{
 		this.moduleId = moduleId;
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
 	 * @param ModuleService
 	 *        The ModuleService to set.
 	 */
 	public void setModuleService(ModuleService moduleService)
 	{
 		this.moduleService = moduleService;
 	}
 
 	/**
 	 * @param noAccFlag
 	 *        the noAccFlag to set
 	 */
 	public void setNoAccFlag(boolean noAccFlag)
 	{
 		this.noAccFlag = noAccFlag;
 	}
 
 	/**
 	 * @param saList
 	 *        special access list
 	 */
 	public void setSaList(List saList)
 	{
 		this.saList = saList;
 	}
 
 	/**
 	 * @param selectAllFlag
 	 *        value of select all flag
 	 */
 	public void setSelectAllFlag(boolean selectAllFlag)
 	{
 		this.selectAllFlag = selectAllFlag;
 	}
 
 	/**
 	 * @param showInvalidAccessId
 	 *        invalid access id
 	 */
 	public void setShowInvalidAccessId(int showInvalidAccessId)
 	{
 		this.showInvalidAccessId = showInvalidAccessId;
 	}
 
 	/**
 	 * @param specialAccess
 	 *        specialAccessObjService object
 	 */
 	public void setSpecialAccess(SpecialAccessObjService specialAccess)
 	{
 		this.specialAccess = specialAccess;
 	}
 
 	/**
 	 * @param specialAccessService
 	 *        The SpecialAccessService to set.
 	 */
 	public void setSpecialAccessService(SpecialAccessService specialAccessService)
 	{
 		this.specialAccessService = specialAccessService;
 	}
 
 	/**
 	 * @param startDate
 	 *        start date of current module
 	 */
 	public void setStartDate(Date startDate)
 	{
 		this.startDate = startDate;
 	}
 
 	/**
 	 * @param table
 	 */
 	public void setTable(UIData table)
 	{
 		this.table = table;
 	}
 
 	/**
 	 * Dependency: UserDirectoryService.
 	 * 
 	 * @param service
 	 *        The UserDirectoryService.
 	 */
 	public void setUserDirectoryService(UserDirectoryService service)
 	{
 		userDirectoryService = service;
 	}
 
 	/**
 	 * Sets special access object's users to list of users
 	 * 
 	 * @param users
 	 *        list of users
 	 */
 	public void setUsers(List<String> users)
 	{
 		this.specialAccess.setUsers(SqlHelper.encodeStringArray(users.toArray(new String[users.size()])));
 	}
 
 	/**
 	 * Sets showInvalidAccessId to set invalid popup on list page
 	 * 
 	 * @return list_special_access
 	 */
 	public String showHideInvalid()
 	{
 		ModuleDateBean mdbean = null;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UIViewRoot root = ctx.getViewRoot();
 		UIData table = null;
 		table = (UIData) root.findComponent("listspecialaccessform").findComponent("table");
 
 		SpecialAccess saObj = (SpecialAccess) table.getRowData();
 		if (getShowInvalidAccessId() != saObj.getAccessId())
 		{
 			setShowInvalidAccessId(saObj.getAccessId());
 		}
 
 		return "list_special_access";
 	}
 
 	/**
 	 * Adds message to faces context
 	 * 
 	 * @param ctx
 	 *        FacesContext object
 	 * @param msgName
 	 *        Message name
 	 * @param msgDetail
 	 *        Message detail
 	 * @param severity
 	 *        Severity of message
 	 */
 	private void addMessage(FacesContext ctx, String msgName, String msgDetail, FacesMessage.Severity severity)
 	{
 		FacesMessage msg = new FacesMessage(msgName, msgDetail);
 		msg.setSeverity(severity);
 		ctx.addMessage(null, msg);
 	}
 
 	/**
 	 * converts the user list to selectItems for displaying at the list boxes in the JSF page
 	 * 
 	 * @param list
 	 *        list of users
 	 * @return list of select items
 	 */
 	private List forSelectItemsList(List list)
 	{
 		List selectList = new ArrayList();
 		// Adding available list to select box
 		if (list == null || list.size() == 0)
 		{
 			selectList.add(new SelectItem("0", "No Items"));
 			return selectList;
 		}
 
 		Iterator itr = list.iterator();
 		while (itr.hasNext())
 		{
 			User user = (User) itr.next();
 			String value = user.getId();
 			String label = user.getSortName() + " (" + user.getEid() + ")";
 			selectList.add(new SelectItem(value, label));
 		}
 
 		return selectList;
 	}
 
 	/**
 	 * Ensure that the dates do not have a year after 9999 and the end date is after the start date
 	 * 
 	 * @param context
 	 *        FacesContext object
 	 * @param bundle
 	 *        ResourceLoader object
 	 * @param st
 	 *        Start date
 	 * @param end
 	 *        End date
 	 * @return true if dates are valid, false otherwise
 	 */
 	private boolean validateDates(FacesContext context, ResourceLoader bundle, Date st, Date end)
 	{
 		Calendar calstart = new GregorianCalendar();
 		Calendar calend = new GregorianCalendar();
 
 		boolean errorFlag = false;
 		if ((st != null) || (end != null))
 		{
 			if (st != null)
 			{
 				calstart.setTime(st);
 				if (calstart.get(Calendar.YEAR) > 9999)
 				{
 					String errMsg = bundle.getString("year_toobig_error");
 					addMessage(context, "Error Message", errMsg, FacesMessage.SEVERITY_ERROR);
 					errorFlag = true;
 				}
 			}
 			if (end != null)
 			{
 				calend.setTime(end);
 				if (calend.get(Calendar.YEAR) > 9999)
 				{
 					String errMsg = bundle.getString("year_toobig_error");
 					addMessage(context, "Error Message", errMsg, FacesMessage.SEVERITY_ERROR);
 					errorFlag = true;
 				}
 			}
 
 			// validation no 4 b
 			if ((end != null) && (st != null))
 			{
 				if (end.compareTo(st) <= 0)
 				{
 					String errMsg = "";
 					errMsg = bundle.getString("end_date_before_start");
 					addMessage(context, "Error Message", errMsg, FacesMessage.SEVERITY_ERROR);
 					errorFlag = true;
 				}
 			}
 		}
 		// If there is an error, validation fails and the method returns false
 		// If there are no errors, validation passes and the method returns true;
 		if (errorFlag == true) return false;
 		return true;
 	}
 
 }
