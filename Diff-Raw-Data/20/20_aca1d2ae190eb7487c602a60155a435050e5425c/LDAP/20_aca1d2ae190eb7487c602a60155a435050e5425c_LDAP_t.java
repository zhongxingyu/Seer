 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: LDAP.java,v 1.3 2006-05-10 21:07:23 pawand Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 package com.sun.identity.authentication.modules.ldap;
 
 import com.iplanet.am.util.Debug;
 import com.iplanet.am.util.Misc;
 import com.iplanet.am.util.SystemProperties;
 import com.sun.identity.authentication.service.AuthD;
 import com.sun.identity.authentication.spi.AMAuthCallBackImpl;
 import com.sun.identity.authentication.spi.AMAuthCallBackException;
 import com.sun.identity.authentication.spi.AMLoginModule;
 import com.sun.identity.authentication.spi.AuthLoginException;
 import com.sun.identity.authentication.spi.InvalidPasswordException;
 import com.sun.identity.authentication.spi.UserNamePasswordValidationException;
 import com.sun.identity.authentication.util.ISAuthConstants;
 import com.sun.identity.common.Constants;
 import com.sun.identity.sm.ServiceConfig;
 
 import java.io.IOException;
 import java.security.Principal;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 import javax.security.auth.Subject;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.ConfirmationCallback;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import netscape.ldap.util.ConnectionPool;
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPException;
 
 public class LDAP extends AMLoginModule {
     // static variables
     private static HashMap orgMap = new HashMap();
     private static final long DEFAULT_SERVER_CHECK_INTERVAL = 15;
     private static final String USER_CREATION_ATTR =
         "iplanet-am-ldap-user-creation-attr-list";
     private static final String INVALID_CHARS =
         "iplanet-am-auth-ldap-invalid-chars";
     private static final String PIPE_SEPARATOR="|";
     private static boolean ldapSSL = false;
     
     // local variables
     ResourceBundle bundle = null;
     protected String validatedUserID;
     private String userName;
     private String userPassword;
     private String regEx;
     private String currentConfigName;
    private String bindDN;
     private Iterator subConfigNamesIter = null;
     private ServiceConfig sc;
     private boolean firstTry = true;
     private int currentState;
     private boolean primary = true;
     private int previousScreen;
     private final int PASSWORD_CHANGE = 2;
     private final int PASSWORD_EXPIRED_SCREEN = 3;
     private final int USER_INACTIVE = 4;
     private LDAPAuthUtils ldapUtil;
     private static volatile FailbackManager fMgr;
     private boolean isReset;
     private int primaryServerPort;
     private String primaryServerHost;
     private boolean isProfileCreationEnabled;
     private boolean getCredentialsFromSharedState;
 
     private AMAuthCallBackImpl callbackImpl = null;
     
     private long interval = DEFAULT_SERVER_CHECK_INTERVAL;
     
     private Set userCreationAttrs = new HashSet();
     private HashMap userAttrMap = new HashMap();
     private Map sharedState;
     private String serverHost;
     private int serverPort;
     public Map currentConfig;
     
     protected Debug debug = null;
     protected String amAuthLDAP;
     protected Principal userPrincipal;
     
     static {
         // initializing the login parameters by getting the values from
         // ConfigProperties
         ldapSSL = Boolean.valueOf(SystemProperties.get(
         Constants.AM_DIRECTORY_SSL_ENABLED, "false")).booleanValue();
     }
 
     /**
      * TODO-JAVADOC
      */
     public LDAP() {
         amAuthLDAP = "amAuthLDAP";
         debug = Debug.getInstance(amAuthLDAP);
     }
 
     /**
      * TODO-JAVADOC
      */
     public void init(Subject subject, Map sharedState, Map options) {
         sc = (ServiceConfig) options.get("ServiceConfig");
         currentConfig = options;
         currentConfigName = 
             (String)options.get(ISAuthConstants.MODULE_INSTANCE_NAME);
         primary = getPrimaryFlag(currentConfigName);
         java.util.Locale locale = getLoginLocale();
         bundle = amCache.getResBundle(amAuthLDAP, locale);
         if (debug.messageEnabled()) {
             debug.message("LDAP resbundle locale=" + locale);
         }
         this.sharedState = sharedState;
         if (debug.messageEnabled()) {
             debug.message("Host: " + AuthD.directoryHostName +
             "\nPORT : " + AuthD.directoryPort);
         }
     }
 
     /**
      * TODO-JAVADOC
      */
     public boolean initializeLDAP() throws AuthLoginException {
         debug.message("LDAP initialize()");
         serverHost = null;
         
         try {
             if (currentConfig != null) {
                 try {
                     String checkAttr = "iplanet-am-auth-ldap-server-check";
                     interval = Long.parseLong(
                     Misc.getServerMapAttr(currentConfig, checkAttr));
                 } catch (NumberFormatException nfe) {
                     if (debug.messageEnabled()) {
                         debug.message("Server Check Interval is not set.\n"+
                         "Setting it to default value 15 min");
                     }
                     interval = DEFAULT_SERVER_CHECK_INTERVAL;
                 }
                 setInterval(interval);
                 if (primary) {
                     serverHost = Misc.getServerMapAttr(currentConfig,
                     "iplanet-am-auth-ldap-server");
                     if (serverHost == null) {
                         if (debug.messageEnabled()) {
                             debug.message("No primary server for confing " +
                             currentConfigName);
                         }
                         return false;
                     }
                 } else {
                     serverHost = Misc.getServerMapAttr(currentConfig,
                     "iplanet-am-auth-ldap-server2");
                     
                     if (serverHost == null) {
                         if (debug.messageEnabled()) {
                             debug.message("No secondary server for confing " +
                             currentConfigName);
                         }
                         return false;
                     }
                 }
                 
                 String baseDN = Misc.getServerMapAttr(currentConfig,
                 "iplanet-am-auth-ldap-base-dn");
                 if (baseDN == null) {
                     debug.error("BaseDN for search is invalid: " + baseDN);
                 }
                 
                bindDN = Misc.getMapAttr(currentConfig,
                 "iplanet-am-auth-ldap-bind-dn", "");
                 String bindPassword = Misc.getMapAttr(currentConfig,
                 "iplanet-am-auth-ldap-bind-passwd", "");
                 String userNamingAttr = Misc.getMapAttr(currentConfig,
                 "iplanet-am-auth-ldap-user-naming-attribute", "uid");
                 Set userSearchAttrs = (Set) currentConfig.get(
                 "iplanet-am-auth-ldap-user-search-attributes");
                 String searchFilter = Misc.getMapAttr(currentConfig,
                 "iplanet-am-auth-ldap-search-filter", "");
                 boolean ssl = Boolean.valueOf(Misc.getMapAttr(currentConfig,
                 "iplanet-am-auth-ldap-ssl-enabled",
                 "false")).booleanValue();
                 getUserCreationAttrs(currentConfig);
                 String tmp = Misc.getMapAttr(currentConfig,
                 "iplanet-am-auth-ldap-search-scope", "SUBTREE");
                 
                 String authLevel = Misc.getMapAttr(currentConfig,
                 "iplanet-am-auth-ldap-auth-level");
                 if (authLevel != null) {
                     try {
                         setAuthLevel(Integer.parseInt(authLevel));
                     } catch (Exception e) {
                         debug.error("Unable to set auth level " + authLevel);
                     }
                 }
                 int searchScope = 2; // SUBTREE is the default
                 if (tmp.equalsIgnoreCase("OBJECT")) {
                     searchScope = 0;
                 } else if (tmp.equalsIgnoreCase("ONELEVEL")) {
                     searchScope = 1;
                 }
                 
                 String returnUserDN = Misc.getMapAttr(currentConfig,
                 ISAuthConstants.LDAP_RETURNUSERDN, "true");
                 regEx = Misc.getMapAttr(currentConfig, INVALID_CHARS);
                 
                 // set LDAP Parameters
                 int index = serverHost.indexOf(':');
                 serverPort = 389;
                 
                 if (index != -1) {
                     serverPort = Integer.parseInt(
                     serverHost.substring(index + 1));
                     serverHost = serverHost.substring(0, index);
                 }
                 if (!primary) {
                     primaryServerHost = Misc.getServerMapAttr(currentConfig,
                         "iplanet-am-auth-ldap-server");
                     primaryServerPort = 389;
                     int colonIndex = primaryServerHost.indexOf(':');
                     if (colonIndex != -1) {
                         primaryServerPort = Integer.parseInt(
                         primaryServerHost.substring(colonIndex + 1));
                         primaryServerHost = primaryServerHost.substring(0,
                             colonIndex);
                     }
                     if (LDAPAuthUtils.adminConnectionPoolsStatus != null) {
                         String poolKey = primaryServerHost + ":" +
                             primaryServerPort;
                         String adminPoolStatus = (String)LDAPAuthUtils.
                             adminConnectionPoolsStatus.get(poolKey);
                         if ( (adminPoolStatus == null) ||
                             (adminPoolStatus.equals(LDAPAuthUtils.STATUS_UP))) {
                              setPrimaryFlag(currentConfigName, true);
                              primary = true;
                              serverHost = primaryServerHost;
                              serverPort = primaryServerPort;
                         }
                     }
                 }
                 
                 isProfileCreationEnabled = isDynamicProfileCreationEnabled();
                 // set the optional attributes here
                 ldapUtil = new LDAPAuthUtils(serverHost, serverPort, ssl,
                 bundle, baseDN, debug);
                 ldapUtil.setScope(searchScope);
                 ldapUtil.setFilter(searchFilter);
                 ldapUtil.setUserNamingAttribute(userNamingAttr);
                 ldapUtil.setUserSearchAttribute(userSearchAttrs);
                 ldapUtil.setAuthPassword(bindPassword);
                 ldapUtil.setAuthDN(bindDN);
                 ldapUtil.setReturnUserDN(returnUserDN);
                 ldapUtil.setUserAttributes(userCreationAttrs);
                 ldapUtil.setDynamicProfileCreationEnabled(
                     isProfileCreationEnabled);
                 if (debug.messageEnabled()) {
                     debug.message("bindDN-> " + bindDN +
                     "\nbaseDN-> " + baseDN +
                     "\nuserNamingAttr-> " + userNamingAttr +
                     "\nuserSearchAttr(s)-> " + userSearchAttrs +
                     "\nuserCreationAttrs-> "+userCreationAttrs+
                     "\nsearchFilter-> " + searchFilter +
                     "\nsearchScope-> " + searchScope +
                     "\nssl-> " + ssl +
                     "\nauthLevel: " + authLevel +
                     "\nHost: " + serverHost +
                     "\nPORT : " + serverPort +
                     "\nPattern : " + regEx);
                 }
                 return true;
             }
         } catch (Exception ex) {
             debug.error("Init Exception", ex);
             throw new AuthLoginException(amAuthLDAP, "LDAPex", null, ex);
         }
         return false;
     }
     
     private boolean getPrimaryFlag( String configName) {
         synchronized (orgMap) {
             String reqOrg = getRequestOrg();
             Map flags = (Map) orgMap.get(reqOrg);
             if (flags == null) {
                 flags = new HashMap();
                 flags.put(configName, "true");
                 orgMap.put(reqOrg, flags);
                 return true;
             }
             
             String flag = (String) flags.get(configName);
             if (flag == null) {
                 flags.put(configName, "true");
                 return true;
             }
             
             return (flag.equals("true"));
         }
     }
     
     private void setPrimaryFlag( String configName, boolean flag) {
         synchronized (orgMap) {
             String reqOrg = getRequestOrg();
             Map flags = (Map) orgMap.get(reqOrg);
             if (flags == null) {
                 flags = new HashMap();
                 orgMap.put(reqOrg, flags);
             }
             
             if (flag) {
                 flags.put(configName, "true");
             } else {
                 flags.put(configName, "false");
             }
         }
     }
     
     private boolean getSubConfig() {
         firstTry = true;
         try {
             if (subConfigNamesIter == null) {
                 if (sc != null) {
                     Set subConfigNames = sc.getSubConfigNames();
                     if (subConfigNames == null || subConfigNames.size() == 0) {
                         return false;
                     }
                     subConfigNamesIter = subConfigNames.iterator();
                 }
             }
             
             if (subConfigNamesIter != null) {
                 while (subConfigNamesIter.hasNext()) {
                     String subConfigName = (String) subConfigNamesIter.next();
                     ServiceConfig ssc = sc.getSubConfig(subConfigName);
                     if (ssc != null) {
                         if (debug.messageEnabled()) {
                             debug.message("LDAP.getSubConfig subConfigName = " +
                             subConfigName);
                         }
                         currentConfig = ssc.getAttributes();
                         currentConfigName = subConfigName;
                         primary = getPrimaryFlag(subConfigName);
                         return true;
                     }
                 }
             }
         } catch (Exception ex) {
             if (debug.warningEnabled()) {
                 debug.warning("LDAP.getSubConfig unable to get sub config", ex);
             }
         }
         return false;
     }
     
     public int process(Callback[] callbacks, int state)
             throws AuthLoginException {
         currentState = state;
         int newState = 0;
         try {
             
             if (currentState == ISAuthConstants.LOGIN_START) {
                 if (callbacks !=null && callbacks.length == 0) {
                     userName = (String) sharedState.get(getUserKey());
                     userPassword = (String) sharedState.get(getPwdKey());
                     if (userName == null || userPassword == null) {
                         return ISAuthConstants.LOGIN_START;
                     }
                     getCredentialsFromSharedState = true;
                 } else {
                     //callbacks is not null
                     userName = ( (NameCallback) callbacks[0]).getName();
                     userPassword = charToString(((PasswordCallback)
                     callbacks[1]).getPassword(), callbacks[1]);
                 }
                 //store username password both in success and failure case
                 storeUsernamePasswd(userName, userPassword);
                 
                 /*
                  * checks whether the superadmin and the username supplied
                  * is same . This check is to allow Super Admin to log in
                  * even though ldap parameters are messed up due to
                  * misconfiguration
                  */
                 if (isSuperAdmin(userName)) {
                     ldapUtil = new LDAPAuthUtils(AuthD.directoryHostName,
                     AuthD.directoryPort, ldapSSL, bundle, debug);
                     ldapUtil.authenticateSuperAdmin(userName, userPassword);
                     if (ldapUtil.getState() == LDAPAuthUtils.SUCCESS) {
                         validatedUserID = userName;
                         return ISAuthConstants.LOGIN_SUCCEED;
                     } else {
                         debug.message("Invalid adminID or admin Password");
                         setFailureID(ldapUtil.getUserId(userName));
                         throw new AuthLoginException(
                             amAuthLDAP, "InvalidUP", null);
                     }
                 } else {
                     if (initializeLDAP()) {
                         //validate username and password
                         validateUserName(userName, regEx);
                         validatePassword(userPassword);
                         ldapUtil.authenticateUser(userName, userPassword);
                         newState = ldapUtil.getState();
                     } else {
                         newState = LDAPAuthUtils.SERVER_DOWN;
                     }
                     processLoginScreen(newState);
                     //processLoginScreen(LDAPAuthUtils.PASSWORD_EXPIRING);
                     return currentState;
                 }
             } else if (currentState == PASSWORD_CHANGE) {
                 if (debug.messageEnabled()) {
                     debug.message("you are in PWd Screen:" + currentState);
                 }
                 // callbacks[3] is a user selected button index
                 // PwdAction == 0 is a Submit button
                 int pwdAction =
                     ((ConfirmationCallback)callbacks[3]).getSelectedIndex();
                 if (pwdAction == 0) {
                     String oldPassword = charToString(((PasswordCallback)
                     callbacks[0]).getPassword(), callbacks[0]);
                     String newPassword = charToString(((PasswordCallback)
                     callbacks[1]).getPassword(), callbacks[1]);
                     String confirmPassword = charToString(((PasswordCallback)
                     callbacks[2]).getPassword(), callbacks[2]);
                     validatePassword(newPassword);
                     ldapUtil.changePassword(oldPassword, newPassword,
                         confirmPassword);
                     newState = ldapUtil.getState();
                     processPasswordScreen(newState);
                     if (debug.messageEnabled()) {
                         debug.message("Password change state :" + newState);
                     }
                     return currentState;
                 } else  {
                     if (isReset) {
                         isReset = false;
                         return ISAuthConstants.LOGIN_START;
                     }
                     validatedUserID = ldapUtil.getUserId();
                     return ISAuthConstants.LOGIN_SUCCEED;
                 }
             } else {
                 setFailureID(ldapUtil.getUserId(userName));
                 throw new AuthLoginException(amAuthLDAP, "LDAPex", null);
             }
         } catch (LDAPUtilException ex) {
             if (getCredentialsFromSharedState) {
                 getCredentialsFromSharedState = false;
                 return ISAuthConstants.LOGIN_START;
             }
             setFailureID(ldapUtil.getUserId(userName));
             switch (ex.getLDAPResultCode()) {
                 case LDAPUtilException.NO_SUCH_OBJECT:
                     debug.message("The specified user does not exist.");
                     throw new AuthLoginException(amAuthLDAP, "NoUser", null);
                 case LDAPUtilException.INVALID_CREDENTIALS:
                     debug.message("Invalid password.");
                     String failureUserID = ldapUtil.getUserId();
                     throw new InvalidPasswordException(amAuthLDAP, "InvalidUP",
                         null, failureUserID, null);
                 case LDAPUtilException.UNWILLING_TO_PERFORM:
                     debug.message("Unwilling to perform. Account inactivated.");
                         currentState = USER_INACTIVE;
                     return currentState;
                     
                 case LDAPUtilException.INAPPROPRIATE_AUTHENTICATION:
                     debug.message("Inappropriate authentication.");
                     throw new AuthLoginException(amAuthLDAP, "InappAuth", null);
                 default:
                     throw new AuthLoginException(amAuthLDAP, "LDAPex", null);
             }
         } catch (UserNamePasswordValidationException upve) {
             // Note: Do not set failure Id for this exception
             
             debug.message("Invalid Characters detected");
             throw new AuthLoginException(upve);
         }
     }
    
     /**
      * Returns principal.
      *
      * @return principal.
      */
     public Principal getPrincipal() {
         if (userPrincipal != null) {
             return userPrincipal;
         } else if (validatedUserID != null) {
             userPrincipal = new LDAPPrincipal(validatedUserID);
             return userPrincipal;
         } else {
             return null;
         }
     }
     
     /**
      * Cleans up state fields.
      */
     public void destroyModuleState() {
         validatedUserID = null;
         userPrincipal = null;
     }
 
     /**
      * TODO-JAVADOC
      */
     public void nullifyUsedVars() {
         bundle = null;
         userName = null ;
         userPassword = null;
         regEx = null;
         subConfigNamesIter = null;
         sc = null;
         userCreationAttrs = null;
         userAttrMap = null;
         sharedState = null;
         currentConfig = null;
         
         amAuthLDAP = null;
     }
     
     private void processLoginScreen(int newState) throws AuthLoginException {
         try {
             switch (newState) {
                 case LDAPAuthUtils.SUCCESS:
                     validatedUserID = ldapUtil.getUserId();
                     if (isProfileCreationEnabled &&
                         userCreationAttrs.size() > 0
                     ) {
                         Map userAttributeValues =
                             ldapUtil.getUserAttributeValues();
                         if (debug.messageEnabled()) {
                             debug.message("user creation attributes: " +
                                 userAttributeValues);
                         }
                         Map userValues= getAttributeMap(userAttributeValues);
                         setUserAttributes(userValues);
                     }
                     currentState = ISAuthConstants.LOGIN_SUCCEED;
                     break;
                 case LDAPAuthUtils.PASSWORD_EXPIRING:
                     String fmtMsg = bundle.getString("PasswordExp");
                     String msg = com.iplanet.am.util.Locale.formatMessage(
                         fmtMsg,
                     ldapUtil.getExpTime());
                     replaceHeader(PASSWORD_CHANGE, msg);
                     currentState = PASSWORD_CHANGE;
                     break;
                 case LDAPAuthUtils.PASSWORD_RESET_STATE:
                     isReset = true;
                     String resetMsg = bundle.getString("PasswordReset");
                     replaceHeader(PASSWORD_CHANGE, resetMsg);
                     currentState = PASSWORD_CHANGE;
                     break;
                 case LDAPAuthUtils.PASSWORD_EXPIRED_STATE:
                     currentState = PASSWORD_EXPIRED_SCREEN;
                     break;
                 case LDAPAuthUtils.USER_NOT_FOUND:
                     if (!getSubConfig()) {
                         throw new LDAPUtilException("noUserMatchFound",
                         (Object[])null);
                     }
                     if (initializeLDAP()) {
                         ldapUtil.authenticateUser(userName, userPassword);
                         newState = ldapUtil.getState();
                     } else {
                         newState = LDAPAuthUtils.SERVER_DOWN;
                     }
                     processLoginScreen(newState);
                     break;
                 case LDAPAuthUtils.SERVER_DOWN:
                     if (firstTry) {
                        String key = serverHost + ":"+serverPort + ":" + bindDN;
                         synchronized(LDAPAuthUtils.adminConnectionPoolsStatus){
                             LDAPAuthUtils.adminConnectionPoolsStatus.put(key,
                                 LDAPAuthUtils.STATUS_DOWN);
                         }
                         firstTry = false;
                         primary = !primary;
                         setPrimaryFlag(currentConfigName, primary);
                         if (fMgr == null || !fMgr.isAlive()) {
                             fMgr = new FailbackManager();
                             fMgr.start();
                         } else {
                             if (interval < fMgr.sleepTime) {
                                 fMgr.sleepTime = interval;
                             }
                         }
                         if (initializeLDAP()) {
                             ldapUtil.authenticateUser(userName, userPassword);
                             newState = ldapUtil.getState();
                             processLoginScreen(newState);
                             break;
                         }
                     }
                     
                     if (!getSubConfig()) {
                         throw new AuthLoginException(
                             amAuthLDAP, "LDAPex", null);
                     }
                     if (initializeLDAP()) {
                         ldapUtil.authenticateUser(userName, userPassword);
                         newState = ldapUtil.getState();
                     } else {
                         newState = LDAPAuthUtils.SERVER_DOWN;
                     }
                     processLoginScreen(newState);
                     break;
                 default:
             }
         } catch (LDAPUtilException ex) {
             if (getCredentialsFromSharedState) {
                 getCredentialsFromSharedState = false;
                 currentState = ISAuthConstants.LOGIN_START;
                 return;
             }
             if (newState != LDAPAuthUtils.USER_NOT_FOUND) {
                 debug.error("Unknown Login State:", ex);
             }
             throw new AuthLoginException(amAuthLDAP, "LDAPex", null, ex);
         }
     }
     
     private void processPasswordScreen(int newState)
             throws AuthLoginException {
         switch (newState) {
             case LDAPAuthUtils.PASSWORD_UPDATED_SUCCESSFULLY:
                 validatedUserID = ldapUtil.getUserId();
                 currentState = ISAuthConstants.LOGIN_SUCCEED;
                 // Instantiating the callback implementation variable. This
                 // will be used to notify the plug-in classes that a
                 // successful password change was performed.
                 try {
                     callbackImpl =
                         AMAuthCallBackImpl.getInstance(this.getRequestOrg());
                     // We need the current system time since this is required
                     // as part of the callback method parameter.
                     Long now = new Long(System.currentTimeMillis());
                     // We now notify the plug-in that a successful
                     // password change was performed.
                     callbackImpl.processedPasswordChange(now, validatedUserID);
                 } catch (AMAuthCallBackException acbe) {
                     if (debug.errorEnabled()) {
                         debug.error("process : unable to get " +
                         "AMAuthCallBackImpl instance or callback module " +
                         "raised an exception.", acbe);
                     }
                 }
                 break;
             case LDAPAuthUtils.PASSWORD_NOT_UPDATE:
                 replaceHeader(PASSWORD_CHANGE,
                 bundle.getString("PInvalid"));
                 currentState = PASSWORD_CHANGE;
                 break;
             case LDAPAuthUtils.PASSWORD_MISMATCH:
                 replaceHeader(PASSWORD_CHANGE,
                 bundle.getString("PasswdMismatch"));
                 currentState = PASSWORD_CHANGE;
                 break;
             case LDAPAuthUtils.PASSWORD_USERNAME_SAME:
                 replaceHeader(PASSWORD_CHANGE,
                 bundle.getString("UPSame"));
                 currentState = PASSWORD_CHANGE;
                 break;
             case LDAPAuthUtils.WRONG_PASSWORD_ENTERED:
                 replaceHeader(PASSWORD_CHANGE,
                 bundle.getString("PasswdSame"));
                 currentState = PASSWORD_CHANGE;
                 break;
                 
             case LDAPAuthUtils.PASSWORD_MIN_CHARACTERS:
                 replaceHeader(PASSWORD_CHANGE,
                 bundle.getString("PasswdMinChars"));
                 currentState = PASSWORD_CHANGE;
                 
                 break;
                 
             case LDAPAuthUtils.USER_PASSWORD_SAME:
                 replaceHeader(PASSWORD_CHANGE,
                 bundle.getString("UPsame"));
                 currentState = PASSWORD_CHANGE;
                 
                 break;
                 
             default:
                 
         }
     }
     
     private String charToString(char[] tmpPassword, Callback cbk) {
         if (tmpPassword == null) {
             // treat a NULL password as an empty password
             tmpPassword = new char[0];
         }
         char[] pwd = new char[tmpPassword.length];
         System.arraycopy(tmpPassword, 0, pwd, 0, tmpPassword.length);
         ((PasswordCallback) cbk).clearPassword();
         return new String(pwd);
     }
     
     // reset replace text method should be provided by AMLoginModule
     // untill then this method cannot be used for multiple replacement
     private void setReplaceText(int state, int index, String msg)
             throws AuthLoginException {
         // set #REPLACE# in password callback
         debug.message("Entered in setReplaceText");
         
         Callback[] callbacks2 = getCallback(PASSWORD_CHANGE);
         // reset first callback at PASSWORD_CHANGE state
         resetCallback(state, 0);
         String origMsg = ( (PasswordCallback) callbacks2[0]).getPrompt();
         int idx = origMsg.indexOf("#REPLACE#");
         String setMsg = msg + origMsg.substring(idx + 9);
         replaceCallback(state, 0, new PasswordCallback(setMsg,
             ((PasswordCallback) callbacks2[0]).isEchoOn()));
         
         if (debug.messageEnabled()) {
             debug.message("origmessage:" + origMsg + ":::+setMsg" + setMsg);
         }
     }
     
     
     // sets the time interval to check whether primary is up
     private void setInterval(long iVal) {
         interval = (iVal*60000);
     }
     
     // This class checks in regular intervals whether the connection is open
     // if connection is open it sets primary to true.
     class FailbackManager  extends Thread {
         public long sleepTime = interval;
         public FailbackManager() {}
         public void run() {
             Thread thisThread = Thread.currentThread();
             boolean foundDown = true;
             while (foundDown) {
                 try {
                     foundDown = false;
                     Set set1 = LDAPAuthUtils.adminConnectionPoolsStatus.
                         keySet();
                     Iterator iter1 = set1.iterator();
                     while (iter1.hasNext()){
                         String key = (String)iter1.next();
                         String status  = (String)LDAPAuthUtils.
                             adminConnectionPoolsStatus.get(key);
                         if ( status.equals(LDAPAuthUtils.STATUS_DOWN)) {
                             foundDown = true;
                             if (LDAPAuthUtils.connectionPools != null) {
                                 ConnectionPool cPool = (ConnectionPool)
                                     LDAPAuthUtils.adminConnectionPools.get(key);
                                 LDAPConnection ldapConn = cPool.getConnection();
                                 try {
                                     ldapConn.reconnect();
                                 } catch(LDAPException ex) {
                                 }
                                 if(ldapConn.isConnected()) {
                                     LDAPAuthUtils.adminConnectionPoolsStatus.
                                         put(key,LDAPAuthUtils.STATUS_UP);
                                 }
                     
                             }
                         } 
                     }
                 } catch (Exception exp) {
                     debug.error("Error in Fallback Manager Thread",exp);
                 }
                 try {
                     thisThread.sleep(sleepTime);
                 } catch (InterruptedException e) {
                 }
             }
         }
     }
     
     /**
      * Retrieves the user creation attribute list from the
      * ldap configuration. The format of each line in the attribute
      * list is localAttribute:externalAttribute , this indicates the
      * the mapping of the local attribute in local iDS to the external
      * attribute in remote iDS.
      * This method parses each line in the list to separate the local
      * attribute and external attribute and creates a set of
      * external attributes and a Map with key as the internal
      * attribute and value the external attribute.
      */
     private void getUserCreationAttrs(Map currentConfig) {
         
         Set attrs = (Set)currentConfig.get(USER_CREATION_ATTR);
         if (debug.messageEnabled()) {
             debug.message("attrs is : " + attrs);
         }
         if ((attrs != null) && (!attrs.isEmpty())) {
             Iterator attrIterator = attrs.iterator();
             while (attrIterator.hasNext()) {
                 String userAttr = (String) attrIterator.next();
                 int i = userAttr.indexOf(PIPE_SEPARATOR);
                 if (i != -1) {
                     String localAttr =  userAttr.substring(0,i);
                     String extAttr = userAttr.substring(i+1,userAttr.length());
                     if ( (extAttr == null)  || (extAttr.length() == 0)) {
                         userCreationAttrs.add(localAttr);
                         userAttrMap.put(localAttr,localAttr);
                     } else {
                         userCreationAttrs.add(extAttr);
                         userAttrMap.put(localAttr,extAttr);
                     }
                 } else {
                     userCreationAttrs.add(userAttr);
                     userAttrMap.put(userAttr,userAttr);
                 }
             }
         }
         return;
     }
     
     /**
      * this method retrieves the key which is the external
      * attribute from the attributeValues and maps it
      * to the local attribute. A new map with localAttribute
      * as the key and value is the value of the attribute in
      * external iDS is created.
      */
     private Map getAttributeMap(Map attributeValues) {
         if (debug.messageEnabled()) {
             debug.message("In getAttribute Map: " + attributeValues);
         }
         Map newAttrMap = new HashMap();
         Iterator userIterator = userAttrMap.keySet().iterator();
         while (userIterator.hasNext()) {
             String key = (String) userIterator.next();
             String value = (String) userAttrMap.get(key);
             Set newValue = (Set) attributeValues.get(value);
             if (debug.messageEnabled()) {
                 debug.message("key is : " + key);
                 debug.message("value is : " + newValue);
             }
             if (newValue != null) {
                 newAttrMap.put(key,newValue);
             }
         }
         if (debug.messageEnabled()) {
             debug.message("New attr map is : " + newAttrMap);
         }
         return newAttrMap;
     }
 }
