 /**********************************************************************************
  *
  * $URL: https://source.sakaiproject.org/contrib/etudes/melete/trunk/melete-app/src/java/org/etudes/tool/melete/SpecialAccessPage.java $
  * $Id: SpecialAccessPage.java 56408 2008-12-19 21:16:52Z mallika@etudes.org $
  ***********************************************************************************
  * Copyright (c) 2010 Etudes, Inc.
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
 import java.util.Comparator;
 import java.util.Collections;
 import java.util.ArrayList;
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
 	/** identifier field */
     private SpecialAccessObjService specialAccess;
 
 	private SpecialAccessService specialAccessService;
 
 	protected ModuleService moduleService;
     /** Dependency: The Melete Security service. */
 	protected MeleteSecurityService meleteSecurityService;
 	protected UserDirectoryService userDirectoryService = null;
 
 	private UIData table;
 	private List saList;
 	private ModuleObjService module;
 	private Date startDate, endDate;
 	boolean selectAllFlag;
 	private boolean noAccFlag;
 	
 	private List deleteAccessIds;
 	private String deleteAccessTitles;
 	
 	int count;
 
 	int selectedAccIndex;
 
 	private List selectedAccIndices = null;
 
 	boolean accessSelected;
 	int listSize;
 
 
 	/** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(SpecialAccessPage.class);
 
 	public SpecialAccessPage()
 	{
       noAccFlag = true;
 	}
 	
 	public UIData getTable()
 	{
 		return table;
 	}
 
 	public void setTable(UIData table)
 	{
 		this.table = table;
 	}	
 	
 	public void selectAllAccess(ValueChangeEvent event) throws AbortProcessingException
 	{
 		selectAllFlag= true;
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
 
 	
 	
 
     public SpecialAccessObjService getSpecialAccess() {
    	  FacesContext context = FacesContext.getCurrentInstance();
   	  Map sessionMap = context.getExternalContext().getSessionMap();
   	 if (specialAccess == null)
             {
           	  specialAccess = new SpecialAccess();
           	  specialAccess.setAccessId(0);
           	  specialAccess.setModuleId(this.module.getModuleId().intValue());
           	  specialAccess.setStartDate(this.module.getModuleshdate().getStartDate());
           	  specialAccess.setEndDate(this.module.getModuleshdate().getEndDate());
             }
   	  return specialAccess;
       }
     
 	public String addSpecialAccess()
 	{
 	  FacesContext context = FacesContext.getCurrentInstance();
 	  Map sessionMap = context.getExternalContext().getSessionMap();
 	  ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 	  //SpecialAccess specialAccess = new SpecialAccess();
 
 	  if (specialAccessService == null)
 	    specialAccessService = getSpecialAccessService();
 
 	    try
 	    {
 	      specialAccessService.insertSpecialAccess(this.saList, this.specialAccess, this.module);
 	    }catch(Exception ex)
 		{
 	    	String errMsg = bundle.getString(ex.getMessage());
 	    	context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage(),errMsg));
 			return "failure";
 		}
 	    resetValues();
 	   return "list_special_access";
 
 	}
 
      public String addAccessAction()
      {
     	 this.specialAccess = null;
     	 return "add_special_access";
      }
      
      public String redirectToEditSpecialAccess()
  	{
  		return "add_special_access";
  	}
 
  	public void editSpecialAccess(ActionEvent evt)
  	{     
  		FacesContext ctx = FacesContext.getCurrentInstance();
 		Map params = ctx.getExternalContext().getRequestParameterMap();
  		int selAccIndex = Integer.parseInt((String) params.get("accidx"));
  		setSpecialAccess((SpecialAccess) this.saList.get(selAccIndex));
 
  	}
  	
      public String deleteAction()
  	{
  		FacesContext ctx = FacesContext.getCurrentInstance();
  		List delAccs = null;
  		count = 0;
  	
  		// added by rashmi
  		if (!accessSelected)
  		{
  			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
  			String msg = bundle.getString("select_one_delete");
  			addMessage(ctx, "Select  One", msg, FacesMessage.SEVERITY_ERROR);
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
 				accTitlesBuf.append(", ");
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
 
 
 
 	public String deleteSpecialAccess()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		 ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		try
 	    {
 		  specialAccessService.deleteSpecialAccess(this.deleteAccessIds);
 	    }catch(Exception ex)
 		{
 	    	String errMsg = bundle.getString(ex.getMessage());
 	    	context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage(),errMsg));
 			return "failure";
 		}
 	    resetValues();
 	    return "list_special_access";
 	}
 
 	public String cancelDeleteAccess()
   	{
   		return "list_special_access";
   	}
 	
 	public String cancel()
   	{
 		resetValues();
   		return "list_special_access";
   	}

 
 
 	public void resetValues()
 	{
        saList = null;
        noAccFlag = true;
    	}
 
 
    public void setSpecialAccess(SpecialAccessObjService specialAccess) {
     this.specialAccess = specialAccess;
   }
 
 
    public List getSaList()
    {
 	   if (saList == null)
 	   {
 		  saList = specialAccessService.getSpecialAccess(this.module.getModuleId().intValue());
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
 
    /*Takes in a colon delimited string of user Ids, Returns a formatted string of 
     * user names and eids
     */
    public String generateUserNames(String users)
    {
 	   StringBuffer userNameBuf = new StringBuffer();	
 	   String[] userIds = SqlHelper.decodeStringArray(users);
 	   String label;
 	   User user;
 	   for (String userId:userIds)
 	   {
 			try
 			{
 				user = org.sakaiproject.user.cover.UserDirectoryService.getUser(userId);
 			    label = user.getSortName()+" ("+user.getEid()+")<br>";
 			    userNameBuf.append(label);
 			}
 			catch (UserNotDefinedException e)
 			{
 				logger.warn("User not found while listing special access for "+userId);
 			}
 		}
 	   return userNameBuf.toString();
    }
    
    
    public void setSaList(List saList)
    {
 	   this.saList = saList;
    }
 
    public List<String> getUsers()
 	{
 	   if (this.specialAccess != null && this.specialAccess.getUsers() != null)  return Arrays.asList(SqlHelper.decodeStringArray(this.specialAccess.getUsers()));
 	   else return null;
 	}   
    
    public List<String> getUsersList()
 	{
 	   FacesContext context = FacesContext.getCurrentInstance();
 	    Map sessionMap = context.getExternalContext().getSessionMap();
 	    String courseId = (String)sessionMap.get("courseId");
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
    
    /*
 	 * converts the coursemodule list to selectItems for displaying at
 	 * the list boxes in the JSF page
 	 */
 	private List forSelectItemsList(List list)
 	{
 		List selectList = new ArrayList();
 		// Adding available list to select box
 		if(list == null || list.size()==0)
 		{
 			selectList.add(new SelectItem("0", "No Items"));
 			return selectList;
 		}
 
 		Iterator itr = list.iterator();
 		while (itr.hasNext()) {
 			User  user = (User) itr.next();
 			String value = user.getId();
 			String label = user.getSortName()+" ("+user.getEid()+")";
 			selectList.add(new SelectItem(value, label));
 		}
 
 		return selectList;
 	}
 	
 	private void addMessage(FacesContext ctx, String msgName, String msgDetail, FacesMessage.Severity severity)
 	{
 		FacesMessage msg = new FacesMessage(msgName, msgDetail);
 		msg.setSeverity(severity);
 		ctx.addMessage(null, msg);
 	}	
 	
 	public void setUsers(List<String> users)
 	{
 		this.specialAccess.setUsers(SqlHelper.encodeStringArray(users.toArray(new String[users.size()])));
 	}
 	
 	public void resetSelectedLists()
 	{
 		selectedAccIndices = null;
 		selectAllFlag = false;
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
 	
 	public ModuleObjService getModule()
 	{
 	  return this.module;	
 	}
 	
 	public void setModule(ModuleObjService module)
 	{
 		  this.module = module;
 	}	
  
 	public Date getStartDate()
 	{
 		return this.module.getModuleshdate().getStartDate();
 	}
 	
 	public void setStartDate(Date startDate)
 	{
 		this.startDate = startDate;
 	}
 	
 	public Date getEndDate()
 	{
 		return this.module.getModuleshdate().getEndDate();
 	}
 	
 	public void setEndDate(Date endDate)
 	{
 		this.endDate = endDate;
 	}	
 	
 	public List getDeleteAccessIds() {
 	      return deleteAccessIds;
 	}
 
     public void setDeleteAccessIds(List deleteAccessIds) {
 	     this.deleteAccessIds = deleteAccessIds;
 	 }	
     
     public String getDeleteAccessTitles() {
 	      return deleteAccessTitles;
 	}
 
     public void setDeleteAccessTitles(String deleteAccessTitles) {
 	     this.deleteAccessTitles = deleteAccessTitles;
 	 }	
     
     /**
 	 * @return the noAccFlag
 	 */
 	public boolean isNoAccFlag() {
 		return noAccFlag;
 	}
 
 	/**
 	 * @param noAccFlag the noAccFlag to set
 	 */
 	public void setNoAccFlag(boolean noAccFlag) {
 		this.noAccFlag = noAccFlag;
 	}    
 	/**
 	 * @return Returns the SpecialAccessService.
 	 */
 	public SpecialAccessService getSpecialAccessService()
 	{
 		return specialAccessService;
 	}
 
 	/**
 	 * @param specialAccessService The SpecialAccessService to set.
 	 */
 	public void setSpecialAccessService(SpecialAccessService specialAccessService)
 	{
 		this.specialAccessService = specialAccessService;
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
 	 * @param meleteSecurityService The meleteSecurityService to set.
 	 */
 	public void setMeleteSecurityService(
 			MeleteSecurityService meleteSecurityService) {
 		this.meleteSecurityService = meleteSecurityService;
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
 	
 }
