 package org.jmwiki.servlets;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.Iterator;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.jmwiki.SearchEngine;
 import org.jmwiki.SearchResultEntry;
 import org.jmwiki.WikiBase;
 import org.jmwiki.utils.JSPUtils;
 import org.jmwiki.utils.Utilities;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 /**
  *
  */
 public class SearchServlet extends JMController implements Controller {
 
 	private static final Logger logger = Logger.getLogger(SearchServlet.class);
 
 	/**
 	 *
 	 */
 	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		ModelAndView next = new ModelAndView("wiki");
 		JMController.buildLayout(request, next);
 		search(request, response, next);
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void search(HttpServletRequest request, HttpServletResponse response, ModelAndView next) throws Exception {
 		String virtualWiki = JMController.getVirtualWikiFromURI(request);
 		MessageFormat formatter = new MessageFormat("");
 		formatter.setLocale(request.getLocale());
 		try {
 			String searchField = request.getParameter("text");
			if (request.getParameter("text") == null) {
				next.addObject("title", "Special:Search");
			} else {
				formatter.applyPattern(JMController.getMessage("searchresult.title", request.getLocale()));
				next.addObject("title", formatter.format(new Object[]{searchField}));
			}
 			// forward back to the search page if the request is blank or null
 			if (searchField == null || searchField.length() == 0) {
 				next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_SEARCH);
 				next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 				return;
 			}
 			// grab search engine instance and find
 			boolean fuzzy = false;
 			if (request.getParameter("fuzzy") != null) fuzzy = true;
 			SearchEngine sedb = WikiBase.getInstance().getSearchEngineInstance();
 			Collection results = sedb.findMultiple(virtualWiki, searchField, fuzzy);
 			StringBuffer contents = new StringBuffer();
 			if (results != null && results.size() > 0) {
 				Iterator it = results.iterator();
 				while (it.hasNext()) {
 					SearchResultEntry result = (SearchResultEntry) it.next();
 					contents.append("<p>");
 					contents.append("<div class=\"searchresult\">");
 					contents.append("<a href=\"");
 					contents.append(
 						Utilities.buildInternalLink(request.getContextPath(), virtualWiki, result.getTopic())
 					);
 					if (result.getFoundWord().length() > 0) {
 						contents.append("?highlight=");
 						contents.append(JSPUtils.encodeURL(result.getFoundWord()));
 					}
 					contents.append("\">" + result.getTopic() + "</a>");
 					contents.append("</div>");
 					if (result.getTextBefore().length() > 0 || result.getTextAfter().length() > 0
 						|| result.getFoundWord().length() > 0) {
 						contents.append("<br>");
 						contents.append(result.getTextBefore());
 						contents.append("<a style=\"background:yellow\" href=\"");
 						contents.append(
 							Utilities.buildInternalLink(request.getContextPath(), virtualWiki, result.getTopic())
 						);
 						contents.append("?highlight=");
 						contents.append(JSPUtils.encodeURL(result.getFoundWord()));
 						contents.append("\">");
 						contents.append(result.getFoundWord());
 						contents.append("</a> ");
 						contents.append(result.getTextAfter());
 					}
 					contents.append("</p>");
 				}
 			} else {
				next.addObject("text", searchField);
 				contents.append("<p>");
 				formatter = new MessageFormat("");
 				formatter.setLocale(request.getLocale());
 				formatter.applyPattern(JMController.getMessage("searchresult.notfound", request.getLocale()));
 				contents.append(formatter.format(new Object[]{searchField}));
 				contents.append("</p>");
 			}
 			next.addObject("results", contents.toString());
 			next.addObject("titlelink", "Special:Search");
 			next.addObject(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_SEARCH_RESULTS);
 			next.addObject(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 			return;
 		} catch (Exception err) {
 			logger.error(err);
 			throw new WikiServletException(err.toString());
 		}
 	}
 }
