 /**********************************************************************************
  * $URL:  $
  * $Id:  $
  ***********************************************************************************
  *
  * Copyright (c) 2006, 2007 The Sakai Foundation.
  * 
  * Licensed under the Educational Community License, Version 1.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *      http://www.opensource.org/licenses/ecl1.php
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.content.tool;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.cheftool.Context;
 import org.sakaiproject.cheftool.JetspeedRunData;
 import org.sakaiproject.cheftool.RunData;
 import org.sakaiproject.cheftool.VelocityPortlet;
 import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.api.MultiFileUploadPipe;
 import org.sakaiproject.content.api.ResourceToolAction;
 import org.sakaiproject.content.api.ResourceToolActionPipe;
 import org.sakaiproject.content.api.ResourceType;
 import org.sakaiproject.content.cover.ContentHostingService;
 import org.sakaiproject.content.api.ContentTypeImageService;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.event.api.SessionState;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.time.api.Time;
 import org.sakaiproject.time.cover.TimeService;
 import org.sakaiproject.tool.api.Tool;
 import org.sakaiproject.tool.api.ToolException;
 import org.sakaiproject.tool.api.ToolSession;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.util.FileItem;
 import org.sakaiproject.util.FormattedText;
 import org.sakaiproject.util.ParameterParser;
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.util.StringUtil;
 import org.sakaiproject.util.Validator;
 
 public class ResourcesHelperAction extends VelocityPortletPaneledAction 
 {
 	/** the logger for this class */
     private static final Log logger = LogFactory.getLog(ResourcesHelperAction.class);
     
 	/** Resource bundle using current language locale */
 	private static ResourceLoader rb = new ResourceLoader("types");
 	
 	protected  static final String ACCESS_HTML_TEMPLATE = "resources/sakai_access_html";
 
 	protected  static final String ACCESS_TEXT_TEMPLATE = "resources/sakai_access_text";
 
 	protected  static final String ACCESS_UPLOAD_TEMPLATE = "resources/sakai_access_upload";
 	protected  static final String ACCESS_URL_TEMPLATE = "resources/sakai_access_url";
 	
 	/** copyright path -- MUST have same value as AccessServlet.COPYRIGHT_PATH */
 	public static final String COPYRIGHT_PATH = Entity.SEPARATOR + "copyright";
 	private static final String COPYRIGHT_ALERT_URL = ServerConfigurationService.getAccessUrl() + COPYRIGHT_PATH;
 	
 	protected  static final String CREATE_FOLDERS_TEMPLATE = "resources/sakai_create_folders";
 	protected  static final String CREATE_HTML_TEMPLATE = "resources/sakai_create_html";
 	protected  static final String CREATE_TEXT_TEMPLATE = "resources/sakai_create_text";
 	protected  static final String CREATE_UPLOAD_TEMPLATE = "resources/sakai_create_upload";
 	protected  static final String CREATE_UPLOADS_TEMPLATE = "resources/sakai_create_uploads";
 	protected  static final String CREATE_URL_TEMPLATE = "resources/sakai_create_url";
 	protected static final String CREATE_URLS_TEMPLATE = "resources/sakai_create_urls";
 	
 	public static final String MODE_MAIN = "main";
 	protected static final String PREFIX = "ResourceTypeHelper.";
 	
 	protected  static final String REVISE_HTML_TEMPLATE = "resources/sakai_revise_html";
 	protected  static final String REVISE_TEXT_TEMPLATE = "resources/sakai_revise_text";
 	protected  static final String REVISE_UPLOAD_TEMPLATE = "resources/sakai_revise_upload";
 	protected  static final String REVISE_URL_TEMPLATE = "resources/sakai_revise_url";
 	
 	protected static final String REPLACE_CONTENT_TEMPLATE = "resources/sakai_replace_file";
 
 	/** The content type image lookup service in the State. */
 	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = PREFIX + "content_type_image_service";
 	
 	private static final String STATE_COPYRIGHT_FAIRUSE_URL = PREFIX + "copyright_fairuse_url";
 
 	private static final String STATE_COPYRIGHT_NEW_COPYRIGHT = PREFIX + "new_copyright";
 	
 	/** copyright related info */
 	private static final String STATE_COPYRIGHT_TYPES = PREFIX + "copyright_types";
 
 	private static final String STATE_DEFAULT_COPYRIGHT = PREFIX + "default_copyright";
 	
 	private static final String STATE_DEFAULT_COPYRIGHT_ALERT = PREFIX + "default_copyright_alert";
 	
 	/** The user copyright string */
 	private static final String	STATE_MY_COPYRIGHT = PREFIX + "mycopyright";
 	
 	private static final String STATE_NEW_COPYRIGHT_INPUT = PREFIX + "new_copyright_input";
   
 	/** state attribute indicating whether users in current site should be denied option of making resources public */
 	private static final String STATE_PREVENT_PUBLIC_DISPLAY = PREFIX + "prevent_public_display";
 	
 	/** state attribute indicating whether we're using the Creative Commons dialog instead of the "old" copyright dialog */
 	protected static final String STATE_USING_CREATIVE_COMMONS = PREFIX + "usingCreativeCommons";
 	
 	/** name of state attribute for the default retract time */
 	protected static final String STATE_DEFAULT_RETRACT_TIME = PREFIX + "default_retract_time";
 
 	public String buildAccessContext(VelocityPortlet portlet,
 			Context context,
 			RunData data,
 			SessionState state)
 	{
 		String template = ACCESS_TEXT_TEMPLATE;
 		return template;
 	}
 
 
 	
 	public String buildCreateContext(VelocityPortlet portlet,
 			Context context,
 			RunData data,
 			SessionState state)
 	{
 		String template = CREATE_UPLOAD_TEMPLATE;
 		
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 
 		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 
 		//Reference reference = (Reference) toolSession.getAttribute(ResourceToolAction.COLLECTION_REFERENCE);
 		String typeId = pipe.getAction().getTypeId();
 
 		if(ResourceType.TYPE_TEXT.equals(typeId))
 		{
 			template = CREATE_TEXT_TEMPLATE;
 		}
 		else if(ResourceType.TYPE_HTML.equals(typeId))
 		{
 			template = CREATE_HTML_TEMPLATE;
 		}
 		else if(ResourceType.TYPE_URL.equals(typeId))
 		{
 			template = CREATE_URL_TEMPLATE;
 		}
 		else // assume ResourceType.TYPE_UPLOAD
 		{
 			template = CREATE_UPLOAD_TEMPLATE;
 		}
 		
 		return template;
 	}
 
 	public String buildMainPanelContext(VelocityPortlet portlet,
 			Context context,
 			RunData data,
 			SessionState state)
 	{
 		// context.put("sysout", System.out);
 		context.put("tlang", rb);
 		
 		context.put("validator", new Validator());
 		
 		if(state.getAttribute(ResourcesAction.STATE_MESSAGE) != null)
 		{
 			context.put("itemAlertMessage", state.getAttribute(ResourcesAction.STATE_MESSAGE));
 			state.removeAttribute(ResourcesAction.STATE_MESSAGE);
 		}
 		
 		ContentTypeImageService contentTypeImageService = (ContentTypeImageService) state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE);
 		context.put("contentTypeImageService", contentTypeImageService);
 		
 		String mode = (String) state.getAttribute(ResourceToolAction.STATE_MODE);
 
 		if (mode == null)
 		{
 			initHelper(portlet, context, data, state);
 		}
 
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		if(pipe.isActionCompleted())
 		{
 			return null;
 		}
 
 		String actionId = pipe.getAction().getId();
 		
 		String template = "";
 
 		switch(pipe.getAction().getActionType())
 		{
 		case CREATE:
 			template = buildCreateContext(portlet, context, data, state);
 			break;
 		case REVISE_CONTENT:
 			template = buildReviseContext(portlet, context, data, state);
 			break;
 		case REPLACE_CONTENT:
 			template = buildReplaceContext(portlet, context, data, state);
 			break;
 		case NEW_UPLOAD:
 			template = buildUploadFilesContext(portlet, context, data, state);
 			break;
 		case NEW_FOLDER:
 			template = buildNewFoldersContext(portlet, context, data, state);
 			break;
 		case NEW_URLS:
 			template = buildNewUrlsContext(portlet, context, data, state);
 			break;
 		default:
 			// hmmmm
 			break;
 		}
 		
 		return template;
 	}
 
 	protected String buildNewUrlsContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
     {
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 
 		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		
 		List<ResourceToolActionPipe> pipes = pipe.getPipes();
 		
 		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
 		if(defaultRetractDate == null)
 		{
 			defaultRetractDate = TimeService.newTime();
 			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
 		}
 
 		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
 		if(preventPublicDisplay == null)
 		{
 			preventPublicDisplay = Boolean.FALSE;
 			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
 		}
 		
 		ListItem parent = new ListItem(pipe.getContentEntity());
 		parent.setPubviewPossible(! preventPublicDisplay);
 		ListItem model = new ListItem(pipe, parent, defaultRetractDate);
 		model.setPubviewPossible(! preventPublicDisplay);
 				
 		context.put("model", model);
 		
 		context.put("pipes", pipes);
 		
 		if(ContentHostingService.isAvailabilityEnabled())
 		{
 			context.put("availability_is_enabled", Boolean.TRUE);
 		}
 		
 		
 		ResourcesAction.copyrightChoicesIntoContext(state, context);
 		ResourcesAction.publicDisplayChoicesIntoContext(state, context);
 		
 		return CREATE_URLS_TEMPLATE;
     }
 
 
 
 	/**
 	 * @param portlet
 	 * @param context
 	 * @param data
 	 * @param state
 	 * @return
 	 */
 	private String buildNewFoldersContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
 	{
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 
 		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		
 		List<ResourceToolActionPipe> pipes = pipe.getPipes();
 
 		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
 		if(defaultRetractDate == null)
 		{
 			defaultRetractDate = TimeService.newTime();
 			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
 		}
 
 		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
 		if(preventPublicDisplay == null)
 		{
 			preventPublicDisplay = Boolean.FALSE;
 			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
 		}
 		
 		ListItem parent = new ListItem(pipe.getContentEntity());
 		parent.setPubviewPossible(! preventPublicDisplay);
 		ListItem model = new ListItem(pipe, parent, defaultRetractDate);
 		model.setPubviewPossible(! preventPublicDisplay);
 		
 		context.put("model", model);
 		
 		context.put("pipes", pipes);
 		
 		if(ContentHostingService.isAvailabilityEnabled())
 		{
 			context.put("availability_is_enabled", Boolean.TRUE);
 		}	
 
 		ResourcesAction.publicDisplayChoicesIntoContext(state, context);
 
 		return CREATE_FOLDERS_TEMPLATE;
 	}
 
 
 
 	/**
 	 * @param portlet
 	 * @param context
 	 * @param data
 	 * @param state
 	 * @return
 	 */
 	protected String buildReplaceContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
 	{
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 
 		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		
 		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
 		if(preventPublicDisplay == null)
 		{
 			preventPublicDisplay = Boolean.FALSE;
 			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
 		}
 		
 		ListItem item = new ListItem(pipe.getContentEntity());
 		item.setPubviewPossible(! preventPublicDisplay);
 		
 		context.put("item", item);
 		
 		return REPLACE_CONTENT_TEMPLATE;
 	}
 
 
 
 	public String buildReviseContext(VelocityPortlet portlet,
 			Context context,
 			RunData data,
 			SessionState state)
 	{
 		String template = REVISE_TEXT_TEMPLATE;
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 
 		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 
 		//Reference reference = (Reference) toolSession.getAttribute(ResourceToolAction.COLLECTION_REFERENCE);
 		String typeId = pipe.getAction().getTypeId();
 		String mimetype = pipe.getMimeType();
 		
 		context.put("pipe", pipe);
 
 		if(ResourceType.TYPE_TEXT.equals(typeId))
 		{
 			template = REVISE_TEXT_TEMPLATE;
 		}
 		else if(ResourceType.TYPE_HTML.equals(typeId))
 		{
 			template = REVISE_HTML_TEMPLATE;
 		}
 		else if(ResourceType.TYPE_URL.equals(typeId))
 		{
 			template = REVISE_URL_TEMPLATE;
 		}
 		else if(ResourceType.TYPE_UPLOAD.equals(typeId) && mimetype != null && ResourceType.MIME_TYPE_HTML.equals(mimetype))
 		{
 			template = REVISE_HTML_TEMPLATE;
 		}
 		else if(ResourceType.TYPE_UPLOAD.equals(typeId) && mimetype != null && ResourceType.MIME_TYPE_TEXT.equals(mimetype))
 		{
 			template = REVISE_TEXT_TEMPLATE;
 		}
 		else // assume ResourceType.TYPE_UPLOAD
 		{
 			template = REVISE_UPLOAD_TEMPLATE;
 		}
 		
 		return template;
 	}
 
 	/**
 	 * @param portlet
 	 * @param context
 	 * @param data
 	 * @param state
 	 * @return
 	 */
 	protected String buildUploadFilesContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
 	{
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 
 		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		
 		List<ResourceToolActionPipe> pipes = pipe.getPipes();
 		
 		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
 		if(defaultRetractDate == null)
 		{
 			defaultRetractDate = TimeService.newTime();
 			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
 		}
 
 		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
 		if(preventPublicDisplay == null)
 		{
 			preventPublicDisplay = Boolean.FALSE;
 			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
 		}
 		
 		ListItem parent = new ListItem(pipe.getContentEntity());
 		parent.setPubviewPossible(! preventPublicDisplay);
 		ListItem model = new ListItem(pipe, parent, defaultRetractDate);
 		model.setPubviewPossible(! preventPublicDisplay);
 				
 		context.put("model", model);
 		
 		context.put("pipes", pipes);
 		
 		if(ContentHostingService.isAvailabilityEnabled())
 		{
 			context.put("availability_is_enabled", Boolean.TRUE);
 		}
 		
 		
 		ResourcesAction.copyrightChoicesIntoContext(state, context);
 		ResourcesAction.publicDisplayChoicesIntoContext(state, context);
 		
 		String defaultCopyrightStatus = (String) state.getAttribute(STATE_DEFAULT_COPYRIGHT);
 		if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
 		{
 			defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
 			state.setAttribute(STATE_DEFAULT_COPYRIGHT, defaultCopyrightStatus);
 		}
 
 		context.put("defaultCopyrightStatus", defaultCopyrightStatus);
 	
 		
 		
 
 		return CREATE_UPLOADS_TEMPLATE;
 	}
 
 	public void doCancel(RunData data)
 	{
 		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
 		ParameterParser params = data.getParameters ();
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 		
 		//Tool tool = ToolManager.getCurrentTool();
 		//String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
 		//toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
 		
 		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		pipe.setActionCanceled(true);
 		pipe.setErrorEncountered(false);
 		pipe.setActionCompleted(true);
 
 		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
 		
 		
 	}
 	
 	public void doContinue(RunData data)
 	{
 		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
 		ParameterParser params = data.getParameters ();
 
 		String content = params.getString("content");
 		if(content == null)
 		{
 			addAlert(state, rb.getString("text.notext"));
 			return;
 		}
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 		
 //		Tool tool = ToolManager.getCurrentTool();
 //		String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
 //		toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
 
 		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		
 		String resourceType = pipe.getAction().getTypeId();
 		String mimetype = pipe.getMimeType();
 		
 		pipe.setRevisedMimeType(pipe.getMimeType());
 		if(ResourceType.TYPE_TEXT.equals(resourceType) || ResourceType.MIME_TYPE_TEXT.equals(mimetype))
 		{
 			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_TEXT);
 			pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
 
 		}
 		else if(ResourceType.TYPE_HTML.equals(resourceType) || ResourceType.MIME_TYPE_HTML.equals(mimetype))
 		{
 			StringBuffer alertMsg = new StringBuffer();
 			content = FormattedText.processHtmlDocument(content, alertMsg);
 			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_HTML);
 			pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
 			if (alertMsg.length() > 0)
 			{
 				addAlert(state, alertMsg.toString());
 				return;
 			}
 		}
 		else if(ResourceType.TYPE_URL.equals(resourceType))
 		{
 			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_URL);
 		}
 		else if(ResourceType.TYPE_FOLDER.equals(resourceType))
 		{
 			MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
 			int count = params.getInt("folderCount");
 			mfp.setFileCount(count);
 			
 			List<ResourceToolActionPipe> pipes = mfp.getPipes();
 			for(int i = 0; i < pipes.size(); i++)
 			{
 				ResourceToolActionPipe fp = pipes.get(i);
 				String folderName = params.getString("folder" + (i + 1));
 				fp.setFileName(folderName);
 			}
 		}
 	
 		pipe.setRevisedContent(content.getBytes());
 		pipe.setActionCanceled(false);
 		pipe.setErrorEncountered(false);
 		pipe.setActionCompleted(true);
 		
 		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
 
 	}
 	
 	public void doCreateFolders(RunData data)
 	{
 		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
 		ParameterParser params = data.getParameters ();
 
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 		
 		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		
 		String resourceType = pipe.getAction().getTypeId();
 		
 		int count = params.getInt("fileCount");
 		pipe.setFileCount(count);
 		
 		int lastIndex = params.getInt("lastIndex");
 		
 		List<ResourceToolActionPipe> pipes = pipe.getPipes();
 		
 		for(int i = 1, c = 0; i <= lastIndex && c < count; i++)
 		{
 			String exists = params.getString("exists." + i);
 			if(exists == null || exists.equals(""))
 			{
 				continue;
 			}
 			ResourceToolActionPipe fp = pipes.get(c);
 			String folderName = params.getString("content." + i);
 			
 			fp.setFileName(folderName);
 			
 			ListItem newFolder = new ListItem(folderName);
 			
 			// capture properties
 			newFolder.captureProperties(params, "." + i);
 			
 			fp.setRevisedListItem(newFolder);
 			
 			c++;
 		}
 
 		pipe.setActionCanceled(false);
 		pipe.setErrorEncountered(false);
 		pipe.setActionCompleted(true);
 		
 		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
 
 	}
 
 	public void doReplace(RunData data)
 	{
 		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
 		ParameterParser params = data.getParameters ();
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 		
 
 		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		
 			
 		FileItem fileitem = null;
 		try
 		{
 			fileitem = params.getFileItem("content");
 		}
 		catch(Exception e)
 		{
 			logger.warn("Exception ", e);
 		}
 		
 		if(fileitem == null)
 		{
 			String max_file_size_mb = (String) state.getAttribute(ResourcesAction.STATE_FILE_UPLOAD_MAX_SIZE);
 			int max_bytes = 1024 * 1024;
 			try
 			{
 				max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
 			}
 			catch(Exception e)
 			{
 				// if unable to parse an integer from the value
 				// in the properties file, use 1 MB as a default
 				max_file_size_mb = "1";
 				max_bytes = 1024 * 1024;
 			}
 			
 			String max_bytes_string = ResourcesAction.getFileSizeString(max_bytes, rb);
 			// "The user submitted a file to upload but it was too big!"
 			addAlert(state, rb.getFormattedMessage("size.exceeded", new Object[]{ max_bytes_string }));
 			//max_file_size_mb + "MB " + rb.getString("exceeded2"));
 		}
 		else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
 		{
 			addAlert(state, rb.getString("choosefile7"));
 		}
 		else if (fileitem.getFileName().length() > 0)
 		{
 			String filename = Validator.getFileName(fileitem.getFileName());
 			InputStream stream;
             stream = fileitem.getInputStream();
             if(stream == null)
             {
             	byte[] bytes = fileitem.get();
             	pipe.setRevisedContent(bytes);
             }
             else
             {
                 pipe.setRevisedContentStream(stream);
             }
             String contentType = fileitem.getContentType();
             //pipe.setRevisedContent(bytes);
             pipe.setRevisedMimeType(contentType);
             pipe.setFileName(filename);
             
             if(ResourceType.MIME_TYPE_HTML.equals(contentType) || ResourceType.MIME_TYPE_TEXT.equals(contentType))
             {
             	pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
             }
             else if(pipe.getPropertyValue(ResourceProperties.PROP_CONTENT_ENCODING) != null)
             {
             	pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, (String) pipe.getPropertyValue(ResourceProperties.PROP_CONTENT_ENCODING));
             }
             
 			ListItem newFile = new ListItem(filename);
 			
 			pipe.setRevisedListItem(newFile);
 			
 			pipe.setActionCanceled(false);
 			pipe.setErrorEncountered(false);
 			pipe.setActionCompleted(true);
 			
 			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
 		}
 
 
 	}
 	
 	public void doAddUrls(RunData data)
 	{
 		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
 		ParameterParser params = data.getParameters ();
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 		
 
 		MultiFileUploadPipe mfp = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		
 		int count = params.getInt("fileCount");
 		mfp.setFileCount(count);
 		
 		int lastIndex = params.getInt("lastIndex");
 		
 		List<ResourceToolActionPipe> pipes = mfp.getPipes();
 		
 		int actualCount = 0;
 		for(int i = 1; i <= lastIndex && actualCount < count; i++)
 		{
 			String exists = params.getString("exists." + i);
 			if(exists == null || exists.equals(""))
 			{
 				continue;
 			}
 			
 			ResourceToolActionPipe pipe = pipes.get(actualCount);
 			
 			String url = params.getString("content." + i );
             if(url == null)
             {
             	continue;
             }
             else
             {
             	try
                 {
 	                url = ResourcesAction.validateURL(url);
                 }
                 catch (MalformedURLException e)
                 {
                 	addAlert(state, rb.getFormattedMessage("url.invalid", new String[]{url}));
 	                continue;
                 }
             	
                 pipe.setRevisedContent(url.getBytes());
             }
             
             pipe.setFileName(Validator.escapeResourceName(url));
             pipe.setRevisedMimeType(ResourceType.MIME_TYPE_URL);
             
 			ListItem newFile = new ListItem(pipe.getFileName());
 			
 			// capture properties
 			newFile.captureProperties(params, "." + i);
 			            
 			pipe.setRevisedListItem(newFile);
     			
 			actualCount++;
 			
 		}
 		if(actualCount < 1)
 		{
 			addAlert(state, rb.getString("url.noinput"));
 			return;
 		}
 
 		mfp.setActionCanceled(false);
 		mfp.setErrorEncountered(false);
 		mfp.setActionCompleted(true);
 		
 		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
 
 	}
 	
 	
 	public void doUpload(RunData data)
 	{
 		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
 		ParameterParser params = data.getParameters ();
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 		
 
 		MultiFileUploadPipe mfp = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
 		
 		int count = params.getInt("fileCount");
 		mfp.setFileCount(count);
 		
 		int lastIndex = params.getInt("lastIndex");
 		
 		List<ResourceToolActionPipe> pipes = mfp.getPipes();
 		
 		int uploadCount = 0;
 		
 		for(int i = 1, c = 0; i <= lastIndex && c < count; i++)
 		{
 			String exists = params.getString("exists." + i);
 			if(exists == null || exists.equals(""))
 			{
 				continue;
 			}
 			
 			ResourceToolActionPipe pipe = pipes.get(c);
 			
 			FileItem fileitem = null;
 			try
 			{
 				fileitem = params.getFileItem("content." + i );
 			}
 			catch(Exception e)
 			{
 				logger.warn("Exception ", e);
 			}
 			
 			if(fileitem == null)
 			{
 				String max_file_size_mb = (String) state.getAttribute(ResourcesAction.STATE_FILE_UPLOAD_MAX_SIZE);
 				int max_bytes = 1024 * 1024;
 				try
 				{
 					max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
 				}
 				catch(Exception e)
 				{
 					// if unable to parse an integer from the value
 					// in the properties file, use 1 MB as a default
 					max_file_size_mb = "1";
 					max_bytes = 1024 * 1024;
 				}
 				
 				String max_bytes_string = ResourcesAction.getFileSizeString(max_bytes, rb);
 				// "The user submitted a file to upload but it was too big!"
 				addAlert(state, rb.getFormattedMessage("size.exceeded", new Object[]{ max_bytes_string }));
 				//max_file_size_mb + "MB " + rb.getString("exceeded2"));
 			}
 			else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
 			{
 				// no file selected -- skip this one
 			}
 			else if (fileitem.getFileName().length() > 0)
 			{
 				String filename = Validator.getFileName(fileitem.getFileName());
 				InputStream stream;
                 stream = fileitem.getInputStream();
                 if(stream == null)
                 {
                 	byte[] bytes = fileitem.get();
                 	pipe.setRevisedContent(bytes);
                 }
                 else
                 {
                     pipe.setRevisedContentStream(stream);
                 }
                 String contentType = fileitem.getContentType();
                 //pipe.setRevisedContent(bytes);
                 pipe.setRevisedMimeType(contentType);
                 if(ResourceType.MIME_TYPE_HTML.equals(contentType) || ResourceType.MIME_TYPE_TEXT.equals(contentType))
                 {
                 	pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
                 }
                 else if(pipe.getPropertyValue(ResourceProperties.PROP_CONTENT_ENCODING) != null)
                 {
                 	pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, (String) pipe.getPropertyValue(ResourceProperties.PROP_CONTENT_ENCODING));
                 }
                 
                 pipe.setFileName(filename);
                 
     			ListItem newFile = new ListItem(filename);
     			
     			// capture properties
     			newFile.captureProperties(params, "." + i);
     			
     			pipe.setRevisedListItem(newFile);
     			
     			uploadCount++;
     			
 			}
 			c++;
 			
 		}
 		
		if(uploadCount < 1 && state.getAttribute(ResourcesAction.STATE_MESSAGE) == null)
 		{
 			addAlert(state, rb.getString("choosefile7"));
 		}
 
 		if(state.getAttribute(ResourcesAction.STATE_MESSAGE) == null)
 		{
 			mfp.setActionCanceled(false);
 			mfp.setErrorEncountered(false);
 			mfp.setActionCompleted(true);
 			
 			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
 		}
 
 	}
 	
 	protected void initHelper(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 		//toolSession.setAttribute(ResourceToolAction.STARTED, Boolean.TRUE);
 		//state.setAttribute(ResourceToolAction.STATE_MODE, MODE_MAIN);
 		if(state.getAttribute(STATE_USING_CREATIVE_COMMONS) == null)
 		{
 			String usingCreativeCommons = ServerConfigurationService.getString("copyright.use_creative_commons");
 			if( usingCreativeCommons != null && usingCreativeCommons.equalsIgnoreCase(Boolean.TRUE.toString()))
 			{
 				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.TRUE.toString());
 			}
 			else
 			{
 				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.FALSE.toString());
 			}
 		}
 
 		if (state.getAttribute(STATE_COPYRIGHT_TYPES) == null)
 		{
 			if (ServerConfigurationService.getStrings("copyrighttype") != null)
 			{
 				state.setAttribute(STATE_COPYRIGHT_TYPES, new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("copyrighttype"))));
 			}
 		}
 
 		if (state.getAttribute(STATE_DEFAULT_COPYRIGHT) == null)
 		{
 			if (ServerConfigurationService.getString("default.copyright") != null)
 			{
 				state.setAttribute(STATE_DEFAULT_COPYRIGHT, ServerConfigurationService.getString("default.copyright"));
 			}
 		}
 
 		if (state.getAttribute(STATE_DEFAULT_COPYRIGHT_ALERT) == null)
 		{
 			if (ServerConfigurationService.getString("default.copyright.alert") != null)
 			{
 				state.setAttribute(STATE_DEFAULT_COPYRIGHT_ALERT, ServerConfigurationService.getString("default.copyright.alert"));
 			}
 		}
 
 		if (state.getAttribute(STATE_NEW_COPYRIGHT_INPUT) == null)
 		{
 			if (ServerConfigurationService.getString("newcopyrightinput") != null)
 			{
 				state.setAttribute(STATE_NEW_COPYRIGHT_INPUT, ServerConfigurationService.getString("newcopyrightinput"));
 			}
 		}
 
 		if (state.getAttribute(STATE_COPYRIGHT_FAIRUSE_URL) == null)
 		{
 			if (ServerConfigurationService.getString("fairuse.url") != null)
 			{
 				state.setAttribute(STATE_COPYRIGHT_FAIRUSE_URL, ServerConfigurationService.getString("fairuse.url"));
 			}
 		}
 
 		if (state.getAttribute(STATE_COPYRIGHT_NEW_COPYRIGHT) == null)
 		{
 			if (ServerConfigurationService.getString("copyrighttype.new") != null)
 			{
 				state.setAttribute(STATE_COPYRIGHT_NEW_COPYRIGHT, ServerConfigurationService.getString("copyrighttype.new"));
 			}
 		}
 
 		state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.FALSE);
 		String[] siteTypes = ServerConfigurationService.getStrings("prevent.public.resources");
 		String siteType = null;
 		Site site;
 		try
 		{
 			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
 			siteType = site.getType();
 			if(siteTypes != null)
 			{
 				for(int i = 0; i < siteTypes.length; i++)
 				{
 					if ((StringUtil.trimToNull(siteTypes[i])).equals(siteType))
 					{
 						state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.TRUE);
 						break;
 					}
 				}
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			// allow public display
 		}
 		catch(NullPointerException e)
 		{
 			// allow public display
 		}
 
 		state.setAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE, org.sakaiproject.content.cover.ContentTypeImageService.getInstance());
 	}
 	
 	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
 		throws ToolException
 	{
 		SessionState sstate = getState(req);
 		ToolSession toolSession = SessionManager.getCurrentToolSession();
 		
 		//String mode = (String) sstate.getAttribute(ResourceToolAction.STATE_MODE);
 		//Object started = toolSession.getAttribute(ResourceToolAction.STARTED);
 		Object done = toolSession.getAttribute(ResourceToolAction.DONE);
 		
 		if (done != null)
 		{
 			toolSession.removeAttribute(ResourceToolAction.STARTED);
 			Tool tool = ToolManager.getCurrentTool();
 		
 			String url = (String) SessionManager.getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
 		
 			SessionManager.getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
 		
 			try
 			{
 				res.sendRedirect(url);
 			}
 			catch (IOException e)
 			{
 				// Log.warn("chef", this + " : ", e);
 			}
 			return;
 		}
 		
 		super.toolModeDispatch(methodBase, methodExt, req, res);
 	}
 
 }
