 /**********************************************************************************
  *
  * $URL$
  * $Id$  
  ***********************************************************************************
  *
  * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  **********************************************************************************/
 package org.etudes.tool.melete;
 
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UICommand;
 import javax.faces.component.UIData;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.MeleteCHService;
 import org.etudes.api.app.melete.SectionService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.api.app.melete.exception.UserErrorException;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.util.ResourceLoader;
 
 public class AddResourcesPage implements ServletContextListener
 {
 
 	private String fileType;
 	private String numberItems;
 	private int maxUploadSize;
 	private int removeLinkIndex;
 	private List<UrlTitleObj> utList;
 	protected MeleteCHService meleteCHService;
 	protected SectionService sectionService;
 	private UIData table;
 	/** Dependency: The logging service. */
 	protected Log logger = LogFactory.getLog(AddResourcesPage.class);
 	private HashMap<String, ArrayList<String>> hm_msgs;
 
 	/**
 	 * default constructor
 	 */
 	public AddResourcesPage()
 	{
 	}
 
 	/**
 	 * Get the number of items added
 	 * 
 	 * @return
 	 */
 	public String getNumberItems()
 	{
 		FacesContext facesContext = FacesContext.getCurrentInstance();
 		if (facesContext.getExternalContext().getRequestParameterMap().get("showMessage") != null)
 		{
 			String show = (String) facesContext.getExternalContext().getRequestParameterMap().get("showMessage");
 
 			if (show == null || show.length() == 0) return numberItems;
 
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			String errMsg = bundle.getString("file_too_large");
 			facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "file_too_large", errMsg));
 		}
 		return this.numberItems;
 	}
 
 	/**
 	 * Set the number of upload/link items to add
 	 * 
 	 * @param numberItems
 	 *        Number
 	 */
 	public void setNumberItems(String numberItems)
 	{
 		this.numberItems = numberItems;
 	}
 
 	/**
 	 * Read the max size of a file specified in sakai.properties file
 	 * 
 	 * @return
 	 */
 	public int getMaxUploadSize()
 	{
 		if (maxUploadSize == 0)
 		{
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			MeleteSiteAndUserInfo mPage = (MeleteSiteAndUserInfo) binding.getValue(context);
 			maxUploadSize = mPage.getMaxUploadSize();
 		}
 		return maxUploadSize;
 	}
 
 	/**
 	 * Reset the values
 	 */
 	public void resetValues()
 	{
 		this.utList = null;
 		this.numberItems = null;
 		this.fileType = null;
 		this.maxUploadSize = 0;
 	}
 
 	/**
 	 * Initialize the values
 	 */
 	public void cancelResetValues()
 	{
 		this.utList = null;
 		this.numberItems = "1";
 	}
 
 	/**
 	 * Adds the items. Read a file from apache FileItem object and add to meleteDocs/uploads collection.
 	 * 
 	 * @return
 	 */
 	public String addItems()
 	{
 		byte[] secContentData;
 		String secResourceName;
 		String secContentMimeType;
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 
 		ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 		MeleteSiteAndUserInfo info = (MeleteSiteAndUserInfo) binding.getValue(context);
 		String addCollId = getMeleteCHService().getUploadCollectionId(info.getCourse_id());
 
 		// Code that validates required fields
 		int emptyCounter = 0;
 
 		if (this.fileType.equals("upload"))
 		{
 			for (int i = 1; i <= 10; i++)
 			{
 				org.apache.commons.fileupload.FileItem fi = (org.apache.commons.fileupload.FileItem) context.getExternalContext().getRequestMap()
 				.get("file" + i);
 				if (fi == null || fi.getName() == null || fi.getName().length() == 0)
 				{
 					emptyCounter = emptyCounter + 1;
 				}
 			}
 
 			if (emptyCounter == 10)
 			{
 				binding = Util.getBinding("#{manageResourcesPage}");
 				ManageResourcesPage manResPage = (ManageResourcesPage) binding.getValue(context);
 				manResPage.resetValues();
 				return "manage_content";
 			}
 			/*
 			 * try { if (emptyCounter == 10) throw new MeleteException("all_uploads_empty"); } catch (MeleteException mex) { String errMsg = bundle.getString(mex.getMessage()); context.addMessage (null, new
 			 * FacesMessage(FacesMessage.SEVERITY_ERROR,mex.getMessage(),errMsg)); return "failure"; }
 			 */
 
 			for (int i = 1; i <= 10; i++)
 			{
 				try
 				{
 					org.apache.commons.fileupload.FileItem fi = (org.apache.commons.fileupload.FileItem) context.getExternalContext().getRequestMap()
 					.get("file" + i);
 
 					if (fi != null && fi.getName() != null && fi.getName().length() != 0)
 					{
 						// validate fileName
 						Util.validateUploadFileName(fi.getName());
 						validateFileSize(fi.getSize());
 						// filename on the client
 						secResourceName = fi.getName();
 						if (secResourceName.indexOf("/") != -1)
 						{
 							secResourceName = secResourceName.substring(secResourceName.lastIndexOf("/") + 1);
 						}
 						if (secResourceName.indexOf("\\") != -1)
 						{
 							secResourceName = secResourceName.substring(secResourceName.lastIndexOf("\\") + 1);
 						}
 
 						if (logger.isDebugEnabled()) logger.debug("Rsrc name is " + secResourceName);
 						if (logger.isDebugEnabled()) logger.debug("upload section content data " + (int) fi.getSize());
 
 						secContentData = new byte[(int) fi.getSize()];
 						InputStream is = fi.getInputStream();
 						is.read(secContentData);
 
 						secContentMimeType = fi.getContentType();
 
 						if (logger.isDebugEnabled()) logger.debug("file upload success" + secContentMimeType);
 						if (logger.isDebugEnabled()) logger.debug("new names for upload content is" + secContentMimeType + "," + secResourceName);
 
 						addItem(secResourceName, secContentMimeType, addCollId, secContentData);
 					}
 					else
 					{
 						logger.debug("File being uploaded is NULL");
 						continue;
 					}
 				}
 				catch (MeleteException mex)
 				{
 					String mexMsg = mex.getMessage();
 					if (mexMsg.equals("embed_img_bad_filename")) mexMsg = "img_bad_filename";
 					String errMsg = bundle.getString(mexMsg);
 					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mexMsg, errMsg));
 					return "failure";
 				}
 				catch (Exception e)
 				{
 					logger.debug("file upload FAILED" + e.toString());
 				}
 			}
 		}
 
 		if (this.fileType.equals("link"))
 		{
 			Iterator<UrlTitleObj> utIterator = utList.iterator();
 			// Finish validating here
 			int count = -1;
 			while (utIterator.hasNext())
 			{
 				count++;
 				try
 				{
 					UrlTitleObj utObj = (UrlTitleObj) utIterator.next();
 					if (utObj.title != null) utObj.title = utObj.title.trim();
 					String linkUrl = utObj.getUrl();
 					if (linkUrl != null) linkUrl = linkUrl.trim();
 					String checkUrl = linkUrl;
 					if (checkUrl != null)
 					{
 						checkUrl = checkUrl.replace("http://", "");
 						checkUrl = checkUrl.replace("https://", "");
 					}
 					if ((utObj.title == null || utObj.title.length() == 0) && (checkUrl == null || checkUrl.length() == 0))
 					{
 						utIterator.remove();
 						continue;
 					}
 					if (utObj.title == null || utObj.title.length() == 0)
 					{
 						context.addMessage("LinkUploadForm:utTable:" + count + ":title", new FacesMessage(FacesMessage.SEVERITY_ERROR,
 								"URL_title_reqd", bundle.getString("URL_title_reqd")));
 						return "#";
 					}
 
 					if (checkUrl == null || checkUrl.length() == 0)
 					{
 						context.addMessage("LinkUploadForm:utTable:" + count + ":url", new FacesMessage(FacesMessage.SEVERITY_ERROR, "URL_reqd",
 								bundle.getString("URL_reqd")));
 						return "#";
 					}
 					Util.validateLink(linkUrl);
 				}
 				catch (UserErrorException uex)
 				{
 					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "add_section_bad_url_formats", bundle
 							.getString("add_section_bad_url_formats")));
 					return "failure";
 				}
 				catch (Exception e)
 				{
 					logger.debug("link upload FAILED" + e.toString());
 				}
 			}
 			utIterator = utList.iterator();
 			while (utIterator.hasNext())
 			{
 				UrlTitleObj utObj = (UrlTitleObj) utIterator.next();
 				try
 				{
 					secContentMimeType = MeleteCHService.MIME_TYPE_LINK;
 					String linkUrl = utObj.getUrl();
 					secResourceName = utObj.getTitle();
 					if ((linkUrl != null) && (linkUrl.trim().length() > 0) && (secResourceName != null) && (secResourceName.trim().length() > 0))
 					{
 						secContentData = new byte[linkUrl.length()];
 						secContentData = linkUrl.getBytes();
 						addItem(secResourceName, secContentMimeType, addCollId, secContentData);
 					}
 				}
 				catch (MeleteException mex)
 				{
 					String errMsg = bundle.getString(mex.getMessage());
 					context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mex.getMessage(), errMsg));
 					return "failure";
 				}
 				catch (Exception e)
 				{
 					logger.debug("link upload FAILED" + e.toString());
 				}
 			}
 		}
 		binding = Util.getBinding("#{manageResourcesPage}");
 		ManageResourcesPage manResPage = (ManageResourcesPage) binding.getValue(context);
 		manResPage.resetValues();
 		return "manage_content";
 	}
 
 	/**
 	 * Add an item to meleteDocs collection.
 	 * 
 	 * @param secResourceName
 	 *        Resource display name
 	 * @param secContentMimeType
 	 *        Mime type
 	 * @param addCollId
 	 *        Collection Id
 	 * @param secContentData
 	 *        Resource content data
 	 * @return Resource Id
 	 * @throws MeleteException
 	 */
 	public String addItem(String secResourceName, String secContentMimeType, String addCollId, byte[] secContentData) throws MeleteException
 	{
 		ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(false, secResourceName, "");
 		if (logger.isDebugEnabled()) logger.debug("add resource now " + secContentData);
 		try
 		{
 			return getMeleteCHService().addResourceItem(secResourceName, secContentMimeType, addCollId, secContentData, res);
 		}
 		catch (MeleteException me)
 		{
 			logger.debug("error in creating resource for section content");
 			throw me;
 		}
 		catch (Exception e)
 		{
 			logger.debug("error in creating resource for section content" + e.toString());
 			throw new MeleteException("add_item_fail");
 		}
 	}
 
 	/**
 	 * Updates the number of file upload boxes to show.
 	 * 
 	 * @param event
 	 *        ValueChangeEvent
 	 * @throws AbortProcessingException
 	 */
 	public void updateNumber(ValueChangeEvent event) throws AbortProcessingException
 	{
 		UIInput numberItemsInput = (UIInput) event.getComponent();
 
 		this.numberItems = (String) numberItemsInput.getValue();
 
 		if (Integer.parseInt(this.numberItems) > this.utList.size())
 		{
 			int newItemsCount = Integer.parseInt(this.numberItems) - this.utList.size();
 			for (int i = 0; i < newItemsCount; i++)
 			{
 				UrlTitleObj utObj = new UrlTitleObj("http://", "");
 				this.utList.add(utObj);
 			}
 		}
 		if (Integer.parseInt(this.numberItems) < this.utList.size())
 		{
 			int listSize = this.utList.size();
 			for (int i = Integer.parseInt(this.numberItems); i < listSize; i++)
 			{
 
 				this.utList.remove(Integer.parseInt(this.numberItems));
 			}
 		}
 
 	}
 
 	/**
 	 * Removes the box.
 	 * 
 	 * @param evt
 	 *        ActionEvent
 	 */
 	public void removeLink(ActionEvent evt)
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		UICommand cmdLink = (UICommand) evt.getComponent();
 		String selclientId = cmdLink.getClientId(ctx);
 		selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 		selclientId = selclientId.substring(selclientId.indexOf(':') + 1);
 		String rowId = selclientId.substring(0, selclientId.indexOf(':'));
 		this.removeLinkIndex = Integer.parseInt(rowId);
 		if (Integer.parseInt(this.numberItems) > 1)
 		{
 			this.numberItems = String.valueOf(Integer.parseInt(this.numberItems) - 1);
 			this.utList.remove(this.removeLinkIndex);
 		}
 		else
 		{
 			this.utList = new ArrayList<UrlTitleObj>();
 			this.utList.add(new UrlTitleObj("http://", ""));
 		}
 
 	}
 
 	/**
 	 * Navigation rule
 	 * 
 	 * @return
 	 */
 	public String redirectToLinkUpload()
 	{
 		return "link_upload_view";
 	}
 
 	/**
 	 * Cancel adding items.
 	 * 
 	 * @return
 	 */
 	public String cancel()
 	{
 		FacesContext ctx = FacesContext.getCurrentInstance();
 		ValueBinding binding = Util.getBinding("#{manageResourcesPage}");
 		ManageResourcesPage manResPage = (ManageResourcesPage) binding.getValue(ctx);
 		manResPage.resetValues();
 		return "manage_content";
 	}
 
 	/**
 	 * Get MeleteCHService service
 	 * 
 	 * @return
 	 */
 	public MeleteCHService getMeleteCHService()
 	{
 		return this.meleteCHService;
 	}
 
 	/**
 	 * Set MeleteCHService service
 	 * 
 	 * @param meleteCHService
 	 *        MeleteCHService
 	 */
 	public void setMeleteCHService(MeleteCHService meleteCHService)
 	{
 		this.meleteCHService = meleteCHService;
 	}
 
 	public void setSectionService(SectionService sectionService)
 	{
 		this.sectionService = sectionService;
 	}
 
 	/**
 	 * Get the type of file being added - upload or link
 	 * 
 	 * @return
 	 */
 	public String getFileType()
 	{
 		return this.fileType;
 	}
 
 	/**
 	 * Set the type of file being added - upload or link
 	 * 
 	 * @return
 	 */
 	public void setFileType(String fileType)
 	{
 		this.fileType = fileType;
 	}
 
 	/**
 	 * Get the Url titles of all items being added.
 	 * 
 	 * @return the list of UrlTitleObj objects
 	 */
 	public List<UrlTitleObj> getUtList()
 	{
 		if (this.utList == null)
 		{
 			utList = new ArrayList<UrlTitleObj>();
 			if (this.fileType.equals("link"))
 			{
 				for (int i = 0; i < Integer.parseInt(this.numberItems); i++)
 				{
 					UrlTitleObj utObj = new UrlTitleObj("http://", "");
 					utList.add(utObj);
 				}
 			}
 		}
 		return this.utList;
 	}
 
 	/**
 	 * Set the list.
 	 * 
 	 * @param utList
 	 */
 	public void setUtList(List<UrlTitleObj> utList)
 	{
 		this.utList = utList;
 	}
 
 	/**
 	 * 
 	 * Inner class to get URL title and URL location.
 	 * 
 	 */
 	public class UrlTitleObj
 	{
 		String url, title;
 
 		public UrlTitleObj(String url, String title)
 		{
 			this.url = url;
 			this.title = title;
 		}
 
 		public String getUrl()
 		{
 			return this.url;
 		}
 
 		public void setUrl(String url)
 		{
 			this.url = url;
 		}
 
 		public String getTitle()
 		{
 			return this.title;
 		}
 
 		public void setTitle(String title)
 		{
 			this.title = title;
 		}
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public UIData getTable()
 	{
 		return this.table;
 	}
 
 	/**
 	 * 
 	 * @param table
 	 */
 	public void setTable(UIData table)
 	{
 		this.table = table;
 	}
 
 	/**
 	 * Validates file name. allowed pattern is [a-zA-z0-9]_-.
 	 */
 	public boolean validateFile(String up_field)
 	{
 		// File f = new File(up_field);
 		try
 		{
 			Util.validateUploadFileName(up_field);
 		}
 		catch (MeleteException me)
 		{
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Checks the size of file. If > than max size allowed then show error message.
 	 * 
 	 * @param sz
 	 *        Size of uploaded file
 	 * @throws MeleteException
 	 */
 	public void validateFileSize(long sz) throws MeleteException
 	{
 		// 1 MB = 1048576 bytes
 		if (new Long((sz / 1048576)).intValue() > getMaxUploadSize()) throw new MeleteException("file_too_large");
 	}
 
 	/**
 	 *Get uploads collection so that save.jsp knows where to upload items.
 	 * 
 	 * @param courseId
 	 *        The site Id
 	 * @return
 	 */
 	public String getCollectionId(String courseId)
 	{
 		return getMeleteCHService().getUploadCollectionId(courseId);
 	}
 
 	/**
 	 * 
 	 * Records Sferyx embedded resource to melete resource table
 	 * 
 	 * @param sectionId
 	 *        The section Id
 	 * @param resourceId
 	 *        The resource Id
 	 */
 	public void addtoMeleteResource(String sectionId, String resourceId) throws Exception
 	{
 		getMeleteCHService().addToMeleteResource(sectionId, resourceId);
 	}
 
 	/**
 	 * Saves/creates the section_xxx.html file for sferyx editor. If Section_xxx.html resource doesn't exist for add section or when content is added to a notype section then this method creates the resource item and adds to melete resource table.
 	 * 
 	 * @param UploadCollId
 	 *        The collection id
 	 * @param courseId
 	 *        The Site Id
 	 * @param resourceId
 	 *        The resource Id
 	 * @param sectionId
 	 *        The section Id
 	 * @param userId
 	 *        The user Id
 	 * @param edited
 	 * 		  User made changes to editor's content	       
 	 * @param newEmbeddedResources
 	 *        Map of all embedded resources. Keyed with local file name.
 	 * @param htmlContentData
 	 *        Composed content
 	 */
 	public void saveSectionHtmlItem(String UploadCollId, String courseId, String resourceId, String sectionId, String userId, String lastSaveTime, String edited,
 			Map<String, String> newEmbeddedResources, String htmlContentData) throws Exception
 	{
 		ArrayList<String> errs = new ArrayList<String>();
 		
 		//check for overwrite
 		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
 		Date checkLastWork = sectionService.getLastModifiedDate(Integer.parseInt(sectionId));
 
 		if (checkLastWork != null && checkLastWork.compareTo(df.parse(lastSaveTime)) > 0)
 		{
 			return;
 		}
 		try
 		{			
 			// in case of add and edit from notype to compose section
 			if (resourceId == null || resourceId.length() == 0)
 			{
 				resourceId = getMeleteCHService().getSectionResource(sectionId);
 				if (resourceId == null) throw new MeleteException("resource_null");
 			}
 			// In case type is change from typelink or typeUploads to compose
 			// for sections collection is module_id
 			if (resourceId.indexOf("/private/meleteDocs/") != -1 && resourceId.indexOf("/uploads/") != -1)
 				throw new MeleteException("section_html_null");
 			
 			String revisedData = getMeleteCHService().findLocalImagesEmbeddedInEditor(courseId, errs, newEmbeddedResources, htmlContentData);
 
 				// add messages to hashmap
 				if (errs.size() > 0)
 				{
 					for (String err : errs)
 					{
 						String k = sectionId + "-" + userId;
 						addToHm_Msgs(k, err);
 					}
 				}
 				getMeleteCHService().editResource(courseId, resourceId, revisedData);
 			
 		}
 		catch (Exception ex)
 		{
 			htmlContentData = getMeleteCHService().findLocalImagesEmbeddedInEditor(courseId, errs, newEmbeddedResources, htmlContentData);
 			byte[] secContentData = htmlContentData.getBytes();
 
 			String secResourceName = getMeleteCHService().getTypeEditorSectionName(new Integer(sectionId));
 			ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(true, secResourceName, "compose content");
 			String newResourceId = getMeleteCHService().addResourceItem(secResourceName, MeleteCHService.MIME_TYPE_EDITOR,
 					getMeleteCHService().getCollectionId(courseId, "typeEditor", getMeleteCHService().getContainingModule(sectionId)),
 					secContentData, res);
 			addtoMeleteResource(sectionId, newResourceId);			
 		}		
 	}
 
 	/**
 	 * 
 	 * Fetch section's data to show in Sferyx editor
 	 * 
 	 * @param sectionId
 	 *        The section Id
 	 */
 	public String getResourceData(String sectionId)
 	{
 		String data = null;
 		try
 		{
			if (sectionId == null || sectionId.length() == 0) return data;
 			String resourceId = getMeleteCHService().getSectionResource(sectionId);
 			logger.debug("resource id in AddResource getdata method:" + resourceId);
 			ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 			data = bundle.getString("compose_content");
 
 			if (resourceId == null || resourceId.length() == 0) return data;
 			ContentResource cr = getMeleteCHService().getResource(resourceId);
 
 			if (cr != null && "text/html".equals(cr.getContentType()))
 			{
 				data = new String(cr.getContent());
 				data = java.net.URLEncoder.encode(data, "UTF-8");
 			}
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return data;
 	}
 
 	/**
 	 * Get the error messages while adding the embedded resources.
 	 * 
 	 * @return the map. Key is section_id-user-id and value is the error message
 	 */
 	public HashMap<String, ArrayList<String>> getHm_msgs()
 	{
 		return hm_msgs;
 	}
 
 	/**
 	 * Set for error messages.
 	 * 
 	 * @param hm_msgs
 	 */
 	public void setHm_msgs(HashMap<String, ArrayList<String>> hm_msgs)
 	{
 		this.hm_msgs = hm_msgs;
 	}
 
 	/**
 	 * Add a message. This records the bad file, large file messages when processing the composed data. Key is section_id-user-id and value is the error message
 	 */
 	public void addToHm_Msgs(String k, String o)
 	{
 		logger.debug("add to messages" + k + o);
 		if (hm_msgs == null) hm_msgs = new HashMap<String, ArrayList<String>>();
 
 		ArrayList<String> v = new ArrayList<String>();
 		if (hm_msgs.containsKey(k))
 		{
 			v = hm_msgs.get(k);
 		}
 		if (!v.contains(o)) v.add(o);
 		hm_msgs.put(k, v);
 	}
 
 	/**
 	 * After displaying the error message remove it.
 	 */
 	public void removeFromHm_Msgs(String k)
 	{
 		if (hm_msgs != null && hm_msgs.containsKey(k))
 		{
 			hm_msgs.remove(k);
 		}
 	}
 
 	/**
 	 * Get internationalized message to display through addMessageError page
 	 * 
 	 * @param errcode
 	 *        Error code
 	 */
 	public String getMessageText(String errcode)
 	{
 		ResourceLoader bundle = new ResourceLoader("org.etudes.tool.melete.bundle.Messages");
 		String msg = "";
 		if (("embed_image_size_exceed").equals(errcode))
 		{
 			msg = bundle.getString("embed_image_size_exceed");
 			msg = msg.concat(ServerConfigurationService.getString("content.upload.max", "0"));
 			msg = msg.concat(bundle.getString("embed_image_size_exceed1"));
 		}
 		else if (("embed_image_size_exceed2").equals(errcode))
 		{
 			msg = bundle.getString("embed_image_size_exceed2");
 			msg = msg.concat(ServerConfigurationService.getString("content.upload.max", "0"));
 			msg = msg.concat(bundle.getString("embed_image_size_exceed2-1"));
 		}
 		else
 			msg = bundle.getString(errcode);
 		return msg;
 	}
 
 	/**
 	 * create error map
 	 */
 	public void contextInitialized(ServletContextEvent event)
 	{
 		hm_msgs = new HashMap<String, ArrayList<String>>();
 	}
 
 	/**
 	 * Delete the map
 	 */
 	public void contextDestroyed(ServletContextEvent event)
 	{
 		hm_msgs = null;
 	}
 
 }
