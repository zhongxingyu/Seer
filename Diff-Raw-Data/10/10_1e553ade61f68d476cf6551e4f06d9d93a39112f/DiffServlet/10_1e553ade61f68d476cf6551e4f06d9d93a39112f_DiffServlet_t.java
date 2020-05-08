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
 
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.WikiDiff;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.DiffUtil;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to display a diff between two versions of a topic.
  */
 public class DiffServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(DiffServlet.class.getName());
 	/** The name of the JSP file used to render the servlet output. */
 	protected static final String JSP_DIFF = "diff.jsp";
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		this.diff(request, next, pageInfo);
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void diff(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		String topicName = WikiUtil.getTopicFromRequest(request);
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic", topicName));
 		}
 		int topicVersionId1 = 0;
 		if (!StringUtils.isBlank(request.getParameter("version1"))) {
 			topicVersionId1 = Integer.valueOf(request.getParameter("version1"));
 		}
 		int topicVersionId2 = 0;
 		if (!StringUtils.isBlank(request.getParameter("version2"))) {
 			topicVersionId2 = Integer.valueOf(request.getParameter("version2"));
 		}
		if (topicVersionId1 == 0 && topicVersionId2 == 0) {
			// default to the current version in case no version is present in the request,
			// which can happen when clicking the "diff" button from a history page for a topic
			// with only one version.
			topicVersionId1 = topic.getCurrentVersionId();
		}
		TopicVersion version1 = (topicVersionId1 != 0) ? WikiBase.getDataHandler().lookupTopicVersion(topicVersionId1) : null;
		TopicVersion version2 = (topicVersionId2 != 0) ? WikiBase.getDataHandler().lookupTopicVersion(topicVersionId2) : null;
 		if (version1 == null && version2 == null) {
 			String msg = "Versions " + topicVersionId1 + " and " + topicVersionId2 + " not found for " + topicName;
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 		String contents1 = (version1 != null) ? version1.getVersionContent() : null;
 		String contents2 = (version2 != null) ? version2.getVersionContent() : null;
 		if (contents1 == null && contents2 == null) {
 			String msg = "No versions found for " + topicVersionId1 + " against " + topicVersionId2;
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 		List<WikiDiff> diffs = DiffUtil.diff(contents1, contents2);
 		next.addObject("diffs", diffs);
 		if (version1 != null) {
 			WikiUser user = (version1.getAuthorId() != null) ? WikiBase.getDataHandler().lookupWikiUser(version1.getAuthorId()) : null;
 			String author1 = ((user != null) ? user.getUsername() : version1.getAuthorDisplay());
 			next.addObject("version1", RecentChange.initRecentChange(topic, version1, author1));
 		}
 		if (version2 != null) {
 			WikiUser user = (version2.getAuthorId() != null) ? WikiBase.getDataHandler().lookupWikiUser(version2.getAuthorId()) : null;
 			String author2 = ((user != null) ? user.getUsername() : version2.getAuthorDisplay());
 			next.addObject("version2", RecentChange.initRecentChange(topic, version2, author2));
 		}
 		Integer nextTopicVersionId = (version1 != null) ? WikiBase.getDataHandler().lookupTopicVersionNextId(version1.getTopicVersionId()) : null;
 		next.addObject("nextTopicVersionId", nextTopicVersionId);
 		pageInfo.setPageTitle(new WikiMessage("diff.title", topicName));
 		pageInfo.setTopicName(topicName);
 		pageInfo.setContentJsp(JSP_DIFF);
 	}
 }
