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
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class AdminVirtualWikiServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(AdminVirtualWikiServlet.class.getName());
 	/** The name of the JSP file used to render the servlet output when searching. */
 	protected static final String JSP_ADMIN_VIRTUAL_WIKI = "admin-virtual-wiki.jsp";
 
 	/**
 	 * This method handles the request after its parent class receives control.
 	 *
 	 * @param request - Standard HttpServletRequest object.
 	 * @param response - Standard HttpServletResponse object.
 	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String function = request.getParameter("function");
 		next.addObject("function", function);
 		if (StringUtils.isBlank(function)) {
 			view(request, next, pageInfo);
 		} else if (function.equals("addnamespace")) {
 			addNamespace(request, next, pageInfo);
 		} else if (function.equals("namespaces")) {
 			namespaces(request, next, pageInfo);
 		} else if (function.equals("search")) {
 			search(request, next, pageInfo);
 		} else if (function.equals("virtualwiki")) {
 			virtualWiki(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void addNamespace(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		List<WikiMessage> errors = new ArrayList<WikiMessage>();
 		String mainNamespace = request.getParameter("mainNamespace");
 		String commentsNamespace = request.getParameter("commentsNamespace");
 		// validate that the namespace values are acceptable
 		try {
 			WikiUtil.validateNamespaceName(mainNamespace);
 			if (mainNamespace.equals(commentsNamespace)) {
 				throw new WikiException(new WikiMessage("admin.vwiki.error.namespace.unique", mainNamespace));
 			}
 			WikiUtil.validateNamespaceName(commentsNamespace);
 			// write namespaces to the database
 			Namespace mainNamespaceObj = new Namespace(null, mainNamespace);
 			Namespace commentsNamespaceObj = null;
 			if (!StringUtils.isBlank(commentsNamespace)) {
 				commentsNamespaceObj = new Namespace(null, commentsNamespace);
 				commentsNamespaceObj.setMainNamespace(mainNamespaceObj);
 			}
 			WikiBase.getDataHandler().writeNamespace(mainNamespaceObj, commentsNamespaceObj);
 		} catch (WikiException e) {
 			errors.add(e.getWikiMessage());
 		} catch (DataAccessException e) {
 			logger.severe("Failure while retrieving virtual wiki", e);
 			errors.add(new WikiMessage("error.unknown", e.getMessage()));
 		}
 		if (!errors.isEmpty()) {
 			next.addObject("errors", errors);
 			next.addObject("mainNamespace", mainNamespace);
 			next.addObject("commentsNamespace", commentsNamespace);
 		} else {
 			next.addObject("message", new WikiMessage("admin.vwiki.message.addnamespacesuccess", mainNamespace));
 		}
 		this.view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void namespaces(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String virtualWiki = request.getParameter("selected");
 		List<Namespace> namespaces = new ArrayList<Namespace>();
 		String[] namespaceIds = request.getParameterValues("namespace_id");
 		String defaultLabel;
 		String updatedLabel;
 		String translatedLabel;
 		try {
 			for (String namespaceId : namespaceIds) {
 				defaultLabel = request.getParameter(namespaceId + "_label");
 				Namespace namespace = WikiBase.getDataHandler().lookupNamespace(null, defaultLabel);
 				updatedLabel = request.getParameter(namespaceId + "_newlabel");
 				translatedLabel = request.getParameter(namespaceId + "_vwiki");
 				if (StringUtils.equals(defaultLabel, translatedLabel) || StringUtils.isBlank(translatedLabel)) {
 					namespace.getNamespaceTranslations().remove(virtualWiki);
 				} else {
 					namespace.getNamespaceTranslations().put(virtualWiki, translatedLabel);
 				}
 				namespaces.add(namespace);
 			}
 			WikiBase.getDataHandler().writeNamespaceTranslations(namespaces, virtualWiki);
 			next.addObject("message", new WikiMessage("admin.vwiki.message.namespacesuccess", virtualWiki));
 		} catch (DataAccessException e) {
 			logger.severe("Failure while retrieving adding/updating namespace translations", e);
 			List<WikiMessage> errors = new ArrayList<WikiMessage>();
 			errors.add(new WikiMessage("admin.vwiki.error.addnamespacefail", e.getMessage()));
 			next.addObject("errors", errors);
 		}
 		this.view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void search(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		this.view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		List<WikiMessage> errors = (next.getModel().get("errors") != null) ? (List<WikiMessage>)next.getModel().get("errors") : new ArrayList<WikiMessage>();
 		// find the current virtual wiki
 		String selected = request.getParameter("selected");
 		if (!StringUtils.isBlank(selected)) {
 			VirtualWiki virtualWiki = null;
 			try {
 				virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(selected);
 			} catch (DataAccessException e) {
 				logger.severe("Failure while retrieving virtual wiki", e);
 				errors.add(new WikiMessage("error.unknown", e.getMessage()));
 			}
 			if (virtualWiki != null) {
 				next.addObject("selected", virtualWiki);
 			}
 		}
 		// initialize page defaults
 		pageInfo.setAdmin(true);
		try {
			List<VirtualWiki> virtualWikiList = WikiBase.getDataHandler().getVirtualWikiList();
			next.addObject("wikis", virtualWikiList);
			List<Namespace> namespaces = WikiBase.getDataHandler().lookupNamespaces();
			next.addObject("namespaces", namespaces);
		} catch (DataAccessException e) {
			logger.severe("Failure while retrieving database records", e);
			errors.add(new WikiMessage("error.unknown", e.getMessage()));
		}
 		pageInfo.setContentJsp(JSP_ADMIN_VIRTUAL_WIKI);
 		pageInfo.setPageTitle(new WikiMessage("admin.vwiki.title"));
		next.addObject("errors", errors);
 	}
 
 	/**
 	 *
 	 */
 	private void virtualWiki(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		WikiUser user = ServletUtil.currentWikiUser();
 		try {
 			VirtualWiki virtualWiki = new VirtualWiki();
 			if (!StringUtils.isBlank(request.getParameter("virtualWikiId"))) {
 				virtualWiki.setVirtualWikiId(Integer.valueOf(request.getParameter("virtualWikiId")));
 			}
 			virtualWiki.setName(request.getParameter("name"));
 			String defaultTopicName = WikiUtil.getParameterFromRequest(request, "defaultTopicName", true);
 			virtualWiki.setDefaultTopicName(defaultTopicName);
 			WikiBase.getDataHandler().writeVirtualWiki(virtualWiki);
 			if (StringUtils.isBlank(request.getParameter("virtualWikiId"))) {
 				WikiBase.getDataHandler().setupSpecialPages(request.getLocale(), user, virtualWiki);
 			}
 			next.addObject("selected", virtualWiki);
 			next.addObject("message", new WikiMessage("admin.message.virtualwikiadded"));
 		} catch (Exception e) {
 			logger.severe("Failure while adding virtual wiki", e);
 			List<WikiMessage> errors = new ArrayList<WikiMessage>();
 			errors.add(new WikiMessage("admin.message.virtualwikifail", e.getMessage()));
 			next.addObject("errors", errors);
 		}
 		view(request, next, pageInfo);
 	}
 }
