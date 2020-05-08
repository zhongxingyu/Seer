 /**
  * cas-webservice - to.networld.fbtosemweb.ws
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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import to.networld.facebook.AppSecretHandler;
 import to.networld.fbtosemweb.ws.common.ServletHelper;
 
 /**
  * @author Alex Oberhauser
  */
 public class FBLogin extends HttpServlet {
 	private static final long serialVersionUID = 9126837487867288807L;
 
 	private static final String APP_ID = AppSecretHandler.getApplicationID();
 	private static final String SECRET= AppSecretHandler.getApplicationSecret();
	
	public static final String SESSION_NAME = "nw_access_token";
 
 	/**
 	 * Get the access_token to be able to access the client information.
 	 * 
 	 * @param _fbcode Code that allows us to get the access_token for the current client.
 	 * @return The access_token (access_token=...)
 	 * @throws IOException
 	 */
 	private static String getAccessToken(String _fbcode, String _callbackURL) throws IOException {
 		StringBuffer fbtokenURL = new StringBuffer();
 		fbtokenURL.append("https://graph.facebook.com/oauth/access_token");
 		fbtokenURL.append("?client_id=" + APP_ID);
 		fbtokenURL.append("&redirect_uri=" + _callbackURL);
 		fbtokenURL.append("&client_secret=" + SECRET);
 		fbtokenURL.append("&code=" + _fbcode);
 		
 		/**
 		 * Authenticate with the credentials received from the client.
 		 */
 		URL authenticateURL = new URL(fbtokenURL.toString());
 		BufferedReader reader = new BufferedReader(new InputStreamReader(authenticateURL.openStream()));
 		String response = reader.readLine();
 		return response.split("&")[0];
 	}
 	
 	@Override
 	public void doGet(HttpServletRequest _request, HttpServletResponse _response) throws ServletException, IOException {
 		String fbcode = _request.getParameter("code");
 		PrintWriter out = _response.getWriter();
 		
		String access_token = (String) _request.getSession().getAttribute(SESSION_NAME);
 		if ( fbcode == null && access_token == null ) {
 			StringBuffer fbcodeURL = new StringBuffer();
 			fbcodeURL.append("https://www.facebook.com/dialog/oauth");
 			fbcodeURL.append("?client_id=" + APP_ID);
 			fbcodeURL.append("&redirect_uri=" + ServletHelper.getCurrentUrl(_request, false));
 
 			_response.sendRedirect(fbcodeURL.toString());
 		} else {
 			if ( access_token == null ) {
 				access_token = getAccessToken(fbcode, ServletHelper.getCurrentUrl(_request, false));
				_request.getSession().setAttribute(SESSION_NAME, access_token);
 			}
 			_response.sendRedirect(".");
 		}
 		
 		out.close();
 	}
 }
