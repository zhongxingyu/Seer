 //
 // AutoLoginHandler.java
 //
 
 /*
 Java code for use with WiscScan.
 
 Copyright (c) 2008, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the UW-Madison LOCI nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package loci.wiscscan.auth;
 
 import java.io.IOException;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 
 public class AutoLoginHandler implements CallbackHandler {
 
 	// The storage variables
 	private final String m_password;
 	private final String m_username;
 
 	/**
 	 * This is the constructor which takes in the username and password to send
 	 * the JAAS call.
	 * @param Credentials This is the object containing the login info
 	 */
 	public AutoLoginHandler( Credentials credentials ) {
 		m_username = credentials.m_username;
 		m_password = credentials.m_password;
 	}
 
 	@Override
 	public void handle(Callback[] callbacks) throws IOException,
 			UnsupportedCallbackException {
 
 		for( int i = 0; i < callbacks.length; i++ ) {
 			Callback callback = callbacks[i];
 			if ( callback instanceof NameCallback ) {
 				( (NameCallback) callback ).setName( m_username );
 			}
 			else if ( callback instanceof PasswordCallback ) {
 				( (PasswordCallback) callback ).setPassword( m_password.toCharArray() );
 			}
 			else {
 				throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
 	        }
 	    }
 	}
 
 }
