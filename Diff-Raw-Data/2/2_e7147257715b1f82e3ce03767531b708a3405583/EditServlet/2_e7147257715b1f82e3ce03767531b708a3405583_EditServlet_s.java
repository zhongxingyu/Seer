 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.servlets;
 
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.authentication.WikiUserAuth;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.Watchlist;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.parser.ParserUtil;
 import org.jamwiki.utils.DiffUtil;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.NamespaceHandler;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to process topic edits including saving an edit, preview, resolving
  * conflicts and dealing with spam.
  */
 public class EditServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(EditServlet.class.getName());
 	protected static final String JSP_EDIT = "edit.jsp";
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		ModelAndView loginRequired = loginRequired(request, pageInfo);
 		if (loginRequired != null) {
 			return loginRequired;
 		}
 		if (isSave(request)) {
 			save(request, next, pageInfo);
 		} else {
 			edit(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void edit(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
 		Topic topic = loadTopic(virtualWiki, topicName);
 		// topic name might be updated by loadTopic
 		topicName = topic.getName();
 		Integer lastTopicVersionId = retrieveLastTopicVersionId(request, topic);
 		next.addObject("lastTopicVersionId", lastTopicVersionId);
 		loadEdit(request, next, pageInfo, virtualWiki, topicName, true);
 		String contents = null;
 		if (isPreview(request)) {
 			preview(request, next, pageInfo);
 			return;
 		}
 		pageInfo.setContentJsp(JSP_EDIT);
 		if (!StringUtils.isBlank(request.getParameter("topicVersionId"))) {
 			// editing an older version
 			Integer topicVersionId = new Integer(request.getParameter("topicVersionId"));
 			TopicVersion topicVersion = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId.intValue(), null);
 			if (topicVersion == null) {
 				throw new WikiException(new WikiMessage("common.exception.notopic"));
 			}
 			contents = topicVersion.getVersionContent();
 			if (!lastTopicVersionId.equals(topicVersionId)) {
 				next.addObject("topicVersionId", topicVersionId);
 			}
 		} else if (!StringUtils.isBlank(request.getParameter("section"))) {
 			// editing a section of a topic
 			int section = (new Integer(request.getParameter("section"))).intValue();
 			contents = ParserUtil.parseSlice(request.getContextPath(), request.getLocale(), virtualWiki, topicName, section);
 		} else {
 			// editing a full new or existing topic
 			contents = (topic == null) ? "" : topic.getTopicContent();
 		}
 		next.addObject("contents", contents);
 	}
 
 	/**
 	 *
 	 */
 	private boolean handleSpam(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String topicName, String contents) throws Exception {
 		String result = ServletUtil.checkForSpam(request, topicName, contents);
 		if (result == null) {
 			return false;
 		}
 		WikiMessage spam = new WikiMessage("edit.exception.spam", result);
 		next.addObject("spam", spam);
 		String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
 		next.addObject("contents", contents);
 		loadEdit(request, next, pageInfo, virtualWiki, topicName, false);
 		next.addObject("editSpam", "true");
 		pageInfo.setContentJsp(JSP_EDIT);
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	private boolean isPreview(HttpServletRequest request) {
 		return !StringUtils.isBlank(request.getParameter("preview"));
 	}
 
 	/**
 	 *
 	 */
 	private boolean isSave(HttpServletRequest request) {
 		return !StringUtils.isBlank(request.getParameter("save"));
 	}
 
 	/**
 	 *
 	 */
 	private void loadEdit(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String virtualWiki, String topicName, boolean useSection) throws Exception {
 		pageInfo.setPageTitle(new WikiMessage("edit.title", topicName));
 		pageInfo.setTopicName(topicName);
 		WikiLink wikiLink = LinkUtil.parseWikiLink(topicName);
 		String namespace = wikiLink.getNamespace();
 		if (namespace != null && namespace.equals(NamespaceHandler.NAMESPACE_CATEGORY)) {
 			ServletUtil.loadCategoryContent(next, virtualWiki, topicName);
 		}
 		if (request.getParameter("editComment") != null) {
 			next.addObject("editComment", request.getParameter("editComment"));
 		}
 		if (useSection && request.getParameter("section") != null) {
 			next.addObject("section", request.getParameter("section"));
 		}
 		next.addObject("minorEdit", new Boolean(request.getParameter("minorEdit") != null));
 		Watchlist watchlist = ServletUtil.currentWatchlist(request, virtualWiki);
 		if (request.getParameter("watchTopic") != null || (watchlist.containsTopic(topicName) && !isPreview(request))) {
 			next.addObject("watchTopic", new Boolean(true));
 		}
 	}
 
 	/**
 	 * Initialize topic values for the topic being edited.  If a topic with
 	 * the specified name already exists then it will be initialized,
 	 * otherwise a new topic is created.
 	 */
 	private Topic loadTopic(String virtualWiki, String topicName) throws Exception {
 		Topic topic = ServletUtil.initializeTopic(virtualWiki, topicName);
 		if (topic.getReadOnly()) {
 			throw new WikiException(new WikiMessage("error.readonly"));
 		}
 		return topic;
 	}
 
 	/**
 	 *
 	 */
 	private ModelAndView loginRequired(HttpServletRequest request, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
 		WikiUserAuth user = ServletUtil.currentUser();
 		if (ServletUtil.isEditable(virtualWiki, topicName, user)) {
 			return null;
 		}
 		if (!user.hasRole(Role.ROLE_EDIT_EXISTING)) {
 			WikiMessage messageObject = new WikiMessage("login.message.edit");
 			return ServletUtil.viewLogin(request, pageInfo, WikiUtil.getTopicFromURI(request), messageObject);
 		}
 		if (!user.hasRole(Role.ROLE_EDIT_NEW) && WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null) == null) {
 			WikiMessage messageObject = new WikiMessage("login.message.editnew");
 			return ServletUtil.viewLogin(request, pageInfo, WikiUtil.getTopicFromURI(request), messageObject);
 		}
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			// this should never trigger, but better safe than sorry...
 			return null;
 		}
 		if (topic.getAdminOnly()) {
 			WikiMessage messageObject = new WikiMessage("login.message.editadmin", topicName);
 			return ServletUtil.viewLogin(request, pageInfo, WikiUtil.getTopicFromURI(request), messageObject);
 		}
 		if (topic.getReadOnly()) {
 			throw new WikiException(new WikiMessage("error.readonly"));
 		}
 		// it should be impossible to get here...
 		throw new WikiException(new WikiMessage("error.unknown", "Unable to determine topic editing permissions"));
 	}
 
 	/**
 	 *
 	 */
 	private void preview(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
 		String contents = (String)request.getParameter("contents");
 		Topic previewTopic = new Topic();
 		previewTopic.setName(topicName);
 		previewTopic.setTopicContent(contents);
 		previewTopic.setVirtualWiki(virtualWiki);
 		next.addObject("editPreview", "true");
 		pageInfo.setContentJsp(JSP_EDIT);
 		next.addObject("contents", contents);
 		ServletUtil.viewTopic(request, next, pageInfo, null, previewTopic, false);
 	}
 
 	/**
 	 *
 	 */
 	private void resolve(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
 		Topic lastTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		String contents1 = lastTopic.getTopicContent();
 		String contents2 = request.getParameter("contents");
 		next.addObject("lastTopicVersionId", lastTopic.getCurrentVersionId());
 		next.addObject("contents", contents1);
 		next.addObject("contentsResolve", contents2);
 		Vector diffs = DiffUtil.diff(contents1, contents2);
 		next.addObject("diffs", diffs);
 		loadEdit(request, next, pageInfo, virtualWiki, topicName, false);
 		next.addObject("editResolve", "true");
 		pageInfo.setContentJsp(JSP_EDIT);
 	}
 
 	/**
 	 *
 	 */
 	private Integer retrieveLastTopicVersionId(HttpServletRequest request, Topic topic) throws Exception {
 		return (!StringUtils.isBlank(request.getParameter("lastTopicVersionId"))) ? new Integer(request.getParameter("lastTopicVersionId")) : topic.getCurrentVersionId();
 	}
 
 	/**
 	 *
 	 */
 	private void save(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
 		Topic topic = loadTopic(virtualWiki, topicName);
 		Topic lastTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (lastTopic != null && !lastTopic.getCurrentVersionId().equals(retrieveLastTopicVersionId(request, topic))) {
 			// someone else has edited the topic more recently
 			resolve(request, next, pageInfo);
 			return;
 		}
 		String contents = request.getParameter("contents");
 		String sectionName = "";
 		if (!StringUtils.isBlank(request.getParameter("section"))) {
 			// load section of topic
 			int section = (new Integer(request.getParameter("section"))).intValue();
 			ParserOutput parserOutput = new ParserOutput();
 			contents = ParserUtil.parseSplice(parserOutput, request.getContextPath(), request.getLocale(), virtualWiki, topicName, section, contents);
 			sectionName = parserOutput.getSectionName();
 		}
 		if (contents == null) {
 			logger.warning("The topic " + topicName + " has no content");
 			throw new WikiException(new WikiMessage("edit.exception.nocontent", topicName));
 		}
 		// strip line feeds
 		contents = StringUtils.remove(contents, '\r');
 		String lastTopicContent = (lastTopic != null) ? StringUtils.remove(lastTopic.getTopicContent(), '\r') : "";
		if (StringUtils.equals(lastTopicContent, contents)) {
 			// topic hasn't changed. redirect to prevent user from refreshing and re-submitting
 			ServletUtil.redirect(next, virtualWiki, topic.getName());
 			return;
 		}
 		if (handleSpam(request, next, pageInfo, topicName, contents)) {
 			return;
 		}
 		// parse for signatures and other syntax that should not be saved in raw form
 		WikiUserAuth user = ServletUtil.currentUser();
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext(request.getContextPath());
 		parserInput.setLocale(request.getLocale());
 		parserInput.setWikiUser(user);
 		parserInput.setTopicName(topicName);
 		parserInput.setUserIpAddress(ServletUtil.getIpAddress(request));
 		parserInput.setVirtualWiki(virtualWiki);
 		ParserOutput parserOutput = ParserUtil.parseMetadata(parserInput, contents);
 		// parse signatures and other values that need to be updated prior to saving
 		contents = ParserUtil.parseMinimal(parserInput, contents);
 		topic.setTopicContent(contents);
 		if (!StringUtils.isBlank(parserOutput.getRedirect())) {
 			// set up a redirect
 			topic.setRedirectTo(parserOutput.getRedirect());
 			topic.setTopicType(Topic.TYPE_REDIRECT);
 		} else if (topic.getTopicType() == Topic.TYPE_REDIRECT) {
 			// no longer a redirect
 			topic.setRedirectTo(null);
 			topic.setTopicType(Topic.TYPE_ARTICLE);
 		}
 		int charactersChanged = StringUtils.length(contents) - StringUtils.length(lastTopicContent);
 		TopicVersion topicVersion = new TopicVersion(user, ServletUtil.getIpAddress(request), request.getParameter("editComment"), contents, charactersChanged);
 		if (request.getParameter("minorEdit") != null) {
 			topicVersion.setEditType(TopicVersion.EDIT_MINOR);
 		}
 		WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks(), true, null);
 		// update watchlist
 		if (user.hasRole(Role.ROLE_USER)) {
 			Watchlist watchlist = ServletUtil.currentWatchlist(request, virtualWiki);
 			boolean watchTopic = (request.getParameter("watchTopic") != null);
 			if (watchlist.containsTopic(topicName) != watchTopic) {
 				WikiBase.getDataHandler().writeWatchlistEntry(watchlist, virtualWiki, topicName, user.getUserId(), null);
 			}
 		}
 		// redirect to prevent user from refreshing and re-submitting
 		String target = topic.getName();
 		if (!StringUtils.isBlank(sectionName)) {
 			target += "#" + sectionName;
 		}
 		ServletUtil.redirect(next, virtualWiki, target);
 	}
 }
