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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.InconsistenciesController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;
 import org.infoglue.cms.entities.content.ContentVO;
 
 /**
  * This action removes a content from the system.
  * 
  * @author Mattias Bogeblad
  */
 
 public class DeleteContentAction extends InfoGlueAbstractAction
 {
     private final static Logger logger = Logger.getLogger(DeleteContentAction.class.getName());
 
 	private static final long serialVersionUID = 1L;
 	
 	private ContentVO contentVO;
 	private Integer parentContentId;
 	private Integer changeTypeId;
 	private String[] registryId;
 	
 	//Used for the relatedPages control
 	private Integer siteNodeId;
 	
 	private List referenceBeanList = new ArrayList();
 	
 	public DeleteContentAction()
 	{
 		this(new ContentVO());
 	}
 
 	public DeleteContentAction(ContentVO contentVO) 
 	{
 		this.contentVO = contentVO;
 	}
 	
 	public String doExecute() throws Exception 
 	{
 		this.referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(this.contentVO.getContentId());
 		if(this.referenceBeanList != null && this.referenceBeanList.size() > 0)
 		{
 		    return "showRelations";
 		}
 	    else
 	    {
 	    	try
 			{
 				this.parentContentId = ContentController.getParentContent(this.contentVO.getContentId()).getContentId();
 			}
 			catch(Exception e)
 			{
 				logger.info("The content must have been a root-content because we could not find a parent.");
 			}
 
 	    	ContentControllerProxy.getController().acDelete(this.getInfoGluePrincipal(), this.contentVO);	    
 			
 	    	return "success";
 	    }
 	}	
 	
 	public String doStandalone() throws Exception 
 	{
 		this.referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(this.contentVO.getContentId());
 		if(this.referenceBeanList != null && this.referenceBeanList.size() > 0)
 		{
 		    return "showRelations";
 		}
 	    else
 	    {
 	    	try
 			{
 				this.parentContentId = ContentController.getParentContent(this.contentVO.getContentId()).getContentId();
 			}
 			catch(Exception e)
 			{
 			    logger.info("The content must have been a root-content because we could not find a parent.");
 			}
 
 	    	ContentControllerProxy.getController().acDelete(this.getInfoGluePrincipal(), this.contentVO);	    
 			
 	    	return "successStandalone";
 	    }
 	}	
 
 	public String doDeleteReference() throws Exception 
 	{
 	    for(int i=0; i<registryId.length; i++)
 	    {
	    	InconsistenciesController.getController().removeReferences(new Integer(registryId[i]), this.getInfoGluePrincipal());
	    	RegistryController.getController().delete(new Integer(registryId[i]));
 	    }
 	    
 	    return doExecute();
 	}	
 	
 	public String doFixPage() throws Exception 
 	{
 	    return "fixPage";
 	}
 
 	public String doFixPageHeader() throws Exception 
 	{
 	    return "fixPageHeader";
 	}
 
 	public void setContentId(Integer contentId)
 	{
 		this.contentVO.setContentId(contentId);
 	}
 
 	public void setParentContentId(Integer parentContentId)
 	{
 		this.parentContentId = parentContentId;
 	}
 
 	public void setChangeTypeId(Integer changeTypeId)
 	{
 		this.changeTypeId = changeTypeId;
 	}
 
 	public Integer getContentId()
 	{
 		return this.parentContentId;
 	}
 	
 	public Integer getOriginalContentId()
 	{
 		return this.contentVO.getContentId();
 	}
 	
 	public Integer getUnrefreshedContentId()
 	{
 		return this.parentContentId;
 	}
 	
 	public Integer getChangeTypeId()
 	{
 		return this.changeTypeId;
 	}
         
     public String getErrorKey()
 	{
 		return "ContentVersion.stateId";
 	}
 	
 	public String getReturnAddress()
 	{
 		return "ViewContent.action?contentId=" + this.contentVO.getId() + "&repositoryId=" + this.contentVO.getRepositoryId();
 	}
 
     public List getReferenceBeanList()
     {
         return referenceBeanList;
     }
     
     public Integer getSiteNodeId()
     {
         return siteNodeId;
     }
     
     public void setSiteNodeId(Integer siteNodeId)
     {
         this.siteNodeId = siteNodeId;
     }
     
     public String[] getRegistryId()
     {
         return registryId;
     }
     
     public void setRegistryId(String[] registryId)
     {
         this.registryId = registryId;
     }
 }
