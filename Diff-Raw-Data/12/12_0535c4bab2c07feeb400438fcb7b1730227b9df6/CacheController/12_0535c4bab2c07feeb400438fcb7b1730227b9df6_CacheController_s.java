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
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.entities.content.impl.simple.*;
 import org.infoglue.cms.entities.structure.impl.simple.*;
 import org.infoglue.cms.entities.publishing.impl.simple.*;
 import org.infoglue.cms.entities.management.impl.simple.*;
 import org.infoglue.cms.entities.workflow.impl.simple.*;
 import org.infoglue.cms.util.CmsLogger;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BaseDeliveryController;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 
 
 public class CacheController extends Thread
 { 
 	private static Map cache = new HashMap();
 	private boolean expireCacheAutomatically = false;
 	private int cacheExpireInterval = 1800000;
 	private boolean continueRunning = true;
 	
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
 		if(!cache.containsKey(cacheName))
 			cache.put(cacheName, new HashMap());
 			
 		Map cacheInstance = (Map)cache.get(cacheName);
 		cacheInstance.put(key, value);
 	}	
 	
 	public static Object getCachedObject(String cacheName, Object key)
 	{
 		Map cacheInstance = (Map)cache.get(cacheName);
 		return (cacheInstance == null) ? null : cacheInstance.get(key);
 	}
 
 
 	public static void clearCache(String cacheName)
 	{
 		CmsLogger.logInfo("Clearing the cache called " + cacheName);
 		if(cache.containsKey(cacheName))
 		{
 			Map cacheInstance = (Map)cache.get(cacheName);
 			cacheInstance.clear();
 			//cache.remove(cacheName);
 		}
 	}
 		
 	public static void clearCaches(String entity)
 	{
 		if(entity == null)
 		{	
 			CmsLogger.logInfo("Clearing the caches");
 			CmsLogger.logInfo("cache.entrySet().size:" + cache.entrySet().size());
 			for (Iterator i = cache.entrySet().iterator(); i.hasNext(); ) 
 			{
 				Map.Entry e = (Map.Entry) i.next();
 				CmsLogger.logInfo("e:" + e.getKey());
 				Map cacheInstance = (Map)e.getValue();
 				cacheInstance.clear();
 			}
 		}
 		else
 		{
 			CmsLogger.logInfo("Clearing some caches");
 			CmsLogger.logInfo("entity:" + entity);
 			for (Iterator i = cache.entrySet().iterator(); i.hasNext(); ) 
 			{
 				Map.Entry e = (Map.Entry) i.next();
 				CmsLogger.logInfo("e:" + e.getKey());
 				boolean clear = false;
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
 				if(cacheName.equalsIgnoreCase("pageCache"))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("includeCache"))
 				{	
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("authorizationCache") && entity.indexOf("AccessRight") > 0)
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("userCache") && (entity.indexOf("AccessRight") > 0 || entity.indexOf("SystemUser") > 0 || entity.indexOf("Role") > 0))
 				{
 					clear = true;
 				}
 				if((cacheName.equalsIgnoreCase("assetUrlCache") || cacheName.equalsIgnoreCase("assetThumbnailUrlCache")) && (entity.indexOf("DigitalAsset") > 0 || entity.indexOf("ContentVersion") > 0))
 				{
 					clear = true;
 				}
 				if(cacheName.equalsIgnoreCase("sortedChildContentsCache") && (entity.indexOf("Content") > 0 || entity.indexOf("ContentVersion") > 0))
 				{
 					clear = true;
 				}
 				
 				if(clear)
 				{	
 					CmsLogger.logWarning("clearing:" + e.getKey());
 					Map cacheInstance = (Map)e.getValue();
 					cacheInstance.clear();
 				}
 				else
 				{
 					CmsLogger.logInfo("Did not clear " + e.getKey());
 				}
 			}
 		}
 	}
 	
 	public static void clearCastorCaches()
 	{
 		CmsLogger.logInfo("Emptying the Castor Caches");
 		
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
 			clearCache(CategoryImpl.class);
 			clearCache(ContentCategoryImpl.class);
 			
 			clearCache(InterceptionPointImpl.class);
 			clearCache(InterceptorImpl.class);
 			clearCache(AccessRightImpl.class);
 	
 			clearCache(RolePropertiesImpl.class);
 			clearCache(UserPropertiesImpl.class);
 			clearCache(UserContentTypeDefinitionImpl.class);
 			clearCache(RoleContentTypeDefinitionImpl.class);
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
 		    //CacheManager manager = db.getCacheManager();
 		    //manager.expireCache(type, ids);
 		    Class[] types = {type};
 		    db.expireCache(types, ids);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			BaseDeliveryController.closeDatabase(db);			
 		}
 	}
 	
 	private static synchronized void clearCache(Class c) throws Exception
 	{
 		Database db = CastorDatabaseService.getDatabase();
 
 		try
 		{
 			Class[] types = {c};
 			Class[] ids = {null};
 			//CacheManager manager = db.getCacheManager();
 			//manager.expireCache(types);
			db.expireCache(types, ids);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			BaseDeliveryController.closeDatabase(db);			
 		}
 	}
 	
 	
 	public void run() 
 	{
 		while(this.continueRunning && expireCacheAutomatically)
 		{
 			CmsLogger.logWarning("Clearing caches");
 			clearCastorCaches();
 			CmsLogger.logInfo("Castor cache cleared");
 			clearCaches(null);
 			CmsLogger.logInfo("All other caches cleared");
 			
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
 }
