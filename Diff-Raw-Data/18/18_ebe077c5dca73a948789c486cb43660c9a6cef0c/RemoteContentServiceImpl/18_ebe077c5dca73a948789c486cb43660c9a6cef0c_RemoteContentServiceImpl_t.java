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
 
 package org.infoglue.cms.webservices;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
 import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.content.DigitalAssetVO;
 import org.infoglue.cms.entities.publishing.PublicationVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.dom.DOMBuilder;
 import org.infoglue.cms.webservices.elements.RemoteAttachment;
 import org.infoglue.deliver.util.webservices.DynamicWebserviceSerializer;
 
 
 /**
  * This class is responsible for letting an external application call InfoGlue
  * API:s remotely. It handles api:s to manage contents and associated entities.
  * 
  * @author Mattias Bogeblad
  */
 
 public class RemoteContentServiceImpl 
 {
     private final static Logger logger = Logger.getLogger(RemoteContentServiceImpl.class.getName());
 
 	/**
 	 * The principal executing the workflow.
 	 */
 	private InfoGluePrincipal principal;
 
     private static ContentControllerProxy contentControllerProxy = ContentControllerProxy.getController();
     private static ContentVersionControllerProxy contentVersionControllerProxy = ContentVersionControllerProxy.getController();
     
     /**
      * Inserts a new Content including versions etc.
      */
     
     public int createContent(final String principalName, ContentVO contentVO, int parentContentId, int contentTypeDefinitionId, int repositoryId) 
     {
         int newContentId = 0;
         
         logger.info("***************************************");
         logger.info("Creating content through webservice....");
         logger.info("***************************************");
         
         logger.info("parentContentId:" + parentContentId);
         logger.info("contentTypeDefinitionId:" + contentTypeDefinitionId);
         logger.info("repositoryId:" + repositoryId);
         
         try
         {
             initializePrincipal(principalName);
 	        ContentVO newContentVO = contentControllerProxy.acCreate(this.principal, new Integer(parentContentId), new Integer(contentTypeDefinitionId), new Integer(repositoryId), contentVO);
 	        newContentId = newContentVO.getId().intValue();
         }
         catch(Exception e)
         {
             logger.error("En error occurred when we tried to create a new content:" + e.getMessage(), e);
         }
         
         return newContentId;
     }
     
     /**
      * Inserts a new ContentVersion.
      */
     
     public int createContentVersion(final String principalName, ContentVersionVO contentVersionVO, int contentId, int languageId) 
     {
         int newContentVersionId = 0;
         
         logger.info("***************************************");
         logger.info("Creating content through webservice....");
         logger.info("***************************************");
         
         try
         {
             initializePrincipal(principalName);
 	        ContentVersionVO newContentVersionVO = contentVersionControllerProxy.acCreate(this.principal, new Integer(contentId), new Integer(languageId), contentVersionVO);
 	        newContentVersionId = newContentVersionVO.getId().intValue();
         }
         catch(Exception e)
         {
             logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
         }
         
         return newContentVersionId;
     }
     
  
     /**
      * Inserts a new Content including versions etc.
      */
     
     public Boolean createContents(final String principalName, final Object[] inputsArray/*List contents*/) 
     {
         List newContentIdList = new ArrayList();
         
         logger.info("****************************************");
         logger.info("Creating contents through webservice....");
         logger.info("****************************************");
         
         logger.info("principalName:" + principalName);
         logger.info("inputsArray:" + inputsArray);
         //logger.warn("contents:" + contents);
         
         try
         {
 			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
             List contents = (List) serializer.deserialize(inputsArray);
 	        logger.info("contents:" + contents);
 
             initializePrincipal(principalName);
 	        Iterator contentIterator = contents.iterator();
 	        while(contentIterator.hasNext())
 	        {
 	            //String value = (String)contentIterator.next();
 	            //logger.info("value:" + value);
 	            
 	            Map content = (Map)contentIterator.next();
 	            
 	            String name 					= (String)content.get("name");
 	            Integer contentTypeDefinitionId = (Integer)content.get("contentTypeDefinitionId");
 	            Integer repositoryId 			= (Integer)content.get("repositoryId");
 	            Integer parentContentId 		= (Integer)content.get("parentContentId");
 	            
 	            logger.info("name:" + name);
 	            logger.info("contentTypeDefinitionId:" + contentTypeDefinitionId);
 	            logger.info("repositoryId:" + repositoryId);
 	            logger.info("parentContentId:" + parentContentId);
 	            
 	           	ContentVO contentVO = new ContentVO();
 	            contentVO.setName(name);
 	            contentVO.setContentTypeDefinitionId(contentTypeDefinitionId);
 	            contentVO.setRepositoryId(repositoryId);
 	            contentVO.setParentContentId(parentContentId);
 	            
 	            if(contentVO.getCreatorName() == null)
 	                contentVO.setCreatorName(this.principal.getName());
 	            
 	            ContentVO newContentVO = contentControllerProxy.acCreate(this.principal, contentVO.getParentContentId(), contentVO.getContentTypeDefinitionId(), contentVO.getRepositoryId(), contentVO);
 	            
 	            List contentVersions = (List)content.get("contentVersions");
 	            Iterator contentVersionIterator = contentVersions.iterator();
 	            while(contentVersionIterator.hasNext())
 	            {
 	                Map contentVersion = (Map)contentVersionIterator.next();
 	                
 	                Integer languageId = (Integer)contentVersion.get("languageId");
 	                Integer stateId = (Integer)contentVersion.get("stateId");
 	                
 	    	        logger.info("languageId:" + languageId);
 	    	        logger.info("stateId:" + stateId);
 
 	                ContentVersionVO contentVersionVO = new ContentVersionVO();
 	                contentVersionVO.setLanguageId(languageId);
 	                
 	                if(contentVersionVO.getVersionModifier() == null)
 	                    contentVersionVO.setVersionModifier(this.principal.getName());
 	                
 	                Map attributes = (Map)contentVersion.get("contentVersionAttributes");
 	                
 	                DOMBuilder domBuilder = new DOMBuilder();
 	                Document document = domBuilder.createDocument();
 	                
 	                Element rootElement = domBuilder.addElement(document, "root");
 	                domBuilder.addAttribute(rootElement, "xmlns", "x-schema:Schema.xml");
 	                Element attributesRoot = domBuilder.addElement(rootElement, "attributes");
 	                
 	                Iterator attributesIterator = attributes.keySet().iterator();
 	                while(attributesIterator.hasNext())
 	                {
 	                    String attributeName  = (String)attributesIterator.next();
 	                    String attributeValue = (String)attributes.get(attributeName);
 		                
 	                    Element attribute = domBuilder.addElement(attributesRoot, attributeName);
 		                domBuilder.addCDATAElement(attribute, attributeValue);
 	                }	                
 
 	                contentVersionVO.setVersionValue(document.asXML());
 	                
 	    	        ContentVersionVO newContentVersionVO = contentVersionControllerProxy.acCreate(this.principal, newContentVO.getId(), languageId, contentVersionVO);
 	    	        Integer newContentVersionId = newContentVersionVO.getId();
 	    	        
 	    	        List digitalAssets = (List)contentVersion.get("digitalAssets");
 	    	        
 	    	        logger.info("digitalAssets:" + digitalAssets);
 	    	        if(digitalAssets != null)
 	    	        {
 		    	        Iterator digitalAssetIterator = digitalAssets.iterator();
 		    	        while(digitalAssetIterator.hasNext())
 		    	        {
 		    	            RemoteAttachment remoteAttachment = (RemoteAttachment)digitalAssetIterator.next();
 			    	        logger.info("digitalAssets in ws:" + remoteAttachment);
 			    	        
 			            	DigitalAssetVO newAsset = new DigitalAssetVO();
 							newAsset.setAssetContentType(remoteAttachment.getContentType());
 							newAsset.setAssetKey(remoteAttachment.getName());
 							newAsset.setAssetFileName("Unknown");
 							newAsset.setAssetFilePath("Unknown");
 							newAsset.setAssetFileSize(new Integer(new Long(remoteAttachment.getBytes().length).intValue()));
 							//is = new FileInputStream(renamedFile);
 							InputStream is = new ByteArrayInputStream(remoteAttachment.getBytes());
 		
 			    	        DigitalAssetController.create(newAsset, is, newContentVersionId);
 			    	    }	 
 	    	        }
 	    	        
 	    	        //Should we also change state on newly created content version?
 	    	        if(stateId != null && !stateId.equals(ContentVersionVO.WORKING_STATE))
 	    	        {
 	    	        	List events = new ArrayList();
 	    	    		ContentStateController.changeState(newContentVersionId, stateId, "Remote update from deliver", false, this.principal, newContentVO.getId(), events);
 	    	        
 	    	    		if(stateId.equals(ContentVersionVO.PUBLISHED_STATE))
 	    	    		{
 	    	    		    PublicationVO publicationVO = new PublicationVO();
 	    	    		    publicationVO.setName("Direct publication by " + this.principal.getName());
 	    	    		    publicationVO.setDescription("Direct publication from deliver");
 	    	    		    publicationVO.setRepositoryId(repositoryId);
 	    	    		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, false, this.principal);
 	    	    		}
 
 	    	        }
 	            }
 	            
 	            newContentIdList.add(newContentVO.getId());
 	            
 	        }
 	        logger.info("Done with contents..");
 
         }
         catch(Throwable e)
         {
             logger.error("En error occurred when we tried to create a new content:" + e.getMessage(), e);
         }
         
         return new Boolean(true);
     }
 
     
     /**
      * Updates a content.
      */
     
     public Boolean updateContent(final String principalName, final Object[] inputsArray) 
     {
         List newContentIdList = new ArrayList();
         
         logger.info("****************************************");
         logger.info("Updating content through webservice....");
         logger.info("****************************************");
         
         logger.info("principalName:" + principalName);
         logger.info("inputsArray:" + inputsArray);
         //logger.warn("contents:" + contents);
         
         try
         {
 			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
             Map content = (Map) serializer.deserialize(inputsArray);
 	        logger.info("content:" + content);
 
             initializePrincipal(principalName);
             
             Integer contentId 				= (Integer)content.get("contentId");
             String name 					= (String)content.get("name");
             Date publishDateTime 			= (Date)content.get("publishDateTime");
             Date expireDateTime 			= (Date)content.get("expireDateTime");
             
             logger.info("contentId:" + contentId);
             logger.info("name:" + name);
             logger.info("publishDateTime:" + publishDateTime);
             logger.info("expireDateTime:" + expireDateTime);
             
             ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
 
             if(name != null)
                 contentVO.setName(name);
 
             if(publishDateTime != null)
                 contentVO.setPublishDateTime(publishDateTime);
 
             if(expireDateTime != null)
                 contentVO.setExpireDateTime(expireDateTime);
 
             ContentVO newContentVO = contentControllerProxy.acUpdate(this.principal, contentVO, null);
 	           
 	        logger.info("Done with contents..");
 
         }
         catch(Throwable e)
         {
             logger.error("En error occurred when we tried to create a new content:" + e.getMessage(), e);
         }
         
         return new Boolean(true);
     }
 
     /**
      * Updates a content.
      */
     
     public Boolean updateContentVersion(final String principalName, final Object[] inputsArray) 
     {
         List newContentIdList = new ArrayList();
         
         logger.info("****************************************");
         logger.info("Updating content versions through webservice....");
         logger.info("****************************************");
         
         logger.info("principalName:" + principalName);
         logger.info("inputsArray:" + inputsArray);
         //logger.warn("contents:" + contents);
         
         try
         {
 			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
             Map contentVersion = (Map) serializer.deserialize(inputsArray);
 	        logger.info("contentVersion:" + contentVersion);
 
             initializePrincipal(principalName);
             
             Integer contentVersionId = (Integer)contentVersion.get("contentVersionId");
             Integer contentId 		 = (Integer)contentVersion.get("contentId");
             Integer languageId 		 = (Integer)contentVersion.get("languageId");
             Integer stateId			 = (Integer)contentVersion.get("stateId");
             String versionComment 	 = (String)contentVersion.get("versionComment");
             
             logger.info("contentVersionId:" + contentVersionId);
             logger.info("contentId:" + contentId);
             logger.info("languageId:" + languageId);
             logger.info("stateId:" + stateId);
             logger.info("versionComment:" + versionComment);
 
             ContentVersionVO contentVersionVO = null;
             if(contentVersionId != null)
                 contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId);
             else
                 contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId);
                 
             if(contentVersionVO == null)
                 return new Boolean(false);
                 
             contentVersionVO.setVersionComment(versionComment);
             
             Map attributes = (Map)contentVersion.get("contentVersionAttributes");
             
             if(attributes != null && attributes.size() > 0)
             {
 	            DOMBuilder domBuilder = new DOMBuilder();
 	            Document document = domBuilder.createDocument();
 	            
 	            Element rootElement = domBuilder.addElement(document, "root");
 	            domBuilder.addAttribute(rootElement, "xmlns", "x-schema:Schema.xml");
 	            Element attributesRoot = domBuilder.addElement(rootElement, "attributes");
 	            
 	            Iterator attributesIterator = attributes.keySet().iterator();
 	            while(attributesIterator.hasNext())
 	            {
 	                String attributeName  = (String)attributesIterator.next();
 	                String attributeValue = (String)attributes.get(attributeName);
 	                
 	                Element attribute = domBuilder.addElement(attributesRoot, attributeName);
 	                domBuilder.addCDATAElement(attribute, attributeValue);
 	            }	                
 
 	            contentVersionVO.setVersionValue(document.asXML());
             }
             
             ContentVersionControllerProxy.getController().acUpdate(principal, contentId, languageId, contentVersionVO);
             
             //Assets now
             List digitalAssets = (List)contentVersion.get("digitalAssets");
 	        
             if(digitalAssets != null)
             {
 		        logger.info("digitalAssets:" + digitalAssets.size());
 		        
 		        Iterator digitalAssetIterator = digitalAssets.iterator();
 		        while(digitalAssetIterator.hasNext())
 		        {
 		            RemoteAttachment remoteAttachment = (RemoteAttachment)digitalAssetIterator.next();
 	    	        logger.info("digitalAssets in ws:" + remoteAttachment);
 	    	        
 	            	DigitalAssetVO newAsset = new DigitalAssetVO();
 					newAsset.setAssetContentType(remoteAttachment.getContentType());
 					newAsset.setAssetKey(remoteAttachment.getName());
 					newAsset.setAssetFileName("Unknown");
 					newAsset.setAssetFilePath("Unknown");
 					newAsset.setAssetFileSize(new Integer(new Long(remoteAttachment.getBytes().length).intValue()));
 					InputStream is = new ByteArrayInputStream(remoteAttachment.getBytes());
 	
 	    	        DigitalAssetController.create(newAsset, is, contentVersionVO.getContentVersionId());
 	    	    }
             }
             
	        //Should we also change state on newly created content version?
	        if(stateId != null && !stateId.equals(ContentVersionVO.WORKING_STATE))
	        {
	        	List events = new ArrayList();
	    		ContentStateController.changeState(contentVersionVO.getId(), stateId, "Remote update from deliver", false, this.principal, contentVersionVO.getContentId(), events);
	        
	    		if(stateId.equals(ContentVersionVO.PUBLISHED_STATE))
	    		{
	    		    PublicationVO publicationVO = new PublicationVO();
	    		    publicationVO.setName("Direct publication by " + this.principal.getName());
	    		    publicationVO.setDescription("Direct publication from deliver");
	    		    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId());
	    		    publicationVO.setRepositoryId(contentVO.getRepositoryId());
	    		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, false, this.principal);
	    		}

	        }
            
 	        logger.info("Done with contentVersion..");
 
         }
         catch(Throwable e)
         {
             logger.error("En error occurred when we tried to create a new content:" + e.getMessage(), e);
         }
         
         return new Boolean(true);
     }
 
     
     
     /**
      * Deletes a digital asset.
      */
     
     public Boolean deleteDigitalAsset(final String principalName, final Object[] inputsArray) 
     {
         List newContentIdList = new ArrayList();
         
         logger.info("****************************************");
         logger.info("Updating content through webservice....");
         logger.info("****************************************");
         
         logger.info("principalName:" + principalName);
         logger.info("inputsArray:" + inputsArray);
         //logger.warn("contents:" + contents);
         
         try
         {
 			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
             Map digitalAsset = (Map) serializer.deserialize(inputsArray);
 	        logger.info("digitalAsset:" + digitalAsset);
 
             initializePrincipal(principalName);
             
             Integer contentVersionId = (Integer)digitalAsset.get("contentVersionId");
             Integer contentId 		 = (Integer)digitalAsset.get("contentId");
             Integer languageId 		 = (Integer)digitalAsset.get("languageId");
             String assetKey 		 = (String)digitalAsset.get("assetKey");
             
             logger.info("contentVersionId:" + contentVersionId);
             logger.info("contentId:" + contentId);
             logger.info("languageId:" + languageId);
             logger.info("assetKey:" + assetKey);
             
             ContentVersionController.getContentVersionController().deleteDigitalAsset(contentId, languageId, assetKey);
                
 	        logger.info("Done with contents..");
 
         }
         catch(Throwable e)
         {
             logger.error("En error occurred when we tried to delete a digitalAsset:" + e.getMessage(), e);
         }
         
         return new Boolean(true);
     }
 
     /**
      * Deletes a content.
      */
     
     public Boolean deleteContent(final String principalName, final Object[] inputsArray) 
     {
         List newContentIdList = new ArrayList();
         
         logger.info("****************************************");
         logger.info("Updating content through webservice....");
         logger.info("****************************************");
         
         logger.info("principalName:" + principalName);
         logger.info("inputsArray:" + inputsArray);
         //logger.warn("contents:" + contents);
         
         try
         {
 			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
             Map digitalAsset = (Map) serializer.deserialize(inputsArray);
 	        logger.info("digitalAsset:" + digitalAsset);
 
             initializePrincipal(principalName);
             
             Integer contentId = (Integer)digitalAsset.get("contentId");
             
             logger.info("contentId:" + contentId);
             
             ContentVO contentVO = new ContentVO();
             contentVO.setContentId(contentId);
             
             ContentController.getContentController().delete(contentVO);
                
 	        logger.info("Done with contents..");
 
         }
         catch(Throwable e)
         {
             logger.error("En error occurred when we tried to delete a digitalAsset:" + e.getMessage(), e);
         }
         
         return new Boolean(true);
     }
 
     /**
      * Deletes a content.
      */
     
     public Boolean deleteContentVersion(final String principalName, final Object[] inputsArray) 
     {
         List newContentIdList = new ArrayList();
         
         logger.info("****************************************");
         logger.info("Updating content through webservice....");
         logger.info("****************************************");
         
         logger.info("principalName:" + principalName);
         logger.info("inputsArray:" + inputsArray);
         //logger.warn("contents:" + contents);
         
         try
         {
 			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
             Map digitalAsset = (Map) serializer.deserialize(inputsArray);
 	        logger.info("digitalAsset:" + digitalAsset);
 
             initializePrincipal(principalName);
             
             Integer contentVersionId = (Integer)digitalAsset.get("contentVersionId");
             
             logger.info("contentVersionId:" + contentVersionId);
             ContentVersionVO contentVersionVO = new ContentVersionVO(); 
             contentVersionVO.setContentVersionId(contentVersionId);
             
             ContentVersionController.getContentVersionController().delete(contentVersionVO);
                
 	        logger.info("Done with contents..");
 
         }
         catch(Throwable e)
         {
             logger.error("En error occurred when we tried to delete a digitalAsset:" + e.getMessage(), e);
         }
         
         return new Boolean(true);
     }
 
     
 	/**
 	 * Checks if the principal exists and if the principal is allowed to create the workflow.
 	 * 
 	 * @param userName the name of the user.
 	 * @param workflowName the name of the workflow to create.
 	 * @throws SystemException if the principal doesn't exists or doesn't have permission to create the workflow.
 	 */
 	private void initializePrincipal(final String userName) throws SystemException 
 	{
 		try 
 		{
 			principal = UserControllerProxy.getController().getUser(userName);
 		}
 		catch(SystemException e)
 		{
 			throw e;
 		}
 		catch(Exception e)
 		{
 			throw new SystemException(e);
 		}
 		if(principal == null) 
 		{
 			throw new SystemException("No such principal [" + userName + "].");
 		}
 	}
 
 
 }
