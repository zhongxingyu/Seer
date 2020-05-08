 /**
  *    Copyright (C) 2013 TabTonic LLC
  * 
  *    This file is part of kerberosAREA.
  *
  *    kerberosAREA is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    kerberosAREA is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with kerberosAREA.  If not, see <http://opensource.org/licenses/gpl-3.0.html>.
  * 
  */
 package com.tabtonic.kerberosAREA;
 
 import java.io.IOException;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * <h4>UAndPCallbackHandler</h4>
  * Kerberos requires a callback handler for user login authentication.  
  * <br>When we create a login context, we pass in a callback handler.  When authentication is requested (via LoginContext.Login()) kerberos will ask the 
  * <br>callback handler for the username and password. 
  * 
  * @author      Steve Kallestad, <a href="http://www.tabtonic.com/">TabTonic LLC</a>
  *
  * @version     0.0.1-SNAPSHOT      
  */
 public class UAndPCallbackHandler implements CallbackHandler {
 	/**
 	 * for log output
 	 */
 	final Logger logger = LoggerFactory.getLogger(UAndPCallbackHandler.class);
 	
 	private String _userName;
 	private char[] _password;
 	
 	public UAndPCallbackHandler(String userName, char[] password){
 		_userName = userName;
		_password = password;
 	}
 	
 	public void handle(Callback[] callbacks) throws IOException,
 			UnsupportedCallbackException {
 		logger.trace("Callback Handler Handle called");
 		for(Callback callback : callbacks){
 			if(callback instanceof NameCallback && _userName != null){
 				logger.trace("Name callback handler called for {}", _userName);
 				((NameCallback) callback).setName(_userName);
 			} else if (callback instanceof PasswordCallback && _password != null){
 				logger.trace("Password callback handler called for {}", _userName);
 				((PasswordCallback) callback).setPassword(_password);
 			}
 		}
 	}
 }
