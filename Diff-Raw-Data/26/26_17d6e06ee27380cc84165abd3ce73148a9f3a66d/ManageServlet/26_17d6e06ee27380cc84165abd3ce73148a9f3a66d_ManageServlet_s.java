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
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to provide capability for deleting, undeleting, and protecting topics.
  */
 public class ManageServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(ManageServlet.class.getName());
 	/** The name of the JSP file used to render the servlet output. */
 	protected static final String JSP_ADMIN_MANAGE = "admin-manage.jsp";
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (!StringUtils.isBlank(request.getParameter("delete"))) {
 			delete(request, next, pageInfo);
 		} else if (!StringUtils.isBlank(request.getParameter("undelete"))) {
 			undelete(request, next, pageInfo);
 		} else if (!StringUtils.isBlank(request.getParameter("permissions"))) {
 			permissions(request, next, pageInfo);
 		} else {
 			view(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void delete(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		if (topicName == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		deletePage(request, next, pageInfo, topicName);
		if (!StringUtils.isBlank(request.getParameter("manageCommentsPage"))) {
			String manageCommentsPage = Utilities.decodeTopicName(request.getParameter("manageCommentsPage"), true);
 			if (WikiUtil.isCommentsPage(manageCommentsPage) && !manageCommentsPage.equals(topicName)) {
 				deletePage(request, next, pageInfo, manageCommentsPage);
 			}
 		}
 		next.addObject("message", new WikiMessage("manage.message.updated", topicName));
 		view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void deletePage(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String topicName) throws Exception {
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, true, null);
 		if (topic.getDeleted()) {
 			logger.warning("Attempt to delete a topic that is already deleted: " + virtualWiki + " / " + topicName);
 			return;
 		}
 		int charactersChanged = 0 - StringUtils.length(topic.getTopicContent());
 		String contents = "";
 		topic.setTopicContent(contents);
 		WikiUser user = ServletUtil.currentWikiUser();
 		TopicVersion topicVersion = new TopicVersion(user, ServletUtil.getIpAddress(request), request.getParameter("deleteComment"), contents, charactersChanged);
 		topicVersion.setEditType(TopicVersion.EDIT_DELETE);
 		WikiBase.getDataHandler().deleteTopic(topic, topicVersion);
 	}
 
 	/**
 	 *
 	 */
 	private void permissions(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		if (topicName == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		topic.setReadOnly(request.getParameter("readOnly") != null);
 		topic.setAdminOnly(request.getParameter("adminOnly") != null);
 		WikiUser user = ServletUtil.currentWikiUser();
 		TopicVersion topicVersion = new TopicVersion(user, ServletUtil.getIpAddress(request), null, topic.getTopicContent(), 0);
 		topicVersion.setEditType(TopicVersion.EDIT_PERMISSION);
 		WikiBase.getDataHandler().writeTopic(topic, topicVersion, null, null);
 		next.addObject("message", new WikiMessage("manage.message.updated", topicName));
 		view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void undelete(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		if (topicName == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		undeletePage(request, next, pageInfo, topicName);
		if (!StringUtils.isBlank(request.getParameter("manageCommentsPage"))) {
			String manageCommentsPage = Utilities.decodeTopicName(request.getParameter("manageCommentsPage"), true);
 			if (WikiUtil.isCommentsPage(manageCommentsPage) && !manageCommentsPage.equals(topicName)) {
 				undeletePage(request, next, pageInfo, manageCommentsPage);
 			}
 		}
 		next.addObject("message", new WikiMessage("manage.message.updated", topicName));
 		view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void undeletePage(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String topicName) throws Exception {
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, true, null);
 		if (!topic.getDeleted()) {
 			logger.warning("Attempt to undelete a topic that is not deleted: " + virtualWiki + " / " + topicName);
 			return;
 		}
 		TopicVersion previousVersion = WikiBase.getDataHandler().lookupTopicVersion(topic.getCurrentVersionId());
 		while (previousVersion != null && previousVersion.getPreviousTopicVersionId() != null && previousVersion.getEditType() == TopicVersion.EDIT_DELETE) {
 			// loop back to find the last non-delete edit
 			previousVersion = WikiBase.getDataHandler().lookupTopicVersion(previousVersion.getPreviousTopicVersionId());
 		}
 		String contents = previousVersion.getVersionContent();
 		topic.setTopicContent(contents);
 		WikiUser user = ServletUtil.currentWikiUser();
 		int charactersChanged = StringUtils.length(contents);
 		TopicVersion topicVersion = new TopicVersion(user, ServletUtil.getIpAddress(request), request.getParameter("undeleteComment"), contents, charactersChanged);
 		topicVersion.setEditType(TopicVersion.EDIT_UNDELETE);
 		WikiBase.getDataHandler().undeleteTopic(topic, topicVersion);
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, true, null);
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		String commentsPage = WikiUtil.extractCommentsLink(topicName);
 		if (!topicName.equals(commentsPage)) {
 			Topic commentsTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, commentsPage, true, null);
 			if (commentsTopic != null && commentsTopic.getDeleted() == topic.getDeleted()) {
 				// add option to also move comments page
 				next.addObject("manageCommentsPage", commentsPage);
 			}
 		}
 		next.addObject("readOnly", topic.getReadOnly());
 		next.addObject("adminOnly", topic.getAdminOnly());
 		next.addObject("deleted", (topic.getDeleteDate() != null));
 		pageInfo.setTopicName(topicName);
 		pageInfo.setContentJsp(JSP_ADMIN_MANAGE);
 		pageInfo.setPageTitle(new WikiMessage("manage.title", topicName));
 	}
 }
