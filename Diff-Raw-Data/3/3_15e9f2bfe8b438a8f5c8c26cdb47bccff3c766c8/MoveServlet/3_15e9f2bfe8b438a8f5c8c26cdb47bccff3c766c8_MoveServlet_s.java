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
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.utils.Utilities;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class MoveServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(MoveServlet.class.getName());
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (!Environment.getBooleanValue(Environment.PROP_TOPIC_NON_ADMIN_TOPIC_MOVE) && !Utilities.isAdmin(request)) {
 			WikiMessage messageObject = new WikiMessage("admin.message.loginrequired");
 			return ServletUtil.viewLogin(request, pageInfo, Utilities.getTopicFromURI(request), messageObject);
 		}
 		if (request.getParameter("move") == null) {
 			view(request, next, pageInfo);
 		} else {
 			move(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void move(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = Utilities.getTopicFromRequest(request);
 		if (!StringUtils.hasText(topicName)) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		WikiMessage pageTitle = new WikiMessage("move.title", topicName);
 		pageInfo.setPageTitle(pageTitle);
 		pageInfo.setTopicName(topicName);
 		String moveDestination = request.getParameter("moveDestination");
 		if (!movePage(request, next, pageInfo, topicName, moveDestination)) {
 			return;
 		}
 		if (StringUtils.hasText(request.getParameter("moveCommentsPage"))) {
 			String moveCommentsPage = Utilities.decodeFromRequest(request.getParameter("moveCommentsPage"));
 			String commentsDestination = Utilities.extractCommentsLink(moveDestination);
 			if (Utilities.isCommentsPage(moveCommentsPage) && !moveCommentsPage.equals(topicName) && !commentsDestination.equals(moveDestination)) {
 				if (!movePage(request, next, pageInfo, moveCommentsPage, commentsDestination)) {
 					return;
 				}
 			}
 		}
		ServletUtil.viewTopic(request, next, pageInfo, moveDestination);
 	}
 
 	/**
 	 *
 	 */
 	private boolean movePage(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String moveFrom, String moveDestination) throws Exception {
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		Topic fromTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, moveFrom, false, null);
 		if (fromTopic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		if (!StringUtils.hasText(moveDestination)) {
 			pageInfo.setAction(WikiPageInfo.ACTION_MOVE);
 			next.addObject("messageObject", new WikiMessage("move.exception.nodestination"));
 			return false;
 		}
 		if (!ServletUtil.isMoveable(virtualWiki, moveFrom, Utilities.currentUser(request))) {
 			pageInfo.setAction(WikiPageInfo.ACTION_MOVE);
 			next.addObject("messageObject", new WikiMessage("move.exception.permission", moveFrom));
 			return false;
 		}
 		if (!WikiBase.getDataHandler().canMoveTopic(fromTopic, moveDestination)) {
 			pageInfo.setAction(WikiPageInfo.ACTION_MOVE);
 			next.addObject("messageObject", new WikiMessage("move.exception.destinationexists", moveDestination));
 			next.addObject("moveDestination", moveDestination);
 			next.addObject("moveComment", request.getParameter("moveComment"));
 			return false;
 		}
 		String moveComment = Utilities.formatMessage("move.editcomment", request.getLocale(), new String[]{moveFrom, moveDestination});
 		if (StringUtils.hasText(request.getParameter("moveComment"))) {
 			moveComment += " (" + request.getParameter("moveComment") + ")";
 		}
 		TopicVersion topicVersion = new TopicVersion(Utilities.currentUser(request), request.getRemoteAddr(), moveComment, fromTopic.getTopicContent());
 		topicVersion.setEditType(TopicVersion.EDIT_MOVE);
 		WikiBase.getDataHandler().moveTopic(fromTopic, topicVersion, moveDestination, null);
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = Utilities.getTopicFromRequest(request);
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		if (!StringUtils.hasText(topicName)) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		String commentsPage = Utilities.extractCommentsLink(topicName);
 		Topic commentsTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, commentsPage, false, null);
 		if (commentsTopic != null) {
 			// add option to also move comments page
 			next.addObject("moveCommentsPage", commentsPage);
 		}
 		WikiMessage pageTitle = new WikiMessage("move.title", topicName);
 		pageInfo.setPageTitle(pageTitle);
 		pageInfo.setAction(WikiPageInfo.ACTION_MOVE);
 		pageInfo.setTopicName(topicName);
 	}
 }
