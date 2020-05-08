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
 
 package org.infoglue.deliver.controllers.kernel.impl.simple;
 
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.*;
 import org.infoglue.cms.io.*;
 import org.infoglue.cms.exception.*;
 import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
 import org.infoglue.deliver.applications.databeans.WebPage;
 
 import java.util.List;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * This is the most basic template controller supplying the templates using it with
  * methods to fetch contents, structure and other suff needed for a site. Mostly this class just acts as a
  * delegator to other more specialized classes.
  */
 
 public class EditOnSiteBasicTemplateController extends BasicTemplateController
 {
     public EditOnSiteBasicTemplateController(DatabaseWrapper databaseWrapper, InfoGluePrincipal infoGluePrincipal)
     {
         super(databaseWrapper, infoGluePrincipal);
     }
     
 	/**
 	 * This method adds the neccessairy html to a output for it to be editable.
 	 */	
 	
 	private String decorateTag(Integer contentId, Integer languageId, String attributeName, String attributeValue)
 	{
 		String editOnSiteUrl = CmsPropertyHandler.getProperty("editOnSiteUrl");
 		String decoratedAttributeValue = "<span oncontextmenu=\"setContentItemParameters(" + contentId + "," + languageId + ",'" + attributeName + "'); setEditUrl('" + editOnSiteUrl + "?contentId=" + contentId + "&languageId=" + languageId + "&attributeName=" + attributeName + "');\">" + attributeValue + "</span>";
 		return decoratedAttributeValue;
 	} 
 	
 	/**
 	 * This method adds the neccessairy html to a template to make it right-clickable.
 	 */	
  
 	public String decoratePage(String page)
 	{
 		String decoratedTemplate = page;
 		
 		try
 		{
			String extraHeader = FileHelper.getStreamAsString(EditOnSiteBasicTemplateController.class.getResourceAsStream("/resources/templates/preview/editOnSiteHeader.vm"));
 			String servletContext = request.getContextPath();
			//CmsLogger.logInfo("extraHeader:" + extraHeader);
 			extraHeader = extraHeader.replaceAll("\\{applicationContext\\}", servletContext);
 			//CmsLogger.logInfo("extraHeader:" + extraHeader);
			String extraBody   = FileHelper.getStreamAsString(EditOnSiteBasicTemplateController.class.getResourceAsStream("/resources/templates/preview/editOnSiteBody.vm"));
		
 			StringBuffer modifiedTemplate = new StringBuffer(page);
 			
 			//Adding stuff in the header
 			int indexOfHeadEndTag = modifiedTemplate.indexOf("</head");
 			if(indexOfHeadEndTag == -1)
 				indexOfHeadEndTag = modifiedTemplate.indexOf("</HEAD");
 			
 			if(indexOfHeadEndTag > -1)
 			{
 				modifiedTemplate = modifiedTemplate.replace(indexOfHeadEndTag, modifiedTemplate.indexOf(">", indexOfHeadEndTag) + 1, extraHeader);
 			}
 			else
 			{
 				int indexOfHTMLStartTag = modifiedTemplate.indexOf("<html");
 				if(indexOfHTMLStartTag == -1)
 					indexOfHTMLStartTag = modifiedTemplate.indexOf("<HTML");
 		
 				if(indexOfHTMLStartTag > -1)
 				{
 					modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfHTMLStartTag) + 1, "<head>" + extraHeader);
 				}
 				else
 				{
 					CmsLogger.logWarning("The current template is not a valid document. It does not comply with the simplest standards such as having a correct header.");
 				}
 			}
 
 			//Adding stuff in the body	
 			int indexOfBodyStartTag = modifiedTemplate.indexOf("<body");
 			if(indexOfBodyStartTag == -1)
 				indexOfBodyStartTag = modifiedTemplate.indexOf("<BODY");
 				
 			if(indexOfBodyStartTag > -1)
 			{
 				modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfBodyStartTag) + 1, extraBody);
 			}
 			else
 			{
 				CmsLogger.logWarning("The current template is not a valid document. It does not comply with the simplest standards such as having a correct body.");
 			}
 			
 			decoratedTemplate = modifiedTemplate.toString();
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logWarning("An error occurred when deliver tried to decorate your template to enable onSiteEditing. Reason " + e.getMessage(), e);
 		}
 		
 		return decoratedTemplate;
 	}
 	
 		
 	/**
 	 * This method deliveres a String with the content-attribute asked for if it exists in the content
 	 * defined in the url-parameter contentId. It decorates the attibute with html so the attribute can be clicked on for
 	 * editing.
 	 */
 	 
 	public String getContentAttribute(String attributeName) 
 	{
 		return decorateTag(this.getContentId(), this.getLanguageId(), attributeName, super.getContentAttribute(attributeName));
 	}
 
 	/**
 	 * This method deliveres a String with the content-attribute asked for.
 	 * As the siteNode can have multiple bindings as well as a content as a parameter this
 	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
 	 */
 	 
 	public String getContentAttribute(String contentBindningName, String attributeName) 
 	{
 		String attributeValue = "";
 		
 		try
 		{
 			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(this.getDatabase(), this.getPrincipal(), this.getSiteNodeId(), this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
 			if(contentVO != null)
 			{
 				attributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(this.getDatabase(), contentVO.getContentId(), this.languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext);
 				attributeValue = decorateTag(contentVO.getContentId(), this.getLanguageId(), attributeName, attributeValue);
 			}
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred trying to get attributeName=" + attributeName + " on contentBindning " + contentBindningName + ":" + e.getMessage(), e);
 		}
 				
 		return attributeValue;
 	}
 
 
 	/**
 	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
 	 * value if OnSiteEdit is on.
 	 */
 	 
 	public String getContentAttribute(String contentBindningName, String attributeName, boolean clean) 
 	{				
 		return super.getContentAttribute(contentBindningName, attributeName);
 	}
 
 	/**
 	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
 	 * value if OnSiteEdit is on.
 	 */
 	 
 	public String getContentAttribute(String attributeName, boolean clean) 
 	{				
 		return super.getContentAttribute(attributeName);
 	}
 
 
 	/**
 	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
 	 * value if OnSiteEdit is on.
 	 */
  
 	public String getContentAttribute(Integer contentId, String attributeName, boolean clean)
 	{
 		return super.getContentAttribute(contentId, attributeName);
 	}
 		
 	/**
 	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
 	 * value if OnSiteEdit is on.
 	 */
  
 	public String getContentAttribute(Integer contentId, Integer languageId, String attributeName, boolean clean)
 	{
 		return super.getContentAttribute(contentId, languageId, attributeName);
 	}
 	
 	/**
 	 * This method deliveres a String with the content-attribute asked for.
 	 * As the siteNode can have multiple bindings as well as a content as a parameter this
 	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
 	 */
 	 
 	public String getContentAttribute(Integer contentId, String attributeName) 
 	{
 		if(attributeName.equalsIgnoreCase(this.getTemplateAttributeName()))
 			return super.getContentAttribute(contentId, attributeName);
 			//return decorateTemplate(super.getContentAttribute(contentId, attributeName));		
 		else
 			return decorateTag(contentId, this.getLanguageId(), attributeName, super.getContentAttribute(contentId, attributeName));
 	}
 
 
 
 	/**
 	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
 	 * As the siteNode can have multiple bindings as well as a content as a parameter this
 	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
 	 */
 	 
 	public String getParsedContentAttribute(String attributeName) 
 	{
 		return decorateTag(this.getContentId(), this.getLanguageId(), attributeName, super.getParsedContentAttribute(attributeName));
 	}
 
 
 	/**
 	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
 	 * As the siteNode can have multiple bindings as well as a content as a parameter this
 	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
 	 */
 	 
 	public String getParsedContentAttribute(String contentBindningName, String attributeName) 
 	{
 		String attributeValue = "";
 		
 		try
 		{
 			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(this.getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
 			if(contentVO != null)
 			{
 				attributeValue = getParsedContentAttribute(contentVO.getContentId(), attributeName);
 				attributeValue = decorateTag(contentVO.getContentId(), this.getLanguageId(), attributeName, attributeValue);
 			}
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere("An error occurred trying to get attributeName=" + attributeName + " on contentBindning " + contentBindningName + ":" + e.getMessage(), e);
 		}
 				
 		return attributeValue;
 	}
 
 	/**
 	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
 	 * value if OnSiteEdit is on.
 	 */
 	 
 	public String getParsedContentAttribute(String attributeName, boolean clean) 
 	{				
 		return super.getParsedContentAttribute(attributeName);
 	}
 	
 	/**
 	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
 	 * value if OnSiteEdit is on.
 	 */
 	 
 	public String getParsedContentAttribute(String contentBindningName, String attributeName, boolean clean) 
 	{				
 		return super.getParsedContentAttribute(contentBindningName, attributeName);
 	}
 
 	/**
 	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
 	 * value if OnSiteEdit is on.
 	 */
 	 
 	public String getParsedContentAttribute(Integer contentId, String attributeName, boolean clean) 
 	{				
 		return super.getParsedContentAttribute(contentId, attributeName);
 	}
 	
 	/**
 	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
 	 * The attribute is fetched from the specified content.
 	 */
 	 
 	public String getParsedContentAttribute(Integer contentId, String attributeName) 
 	{
 		return decorateTag(contentId, this.getLanguageId(), attributeName, super.getParsedContentAttribute(contentId, attributeName));
 	}
 
 
 	
 	/**
 	 * The method returns a list of WebPage-objects that is the bound sitenodes of named binding. 
 	 * The method is great for navigation-purposes on any site. 
 	 */
 
 	public List getBoundPages(String structureBindningName)
 	{
 		List boundPages = super.getBoundPages(structureBindningName);
 		Iterator i = boundPages.iterator();
 		while(i.hasNext())
 		{
 			WebPage webPage = (WebPage)i.next();
 			Integer contentId = super.getContentId(webPage.getSiteNodeId(), super.META_INFO_BINDING_NAME);
 			String navigationTitle = decorateTag(contentId, this.getLanguageId(), super.NAV_TITLE_ATTRIBUTE_NAME, webPage.getNavigationTitle());
 			webPage.setNavigationTitle(navigationTitle);
 		}
 		
 		return boundPages;
 	}
 	
 
 	/**
 	 * The method returns a list of WebPage-objects that is the bound sitenodes of named binding. 
 	 * The method is great for navigation-purposes on any site. 
 	 */
 
 	public List getBoundPages(Integer siteNodeId, String structureBindningName)
 	{
 		List boundPages = super.getBoundPages(siteNodeId, structureBindningName);
 		Iterator i = boundPages.iterator();
 		while(i.hasNext())
 		{
 			WebPage webPage = (WebPage)i.next();
 			Integer contentId = super.getContentId(webPage.getSiteNodeId(), super.META_INFO_BINDING_NAME);
 			String navigationTitle = decorateTag(contentId, this.getLanguageId(), super.NAV_TITLE_ATTRIBUTE_NAME, webPage.getNavigationTitle());
 			webPage.setNavigationTitle(navigationTitle);
 		}
 		
 		return boundPages;
 	}
 
 	/**
 	 * This method deliveres a String with the Navigation title the page asked for has.
 	 * As the siteNode can have multiple bindings the method requires a bindingName 
 	 * which refers to the AvailableServiceBinding.name-attribute. The navigation-title is fetched
 	 * from the meta-info-content bound to the site node.
 	 */
 	 
 	public String getPageNavTitle(String structureBindningName) 
 	{
 		Integer siteNodeId = super.getSiteNodeId(structureBindningName);
 		Integer contentId = super.getContentId(siteNodeId, super.META_INFO_BINDING_NAME);
 		String navTitle = decorateTag(contentId, this.getLanguageId(), super.NAV_TITLE_ATTRIBUTE_NAME, super.getPageNavTitle(structureBindningName));
 						
 		return navTitle;
 	}
 	
 	
 	/**
 	 * This method deliveres a String with the URL to the digital asset asked for.
 	 * As the siteNode can have multiple bindings as well as a content as a parameter this
 	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
 	 */
 /*	 
 	public String getAssetUrl(String contentBindningName) 
 	{
 		return decorateTag(this.getContentId(), this.getLanguageId(), "", super.getAssetUrl(contentBindningName));
 	}
 */
 	/**
 	 * This method deliveres a String with the URL to the digital asset asked for.
 	 */
 	/*	 
 	 
 	public String getAssetUrl(Integer contentId) 
 	{
 		return decorateTag(this.getContentId(), this.getLanguageId(), "", super.getAssetUrl(contentId));
 	}
 */
 
 	/**
 	 * This method deliveres a String with the URL to the digital asset asked for.
 	 */
 	/*	 
 	 
 	public String getAssetUrl(Integer contentId, String assetKey) 
 	{
 		return decorateTag(this.getContentId(), this.getLanguageId(), assetKey, super.getAssetUrl(contentId, assetKey));
 	}
 */
 
 	/**
 	 * This method deliveres a String with the URL to the digital asset asked for.
 	 * As the siteNode can have multiple bindings as well as a content as a parameter this
 	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
 	 */
 	/*	 
 	 
 	public String getAssetUrl(String contentBindningName, int index) 
 	{
 		return decorateTag(this.getContentId(), this.getLanguageId(), "", super.getAssetUrl(contentBindningName, index));
 	}
 */
 
 	/**
 	 * This method deliveres a String with the URL to the digital asset asked for.
 	 * As the siteNode can have multiple bindings as well as a content as a parameter this
 	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
 	 */
 	/*	 
 	 
 	public String getAssetUrl(String contentBindningName, String assetKey) 
 	{
 		return decorateTag(this.getContentId(), this.getLanguageId(), assetKey, super.getAssetUrl(contentBindningName, assetKey));
 	}
 */	
 	
 	/**
 	 * This method should be much more sophisticated later and include a check to see if there is a 
 	 * digital asset uploaded which is more specialized and can be used to act as serverside logic to the template.
 	 */
 	
 	public TemplateController getTemplateController(Integer siteNodeId, Integer languageId, Integer contentId, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
 	{
 		return getTemplateController(siteNodeId, languageId, contentId, this.request, infoGluePrincipal);
 	}	
 	
 	public TemplateController getTemplateController(Integer siteNodeId, Integer languageId, Integer contentId, HttpServletRequest request, InfoGluePrincipal infoGluePrincipal) throws SystemException, Exception
 	{
 		TemplateController templateController = null;
 		templateController = new EditOnSiteBasicTemplateController(this.databaseWrapper, infoGluePrincipal);
 		templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
 		templateController.setHttpRequest(request);	
 		templateController.setBrowserBean(this.browserBean);
 		templateController.setDeliveryControllers(this.nodeDeliveryController, null, this.integrationDeliveryController);	
 		return templateController;		
 	}
 	
 
 }
