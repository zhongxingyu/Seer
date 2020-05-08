 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
  * Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
  * Use is subject to license terms.                                            *
  * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.lzproject.security;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.RequestDispatcher;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.openlaszlo.lzproject.Common;
 import org.openlaszlo.lzproject.model.User;
 
 /**
  *
  * @web.filter name="SecurityFilter" display-name = "This filter checks if the
  *             request is made to one of the protected web services, making
  *             sure, only authorized requests get through" load-on-startup = "1"
  * @web.filter-mapping url-pattern="/lzproject/rest/*"
  *
  * @author $Author: raju $
  * @version $Version$
  */
 public class SecurityFilter implements Filter {
 
     /**
      * Log4J logger
      */
     protected Logger log = Logger.getLogger(SecurityFilter.class);
 
     /**
      * The JSP page used in case of an unauthorized access.
      */
     private final static String UNAUTHORIZED_JSP = Common.LZPROJECTROOT + "/lzproject/webservice/UnauthorizedAccess.jsp";
 
     /**
      * Initialize the filter.
      */
     public void init(FilterConfig config) throws ServletException {
         log.debug("Security filter initialized");
     }
 
     /**
      * Security filter for the LZProject web services. Only lets the application/i18n
      * and the user/login command pass through when there's not valid user in the
      * session.
      *
      * @param request
      *            ServletRequest object
      * @param response
      *            ServletResponse object
      *
      */
     public void doFilter(ServletRequest request, ServletResponse response,
             FilterChain filterChain) throws IOException, ServletException {
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpSession session = httpRequest.getSession();
 
         // Get some information on client and the request
         if (log.isDebugEnabled()) {
             log.debug("Client IP: " + httpRequest.getRemoteAddr());
             log.debug("Client host: " + httpRequest.getRemoteHost());
             log.debug("Request method: " + httpRequest.getMethod());
             log.debug("Request URI: " + httpRequest.getRequestURI());
             log.debug("Session's max inactive interval: " + session.getMaxInactiveInterval());
         }
 
         // Try to retrieve a user object from the session
         User user = (User) session.getAttribute("user");
 
         // if we have a user object in the session, every request is valid
         if ( user != null) {
             // Just let the request pass through
             filterChain.doFilter(request, response);
         } else {
             // Get the command for the web service
             String command = this.getRestMethod(httpRequest.getRequestURI(),
                     httpRequest.getContextPath());
             // Only "appliation/i18n" and "user/login" are allowed
             if (command.equals("application/i18n") || command.equals("logout")
                     || command.equals("user/login")) {
                 filterChain.doFilter(request, response);
             } else {
                 // This access is not authorized, forward to UNAUTHORIZED_JSP
                 log.debug("Unauthorized access for " + httpRequest.getRequestURL());
                 httpRequest.setAttribute("command", command);
                 RequestDispatcher dispatcher = httpRequest.getRequestDispatcher(UNAUTHORIZED_JSP);
                 dispatcher.forward(request, response);
             }
         }
 
     }
 
     /**
      * Stopping filter operation.
      */
     public void destroy() {
         log.debug("Security filter destroyed");
     }
 
     /**
      * Extracts the REST command from the URL.
      *
      * <p>
      * Only calls to the i18n service as well as the login service
      * are valid without authentication. The command is used to identify
      * them.
      *
      * @param url HTTP request URL
      * @param contextPath web application context path
      * @return The REST web service command for this request
      */
     protected String getRestMethod(String url, String contextPath) {
         // Take the last section of the servlet path as the command to execute
        String command = url.replaceAll("\\" + contextPath + "\\/lzproject\\/rest\\/", "");
         return command;
     }
 
 }
