 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  * Copyright (c) 2008,2009, 2010, 2011 Etudes, Inc.
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
 import javax.faces.context.FacesContext;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.*;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.*;
 
 import org.etudes.component.app.melete.MeleteResource;
 import org.etudes.component.app.melete.Section;
 import org.etudes.component.app.melete.SectionResource;
 
 import org.etudes.api.app.melete.MeleteCHService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionObjService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.api.app.melete.exception.UserErrorException;
 
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentResource;
 
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.util.ResourceLoader;
 
 import org.sakaiproject.event.cover.EventTrackingService;
 
 
 public class EditSectionPage extends SectionPage implements Serializable
 {
 	private boolean sizeWarning = false;
 
 	private ModuleService moduleService;
 
 	private boolean shouldRenderContentTypeSelect = false;
 
 	// picking from server
 	private String editSelection;
 
 	private String containCollectionId;
 
 	private boolean shouldRenderServerResources = false;
 
 	private boolean shouldRenderLocalUpload = false;
 
 	private Boolean hasNext = false;
 
 	private Boolean hasPrev = false;
 
 	/**
 	 * Default constructor
 	 */
 	public EditSectionPage()
 	{
 		setFormName("EditSectionForm");
 	}
 
 	/**
 	 * initializes values. 
 	 * @param section1
 	 * SectionObjService 
 	 * @return
 	 */
 	public String setEditInfo(SectionObjService section1)
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 
 		resetSectionValues();
 
 		setFormName("EditSectionForm");
 		setSection(section1);
 		setModule(this.section.getModule());
 	//	setSecResource(this.section.getSectionResource());
 		setSecResource(sectionService.getSectionResourcebyId(this.section.getSectionId().toString()));
 		if (this.secResource != null && this.secResource.getResource() != null)
 		{
 		//	setMeleteResource((MeleteResource) this.secResource.getResource());
 			setMeleteResource((MeleteResource)sectionService.getMeleteResource(secResource.getResource().getResourceId()));
 			if(this.meleteResource != null && this.meleteResource.getResourceId() != null && this.meleteResource.getResourceId().length() != 0)
 				setContentResourceData(this.meleteResource.getResourceId());
 	    	ValueBinding binding = Util.getBinding("#{licensePage}");
 	  		LicensePage lPage = (LicensePage)binding.getValue(context);
 	  		lPage.setInitialValues(this.formName, getMeleteResource());
 
 	  		// for incomplete add action and resource file is saved through intermediate save
 	  		try
 	  		{
 	  			if(("notype").equals(this.section.getContentType()) && this.meleteResource != null && this.meleteResource.getResourceId() != null)
 	  			{
 	  				if(this.meleteResource.getResourceId().indexOf("Section_")!= -1)
 	  					this.section.setContentType("typeEditor");
 	  				else this.section.setContentType("typeUpload");
 	  				sectionService.insertSectionResource(this.section, this.meleteResource);
 	  			}
 	  		} catch(Exception ex) {}
 		}
 		else
 		{
 			setMeleteResource(null);
 			setLicenseCodes(null);
 		}
 		setSuccess(false);
 
 		ValueBinding binding = Util.getBinding("#{authorPreferences}");
 		AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(context);
 		preferencePage.setEditorFlags();
 
 		// if fck editor then push advisors and other config values
 		if (this.section.getContentType().equals("typeEditor") && preferencePage.isShouldRenderFCK())
 		{
 			setFCKCollectionAttrib();
 		}
 		if (this.section.getContentType().equals("typeEditor") && preferencePage.isShouldRenderSferyx())
 			preferencePage.setDisplaySferyx(true);
 
 		return "success";
 	}
 
 	/**
 	 * @return sizeWarning render sizeWarning message if this flag is true
 	 */
 	public boolean getSizeWarning()
 	{
 		return this.sizeWarning;
 	}
 
 	/**
 	 * @param sizeWarning
 	 *        to set sizeWarning to true if section save is successful.
 	 */
 	public void setSizeWarning(boolean sizeWarning)
 	{
 		this.sizeWarning = sizeWarning;
 	}
 /**
  * set resource data.
  * @param resourceId
  * The resource Id
  */
 	private void setContentResourceData(String resourceId)
 	{
 
 		try
 		{
 			if (resourceId != null)
 			{
 				ContentResource cr = getMeleteCHService().getResource(resourceId);
 				if (cr == null) return;
 
 				if (cr.getContentType().equals(MeleteCHService.MIME_TYPE_EDITOR))
 					this.contentEditor = new String(cr.getContent());
 				else if (cr.getContentType().equals(MeleteCHService.MIME_TYPE_LINK)) setCurrLinkUrl(new String(cr.getContent()));
 
 				this.secResourceName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 				uploadFileName = this.secResourceName;
 				this.secResourceDescription = cr.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
 				containCollectionId = cr.getContainingCollection().getId();
 			}
 		}
 		catch (Exception e)
 		{
 			logger.debug("error in reading resource properties in edit section" + e);
 		}
 
 	}
 
 	/**
 	 * Get composed section content.
 	 * return content editor
 	 */
 	public String getContentEditor()
 	{
 		if (logger.isDebugEnabled()) logger.debug("EDIT GETCONTENT EDITOR CALLED");
 		return this.contentEditor;
 	}
 
 	/**
 	 * Checks if section has a content type.If not show the dropdown menu to pick a content type.
 	 * @return
 	 */
 	public boolean isShouldRenderContentTypeSelect()
 	{
 		// shouldRenderContentTypeSelect = false;
 		if (shouldRenderContentTypeSelect == false && this.section != null && this.section.getContentType().equals("notype"))
 		{
 			shouldRenderContentTypeSelect = true;
 			shouldRenderUpload = false;
 			shouldRenderEditor = false;
 			shouldRenderLink = false;
 			shouldRenderNotype = true;
 		}
 		return shouldRenderContentTypeSelect;
 	}
 
 	/**
 	 * Delete the selected resource.
 	 * @param evt
 	 * ActionEvent
 	 */
 	public void selectedResourceDeleteAction(ActionEvent evt)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
     	MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(ctx);
      	
 		UICommand cmdLink = (UICommand)evt.getComponent();
 
 		List<?> cList = cmdLink.getChildren();
 		if(cList == null || cList.size() <1) return;
 		UIParameter param1 = (UIParameter) cList.get(0);
 		String delRes_id = (String) param1.getValue();
 		
 		binding =Util.getBinding("#{deleteResourcePage}");
 		DeleteResourcePage delResPage = (DeleteResourcePage) binding.getValue(ctx);
 		delResPage.resetValues();
 		if(section.getContentType().equals("typeUpload"))
 			delResPage.setFromPage("editContentUploadServerView");
 		else if (section.getContentType().equals("typeLink"))
 			delResPage.setFromPage("editContentLinkServerView");
 		else if (section.getContentType().equals("typeLTI"))
 			delResPage.setFromPage("editContentLTIServerView");
 
 		delResPage.setResourceName(meleteCHService.getDisplayName(delRes_id));
 		delResPage.processDeletion(delRes_id, mPage.getCurrentUser().getId());
 		return;
 	}
 
 /**
  * Save the section.Validates modality and license required fields. Track section edit event.
  */
 	public String saveHere()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		// validation 1: modality is required.
 		if (!validateModality())
 		{
 			String errMsg = bundle.getString("add_section_modality_reqd");
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "add_section_modality_reqd", errMsg));
 			return "failure";
 		}
 
 		ValueBinding binding =  Util.getBinding("#{licensePage}");
 		 LicensePage lPage = (LicensePage)binding.getValue(context);
 		 lPage.setFormName(formName);
 		try
 		{
 			// validation 2a: if content is provided then check for license and year lengths
 			if (!section.getContentType().equals("notype") && !lPage.getLicenseCodes().equals(LicensePage.NO_CODE))
 			{
 				lPage.validateLicenseLengths();
 			}
 			// validation 2: if content is provided then check for copyright license
 			if (!section.getContentType().equals("notype") && lPage.getLicenseCodes().equals(LicensePage.Copyright_CODE))
 			{
 				lPage.checkForRequiredFields();
 			}
 
 			// validation 3: if upload a new file check fileName format -- move to uploadSectionContent()
 			// validation 3-1: if typeEditor and saved by sferyx then check for error messages
 			if (section.getContentType().equals("typeEditor"))
 			{
 				binding = Util.getBinding("#{addResourcesPage}");
 				AddResourcesPage resourcesPage = (AddResourcesPage) binding.getValue(context);
 				HashMap<String,ArrayList<String>> save_err = resourcesPage.getHm_msgs();
 				logger.debug("hashmap in editsectionpage is " + save_err);
 				String errKey = section.getSectionId().toString() + "-" + getCurrUserId();
 				if(save_err != null && !save_err.isEmpty() && save_err.containsKey(errKey))
 				{
 					ArrayList<String> errs = save_err.get(errKey);
 					for(String err:errs)
 					{
 					String errMsg = resourcesPage.getMessageText(err);
 					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, err, errMsg));
 					}
 					resourcesPage.removeFromHm_Msgs(errKey);
 					return "failure";
 				}
 			}
 
 			// save section
 			if (logger.isDebugEnabled()) logger.debug("EditSectionpage:save section" + section.getContentType());
 
 			if (section.getContentType().equals("notype"))
 			{
 				meleteResource = null;
 				sectionService.editSection(section);
 			}
 			else{
 				shouldRenderContentTypeSelect = false;
 				// step 1: check if new resource content or existing resource is edited
 				try
 				{
 					binding = Util.getBinding("#{authorPreferences}");
 	    			AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(context);
 
 	    			if ("typeEditor".equals(section.getContentType()) && preferencePage.isShouldRenderSferyx())
 	    			{
 	    				// get secResource object
 	    				secResource = sectionService.getSectionResourcebyId(section.getSectionId().toString());
 	    				logger.debug("after fetching section resource object is:" + secResource.getResource() + secResource.getSectionId());
 	    				meleteResource.setResourceId(secResource.getResource().getResourceId());
 	    				section.setSectionResource(secResource);
 	    				// refresh contentEditor
 	    				ContentResource cr = getMeleteCHService().getResource(secResource.getResource().getResourceId());
 	    				if (cr != null) this.contentEditor = new String(cr.getContent());
 	    			}
 	    			else if ("typeEditor".equals(section.getContentType()) && preferencePage.isShouldRenderFCK())
 	    			{
 	       				// type change from type upload or typeLink to compose then create section xxx.html file
 	    				if (meleteResource != null && meleteResource.getResourceId() != null && meleteResource.getResourceId().length() != 0 &&
 	    					meleteResource.getResourceId().indexOf("/private/meleteDocs/") != -1 && meleteResource.getResourceId().indexOf("/uploads/") != -1)
 	    				{
 		   					throw new MeleteException("section_html_null");
 	    				}
 	    				// no type to editor
 	    				if (meleteResource != null && meleteResource.getResourceId() != null && meleteResource.getResourceId().length() == 0)
 	    				{
 	      					throw new MeleteException("section_html_null");
 	    				}
 	    				getMeleteCHService().checkResource(meleteResource.getResourceId());
     					editMeleteCollectionResource(meleteResource.getResourceId());
 	    			}
 	    			//The condition below was put in to handle ME-639
 	    			else
 	    			{
 	    				if (meleteResource != null && meleteResource.getResourceId() != null && meleteResource.getResourceId().trim().length() != 0)
 	    				{
 	    					if (logger.isDebugEnabled()) logger.debug("Ist step of edit - check meleteResource" + meleteResource.getResourceId());
 	    					// validation 4: check link url title
 	    					if (section.getContentType().equals("typeLink") && (secResourceName == null || secResourceName.trim().length() == 0))
 	    						throw new UserErrorException("URL_title_reqd");
 	    					getMeleteCHService().checkResource(meleteResource.getResourceId());
 	    					editMeleteCollectionResource(meleteResource.getResourceId());
 	    				}
 	    				else
 	    				{
 	    					//resource is removed
 	    					if (logger.isDebugEnabled()) logger.debug("Resource ID is null i.e resource is removed");
 	    					editMeleteCollectionResource( null);
 	    					meleteResource = null;
 	    					    					
 	    					// delete existing record from section_resource table
 	    					sectionService.deleteSectionResourcebyId(section.getSectionId().toString());
 	    					//change section type to notype
 	    					section.setContentType("notype");
 	    					//insert just into section table	    					
 	    					sectionService.editSection(section);
 	    					//Track the event
 	    					EventTrackingService.post(EventTrackingService.newEvent("melete.section.edit", ToolManager.getCurrentPlacement().getContext(), true));
 
 	    					return "success";
 	    				}
 	    			}
 
 				}
 				catch (Exception e)
 				{
 					// resource is not there when no content type is choosen
 					if (logger.isDebugEnabled()) logger.debug("resource is new i.e. coming from notype content");
 
 					String addCollId = null;
 					if("typeEditor".equals(section.getContentType()))
 						addCollId = getMeleteCHService().getCollectionId(getCurrentCourseId(),section.getContentType(), module.getModuleId());
 					else addCollId = getMeleteCHService().getUploadCollectionId(getCurrentCourseId());
 
 					logger.debug("addCollId is:" + addCollId);
 					String newResourceId = addResourceToMeleteCollection(addCollId);
 					meleteResource.setResourceId(newResourceId);
 					if (logger.isDebugEnabled()) logger.debug("new resource id" + newResourceId + meleteResource);
 					/* here create association and insert new resource */
 					sectionService.insertSectionResource(section, meleteResource);
 				}
 
 				// step 3: edit license information
 				if(meleteResource != null && meleteResource.getResourceId() != null && meleteResource.getResourceId().length() != 0)
 				{
 				meleteResource = lPage.processLicenseInformation(meleteResource);
 				sectionService.updateResource(meleteResource);
 				}
 				sectionService.insertSectionResource(section, meleteResource);
 			}
 
 			//Track the event
 			EventTrackingService.post(EventTrackingService.newEvent("melete.section.edit", ToolManager.getCurrentPlacement().getContext(), true));
 
 			return "success";
 		}
 		catch (UserErrorException uex)
 		{
 			String errMsg = bundle.getString(uex.getMessage());
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, uex.getMessage(), errMsg));
 			return "failure";
 		}
 		catch (MeleteException mex)
 		{
 			logger.debug("error in updating section " + mex.toString());
 			String errMsg = bundle.getString(mex.getMessage());
 			// uncomment it after sferyx brings uploadfile limit param
 			/*if(mex.getMessage().equals("embed_image_size_exceed"))
 			{
 				errMsg = errMsg.concat(ServerConfigurationService.getString("content.upload.max", "0"));
 				errMsg = errMsg.concat(bundle.getString("embed_image_size_exceed1"));
 			}*/
 			if(mex.getMessage().equals("embed_image_size_exceed2"))
 			{
 				errMsg = errMsg.concat(ServerConfigurationService.getString("content.upload.max", "0"));
 				errMsg = errMsg.concat(bundle.getString("embed_image_size_exceed2-1"));
 			}
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mex.getMessage(), errMsg));
 			return "failure";
 		}
 		catch (Exception ex)
 		{
 			logger.debug("error in updating section " + ex.toString());
 			String errMsg = bundle.getString("add_section_fail");
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "add_section_fail", errMsg));
 		//	ex.printStackTrace();
 			return "failure";
 		}
 
 	}
 
 	/**
 	 * instantiates saving of a section. if file needs to be uploaded, upload file. Validate content. save section. If sucess set success flag and
 	 * show the success message. if any error, display error message to the user.
 	 */
 	public String save()
 	{
 		setSuccess(false);
 		if (!saveHere().equals("failure"))
 		{
 			setSuccess(true);
 			return "editmodulesections";
 		}
 		return "editmodulesections";
 	}
 	
 	/**
      * For top mode bar clicks, auto save section
      * Returns # if save is success else stay on same page to correct error
      */
 	public String autoSave()
 	{
 		setSuccess(false);
 		if (!saveHere().equals("failure"))
 		{
 			setSuccess(true);
 			// clear return url
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 			mPage.setNavigateCM(null);
 			return "#";
 		}
 		return "editmodulesections";
 	}
 
 	/**
 	 * save the section and create next section.
 	 */
 	public String saveAndAddAnotherSection()
 	{
 		setSuccess(false);
 			if(!saveHere().equals("failure"))
 			{
 				setSuccess(true);
 			}
 			else return "editmodulesections";
 
 
 		// create new instance of section model
 		setSection(null);
 		resetSectionValues();
 		setSizeWarning(false);
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		sessionMap.put("currModule", module);
 
 		ValueBinding binding = Util.getBinding("#{authorPreferences}");
 		AuthorPreferencePage authPage = (AuthorPreferencePage) binding.getValue(context);
 		authPage.setEditorFlags();
 
 		// create new section
 		addBlankSection();
 
 		return "editmodulesections";
 	}
 
 	/**
 	 * Save the section and navigate to author list page.
 	 */
 	public String Finish()
 	{
 		setSuccess(false);
 
 			if(!saveHere().equals("failure"))
 			{
 			setSuccess(true);
 			} else return "editmodulesections";
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 
 		MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 		if (mPage.getNavigateCM() != null)
 		{
 			return "coursemap";
 		}
 		// un-comment to show success message again.
 		/*FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		String successMsg = bundle.getString("edit_section_confirm");
 		FacesMessage msg = new FacesMessage("Info message", successMsg);
 		msg.setSeverity(FacesMessage.SEVERITY_INFO);
 		context.addMessage(null, msg);*/
 
 		binding =Util.getBinding("#{listAuthModulesPage}");
 		ListAuthModulesPage listPage = (ListAuthModulesPage) binding.getValue(context);
         listPage.resetValues();
 		return "list_auth_modules";
 
 	}
 
 	/**
 	 * Preview the content. Save section.
 	 */
 	public String getPreviewPage()
 	{
 		setSuccess(false);
 		if(!saveHere().equals("failure"))
 		{
 			setSuccess(true);
 		}
 		else return "editmodulesections";
 
 		contentWithHtml = false;
 		this.previewContentData = null;
 
 		if (meleteResource != null && meleteResource.getResourceId() != null)
 		   {
 			   if (this.section.getContentType().equals("typeEditor"))
 			   {
 				   this.previewContentData = contentEditor;
 				   if(Util.FindNestedHTMLTags(contentEditor))
 				   {
 					   this.previewContentData = getMeleteCHService().getResourceUrl(meleteResource.getResourceId());
 					   contentWithHtml = true;
 				   }
 			   }
 			   else this.previewContentData = getMeleteCHService().getResourceUrl(meleteResource.getResourceId());
 		   }
 		return "editpreview";
 	}
 
 	/**
 	 * Navigate back to edit page from preview.
 	 * @return
 	 */
 	public String returnBack()
 	{
 		return "editmodulesections";
 	}
 
 	/**
 	 * reset values
 	 */
 	public void resetSectionValues()
 	{
 		shouldRenderContentTypeSelect = false;
 		currLinkUrl = null;
 		editSelection = null;
 		//m_selected_license = null;
 		selectedResourceName = null;
 		selectedResourceDescription = null;
 		selectedResource = null;
 		hasNext = null;
 		hasPrev = null;
 		super.resetSectionValues();
 	}
 
 	/**
 	 * Navigate to edit page
 	 * @return
 	 */
 	public String redirectLinktoEdit()
 	{
 		return "editmodulesections";
 		// return "#";
 	}
 
 	/**
 	 * on clicking link 2 me the page navigates back
 	 */
 	public String redirectLink()
 	{
 		return "editContentUploadServerView";
 		// return "#";
 	}
 
 	/**
 	 * Navigate page to delete a resource
 	 * @return
 	 */
 	public String redirectDeleteLink()
 	{
 		 return "delete_resource";
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
 	 * @return Returns the editSelection.
 	 */
 	public String getEditSelection()
 	{
 		// if(editSelection == null) editSelection = "none";
 		return editSelection;
 	}
 
 	/**
 	 * @param editSelection
 	 *        The editSelection to set.
 	 */
 	public void setEditSelection(String editSelection)
 	{
 		this.editSelection = editSelection;
 	}
 
 	/**
 	 * @return Returns the m_selected_license.
 	 */
 	/*public SectionResourceLicenseSelector getM_selected_license()
 	{
 		if (m_selected_license == null)
 		{
 			m_selected_license = new SectionResourceLicenseSelector();
 			m_selected_license.setInitialValues(this.formName, sectionService, getSelectedResource());
 		}
 		return m_selected_license;
 	}*/
 
 	/**
 	 * @param m_selected_license
 	 *        The m_selected_license to set.
 	 */
 	/*public void setM_selected_license(SectionResourceLicenseSelector m_selected_license)
 	{
 		this.m_selected_license = m_selected_license;
 	}*/
 
 	/**
 	 * navigate to upload screen.
 	 */
 	public String gotoServerView()
 	{
 		selectedResourceName = null;
 		selectedResourceDescription = null;
 		selResourceIdFromList = null;
 		shouldRenderServerResources = false;
 		shouldRenderLocalUpload = false;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ValueBinding binding =Util.getBinding("#{listResourcesPage}");
 		ListResourcesPage listResPage = (ListResourcesPage) binding.getValue(ctx);
 		listResPage.setFromPage("editContentUploadServerView");
 		listResPage.resetValues();
 		return "editContentUploadServerView";
 	}
 
 	/**
 	 * Set the selected file as resource selected for the section.
 	 * @return
 	 */
 	public String setServerFile()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		if (logger.isDebugEnabled()) logger.debug("selected resource properties" + selectedResourceName + " , " + selectedResourceDescription);
 		selResourceIdFromList = getSelResourceIdFromList();
 
 		try
 		{
 			if (selResourceIdFromList == null)
 			{
 				String res_mime_type = uploadSectionContent("file1");
 				if (logger.isDebugEnabled()) logger.debug("new names for upload content is" + res_mime_type);
 				// if new resource i.e. a local file is choosen is added
 				if (res_mime_type != null)
 				{
 					secResourceDescription = "";
                     setLicenseInfo();
 					ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(false, secResourceName, secResourceDescription);
 
 			//		if (containCollectionId == null) containCollectionId = getMeleteCHService().getUploadCollectionId(getCurrentCourseId());
 					containCollectionId = getMeleteCHService().getUploadCollectionId(getCurrentCourseId());
 					String newResourceId = getMeleteCHService().addResourceItem(secResourceName, res_mime_type, containCollectionId,
 							getSecContentData(), res);
 					selectedResource = new MeleteResource();
 					selectedResource.setResourceId(newResourceId);
 					sectionService.insertResource(selectedResource);
 					secResourceName = getDisplayName(newResourceId);
 				}
 				else
 				{
 					String errMsg = bundle.getString("select_or_cancel");
 					ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "select_or_cancel", errMsg));
 					return "editContentUploadServerView";
 				}
 			}
 			else
 			{
 				logger.debug("selected existing file");
 				/*secResourceName = selectedResourceName;
 				secResourceDescription = selectedResourceDescription;*/
 				processSelectedResource(selResourceIdFromList);
 				//setLicenseCodes(m_selected_license);
 			}
 			meleteResource = selectedResource;
 			ctx.renderResponse();
 		}
 		catch (MeleteException me)
 		{
 			logger.debug("error in set server file for edit section content" + me.toString());
 //			me.printStackTrace();
 			String errMsg = bundle.getString(me.getMessage());
 			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", errMsg));
 			return "editContentUploadServerView";
 		}
 		catch (Exception e)
 		{
 			if (logger.isDebugEnabled()) {
 			logger.debug("error in set server file for edit section content");
 			e.printStackTrace();
 			}
 			String errMsg = bundle.getString("add_section_fail");
 			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", errMsg));
 			return "editContentUploadServerView";
 		}
 		return "editmodulesections";
 	}
 
 	/**
 	 * Navigate to select a link resource.
 	 * @return
 	 */
 	public String gotoServerLinkView()
 	{
 		selectedResourceName = null;
 		selectedResourceDescription = null;
 		selResourceIdFromList = null;
 		shouldRenderServerResources = false;
 		setLinkUrl(null);
 		newURLTitle ="";
 		if(displayCurrLink == null)	newURLTitle = secResourceName;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ValueBinding binding =Util.getBinding("#{listResourcesPage}");
 		ListResourcesPage listResPage = (ListResourcesPage) binding.getValue(ctx);
 		listResPage.setFromPage("editContentLinkServerView");
 		listResPage.resetValues();
 		return "editContentLinkServerView";
 	}
 
 	/**
 	 * Navigate to LTI resources list
 	 * @return
 	 */
 	public String gotoServerLTIView()
 	{
 		gotoServerLinkView();
 		setLTIUrl(null);
 		setLTIPassword(null);
 		setLTIKey(null);
 		setLTIDisplay("Basic");
 		setLTIDescriptor(null);
 		newURLTitle ="";
 		FacesContext ctx = FacesContext.getCurrentInstance();
 	  	ValueBinding binding =Util.getBinding("#{listResourcesPage}");
 		ListResourcesPage listResPage = (ListResourcesPage) binding.getValue(ctx);
 		listResPage.setFromPage("editContentLTIServerView");
 		listResPage.resetValues();
 		return "editContentLTIServerView";
 	}
 
 	/**
 	 * Associate section with the user selected resource item.
 	 * @return
 	 */
 	public String setServerUrl()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		String errMsg = null;
 		if (logger.isDebugEnabled()) logger.debug("selected resource properties" + selectedResourceName + " , " + selectedResourceDescription);
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
 					return "editContentLinkServerView";
 				}
 				Util.validateLink(getLinkUrl());
 				/*
 				 * if(!check.equals("OK")) { errMsg = bundle.getString("add_section_bad_url"); ctx.addMessage (null, new
 				 * FacesMessage(FacesMessage.SEVERITY_ERROR,"add_section_bad_url",errMsg)); return "editContentLinkServerView"; }
 				 */
 				/*secResourceName = "";
 				secResourceDescription = "";*/
 				setLicenseInfo();
 				if(newURLTitle == null || newURLTitle.length() == 0)
 				{
 					errMsg = bundle.getString("URL_title_reqd");
 					ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "URL_title_reqd", errMsg));
 					return "editContentLinkServerView";
 				}
 				secResourceName = newURLTitle;
 				createLinkUrl();
 				String res_mime_type = MeleteCHService.MIME_TYPE_LINK;
 				ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(false, secResourceName, secResourceDescription);
 			//	if (containCollectionId == null) containCollectionId = getMeleteCHService().getUploadCollectionId(getCurrentCourseId());
 				containCollectionId = getMeleteCHService().getUploadCollectionId(getCurrentCourseId());
 				String newResourceId = getMeleteCHService().addResourceItem(secResourceName, res_mime_type, containCollectionId, getSecContentData(),
 						res);
 				selectedResource = new MeleteResource();
 				selectedResource.setResourceId(newResourceId);
 				sectionService.insertResource(selectedResource);
 				secResourceName = getDisplayName(newResourceId);
 				currLinkUrl = getLinkContent(newResourceId);
 			}
 			else
 			{
 				processSelectedResource(selResourceIdFromList);
 				// pick from server list
 				/*secResourceName = selectedResourceName;
 				secResourceDescription = selectedResourceDescription;
 				//setLicenseCodes(m_selected_license);
 				ContentResource cr = getMeleteCHService().getResource(selResourceIdFromList);
 				if(cr.getContentLength() > 0)
 					currLinkUrl = new String(cr.getContent());*/
 			}
 			setLinkUrl(currLinkUrl);
 			meleteResource = selectedResource;
 			ctx.renderResponse();
 		}
 		catch (UserErrorException uex)
 				{
 					if (uex.getMessage() != null)
 					{
 					  errMsg = bundle.getString(uex.getMessage());
 					  ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, uex.getMessage(), errMsg));
 					}
 					return "editContentLinkServerView";
 		}
 		catch (Exception e)
 		{
 			if (e.getMessage() != null)
 			{
 			  errMsg = bundle.getString(e.getMessage());
 			  ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), errMsg));
 			}
 			return "editContentLinkServerView";
 		}
 		return "editmodulesections";
 	}
 
 	/**
 	 * Associate section with LTI resource selected.
 	 * @return
 	 */
 	public String setServerLTI()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		String errMsg = null;
 		if (logger.isDebugEnabled()) logger.debug("selected resource properties" + selectedResourceName + " , " + selectedResourceDescription);
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
 					return "editContentLTIServerView";
 				}
 				setLicenseInfo();
 				if(newURLTitle == null || newURLTitle.length() == 0)
 				{
 					errMsg = bundle.getString("URL_title_reqd");
 					ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "URL_title_reqd", errMsg));
 					return "editContentLTIServerView";
 				}
 				secResourceName = newURLTitle;
 				createLTIDescriptor();
 				String res_mime_type = MeleteCHService.MIME_TYPE_LTI;
 				ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(false, secResourceName, secResourceDescription);
 		//		if (containCollectionId == null) containCollectionId = getMeleteCHService().getUploadCollectionId(getCurrentCourseId());
 				containCollectionId = getMeleteCHService().getUploadCollectionId(getCurrentCourseId());
 				String newResourceId = getMeleteCHService().addResourceItem(secResourceName, res_mime_type, containCollectionId, getSecContentData(),
 						res);
 				selectedResource = new MeleteResource();
 				selectedResource.setResourceId(newResourceId);
 				sectionService.insertResource(selectedResource);
 				currLinkUrl = secResourceName;
 			}
 			else
 			{
 				processSelectedResource(selResourceIdFromList);
 
 				// pick from server list
 				/*secResourceName = selectedResourceName;
 				secResourceDescription = selectedResourceDescription;
 				//setLicenseCodes(m_selected_license);
 				ContentResource cr = getMeleteCHService().getResource(selResourceIdFromList);
 				if(cr.getContentLength() > 0)
 					currLinkUrl = new String(cr.getContent());*/
 			}
 			meleteResource = selectedResource;
 			ctx.renderResponse();
 		}
 		catch (Exception e)
 		{
 			logger.debug("error in set server url for edit section content" + errMsg);
 		//	e.printStackTrace();
 			if (e.getMessage() != null)
 			{
 			  errMsg = bundle.getString(e.getMessage());
 			  ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), errMsg));
 			}
 			return "editContentLTIServerView";
 		}
 		return "editmodulesections";
 	}
 
 	/**
 	 * Cancel selected file.
 	 * @return
 	 */
 	public String cancelServerFile()
 	{
 		selectedResource = null;
 		selResourceIdFromList = null;
 		setLinkUrl(currLinkUrl);
 		selectedResourceName = null;
 		selectedResourceDescription = null;
 		return "editmodulesections";
 	}
 
 	/**
 	 * @return Returns the shouldRenderServerResources.
 	 */
 	public boolean isShouldRenderServerResources()
 	{
 		return shouldRenderServerResources;
 	}
 
 	/**
 	 * @param shouldRenderServerResources
 	 *        The shouldRenderServerResources to set.
 	 */
 	public void setShouldRenderServerResources(boolean shouldRenderServerResources)
 	{
 		this.shouldRenderServerResources = shouldRenderServerResources;
 	}
 
 	/**
 	 * @return Returns the shouldRenderLocalUpload.
 	 */
 	public boolean isShouldRenderLocalUpload()
 	{
 		return shouldRenderLocalUpload;
 	}
 
 	/**
 	 * @param shouldRenderLocalUpload
 	 *        The shouldRenderLocalUpload to set.
 	 */
 	public void setShouldRenderLocalUpload(boolean shouldRenderLocalUpload)
 	{
 		this.shouldRenderLocalUpload = shouldRenderLocalUpload;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isHasNext()
 	{
 		SectionObjService nextSection = null;
 		if (hasNext == null)
 		{
 			hasNext = false;
 			try
 			{
 				nextSection = sectionService.getNextSection(section.getSectionId().toString(), module.getSeqXml());
 
 				if (nextSection != null) hasNext = true;
 			}
 			catch (Exception e)
 			{
 			}
 		}
 		return hasNext.booleanValue();
 	}
 
 	public String editNextSection()
 	{
 		setSuccess(false);
 		if (!saveHere().equals("failure"))
 		{
 			setSuccess(true);
 		}
 		else
 			return "editmodulesections";
 
 		// find Next Section/subsection
 		SectionObjService nextSection = null;
 		try
 		{
 			nextSection = sectionService.getNextSection(section.getSectionId().toString(), module.getSeqXml());
 		}
 		catch (Exception e)
 		{
 			logger.debug("error in finding next so probably this is the last one");
 			//e.printStackTrace();
 			return cancel();
 		}
 		// reset section model to refresh and set to next
 		setSection(null);
 		resetSectionValues();
 		setSizeWarning(false);
 		if(nextSection != null)
 			setEditInfo(nextSection);
 		else return cancel();
 
 		return "editmodulesections";
 	}
 
 	public boolean isHasPrev()
 	{
 		SectionObjService prevSection = null;
 		if (hasPrev == null)
 		{
 			hasPrev = false;
 			try
 			{
 				prevSection = sectionService.getPrevSection(section.getSectionId().toString(), module.getSeqXml());
 				if(prevSection != null)hasPrev = true;
 			}
 			catch (Exception e)
 			{
 			}
 		}
 		return hasPrev.booleanValue();
 	}
 
 	public String editPrevSection()
 	{
 		setSuccess(false);
 		if (!saveHere().equals("failure"))
 		{
 			setSuccess(true);
 		}
 		else return "editmodulesections";
 
 		// find Next Section/subsection
 		SectionObjService prevSection = null;
 		try
 		{
 			prevSection = sectionService.getPrevSection(section.getSectionId().toString(), module.getSeqXml());
 		}
 		catch (Exception e)
 		{
 			logger.debug("error in finding prev section to edit");
 			//e.printStackTrace();
 			return cancel();
 		}
 		// reset section model to refresh and set to next
 		setSection(null);
 		resetSectionValues();
 		setSizeWarning(false);
 		if(prevSection != null)
 			setEditInfo(prevSection);
 		else return cancel();
 
 		return "editmodulesections";
 	}
 
 	public String goTOC()
 	{
 		setSuccess(false);
 		if (!saveHere().equals("failure"))
 		{
 			setSuccess(true);
 		}
 		else
 			return "editmodulesections";
 
 		//clear return url
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 		MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 		mPage.setNavigateCM(null);
 		
 		// reset section model to refresh
 		setSection(null);
 		resetSectionValues();
 		setSizeWarning(false);
 		return cancel();
 	}
 
 	public String getDataUrl()
 	{
 
 		String rUrl = null;
 		if (this.section == null) return null;
 		SectionResource secRes = (SectionResource)this.section.getSectionResource();
 		String resourceId = null;
 		if (secRes != null && (secRes.getResource() != null))
 		{
 			resourceId = secRes.getResource().getResourceId();
 		}
 
 		if (resourceId != null)
 		{
 			try
 			{
 					rUrl = getMeleteCHService().getResourceUrl(resourceId);
 			}catch(Exception e) {
 				// do nothing
 			}
 		}
 	  return rUrl;
 	}
 
 	public String gotoMyBookmarks()
 	{
 
 		setSuccess(false);
 		if(!saveHere().equals("failure"))
 		{
 			setSuccess(true);
 		}
 		else return "editmodulesections";
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{bookmarkPage}");
 		BookmarkPage bmrkPage = (BookmarkPage) binding.getValue(context);
 
 		return bmrkPage.gotoMyBookmarks("editmodulesections", module.getModuleId());
 	}
 
 	public String saveAndAddBookmark()
 	{
 
 		setSuccess(false);
 		if(!saveHere().equals("failure"))
 		{
 			setSuccess(true);
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{bookmarkPage}");
 			BookmarkPage bmrkPage = (BookmarkPage) binding.getValue(context);
 			bmrkPage.setSectionId(this.section.getSectionId().toString());
 		}
 		return "editmodulesections";
 
 	}
 
 	/*
 	 *  click of add creates a blank section
 	 */
 	public void addBlankSection()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		Map sessionMap = context.getExternalContext().getSessionMap();
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 		MeleteSiteAndUserInfo info = (MeleteSiteAndUserInfo) binding.getValue(context);
 		
 		try
 		{
 			Section s = new Section();
 			s.setContentType("notype");
 			s.setTextualContent(true);
 			// user info from session
 			s.setCreatedByFname(info.getCurrentUser().getFirstName());
 			s.setCreatedByLname(info.getCurrentUser().getLastName());
 			//reset flags
 			shouldRenderEditor=false;
 			shouldRenderLink=false;
 			shouldRenderLTI=false;
 			shouldRenderUpload=false;
 			shouldRenderNotype = true;
 			int mId = module.getModuleId().intValue();
 			logger.debug("mId in blank section" + mId);
			Integer newSectionId = sectionService.insertSection(module,s);
			s.setSectionId(newSectionId);
 
 			// refresh module
 			this.module = moduleService.getModule(mId);
 			sessionMap.put("currModule",module);
 			s.setModule(module);
 
 			// set edit page for this section
 			setEditInfo(s);
 		}
 		catch(Exception ex)
 		{
 			// do nothing
 		}
 	}	
 }
