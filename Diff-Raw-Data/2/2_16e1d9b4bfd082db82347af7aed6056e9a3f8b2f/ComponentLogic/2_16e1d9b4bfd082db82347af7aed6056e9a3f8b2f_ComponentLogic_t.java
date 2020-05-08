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
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.XMLHelper;
 import org.infoglue.deliver.applications.actions.InfoGlueComponent;
 import org.infoglue.deliver.applications.databeans.ComponentDeliveryContext;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.applications.databeans.Slot;
 import org.infoglue.deliver.applications.databeans.WebPage;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.Support;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class ComponentLogic 
 {
     private final static Logger logger = Logger.getLogger(ComponentLogic.class.getName());
 
 	private TemplateController templateController = null;
 	private InfoGlueComponent infoGlueComponent = null;
 	private boolean useInheritance = true;
 	private boolean useEditOnSight = true;
 	private boolean threatFoldersAsContents = false;
 	private ComponentDeliveryContext componentDeliveryContext;
 	
  	public ComponentLogic(TemplateController templateController, InfoGlueComponent infoGlueComponent)
  	{
  		this.templateController = templateController;
  		this.infoGlueComponent 	= infoGlueComponent;
  		this.componentDeliveryContext = ComponentDeliveryContext.getComponentDeliveryContext(templateController.getDeliveryContext(), infoGlueComponent);
  		this.componentDeliveryContext.addUsedContent("content_" + infoGlueComponent.getContentId());
  	}
  	
  	/*
 	public void getDatabaseStatus(String debug)
 	{
 		try
 		{
 		    this.templateController.getDatabaseStatus(debug);
 		}
 		catch(Exception e) 
 		{
 		    e.printStackTrace();
 		}
 	}
 	*/
 
 	/**
 	 * The method returns a list of ContentVO-objects that is children to the bound content of named binding on the siteNode sent in. 
 	 * The method is great for collection-pages on any site where you want to bind to a folder containing all contents to list.
 	 * You can also state if the method should recurse into subfolders and how the contents should be sorted.
 	 * The recursion only deals with three levels at the moment for performance-reasons. 
 	 */
 	
 	public List getBoundFolderContents(String propertyName, boolean searchRecursive, String sortAttribute, String sortOrder) throws Exception
 	{
 	    return getBoundFolderContents(propertyName, searchRecursive, sortAttribute, sortOrder, false);
 	}
 
 	/**
 	 * The method returns a list of ContentVO-objects that is children to the bound content of named binding on the siteNode sent in. 
 	 * The method is great for collection-pages on any site where you want to bind to a folder containing all contents to list.
 	 * You can also state if the method should recurse into subfolders and how the contents should be sorted.
 	 * The recursion only deals with three levels at the moment for performance-reasons. 
 	 */
 	
 	public List getBoundFolderContents(String propertyName, boolean searchRecursive, String sortAttribute, String sortOrder, boolean includeFolders) throws Exception
 	{
 		List childContents = new ArrayList();
 		
 		//Map property = this.getComponentProperty(propertyName);
 		//if(property != null)
 		//{
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				childContents = this.templateController.getChildContents(contentId, searchRecursive, sortAttribute, sortOrder, includeFolders);
 			}
 		}
 		
 		return childContents;
 	}
 
 	/**
 	 * The method returns a list of ContentVO-objects that are related to the category of named binding on the siteNode sent in.
 	 * The method is great for collection-pages on any site where you want to bind a category.
 	 */
 	public List getBoundCategoryContents(String categoryAttribute, String typeAttribute) throws Exception
 	{
 		Map categoryComponent = getInheritedComponentProperty(infoGlueComponent, categoryAttribute, this.useInheritance);
 		Map attributeComponent = getInheritedComponentProperty(infoGlueComponent, typeAttribute, this.useInheritance);
 		if(categoryComponent != null && attributeComponent != null)
 		{
 			String attr = (String)attributeComponent.get("path");
 			Integer categoryId = getSingleBindingAsInteger(categoryComponent);
 			final List contentVersionsByCategory = templateController.getContentVersionsByCategory(categoryId, attr);
 			return contentVersionsByCategory;
 		}
 
 		return Collections.EMPTY_LIST;
 	}
 
 	private Integer getSingleBindingAsInteger(Map componentProperty)
 	{
 		List bindings = (List)componentProperty.get("bindings");
 		return (bindings.size() > 0)
 			   		? new Integer((String)bindings.get(0))
 			   		: new Integer(0);
 	}
 
 	public String getAssetUrl(String propertyName) throws Exception
 	{
 		String assetUrl = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			logger.info("bindings:" + bindings.size());
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				logger.info("contentId:" + contentId);
 				assetUrl = templateController.getAssetUrl(contentId);
 			}
 		}
 		return assetUrl;
 	}
 	
 	public String getAssetUrl(String propertyName, boolean useInheritance) throws Exception
 	{
 		String assetUrl = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			logger.info("bindings:" + bindings.size());
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				logger.info("contentId:" + contentId);
 				assetUrl = templateController.getAssetUrl(contentId);
 			}
 		}
 		return assetUrl;
 	}
 
 	public String getAssetUrl(String propertyName, String assetKey) throws Exception
 	{
 		String assetUrl = "";
 		 		
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				assetUrl = templateController.getAssetUrl(contentId, assetKey);
 			}
 		}
 		return assetUrl;
 	}
 
 	public String getAssetUrl(String propertyName, String assetKey, boolean useInheritance) throws Exception
 	{
 		String assetUrl = "";
 		 		
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				assetUrl = templateController.getAssetUrl(contentId, assetKey);
 			}
 		}
 		return assetUrl;
 	}
 
 	public String getAssetThumbnailUrl(String propertyName, int width, int height) throws Exception
 	{
 	    return getAssetThumbnailUrl(propertyName, width, height, this.useInheritance);
 	}
 	
 	public String getAssetThumbnailUrl(String propertyName, int width, int height, boolean useInheritance) throws Exception
 	{
 		String assetUrl = "";
 		 		
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				assetUrl = templateController.getAssetThumbnailUrl(contentId, width, height);
 			}
 		}
 		return assetUrl;
 	}
 
  	public String getAssetThumbnailUrl(String propertyName, String assetKey, int width, int height) throws Exception
  	{
  	   return getAssetThumbnailUrl(propertyName, assetKey, width, height, this.useInheritance);
  	}
  	
 	public String getAssetThumbnailUrl(String propertyName, String assetKey, int width, int height, boolean useInheritance) throws Exception
  	{
 		String assetUrl = "";
 
 		try
 		{
 			Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 			if(property != null)
 			{	
 				List bindings = (List)property.get("bindings");
 				if(bindings.size() > 0)
 				{
 					Integer contentId = new Integer((String)bindings.get(0));
 					assetUrl = templateController.getAssetThumbnailUrl(contentId, assetKey, width, height);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return assetUrl;
  	}
 
 	public String getAssetThumbnailUrl(Integer contentId, int width, int height) throws Exception
 	{
 		String assetUrl = templateController.getAssetThumbnailUrl(contentId, width, height);
 		return assetUrl;
 	}
 
 	public String getAssetThumbnailUrl(Integer contentId, String assetKey, int width, int height) throws Exception
 	{
 		String assetUrl = templateController.getAssetThumbnailUrl(contentId, assetKey, width, height);
 		return assetUrl;
 	}
 
  	
 	public String getAssetUrl(Integer contentId, String assetKey)
 	{
 		String assetUrl = templateController.getAssetUrl(contentId, assetKey);
 
 		return assetUrl;
 	}
 	
 	public String getContentAttribute(String propertyName, String attributeName)
 	{
 	    return getContentAttribute(propertyName, attributeName, !this.useEditOnSight, this.useInheritance);
 	}
 
 	public String getContentAttribute(String propertyName, String attributeName, boolean disableEditOnSight)
 	{
 	    return getContentAttribute(propertyName, attributeName, disableEditOnSight, this.useInheritance);
 	}
 
 	/*
 	public String getContentAttribute(String propertyName, String attributeName)
 	{
 		String attributeValue = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				attributeValue = templateController.getContentAttribute(contentId, attributeName);
 			}
 		}
 
 		return attributeValue;
 	}
 	*/
 	
 	public Integer getContentId(Map property)
 	{
 	    Integer contentId = null;
 
 	    if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				contentId = new Integer((String)bindings.get(0));
 			}
 		}
 
 		return contentId;
 	}
 
 	public List getBoundContents(Map property)
 	{
 	    List contents = new ArrayList();
 
 	    if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			Iterator bindingsIterator = bindings.iterator();
 			while(bindingsIterator.hasNext())
 			{
 				Integer contentId = new Integer((String)bindingsIterator.next());
 				contents.add(this.templateController.getContent(contentId));
 			}
 		}
 
 		return contents;
 	}
 
 	public Integer getSiteNodeId(Map property)
 	{
 	    Integer siteNodeId = null;
 
 	    if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 			    siteNodeId = new Integer((String)bindings.get(0));
 			}
 		}
 
 		return siteNodeId;
 	}
 
 	public List getBoundPages(Map property)
 	{
 		List pages = new ArrayList();
 
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			Iterator bindingsIterator = bindings.iterator();
 			while(bindingsIterator.hasNext())
 			{
 				Integer siteNodeId = new Integer((String)bindingsIterator.next());
 				WebPage webPage = new WebPage();						
 				webPage.setSiteNodeId(siteNodeId);
 				webPage.setLanguageId(templateController.getLanguageId());
 				webPage.setContentId(null);
 				webPage.setNavigationTitle(getPageNavTitle(siteNodeId));
 				webPage.setMetaInfoContentId(templateController.getContentId(siteNodeId, DeliveryContext.META_INFO_BINDING_NAME));
 				webPage.setUrl(getPageUrl(siteNodeId));
 				pages.add(webPage);
 			}
 		}
 
 		return pages;
 	}
 
 	
 	public String getContentAttribute(String propertyName, String attributeName, boolean disableEditOnSight, boolean useInheritance)
 	{
 		String attributeValue = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				if(disableEditOnSight)
 				    attributeValue = templateController.getContentAttribute(contentId, attributeName, disableEditOnSight);
 				else
 				    attributeValue = templateController.getContentAttribute(contentId, attributeName);
 			}
 		}
 
 		return attributeValue;
 	}
 
 	public String getContentAttribute(String propertyName, Integer languageId, String attributeName, boolean disableEditOnSight, boolean useInheritance)
 	{
 		String attributeValue = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				attributeValue = templateController.getContentAttribute(contentId, languageId, attributeName, disableEditOnSight);
 			}
 		}
 
 		return attributeValue;
 	}
 
 	public String getParsedContentAttribute(String propertyName, String attributeName)
 	{
 		return getParsedContentAttribute(propertyName, attributeName, !this.useEditOnSight, this.useInheritance);
 	}
 
 	public String getParsedContentAttribute(String propertyName, String attributeName, boolean disableEditOnSight, boolean useInheritance)
 	{
 		String attributeValue = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				attributeValue = templateController.getParsedContentAttribute(contentId, attributeName, disableEditOnSight);
 			}
 		}
 
 		return attributeValue;
 	}
 
 	public String getParsedContentAttribute(String propertyName, Integer languageId, String attributeName, boolean disableEditOnSight, boolean useInheritance)
 	{
 		String attributeValue = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				attributeValue = templateController.getParsedContentAttribute(contentId, languageId, attributeName, disableEditOnSight);
 			}
 		}
 
 		return attributeValue;
 	}
 
 	public List getFormAttributes(String propertyName, String attributeName)
 	{
 		List formAttributes = new ArrayList();
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				String formDefinition = templateController.getContentAttribute(contentId, attributeName, true);
 				formAttributes = FormDeliveryController.getFormDeliveryController().getContentTypeAttributes(formDefinition);
 			}
 		}
 
 		return formAttributes;
 	}
 	
 	public String getPropertyValue(String propertyName) throws SystemException
 	{
 		return getPropertyValue(propertyName, true);
 	}
 
 	public String getPropertyValue(String propertyName, boolean useLangaugeFallback) throws SystemException
 	{
 		return getPropertyValue(propertyName, useLangaugeFallback, this.useInheritance);
 	}
 
 	public String getPropertyValue(String propertyName, boolean useLangaugeFallback, boolean useInheritance) throws SystemException
 	{
 		String propertyValue = "";
 
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), templateController.getLanguageId());
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			if(property != null)
 			{
 				propertyValue = (String)property.get("path");
 				if(propertyValue == null)
 				{
 					Iterator keysIterator = property.keySet().iterator();
 					while(keysIterator.hasNext())
 					{
 						String key = (String)keysIterator.next();
 					}
 				}
 			}
 		}
 
 		return propertyValue;
 	}
 
 
 	
 	public String getPropertyValue(Integer siteNodeId, String propertyName, boolean useLangaugeFallback, boolean useInheritance) throws SystemException
 	{
 		String propertyValue = "";
 
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), templateController.getLanguageId());
 
 		Map property = getInheritedComponentProperty(siteNodeId, this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			if(property != null)
 			{
 				propertyValue = (String)property.get("path");
 				if(propertyValue == null)
 				{
 					Iterator keysIterator = property.keySet().iterator();
 					while(keysIterator.hasNext())
 					{
 						String key = (String)keysIterator.next();
 					}
 				}
 			}
 		}
 
 		return propertyValue;
 	}
 
 
 	
 	public ContentVO getBoundContent(String propertyName)
 	{
 	    return getBoundContent(propertyName, true);
 	}
 	
 	public ContentVO getBoundContent(String propertyName, boolean useInheritance)
 	{
 		ContentVO content = null;
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				content = this.templateController.getContent(contentId);
 			}
 		}
 
 		return content;
 	}
 
 	public ContentVO getBoundContent(Integer siteNodeId, String propertyName, boolean useInheritance)
 	{
 		ContentVO content = null;
 
 		Map property = getInheritedComponentProperty(siteNodeId, this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				content = this.templateController.getContent(contentId);
 			}
 		}
 
 		return content;
 	}
 
 	public Integer getBoundContentId(String propertyName)
 	{
 	    return getBoundContentId(propertyName, true);
 	}
 	
 	public Integer getBoundContentId(String propertyName, boolean useInheritance)
 	{
 		Integer contentId = null;
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				contentId = new Integer((String)bindings.get(0));
 			}
 		}
 
 		return contentId;
 	}
 
 	public List getBoundContents(String propertyName)
 	{
 	    return getBoundContents(propertyName, this.useInheritance);
 	}
 	
 	public List getBoundContents(String propertyName, boolean useInheritance)
 	{
 		List contents = new ArrayList();
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			Iterator bindingsIterator = bindings.iterator();
 			while(bindingsIterator.hasNext())
 			{
 				Integer contentId = new Integer((String)bindingsIterator.next());
 				contents.add(this.templateController.getContent(contentId));
 			}
 		}
 
 		return contents;
 	}
 	
 	public WebPage getBoundPage(String propertyName)
 	{
 	    return getBoundPage(propertyName, this.useInheritance);
 	}
 	
 	/**
 	 * This method returns a page bound to the component.
 	 */
 
 	public WebPage getBoundPage(String propertyName, boolean useInheritance)
 	{
 		WebPage webPage = null;
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			Iterator bindingsIterator = bindings.iterator();
 			if(bindingsIterator.hasNext())
 			{
 			    webPage = new WebPage();
 				Integer siteNodeId = new Integer((String)bindingsIterator.next());
 				webPage.setSiteNodeId(siteNodeId);
 				webPage.setLanguageId(templateController.getLanguageId());
 				webPage.setContentId(null);
 				webPage.setNavigationTitle(getPageNavTitle(siteNodeId));
 				webPage.setMetaInfoContentId(templateController.getContentId(siteNodeId, DeliveryContext.META_INFO_BINDING_NAME));
 				webPage.setUrl(getPageUrl(siteNodeId));
 			}
 		}
 
 		return webPage;
 	}
 
 	
 	public List getBoundPages(String propertyName)
 	{
 	    return getBoundPages(propertyName, this.useInheritance);
 	}
 	
 	/**
 	 * This method returns a list of pages bound to the component.
 	 */
 
 	public List getBoundPages(String propertyName, boolean useInheritance)
 	{
 		List pages = new ArrayList();
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			Iterator bindingsIterator = bindings.iterator();
 			while(bindingsIterator.hasNext())
 			{
 				Integer siteNodeId = new Integer((String)bindingsIterator.next());
 				WebPage webPage = new WebPage();						
 				webPage.setSiteNodeId(siteNodeId);
 				webPage.setLanguageId(templateController.getLanguageId());
 				webPage.setContentId(null);
 				webPage.setNavigationTitle(getPageNavTitle(siteNodeId));
 				webPage.setMetaInfoContentId(templateController.getContentId(siteNodeId, DeliveryContext.META_INFO_BINDING_NAME));
 				webPage.setUrl(getPageUrl(siteNodeId));
 				pages.add(webPage);
 			}
 		}
 
 		return pages;
 	}
 
 	/**
 	 * This method returns a list of pages bound to the component.
 	 */
 
 	public SiteNodeVO getBoundSiteNode(String propertyName, boolean useInheritance)
 	{
 	    SiteNodeVO siteNodeVO = null;
 	    
 	    Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			Iterator bindingsIterator = bindings.iterator();
 			if(bindingsIterator.hasNext())
 			{
 				Integer siteNodeId = new Integer((String)bindingsIterator.next());
 				siteNodeVO = templateController.getSiteNode(siteNodeId);
 			}
 		}
 
 		return siteNodeVO;
 	}
 
 	/**
 	 * This method returns a list of pages bound to the component on the given siteNode.
 	 */
 
 	public SiteNodeVO getBoundSiteNode(Integer targetSiteNodeId, String propertyName, boolean useInheritance)
 	{
 	    SiteNodeVO siteNodeVO = null;
 	    
 	    Map property = getInheritedComponentProperty(targetSiteNodeId, this.infoGlueComponent, propertyName, useInheritance);
 		
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			Iterator bindingsIterator = bindings.iterator();
 			if(bindingsIterator.hasNext())
 			{
 				Integer siteNodeId = new Integer((String)bindingsIterator.next());
 				siteNodeVO = templateController.getSiteNode(siteNodeId);
 			}
 		}
 
 		return siteNodeVO;
 	}
 
 	/**
 	 * This method returns a list of childContents using inheritence as default.
 	 */
 
 	public List getChildContents(String propertyName)
 	{
 	    return getChildContents(propertyName, this.useInheritance, false, "id", "asc", false);
 	}
 	
 	/**
 	 * This method returns a list of childcontents.
 	 */
 
 	public List getChildContents(String propertyName, boolean useInheritance, boolean searchRecursive, String sortAttribute, String sortOrder, boolean includeFolders)
 	{
 	    List childContents = new ArrayList();
 	    
 	    Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			Iterator bindingsIterator = bindings.iterator();
 			while(bindingsIterator.hasNext())
 			{
 				Integer contentId = new Integer((String)bindingsIterator.next());
 				childContents.addAll(this.templateController.getChildContents(contentId, searchRecursive, sortAttribute, sortOrder, includeFolders));
 			}
 		}	
 		return childContents;
 	}
 
 	
 	/**
 	 * This method returns a list of childpages using inheritence as default.
 	 */
 
 	public List getChildPages(String propertyName)
 	{
 	    return getChildPages(propertyName, this.useInheritance);
 	}
 
 	public List getChildPages(String propertyName, boolean useInheritance)
 	{
 	    return getChildPages(propertyName, useInheritance, false, false);
 	}
 
 	public List getChildPages(String propertyName, boolean useInheritance, boolean escapeHTML)
 	{
 	    return getChildPages(propertyName, this.useInheritance, escapeHTML, false);
 	}
 
 	/**
 	 * This method returns a list of childpages.
 	 */
 
 	public List getChildPages(String propertyName, boolean useInheritance, boolean escapeHTML, boolean hideUnauthorizedPages)
 	{
 	    List childPages = new ArrayList();
 	    
 	    Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			Iterator bindingsIterator = bindings.iterator();
 			while(bindingsIterator.hasNext())
 			{
 				Integer siteNodeId = new Integer((String)bindingsIterator.next());
 				childPages.addAll(getChildPages(siteNodeId));
 			}
 		}	
 		return childPages;
 	}
 
 	
 	/**
 	 * This method returns a list of childpages.
 	 */
 
 	public List getChildPages(Integer siteNodeId)
 	{
 		List pages = templateController.getChildPages(siteNodeId);
 
 		Iterator pagesIterator = pages.iterator();
 		while(pagesIterator.hasNext())
 		{
 			WebPage webPage = (WebPage)pagesIterator.next();
 			webPage.setUrl(getPageUrl(webPage.getSiteNodeId()));
 		}
 	
 		return pages;
 	}
 
 	public String getPageUrl(String propertyName) throws Exception
 	{
 	    return getPageUrl(propertyName, this.useInheritance);
 	}
 
 	public String getPageUrl(String propertyName, boolean useInheritance)
 	{
 		String pageUrl = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer siteNodeId = new Integer((String)bindings.get(0));
 				pageUrl = this.getPageUrl(siteNodeId, templateController.getLanguageId(), templateController.getContentId());
 			}
 		}
 		
 		return pageUrl;		
 	}
 	
 	public String getPageUrl(Integer siteNodeId)
 	{
 		String pageUrl = "";
 
 		pageUrl = this.getPageUrl(siteNodeId, templateController.getLanguageId(), null);
 
 		return pageUrl;
 	}
 
 	public String getPageUrl(String propertyName, Integer contentId)
 	{
 	    return getPageUrl(propertyName, contentId, this.useInheritance);
 	}
 	
 	public String getPageUrl(String propertyName, Integer contentId, boolean useInheritance)
 	{
 		String pageUrl = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer siteNodeId = new Integer((String)bindings.get(0));
 				pageUrl = this.getPageUrl(siteNodeId, templateController.getLanguageId(), contentId);
 			}
 		}
 
 		return pageUrl;
 	}
 
 	public String getPageUrl(String propertyName, Integer contentId, Integer languageId, boolean useInheritance)
 	{
 		String pageUrl = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer siteNodeId = new Integer((String)bindings.get(0));
 				pageUrl = this.getPageUrl(siteNodeId, languageId, contentId);
 			}
 		}
 
 		return pageUrl;
 	}
 
 	public String getPageNavTitle(String propertyName)
 	{
 		String pageUrl = "";
 
 		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer siteNodeId = new Integer((String)bindings.get(0));
 				pageUrl = templateController.getPageNavTitle(siteNodeId);
 			}
 		}
 		
 		return pageUrl;
 	}
 	
 	public List getRelatedPages(String propertyName, String attributeName)
 	{
 	    List relatedPages = new ArrayList();
 	    
 	    Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance);
 		if(property != null)
 		{	
 			List bindings = (List)property.get("bindings");
 			if(bindings.size() > 0)
 			{
 				Integer contentId = new Integer((String)bindings.get(0));
 				relatedPages = templateController.getRelatedPages(contentId, attributeName);
 			}
 		}
 		
 		return relatedPages;
 	}
 	
 	/**
 	 * This method gets a property from the component and if not found there checks in parent components.
 	 */
 
 	public Map getInheritedComponentProperty(InfoGlueComponent component, String propertyName, boolean useInheritance)
 	{
 	    try
 		{
 			Map property1 = getComponentProperty(propertyName, useInheritance);
 			if(property1 != null)
 				return property1;
 				
 			Map property = (Map)component.getProperties().get(propertyName);
 			InfoGlueComponent parentComponent = component.getParentComponent();
 			//logger.info("parentComponent: " + parentComponent);
 			while(property == null && parentComponent != null)
 			{
 				property = (Map)parentComponent.getProperties().get(propertyName);
 				parentComponent = parentComponent.getParentComponent();
 			}
 			
 			return property;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 
 	/**
 	 * This method gets a property from the component and if not found there checks in parent components.
 	 */
 
 	public Map getInheritedComponentProperty(Integer siteNodeId, InfoGlueComponent component, String propertyName, boolean useInheritance)
 	{
 	    try
 		{
 			Map property1 = getComponentProperty(siteNodeId, propertyName, useInheritance);
 			if(property1 != null)
 				return property1;
 			/*	
 			Map property = (Map)component.getProperties().get(propertyName);
 			InfoGlueComponent parentComponent = component.getParentComponent();
 			//logger.info("parentComponent: " + parentComponent);
 			while(property == null && parentComponent != null)
 			{
 				property = (Map)parentComponent.getProperties().get(propertyName);
 				parentComponent = parentComponent.getParentComponent();
 			}
 			*/
 			
 			return null;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 
 	/**
 	 * This method gets a property from the component and if not found there checks in parent components.
 	 */
 
 	public List getInheritedComponentProperties(String propertyName, boolean useInheritance)
 	{
 		return getInheritedComponentProperties(this.templateController.getSiteNodeId(), propertyName, useInheritance);
 	}
 
 	/**
 	 * This method gets a property from the component and if not found there checks in parent components.
 	 */
 
 	public List getInheritedComponentProperties(Integer siteNodeId, String propertyName, boolean useInheritance)
 	{
 	    try
 		{
 			List properties = getComponentProperties(siteNodeId, propertyName, useInheritance);
 			if(properties != null)
 				return properties;
 			
 			return null;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 
 	/**
 	 * This method gets if a property is defined and available in the given page.
 	 */
 
 	public boolean getHasDefinedProperty(Integer siteNodeId, Integer languageId, String propertyName, boolean useInheritance)
 	{
 	    Map property = getComponentProperty(siteNodeId, languageId, propertyName, useInheritance);
 	    
 	    return property == null ? false : true;
 	}   
 
 	
 	/**
 	 * This method gets a property from the sitenode given and also looks recursively upwards.
 	 */
 	
 	public Map getComponentProperty(Integer siteNodeId, Integer languageId, String propertyName, boolean useInheritance)
 	{
 	    Map componentProperty = getComponentProperty(siteNodeId, languageId, propertyName);
 	    SiteNodeVO parentSiteNodeVO = this.templateController.getParentSiteNode(siteNodeId);
 	    while(componentProperty == null && useInheritance && parentSiteNodeVO != null)
 	    {
 	        componentProperty = getComponentProperty(parentSiteNodeVO.getId(), languageId, propertyName);
 	        parentSiteNodeVO = this.templateController.getParentSiteNode(parentSiteNodeVO.getId());
 	    }
 	    
 	    return componentProperty;
 	}
 
 	    
 	/**
 	 * This method gets a property from the sitenode given	.
 	 */
 
 	public Map getComponentProperty(Integer siteNodeId, Integer languageId, String propertyName)
 	{
 	    try
 		{
 		    List contentVersionIdList = new ArrayList();
 
 	        String componentPropertiesXML = getPageComponentsString(this.templateController, siteNodeId, languageId, new Integer(-1), contentVersionIdList);
 	        logger.info("componentPropertiesXML:" + componentPropertiesXML);
 	        
 		    String key = "" + siteNodeId + "_" + languageId + "_" + propertyName;
 			Map property = (Map)CacheController.getCachedObjectFromAdvancedCache("componentPropertyCache", key);
 			
 			if(property != null)
 			{
 				logger.info("There was an cached property:" + key + ":" + property);
 			    //System.out.println("Returning cached property...");
 			}
 			else
 			{
 			    logger.info("Have to fetch property from XML...:" + key);
 	        
 		        //HashMap property = null;
 				
 				if(componentPropertiesXML != null && componentPropertiesXML.length() > 0)
 				{
 					Document document = XMLHelper.readDocumentFromByteArray(componentPropertiesXML.getBytes("UTF-8"));
 					String propertyXPath = "//component/properties/property[@name='" + propertyName + "']";
 					//logger.info("propertyXPath:" + propertyXPath);
 					NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), propertyXPath);
 					//logger.info("*********************************************************anl:" + anl.getLength());
 									
 					for(int i=0; i < anl.getLength(); i++)
 					{
 						Element propertyElement = (Element)anl.item(i);
 	
 						String name		= propertyElement.getAttribute("name");
 						String type		= propertyElement.getAttribute("type");
 						String entity 	= propertyElement.getAttribute("entity");
 						boolean isMultipleBinding = new Boolean(propertyElement.getAttribute("multiple")).booleanValue();
 						
 						String value = null;
 						
 						if(type.equalsIgnoreCase("textfield") || type.equalsIgnoreCase("textarea") || type.equalsIgnoreCase("select"))
 						{
 						    value = propertyElement.getAttribute("path");
 	
 						    Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
 	
 						    if(propertyElement.hasAttribute("path_" + locale.getLanguage()))
 							    value = propertyElement.getAttribute("path_" + locale.getLanguage());
 						}
 						
 						property = new HashMap();
 						property.put("name", name);
 						property.put("path", value);
 						property.put("type", type);
 						
 						List bindings = new ArrayList();
 						NodeList bindingNodeList = propertyElement.getElementsByTagName("binding");
 						//logger.info("bindingNodeList:" + bindingNodeList.getLength());
 						for(int j=0; j < bindingNodeList.getLength(); j++)
 						{
 							Element bindingElement = (Element)bindingNodeList.item(j);
 							String entityName = bindingElement.getAttribute("entity");
 							String entityId = bindingElement.getAttribute("entityId");
 							//logger.info("Binding found:" + entityName + ":" + entityId);
 							if(entityName.equalsIgnoreCase("Content"))
 							{
 								//logger.info("Content added:" + entityName + ":" + entityId);
 								bindings.add(entityId);
 							}
 							else
 							{
 								//logger.info("SiteNode added:" + entityName + ":" + entityId);
 								bindings.add(entityId); 
 							} 
 						}
 	
 						property.put("bindings", bindings);
 					}
 					
 					if(property != null && contentVersionIdList.size() > 0)
 			        {
 					    //System.out.println("Caching property: " + contentVersionIdList.get(0) + ":" + property);
 					    
 					    Integer contentVersionId = (Integer)contentVersionIdList.get(0);
 					    if(contentVersionId != null)
 					    {
 						    ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionId, this.templateController.getDatabase());
 						    logger.info("Caching property:" + key);
 					        CacheController.cacheObjectInAdvancedCache("componentPropertyCache", key, property, new String[]{"contentVersion_" + contentVersionId, "content_" + contentVersion.getValueObject().getContentId()}, true);
 					    }
 			        }
 				}							
 			}
 			
 			return property;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 
 	/**
 	 * This method returns a url to the given page. The url is composed of siteNode, language and content
 	 * TODO - temporary dev solution
 	 */
 
 	public String getPageUrl(Integer siteNodeId, Integer languageId, Integer contentId)
 	{
 		String pageUrl = this.templateController.getPageUrl(siteNodeId, languageId, contentId);
 		
 		return pageUrl;
 	}
 
 		
 	public String getPageNavTitle(Integer siteNodeId)
 	{
 		String navTitle = "";
 
 		navTitle = templateController.getPageNavTitle(siteNodeId);
 	
 		return navTitle;
 	}
 	
 	
 	/**
 	 * This method fetches the component named component property. If not available on the current page metainfo we go up recursive.
 	 */
 	
 	private Map getComponentProperty(String propertyName, boolean useInheritance) throws Exception
 	{
 		Map property = (Map)this.infoGlueComponent.getProperties().get(propertyName);
 		
 		if(useInheritance)
 		{
 			try
 			{
 			    NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(this.templateController.getSiteNodeId(), this.templateController.getLanguageId(), this.templateController.getContentId());
 			
 				SiteNodeVO parentSiteNodeVO = nodeDeliveryController.getSiteNode(templateController.getDatabase(), this.templateController.getSiteNodeId()).getValueObject();
 			    while(property == null && parentSiteNodeVO != null)
 				{
 				    property = getInheritedComponentProperty(this.templateController, parentSiteNodeVO.getId(), this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName);
 					
 				    SiteNodeVO newParentSiteNodeVO = nodeDeliveryController.getParentSiteNode(templateController.getDatabase(), parentSiteNodeVO.getId());
 				
 				    if(newParentSiteNodeVO == null)
 					{
 					    Integer parentRepositoryId = this.templateController.getParentRepositoryId(parentSiteNodeVO.getRepositoryId());
 					    logger.info("parentRepositoryId:" + parentRepositoryId);
 					    if(parentRepositoryId != null)
 					    {
 					        newParentSiteNodeVO = this.templateController.getRepositoryRootSiteNode(parentRepositoryId);
 						}
 					}
 					
 					parentSiteNodeVO = newParentSiteNodeVO;
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 
         //logger.info("Done..." + propertyName);
 
 		return property;
 	}
 
 	/**
 	 * This method fetches the component named component property. If not available on the sent in page metainfo we go up recursive.
 	 */
 	
 	private Map getComponentProperty(Integer siteNodeId, String propertyName, boolean useInheritance) throws Exception
 	{
 		//Map property = (Map)this.infoGlueComponent.getProperties().get(propertyName);
 		//logger.info("property1:" + property);
 		Map property = getInheritedComponentProperty(this.templateController, siteNodeId, this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName);
 		
 		if(useInheritance)
 		{
 			try
 			{
 			    NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.templateController.getLanguageId(), this.templateController.getContentId());
 			
 				SiteNodeVO parentSiteNodeVO = nodeDeliveryController.getSiteNode(templateController.getDatabase(), siteNodeId).getValueObject();
 				while(property == null && parentSiteNodeVO != null)
 				{
 				    property = getInheritedComponentProperty(this.templateController, parentSiteNodeVO.getId(), this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName);
 					
 				    SiteNodeVO newParentSiteNodeVO = nodeDeliveryController.getParentSiteNode(templateController.getDatabase(), parentSiteNodeVO.getId());
 				
 				    if(newParentSiteNodeVO == null)
 					{
 					    Integer parentRepositoryId = this.templateController.getParentRepositoryId(parentSiteNodeVO.getRepositoryId());
 					    logger.info("parentRepositoryId:" + parentRepositoryId);
 					    if(parentRepositoryId != null)
 					    {
 					        newParentSiteNodeVO = this.templateController.getRepositoryRootSiteNode(parentRepositoryId);
 						}
 					}
 					
 					parentSiteNodeVO = newParentSiteNodeVO;
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 
         //logger.info("Done..." + propertyName);
 
 		return property;
 	}
 
 	
 	/**
 	 * This method gets a component property from the parent to the current recursively until found.
 	 */
 	 
 	private Map getInheritedComponentProperty(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId, Integer componentId, String propertyName) throws Exception
 	{
 	    List contentVersionIdList = new ArrayList();
 	    
 	    //logger.info("Checking for property " + propertyName + " on siteNodeId " + siteNodeId);
 		String inheritedPageComponentsXML = getPageComponentsString(templateController, siteNodeId, languageId, contentId, contentVersionIdList);
 		//logger.info("inheritedPageComponentsXML:" + inheritedPageComponentsXML);
 		
 	    String key = "" + siteNodeId + "_" + languageId + "_" + propertyName;
 		Map property = (Map)CacheController.getCachedObjectFromAdvancedCache("componentPropertyCache", key);
 		
 		if(property != null)
 		{
 			//getLogger().info("There was an cached content attribute:" + attribute);
 		    //System.out.println("Returning cached property...");
 		}
 		else
 		{
 		    //System.out.println("Have to fetch property from XML...:" + contentVersionIdList.size());
         
 			//HashMap property = null;
 			
 			if(inheritedPageComponentsXML != null && inheritedPageComponentsXML.length() > 0)
 			{
 				Document document = XMLHelper.readDocumentFromByteArray(inheritedPageComponentsXML.getBytes("UTF-8"));
 				String propertyXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + propertyName + "']";
 				//logger.info("propertyXPath:" + propertyXPath);
 				//logger.info("propertyXPath:" + propertyXPath);
 				NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), propertyXPath);
 				//logger.info("*********************************************************anl:" + anl.getLength());
 				
 				//If not found on the same component id - let's check them all and use the first we find.
 				if(anl == null || anl.getLength() == 0)
 				{
 					String globalPropertyXPath = "//component/properties/property[@name='" + propertyName + "'][1]";
 					//logger.info("globalPropertyXPath:" + globalPropertyXPath);
 					//logger.info("globalPropertyXPath:" + globalPropertyXPath);
 					anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), globalPropertyXPath);
 					//logger.info("anl:" + anl.getLength());
 					//logger.info("*********************************************************anl:" + anl.getLength());
 				}			
 				
 				for(int i=0; i < anl.getLength(); i++)
 				{
 					Element propertyElement = (Element)anl.item(i);
 					//logger.info(XMLHelper.serializeDom(propertyElement, new StringBuffer()));
 					//logger.info("YES - we read the property...");		
 					
 					String name		= propertyElement.getAttribute("name");
 					String type		= propertyElement.getAttribute("type");
 					String entity 	= propertyElement.getAttribute("entity");
 					boolean isMultipleBinding = new Boolean(propertyElement.getAttribute("multiple")).booleanValue();
 					
 					//logger.info("name:" + name);
 					//logger.info("type:" + type);
 					//logger.info("entity:" + entity);
 					//logger.info("isMultipleBinding:" + isMultipleBinding);
 					
 					//logger.info("name:" + name);
 					//logger.info("type:" + type);
 					//logger.info("entity:" + entity);
 					//logger.info("isMultipleBinding:" + isMultipleBinding);
 					
 					String value = null;
 					
 					if(type.equalsIgnoreCase("textfield") || type.equalsIgnoreCase("textarea") || type.equalsIgnoreCase("select"))
 					{
 					    value = propertyElement.getAttribute("path");
 	
 					    Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
 	
 					    if(propertyElement.hasAttribute("path_" + locale.getLanguage()))
 						    value = propertyElement.getAttribute("path_" + locale.getLanguage());
 					}
 					else
 					{
 					    value = getComponentPropertyValue(inheritedPageComponentsXML, componentId, languageId, name);
 					}
 					
 					
 					property = new HashMap();
 					property.put("name", name);
 					//property.put("path", "Inherited");
 					property.put("path", value);
 					property.put("type", type);
 					
 					List bindings = new ArrayList();
 					NodeList bindingNodeList = propertyElement.getElementsByTagName("binding");
 					//logger.info("bindingNodeList:" + bindingNodeList.getLength());
 					for(int j=0; j < bindingNodeList.getLength(); j++)
 					{
 						Element bindingElement = (Element)bindingNodeList.item(j);
 						String entityName = bindingElement.getAttribute("entity");
 						String entityId = bindingElement.getAttribute("entityId");
 						//logger.info("Binding found:" + entityName + ":" + entityId);
 						if(entityName.equalsIgnoreCase("Content"))
 						{
 							//logger.info("Content added:" + entityName + ":" + entityId);
 							bindings.add(entityId);
 						}
 						else
 						{
 							//logger.info("SiteNode added:" + entityName + ":" + entityId);
 							bindings.add(entityId); 
 						} 
 					}
 	
 					property.put("bindings", bindings);
 				}
 			}
 			
 			if(property != null && contentVersionIdList.size() > 0)
 	        {
 			    //System.out.println("Caching property: " + contentVersionIdList.get(0) + ":" + property);
 			    
 			    Integer contentVersionId = (Integer)contentVersionIdList.get(0);
 			    //System.out.println("contentVersionId:" + contentVersionId);
 			    if(contentVersionId != null)
 			    {
 				    ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionId, this.templateController.getDatabase());
 					CacheController.cacheObjectInAdvancedCache("componentPropertyCache", key, property, new String[]{"contentVersion_" + contentVersionId, "content_" + contentVersion.getValueObject().getContentId()}, true);
 			    }
 	        }
 
 		}
 		
 		return property;
 	}
 
 
 	/**
 	 * This method fetches the components named component property. If not available on the sent in page metainfo we go up recursive.
 	 */
 	
 	private List getComponentProperties(Integer siteNodeId, String propertyName, boolean useInheritance) throws Exception
 	{
 		List properties = getInheritedComponentProperties(this.templateController, siteNodeId, this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName);
 		
 		if(useInheritance)
 		{
 			try
 			{
 			    NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.templateController.getLanguageId(), this.templateController.getContentId());
 			
 				SiteNodeVO parentSiteNodeVO = nodeDeliveryController.getSiteNode(templateController.getDatabase(), siteNodeId).getValueObject();
 				while(properties == null || properties.size() == 0 && parentSiteNodeVO != null)
 				{
 					properties = getInheritedComponentProperties(this.templateController, parentSiteNodeVO.getId(), this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName);
 					
 				    SiteNodeVO newParentSiteNodeVO = nodeDeliveryController.getParentSiteNode(templateController.getDatabase(), parentSiteNodeVO.getId());
 				
 				    if(newParentSiteNodeVO == null)
 					{
 					    Integer parentRepositoryId = this.templateController.getParentRepositoryId(parentSiteNodeVO.getRepositoryId());
 					    logger.info("parentRepositoryId:" + parentRepositoryId);
 					    if(parentRepositoryId != null)
 					    {
 					        newParentSiteNodeVO = this.templateController.getRepositoryRootSiteNode(parentRepositoryId);
 						}
 					}
 					
 					parentSiteNodeVO = newParentSiteNodeVO;
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 
 		return properties;
 	}
 
 	
 	/**
 	 * This method gets a component property from the parent to the current recursively until found.
 	 */
 	 
 	private List getInheritedComponentProperties(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId, Integer componentId, String propertyName) throws Exception
 	{
 		List contentVersionIdList = new ArrayList();
 	    
 	    //logger.info("Checking for property " + propertyName + " on siteNodeId " + siteNodeId);
 		String inheritedPageComponentsXML = getPageComponentsString(templateController, siteNodeId, languageId, contentId, contentVersionIdList);
 		//logger.info("inheritedPageComponentsXML:" + inheritedPageComponentsXML);
 		
	    String key = "all_" + siteNodeId + "_" + languageId + "_" + propertyName;
 		List properties = (List)CacheController.getCachedObjectFromAdvancedCache("componentPropertyCache", key);
 		
 		if(properties != null)
 		{
 			//getLogger().info("There was an cached content attribute:" + attribute);
 		    //System.out.println("Returning cached property...");
 		}
 		else
 		{
 			properties = new ArrayList();
 			
 		    //System.out.println("Have to fetch property from XML...:" + contentVersionIdList.size());
         
 			//HashMap property = null;
 			
 			if(inheritedPageComponentsXML != null && inheritedPageComponentsXML.length() > 0)
 			{
 				Document document = XMLHelper.readDocumentFromByteArray(inheritedPageComponentsXML.getBytes("UTF-8"));
 				
 				String globalPropertyXPath = "//component/properties/property[@name='" + propertyName + "']";
 				//logger.info("globalPropertyXPath:" + globalPropertyXPath);
 				//logger.info("globalPropertyXPath:" + globalPropertyXPath);
 				NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), globalPropertyXPath);
 				//logger.info("anl:" + anl.getLength());
 				//logger.info("*********************************************************anl:" + anl.getLength());
 				
 				for(int i=0; i < anl.getLength(); i++)
 				{
 					Element propertyElement = (Element)anl.item(i);
 					//logger.info(XMLHelper.serializeDom(propertyElement, new StringBuffer()));
 					//logger.info("YES - we read the property...");		
 					
 					String name		= propertyElement.getAttribute("name");
 					String type		= propertyElement.getAttribute("type");
 					String entity 	= propertyElement.getAttribute("entity");
 					boolean isMultipleBinding = new Boolean(propertyElement.getAttribute("multiple")).booleanValue();
 					
 					//logger.info("name:" + name);
 					//logger.info("type:" + type);
 					//logger.info("entity:" + entity);
 					//logger.info("isMultipleBinding:" + isMultipleBinding);
 					
 					String value = null;
 					
 					if(type.equalsIgnoreCase("textfield") || type.equalsIgnoreCase("textarea") || type.equalsIgnoreCase("select"))
 					{
 					    value = propertyElement.getAttribute("path");
 	
 					    Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
 	
 					    if(propertyElement.hasAttribute("path_" + locale.getLanguage()))
 						    value = propertyElement.getAttribute("path_" + locale.getLanguage());
 					}
 					else
 					{
 						value = getComponentPropertyValue(propertyElement, languageId, name);
 					}					
 					/*
 					System.out.println("name:" + name);
 					System.out.println("type:" + type);
 					System.out.println("entity:" + entity);
 					System.out.println("value:" + value);
 					System.out.println("isMultipleBinding:" + isMultipleBinding);
 					*/
 					Map property = new HashMap();
 					property.put("name", name);
 					property.put("path", value);
 					property.put("type", type);
 					
 					List bindings = new ArrayList();
 					NodeList bindingNodeList = propertyElement.getElementsByTagName("binding");
 					//logger.info("bindingNodeList:" + bindingNodeList.getLength());
 					for(int j=0; j < bindingNodeList.getLength(); j++)
 					{
 						Element bindingElement = (Element)bindingNodeList.item(j);
 						String entityName = bindingElement.getAttribute("entity");
 						String entityId = bindingElement.getAttribute("entityId");
 						//logger.info("Binding found:" + entityName + ":" + entityId);
 						if(entityName.equalsIgnoreCase("Content"))
 						{
 							//logger.info("Content added:" + entityName + ":" + entityId);
 							bindings.add(entityId);
 						}
 						else
 						{
 							//logger.info("SiteNode added:" + entityName + ":" + entityId);
 							bindings.add(entityId); 
 						} 
 					}
 	
 					property.put("bindings", bindings);
 
 					properties.add(property);
 				}
 			}
 			
 			if(properties != null && contentVersionIdList.size() > 0)
 	        {
 			    //System.out.println("Caching property: " + contentVersionIdList.get(0) + ":" + property);
 			    
 			    Integer contentVersionId = (Integer)contentVersionIdList.get(0);
 			    //System.out.println("contentVersionId:" + contentVersionId);
 			    if(contentVersionId != null)
 			    {
 				    ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionId, this.templateController.getDatabase());
 					CacheController.cacheObjectInAdvancedCache("componentPropertyCache", key, properties, new String[]{"contentVersion_" + contentVersionId, "content_" + contentVersion.getValueObject().getContentId()}, true);
 			    }
 	        }
 
 		}
 		
 		return properties;
 	}
 
 	/**
 	 * This method fetches the template-string.
 	 */
     
 	private String getComponentPropertiesString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{
 		String template = null;
     	
 		try
 		{
 			template = templateController.getContentAttribute(contentId, "ComponentStructure", true);
 
 			if(template == null)
 				throw new SystemException("There was no component properties bound to this page which makes it impossible to render.");	
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 
 		return template;
 	}
 	
 	
 	/**
 	 * This method returns a value for a property if it's set. The value is collected in the
 	 * properties for the page.
 	 */
 	
 	private String getComponentPropertyValue(String componentXML, Integer componentId, Integer languageId, String name) throws Exception
 	{
 		String value = "Undefined";
 		
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
 
 		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
 		//logger.info("componentXPath:" + componentXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		for(int i=0; i < anl.getLength(); i++)
 		{
 			Element property = (Element)anl.item(i);
 			
 			String id 			= property.getAttribute("type");
 			String path 		= property.getAttribute("path");
 			
 			if(property.hasAttribute("path_" + locale.getLanguage()))
 				path = property.getAttribute("path_" + locale.getLanguage());
 			
 			value 				= path;
 		}
 
 		
 		return value;
 	}
 
 	/**
 	 * This method returns a value for a property if it's set. The value is collected in the
 	 * properties for the page.
 	 */
 	
 	private String getComponentPropertyValue(Element property, Integer languageId, String name) throws Exception
 	{
 		String value = "Undefined";
 		
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
 
 		String id = property.getAttribute("type");
 		String path	= property.getAttribute("path");
 		
 		if(property.hasAttribute("path_" + locale.getLanguage()))
 			path = property.getAttribute("path_" + locale.getLanguage());
 		
 		value = path;
 		
 		return value;
 	}
 
 	/**
 	 * This method returns all components which are on slots under the current component.
 	 */
 	
 	public List getChildComponents()
 	{
 	    return getChildComponents(this.getInfoGlueComponent(), null);
 	}
 	
 	/**
 	 * This method returns all components which are on a given slots under the current component.
 	 */
 	
 	public List getChildComponents(String slotId)
 	{
 	    return getChildComponents(this.getInfoGlueComponent(), slotId);
 	}
 	
 	/**
 	 * This method returns all components which are on slots under the current component.
 	 */
 	
 	public List getChildComponents(InfoGlueComponent component, String slotId)
 	{
         List childComponents = new ArrayList();
         
 	    List slotList = component.getSlotList();
         
         Iterator slotListIterator = slotList.iterator();
         while(slotListIterator.hasNext())
         {
             Slot slot = (Slot)slotListIterator.next();
             if(slotId == null || slotId.equalsIgnoreCase(slot.getId()))
             {
                 childComponents.addAll(slot.getComponents());
             }
         }
         
         return childComponents;
 	}
 	
 	/**
 	 * This method fetches the pageComponent structure from the metainfo content.
 	 */
 	    
 	protected String getPageComponentsString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId, List usedContentVersionId) throws SystemException, Exception
 	{ 
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "pageComponentString_" + siteNodeId + "_" + languageId + "_" + contentId;
 		String versionKey 	= cacheKey + "_contentVersionId";
 		String cachedPageComponentsString = (String)CacheController.getCachedObject(cacheName, cacheKey);
 		Integer contentVersionId = (Integer)CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", versionKey);
 		
 		if(cachedPageComponentsString != null)
 		{
 		    //System.out.println("Returning cachedPageComponentsString..");
 		    if(usedContentVersionId != null)
 		        usedContentVersionId.add(contentVersionId);
 		    
 			return cachedPageComponentsString;
 		}
 		
 		String pageComponentsString = null;
     	
 		ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(templateController.getDatabase(), templateController.getPrincipal(), siteNodeId, languageId, true, "Meta information", templateController.getDeliveryContext());
 		
 		if(contentVO == null)
 			throw new SystemException("There was no Meta Information bound to this page which makes it impossible to render.");	
 		
 		Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getId();
 		pageComponentsString = templateController.getContentAttributeWithReturningId(contentVO.getContentId(), masterLanguageId, "ComponentStructure", true, usedContentVersionId);
 		
 		if(pageComponentsString == null)
 			throw new SystemException("There was no Meta Information bound to this page which makes it impossible to render.");	
 				    
 		logger.info("pageComponentsString: " + pageComponentsString);
 	
 		CacheController.cacheObject(cacheName, cacheKey, pageComponentsString);
 		if(usedContentVersionId != null && usedContentVersionId.size() > 0)
 		{
 			Integer tempContentVersionId = (Integer)usedContentVersionId.get(0);
 			ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(tempContentVersionId, this.templateController.getDatabase());
 			//System.out.println("contentVersion:" + contentVersion.getValueObject().getId());
 			CacheController.cacheObjectInAdvancedCache("contentVersionCache", versionKey, tempContentVersionId, new String[]{"contentVersion_" + tempContentVersionId, "content_" + contentVersion.getValueObject().getContentId()}, true);
 		}
 		
 		return pageComponentsString;
 	}
 
 
 	/**
 	 * This method fetches the template-string.
 	 */
     /*
 	private String getPageComponentsString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{
 		String template = null;
     	
 		try
 		{
 			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(templateController.getPrincipal(), siteNodeId, languageId, true, "Meta information");		
 
 			if(contentVO == null)
 				throw new SystemException("There was no metainformation bound to this page which makes it impossible to render.");	
 			
 			template = templateController.getContentAttribute(contentVO.getContentId(), "ComponentStructure", true);
 			//logger.info(template);
 			if(template == null)
 				throw new SystemException("There was no metainformation bound to this page which makes it impossible to render.");	
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 
 		return template;
 	}
 	*/
 
 	/**
 	 * @return Returns the infoGlueComponent.
 	 */
 	
 	public InfoGlueComponent getInfoGlueComponent()
 	{
 		return infoGlueComponent;
 	}
 
     public boolean getUseInheritance()
     {
         return useInheritance;
     }
     
     public void setUseInheritance(boolean useInheritance)
     {
         this.useInheritance = useInheritance;
     }
     
     public boolean getUseEditOnSight()
     {
         return useEditOnSight;
     }
     
     public void setUseEditOnSight(boolean useEditOnSight)
     {
         this.useEditOnSight = useEditOnSight;
     }
     
     public boolean getThreatFoldersAsContents()
     {
         return threatFoldersAsContents;
     }
     
     public void setThreatFoldersAsContents(boolean threatFoldersAsContents)
     {
         this.threatFoldersAsContents = threatFoldersAsContents;
         this.templateController.setThreatFoldersAsContents(threatFoldersAsContents);
     }
     
     public ComponentDeliveryContext getComponentDeliveryContext()
     {
         return componentDeliveryContext;
     }
     
 
     /**
      * Returns a map value from a content attribute bound by a propertie. This
      * method is good for global labels and translations, instead of having
      * property files on disk you have them in the cms.
      * @param propertyName a bound content
      * @param attributeName a attribute in the content
      * @param keyName keyname in the propertyu, the text should be formed as a
      *            standard java property file (key=val)
      * @return The value of the keyname in the content attribute or empty string
      *         if none found.
      * @author Per Jonsson - per.jonsson@it-huset.se
      */
     public String getContentAttributeMapValue( String propertyName, String attributeName, String keyName )
     {
         String mapValue = "";
         try
         {
             mapValue = (String)Support.convertTextToProperties(
                     getContentAttribute( propertyName, attributeName, true, true ) ).get( keyName );
         }
         catch ( Exception e )
         {
             logger.error( "An error occurred trying to get getContentAttributeMapValue ( " + propertyName +", " + attributeName + ", " + keyName + " ) ; "+ e.getMessage(), e );
         }
         if ( mapValue == null )
         {
             mapValue = "";
         }
         return mapValue;
     }
 
     /**
      * Renders a text to a PNG file, the preferences of the text rendering is
      * taken from the deliver.properties. ie. fontrender.fontName=Arial
      * @param text The text to render
      * @return an asseturl to the rendered text or an empty string if something
      *         went wrong
      * @author Per Jonsson - per.jonsson@it-huset.se
      */
     public String getRenderedTextUrl( String text )
     {
         return templateController.getRenderedTextUrl( text, null );
     }
 
     /**
      * Renders a text to a PNG file, the preferences of the text rendering is
      * taken from the bound component content of the property. The content is
      * iterated and extract all matching properties. ie. fontName, fontsize etc.
      * @param fontConfigPropertyName name of the bound component with font render
      *            properties.
      * @param text the text to render.
      * @return an asseturl to the rendered text or an empty string if something
      *         went wrong
      * @author Per Jonsson - per.jonsson@it-huset.se
      */
     public String getRenderedTextUrl( String fontConfigPropertyName, String text )
     {
         return getRenderedTextUrl( fontConfigPropertyName, text, (Map)null );
     }
 
     /**
      * Renders a text to a PNG file, the preferences of the text rendering is
      * taken from the bound component content of the property. The content is
      * iterated and extract all matching properties. ie. fontName, fontsize etc.
      * @param propertyName name of the bound component with font render
      *            properties.
      * @param text the text to render
      * @param renderAttributes render attributes in a commaseparated string ie.
      *            "fontname=Arial,fontsize=12" to override the bound content
      *            preferences
      * @return an asseturl to the rendered text or an empty string if something
      *         went wrong
      * @author Per Jonsson - per.jonsson@it-huset.se
      */
     public String getRenderedTextUrl( String fontConfigPropertyName, String text, String renderAttributes )
     {
         return getRenderedTextUrl( fontConfigPropertyName, text, Support.convertTextToMap( renderAttributes, "=", "," ) );
     }
 
     /**
      * Renders a text to a PNG file, the preferences of the text rendering is
      * taken from the bound component content of the property. The content is
      * iterated and extract all matching properties. ie. fontName, fontsize etc.
      * @param propertyName name of the bound component with font render
      *            properties.
      * @param text the text to render
      * @param renderAttributes render attributes in a commaseparated string ie.
      *            "fontname=Arial,fontsize=12" to override the bound content
      *            preferences
      * @return an asseturl to the rendered text or an empty string if something
      *         went wrong
      * @author Per Jonsson - per.jonsson@it-huset.se
      */
     public String getRenderedTextUrl( String fontConfigPropertyName, String text, Map renderAttributes )
     {
         String assetUrl = "";
         try
         {
             Map property = getInheritedComponentProperty( this.infoGlueComponent, fontConfigPropertyName, true );
             if ( property != null )
             {
                 List bindings = (List)property.get( "bindings" );
                 if ( bindings.size() > 0 )
                 {
                     Integer contentId = new Integer( (String)bindings.get( 0 ) );
                     assetUrl = templateController.getRenderedTextUrl( contentId, text, renderAttributes );
                 }
             }
 
         }
         catch ( Exception e )
         {
             logger.error( "An error occurred trying to get getRenderedTextUrl As ImageUrl:" + e.getMessage(), e );
         }
 
         return assetUrl;
     }
     
 }
