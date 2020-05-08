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
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.CacheManager;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.applications.common.Session;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.controllers.kernel.impl.simple.CmsJDOCallback;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
 import org.infoglue.cms.entities.content.Content;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.impl.simple.*;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.entities.structure.impl.simple.*;
 import org.infoglue.cms.entities.publishing.impl.simple.*;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.impl.simple.*;
 import org.infoglue.cms.entities.workflow.impl.simple.*;
 import org.infoglue.cms.exception.SystemException;
 
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.FakeHttpServletRequest;
 import org.infoglue.cms.util.FakeHttpServletResponse;
 import org.infoglue.cms.util.FakeHttpSession;
 import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
 import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.applications.databeans.WebPage;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BaseDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.EditOnSiteBasicTemplateController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.RepositoryDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 import org.infoglue.deliver.invokers.PageInvoker;
 import org.infoglue.deliver.portal.PortalService;
 
 import com.opensymphony.oscache.base.CacheEntry;
 import com.opensymphony.oscache.base.NeedsRefreshException;
 import com.opensymphony.oscache.base.events.CacheEntryEventListener;
 import com.opensymphony.oscache.base.events.CacheMapAccessEventListener;
 import com.opensymphony.oscache.extra.CacheEntryEventListenerImpl;
 import com.opensymphony.oscache.extra.CacheMapAccessEventListenerImpl;
 import com.opensymphony.oscache.general.GeneralCacheAdministrator;
 import com.steadystate.css.parser.selectors.BeginHyphenAttributeConditionImpl;
 
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 
 public class ContentCentricCachePopulator
 { 
     public final static Logger logger = Logger.getLogger(ContentCentricCachePopulator.class.getName());
 
 	//These are the standard parameters which uniquely defines which page to show.
 	private Integer siteNodeId = null;
 	private Integer contentId  = null; 
 	private Integer languageId = null;
 	
 	private boolean showSimple = false;
 	
 	//This parameter are set if you want to access a certain repository startpage
 	private String repositoryName = null;
 	
 	//A cached nodeDeliveryController
 	protected NodeDeliveryController nodeDeliveryController					= null;
 	protected IntegrationDeliveryController integrationDeliveryController 	= null;
 	protected TemplateController templateController 						= null;
 		
 	private static final boolean USE_LANGUAGE_FALLBACK        			= true;
 	private static final boolean DO_NOT_USE_LANGUAGE_FALLBACK 			= false;
 	
 	//The browserbean
 	private BrowserBean browserBean = null;
 	private Principal principal = null;
 		
 
 	/**
 	 * This method simulates a call to a page so all castor caches fills up before we throw the old page cache.
 	 * @param db
 	 * @param siteNodeId
 	 * @param languageId
 	 * @param contentId
 	 */
 	
 	public void recache(DatabaseWrapper dbWrapper, Integer siteNodeId) throws SystemException, Exception
 	{
         logger.info("recache starting..");
 
         FakeHttpSession fakeHttpServletSession = new FakeHttpSession();
         FakeHttpServletResponse fakeHttpServletResponse = new FakeHttpServletResponse();
         FakeHttpServletRequest fakeHttpServletRequest = new FakeHttpServletRequest();
         fakeHttpServletRequest.setParameter("siteNodeId", "" + siteNodeId);
         fakeHttpServletRequest.setParameter("languageId", "" + languageId);
         fakeHttpServletRequest.setParameter("contentId", "" + contentId);
         fakeHttpServletRequest.setRequestURI("ViewPage.action");
 
         fakeHttpServletRequest.setAttribute("siteNodeId", "" + siteNodeId);
         fakeHttpServletRequest.setAttribute("languageId", "" + languageId);
         fakeHttpServletRequest.setAttribute("contentId", "" + contentId);
 
         fakeHttpServletRequest.setServletContext(DeliverContextListener.getServletContext());
         
 		this.browserBean = new BrowserBean();
 	    //this.browserBean.setRequest(getRequest());
 
 	    this.siteNodeId = siteNodeId;
 	    
 		LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(dbWrapper.getDatabase(), this.siteNodeId);
 		if(masterLanguageVO == null)
 			throw new SystemException("There was no master language for the siteNode " + siteNodeId);
 	
 		this.languageId = masterLanguageVO.getLanguageId();
 		if(languageId == null)
 		    this.languageId = masterLanguageVO.getLanguageId();				
 		
 	    Map arguments = new HashMap();
 	    arguments.put("j_username", "anonymous");
 	    arguments.put("j_password", "anonymous");
 	    
 		this.principal = ExtranetController.getController().getAuthenticatedPrincipal(dbWrapper.getDatabase(), arguments);
 					
 		if(principal != null)
 			CacheController.cacheObject("userCache", "anonymous", this.principal);
 
         logger.info("recache parameter setup done..");
 
     	this.nodeDeliveryController			= NodeDeliveryController.getNodeDeliveryController(this.siteNodeId, this.languageId, this.contentId);
 		this.integrationDeliveryController	= IntegrationDeliveryController.getIntegrationDeliveryController(this.siteNodeId, this.languageId, this.contentId);
 		this.templateController 			= getTemplateController(dbWrapper, this.siteNodeId, this.languageId, this.contentId, fakeHttpServletRequest, (InfoGluePrincipal)this.principal, false);
 		
     	String pageKey = this.nodeDeliveryController.getPageCacheKey(dbWrapper.getDatabase(), fakeHttpServletSession, this.templateController, this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), fakeHttpServletRequest.getQueryString(), "");
     	//String pageKey = CacheController.getPageCacheKey(this.siteNodeId, this.languageId, this.contentId, browserBean.getUseragent(), this.getRequest().getQueryString(), "");
 
     	String pagePath	= null;
     	
     	boolean isUserRedirected = false;
 	
 		if(!isUserRedirected)
 		{	
 			logger.info("this.templateController.getPrincipal():" + this.templateController.getPrincipal());
 			DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext(/*(InfoGluePrincipal)this.principal*/);
 			deliveryContext.setRepositoryName(this.repositoryName);
 			deliveryContext.setSiteNodeId(this.siteNodeId);
 			deliveryContext.setContentId(this.contentId);
 			deliveryContext.setLanguageId(this.languageId);
 			deliveryContext.setPageKey(pageKey);
 			deliveryContext.setSession(new Session(fakeHttpServletSession));
 			deliveryContext.setInfoGlueAbstractAction(null);
 			deliveryContext.setHttpServletRequest(fakeHttpServletRequest);
 			deliveryContext.setHttpServletResponse(fakeHttpServletResponse);
 
 			this.templateController.setDeliveryContext(deliveryContext);
 			
 			//We don't want a page cache entry to be created
 			deliveryContext.setDisablePageCache(true);
 
 			SiteNode siteNode = nodeDeliveryController.getSiteNode(dbWrapper.getDatabase(), this.siteNodeId);
 			if(siteNode == null)
 			    throw new SystemException("There was no page with this id.");
 			
 			logger.info("siteNode:" + siteNode.getName());
 			
 			Integer rootMetaInfoContentId = this.templateController.getMetaInformationContentId(siteNodeId);
 			logger.info("rootMetaInfoContentId:" + rootMetaInfoContentId);
 			
 			recurseSiteNodeTree(siteNode.getSiteNodeId(), languageId);
 
 		    Integer topContentId = null;
 		    ContentVO contentVO = this.templateController.getContent(rootMetaInfoContentId);
 		    logger.info("contentVO:" + contentVO.getName());
 			ContentVO parentContentVO = this.templateController.getContent(contentVO.getParentContentId());
 			logger.info("parentContentVO:" + parentContentVO.getName());
 			while(parentContentVO != null)
 		    {
 	            topContentId = parentContentVO.getContentId();
 
 	            if(parentContentVO.getParentContentId() != null)
 	                parentContentVO = this.templateController.getContent(parentContentVO.getParentContentId());
 	            else
 	                parentContentVO = null;
 		    }
 		    
 			logger.info("topContentId:" + topContentId);
 			
 			if(topContentId != null)
 		        recurseContentTree(topContentId, languageId);
 		}
 
 	}
 	
 	
 	private void recurseContentTree(Integer contentId, Integer languageId) throws Exception
 	{
 	    Content content = ContentController.getContentController().getReadOnlyContentWithId(contentId, this.templateController.getDatabase());
 	    ContentVO contentVO = this.templateController.getContent(contentId);
 	    
 	    Collection childContents = content.getChildren();
 		
 	    Iterator childContentsIterator = childContents.iterator();
 	    while(childContentsIterator.hasNext())
         {
 	        Content childContent = (Content)childContentsIterator.next();
 	        recurseContentTree(childContent.getId(), languageId);
 	        
 	        //logger.warn("Before read title of content...");
	        this.templateController.getContentAttribute(childContent.getId(), languageId, "Title", true); 
	        this.templateController.getContentAttribute(childContent.getId(), languageId, "NavigationTitle", true); 
	        this.templateController.getContentAttribute(childContent.getId(), languageId, "LeadIn", true); 
	        this.templateController.getContentAttribute(childContent.getId(), languageId, "FullText", true); 
 	        //logger.warn("Read title of content...");
         }
 	}
 	
 	private void recurseSiteNodeTree(Integer siteNodeId, Integer languageId) throws Exception
 	{
 	    SiteNode siteNode = SiteNodeController.getController().getSiteNodeWithId(siteNodeId, this.templateController.getDatabase(), true);
 	    SiteNodeVO siteNodeVO = this.templateController.getSiteNode(siteNodeId);
 	    Collection childSiteNodes = siteNode.getChildSiteNodes();
 	    
 	    Iterator childSiteNodesIterator = childSiteNodes.iterator();
 	    while(childSiteNodesIterator.hasNext())
         {
 	        SiteNode childSiteNode = (SiteNode)childSiteNodesIterator.next();
 	        recurseSiteNodeTree(childSiteNode.getSiteNodeId(), languageId);
 	        
 	        Integer metaInfoContentId = this.templateController.getMetaInformationContentId(childSiteNode.getSiteNodeId()); 
 
 	        this.templateController.getContentAttribute(metaInfoContentId, languageId, "ComponentStructure", true); 
         }
 	}
 
 	
    	/**
 	 * This method should be much more sophisticated later and include a check to see if there is a 
 	 * digital asset uploaded which is more specialized and can be used to act as serverside logic to the template.
 	 * The method also consideres wheter or not to invoke the preview-version with administrative functioality or the 
 	 * normal site-delivery version.
 	 */
 	
 	public TemplateController getTemplateController(DatabaseWrapper dbWrapper, Integer siteNodeId, Integer languageId, Integer contentId, HttpServletRequest request, InfoGluePrincipal infoGluePrincipal, boolean allowEditOnSightAtAll) throws SystemException, Exception
 	{
 		TemplateController templateController = new BasicTemplateController(dbWrapper, infoGluePrincipal);
 		templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
 		templateController.setHttpRequest(request);	
 		templateController.setBrowserBean(browserBean);
 		templateController.setDeliveryControllers(this.nodeDeliveryController, null, this.integrationDeliveryController);	
 		
 		return templateController;		
 	}
 
 }
