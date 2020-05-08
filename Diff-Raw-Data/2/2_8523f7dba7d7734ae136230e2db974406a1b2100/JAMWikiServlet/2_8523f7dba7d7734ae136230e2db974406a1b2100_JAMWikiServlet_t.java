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
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.commons.io.FilenameUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Category;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiFileVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserDocument;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.NamespaceHandler;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 /**
  * JAMWikiServlet is the base servlet which all other JAMWiki servlets extend.
  */
 public abstract class JAMWikiServlet extends AbstractController {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiServlet.class.getName());
 	public static final String PARAMETER_PAGE_INFO = "pageInfo";
 	public static final String PARAMETER_TOPIC = "topic";
 	public static final String PARAMETER_TOPIC_OBJECT = "topicObject";
 	public static final String PARAMETER_USER = "user";
 	public static final String PARAMETER_VIRTUAL_WIKI = "virtualWiki";
 	public static final String SPRING_REDIRECT_PREFIX = "redirect:";
 	public static final String USER_COOKIE = "user-cookie";
 	public static final String USER_COOKIE_DELIMITER = "|";
 	// FIXME - make configurable
 	public static final int USER_COOKIE_EXPIRES = 60 * 60 * 24 * 14; // 14 days
 	private static LinkedHashMap cachedContents = new LinkedHashMap();
 
 	/**
 	 * This method ensures that the left menu, logo, and other required values
 	 * have been loaded into the session object.
 	 *
 	 * @param request The servlet request object.
 	 * @param next A ModelAndView object corresponding to the page being
 	 *  constructed.
 	 */
 	private static void buildLayout(HttpServletRequest request, ModelAndView next) {
 		String virtualWikiName = JAMWikiServlet.getVirtualWikiFromURI(request);
 		if (virtualWikiName == null) {
 			logger.severe("No virtual wiki available for page request " + request.getRequestURI());
 			virtualWikiName = WikiBase.DEFAULT_VWIKI;
 		}
 		VirtualWiki virtualWiki = null;
 		String defaultTopic = null;
 		try {
 			virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
 			defaultTopic = virtualWiki.getDefaultTopicName();
 		} catch (Exception e) {}
 		if (virtualWiki == null) {
 			logger.severe("No virtual wiki found for " + virtualWikiName);
 			virtualWikiName = WikiBase.DEFAULT_VWIKI;
 			defaultTopic = Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC);
 		}
 		// build the layout contents
 		String leftMenu = JAMWikiServlet.getCachedContent(request, virtualWikiName, WikiBase.SPECIAL_PAGE_LEFT_MENU, true);
 		next.addObject("leftMenu", leftMenu);
 		next.addObject("defaultTopic", defaultTopic);
 		next.addObject("logo", Environment.getValue(Environment.PROP_BASE_LOGO_IMAGE));
 		String bottomArea = JAMWikiServlet.getCachedContent(request, virtualWikiName, WikiBase.SPECIAL_PAGE_BOTTOM_AREA, true);
 		next.addObject("bottomArea", bottomArea);
 		next.addObject(PARAMETER_VIRTUAL_WIKI, virtualWikiName);
 	}
 
 	/**
 	 * Create a pagination object based on parameters found in the current
 	 * request.
 	 *
 	 * @param request The servlet request object.
 	 * @param next A ModelAndView object corresponding to the page being
 	 *  constructed.
 	 * @return A Pagination object constructed from parameters found in the
 	 *  request object.
 	 */
 	public static Pagination buildPagination(HttpServletRequest request, ModelAndView next) {
 		int num = Environment.getIntValue(Environment.PROP_RECENT_CHANGES_NUM);
 		if (request.getParameter("num") != null) {
 			try {
 				num = new Integer(request.getParameter("num")).intValue();
 			} catch (Exception e) {
 				// invalid number
 			}
 		}
 		int offset = 0;
 		if (request.getParameter("offset") != null) {
 			try {
 				offset = new Integer(request.getParameter("offset")).intValue();
 			} catch (Exception e) {
 				// invalid number
 			}
 		}
 		if (next != null) {
 			next.addObject("num", new Integer(num));
 			next.addObject("offset", new Integer(offset));
 		}
 		return new Pagination(num, offset);
 	}
 
 	/**
 	 *
 	 */
 	public static String getCachedContent(HttpServletRequest request, String virtualWiki, String topicName, boolean cook) {
 		String content = (String)cachedContents.get(virtualWiki + "-" + topicName);
 		if (content == null) {
 			try {
 				Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 				content = topic.getTopicContent();
 				if (cook) {
 					ParserInput parserInput = new ParserInput();
 					parserInput.setContext(request.getContextPath());
 					parserInput.setLocale(request.getLocale());
 					parserInput.setVirtualWiki(virtualWiki);
 					parserInput.setTopicName(topicName);
 					ParserDocument parserDocument = Utilities.parse(parserInput, content);
 					content = parserDocument.getContent();
 				}
 				cachedContents.put(virtualWiki + "-" + topicName, content);
 			} catch (Exception e) {
 				logger.warning("error getting cached page " + virtualWiki + " / " + topicName, e);
 				return null;
 			}
 		}
 		return content;
 	}
 
 	/**
 	 *
 	 */
 	public static String getTopicFromURI(HttpServletRequest request) throws Exception {
 		// skip one directory, which is the virutal wiki
 		String topic = Utilities.retrieveDirectoriesFromURI(request, 1);
 		if (topic == null) {
 			throw new Exception("No topic in URL: " + request.getRequestURI());
 		}
 		return Utilities.decodeFromURL(topic);
 	}
 
 	/**
 	 *
 	 */
 	public static String getTopicFromRequest(HttpServletRequest request) throws Exception {
 		String topic = request.getParameter(JAMWikiServlet.PARAMETER_TOPIC);
 		if (topic == null) {
 			topic = (String)request.getAttribute(JAMWikiServlet.PARAMETER_TOPIC);
 		}
 		if (topic == null) return null;
 		return Utilities.decodeFromRequest(topic);
 	}
 
 	/**
 	 *
 	 */
 	public static String getVirtualWikiFromRequest(HttpServletRequest request) {
 		String virtualWiki = request.getParameter(JAMWikiServlet.PARAMETER_VIRTUAL_WIKI);
 		if (virtualWiki == null) {
 			virtualWiki = (String)request.getAttribute(JAMWikiServlet.PARAMETER_VIRTUAL_WIKI);
 		}
 		if (virtualWiki == null) return null;
 		return Utilities.decodeFromRequest(virtualWiki);
 	}
 
 	/**
 	 *
 	 */
 	public static String getVirtualWikiFromURI(HttpServletRequest request) {
 		String uri = Utilities.retrieveDirectoriesFromURI(request, 0);
 		if (uri == null) {
 			logger.warning("No virtual wiki found in URL: " + request.getRequestURI());
 			return null;
 		}
 		int slashIndex = uri.indexOf('/');
 		if (slashIndex == -1) {
 			logger.warning("No virtual wiki found in URL: " + request.getRequestURI());
 			return null;
 		}
 		String virtualWiki = uri.substring(0, slashIndex);
 		return Utilities.decodeFromURL(virtualWiki);
 	}
 
 	/**
 	 * Initialize topic values for the topic being edited.  If a topic with
 	 * the specified name already exists then it will be initialized,
 	 * otherwise a new topic is created.
 	 */
 	protected Topic initializeTopic(String virtualWiki, String topicName) throws Exception {
 		Utilities.validateTopicName(topicName);
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		if (topic == null) {
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
 		}
 		return topic;
 	}
 
 	/**
 	 *
 	 */
 	protected static boolean isTopic(HttpServletRequest request, String value) {
 		try {
 			String topic = JAMWikiServlet.getTopicFromURI(request);
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
 	 * Utility method for adding category content to the ModelAndView object.
 	 */
 	protected void loadCategoryContent(ModelAndView next, String virtualWiki, String topicName) throws Exception {
 		String categoryName = topicName.substring(NamespaceHandler.NAMESPACE_CATEGORY.length() + NamespaceHandler.NAMESPACE_SEPARATOR.length());
 		next.addObject("categoryName", categoryName);
 		Collection categoryTopics = WikiBase.getHandler().lookupCategoryTopics(virtualWiki, topicName, Topic.TYPE_ARTICLE);
 		next.addObject("categoryTopics", categoryTopics);
 		next.addObject("numCategoryTopics", new Integer(categoryTopics.size()));
 		Collection categoryImages = WikiBase.getHandler().lookupCategoryTopics(virtualWiki, topicName, Topic.TYPE_IMAGE);
 		next.addObject("categoryImages", categoryImages);
 		next.addObject("numCategoryImages", new Integer(categoryImages.size()));
 		Collection tempSubcategories = WikiBase.getHandler().lookupCategoryTopics(virtualWiki, topicName, Topic.TYPE_CATEGORY);
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
 	 *
 	 */
 	protected void loadDefaults(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
 		if (next.getViewName() != null && next.getViewName().startsWith(SPRING_REDIRECT_PREFIX)) {
 			// if this is a redirect, no need to load anything
 			return;
 		}
 		// load cached top area, nav bar, etc.
 		try {
 			this.buildLayout(request, next);
 			// add link to user page and comments page
 			WikiUser user = Utilities.currentUser(request);
 			if (user != null) {
 				next.addObject("userpage", NamespaceHandler.NAMESPACE_USER + NamespaceHandler.NAMESPACE_SEPARATOR + user.getLogin());
 				next.addObject("usercomments", NamespaceHandler.NAMESPACE_USER_COMMENTS + NamespaceHandler.NAMESPACE_SEPARATOR + user.getLogin());
 				next.addObject("adminUser", new Boolean(user.getAdmin()));
 			}
 			if (!pageInfo.getSpecial()) {
 				String article = Utilities.extractTopicLink(pageInfo.getTopicName());
 				String comments = Utilities.extractCommentsLink(pageInfo.getTopicName());
 				next.addObject("article", article);
 				next.addObject("comments", comments);
 				String editLink = "Special:Edit?topic=" + Utilities.encodeForURL(pageInfo.getTopicName());
 				if (StringUtils.hasText(request.getParameter("topicVersionId"))) {
 					editLink += "&topicVersionId=" + request.getParameter("topicVersionId");
 				}
 				next.addObject("edit", editLink);
 				if (Environment.getBooleanValue(Environment.PROP_TOPIC_NON_ADMIN_TOPIC_MOVE) || (user != null && user.getAdmin())) {
 					String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 					if (WikiBase.getHandler().exists(virtualWiki, article)) {
 						pageInfo.setCanMove(true);
 					}
 				}
 			}
 			if (!StringUtils.hasText(pageInfo.getTopicName())) {
 				pageInfo.setTopicName(JAMWikiServlet.getTopicFromURI(request));
 			}
 		} catch (Exception e) {
 			logger.severe("Unable to build default page layout", e);
 		}
 		next.addObject(PARAMETER_PAGE_INFO, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	protected void redirect(ModelAndView next, String virtualWiki, String destination) throws Exception {
 		String target = LinkUtil.buildInternalLinkUrl(null, virtualWiki, destination);
 		String view = SPRING_REDIRECT_PREFIX + target;
 		next.clear();
 		next.setViewName(view);
 	}
 
 	/**
 	 * Clears cached contents including the top area, left nav, bottom area, etc.
 	 * This method should be called when the contents of these areas may have been
 	 * modified.
 	 */
 	public static void removeCachedContents() {
 		cachedContents.clear();
 	}
 
 	/**
 	 * Action used when redirecting to an error page.
 	 *
 	 * @param request The servlet request object.
 	 * @return Returns a ModelAndView object corresponding to the error page display.
 	 * @param e The exception that is the source of the error.
 	 */
 	protected ModelAndView viewError(HttpServletRequest request, Exception e) {
 		if (!(e instanceof WikiException)) {
 			logger.severe("Servlet error", e);
 		}
 		ModelAndView next = new ModelAndView("wiki");
 		WikiPageInfo pageInfo = new WikiPageInfo();
 		pageInfo.setPageTitle(new WikiMessage("error.title"));
 		pageInfo.setAction(WikiPageInfo.ACTION_ERROR);
 		pageInfo.setSpecial(true);
 		if (e instanceof WikiException) {
 			WikiException we = (WikiException)e;
 			next.addObject("errorMessage", we.getWikiMessage());
 		} else {
			next.addObject("errorMessage", new WikiMessage("error.unknown", e.toString()));
 		}
 		loadDefaults(request, next, pageInfo);
 		return next;
 	}
 
 	/**
 	 * Action used when redirecting to a login page.
 	 *
 	 * @param request The servlet request object.
 	 * @param topic The topic to be redirected to.  Valid examples are "Special:Admin",
 	 *  "StartingPoints", etc.
 	 * @param errorMessage A WikiMessage object to be displayed on the login page.
 	 * @return Returns a ModelAndView object corresponding to the login page display.
 	 * @throws Exception Thrown if any error occurs while preparing the login page display.
 	 */
 	protected ModelAndView viewLogin(HttpServletRequest request, String topic, WikiMessage errorMessage) throws Exception {
 		ModelAndView next = new ModelAndView("wiki");
 		WikiPageInfo pageInfo = new WikiPageInfo();
 		String virtualWikiName = JAMWikiServlet.getVirtualWikiFromURI(request);
 		String redirect = request.getParameter("redirect");
 		if (!StringUtils.hasText(redirect)) {
 			if (!StringUtils.hasText(topic)) {
 				VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
 				topic = virtualWiki.getDefaultTopicName();
 			}
 			redirect = topic;
 			if (StringUtils.hasText(request.getQueryString())) {
 				redirect += "?" + request.getQueryString();
 			}
 		}
 		next.addObject("redirect", redirect);
 		pageInfo.setPageTitle(new WikiMessage("login.title"));
 		pageInfo.setAction(WikiPageInfo.ACTION_LOGIN);
 		pageInfo.setSpecial(true);
 		if (errorMessage != null) {
 			next.addObject("errorMessage", errorMessage);
 		}
 		loadDefaults(request, next, pageInfo);
 		return next;
 	}
 
 	/**
 	 * Action used when viewing a topic.
 	 *
 	 * @param request The servlet request object.
 	 * @param next The Spring ModelAndView object.
 	 * @param topicName The topic being viewed.  This value must be a valid topic that
 	 *  can be loaded as a org.jamwiki.model.Topic object.
 	 */
 	protected void viewTopic(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String topicName) throws Exception {
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		if (!StringUtils.hasText(virtualWiki)) {
 			virtualWiki = WikiBase.DEFAULT_VWIKI;
 		}
 		Topic topic = this.initializeTopic(virtualWiki, topicName);
 		if (topic.getTopicId() <= 0) {
 			// topic does not exist, display empty page
 			next.addObject("notopic", new WikiMessage("topic.notcreated", topicName));
 		}
 		WikiMessage pageTitle = new WikiMessage("topic.title", topicName);
 		viewTopic(request, next, pageInfo, pageTitle, topic, true);
 	}
 
 	/**
 	 * Action used when viewing a topic.
 	 *
 	 * @param request The servlet request object.
 	 * @param next The Spring ModelAndView object.
 	 * @param pageInfo A WikiPageInfo object containing page configuration info.
 	 * @param topic The Topic object for the topic being displayed.
 	 * @param sectionEdit Set to <code>true</code> if edit links should be displayed
 	 *  for each section of the topic.
 	 * @throws Exception Thrown if any error occurs during topic display.
 	 */
 	protected void viewTopic(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, WikiMessage pageTitle, Topic topic, boolean sectionEdit) throws Exception {
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
 		String displayName = request.getRemoteAddr();
 		WikiUser user = Utilities.currentUser(request);
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext(request.getContextPath());
 		parserInput.setLocale(request.getLocale());
 		parserInput.setWikiUser(user);
 		parserInput.setTopicName(topicName);
 		parserInput.setUserIpAddress(request.getRemoteAddr());
 		parserInput.setVirtualWiki(virtualWiki);
 		parserInput.setAllowSectionEdit(sectionEdit);
 		ParserDocument parserDocument = Utilities.parse(parserInput, topic.getTopicContent());
 		if (parserDocument != null) {
 			if (parserDocument.getCategories().size() > 0) {
 				LinkedHashMap categories = new LinkedHashMap();
 				for (Iterator iterator = parserDocument.getCategories().keySet().iterator(); iterator.hasNext();) {
 					String key = (String)iterator.next();
 					String value = key.substring(NamespaceHandler.NAMESPACE_CATEGORY.length() + NamespaceHandler.NAMESPACE_SEPARATOR.length());
 					categories.put(key, value);
 				}
 				next.addObject("categories", categories);
 			}
 			topic.setTopicContent(parserDocument.getContent());
 		}
 		if (topic.getTopicType() == Topic.TYPE_CATEGORY) {
 			loadCategoryContent(next, virtualWiki, topic.getName());
 		}
 		if (topic.getTopicType() == Topic.TYPE_IMAGE || topic.getTopicType() == Topic.TYPE_FILE) {
 			Collection fileVersions = WikiBase.getHandler().getAllWikiFileVersions(virtualWiki, topicName, true);
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
 		next.addObject(JAMWikiServlet.PARAMETER_TOPIC_OBJECT, topic);
 		if (pageTitle != null) {
 			pageInfo.setPageTitle(pageTitle);
 		}
 	}
 }
