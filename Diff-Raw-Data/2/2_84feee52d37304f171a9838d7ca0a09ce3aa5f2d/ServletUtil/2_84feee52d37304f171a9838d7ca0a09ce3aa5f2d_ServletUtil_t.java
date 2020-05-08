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
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Locale;
 import javax.servlet.http.HttpServletRequest;
 import net.sf.ehcache.Element;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Category;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.Watchlist;
 import org.jamwiki.model.WikiFileVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserDocument;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.NamespaceHandler;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiCache;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Utility methods useful when processing JAMWiki servlet requests.
  */
 public class ServletUtil {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(ServletUtil.class.getName());
 	protected static final String JSP_ERROR = "error-display.jsp";
 	protected static final String JSP_LOGIN = "login.jsp";
 	public static final String PARAMETER_PAGE_INFO = "pageInfo";
 	public static final String PARAMETER_TOPIC = "topic";
 	public static final String PARAMETER_TOPIC_OBJECT = "topicObject";
 	public static final String PARAMETER_VIRTUAL_WIKI = "virtualWiki";
 	public static final String PARAMETER_WATCHLIST = "watchlist";
 	private static final String SPRING_REDIRECT_PREFIX = "redirect:";
 	public static final String USER_COOKIE = "user-cookie";
 	public static final String USER_COOKIE_DELIMITER = "|";
 	// FIXME - make configurable
 	public static final int USER_COOKIE_EXPIRES = 60 * 60 * 24 * 14; // 14 days
 
 	/**
 	 *
 	 */
 	private ServletUtil() {
 	}
 
 	/**
 	 * This method ensures that the left menu, logo, and other required values
 	 * have been loaded into the session object.
 	 *
 	 * @param request The servlet request object.
 	 * @param next A ModelAndView object corresponding to the page being
 	 *  constructed.
 	 */
 	private static void buildLayout(HttpServletRequest request, ModelAndView next) {
 		String virtualWikiName = Utilities.getVirtualWikiFromURI(request);
 		if (virtualWikiName == null) {
 			logger.severe("No virtual wiki available for page request " + request.getRequestURI());
 			virtualWikiName = WikiBase.DEFAULT_VWIKI;
 		}
 		VirtualWiki virtualWiki = null;
 		String defaultTopic = null;
 		try {
 			virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(virtualWikiName);
 			defaultTopic = virtualWiki.getDefaultTopicName();
 		} catch (Exception e) {}
 		if (virtualWiki == null) {
 			logger.severe("No virtual wiki found for " + virtualWikiName);
 			virtualWikiName = WikiBase.DEFAULT_VWIKI;
 			defaultTopic = Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC);
 		}
 		// build the layout contents
 		String leftMenu = ServletUtil.cachedContent(request.getContextPath(), request.getLocale(), virtualWikiName, WikiBase.SPECIAL_PAGE_LEFT_MENU, true);
 		next.addObject("leftMenu", leftMenu);
 		next.addObject("defaultTopic", defaultTopic);
 		next.addObject("virtualWiki", virtualWikiName);
 		next.addObject("logo", Environment.getValue(Environment.PROP_BASE_LOGO_IMAGE));
 		String bottomArea = ServletUtil.cachedContent(request.getContextPath(), request.getLocale(), virtualWikiName, WikiBase.SPECIAL_PAGE_BOTTOM_AREA, true);
 		next.addObject("bottomArea", bottomArea);
 		next.addObject(ServletUtil.PARAMETER_VIRTUAL_WIKI, virtualWikiName);
 	}
 
 	/**
 	 * Build a map of links and the corresponding link text to be used as the
 	 * tab menu links for the WikiPageInfo object.
 	 */
 	private static LinkedHashMap buildTabMenu(HttpServletRequest request, WikiPageInfo pageInfo) {
 		LinkedHashMap links = new LinkedHashMap();
 		WikiUser user = Utilities.currentUser();
 		String pageName = pageInfo.getTopicName();
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		try {
 			if (pageInfo.getAdmin()) {
 				if (user.hasRole(Role.ROLE_ADMIN)) {
 					links.put("Special:Admin", new WikiMessage("tab.admin.configuration"));
 					links.put("Special:Maintenance", new WikiMessage("tab.admin.maintenance"));
 					links.put("Special:Roles", new WikiMessage("tab.admin.roles"));
 				}
 				if (user.hasRole(Role.ROLE_TRANSLATE)) {
 					links.put("Special:Translation", new WikiMessage("tab.admin.translations"));
 				}
 			} else if (pageInfo.getSpecial()) {
 				links.put(pageName, new WikiMessage("tab.common.special"));
 			} else {
 				String article = Utilities.extractTopicLink(pageName);
 				String comments = Utilities.extractCommentsLink(pageName);
 				links.put(article, new WikiMessage("tab.common.article"));
 				links.put(comments, new WikiMessage("tab.common.comments"));
 				if (ServletUtil.isEditable(virtualWiki, pageName, user)) {
 					String editLink = "Special:Edit?topic=" + Utilities.encodeForURL(pageName);
 					if (StringUtils.hasText(request.getParameter("topicVersionId"))) {
 						editLink += "&topicVersionId=" + request.getParameter("topicVersionId");
 					}
 					links.put(editLink, new WikiMessage("tab.common.edit"));
 				}
 				String historyLink = "Special:History?topic=" + Utilities.encodeForURL(pageName);
 				links.put(historyLink, new WikiMessage("tab.common.history"));
 				if (ServletUtil.isMoveable(virtualWiki, pageName, user)) {
 					String moveLink = "Special:Move?topic=" + Utilities.encodeForURL(pageName);
 					links.put(moveLink, new WikiMessage("tab.common.move"));
 				}
 				if (user.hasRole(Role.ROLE_USER)) {
 					Watchlist watchlist = Utilities.currentWatchlist(request, virtualWiki);
 					boolean watched = (watchlist.containsTopic(pageName));
 					String watchlistLabel = (watched) ? "tab.common.unwatch" : "tab.common.watch";
 					String watchlistLink = "Special:Watchlist?topic=" + Utilities.encodeForURL(pageName);
 					links.put(watchlistLink, new WikiMessage(watchlistLabel));
 				}
 				if (pageInfo.isUserPage()) {
 					WikiLink wikiLink = LinkUtil.parseWikiLink(pageName);
 					String contributionsLink = "Special:Contributions?contributor=" + Utilities.encodeForURL(wikiLink.getArticle());
 					links.put(contributionsLink, new WikiMessage("tab.common.contributions"));
 				}
 				String linkToLink = "Special:LinkTo?topic=" + Utilities.encodeForURL(pageName);
 				links.put(linkToLink, new WikiMessage("tab.common.links"));
 				if (user.hasRole(Role.ROLE_ADMIN)) {
 					String manageLink = "Special:Manage?topic=" + Utilities.encodeForURL(pageName);
 					links.put(manageLink, new WikiMessage("tab.common.manage"));
 				}
 				String printLink = "Special:Print?topic=" + Utilities.encodeForURL(pageName);
 				links.put(printLink, new WikiMessage("tab.common.print"));
 			}
 		} catch (Exception e) {
 			logger.severe("Unable to build tabbed menu links", e);
 		}
 		return links;
 	}
 
 	/**
 	 * Build a map of links and the corresponding link text to be used as the
 	 * user menu links for the WikiPageInfo object.
 	 */
 	private static LinkedHashMap buildUserMenu() {
 		LinkedHashMap links = new LinkedHashMap();
 		WikiUser user = Utilities.currentUser();
 		if (user.hasRole(Role.ROLE_ANONYMOUS) && !user.hasRole(Role.ROLE_EMBEDDED)) {
 			links.put("Special:Login", new WikiMessage("common.login"));
 			links.put("Special:Account", new WikiMessage("usermenu.register"));
 		}
 		if (user.hasRole(Role.ROLE_USER)) {
 			String userPage = NamespaceHandler.NAMESPACE_USER + NamespaceHandler.NAMESPACE_SEPARATOR + user.getUsername();
 			String userCommentsPage = NamespaceHandler.NAMESPACE_USER_COMMENTS + NamespaceHandler.NAMESPACE_SEPARATOR + user.getUsername();
 			String username = user.getUsername();
 			if (StringUtils.hasText(user.getDisplayName())) {
 				username = user.getDisplayName();
 			}
 			links.put(userPage, new WikiMessage("usermenu.user", username));
 			links.put(userCommentsPage, new WikiMessage("usermenu.usercomments"));
 			links.put("Special:Watchlist", new WikiMessage("usermenu.watchlist"));
 		}
 		if (user.hasRole(Role.ROLE_USER) && !user.hasRole(Role.ROLE_NO_ACCOUNT)) {
 			links.put("Special:Account", new WikiMessage("usermenu.account"));
 		}
 		if (user.hasRole(Role.ROLE_USER) && !user.hasRole(Role.ROLE_EMBEDDED)) {
 			links.put("Special:Logout", new WikiMessage("common.logout"));
 		}
 		if (user.hasRole(Role.ROLE_ADMIN)) {
 			links.put("Special:Admin", new WikiMessage("usermenu.admin"));
 		} else if (user.hasRole(Role.ROLE_TRANSLATE)) {
			links.put("Special:Translation", new WikiMessage("tab.admin.translations"));
 		}
 		return links;
 	}
 
 	/**
 	 * Retrieve the content of a topic from the cache, or if it is not yet in
 	 * the cache then add it to the cache.
 	 *
 	 * @param context The servlet context for the topic being retrieved.  May
 	 *  be <code>null</code> if the <code>cook</code> parameter is set to
 	 *  <code>false</code>.
 	 * @param locale The locale for the topic being retrieved.  May be
 	 *  <code>null</code> if the <code>cook</code> parameter is set to
 	 *  <code>false</code>.
 	 * @param virtualWiki The virtual wiki for the topic being retrieved.
 	 * @param topicName The name of the topic being retrieved.
 	 * @param cook A parameter indicating whether or not the content should be
 	 *  parsed before it is added to the cache.  Stylesheet content (CSS) is not
 	 *  parsed, but most other content is parsed.
 	 * @return The parsed or unparsed (depending on the <code>cook</code>
 	 *  parameter) topic content.
 	 */
 	protected static String cachedContent(String context, Locale locale, String virtualWiki, String topicName, boolean cook) {
 		String content = null;
 		String key = WikiCache.key(virtualWiki, topicName);
 		Element cacheElement = WikiCache.retrieveFromCache(WikiBase.CACHE_PARSED_TOPIC_CONTENT, key);
 		if (cacheElement != null) {
 			content = (String)cacheElement.getObjectValue();
 			return (content == null) ? null : new String(content);
 		}
 		try {
 			Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 			content = topic.getTopicContent();
 			if (cook) {
 				ParserInput parserInput = new ParserInput();
 				parserInput.setContext(context);
 				parserInput.setLocale(locale);
 				parserInput.setVirtualWiki(virtualWiki);
 				parserInput.setTopicName(topicName);
 				content = Utilities.parse(parserInput, null, content);
 			}
 			WikiCache.addToCache(WikiBase.CACHE_PARSED_TOPIC_CONTENT, key, content);
 		} catch (Exception e) {
 			logger.warning("error getting cached page " + virtualWiki + " / " + topicName, e);
 			return null;
 		}
 		return content;
 	}
 
 	/**
 	 * Initialize topic values for a Topic object.  This method will check to
 	 * see if a topic with the specified name exists, and if it does exist
 	 * then that topic will be returned.  Otherwise a new topic will be
 	 * initialized, setting initial parameters such as topic name, virtual
 	 * wiki, and topic type.
 	 *
 	 * @param virtualWiki The virtual wiki name for the topic being
 	 *  initialized.
 	 * @param topicName The name of the topic being initialized.
 	 * @return A new topic object with basic fields initialized, or if a topic
 	 *  with the given name already exists then the pre-existing topic is
 	 *  returned.
 	 * @throws Exception Thrown if any error occurs while retrieving or
 	 *  initializing the topic object.
 	 */
 	protected static Topic initializeTopic(String virtualWiki, String topicName) throws Exception {
 		Utilities.validateTopicName(topicName);
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic != null) {
 			return topic;
 		}
 		topic = new Topic();
 		topic.setName(topicName);
 		topic.setVirtualWiki(virtualWiki);
 		WikiLink wikiLink = LinkUtil.parseWikiLink(topicName);
 		String namespace = wikiLink.getNamespace();
 		if (namespace != null) {
 			if (namespace.equals(NamespaceHandler.NAMESPACE_CATEGORY)) {
 				topic.setTopicType(Topic.TYPE_CATEGORY);
 			} else if (namespace.equals(NamespaceHandler.NAMESPACE_TEMPLATE)) {
 				topic.setTopicType(Topic.TYPE_TEMPLATE);
 			}
 		}
 		return topic;
 	}
 
 	/**
 	 * Determine if a user has permission to edit a topic.
 	 *
 	 * @param virtualWiki The virtual wiki name for the topic in question.
 	 * @param topicName The name of the topic in question.
 	 * @param user The current Wiki user, or <code>null</code> if there is
 	 *  no current user.
 	 * @return <code>true</code> if the user is allowed to edit the topic,
 	 *  <code>false</code> otherwise.
 	 */
 	protected static boolean isEditable(String virtualWiki, String topicName, WikiUser user) throws Exception {
 		if (user == null || !user.hasRole(Role.ROLE_EDIT_EXISTING)) {
 			// user does not have appropriate permissions
 			return false;
 		}
 		if (!user.hasRole(Role.ROLE_EDIT_NEW) && WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null) == null) {
 			// user does not have appropriate permissions
 			return false;
 		}
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			// new topic, edit away...
 			return true;
 		}
 		if (topic.getAdminOnly() && (user == null || !user.hasRole(Role.ROLE_ADMIN))) {
 			return false;
 		}
 		if (topic.getReadOnly()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Determine if a user has permission to move a topic.
 	 *
 	 * @param virtualWiki The virtual wiki name for the topic in question.
 	 * @param topicName The name of the topic in question.
 	 * @param user The current Wiki user, or <code>null</code> if there is
 	 *  no current user.
 	 * @return <code>true</code> if the user is allowed to move the topic,
 	 *  <code>false</code> otherwise.
 	 */
 	protected static boolean isMoveable(String virtualWiki, String topicName, WikiUser user) throws Exception {
 		if (user == null || !user.hasRole(Role.ROLE_MOVE)) {
 			// no permission granted to move pages
 			return false;
 		}
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			// cannot move a topic that doesn't exist
 			return false;
 		}
 		if (topic.getReadOnly()) {
 			return false;
 		}
 		if (topic.getAdminOnly() && (user == null || !user.hasRole(Role.ROLE_ADMIN))) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Examine the request object, and see if the requested topic or page
 	 * matches a given value.
 	 *
 	 * @param request The servlet request object.
 	 * @param value The value to match against the current topic or page name.
 	 * @return <code>true</code> if the value matches the current topic or
 	 *  page name, <code>false</code> otherwise.
 	 */
 	protected static boolean isTopic(HttpServletRequest request, String value) {
 		try {
 			String topic = Utilities.getTopicFromURI(request);
 			if (!StringUtils.hasText(topic)) {
 				return false;
 			}
 			if (value != null &&  topic.equals(value)) {
 				return true;
 			}
 		} catch (Exception e) {}
 		return false;
 	}
 
 	/**
 	 * Utility method for adding categories associated with the current topic
 	 * to the ModelAndView object.  This method adds a hashmap of category
 	 * names and sort keys to the session that can then be retrieved for
 	 * display during rendering.
 	 *
 	 * @param next The current ModelAndView object used to return rendering
 	 *  information.
 	 * @param virtualWiki The virtual wiki name for the topic being rendered.
 	 * @param topicName The name of the topic that is being rendered.
 	 */
 	protected static void loadCategoryContent(ModelAndView next, String virtualWiki, String topicName) throws Exception {
 		String categoryName = topicName.substring(NamespaceHandler.NAMESPACE_CATEGORY.length() + NamespaceHandler.NAMESPACE_SEPARATOR.length());
 		next.addObject("categoryName", categoryName);
 		Collection categoryTopics = WikiBase.getDataHandler().lookupCategoryTopics(virtualWiki, topicName, Topic.TYPE_ARTICLE);
 		next.addObject("categoryTopics", categoryTopics);
 		next.addObject("numCategoryTopics", new Integer(categoryTopics.size()));
 		Collection categoryImages = WikiBase.getDataHandler().lookupCategoryTopics(virtualWiki, topicName, Topic.TYPE_IMAGE);
 		next.addObject("categoryImages", categoryImages);
 		next.addObject("numCategoryImages", new Integer(categoryImages.size()));
 		Collection tempSubcategories = WikiBase.getDataHandler().lookupCategoryTopics(virtualWiki, topicName, Topic.TYPE_CATEGORY);
 		LinkedHashMap subCategories = new LinkedHashMap();
 		for (Iterator iterator = tempSubcategories.iterator(); iterator.hasNext();) {
 			Category category = (Category)iterator.next();
 			String value = category.getChildTopicName().substring(NamespaceHandler.NAMESPACE_CATEGORY.length() + NamespaceHandler.NAMESPACE_SEPARATOR.length());
 			subCategories.put(category.getChildTopicName(), value);
 		}
 		next.addObject("subCategories", subCategories);
 		next.addObject("numSubCategories", new Integer(subCategories.size()));
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
 	protected static void loadDefaults(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (next.getViewName() != null && next.getViewName().startsWith(ServletUtil.SPRING_REDIRECT_PREFIX)) {
 			// if this is a redirect, no need to load anything
 			return;
 		}
 		// load cached top area, nav bar, etc.
 		ServletUtil.buildLayout(request, next);
 		if (!StringUtils.hasText(pageInfo.getTopicName())) {
 			pageInfo.setTopicName(Utilities.getTopicFromURI(request));
 		}
 		pageInfo.setUserMenu(ServletUtil.buildUserMenu());
 		pageInfo.setTabMenu(ServletUtil.buildTabMenu(request, pageInfo));
 		next.addObject(ServletUtil.PARAMETER_PAGE_INFO, pageInfo);
 	}
 
 	/**
 	 * Utility method for parsing a multipart servlet request.  This method returns
 	 * an iterator of FileItem objects that corresponds to the request.
 	 *
 	 * @param request The servlet request containing the multipart request.
 	 * @return Returns an iterator of FileItem objects the corresponds to the request.
 	 * @throws Exception Thrown if any problems occur while processing the request.
 	 */
 	protected static Iterator processMultipartRequest(HttpServletRequest request) throws Exception {
 		// Create a factory for disk-based file items
 		DiskFileItemFactory factory = new DiskFileItemFactory();
 		factory.setRepository(new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH)));
 		ServletFileUpload upload = new ServletFileUpload(factory);
 		upload.setHeaderEncoding("UTF-8");
 		upload.setSizeMax(Environment.getLongValue(Environment.PROP_FILE_MAX_FILE_SIZE));
 		return upload.parseRequest(request).iterator();
 	}
 
 	/**
 	 * Modify the current ModelAndView object to create a Spring redirect
 	 * response, meaning that the view name becomes "redirect:" followed by
 	 * the redirection target.
 	 *
 	 * @param next The current ModelAndView object, which will be reset by
 	 *  this method.
 	 * @param virtualWiki The virtual wiki name for the page being redirected
 	 *  to.
 	 * @param destination The topic or page name that is the redirection
 	 *  target.  An example might be "Special:Login".
 	 */
 	protected static void redirect(ModelAndView next, String virtualWiki, String destination) throws Exception {
 		String target = LinkUtil.buildInternalLinkUrl(null, virtualWiki, destination);
 		String view = ServletUtil.SPRING_REDIRECT_PREFIX + target;
 		next.clear();
 		next.setViewName(view);
 	}
 
 	/**
 	 * Utility method used when redirecting to an error page.
 	 *
 	 * @param request The servlet request object.
 	 * @param t The exception that is the source of the error.
 	 * @return Returns a ModelAndView object corresponding to the error page display.
 	 */
 	protected static ModelAndView viewError(HttpServletRequest request, Throwable t) {
 		if (!(t instanceof WikiException)) {
 			logger.severe("Servlet error", t);
 		}
 		ModelAndView next = new ModelAndView("wiki");
 		WikiPageInfo pageInfo = new WikiPageInfo();
 		pageInfo.setPageTitle(new WikiMessage("error.title"));
 		pageInfo.setContentJsp(JSP_ERROR);
 		pageInfo.setSpecial(true);
 		if (t instanceof WikiException) {
 			WikiException we = (WikiException)t;
 			next.addObject("messageObject", we.getWikiMessage());
 		} else {
 			next.addObject("messageObject", new WikiMessage("error.unknown", t.toString()));
 		}
 		try {
 			ServletUtil.loadDefaults(request, next, pageInfo);
 		} catch (Exception err) {
 			logger.severe("Unable to load default layout", err);
 		}
 		return next;
 	}
 
 	/**
 	 * Utility method used when redirecting to a login page.
 	 *
 	 * @param request The servlet request object.
 	 * @param pageInfo The current WikiPageInfo object, which contains
 	 *  information needed for rendering the final JSP page.
 	 * @param topic The topic to be redirected to.  Valid examples are
 	 *  "Special:Admin", "StartingPoints", etc.
 	 * @param messageObject A WikiMessage object to be displayed on the login
 	 *  page.
 	 * @return Returns a ModelAndView object corresponding to the login page
 	 *  display.
 	 * @throws Exception Thrown if any error occurs during processing.
 	 */
 	protected static ModelAndView viewLogin(HttpServletRequest request, WikiPageInfo pageInfo, String topic, WikiMessage messageObject) throws Exception {
 		ModelAndView next = new ModelAndView("wiki");
 		pageInfo.reset();
 		String virtualWikiName = Utilities.getVirtualWikiFromURI(request);
 		String target = request.getParameter("target");
 		if (!StringUtils.hasText(target)) {
 			if (!StringUtils.hasText(topic)) {
 				VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(virtualWikiName);
 				topic = virtualWiki.getDefaultTopicName();
 			}
 			target = topic;
 			if (StringUtils.hasText(request.getQueryString())) {
 				target += "?" + request.getQueryString();
 			}
 		}
 		next.addObject("target", target);
 		pageInfo.setPageTitle(new WikiMessage("login.title"));
 		pageInfo.setContentJsp(JSP_LOGIN);
 		pageInfo.setSpecial(true);
 		if (messageObject != null) {
 			next.addObject("messageObject", messageObject);
 		}
 		return next;
 	}
 
 	/**
 	 * Utility method used when viewing a topic.
 	 *
 	 * @param request The current servlet request object.
 	 * @param next The current Spring ModelAndView object.
 	 * @param pageInfo The current WikiPageInfo object, which contains
 	 *  information needed for rendering the final JSP page.
 	 * @param topicName The topic being viewed.  This value must be a valid
 	 *  topic that can be loaded as a org.jamwiki.model.Topic object.
 	 * @throws Exception Thrown if any error occurs during processing.
 	 */
 	protected static void viewTopic(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String topicName) throws Exception {
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		if (!StringUtils.hasText(virtualWiki)) {
 			virtualWiki = WikiBase.DEFAULT_VWIKI;
 		}
 		Topic topic = ServletUtil.initializeTopic(virtualWiki, topicName);
 		if (topic.getTopicId() <= 0) {
 			// topic does not exist, display empty page
 			next.addObject("notopic", new WikiMessage("topic.notcreated", topicName));
 		}
 		WikiMessage pageTitle = new WikiMessage("topic.title", topicName);
 		viewTopic(request, next, pageInfo, pageTitle, topic, true);
 	}
 
 	/**
 	 * Utility method used when viewing a topic.
 	 *
 	 * @param request The current servlet request object.
 	 * @param next The current Spring ModelAndView object.
 	 * @param pageInfo The current WikiPageInfo object, which contains
 	 *  information needed for rendering the final JSP page.
 	 * @param pageTitle The title of the page being rendered.
 	 * @param topic The Topic object for the topic being displayed.
 	 * @param sectionEdit Set to <code>true</code> if edit links should be displayed
 	 *  for each section of the topic.
 	 * @throws Exception Thrown if any error occurs during processing.
 	 */
 	protected static void viewTopic(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, WikiMessage pageTitle, Topic topic, boolean sectionEdit) throws Exception {
 		// FIXME - what should the default be for topics that don't exist?
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		Utilities.validateTopicName(topic.getName());
 		if (topic.getTopicType() == Topic.TYPE_REDIRECT && (request.getParameter("redirect") == null || !request.getParameter("redirect").equalsIgnoreCase("no"))) {
 			Topic child = Utilities.findRedirectedTopic(topic, 0);
 			if (!child.getName().equals(topic.getName())) {
 				pageInfo.setRedirectName(topic.getName());
 				pageTitle = new WikiMessage("topic.title", child.getName());
 				topic = child;
 			}
 		}
 		String virtualWiki = topic.getVirtualWiki();
 		String topicName = topic.getName();
 		WikiUser user = Utilities.currentUser();
 		if (sectionEdit && !ServletUtil.isEditable(virtualWiki, topicName, user)) {
 			sectionEdit = false;
 		}
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext(request.getContextPath());
 		parserInput.setLocale(request.getLocale());
 		parserInput.setWikiUser(user);
 		parserInput.setTopicName(topicName);
 		parserInput.setUserIpAddress(request.getRemoteAddr());
 		parserInput.setVirtualWiki(virtualWiki);
 		parserInput.setAllowSectionEdit(sectionEdit);
 		ParserDocument parserDocument = new ParserDocument();
 		String content = Utilities.parse(parserInput, parserDocument, topic.getTopicContent());
 		// FIXME - the null check should be unnecessary
 		if (parserDocument != null && parserDocument.getCategories().size() > 0) {
 			LinkedHashMap categories = new LinkedHashMap();
 			for (Iterator iterator = parserDocument.getCategories().keySet().iterator(); iterator.hasNext();) {
 				String key = (String)iterator.next();
 				String value = key.substring(NamespaceHandler.NAMESPACE_CATEGORY.length() + NamespaceHandler.NAMESPACE_SEPARATOR.length());
 				categories.put(key, value);
 			}
 			next.addObject("categories", categories);
 		}
 		topic.setTopicContent(content);
 		if (topic.getTopicType() == Topic.TYPE_CATEGORY) {
 			loadCategoryContent(next, virtualWiki, topic.getName());
 		}
 		if (topic.getTopicType() == Topic.TYPE_IMAGE || topic.getTopicType() == Topic.TYPE_FILE) {
 			Collection fileVersions = WikiBase.getDataHandler().getAllWikiFileVersions(virtualWiki, topicName, true);
 			for (Iterator iterator = fileVersions.iterator(); iterator.hasNext();) {
 				// update version urls to include web root path
 				WikiFileVersion fileVersion = (WikiFileVersion)iterator.next();
 				String url = FilenameUtils.normalize(Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) + "/" + fileVersion.getUrl());
 				url = FilenameUtils.separatorsToUnix(url);
 				fileVersion.setUrl(url);
 			}
 			next.addObject("fileVersions", fileVersions);
 			if (topic.getTopicType() == Topic.TYPE_IMAGE) {
 				next.addObject("topicImage", new Boolean(true));
 			} else {
 				next.addObject("topicFile", new Boolean(true));
 			}
 		}
 		pageInfo.setSpecial(false);
 		pageInfo.setTopicName(topicName);
 		next.addObject(ServletUtil.PARAMETER_TOPIC_OBJECT, topic);
 		if (pageTitle != null) {
 			pageInfo.setPageTitle(pageTitle);
 		}
 	}
 }
