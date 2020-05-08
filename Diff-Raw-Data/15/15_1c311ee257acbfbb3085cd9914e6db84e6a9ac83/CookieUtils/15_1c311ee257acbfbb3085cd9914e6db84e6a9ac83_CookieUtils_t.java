 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: CookieUtils.java,v 1.2 2007-02-15 21:47:42 qcheng Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 
 package com.sun.identity.saml2.idpdiscovery;
 
 import java.util.StringTokenizer;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import com.sun.identity.shared.configuration.SystemPropertiesManager;
 import com.sun.identity.shared.encode.URLEncDec;
 
 /**
  * Implements utility methods for handling Cookie.
  * <p>
  */ 
 
 public class CookieUtils {
     static boolean secureCookie =
         (SystemPropertiesManager.get(IDPDiscoveryConstants.AM_COOKIE_SECURE)
         != null &&
         SystemPropertiesManager.get(IDPDiscoveryConstants.AM_COOKIE_SECURE).
         equalsIgnoreCase("true"));
     static boolean cookieEncoding =
         (SystemPropertiesManager.get(IDPDiscoveryConstants.AM_COOKIE_ENCODE)
         != null &&
         SystemPropertiesManager.get(IDPDiscoveryConstants.AM_COOKIE_ENCODE).
         equalsIgnoreCase("true"));
     private static int defAge = -1;
    public static Debug debug = Debug.getInstance("libIDPDiscovery");
 
     /**
      * Gets property value of "com.iplanet.am.cookie.secure"
      *
      * @return the property value of "com.iplanet.am.cookie.secure"
      */
     public static boolean isCookieSecure() {
         return secureCookie;
     }
 
     /**
      * Gets value of cookie that has mached name in servlet request
      *
      * @param req HttpServletRequest request
      * @param name cookie name in servlet request
      * @return value of that name of cookie
      */
     public static String getCookieValueFromReq(
         HttpServletRequest req,
         String name
     ) {
         String cookieValue = null;
         try {
             Cookie cookies[] = req.getCookies();
             if (cookies != null) {
                 for (int nCookie = 0; nCookie < cookies.length; nCookie++) {
                     if (cookies[nCookie].getName().equalsIgnoreCase(name)) {
                         cookieValue = cookies[nCookie].getValue();
                         break;
                     }
                 }
         
                 // Check property value and it decode value
                 // Bea, IBM
                 if (cookieEncoding && (cookieValue != null)) {
                     cookieValue= AMURLEncDec.decode(cookieValue);
                 }
             } else {
                 debug.message("No Cookie is in the request");
             }
         } catch (Exception e) {
             debug.error("Error getting cookie  : " , e);
         }
         
         // check this is for idff or saml2
         String reqURI = req.getRequestURI(); 
         boolean isSAML2 = true; 
         if (reqURI.endsWith(IDPDiscoveryConstants.IDFF_READER_URI) ||
             reqURI.endsWith(IDPDiscoveryConstants.IDFF_WRITER_URI)) { 
             isSAML2 = false; 
         }
 
         // take care of the case where there is a '+' in preferred idp
         // When '+' is decoded, it became ' ' which is also the seperator
         // of different preferred idps
         if (cookieValue == null) {
             return cookieValue;
         } else {
             StringBuffer result = new StringBuffer(200);
             StringTokenizer st = new StringTokenizer(cookieValue, " ");
             while (st.hasMoreTokens()) {
                 String curIdpString = (String)st.nextToken();
                 while (!isSAML2 && curIdpString.length() < 28 && 
                     st.hasMoreTokens()) {
                     curIdpString = curIdpString + "+" + (String) st.nextToken();
                 }
                 result.append(curIdpString + " ");
             }
             if (debug.messageEnabled()) {
                 debug.message("CookieUtils:cookieValue=" + cookieValue
                         + ", result=" + result.toString());
             }
             return result.toString().trim();
         }
     }
               
         
         /**
          * Constructs a cookie with a specified name and value.
          *
          * @param name  a String specifying the name of the cookie
          *
          * @param value  a String specifying the value of the cookie
          *
          * @return constructed cookie
          */
         public static Cookie newCookie(String name, String value) {
             return newCookie(name, value, defAge, null, null);
         }
         
         /**
          * Constructs a cookie with a specified name and value and sets
          * the maximum age of the cookie in seconds.
          *
          * @param name  a String specifying the name of the cookie
          *
          * @param value  a String specifying the value of the cookie
          *
          * @param maxAge an integer specifying the maximum age of the cookie in 
          * seconds; if negative, means the cookie is not stored; 
          * if zero, deletes the cookie
          *
          * @return constructed cookie
          */
         public static Cookie newCookie(String name, String value, int maxAge) {
             return newCookie(name, value, maxAge, null, null);
         }
 
         /**
          * Constructs a cookie with a specified name and value and sets
          * a path for the cookie to which the client should return the cookie.
          *
          * @param name  a String specifying the name of the cookie
          *
          * @param value  a String specifying the value of the cookie
          *
          * @param path a String specifying a path 
          *
          * @return constructed cookie
          */
         public static Cookie newCookie(String name, String value, String path) {
             return newCookie(name, value, defAge, path, null);
         }
 
         /**
          * Constructs a cookie with a specified name and value and sets
          * a path for the cookie to which the client should return the cookie
          * and sets the domain within which this cookie should be presented.
          *
          * @param name  a String specifying the name of the cookie
          *
          * @param value  a String specifying the value of the cookie
          *
          * @param path a String specifying a path 
          *
          * @param domain a String containing the domain name within which 
          * this cookie is visible; form is according to <code>RFC 2109</code>
          *
          * @return constructed cookie
          */
         public static Cookie newCookie
                       (String name, String value, String path, String domain) {
             return newCookie(name, value, defAge, path, domain);
         }
 
         /**
          * Constructs a cookie with a specified name and value and sets
          * the maximum age of the cookie in seconds and sets
          * a path for the cookie to which the client should return the cookie
          * and sets the domain within which this cookie should be presented.
          *
          * @param name  a String specifying the name of the cookie
          *
          * @param value  a String specifying the value of the cookie
          *
          * @param maxAge an integer specifying the maximum age of the cookie in 
          * seconds; if negative, means the cookie is not stored; 
          * if zero, deletes the cookie
          *
          * @param path a String specifying a path 
          *
          * @param domain a String containing the domain name within which 
          * this cookie is visible; form is according to RFC 2109
          *
          * @return constructed cookie
          */
         public static Cookie newCookie
           (String name, String value, int maxAge, String path, String domain) {
             Cookie cookie = null;
             
             // Based on property value it does url encoding.
             // BEA, IBM
             if (cookieEncoding) {
                 cookie = new Cookie(name, URLEncDec.encode(value));
             } else {
                 cookie = new Cookie(name, value);
             }
 
             cookie.setMaxAge(maxAge);
 
             if ((path != null) && (path.length() > 0)) {
                 cookie.setPath(path);
             } else {
                 cookie.setPath("/");
             }
                 
             if ((domain != null) && (domain.length() > 0)) {
                 cookie.setDomain(domain);
             }
 
             cookie.setSecure(isCookieSecure());
                 
             return cookie;
         }
         
         /**
          * Gets the preferred cookie name based on the HttpRequest URI. 
          *
          * @param reqURI  a String specifying the HttpRequest URI.
          *
          * @return the preferred cookie name.
          *         _saml_idp if the HttpRequest URI matches the SAML2 
          *         reader or writer servlet uri. 
          *         _liberty_idp if the HttpRequest URI matches the IDFF 
          *         reader or writer servlet uri. 
          *         return empty string if no above match found. 
          *         return null if the input HttpRequest uri is null or empty.
          */
         public static String getPreferCookieName( String reqURI) 
         {
            if (reqURI != null &&  !reqURI.equals("")) { 
                if (reqURI.endsWith(IDPDiscoveryConstants.IDFF_READER_URI) ||
                    reqURI.endsWith(IDPDiscoveryConstants.IDFF_WRITER_URI)) { 
                    return(IDPDiscoveryConstants.IDFF_COOKIE_NAME); 
                } else if (reqURI.endsWith(
                    IDPDiscoveryConstants.SAML2_READER_URI) ||
                    reqURI.endsWith(IDPDiscoveryConstants.SAML2_WRITER_URI)) { 
                    return(IDPDiscoveryConstants.SAML2_COOKIE_NAME);
                } else {
                    return "";
                }
             } else {
                 return null;
             }
         }       
 }
