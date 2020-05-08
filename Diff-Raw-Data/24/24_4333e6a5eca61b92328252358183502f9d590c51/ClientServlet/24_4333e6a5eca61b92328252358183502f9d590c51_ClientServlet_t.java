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
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public class ClientServlet extends HttpServlet {
 
     /**
      * Handles the GET requests.
      *
      * @param request  the HTTP request object
      * @param response the HTTP response object
      * @throws java.io.IOException
      * @throws javax.servlet.ServletException
      */
     public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
             throws IOException, ServletException {
 
       // Do not have ticket
         // - Contact dsssfsd
         if (request.getParameter(RequestUtil.PROP_LOGIN_TICKET_PARAM) == null) {
            /* Process jsp */
            RequestDispatcher rd = getServletContext().getNamedDispatcher("Client.JSP");
            rd.forward(request, response);
         }
 
 
         // Have ticket
         // - show
 
 
     }
 
     public final void doPost(final HttpServletRequest request, final HttpServletResponse response)
             throws IOException, ServletException {
         // TODO: Do not throw exceptions, set INTERNAL SERVER ERRROR status
         String jspLocation = getServletContext().getInitParameter("jsp.location");
         String moriaID = null;
         boolean error = false;
 
         try {
             MoriaController.initController(getServletContext());
             moriaID = MoriaController.initiateAuthentication(request.getParameter("attributes").split(","),
                     request.getParameter("urlPrefix"),
                     request.getParameter("urlPostfix"),
                     false,
                     request.getParameter("principal"));
         } catch (IllegalInputException e) {
             // TODO:  Set error message
             error = true;
             request.setAttribute("error", e);
 
         } catch (AuthorizationException e) {
             error = true;
             request.setAttribute("error", e);
         } catch (InoperableStateException e) {
             error = true;
             request.setAttribute("error", e);
         }
 
         if (!error) {
             Properties config = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
             String redirectURL = config.getProperty(RequestUtil.PROP_LOGIN_URL_PREFIX) + "?" + config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM) + "=" + moriaID;
             response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
             response.setHeader("Location", redirectURL);
         } else {
             RequestDispatcher rd = getServletContext().getRequestDispatcher(jspLocation + "/client.jsp");
             rd.include(request, response);
         }
     }
 }
