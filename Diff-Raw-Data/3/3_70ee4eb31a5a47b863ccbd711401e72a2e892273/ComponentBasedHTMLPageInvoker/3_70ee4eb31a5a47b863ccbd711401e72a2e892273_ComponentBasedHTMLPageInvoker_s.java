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
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Attribute;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.DateHelper;
 import org.infoglue.cms.util.XMLHelper;
 import org.infoglue.cms.util.dom.DOMBuilder;
 import org.infoglue.deliver.applications.actions.InfoGlueComponent;
 import org.infoglue.deliver.applications.databeans.ComponentBinding;
 import org.infoglue.deliver.applications.databeans.ComponentRestriction;
 import org.infoglue.deliver.applications.databeans.Slot;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ComponentLogic;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.InfoGlueHashSet;
 import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.RepositoryDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.NullObject;
 import org.infoglue.deliver.util.Timer;
 import org.infoglue.deliver.util.VelocityTemplateProcessor;
 
 /**
 * @author Mattias Bogeblad
 *
 * This class delivers a normal html page by using the component-based method.
 */
 
 public class ComponentBasedHTMLPageInvoker extends PageInvoker
 {
 	private final static DOMBuilder domBuilder = new DOMBuilder();
 	private final static VisualFormatter vf = new VisualFormatter();
 	
     private final static Logger logger = Logger.getLogger(ComponentBasedHTMLPageInvoker.class.getName());
     
    /**
 	 * This method should return an instance of the class that should be used for page editing inside the tools or in working. 
 	 * Makes it possible to have an alternative to the ordinary delivery optimized class.
 	 */
 	
     public PageInvoker getDecoratedPageInvoker(TemplateController templateController) throws SystemException
 	{
     	String repositoryDecoratedPageInvoker = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(templateController.getSiteNode().getRepositoryId(), "decoratedPageInvoker");
     	if(repositoryDecoratedPageInvoker != null && !repositoryDecoratedPageInvoker.equals(""))
     	{
     		if(repositoryDecoratedPageInvoker != null && repositoryDecoratedPageInvoker.equalsIgnoreCase("ajax"))
 	    		return new AjaxDecoratedComponentBasedHTMLPageInvoker();
 	    	else
 	    		return new DecoratedComponentBasedHTMLPageInvoker();
     	}
     	else
     	{
 	    	String decoratedPageInvoker = CmsPropertyHandler.getDecoratedPageInvoker();
 	    	if(decoratedPageInvoker != null && decoratedPageInvoker.equalsIgnoreCase("ajax"))
 	    		return new AjaxDecoratedComponentBasedHTMLPageInvoker();
 	    	else
 	    		return new DecoratedComponentBasedHTMLPageInvoker();
     	}
     }
 
     protected String appendPagePartTemplates(String componentXML) throws Exception
     {
     	String resultComponentXML = componentXML;
     	
 		List entries = new ArrayList();
 		int isPagePartReferenceIndex = componentXML.indexOf("isPagePartReference");
 		while(isPagePartReferenceIndex > -1)
 		{
 			int tagStartIndex = componentXML.lastIndexOf("<component ", isPagePartReferenceIndex);
 			int tagEndIndex = componentXML.indexOf(">", isPagePartReferenceIndex);
 			String componentString = componentXML.substring(tagStartIndex, tagEndIndex);
 			
 			int contentIdIndex = componentString.indexOf(" contentId=");
 			String contentId = componentString.substring(contentIdIndex + 12, componentString.indexOf("\"", contentIdIndex + 12));
 			
 			int idIndex = componentString.indexOf(" id=");
 			String id = componentString.substring(idIndex + 5, componentString.indexOf("\"", idIndex + 5));
 
 			int nameIndex = componentString.indexOf(" name=");
 			String name = componentString.substring(nameIndex + 7, componentString.indexOf("\"", nameIndex + 7));
 			
 			Map entry = new HashMap();
 			entry.put("contentId", contentId);
 			entry.put("id", id);
 			entry.put("name", name);
 			entries.add(entry);
 			
 			isPagePartReferenceIndex = componentXML.indexOf("isPagePartReference", isPagePartReferenceIndex + 20);
 		}
 		
 		Iterator entriesIterator = entries.iterator();
 		while(entriesIterator.hasNext())
 		{
 			Map entry = (Map)entriesIterator.next();
 			
 			String contentIdString = (String)entry.get("contentId");
 			Integer contentId = new Integer(contentIdString);
 			String id = (String)entry.get("id");
 			String name = (String)entry.get("name");
 			ContentTypeDefinitionVO contentTypeDefinitionVO = ContentDeliveryController.getContentDeliveryController().getContentTypeDefinitionVO(getDatabase(), contentId);
 		    if(contentTypeDefinitionVO != null && contentTypeDefinitionVO.getName().equals("PagePartTemplate"))
 		    {
 		    	//logger.info("This was a pagePart reference..");
 		    	this.getTemplateController().getDeliveryContext().addUsedContent("selectiveCacheUpdateNonApplicable");
 
 		    	String pagePartString = this.getTemplateController().getContentAttribute(contentId, "ComponentStructure", true);
 		    	logger.info("pagePartString: " + pagePartString);
 		    	if(pagePartString == null || pagePartString.equals(""))
 		    	{
 		    		ContentVO contentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(getTemplateController().getDatabase(), contentId, getTemplateController().getDeliveryContext());
 		    		LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId(), getTemplateController().getDatabase());
 		    		pagePartString = this.getTemplateController().getContentAttribute(contentId, masterLanguageVO.getId(), "ComponentStructure", true);
 			    	logger.info("pagePartString: " + pagePartString);
 		    	}
 		    	
 		    	pagePartString = pagePartString.replaceFirst(" id=\".*?\"", " id=\"" + id + "\"");
 		    	pagePartString = pagePartString.replaceFirst(" name=\".*?\"", " name=\"" + name + "\"");
 		    	pagePartString = pagePartString.replaceFirst(" pagePartTemplateContentId=\".*?\"", " pagePartTemplateContentId=\"" + contentId + "\"");
 		    	logger.info("Bytte id och namn: " + pagePartString);
 		    	
 		    	pagePartString = pagePartString.substring(pagePartString.indexOf("<component "));
 		    	pagePartString = pagePartString.substring(0, pagePartString.lastIndexOf("</components>"));
 		    	
 		    	
 		    	//logger.info("componentXML: " + componentXML);
 		    	logger.info("contentId" + contentId);
 		    	logger.info("pagePartString" + pagePartString);
 		    	String newComponentXML = componentXML.replaceAll("<component contentId=\"" + contentId + ".*?</component>", "" + pagePartString);
 		    	//logger.info("newComponentXML: " + newComponentXML);
 		    	resultComponentXML = newComponentXML;
 		    }
 		}
 
     	return resultComponentXML;
     }
     
 	/**
 	 * This is the method that will render the page. It uses the new component based structure. 
 	 */ 
 
 	public void invokePage() throws SystemException, Exception
 	{
 		String pageContent = "";
 		
 		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(this.getDeliveryContext());
 		
 		Integer repositoryId = nodeDeliveryController.getSiteNode(getDatabase(), this.getDeliveryContext().getSiteNodeId()).getRepository().getId();
 
 		String componentXML = getPageComponentsString(getDatabase(), this.getTemplateController(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
 		componentXML = appendPagePartTemplates(componentXML);
 		
 		InfoGlueComponent baseComponent = null;
 		
    		if(componentXML != null && componentXML.length() != 0)
 		{
    			Document document = domBuilder.getDocument(componentXML);
 			
    			List unsortedPageComponents = new ArrayList();
 			List pageComponents = getPageComponents(getDatabase(), componentXML, document.getRootElement(), "base", this.getTemplateController(), null, unsortedPageComponents);
 			
 			preProcessComponents(nodeDeliveryController, repositoryId, unsortedPageComponents, pageComponents);
 			
 			if(pageComponents.size() > 0)
 			{
 				baseComponent = (InfoGlueComponent)pageComponents.get(0);
 			}
 		
 			if(baseComponent != null)
 			{
 				ContentVO metaInfoContentVO = nodeDeliveryController.getBoundContent(getDatabase(), this.getTemplateController().getPrincipal(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), true, "Meta information", this.getDeliveryContext());
 				pageContent = renderComponent(baseComponent, this.getTemplateController(), repositoryId, this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId(), metaInfoContentVO.getId(), 15, 0);
 			}
 		}
 
    		/*
 		Map context = getDefaultContext();
 		StringWriter cacheString = new StringWriter();
 		PrintWriter cachedStream = new PrintWriter(cacheString);
 		new VelocityTemplateProcessor().renderTemplate(context, cachedStream, pageContent, false, baseComponent);
 		
 		String pageString = pageContent;
 		if(this.getDeliveryContext().getEvaluateFullPage())
 			pageString = cacheString.toString();
 		
 		pageString = this.getTemplateController().decoratePage(pageString);
 		
 		this.setPageString(pageString);
 		*/
    		
    		String pageString = pageContent;
    		
 		if(this.getDeliveryContext().getEvaluateFullPage())
 		{
 			Map context = getDefaultContext();
 			StringWriter cacheString = new StringWriter();
 			PrintWriter cachedStream = new PrintWriter(cacheString);
 			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, pageContent, false, baseComponent);
 			
 			pageString = cacheString.toString();
 		}
 
 		//pageString = decorateHeadAndPageWithVarsFromComponents(pageString);
 		
 		this.setPageString(pageString);
 	}
 
 	protected void preProcessComponents(NodeDeliveryController nodeDeliveryController, Integer repositoryId, List unsortedPageComponents, List pageComponents) throws SystemException, Exception
 	{
 		List sortedPageComponents = new ArrayList();
 		Iterator unsortedPageComponentsIterator = unsortedPageComponents.iterator();
 		while(unsortedPageComponentsIterator.hasNext())
 		{
 			InfoGlueComponent component = (InfoGlueComponent)unsortedPageComponentsIterator.next();
 
 			this.getTemplateController().setComponentLogic(new ComponentLogic(this.getTemplateController(), component));
 			this.getTemplateController().getDeliveryContext().getUsageListeners().add(this.getTemplateController().getComponentLogic().getComponentDeliveryContext());
 			
 			int index = 0;
 			Iterator sortedPageComponentsIterator = sortedPageComponents.iterator();
 			while(sortedPageComponentsIterator.hasNext())
 			{
 				InfoGlueComponent sortedComponent = (InfoGlueComponent)sortedPageComponentsIterator.next();
 
 				this.getTemplateController().setComponentLogic(new ComponentLogic(this.getTemplateController(), sortedComponent));
 				this.getTemplateController().getDeliveryContext().getUsageListeners().add(this.getTemplateController().getComponentLogic().getComponentDeliveryContext());
 
 				if(sortedComponent.getPreProcessingOrder().compareTo(component.getPreProcessingOrder()) < 0)
 					break;
 
 				index++;
 			}
 			
 			sortedPageComponents.add(index, component);
 		}
 
 		Iterator sortedPageComponentsIterator = sortedPageComponents.iterator();
 		while(sortedPageComponentsIterator.hasNext())
 		{
 			InfoGlueComponent component = (InfoGlueComponent)sortedPageComponentsIterator.next();
 
 			this.getTemplateController().setComponentLogic(new ComponentLogic(this.getTemplateController(), component));
 			this.getTemplateController().getDeliveryContext().getUsageListeners().add(this.getTemplateController().getComponentLogic().getComponentDeliveryContext());
 			
 			ContentVO metaInfoContentVO = nodeDeliveryController.getBoundContent(getDatabase(), this.getTemplateController().getPrincipal(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), true, "Meta information", this.getDeliveryContext());
 			preProcessComponent(component, this.getTemplateController(), repositoryId, this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId(), metaInfoContentVO.getId());
 		}
 	}
 	
 	/*
 	protected String decorateHeadAndPageWithVarsFromComponents(String pageString)
 	{
 		if(pageString.length() < 500000)
 		{
 			pageString = this.getTemplateController().decoratePage(pageString);
 			
 			StringBuffer sb = null;
 			
 			List htmlHeadItems = this.getTemplateController().getDeliveryContext().getHtmlHeadItems();
 			if(htmlHeadItems != null || htmlHeadItems.size() > 0)
 			{
 				int indexOfHeadEndTag = pageString.indexOf("</head");
 				if(indexOfHeadEndTag == -1)
 					indexOfHeadEndTag = pageString.indexOf("</HEAD");
 	
 				if(indexOfHeadEndTag != -1)
 				{
 					sb = new StringBuffer(pageString);
 					Iterator htmlHeadItemsIterator = htmlHeadItems.iterator();
 					while(htmlHeadItemsIterator.hasNext())
 					{
 						String value = (String)htmlHeadItemsIterator.next();
 						sb.insert(indexOfHeadEndTag, value + "\n");
 					}
 					//pageString = sb.toString();
 				}
 			}
 			
 			try
 			{
 				int lastModifiedDateTimeIndex;
 				if(sb == null)
 					lastModifiedDateTimeIndex = pageString.indexOf("<ig:lastModifiedDateTime");
 				else
 					lastModifiedDateTimeIndex = sb.indexOf("<ig:lastModifiedDateTime");
 					
 				if(lastModifiedDateTimeIndex > -1)
 				{
 					if(sb == null)
 						sb = new StringBuffer(pageString);
 
 					int lastModifiedDateTimeEndIndex = sb.indexOf("</ig:lastModifiedDateTime>", lastModifiedDateTimeIndex);
 	
 					String tagInfo = sb.substring(lastModifiedDateTimeIndex, lastModifiedDateTimeEndIndex);
 					logger.info("tagInfo:" + tagInfo);
 					String dateFormat = "yyyy-MM-dd HH:mm";
 					int formatStartIndex = tagInfo.indexOf("format");
 					if(formatStartIndex > -1)
 					{
 						int formatEndIndex = tagInfo.indexOf("\"", formatStartIndex + 7);
 						if(formatEndIndex > -1)
 							dateFormat = tagInfo.substring(formatStartIndex + 7, formatEndIndex);
 					}
 						
 					String dateString = vf.formatDate(this.getTemplateController().getDeliveryContext().getLastModifiedDateTime(), this.getTemplateController().getLocale(), dateFormat);
 					logger.info("dateString:" + dateString);
 					sb.replace(lastModifiedDateTimeIndex, lastModifiedDateTimeEndIndex + "</ig:lastModifiedDateTime>".length(), dateString);
 					logger.info("Replaced:" + lastModifiedDateTimeIndex + " to " + lastModifiedDateTimeEndIndex + "</ig:lastModifiedDateTime>".length() + " with " + dateString);
 				}
 			}
 			catch (Exception e) 
 			{
 				logger.error("Problem setting lastModifiedDateTime:" + e.getMessage(), e);
 			}
 			
 			if(sb != null)
 				pageString = sb.toString();			
 		}
 		else
 		{
 			if(logger.isInfoEnabled())
 				logger.info("pageString was to large (" + pageString.length() + ") so the headers was not inserted.");
 		}
 		
 		return pageString;
 	}
 	*/
 	
 	/**
 	 * This method fetches the pageComponent structure from the metainfo content.
 	 */
 	    
 	protected String getPageComponentsString(Database db, TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{ 
 	    SiteNodeVO siteNodeVO = templateController.getSiteNode(siteNodeId);
 	    ContentVO contentVO = null;
 	    if(siteNodeVO.getMetaInfoContentId() != null && siteNodeVO.getMetaInfoContentId().intValue() > -1)
 	        contentVO = templateController.getContent(siteNodeVO.getMetaInfoContentId());
 	    else
 		    contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(db, templateController.getPrincipal(), siteNodeId, languageId, true, "Meta information", this.getDeliveryContext());		
 
 		if(contentVO == null)
 			throw new SystemException("There was no Meta Information bound to this page which makes it impossible to render.");	
 
 	    String cacheName 	= "componentEditorCache";
 		String cacheKey		= "pageComponentString_" + siteNodeId + "_" + languageId + "_" + contentId;
 		String versionKey 	= cacheKey + "_contentVersionId";
 
 		String attributeName = "ComponentStructure";
 		//String attributeKey = "" + contentVO.getId() + "_" + languageId + "_" + attributeName + "_" + siteNodeId + "_" + true;
 		//String attributeKey = "" + contentVO.getId() + "_" + languageId + "_" + attributeName + "_" + true + "_" + false;
 		//String versionKey 	= attributeKey + "_contentVersionId";
 
 	    String cachedPageComponentsString = (String)CacheController.getCachedObject(cacheName, cacheKey);
 	    Set contentVersionId = (Set)CacheController.getCachedObjectFromAdvancedCache("contentVersionCache", versionKey);
 	    //logger.info("Getting with versionKey-------------------------->:" + versionKey);
 
 
 		if(cachedPageComponentsString != null)
 		{
 		    //logger.info("Returning cached...");
 		    //logger.info("First added..." + versionKey + ":" + "contentVersion:" + contentVersionId);
 			if(contentVersionId != null)
 			{    
 				Iterator contentVersionIdIterator = contentVersionId.iterator();
 				while(contentVersionIdIterator.hasNext())
 				{
 					Integer currentContentVersionId = (Integer)contentVersionIdIterator.next();
 					templateController.getDeliveryContext().addUsedContentVersion("contentVersion_" + currentContentVersionId);
 			    	//logger.info("\nThere was a cached page string and the meta info content version was " + contentVersionId);
 			    	templateController.getDeliveryContext().getUsedPageMetaInfoContentVersionIdSet().add(currentContentVersionId);
 				}
 			}
 			
 		    return cachedPageComponentsString;
 		}
 		
 		String pageComponentsString = null;
    					
 		//logger.info("contentVO in getPageComponentsString: " + contentVO.getContentId());
 		Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(db, siteNodeId).getId();
 	    pageComponentsString = templateController.getMetaInfoContentAttribute(contentVO.getContentId(), masterLanguageId, "ComponentStructure", true);
 		
 		if(pageComponentsString == null)
 			throw new SystemException("There was no Meta Information bound to this page which makes it impossible to render.");	
 				    
 		logger.info("pageComponentsString: " + pageComponentsString);
 	
 		CacheController.cacheObject(cacheName, cacheKey, pageComponentsString);
 	    //logger.info("Caching usedContentVersionId for cacheKey: " + cacheKey + ":" + templateController.getDeliveryContext().getPageMetaInfoContentVersionId());
 		
 	    Set contentVersionIds = new InfoGlueHashSet();
 	    //TODO - mste fixa s att inte nulls slngs in i getUsedPageMetaInfoContentVersionIdSet... hur det kan hnda
 	    contentVersionIds.addAll(templateController.getDeliveryContext().getUsedPageMetaInfoContentVersionIdSet());
 	    
 		Set groups = new InfoGlueHashSet();
 		if(templateController.getDeliveryContext().getUsedPageMetaInfoContentVersionIdSet().size() > 0)
 		{
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId((Integer)templateController.getDeliveryContext().getUsedPageMetaInfoContentVersionIdSet().toArray()[0], templateController.getDatabase());
 			groups.add("contentVersion_" + contentVersionVO.getId());
 			groups.add("content_" + contentVersionVO.getContentId());
 		}
 		
 		if(groups.size() > 0)
 			CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("contentVersionCache", versionKey, contentVersionIds, groups, true);
 		
 		return pageComponentsString;
 	}
 
 
 	/**
 	 * This method fetches the pageComponent structure as a document.
 	 */
 	/*
 	protected org.w3c.dom.Document getPageComponentsDocument(Database db, TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{ 
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "pageComponentDocument_" + siteNodeId + "_" + languageId + "_" + contentId;
 		org.w3c.dom.Document cachedPageComponentsDocument = (org.w3c.dom.Document)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedPageComponentsDocument != null)
 			return cachedPageComponentsDocument;
 		
 		org.w3c.dom.Document pageComponentsDocument = null;
    	
 		try
 		{
 			String xml = this.getPageComponentsString(db, templateController, siteNodeId, languageId, contentId);
 			pageComponentsDocument = XMLHelper.readDocumentFromByteArray(xml.getBytes("UTF-8"));
 			
 			CacheController.cacheObject(cacheName, cacheKey, pageComponentsDocument);
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 		
 		return pageComponentsDocument;
 	}
 	*/
 
 	protected org.dom4j.Document getPageComponentsDOM4JDocument(Database db, TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{ 
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "pageComponentDocument_" + siteNodeId + "_" + languageId + "_" + contentId;
 		org.dom4j.Document cachedPageComponentsDocument = (org.dom4j.Document)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedPageComponentsDocument != null)
 			return cachedPageComponentsDocument;
 		
 		org.dom4j.Document pageComponentsDocument = null;
    	
 		try
 		{
 			String xml = this.getPageComponentsString(db, templateController, siteNodeId, languageId, contentId);
 			pageComponentsDocument = domBuilder.getDocument(xml);
 			
 			CacheController.cacheObject(cacheName, cacheKey, pageComponentsDocument);
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 		
 		return pageComponentsDocument;
 	}
 
 	/**
 	 * This method gets a Map of the components available on the page.
 	 */
 
 	protected Map getComponents(Database db, Element element, TemplateController templateController, InfoGlueComponent parentComponent) throws Exception
 	{
 		InfoGlueComponent component = null;
 
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(db, templateController.getLanguageId());
 		
 		Map components = new HashMap();
 		
 		String componentXPath = "component";
 		//logger.info("componentXPath - A:" + componentXPath);
 		List componentNodeList = element.selectNodes(componentXPath);
 		Iterator componentNodeListIterator = componentNodeList.iterator();
 		while(componentNodeListIterator.hasNext())
 		{
 			Element child 		= (Element)componentNodeListIterator.next();
 			Integer id 			= new Integer(child.attributeValue("id"));
 			Integer contentId 	= new Integer(child.attributeValue("contentId"));
 			String name 	  	= child.attributeValue("name");
 			
 			//logger.info("id 2:" + id);
 			//logger.info("contentId 2:" + contentId);
 			//logger.info("name 2:" + name);
 			
 			ContentVO contentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(contentId, db);
 			
 			component = new InfoGlueComponent();
 			component.setId(id);
 			component.setContentId(contentId);
 			component.setName(contentVO.getName());
 			//component.setName(name);
 			component.setSlotName(name);
 			component.setParentComponent(parentComponent);
 			if(parentComponent != null)
 				component.setIsInherited(parentComponent.getIsInherited());
 				
 			//Change to this later
 			//getComponentProperties(child, component, locale, templateController);
 			//logger.info("componentXPath - B:" + componentXPath);
 			List propertiesNodeList = child.selectNodes("properties");
 			//logger.info("propertiesNodeList:" + propertiesNodeList.getLength());
 			if(propertiesNodeList.size() > 0)
 			{
 				Element propertiesElement = (Element)propertiesNodeList.get(0);
 				
 				List propertyNodeList = propertiesElement.selectNodes("property");
 				//logger.info("propertyNodeList:" + propertyNodeList.getLength());
 				Iterator propertyNodeListIterator = propertyNodeList.iterator();
 				while(propertyNodeListIterator.hasNext())
 				{
 					Element propertyElement = (Element)propertyNodeListIterator.next();
 					
 					String propertyName = propertyElement.attributeValue("name");
 					String type = propertyElement.attributeValue("type");
 					String path = propertyElement.attributeValue("path");
 
 					if(path == null)
 					{
 						LanguageVO langaugeVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), templateController.getSiteNodeId());
 						if(propertyElement.attributeValue("path_" + langaugeVO.getLanguageCode()) != null)
 							path = propertyElement.attributeValue("path_" + langaugeVO.getLanguageCode());
 					}
 
 					//logger.info("path:" + "path_" + locale.getLanguage() + ":" + propertyElement.attributeValue("path_" + locale.getLanguage()));
 					if(propertyElement.attributeValue("path_" + locale.getLanguage()) != null)
 						path = propertyElement.attributeValue("path_" + locale.getLanguage());
 
 					if(path == null || path.equals(""))
 					{
 						logger.info("Falling back to content master language 1 for property:" + propertyName);
 						LanguageVO contentMasterLangaugeVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(getDatabase(), contentVO.getRepositoryId());
 						if(propertyElement.attributeValue("path_" + contentMasterLangaugeVO.getLanguageCode()) != null)
 							path = propertyElement.attributeValue("path_" + contentMasterLangaugeVO.getLanguageCode());	
 					}
 					
 					Map property = new HashMap();
 					property.put("name", propertyName);
 					property.put("path", path);
 					property.put("type", type);
 					
 					List attributes = propertyElement.attributes();
 					Iterator attributesIterator = attributes.iterator();
 					while(attributesIterator.hasNext())
 					{
 						Attribute attribute = (Attribute)attributesIterator.next();
 						if(attribute.getName().startsWith("path_"))
 							property.put(attribute.getName(), attribute.getValue());
 					}
 					
 					if(path != null)
 					{
 						if(propertyName.equals(InfoGlueComponent.CACHE_RESULT_PROPERTYNAME) && (path.equalsIgnoreCase("true") || path.equalsIgnoreCase("yes")))
 						{
 							component.setCacheResult(true);
 						}
 						if(propertyName.equals(InfoGlueComponent.UPDATE_INTERVAL_PROPERTYNAME) && !path.equals(""))
 						{
 							try { component.setUpdateInterval(Integer.parseInt(path)); } catch (Exception e) { logger.warn("The component " + component.getName() + " " + InfoGlueComponent.UPDATE_INTERVAL_PROPERTYNAME + " with a faulty value on page " + this.getTemplateController().getOriginalFullURL() + ":" + e.getMessage()); }
 						}
 						if(propertyName.equals(InfoGlueComponent.CACHE_KEY_PROPERTYNAME) && !path.equals(""))
 						{
 							component.setCacheKey(path);
 						}
 						if(propertyName.equals(InfoGlueComponent.PREPROCESSING_ORDER_PROPERTYNAME) && !path.equals(""))
 						{
 							component.setPreProcessingOrder(path);
 						}
 					}
 					
 					List bindings = new ArrayList();
 					List bindingNodeList = propertyElement.selectNodes("binding");
 					//logger.info("bindingNodeList:" + bindingNodeList.getLength());
 					Iterator bindingNodeListIterator = bindingNodeList.iterator();
 					while(bindingNodeListIterator.hasNext())
 					{
 						Element bindingElement = (Element)bindingNodeListIterator.next();
 						String entity = bindingElement.attributeValue("entity");
 						String entityId = bindingElement.attributeValue("entityId");
 						String assetKey = bindingElement.attributeValue("assetKey");
 						//logger.info("Binding found:" + entity + ":" + entityId);
 
 						ComponentBinding componentBinding = new ComponentBinding();
 						//componentBinding.setId(new Integer(id));
 						//componentBinding.setComponentId(componentId);
 						componentBinding.setEntityClass(entity);
 						componentBinding.setEntityId(new Integer(entityId));
 						componentBinding.setAssetKey(assetKey);
 						componentBinding.setBindingPath(path);
 						
 						bindings.add(componentBinding);
 					}
 	
 					property.put("bindings", bindings);
 					
 					component.getProperties().put(propertyName, property);
 
 					//TEST
 					//component.getProperties().put(propertyName, property);
 
 				}
 			}
 			
 			
 			getComponentRestrictions(child, component, locale, templateController);
 
 			
 			//Getting slots for the component
 			String componentString = this.getComponentString(templateController, contentId, component);
 			//logger.info("Getting the slots for component.......");
 			//logger.info("componentString:" + componentString);
 			int offset = 0;
 			int slotStartIndex = componentString.indexOf("<ig:slot", offset);
 			while(slotStartIndex > -1)
 			{
 				int slotStopIndex = componentString.indexOf("</ig:slot>", slotStartIndex);
 				String slotString = componentString.substring(slotStartIndex, slotStopIndex + 10);
 				String slotId = slotString.substring(slotString.indexOf("id") + 4, slotString.indexOf("\"", slotString.indexOf("id") + 4));
 
 				boolean inherit = true;
 				int inheritIndex = slotString.indexOf("inherit");
 				if(inheritIndex > -1)
 				{    
 				    String inheritString = slotString.substring(inheritIndex + 9, slotString.indexOf("\"", inheritIndex + 9));
 				    inherit = Boolean.parseBoolean(inheritString);
 				}
 
 				boolean disableAccessControl = false;
 				int disableAccessControlIndex = slotString.indexOf("disableAccessControl");
 				if(disableAccessControlIndex > -1)
 				{    
 				    String disableAccessControlString = slotString.substring(disableAccessControlIndex + "disableAccessControl".length() + 2, slotString.indexOf("\"", disableAccessControlIndex + "disableAccessControl".length() + 2));
 				    disableAccessControl = Boolean.parseBoolean(disableAccessControlString);
 				}
 
 				String[] allowedComponentNamesArray = null;
 				int allowedComponentNamesIndex = slotString.indexOf(" allowedComponentNames");
 				if(allowedComponentNamesIndex > -1)
 				{    
 				    String allowedComponentNames = slotString.substring(allowedComponentNamesIndex + 24, slotString.indexOf("\"", allowedComponentNamesIndex + 24));
 				    allowedComponentNamesArray = allowedComponentNames.split(",");
 				}
 
 				String[] disallowedComponentNamesArray = null;
 				int disallowedComponentNamesIndex = slotString.indexOf(" disallowedComponentNames");
 				if(disallowedComponentNamesIndex > -1)
 				{    
 				    String disallowedComponentNames = slotString.substring(disallowedComponentNamesIndex + 27, slotString.indexOf("\"", disallowedComponentNamesIndex + 27));
 				    disallowedComponentNamesArray = disallowedComponentNames.split(",");
 				}
 
 				String[] allowedComponentGroupNamesArray = null;
 				int allowedComponentGroupNamesIndex = slotString.indexOf(" allowedComponentGroupNames");
 				if(allowedComponentGroupNamesIndex > -1)
 				{    
 				    String allowedComponentGroupNames = slotString.substring(allowedComponentGroupNamesIndex + 29, slotString.indexOf("\"", allowedComponentGroupNamesIndex + 29));
 				    allowedComponentGroupNamesArray = allowedComponentGroupNames.split(",");
 				}
 
 				String addComponentText = null;
 				int addComponentTextIndex = slotString.indexOf("addComponentText");
 				if(addComponentTextIndex > -1)
 				{    
 				    addComponentText = slotString.substring(addComponentTextIndex + "addComponentText".length() + 2, slotString.indexOf("\"", addComponentTextIndex + "addComponentText".length() + 2));
 				}
 
 				String addComponentLinkHTML = null;
 				int addComponentLinkHTMLIndex = slotString.indexOf("addComponentLinkHTML");
 				if(addComponentLinkHTMLIndex > -1)
 				{    
 				    addComponentLinkHTML = slotString.substring(addComponentLinkHTMLIndex + "addComponentLinkHTML".length() + 2, slotString.indexOf("\"", addComponentLinkHTMLIndex + "addComponentLinkHTML".length() + 2));
 				}
 
 				int allowedNumberOfComponentsInt = -1;
 				int allowedNumberOfComponentsIndex = slotString.indexOf("allowedNumberOfComponents");
 				if(allowedNumberOfComponentsIndex > -1)
 				{    
 					String allowedNumberOfComponents = slotString.substring(allowedNumberOfComponentsIndex + "allowedNumberOfComponents".length() + 2, slotString.indexOf("\"", allowedNumberOfComponentsIndex + "allowedNumberOfComponents".length() + 2));
 					try
 					{
 						allowedNumberOfComponentsInt = new Integer(allowedNumberOfComponents);
 					}
 					catch (Exception e) 
 					{
 						allowedNumberOfComponentsInt = -1;
 					}
 				}
 
 			  	Slot slot = new Slot();
 			  	slot.setId(slotId);
 			    slot.setInherit(inherit);
 			    slot.setDisableAccessControl(disableAccessControl);
 			  	slot.setAllowedComponentsArray(allowedComponentNamesArray);
 			  	slot.setDisallowedComponentsArray(disallowedComponentNamesArray);
 			  	slot.setAllowedComponentGroupsArray(allowedComponentGroupNamesArray);
 			    slot.setAddComponentLinkHTML(addComponentLinkHTML);
 			    slot.setAddComponentText(addComponentText);
 			    slot.setAllowedNumberOfComponents(new Integer(allowedNumberOfComponentsInt));
 			    
 			  	List subComponents = getComponents(db, templateController, component, templateController.getSiteNodeId(), slotId);
 			  	slot.setComponents(subComponents);
 			  	
 			  	component.getSlotList().add(slot);
 
 			  	offset = slotStopIndex; // + 10;
 				slotStartIndex = componentString.indexOf("<ig:slot", offset);
 			}
 			
 			
 			List anl = child.selectNodes("components");
 			if(anl.size() > 0)
 			{
 				Element componentsElement = (Element)anl.get(0);
 				component.setComponents(getComponents(db, componentsElement, templateController, component));
 			}
 			
 			components.put(name, component);
 		}
 		
 		
 		return components;
 	}
 
 	/**
 	 * This method gets a specific component.
 	 */
 
 	protected Map getComponent(Database db, Element element, String componentName, TemplateController templateController, InfoGlueComponent parentComponent) throws Exception
 	{
 		//logger.info("Getting component with name:" + componentName);
 		InfoGlueComponent component = null;
 
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(db, templateController.getLanguageId());
 
 		Map components = new HashMap();
 		
 		String componentXPath = getComponentXPath(parentComponent) + "/components/component[@name='" + componentName + "']";
 		//logger.info("componentXPath:" + componentXPath);
 		
 		//logger.info("componentXPath:" + componentXPath);
 		List componentNodeList = element.selectNodes(componentXPath);
 		Iterator componentNodeListIterator = componentNodeList.iterator();
 		while(componentNodeListIterator.hasNext())
 		{
 			Element child 		= (Element)componentNodeListIterator.next();
 			Integer id 			= new Integer(child.attributeValue("id"));
 			Integer contentId 	= new Integer(child.attributeValue("contentId"));
 			String name 	  	= child.attributeValue("name");
 	
 			ContentVO contentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(contentId, db);
 			
 			component = new InfoGlueComponent();
 			component.setId(id);
 			component.setContentId(contentId);
 			component.setName(contentVO.getName());
 			//component.setName(name);
 			component.setSlotName(name);
 			component.setParentComponent(parentComponent);
 			if(parentComponent != null)
 				component.setIsInherited(parentComponent.getIsInherited());
 
 			//Change to this later
 			//getComponentProperties(child, component, locale, templateController);
 			List propertiesNodeList = child.selectNodes("properties");
 			////logger.info("propertiesNodeList:" + propertiesNodeList.getLength());
 			if(propertiesNodeList.size() > 0)
 			{
 				Element propertiesElement = (Element)propertiesNodeList.get(0);
 				
 				List propertyNodeList = propertiesElement.selectNodes("property");
 				////logger.info("propertyNodeList:" + propertyNodeList.getLength());
 				Iterator propertyNodeListIterator = propertyNodeList.iterator();
 				while(propertyNodeListIterator.hasNext())
 				{
 					Element propertyElement = (Element)propertyNodeListIterator.next();
 					
 					String propertyName = propertyElement.attributeValue("name");
 					String type = propertyElement.attributeValue("type");
 					String path = propertyElement.attributeValue("path");
 
 					if(path == null)
 					{
 						LanguageVO langaugeVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), templateController.getSiteNodeId());
 						if(propertyElement.attributeValue("path_" + langaugeVO.getLanguageCode()) != null)
 							path = propertyElement.attributeValue("path_" + langaugeVO.getLanguageCode());
 					}
 
 					//logger.info("path:" + "path_" + locale.getLanguage() + ":" + propertyElement.attributeValue("path_" + locale.getLanguage()));
 					if(propertyElement.attributeValue("path_" + locale.getLanguage()) != null)
 						path = propertyElement.attributeValue("path_" + locale.getLanguage());
 					//logger.info("path:" + path);
 
 					if(path == null || path.equals(""))
 					{
 						logger.info("Falling back to content master language 2 for property:" + propertyName);
 						LanguageVO contentMasterLangaugeVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(getDatabase(), contentVO.getRepositoryId());
 						if(propertyElement.attributeValue("path_" + contentMasterLangaugeVO.getLanguageCode()) != null)
 							path = propertyElement.attributeValue("path_" + contentMasterLangaugeVO.getLanguageCode());	
 					}
 
 					Map property = new HashMap();
 					property.put("name", propertyName);
 					property.put("path", path);
 					property.put("type", type);
 					
 					List attributes = propertyElement.attributes();
 					Iterator attributesIterator = attributes.iterator();
 					while(attributesIterator.hasNext())
 					{
 						Attribute attribute = (Attribute)attributesIterator.next();
 						if(attribute.getName().startsWith("path_"))
 							property.put(attribute.getName(), attribute.getValue());
 					}
 					
 					if(path != null)
 					{
 						if(propertyName.equals(InfoGlueComponent.CACHE_RESULT_PROPERTYNAME) && (path.equalsIgnoreCase("true") || path.equalsIgnoreCase("yes")))
 						{
 							component.setCacheResult(true);
 						}
 						if(propertyName.equals(InfoGlueComponent.UPDATE_INTERVAL_PROPERTYNAME) && !path.equals(""))
 						{
 							try { component.setUpdateInterval(Integer.parseInt(path)); } catch (Exception e) { logger.warn("The component " + component.getName() + " " + InfoGlueComponent.UPDATE_INTERVAL_PROPERTYNAME + " with a faulty value on page " + this.getTemplateController().getOriginalFullURL() + ":" + e.getMessage()); }
 						}
 						if(propertyName.equals(InfoGlueComponent.CACHE_KEY_PROPERTYNAME) && !path.equals(""))
 						{
 							component.setCacheKey(path);
 						}
 						if(propertyName.equals(InfoGlueComponent.PREPROCESSING_ORDER_PROPERTYNAME) && !path.equals(""))
 						{
 							component.setPreProcessingOrder(path);
 						}
 					}
 					
 					List bindings = new ArrayList();
 					List bindingNodeList = propertyElement.selectNodes("binding");
 					////logger.info("bindingNodeList:" + bindingNodeList.getLength());
 					Iterator bindingNodeListIterator = bindingNodeList.iterator();
 					while(bindingNodeListIterator.hasNext())
 					{
 						Element bindingElement = (Element)bindingNodeListIterator.next();
 						String entity = bindingElement.attributeValue("entity");
 						String entityId = bindingElement.attributeValue("entityId");
 						String assetKey = bindingElement.attributeValue("assetKey");
 						////logger.info("Binding found:" + entity + ":" + entityId);
 						
 						ComponentBinding componentBinding = new ComponentBinding();
 						//componentBinding.setId(new Integer(id));
 						//componentBinding.setComponentId(componentId);
 						componentBinding.setEntityClass(entity);
 						componentBinding.setEntityId(new Integer(entityId));
 						componentBinding.setAssetKey(assetKey);
 						componentBinding.setBindingPath(path);
 						
 						bindings.add(componentBinding);
 					}
 	
 					property.put("bindings", bindings);
 					
 					component.getProperties().put(propertyName, property);
 				}
 			}
 			
 			getComponentRestrictions(child, component, locale, templateController);
 			
 			List anl = child.selectNodes("components");
 			////logger.info("Components NL:" + anl.getLength());
 			if(anl.size() > 0)
 			{
 				Element componentsElement = (Element)anl.get(0);
 				component.setComponents(getComponents(db, componentsElement, templateController, component));
 			}
 			
 			List componentList = new ArrayList();
 			if(components.containsKey(name))
 				componentList = (List)components.get(name);
 				
 			componentList.add(component);
 			
 			components.put(name, componentList);
 		}
 		
 		return components;
 	}
 	
 	
 	/**
 	 * This method renders the base component and all it's children.
 	 */
 
 	private String renderComponent(InfoGlueComponent component, TemplateController templateController, Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId, Integer metainfoContentId, int maxDepth, int currentDepth) throws Exception
 	{
 		if(currentDepth > maxDepth)
 		{
 			logger.error("A page with to many levels (possibly infinite loop) was found on " + templateController.getOriginalFullURL());
 			return "";
 		}
 
 		if(logger.isDebugEnabled())
 		{
 			logger.debug("\n\n**** Rendering component ****");
 			logger.debug("id: " + component.getId());
 			logger.debug("contentId: " + component.getContentId());
 			logger.debug("name: " + component.getName());
 			logger.debug("slotName: " + component.getSlotName());
 		}
 		
 		StringBuffer decoratedComponent = new StringBuffer();
 		
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 		templateController.setComponentLogic(new ComponentLogic(templateController, component));
 		templateController.getDeliveryContext().getUsageListeners().add(templateController.getComponentLogic().getComponentDeliveryContext());
 
 	    boolean renderComponent = false;
 	    boolean cacheComponent = false;
 
 		//String cacheResult 		 = templateController.getComponentLogic().getPropertyValue("CacheResult", true, false);
 	    //String updateInterval 	 = templateController.getComponentLogic().getPropertyValue("UpdateInterval", true, false);
 	    //String componentCacheKey = templateController.getComponentLogic().getPropertyValue("CacheKey", true, false);
 
 		boolean cacheResult  		= component.getCacheResult();
 		int updateInterval 	 		= component.getUpdateInterval();
 		String componentCacheKey 	= component.getCacheKey();
 		
 		if(componentCacheKey == null || componentCacheKey.equals(""))
 			componentCacheKey = CmsPropertyHandler.getComponentKey();
 		
 		if(componentCacheKey != null && !componentCacheKey.equals(""))
 		{
 			componentCacheKey = CacheController.getComponentCacheKey(componentCacheKey, 
 					templateController.getComponentLogic().getComponentDeliveryContext().getPageKey(), 
 					templateController.getHttpServletRequest().getSession(), 
 					templateController.getHttpServletRequest(), 
 					siteNodeId, 
 					languageId, 
 					contentId, 
 					templateController.getBrowserBean().getUseragent(), 
 					templateController.getHttpServletRequest().getQueryString(), 
 					component,
 					"");
 		}
 		else
 		{
 			StringBuffer componentCacheKeySB = new StringBuffer();
 			componentCacheKeySB.append(templateController.getComponentLogic().getComponentDeliveryContext().getPageKey()).append("_")
 							   .append(component.getId()).append("_")
 							   .append(component.getSlotName()).append("_")
 							   .append(component.getContentId()).append("_")
 							   .append(component.getIsInherited());
 
 			componentCacheKey = componentCacheKeySB.toString();
 			//componentCacheKey = templateController.getComponentLogic().getComponentDeliveryContext().getPageKey() + "_" + component.getId() + "_" + component.getSlotName() + "_" + component.getContentId() + "_" + component.getIsInherited();
 		}
 
 		if(logger.isDebugEnabled())
 		{
 			logger.debug("cacheResult:" + cacheResult);
 			logger.debug("updateInterval:" + updateInterval);
 			logger.debug("componentCacheKey:" + componentCacheKey);
 		}
 
 	    //if(cacheResult == null || !cacheResult.equalsIgnoreCase("true"))
 	    if(!cacheResult)
 	    {
 		    renderComponent = true;
 		}
 		else
 		{
 		    cacheComponent = true;
 		    String refresh = this.getRequest().getParameter("refresh");
 		    if(refresh != null && refresh.equalsIgnoreCase("true"))
 		        renderComponent = true;
 		}
 		
 		if(logger.isDebugEnabled())
 			logger.debug("renderComponent:" + renderComponent);
 	    
 	    if(!renderComponent)
 	    {
 			if(logger.isDebugEnabled())
 				logger.debug("componentCacheKey:" + componentCacheKey);
 
             //if(updateInterval != null && !updateInterval.equals("") && !updateInterval.equals("-1"))
             if(updateInterval > 0)
             {
             	String cachedString = (String)CacheController.getCachedObjectFromAdvancedCache("componentCache", componentCacheKey, updateInterval);
             	if(cachedString != null)
     	        	decoratedComponent.append(cachedString);
             }
 		    else
 		    {
 		    	String cachedString = (String)CacheController.getCachedObjectFromAdvancedCache("componentCache", componentCacheKey);
             	if(cachedString != null)
             		decoratedComponent.append(cachedString);
 		    }
 
             if(decoratedComponent == null || decoratedComponent.length() == 0)
 		        renderComponent = true;
 		}
 	    
 		if(logger.isDebugEnabled())
 			logger.debug("Will we render component:" + component.getName() + ":" + renderComponent);
 	    
 		if(renderComponent)
 	    {
 			decoratedComponent.append("");
 		    
 			try
 			{
 			    String componentString = getComponentString(templateController, component.getContentId(), component); 
 				if(logger.isDebugEnabled())
 					logger.debug("componentString:" + componentString);
 			    
 				Map context = getDefaultContext();
 		    	context.put("templateLogic", templateController);
 		    	StringWriter cacheString = new StringWriter();
 				PrintWriter cachedStream = new PrintWriter(cacheString);
 				//Timer t = new Timer();
 				new VelocityTemplateProcessor().renderTemplate(context, cachedStream, componentString, false, component);
 				//t.printElapsedTime("Rendering of " + component.getName() + " took ");
 				componentString = cacheString.toString();
 
 				if(logger.isDebugEnabled())
 					logger.debug("componentString:" + componentString);
 
 				int offset = 0;
 				int slotStartIndex = componentString.indexOf("<ig:slot", offset);
 				int slotStopIndex = 0;
 				
 				while(slotStartIndex > -1)
 				{
 					if(offset > 0)
 						decoratedComponent.append(componentString.substring(offset + 10, slotStartIndex));
 					else
 						decoratedComponent.append(componentString.substring(offset, slotStartIndex));
 					
 					slotStopIndex = componentString.indexOf("</ig:slot>", slotStartIndex);
 					
 					String slot = componentString.substring(slotStartIndex, slotStopIndex + 10);
 					String id = slot.substring(slot.indexOf("id") + 4, slot.indexOf("\"", slot.indexOf("id") + 4));
 					
 					boolean inherit = true;
 					int inheritIndex = slot.indexOf("inherit");
 					if(inheritIndex > -1)
 					{    
 					    String inheritString = slot.substring(inheritIndex + 9, slot.indexOf("\"", inheritIndex + 9));
 					    inherit = Boolean.parseBoolean(inheritString);
 					}
 
 					List subComponents = getInheritedComponents(templateController.getDatabase(), templateController, component, templateController.getSiteNodeId(), id, inherit);
 					Iterator subComponentsIterator = subComponents.iterator();
 					while(subComponentsIterator.hasNext())
 					{
 						InfoGlueComponent subComponent = (InfoGlueComponent)subComponentsIterator.next();
 						
 						if(logger.isInfoEnabled())
 							logger.info(component.getName() + " had subcomponent " + subComponent.getName() + ":" + subComponent.getId());
 						
 						String subComponentString = "";
 						if(subComponent != null)
 						{
 							subComponentString = renderComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId, metainfoContentId, maxDepth, currentDepth + 1);
 						}
 						
 						decoratedComponent.append(subComponentString.trim());
 					}
 					
 					offset = slotStopIndex;
 					slotStartIndex = componentString.indexOf("<ig:slot", offset);
 				}
 				
 				if(offset > 0)
 				{	
 					decoratedComponent.append(componentString.substring(offset + 10));
 				}
 				else
 				{	
 					decoratedComponent.append(componentString.substring(offset));
 				}
 
 		        if(cacheComponent)
 		        {
 		            if(this.getTemplateController().getOperatingMode().intValue() == 3 && !CmsPropertyHandler.getLivePublicationThreadClass().equalsIgnoreCase("org.infoglue.deliver.util.SelectiveLivePublicationThread"))
 		        	    CacheController.cacheObjectInAdvancedCache("componentCache", componentCacheKey, decoratedComponent.toString(), templateController.getComponentLogic().getComponentDeliveryContext().getAllUsedEntities(), false);
 		        	else
 		                CacheController.cacheObjectInAdvancedCache("componentCache", componentCacheKey, decoratedComponent.toString(), templateController.getComponentLogic().getComponentDeliveryContext().getAllUsedEntities(), true);
 		        }	    
 
 			}
 			catch(Exception e)
 			{		
 			    logger.warn("An component with either an empty template or with no template in the sitelanguages was found:" + e.getMessage(), e);	
 			}    	
 
 		}
 		
 		templateController.getDeliveryContext().getUsageListeners().remove(templateController.getComponentLogic().getComponentDeliveryContext());
 
 		if(logger.isDebugEnabled())
 			logger.debug("decoratedComponent:" + decoratedComponent.toString());
 
 		return decoratedComponent.toString();
 	}
 
 	/**
 	 * This method renders the base component and all it's children.
 	 */
 
 	private String preProcessComponent(InfoGlueComponent component, TemplateController templateController, Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId, Integer metainfoContentId) throws Exception
 	{
 		if(logger.isDebugEnabled())
 		{
 			logger.debug("\n\n**** Pre processing component ****");
 			logger.debug("id: " + component.getId());
 			logger.debug("contentId: " + component.getContentId());
 			logger.debug("name: " + component.getName());
 			logger.debug("slotName: " + component.getSlotName());
 		}
 		
 		StringBuffer decoratedComponent = new StringBuffer();
 		
 		templateController.setComponentLogic(new ComponentLogic(templateController, component));
 		templateController.getDeliveryContext().getUsageListeners().add(templateController.getComponentLogic().getComponentDeliveryContext());
 
 		try
 		{
 		    String componentString = getComponentPreProcessingTemplateString(templateController, component.getContentId(), component); 
 			if(logger.isDebugEnabled())
 				logger.debug("componentString:" + componentString);
 		    
 			if(componentString != null && !componentString.equals(""))
 			{
 				Map context = getDefaultContext();
 		    	context.put("templateLogic", templateController);
 		    	StringWriter cacheString = new StringWriter();
 				PrintWriter cachedStream = new PrintWriter(cacheString);
 				//Timer t = new Timer();
 				new VelocityTemplateProcessor().renderTemplate(context, cachedStream, componentString, false, component, " - PreTemplate");
 				//t.printElapsedTime("Rendering of " + component.getName() + " took ");
 				componentString = cacheString.toString();
 				
 				if(logger.isDebugEnabled())
 					logger.debug("componentString:" + componentString);
 			}
 			
 			String templateComponentString = getComponentString(templateController, component.getContentId(), component);
 		    if(logger.isDebugEnabled())
 				logger.debug("templateComponentString:" + templateComponentString);
 
 			int offset = 0;
 			int slotStartIndex = templateComponentString.indexOf("<ig:slot", offset);
 			int slotStopIndex = 0;
 			
 			while(slotStartIndex > -1)
 			{					
 				slotStopIndex = templateComponentString.indexOf("</ig:slot>", slotStartIndex);
 				
 				String slot = templateComponentString.substring(slotStartIndex, slotStopIndex + 10);
 				String id = slot.substring(slot.indexOf("id") + 4, slot.indexOf("\"", slot.indexOf("id") + 4));
 				
 				boolean inherit = true;
 				int inheritIndex = slot.indexOf("inherit");
 				if(inheritIndex > -1)
 				{    
 				    String inheritString = slot.substring(inheritIndex + 9, slot.indexOf("\"", inheritIndex + 9));
 				    inherit = Boolean.parseBoolean(inheritString);
 				}
 
 				List subComponents = getInheritedComponents(templateController.getDatabase(), templateController, component, templateController.getSiteNodeId(), id, inherit);
 				Iterator subComponentsIterator = subComponents.iterator();
 				while(subComponentsIterator.hasNext())
 				{
 					InfoGlueComponent subComponent = (InfoGlueComponent)subComponentsIterator.next();
 					
 					if(subComponent.getIsInherited())
 					{
 						String subComponentString = preProcessComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId, metainfoContentId);
 					}
 				}
 				
 				offset = slotStopIndex;
 				slotStartIndex = templateComponentString.indexOf("<ig:slot", offset);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		    logger.warn("An component with either an empty template or with no template in the sitelanguages was found:" + e.getMessage(), e);	
 		}    	
 		
 		templateController.getDeliveryContext().getUsageListeners().remove(templateController.getComponentLogic().getComponentDeliveryContext());
 
 		if(logger.isDebugEnabled())
 			logger.debug("decoratedComponent:" + decoratedComponent.toString());
 
 		return decoratedComponent.toString();
 	}
 
 
 	/**
 	 * This method fetches the component template as a string.
 	 */
    
 	protected String getComponentString(TemplateController templateController, Integer contentId, InfoGlueComponent component) throws SystemException, Exception
 	{
 		String template = null;
 
 		try
 		{
 		    if(templateController.getDeliveryContext().getShowSimple() == true)
 		    {
 		        String componentString = templateController.getContentAttribute(contentId, templateController.getTemplateAttributeName(), true);
                 String slots = "";
                 int offset = 0;
 		        int index = componentString.indexOf("<ig:slot");
                 int end = componentString.indexOf("</ig:slot>", offset);
 		        while(index > -1 && end > -1)
 		        {
 		            offset = end;
 		            slots += componentString.substring(index, end + 10);
 		            index = componentString.indexOf("<ig:slot", offset + 1);
 	                end = componentString.indexOf("</ig:slot>", index);
 			    }
                 template = "<div style=\"position:relative; padding: 5px 5px 5px 5px; font-family:verdana, sans-serif; font-size:10px; border: 1px solid black;\">" + component.getName() + slots + "</div>";
 		    }
 		    else
 		        template = templateController.getContentAttribute(contentId, templateController.getTemplateAttributeName(), true);
 
 			if(template == null)
 				throw new SystemException("There was no template available on the content with id " + contentId + ". Check so that the templates language are active on your site.");	
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 
 		return template;
 	}
 	
 	/**
 	 * This method fetches the component template as a string.
 	 */
    
 	protected String getComponentPreProcessingTemplateString(TemplateController templateController, Integer contentId, InfoGlueComponent component) throws SystemException, Exception
 	{
 		String template = null;
 
 		try
 		{
 	        template = templateController.getContentAttribute(contentId, "PreTemplate", true);
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 		}
 
 		return template;
 	}
 
 	
 	/**
 	 * This method fetches a subcomponent from either the current page or from a parent node if it's not defined.
 	 */
    
 	protected List getInheritedComponents(Database db, TemplateController templateController, InfoGlueComponent component, Integer siteNodeId, String id, boolean inherit) throws Exception
 	{
 		//logger.info("slotId:" + id);
 		//logger.info("component:" + component);
 		//logger.info("getInheritedComponents with " + component.getName() + ":" + component.getSlotName() + ":" + component.getId());
 		List inheritedComponents = new ArrayList();
 		
 		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(templateController.getSiteNodeId(), templateController.getLanguageId(), templateController.getContentId());
 		
 		Iterator slotIterator = component.getSlotList().iterator();
 		while(slotIterator.hasNext())
 		{
 			Slot slot = (Slot)slotIterator.next();
 			//logger.info("Slot for component " + component.getName() + ":" + slot.getId());
 			//logger.info("Slot for component " + id + ":" + slot.getId() + ":" + slot.getName());
 			if(slot.getId().equalsIgnoreCase(id))
 			{
 				Iterator subComponentIterator = slot.getComponents().iterator();
 				while(subComponentIterator.hasNext())
 				{
 					InfoGlueComponent infoGlueComponent = (InfoGlueComponent)subComponentIterator.next();
 					//logger.info("Adding not inherited component " + infoGlueComponent.getName() + " to list...");
 					inheritedComponents.add(infoGlueComponent);
 				}
 			}
 		}
 		
 		SiteNodeVO parentSiteNodeVO = nodeDeliveryController.getParentSiteNode(db, siteNodeId);
 		
 		boolean restrictAll = false;
 		Iterator restrictionsIterator = component.getRestrictions().iterator();
 		while(restrictionsIterator.hasNext())
 		{
 		    ComponentRestriction restriction = (ComponentRestriction)restrictionsIterator.next();
 		    if(restriction.getType().equalsIgnoreCase("blockComponents"))
 		    {
 		        if(restriction.getSlotId().equalsIgnoreCase(id) && restriction.getArguments().equalsIgnoreCase("*"))
 		        {
 		            restrictAll = true;
 		        }
 		    }
 		}
 		while(inheritedComponents.size() == 0 && parentSiteNodeVO != null && inherit && !restrictAll)
 		{
 		    //logger.info("*********************************************");
 		    //logger.info("*         INHERITING COMPONENTS             *");
 		    //logger.info("*********************************************");
 			String componentXML = this.getPageComponentsString(db, templateController, parentSiteNodeVO.getId(), templateController.getLanguageId(), component.getContentId());
 			//logger.info("componentXML:" + componentXML);
 			//logger.info("id:" + id);
 			
 			String key = "" + parentSiteNodeVO.getId() + "_" + componentXML.hashCode();
 			String mapKey = "" + parentSiteNodeVO.getId() + "_" + componentXML.hashCode() + "_" + id + "_components";
 			
 			Map components = (Map)CacheController.getCachedObjectFromAdvancedCache("componentPropertyCache", mapKey);
 			if(components == null)
 			{
 				//logger.info("No components...");
 				Object componentsCandidate = CacheController.getCachedObjectFromAdvancedCache("componentPropertyCache", key);
 				Document document = null;
 				if(componentsCandidate == null)
 				{
 					//logger.info("Parsing dom document..." + key);
 					document = domBuilder.getDocument(componentXML);
 					//TODO - could be enabled perhaps....
 					//CacheController.cacheObjectInAdvancedCache("componentPropertyCache", key, document, new String[]{}, false);
 				}
 				else
 				{
 					document = (Document)componentsCandidate;
 				}
 				
 				components = getComponent(db, document.getRootElement(), id, templateController, component);
 				//logger.info("components:" + components);
 				//TODO
 				if(components != null)
 					CacheController.cacheObjectInAdvancedCache("componentPropertyCache", mapKey, components, null, false);
 			}
 			
 			//logger.info("components:" + components.size());
 			//logger.info("id:" + id);
 			if(components.containsKey(id))
 			{
 				inheritedComponents = (List)components.get(id);
 				//logger.info("inheritedComponents:" + inheritedComponents);
 				Iterator inheritedComponentIterator = inheritedComponents.iterator();
 				while(inheritedComponentIterator.hasNext())
 				{
 					InfoGlueComponent infoGlueComponent = (InfoGlueComponent)inheritedComponentIterator.next();
 				    infoGlueComponent.setIsInherited(true);
 				}
 			}
 						
 			parentSiteNodeVO = nodeDeliveryController.getParentSiteNode(db, parentSiteNodeVO.getId());
 		}
 			
 		return inheritedComponents;
 	}
 	
 	/**
 	 * This method returns a path to the component so one does not mix them up.
 	 */
 	
 	private String getComponentXPath(InfoGlueComponent infoGlueComponent)
 	{	    
 	    String path = "";
 	    String parentPath = "";
 	    
 	    InfoGlueComponent parentInfoGlueComponent = infoGlueComponent.getParentComponent();
 	    //logger.info("infoGlueComponent.getParentComponent():" + parentInfoGlueComponent);
 	    if(parentInfoGlueComponent != null && parentInfoGlueComponent.getId().intValue() != infoGlueComponent.getId().intValue())
 	    {
 	        //logger.info("Had parent component...:" + parentInfoGlueComponent.getId() + ":" + parentInfoGlueComponent.getName());
 	        parentPath = getComponentXPath(parentInfoGlueComponent);
 	        //logger.info("parentPath:" + parentPath);
 	    }
 	    
 	    //logger.info("infoGlueComponent:" + infoGlueComponent.getSlotName());
 	    path = parentPath + "/components/component[@name='" + infoGlueComponent.getSlotName() + "']";
 	    //logger.info("returning path:" + path);
 	    
 	    return path;
 	}
 	
 	/**
 	 * This method fetches a subcomponent from either the current page or from a parent node if it's not defined.
 	 */
    
 	protected InfoGlueComponent getComponent(Database db, TemplateController templateController, InfoGlueComponent component, Integer siteNodeId, String id) throws Exception
 	{
 		//logger.info("Inside getComponent");
 		//logger.info("component:" + component.getName());
 		//logger.info("siteNodeId:" + siteNodeId);
 		//logger.info("id:" + id);
 		
 		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(templateController.getSiteNodeId(), templateController.getLanguageId(), templateController.getContentId());
 
 		String componentXML = this.getPageComponentsString(db, templateController, siteNodeId, templateController.getLanguageId(), component.getContentId());
 		//logger.info("componentXML:" + componentXML);
 
 		Document document = domBuilder.getDocument(componentXML);
 			
 		Map components = getComponent(db, document.getRootElement(), id, templateController, component);
 		
 		InfoGlueComponent infoGlueComponent = (InfoGlueComponent)components.get(id);
 		//logger.info("infoGlueComponent:" + infoGlueComponent);
 					
 		SiteNodeVO parentSiteNodeVO = nodeDeliveryController.getParentSiteNode(db, siteNodeId);
 		//logger.info("parentSiteNodeVO:" + parentSiteNodeVO);
 
 		while(infoGlueComponent == null && parentSiteNodeVO != null)
 		{
 			componentXML = this.getPageComponentsString(db, templateController, parentSiteNodeVO.getId(), templateController.getLanguageId(), component.getContentId());
 			//logger.info("componentXML:" + componentXML);
 		
 			document = domBuilder.getDocument(componentXML);
 						
 			components = getComponent(db, document.getRootElement(), id, templateController, component);
 			
 			infoGlueComponent = (InfoGlueComponent)components.get(id);
 			//logger.info("infoGlueComponent:" + infoGlueComponent);
 			if(infoGlueComponent != null)
 				infoGlueComponent.setIsInherited(true);
 			
 			parentSiteNodeVO = nodeDeliveryController.getParentSiteNode(db, parentSiteNodeVO.getId());
 			//logger.info("parentSiteNodeVO:" + parentSiteNodeVO);	
 		}
 			
 		//logger.info("*************************STOP**********************");
    	
 		return infoGlueComponent;
 	}
 
 
 	/**
 	 * This method fetches a subcomponent from either the current page or from a parent node if it's not defined.
 	 */
    
 	protected List getComponents(Database db, TemplateController templateController, InfoGlueComponent component, Integer siteNodeId, String id) throws Exception
 	{
 		//logger.info("Inside getComponents");
 		//logger.info("component:" + component.getName());
 		//logger.info("siteNodeId:" + siteNodeId);
 		//logger.info("id:" + id);
 		
 		List subComponents = new ArrayList();
 
 		try
 		{
 		
 		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(templateController.getSiteNodeId(), templateController.getLanguageId(), templateController.getContentId());
 
 		String componentStructureXML = this.getPageComponentsString(db, templateController, siteNodeId, templateController.getLanguageId(), component.getContentId());
 		//logger.info("componentStructureXML:" + componentStructureXML);
 
 		Document document = domBuilder.getDocument(componentStructureXML);
 			
 		Map components = getComponent(db, document.getRootElement(), id, templateController, component);
 		
 		if(components.containsKey(id))
 			subComponents = (List)components.get(id);
 		
 		SiteNodeVO parentSiteNodeVO = nodeDeliveryController.getParentSiteNode(db, siteNodeId);
 		//logger.info("parentSiteNodeVO:" + parentSiteNodeVO);
 
 		while((subComponents == null || subComponents.size() == 0) && parentSiteNodeVO != null)
 		{
 			//logger.info("parentSiteNodeVO:" + parentSiteNodeVO);
 			//logger.info("component:" + component);
 			componentStructureXML = this.getPageComponentsString(db, templateController, parentSiteNodeVO.getId(), templateController.getLanguageId(), component.getContentId());
 			//logger.info("componentStructureXML:" + componentStructureXML);
 		
 			document = domBuilder.getDocument(componentStructureXML);
 						
 			components = getComponent(db, document.getRootElement(), id, templateController, component);
 			
 			if(components.containsKey(id))
 				subComponents = (List)components.get(id);
 				
 			if(subComponents != null)
 			{
 				//logger.info("infoGlueComponent:" + infoGlueComponent);
 				Iterator inheritedComponentsIterator = subComponents.iterator();
 				while(inheritedComponentsIterator.hasNext())
 				{
 					InfoGlueComponent infoGlueComponent = (InfoGlueComponent)inheritedComponentsIterator.next();
 					infoGlueComponent.setIsInherited(true);
 				}
 			}
 			
 			parentSiteNodeVO = nodeDeliveryController.getParentSiteNode(db, parentSiteNodeVO.getId());
 			//logger.info("parentSiteNodeVO:" + parentSiteNodeVO);	
 		}
 			
 		//logger.info("*************************STOP**********************");
 		}
 		catch(Exception e)
 		{
         	logger.warn("An error occurred: " + e.getMessage(), e);
 			throw e;
 		}
 		
 		return subComponents;
 	}
 
 
 	/**
 	 * This method gets the component structure on the page.
 	 *
 	 * @author mattias
 	 */
 	/*
 	protected List getPageComponents(Database db, String componentXML, Element element, String slotName, TemplateController templateController, InfoGlueComponent parentComponent) throws Exception
 	{
 		return getPageComponents(db, componentXML, element, slotName, templateController, parentComponent, null);
 	}
 	*/
 	protected List getPageComponents(Database db, String componentXML, Element element, String slotName, TemplateController templateController, InfoGlueComponent parentComponent, List sortedPageComponents) throws Exception
 	{
 		//List components = new ArrayList();
 		
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(db, templateController.getLanguageId());
 
 		String key = "" + componentXML.hashCode() + "_" +  templateController.getLanguageId() + "_" + slotName;
 		if(parentComponent != null)
 			key = "" + componentXML.hashCode() + "_" +  templateController.getLanguageId() + "_" + slotName + "_" + parentComponent.getId() + "_" + parentComponent.getName() + "_" + parentComponent.getIsInherited();
 		String keyChildComponents = key + "_childComponents";
 			
 		Object componentsCandidate = CacheController.getCachedObjectFromAdvancedCache("pageComponentsCache", key);
 		Object childComponentsCandidate = CacheController.getCachedObjectFromAdvancedCache("pageComponentsCache", keyChildComponents);
 		List components = new ArrayList();
 		List childComponents = new ArrayList();
 		String[] groups = null;
 
 		if(childComponentsCandidate != null)
 		{
 			if(componentsCandidate instanceof NullObject)
 				childComponents = Collections.EMPTY_LIST;
 			else
 				childComponents = (List)childComponentsCandidate;
 		}
 
 		if(componentsCandidate != null)
 		{
 			if(componentsCandidate instanceof NullObject)
 				components = null;
 			else
 				components = (List)componentsCandidate;
 		}
 		else
 		{
 			String componentXPath = "component[@name='" + slotName + "']";
 			//logger.info("componentXPath:" + componentXPath);
 			List componentElements = element.selectNodes(componentXPath);
 			//logger.info("componentElements:" + componentElements.size());
 			Iterator componentIterator = componentElements.iterator();
 			int slotPosition = 0;
 			while(componentIterator.hasNext())
 			{
 				Element componentElement = (Element)componentIterator.next();
 			
 				Integer id 							= new Integer(componentElement.attributeValue("id"));
 				Integer contentId 					= new Integer(componentElement.attributeValue("contentId"));
 				String name 	  					= componentElement.attributeValue("name");
 				String isInherited 					= componentElement.attributeValue("isInherited");
 				String pagePartTemplateContentId 	= componentElement.attributeValue("pagePartTemplateContentId");
 				
 				//logger.info("id 1: " + id);
 				//logger.info("contentId 1:" + contentId);
 				//logger.info("name 1: " + name);
 				//logger.info("isInherited 1: " + isInherited);
 				//logger.info("pagePartTemplateContentId 1: " + pagePartTemplateContentId);
 				
 				try
 				{
 				    ContentVO contentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(contentId, db);
 				    
 				    //logger.info("slotName:" + slotName + " should get connected with content_" + contentVO.getId());
 				    
 				    groups = new String[]{"content_" + contentVO.getId()};
 				    
 					InfoGlueComponent component = new InfoGlueComponent();
 					component.setPositionInSlot(new Integer(slotPosition));
 					component.setId(id);
 					component.setContentId(contentId);
 					component.setName(contentVO.getName());
 					component.setSlotName(name);
 					component.setParentComponent(parentComponent);
 					if(isInherited != null && isInherited.equals("true"))
 						component.setIsInherited(true);
 					else if(parentComponent != null)
 						component.setIsInherited(parentComponent.getIsInherited());
 
 					if(pagePartTemplateContentId != null && !pagePartTemplateContentId.equals("") && !pagePartTemplateContentId.equals("-1"))
 					{
 						Integer pptContentId = new Integer(pagePartTemplateContentId);
 					    ContentVO pptContentIdContentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(pptContentId, db);
 
 						InfoGlueComponent partTemplateReferenceComponent = new InfoGlueComponent();
 						partTemplateReferenceComponent.setPositionInSlot(new Integer(slotPosition));
 						partTemplateReferenceComponent.setId(id);
 						partTemplateReferenceComponent.setContentId(pptContentId);
 						partTemplateReferenceComponent.setName(pptContentIdContentVO.getName());
 						partTemplateReferenceComponent.setSlotName(name);
 						partTemplateReferenceComponent.setParentComponent(parentComponent);
 						partTemplateReferenceComponent.setIsInherited(true);
 						
 						component.setPagePartTemplateContentId(pptContentId);
 						component.setPagePartTemplateComponent(partTemplateReferenceComponent);
 					}
 			
 					//Use this later
 					//getComponentProperties(componentElement, component, locale, templateController);
 					List propertiesNodeList = componentElement.selectNodes("properties");
 					if(propertiesNodeList.size() > 0)
 					{
 						Element propertiesElement = (Element)propertiesNodeList.get(0);
 						
 						List propertyNodeList = propertiesElement.selectNodes("property");
 						Iterator propertyNodeListIterator = propertyNodeList.iterator();
 						while(propertyNodeListIterator.hasNext())
 						{
 							Element propertyElement = (Element)propertyNodeListIterator.next();
 							
 							String propertyName = propertyElement.attributeValue("name");
 							String type = propertyElement.attributeValue("type");
 							String path = propertyElement.attributeValue("path");
 			
 							if(path == null)
 							{
 								LanguageVO langaugeVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), templateController.getSiteNodeId());
 								if(propertyElement.attributeValue("path_" + langaugeVO.getLanguageCode()) != null)
 									path = propertyElement.attributeValue("path_" + langaugeVO.getLanguageCode());
 							}
 								
 							if(propertyElement.attributeValue("path_" + locale.getLanguage()) != null)
 								path = propertyElement.attributeValue("path_" + locale.getLanguage());
 					
 							if(path == null || path.equals(""))
 							{
 								logger.info("Falling back to content master language 1 for property:" + propertyName);
 								LanguageVO contentMasterLangaugeVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(getDatabase(), contentVO.getRepositoryId());
 								if(propertyElement.attributeValue("path_" + contentMasterLangaugeVO.getLanguageCode()) != null)
 									path = propertyElement.attributeValue("path_" + contentMasterLangaugeVO.getLanguageCode());	
 							}
 							
 							Map property = new HashMap();
 							property.put("name", propertyName);
 							property.put("path", path);
 							property.put("type", type);
 							
 							List attributes = propertyElement.attributes();
 							Iterator attributesIterator = attributes.iterator();
 							while(attributesIterator.hasNext())
 							{
 								Attribute attribute = (Attribute)attributesIterator.next();
 								if(attribute.getName().startsWith("path_"))
 									property.put(attribute.getName(), attribute.getValue());
 							}
 							
 							if(path != null)
 							{
 								if(propertyName.equals(InfoGlueComponent.CACHE_RESULT_PROPERTYNAME) && (path.equalsIgnoreCase("true") || path.equalsIgnoreCase("yes")))
 								{
 									component.setCacheResult(true);
 								}
 								if(propertyName.equals(InfoGlueComponent.UPDATE_INTERVAL_PROPERTYNAME) && !path.equals(""))
 								{
 									try { component.setUpdateInterval(Integer.parseInt(path)); } catch (Exception e) { logger.warn("The component " + component.getName() + " " + InfoGlueComponent.UPDATE_INTERVAL_PROPERTYNAME + " with a faulty value on page " + this.getTemplateController().getOriginalFullURL() + ":" + e.getMessage()); }
 								}
 								if(propertyName.equals(InfoGlueComponent.CACHE_KEY_PROPERTYNAME) && !path.equals(""))
 								{
 									component.setCacheKey(path);
 								}
 								if(propertyName.equals(InfoGlueComponent.PREPROCESSING_ORDER_PROPERTYNAME) && !path.equals(""))
 								{
 									component.setPreProcessingOrder(path);
 								}
 							}
 							
 							List<ComponentBinding> bindings = new ArrayList<ComponentBinding>();
 							List bindingNodeList = propertyElement.selectNodes("binding");
 							Iterator bindingNodeListIterator = bindingNodeList.iterator();
 							while(bindingNodeListIterator.hasNext())
 							{
 								Element bindingElement = (Element)bindingNodeListIterator.next();
 								String entity = bindingElement.attributeValue("entity");
 								String entityId = bindingElement.attributeValue("entityId");
 								String assetKey = bindingElement.attributeValue("assetKey");
 
 								ComponentBinding componentBinding = new ComponentBinding();
 								//componentBinding.setId(new Integer(id));
 								//componentBinding.setComponentId(componentId);
 								componentBinding.setEntityClass(entity);
 								componentBinding.setEntityId(new Integer(entityId));
 								componentBinding.setAssetKey(assetKey);
 								componentBinding.setBindingPath(path);
 								
 								bindings.add(componentBinding);
 							}
 			
 							property.put("bindings", bindings);
 							
 							component.getProperties().put(propertyName, property);
 						}
 					}
 					
 					
 					getComponentRestrictions(componentElement, component, locale, templateController);
 					
 					//Getting slots for the component
 					try
 					{
 						String componentString = this.getComponentString(templateController, contentId, component);
 						int offset = 0;
 						int slotStartIndex = componentString.indexOf("<ig:slot", offset);
 						while(slotStartIndex > -1)
 						{
 							int slotStopIndex = componentString.indexOf("</ig:slot>", slotStartIndex);
 							String slotString = componentString.substring(slotStartIndex, slotStopIndex + 10);
 							String slotId = slotString.substring(slotString.indexOf("id") + 4, slotString.indexOf("\"", slotString.indexOf("id") + 4));
 							
 							boolean inherit = true;
 							int inheritIndex = slotString.indexOf("inherit");
 							if(inheritIndex > -1)
 							{    
 							    String inheritString = slotString.substring(inheritIndex + 9, slotString.indexOf("\"", inheritIndex + 9));
 							    inherit = Boolean.parseBoolean(inheritString);
 							}
 	
 							boolean disableAccessControl = false;
 							int disableAccessControlIndex = slotString.indexOf("disableAccessControl");
 							if(disableAccessControlIndex > -1)
 							{    
 							    String disableAccessControlString = slotString.substring(disableAccessControlIndex + "disableAccessControl".length() + 2, slotString.indexOf("\"", disableAccessControlIndex + "disableAccessControl".length() + 2));
 							    disableAccessControl = Boolean.parseBoolean(disableAccessControlString);
 							}
 
 							String[] allowedComponentNamesArray = null;
 							int allowedComponentNamesIndex = slotString.indexOf(" allowedComponentNames");
 							if(allowedComponentNamesIndex > -1)
 							{    
 							    String allowedComponentNames = slotString.substring(allowedComponentNamesIndex + 24, slotString.indexOf("\"", allowedComponentNamesIndex + 24));
 							    allowedComponentNamesArray = allowedComponentNames.split(",");
 							}
 
 							String[] disallowedComponentNamesArray = null;
 							int disallowedComponentNamesIndex = slotString.indexOf(" disallowedComponentNames");
 							if(disallowedComponentNamesIndex > -1)
 							{    
 							    String disallowedComponentNames = slotString.substring(disallowedComponentNamesIndex + 27, slotString.indexOf("\"", disallowedComponentNamesIndex + 27));
 							    disallowedComponentNamesArray = disallowedComponentNames.split(",");
 							}
 							
 							String[] allowedComponentGroupNamesArray = null;
 							int allowedComponentGroupNamesIndex = slotString.indexOf(" allowedComponentGroupNames");
 							if(allowedComponentGroupNamesIndex > -1)
 							{    
 							    String allowedComponentGroupNames = slotString.substring(allowedComponentGroupNamesIndex + 29, slotString.indexOf("\"", allowedComponentGroupNamesIndex + 29));
 							    allowedComponentGroupNamesArray = allowedComponentGroupNames.split(",");
 							}
 
 							String addComponentText = null;
 							int addComponentTextIndex = slotString.indexOf("addComponentText");
 							if(addComponentTextIndex > -1)
 							{    
 							    addComponentText = slotString.substring(addComponentTextIndex + "addComponentText".length() + 2, slotString.indexOf("\"", addComponentTextIndex + "addComponentText".length() + 2));
 							}
 
 							String addComponentLinkHTML = null;
 							int addComponentLinkHTMLIndex = slotString.indexOf("addComponentLinkHTML");
 							if(addComponentLinkHTMLIndex > -1)
 							{    
 							    addComponentLinkHTML = slotString.substring(addComponentLinkHTMLIndex + "addComponentLinkHTML".length() + 2, slotString.indexOf("\"", addComponentLinkHTMLIndex + "addComponentLinkHTML".length() + 2));
 							}
 
 							int allowedNumberOfComponentsInt = -1;
 							int allowedNumberOfComponentsIndex = slotString.indexOf("allowedNumberOfComponents");
 							if(allowedNumberOfComponentsIndex > -1)
 							{    
 								String allowedNumberOfComponents = slotString.substring(allowedNumberOfComponentsIndex + "allowedNumberOfComponents".length() + 2, slotString.indexOf("\"", allowedNumberOfComponentsIndex + "allowedNumberOfComponents".length() + 2));
 								try
 								{
 									allowedNumberOfComponentsInt = new Integer(allowedNumberOfComponents);
 								}
 								catch (Exception e) 
 								{
 									allowedNumberOfComponentsInt = -1;
 								}
 							}
 
 							Slot slot = new Slot();
 							slot.setId(slotId);
 							slot.setInherit(inherit);
 							slot.setDisableAccessControl(disableAccessControl);
 							slot.setAllowedComponentsArray(allowedComponentNamesArray);
 							slot.setDisallowedComponentsArray(disallowedComponentNamesArray);
 							slot.setAllowedComponentGroupsArray(allowedComponentGroupNamesArray);
 						    slot.setAddComponentLinkHTML(addComponentLinkHTML);
 						    slot.setAddComponentText(addComponentText);
 						    slot.setAllowedNumberOfComponents(new Integer(allowedNumberOfComponentsInt));
 
 							Element componentsElement = (Element)componentElement.selectSingleNode("components");
 							
 							//groups = new String[]{"content_" + contentVO.getId()};
 							
 							List tempChildComponents = new ArrayList();
 							List subComponents = getPageComponents(db, componentXML, componentsElement, slotId, templateController, component, childComponents);
 							childComponents.addAll(tempChildComponents);
 							slot.setComponents(subComponents);
 							
 							component.getSlotList().add(slot);
 					
 							offset = slotStopIndex;
 							slotStartIndex = componentString.indexOf("<ig:slot", offset);
 						}
 					}
 					catch(Exception e)
 					{		
 						logger.warn("An component with either an empty template or with no template in the sitelanguages was found:" + e.getMessage(), e);	
 					}
 					
 					components.add(component);
 				}
 				catch(Exception e)
 				{
 					logger.warn("There was deleted referenced component or some other problem when rendering siteNode: " + templateController.getCurrentPagePath() + "(" + templateController.getSiteNodeId() + ") in language " + templateController.getLanguageId() + ":" + e.getMessage(), e);
 				}
 				slotPosition++;
 			}			
 
 			if(groups == null)
 				groups = new String[]{"selectiveCacheUpdateNonApplicable"};
 			
 			if(components != null)
 			{
 				CacheController.cacheObjectInAdvancedCache("pageComponentsCache", key, components, groups, false);				
 				CacheController.cacheObjectInAdvancedCache("pageComponentsCache", keyChildComponents, childComponents, groups, false);				
 			}
 			else
 			{
 				CacheController.cacheObjectInAdvancedCache("pageComponentsCache", key, new NullObject(), groups, false);
 				CacheController.cacheObjectInAdvancedCache("pageComponentsCache", keyChildComponents, new NullObject(), groups, false);
 			}
 		}		
 		
 		//logger.info("sortedPageComponents:" + sortedPageComponents.size());
 		//logger.info("childComponents:" + childComponents.size());
 		if(sortedPageComponents != null)
 		{
 			sortedPageComponents.addAll(components);
 			sortedPageComponents.addAll(childComponents);
 		}
 		
 		return components;
 	}
 
 	/**
 	 * This method gets the component properties
 	 */
 	/*
 	private void getComponentProperties(Element child, InfoGlueComponent component, Locale locale, TemplateController templateController) throws Exception
 	{
 		List propertiesNodeList = child.selectNodes("properties");
 		//logger.info("propertiesNodeList:" + propertiesNodeList.getLength());
 		if(propertiesNodeList.size() > 0)
 		{
 			Element propertiesElement = (Element)propertiesNodeList.get(0);
 			
 			List propertyNodeList = propertiesElement.selectNodes("property");
 			//logger.info("propertyNodeList:" + propertyNodeList.getLength());
 			Iterator propertyNodeListIterator = propertyNodeList.iterator();
 			while(propertyNodeListIterator.hasNext())
 			{
 				Element propertyElement = (Element)propertyNodeListIterator.next();
 				
 				String propertyName = propertyElement.attributeValue("name");
 				String type = propertyElement.attributeValue("type");
 				String path = propertyElement.attributeValue("path");
 
 				if(path == null)
 				{
 					LanguageVO langaugeVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), templateController.getSiteNodeId());
 					if(propertyElement.attributeValue("path_" + langaugeVO.getLanguageCode()) != null)
 						path = propertyElement.attributeValue("path_" + langaugeVO.getLanguageCode());
 				}
 
 				//logger.info("path:" + "path_" + locale.getLanguage() + ":" + propertyElement.attributeValue("path_" + locale.getLanguage()));
 				if(propertyElement.attributeValue("path_" + locale.getLanguage()) != null)
 					path = propertyElement.attributeValue("path_" + locale.getLanguage());
 				//logger.info("path:" + path);
 
 				if(path == null || path.equals(""))
 				{
 					logger.info("Falling back to content master language 1 for property:" + propertyName);
 					LanguageVO contentMasterLangaugeVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(getDatabase(), contentVO.getRepositoryId());
 					if(propertyElement.attributeValue("path_" + contentMasterLangaugeVO.getLanguageCode()) != null)
 						path = propertyElement.attributeValue("path_" + contentMasterLangaugeVO.getLanguageCode());	
 				}
 
 				Map property = new HashMap();
 				property.put("name", propertyName);
 				property.put("path", path);
 				property.put("type", type);
 				
 				List bindings = new ArrayList();
 				List bindingNodeList = propertyElement.selectNodes("binding");
 				//logger.info("bindingNodeList:" + bindingNodeList.getLength());
 				Iterator bindingNodeListIterator = bindingNodeList.iterator();
 				while(bindingNodeListIterator.hasNext())
 				{
 					Element bindingElement = (Element)bindingNodeListIterator.next();
 					String entity = bindingElement.attributeValue("entity");
 					String entityId = bindingElement.attributeValue("entityId");
 					String assetKey = bindingElement.attributeValue("assetKey");
 					//logger.info("Binding found:" + entity + ":" + entityId);
 					
 					ComponentBinding componentBinding = new ComponentBinding();
 					//componentBinding.setId(new Integer(id));
 					//componentBinding.setComponentId(componentId);
 					componentBinding.setEntityClass(entity);
 					componentBinding.setEntityId(new Integer(entityId));
 					componentBinding.setAssetKey(assetKey);
 					componentBinding.setBindingPath(path);
 					
 					bindings.add(componentBinding);
 				}
 
 				property.put("bindings", bindings);
 				
 				component.getProperties().put(propertyName, property);
 			}
 		}
 	}
 	*/
 
 	/**
 	 * This method gets the restrictions for this component
 	 */
 	private void getComponentRestrictions(Element child, InfoGlueComponent component, Locale locale, TemplateController templateController) throws Exception
 	{
 	    //logger.info("Getting restrictions for " + component.getId() + ":" + child.getName());
 		List restrictionsNodeList = child.selectNodes("restrictions");
 		//logger.info("restrictionsNodeList:" + restrictionsNodeList.getLength());
 		if(restrictionsNodeList.size() > 0)
 		{
 			Element restrictionsElement = (Element)restrictionsNodeList.get(0);
 			
 			List restrictionNodeList = restrictionsElement.selectNodes("restriction");
 			//logger.info("restrictionNodeList:" + restrictionNodeList.getLength());
 			Iterator restrictionNodeListIterator = restrictionNodeList.iterator();
 			while(restrictionNodeListIterator.hasNext())
 			{
 				Element restrictionElement = (Element)restrictionNodeListIterator.next();
 				
 				ComponentRestriction restriction = new ComponentRestriction();
 			    
 				String type = restrictionElement.attributeValue("type");
 				if(type.equals("blockComponents"))
 				{
 				    String slotId = restrictionElement.attributeValue("slotId");
 				    String arguments = restrictionElement.attributeValue("arguments");
 
 				    restriction.setType(type);
 					restriction.setSlotId(slotId);
 					restriction.setArguments(arguments);
 				}
 				
 				component.getRestrictions().add(restriction);
 			}
 		}
 	}
 
 }
