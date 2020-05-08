 package us.walkenhorst.glassfish.security.jdbc;
 
 import javax.security.auth.login.LoginException;
 
 import com.sun.appserv.security.AppservPasswordLoginModule;
 /*
 The MIT License (MIT)
 
 Copyright (c) 2013 Jacob Walkenhorst
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 /* To use this module, open the login.conf file in the \glassfish\domains\domain1\config directory. Add the following lines at the end of the file and save:
 	DBRealm{
		edu.neumont.csc280.security.jdbc.DBLogin required;
 	};
 */
 public class DBLogin extends AppservPasswordLoginModule{
 	
 	@Override
 	protected void authenticateUser() throws LoginException{
 		DBRealm currentRealm = (DBRealm)this.getCurrentRealm();
 		DBAuthenticate auth = currentRealm.getDBAuthenticate();
 		String[] grpList;
 		grpList = auth.getUserGroups(this.getUsername(), this.getPasswordChar());
 		if (grpList == null) throw new LoginException("Unable to authenticate user "
 				+ this.getUsername());
 		commitUserAuthentication(grpList);
 	}
 }
