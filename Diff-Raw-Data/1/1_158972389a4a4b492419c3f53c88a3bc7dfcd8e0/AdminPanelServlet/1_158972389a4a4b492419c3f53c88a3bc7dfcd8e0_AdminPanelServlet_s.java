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
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.daxplore.presenter.server.throwable.InternalServerException;
 
 @SuppressWarnings("serial")
 public class AdminPanelServlet extends HttpServlet {
 	protected static Logger logger = Logger.getLogger(AdminPanelServlet.class.getName());
 	protected static String adminHtmlTemplate = null;
 	
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
 		try {
 				Locale locale = new Locale("en");
 				String pageTitle = "Daxplore Admin";
 				
 				if (adminHtmlTemplate == null) {
 					try {
 						adminHtmlTemplate = IOUtils.toString(getServletContext().getResourceAsStream("/templates/admin.html"));
 					} catch (IOException e) {
 						throw new InternalServerException("Failed to load the html admin template", e);
 					}
 				}
 				
 				String baseurl = request.getRequestURL().toString();
				baseurl = baseurl.substring(0, baseurl.lastIndexOf("/"));
 				baseurl = baseurl.substring(0, baseurl.lastIndexOf("/")+1);
 				
 				String[] arguments = {
 						locale.toLanguageTag(), // {0}
 						pageTitle,				// {1}
 						baseurl					// {2}
 						};
 				
 			try {
 				Writer writer = response.getWriter();
 				writer.write(MessageFormat.format(adminHtmlTemplate, (Object[])arguments));
 				writer.close();
 			} catch (IOException e) {
 				throw new InternalServerException(e);
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
