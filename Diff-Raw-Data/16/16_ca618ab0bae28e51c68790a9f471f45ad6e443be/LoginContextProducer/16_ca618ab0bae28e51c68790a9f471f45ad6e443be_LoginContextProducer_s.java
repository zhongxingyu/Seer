 package com.blazebit.security.auth.impl;
 
 import java.io.Serializable;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.security.auth.login.AppConfigurationEntry;
 import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
 import javax.security.auth.login.Configuration;
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 
 import org.jboss.logging.Logger;
 
 /**
  * @author blep Date: 16/02/12 Time: 07:28
  */
 @SessionScoped
 public class LoginContextProducer implements Serializable {
 
     // ======================================
     // = Attributes =
     // ======================================
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     protected Logger log = Logger.getLogger(LoginContextProducer.class);
 
     @Inject
     private SimpleCallbackHandler callbackHandler;
 
     private LoginContext loginContext;
 
     // ======================================
     // = Business methods =
     // ======================================
 
     @Produces
     public LoginContext produceLoginContext(@LoginConfigFile String loginConfigFileName, @LoginModuleName final String loginModuleName) throws LoginException, URISyntaxException {
         if (loginContext == null) {
             return createLoginContext(loginConfigFileName, loginModuleName);
         }
         return loginContext;
     }
 
    // @Produces
     public LoginContext createLoginContext(String loginConfigFileName, final String loginModuleName) throws LoginException, URISyntaxException {
        // Subject subject = loginContext != null ? loginContext.getSubject() : null;
         // System.setProperty("java.security.auth.login.config", new
         // File(LoginContextProducer.class.getResource(loginConfigFileName).getFile()).getPath());
         try {
             loginContext = new LoginContext(loginModuleName, null, callbackHandler, new Configuration() {
 
                 @Override
                 public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                     AppConfigurationEntry[] entry = new AppConfigurationEntry[1];
                     Map<String, String> options = new HashMap<String, String>();
                     options.put("password-stacking", "useFirstPass");
                     entry[0] = new AppConfigurationEntry(loginModuleName, LoginModuleControlFlag.REQUIRED, options);
                     return entry;
                 }
 
             });
         } catch (Exception e) {
             log.error("Failed to create login context!", e);
         }
         return loginContext;
     }
 }
