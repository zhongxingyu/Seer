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
 
 import java.io.IOException;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 
 /**
  * Helper class for managing secure Kerberos logins.
  *
  * @author Hanly De Los Santos
  */
 public class AutoLoginHandler implements CallbackHandler {
 
 	// The storage variables
 	private final String m_password;
 	private final String m_username;
 
 	/**
 	 * This is the constructor which takes in the username and password to send
 	 * the JAAS call.
 	 * @param credentials This is the object containing the login info
 	 */
 	public AutoLoginHandler(Credentials credentials) {
 		m_username = credentials.m_username;
 		m_password = credentials.m_password;
 	}
 
 	@Override
 	public void handle(Callback[] callbacks) throws IOException,
 		UnsupportedCallbackException
 	{
 
 		for( int i = 0; i < callbacks.length; i++ ) {
 			Callback callback = callbacks[i];
 			if (callback instanceof NameCallback) {
 				((NameCallback) callback).setName(m_username);
 			}
 			else if (callback instanceof PasswordCallback) {
 				((PasswordCallback) callback).setPassword(m_password.toCharArray());
 			}
 			else {
 				throw new UnsupportedCallbackException(callbacks[i],
 					"Unrecognized Callback");
 			}
 		}
 	}
 
 }
