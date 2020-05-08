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
 
 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.entities.content.Content;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.content.DigitalAsset;
 import org.infoglue.cms.entities.content.DigitalAssetVO;
 import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
 import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.management.GroupProperties;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.RoleProperties;
 import org.infoglue.cms.entities.management.UserProperties;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 import org.infoglue.cms.util.graphics.ThumbnailGenerator;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.HttpHelper;
 import org.infoglue.deliver.util.Timer;
 
 /**
  * @author Mattias Bogeblad
  */
 
 public class DigitalAssetController extends BaseController 
 {
     private final static Logger logger = Logger.getLogger(DigitalAssetController.class.getName());
     
     private final static String BROKENFILENAME = "brokenAsset.gif";
     
     public static DigitalAssetController getController()
     {
         return new DigitalAssetController();
     }
 
     
    	/**
    	 * returns the digital asset VO
    	 */
    	
    	public static DigitalAssetVO getDigitalAssetVOWithId(Integer digitalAssetId) throws SystemException, Bug
     {
 		return (DigitalAssetVO) getVOWithId(DigitalAssetImpl.class, digitalAssetId);
     }
     
     /**
      * returns a digitalasset
      */
     
     public static DigitalAsset getDigitalAssetWithId(Integer digitalAssetId, Database db) throws SystemException, Bug
     {
 		return (DigitalAsset) getObjectWithId(DigitalAssetImpl.class, digitalAssetId, db);
     }
 
     /**
      * returns a shallow digitalasset
      */
     
     public static DigitalAssetVO getSmallDigitalAssetVOWithId(Integer digitalAssetId, Database db) throws SystemException, Bug
     {
     	return (DigitalAssetVO) getVOWithId(SmallDigitalAssetImpl.class, digitalAssetId, db);
     }
 
     /**
      * returns a shallow digitalasset
      */
     
     public static DigitalAsset getSmallDigitalAssetWithId(Integer digitalAssetId, Database db) throws SystemException, Bug
     {
     	return (DigitalAsset) getObjectWithId(SmallDigitalAssetImpl.class, digitalAssetId, db);
     }
 
 
    	/**
    	 * This method creates a new digital asset in the database and connects it to the contentVersion it belongs to.
    	 * The asset is send in as an InputStream which castor inserts automatically.
    	 */
 
    	public static DigitalAssetVO create(DigitalAssetVO digitalAssetVO, InputStream is, Integer contentVersionId) throws SystemException
    	{
 		Database db = CastorDatabaseService.getDatabase();
 
 		beginTransaction(db);
 		
 		try
 		{
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionId, db);
 
 			digitalAssetVO = create(digitalAssetVO, is, contentVersion, db);
 		    
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}		
 				
         return digitalAssetVO;
    	}
 
    	/**
    	 * This method creates a new digital asset in the database and connects it to the contentVersion it belongs to.
    	 * The asset is send in as an InputStream which castor inserts automatically.
    	 */
 
    	public static DigitalAssetVO create(DigitalAssetVO digitalAssetVO, InputStream is, ContentVersion contentVersion, Database db) throws SystemException, Exception
    	{
 		DigitalAsset digitalAsset = null;
 		
 		Collection contentVersions = new ArrayList();
 		contentVersions.add(contentVersion);
 		logger.info("Added contentVersion:" + contentVersion.getId());
 	
 		digitalAsset = new DigitalAssetImpl();
 		digitalAsset.setValueObject(digitalAssetVO.createCopy());
 		if(CmsPropertyHandler.getEnableDiskAssets().equals("false"))
 			digitalAsset.setAssetBlob(is);
 		digitalAsset.setContentVersions(contentVersions);
 
 		db.create(digitalAsset);
         
 		//if(contentVersion.getDigitalAssets() == null)
 		//    contentVersion.setDigitalAssets(new ArrayList());
 		
 		contentVersion.getDigitalAssets().add(digitalAsset);
 						
         return digitalAsset.getValueObject();
    	}
 
   	/**
    	 * This method creates a new digital asset in the database and connects it to the contentVersion it belongs to.
    	 * The asset is send in as an InputStream which castor inserts automatically.
    	 */
 
    	public static DigitalAssetVO create(DigitalAssetVO digitalAssetVO, InputStream is, String entity, Integer entityId) throws ConstraintException, SystemException
    	{
 		Database db = CastorDatabaseService.getDatabase();
         
 		DigitalAsset digitalAsset = null;
 		
 		beginTransaction(db);
 		
 		try
 		{
 		    if(entity.equalsIgnoreCase("ContentVersion"))
 		    {
 				ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(entityId, db);
 				Collection contentVersions = new ArrayList();
 				contentVersions.add(contentVersion);
 				logger.info("Added contentVersion:" + contentVersion.getId());
 	   		
 				digitalAsset = new DigitalAssetImpl();
 				digitalAsset.setValueObject(digitalAssetVO);
 				if(CmsPropertyHandler.getEnableDiskAssets().equals("false"))
 					digitalAsset.setAssetBlob(is);
 				digitalAsset.setContentVersions(contentVersions);
 
 				db.create(digitalAsset);
 	        
 				contentVersion.getDigitalAssets().add(digitalAsset);		        
 		    }
 		    else if(entity.equalsIgnoreCase(UserProperties.class.getName()))
 		    {
 				UserProperties userProperties = UserPropertiesController.getController().getUserPropertiesWithId(entityId, db);
 				Collection userPropertiesList = new ArrayList();
 				userPropertiesList.add(userProperties);
 				logger.info("Added userProperties:" + userProperties.getId());
 	   		
 				digitalAsset = new DigitalAssetImpl();
 				digitalAsset.setValueObject(digitalAssetVO);
 				if(CmsPropertyHandler.getEnableDiskAssets().equals("false"))
 					digitalAsset.setAssetBlob(is);
 				digitalAsset.setUserProperties(userPropertiesList);
 				
 				db.create(digitalAsset);
 	        
 				userProperties.getDigitalAssets().add(digitalAsset);		        
 		    }
 		    else if(entity.equalsIgnoreCase(RoleProperties.class.getName()))
 		    {
 		        RoleProperties roleProperties = RolePropertiesController.getController().getRolePropertiesWithId(entityId, db);
 				Collection rolePropertiesList = new ArrayList();
 				rolePropertiesList.add(roleProperties);
 				logger.info("Added roleProperties:" + roleProperties.getId());
 	   		
 				digitalAsset = new DigitalAssetImpl();
 				digitalAsset.setValueObject(digitalAssetVO);
 				if(CmsPropertyHandler.getEnableDiskAssets().equals("false"))
 					digitalAsset.setAssetBlob(is);
 				digitalAsset.setRoleProperties(rolePropertiesList);
 				
 				db.create(digitalAsset);
 	        
 				roleProperties.getDigitalAssets().add(digitalAsset);		        		        
 		    }
 		    else if(entity.equalsIgnoreCase(GroupProperties.class.getName()))
 		    {
 		        GroupProperties groupProperties = GroupPropertiesController.getController().getGroupPropertiesWithId(entityId, db);
 				Collection groupPropertiesList = new ArrayList();
 				groupPropertiesList.add(groupProperties);
 				logger.info("Added groupProperties:" + groupProperties.getId());
 	   		
 				digitalAsset = new DigitalAssetImpl();
 				digitalAsset.setValueObject(digitalAssetVO);
 				if(CmsPropertyHandler.getEnableDiskAssets().equals("false"))
 					digitalAsset.setAssetBlob(is);
 				digitalAsset.setGroupProperties(groupPropertiesList);
 				
 				db.create(digitalAsset);
 	        
 				groupProperties.getDigitalAssets().add(digitalAsset);		        		        
 		    }
 		
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}		
 				
         return digitalAsset.getValueObject();
    	}
 
    	/**
    	 * This method creates a new digital asset in the database.
    	 * The asset is send in as an InputStream which castor inserts automatically.
    	 */
 
    	public DigitalAsset create(Database db, DigitalAssetVO digitalAssetVO, InputStream is) throws SystemException, Exception
    	{
 		DigitalAsset digitalAsset = new DigitalAssetImpl();
 		digitalAsset.setValueObject(digitalAssetVO);
 		digitalAsset.setAssetBlob(is);
 
 		db.create(digitalAsset);
 				
         return digitalAsset;
    	}
 
    	/**
    	 * This method creates a new digital asset in the database and connects it to the contentVersion it belongs to.
    	 * The asset is send in as an InputStream which castor inserts automatically.
    	 */
 
 	public DigitalAssetVO createByCopy(Integer originalContentVersionId, String oldAssetKey, Integer newContentVersionId, String newAssetKey, Database db) throws ConstraintException, SystemException
 	{
 		logger.info("Creating by copying....");
 		logger.info("originalContentVersionId:" + originalContentVersionId);
 		logger.info("oldAssetKey:" + oldAssetKey);
 		logger.info("newContentVersionId:" + newContentVersionId);
 		logger.info("newAssetKey:" + newAssetKey);
 		
 		DigitalAsset oldDigitalAsset = getDigitalAsset(originalContentVersionId, oldAssetKey, db);
 		
 		ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(newContentVersionId, db);
 		Collection contentVersions = new ArrayList();
 		contentVersions.add(contentVersion);
 		logger.info("Added contentVersion:" + contentVersion.getId());
    		
 		DigitalAssetVO digitalAssetVO = new DigitalAssetVO();
 		digitalAssetVO.setAssetContentType(oldDigitalAsset.getAssetContentType());
 		digitalAssetVO.setAssetFileName(oldDigitalAsset.getAssetFileName());
 		digitalAssetVO.setAssetFilePath(oldDigitalAsset.getAssetFilePath());
 		digitalAssetVO.setAssetFileSize(oldDigitalAsset.getAssetFileSize());
 		digitalAssetVO.setAssetKey(newAssetKey);
 		
 		DigitalAsset digitalAsset = new DigitalAssetImpl();
 		digitalAsset.setValueObject(digitalAssetVO);
 		digitalAsset.setAssetBlob(oldDigitalAsset.getAssetBlob());
 		digitalAsset.setContentVersions(contentVersions);
 		
 		try
 		{
 			db.create(digitalAsset);
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred so we should not complete the transaction:" + e, e);
 			throw new SystemException(e.getMessage());
 		}
 		//contentVersion.getDigitalAssets().add(digitalAsset);
 		
 		return digitalAsset.getValueObject();
 	}
 
 	/**
 	 * This method gets a asset with a special key inside the given transaction.
 	 */
 	
 	public DigitalAsset getDigitalAsset(Integer contentVersionId, String assetKey, Database db) throws SystemException
 	{
 		DigitalAsset digitalAsset = null;
 		
 		ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionId, db);
 		Collection digitalAssets = contentVersion.getDigitalAssets();
 		Iterator assetIterator = digitalAssets.iterator();
 		while(assetIterator.hasNext())
 		{
 			DigitalAsset currentDigitalAsset = (DigitalAsset)assetIterator.next();
 			if(currentDigitalAsset.getAssetKey().equals(assetKey))
 			{
 				digitalAsset = currentDigitalAsset;
 				break;
 			}
 		}
 		
 		return digitalAsset;
 	}
 	
     
    	/**
    	 * This method deletes a digital asset in the database.
    	 */
 
    	public static void delete(Integer digitalAssetId) throws ConstraintException, SystemException
    	{
 		deleteEntity(DigitalAssetImpl.class, digitalAssetId);
    	}
 
    	/**
    	 * This method deletes a digital asset in the database.
    	 */
 
    	public void delete(Integer digitalAssetId, Database db) throws ConstraintException, SystemException
    	{
 		deleteEntity(DigitalAssetImpl.class, digitalAssetId, db);
    	}
 
    	/**
    	 * This method deletes a digital asset in the database.
    	 */
 
    	public void delete(Integer digitalAssetId, String entity, Integer entityId) throws ConstraintException, SystemException
    	{
     	Database db = CastorDatabaseService.getDatabase();
 
         beginTransaction(db);
 
         try
         {           
     		DigitalAsset digitalAsset = DigitalAssetController.getDigitalAssetWithId(digitalAssetId, db);			
     		
     		if(entity.equalsIgnoreCase("ContentVersion"))
                 ContentVersionController.getContentVersionController().deleteDigitalAssetRelation(entityId, digitalAsset, db);
             else if(entity.equalsIgnoreCase(UserProperties.class.getName()))
                 UserPropertiesController.getController().deleteDigitalAssetRelation(entityId, digitalAsset, db);
             else if(entity.equalsIgnoreCase(RoleProperties.class.getName()))
                 RolePropertiesController.getController().deleteDigitalAssetRelation(entityId, digitalAsset, db);
             else if(entity.equalsIgnoreCase(GroupProperties.class.getName()))
                 GroupPropertiesController.getController().deleteDigitalAssetRelation(entityId, digitalAsset, db);
 
             db.remove(digitalAsset);
 
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
    	}
    	
    	public static File[] getCachedFiles() throws SystemException, Exception
    	{
    		File[] cachedAssets = (File[])CacheController.getCachedObjectFromAdvancedCache("cachedAssetFileList", "allAssets", 300);
    		if(cachedAssets == null)
    		{
    			File assetDirectory = new File(CmsPropertyHandler.getDigitalAssetPath());
    			cachedAssets = assetDirectory.listFiles(); 
 			//System.out.println("Read and cached files:" + cachedAssets);
 			
    			CacheController.cacheObjectInAdvancedCache("cachedAssetFileList", "allAssets", cachedAssets);
    		}   			
    		
    		return cachedAssets;
    	}
 
    	/**
 	 * This method removes all images in the digitalAsset directory which belongs to a certain digital asset.
 	 */
 	
 	public static void deleteCachedDigitalAssets(Integer digitalAssetId) throws SystemException, Exception
 	{ 
 		try
 		{
 			File[] cachedFiles = getCachedFiles();
 			for(int i=0; i<cachedFiles.length; i++)
 			{
 				File cachedFile = cachedFiles[i];
 				if(cachedFile.getName().startsWith("" + digitalAssetId))
 				{
 					//File file = files[i];
 					cachedFile.delete();
 				}
 			}
 			/*
 			File assetDirectory = new File(CmsPropertyHandler.getDigitalAssetPath());
 			File[] files = assetDirectory.listFiles(new FilenameFilterImpl(digitalAssetId.toString())); 	
 			for(int i=0; i<files.length; i++)
 			{
 				File file = files[i];
 				file.delete();
 			}
 			*/
 		}
 		catch(Exception e)
 		{
 			logger.error("Could not delete the assets for the digitalAsset " + digitalAssetId + ":" + e.getMessage(), e);
 		}
 	}
 	
    	/**
    	 * This method updates a digital asset in the database.
    	 */
    	
    	public static DigitalAssetVO update(DigitalAssetVO digitalAssetVO, InputStream is) throws ConstraintException, SystemException
     {
 		Database db = CastorDatabaseService.getDatabase();
 		
 		DigitalAsset digitalAsset = null;
 		
 		beginTransaction(db);
 
 		try
 		{
 			digitalAsset = getDigitalAssetWithId(digitalAssetVO.getId(), db);
 			
 			digitalAsset.setValueObject(digitalAssetVO);
 			if(is != null)
 			    digitalAsset.setAssetBlob(is);
 
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			logger.error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 
 		return digitalAsset.getValueObject();		
     } 
     
    	/**
 	 * This method deletes all contentVersions for the content sent in and also clears all the digital Assets.
 	 * Should not be available probably as you might destroy for other versions and other contents.
 	 * /
 	/*
 	public static void deleteDigitalAssetsForContentVersionWithId(Integer contentVersionId) throws ConstraintException, SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
         ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
 
         beginTransaction(db);
 		List digitalAssets = new ArrayList();
         try
         {        
             ContentVersion contentVersion = ContentVersionController.getContentVersionWithId(contentVersionId, db);
             
         	Collection digitalAssetList = contentVersion.getDigitalAssets();
 			Iterator assets = digitalAssetList.iterator();
 			while (assets.hasNext()) 
             {
             	DigitalAsset digitalAsset = (DigitalAsset)assets.next();
 				digitalAssets.add(digitalAsset.getValueObject());
             }
             
             commitTransaction(db);
         }
         catch(Exception e)
         {
         	e.printStackTrace();
             logger.error("An error occurred so we should not completes the transaction:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
 		Iterator i = digitalAssets.iterator();
 		while(i.hasNext())
 		{
 			DigitalAssetVO digitalAssetVO = (DigitalAssetVO)i.next();
 			logger.info("Deleting digitalAsset:" + digitalAssetVO.getDigitalAssetId());
 			delete(digitalAssetVO.getDigitalAssetId());
 		}    	
 
     }
 	*/
 
 	/**
 	 * This method should return a list of those digital assets the contentVersion has.
 	 */
 	   	
 	public static List getDigitalAssetVOList(Integer contentVersionId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
 
     	List digitalAssetVOList = new ArrayList();
 
         beginTransaction(db);
 
         try
         {
         	digitalAssetVOList = getDigitalAssetVOList(contentVersionId, db);
 			
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.info("An error occurred when we tried to fetch the list of digitalAssets belonging to this contentVersion:" + e);
             e.printStackTrace();
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return digitalAssetVOList;
     }
 
 	/**
 	 * This method should return a list of those digital assets the contentVersion has.
 	 */
 	   	
 	public static List getDigitalAssetVOList(Integer contentVersionId, Database db) throws Exception
     {
 		String key = "" + contentVersionId;
 		String cacheName = "digitalAssetCache";
 		List digitalAssetVOList = (List)CacheController.getCachedObject(cacheName, key);
 		if(digitalAssetVOList != null)
 		{
 			if(logger.isInfoEnabled())
 				logger.info("There was an cached digitalAssetVOList:" + digitalAssetVOList);
 			
 			return digitalAssetVOList;
 		}
 
 		digitalAssetVOList = new ArrayList();
     	
 		if(logger.isInfoEnabled())
 			logger.info("Making a sql call for assets on " + contentVersionId);
 
     	OQLQuery oql = db.getOQLQuery("CALL SQL SELECT c.digitalAssetId, c.assetFileName, c.assetKey, c.assetFilePath, c.assetContentType, c.assetFileSize FROM cmDigitalAsset c, cmContentVersionDigitalAsset cvda where cvda.digitalAssetId = c.digitalAssetId AND cvda.contentVersionId = $1 ORDER BY c.digitalAssetId AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl");
     	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
     		oql = db.getOQLQuery("CALL SQL SELECT c.DigAssetId, c.assetFileName, c.assetKey, c.assetFilePath, c.assetContentType, c.assetFileSize FROM cmDigAsset c, cmContVerDigAsset cvda where cvda.DigAssetId = c.DigAssetId AND cvda.ContVerId = $1 ORDER BY c.DigAssetId AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl");
 
     	oql.bind(contentVersionId);
     	
     	QueryResults results = oql.execute(Database.ReadOnly);
 		
 		while(results.hasMore()) 
         {
         	SmallDigitalAssetImpl digitalAsset = (SmallDigitalAssetImpl)results.next();
         	digitalAssetVOList.add(digitalAsset.getValueObject());
         }
 		
 		results.close();
 		oql.close();
     	
 		if(digitalAssetVOList != null)
 			CacheController.cacheObject(cacheName, key, digitalAssetVOList);
 		
 		return digitalAssetVOList;
     }
 
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static List getDigitalAssetVOList(Integer contentId, Integer languageId, boolean useLanguageFallback) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
 
         List digitalAssetVOList = null;
 
         beginTransaction(db);
 
         try
         {
             digitalAssetVOList = getDigitalAssetVOList(contentId, languageId, useLanguageFallback, db);
         	
             commitTransaction(db);
         }
         catch(Exception e)
         {
             e.printStackTrace();
             logger.info("An error occurred when we tried to cache and show the digital asset:" + e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return digitalAssetVOList;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static List getDigitalAssetVOList(Integer contentId, Integer languageId, boolean useLanguageFallback, Database db) throws SystemException, Bug, Exception
     {
 		String key = "" + contentId + "_" + languageId + "_" + useLanguageFallback;
 		String cacheName = "digitalAssetCache";
 		List digitalAssetVOList = (List)CacheController.getCachedObject(cacheName, key);
 		if(digitalAssetVOList != null)
 		{
 			if(logger.isInfoEnabled())
 				logger.info("There was an cached digitalAssetVOList:" + digitalAssetVOList);
 			
 			return digitalAssetVOList;
 		}
 
 		digitalAssetVOList = new ArrayList();
 		
     	Content content = ContentController.getContentController().getContentWithId(contentId, db);
     	logger.info("content:" + content.getName());
     	logger.info("repositoryId:" + content.getRepository().getId());
     	logger.info("languageId:" + languageId);
     	ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentId, languageId, db);
     	LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(content.getRepository().getRepositoryId(), db);	
 		
     	logger.info("contentVersion:" + contentVersion);
 		if(contentVersion != null)
 		{
 		    digitalAssetVOList = getDigitalAssetVOList(contentVersion.getId(), db);
 			
 			logger.info("digitalAssetVOList:" + digitalAssetVOList.size());
 			if(useLanguageFallback && languageId.intValue() != masterLanguageVO.getId().intValue())
 			{
 			    List masterDigitalAssetVOList = getDigitalAssetVOList(contentId, masterLanguageVO.getId(), useLanguageFallback, db);
 			    Iterator digitalAssetVOListIterator = digitalAssetVOList.iterator();
 			    while(digitalAssetVOListIterator.hasNext())
 			    {
 			        DigitalAssetVO currentDigitalAssetVO = (DigitalAssetVO)digitalAssetVOListIterator.next();
 			        
 			        Iterator masterDigitalAssetVOListIterator = masterDigitalAssetVOList.iterator();
 				    while(masterDigitalAssetVOListIterator.hasNext())
 				    {
 				        DigitalAssetVO masterCurrentDigitalAssetVO = (DigitalAssetVO)masterDigitalAssetVOListIterator.next();
 				        if(currentDigitalAssetVO.getAssetKey().equalsIgnoreCase(masterCurrentDigitalAssetVO.getAssetKey()))
 				            masterDigitalAssetVOListIterator.remove();
 				    }
 			    }
 			    digitalAssetVOList.addAll(masterDigitalAssetVOList);
 			}
 		}
 		else if(useLanguageFallback && languageId.intValue() != masterLanguageVO.getId().intValue())
 		{
 		    contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentId, masterLanguageVO.getId(), db);
 	    	
 	    	logger.info("contentVersion:" + contentVersion);
 			if(contentVersion != null)
 			{
 			    digitalAssetVOList = getDigitalAssetVOList(contentVersion.getId(), db);		
 			}
 		}
 		
 		if(digitalAssetVOList != null)
 			CacheController.cacheObject(cacheName, key, digitalAssetVOList);
 		
 		return digitalAssetVOList;
     }
 	
 	
 	
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 
 	public static String getDigitalAssetUrl(Integer digitalAssetId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
 
     	String assetUrl = null;
 
         beginTransaction(db);
 
         try
         {
         	DigitalAsset digitalAsset = getSmallDigitalAssetWithId(digitalAssetId, db);
 
 			if(digitalAsset != null)
 			{
 				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
 				if(logger.isInfoEnabled())
 				{
 					logger.info("folderName:" + folderName);
 					logger.info("Found a digital asset:" + digitalAsset.getAssetFileName());
 				}
 				//String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 				String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 				boolean fileExists = dumpDigitalAsset(digitalAsset.getValueObject(), fileName, filePath, db);
 				
 				//File outputFile = new File(filePath + File.separator + fileName);
 				if(!fileExists)
 					assetUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getImagesBaseUrl() + "/" + BROKENFILENAME;
 				else
 					assetUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + folderName + "/" + fileName;
 			}			       	
 
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred when we tried to cache and show the digital asset:" + e.getMessage());
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetUrl;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 
 	public static String getDigitalAssetFilePath(Integer digitalAssetId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
 
     	String assetPath = null;
 
         beginTransaction(db);
 
         try
         {
 			DigitalAsset digitalAsset = getSmallDigitalAssetWithId(digitalAssetId, db);
 						
 			if(digitalAsset != null)
 			{
 				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
 				if(logger.isInfoEnabled())
 				{
 					logger.info("folderName:" + folderName);
 					logger.info("Found a digital asset:" + digitalAsset.getAssetFileName());
 				}
 				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 				String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 				dumpDigitalAsset(digitalAsset.getValueObject(), fileName, filePath, db);
 				assetPath = filePath + File.separator + fileName;
 			}			       	
 
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred when we tried to cache and show the digital asset:" + e.getMessage());
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetPath;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 
 	public static String getDigitalAssetProtectedFilePath(Integer digitalAssetId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
 
     	String assetPath = null;
 
         beginTransaction(db);
 
         try
         {
 			DigitalAsset digitalAsset = getSmallDigitalAssetWithId(digitalAssetId, db);
 						
 			if(digitalAsset != null)
 			{
 				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
 				if(logger.isInfoEnabled())
 				{
 					logger.info("folderName:" + folderName);
 					logger.info("Found a digital asset:" + digitalAsset.getAssetFileName());
 				}
 				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 				String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "protected" + File.separator + folderName;
 				dumpDigitalAsset(digitalAsset.getValueObject(), fileName, filePath, db);
 				assetPath = filePath + File.separator + fileName;
 			}			       	
 
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.info("An error occurred when we tried to cache and show the digital asset:" + e.getMessage());
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetPath;
     }
 
 	/**
 	 * This is a method that stores the asset on disk if not there allready and returns the asset as an InputStream
 	 * from that location. To avoid trouble with in memory blobs.
 	 */
 	/*
 	public InputStream getAssetInputStream(DigitalAsset digitalAsset) throws Exception
 	{
 	    String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 		String filePath = CmsPropertyHandler.getDigitalAssetPath();
 		dumpDigitalAsset(digitalAsset, fileName, filePath);
 		File assetFile = new File(filePath + File.separator + fileName);
 		if(assetFile.exists())
 			return new FileInputStream(assetFile);
 		else
 			return new ByteArrayInputStream("archived".getBytes());
 	}
 	*/
 
 	/**
 	 * This is a method that stores the asset on disk if not there allready and returns the asset as an InputStream
 	 * from that location. To avoid trouble with in memory blobs.
 	 */
 	
 	public InputStream getAssetInputStream(DigitalAsset digitalAsset, boolean returnNullIfBroken) throws Exception
 	{
 		String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
 		if(logger.isInfoEnabled())
 			logger.info("folderName:" + folderName);
 	    String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 		String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 		boolean ok = dumpDigitalAsset(digitalAsset, fileName, filePath);
 		File assetFile = new File(filePath + File.separator + fileName);
 		if(ok)
 		{
 			return new FileInputStream(assetFile);
 		}
 		else
 		{
 			if(returnNullIfBroken)
 				return null;
 			else
 				return new ByteArrayInputStream("archived".getBytes());
 		}
 	}
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 
 	public String getDigitalAssetUrl(DigitalAssetVO digitalAssetVO, Database db) throws SystemException, Bug, Exception
     {
     	String assetUrl = null;
 
 		if(digitalAssetVO != null)
 		{
 			String folderName = "" + (digitalAssetVO.getDigitalAssetId().intValue() / 1000);
 			if(logger.isInfoEnabled())
 			{	
 				logger.info("folderName:" + folderName);
 				logger.info("Found a digital asset:" + digitalAssetVO.getAssetFileName());
 			}
 			String fileName = digitalAssetVO.getDigitalAssetId() + "_" + digitalAssetVO.getAssetFileName();
 			String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 			dumpDigitalAsset(digitalAssetVO, fileName, filePath, db);
 			assetUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + folderName + "/" + fileName;
 		}			       	
     	
 		return assetUrl;
     }
 
 
 	/**
 	 * This method should return a String containing the URL for this digital assets icon/thumbnail.
 	 * In the case of an image the downscaled image is returned - otherwise an icon that represents the
 	 * content-type of the file. It always fetches the latest one if several assets exists.
 	 */
 	   	
 	public static String getDigitalAssetThumbnailUrl(Integer digitalAssetId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
 
     	String assetUrl = null;
 
         beginTransaction(db);
 
         try
         {
 			DigitalAsset digitalAsset = getSmallDigitalAssetWithId(digitalAssetId, db);
 			if(digitalAsset != null)
 			{
 				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
 				if(logger.isInfoEnabled())
 				{
 					logger.info("folderName:" + folderName);
 					logger.info("Found a digital asset:" + digitalAsset.getAssetFileName());
 				}
 				String contentType = digitalAsset.getAssetContentType();
 				String assetFilePath = digitalAsset.getAssetFilePath();
 				if(assetFilePath.indexOf("IG_ARCHIVE:") > -1)
 				{
 					assetUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getImagesBaseUrl() + "/archivedAsset.gif";
 				}
 				else if(contentType.equalsIgnoreCase("image/gif") || contentType.equalsIgnoreCase("image/jpg") || contentType.equalsIgnoreCase("image/pjpeg") || contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/png"))
 				{
 					String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 					logger.info("fileName:" + fileName);
 					String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 					logger.info("filePath:" + filePath);
 					logger.info("Making thumb from:" + filePath + File.separator + fileName);
 					String thumbnailFileName = digitalAsset.getDigitalAssetId() + "_thumbnail_" + digitalAsset.getAssetFileName();
 					//String thumbnailFileName = "thumbnail_" + fileName;
 					File thumbnailFile = new File(filePath + File.separator + thumbnailFileName);
 					File originalFile = new File(filePath + File.separator + fileName);
 					if(!originalFile.exists())
 					{
 						logger.warn("The original file " + filePath + File.separator + fileName + " was not found - missing from system.");
 						assetUrl = "images" + File.separator + BROKENFILENAME;
 					}
 					else
 					{
 						if(!thumbnailFile.exists() && originalFile.exists())
 						{
 							logger.info("transforming...");
 							ThumbnailGenerator tg = new ThumbnailGenerator();
 							tg.transform(filePath + File.separator + fileName, filePath + File.separator + thumbnailFileName, 75, 75, 100);
 							logger.info("transform done...");
 						}
 						
 						assetUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + folderName + "/" + thumbnailFileName;
 						logger.info("assetUrl:" + assetUrl);
 					}
 				}
 				else
 				{
 					String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 					String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 					File originalFile = new File(filePath + File.separator + fileName);
 					if(!originalFile.exists())
 					{
 						assetUrl = "images" + File.separator + BROKENFILENAME;
 					}
 					else if(contentType.equalsIgnoreCase("application/pdf"))
 					{
 						assetUrl = "images/pdf.gif"; 
 					}
 					else if(contentType.equalsIgnoreCase("application/msword"))
 					{
 						assetUrl = "images/msword.gif"; 
 					}
 					else if(contentType.equalsIgnoreCase("application/vnd.ms-excel"))
 					{
 						assetUrl = "images/msexcel.gif"; 
 					}
 					else if(contentType.equalsIgnoreCase("application/vnd.ms-powerpoint"))
 					{
 						assetUrl = "images/mspowerpoint.gif"; 
 					}
 					else if(contentType.equalsIgnoreCase("application/zip"))
 					{
 						assetUrl = "images/zipIcon.gif"; 
 					}
 					else if(contentType.equalsIgnoreCase("text/xml"))
 					{
 						assetUrl = "images/xmlIcon.gif"; 
 					}
 					else
 					{
 						assetUrl = "images/digitalAsset.gif"; 
 					}		
 				}	
 			}	
 			            
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.info("An error occurred when we tried to cache and show the digital asset thumbnail:" + e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetUrl;
     }
 
    	
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static String getDigitalAssetUrl(Integer contentId, Integer languageId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
 
     	String assetUrl = null;
 
         beginTransaction(db);
 
         try
         {
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestContentVersion(contentId, languageId, db); 
 			if(contentVersion != null)
 			{
 				DigitalAssetVO digitalAssetVO = getLatestDigitalAssetVO(contentVersion.getId(), db);
 				
 				if(digitalAssetVO != null)
 				{
 					String folderName = "" + (digitalAssetVO.getDigitalAssetId().intValue() / 1000);
 					if(logger.isInfoEnabled())
 					{
 						logger.info("folderName:" + folderName);
 						logger.info("Found a digital asset:" + digitalAssetVO.getAssetFileName());
 					}
 					String fileName = digitalAssetVO.getDigitalAssetId() + "_" + digitalAssetVO.getAssetFileName();
 					String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 					
 					dumpDigitalAsset(digitalAssetVO, fileName, filePath, db);
 					assetUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + folderName + "/" + fileName;
 				}			       	
 			}
 			            
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.info("An error occurred when we tried to cache and show the digital asset:" + e);
             e.printStackTrace();
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetUrl;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static String getDigitalAssetUrl(Integer contentId, Integer languageId, String assetKey, boolean useLanguageFallback) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
 
     	String assetUrl = null;
 
         beginTransaction(db);
 
         try
         {
         	assetUrl = getDigitalAssetUrl(contentId, languageId, assetKey, useLanguageFallback, db);
         	
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.info("An error occurred when we tried to cache and show the digital asset:" + e);
             e.printStackTrace();
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetUrl;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static String getDigitalAssetUrl(Integer contentId, Integer languageId, String assetKey, boolean useLanguageFallback, Database db) throws SystemException, Bug, Exception
     {
     	String assetUrl = null;
 
     	Content content = ContentController.getContentController().getContentWithId(contentId, db);
     	if(logger.isInfoEnabled())
     	{
 	    	logger.info("content:" + content.getName());
 	    	logger.info("repositoryId:" + content.getRepository().getId());
 	    	logger.info("languageId:" + languageId);
 	    	logger.info("assetKey:" + assetKey);
     	}
     	
 		String fromEncoding = CmsPropertyHandler.getAssetKeyFromEncoding();
 		if(fromEncoding == null)
 			fromEncoding = "iso-8859-1";
 		
 		String toEncoding = CmsPropertyHandler.getAssetKeyToEncoding();
 		if(toEncoding == null)
 			toEncoding = "utf-8";
 		
 		assetKey = new String(assetKey.getBytes(fromEncoding), toEncoding);
 		
     	ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentId, languageId, db);
     	LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(content.getRepository().getRepositoryId(), db);	
 		logger.info("contentVersion:" + contentVersion);
 		if(contentVersion != null)
 		{
 			DigitalAssetVO digitalAssetVO = getLatestDigitalAssetVO(contentVersion.getContentVersionId(), assetKey, db);
 			logger.info("digitalAssetVO:" + digitalAssetVO);
 			if(digitalAssetVO != null)
 			{
 				String folderName = "" + (digitalAssetVO.getDigitalAssetId().intValue() / 1000);
 				if(logger.isInfoEnabled())
 				{
 					logger.info("folderName:" + folderName);
 					logger.info("digitalAsset:" + digitalAssetVO.getAssetKey());
 					logger.info("Found a digital asset:" + digitalAssetVO.getAssetFileName());
 				}
 				String fileName = digitalAssetVO.getDigitalAssetId() + "_" + digitalAssetVO.getAssetFileName();
 				String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 				
 				dumpDigitalAsset(digitalAssetVO, fileName, filePath, db);
 				assetUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + folderName + "/" + fileName;
 			}
 			else
 			{
 				//LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(content.getRepository().getRepositoryId(), db);
 				if(useLanguageFallback && languageId.intValue() != masterLanguageVO.getId().intValue())
 					return getDigitalAssetUrl(contentId, masterLanguageVO.getId(), assetKey, useLanguageFallback, db);
 			}
 		}
 		else if(useLanguageFallback && languageId.intValue() != masterLanguageVO.getId().intValue())
 		{
 		    contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentId, masterLanguageVO.getId(), db);
 	    	
 	    	logger.info("contentVersion:" + contentVersion);
 			if(contentVersion != null)
 			{
 			    DigitalAssetVO digitalAssetVO = getLatestDigitalAssetVO(contentVersion.getId(), assetKey, db);
 				logger.info("digitalAssetVO:" + digitalAssetVO);
 				if(digitalAssetVO != null)
 				{
 					String folderName = "" + (digitalAssetVO.getDigitalAssetId().intValue() / 1000);
 					if(logger.isInfoEnabled())
 					{
 						logger.info("folderName:" + folderName);
 						logger.info("digitalAsset:" + digitalAssetVO.getAssetKey());
 						logger.info("Found a digital asset:" + digitalAssetVO.getAssetFileName());
 					}
 					String fileName = digitalAssetVO.getDigitalAssetId() + "_" + digitalAssetVO.getAssetFileName();
 					String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 					
 					dumpDigitalAsset(digitalAssetVO, fileName, filePath, db);
 					assetUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + folderName + "/" + fileName;
 				}
 			}
 		}
 		
 		return assetUrl;
     }
 
 	/**
 	 * This method should return a String containing the URL for this digital asset.
 	 */
 	   	
 	public static DigitalAssetVO getDigitalAssetVO(Integer contentId, Integer languageId, String assetKey, boolean useLanguageFallback) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
 
     	DigitalAssetVO digitalAssetVO = null;
 
         beginTransaction(db);
 
         try
         {
         	digitalAssetVO = getDigitalAssetVO(contentId, languageId, assetKey, useLanguageFallback, db);
         	
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.info("An error occurred when we tried to get a digitalAssetVO:" + e);
             e.printStackTrace();
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return digitalAssetVO;
     }
 
 	/**
 	 * This method should return a DigitalAssetVO
 	 */
 	   	
 	public static DigitalAssetVO getDigitalAssetVO(Integer contentId, Integer languageId, String assetKey, boolean useLanguageFallback, Database db) throws SystemException, Bug, Exception
     {
     	DigitalAssetVO digitalAssetVO = null;
 
     	Content content = ContentController.getContentController().getContentWithId(contentId, db);
     	ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(contentId, languageId, db);
 		if(contentVersion != null)
 		{
 			DigitalAssetVO digitalAsset = getLatestDigitalAssetVO(contentVersion.getId(), assetKey, db);
 			if(digitalAsset != null)
 			{
 				digitalAssetVO = digitalAsset;
 			}
 			else
 			{
 				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(content.getRepository().getRepositoryId(), db);
 				if(useLanguageFallback && languageId.intValue() != masterLanguageVO.getId().intValue())
 					return getDigitalAssetVO(contentId, masterLanguageVO.getId(), assetKey, useLanguageFallback, db);
 			}
 		}
 		
 		return digitalAssetVO;
     }
 
 	
 	
 	/**
 	 * This method should return a String containing the URL for this digital assets icon/thumbnail.
 	 * In the case of an image the downscaled image is returned - otherwise an icon that represents the
 	 * content-type of the file. It always fetches the latest one if several assets exists.
 	 */
 	   	
 	public static String getDigitalAssetThumbnailUrl(Integer contentId, Integer languageId) throws SystemException, Bug
     {
     	Database db = CastorDatabaseService.getDatabase();
 
     	String assetUrl = null;
 
         beginTransaction(db);
 
         try
         {
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestContentVersion(contentId, languageId, db); 
 			if(contentVersion != null)
 			{
 				DigitalAsset digitalAsset = getSmallDigitalAssetWithId(contentVersion.getId(), db);
 
 				if(digitalAsset != null)
 				{
 					logger.info("Found a digital asset:" + digitalAsset.getAssetFileName());
 					String contentType = digitalAsset.getAssetContentType();
 					
 					String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
 					logger.info("folderName:" + folderName);
 					if(contentType.equalsIgnoreCase("image/gif") || contentType.equalsIgnoreCase("image/jpg") || contentType.equalsIgnoreCase("image/png"))
 					{
 						String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 						//String filePath = digitalAsset.getAssetFilePath();
 						String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 						String thumbnailFileName = digitalAsset.getDigitalAssetId() + "_thumbnail_" + digitalAsset.getAssetFileName();
 						//String thumbnailFileName = "thumbnail_" + fileName;
 						File thumbnailFile = new File(filePath + File.separator + thumbnailFileName);
 						if(!thumbnailFile.exists())
 						{
 							ThumbnailGenerator tg = new ThumbnailGenerator();
 							tg.transform(filePath + File.separator + fileName, filePath + File.separator + thumbnailFileName, 150, 150, 100);
 						}
 						assetUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + folderName + "/" + thumbnailFileName;
 					}
 					else
 					{
 						String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 						String filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
 						File originalFile = new File(filePath + File.separator + fileName);
 						if(!originalFile.exists())
 						{
 							assetUrl = "images" + File.separator + BROKENFILENAME;
 						}
 						else if(contentType.equalsIgnoreCase("application/pdf"))
 						{
 							assetUrl = "images/pdf.gif"; 
 						}
 						else if(contentType.equalsIgnoreCase("application/msword"))
 						{
 							assetUrl = "images/msword.gif"; 
 						}
 						else if(contentType.equalsIgnoreCase("application/vnd.ms-excel"))
 						{
 							assetUrl = "images/msexcel.gif"; 
 						}
 						else if(contentType.equalsIgnoreCase("application/vnd.ms-powerpoint"))
 						{
 							assetUrl = "images/mspowerpoint.gif"; 
 						}
 						else if(contentType.equalsIgnoreCase("application/zip"))
 						{
 							assetUrl = "images/zipIcon.gif"; 
 						}
 						else if(contentType.equalsIgnoreCase("text/xml"))
 						{
 							assetUrl = "images/xmlIcon.gif"; 
 						}
 						else
 						{
 							assetUrl = "images/digitalAsset.gif"; 
 						}		
 					}	
 				}	
 				else
 				{
 					assetUrl = "images/notDefined.gif";
 				}		       	
 			}
 			            
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.info("An error occurred when we tried to cache and show the digital asset thumbnail:" + e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
     	
 		return assetUrl;
     }
 
 	/**
 	 * Returns the latest digital asset for a contentversion.
 	 */
 	
 	private static DigitalAsset getLatestDigitalAsset(ContentVersion contentVersion)
 	{
 		Collection digitalAssets = contentVersion.getDigitalAssets();
 		Iterator iterator = digitalAssets.iterator();
 		
 		DigitalAsset digitalAsset = null;
 		while(iterator.hasNext())
 		{
 			DigitalAsset currentDigitalAsset = (DigitalAsset)iterator.next();	
 			if(digitalAsset == null || currentDigitalAsset.getDigitalAssetId().intValue() > digitalAsset.getDigitalAssetId().intValue())
 				digitalAsset = currentDigitalAsset;
 		}
 		return digitalAsset;
 	}
 
 
 	/**
 	 * Returns the latest digital asset for a contentversion.
 	 */
 	
 	private static DigitalAsset getLatestDigitalAsset(ContentVersion contentVersion, String assetKey)
 	{
 		Collection digitalAssets = contentVersion.getDigitalAssets();
 		Iterator iterator = digitalAssets.iterator();
 		
 		DigitalAsset digitalAsset = null;
 		while(iterator.hasNext())
 		{
 			DigitalAsset currentDigitalAsset = (DigitalAsset)iterator.next();	
 			if((digitalAsset == null || currentDigitalAsset.getDigitalAssetId().intValue() > digitalAsset.getDigitalAssetId().intValue()) && currentDigitalAsset.getAssetKey().equalsIgnoreCase(assetKey))
 				digitalAsset = currentDigitalAsset;
 		}
 		return digitalAsset;
 	}
 
 	/**
 	 * Returns the latest digital asset for a contentversion.
 	 */
 	
 	public static DigitalAssetVO getLatestDigitalAssetVO(Integer contentVersionId, Database db) throws Exception
 	{
 		String key = "" + contentVersionId;
 		String cacheName = "digitalAssetCache";
 		DigitalAssetVO digitalAssetVO = (DigitalAssetVO)CacheController.getCachedObject(cacheName, key);
 		if(digitalAssetVO != null)
 		{
 			if(logger.isInfoEnabled())
 				logger.info("There was an cached digitalAssetVO:" + digitalAssetVO);
 			
 			return digitalAssetVO;
 		}
 
 		if(logger.isInfoEnabled())
 			logger.info("Making a sql call for assets on " + contentVersionId);
 
 		OQLQuery oql = db.getOQLQuery("CALL SQL SELECT c.digitalAssetId, c.assetFileName, c.assetKey, c.assetFilePath, c.assetContentType, c.assetFileSize FROM cmDigitalAsset c, cmContentVersionDigitalAsset cvda where cvda.digitalAssetId = c.digitalAssetId AND cvda.contentVersionId = $1 ORDER BY c.digitalAssetId AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl");
     	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
     		oql = db.getOQLQuery("CALL SQL SELECT c.DigAssetId, c.assetFileName, c.assetKey, c.assetFilePath, c.assetContentType, c.assetFileSize FROM cmDigAsset c, cmContVerDigAsset cvda where cvda.DigAssetId = c.DigAssetId AND cvda.ContVerId = $1 ORDER BY c.DigAssetId AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl");
 
     	oql.bind(contentVersionId);
     	
     	QueryResults results = oql.execute(Database.ReadOnly);
 		
 		if(results.hasMore()) 
         {
         	SmallDigitalAssetImpl digitalAsset = (SmallDigitalAssetImpl)results.next();
         	digitalAssetVO = digitalAsset.getValueObject();
         }
 		
 		results.close();
 		oql.close();
 
 		if(digitalAssetVO != null)
 			CacheController.cacheObject(cacheName, key, digitalAssetVO);
 			
 		return digitalAssetVO;
 	}
 
 	/**
 	 * Returns the latest digital asset for a contentversion.
 	 */
 	
 	public static DigitalAssetVO getLatestDigitalAssetVO(Integer contentVersionId, String assetKey, Database db) throws Exception
 	{
 		String key = "" + contentVersionId + "_" + assetKey;
 		String cacheName = "digitalAssetCache";
 		DigitalAssetVO digitalAssetVO = (DigitalAssetVO)CacheController.getCachedObject(cacheName, key);
 		if(digitalAssetVO != null)
 		{
 			if(logger.isInfoEnabled())
 				logger.info("There was an cached digitalAssetVO:" + digitalAssetVO);
 
 			return digitalAssetVO;
 		}
 		
 		if(logger.isInfoEnabled())
 			logger.info("Making an sql call for asset with key " + assetKey + " on contentVersionId " + contentVersionId);
 
 		OQLQuery oql = db.getOQLQuery("CALL SQL SELECT c.digitalAssetId, c.assetFileName, c.assetKey, c.assetFilePath, c.assetContentType, c.assetFileSize FROM cmDigitalAsset c, cmContentVersionDigitalAsset cvda where cvda.digitalAssetId = c.digitalAssetId AND cvda.contentVersionId = $1 AND c.assetKey = $2 ORDER BY c.digitalAssetId AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl");
     	if(CmsPropertyHandler.getUseShortTableNames() != null && CmsPropertyHandler.getUseShortTableNames().equalsIgnoreCase("true"))
     		oql = db.getOQLQuery("CALL SQL SELECT c.DigAssetId, c.assetFileName, c.assetKey, c.assetFilePath, c.assetContentType, c.assetFileSize FROM cmDigAsset c, cmContVerDigAsset cvda where cvda.DigAssetId = c.DigAssetId AND cvda.ContVerId = $1 AND c.assetKey = $2 ORDER BY c.DigAssetId AS org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl");
 
 		oql.bind(contentVersionId);
     	oql.bind(assetKey);
     	
     	QueryResults results = oql.execute(Database.ReadOnly);
 		
 		if(results.hasMore()) 
         {
         	SmallDigitalAssetImpl digitalAsset = (SmallDigitalAssetImpl)results.next();
         	digitalAssetVO = digitalAsset.getValueObject();
         }
 		
 		results.close();
 		oql.close();
 
 		if(digitalAssetVO != null)
 			CacheController.cacheObject(cacheName, key, digitalAssetVO);
 		
 		return digitalAssetVO;
 	}
 	
 	
 	/**
 	 * This method checks if the given file exists on disk. If it does it's ignored because
 	 * that means that the file is allready cached on the server. If not we take out the stream from the 
 	 * digitalAsset-object and dumps it.
 	 */
    	
 	public static boolean dumpDigitalAsset(DigitalAssetVO digitalAssetVO, String fileName, String filePath, Database db) throws Exception
 	{
 		File outputFile = new File(filePath + File.separator + fileName);
 		File tmpOutputFile = new File(filePath + File.separator + "tmp_" + fileName);
 		if(outputFile.exists())
 		{
 			if(logger.isInfoEnabled())
 				logger.info("The file allready exists so we don't need to dump it again..");
 			
 			return true;
 		}
 
 		try
 		{
 			long timer = System.currentTimeMillis();
 	
 			File outputFileDir = new File(filePath);
 			outputFileDir.mkdirs();
 			
 			DigitalAsset digitalAsset = getDigitalAssetWithId(digitalAssetVO.getDigitalAssetId(), db);
 			
 			if((CmsPropertyHandler.getEnableDiskAssets().equals("false") || !tmpOutputFile.exists()) && digitalAsset.getAssetBlob() != null)
 			{
 				logger.info("Dumping from blob.");
 
 				FileOutputStream fis = new FileOutputStream(outputFile);
 				BufferedOutputStream bos = new BufferedOutputStream(fis);
 				
 				BufferedInputStream bis = new BufferedInputStream(digitalAsset.getAssetBlob());
 		
 				int character;
 				while ((character = bis.read()) != -1)
 				{
 					bos.write(character);
 				}
 				bos.flush();
 				
 				bis.close();
 				fis.close();
 				bos.close();
 			}
 			else
 			{
 				if(logger.isInfoEnabled())
 				{
 					logger.info("Dumping from file - diskassets is on probably.");
 					logger.info("Inside the cms-app I think - we should take the file from disk");
 					logger.info("tmpOutputFile:" + tmpOutputFile.getAbsolutePath() + ":" + tmpOutputFile.exists());
 					logger.info("outputFile:" + outputFile.getAbsolutePath() + ":" + outputFile.exists());
 				}
 				
 				if(tmpOutputFile.exists())
 				{
 					//new DigitalAssetController().copyInputStream(new FileInputStream(tmpOutputFile), new BufferedOutputStream(new FileOutputStream(outputFile)));
 	
 					logger.info("\n\nExists" + tmpOutputFile.getAbsolutePath() + "=" + tmpOutputFile.exists() + " OR " + outputFile.exists() + ":" + outputFile.length());
 					if(tmpOutputFile.length() == 0 || outputFile.exists())
 					{
 						logger.info("outputFile:" + outputFile.getAbsolutePath());	
 						logger.info("written file:" + tmpOutputFile.length() + " - removing temp and not renaming it...");	
 						tmpOutputFile.delete();
 					}
 					else
 					{
 						logger.info("written file:" + tmpOutputFile.getAbsolutePath() + " - renaming it to " + outputFile.getAbsolutePath());	
 						tmpOutputFile.renameTo(outputFile);
 						logger.info("Renamed to" + outputFile.getAbsolutePath() + "=" + outputFile.exists());
 					}
 				}				
 			}
 			
 			if(logger.isInfoEnabled())
 			{
 				logger.info("Time for dumping file " + fileName + ":" + (System.currentTimeMillis() - timer));
 				logger.info("assetPath in dump:" + filePath + File.separator + fileName);
 				logger.info("Time for dumping file " + fileName + ":" + (System.currentTimeMillis() - timer));
 			}
 		}
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 		
 		return outputFile.exists();
 	}
 	
 	
 	/**
 	 * This method checks if the given file exists on disk. If it does it's ignored because
 	 * that means that the file is allready cached on the server. If not we take out the stream from the 
 	 * digitalAsset-object and dumps it.
 	 */
    	
 	public static boolean dumpDigitalAsset(DigitalAsset digitalAsset, String fileName, String filePath) throws Exception
 	{
 		if(digitalAsset.getAssetFileSize().intValue() == -1)
 		{
 			return false;
 		}
 		
 		long timer = System.currentTimeMillis();
 
 		File outputFile = new File(filePath + File.separator + fileName);
 		if(outputFile.exists())
 		{
 			if(logger.isInfoEnabled())
 				logger.info("The file allready exists so we don't need to dump it again..");
 		
 			return true;
 		}
 		
 		if(digitalAsset.getAssetBlob() != null)
 		{
 			new File(filePath).mkdirs();
 
 			FileOutputStream fis = new FileOutputStream(outputFile);
 			BufferedOutputStream bos = new BufferedOutputStream(fis);
 			
 			BufferedInputStream bis = new BufferedInputStream(digitalAsset.getAssetBlob());
 			
 			int character;
 			while ((character = bis.read()) != -1)
 			{
 				bos.write(character);
 			}
 			bos.flush();
 			
 			bis.close();
 			fis.close();
 			bos.close();
 			
 			if(logger.isInfoEnabled())
 				logger.info("Time for dumping file " + fileName + ":" + (System.currentTimeMillis() - timer));
 
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/**
 	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
 	 * is handling.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return new DigitalAssetVO();
 	}
 
 	/**
 	 * This method archives selected assets and puts them into a zip-file which is returned as a url.
 	 * @param digitalAssetIdStrings
 	 * @return
 	 * @throws SystemException
 	 */
 	
 	public String archiveDigitalAssets(String[] digitalAssetIdStrings, StringBuffer archiveFileSize, boolean nullBlob) throws SystemException 
 	{
     	Database db = CastorDatabaseService.getDatabase();
 
     	String assetUrl = null;
     	List assetIdList = new ArrayList();
     	
         beginTransaction(db);
 
         try
         {
         	String filePath = CmsPropertyHandler.getDigitalAssetPath();
         	String archiveFileName = "assetArchive" + new VisualFormatter().formatDate(new Date(), "yyyy-MM-dd_HH-mm") + ".zip";
 			File outputFile = new File(filePath + File.separator + archiveFileName);
 
         	String[] filenames = new String[digitalAssetIdStrings.length];
         	Map names = new HashMap();
         	
         	for(int i = 0; i < digitalAssetIdStrings.length; i++)
         	{
         		Integer digitalAssetId = new Integer(digitalAssetIdStrings[i]);
         		DigitalAsset digitalAsset = getDigitalAssetWithId(digitalAssetId, db);
         		
 				String folderName = "" + (digitalAsset.getDigitalAssetId().intValue() / 1000);
 				logger.info("folderName:" + folderName);
 
 				String fileName = digitalAsset.getDigitalAssetId() + "_" + digitalAsset.getAssetFileName();
 				if(!outputFile.exists() || outputFile.length() == digitalAsset.getAssetFileSize().intValue())
 				{
 					dumpDigitalAsset(digitalAsset.getValueObject(), fileName, filePath + File.separator + folderName, db);
 				}
 
				filenames[i] = 	"" + filePath + File.separator + folderName + File.separator + fileName;	
 				names.put(filenames[i], fileName);
 				
 				digitalAsset.setAssetFilePath("IG_ARCHIVE:" + archiveFileName + ":" + fileName);
 				digitalAsset.setAssetFileSize(new Integer(-1));
 				
 		        if(nullBlob)
 		        	digitalAsset.setAssetBlob(null);
 		        else
 		        	digitalAsset.setAssetBlob(new ByteArrayInputStream("archived".getBytes()));
 		        
 				assetIdList.add(digitalAsset.getId());
         	}
         	
             // Create a buffer for reading the files
             byte[] buf = new byte[1024];
             
             try 
             {
             	// Create the ZIP file				
                 ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
             
                 // Compress the files
                 for (int i=0; i<filenames.length; i++) 
                 {
                     FileInputStream in = new FileInputStream(filenames[i]);
              
                     // Add ZIP entry to output stream.
                     String fileName = filenames[i];
                     String fileShortName = (String)names.get(fileName);
                     
                     out.putNextEntry(new ZipEntry(fileShortName));
             
                     // Transfer bytes from the file to the ZIP file
                     int len;
                     while ((len = in.read(buf)) > 0) 
                     {
                         out.write(buf, 0, len);
                     }
             
                     // Complete the entry
                     out.closeEntry();
                     in.close();
                 }
             
                 // Complete the ZIP file
                 out.close();
 
                 archiveFileSize.append(outputFile.length() / (1000 * 1000));
         		
     			assetUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + archiveFileName;
             } 
             catch (IOException e) 
             {
             	e.printStackTrace();
             }
 
             commitTransaction(db);
         }
         catch(Exception e)
         {
             logger.error("An error occurred when we tried to archive the digitalAssets:" + e, e);
             rollbackTransaction(db);
             throw new SystemException(e.getMessage());
         }
 
 		return assetUrl;
 	}
 	
 	/**
 	 * This method restores digital assets from a zip into the database again.
 	 * @param archiveFile
 	 * @return
 	 * @throws SystemException
 	 */
 	
 	public synchronized void restoreAssetArchive(File archiveFile) throws SystemException 
 	{
         try
         {
         	String tempAssetsPath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + "temp_" + archiveFile.hashCode();
         	File tempAssetsFile = new File(tempAssetsPath);
         	if(tempAssetsFile.exists())
         		tempAssetsFile.delete();
         	
         	tempAssetsFile.mkdir();
 
         	unzipFile(archiveFile, tempAssetsPath);
         	
         	String[] fileNames = tempAssetsFile.list();
         	
         	for (int i = 0; i < fileNames.length; i++) 
             {
         		Database db = CastorDatabaseService.getDatabase();
 
                 beginTransaction(db);
                 
                 try
                 {
 	        		String zipEntryName = fileNames[i];
 	               File assetFile = new File(tempAssetsPath + File.separator + zipEntryName);
 	        		
 	        		FileInputStream is = new FileInputStream(assetFile);
 	        		String assetId = zipEntryName.substring(0, zipEntryName.indexOf("_"));
 	        		
 	        		DigitalAsset digitalAsset = getDigitalAssetWithId(new Integer(assetId), db);
 	        		
 	        		digitalAsset.setAssetFilePath(zipEntryName);
 	    			digitalAsset.setAssetFileSize(new Integer((int)assetFile.length()));
 	    			digitalAsset.setAssetBlob(is);
 
 	                commitTransaction(db);
 	                
 	            	is.close();
                 }
                 catch(Exception e)
                 {
                 	rollbackTransaction(db);
                     logger.error("An error occurred when we tried to cache and show the digital asset thumbnail:" + e);
                     throw new SystemException(e.getMessage());
                 }
             }     
         	
         	if(tempAssetsFile.exists())
         		tempAssetsFile.delete();
         }
         catch(Exception e)
         {
         	e.printStackTrace();
             logger.error("An error occurred when we tried to cache and show the digital asset thumbnail:" + e);
             throw new SystemException(e.getMessage());
         }
 	}
 	
 	/**
 	 * This method unzips the cms war-file.
 	 */
 	
 	protected void unzipFile(File file, String targetFolder) throws Exception
 	{
     	Enumeration entries;
     	
     	ZipFile zipFile = new ZipFile(file);
     	
       	entries = zipFile.entries();
 
       	while(entries.hasMoreElements()) 
       	{
         	ZipEntry entry = (ZipEntry)entries.nextElement();
 
 	        if(entry.isDirectory()) 
 	        {
 	          	(new File(targetFolder + File.separator + entry.getName())).mkdir();
 	          	continue;
 	        }
 	
 	        //System.err.println("Extracting file: " + this.cmsTargetFolder + File.separator + entry.getName());
 	        copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(targetFolder + File.separator + entry.getName())));
 	    }
 	
 	    zipFile.close();
 	}
 	
 
 	/**
 	 * Just copies the files...
 	 */
 	
 	protected void copyInputStream(InputStream in, OutputStream out) throws IOException
 	{
 	    byte[] buffer = new byte[1024];
     	int len;
 
     	while((len = in.read(buffer)) >= 0)
       		out.write(buffer, 0, len);
 
     	in.close();
     	out.close();    	
   	}
 
 }
 
 class FilenameFilterImpl implements FilenameFilter 
 {
 	private String filter = ".";
 	
 	public FilenameFilterImpl(String aFilter)
 	{
 		filter = aFilter;
 	}
 	
 	public boolean accept(File dir, String name) 
 	{
     	return name.startsWith(filter);
 	}
 };
