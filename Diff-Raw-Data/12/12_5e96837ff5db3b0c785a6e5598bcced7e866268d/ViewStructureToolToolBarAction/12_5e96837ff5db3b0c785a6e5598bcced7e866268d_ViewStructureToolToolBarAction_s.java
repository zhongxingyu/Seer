 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package org.infoglue.cms.applications.structuretool.actions;
 
 import org.infoglue.cms.applications.common.actions.WebworkAbstractAction;
 import org.infoglue.cms.applications.common.ImageButton;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.CmsLogger;
 
 import org.infoglue.cms.entities.structure.*;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.management.*;
 import org.infoglue.cms.entities.workflow.*;
 
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.AvailableServiceBindingController;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.net.URLEncoder;
 
 /**
  * This class implements the action class for the framed page in the siteNode tool.
  * 
  * @author Mattias Bogeblad  
  */
 
 public class ViewStructureToolToolBarAction extends WebworkAbstractAction
 {
 	private String title = "";
 	private String name  = "";
 	private String toolbarKey = "";
 	private String url   = "";
 	private Boolean isBranch = new Boolean(false);
 		
 	//All id's that are used
 	private Integer repositoryId = null;
 	private Integer siteNodeId = null;
 	private Integer siteNodeVersionId = null;
 	private Integer lastPublishedSiteNodeVersionId = null;
 	private Integer metaInfoAvailableServiceBindingId = null;
 	private Integer serviceBindingId = null;
 	private SiteNodeVersionVO siteNodeVersionVO = null;
 	
 	/**
 	 * This execute method first of all gets the id of the available service-binding 
 	 * the meta-info-content-type has. Then we check if there is a meta-info allready bound.
 	 */
 	
 	public String doExecute() throws Exception
     {
 	    try
 	    {
 			if(siteNodeVersionId != null)
 	    	{
 				this.siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
 				
 				AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithName("Meta information");
 				if(availableServiceBindingVO != null)
 					this.metaInfoAvailableServiceBindingId = availableServiceBindingVO.getAvailableServiceBindingId();
 				
 				List serviceBindings = SiteNodeVersionController.getServiceBindningVOList(siteNodeVersionId);
 				Iterator serviceBindingIterator = serviceBindings.iterator();
 				while(serviceBindingIterator.hasNext())
 				{
 					ServiceBindingVO serviceBindingVO = (ServiceBindingVO)serviceBindingIterator.next();
 					if(serviceBindingVO.getAvailableServiceBindingId().intValue() == metaInfoAvailableServiceBindingId.intValue())
 					{
 						this.serviceBindingId = serviceBindingVO.getServiceBindingId();
 						break;
 					}
 				}
 			}
 			else if(siteNodeId != null)
 			{
 				this.siteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(siteNodeId);
 				
 				AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithName("Meta information");
 				if(availableServiceBindingVO != null)
 					this.metaInfoAvailableServiceBindingId = availableServiceBindingVO.getAvailableServiceBindingId();
 				
 				List serviceBindings = SiteNodeVersionController.getServiceBindningVOList(siteNodeVersionVO.getId());
 				Iterator serviceBindingIterator = serviceBindings.iterator();
 				while(serviceBindingIterator.hasNext())
 				{
 					ServiceBindingVO serviceBindingVO = (ServiceBindingVO)serviceBindingIterator.next();
 					if(serviceBindingVO.getAvailableServiceBindingId().intValue() == metaInfoAvailableServiceBindingId.intValue())
 					{
 						this.serviceBindingId = serviceBindingVO.getServiceBindingId();
 						break;
 					}
 				}		    
 			}
 	    }
 	    catch(Exception e)
 	    {
 	        e.printStackTrace();
 	    }
 	    
         return "success";
     }
 
 	public Integer getRepositoryId()
 	{
 		return this.repositoryId;
 	}                   
 
 	public void setRepositoryId(Integer repositoryId)
 	{
 		this.repositoryId = repositoryId;
 	}
 
 	public Integer getSiteNodeId()
 	{
 		return this.siteNodeId;
 	}                   
 
 	public void setSiteNodeId(Integer siteNodeId)
 	{
 		this.siteNodeId = siteNodeId;
 	}
 
 	public Integer getSiteNodeVersionId()
 	{
 		return this.siteNodeVersionId;
 	}                   
 	
 	public void setSiteNodeVersionId(Integer siteNodeVersionId)
 	{
 		this.siteNodeVersionId = siteNodeVersionId;
 	}                   
 
 	public String getTitle()
 	{
 		return this.title;
 	}                   
 	
 	public void setTitle(String title)
 	{
 		this.title = title;
 	}
 
 	public String getName()
 	{
 		return this.name;
 	}                   
 	
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public Boolean getIsBranch()
 	{
 		return this.isBranch;
 	}                   
 	
 	public void setIsBranch(Boolean isBranch)
 	{
 		this.isBranch = isBranch;
 	}
 	
 	public String getToolbarKey()
 	{
 		return this.toolbarKey;
 	}                   
 
 	public void setToolbarKey(String toolbarKey)
 	{
 		this.toolbarKey = toolbarKey;
 	}
 
 	public void setUrl(String url)
 	{
 		this.url = url;
 	}
 	
 	public String getUrl()
 	{
 		return this.url;
 	}
 
 	/**
 	 * This method checks if the site node version is read only (ie publish, published or final).
 	 */
 	
 	private boolean isReadOnly()
 	{
 		boolean isReadOnly = false;
 		
 		try
 		{
 			//SiteNodeVersionVO siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(this.siteNodeVersionId);
 			if(this.siteNodeVersionVO != null && (this.siteNodeVersionVO.getStateId().intValue() == 1 || this.siteNodeVersionVO.getStateId().intValue() == 2 || this.siteNodeVersionVO.getStateId().intValue() == 3))
 			{
 				isReadOnly = true;	
 			}
 		}
 		catch(Exception e){}
 				
 		return isReadOnly;
 	}
 
 	public List getButtons()
 	{
 		CmsLogger.logInfo("Title:" + this.title);
 		CmsLogger.logInfo("toolbarKey:" + this.toolbarKey);
 		try
 		{		
 		    if(this.toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeDetailsHeader") || this.toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeComponentsHeader"))
 			{
 			    if(this.isBranch.booleanValue())
 					return getBranchSiteNodeButtons();
 				else
 					return getSiteNodeButtons();
 			}	
 			else if(this.toolbarKey.equalsIgnoreCase("tool.structuretool.siteNodeVersionHeader"))
 			{
 				return this.getSiteNodeVersionButtons();
 			}
 		}
 		catch(Exception e)
 		{
 		    e.printStackTrace();
 			CmsLogger.logWarning("Exception when generating buttons:" + e.getMessage(), e);
 		}
 							
 		return null;				
 	}
 	
 	
 	/**
 	 * This method checks if there are published versions available for the siteNodeVersion.
 	 */
 	
 	private boolean hasPublishedVersion()
 	{
 		boolean hasPublishedVersion = false;
 		
 		try
 		{
 			SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getLatestPublishedSiteNodeVersion(this.siteNodeId);
 			if(siteNodeVersion != null)
 			{
 				hasPublishedVersion = true;
 				lastPublishedSiteNodeVersionId = siteNodeVersion.getId();
 				this.repositoryId = siteNodeVersion.getOwningSiteNode().getRepository().getId();
 				this.name = siteNodeVersion.getOwningSiteNode().getName();
 				this.siteNodeId = siteNodeVersion.getOwningSiteNode().getId();
 			}
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logWarning("Exception when generating buttons:" + e.getMessage(), e);
 		}
 				
 		return hasPublishedVersion;
 	}
 	
 	
 
 	private List getBranchSiteNodeButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("CreateSiteNode!input.action?isBranch=true&parentSiteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.newSiteNode"), "New SiteNode"));	
 		buttons.add(getMoveButton());	
 		buttons.add(new ImageButton("Confirm.action?header=tool.structuretool.deleteSiteNode.header&yesDestination=" + URLEncoder.encode(URLEncoder.encode("DeleteSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&changeTypeId=4", "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewSiteNode.action?title=SiteNode&siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8") + "&message=tool.structuretool.deleteSiteNode.message", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.deleteSiteNode"), "Delete SiteNode"));
 		String serviceBindingIdString = this.serviceBindingId == null ? "" : this.serviceBindingId.toString();
 		buttons.add(new ImageButton(true, "javascript:openPopup('ViewAndCreateContentForServiceBinding.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&siteNodeVersionId=" + this.siteNodeVersionVO.getId() + "&availableServiceBindingId=" + this.metaInfoAvailableServiceBindingId + "&serviceBindingId=" + serviceBindingIdString + "', 'PageProperties', 'width=400,height=525,resizable=no,status=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.editSiteNodeProperties"), "Edit siteNode properties"));
 
 		//buttons.add(new ImageButton(true, "javascript:openPopup('" + CmsPropertyHandler.getProperty("previewDeliveryUrl") + "?siteNodeId=" + this.siteNodeId + "', 'SiteNode', 'width=800,height=600,resizable=yes');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.previewSiteNode"), "Preview siteNode"));
 		buttons.add(getPreviewButtons());
 		
 		if(hasPublishedVersion())
 			buttons.add(new ImageButton("Confirm.action?header=Unpublish%20node&yesDestination=" + URLEncoder.encode(URLEncoder.encode("RequestSiteNodeVersionUnpublish.action?entityClass=" + SiteNodeVersion.class.getName() + "&entityId=" + this.lastPublishedSiteNodeVersionId + "&typeId=" + EventVO.UNPUBLISH_LATEST + "&repositoryId=" + this.repositoryId + "&name=" + this.name + "&description=Unpublish of latest published version&siteNodeId=" + this.siteNodeId, "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewSiteNode.action?title=tool.structuretool.siteNodeDetailsHeader&siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8") + "&message=" + URLEncoder.encode("Do you really want to ask the editor to unpublish the latest published version of " + this.name, "UTF-8"), getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.unpublishVersion"), "Unpublish SiteNode"));
 		
 		buttons.add(new ImageButton("ViewSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&stay=true", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeCover"), "SiteNode Cover"));	
 
 		if(!isReadOnly())
 			buttons.add(getViewPageComponentsButton());	
 		
 		buttons.add(getPublishButton());
 		buttons.add(getExecuteTaskButton());
 
 		if(this.siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES.intValue())
 			buttons.add(getAccessRightsButton());	
 			
 		return buttons;
 	}
 
 	private ImageButton getPreviewButtons() throws Exception
 	{
 		RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(this.repositoryId);
 		
		ImageButton imageButton = new ImageButton(true, "javascript:openPopup('" + CmsPropertyHandler.getProperty("previewDeliveryUrl") + "?siteNodeId=" + this.siteNodeId + "&repositoryName=" + repositoryVO.getName() + "' , 'SiteNode', 'width=800,height=600,resizable=yes');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.previewSiteNode"), "Preview siteNode");
 		
 		return imageButton;
 	}
 	
 	private List getSiteNodeButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("Confirm.action?header=Delete%20siteNode&yesDestination=" + URLEncoder.encode(URLEncoder.encode("DeleteSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&changeTypeId=4", "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewSiteNode.action?title=SiteNode&siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8") + "&message=" + URLEncoder.encode("Do you really want to delete the siteNode " + this.name + " and all its children", "UTF-8"), getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.deleteSiteNode"), "Delete SiteNode"));
 		buttons.add(getMoveButton());	
 		buttons.add(getPublishButton());
 		return buttons;				
 	}
 
 	private List getSiteNodeVersionButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		if(this.siteNodeVersionId != null)
 		{
 			buttons.add(new ImageButton(true, "javascript:openPopup('ViewSiteNodeVersion!preview.action?siteNodeVersionId=" + this.siteNodeVersionId + "&siteNodeId=" + this.siteNodeId + "', 'SiteNodePreview', 'width=600,height=600,resizable=yes');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.previewSiteNode"), "Preview siteNode version"));	
 		}
 		
 		return buttons;				
 	}
 
 	private ImageButton getMoveButton() throws Exception
 	{
 		return new ImageButton(true, "javascript:openPopup('ViewSiteNodeTree.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId + "&hideLeafs=true', 'SiteNode', 'width=400,height=600,resizable=no');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.moveSiteNode"), "Move siteNode");
 	}
 
 	private ImageButton getViewPageComponentsButton() throws Exception
 	{
 		try
 		{
 		    boolean isMetaInfoInWorkingState = false;
 			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(this.repositoryId);
 			Integer languageId = masterLanguageVO.getLanguageId();
 			
 			List boundContents = ContentController.getBoundContents(serviceBindingId); 			
 			if(boundContents.size() > 0)
 			{
 				ContentVO contentVO = (ContentVO)boundContents.get(0);
 				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId);
 				if(contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
 					isMetaInfoInWorkingState = true;
 			}
 			
 			CmsLogger.logInfo("isMetaInfoInWorkingState:" + isMetaInfoInWorkingState);
 			if(isMetaInfoInWorkingState)
 			    return new ImageButton(CmsPropertyHandler.getProperty("componentRendererUrl") + "ViewPage!renderDecoratedPage.action?siteNodeId=" + this.siteNodeId + "&languageId=" + masterLanguageVO.getId() + "&contentId=-1", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeComponents"), "Site Node Components");
 			    //return new ImageButton("ViewSiteNodePageComponents.action?siteNodeId=" + this.siteNodeId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeComponents"), "Site Node Components");
 			else
 				return new ImageButton(true, "javascript:alert('Cannot edit this page. You must first set the meta info to working. Do this by entering node properties and changing the state to working.');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeComponents"), "Site Node Components");
 		}
 		catch(Exception e)
 		{
 			return new ImageButton(true, "javascript:alert('Cannot edit this page. You must first assign a metainfo content. Do this by entering node properties and fill in the information requested.');", getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeComponents"), "Site Node Components");
 		}
 	}
 
 	private ImageButton getExecuteTaskButton()
 	{
 		return new ImageButton(true, "javascript:openPopup('ViewExecuteTask.action?contentId=" + this.siteNodeId + "', 'SiteNode', 'width=400,height=600,resizable=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.global.buttons.executeTask"), "tool.common.executeTask.header");	
 	}
 	
 	private ImageButton getPublishButton()
 	{
 		return new ImageButton("ViewListSiteNodeVersion.action?siteNodeId=" + this.siteNodeId, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.publishSiteNode"), "tool.structuretool.publishSiteNode.header");	
 	}
 
 /*
 	private ImageButton getViewPageExtranetAccessButton() throws Exception
 	{
 		String returnAddress = "ViewSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId;
 		//return new ImageButton("ViewSiteNodeAccessRights.action?siteNodeId=" + this.siteNodeId + "&name=SiteNode&value=" + this.siteNodeId + "&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeAccessRights"), "Site Node Access Rights");
 		return new ImageButton("ViewAccessRights.action?siteNodeId=" + this.siteNodeId + "&name=SiteNode&value=" + this.siteNodeId + "&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeAccessRights"), "Site Node Access Rights");
 	}
 */
 
 	private ImageButton getAccessRightsButton() throws Exception
 	{
 		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewSiteNode.action?siteNodeId=" + this.siteNodeId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8");
 		return new ImageButton("ViewAccessRights.action?interceptionPointCategory=SiteNodeVersion&extraParameters=" + this.siteNodeVersionId +"&colorScheme=StructureTool&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.structuretool.buttons.siteNodeAccessRights"), "Site Node Access Rights");
 	}
 
 	public Integer getServiceBindingId()
 	{
 		return serviceBindingId;
 	}
 
 	public void setServiceBindingId(Integer integer)
 	{
 		serviceBindingId = integer;
 	}
 
 }
