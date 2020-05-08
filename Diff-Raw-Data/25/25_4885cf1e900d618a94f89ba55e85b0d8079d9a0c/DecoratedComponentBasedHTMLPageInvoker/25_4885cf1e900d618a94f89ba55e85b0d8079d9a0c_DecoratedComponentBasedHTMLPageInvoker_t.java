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
 
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.util.*;
 import org.infoglue.cms.util.dom.DOMBuilder;
 import org.infoglue.cms.exception.*;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.content.DigitalAssetVO;
 import org.infoglue.cms.io.FileHelper;
 import org.infoglue.deliver.applications.actions.InfoGlueComponent;
 import org.infoglue.deliver.applications.databeans.ComponentBinding;
 import org.infoglue.deliver.applications.databeans.ComponentProperty;
 import org.infoglue.deliver.applications.databeans.ComponentTask;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.applications.databeans.Slot;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.DecoratedComponentLogic;
 import org.infoglue.deliver.controllers.kernel.impl.simple.EditOnSiteBasicTemplateController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.Timer;
 import org.infoglue.deliver.util.VelocityTemplateProcessor;
 import org.w3c.dom.NodeList;
 
 import java.io.*;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.HashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.dom4j.Document;
 
 /**
  * @author Mattias Bogeblad
  *
  * This class delivers a normal html page by using the component-based method but also decorates it
  * so it can be used by the structure tool to manage the page components.
  */
 
 public class DecoratedComponentBasedHTMLPageInvoker extends ComponentBasedHTMLPageInvoker
 {
 	private String propertiesDivs 	= "";
 	private String tasksDivs 		= "";
 	
 	public DecoratedComponentBasedHTMLPageInvoker(HttpServletRequest request, HttpServletResponse response, TemplateController templateController, DeliveryContext deliveryContext)
 	{
 		super(request, response, templateController, deliveryContext);
 	}
 
 	/**
 	 * This is the method that will render the page. It uses the new component based structure. 
 	 */ 
 	
 	public void invokePage() throws SystemException, Exception
 	{
 		Timer timer = new Timer();
 		timer.setActive(false);
 		
 		String decoratePageTemplate = "";
 		
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(this.getDeliveryContext());
 		IntegrationDeliveryController integrationDeliveryController = IntegrationDeliveryController.getIntegrationDeliveryController(this.getDeliveryContext());
 		
 		timer.printElapsedTime("Initialized controllers");
 		
 		Integer repositoryId = nodeDeliveryController.getSiteNode(this.getDeliveryContext().getSiteNodeId()).getRepository().getId();
 		//CmsLogger.logInfo("this.getDeliveryContext().getContentId():" + this.getDeliveryContext().getContentId());
 		String componentXML = getPageComponentsString(this.getTemplateController(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
 		//CmsLogger.logInfo("componentXML:" + componentXML);
 		
 		timer.printElapsedTime("After getPageComponentsString");
 		
 		Timer decoratorTimer = new Timer();
 		decoratorTimer.setActive(false);
 		
 		if(componentXML == null || componentXML.length() == 0)
 		{
 			decoratePageTemplate = showInitialBindingDialog(this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
 		}
 		else
 		{
 			Document document = new DOMBuilder().getDocument(componentXML);
 			
 			decoratorTimer.printElapsedTime("After reading document");
 			
 			//decoratorTimer.printElapsedTime("After getting components");
 			
 			List pageComponents = getPageComponents(document.getRootElement(), "base", this.getTemplateController(), null);
 
 			//CmsLogger.logInfo("**********************pageComponents*****************************");
 		
 			//printComponentHierarchy(pageComponents, 0);
 			
 			//CmsLogger.logInfo("**********************pageComponents*****************************");
 		
 			
 			InfoGlueComponent baseComponent = null;
 			if(pageComponents.size() > 0)
 			{
 				baseComponent = (InfoGlueComponent)pageComponents.get(0);
 			}
 
 			decoratorTimer.printElapsedTime("After getting basecomponent");
 			
 			if(baseComponent == null)
 			{
 				decoratePageTemplate = showInitialBindingDialog(this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
 			}
 			else
 			{
 				ContentVO metaInfoContentVO = nodeDeliveryController.getBoundContent(this.getTemplateController().getPrincipal(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), true, "Meta information");
 				
 				decoratorTimer.printElapsedTime("After metaInfoContentVO");
 				
 				decoratePageTemplate = decorateComponent(baseComponent, this.getTemplateController(), repositoryId, this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId()/*, metaInfoContentVO.getId()*/);
 				
 				decoratorTimer.printElapsedTime("After decorateComponent");
 				
 				decoratePageTemplate = decorateTemplate(this.getTemplateController(), decoratePageTemplate, this.getDeliveryContext(), baseComponent);
 				
 				decoratorTimer.printElapsedTime("After decorateTemplate");
 			}
 		}
 		
 		timer.printElapsedTime("After main decoration");
 		
 		//TODO - TEST
 		decoratePageTemplate += propertiesDivs + tasksDivs;
 		
 		String componentEditorUrl = CmsPropertyHandler.getProperty("componentEditorUrl");
 		
 		//-- moved the creation of a default context into the baseclass
         // (robert)
         Map context = getDefaultContext();
 
 		context.put("componentEditorUrl", componentEditorUrl);
 		StringWriter cacheString = new StringWriter();
 		PrintWriter cachedStream = new PrintWriter(cacheString);
 		new VelocityTemplateProcessor().renderTemplate(context, cachedStream, decoratePageTemplate);
 		
 		this.setPageString(cacheString.toString());
 		
 		timer.printElapsedTime("End invokePage");
 		
 	}
 	
 	 /**
 	  * This method prints out the first template dialog.
 	  */
 
 	 private String showInitialBindingDialog(Integer siteNodeId, Integer languageId, Integer contentId)
 	 {
 		 String componentEditorUrl = CmsPropertyHandler.getProperty("componentEditorUrl");
 		 String url = "javascript:window.open('" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&specifyBaseTemplate=true', 'BaseTemplate', 'width=500,height=700,left=50,top=50,toolbar=no,status=no,scrollbars=yes,location=no,menubar=no,directories=no,resizable=yes');";
 		 return "<html><body style=\"font-family:verdana, sans-serif; font-size:10px;\">The page has no base component assigned yet. Click <a href=\"" + url + "\">here</a> to assign one</body></html>";
 	 }
 	 
 	/**
 	 * This method adds the neccessairy html to a template to make it right-clickable.
 	 */	
 
 	private String decorateTemplate(TemplateController templateController, String template, DeliveryContext deliveryContext, InfoGlueComponent component)
 	{
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		String decoratedTemplate = template;
 		
 		try
 		{
 		    String extraHeader 	= FileHelper.getFileAsString(new File(CmsPropertyHandler.getProperty("contextRootPath") + "preview/pageComponentEditorHeader.vm"));
 		    String extraBody 	= FileHelper.getFileAsString(new File(CmsPropertyHandler.getProperty("contextRootPath") + "preview/pageComponentEditorBody.vm"));
 			//String extraHeader = FileHelper.getStreamAsString(EditOnSiteBasicTemplateController.class.getResourceAsStream("/preview/pageComponentEditorHeader.vm"));
 		    //String extraBody   = FileHelper.getStreamAsString(EditOnSiteBasicTemplateController.class.getResourceAsStream("/preview/pageComponentEditorBody.vm"));
 			
 			//List tasks = getTasks();
 			//component.setTasks(tasks);
 			
 			//String tasks = templateController.getContentAttribute(component.getContentId(), "ComponentTasks", true);
 			
 			/*
 			Map context = new HashMap();
 			context.put("templateLogic", templateController);
 			StringWriter cacheString = new StringWriter();
 			PrintWriter cachedStream = new PrintWriter(cacheString);
 			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, extraBody);
 			extraBody = cacheString.toString();
 			*/
 			
 			//extraHeader.replaceAll()
 			
 			timer.printElapsedTime("Read files");
 			
 			StringBuffer modifiedTemplate = new StringBuffer(template);
 			
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
 
 			timer.printElapsedTime("Header handled");
 
 			//Adding stuff in the body	
 			int indexOfBodyStartTag = modifiedTemplate.indexOf("<body");
 			if(indexOfBodyStartTag == -1)
 				indexOfBodyStartTag = modifiedTemplate.indexOf("<BODY");
 			
 			if(indexOfBodyStartTag > -1)
 			{
 				String pageComponentStructureDiv = getPageComponentStructureDiv(templateController, deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), component);
 				timer.printElapsedTime("pageComponentStructureDiv");
 				String componentPaletteDiv = getComponentPaletteDiv(deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), templateController);
 				timer.printElapsedTime("componentPaletteDiv");
 				modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfBodyStartTag) + 1, extraBody + pageComponentStructureDiv + componentPaletteDiv);
 			}
 			else
 			{
 				CmsLogger.logWarning("The current template is not a valid document. It does not comply with the simplest standards such as having a correct body.");
 			}
 			
 			timer.printElapsedTime("Body handled");
 
 			decoratedTemplate = modifiedTemplate.toString();
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logWarning("An error occurred when deliver tried to decorate your template to enable onSiteEditing. Reason " + e.getMessage(), e);
 		}
 		
 		return decoratedTemplate;
 	}
 
     
 	private String decorateComponent(InfoGlueComponent component, TemplateController templateController, Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId/*, Integer metainfoContentId*/) throws Exception
 	{
 		String decoratedComponent = "";
 		
 		//CmsLogger.logInfo("decorateComponent.contentId:" + contentId);
 
 		//CmsLogger.logInfo("decorateComponent:" + component.getName());
 		
 		String componentEditorUrl = CmsPropertyHandler.getProperty("componentEditorUrl");
 
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		try
 		{
 			String componentString = getComponentString(templateController, component.getContentId()); 
 			//CmsLogger.logInfo("componentString:" + componentString);
 			
 			timer.printElapsedTime("1");
 			
 			templateController.setComponentLogic(new DecoratedComponentLogic(templateController, component));
 			Map context = super.getDefaultContext();
 			context.put("templateLogic", templateController);
 			StringWriter cacheString = new StringWriter();
 			PrintWriter cachedStream = new PrintWriter(cacheString);
 			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, componentString);
 			componentString = cacheString.toString();
 	
 			timer.printElapsedTime("2");
 			
 			int bodyIndex = componentString.indexOf("<body");
 			if(bodyIndex == -1)
 				bodyIndex = componentString.indexOf("<BODY");
 		
 			if(component.getParentComponent() == null && bodyIndex > -1)
 			{
 				//CmsLogger.logInfo("onContextMenu.contentId:" + contentId);
				String onContextMenu = " style=\"border: 1px solid blue; margin: 0px 0px 0px 0px; padding: 0px 0px 0px 0px;\" onload=\"javascript:setToolbarInitialPosition();\"";
 				StringBuffer sb = new StringBuffer(componentString);
 				sb.insert(bodyIndex + 5, onContextMenu);
 				componentString = sb.toString();
 				/*
 				String componentPropertiesString = getComponentPropertiesString(templateController, siteNodeId, languageId, component.getContentId()); 
 				componentString = componentString + getComponentPropertiesDiv(repositoryId, siteNodeId, languageId, contentId, component.getId(), componentPropertiesString);
 				*/
 				org.w3c.dom.Document componentPropertiesDocument = getComponentPropertiesDocument(templateController, siteNodeId, languageId, component.getContentId()); 
 				//CmsLogger.logInfo("getComponentPropertiesDiv.contentId:" + contentId);
 				this.propertiesDivs += getComponentPropertiesDiv(repositoryId, siteNodeId, languageId, contentId, component.getId(), componentPropertiesDocument);
 
 				org.w3c.dom.Document componentTasksDocument = getComponentTasksDocument(templateController, siteNodeId, languageId, component.getContentId()); 
 				//CmsLogger.logInfo("getComponentPropertiesDiv.contentId:" + contentId);
 				this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, component.getId(), componentTasksDocument);
 			}
 			//CmsLogger.logInfo("****************************************************************");
 	
 			timer.printElapsedTime("3");
 	
 			//End Test
 		
 			////CmsLogger.logInfo("Before:" + componentString);
 			int offset = 0;
 			int slotStartIndex = componentString.indexOf("<ig:slot", offset);
 			//CmsLogger.logInfo("slotStartIndex:" + slotStartIndex);
 			while(slotStartIndex > -1)
 			{
 				decoratedComponent += componentString.substring(offset, slotStartIndex);
 				int slotStopIndex = componentString.indexOf("</ig:slot>", slotStartIndex);
 				
 				String slot = componentString.substring(slotStartIndex, slotStopIndex + 10);
 				//CmsLogger.logInfo("Slot:" + slot);
 				String id = slot.substring(slot.indexOf("id") + 4, slot.indexOf("\"", slot.indexOf("id") + 4));
 				
 				String subComponentString = "";
 				
 				//TODO - test
 				if(component.getIsInherited())
 				    subComponentString += "<div id=\"" + component.getId() + "_" + id + "\" style=\"border: dotted 1px #0070FF; width: 100%;\");\">";
 				else
 				    subComponentString += "<div id=\"" + component.getId() + "_" + id + "\" style=\"border: dotted 1px #0070FF; width: 100%;\" onmouseup=\"javascript:assignComponent('" + siteNodeId + "', '" + languageId + "', '" + contentId + "', '" + component.getId() + "', '" + id + "', '" + false + "');\">";
 				    
 				List subComponents = getInheritedComponents(templateController, component, templateController.getSiteNodeId(), id);
 
 				timer.printElapsedTime("4");
 	
 				//CmsLogger.logInfo("subComponents for " + id + ":" + subComponents);
 				if(subComponents != null && subComponents.size() > 0)
 				{
 					//CmsLogger.logInfo("SUBCOMPONENTS:" + subComponents.size());
 					int index = 0;
 					Iterator subComponentsIterator = subComponents.iterator();
 					while(subComponentsIterator.hasNext())
 					{
 						InfoGlueComponent subComponent = (InfoGlueComponent)subComponentsIterator.next();
 						if(subComponent != null)
 						{
 							component.getComponents().put(subComponent.getName(), subComponent);
 							//CmsLogger.logInfo("Adding subcomponent:" + subComponent.getName() + " to " + component.getName());
 							//CmsLogger.logInfo("");
 							//CmsLogger.logInfo("Is it inherited: " + subComponent.getIsInherited());
 							//CmsLogger.logInfo("");
 							if(subComponent.getIsInherited())
 							{
 								timer.printElapsedTime("5a0");
 	
 								//CmsLogger.logInfo("Inherited..." + contentId);
 								String childComponentsString = decorateComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId/*, metainfoContentId*/);
 								//subComponentString += "<span id=\""+ id + "\" class=\"dragTarget\"><table width=\"100%\" heigh=\"100%\" style=\"border-style: solid; border-width: 1px; border-color: #333333;\"><tr><td class=\"inheritedslot\" onMouseOver=\"listRowOn(this);\" onMouseOut=\"listRowOff(this);\"><!--<div class=\"inheritedOverlay\">Inherited</div><div class=\"inherited\">-->" + subComponentString + "<!--</div>--></td></tr></table></span>";
 								subComponentString += "<span id=\""+ id + index + "Comp\" class=\"inheritedslot\" onMouseOver=\"listRowOn(this);\" onMouseOut=\"listRowOff(this);\">" + childComponentsString + "</span>";
 								
 								timer.printElapsedTime("5a1");
 								/*
 								String componentPropertiesString = getComponentPropertiesString(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								this.propertiesDivs += getComponentPropertiesDiv(repositoryId, siteNodeId, languageId, contentId, new Integer(siteNodeId.intValue()*100 + subComponent.getId().intValue()), componentPropertiesString);
 								//subComponentString = getComponentPropertiesDiv(repositoryId, siteNodeId, languageId, contentId, new Integer(siteNodeId.intValue()*100 + subComponent.getId().intValue()), componentPropertiesString) + subComponentString;
 								*/
 								org.w3c.dom.Document componentPropertiesDocument = getComponentPropertiesDocument(templateController, siteNodeId, languageId, component.getContentId()); 
 								this.propertiesDivs += getComponentPropertiesDiv(repositoryId, siteNodeId, languageId, contentId, new Integer(siteNodeId.intValue()*100 + subComponent.getId().intValue()), componentPropertiesDocument);
 								
 								timer.printElapsedTime("5a2");
 							
 								org.w3c.dom.Document componentTasksDocument = getComponentTasksDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, subComponent.getId(), componentTasksDocument);
 								
 							}
 							else
 							{
 								timer.printElapsedTime("5b0");
 	
 								//CmsLogger.logInfo("Not inherited..." + contentId);
 								String childComponentsString = decorateComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId/*, metainfoContentId*/);
 								//CmsLogger.logInfo("childComponentsString:" + childComponentsString);
 								
 								//TODO - TEST
 								subComponentString += "<span id=\""+ id + index + "_" + subComponent.getId() + "Comp\" class=\"dragTarget\" onMouseOver=\"listRowOn(this);\" onMouseOut=\"listRowOff(this);\">" + childComponentsString + "<script type=\"text/javascript\">initializeComponentEventHandler('" + id + index + "_" + subComponent.getId() + "Comp', '" + subComponent.getId() + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&parentComponentId=" + component.getId() + "&slotId=" + id + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponent.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + subComponent.getId() + "&slotId=" + id + "');</script></span>";
 								timer.printElapsedTime("5b1");
 								
 								/*
 								String componentPropertiesString = getComponentPropertiesString(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								this.propertiesDivs += getComponentPropertiesDiv(repositoryId, siteNodeId, languageId, contentId, subComponent.getId(), componentPropertiesString);
 								//subComponentString = getComponentPropertiesDiv(repositoryId, siteNodeId, languageId, contentId, subComponent.getId(), componentPropertiesString) + subComponentString;
 								*/
 								
 								org.w3c.dom.Document componentPropertiesDocument = getComponentPropertiesDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								//CmsLogger.logInfo("subComponent.getId():" + subComponent.getId());
 								this.propertiesDivs += getComponentPropertiesDiv(repositoryId, siteNodeId, languageId, contentId, subComponent.getId(), componentPropertiesDocument);
 								//CmsLogger.logInfo("................");
 	
 								timer.printElapsedTime("5b2");							
 
 								org.w3c.dom.Document componentTasksDocument = getComponentTasksDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, subComponent.getId(), componentTasksDocument);
 							}
 						}
 						index++;
 					}
 					//CmsLogger.logInfo("-------------------------------------------");
 				}
 				else
 				{
 					subComponentString += "Click to add component";
 				}
 				
 				if(!component.getIsInherited())
 				    subComponentString += "<script type=\"text/javascript\">initializeSlotEventHandler('" + component.getId() + "_" + id + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&parentComponentId=" + component.getId() + "&slotId=" + id + "', '');</script></div>";
 				
 				decoratedComponent += subComponentString;
 							
 				offset = slotStopIndex + 10;
 				slotStartIndex = componentString.indexOf("<ig:slot", offset);
 			}
 			
 			//CmsLogger.logInfo("offset:" + offset);
 			decoratedComponent += componentString.substring(offset);
 		}
 		catch(Exception e)
 		{		
 			CmsLogger.logWarning("An component with either an empty template or with no template in the sitelanguages was found:" + e.getMessage(), e);	
 		}
 		
 		return decoratedComponent;
 	}
 
 
 	/**
 	 * This method creates a div for the components properties.
 	 */
 	
 	private String getComponentPropertiesDiv(Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId, Integer componentId, org.w3c.dom.Document document/*String componentPropertiesString*/) throws Exception
 	{		
 		//CmsLogger.logInfo("***************************************************************");
 		//CmsLogger.logInfo("componentId:" + componentId);
 
 		StringBuffer sb = new StringBuffer();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		String componentEditorUrl = CmsPropertyHandler.getProperty("componentEditorUrl");
 				
 		sb.append("<div id=\"component" + componentId + "Properties\" class=\"componentProperties\" style=\"right:5px; top:5px; visibility:hidden;\">");
 		sb.append("	<div id=\"component" + componentId + "PropertiesHandle\" class=\"componentPropertiesHandle\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"100%\"><tr><td align=\"left\" class=\"smallwhitelabel\">Properties</td><td align=\"right\"><a href=\"javascript:hideDiv('component" + componentId + "Properties');\" class=\"white\">close</a></td></tr></table></div>");
 		sb.append("	<div id=\"component" + componentId + "PropertiesBody\" class=\"componentPropertiesBody\">");
 		
 		sb.append("	<form id=\"component" + componentId + "PropertiesForm\" name=\"component" + componentId + "PropertiesForm\" action=\"" + componentEditorUrl + "ViewSiteNodePageComponents!updateComponentProperties.action\" method=\"POST\">");
 		sb.append("		<table border=\"0\" cellpadding=\"4\" cellspacing=\"0\">");
 
 		sb.append("		<tr>");
 		sb.append("			<td class=\"propertylabel\">Choose language</td>");  //$ui.getString("tool.contenttool.languageVersionsLabel")
 		sb.append("			<td class=\"propertyvalue\">");
 	
 		sb.append("			");
 		sb.append("			<select class=\"mediumdrop\" name=\"languageId\" onChange=\"javascript:changeLanguage(" + siteNodeId + ", this, " + contentId + ");\">");
 		
 		timer.printElapsedTime("getComponentPropertiesDiv: 1");
 		
 		List languages = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguages(siteNodeId);
 		timer.printElapsedTime("getComponentPropertiesDiv: 2");
 
 		Iterator languageIterator = languages.iterator();
 		int index = 0;
 		int languageIndex = index;
 		while(languageIterator.hasNext())
 		{
 			LanguageVO languageVO = (LanguageVO)languageIterator.next();
 			if(languageVO.getLanguageId().intValue() == languageId.intValue())
 			{
 				sb.append("					<option value=\"" + languageVO.getLanguageId() + "\" selected>" + languageVO.getName() + "</option>");
 				sb.append("					<script type=\"text/javascript\">");
 				sb.append("					</script>");
 				languageIndex = index;
 			}
 			else
 			{
 				sb.append("					<option value=\"" + languageVO.getLanguageId() + "\">" + languageVO.getName() + "</option>");
 			}
 			index++;
 			timer.printElapsedTime("getComponentPropertiesDiv: 3");
 		}
 		sb.append("			</select>");
 		sb.append("			<!--");
 		sb.append("				var originalIndex = " + languageIndex + ";");
 		sb.append("			-->");
 
 		sb.append("			</td>");
 		sb.append("			<td>&nbsp;</td>");
 		sb.append("		</tr>");
 		
 		//CmsLogger.logInfo("componentPropertiesString:" + componentPropertiesString);
 		Collection componentProperties = getComponentProperties(componentId, document /*componentPropertiesString*/);
 		timer.printElapsedTime("getComponentPropertiesDiv: 4");
 
 		int propertyIndex = 0;
 		//CmsLogger.logInfo("componentProperties:" + componentProperties.size());
 		Iterator componentPropertiesIterator = componentProperties.iterator();
 		while(componentPropertiesIterator.hasNext())
 		{
 			ComponentProperty componentProperty = (ComponentProperty)componentPropertiesIterator.next();
 			//CmsLogger.logInfo("componentProperty type:" + componentProperty.getType());
 			if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.BINDING))
 			{
 				String assignUrl = "";
 				String createUrl = "";
 				 
 				if(componentProperty.getVisualizingAction() != null && !componentProperty.getVisualizingAction().equals(""))
 				{
 					assignUrl = componentEditorUrl + componentProperty.getVisualizingAction() + "?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName();
 				}
 				else
 				{	
 					if(componentProperty.getEntityClass().equalsIgnoreCase("Content"))
 					{
 						if(componentProperty.getIsMultipleBinding())
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showContentTreeForMultipleBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName();
 						else
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showContentTree.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName();
 					}
 					else if(componentProperty.getEntityClass().equalsIgnoreCase("SiteNode"))
 					{
 						if(componentProperty.getIsMultipleBinding())
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showStructureTreeForMultipleBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName();
 						else
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showStructureTree.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName();
 					}
 					else if(componentProperty.getEntityClass().equalsIgnoreCase("Category"))
 					{
 						if(componentProperty.getIsMultipleBinding())
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showCategoryTreeForMultipleBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName();
 						else
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showCategoryTree.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName();
 					}
 				}
 					
 				if(componentProperty.getCreateAction() != null && !componentProperty.getCreateAction().equals(""))
 				{
 					createUrl = componentEditorUrl + componentProperty.getCreateAction() + "?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName();
 				}
 				else
 				{	
 					if(componentProperty.getVisualizingAction() != null && !componentProperty.getVisualizingAction().equals(""))
 					{
 						createUrl = assignUrl;
 					}
 					else if(componentProperty.getEntityClass().equalsIgnoreCase("Content"))
 					{
 						String returnAddress = URLEncoder.encode("ViewSiteNodePageComponents!addComponentPropertyBinding.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=-1&entity=Content&entityId=#entityId&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&path=TEMPPPP", "UTF-8");
 						
 						if(componentProperty.getIsMultipleBinding())
 							createUrl = componentEditorUrl + "CreateContentWizardFinish.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&refreshAddress=" + returnAddress;
 						else
 							createUrl = componentEditorUrl + "CreateContentWizardFinish.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&refreshAddress=" + returnAddress;
 					}
 					else if(componentProperty.getEntityClass().equalsIgnoreCase("SiteNode"))
 					{
 						if(componentProperty.getIsMultipleBinding())
 							createUrl = componentEditorUrl + "CreateSiteNodeWizard!input.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName();
 						else
 							createUrl = componentEditorUrl + "CreateSiteNodeWizard!input.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName();
 					}
 				}
 					
 				sb.append("		<tr>");
 				sb.append("			<td class=\"propertylabel\" valign=\"top\">" + componentProperty.getName() + "</td>");
 				sb.append("			<td class=\"propertyvalue\"><a href=\"javascript:window.open('" + assignUrl + "','Assign','toolbar=no,status=yes,scrollbars=yes,location=no,menubar=no,directories=no,resizable=no,width=300,height=600,left=5,top=5');\">" + componentProperty.getValue() + "</a></td>");
 				//sb.append("			<td class=\"propertyvalue\"><a href=\"" + assignUrl + "\">" + componentProperty.getValue() + "</a></td>");
 				
 				if(componentProperty.getValue().equalsIgnoreCase("Undefined"))
 					//sb.append("			<td><a href=\"" + createUrl + "\"><img src=\"" + componentEditorUrl + "/images/createContent.gif\" border=\"0\" alt=\"Create new content to show\"></a></td>");
 					sb.append("			<td><!--<a href=\"" + createUrl + "\"><img src=\"" + componentEditorUrl + "/images/createContent.gif\" border=\"0\" alt=\"Create new content to show\"></a>--></td>");
 				else
 					sb.append("			<td><a href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\"></a></td>");
 		
 				sb.append("		</tr>");
 			}
 			else if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.TEXTFIELD))
 			{
 				sb.append("		<tr>");
 				sb.append("			<td class=\"propertylabel\" valign=\"top\">" + componentProperty.getName() + "<input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "\"></td>");
 				sb.append("			<td class=\"propertyvalue\"><input type=\"text\" name=\"" + componentProperty.getName() + "\" value=\"" + componentProperty.getValue() + "\"></td>");
 				sb.append("			<td><a href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\"></a></td>");
 				sb.append("			<!--<td>&nbsp;</td>-->");
 				sb.append("		</tr>");
 				
 				propertyIndex++;
 			}
 		}
 		
 		timer.printElapsedTime("getComponentPropertiesDiv: 5");
 
 		sb.append("		<tr>");
 		sb.append("			<td colspan=\"3\"><img src=\"images/trans.gif\" height=\"5\" width=\"1\"></td>");
 		sb.append("		</tr>");
 		sb.append("		<tr>");
 		sb.append("			<td colspan=\"3\">");
 		sb.append("				<a href=\"javascript:submitForm('component" + componentId + "PropertiesForm');\"><img src=\"" + componentEditorUrl + "" + this.getDeliveryContext().getWebworkAbstractAction().getLocalizedString(this.getDeliveryContext().getSession().getLocale(), "images.contenttool.buttons.save") + "\" width=\"50\" height=\"25\" border=\"0\"></a>");
 		sb.append("				<a href=\"javascript:hideDiv('component" + componentId + "Properties');\"><img src=\"" + componentEditorUrl + "" + this.getDeliveryContext().getWebworkAbstractAction().getLocalizedString(this.getDeliveryContext().getSession().getLocale(), "images.contenttool.buttons.close") + "\" width=\"50\" height=\"25\" border=\"0\"></a>");
 		sb.append("			</td>");
 		sb.append("		</tr>");
 		sb.append("		</table>");
 		sb.append("		<input type=\"hidden\" name=\"repositoryId\" value=\"" + repositoryId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"siteNodeId\" value=\"" + siteNodeId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"languageId\" value=\"" + languageId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"contentId\" value=\"" + contentId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"componentId\" value=\"" + componentId + "\">");
 		sb.append("		</form>");
 		sb.append("	</div>");
 		sb.append("	</div>");
 		
 		sb.append("	<script type=\"text/javascript\">");
 		sb.append("		var theHandle = document.getElementById(\"component" + componentId + "PropertiesHandle\");");
 		sb.append("		var theRoot   = document.getElementById(\"component" + componentId + "Properties\");");
 		
 		sb.append("		componentId = \"" + componentId + "\";");
 		sb.append("		activatedComponentId = QueryString(\"activatedComponentId\");");
 		sb.append("		if(activatedComponentId && activatedComponentId == componentId)"); 
 		sb.append("			showDiv(\"component\" + componentId + \"Properties\");"); 
 
 		sb.append("		Drag.init(theHandle, theRoot);");
 		sb.append("     theRoot.style.left = 160;");
 		sb.append("     theRoot.style.top = 150;");
 		sb.append("	</script>");
 		
 		return sb.toString();
 	}
 
 	
 	/**
 	 * This method creates a div for the components properties.
 	 */
 	
 	private String getComponentTasksDiv(Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId, Integer componentId, org.w3c.dom.Document document) throws Exception
 	{		
 		StringBuffer sb = new StringBuffer();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		String componentEditorUrl = CmsPropertyHandler.getProperty("componentEditorUrl");
 		
 		sb.append("<div id=\"component" + componentId + "Menu\" class=\"skin0\">");
 		
 		Collection componentTasks = getComponentTasks(componentId, document);
 		timer.printElapsedTime("getComponentTasksDiv: 4");
 
 		int taskIndex = 0;
 		Iterator componentTasksIterator = componentTasks.iterator();
 		while(componentTasksIterator.hasNext())
 		{
 		    ComponentTask componentTask = (ComponentTask)componentTasksIterator.next();
 		    
 		    String view = componentTask.getView();
 		    
 		    view = view.replaceAll("\\$componentEditorUrl", componentEditorUrl);
 		    view = view.replaceAll("\\$repositoryId", repositoryId.toString());
 		    view = view.replaceAll("\\$siteNodeId", siteNodeId.toString());
 		    view = view.replaceAll("\\$languageId", languageId.toString());
 		    view = view.replaceAll("\\$componentId", componentId.toString());
 		    sb.append("<div class=\"menuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"executeTask('" + view + "');\">" + componentTask.getName() + "</div>");
 		}
 		
 		sb.append("<div class=\"menuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"edit();\">Edit</div>");
 		sb.append("<div class=\"menuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"insertComponent();\">Add&nbsp;component</div>");
 		sb.append("<div class=\"menuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"deleteComponent();\">Delete&nbsp;component</div>");
 		sb.append("<hr>");
 		sb.append("<div class=\"menuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"javascript:showComponent();\">Properties</div>");
 		sb.append("<hr>");
 		sb.append("<div class=\"menuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"javascript:viewSource();\">View&nbsp;Source</div>");
 		sb.append("</div>");
 				
 		return sb.toString();
 	}
 
 	/**
 	 * This method creates a div for the components properties.
 	 */
 	
 	private String getPageComponentStructureDiv(TemplateController templateController, Integer siteNodeId, Integer languageId, InfoGlueComponent component) throws Exception
 	{		
 		StringBuffer sb = new StringBuffer();
 		
 		String componentEditorUrl = CmsPropertyHandler.getProperty("componentEditorUrl");
 
		sb.append("<div id=\"pageComponents\" style=\"right:5px; top:5px; visibility:hidden; display: none;\">");
 
		sb.append("	<div id=\"dragCorner\" style=\"position: absolute; width: 16px; height: 16px; background-color: white; bottom: 0px; right: 0px;\"><a href=\"javascript:expandWindow('pageComponents');\"><img src=\"images/enlarge.gif\" border=\"0\" width=\"16\" height=\"16\"></a></div>");
 			
 		sb.append("		<div id=\"pageComponentsHandle\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"100%\"><tr><td align=\"left\" class=\"smallwhitelabel\">Page components</td><td align=\"right\"><a href=\"javascript:hideDiv('pageComponents');\" class=\"white\">close</a></td></tr></table></div>");
 		sb.append("		<div id=\"pageComponentsBody\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
 		
 		sb.append("		<tr>");
 		sb.append("			<td colspan=\"20\">");
 		sb.append("<img src=\"images/tcross.png\" width=\"19\" height=\"16\"><span id=\"" + component.getId() + "\" class=\"label\"><img src=\"images/slotIcon.gif\" width=\"16\" height=\"16\"><img src=\"images/trans.gif\" width=\"5\" height=\"1\">" + component.getName() + "</span><script type=\"text/javascript\">initializeSlotEventHandler('" +  component.getId() + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&parentComponentId=" + component.getId() + "&slotId=base', '');</script></td>");
 		//sb.append("<img src=\"images/tcross.png\" width=\"19\" height=\"16\"><span onclick=\"javascript:showDiv('component" + component.getId() + "Properties');\" class=\"label\"><img src=\"images/slotIcon.gif\" width=\"16\" height=\"16\"><img src=\"images/trans.gif\" width=\"5\" height=\"1\">" + component.getName() + "</span></td>");
 		sb.append("		</tr>");
 		
 		renderComponentTree(templateController, sb, component, 0, 0, 1);
 
 		sb.append("		<tr>");
 		for(int i=0; i<20; i++)
 		{
 			sb.append("<td width=\"19\"><img src=\"images/trans.gif\" width=\"19\" height=\"1\"></td>");
 		}
 		sb.append("		</tr>");
 		sb.append("		</table>");
 		sb.append("		</div>");
 		sb.append("	</div>");
 		
 		sb.append("	<script type=\"text/javascript\">");
 		sb.append("		var theHandle = document.getElementById(\"pageComponentsHandle\");");
 		sb.append("		var theRoot   = document.getElementById(\"pageComponents\");");
 		sb.append("		Drag.init(theHandle, theRoot);");
 		sb.append("     theRoot.style.left = 160;");
 		sb.append("     theRoot.style.top = 150;");
 		sb.append("	</script>");
 
 		return sb.toString();
 	}
 
 	/**
 	 * This method renders the component tree visually
 	 */
 	
 	private void renderComponentTree(TemplateController templateController, StringBuffer sb, InfoGlueComponent component, int level, int position, int maxPosition) throws Exception
 	{
 		String componentEditorUrl = CmsPropertyHandler.getProperty("componentEditorUrl");
 
 		ContentVO componentContentVO = templateController.getContent(component.getContentId());
 		
 		int colspan = 20 - level;
 		
 		sb.append("		<tr>");
 		sb.append("			<td><img src=\"images/trans.gif\" width=\"19\" height=\"16\"></td>");
 		for(int i=0; i<level; i++)
 		{
 			sb.append("<td width=\"19\"><img src=\"images/vline.png\" width=\"19\" height=\"16\"></td>");
 		}
 		
 		sb.append("<td width=\"19\"><img src=\"images/tcross.png\" width=\"19\" height=\"16\"></td><td><img src=\"images/componentIcon.gif\" width=\"16\" height=\"16\"></td><td colspan=\"" + (colspan - 2) + "\"><span id=\"" + component.getId() + "\" onclick=\"javascript:showDiv('component" + component.getId() + "Properties');\" class=\"clickableLabel\">" + componentContentVO.getName() + "</span><script type=\"text/javascript\">initializeComponentInTreeEventHandler('" + component.getId() + "', '" + component.getId() + "', '', '" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&slotId=" + component.getId() + "');</script>");
 		String upUrl = componentEditorUrl + "ViewSiteNodePageComponents!moveComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&direction=0";
 		String downUrl = componentEditorUrl + "ViewSiteNodePageComponents!moveComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&direction=1";
 		
 		if(position > 0)
 		    sb.append("<a href=\"" + upUrl + "\"><img src=\"images/upArrow.gif\" border=\"0\" width=\"11\" width=\"10\"></a>");
 		if(maxPosition > position)
 		    sb.append("<a href=\"" + downUrl + "\"><img src=\"images/downArrow.gif\" border=\"0\" width=\"11\" width=\"10\"></a>");
 		
 		sb.append("</td>");
 		
 		sb.append("		</tr>");
 		
 		//Properties
 		sb.append("		<tr>");
 		sb.append("			<td><img src=\"images/trans.gif\" width=\"19\" height=\"1\"></td><td><img src=\"images/vline.png\" width=\"19\" height=\"16\"></td>");
 		for(int i=0; i<level; i++)
 		{
 			sb.append("<td><img src=\"images/vline.png\" width=\"19\" height=\"16\"></td>");
 		}
		sb.append("<td><img src=\"images/tcross.png\" width=\"19\" height=\"16\"></td><td width=\"19\"><img src=\"images/propertiesIcon.gif\" width=\"16\" height=\"16\" border=\"0\"></td><td colspan=\"" + (colspan - 3) + "\"><span onclick=\"javascript:showComponentProperties('component" + component.getId() + "Properties');\" class=\"label\">Properties</span></td>");
 		sb.append("		</tr>");
 		
 		sb.append("		<tr>");
 		sb.append("			<td width=\"19\"><img src=\"images/trans.gif\" width=\"19\" height=\"1\"></td><td width=\"19\"><img src=\"images/vline.png\" width=\"19\" height=\"16\"></td>");
 		for(int i=0; i<level; i++)
 		{
 			sb.append("<td><img src=\"images/vline.png\" width=\"19\" height=\"16\"></td>");
 		}
 		sb.append("<td width=\"19\"><img src=\"images/endline.png\" width=\"19\" height=\"16\"></td><td width=\"19\"><img src=\"images/containerIcon.gif\" width=\"16\" height=\"16\"></td><td colspan=\"" + (colspan - 4) + "\"><span class=\"label\">Slots</span></td>");
 		sb.append("</tr>");
 
 		Iterator slotIterator = component.getSlotList().iterator();
 		//CmsLogger.logInfo("Number of slots on " + component.getName() + ":" + component.getSlotList().size());
 		while(slotIterator.hasNext())
 		{
 			Slot slot = (Slot)slotIterator.next();
 	
 			sb.append("		<tr>");
 			sb.append("			<td width=\"19\"><img src=\"images/trans.gif\" width=\"19\" height=\"16\"></td><td width=\"19\"><img src=\"images/vline.png\" width=\"19\" height=\"16\"></td><td><img src=\"images/vline.png\" width=\"19\" height=\"16\"></td>");
 			for(int i=0; i<level; i++)
 			{
 				sb.append("<td width=\"19\"><img src=\"images/vline.png\" width=\"19\" height=\"16\"></td>");
 			}
 			if(slot.getComponents().size() > 0)
 				sb.append("<td width=\"19\"><img src=\"images/tcross.png\" width=\"19\" height=\"16\"></td><td width=\"19\"><img src=\"images/slotIcon.gif\" width=\"16\" height=\"16\"></td>");
 			else
 				sb.append("<td width=\"19\"><img src=\"images/endline.png\" width=\"19\" height=\"16\"></td><td width=\"19\"><img src=\"images/slotIcon.gif\" width=\"16\" height=\"16\"></td>");
 
 //			sb.append("<td colspan=\"" + (colspan - 4) + "\"><span id=\"" + component.getId() + "\" class=\"label\">" + slot.getId() + "</span><script type=\"text/javascript\">initializeSlotEventHandler('" +  component.getId() + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&componentId=" + component.getId() + "&contentId=" + -1 + "&slotId=" + slot.getId() + "', '');</script></td>");
 			sb.append("<td colspan=\"" + (colspan - 4) + "\"><span id=\"" + slot.getId() + "ClickableDiv\" class=\"label\">" + slot.getId() + "</span><script type=\"text/javascript\">initializeSlotEventHandler('" + slot.getId() + "ClickableDiv', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&parentComponentId=" + component.getId() + "&slotId=" + slot.getId() + "', '');</script></td>");
 			
 			sb.append("		</tr>");
 
 			List slotComponents = slot.getComponents();
 			//CmsLogger.logInfo("Number of components in slot " + slot.getId() + ":" + slotComponents.size());
 
 			if(slotComponents != null)
 			{
 				Iterator slotComponentIterator = slotComponents.iterator();
 				int newPosition = 0;
 				while(slotComponentIterator.hasNext())
 				{
 					InfoGlueComponent slotComponent = (InfoGlueComponent)slotComponentIterator.next();
 					ContentVO componentContent = templateController.getContent(slotComponent.getContentId()); 
 					
 					String imageUrl = "images/componentIcon.gif";
 					//String imageUrlTemp = getDigitalAssetUrl(componentContent.getId(), "thumbnail");
 					//if(imageUrlTemp != null && imageUrlTemp.length() > 0)
 					//	imageUrl = imageUrlTemp;
 		
 					renderComponentTree(templateController, sb, slotComponent, level + 3, newPosition, slotComponents.size() - 1);
 
 					newPosition++;
 				}	
 			}
 			
 					
 		}
 
 	}
 
 
 
 	/**
 	 * This method creates a div for the components properties.
 	 */
 	
 	private String getOldComponentPaletteDiv(Integer siteNodeId, Integer languageId) throws Exception
 	{		
 		StringBuffer sb = new StringBuffer();
 		
 		String componentEditorUrl 		= CmsPropertyHandler.getProperty("componentEditorUrl");
 		String componentRendererUrl 	= CmsPropertyHandler.getProperty("componentRendererUrl");
 		String componentRendererAction 	= CmsPropertyHandler.getProperty("componentRendererAction");
 
 		sb.append("<div id=\"buffer\" style=\"top: 0px; left: 0px; z-index:1000;\"><img src=\"images/componentDraggedIcon.gif\"></div>");
 		
 		sb.append("<div id=\"palette\" style=\"right:5px; top:200px;\">");
 		sb.append("		<div id=\"paletteHandle\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"100%\"><tr><td align=\"left\" class=\"smallwhitelabel\">Component palette</td><td align=\"right\"><a href=\"javascript:hideDiv('palette');\" class=\"white\">close</a></td></tr></table></div>");
 		sb.append("		<div id=\"paletteHeader\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"100%\">");
 		sb.append("		<tr class=\"darkyellow\">");
 		sb.append("			<td colspan=\"2\" class=\"smalllabel\">Drag components to the slots available</td>");
 		sb.append("		</tr>");
 		sb.append("		<tr>");
 		sb.append("			<td colspan=\"2\" height=\"1\"><img src=\"images/trans.gif\" width=\"1\" height=\"5\"></td>");
 		sb.append("		</tr>");
 		sb.append("		<tr>");
 		sb.append("			<td height=\"1\" width=\"20\"><span class=\"boldsmalllabel\">Icon</span></td>");
 		sb.append("			<td height=\"1\"><span class=\"boldsmalllabel\">Component Name</span></td>");
 		sb.append("		</tr></table>");
 		sb.append("		</div>");
 		
 		sb.append("		<div id=\"paletteBody\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"100%\">");
 		
 		boolean isOdd = true;
 		String cssClass = "";
 		String imageUrl = "images/componentIcon.gif";
 		List components = getComponentContents();
 		Iterator componentIterator = components.iterator();
 		while(componentIterator.hasNext())
 		{
 			if(isOdd)
 			{
 				cssClass = "class=\"lightyellow\"";
 				isOdd = false;
 			}
 			else
 			{
 				cssClass = "class=\"white\"";
 				isOdd = true;
 			}
 			ContentVO componentContentVO = (ContentVO)componentIterator.next();
 			
 			//String imageUrlTemp = getDigitalAssetUrl(componentContentVO.getId(), "thumbnail");
 			//if(imageUrlTemp != null && imageUrlTemp.length() > 0)
 			//	imageUrl = imageUrlTemp;
 	
 			sb.append("	<tr " + cssClass + ">");
 			sb.append("		<td align=\"center\" width=\"20\"><img src=\"" + imageUrl + "\" width=\"16\" height=\"16\" border=\"0\">&nbsp;</td>");
 			sb.append("		<td valign=\"middle\"><span onMouseDown=\"grabIt(event);\" id=\""+ componentContentVO.getId() + "\" class=\"draggableItem\">"+ componentContentVO.getName() + "</span></td>");
 			sb.append("	</tr>");
 			
 			imageUrl = "images/componentIcon.gif";
 		}
 		
 		sb.append("		</table></div>");
 		
 		sb.append("	</div>");
 		
 		
 		sb.append("	<script type=\"text/javascript\">");
 		sb.append("		var theHandle = document.getElementById(\"paletteHandle\");");
 		sb.append("		var theRoot   = document.getElementById(\"palette\");");
 		//sb.append("		Drag.init(theHandle, theRoot);");
 		sb.append("		Drag.init(theHandle, theRoot, 0, 0, 0, 1000);");
 		sb.append("     theRoot.style.left = 450;");
 		sb.append("     theRoot.style.top = 150;");
 		sb.append("	</script>");
 		
 		return sb.toString();
 	}
 
 
 	/**
 	 * This method creates the tabpanel for the component-palette.
 	 */
 	private static String componentPaletteDiv = null;
 	
 	private String getComponentPaletteDiv(Integer siteNodeId, Integer languageId, TemplateController templateController) throws Exception
 	{		
 		if(componentPaletteDiv != null && (templateController.getRequestParameter("refresh") == null || !templateController.getRequestParameter("refresh").equalsIgnoreCase("true")))
 		{
 			return componentPaletteDiv;
 		}
 		
 		StringBuffer sb = new StringBuffer();
 	
 		ContentVO contentVO = templateController.getBoundContent(BasicTemplateController.META_INFO_BINDING_NAME);
 		
 		String componentEditorUrl 		= CmsPropertyHandler.getProperty("componentEditorUrl");
 		String componentRendererUrl 	= CmsPropertyHandler.getProperty("componentRendererUrl");
 		String componentRendererAction 	= CmsPropertyHandler.getProperty("componentRendererAction");
 		
 		sb.append("<div id=\"buffer\" style=\"top: 0px; left: 0px; z-index:200;\"><img src=\"images/componentDraggedIcon.gif\"></div>");
 		
 		Map componentGroups = getComponentGroups(getComponentContents(), templateController);
 
 		sb.append("<div id=\"paletteDiv\" style=\"z-Index:1000; border: solid 1px blue; position:absolute; background:#999999; height:80px; width:100%; left:0px; top:0px;\">");
 		 
 		sb.append("<div id=\"paletteHandle\" style=\"width:100%; height:10px; background:navy;\" cellpadding=\"0\" cellspacing=\"0\">");
 		sb.append("	<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"100%\"><tr><td align=\"left\" class=\"smallwhitelabel\">Component palette</td><td align=\"right\"><a href=\"javascript:hideDiv('paletteDiv');\" class=\"white\">close</a></td></tr></table>");
 		sb.append("</div>");
 				
 		sb.append("<div id=\"componentPalette\" style=\"background:#999999; height:20px; width:100%; left:0px; top:0px;\">");
 		sb.append("<table style=\"width:100%\" cellpadding=\"0\" cellspacing=\"0\">");
 		sb.append(" <tr>");
 		
 		Iterator groupIterator = componentGroups.keySet().iterator();
 		int index = 0;
 		String groupName = "";
 		String initialGroupName = "";
 		while(groupIterator.hasNext())
 		{
 			groupName = (String)groupIterator.next();
 
 			if(index == 0)
 			{	
 				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"thistab\" onclick=\"javascript:changeTab('" + groupName + "');\" height=\"20\"><nobr>" + groupName + "</nobr></td>");
 				initialGroupName = groupName;
 			}
 			else if(!groupIterator.hasNext())
 				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"tab\" style=\"border-right: solid thin black\" onclick=\"javascript:changeTab('" + groupName + "');\"><nobr>" + groupName + "</nobr></td>");
 			else
 				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"tab\" onclick=\"javascript:changeTab('" + groupName + "');\"><nobr>" + groupName + "</nobr></td>");
 
 			index++;
 		}
 		sb.append("  <td width=\"90%\" style=\"border-right: solid thin gray; border-bottom: solid thin white\" align=\"right\">&nbsp;<a href=\"javascript:refreshComponents(document.location.href);\" class=\"white\"><img src=\"images/refresh.gif\" alt=\"Refresh palette\" border=\"0\"></a>&nbsp;<a href=\"javascript:moveDivDown('paletteDiv');\" class=\"white\"><img src=\"images/arrowDown.gif\" alt=\"Move down\" border=\"0\"></a>&nbsp;<a href=\"javascript:moveDivUp('paletteDiv');\" class=\"white\"><img src=\"images/arrowUp.gif\" alt=\"Move up\" border=\"0\"></a>&nbsp;<a href=\"javascript:toggleDiv('pageComponents');\" class=\"white\"><img src=\"images/pageStructure.gif\" alt=\"Toggle page structure\" border=\"0\"></a>&nbsp;<a href=\"javascript:saveComponentStructure('" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + contentVO.getId() + "');\" class=\"white\"><img src=\"images/saveComponentStructure.gif\" alt=\"Save the page as a template page\" border=\"0\"></a>&nbsp;<a href=\"javascript:window.open(document.location.href, 'PageComponents', '');\"><img src=\"images/fullscreen.gif\" alt=\"Pop up in a large window\" border=\"0\"></a>&nbsp;</td>");
 		sb.append(" </tr>");
 		sb.append("</table>");
 		sb.append("</div>");
 		
 		sb.append("<script type=\"text/javascript\">");
 		sb.append("var currentGroup = \"" + initialGroupName + "\";");
 		sb.append("</script>");
 				
 		String openGroupName = "";
 
 		
 		groupIterator = componentGroups.keySet().iterator();
 		index = 0;
 		while(groupIterator.hasNext())
 		{
 			groupName = (String)groupIterator.next();
 
 			if(index == 0)
 			{
 				sb.append("<div id=\"" + groupName + "ComponentsBg\" style=\"border-bottom: 1px solid gray; border-left: 1px solid white; border-right: 0px solid red; background:#CCCCCC; zIndex:3; width:100%; height:24px; position:absolute; top:50px; left:10px;\">");
 				openGroupName = groupName;
 			}
 			else
 				sb.append("<div id=\"" + groupName + "ComponentsBg\" style=\"border-bottom: 1px solid gray; border-left: 1px solid white; border-right: 0px solid red; background:#CCCCCC; zIndex:2; width:100%; height:24px; position:absolute; top:50px; left:10px;\">");	
 			
 			sb.append("<div id=\"" + groupName + "Components\" style=\"visibility:inherit; position:absolute; top:1px; left:5px; height:50px; border-left: 1px solid white;\">");
 			sb.append("	<table style=\"width:100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
 			sb.append("	<tr>");
 			//sb.append("	<td width=\"100%\"><nobr>");
 			
 			String imageUrl = "images/componentIcon.gif";
 			List components = (List)componentGroups.get(groupName); //getComponentContents();
 			Iterator componentIterator = components.iterator();
 			int componentIndex = 0;
 			while(componentIterator.hasNext())
 			{
 				ContentVO componentContentVO = (ContentVO)componentIterator.next();
 	
 				//String imageUrlTemp = getDigitalAssetUrl(componentContentVO.getId(), "thumbnail");
 				//if(imageUrlTemp != null && imageUrlTemp.length() > 0)
 				//	imageUrl = imageUrlTemp;
 				sb.append("	<td>");
 				sb.append("		<div id=\"" + componentIndex + "\" style=\"display: block;\"><nobr><img src=\"" + imageUrl + "\" width=\"16\" height=\"16\" border=\"0\">");
 				sb.append("		<span onMouseDown=\"grabIt(event);\" onmouseover=\"showDetails('" + componentContentVO.getName() + "');\" id=\""+ componentContentVO.getId() + "\" class=\"draggableItem\" nowrap=\"1\">" + ((componentContentVO.getName().length() > 22) ? componentContentVO.getName().substring(0, 17) : componentContentVO.getName()) + "...</span>");
 				sb.append("     </nobr></div>"); 
 				sb.append("	</td>");
 				
 				imageUrl = "images/componentIcon.gif";
 			}
 			sb.append("  <td width=\"90%\">&nbsp;</td>");
 			
 			//sb.append("	</nobr></td>");
 			sb.append("	</tr>");
 			sb.append("	</table>");
 			sb.append("</div>");
 			
 			sb.append("</div>");
 			
 			sb.append("<script type=\"text/javascript\"> if (bw.bw) tabInit('" + groupName + "Components'); </script>");
 
 			
 			index++;
 		}
 		
 		sb.append("<div id=\"statusListBg\" style=\"position:absolute; top:65px; left:0px; border-bottom: 1px solid gray; border-left: 1px solid white; border-right: 1px solid gray; background:#CCCCCC; width:100%; height:15px;\">");
 		sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
 		sb.append("<tr>");
 		sb.append("	<td align=\"left\" width=\"15px\">&nbsp;<a href=\"#\" onclick=\"moveLeft(currentGroup)\" return false\" onfocus=\"if(this.blur)this.blur()\"><img src=\"images/arrowleft.gif\" alt=\"previous\" border=\"0\"></a></td>");
 		sb.append("	<td align=\"left\" width=\"95%\"><span class=\"componentsStatusText\">Details: </span><span id=\"statusText\" class=\"componentsStatusText\">&nbsp;</span></td>");
 		sb.append("	<td align=\"right\"><a href=\"#\" onclick=\"moveRight(currentGroup)\" return false\" onfocus=\"if(this.blur)this.blur()\"><img src=\"images/arrowright.gif\" alt=\"next\" border=\"0\"></a>&nbsp;</td>");
 		sb.append("</tr>");
 		sb.append("</table>");
 		sb.append("</div>");
 
 		sb.append("	<script type=\"text/javascript\">");
 		sb.append("	  	changeTab('" + openGroupName + "');");
 		
 		sb.append("		var theHandle = document.getElementById(\"paletteHandle\");");
 		sb.append("		var theRoot   = document.getElementById(\"paletteDiv\");");
 		//sb.append("		Drag.init(theHandle, theRoot);");
 		sb.append("		Drag.init(theHandle, theRoot, 0, 0, 0, 1000);");
 
 		//sb.append("     theRoot.style.left = 450;");
 		//sb.append("     theRoot.style.top = 150;");
 		
 		sb.append("	</script>");
 
 		sb.append("</div>");
 		
 		//Caching the result
 		componentPaletteDiv = sb.toString();
 		
 		return sb.toString();
 	}
 /*	
 	private String getComponentPaletteDiv(Integer siteNodeId, Integer languageId, TemplateController templateController) throws Exception
 	{		
 		StringBuffer sb = new StringBuffer();
 	
 		String componentEditorUrl 		= CmsPropertyHandler.getProperty("componentEditorUrl");
 		String componentRendererUrl 	= CmsPropertyHandler.getProperty("componentRendererUrl");
 		String componentRendererAction 	= CmsPropertyHandler.getProperty("componentRendererAction");
 		
 		sb.append("<div id=\"buffer\" style=\"top: 0px; left: 0px; z-index:200;\"><img src=\"images/componentDraggedIcon.gif\"></div>");
 		
 		Map componentGroups = getComponentGroups(getComponentContents(), templateController);
 
 		sb.append("<div style=\"position:absolute; background:#999999; height:20px; width:100%; left:0px; top:0px;\">");
 		sb.append("<table style=\"width:100%\" cellpadding=\"0\" cellspacing=\"0\">");
 		sb.append(" <tr>");
 		
 		Iterator groupIterator = componentGroups.keySet().iterator();
 		int index = 0;
 		String groupName = "";
 		String initialGroupName = "";
 		while(groupIterator.hasNext())
 		{
 			groupName = (String)groupIterator.next();
 
 			if(index == 0)
 			{	
 				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"thistab\" onclick=\"javascript:changeTab('" + groupName + "');\" height=\"20\"><nobr>" + groupName + "</nobr></td>");
 				initialGroupName = groupName;
 			}
 			else if(!groupIterator.hasNext())
 				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"tab\" style=\"border-right: solid thin black\" onclick=\"javascript:changeTab('" + groupName + "');\"><nobr>" + groupName + "</nobr></td>");
 			else
 				sb.append("  <td id=\"" + groupName + "Tab\" valign=\"top\" class=\"tab\" onclick=\"javascript:changeTab('" + groupName + "');\"><nobr>" + groupName + "</nobr></td>");
 
 			index++;
 		}
 		sb.append("  <td width=\"90%\" style=\"border-right: solid thin white; border-bottom: solid thin white\" align=\"right\">&nbsp;<a href=\"javascript:toggleDiv('pageComponents');\" class=\"white\"><img src=\"images/pageStructure.gif\" alt=\"Toggle page structure\" border=\"0\"></a>&nbsp;<a href=\"javascript:window.open(document.location.href, 'PageComponents', '');\"><img src=\"images/fullscreen.gif\" alt=\"Pop up in a large window\" border=\"0\"></a>&nbsp;</td>");
 		sb.append(" </tr>");
 		sb.append("</table>");
 		sb.append("</div>");
 		
 		sb.append("<script type=\"text/javascript\">");
 		sb.append("var currentGroup = \"" + initialGroupName + "\";");
 		sb.append("</script>");
 		
 		sb.append("<div id=\"statusListBg\" class=\"componentsStatus\">");
 		sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
 		sb.append("<tr>");
 		sb.append("	<td align=\"left\" width=\"15px\">&nbsp;<a href=\"#\" onmouseover=\"noScroll=false; mLeft()\" onmouseout=\"noMove('" + groupName + "')\" onclick=\"sScrollPx-=sScrollExtra; return false\" onfocus=\"if(this.blur)this.blur()\" onmousedown=\"sScrollPx+=sScrollExtra\"><img src=\"images/arrowleft.gif\" alt=\"previous\" border=\"0\"></a></td>");
 		sb.append("	<td align=\"left\" width=\"95%\"><span class=\"componentsStatusText\">Details: </span><span id=\"statusText\" class=\"componentsStatusText\">&nbsp;</span></td>");
 		sb.append("	<td align=\"right\"><a href=\"#\" onmouseover=\"noScroll=false; mRight()\" onmouseout=\"noMove('" + groupName + "')\" onclick=\"sScrollPx-=sScrollExtra; return false\" onfocus=\"if(this.blur)this.blur()\" onmousedown=\"sScrollPx+=sScrollExtra\"><img src=\"images/arrowright.gif\" alt=\"next\" border=\"0\"></a>&nbsp;</td>");
 		sb.append("</tr>");
 		sb.append("</table>");
 		sb.append("</div>");
 		
 		String openGroupName = "";
 
 		
 		groupIterator = componentGroups.keySet().iterator();
 		index = 0;
 		while(groupIterator.hasNext())
 		{
 			groupName = (String)groupIterator.next();
 
 			if(index == 0)
 			{
 				sb.append("<div id=\"" + groupName + "ComponentsBg\" class=\"componentsBackground\" style=\"zIndex:3;\">");
 				openGroupName = groupName;
 			}
 			else
 				sb.append("<div id=\"" + groupName + "ComponentsBg\" class=\"componentsBackground\" style=\"zIndex:2;\">");	
 			
 			
 			sb.append("<div class=\"components\" id=\"" + groupName + "Components\" style=\"border: 0px solid green; position:absolute; left:0px; top:0px; visibility:inherit;\">");
 			sb.append("	<table style=\"width:100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
 			sb.append("	<tr>");
 			
 			String imageUrl = "images/componentIcon.gif";
 			List components = (List)componentGroups.get(groupName); //getComponentContents();
 			Iterator componentIterator = components.iterator();
 			while(componentIterator.hasNext())
 			{
 				ContentVO componentContentVO = (ContentVO)componentIterator.next();
 	
 				//String imageUrlTemp = getDigitalAssetUrl(componentContentVO.getId(), "thumbnail");
 				//if(imageUrlTemp != null && imageUrlTemp.length() > 0)
 				//	imageUrl = imageUrlTemp;
 
 				sb.append("		<td class=\"component\" width=\"16\"><img src=\"" + imageUrl + "\" width=\"16\" height=\"16\" border=\"0\"></td>");
 				sb.append("		<td class=\"component\" width=\"50\"><nobr><span onMouseDown=\"grabIt(event);\" onmouseover=\"showDetails('" + componentContentVO.getName() + "');\" id=\""+ componentContentVO.getId() + "\" class=\"draggableItem\" nowrap=\"1\">" + ((componentContentVO.getName().length() > 22) ? componentContentVO.getName().substring(0, 17) : componentContentVO.getName()) + "...</span></nobr></td>");
 				sb.append("     <td class=\"component\" width=\"2\"><img src=\"images/divider.gif\"></td>"); 
 				
 				imageUrl = "images/componentIcon.gif";
 			}
 			sb.append("  <td width=\"90%\">&nbsp;</td>");
 			
 			sb.append("	</tr>");
 			sb.append("	</table>");
 			sb.append("</div>");
 			sb.append("</div>");
 			
 			sb.append("<script type=\"text/javascript\"> if (bw.bw) tabInit('" + groupName + "Components');</script>");
 
 			
 			index++;
 		}
 		
 		sb.append("	<script type=\"text/javascript\">");
 		sb.append("	  javascript:changeTab('" + openGroupName + "');");
 		sb.append("	</script>");
 
 		return sb.toString();
 	}
 */
 
 	/**
 	 * This method gets all component groups from the available components.
 	 * This is dynamically so if one states a different group in the component the group is created.
 	 */
 
 	private Map getComponentGroups(List components, TemplateController templateController)
 	{
 		Map componentGroups = new HashMap();
 		
 		Iterator componentIterator = components.iterator();
 		while(componentIterator.hasNext())
 		{
 			ContentVO componentContentVO = (ContentVO)componentIterator.next();
 			String groupName = templateController.getContentAttribute(componentContentVO.getId(), "GroupName", true);
 			if(groupName == null || groupName.equals(""))
 				groupName = "Other";
 			
 			List groupComponents = (List)componentGroups.get(groupName);
 			if(groupComponents == null)
 			{
 				groupComponents = new ArrayList();
 				componentGroups.put(groupName, groupComponents);
 			}
 			
 			groupComponents.add(componentContentVO);
 		}
 		
 		return componentGroups;
 	}
 
 	/**
 	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate"
 	 */
 	
 	public List getComponentContents() throws Exception
 	{
 		HashMap arguments = new HashMap();
 		arguments.put("method", "selectListOnContentTypeName");
 		
 		List argumentList = new ArrayList();
 		HashMap argument = new HashMap();
 		argument.put("contentTypeDefinitionName", "HTMLTemplate");
 		argumentList.add(argument);
 		arguments.put("arguments", argumentList);
 		
 		return ContentController.getContentController().getContentVOList(arguments);
 	}
 	
 	/**
 	 * This method fetches an url to the asset for the component.
 	 */
 	
 	public String getDigitalAssetUrl(Integer contentId, String key) throws Exception
 	{
 		String imageHref = null;
 		try
 		{
 			LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(ContentController.getContentController().getContentVOWithId(contentId).getRepositoryId());
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguage.getId());
 			List digitalAssets = DigitalAssetController.getDigitalAssetVOList(contentVersionVO.getId());
 			Iterator i = digitalAssets.iterator();
 			while(i.hasNext())
 			{
 				DigitalAssetVO digitalAssetVO = (DigitalAssetVO)i.next();
 				if(digitalAssetVO.getAssetKey().equals(key))
 				{
 					imageHref = DigitalAssetController.getDigitalAssetUrl(digitalAssetVO.getId()); 
 					break;
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logWarning("We could not get the url of the digitalAsset: " + e.getMessage(), e);
 			imageHref = e.getMessage();
 		}
 		
 		return imageHref;
 	}
 
 
 	/**
 	 * This method fetches the pageComponent structure as a document.
 	 */
 	    
 	protected org.w3c.dom.Document getComponentPropertiesDocument(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{ 
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "componentPropertiesDocument_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
 		org.w3c.dom.Document cachedComponentPropertiesDocument = (org.w3c.dom.Document)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedComponentPropertiesDocument != null)
 			return cachedComponentPropertiesDocument;
 		
 		org.w3c.dom.Document componentPropertiesDocument = null;
     	
 		try
 		{
 			String xml = this.getComponentPropertiesString(templateController, siteNodeId, languageId, contentId);
 			//CmsLogger.logInfo("xml: " + xml);
 			if(xml != null && xml.length() > 0)
 			{
 				componentPropertiesDocument = XMLHelper.readDocumentFromByteArray(xml.getBytes("UTF-8"));
 				
 				CacheController.cacheObject(cacheName, cacheKey, componentPropertiesDocument);
 			}
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere(e.getMessage(), e);
 			throw e;
 		}
 		
 		return componentPropertiesDocument;
 	}
 
 	/**
 	 * This method fetches the template-string.
 	 */
     
 	private String getComponentPropertiesString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "componentPropertiesString_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
 		String cachedComponentPropertiesString = (String)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedComponentPropertiesString != null)
 			return cachedComponentPropertiesString;
 			
 		String componentPropertiesString = null;
     	
 		try
 		{
 		    Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(siteNodeId).getId();
 		    //CmsLogger.logInfo("masterLanguageId:" + masterLanguageId);
 		    componentPropertiesString = templateController.getContentAttribute(contentId, masterLanguageId, "ComponentProperties", true);
 
 			if(componentPropertiesString == null)
 				throw new SystemException("There was no properties assigned to this content.");
 		
 			CacheController.cacheObject(cacheName, cacheKey, componentPropertiesString);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere(e.getMessage(), e);
 			throw e;
 		}
 
 		return componentPropertiesString;
 	}
 	
 	
 	/**
 	 * This method fetches the tasks as a document.
 	 */
 	    
 	protected org.w3c.dom.Document getComponentTasksDocument(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{ 	    
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "componentTasksDocument_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
 		org.w3c.dom.Document cachedComponentTasksDocument = (org.w3c.dom.Document)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedComponentTasksDocument != null)
 			return cachedComponentTasksDocument;
 		
 		org.w3c.dom.Document componentTasksDocument = null;
     	
 		try
 		{
 			String xml = this.getComponentTasksString(templateController, siteNodeId, languageId, contentId);
 			if(xml != null && xml.length() > 0)
 			{
 			    componentTasksDocument = XMLHelper.readDocumentFromByteArray(xml.getBytes("UTF-8"));
 				
 				CacheController.cacheObject(cacheName, cacheKey, componentTasksDocument);
 			}
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere(e.getMessage(), e);
 			throw e;
 		}
 		
 		return componentTasksDocument;
 	}
 
 	/**
 	 * This method fetches the tasks for a certain component.
 	 */
     
 	private String getComponentTasksString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{
 		String cacheName 	= "componentEditorCache";
 		String cacheKey		= "componentTasksString_" + siteNodeId + "_" + templateController.getLanguageId() + "_" + contentId;
 		String cachedComponentTasksString = (String)CacheController.getCachedObject(cacheName, cacheKey);
 		if(cachedComponentTasksString != null)
 			return cachedComponentTasksString;
 			
 		String componentTasksString = null;
     	
 		try
 		{
 		    Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(siteNodeId).getId();
 		    componentTasksString = templateController.getContentAttribute(contentId, masterLanguageId, "ComponentTasks", true);
 
 			if(componentTasksString == null)
 				throw new SystemException("There was no tasks assigned to this content.");
 		
 			CacheController.cacheObject(cacheName, cacheKey, componentTasksString);
 		}
 		catch(Exception e)
 		{
 			CmsLogger.logSevere(e.getMessage(), e);
 			throw e;
 		}
 
 		return componentTasksString;
 	}
 	
 	/*
 	 * This method returns a bean representing a list of ComponentProperties that the component has.
 	 */
 	 
 	private List getComponentProperties(Integer componentId, org.w3c.dom.Document document/*String componentPropertiesXML*/) throws Exception
 	{
 		//CmsLogger.logInfo("componentPropertiesXML:" + componentPropertiesXML);
 		List componentProperties = new ArrayList();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		try
 		{
 			if(document != null)
 			{
 			//if(componentPropertiesXML != null && componentPropertiesXML.length() > 0)
 			//{
 				//org.w3c.dom.Document document = XMLHelper.readDocumentFromByteArray(componentPropertiesXML.getBytes("UTF-8"));
 
 				timer.printElapsedTime("Read document");
 
 				String propertyXPath = "//property";
 				//CmsLogger.logInfo("propertyXPath:" + propertyXPath);
 				NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), propertyXPath);
 				timer.printElapsedTime("Set property xpath");
 				//CmsLogger.logInfo("*********************************************************anl:" + anl.getLength());
 				for(int i=0; i < anl.getLength(); i++)
 				{
 					org.w3c.dom.Element binding = (org.w3c.dom.Element)anl.item(i);
 					
 					String name		= binding.getAttribute("name");
 					String type		= binding.getAttribute("type");
 					String visualizingAction = binding.getAttribute("visualizingAction");
 					String createAction = binding.getAttribute("createAction");
 					//CmsLogger.logInfo("name:" + name);
 					//CmsLogger.logInfo("type:" + type);
 
 					ComponentProperty property = new ComponentProperty();
 					property.setName(name);
 					property.setType(type);
 					property.setVisualizingAction(visualizingAction);
 					property.setCreateAction(createAction);
 					
 					if(type.equalsIgnoreCase(ComponentProperty.BINDING))
 					{
 						String entity 	= binding.getAttribute("entity");
 						boolean isMultipleBinding = new Boolean(binding.getAttribute("multiple")).booleanValue();
 						
 						property.setEntityClass(entity);
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property1");
 
 						property.setValue(value);
 						property.setIsMultipleBinding(isMultipleBinding);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.TEXTFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property2");
 
 						//CmsLogger.logInfo("value:" + value);
 						property.setValue(value);
 					}
 					
 					componentProperties.add(property);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			CmsLogger.logWarning("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
 		}
 							
 		return componentProperties;
 	}
 
 
 	/*
 	 * This method returns a bean representing a list of ComponentProperties that the component has.
 	 */
 	 
 	private List getComponentTasks(Integer componentId, org.w3c.dom.Document document) throws Exception
 	{
 		List componentTasks = new ArrayList();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		try
 		{
 			if(document != null)
 			{
 				timer.printElapsedTime("Read document");
 
 				String propertyXPath = "//task";
 				//CmsLogger.logInfo("propertyXPath:" + propertyXPath);
 				NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), propertyXPath);
 				timer.printElapsedTime("Set property xpath");
 				for(int i=0; i < anl.getLength(); i++)
 				{
 					org.w3c.dom.Element binding = (org.w3c.dom.Element)anl.item(i);
 					
 					String name		= binding.getAttribute("name");
 					String view		= binding.getAttribute("view");
 					CmsLogger.logInfo("name:" + name);
 					CmsLogger.logInfo("view:" + view);
 
 					ComponentTask task = new ComponentTask();
 					task.setName(name);
 					task.setView(view);
 					task.setComponentId(componentId);
 					
 					componentTasks.add(task);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			CmsLogger.logWarning("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
 		}
 							
 		return componentTasks;
 	}
 
 
 	/**
 	 * This method returns a value for a property if it's set. The value is collected in the
 	 * properties for the page.
 	 */
 	
 	private String getComponentPropertyValue(Integer componentId, String name) throws Exception
 	{
 		String value = "Undefined";
 		
 		Timer timer = new Timer();
 		timer.setActive(false);
 				
 		Integer siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
 		Integer languageId = new Integer(this.getRequest().getParameter("languageId"));
 		Integer contentId  = new Integer(-1);
 		
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(languageId);
 		
 		timer.printElapsedTime("AAA1");
 		
 		if(this.getRequest().getParameter("contentId") == null)
 			contentId  = new Integer(this.getRequest().getParameter("contentId"));
 
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 		IntegrationDeliveryController integrationDeliveryController = IntegrationDeliveryController.getIntegrationDeliveryController(siteNodeId, languageId, contentId);
 		
 		/*
 		String componentXML = getPageComponentsString(this.getTemplateController(), siteNodeId, languageId, contentId);			
 		////CmsLogger.logInfo("componentXML:" + componentXML);
 
 		timer.printElapsedTime("AAA2");
 
 		org.w3c.dom.Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		*/
 		org.w3c.dom.Document document = getPageComponentsDocument(this.getTemplateController(), siteNodeId, languageId, contentId);
 		
 		timer.printElapsedTime("AAA3");
 
 		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
 		//CmsLogger.logInfo("componentXPath:" + componentXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		for(int i=0; i < anl.getLength(); i++)
 		{
 			org.w3c.dom.Element property = (org.w3c.dom.Element)anl.item(i);
 			
 			String id 			= property.getAttribute("type");
 			String path 		= property.getAttribute("path");
 			//CmsLogger.logInfo("path:" + path);
 			//CmsLogger.logInfo("path:" + "path_" + locale.getLanguage() + ":" + property.hasAttribute("path_" + locale.getLanguage()));
 			if(property.hasAttribute("path_" + locale.getLanguage()))
 				path = property.getAttribute("path_" + locale.getLanguage());
 			//CmsLogger.logInfo("path:" + path);
 				
 			value 				= path;
 		}
 		timer.printElapsedTime("AAA4");
 
 		
 		return value;
 	}
 
 
 	/*
 	 * This method returns a bean representing a list of bindings that the component has.
 	 */
 	 
 	private List getContentBindnings(Integer componentId) throws Exception
 	{
 		List contentBindings = new ArrayList();
 		
 		Integer siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
 		Integer languageId = new Integer(this.getRequest().getParameter("languageId"));
 		Integer contentId  = new Integer(this.getRequest().getParameter("contentId"));
 
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 		IntegrationDeliveryController integrationDeliveryController = IntegrationDeliveryController.getIntegrationDeliveryController(siteNodeId, languageId, contentId);
 		
 		boolean USE_LANGUAGE_FALLBACK        			= true;
 		boolean DO_NOT_USE_LANGUAGE_FALLBACK 			= false;
 
 		String componentXML = getPageComponentsString(this.getTemplateController(), siteNodeId, languageId, contentId);			
 		////CmsLogger.logInfo("componentXML:" + componentXML);
 
 		org.w3c.dom.Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		String componentXPath = "//component[@id=" + componentId + "]/bindings/binding";
 		//CmsLogger.logInfo("componentXPath:" + componentXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		for(int i=0; i < anl.getLength(); i++)
 		{
 			org.w3c.dom.Element binding = (org.w3c.dom.Element)anl.item(i);
 			//CmsLogger.logInfo(XMLHelper.serializeDom(binding, new StringBuffer()));
 			//CmsLogger.logInfo("YES - we read the binding properties...");		
 			
 			String id 			= binding.getAttribute("id");
 			String entityClass 	= binding.getAttribute("entity");
 			String entityId 	= binding.getAttribute("entityId");
 			//CmsLogger.logInfo("id:" + id);
 			//CmsLogger.logInfo("entityClass:" + entityClass);
 			//CmsLogger.logInfo("entityId:" + entityId);
 			
 			if(entityClass.equalsIgnoreCase("Content"))
 			{
 				ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(entityId));
 				ComponentBinding componentBinding = new ComponentBinding();
 				componentBinding.setId(new Integer(id));
 				componentBinding.setComponentId(componentId);
 				componentBinding.setEntityClass(entityClass);
 				componentBinding.setEntityId(new Integer(entityId));
 				componentBinding.setBindingPath(contentVO.getName());
 				
 				contentBindings.add(componentBinding);
 			}
 		}
 			
 		return contentBindings;
 	}
 	 
 	 
 	private void printComponentHierarchy(List pageComponents, int level)
 	{
 		Iterator pageComponentIterator = pageComponents.iterator();
 		while(pageComponentIterator.hasNext())
 		{
 			InfoGlueComponent tempComponent = (InfoGlueComponent)pageComponentIterator.next();
 			
 			for(int i=0; i<level; i++)
 				System.out.print(" ");
 			
 			CmsLogger.logInfo("  component:" + tempComponent.getName());
 			
 			Iterator slotIterator = tempComponent.getSlotList().iterator();
 			while(slotIterator.hasNext())
 			{
 				Slot slot = (Slot)slotIterator.next();
 				
 				for(int i=0; i<level; i++)
 					CmsLogger.logInfo(" ");
 					
 				CmsLogger.logInfo(" slot for " + tempComponent.getName() + ":" + slot.getId());
 				printComponentHierarchy(slot.getComponents(), level + 1);
 			}
 		}			
 	}
 	
 }
