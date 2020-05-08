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
 
 package org.infoglue.cms.applications.managementtool.actions;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.TransactionNotInProgressException;
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.xml.Unmarshaller;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.AvailableServiceBindingController;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ServiceDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
 import org.infoglue.cms.entities.content.Content;
 import org.infoglue.cms.entities.content.ContentCategory;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.content.DigitalAsset;
 import org.infoglue.cms.entities.content.impl.simple.ContentCategoryImpl;
 import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
 import org.infoglue.cms.entities.management.AvailableServiceBinding;
 import org.infoglue.cms.entities.management.Category;
 import org.infoglue.cms.entities.management.CategoryVO;
 import org.infoglue.cms.entities.management.ContentTypeDefinition;
 import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
 import org.infoglue.cms.entities.management.Language;
 import org.infoglue.cms.entities.management.Repository;
 import org.infoglue.cms.entities.management.RepositoryLanguage;
 import org.infoglue.cms.entities.management.ServiceDefinition;
 import org.infoglue.cms.entities.management.SiteNodeTypeDefinition;
 import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
 import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
 import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.InfoGlueExportImpl;
 import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
 import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
 import org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
 import org.infoglue.cms.entities.structure.Qualifyer;
 import org.infoglue.cms.entities.structure.ServiceBinding;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.SiteNodeVersion;
 import org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.FileUploadHelper;
 
 import webwork.action.ActionContext;
 
 /**
  * This class handles Exporting of a repository to an XML-file.
  * 
  * @author mattias
  */
 
 public class ImportRepositoryAction extends InfoGlueAbstractAction
 {
     public final static Logger logger = Logger.getLogger(ImportRepositoryAction.class.getName());
 
 	private String onlyLatestVersions = "true";
 	
 	/**
 	 * This shows the dialog before export.
 	 * @return
 	 * @throws Exception
 	 */	
 
 	public String doInput() throws Exception
 	{
 		return "input";
 	}
 	
 	/**
 	 * This handles the actual importing.
 	 */
 	
 	protected String doExecute() throws SystemException 
 	{
 		Database db = CastorDatabaseService.getDatabase();
 		
 		try 
 		{
 			//now restore the value and list what we get
 			File file = FileUploadHelper.getUploadedFile(ActionContext.getContext().getMultiPartRequest());
 			//File file = new File("C:\\Program Files\\Apache Software Foundation\\Tomcat 5.5\\webapps\\infoglueCMS\\digitalAssets\\Export__genus.gu.se_2_2007-02-15.xml");
 			if(file == null || !file.exists())
 				throw new SystemException("The file upload must have gone bad as no file reached the import utility.");
 			
 			String encoding = "UTF-8";
 			int version = 1;
 			
 			//Looking up what kind of dialect this is.
 	        
 			FileInputStream fisTemp = new FileInputStream(file);
             InputStreamReader readerTemp = new InputStreamReader(fisTemp, encoding);
             BufferedReader bufferedReaderTemp = new BufferedReader(readerTemp);
             String line = bufferedReaderTemp.readLine();
             int index = 0;
             while(line != null && index < 50)
             {
             	logger.info("line:" + line + '\n');
             	if(line.indexOf("contentTypeDefinitionId") > -1)
             	{
             		logger.info("This was a new export...");
             		version = 2;
             		break;
             	}
             	line = bufferedReaderTemp.readLine();
             	index++;
             }
             
             bufferedReaderTemp.close();
             readerTemp.close();
             fisTemp.close();
 
             Mapping map = new Mapping();
 			if(version == 1)
 			{
 	            logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site.xml").toString());
 				map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site.xml").toString());
 			}
 			else if(version == 2)
 			{
 			    logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());
 				map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());	
 			}
 
 			// All ODMG database access requires a transaction
 			db.begin();
 			
 
 			//String encoding = "ISO-8859-1";
 	        FileInputStream fis = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(fis, encoding);
 			//Reader reader = new FileReader(file);
 
 			Unmarshaller unmarshaller = new Unmarshaller(map);
 			unmarshaller.setWhitespacePreserve(true);
 			InfoGlueExportImpl infoGlueExportImplRead = (InfoGlueExportImpl)unmarshaller.unmarshal(reader);
 			Collection contentTypeDefinitions = infoGlueExportImplRead.getContentTypeDefinitions();
 			logger.info("Found " + contentTypeDefinitions.size() + " content type definitions");
 			Collection categories = infoGlueExportImplRead.getCategories();
 			logger.info("Found " + categories.size() + " categories");
 
 			Map categoryIdMap = new HashMap();
 			Map contentTypeIdMap = new HashMap();
 
 			importCategories(categories, null, categoryIdMap, db);
 			
 			updateContentTypeDefinitions(contentTypeDefinitions, categoryIdMap);
 			
 			List readSiteNodes = infoGlueExportImplRead.getRootSiteNode();
 			//SiteNode readSiteNode = infoGlueExportImplRead.getRootSiteNode();
 			//logger.info(readSiteNode.getName());
 			List readContents = infoGlueExportImplRead.getRootContent();
 			//Content readContent = infoGlueExportImplRead.getRootContent();
 			//logger.info(readContent.getName());
 
 			Map contentIdMap = new HashMap();
 			Map siteNodeIdMap = new HashMap();
 			
 			List allContents = new ArrayList();
 			List allSiteNodes = new ArrayList();
			
			Map<String, AvailableServiceBinding> readAvailableServiceBindings = new HashMap<String, AvailableServiceBinding>();
 
 			Iterator readSiteNodesIterator = readSiteNodes.iterator();
 			while(readSiteNodesIterator.hasNext())
 			{
 				SiteNode readSiteNode = (SiteNode)readSiteNodesIterator.next();
 
 				Repository repositoryRead = readSiteNode.getRepository();
 				logger.info(repositoryRead.getName());
 
 				Content readContent = null;
 				if(readContents != null && readContents.size() > 1)
 				{
 					Iterator readContentsIterator = readContents.iterator();
 					while(readContentsIterator.hasNext())
 					{
 						Content readContentCandidate = (Content)readContentsIterator.next();
 						if(readContentCandidate.getRepositoryId().equals(repositoryRead.getId()))
 						{
 							readContent = readContentCandidate;
 							break;
 						}
 					}
 				}
 				else
 				{
 					readContent = (Content)readContents.get(0);
 				}
 				
 				readContent.setRepository((RepositoryImpl)repositoryRead);
 
 				db.create(repositoryRead);
 
 				Collection repositoryLanguages = repositoryRead.getRepositoryLanguages();
 				Iterator repositoryLanguagesIterator = repositoryLanguages.iterator();
 				while(repositoryLanguagesIterator.hasNext())
 				{
 					RepositoryLanguage repositoryLanguage = (RepositoryLanguage)repositoryLanguagesIterator.next();
 					Language originalLanguage = repositoryLanguage.getLanguage();
 					
 					Language language = LanguageController.getController().getLanguageWithCode(originalLanguage.getLanguageCode(), db);
 					if(language == null)
 					{
 					    db.create(originalLanguage);
 					    language = originalLanguage;
 					}
 					
 					repositoryLanguage.setLanguage(language);
 					repositoryLanguage.setRepository(repositoryRead);
 
 					db.create(repositoryLanguage);
 					
 					logger.info("language:" + language);
 					logger.info("language.getRepositoryLanguages():" + language.getRepositoryLanguages());
 					language.getRepositoryLanguages().add(repositoryLanguage);
 				}
 				
 				readSiteNode.setRepository((RepositoryImpl)repositoryRead);
 				
 				createContents(readContent, contentIdMap, contentTypeIdMap, allContents, Collections.unmodifiableCollection(contentTypeDefinitions), categoryIdMap, version, db);
				createStructure(readSiteNode, contentIdMap, siteNodeIdMap, readAvailableServiceBindings, allSiteNodes, db);
 			}
 						
 			List allContentIds = new ArrayList();
 			Iterator allContentsIterator = allContents.iterator();
 			while(allContentsIterator.hasNext())
 			{
 				Content content = (Content)allContentsIterator.next();
 				allContentIds.add(content.getContentId());
 			}
 
 			db.commit();
 			db.close();
 						
 			Iterator allContentIdsIterator = allContentIds.iterator();
 			while(allContentIdsIterator.hasNext())
 			{
 				Integer contentId = (Integer)allContentIdsIterator.next();
 				try
 				{
 					db = CastorDatabaseService.getDatabase();
 					db.begin();
 	
 					Content content = ContentController.getContentController().getContentWithId(contentId, db);
 					updateContentVersions(content, contentIdMap, siteNodeIdMap);
 	
 					db.commit();
 				}
 				catch(Exception e)
 				{
 					try
 					{
 						db.rollback();
 					}
 					catch(Exception e2) { e2.printStackTrace(); }
 	                logger.error("An error occurred when updating content version for content: " + e.getMessage(), e);					
 				}
 				finally
 				{
 					db.close();					
 				}
 			}
 
 			//updateContentVersions(allContents, contentIdMap, siteNodeIdMap);
 			
 			reader.close();
 			
 			//db.commit();
 			//db.close();
 
 		} 
 		catch ( Exception e) 
 		{
 			try
             {
                 db.rollback();
                 db.close();
             } 
 			catch (Exception e1)
             {
                 logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
     			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
             }
 			
 			logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
 			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
 		}
 		
 		return "success";
 	}
 
 	
 	private void updateContentTypeDefinitions(Collection contentTypeDefinitions, Map categoryIdMap) 
 	{
 		Iterator contentTypeDefinitionsIterator = contentTypeDefinitions.iterator();
 		while(contentTypeDefinitionsIterator.hasNext())
 		{
 			ContentTypeDefinition contentTypeDefinition = (ContentTypeDefinition)contentTypeDefinitionsIterator.next();
 			String schema = contentTypeDefinition.getSchemaValue();
 			Iterator categoryIdMapIterator = categoryIdMap.keySet().iterator();
 			while(categoryIdMapIterator.hasNext())
 			{
 				Integer oldId = (Integer)categoryIdMapIterator.next();
 				Integer newId = (Integer)categoryIdMap.get(oldId);
 				schema = schema.replaceAll("<categoryId>" + oldId + "</categoryId>", "<categoryId>new_" + newId + "</categoryId>");
 			}
 			schema = schema.replaceAll("<categoryId>new_", "<categoryId>");
 			contentTypeDefinition.setSchemaValue(schema);
 		}
 	}
 
 	private void importCategories(Collection categories, Category parentCategory, Map categoryIdMap, Database db) throws SystemException
 	{
 		logger.info("We want to create a list of categories if not existing under the parent category " + parentCategory);
 		Iterator categoryIterator = categories.iterator();
 		while(categoryIterator.hasNext())
 		{
 			CategoryVO categoryVO = (CategoryVO)categoryIterator.next();
 			Category newParentCategory = null;
 			
 			List existingCategories = null;
 			if(parentCategory != null)
 				existingCategories = CategoryController.getController().findByParent(parentCategory.getCategoryId(), db);
 			else
 				existingCategories = CategoryController.getController().findRootCategories(db);
 				
 			Iterator existingCategoriesIterator = existingCategories.iterator();
 			while(existingCategoriesIterator.hasNext())
 			{
 				Category existingCategory = (Category)existingCategoriesIterator.next();
 				logger.info("existingCategory:" + existingCategory.getName());
 				if(existingCategory.getName().equals(categoryVO.getName()))
 				{
 					logger.info("Existed... setting " + existingCategory.getName() + " to new parent category.");
 					newParentCategory = existingCategory;
 					break;
 				}
 			}
 
 			if(newParentCategory == null)
 			{
 				logger.info("No existing category - we create it.");
 				Integer oldId = categoryVO.getId();
 				categoryVO.setCategoryId(null);
 				if(parentCategory != null)	
 					categoryVO.setParentId(parentCategory.getCategoryId());
 				else
 					categoryVO.setParentId(null);
 					
 				Category newCategory = CategoryController.getController().save(categoryVO, db);
 				categoryIdMap.put(oldId, newCategory.getCategoryId());
 				newParentCategory = newCategory;
 			}
 			else
 			{
 				categoryIdMap.put(categoryVO.getId(), newParentCategory.getCategoryId());
 			}
 				
 			importCategories(categoryVO.getChildren(), newParentCategory, categoryIdMap, db);
 		}
 	}
 	
 	/**
 	 * This method copies a sitenode and all it relations.
 	 * 
 	 * @param siteNode
 	 * @param db
 	 * @throws Exception
 	 */
	private void createStructure(SiteNode siteNode, Map contentIdMap, Map siteNodeIdMap, Map readAvailableServiceBindings, List allSiteNodes, Database db) throws Exception
 	{
 		logger.info("siteNode:" + siteNode.getName());
 
 		Integer originalSiteNodeId = siteNode.getSiteNodeId();
 
 		logger.info("originalSiteNodeId:" + originalSiteNodeId);
 
 		SiteNodeTypeDefinition originalSiteNodeTypeDefinition = siteNode.getSiteNodeTypeDefinition();
 		SiteNodeTypeDefinition siteNodeTypeDefinition = null;
 		if(originalSiteNodeTypeDefinition != null)
 		{
 			logger.info("originalSiteNodeTypeDefinition:" + originalSiteNodeTypeDefinition);
 			siteNodeTypeDefinition = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionWithName(siteNode.getSiteNodeTypeDefinition().getName(), db, false);
 			logger.info("siteNodeTypeDefinition:" + siteNodeTypeDefinition);
 			if(siteNodeTypeDefinition == null)
 			{
 			    db.create(originalSiteNodeTypeDefinition);
 			    siteNodeTypeDefinition = originalSiteNodeTypeDefinition;
 			}
 			
 			siteNode.setSiteNodeTypeDefinition((SiteNodeTypeDefinitionImpl)siteNodeTypeDefinition);
 		}
 		
 		String mappedMetaInfoContentId = "-1";
 		if(siteNode.getMetaInfoContentId() != null)
 		{
 			if(contentIdMap.containsKey(siteNode.getMetaInfoContentId().toString()))
 				mappedMetaInfoContentId = (String)contentIdMap.get(siteNode.getMetaInfoContentId().toString());
 			//System.out.println("siteNode meta info content id was:" + siteNode.getMetaInfoContentId() + " and is now " + mappedMetaInfoContentId);
 		}
 		siteNode.setMetaInfoContentId(new Integer(mappedMetaInfoContentId));
 		
 		db.create(siteNode);
 		
 		allSiteNodes.add(siteNode);
 		    
 		Integer newSiteNodeId = siteNode.getSiteNodeId();
 		logger.info(originalSiteNodeId + ":" + newSiteNodeId);
 		siteNodeIdMap.put(originalSiteNodeId.toString(), newSiteNodeId.toString());
 		
 		Collection childSiteNodes = siteNode.getChildSiteNodes();
 		if(childSiteNodes != null)
 		{
 			Iterator childSiteNodesIterator = childSiteNodes.iterator();
 			while(childSiteNodesIterator.hasNext())
 			{
 				SiteNode childSiteNode = (SiteNode)childSiteNodesIterator.next();
 				childSiteNode.setRepository(siteNode.getRepository());
 				childSiteNode.setParentSiteNode((SiteNodeImpl)siteNode);
				createStructure(childSiteNode, contentIdMap, siteNodeIdMap, readAvailableServiceBindings, allSiteNodes, db);
 			}
 		}
 
 		Collection siteNodeVersions = siteNode.getSiteNodeVersions();
 		
 		if(onlyLatestVersions.equalsIgnoreCase("true"))
 		{
 		    logger.info("org siteNodeVersions:" + siteNodeVersions.size());
 			List selectedSiteNodeVersions = new ArrayList();
 			Iterator realSiteNodeVersionsIterator = siteNodeVersions.iterator();
 			while(realSiteNodeVersionsIterator.hasNext())
 			{
 				SiteNodeVersion siteNodeVersion = (SiteNodeVersion)realSiteNodeVersionsIterator.next();			
 				Iterator selectedSiteNodeVersionsIterator = selectedSiteNodeVersions.iterator();
 				boolean addVersion = true;
 				while(selectedSiteNodeVersionsIterator.hasNext())
 				{
 					SiteNodeVersion currentSiteNodeVersion = (SiteNodeVersion)selectedSiteNodeVersionsIterator.next();
 					if(siteNodeVersion.getIsActive().booleanValue() && siteNodeVersion.getSiteNodeVersionId().intValue() > currentSiteNodeVersion.getSiteNodeVersionId().intValue())
 					{
 						logger.info("A later version was found... removing this one..");
 						selectedSiteNodeVersionsIterator.remove();
 						addVersion = true;
 					}						
 				}
 	
 				if(addVersion)
 					selectedSiteNodeVersions.add(siteNodeVersion);
 			}	
 			
 			siteNodeVersions = selectedSiteNodeVersions;
 		}
 
 		Iterator siteNodeVersionsIterator = siteNodeVersions.iterator();
 		while(siteNodeVersionsIterator.hasNext())
 		{
 			SiteNodeVersion siteNodeVersion = (SiteNodeVersion)siteNodeVersionsIterator.next();
 			
 			Collection serviceBindings = siteNodeVersion.getServiceBindings();
 
 			siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
 			
 			db.create(siteNodeVersion);
						
 			Iterator serviceBindingsIterator = serviceBindings.iterator();
 			while(serviceBindingsIterator.hasNext())
 			{
 				ServiceBinding serviceBinding = (ServiceBinding)serviceBindingsIterator.next();
 				logger.info("serviceBinding:" + serviceBinding.getName());
 				
 				ServiceDefinition originalServiceDefinition = serviceBinding.getServiceDefinition();
 				String serviceDefinitionName = originalServiceDefinition.getName();
 				ServiceDefinition serviceDefinition = ServiceDefinitionController.getController().getServiceDefinitionWithName(serviceDefinitionName, db, false);
 				if(serviceDefinition == null)
 				{
 				    db.create(originalServiceDefinition);
 				    serviceDefinition = originalServiceDefinition;
 				    //availableServiceBinding.getServiceDefinitions().add(serviceDefinition);
 				}
 				
 				serviceBinding.setServiceDefinition((ServiceDefinitionImpl)serviceDefinition);
 
 				AvailableServiceBinding originalAvailableServiceBinding = serviceBinding.getAvailableServiceBinding();
 				String availableServiceBindingName = originalAvailableServiceBinding.getName();
 				logger.info("availableServiceBindingName:" + availableServiceBindingName);
				logger.info("readAvailableServiceBindings:" + readAvailableServiceBindings.size() + ":" + readAvailableServiceBindings.containsKey(availableServiceBindingName));
 				AvailableServiceBinding availableServiceBinding = (AvailableServiceBinding)readAvailableServiceBindings.get(availableServiceBindingName);
				logger.info("availableServiceBinding:" + availableServiceBinding);
 				if(availableServiceBinding == null)
 				{
 					availableServiceBinding = AvailableServiceBindingController.getController().getAvailableServiceBindingWithName(availableServiceBindingName, db, false);
					logger.info("Read availableServiceBinding from database:" + availableServiceBindingName + "=" + availableServiceBinding);
 					readAvailableServiceBindings.put(availableServiceBindingName, availableServiceBinding);
					logger.info("readAvailableServiceBindings:" + readAvailableServiceBindings.size() + ":" + readAvailableServiceBindings.containsKey(availableServiceBindingName));
 				}
 				
 				if(availableServiceBinding == null)
 				{
 				    logger.info("There was no availableServiceBinding registered under:" + availableServiceBindingName);
 				    logger.info("originalAvailableServiceBinding:" + originalAvailableServiceBinding.getName() + ":" + originalAvailableServiceBinding.getIsInheritable());
 				    db.create(originalAvailableServiceBinding);
 				    availableServiceBinding = originalAvailableServiceBinding;
 				    readAvailableServiceBindings.put(availableServiceBindingName, availableServiceBinding);
 				    
 				    logger.info("Notifying:" + siteNodeTypeDefinition.getName() + " about the new availableServiceBinding " + availableServiceBinding.getName());
 				    if(siteNodeTypeDefinition != null)
 				    {
 					    siteNodeTypeDefinition.getAvailableServiceBindings().add((AvailableServiceBindingImpl)availableServiceBinding);
 					    serviceDefinition.getAvailableServiceBindings().add((AvailableServiceBindingImpl)availableServiceBinding);
 					    availableServiceBinding.getSiteNodeTypeDefinitions().add((SiteNodeTypeDefinitionImpl)siteNodeTypeDefinition);
 					    availableServiceBinding.getServiceDefinitions().add((ServiceDefinitionImpl)serviceDefinition);
 				    }
 				}
 				else
 				{
 					if(siteNodeTypeDefinition != null && !siteNodeTypeDefinition.getAvailableServiceBindings().contains(availableServiceBinding))
 					{
 						siteNodeTypeDefinition.getAvailableServiceBindings().add((AvailableServiceBindingImpl)availableServiceBinding);
 						availableServiceBinding.getSiteNodeTypeDefinitions().add(siteNodeTypeDefinition);
 					}
 				}
 				
 				serviceBinding.setAvailableServiceBinding((AvailableServiceBindingImpl)availableServiceBinding);
 				
 				
 				Collection qualifyers = serviceBinding.getBindingQualifyers();
 				Iterator qualifyersIterator = qualifyers.iterator();
 				while(qualifyersIterator.hasNext())
 				{
 					Qualifyer qualifyer = (Qualifyer)qualifyersIterator.next();
 					qualifyer.setServiceBinding((ServiceBindingImpl)serviceBinding);
 					
 					String entityName 	= qualifyer.getName();
 					String entityId		= qualifyer.getValue();
 					
 					if(entityName.equalsIgnoreCase("contentId"))
 					{
 						String mappedContentId = (String)contentIdMap.get(entityId);
 						qualifyer.setValue((mappedContentId == null) ? entityId : mappedContentId);
 					}
 					else if(entityName.equalsIgnoreCase("siteNodeId"))
 					{
 						String mappedSiteNodeId = (String)siteNodeIdMap.get(entityId);
 						qualifyer.setValue((mappedSiteNodeId == null) ? entityId : mappedSiteNodeId);						
 					}
 				}
 
 				serviceBinding.setSiteNodeVersion((SiteNodeVersionImpl)siteNodeVersion);				
 
 				db.create(serviceBinding);
 
 			}
 		}		
 		
 	}
 
 
 	/**
 	 * This method copies a content and all it relations.
 	 * 
 	 * @param siteNode
 	 * @param db
 	 * @throws Exception
 	 */
 	
 	private List createContents(Content content, Map idMap, Map contentTypeDefinitionIdMap, List allContents, Collection contentTypeDefinitions, Map categoryIdMap, int version, Database db) throws Exception
 	{
 		ContentTypeDefinition contentTypeDefinition = null;
 		
 	    Integer originalContentId = content.getContentId();
 	    if(version == 2)
 	    {
 		    Integer contentTypeDefinitionId = ((ContentImpl)content).getContentTypeDefinitionId();
 		    
     		if(contentTypeDefinitionId != null)
 			{
     			if(contentTypeDefinitionIdMap.containsKey(contentTypeDefinitionId))
     				contentTypeDefinitionId = (Integer)contentTypeDefinitionIdMap.get(contentTypeDefinitionId);
     				
 		    	ContentTypeDefinition originalContentTypeDefinition = null;
 		    	Iterator contentTypeDefinitionsIterator = contentTypeDefinitions.iterator();
 		    	while(contentTypeDefinitionsIterator.hasNext())
 		    	{
 		    		ContentTypeDefinition contentTypeDefinitionCandidate = (ContentTypeDefinition)contentTypeDefinitionsIterator.next();		    		
 		    		if(contentTypeDefinitionCandidate.getId().intValue() == contentTypeDefinitionId.intValue())
 		    		{
 		    			originalContentTypeDefinition = contentTypeDefinitionCandidate;
 		    			break;
 		    		}
 		    	}
 
 		    	//System.out.println("originalContentTypeDefinition:" + originalContentTypeDefinition);
 
 		    	if(originalContentTypeDefinition != null)
 		    	{
 			    	contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithName(originalContentTypeDefinition.getName(), db);
 
 			    	if(contentTypeDefinition == null)
 					{
 			    		Integer before = originalContentTypeDefinition.getId();
 			    		db.create(originalContentTypeDefinition);
 			    		contentTypeDefinition = originalContentTypeDefinition;
 			    		Integer after = originalContentTypeDefinition.getId();
 			    		contentTypeDefinitionIdMap.put(before, after);
 			    	}
 				
 		    		content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
 
 		    	}
 			    else
 			    	System.out.println("The content " + content.getName() + " had a content type not found amongst the listed ones:" + contentTypeDefinitionId);
 			}
 		    else
 		    	System.out.println("The content " + content.getName() + " had no content type at all");
 	    }
 	    else if(version == 1)
 	    {
 			ContentTypeDefinition originalContentTypeDefinition = content.getContentTypeDefinition();
 			if(originalContentTypeDefinition != null)
 			{
 			    contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithName(originalContentTypeDefinition.getName(), db);
 				if(contentTypeDefinition == null)
 				{
 		    		//contentTypeDefinition = ContentTypeDefinitionController.getController().create(originalContentTypeDefinition.getValueObject(), db);
 		    		//contentTypeDefinitions.add(contentTypeDefinition);
 		    		
 				    db.create(originalContentTypeDefinition);
 				    contentTypeDefinition = originalContentTypeDefinition;
 				}
 
 	    		content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
 			}
 	    }
 	    
 	    if(content.getContentTypeDefinition() == null)
 	    	logger.error("No content type definition for content:" + content.getId());
 	    	
 	    logger.info("Creating content:" + content.getName());
 
 	    db.create(content);
 		
 		allContents.add(content);
 		
 		Integer newContentId = content.getContentId();
 		idMap.put(originalContentId.toString(), newContentId.toString());
 		
 		Collection contentVersions = content.getContentVersions();
 	    
 		if(onlyLatestVersions.equalsIgnoreCase("true"))
 		{
 			logger.info("org contentVersions:" + contentVersions.size());
 			List selectedContentVersions = new ArrayList();
 			Iterator realContentVersionsIterator = contentVersions.iterator();
 			while(realContentVersionsIterator.hasNext())
 			{
 				ContentVersion contentVersion = (ContentVersion)realContentVersionsIterator.next();			
 				Iterator selectedContentVersionsIterator = selectedContentVersions.iterator();
 				boolean addLanguageVersion = true;
 				boolean noLanguageVersionFound = true;
 				while(selectedContentVersionsIterator.hasNext())
 				{
 					ContentVersion currentContentVersion = (ContentVersion)selectedContentVersionsIterator.next();
 					logger.info("" + currentContentVersion.getLanguage().getLanguageCode() + "=" + contentVersion.getLanguage().getLanguageCode());
 					if(currentContentVersion.getLanguage().getLanguageCode().equals(contentVersion.getLanguage().getLanguageCode()))
 					{
 						noLanguageVersionFound = false;
 						
 						logger.info("" + contentVersion.getIsActive() + "=" + contentVersion.getLanguage().getLanguageCode());
 						if(contentVersion.getIsActive().booleanValue() && contentVersion.getContentVersionId().intValue() > currentContentVersion.getContentVersionId().intValue())
 						{
 							logger.info("A later version was found... removing this one..");
 							selectedContentVersionsIterator.remove();
 							addLanguageVersion = true;
 						}						
 					}
 				}
 	
 				if(addLanguageVersion || noLanguageVersionFound)
 					selectedContentVersions.add(contentVersion);
 			}	
 			
 			contentVersions = selectedContentVersions;
 		}
 		
 		logger.info("new contentVersions:" + contentVersions.size());
 		//Collection contentVersions = content.getContentVersions();
 		Iterator contentVersionsIterator = contentVersions.iterator();
 		while(contentVersionsIterator.hasNext())
 		{
 			ContentVersion contentVersion = (ContentVersion)contentVersionsIterator.next();
 			Language language = LanguageController.getController().getLanguageWithCode(contentVersion.getLanguage().getLanguageCode(), db);
 			logger.info("Creating contentVersion for language:" + contentVersion.getLanguage().getLanguageCode() + " on content " + content.getName());
 
 			contentVersion.setOwningContent((ContentImpl)content);
 			contentVersion.setLanguage((LanguageImpl)language);
 			
 			Collection digitalAssets = contentVersion.getDigitalAssets();
 			if(digitalAssets != null)
 			{
 				List initialDigitalAssets = new ArrayList();
 					
 				Iterator digitalAssetsIterator = digitalAssets.iterator();
 				while(digitalAssetsIterator.hasNext())
 				{
 					DigitalAsset digitalAsset = (DigitalAsset)digitalAssetsIterator.next();
 					
 					List initialContentVersions = new ArrayList();
 					initialContentVersions.add(contentVersion);
 					digitalAsset.setContentVersions(initialContentVersions);
 	
 					db.create(digitalAsset);
 					
 					initialDigitalAssets.add(digitalAsset);
 				}
 				
 				contentVersion.setDigitalAssets(initialDigitalAssets);
 			}
 
 			Collection contentCategories = contentVersion.getContentCategories();
 			logger.info("contentCategories:" + contentCategories.size());
 			
 			db.create(contentVersion);
 			
 			if(contentCategories != null)
 			{
 				List initialContentCategories = new ArrayList();
 					
 				Iterator contentCategoriesIterator = contentCategories.iterator();
 				while(contentCategoriesIterator.hasNext())
 				{
 					ContentCategory contentCategory = (ContentCategory)contentCategoriesIterator.next();
 					logger.info("contentCategory:" + contentCategory);
 					contentCategory.setContentVersion((ContentVersionImpl)contentVersion);
 					
 					Integer oldCategoryId = contentCategory.getCategoryId();
 					logger.info("oldCategoryId:" + oldCategoryId);
 					Integer newCategoryId = (Integer)categoryIdMap.get(oldCategoryId);
 					logger.info("newCategoryId:" + newCategoryId);
 					if(newCategoryId == null)
 						newCategoryId = oldCategoryId;
 					
 					if(newCategoryId != null)
 					{
 						Category category = CategoryController.getController().findById(newCategoryId, db);
 						logger.info("Got category:" + category);
 						if(category != null)
 						{
 							contentCategory.setCategory((CategoryImpl)category);
 							logger.info("Creating content category:" + contentCategory);
 							
 							db.create(contentCategory);
 						
 							initialContentCategories.add(contentCategory);
 						}
 					}
 				}
 				
 				contentVersion.setContentCategories(initialContentCategories);
 			}
 
 		}		
 		
 		Collection childContents = content.getChildren();
 		if(childContents != null)
 		{
 			Iterator childContentsIterator = childContents.iterator();
 			while(childContentsIterator.hasNext())
 			{
 				Content childContent = (Content)childContentsIterator.next();
 				childContent.setRepository(content.getRepository());
 				childContent.setParentContent((ContentImpl)content);
 				createContents(childContent, idMap, contentTypeDefinitionIdMap, allContents, contentTypeDefinitions, categoryIdMap, version, db);
 			}
 		}
 		
 		return allContents;
 	}
 
 
 	/**
 	 * This method updates all the bindings in content-versions to reflect the move. 
 	 */
 	/*
 	private void updateContentVersions(List allContents, Map contentIdMap, Map siteNodeIdMap)
 	{
 	    logger.info("allContents:" + allContents.size());
 	    Iterator allContentsIterator = allContents.iterator();
 	    while(allContentsIterator.hasNext())
 	    {
 	        Content content = (Content)allContentsIterator.next();
 	        
 	        logger.info("content:" + content.getName());
 	        Collection contentVersions = content.getContentVersions();
 	        
 			if(onlyLatestVersions.equalsIgnoreCase("true"))
 			{
 				logger.info("org contentVersions:" + contentVersions.size());
 				List selectedContentVersions = new ArrayList();
 				Iterator realContentVersionsIterator = contentVersions.iterator();
 				while(realContentVersionsIterator.hasNext())
 				{
 					ContentVersion contentVersion = (ContentVersion)realContentVersionsIterator.next();			
 					Iterator selectedContentVersionsIterator = selectedContentVersions.iterator();
 					boolean addLanguageVersion = true;
 					boolean noLanguageVersionFound = true;
 					while(selectedContentVersionsIterator.hasNext())
 					{
 						ContentVersion currentContentVersion = (ContentVersion)selectedContentVersionsIterator.next();
 						logger.info("" + currentContentVersion.getLanguage().getLanguageCode() + "=" + contentVersion.getLanguage().getLanguageCode());
 						if(currentContentVersion.getLanguage().getLanguageCode().equals(contentVersion.getLanguage().getLanguageCode()))
 						{
 							noLanguageVersionFound = false;
 							
 							logger.info("" + contentVersion.getIsActive() + "=" + contentVersion.getLanguage().getLanguageCode());
 							if(contentVersion.getIsActive().booleanValue() && contentVersion.getContentVersionId().intValue() > currentContentVersion.getContentVersionId().intValue())
 							{
 								logger.info("A later version was found... removing this one..");
 								selectedContentVersionsIterator.remove();
 								addLanguageVersion = true;
 							}						
 						}
 					}
 		
 					if(addLanguageVersion || noLanguageVersionFound)
 						selectedContentVersions.add(contentVersion);
 				}	
 				
 				contentVersions = selectedContentVersions;
 			}
 
 	        
 	        Iterator contentVersionIterator = contentVersions.iterator();
 	        while(contentVersionIterator.hasNext())
 	        {
 	            ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
 	            String contentVersionValue = contentVersion.getVersionValue();
 
                 contentVersionValue = contentVersionValue.replaceAll("contentId=\"", "contentId=\"old_");
                 contentVersionValue = contentVersionValue.replaceAll("\\?contentId=", "\\?contentId=old_");
                 contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(", "getInlineAssetUrl\\(old_");
                 contentVersionValue = contentVersionValue.replaceAll("languageId,", "languageId,old_");
                 contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"", "entity=\"Content\" entityId=\"old_");
                 contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>", "entity='Content'><id>old_");
 
                 contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"", "siteNodeId=\"old_");
                 //if(contentVersionValue.indexOf("getPageUrl") > -1)
                 //	System.out.println("contentVersionValue:" + contentVersionValue);
                 
                 contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\((\\d)", "getPageUrl\\(old_$1");
                 
                 //if(contentVersionValue.indexOf("getPageUrl") > -1)
                 //	System.out.println("contentVersionValue:" + contentVersionValue);
                 
                 contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"", "entity=\"SiteNode\" entityId=\"old_");
                 contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>", "entity='SiteNode'><id>old_");
 	            
 	            //logger.info("contentVersionValue before:" + contentVersionValue);
                 
 	            Iterator contentIdMapIterator = contentIdMap.keySet().iterator();
 	            while (contentIdMapIterator.hasNext()) 
 	            {
 	                String oldContentId = (String)contentIdMapIterator.next();
 	                String newContentId = (String)contentIdMap.get(oldContentId);
 	                
 	                //logger.info("Replacing all:" + oldContentId + " with " + newContentId);
 	                
 	                contentVersionValue = contentVersionValue.replaceAll("contentId=\"old_" + oldContentId + "\"", "contentId=\"" + newContentId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("\\?contentId=old_" + oldContentId + "&", "\\?contentId=" + newContentId + "&");
 	                contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(old_" + oldContentId + ",", "getInlineAssetUrl\\(" + newContentId + ",");
 	                contentVersionValue = contentVersionValue.replaceAll("languageId,old_" + oldContentId + "\\)", "languageId," + newContentId + "\\)");
 	                contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"old_" + oldContentId + "\"", "entity=\"Content\" entityId=\"" + newContentId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>old_" + oldContentId + "</id>", "entity='Content'><id>" + newContentId + "</id>");
 	                //contentVersionValue = contentVersionValue.replaceAll("<id>" + oldContentId + "</id>", "<id>" + newContentId + "</id>");
 	            }
 	            
 	            Iterator siteNodeIdMapIterator = siteNodeIdMap.keySet().iterator();
 	            while (siteNodeIdMapIterator.hasNext()) 
 	            {
 	                String oldSiteNodeId = (String)siteNodeIdMapIterator.next();
 	                String newSiteNodeId = (String)siteNodeIdMap.get(oldSiteNodeId);
 	                
 	                //logger.info("Replacing all:" + oldSiteNodeId + " with " + newSiteNodeId);
 	                
 	                contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"old_" + oldSiteNodeId + "\"", "siteNodeId=\"" + newSiteNodeId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\(old_" + oldSiteNodeId + ",", "getPageUrl\\(" + newSiteNodeId + ",");
 	                contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"old_" + oldSiteNodeId + "\"", "entity=\"SiteNode\" entityId=\"" + newSiteNodeId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>old_" + oldSiteNodeId + "</id>", "entity='SiteNode'><id>" + newSiteNodeId + "</id>");
 	            }
 	            
 	            //logger.info("contentVersionValue after:" + contentVersionValue);
 	            
 	            //Now replace all occurrances of old as they should never be there.
                 contentVersionValue = contentVersionValue.replaceAll("old_", "");
 
 	            logger.info("new contentVersionValue:" + contentVersionValue);
 	            contentVersion.setVersionValue(contentVersionValue);
 	        }
 	    }
 	}
 	*/
 
 	/**
 	 * This method updates all the bindings in content-versions to reflect the move. 
 	 */
 	private void updateContentVersions(Content content, Map contentIdMap, Map siteNodeIdMap) throws Exception
 	{
 	    logger.info("content:" + content.getName());
 
 	    Collection contentVersions = content.getContentVersions();
 	        
 		if(onlyLatestVersions.equalsIgnoreCase("true"))
 		{
 			logger.info("org contentVersions:" + contentVersions.size());
 			List selectedContentVersions = new ArrayList();
 			Iterator realContentVersionsIterator = contentVersions.iterator();
 			while(realContentVersionsIterator.hasNext())
 			{
 				ContentVersion contentVersion = (ContentVersion)realContentVersionsIterator.next();			
 				Iterator selectedContentVersionsIterator = selectedContentVersions.iterator();
 				boolean addLanguageVersion = true;
 				boolean noLanguageVersionFound = true;
 				while(selectedContentVersionsIterator.hasNext())
 				{
 					ContentVersion currentContentVersion = (ContentVersion)selectedContentVersionsIterator.next();
 					logger.info("" + currentContentVersion.getLanguage().getLanguageCode() + "=" + contentVersion.getLanguage().getLanguageCode());
 					if(currentContentVersion.getLanguage().getLanguageCode().equals(contentVersion.getLanguage().getLanguageCode()))
 					{
 						noLanguageVersionFound = false;
 						
 						logger.info("" + contentVersion.getIsActive() + "=" + contentVersion.getLanguage().getLanguageCode());
 						if(contentVersion.getIsActive().booleanValue() && contentVersion.getContentVersionId().intValue() > currentContentVersion.getContentVersionId().intValue())
 						{
 							logger.info("A later version was found... removing this one..");
 							selectedContentVersionsIterator.remove();
 							addLanguageVersion = true;
 						}						
 					}
 				}
 	
 				if(addLanguageVersion || noLanguageVersionFound)
 					selectedContentVersions.add(contentVersion);
 			}	
 			
 			contentVersions = selectedContentVersions;
 		}
 
         
         Iterator contentVersionIterator = contentVersions.iterator();
         while(contentVersionIterator.hasNext())
         {
             ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
             String contentVersionValue = contentVersion.getVersionValue();
 
             contentVersionValue = contentVersionValue.replaceAll("contentId=\"", "contentId=\"oldContentId_");
             contentVersionValue = contentVersionValue.replaceAll("\\?contentId=", "\\?contentId=oldContentId_");
             contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(", "getInlineAssetUrl\\(oldContentId_");
             contentVersionValue = contentVersionValue.replaceAll("languageId,", "languageId,oldContentId_");
             contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"", "entity=\"Content\" entityId=\"oldContentId_");
             //contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>", "entity='Content'><id>oldContentId_");
             contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"", "siteNodeId=\"oldSiteNodeId_");
             contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\((\\d)", "getPageUrl\\(oldSiteNodeId_$1");
             contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"", "entity=\"SiteNode\" entityId=\"oldSiteNodeId_");
             //contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>", "entity='SiteNode'><id>old_");
 
             contentVersionValue = this.prepareAllRelations(contentVersionValue);
             	            
             
             //logger.info("contentVersionValue before:" + contentVersionValue);
             
             Iterator contentIdMapIterator = contentIdMap.keySet().iterator();
             while (contentIdMapIterator.hasNext()) 
             {
                 String oldContentId = (String)contentIdMapIterator.next();
                 String newContentId = (String)contentIdMap.get(oldContentId);
                 
                 //logger.info("Replacing all:" + oldContentId + " with " + newContentId);
                 
                 contentVersionValue = contentVersionValue.replaceAll("contentId=\"oldContentId_" + oldContentId + "\"", "contentId=\"" + newContentId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("\\?contentId=oldContentId_" + oldContentId + "&", "\\?contentId=" + newContentId + "&");
                 contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(oldContentId_" + oldContentId + ",", "getInlineAssetUrl\\(" + newContentId + ",");
                 contentVersionValue = contentVersionValue.replaceAll("languageId,oldContentId_" + oldContentId + "\\)", "languageId," + newContentId + "\\)");
                 contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"oldContentId_" + oldContentId + "\"", "entity=\"Content\" entityId=\"" + newContentId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("<id>oldContentId_" + oldContentId + "</id>", "<id>" + newContentId + "</id>");
                 //contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>old_" + oldContentId + "</id>", "entity='Content'><id>" + newContentId + "</id>");
                 //contentVersionValue = contentVersionValue.replaceAll("<id>" + oldContentId + "</id>", "<id>" + newContentId + "</id>");
             }
             
             Iterator siteNodeIdMapIterator = siteNodeIdMap.keySet().iterator();
             while (siteNodeIdMapIterator.hasNext()) 
             {
                 String oldSiteNodeId = (String)siteNodeIdMapIterator.next();
                 String newSiteNodeId = (String)siteNodeIdMap.get(oldSiteNodeId);
                 
                 //logger.info("Replacing all:" + oldSiteNodeId + " with " + newSiteNodeId);
                 
                 contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "siteNodeId=\"" + newSiteNodeId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\(oldSiteNodeId_" + oldSiteNodeId + ",", "getPageUrl\\(" + newSiteNodeId + ",");
                 contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "entity=\"SiteNode\" entityId=\"" + newSiteNodeId + "\"");
                 //contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>old_" + oldSiteNodeId + "</id>", "entity='SiteNode'><id>" + newSiteNodeId + "</id>");
                 contentVersionValue = contentVersionValue.replaceAll("<id>oldSiteNodeId_" + oldSiteNodeId + "</id>", "<id>" + newSiteNodeId + "</id>");
             }
             
             //logger.info("contentVersionValue after:" + contentVersionValue);
             
             //Now replace all occurrances of old as they should never be there.
             contentVersionValue = contentVersionValue.replaceAll("oldContentId_", "");
             contentVersionValue = contentVersionValue.replaceAll("oldSiteNodeId_", "");
 
             logger.info("new contentVersionValue:" + contentVersionValue);
             contentVersion.setVersionValue(contentVersionValue);
         }
 	}
 
 	private String prepareAllRelations(String xml) throws Exception
 	{
 		StringBuffer newXML = new StringBuffer();
 		
 		logger.info("xml: " + xml);
 		
     	String after = xml;
     	String before = "";
     	String qualifyer = "";
     	boolean changed = false; 
     	
     	int startIndex = xml.indexOf("<qualifyer");
     	while(startIndex > -1)
     	{
     		int stopIndex = xml.indexOf("</qualifyer>", startIndex);
     		if(stopIndex > -1)
     		{
     			changed = true;
 	    		before = xml.substring(0, startIndex);
 	    		after = xml.substring(stopIndex + 12);
 	    		qualifyer = xml.substring(startIndex, stopIndex + 12);
 	    		//logger.info("startIndex: " + startIndex);
 	    		//System.out.println("stopIndex: " + stopIndex);
 	    		//System.out.println("before: " + before);
 	    		//System.out.println("after: " + after);
 	    		//System.out.println("qualifyer: " + qualifyer);
 	    		
 	    		String newQualifyer = qualifyer;
 	    		
 	    		if(qualifyer.indexOf("entity='Content'") > 0)
 	    			newQualifyer = qualifyer.replaceAll("<id>", "<id>oldContentId_");
 	    		else if(qualifyer.indexOf("entity='SiteNode'") > 0)
 	    			newQualifyer = qualifyer.replaceAll("<id>", "<id>oldSiteNodeId_");
 	    			
 	    		newXML.append(before);
 	    		newXML.append(newQualifyer);
 	    		//System.out.println("newXML:" + newXML);
 	    		xml = after;
     		}
     		else
     		{
     			throw new Exception("Error in xml - qualifyer tag broken in " + xml);
     		}
     		
     		startIndex = xml.indexOf("<qualifyer");
     	}
 
 		newXML.append(after);
 		
 		if(changed)
 			logger.info("newXML:" + newXML);
 		
 		return newXML.toString();
 	}
 	
 	public String getOnlyLatestVersions() {
 		return onlyLatestVersions;
 	}
 
 	public void setOnlyLatestVersions(String onlyLatestVersions) {
 		this.onlyLatestVersions = onlyLatestVersions;
 	}
 
 	/*
 		private void updateContentVersions(List allContents, Map contentIdMap, Map siteNodeIdMap)
 	{
 	    logger.info("allContents:" + allContents.size());
 	    Iterator allContentsIterator = allContents.iterator();
 	    while(allContentsIterator.hasNext())
 	    {
 	        Content content = (Content)allContentsIterator.next();
 	        
 	        logger.info("content:" + content);
 	        
 	        Iterator contentVersionIterator = content.getContentVersions().iterator();
 	        while(contentVersionIterator.hasNext())
 	        {
 	            ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
 	            String contentVersionValue = contentVersion.getVersionValue();
 
                 contentVersionValue = contentVersionValue.replaceAll("contentId=\"" + oldContentId + "\"", "contentId=\"" + newContentId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("contentId=" + oldContentId + "&", "contentId=" + newContentId + "&");
                 contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(" + oldContentId + ",", "getInlineAssetUrl\\(" + newContentId + ",");
                 contentVersionValue = contentVersionValue.replaceAll("languageId," + oldContentId + "\\)", "languageId," + newContentId + "\\)");
                 contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"" + oldContentId + "\"", "entity=\"Content\" entityId=\"" + newContentId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>" + oldContentId + "</id>", "entity='Content'><id>" + newContentId + "</id>");
 
                 contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"" + oldSiteNodeId + "\"", "siteNodeId=\"" + newSiteNodeId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\(" + oldSiteNodeId + ",", "getPageUrl\\(" + newSiteNodeId + ",");
                 contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"" + oldSiteNodeId + "\"", "entity=\"SiteNode\" entityId=\"" + newSiteNodeId + "\"");
                 contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>" + oldSiteNodeId + "</id>", "entity='SiteNode'><id>" + newSiteNodeId + "</id>");
 	            
 	            logger.info("contentVersionValue before:" + contentVersionValue);
                 
 	            Iterator contentIdMapIterator = contentIdMap.keySet().iterator();
 	            while (contentIdMapIterator.hasNext()) 
 	            {
 	                String oldContentId = (String)contentIdMapIterator.next();
 	                String newContentId = (String)contentIdMap.get(oldContentId);
 	                
 	                logger.info("Replacing all:" + oldContentId + " with " + newContentId);
 	                
 	                contentVersionValue = contentVersionValue.replaceAll("contentId=\"" + oldContentId + "\"", "contentId=\"" + newContentId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("contentId=" + oldContentId + "&", "contentId=" + newContentId + "&");
 	                contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(" + oldContentId + ",", "getInlineAssetUrl\\(" + newContentId + ",");
 	                contentVersionValue = contentVersionValue.replaceAll("languageId," + oldContentId + "\\)", "languageId," + newContentId + "\\)");
 	                contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"" + oldContentId + "\"", "entity=\"Content\" entityId=\"" + newContentId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>" + oldContentId + "</id>", "entity='Content'><id>" + newContentId + "</id>");
 	                //contentVersionValue = contentVersionValue.replaceAll("<id>" + oldContentId + "</id>", "<id>" + newContentId + "</id>");
 	            }
 	            
 	            Iterator siteNodeIdMapIterator = siteNodeIdMap.keySet().iterator();
 	            while (siteNodeIdMapIterator.hasNext()) 
 	            {
 	                String oldSiteNodeId = (String)siteNodeIdMapIterator.next();
 	                String newSiteNodeId = (String)siteNodeIdMap.get(oldSiteNodeId);
 	                
 	                logger.info("Replacing all:" + oldSiteNodeId + " with " + newSiteNodeId);
 	                
 	                contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"" + oldSiteNodeId + "\"", "siteNodeId=\"" + newSiteNodeId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\(" + oldSiteNodeId + ",", "getPageUrl\\(" + newSiteNodeId + ",");
 	                contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"" + oldSiteNodeId + "\"", "entity=\"SiteNode\" entityId=\"" + newSiteNodeId + "\"");
 	                contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>" + oldSiteNodeId + "</id>", "entity='SiteNode'><id>" + newSiteNodeId + "</id>");
 	            }
 	            
 	            logger.info("contentVersionValue after:" + contentVersionValue);
 
 	            logger.info("new contentVersionValue:" + contentVersionValue);
 	            contentVersion.setVersionValue(contentVersionValue);
 	        }
 	    }
 	}
 	*/
 }
