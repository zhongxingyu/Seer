 /**********************************************************************************
  *
  * $URL$
  * $$
  * $Id$
  ************************************************************************************
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
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URLEncoder;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Collection;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UICommand;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.component.UIParameter;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 
 import org.etudes.component.app.melete.MeleteResource;
 import org.etudes.component.app.melete.Module;
 import org.etudes.component.app.melete.Section;
 import org.etudes.component.app.melete.SectionResource;
 import org.etudes.component.app.melete.MeleteUserPreference;
 import org.imsglobal.simplelti.SimpleLTIUtil;
 import org.imsglobal.basiclti.BasicLTIUtil;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.ModuleObjService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionObjService;
 import org.etudes.api.app.melete.SectionResourceService;
 import org.etudes.api.app.melete.MeleteResourceService;
 import org.etudes.api.app.melete.SectionService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.api.app.melete.exception.UserErrorException;
 import org.etudes.api.app.melete.MeleteCHService;
 
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.cover.ContentTypeImageService;
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.util.ResourceLoader;
 import javax.faces.model.SelectItem;
 /**
  * @author Rashmi
  *
  * This class is the backing bean for AddModuleSections.jsp and EditModuleSections.jsp page.
  *
  * Rashmi - 8/14/06 - add license code and show access options
  * Rashmi - add section content as a resource using CH and create module collection using CH
  * Rashmi - 8/23/06 - use meletelicense instead of modulelicense
  * Rashmi - 3/12/07 - configure FCK editor collectionBase and security advisor
  * Rashmi - 3/12/07 - show listing in chunks
  */
 
 
 public abstract class SectionPage implements Serializable {
 
     private StringBuffer author;
     protected String contentEditor;
     protected String previewContentData;
     protected String hiddenUpload;
     protected String checkUploadChange;
     private boolean success = false;
     private boolean renderInstr=false;
     private int maxUploadSize;
     public String serverLocation;
 	public String formName;
 	protected String uploadFileName;
 	//rendering flags
 	protected boolean shouldRenderEditor=false;
 	protected boolean shouldRenderLink=false;
 	protected boolean shouldRenderLTI=false;
 	protected boolean shouldRenderUpload=false;
 	protected boolean shouldRenderNotype = false;
 
 	protected boolean shouldLTIDisplayAdvanced = false;
 
 	/** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(SectionPage.class);
 	protected SectionService sectionService;
 	protected ModuleService moduleService;
 	protected ServerConfigurationService serverConfigurationService;
 	protected ModuleObjService module;
     protected SectionObjService section;
     protected MeleteCHService meleteCHService;
 	protected ContentHostingService contentservice;
 
     // rashmi - 3.0 added variables
 	 protected String access;
 	  protected SectionResourceService secResource;
 
 	  private String linkUrl;
 	  private String ltiDescriptor;
 	  private byte[] secContentData;
 	  protected String selResourceIdFromList;
 	  private String nullString = null;
 	  protected String secResourceName;
 	  protected String secResourceDescription;
 	  protected MeleteResource meleteResource;
 	  protected String FCK_CollId;
 
 	 protected String currLinkUrl;
 	 protected String currLTIDescriptor;
 	 protected String currLTIKey;
 	 protected String currLTIPassword;
 	 protected String currLTIUrl;
      protected String displayCurrLink;
      protected String newURLTitle;
      protected String newLTIDescriptor;
      protected ArrayList allContentTypes;
 
      protected String selectedResourceName;
 
      protected String selectedResourceDescription;
 
  	protected MeleteResource selectedResource;
 
 
  	protected String oldType;
 
     public SectionPage()
             {
             module=null;
             section=null;
             contentEditor=null;
             hiddenUpload=null;
             checkUploadChange=null;
             serverLocation = "http://localhost:8080";
             secResourceName=null;
             secResourceDescription="";
             selResourceIdFromList = null;
             secResource = null;
             meleteResource = null;
             allContentTypes = null;
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
 
       /**
          * @return success
          * render sucess message if this flag is true
          */
     public boolean getSuccess()
     {
             return this.success;
     }
 
 
     /**
      * @param success
      * to set sucess to true if section save is successful.
      */
     public void setSuccess(boolean success)
     {
             this.success = success;
     }
 
 //  get rendering flags
 
     public boolean getShouldRenderEditor()
     {
             if(this.section != null && this.section.getContentType() != null)
             {
             shouldRenderEditor = this.section.getContentType().equals("typeEditor");
             }
             return shouldRenderEditor;
     }
 
 
     public boolean getShouldRenderLink()
     {
             shouldRenderLink = false;
             if(this.section != null && this.section.getContentType() != null)
             {
                     shouldRenderLink = this.section.getContentType().equals("typeLink");
             }
             return shouldRenderLink;
     }
 
     public boolean getShouldRenderLTI()
     {
             shouldRenderLTI = false;
             if(this.section != null && this.section.getContentType() != null)
             {
                     shouldRenderLTI = this.section.getContentType().equals("typeLTI");
             }
             return shouldRenderLTI;
     }
 
     public boolean getShouldRenderUpload()
     {
             if(this.section != null && this.section.getContentType() != null)
             {
             shouldRenderUpload = this.section.getContentType().equals("typeUpload");
             }
             return shouldRenderUpload;
     }
 
 
 
 	/**
 	 * @return Returns the shouldRenderNotype.
 	 */
 	public boolean isShouldRenderNotype() {
 		if(this.section != null && this.section.getContentType() != null)
         {
 			shouldRenderNotype = this.section.getContentType().equals("notype");
         }
 		return shouldRenderNotype;
 	}
     /**
      * @return module
      * if module is not set, get the module from session.
      * revision -- 12/20 Rashmi -- to refresh module to currModule
      */
     public ModuleObjService getModule()
     {
             FacesContext context = FacesContext.getCurrentInstance();
             Map sessionMap = context.getExternalContext().getSessionMap();
             ModuleObjService nextModule =null;
             if(module == null && section != null && section.getModule()!= null )
             {
                     module=(Module)section.getModule();
                      return module;
             }
             if(sessionMap.containsKey("currModule"))
             {
                     nextModule= (ModuleObjService)sessionMap.get("currModule");
                   if (logger.isDebugEnabled()) logger.debug("contains currModule in sessionMap and next curr module and module" + nextModule + module);
             }
             if(module == null || (nextModule!=null && nextModule.getModuleId() != module.getModuleId()))
             {
           if (logger.isDebugEnabled()) logger.debug("get module of add section page called and module is null");
             module = (ModuleObjService)sessionMap.get("currModule");
             }
             return module;
     }
 
     public void setModule(ModuleObjService module)
     {
     	this.module = module;
     }
 
     /**
      * @return section.
      * create a new instance of section and assign default values.
      * set name from user
      *
      * Revision -- 11/22 user info from session
      */
     public SectionObjService getSection()
     {
         FacesContext context = FacesContext.getCurrentInstance();
         Map sessionMap = context.getExternalContext().getSessionMap();
           if (null == this.section) {
                         if (logger.isDebugEnabled()) logger.debug("get section is null so creating one");
               this.section = new Section();
               this.section.setContentType("notype");
               // user info from session
               this.section.setCreatedByFname((String)sessionMap.get("firstName"));
               this.section.setCreatedByLname((String)sessionMap.get("lastName"));
               this.section.setTextualContent(true);
             }
 
 
         return this.section;
     }
 
 
     /*
      * set section. This method is called to set section for edit
      * by breadcrumps hyperlinks in editmodulesections.jsp page and
      * also by editmodule.jsp and list_modules_auth.jsp
      *
      * Revision -- Rashmi 12/20 to set section as null
      */
     public void setSection(SectionObjService sec)
     {
         try
 			{
             this.section = null;
             if(sec !=null)
             {
                     if (logger.isDebugEnabled()) logger.debug("setSection called and section is not null");
                     this.module = (Module)sec.getModule();
                     this.section = sec;
             }
         }catch(Exception ex){logger.debug(ex.toString());}
     }
 /*
  *  added to set module null. seggregated from the setSection method
  */
     public void setModuleNull()
     {
             this.module = null;
     }
     /*
      * get the max uploads size allowed for the course.
      * revision - get course from session 11/22 Rashmi
      *
      * revision -- Rashmi 2/1/05 in accordance to service etc
      *
      */
     public int getMaxUploadSize()
     {
             /*
              * get from session
              */
               FacesContext context = FacesContext.getCurrentInstance();
               Map sessionMap = context.getExternalContext().getSessionMap();
 
 
              int sz = Integer.parseInt((String)sessionMap.get("maxSize"));
               if (logger.isDebugEnabled()) logger.debug("Size is "+sz);
 
 
             return sz;
     }
 
 
 //Mallika - new code beg
     public void setMaxUploadSize(int sz)
     {
             this.maxUploadSize = sz;
     }
     //Mallika - new code end
 
 
 
     /**
      * concatenates first name and last name of the creator of this section.
      * @return author name
      */
     public String getAuthor()
       {
             if(author == null)
             {
                     author = new StringBuffer();
                     author.append(this.section.getCreatedByFname() + " ");
                     author.append(this.section.getCreatedByLname());
             }
             return author.toString();
       }
       /**
        * not required as the fields are disabled.
        */
       public void setAuthor(String author){ }
 
       /**
      * @param contentEditor
      *
      */
     public void setContentEditor(String contentEditor){		
                     this.contentEditor = contentEditor;
             }
 
     /**
      * @return
      */
     public String getHiddenUpload()
     {
     	try{
 
             if(section!=null && hiddenUpload == null && meleteResource != null)
             {
             	ContentResource cr = getMeleteCHService().getResource(meleteResource.getResourceId());
             	 hiddenUpload =cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
                   checkUploadChange = hiddenUpload;
             }
             else if(hiddenUpload !=null)
             {
                     hiddenUpload = hiddenUpload.substring(hiddenUpload.lastIndexOf(File.separator)+1);
             }
     	}catch(Exception ex){
     		logger.debug("error accessing hidden upload field");}
             return hiddenUpload;
     }
 
 
     /**
      * @param hiddenUpload
      */
     public void setHiddenUpload(String hiddenUpload)
     {
             this.hiddenUpload = hiddenUpload;
     }
 
     /**
      * @param event
      * @throws AbortProcessingException
      * show hides the input boxes to specify the uploaded file name,
      * link or writing new content. based on the user radio button selection.
      */
     public void showHideContent(ValueChangeEvent event)throws AbortProcessingException
     {
     	FacesContext context = FacesContext.getCurrentInstance();
 
             UIInput contentTypeRadio = (UIInput)event.getComponent();
 
             shouldRenderEditor = contentTypeRadio.getValue().equals("typeEditor");
             shouldRenderLink = contentTypeRadio.getValue().equals("typeLink");
             shouldRenderUpload = contentTypeRadio.getValue().equals("typeUpload");
             shouldRenderNotype = contentTypeRadio.getValue().equals("notype");
             shouldRenderLTI = contentTypeRadio.getValue().equals("typeLTI");
 
             selResourceIdFromList = null;
             secResourceName = null;
             secResourceDescription = null;
             setMeleteResource(null);
             oldType = section.getContentType();
 
             if(contentTypeRadio.findComponent(getFormName()).findComponent("uploadPath") != null)
                     {
                     contentTypeRadio.findComponent(getFormName()).findComponent("uploadPath").setRendered(shouldRenderUpload);
                     contentTypeRadio.findComponent(getFormName()).findComponent("BrowsePath").setRendered(shouldRenderUpload);
                     }
 
 
             if(contentTypeRadio.findComponent(getFormName()).findComponent("link") != null)
             	{
                    contentTypeRadio.findComponent(getFormName()).findComponent("link").setRendered(shouldRenderLink);
             	}
 
             if(contentTypeRadio.findComponent(getFormName()).findComponent("ContentLTIView") != null)
             	{
                    contentTypeRadio.findComponent(getFormName()).findComponent("ContentLTIView").setRendered(shouldRenderLTI);
             	}
 
               if(shouldRenderEditor)
 	            {
             	  ValueBinding binding = Util.getBinding("#{authorPreferences}");
    	    		  AuthorPreferencePage preferencePage = (AuthorPreferencePage)binding.getValue(context);
 	               String usereditor = preferencePage.getUserEditor();
 	    		   this.contentEditor = new String("Compose content here");
 		           if(contentTypeRadio.findComponent(getFormName()).findComponent("otherMeletecontentEditor") != null && usereditor.equals(preferencePage.FCKEDITOR))
 		                {
 		                contentTypeRadio.findComponent(getFormName()).findComponent("otherMeletecontentEditor").setRendered(shouldRenderEditor);
 		                 setFCKCollectionAttrib();
 		                }
 
 		           if(contentTypeRadio.findComponent(getFormName()).findComponent("contentEditorView") != null && usereditor.equals(preferencePage.SFERYX))
 		           		{
 		           		preferencePage.setDisplaySferyx(true);
 		           		contentTypeRadio.findComponent(getFormName()).findComponent("contentEditorView").setRendered(shouldRenderEditor);
 		           		}
 	            }
 
             if(contentTypeRadio.findComponent(getFormName()).findComponent("ResourceListingForm") != null)
             {
             	section.setContentType((String)contentTypeRadio.getValue());
               contentTypeRadio.findComponent(getFormName()).findComponent("ResourceListingForm").setRendered(false);
             }
 
             //Upon changing content type, license is set by the selected resource, if there is one,
             //or via User preferences
             ValueBinding binding = Util.getBinding("#{licensePage}");
             LicensePage lPage = (LicensePage)binding.getValue(context);
             lPage.setFormName(this.formName);
             lPage.resetValues();
             /*if (getMeleteResource().getResourceId() != null)
             {
                	lPage.setInitialValues(this.formName, getMeleteResource());
             }
             else
             {*/
                 ValueBinding binding2 = Util.getBinding("#{authorPreferences}");
          	    AuthorPreferencePage preferencePage = (AuthorPreferencePage)binding2.getValue(context);
          	    MeleteUserPreference mup = preferencePage.getMup();
          	    lPage.setInitialValues(this.formName, mup);
             //}
             //The code below is required because the setter for the license code kicks in by default
             //and we need to actually set the component with the values determined above.(ME-1071)         	   
             UIComponent licComp = (UIComponent)contentTypeRadio.findComponent(getFormName());
             if(licComp != null && licComp.findComponent("ResourcePropertiesPanel") != null && licComp.findComponent("ResourcePropertiesPanel").findComponent("LicenseForm") != null
             	&& licComp.findComponent("ResourcePropertiesPanel").findComponent("LicenseForm").findComponent("SectionView") != null)
             	{
             		licComp = licComp.findComponent("ResourcePropertiesPanel").findComponent("LicenseForm").findComponent("SectionView");
             		UIInput uiInp = (UIInput)licComp.findComponent("licenseCodes");
             		uiInp.setValue(lPage.getLicenseCodes());
             		licComp = (UIComponent)contentTypeRadio.findComponent(getFormName()).findComponent("ResourcePropertiesPanel").findComponent("LicenseForm").findComponent("CCLicenseForm");
             		uiInp = (UIInput)licComp.findComponent("allowCmrcl");
             		uiInp.setValue(lPage.getAllowCmrcl());
             		uiInp = (UIInput)licComp.findComponent("allowMod");
             		uiInp.setValue(lPage.getAllowMod());
             		if (lPage.isShouldRenderCC())
             		{
             			uiInp = (UIInput)licComp.findComponent("copy_owner");
             			uiInp.setValue(lPage.getCopyright_owner());
             			uiInp = (UIInput)licComp.findComponent("copy_year");
             			uiInp.setValue(lPage.getCopyright_year());
             		}
             		if (lPage.isShouldRenderCopyright()||lPage.isShouldRenderPublicDomain()||lPage.isShouldRenderFairUse())
             		{
             			uiInp = (UIInput)licComp.findComponent("copy_owner1");
             			uiInp.setValue(lPage.getCopyright_owner());
             			uiInp = (UIInput)licComp.findComponent("copy_year1");
             			uiInp.setValue(lPage.getCopyright_year());
             		}
             	}
     }
 
     /**
      * @param event
      * @throws AbortProcessingException
      * Changes the LTI view from basic to advanced.
      */
     public void toggleLTIDisplay(ValueChangeEvent event)throws AbortProcessingException
     {
 	// Nothing to do - because the setter handles it all
     }
 
     public String getLTIDisplay()
     {
 	if ( shouldLTIDisplayAdvanced ) return "Advanced";
 	return "Basic";
     }
 
     public void setLTIDisplay(String newDisplay)
     {
 	shouldLTIDisplayAdvanced = "Advanced".equals(newDisplay);
     }
 
     public String getLTIUrl()
     {
 	return currLTIUrl;
     }
     public void setLTIUrl(String LTIUrl)
     {
 	currLTIUrl = LTIUrl;
 	fixDescriptor();
     }
 
     public String getLTIKey()
     {
 	return currLTIKey;
     }
     public void setLTIKey(String LTIKey)
     {
 	currLTIKey = LTIKey;
 	fixDescriptor();
     }
 
     public String getLTIPassword()
     {
 	return currLTIPassword;
     }
     public void setLTIPassword(String LTIPassword)
     {
 	currLTIPassword = LTIPassword;
 	fixDescriptor();
     }
 
     // Produce a basic descriptor from the URL and Password
     private void fixDescriptor()
     {
          if ( currLTIUrl == null ) return;
          String desc =
 		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 		"<basic_lti_link\n" +
 		"     xmlns=\"http://www.imsglobal.org/xsd/imsbasiclti_v1p0\"\n" +
 		"     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
 		"  <melete-basic>true</melete-basic> \n" +
 		"  <launch_url>"+currLTIUrl+"</launch_url> \n" +
 		"  <x-secure>\n" ;
          if ( currLTIKey != null && currLTIKey.trim().length() > 0 ) {
 		desc = desc + "    <launch_key>"+currLTIKey+"</launch_key> \n" ;
          }
          if ( currLTIPassword != null && currLTIPassword.trim().length() > 0 ) {
 		desc = desc + "    <launch_secret>"+currLTIPassword+"</launch_secret> \n" ;
          }
 	desc = desc + "  </x-secure>\n";
 	desc = desc + "</basic_lti_link>\n";
 	setLTIDescriptor(desc);
     }
 
     public boolean getShouldLTIDisplayAdvanced()
     {
 	return shouldLTIDisplayAdvanced;
     }
 
     public boolean getShouldLTIDisplayBasic()
     {
 	return ! shouldLTIDisplayAdvanced;
     }
 
     /*
      * modality is required. check if one is selected or not
      */
     protected boolean validateModality()
     {
             if(!(this.section.isAudioContent()|| this.section.isTextualContent()|| this.section.isVideoContent()))
                     return false;
             return true;
     }
 
 	/*
 	 *  adds resource to specified melete module or uploads collection.
 	 */
     public String addResourceToMeleteCollection(String uploadHomeDir, String addCollId) throws UserErrorException,MeleteException
     {
     	try{
     		String res_mime_type=getMeleteCHService().MIME_TYPE_EDITOR;
     		boolean encodingFlag = false;
 
     		if(section.getContentType().equals("typeEditor"))
     		{
     			contentEditor =  getMeleteCHService().findLocalImagesEmbeddedInEditor(uploadHomeDir,contentEditor);
     			res_mime_type= getMeleteCHService().MIME_TYPE_EDITOR;
     			secContentData = new byte[contentEditor.length()];
     			secContentData = contentEditor.getBytes();
     			encodingFlag = true;
     			secResourceName = getMeleteCHService().getTypeEditorSectionName(section.getSectionId());
     			secResourceDescription="compose content";
     		}
 
     		if (section.getContentType().equals("typeLink")) {
     			res_mime_type = getMeleteCHService().MIME_TYPE_LINK;
     			Util.validateLink(getLinkUrl());
     		    if ((secResourceName == null)|| (secResourceName.trim().length() == 0))
     				throw new MeleteException("URL_title_reqd");
     			secContentData = new byte[linkUrl.length()];
     			secContentData = linkUrl.getBytes();
     		}
     		if (section.getContentType().equals("typeLTI")) {
     			if(getLTIUrl() != null)
     			Util.validateLink(getLTIUrl());
     			String pitch = getLTIDescriptor();
     			if (ltiDescriptor == null || ltiDescriptor.trim().length() == 0) {
     				throw new MeleteException("add_section_empty_lti");
     			}
     			if (! ( SimpleLTIUtil.validateDescriptor(ltiDescriptor)
 			      || BasicLTIUtil.validateDescriptor(ltiDescriptor) != null ) ) {
     				throw new MeleteException("add_section_bad_lti");
     			}
     			res_mime_type = getMeleteCHService().MIME_TYPE_LTI;
     			secContentData = new byte[ltiDescriptor.length()];
     			secContentData = ltiDescriptor.getBytes();
     		}
     		if (section.getContentType().equals("typeUpload")) {
     			res_mime_type = uploadSectionContent("file1");
     			if (logger.isDebugEnabled())
     				logger.debug("new names for upload content is"
     						+ res_mime_type + "," + secResourceName);
     			if (res_mime_type == null)
     				throw new MeleteException("select_or_cancel");
     		}
     		ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(encodingFlag,
     				secResourceName, secResourceDescription);
     		if (logger.isDebugEnabled())
     			logger.debug("add resource now " + secResourceName);
 
     		String newResourceId = getMeleteCHService().addResourceItem(secResourceName, res_mime_type, addCollId, secContentData,res);
     		return newResourceId;
     	}
     	catch(UserErrorException uex)
     	{
     		throw uex;
     	}
     	catch(MeleteException me)
     	{
     		logger.debug("error in creating resource for section content" + me.toString());
     		throw me;
     	}
     	catch(Exception e)
     	{
     		if (logger.isDebugEnabled()) {
     			logger.debug("error in creating resource for section content" + e.toString());
     			//e.printStackTrace();
     		}
     		throw new MeleteException("add_section_fail");
     	}
     }
 
 	/*
 	 *  adds resource to specified melete module or uploads collection.
 	 */
 	public void editMeleteCollectionResource(String uploadHomeDir,String resourceId) throws MeleteException
 	{
 			if (logger.isDebugEnabled()) logger.debug("edit resource function");
             String res_mime_type=null;
             boolean encodingFlag = false;
 
             try
 			{
 				 if (logger.isDebugEnabled()) logger.debug("editing properties for " + resourceId);
 
 	            if(section.getContentType().equals("typeEditor"))
 	            {
             		contentEditor =  getMeleteCHService().findLocalImagesEmbeddedInEditor(uploadHomeDir,contentEditor);
             		getMeleteCHService().editResource(resourceId, contentEditor);
 	            }
 
 	            if(resourceId != null && (section.getContentType().equals("typeLink") || section.getContentType().equals("typeUpload") || section.getContentType().equals("typeLTI")))
 	            {
 	                  getMeleteCHService().editResourceProperties(resourceId,secResourceName,secResourceDescription);
 	            }
             }
             catch(MeleteException me)
 			{
 			logger.debug("error in editing resource for section content" + me.toString());
 			throw me;
 			}
 			catch(Exception e)
 				{
 					if (logger.isDebugEnabled()) {
 					logger.debug("error in editing resource for section content" + e.toString());
 					e.printStackTrace();
 				}
 				throw new MeleteException("add_section_fail");
 				}
 	}
 
 
     public abstract String saveHere();
 
 
     /*
      * listener to set action for save button for setting data from html editor
      */
     public void saveListener(ActionEvent event)
     {
             if (logger.isDebugEnabled()) logger.debug("Hello Rashmi ------saveListener called");
     }
 
     /*
      * Render instructions on preview page
      */
     public boolean getRenderInstr()
     {
            if (this.section == null || this.section.getInstr()== null ||this.section.getInstr().length() == 0 )
                     renderInstr=false;
             else renderInstr=true;
             return renderInstr;
     }
 
 
     /*
      * Revised Rashmi on 1/21/05
      * new var module number set back to 0
      */
     public void resetSectionValues()
     {
      this.section= null;
      contentEditor=null;
  	setSuccess(false);
  	hiddenUpload=null;
  	checkUploadChange=null;
  	uploadFileName = null;
     secResource = null;
     secResourceName = null;
     secResourceDescription=null;
     secContentData=null;
     selResourceIdFromList = null;
     meleteResource = null;
     setLicenseCodes(null);
     linkUrl = null;
     ltiDescriptor = null;
     FCK_CollId = null;
    currLinkUrl = null;
     currLTIDescriptor = null;
     currLTIUrl = null;
     currLTIKey = null;
     currLTIPassword = null;
     displayCurrLink = null;
     FacesContext ctx = FacesContext.getCurrentInstance();
   	ValueBinding binding =  Util.getBinding("#{remoteBrowserFile}");
   	RemoteBrowserFile rbPage = (RemoteBrowserFile)binding.getValue(ctx);
   	if(rbPage != null)
   		{
 		  	rbPage.setRemoteFiles(null);
 		  	rbPage.setRemoteLinkFiles(null);
   		}
 
 	shouldRenderEditor=false;
 	shouldRenderLink=false;
 	shouldRenderLTI=false;
 	shouldRenderUpload=false;
 	shouldRenderNotype = false;
 	allContentTypes = null;
 	oldType = null;
     if (logger.isDebugEnabled()) logger.debug("!!!!!!!!!reseting section values done !!!!!!!");
     }
 
     /*
      *  reset resource values and license from java cache when its deleted and associated with current section
      */
 	public void resetMeleteResourceValues()
 	{
 		currLinkUrl = null;
 		currLTIDescriptor = null;
 		currLTIUrl = null;
 		currLTIKey = null;
 		currLTIPassword = null;
 		displayCurrLink = null;
 		secResourceName = null;
 		secResourceDescription = null;
 		uploadFileName = null;
 		setLicenseCodes(null);
 	}
 	  protected void processSelectedResource(String selResourceIdFromList)
 	  {
 		  FacesContext ctx = FacesContext.getCurrentInstance();
 		  try{
 			    currLinkUrl = getLinkContent(selResourceIdFromList);
   				ContentResource cr= getMeleteCHService().getResource(selResourceIdFromList);
 		    	this.secResourceName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 		    	this.secResourceDescription = cr.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
 
   		   //get resource object
 		    	selectedResource = (MeleteResource)sectionService.getMeleteResource(selResourceIdFromList);
 		    	//just take resource properties from this object as its assoc with another section
 		    	if(selectedResource != null)
 		    	{
 					meleteResource = selectedResource;
 					ValueBinding binding = Util.getBinding("#{licensePage}");
 
 					LicensePage lPage = (LicensePage)binding.getValue(ctx);
 					lPage.setInitialValues(formName, selectedResource);
 
 				// render selected file name
 		    		selectedResourceName = secResourceName;
 		    		if (logger.isDebugEnabled()) logger.debug("values changed in resource action for res name and desc" + secResourceName + secResourceDescription);
 		    	}
   	    }
   	    catch(Exception ex)
 		 {
   		logger.debug("error while accessing content resource" + ex.toString());
 		 }
 	  }
     /**
      * @return remove values from session before closing
      */
     public String cancel()
     {
             FacesContext context = FacesContext.getCurrentInstance();
             Map sessionMap = context.getExternalContext().getSessionMap();
             if(sessionMap.containsKey("currModule"))
             {
                     sessionMap.remove("currModule");
             }
             resetSectionValues();
             return "list_auth_modules";
     }
 
 
 
     /**
      * @return uploaded file or link name for preview page
      */
     public String getContentLink()
     {
        if (logger.isDebugEnabled()) logger.debug("getContentLink fn is called");
        return "#";
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
 
 
 
 
 
 //melete 3.0 work
                     /**
              * @param access The access to set.
              */
             public void setAccess(String access) {
                     this.access = access;
             }
     /**
      * @return Returns the access.
      */
     public String getAccess() {
             if(this.access == null)
                     this.access = "site";
             return access;
     }
 
 
 
     /**
      * @return Returns the m_license.
      */
    /* public SectionResourceLicenseSelector getM_license() {
             if(m_license == null)
             {
                     m_license = new SectionResourceLicenseSelector();
                     if (getMeleteResource().getResourceId() != null)
                     {
                     	m_license.setInitialValues(this.formName, sectionService,getMeleteResource());
                     }
                     else
                     {
                     	FacesContext context = FacesContext.getCurrentInstance();
          	    		ValueBinding binding = Util.getBinding("#{authorPreferences}");
          	    		AuthorPreferencePage preferencePage = (AuthorPreferencePage)binding.getValue(context);
          	    		MeleteUserPreference mup = preferencePage.getMup();
          	    		m_license.setInitialValues(this.formName, sectionService, mup);
                     }
 
             }
             return m_license;
     }*/
 
     /**
      * @param m_license The m_license to set.
      */
    /* public void setM_license(SectionResourceLicenseSelector m_license) {
             this.m_license = m_license;
     }*/
 
     public void setLicenseCodes(String licenseCodes)
     {
 		FacesContext context = FacesContext.getCurrentInstance();
     	ValueBinding binding = Util.getBinding("#{licensePage}");
   		LicensePage lPage = (LicensePage)binding.getValue(context);
   		lPage.setFormName(formName);
 		lPage.setLicenseCodes(licenseCodes);
     }
 
 	/**
      * @return Returns the linkUrl.
      */
     public String getLinkUrl() {
             if(linkUrl == null)linkUrl ="http://";
             return linkUrl;
     }
     /**
      * @param linkUrl The linkUrl to set.
      * as from section table we will remove link,contentpath and uploadpath fields.
      * This will get stored in resources.
      */
     public void setLinkUrl(String linkUrl) {
             this.linkUrl = linkUrl;
     }
 
     /**
      * @return Returns the ltiDescriptor.
      */
     public String getLTIDescriptor() {
             if(ltiDescriptor == null)ltiDescriptor ="";
             return ltiDescriptor;
     }
     /**
      * @param ltiDescriptor The ltiDescriptor to set.
      * as from section table we will remove link,contentpath and uploadpath fields.
      * This will get stored in resources.
      */
     public void setLTIDescriptor(String ltiDescriptor) {
             this.ltiDescriptor = ltiDescriptor;
     }
 
     /*
      * get material from the new provided link
      */
     public void createLinkUrl()
     {
     	if (secResourceName == null || secResourceName.length() == 0) secResourceName = linkUrl;
  	   	secContentData = new byte[getLinkUrl().length()];
         secContentData = getLinkUrl().getBytes();
     }
 
     /*
      * get material from the new provided Descriptor
      */
     public void createLTIDescriptor()
     {
     	if (secResourceName == null || secResourceName.length() == 0) secResourceName = "create name in createLTIDescriptor";
 	secContentData = new byte[getLTIDescriptor().length()];
         secContentData = getLTIDescriptor().getBytes();
     }
 
     /*
      * code for uploading content
      */
     public String uploadSectionContent(String fieldname) throws Exception
     {
      try{
      	FacesContext context = FacesContext.getCurrentInstance();
          org.apache.commons.fileupload.FileItem fi = (org.apache.commons.fileupload.FileItem) context.getExternalContext().getRequestMap().get(fieldname);
 
          if(fi !=null && fi.getName() != null && fi.getName().length() !=0)
              {
 
          	 Util.validateUploadFileName(fi.getName());
               // filename on the client
              secResourceName = fi.getName();
              if (secResourceName.indexOf("/") != -1)
              {
                secResourceName = secResourceName.substring(secResourceName.lastIndexOf("/")+1);
              }
              if (secResourceName.indexOf("\\") != -1)
              {
                secResourceName = secResourceName.substring(secResourceName.lastIndexOf("\\")+1);
              }
              if (logger.isDebugEnabled()) logger.debug("Rsrc name is "+secResourceName);
              if (logger.isDebugEnabled()) logger.debug("upload section content data " + (int)fi.getSize());
              this.secContentData= new byte[(int)fi.getSize()];
              InputStream is = fi.getInputStream();
              is.read(this.secContentData);
 
              String secContentMimeType = fi.getContentType();
              if (logger.isDebugEnabled()) logger.debug("file upload success" + secContentMimeType);
              return secContentMimeType;
             }
          else
          {
             logger.debug("File being uploaded is NULL");
             return null;
          }
          }
      	catch(MeleteException me)
 		{
      		logger.debug("file upload FAILED" + me.toString());
      		throw me;
 		}
      	catch(Exception e)
              {
             logger.error("file upload FAILED" + e.toString());
             return null;
              }
 
     }
 
 
 // to add spring dependency methods
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
      * @param serverConfigurationService The ServerConfigurationService to set.
      */
     public void setServerConfigurationService(
                     ServerConfigurationService serverConfigurationService) {
             this.serverConfigurationService = serverConfigurationService;
     }
 
 
 
      /**
      * @return Returns the secResource.
      */
     public SectionResourceService getSecResource() {
     		if (null == this.secResource || (this.section != null && this.section.getSectionResource() == null)) {
                     this.secResource = new SectionResource();
             }
             else this.secResource = this.section.getSectionResource();
             return secResource;
     }
     /**
      * @param secResource The secResource to set.
      */
     public void setSecResource(SectionResourceService secResource) {
             this.secResource = secResource;
     }
 
  	/**
 	 * @return Returns the nullString.
 	 */
 	public String getNullString() {
 		return nullString;
 	}
 
     /**
      * @return Returns the secResourceDescription.
      */
     public String getSecResourceDescription() {
     	    return secResourceDescription;
     }
     /**
      * @param secResourceDescription The secResourceDescription to set.
      */
     public void setSecResourceDescription(String secResourceDescription) {
             this.secResourceDescription = secResourceDescription;
     }
 
     /**
      * @return Returns the secResourceName.
      */
     public String getSecResourceName() {
     	   return secResourceName;
     }
 
     /**
      * @param secResourceName The secResourceName to set.
      */
     public void setSecResourceName(String secResourceName) {
             this.secResourceName = secResourceName;
     }
 
 	/**
 	 * @return Returns the secContentData.
 	 */
 	public byte[] getSecContentData() {
 		return secContentData;
 	}
 
 	public String getPreviewContentData() {
 		return previewContentData;
 	}
     /**
 	 * @return Returns the meleteCHService.
 	 */
 	public MeleteCHService getMeleteCHService() {
 			return meleteCHService;
 		}
 
 	public void setMeleteCHService(MeleteCHService meleteCHService) {
 			this.meleteCHService = meleteCHService;
 	}
 
 	/**
 	 * @return Returns the contentservice.
 	 */
 	public ContentHostingService getContentservice() {
 			return contentservice;
 	}
 
 	/**
 	 * @param contentservice The contentservice to set.
 	 */
 	public void setContentservice(ContentHostingService contentservice) {
 			this.contentservice = contentservice;
 	}
 
 	/**
 	 * @return Returns the meleteResource.
 	 */
 	public MeleteResource getMeleteResource() {
 		logger.debug("check meleteResource" + meleteResource + secResource);
 
 		if(formName.equals("AddSectionForm") && meleteResource == null)
 		{
 		    this.meleteResource = new MeleteResource();
 		}
 
        if(formName.equals("EditSectionForm") && meleteResource == null)
        {
        		if(secResource != null)	this.meleteResource = (MeleteResource)this.secResource.getResource();
        		if(meleteResource == null) this.meleteResource = new MeleteResource();
        }
 
 
 		return meleteResource;
 	}
 	/**
 	 * @param meleteResource The meleteResource to set.
 	 */
 	public void setMeleteResource(MeleteResource meleteResource) {
 		this.meleteResource = meleteResource;
 	}
 
 	public void setFCKCollectionAttrib()
 	{
 		FCK_CollId = getMeleteCHService().getUploadCollectionId();
 		String attrb = "fck.security.advisor." + FCK_CollId;
 
 		SessionManager.getCurrentSession().setAttribute(attrb, new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				try
 				{
 					Collection meleteGrpAllow = org.sakaiproject.authz.cover.AuthzGroupService.getAuthzGroupsIsAllowed(userId,"melete.author",null );
 
 					String anotherRef = new String(reference);
 					anotherRef = anotherRef.replace("/content/private/meleteDocs" ,"/site");
 					org.sakaiproject.entity.api.Reference ref1 = org.sakaiproject.entity.cover.EntityManager.newReference(anotherRef);
 
 					if (meleteGrpAllow.contains("/site/"+ref1.getContainer()))
 					{
 						return SecurityAdvice.ALLOWED;
 					}
 				}
 				catch (Exception e)
 				{
 					logger.warn("exception in setting security advice for FCK collection" + e.toString());
 					return SecurityAdvice.NOT_ALLOWED;
 				}
 				return SecurityAdvice.NOT_ALLOWED;
 			}
 		});
 	}
 
 
 	/**
 	 * @return Returns the fCK_CollId.
 	 */
 	public String getFCK_CollId() {
 		return FCK_CollId;
 	}
 
 
 	/**
 	 * @return Returns the selResourceIdFromList.
 	 */
 	public String getSelResourceIdFromList() {
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding =Util.getBinding("#{listResourcesPage}");
 		ListResourcesPage listResPage = (ListResourcesPage) binding.getValue(ctx);
 		selResourceIdFromList = listResPage.getSelResourceIdFromList();
 		return selResourceIdFromList;
 	}
 
 	/**
 	 * @return Returns the uploadFileName.
 	 */
 	public String getUploadFileName() {
 		if(secResourceName != null && secResourceName.length() !=0) uploadFileName = secResourceName;
 		return uploadFileName;
 	}
 	/**
 	 * @param uploadFileName The uploadFileName to set.
 	 */
 	public void setUploadFileName(String uploadFileName) {
 		this.uploadFileName = uploadFileName;
 	}
 
 	public String getDisplayCurrLink()
 	{
 	    String retval = currLinkUrl;
 
 		if (retval != null && retval.length() > 50)
 			displayCurrLink = retval.substring(0, 50) + "...";
 		else
 			displayCurrLink = retval;
 
 		return displayCurrLink;
 	}
 
 	public String getDisplayCurrLTI()
 	{
 	    String retval = secResourceName;
 
 		if (retval != null && retval.length() > 50)
 			displayCurrLink = retval.substring(0, 50) + "...";
 		else
 			displayCurrLink = retval;
 
 		return displayCurrLink;
 	}
 
 	/**
 	 * @return Returns the currLinkUrl.
 	 */
 	public String getCurrLinkUrl()
 	{
 		if (!(getLinkUrl().equals("http://") || getLinkUrl().equals("https://"))) currLinkUrl = getLinkUrl();
 		return currLinkUrl;
 	}
 
 	public String getCurrLTIUrl()
 	{
 		if ( meleteResource != null )
 		{
 			try
 			{
 				String rUrl = getMeleteCHService().getResourceUrl(meleteResource.getResourceId());
 				return rUrl;
 			}
 			catch (Exception e)
 			{
 				return "about:blank";
 			}
 		}
 		return "about:blank";
 	}
 
 	/**
 	 * @param currLinkUrl
 	 *        The currLinkUrl to set.
 	 */
 	public void setCurrLinkUrl(String currLinkUrl)
 	{
 		this.currLinkUrl = currLinkUrl;
 	}
 
 	/**
 	 * @return Returns the currLTIDescriptor.
 	 */
 	public String getCurrLTIDescriptor()
 	{
 		if (!(getLTIDescriptor().equals("http://") || getLTIDescriptor().equals("https://"))) currLTIDescriptor = getLTIDescriptor();
 		return currLTIDescriptor;
 	}
 
 	/**
 	 * @param currLTIDescriptor
 	 *        The currLTIDescriptor to set.
 	 */
 	public void setCurrLTIDescriptor(String currLTIDescriptor)
 	{
 		this.currLTIDescriptor = currLTIDescriptor;
 	}
 
 	/**
 	 * @return Returns the selectedResource.
 	 */
 	public MeleteResource getSelectedResource()
 	{
 		if (selectedResource == null) selectedResource = new MeleteResource();
 		return selectedResource;
 	}
 
 	/**
 	 * @param selectedResource
 	 *        The selectedResource to set.
 	 */
 	public void setSelectedResource(MeleteResource selectedResource)
 	{
 		this.selectedResource = selectedResource;
 	}
 
 	/**
 	 * @return Returns the selectedResourceDescription.
 	 */
 	public String getSelectedResourceDescription()
 	{
 		return selectedResourceDescription;
 	}
 
 	/**
 	 * @param selectedResourceDescription
 	 *        The selectedResourceDescription to set.
 	 */
 	public void setSelectedResourceDescription(String selectedResourceDescription)
 	{
 		this.selectedResourceDescription = selectedResourceDescription;
 	}
 
 	/**
 	 * @return Returns the selectedResourceName.
 	 */
 	public String getSelectedResourceName()
 	{
 		return selectedResourceName;
 	}
 
 	/**
 	 * @param selectedResourceName
 	 *        The selectedResourceName to set.
 	 */
 	public void setSelectedResourceName(String selectedResourceName)
 	{
 		this.selectedResourceName = selectedResourceName;
 	}
 
 
 	/**
 	 * @return the newURLTitle
 	 */
 	public String getNewURLTitle()
 	{
 		return this.newURLTitle;
 	}
 
 	/**
 	 * @param newURLTitle the newURLTitle to set
 	 */
 	public void setNewURLTitle(String newURLTitle)
 	{
 		this.newURLTitle = newURLTitle;
 	}
 
 	/**
 	 * @return the newLTIDescriptor
 	 */
 	public String getNewLTIDescriptor()
 	{
 		return this.newLTIDescriptor;
 	}
 
 	/**
 	 * @return the newLTIDescriptor to set
 	 */
 	public void setNewLTIDescriptor(String newLTIDescriptor)
 	{
 		this.newLTIDescriptor = newLTIDescriptor;
 	}
 
 	public List getAllContentTypes() {
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader(
 				"org.etudes.tool.melete.bundle.Messages");
 		if (allContentTypes == null) {
 			Map sessionMap = context.getExternalContext().getSessionMap();
 			String userId = (String) sessionMap.get("userId");
 
 			ValueBinding binding = Util.getBinding("#{authorPreferences}");
 			AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(context);
 			boolean userLTIChoice = preferencePage.getUserLTIChoice(userId);
 
 			allContentTypes = new ArrayList<SelectItem>(0);
 			String notypemsg = bundle.getString("addmodulesections_choose_one");
 			allContentTypes.add(new SelectItem("notype", notypemsg));
 			String typeEditormsg = bundle.getString("addmodulesections_compose");
 			allContentTypes.add(new SelectItem("typeEditor", typeEditormsg));
 			String typeUploadmsg = bundle.getString("addmodulesections_upload_local");
 			allContentTypes.add(new SelectItem("typeUpload", typeUploadmsg));
 			String typeLinkmsg = bundle.getString("addmodulesections_link_url");
 			allContentTypes.add(new SelectItem("typeLink", typeLinkmsg));
 			if (userLTIChoice) {
 				String typeLTImsg = bundle.getString("addmodulesections_lti");
 				allContentTypes.add(new SelectItem("typeLTI", typeLTImsg));
 			}
 		}
 		return allContentTypes;
 	}
 
 	 protected String getDisplayName(String resourceId)
 		{
 		 return getMeleteCHService().getDisplayName(resourceId);
 		}
 
 	 protected String getLinkContent(String resourceId)
 	 	 {
 	 		 return getMeleteCHService().getLinkContent(resourceId);
 	 	 }
 
 }
