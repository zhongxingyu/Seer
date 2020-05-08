 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2010-2011, Centre National de la Recherche Scientifique
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 package eu.stratuslab.authn;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import javax.security.auth.Subject;
 import javax.security.auth.callback.CallbackHandler;
 
 import org.eclipse.jetty.http.security.Credential;
 import org.eclipse.jetty.plus.jaas.spi.AbstractLoginModule;
 import org.eclipse.jetty.plus.jaas.spi.UserInfo;
 
 public class CertLoginModule extends AbstractLoginModule {
 
     private static final AtomicReference<AuthnData> AUTHN_USERS_REF = new AtomicReference<AuthnData>();
 
     static {
         AUTHN_USERS_REF.set(new AuthnData(null));
     }
 
     final private static Logger LOGGER;
     static {
         LOGGER = Logger.getLogger(CertLoginModule.class.getCanonicalName());
         for (Handler h : LOGGER.getHandlers()) {
             LOGGER.removeHandler(h);
         }
 
         Handler handler = new ConsoleHandler();
         handler.setFormatter(new ShortMsgFormatter());
         LOGGER.addHandler(handler);
 
         LOGGER.setUseParentHandlers(false);
     }
 
     @Override
     public UserInfo getUserInfo(String username) throws Exception {
 
         LOGGER.info("checking user: " + username);
 
         String strippedUsername = stripCNProxy(username);
 
         AuthnData data = AUTHN_USERS_REF.get();
         if (data.isValidUser(strippedUsername)) {
             LOGGER.info("authorized user: " + strippedUsername);
             Credential credential = new ValidCredential();
             List<String> roles = getUserRoles(strippedUsername);
 
             return new UserInfo(strippedUsername, credential, roles);
         } else {
             LOGGER.info("unauthorized user: " + strippedUsername);
             return null;
         }
     }
 
    @SuppressWarnings("rawtypes")
     @Override
     public void initialize(Subject subject, CallbackHandler callbackHandler,
             Map sharedState, Map options) {
 
         super.initialize(subject, callbackHandler, sharedState, options);
 
         AUTHN_USERS_REF.set(new AuthnData(options.get("file")));
     }
 
     private List<String> getUserRoles(String username) {
         AuthnData data = AUTHN_USERS_REF.get();
         return data.groups(username);
     }
 
     public static String stripCNProxy(String username) {
         return username.replaceFirst("^CN\\s*=\\s*proxy\\s*,\\s*", "");
     }
 
     @SuppressWarnings("serial")
     private static class ValidCredential extends Credential {
 
         @Override
         public boolean check(Object credentials) {
             return true;
         }
     }
 
     // TODO: pull into separate class
     private static class ShortMsgFormatter extends Formatter {
 
         public String format(LogRecord record) {
             StringBuilder sb = new StringBuilder();
 
             Date date = new Date(record.getMillis());
             DateFormat dateFormat = new SimpleDateFormat(
                     "yyyy-MM-dd HH:mm:ss.SSS");
             sb.append(dateFormat.format(date));
             sb.append(":");
 
             sb.append(record.getLevel().getName());
             sb.append("::");
 
             sb.append(record.getMessage());
             sb.append("\n");
 
             return sb.toString();
         }
     }
 
 }
