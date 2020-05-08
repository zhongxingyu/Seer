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
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.PageTemplateController;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.io.FileHelper;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.StringManager;
 import org.infoglue.cms.util.StringManagerFactory;
 import org.infoglue.cms.util.XMLHelper;
 import org.infoglue.cms.util.dom.DOMBuilder;
 import org.infoglue.deliver.applications.actions.InfoGlueComponent;
 import org.infoglue.deliver.applications.databeans.ComponentBinding;
 import org.infoglue.deliver.applications.databeans.ComponentProperty;
 import org.infoglue.deliver.applications.databeans.ComponentPropertyOption;
 import org.infoglue.deliver.applications.databeans.ComponentTask;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.applications.databeans.Slot;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.DecoratedComponentLogic;
 import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
 import org.infoglue.deliver.util.CacheController;
 import org.infoglue.deliver.util.Timer;
 import org.infoglue.deliver.util.VelocityTemplateProcessor;
 import org.w3c.dom.NodeList;
 
 /**
 * @author Mattias Bogeblad
 *
 * This class delivers a normal html page by using the component-based method but also decorates it
 * so it can be used by the structure tool to manage the page components.
 */
 
 public class DecoratedComponentBasedHTMLPageInvoker extends ComponentBasedHTMLPageInvoker
 {
     private final static Logger logger = Logger.getLogger(DecoratedComponentBasedHTMLPageInvoker.class.getName());
 
 	private String propertiesDivs 	= "";
 	private String tasksDivs 		= "";
 	
 	/**
 	 * This is the method that will render the page. It uses the new component based structure. 
 	 */ 
 	
 	public void invokePage() throws SystemException, Exception
 	{
 		Timer timer = new Timer();
 		timer.setActive(false);
 		
 		String decoratePageTemplate = "";
 		
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(this.getDeliveryContext());
 		
 		timer.printElapsedTime("Initialized controllers");
 		
 		Integer repositoryId = nodeDeliveryController.getSiteNode(getDatabase(), this.getDeliveryContext().getSiteNodeId()).getRepository().getId();
 		String componentXML = getPageComponentsString(getDatabase(), this.getTemplateController(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
 		//logger.info("componentXML:" + componentXML);
 		
 		timer.printElapsedTime("After getPageComponentsString");
 		
 		Timer decoratorTimer = new Timer();
 		decoratorTimer.setActive(false);
 		
 		if(componentXML == null || componentXML.length() == 0)
 		{
 			decoratePageTemplate = showInitialBindingDialog(this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId());
 		}
 		else
 		{
 		    Document document = null;
 		    try
 		    {
 		        document = new DOMBuilder().getDocument(componentXML);
 		    }
 		    catch(Exception e)
 		    {
 		        throw new SystemException("There was a problem parsing the component structure on the page. Could be invalid XML in the ComponentStructure attribute. Message:" + e.getMessage(), e);
 		    }
 		    
 			decoratorTimer.printElapsedTime("After reading document");
 			
 			List pageComponents = getPageComponents(getDatabase(), document.getRootElement(), "base", this.getTemplateController(), null);
 
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
 				//if(this.getDeliveryContext().getShowSimple() == true)
 			    //{
 			    //    decoratePageTemplate = showSimplePageStructure(this.getTemplateController(), repositoryId, this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), baseComponent);
 			    //}
 			    //else
 			    //{
 				    ContentVO metaInfoContentVO = nodeDeliveryController.getBoundContent(getDatabase(), this.getTemplateController().getPrincipal(), this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), true, "Meta information", this.getDeliveryContext());
 					decoratePageTemplate = decorateComponent(baseComponent, this.getTemplateController(), repositoryId, this.getDeliveryContext().getSiteNodeId(), this.getDeliveryContext().getLanguageId(), this.getDeliveryContext().getContentId()/*, metaInfoContentVO.getId()*/);
 					decoratePageTemplate = decorateTemplate(this.getTemplateController(), decoratePageTemplate, this.getDeliveryContext(), baseComponent);
 				//}
 			}
 		}
 		
 		timer.printElapsedTime("After main decoration");
 		
 		//TODO - TEST
 		decoratePageTemplate += propertiesDivs + tasksDivs;
 		
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 		
 		//-- moved the creation of a default context into the baseclass
 		// (robert)
 		Map context = getDefaultContext();
 
 		context.put("componentEditorUrl", componentEditorUrl);
 		boolean oldUseFullUrl = this.getTemplateController().getDeliveryContext().getUseFullUrl();
 		this.getTemplateController().getDeliveryContext().setUseFullUrl(true);
 		context.put("currentUrl", URLEncoder.encode(this.getTemplateController().getCurrentPageUrl(), "UTF-8"));
 		context.put("contextName", this.getRequest().getContextPath());
 		
 		this.getTemplateController().getDeliveryContext().setUseFullUrl(oldUseFullUrl);
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
 		 String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 		 String url = "javascript:window.open('" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&specifyBaseTemplate=true&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "', 'BaseTemplate', 'width=600,height=700,left=50,top=50,toolbar=no,status=no,scrollbars=yes,location=no,menubar=no,directories=no,resizable=yes');";
 		 
 		 String pageTemplateHTML = " or choose a page template below.<br><br>";
 		 
 	     boolean foundPageTemplate = false;
 
 	     try
 		 {
 		     List sortedPageTemplates = PageTemplateController.getController().getPageTemplates(this.getTemplateController().getPrincipal());
 			 Iterator sortedPageTemplatesIterator = sortedPageTemplates.iterator();
 			 while(sortedPageTemplatesIterator.hasNext())
 			 {
 			     ContentVO contentVO = (ContentVO)sortedPageTemplatesIterator.next();
 			     ContentVersionVO contentVersionVO = this.getTemplateController().getContentVersion(contentVO.getId(), LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), siteNodeId).getId(), false);
 			     if(contentVersionVO != null)
 			     {
 				     String imageUrl = this.getTemplateController().getAssetUrl(contentVO.getId(), "thumbnail");
 				     if(imageUrl == null || imageUrl.equals(""))
 				         imageUrl = this.getRequest().getContextPath() + "/images/undefinedPageTemplate.jpg";
 				 
 				     pageTemplateHTML += "<div style=\"border: 1px solid #C2D0E2; float: left; text-align: center; padding: 5px 5px 5px 5px;\"><a href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!addPageTemplate.action?repositoryId=" + contentVO.getRepositoryId() + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&pageTemplateContentId=" + contentVO.getId() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + imageUrl + "\" border=\"0\"><br>";
 				     pageTemplateHTML += contentVO.getName() + "</a>";
 				     pageTemplateHTML += "</div>";	
 				     foundPageTemplate = true;
 			     }
 			 }
 		 }
 		 catch(Exception e)
 		 {
 		     logger.warn("A problem arouse when getting the page templates:" + e.getMessage(), e);
 		 }
 		 
 		 this.getTemplateController().getDeliveryContext().setContentType("text/html");
 		 this.getTemplateController().getDeliveryContext().setDisablePageCache(true);
 		 return "<html><body style=\"font-family:verdana, sans-serif; font-size:10px;\">The page has no base component assigned yet. Click <a href=\"" + url + "\">here</a> to assign one" + (foundPageTemplate ? pageTemplateHTML : "") + "</body></html>";
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
 		    InfoGluePrincipal principal = templateController.getPrincipal();
 		    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
 		    if(cmsUserName != null)
 			    principal = templateController.getPrincipal(cmsUserName);
 
 			boolean hasAccessToAccessRights = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeSlotAccess", "");
 			boolean hasAccessToAddComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.AddComponent", "" + component.getContentId() + "_" + component.getSlotName());
 			boolean hasAccessToDeleteComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.DeleteComponent", "" + component.getContentId() + "_" + component.getSlotName());
 			
 		    String extraHeader 	= FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "preview/pageComponentEditorHeader.vm"));
 		    String extraBody 	= FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "preview/pageComponentEditorBody.vm"));
 			
 		    Locale locale = templateController.getLocale();
 			String submitToPublishHTML = getLocalizedString(locale, "deliver.editOnSight.submitToPublish");
 		    String addComponentHTML = getLocalizedString(locale, "deliver.editOnSight.addComponentHTML");
 			String deleteComponentHTML = getLocalizedString(locale, "deliver.editOnSight.deleteComponentHTML");
 			String accessRightsHTML = getLocalizedString(locale, "deliver.editOnSight.accessRightsHTML");
 			String pageComponentsHTML = getLocalizedString(locale, "deliver.editOnSight.pageComponentsHTML");
 			String viewSourceHTML = getLocalizedString(locale, "deliver.editOnSight.viewSourceHTML");
 			String componentEditorInNewWindowHTML = getLocalizedString(locale, "deliver.editOnSight.componentEditorInNewWindowHTML");
 			String savePageTemplateHTML = getLocalizedString(locale, "deliver.editOnSight.savePageTemplateHTML");
 
 			String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 			String saveTemplateUrl = "" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + templateController.getSiteNode(deliveryContext.getSiteNodeId()).getMetaInfoContentId();
 			
 			extraBody = extraBody.replaceAll("\\$siteNodeId", "" + templateController.getSiteNodeId());
 			extraBody = extraBody.replaceAll("\\$repositoryId", "" + templateController.getSiteNode().getRepositoryId());
 			extraBody = extraBody.replaceAll("\\$originalFullURL", URLEncoder.encode(templateController.getOriginalFullURL(), "UTF-8"));
 			
 			extraBody = extraBody.replaceAll("\\$submitToPublishHTML", submitToPublishHTML);
 			extraBody = extraBody.replaceAll("\\$addComponentHTML", addComponentHTML);
 			extraBody = extraBody.replaceAll("\\$deleteComponentHTML", deleteComponentHTML);
 		    extraBody = extraBody.replaceAll("\\$accessRightsHTML", accessRightsHTML);
 		    
 		    extraBody = extraBody.replaceAll("\\$pageComponents", pageComponentsHTML);
 		    extraBody = extraBody.replaceAll("\\$componentEditorInNewWindowHTML", componentEditorInNewWindowHTML);
 		    extraBody = extraBody.replaceAll("\\$savePageTemplateHTML", savePageTemplateHTML);
 		    extraBody = extraBody.replaceAll("\\$saveTemplateUrl", saveTemplateUrl);
 		    extraBody = extraBody.replaceAll("\\$viewSource", viewSourceHTML);
 		    
 		    extraBody = extraBody.replaceAll("\\$addComponentJavascript", "var hasAccessToAddComponent" + component.getSlotName() + " = " + hasAccessToAddComponent + ";");
 		    extraBody = extraBody.replaceAll("\\$deleteComponentJavascript", "var hasAccessToDeleteComponent" + component.getSlotName() + " = " + hasAccessToDeleteComponent + ";");
 		    extraBody = extraBody.replaceAll("\\$changeAccessJavascript", "var hasAccessToAccessRights" + component.getSlotName() + " = " + hasAccessToAccessRights + ";");
 		    
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
 					logger.info("The current template is not a valid document. It does not comply with the simplest standards such as having a correct header.");
 				}
 			}
 
 			timer.printElapsedTime("Header handled");
 
 			//Adding stuff in the body	
 			int indexOfBodyStartTag = modifiedTemplate.indexOf("<body");
 			if(indexOfBodyStartTag == -1)
 				indexOfBodyStartTag = modifiedTemplate.indexOf("<BODY");
 			
 			if(indexOfBodyStartTag > -1)
 			{
 			    //String pageComponentStructureDiv = "";
 				String pageComponentStructureDiv = getPageComponentStructureDiv(templateController, deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), component);
 				timer.printElapsedTime("pageComponentStructureDiv");
 				String componentPaletteDiv = getComponentPaletteDiv(deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), templateController);
 				//String componentPaletteDiv = "";
 				timer.printElapsedTime("componentPaletteDiv");
 				modifiedTemplate = modifiedTemplate.insert(modifiedTemplate.indexOf(">", indexOfBodyStartTag) + 1, extraBody + pageComponentStructureDiv + componentPaletteDiv);
 			}
 			else
 			{
 				logger.info("The current template is not a valid document. It does not comply with the simplest standards such as having a correct body.");
 			}
 			
 			timer.printElapsedTime("Body handled");
 
 			decoratedTemplate = modifiedTemplate.toString();
 		}
 		catch(Exception e)
 		{
 			logger.warn("An error occurred when deliver tried to decorate your template to enable onSiteEditing. Reason " + e.getMessage(), e);
 		}
 		
 		return decoratedTemplate;
 	}
 
    
 	private String decorateComponent(InfoGlueComponent component, TemplateController templateController, Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId/*, Integer metainfoContentId*/) throws Exception
 	{
 		String decoratedComponent = "";
 		
 		//logger.info("decorateComponent.contentId:" + contentId);
 
 		//logger.info("decorateComponent:" + component.getName());
 		
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		try
 		{
 			String componentString = getComponentString(templateController, component.getContentId(), component); 
 
 			if(component.getParentComponent() == null && templateController.getDeliveryContext().getShowSimple())
 			{
 			    templateController.getDeliveryContext().setContentType("text/html");
 			    templateController.getDeliveryContext().setDisablePageCache(true);
 			    componentString = "<html><head></head><body onload=\"toggleDiv('pageComponents');\">" + componentString + "</body></html>";
 			}
 			
 			templateController.setComponentLogic(new DecoratedComponentLogic(templateController, component));
 			Map context = super.getDefaultContext();
 			context.put("templateLogic", templateController);
 			StringWriter cacheString = new StringWriter();
 			PrintWriter cachedStream = new PrintWriter(cacheString);
 			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, componentString);
 			componentString = cacheString.toString();
 	
 			int bodyIndex = componentString.indexOf("<body");
 			if(bodyIndex == -1)
 				bodyIndex = componentString.indexOf("<BODY");
 		
 			if(component.getParentComponent() == null && bodyIndex > -1)
 			{
 				//String onContextMenu = " onload=\"javascript:setToolbarInitialPosition();\"";
 				//String onContextMenu = " class=\"siteBody\" onload=\"javascript:setToolbarInitialPosition();\"";
 				String onContextMenu = " onload=\"javascript:setToolbarInitialPosition();\"";
 				if(templateController.getDeliveryContext().getShowSimple())
 					onContextMenu = " onload=\"javascript:setToolbarInitialPosition();\"";
 				
 				
 				StringBuffer sb = new StringBuffer(componentString);
 				sb.insert(bodyIndex + 5, onContextMenu);
 				componentString = sb.toString();
 
 				org.w3c.dom.Document componentPropertiesDocument = getComponentPropertiesDocument(templateController, siteNodeId, languageId, component.getContentId()); 
 				this.propertiesDivs += getComponentPropertiesDiv(templateController, repositoryId, siteNodeId, languageId, contentId, component.getId(), component.getContentId(), componentPropertiesDocument);
 
 				org.w3c.dom.Document componentTasksDocument = getComponentTasksDocument(templateController, siteNodeId, languageId, component.getContentId()); 
 				this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, component, componentTasksDocument, templateController);
 			}
 	
 			////logger.info("Before:" + componentString);
 			int offset = 0;
 			int slotStartIndex = componentString.indexOf("<ig:slot", offset);
 			//logger.info("slotStartIndex:" + slotStartIndex);
 			while(slotStartIndex > -1)
 			{
 				decoratedComponent += componentString.substring(offset, slotStartIndex);
 				int slotStopIndex = componentString.indexOf("</ig:slot>", slotStartIndex);
 				
 				String slot = componentString.substring(slotStartIndex, slotStopIndex + 10);
 				//logger.info("Slot:" + slot);
 				String id = slot.substring(slot.indexOf("id") + 4, slot.indexOf("\"", slot.indexOf("id") + 4));
 				
 				Slot slotBean = new Slot();
 			    String[] allowedComponentNamesArray = null;
 				int allowedComponentNamesIndex = slot.indexOf("allowedComponentNames");
 				if(allowedComponentNamesIndex > -1)
 				{    
 				    String allowedComponentNames = slot.substring(allowedComponentNamesIndex + 23, slot.indexOf("\"", allowedComponentNamesIndex + 23));
 				    allowedComponentNamesArray = allowedComponentNames.split(",");
 
 				    slotBean.setId(id);
 				    slotBean.setAllowedComponentsArray(allowedComponentNamesArray);
 				}
 
 				boolean inherit = true;
 				int inheritIndex = slot.indexOf("inherit");
 				if(inheritIndex > -1)
 				{    
 				    String inheritString = slot.substring(inheritIndex + 9, slot.indexOf("\"", inheritIndex + 9));
 				    inherit = Boolean.getBoolean(inheritString);
 				}
 				slotBean.setInherit(inherit);
 				
 				String subComponentString = "";
 				
 				//TODO - test
 				if(component.getIsInherited())
 				    subComponentString += "<div id=\"" + component.getId() + "_" + id + "\" class=\"inheritedComponentDiv\");\">";
 				else
 				    subComponentString += "<div id=\"" + component.getId() + "_" + id + "\" class=\"componentDiv\" onmouseup=\"javascript:assignComponent('" + siteNodeId + "', '" + languageId + "', '" + contentId + "', '" + component.getId() + "', '" + id + "', '" + false + "', '" + slotBean.getAllowedComponentsArrayAsUrlEncodedString() + "');\">";
 				    
 				List subComponents = getInheritedComponents(getDatabase(), templateController, component, templateController.getSiteNodeId(), id, inherit);
 
 			    InfoGluePrincipal principal = templateController.getPrincipal();
 			    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
 			    if(cmsUserName != null)
 				    principal = templateController.getPrincipal(cmsUserName);
 
 				String clickToAddHTML = "";
 				boolean hasAccessToAddComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.AddComponent", "" + component.getContentId() + "_" + id);
 				if(hasAccessToAddComponent)
 				    clickToAddHTML = getLocalizedString(templateController.getLocale(), "deliver.editOnSight.slotInstructionHTML");
 				
 				//logger.info("subComponents for " + id + ":" + subComponents);
 				if(subComponents != null && subComponents.size() > 0)
 				{
 					//logger.info("SUBCOMPONENTS:" + subComponents.size());
 					int index = 0;
 					Iterator subComponentsIterator = subComponents.iterator();
 					while(subComponentsIterator.hasNext())
 					{
 						InfoGlueComponent subComponent = (InfoGlueComponent)subComponentsIterator.next();
 						if(subComponent != null)
 						{
 							component.getComponents().put(subComponent.getSlotName(), subComponent);
 							if(subComponent.getIsInherited())
 							{
 								//logger.info("Inherited..." + contentId);
 								String childComponentsString = decorateComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId/*, metainfoContentId*/);
 								if(!this.getTemplateController().getDeliveryContext().getShowSimple())
 								    subComponentString += "<span id=\""+ id + index + "Comp\" class=\"inheritedslot\">" + childComponentsString + "</span>";
 								else
 								    subComponentString += childComponentsString;
 								    
 								org.w3c.dom.Document componentPropertiesDocument = getComponentPropertiesDocument(templateController, siteNodeId, languageId, component.getContentId()); 
 								this.propertiesDivs += getComponentPropertiesDiv(templateController, repositoryId, siteNodeId, languageId, contentId, new Integer(siteNodeId.intValue()*100 + subComponent.getId().intValue()), subComponent.getContentId(), componentPropertiesDocument);
 								
 								org.w3c.dom.Document componentTasksDocument = getComponentTasksDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, subComponent, componentTasksDocument, templateController);
 								
 							}
 							else
 							{
 								//logger.info("Not inherited..." + contentId);
 								String childComponentsString = decorateComponent(subComponent, templateController, repositoryId, siteNodeId, languageId, contentId/*, metainfoContentId*/);
 								//logger.info("childComponentsString:" + childComponentsString);
 								
 								if(!this.getTemplateController().getDeliveryContext().getShowSimple())
 								{    
 								    String allowedComponentNamesAsEncodedString = null;
 
 								    for(int i=0; i < subComponent.getParentComponent().getSlotList().size(); i++)
 								    {
 								        Slot subSlotBean = (Slot)subComponent.getParentComponent().getSlotList().get(i);
 
 								        if(subSlotBean.getId() != null && subSlotBean.getId().equals(subComponent.getSlotName()))
 								        {
 								            allowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
 								        }
 								    }
 
 								    subComponentString += "<span id=\""+ id + index + "_" + subComponent.getId() + "Comp\">" + childComponentsString + "<script type=\"text/javascript\">initializeComponentEventHandler('" + id + index + "_" + subComponent.getId() + "Comp', '" + subComponent.getId() + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&parentComponentId=" + component.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "&AAAA=1") + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponent.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + subComponent.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "');</script></span>";
 								}
 								else
 								{
 								    subComponentString += childComponentsString;
 								}
 								
 								org.w3c.dom.Document componentPropertiesDocument = getComponentPropertiesDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								this.propertiesDivs += getComponentPropertiesDiv(templateController, repositoryId, siteNodeId, languageId, contentId, subComponent.getId(), subComponent.getContentId(), componentPropertiesDocument);
 								
 								org.w3c.dom.Document componentTasksDocument = getComponentTasksDocument(templateController, siteNodeId, languageId, subComponent.getContentId()); 
 								this.tasksDivs += getComponentTasksDiv(repositoryId, siteNodeId, languageId, contentId, subComponent, componentTasksDocument, templateController);
 							}
 						}
 						index++;
 					}
 					subComponentString += "" + clickToAddHTML;
 				}
 				else
 				{
 					subComponentString += "" + clickToAddHTML;
 				}
 				
 				if(!component.getIsInherited())
 				{
 				    String allowedComponentNamesAsEncodedString = null;
 				    
 				    for(int i=0; i < component.getSlotList().size(); i++)
 				    {
 				        Slot subSlotBean = (Slot)component.getSlotList().get(i);
 				        if(subSlotBean.getId() != null && subSlotBean.getId().equals(id))
 				        {
 				            allowedComponentNamesAsEncodedString = subSlotBean.getAllowedComponentsArrayAsUrlEncodedString();
 				        }
 				    }
 
 				    subComponentString += "<script type=\"text/javascript\">initializeSlotEventHandler('" + component.getId() + "_" + id + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&parentComponentId=" + component.getId() + "&slotId=" + id + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "&BBBB=1") + "', '', '" + id + "', '" + component.getContentId() + "');</script></div>";
 				}
 				else
 				    subComponentString += "</div>";
 				  
 				/**
 				 * 
 				 */
 
 				boolean hasAccessToAccessRights = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeSlotAccess", "");
 				boolean hasAccessToDeleteComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.DeleteComponent", "" + component.getContentId() + "_" + id);
 				
 			    StringBuffer sb = new StringBuffer();
 			    sb.append("<script type=\"text/javascript\">");
 				//sb.append("<!--");
 				sb.append("hasAccessToAddComponent" + id + " = " + hasAccessToAddComponent + ";");
 				sb.append("hasAccessToAccessRights" + id + " = " + hasAccessToAccessRights + ";");
 			    //sb.append("-->");
 				sb.append("</script>");
 
 				subComponentString += sb.toString();
 			    /**
 			     * 
 			     */
 			    
 				decoratedComponent += subComponentString;
 							
 				offset = slotStopIndex + 10;
 				slotStartIndex = componentString.indexOf("<ig:slot", offset);
 			}
 			
 			//logger.info("offset:" + offset);
 			decoratedComponent += componentString.substring(offset);
 		}
 		catch(Exception e)
 		{		
 			logger.warn("An component with either an empty template or with no template in the sitelanguages was found:" + e.getMessage(), e);	
 		}
 		
 		return decoratedComponent;
 	}
 
 
 	/**
 	 * This method creates a div for the components properties.
 	 */
 	
 	private String getComponentPropertiesDiv(TemplateController templateController, Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId, Integer componentId, Integer componentContentId, org.w3c.dom.Document document) throws Exception
 	{	
 	    if(templateController.getRequestParameter("skipPropertiesDiv") != null && templateController.getRequestParameter("skipPropertiesDiv").equalsIgnoreCase("true"))
 	        return "";
 
 	    StringBuffer sb = new StringBuffer();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		Locale locale = templateController.getLocale();
 		
 		timer.printElapsedTime("After locale");
 	    
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 		/*
 		String formsEncoding = CmsPropertyHandler.getFormsEncoding();
 		String acceptCharset = "";
 		if(formsEncoding != null && formsEncoding.length() > 0)
 		    acceptCharset = "accept-charset=\"" + formsEncoding + "\"";
 		*/
 		sb.append("<div id=\"component" + componentId + "Properties\" class=\"componentProperties\" style=\"right:5px; top:5px; visibility:hidden;\">");
		sb.append("	<div id=\"component" + componentId + "PropertiesHandle\" class=\"componentPropertiesHandle\"><div id=\"leftPaletteHandle\">Properties</div><div id=\"rightPaletteHandle\"><a href=\"javascript:hideDiv('component" + componentId + "Properties');\" class=\"white\"><img src=\"" + componentEditorUrl + "/images/closeIcon.gif\" border=\"0\"/></a></div></div>");
 		sb.append("	<div id=\"component" + componentId + "PropertiesBody\" class=\"componentPropertiesBody\">");
 		
 		//sb.append("	<form " + acceptCharset + " id=\"component" + componentId + "PropertiesForm\" name=\"component" + componentId + "PropertiesForm\" action=\"" + componentEditorUrl + "ViewSiteNodePageComponents!updateComponentProperties.action\" method=\"POST\">");
 		sb.append("	<form id=\"component" + componentId + "PropertiesForm\" name=\"component" + componentId + "PropertiesForm\" action=\"" + componentEditorUrl + "ViewSiteNodePageComponents!updateComponentProperties.action\" method=\"POST\">");
 		sb.append("		<table class=\"igPropertiesTable\" border=\"0\" cellpadding=\"4\" cellspacing=\"0\">");
 
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td class=\"igpropertylabel\" align=\"left\">Choose language</td>");  //$ui.getString("tool.contenttool.languageVersionsLabel")
 		sb.append("			<td class=\"igtd\">&nbsp;</td>");
 		sb.append("			<td class=\"igpropertyvalue\" align=\"left\">");
 	
 		sb.append("			");
 		sb.append("			<select class=\"mediumdrop\" name=\"languageId\" onChange=\"javascript:changeLanguage(" + siteNodeId + ", this, " + contentId + ");\">");
 		
 		List languages = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguages(getDatabase(), siteNodeId);
 
 		Iterator languageIterator = languages.iterator();
 		int index = 0;
 		int languageIndex = index;
 		while(languageIterator.hasNext())
 		{
 			LanguageVO languageVO = (LanguageVO)languageIterator.next();
 			if(languageVO.getLanguageId().intValue() == languageId.intValue())
 			{
 				sb.append("					<option class=\"iglabel\" value=\"" + languageVO.getLanguageId() + "\" selected><span class=\"iglabel\">" + languageVO.getName() + "</span></option>");
 				sb.append("					<script type=\"text/javascript\">");
 				sb.append("					</script>");
 				languageIndex = index;
 			}
 			else
 			{
 				sb.append("					<option value=\"" + languageVO.getLanguageId() + "\">" + languageVO.getName() + "</option>");
 			}
 			index++;
 		}
 		sb.append("			</select>");
 		sb.append("			<!--");
 		sb.append("				var originalIndex = " + languageIndex + ";");
 		sb.append("			-->");
 
 		sb.append("			</td>");
 		sb.append("			<td class=\"igtd\">&nbsp;</td>");
 		sb.append("		</tr>");
 
 		Collection componentProperties = getComponentProperties(componentId, document);
 
 		int propertyIndex = 0;
 		Iterator componentPropertiesIterator = componentProperties.iterator();
 		while(componentPropertiesIterator.hasNext())
 		{
 			ComponentProperty componentProperty = (ComponentProperty)componentPropertiesIterator.next();
 		
 		    InfoGluePrincipal principal = templateController.getPrincipal();
 		    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
 		    if(cmsUserName != null)
 			    principal = templateController.getPrincipal(cmsUserName);
 
 			boolean hasAccessToProperty = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentPropertyEditor.EditProperty", "" + componentContentId + "_" + componentProperty.getName());
 
 			if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.BINDING))
 			{
 				String assignUrl = "";
 				String createUrl = "";
 				 
 				if(componentProperty.getVisualizingAction() != null && !componentProperty.getVisualizingAction().equals(""))
 				{
 					assignUrl = componentEditorUrl + componentProperty.getVisualizingAction() + "?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 				}
 				else
 				{	
 					if(componentProperty.getEntityClass().equalsIgnoreCase("Content"))
 					{
 					    String allowedContentTypeIdParameters = "";
 
 					    if(componentProperty.getAllowedContentTypeNamesArray() != null && componentProperty.getAllowedContentTypeNamesArray().length > 0)
 					    {
 					        allowedContentTypeIdParameters = "&" + componentProperty.getAllowedContentTypeIdAsUrlEncodedString(templateController.getDatabase());
 					        logger.info("allowedContentTypeIdParameters:" + allowedContentTypeIdParameters);
 					    }
 					    
 						if(componentProperty.getIsMultipleBinding())
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showContentTreeForMultipleBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + allowedContentTypeIdParameters + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 						else
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showContentTree.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + allowedContentTypeIdParameters + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 					}
 					else if(componentProperty.getEntityClass().equalsIgnoreCase("SiteNode"))
 					{
 						if(componentProperty.getIsMultipleBinding())
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showStructureTreeForMultipleBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 						else
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showStructureTree.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 					}
 					else if(componentProperty.getEntityClass().equalsIgnoreCase("Category"))
 					{
 						if(componentProperty.getIsMultipleBinding())
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showCategoryTreeForMultipleBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 						else
 							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showCategoryTree.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 					}
 				}
 					
 				if(componentProperty.getCreateAction() != null && !componentProperty.getCreateAction().equals(""))
 				{
 					createUrl = componentEditorUrl + componentProperty.getCreateAction() + "?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + getTemplateController().getDeliveryContext().getShowSimple();
 				}
 				else
 				{	
 					if(componentProperty.getVisualizingAction() != null && !componentProperty.getVisualizingAction().equals(""))
 					{
 						createUrl = assignUrl;
 					}
 					else if(componentProperty.getEntityClass().equalsIgnoreCase("Content"))
 					{
 					    String allowedContentTypeIdParameters = "";
 
 					    if(componentProperty.getAllowedContentTypeNamesArray() != null && componentProperty.getAllowedContentTypeNamesArray().length > 0)
 					    {
 					        allowedContentTypeIdParameters = "&" + componentProperty.getAllowedContentTypeIdAsUrlEncodedString(templateController.getDatabase());
 					        logger.info("allowedContentTypeIdParameters:" + allowedContentTypeIdParameters);
 					    }
 
 					    String returnAddress = URLEncoder.encode("ViewSiteNodePageComponents!addComponentPropertyBinding.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=-1&entity=Content&entityId=#entityId&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&path=#path&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "", "UTF-8");
 						
 						if(componentProperty.getIsMultipleBinding())
 							createUrl = componentEditorUrl + "CreateContentWizardFinish.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + allowedContentTypeIdParameters + "&refreshAddress=" + returnAddress + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple();
 						else
 							createUrl = componentEditorUrl + "CreateContentWizardFinish.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + allowedContentTypeIdParameters + "&refreshAddress=" + returnAddress + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple();
 					}
 					else if(componentProperty.getEntityClass().equalsIgnoreCase("SiteNode"))
 					{
 						createUrl = null;
 						/*
 						if(componentProperty.getIsMultipleBinding())
 							createUrl = componentEditorUrl + "CreateSiteNodeWizard!input.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple();
 						else
 							createUrl = componentEditorUrl + "CreateSiteNodeWizard!input.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple();
 						*/
 					}
 				}
 				
 				StringBuffer helpSB = new StringBuffer();
 				helpSB.append("<div style=\"border: 1px solid black; visibility: hidden; z-index: 200000; position: absolute;\" id=\"helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "\">");
 				helpSB.append("<table class=\"igtable\" bgcolor=\"white\" width=\"200\"><tr class=\"igtr\"><td class=\"igpropertylabel\">" + (componentProperty.getDescription().equalsIgnoreCase("") ? "No description" : componentProperty.getDescription()) + "</td></tr></table>");
 				helpSB.append("</div>");
 				
 				sb.append("		<tr class=\"igtr\">");
 				sb.append("			<td class=\"igpropertylabel\" valign=\"top\" align=\"left\">" + componentProperty.getName() + "</td>");
 				sb.append("			<td class=\"igtd\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMark.gif\" onMouseOver=\"javascript:showDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\" onMouseOut=\"javascript:hideDiv('helpLayer" + componentProperty.getComponentId() + "_" + componentProperty.getName() + "');\">" + helpSB + "</td>");
 				sb.append("			<td class=\"igpropertyvalue\" align=\"left\">");
 
 				if(hasAccessToProperty)
 					sb.append("<a class=\"componentEditorLink\" href=\"javascript:window.open('" + assignUrl + "','Assign','toolbar=no,status=yes,scrollbars=yes,location=no,menubar=no,directories=no,resizable=no,width=300,height=600,left=5,top=5');\">");
 				
 				sb.append("" + (componentProperty.getValue().equalsIgnoreCase("") ? "Undefined" : componentProperty.getValue()));
 				
 				if(hasAccessToProperty)
 					sb.append("</a>");
 				
 				sb.append("</td>");
 				
 				if(componentProperty.getValue().equalsIgnoreCase("Undefined"))
 				{	
 					if(hasAccessToProperty && createUrl != null)
 						sb.append("			<td class=\"igtd\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + createUrl + "\"><img src=\"" + componentEditorUrl + "/images/createContent.gif\" border=\"0\" alt=\"Create new content to show\"></a></td>");
 					else
 						sb.append("			<td class=\"igtd\" width=\"16\">&nbsp;</td>");
 				}
 				else
 				{
 					sb.append("			<td class=\"igtd\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\"></a></td>");
 				}
 				
 				sb.append("		</tr>");
 			}
 			else if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.TEXTFIELD))
 			{
 				sb.append("		<tr class=\"igtr\">");
 				sb.append("			<td class=\"igpropertylabel\" valign=\"top\" align=\"left\">" + componentProperty.getName() + "</td>");
 				sb.append("			<td class=\"igtd\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMark.gif\" title=\"" + (componentProperty.getDescription().equalsIgnoreCase("") ? "No description" : componentProperty.getDescription()) + "\"></td>");
 				
 				if(hasAccessToProperty)
 					sb.append("			<td class=\"igpropertyvalue\" align=\"left\"><input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "\"><input type=\"text\" class=\"propertytextfield\" name=\"" + componentProperty.getName() + "\" value=\"" + componentProperty.getValue() + "\"></td>");
 				else
 					sb.append("			<td class=\"igpropertyvalue\" align=\"left\">" + componentProperty.getValue() + "</td>");
 	
 				if(hasAccessToProperty)
 					sb.append("			<td class=\"igtd\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\"></a></td>");
 				else
 					sb.append("			<td class=\"igtd\" width=\"16\"></td>");
 				
 				sb.append("			<!--<td>&nbsp;</td>-->");
 				sb.append("		</tr>");
 				
 				if(hasAccessToProperty)
 				    propertyIndex++;
 			}
 			else if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.TEXTAREA))
 			{
 				sb.append("		<tr class=\"igtr\">");
 				sb.append("			<td class=\"igpropertylabel\" valign=\"top\" align=\"left\">" + componentProperty.getName() + "</td>");
 				sb.append("			<td class=\"igtd\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMark.gif\" title=\"" + (componentProperty.getDescription().equalsIgnoreCase("") ? "No description" : componentProperty.getDescription()) + "\"></td>");
 				
 				if(hasAccessToProperty)
 					sb.append("			<td class=\"igpropertyvalue\" align=\"left\"><input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "\"><textarea class=\"propertytextarea\" name=\"" + componentProperty.getName() + "\">" + (componentProperty.getValue() == null ? "" : componentProperty.getValue()) + "</textarea></td>");
 				else
 					sb.append("			<td class=\"igpropertyvalue\" align=\"left\">" + componentProperty.getValue() + "</td>");
 	
 				if(hasAccessToProperty)
 					sb.append("			<td class=\"igtd\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\"></a></td>");
 				else
 					sb.append("			<td class=\"igtd\" width=\"16\"></td>");
 				
 				sb.append("			<!--<td>&nbsp;</td>-->");
 				sb.append("		</tr>");
 				
 				if(hasAccessToProperty)
 				    propertyIndex++;
 			}
 			else if(componentProperty.getType().equalsIgnoreCase(ComponentProperty.SELECTFIELD))
 			{
 				sb.append("		<tr class=\"igtr\">");
 				sb.append("			<td class=\"igpropertylabel\" valign=\"top\" align=\"left\">" + componentProperty.getName() + "</td>");
 				sb.append("			<td class=\"igtd\" width=\"16\"><img src=\"" + componentEditorUrl + "/images/questionMark.gif\" title=\"" + (componentProperty.getDescription().equalsIgnoreCase("") ? "No description" : componentProperty.getDescription()) + "\"></td>");
 				
 				if(hasAccessToProperty)
 				{
 					sb.append("			<td class=\"igpropertyvalue\" align=\"left\"><input type=\"hidden\" name=\"" + propertyIndex + "_propertyName\" value=\"" + componentProperty.getName() + "\"><select class=\"propertytextfield\" name=\"" + componentProperty.getName() + "\">");
 					
 					Iterator optionsIterator = componentProperty.getOptions().iterator();
 					while(optionsIterator.hasNext())
 					{
 					    ComponentPropertyOption option = (ComponentPropertyOption)optionsIterator.next();
 					    boolean isSame = componentProperty.getValue().equals(option.getValue());
 					    sb.append("<option value=\"" + option.getValue() + "\"" + (isSame ? " selected=\"1\"" : "") + ">" + option.getName() + "</option>");
 					}
 					
 				    sb.append("</td>");
 				}
 				else
 					sb.append("			<td class=\"igpropertyvalue\" align=\"left\">" + componentProperty.getValue() + "</td>");
 	
 				if(hasAccessToProperty)
 					sb.append("			<td class=\"igtd\" width=\"16\"><a class=\"componentEditorLink\" href=\"" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponentPropertyValue.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + componentProperty.getName() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\"><img src=\"" + componentEditorUrl + "/images/delete.gif\" border=\"0\"></a></td>");
 				else
 					sb.append("			<td class=\"igtd\" width=\"16\"></td>");
 				
 				sb.append("			<!--<td>&nbsp;</td>-->");
 				sb.append("		</tr>");
 				
 				if(hasAccessToProperty)
 				    propertyIndex++;
 			}
 		}
 		
 		timer.printElapsedTime("getComponentPropertiesDiv: 5");
 
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td colspan=\"4\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" height=\"5\" width=\"1\"></td>");
 		sb.append("		</tr>");
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td colspan=\"4\">");
 		sb.append("				<a href=\"javascript:submitForm('component" + componentId + "PropertiesForm');\"><img src=\"" + componentEditorUrl + "" + this.getDeliveryContext().getInfoGlueAbstractAction().getLocalizedString(this.getDeliveryContext().getSession().getLocale(), "images.contenttool.buttons.save") + "\" width=\"50\" height=\"25\" border=\"0\"></a>");
 		sb.append("				<a href=\"javascript:hideDiv('component" + componentId + "Properties');\"><img src=\"" + componentEditorUrl + "" + this.getDeliveryContext().getInfoGlueAbstractAction().getLocalizedString(this.getDeliveryContext().getSession().getLocale(), "images.contenttool.buttons.close") + "\" width=\"50\" height=\"25\" border=\"0\"></a>");
 		sb.append("			</td>");
 		sb.append("		</tr>");
 		sb.append("		</table>");
 		sb.append("		<input type=\"hidden\" name=\"repositoryId\" value=\"" + repositoryId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"siteNodeId\" value=\"" + siteNodeId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"languageId\" value=\"" + languageId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"contentId\" value=\"" + contentId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"componentId\" value=\"" + componentId + "\">");
 		sb.append("		<input type=\"hidden\" name=\"showSimple\" value=\"" + this.getTemplateController().getDeliveryContext().getShowSimple() + "\">");
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
 		
 		sb.append("     floatDiv(\"component" + componentId + "Properties\", 200, 50).flt();");
 		sb.append("	</script>");
 
 		return sb.toString();
 	}
 
 	
 	/**
 	 * This method creates a div for the components properties.
 	 */
 	
 	private String getComponentTasksDiv(Integer repositoryId, Integer siteNodeId, Integer languageId, Integer contentId, InfoGlueComponent component, org.w3c.dom.Document document, TemplateController templateController) throws Exception
 	{		
 	    InfoGluePrincipal principal = templateController.getPrincipal();
 	    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
 	    if(cmsUserName != null)
 		    principal = templateController.getPrincipal(cmsUserName);
 
 		boolean hasAccessToAccessRights = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.ChangeSlotAccess", "");
 		boolean hasAccessToAddComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.AddComponent", "" + (component.getParentComponent() == null ? component.getContentId() : component.getParentComponent().getContentId()) + "_" + component.getSlotName());
 		boolean hasAccessToDeleteComponent = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "ComponentEditor.DeleteComponent", "" + (component.getParentComponent() == null ? component.getContentId() : component.getParentComponent().getContentId()) + "_" + component.getSlotName());
 	    boolean hasSaveTemplateAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "StructureTool.SaveTemplate", "");
 
 	    if(component.getIsInherited())
 		{
 		    StringBuffer sb = new StringBuffer();
 		    sb.append("<script type=\"text/javascript\">");
 			//sb.append("<!--");
 			sb.append("hasAccessToAddComponent" + component.getSlotName() + " = " + hasAccessToAddComponent + ";");
 			sb.append("hasAccessToDeleteComponent" + component.getSlotName() + " = " + hasAccessToDeleteComponent + ";");
 			sb.append("hasAccessToAccessRights" + component.getSlotName() + " = " + hasAccessToAccessRights + ";");
 		    //sb.append("-->");
 			sb.append("</script>");
 			return sb.toString();
 		}
 	    
 	    StringBuffer sb = new StringBuffer();
 		Timer timer = new Timer();
 		timer.setActive(false);
 
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 		
 		//if(component.getIsInherited())
 		//    sb.append("<div id=\"inheritedComponent" + component.getId() + "Menu\" class=\"skin0\">");
 		//else 
 		    sb.append("<div id=\"component" + component.getId() + "Menu\" class=\"skin0\">");
 		
 		Collection componentTasks = getComponentTasks(component.getId(), document);
 
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
 		    view = view.replaceAll("\\$componentId", component.getId().toString());
 		    sb.append("<div class=\"igmenuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"executeTask('" + view + "');\">" + componentTask.getName() + "</div>");
 		}
 
 		Locale locale = templateController.getLocale();
 		String editHTML = getLocalizedString(locale, "deliver.editOnSight.editHTML");
 		String submitToPublishHTML = getLocalizedString(locale, "deliver.editOnSight.submitToPublish");
 		String addComponentHTML = getLocalizedString(locale, "deliver.editOnSight.addComponentHTML");
 		String deleteComponentHTML = getLocalizedString(locale, "deliver.editOnSight.deleteComponentHTML");
 		String propertiesHTML = getLocalizedString(locale, "deliver.editOnSight.propertiesHTML");
 		String pageComponentsHTML = getLocalizedString(locale, "deliver.editOnSight.pageComponentsHTML");
 		String viewSourceHTML = getLocalizedString(locale, "deliver.editOnSight.viewSourceHTML");
 		String componentEditorInNewWindowHTML = getLocalizedString(locale, "deliver.editOnSight.componentEditorInNewWindowHTML");
 		String savePageTemplateHTML = getLocalizedString(locale, "deliver.editOnSight.savePageTemplateHTML");
 		
 		sb.append("<div class=\"igmenuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"edit();\">" + editHTML + "</div>");
 		sb.append("<div class=\"igmenuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"submitToPublish(" + siteNodeId + ", " + repositoryId + ", '" + URLEncoder.encode(templateController.getOriginalFullURL(), "UTF-8") + "');\">" + submitToPublishHTML + "</div>");
 		if(hasAccessToAddComponent)
 			sb.append("<div class=\"igmenuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"insertComponent();\">" + addComponentHTML + "</div>");
 		if(hasAccessToDeleteComponent)
 		    sb.append("<div class=\"igmenuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"deleteComponent();\">" + deleteComponentHTML + "</div>");
 		if(hasSaveTemplateAccess)
 		    sb.append("<div class=\"igmenuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"saveComponentStructure('" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + templateController.getSiteNode(siteNodeId).getMetaInfoContentId() + "');\">" + savePageTemplateHTML + "</div>");
 
 		//sb.append("<div class=\"igmenudivider\"><img src=\" + this.getRequest().getContextPath() + "/images/dividerLine.gif\"  width=\"115\" height=\"1\"></div>");
 		sb.append("<hr/>");
 		sb.append("<div class=\"igmenuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"javascript:showComponent();\">" + propertiesHTML + "</div>");
 		sb.append("<hr/>");
 		//sb.append("<div class=\"igmenudivider\"><img src=\" + this.getRequest().getContextPath() + "/images/dividerLine.gif\"  width=\"115\" height=\"1\"></div>");
 		sb.append("<div class=\"igmenuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"javascript:toggleDiv('pageComponents');\">" + pageComponentsHTML + "</div>");
 		sb.append("<div class=\"igmenuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"window.open(document.location.href,'PageComponents','');\">" + componentEditorInNewWindowHTML + "</div>");
 		sb.append("<div class=\"igmenuitems\" onMouseover=\"javascript:highlightie5(event);\" onMouseout=\"javascript:lowlightie5(event);\" onClick=\"javascript:viewSource();\">" + viewSourceHTML + "</div>");
 		//sb.append("<div class=\"igmenuitems\">getComponentTasksDiv_" + component.getSlotName() + "</div>");
 		sb.append("</div>");
 		
 		sb.append("<script type=\"text/javascript\">");
 		//sb.append("<!--");
 		sb.append("hasAccessToAddComponent" + component.getSlotName() + " = " + hasAccessToAddComponent + ";");
 		sb.append("hasAccessToDeleteComponent" + component.getSlotName() + " = " + hasAccessToDeleteComponent + ";");
 		sb.append("hasAccessToAccessRights" + component.getSlotName() + " = " + hasAccessToAccessRights + ";");
 		//sb.append("-->");
 		sb.append("</script>");
 		
 		return sb.toString();
 	}
 
 	/**
 	 * This method creates a div for the components properties.
 	 */
 	
 	private String getPageComponentStructureDiv(TemplateController templateController, Integer siteNodeId, Integer languageId, InfoGlueComponent component) throws Exception
 	{		
 	    if(templateController.getRequestParameter("skipComponentStructure") != null && templateController.getRequestParameter("skipComponentStructure").equalsIgnoreCase("true"))
 	        return "";
 	    
 		StringBuffer sb = new StringBuffer();
 		
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 
 		sb.append("<div id=\"pageComponents\" style=\"right:5px; top:5px; visibility:hidden; display: none;\">");
 
 		sb.append("	<div id=\"dragCorner\" style=\"position: absolute; width: 16px; height: 16px; background-color: white; bottom: 0px; right: 0px;\"><a href=\"javascript:expandWindow('pageComponents');\"><img src=\"" + this.getRequest().getContextPath() + "/images/enlarge.gif\" border=\"0\" width=\"16\" height=\"16\"></a></div>");
 			
 		sb.append("		<div id=\"pageComponentsHandle\"><div id=\"leftPaletteHandle\">Page components</div><div id=\"rightPaletteHandle\"><a href=\"javascript:hideDiv('pageComponents');\" class=\"white\">close</a></div></div>");
 		sb.append("		<div id=\"pageComponentsBody\"><table class=\"igtable\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
 
 		sb.append("		<tr class=\"igtr\">");
 	    //sb.append("			<td class=\"igtd\" colspan=\"20\"><img src=\" + this.getRequest().getContextPath() + "/images/tcross.png\" width=\"19\" height=\"16\"><span id=\"" + component.getId() + "\" class=\"iglabel\"><img src=\" + this.getRequest().getContextPath() + "/images/slotIcon.gif\" width=\"16\" height=\"16\"><img src=\" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"5\" height=\"1\">" + component.getName() + "</span><script type=\"text/javascript\">initializeSlotEventHandler('" + component.getId() + "', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&parentComponentId=" + component.getId() + "&slotId=base&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "', '', 'base');</script></td>");
 	    sb.append("			<td class=\"igtd\" colspan=\"20\"><img src=\"" + this.getRequest().getContextPath() + "/images/tcross.png\" width=\"19\" height=\"16\"><span id=\"" + component.getSlotName() + "ClickableDiv\" class=\"iglabel\"><img src=\"" + this.getRequest().getContextPath() + "/images/slotIcon.gif\" width=\"16\" height=\"16\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"5\" height=\"1\">" + component.getName() + "</span><script type=\"text/javascript\">initializeSlotEventHandler('" + component.getSlotName() + "ClickableDiv', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&parentComponentId=" + component.getId() + "&slotId=base&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "', '', 'base', '" + component.getContentId() + "');</script></td>");
 		sb.append("		</tr>");
 		
 		renderComponentTree(templateController, sb, component, 0, 0, 1);
 
 		sb.append("		<tr class=\"igtr\">");
 		for(int i=0; i<20; i++)
 		{
 			sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"19\" height=\"1\"></td>");
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
 		String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
 
 		ContentVO componentContentVO = templateController.getContent(component.getContentId());
 		
 		int colspan = 20 - level;
 		
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"19\" height=\"16\"></td>");
 		
 		for(int i=0; i<level; i++)
 		{
 			sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 		}
 		
 		sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/tcross.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/componentIcon.gif\" width=\"16\" height=\"16\"></td><td class=\"igtd\" colspan=\"" + (colspan - 2) + "\"><span id=\"" + component.getId() + "\" onclick=\"javascript:showDiv('component" + component.getId() + "Properties');\" class=\"clickableLabel\">" + componentContentVO.getName() + "</span><script type=\"text/javascript\">initializeComponentInTreeEventHandler('" + component.getId() + "', '" + component.getId() + "', '', '" + componentEditorUrl + "ViewSiteNodePageComponents!deleteComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&slotId=" + component.getId() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "', '" + component.getSlotName() + "');</script>");
 		String upUrl = componentEditorUrl + "ViewSiteNodePageComponents!moveComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&direction=0&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "";
 		String downUrl = componentEditorUrl + "ViewSiteNodePageComponents!moveComponent.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&componentId=" + component.getId() + "&direction=1&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + "";
 		
 		if(position > 0)
 		    sb.append("<a href=\"" + upUrl + "\"><img src=\"" + this.getRequest().getContextPath() + "/images/upArrow.gif\" border=\"0\" width=\"11\" width=\"10\"></a>");
 		if(maxPosition > position)
 		    sb.append("<a href=\"" + downUrl + "\"><img src=\"" + this.getRequest().getContextPath() + "/images/downArrow.gif\" border=\"0\" width=\"11\" width=\"10\"></a>");
 		
 		sb.append("</td>");
 		
 		sb.append("		</tr>");
 		
 		//Properties
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"19\" height=\"1\"></td><td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 		for(int i=0; i<level; i++)
 		{
 			sb.append("<td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 		}
 		sb.append("<td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/tcross.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/propertiesIcon.gif\" width=\"16\" height=\"16\" border=\"0\"></td><td class=\"igtd\" colspan=\"" + (colspan - 3) + "\"><span onclick=\"javascript:showComponentProperties('component" + component.getId() + "Properties');\" class=\"iglabel\">Properties</span></td>");
 		sb.append("		</tr>");
 		
 		sb.append("		<tr class=\"igtr\">");
 		sb.append("			<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"19\" height=\"1\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 		for(int i=0; i<level; i++)
 		{
 			sb.append("<td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 		}
 		sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/endline.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/containerIcon.gif\" width=\"16\" height=\"16\"></td><td class=\"igtd\" colspan=\"" + (colspan - 4) + "\"><span class=\"iglabel\">Slots</span></td>");
 		sb.append("</tr>");
 
 		Iterator slotIterator = component.getSlotList().iterator();
 		while(slotIterator.hasNext())
 		{
 			Slot slot = (Slot)slotIterator.next();
 	
 			sb.append("		<tr class=\"igtr\">");
 			sb.append("			<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/trans.gif\" width=\"19\" height=\"16\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 			for(int i=0; i<level; i++)
 			{
 				sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/vline.png\" width=\"19\" height=\"16\"></td>");
 			}
 			if(slot.getComponents().size() > 0)
 				sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/tcross.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/slotIcon.gif\" width=\"16\" height=\"16\"></td>");
 			else
 				sb.append("<td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/endline.png\" width=\"19\" height=\"16\"></td><td class=\"igtd\" width=\"19\"><img src=\"" + this.getRequest().getContextPath() + "/images/slotIcon.gif\" width=\"16\" height=\"16\"></td>");
 
 		    String allowedComponentNamesAsEncodedString = slot.getAllowedComponentsArrayAsUrlEncodedString();
 
 		    sb.append("<td class=\"igtd\" colspan=\"" + (colspan - 4) + "\"><span id=\"" + slot.getId() + "ClickableDiv\" class=\"iglabel\">" + slot.getId() + "</span><script type=\"text/javascript\">initializeSlotEventHandler('" + slot.getId() + "ClickableDiv', '" + componentEditorUrl + "ViewSiteNodePageComponents!listComponents.action?siteNodeId=" + templateController.getSiteNodeId() + "&languageId=" + templateController.getLanguageId() + "&contentId=" + templateController.getContentId() + "&parentComponentId=" + component.getId() + "&slotId=" + slot.getId() + "&showSimple=" + this.getTemplateController().getDeliveryContext().getShowSimple() + ((allowedComponentNamesAsEncodedString != null) ? "&" + allowedComponentNamesAsEncodedString : "") + "', '', '" + slot.getId() + "', '" + component.getContentId() + "');</script></td>");
 			
 			sb.append("		</tr>");
 
 			List slotComponents = slot.getComponents();
 			//logger.info("Number of components in slot " + slot.getId() + ":" + slotComponents.size());
 
 			if(slotComponents != null)
 			{
 				Iterator slotComponentIterator = slotComponents.iterator();
 				int newPosition = 0;
 				while(slotComponentIterator.hasNext())
 				{
 					InfoGlueComponent slotComponent = (InfoGlueComponent)slotComponentIterator.next();
 					ContentVO componentContent = templateController.getContent(slotComponent.getContentId()); 
 					
 					String imageUrl = "" + this.getRequest().getContextPath() + "/images/componentIcon.gif";
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
 	 * This method creates the tabpanel for the component-palette.
 	 */
 	//private static String componentPaletteDiv = null;
 	
 	private String getComponentPaletteDiv(Integer siteNodeId, Integer languageId, TemplateController templateController) throws Exception
 	{		
 		InfoGluePrincipal principal = templateController.getPrincipal();
 	    String cmsUserName = (String)templateController.getHttpServletRequest().getSession().getAttribute("cmsUserName");
 	    if(cmsUserName != null)
 		    principal = templateController.getPrincipal(cmsUserName);
 
 		if(!templateController.getDeliveryContext().getShowSimple())
 	    {
 		    boolean hasAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "StructureTool.Palette", "");
 		    
 		    if(!hasAccess || templateController.getRequestParameter("skipToolbar") != null && templateController.getRequestParameter("skipToolbar").equalsIgnoreCase("true"))
 		        return "";
 	    }
 	    
 		ContentVO contentVO = templateController.getBoundContent(BasicTemplateController.META_INFO_BINDING_NAME);
 
 		//Cache
 		String key = "" + templateController.getPrincipal().getName();
 		String componentPaletteDiv = (String)CacheController.getCachedObject("componentPaletteDivCache", key);
 		if(componentPaletteDiv != null)
 		{
 			if(componentPaletteDiv != null && (templateController.getRequestParameter("refresh") == null || !templateController.getRequestParameter("refresh").equalsIgnoreCase("true")))
 			{
 				return componentPaletteDiv.replaceAll("CreatePageTemplate\\!input.action\\?contentId=.*?'", "CreatePageTemplate!input.action?contentId=" + contentVO.getContentId() + "'");
 			}
 		}
 		//End Cache
 		
 		StringBuffer sb = new StringBuffer();
 			
 		String componentEditorUrl 		= CmsPropertyHandler.getComponentEditorUrl();
 		String componentRendererUrl 	= CmsPropertyHandler.getComponentRendererUrl();
 		String componentRendererAction 	= CmsPropertyHandler.getComponentRendererAction();
 		
 		
 		sb.append("<div id=\"buffer\" style=\"top: 0px; left: 0px; z-index:200;\"><img src=\"" + this.getRequest().getContextPath() + "/images/componentDraggedIcon.gif\"></div>");
 		
 		Map componentGroups = getComponentGroups(getComponentContents(), templateController);
 		
 		sb.append("<div id=\"paletteDiv\">");
 		 
 		sb.append("<div id=\"paletteHandle\">");
 		sb.append("	<div id=\"leftPaletteHandle\">Component palette</div><div id=\"rightPaletteHandle\"><a href=\"javascript:hideDiv('paletteDiv');\" class=\"white\">close</a></div>");
 		sb.append("</div>");
 
 		sb.append("<div id=\"paletteBody\">");
 		sb.append("<table class=\"tabPanel\" cellpadding=\"0\" cellspacing=\"0\">");
 		sb.append(" <tr class=\"igtr\">");
 		
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
 		
 	    boolean hasSaveTemplateAccess = AccessRightController.getController().getIsPrincipalAuthorized(templateController.getDatabase(), principal, "StructureTool.SaveTemplate", "");
 	    
 		sb.append("  <td class=\"igpalettetd\" width=\"90%\" style=\"text-align: right; border-right: solid thin gray; border-bottom: solid thin white\" align=\"right\">&nbsp;<a href=\"javascript:refreshComponents(document.location.href);\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/refresh.gif\" alt=\"Refresh palette\" border=\"0\"></a>&nbsp;<a href=\"javascript:moveDivDown('paletteDiv');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowDown.gif\" alt=\"Move down\" border=\"0\"></a>&nbsp;<a href=\"javascript:moveDivUp('paletteDiv');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowUp.gif\" alt=\"Move up\" border=\"0\"></a>&nbsp;<a href=\"javascript:toggleDiv('pageComponents');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/pageStructure.gif\" alt=\"Toggle page structure\" border=\"0\"></a>&nbsp;");
 		if(hasSaveTemplateAccess)
 		    sb.append("<a href=\"javascript:saveComponentStructure('" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + contentVO.getId() + "');\" class=\"white\"><img src=\"" + this.getRequest().getContextPath() + "/images/saveComponentStructure.gif\" alt=\"Save the page as a template page\" border=\"0\"></a>&nbsp;");
 		
 		sb.append("<a href=\"javascript:window.open(document.location.href, 'PageComponents', '');\"><img src=\"" + this.getRequest().getContextPath() + "/images/fullscreen.gif\" alt=\"Pop up in a large window\" border=\"0\"></a>&nbsp;</td>");
 		
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
 				sb.append("<div id=\"" + groupName + "ComponentsBg\" class=\"componentsBackground\" style=\"zIndex:3; visibility: inherited;\">");
 				openGroupName = groupName;
 			}
 			else
 			    sb.append("<div id=\"" + groupName + "ComponentsBg\" class=\"componentsBackground\" style=\"zIndex:2; visibility: inherited;\">");	
 			
 			sb.append("<div id=\"" + groupName + "Components\" style=\"visibility:inherit; position:absolute; top:1px; left:5px; height:50px; \">");
 			sb.append("	<table class=\"igtable\" style=\"width:100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
 			sb.append("	<tr class=\"igtr\">");
 			//sb.append("	<td width=\"100%\"><nobr>");
 			
 			String imageUrl = this.getRequest().getContextPath() + "/images/componentIcon.gif";
 			List components = (List)componentGroups.get(groupName); //getComponentContents();
 			Iterator componentIterator = components.iterator();
 			int componentIndex = 0;
 			while(componentIterator.hasNext())
 			{
 				ContentVO componentContentVO = (ContentVO)componentIterator.next();
 	
 				//String imageUrlTemp = getDigitalAssetUrl(componentContentVO.getId(), "thumbnail");
 				//if(imageUrlTemp != null && imageUrlTemp.length() > 0)
 				//	imageUrl = imageUrlTemp;
 				sb.append("	<td class=\"igpalettetd\">");
 				sb.append("		<div id=\"" + componentIndex + "\" style=\"display: block; visibility: inherited;\"><nobr><img src=\"" + imageUrl + "\" width=\"16\" height=\"16\" border=\"0\">");
 				sb.append("		<span onMouseDown=\"grabIt(event);\" onmouseover=\"showDetails('" + componentContentVO.getName() + "');\" id=\""+ componentContentVO.getId() + "\" class=\"draggableItem\" nowrap=\"1\">" + ((componentContentVO.getName().length() > 22) ? componentContentVO.getName().substring(0, 17) : componentContentVO.getName()) + "...</span>");
 				sb.append("     </nobr></div>"); 
 				sb.append("	</td>");
 				
 				imageUrl = this.getRequest().getContextPath() + "/images/componentIcon.gif";
 			}
 			sb.append("  <td class=\"igpalettetd\" width=\"90%\">&nbsp;</td>");
 			
 			//sb.append("	</nobr></td>");
 			sb.append("	</tr>");
 			sb.append("	</table>");
 			sb.append("</div>");
 			
 			sb.append("</div>");
 			
 			sb.append("<script type=\"text/javascript\"> if (bw.bw) tabInit('" + groupName + "Components'); </script>");
 
 			
 			index++;
 		}
 		
 		sb.append("<div id=\"statusListBg\">");
 		sb.append("<table class=\"igtable\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
 		sb.append("<tr class=\"igtr\">");
 		sb.append("	<td class=\"igpalettetd\" align=\"left\" width=\"15px\">&nbsp;<a href=\"#\" onclick=\"moveLeft(currentGroup)\" return false\" onfocus=\"if(this.blur)this.blur()\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowleft.gif\" alt=\"previous\" border=\"0\"></a></td>");
 		sb.append("	<td class=\"igpalettetd\" align=\"left\" width=\"95%\"><span class=\"componentsStatusText\">Details: </span><span id=\"statusText\" class=\"componentsStatusText\">&nbsp;</span></td>");
 		sb.append("	<td class=\"igpalettetd\" align=\"right\"><a href=\"#\" onclick=\"moveRight(currentGroup)\" return false\" onfocus=\"if(this.blur)this.blur()\"><img src=\"" + this.getRequest().getContextPath() + "/images/arrowright.gif\" alt=\"next\" border=\"0\"></a>&nbsp;</td>");
 		sb.append("</tr>");
 		sb.append("</table>");
 		sb.append("</div>");
 
 		sb.append("	<script type=\"text/javascript\">");
 		sb.append("	  	changeTab('" + openGroupName + "');");
 		
 		sb.append("		var theHandle = document.getElementById(\"paletteHandle\");");
 		sb.append("		var theRoot   = document.getElementById(\"paletteDiv\");");
 		sb.append("		Drag.init(theHandle, theRoot, 0, 0, 0, 1000);");
 		
 		sb.append("	</script>");
 
 		sb.append("</div>");
 		
 		//Caching the result
 		componentPaletteDiv = sb.toString();
 		CacheController.cacheObject("componentPaletteDivCache", key, componentPaletteDiv);				
 		
 		return componentPaletteDiv;
 	}
 
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
 		
 		return ContentController.getContentController().getContentVOList(arguments, getDatabase());
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
 			//logger.info("xml: " + xml);
 			if(xml != null && xml.length() > 0)
 			{
 				componentPropertiesDocument = XMLHelper.readDocumentFromByteArray(xml.getBytes("UTF-8"));
 				
 				CacheController.cacheObject(cacheName, cacheKey, componentPropertiesDocument);
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
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
 		    Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getId();
 		    //logger.info("masterLanguageId:" + masterLanguageId);
 		    componentPropertiesString = templateController.getContentAttribute(contentId, masterLanguageId, "ComponentProperties", true);
 
 			if(componentPropertiesString == null)
 				throw new SystemException("There was no properties assigned to this content.");
 		
 			CacheController.cacheObject(cacheName, cacheKey, componentPropertiesString);
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
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
 			logger.error(e.getMessage(), e);
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
 		    Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), siteNodeId).getId();
 		    componentTasksString = templateController.getContentAttribute(contentId, masterLanguageId, "ComponentTasks", true);
 
 			if(componentTasksString == null)
 				throw new SystemException("There was no tasks assigned to this content.");
 		
 			CacheController.cacheObject(cacheName, cacheKey, componentTasksString);
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 
 		return componentTasksString;
 	}
 	
 	public Collection getComponentProperties(Integer componentId, TemplateController templateController, Integer siteNodeId, Integer languageId, Integer componentContentId) throws Exception
 	{
 		org.w3c.dom.Document componentPropertiesDocument = getComponentPropertiesDocument(templateController, siteNodeId, languageId, componentContentId);
 		return getComponentProperties(componentId, componentPropertiesDocument, templateController);
 	}
 	
 	/*
 	 * This method returns a bean representing a list of ComponentProperties that the component has.
 	 */
 	 
 	private List getComponentProperties(Integer componentId, org.w3c.dom.Document document) throws Exception
 	{
 		//TODO - hr kan vi skert cache:a.
 		
 		//logger.info("componentPropertiesXML:" + componentPropertiesXML);
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
 				//logger.info("propertyXPath:" + propertyXPath);
 				NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), propertyXPath);
 				timer.printElapsedTime("Set property xpath");
 				//logger.info("*********************************************************anl:" + anl.getLength());
 				for(int i=0; i < anl.getLength(); i++)
 				{
 					org.w3c.dom.Element binding = (org.w3c.dom.Element)anl.item(i);
 					
 					String name							 = binding.getAttribute("name");
 					String description					 = binding.getAttribute("description");
 					String type							 = binding.getAttribute("type");
 					String allowedContentTypeNamesString = binding.getAttribute("allowedContentTypeDefinitionNames");
 					String visualizingAction 			 = binding.getAttribute("visualizingAction");
 					String createAction 				 = binding.getAttribute("createAction");
 					//logger.info("name:" + name);
 					//logger.info("type:" + type);
 
 					ComponentProperty property = new ComponentProperty();
 					property.setComponentId(componentId);
 					property.setName(name);
 					property.setDescription(description);
 					property.setType(type);
 					property.setVisualizingAction(visualizingAction);
 					property.setCreateAction(createAction);
 					if(allowedContentTypeNamesString != null && allowedContentTypeNamesString.length() > 0)
 					{
 					    String[] allowedContentTypeNamesArray = allowedContentTypeNamesString.split(",");
 					    property.setAllowedContentTypeNamesArray(allowedContentTypeNamesArray);
 					}
 					
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
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.TEXTAREA))	
 					{		
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property2");
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.SELECTFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name);
 						timer.printElapsedTime("Set property2");
 						
 						NodeList optionList = binding.getElementsByTagName("option");
 						for(int j=0; j < optionList.getLength(); j++)
 						{
 							org.w3c.dom.Element option = (org.w3c.dom.Element)optionList.item(j);
 							String optionName	= option.getAttribute("name");
 							String optionValue	= option.getAttribute("value");
 							ComponentPropertyOption cpo = new ComponentPropertyOption();
 							cpo.setName(optionName);
 							cpo.setValue(optionValue);
 							property.getOptions().add(cpo);
 						}
 						
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					
 					componentProperties.add(property);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			logger.warn("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
 		}
 							
 		return componentProperties;
 	}
 
 
 	/*
 	 * This method returns a bean representing a list of ComponentProperties that the component has.
 	 */
 	 
 	private List getComponentProperties(Integer componentId, org.w3c.dom.Document document, TemplateController templateController) throws Exception
 	{
 		//TODO - hr kan vi skert cache:a.
 		
 		//logger.info("componentPropertiesXML:" + componentPropertiesXML);
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
 				//logger.info("propertyXPath:" + propertyXPath);
 				NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), propertyXPath);
 				timer.printElapsedTime("Set property xpath");
 				//logger.info("*********************************************************anl:" + anl.getLength());
 				for(int i=0; i < anl.getLength(); i++)
 				{
 					org.w3c.dom.Element binding = (org.w3c.dom.Element)anl.item(i);
 					
 					String name							 = binding.getAttribute("name");
 					String description					 = binding.getAttribute("description");
 					String type							 = binding.getAttribute("type");
 					String allowedContentTypeNamesString = binding.getAttribute("allowedContentTypeDefinitionNames");
 					String visualizingAction 			 = binding.getAttribute("visualizingAction");
 					String createAction 				 = binding.getAttribute("createAction");
 					//logger.info("name:" + name);
 					//logger.info("type:" + type);
 
 					ComponentProperty property = new ComponentProperty();
 					property.setComponentId(componentId);
 					property.setName(name);
 					property.setDescription(description);
 					property.setType(type);
 					property.setVisualizingAction(visualizingAction);
 					property.setCreateAction(createAction);
 					if(allowedContentTypeNamesString != null && allowedContentTypeNamesString.length() > 0)
 					{
 					    String[] allowedContentTypeNamesArray = allowedContentTypeNamesString.split(",");
 					    property.setAllowedContentTypeNamesArray(allowedContentTypeNamesArray);
 					}
 					
 					if(type.equalsIgnoreCase(ComponentProperty.BINDING))
 					{
 						String entity 	= binding.getAttribute("entity");
 						boolean isMultipleBinding = new Boolean(binding.getAttribute("multiple")).booleanValue();
 						
 						property.setEntityClass(entity);
 						String value = getComponentPropertyValue(componentId, name, templateController);
 						timer.printElapsedTime("Set property1");
 
 						property.setValue(value);
 						property.setIsMultipleBinding(isMultipleBinding);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.TEXTFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name, templateController);
 						timer.printElapsedTime("Set property2");
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.TEXTAREA))	
 					{		
 						String value = getComponentPropertyValue(componentId, name, templateController);
 						timer.printElapsedTime("Set property2");
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					else if(type.equalsIgnoreCase(ComponentProperty.SELECTFIELD))	
 					{		
 						String value = getComponentPropertyValue(componentId, name, templateController);
 						timer.printElapsedTime("Set property2");
 						
 						NodeList optionList = binding.getElementsByTagName("option");
 						for(int j=0; j < optionList.getLength(); j++)
 						{
 							org.w3c.dom.Element option = (org.w3c.dom.Element)optionList.item(j);
 							String optionName	= option.getAttribute("name");
 							String optionValue	= option.getAttribute("value");
 							ComponentPropertyOption cpo = new ComponentPropertyOption();
 							cpo.setName(optionName);
 							cpo.setValue(optionValue);
 							property.getOptions().add(cpo);
 						}
 						
 						//logger.info("value:" + value);
 						property.setValue(value);
 					}
 					
 					componentProperties.add(property);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			logger.warn("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
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
 				//logger.info("propertyXPath:" + propertyXPath);
 				NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), propertyXPath);
 				timer.printElapsedTime("Set property xpath");
 				for(int i=0; i < anl.getLength(); i++)
 				{
 					org.w3c.dom.Element binding = (org.w3c.dom.Element)anl.item(i);
 					
 					String name		= binding.getAttribute("name");
 					String view		= binding.getAttribute("view");
 					logger.info("name:" + name);
 					logger.info("view:" + view);
 
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
 			logger.warn("The component with id " + componentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
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
 		Integer languageId = null;
 		if(this.getRequest().getParameter("languageId") != null && this.getRequest().getParameter("languageId").length() > 0)
 		    languageId = new Integer(this.getRequest().getParameter("languageId"));
 		else
 		    languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), siteNodeId).getId();
 		        
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(getDatabase(), languageId);
 		
 		Integer contentId  = new Integer(-1);
 		if(this.getRequest().getParameter("contentId") != null && this.getRequest().getParameter("contentId").length() > 0)
 			contentId  = new Integer(this.getRequest().getParameter("contentId"));
 
 		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 		
 		org.w3c.dom.Document document = getPageComponentsDocument(getDatabase(), this.getTemplateController(), siteNodeId, languageId, contentId);
 		
 		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
 		//logger.info("componentXPath:" + componentXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		for(int i=0; i < anl.getLength(); i++)
 		{
 			org.w3c.dom.Element property = (org.w3c.dom.Element)anl.item(i);
 			
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
 	
 	private String getComponentPropertyValue(Integer componentId, String name, TemplateController templateController) throws Exception
 	{
 		String value = "Undefined";
 		
 		Timer timer = new Timer();
 		timer.setActive(false);
 				
 		Integer languageId = null;
 		if(this.getRequest() != null && this.getRequest().getParameter("languageId") != null && this.getRequest().getParameter("languageId").length() > 0)
 		    languageId = new Integer(this.getRequest().getParameter("languageId"));
 		else
 		    languageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), templateController.getSiteNodeId()).getId();
 		        
 		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
 		
 		Integer contentId  = new Integer(-1);
 		if(this.getRequest() != null && this.getRequest().getParameter("contentId") != null && this.getRequest().getParameter("contentId").length() > 0)
 			contentId  = new Integer(this.getRequest().getParameter("contentId"));
 
 		NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(templateController.getSiteNodeId(), languageId, contentId);
 		
 		org.w3c.dom.Document document = getPageComponentsDocument(templateController.getDatabase(), templateController, templateController.getSiteNodeId(), languageId, contentId);
 		
 		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
 		//logger.info("componentXPath:" + componentXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		for(int i=0; i < anl.getLength(); i++)
 		{
 			org.w3c.dom.Element property = (org.w3c.dom.Element)anl.item(i);
 			
 			String id 			= property.getAttribute("type");
 			String path 		= property.getAttribute("path");
 
 			if(property.hasAttribute("path_" + locale.getLanguage()))
 				path = property.getAttribute("path_" + locale.getLanguage());
 				
 			value 				= path;
 		}
 
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
 
 		String componentXML = getPageComponentsString(getDatabase(), this.getTemplateController(), siteNodeId, languageId, contentId);			
 		////logger.info("componentXML:" + componentXML);
 
 		org.w3c.dom.Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		String componentXPath = "//component[@id=" + componentId + "]/bindings/binding";
 		//logger.info("componentXPath:" + componentXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		for(int i=0; i < anl.getLength(); i++)
 		{
 			org.w3c.dom.Element binding = (org.w3c.dom.Element)anl.item(i);
 			//logger.info(XMLHelper.serializeDom(binding, new StringBuffer()));
 			//logger.info("YES - we read the binding properties...");		
 			
 			String id 			= binding.getAttribute("id");
 			String entityClass 	= binding.getAttribute("entity");
 			String entityId 	= binding.getAttribute("entityId");
 			//logger.info("id:" + id);
 			//logger.info("entityClass:" + entityClass);
 			//logger.info("entityId:" + entityId);
 			
 			if(entityClass.equalsIgnoreCase("Content"))
 			{
 				ContentVO contentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(new Integer(entityId), getDatabase());
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
 			    logger.info(" ");
 			
 			logger.info("  component:" + tempComponent.getName());
 			
 			Iterator slotIterator = tempComponent.getSlotList().iterator();
 			while(slotIterator.hasNext())
 			{
 				Slot slot = (Slot)slotIterator.next();
 				
 				for(int i=0; i<level; i++)
 					logger.info(" ");
 					
 				logger.info(" slot for " + tempComponent.getName() + ":" + slot.getId());
 				printComponentHierarchy(slot.getComponents(), level + 1);
 			}
 		}			
 	}
 	
   	public String getLocalizedString(Locale locale, String key) 
   	{
     	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);
 
     	return stringManager.getString(key);
   	}
 
 }
