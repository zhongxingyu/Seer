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
 import java.io.File;
 import java.io.Serializable;
 
 import javax.faces.component.UICommand;
 import javax.faces.component.UIData;
 import javax.faces.component.UIInput;
 import javax.faces.component.UIParameter;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.application.FacesMessage;
 
 import org.etudes.component.app.melete.MeleteResource;
 import org.sakaiproject.util.ResourceLoader;
 
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.api.app.melete.exception.UserErrorException;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.ResourceProperties;
 
 import org.sakaiproject.event.cover.EventTrackingService;
 import org.sakaiproject.tool.cover.ToolManager;
 
 //import org.sakaiproject.jsf.ToolBean;
 
 
 /**
  * @author Rashmi
  *
  * This class is the backing bean for AddModuleSections.jsp.
  * Rashmi - 5/31/07 - add upload is similar to edit upload and goes to next page to get the file
  */
 
 public class AddSectionPage extends SectionPage implements Serializable{
 	private boolean sizeWarning=false;
 
 	public AddSectionPage(){
 		setFormName("AddSectionForm");
 		shouldRenderNotype = true;
 		}
 
 	/**
 	 * @return sizeWarning
 	 * render sizeWarning message if this flag is true
 	 */
 	public boolean getSizeWarning()
 	{
 		return this.sizeWarning;
 	}
 
 	/**
 	 * @param sizeWarning
 	 * to set sizeWarning to true if section save is successful.
 	 */
 	public void setSizeWarning(boolean sizeWarning)
 	{
 		this.sizeWarning = sizeWarning;
 	}
 
     /**
      * return content editor
     */
     public String getContentEditor()
     {
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
           if(this.contentEditor == null || this.contentEditor.length()== 0)
           {
                   this.contentEditor = new String("Compose content here");
           }
           return this.contentEditor;
     }
 
 	/**
 	 * instantiates saving of a section.
 	 * Validate required fields - modality and if content is there then copyright.
 	 * If section has content then save section resource and resource using content hosting
 	 * otherwise just save a section.
 	 * If sucess set success flag and show the success message.
 	 * if any error, display error message to the user.
 	 * revised - to get all error messages at one time
 	 **/
 	public String saveHere()
 	{
 		setSuccess(false);
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
         ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
         //validation 1:	   modality is required.
 	     if (!validateModality())
 	     {
 	     	String errMsg = bundle.getString("add_section_modality_reqd");
 			context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"add_section_modality_reqd",errMsg));
 			return "failure";
 	     }
 	     ValueBinding binding = Util.getBinding("#{licensePage}");
 
 	     LicensePage lPage = (LicensePage)binding.getValue(context);
 	     lPage.setFormName(getFormName());
 	     //validation 2: if content is provided then check for copyright license
 	     try
 		   {
 	          if(!section.getContentType().equals("notype") && lPage.getLicenseCodes().equals(lPage.Copyright_CODE))
 	          {
 	        	lPage.checkForRequiredFields();
 	          }
 		   }
 		catch(UserErrorException uex)
 		   {
 			String errMsg = bundle.getString(uex.getMessage());
 			context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,uex.getMessage(),errMsg));
 			return "failure";
 		   }
 	    //validation 3: if upload a new file check fileName format - moved to uploadSerctionContent()
 	  	// validation 4: check link url - moved to addresourcetoMeleteCollection()
 	  	 try
 		 {
	 		if(!section.getContentType().equals("notype") && !section.getContentType().equals("typeEditor") && meleteResource.getResourceId() == null)
 	 		{
 	 			String errMsg = bundle.getString("section_content_required");
 				context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"section_content_required",errMsg));
 				return "failure";
 	 		}
 	   //   save section
 		    if (logger.isDebugEnabled()) logger.debug("AddSectionpage:inserting section");
 		     String uploadHomeDir = ServerConfigurationService.getString("melete.uploadDir", "");
 		    String addCollId = getMeleteCHService().getCollectionId( section.getContentType(), module.getModuleId());
 
 			// step 1: insert section
 			Integer newSectionId = sectionService.insertSection(module,section);
 			section.setSectionId(newSectionId);
 			section.setModule(module);
 
 //			Step2: if section has content then only create section resource and resource
 			if(!section.getContentType().equals("notype"))
 			{
 				meleteResource = lPage.processLicenseInformation(getMeleteResource());
 				selResourceIdFromList = getSelResourceIdFromList();
 
 			//	step 2.1:existing resource is selected for section content
 				if(!section.getContentType().equals("typeEditor") && selResourceIdFromList != null)
 					{
 					if (logger.isDebugEnabled()) logger.debug("existing resource is selected");
 					meleteResource.setResourceId(selResourceIdFromList);
 					getMeleteCHService().editResourceProperties(selResourceIdFromList, secResourceName, secResourceDescription);
 					}
 				else
 					{
 			//	Step 2.2: add the new resource to course site module /uploads collection
 				// in case of upload and link, resource is added on clicking Continue
 					selResourceIdFromList = null;
 					if(section.getContentType().equals("typeEditor"))
 					{
 						   String newResourceId = addResourceToMeleteCollection(uploadHomeDir,addCollId);
 						   meleteResource.setResourceId(newResourceId);
 					}
 					else getMeleteCHService().editResourceProperties(meleteResource.getResourceId(), secResourceName, secResourceDescription);
 
 					}
 
 			//step 3: insert section resource in melete table i.e. if new resource then insert in melete resource table
 			//	otherwise just insert in sectionResource table
 				if(selResourceIdFromList == null) sectionService.insertMeleteResource(section, meleteResource);
 				else
 				{
 					sectionService.updateResource(meleteResource);
 					sectionService.insertSectionResource(section, meleteResource);
 				}
 			}
 
 		}
 	     catch(MeleteException mex)
 			{
 			logger.debug("error in inserting section "+ mex.toString());
 			//rollback and delete section
 			try{
 				if(selResourceIdFromList != null) sectionService.deleteResource(meleteResource);
 				if(section.getSectionId()!= null && section.getSectionId().intValue() != 0)
 					sectionService.deleteSection(section,(String)sessionMap.get("courseId"), null);
 				} catch (Exception e){}
 			String errMsg = bundle.getString(mex.getMessage());
 			if(mex.getMessage().equals("embed_image_size_exceed"))
 			{				
 				errMsg = errMsg.concat(ServerConfigurationService.getString("content.upload.max", "0"));
 				errMsg = errMsg.concat(bundle.getString("embed_image_size_exceed1"));
 			}
 			context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,mex.getMessage(),errMsg));
 			return "failure";
 			}
 		catch(Exception ex)
 			{
 			logger.debug("error in inserting section "+ ex.toString());
 			try{
 			if(selResourceIdFromList != null) sectionService.deleteResource(meleteResource);
 			if(section.getSectionId()!= null && section.getSectionId().intValue() != 0)
 					sectionService.deleteSection(section,(String)sessionMap.get("courseId"), null);
 			} catch (Exception e){}
 			String errMsg = bundle.getString("add_section_fail");
 			context.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"add_section_fail",errMsg));
 			//ex.printStackTrace();
 			return "failure";
 			}
 
 		//Track the event
 		EventTrackingService.post(EventTrackingService.newEvent("melete.section.new", ToolManager.getCurrentPlacement().getContext(), true));
 
 		return "success";
 	}
 
 	public String save()
 	{
 		setSuccess(false);
 		if(!saveHere().equals("failure"))
 		{
 		setSuccess(true);
 		return "confirm_addmodulesection";
 		}
 		return "addmodulesections";
 	}
 
 	/*
 	 *  on clicking link 2 me the page navigates back to add module section
 	 */
 	public String redirectLink()
 	{
 		return "ContentLinkServerView";
 	}
 
 
 
 
 	/**
 	 * save the section, if not saved yet and then refresh the page to
 	 * add more sections.
 	 **/
 	public String saveAndAddAnotherSection()
 	{
 
 	     // create new instance of section model
 	     resetSectionValues();
 	     setSizeWarning(false);
 
 		return "addmodulesections";
 	}
 
 	/**
 	 * returns failure if the section has not been saved first.
 	 * returns finish to redirect to addmodulefinish page.
 	 * */
 	public String Finish()
 	{
 			return "list_auth_modules";
 	}
 
 	public String previewFromAdd()
 	{
 
 		try{
 			if(!section.getContentType().equals("notype"))
 			{
 			  if (this.section.getContentType().equals("typeEditor"))
 			  {
 				this.previewContentData = this.contentEditor;
 				return "addpreview";
 			  }
 			  else
 			  {
 	    	      this.previewContentData = getMeleteCHService().getResourceUrl(meleteResource.getResourceId());
 	               return "addpreview";
 	           }
 			}
 			else
 			{
 				this.previewContentData = null;
 				return "addpreview";
 			}
   	     } catch (Exception e) {
 			logger.debug(e.toString());
 		  }
 		return "#";
 	}
 
 	  public String gotoServerView()
 	  {
 	  	logger.debug("going to server view page");
 	  	FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ValueBinding binding =Util.getBinding("#{listResourcesPage}");
 		ListResourcesPage listResPage = (ListResourcesPage) binding.getValue(ctx);
 		listResPage.setFromPage("ContentUploadServerView");
 		listResPage.resetValues();
 	    selResourceIdFromList = null;
  	    return "ContentUploadServerView";
 	  }
 
 
 
 
 	  public String setServerFile()
 	  {
 	  	FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		selResourceIdFromList = getSelResourceIdFromList();
 	 	try
 		{
 	  		// local file is selected so create a resource to move on
 	  	  if(selResourceIdFromList == null)
 	  		{
             	   String addCollectionId = getMeleteCHService().getUploadCollectionId();
             	   String uploadHomeDir = ServerConfigurationService.getString("melete.uploadDir", "");
             	   String newResourceId = addResourceToMeleteCollection(uploadHomeDir,addCollectionId);
 				   getMeleteResource().setResourceId(newResourceId);
             	   secResourceName = getDisplayName(newResourceId);
 				   logger.debug("sec name after reading from display name prop" + secResourceName);
           }
 	  	  else
 	  	  {
 	  		 processSelectedResource(selResourceIdFromList);
 	  	  }
 
 	  		ctx.renderResponse();
 		}
 		catch(Exception e)
 			{
 			logger.debug("error in set server file for add section content" + e.toString());
 			//e.printStackTrace();
 			String errMsg;
 			try{
 				 errMsg = bundle.getString(e.getMessage());
 			} catch(Exception ex){errMsg = bundle.getString("exist_upload_fail");}
      		ctx.addMessage (null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"",errMsg));
 			return "ContentUploadServerView";
 			}
 	  		return "addmodulesections";
 	  }
 
 	  public String cancelServerFile()
 	  {
 			selResourceIdFromList = null;
 	  		setLinkUrl(null);
 	  		currLinkUrl = null;
 	  		displayCurrLink = null;
 	  		secResourceName = null;
 			secResourceDescription = null;
 	  		return "addmodulesections";
 	  }
 
 
 
 	  public String gotoServerLinkView()
 	  {
 		  FacesContext ctx = FacesContext.getCurrentInstance();
 		  ValueBinding binding =Util.getBinding("#{listResourcesPage}");
 			ListResourcesPage listResPage = (ListResourcesPage) binding.getValue(ctx);
 			listResPage.setFromPage("ContentLinkServerView");
 			listResPage.resetValues();
 		    selResourceIdFromList = null;
 			setLinkUrl(null);
 			newURLTitle="";
 			if(displayCurrLink == null || displayCurrLink.length() == 0) newURLTitle = secResourceName;
 			return "ContentLinkServerView";
 	  }
 
 	  public String gotoServerLTIView()
 	  {
 			FacesContext ctx = FacesContext.getCurrentInstance();
 			ValueBinding binding =Util.getBinding("#{listResourcesPage}");
 			ListResourcesPage listResPage = (ListResourcesPage) binding.getValue(ctx);
 			listResPage.setFromPage("ContentLTIServerView");
 			listResPage.resetValues();
 			selResourceIdFromList = null;
 			setLinkUrl(null);
 			newURLTitle="";
 			if(displayCurrLink == null || displayCurrLink.length() == 0) newURLTitle = secResourceName;
 
 			setLTIUrl(null);
 			setLTIPassword(null);
 			setLTIKey(null);
 			setLTIDisplay("Basic");
 			// Do this after the first two
 			setLTIDescriptor(null);
 			newURLTitle ="";
 			return "ContentLTIServerView";
 	  }
 
 	  public String setServerUrl()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		String errMsg = null;
 		logger.debug("set server url of add page");
 		selResourceIdFromList = getSelResourceIdFromList();
 
 		try
 		{
 			// new link provided
 			if (selResourceIdFromList == null)
 			{
 				if (getLinkUrl().equals("http://") || getLinkUrl().equals("https://"))
 				{
 					errMsg = bundle.getString("select_or_cancel");
 					ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "select_or_cancel", errMsg));
 					return "ContentLinkServerView";
 				}
 
 				if(newURLTitle == null || newURLTitle.length() == 0)
 				{
 					errMsg = bundle.getString("URL_title_reqd");
 					ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "URL_title_reqd", errMsg));
 					return "editContentLinkServerView";
 				}
 
 				secResourceName = newURLTitle;
 				String addCollectionId = getMeleteCHService().getUploadCollectionId();
 				String newResourceId = addResourceToMeleteCollection(null, addCollectionId);
 				getMeleteResource().setResourceId(newResourceId);
 				//currLinkUrl = getLinkUrl();
 				currLinkUrl = getLinkContent(newResourceId);				
 				secResourceName = getDisplayName(newResourceId);
 			}
 			else
 		  	{
 		  		 processSelectedResource(selResourceIdFromList);
 		  	}
 			logger.debug("currlink value in setServer is" + currLinkUrl);
 			setLinkUrl(currLinkUrl);
 			createLinkUrl();
 			ctx.renderResponse();
 		}
 		catch (UserErrorException uex)
 		{
 			if (uex.getMessage() != null)
 			{
 			  errMsg = bundle.getString(uex.getMessage());
 			  ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, uex.getMessage(), errMsg));
 			}
 			return "ContentLinkServerView";
 		}
 			catch (Exception e)
 			{
 				if (e.getMessage() != null)
 				{
 				  errMsg = bundle.getString(e.getMessage());
 				  ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), errMsg));
 				}
 				return "ContentLinkServerView";
 			}
 			return "addmodulesections";
 		}
 
 	public String setServerLTI()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		String errMsg = null;
 		logger.debug("set server url of add page");
 		selResourceIdFromList = getSelResourceIdFromList();
 
 		try
 		{
 			// new link provided
 			if (selResourceIdFromList == null)
 			{
 				if (getLTIDescriptor().equals("http://") || getLTIDescriptor().equals("https://"))
 				{
 					errMsg = bundle.getString("select_or_cancel");
 					ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "select_or_cancel", errMsg));
 					return "ContentLTIServerView";
 				}
 
                if(newURLTitle == null || newURLTitle.length() == 0)
                {
                     errMsg = bundle.getString("URL_title_reqd");
                     ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "URL_title_reqd", errMsg));
                     return "editContentLTI";
                }
 
 				secResourceName = newURLTitle;
 				String addCollectionId = getMeleteCHService().getUploadCollectionId();
 				String newResourceId = addResourceToMeleteCollection(null, addCollectionId);
 				getMeleteResource().setResourceId(newResourceId);
 				currLinkUrl = getLTIDescriptor();
 			}
 			else
 		  	{
 		  		 processSelectedResource(selResourceIdFromList);
 		  	}
 			logger.debug("currlink value in setServer is" + currLinkUrl);
 			createLTIDescriptor();
 			ctx.renderResponse();
 		}
 		catch (Exception e)
 		{
 			logger.debug("error in set server url for edit section content" + errMsg);
 			// e.printStackTrace();
 			if (e.getMessage() != null)
 			{
 				errMsg = bundle.getString(e.getMessage());
 				ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), errMsg));
 			}
 			return "ContentLTIServerView";
 		}
 		return "addmodulesections";
 	}
 
 	  public void selectedResourceAction(ActionEvent evt) {
 	    	FacesContext ctx = FacesContext.getCurrentInstance();
 	    	UICommand cmdLink = (UICommand)evt.getComponent();
 
 	      	List cList = cmdLink.getChildren();
 	    	UIParameter param = (UIParameter) cList.get(0);
 	    	selResourceIdFromList = (String)param.getValue();
 	    	if (logger.isDebugEnabled()) logger.debug("selected resource id by user is " + selResourceIdFromList);
 
 	    	// populate properties panel with the selected resource
 	    	try{
 	    			ContentResource cr= getMeleteCHService().getResource(selResourceIdFromList);
 			    	this.secResourceName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 			    	this.secResourceDescription = cr.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
 			    	if(cr.getContentLength() > 0)
 						currLinkUrl = new String(cr.getContent());
 	    		//get resource object
 			    	MeleteResource existResource = (MeleteResource)sectionService.getMeleteResource(selResourceIdFromList);
 			    	//just take resource properties from this object as its assoc with another section
 			    	if(existResource != null)
 			    		{
 						meleteResource = existResource;
 						ValueBinding binding = Util.getBinding("#{licensePage}");
 
 						LicensePage lPage = (LicensePage)binding.getValue(ctx);
 						lPage.setInitialValues(formName, existResource);
 
 					// render selected file name
 			    		selectedResourceName = secResourceName;
 			   		if (logger.isDebugEnabled()) logger.debug("values changed in resource action for res name and desc" + secResourceName + secResourceDescription);
 			    	}
 			    	ctx.renderResponse();
 	    	}
 
 	    	catch(Exception ex)
 			{
 	    		logger.debug("error while accessing content resource" + ex.toString());
 			}
 			return;
 		}
 
 	  public void resetSectionValues()
 	    {
 		  super.resetSectionValues();
 		  shouldRenderNotype = true;
 		  if(section != null) section = null;
 	    }
 }
