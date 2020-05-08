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
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiConfiguration;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.SearchResultEntry;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to display search results.
  *
  * @see org.jamwiki.SearchEngine
  */
 public class SearchServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(SearchServlet.class.getName());
 	/** The name of the JSP file used to render the servlet output when searching. */
 	protected static final String JSP_SEARCH = "search.jsp";
 	/** The name of the JSP file used to render the servlet output when displaying search results. */
 	protected static final String JSP_SEARCH_RESULTS = "search-results.jsp";
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (request.getParameter("jumpto") == null) {
 			search(request, next, pageInfo);
 		} else {
 			jumpTo(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void jumpTo(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		String topic = request.getParameter("text");
 		String targetTopic = LinkUtil.isExistingArticle(virtualWiki, topic);
 		if (targetTopic != null) {
 			ServletUtil.redirect(next, virtualWiki, targetTopic);
 		} else {
 			next.addObject("notopic", topic);
 			this.search(request, next, pageInfo);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void search(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		String searchField = request.getParameter("text");
 		if (request.getParameter("text") == null) {
 			pageInfo.setPageTitle(new WikiMessage("search.title"));
 		} else {
 			pageInfo.setPageTitle(new WikiMessage("searchresult.title", searchField));
 		}
		next.addObject("searchConfig", WikiConfiguration.getCurrentSearchConfiguration());
 		// forward back to the search page if the request is blank or null
 		if (StringUtils.isBlank(searchField)) {
 			pageInfo.setContentJsp(JSP_SEARCH);
 			pageInfo.setSpecial(true);
 			return;
 		}
 		// grab search engine instance and find
 		List<SearchResultEntry> results = WikiBase.getSearchEngine().findResults(virtualWiki, searchField);
 		next.addObject("searchField", searchField);
 		next.addObject("results", results);
 		pageInfo.setContentJsp(JSP_SEARCH_RESULTS);
 		pageInfo.setSpecial(true);
 	}
 }
