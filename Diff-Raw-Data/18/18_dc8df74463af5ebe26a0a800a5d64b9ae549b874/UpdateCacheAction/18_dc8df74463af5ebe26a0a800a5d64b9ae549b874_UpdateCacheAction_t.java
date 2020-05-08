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
 
 
 package org.infoglue.deliver.applications.actions;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
 import org.infoglue.cms.util.*;
 
 import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
 import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
 import org.infoglue.cms.entities.content.impl.simple.MediumContentImpl;
 import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
 import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
 import org.infoglue.cms.entities.management.impl.simple.GroupImpl;
 import org.infoglue.cms.entities.management.impl.simple.RoleImpl;
 import org.infoglue.cms.entities.management.impl.simple.SmallAvailableServiceBindingImpl;
 import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
 import org.infoglue.cms.entities.publishing.PublicationDetailVO;
 import org.infoglue.cms.entities.publishing.impl.simple.PublicationDetailImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
 import org.infoglue.cms.entities.structure.impl.simple.SmallSiteNodeImpl;
 import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
 import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;
 import org.infoglue.deliver.util.CacheController;
 
 
 /**
  * This is the action that takes care of all incoming update-calls. This action is
  * called by either the system or by replication-program and the class the distibutes the 
  * update-call to all the listeners which have registered earlier.
  *
  * @author Mattias Bogeblad
  */
 
 public class UpdateCacheAction extends InfoGlueAbstractAction 
 {
 	private String className = null;
 	private String objectId = null;
 	private String objectName = null;
 	private String typeId = null;
 
 	private String repositoryName = null;
 	private Integer languageId    = null;
 	private Integer siteNodeId    = null;
 	
 	private static boolean cachingInProgress = false;
 	
 	/**
 	 * The constructor for this action - contains nothing right now.
 	 */
     
     public UpdateCacheAction() 
     {
 	
     }
     
     /**
      * This method will just reply to a testcall. 
      */
          
     public String doTest() throws Exception
     {
        String operatingMode = CmsPropertyHandler.getProperty("operatingMode");
		
        if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
         {
	        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
	        {
	            this.getResponse().setContentType("text/plain");
	            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
	            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
	            
	            return NONE;
	        }
         }
         
         this.getResponse().getWriter().println("test ok - cache action available");
         
         //this.getHttpSession().invalidate();
         
         return NONE;
     }
     
     /**
      * This method is the application entry-point. The parameters has been set through the setters
      * and now we just have to render the appropriate output. 
      */
          
     public String doExecute() throws Exception
     {
         String operatingMode = CmsPropertyHandler.getProperty("operatingMode");
 		
         if(operatingMode != null && operatingMode.equalsIgnoreCase("3"))
         {
 	        long start = System.currentTimeMillis();
 	        if(!ServerNodeController.getController().getIsIPAllowed(this.getRequest()))
 	        {
 	            this.getResponse().setContentType("text/plain");
 	            this.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
 	            this.getResponse().getWriter().println("You have no access to this view - talk to your administrator if you should.");
 	            
 	            return NONE;
 	        }
 	        
         }
         
 		try
 		{  
 		    getLogger().info("className:" + className);
 			getLogger().info("objectId:" + objectId);
 		    	
 		    //boolean isDependsClass = false;
 			//if(className.equalsIgnoreCase(PublicationDetailImpl.class.getName()))
 			//    isDependsClass = true;
 
 			//Iterate through all registered listeners and call them... dont place logic here... have specialized handlers.			
 
 			if(operatingMode != null && operatingMode.equalsIgnoreCase("3")) //If published-mode we update entire cache to be sure..
 			{
 			    getLogger().info("className:" + className);
 			    getLogger().info("objectId:" + objectId);
 
 		        synchronized(CacheController.getNotifications())
 		        {
 				    CacheEvictionBean cacheEvictionBean = new CacheEvictionBean(this.className, this.typeId, this.objectId, this.objectName);
 				    CacheController.getNotifications().add(cacheEvictionBean);
 				    getLogger().info("Added a cacheEvictionBean....");
 		        }
 
 			    //Hardcoded some stuff to clear.... not nice. Instead have some register which 
 				//different caches can register to.
 				/*
 		        CacheController.clearCaches(null, null);
 				getLogger().info("Updating all caches as this was a publishing-update");
 				CacheController.clearCastorCaches();
 				*/
 				
 				//If it's an contentVersion we should delete all images it might have generated from attributes.
 				/*
 				if(className.equalsIgnoreCase(PublicationDetailImpl.class.getName()))
 				{
 				    PublicationDetailVO publicationDetailVO = PublicationController.getController().getPublicationDetailVOWithId(new Integer(objectId));
 					String innerClassName = publicationDetailVO.getEntityClass();
 					Integer innerObjectId  = publicationDetailVO.getEntityId();
 					System.out.println("innerClassName:" + innerClassName);
 				    System.out.println("innerObjectId:" + innerObjectId);
 				    	
 				    if(Class.forName(innerClassName).getName().equals(ContentVersionImpl.class.getName()))
 					{
 						getLogger().info("We should delete all images with contentVersionId " + objectId);
 						DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteContentVersionAssets(new Integer(objectId));
 					}
 				}
 				*/	
 				/*
 				else if(Class.forName(className).getName().equals(DigitalAssetImpl.class.getName()))
 				{
 					getLogger().info("We should delete all images with digitalAssetId " + objectId);
 					DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteDigitalAssets(new Integer(objectId));
 				}
 				*/
 			}			
 			else
 			{			
 				//Hardcoded some stuff to clear.... not nice. Instead have some register which 
 				//different caches can register to.
 		        synchronized(CacheController.getNotifications())
 		        {
 				    CacheEvictionBean cacheEvictionBean = new CacheEvictionBean(this.className, this.typeId, this.objectId, this.objectName);
 				    CacheController.getNotifications().add(cacheEvictionBean);
 				    getLogger().info("Added a cacheEvictionBean....");
 		        }
 			    /*
 			    CacheController.clearCaches(className, objectId);
 
 			    getLogger().info("Updating className with id:" + className + ":" + objectId);
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
 					    getLogger().info("We should delete all images with contentVersionId " + objectId);
 						DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteContentVersionAssets(new Integer(objectId));
 					}
 					else */
 			    
 			    	/*
 			    	if(Class.forName(className).getName().equals(ContentImpl.class.getName()))
 					{
 					    getLogger().info("We clear all small contents as well " + objectId);
 						Class typesExtra = SmallContentImpl.class;
 						Object[] idsExtra = {new Integer(objectId)};
 						CacheController.clearCache(typesExtra, idsExtra);
 
 						getLogger().info("We clear all medium contents as well " + objectId);
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
 					    getLogger().info("We should delete all images with digitalAssetId " + objectId);
 						DigitalAssetDeliveryController.getDigitalAssetDeliveryController().deleteDigitalAssets(new Integer(objectId));
 					}
 				}
 				*/
 			}		
 			
 			getLogger().info("UpdateCache finished...");
 		}
 		catch(Exception e)
 		{
 		    e.printStackTrace();
 			getLogger().error(e.getMessage(), e);
 		}
 		catch(Throwable t)
 		{
 		    t.printStackTrace();
 		    getLogger().error(t.getMessage());
 		}
                 
 		//this.getHttpSession().invalidate();
 		
         return NONE;
     }
     
     
 	/**
 	 * Setters and getters for all things sent to the page in the request
 	 */
 	        
     public void setClassName(String className)
     {
 	    this.className = className;
     }
         
     public void setObjectId(String objectId)
     {
 	    this.objectId = objectId;
     }
 
     public void setObjectName(String objectName)
     {
 	    this.objectName = objectName;
     }
 
     public void setTypeId(String typeId)
     {
 	    this.typeId = typeId;
     }
     
 }
