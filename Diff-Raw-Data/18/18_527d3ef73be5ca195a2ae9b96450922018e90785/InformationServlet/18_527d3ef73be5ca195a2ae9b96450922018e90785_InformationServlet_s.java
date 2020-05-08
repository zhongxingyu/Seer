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
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.Arrays;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.File;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.SAXParser;
 import no.feide.moria.controller.AuthorizationException;
 import no.feide.moria.controller.IllegalInputException;
 import no.feide.moria.controller.InoperableStateException;
 import no.feide.moria.controller.MoriaController;
 import no.feide.moria.controller.UnknownTicketException;
 import no.feide.moria.log.MessageLogger;
 import java.util.Vector;
 
 
 /**
  * This servlet is responsible for retrieving information about a user, and
  * sending it to information.jsp for display.
  *
  * @author Eva Indal
  * @version $Revision$
  */
 public class InformationServlet extends HttpServlet {
 
     /**
      * A hash map containing all possible attributes for a user.
      * Each item in the hashmap maps from an attribute name to an
      * AttribsData class instance
      */
     private HashMap feideattribsStored = null;
     
     /**
      * Monitor for the feideattribs xml file.
      */
     private FileMonitor feideattribsMonitor = null;
 
     /** Principal name of the service.
      *  Current value is "info"
      */
     private static final String PRINCIPAL = "info";
 
     /** Used for logging. */
     private final MessageLogger log = new MessageLogger(InformationServlet.class);
 
     /**
      * List of parameters required by <code>InformationServlet</code>.
      * <br>
      * <br>
      * Current required parameters are:
      * <ul>
      * <li><code>RequestUtil.PROP_COOKIE_LANG</code>
      * <li><code>RequestUtil.PROP_COOKIE_LANG_TTL</code>
      * <li><code>RequestUtil.PROP_COOKIE_DENYSSO</code>
      * <li><code>RequestUtil.PROP_COMMON</code>
      * <li><code>RequestUtil.PROP_LOGIN_TICKET_PARAM</code>
      * <li><code>RequestUtil.PROP_INFORMATION_URL_PREFIX</code>
      * <li><code>RequestUtil.PROP_INFORMATION_FEIDEATTRIBS_XML</code>
      * </ul>
      * @see RequestUtil#PROP_COOKIE_LANG
      * @see RequestUtil#PROP_COOKIE_LANG_TTL
      * @see RequestUtil#PROP_COOKIE_DENYSSO
      * @see RequestUtil#PROP_COMMON
      * @see RequestUtil#PROP_LOGIN_TICKET_PARAM
      * @see RequestUtil#PROP_INFORMATION_URL_PREFIX
      * @see RequestUtil#PROP_INFORMATION_FEIDEATTRIBS_XML
      */
     private static final String[] REQUIRED_PARAMETERS = {
         RequestUtil.PROP_COOKIE_LANG,
         RequestUtil.PROP_COOKIE_LANG_TTL,
         RequestUtil.PROP_COOKIE_DENYSSO,
         RequestUtil.PROP_COMMON,
         RequestUtil.PROP_LOGIN_TICKET_PARAM,
         RequestUtil.PROP_INFORMATION_URL_PREFIX,
         RequestUtil.PROP_INFORMATION_FEIDEATTRIBS_XML
     };
 
     /**
      * Constructor.
      */
     public InformationServlet() {
     }
 
     /**
      * Implements a simple xml parser that parses the feideattribs.xml file
      * into a HashMap with AttribsData instances.
      *
      * @see AttribsHandler
      *      AttribsData
      */
     public final synchronized HashMap getAttribs() {
          if (feideattribsMonitor == null || feideattribsMonitor.hasChanged()) {
           Properties config = getConfig();
           if (config != null) {
             AttribsHandler handler = new AttribsHandler();
             SAXParserFactory factory = SAXParserFactory.newInstance();
             try {
                String filename = (String) config.get(RequestUtil.PROP_INFORMATION_FEIDEATTRIBS_XML);
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(new File(filename), handler);
                feideattribsMonitor = new FileMonitor(filename);
             } catch (Throwable t) {
              log.logCritical("Error parsing feideattribs.xml");
             } finally {
               feideattribsStored = handler.getAttribs();
             }
           }
         }
         return feideattribsStored;
     }
 
     /**
      * Generates table for a user. The vector consists of rows of data. Each row
      * has four columns. The first column is the URL link for the user attribute. The
      * second column is the attribute description as presented to the user. The
      * third column is the actual data stored for the attribute, and the fourth
      * columns is either fd_mandatory or fd_optional, as a key for a mandatory
      * or optional attribute.
      *
      * @param userData The user data.
      * @param bundle Resource bundle for language.
      * @return Vector with table data.
      */
     private Vector printTableToVector(final Map userData, final ResourceBundle bundle) {
 
         /* Stores the data in a temporary vector */
         Vector temp = new Vector();
         HashMap feideattribs = getAttribs();
         for (Iterator iterator = feideattribs.keySet().iterator(); iterator.hasNext();) {
             String key = (String) iterator.next();
             AttribsData attribsData = (AttribsData) feideattribs.get(key);
             temp.add(attribsData);
         }
         /* Sorts the description fields according to the xml file */
         Object[] attribsArray = temp.toArray();
         Arrays.sort(attribsArray, new AttribsData(0));
 
         Vector out = new Vector();
 
         /* Fetch the data to print */
         int n = temp.size();
         for (int i = 0; i < n; i++) {
             AttribsData adata = (AttribsData) attribsArray[i];
             String key2 = (String) adata.getData("key");
             boolean hasuserdata = userData.containsKey(key2);
            // Vector userdata = (Vector) userData.get(key2);
             String[] userdata = (String[]) userData.get(key2);
 
             String description = adata.getData("description");
             String bundleid = adata.getData("resourcebundle");
             String bundlename = (String) bundle.getString(bundleid);
             /* see if resourcebundle has a name */
             if (bundlename != null)
                 description = bundlename;
             String link = adata.getData("link");
             String relevance = adata.getData("relevance");
 
             String userstring = "";
             String relevanceString = null;
 
             if (userdata != null) {
                 for (int j = 0; j < userdata.length; j++) {
                     userstring += userdata[j];
                     userstring += "<BR>";
                 }
             }
             /*
              * Checks if the data is mandatory or optional for printing in the
              * right language
              */
             if (relevance.equals("Mandatory")) {
                 // set to bundle name
                 relevanceString = "fd_mandatory";
             } else {
                 // set to bundle name
                 relevanceString = "fd_optional";
             }
             out.add(link);
             out.add(description);
             out.add(userstring);
             out.add(relevanceString);
         }
         return out;
     }
 
 
     /**
      * Get the config from the context. The configuration is expected to be set
      * by the controller before requests are sent to this servlet.
      *
      * @return The configuration.
      * @throws IllegalStateException
      *          If the config is not properly set.
      *
      */
     private Properties getConfig() {
         final Properties config;
 
         /* Validate config */
         try {
             config = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
         } catch (ClassCastException e) {
             throw new IllegalStateException("Config is not correctly set in context.");
         }
         if (config == null)
             throw new IllegalStateException("Config is not set in context.");
 
         // Are we missing some required properties?
         for (int i = 0; i < REQUIRED_PARAMETERS.length; i++) {
             String requiredParameter = REQUIRED_PARAMETERS[i];
             if ((requiredParameter == null) || (requiredParameter.equals(""))) {
                     throw new IllegalStateException("Required parameter '" + requiredParameter + "' is not set");
             }
         }
         return config;
 
     }
 
     /**
      * Implements the HttpServlet.doGet method.
      *
      * @param request   The HTTP request.
      * @param response  The HTTP response.
      * @throws IOException
      *           If getAttribs returns null.
      * @throws ServletException
      *           If the Moria controller throws an exception.
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
             throws ServletException, IOException {
         if (getAttribs() == null) throw new IOException("feideattribs.xml not parsed");
 
         String ticketId = request.getParameter("moriaID");
 
         if (request.getParameter("logout") != null) {
             log.logInfo("Logout received");
             final RequestDispatcher rd = getServletContext().getNamedDispatcher("Logout");
             rd.forward(request, response);
             return;
         }
 
         /*
          * Makes a Properties object named config, which gets the config
          * from the getConfig() method
          */
         Properties config;
 
         try {
             config = getConfig();
         } catch (IllegalStateException e) {
             config = null;
         }
 
         /* Resource bundle. */
         String langFromCookie = null;
         if (config != null && request.getCookies() != null) {
             langFromCookie = RequestUtil.getCookieValue((String) config.get(RequestUtil.PROP_COOKIE_LANG), request.getCookies());
         }
 
         /*Use default login language as default for the information servlet */
         final ResourceBundle bundle = RequestUtil.getBundle(
                 RequestUtil.BUNDLE_INFORMATIONSERVLET,
                 request.getParameter(RequestUtil.PARAM_LANG), langFromCookie, null,
                 request.getHeader("Accept-Language"), (String) config.get(RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE));
 
         HttpSession session = request.getSession(true);
 
         request.setAttribute("bundle", bundle);
        request.setAttribute("urlPrefix", config.get(RequestUtil.PROP_INFORMATION_URL_PREFIX));

         // update cookie if language has changed
         boolean changelanguage = false;
         if (request.getParameter(RequestUtil.PARAM_LANG) != null) {
             response.addCookie(RequestUtil.createCookie(
                     (String) config.get(RequestUtil.PROP_COOKIE_LANG),
                     request.getParameter(RequestUtil.PARAM_LANG),
                     new Integer((String) config.get(RequestUtil.PROP_COOKIE_LANG_TTL)).intValue()));
             changelanguage = true;
         }
         Map userData = null;
         // to change language, we have to get a new ticket from the LoginServlet
         if (ticketId != null && !changelanguage) {
             try {
                 userData = MoriaController.getUserAttributes(ticketId, PRINCIPAL);
             } catch (AuthorizationException e) {
                 log.logCritical("AuthorizationException");
                 throw new ServletException(e);
             } catch (IllegalInputException e) {
                 log.logCritical("IllegalInputException");
                 throw new ServletException(e);
             } catch (UnknownTicketException e) {
                 log.logCritical("UnknownTicketException");
                 throw new ServletException(e);
             } catch (InoperableStateException e) {
                 log.logCritical("InoperableStateException");
                 throw new ServletException(e);
             }
         }
         if (userData != null) {
             // need userorg as an attribute in the JSP to be able to print
             // instructions on where to update the optional or mandatory info
             String [] userorgarray = (String[]) userData.get(RequestUtil.EDU_ORG_LEGAL_NAME);
             String userorg = bundle.getString("unknown_userorg");
             if (userorgarray != null && userorgarray.length > 0) {
                 userorg = userorgarray[0];
             }
             request.setAttribute("userorg", userorg);
 
             Vector tabledata = printTableToVector(userData, bundle);
             request.setAttribute("tabledata", tabledata);
 
             /* Configured values */
             request.setAttribute(RequestUtil.ATTR_ORGANIZATIONS,
                                  RequestUtil.parseConfig(config, RequestUtil.PROP_ORG, bundle.getLocale().getLanguage()));
             request.setAttribute(RequestUtil.ATTR_LANGUAGES,
                                  RequestUtil.parseConfig(config, RequestUtil.PROP_LANGUAGE, RequestUtil.PROP_COMMON));
             request.setAttribute(RequestUtil.ATTR_BASE_URL,
                                  config.getProperty(RequestUtil.PROP_INFORMATION_URL_PREFIX) + "?"
                                  + config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM) + "=" + ticketId);
             request.setAttribute(RequestUtil.ATTR_SELECTED_LANG, bundle.getLocale());
 
             // only print language menu if SSO is enabled
             final String denySSOChoice = RequestUtil.getCookieValue(config.getProperty(RequestUtil.PROP_COOKIE_DENYSSO), request.getCookies());
             final boolean denySSO;
             if (denySSOChoice == null || denySSOChoice.equals("false") || denySSOChoice.equals("")) {
                 // Allow SSO.
                 request.setAttribute(RequestUtil.ATTR_SELECTED_DENYSSO, new Boolean(false));
                 denySSO = false;
 
             } else {
                 // Deny SSO.
                 request.setAttribute(RequestUtil.ATTR_SELECTED_DENYSSO, new Boolean(true));
                 denySSO = true;
             }
             // print the table in the JSP
             final RequestDispatcher rd = getServletContext().getNamedDispatcher("Information.JSP");
             rd.forward(request, response);
         } else {
             // call doPost() to get the user attributes from the Controller
             doPost(request, response);
         }
     }
 
     /**
      * Implements the HttpServlet.doPost method.
      *
      * @param request   The HTTP request.
      * @param response  The HTTP response.
      * @throws IOException      Required by interface.
      * @throws ServletException
      *           If the Moria controller throws an exception.
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     public final void doPost(final HttpServletRequest request, final HttpServletResponse response)
     throws ServletException, IOException {
 
         String jspLocation = getServletContext().getInitParameter("jsp.location");
         log.logInfo("jsp.location is '" + jspLocation + "'");
         String moriaID = null;
         boolean error = false;
 
         String attributes = getAllAttributes();
         String urlPrefix = (String) request.getAttribute("urlPrefix") + "?moriaID=";
         String urlPostfix = "";
         String principal = PRINCIPAL;
 
         try {
             MoriaController.initController(getServletContext());
             log.logInfo("Requested attributes: " + attributes);
             log.logInfo("URL prefix: " + urlPrefix);
             log.logInfo("URL postfix: " + urlPostfix);
             log.logInfo("Principal: " + principal);
             moriaID = MoriaController.initiateAuthentication(attributes.split(","),
                                                              urlPrefix, urlPostfix, false, principal);
             log.logInfo("Moria ID is now " + moriaID);
 
         } catch (IllegalInputException e) {
             log.logCritical("IllegalInputException");
             throw new ServletException(e);
         } catch (AuthorizationException e) {
             log.logCritical("AuthorizationException");
             throw new ServletException(e);
         } catch (InoperableStateException e) {
             log.logCritical("InoperableStateException");
             throw new ServletException(e);
         }
 
         if (!error) {
             Properties config = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
             log.logCritical("Configuration: " + config.toString());
             String redirectURL = config.getProperty(RequestUtil.PROP_LOGIN_URL_PREFIX)
                  + "?" + config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM) + "=" + moriaID;
             log.logCritical("Redirect URL: " + redirectURL);
             response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
             response.setHeader("Location", redirectURL);
         } else {
             // should never happen
             throw new ServletException("Unknown error in InformationServlet");
         }
     }
 
     /**
      * Builds a list of all possible user attributes.
      *
      * @return Comma separated list of attributes.
      */
     private String getAllAttributes() {
         String acc = "";
         HashMap feideattribs = getAttribs();
         for (Iterator iterator = feideattribs.keySet().iterator(); iterator.hasNext();) {
             String key = (String) iterator.next();
             acc += key;
             if (iterator.hasNext()) acc += ",";
         }
         return acc;
     }
 }
 
