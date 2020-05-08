 /*
  *  Copyright 2009 brh.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package com.melexis;
 
 import com.melexis.repository.UserProfileRepository;
 import org.apache.wicket.Request;
 import org.apache.wicket.authentication.AuthenticatedWebSession;
 import org.apache.wicket.authorization.strategies.role.Roles;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 /**
  *
  * @author brh
  */
 public class LunchAuthenticatedWebSession extends AuthenticatedWebSession {
 
 	@SpringBean
 	private UserProfileRepository userProfileRepository;
 
 	private String username;
 
 	public LunchAuthenticatedWebSession(Request r) {
 		super(r);
 	}
 
 	@Override
 	public boolean authenticate(String username, String password) {
 		this.username = username;
 		return username.equals(password);
 	}
 
 	@Override
 	public Roles getRoles() {
 		if (isSignedIn() && isAdmin()) {
 			return new Roles(Roles.ADMIN);
 		}
 		return null;
 	}
 
 	private boolean isAdmin() {
 		if (getUsername().equals("brh")) {
 			return true;
 		}
 
 		return userProfileRepository.findUserOrCreateNew(getUsername()).isAdmin();
 	}
 
 	/**
 	 * @return the username
 	 */
 	public String getUsername() {
 		return username;
 	}
 
 }
