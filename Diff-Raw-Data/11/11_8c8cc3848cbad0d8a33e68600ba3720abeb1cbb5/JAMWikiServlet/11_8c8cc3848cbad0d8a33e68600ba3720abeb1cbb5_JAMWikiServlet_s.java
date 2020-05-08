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
 
 import java.util.LinkedHashMap;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.authentication.WikiUserDetailsImpl;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.Watchlist;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 /**
  * Provides the infrastructure that is common to all JAMWiki servlets.  Unless
  * special handling is required all JAMWiki servlets should extend this
  * servlet.
  */
 public abstract class JAMWikiServlet extends AbstractController {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiServlet.class.getName());
 
 	/** Flag to indicate whether or not the servlet should load the nav bar and other layout elements. */
 	protected boolean layout = true;
 	/** The prefix of the JSP file used to display the servlet output. */
 	protected String displayJSP = "wiki";
 	/** The name of the JSP file used to render the servlet output in case of errors. */
 	private static final String JSP_ERROR = "error-display.jsp";
 	/** Any page that take longer than this value (specified in milliseconds) will print a warning to the log. */
 	protected static final int SLOW_PAGE_LIMIT = 1000;
 	/** Parameter used to indicate that a topic should be the target of a successful login. */
 	protected static final String PARAM_LOGIN_SUCCESS_TARGET = "returnto";
 
 	/**
 	 * This method ensures that the left menu, logo, and other required values
 	 * have been loaded into the session object.
 	 *
 	 * @param request The servlet request object.
 	 * @param next A ModelAndView object corresponding to the page being
 	 *  constructed.
 	 */
 	private void buildLayout(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException {
 		String virtualWikiName = pageInfo.getVirtualWikiName();
 		if (virtualWikiName == null) {
 			logger.severe("No virtual wiki available for page request " + request.getRequestURI());
 			virtualWikiName = Environment.getValue(Environment.PROP_VIRTUAL_WIKI_DEFAULT);
 		}
 		VirtualWiki virtualWiki = ServletUtil.retrieveVirtualWiki(virtualWikiName);
 		// build the layout contents
 		String leftMenu = ServletUtil.cachedContent(request.getContextPath(), request.getLocale(), virtualWikiName, WikiBase.SPECIAL_PAGE_LEFT_MENU, true);
 		next.addObject("leftMenu", leftMenu);
 		next.addObject("defaultTopic", virtualWiki.getDefaultTopicName());
 		next.addObject("virtualWiki", virtualWiki.getName());
 		next.addObject("logo", Environment.getValue(Environment.PROP_BASE_LOGO_IMAGE));
 		String bottomArea = ServletUtil.cachedContent(request.getContextPath(), request.getLocale(), virtualWiki.getName(), WikiBase.SPECIAL_PAGE_BOTTOM_AREA, true);
 		next.addObject("bottomArea", bottomArea);
 		next.addObject(WikiUtil.PARAMETER_VIRTUAL_WIKI, virtualWiki.getName());
 		int cssRevision = 0;
 		try {
 			cssRevision = WikiBase.getDataHandler().lookupTopic(virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, false, null).getCurrentVersionId();
 		} catch (DataAccessException e) {}
 		next.addObject("cssRevision", cssRevision);
 	}
 
 	/**
 	 * Build a map of links and the corresponding link text to be used as the
 	 * tab menu links for the WikiPageInfo object.
 	 */
 	private void buildTabMenu(HttpServletRequest request, WikiPageInfo pageInfo) {
 		LinkedHashMap<String, WikiMessage> links = new LinkedHashMap<String, WikiMessage>();
 		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
 		String pageName = pageInfo.getTopicName();
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		if (pageInfo.getAdmin()) {
 			if (userDetails.hasRole(Role.ROLE_SYSADMIN)) {
 				links.put("Special:Admin", new WikiMessage("tab.admin.configuration"));
 				links.put("Special:Maintenance", new WikiMessage("tab.admin.maintenance"));
 				links.put("Special:VirtualWiki", new WikiMessage("tab.admin.vwiki"));
 				links.put("Special:Roles", new WikiMessage("tab.admin.roles"));
 			}
 			if (userDetails.hasRole(Role.ROLE_TRANSLATE)) {
 				links.put("Special:Translation", new WikiMessage("tab.admin.translations"));
 			}
 		} else if (pageInfo.getSpecial()) {
 			// append query params for pages such as Special:Contributions that need it
 			String specialUrl = pageName;
 			if (!StringUtils.isBlank(request.getQueryString())) {
 				specialUrl = pageName + "?" + request.getQueryString();
 			}
 			links.put(specialUrl, new WikiMessage("tab.common.special"));
 		} else {
 			try {
 				String article = WikiUtil.extractTopicLink(virtualWiki, pageName);
 				String comments = WikiUtil.extractCommentsLink(virtualWiki, pageName);
 				links.put(article, new WikiMessage("tab.common.article"));
 				links.put(comments, new WikiMessage("tab.common.comments"));
 				if (ServletUtil.isEditable(virtualWiki, pageName, userDetails)) {
 					String editLink = "Special:Edit?topic=" + Utilities.encodeAndEscapeTopicName(pageName);
 					if (!StringUtils.isBlank(request.getParameter("topicVersionId"))) {
 						editLink += "&topicVersionId=" + request.getParameter("topicVersionId");
 					}
 					links.put(editLink, new WikiMessage("tab.common.edit"));
 				}
 				String historyLink = "Special:History?topic=" + Utilities.encodeAndEscapeTopicName(pageName);
 				links.put(historyLink, new WikiMessage("tab.common.history"));
 				if (ServletUtil.isMoveable(virtualWiki, pageName, userDetails)) {
 					String moveLink = "Special:Move?topic=" + Utilities.encodeAndEscapeTopicName(pageName);
 					links.put(moveLink, new WikiMessage("tab.common.move"));
 				}
 				if (!userDetails.hasRole(Role.ROLE_ANONYMOUS)) {
 					Watchlist watchlist = ServletUtil.currentWatchlist(request, virtualWiki);
 					boolean watched = watchlist.containsTopic(pageName);
 					String watchlistLabel = (watched) ? "tab.common.unwatch" : "tab.common.watch";
 					String watchlistLink = "Special:Watchlist?topic=" + Utilities.encodeAndEscapeTopicName(pageName);
 					links.put(watchlistLink, new WikiMessage(watchlistLabel));
 				}
 				if (pageInfo.isUserPage()) {
 					WikiLink wikiLink = LinkUtil.parseWikiLink(virtualWiki, pageName);
 					String contributionsLink = "Special:Contributions?contributor=" + Utilities.encodeAndEscapeTopicName(wikiLink.getArticle());
 					links.put(contributionsLink, new WikiMessage("tab.common.contributions"));
 				}
 				String linkToLink = "Special:LinkTo?topic=" + Utilities.encodeAndEscapeTopicName(pageName);
 				links.put(linkToLink, new WikiMessage("tab.common.links"));
 				if (userDetails.hasRole(Role.ROLE_ADMIN)) {
 					String manageLink = "Special:Manage?topic=" + Utilities.encodeAndEscapeTopicName(pageName);
 					links.put(manageLink, new WikiMessage("tab.common.manage"));
 				}
 				String printLink = "Special:Print?topic=" + Utilities.encodeAndEscapeTopicName(pageName);
 				links.put(printLink, new WikiMessage("tab.common.print"));
 			} catch (WikiException e) {
 				logger.severe("Unable to build tabbed menu links", e);
 			}
 		}
 		pageInfo.setTabMenu(links);
 		// determine the currently active tab
 		String activePage = WikiUtil.getTopicFromURI(request);
 		int pos;
 		for (String link : links.keySet()) {
 			if (StringUtils.isBlank(pageInfo.getSelectedTab())) {
 				// if no tab is selected default to the first one
 				pageInfo.setSelectedTab(link);
 			}
 			pos = link.indexOf("?");
 			if ((pos != -1 && StringUtils.equals(activePage, link.substring(0, pos))) || (pos == -1 && StringUtils.equals(activePage, link))) {
 				pageInfo.setSelectedTab(link);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Build a map of links and the corresponding link text to be used as the
 	 * user menu links for the WikiPageInfo object.
 	 */
 	private LinkedHashMap<String, WikiMessage> buildUserMenu(WikiPageInfo pageInfo) {
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		LinkedHashMap<String, WikiMessage> links = new LinkedHashMap<String, WikiMessage>();
 		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
 		if (userDetails.hasRole(Role.ROLE_ANONYMOUS) && !userDetails.hasRole(Role.ROLE_EMBEDDED)) {
 			// include the current page in the login link 
 			String loginLink = "Special:Login";
 			if (!StringUtils.startsWith(pageInfo.getTopicName(), "Special:Login")) {
 				loginLink += LinkUtil.appendQueryParam("", PARAM_LOGIN_SUCCESS_TARGET, pageInfo.getTopicName());
 			}
 			links.put(loginLink, new WikiMessage("common.login"));
 			links.put("Special:Account", new WikiMessage("usermenu.register"));
 		}
 		if (!userDetails.hasRole(Role.ROLE_ANONYMOUS)) {
 			WikiUser user = ServletUtil.currentWikiUser();
 			String userPage = Namespace.namespace(Namespace.USER_ID).getLabel(virtualWiki) + Namespace.SEPARATOR + user.getUsername();
 			String userCommentsPage = Namespace.namespace(Namespace.USER_COMMENTS_ID).getLabel(virtualWiki) + Namespace.SEPARATOR + user.getUsername();
 			String username = user.getUsername();
 			if (!StringUtils.isBlank(user.getDisplayName())) {
 				username = user.getDisplayName();
 			}
 			// user name will be escaped by the jamwiki:link tag
 			WikiMessage userMenuMessage = new WikiMessage("usermenu.user");
 			userMenuMessage.setParamsWithoutEscaping(new String[]{username});
 			links.put(userPage, userMenuMessage);
 			links.put(userCommentsPage, new WikiMessage("usermenu.usercomments"));
 			links.put("Special:Watchlist", new WikiMessage("usermenu.watchlist"));
 		}
 		if (!userDetails.hasRole(Role.ROLE_ANONYMOUS) && !userDetails.hasRole(Role.ROLE_NO_ACCOUNT)) {
 			links.put("Special:Account", new WikiMessage("usermenu.account"));
 		}
 		if (!userDetails.hasRole(Role.ROLE_ANONYMOUS) && !userDetails.hasRole(Role.ROLE_EMBEDDED)) {
 			links.put("Special:Logout", new WikiMessage("common.logout"));
 		}
 		if (userDetails.hasRole(Role.ROLE_SYSADMIN)) {
 			links.put("Special:Admin", new WikiMessage("usermenu.admin"));
 		} else if (userDetails.hasRole(Role.ROLE_TRANSLATE)) {
 			links.put("Special:Translation", new WikiMessage("tab.admin.translations"));
 		}
 		return links;
 	}
 
 	/**
 	 * Abstract method that must be implemented by all sub-classes to handle
 	 * servlet requests.
 	 *
 	 * @param request The servlet request object.
 	 * @param response The servlet response object.
 	 * @param next A ModelAndView object that has been initialized to the view
 	 *  specified by the <code>displayJSP</code> member variable.
 	 * @param pageInfo A WikiPageInfo object that will hold output parameters
 	 *  to be passed to the output JSP.
 	 * @return A ModelAndView object corresponding to the information to be
 	 *  rendered, or <code>null</code> if the method directly handles its own
 	 *  output, for example by writing directly to the output response.
 	 */
 	protected abstract ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception;
 
 	/**
 	 * Implement the handleRequestInternal method specified by the
 	 * Spring AbstractController class.
 	 *
 	 * @param request The servlet request object.
 	 * @param response The servlet response object.
 	 * @return A ModelAndView object corresponding to the information to be
 	 *  rendered, or <code>null</code> if the method directly handles its own
 	 *  output, for example by writing directly to the output response.
 	 * @throws Exception Thrown if any error occurs during method execution.
 	 */
 	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
 		long start = System.currentTimeMillis();
 		initParams();
 		ModelAndView next = new ModelAndView(this.displayJSP);
 		WikiPageInfo pageInfo = new WikiPageInfo(request);
 		try {
 			next = this.handleJAMWikiRequest(request, response, next, pageInfo);
 			if (next != null && this.layout) {
 				this.loadLayout(request, next, pageInfo);
 			}
 			if (next != null) {
 				next.addObject(ServletUtil.PARAMETER_PAGE_INFO, pageInfo);
 			}
 		} catch (Throwable t) {
			return this.viewError(request, t);
 		}
 		long execution = System.currentTimeMillis() - start;
 		if (execution > JAMWikiServlet.SLOW_PAGE_LIMIT) {
 			logger.info("Slow page loading time: " + request.getRequestURI() + " (" + (execution / 1000.000) + " s.)");
 		}
 		if (logger.isInfoEnabled()) {
 			String url = request.getRequestURI() + (!StringUtils.isEmpty(request.getQueryString()) ? "?" + request.getQueryString() : "");
 			logger.info("Loaded page " + url + " (" + (execution / 1000.000) + " s.)");
 		}
 		return next;
 	}
 
 	/**
 	 * Determine if a topic contains a spam pattern, and if so set the appropriate page parameters
 	 * including a "hasSpam" flag in the ModelAndView object.
 	 *
 	 * @param request The servlet request object.
 	 * @param next The current ModelAndView object.
 	 * @param topicName The name of the topic being examined for spam.
 	 * @param contents The contents of the topic being examined for spam.
 	 * @param editComment (Optional) The topic edit comment, which has also been a
 	 *  target for spambots.
 	 * @return <code>true</code> if the topic in question matches any spam pattern.
 	 */
 	protected boolean handleSpam(HttpServletRequest request, ModelAndView next, String topicName, String contents, String editComment) throws DataAccessException {
 		String result = ServletUtil.checkForSpam(request, topicName, contents, editComment);
 		if (result == null) {
 			return false;
 		}
 		WikiMessage spam = new WikiMessage("edit.exception.spam", result);
 		next.addObject("spam", spam);
 		next.addObject("hasSpam", "true");
 		return true;
 	}
 
 	/**
 	 * If any special servlet initialization needs to be performed it can be done
 	 * by overriding this method.  In particular, this method can be used to
 	 * override the defaults for the <code>layout</code> member variable, which
 	 * determines whether or not the output JSP should include the left navigation
 	 * and other layout values, and the <code>displayJSP</code> member variable,
 	 * which determine the JSP file used to render output.
 	 */
 	protected void initParams() {
 	}
 
 	/**
 	 * This method ensures that values required for rendering a JSP page have
 	 * been loaded into the ModelAndView object.  Examples of values that
 	 * may be handled by this method include topic name, username, etc.
 	 *
 	 * @param request The current servlet request object.
 	 * @param next The current ModelAndView object.
 	 * @param pageInfo The current WikiPageInfo object, containing basic page
 	 *  rendering information.
 	 */
 	private void loadLayout(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (next.getViewName() != null && next.getViewName().startsWith(ServletUtil.SPRING_REDIRECT_PREFIX)) {
 			// if this is a redirect, no need to load anything
 			return;
 		}
 		// load cached top area, nav bar, etc.
 		this.buildLayout(request, next, pageInfo);
 		if (StringUtils.isBlank(pageInfo.getTopicName())) {
 			pageInfo.setTopicName(WikiUtil.getTopicFromURI(request));
 		}
 		pageInfo.setUserMenu(this.buildUserMenu(pageInfo));
 		this.buildTabMenu(request, pageInfo);
 	}
 
 	/**
	 * Method used when redirecting to an error page.
 	 *
 	 * @param request The servlet request object.
 	 * @param t The exception that is the source of the error.
 	 * @return Returns a ModelAndView object corresponding to the error page display.
 	 */
	private ModelAndView viewError(HttpServletRequest request, Throwable t) {
 		if (!(t instanceof WikiException)) {
 			logger.severe("Servlet error", t);
 		}
 		ModelAndView next = new ModelAndView("wiki");
 		WikiPageInfo pageInfo = new WikiPageInfo(request);
 		pageInfo.setPageTitle(new WikiMessage("error.title"));
 		pageInfo.setContentJsp(JSP_ERROR);
 		pageInfo.setSpecial(true);
 		if (t instanceof WikiException) {
 			WikiException we = (WikiException)t;
 			pageInfo.setException(we.getWikiMessage());
 			next.addObject("messageObject", we.getWikiMessage());
 		} else {
 			String errorMessage = t.toString();
 			if (t.getCause() != null) {
 				errorMessage += " / " + t.getCause().toString();
 			}
 			WikiMessage wm = new WikiMessage("error.unknown", errorMessage);
 			pageInfo.setException(wm);
 			next.addObject("messageObject", wm);
 			logger.severe("Failure while loading JSP: " + request.getServletPath(), t);
 		}
 		try {
 			this.loadLayout(request, next, pageInfo);
 		} catch (Exception err) {
 			logger.severe("Unable to load default layout", err);
 		}
 		next.addObject(ServletUtil.PARAMETER_PAGE_INFO, pageInfo);
 		return next;
 	}
 }
