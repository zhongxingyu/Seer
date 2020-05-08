 /**********************************************************************************
  *
  * $URL$
  * $Id$  
  ***********************************************************************************
  *
  * Copyright (c) 2008 Etudes, Inc.
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
 import org.etudes.api.app.melete.*;
 
 import javax.faces.component.*;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.io.Serializable;
 
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.application.FacesMessage;
 import javax.faces.model.ListDataModel;
 
 import org.sakaiproject.util.ResourceLoader;
 //import com.sun.faces.util.Util;
 import java.sql.Timestamp;
 import org.etudes.api.app.melete.ModuleService;
 import org.sakaiproject.authz.cover.AuthzGroupService;
 import org.sakaiproject.authz.api.AuthzGroup;
 import javax.faces.event.*;
 import org.sakaiproject.tool.cover.ToolManager;
 /**
  * @author Faculty
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 /*
  * Mallika - 2/8/07 - consolidated view section pages into one
  */
 public class ListModulesPage implements Serializable{
 	/** Dependency:  The logging service. */
 	  protected Log logger = LogFactory.getLog(ListModulesPage.class);
 	  private List viewModuleBeans = null;
 	  /** identifier field */
       private int showModuleId;
 
       private String formName;
       private String role;
       private boolean instRole;
       private String typeEditor;
       private String typeLink;
       private String typeUpload;
       private boolean nomodsFlag;
       private boolean expandAllFlag;
       private boolean closedModulesFlag;
       private boolean trueFlag = true;
 
       private ModuleService moduleService;
 
       private Section nullSection = null;
       private List nullList = null;
       private String isNull = null;
       private Date nullDate = null;
       private Integer printModuleId =null;
       private Boolean printMaterial = null;
       private Boolean autonumberMaterial = null;
       private boolean printable;
       private boolean autonumber;
 
 	  //This needs to be set later using Utils.getBinding
 	  String courseId;
 	  String userId;
 
 	  private UIData modTable;
 	  private UIData secTable;
 	  private ListDataModel modDataModel;
 
 
 	  public ListDataModel getModDataModel()
 	{
 		this.modDataModel = new ListDataModel(getViewModuleBeans());
 		return this.modDataModel;
 	}
 
 	public void setModDataModel(ListDataModel modDataModel)
 	{
 		this.modDataModel = modDataModel;
 	}
 
 	public ListModulesPage(){
 
 	  	FacesContext context = FacesContext.getCurrentInstance();
 //	  	context.getViewRoot().setTransient(true);
 	  	Map sessionMap = context.getExternalContext().getSessionMap();
 	  	role = (String)sessionMap.get("role");
 	  	courseId = null;
 	  	userId = null;
 	  	nomodsFlag = false;
 	  	closedModulesFlag = false;
 	  	setShowModuleId(-1);
 	  	if (getRole()!= null)
 		{
 	  		 ValueBinding binding = Util.getBinding("#{authorPreferences}");
 	 		AuthorPreferencePage preferencePage = (AuthorPreferencePage)binding.getValue(context);
 	 		String expFlag = preferencePage.getUserView();
 	 		if (expFlag.equals("true"))
 	 		{
 	 	      expandAllFlag = true;
 	 		}
 	 		else
 	 		{
 	 		  expandAllFlag = false;
 	 		}
 		}
 	  	else
 	  	{
 
 	  	  expandAllFlag = true;
 	  	}
 	  }
 
 	  public void resetValues()
 	  {
 	  	nomodsFlag = false;
 	  	closedModulesFlag = false;
 	  	printMaterial = null;
 	  	autonumberMaterial = null;
 	  	FacesContext context = FacesContext.getCurrentInstance();
 //	  	context.getViewRoot().setTransient(true);
 		if (getRole()!= null)
 		{
 	  		 ValueBinding binding = Util.getBinding("#{authorPreferences}");
 	 		AuthorPreferencePage preferencePage = (AuthorPreferencePage)binding.getValue(context);
 	 		String expFlag = preferencePage.getUserView();
 
 	 		if (expFlag.equals("true"))
 	 		{
 	 	      expandAllFlag = true;
 	 		}
 	 		else
 	 		{
 	 		  expandAllFlag = false;
 	 		}
 		}
 	  	else
 	  	{
 	  	  expandAllFlag = true;
 	  	}
 	  	setShowModuleId(-1);
 	  }
 
 	  public boolean getInstRole()
 	    {
 	    	FacesContext context = FacesContext.getCurrentInstance();
 		  	Map sessionMap = context.getExternalContext().getSessionMap();
 		  	if ((String)sessionMap.get("role") !=null)
 		  		this.instRole = ((String)sessionMap.get("role")).equals("INSTRUCTOR");
 		  	else this.instRole = false;
 	    	return instRole;
 	    }
 
 	    public void setInstRole(boolean instRole)
 	    {
 	    	this.instRole = instRole;
 	    }
 
  /**
 	   * @return Returns the ModuleService.
 	   */
 	  public ModuleService getModuleService() {
 		return moduleService;
 	  }
 
 	 /**
 	  * @param moduleService The moduleService to set.
 	  */
 	  public void setModuleService(ModuleService moduleService) {
 		this.moduleService = moduleService;
 	  }
 
 
 	  public String getRole() {
 	  	return role;
 	  }
 
 	  public void setRole(String role) {
 	  	this.role = role;
 	  }
 
 	 
 	  public List  getNullList() {
 	  	return nullList;
 	  }
 
 	  public void setNullList(List nullList) {
 	  	this.nullList = nullList;
 	  }
 
       public boolean  getTrueFlag() {
 	  	return trueFlag;
 	  }
 
 	  public void setTrueFlag(boolean  trueFlag) {
 	  	this.trueFlag = trueFlag;
       }
 
 	  public boolean getNomodsFlag() {
 	  	return nomodsFlag;
 	  }
 
 	  public void setNomodsFlag(boolean nomodsFlag) {
 	  	this.nomodsFlag = nomodsFlag;
 	  }
 
       public boolean getExpandAllFlag() {
 	  	return expandAllFlag;
 	  }
 
 	  public void setExpandAllFlag(boolean expandAllFlag) {
 	  	this.expandAllFlag = expandAllFlag;
 	  }
 
 	  public Section  getNullSection() {
 	  	return nullSection;
 	  }
 
 	  public Date getNullDate() {
 		  return nullDate;
 	  }
 	  public void setNullSection(Section  nullSection) {
 	  	this.nullSection = nullSection;
 	  }
 
 	  public String getTypeEditor(){
 	  	return "typeEditor";
 	  }
 	  public void setTypeEditor(String typeEditor){
 	  	this.typeEditor = typeEditor;
 	  }
 
 	  public String getTypeLink(){
 	  	return "typeLink";
 	  }
 	  public void setTypeLink(String typeLink){
 	  	this.typeLink = typeLink;
 	  }
 
 	  public String getTypeUpload(){
 	  	return "typeUpload";
 	  }
 	  public void setTypeUpload(String typeUpload){
 	  	this.typeUpload = typeUpload;
 	  }
 	  public String getIsNull()
 	  {
 	  	return isNull;
 	  }
 
 	  public List getViewModuleBeans() {
 
 	  	    try {
 	  	    if(viewModuleBeans == null)
 	  	    	viewModuleBeans = getModuleService().getViewModules(getUserId(), getCourseId());
 
 	  	    }
 	  	    catch (Exception e)
 		    {
 	  		  logger.debug(e.toString());
 		    }
 
 
 	  	    //If list of modules returned is zero or if all of them are hidden
 	  	    if ((viewModuleBeans == null)||(viewModuleBeans.size() == 0))
 	  	    {
 	  	      nomodsFlag = true;
 	  	      FacesContext ctx = FacesContext.getCurrentInstance();
   		      addNoModulesMessage(ctx);
   		      viewModuleBeans = new ArrayList();
 	  	    }
 	  	    else
 	  	    {
 	  	    	for (ListIterator i = viewModuleBeans.listIterator(); i.hasNext();)
 				{
 					ViewModBean vmbean = (ViewModBean) i.next();
 					if (vmbean.isVisibleFlag() == false)
 					{
 						closedModulesFlag = true;
 						break;
 					}
 				}
 	  	    }
 		  	return viewModuleBeans;
 	  }
 
 	  public void setViewModuleBeans(List viewModuleBeansList) {
 	    viewModuleBeans = viewModuleBeansList;
 	  }
 
 
 
 	  public int getShowModuleId() {
 	        return this.showModuleId;
 	  }
 
 	  public void setShowModuleId(int moduleId) {
 	        this.showModuleId = moduleId;
 	  }
 
 	  public String showHideSections()
 		{
 			if (getExpandAllFlag() == true)
 			{
 				setShowModuleId(-1);
 				setExpandAllFlag(false);
 			}
 			else
 			{
 				ViewModBean vmbean = null;
 				FacesContext ctx = FacesContext.getCurrentInstance();
 				UIViewRoot root = ctx.getViewRoot();
 			    UIData table = null;
 			    if (getRole()!= null && getRole().equals("INSTRUCTOR")){
 			       table = (UIData)
 			        root.findComponent("listmodulesform").findComponent("table");
 			    }
 			    if (getRole()!= null && getRole().equals("STUDENT")){
 			        table = (UIData)
 		            root.findComponent("listmodulesStudentform").findComponent("table");
 			     }
 		        vmbean = (ViewModBean) table.getRowData();
 		        if (getShowModuleId() != vmbean.getModuleId())
 				{	
 				   setShowModuleId(vmbean.getModuleId());
 				}
 				else
 				{
 					setShowModuleId(-1);
 					setExpandAllFlag(false);
 				}
 			}
 			String retVal = "list_modules_student";
 		   if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 		    {
 		    	retVal = "list_modules_inst";
 		    }
 			  	return retVal;
 		}
 
 	  public String expandCollapseAction()
 		{
 			if (getExpandAllFlag() == false)
 			{		
 			  setExpandAllFlag(true);
 			}
 			else
 			{	
 			  setExpandAllFlag(false);
 			  setShowModuleId(-1);
 			}  
 			 String retVal = "list_modules_student";
 		    if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 			{
 			   	retVal = "list_modules_inst";
 			}
 		   return retVal;	
 		}
 	  
    
       public String redirectToViewModule()
 	  {
 	  	String retVal = "view_module_student";
 	    if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 		{
 		  retVal = "view_module";
 	    }
 
 	  	return retVal;
 	  }
 
       public void viewModule(ActionEvent evt)
 	  {
     	  ViewModBean vmbean = null;
     	  FacesContext ctx = FacesContext.getCurrentInstance();
     	  Map params = ctx.getExternalContext().getRequestParameterMap();
     	  int selModIndex;
     	  if(params != null && params.containsKey("modidx"))
     		 {
     		  String modidxStr = (String) params.get("modidx");
     		  //This condition was added to fix ME-809 bug report issue
     		  if ((modidxStr != null)&&(modidxStr.length() > 0)&&(!(modidxStr.equals("null"))))
     		  {
     		    selModIndex = Integer.parseInt(modidxStr);
     		  }
     		  else
     		  {
     			 selModIndex = 0;
     		  }
     	     }
     	  else
           {
           	ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
   			String Errmsg = bundle.getString("error_view_module");
   			FacesMessage msg =new FacesMessage(Errmsg);
   		  	msg.setSeverity(FacesMessage.SEVERITY_ERROR);
   			ctx.addMessage (null, msg);
   			return;
           }
     	  ValueBinding binding =
     		  Util.getBinding("#{viewModulesPage}");
     	  ViewModulesPage vmPage = (ViewModulesPage)
     	  binding.getValue(ctx);
     	  vmPage.setPrintable(null);
     	  if (getRole()!= null && (getRole().equals("INSTRUCTOR") || getRole().equals("STUDENT"))){
     		  if ((viewModuleBeans != null)&&(viewModuleBeans.size() > 0))
     		  {
     			  vmbean = (ViewModBean) viewModuleBeans.get(selModIndex);
     			  vmPage.setModuleId(vmbean.getModuleId());
     			  vmPage.setMdbean(null);
     			  vmPage.setPrevMdbean(null);
     			  vmPage.setModuleSeqNo(vmbean.getSeqNo());
     			  vmPage.setAutonumber(null);
     		  }
     	  }
 
       }
 
       public String redirectToViewSection()
 	  {
         String retVal = "view_section_student";
 	    //3/21/05 - Mallika - added this code in to handle linked and uploaded sections
         if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 	    {
 	    	retVal = "view_section";
 
 	    }
 
 	  	return retVal;
 	  }
 
       public String redirectToViewSectionLink()
 	  {
         String retVal = "view_section_student";
 	    //3/21/05 - Mallika - added this code in to handle linked and uploaded sections
         if (getRole()!= null && getRole().equals("INSTRUCTOR"))
 	    {
 	    		retVal = "view_section";
 	    }
 
 	  	return retVal;
 	  }
 
 
 
       public void viewSection(ActionEvent evt)
 	  {
         FacesContext ctx = FacesContext.getCurrentInstance();
 
         Map params = ctx.getExternalContext().getRequestParameterMap();
         int selModIndex,selSecIndex;
         if(params != null && params.containsKey("modidx")&& params.containsKey("secidx"))
         {
            String modidxStr = (String) params.get("modidx");
    		  //This condition was added to fix ME-809 bug report issue
    		  if ((modidxStr != null)&&(modidxStr.length() > 0)&&(!(modidxStr.equals("null"))))
    		  {
    		    selModIndex = Integer.parseInt(modidxStr);
    		  }
    		  else
    		  {
    			 selModIndex = 0;
    		  }
    		   String secidxStr = (String) params.get("secidx");
 		  //This condition was added to fix ME-809 bug report issue
 		  if ((secidxStr != null)&&(secidxStr.length() > 0)&&(!(secidxStr.equals("null"))))
 		  {
 		    selSecIndex = Integer.parseInt(secidxStr);
 		  }
 		  else
 		  {
 			 selSecIndex = 0;
 		  }
 
         }
         else
         {
         	ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String Errmsg = bundle.getString("error_view_section");
 			FacesMessage msg =new FacesMessage(Errmsg);
 		  	msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			ctx.addMessage (null, msg);
 			return;
         }
 		ModuleObjService mod = null;
 		ViewSecBean vsBean = null;
 		ViewModBean vmBean = null;
 		int modSeqNo = 0;
 
 		if (getRole()!= null && (getRole().equals("INSTRUCTOR") || getRole().equals("STUDENT"))) {
 			if ((viewModuleBeans != null)&&(viewModuleBeans.size() > 0))
 			{
 		    	vmBean = (ViewModBean) viewModuleBeans.get(selModIndex);
 		    	//Fix for ArrayIndexOutofBoundsException: -1
		    	if (selSecIndex == -1) selSecIndex = 0;
 			    vsBean = (ViewSecBean) vmBean.getVsBeans().get(selSecIndex);
 		        modSeqNo = vmBean.getSeqNo();
 			}
 		}
 
 		ValueBinding binding =
 	    Util.getBinding("#{viewSectionsPage}");
 	    ViewSectionsPage vsPage = (ViewSectionsPage)
 	    binding.getValue(ctx);
 	    vsPage.resetValues();
 	    vsPage.setSection(null);
 	    vsPage.setModule(null);
 	    if (vsBean != null)
 	    {
 	    // Section sec = vsBean.getSection();
 	    vsPage.setModuleId(vmBean.getModuleId());
 	    vsPage.setSectionId(vsBean.getSectionId());
 	    //vsPage.setSection(sec);
 	    }
 
 	    vsPage.setModuleSeqNo(modSeqNo);
    }
       
       public String goWhatsNext()
       {
         ViewModBean vmBean = null;
     	FacesContext ctx = FacesContext.getCurrentInstance();
     	Map params = ctx.getExternalContext().getRequestParameterMap();
     	int selModIndex=0,modSeqNo=-1;
     	
     	if(params != null)
     	 {
     		  String modidxStr = (String) params.get("modidx2");
     		  //This condition was added to fix ME-809 bug report issue
     		  if ((modidxStr != null)&&(modidxStr.length() > 0)&&(!(modidxStr.equals("null"))))
     		  {
     		    selModIndex = Integer.parseInt(modidxStr);
     		  }
     		  else
     		  {
     			 selModIndex = 0;
     		  }
     		  String modSeqStr = (String) params.get("modseqno");
     		  if ((modSeqStr != null)&&(modSeqStr.length() > 0)&&(!(modSeqStr.equals("null"))))
     		  {
     		    modSeqNo = Integer.parseInt(modSeqStr);
     		  }
     		  else
     		  {
     			 modSeqNo = -1;
     		  }
     	}
     	 
     	  ValueBinding binding =
               Util.getBinding("#{viewNextStepsPage}");
             ViewNextStepsPage vnPage = (ViewNextStepsPage)
               binding.getValue(ctx);
     	  if (getRole()!= null && (getRole().equals("INSTRUCTOR") || getRole().equals("STUDENT"))) 
     	  {
   			if ((viewModuleBeans != null)&&(viewModuleBeans.size() > 0))
   			{
   		    	vmBean = (ViewModBean) viewModuleBeans.get(selModIndex);
   			}
   		}    	  
       	int nextSeqNo = getModuleService().getNextSeqNo(getCourseId(),new Integer(modSeqNo),getInstRole());
       	vnPage.setNextSeqNo(nextSeqNo);
       	vnPage.setModule(getModuleService().getModule(vmBean.getModuleId()));
       	if ((vmBean.getVsBeans() == null)||(vmBean.getVsBeans().size() == 0))
       	{	
       	  vnPage.setPrevSecId(0);
       	  vnPage.setPrevModId(vmBean.getModuleId());
       	}
       	else
       	{
       		vnPage.setPrevModId(vmBean.getModuleId());
       		ViewSecBean vsBean = (ViewSecBean) vmBean.getVsBeans().get(vmBean.getVsBeans().size()-1);
       	    vnPage.setPrevSecId(vsBean.getSectionId());   
       	}
 
       	return "view_whats_next";
 
       }      
 
 	  private void addNoModulesMessage(FacesContext ctx){
 	  	FacesMessage msg =
 	  		new FacesMessage("No modules", "No modules are available for the course at this time.");
 	  	ctx.addMessage(null,msg);
 	  }
 
 	  public Integer getPrintModuleId()
 		{
 			FacesContext ctx = FacesContext.getCurrentInstance();
 			try
 			{
 				UIViewRoot root = ctx.getViewRoot();
 				UIData table;
 				if (getRole() != null && getRole().equals("INSTRUCTOR"))
 				{
 					table = (UIData) root.findComponent("listmodulesform").findComponent("table");
 				}
 				else
 					table = (UIData) root.findComponent("listmodulesStudentform").findComponent("table");
 			ViewModBean vmbean = (ViewModBean) table.getRowData();
 			printModuleId = vmbean.getModuleId();
 			return printModuleId;
 			}
 			catch (Exception me)
 			{
 				logger.error(me.toString());
 			}
 			return 0;
 		}
 
 	  public boolean isPrintable()
 	  {
 		  FacesContext ctx = FacesContext.getCurrentInstance();
 		  try{
 			  if (printMaterial == null)
 			  {
 				  String site_id = ToolManager.getCurrentPlacement().getContext();
 				  ValueBinding binding = Util.getBinding("#{authorPreferences}");
 				  AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(ctx);
 				  printable = preferencePage.isMaterialPrintable(site_id);
 				  printMaterial = new Boolean(printable);
 			  }
 		  }
 		  catch(Exception e){e.printStackTrace();
 		  printable=false;}
 		  return printable;
 	  }
 
 	  public boolean isAutonumber()
 	  {
 		  FacesContext ctx = FacesContext.getCurrentInstance();
 		  try{
 			  if (autonumberMaterial == null)
 			  {
 				  String site_id = ToolManager.getCurrentPlacement().getContext();
 				  ValueBinding binding = Util.getBinding("#{authorPreferences}");
 				  AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(ctx);
 				  autonumber = preferencePage.isMaterialAutonumber(site_id);
 				  autonumberMaterial = new Boolean(autonumber);
 			  }
 		  }
 		  catch(Exception e){e.printStackTrace();
 		  autonumber=false;}
 		  return autonumber;
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
 
 	public boolean isClosedModulesFlag()
 	{
 		return this.closedModulesFlag;
 	}
 
 	public void setClosedModulesFlag(boolean closedModulesFlag)
 	{
 		this.closedModulesFlag = closedModulesFlag;
 	}
 
 	public UIData getModTable()
 	{
 		return this.modTable;
 	}
 
 	public void setModTable(UIData modTable)
 	{
 		this.modTable = modTable;
 	}
 
 	public UIData getSecTable()
 	{
 		return this.secTable;
 	}
 
 	public void setSecTable(UIData secTable)
 	{
 		this.secTable = secTable;
 	}
 
 	/**
 	 * @param autonumberMaterial the autonumberMaterial to set
 	 */
 	public void setAutonumberMaterial(Boolean autonumberMaterial)
 	{
 		this.autonumberMaterial = autonumberMaterial;
 	}
 }
