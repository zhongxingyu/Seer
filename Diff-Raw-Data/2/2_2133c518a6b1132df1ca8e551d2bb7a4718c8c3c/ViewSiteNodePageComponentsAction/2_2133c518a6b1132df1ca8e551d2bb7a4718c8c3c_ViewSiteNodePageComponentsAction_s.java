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
 
 package org.infoglue.cms.applications.structuretool.actions;
 
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.ComponentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeStateController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.content.DigitalAssetVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.structure.SiteNodeVO;
 import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 import org.infoglue.cms.util.XMLHelper;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.controllers.kernel.impl.simple.IntegrationDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.module.propertyset.PropertySetManager;
 
 
 
 public class ViewSiteNodePageComponentsAction extends InfoGlueAbstractAction
 {
     private final static Logger logger = Logger.getLogger(ViewSiteNodePageComponentsAction.class.getName());
 
 	private static final long serialVersionUID = 1L;
 
 	public static final String CATEGORY_TREE = "showCategoryTree";
 	public static final String CATEGORY_TREE_MULTIPLE = "showCategoryTreeForMultipleBinding";
 
 	private Integer repositoryId = null;
 	private Integer siteNodeId = null;
 	private Integer languageId = null;
 	private Integer contentId = null;
 	private String assetKey = null;
 	private Integer parentComponentId = null;
 	private Integer componentId = null;
 	private Integer newComponentContentId = null;
 	private String propertyName = null;
 	private String path 		= null;
 	private String slotId		= null;
 	private String specifyBaseTemplate = null;
 	private String url			= null;
 	private Integer direction 	= null;
 	private boolean showSimple 	= false;
 	private Integer pageTemplateContentId;
 	private String showDecorated = "true";
 	
 	LanguageVO masterLanguageVO = null;
 	
 	private List repositories 				 = null;
 	private String currentAction 		 	 = null;
 	private Integer filterRepositoryId 		 = null; 
 	private String sortProperty 			 = "name";
 	private String[] allowedContentTypeIds	 = null;
 	private String[] allowedComponentNames 	 = null;
 	private String[] disallowedComponentNames= null;
 	
 	public ViewSiteNodePageComponentsAction()
 	{
 	}
 
 	private void initialize() throws Exception
 	{
 		SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getACLatestActiveSiteNodeVersionVO(this.getInfoGluePrincipal(), this.siteNodeId);
 		logger.info("siteNodeVersionVO:" + siteNodeVersionVO.getId() + ":" + siteNodeVersionVO.getIsActive());
 		if(siteNodeVersionVO.getStateId().intValue() != SiteNodeVersionVO.WORKING_STATE.intValue())
 		{
 	    	List events = new ArrayList();
 			SiteNodeStateController.getController().changeState(siteNodeVersionVO.getId(), SiteNodeVersionVO.WORKING_STATE, "Edit on sight editing", true, this.getInfoGluePrincipal(), this.siteNodeId, events);
 		}
 		
 		Integer currentRepositoryId = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeId).getRepositoryId();
 		this.masterLanguageVO = LanguageController.getController().getMasterLanguage(currentRepositoryId);		
 		if(filterRepositoryId == null)
 		{
 			Map args = new HashMap();
 		    args.put("globalKey", "infoglue");
 		    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
 
 		    String defaultTemplateRepository = ps.getString("repository_" + currentRepositoryId + "_defaultTemplateRepository");
 		    if(defaultTemplateRepository != null && !defaultTemplateRepository.equals(""))
 		        filterRepositoryId = new Integer(defaultTemplateRepository);
 		    else
 		        filterRepositoryId = currentRepositoryId;
 		}
 	}
 
 	/**
 	 * This method initializes the tree
 	 */
 	
 	private void initializeTreeView(String currentAction) throws Exception
 	{
 		this.currentAction = currentAction;
 		
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
 		
 		if(this.repositoryId == null)
 			this.repositoryId = RepositoryController.getController().getFirstRepositoryVO().getRepositoryId();
 	}
 
 	    
 	/**
 	 * This method which is the default one only serves to show a list 
 	 * of tasks to the user so he/she can select one to run. 
 	 */
     
 	public String doExecute() throws Exception
 	{
 		initialize();
 		return "success";
 	}
 
 
 	/**
 	 * This method shows the user a list of Components(HTML Templates). 
 	 */
     
 	public String doListComponents() throws Exception
 	{
 		logger.info("queryString:" + this.getRequest().getQueryString());
 		initialize();
 
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
 
 		return "listComponents";
 	}
 
 	/**
 	 * This method shows the user a list of Components(HTML Templates). 
 	 */
     
 	public String doListComponentsForChange() throws Exception
 	{
 		logger.info("queryString:" + this.getRequest().getQueryString());
 		initialize();
 
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
 
 		return "listComponentsForChange";
 	}
 
 	/**
 	 * This method shows the user a list of Components(HTML Templates). 
 	 */
     
 	public String doListComponentsForPalette() throws Exception
 	{
 		initialize();
 		return "listComponentsForPalette";
 	}
 	
 	/**
 	 * This method shows the user a list of Contents. 
 	 */
     
 	public String doShowContentTree() throws Exception
 	{
 		initialize();
 		initializeTreeView("ViewSiteNodePageComponents!showContentTree.action");
 		return "showContentTree";
 	}
 
 	/**
 	 * This method shows the user a interface to choose multiple contents. 
 	 */
     
 	public String doShowContentTreeForMultipleBinding() throws Exception
 	{
 		initialize();
 		initializeTreeView("ViewSiteNodePageComponents!showContentTreeForMultipleBinding.action");
 		return "showContentTreeForMultipleBinding";
 	}
 
 	/**
 	 * This method shows the user a list of SiteNodes. 
 	 */
     
 	public String doShowStructureTree() throws Exception
 	{
 		initialize();
 		initializeTreeView("ViewSiteNodePageComponents!showStructureTree.action");
 		return "showStructureTree";
 	}
 	
 	/**
 	 * This method shows the user a interface to choose multiple sitenodes. 
 	 */
     
 	public String doShowStructureTreeForMultipleBinding() throws Exception
 	{
 		initialize();
 		initializeTreeView("ViewSiteNodePageComponents!showStructureTreeForMultipleBinding.action");
 		return "showStructureTreeForMultipleBinding";
 	}
 	
 	/**
 	 * This method shows the user a list of Categories.
 	 */
 	public String doShowCategoryTree() throws Exception
 	{
 		initialize();
 		initializeTreeView("ViewSiteNodePageComponents!showCategoryTree.action");
 		return CATEGORY_TREE;
 	}
 
 	/**
 	 * This method shows the user a list of Categories to chose multiple.
 	 */
 	public String doShowCategoryTreeForMultipleBinding() throws Exception
 	{
 		initialize();
 		initializeTreeView("ViewSiteNodePageComponents!showCategoryTreeForMultipleBinding.action");
 		return CATEGORY_TREE_MULTIPLE;
 	}
 
 
 	public List getRepositories()
 	{
 		return this.repositories;
 	}
 
 	public String getCurrentAction()
 	{
 		return this.currentAction;
 	}
 
 	public String getContentAttribute(Integer contentId, String attributeName) throws Exception
 	{
 	    String attribute = "Undefined";
 	    
 	    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
 		
 		LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId).getRepositoryId());
 		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguageVO.getId());
 
 		attribute = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, attributeName, false);
 		
 		return attribute;
 	}	
 	
 	
 	/**
 	 * This method adds a page template to a sitenode. 
 	 */
     
 	public String doAddPageTemplate() throws Exception
 	{
 		logger.info("************************************************************");
 		logger.info("* ADDING PAGE TEMPLATE                                     *");
 		logger.info("************************************************************");
 		logger.info("siteNodeId:" + this.siteNodeId);
 		logger.info("languageId:" + this.languageId);
 		logger.info("contentId:" + this.contentId);
 		logger.info("pageTemplateContentId:" + this.pageTemplateContentId);
 		logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
 
 		initialize();
 
 		Integer newComponentId = new Integer(0);
 
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 		
 		if(this.pageTemplateContentId != null)
 		{
 		    Integer languageId = LanguageController.getController().getMasterLanguage(this.repositoryId).getId();
 			ContentVersionVO pageTemplateContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.pageTemplateContentId, languageId);
 			
 		    String componentXML = ContentVersionController.getContentVersionController().getAttributeValue(pageTemplateContentVersionVO.getId(), "ComponentStructure", false);
 		
 			ContentVO pageMetaInfoContentVO = nodeDeliveryController.getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
 			//ContentVO templateContentVO = nodeDeliveryController.getBoundContent(siteNodeId, "Meta information");		
 			
 			//logger.info("templateContentVO:" + templateContentVO);
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(pageMetaInfoContentVO.getId(), languageId);
 			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", componentXML, new InfoGluePrincipal("ComponentEditor", "none", "none", "none", new ArrayList(), new ArrayList(), true, null));
 		}
 		
 		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + newComponentId + "&showSimple=" + this.showSimple;
 		//this.getResponse().sendRedirect(url);		
 		
 		this.url = this.getResponse().encodeURL(url);
 		this.getResponse().sendRedirect(url);
 	    return NONE; 
 	}
 
 	/**
 	 * This method adds a component to the page. 
 	 */
     
 	public String doAddComponent() throws Exception
 	{
 		logger.info("************************************************************");
 		logger.info("* ADDING COMPONENT                                         *");
 		logger.info("************************************************************");
 		logger.info("siteNodeId:" + this.siteNodeId);
 		logger.info("languageId:" + this.languageId);
 		logger.info("contentId:" + this.contentId);
 		logger.info("queryString:" + this.getRequest().getQueryString());
 		logger.info("parentComponentId:" + this.parentComponentId);
 		logger.info("componentId:" + this.componentId);
 		logger.info("slotId:" + this.slotId);
 		logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
 
 		initialize();
 
 		logger.info("masterLanguageId:" + this.masterLanguageVO.getId());
 
 		Integer newComponentId = new Integer(0);
 
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 		
 		if(this.specifyBaseTemplate.equalsIgnoreCase("true"))
 		{
 			String componentXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><components><component contentId=\"" + componentId + "\" id=\"" + newComponentId + "\" name=\"base\"><properties></properties><bindings></bindings><components></components></component></components>";
 			ContentVO templateContentVO = nodeDeliveryController.getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
 			
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(templateContentVO.getId(), this.masterLanguageVO.getId());
 			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", componentXML, new InfoGluePrincipal("ComponentEditor", "none", "none", "none", new ArrayList(), new ArrayList(), true, null));
 		}
 		else
 		{
 		    String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId(), contentId);			
 	
 			Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 			String componentXPath = "//component[@id=" + this.parentComponentId + "]/components";
 
 			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 			if(anl.getLength() > 0)
 			{
 				Element component = (Element)anl.item(0);
 				
 				String componentsXPath = "//component";
 				NodeList nodes = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentsXPath);
 				for(int i=0; i < nodes.getLength(); i++)
 				{
 					Element element = (Element)nodes.item(i);
 					if(new Integer(element.getAttribute("id")).intValue() > newComponentId.intValue())
 						newComponentId = new Integer(element.getAttribute("id"));
 				}
 				newComponentId = new Integer(newComponentId.intValue() + 1);
 				
 				Element newComponent = addComponentElement(component, new Integer(newComponentId.intValue()), this.slotId, this.componentId);
 				String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
 
 				ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.masterLanguageVO.getId(), contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, this.masterLanguageVO.getId(), true, "Meta information", DeliveryContext.getDeliveryContext());
 				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
 				
 				ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
 			}
 		}
 		
 		logger.info("newComponentId:" + newComponentId);
 		
 		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + newComponentId + "&showSimple=" + this.showSimple;
 		//this.getResponse().sendRedirect(url);		
 		
 		this.url = this.getResponse().encodeURL(url);
 		this.getResponse().sendRedirect(url);
 	    return NONE; 
 	}
 
 
 	/**
 	 * This method moves the component up a step if possible within the same slot. 
 	 */
     
 	public String doMoveComponent() throws Exception
 	{
 		initialize();
 			
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 		
 		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId(), contentId);			
 		//logger.info("componentXML:" + componentXML);
 		
 		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		String componentXPath = "//component[@id=" + this.componentId + "]";
 	
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		if(anl.getLength() > 0)
 		{
 			Element component = (Element)anl.item(0);
 			String name = component.getAttribute("name");
 			//logger.info(XMLHelper.serializeDom(component, new StringBuffer()));
 			Node parentNode = component.getParentNode();
 			
 			boolean hasChanged = false;
 			
 			if(this.direction.intValue() == 0) //Up
 			{
 			    Node previousNode = component.getPreviousSibling();
 		        
 			    while(previousNode != null && previousNode.getNodeType() != Node.ELEMENT_NODE)
 		        {
 				    previousNode = previousNode.getPreviousSibling();
 		        	break;
 		        }
 			    
 			    Element element = ((Element)previousNode);
 				while(element != null && !element.getAttribute("name").equalsIgnoreCase(name))
 			    {
 			        previousNode = previousNode.getPreviousSibling();
 					element = ((Element)previousNode);
 			    }
 				
 				if(previousNode != null)
 				{
 					parentNode.removeChild(component);
 				    parentNode.insertBefore(component, previousNode);
 				    hasChanged = true;
 				}
 			}
 			else if(this.direction.intValue() == 1) //Down
 			{
 			    Node nextNode = component.getNextSibling();
 			    
 		        while(nextNode != null && nextNode.getNodeType() != Node.ELEMENT_NODE)
 		        {
 		        	nextNode = nextNode.getNextSibling();
 		        	break;
 		        }
 			    
 			    Element element = ((Element)nextNode);
 				while(element != null && !element.getAttribute("name").equalsIgnoreCase(name))
 			    {
 				    nextNode = nextNode.getNextSibling();
 					element = ((Element)nextNode);
 			    }
 				
 				if(nextNode != null)
 				    nextNode = nextNode.getNextSibling();
 				
 				if(nextNode != null)
 				{
 					parentNode.removeChild(component);
 				    parentNode.insertBefore(component, nextNode);
 				    hasChanged = true;
 				}
 				else
 				{
 				    parentNode.removeChild(component);
 				    parentNode.appendChild(component);
 				    hasChanged = true;
 				}
 			}		
 			
 			if(hasChanged)
 			{
 				String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
 				//logger.info("modifiedXML:" + modifiedXML);
 				
 				ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, this.masterLanguageVO.getId(), true, "Meta information", DeliveryContext.getDeliveryContext());
 				ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
 				ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
 			}
 		}
 				
 		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&showSimple=" + this.showSimple;
 		//this.getResponse().sendRedirect(url);		
 		
 		this.url = this.getResponse().encodeURL(url);
 		this.getResponse().sendRedirect(url);
 	    return NONE; 
 	}
 	
 
 	
 	/**
 	 * This method updates the given properties with new values. 
 	 */
     
 	public String doUpdateComponentProperties() throws Exception
 	{
 		initialize();
 
 		if(logger.isInfoEnabled())
 		{
 			logger.info("************************************************************");
 			logger.info("* doUpdateComponentProperties                              *");
 			logger.info("************************************************************");
 			logger.info("siteNodeId:" + this.siteNodeId);
 			logger.info("languageId:" + this.languageId);
 			logger.info("contentId:" + this.contentId);
 			logger.info("componentId:" + this.componentId);
 			logger.info("slotId:" + this.slotId);
 			logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
 		}
 		
 		Iterator parameterNames = this.getRequest().getParameterMap().keySet().iterator();
 		while(parameterNames.hasNext())
 		{
 			String name = (String)parameterNames.next();
 			String value = (String)this.getRequest().getParameter(name);
 			logger.info(name + "=" + value);
 		}
 
 		Integer siteNodeId 	= new Integer(this.getRequest().getParameter("siteNodeId"));
 		Integer languageId 	= new Integer(this.getRequest().getParameter("languageId"));
 		
 		Locale locale = LanguageController.getController().getLocaleWithId(languageId);
 		
 		String entity  		= this.getRequest().getParameter("entity");
 		
 		String componentXML = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId(), contentId);			
 		//logger.info("componentXML:" + componentXML);
 		
 		ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
 		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
 
 		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		
 		String characterEncoding= this.getRequest().getCharacterEncoding();
 		characterEncoding= this.getResponse().getCharacterEncoding();
 	
 		logger.info("siteNodeId:" + siteNodeId);
 		logger.info("languageId:" + languageId);
 		logger.info("entity:" + entity);
 		
 		int propertyIndex = 0;	
 		String propertyName = this.getRequest().getParameter(propertyIndex + "_propertyName");
 		while(propertyName != null && !propertyName.equals(""))
 		{
 			String[] propertyValues = this.getRequest().getParameterValues(propertyName);
 			String propertyValue = "";
 			
 			if(propertyValues != null && propertyValues.length == 1)
 				propertyValue = propertyValues[0];
 			else if(propertyValues != null)
 			{
 				StringBuffer sb = new StringBuffer();
 				for(int i=0; i<propertyValues.length;i++)
 				{
 					if(i > 0)
 						sb.append(",");
 					sb.append(propertyValues[i]);
 				}
 				propertyValue = sb.toString();
 			}
 			
 			logger.info("propertyName:" + propertyName);
 			logger.info("propertyValue:" + propertyValue);
 			if(propertyValue != null && !propertyValue.equals("") && !propertyValue.equalsIgnoreCase("undefined"))
 			{
 				String componentPropertyXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']";
 				//logger.info("componentPropertyXPath:" + componentPropertyXPath);
 				NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
 				if(anl.getLength() == 0)
 				{
 					String componentXPath = "//component[@id=" + this.componentId + "]/properties";
 					//logger.info("componentXPath:" + componentXPath);
 					NodeList componentNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 					if(componentNodeList.getLength() > 0)
 					{
 						Element componentProperties = (Element)componentNodeList.item(0);
 						addPropertyElement(componentProperties, propertyName, propertyValue, "textfield", locale);
 						anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
 					}
 				}
 				
 				logger.info("anl:" + anl);
 				if(anl.getLength() > 0)
 				{
 					Element component = (Element)anl.item(0);
 					component.setAttribute("path_" + locale.getLanguage(), propertyValue);
 				    logger.info("Setting 'path_" + locale.getLanguage() + ":" + propertyValue);
 				}
 				else
 				{
 				    logger.warn("No property could be updated... must be wrong.");
 				}
 			}
 			
 			propertyIndex++;
 			
 			propertyName = this.getRequest().getParameter(propertyIndex + "_propertyName");
 		}
 
 		String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
 			
 		logger.info("contentVersionVO:" + contentVersionVO.getContentVersionId());
 		ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
 		
 		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple;
 		//this.getResponse().sendRedirect(url);		
 		
 		this.url = this.getResponse().encodeURL(url);
 		this.getResponse().sendRedirect(url);
 	    return NONE; 
 	}
 
 
 	/**
 	 * This method shows the user a list of Components(HTML Templates). 
 	 */
     
 	public String doDeleteComponent() throws Exception
 	{
 		initialize();
 		//logger.info("************************************************************");
 		//logger.info("* DELETING COMPONENT                                         *");
 		//logger.info("************************************************************");
 		//logger.info("siteNodeId:" + this.siteNodeId);
 		//logger.info("languageId:" + this.languageId);
 		//logger.info("contentId:" + this.contentId);
 		//logger.info("componentId:" + this.componentId);
 		//logger.info("slotId:" + this.slotId);
 		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
 				
 		logger.info("doDeleteComponent:" + this.getRequest().getQueryString());
 		
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 		
 		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId(), contentId);			
 
 		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		String componentXPath = "//component[@id=" + this.componentId + "]";
 		//logger.info("componentXPath:" + componentXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		//logger.info("anl:" + anl.getLength());
 		if(anl.getLength() > 0)
 		{
 			Element component = (Element)anl.item(0);
 			component.getParentNode().removeChild(component);
 			
 			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
 			
 			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
 
 			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
 		}
 		
 		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&showSimple=" + this.showSimple;
 		//this.getResponse().sendRedirect(url);		
 		
 		this.url = this.getResponse().encodeURL(url);
 		this.getResponse().sendRedirect(url);
 	    return NONE; 
 	}
 	
 	/**
 	 * This method shows the user a list of Components(HTML Templates). 
 	 */
     
 	public String doChangeComponent() throws Exception
 	{
 		initialize();
 		//logger.info("************************************************************");
 		//logger.info("* DELETING COMPONENT                                         *");
 		//logger.info("************************************************************");
 		//logger.info("siteNodeId:" + this.siteNodeId);
 		//logger.info("languageId:" + this.languageId);
 		//logger.info("contentId:" + this.contentId);
 		//logger.info("componentId:" + this.componentId);
 		//logger.info("slotId:" + this.slotId);
 		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
 				
 		logger.info("doChangeComponent:" + this.getRequest().getQueryString());
 		
 		logger.info("masterLanguageId:" + this.masterLanguageVO.getId());
 
 		Integer newComponentId = new Integer(0);
 
 		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId(), contentId);			
 		logger.info("componentXML:" + componentXML);
 		
 		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		String componentXPath = "//component[@id=" + this.componentId + "]";
 		
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		if(anl.getLength() > 0 && this.newComponentContentId != null)
 		{
 			Element component = (Element)anl.item(0);
 			
 			ContentVersionVO newComponentContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.newComponentContentId, this.masterLanguageVO.getId());
 			String template = ContentVersionController.getContentVersionController().getAttributeValue(newComponentContentVersionVO, "Template", false);
 			logger.info("template:" + template);
 			
 			String subComponentsXPath = "//component[@id=" + this.componentId + "]//component";
 			NodeList subComponents = org.apache.xpath.XPathAPI.selectNodeList(component, subComponentsXPath);
 			logger.info("subComponents:" + subComponents.getLength());
 			for(int i=0; i<subComponents.getLength(); i++)
 			{
 				Element subComponent = (Element)subComponents.item(i);
 				String slotId = subComponent.getAttribute("name");
 				logger.info("subComponent slotId:" + slotId);	
 				if(template.indexOf("id=\"" + slotId + "\"") == -1)
 				{
 					logger.info("deleting subComponent as it was not part of the new template");
 					Node parentNode = subComponent.getParentNode();
 					parentNode.removeChild(subComponent);
 				}	
 			}
 			
 			component.setAttribute("contentId", "" + this.newComponentContentId);
 			
 			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
 			logger.info("modifiedXML:" + modifiedXML);
 			
 			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.masterLanguageVO.getId(), contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, this.masterLanguageVO.getId(), true, "Meta information", DeliveryContext.getDeliveryContext());
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
 			
 			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
 		}
 		
 		logger.info("newComponentId:" + newComponentId);
 		
 		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + newComponentId + "&showSimple=" + this.showSimple;
 		//this.getResponse().sendRedirect(url);		
 		
 		this.url = this.getResponse().encodeURL(url);
 		this.getResponse().sendRedirect(url);
 		
 	    return NONE; 
 	}
 
 	
 	/**
 	 * This method shows the user a list of Components(HTML Templates). 
 	 */
     
 	public String doAddComponentPropertyBinding() throws Exception
 	{
 		initialize();
 		//logger.info("************************************************************");
 		//logger.info("* doAddComponentPropertyBinding                            *");
 		//logger.info("************************************************************");
 		//logger.info("siteNodeId:" + this.siteNodeId);
 		//logger.info("languageId:" + this.languageId);
 		//logger.info("contentId:" + this.contentId);
 		//logger.info("componentId:" + this.componentId);
 		//logger.info("slotId:" + this.slotId);
 		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
 		//logger.info("assetKey:" + assetKey);
 		
 		Integer siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
 		Integer languageId = new Integer(this.getRequest().getParameter("languageId"));
 		
 		Locale locale = LanguageController.getController().getLocaleWithId(languageId);
 		
 		String entity = this.getRequest().getParameter("entity");
 		Integer entityId  = new Integer(this.getRequest().getParameter("entityId"));
 		String propertyName = this.getRequest().getParameter("propertyName");
 			
 		String componentXML = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId(), contentId);			
 
 		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		String componentPropertyXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']";
 		//logger.info("componentPropertyXPath:" + componentPropertyXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
 		if(anl.getLength() == 0)
 		{
 			String componentXPath = "//component[@id=" + this.componentId + "]/properties";
 			//logger.info("componentXPath:" + componentXPath);
 			NodeList componentNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 			if(componentNodeList.getLength() > 0)
 			{
 				Element componentProperties = (Element)componentNodeList.item(0);
 				if(entity.equalsIgnoreCase("SiteNode"))
 				    addPropertyElement(componentProperties, propertyName, path, "siteNodeBinding", locale);
 				else
 				    addPropertyElement(componentProperties, propertyName, path, "contentBinding", locale);
 				
 				anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
 			}
 		}
 		
 		//logger.info("anl:" + anl);
 		if(anl.getLength() > 0)
 		{
 			Element component = (Element)anl.item(0);
 			component.setAttribute("path", path);
 			//component.setAttribute("path_" + locale.getLanguage(), path);
 			NamedNodeMap attributes = component.getAttributes();
 			logger.debug("NumberOfAttributes:" + attributes.getLength() + ":" + attributes);
 			
 			StringBuffer sb = new StringBuffer();
 			XMLHelper.serializeDom(component, sb);
 			logger.debug("SB:" + sb.toString());
 			
 			List removableAttributes = new ArrayList();
 			for(int i=0; i<attributes.getLength(); i++)
 			{
 				Node node = attributes.item(i);
 				logger.debug("Node:" + node.getNodeName());
 				if(node.getNodeName().startsWith("path_"))
 				{
 					removableAttributes.add("" + node.getNodeName());
 				}
 			}
 			
 			Iterator removableAttributesIterator = removableAttributes.iterator();
 			while(removableAttributesIterator.hasNext())
 			{
 				String attributeName = (String)removableAttributesIterator.next();
 				logger.debug("Removing node:" + attributeName);
 				component.removeAttribute(attributeName);
 			}
 			
 			NodeList children = component.getChildNodes();
 			for(int i=0; i < children.getLength(); i++)
 			{
 				Node node = children.item(i);
 				component.removeChild(node);
 			}
 			
 			Element newComponent = addBindingElement(component, entity, entityId, assetKey);
 			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
 			//logger.info("modifiedXML:" + modifiedXML);
 			
 			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
 
 			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
 		}
 					
		if(showDecorated == null || showDecorated.equalsIgnoreCase("true"))
 			this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple;
 		else
 			this.url = getComponentRendererUrl() + "ViewPage.action?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple;
 		
 		this.url = this.getResponse().encodeURL(url);
 		this.getResponse().sendRedirect(url);
 	    return NONE; 
 	}
 
 
 	/**
 	 * This method shows the user a list of Components(HTML Templates). 
 	 */
     
 	public String doAddComponentPropertyBindingWithQualifyer() throws Exception
 	{
 		initialize();
 		//logger.info("************************************************************");
 		//logger.info("* doAddComponentPropertyBindingWithQualifyer               *");
 		//logger.info("************************************************************");
 		//logger.info("siteNodeId:" + this.siteNodeId);
 		//logger.info("languageId:" + this.languageId);
 		//logger.info("contentId:" + this.contentId);
 		//logger.info("componentId:" + this.componentId);
 		//logger.info("slotId:" + this.slotId);
 		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
 		
 		Integer siteNodeId 	= new Integer(this.getRequest().getParameter("siteNodeId"));
 		Integer languageId 	= new Integer(this.getRequest().getParameter("languageId"));
 		Integer contentId 	= new Integer(this.getRequest().getParameter("contentId"));
 		
 		Locale locale = LanguageController.getController().getLocaleWithId(languageId);
 
 		String qualifyerXML = this.getRequest().getParameter("qualifyerXML");
 		String propertyName = this.getRequest().getParameter("propertyName");
 		
 		//logger.info("siteNodeId:" + siteNodeId);
 		//logger.info("languageId:" + languageId);
 		//logger.info("contentId:" + contentId);
 		//logger.info("qualifyerXML:" + qualifyerXML);
 		//logger.info("propertyName:" + propertyName);
 			
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 		
 		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId(), contentId);			
 		//logger.info("componentXML:" + componentXML);
 
 		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		String componentPropertyXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']";
 		//logger.info("componentPropertyXPath:" + componentPropertyXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
 		if(anl.getLength() > 0)
 		{
 			Node propertyNode = anl.item(0);
 			propertyNode.getParentNode().removeChild(propertyNode);
 		}
 
 		String componentXPath = "//component[@id=" + this.componentId + "]/properties";
 		//logger.info("componentXPath:" + componentXPath);
 		NodeList componentNodeList = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		if(componentNodeList.getLength() > 0)
 		{
 			Element componentProperties = (Element)componentNodeList.item(0);
 			addPropertyElement(componentProperties, propertyName, path, "contentBinding", locale);
 			anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
 		}
 		//}
 		
 		if(anl.getLength() > 0)
 		{
 			Element component = (Element)anl.item(0);
 			component.setAttribute("path", path);
 			component.setAttribute("path_" + locale.getLanguage(), path);
 			
 			addBindingElement(component, qualifyerXML);
 			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
 			
 			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
 
 			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
 		}
 					
 		if(showDecorated == null || showDecorated.equalsIgnoreCase("true"))
 			this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple;
 		else
 			this.url = getComponentRendererUrl() + "ViewPage.action?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple;
 		
 		this.url = this.getResponse().encodeURL(url);
 		this.getResponse().sendRedirect(url);
 	    return NONE; 
 	}
 	
 	/**
 	 * This method shows the user a list of Components(HTML Templates). 
 	 */
     
 	public String doDeleteComponentBinding() throws Exception
 	{
 		initialize();
 		//logger.info("************************************************************");
 		//logger.info("* doDeleteComponentBinding               *");
 		//logger.info("************************************************************");
 		//logger.info("siteNodeId:" + this.siteNodeId);
 		//logger.info("languageId:" + this.languageId);
 		//logger.info("contentId:" + this.contentId);
 		//logger.info("componentId:" + this.componentId);
 		//logger.info("slotId:" + this.slotId);
 		//logger.info("specifyBaseTemplate:" + this.specifyBaseTemplate);
 
 		Integer siteNodeId 	= new Integer(this.getRequest().getParameter("siteNodeId"));
 		Integer languageId 	= new Integer(this.getRequest().getParameter("languageId"));
 		Integer contentId  	= new Integer(this.getRequest().getParameter("contentId"));
 		Integer bindingId  	= new Integer(this.getRequest().getParameter("bindingId"));
 		
 		//logger.info("siteNodeId:" + siteNodeId);
 		//logger.info("languageId:" + languageId);
 		//logger.info("contentId:" + contentId);
 			
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 		
 		//String templateString = getPageTemplateString(templateController, siteNodeId, languageId, contentId); 
 		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId(), contentId);			
 		//logger.info("componentXML:" + componentXML);
 
 		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		String componentXPath = "//component[@id=" + this.componentId + "]/bindings/binding[@id=" + bindingId + "]";
 		//logger.info("componentXPath:" + componentXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 		//logger.info("anl:" + anl.getLength());
 		if(anl.getLength() > 0)
 		{
 			Element component = (Element)anl.item(0);
 			component.getParentNode().removeChild(component);
 			//logger.info(XMLHelper.serializeDom(component, new StringBuffer()));
 			String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
 			//logger.info("modifiedXML:" + modifiedXML);
 			
 			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
 
 			ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
 		}
 			
 		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple;
 		//this.getResponse().sendRedirect(url);		
 		
 		this.url = this.getResponse().encodeURL(url);
 		//this.getResponse().sendRedirect(url);
 	    return NONE; 
 	}
 		    
 		    
 	/**
 	 * This method shows the user a list of Components(HTML Templates). 
 	 */
     
 	public List getComponentBindings() throws Exception
 	{
 		List bindings = new ArrayList();
 			
 		try
 		{
 			Integer siteNodeId = new Integer(this.getRequest().getParameter("siteNodeId"));
 			Integer languageId = new Integer(this.getRequest().getParameter("languageId"));
 			Integer contentId  = new Integer(this.getRequest().getParameter("contentId"));
 			String propertyName = this.getRequest().getParameter("propertyName");
 	
 			//logger.info("**********************************************************************************");
 			//logger.info("siteNodeId:" + siteNodeId);
 			//logger.info("languageId:" + languageId);
 			//logger.info("contentId:" + contentId);
 			//logger.info("**********************************************************************************");
 				
 			NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 			
 			//String templateString = getPageTemplateString(templateController, siteNodeId, languageId, contentId); 
 			String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId(), contentId);			
 			//logger.info("componentXML:" + componentXML);
 	
 			Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 			String componentXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']/binding";
 			//logger.info("componentXPath:" + componentXPath);
 			NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
 			//logger.info("anl:" + anl.getLength());
 			for(int i=0; i<anl.getLength(); i++)
 			{
 				Element component = (Element)anl.item(i);
 				String entityName = component.getAttribute("entity");
 				String entityId = component.getAttribute("entityId");
 				String assetKey = component.getAttribute("assetKey");
 				
 				try
 				{
 					String path = "Undefined";
 					if(entityName.equalsIgnoreCase("SiteNode"))
 					{
 						SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(entityId));
 						path = siteNodeVO.getName();
 					}
 					else if(entityName.equalsIgnoreCase("Content")) 
 					{
 						ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(entityId));
 						path = contentVO.getName();
 					}
 					
 					Map binding = new HashMap();
 					binding.put("entityName", entityName);
 					binding.put("entityId", entityId);
 					binding.put("assetKey", assetKey);
 					binding.put("path", path);
 					bindings.add(binding);
 				}
 				catch(Exception e) 
 				{
 				    logger.warn("There was " + entityName + " bound to property '" + propertyName + "' on siteNode " + siteNodeId + " which appears to have been deleted.");
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return bindings;
 	}
 			    
 	//Nice code
 	
 	/**
 	 * This method deletes a component property value. This is to enable users to quickly remove a property value no matter what type.
 	 */
     
 	public String doDeleteComponentPropertyValue() throws Exception
 	{
 		initialize();
 	
 		Integer siteNodeId 	= new Integer(this.getRequest().getParameter("siteNodeId"));
 		Integer languageId 	= new Integer(this.getRequest().getParameter("languageId"));
 		Integer contentId  	= new Integer(this.getRequest().getParameter("contentId"));
 		String propertyName	= this.getRequest().getParameter("propertyName");
 		
 		Locale locale = LanguageController.getController().getLocaleWithId(languageId);
 
 		//logger.info("siteNodeId:" + siteNodeId);
 		//logger.info("languageId:" + languageId);
 		//logger.info("contentId:" + contentId);
 		//logger.info("propertyName:" + propertyName);
 			
 		NodeDeliveryController nodeDeliveryController			    = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
 		
 		String componentXML   = getPageComponentsString(siteNodeId, this.masterLanguageVO.getId(), contentId);			
 		//logger.info("componentXML:" + componentXML);
 
 		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
 		String componentPropertyXPath = "//component[@id=" + this.componentId + "]/properties/property[@name='" + propertyName + "']";
 		//logger.info("componentPropertyXPath:" + componentPropertyXPath);
 		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentPropertyXPath);
 		if(anl.getLength() > 0)
 		{
 			Node propertyNode = anl.item(0);
 			Element propertyElement = (Element)propertyNode;
 			
 			propertyElement.removeAttribute("path_" + locale.getLanguage());
 			if(propertyElement.getAttributes().getLength() == 0);
 			{
 				propertyNode.getParentNode().removeChild(propertyNode);
 			}
 		}
 
 		String modifiedXML = XMLHelper.serializeDom(document, new StringBuffer()).toString(); 
 		//logger.info("modifiedXML:" + modifiedXML);
 		
 		ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
 		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), this.masterLanguageVO.getId());
 
 		ContentVersionController.getContentVersionController().updateAttributeValue(contentVersionVO.getContentVersionId(), "ComponentStructure", modifiedXML, this.getInfoGluePrincipal());
 
 		this.url = getComponentRendererUrl() + getComponentRendererAction() + "?siteNodeId=" + this.siteNodeId + "&languageId=" + this.languageId + "&contentId=" + this.contentId + "&activatedComponentId=" + this.componentId + "&showSimple=" + this.showSimple;
 		//this.getResponse().sendRedirect(url);		
 		
 		this.url = this.getResponse().encodeURL(url);
 		this.getResponse().sendRedirect(url);
 	    return NONE; 
 	}
 		    
 	/**
 	 * This method creates a parameter for the given input type.
 	 * This is to support form steering information later.
 	 */
 	
 	private Element addPropertyElement(Element parent, String name, String path, String type, Locale locale)
 	{
 		Element element = parent.getOwnerDocument().createElement("property");
 		element.setAttribute("name", name);
 		
 		if(type.equalsIgnoreCase("siteNodeBinding") || type.equalsIgnoreCase("contentBinding"))
 		{
 			element.setAttribute("path", path);
 			element.setAttribute("path_" + locale.getLanguage(), path);
 		}
 		else
 		{
 			element.setAttribute("path_" + locale.getLanguage(), path);
 		}
 		
 		element.setAttribute("type", type);
 		parent.appendChild(element);
 		return element;
 	}
 
 	/**
 	 * This method creates a parameter for the given input type.
 	 * This is to support form steering information later.
 	 */
 	
 	private Element addComponentElement(Element parent, Integer id, String name, Integer contentId)
 	{
 		Element element = parent.getOwnerDocument().createElement("component");
 		element.setAttribute("id", id.toString());
 		element.setAttribute("contentId", contentId.toString());
 		element.setAttribute("name", name);
 		Element properties = parent.getOwnerDocument().createElement("properties");
 		element.appendChild(properties);
 		Element subComponents = parent.getOwnerDocument().createElement("components");
 		element.appendChild(subComponents);
 		parent.appendChild(element);
 		return element;
 	}
 	   
 	/**
 	 * This method creates a parameter for the given input type.
 	 * This is to support form steering information later.
 	 */
 	
 	private Element addBindingElement(Element parent, String entity, Integer entityId, String assetKey)
 	{
 		Element element = parent.getOwnerDocument().createElement("binding");
 		element.setAttribute("entityId", entityId.toString());
 		element.setAttribute("entity", entity);
 		if(assetKey != null && !assetKey.equals(""))
 			element.setAttribute("assetKey", assetKey);
 		
 		parent.appendChild(element);
 		return element;
 	}
 
 	/**
 	 * This method creates a parameter for the given input type.
 	 * This is to support form steering information later.
 	 */
 	
 	private void addBindingElement(Element parent, String qualifyerXML) throws Exception
 	{
 	//logger.info("qualifyerXML:" + qualifyerXML);
 		Document document = XMLHelper.readDocumentFromByteArray(qualifyerXML.getBytes());
 		NodeList nl = document.getChildNodes().item(0).getChildNodes();
 		for(int i=0; i<nl.getLength(); i++)
 		{
 			Element qualifyerElement = (Element)nl.item(i);
 		//logger.info("qualifyerElement:" + qualifyerElement);
 			String entityName = qualifyerElement.getNodeName();
 			String entityId = qualifyerElement.getFirstChild().getNodeValue();
 		//logger.info("entityName:" + entityName);
 		//logger.info("entityId:" + entityId);
 			
 			Element element = parent.getOwnerDocument().createElement("binding");
 			element.setAttribute("entityId", entityId);
 			element.setAttribute("entity", entityName);
 			parent.appendChild(element);
 		}
 	}
 	
 	
 	/**
 	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate" sorted on the property given.
 	 */
 	
 	public List getSortedComponents(String sortProperty) throws Exception
 	{
 	    List componentVOList = null;
 	    
 	    try
 	    {
 	        String direction = "asc";
 	        componentVOList = ComponentController.getController().getComponentVOList(sortProperty, direction, allowedComponentNames, disallowedComponentNames);
 	    }
 	    catch(Exception e)
 	    {
 	        e.printStackTrace();
 	    }
 		
 	    return componentVOList;
 	}
 	     	     
 	/**
 	 * This method fetches the template-string.
 	 */
     
 	private String getPageComponentsString(Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
 	{
 		String template = null;
     	
 		try
 		{
 			ContentVO contentVO = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId).getBoundContent(this.getInfoGluePrincipal(), siteNodeId, languageId, true, "Meta information", DeliveryContext.getDeliveryContext());
 
 			if(contentVO == null)
 				throw new SystemException("There was no template bound to this page which makes it impossible to render.");	
 			
 			ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId);
 			if(contentVersionVO == null)
 			{
 				SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeId);
 				LanguageVO masterLanguage = LanguageController.getController().getMasterLanguage(siteNodeVO.getRepositoryId());
 				contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), masterLanguage.getLanguageId());
 			}
 			
 			template = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO.getId(), "ComponentStructure", false);
 			
 			if(template == null)
 				throw new SystemException("There was no template bound to this page which makes it impossible to render.");	
 		}
 		catch(Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw e;
 		}
 
 		return template;
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
 			if(contentVersionVO != null)
 			{
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
 		}
 		catch(Exception e)
 		{
 			logger.warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
 			imageHref = e.getMessage();
 		}
 		
 		return imageHref;
 	}
 	
 	public Integer getContentId()
 	{
 		return contentId;
 	}
 
 	public void setContentId(Integer integer)
 	{
 		contentId = integer;
 	}
 
 	public Integer getComponentId()
 	{
 		return this.componentId;
 	}
 
 	public void setComponentId(Integer componentId)
 	{
 		this.componentId = componentId;
 	}
 	
 	public Integer getParentComponentId() 
 	{
 		return parentComponentId;
 	}
 	
     public void setParentComponentId(Integer parentComponentId) 
     {
 		this.parentComponentId = parentComponentId;
 	}
 
 	public Integer getLanguageId()
 	{
 		return this.languageId;
 	}
 
 	public Integer getSiteNodeId()
 	{
 		return this.siteNodeId;
 	}
 
 	public void setLanguageId(Integer languageId)
 	{
 		this.languageId = languageId;
 	}
 
 	public void setSiteNodeId(Integer siteNodeId)
 	{
 		this.siteNodeId = siteNodeId;
 	}
 
 	public String getSlotId()
 	{
 		return this.slotId;
 	}
 
 	public void setSlotId(String slotId)
 	{
 		this.slotId = slotId;
 	}
 
 	public Integer getRepositoryId()
 	{
 		return this.repositoryId;
 	}
 
 	public void setRepositoryId(Integer repositoryId)
 	{
 		this.repositoryId = repositoryId;
 	}
 
     public Integer getFilterRepositoryId()
     {
         return filterRepositoryId;
     }
     
     public void setFilterRepositoryId(Integer filterRepositoryId)
     {
         this.filterRepositoryId = filterRepositoryId;
     }
 
 	public String getSpecifyBaseTemplate()
 	{
 		return this.specifyBaseTemplate;
 	}
 
 	public void setSpecifyBaseTemplate(String specifyBaseTemplate)
 	{
 		this.specifyBaseTemplate = specifyBaseTemplate;
 	}
 
 	public String getPropertyName()
 	{
 		return this.propertyName;
 	}
 
 	public void setPropertyName(String propertyName)
 	{
 		this.propertyName = propertyName;
 	}
 
 	public String getPath()
 	{
 		return this.path;
 	}
 
 	public void setPath(String path)
 	{
 		this.path = path;
 	}
 	
 	public LanguageVO getMasterLanguageVO()
 	{
 		return masterLanguageVO;
 	}
 	
     public String getUrl()
     {
         return url;
     }
 	
     public String getSortProperty()
     {
         return sortProperty;
     }
     
     public void setSortProperty(String sortProperty)
     {
         this.sortProperty = sortProperty;
     }
     
     public Integer getDirection()
     {
         return direction;
     }
     
     public void setDirection(Integer direction)
     {
         this.direction = direction;
     }
     
     public String[] getAllowedContentTypeIds()
     {
         return allowedContentTypeIds;
     }
     
     public void setAllowedContentTypeIds(String[] allowedContentTypeIds)
     {
         this.allowedContentTypeIds = allowedContentTypeIds;
     }
 
     public String getAllowedContentTypeIdsAsUrlEncodedString() throws Exception
     {
         StringBuffer sb = new StringBuffer();
         
         for(int i=0; i<allowedContentTypeIds.length; i++)
         {
             if(i > 0)
                 sb.append("&");
             
             sb.append("allowedContentTypeIds=" + URLEncoder.encode(allowedContentTypeIds[i], "UTF-8"));
         }
 
         return sb.toString();
     }
 
     public boolean getShowSimple()
     {
         return showSimple;
     }
     
     public void setShowSimple(boolean showSimple)
     {
         this.showSimple = showSimple;
     }
     
     public Integer getPageTemplateContentId()
     {
         return pageTemplateContentId;
     }
     
     public void setPageTemplateContentId(Integer pageTemplateContentId)
     {
         this.pageTemplateContentId = pageTemplateContentId;
     }
     
     public String[] getAllowedComponentNames()
     {
         return allowedComponentNames;
     }
     
     public void setAllowedComponentNames(String[] allowedComponentNames)
     {
         this.allowedComponentNames = allowedComponentNames;
     }
 
     public String[] getDisallowedComponentNames()
     {
         return disallowedComponentNames;
     }
     
     public void setDisallowedComponentNames(String[] disallowedComponentNames)
     {
         this.disallowedComponentNames = disallowedComponentNames;
     }
 
     public String getAllowedComponentNamesAsUrlEncodedString() throws Exception
     {
         StringBuffer sb = new StringBuffer();
         
         for(int i=0; i<allowedComponentNames.length; i++)
         {
             if(i > 0)
                 sb.append("&");
             
             sb.append("allowedComponentNames=" + URLEncoder.encode(allowedComponentNames[i], "UTF-8"));
         }
         
         return sb.toString();
     }
 
     public String getDisallowedComponentNamesAsUrlEncodedString() throws Exception
     {
         StringBuffer sb = new StringBuffer();
         
         for(int i=0; i<disallowedComponentNames.length; i++)
         {
             if(i > 0)
                 sb.append("&");
             
             sb.append("disallowedComponentNames=" + URLEncoder.encode(disallowedComponentNames[i], "UTF-8"));
         }
         
         return sb.toString();
     }
 
 	public String getAssetKey()
 	{
 		return assetKey;
 	}
 
 	public void setAssetKey(String assetKey)
 	{
 		this.assetKey = assetKey;
 	}
 
 	public void setNewComponentContentId(Integer newComponentContentId)
 	{
 		this.newComponentContentId = newComponentContentId;
 	}
 
 	public String getShowDecorated()
 	{
 		return showDecorated;
 	}
 
 	public void setShowDecorated(String showDecorated)
 	{
 		this.showDecorated = showDecorated;
 	}
 
 }
