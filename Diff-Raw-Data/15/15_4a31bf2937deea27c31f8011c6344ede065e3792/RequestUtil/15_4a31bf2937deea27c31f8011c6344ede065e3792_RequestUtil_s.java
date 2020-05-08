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
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
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
 
 /**
  * This class is a toolkit for the servlets and it's main functionality is to retrieve
  * resource bundles.
  *
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public abstract class RequestUtil extends HttpServlet {
 
     /**
      * Generate a resource bundle. The language of the resource bundle is selected
      * from the following priority list: URL parameter, cookie, service config, browser setting, Moria default
      *
     * @param bundleName       name of the bundle to retrieve
     * @param requestParamLang language specified as URL parameter
     * @param cookies          cookies from the HTTP request
     * @param serviceLang      default language specified by service
     * @param browserLang      language requested by the users browser
     * @param moriaLang        default language for Moria
     * @return the requested bundle, null if no b
      */
     public static ResourceBundle getBundle(final String bundleName, final String requestParamLang, final Cookie[] cookies,
                                            final String serviceLang, final String browserLang, final String moriaLang) {
 
         /* Validate parameters */
         if (bundleName == null || bundleName.equals("")) {
             throw new IllegalArgumentException("bundleName must be a non-empty string.");
         }
         if (moriaLang == null || moriaLang.equals("")) {
             throw new IllegalArgumentException("moriaDefaultLang must be a non-empty string.");
         }
 
         /* Build array of preferred language selections */
         Vector langSelections = new Vector();
 
         /* Parameter */
         if (requestParamLang != null && !requestParamLang.equals("")) {
             langSelections.add(requestParamLang);
         }
 
         /* Cookies */
         if (cookies != null) {
             String cookieValue = getCookieValue("lang", cookies);
             if (cookieValue != null) {
                 langSelections.add(cookieValue);
             }
         }
 
         /* Service */
         if (serviceLang != null && !serviceLang.equals("")) {
             langSelections.add(serviceLang);
         }
 
         /* Browser */
         if (browserLang != null && !browserLang.equals("")) {
             String[] browserLangs = sortedAcceptLang(browserLang);
             for (int i = 0; i < browserLangs.length; i++) {
                 langSelections.add(browserLangs[i]);
             }
         }
 
         /* Moria */
         if (moriaLang != null && !moriaLang.equals("")) {
             langSelections.add(moriaLang);
         }
 
         ResourceBundle bundle = null;
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
 
         /* Validate parameters */
         if (bundleName == null || bundleName.equals("")) {
             throw new IllegalArgumentException("bundleName must be a non-empty string.");
         }
         if (lang == null || lang.equals("")) {
             throw new IllegalArgumentException("lang must be a non-empty string.");
         }
 
         /* Find fallback resource bundle. */
         ResourceBundle fallback = null;
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
 
         /* Check if the fallback is actually requested */
         if (bundle == fallback && locale.getLanguage().equals(Locale.getDefault().getLanguage())) {
             return bundle;
         }
 
         /* No bundle found */
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
 
         /* Validate parameters */
         if (cookieName == null || cookieName.equals("")) {
             throw new IllegalArgumentException("cookieName must be a non-empty string");
         }
         if (cookies == null) {
             throw new IllegalArgumentException("cookies cannot be null");
         }
 
         String value = null;
         if (cookies != null) {
             for (int i = 0; i < cookies.length; i++) {
                 if (cookies[i].getName().equals(cookieName)) {
                     value = cookies[i].getValue();
                 }
             }
         }
 
         return value;
     }
 
     /**
      * Generate a cookie.
      *
      * @param cookieName  Name of the cookie
      * @param cookieValue Value to be set
      * @param validDays   Number of days before the cookie expires
      * @return a Cookie with the specified name and value
      */
     public static Cookie createCookie(final String cookieName, final String cookieValue, final int validDays) {
 
         /* Validate parameters */
         if (cookieName == null || cookieName.equals("")) {
             throw new IllegalArgumentException("cookieName must be a non-empty string.");
         }
         if (cookieValue == null || cookieValue.equals("")) {
             throw new IllegalArgumentException("cookieValue must be a non-empty string.");
         }
         if (validDays < 0) {
             throw new IllegalArgumentException("validDays must be a >= 0.");
         }
 
         Cookie cookie = new Cookie(cookieName, cookieValue);
         cookie.setMaxAge(validDays * 24 * 60 * 60); // Days to seconds
         cookie.setVersion(0);
         return cookie;
     }
 
     /**
      * Parser for the Accept-Language header sent from browsers. The language entries in the
      * string can be weighted and the parser generates a list of the languages sorted by the
      * weight value.
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
 
             /* Language and weighting are devided by ";" */
             if ((index = token.indexOf(";")) != -1) {
                 String parsedWeight;
                 lang = token.substring(0, index);
 
                 /* Weight data */
                 parsedWeight = token.substring(index + 1, token.length());
                 parsedWeight = parsedWeight.trim();
                 if (parsedWeight.startsWith("q=")) {
                     parsedWeight = parsedWeight.substring(2, parsedWeight.length());
                     weight = parsedWeight;
                 } else {
                     /* Format error, flag to ignore token */
                     ignore = true;
                 }
             }
 
             if (!ignore) {
                 lang = lang.trim();
 
                 /* Country and language is devided by "-" (optional) */
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
      * @param language the language to generate institution names on
      * @return         a TreeMap of institution names with full name as key and id as value object
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
             throw new IllegalStateException("No elements of type '"+element+"' in config.");
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
      * Replaces a given token with hyperlinks. The URL and name of the hyperlink
      * is given as parameters. Every occurance of the token in the data string is
      * replaced by a hyperlink.
      *
      * @param token the token to replace with link
      * @param data  the data containing text and token(s)
      * @param name  the link text
      * @param url   the URL to link to
      * @return a string with hyperlinks in stead of tokens
      */
     public static String insertLink(String token, String data, String name, String url) {
         /* Validate parameters */
         if (token == null || token.equals("")) {
             // TODO: Log
             throw new IllegalArgumentException("token must be a non-empty string");
         }
         if (data == null || data.equals("")) {
             // TODO: Log
             throw new IllegalArgumentException("data must be a non-empty string");
         }
         if (name == null || name.equals("")) {
             // TODO: Log
             throw new IllegalArgumentException("name must be a non-empty string");
         }
         if (url == null || url.equals("")) {
             // TODO: Log
             throw new IllegalArgumentException("url must be a non-empty string");
         }
 
         String link = "<a href=\"" + url + "\">" + name + "</a>";
 
         return data.replaceAll(token, link);
     }
 }
