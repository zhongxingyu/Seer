 /**
  * fbtosemweb-webservice - to.networld.fbtosemweb.ws
  *
  * Copyright (C) 2011 by Networld Project
  * Written by Alex Oberhauser <oberhauseralex@networld.to>
  * All Rights Reserved
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, version 3 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of 
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>
  */
 
 package to.networld.fbtosemweb.ws;
 
 import java.io.IOException;
 
import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import to.networld.fbtosemweb.ws.common.ServletHelper;
 
 /**
  * @author Alex Oberhauser
  */
 public class FBLogout extends HttpServlet {
 	private static final long serialVersionUID = -5770420350901027081L;

 	@Override
 	public void doGet(HttpServletRequest _request, HttpServletResponse _response) throws ServletException, IOException {
		ServletContext context = getServletContext();
		String access_token = (String) context.getAttribute("access_token");
 		if ( access_token != null ) {
 			StringBuffer logoutURL = new StringBuffer();
 			logoutURL.append("https://www.facebook.com/logout.php");
 			logoutURL.append("?next=" + ServletHelper.getCurrentUrl(_request, false));
 			logoutURL.append("&" + access_token);
			context.removeAttribute("access_token");
 			_response.sendRedirect(".");
 		} else {
 			_response.sendRedirect(".");
 		}
 	}
 }
