 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
 
 package org.sakaiproject.discussion.tool;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.sakaiproject.authz.api.PermissionsHelper;
 import org.sakaiproject.cheftool.Context;
 import org.sakaiproject.cheftool.JetspeedRunData;
 import org.sakaiproject.cheftool.PortletConfig;
 import org.sakaiproject.cheftool.RunData;
 import org.sakaiproject.cheftool.VelocityPortlet;
 import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
 import org.sakaiproject.cheftool.api.Menu;
 import org.sakaiproject.cheftool.api.MenuItem;
 import org.sakaiproject.cheftool.menu.MenuEntry;
 import org.sakaiproject.cheftool.menu.MenuImpl;
 import org.sakaiproject.content.api.ContentTypeImageService;
 import org.sakaiproject.content.api.FilePickerHelper;
 import org.sakaiproject.discussion.api.DiscussionChannel;
 import org.sakaiproject.discussion.api.DiscussionChannelEdit;
 import org.sakaiproject.discussion.api.DiscussionMessage;
 import org.sakaiproject.discussion.api.DiscussionMessageEdit;
 import org.sakaiproject.discussion.api.DiscussionMessageHeader;
 import org.sakaiproject.discussion.api.DiscussionMessageHeaderEdit;
 import org.sakaiproject.discussion.cover.DiscussionService;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.entity.cover.EntityManager;
 import org.sakaiproject.event.api.SessionState;
 import org.sakaiproject.exception.IdInvalidException;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.InUseException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.time.cover.TimeService;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.util.FormattedText;
 import org.sakaiproject.util.ParameterParser;
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.util.StringUtil;
 import org.sakaiproject.util.Validator;
 import org.sakaiproject.util.Web;
 
 /**
  * <p>
  * DiscussionAction is the action class for the dicussion tool.
  * </p>
  */
 public class DiscussionAction extends VelocityPortletPaneledAction
 {
 	/* reply type */
 	private static final String STATE_REPLY_TOPIC = "replyToTopic";
 
 	private static final String STATE_REPLY_MSG = "replyToMsgOnly";
 
 	/** Resource bundle using current language locale */
 	private static ResourceLoader rb = new ResourceLoader("discussion");
 
 	/** channel exist */
 	private static final String CHANNEL_EXIST = "channel_exist";
 
 	/** the attachments** */
 	private static final String ATTACHMENTS = "threadeddiscussionII.attachments";
 
 	/** the channel id ** */
 	private static final String STATE_CHANNEL_REF = "threadeddiscussionII.channel_id";
 
 	/** the state mode ** */
 	private static final String STATE_MODE = "threadeddiscussionII.state_mode";
 
 	/** the category show list ** */
 	private static final String STATE_CATEGORIES_SHOW_LIST = "threadeddiscussionII.state_category_show_list";
 
 	/** the sorted by ** */
 	private static final String STATE_SORTED_BY = "threadeddiscussionII.state_sorted_by";
 
 	/** sorted by topic * */
 	private static final String STATE_SORTED_BY_TOPIC = "topic";
 
 	/** sorted by author * */
 	private static final String STATE_SORTED_BY_AUTHOR = "author";
 
 	/** sorted by date * */
 	private static final String STATE_SORTED_BY_DATE = "date";
 
 	/** sorted by category alphabetically * */
 	private static final String STATE_SORTED_BY_CATEGORY_ALPHA = "category";
 
 	/** the sorted ascending * */
 	private static final String STATE_SORTED_ASC = "threadeddiscussionII.state_sorted_asc";
 
 	/** the expand all topic flag * */
 	private static final String STATE_EXPAND_ALL_TOPIC_FLAG = "threadeddiscussionII.state_expand_all_topic_flag";
 
 	/** the expand message flag * */
 	private static final String STATE_EXPAND_MESSAGE_LIST = "threadeddiscussionII.state_expand_message_list";
 
 	/** the expand category list * */
 	private static final String STATE_EXPAND_CATEGORY_LIST = "threadeddiscussionII.state_expand_category_list";
 
 	/** The content type image lookup service in the State. */
 	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = "attachment.content_type_image_service";
 
 	/** ********** new topic context ******************** */
 	/** the new topic category */
 	private static final String NEW_TOPIC_CATEGORY = "threadeddiscussionII.new_topic_category";
 
 	/** create new category? */
 	private static final String NEW_TOPIC_NEW_CATEGORY = "threadeddiscussionII.new_topic_new_category";
 
 	/** the new topic message body */
 	private static final String NEW_TOPIC_BODY = "threadeddiscussionII.new_topic_body";
 
 	/** the new topic subject */
 	private static final String NEW_TOPIC_SUBJECT = "threadeddiscussionII.new_topic_subject";
 
 	/** the new topic reply message style */
 	private static final String NEW_TOPIC_REPLY_STYLE = "threadeddiscussionII.new_topic_reply_style";
 
 	/** ********** draft topic context ******************** */
 	/** the draft message category */
 	private static final String DRAFT_MESSAGE_CATEGORY = "threadeddiscussionII.draft_message_category";
 
 	/** the draft message body */
 	private static final String DRAFT_MESSAGE_BODY = "threadeddiscussionII.draft_message_body";
 
 	/** the draft message subject */
 	private static final String DRAFT_MESSAGE_SUBJECT = "threadeddiscussionII.draft_message_subject";
 
 	/** the draft message reply style */
 	private static final String DRAFT_MESSAGE_REPLY_STYLE = "threadeddiscussionII.draft_message_reply_style";
 
 	/** ********* the respond context ******************** */
 	/** the respond message body */
 	private static final String RESPOND_BODY = "threadeddiscussionII.respond_body";
 
 	/** the respond message subject */
 	private static final String RESPOND_SUBJECT = "threadeddiscussionII.respond_subject";
 
 	/** the respond from */
 	private static final String RESPOND_REPLY_TO = "threadeddiscussionII.respond_reply_to";
 
 	/** the respond attachment */
 	private static final String RESPOND_ATTACHMENT = "threadeddiscussionII.respond_attachment";
 
 	/** The permission alert message eader */
 	/* private static final String PERMISSION_HEADER_STRING = "You are not allowed to "; */
 	private static final String PERMISSION_HEADER_STRING = rb.getString("youarenot5") + " ";
 
 	/** ************ delete message context ******************* */
 	/** the delete message id * */
 	private static final String DELETE_MESSAGE_ID = "threadeddiscussionII.delete_message_id";
 
 	private static final String DELETE_WARNING = "delete_message_warning";
 
 	/** portlet configuration parameter names. */
 	private static final String PARAM_CHANNEL = "channel";
 
 	private static final String PARAM_ASCENDING = "ascending";
 
 	/** Configure form field names. */
 	private static final String FORM_CHANNEL = "channel";
 
 	private static final String FORM_ASCENDING = "ascending";
 
 	/** Control form field names. */
 	private static final String FORM_MESSAGE = "message";
 
 	/** names and values of request parameters to select sub-panels */
 	private static final String MONITOR_PANEL = "List";
 
 	private static final String CONTROL_PANEL = "Control";
 
 	private static final String TOOLBAR_PANEL = "Toolbar";
 
 	private static final String NEXT_PANEL = "Next";
 
 	/** state attribute names. */
 	private static final String STATE_ASCENDING = "ascending";
 
 	private static final String STATE_ERROR = "error";
 
 	private static final String STATE_UPDATE = "update";
 
 	/** UI messages. */
 	private static final String PERMISSION_POST_MESSAGE = rb.getString("youdonot5");
 
 	private static final String STATE_DISPLAY_MESSAGE = "display_message";
 
 	private static final String STATE_LIST_PANNEL_UPDATED = "state_list_pannel_updated";
 
 	/** the category to be deleted */
 	private static final String DELETE_CATEGORY = "delete_category";
 
 	/** the id of the showing content topic */
 	private static final String STATE_SHOW_CONTENT_TOPIC_ID = "show_content_topic_id";
 
 	/** state mode when showing the topic content */
 	private static final String MODE_SHOW_TOPIC_CONTENT = "mode_show_topic_content";
 
 	/** the state flag of expand all */
 	private static final String STATE_EXPAND_ALL_FLAG = "state_expand_all_flag";
 
 	/** state mode when showing the new topic form */
 	private static final String MODE_NEW_TOPIC = "mode_new_topic";
 
 	/** state mode when showing the new category form */
 	private static final String MODE_NEW_CATEGORY = "mode_new_category";
 
 	/** state mode when showing the reply form */
 	private static final String MODE_REPLY = "mode_reply";
 
 	/** state mode when showing the reply preview */
 	private static final String MODE_REPLY_PREVIEW = "mode_reply_preview";
 
 	/** state mode when confirm deleting category */
 	private static final String MODE_DELETE_CATEGORY_CONFIRM = "mode_delete_category_confirm";
 
 	/** state mode when confirm deleting message */
 	private static final String MODE_DELETE_MESSAGE_CONFIRM = "mode_delete_message_confirm";
 
 	/** visited messages */
 	private static final String VISITED_MESSAGES = "visited_messages";
 
 	/** left_right layout */
 	private static final String STATE_LEFT_RIGHT_LAYOUT = "state_left_right_layout";
 
 	/** search criteria */
 	private static final String STATE_SEARCH = "state_search";
 
 	/** boolean value indicating the need to update the current message shown */
 	private static final String STATE_SEARCH_REFRESH = "state_search_refresh";
 
 	/** Form fields. */
 	protected static final String FORM_SEARCH = "search";
 
 	/** state selected view */
 	private static final String STATE_SELECTED_VIEW = "state_selected_view";
 
 	/** State attribute set when we need to go into permissions mode. */
 	private static final String STATE_PERMISSIONS = "sakai:discussion:permissions";
 
 	/**
 	 * Populate the state object, if needed.
 	 * 
 	 * @param config
 	 *        The portlet config.
 	 * @param pageSessionId
 	 *        The is of the current portal page session.
 	 * @param elementId
 	 *        The id of the portlet element that wants to be notified on change.
 	 */
 	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
 	{
 		super.initState(state, portlet, rundata);
 
 		PortletConfig config = portlet.getPortletConfig();
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		String channelExist = (String) state.getAttribute(CHANNEL_EXIST);
 		if (channelExist == null)
 		{
 
 			if (channelId == null)
 			{
 				// read the channel from configuration, or, if not specified, use the default for the page
 				channelId = StringUtil.trimToNull(config.getInitParameter("channel"));
 				if (channelId == null)
 				{
 					channelId = DiscussionService.channelReference(ToolManager.getCurrentPlacement().getContext(),
 							SiteService.MAIN_CONTAINER);
 				}
 				state.setAttribute(STATE_CHANNEL_REF, channelId);
 			}
 
 			DiscussionChannel channel = null;
 			try
 			{
 				channel = (DiscussionChannel) DiscussionService.getDiscussionChannel(channelId);
 			}
 			catch (IdUnusedException e)
 			{
 				try
 				{
 					DiscussionChannelEdit channelEdit = DiscussionService.addDiscussionChannel(channelId);
 					DiscussionService.commitChannel(channelEdit);
 					channel = (DiscussionChannel) channelEdit;
 				}
 				catch (IdUsedException ee)
 				{
 					Log.error("chef", this + "" + ee);
 				}
 				catch (IdInvalidException ee)
 				{
 					Log.error("chef", this + "" + ee);
 				}
 				catch (PermissionException ee)
 				{
 					Log.error("chef", this + "no permission");
 				}
 			}
 			catch (PermissionException e)
 			{
 			}
 
 			if (channel != null)
 			{
 				state.setAttribute(CHANNEL_EXIST, "true");
 
 				// TODO: Note: calling addCatagory here, which uses the state's STATE_EXPAND_CATEGORY_LIST which is not yet set is not good -ggolden
 				// // make sure that the channel has categories as seeded in the config
 				// String[] configCategories = parseCategoryString(StringUtil.trimToNull(config.getInitParameter("categories")));
 				// if (configCategories != null)
 				// {
 				// for (int i = 0; i < configCategories.length; i++)
 				// {
 				// addCategory(state, channel, configCategories[i]);
 				// }
 
 				if (state.getAttribute(STATE_CATEGORIES_SHOW_LIST) == null)
 				{
 					// get the catagories as defined without scanning all messages for more
 					List categories = channel.getCategories(false);
 
 					Hashtable h = new Hashtable();
 					for (Iterator i = categories.iterator(); i.hasNext();)
 					{
 						String cat = (String) i.next();
 
 						// store the ids of topic messages
 						List topicMsgIds = channel.getTopicMsgIds(cat);
 						h.put(cat, topicMsgIds);
 					}
 					state.setAttribute(STATE_CATEGORIES_SHOW_LIST, h);
 
 					HashSet s = new HashSet();
 					s.addAll(categories);
 					state.setAttribute(STATE_EXPAND_CATEGORY_LIST, s);
 
 					Hashtable t = new Hashtable();
 					for (Iterator i = categories.iterator(); i.hasNext();)
 					{
 						String cat = (String) i.next();
 						HashSet v = new HashSet();
 						t.put(cat, v);
 					}
 					state.setAttribute(STATE_EXPAND_MESSAGE_LIST, t);
 
 				}
 			}
 
 			// // setup the observer to notify our MONITOR_PANEL panel(inside the Main panel)
 			// if (state.getAttribute(STATE_OBSERVER) == null)
 			// {
 			// // the delivery location for this tool
 			// String deliveryId = clientWindowId(state, portlet.getID());
 			//
 			// // the html element to update on delivery
 			// String elementId = mainPanelUpdateId(portlet.getID()) + "." + MONITOR_PANEL;
 			//
 			// // the event resource reference pattern to watch for
 			// Reference r = new Reference(channelId);
 			//				
 			// // add message pattern
 			// String pattern = DiscussionService.messageReference(r.getContext(), r.getId(), "");
 			// MultipleEventsObservingCourier o = new MultipleEventsObservingCourier(deliveryId, elementId, pattern);
 			// // add channel pattern
 			// pattern = DiscussionService.channelReference(r.getContext(), r.getId());
 			// o.addResourcePattern(pattern);
 			// o.enable();
 			// state.setAttribute(STATE_OBSERVER, o);
 			// }
 		}
 
 		if (state.getAttribute(STATE_DISPLAY_MESSAGE) == null)
 		{
 			state.setAttribute(STATE_DISPLAY_MESSAGE, new DisplayMessage(""));
 		}
 
 		/** The content type image lookup service in the State. */
 		ContentTypeImageService iService = (ContentTypeImageService) state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE);
 		if (iService == null)
 		{
 			iService = org.sakaiproject.content.cover.ContentTypeImageService.getInstance();
 			state.setAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE, iService);
 		} // if
 
 		if (state.getAttribute(STATE_SORTED_BY) == null)
 		{
 			state.setAttribute(STATE_SORTED_BY, STATE_SORTED_BY_DATE);
 		}
 
 		if (state.getAttribute(STATE_SORTED_ASC) == null)
 		{
 			state.setAttribute(STATE_SORTED_ASC, Boolean.TRUE.toString());
 		}
 
 		if (state.getAttribute(ATTACHMENTS) == null)
 		{
 			state.setAttribute(ATTACHMENTS, EntityManager.newReferenceList());
 		}
 
 		if (state.getAttribute(STATE_ASCENDING) == null)
 		{
 			state.setAttribute(STATE_ASCENDING, new Boolean(config.getInitParameter(PARAM_ASCENDING)));
 		}
 
 		if (state.getAttribute(STATE_EXPAND_ALL_FLAG) == null)
 		{
 			state.setAttribute(STATE_EXPAND_ALL_FLAG, new Boolean(false));
 		}
 
 		if (state.getAttribute(STATE_LEFT_RIGHT_LAYOUT) == null)
 		{
 			state.setAttribute(STATE_LEFT_RIGHT_LAYOUT, new Boolean(false));
 		}
 
 		if (state.getAttribute(VISITED_MESSAGES) == null)
 		{
 			state.setAttribute(VISITED_MESSAGES, new HashSet());
 		}
 
 		// show the list of assignment view first
 		if (state.getAttribute(STATE_SELECTED_VIEW) == null)
 		{
 			state.setAttribute(STATE_SELECTED_VIEW, rb.getString("rowlay"));
 		}
 
 		if (state.getAttribute(STATE_SEARCH) == null)
 		{
 			state.setAttribute(STATE_SEARCH, "");
 		}
 
 		// make sure the observer is in sync with state
 		updateObservationOfChannel(state, portlet.getID());
 
 	} // initState
 
 	/**
 	 * Action is to use when doAttachment when "add attachments" is clicked
 	 */
 	public void doAttachments(RunData data, Context context)
 	{
 		// get into helper mode with this helper tool
 		startHelper(data.getRequest(), "sakai.filepicker");
 
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, state.getAttribute(ATTACHMENTS));
 
 		ParameterParser params = data.getParameters();
 		String subject = params.getString("subject");
 
 		String mode = (String) state.getAttribute(STATE_MODE);
 		if (mode != null && mode.equals(MODE_NEW_TOPIC))
 		{
 			subject = ((String) params.getString("subject"));
 			if (subject != null)
 			{
 				subject = subject.trim();
 			}
 			state.setAttribute(NEW_TOPIC_SUBJECT, subject);
 
 			String body = params.getCleanString("body");
 			body = processFormattedTextFromBrowser(state, body);
 			state.setAttribute(NEW_TOPIC_BODY, body);
 
 			String style = params.getString("style");
 			state.setAttribute(NEW_TOPIC_REPLY_STYLE, style);
 
 			String category = ((String) params.getString("newcategory"));
 			if (category != null)
 			{
 				category = category.trim();
 			}
 			if (category == null || category.length() == 0)
 			{
 				// no new category input
 				state.setAttribute(NEW_TOPIC_NEW_CATEGORY, new Boolean(Boolean.FALSE.toString()));
 				category = ((String) params.getString("category"));
 				if (category != null)
 				{
 					category = category.trim();
 				}
 				state.setAttribute(NEW_TOPIC_CATEGORY, category);
 			}
 			else
 			{
 				// new category input
 				state.setAttribute(NEW_TOPIC_NEW_CATEGORY, new Boolean(Boolean.TRUE.toString()));
 				state.setAttribute(NEW_TOPIC_CATEGORY, category);
 			}
 		}
 		else if (mode != null && mode.equals(MODE_REPLY))
 		{
 			// save the input infos for the respond message
 			subject = params.getString("subject");
 			if (subject != null)
 			{
 				subject = subject.trim();
 			}
 			state.setAttribute(RESPOND_SUBJECT, subject);
 
 			String body = params.getCleanString("body");
 			body = processFormattedTextFromBrowser(state, body);
 			state.setAttribute(RESPOND_BODY, body);
 		}
 		else
 		{
 			// must be inside control pannel editing draft message
 			state.setAttribute(DRAFT_MESSAGE_BODY, processFormattedTextFromBrowser(state, params.getCleanString("body")));
 			state.setAttribute(DRAFT_MESSAGE_SUBJECT, params.getString("subject").trim());
 			state.setAttribute(DRAFT_MESSAGE_REPLY_STYLE, params.getString("style"));
 		}
 
 		// make sure the Main panel is updated
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		String main = VelocityPortletPaneledAction.mainPanelUpdateId(peid);
 		schedulePeerFrameRefresh(main);
 
 	} // doAttachments
 
 	/**
 	 * Setup our observer to be watching for change events for our channel.
 	 */
 	private void updateObservationOfChannel(SessionState state, String peid)
 	{
 	} // updateObservationOfChannel
 
 	/**
 	 * If the state indicates an update is needed, update the portlet's configuration.
 	 * 
 	 * @param state
 	 *        The session state.
 	 * @param portlet
 	 *        The portlet to update.
 	 * @param data
 	 *        The current request run data.
 	 */
 	private void updatePortlet(SessionState state, VelocityPortlet portlet, RunData data)
 	{
 		// check the flag
 		if (state.getAttribute(STATE_UPDATE) == null) return;
 
 		// change the portlet's configuration
 		portlet.setAttribute(PARAM_CHANNEL, (String) state.getAttribute(STATE_CHANNEL_REF), data);
 		portlet.setAttribute(PARAM_ASCENDING, ((Boolean) state.getAttribute(STATE_ASCENDING)).toString(), data);
 
 		// clear the flag
 		state.removeAttribute(STATE_UPDATE);
 
 	} // updatePortlet
 
 	/**
 	 * build the context for the Layout panel
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		context.put("mainFrameId", Web.escapeJavascript("Main" + ToolManager.getCurrentPlacement().getId()));
 
 		context.put("tlang", rb);
 
 		String state_mode = (String) state.getAttribute(STATE_MODE);
 
 		if (state_mode != null)
 		{
 			if (state_mode.equals(MODE_SHOW_TOPIC_CONTENT))
 			{
 				return buildContentContext(portlet, context, rundata, state);
 			}
 			else if (state_mode.equals(MODE_NEW_TOPIC))
 			{
 				return buildNewTopicContext(portlet, context, rundata, state);
 			}
 			else if (state_mode.equals(MODE_NEW_CATEGORY))
 			{
 				return buildNewCategoryContext(portlet, context, rundata, state);
 			}
 			else if (state_mode.equals(MODE_REPLY))
 			{
 				return buildReplyContext(portlet, context, rundata, state);
 			}
 			else if (state_mode.equals(MODE_REPLY_PREVIEW))
 			{
 				return buildReplyPreviewContext(portlet, context, rundata, state);
 			}
 			else if (state_mode.equals(MODE_DELETE_CATEGORY_CONFIRM))
 			{
 				return buildDeleteCategoryConfirmContext(portlet, context, rundata, state);
 			}
 			else if (state_mode.equals(MODE_DELETE_MESSAGE_CONFIRM))
 			{
 				return buildDeleteMessageConfirmContext(portlet, context, rundata, state);
 			}
 		}
 
 		// assure that the portlet parameters are current with the user's configuration choices
 		updatePortlet(state, portlet, rundata);
 
 		context.put("panel-control", CONTROL_PANEL);
 		context.put("panel-monitor", MONITOR_PANEL);
 		context.put("panel-toolbar", TOOLBAR_PANEL);
 		context.put("panel-next", NEXT_PANEL);
 
 		context.put("layout_left_right", (Boolean) state.getAttribute(STATE_LEFT_RIGHT_LAYOUT));
 		return (String) getContext(rundata).get("template") + "-Layout";
 
 	} // buildMainPanelContext
 
 	/**
 	 * build the context for the menu panel
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildToolbarPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
 	{
 		// we might be on the way to a permissions...
 		if (state.getAttribute(STATE_PERMISSIONS) != null)
 		{
 			state.removeAttribute(STATE_PERMISSIONS);
 			doPermissionsNow(data, context);
 		}
 
 		boolean allowNewTopic = false;
 		boolean allowNewCategory = false;
 		boolean hasPrevious = false;
 		boolean hasNext = false;
 
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 
 		try
 		{
 			// get the current channel ID from state object
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 
 			String currentId = null;
 			DisplayMessage dMessage = (DisplayMessage) state.getAttribute(STATE_DISPLAY_MESSAGE);
 			if (dMessage != null)
 			{
 				currentId = dMessage.getId();
 			}
 			if (currentId != null && !currentId.equals(""))
 			{
 				DiscussionMessage message = channel.getDiscussionMessageNoException(currentId);
 				if (message != null)
 				{
 					String category = message.getDiscussionHeader().getCategory();
 					Vector messageIds = (Vector) ((Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST)).get(category);
 					hasPrevious = (messageIds.indexOf(currentId) == 0) ? false : true;
 					hasNext = ((messageIds.indexOf(currentId) == messageIds.size() - 1)) ? false : true;
 				}
 				else
 				{
 					hasPrevious = false;
 					hasNext = false;
 				}
 			}
 
 			// detect whether channel is existed
 			if (channel != null)
 			{
 				allowNewTopic = channel.allowAddTopicMessage();
 				allowNewCategory = channel.allowAddTopicMessage();
 			}
 			else
 			{
 				allowNewTopic = DiscussionService.allowAddChannel(channelId);
 				allowNewCategory = DiscussionService.allowAddChannel(channelId);
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youdonot1"));
 		}
 
 		// build menu
 		Menu bar = new MenuImpl(portlet, data, "ThreadedDiscussionIIAction");
 		bar.add(new MenuEntry(rb.getString("newtopic"), null, allowNewTopic, MenuItem.CHECKED_NA, "doSet_new_topic"));
 		bar.add(new MenuEntry(rb.getString("newcat"), null, allowNewCategory, MenuItem.CHECKED_NA, "doSet_new_category"));
 
 		if (hasPrevious)
 		{
 			bar.add(new MenuEntry(rb.getString("premess"), null, true, MenuItem.CHECKED_NA, "doPre_message"));
 		}
 		if (hasNext)
 		{
 			bar.add(new MenuEntry(rb.getString("nexmess"), null, true, MenuItem.CHECKED_NA, "doNext_message"));
 		}
 
 		if (!((Boolean) state.getAttribute(STATE_EXPAND_ALL_FLAG)).booleanValue())
 		{
 			bar.add(new MenuEntry(rb.getString("expall"), null, true, MenuItem.CHECKED_NA, "doExpand_all"));
 		}
 		else
 		{
 			bar.add(new MenuEntry(rb.getString("collall"), null, true, MenuItem.CHECKED_NA, "doCollapse_all"));
 		}
 
 		// Set menu state attribute
 		SessionState stateForMenus = ((JetspeedRunData) data).getPortletSessionState(portlet.getID());
 		stateForMenus.setAttribute(MenuItem.STATE_MENU, bar);
 
 		// add permissions, if allowed
 		if (SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
 		{
 			bar.add(new MenuEntry(rb.getString("permis"), "doPermissions"));
 		}
 
 		context.put(Menu.CONTEXT_MENU, bar);
 		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
 
 		/* Added this array to load the combo box options */
 		String[] comoptions = { rb.getString("rowlay"), rb.getString("collay") };
 		context.put("ord", comoptions);
 
 		context.put("tlang", rb);
 		add2ndToolbarFields(data, context);
 
 		return null;
 
 	} // buildToolbarPanelContext
 
 	/**
 	 * build the context for the List panel
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildListPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		context.put("tlang", rb);
 
 		String search = (String) state.getAttribute(STATE_SEARCH);
 		if (search == null || search.equals(""))
 		{
 			context.put("searching", Boolean.FALSE);
 		}
 		else
 		{
 			context.put("searching", Boolean.TRUE);
 		}
 
 		Vector searchResultList = new Vector();
 
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 
 		// the sorting defaults for the outline view
 		String sortedBy = "";
 		String sortedAsc = (String) state.getAttribute(STATE_SORTED_ASC);
 
 		// set default sort attribute
 		sortedBy = (String) state.getAttribute(STATE_SORTED_BY);
 		if ((!sortedBy.equals(STATE_SORTED_BY_TOPIC)) && (!sortedBy.equals(STATE_SORTED_BY_AUTHOR))
 				&& (!sortedBy.equals(STATE_SORTED_BY_DATE)))
 		{
 			sortedBy = STATE_SORTED_BY_DATE;
 			state.setAttribute(STATE_SORTED_BY, sortedBy);
 		}
 		context.put("currentSortedBy", sortedBy);
 		context.put("currentSortAsc", sortedAsc);
 
 		boolean allowNewTopic = false;
 		boolean allowRemoveCategory = false;
 		try
 		{
 			// get the current channel ID from state object
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 
 			// detect whether channel is existed
 			if (channel != null)
 			{
 				allowNewTopic = channel.allowAddTopicMessage();
 				allowRemoveCategory = channel.allowRemoveCategory();
 				context.put("channel", channel);
 
 				// get the sorted categories Vector according to the current sorting criteria
 				List categories = sortedCategories(channel, sortedBy, sortedAsc);
 				if (categories != null)
 				{
 					context.put("categories", categories);
 				}
 
 				// provide the expanded category list
 				HashSet expandedCategories = (HashSet) state.getAttribute(STATE_EXPAND_CATEGORY_LIST);
 				context.put("expandedCategoryList", expandedCategories);
 
 				if (categories != null)
 				{
 					for (Iterator i = categories.iterator(); i.hasNext();)
 					{
 						String currentCategory = (String) i.next();
 
 						// the category is expanded; get the most recent topics
 						if (expandedCategories.contains(currentCategory))
 						{
 							Hashtable showList = (Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST);
 							Vector v = (Vector) showList.get(currentCategory);
 							if (v == null) v = new Vector();
 							Iterator topics = channel.getTopics(currentCategory);
 							while (topics.hasNext())
 							{
 								DiscussionMessage topic = (DiscussionMessage) topics.next();
 								// the topic is newly added, add the topic
 								if (!v.contains(topic.getId()))
 								{
 									int index = 0;
 									// the replied to message has already been expanded
 									if (topic.getDiscussionHeader().getDraft())
 									{
 										try
 										{
 											// filter out drafts not by this user (unless this user is a super user) %%% not sure I like this -zqian
 											channel.getDiscussionMessage(topic.getId());
 
 											// if the new message is a draft message, add to the bottom of drafts
 											int firstNondraftIndex = -1;
 											for (int j = 0; (j < v.size() && firstNondraftIndex == -1); j++)
 											{
 												DiscussionMessage next = (DiscussionMessage) channel
 														.getDiscussionMessage((String) v.get(j));
 												index = index + 1;
 												if (!next.getDiscussionHeader().getDraft())
 												{
 													firstNondraftIndex = index;
 												}
 											}
 											if (firstNondraftIndex == -1)
 											{
 												v.add(topic.getId());
 											}
 											else
 											{
 												v.add(firstNondraftIndex, topic.getId());
 											}
 										}
 										catch (PermissionException e)
 										{
 										}
 										catch (IdUnusedException e)
 										{
 										}
 									}
 									else
 									{
 										// add it to the end
 										v.add(topic.getId());
 									} // if-else
 
 								}
 							}
 
 							// check for the expanded message's responses
 							Hashtable t = (Hashtable) state.getAttribute(STATE_EXPAND_MESSAGE_LIST);
 							HashSet s = (HashSet) t.get(currentCategory);
 							if (s != null)
 							{
 								Iterator expandMessageInCategory = ((HashSet) t.get(currentCategory)).iterator();
 								while (expandMessageInCategory.hasNext())
 								{
 									DiscussionMessage nextMessage = (DiscussionMessage) expandMessageInCategory.next();
 									Iterator repliesToNextMessage = nextMessage.getReplies();
 									// only test for the direct response
 									int depth = nextMessage.getReplyToDepth() + 1;
 									while (repliesToNextMessage.hasNext())
 									{
 										DiscussionMessage testReply = (DiscussionMessage) repliesToNextMessage.next();
 										if ((testReply.getReplyToDepth() == depth) && (!v.contains(testReply.getId())))
 										{
 											int index = v.indexOf(nextMessage.getId());
 											// the replied to message has already been expanded
 											if (testReply.getDiscussionHeader().getDraft())
 											{
 												try
 												{
 													// filter out drafts not by this user (unless this user is a super user) %%% not sure I like this -zqian
 													channel.getDiscussionMessage(testReply.getId());
 
 													// if the new message is a draft message, add to the bottom of drafts
 													int firstNondraftIndex = -1;
 													int k = index;
 													while (k < v.size() && (firstNondraftIndex == -1))
 													{
 														DiscussionMessage next = (DiscussionMessage) channel
 																.getDiscussionMessage((String) v.get(k));
 														index = index + 1;
 														if (!next.getDiscussionHeader().getDraft())
 														{
 															firstNondraftIndex = index;
 														}
 														k++;
 													}
 													v.add(firstNondraftIndex, testReply.getId());
 												}
 												catch (PermissionException e)
 												{
 												}
 												catch (IdUnusedException e)
 												{
 												}
 
 											}
 											else
 											{
 												// if the new message is not a draft message, add to the bottom of non-drafts
 												index = v.indexOf(nextMessage.getId()) + 1;
 												String id = nextMessage.getId();
 												String replyToId = id;
 												while (replyToId.equals(id) && index < v.size())
 												{
 													DiscussionMessage m = (DiscussionMessage) channel
 															.getDiscussionMessage((String) v.get(index));
 													String newId = m.getDiscussionHeader().getReplyTo();
 													if (!newId.equals(id))
 													{
 														replyToId = newId;
 													}
 													else
 													{
 														index++;
 													}
 												}
 												v.add(index, testReply.getId());
 											} // if-else: draft vs. non draft
 										} // if: the message is not contained in the show list
 									} // while: check for all response to the message
 								} // while: check for all opened message
 							} // if: there is expanded message
 
 							// update the show list in that category
 							showList.put(currentCategory, v);
 
 							// find the search result
 							if ((search != null) && (!search.equals("")))
 							{
 								for (int k = 0; k < v.size(); k++)
 								{
 									DiscussionMessage message = channel.getDiscussionMessage((String) v.get(k));
 									if (StringUtil.containsIgnoreCase(message.getDiscussionHeader().getSubject(), search)
 											|| StringUtil.containsIgnoreCase(FormattedText.convertFormattedTextToPlaintext(message
 													.getBody()), search))
 									{
 										searchResultList.add(message.getId());
 									}
 								}
 							}
 							state.setAttribute(STATE_CATEGORIES_SHOW_LIST, showList);
 						} // if: for every opened category
 					} // for: for all categories
 				} // if: categories not null
 
 				if (searchResultList.size() > 0 && state.getAttribute(STATE_SEARCH_REFRESH) != null)
 				{
 					// if the current message is not in result list, make the current message to be the first one of search result
 					state.setAttribute(STATE_DISPLAY_MESSAGE, new DisplayMessage((String) searchResultList.get(0)));
 					state.removeAttribute(STATE_SEARCH_REFRESH);
 				}
 				context.put("searchResultList", searchResultList);
 
 				// provide the category show list
 				context.put("categoriesShowList", state.getAttribute(STATE_CATEGORIES_SHOW_LIST));
 				// the topics has been expanded for outline
 				context.put("expandAllTopics", state.getAttribute(STATE_EXPAND_ALL_TOPIC_FLAG));
 				// provide the expanded message list
 				context.put("expandedMessageList", state.getAttribute(STATE_EXPAND_MESSAGE_LIST));
 			}
 			else
 			{
 				allowNewTopic = DiscussionService.allowAddChannel(channelId);
 			}
 
 			if (state.getAttribute(STATE_DISPLAY_MESSAGE) != null)
 			{
 				DisplayMessage d = (DisplayMessage) state.getAttribute(STATE_DISPLAY_MESSAGE);
 				try
 				{
 					channel.getDiscussionMessage(d.getId());
 					context.put("currentMessage", state.getAttribute(STATE_DISPLAY_MESSAGE));
 				}
 				catch (Exception e)
 				{
 					// current displayed message no longer exist, refresh the control pannel
 					state.removeAttribute(STATE_DISPLAY_MESSAGE);
 					String peid = ((JetspeedRunData) rundata).getJs_peid();
 					schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + CONTROL_PANEL);
 				}
 			}
 
 			// inform the observing courier that we just updated the page...
 			// if there are pending requests to do so they can be cleared
 			justDelivered(state);
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youdonot2"));
 		}
 
 		context.put("allowEditChannel", new Boolean(DiscussionService.allowEditChannel(channelId)));
 		context.put("action", (String) state.getAttribute(STATE_ACTION));
 		context.put("service", DiscussionService.getInstance());
 		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
 
 		context.put("allow_new_topic", new Boolean(allowNewTopic));
 		context.put("allow_new_category", new Boolean(allowNewTopic));
 		context.put("allow_remove_category", new Boolean(allowRemoveCategory));
 
 		context.put("panel-control", CONTROL_PANEL);
 		context.put("panel-monitor", MONITOR_PANEL);
 		context.put("panel-target", MONITOR_PANEL);
 		context.put("updated", (String) state.getAttribute(STATE_LIST_PANNEL_UPDATED));
 		state.setAttribute(STATE_LIST_PANNEL_UPDATED, "false");
 
 		context.put("visitedMessages", state.getAttribute(VISITED_MESSAGES));
 
 		if (state.getAttribute(DELETE_WARNING) != null)
 		{
 			addAlert(state, (String) state.getAttribute(DELETE_WARNING));
 			state.removeAttribute(DELETE_WARNING);
 		}
 
 		return null;
 
 	} // buildListPanelContext
 
 	/**
 	 * build the context for the Control panel (has a send field)
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildControlPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		context.put("tlang", rb);
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		DiscussionChannel channel = null;
 		try
 		{
 			channel = DiscussionService.getDiscussionChannel(channelId);
 
 			// possible error message
 			String error = (String) state.getAttribute(STATE_ERROR);
 			if (error != null)
 			{
 				context.put("message", error);
 			}
 
 			// is this user going to be able to post?
 			boolean allowed = false;
 
 			// if no channel, we can check the user's allow add discussion Channel(channelId)
 			if (channel != null)
 			{
 				allowed = channel.allowAddMessage();
 			}
 			else
 			{
 				allowed = DiscussionService.allowAddChannel(channelId);
 			}
 			if (!allowed)
 			{
 				context.put("message", PERMISSION_POST_MESSAGE); // %%% or no message?
 			}
 			context.put("allow-send", new Boolean(allowed));
 
 			DisplayMessage dMessage = (DisplayMessage) state.getAttribute(STATE_DISPLAY_MESSAGE);
 			if (dMessage != null)
 			{
 				String mId = dMessage.getId();
 				if (!mId.equals(""))
 				{
 					try
 					{
 						DiscussionMessage m = channel.getDiscussionMessage(mId);
 						context.put("currentMessage", m);
 
 						DiscussionMessageHeader mHeader = m.getDiscussionHeader();
 						String replyTo = (String) state.getAttribute(RESPOND_REPLY_TO);
 						context.put("attachments", state.getAttribute(ATTACHMENTS));
 						if (mHeader.getDraft())
 						{
 							// a draft message
 							context.put("draftCategory", state.getAttribute(DRAFT_MESSAGE_CATEGORY) != null ? state
 									.getAttribute(DRAFT_MESSAGE_CATEGORY) : mHeader.getCategory());
 							context.put("draftBody", state.getAttribute(DRAFT_MESSAGE_BODY) != null ? state
 									.getAttribute(DRAFT_MESSAGE_BODY) : m.getBody());
 							context.put("draftSubject", state.getAttribute(DRAFT_MESSAGE_SUBJECT) != null ? state
 									.getAttribute(DRAFT_MESSAGE_SUBJECT) : mHeader.getSubject());
 							if (m.getReplyToDepth() == 0)
 							{
 								String style = (String) state.getAttribute(DRAFT_MESSAGE_REPLY_STYLE);
 								if (style == null)
 								{
 									style = "thread";
 								}
 								context.put("draftStyle", style);
 							}
 						}
 						else
 						{
 							// respond to posted message
 							if (replyTo == null)
 							{
 								replyTo = "totopic";
 							}
 							context.put("replyto", replyTo);
 							context.put("subject", state.getAttribute(RESPOND_SUBJECT));
 							context.put("body", state.getAttribute(RESPOND_BODY));
 						}
 
 						DiscussionMessage topic = m;
 						StringBuffer messagePath = new StringBuffer(Validator.escapeHtml(m.getDiscussionHeader().getSubject()));
 						while (topic.getReplyToDepth() != 0)
 						{
 							replyTo = topic.getDiscussionHeader().getReplyTo();
 							topic = channel.getDiscussionMessage(replyTo);
 							messagePath.insert(0, Validator.escapeHtml(topic.getDiscussionHeader().getSubject() + " > "));
 						}
 						messagePath.insert(0, Validator.escapeHtml(topic.getDiscussionHeader().getCategory() + " > "));
 						context.put("messagePath", messagePath.toString());
 						context.put("topic", topic);
 						context.put("style", topic.getProperties().getProperty(ResourceProperties.PROP_REPLY_STYLE));
 					}
 					catch (Exception e)
 					{
 					} // try - catch
 				} // if - else
 			} // based on the DisplayMessage type
 			context.put("channel", channel);
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youdonot1"));
 		}
 
 		context.put("panel-target", CONTROL_PANEL);
 
 		// put this pannel's name for the return url
 		context.put("panel-control", CONTROL_PANEL);
 
 		// set the action for form processing
 		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
 
 		// set the form field name for the send button
 		context.put("form-submit", BUTTON + "doSend");
 
 		// set the form field name for the send button
 		context.put("form-message", FORM_MESSAGE);
 
 		context.put("date", TimeService.newTime());
 		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
 
 		context.put("action", (String) state.getAttribute(STATE_ACTION));
 
 		return null;
 
 	} // buildControlPanelContext
 
 	/**
 	 * build the context for showing topic content (has a send field)
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildContentContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		String topicId = (String) state.getAttribute(STATE_SHOW_CONTENT_TOPIC_ID);
 		context.put("tlang", rb);
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 			DiscussionMessage msg = channel.getDiscussionMessage(topicId);
 
 			context.put("topic", msg);
 			context.put("channel", channel);
 
 			String cat = msg.getDiscussionHeader().getCategory();
 			List topicMsgIds = channel.getTopicMsgIds(cat);
 			for (int i = 0; i < topicMsgIds.size(); i++)
 			{
 				if (topicMsgIds.get(i).equals(topicId))
 				{
 					boolean goPT = false;
 					boolean goNT = false;
 					if ((i - 1) >= 0)
 					{
 						goPT = true;
 					}
 					if ((i + 1) < topicMsgIds.size())
 					{
 						goNT = true;
 					}
 					context.put("goPTButton", new Boolean(goPT));
 					context.put("goNTButton", new Boolean(goNT));
 				}
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin1"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("yodonot2"));
 		}
 
 		context.put("action", (String) state.getAttribute(STATE_ACTION));
 		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
 		return (String) getContext(rundata).get("template") + "-topic_content";
 
 	} // buildContentContext
 
 	/**
 	 * Responding to the request of going to next topic within the current category
 	 */
 	public void doNext_topic_content(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		String topicId = (String) state.getAttribute(STATE_SHOW_CONTENT_TOPIC_ID);
 
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 			DiscussionMessage msg = channel.getDiscussionMessage(topicId);
 			String cat = msg.getDiscussionHeader().getCategory();
 			List topicMsgIds = channel.getTopicMsgIds(cat);
 			int index = topicMsgIds.indexOf(topicId);
 			if ((index != -1) && ((index + 1) < topicMsgIds.size()))
 			{
 				String prevId = (String) (topicMsgIds.get(index + 1));
 				state.setAttribute(STATE_SHOW_CONTENT_TOPIC_ID, prevId);
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin1"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("yodonot2"));
 		}
 
 		state.setAttribute(STATE_MODE, MODE_SHOW_TOPIC_CONTENT);
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 
 	} // doNext_topic_content
 
 	/**
 	 * Responding to the request of going to previous topic within the current category
 	 */
 	public void doPrev_topic_content(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		String topicId = (String) state.getAttribute(STATE_SHOW_CONTENT_TOPIC_ID);
 
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 			DiscussionMessage msg = channel.getDiscussionMessage(topicId);
 			String cat = msg.getDiscussionHeader().getCategory();
 			List topicMsgIds = channel.getTopicMsgIds(cat);
 			int index = topicMsgIds.indexOf(topicId);
 			if ((index != -1) && ((index - 1) >= 0))
 			{
 				String prevId = (String) (topicMsgIds.get(index - 1));
 				state.setAttribute(STATE_SHOW_CONTENT_TOPIC_ID, prevId);
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin1"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("yodonot2"));
 		}
 
 		state.setAttribute(STATE_MODE, MODE_SHOW_TOPIC_CONTENT);
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 
 	} // doPrev_topic_content
 
 	/**
 	 * build the context for creating new topic (has a send field)
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildNewTopicContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		context.put("tlang", rb);
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 			context.put("categories", sortedCategories(channel, STATE_SORTED_BY_CATEGORY_ALPHA, Boolean.TRUE.toString()));
 			context.put("date", TimeService.newTime());
 			context.put("category", state.getAttribute(NEW_TOPIC_CATEGORY));
 			context.put("newcategory", state.getAttribute(NEW_TOPIC_NEW_CATEGORY));
 			context.put("newtopicbody", state.getAttribute(NEW_TOPIC_BODY));
 			context.put("newtopicsubject", state.getAttribute(NEW_TOPIC_SUBJECT));
 			context.put("newtopicreplystyle", state.getAttribute(NEW_TOPIC_REPLY_STYLE));
 			context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
 			context.put("action", (String) state.getAttribute(STATE_ACTION));
			context.put("attachments", state.getAttribute(ATTACHMENTS));
 
 			return (String) getContext(rundata).get("template") + "-Newtopic";
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youarenot4"));
 		}
 
 		return (String) getContext(rundata).get("template") + "-Layout";
 
 	} // buildNewTopicContext
 
 	/**
 	 * build the context for creating new category (has a send field)
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildNewCategoryContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		context.put("tlang", rb);
 
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 			context.put("categories", sortedCategories(channel, STATE_SORTED_BY_CATEGORY_ALPHA, Boolean.TRUE.toString()));
 			context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
 			context.put("action", (String) state.getAttribute(STATE_ACTION));
 			return (String) getContext(rundata).get("template") + "-Newcategory";
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youarenot4"));
 		}
 
 		return (String) getContext(rundata).get("template") + "-Layout";
 
 	} // buildNewCategoryContext
 
 	/**
 	 * build the context for confirming the delete of message (has a send field)
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildDeleteMessageConfirmContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		context.put("tlang", rb);
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 			DiscussionMessage message = channel.getDiscussionMessage((String) state.getAttribute(DELETE_MESSAGE_ID));
 			context.put("message", message);
 			context.put("delete_messages", channel.getThread(message));
 
 			// can the user delete all the messages in the thread?
 			Iterator i = channel.getThread(message);
 			boolean allowRemove = true;
 			while (i.hasNext() && allowRemove)
 			{
 				allowRemove = channel.allowRemoveMessage((DiscussionMessage) i.next());
 			}
 			context.put("allowRemove", Boolean.valueOf(allowRemove));
 
 			return (String) getContext(rundata).get("template") + "-DeleteTopicConfirm";
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youarenot1"));
 		}
 
 		return (String) getContext(rundata).get("template") + "-Layout";
 
 	} // buildDeleteMessageConfirmContext
 
 	/**
 	 * build the context for confirming the delete of category (has a send field)
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildDeleteCategoryConfirmContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 
 		String category = (String) state.getAttribute(DELETE_CATEGORY);
 		context.put("tlang", rb);
 		context.put("category", category);
 
 		// get all the messages within the category, that will be deleted
 		try
 		{
 			DiscussionChannel channel = (DiscussionChannel) DiscussionService.getChannel((String) state
 					.getAttribute(STATE_CHANNEL_REF));
 			Iterator messages = channel.getThreads(category);
 			context.put("delete_messages", messages);
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 			return (String) getContext(rundata).get("template");
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youarenot2"));
 			return (String) getContext(rundata).get("template");
 		}
 
 		return (String) getContext(rundata).get("template") + "-DeleteCategoryConfirm";
 
 	} // buildDeleteCategoryConfirmContext
 
 	/**
 	 * build the context for the reply form (has a send field)
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildReplyContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		String replyToMessageId = (String) state.getAttribute(RESPOND_REPLY_TO);
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		context.put("tlang", rb);
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 			try
 			{
 				DiscussionMessage m = channel.getDiscussionMessage(replyToMessageId);
 				context.put("currentMessage", m);
 
 				context.put("subject", state.getAttribute(RESPOND_SUBJECT));
 				context.put("body", state.getAttribute(RESPOND_BODY));
 
 				DiscussionMessage topic = m;
 				String replyTo = "";
 				StringBuffer replyPath = new StringBuffer(Validator.escapeHtml(m.getDiscussionHeader().getSubject()));
 				while (topic.getReplyToDepth() != 0)
 				{
 					replyTo = topic.getDiscussionHeader().getReplyTo();
 					topic = channel.getDiscussionMessage(replyTo);
 					replyPath.insert(0, Validator.escapeHtml(topic.getDiscussionHeader().getSubject() + " > "));
 				}
 				replyPath.insert(0, Validator.escapeHtml(topic.getDiscussionHeader().getCategory() + " > "));
 				context.put("replyPath", replyPath.toString());
 				context.put("topic", topic);
 			}
 			catch (IdUnusedException e)
 			{
 				addAlert(state, rb.getString("cannotfin2"));
 			}
 			catch (PermissionException e)
 			{
 				addAlert(state, rb.getString("youarenot3"));
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youarenot4"));
 		}
 
 		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
 		context.put("attachments", state.getAttribute(ATTACHMENTS));
 		context.put("action", (String) state.getAttribute(STATE_ACTION));
 
 		context.put("realDate", TimeService.newTime());
 
 		return (String) getContext(rundata).get("template") + "-Reply";
 
 	} // buildReplyContext
 
 	/**
 	 * build the context for the reply preview (has a send field)
 	 * 
 	 * @return (optional) template name for this panel
 	 */
 	public String buildReplyPreviewContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		String replyToId = (String) state.getAttribute(RESPOND_REPLY_TO);
 
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		context.put("tlang", rb);
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 			try
 			{
 				DiscussionMessage m = channel.getDiscussionMessage(replyToId);
 				context.put("currentMessage", m);
 
 				DiscussionMessage topic = m;
 				String replyTo = "";
 				while (topic.getReplyToDepth() != 0)
 				{
 					replyTo = topic.getDiscussionHeader().getReplyTo();
 					topic = channel.getDiscussionMessage(replyTo);
 				}
 				context.put("topic", topic);
 			}
 			catch (IdUnusedException e)
 			{
 				addAlert(state, rb.getString("cannotfin2"));
 			}
 			catch (PermissionException e)
 			{
 				addAlert(state, rb.getString("youarenot3"));
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youarenot4"));
 		}
 
 		context.put("subject", state.getAttribute(RESPOND_SUBJECT));
 		context.put("body", state.getAttribute(RESPOND_BODY));
 		context.put("attachments", state.getAttribute(RESPOND_ATTACHMENT));
 		context.put("contentTypeImageService", state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE));
 
 		context.put("action", (String) state.getAttribute(STATE_ACTION));
 		return (String) getContext(rundata).get("template") + "-Reply_Preview";
 
 	} // buildReplyPreviewContext
 
 	/**
 	 * Post the draft message
 	 */
 	public void doPost(RunData data, Context context)
 	{
 		ParameterParser params = data.getParameters();
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		String messageId = params.getString("messageId");
 		String subject = ((String) params.getString("subject")).trim();
 		if (subject.length() == 0)
 		{
 			state.setAttribute(DRAFT_MESSAGE_SUBJECT, "");
 			addAlert(state, rb.getString("plespe1"));
 		}
 		String body = params.getCleanString("body");
 		body = processFormattedTextFromBrowser(state, body);
 		state.setAttribute(DRAFT_MESSAGE_BODY, body);
 
 		String style = params.getString("style");
 		state.setAttribute(DRAFT_MESSAGE_REPLY_STYLE, style);
 
 		if (state.getAttribute(STATE_MESSAGE) == null)
 		{
 			try
 			{
 				DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 				DiscussionMessageEdit postMessage = channel.editDiscussionMessage(messageId);
 
 				postMessage.getDiscussionHeaderEdit().setSubject(subject);
 				postMessage.setBody(body);
 
 				// add the reply style property
 				if (postMessage.getReplyToDepth() == 0)
 				{
 					ResourcePropertiesEdit pEdit = postMessage.getPropertiesEdit();
 					pEdit.addProperty(ResourceProperties.PROP_REPLY_STYLE, style);
 				}
 				postMessage.getDiscussionHeaderEdit().setDraft(false);
 				postMessage.getDiscussionHeaderEdit().replaceAttachments((List) state.getAttribute(ATTACHMENTS));
 
 				// update time
 				postMessage.getDiscussionHeaderEdit().setDate(TimeService.newTime());
 
 				channel.commitMessage(postMessage);
 				state.setAttribute(STATE_DISPLAY_MESSAGE, new DisplayMessage(postMessage.getId()));
 			}
 			catch (IdUnusedException e)
 			{
 				addAlert(state, rb.getString("cannotfin5") + " " + messageId + " " + rb.getString("hasnotbee"));
 			}
 			catch (PermissionException e)
 			{
 				addAlert(state, PERMISSION_HEADER_STRING + rb.getString("posthemes"));
 			}
 			catch (InUseException e)
 			{
 				addAlert(state, rb.getString("themess"));
 			}
 		}
 
 		if (state.getAttribute(STATE_MESSAGE) == null)
 		{
 			state.removeAttribute(DRAFT_MESSAGE_CATEGORY);
 			state.removeAttribute(DRAFT_MESSAGE_SUBJECT);
 			state.removeAttribute(DRAFT_MESSAGE_BODY);
 			state.removeAttribute(DRAFT_MESSAGE_REPLY_STYLE);
 			state.setAttribute(ATTACHMENTS, null);
 
 			// update the list panel
 			String peid = ((JetspeedRunData) data).getJs_peid();
 			schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + MONITOR_PANEL);
 
 			// post sucessful
 		} // if-else
 
 	} // doPost
 
 	/**
 	 * Save the draft message
 	 */
 	public void doSave(RunData data, Context context)
 	{
 		ParameterParser params = data.getParameters();
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		String messageId = params.getString("messageId");
 		String subject = ((String) params.getString("subject")).trim();
 		if (subject.length() == 0)
 		{
 			state.setAttribute(DRAFT_MESSAGE_SUBJECT, "");
 			addAlert(state, rb.getString("plespe1"));
 		}
 		String body = params.getCleanString("body");
 		body = processFormattedTextFromBrowser(state, body);
 		state.setAttribute(DRAFT_MESSAGE_BODY, body);
 
 		if (state.getAttribute(STATE_MESSAGE) == null)
 		{
 			try
 			{
 				DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 				DiscussionMessageEdit postMessage = channel.editDiscussionMessage(messageId);
 
 				postMessage.getDiscussionHeaderEdit().setSubject(subject);
 				postMessage.setBody(body);
 
 				// add the reply style property
 				if (postMessage.getReplyToDepth() == 0)
 				{
 					String style = params.getString("style");
 					state.setAttribute(DRAFT_MESSAGE_REPLY_STYLE, style);
 					ResourcePropertiesEdit pEdit = postMessage.getPropertiesEdit();
 					pEdit.addProperty(ResourceProperties.PROP_REPLY_STYLE, style);
 				}
 				postMessage.getDiscussionHeaderEdit().setDraft(true);
 				postMessage.getDiscussionHeaderEdit().replaceAttachments((List) state.getAttribute(ATTACHMENTS));
 
 				// update time
 				postMessage.getDiscussionHeaderEdit().setDate(TimeService.newTime());
 
 				channel.commitMessage(postMessage);
 				state.setAttribute(STATE_DISPLAY_MESSAGE, new DisplayMessage(postMessage.getId()));
 			}
 			catch (IdUnusedException e)
 			{
 				addAlert(state, rb.getString("theid") + " " + messageId + " " + rb.getString("hasnotbee"));
 			}
 			catch (PermissionException e)
 			{
 				addAlert(state, PERMISSION_HEADER_STRING + rb.getString("viemes"));
 			}
 			catch (InUseException e)
 			{
 				addAlert(state, rb.getString("themess"));
 			}
 		}
 
 		if (state.getAttribute(STATE_MESSAGE) == null)
 		{
 			// save successful
 			state.setAttribute(ATTACHMENTS, null);
 
 			// update the list panel
 			String peid = ((JetspeedRunData) data).getJs_peid();
 			schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + MONITOR_PANEL);
 		} // if-else
 
 	} // doSave
 
 	/**
 	 * Start a new category
 	 */
 	public void doNew_category(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 
 			ParameterParser params = data.getParameters();
 			String category = ((String) params.getString("newcategory")).trim();
 
 			if (category.length() == 0)
 			{
 				addAlert(state, rb.getString("pleent"));
 			}
 			else
 			{
 				try
 				{
 					// Note: removed code to detect that the category already exists - we just "fail" quietly to add it again -ggolden
 					addCategory(state, channel, category);
 
 					// clean the input frame
 					state.removeAttribute(STATE_DISPLAY_MESSAGE);
 
 					// clean the state mode
 					state.removeAttribute(STATE_MODE);
 				}
 				catch (InUseException e)
 				{
 					addAlert(state, rb.getString("someone") + " channel.");
 				}
 				catch (PermissionException e)
 				{
 					addAlert(state, rb.getString("youdonot1"));
 				}
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youdonot1"));
 		}
 	} // doNew_category
 
 	/**
 	 * Start a new topic
 	 */
 	public void doNew_topic(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 
 			ParameterParser params = data.getParameters();
 			String subject = ((String) params.getString("subject")).trim();
 			state.setAttribute(NEW_TOPIC_SUBJECT, subject);
 
 			String body = params.getCleanString("body");
 			body = processFormattedTextFromBrowser(state, body);
 			state.setAttribute(NEW_TOPIC_BODY, body);
 
 			String style = params.getString("style");
 			state.setAttribute(NEW_TOPIC_REPLY_STYLE, style);
 
 			boolean newCategory = true;
 			String category = ((String) params.getString("newcategory")).trim();
 			if (category.length() == 0)
 			{
 				// no new category input
 				state.setAttribute(NEW_TOPIC_NEW_CATEGORY, new Boolean("false"));
 				category = params.getString("category")!=null?((String) params.getString("category")).trim():"";
 				state.setAttribute(NEW_TOPIC_CATEGORY, category);
 				if (category.length() == 0)
 				{
 					// no category specified
 					addAlert(state, rb.getString("plecho"));
 				}
 				newCategory = false;
 			}
 			else
 			{
 				// get the new category
 				state.setAttribute(NEW_TOPIC_NEW_CATEGORY, new Boolean("true"));
 				state.setAttribute(NEW_TOPIC_CATEGORY, category);
 				// Note: removed the check that the category already exists - we just quietly "fail" to add it again -ggoldne
 			} // if
 
 			if (state.getAttribute(STATE_MESSAGE) == null)
 			{
 				try
 				{
 					// message is a draft?
 					boolean draft = false;
 
 					String newTopic_option = params.getString("eventSubmit_doNew_topic");
 
 					if (newTopic_option.equals(rb.getString("gen.savdra")))// We put this (not code Strings) because they binds with vm
 					{
 						draft = true;
 					}
 					else if (newTopic_option.equals(rb.getString("gen.pos")))
 					{
 						draft = false;
 					} // if: draft?
 
 					if (subject.length() == 0)
 					{
 						addAlert(state, rb.getString("plespe2"));
 					}
 					else
 					{
 						boolean sameTopic = false;
 						Iterator l = channel.getTopics(category);
 						while (l.hasNext())
 						{
 							if (subject.equals(((DiscussionMessage) l.next()).getDiscussionHeader().getSubject()))
 							{
 								addAlert(state, rb.getString("samtop"));
 								sameTopic = true;
 							}
 						}
 
 						if (!sameTopic)
 						{
 							// add the reply style property
 							DiscussionMessageEdit addedMessageEdit = channel.addDiscussionMessage("");
 							addedMessageEdit.setBody(body);
 
 							DiscussionMessageHeaderEdit hEdit = addedMessageEdit.getDiscussionHeaderEdit();
 							hEdit.setCategory(category);
 							hEdit.setSubject(subject);
 							hEdit.setDraft(draft);
 							hEdit.replaceAttachments((List) state.getAttribute(ATTACHMENTS));
 
 							ResourcePropertiesEdit pEdit = addedMessageEdit.getPropertiesEdit();
 							pEdit.addProperty(ResourceProperties.PROP_REPLY_STYLE, style);
 
 							// update time
 							hEdit.setDate(TimeService.newTime());
 
 							channel.commitMessage(addedMessageEdit);
 
 							// if the category is newly added
 							if (newCategory)
 							{
 								try
 								{
 									addCategory(state, channel, category);
 								}
 								catch (InUseException e)
 								{
 									addAlert(state, rb.getString("someone") + " channel.");
 								}
 								catch (PermissionException e)
 								{
 									addAlert(state, rb.getString("youdonot1"));
 								}
 							}
 							setCategoryExpanded(state, category, true, channel);
 
 							// make it the current message
 							showMessage(data, addedMessageEdit.getId());
 							String peid = ((JetspeedRunData) data).getJs_peid();
 							schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + CONTROL_PANEL);
 						} // if-else
 					}
 				}
 				catch (PermissionException e)
 				{
 					addAlert(state, PERMISSION_HEADER_STRING + rb.getString("statop"));
 				}
 			} // if category has been specified.
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youdonot1"));
 		}
 
 		if (state.getAttribute(STATE_MESSAGE) == null)
 		{
 			// add new topic sucessful
 			state.removeAttribute(NEW_TOPIC_NEW_CATEGORY);
 			state.removeAttribute(NEW_TOPIC_CATEGORY);
 			state.removeAttribute(NEW_TOPIC_SUBJECT);
 			state.removeAttribute(NEW_TOPIC_BODY);
 			state.removeAttribute(NEW_TOPIC_REPLY_STYLE);
 
 			// clean the state mode
 			state.removeAttribute(STATE_MODE);
 
 		} // if-else
 
 	} // doNew_topic
 
 	/**
 	 * Handle a user posting a respond message
 	 */
 	public void doRespond(RunData runData, Context context)
 	{
 		// access the portlet element id to find our state
 		String peid = ((JetspeedRunData) runData).getJs_peid();
 		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);
 		ParameterParser params = runData.getParameters();
 
 		// message is a draft?
 		boolean draft = false;
 
 		String subject = StringUtil.trimToNull((String) params.getString("subject"));
 		state.setAttribute(RESPOND_SUBJECT, subject);
 
 		// get info from input
 		String body = params.getCleanString("body");
 		body = processFormattedTextFromBrowser(state, body);
 		state.setAttribute(RESPOND_BODY, body);
 
 		if (subject == null)
 		{
 			addAlert(state, rb.getString("plespe1"));
 		}
 		else
 		{
 			boolean replyToTopic = state.getAttribute(STATE_REPLY_TOPIC) != null;
 			boolean replyToMsg = state.getAttribute(STATE_REPLY_MSG) != null;
 
 			String replyToId = "";
 			if (replyToMsg)
 			{
 				replyToId = params.getString("messageId");
 			}
 			else if (replyToTopic)
 			{
 				replyToId = params.getString("topicId");
 			}
 
 			if (state.getAttribute(STATE_MESSAGE) == null)
 			{
 				try
 				{
 					DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state
 							.getAttribute(STATE_CHANNEL_REF));
 
 					try
 					{
 						// get the message thread before adding a new message
 						DiscussionMessage originalMessage = channel.getDiscussionMessage(replyToId);
 						String category = originalMessage.getDiscussionHeader().getCategory();
 						Vector v = (Vector) ((Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST)).get(category);
 						int index = v.indexOf(originalMessage.getId());
 						Iterator l = originalMessage.getReplies();
 
 						// insert the response message
 						DiscussionMessage addedMessage = channel.addDiscussionMessage(category, subject, draft, replyToId,
 								(List) state.getAttribute(ATTACHMENTS), body);
 
 						// show the added message as the current message
 						showMessage(runData, addedMessage.getId());
 
 						if (!isMessageExpanded(state, originalMessage))
 						{
 							// the replied to message has not been expanded
 							setMessageExpanded(state, originalMessage, true, channel);
 						}
 						else
 						{
 							if ((l == null) || (!l.hasNext()))
 							{
 								// if there is no reply message yet
 								v.add(index + 1, addedMessage.getId());
 							}
 							else
 							{
 								// if the new message is not a draft message, add to the bottom of non-drafts
 								int finalIndexOfShownThread = index;
 								int finalDraftIndex = 0;
 								while (l.hasNext())
 								{
 									DiscussionMessage next = (DiscussionMessage) l.next();
 									if (!next.getHeader().getDraft())
 									{
 										finalIndexOfShownThread = v.indexOf(next.getId());
 									}
 									else
 									{
 										finalDraftIndex = v.indexOf(next.getId());
 									}
 								}
 								if (finalIndexOfShownThread == index)
 								{
 									// if there is no post yet
 									finalIndexOfShownThread = finalDraftIndex;
 								}
 								v.add(finalIndexOfShownThread + 1, addedMessage.getId());
 								setCategoryShowList(state, category, v);
 							} // if - else
 						} // if-else
 
 					}
 					catch (PermissionException e)
 					{
 						addAlert(state, PERMISSION_HEADER_STRING + rb.getString("res"));
 					}
 					catch (IdUnusedException e)
 					{
 						addAlert(state, rb.getString("cannotfin3"));
 					}
 				}
 				catch (PermissionException e)
 				{
 					addAlert(state, rb.getString("youdonot3"));
 				}
 				catch (IdUnusedException e)
 				{
 					addAlert(state, rb.getString("cannotfin5"));
 				}
 			}
 		} // if-else
 
 		if (state.getAttribute(STATE_MESSAGE) == null)
 		{
 			state.setAttribute(ATTACHMENTS, null);
 			// respond sucessful
 			state.removeAttribute(RESPOND_REPLY_TO);
 			state.removeAttribute(RESPOND_SUBJECT);
 			state.removeAttribute(RESPOND_BODY);
 			state.removeAttribute(RESPOND_ATTACHMENT);
 			state.removeAttribute(STATE_MODE);
 
 		} // if-else
 
 	} // doRespond
 
 	/**
 	 * Handle a user posting a drafted respond message
 	 */
 	public void doRespond_draft(RunData runData, Context context)
 	{
 		// access the portlet element id to find our state
 		String peid = ((JetspeedRunData) runData).getJs_peid();
 		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);
 		ParameterParser params = runData.getParameters();
 		// get the reply to message id
 		// String replyType = params.getString("replyto");
 		String replyType = "";
 
 		boolean replyToTopic = state.getAttribute(STATE_REPLY_TOPIC) != null;
 		boolean replyToMsg = state.getAttribute(STATE_REPLY_MSG) != null;
 
 		if (replyToTopic)
 			replyType = "totopic";
 		else if (replyToMsg) replyType = "tomessage";
 
 		String replyToId = "";
 		if (replyType.equalsIgnoreCase("tomessage"))
 		{
 			replyToId = params.getString("messageId");
 		}
 		else if (replyType.equalsIgnoreCase("totopic"))
 		{
 			replyToId = params.getString("topicId");
 		}
 
 		// get info from input
 		String body = params.getCleanString("body");
 		body = processFormattedTextFromBrowser(state, body);
 		String subject = ((String) params.getString("subject")).trim();
 
 		state.setAttribute(RESPOND_SUBJECT, subject);
 		state.setAttribute(RESPOND_BODY, body);
 
 		if (state.getAttribute(STATE_MESSAGE) == null)
 		{
 			if (subject.length() == 0)
 			{
 				addAlert(state, rb.getString("plespe1"));
 			}
 			else
 			{
 				try
 				{
 					DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state
 							.getAttribute(STATE_CHANNEL_REF));
 
 					try
 					{
 						// message is a draft?
 						boolean draft = true;
 
 						// get the message thread before adding a new message
 						DiscussionMessage originalMessage = channel.getDiscussionMessage(replyToId);
 						String category = originalMessage.getDiscussionHeader().getCategory();
 						Vector v = (Vector) ((Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST)).get(category);
 						int index = v.indexOf(originalMessage.getId());
 						Iterator l = originalMessage.getReplies();
 
 						// insert the response message
 						DiscussionMessage addedMessage = channel.addDiscussionMessage(category, subject, draft, replyToId,
 								(List) state.getAttribute(ATTACHMENTS), body);
 						showMessage(runData, addedMessage.getId());
 
 						if (!isMessageExpanded(state, originalMessage))
 						{
 							// the replied to message has not been expanded
 							setMessageExpanded(state, originalMessage, true, channel);
 						}
 						else
 						{
 							if ((l == null) || (!l.hasNext()))
 							{
 								// if there is no reply message yet
 								v.add(index + 1, addedMessage.getId());
 							}
 							else
 							{
 								// the replied to message has already been expanded
 								// if the new message is a draft message, add to the bottom of drafts
 								int firstNondraftIndex = -1;
 								while (l.hasNext())
 								{
 									DiscussionMessage next = (DiscussionMessage) l.next();
 									if (next.getDiscussionHeader().getDraft())
 									{
 										firstNondraftIndex = v.indexOf(next.getId());
 									}
 								}
 
 								if (firstNondraftIndex == -1)
 								{
 									// no draft till now
 									firstNondraftIndex = index + 1;
 								}
 								else
 								{
 									// otherwise
 									firstNondraftIndex++;
 								}
 								v.add(firstNondraftIndex, addedMessage.getId());
 								setCategoryShowList(state, category, v);
 							} // if - else
 
 						} // if-else
 
 					}
 					catch (PermissionException e)
 					{
 						addAlert(state, PERMISSION_HEADER_STRING + rb.getString("res"));
 					}
 					catch (IdUnusedException e)
 					{
 						addAlert(state, rb.getString("cannotfin3"));
 					}
 				}
 				catch (PermissionException e)
 				{
 					addAlert(state, rb.getString("youdonot3"));
 				}
 				catch (IdUnusedException e)
 				{
 					addAlert(state, rb.getString("cannotfin5"));
 				}
 			}
 		} // if
 
 		if (state.getAttribute(STATE_MESSAGE) == null)
 		{
 			// respond sucessful
 			state.removeAttribute(RESPOND_REPLY_TO);
 			state.removeAttribute(RESPOND_SUBJECT);
 			state.removeAttribute(RESPOND_BODY);
 			state.removeAttribute(RESPOND_ATTACHMENT);
 			state.removeAttribute(STATE_MODE);
 
 			schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 		} // if-else
 
 	} // doRespondDraft
 
 	/**
 	 * Cancel from the reply page
 	 */
 	public void doCancel_reply(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.removeAttribute(RESPOND_REPLY_TO);
 		state.removeAttribute(RESPOND_SUBJECT);
 		state.removeAttribute(RESPOND_BODY);
 		state.removeAttribute(RESPOND_ATTACHMENT);
 		state.removeAttribute(ATTACHMENTS);
 		state.removeAttribute(STATE_MODE);
 
 	} // doCancel_reply
 
 	/**
 	 * Cancel from the reply preview page
 	 */
 	public void doCancel_reply_preview(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.setAttribute(STATE_MODE, MODE_REPLY);
 
 	} // doCancel_reply
 
 	/**
 	 * confirm the deletion of category
 	 */
 	public void doDelete_category_confirm(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		ParameterParser params = data.getParameters();
 		String category = FormattedText.decodeNumericCharacterReferences(params.getString("category"));
 		state.setAttribute(STATE_MODE, MODE_DELETE_CATEGORY_CONFIRM);
 		state.setAttribute(DELETE_CATEGORY, category);
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 	} // doDelete_category_confirm
 
 	/**
 	 * cancel the deletion of category
 	 */
 	public void doCancel_delete_category(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.setAttribute(DELETE_CATEGORY, "");
 		state.removeAttribute(STATE_MODE);
 	} // doCancel_delelete_category
 
 	/**
 	 * delete the category
 	 */
 	public void doDelete_category(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		ParameterParser params = data.getParameters();
 		String category = params.getString("category");
 
 		try
 		{
 			// remove the category from the channel
 			DiscussionChannel channel = (DiscussionChannel) DiscussionService.getChannel((String) state
 					.getAttribute(STATE_CHANNEL_REF));
 			try
 			{
 				if (!channel.removeCategory(category))
 				{
 					state.setAttribute(DELETE_WARNING, rb.getString("cannotfin7"));
 				}
 			}
 			catch (InUseException e)
 			{
 				state.setAttribute(DELETE_WARNING, rb.getString("someone") + " channel. ");
 			}
 			catch (PermissionException e)
 			{
 				state.setAttribute(DELETE_WARNING, rb.getString("youarenot2"));
 			}
 
 			// remove the category from the set of categories expanded on the user interface
 			HashSet expandedCategories = (HashSet) state.getAttribute(STATE_EXPAND_CATEGORY_LIST);
 			expandedCategories.remove(category);
 			state.setAttribute(STATE_EXPAND_CATEGORY_LIST, expandedCategories);
 		}
 		catch (IdUnusedException e)
 		{
 			state.setAttribute(DELETE_WARNING, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			state.setAttribute(DELETE_WARNING, rb.getString("youarenot2"));
 		}
 
 		state.setAttribute(DELETE_CATEGORY, "");
 		state.removeAttribute(STATE_MODE);
 
 	} // doDelete_category
 
 	/**
 	 * confirm the deletion of message
 	 */
 	public void doDelete_message_confirm(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		String messageId = data.getParameters().getString("messageId");
 
 		state.setAttribute(STATE_MODE, MODE_DELETE_MESSAGE_CONFIRM);
 		state.setAttribute(DELETE_MESSAGE_ID, messageId);
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 	} // doDelete_message_confirm
 
 	public void doDelete_message(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		ParameterParser params = data.getParameters();
 		String messageId = params.getString("messageId");
 
 		try
 		{
 			DiscussionChannel channel = (DiscussionChannel) DiscussionService.getDiscussionChannel((String) state
 					.getAttribute(STATE_CHANNEL_REF));
 
 			try
 			{
 				DiscussionMessage message = (DiscussionMessageEdit) channel.getMessage(messageId);
 				String category = message.getDiscussionHeader().getCategory();
 
 				// remove all the replies to the message
 				Iterator replies = channel.getThread(message);
 				while (replies.hasNext())
 				{
 					// remove the message from the channel
 					DiscussionMessage reply = (DiscussionMessage) replies.next();
 					channel.removeMessage(reply.getId());
 
 					// remove the message from the show list
 					Hashtable t = (Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST);
 					Vector v = (Vector) t.get(category);
 					v.remove(reply.getId());
 					t.put(category, v);
 					state.setAttribute(STATE_CATEGORIES_SHOW_LIST, t);
 
 					// remove the message from the expanded message list if necessary
 					Hashtable expandMessageTable = (Hashtable) state.getAttribute(STATE_EXPAND_MESSAGE_LIST);
 					HashSet expandMessageCategory = (HashSet) expandMessageTable.get(category);
 					if (expandMessageCategory != null)
 					{
 						expandMessageCategory.remove(reply.getId());
 					}
 				}
 				// remove the message
 				channel.removeMessage(message.getId());
 
 				// remove the message from the show list
 				Hashtable t = (Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST);
 				Vector v = (Vector) t.get(category);
 				v.remove(messageId);
 				t.put(category, v);
 				state.setAttribute(STATE_CATEGORIES_SHOW_LIST, t);
 
 				// remove the message from the expanded message list if necessary
 				Hashtable expandMessageTable = (Hashtable) state.getAttribute(STATE_EXPAND_MESSAGE_LIST);
 				HashSet expandMessageCategory = (HashSet) expandMessageTable.get(category);
 				if (expandMessageCategory != null)
 				{
 					expandMessageCategory.remove(messageId);
 				}
 			}
 			catch (IdUnusedException e)
 			{
 				state.setAttribute(DELETE_WARNING, rb.getString("cannotfin6"));
 			}
 			catch (PermissionException e)
 			{
 				// addAlert(state, rb.getString("youarenot1"));
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			state.setAttribute(DELETE_WARNING, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			state.setAttribute(DELETE_WARNING, rb.getString("youarenot1"));
 		}
 
 		state.removeAttribute(STATE_DISPLAY_MESSAGE);
 		state.removeAttribute(STATE_MODE);
 
 	} // dodelete_message
 
 	/**
 	 * cancel the deletion of message
 	 */
 	public void doCancel_delete_message(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.setAttribute(ATTACHMENTS, null);
 		state.setAttribute(DELETE_MESSAGE_ID, "");
 		state.removeAttribute(STATE_MODE);
 	} // doCancel_delelete_category
 
 	/**
 	 * Handle the eventSubmit_doCancel_show_topic_content command to abort the show topic content page.
 	 */
 	public void doCancel_show_topic_content(RunData data)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.removeAttribute(STATE_DISPLAY_MESSAGE);
 		state.removeAttribute(STATE_SHOW_CONTENT_TOPIC_ID);
 		state.removeAttribute(STATE_MODE);
 
 	} // doCancel_show_topic_content
 
 	/**
 	 * Handle the eventSubmit_doCancel_draft to stop showing the draft message
 	 */
 	public void doCancel_draft(RunData data)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.removeAttribute(DRAFT_MESSAGE_CATEGORY);
 		state.removeAttribute(DRAFT_MESSAGE_SUBJECT);
 		state.removeAttribute(DRAFT_MESSAGE_BODY);
 		state.removeAttribute(DRAFT_MESSAGE_REPLY_STYLE);
 		state.removeAttribute(STATE_DISPLAY_MESSAGE);
 		state.setAttribute(ATTACHMENTS, null);
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + MONITOR_PANEL);
 	} // doCancel_draft
 
 	/**
 	 * Handle the eventSubmit_doCancel_new_topic to stop showing the new topic page
 	 */
 	public void doCancel_new_topic(RunData data)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.removeAttribute(NEW_TOPIC_NEW_CATEGORY);
 		state.removeAttribute(NEW_TOPIC_CATEGORY);
 		state.removeAttribute(NEW_TOPIC_SUBJECT);
 		state.removeAttribute(NEW_TOPIC_BODY);
 		state.removeAttribute(NEW_TOPIC_REPLY_STYLE);
 		state.removeAttribute(STATE_DISPLAY_MESSAGE);
 
 		// clean state mode
 		state.removeAttribute(STATE_MODE);
 
 		state.setAttribute(ATTACHMENTS, null);
 	} // doCancel_new_topic
 
 	/**
 	 * Handle the eventSubmit_doCancel_new_category to stop showing the new category page
 	 */
 	public void doCancel_new_category(RunData data)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.removeAttribute(STATE_DISPLAY_MESSAGE);
 
 		// clean state mode
 		state.removeAttribute(STATE_MODE);
 
 	} // doCancel_new_category
 
 	/**
 	 * To show the topic content when "add attachments" is clicked
 	 */
 	public void doShow_topic_content(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		ParameterParser params = data.getParameters();
 		String topicId = params.getString("topicId");
 		state.setAttribute(STATE_SHOW_CONTENT_TOPIC_ID, topicId);
 		state.setAttribute(STATE_MODE, MODE_SHOW_TOPIC_CONTENT);
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 
 	} // doShow_topic_content
 
 	/**
 	 * Show the discussion message
 	 */
 	public void doShow(RunData data, Context context)
 	{
 		ParameterParser params = data.getParameters();
 
 		// set current message id
 		String messageId = params.getString("messageId");
 
 		showMessage(data, messageId);
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + TOOLBAR_PANEL);
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + CONTROL_PANEL);
 	} // doShow
 
 	private void showMessage(RunData data, String messageId)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		try
 		{
 			String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 
 			try
 			{
 				DiscussionMessage m = channel.getDiscussionMessage(messageId);
 				DiscussionMessageHeader mHeader = m.getDiscussionHeader();
 				if (mHeader.getDraft())
 				{
 					state.setAttribute(DRAFT_MESSAGE_CATEGORY, mHeader.getCategory());
 					state.setAttribute(DRAFT_MESSAGE_BODY, m.getBody());
 					state.setAttribute(DRAFT_MESSAGE_SUBJECT, mHeader.getSubject());
 					if (m.getReplyToDepth() == 0)
 					{
 						ResourceProperties mProperties = m.getProperties();
 						state.setAttribute(DRAFT_MESSAGE_REPLY_STYLE, mProperties.getProperty(mProperties.getNamePropReplyStyle()));
 					}
 					state.setAttribute(ATTACHMENTS, mHeader.getAttachments());
 				}
 				else
 				{
 					state.setAttribute(ATTACHMENTS, null);
 				}
 				DisplayMessage dMessage = new DisplayMessage(messageId);
 				state.setAttribute(STATE_DISPLAY_MESSAGE, dMessage);
 
 				// update the visited messages list
 				HashSet visited = (HashSet) state.getAttribute(VISITED_MESSAGES);
 				visited.add(messageId);
 				state.setAttribute(VISITED_MESSAGES, visited);
 			}
 			catch (IdUnusedException e)
 			{
 				addAlert(state, rb.getString("cannotfin2"));
 			}
 			catch (PermissionException e)
 			{
 				addAlert(state, rb.getString("youarenot3"));
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youarenot4"));
 		}
 	} // showMessage
 
 	/**
 	 * Preview the response
 	 */
 	public void doPreview(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		ParameterParser params = data.getParameters();
 
 		// save the input infos for the respond message
 		// String replyType = params.getString("replyto");
 		String replyType = "";
 
 		boolean replyToTopic = state.getAttribute(STATE_REPLY_TOPIC) != null;
 		boolean replyToMsg = state.getAttribute(STATE_REPLY_MSG) != null;
 
 		if (replyToTopic)
 			replyType = "totopic";
 		else if (replyToMsg) replyType = "tomessage";
 
 		String replyToId = "";
 		if (replyType.equalsIgnoreCase("tomessage"))
 		{
 			replyToId = params.getString("messageId");
 		}
 		else if (replyType.equalsIgnoreCase("totopic"))
 		{
 			replyToId = params.getString("topicId");
 		}
 		state.setAttribute(RESPOND_REPLY_TO, replyToId);
 
 		String subject = ((String) params.getString("subject")).trim();
 		state.setAttribute(RESPOND_SUBJECT, StringUtil.trimToZero(subject));
 
 		String body = params.getCleanString("body");
 		body = processFormattedTextFromBrowser(state, body);
 		state.setAttribute(RESPOND_BODY, body);
 
 		Vector attachments = (Vector) state.getAttribute(ATTACHMENTS);
 		state.setAttribute(RESPOND_ATTACHMENT, attachments);
 
 		if (subject == null || subject.length() == 0)
 		{
 			addAlert(state, rb.getString("plespe1"));
 		}
 
 		if (state.getAttribute(STATE_MESSAGE) == null)
 		{
 			state.setAttribute(STATE_MODE, MODE_REPLY_PREVIEW);
 		}
 
 	} // doPreview
 
 	/**
 	 * set the form for replying
 	 */
 	public void doSet_reply(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		String currentMessageId = ((DisplayMessage) state.getAttribute(STATE_DISPLAY_MESSAGE)).getId();
 		state.setAttribute(RESPOND_REPLY_TO, currentMessageId);
 		state.setAttribute(RESPOND_ATTACHMENT, new Vector());
 		state.setAttribute(STATE_MODE, MODE_REPLY);
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 
 	} // doSet_reply
 
 	/**
 	 * set the form for replying message only
 	 */
 	public void doSet_reply_msg(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		String currentMessageId = ((DisplayMessage) state.getAttribute(STATE_DISPLAY_MESSAGE)).getId();
 		state.setAttribute(RESPOND_REPLY_TO, currentMessageId);
 		state.setAttribute(RESPOND_ATTACHMENT, new Vector());
 		state.setAttribute(STATE_MODE, MODE_REPLY);
 		state.setAttribute(STATE_REPLY_MSG, Boolean.TRUE);
 		state.removeAttribute(STATE_REPLY_TOPIC);
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 	} // doSet_reply_msg
 
 	/**
 	 * set the form for replying
 	 */
 	public void doSet_reply_topic(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.setAttribute(RESPOND_REPLY_TO, data.getParameters().getString("topicId"));
 		state.setAttribute(RESPOND_ATTACHMENT, new Vector());
 		state.setAttribute(STATE_MODE, MODE_REPLY);
 		state.setAttribute(STATE_REPLY_TOPIC, Boolean.TRUE);
 		state.removeAttribute(STATE_REPLY_MSG);
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 
 	} // doSet_reply_topic
 
 	/**
 	 * get the control pannel ready for input new topic
 	 */
 	public void doSet_new_topic(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.setAttribute(ATTACHMENTS, null);
 		state.setAttribute(STATE_MODE, MODE_NEW_TOPIC);
 		addAlert(state, "");
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 
 	} // doSet_new_topic
 
 	/**
 	 * get the control pannel ready for input new category
 	 */
 	public void doSet_new_category(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		state.setAttribute(ATTACHMENTS, null);
 		state.setAttribute(STATE_MODE, MODE_NEW_CATEGORY);
 		addAlert(state, "");
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 
 	} // doSet_new_category
 
 	/**
 	 * Setup for options. %%% Note: not sure this and doUpdate are correct, not currently invoked (no menu entry) -ggolden
 	 */
 	public String buildOptionsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
 	{
 		// set the action for form processing
 		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
 		context.put("form-submit", BUTTON + "doUpdate");
 
 		// pick the "-customize" template based on the standard template name
 		String template = (String) getContext(rundata).get("template");
 		return template + "-customize";
 
 	} // buildOptionsPanelContext
 
 	/**
 	 * doUpdate called for form input tags type="submit" named="eventSubmit_doUpdate"
 	 */
 	public void doUpdate(RunData data, Context context)
 	{
 		// access the portlet element id to find our state
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
 
 		// channel
 		String channel = data.getParameters().getString(FORM_CHANNEL);
 		if (!channel.equals((String) state.getAttribute(STATE_CHANNEL_REF)))
 		{
 			state.setAttribute(STATE_CHANNEL_REF, channel);
 			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + ".doUpdate(): channel: " + channel);
 			updateObservationOfChannel(state, ((JetspeedRunData) data).getJs_peid());
 			state.setAttribute(STATE_UPDATE, STATE_UPDATE);
 		}
 
 		// ascending
 		String ascending = data.getParameters().getString(FORM_ASCENDING);
 		Boolean asc = new Boolean(ascending);
 		if (!asc.equals((Boolean) state.getAttribute(STATE_ASCENDING)))
 		{
 			state.setAttribute(STATE_ASCENDING, asc);
 			state.setAttribute(STATE_UPDATE, STATE_UPDATE);
 		}
 
 		// we are done with customization
 		// %%% clear or change mode to something like list to leave the options mode
 
 	} // doUpdate
 
 	/**
 	 * get the categories sorted under the sorting criteria
 	 */
 	private List sortedCategories(DiscussionChannel channel, String sortedBy, String sortedAsc)
 	{
 		// categories
 		List categories = channel.getCategories(true);
 		if (categories == null) return null;
 
 		// return the categories alpha sorted
 		Collections.sort(categories);
 
 		return categories;
 
 	} // sortedCategories
 
 	/**
 	 * transforms the Iterator to Vector
 	 */
 	private Vector iterator_to_vector(Iterator l)
 	{
 		Vector v = new Vector();
 		while (l.hasNext())
 		{
 			v.add(l.next());
 		}
 		return v;
 	} // iterator_to_vector
 
 	/**
 	 * set the category in the expanded category list?
 	 */
 	private void setCategoryExpanded(SessionState state, String category, boolean expand, DiscussionChannel channel)
 	{
 		HashSet s = (HashSet) state.getAttribute(STATE_EXPAND_CATEGORY_LIST);
 
 		if (expand)
 		{
 			s.add(category);
 
 			// the show topics in category
 			Vector topics = iterator_to_vector(channel.getTopics(category));
 			Vector drafts = new Vector();
 			Vector nonDrafts = new Vector();
 			Vector showTopicsList = new Vector();
 
 			// devide the drafted messages and posted messages; drafted message come first
 			for (int j = 0; j < topics.size(); j++)
 			{
 				try
 				{
 					DiscussionMessage m = (DiscussionMessage) topics.get(j);
 
 					// filter out drafts not by this user (unless this user is a super user) %%% not sure I like this -zqian
 					channel.getMessage(m.getId());
 					if (m.getDiscussionHeader().getDraft())
 					{
 						drafts.add(m.getId());
 					}
 					else
 					{
 						nonDrafts.add(m.getId());
 					}
 				}
 				catch (PermissionException e)
 				{
 				}
 				catch (IdUnusedException e)
 				{
 				}
 			}
 			showTopicsList.addAll(drafts);
 			showTopicsList.addAll(nonDrafts);
 			setCategoryShowList(state, category, showTopicsList);
 		}
 		else
 		{
 			s.remove(category);
 			setCategoryShowList(state, category, new Vector());
 
 			// hide the show message
 			Hashtable t = (Hashtable) state.getAttribute(STATE_EXPAND_MESSAGE_LIST);
 			t.put(category, new HashSet());
 		}
 	} // setCategoryExpanded
 
 	/**
 	 * Toggle the state attribute
 	 * 
 	 * @param stateName
 	 *        The name of the state attribute to toggle
 	 */
 	private void toggleState(RunData runData, String stateName)
 	{
 		// access the portlet element id to find our state
 		// %%% use CHEF api instead of Jetspeed to get state
 		String peid = ((JetspeedRunData) runData).getJs_peid();
 		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);
 
 		// toggle the state setting
 		boolean newValue = !((Boolean) state.getAttribute(stateName)).booleanValue();
 		state.setAttribute(stateName, new Boolean(newValue));
 
 	} // toggleState
 
 	/**
 	 * Show the whole message hireachy
 	 */
 	public void doExpand_all(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 
 			// the sorting defaults for the outline view
 			String sortedBy = "";
 			String sortedAsc = (String) state.getAttribute(STATE_SORTED_ASC);
 
 			// set default sort attribute
 			sortedBy = (String) state.getAttribute(STATE_SORTED_BY);
 			if ((!sortedBy.equals(STATE_SORTED_BY_TOPIC)) && (!sortedBy.equals(STATE_SORTED_BY_AUTHOR))
 					&& (!sortedBy.equals(STATE_SORTED_BY_DATE)))
 			{
 				sortedBy = STATE_SORTED_BY_DATE;
 				state.setAttribute(STATE_SORTED_BY, sortedBy);
 			}
 
 			// get the sorted categories Vector according to the current sorting criteria
 			List categories = sortedCategories(channel, sortedBy, sortedAsc);
 			if (categories != null)
 			{
 				for (Iterator i = categories.iterator(); i.hasNext();)
 				{
 					String cat = (String) i.next();
 
 					setCategoryExpanded(state, cat, true, channel);
 					Iterator topics = channel.getTopics(cat);
 					while (topics.hasNext())
 					{
 						// expand all the message in the thread
 						DiscussionMessage m = (DiscussionMessage) topics.next();
 						setMessageExpanded(state, m, true, channel);
 						Iterator replyThread = channel.getThread(m);
 						while (replyThread.hasNext())
 						{
 							setMessageExpanded(state, (DiscussionMessage) replyThread.next(), true, channel);
 						}
 					}
 				}
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youarenot4"));
 		}
 
 		toggleState(data, STATE_EXPAND_ALL_FLAG);
 
 		// schedule a refresh of the "List" panel
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		// String address = ((EventObservingCourier) state.getAttribute(STATE_OBSERVER)).getDeliveryId();
 		// String elementID = ((EventObservingCourier) state.getAttribute(STATE_OBSERVER)).getElementId();
 		schedulePeerFrameRefresh(mainPanelUpdateId(peid) + "." + MONITOR_PANEL);
 
 	} // doExpand_all
 
 	/**
 	 * Hide the whole message hireachy
 	 */
 	public void doCollapse_all(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 
 			// the sorting defaults for the outline view
 			String sortedBy = "";
 			String sortedAsc = (String) state.getAttribute(STATE_SORTED_ASC);
 
 			// set default sort attribute
 			sortedBy = (String) state.getAttribute(STATE_SORTED_BY);
 			if ((!sortedBy.equals(STATE_SORTED_BY_TOPIC)) && (!sortedBy.equals(STATE_SORTED_BY_AUTHOR))
 					&& (!sortedBy.equals(STATE_SORTED_BY_DATE)))
 			{
 				sortedBy = STATE_SORTED_BY_DATE;
 				state.setAttribute(STATE_SORTED_BY, sortedBy);
 			}
 			// get the sorted categories Vector according to the current sorting criteria
 			List categories = sortedCategories(channel, sortedBy, sortedAsc);
 			if (categories != null)
 			{
 				for (Iterator i = categories.iterator(); i.hasNext();)
 				{
 					setCategoryExpanded(state, (String) i.next(), false, channel);
 				}
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youarenot4"));
 		}
 
 		// remove current display message due to close of categories
 		state.setAttribute(STATE_DISPLAY_MESSAGE, new DisplayMessage(""));
 
 		toggleState(data, STATE_EXPAND_ALL_FLAG);
 
 		// schedule a refresh of the "List" panel
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		// String address = ((EventObservingCourier) state.getAttribute(STATE_OBSERVER)).getDeliveryId();
 		// String elementID = ((EventObservingCourier) state.getAttribute(STATE_OBSERVER)).getElementId();
 		schedulePeerFrameRefresh(mainPanelUpdateId(peid) + "." + MONITOR_PANEL);
 
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + CONTROL_PANEL);
 
 	} // doCollapse_all
 
 	/**
 	 * is the message id in the expanded message list?
 	 */
 	private boolean isMessageExpanded(SessionState state, DiscussionMessage message)
 	{
 		String category = message.getDiscussionHeader().getCategory();
 		Hashtable expandMessageTable = (Hashtable) state.getAttribute(STATE_EXPAND_MESSAGE_LIST);
 		HashSet s = (HashSet) expandMessageTable.get(category);
 		if (s != null)
 			return s.contains(message);
 		else
 			return false;
 
 	} // isMessageExpanded
 
 	/**
 	 * set the message id in the expanded message list?
 	 */
 	private void setMessageExpanded(SessionState state, DiscussionMessage message, boolean expand, DiscussionChannel channel)
 	{
 		// get the current category message list
 		String category = message.getDiscussionHeader().getCategory();
 		Vector v = (Vector) ((Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST)).get(category);
 
 		// get the show message
 		Hashtable expandMessageTable = (Hashtable) state.getAttribute(STATE_EXPAND_MESSAGE_LIST);
 		HashSet expandMessageCategory = (HashSet) expandMessageTable.get(category);
 		if (expandMessageCategory == null)
 		{
 			expandMessageCategory = new HashSet();
 		}
 
 		// get the index of the original message
 		int index = v.indexOf(message.getId());
 
 		if (expand)
 		{
 			expandMessageCategory.add(message);
 
 			// divide the drafted messages and posted messages, drafted message come first
 			Vector drafts = new Vector();
 			Vector nonDrafts = new Vector();
 			Vector showMessagesList = new Vector();
 			Vector replies = iterator_to_vector(message.getReplies());
 			for (int j = 0; j < replies.size(); j++)
 			{
 				DiscussionMessage m = (DiscussionMessage) replies.get(j);
 				try
 				{
 					// filter out drafts not by this user (unless this user is a super user) %%% not sure I like this -zqian
 					channel.getDiscussionMessage(m.getId());
 					if (m.getDiscussionHeader().getDraft())
 					{
 						drafts.add(m.getId());
 					}
 					else
 					{
 						nonDrafts.add(m.getId());
 					}
 				}
 				catch (IdUnusedException e)
 				{
 				}
 				catch (PermissionException e)
 				{
 				}
 			}
 			showMessagesList.addAll(drafts);
 			showMessagesList.addAll(nonDrafts);
 
 			v.addAll(index + 1, showMessagesList);
 		}
 		else
 		{
 			expandMessageCategory.remove(message);
 			// remove the whole thread
 			Vector replies = iterator_to_vector(channel.getThread(message));
 			for (int j = 0; j < replies.size(); j++)
 			{
 				DiscussionMessage m = (DiscussionMessage) replies.get(j);
 				if (v.contains(m.getId()))
 				{
 					v.remove(m.getId());
 				}
 			}
 		}
 
 		setCategoryShowList(state, category, v);
 		expandMessageTable.put(category, expandMessageCategory);
 	} // setMessageExpanded
 
 	/**
 	 * set the show list for each category
 	 */
 	private void setCategoryShowList(SessionState state, String category, Vector showList)
 	{
 		Hashtable t = (Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST);
 		t.put(category, showList);
 		state.setAttribute(STATE_CATEGORIES_SHOW_LIST, t);
 	} // setCategoryShowList
 
 	/**
 	 * doExpandmessage expand the message with the content and replies
 	 */
 	public void doExpand_message(RunData data, Context context)
 	{
 		String messageId = data.getParameters().getString("messageId");
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 
 			try
 			{
 				setMessageExpanded(state, channel.getDiscussionMessage(messageId), true, channel);
 				showMessage(data, messageId);
 				String peid = ((JetspeedRunData) data).getJs_peid();
 				schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + TOOLBAR_PANEL);
 				schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + CONTROL_PANEL);
 			}
 			catch (IdUnusedException e)
 			{
 				addAlert(state, rb.getString("cannotfin4"));
 			}
 			catch (PermissionException e)
 			{
 				addAlert(state, PERMISSION_HEADER_STRING + rb.getString("toview"));
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youarenot4"));
 		}
 
 	} // doExpand_message
 
 	/**
 	 * doCollapsemessage hides all the messages inside the category
 	 */
 	public void doCollapse_message(RunData data, Context context)
 	{
 		String messageId = data.getParameters().getString("messageId");
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		try
 		{
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel((String) state.getAttribute(STATE_CHANNEL_REF));
 			try
 			{
 				DiscussionMessage message = channel.getDiscussionMessage(messageId);
 
 				// set the message itself to be collapsed
 				setMessageExpanded(state, message, false, channel);
 
 				// the current message been displayed
 				String currentMessageId = ((DisplayMessage) state.getAttribute(STATE_DISPLAY_MESSAGE)).getId();
 				boolean found = false;
 
 				Iterator replies = channel.getThread(message);
 				while (replies.hasNext())
 				{
 					DiscussionMessage nextReply = (DiscussionMessage) replies.next();
 
 					// set the message's reply to be collapsed
 					setMessageExpanded(state, nextReply, false, channel);
 
 					if ((!found) && currentMessageId.equals(nextReply.getId()))
 					{
 						// remove the current message due to close of thread
 						state.setAttribute(STATE_DISPLAY_MESSAGE, new DisplayMessage(""));
 						found = true;
 					}
 
 				}
 			}
 			catch (IdUnusedException e)
 			{
 				addAlert(state, rb.getString("cannotfin4"));
 			}
 			catch (PermissionException e)
 			{
 				addAlert(state, PERMISSION_HEADER_STRING + rb.getString("toview"));
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youdonot1"));
 		}
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + CONTROL_PANEL);
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + TOOLBAR_PANEL);
 
 	} // doCollapse_message
 
 	/**
 	 * doExpandcategory shows all the topics inside the category
 	 */
 	public void doExpand_category(RunData data, Context context)
 	{
 		String category = data.getParameters().getString("category");
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		try
 		{
 			// get the current channel ID from state object
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 			setCategoryExpanded(state, category, true, channel);
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youdonot1"));
 		}
 
 	} // doExpand_category
 
 	/**
 	 * doCollapsecategory hides all the topics inside the category
 	 */
 	public void doCollapse_category(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		String category = data.getParameters().getString("category");
 
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		try
 		{
 			// get the current channel ID from state object
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 
 			String currentId = ((DisplayMessage) state.getAttribute(STATE_DISPLAY_MESSAGE)).getId();
 			if (!currentId.equals(""))
 			{
 				DiscussionMessage message = channel.getDiscussionMessage(currentId);
 				String mCategory = message.getDiscussionHeader().getCategory();
 				if (mCategory.equals(category))
 				{
 					// remove current display message due to close of category
 					state.setAttribute(STATE_DISPLAY_MESSAGE, new DisplayMessage(""));
 				}
 			}
 			setCategoryExpanded(state, category, false, channel);
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youdonot1"));
 		}
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + CONTROL_PANEL);
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + TOOLBAR_PANEL);
 
 	} // doCollapse_category
 
 	/**
 	 * the class which is using by one of the iFrames to show the content of selected message
 	 */
 	public class DisplayMessage
 	{
 		String m_id = null;
 
 		public DisplayMessage(String id)
 		{
 			m_id = id;
 		}
 
 		public void setId(String id)
 		{
 			m_id = id;
 		}
 
 		public String getId()
 		{
 			return m_id;
 		}
 
 	} // DisplayMessage
 
 	/**
 	 * add the category to the state attribute
 	 */
 	private void addCategory(SessionState state, DiscussionChannel channel, String category) throws InUseException,
 			PermissionException
 	{
 		try
 		{
 			channel.addCategory(category);
 
 			HashSet expandedCategories = (HashSet) state.getAttribute(STATE_EXPAND_CATEGORY_LIST);
 			expandedCategories.add(category);
 			state.setAttribute(STATE_EXPAND_CATEGORY_LIST, expandedCategories);
 
 			Hashtable h = (Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST);
 			h.put(category, new Vector());
 			state.setAttribute(STATE_CATEGORIES_SHOW_LIST, h);
 		}
 		catch (InUseException e)
 		{
 			throw new InUseException(channel.getId());
 		}
 		catch (PermissionException e)
 		{
 			throw new PermissionException(null, null, null);
 		}
 
 	} // addCategory
 
 	/**
 	 * Fire up the permissions editor, next request cycle
 	 */
 	public void doPermissions(RunData data, Context context)
 	{
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
 
 		// trigger the switch on the next request (which is going to happen after this action is processed with its redirect response to the build)
 		state.setAttribute(STATE_PERMISSIONS, STATE_PERMISSIONS);
 
 		// schedule a main refresh to excape from the toolbar panel
 		schedulePeerFrameRefresh(mainPanelUpdateId(peid));
 	}
 
 	/**
 	 * Fire up the permissions editor
 	 */
 	protected void doPermissionsNow(RunData data, Context context)
 	{
 		// get into helper mode with this helper tool
 		startHelper(data.getRequest(), "sakai.permissions.helper");
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
 
 		String channelRefStr = (String) state.getAttribute(STATE_CHANNEL_REF);
 		Reference channelRef = EntityManager.newReference(channelRefStr);
 		String siteRef = SiteService.siteReference(channelRef.getContext());
 
 		// setup for editing the permissions of the site for this tool, using the roles of this site, too
 		state.setAttribute(PermissionsHelper.SITE_REF, siteRef);
 
 		// ... with this description
 		state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("setperfor")
 				+ SiteService.getSiteDisplay(channelRef.getContext()));
 
 		// ... showing only locks that are prpefixed with this
 		state.setAttribute(PermissionsHelper.PREFIX, "disc.");
 
 	} // doPermissions
 
 	/**
 	 * Handle a \ request.
 	 */
 	public void doSearch(RunData runData, Context context)
 	{
 		// access the portlet element id to find our state
 		String peid = ((JetspeedRunData) runData).getJs_peid();
 		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);
 
 		// read the search form field into the state object
 		String search = StringUtil.trimToNull(runData.getParameters().getString(FORM_SEARCH));
 
 		// set the flag to go to the prev page on the next list
 		if (search == null)
 		{
 			state.removeAttribute(STATE_SEARCH);
 		}
 		else
 		{
 			state.setAttribute(STATE_SEARCH, search);
 		}
 
 		state.setAttribute(STATE_SEARCH_REFRESH, Boolean.TRUE);
 
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + MONITOR_PANEL);
 
 		// disable auto-updates while in view mode
 		disableObservers(state);
 
 	} // doSearch
 
 	/**
 	 * Handle a Search Clear request.
 	 */
 	public void doSearch_clear(RunData runData, Context context)
 	{
 		// access the portlet element id to find our state
 		String peid = ((JetspeedRunData) runData).getJs_peid();
 		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);
 
 		// clear the search
 		state.removeAttribute(STATE_SEARCH);
 
 		// turn on auto refresh
 		enableObserver(state);
 
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid) + "." + MONITOR_PANEL);
 
 		// make sure auto-updates are enabled
 		enableObserver(state);
 
 	} // doSearch_clear
 
 	/**
 	 * Enable the observer, unless we are in search mode, where we want it disabled.
 	 */
 	public void enableObserver(SessionState state)
 	{
 		// we leave it disabled if we are searching, or if the user has last selected to be manual
 		if (state.getAttribute(STATE_SEARCH) != null)
 		{
 			disableObservers(state);
 		}
 		else
 		{
 			enableObservers(state);
 		}
 
 	} // enableObserver
 
 	public void doView(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		String viewMode = data.getParameters().getString("view");
 		state.setAttribute(STATE_SELECTED_VIEW, viewMode);
 
 		if (viewMode.equalsIgnoreCase(rb.getString("rowlay")))
 		{
 			// not left - right layout
 			state.setAttribute(STATE_LEFT_RIGHT_LAYOUT, new Boolean(false));
 
 			String peid = ((JetspeedRunData) data).getJs_peid();
 			schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 		}
 		else if (viewMode.equalsIgnoreCase(rb.getString("collay")))
 		{
 			// not left - right layout
 			state.setAttribute(STATE_LEFT_RIGHT_LAYOUT, new Boolean(true));
 
 			String peid = ((JetspeedRunData) data).getJs_peid();
 			schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 		}
 	} // doView
 
 	/**
 	 * put those variables related to 2ndToolbar into context
 	 */
 	private void add2ndToolbarFields(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 
 		context.put("searchString", state.getAttribute(STATE_SEARCH));
 		context.put("form_search", FORM_SEARCH);
 		context.put("selectedView", state.getAttribute(STATE_SELECTED_VIEW));
 
 	} // add2ndToolbarFields
 
 	/**
 	 * navigate to previous message in the category
 	 */
 	public void doPre_message(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		try
 		{
 			// get the current channel ID from state object
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 
 			DisplayMessage dMessage = (DisplayMessage) state.getAttribute(STATE_DISPLAY_MESSAGE);
 			String currentId = dMessage.getId();
 			if (!currentId.equals(""))
 			{
 				DiscussionMessage message = channel.getDiscussionMessage(currentId);
 				String category = message.getDiscussionHeader().getCategory();
 				Vector messageIds = (Vector) ((Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST)).get(category);
 				dMessage.setId((String) messageIds.get(messageIds.indexOf(currentId) - 1));
 				state.setAttribute(STATE_DISPLAY_MESSAGE, dMessage);
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youdonot1"));
 		}
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 	} // doPre_message
 
 	/**
 	 * Navigate to next message in the category
 	 */
 	public void doNext_message(RunData data, Context context)
 	{
 		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
 		String channelId = (String) state.getAttribute(STATE_CHANNEL_REF);
 		try
 		{
 			// get the current channel ID from state object
 			DiscussionChannel channel = DiscussionService.getDiscussionChannel(channelId);
 
 			DisplayMessage dMessage = (DisplayMessage) state.getAttribute(STATE_DISPLAY_MESSAGE);
 			String currentId = dMessage.getId();
 			if (!currentId.equals(""))
 			{
 				DiscussionMessage message = channel.getDiscussionMessage(currentId);
 				String category = message.getDiscussionHeader().getCategory();
 				Vector messageIds = (Vector) ((Hashtable) state.getAttribute(STATE_CATEGORIES_SHOW_LIST)).get(category);
 				dMessage.setId((String) messageIds.get(messageIds.indexOf(currentId) + 1));
 				state.setAttribute(STATE_DISPLAY_MESSAGE, dMessage);
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			addAlert(state, rb.getString("cannotfin5"));
 		}
 		catch (PermissionException e)
 		{
 			addAlert(state, rb.getString("youdonot1"));
 		}
 
 		String peid = ((JetspeedRunData) data).getJs_peid();
 		schedulePeerFrameRefresh(VelocityPortletPaneledAction.mainPanelUpdateId(peid));
 
 	} // doNext_message
 
 	/**
 	 * Processes formatted text that is coming back from the browser (from the formatted text editing widget).
 	 * 
 	 * @param state
 	 *        Used to pass in any user-visible alerts or errors when processing the text
 	 * @param strFromBrowser
 	 *        The string from the browser
 	 * @return The formatted text
 	 */
 	private String processFormattedTextFromBrowser(SessionState state, String strFromBrowser)
 	{
 		StringBuffer alertMsg = new StringBuffer();
 		try
 		{
 			String text = FormattedText.processFormattedText(strFromBrowser, alertMsg);
 			if (alertMsg.length() > 0) addAlert(state, alertMsg.toString());
 			return text;
 		}
 		catch (Exception e)
 		{
 			Log.warn("chef", this + ": ", e);
 			return strFromBrowser;
 		}
 	}
 }
