 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
  * Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
  * Use is subject to license terms.                                            *
  * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.lzproject.rest;
 
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 /**
  * BaseRestController is the base class for all REST controllers.
  *
  * @author raju
  */
 public class BaseRestController extends HttpServlet {
 
     /**
      * Log4J logger.
      */
     protected Logger log = Logger.getLogger(BaseRestController.class);
 
     /**
      * The real path, used to access the resource bundle for localization.
      */
     protected String realPath = null;
 
     /**
      * The base name for the resource bundle.
      */
     protected final static String RESOURCE_BASE_NAME = "org.openlaszlo.lzproject.LzTrackMessages";
 
     /**
      * Extracts the name of the REST method from the URI of this request.
      *
      * <p>
      * The structure of all REST request is:<br/>
      * <em>{webbapp name}/lzproject/rest/NOUN/VERB</em><br/>
      *
      * <p>
      * An example for a URI is: /lzproject/rest/task/create, which is used for
      * creating a task.
      *
      * @param url
      *            The URL of this request
      * @param contextPath
      *            The context path (name of the
      * @param noun
      *            The item upon which this operation is executed.
      * @return The REST method extracted from the URL.
      */
     protected String getRestMethod(String url, String contextPath, String noun) {
         // Take the last section of the servlet path as the command to execute
        // /demos/lzproject/lzx/lzproject/rest/user/
         log.debug("ContextPath: " + contextPath);
        String command = url.replaceAll(contextPath + "\\/demos/lzproject/lzx/lzproject\\/rest\\/"
                 + noun + "\\/", "");
         log.debug("REST command: " + noun + "/" + command);
         return command;
     }
 
     /**
      * Load a resource bundle based on the locale object in the HTTP session.
      *
      * <p>
      * Within
      * {@link org.openlaszlo.lzproject.i18n.I18NFilter#doFilter}
      * a locale is stored as a session attribute called <em>myLocale</em>.
      * Here we use this locale to load the corresponding resource bundle.
      *
      * @param request
      *            The HTTP servlet request.
      * @return The resource bundle based on the locale found in the session.
      */
     protected ResourceBundle loadResourceBundle(HttpServletRequest request) {
         // Extract locale from session to load resource bundle
         HttpSession session = request.getSession();
         Locale locale = (Locale) session.getAttribute("myLocale");
         // Load the message bundle for this locale and pass it to the user
         // object
         return ResourceBundle.getBundle(BaseRestController.RESOURCE_BASE_NAME,
                 locale);
     }
 
}
