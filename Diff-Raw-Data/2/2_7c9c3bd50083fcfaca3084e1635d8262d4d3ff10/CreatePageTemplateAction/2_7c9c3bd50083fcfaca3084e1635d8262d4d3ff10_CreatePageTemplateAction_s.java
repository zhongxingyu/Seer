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
 
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.content.DigitalAssetVO;
 import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
 import org.infoglue.cms.entities.management.RepositoryVO;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.applications.contenttool.actions.ViewContentTreeActionInterface;
 
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
 import org.infoglue.cms.util.CmsLogger;
 
 import webwork.action.Action;
 import webwork.action.ActionContext;
 import webwork.multipart.MultiPartRequestWrapper;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.List;
 
 /**
  * This action represents the CreatePageTemplate Usecase.
  */
 
 public class CreatePageTemplateAction extends InfoGlueAbstractAction implements ViewContentTreeActionInterface
 {
 	//Used by the tree only
 	private List repositories;
 	private Integer contentId;
 	private String tree;
 	private String hideLeafs;
 	
 	private Integer parentContentId;
 	private Integer repositoryId;
 	private ConstraintExceptionBuffer ceb;
 
 	private Integer siteNodeId;
 	private String name;
 	
 	private String returnAddress;
 
 	
     public String doInput() throws Exception
     {
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal());
 
         return Action.INPUT;
     }
 
     public String doExecute() throws Exception
     {
         CmsLogger.logInfo("contentId:" + contentId);
         CmsLogger.logInfo("parentContentId:" + parentContentId);
         CmsLogger.logInfo("repositoryId:" + repositoryId);
         CmsLogger.logInfo("siteNodeId:" + siteNodeId);
         CmsLogger.logInfo("name:" + name);
         
         ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("PageTemplate");
         
         ContentVO contentVO = new ContentVO();
         
 		contentVO.setCreatorName(this.getInfoGluePrincipal().getName());
 		contentVO.setIsBranch(new Boolean(false));
 		contentVO.setName(name);
 		contentVO.setRepositoryId(this.repositoryId);
 
 		contentVO = ContentControllerProxy.getController().create(parentContentId, contentTypeDefinitionVO.getId(), this.repositoryId, contentVO);
 		
 		String componentStructure = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><components></components>";
 		
 		ContentVO metaInfoContentVO = ContentController.getContentController().getContentVOWithId(this.contentId);
 		Integer originalMetaInfoMasterLanguageId = LanguageController.getController().getMasterLanguage(metaInfoContentVO.getRepositoryId()).getId();
 		Integer destinationMasterLanguageId = LanguageController.getController().getMasterLanguage(this.repositoryId).getId();
 		ContentVersionVO originalContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.contentId, originalMetaInfoMasterLanguageId);
 		CmsLogger.logInfo("originalMetaInfoMasterLanguageId:" + originalMetaInfoMasterLanguageId);
 		CmsLogger.logInfo("contentId:" + contentId);
 		CmsLogger.logInfo("originalContentVersionVO:" + originalContentVersionVO);
 		
 	    componentStructure = ContentVersionController.getContentVersionController().getAttributeValue(originalContentVersionVO.getId(), "ComponentStructure", false);
 	    CmsLogger.logInfo("componentStructure:" + componentStructure);
 		
 		//Create initial content version also... in masterlanguage
 		String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes><Name><![CDATA[" + this.name + "]]></Name><ComponentStructure><![CDATA[" + componentStructure + "]]></ComponentStructure></attributes></article>";
 	
 		ContentVersionVO contentVersionVO = new ContentVersionVO();
 		contentVersionVO.setVersionComment("Saved page template");
 		contentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
 		contentVersionVO.setVersionValue(versionValue);
 		ContentVersionVO newContentVersion = ContentVersionController.getContentVersionController().create(contentVO.getId(), destinationMasterLanguageId, contentVersionVO, null);
         
 		
     	InputStream is = null;
 		File file = null;
 		
     	try 
     	{
     		MultiPartRequestWrapper mpr = ActionContext.getContext().getMultiPartRequest();
     		CmsLogger.logInfo("mpr:" + mpr);
     		if(mpr != null)
     		{ 
 	    		Enumeration names = mpr.getFileNames();
 	         	while (names.hasMoreElements()) 
 	         	{
 	            	String name 		  = (String)names.nextElement();
 					String contentType    = mpr.getContentType(name);
 					String fileSystemName = mpr.getFilesystemName(name);
 					
 					CmsLogger.logInfo("name:" + name);
 					CmsLogger.logInfo("contentType:" + contentType);
 					CmsLogger.logInfo("fileSystemName:" + fileSystemName);
 	            	
 	            	file = mpr.getFile(name);
 					String fileName = fileSystemName;
 					fileName = new VisualFormatter().replaceNonAscii(fileName, '_');
 					
 					String tempFileName = "tmp_" + System.currentTimeMillis() + "_" + fileName;
 	            	String filePath = CmsPropertyHandler.getProperty("digitalAssetPath");
 	            	fileSystemName = filePath + File.separator + tempFileName;
 	            	
 	            	DigitalAssetVO newAsset = new DigitalAssetVO();
 					newAsset.setAssetContentType(contentType);
 					newAsset.setAssetKey("thumbnail");
 					newAsset.setAssetFileName(fileName);
 					newAsset.setAssetFilePath(filePath);
 					newAsset.setAssetFileSize(new Integer(new Long(file.length()).intValue()));
 					is = new FileInputStream(file);
 					
 				    DigitalAssetController.create(newAsset, is, newContentVersion.getContentVersionId());	         		    
 	         	}
     		}
     		else
     		{
     		    CmsLogger.logSevere("File upload failed for some reason.");
     		}
       	} 
       	catch (Exception e) 
       	{
       		CmsLogger.logSevere("An error occurred when we tried to upload a new asset:" + e.getMessage(), e);
       	}
 		finally
 		{
 			try
 			{
 				is.close();
 				file.delete();
 			}
 			catch(Exception e){}
 		}
 
 		
 		
         return Action.SUCCESS;
     }
     
     
 	public Integer getTopRepositoryId() throws ConstraintException, SystemException, Bug
 	{
 		List repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal());
 		
 		Integer topRepositoryId = null;
 
 		if (repositoryId != null)
 			topRepositoryId = repositoryId;
 
 		if(repositories.size() > 0)
 		{
 			topRepositoryId = ((RepositoryVO)repositories.get(0)).getRepositoryId();
 		}
   	
 		return topRepositoryId;
 	}
   
 	public void setHideLeafs(String hideLeafs)
 	{
 		this.hideLeafs = hideLeafs;
 	}
 
 	public String getHideLeafs()
 	{
 		return this.hideLeafs;
 	}    
 
 	public String getTree()
 	{
 		return tree;
 	}
 
 	public void setTree(String tree)
 	{
 		this.tree = tree;
 	}
 
 	public void setParentContentId(Integer parentContentId)
 	{
 		this.parentContentId = parentContentId;
 	}
 
 	public Integer getParentContentId()
 	{
 		return this.parentContentId;
 	}
 	
 	public List getRepositories()
 	{
 		return this.repositories;
 	}  
 
 	public void setRepositoryId(Integer repositoryId)
 	{
 		this.repositoryId = repositoryId;
 	}
 
 	public Integer getRepositoryId() 
 	{
 		try
 		{
 			if(this.repositoryId == null)
 			{	
 				this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
 					
 				if(this.repositoryId == null)
 				{
 					this.repositoryId = getTopRepositoryId();
 					getHttpSession().setAttribute("repositoryId", this.repositoryId);		
 				}
 			}
 		}
 		catch(Exception e)
 		{
 		}
 	    	
 		return repositoryId;
 	}
 
 	public void setContentId(Integer contentId)
 	{
 		this.contentId = contentId;
 	}
 
 	public Integer getContentId()
 	{
 		return this.contentId;
 	}    
 	
 	public String getReturnAddress()
 	{
 		return returnAddress;
 	}
 
 	public void setReturnAddress(String string)
 	{
 		returnAddress = string;
 	}
     
 	public Integer getSiteNodeId()
     {
         return siteNodeId;
     }
 
     public void setSiteNodeId(Integer siteNodeId)
     {
         this.siteNodeId = siteNodeId;
     }
     
     public String getName()
     {
         return name;
     }
     public void setName(String name)
     {
         this.name = name;
     }
 }
