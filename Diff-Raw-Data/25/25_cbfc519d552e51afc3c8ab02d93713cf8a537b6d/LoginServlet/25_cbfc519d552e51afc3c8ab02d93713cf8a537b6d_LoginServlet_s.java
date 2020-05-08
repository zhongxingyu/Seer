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
 
 import no.feide.moria.controller.IllegalInputException;
 import no.feide.moria.controller.InoperableStateException;
 import no.feide.moria.controller.MoriaController;
 import no.feide.moria.controller.UnknownTicketException;
 import no.feide.moria.controller.AuthenticationException;
 import no.feide.moria.controller.DirectoryUnavailableException;
 import no.feide.moria.controller.MoriaControllerException;
 import no.feide.moria.log.MessageLogger;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.UnavailableException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Cookie;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.Map;
 
 /**
  * Use this servlet to bootstrap the system. Set &lt;load-on-startup&gt;1&lt;/load-on-startup&gt; in web.xml.
  *
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public class LoginServlet extends HttpServlet {
 
     MessageLogger messageLogger = new MessageLogger(LoginServlet.class);
 
     /**
      * Intitiates the controller.
      */
     public void init() throws UnavailableException {
         try {
             MoriaController.initController(getServletContext());
         } catch (Exception e) {
             String message = "Controller initialization failed";
             messageLogger.logCritical(message, e);
             throw new UnavailableException(message + ": " + e.getMessage());
         }
     }
 
     /**
      * Handles the GET requests.
      *
      * @param request  the HTTP request object
      * @param response the HTTP response object
      * @throws IOException
      * @throws ServletException
      */
     public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
             throws IOException, ServletException {
         Properties config = getConfig();
 
         /* Public computer (deny SSO)? */
        String denySSOChoice = "false";
        if (request.getCookies() != null) {
            denySSOChoice = RequestUtil.getCookieValue(config.getProperty(RequestUtil.PROP_COOKIE_DENYSSO),
                                                       request.getCookies());
        }
         boolean denySSO;
         if (denySSOChoice == null || denySSOChoice.equals("false") || denySSOChoice.equals("")) {
             request.setAttribute(RequestUtil.ATTR_SELECTED_DENYSSO, new Boolean(false));
             denySSO = false;
         } else {
             request.setAttribute(RequestUtil.ATTR_SELECTED_DENYSSO, new Boolean(true));
             denySSO = true;
         }
 
         /* Single Sign On */
         if (!denySSO) {
             String serviceTicket;
             try {
                 String loginTicketId = request.getParameter(config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM));
                 String ssoTicketId = RequestUtil.getCookieValue(config.getProperty(RequestUtil.PROP_COOKIE_SSO),
                                                                 request.getCookies());
                serviceTicket = MoriaController.attemptSingleSignOn(loginTicketId, ssoTicketId);
 
                /* Redirect back to web service */
                response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                response.setHeader("Location", MoriaController.getRedirectURL(serviceTicket));
 
                /* Single Sign On succeeded, we're finished. */
                return;
             } catch (UnknownTicketException e) {
                 /* Single Sing On failed, continue with normal authentication */
             } catch (MoriaControllerException e) {
                 /* Do not handle this exception here. Will be handeled by the showLoginPage method. */
             }
         }
 
         /* Display login page */
         showLoginPage(request, response, null);
     }
 
     public final void doPost(final HttpServletRequest request, final HttpServletResponse response)
             throws IOException, ServletException {
         Properties config = getConfig();
 
         /* Login ticket */
         String loginTicketId = request.getParameter(config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM));
 
         /* SSO ticket */
         String ssoTicketId = request.getParameter(config.getProperty(RequestUtil.PROP_COOKIE_SSO));
 
         String username = request.getParameter(RequestUtil.PARAM_USERNAME);
         String password = request.getParameter(RequestUtil.PARAM_PASSWORD);
         String org = request.getParameter(RequestUtil.PARAM_ORG);
 
         /* Wash SSO selection input */
         String denySSOStr = request.getParameter(RequestUtil.PARAM_DENYSSO);
         boolean denySSO = (denySSOStr != null && denySSOStr.equals("true"));
         if (denySSO) {
             denySSOStr = "true";
         } else {
             denySSOStr = "false";
         }
 
         /* Set cookie and mark request for SSO denial */
         Cookie denySSOCookie =
                 RequestUtil.createCookie((String) config.get(RequestUtil.PROP_COOKIE_DENYSSO), denySSOStr,
                                          new Integer((String) config.get(RequestUtil.PROP_COOKIE_DENYSSO_TTL)).intValue());
         response.addCookie(denySSOCookie);
         request.setAttribute(RequestUtil.ATTR_SELECTED_DENYSSO, new Boolean(denySSO));
 
         /* Parse username */
         if (username.indexOf("@") != -1) {
             org = username.substring(username.indexOf("@") + 1, username.length());
         }
 
         /* Validate organization */
         if (org == null || org.equals("") || org.equals("null")) {
             showLoginPage(request, response, RequestUtil.ERROR_NO_ORG);
             return;
         } else if (!RequestUtil.parseConfig(getConfig(), RequestUtil.PROP_ORG,
                                             (String) config.get(RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE))
                 .containsValue(org)) {
             showLoginPage(request, response, RequestUtil.ERROR_INVALID_ORG);
             return;
         }
 
         /* Attempt login */
         Map tickets;
         String redirectURL;
         try {
             tickets = MoriaController.attemptLogin(loginTicketId, ssoTicketId, username + "@" + org, password);
             redirectURL = MoriaController.getRedirectURL((String) tickets.get(MoriaController.SERVICE_TICKET));
         } catch (AuthenticationException e) {
             showLoginPage(request, response, RequestUtil.ERROR_AUTHENTICATION_FAILED);
             return;
         } catch (UnknownTicketException e) {
             showLoginPage(request, response, RequestUtil.ERROR_UNKNOWN_TICKET);
             return;
         } catch (DirectoryUnavailableException e) {
             showLoginPage(request, response, RequestUtil.ERROR_DIRECTORY_DOWN);
             return;
         } catch (InoperableStateException e) {
             showLoginPage(request, response, RequestUtil.ERROR_MORIA_DOWN);
             return;
         } catch (IllegalInputException e) {
             showLoginPage(request, response, RequestUtil.ERROR_NO_CREDENTIALS);
             return;
             /* This should not happen. Programming error. */
         }
 
         /* Authentication has been successful. Remember SSO ticket and organization. */
         Cookie orgCookie =
                 RequestUtil.createCookie(RequestUtil.PROP_COOKIE_ORG,
                                          request.getParameter(RequestUtil.PARAM_ORG),
                                          new Integer((String) config.get(RequestUtil.PROP_COOKIE_LANG_TTL)).intValue());
         response.addCookie(orgCookie);
 
         if (!denySSO) {
             Cookie ssoTicketCookie =
                     RequestUtil.createCookie((String) config.get(RequestUtil.PROP_COOKIE_SSO),
                                              (String) tickets.get(MoriaController.SSO_TICKET),
                                              new Integer((String) config.get(RequestUtil.PROP_COOKIE_SSO_TTL)).intValue());
             response.addCookie(ssoTicketCookie);
         }
 
         /* Redirect back to web service */
         response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
         response.setHeader("Location", redirectURL);
     }
 
     /**
      * Displays the login page. The method fills the request object with values and then pass the request to the jsp.
      *
      * @param request
      * @param response
      * @throws IOException
      * @throws ServletException
      */
     private void showLoginPage(final HttpServletRequest request, final HttpServletResponse response, String errorType)
             throws IOException, ServletException {
 
         Properties config = getConfig();
         HashMap serviceProperties = null;
 
 
         /* Ticket */
         String loginTicketId = request.getParameter(config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM));
 
         /* Base URL */
         request.setAttribute(RequestUtil.ATTR_BASE_URL,
                              config.getProperty(RequestUtil.PROP_LOGIN_URL_PREFIX) + "?"
                              + config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM)
                              + "="
                              + loginTicketId);
 
         try {
             /* Service properties */
             serviceProperties = MoriaController.getServiceProperties(loginTicketId);
             /* Seclevel */
             request.setAttribute(RequestUtil.ATTR_SEC_LEVEL, "" + MoriaController.getSecLevel(loginTicketId));
         } catch (UnknownTicketException e) {
             errorType = RequestUtil.ERROR_UNKNOWN_TICKET;
         } catch (IllegalInputException e) {
             errorType = RequestUtil.ERROR_NO_CREDENTIALS;
         } catch (InoperableStateException e) {
             errorType = RequestUtil.ERROR_MORIA_DOWN;
         }
 
         /* Error message */
         request.setAttribute(RequestUtil.ATTR_ERROR_TYPE, errorType);
 
         /* Resource bundle */
         String serviceLang = null;
         if (serviceProperties != null) {
             serviceLang = (String) serviceProperties.get(RequestUtil.CONFIG_LANG);
         }
         String langFromCookie = null;
         if (request.getCookies() != null) {
             langFromCookie = RequestUtil.getCookieValue((String) config.get(RequestUtil.PROP_COOKIE_LANG),
                                                         request.getCookies());
         }
         ResourceBundle bundle = RequestUtil.getBundle(RequestUtil.BUNDLE_LOGIN,
                                                       request.getParameter(RequestUtil.PARAM_LANG),
                                                       langFromCookie,
                                                       serviceLang,
                                                       request.getHeader("Accept-Language"),
                                                       (String) config.get(RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE));
         request.setAttribute(RequestUtil.ATTR_BUNDLE, bundle);
 
         /* Configured values */
         request.setAttribute(RequestUtil.ATTR_ORGANIZATIONS,
                              RequestUtil.parseConfig(getConfig(), RequestUtil.PROP_ORG,
                                                      bundle.getLocale().getLanguage()));
         request.setAttribute(RequestUtil.ATTR_LANGUAGES,
                              RequestUtil.parseConfig(getConfig(), RequestUtil.PROP_LANGUAGE, RequestUtil.PROP_COMMON));
 
         /* Selected realm */
         String selectedOrg = request.getParameter(RequestUtil.PARAM_ORG);
         if (selectedOrg == null || selectedOrg.equals("")) {
 
             if (request.getCookies() != null) {
                 selectedOrg = RequestUtil.getCookieValue(RequestUtil.PROP_COOKIE_ORG, request.getCookies());
             }
 
             if (selectedOrg == null || selectedOrg.equals("")) {
                 if (serviceProperties != null) {
                     selectedOrg = (String) serviceProperties.get(RequestUtil.CONFIG_HOME);
                 }
             }
         }
         request.setAttribute(RequestUtil.ATTR_SELECTED_ORG, selectedOrg);
 
 
         /* Selected language */
         request.setAttribute(RequestUtil.ATTR_SELECTED_LANG, bundle.getLocale());
         if (request.getParameter(RequestUtil.PARAM_LANG) != null) {
             response.addCookie(
                     RequestUtil.createCookie((String) config.get(RequestUtil.PROP_COOKIE_LANG),
                                              request.getParameter(RequestUtil.PARAM_LANG),
                                              new Integer((String) config.get(RequestUtil.PROP_COOKIE_LANG_TTL)).intValue()));
         }
 
         /* Service attributes */
         if (serviceProperties != null) {
             request.setAttribute(RequestUtil.ATTR_CLIENT_NAME, serviceProperties.get(RequestUtil.CONFIG_DISPLAY_NAME));
             request.setAttribute(RequestUtil.ATTR_CLIENT_URL, serviceProperties.get(RequestUtil.CONFIG_URL));
         }
 
         /* Process jsp */
         RequestDispatcher rd = getServletContext().getNamedDispatcher("Login.JSP");
         rd.forward(request, response);
     }
 
     /**
      * Get the config from the context. The configuration is expected to be set by the controller before requests are
      * sent to this servlet.
      *
      * @return the configuration
      * @throws IllegalStateException if the config is not set in the context
      */
     private Properties getConfig() {
         Properties config;
 
         /* Validate config */
         try {
             config = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
         } catch (ClassCastException e) {
             throw new IllegalStateException("Config is not correctly set in context.");
         }
         if (config == null) {
             throw new IllegalStateException("Config is not set in context.");
         }
 
         return config;
     }
 }
