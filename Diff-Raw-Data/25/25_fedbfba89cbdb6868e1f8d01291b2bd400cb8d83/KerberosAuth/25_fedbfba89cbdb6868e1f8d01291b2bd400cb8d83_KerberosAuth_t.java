 //
 // KerberosAuth.java
 //
 
 /*
 Utility code for use with WiscScan.
 
 Copyright (c) 2011, UW-Madison LOCI
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
 
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 
 /**
  * Code to manage kerberos user authentication in WiscScan.
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/wiscscan-utils/src/main/java/loci/wiscscan/utils/App.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/wiscscan-utils/src/main/java/wiscscan/utils/App.java">SVN</a></dd></dl>
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
