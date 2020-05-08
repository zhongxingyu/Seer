 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.user.controller;
 
 import static br.octahedron.cotopaxi.CotopaxiProperty.getProperty;
 import br.octahedron.cotopaxi.auth.AbstractGoogleAuthenticationInterceptor;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.figgo.modules.user.manager.UserManager;
 import br.octahedron.util.Log;
 /**
  * AuthenticationInterceptor for figgo. Uses the Google for authentication and the internal
  * mechanism to check if user is valid
  * 
  * @author Danilo Queiroz
  */
 public class AuthenticationInterceptor extends AbstractGoogleAuthenticationInterceptor {
 	
	private static final String NEW_USER_URL = "/users/new";
 	
 	@Inject
 	private UserManager usersManager;
 	
 
 	public AuthenticationInterceptor() {
 		super(new Log(AuthenticationInterceptor.class), getProperty("AUTHORIZATION_DOMAIN"));
 	}
 
 	/**
 	 * @param usersManager the usersManager to set
 	 */
 	public void setUserManager(UserManager usersManager) {
 		this.usersManager = usersManager;
 	}
 	
 	@Override
 	protected void checkUserValidation() {
 		String userEmail = this.currentUser();
 		if (!this.usersManager.existsUser(userEmail)) {
 			log.debug("User doesn't exist. It's a new/invalid user");
 			redirect(NEW_USER_URL);
 		} else {
 			out("user", this.usersManager.getUser(userEmail));
 		}
 	}
 }
