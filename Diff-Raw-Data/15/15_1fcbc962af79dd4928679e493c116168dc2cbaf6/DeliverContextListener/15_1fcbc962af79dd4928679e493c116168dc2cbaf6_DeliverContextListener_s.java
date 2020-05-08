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
 
 import javax.servlet.*;
 
 import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
 import org.infoglue.cms.util.CmsPropertyHandler;
 
 import java.io.File;
 
 /**
  * This class functions as the entry-point for all initialization of the Cms-tool.
  * The class responds to the startup or reload of a whole context.
  */
 
 public final class DeliverContextListener implements ServletContextListener 
 {
 	private static CacheController cacheController = new CacheController();
 	
 	private static ServletContext servletContext = null;
 	
 	public static ServletContext getServletContext()
 	{
 	    return servletContext;
 	}
 	
 	/**
 	 * This method is called when the servlet context is 
 	 * initialized(when the Web Application is deployed). 
 	 * You can initialize servlet context related data here.
      */
 	 
     public void contextInitialized(ServletContextEvent event) 
     {
 		System.out.println("contextInitialized for deliver...");
 		try
 		{
 		    servletContext = event.getServletContext();
 		    
 			String isHeadless = System.getProperty("java.awt.headless");
 			System.out.println("java.awt.headless=" + isHeadless);
 			if(isHeadless == null || !isHeadless.equalsIgnoreCase("true"))
 			{
 				System.setProperty("java.awt.headless", "true");
 			}
 
 			String contextRootPath = event.getServletContext().getRealPath("/");
 			if(!contextRootPath.endsWith("/") && !contextRootPath.endsWith("\\")) 
 				contextRootPath = contextRootPath + "/";
 							
 			CmsPropertyHandler.setApplicationName("deliver");
 			
 			CmsPropertyHandler.setProperty("contextRootPath", contextRootPath); 
 			
 			String logPath = CmsPropertyHandler.getProperty("logPath");
 			if(logPath == null || logPath.equals(""))
 			{
 				logPath = contextRootPath + "logs" + File.separator + "infogluedeliver.log";
 				CmsPropertyHandler.setProperty("logPath", logPath);
 			}
 
 			String statisticsLogPath = CmsPropertyHandler.getProperty("statisticsLogPath");
 			if(statisticsLogPath == null || statisticsLogPath.equals(""))
 			{
 				statisticsLogPath = contextRootPath + "logs";
 				CmsPropertyHandler.setProperty("statisticsLogPath", statisticsLogPath);
 			}
 
 			String assetPath = CmsPropertyHandler.getProperty("digitalAssetPath");
 			if(assetPath == null || assetPath.equals(""))
 			{
 				assetPath = contextRootPath + "digitalAssets";
 				CmsPropertyHandler.setProperty("digitalAssetPath", assetPath);
				
				String digitalAssetPath0 = CmsPropertyHandler.getProperty("digitalAssetPath.0");
				if(digitalAssetPath0 == null || digitalAssetPath0.equals(""))
				{
					CmsPropertyHandler.setProperty("digitalAssetPath.0", assetPath);
				}
 			}
			
 			String expireCacheAutomaticallyString = CmsPropertyHandler.getProperty("expireCacheAutomatically");
 			if(expireCacheAutomaticallyString != null)
 				cacheController.setExpireCacheAutomatically(Boolean.getBoolean(expireCacheAutomaticallyString));
 
 			String intervalString = CmsPropertyHandler.getProperty("cacheExpireInterval");
 			if(intervalString != null)
 				cacheController.setCacheExpireInterval(Integer.parseInt(intervalString));
 		
 			//Starting the cache-expire-thread
 			if(cacheController.getExpireCacheAutomatically())
 				cacheController.start();
 			
 			InfoGlueAuthenticationFilter.initializeProperties();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
     }
 
     /**
      * This method is invoked when the Servlet Context 
      * (the Web Application) is undeployed or 
      * WebLogic Server shuts down.
      */			    
 
     public void contextDestroyed(ServletContextEvent event) 
     {
 		System.out.println("contextDestroyed....");
 		cacheController.stopThread();
 		cacheController.interrupt();
     }
 }
 
