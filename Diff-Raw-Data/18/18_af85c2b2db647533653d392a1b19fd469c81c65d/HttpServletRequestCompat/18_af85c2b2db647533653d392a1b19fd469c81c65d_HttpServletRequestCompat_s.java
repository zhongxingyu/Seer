 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2001 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.util.servletcompat;
 
 import java.util.*;
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.lang.reflect.*;
 import org.melati.util.UnexpectedExceptionException;
 
 public class HttpServletRequestCompat {
 
   private HttpServletRequestCompat() {}
 
   private static Method
       getUserPrincipal, getContextPath, getHeaders, getSession,
       isRequestedSessionIdFromURL, isUserInRole, getAttributeNames, getLocale,
       getLocales, getRequestDispatcher, isSecure, removeAttribute,
       setAttribute;
 
   private static Method methodOrNull(Class c, String n, String[] pn) {
     try {
       Class[] p = new Class[pn.length];
       for (int i = 0; i < pn.length; ++i)
         p[i] = Class.forName(pn[i]);
       return c.getMethod(n, p);
     }
     catch (NoSuchMethodException e) {
       return null;
     }
     catch (ClassNotFoundException e) {
       return null;
     }
   }
 
   static {
     try {
       String[] noparams = {};
 
       Class hsr = Class.forName("javax.servlet.http.HttpServletRequest");
       getUserPrincipal = methodOrNull(hsr, "getUserPrincipal", noparams);
       getContextPath = methodOrNull(hsr, "getContextPath", noparams);
       getHeaders = methodOrNull(hsr, "getHeaders", new String[] { "java.lang.String" } );
       getSession = methodOrNull(hsr, "getSession", noparams);
       isRequestedSessionIdFromURL = methodOrNull(hsr, "isRequestedSessionIdFromURL", noparams);
       isUserInRole = methodOrNull(hsr, "isUserInRole", new String[] { "java.lang.String" } );
       getAttributeNames = methodOrNull(hsr, "getAttributeNames", noparams);
       getLocale = methodOrNull(hsr, "getLocale", noparams);
       getLocales = methodOrNull(hsr, "getLocales", noparams);
       getRequestDispatcher = methodOrNull(hsr, "getRequestDispatcher",
                                           new String[] { "java.lang.String" } );
       isSecure = methodOrNull(hsr, "isSecure", noparams);
       removeAttribute = methodOrNull(hsr, "removeAttribute",
                                      new String[] { "java.lang.String" } );
       setAttribute = methodOrNull(hsr, "setAttribute",
                                   new String[] { "java.lang.String", "java.lang.Object" });
     }
     catch (Exception e) {
       e.printStackTrace();
       throw new Error(
           "org.melati.util.servletcompat.HttpServletRequestCompat" +
           "failed to initialize; contact the Melati developers");
     }
   }
 
   public static String getAuthType(HttpServletRequest it) {
     return it.getAuthType();
   }
   public static Cookie[] getCookies(HttpServletRequest it) {
     return it.getCookies();
   }
   public static long getDateHeader(HttpServletRequest it, String a) {
     return it.getDateHeader(a);
   }
   public static String getHeader(HttpServletRequest it, String a) {
     return it.getHeader(a);
   }
   public static Enumeration getHeaderNames(HttpServletRequest it) {
     return it.getHeaderNames();
   }
   public static int getIntHeader(HttpServletRequest it, String a) {
     return it.getIntHeader(a);
   }
   public static String getMethod(HttpServletRequest it) {
     return it.getMethod();
   }
   public static String getPathInfo(HttpServletRequest it) {
     return it.getPathInfo();
   }
   public static String getPathTranslated(HttpServletRequest it) {
     return it.getPathTranslated();
   }
   public static String getQueryString(HttpServletRequest it) {
     return it.getQueryString();
   }
   public static String getRemoteUser(HttpServletRequest it) {
     return it.getRemoteUser();
   }
   public static String getRequestURI(HttpServletRequest it) {
     return it.getRequestURI();
   }
   public static String getRequestedSessionId(HttpServletRequest it) {
     return it.getRequestedSessionId();
   }
   public static String getServletPath(HttpServletRequest it) {
     return it.getServletPath();
   }
   public static HttpSession getSession(HttpServletRequest it, boolean a) {
     return it.getSession(a);
   }
   public static boolean isRequestedSessionIdFromUrl(HttpServletRequest it) {
     return it.isRequestedSessionIdFromUrl();
   }
   public static boolean isRequestedSessionIdValid(HttpServletRequest it) {
     return it.isRequestedSessionIdValid();
   }
 
   /**
    * @deprecated in Servlet API 2.2
    */
 
   public static boolean isRequestedSessionIdFromCookie(HttpServletRequest it) {
     return it.isRequestedSessionIdFromCookie();
   }
 
   // 
   // ============================
   //  Servlet API 2.2 extensions
   // ============================
   // 
 
   public static class MissingMethodError extends NoSuchMethodError {
     public String getMessage() {
       return "The application tried to use a method from the " +
              "Servlet API 2.2, but was running against the 2.0 " +
              "API.";
     }
   }
 
   private static Object invoke(Method method, HttpServletRequest it,
                                Object[] args) {
     if (method == null)
       throw new MissingMethodError();
     else {
       try {
         return method.invoke(it, args);
       }
       catch (InvocationTargetException e) {
         Throwable f = e.getTargetException();
         if (f instanceof RuntimeException) // they all should be
           throw (RuntimeException)f;
         else if (f instanceof Exception)
           throw new UnexpectedExceptionException(
               "while invoking a Servlet API method", (Exception)f);
         else if (f instanceof Error)
           throw (Error)f;
         else {
           f.printStackTrace();
           throw new Error(
                "totally unexpected kind of throwable in " +
                "org.melati.util.servletcompat.HttpServletRequestCompat");
         }
       }
       catch (IllegalAccessException e) {
         e.printStackTrace();
         throw new Error(
             "org.melati.util.servletcompat.HttpServletRequestCompat" +
             "got an unexpected IllegalAccessException trying to " +
             "invoke a method; contact the Melati developers");
       }
     }
   }
 
   private static final Object[] noargs = {};
 
   /**
    * Returns the empty string when run against 2.0 API.
    */
 
   public static String getContextPath(HttpServletRequest it) {
     if (getContextPath == null) return "";
     else return (String)invoke(getContextPath, it, noargs);
     // return it.getContextPath();
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static java.security.Principal getUserPrincipal(HttpServletRequest it) {
     return (java.security.Principal)invoke(getUserPrincipal, it, noargs);
     // return it.getUserPrincipal();
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static Enumeration getHeaders(HttpServletRequest it, String a) {
     return (Enumeration)invoke(getHeaders, it, new Object[] { a });
     // return it.getHeaders(a);
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static HttpSession getSession(HttpServletRequest it) {
     return (HttpSession)invoke(getSession, it, noargs);
     // return it.getSession();
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static boolean isRequestedSessionIdFromURL(HttpServletRequest it) {
     return ((Boolean)invoke(isRequestedSessionIdFromURL, it, noargs)).
         booleanValue();
     // return it.isRequestedSessionIdFromURL();
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static boolean isUserInRole(HttpServletRequest it, String a) {
     return ((Boolean)invoke(isUserInRole, it, new Object[] { a })).
         booleanValue();
     // return it.isUserInRole(a);
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static Enumeration getAttributeNames(HttpServletRequest it) {
     return (Enumeration)invoke(getAttributeNames, it, noargs);
     // return it.getAttributeNames();
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static Locale getLocale(HttpServletRequest it)  {
     return (Locale)invoke(getLocale, it, noargs);
     // return it.getLocale();
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static Enumeration getLocales(HttpServletRequest it) {
     return (Enumeration)invoke(getLocales, it, noargs);
     // return it.getLocales();
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static RequestDispatcher getRequestDispatcher(HttpServletRequest it, String arg) {
     return (RequestDispatcher)invoke(getRequestDispatcher, it, new Object[] { arg });
     // return it.getRequestDispatcher(arg);
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static boolean isSecure(HttpServletRequest it) {
     return ((Boolean)invoke(isSecure, it, noargs)).booleanValue();
     // return it.isSecure();
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static void removeAttribute(HttpServletRequest it, String arg) {
     invoke(removeAttribute, it, new Object[] { arg });
     // it.removeAttribute(arg);
   }
 
   /**
    * Throws <TT>MissingMethodError</TT> when run against 2.0 API.
    */
 
   public static void setAttribute(HttpServletRequest it, String arg1, Object arg2) {
     invoke(setAttribute, it, new Object[] { arg1, arg2 });
     // it.setAttribute(arg1, arg2);
   }
 }
