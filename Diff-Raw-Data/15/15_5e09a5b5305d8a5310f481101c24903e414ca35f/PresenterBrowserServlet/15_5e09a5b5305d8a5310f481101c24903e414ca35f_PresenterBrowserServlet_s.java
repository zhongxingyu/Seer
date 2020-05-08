 /*
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.server.servlets;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.text.MessageFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.daxplore.presenter.server.throwable.InternalServerException;
 
 @SuppressWarnings("serial")
 public class PresenterBrowserServlet extends HttpServlet {
 	private static Logger logger = Logger.getLogger(PresenterBrowserServlet.class.getName());
	
 	private static String welcomeTemplate = null;
 	
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
 		try {
 			if (welcomeTemplate == null) {
 				try {
 					welcomeTemplate = IOUtils.toString(getServletContext().getResourceAsStream("/templates/welcome.html"));
 				} catch (IOException e) {
 					throw new InternalServerException("Failed to load the welcome template", e);
 				}
 			}
 			
			String baseurl = request.getRequestURL().toString();
			baseurl = baseurl.substring(0, baseurl.lastIndexOf("/"));
			baseurl = baseurl.substring(0, baseurl.lastIndexOf("/")+1);
 			
 			String[] arguments = {
 					baseurl
 				};
 			
 			String responseHTML = MessageFormat.format(welcomeTemplate, (Object[])arguments);
 			
 			response.setContentType("text/html; charset=UTF-8");
 			try (Writer writer = response.getWriter()) {
 				writer.write(responseHTML);
 			} catch (IOException e) {
 				throw new InternalServerException("Failed to display presenter servlet", e);
 			}
 		} catch (InternalServerException e) {
 			logger.log(Level.SEVERE, e.getMessage(), e);
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Unexpected exception: " + e.getMessage(), e);
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 }
