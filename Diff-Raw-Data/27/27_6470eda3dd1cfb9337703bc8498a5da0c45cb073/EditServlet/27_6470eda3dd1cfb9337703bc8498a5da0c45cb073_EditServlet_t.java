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
 
 import java.util.Collection;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.PseudoTopicHandler;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserInfo;
 import org.jamwiki.search.SearchEngine;
 import org.jamwiki.utils.DiffUtil;
 import org.jamwiki.utils.Utilities;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class EditServlet extends JAMWikiServlet {
 
 	private static Logger logger = Logger.getLogger(EditServlet.class);
 
 	/**
 	 *
 	 */
 	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
 		ModelAndView next = new ModelAndView("wiki");
 		try {
 			if (isSave(request)) {
 				save(request, next);
 			} else {
 				edit(request, next);
 			}
 		} catch (Exception e) {
 			viewError(request, next, e);
 		}
 		loadDefaults(request, next, this.pageInfo);
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void edit(HttpServletRequest request, ModelAndView next) throws Exception {
 		String topicName = JAMWikiServlet.getTopicFromRequest(request);
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		if (loginRequired(request, next, virtualWiki, topicName)) {
 			return;
 		}
 		loadTopic(request, virtualWiki, topicName);
 		int lastTopicVersionId = retrieveLastTopicVersionId(request, virtualWiki, topicName);
 		next.addObject("lastTopicVersionId", new Integer(lastTopicVersionId));
 		loadEdit(request, next, topicName);
 		String contents = null;
 		if (isPreview(request)) {
 			preview(request, next);
 			return;
 		}
 		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_EDIT);
 		if (StringUtils.hasText(request.getParameter("topicVersionId"))) {
 			// editing an older version
 			int topicVersionId = Integer.parseInt(request.getParameter("topicVersionId"));
 			TopicVersion topicVersion = WikiBase.getHandler().lookupTopicVersion(virtualWiki, topicName, topicVersionId);
 			if (topicVersion == null) {
 				throw new Exception(Utilities.getMessage("edit.exception.notopic", request.getLocale()));
 			}
 			contents = topicVersion.getVersionContent();
 			if (lastTopicVersionId != topicVersionId) {
 				next.addObject("topicVersionId", new Integer(topicVersionId));
 			}
 		} else if (StringUtils.hasText(request.getParameter("section"))) {
 			// editing a section of a topic
 			int section = (new Integer(request.getParameter("section"))).intValue();
 			contents = Utilities.parseSlice(request, virtualWiki, topicName, section);
 		} else {
 			// editing a full new or existing topic
 			Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 			contents = (topic == null) ? "" : topic.getTopicContent();
 		}
 		next.addObject("contents", contents);
 	}
 
 	/**
 	 *
 	 */
 	private boolean isPreview(HttpServletRequest request) {
		return StringUtils.hasText(request.getParameter("preview"));
 	}
 
 	/**
 	 *
 	 */
 	private boolean isSave(HttpServletRequest request) {
		return StringUtils.hasText(request.getParameter("save"));
 	}
 
 	/**
 	 *
 	 */
 	private void loadEdit(HttpServletRequest request, ModelAndView next, String topicName) throws Exception {
 		String pageTitle = Utilities.getMessage("edit.title", request.getLocale(), topicName);
 		this.pageInfo.setPageTitle(pageTitle);
 		this.pageInfo.setTopicName(topicName);
 		if (request.getParameter("editComment") != null) {
 			next.addObject("editComment", request.getParameter("editComment"));
 		}
 		if (request.getParameter("section") != null) {
 			next.addObject("section", request.getParameter("section"));
 		}
 		next.addObject("minorEdit", new Boolean(request.getParameter("minorEdit") != null));
 	}
 
 	/**
 	 *
 	 */
 	private Topic loadTopic(HttpServletRequest request, String virtualWiki, String topicName) throws Exception {
 		if (!StringUtils.hasText(topicName)) {
 			throw new Exception(Utilities.getMessage("edit.exception.notopic", request.getLocale()));
 		}
 		if (PseudoTopicHandler.isPseudoTopic(topicName)) {
 			throw new Exception(Utilities.getMessage("edit.exception.pseudotopic", request.getLocale(), topicName));
 		}
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		if (topic == null) {
 			topic = new Topic();
 			topic.setName(topicName);
 			topic.setVirtualWiki(virtualWiki);
 		}
 		if (topic.getReadOnly()) {
 			throw new Exception(Utilities.getMessage("error.readonly", request.getLocale()));
 		}
 		return topic;
 	}
 
 	/**
 	 *
 	 */
 	private boolean loginRequired(HttpServletRequest request, ModelAndView next, String virtualWiki, String topicName) throws Exception {
 		if (Environment.getBooleanValue(Environment.PROP_TOPIC_FORCE_USERNAME) && Utilities.currentUser(request) == null) {
 			next.addObject("errorMessage", Utilities.getMessage("edit.exception.login", request.getLocale()));
 			viewLogin(request, next, JAMWikiServlet.getTopicFromURI(request));
 			return true;
 		}
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		if (topic != null && topic.getAdminOnly() && !Utilities.isAdmin(request)) {
 			next.addObject("errorMessage", Utilities.getMessage("edit.exception.loginadmin", request.getLocale(), topicName));
 			viewLogin(request, next, JAMWikiServlet.getTopicFromURI(request));
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	private void preview(HttpServletRequest request, ModelAndView next) throws Exception {
 		String topicName = JAMWikiServlet.getTopicFromRequest(request);
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		WikiUser user = Utilities.currentUser(request);
 		JAMWikiServlet.removeCachedContents();
 		String contents = (String)request.getParameter("contents");
 		ParserInfo parserInfo = new ParserInfo(request.getContextPath(), request.getLocale());
 		parserInfo.setWikiUser(user);
 		parserInfo.setTopicName(topicName);
 		parserInfo.setUserIpAddress(request.getRemoteAddr());
 		parserInfo.setVirtualWiki(virtualWiki);
 		parserInfo.setMode(ParserInfo.MODE_PREVIEW);
 		String preview = Utilities.parse(parserInfo, contents, topicName);
 		Topic previewTopic = new Topic();
 		previewTopic.setName(topicName);
 		previewTopic.setTopicContent(preview);
 		next.addObject(JAMWikiServlet.PARAMETER_TOPIC_OBJECT, previewTopic);
 		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_PREVIEW);
 		next.addObject("contents", contents);
 	}
 
 	/**
 	 *
 	 */
 	private void resolve(HttpServletRequest request, ModelAndView next) throws Exception {
 		String topicName = JAMWikiServlet.getTopicFromRequest(request);
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		TopicVersion version = WikiBase.getHandler().lookupLastTopicVersion(virtualWiki, topicName);
 		String contents1 = version.getVersionContent();
 		String contents2 = request.getParameter("contents");
 		next.addObject("lastTopicVersionId", new Integer(version.getTopicVersionId()));
 		next.addObject("contents", contents1);
 		next.addObject("contentsResolve", contents2);
 		String diff = DiffUtil.diff(contents1, contents2, true, request.getLocale());
 		next.addObject("diff", diff);
 		loadEdit(request, next, topicName);
 		this.pageInfo.setPageAction(JAMWikiServlet.ACTION_EDIT_RESOLVE);
 	}
 
 	/**
 	 *
 	 */
 	private int retrieveLastTopicVersionId(HttpServletRequest request, String virtualWiki, String topicName) throws Exception {
 		int lastTopicVersionId = 0;
 		if (request.getParameter("lastTopicVersionId") == null) {
 			TopicVersion version = WikiBase.getHandler().lookupLastTopicVersion(virtualWiki, topicName);
 			if (version != null) lastTopicVersionId = version.getTopicVersionId();
 		} else {
 			lastTopicVersionId = new Integer(request.getParameter("lastTopicVersionId")).intValue();
 		}
 		return lastTopicVersionId;
 	}
 
 	/**
 	 *
 	 */
 	private void save(HttpServletRequest request, ModelAndView next) throws Exception {
 		String topicName = request.getParameter(JAMWikiServlet.PARAMETER_TOPIC);
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		if (loginRequired(request, next, virtualWiki, topicName)) {
 			return;
 		}
 		Topic topic = loadTopic(request, virtualWiki, topicName);
 		TopicVersion lastTopicVersion = WikiBase.getHandler().lookupLastTopicVersion(virtualWiki, topicName);
 		if (lastTopicVersion != null && lastTopicVersion.getTopicVersionId() != retrieveLastTopicVersionId(request, virtualWiki, topicName)) {
 			// someone else has edited the topic more recently
 			resolve(request, next);
 			return;
 		}
 		TopicVersion topicVersion = new TopicVersion();
 		String contents = request.getParameter("contents");
 		if (StringUtils.hasText(request.getParameter("section"))) {
 			// load section of topic
 			int section = (new Integer(request.getParameter("section"))).intValue();
 			contents = Utilities.parseSplice(request, virtualWiki, topicName, section, contents);
 		}
 		if (contents == null) {
 			logger.warn("The topic " + topicName + " has no content");
 			throw new Exception(Utilities.getMessage("edit.exception.nocontent", request.getLocale(), topicName));
 		}
 		if (lastTopicVersion != null && lastTopicVersion.getVersionContent().equals(contents)) {
 			viewTopic(request, next, topicName);
 			return;
 		}
 		// parse for signatures and other syntax that should not be saved in raw form
 		WikiUser user = Utilities.currentUser(request);
 		ParserInfo parserInfo = new ParserInfo(request.getContextPath(), request.getLocale());
 		parserInfo.setWikiUser(user);
 		parserInfo.setTopicName(topicName);
 		parserInfo.setUserIpAddress(request.getRemoteAddr());
 		parserInfo.setVirtualWiki(virtualWiki);
 		parserInfo.setMode(ParserInfo.MODE_SAVE);
 		contents = Utilities.parsePreSave(parserInfo, contents);
 		topic.setTopicContent(contents);
 		topicVersion.setVersionContent(contents);
 		topicVersion.setEditComment(request.getParameter("editComment"));
 		topicVersion.setAuthorIpAddress(request.getRemoteAddr());
 		if (request.getParameter("minorEdit") != null) {
 			topicVersion.setEditType(TopicVersion.EDIT_MINOR);
 		}
 		if (user != null) {
 			topicVersion.setAuthorId(new Integer(user.getUserId()));
 		}
 		WikiBase.getHandler().writeTopic(topic, topicVersion);
 		// a save request has been made
 		JAMWikiServlet.removeCachedContents();
 		SearchEngine sedb = WikiBase.getSearchEngineInstance();
 		sedb.indexText(virtualWiki, topicName, request.getParameter("contents"));
 		viewTopic(request, next, topicName);
 	}
 }
