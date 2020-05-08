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
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.XMLHelper;
 import org.infoglue.cms.util.dom.DOMBuilder;
 import org.infoglue.deliver.applications.actions.InfoGlueComponent;
 import org.infoglue.deliver.applications.databeans.ComponentRestriction;
 import org.infoglue.deliver.applications.databeans.Slot;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ComponentLogic;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.VelocityTemplateProcessor;
 
 /**
 * @author Mattias Bogeblad
 *
 * This class delivers a normal html page by using the component-based method.
 */
 
 public class ComponentBasedHTMLPageInvoker extends PageInvoker
 {
    
     private final static Logger logger = Logger.getLogger(ComponentBasedHTMLPageInvoker.class.getName());
 
    /**
 	 * This method should return an instance of the class that should be used for page editing inside the tools or in working. 
 	 * Makes it possible to have an alternative to the ordinary delivery optimized class.
 	 */
 	
    public PageInvoker getDecoratedPageInvoker() throws SystemException
 	{
 	    return new DecoratedComponentBasedHTMLPageInvoker();
 	}
 
 	/**
 	 * This is the method that will render the page. It uses the new component based structure. 
 	 */ 
 
 	public void invokePage() throws SystemException, Exception
 	{
 		String pageContent = "";
 		
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(this.getDeliveryContext());
 		IntegrationDeliveryController integrationDeliveryController = IntegrationDeliveryController.getIntegrationDeliveryController(this.getDeliveryContext());
 		
 		Integer repositoryId = nodeDeliveryController.getSiteNode(getDatabase(), this.getDeliveryContext().getSiteNodeId()).getRepository().getId();
 
 		String componentXML = getPageComponentsString(getDatabase(), this.getTemplateController(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
 		
    		if(componentXML != null && componentXML.length() != 0)
 		{
 			Document document = new DOMBuilder().getDocument(componentXML);
 
 			List pageComponents = getPageComponents(getDatabase(), document.getRootElement(), "base", this.getTemplateController(), null);
 
 			InfoGlueComponent baseComponent = null;
 			if(pageComponents.size() > 0)
 			{
 				baseComponent = (InfoGlueComponent)pageComponents.get(0);
 			}
 			
 			if(baseComponent != null)
 			{
 				ContentVO metaInfoContentVO = nodeDeliveryController.getBoundContent(getDatabase(), this.getTemplateController().getPrincipal(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), true, "Meta information", this.getDeliveryContext());
 
 				pageContent = renderComponent(baseComponent, this.getTemplateController(), repositoryId, this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId(), metaInfoContentVO.getId());
 			}
 		}
 
 		Map context = getDefaultContext();
 		StringWriter cacheString = new StringWriter();
 		PrintWriter cachedStream = new PrintWriter(cacheString);
 		new VelocityTemplateProcessor().renderTemplate(context, cachedStream, pageContent);
 		
 		String pageString = cacheString.toString();
 			
 		pageString = this.getTemplateController().decoratePage(pageString);
 		
 		this.setPageString(pageString);
 
 	}
 	
 	
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
 	    
 		String attributeName = "ComponentStructure";
 		String attributeKey = "" + contentVO.getId() + "_" + languageId + "_" + attributeName + "_" + siteNodeId + "_" + true;
 	    
 		String versionKey 	= attributeKey + "_contentVersionId";
 
 	    String cachedPageComponentsString = (String)CacheController.getCachedObject(cacheName, cacheKey);
 	    //Integer contentVersionId = (Integer)CacheController.getCachedObject("contentAttributeCache", versionKey);
 	    Integer contentVersionId = (Integer)CacheController.getCachedObjectFromAdvancedCache("contentAttributeCache", versionKey);
 		
 
 		if(cachedPageComponentsString != null)
 		{
 		    //logger.info("Returning cached...");
 		    //logger.info("First added..." + versionKey + ":" + "contentVersion:" + contentVersionId);
 		    templateController.getDeliveryContext().addUsedContentVersion("contentVersion_" + contentVersionId);
 		    return cachedPageComponentsString;
 		}
 		
 		String pageComponentsString = null;
    					
 		//logger.info("contentVO in getPageComponentsString: " + contentVO.getContentId());
		Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), siteNodeId).getId();
 	    pageComponentsString = templateController.getContentAttribute(contentVO.getContentId(), masterLanguageId, "ComponentStructure", true);
 		
 		if(pageComponentsString == null)
 			throw new SystemException("There was no Meta Information bound to this page which makes it impossible to render.");	
 				    
 		logger.info("pageComponentsString: " + pageComponentsString);
 	
 		CacheController.cacheObject(cacheName, cacheKey, pageComponentsString);
 		
 		return pageComponentsString;
 	}
 
 
 	/**
 	 * This method fetches the pageComponent structure as a document.
 	 */
 	    
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
 
 	
 	/**
 	 * This method gets a Map of the components available on the page.
 	 */
 
 	protected Map getComponents(Database db, Element element, TemplateController templateController, InfoGlueComponent parentComponent) throws Exception
 	{
 		InfoGlueComponent component = null;
 
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(db, templateController.getLanguageId());
 		
 		Map components = new HashMap();
 		
 		String componentXPath = "component";
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
 			
 			//Change to this later
 			//getComponentProperties(child, component, locale, templateController);
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
 						//logger.info("Binding found:" + entity + ":" + entityId);
 						if(entity.equalsIgnoreCase("Content"))
 						{
 							bindings.add(entityId);
 						}
 						else
 						{
 							bindings.add(entityId); 
 						} 
 					}
 	
 					property.put("bindings", bindings);
 					
 					component.getProperties().put(propertyName, property);
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
 				    inherit = Boolean.getBoolean(inheritString);
 				}
 
 				String[] allowedComponentNamesArray = null;
 				int allowedComponentNamesIndex = slotString.indexOf("allowedComponentNames");
 				if(allowedComponentNamesIndex > -1)
 				{    
 				    String allowedComponentNames = slotString.substring(allowedComponentNamesIndex + 23, slotString.indexOf("\"", allowedComponentNamesIndex + 23));
 				    allowedComponentNamesArray = allowedComponentNames.split(",");
 				}
 				
 			  	Slot slot = new Slot();
 			  	slot.setId(slotId);
 			    slot.setInherit(inherit);
 			  	slot.setAllowedComponentsArray(allowedComponentNamesArray);
 			    
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
 			////logger.info("Name:" + name);
 
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
 
 					Map property = new HashMap();
 					property.put("name", propertyName);
 					property.put("path", path);
 					property.put("type", type);
 					
 					List bindings = new ArrayList();
 					List bindingNodeList = propertyElement.selectNodes("binding");
 					////logger.info("bindingNodeList:" + bindingNodeList.getLength());
 					Iterator bindingNodeListIterator = bindingNodeList.iterator();
 					while(bindingNodeListIterator.hasNext())
 					{
 						Element bindingElement = (Element)bindingNodeListIterator.next();
 						String entity = bindingElement.attributeValue("entity");
 						String entityId = bindingElement.attributeValue("entityId");
 						////logger.info("Binding found:" + entity + ":" + entityId);
 						if(entity.equalsIgnoreCase("Content"))
 						{
 							////logger.info("Content added:" + entity + ":" + entityId);
 							bindings.add(entityId);
 						}
 						else
 						{
 							////logger.info("SiteNode added:" + entity + ":" + entityId);
 							bindings.add(entityId); 
 						} 
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
 
 	private String renderComponent(InfoGlueComponent component, TemplateController templateController, Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId, Integer metainfoContentId) throws Exception
 	{
 		String decoratedComponent = "";
 		
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 		templateController.setComponentLogic(new ComponentLogic(templateController, component));
 		templateController.getDeliveryContext().getUsageListeners().add(templateController.getComponentLogic().getComponentDeliveryContext());
 
 	    boolean renderComponent = false;
 	    boolean cacheComponent = false;
 
 		String cacheResult 		= templateController.getComponentLogic().getPropertyValue("CacheResult", true, false);
 		String updateInterval 	= templateController.getComponentLogic().getPropertyValue("UpdateInterval", true, false);
 
 		if(cacheResult == null || !cacheResult.equalsIgnoreCase("true"))
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
 	    
 	    if(!renderComponent)
 	    {
             //logger.info("ComponentKey:" + templateController.getComponentLogic().getComponentDeliveryContext().getComponentKey());
 	        if(updateInterval != null && !updateInterval.equals("") && !updateInterval.equals("-1"))
 	            decoratedComponent = (String)CacheController.getCachedObjectFromAdvancedCache("componentCache", templateController.getComponentLogic().getComponentDeliveryContext().getComponentKey(), new Integer(updateInterval).intValue());
 		    else
 		        decoratedComponent = (String)CacheController.getCachedObjectFromAdvancedCache("componentCache", templateController.getComponentLogic().getComponentDeliveryContext().getComponentKey());
 		    
 	        if(decoratedComponent == null)
 		        renderComponent = true;
 		}
 	    
 	    //logger.info("Will we render component:" + component.getName() + ":" + renderComponent);
 	    
 		if(renderComponent)
 	    {
 		    decoratedComponent = "";
 		    
 			try
 			{
 			    String componentString = getComponentString(templateController, component.getContentId(), component); 
 				
 				Map context = getDefaultContext();
 		    	context.put("templateLogic", templateController);
 		    	StringWriter cacheString = new StringWriter();
 				PrintWriter cachedStream = new PrintWriter(cacheString);
 				new VelocityTemplateProcessor().renderTemplate(context, cachedStream, componentString);
 				componentString = cacheString.toString();
 
 				int offset = 0;
 				int slotStartIndex = componentString.indexOf("<ig:slot", offset);
 				int slotStopIndex = 0;
 				
 				while(slotStartIndex > -1)
 				{
 					if(offset > 0)
 						decoratedComponent += componentString.substring(offset + 10, slotStartIndex);
 					else
 						decoratedComponent += componentString.substring(offset, slotStartIndex);
 					
 					slotStopIndex = componentString.indexOf("</ig:slot>", slotStartIndex);
 					
 					String slot = componentString.substring(slotStartIndex, slotStopIndex + 10);
 					String id = slot.substring(slot.indexOf("id") + 4, slot.indexOf("\"", slot.indexOf("id") + 4));
 					
 					boolean inherit = true;
 					int inheritIndex = slot.indexOf("inherit");
 					if(inheritIndex > -1)
 					{    
 					    String inheritString = slot.substring(inheritIndex + 9, slot.indexOf("\"", inheritIndex + 9));
 					    inherit = Boolean.getBoolean(inheritString);
 					}
 
 					List subComponents = getInheritedComponents(templateController.getDatabase(), templateController, component, templateController.getSiteNodeId(), id, inherit);
 					Iterator subComponentsIterator = subComponents.iterator();
 					while(subComponentsIterator.hasNext())
 					{
 						InfoGlueComponent subComponent = (InfoGlueComponent)subComponentsIterator.next();
 						//logger.info(component.getName() + " had subcomponent " + subComponent.getName() + ":" + subComponent.getId());
 						String subComponentString = "";
 						if(subComponent != null)
 						{
 							subComponentString = renderComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId, metainfoContentId);
 						}
 						decoratedComponent += subComponentString.trim();	
 					}
 					
 					offset = slotStopIndex;
 					slotStartIndex = componentString.indexOf("<ig:slot", offset);
 				}
 				
 				if(offset > 0)
 				{	
 					decoratedComponent += componentString.substring(offset + 10);
 				}
 				else
 				{	
 					decoratedComponent += componentString.substring(offset);
 				}
 
 		        if(cacheComponent)
 		        {
 		            logger.info("The component used: " + templateController.getComponentLogic().getComponentDeliveryContext().getAllUsedEntities().length);
 		            if(this.getTemplateController().getOperatingMode().intValue() == 3)
 		                CacheController.cacheObjectInAdvancedCache("componentCache", templateController.getComponentLogic().getComponentDeliveryContext().getComponentKey(), decoratedComponent, templateController.getComponentLogic().getComponentDeliveryContext().getAllUsedEntities(), false);
 		            else
 		                CacheController.cacheObjectInAdvancedCache("componentCache", templateController.getComponentLogic().getComponentDeliveryContext().getComponentKey(), decoratedComponent, templateController.getComponentLogic().getComponentDeliveryContext().getAllUsedEntities(), true);
 		        }	    
 
 			}
 			catch(Exception e)
 			{		
 			    e.printStackTrace();
 				logger.warn("An component with either an empty template or with no template in the sitelanguages was found:" + e.getMessage(), e);	
 			}    	
 
 		}
 		
 		templateController.getDeliveryContext().getUsageListeners().remove(templateController.getComponentLogic().getComponentDeliveryContext());
 		//logger.info("decoratedComponent:" + decoratedComponent);
 		
 		return decoratedComponent;
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
                 template = "<div style=\"position:relative; awidth:90%; padding: 5px 5px 5px 5px; font-family:verdana, sans-serif; font-size:10px; border: 1px solid black;\">" + component.getName() + slots + "</div>";
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
 	 * This method fetches a subcomponent from either the current page or from a parent node if it's not defined.
 	 */
    
 	protected List getInheritedComponents(Database db, TemplateController templateController, InfoGlueComponent component, Integer siteNodeId, String id, boolean inherit) throws Exception
 	{
 	    //logger.info("slotId");
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
 		
 			Document document = new DOMBuilder().getDocument(componentXML);
 						
 			Map components = getComponent(db, document.getRootElement(), id, templateController, component);
 			//logger.info("components:" + components.size());
 			
 			if(components.containsKey(id))
 			{
 				inheritedComponents = (List)components.get(id);
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
 
 		Document document = new DOMBuilder().getDocument(componentXML);
 			
 		Map components = getComponent(db, document.getRootElement(), id, templateController, component);
 		
 		InfoGlueComponent infoGlueComponent = (InfoGlueComponent)components.get(id);
 		//logger.info("infoGlueComponent:" + infoGlueComponent);
 					
 		SiteNodeVO parentSiteNodeVO = nodeDeliveryController.getParentSiteNode(db, siteNodeId);
 		//logger.info("parentSiteNodeVO:" + parentSiteNodeVO);
 
 		while(infoGlueComponent == null && parentSiteNodeVO != null)
 		{
 			componentXML = this.getPageComponentsString(db, templateController, parentSiteNodeVO.getId(), templateController.getLanguageId(), component.getContentId());
 			//logger.info("componentXML:" + componentXML);
 		
 			document = new DOMBuilder().getDocument(componentXML);
 						
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
 
 		Document document = new DOMBuilder().getDocument(componentStructureXML);
 			
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
 		
 			document = new DOMBuilder().getDocument(componentStructureXML);
 						
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
 			e.printStackTrace(System.out);
 			throw e;
 		}
 		
 		return subComponents;
 	}
 
 
 	/**
 	 * This method gets the component structure on the page.
 	 *
 	 * @author mattias
 	 */
 
 	protected List getPageComponents(Database db, Element element, String slotName, TemplateController templateController, InfoGlueComponent parentComponent) throws Exception
 	{
 		List components = new ArrayList();
 		
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(db, templateController.getLanguageId());
 
 		OutputFormat format = OutputFormat.createPrettyPrint();
 		XMLWriter writer = new XMLWriter( System.out, format );
 		//writer.write( element );
 						
 		String componentXPath = "component[@name='" + slotName + "']";
 		List componentElements = element.selectNodes(componentXPath);
 		//logger.info("componentElements:" + componentElements.size());
 		Iterator componentIterator = componentElements.iterator();
 		while(componentIterator.hasNext())
 		{
 			Element componentElement = (Element)componentIterator.next();
 		
 			Integer id 			= new Integer(componentElement.attributeValue("id"));
 			Integer contentId 	= new Integer(componentElement.attributeValue("contentId"));
 			String name 	  	= componentElement.attributeValue("name");
 			
 			try
 			{
 			    ContentVO contentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(contentId, db);
 			
 				InfoGlueComponent component = new InfoGlueComponent();
 				component.setId(id);
 				component.setContentId(contentId);
 				component.setName(contentVO.getName());
 				component.setSlotName(name);
 				component.setParentComponent(parentComponent);
 		
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
 				
 						Map property = new HashMap();
 						property.put("name", propertyName);
 						property.put("path", path);
 						property.put("type", type);
 						
 						List bindings = new ArrayList();
 						List bindingNodeList = propertyElement.selectNodes("binding");
 						Iterator bindingNodeListIterator = bindingNodeList.iterator();
 						while(bindingNodeListIterator.hasNext())
 						{
 							Element bindingElement = (Element)bindingNodeListIterator.next();
 							String entity = bindingElement.attributeValue("entity");
 							String entityId = bindingElement.attributeValue("entityId");
 							if(entity.equalsIgnoreCase("Content"))
 							{
 								bindings.add(entityId);
 							}
 							else
 							{
 								bindings.add(entityId); 
 							} 
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
 						    //System.out.println("inheritString:" + inheritString);
 						    inherit = Boolean.getBoolean(inheritString);
 						}
 
 						String[] allowedComponentNamesArray = null;
 						int allowedComponentNamesIndex = slotString.indexOf("allowedComponentNames");
 						if(allowedComponentNamesIndex > -1)
 						{    
 						    String allowedComponentNames = slotString.substring(allowedComponentNamesIndex + 23, slotString.indexOf("\"", allowedComponentNamesIndex + 23));
 						    //System.out.println("allowedComponentNames:" + allowedComponentNames);
 						    allowedComponentNamesArray = allowedComponentNames.split(",");
 						}
 
 						Slot slot = new Slot();
 						slot.setId(slotId);
 						slot.setInherit(inherit);
 						slot.setAllowedComponentsArray(allowedComponentNamesArray);
 						
 						Element componentsElement = (Element)componentElement.selectSingleNode("components");
 						
 						List subComponents = getPageComponents(db, componentsElement, slotId, templateController, component);
 						//logger.info("subComponents:" + subComponents);
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
 			    e.printStackTrace();
 			}
 			
 
 		}		
 		
 		return components;
 	}
 
 	/**
 	 * This method gets the component properties
 	 */
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
 					//logger.info("Binding found:" + entity + ":" + entityId);
 					if(entity.equalsIgnoreCase("Content"))
 					{
 						bindings.add(entityId);
 					}
 					else
 					{
 						bindings.add(entityId); 
 					} 
 				}
 
 				property.put("bindings", bindings);
 				
 				component.getProperties().put(propertyName, property);
 			}
 		}
 	}
 
 
 	/**
 	 * This method gets the restrictions for this component
 	 */
 	private void getComponentRestrictions(Element child, InfoGlueComponent component, Locale locale, TemplateController templateController) throws Exception
 	{
 	    //System.out.println("Getting restrictions for " + component.getId() + ":" + child.getName());
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
