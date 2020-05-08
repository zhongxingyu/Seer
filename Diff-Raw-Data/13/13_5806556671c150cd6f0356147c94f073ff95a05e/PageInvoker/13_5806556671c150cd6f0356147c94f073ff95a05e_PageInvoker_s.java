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
 
 package org.infoglue.deliver.invokers;
 
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.exception.SystemException;
 
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 import org.infoglue.deliver.portal.PortalController;
 import org.infoglue.deliver.util.CacheController;
 
 /**
  * @author Mattias Bogeblad
  *
  * This interface defines what a Invoker of a page have to be able to do.
  * The invokers are used to deliver a page to the user in a certain fashion.
  *
  */
 
 public abstract class PageInvoker
 {	
     private final static Logger logger = Logger.getLogger(PageInvoker.class.getName());
 
     private DatabaseWrapper dbWrapper				= null;
 	private HttpServletRequest request				= null;
 	private HttpServletResponse response 			= null;
 	private TemplateController templateController	= null;
 	private DeliveryContext deliveryContext 		= null;
 	
 	private String pageString	 					= null;
 	
 	/*public PageInvoker()
 	{
 	}
 	*/
 	
 	/**
 	 * The default constructor for PageInvokers. 
 	 * @param request
 	 * @param response
 	 * @param templateController
 	 * @param deliveryContext
 	 */
 	/*
 	public PageInvoker(HttpServletRequest request, HttpServletResponse response, TemplateController templateController, DeliveryContext deliveryContext)
 	{
 		this.request = request;
 		this.response = response;
 		this.templateController = templateController;
 		this.deliveryContext = deliveryContext;
 		this.templateController.setDeliveryContext(this.deliveryContext);
 	}
 	*/
 	
 	/**
 	 * This method should return an instance of the class that should be used for page editing inside the tools or in working. 
 	 * Makes it possible to have an alternative to the ordinary delivery optimized class.
 	 */
 	
 	public abstract PageInvoker getDecoratedPageInvoker() throws SystemException;
 	
 	/**
 	 * The default initializer for PageInvokers. 
 	 * @param request
 	 * @param response
 	 * @param templateController
 	 * @param deliveryContext
 	 */
 
 	public void setParameters(DatabaseWrapper dbWrapper, HttpServletRequest request, HttpServletResponse response, TemplateController templateController, DeliveryContext deliveryContext)
 	{
 	    this.dbWrapper = dbWrapper;
 		this.request = request;
 		this.response = response;
 		this.templateController = templateController;
 		this.deliveryContext = deliveryContext;
 		this.templateController.setDeliveryContext(this.deliveryContext);
 	}
 	
     public Database getDatabase() throws SystemException
     {
         /*
         if(this.db == null || this.db.isClosed() || !this.db.isActive())
         {
             beginTransaction();
         }
         */
         return dbWrapper.getDatabase();
     }
 
     
 	/**
 	 * This is the method that will deliver the page to the user. It can have special
 	 * handling of all sorts to enable all sorts of handlers. An example of uses might be to
 	 * be to implement a WAP-version of page delivery where you have to set certain headers in the response
 	 * or a redirect page which just redirects you to another page.  
 	 */
 	
 	public abstract void invokePage() throws SystemException, Exception;
 	
 
 	/**
 	 * This method is used to send the page out to the browser or other device.
 	 * Override this if you need to set other headers or do other specialized things.
 	 */
 
 	public void deliverPage() throws Exception
 	{
 		logger.info("C PageKey:" + this.getDeliveryContext().getPageKey());
 		
 		LanguageVO languageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageVO(getDatabase(), this.getTemplateController().getLanguageId());
 		logger.info("languageVO:" + languageVO);
 		
 		String isPageCacheOn = CmsPropertyHandler.getProperty("isPageCacheOn");
 		logger.info("isPageCacheOn:" + isPageCacheOn);
 		String refresh = this.getRequest().getParameter("refresh");
 		
		if(isPageCacheOn.equalsIgnoreCase("true") && (refresh == null || !refresh.equalsIgnoreCase("true")))
 		{
 		    this.pageString = (String)CacheController.getCachedObjectFromAdvancedCache("pageCache", this.getDeliveryContext().getPageKey());
 			if(this.pageString == null)
 			{
 				invokePage();
 				this.pageString = getPageString();
 				
 				if(!this.getTemplateController().getIsPageCacheDisabled() && !this.getDeliveryContext().getDisablePageCache()) //Caching page if not disabled
 					CacheController.cacheObjectInAdvancedCache("pageCache", this.getDeliveryContext().getPageKey(), pageString, this.getDeliveryContext().getAllUsedEntities());
 			}
 			else
 			{
 				logger.info("There was a cached copy..."); // + pageString);
 			}
 			
 			//Caching the pagePath
 			logger.info("Caching the pagePath...");
 			this.getDeliveryContext().setPagePath((String)CacheController.getCachedObject("pagePathCache", this.getDeliveryContext().getPageKey()));
 			if(this.getDeliveryContext().getPagePath() == null)
 			{
 				this.getDeliveryContext().setPagePath(this.getTemplateController().getCurrentPagePath());
 			
 				if(!this.getTemplateController().getIsPageCacheDisabled() && !this.getDeliveryContext().getDisablePageCache()) //Caching page path if not disabled
 					CacheController.cacheObject("pagePathCache", this.getDeliveryContext().getPageKey(), this.getDeliveryContext().getPagePath());
 			}
 			logger.info("Done caching the pagePath...");	
 		}
 		else
 		{
 			invokePage();
 			this.pageString = getPageString();
 			
 			this.getDeliveryContext().setPagePath(this.templateController.getCurrentPagePath());
 		}
 
 		String contentType = this.getTemplateController().getPageContentType();
 		//logger.info("ContentType in deliveryContext:" + this.deliveryContext.getContentType());
 		if(this.deliveryContext.getContentType() != null && !contentType.equalsIgnoreCase(this.deliveryContext.getContentType()))
 		    contentType = this.deliveryContext.getContentType();
 		
 		//logger.info("ContentType:" + contentType);
 		this.getResponse().setContentType(contentType + "; charset=" + languageVO.getCharset());
 		logger.info("contentType:" + contentType + "; charset=" + languageVO.getCharset());
 		
 		PrintWriter out = this.getResponse().getWriter();
 		out.println(pageString);
 		out.flush();
 		out.close();		
 	}
 
 	
 				
 	/**
 	 * This method is used to allow pagecaching on a general level.
 	 */
 
 	public void cachePage()
 	{
 		
 	}
 	
 	public final DeliveryContext getDeliveryContext()
 	{
 		return deliveryContext;
 	}
 
 	public final HttpServletRequest getRequest()
 	{
 		return request;
 	}
 
 	public final HttpServletResponse getResponse()
 	{
 		return response;
 	}
 
 	public final TemplateController getTemplateController()
 	{
 		return templateController;
 	}
 
 	public String getPageString()
 	{
 		return pageString;
 	}
 
 	public void setPageString(String string)
 	{
 		pageString = string;
 	}
 
 	
 	/**
 	 * Creates and returns a defaultContext, currently with the templateLogic 
 	 * and if the portal support is enabled the portalLogic object. 
 	 * (Added to avoid duplication of context creation in the concrete 
 	 * implementations of pageInvokers)
 	 * @author robert
 	 * @return A default context with the templateLogic and portalLogic object in it.
 	 */
 	
 	public Map getDefaultContext() 
 	{
 		Map context = new HashMap();
 		context.put("templateLogic", getTemplateController());		
 		
 		// -- check if the portal is active
         String portalEnabled = CmsPropertyHandler.getProperty("enablePortal") ;
         boolean active = ((portalEnabled != null) && portalEnabled.equals("true"));
 		if (active) 
 		{
 		    PortalController pController = new PortalController(getRequest(), getResponse());
 		    context.put(PortalController.NAME, pController);
 		    logger.info("PortalController.NAME:" + PortalController.NAME);
 		    logger.info("pController:" + pController);
 		}
 		
 		return context;
 	}
     
 }
