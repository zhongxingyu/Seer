 // $Id$
 /*
  * Copyright 2004 The Apache Software Foundation
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.ftpserver.command;
 
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.ftpserver.DirectoryLister;
 import org.apache.ftpserver.FtpRequestImpl;
 import org.apache.ftpserver.FtpWriter;
 import org.apache.ftpserver.RequestHandler;
 import org.apache.ftpserver.ftplet.*;
 import org.apache.ftpserver.interfaces.ICommand;
 import org.apache.ftpserver.interfaces.IConnectionManager;
 import org.apache.ftpserver.interfaces.IFtpConfig;
 import org.apache.ftpserver.interfaces.IFtpStatistics;
 
 /**
  * <code>PASS &lt;SP&gt; <password> &lt;CRLF&gt;</code><br>
  *
  * The argument field is a Telnet string specifying the user's
  * password.  This command must be immediately preceded by the
  * user name command.
  * 
  * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
  */
 public 
 class PASS implements ICommand {
     
     /**
      * Execute command.
      */
     public void execute(RequestHandler handler, 
                         FtpRequestImpl request, 
                         FtpWriter out) throws IOException, FtpException {
     
         boolean bSuccess = false;
         IFtpConfig fconfig = handler.getConfig();
         Log log = fconfig.getLogFactory().getInstance(getClass());
         IConnectionManager conManager = fconfig.getConnectionManager();
         IFtpStatistics stat = (IFtpStatistics)fconfig.getFtpStatistics();
         try {
             
             // reset state variables
             request.resetState();
             
             // argument check
             String password = request.getArgument();
             if(password == null) {
                 out.send(501, "PASS", null);
                 return; 
             }
             
             // check user name
             String userName = request.getUserArgument();
             if(userName == null) {
                 out.send(503, "PASS", null);
                 return;
             }
             
             // already logged-in
             if(request.isLoggedIn()) {
                 out.send(202, "PASS", null);
                 bSuccess = true;
                 return;
             }
             
             // anonymous login limit check
             boolean bAnonymous = userName.equals("anonymous");
             int currAnonLogin = stat.getCurrentAnonymousLoginNumber();
             int maxAnonLogin = conManager.getMaxAnonymousLogins();
             if( bAnonymous && (currAnonLogin >= maxAnonLogin) ) {
                 out.send(421, "PASS.anonymous", null);
                 return;
             }
             
             // login limit check
             int currLogin = stat.getCurrentLoginNumber();
             int maxLogin = conManager.getMaxLogins();
             if(currLogin >= maxLogin) {
                 out.send(421, "PASS.login", null);
                 return;
             }
             
             // authenticate user
             UserManager userManager = fconfig.getUserManager();
             User user = null;
             try {
                 if(bAnonymous) {
                     bSuccess = userManager.doesExist("anonymous");
                     if (bSuccess)
                 	user = userManager.getUserByName("anonymous");
                 }
                 else {
                     bSuccess = userManager.authenticate(userName, password);
                     if (bSuccess)
                 	user = userManager.getUserByName(userName);
                 }
             }
             catch(Exception ex) {
                 bSuccess = false;
                 log.warn("PASS.execute()", ex);
             }
             if(bSuccess) {
                 request.setUser(user);
                 request.setUserArgument(null);
                 request.setMaxIdleTime(user.getMaxIdleTime());
             } else {
                 log.warn("Login failure - " + userName);
                 out.send(530, "PASS", userName);
                 return;
             }
             
            // update different objects
            FileSystemManager fmanager = fconfig.getFileSystemManager(); 
            FileSystemView fsview = fmanager.createFileSystemView(user);
            handler.setDirectoryLister(new DirectoryLister(fsview));
            request.setLogin(fsview);
            stat.setLogin(handler);

             // everything is fine - send login ok message
             out.send(230, "PASS", userName);
             if(bAnonymous) {
                 log.info("Anonymous login success - " + password);
             }
             else {
                 log.info("Login success - " + userName);
             }
             
             // call Ftplet.onLogin() method
             Ftplet ftpletContainer = fconfig.getFtpletContainer();
             FtpletEnum ftpletRet = ftpletContainer.onLogin(request, out);
             if(ftpletRet == FtpletEnum.RET_DISCONNECT) {
                 fconfig.getConnectionManager().closeConnection(handler);
                 return;
             }    
         }
         finally {
             
             // if login failed - close connection
             if(!bSuccess) {
                 request.setUserArgument(null);
                 //conManager.closeConnection(handler);
             }
         }
     }
 }
