 /*
  * #%L
  * Java code for use with WiscScan.
  * %%
 * Copyright (C) 2008 - 2013 Board of Regents of the University of
  * Wisconsin-Madison.
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 package loci.wiscscan.auth;
 
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 
 /**
  * Code to manage kerberos user authentication in WiscScan.
  *
  * @author Hanly De Los Santos
  */
 public class KerberosAuth {
 
 	/**
 	 * Attempts to log-in with the specified username and password.
 	 *
 	 * @return true if successfully logged in, false if unable to log in.
 	 */
 	public static boolean tryLogin(String username, String password) {
 		// Create the credentials file
 		Credentials credentials = new Credentials();
 		credentials.setM_username(username);
 		credentials.setM_password(password);
 
 		LoginContext lc = null;
 		try {
 			lc = new LoginContext("WiscScanLogin", new AutoLoginHandler(credentials));
 		}
 		catch (LoginException le) {
 			System.err.println("Cannot create LoginContext. " + le.getMessage());
 			return false;
 		}
 		catch (SecurityException se) {
 			return false;
 		}
 
 		try {
 			// attempt authentication
 			lc.login();
 		}
 		catch (LoginException le) {
 			System.err.println("Authentication failed:");
 			System.err.println("  " + le.getMessage());
 			return false;
 		}
 
 		return true;
 	}
 
 }
