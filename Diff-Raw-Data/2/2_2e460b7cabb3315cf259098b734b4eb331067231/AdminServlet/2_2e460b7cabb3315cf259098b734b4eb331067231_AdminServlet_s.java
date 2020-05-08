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
 import java.util.Properties;
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiConfiguration;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.db.WikiDatabase;
 import org.jamwiki.model.VirtualWiki;
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
 	protected static final String JSP_ADMIN = "admin.jsp";
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
		if (StringUtils.isBlank(function) && !ServletUtil.isTopic(request, "Special:Maintenance")) {
 			viewAdminSystem(request, next, pageInfo);
 		} else if (StringUtils.isBlank(function)) {
 			viewAdmin(request, next, pageInfo, null);
 		} else if (function.equals("cache")) {
 			cache(request, next, pageInfo);
 		} else if (function.equals("refreshIndex")) {
 			refreshIndex(request, next, pageInfo);
 		} else if (function.equals("properties")) {
 			properties(request, next, pageInfo);
 		} else if (function.equals("addVirtualWiki")) {
 			addVirtualWiki(request, next, pageInfo);
 		} else if (function.equals("recentChanges")) {
 			recentChanges(request, next, pageInfo);
 		} else if (function.equals("spamFilter")) {
 			spamFilter(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void addVirtualWiki(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		WikiUser user = WikiUtil.currentUser();
 		try {
 			VirtualWiki virtualWiki = new VirtualWiki();
 			if (!StringUtils.isBlank(request.getParameter("virtualWikiId"))) {
 				virtualWiki.setVirtualWikiId(new Integer(request.getParameter("virtualWikiId")).intValue());
 			}
 			virtualWiki.setName(request.getParameter("name"));
 			virtualWiki.setDefaultTopicName(Utilities.encodeForURL(request.getParameter("defaultTopicName")));
 			WikiBase.getDataHandler().writeVirtualWiki(virtualWiki, null);
 			if (StringUtils.isBlank(request.getParameter("virtualWikiId"))) {
 				WikiBase.getDataHandler().setupSpecialPages(request.getLocale(), user, virtualWiki, null);
 			}
 			next.addObject("message", new WikiMessage("admin.message.virtualwikiadded"));
 		} catch (Exception e) {
 			logger.severe("Failure while adding virtual wiki", e);
 			next.addObject("message", new WikiMessage("admin.message.virtualwikifail", e.getMessage()));
 		}
 		viewAdminSystem(request, next, pageInfo);
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
 			next.addObject("errors", new WikiMessage("admin.cache.message.clearfailed", e.getMessage()));
 		}
 		viewAdminSystem(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void properties(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		Properties props = new Properties();
 		try {
 			setProperty(props, request, Environment.PROP_BASE_DEFAULT_TOPIC);
 			setProperty(props, request, Environment.PROP_BASE_LOGO_IMAGE);
 			setProperty(props, request, Environment.PROP_BASE_META_DESCRIPTION);
 			setBooleanProperty(props, request, Environment.PROP_TOPIC_WYSIWYG);
 			setProperty(props, request, Environment.PROP_IMAGE_RESIZE_INCREMENT);
 			setProperty(props, request, Environment.PROP_RECENT_CHANGES_NUM);
 			setBooleanProperty(props, request, Environment.PROP_TOPIC_SPAM_FILTER);
 			setBooleanProperty(props, request, Environment.PROP_TOPIC_USE_PREVIEW);
 			setBooleanProperty(props, request, Environment.PROP_PRINT_NEW_WINDOW);
 			setBooleanProperty(props, request, Environment.PROP_EXTERNAL_LINK_NEW_WINDOW);
 			setProperty(props, request, Environment.PROP_BASE_SEARCH_ENGINE);
 			setProperty(props, request, Environment.PROP_PARSER_CLASS);
 			setBooleanProperty(props, request, Environment.PROP_PARSER_TOC);
 			setProperty(props, request, Environment.PROP_PARSER_TOC_DEPTH);
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
 			setProperty(props, request, Environment.PROP_DBCP_MAX_ACTIVE);
 			setProperty(props, request, Environment.PROP_DBCP_MAX_IDLE);
 			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_ON_BORROW);
 			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_ON_RETURN);
 			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_WHILE_IDLE);
 			setProperty(props, request, Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME);
 			setProperty(props, request, Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS);
 			setProperty(props, request, Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN);
 			setProperty(props, request, Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION);
 			int maxFileSizeInKB = Integer.parseInt(request.getParameter(Environment.PROP_FILE_MAX_FILE_SIZE));
 			props.setProperty(Environment.PROP_FILE_MAX_FILE_SIZE, Integer.toString(maxFileSizeInKB * 1000));
 			setProperty(props, request, Environment.PROP_FILE_DIR_FULL_PATH);
 			setProperty(props, request, Environment.PROP_FILE_DIR_RELATIVE_PATH);
 			setProperty(props, request, Environment.PROP_FILE_BLACKLIST_TYPE);
 			setProperty(props, request, Environment.PROP_FILE_BLACKLIST);
 			setProperty(props, request, Environment.PROP_FILE_WHITELIST);
 			/*
 			setProperty(props, request, Environment.PROP_EMAIL_SMTP_HOST);
 			setProperty(props, request, Environment.PROP_EMAIL_SMTP_USERNAME);
 			setPassword(props, request, next, Environment.PROP_EMAIL_SMTP_PASSWORD, "smtpPassword");
 			setProperty(props, request, Environment.PROP_EMAIL_REPLY_ADDRESS);
 			*/
 			setProperty(props, request, Environment.PROP_LDAP_CONTEXT);
 			setProperty(props, request, Environment.PROP_LDAP_FACTORY_CLASS);
 			setProperty(props, request, Environment.PROP_LDAP_FIELD_EMAIL);
 			setProperty(props, request, Environment.PROP_LDAP_FIELD_FIRST_NAME);
 			setProperty(props, request, Environment.PROP_LDAP_FIELD_LAST_NAME);
 			setProperty(props, request, Environment.PROP_LDAP_FIELD_USERID);
 			setProperty(props, request, Environment.PROP_BASE_USER_HANDLER);
 			setProperty(props, request, Environment.PROP_LDAP_LOGIN);
 			setPassword(props, request, next, Environment.PROP_LDAP_PASSWORD, "ldapPassword");
 			setProperty(props, request, Environment.PROP_LDAP_SECURITY_AUTHENTICATION);
 			setProperty(props, request, Environment.PROP_LDAP_URL);
 			setProperty(props, request, Environment.PROP_CACHE_INDIVIDUAL_SIZE);
 			setProperty(props, request, Environment.PROP_CACHE_MAX_AGE);
 			setProperty(props, request, Environment.PROP_CACHE_MAX_IDLE_AGE);
 			setProperty(props, request, Environment.PROP_CACHE_TOTAL_SIZE);
 			setBooleanProperty(props, request, Environment.PROP_RSS_ALLOWED);
 			setProperty(props, request, Environment.PROP_RSS_TITLE);
 			Vector errors = WikiUtil.validateSystemSettings(props);
 			if (!errors.isEmpty()) {
 				next.addObject("errors", errors);
 				next.addObject("message", new WikiMessage("admin.message.changesnotsaved"));
 			} else {
 				// all is well, save the properties
 				Iterator iterator = props.keySet().iterator();
 				while (iterator.hasNext()) {
 					String key = (String)iterator.next();
 					String value = props.getProperty(key);
 					Environment.setValue(key, value);
 				}
 				Environment.saveProperties();
 				// re-initialize to reset database settings (if needed)
 				WikiUser user = WikiUtil.currentUser();
 				WikiBase.reset(request.getLocale(), user);
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
 			WikiBase.getDataHandler().reloadRecentChanges(null);
 			next.addObject("message", new WikiMessage("admin.message.recentchanges"));
 		} catch (Exception e) {
 			logger.severe("Failure while loading recent changes", e);
 			next.addObject("errors", new WikiMessage("admin.message.recentchangesfail", e.getMessage()));
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
 	private static void setBooleanProperty(Properties props, HttpServletRequest request, String parameter) {
 		boolean value = (request.getParameter(parameter) != null);
 		props.setProperty(parameter, Boolean.toString(value));
 	}
 
 	/**
 	 *
 	 */
 	private static void setPassword(Properties props, HttpServletRequest request, ModelAndView next, String parameter, String passwordParam) throws Exception {
 		if (!StringUtils.isBlank(request.getParameter(parameter))) {
 			String value = request.getParameter(parameter);
 			Encryption.setEncryptedProperty(parameter, value, props);
 			next.addObject(passwordParam, request.getParameter(parameter));
 		} else {
 			props.setProperty(parameter, Environment.getValue(parameter));
 		}
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
 	private void spamFilter(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		try {
 			SpamFilter.reload();
 			next.addObject("message", new WikiMessage("admin.message.spamfilter"));
 		} catch (Exception e) {
 			logger.severe("Failure while reloading spam filter patterns", e);
 			next.addObject("errors", new WikiMessage("admin.message.spamfilterfail", e.getMessage()));
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
 		Collection userHandlers = WikiConfiguration.getInstance().getUserHandlers();
 		next.addObject("userHandlers", userHandlers);
 		Collection dataHandlers = WikiConfiguration.getInstance().getDataHandlers();
 		next.addObject("dataHandlers", dataHandlers);
 		Collection searchEngines = WikiConfiguration.getInstance().getSearchEngines();
 		next.addObject("searchEngines", searchEngines);
 		Collection parsers = WikiConfiguration.getInstance().getParsers();
 		next.addObject("parsers", parsers);
 		LinkedHashMap poolExhaustedMap = new LinkedHashMap();
 		poolExhaustedMap.put(new Integer(GenericObjectPool.WHEN_EXHAUSTED_FAIL), "admin.persistence.caption.whenexhaustedaction.fail");
 		poolExhaustedMap.put(new Integer(GenericObjectPool.WHEN_EXHAUSTED_BLOCK), "admin.persistence.caption.whenexhaustedaction.block");
 		poolExhaustedMap.put(new Integer(GenericObjectPool.WHEN_EXHAUSTED_GROW), "admin.persistence.caption.whenexhaustedaction.grow");
 		next.addObject("poolExhaustedMap", poolExhaustedMap);
 		LinkedHashMap blacklistTypesMap = new LinkedHashMap();
 		blacklistTypesMap.put(new Integer(WikiBase.UPLOAD_ALL), "admin.upload.caption.allowall");
 		blacklistTypesMap.put(new Integer(WikiBase.UPLOAD_NONE), "admin.upload.caption.allownone");
 		blacklistTypesMap.put(new Integer(WikiBase.UPLOAD_BLACKLIST), "admin.upload.caption.useblacklist");
 		blacklistTypesMap.put(new Integer(WikiBase.UPLOAD_WHITELIST), "admin.upload.caption.usewhitelist");
 		next.addObject("blacklistTypes", blacklistTypesMap);
 		if (props == null) {
 			props = Environment.getInstance();
 		}
 		Integer maximumFileSize = new Integer(new Integer(props.getProperty(Environment.PROP_FILE_MAX_FILE_SIZE)).intValue()/1000);
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
 		Collection virtualWikiList = WikiBase.getDataHandler().getVirtualWikiList(null);
 		next.addObject("wikis", virtualWikiList);
 	}
 }
