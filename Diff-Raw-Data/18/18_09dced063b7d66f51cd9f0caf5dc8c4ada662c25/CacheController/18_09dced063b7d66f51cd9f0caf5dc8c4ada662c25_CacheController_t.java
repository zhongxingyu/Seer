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
 
 package org.infoglue.deliver.util;
 
 //import org.exolab.castor.jdo.CacheManager;
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.CacheManager;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.applications.common.Session;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.controllers.kernel.impl.simple.CmsJDOCallback;
 import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
 import org.infoglue.cms.entities.content.impl.simple.*;
 import org.infoglue.cms.entities.structure.impl.simple.*;
 import org.infoglue.cms.entities.publishing.impl.simple.*;
 import org.infoglue.cms.entities.management.impl.simple.*;
 import org.infoglue.cms.entities.workflow.impl.simple.*;
 
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BaseDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 
 import com.opensymphony.oscache.base.CacheEntry;
 import com.opensymphony.oscache.base.NeedsRefreshException;
 import com.opensymphony.oscache.base.events.CacheEntryEventListener;
 import com.opensymphony.oscache.base.events.CacheMapAccessEventListener;
 import com.opensymphony.oscache.extra.CacheEntryEventListenerImpl;
 import com.opensymphony.oscache.extra.CacheMapAccessEventListenerImpl;
 import com.opensymphony.oscache.general.GeneralCacheAdministrator;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpSession;
 
 
 public class CacheController extends Thread
 { 
     private final static Logger logger = Logger.getLogger(CacheController.class.getName());
 
     public static List notifications = new ArrayList();
     
     private static Map eventListeners = new HashMap();
 	private static Map caches = new HashMap();
 	private boolean expireCacheAutomatically = false;
 	private int cacheExpireInterval = 1800000;
 	private boolean continueRunning = true;
 	
 	private static GeneralCacheAdministrator generalCache = new GeneralCacheAdministrator();
 	
     public static Date expireDateTime = null;
     public static Date publishDateTime = null;
 
 	public CacheController()
 	{
 		super();
 	}
 
 	public void setCacheExpireInterval(int cacheExpireInterval)
 	{
 		this.cacheExpireInterval = cacheExpireInterval;
 	}
 	
 	public static void cacheObject(String cacheName, Object key, Object value)
 	{
 		if(!caches.containsKey(cacheName))
 		    caches.put(cacheName, new HashMap());
 			
 		Map cacheInstance = (Map)caches.get(cacheName);
 		cacheInstance.put(key, value);
 	}	
 	
 	public static Object getCachedObject(String cacheName, Object key)
 	{
         Map cacheInstance = (Map)caches.get(cacheName);
         return (cacheInstance == null) ? null : cacheInstance.get(key);
     }
 
 	public static void cacheObjectInAdvancedCache(String cacheName, Object key, Object value, String[] groups)
 	{
 	    //cacheObject(cacheName, key, value);
 	    
 	    if(!caches.containsKey(cacheName))
 	    {
 	        GeneralCacheAdministrator cacheAdministrator = new GeneralCacheAdministrator();
 
 	        CacheEntryEventListenerImpl cacheEntryEventListener = new ExtendedCacheEntryEventListenerImpl();
 	        CacheMapAccessEventListenerImpl cacheMapAccessEventListener = new CacheMapAccessEventListenerImpl(); 
 	        
 	        cacheAdministrator.getCache().addCacheEventListener(cacheEntryEventListener, CacheEntryEventListener.class);
 	        cacheAdministrator.getCache().addCacheEventListener(cacheMapAccessEventListener, CacheMapAccessEventListener.class);
 	        caches.put(cacheName, cacheAdministrator);
 	        eventListeners.put(cacheName + "_cacheEntryEventListener", cacheEntryEventListener);
 	        eventListeners.put(cacheName + "_cacheMapAccessEventListener", cacheMapAccessEventListener);
 	    }
 	        
 	    logger.info("Putting " + cacheName + " with key: " + key + " in relation to:");
 	    for(int i=0; i<groups.length; i++)
 	    {
 	        logger.info("group:" + groups[i]);
 	    }
 	    
 		GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
 		cacheAdministrator.putInCache(key.toString(), value, groups);
 		
 		logger.info("Done cacheObjectInAdvancedCache");
 	}	
 	
 	public static Object getCachedObjectFromAdvancedCache(String cacheName, Object key)
 	{
 	    //return getCachedObject(cacheName, key);
 	    Object value = null;
 	    
 	    GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
 	    try 
 	    {
 	        value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache(key.toString(), CacheEntry.INDEFINITE_EXPIRY);
 		} 
 	    catch (NeedsRefreshException nre) 
 	    {
 	        cacheAdministrator.cancelUpdate(key.toString());
 		}
 		 
 		return value;
 	}
 
 	public static Object getCachedObjectFromAdvancedCache(String cacheName, Object key, int updateInterval)
 	{
 	    //return getCachedObject(cacheName, key);
 	    Object value = null;
 	    
 	    GeneralCacheAdministrator cacheAdministrator = (GeneralCacheAdministrator)caches.get(cacheName);
 	    try 
 	    {
 	        value = (cacheAdministrator == null) ? null : cacheAdministrator.getFromCache(key.toString(), updateInterval);
 		} 
 	    catch (NeedsRefreshException nre) 
 	    {
 	        cacheAdministrator.cancelUpdate(key.toString());
 		}
 		 
 		return value;
 	}
 
 	public static void clearCache(String cacheName)
 	{
 		logger.info("Clearing the cache called " + cacheName);
 		if(caches.containsKey(cacheName))
 		{
 		    Object object = caches.get(cacheName);
 		    if(object instanceof Map)
 			{
 				Map cacheInstance = (Map)object;
 				cacheInstance.clear();
 			}
 			else
 			{
 			    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)object;
 		    	cacheInstance.flushAll();
 			}
 	    	caches.remove(cacheName);
 		    eventListeners.remove(cacheName + "_cacheEntryEventListener");
 		    eventListeners.remove(cacheName + "_cacheMapAccessEventListener");
 		}
 	}
 		
 	public static void clearCaches(String entity, String entityId)
 	{
 		if(entity == null)
 		{	
 			logger.info("Clearing the caches");
 			logger.info("caches.entrySet().size:" + caches.entrySet().size());
 			for (Iterator i = caches.entrySet().iterator(); i.hasNext(); ) 
 			{
 				Map.Entry e = (Map.Entry) i.next();
 				logger.info("e:" + e.getKey());
 				Object object = e.getValue();
 				if(object instanceof Map)
 				{
 					Map cacheInstance = (Map)e.getValue();
 					cacheInstance.clear();
 				}
 				else
 				{
 				    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)e.getValue();
 			    	cacheInstance.flushAll();
 			        eventListeners.clear();
 				}
 		    	i.remove();
 			}
 		}
 		else
 		{
 			logger.info("Clearing some caches");
 			logger.info("entity:" + entity);
 			for (Iterator i = caches.entrySet().iterator(); i.hasNext(); ) 
 			{
 				Map.Entry e = (Map.Entry) i.next();
 				logger.info("e:" + e.getKey());
 				boolean clear = false;
 				boolean selectiveCacheUpdate = false;
 				String cacheName = e.getKey().toString();
 				
 				if(cacheName.equalsIgnoreCase("serviceDefinitionCache") && entity.indexOf("ServiceBinding") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("qualifyerListCache") && (entity.indexOf("Qualifyer") > 0 || entity.indexOf("ServiceBinding") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("availableServiceBindingCache") && entity.indexOf("AvailableServiceBinding") > 0)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("languageCache") && entity.indexOf("Language") > 0)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("latestSiteNodeVersionCache") && entity.indexOf("SiteNode") > 0)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("parentSiteNodeCache") && entity.indexOf("SiteNode") > 0)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("NavigationCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("pagePathCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("componentEditorCache") && (entity.indexOf("SiteNode") > 0 || entity.indexOf("Content") > 0))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("masterLanguageCache") && (entity.indexOf("Repository") > 0 || entity.indexOf("Language") > 0))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("parentRepository") && entity.indexOf("Repository") > 0)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("contentAttributeCache") && entity.indexOf("ContentVersion") > -1)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("contentVersionCache") && entity.indexOf("Content") > -1)
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("boundSiteNodeCache") && (entity.indexOf("ServiceBinding") > 0 || entity.indexOf("Qualifyer") > 0 || entity.indexOf("SiteNodeVersion") > 0 || entity.indexOf("SiteNodeVersion") > 0 || entity.indexOf("SiteNode") > 0 || entity.indexOf("AccessRight") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("boundContentCache") && (entity.indexOf("ServiceBinding") > 0 || entity.indexOf("Qualifyer") > 0 || entity.indexOf("SiteNodeVersion") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("Content") > 0 || entity.indexOf("AccessRight") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("pageCache") && entity.indexOf("Registry") == -1)
 				{	
 					clear = true;
 					selectiveCacheUpdate = true;
 				}
 				if(cacheName.equalsIgnoreCase("componentCache") && entity.indexOf("Registry") == -1)
 				{	
 					clear = true;
 					selectiveCacheUpdate = true;
 				}
 				if(cacheName.equalsIgnoreCase("includeCache"))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("authorizationCache") && entity.indexOf("AccessRight") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("userCache") && (entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
 				{
 					clear = true;
 				}
 				if((cacheName.equalsIgnoreCase("assetUrlCache") || cacheName.equalsIgnoreCase("assetThumbnailUrlCache")) && (entity.indexOf("DigitalAsset") > 0 || entity.indexOf("ContentVersion") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("sortedChildContentsCache") && (entity.indexOf("Content") > 0 || entity.indexOf("ContentVersion") > 0 || entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0  || entity.indexOf("Group") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("workflowCache") && entity.indexOf("WorkflowDefinition") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("rootSiteNodeCache") && entity.indexOf("SiteNode") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("groupListCache") && entity.indexOf("Group") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("roleListCache") && entity.indexOf("Role") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("groupPropertiesCache") && entity.indexOf("Group") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("rolePropertiesCache") && entity.indexOf("Role") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("relatedCategoriesCache") && (entity.indexOf("Group") > 0 || entity.indexOf("Role") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("redirectCache") && entity.indexOf("Redirect") > 0)
 				{
 					clear = true;
 				}
 				
 				
 				if(clear)
 				{	
 					logger.info("clearing:" + e.getKey());
 					Object object = e.getValue();
 					if(object instanceof Map)
 					{
 						Map cacheInstance = (Map)e.getValue();
 						cacheInstance.clear();
 					}
 					else
 					{
 					    String useSelectivePageCacheUpdateString = CmsPropertyHandler.getProperty("useSelectivePageCacheUpdate");
 					    boolean useSelectivePageCacheUpdate = false;
 					    if(useSelectivePageCacheUpdateString != null && useSelectivePageCacheUpdateString.equalsIgnoreCase("true"))
 					        useSelectivePageCacheUpdate = true;
 					        
 					    GeneralCacheAdministrator cacheInstance = (GeneralCacheAdministrator)e.getValue();
 
 					    if(selectiveCacheUpdate && entity.indexOf("SiteNode") > 0)
 					    {
 					    	cacheInstance.flushAll();
 					    	eventListeners.remove(cacheName + "_cacheEntryEventListener");
 						    eventListeners.remove(cacheName + "_cacheMapAccessEventListener");
 					    	logger.info("clearing:" + e.getKey());
 					    }
 					    else if(selectiveCacheUpdate && entity.indexOf("ContentVersion") > 0 && useSelectivePageCacheUpdate)
 					    {
 					    	cacheInstance.flushGroup("contentVersion_" + entityId);
 					    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
 					    	logger.info("clearing " + e.getKey() + " with group " + "contentVersion_" + entityId);
 					    }
 					    else if(selectiveCacheUpdate && entity.indexOf("Content") > 0 && useSelectivePageCacheUpdate)
 					    {
 					    	cacheInstance.flushGroup("content_" + entityId);
 					    	cacheInstance.flushGroup("selectiveCacheUpdateNonApplicable");
 					    	logger.info("clearing " + e.getKey() + " with group " + "content_" + entityId);
 					    }
 					    else
 					    {
 							cacheInstance.flushAll();
 					    	eventListeners.remove(cacheName + "_cacheEntryEventListener");
 						    eventListeners.remove(cacheName + "_cacheMapAccessEventListener");
 							logger.info("clearing:" + e.getKey());
 					    }
 					}
 
 					i.remove();
 
 				}
 				else
 				{
 					logger.info("Did not clear " + e.getKey());
 				}
 			}
 		}
 	}
 	
 	public static void clearCastorCaches()
 	{
 		logger.info("Emptying the Castor Caches");
 		
 		try
 		{
 			clearCache(SmallContentImpl.class);
 			clearCache(MediumContentImpl.class);
 			clearCache(ContentImpl.class);
 			clearCache(ContentRelationImpl.class);
 			clearCache(ContentVersionImpl.class);
 			clearCache(DigitalAssetImpl.class);
 			clearCache(SmallAvailableServiceBindingImpl.class);
 			clearCache(AvailableServiceBindingImpl.class);
 			clearCache(ContentTypeDefinitionImpl.class);
 			clearCache(LanguageImpl.class);
 			clearCache(RepositoryImpl.class);
 			clearCache(RepositoryLanguageImpl.class);
 			clearCache(RoleImpl.class);
 			clearCache(GroupImpl.class);
 			clearCache(ServiceDefinitionImpl.class);
 			clearCache(SiteNodeTypeDefinitionImpl.class);
 			clearCache(SystemUserImpl.class);
 			clearCache(QualifyerImpl.class);
 			clearCache(ServiceBindingImpl.class);
 			clearCache(SmallSiteNodeImpl.class);
 			clearCache(SiteNodeImpl.class);
 			clearCache(SiteNodeVersionImpl.class);
 			clearCache(PublicationImpl.class);
 			//clearCache(PublicationDetailImpl.class); // This class depends on publication
 			clearCache(ActionImpl.class);
 			clearCache(ActionDefinitionImpl.class);
 			clearCache(ActorImpl.class);
 			clearCache(ConsequenceImpl.class);
 			clearCache(ConsequenceDefinitionImpl.class);
 			clearCache(EventImpl.class);
 			clearCache(WorkflowImpl.class);
 			clearCache(WorkflowDefinitionImpl.class);
 			clearCache(CategoryImpl.class);
 			clearCache(ContentCategoryImpl.class);
 			clearCache(RegistryImpl.class);
 			clearCache(RedirectImpl.class);
 			
 			clearCache(InterceptionPointImpl.class);
 			clearCache(InterceptorImpl.class);
 			clearCache(AccessRightImpl.class);
 	
 			clearCache(RolePropertiesImpl.class);
 			clearCache(UserPropertiesImpl.class);
 			clearCache(GroupPropertiesImpl.class);
 			clearCache(UserContentTypeDefinitionImpl.class);
 			clearCache(RoleContentTypeDefinitionImpl.class);
 			clearCache(GroupContentTypeDefinitionImpl.class);			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public static synchronized void clearCache(Class type, Object[] ids) throws Exception
 	{
 		Database db = CastorDatabaseService.getDatabase();
 
 		try
 		{
 		    CacheManager manager = db.getCacheManager();
 		    manager.expireCache(type, ids);
 		    //Class[] types = {type};
 		    //db.expireCache(types, ids);
 		    
 		    if(type.getName().equalsIgnoreCase(SmallContentImpl.class.getName()) || 
 		       type.getName().equalsIgnoreCase(MediumContentImpl.class.getName()) ||
 		       type.getName().equalsIgnoreCase(ContentImpl.class.getName()) ||
 		       type.getName().equalsIgnoreCase(SmallSiteNodeImpl.class.getName()) || 
 			   type.getName().equalsIgnoreCase(SiteNodeImpl.class.getName()))
 		    {
 		        expireDateTime = null;
 		        publishDateTime = null;
 		    }
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 		    db.close();			
 		}
 	}
 	
 	private static synchronized void clearCache(Class c) throws Exception
 	{
 	    Database db = CastorDatabaseService.getDatabase();
 
 		try
 		{
 			Class[] types = {c};
 			Class[] ids = {null};
 			CacheManager manager = db.getCacheManager();
 			manager.expireCache(types);
 			//db.expireCache(types, null);
 			
 		    if(c.getName().equalsIgnoreCase(SmallContentImpl.class.getName()) || 
 		       c.getName().equalsIgnoreCase(MediumContentImpl.class.getName()) ||
 		       c.getName().equalsIgnoreCase(ContentImpl.class.getName()) ||
 		       c.getName().equalsIgnoreCase(SmallSiteNodeImpl.class.getName()) || 
 			   c.getName().equalsIgnoreCase(SiteNodeImpl.class.getName()))
 		    {
 		        expireDateTime = null;
 		        publishDateTime = null;
 		    }
 
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			db.close();			
 		}
 	}
 	
 	
 	public void run() 
 	{
 		while(this.continueRunning && expireCacheAutomatically)
 		{
 			logger.warn("Clearing caches");
 			clearCastorCaches();
 			logger.info("Castor cache cleared");
 			clearCaches(null, null);
 			logger.info("All other caches cleared");
 			
 			try
 			{
 				sleep(cacheExpireInterval);
 			} 
 			catch (InterruptedException e){}
 		}
 	}
 
 	public void stopThread()
 	{
 		this.continueRunning = false;
 	}
 
 	public boolean getExpireCacheAutomatically() 
 	{
 		return expireCacheAutomatically;
 	}
 
 	public void setExpireCacheAutomatically(boolean expireCacheAutomatically) 
 	{
 		this.expireCacheAutomatically = expireCacheAutomatically;
 	}
 	
     public static Map getCaches()
     {
         return caches;
     }
     
     public static Map getEventListeners()
     {
         return eventListeners;
     }
 
     public static GeneralCacheAdministrator getGeneralCache()
     {
         return generalCache;
     }
     
     public static List getNotifications()
     {
         return notifications;
     }
     
     public static void evictWaitingCache() throws Exception
     {
         synchronized(notifications)
         {
 			Iterator i = notifications.iterator();
 			while(i.hasNext())
 			{
 			    CacheEvictionBean cacheEvictionBean = (CacheEvictionBean)i.next();
 			    String className = cacheEvictionBean.getClassName();
 			    String objectId = cacheEvictionBean.getObjectId();
 			    String objectName = cacheEvictionBean.getObjectName();
 				String typeId = cacheEvictionBean.getTypeId();
 				
 			    logger.info("className:" + className);
 				logger.info("objectId:" + objectId);
 
 				try
 			    {
 				    //Should contain permissioncontrol later...
 			
 				    boolean isDependsClass = false;
 				    if(className.equalsIgnoreCase(PublicationDetailImpl.class.getName()))
 				        isDependsClass = true;
 			
 				    CacheController.clearCaches(className, objectId);
 			
 				    logger.info("Updating className with id:" + className + ":" + objectId);
 					if(className != null)
 					{
 					    //Class[] types = {Class.forName(className)};
 					    Class type = Class.forName(className);
 					    
 					    if(!isDependsClass && className.equalsIgnoreCase(SystemUserImpl.class.getName()) || className.equalsIgnoreCase(RoleImpl.class.getName()) || className.equalsIgnoreCase(GroupImpl.class.getName()))
 					    {
 					        Object[] ids = {objectId};
 					        CacheController.clearCache(type, ids);
 						}
 					    else if(!isDependsClass)
 					    {
 					        Object[] ids = {new Integer(objectId)};
 						    CacheController.clearCache(type, ids);
 					    }
 					    
 						//If it's an contentVersion we should delete all images it might have generated from attributes.
 						/*
 						if(Class.forName(className).getName().equals(ContentVersionImpl.class.getName()))
 						{
 						    logger.info("We should delete all images with contentVersionId " + objectId);
 							DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteContentVersionAssets(new Integer(objectId));
 						}
 						else */if(Class.forName(className).getName().equals(ContentImpl.class.getName()))
 						{
 						    logger.info("We clear all small contents as well " + objectId);
 							Class typesExtra = SmallContentImpl.class;
 							Object[] idsExtra = {new Integer(objectId)};
 							CacheController.clearCache(typesExtra, idsExtra);
 			
 							logger.info("We clear all medium contents as well " + objectId);
 							Class typesExtraMedium = MediumContentImpl.class;
 							Object[] idsExtraMedium = {new Integer(objectId)};
 							CacheController.clearCache(typesExtraMedium, idsExtraMedium);
 						}
 						else if(Class.forName(className).getName().equals(AvailableServiceBindingImpl.class.getName()))
 						{
 						    Class typesExtra = SmallAvailableServiceBindingImpl.class;
 							Object[] idsExtra = {new Integer(objectId)};
 							CacheController.clearCache(typesExtra, idsExtra);
 						}
 						else if(Class.forName(className).getName().equals(SiteNodeImpl.class.getName()))
 						{
 						    Class typesExtra = SmallSiteNodeImpl.class;
 							Object[] idsExtra = {new Integer(objectId)};
 							CacheController.clearCache(typesExtra, idsExtra);
 						}
 						else if(Class.forName(className).getName().equals(DigitalAssetImpl.class.getName()))
 						{
 						    logger.info("We should delete all images with digitalAssetId " + objectId);
 							DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteDigitalAssets(new Integer(objectId));
 						}
 					}				    
 			    }
 			    catch(Exception e)
 			    {
 			        logger.warn("Cache eviction reported an error:" + e.getMessage(), e);
 			    }
 			    
 				i.remove();
 			}
         }
     }
 
     /**
      * Composer of the pageCacheKey.
      * 
      * @param siteNodeId
      * @param languageId
      * @param contentId
      * @param userAgent
      * @param queryString
      * @return
      */
     
     public static String getPageCacheKey(HttpSession session, TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId, String userAgent, String queryString, String extra)
     {
     	String pageKey = null;
     	String pageKeyProperty = CmsPropertyHandler.getProperty("pageKey");
     	if(pageKeyProperty != null && pageKeyProperty.length() > 0)
     	{    
     	    pageKey = pageKeyProperty;
     	    pageKey = pageKey.replaceAll("\\$siteNodeId", "" + siteNodeId);
     	    pageKey = pageKey.replaceAll("\\$languageId", "" + languageId);
     	    pageKey = pageKey.replaceAll("\\$contentId", "" + contentId);
     	    pageKey = pageKey.replaceAll("\\$useragent", "" + userAgent);
     	    pageKey = pageKey.replaceAll("\\$queryString", "" + queryString);
     	    
     	    int sessionAttributeStartIndex = pageKey.indexOf("$session.");
     	    while(sessionAttributeStartIndex > -1)
     	    {
         	    int sessionAttributeEndIndex = pageKey.indexOf("_", sessionAttributeStartIndex);
         	    String sessionAttribute = null;
         	    if(sessionAttributeEndIndex > -1)
         	        sessionAttribute = pageKey.substring(sessionAttributeStartIndex + 9, sessionAttributeEndIndex);
         	    else
         	        sessionAttribute = pageKey.substring(sessionAttributeStartIndex + 9);
 
         	    pageKey = pageKey.replaceAll("\\$session." + sessionAttribute, "" + session.getAttribute(sessionAttribute));    	    
     	    
        	    sessionAttributeStartIndex = pageKey.indexOf("$session.");
     	    }
     	    
     	    int cookieAttributeStartIndex = pageKey.indexOf("$cookie.");
     	    while(cookieAttributeStartIndex > -1)
     	    {
         	    int cookieAttributeEndIndex = pageKey.indexOf("_", cookieAttributeStartIndex);
         	    String cookieAttribute = null;
         	    if(cookieAttributeEndIndex > -1)
         	        cookieAttribute = pageKey.substring(cookieAttributeStartIndex + 8, cookieAttributeEndIndex);
         	    else
         	        cookieAttribute = pageKey.substring(cookieAttributeStartIndex + 8);
 
         	    pageKey = pageKey.replaceAll("\\$cookie." + cookieAttribute, "" + templateController.getCookie(cookieAttribute));    	    
     	    
        	    cookieAttributeStartIndex = pageKey.indexOf("$cookie.");
     	    }
 
     	}
     	else
     	    pageKey  = "" + siteNodeId + "_" + languageId + "_" + contentId + "_" + userAgent + "_" + queryString;
     	
     	return pageKey + extra;
     }
 }
