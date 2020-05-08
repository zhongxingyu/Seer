 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.requests;
 
 import org.geotools.validation.ValidationProcessor;
 import org.vfny.geoserver.global.ApplicationState;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.UserContainer;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 
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
     public static GeoServer getGeoServer(HttpServletRequest request) {
         ServletRequest req = request;
         HttpSession session = request.getSession();
         ServletContext context = session.getServletContext();
 
         return (GeoServer) context.getAttribute(GeoServer.WEB_CONTAINER_KEY);
     }
 
     /**
      * Aquire ValidationProcessor from Web Container.
      * 
      * <p>
      * In ValidationProcessor is create by a STRUTS plug-in and is available
      * through the Web container.
      * </p>
      * 
      * <p>
      * Test cases may seed the request object with a Mock WebContainer and a
      * Mock ValidationProcessor.
      * </p>
      *
      * @param request HttpServletRequest used to aquire servlet context
      *
      * @return ValidationProcessor instance for the current Web Application
      */
     public static ValidationProcessor getValidationProcessor(HttpServletRequest request) {
         return  getGeoServer(request).getProcessor();
     }
 
     public static String getBaseUrl(HttpServletRequest httpServletRequest) {
         return "http://" + httpServletRequest.getServerName() + ":"
         + httpServletRequest.getServerPort() + "/geoserver/";
     }
 
     /**
      * Aquire type safe session information in a UserContainer.
      * 
      * <p>
      * Please note that the UserContainer may be lazyly created.
      * </p>
      *
      * @param request Http Request used to aquire session reference
      *
      * @return UserContainer containing typesafe session information.
      */
     public static UserContainer getUserContainer(HttpServletRequest request) {
         HttpSession session = request.getSession();
 
         synchronized (session) {
             UserContainer user = (UserContainer) session.getAttribute(UserContainer.SESSION_KEY);
 
             if (user == null) {
                 user = new UserContainer(request.getLocale());
                 user.setUsername("Administrator");
 
                 session.setAttribute(UserContainer.SESSION_KEY, user);
             }
 
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
