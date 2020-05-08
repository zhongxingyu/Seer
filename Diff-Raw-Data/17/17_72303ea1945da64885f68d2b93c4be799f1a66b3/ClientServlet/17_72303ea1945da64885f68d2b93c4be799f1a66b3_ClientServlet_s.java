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
 
 import no.feide.moria.controller.AuthorizationException;
 import no.feide.moria.controller.IllegalInputException;
 import no.feide.moria.controller.MoriaController;
 import no.feide.moria.controller.InoperableStateException;
 import no.feide.moria.controller.MoriaControllerException;
 import no.feide.moria.log.MessageLogger;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public class ClientServlet
 extends HttpServlet {
 
     /** Used for logging. */
     private final MessageLogger log = new MessageLogger(ClientServlet.class);
 
 
     /**
      * Handles the GET requests.
      * @param request
      *            The HTTP request object. If it contains a request parameter
      *            <i>moriaID </i> then the request's attribute <i>attributes
      *            </i> will be filled with the attributes contained in the
      *            session given by <i>moriaID </i>.
      * @param response
      *            The HTTP response object.
      * @throws java.io.IOException
      *             If an input or output error is detected when the servlet
      *             handles the GET request.
      * @throws javax.servlet.ServletException
      *             If the request for the GET could not be handled.
      * @see javax.servlet.http.HttpServlet.doGet(javax.servlet.http.HttpServletRequest,
      *      javax.servlet.http.HttpServletResponse)
      */
     public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
     throws IOException, ServletException {
         
         // Do we have a ticket?
         String loginTicketId = request.getParameter("moriaID");
         if (loginTicketId != null) {
             try {
 
                 // Ticket found. Use it to retrieve previously requested
                 // attributes.
                 final Map attributes = MoriaController.getUserAttributes(loginTicketId, "test");
                 log.logInfo("User attributes are " + attributes.toString());
                 request.setAttribute("attributes", attributes);
 
             } catch (MoriaControllerException e) {
                 log.logCritical("Exception caught reading attributes", e);
                request.setAttribute("error", e);
             }
         }
 
         // Forward the GET request.
         RequestDispatcher rd = getServletContext().getNamedDispatcher("Client.JSP");
         rd.forward(request, response);
 
     }
 
 
     /**
      * Handles POST requests.
      * @param request
      *            The HTTP request object.
      * @param response
      *            The HTTP response object.
      * @throws java.io.IOException
      *             If an input or output error is detected when the servlet
      *             handles the GET request.
      * @throws javax.servlet.ServletException
      *             If the request for the GET could not be handled.
      */
     public final void doPost(final HttpServletRequest request, final HttpServletResponse response)
     throws IOException, ServletException {
         
         log.logInfo("doPost");
 
         String jspLocation = getServletContext().getInitParameter("jsp.location");
         log.logCritical("jsp.location is '" + jspLocation + "'");
         String moriaID = null;
         boolean error = false;
 
         try {
 
             MoriaController.initController(getServletContext());
             log.logCritical("Requested attributes: " + request.getParameter("attributes"));
             log.logCritical("URL prefix: " + request.getParameter("urlPrefix"));
             log.logCritical("URL postfix: " + request.getParameter("urlPostfix"));
             log.logCritical("Principal: " + request.getParameter("principal"));
             moriaID = MoriaController.initiateAuthentication(request.getParameter("attributes").split(","), request.getParameter("urlPrefix"), request.getParameter("urlPostfix"), false, request.getParameter("principal"));
             log.logCritical("Moria ID is now " + moriaID);
 
         } catch (IllegalInputException e) {
            error = true;
             log.logCritical("IllegalInputException");
            request.setAttribute("error", e);
         } catch (AuthorizationException e) {
            error = true;
             log.logCritical("AuthorizationException");
            request.setAttribute("error", e);
         } catch (InoperableStateException e) {
            error = true;
             log.logCritical("InoperableStateException");
            request.setAttribute("error", e);
         }
 
         if (!error) {
             Properties config = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
             String redirectURL = config.getProperty(RequestUtil.PROP_LOGIN_URL_PREFIX) + "?" + config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM) + "=" + moriaID;
             log.logCritical("Redirect URL: " + redirectURL);
             response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
             response.setHeader("Location", redirectURL);
         } else {
             log.logCritical("error!");
             RequestDispatcher rd = getServletContext().getRequestDispatcher(jspLocation + "/client.jsp");
             rd.include(request, response);
         }
     }
 }
