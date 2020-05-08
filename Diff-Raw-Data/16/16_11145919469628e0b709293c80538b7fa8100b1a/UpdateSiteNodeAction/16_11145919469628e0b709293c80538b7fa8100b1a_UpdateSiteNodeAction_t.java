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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 import org.infoglue.cms.controllers.usecases.structuretool.UpdateSiteNodeUCC;
 import org.infoglue.cms.controllers.usecases.structuretool.UpdateSiteNodeUCCFactory;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.module.propertyset.PropertySetManager;
 
  
 /**
   * This is the action-class for UpdateSiteNode
   * 
   * @author Mattias Bogeblad
   */
 public class UpdateSiteNodeAction extends ViewSiteNodeAction //WebworkAbstractAction
 {
 	private SiteNodeVO siteNodeVO;
 	private Integer siteNodeId;
 	private Integer repositoryId;
 	private Integer siteNodeTypeDefinitionId;
     private String name;
     private Boolean isBranch;
     
 	private Integer isProtected;
 	private Integer disablePageCache;
 	private Integer disableEditOnSight;
 	private Integer disableLanguages;
 	private String contentType;
 	private String pageCacheKey;
 
 	private ConstraintExceptionBuffer ceb;
 	
 	public UpdateSiteNodeAction()
 	{
 		this(new SiteNodeVO());
 	}
 	
 	public UpdateSiteNodeAction(SiteNodeVO siteNodeVO)
 	{
 		this.siteNodeVO = siteNodeVO;
 		this.ceb 		= new ConstraintExceptionBuffer();	
 	}
 	
 	public String doExecute() throws Exception
     {
 		//try
 		//{
 			super.initialize(getSiteNodeId());
 			SiteNodeVO oldSiteNodeVO = SiteNodeController.getSiteNodeVOWithId(getSiteNodeId());
 
 			this.siteNodeVO.setCreatorName(this.getInfoGluePrincipal().getName());
 			this.siteNodeVO.setMetaInfoContentId(oldSiteNodeVO.getMetaInfoContentId());
 			ceb = this.siteNodeVO.validate();
 	    	
 			ceb.throwIfNotEmpty();
 
 			SiteNodeVersionVO siteNodeVersionVO = new SiteNodeVersionVO();
 			siteNodeVersionVO.setContentType(this.getContentType());
 			siteNodeVersionVO.setPageCacheKey(this.getPageCacheKey());
 			siteNodeVersionVO.setDisableEditOnSight(this.getDisableEditOnSight());
 			siteNodeVersionVO.setDisableLanguages(this.disableLanguages);
 			siteNodeVersionVO.setDisablePageCache(this.getDisablePageCache());
 			siteNodeVersionVO.setIsProtected(this.getIsProtected());
 			siteNodeVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
 			
 			UpdateSiteNodeUCC updateSiteNodeUCC = UpdateSiteNodeUCCFactory.newUpdateSiteNodeUCC();
 			updateSiteNodeUCC.updateSiteNode(this.getInfoGluePrincipal(), this.siteNodeVO, this.siteNodeTypeDefinitionId, siteNodeVersionVO);		
 			
 	    	String[] values = getRequest().getParameterValues("disabledLanguageId");
 	    	String valueString = "";
 	    	if(values != null)
 	    	{
	    		for(int i=0; i<values.length; i++)
 		    	{
 		    	    if(i > 0)
 		    	        valueString = valueString + ",";
 		    	    valueString = valueString + values[i];  
 		    	}

		    	Map args = new HashMap();
			    args.put("globalKey", "infoglue");
			    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

		    	ps.setString("siteNode_" + getSiteNodeId() + "_disabledLanguages", valueString);
 	    	}
 
 		//}
 		//catch(Exception e)
 		//{
 		//	e.printStackTrace();
 		//}
 			
 		return "success";
 	}
 
 	public String doSaveAndExit() throws Exception
     {
 		doExecute();
 						
 		return "saveAndExit";
 	}
 
 	public void setSiteNodeId(Integer siteNodeId)
 	{
 		this.siteNodeVO.setSiteNodeId(siteNodeId);	
 	}
 
     public java.lang.Integer getSiteNodeId()
     {
         return this.siteNodeVO.getSiteNodeId();
     }
 
     public java.lang.String getName()
     {
         return this.siteNodeVO.getName();
     }
  
 	public Boolean getIsBranch()
 	{
  		return this.siteNodeVO.getIsBranch();
 	}    
         
     public void setName(java.lang.String name)
     {
         this.siteNodeVO.setName(name);
     }
     
    	public void setPublishDateTime(String publishDateTime)
     {
    		this.siteNodeVO.setPublishDateTime(new VisualFormatter().parseDate(publishDateTime, "yyyy-MM-dd HH:mm"));
     }
 
     public void setExpireDateTime(String expireDateTime)
     {
        	this.siteNodeVO.setExpireDateTime(new VisualFormatter().parseDate(expireDateTime, "yyyy-MM-dd HH:mm"));
 	}
 
     public void setIsBranch(Boolean isBranch)
     {
        	this.siteNodeVO.setIsBranch(isBranch);
     }
 
 	public void setSiteNodeTypeDefinitionId(Integer siteNodeTypeDefinitionId)
 	{
 		this.siteNodeTypeDefinitionId = siteNodeTypeDefinitionId;	
 	}
 
     public java.lang.Integer getSiteNodeTypeDefinitionId()
     {
         return this.siteNodeTypeDefinitionId;
     }
     
     public void setRepositoryId(Integer repositoryId)
 	{
 		this.repositoryId = repositoryId;
 	}
 
 	public Integer getRepositoryId()
 	{
 		return this.repositoryId;
 	}
 
 	public String getContentType()
 	{
 		return this.contentType;
 	}
 
 	public void setContentType(String contentType)
 	{
 		this.contentType = contentType;
 	}
 
 	public Integer getDisableEditOnSight()
 	{
 		return this.disableEditOnSight;
 	}
 
 	public void setDisableEditOnSight(Integer disableEditOnSight)
 	{
 		this.disableEditOnSight = disableEditOnSight;
 	}
 
 	public Integer getDisableLanguages()
 	{
 		return this.disableLanguages;
 	}
 
 	public void setDisableLanguages(Integer disableLanguages)
 	{
 		this.disableLanguages = disableLanguages;
 	}
 
 	public Integer getDisablePageCache()
 	{
 		return this.disablePageCache;
 	}
 
 	public void setDisablePageCache(Integer disablePageCache)
 	{
 		this.disablePageCache = disablePageCache;
 	}
 
 	public Integer getIsProtected()
 	{
 		return this.isProtected;
 	}
 
 	public void setIsProtected(Integer isProtected)
 	{
 		this.isProtected = isProtected;
 	}
 
     public String getPageCacheKey()
     {
         return pageCacheKey;
     }
     
     public void setPageCacheKey(String pageCacheKey)
     {
         this.pageCacheKey = pageCacheKey;
     }
 }
