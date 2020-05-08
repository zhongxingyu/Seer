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
 
 package org.infoglue.cms.applications.contenttool.actions;
 
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ComponentPropertyDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
 import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersion;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.structure.QualifyerVO;
 import org.infoglue.cms.entities.workflow.EventVO;
 import org.infoglue.cms.exception.Bug;
 import org.infoglue.cms.exception.SystemException;
 
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.cms.util.css.CSSHelper;
 import org.infoglue.cms.util.dom.DOMBuilder;
 import com.opensymphony.module.propertyset.*;
 
 
 public class ViewContentVersionAction extends InfoGlueAbstractAction
 {
 	private static CategoryController categoryController = CategoryController.getController();
 	private static ContentCategoryController contentCategoryController = ContentCategoryController.getController();
 
 	private Integer unrefreshedContentId = new Integer(0);
 	private Integer changeTypeId = new Integer(0);
 	private Integer newContentId = new Integer(0);
 	
 	private Integer digitalAssetId = null;
 	public ContentTypeDefinitionVO contentTypeDefinitionVO;
 	public List availableLanguages = null;
 	
 	private Integer languageId;
 	private Integer repositoryId;
 	private Integer currentEditorId;
 	private String attributeName = "";
 	private String textAreaId = "";
 			
     private ContentVO contentVO;
    private ContentVersionVO contentVersionVO;
 	public List attributes = null;
 
 	private List repositories;
 	
 	//This is used for showing navigationdata
 	private Integer siteNodeId;
 
 	private Integer oldContentId 	= null;
 	private String assetKey 		= null;
 	private boolean treatAsLink    = false;
 	
 	private Map WYSIWYGProperties = null;
 	
 	private String closeOnLoad;
 	
 
 	public String getQualifyerPath(String entity, String entityId)
 	{	
 		try
 		{	
 			if(entity.equalsIgnoreCase("Content"))
 				return ContentController.getContentController().getContentVOWithId(new Integer(entityId)).getName();
 			else if(entity.equalsIgnoreCase("SiteNode"))
 				return SiteNodeController.getController().getSiteNodeVOWithId(new Integer(entityId)).getName();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	public List getContentRelationQualifyers(String qualifyerXML)
 	{
 		getLogger().info("Content qualifyerXML:" + qualifyerXML);
 	    return parseQualifyersFromXML(qualifyerXML, "contentId");
 	}
 
 	public List getSiteNodeRelationQualifyers(String qualifyerXML)
 	{
 		getLogger().info("Content qualifyerXML:" + qualifyerXML);
 	    return parseQualifyersFromXML(qualifyerXML, "siteNodeId");
 	}
 
 	public List getComponentPropertyDefinitions(String componentPropertiesXML)
 	{
 	    return ComponentPropertyDefinitionController.getController().parseComponentPropertyDefinitions(componentPropertiesXML);
 	}
     
 	
 	private List parseQualifyersFromXML(String qualifyerXML, String currentEntityIdentifyer)
 	{
 		List qualifyers = new ArrayList(); 
     	
 		if(qualifyerXML == null || qualifyerXML.length() == 0)
 			return qualifyers;
 		
 		try
 		{
 			Document document = new DOMBuilder().getDocument(qualifyerXML);
 			
 			String entity = document.getRootElement().attributeValue("entity");
 			
 			List children = document.getRootElement().elements();
 			Iterator i = children.iterator();
 			while(i.hasNext())
 			{
 				Element child = (Element)i.next();
 				String id = child.getStringValue();
 				
 				QualifyerVO qualifyerVO = new QualifyerVO();
 				qualifyerVO.setName(currentEntityIdentifyer);
 				qualifyerVO.setValue(id);    
 				qualifyerVO.setPath(this.getQualifyerPath(entity, id));
 				//qualifyerVO.setSortOrder(new Integer(i));
 				qualifyers.add(qualifyerVO);     	
 			}		        	
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return qualifyers;
 	}
 	
     public ViewContentVersionAction()
     {
         this(new ContentVO(), new ContentVersionVO());
     }
     
     public ViewContentVersionAction(ContentVO contentVO, ContentVersionVO contentVersionVO)
     {
         this.contentVO = contentVO;
         this.contentVersionVO = contentVersionVO;
     }
 
     protected void initialize(Integer contentVersionId, Integer contentId, Integer languageId) throws Exception
     {
         initialize(contentVersionId, contentId, languageId, false);
     }
     
     protected void initialize(Integer contentVersionId, Integer contentId, Integer languageId, boolean fallBackToMasterLanguage) throws Exception
     {
         this.contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), contentId);
 		    
         //this.contentVO = ContentController.getContentVOWithId(contentId);
         this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentId);
         this.availableLanguages = ContentController.getContentController().getRepositoryLanguages(contentId);
         
         if(contentVersionId == null)
 		{	
 			//this.contentVersionVO = ContentVersionControllerProxy.getController().getACLatestActiveContentVersionVO(this.getInfoGluePrincipal(), contentId, languageId);
 			//this.contentVersionVO = ContentVersionController.getLatestActiveContentVersionVO(contentId, languageId);
 			this.contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId);
 			if(this.contentVersionVO == null && fallBackToMasterLanguage)
 			{
 			    //getLogger().info("repositoryId:" + repositoryId);
 			    Integer usedRepositoryId = this.repositoryId;
 			    if(this.repositoryId == null && this.contentVO != null)
 			        usedRepositoryId = this.contentVO.getRepositoryId();
 			    
 			    LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(usedRepositoryId);
 			    //getLogger().info("MasterLanguage: " + masterLanguageVO);
 			    this.contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, masterLanguageVO.getId());
 			}
 			
 			if(this.contentVersionVO != null)
 				contentVersionId = contentVersionVO.getContentVersionId();
 		}
 
         if(contentVersionId != null)	
 			this.contentVersionVO = ContentVersionControllerProxy.getController().getACContentVersionVOWithId(this.getInfoGluePrincipal(), contentVersionId);    		 	
     		//this.contentVersionVO = ContentVersionController.getContentVersionVOWithId(contentVersionId);    		 	
 
         if(this.contentTypeDefinitionVO != null)
         {
             this.contentTypeDefinitionVO = ContentTypeDefinitionController.getController().validateAndUpdateContentType(this.contentTypeDefinitionVO);
             this.attributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(this.contentTypeDefinitionVO.getSchemaValue());
         }
     } 
 
     public String doExecute() throws Exception
     {
     	this.initialize(getContentVersionId(), getContentId(), this.languageId);
         
     	String wysiwygEditor = CmsPropertyHandler.getProperty("wysiwygEditor");
     	if(wysiwygEditor == null || wysiwygEditor.equalsIgnoreCase("") || wysiwygEditor.equalsIgnoreCase("HTMLArea"))
     	    return "success";
     	else
     	    return "successForFCKEditor";
     }
 
 	public String doStandalone() throws Exception
 	{
 		this.initialize(getContentVersionId(), getContentId(), this.languageId);
 
     	String wysiwygEditor = CmsPropertyHandler.getProperty("wysiwygEditor");
     	if(wysiwygEditor == null || wysiwygEditor.equalsIgnoreCase("") || wysiwygEditor.equalsIgnoreCase("HTMLArea"))
     	    return "standalone";
     	else
     	    return "standaloneForFCKEditor";
 	}
 
 	public String doBackground() throws Exception
 	{
 		this.initialize(getContentVersionId(), getContentId(), this.languageId);
 		return "background";
 	}
 	
 	public String doViewAssetsDialog() throws Exception
 	{
 	    if(this.oldContentId != null)
 		{
 	        this.contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), getOldContentId());
 		}
 		else
 		{
 		    if(getContentId() != null && getContentId().intValue() != -1)
 		        this.contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), getContentId());
 		}
 		
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
 
 		return "viewAssetsDialog";
 	}
 
 	public String doViewAssetsDialogForFCKEditor() throws Exception
 	{
 	    if(this.oldContentId != null)
 		{
 	        this.contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), getOldContentId());
 		}
 		else
 		{
 		    if(getContentId() != null && getContentId().intValue() != -1)
 		        this.contentVO = ContentControllerProxy.getController().getACContentVOWithId(this.getInfoGluePrincipal(), getContentId());
 		}
 		
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
 
 		return "viewAssetsDialogForFCKEditor";
 	}
 	
 	
 	public String doViewAssets() throws Exception
 	{
 		if(getContentId() != null && getContentId().intValue() != -1)
 		{
 		    this.initialize(getContentVersionId(), getContentId(), this.languageId, true);
 		}
 
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
 
 		return "viewAssets";
 	}
 
 	public String doViewAssetsForFCKEditor() throws Exception
 	{
 		if(getContentId() != null && getContentId().intValue() != -1)
 		{
 		    this.initialize(getContentVersionId(), getContentId(), this.languageId, true);
 		}
 
 		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
 
 		return "viewAssetsForFCKEditor";
 	}
 
     public String doPreview() throws Exception
     {
         this.initialize(getContentVersionId(), getContentId(), this.languageId);
         return "preview";
     }
 
     public String doDeleteDigitalAsset() throws Exception
     {
     	ContentVersionController.getContentVersionController().deleteDigitalAssetRelation(getContentVersionId(), this.digitalAssetId);
     	//this.initialize(getContentVersionId(), getContentId(), this.languageId);
     	//return "success";
     	return doExecute();
     }
     
     public String doDeleteDigitalAssetStandalone() throws Exception
     {
     	ContentVersionController.getContentVersionController().deleteDigitalAssetRelation(getContentVersionId(), this.digitalAssetId);
     	//this.initialize(getContentVersionId(), getContentId(), this.languageId);
     	//return "success";
     	return doStandalone();
     }
     
     public EventVO getEvent(Integer contentVersionId)
 	{
 		EventVO eventVO = null;
 		try
 		{
 			if(contentVersionId != null)
 			{
 				ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(contentVersionId);
 				List events = EventController.getEventVOListForEntity(ContentVersion.class.getName(), contentVersion.getId());
 				if(events != null && events.size() > 0)
 					eventVO = (EventVO)events.get(0);
 			}
 		}
 		catch(Exception e)
 		{
 			getLogger().error("An error occurred when we tried to get any events for this version:" + e.getMessage(), e);
 		}
 		
 		return eventVO;
 	}
 
 	public List getContentPath()
 	{
 		ContentVO contentVO = this.contentVO;
 		List ret = new ArrayList();
 		// ret.add(0, contentVO);
 
 		while (contentVO.getParentContentId() != null)
 		{
 			try {
 				contentVO = ContentControllerProxy.getController().getContentVOWithId(contentVO.getParentContentId());
 			} catch (SystemException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Bug e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			ret.add(0, contentVO);
 		}
 		return ret;
 	}
 
     public java.lang.Integer getContentVersionId()
     {
         return this.contentVersionVO.getContentVersionId();
     }
     
     public void setContentVersionId(java.lang.Integer contentVersionId)
     {
         this.contentVersionVO.setContentVersionId(contentVersionId);
     }
         
     public java.lang.Integer getContentId()
     {
         return this.contentVO.getContentId();
     }
         
     public void setContentId(java.lang.Integer contentId)
     {
 	    this.contentVO.setContentId(contentId);
     }
     
     public java.lang.Integer getContentTypeDefinitionId()
     {
         return this.contentTypeDefinitionVO.getContentTypeDefinitionId();
     }
 
     public String getContentTypeDefinitionName()
     {
         return this.contentTypeDefinitionVO.getName();
     }
             
    	public void setLanguageId(Integer languageId)
 	{
    	    this.languageId = languageId;
 	}
 
     public java.lang.Integer getLanguageId()
     {
         return this.languageId;
     }
 	
 	public void setStateId(Integer stateId)
 	{
 		this.contentVersionVO.setStateId(stateId);
 	}
 
 	public void setVersionComment(String versionComment)
 	{
 		this.contentVersionVO.setVersionComment(versionComment);
 	}
 
 	public void setDigitalAssetId(Integer digitalAssetId)
 	{
 		this.digitalAssetId = digitalAssetId;
 	}
 	
 	public String getVersionComment()
 	{
 		return this.contentVersionVO.getVersionComment();
 	}
 	
 	public Integer getStateId()
 	{
 		return this.contentVersionVO.getStateId();
 	}
 
 	public Boolean getIsActive()
 	{
 		return this.contentVersionVO.getIsActive();
 	}
             
     public String getName()
     {
         return this.contentVO.getName();
     }
 
     public java.lang.Integer getRepositoryId()
     {
         if(this.contentVO != null && this.contentVO.getRepositoryId() != null)
             return this.contentVO.getRepositoryId();
         else
             return this.repositoryId;
     }
 
 	public List getAvailableLanguages()
 	{
 		return this.availableLanguages;
 	}	
 
 	/**
 	 * Returns a list of digital assets available for this content version.
 	 */
 	
 	public List getDigitalAssets()
 	{
 		List digitalAssets = null;
 		
 		try
 		{
 			if(this.contentVersionVO != null && this.contentVersionVO.getContentVersionId() != null)
 	       	{
 	       		digitalAssets = DigitalAssetController.getDigitalAssetVOList(this.contentVersionVO.getContentVersionId());
 	       	}
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("We could not fetch the list of digitalAssets: " + e.getMessage(), e);
 		}
 		
 		return digitalAssets;
 	}	
 	
 	/**
 	 * Returns a list of digital assets available for this content version.
 	 */
 	
 	public List getInheritedDigitalAssets()
 	{
 		List digitalAssets = null;
 		
 		try
 		{
 			if(this.contentVO != null && this.contentVO.getContentId() != null && this.contentVO.getContentId().intValue() != -1)
 	       	{
 	       		digitalAssets = DigitalAssetController.getDigitalAssetVOList(this.contentVO.getContentId(), this.languageId, true);
 	       	}
 			/*
 			if(this.contentVersionVO != null && this.contentVersionVO.getContentVersionId() != null)
 	       	{
 	       		digitalAssets = DigitalAssetController.getDigitalAssetVOList(this.contentVersionVO.getContentVersionId());
 	       	}
 	       	*/
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("We could not fetch the list of digitalAssets: " + e.getMessage(), e);
 		}
 		
 		return digitalAssets;
 	}	
 
 
 	/**
 	 * This method fetches the blob from the database and saves it on the disk.
 	 * Then it returnes a url for it
 	 */
 	
 	public String getDigitalAssetUrl(Integer digitalAssetId) throws Exception
 	{
 		String imageHref = null;
 		try
 		{
        		imageHref = DigitalAssetController.getDigitalAssetUrl(digitalAssetId);
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
 			imageHref = e.getMessage();
 		}
 		
 		return imageHref;
 	}
 	
 	
 	/**
 	 * This method fetches the blob from the database and saves it on the disk.
 	 * Then it returnes a url for it
 	 */
 	
 	public String getDigitalAssetThumbnailUrl(Integer digitalAssetId) throws Exception
 	{
 		String imageHref = null;
 		try
 		{
        		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(digitalAssetId);
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
 			imageHref = e.getMessage();
 		}
 		
 		return imageHref;
 	}
 
 	
 	/**
 	 * This method fetches the blob from the database and saves it on the disk.
 	 * Then it returnes a url for it
 	 */
 	
 	public String getDigitalAssetUrl(Integer contentId, Integer languageId) throws Exception
 	{
 		String imageHref = null;
 		try
 		{
        		imageHref = DigitalAssetController.getDigitalAssetUrl(contentId, languageId);
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("We could not get the url of the digitalAsset: " + e.getMessage(), e);
 			imageHref = e.getMessage();
 		}
 		
 		return imageHref;
 	}
 	
 	
 	/**
 	 * This method fetches the blob from the database and saves it on the disk.
 	 * Then it returnes a url for it
 	 */
 	
 	public String getDigitalAssetThumbnailUrl(Integer contentId, Integer languageId) throws Exception
 	{
 		String imageHref = null;
 		try
 		{
        		imageHref = DigitalAssetController.getDigitalAssetThumbnailUrl(contentId, languageId);
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("We could not get the url of the thumbnail: " + e.getMessage(), e);
 			imageHref = e.getMessage();
 		}
 		
 		return imageHref;
 	}
 	
 
 	
 	/**
 	 * This method fetches a value from the xml that is the contentVersions Value. If the 
 	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
 	 */
 	 
 	public String getAttributeValue(String key)
 	{
 		String value = "";
 		if(this.contentVersionVO != null)
 		{
 			try
 	        {
 		        getLogger().info("key:" + key);
 				getLogger().info("VersionValue:" + this.contentVersionVO.getVersionValue());
 		
 				String xml = this.contentVersionVO.getVersionValue();
 				
 				int startTagIndex = xml.indexOf("<" + key + ">");
 				int endTagIndex   = xml.indexOf("]]></" + key + ">");
 
 				if(startTagIndex > 0 && startTagIndex < xml.length() && endTagIndex > startTagIndex && endTagIndex <  xml.length())
 				{
 					value = xml.substring(startTagIndex + key.length() + 11, endTagIndex);
 					value = new VisualFormatter().escapeHTML(value);
 				}					
 				
 				/*
 		        InputSource inputSource = new InputSource(new StringReader(this.contentVersionVO.getVersionValue()));
 				
 				DOMParser parser = new DOMParser();
 				parser.parse(inputSource);
 				Document document = parser.getDocument();
 				NodeList nl = document.getDocumentElement().getChildNodes();
 				Node n = nl.item(0);
 
 				nl = n.getChildNodes();
 				for(int i=0; i<nl.getLength(); i++)
 				{
 					n = nl.item(i);
 					if(n.getNodeName().equalsIgnoreCase(key))
 					{
 						if(n.getFirstChild() != null && n.getFirstChild().getNodeValue() != null)
 						{
 							value = n.getFirstChild().getNodeValue();
 							getLogger().info("Getting value: " + value);
 
 							//getLogger().info("VersionValue:" + value);
 							if(value != null)
 								value = new VisualFormatter().escapeHTML(value);
 							
 							break;
 						}
 					}
 				}	
 				*/	   				
 	        }
 	        catch(Exception e)
 	        {
 	        	e.printStackTrace();
 	        }
 		}
 		
		//getLogger().info("value:" + value);	
 		
 		return value;
 	}
 	
 	/**
 	 * This method fetches a value from the xml that is the contentVersions Value. If the 
 	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
 	 */
 	 
 	public String getUnescapedAttributeValue(String key)
 	{
 		String value = "";
 		if(this.contentVersionVO != null)
 		{
 			try
 			{
 				getLogger().info("key:" + key);
 				getLogger().info("VersionValue:" + this.contentVersionVO.getVersionValue());
 				
 				String xml = this.contentVersionVO.getVersionValue();
 				
 				int startTagIndex = xml.indexOf("<" + key + ">");
 				int endTagIndex   = xml.indexOf("]]></" + key + ">");
 
 				if(startTagIndex > 0 && startTagIndex < xml.length() && endTagIndex > startTagIndex && endTagIndex <  xml.length())
 				{
 					value = xml.substring(startTagIndex + key.length() + 11, endTagIndex);
 				}					
 
 				/*
 				InputSource inputSource = new InputSource(new StringReader(this.contentVersionVO.getVersionValue()));
 				
 				DOMParser parser = new DOMParser();
 				parser.parse(inputSource);
 				Document document = parser.getDocument();
 				NodeList nl = document.getDocumentElement().getChildNodes();
 				Node n = nl.item(0);
 
 				nl = n.getChildNodes();
 				for(int i=0; i<nl.getLength(); i++)
 				{
 					n = nl.item(i);
 					if(n.getNodeName().equalsIgnoreCase(key))
 					{
 						if(n.getFirstChild() != null && n.getFirstChild().getNodeValue() != null)
 						{
 							value = n.getFirstChild().getNodeValue();
 							getLogger().info("Getting value: " + value);
 							break;
 						}
 					}
 				}	
 				*/	        	
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		//getLogger().info("value:" + value);	
 		return value;
 	}
 	
 	/**
 	 * This method returns the attributes in the content type definition for generation.
 	 */
 	
 	public List getContentTypeAttributes()
 	{   		
 		return this.attributes;
 	}
 
 	public ContentVersionVO getContentVersionVO()
 	{
 		return contentVersionVO;
 	}
 
 	/**
 	 * This method gets the WYSIWYG Properties
 	 */
 	
 	public Map getWYSIWYGProperties() throws Exception
 	{
 		if(this.WYSIWYGProperties != null)
 			return this.WYSIWYGProperties;
 		
 		//First we got the values from repository properties... 
 		Map args = new HashMap();
 	    args.put("globalKey", "infoglue");
 	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
 	    
 	    byte[] WYSIWYGConfigBytes = ps.getData("repository_" + this.getRepositoryId() + "_WYSIWYGConfig");
 	    if(WYSIWYGConfigBytes != null)
 	    {
 	    	Properties properties = new Properties();
 	    	properties.load(new ByteArrayInputStream(WYSIWYGConfigBytes));
 	    	this.WYSIWYGProperties = properties;
 	    }
 	    
 	    //Now we add the overridden parameters in role/user properties.
 	    Map principalWYSIWYGProperties = getPrincipalPropertyHashValues("WYSIWYGConfig", false);
 	    
 	    if(this.WYSIWYGProperties != null)
 	    {
 		    getLogger().info("this.WYSIWYGProperties:" + this.WYSIWYGProperties.size());
 		    
 		    //Clear sections of the configuration if overridden
 		    if(principalWYSIWYGProperties.containsKey("toolbar_line0_position0"))
 		    {
 		    	for(int lineIndex=0; lineIndex<3; lineIndex++)
 		    	{
 		    		for(int colIndex=0; colIndex<30; colIndex++)
 		    		{
 		    			WYSIWYGProperties.remove("toolbar_line" + lineIndex + "_position" + colIndex);
 		    		}
 		    	}
 		    }
 	
 		    if(principalWYSIWYGProperties.containsKey("css.url.0"))
 		    {
 		    	for(int index=0; index<10; index++)
 		    	{
 		    		WYSIWYGProperties.remove("css.url." + index);
 		    	}
 		    }
 	
 		    if(principalWYSIWYGProperties.containsKey("css.class.0"))
 		    {
 		    	for(int index=0; index<50; index++)
 		    	{
 		    		WYSIWYGProperties.remove("css.class." + index);
 		    	}
 		    }
 	    }
 	    else
 	    {
 	    	this.WYSIWYGProperties = new HashMap();
 	    }
 	    
 	    //Now we add the new properties
 	    this.WYSIWYGProperties.putAll(principalWYSIWYGProperties);
 	    
 	    return this.WYSIWYGProperties;
 	}
 	
 	/**
 	 * This method returns a list of css-classes available to the WYSIWYG.
 	 */
 	
 	public List getCSSRules(String url)
 	{
 		getLogger().info("url:" + url);
 	    CSSHelper cssHelper = CSSHelper.getHelper(); 
 	    cssHelper.setCSSUrl(url);
 	    
 	    return cssHelper.getCSSRules();
 	}
 
 	/**
 	 * This method returns a infoglue-specific PropertySet
 	 */
 
 	public boolean getEnableCSSPlugin() throws Exception
 	{
 		boolean enableCSSPlugin = false;
 		
     	Map properties = getWYSIWYGProperties();
 
     	String enableCSSPluginString = (String)properties.get("enableCSSPlugin");
     	if(enableCSSPluginString != null && enableCSSPluginString.trim().equalsIgnoreCase("true"))
     	{
     		enableCSSPlugin = true;
     	}
         
 	    return enableCSSPlugin;
 	}
 	
 	/**
 	 * This method returns a infoglue-specific PropertySet
 	 */
 
 	public List getCSSList() throws Exception
 	{
 		List cssList = new ArrayList();
 		
 		Map properties = getWYSIWYGProperties();
 
     	int index = 0;
     	String cssUrl = (String)properties.get("css.url." + index);
     	while(cssUrl != null)
     	{
     		cssList.add(cssUrl);
     		index++;
 	    	cssUrl = (String)properties.get("css.url." + index);
     	}
         
 	    return cssList;
 	}
 	
 	/**
 	 * This method returns a infoglue-specific PropertySet
 	 */
 
 	public List getAllowedClasses() throws Exception
 	{
 		List allowedClasses = new ArrayList();
 		
 		Map properties = getWYSIWYGProperties();
     	
     	int index = 0;
     	String cssUrl = (String)properties.get("css.class." + index);
     	while(cssUrl != null)
     	{
     		allowedClasses.add(cssUrl);
     		index++;
 	    	cssUrl = (String)properties.get("css.class." + index);
     	}
 	    
 	    return allowedClasses;
 	}
 	
 	
 	/**
 	 * This method returns the base-url to the delivery-engine.
 	 */
 	
 	public String getDeliveryBaseUrl()
 	{
 		String previewDeliveryUrl = CmsPropertyHandler.getProperty("previewDeliveryUrl");
 		int index = previewDeliveryUrl.lastIndexOf("/");
 		if(index > 0)
 		{
 			previewDeliveryUrl = previewDeliveryUrl.substring(0, index);
 		}
 		return previewDeliveryUrl;
 	}
 
 	/**
 	 * Return the listing of Category attributes for this type of Content
 	 */
 	public List getDefinedCategoryKeys()
 	{
 		try
 		{
 			if(contentTypeDefinitionVO != null)
 				return ContentTypeDefinitionController.getController().getDefinedCategoryKeys(contentTypeDefinitionVO.getSchemaValue());
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("We could not fetch the list of defined category keys: " + e.getMessage(), e);
 		}
 
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * Returns the Category tree for the given Category id.
 	 * @param categoryId The base Category
 	 * @return A list of all Children (and their children, etc)
 	 */
 	public List getAvailableCategories(Integer categoryId)
 	{
 		try
 		{	
 		    String protectCategories = CmsPropertyHandler.getProperty("protectCategories");
 		    if(protectCategories != null && protectCategories.equalsIgnoreCase("true"))
 		        return categoryController.getAuthorizedActiveChildren(categoryId, this.getInfoGluePrincipal());
 			else
 			    return categoryController.findAllActiveChildren(categoryId);
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("We could not fetch the list of categories: " + e.getMessage(), e);
 		}
 
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * Returns all current Category relationships for th specified attrbiute name
 	 * @param attribute
 	 * @return
 	 */
 	public List getRelatedCategories(String attribute)
 	{
 		try
 		{
 			if(this.contentVersionVO != null && this.contentVersionVO.getContentVersionId() != null)
 				return contentCategoryController.findByContentVersionAttribute(attribute, contentVersionVO.getContentVersionId());
 		}
 		catch(Exception e)
 		{
 			getLogger().warn("We could not fetch the list of defined category keys: " + e.getMessage(), e);
 		}
 
 		return Collections.EMPTY_LIST;
 	}
 	
 	
 	public Integer getCurrentEditorId()
 	{
 		return currentEditorId;
 	}
 
 	public void setCurrentEditorId(Integer integer)
 	{
 		currentEditorId = integer;
 	}
 
 	public String getAttributeName()
 	{
 		return this.attributeName;
 	}
 
 	public void setAttributeName(String attributeName)
 	{
 		this.attributeName = attributeName;
 	}
 
 	public String getTextAreaId()
 	{
 		return this.textAreaId;
 	}
 
 	public void setTextAreaId(String textAreaId)
 	{
 		this.textAreaId = textAreaId;
 	}
 
 	public Integer getSiteNodeId()
 	{
 		return this.siteNodeId;
 	}
 
 	public void setSiteNodeId(Integer siteNodeId)
 	{
 		this.siteNodeId = siteNodeId;
 	}
 
     public void setRepositoryId(Integer repositoryId)
     {
         this.repositoryId = repositoryId;
     }
     
     public List getRepositories()
     {
         return repositories;
     }
     
     public String getAssetKey()
     {
         return assetKey;
     }
     
     public void setAssetKey(String assetKey)
     {
         this.assetKey = assetKey;
     }
     
     public Integer getOldContentId()
     {
         return oldContentId;
     }
     
     public void setOldContentId(Integer oldContentId)
     {
         this.oldContentId = oldContentId;
     }
     
     public boolean getTreatAsLink()
     {
         return treatAsLink;
     }
     
     public void setTreatAsLink(boolean treatAsLink)
     {
         this.treatAsLink = treatAsLink;
     }
     
 	public ContentVO getContentVO() 
 	{
 		return contentVO;
 	}
 	
     public String getCloseOnLoad()
     {
         return closeOnLoad;
     }
     
     public void setCloseOnLoad(String closeOnLoad)
     {
         this.closeOnLoad = closeOnLoad;
     }
     
     public Integer getNewContentId()
     {
         return newContentId;
     }
     
     public void setNewContentId(Integer newContentId)
     {
         this.newContentId = newContentId;
     }
     
     public void setContentVersionVO(ContentVersionVO contentVersionVO)
     {
         this.contentVersionVO = contentVersionVO;
     }
 }
