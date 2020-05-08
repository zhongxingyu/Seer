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
 import java.io.FileNotFoundException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Properties;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import net.sf.ehcache.Element;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.lang.LocaleUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.authentication.JAMWikiAuthenticationConstants;
 import org.jamwiki.authentication.WikiUserDetailsImpl;
 import org.jamwiki.db.DatabaseConnection;
 import org.jamwiki.model.Category;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicType;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.Watchlist;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiFileVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.parser.ParserUtil;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.SpamFilter;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiCache;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Utility methods useful when processing JAMWiki servlet requests.
  */
 public class ServletUtil {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(ServletUtil.class.getName());
 	/** The name of the JSP file used to render the servlet output for logins. */
 	protected static final String JSP_LOGIN = "login.jsp";
 	/** The name of the output parameter used to store page information. */
 	public static final String PARAMETER_PAGE_INFO = "pageInfo";
 	/** The name of the output parameter used to store topic information. */
 	public static final String PARAMETER_TOPIC_OBJECT = "topicObject";
 	/** The name of the output parameter used to indicate that Spring should redirect to another servlet. */
 	protected static final String SPRING_REDIRECT_PREFIX = "redirect:";
 
 	/**
 	 *
 	 */
 	private ServletUtil() {
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
 	protected static String cachedContent(String context, Locale locale, String virtualWiki, String topicName, boolean cook) throws DataAccessException {
 		String content = null;
 		String key = WikiCache.key(virtualWiki, topicName);
 		Element cacheElement = WikiCache.retrieveFromCache(WikiBase.CACHE_PARSED_TOPIC_CONTENT, key);
 		if (cacheElement != null) {
 			content = (String)cacheElement.getObjectValue();
 			return (content == null) ? null : content;
 		}
 		try {
 			Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 			content = topic.getTopicContent();
 			if (cook) {
 				ParserInput parserInput = new ParserInput(virtualWiki, topicName);
 				parserInput.setContext(context);
 				parserInput.setLocale(locale);
 				content = ParserUtil.parse(parserInput, null, content);
 			}
 			WikiCache.addToCache(WikiBase.CACHE_PARSED_TOPIC_CONTENT, key, content);
 		} catch (Exception e) {
 			logger.warn("error getting cached page " + virtualWiki + " / " + topicName, e);
 			return null;
 		}
 		return content;
 	}
 
 	/**
 	 * This is a utility method that will check topic content for spam, and
 	 * return <code>null</code> if no matching values are found, or if a spam
 	 * pattern is found then that pattern will be returned.  It will also log
 	 * information about the offending spam and user to the logs.
 	 *
 	 * @param request The current servlet request.
 	 * @param topicName The name of the current topic being edited.
 	 * @param contents The text for the current topic that the user is trying to
 	 *  add.
 	 * @param editComment (Optional) The topic edit comment, which has also been a
 	 *  target for spambots.
 	 * @return <code>null</code> if nothing in the topic content matches a current
 	 *  spam pattern, or the text that matches a spam pattern if one is found.
 	 */
 	protected static String checkForSpam(HttpServletRequest request, String topicName, String contents, String editComment) throws DataAccessException {
 		String result = SpamFilter.containsSpam(contents);
 		if (StringUtils.isBlank(result) && !StringUtils.isBlank(editComment)) {
 			result = SpamFilter.containsSpam(editComment);
 		}
 		if (StringUtils.isBlank(result)) {
 			return null;
 		}
 		String message = "SPAM found in topic " + topicName + " (";
 		WikiUserDetailsImpl user = ServletUtil.currentUserDetails();
 		if (!user.hasRole(Role.ROLE_ANONYMOUS)) {
 			message += user.getUsername() + " / ";
 		}
 		message += ServletUtil.getIpAddress(request) + "): " + result;
 		logger.info(message);
 		return result;
 	}
 
 	/**
 	 * Retrieve the current <code>WikiUserDetailsImpl</code> from Spring Security
 	 * <code>SecurityContextHolder</code>.  If the current user is not
 	 * logged-in then this method will return an empty <code>WikiUserDetailsImpl</code>
 	 * object.
 	 *
 	 * @return The current logged-in <code>WikiUserDetailsImpl</code>, or an empty
 	 *  <code>WikiUserDetailsImpl</code> if there is no user currently logged in.
 	 *  This method will never return <code>null</code>.
 	 * @throws AuthenticationCredentialsNotFoundException If authentication
 	 *  credentials are unavailable.
 	 */
 	public static WikiUserDetailsImpl currentUserDetails() throws AuthenticationCredentialsNotFoundException {
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		return WikiUserDetailsImpl.initWikiUserDetailsImpl(auth);
 	}
 
 	/**
 	 * Retrieve the current <code>WikiUser</code> using the <code>WikiUserDetailsImpl</code>
 	 * from Spring Security <code>SecurityContextHolder</code>.  If there is no current
 	 * user (the user is not logged in) then this method will return an empty WikiUser.
 	 * The method will never return <code>null</code>.
 	 *
 	 * @return The current logged-in <code>WikiUser</code>, or an empty WikiUser if
 	 *  there is no user currently logged in.
 	 */
 	public static WikiUser currentWikiUser() throws AuthenticationCredentialsNotFoundException {
 		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
 		WikiUser user = new WikiUser();
 		String username = userDetails.getUsername();
 		if (username.equals(WikiUserDetailsImpl.ANONYMOUS_USER_USERNAME)) {
 			return user;
 		}
 		if (!WikiUtil.isFirstUse() && !WikiUtil.isUpgrade()) {
 			try {
 				// FIXME - do not lookup the user every time this method is called, that will kill performance
 				user = WikiBase.getDataHandler().lookupWikiUser(username);
 			} catch (DataAccessException e) {
 				logger.error("Failure while retrieving user from database with login: " + username, e);
 				return user;
 			}
 			if (user == null) {
 				// invalid user.  someone has either spoofed a cookie or the user account is no longer in
 				// the database.
 				logger.warn("No user exists for principal found in security context authentication: " + username);
 				SecurityContextHolder.clearContext();
 				throw new AuthenticationCredentialsNotFoundException("Invalid user credentials found - username " + username + " does not exist in this wiki installation");
 			}
 		}
 		return user;
 	}
 
 	/**
 	 * Retrieve the current logged-in user's watchlist from the session.  If
 	 * there is no watchlist return an empty watchlist.
 	 *
 	 * @param request The servlet request object.
 	 * @param virtualWiki The virtual wiki for the watchlist being parsed.
 	 * @return The current logged-in user's watchlist, or an empty watchlist
 	 *  if there is no watchlist in the session.
 	 * @throws WikiException Thrown if any error occurs during processing.
 	 */
 	public static Watchlist currentWatchlist(HttpServletRequest request, String virtualWiki) throws WikiException {
 		// try to get watchlist stored in session
 		if (request.getSession(false) != null) {
 			Watchlist watchlist = (Watchlist)request.getSession(false).getAttribute(WikiUtil.PARAMETER_WATCHLIST);
 			if (watchlist != null) {
 				return watchlist;
 			}
 		}
 		// no watchlist in session, retrieve from database
 		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
 		Watchlist watchlist = new Watchlist();
 		if (userDetails.hasRole(Role.ROLE_ANONYMOUS)) {
 			return watchlist;
 		}
 		WikiUser user = ServletUtil.currentWikiUser();
 		try {
 			watchlist = WikiBase.getDataHandler().getWatchlist(virtualWiki, user.getUserId());
 		} catch (DataAccessException e) {
 			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 		}
 		if (request.getSession(false) != null) {
 			// add watchlist to session
 			request.getSession(false).setAttribute(WikiUtil.PARAMETER_WATCHLIST, watchlist);
 		}
 		return watchlist;
 	}
 
 	/**
 	 * Duplicate the functionality of the request.getRemoteAddr() method, but
 	 * for IPv6 addresses strip off any local interface information (anything
 	 * following a "%").
 	 *
 	 * @param request the HTTP request object.
 	 * @return The IP address that the request originated from, or 0.0.0.0 if
 	 *  the originating address cannot be determined.
 	 */
 	public static String getIpAddress(HttpServletRequest request) {
 		if (request == null) {
 			throw new IllegalArgumentException("Request object cannot be null");
 		}
 		String ipAddress = request.getRemoteAddr();
 		int pos = ipAddress.indexOf('%');
 		if (pos != -1) {
 			ipAddress = ipAddress.substring(0, pos);
 		}
 		if (!Utilities.isIpAddress(ipAddress)) {
 			logger.info("Invalid IP address found in request: " + ipAddress);
 			ipAddress = "0.0.0.0";
 		}
 		return ipAddress;
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
 	 * @throws WikiException Thrown if any error occurs while retrieving or
 	 *  initializing the topic object.
 	 */
 	protected static Topic initializeTopic(String virtualWiki, String topicName) throws WikiException {
 		WikiUtil.validateTopicName(virtualWiki, topicName);
 		Topic topic = null;
 		try {
 			topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		} catch (DataAccessException e) {
 			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 		}
 		if (topic != null) {
 			return topic;
 		}
 		topic = new Topic(virtualWiki, topicName);
 		WikiLink wikiLink = LinkUtil.parseWikiLink(virtualWiki, topicName);
 		topic.setTopicType(WikiUtil.findTopicTypeForNamespace(wikiLink.getNamespace()));
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
 	 * @throws WikiException Thrown if any error occurs during processing.
 	 */
 	protected static boolean isEditable(String virtualWiki, String topicName, WikiUserDetailsImpl user) throws WikiException {
 		if (user == null || !user.hasRole(Role.ROLE_EDIT_EXISTING)) {
 			// user does not have appropriate permissions
 			return false;
 		}
 		Topic topic = null;
 		try {
 			if (!user.hasRole(Role.ROLE_EDIT_NEW) && WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null) == null) {
 				// user does not have appropriate permissions
 				return false;
 			}
 			topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		} catch (DataAccessException e) {
 			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 		}
 		if (topic == null) {
 			// new topic, edit away...
 			return true;
 		}
 		if (topic.getAdminOnly() && !user.hasRole(Role.ROLE_ADMIN)) {
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
 	 * @throws WikiException Thrown if any error occurs during processing.
 	 */
 	protected static boolean isMoveable(String virtualWiki, String topicName, WikiUserDetailsImpl user) throws WikiException {
 		if (user == null || !user.hasRole(Role.ROLE_MOVE)) {
 			// no permission granted to move pages
 			return false;
 		}
 		Topic topic = null;
 		try {
 			topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		} catch (DataAccessException e) {
 			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 		}
 		if (topic == null) {
 			// cannot move a topic that doesn't exist
 			return false;
 		}
 		if (topic.getReadOnly()) {
 			return false;
 		}
 		if (topic.getAdminOnly() && !user.hasRole(Role.ROLE_ADMIN)) {
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
 		String topic = WikiUtil.getTopicFromURI(request);
 		if (StringUtils.isBlank(topic)) {
 			return false;
 		}
 		if (value != null &&  topic.equals(value)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Utility method for adding categories associated with the current topic
 	 * to the ModelAndView object.  This method adds a hashmap of category
 	 * names and sort keys to the session that can then be retrieved for
 	 * display during rendering.
 	 *
 	 * @param request The current servlet request object.
 	 * @param next The current ModelAndView object used to return rendering
 	 *  information.
 	 * @param virtualWiki The virtual wiki name for the topic being rendered.
 	 * @param topicName The name of the topic that is being rendered.
 	 * @throws WikiException Thrown if any error occurs during processing.
 	 */
 	protected static void loadCategoryContent(HttpServletRequest request, ModelAndView next, String virtualWiki, String topicName) throws WikiException {
 		String categoryName = topicName.substring(Namespace.namespace(Namespace.CATEGORY_ID).getLabel(virtualWiki).length() + Namespace.SEPARATOR.length());
 		next.addObject("categoryName", categoryName);
 		List<Category> categoryTopics = null;
 		try {
 			categoryTopics = WikiBase.getDataHandler().lookupCategoryTopics(virtualWiki, topicName);
 		} catch (DataAccessException e) {
 			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 		}
 		Pagination pagination = ServletUtil.loadPagination(request, next);
 		List<Category> categoryImages = new ArrayList<Category>();
 		LinkedHashMap<String, String> subCategories = new LinkedHashMap<String, String>();
 		int i = 0;
 		// loop through the results and split out images and sub-categories
 		while (i < categoryTopics.size()) {
 			Category category = categoryTopics.get(i);
 			if (category.getTopicType() == TopicType.IMAGE) {
 				categoryTopics.remove(i);
 				categoryImages.add(category);
 				continue;
 			}
 			if (category.getTopicType() == TopicType.CATEGORY) {
 				categoryTopics.remove(i);
 				String value = category.getChildTopicName().substring(Namespace.namespace(Namespace.CATEGORY_ID).getLabel(virtualWiki).length() + Namespace.SEPARATOR.length());
 				subCategories.put(category.getChildTopicName(), value);
 				continue;
 			}
 			i++;
 		}
 		// manually process pagination
 		List<Category> paginatedCategories = Pagination.retrievePaginatedSubset(pagination, categoryTopics);
 		next.addObject("categoryTopics", paginatedCategories);
 		next.addObject("numCategoryTopics", categoryTopics.size());
 		next.addObject("categoryImages", categoryImages);
 		next.addObject("numCategoryImages", categoryImages.size());
 		next.addObject("subCategories", subCategories);
 		next.addObject("numSubCategories", subCategories.size());
 		next.addObject("displayCategoryCount", paginatedCategories.size());
 	}
 
 	/**
 	 * Create a Pagination object and load all necessary values into the
 	 * request for processing by a JSP.
 	 *
 	 * @param request The servlet request object.
 	 * @param next A ModelAndView object corresponding to the page being
 	 *  constructed.
 	 * @return A Pagination object constructed from parameters found in the
 	 *  request object.
 	 */
 	public static Pagination loadPagination(HttpServletRequest request, ModelAndView next) {
 		if (next == null) {
 			throw new IllegalArgumentException("A non-null ModelAndView object must be specified when loading pagination values");
 		}
 		Pagination pagination = WikiUtil.buildPagination(request);
 		next.addObject("num", pagination.getNumResults());
 		next.addObject("offset", pagination.getOffset());
 		return pagination;
 	}
 
 	/**
 	 * Utility method for parsing a multipart servlet request.  This method returns
 	 * an iterator of FileItem objects that corresponds to the request.
 	 *
 	 * @param request The servlet request containing the multipart request.
 	 * @param uploadDirectory The directory into which files will be uploaded.
 	 * @param maxFileSize The maximum allowed file size in bytes.
 	 * @return Returns an iterator of FileItem objects the corresponds to the request.
 	 * @throws WikiException Thrown if any problems occur while processing the request.
 	 */
 	public static Iterator processMultipartRequest(HttpServletRequest request, String uploadDirectory, long maxFileSize) throws WikiException {
 		// Create a factory for disk-based file items
 		DiskFileItemFactory factory = new DiskFileItemFactory();
 		factory.setRepository(new File(uploadDirectory));
 		ServletFileUpload upload = new ServletFileUpload(factory);
 		upload.setHeaderEncoding("UTF-8");
 		upload.setSizeMax(maxFileSize);
 		try {
 			return upload.parseRequest(request).iterator();
 		} catch (FileUploadException e) {
 			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 		}
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
 	 * @throws WikiException Thrown if any error occurs while processing.
 	 */
 	protected static void redirect(ModelAndView next, String virtualWiki, String destination) throws WikiException {
 		String target = null;
 		try {
 			target = LinkUtil.buildTopicUrl(null, virtualWiki, destination, true);
 		} catch (DataAccessException e) {
 			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 		}
 		String view = ServletUtil.SPRING_REDIRECT_PREFIX + target;
 		next.clear();
 		next.setViewName(view);
 	}
 
 	/**
 	 * Generate a target URL for redirection after login if required by
 	 * Spring Security.
 	 */
 	private static String retrieveLoginTargetUrl(HttpServletRequest request, String virtualWikiName, String topic) {
 		String target = request.getParameter(JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGIN_TARGET_URL_FIELD_NAME);
 		if (!StringUtils.isBlank(target) && !target.startsWith("/")) {
 			// Link hijacking is possible with a link such as
 			// Special:Login?spring-security-redirect=http://www.google.com
 			String message = "Possible link hijacking attempt from " + ServletUtil.getIpAddress(request);
 			message += " / request URL: " + request.getRequestURL();
 			if (!StringUtils.isBlank(request.getQueryString())) {
 				message += "?" + Utilities.getQueryString(request);
 			}
 			logger.warn(message);
 		}
 		if (StringUtils.isBlank(target)) {
 			if (StringUtils.isBlank(topic)) {
 				VirtualWiki virtualWiki = ServletUtil.retrieveVirtualWiki(virtualWikiName);
 				topic = virtualWiki.getRootTopicName();
 			}
 			target = "/" + virtualWikiName + "/" + topic;
 			if (!StringUtils.isBlank(request.getQueryString())) {
 				target += "?" + Utilities.getQueryString(request);
 			}
 		}
 		return target;
 	}
 
 	/**
 	 * Users can specify a default locale in their preferences, so determine
 	 * if the current user is logged-in and has chosen a locale.  If not, use
 	 * the default locale from the request object.
 	 *
 	 * @param request The request object for the HTTP request.
 	 * @return Either the user's default locale (for logged-in users) or the
 	 *  locale specified in the request if no default locale is available.
 	 */
 	public static Locale retrieveUserLocale(HttpServletRequest request) {
 		try {
 			WikiUser user = ServletUtil.currentWikiUser();
 			if (user.getDefaultLocale() != null) {
 				return LocaleUtils.toLocale(user.getDefaultLocale());
 			}
 		} catch (AuthenticationCredentialsNotFoundException e) {
 			// ignore
 		}
 		return request.getLocale();
 	}
 
 	/**
 	 * Given a virtual wiki name, return a <code>VirtualWiki</code> object.
 	 * If there is no virtual wiki available with the given name then the
 	 * default virtual wiki is returned.
 	 *
 	 * @param virtualWikiName The name of the virtual wiki that is being
 	 *  retrieved.
 	 * @return A <code>VirtualWiki</code> object.  If there is no virtual
 	 *  wiki available with the given name then the default virtual wiki is
 	 *  returned.
 	 */
 	public static VirtualWiki retrieveVirtualWiki(String virtualWikiName) {
 		VirtualWiki virtualWiki = null;
 		if (virtualWikiName == null) {
 			virtualWikiName = VirtualWiki.defaultVirtualWiki().getName();
 		}
 		// FIXME - the check here for initialized properties is due to this
 		// change being made late in a release cycle.  Revisit in a future
 		// release & clean this up.
 		if (Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED)) {
 			try {
 				virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(virtualWikiName);
 			} catch (DataAccessException e) {}
 		}
 		if (virtualWiki == null) {
 			logger.error("No virtual wiki found for " + virtualWikiName);
 			virtualWiki = VirtualWiki.defaultVirtualWiki();
 		}
 		return virtualWiki;
 	}
 
 	/**
 	 * Validate that vital system properties, such as database connection settings,
 	 * have been specified properly.
 	 *
 	 * @param props The property object to validate against.
 	 * @return A list of WikiMessage objects containing any errors encountered,
 	 *  or an empty list if no errors are encountered.
 	 */
 	protected static List<WikiMessage> validateSystemSettings(Properties props) {
 		List<WikiMessage> errors = new ArrayList<WikiMessage>();
 		// test directory permissions & existence
 		WikiMessage baseDirError = WikiUtil.validateDirectory(props.getProperty(Environment.PROP_BASE_FILE_DIR));
 		if (baseDirError != null) {
 			errors.add(baseDirError);
 		}
 		WikiMessage fullDirError = WikiUtil.validateDirectory(props.getProperty(Environment.PROP_FILE_DIR_FULL_PATH));
 		if (fullDirError != null) {
 			errors.add(fullDirError);
 		}
 		String classesDir = null;
 		try {
 			classesDir = Utilities.getClassLoaderRoot().getPath();
 			WikiMessage classesDirError = WikiUtil.validateDirectory(classesDir);
 			if (classesDirError != null) {
 				errors.add(classesDirError);
 			}
 		} catch (FileNotFoundException e) {
 			errors.add(new WikiMessage("error.directorywrite", classesDir, e.getMessage()));
 		}
 		// test database
 		String driver = props.getProperty(Environment.PROP_DB_DRIVER);
 		String url = props.getProperty(Environment.PROP_DB_URL);
 		String userName = props.getProperty(Environment.PROP_DB_USERNAME);
 		String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, props);
 		try {
 			DatabaseConnection.testDatabase(driver, url, userName, password, false);
 		} catch (ClassNotFoundException e) {
 			logger.error("Invalid database settings", e);
 			errors.add(new WikiMessage("error.databaseconnection", e.getMessage()));
 		} catch (SQLException e) {
 			logger.error("Invalid database settings", e);
 			errors.add(new WikiMessage("error.databaseconnection", e.getMessage()));
 		}
 		// verify valid parser class
 		String parserClass = props.getProperty(Environment.PROP_PARSER_CLASS);
 		String abstractParserClass = "org.jamwiki.parser.AbstractParser";
 		boolean validParser = (parserClass != null && !parserClass.equals(abstractParserClass));
 		if (validParser) {
 			try {
 				Class parent = ClassUtils.getClass(parserClass);
 				Class child = ClassUtils.getClass(abstractParserClass);
 				if (!child.isAssignableFrom(parent)) {
 					validParser = false;
 				}
 			} catch (ClassNotFoundException e) {
 				validParser = false;
 			}
 		}
 		if (!validParser) {
 			errors.add(new WikiMessage("error.parserclass", parserClass));
 		}
 		return errors;
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
 	 * @throws WikiException Thrown if any error occurs during processing.
 	 */
 	protected static ModelAndView viewLogin(HttpServletRequest request, WikiPageInfo pageInfo, String topic, WikiMessage messageObject) throws WikiException {
 		ModelAndView next = new ModelAndView("wiki");
 		pageInfo.reset();
 		String virtualWikiName = pageInfo.getVirtualWikiName();
 		next.addObject("springSecurityTargetUrlField", JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGIN_TARGET_URL_FIELD_NAME);
 		HttpSession session = request.getSession(false);
 		if (request.getRequestURL().indexOf(request.getRequestURI()) != -1 && (session == null || session.getAttribute(JAMWikiAuthenticationConstants.SPRING_SECURITY_SAVED_REQUEST_SESSION_KEY) == null)) {
 			// Only add a target URL if Spring Security has not saved a request in the session.  The request
 			// URL vs URI check is needed due to the fact that the first time a user is redirected by Spring
 			// Security to the login page the saved request attribute is not yet available in the session
 			// due to weirdness and magic which I've thus far been unable to track down, so comparing the URI
 			// to the URL provides a way of determining if the user was redirected.  Anyone who can create
 			// a check that reliably captures whether or not Spring Security has a saved request should
 			// feel free to modify the conditional above.
 			String target = ServletUtil.retrieveLoginTargetUrl(request, virtualWikiName, topic);
 			if (target != null) {
 				next.addObject("springSecurityTargetUrl", target);
 			}
 		}
 		String springSecurityLoginUrl = "/" + virtualWikiName + JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGIN_URL;
 		next.addObject("springSecurityLoginUrl", springSecurityLoginUrl);
 		next.addObject("springSecurityUsernameField", JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGIN_USERNAME_FIELD_NAME);
 		next.addObject("springSecurityPasswordField", JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGIN_PASSWORD_FIELD_NAME);
 		next.addObject("springSecurityRememberMeField", JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGIN_REMEMBER_ME_FIELD_NAME);
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
 	 * @param pageTitle A WikiMessage for the title of the page being rendered.  The
 	 *  first parameter of the message should be the topic name.
 	 * @param topic The Topic object for the topic being displayed.
 	 * @param sectionEdit Set to <code>true</code> if edit links should be displayed
 	 *  for each section of the topic.
 	 * @param allowRedirect Setting this parameter to <code>true</code> will force the
 	 *  redirection target to be displayed (rather than a redirect page) if the topic is a
 	 *  redirect.
 	 * @throws WikiException Thrown if any error occurs while retrieving or parsing the topic.
 	 */
 	protected static void viewTopic(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, WikiMessage pageTitle, Topic topic, boolean sectionEdit, boolean allowRedirect) throws WikiException {
 		// FIXME - what should the default be for topics that don't exist?
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		WikiUtil.validateTopicName(topic.getVirtualWiki(), topic.getName());
 		if (allowRedirect && topic.getTopicType() == TopicType.REDIRECT && (request.getParameter("redirect") == null || !request.getParameter("redirect").equalsIgnoreCase("no"))) {
 			Topic child = null;
 			try {
 				child = WikiUtil.findRedirectedTopic(topic, 0);
 			} catch (DataAccessException e) {
 				throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 			}
 			if (!child.getName().equals(topic.getName())) {
 				String redirectUrl = null;
 				try {
 					redirectUrl = LinkUtil.buildTopicUrl(request.getContextPath(), topic.getVirtualWiki(), topic.getName(), true);
 				} catch (DataAccessException e) {
 					throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 				}
 				// FIXME - hard coding
 				redirectUrl += LinkUtil.appendQueryParam("", "redirect", "no");
 				String redirectName = topic.getName();
 				pageInfo.setRedirectInfo(redirectUrl, redirectName);
 				pageTitle.replaceParameter(0, child.getName());
 				topic = child;
 				try {
 					pageInfo.setCanonicalUrl(LinkUtil.buildTopicUrl(request.getContextPath(), topic.getVirtualWiki(), topic.getName(), false));
 				} catch (DataAccessException e) {
 					throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 				}
 				// update the page info's virtual wiki in case this redirect is to another virtual wiki
 				pageInfo.setVirtualWikiName(topic.getVirtualWiki());
 			}
 		}
 		String virtualWiki = topic.getVirtualWiki();
 		String topicName = topic.getName();
 		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
 		if (sectionEdit && !ServletUtil.isEditable(virtualWiki, topicName, userDetails)) {
 			sectionEdit = false;
 		}
 		WikiUser user = ServletUtil.currentWikiUser();
 		ParserInput parserInput = new ParserInput(virtualWiki, topicName);
 		parserInput.setContext(request.getContextPath());
 		parserInput.setLocale(request.getLocale());
 		parserInput.setWikiUser(user);
 		parserInput.setUserDisplay(ServletUtil.getIpAddress(request));
 		parserInput.setAllowSectionEdit(sectionEdit);
 		ParserOutput parserOutput = new ParserOutput();
 		String content = null;
 		try {
 			content = ParserUtil.parse(parserInput, parserOutput, topic.getTopicContent());
 		} catch (ParserException e) {
 			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 		}
 		if (parserOutput.getCategories().size() > 0) {
 			LinkedHashMap<String, String> categories = new LinkedHashMap<String, String>();
 			for (String key : parserOutput.getCategories().keySet()) {
 				String value = key.substring(Namespace.namespace(Namespace.CATEGORY_ID).getLabel(virtualWiki).length() + Namespace.SEPARATOR.length());
 				categories.put(key, value);
 			}
 			next.addObject("categories", categories);
 		}
 		topic.setTopicContent(content);
 		if (topic.getTopicType() == TopicType.CATEGORY) {
 			loadCategoryContent(request, next, virtualWiki, topic.getName());
 		}
 		next.addObject("interwikiLinks", parserOutput.getInterwikiLinks());
 		next.addObject("virtualWikiLinks", parserOutput.getVirtualWikiLinks());
 		if (topic.getTopicType() == TopicType.IMAGE || topic.getTopicType() == TopicType.FILE) {
 			WikiFile wikiFile = null;
 			List<WikiFileVersion> fileVersions = null;
 			try {
 				wikiFile = WikiBase.getDataHandler().lookupWikiFile(topic.getVirtualWiki(), topicName);
 				fileVersions = WikiBase.getDataHandler().getAllWikiFileVersions(topic.getVirtualWiki(), topicName, true);
 			} catch (DataAccessException e) {
 				throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 			}
 			WikiUser wikiUser;
 			for (WikiFileVersion fileVersion : fileVersions) {
 				// update version urls to include web root path
 				String url = FilenameUtils.normalize(Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) + "/" + fileVersion.getUrl());
 				url = FilenameUtils.separatorsToUnix(url);
 				fileVersion.setUrl(url);
 				// make sure the authorDisplay field is equal to the login for non-anonymous uploads
 				if (fileVersion.getAuthorId() != null) {
 					try {
 						wikiUser = WikiBase.getDataHandler().lookupWikiUser(fileVersion.getAuthorId());
 					} catch (DataAccessException e) {
 						throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 					}
 					if (wikiUser != null) {
 						// wikiUser should never be null unless the data in the database is somehow corrupt
 						fileVersion.setAuthorDisplay(wikiUser.getUsername());
 					}
 				}
 			}
 			next.addObject("fileVersions", fileVersions);
 			if (topic.getTopicType() == TopicType.IMAGE) {
 				next.addObject("topicImage", true);
 			} else {
 				next.addObject("topicFile", true);
 			}
 			// use the WikiFile virtual wiki rather than the topic to work around
 			// a corner case where there could be an image page topic created for
 			// the virtual wiki even though the actual image file is only on the
 			// shared virtual wiki.
 			boolean sharedImage = !pageInfo.getVirtualWikiName().equals(wikiFile.getVirtualWiki());
 			if (sharedImage) {
 				try {
 					Topic sharedImageTopic = topic;
 					if (!StringUtils.equals(wikiFile.getVirtualWiki(), topic.getVirtualWiki())) {
 						// look up the shared topic file
 						sharedImageTopic = WikiBase.getDataHandler().lookupTopicById(wikiFile.getVirtualWiki(), wikiFile.getTopicId());
 					}
 					pageInfo.setCanonicalUrl(LinkUtil.buildTopicUrl(request.getContextPath(), sharedImageTopic.getVirtualWiki(), sharedImageTopic.getName(), false));
 					next.addObject("sharedImageTopicObject", sharedImageTopic);
 				} catch (DataAccessException e) {
 					throw new WikiException(new WikiMessage("error.unknown", e.getMessage()), e);
 				}
 			}
 		}
 		pageInfo.setSpecial(false);
 		pageInfo.setTopicName(topicName);
 		next.addObject(ServletUtil.PARAMETER_TOPIC_OBJECT, topic);
 		if (pageTitle != null) {
 			if (parserOutput.getPageTitle() != null) {
 				pageTitle.replaceParameter(0, parserOutput.getPageTitle());
 			}
 			pageInfo.setPageTitle(pageTitle);
 		}
 	}
 }
