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
 
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.entities.structure.ServiceBindingVO;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.entities.workflow.EventVO;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.management.AvailableServiceBindingVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.SiteNodeTypeDefinitionVO;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.applications.common.actions.WebworkAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.*;
 import org.infoglue.cms.entities.structure.*;
 import org.infoglue.cms.entities.management.*;
 import org.infoglue.cms.util.CmsLogger;
 import org.infoglue.cms.util.CmsPropertyHandler;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 
 
 /** 
  * This class represents the view of a siteNode to the user. In fact - it presents the
  * view of the siteNode as well as the view of the latest siteNodeVersion as well.
  */
 
 public class ViewSiteNodeAction extends InfoGlueAbstractAction
 {
 	private Integer unrefreshedSiteNodeId = new Integer(0);
 	private Integer changeTypeId = new Integer(0);
 	private Integer repositoryId = null;
 	private SiteNodeTypeDefinitionVO siteNodeTypeDefinitionVO;
 	private List availableServiceBindings = null;
 	private List serviceBindings = null;
 	
 	private SiteNodeVO siteNodeVO;
 	private SiteNodeVersionVO siteNodeVersionVO;
 	
    	private String stay = null;
    	private String dest = "";
 
 
     public ViewSiteNodeAction()
     {
         this(new SiteNodeVO(), new SiteNodeVersionVO());
     }
     
     public ViewSiteNodeAction(SiteNodeVO siteNodeVO, SiteNodeVersionVO siteNodeVersionVO)
     {
 		CmsLogger.logInfo("Construction ViewSiteNodeAction");
         this.siteNodeVO = siteNodeVO;
         this.siteNodeVersionVO = siteNodeVersionVO;
     }
 
 	protected void initialize(Integer siteNodeId) throws Exception
 	{
 		this.siteNodeVersionVO = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getACLatestSiteNodeVersionVO(this.getInfoGluePrincipal(), siteNodeId);
 		this.siteNodeVO = SiteNodeController.getSiteNodeVOWithId(siteNodeId);
 		//SiteNodeControllerProxy.getController().getACSiteNodeVOWithId(this.getInfoGluePrincipal(), siteNodeId);
 		
 		if(siteNodeVO.getSiteNodeTypeDefinitionId() != null)
 		{
 			this.siteNodeTypeDefinitionVO = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOWithId(siteNodeVO.getSiteNodeTypeDefinitionId());
 			this.availableServiceBindings = SiteNodeTypeDefinitionController.getController().getAvailableServiceBindingVOList(siteNodeVO.getSiteNodeTypeDefinitionId());
 			this.serviceBindings = SiteNodeVersionController.getServiceBindningVOList(siteNodeVersionVO.getSiteNodeVersionId());
 		}
 	} 
 
 /*    
     protected void initialize(Integer siteNodeId) throws Exception
     {
     	this.siteNodeVO = SiteNodeController.getSiteNodeVOWithId(siteNodeId);
         this.siteNodeVersionVO = SiteNodeVersionController.getLatestSiteNodeVersionVO(siteNodeId);
 		
         if(siteNodeVO.getSiteNodeTypeDefinitionId() != null)
         {
 	        this.siteNodeTypeDefinitionVO = SiteNodeTypeDefinitionController.getSiteNodeTypeDefinitionVOWithId(siteNodeVO.getSiteNodeTypeDefinitionId());
 			this.availableServiceBindings = SiteNodeTypeDefinitionController.getAvailableServiceBindingVOList(siteNodeVO.getSiteNodeTypeDefinitionId());
 			this.serviceBindings = SiteNodeVersionController.getServiceBindningVOList(siteNodeVersionVO.getSiteNodeVersionId());
 		}
     } 
 */
 
     public String doExecute() throws Exception
     {
         if(getSiteNodeId() != null)
 		{	
             this.initialize(getSiteNodeId());
 
            if((this.stay == null || !this.stay.equalsIgnoreCase("true")) && this.siteNodeVO.getSiteNodeTypeDefinitionId() != null && this.siteNodeVersionVO.getStateId().intValue() == SiteNodeVersionVO.WORKING_STATE.intValue() && getShowComponentsFirst().equalsIgnoreCase("true"))
 	        {
                 boolean isMetaInfoInWorkingState = false;
     			LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(this.repositoryId);
     			Integer languageId = masterLanguageVO.getLanguageId();
     			
     			AvailableServiceBindingVO availableServiceBindingVO = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithName("Meta information");
     			Integer metaInfoAvailableServiceBindingId = null;
     			if(availableServiceBindingVO != null)
     			    metaInfoAvailableServiceBindingId = availableServiceBindingVO.getAvailableServiceBindingId();
     			
     			List serviceBindings = SiteNodeVersionController.getServiceBindningVOList(this.siteNodeVersionVO.getId());
     			Iterator serviceBindingIterator = serviceBindings.iterator();
     			while(serviceBindingIterator.hasNext())
     			{
     				ServiceBindingVO serviceBindingVO = (ServiceBindingVO)serviceBindingIterator.next();
     				if(serviceBindingVO.getAvailableServiceBindingId().intValue() == metaInfoAvailableServiceBindingId.intValue())
     				{
     					List boundContents = ContentController.getBoundContents(serviceBindingVO.getServiceBindingId()); 			
     					if(boundContents.size() > 0)
     	    			{
     	    				ContentVO contentVO = (ContentVO)boundContents.get(0);
     	    				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId);
     	    				if(contentVersionVO != null && contentVersionVO.getStateId().equals(ContentVersionVO.WORKING_STATE))
     	    					isMetaInfoInWorkingState = true;
 
     	    				break;
     	    			}                					
     				}
     			}
 
     			if(isMetaInfoInWorkingState)
     			{
     			    String url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + getSiteNodeId() + "&languageId=" + masterLanguageVO.getId() + "&contentId=-1";
     			    url = this.getResponse().encodeURL(url);
     				this.getResponse().sendRedirect(url);
     			    return NONE;
     			}
     			else
     			    return "success";
 	            
     			//if(this.repositoryId == null)
 	            //    this.repositoryId = contentVO.getRepositoryId();
 	            
 	            //this.languageId = getMasterLanguageVO().getId();
 	            //return "viewVersion";
 	        }
 	        else
 	        {
     			
     			return "success";
 	        }
 		}
 		else
 			return "blank";
     }
     
     public String doChangeState() throws Exception
     {
     	CmsLogger.logInfo("Gonna change state with comment:" + this.siteNodeVersionVO.getVersionComment());
     	SiteNodeVersionController.getController().updateStateId(this.siteNodeVersionVO.getSiteNodeVersionId(), getStateId(), this.siteNodeVersionVO.getVersionComment(), this.getInfoGluePrincipal(), this.getSiteNodeId());
     	this.initialize(getSiteNodeId());
         return "success";
     }
         
     public String doCommentVersion() throws Exception
     { 
     	CmsLogger.logInfo("Gonna show the comment-view");
         return "commentVersion";
     }
         	
     public java.lang.Integer getSiteNodeId()
     {
         return this.siteNodeVO.getSiteNodeId();
     }
         
     public boolean getIsSiteNodeTypeDefinitionAssigned()
     {
         return (this.siteNodeVO.getSiteNodeTypeDefinitionId() != null) ? true : false;
     }
  
     public void setSiteNodeId(java.lang.Integer siteNodeId)
     {
 	    this.siteNodeVO.setSiteNodeId(siteNodeId);
     }
     
     public java.lang.Integer getRepositoryId()
     {
     	if(this.repositoryId != null)
 	        return this.repositoryId;
     	else
     		return this.siteNodeVO.getRepositoryId();
     }
         
     public void setRepositoryId(java.lang.Integer repositoryId)
     {
 	    this.repositoryId = repositoryId;
     }
     
     public java.lang.Integer getUnrefreshedSiteNodeId()
     {
         return this.unrefreshedSiteNodeId;
     }
         
     public void setUnrefreshedSiteNodeId(java.lang.Integer unrefreshedSiteNodeId)
     {
 	    this.unrefreshedSiteNodeId = unrefreshedSiteNodeId;
     }
 
     public java.lang.Integer getChangeTypeId()
     {
         return this.changeTypeId;
     }
         
     public void setChangeTypeId(java.lang.Integer changeTypeId)
     {
 	    this.changeTypeId = changeTypeId;
     }
     
     public String getName()
     {
         return this.siteNodeVO.getName();
     }
 
     public String getPublishDateTime()
     {    		
         return new VisualFormatter().formatDate(this.siteNodeVO.getPublishDateTime(), "yyyy-MM-dd HH:mm");
     }
         
     public String getExpireDateTime()
     {
         return new VisualFormatter().formatDate(this.siteNodeVO.getExpireDateTime(), "yyyy-MM-dd HH:mm");
     }
 
    	public long getPublishDateTimeAsLong()
     {    		
         return this.siteNodeVO.getPublishDateTime().getTime();
     }
         
     public long getExpireDateTimeAsLong()
     {
         return this.siteNodeVO.getExpireDateTime().getTime();
     }
     
 	public Boolean getIsBranch()
 	{
 		return this.siteNodeVO.getIsBranch();
 	}     
 
 	public String getContentType()
 	{
 		return this.siteNodeVersionVO.getContentType();
 	}
 
 	public void setContentType(String contentType)
 	{
 		this.siteNodeVersionVO.setContentType(contentType);
 	}
 
 	public Integer getDisableEditOnSight()
 	{
 		return this.siteNodeVersionVO.getDisableEditOnSight();
 	}
 
 	public void setDisableEditOnSight(Integer disableEditOnSight)
 	{
 		this.siteNodeVersionVO.setDisableEditOnSight(disableEditOnSight);
 	}
 
 	public Integer getDisablePageCache()
 	{
 		return this.siteNodeVersionVO.getDisablePageCache();
 	}
 
 	public void setDisablePageCache(Integer disablePageCache)
 	{
 		this.siteNodeVersionVO.setDisablePageCache(disablePageCache);
 	}
 
 	public Integer getIsProtected()
 	{
 		return this.siteNodeVersionVO.getIsProtected();
 	}
 
 	public void setIsProtected(Integer isProtected)
 	{
 		this.siteNodeVersionVO.setIsProtected(isProtected);
 	}
 
 	public void setStateId(Integer stateId)
 	{
 		this.siteNodeVersionVO.setStateId(stateId);
 	}
 
 	public Integer getStateId()
 	{
 		return this.siteNodeVersionVO.getStateId();
 	}
 	
 	public SiteNodeVersionVO getSiteNodeVersion()
 	{
 		return this.siteNodeVersionVO;
 	}	
 
 	public Integer getSiteNodeVersionId()
 	{
 		return this.siteNodeVersionVO.getSiteNodeVersionId();
 	}	
 
 	public void setSiteNodeVersionId(Integer siteNodeVersionId)
 	{
 		this.siteNodeVersionVO.setSiteNodeVersionId(siteNodeVersionId);
 	}	
 
 	public void setVersionComment(String versionComment)
 	{
 		this.siteNodeVersionVO.setVersionComment(versionComment);
 	}
 	
 	public String getVersionComment()
 	{
 		return this.siteNodeVersionVO.getVersionComment();
 	}
 
 	public SiteNodeTypeDefinitionVO getSiteNodeTypeDefinition()
 	{
 		return this.siteNodeTypeDefinitionVO;
 	}	
 
 	public List getAvailableServiceBindings()
 	{
 		return this.availableServiceBindings;
 	}	
 	
 	public String getShowComponentsFirst()
 	{
 	    return CmsPropertyHandler.getProperty("showComponentsFirst");
 	}
 	
 	/**
 	 * This method sorts a list of available service bindings on the name of the binding.
 	 */
 	
 	public List getSortedAvailableServiceBindings()
 	{
 		List sortedAvailableServiceBindings = new ArrayList();
 		
 		Iterator iterator = this.availableServiceBindings.iterator();
 		while(iterator.hasNext())
 		{
 			AvailableServiceBindingVO availableServiceBinding = (AvailableServiceBindingVO)iterator.next();
 			int index = 0;
 			Iterator sortedListIterator = sortedAvailableServiceBindings.iterator();
 			while(sortedListIterator.hasNext())
 			{
 				AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
 				
 				String currentAttribute = availableServiceBinding.getName();
 				String sortedAttribute  = sortedAvailableServiceBinding.getName();
 				
 				if(currentAttribute != null && sortedAttribute != null && currentAttribute.compareTo(sortedAttribute) < 0)
 		    	{
 		    		break;
 		    	}
 		    	index++;
 			}
 			sortedAvailableServiceBindings.add(index, availableServiceBinding);
 		}
 			
 		return sortedAvailableServiceBindings;
 	}
 	
 	/**
 	 * This method sorts a list of available service bindings on the name of the binding.
 	 */
 
 	public List getSortedAvailableContentServiceBindings()
 	{
 		List sortedAvailableContentServiceBindings = new ArrayList();
 		
 		Iterator sortedListIterator = getSortedAvailableServiceBindings().iterator();
 		while(sortedListIterator.hasNext())
 		{
 			AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
 			if(sortedAvailableServiceBinding.getVisualizationAction().indexOf("Structure") == -1)
 				sortedAvailableContentServiceBindings.add(sortedAvailableServiceBinding);
 		}
 			
 		return sortedAvailableContentServiceBindings;
 	}
 
 	/**
 	 * This method sorts a list of available service bindings on the name of the binding.
 	 */
 
 	public List getSortedAvailableStructureServiceBindings()
 	{
 		List sortedAvailableStructureServiceBindings = new ArrayList();
 		
 		Iterator sortedListIterator = getSortedAvailableServiceBindings().iterator();
 		while(sortedListIterator.hasNext())
 		{
 			AvailableServiceBindingVO sortedAvailableServiceBinding = (AvailableServiceBindingVO)sortedListIterator.next();
 			if(sortedAvailableServiceBinding.getVisualizationAction().indexOf("Structure") > -1)
 				sortedAvailableStructureServiceBindings.add(sortedAvailableServiceBinding);
 		}
 			
 		return sortedAvailableStructureServiceBindings;
 	}
 
 	
 	public List getServiceBindings()
 	{
 		return this.serviceBindings;
 	}	
 	
 	public String getStateDescription(Integer siteNodeId, Integer languageId)
 	{
 		String stateDescription = "Not created";
 		/*
 		try
 		{
 			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getLatestSiteNodeVersionVO(siteNodeId, languageId);
 			Integer stateId = siteNodeVersionVO.getStateId();
 			if(stateId.intValue() == 0)
 				stateDescription = "Working";
 			else if(stateId.intValue() == 2)
 				stateDescription = "Publish";
 		}
 		catch(Exception e)
 		{
 			//e.printStackTrace();
 		}
 		*/
 		return stateDescription;
 	}
 	
 	
 	/**
 	 * This method fetches a description of the qualifyer.
 	 */
 	
 	public String getQualifyerDescription(Integer serviceBindingId) throws Exception
 	{
 		String qualifyerDescription = "";
 		
 		List qualifyers = ServiceBindingController.getQualifyerVOList(serviceBindingId);
 		Iterator i = qualifyers.iterator();
 		while(i.hasNext())
 		{
 			QualifyerVO qualifyerVO = (QualifyerVO)i.next();
 			if(!qualifyerDescription.equalsIgnoreCase(""))
 				qualifyerDescription += ",";
 				
 			qualifyerDescription += qualifyerVO.getName() + "=" + qualifyerVO.getValue();
 		}
 		
 		return qualifyerDescription;
 	}
 	
 	public List getListPreparedQualifyers(Integer serviceBindingId) throws Exception
 	{
 		List qualifyers = ServiceBindingController.getQualifyerVOList(serviceBindingId);
 		Iterator i = qualifyers.iterator();
 		while(i.hasNext())
 		{
 			QualifyerVO qualifyerVO = (QualifyerVO)i.next();
 			if(qualifyerVO.getName().equalsIgnoreCase("contentid"))
 			{
 			    try {
 			        ContentVO contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), new Integer(qualifyerVO.getValue()));
 			        qualifyerVO.setPath(contentVO.getName());
 			    }
 			    catch(Exception e)
 			    {
 			    }
 			}
 		}
 		return qualifyers;
 	}
 	
 	/**
 	 * This method fetches the list of SiteNodeTypeDefinitions
 	 */
 	
 	public List getSiteNodeTypeDefinitions() throws Exception
 	{
 		return SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOList();
 	}      
     
     
     public EventVO getSiteNodeVersionEvent(Integer siteNodeVersionId)
 	{
 		EventVO eventVO = null;
 		try
 		{
 			List events = EventController.getEventVOListForEntity(SiteNodeVersion.class.getName(), siteNodeVersionId);
 			if(events != null && events.size() > 0)
 				eventVO = (EventVO)events.get(0);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred when we tried to get any events for this version:" + e.getMessage(), e);
 		}
 		
 		return eventVO;
 	}
 
 	public EventVO getSiteNodeEvent(Integer siteNodeId)
 	{
 		EventVO eventVO = null;
 		try
 		{
 			List events = EventController.getEventVOListForEntity(SiteNode.class.getName(), siteNodeId);
 			if(events != null && events.size() > 0)
 				eventVO = (EventVO)events.get(0);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred when we tried to get any events for this siteNode:" + e.getMessage(), e);
 		}
 		
 		return eventVO;
 	}
 	
 	public SiteNodeVersionVO getSiteNodeVersionVO()
 	{
 		return siteNodeVersionVO;
 	}
 
     public String getStay()
     {
         return stay;
     }
     
     public void setStay(String stay)
     {
         this.stay = stay;
     }
     
     public String getDest()
     {
         return dest;
     }
 }
