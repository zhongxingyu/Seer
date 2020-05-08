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
  
 import org.exolab.castor.jdo.CacheManager;
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.persist.spi.CallbackInterceptor;
 import org.infoglue.cms.util.ChangeNotificationController;
 import org.infoglue.cms.util.NotificationMessage;
 
 import org.infoglue.cms.entities.management.AccessRightGroup;
 import org.infoglue.cms.entities.management.impl.simple.AccessRightGroupImpl;
 import org.infoglue.cms.entities.management.impl.simple.AccessRightImpl;
 import org.infoglue.cms.entities.management.impl.simple.AccessRightRoleImpl;
 import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
 import org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl;
 import org.infoglue.cms.entities.management.impl.simple.InterceptorImpl;
 import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
 import org.infoglue.cms.entities.management.impl.simple.RepositoryLanguageImpl;
 import org.infoglue.cms.entities.management.impl.simple.TransactionHistoryImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
 import org.infoglue.cms.entities.workflow.impl.simple.WorkflowDefinitionImpl;
 
 import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
 import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
 import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
 import org.infoglue.cms.entities.kernel.IBaseEntity;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.util.CmsLogger;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BaseDeliveryController;
 import org.infoglue.deliver.util.CacheController;
 
 
 /**
  * CMSJDOCallback.java
  * Created on 2002-okt-09 
  * @author Stefan Sik, ss@frovi.com 
  * ss
  * 
  */
 public class CmsJDOCallback implements CallbackInterceptor
 {
 
     public void using(Object object, Database db)
     {
     	//System.out.println("Using " + object);
         // ( (Persistent) object ).jdoPersistent( db );
     }
 
 
     public Class loaded(Object object, short accessMode) throws Exception
     {
 		//System.out.println("Loaded " + object.getClass().getName() + " accessMode:" + accessMode);
     	// return ( (Persistent) object ).jdoLoad(accessMode);
         return null;
     }
 
 
     public void storing(Object object, boolean modified) throws Exception
     {
 		//System.out.println("storing...:" + object + ":" + modified);
         // ( (Persistent) object ).jdoStore( modified );
    		
    		//CmsLogger.logInfo("Should we store -------------->" + object + ":" + modified);
     	if (TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1 && modified)
 	    {
 	        //System.out.println("Actually stored it:" + object + ":" + modified);
 	    	CmsLogger.logInfo("Actually stored it:" + object + ":" + modified);
     	    
 			String userName = "SYSTEM";
 	    	NotificationMessage notificationMessage = new NotificationMessage("CmsJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_UPDATE, getObjectIdentity(object), object.toString());
 			ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
 	    
 			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
 			{
 				CacheController.clearCache("repositoryCache");
 			}
 			else if(object.getClass().getName().equals(InterceptionPointImpl.class.getName()))
 			{
 				CacheController.clearCache("interceptionPointCache");
 				CacheController.clearCache("interceptorsCache");
 				CacheController.clearCache("authorizationCache");
 			}
 			else if(object.getClass().getName().equals(InterceptorImpl.class.getName()))
 			{
 				CacheController.clearCache("interceptionPointCache");
 				CacheController.clearCache("interceptorsCache");
 				CacheController.clearCache("authorizationCache");
 			}
 			else if(object.getClass().getName().equals(AccessRightImpl.class.getName()) || object.getClass().getName().equals(AccessRightRoleImpl.class.getName()) || object.getClass().getName().equals(AccessRightGroupImpl.class.getName()))
 			{
 				CacheController.clearCache("interceptionPointCache");
 				CacheController.clearCache("interceptorsCache");
 				CacheController.clearCache("authorizationCache");
 			}
 			else if(object.getClass().getName().equals(ContentTypeDefinitionImpl.class.getName()))
 			{
 				CacheController.clearCache("contentTypeDefinitionCache");
 			}
 			else if(object.getClass().getName().equals(ContentImpl.class.getName()))
 			{
 				CacheController.clearCache("childContentCache");
				CacheController.clearCache("componentContentsCache");
 				clearCache(SmallContentImpl.class);
 				clearCache(MediumContentImpl.class);
 			}
 			else if(object.getClass().getName().equals(ContentVersionImpl.class.getName()))
 			{
 				CacheController.clearCache("componentContentsCache");
 			}
 			else if(object.getClass().getName().equals(RepositoryLanguageImpl.class.getName()))
 			{
 				CacheController.clearCache("masterLanguageCache");
 				CacheController.clearCache("repositoryLanguageListCache");
 			}
 			else if(object.getClass().getName().equals(DigitalAssetImpl.class.getName()))
 			{
 				//System.out.println("We should delete all images with digitalAssetId " + getObjectIdentity(object));
 				DigitalAssetController.deleteCachedDigitalAssets((Integer)getObjectIdentity(object));
 			}
 			else if(object.getClass().getName().equals(WorkflowDefinitionImpl.class.getName()))
 			{
 				CacheController.clearCache("workflowCache");
 			}
     	}
     }
 
 	private synchronized void clearCache(Class c) throws Exception
 	{
 		Database db = CastorDatabaseService.getDatabase();
 
 		try
 		{
 		    Class[] types = {c};
 			Class[] ids = {null};
 			CacheManager manager = db.getCacheManager();
 			manager.expireCache(types);
 			//db.expireCache(types, null);
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
 
     public void creating( Object object, Database db )
         throws Exception
     {
         // ( (Persistent) object ).jdoBeforeCreate( db );
     }
 
 
     public void created(Object object) throws Exception
     {
         // ( (Persistent) object ).jdoAfterCreate();
 
 		// Write to trans-log
     	
     	//String className = object.getClass().getName();		
 		//if (CmsSystem.getTransactionHistoryEntityClassName().indexOf(className) == -1)
 		//	CmsSystem.transactionLogEntry("CMSJDOCallback:" + object.getClass().getName(), CmsSystem.TRANS_CREATE, getEntityId(object), object.toString());        
 		//System.out.println("created...:" + object);
     	CmsLogger.logInfo("created..........................." + object);
     	if (TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1)
 	    {
     	    String userName = "SYSTEM";
     	    NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_CREATE, getObjectIdentity(object), object.toString());
     	    ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
     	    
 			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
 			{
 				CacheController.clearCache("repositoryCache");
 			}
 			else if(object.getClass().getName().equals(InterceptionPointImpl.class.getName()))
 			{
 				CacheController.clearCache("interceptionPointCache");
 				CacheController.clearCache("interceptorsCache");
 				CacheController.clearCache("authorizationCache");
 			}
 			else if(object.getClass().getName().equals(InterceptorImpl.class.getName()))
 			{
 				CacheController.clearCache("interceptionPointCache");
 				CacheController.clearCache("interceptorsCache");
 				CacheController.clearCache("authorizationCache");
 			}
 			else if(object.getClass().getName().equals(AccessRightImpl.class.getName()) || object.getClass().getName().equals(AccessRightRoleImpl.class.getName()) || object.getClass().getName().equals(AccessRightGroupImpl.class.getName()))
 			{
 				CacheController.clearCache("interceptionPointCache");
 				CacheController.clearCache("interceptorsCache");
 				CacheController.clearCache("authorizationCache");
 			}
 			else if(object.getClass().getName().equals(ContentTypeDefinitionImpl.class.getName()))
 			{
 				CacheController.clearCache("contentTypeDefinitionCache");
 			}
 			else if(object.getClass().getName().equals(ContentImpl.class.getName()))
 			{
 				CacheController.clearCache("childContentCache");
 				clearCache(SmallContentImpl.class);
 				clearCache(MediumContentImpl.class);
 			}
 			else if(object.getClass().getName().equals(ContentVersionImpl.class.getName()))
 			{
 				CacheController.clearCache("componentContentsCache");
 			}
 			else if(object.getClass().getName().equals(RepositoryLanguageImpl.class.getName()))
 			{
 				CacheController.clearCache("masterLanguageCache");
 				CacheController.clearCache("repositoryLanguageListCache");
 			}
 			else if(object.getClass().getName().equals(WorkflowDefinitionImpl.class.getName()))
 			{
 				CacheController.clearCache("workflowCache");
 			}
 
 
 			//System.out.println("created end...:" + object);
     	}
     }
 
 
     public void removing( Object object )
         throws Exception
     {
         // ( (Persistent) object ).jdoBeforeRemove();
     }
 
 
     public void removed( Object object ) throws Exception
     {
 		//System.out.println("removed...:" + object);
         // ( (Persistent) object ).jdoAfterRemove();
         
        	if (TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1)
 	    {
        	    String userName = "SYSTEM";
 		    NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_DELETE, getObjectIdentity(object), object.toString());
 		    ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
        	    
 			if(object.getClass().getName().equals(RepositoryImpl.class.getName()))
 			{
 				CacheController.clearCache("repositoryCache");
 			}
 			else if(object.getClass().getName().equals(InterceptionPointImpl.class.getName()))
 			{
 				CacheController.clearCache("interceptionPointCache");
 				CacheController.clearCache("interceptorsCache");
 				CacheController.clearCache("authorizationCache");
 			}
 			else if(object.getClass().getName().equals(InterceptorImpl.class.getName()))
 			{
 				CacheController.clearCache("interceptionPointCache");
 				CacheController.clearCache("interceptorsCache");
 				CacheController.clearCache("authorizationCache");
 			}
 			else if(object.getClass().getName().equals(AccessRightImpl.class.getName()) || object.getClass().getName().equals(AccessRightRoleImpl.class.getName()) || object.getClass().getName().equals(AccessRightGroupImpl.class.getName()))
 			{
 				CacheController.clearCache("interceptionPointCache");
 				CacheController.clearCache("interceptorsCache");
 				CacheController.clearCache("authorizationCache");
 			}
 			else if(object.getClass().getName().equals(ContentTypeDefinitionImpl.class.getName()))
 			{
 				CacheController.clearCache("contentTypeDefinitionCache");
 			}
 			else if(object.getClass().getName().equals(ContentImpl.class.getName()))
 			{
 				CacheController.clearCache("childContentCache");
 				clearCache(SmallContentImpl.class);
 				clearCache(MediumContentImpl.class);
 
 				RegistryController.getController().clearRegistryForReferencedEntity(ContentImpl.class.getName(), getObjectIdentity(object).toString());
 			}
 			else if(object.getClass().getName().equals(ContentVersionImpl.class.getName()))
 			{
 				CacheController.clearCache("componentContentsCache");
 			}
 			else if(object.getClass().getName().equals(RepositoryLanguageImpl.class.getName()))
 			{
 				CacheController.clearCache("masterLanguageCache");
 				CacheController.clearCache("repositoryLanguageListCache");
 			}
 			else if(object.getClass().getName().equals(DigitalAssetImpl.class.getName()))
 			{
 				//CmsLogger.logInfo("We should delete all images with digitalAssetId " + getObjectIdentity(object));
 				DigitalAssetController.deleteCachedDigitalAssets((Integer)getObjectIdentity(object));
 			}
 			else if(object.getClass().getName().equals(SiteNodeImpl.class.getName()))
 			{
 			    RegistryController.getController().clearRegistryForReferencedEntity(SiteNodeImpl.class.getName(), getObjectIdentity(object).toString());
 			}
 			else if(object.getClass().getName().equals(WorkflowDefinitionImpl.class.getName()))
 			{
 				CacheController.clearCache("workflowCache");
 			}
 
 
        	}
     }
 
 
     public void releasing(Object object, boolean committed)
     {
         //System.out.println("releasing...:" + object + ":" + committed);
         // ( (Persistent) object ).jdoTransient();
         
         /*
         System.out.println("releasing...:" + object + ":" + committed);
 	    if(DigitalAssetImpl.class.getName().equals(object.getClass().getName()) && committed)
 	    {
 	        System.out.println("releasing...:" + object + ":" + committed);
 	    	CmsLogger.logInfo("Actually releasing it:" + object + ":" + committed);
     		String userName = "SYSTEM";
 	    	NotificationMessage notificationMessage = new NotificationMessage("CmsJDOCallback", object.getClass().getName(), userName, NotificationMessage.TRANS_UPDATE, getObjectIdentity(object), object.toString());
 	    	ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
 	    }
 	    */
 
     }
 
 
     public void updated(Object object) throws Exception
     {
         //System.out.println("updated...:" + object);
         // ( (Persistent) object ).jdoUpdate();
     	
     	//String className = object.getClass().getName();
 		//if (CmsSystem.getTransactionHistoryEntityClassName().indexOf(className) == -1)
 		//	CmsSystem.transactionLogEntry("CMSJDOCallback:" + object.getClass().getName(), CmsSystem.TRANS_UPDATE, getEntityId(object), object.toString());   
 
 //		CmsLogger.logInfo("updated..........................." + object);
 /*
      	if (TransactionHistoryImpl.class.getName().indexOf(object.getClass().getName()) == -1)
 	    {
 	    	String userName = "Fix later";
 	    	NotificationMessage notificationMessage = new NotificationMessage("CMSJDOCallback:" + object.getClass().getName(), object.getClass().getName(), userName, CmsSystem.TRANS_UPDATE, getEntityId(object), object.toString());
 	    	ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
 	    }
 */
      }
 
 
 	private Integer getEntityId(Object entity) throws Bug
 	{
 		Integer entityId = new Integer(-1);
 		
 		try 
 		{
 			entityId = ((IBaseEntity) entity).getId();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			throw new Bug("Unable to retrieve object id");
 		}
 		
 		return entityId;
 	}
 
 	private Object getObjectIdentity(Object entity) throws Bug
 	{
 		Object objectIdentity = new Integer(-1);
 		
 		try 
 		{
 			objectIdentity = ((IBaseEntity) entity).getIdAsObject();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			throw new Bug("Unable to retrieve object identity");
 		}
 		
 		return objectIdentity;
 	}
 
 }
 
