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
 
 package org.infoglue.cms.applications.contenttool.actions;
 
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.applications.common.ImageButton;
 import org.infoglue.cms.util.CmsPropertyHandler;
 
 
 import org.infoglue.cms.entities.content.*;
 import org.infoglue.cms.entities.workflow.*;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.RepositoryVO;
 import org.infoglue.cms.entities.structure.*;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.net.URLEncoder;
 
 /**
  * This class implements the action class for the framed page in the content tool.
  * 
  * @author Mattias Bogeblad  
  */
 
 public class ViewContentToolToolBarAction extends InfoGlueAbstractAction
 {
 	private String title = "";
 	private String name  = "";
 	private String toolbarKey = "";
 	private String url   = "";
 	private Boolean isBranch = new Boolean(false);
 		
 	//All id's that are used
 	private Integer repositoryId = null;
 	private Integer siteNodeId = null;
 	private Integer languageId = null;
 	private Integer contentId = null;
 	private Integer contentVersionId = null;
 	private Integer lastPublishedContentVersionId = null;
 	private String languageName = "";
 	
 	private ContentVO contentVO = null;
 	
 	public String doExecute() throws Exception
     {
 		if(this.contentId != null)
 		{
 			this.contentVO = ContentController.getContentController().getContentVOWithId(this.contentId);
 		}
 			
     	if(this.repositoryId == null && this.contentId != null)
     	{
 			this.repositoryId = ContentController.getContentController().getContentVOWithId(this.contentId).getRepositoryId();
 	    	SiteNodeVO rootSiteNodeVO = SiteNodeController.getController().getRootSiteNodeVO(this.repositoryId);
 			if(rootSiteNodeVO != null)
 				this.siteNodeId = rootSiteNodeVO.getId();
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
 
 	public Integer getContentId()
 	{
 		return this.contentId;
 	}                   
 
 	public void setContentId(Integer contentId)
 	{
 		this.contentId = contentId;
 	}
 
 	public Integer getContentVersionId()
 	{
 		return this.contentVersionId;
 	}                   
 	
 	public void setContentVersionId(Integer contentVersionId)
 	{
 		this.contentVersionId = contentVersionId;
 	}                   
 
 	public Integer getLanguageId()
 	{
 		return this.languageId;
 	}                   
 
 	public void setLanguageId(Integer languageId)
 	{
 		this.languageId = languageId;
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
 
 	public List getButtons()
 	{
 		getLogger().info("Title:" + this.title);
 		getLogger().info("toolbarKey:" + this.toolbarKey);
 		
 		if(this.toolbarKey.equalsIgnoreCase("content details"))
 		{
 			if(this.isBranch.booleanValue())
 				return getBranchContentButtons();
 			else
 				return getContentButtons();
 		}	
 		else if(this.toolbarKey.equalsIgnoreCase("content version"))
 		{
 			return this.getContentVersionButtons();
 		}
 		else if(this.toolbarKey.equalsIgnoreCase("ContentVersionHistory"))
 		{
 			return this.getContentVersionHistoryButtons();
 		}
 					
 		return null;				
 	}
 
 	/**
 	 * This method checks if there are published versions available for the contentVersion.
 	 */
 	
 	private boolean hasAnyPublishedVersion()
 	{
 		boolean hasPublishedVersion = false;
 		
 		try
 		{
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestPublishedContentVersion(this.contentId);
 			if(contentVersion != null)
 			{
 				hasPublishedVersion = true;
 				lastPublishedContentVersionId = contentVersion.getContentVersionId();
 				this.repositoryId = contentVersion.getOwningContent().getRepository().getId();
 				this.name = contentVersion.getOwningContent().getName();
 				this.languageName = contentVersion.getLanguage().getName();
 				this.contentId = contentVersion.getOwningContent().getId();
 				this.languageId = contentVersion.getLanguage().getId();
 			}
 		}
 		catch(Exception e){}
 				
 		return hasPublishedVersion;
 	}
 	
 	/**
 	 * This method checks if there are published versions available for the contentVersion.
 	 */
 	
 	private boolean hasPublishedVersion()
 	{
 		boolean hasPublishedVersion = false;
 		
 		try
 		{
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestPublishedContentVersion(this.contentId, this.languageId);
 			if(contentVersion != null)
 			{
 				hasPublishedVersion = true;
 				lastPublishedContentVersionId = contentVersion.getContentVersionId();
 				this.repositoryId = contentVersion.getOwningContent().getRepository().getId();
 				this.name = contentVersion.getOwningContent().getName();
 				this.languageName = contentVersion.getLanguage().getName();
 				this.contentId = contentVersion.getOwningContent().getId();
 				this.languageId = contentVersion.getLanguage().getId();
 			}
 		}
 		catch(Exception e){}
 				
 		return hasPublishedVersion;
 	}
 	
 	
 	/**
 	 * This method checks if the content version is read only (ie publish, published or final).
 	 */
 	
 	private boolean isReadOnly()
 	{
 		boolean isReadOnly = false;
 		
 		try
 		{
 			ContentVersionVO contentVersion = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
 			if(contentVersion != null && (contentVersion.getStateId().intValue() == 1 || contentVersion.getStateId().intValue() == 2 || contentVersion.getStateId().intValue() == 3))
 			{
 				isReadOnly = true;	
 			}
 		}
 		catch(Exception e){}
 				
 		return isReadOnly;
 	}
 
 
 	private List getBranchContentButtons()
 	{
 		List buttons = new ArrayList();
 		
 		try
 		{
 			buttons.add(new ImageButton("CreateContent!input.action?isBranch=false&parentContentId=" + this.contentId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.newContent"), "tool.contenttool.newContent.header"));	
 			buttons.add(new ImageButton("CreateContent!input.action?isBranch=true&parentContentId=" + this.contentId + "&repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.newContentFolder"), "tool.contenttool.newContentFolder.header"));	
 			
 			ImageButton moveButton = getMoveButton();
 			moveButton.getSubButtons().add(getMoveMultipleButton());
 			buttons.add(moveButton);
 			
 			buttons.add(getDeleteButton());
 			buttons.add(getPublishButton());
 			if(hasAnyPublishedVersion())
 			    buttons.add(getUnpublishButton());
 			buttons.add(getExecuteTaskButton());
 			if(this.contentVO.getIsProtected().intValue() == ContentVO.YES.intValue())
 				buttons.add(getAccessRightsButton());
 
 			buttons.add(new ImageButton("ViewContentVersionHistory.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.viewHistory"), "History", new Integer(22), new Integer(80)));
 
 			buttons.add(getSyncTreeButton());
 			
 			buttons.add(new ImageButton("ViewContentProperties.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.global.buttons.editProperties"), "Edit Properties", new Integer(22), new Integer(80)));
 
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("Exception when generating buttons:" + e.getMessage(), e);
 		}
 		
 		return buttons;
 	}
 	
 	private List getContentButtons()
 	{
 		List buttons = new ArrayList();
 		try
 		{
 			buttons.add(getDeleteButton());	
 			
 			ImageButton moveButton = getMoveButton();
 			moveButton.getSubButtons().add(getMoveMultipleButton());
 			buttons.add(moveButton);
 			
 			buttons.add(getPublishButton());
 			if(hasAnyPublishedVersion())
 			    buttons.add(getUnpublishButton());
 			buttons.add(getExecuteTaskButton());
 			if(this.contentVO.getIsProtected().intValue() == ContentVO.YES.intValue())
 				buttons.add(getAccessRightsButton());
 
 			buttons.add(new ImageButton("ViewContentVersionHistory.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.viewHistory"), "History", new Integer(22), new Integer(80)));
 			
 			buttons.add(getSyncTreeButton());
 			
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("Exception when generating buttons:" + e.getMessage(), e);
 		}
 
 		return buttons;				
 	}
 
 
 	private List getContentVersionButtons()
 	{
 		List buttons = new ArrayList();
 		
 		try
 		{
 		    boolean latest = true;
 		    if(this.contentVersionId != null)
 		    {
 		        ContentVersionVO currentContentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(this.contentVersionId);
 		        ContentVersionVO latestContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(currentContentVersionVO.getContentId(), currentContentVersionVO.getLanguageId());
 		        if(currentContentVersionVO.getId().intValue() != latestContentVersionVO.getId().intValue())
 		            latest = false;
 			}
 		    
 		    buttons.add(getCoverButton());
 		    
 		    if(latest)
 		    {
 				buttons.add(getDeleteButton());
 			    
 			    if(this.contentVersionId != null)
 				{
 			        if(!isReadOnly())
 						buttons.add(new ImageButton(true, "javascript:openPopup('ViewDigitalAsset.action?contentVersionId=" + this.contentVersionId + "', 'FileUpload', 'width=400,height=200,resizable=no');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.newAsset"), "tool.contenttool.uploadDigitalAsset.header"));	
 				
 					if(this.siteNodeId != null)
 					{
 						RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(this.repositoryId);
 	
 						String dnsName = repositoryVO.getDnsName();
 	
 					    String workingUrl = null;
 					    
 					    String keyword = "working=";
 					    int startIndex = (dnsName == null) ? -1 : dnsName.indexOf(keyword);
 					    if(startIndex != -1)
 					    {
 					        int endIndex = dnsName.indexOf(",", startIndex);
 						    if(endIndex > -1)
 					            dnsName = dnsName.substring(startIndex, endIndex);
 					        else
 					            dnsName = dnsName.substring(startIndex);
 	
 						    workingUrl = dnsName.split("=")[1] + CmsPropertyHandler.getProperty("componentRendererUrl") + "ViewPage.action";
 					    }
 					    else
 					    {
 					        workingUrl = CmsPropertyHandler.getProperty("previewDeliveryUrl");
 					    }
 					    
 					    ImageButton previewSiteButton = new ImageButton(true, "javascript:openPopup('" + workingUrl + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "', 'SitePreview', 'width=800,height=600,resizable=yes,toolbar=yes,scrollbars=yes,status=yes,location=yes,menubar=yes');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.previewSite"), "tool.contenttool.previewSite.header");
 						ImageButton previewContentButton = new ImageButton(true, "javascript:openPopup('ViewContentVersion!preview.action?contentVersionId=" + this.contentVersionId + "&contentId=" + this.contentId + "&languageId=" + this.languageId + "', 'ContentPreview', 'width=800,height=600,resizable=yes,toolbar=yes,scrollbars=yes,status=yes,location=yes,menubar=yes');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.previewContent"), "tool.contenttool.previewContent.header");	
 						previewSiteButton.getSubButtons().add(previewContentButton);
 	
 						buttons.add(previewSiteButton);			
 					}
 					
 					if(hasPublishedVersion())
 						buttons.add(getUnpublishButton());
 					
 					if(this.contentVO.getIsProtected().intValue() == ContentVO.YES.intValue())
 						buttons.add(getContentVersionAccessRightsButton());
 	
 					if(!isReadOnly())
 						buttons.add(getPublishButton());
 				}
 				buttons.add(getSyncTreeButton());
 		    }		    
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("Exception when generating buttons:" + e.getMessage(), e);
 		}
 
 		return buttons;				
 	}
 
 	
 
 	private List getContentVersionHistoryButtons()
 	{
 		List buttons = new ArrayList();
 		
 		try
 		{
 		    buttons.add(getCompareButton());
 			//buttons.add(getDeleteButton());
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("Exception when generating buttons:" + e.getMessage(), e);
 		}
 
 		return buttons;				
 	}
 
 	private ImageButton getCompareButton()
 	{
 	    return new ImageButton(true, "javascript:compareVersions('contentVersion');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.compareVersions"), "tool.contenttool.compareVersions.header");
 		
 	    //return new ImageButton(true, "javascript:openPopup('ViewContentVersionDifference.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&hideLeafs=true', 'MoveContent', 'width=400,height=600,resizable=no');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.moveContent"), "tool.contenttool.moveContent.header");	
 	}
 
 	
 	private ImageButton getCoverButton()
 	{
 		try
 		{
 			return new ImageButton("ViewContent.action?contentId=" + this.contentId + "&stay=true", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.contentCover"), "tool.contenttool.contentDetailsHeader");
 		}
 		catch(Exception e){}
 
 		return null;
 	}
 	
 	private ImageButton getUnpublishButton()
 	{
 		try
 		{
 			//return new ImageButton("Confirm.action?header=tool.contenttool.unpublishVersion.header&yesDestination=" + URLEncoder.encode(URLEncoder.encode("RequestContentVersionUnpublish.action?entityClass=" + ContentVersion.class.getName() + "&entityId=" + this.lastPublishedContentVersionId + "&typeId=" + EventVO.UNPUBLISH_LATEST + "&repositoryId=" + this.repositoryId + "&name=" + this.name + " - " + this.languageName + "&description=tool.contenttool.unpublishVersion.text&contentId=" + this.contentId + "&languageId=" + this.languageId, "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewContentVersion.action?title=ContentVersion&contentVersionId=" + this.contentVersionId + "&contentId=" + this.contentId + "&languageId=" + this.languageId, "UTF-8"), "UTF-8") + "&message=tool.contenttool.unpublishVersion.text", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.unpublishVersion"), "tool.contenttool.unpublishVersion.header");
 			return new ImageButton("UnpublishContentVersion!input.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.unpublishVersion"), "tool.contenttool.unpublishVersion.header");
 		}
 		catch(Exception e){}
 
 		return null;
 	}
 
 	private ImageButton getDeleteButton()
 	{
 		try
 		{
 			//return new ImageButton("Confirm.action?header=" + URLEncoder.encode(getLocalizedString(getSession().getLocale(), "tool.contenttool.deleteContent.header"), "UTF-8") + "&yesDestination=" + URLEncoder.encode(URLEncoder.encode("DeleteContent.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&changeTypeId=4", "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewContent.action?title=Content&contentId=" + this.contentId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8") + "&message=" + URLEncoder.encode(getLocalizedString(getSession().getLocale(), "tool.contenttool.deleteContent.text"), "UTF-8"), getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.deleteContent"), getLocalizedString(getSession().getLocale(), "tool.contenttool.deleteContent.header"));
 			String url = "Confirm.action?header=tool.contenttool.deleteContent.header&yesDestination=" + URLEncoder.encode(URLEncoder.encode("DeleteContent.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&changeTypeId=4", "UTF-8"), "UTF-8") + "&noDestination=" + URLEncoder.encode(URLEncoder.encode("ViewContent.action?title=Content&contentId=" + this.contentId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8") + "&message=tool.contenttool.deleteContent.text";
 			
 		    return new ImageButton(url, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.deleteContent"), "tool.contenttool.deleteContent.header");
 		}
 		catch(Exception e){e.printStackTrace();}
 
 		return null;
 	}
 
 	private ImageButton getMoveButton()
 	{
 		return new ImageButton(true, "javascript:openPopup('ViewContentTree.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "&hideLeafs=true', 'MoveContent', 'width=400,height=600,resizable=no');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.moveContent"), "tool.contenttool.moveContent.header");	
 	}
 
 	private ImageButton getMoveMultipleButton()
 	{
 		return new ImageButton(true, "javascript:openPopup('MoveMultipleContent!input.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId + "', 'MoveMultipleContent', 'width=400,height=600,resizable=no');", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.moveMultipleContent"), "tool.contenttool.moveMultipleContent.header");	
 	}
 
 	private ImageButton getSyncTreeButton()
 	{
 		return new ImageButton(true, "javascript:parent.frames['main'].syncWithTree();", getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.revealInTree"), "tool.contenttool.revealInTree.header");	
 	}
 
 	private ImageButton getPublishButton()
 	{
 		return new ImageButton("ViewListContentVersion.action?contentId=" + this.contentId, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.publishContent"), "tool.contenttool.publishContent.header");	
 	}
 
 	private ImageButton getExecuteTaskButton()
 	{
 		return new ImageButton(true, "javascript:openPopup('ViewExecuteTask.action?contentId=" + this.contentId + "', 'ExecuteTask', 'width=400,height=600,resizable=yes,scrollbars=yes');", getLocalizedString(getSession().getLocale(), "images.global.buttons.executeTask"), "tool.common.executeTask.header");	
 	}
 	
 	private ImageButton getAccessRightsButton() throws Exception
 	{
		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewContent.action?contentId=" + this.contentId + "&repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8");
 		//return new ImageButton("ViewAccessRights.action?name=Content&value=" + this.contentId + "&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.contentAccessRights"), "Content Access Rights");
 		return new ImageButton("ViewAccessRights.action?interceptionPointCategory=Content&extraParameters=" + this.contentId +"&colorScheme=ContentTool&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.contentAccessRights"), "tool.contenttool.contentAccessRights.header");
 	}
 
 	private ImageButton getContentVersionAccessRightsButton() throws Exception
 	{
 		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewContentVersion.action?contentVersionId=" + this.contentVersionId + "&contentId=" + contentId + "&languageId=" + languageId, "UTF-8"), "UTF-8");
 		return new ImageButton("ViewAccessRights.action?interceptionPointCategory=ContentVersion&extraParameters=" + this.contentVersionId +"&colorScheme=ContentTool&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.contenttool.buttons.contentAccessRights"), "tool.contenttool.contentVersionAccessRights.header");
 	}
 
 }
