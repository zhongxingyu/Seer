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
  *
 * $Id: RegistryController.java,v 1.16 2005/06/15 21:30:17 mattias Exp $
  */
 
 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 import org.infoglue.cms.applications.databeans.ReferenceBean;
 import org.infoglue.cms.applications.databeans.ReferenceVersionBean;
 import org.infoglue.cms.entities.content.Content;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.management.CategoryVO;
 import org.infoglue.cms.entities.management.Registry;
 import org.infoglue.cms.entities.management.RegistryVO;
 import org.infoglue.cms.entities.management.impl.simple.RegistryImpl;
 import org.infoglue.cms.entities.structure.Qualifyer;
 import org.infoglue.cms.entities.structure.ServiceBinding;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.SiteNodeVersion;
 import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.CmsLogger;
 
 /**
  * The RegistryController manages the registry-parts of InfoGlue. 
  * The Registry is metadata for how things are related - especially to handle bindings and inline links etc
  * when looking them up in the model is to slow.
  *
  * @author Mattias Bogeblad
  */
 
 public class RegistryController extends BaseController
 {
 	private static final RegistryController instance = new RegistryController();
 	
 	public static RegistryController getController()
 	{ 
 	    return instance; 
 	}
 
 	private RegistryController()
 	{
 	}
 	
 	/**
 	 * This method creates a registry entity in the db.
 	 * @param valueObject
 	 * @return
 	 * @throws ConstraintException
 	 * @throws SystemException
 	 */
 	
     public RegistryVO create(RegistryVO valueObject, Database db) throws ConstraintException, SystemException, Exception
     {
         Registry registry = new RegistryImpl();
         registry.setValueObject(valueObject);
         db.create(registry);
         return registry.getValueObject();
     }     
 
     /**
      * This method updates a registry entry
      * @param vo
      * @return
      * @throws ConstraintException
      * @throws SystemException
      */
     
     public RegistryVO update(RegistryVO valueObject, Database db) throws ConstraintException, SystemException
     {
     	return (RegistryVO) updateEntity(RegistryImpl.class, (BaseEntityVO) valueObject, db);
     }    
     
     
     /**
      * This method deletes a registry entry
      * @return registryId
      * @throws ConstraintException
      * @throws SystemException
      */
     
     public void delete(Integer registryId) throws ConstraintException, SystemException
     {
     	this.deleteEntity(RegistryImpl.class, registryId);
     }   
     
 	/**
 	 * this method goes through all inline stuff and all relations if ordinary content 
 	 * and all components and bindings if a metainfo.
 	 * 
 	 * @param contentVersionVO
 	 * @throws SystemException
 	 * @throws Exception
 	 */
 	
 	public void updateContentVersion(ContentVersionVO contentVersionVO) throws ConstraintException, SystemException
 	{
 	    String versionValue = contentVersionVO.getVersionValue();
 	    
 	    Database db = CastorDatabaseService.getDatabase();
 		
 		try 
 		{
 			beginTransaction(db);
 		
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionVO.getContentVersionId(), db);
 			updateContentVersion(contentVersion, db);
 		    
 			commitTransaction(db);
 		}
 		catch (Exception e)		
 		{
 		    rollbackTransaction(db);
 			throw new SystemException("An error occurred when we tried to fetch which sitenode uses a metainfo. Reason:" + e.getMessage(), e);			
 		}
 	}
 
 	
 	
 	/**
 	 * this method goes through all inline stuff and all relations if ordinary content 
 	 * and all components and bindings if a metainfo.
 	 * 
 	 * @param contentVersionVO
 	 * @throws SystemException
 	 * @throws Exception
 	 */
 	
 	public void updateContentVersion(ContentVersion contentVersion, Database db) throws ConstraintException, SystemException, Exception
 	{
 	    String versionValue = contentVersion.getVersionValue();
 	    
 	    ContentVersion oldContentVersion = contentVersion; //ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionVO.getContentVersionId(), db);
 	    Content oldContent = oldContentVersion.getOwningContent();
 	    
 	    if(oldContent.getContentTypeDefinition().getName().equalsIgnoreCase("Meta info"))
 	    {
 	        CmsLogger.logInfo("It was a meta info so lets check it for other stuff as well");
 		    
 	        List siteNodeVersions = getSiteNodeVersionsWhichUsesContentVersionAsMetaInfo(oldContentVersion, db);
 	        CmsLogger.logInfo("siteNodeVersions:" + siteNodeVersions.size());
 	        Iterator siteNodeVersionsIterator = siteNodeVersions.iterator();
 	        while(siteNodeVersionsIterator.hasNext())
 	        {
 		        SiteNodeVersion siteNodeVersion = (SiteNodeVersion)siteNodeVersionsIterator.next();
 			    if(siteNodeVersion != null)
 			    {
 			        CmsLogger.logInfo("Going to use " + siteNodeVersion.getId() + " as reference");
 			        clearRegistryVOList(SiteNodeVersion.class.getName(), siteNodeVersion.getId().toString(), db);
 				    
 				    getComponents(siteNodeVersion, versionValue, db);
 				    getComponentBindings(siteNodeVersion, versionValue, db);
 			    }
 	        }
 	        
 		    getInlineSiteNodes(oldContentVersion, versionValue, db);
 		    getInlineContents(oldContentVersion, versionValue, db);
 		    getRelationSiteNodes(oldContentVersion, versionValue, db);
 		    getRelationContents(oldContentVersion, versionValue, db);
 	    }
 	    else
 	    {
 	        clearRegistryVOList(ContentVersion.class.getName(), oldContentVersion.getContentVersionId().toString(), db);
 		    getInlineSiteNodes(oldContentVersion, versionValue, db);
 		    getInlineContents(oldContentVersion, versionValue, db);
 		    getRelationSiteNodes(oldContentVersion, versionValue, db);
 		    getRelationContents(oldContentVersion, versionValue, db);
 	    }		
 	}
 	
 
 	/**
 	 * this method goes through all page bindings and makes registry entries for them
 	 * 
 	 * @param siteNodeVersion
 	 * @throws SystemException
 	 * @throws Exception
 	 */
 	
 	public void updateSiteNodeVersion(SiteNodeVersionVO siteNodeVersionVO) throws ConstraintException, SystemException
 	{
 	    Database db = CastorDatabaseService.getDatabase();
 		
 		try 
 		{
 			beginTransaction(db);
 		
 			SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionVO.getId(), db);
 			updateSiteNodeVersion(siteNodeVersion, db);
 		    
 			commitTransaction(db);
 		}
 		catch (Exception e)		
 		{
 		    rollbackTransaction(db);
 			throw new SystemException("An error occurred when we tried to fetch which sitenode uses a metainfo. Reason:" + e.getMessage(), e);			
 		}
 	}
 
 	/**
 	 * this method goes through all page bindings and makes registry entries for them
 	 * 
 	 * @param siteNodeVersion
 	 * @throws SystemException
 	 * @throws Exception
 	 */
 	
 	public void updateSiteNodeVersion(SiteNodeVersion siteNodeVersion, Database db) throws ConstraintException, SystemException, Exception
 	{
 	    SiteNodeVersion oldSiteNodeVersion = siteNodeVersion;
 	    SiteNode oldSiteNode = oldSiteNodeVersion.getOwningSiteNode();
 	    
 	    clearRegistryVOList(SiteNodeVersion.class.getName(), siteNodeVersion.getId().toString(), db);
 	    
 		Collection serviceBindings = siteNodeVersion.getServiceBindings();
 		Iterator serviceBindingIterator = serviceBindings.iterator();
 		while(serviceBindingIterator.hasNext())
 		{
 		    ServiceBinding serviceBinding = (ServiceBinding)serviceBindingIterator.next();
 		    if(serviceBinding.getBindingQualifyers() != null)
 		    {
 			    Iterator qualifyersIterator = serviceBinding.getBindingQualifyers().iterator();
 			    while(qualifyersIterator.hasNext())
 			    {
 			        Qualifyer qualifyer = (Qualifyer)qualifyersIterator.next();
 			        String name = qualifyer.getName();
 			        String value = qualifyer.getValue();
 	
 	                try
 	                {
 				        RegistryVO registryVO = new RegistryVO();
 				        registryVO.setReferenceType(RegistryVO.PAGE_BINDING);
 			            if(name.equalsIgnoreCase("contentId"))
 				        {
 			                Content content = ContentController.getContentController().getContentWithId(new Integer(value), db);
			            
 			                registryVO.setEntityId(value);
 				            registryVO.setEntityName(Content.class.getName());
 				            
 				            registryVO.setReferencingEntityId(siteNodeVersion.getId().toString());
 				            registryVO.setReferencingEntityName(SiteNodeVersion.class.getName());
 				            registryVO.setReferencingEntityCompletingId(oldSiteNode.getId().toString());
 				            registryVO.setReferencingEntityCompletingName(SiteNode.class.getName());
				        
				            Collection contentVersions = content.getContentVersions();
				            Iterator contentVersionIterator = contentVersions.iterator();
				            while(contentVersionIterator.hasNext())
				            {
				                ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
				                getComponents(siteNodeVersion, contentVersion.getVersionValue(), db);
				                getComponentBindings(siteNodeVersion, contentVersion.getVersionValue(), db);
				            }
 				        }
 			            else if(name.equalsIgnoreCase("siteNodeId"))
 				        {
 			                SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(new Integer(value), db);
 			                
 			                registryVO.setEntityId(value);
 				            registryVO.setEntityName(SiteNode.class.getName());
 				            
 				            registryVO.setReferencingEntityId(siteNodeVersion.getId().toString());
 				            registryVO.setReferencingEntityName(SiteNodeVersion.class.getName());
 				            registryVO.setReferencingEntityCompletingId(oldSiteNode.getId().toString());
 				            registryVO.setReferencingEntityCompletingName(SiteNode.class.getName());
 				        }
 			            
 			            this.create(registryVO, db);
 	                }
 	                catch(Exception e)
 	                {
 	                    e.printStackTrace();
 	                }		        
 			    }
 		    }
 		}
 	}
 
 	/**
 	 * This method fetches all inline links from any text.
 	 */
 	
 	public void getInlineSiteNodes(ContentVersion contentVersion, String versionValue, Database db) throws ConstraintException, SystemException, Exception
 	{
 	    Pattern pattern = Pattern.compile("\\$templateLogic\\.getPageUrl\\(.*?\\)");
 	    Matcher matcher = pattern.matcher(versionValue);
 	    while ( matcher.find() ) 
 	    { 
 	        String match = matcher.group();
 	        CmsLogger.logInfo("Adding match to registry after some processing: " + match);
 	        Integer siteNodeId;
 	        
 	        int siteNodeStartIndex = match.indexOf("(");
 	        int siteNodeEndIndex = match.indexOf(",");
 	        if(siteNodeStartIndex > 0 && siteNodeEndIndex > 0 && siteNodeEndIndex > siteNodeStartIndex)
 	        {
 	            String siteNodeIdString = match.substring(siteNodeStartIndex + 1, siteNodeEndIndex); 
 	            try
 	            {
 		            siteNodeId = new Integer(siteNodeIdString);
 		            CmsLogger.logInfo("siteNodeId:" + siteNodeId);
 		            RegistryVO registryVO = new RegistryVO();
 		            registryVO.setEntityId(siteNodeId.toString());
 		            registryVO.setEntityName(SiteNode.class.getName());
 		            registryVO.setReferenceType(RegistryVO.INLINE_LINK);
 		            registryVO.setReferencingEntityId(contentVersion.getContentVersionId().toString());
 		            registryVO.setReferencingEntityName(ContentVersion.class.getName());
 		            registryVO.setReferencingEntityCompletingId(contentVersion.getOwningContent().getContentId().toString());
 		            registryVO.setReferencingEntityCompletingName(Content.class.getName());
 		            
 		            this.create(registryVO, db);
 	            }
 	            catch(Exception e)
 	            {
 	                CmsLogger.logWarning("Tried to register old inline asset with exception as result:" + e.getMessage(), e);
 	            }
 	        }
 	    }
 	}
 	
 	/**
 	 * This method fetches all inline links from any text.
 	 */
 	
 	public void getInlineContents(ContentVersion contentVersion, String versionValue, Database db) throws ConstraintException, SystemException, Exception
 	{
 	    Pattern pattern = Pattern.compile("\\$templateLogic\\.getInlineAssetUrl\\(.*?\\)");
 	    Matcher matcher = pattern.matcher(versionValue);
 	    while ( matcher.find() ) 
 	    { 
 	        String match = matcher.group();
 	        CmsLogger.logInfo("Adding match to registry after some processing: " + match);
 	        Integer contentId;
 	        
 	        int contentStartIndex = match.indexOf("(");
 	        int contentEndIndex = match.indexOf(",");
 	        if(contentStartIndex > 0 && contentEndIndex > 0 && contentEndIndex > contentStartIndex)
 	        {
 	            contentId = new Integer(match.substring(contentStartIndex + 1, contentEndIndex));
 	            CmsLogger.logInfo("contentId:" + contentId);
 	            
 	            RegistryVO registryVO = new RegistryVO();
 	            registryVO.setEntityId(contentId.toString());
 	            registryVO.setEntityName(Content.class.getName());
 	            registryVO.setReferenceType(RegistryVO.INLINE_ASSET);
 	            registryVO.setReferencingEntityId(contentVersion.getContentVersionId().toString());
 	            registryVO.setReferencingEntityName(ContentVersion.class.getName());
 	            registryVO.setReferencingEntityCompletingId(contentVersion.getOwningContent().getContentId().toString());
 	            registryVO.setReferencingEntityCompletingName(Content.class.getName());
 	            
 	            this.create(registryVO, db);
 	        }
 	    }
 	}
 	
 
 	/**
 	 * This method fetches all inline links from any text.
 	 */
 	
 	public void getRelationSiteNodes(ContentVersion contentVersion, String versionValue, Database db) throws ConstraintException, SystemException, Exception
 	{
 	    Pattern pattern = Pattern.compile("<qualifyer entity='SiteNode'>.*?</qualifyer>");
 	    Matcher matcher = pattern.matcher(versionValue);
 	    while ( matcher.find() ) 
 	    { 
 	        String match = matcher.group();
 	        CmsLogger.logInfo("Adding match to registry after some processing: " + match);
 	        Integer siteNodeId;
 	        
 	        int siteNodeStartIndex = match.indexOf("<id>");
 	        int siteNodeEndIndex = match.indexOf("</id>");
 	        while(siteNodeStartIndex > 0 && siteNodeEndIndex > 0 && siteNodeEndIndex > siteNodeStartIndex)
 	        {
 	            siteNodeId = new Integer(match.substring(siteNodeStartIndex + 4, siteNodeEndIndex));
 	            CmsLogger.logInfo("siteNodeId:" + siteNodeId);
 	            RegistryVO registryVO = new RegistryVO();
 	            registryVO.setEntityId(siteNodeId.toString());
 	            registryVO.setEntityName(SiteNode.class.getName());
 	            registryVO.setReferenceType(RegistryVO.INLINE_SITE_NODE_RELATION);
 	            registryVO.setReferencingEntityId(contentVersion.getContentVersionId().toString());
 	            registryVO.setReferencingEntityName(ContentVersion.class.getName());
 	            registryVO.setReferencingEntityCompletingId(contentVersion.getOwningContent().getContentId().toString());
 	            registryVO.setReferencingEntityCompletingName(Content.class.getName());
 	            
 	            this.create(registryVO, db);
 	            
 	            siteNodeStartIndex = match.indexOf("<id>", siteNodeEndIndex);
 		        siteNodeEndIndex = match.indexOf("</id>", siteNodeStartIndex);
 	        }
 	    }
 	}
 	
 	/**
 	 * This method fetches all inline links from any text.
 	 */
 	
 	public void getRelationContents(ContentVersion contentVersion, String versionValue, Database db) throws ConstraintException, SystemException, Exception
 	{
 	    Pattern pattern = Pattern.compile("<qualifyer entity='Content'>.*?</qualifyer>");
 	    Matcher matcher = pattern.matcher(versionValue);
 	    while ( matcher.find() ) 
 	    { 
 	        String match = matcher.group();
 	        CmsLogger.logInfo("Adding match to registry after some processing: " + match);
 	        Integer contentId;
 	        
 	        int contentStartIndex = match.indexOf("<id>");
 	        int contentEndIndex = match.indexOf("</id>");
 	        while(contentStartIndex > 0 && contentEndIndex > 0 && contentEndIndex > contentStartIndex)
 	        {
 	            contentId = new Integer(match.substring(contentStartIndex + 4, contentEndIndex));
 	            CmsLogger.logInfo("contentId:" + contentId);
 	            
 	            RegistryVO registryVO = new RegistryVO();
 	            registryVO.setEntityId(contentId.toString());
 	            registryVO.setEntityName(Content.class.getName());
 	            registryVO.setReferenceType(RegistryVO.INLINE_CONTENT_RELATION);
 	            registryVO.setReferencingEntityId(contentVersion.getContentVersionId().toString());
 	            registryVO.setReferencingEntityName(ContentVersion.class.getName());
 	            registryVO.setReferencingEntityCompletingId(contentVersion.getOwningContent().getContentId().toString());
 	            registryVO.setReferencingEntityCompletingName(Content.class.getName());
 	            
 	            this.create(registryVO, db);
 	            
 	            contentStartIndex = match.indexOf("<id>", contentEndIndex);
 	            contentEndIndex = match.indexOf("</id>", contentStartIndex);
 	        }
 	    }
 	}
 	                
 	
 	/**
 	 * This method fetches all components and adds entries to the registry.
 	 */
 	
 	public void getComponents(SiteNodeVersion siteNodeVersion, String versionValue, Database db) throws ConstraintException, SystemException, Exception
 	{
 	    Pattern pattern = Pattern.compile("contentId=\".*?\"");
 	    Matcher matcher = pattern.matcher(versionValue);
 	    while ( matcher.find() ) 
 	    { 
 	        String match = matcher.group();
 	        CmsLogger.logInfo("Adding match to registry after some processing: " + match);
 	        Integer contentId;
 	        
 	        int contentStartIndex = match.indexOf("\"");
 	        int contentEndIndex = match.lastIndexOf("\"");
 	        if(contentStartIndex > 0 && contentEndIndex > 0 && contentEndIndex > contentStartIndex)
 	        {
 	            contentId = new Integer(match.substring(contentStartIndex + 1, contentEndIndex));
 	            CmsLogger.logInfo("contentId:" + contentId);
 	            
 	            RegistryVO registryVO = new RegistryVO();
 	            registryVO.setEntityId(contentId.toString());
 	            registryVO.setEntityName(Content.class.getName());
 	            registryVO.setReferenceType(RegistryVO.PAGE_COMPONENT);
 	            registryVO.setReferencingEntityId(siteNodeVersion.getSiteNodeVersionId().toString());
 	            registryVO.setReferencingEntityName(SiteNodeVersion.class.getName());
 	            registryVO.setReferencingEntityCompletingId(siteNodeVersion.getOwningSiteNode().getSiteNodeId().toString());
 	            registryVO.setReferencingEntityCompletingName(SiteNode.class.getName());
 	            
 	            this.create(registryVO, db);
 	        }
 	    }
 	}
 
 	/**
 	 * This method fetches all components and adds entries to the registry.
 	 */
 
 	public void getComponentBindings(SiteNodeVersion siteNodeVersion, String versionValue, Database db) throws ConstraintException, SystemException, Exception
 	{
 	    Pattern pattern = Pattern.compile("<binding entity=\".*?\" entityId=\".*?\">");
 	    Matcher matcher = pattern.matcher(versionValue);
 	    while ( matcher.find() ) 
 	    { 
 	        String match = matcher.group();
 	        CmsLogger.logInfo("Adding match to registry after some processing: " + match);
 	        String entityName;
 	        String entityId;
 	        
 	        int entityNameStartIndex = match.indexOf("\"");
 	        int entityNameEndIndex = match.indexOf("\"", entityNameStartIndex + 1);
 	        CmsLogger.logInfo("entityNameStartIndex:" + entityNameStartIndex);
 	        CmsLogger.logInfo("entityNameEndIndex:" + entityNameEndIndex);
 	        if(entityNameStartIndex > 0 && entityNameEndIndex > 0 && entityNameEndIndex > entityNameStartIndex)
 	        {
 	            entityName = match.substring(entityNameStartIndex + 1, entityNameEndIndex);
 	            CmsLogger.logInfo("entityName:" + entityName);
 
 		        int entityIdStartIndex = match.indexOf("\"", entityNameEndIndex + 1);
 		        int entityIdEndIndex = match.indexOf("\"", entityIdStartIndex + 1);
 		        CmsLogger.logInfo("entityIdStartIndex:" + entityIdStartIndex);
 		        CmsLogger.logInfo("entityIdEndIndex:" + entityIdEndIndex);
 		        if(entityIdStartIndex > 0 && entityIdEndIndex > 0 && entityIdEndIndex > entityIdStartIndex)
 		        {
 		            entityId = match.substring(entityIdStartIndex + 1, entityIdEndIndex);
 		            CmsLogger.logInfo("entityId:" + entityId);
 
 		            RegistryVO registryVO = new RegistryVO();
 		            if(entityName.indexOf("Content") > -1)
 		                registryVO.setEntityName(Content.class.getName());
 		            else
 		                registryVO.setEntityName(SiteNode.class.getName());
 		                
 		            registryVO.setEntityId(entityId);
 		            registryVO.setReferenceType(RegistryVO.PAGE_COMPONENT_BINDING);
 		            registryVO.setReferencingEntityId(siteNodeVersion.getSiteNodeVersionId().toString());
 		            registryVO.setReferencingEntityName(SiteNodeVersion.class.getName());
 		            registryVO.setReferencingEntityCompletingId(siteNodeVersion.getOwningSiteNode().getSiteNodeId().toString());
 		            registryVO.setReferencingEntityCompletingName(SiteNode.class.getName());
 		            
 		            this.create(registryVO, db);
 		        }
 	        }
 	    }
 	}
 
 	/**
 	 * Implemented for BaseController
 	 */
 	public BaseEntityVO getNewVO()
 	{
 		return new CategoryVO();
 	}
 
     /**
      * This method gets all referencing content versions
      * 
      * @param contentId
      * @return
      */
 	/*
     public List getReferencingObjectsForContent(Integer contentId) throws SystemException
     {
         List referenceBeanList = new ArrayList();
         
 		Database db = CastorDatabaseService.getDatabase();
 		
 		try 
 		{
 			beginTransaction(db);
 			
 			Map entries = new HashMap();
 			
 	        List registryEntires = getMatchingRegistryVOList(Content.class.getName(), contentId.toString(), db);
 	        Iterator registryEntiresIterator = registryEntires.iterator();
 	        while(registryEntiresIterator.hasNext())
 	        {
 	            RegistryVO registryVO = (RegistryVO)registryEntiresIterator.next();
 	            CmsLogger.logInfo("registryVO:" + registryVO.getReferencingEntityId() + ":" +  registryVO.getReferencingEntityCompletingId());
 	            
 	            ReferenceBean referenceBean = new ReferenceBean();
 	            	            
 	            if(registryVO.getReferencingEntityName().indexOf("Content") > -1)
 	            {
 		            ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(new Integer(registryVO.getReferencingEntityId()), db);
 		    		CmsLogger.logInfo("contentVersion:" + contentVersion.getContentVersionId());
 		    		referenceBean.setName(contentVersion.getOwningContent().getName());
 		    		referenceBean.setReferencingObject(contentVersion.getValueObject());
 	            }
 	            else
 	            {
 	                SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(new Integer(registryVO.getReferencingEntityId()), db);
 		    		CmsLogger.logInfo("siteNodeVersion:" + siteNodeVersion.getSiteNodeVersionId());
 		    		referenceBean.setName(siteNodeVersion.getOwningSiteNode().getName());
 		    		referenceBean.setReferencingObject(siteNodeVersion.getValueObject());
 	            }
 	            
 	            String key = "" + registryVO.getReferencingEntityName() + "_" + registryVO.getReferencingEntityId();
 	            ReferenceBean existingReferenceBean = (ReferenceBean)entries.get(key);
 	            if(existingReferenceBean == null)
 	            {
 		            List registryVOList = new ArrayList();
 		            registryVOList.add(registryVO);
 		            referenceBean.setRegistryVOList(registryVOList);
 		            CmsLogger.logInfo("Adding referenceBean to entries with key:" + key);
 		            entries.put(key, referenceBean);
 		            referenceBeanList.add(referenceBean);
 	            }
 	            else
 	            {
 	                CmsLogger.logInfo("Found referenceBean in entries with key:" + key);
 	                existingReferenceBean.getRegistryVOList().add(registryVO);
 	            }
 	        }
 
 	        commitTransaction(db);
 		}
 		catch ( Exception e)		
 		{
 		    CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + e);
 		    rollbackTransaction(db);
 		}
 
         return referenceBeanList;
     }
     */
     
 	public List getReferencingObjectsForContent(Integer contentId) throws SystemException
     {
         List referenceBeanList = new ArrayList();
         
 		Database db = CastorDatabaseService.getDatabase();
 		
 		try 
 		{
 			beginTransaction(db);
 			
 			Map entries = new HashMap();
 			
 	        List registryEntires = getMatchingRegistryVOList(Content.class.getName(), contentId.toString(), db);
 	        CmsLogger.logInfo("registryEntires:" + registryEntires.size());
 	        Iterator registryEntiresIterator = registryEntires.iterator();
 	        while(registryEntiresIterator.hasNext())
 	        {
 	            RegistryVO registryVO = (RegistryVO)registryEntiresIterator.next();
 	            CmsLogger.logInfo("registryVO:" + registryVO.getReferencingEntityId() + ":" +  registryVO.getReferencingEntityCompletingId());
 	            
 	            String key = "" + registryVO.getReferencingEntityCompletingName() + "_" + registryVO.getReferencingEntityCompletingId();
 	            //String key = "" + registryVO.getReferencingEntityName() + "_" + registryVO.getReferencingEntityId();
 	            ReferenceBean existingReferenceBean = (ReferenceBean)entries.get(key);
 	            if(existingReferenceBean == null)
 	            {
 	                existingReferenceBean = new ReferenceBean();
 		            CmsLogger.logInfo("Adding referenceBean to entries with key:" + key);
 		            entries.put(key, existingReferenceBean);
 		            referenceBeanList.add(existingReferenceBean);
 		        }
 
 	            ReferenceVersionBean referenceVersionBean = new ReferenceVersionBean();
 	            
 	            if(registryVO.getReferencingEntityName().indexOf("Content") > -1)
 	            {
                     ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(new Integer(registryVO.getReferencingEntityId()), db);
 		    		CmsLogger.logInfo("contentVersion:" + contentVersion.getContentVersionId());
 		    		existingReferenceBean.setName(contentVersion.getOwningContent().getName());
 		    		existingReferenceBean.setReferencingCompletingObject(contentVersion.getOwningContent().getValueObject());
 		    		
 		    		referenceVersionBean.setReferencingObject(contentVersion.getValueObject());
 		    		referenceVersionBean.getRegistryVOList().add(registryVO);
 	            }
 	            else
 	            {
 	                SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(new Integer(registryVO.getReferencingEntityId()), db);
 		    		CmsLogger.logInfo("siteNodeVersion:" + siteNodeVersion.getSiteNodeVersionId());
 		    		CmsLogger.logInfo("siteNode:" + siteNodeVersion.getOwningSiteNode().getId());
 		    		existingReferenceBean.setName(siteNodeVersion.getOwningSiteNode().getName());
 		    		existingReferenceBean.setReferencingCompletingObject(siteNodeVersion.getOwningSiteNode().getValueObject());
 
 		    		referenceVersionBean.setReferencingObject(siteNodeVersion.getValueObject());
 		    		referenceVersionBean.getRegistryVOList().add(registryVO);
 	            }
 	               
 	            boolean exists = false;
 	            ReferenceVersionBean existingReferenceVersionBean = null;
 	            Iterator versionsIterator = existingReferenceBean.getVersions().iterator();
 	            while(versionsIterator.hasNext())
 	            {
 	                existingReferenceVersionBean = (ReferenceVersionBean)versionsIterator.next();
 	                if(existingReferenceVersionBean.getReferencingObject().equals(referenceVersionBean.getReferencingObject()))
 	                {
 	                    exists = true;
 	                    break;
 	                }
 	            }
 	            
 	            if(!exists)
 	                existingReferenceBean.getVersions().add(referenceVersionBean);
 	            else
 	                existingReferenceVersionBean.getRegistryVOList().add(registryVO);
 	        }
 	        
 	        commitTransaction(db);
 		}
 		catch (Exception e)		
 		{
 		    e.printStackTrace();
 		    CmsLogger.logWarning("One of the references was not found which is bad but not critical:" + e.getMessage(), e);
 		    rollbackTransaction(db);
 			//throw new SystemException("An error occurred when we tried to fetch a list of roles in the repository. Reason:" + e.getMessage(), e);			
 		}
 		
 		CmsLogger.logInfo("referenceBeanList:" + referenceBeanList.size());
 		
         return referenceBeanList;
     }
 
     
     
     /**
      * This method gets all referencing sitenode versions
      * 
      * @param siteNodeId
      * @return
      */
 	/*
     public List getReferencingObjectsForSiteNode(Integer siteNodeId) throws SystemException, Exception
     {
         List referenceBeanList = new ArrayList();
         
         Database db = CastorDatabaseService.getDatabase();
 		
 		try 
 		{
 			beginTransaction(db);
 			
 			Map entries = new HashMap();
 			
 			List registryEntires = getMatchingRegistryVOList(SiteNode.class.getName(), siteNodeId.toString(), db);
 	        Iterator registryEntiresIterator = registryEntires.iterator();
 	        while(registryEntiresIterator.hasNext())
 	        {
 	            RegistryVO registryVO = (RegistryVO)registryEntiresIterator.next();
 	            CmsLogger.logInfo("registryVO:" + registryVO.getReferencingEntityId() + ":" +  registryVO.getReferencingEntityCompletingId());
 	            
 	            ReferenceBean referenceBean = new ReferenceBean();
 	           
 	            if(registryVO.getReferencingEntityName().indexOf("Content") > -1)
 	            {
                     ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(new Integer(registryVO.getReferencingEntityId()), db);
 		    		CmsLogger.logInfo("contentVersion:" + contentVersion.getContentVersionId());
 		    		referenceBean.setName(contentVersion.getOwningContent().getName());
 		    		referenceBean.setReferencingObject(contentVersion.getValueObject());
 		    	}
 	            else
 	            {
 	                SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(new Integer(registryVO.getReferencingEntityId()), db);
 		    		CmsLogger.logInfo("siteNodeVersion:" + siteNodeVersion.getSiteNodeVersionId());
 		    		referenceBean.setName(siteNodeVersion.getOwningSiteNode().getName());
 		    		referenceBean.setReferencingObject(siteNodeVersion.getValueObject());
 	            }
 	            
 	            String key = "" + registryVO.getReferencingEntityName() + "_" + registryVO.getReferencingEntityId();
 	            //String key = "" + registryVO.getReferencingEntityCompletingName() + "_" + registryVO.getReferencingEntityCompletingId();
 	            ReferenceBean existingReferenceBean = (ReferenceBean)entries.get(key);
 	            if(existingReferenceBean == null)
 	            {
 		            List registryVOList = new ArrayList();
 		            registryVOList.add(registryVO);
 		            referenceBean.setRegistryVOList(registryVOList);
 		            CmsLogger.logInfo("Adding referenceBean to entries with key:" + key);
 		            entries.put(key, referenceBean);
 		            referenceBeanList.add(referenceBean);
 	            }
 	            else
 	            {
 	                CmsLogger.logInfo("Found referenceBean in entries with key:" + key);
 	                existingReferenceBean.getRegistryVOList().add(registryVO);
 	            }
 	        }
 	        
 	        commitTransaction(db);
 		}
 		catch (Exception e)		
 		{
 		    CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + e);
 		    rollbackTransaction(db);
 			//throw new SystemException("An error occurred when we tried to fetch a list of roles in the repository. Reason:" + e.getMessage(), e);			
 		}
 		
         return referenceBeanList;
     }
     */
     
     public List getReferencingObjectsForSiteNode(Integer siteNodeId) throws SystemException, Exception
     {
         List referenceBeanList = new ArrayList();
         
         Database db = CastorDatabaseService.getDatabase();
 		
 		try 
 		{
 			beginTransaction(db);
 			
 			Map entries = new HashMap();
 			
 			List registryEntires = getMatchingRegistryVOList(SiteNode.class.getName(), siteNodeId.toString(), db);
 	        Iterator registryEntiresIterator = registryEntires.iterator();
 	        while(registryEntiresIterator.hasNext())
 	        {
 	            RegistryVO registryVO = (RegistryVO)registryEntiresIterator.next();
 	            CmsLogger.logInfo("registryVO:" + registryVO.getReferencingEntityId() + ":" +  registryVO.getReferencingEntityCompletingId());
 	     
 	            String key = "" + registryVO.getReferencingEntityCompletingName() + "_" + registryVO.getReferencingEntityCompletingId();
 	            //String key = "" + registryVO.getReferencingEntityName() + "_" + registryVO.getReferencingEntityId();
 	            ReferenceBean existingReferenceBean = (ReferenceBean)entries.get(key);
 	            if(existingReferenceBean == null)
 	            {
 	                existingReferenceBean = new ReferenceBean();
 		            CmsLogger.logInfo("Adding referenceBean to entries with key:" + key);
 		            entries.put(key, existingReferenceBean);
 		            referenceBeanList.add(existingReferenceBean);
 		        }
 
 	            ReferenceVersionBean referenceVersionBean = new ReferenceVersionBean();
 	            
 	            if(registryVO.getReferencingEntityName().indexOf("Content") > -1)
 	            {
                     ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(new Integer(registryVO.getReferencingEntityId()), db);
 		    		CmsLogger.logInfo("contentVersion:" + contentVersion.getContentVersionId());
 		    		existingReferenceBean.setName(contentVersion.getOwningContent().getName());
 		    		existingReferenceBean.setReferencingCompletingObject(contentVersion.getOwningContent().getValueObject());
 		    		
 		    		referenceVersionBean.setReferencingObject(contentVersion.getValueObject());
 		    		referenceVersionBean.getRegistryVOList().add(registryVO);
 	            }
 	            else
 	            {
 	                SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(new Integer(registryVO.getReferencingEntityId()), db);
 		    		CmsLogger.logInfo("siteNodeVersion:" + siteNodeVersion.getSiteNodeVersionId());
 		    		CmsLogger.logInfo("siteNode:" + siteNodeVersion.getOwningSiteNode().getId());
 		    		existingReferenceBean.setName(siteNodeVersion.getOwningSiteNode().getName());
 		    		existingReferenceBean.setReferencingCompletingObject(siteNodeVersion.getOwningSiteNode().getValueObject());
 
 		    		referenceVersionBean.setReferencingObject(siteNodeVersion.getValueObject());
 		    		referenceVersionBean.getRegistryVOList().add(registryVO);
 	            }
 	               
 	            boolean exists = false;
 	            ReferenceVersionBean existingReferenceVersionBean = null;
 	            Iterator versionsIterator = existingReferenceBean.getVersions().iterator();
 	            while(versionsIterator.hasNext())
 	            {
 	                existingReferenceVersionBean = (ReferenceVersionBean)versionsIterator.next();
 	                if(existingReferenceVersionBean.getReferencingObject().equals(referenceVersionBean.getReferencingObject()))
 	                {
 	                    exists = true;
 	                    break;
 	                }
 	            }
 	            
 	            if(!exists)
 	                existingReferenceBean.getVersions().add(referenceVersionBean);
 	            else
 	                existingReferenceVersionBean.getRegistryVOList().add(registryVO);
 	        }
 	        
 	        commitTransaction(db);
 		}
 		catch (Exception e)		
 		{
 		    e.printStackTrace();
 		    CmsLogger.logWarning("One of the references was not found which is bad but not critical:" + e.getMessage(), e);
 		    rollbackTransaction(db);
 			//throw new SystemException("An error occurred when we tried to fetch a list of roles in the repository. Reason:" + e.getMessage(), e);			
 		}
 		
         return referenceBeanList;
     }
 
 	/**
 	 * Gets matching references
 	 */
 	
 	public List getMatchingRegistryVOList(String entityName, String entityId, Database db) throws SystemException, Exception
 	{
 	    List matchingRegistryVOList = new ArrayList();
 	    
 		OQLQuery oql = db.getOQLQuery("SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RegistryImpl r WHERE r.entityName = $1 AND r.entityId = $2 ORDER BY r.registryId");
 		oql.bind(entityName);
 		oql.bind(entityId);
 		
 		QueryResults results = oql.execute(Database.ReadOnly);
 		
 		while (results.hasMore()) 
         {
             Registry registry = (Registry)results.next();
             RegistryVO registryVO = registry.getValueObject();
             
             matchingRegistryVOList.add(registryVO);
         }            
 		
 		return matchingRegistryVOList;		
 	}
 	
 	
 	/**
 	 * Gets matching references
 	 */
 	
 	public List getMatchingRegistryVOListForReferencingEntity(String referencingEntityName, String referencingEntityId, Database db) throws SystemException, Exception
 	{
 	    List matchingRegistryVOList = new ArrayList();
 
 	    CmsLogger.logInfo("referencingEntityName:" + referencingEntityName);
 	    CmsLogger.logInfo("referencingEntityId:" + referencingEntityId);
 		
 	    OQLQuery oql = db.getOQLQuery("SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RegistryImpl r WHERE r.referencingEntityName = $1 AND r.referencingEntityId = $2 ORDER BY r.registryId");
 		oql.bind(referencingEntityName);
 		oql.bind(referencingEntityId);
 		
 		QueryResults results = oql.execute(Database.ReadOnly);
 		
 		while (results.hasMore()) 
         {
             Registry registry = (Registry)results.next();
             RegistryVO registryVO = registry.getValueObject();
     	    CmsLogger.logInfo("found match:" + registryVO.getEntityName() + ":" + registryVO.getEntityId());
             
             matchingRegistryVOList.add(registryVO);
         }       
 		
 		return matchingRegistryVOList;		
 	}
 	
 	/**
 	 * Gets matching references
 	 */
 	
 	public List clearRegistryVOList(String referencingEntityName, String referencingEntityId, Database db) throws SystemException, Exception
 	{
 	    List matchingRegistryVOList = new ArrayList();
 	    
 		OQLQuery oql = db.getOQLQuery("SELECT r FROM org.infoglue.cms.entities.management.impl.simple.RegistryImpl r WHERE r.referencingEntityName = $1 AND r.referencingEntityId = $2 ORDER BY r.registryId");
 		oql.bind(referencingEntityName);
 		oql.bind(referencingEntityId);
 		
 		QueryResults results = oql.execute();
 		
 		while (results.hasMore()) 
         {
             Registry registry = (Registry)results.next();
             db.remove(registry);
         }
 		
 		return matchingRegistryVOList;		
 	}
 	
 	
 	/**
 	 * Clears all references to a entity
 	 */
 	
 	public void clearRegistryForReferencedEntity(String entityName, String entityId) throws SystemException, Exception
 	{
 	    Database db = CastorDatabaseService.getDatabase();
 		
 		try 
 		{
 			beginTransaction(db);
 			
 			OQLQuery oql = db.getOQLQuery("DELETE FROM org.infoglue.cms.entities.management.impl.simple.RegistryImpl r WHERE r.entityName = $1 AND r.entityId = $2");
 			oql.bind(entityName);
 			oql.bind(entityId);
 			
 			QueryResults results = oql.execute();		
 		    
 	        commitTransaction(db);
 		}
 		catch (Exception e)		
 		{
 		    CmsLogger.logWarning("An error occurred so we should not complete the transaction:" + e);
 		    rollbackTransaction(db);
 		}
 	}
 	
 	/**
 	 * Gets siteNodeVersions which uses the metainfo
 	 */
 	
 	public List getSiteNodeVersionsWhichUsesContentVersionAsMetaInfo(ContentVersion contentVersion, Database db) throws SystemException, Exception
 	{
 	    List siteNodeVersions = new ArrayList();
 	    
 	    OQLQuery oql = db.getOQLQuery("SELECT snv FROM org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl snv WHERE snv.serviceBindings.availableServiceBinding.name = $1 AND snv.serviceBindings.bindingQualifyers.name = $2 AND snv.serviceBindings.bindingQualifyers.value = $3");
 	    oql.bind("Meta information");
 		oql.bind("contentId");
 		oql.bind(contentVersion.getOwningContent().getId());
 		
 		QueryResults results = oql.execute();
 		
 		while (results.hasMore()) 
         {
 		    SiteNodeVersion siteNodeVersion = (SiteNodeVersion)results.next();
 		    siteNodeVersions.add(siteNodeVersion);
 		    //CmsLogger.logInfo("siteNodeVersion:" + siteNodeVersion.getId());
         }
     	
 		return siteNodeVersions;		
 	}
 
 }
