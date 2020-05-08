 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.servlet;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.Cookie;
 
 /**
  * This class is a toolkit for the servlets and it's main functionality is to retrieve resource bundles.
  *
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public final class RequestUtil {
 
     /**
      * Prefix for properties in config
      */
     public static final String PATH_PREFIX = "no.feide.moria.web.";
 
     /**
      * Property name for: Config.
      */
     public static final String PROP_CONFIG = PATH_PREFIX + "config";
     /**
      * Property name for: Ticket ID.
      */
     public static final String PROP_LOGIN_TICKET_PARAM = PATH_PREFIX + "login.ticket_param";
     /**
      * Property name for: Organization.
      */
     public static final String PROP_ORG = PATH_PREFIX + "org";
     /**
      * Property name for: Default language.
      */
     public static final String PROP_LOGIN_DEFAULT_LANGUAGE = PATH_PREFIX + "login.default_language";
     /**
      * Property name for: Language.
      */
     public static final String PROP_LANGUAGE = PATH_PREFIX + "lang";
     /**
      * Property name for: URL prefix.
      */
     public static final String PROP_LOGIN_URL_PREFIX = PATH_PREFIX + "login.url_prefix";
     /**
      * Property name for: Common.
      */
     public static final String PROP_COMMON = "common";
     /**
      * Element in cookie: Organiszation.
      */
     public static final String PROP_COOKIE_ORG = PATH_PREFIX + "cookie.org.name";
     /**
      * Property name for: TTL of cookie.
      */
     public static final String PROP_COOKIE_ORG_TTL = PATH_PREFIX + "cookie.org.ttl";
     /**
      * Element in cookie: Language.
      */
     public static final String PROP_COOKIE_LANG = PATH_PREFIX + "cookie.lang.name";
     /**
      * Property name for: TTL of cookie.
      */
     public static final String PROP_COOKIE_LANG_TTL = PATH_PREFIX + "cookie.lang.ttl";
     /**
      * Element in cookie: SSO.
      */
     public static final String PROP_COOKIE_SSO = PATH_PREFIX + "cookie.sso.name";
     /**
      * Property name for: TTL of cookie.
      */
     public static final String PROP_COOKIE_SSO_TTL = PATH_PREFIX + "cookie.sso.ttl";
     /**
      * Property name for: Logout URL.
      */
     public static final String PROP_LOGOUT_URL_PARAM = PATH_PREFIX + "logout.url_param";
 
     /**
      * Bundle for the login page.
      */
     public static final String BUNDLE_LOGIN = "login";
 
     /**
      * From Authorization config: Language.
      */
     public static final String CONFIG_LANG = "lang";
     /**
      * From Authorization config: Home organization of service.
      */
     public static final String CONFIG_HOME = "home";
     /**
      * From Authorization config: Service name.
      */
     public static final String CONFIG_DISPLAY_NAME = "displayName";
     /**
      * From Authorization config: Service URL.
      */
     public static final String CONFIG_URL = "url";
 
 
     /**
      * Parameter in request object: Username.
      */
     public static final String PARAM_USERNAME = "username";
     /**
      * Parameter in request object: Password.
      */
     public static final String PARAM_PASSWORD = "password";
     /**
      * Parameter in request object: Organization.
      */
     public static final String PARAM_ORG = "org";
     /**
      * Parameter in request object: Language.
      */
     public static final String PARAM_LANG = "lang";
 
     /**
      * Attribute in request object: Base URL.
      */
     public static final String ATTR_BASE_URL = "baseURL";
     /**
      * Attribute in request object: Security level.
      */
     public static final String ATTR_SEC_LEVEL = "secLevel";
     /**
      * Attribute in request object: Error type.
      */
     public static final String ATTR_ERROR_TYPE = "errorType";
     /**
      * Attribute in request object: Available languages.
      */
     public static final String ATTR_LANGUAGES = "languages";
     /**
      * Attribute in request object:  Available organizations.
      */
     public static final String ATTR_ORGANIZATIONS = "organizations";
     /**
      * Attribute in request object: Preselected organization.
      */
     public static final String ATTR_SELECTED_ORG = "selectedOrg";
     /**
      * Attribute in request object: Preselected lanugage.
      */
     public static final String ATTR_SELECTED_LANG = "selectedLang";
     /**
      * Attribute in request object: Name of client/service.
      */
     public static final String ATTR_CLIENT_NAME = "clientName";
     /**
      * Attribute in request object: Link to associate with service name.
      */
     public static final String ATTR_CLIENT_URL = "clientURL";
     /**
      * Attribute in request object: Language bundle
      */
     public static final String ATTR_BUNDLE = "bundle";
 
     /**
      * Error type: No organization selected.
      */
     public static final String ERROR_NO_ORG = "noOrg";
     /**
      * Error type: Invalid organization selected.
      */
     public static final String ERROR_INVALID_ORG = "invalidOrg";
     /**
      * Error type: Authentication failed.
      */
     public static final String ERROR_AUTHENTICATION_FAILED = "authnFailed";
     /**
      * Error type: Unknown ticket.
      */
     public static final String ERROR_UNKNOWN_TICKET = "unknownTicket";
     /**
      * Error type: The directory is down.
      */
     public static final String ERROR_DIRECTORY_DOWN = "directoryDown";
     /**
      * Error type: Moria is unavailable.
      */
     public static final String ERROR_MORIA_DOWN = "moriaDown";
     /**
      * Error type: User must supply username and password.
      */
     public static final String ERROR_NO_CREDENTIALS = "noCredentials";
 
     /**
      * Default private constructor.
      */
     private RequestUtil() {
     }
 
     /**
      * Generate a resource bundle. The language of the resource bundle is selected from the following priority list: URL
      * parameter, cookie, service config, browser setting, Moria default
      *
      * @param bundleName       name of the bundle to retrieve, cannot be null
      * @param requestParamLang language specified as URL parameter, can be null
      * @param langFromCookie   language specified as cookie
      * @param serviceLang      default language specified by service, can be null
      * @param browserLang      language requested by the users browser, can be null
      * @param moriaLang        default language for Moria, cannot be null
      * @return the requested bundle
      * @throws MissingResourceException if no bundle is found
      */
     public static ResourceBundle getBundle(final String bundleName, final String requestParamLang, final String langFromCookie,
                                            final String serviceLang, final String browserLang, final String moriaLang) {
 
         /* Validate parameters. */
         if (bundleName == null || bundleName.equals("")) {
             throw new IllegalArgumentException("bundleName must be a non-empty string.");
         }
         if (moriaLang == null || moriaLang.equals("")) {
             throw new IllegalArgumentException("moriaDefaultLang must be a non-empty string.");
         }
 
         /* Build array of preferred language selections. */
         Vector langSelections = new Vector();
 
         /* Parameter. */
         if (requestParamLang != null && !requestParamLang.equals("")) {
             langSelections.add(requestParamLang);
         }
 
         /* Cookies. */
         if (langFromCookie != null) {
             langSelections.add(langFromCookie);
         }
 
         /* Service. */
         if (serviceLang != null && !serviceLang.equals("")) {
             langSelections.add(serviceLang);
         }
 
         /* Browser. */
         if (browserLang != null && !browserLang.equals("")) {
             String[] browserLangs = sortedAcceptLang(browserLang);
             for (int i = 0; i < browserLangs.length; i++) {
                 langSelections.add(browserLangs[i]);
             }
         }
 
         /* Moria default */
         langSelections.add(moriaLang);
 
         ResourceBundle bundle;
         for (Enumeration e = langSelections.elements(); e.hasMoreElements();) {
             bundle = locateBundle(bundleName, (String) e.nextElement());
             if (bundle != null) {
                 return bundle;
             }
         }
 
         throw new MissingResourceException("ResourceBundle not found", "ResourceBundle", "bundleName");
     }
 
     /**
      * Locates a bundle on a given language.
      *
      * @param bundleName name of the bundle, cannot be null or ""
      * @param lang       the bundles langauge
      * @return the resourceBundle for the selected language, null if it's not found
      */
     private static ResourceBundle locateBundle(final String bundleName, final String lang) {
 
         /* Validate parameters. */
         if (bundleName == null || bundleName.equals("")) {
             throw new IllegalArgumentException("bundleName must be a non-empty string.");
         }
         if (lang == null || lang.equals("")) {
             throw new IllegalArgumentException("lang must be a non-empty string.");
         }
 
         /* Find fallback resource bundle. */
         ResourceBundle fallback;
         try {
             fallback = ResourceBundle.getBundle(bundleName, new Locale("bogus"));
         } catch (MissingResourceException e) {
             fallback = null;
         }
 
         Locale locale = new Locale(lang);
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
 
         if (bundle != fallback) {
             return bundle;
         }
 
         /* Check if the fallback is actually requested. */
        if (bundle == fallback && locale.getLanguage().equals(Locale.getDefault().getLanguage())) {
             return bundle;
         }
 
         /* No bundle found. */
         return null;
     }
 
     /**
      * Return a requested cookie value from the HTTP request.
      *
      * @param cookieName Name of the cookie
      * @param cookies    The cookies from the HTTP request
      * @return Requested value, empty string if not found
      */
     public static String getCookieValue(final String cookieName, final Cookie[] cookies) {
 
         /* Validate parameters. */
         if (cookieName == null || cookieName.equals("")) {
             throw new IllegalArgumentException("cookieName must be a non-empty string");
         }
         if (cookies == null) {
             throw new IllegalArgumentException("cookies cannot be null");
         }
 
         String value = null;
         for (int i = 0; i < cookies.length; i++) {
             if (cookies[i].getName().equals(cookieName)) {
                 value = cookies[i].getValue();
             }
         }
 
         return value;
     }
 
     /**
      * Generate a cookie.
      *
      * @param cookieName  Name of the cookie
      * @param cookieValue Value to be set
      * @param validHours  Number of hours before the cookie expires
      * @return a Cookie with the specified name and value
      */
     public static Cookie createCookie(final String cookieName, final String cookieValue, final int validHours) {
 
         /* Validate parameters. */
         if (cookieName == null || cookieName.equals("")) {
             throw new IllegalArgumentException("cookieName must be a non-empty string.");
         }
         if (cookieValue == null || cookieValue.equals("")) {
             throw new IllegalArgumentException("cookieValue must be a non-empty string.");
         }
         if (validHours < 0) {
             throw new IllegalArgumentException("validDays must be a >= 0.");
         }
 
         Cookie cookie = new Cookie(cookieName, cookieValue);
         cookie.setMaxAge(validHours * 60 * 60); // Hours to seconds
         cookie.setVersion(0);
         return cookie;
     }
 
     /**
      * Parser for the Accept-Language header sent from browsers. The language entries in the string can be weighted and
      * the parser generates a list of the languages sorted by the weight value.
      *
      * @param acceptLang the accept language header, cannot be null or ""
      * @return a string array of language names, sorted by the browsers weight preferences
      */
     static String[] sortedAcceptLang(final String acceptLang) {
 
         if (acceptLang == null || acceptLang.equals("")) {
             throw new IllegalArgumentException("acceptLang must be a non-empty string.");
         }
 
         StringTokenizer tokenizer = new StringTokenizer(acceptLang, ",");
         HashMap weightedLangs = new HashMap();
 
         while (tokenizer.hasMoreTokens()) {
             String token = tokenizer.nextToken();
             String lang = token;
             boolean ignore = false;
             String weight = "1.0";
             int index;
 
             /* Language and weighting are devided by ";". */
             if ((index = token.indexOf(";")) != -1) {
                 String parsedWeight;
                 lang = token.substring(0, index);
 
                 /* Weight data. */
                 parsedWeight = token.substring(index + 1, token.length());
                 parsedWeight = parsedWeight.trim();
                 if (parsedWeight.startsWith("q=")) {
                     parsedWeight = parsedWeight.substring(2, parsedWeight.length());
                     weight = parsedWeight;
                 } else {
                     /* Format error, flag to ignore token. */
                     ignore = true;
                 }
             }
 
             if (!ignore) {
                 lang = lang.trim();
 
                 /* Country and language is devided by "-" (optional). */
                 if ((index = lang.indexOf("-")) != -1) {
                     lang = lang.substring(index + 1, lang.length());
                 }
 
                 weightedLangs.put(weight, lang);
             }
         }
 
         Vector sortedLangs = new Vector();
         String[] sortedKeys = (String[]) weightedLangs.keySet().toArray(new String[weightedLangs.size()]);
         Arrays.sort(sortedKeys, Collections.reverseOrder());
 
         for (int i = 0; i < sortedKeys.length; i++) {
             sortedLangs.add(weightedLangs.get(sortedKeys[i]));
         }
 
         return (String[]) sortedLangs.toArray(new String[sortedLangs.size()]);
     }
 
     /**
      * Reads institution names from the servlet config and generates a TreeMap with the result.
      *
      * @param config   the web modules configuration
      * @param element  the sub element of the configuration to process
      * @param language the language to generate institution names on
      * @return a TreeMap of institution names with full name as key and id as value object
      */
     static TreeMap parseConfig(final Properties config, final String element, final String language) {
         /* Validate parameters */
         if (config == null) {
             throw new IllegalArgumentException("config cannot be null.");
         }
         if (element == null || element.equals("")) {
             throw new IllegalArgumentException("element must be a non-empty string.");
         }
         if (language == null || language.equals("")) {
             throw new IllegalArgumentException("language must be a non-empty string.");
         }
 
         String value = config.getProperty(element + "_" + language);
         if (value == null) {
             throw new IllegalStateException("No elements of type '" + element + "' in config.");
         }
 
         StringTokenizer tokenizer = new StringTokenizer(value, ",");
         TreeMap names = new TreeMap();
 
         while (tokenizer.hasMoreTokens()) {
             String token = tokenizer.nextToken();
             int index = token.indexOf(":");
 
             /* Abort if there is no separator in token */
             if (index == -1) {
                 // TODO: Log
                 throw new IllegalStateException("Config has wrong format.");
             }
 
             String shortName = token.substring(0, index);
             String longName = token.substring(index + 1, token.length());
 
             /* Abort if there is more than one separator in one token */
             if (shortName.indexOf(":") != -1 || longName.indexOf(":") != -1) {
                 // TODO: Log
                 throw new IllegalStateException("Config has wrong format.");
             }
 
             names.put(longName, shortName);
         }
 
         return names;
     }
 
     /**
      * Replaces a given token with hyperlinks. The URL and name of the hyperlink is given as parameters. Every occurance
      * of the token in the data string is replaced by a hyperlink.
      *
      * @param token the token to replace with link
      * @param data  the data containing text and token(s)
      * @param name  the link text
      * @param url   the URL to link to
      * @return a string with hyperlinks in stead of tokens
      */
     public static String insertLink(final String token, final String data, final String name, final String url) {
         /* Validate parameters */
         if (token == null || token.equals("")) {
             throw new IllegalArgumentException("token must be a non-empty string");
         }
         if (data == null || data.equals("")) {
             throw new IllegalArgumentException("data must be a non-empty string");
         }
         if (name == null || name.equals("")) {
             throw new IllegalArgumentException("name must be a non-empty string");
         }
         if (url == null || url.equals("")) {
             throw new IllegalArgumentException("url must be a non-empty string");
         }
 
         String link = "<a href=\"" + url + "\">" + name + "</a>";
 
         return data.replaceAll(token, link);
     }
 
     /**
      * Get the config from the context. The configuration is expected to be set by the controller before requests are
      * sent to this servlet.
      *
      * @param context ServletContext containing the configuration.
      * @return the configuration
      * @throws IllegalStateException if the config is not set in the context
      */
     static Properties getConfig(final ServletContext context) {
         /* Validate parameters */
         if (context == null) {
             throw new IllegalArgumentException("context must be a non-empty string");
         }
 
         Properties config;
 
         /* Validate config */
         try {
             config = (Properties) context.getAttribute("no.feide.moria.web.config");
         } catch (ClassCastException e) {
             throw new IllegalStateException(
                     "Config is not correctly set in context. Not a java.util.Properties object.");
         }
 
         if (config == null) {
             throw new IllegalStateException("Config is not set in context.");
         }
 
         return config;
     }
 }
