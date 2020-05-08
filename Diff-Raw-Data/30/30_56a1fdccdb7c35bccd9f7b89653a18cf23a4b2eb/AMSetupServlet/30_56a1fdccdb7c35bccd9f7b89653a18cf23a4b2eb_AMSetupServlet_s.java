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
 * $Id: AMSetupServlet.java,v 1.8 2006-08-29 21:55:09 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 
 import com.iplanet.services.util.Crypt;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.am.util.SystemProperties;
 import com.sun.identity.authentication.UI.LoginLogoutMapping;
 import com.sun.identity.authentication.config.AMAuthenticationManager;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.policy.PolicyException;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.encode.Hash;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.SMSException;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.security.AccessController;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * This class is the first class to get loaded by the Servlet container. 
  * It has helper methods to determine the status of Access Manager 
  * configuration when deployed as a single web-application. If 
  * Access Manager is not deployed as single web-application then the 
  * configured status returned is always true.   
  */
 public class AMSetupServlet extends HttpServlet {
     private ServletConfig config = null;
     private static ServletContext servletCtx = null;
     private static boolean isConfiguredFlag = false;
 
     private final static String AMC_OVERRIDE_PROPERTY = 
         "com.sun.identity.overrideAMC";
     private final static String DEBUG_NAME = "amSetupServlet";
     private final static String PROPERTY_CONFIGURATOR_PLUGINS =
         "configuratorPlugins";
     private final static String KEY_CONFIGURATOR_PLUGINS =
         "configurator.plugins";
     private final static String AMCONFIG = "AMConfig";
     private final static String SMS_STR = "sms";
     private final static String AMCONFIG_PROPERTIES = "AMConfig.properties";
     private static SSOToken adminToken = null;
     
     /*
      * Initializes the servlet.
      */  
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         System.setProperty("file.separator", "/");
         if (servletCtx == null ) {
             servletCtx = config.getServletContext();
         }
         checkConfigProperties();
     }
 
     /*
      * Flag indicating if Access Manager is configured
      */  
     static public boolean isConfigured() {
         return isConfiguredFlag;
     } 
 
     /**
      * Checks if the product is already configured. This is required when
      * the container on which WAR is deployed is restarted. If  
      * product is configured the flag is set true. Also the flag is
      * set to true in case of non-single war deployment.
      */
     public static void checkConfigProperties() {
         String overrideAMC = SystemProperties.get(AMC_OVERRIDE_PROPERTY);
         if (overrideAMC != null && overrideAMC.equalsIgnoreCase("true")) {
             try {
                 if (servletCtx != null) {
                     String bootstrap = getBootStrapFile();
                     FileReader frdr = new FileReader(bootstrap);
                     BufferedReader brdr = new BufferedReader(frdr);
                     String configLocation = brdr.readLine();
                     frdr.close();
                     String overridingAMC =  configLocation + "/" +
                         AMCONFIG_PROPERTIES; 
                     FileInputStream fin = new FileInputStream(overridingAMC);
                     if (fin != null) {
                         Properties oprops = new Properties();
                         oprops.load(fin);
                         SystemProperties.initializeProperties(oprops);
                        //dennis
                         isConfiguredFlag = true;
                     } else {
                         Debug.getInstance(DEBUG_NAME).error(
                             "AMSetupServlet.checkConfigProperties: " +
                             "Unable to open : " + overridingAMC);
                     }
                 } else {
                     Debug.getInstance(DEBUG_NAME).error(
                         "AMSetupServlet.checkConfigProperties: " +
                         "Context is null");
                 }
             } catch (FileNotFoundException fex) {
                 //nothing to do
             } catch (IOException ioex) {
                 if (Debug.getInstance(DEBUG_NAME).messageEnabled()) { 
                     Debug.getInstance(DEBUG_NAME).message(
                         "AMSetupServlet.checkConfigProperties: " +
                         "Exception in reading properties", ioex);
                 }
             }
         } else {
             isConfiguredFlag = true;
         }
     }
    
     /**
      * Invoked from the filter to decide which page needs to be 
      * displayed.
      * @param servletctx is the Servlet Context
      * @return true if AM is already configured, false otherwise 
      */
     public static boolean checkInitState(ServletContext servletctx) {
         try {
             servletCtx = servletctx;
             String bootstrap = getBootStrapFile();
             FileReader frdr = new FileReader(bootstrap);
             BufferedReader brdr = new BufferedReader(frdr);
             String base =  brdr.readLine();
             frdr.close();
             File configFile = new File(base + "/" + AMCONFIG_PROPERTIES);
 
             Map map = ServicesDefaultValues.getDefaultValues();
             String deployuri = (String)map.get("SERVER_URI");
             File smsFile = new File(base + deployuri + "/" + SMS_STR);
 
             if (configFile.exists() && smsFile.exists()) {
                 isConfiguredFlag = true;
             }
         } catch (FileNotFoundException fex) {
             // no action required. The server is not configured yet.
         } catch (IOException ioex) {
             if (Debug.getInstance(DEBUG_NAME).messageEnabled()) { 
                 Debug.getInstance(DEBUG_NAME).message(
                     "AMSetupServlet.checkInitState: " +
                     "Exception in reading properties", ioex);
             }
         }
         return isConfiguredFlag;
     }
 
     /**
      * Sets the configured flag 
      */
     public static void setConfigured(){
         isConfiguredFlag = true;
     }
 
     /**
      * The main enrty point for configuring Access Manager. The parameters
      * are passed from configurator page.
      *
      * @param request Servlet request.
      * @param response Servlet response. 
      * @return <code>true</code> if the configuration succeeded.
      */
     public static boolean processRequest(
         HttpServletRequest request, 
         HttpServletResponse response
     ) {
         setServiceDefaultValues(request);
         Map map = ServicesDefaultValues.getDefaultValues();
         String basedir = (String)map.get("BASE_DIR");
         String bootstrap = getBootStrapFile();
 
         try {
             if (bootstrap != null) {
                 FileWriter bfout = new FileWriter(bootstrap);
                 bfout.write(basedir+"\n");
                 bfout.close();
 
                 initializeConfigProperties();
                 reInitConfigProperties();
                 SSOToken adminSSOToken = getAdminSSOToken();
                 
                 RegisterServices regService = new RegisterServices();
                 regService.registers(adminSSOToken);
                 processDataRequests("WEB-INF/template/sms");
 
                 handlePostPlugins(adminSSOToken);
 
                 reInitConfigProperties();
                 AMAuthenticationManager.reInitializeAuthServices();
                 
                 AMIdentityRepository.clearCache();
                 ServiceManager svcMgr = new ServiceManager(adminSSOToken);
                 svcMgr.clearCache();
                 LoginLogoutMapping lmp = new LoginLogoutMapping();
                 lmp.initializeAuth(servletCtx);
                 AMSetupServlet.setConfigured();
                 LoginLogoutMapping.setProductInitialized(true);
                 return true;
             } else {      
                 Debug.getInstance(DEBUG_NAME).error(
                     "AMSetupServlet.processRequest: Bootstrap file is missing");
             }
         } catch (FileNotFoundException e) {
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.processRequest: " +
                 "File not found Exception occured", e);
             e.printStackTrace();
         } catch (SecurityException e) {
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         } catch (IOException e) {
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         } catch (SMSException e) {
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         } catch (PolicyException e) {
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         } catch (SSOException e) {
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         }
         return false;
     }
 
     private static void handlePostPlugins(SSOToken adminSSOToken) {
         List<ConfiguratorPlugin> plugins = getConfigPluginClasses();
         for (ConfiguratorPlugin plugin : plugins) {
             plugin.doPostConfiguration(servletCtx, adminSSOToken);
         }
     }
 
     private static List<ConfiguratorPlugin> getConfigPluginClasses() {
         List<ConfiguratorPlugin> plugins = new ArrayList<ConfiguratorPlugin>();
         try {
             ResourceBundle rb = ResourceBundle.getBundle(
                 PROPERTY_CONFIGURATOR_PLUGINS);
             String strPlugins = rb.getString(KEY_CONFIGURATOR_PLUGINS);
 
             if (strPlugins != null) {
                 StringTokenizer st = new StringTokenizer(strPlugins);
                 while (st.hasMoreTokens()) {
                     String className = st.nextToken();
                     Class clazz = Class.forName(className);
                     plugins.add((ConfiguratorPlugin)clazz.newInstance());
                 }
             }
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (MissingResourceException e) {
             //ignore if there are no configurator plugins.
         }
         return plugins;
     }
 
     private static void setServiceDefaultValues(HttpServletRequest request) {
         String portnumstr = request.getParameter("portnum");
         String hostname = request.getParameter("hostname");
         int portnum = Integer.parseInt(portnumstr);
         String protocol = request.getParameter("protocol");
         String deployuri = request.getParameter("deployuri");
         String basedir = request.getParameter("basedir");
         String cookieDomain = request.getParameter("cookieDomain");
         String adminPwd = request.getParameter("adminPwd").trim();
         String platformLocale = request.getParameter("locale");
  
         if (!isHostnameValid(hostname)) {
             throw new RuntimeException("Invalid host name.");
         }
         if (!isCookieDomainValid(cookieDomain)) {
             throw new RuntimeException("Invalid Cookie Domain.");
         }
         
         getCookieDomain(cookieDomain, hostname);
         String encryptAdminPwd = Crypt.encrypt(adminPwd);
         String hashAdminPwd = Hash.hash(adminPwd);
         
         Map map = ServicesDefaultValues.getDefaultValues();
         ServicesDefaultValues.setDeployURI(deployuri, map);
 
         String port = Integer.toString(portnum);
         map.put("SERVER_PROTO", protocol);
         map.put("SERVER_HOST", hostname);
         map.put("SERVER_PORT", port);
         
         map.put("IS_INSTALL_VARDIR", basedir);
         map.put("BASE_DIR", basedir);
         map.put("COOKIE_DOMAIN", cookieDomain);
         map.put("SUCCESS_REDIRECT_URL", protocol + "://" + hostname + ":" + 
             port + deployuri + "/base/AMAdminFrame");
 
         map.put("HASHADMINPASSWD", hashAdminPwd);
         map.put("HASHLDAPUSERPASSWD", hashAdminPwd);
         map.put("ENCADMINPASSWD", encryptAdminPwd);
         map.put("OUTPUT_DIR", basedir + "/" + deployuri);
         
         if (platformLocale != null) {
             map.put("PLATFORM_LOCALE", platformLocale);
             map.put("CURRENT_PLATFORM_LOCALE", platformLocale);
             map.put("AVAILABLE_LOCALES", platformLocale);
         }
     }
 
     /**
      * Reinitializes the system with the new properties values.
     **
      * @throws FileNotFoundException if config file is missing.
      * @throws IOException if config file cannot be read.
      */
    private static void reInitConfigProperties() 
        throws FileNotFoundException, IOException 
     {
        Map map = ServicesDefaultValues.getDefaultValues();
        String basedir = (String)map.get("BASE_DIR");
        reInitAMConfigProperties(basedir);
         List<ConfiguratorPlugin> plugins = getConfigPluginClasses();
 
         for (ConfiguratorPlugin plugin : plugins) {
             plugin.reinitConfiguratioFile(basedir);
         }
     }
 
     private static void reInitAMConfigProperties(String baseDir)
         throws FileNotFoundException, IOException
     {
         // Read config file and initialize
         String fileName = baseDir + "/" + AMCONFIG_PROPERTIES;
         try {
             FileInputStream FInpStr = new FileInputStream(fileName);
             if (FInpStr != null) {
                 Properties oprops = new Properties();
                 oprops.load(FInpStr);
                 SystemProperties.initializeProperties(oprops);
                 FInpStr.close();
             } else {
                 Debug.getInstance(DEBUG_NAME).error(
                    "AMSetupServlet.reInitAMConfigProperties: Unable to open: " +
                         fileName);
             }
         } catch (FileNotFoundException fexp) {
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.reInitAMConfigProperties: " +
                 "Unable to re-initialize properties", fexp);
             throw fexp;
         } catch (IOException ioexp) {
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.reInitAMConfigProperties: " +
                 "Unable to load properties", ioexp);
             throw ioexp;
         }
     }
 
     /**
      * Returns location of the bootstrap file.
      *
      * @return Location of the bootstrap file. Returns null if the file
      *         cannot be located 
      */
     private static String getBootStrapFile() {
         if (servletCtx != null) {
             String path = getAppResource();
             
             if (path != null) {
                 int idx1 = path.lastIndexOf("/");
                 if (idx1 != -1 ) {
                     int idx2 = path.lastIndexOf("/", idx1-1);
                     if (idx2 != -1 ) {
                         Map map = ServicesDefaultValues.getDefaultValues();
                         String deployuri= path.substring(idx2, idx1);
                         ServicesDefaultValues.setDeployURI(deployuri, map);
                     }
                 }
                 path = path.replaceAll("/", "_");
                 return System.getProperty("user.home") + "/" + AMCONFIG +
                     path;
             } else {
                 Debug.getInstance(DEBUG_NAME).error(
                     "AMSetupServlet.getBootStrapFile: " +
                     "Cannot read the bootstrap path");
             }
         } else {
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.getBootStrapFile: Context is null");
         }
         return null;
     }
 
     /**
      * Returns URL of the default resource.
      *
      * @return URL of the default resource. Returns null of servlet context is
      *         null.
      */
     private static String getAppResource() {
         if (servletCtx != null) {
             try {
                 java.net.URL turl = servletCtx.getResource("/");
                 return turl.getPath();
             } catch (MalformedURLException mue) {
                 Debug.getInstance(DEBUG_NAME).error(
                     "AMSetupServlet.getAppResource: Cannot access the resource",
                     mue);
             }
         } else {
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.getAppResource: Context is null");
         }
         return null;
     }
 
     /**
      * This method takes the name of XML file, process each 
      * request object one by one immediately after parsing.
      *
      * @param xmlBaseDir is the location of request xml files
      * @throws SMSException if error occurs in the service management space.
      * @throws SSOException if administrator single sign on is not valid.
      * @throws IOException if error accessing the configuration files.
      * @throws PolicyException if policy cannot be loaded.
      */
     private static void processDataRequests(String xmlBaseDir)
         throws SMSException, SSOException, IOException, PolicyException
     {
         SSOToken ssoToken = getAdminSSOToken();
         try {
             Map map = ServicesDefaultValues.getDefaultValues();
             String hostname = (String)map.get("SERVER_HOST");
             ConfigureData configData = new ConfigureData(
                 xmlBaseDir, servletCtx, hostname, ssoToken);
             configData.configure();
         } catch (SMSException e) {
             e.printStackTrace();
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.processDataRequests", e);
             throw e;
         } catch (SSOException e) {
             e.printStackTrace();
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.processDataRequests", e);
             throw e;
         } catch (IOException e) {
             e.printStackTrace();
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.processDataRequests", e);
             throw e;
         }
     }
 
     /**
      * Helper method to return Admin token
      * @return Admin Token
      */
     private static SSOToken getAdminSSOToken() {
         if (adminToken == null) {
             adminToken = (SSOToken)AccessController.doPrivileged(
                 AdminTokenAction.getInstance());
         }
         return adminToken;
     }
 
     /**
      * Initialize AMConfig.propeties with host specific values
      */
     private static void initializeConfigProperties()
         throws SecurityException, IOException {
         List<String> dataFiles = getTagSwapConfigFiles();
 
         String origpath = "@BASE_DIR@";
         Map map = ServicesDefaultValues.getDefaultValues();
         String basedir = (String)map.get("BASE_DIR");
         String deployuri = (String)map.get("SERVER_URI");
         String newpath = basedir;
         try {
             File fhm = new File(basedir + deployuri + "/" + SMS_STR);
             fhm.mkdirs();
         } catch (SecurityException e){
             Debug.getInstance(DEBUG_NAME).error(
                 "AMSetupServlet.initializeConfigProperties", e);
             throw e;
         }
 
          for (String file : dataFiles) {
             InputStreamReader fin = new InputStreamReader(
                 servletCtx.getResourceAsStream(file));
 
             StringBuffer sbuf = new StringBuffer();
             char[] cbuf = new char[1024];
             int len;
             while ((len = fin.read(cbuf)) > 0) {
                 sbuf.append(cbuf, 0, len);
             }
             FileWriter fout = null;
             
             int idx = file.lastIndexOf("/");
             String absFile = (idx != -1) ? file.substring(idx+1) : file;
             
             try {
                 fout = new FileWriter(basedir + "/" + absFile);
                 String inpStr = sbuf.toString();
                 fout.write(ServicesDefaultValues.tagSwap(inpStr));
             } catch (IOException e) {
                 Debug.getInstance(DEBUG_NAME).error(
                     "AMSetupServlet.initializeConfigProperties", e);
                 throw e;
             } finally {
                 if (fin != null) {
                     try {
                         fin.close();
                     } catch (Exception ex) {
                         //No handling requried
                     }
                 }  
                 if (fout != null) {
                     try {
                         fout.close();
                     } catch (Exception ex) {
                         //No handling requried
                     }
                 }
             }
         }
     }
 
     private static List<String> getTagSwapConfigFiles()
         throws MissingResourceException
     {
         List<String> fileNames = new ArrayList();
         ResourceBundle rb = ResourceBundle.getBundle("configuratorTagSwap");
         String strFiles = rb.getString("tagswap.files");
         StringTokenizer st = new StringTokenizer(strFiles);
         while (st.hasMoreTokens()) {
             fileNames.add(st.nextToken());
         }
         return fileNames;
     }
 
     private static String getCookieDomain(String cookieDomain, String hostname){
         int idx = hostname.lastIndexOf(".");
         
         if ((idx == -1) || (idx == (hostname.length() -1)) ||
             isIPAddress(hostname)
         ) {
             cookieDomain = "";
         } else if ((cookieDomain == null) || (cookieDomain.length() == 0)) {
             // try to determine the cookie domain if it is not set
             String topLevelDomain = hostname.substring(idx+1);
             int idx2 = hostname.lastIndexOf(".", idx-1);
             
             if ((idx2 != -1) && (idx2 < (idx -1))) {
                 cookieDomain = hostname.substring(idx2);
             }
         }
         return cookieDomain;
     }
 
     private static boolean isCookieDomainValid(String cookieDomain) {
         boolean valid = (cookieDomain == null) || (cookieDomain.length() == 0);
         
         if (!valid) {
             int idx1 = cookieDomain.lastIndexOf(".");
 
             // need to have a period and cannot be the last char.
             valid = (idx1 == -1) || (idx1 != (cookieDomain.length() -1));
 
             if (valid) {
                 int idx2 = cookieDomain.lastIndexOf(".", idx1-1);
                 /*
                  * need to be have a period before the last one e.g.
                  * .iplanet.com and cannot be ..com
                  */
                 valid = (idx2 != -1) && (idx2 < (idx1 -1));
             }
         }
         return valid;
     }
 
     private static boolean isIPAddress(String hostname) {
         StringTokenizer st = new StringTokenizer(hostname, ".");
         boolean isIPAddr = (st.countTokens() == 4);
         if (isIPAddr) {
             while (st.hasMoreTokens()) {
                 String token = st.nextToken();
                 try {
                     int node = Integer.parseInt(token);
                     isIPAddr = (node >= 0) && (node < 256);
                 } catch (NumberFormatException e) {
                     isIPAddr = false;
                 }
             }
         }
         return isIPAddr;
     }
 
     /*
      * valid: localhost (no period)
      * valid: abc.sun.com (two periods)
      */
     private static boolean isHostnameValid(String hostname) {
         boolean valid = (hostname != null) && (hostname.length() > 0);
         if (valid) {
             int idx = hostname.lastIndexOf(".");
             if ((idx != -1) && (idx != (hostname.length() -1))) {
                 int idx1 = hostname.lastIndexOf(".", idx-1);
                 valid = (idx1 != -1) && (idx1 < (idx -1));
             }
         }
         return valid;
     }
 }
