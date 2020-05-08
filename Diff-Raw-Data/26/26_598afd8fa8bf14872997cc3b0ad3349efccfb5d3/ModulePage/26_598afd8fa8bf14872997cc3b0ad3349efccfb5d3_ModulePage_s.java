 /**********************************************************************************
  *
  * $URL$
  * $Id$  
  ***********************************************************************************
  *
  * Copyright (c) 2008,2009 Etudes, Inc.
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
 
 import java.util.*;
 import java.io.Serializable;
 import java.io.File;
 import java.io.FileOutputStream;
 
 import javax.faces.context.FacesContext;
 import javax.faces.application.FacesMessage;
 import javax.faces.el.ValueBinding;
 import javax.faces.model.SelectItem;
 import javax.faces.validator.ValidatorException;
 
 import javax.faces.component.*;
 import javax.faces.event.*;
 
 import org.sakaiproject.util.ResourceLoader;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.component.app.melete.CourseModule;
 import org.etudes.component.app.melete.Module;
 import org.etudes.component.app.melete.ModuleShdates;
 import org.etudes.component.app.melete.ModuleDateBean;
 import org.etudes.api.app.melete.*;
 
 /**
  * @author Rashmi
  *
  * Add Module Page is the backing bean for the page add_module.jsp.
  * It also connects to other jsp pages like cclicenseform.jsp and
  * publicdomainform.jsp and license_results.jsp
  *
  * rashmi - 3/21 to make season and yr readable
  * revised by rashmi to get author name from sessionMap
  * revised by murthy to get currYear if its 0
  * revised by rashmi to remove dupl error messages for select a license
  * revised by rashmi on 6/15/05 to make FairUserender boolean as protected
  * revised by rashmi on 6/20/05 to add I agree and add messages
  * Mallika - 8/1/06 - removing I Agree code
  * Rashmi - 28/12/06 - removed extra variables and their getter/setters for license
  * Mallika - 10/18/06 - adding method to checkUploadExists
  * Mallika - 10/30/06 - Adding getAuthor to get author's name for copyright license
  *  */
 
 public abstract class ModulePage implements Serializable{
 
     protected ModuleObjService module;
     private ModuleShdatesService moduleShdates;
     protected ModuleDateBean mdBean;
 
     /** Dependency:  The logging service. */
     protected Log logger = LogFactory.getLog(ModulePage.class);
 	protected ModuleService moduleService;
 
     // license form rendering
     private boolean saveFlag;
 
     private boolean success;
     //added by rashmi -- 11/24
     private boolean editPageFlag;
     private ArrayList showDates;
 
     private StringBuffer author;
     private int year;
     private int currYear;
     private String season;
 
     private String formName;
     protected int moduleNumber;
 
     public ModulePage(){}
 
    	/**
 	 * @return
 	 */
 	public boolean getSuccess()
 	{
 		return success;
 	}
 
 	/**
 	 * @param
 	 */
 	public void setSuccess(boolean success)
 	{
 		this.success = success;
 	}
 
     /**
      * @return list of next 5 years.
      * This method calculates the next 5 yrs and populate the combo box of
      * year in add_module.jsp page.
      */
     public ArrayList getShowDates()
 {
 	if(showDates == null)
 	{
 		showDates = new ArrayList();
         Calendar c = Calendar.getInstance(TimeZone.getTimeZone("PST"));
         int yr = c.get(Calendar.YEAR);
         currYear = yr;
 
         for (int i=0; i < 5; i++) showDates.add(new SelectItem((new Integer(yr+i)).toString()));
 	}
 	return showDates;
 }
     protected boolean validateDates(FacesContext context, ResourceLoader bundle, Date st, Date end)
     {
 	   Calendar calstart = new GregorianCalendar();
      	Calendar calend = new GregorianCalendar();
 
      	boolean errorFlag = false;
      	if ((st != null) || (end!= null))
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
 
   //      validation no 4 b
      	  if ((end != null)&&(st != null))
      	  {
      	  if(end.compareTo(st) <= 0)
      	  {
      		String errMsg = "";
 	     	errMsg = bundle.getString("end_date_before_start");
 	     	addMessage(context, "Error Message", errMsg, FacesMessage.SEVERITY_ERROR);
 	     	errorFlag = true;
      	  }
      	  }
      	}
      	//If there is an error, validation fails and the method returns false
      	//If there are no errors, validation passes and the method returns true;
      	if (errorFlag == true) return false;
      	return true;
     }
 
     protected void addMessage(FacesContext context, String msgName, String msgDetail, FacesMessage.Severity severity)
 	{
 		FacesMessage msg = new FacesMessage(msgName, msgDetail);
 		msg.setSeverity(severity);
 		context.addMessage(null, msg);
 	}
     /**
      *
      * This is the most critical function. It actually submits the form and sends
      * module and moduleShdates instances to the db level for adding a new module.
      *
      * Config file will conatin the outcome values from this function to navigate to next page.
      *
      *  validations
 	 * 1.if learning obj or desc is present
 	 * 2. if license type is cc license or public domain then ensure that
 	 *  user has agreed to the license, hence we need to check licenseAgree flag is true
 	 *
      **/
 
     public abstract String save();
 
 
     /**
      * Gets the module. if module instance is not created , create here and set
      * the default values. set author name and institution from session.
      *
      * add_module.jsp elements bind to the attributes of this
      * instance.
      *
      * Revision - added learning objectives default value -- Rashmi 11/17
      * get first name , institute etc from session -- Rashmi 11/22
      *  set sucess, licenseCoders and agree to fix bug#19, #20 -- Rashmi 12/15
      */
 
     public ModuleObjService getModule() {
         if (null == module) {
   	      module = new Module();
 
 		// get user information from session
 	      FacesContext context = FacesContext.getCurrentInstance();
 	      Map sessionMap = context.getExternalContext().getSessionMap();
 	      module.setCreatedByFname((String)sessionMap.get("firstName"));
 	      module.setCreatedByLname((String)sessionMap.get("lastName"));
 	      module.setInstitute((String)sessionMap.get("institute"));
 
 	      success=false;
 
 	 }
     return module;
   }
 
     /**
      *
      * consolidate author name by concatenating first name and last name.
      * revised by rashmi to get name from sessionMap
      */
   public String getAuthor()
   {
 	if(author == null)
 	{
 		author = new StringBuffer();
 	    FacesContext context = FacesContext.getCurrentInstance();
 	    Map sessionMap = context.getExternalContext().getSessionMap();
 		author.append((String)sessionMap.get("firstName") + " ");
 		author.append((String)sessionMap.get("lastName"));
 	}
 	return author.toString();
   }
   /**
    *
    * not required as the fields are disabled.
    *
    */
   public void setAuthor(String author){ }
 
   /**
    *
    * return yr selected.
    * revised by rashmi - 3/21 to make season and yr readable
    */
   public String getYear(){
 	String yr="";
 	if(year != 0) {
 	 yr=new Integer(year).toString();
 	}
 	else
 	{
 		try{
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo ms = (MeleteSiteAndUserInfo)
             binding.getValue(context);
 
 		} catch(Exception e)
 		{
 			yr=" ";
 			logger.debug("error in getting yr ..prob becos of melete site and user");
 		}
 	}
 	return yr;
 	}
 
   public String getCurrYear(){
 	String yr="";
 	if(currYear != 0) {
 	 yr=new Integer(currYear).toString();
 	}else{
 		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("PST"));
 		currYear = c.get(Calendar.YEAR);
 		 yr=new Integer(currYear).toString();
 	}
 	return yr;
 	}
   /**
    *
    * set the year selected by user.
    *
    */
   public void setYear(String year)
 	{
 	this.year = (new Integer(year)).intValue();
       }
 
    /**
    *
    * default season is "select season"
    *
    */
 
   public String getSeason()
   {
 	if(season == null)
 	{
 		try{
 
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo ms = (MeleteSiteAndUserInfo)binding.getValue(context);
 			season = ms.getCourseTerm();
 			if(season == null )
 				season = " ";
 
 		}catch(Exception e)
 		{
 			season=" ";
 			logger.debug("sesaon is null prob some error in meleteSiteand user");
 		}
 	}
 	return season;
   }
 
   /**
    *
    * set user's selection
    *
    */
   public void setSeason(String season){
 	this.season = season;
  }
   /**
    *
    * set module
    * Revised by rashmi to show results in edit page 11/24
    */
   public void setModule(ModuleObjService module) {
 
     this.module = module;
 
   }
 
   /**
    *
    * create an instance of moduleshdates.
    * Revised to open a course for one year by default --Rashmi 12/6
     * Revised on 12/20 Rashmi to set start default time as 8:00 am and
    * end date time as 11:59 pm
    */
 
   public ModuleShdatesService getModuleShdates() {
     if (null == moduleShdates) {
      moduleShdates = new ModuleShdates();
 	/*       GregorianCalendar cal = new GregorianCalendar();
 	       cal.set(Calendar.HOUR,8);
 	       cal.set(Calendar.MINUTE,0);
 	       cal.set(Calendar.SECOND,0);
 	       cal.set(Calendar.AM_PM,Calendar.AM);
 	       moduleShdates.setStartDate(cal.getTime());
 	       cal.add(Calendar.YEAR, 1);
 	       cal.set(Calendar.HOUR,11);
 	       cal.set(Calendar.MINUTE,59);
 	       cal.set(Calendar.SECOND,0);
 	       cal.set(Calendar.AM_PM,Calendar.PM);
 	       moduleShdates.setEndDate(cal.getTime());
 	  */       }
 	    return moduleShdates;
 	  }
 
   /**
    *
    * @return
    *
    */
   public void setModuleShdates(ModuleShdatesService moduleShdates) {
     this.moduleShdates = moduleShdates;
   }
 
 
 
 public ModuleDateBean getModuleDateBean() {
   	return mdBean;
   }
 
   public void setModuleDateBean(ModuleDateBean mdBean){
   	this.mdBean = mdBean;
   }
 
   public String getFormName(){
 	return formName;
 	}
 
   /**
   * @param formName
   */
   public void setFormName(String formName){
   	this.formName = formName;
   }
 
    //Mallika - 10/18/06 - new method to check if uploads directory exists
        public void checkUploadExists()
        {
    	  FacesContext context = FacesContext.getCurrentInstance();
    	   String uploadDir = context.getExternalContext().getInitParameter("uploadDir");
 	  if (logger.isDebugEnabled()) logger.debug("In checkUploadExists "+uploadDir);
       File uploadsDir = new File(uploadDir);
          if (uploadsDir.exists()) return;
          if (!(uploadsDir.exists()))
          {
    		  boolean res = uploadsDir.mkdirs();
    		  if (logger.isDebugEnabled()) logger.debug("Uploads directory was created "+res);
 
 
    		  Map sessionMap = context.getExternalContext().getSessionMap();
    		  String str = "User ID: "+sessionMap.get("userId")+"| Course ID: "+sessionMap.get("courseId")+"| Module ID: "+getModule().getModuleId().intValue()+"\n";
 
    		  File logFile = new File(uploadsDir+File.separator+"uploads_missing_log.txt");
 
              try
              {
    		    FileOutputStream fos = new FileOutputStream(logFile, true);
    		    fos.write(str.getBytes());
    		    fos.close();
    	      }
    	      catch (Exception e)
    	      {
    		    logger.error("Could not create log file "+e.toString());
    	      }
          }
 
        }
 
  /*
    * to remove retained values
    * Rashmi -- 12/21
    */
   public void resetModuleValues()
   {
                 saveFlag = false;
                  success= false;
                   season=null;
                   moduleNumber = 0;
                   editPageFlag = false;
   }
 
   public void resetModuleNumber()
 	{
   	moduleNumber = 0;
   	editPageFlag = true;
   	}
 
   /*
 	 * get tentative seq# assigned to module. bug fix #211
 	 * Rashmi - 1/21
 	 */
 	public String getModuleNumber()
 	{
 		if(moduleNumber != 0 )
 			return new Integer(moduleNumber).toString();
 
 
 		CourseModule cm = (CourseModule) module.getCoursemodule();
 		if(cm == null)
 		{
 			return "0";
 		}
 
 
 			moduleNumber = cm.getSeqNo();
 
 		return new Integer(moduleNumber).toString();
 	}
 
 
 	public String backToModules()
 	{
 		  FacesContext context = FacesContext.getCurrentInstance();
 	        ValueBinding binding =Util.getBinding("#{listAuthModulesPage}");
 	        ListAuthModulesPage listPage = (ListAuthModulesPage) binding.getValue(context);
 	        listPage.resetValues();
 	        listPage.setModuleDateBeans(null);
 		return "list_auth_modules";
 	}
 
 	/**
 	 * @return naviagtion rule on click of cancel button
 	 */
 	public String cancel()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
         ValueBinding binding =Util.getBinding("#{listAuthModulesPage}");
         ListAuthModulesPage listPage = (ListAuthModulesPage) binding.getValue(context);
         listPage.resetValues();
         listPage.setModuleDateBeans(null);
 		return "list_auth_modules";
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
 
 
  }
