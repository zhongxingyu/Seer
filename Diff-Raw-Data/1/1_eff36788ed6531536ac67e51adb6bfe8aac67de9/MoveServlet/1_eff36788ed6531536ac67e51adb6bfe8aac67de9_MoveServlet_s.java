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
 import org.jamwiki.authentication.RoleImpl;
 import org.jamwiki.authentication.WikiUserDetails;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.NamespaceHandler;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to handle moving a topic to a new name.
  */
 public class MoveServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(MoveServlet.class.getName());
 	/** The name of the JSP file used to render the servlet output. */
 	protected static final String JSP_MOVE = "move.jsp";
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		WikiUserDetails userDetails = ServletUtil.currentUserDetails();
 		if (!userDetails.hasRole(RoleImpl.ROLE_MOVE)) {
 			WikiMessage messageObject = new WikiMessage("login.message.move");
 			return ServletUtil.viewLogin(request, pageInfo, WikiUtil.getTopicFromURI(request), messageObject);
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
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		if (StringUtils.isBlank(topicName)) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		WikiMessage pageTitle = new WikiMessage("move.title", topicName);
 		pageInfo.setPageTitle(pageTitle);
 		pageInfo.setTopicName(topicName);
 		String moveDestination = Utilities.decodeAndEscapeTopicName(request.getParameter("moveDestination"), true);
 		if (!movePage(request, next, pageInfo, topicName, moveDestination)) {
 			return;
 		}
 		String moveCommentsPage = Utilities.decodeAndEscapeTopicName(request.getParameter("moveCommentsPage"), true);
 		if (!StringUtils.isBlank(moveCommentsPage)) {
 			String commentsDestination = WikiUtil.extractCommentsLink(moveDestination);
 			if (WikiUtil.isCommentsPage(moveCommentsPage) && !moveCommentsPage.equals(topicName) && !commentsDestination.equals(moveDestination)) {
 				if (!movePage(request, next, pageInfo, moveCommentsPage, commentsDestination)) {
 					return;
 				}
 			}
 		}
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		ServletUtil.redirect(next, virtualWiki, moveDestination);
 	}
 
 	/**
 	 *
 	 */
 	private boolean movePage(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String moveFrom, String moveDestination) throws Exception {
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		Topic fromTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, moveFrom, false, null);
 		if (fromTopic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		if (StringUtils.isBlank(moveDestination)) {
 			next.addObject("messageObject", new WikiMessage("move.exception.nodestination"));
 			this.view(request, next, pageInfo);
 			return false;
 		}
 		WikiLink fromWikiLink = LinkUtil.parseWikiLink(moveFrom);
 		WikiLink destinationWikiLink = LinkUtil.parseWikiLink(moveDestination);
 		if (!StringUtils.equals(fromWikiLink.getNamespace(), destinationWikiLink.getNamespace())) {
 			// do not allow moving into or out of image & category namespace
 			if (StringUtils.equals(fromWikiLink.getNamespace(), NamespaceHandler.NAMESPACE_CATEGORY)
 					|| StringUtils.equals(fromWikiLink.getNamespace(), NamespaceHandler.NAMESPACE_CATEGORY_COMMENTS)
 					|| StringUtils.equals(destinationWikiLink.getNamespace(), NamespaceHandler.NAMESPACE_CATEGORY)
 					|| StringUtils.equals(destinationWikiLink.getNamespace(), NamespaceHandler.NAMESPACE_CATEGORY_COMMENTS)
 				) {
 				next.addObject("messageObject", new WikiMessage("move.exception.namespacecategory"));
 				this.view(request, next, pageInfo);
 				return false;
 			} else if (StringUtils.equals(fromWikiLink.getNamespace(), NamespaceHandler.NAMESPACE_IMAGE)
 					|| StringUtils.equals(fromWikiLink.getNamespace(), NamespaceHandler.NAMESPACE_IMAGE_COMMENTS)
 					|| StringUtils.equals(destinationWikiLink.getNamespace(), NamespaceHandler.NAMESPACE_IMAGE)
 					|| StringUtils.equals(destinationWikiLink.getNamespace(), NamespaceHandler.NAMESPACE_IMAGE_COMMENTS)
 				) {
 				next.addObject("messageObject", new WikiMessage("move.exception.namespaceimage"));
 				this.view(request, next, pageInfo);
 				return false;
 			}
 		}
 		WikiUserDetails userDetails = ServletUtil.currentUserDetails();
 		if (!ServletUtil.isMoveable(virtualWiki, moveFrom, userDetails)) {
 			this.view(request, next, pageInfo);
 			next.addObject("messageObject", new WikiMessage("move.exception.permission", moveFrom));
 			return false;
 		}
 		if (!WikiBase.getDataHandler().canMoveTopic(fromTopic, moveDestination)) {
 			this.view(request, next, pageInfo);
 			next.addObject("messageObject", new WikiMessage("move.exception.destinationexists", moveDestination));
 			return false;
 		}
 		String moveComment = request.getParameter("moveComment");
 		WikiUser user = ServletUtil.currentWikiUser();
 		TopicVersion topicVersion = new TopicVersion(user, ServletUtil.getIpAddress(request), moveComment, fromTopic.getTopicContent(), 0);
 		topicVersion.setEditType(TopicVersion.EDIT_MOVE);
 		WikiBase.getDataHandler().moveTopic(fromTopic, topicVersion, moveDestination);
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		if (StringUtils.isBlank(topicName)) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		String commentsPage = WikiUtil.extractCommentsLink(topicName);
 		Topic commentsTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, commentsPage, false, null);
 		if (commentsTopic != null) {
 			// add option to also move comments page
 			next.addObject("moveCommentsPage", commentsPage);
 		}
 		WikiMessage pageTitle = new WikiMessage("move.title", topicName);
 		pageInfo.setPageTitle(pageTitle);
 		pageInfo.setContentJsp(JSP_MOVE);
 		pageInfo.setTopicName(topicName);
 		String moveDestination = Utilities.decodeAndEscapeTopicName(request.getParameter("moveDestination"), true);
 		if (StringUtils.isBlank(moveDestination)) {
 			moveDestination = topicName;
 		}
 		next.addObject("moveDestination", moveDestination);
 		next.addObject("moveComment", request.getParameter("moveComment"));
 	}
 }
