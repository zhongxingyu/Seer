 package org.jmwiki.servlets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.ResourceBundle;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.jmwiki.Environment;
 import org.jmwiki.PrintableEntry;
 import org.jmwiki.WikiBase;
 import org.jmwiki.PseudoTopicHandler;
 import org.jmwiki.utils.Utilities;
 import org.jmwiki.utils.JSPUtils;
 
 /**
  * Create a printable view of one servlet.
  *
  * @author garethc, Tobias Schulz-Hess (sourcefoge@schulz-hess.de)
  * Date: Jan 8, 2003
  */
 public class PrintableServlet extends JMWikiServlet {
 
 	/**
 	 * Handle get request
 	 *
 	 * @param request The servlet request
 	 * @param response The servlet response
 	 *
 	 * @throws ServletException If something went wrong with the servlet
 	 * @throws IOException If the servlet cannot print
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response)
 		throws ServletException, IOException {
 		String topic = request.getParameter("topic");
 		String virtualWiki = (String) request.getAttribute("virtualWiki");
 		request.setAttribute("topic", topic);
 		request.setAttribute("title", topic);
 		String strDepth = request.getParameter("depth");
 		if (request.getParameter("hideform") != null) {
 			request.setAttribute("hideform", "true");
 		}
 		int depth = 0;
 		try {
 			depth = Integer.parseInt(strDepth);
 		} catch (NumberFormatException e1) {
 			depth = 0;
 		}
 		request.setAttribute("depth", String.valueOf(depth));
 		String contextPath = request.getContextPath();
 		Environment.setValue(Environment.PROP_TOPIC_BASE_CONTEXT, contextPath);
 		ArrayList result = new ArrayList();
 		Vector alreadyVisited = new Vector();
 		try {
 			result.addAll(parsePage(ResourceBundle.getBundle("ApplicationResources", request.getLocale()), virtualWiki, topic, depth, alreadyVisited));
 		} catch (Exception e) {
 			error(request, response, e);
 			return;
 		}
 		// now go through all pages and replace
 		// all href=Wiki? with href=# for the
 		// pages in the alreadyVisited vector
 		for (Iterator iter = result.iterator(); iter.hasNext();) {
 			PrintableEntry element = (PrintableEntry) iter.next();
 			for (Iterator visitedIterator = alreadyVisited.iterator(); visitedIterator.hasNext();) {
 				String visitedTopic = (String) visitedIterator.next();
 				element.setContent(Utilities.replaceString(element.getContent(),
 					"href=\"Wiki?" + visitedTopic, "href=\"#" + visitedTopic)
 				);
 			}
 		}
 		// put the result in the request
 		request.setAttribute("contentList", result);
 		request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_PRINT);
		dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
 	}
 
 	/**
 	 * Parse page and supages
 	 * @param virtualWiki The virutal wiki to use
 	 * @param topic The topic to start with
 	 * @param depth The depth to go into
 	 * @return Collection of pages
 	 */
 	private Collection parsePage(ResourceBundle messages, String virtualWiki, String topic, int depth, Vector alreadyVisited)
 		throws Exception {
 		WikiBase base = WikiBase.getInstance();
 		String onepage = base.readCooked(virtualWiki, topic);
 		Collection result = new ArrayList();
 		if (onepage != null) {
 			PrintableEntry entry = new PrintableEntry();
 			entry.setTopic(topic);
 			entry.setContent(onepage);
 			result.add(entry);
 			alreadyVisited.add(topic);
 			if (depth > 0) {
 				String searchfor = "href=\"Wiki?";
 				int iPos = onepage.indexOf(searchfor);
 				int iEndPos = onepage.indexOf(messages.getString("topic.ismentionedon"));
 				if (iEndPos == -1) iEndPos = Integer.MAX_VALUE;
 				while (iPos > -1 && iPos < iEndPos) {
 					String link = onepage.substring(iPos + searchfor.length(),
 					onepage.indexOf('"', iPos + searchfor.length()));
 					if (link.indexOf('&') > -1) {
 						link = link.substring(0, link.indexOf('&'));
 					}
 					link = JSPUtils.decodeURL(link);
 					if (link.length() > 3 &&
 						!link.startsWith("topic=") &&
 						!link.startsWith("action=") &&
 						!alreadyVisited.contains(link) &&
 						!PseudoTopicHandler.getInstance().isPseudoTopic(link)) {
 						result.addAll(parsePage(messages, virtualWiki, link, (depth - 1), alreadyVisited));
 					}
 					iPos = onepage.indexOf(searchfor, iPos + 10);
 				}
 			}
 		}
 		return result;
 	}
 }
