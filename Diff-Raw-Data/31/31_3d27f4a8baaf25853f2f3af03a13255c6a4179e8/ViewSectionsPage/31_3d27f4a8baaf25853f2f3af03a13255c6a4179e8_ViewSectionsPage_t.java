 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009,2010,2011 Etudes, Inc.
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
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.faces.application.Application;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIParameter;
 import javax.faces.component.html.HtmlCommandLink;
 import javax.faces.component.html.HtmlOutputText;
 import javax.faces.component.html.HtmlPanelGroup;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.ActionEvent;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.MeleteCHService;
 import org.etudes.api.app.melete.MeleteSecurityService;
 import org.etudes.api.app.melete.ModuleObjService;
 import org.etudes.api.app.melete.ModuleService;
 import org.etudes.api.app.melete.SectionObjService;
 import org.etudes.api.app.melete.SectionResourceService;
 import org.etudes.api.app.melete.SectionService;
 import org.etudes.api.app.melete.SectionTrackViewObjService;
 import org.etudes.simpleti.SakaiSimpleLTI;
 import org.etudes.util.HtmlHelper;
 import org.imsglobal.simplelti.SimpleLTIUtil;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.event.cover.EventTrackingService;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.util.ResourceLoader;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 public class ViewSectionsPage implements Serializable
 {
 
 	public ModuleObjService module;
 	public SectionObjService section;
 	private Boolean autonumber;
 	// added to reduce queries
 	private String contentLinkUrl;
 	private Boolean contentWithHtml;
 
 	private String emptyString = "";
 	private String linkName;
 
 	private MeleteCHService meleteCHService;
 	/** identifier field */
 	private int moduleId;
 	private int moduleSeqNo;
 	private ModuleService moduleService;
 	private int nextSecId;
 	private int nextSeqNo;
 
 	private String nullString = null;
 	private int prevSecId;
 
 	private String sectionContentType;
 	private String sectionDisplaySequence;
 
 	private int sectionId;
 
 	private SectionService sectionService;
 
 	private String sectionTrackDateStr;
 	private org.w3c.dom.Document subSectionW3CDom;
 	private String typeEditor;
 	private String typeLink;
 	private String typeUpload;
 
 	/** Dependency: The logging service. */
 	protected Log logger = LogFactory.getLog(ViewSectionsPage.class);
 	/** Dependency: The Melete Security service. */
 	protected MeleteSecurityService meleteSecurityService;
 	String courseId;
 	String userId;
 
 	public ViewSectionsPage()
 	{
 		courseId = null;
 		userId = null;
 		contentLinkUrl = null;
 		contentWithHtml = null;
 	}
 
 	/**
 	 * Get editor content for typeEditor sections
 	 * 
 	 * @return String with editor content
 	 */
 	public String getContent()
 	{
 		contentWithHtml = false;
 		ContentResource resource = getContentResource();
 		if (resource == null) return "";
 		String str = null;
 		try
 		{
 			byte[] rsrcArray = resource.getContent();
 			str = new String(rsrcArray);
 
 			if (Util.FindNestedHTMLTags(str))
 			{
 				contentWithHtml = true;
 				return "";
 			}
 
 			// strip MS comments and bogus links
 			str = HtmlHelper.stripComments(str);
 			// strip bad link and meta tags
 			str = HtmlHelper.stripLinks(str);
 		}
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 			return "";
 		}
 		return str;
 
 	}
 
 	/**
 	 * Get content of typeLink sections
 	 * 
 	 * @return String of link entered
 	 */
 	public String getContentLink()
 	{
 		String url = null;
 		if (this.section == null) return null;
 		if (contentLinkUrl == null)
 		{
 			SectionResourceService secRes = this.section.getSectionResource();
 			String resourceId = null;
 			if (secRes != null && (secRes.getResource() != null))
 			{
 				resourceId = secRes.getResource().getResourceId();
 			}
 			ContentResource resource = null;
 
 			if (resourceId != null && resourceId.length() != 0)
 			{
 				try
 				{
 					resource = getMeleteCHService().getResource(resourceId);
 					setLinkName(resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
 
 					url = getMeleteCHService().getResourceUrl(resourceId);
 					if (logger.isDebugEnabled()) logger.debug("Resource url is " + url);
 					contentLinkUrl = url;
 				}
 				catch (Exception e)
 				{
 					url = null;
 					contentLinkUrl = null;
 					logger.debug(e.toString());
 				}
 			}
 			// when typeUpload or typeLink resource are not selected
 			else
 			{
 				url = null;
 				contentLinkUrl = null;
 			}
 		}
 		logger.debug("content link value send is" + contentLinkUrl);
 		return contentLinkUrl;
 	}
 
 	/**
 	 * Get content for LTI sections
 	 * 
 	 * @return String content of LTI sections
 	 */
 	public String getContentLTI()
 	{
 		ContentResource resource = getContentResource();
 		if (resource == null)
 		{
 			return "";
 		}
 		String str = null;
 		try
 		{
 			byte[] rsrcArray = resource.getContent();
 			if (rsrcArray == null)
 			{
 				if (logger.isDebugEnabled()) logger.debug("Resource has no content" + resource.getId());
 				return "";
 			}
 			str = new String(rsrcArray);
 		}
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 			return "";
 		}
 
 		// Check to see if we are doing a POST...
 		if (SimpleLTIUtil.isPostLaunch(str))
 		{
 			return getContentPost(str);
 		}
 
 		String context = ToolManager.getCurrentPlacement().getContext();
 
 		// TODO: Deal with POST!!! Return an iFrame
 		Properties props = SakaiSimpleLTI.doLaunch(str, context, resource.getId());
 
 		// The resource *insisted* on a POST
 		if (SimpleLTIUtil.isPostLaunch(props))
 		{
 			return getContentPost(str);
 		}
 
 		String htmltext = props.getProperty("htmltext");
 		if (htmltext != null)
 		{
 			return htmltext;
 		}
 
 		// htmltext not returned from launch
 		if (logger.isDebugEnabled()) logger.debug("Unable to get htmltext for " + resource.getId());
 		return "";
 	}
 
 	/**
 	 * @return typeEditor content
 	 */
 	public Boolean getcontentWithHtml()
 	{
 		if (contentWithHtml == null) getContent();
 		return contentWithHtml;
 	}
 
 	/**
 	 * @return empty string
 	 */
 	public String getEmptyString()
 	{
 		return emptyString;
 	}
 
 	/**
 	 * @return linkName
 	 */
 	public String getLinkName()
 	{
 		return linkName;
 	}
 
 	/**
 	 * @return Returns the meleteCHService.
 	 */
 	public MeleteCHService getMeleteCHService()
 	{
 		return meleteCHService;
 	}
 
 	/**
 	 * Get module. Also assign next sequence number, previous and next section id
 	 * 
 	 * @return module
 	 */
 	public ModuleObjService getModule()
 	{
 		Element secElement;
 		Node prevNode, nextNode;
 		String courseId = null;
 		FacesContext ctx = FacesContext.getCurrentInstance();
 
 		if (this.module == null)
 		{
 			try
 			{
 				if (courseId == null) courseId = getCourseId();
 				this.module = (ModuleObjService) getModuleService().getModule(this.moduleId);
 				this.nextSeqNo = getModuleService().getNextSeqNo(getUserId(), courseId, this.moduleSeqNo);
 				this.subSectionW3CDom = getModuleService().getSubSectionW3CDOM(this.module.getSeqXml());
 				secElement = subSectionW3CDom.getElementById(String.valueOf(this.sectionId));
 				prevNode = getPreviousNode(secElement);
 				if (prevNode != null)
 				{
 					this.prevSecId = Integer.parseInt(prevNode.getAttributes().getNamedItem("id").getNodeValue());
 				}
 				else
 				{
 					this.prevSecId = 0;
 				}
 				nextNode = getNextNode(secElement);
 				if (nextNode != null)
 				{
 					this.nextSecId = Integer.parseInt(nextNode.getAttributes().getNamedItem("id").getNodeValue());
 				}
 				else
 				{
 					this.nextSecId = 0;
 				}
 
 			}
 			catch (Exception e)
 			{
 				logger.debug(e.toString());
 			}
 		}
 		return this.module;
 	}
 
 	/**
 	 * @return module id
 	 */
 	public int getModuleId()
 	{
 		return moduleId;
 	}
 
 	/**
 	 * @return module sequence number
 	 */
 	public int getModuleSeqNo()
 	{
 		return this.moduleSeqNo;
 	}
 
 	/**
 	 * @return Returns the ModuleService.
 	 */
 	public ModuleService getModuleService()
 	{
 		return moduleService;
 	}
 
 	/**
 	 * @return next section id
 	 */
 	public int getNextSecId()
 	{
 		if (this.module == null) getModule();
 		return nextSecId;
 	}
 
 	/**
 	 * @return next seq no
 	 */
 	public int getNextSeqNo()
 	{
 		return nextSeqNo;
 	}
 
 	/**
 	 * @return null value
 	 */
 	public String getNullString()
 	{
 		return nullString;
 	}
 
 	/**
 	 * @return previous section id
 	 */
 	public int getPrevSecId()
 	{
 		if (this.module == null) getModule();
 		return prevSecId;
 	}
 
 	
 	/**
 	 * Returns section and sets display sequence of section
 	 * 
 	 * @return section
 	 */
 	public SectionObjService getSection()
 	{
 		try
 		{
 			if (this.section == null)
 			{
 				this.section = (SectionObjService) getSectionService().getSection(this.sectionId);
 				this.sectionDisplaySequence = getSectionService().getSectionDisplaySequence(this.section);
 			}
 		}
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 		}
 
 		return this.section;
 	}
 
 	/**
 	 * @return noType/typeEditor/typeLink/typeUpload
 	 */
 	public String getSectionContentType()
 	{
 		if (this.section != null && this.section.getContentType() != null)
 			sectionContentType = this.section.getContentType();
 		else
 			sectionContentType = "notype";
 
 		return sectionContentType;
 	}
 
 	/**
 	 * Get section and section display sequence
 	 * 
 	 * @return section display sequence
 	 */
 	public String getSectionDisplaySequence()
 	{
 
 		if (this.sectionDisplaySequence == null)
 		{
 			this.section = (SectionObjService) getSectionService().getSection(this.sectionId);
 			this.sectionDisplaySequence = getSectionService().getSectionDisplaySequence(this.section);
 		}
 		;
 		return this.sectionDisplaySequence;
 	}
 
 	/**
 	 * @return sectionId Section id
 	 */
 	public int getSectionId()
 	{
 		return this.sectionId;
 	}
 
 	/**
 	 * @return Returns the SectionService.
 	 */
 	public SectionService getSectionService()
 	{
 		return sectionService;
 	}
 
 	/**
 	 * Track section read info into database(used for activity meter) Insert info if it doesn't exist. If it exists, fetch current info and then update it
 	 * 
 	 * @return Current date in a formatted string if it exists, or an empty string if it doesn't exist
 	 */
 	public String getSectionTrackDateStr()
 	{
 		if (this.sectionId != 0)
 		{
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 			SectionTrackViewObjService stv = getSectionService().insertSectionTrack(this.sectionId, mPage.getCurrentUser().getId());
 
 			if (stv != null)
 			{
 				Date viewDate = stv.getViewDate();
 				if (viewDate != null)
 				{
 					DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
 					ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 					return " (" + bundle.getString("view_section_lastviewed") + " " + df.format(viewDate) + ")";
 				}
 			}
 		}
 		return "";
 	}
 	
 	/**
 	 * @return typeEditor string
 	 */
 	public String getTypeEditor()
 	{
 		return "typeEditor";
 	}
 
 	/**
 	 * @return typeLink string
 	 */
 	public String getTypeLink()
 	{
 		return "typeLink";
 	}
 
 	/**
 	 * @return typeLTI string
 	 */
 	public String getTypeLTI()
 	{
 		return "typeLTI";
 	}
 
 	/**
 	 * @return typeUpload string
 	 */
 	public String getTypeUpload()
 	{
 		return "typeUpload";
 	}
 
 	/**
 	 * Go to the current module(module that this section belongs to) page
 	 * 
 	 * 
 	 */
 	public void goCurrentModule(ActionEvent evt)
 	{
 		int currModuleId = this.moduleId;
 		resetValues();
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{viewModulesPage}");
 		/*ViewModulesPage vmPage = (ViewModulesPage) binding.getValue(context);
 		vmPage.setViewMbean(null);
 		vmPage.setPrevMbean(null);
 		vmPage.setModuleId(currModuleId);
 		vmPage.setModuleSeqNo(0);
 		vmPage.setPrintable(null);
 		vmPage.setAutonumber(null);
 		vmPage.getViewMbean();*/
 		try
 		{
 			context.getExternalContext().redirect("view_module.jsf?modId="+currModuleId);
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 		return ;
 	}
 
 	/**
 	 * Go to next module page
 	 * 
 	 * 
 	 */
 	public void goNextModule(ActionEvent evt)
 	{
 		resetValues();
 		FacesContext context = FacesContext.getCurrentInstance();
 		this.section = null;
 		// this.module = null;
 		String modSeqNoStr = (String) context.getExternalContext().getRequestParameterMap().get("modseqno");
 		if ((modSeqNoStr == null) || (modSeqNoStr.length() == 0)) modSeqNoStr = "0";
 		this.module = null;
 		/*ValueBinding binding = Util.getBinding("#{viewModulesPage}");
 		ViewModulesPage vmPage = (ViewModulesPage) binding.getValue(context);*/
 
 		/*
 		 * if (nextMdBean != null) { vmPage.setModuleId(nextMdBean.getModuleId()); }
 		 */
 		/*vmPage.setViewMbean(null);
 		vmPage.setPrevMbean(null);
 		vmPage.setModuleId(0);
 		vmPage.setModuleSeqNo(this.nextSeqNo);
 		vmPage.setPrintable(null);
 		vmPage.setAutonumber(null);
 		vmPage.getViewMbean();*/
 		try
 		{
 			context.getExternalContext().redirect("view_module.jsf?modSeqNo="+this.nextSeqNo);
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 		return;
 	}
 
 	/**
 	 * Go to previous module
 	 * 
 	 * 
 	 */
 	public void goPrevModule(ActionEvent evt)
 	{
 		resetValues();
 		FacesContext context = FacesContext.getCurrentInstance();
 		this.section = null;
 		// this.module = null;
 		String moduleIdStr = (String) context.getExternalContext().getRequestParameterMap().get("modid");
 		if (moduleIdStr != null)
 		{
 			if (moduleIdStr.trim().length() > 0)
 			{
 				this.moduleId = new Integer(moduleIdStr).intValue();
 			}
 		}
 		this.module = null;
 		/*ValueBinding binding = Util.getBinding("#{viewModulesPage}");
 		ViewModulesPage vmPage = (ViewModulesPage) binding.getValue(context);
 		vmPage.setModuleId(this.moduleId);
 		vmPage.setPrintable(null);
 		vmPage.setAutonumber(null);
 		vmPage.setViewMbean(null);
 		vmPage.setPrevMbean(null);
 		vmPage.setModuleSeqNo(this.moduleSeqNo);
 		vmPage.getViewMbean();*/
 		try
 		{
 			context.getExternalContext().redirect("view_module.jsf?modId="+this.moduleId+"&modSeqNo="+this.moduleSeqNo);
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 		
 		return;
 	}
 
 	/**
 	 * Go to previous or next section
 	 * 
 	 * 
 	 */
 	public void goPrevNext(ActionEvent evt)
 	{
 		resetValues();
 		FacesContext context = FacesContext.getCurrentInstance();
 		this.section = null;
 		String moduleIdStr = (String) context.getExternalContext().getRequestParameterMap().get("modid");
 		String sectionIdStr = (String) context.getExternalContext().getRequestParameterMap().get("secid");
 		String modSeqNoStr = (String) context.getExternalContext().getRequestParameterMap().get("modseqno");
 		
 		if (moduleIdStr != null)
 		{
			if ((moduleIdStr.trim().length() > 0)&&(!moduleIdStr.trim().equals("null")))
 			{
 				setModuleId(new Integer(moduleIdStr).intValue());
 			}
 		}
 		if (sectionIdStr != null)
 		{
			if ((sectionIdStr.trim().length() > 0)&&(!sectionIdStr.trim().equals("null")))
 			{
 				setSectionId(new Integer(sectionIdStr).intValue());
 			}
 		}
 		if (modSeqNoStr != null)
 		{
			if ((modSeqNoStr.trim().length() > 0)&&(!modSeqNoStr.trim().equals("null")))
 			{
 				setModuleSeqNo(new Integer(modSeqNoStr).intValue());
 			}
 		}
 		this.module = null;
 		
 		try
 		{
			context.getExternalContext().redirect("view_section.jsf?moduleId="+this.moduleId+"&sectionId="+this.sectionId+"&moduleSeqNo="+this.moduleSeqNo);
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 
 	/**
 	 * @return add_bookmark page
 	 */
 	public String gotoAddBookmark()
 	{
 		return "view_section";
 	}
 
 	/**
 	 * Go to table of contents
 	 * 
 	 * @return list_modules_student or list_modules_inst page
 	 */
 	public String goTOC()
 	{
 		resetValues();
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 		MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 		mPage.setNavigateCM(null);
 		binding = Util.getBinding("#{listModulesPage}");
 		ListModulesPage listPage = (ListModulesPage) binding.getValue(context);
 		listPage.setViewModuleBeans(null);
 		listPage.setAutonumberMaterial(null);
 		return listPage.listViewAction();
 	}
 
 	/**
 	 * @return list_bookmarks page
 	 */
 	/*public String gotoMyBookmarks()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{bookmarkPage}");
 		BookmarkPage bPage = (BookmarkPage) binding.getValue(context);
 		return bPage.gotoMyBookmarks("view_section", this.moduleId, new Integer(this.sectionId).toString());
 	}*/
 	
 	/**
 	 * Redirect to view_whats_next page
 	 * 
 	 */
 	public void goWhatsNext(ActionEvent evt)
 	{
 		resetValues();
 		FacesContext context = FacesContext.getCurrentInstance();
 		int currSeqNo = new Integer(((String) context.getExternalContext().getRequestParameterMap().get("modseqno"))).intValue();
 
 		/*ValueBinding binding = Util.getBinding("#{viewNextStepsPage}");
 		ViewNextStepsPage vnPage = (ViewNextStepsPage) binding.getValue(context);
 		vnPage.setPrevSecId(this.sectionId);
 		vnPage.setPrevModId(this.moduleId);
 		vnPage.setNextSeqNo(this.nextSeqNo);
 		vnPage.setModuleSeqNo(this.moduleSeqNo);
 		vnPage.setModule(null);*/
 		// vnPage.setModule(this.module);
 		try
 		{
 			context.getExternalContext().redirect("view_whats_next.jsf?nextSeqNo="+this.nextSeqNo+"&moduleSeqNo="+this.moduleSeqNo+"&prevSecId="+this.sectionId+"&prevModId="+this.moduleId);
 		}
 		catch (Exception e)
 		{
 			return;
 		}
 	}
 
 	/**
 	 * Get auto numbering preference
 	 * 
 	 * @return auto numbering preference value
 	 */
 	public boolean isAutonumber()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		try
 		{
 			if (autonumber == null)
 			{
 				ValueBinding binding = Util.getBinding("#{authorPreferences}");
 				AuthorPreferencePage preferencePage = (AuthorPreferencePage) binding.getValue(ctx);
 				if (courseId == null) getCourseId();
 				autonumber = new Boolean(preferencePage.isMaterialAutonumber(courseId));
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			autonumber = false;
 		}
 		return autonumber.booleanValue();
 	}
 
 	/**
 	 * Check if the current user has permission as student.
 	 * 
 	 * @return true if the current user has permission to perform this action, false if not.
 	 */
 	public boolean isUserStudent()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		try
 		{
 			return meleteSecurityService.allowStudent(ToolManager.getCurrentPlacement().getContext());
 		}
 		catch (Exception e)
 		{
 			String errMsg = bundle.getString("auth_failed");
 			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "auth_failed", errMsg));
 			logger.warn(e.toString());
 		}
 		return false;
 	}
 
 	/**
 	 * Method to reset section values
 	 * 
 	 */
 	public void resetValues()
 	{
 		contentLinkUrl = null;
 		autonumber = null;
 		contentWithHtml = null;
 	}
 
 	/**
 	 * Set autonumbering preference
 	 * 
 	 * @param autonumber
 	 *        true or false depending on if course is set to have autonumbering
 	 */
 	public void setAutonumber(Boolean autonumber)
 	{
 		this.autonumber = autonumber;
 	}
 
 	/**
 	 * Set link name
 	 * 
 	 * @param linkName
 	 */
 	public void setLinkName(String linkName)
 	{
 		this.linkName = linkName;
 	}
 
 	/**
 	 * @param meleteCHService
 	 *        The meleteCHService to set.
 	 */
 	public void setMeleteCHService(MeleteCHService meleteCHService)
 	{
 		this.meleteCHService = meleteCHService;
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
 	 * @param module
 	 *        module to set
 	 */
 	public void setModule(ModuleObjService module)
 	{
 		this.module = module;
 	}
 
 	/**
 	 * @param moduleId
 	 *        module id
 	 */
 	public void setModuleId(int moduleId)
 	{
 		this.moduleId = moduleId;
 	}
 
 	public void setPrevSecId(int prevSecId)
 	{
 		this.prevSecId = prevSecId;
 	}
 
 	public void setNextSecId(int nextSecId)
 	{
 		this.nextSecId = nextSecId;
 	}
 
 	/**
 	 * @param moduleSeqNo
 	 *        module sequence number
 	 */
 	public void setModuleSeqNo(int moduleSeqNo)
 	{
 		this.moduleSeqNo = moduleSeqNo;
 	}
 	
 	public void setNextSeqNo(int nextSeqNo)
 	{
 		this.nextSeqNo = nextSeqNo;
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
 	 * @param section
 	 */
 	public void setSection(SectionObjService section)
 	{
 		this.section = section;
 		this.sectionDisplaySequence = getSectionService().getSectionDisplaySequence(this.section);
 	}
 
 	/**
 	 * @param sectionDisplaySequence
 	 *        section display sequence
 	 */
 	public void setSectionDisplaySequence(String sectionDisplaySequence)
 	{
 		this.sectionDisplaySequence = sectionDisplaySequence;
 	}
 
 	/**
 	 * Set section id
 	 * 
 	 * @param sectionId
 	 *        Section id
 	 */
 	public void setSectionId(int sectionId)
 	{
 		this.sectionId = sectionId;
 		logSectionReadEvent(sectionId);
 	}
 	
 	/**
 	 * @param sectionService
 	 *        The sectionService to set.
 	 */
 	public void setSectionService(SectionService sectionService)
 	{
 		this.sectionService = sectionService;
 	}
 
 	public void setSectionTrackDateStr(String sectionTrackDateStr)
 	{
 		this.sectionTrackDateStr = sectionTrackDateStr;
 	}
 
 	/**
 	 * For LTI content, returns iframe with lti content
 	 * 
 	 * @param str
 	 *        LTI section content
 	 * @return iframe code with typeLink content
 	 */
 	private String getContentPost(String str)
 	{
 		String frameHeight = SimpleLTIUtil.getFrameHeight(str);
 		String htmltext = "<iframe id=\"iframeLTIPost\" src=\"" + getContentLink()
 		+ "\" style=\"visibility:visible\" scrolling= \"auto\" width=\"100%\" " + " marginwidth=\"0\" marginheight=\"0\" " + "height=\""
 		+ frameHeight + "\"  border=\"0\" frameborder= \"0\"></iframe>";
 		return htmltext;
 	}
 
 	/**
 	 * @return contentResource object
 	 */
 	private ContentResource getContentResource()
 	{
 		if (this.section == null) return null;
 		SectionResourceService secRes = this.section.getSectionResource();
 		if (secRes == null) return null;
 		if (secRes.getResource() == null) return null;
 		String resourceId = secRes.getResource().getResourceId();
 		if (resourceId == null || resourceId.length() == 0) return null;
 		try
 		{
 			ContentResource resource = getMeleteCHService().getResource(resourceId);
 			return resource;
 		}
 		catch (Exception e)
 		{
 			logger.debug(e.toString());
 		}
 		return null;
 	}
 
 	/**
 	 * @return course id
 	 */
 	private String getCourseId()
 	{
 		if (courseId == null)
 		{
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 			courseId = mPage.getCurrentSiteId();
 		}
 		return courseId;
 	}
 
 	/**
 	 * Gets the innermost last child node
 	 * 
 	 * @param secElement
 	 *        element to traverse
 	 * @return innermost last child node
 	 */
 	private Node getInnerLastChild(Element secElement)
 	{
 		if (secElement.getLastChild().hasChildNodes() == false)
 		{
 			return secElement.getLastChild();
 		}
 		else
 		{
 			return getInnerLastChild((Element) secElement.getLastChild());
 		}
 	}
 
 	/**
 	 * Method to traverse DOM of sequence xml to get next node.
 	 * 
 	 * @param secElement
 	 *        element to examine
 	 * @return Next node
 	 */
 	private Node getNextNode(Element secElement)
 	{
 		if (secElement.hasChildNodes())
 		{
 			return secElement.getFirstChild();
 		}
 		else
 		{
 			if (secElement.getNextSibling() != null)
 			{
 				return secElement.getNextSibling();
 			}
 			else
 			{
 				if (secElement.getParentNode() != null)
 				{
 					if (secElement.getParentNode().getNodeName().equals("module"))
 					{
 						return null;
 					}
 					else
 					{
 						return getParentsNextSibling(secElement);
 					}
 				}
 				else
 				{
 					return null;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method to traverse DOM of sequence xml to get next sibling of parent.
 	 * 
 	 * @param secElement
 	 *        Element to examine
 	 * @return Next sibling of parent
 	 */
 	private Node getParentsNextSibling(Element secElement)
 	{
 		if (secElement.getParentNode().getNodeName().equals("module"))
 		{
 			return null;
 		}
 		if (secElement.getParentNode().getNextSibling() == null)
 		{
 			return getParentsNextSibling((Element) secElement.getParentNode());
 		}
 		else
 		{
 			if (secElement != null)
 			{
 				if (secElement.getParentNode() != null)
 				{
 					return secElement.getParentNode().getNextSibling();
 				}
 			}
 
 		}
 		return null;
 	}
 
 	/**
 	 * Method to traverse DOM of sequence xml to get previous node.
 	 * 
 	 * @param secElement
 	 *        element to examine
 	 * @return Previous node
 	 */
 	private Node getPreviousNode(Element secElement)
 	{
 
 		if (secElement.getPreviousSibling() != null)
 		{
 			if (secElement.getPreviousSibling().hasChildNodes() == false)
 			{
 				return secElement.getPreviousSibling();
 			}
 			else
 			{
 				return getInnerLastChild((Element) secElement.getPreviousSibling());
 			}
 		}
 		else
 		{
 			if (secElement.getParentNode() != null)
 			{
 
 				if (secElement.getParentNode().getNodeName().equals("module"))
 				{
 					return null;
 				}
 				else
 				{
 					return secElement.getParentNode();
 				}
 			}
 			else
 			{
 				return null;
 			}
 		}
 	}
 
 	/**
 	 * @return user id
 	 */
 	private String getUserId()
 	{
 		if (userId == null)
 		{
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 			userId = mPage.getCurrentUser().getId();
 		}
 		return userId;
 	}
 
 	/**
 	 * Log event melete.section.read if section is read
 	 * 
 	 * @param sectionId
 	 *        section id
 	 */
 	private void logSectionReadEvent(int sectionId)
 	{
 		// Log melete.section.read event, if not logged yet on this user session
 		String readEvent = "melete.section.read";
 		String reference = readEvent + "-" + String.valueOf(sectionId);
 		String sessionValue = (String) SessionManager.getCurrentSession().getAttribute(reference);
 		if (sessionValue == null || sessionValue.equals(""))
 		{
 			SessionManager.getCurrentSession().setAttribute(reference, "true");
 			EventTrackingService.post(EventTrackingService.newEvent(readEvent, ToolManager.getCurrentPlacement().getContext(), true));
 		}
 	}
 }
