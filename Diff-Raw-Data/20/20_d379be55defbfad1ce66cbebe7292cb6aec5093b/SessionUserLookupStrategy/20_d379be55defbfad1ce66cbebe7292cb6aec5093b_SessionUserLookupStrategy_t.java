 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi.controller.auth;
 
import java.util.logging.Logger;

 import br.octahedron.cotopaxi.InputHandler;
 
 /**
  * A simple {@link UserLookupStrategy} that tries to retrieve the current user from session.
  * 
  * @author Danilo Penna Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class SessionUserLookupStrategy implements UserLookupStrategy {
 	
	private static final Logger logger = Logger.getLogger(SessionUserLookupStrategy.class.getName());
	
 	/**
 	 * The user property, used as key to the current user on session map. 
 	 */
 	public static final String USER_SESSION_ATTRIBUTE = "CURRENT_USER";
 	
 	public static final String DEFAULT_LOGIN_URL = "/login";
 	public static final String DEFAULT_REDIRECT_ATTRIBUTE = "redirect";
 
 	private String redirectAttribute;
 	private String loginURL;
 	
 	public SessionUserLookupStrategy() {
 		this.loginURL = DEFAULT_LOGIN_URL;
 		this.redirectAttribute = DEFAULT_REDIRECT_ATTRIBUTE;
 	}
 	
 	public SessionUserLookupStrategy(String loginURL, String redirectAttribute) {
 		this.loginURL = loginURL;
 		this.redirectAttribute = redirectAttribute;
 	}
 
 	@Override
 	public UserInfo getCurrentUSer(InputHandler input) {
		logger.fine("Trying to recover User from session");
 		return (UserInfo) input.getSessionParameter(USER_SESSION_ATTRIBUTE);
 	}
 
 	@Override
 	public String getLoginURL(String redirectURL) {
 		String redirect = "";
 		if ( this.redirectAttribute != null) {
 			redirect = "?"+this.redirectAttribute+"="+redirectURL;
 		}
 		return loginURL + redirect ;
 	} 
 }
