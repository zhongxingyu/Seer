 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.util;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.vfny.geoserver.global.ApplicationState;
 import org.vfny.geoserver.global.UserContainer;
 import org.vfny.geoserver.global.WFS;
 import org.vfny.geoserver.global.WMS;
 
 
 /**
  * Utility methods helpful when processing GeoServer Requests.
  * 
  * <p>
  * Provides helper functions and classes useful when implementing your own
  * Response classes. Of significant importantance are the Request processing
  * functions that allow access to the WebContainer, GeoServer and the User's
  * Session.
  * </p>
  * 
  * <p>
  * If you are working with the STRUTS API the Action method is the direct
  * paralle of the Response classes. You may whish to look at how ConfigAction
  * is implemented, it is a super class which delegates to these Request
  * processing methods.
  * </p>
  *
  * @author Jody Garnett
  */
 public final class Requests {
     /**
      * Aquire GeoServer from Web Container.
      * 
      * <p>
      * In GeoServer is create by a STRUTS plug-in and is available through the
      * Web container.
      * </p>
      * 
      * <p>
      * Test cases may seed the request object with a Mock WebContainer and a
      * Mock GeoServer.
      * </p>
      *
      * @param request HttpServletRequest used to aquire servlet context
      *
      * @return GeoServer instance for the current Web Application
      */
     /*public static GeoServer getGeoServer(HttpServletRequest request) {
         return (GeoServer) getWFS(request).getGeoServer();
     }*/
     /**
      * Aquire WFS from Web Container.
      * 
      * <p>
      * In WFS is create by a STRUTS plug-in and is available through the
      * Web container.
      * </p>
      * 
      * <p>
      * Test cases may seed the request object with a Mock WebContainer and a
      * Mock GeoServer.
      * </p>
      *
      * @param request HttpServletRequest used to aquire servlet context
      *
      * @return WFS instance for the current Web Application
      */
     public static WFS getWFS(HttpServletRequest request) {
     	ServletRequest req = request;
     	HttpSession session = request.getSession();
     	ServletContext context = session.getServletContext();
 
     	return (WFS) context.getAttribute(WFS.WEB_CONTAINER_KEY);
     }
     /**
      * Aquire WMS from Web Container.
      * 
      * <p>
      * In WMS is create by a STRUTS plug-in and is available through the
      * Web container.
      * </p>
      * 
      * <p>
      * Test cases may seed the request object with a Mock WebContainer and a
      * Mock GeoServer.
      * </p>
      *
      * @param request HttpServletRequest used to aquire servlet context
      *
      * @return WMS instance for the current Web Application
      */
     public static WMS getWMS(HttpServletRequest request) {
     	ServletRequest req = request;
     	HttpSession session = request.getSession();
     	ServletContext context = session.getServletContext();
 
     	return (WMS) context.getAttribute(WMS.WEB_CONTAINER_KEY);
     }
 
     /**
      * Get base url used - it is not any more assumed to be http://server:port/geoserver/
      * 
      * Removed the hardcoded "http://" and replaced it with httpServletRequest.getScheme() because
      * the https case was not being handled.
      * @param httpServletRequest
      * @return http://server:port/path-defined-context/
      */
     public static String getBaseUrl(HttpServletRequest httpServletRequest) {
 /*
  * didier (2004/10/03) assumption removed :
  *       return "http://" + httpServletRequest.getServerName() + ":"
  *       + httpServletRequest.getServerPort() + "/geoserver/";
  */
     	return httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + ":"
         + httpServletRequest.getServerPort() + httpServletRequest.getContextPath() +"/";
     }
     
     /**
      * Get capabilities base url used
      * 
      * @param httpServletRequest
      * @return http://server:port/path-defined-context/data/capabilities
      */
     public static String getSchemaBaseUrl(HttpServletRequest httpServletRequest) {
    	return getBaseUrl(httpServletRequest) + "data/capabilities/";
     }
     
     /**
      * Aquire type safe session information in a UserContainer.
      * 
      * @param request Http Request used to aquire session reference
      *
      * @return UserContainer containing typesafe session information.
      */
     public static UserContainer getUserContainer(HttpServletRequest request) {
         HttpSession session = request.getSession();
 
         synchronized (session) {
             UserContainer user = (UserContainer) session.getAttribute(UserContainer.SESSION_KEY);
 
             return user;
         }
     }
 
     /**
      * Tests is user is loggin in.
      * 
      * <p>
      * True if UserContainer exists has been created.
      * </p>
      *
      * @param request HttpServletRequest providing current Session
      *
      * @return
      */
     public static boolean isLoggedIn(HttpServletRequest request) {
         HttpSession session = request.getSession();
 
         synchronized (session) {
             UserContainer user = (UserContainer) session.getAttribute(UserContainer.SESSION_KEY);
             return user != null;
         }
     }
 
     /**
      * Ensures a user is logged out.
      * 
      * <p>
      * Removes the UserContainer, and thus GeoServers knowledge of the current
      * user attached to this Session.
      * </p>
      *
      * @param request HttpServletRequest providing current Session
      */
     public static void logOut(HttpServletRequest request) {
         HttpSession session = request.getSession();
         session.removeAttribute(UserContainer.SESSION_KEY);
     }
 
     /**
      * Access GeoServer Application State from the WebContainer.
      *
      * @param request DOCUMENT ME!
      *
      * @return Configuration model for Catalog information.
      */
     public static ApplicationState getApplicationState(
         HttpServletRequest request) {
 
         ServletRequest req = request;
         HttpSession session = request.getSession();
         ServletContext context = session.getServletContext();
 
         return (ApplicationState) context.getAttribute(ApplicationState.WEB_CONTAINER_KEY);
     }
 }
