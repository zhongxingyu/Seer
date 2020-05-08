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
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiConfiguration;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.authentication.JAMWikiAuthenticationConfiguration;
 import org.jamwiki.authentication.WikiUserDetails;
 import org.jamwiki.db.WikiDatabase;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiConfigurationObject;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.SpamFilter;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiCache;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to provide administrative functions including changing Wiki
  * configuration settings and refreshing internal Wiki objects.
  */
 public class AdminServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(AdminServlet.class.getName());
 	/** The name of the JSP file used to render the servlet output for the admin maintenance configuration. */
 	protected static final String JSP_ADMIN = "admin.jsp";
 	/** The name of the JSP file used to render the servlet output for the admin maintenance functionality. */
 	protected static final String JSP_ADMIN_SYSTEM = "admin-maintenance.jsp";
 
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
 		if (StringUtils.isBlank(function) && ServletUtil.isTopic(request, "Special:Maintenance")) {
 			viewAdminSystem(request, next, pageInfo);
 		} else if (StringUtils.isBlank(function)) {
 			viewAdmin(request, next, pageInfo, null);
 		} else if (function.equals("cache")) {
 			cache(request, next, pageInfo);
 		} else if (function.equals("search")) {
 			refreshIndex(request, next, pageInfo);
 		} else if (function.equals("properties")) {
 			properties(request, next, pageInfo);
 		} else if (function.equals("virtualwiki")) {
 			virtualWiki(request, next, pageInfo);
 		} else if (function.equals("logitems")) {
 			logItems(request, next, pageInfo);
 		} else if (function.equals("recentchanges")) {
 			recentChanges(request, next, pageInfo);
 		} else if (function.equals("spam")) {
 			spam(request, next, pageInfo);
 		} else if (function.equals("export")) {
 			exportToCsv(request, next, pageInfo);
 		} else if (function.equals("migrate")) {
 			migrateDatabase(request, next, pageInfo);
 		} else if (function.equals("password")) {
 			password(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void cache(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		try {
 			WikiCache.initialize();
 			next.addObject("message", new WikiMessage("admin.message.cache"));
 		} catch (Exception e) {
 			logger.severe("Failure while clearing cache", e);
 			next.addObject("message", new WikiMessage("admin.cache.message.clearfailed", e.getMessage()));
 		}
 		viewAdminSystem(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void exportToCsv(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		try {
 			WikiDatabase.exportToCsv();
 			String outputDirectory = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), "database").getPath();
 			next.addObject("message", new WikiMessage("admin.message.exportcsv", outputDirectory));
 		} catch (Exception e) {
 			logger.severe("Failure while exporting database data to CSV file", e);
 			next.addObject("message", new WikiMessage("admin.message.exportcsvfail", e.getMessage()));
 		}
 		viewAdminSystem(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void logItems(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		try {
 			WikiBase.getDataHandler().reloadLogItems();
 			next.addObject("message", new WikiMessage("admin.message.logitems"));
 		} catch (Exception e) {
 			logger.severe("Failure while loading log items", e);
 			next.addObject("message", new WikiMessage("admin.message.logitemsfail", e.getMessage()));
 		}
 		viewAdminSystem(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void migrateDatabase(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		List<WikiMessage> errors = new ArrayList<WikiMessage>();
 		Properties props = new Properties();
 		try {
 			setProperty(props, request, Environment.PROP_BASE_PERSISTENCE_TYPE);
 			if (props.getProperty(Environment.PROP_BASE_PERSISTENCE_TYPE).equals(WikiBase.PERSISTENCE_EXTERNAL)) {
 				setProperty(props, request, Environment.PROP_DB_DRIVER);
 				setProperty(props, request, Environment.PROP_DB_TYPE);
 				setProperty(props, request, Environment.PROP_DB_URL);
 				setProperty(props, request, Environment.PROP_DB_USERNAME);
 				setPassword(props, request, next, Environment.PROP_DB_PASSWORD, "dbPassword");
 			} else {
 				// reverting from external database to an internal database
 				props.setProperty(Environment.PROP_BASE_FILE_DIR, Environment.getValue(Environment.PROP_BASE_FILE_DIR));
 				WikiDatabase.setupDefaultDatabase(props);
 			}
 			// migrate from the current database to the new database
 			// identified by the properties
 			// Will return errors if the new database cannot be connected to,
 			// if it is already populated, or an error occurs copying the contents
 			WikiDatabase.migrateDatabase(props, errors);
 			if (this.saveProperties(request, next, pageInfo, props, errors)) {
 				next.addObject("message", new WikiMessage("admin.message.migratedatabase", Environment.getValue(Environment.PROP_DB_URL)));
 			}
 		} catch (Exception e) {
 			logger.severe("Failure while migrating to a new database", e);
 			next.addObject("message", new WikiMessage("admin.message.migrationfailure", e.getMessage()));
 		}
 		viewAdminSystem(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void password(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		List<WikiMessage> errors = new ArrayList<WikiMessage>();
 		String userLogin = request.getParameter("passwordLogin");
 		String newPassword = request.getParameter("passwordPassword");
 		String confirmPassword = request.getParameter("passwordPasswordConfirm");
 		try {
 			WikiUser user = WikiBase.getDataHandler().lookupWikiUser(userLogin);
 			if (user == null) {
 				throw new WikiException(new WikiMessage("admin.password.message.invalidlogin", userLogin));
 			}
 			WikiUtil.validatePassword(newPassword, confirmPassword);
 			String encryptedPassword = Encryption.encrypt(newPassword);
 			WikiBase.getDataHandler().writeWikiUser(user, userLogin, encryptedPassword);
 		} catch (WikiException e) {
 			errors.add(e.getWikiMessage());
 		} catch (Exception e) {
 			logger.severe("Failure while updating user password", e);
 			errors.add(new WikiMessage("admin.message.exportcsvfail", e.getMessage()));
 		}
 		if (!errors.isEmpty()) {
 			next.addObject("errors", errors);
 			next.addObject("passwordLogin", userLogin);
 			next.addObject("passwordPassword", newPassword);
 			next.addObject("passwordPasswordConfirm", confirmPassword);
 		} else {
 			next.addObject("message", new WikiMessage("admin.password.message.success", userLogin));
 		}
 		viewAdminSystem(request, next, pageInfo);
 	}
 
 	private void properties(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		Properties props = new Properties();
 		try {
 			List<WikiMessage> errors = new ArrayList<WikiMessage>();
 			setProperty(props, request, Environment.PROP_SERVER_URL);
 			setProperty(props, request, Environment.PROP_SITE_NAME);
 			setProperty(props, request, Environment.PROP_BASE_DEFAULT_TOPIC);
 			setProperty(props, request, Environment.PROP_BASE_LOGO_IMAGE);
 			setProperty(props, request, Environment.PROP_BASE_META_DESCRIPTION);
 			setProperty(props, request, Environment.PROP_TOPIC_EDITOR);
 			setNumericProperty(props, request, Environment.PROP_IMAGE_RESIZE_INCREMENT, errors);
 			setNumericProperty(props, request, Environment.PROP_MAX_TOPIC_VERSION_EXPORT, errors);
 			setNumericProperty(props, request, Environment.PROP_RECENT_CHANGES_NUM, errors);
 			setBooleanProperty(props, request, Environment.PROP_TOPIC_SPAM_FILTER);
 			setBooleanProperty(props, request, Environment.PROP_TOPIC_USE_PREVIEW);
 			setBooleanProperty(props, request, Environment.PROP_TOPIC_USE_SHOW_CHANGES);
 			setBooleanProperty(props, request, Environment.PROP_PRINT_NEW_WINDOW);
 			setBooleanProperty(props, request, Environment.PROP_EXTERNAL_LINK_NEW_WINDOW);
 			setProperty(props, request, Environment.PROP_BASE_SEARCH_ENGINE);
 			setProperty(props, request, Environment.PROP_PARSER_CLASS);
 			setBooleanProperty(props, request, Environment.PROP_PARSER_TOC);
 			setNumericProperty(props, request, Environment.PROP_PARSER_TOC_DEPTH, errors);
 			setBooleanProperty(props, request, Environment.PROP_PARSER_ALLOW_HTML);
 			setBooleanProperty(props, request, Environment.PROP_PARSER_ALLOW_JAVASCRIPT);
 			setBooleanProperty(props, request, Environment.PROP_PARSER_ALLOW_TEMPLATES);
 			setProperty(props, request, Environment.PROP_PARSER_SIGNATURE_USER_PATTERN);
 			setProperty(props, request, Environment.PROP_PARSER_SIGNATURE_DATE_PATTERN);
 			setProperty(props, request, Environment.PROP_BASE_FILE_DIR);
 			setProperty(props, request, Environment.PROP_BASE_PERSISTENCE_TYPE);
 			if (props.getProperty(Environment.PROP_BASE_PERSISTENCE_TYPE).equals(WikiBase.PERSISTENCE_EXTERNAL)) {
 				setProperty(props, request, Environment.PROP_DB_DRIVER);
 				setProperty(props, request, Environment.PROP_DB_TYPE);
 				setProperty(props, request, Environment.PROP_DB_URL);
 				setProperty(props, request, Environment.PROP_DB_USERNAME);
 				setPassword(props, request, next, Environment.PROP_DB_PASSWORD, "dbPassword");
 			} else {
 				WikiDatabase.setupDefaultDatabase(props);
 			}
 			setNumericProperty(props, request, Environment.PROP_DBCP_MAX_ACTIVE, errors);
 			setNumericProperty(props, request, Environment.PROP_DBCP_MAX_IDLE, errors);
 			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_ON_BORROW);
 			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_ON_RETURN);
 			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_WHILE_IDLE);
 			setNumericProperty(props, request, Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME, errors);
 			setNumericProperty(props, request, Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS, errors);
 			setNumericProperty(props, request, Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN, errors);
 			setProperty(props, request, Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION);
 			String maxFileSizeString = request.getParameter(Environment.PROP_FILE_MAX_FILE_SIZE);
 			if (StringUtils.isBlank(maxFileSizeString) || !StringUtils.isNumeric(maxFileSizeString)) {
 				errors.add(new WikiMessage("admin.message.nonnumeric", Environment.PROP_FILE_MAX_FILE_SIZE, maxFileSizeString));
 			} else {
 				int maxFileSizeInKB = Integer.parseInt(maxFileSizeString);
 				props.setProperty(Environment.PROP_FILE_MAX_FILE_SIZE, Integer.toString(maxFileSizeInKB * 1000));
 			}
 			setProperty(props, request, Environment.PROP_FILE_DIR_FULL_PATH);
 			setProperty(props, request, Environment.PROP_FILE_DIR_RELATIVE_PATH);
 			setProperty(props, request, Environment.PROP_FILE_SERVER_URL);
 			setProperty(props, request, Environment.PROP_FILE_BLACKLIST_TYPE);
 			setProperty(props, request, Environment.PROP_FILE_BLACKLIST);
 			setProperty(props, request, Environment.PROP_FILE_WHITELIST);
 			/*
 			setProperty(props, request, Environment.PROP_EMAIL_SMTP_HOST);
 			setProperty(props, request, Environment.PROP_EMAIL_SMTP_USERNAME);
 			setPassword(props, request, next, Environment.PROP_EMAIL_SMTP_PASSWORD, "smtpPassword");
 			setProperty(props, request, Environment.PROP_EMAIL_REPLY_ADDRESS);
 			*/
 			setNumericProperty(props, request, Environment.PROP_CACHE_INDIVIDUAL_SIZE, errors);
 			setNumericProperty(props, request, Environment.PROP_CACHE_MAX_AGE, errors);
 			setNumericProperty(props, request, Environment.PROP_CACHE_MAX_IDLE_AGE, errors);
 			setNumericProperty(props, request, Environment.PROP_CACHE_TOTAL_SIZE, errors);
 			setBooleanProperty(props, request, Environment.PROP_RSS_ALLOWED);
 			setProperty(props, request, Environment.PROP_RSS_TITLE);
 			errors.addAll(ServletUtil.validateSystemSettings(props));
 			if (this.saveProperties(request, next, pageInfo, props, errors)) {
 				next.addObject("message", new WikiMessage("admin.message.changessaved"));
 			}
 		} catch (Exception e) {
 			logger.severe("Failure while processing property values", e);
 			next.addObject("message", new WikiMessage("admin.message.propertyfailure", e.getMessage()));
 		}
 		viewAdmin(request, next, pageInfo, props);
 	}
 
 	/**
 	 *
 	 */
 	private void recentChanges(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		try {
 			WikiBase.getDataHandler().reloadRecentChanges();
 			next.addObject("message", new WikiMessage("admin.message.recentchanges"));
 		} catch (Exception e) {
 			logger.severe("Failure while loading recent changes", e);
 			next.addObject("message", new WikiMessage("admin.message.recentchangesfail", e.getMessage()));
 		}
 		viewAdminSystem(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void refreshIndex(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		try {
 			WikiBase.getSearchEngine().refreshIndex();
 			next.addObject("message", new WikiMessage("admin.message.indexrefreshed"));
 		} catch (Exception e) {
 			logger.severe("Failure while refreshing search index", e);
 			next.addObject("message", new WikiMessage("admin.message.searchrefresh", e.getMessage()));
 		}
 		viewAdminSystem(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private boolean saveProperties(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, Properties props, List<WikiMessage> errors) throws Exception {
 		if (!errors.isEmpty()) {
 			next.addObject("errors", errors);
 			next.addObject("message", new WikiMessage("admin.message.changesnotsaved"));
 			return false;
 		}
 		// all is well, save the properties
 		Iterator iterator = props.keySet().iterator();
 		while (iterator.hasNext()) {
 			String key = (String)iterator.next();
 			String value = props.getProperty(key);
 			Environment.setValue(key, value);
 		}
 		Environment.saveProperties();
 		// re-initialize to reset database settings (if needed)
 		WikiUserDetails userDetails = ServletUtil.currentUserDetails();
 		if (userDetails.hasRole(Role.ROLE_ANONYMOUS)) {
 			throw new IllegalArgumentException("Cannot pass null or anonymous WikiUser object to setupAdminUser");
 		}
 		WikiUser user = ServletUtil.currentWikiUser();
 		WikiBase.reset(request.getLocale(), user, user.getUsername(), null);
 		JAMWikiAuthenticationConfiguration.resetJamwikiAnonymousAuthorities();
 		JAMWikiAuthenticationConfiguration.resetDefaultGroupRoles();
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	private static void setBooleanProperty(Properties props, HttpServletRequest request, String parameter) {
 		boolean value = (request.getParameter(parameter) != null);
 		props.setProperty(parameter, Boolean.toString(value));
 	}
 
 	/**
 	 *
 	 */
 	private static void setPassword(Properties props, HttpServletRequest request, ModelAndView next, String parameter, String passwordParam) throws Exception {
 		String value = request.getParameter(parameter);
 		if (!StringUtils.isBlank(value)) {
 			Encryption.setEncryptedProperty(parameter, value, props);
 			next.addObject(passwordParam, request.getParameter(parameter));
 		} else {
 			props.setProperty(parameter, Environment.getValue(parameter));
 		}
 	}
 
 	/**
 	 *
 	 */
 	private static void setNumericProperty(Properties props, HttpServletRequest request, String parameter, List<WikiMessage> errors) {
 		String value = request.getParameter(parameter);
 		if (StringUtils.isBlank(value) || !StringUtils.isNumeric(value)) {
 			errors.add(new WikiMessage("admin.message.nonnumeric", parameter, value));
 		}
 		props.setProperty(parameter, value);
 	}
 
 	/**
 	 *
 	 */
 	private static void setProperty(Properties props, HttpServletRequest request, String parameter) {
 		String value = request.getParameter(parameter);
 		if (value == null) {
 			value = "";
 		}
 		props.setProperty(parameter, value);
 	}
 
 	/**
 	 *
 	 */
 	private void spam(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		try {
 			SpamFilter.reload();
 			next.addObject("message", new WikiMessage("admin.message.spamfilter"));
 		} catch (Exception e) {
 			logger.severe("Failure while reloading spam filter patterns", e);
 			next.addObject("message", new WikiMessage("admin.message.spamfilterfail", e.getMessage()));
 		}
 		viewAdminSystem(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void viewAdmin(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, Properties props) throws Exception {
 		pageInfo.setContentJsp(JSP_ADMIN);
 		pageInfo.setAdmin(true);
 		pageInfo.setPageTitle(new WikiMessage("admin.title"));
 		Map editors = WikiConfiguration.getInstance().getEditors();
 		next.addObject("editors", editors);
 		List<WikiConfigurationObject> dataHandlers = WikiConfiguration.getInstance().getDataHandlers();
 		next.addObject("dataHandlers", dataHandlers);
 		List<WikiConfigurationObject> searchEngines = WikiConfiguration.getInstance().getSearchEngines();
 		next.addObject("searchEngines", searchEngines);
 		List<WikiConfigurationObject> parsers = WikiConfiguration.getInstance().getParsers();
 		next.addObject("parsers", parsers);
 		LinkedHashMap<Integer, String> poolExhaustedMap = new LinkedHashMap<Integer, String>();
 		poolExhaustedMap.put(Integer.valueOf(GenericObjectPool.WHEN_EXHAUSTED_FAIL), "admin.persistence.caption.whenexhaustedaction.fail");
 		poolExhaustedMap.put(Integer.valueOf(GenericObjectPool.WHEN_EXHAUSTED_BLOCK), "admin.persistence.caption.whenexhaustedaction.block");
 		poolExhaustedMap.put(Integer.valueOf(GenericObjectPool.WHEN_EXHAUSTED_GROW), "admin.persistence.caption.whenexhaustedaction.grow");
 		next.addObject("poolExhaustedMap", poolExhaustedMap);
 		LinkedHashMap<Integer, String> blacklistTypesMap = new LinkedHashMap<Integer, String>();
 		blacklistTypesMap.put(Integer.valueOf(WikiBase.UPLOAD_ALL), "admin.upload.caption.allowall");
 		blacklistTypesMap.put(Integer.valueOf(WikiBase.UPLOAD_NONE), "admin.upload.caption.allownone");
 		blacklistTypesMap.put(Integer.valueOf(WikiBase.UPLOAD_BLACKLIST), "admin.upload.caption.useblacklist");
 		blacklistTypesMap.put(Integer.valueOf(WikiBase.UPLOAD_WHITELIST), "admin.upload.caption.usewhitelist");
 		next.addObject("blacklistTypes", blacklistTypesMap);
 		if (props == null) {
 			props = Environment.getInstance();
 		}
 		int maximumFileSize = Integer.valueOf(props.getProperty(Environment.PROP_FILE_MAX_FILE_SIZE))/1000;
 		next.addObject("maximumFileSize", maximumFileSize);
 		next.addObject("props", props);
 	}
 
 	/**
 	 *
 	 */
 	private void viewAdminSystem(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		pageInfo.setContentJsp(JSP_ADMIN_SYSTEM);
 		pageInfo.setAdmin(true);
 		pageInfo.setPageTitle(new WikiMessage("admin.maintenance.title"));
 		List<VirtualWiki> virtualWikiList = WikiBase.getDataHandler().getVirtualWikiList();
 		next.addObject("wikis", virtualWikiList);
 		boolean allowExport = Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE).equals(WikiBase.PERSISTENCE_INTERNAL);
 		next.addObject("allowExport", allowExport);
 		List<WikiConfigurationObject> dataHandlers = WikiConfiguration.getInstance().getDataHandlers();
 		next.addObject("dataHandlers", dataHandlers);
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
			virtualWiki.setDefaultTopicName(Utilities.decodeTopicName(request.getParameter("defaultTopicName"), true));
 			WikiBase.getDataHandler().writeVirtualWiki(virtualWiki);
 			if (StringUtils.isBlank(request.getParameter("virtualWikiId"))) {
 				WikiBase.getDataHandler().setupSpecialPages(request.getLocale(), user, virtualWiki);
 			}
 			next.addObject("message", new WikiMessage("admin.message.virtualwikiadded"));
 		} catch (Exception e) {
 			logger.severe("Failure while adding virtual wiki", e);
 			next.addObject("message", new WikiMessage("admin.message.virtualwikifail", e.getMessage()));
 		}
 		viewAdminSystem(request, next, pageInfo);
 	}
 }
