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
 * $Id: AMSetupServlet.java,v 1.18 2007-06-07 21:11:24 veiming Exp $
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
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.policy.PolicyException;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.encode.Base64;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.ServiceSchemaManager;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.AttributeSchema;
 import com.sun.identity.sm.OrganizationConfigManager;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.security.AccessController;
 import java.security.SecureRandom;
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
     private static ServletContext servletCtx = null;
     private static boolean isConfiguredFlag = false;
 
     private final static String AMCONFIG = "AMConfig";
     private final static String SMS_STR = "sms";
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
         LoginLogoutMapping.setProductInitialized(isConfiguredFlag);
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
         String overrideAMC = SystemProperties.get(
             SetupConstants.AMC_OVERRIDE_PROPERTY);
         if (overrideAMC != null && overrideAMC.equalsIgnoreCase("true")) {
             try {
                 if (servletCtx != null) {
                     String configLocation = getConfigDirectory();
                     String overridingAMC =  configLocation + "/" +
                         SetupConstants.AMCONFIG_PROPERTIES; 
                     FileInputStream fin = new FileInputStream(overridingAMC);
                     if (fin != null) {
                         Properties oprops = new Properties();
                         oprops.load(fin);
                         SystemProperties.initializeProperties(oprops);
                         reInitConfigProperties(configLocation, false);
                         isConfiguredFlag = true;
                     } else {
                         Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                             "AMSetupServlet.checkConfigProperties: " +
                             "Unable to open : " + overridingAMC);
                     }
                 } else {
                     Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                         "AMSetupServlet.checkConfigProperties: " +
                         "Context is null");
                 }
             } catch (ConfiguratorException e) {
                  Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "AMSetupServlet.checkConfigProperties: " +
                     "Exception in getting bootstrap information", e);
             } catch (IOException ioex) {
                 if (Debug.getInstance(
                     SetupConstants.DEBUG_NAME).messageEnabled()
                 ) { 
                     Debug.getInstance(SetupConstants.DEBUG_NAME).message(
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
         return isConfiguredFlag;
     }
 
     /**
      * The main entry point for configuring Access Manager. The parameters
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
         ServicesDefaultValues.setServiceConfigValues(request);
         Map map = ServicesDefaultValues.getDefaultValues();
 
         try {
             createBootstrapFile(map);
             initializeConfigProperties();
             reInitConfigProperties();
             boolean isDITLoaded = ((String)map.get(
                 SetupConstants.DIT_LOADED)).equals("true");
                 
             String dataStore = (String)map.get(
                 SetupConstants.CONFIG_VAR_DATA_STORE);
             boolean isDSServer = dataStore.equals(
                 SetupConstants.SMS_DS_DATASTORE);
             boolean isADServer = (isDSServer) ? false : dataStore.equals(
                 SetupConstants.SMS_AD_DATASTORE);
 
             if ((isDSServer || isADServer) && !isDITLoaded) {
                 boolean loadSDKSchema = (isDSServer) ? ((String)map.get(
                     SetupConstants.CONFIG_VAR_DS_UM_SCHEMA)).equals(
                         "sdkSchema") : false;
                 List schemaFiles = getSchemaFiles(dataStore, loadSDKSchema);
                 String basedir = (String)map.get(
                     SetupConstants.CONFIG_VAR_BASE_DIR);
                 writeSchemaFiles(basedir, schemaFiles);
             }
 
             String hostname = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_HOST);
             String serverURL = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_URL);
             SSOToken adminSSOToken = getAdminSSOToken();
 
             if (!isDITLoaded) {
                 RegisterServices regService = new RegisterServices();
                 regService.registers(adminSSOToken);
                processDataRequests("WEB-INF/template/sms");
             } else {
                 if (isDSServer || isADServer) {
                     //Update the platform server list
                     updatePlatformServerList(serverURL, hostname);
                 }
             }
             handlePostPlugins(adminSSOToken);
 
             reInitConfigProperties();
             AMAuthenticationManager.reInitializeAuthServices();
                 
             AMIdentityRepository.clearCache();
             ServiceManager svcMgr = new ServiceManager(adminSSOToken);
             svcMgr.clearCache();
             LoginLogoutMapping lmp = new LoginLogoutMapping();
             lmp.initializeAuth(servletCtx);
             String deployuri = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_URI);
             /*
              * requiring the keystore.jks file in OpenSSO workspace. The
              * createIdentitiesForWSSecurity is for the JavaEE/NetBeans 
              * integration that we had done.
              * TODO: Uncomment these two line after we have fixed all 
              *       related issue in OpenSSO workspace.
              */
             //createPasswordFiles(basedir, deployuri);
             //createIdentitiesForWSSecurity(serverURL, deployuri);
             isConfiguredFlag = true;
             LoginLogoutMapping.setProductInitialized(true);
             return true;
         } catch (FileNotFoundException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processRequest: " +
                 "File not found Exception occured", e);
             e.printStackTrace();
         } catch (SecurityException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         } catch (IOException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         } catch (SMSException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         } catch (PolicyException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         } catch (ConfiguratorException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         } catch (SSOException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processRequest", e);
             e.printStackTrace();
         }
         return false;
     }
 
     private static void handlePostPlugins(SSOToken adminSSOToken) {
         List plugins = getConfigPluginClasses();
         for (Iterator i = plugins.iterator(); i.hasNext(); ) {
             ConfiguratorPlugin plugin  = (ConfiguratorPlugin)i.next();
             plugin.doPostConfiguration(servletCtx, adminSSOToken);
         }
     }
 
     private static List getConfigPluginClasses() {
         List plugins = new ArrayList();
         try {
             ResourceBundle rb = ResourceBundle.getBundle(
                 SetupConstants.PROPERTY_CONFIGURATOR_PLUGINS);
             String strPlugins = rb.getString(
                 SetupConstants.KEY_CONFIGURATOR_PLUGINS);
 
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
 
     private static void reInitConfigProperties() 
         throws FileNotFoundException, IOException {
         Map map = ServicesDefaultValues.getDefaultValues();
         String basedir = (String)map.get(
             SetupConstants.CONFIG_VAR_BASE_DIR);
         reInitConfigProperties(basedir, true);
     }
 
     /**
      * Reinitializes the system with the new properties values.
      *
      * @throws FileNotFoundException if config file is missing.
      * @throws IOException if config file cannot be read.
      */
     private static void reInitConfigProperties(
         String basedir, 
         boolean initAMConfig
     ) throws FileNotFoundException, IOException 
     {
         if (initAMConfig) {
             reInitAMConfigProperties(basedir);
         }
         List plugins = getConfigPluginClasses();
 
         for (Iterator i = plugins.iterator(); i.hasNext(); ) {
             ConfiguratorPlugin plugin = (ConfiguratorPlugin)i.next();
             plugin.reinitConfiguratioFile(basedir);
         }
     }
 
     private static void reInitAMConfigProperties(String baseDir)
         throws FileNotFoundException, IOException
     {
         // Read config file and initialize
         String fileName = baseDir + "/" + SetupConstants.AMCONFIG_PROPERTIES;
         try {
             FileInputStream FInpStr = new FileInputStream(fileName);
             if (FInpStr != null) {
                 Properties oprops = new Properties();
                 oprops.load(FInpStr);
                 SystemProperties.initializeProperties(oprops);
                 FInpStr.close();
             } else {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                    "AMSetupServlet.reInitAMConfigProperties: Unable to open: " +
                         fileName);
             }
         } catch (FileNotFoundException fexp) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.reInitAMConfigProperties: " +
                 "Unable to re-initialize properties", fexp);
             throw fexp;
         } catch (IOException ioexp) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.reInitAMConfigProperties: " +
                 "Unable to load properties", ioexp);
             throw ioexp;
         }
     }
 
     private static String getPresetConfigDir() {
         String configDir = null;
         try {
             ResourceBundle rb = ResourceBundle.getBundle(
                 SetupConstants.BOOTSTRAP_PROPERTIES_FILE);
             configDir = rb.getString(SetupConstants.PRESET_CONFIG_DIR);
         } catch (MissingResourceException e) {
             //ignored because bootstrap properties file maybe absent.
         }
         return configDir;
     }
 
     private static String getConfigDirectory()
         throws IOException {
         String configDir = null;
         String presetConfigDir = getPresetConfigDir();
         if ((presetConfigDir != null) && (presetConfigDir.length() > 0)) {
             String realPath = getNormalizedRealPath(servletCtx);
             if (realPath != null) {
                 configDir = presetConfigDir + "/" +
                     SetupConstants.CONFIG_VAR_BOOTSTRAP_BASE_PREFIX + realPath;
                 File f = new File(configDir);
                 if (!f.exists()) {
                     configDir = null;
                 }
             } else {
                 throw new ConfiguratorException(
                     "cannot get configuration path");
             }
         } else {
             try {
                 String bootstrap = getBootStrapFile();
                 FileReader frdr = new FileReader(bootstrap);
                 BufferedReader brdr = new BufferedReader(frdr);
                 configDir = brdr.readLine();
                 frdr.close();
             } catch (FileNotFoundException e) {
                 //ignore: war is not configured
             }
         }
         return configDir;
     }
 
     private static void createBootstrapFile(Map configMap)
         throws ConfiguratorException, IOException {
         String configDir = getPresetConfigDir();
         if ((configDir != null) && (configDir.length() > 0)) {
             String realPath = getNormalizedRealPath(servletCtx);
             if (realPath != null) {
                 String basedir = configDir + "/" +
                     SetupConstants.CONFIG_VAR_BOOTSTRAP_BASE_PREFIX + realPath;
                 configMap.put(SetupConstants.CONFIG_VAR_BASE_DIR, basedir);
             } else {
                 throw new ConfiguratorException(
                     "cannot get configuration path");
             }
         } else {
             String bootstrap = getBootStrapFile();
             File btsFile = new File(bootstrap);
             if (!btsFile.getParentFile().exists()) {
                 btsFile.getParentFile().mkdirs();
             }
             String basedir = (String)configMap.get(
                 SetupConstants.CONFIG_VAR_BASE_DIR);
             FileWriter bfout = new FileWriter(bootstrap);
             bfout.write(basedir + "\n");
             bfout.close();
         }
     }
 
     /**
      * Returns location of the bootstrap file.
      *
      * @return Location of the bootstrap file. Returns null if the file
      *         cannot be located 
      * @throws ConfiguratorException if servlet context is null or deployment
      *         application real path cannot be determined.
      */
     private static String getBootStrapFile()
         throws ConfiguratorException {
         String bootFile = null;
         if (servletCtx != null) {
             String path = getNormalizedRealPath(servletCtx);
             if (path != null) {
                 bootFile = System.getProperty("user.home") + "/" +
                     SetupConstants.CONFIG_VAR_BOOTSTRAP_BASE_DIR + "/" + 
                     SetupConstants.CONFIG_VAR_BOOTSTRAP_BASE_PREFIX + path;
             } else {
                 throw new ConfiguratorException(
                     "Cannot read the bootstrap path");
             }
         } else {
             throw new ConfiguratorException("Servlet Context is null");
         }
         return bootFile;
     }
 
     public static String getNormalizedRealPath(ServletContext servletCtx) {
         String path = null;
         if (servletCtx != null) {
             path = getAppResource(servletCtx);
             
             if (path != null) {
                 String realPath = servletCtx.getRealPath("/");
                 if ((realPath != null) && (realPath.length() > 0)) {
                     realPath = realPath.replace('\\', '/');
                     path = realPath.replaceAll("/", "_");
                 } else {
                     path = path.replaceAll("/", "_");
                 }
                 int idx = path.indexOf(":");
                 if (idx != -1) {
                     path = path.substring(idx + 1);
                 }
             }
         }
         return path;
     }
 
     /**
      * Returns URL of the default resource.
      *
      * @return URL of the default resource. Returns null of servlet context is
      *         null.
      */
     private static String getAppResource(ServletContext servletCtx) {
         if (servletCtx != null) {
             try {
                 java.net.URL turl = servletCtx.getResource("/");
                 return turl.getPath();
             } catch (MalformedURLException mue) {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "AMSetupServlet.getAppResource: Cannot access the resource",
                     mue);
             }
         } else {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
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
             String hostname = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_HOST);
             ConfigureData configData = new ConfigureData(
                 xmlBaseDir, servletCtx, hostname, ssoToken);
             configData.configure();
         } catch (SMSException e) {
             e.printStackTrace();
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processDataRequests", e);
             throw e;
         } catch (SSOException e) {
             e.printStackTrace();
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processDataRequests", e);
             throw e;
         } catch (IOException e) {
             e.printStackTrace();
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
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
         List dataFiles = getTagSwapConfigFiles();
 
         String origpath = "@BASE_DIR@";
         Map map = ServicesDefaultValues.getDefaultValues();
         String basedir = (String)map.get(
             SetupConstants.CONFIG_VAR_BASE_DIR);
         String deployuri = (String)map.get(
             SetupConstants.CONFIG_VAR_SERVER_URI);
         String newpath = basedir;
         try {
             File fhm = new File(basedir + deployuri + "/" + SMS_STR);
             fhm.mkdirs();
         } catch (SecurityException e){
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.initializeConfigProperties", e);
             throw e;
         }
 
          for (Iterator i = dataFiles.iterator(); i.hasNext(); ) {
             String file = (String)i.next();
             InputStreamReader fin = new InputStreamReader(
                 servletCtx.getResourceAsStream(file));
 
             StringBuffer sbuf = new StringBuffer();
             char[] cbuf = new char[1024];
             int len;
             while ((len = fin.read(cbuf)) > 0) {
                 sbuf.append(cbuf, 0, len);
             }
 
             int idx = file.lastIndexOf("/");
             String absFile = (idx != -1) ? file.substring(idx+1) : file;
 
             if (absFile.equalsIgnoreCase(SetupConstants.AMCONFIG_PROPERTIES)) {
                 String dataStore = (String)map.get(
                     SetupConstants.CONFIG_VAR_DATA_STORE);
                 boolean isDSServer = dataStore.equals(
                     SetupConstants.SMS_DS_DATASTORE);
                 boolean isADServer = (isDSServer) ? false : dataStore.equals(
                     SetupConstants.SMS_AD_DATASTORE);
 
                 if (isDSServer || isADServer) {
                     int idx1 = sbuf.indexOf(
                         SetupConstants.CONFIG_VAR_SMS_DATASTORE_CLASS);
                     if (idx1 != -1) {
                        sbuf.replace(idx1, idx1 + 
                           (SetupConstants.CONFIG_VAR_SMS_DATASTORE_CLASS)
                               .length(), 
                                   SetupConstants.CONFIG_VAR_DS_DATASTORE_CLASS);
                     }
                 }
             }
             FileWriter fout = null;
 
             try {
                 fout = new FileWriter(basedir + "/" + absFile);
                 String inpStr = sbuf.toString();
                 fout.write(ServicesDefaultValues.tagSwap(inpStr));
             } catch (IOException e) {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
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
 
     /**
      * Returns secure random string.
      *
      * @return secure random string.
      */
     public static String getRandomString() {
         String randomStr = null;
         try {
             byte [] bytes = new byte[24];
             SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
             random.nextBytes(bytes);
             randomStr = Base64.encode(bytes).trim();
         } catch (Exception e) {
             randomStr = null;
             Debug.getInstance(SetupConstants.DEBUG_NAME).message(
                 "AMSetupServlet.getRandomString:" +
                 "Exception in generating encryption key.", e);
             e.printStackTrace();
         }
         return (randomStr != null) ? randomStr : 
             SetupConstants.CONFIG_VAR_DEFAULT_SHARED_KEY;
     }
 
 
     /**
      * Returns schema file names.
      *
      * @param dataStore Name of data store configuration data.
      * @param sdkSchema <code>true</code> to include access manager SDK ldif
      *        file.
      * @return schema file names to be loaded.
      * @throws MissingResourceException if the bundle cannot be found.
      */
 
     private static List getSchemaFiles(String dataStore, boolean sdkSchema)
         throws MissingResourceException
     {
         List fileNames = new ArrayList();
         ResourceBundle rb = ResourceBundle.getBundle(
             SetupConstants.SCHEMA_PROPERTY_FILENAME);
         String strFiles;
         boolean isDSServer = dataStore.equals(
             SetupConstants.SMS_DS_DATASTORE);
 
         if (isDSServer) {
             if (sdkSchema) {
                 strFiles = rb.getString(SetupConstants.SDK_PROPERTY_FILENAME);
             } else {
                 strFiles = rb.getString(SetupConstants.DS_SMS_PROPERTY_FILENAME);
             }
         } else {
             strFiles = rb.getString(SetupConstants.AD_SMS_PROPERTY_FILENAME);
         }
         
         StringTokenizer st = new StringTokenizer(strFiles);
         while (st.hasMoreTokens()) {
             fileNames.add(st.nextToken());
         }
         return fileNames;
     }
 
     private static List getTagSwapConfigFiles()
         throws MissingResourceException
     {
         List fileNames = new ArrayList();
         ResourceBundle rb = ResourceBundle.getBundle("configuratorTagSwap");
         String strFiles = rb.getString("tagswap.files");
         StringTokenizer st = new StringTokenizer(strFiles);
         while (st.hasMoreTokens()) {
             fileNames.add(st.nextToken());
         }
         return fileNames;
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
 
     /**
      * Tag swaps strings in schema files.
      *
      * @param basedir the configuration base directory.
      * @param schemaFiles List of schema files to be loaded.
      * @throws IOException if data files cannot be written.
      */
     private static void writeSchemaFiles(
         String basedir,
         List schemaFiles
     )   throws IOException
     {
         for (Iterator i = schemaFiles.iterator(); i.hasNext(); ) {
             String file = (String)i.next();
             InputStreamReader fin = new InputStreamReader(
                 servletCtx.getResourceAsStream(file));
 
             StringBuffer sbuf = new StringBuffer();
             char[] cbuf = new char[1024];
             int len;
             while ((len = fin.read(cbuf)) > 0) {
                 sbuf.append(cbuf, 0, len);
             }
             FileWriter fout = null;
             try {
                 int idx = file.lastIndexOf("/");
                 String absFile = (idx != -1) ? file.substring(idx+1) : file;
                 fout = new FileWriter(basedir + "/" + absFile);
                 String inpStr = sbuf.toString();
                 fout.write(ServicesDefaultValues.tagSwap(inpStr));
             } catch (IOException ioex) {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "AMSetupDSConfig.writeSchemaFiles: " +
                     "Exception in writing schema files:" , ioex);
                 throw ioex;
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
         AMSetupDSConfig dsConfig = AMSetupDSConfig.getInstance();
         dsConfig.loadSchemaFiles(schemaFiles);
     }
 
     /**
      * Create the storepass and keypass files
      *
      * @param basedir the configuration base directory.
      * @param deployuri the deployment URI. 
      * @throws IOException if password files cannot be written.
      */
     private static void createPasswordFiles(
         String basedir, 
         String deployuri 
     ) throws IOException
     {
         String pwd = Crypt.encrypt("secret");
         String location = basedir + deployuri + "/" ;
         writeContent(location + ".keypass", pwd);
         writeContent(location + ".storepass", pwd);
 
         InputStream in = servletCtx.getResourceAsStream(
                 "/WEB-INF/template/sms/keystore.jks");
         byte[] b = new byte[2007];
         in.read(b);
         in.close();
         FileOutputStream fos = new FileOutputStream(location + "keystore.jks");
         fos.write(b);
         fos.flush();
         fos.close();
     }
 
     /**
      * Helper method to create the storepass and keypass files
      *
      * @param fName is the name of the file to create.
      * @param content is the password to write in the file.
      */
     private static void writeContent(String fName, String content)
         throws IOException
     {
         FileWriter fout = null;
         try {
             fout = new FileWriter(new File(fName));
             fout.write(content);
         } catch (IOException ioex) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.writeContent: " +
                 "Exception in creating password files:" , ioex);
             throw ioex;
         } finally {
             if (fout != null) {
                 try {
                     fout.close();
                 } catch (Exception ex) {
                     //No handling requried
                 }
             }
         }
     }
 
        /**
      * Update platform server list and Organization alias
      */
     private static void updatePlatformServerList(
         String serverURL,
         String hostName
     ) throws SMSException, SSOException 
     {
         SSOToken token = getAdminSSOToken();
         ServiceSchemaManager ssm = new ServiceSchemaManager(
             "iPlanetAMPlatformService", token);
         ServiceSchema ss = ssm.getGlobalSchema();
         AttributeSchema as = ss.getAttributeSchema(
             "iplanet-am-platform-server-list");
         Set values = as.getDefaultValues();
         int instanceNumber, maxNumber = 1;
         // Iterate through the values to find the max. instance names
         for (Iterator items = values.iterator(); items.hasNext();) {
             String item = (String) items.next();
             int index1 = item.indexOf('|');
             if (index1 == -1) {
                 continue;
             }
             int index2 = item.indexOf('|', index1 + 1);
             if (index2 == -1) {
                 item = item.substring(index1 + 1);
             } else {
                 item = item.substring(index1 + 1, index2);
             }
             try {
                 int n = Integer.parseInt(item);
                 if (n > maxNumber) {
                     maxNumber = n;
                 }
             } catch (NumberFormatException nfe) {
                 // Ignore and continue
             }
         }
         String instanceName = Integer.toString(maxNumber + 1);
         if (instanceName.length() == 1) {
             instanceName = "0" + instanceName;
         }
         values.add(serverURL + "|" + instanceName);
         as.setDefaultValues(values);
 
         // Update Organization Aliases
         OrganizationConfigManager ocm =
             new OrganizationConfigManager(token, "/");
         values = new HashSet();
         values.add(hostName);
         ocm.addAttributeValues("sunIdentityRepositoryService",
             "sunOrganizationAliases", values);
     }
 
     /**
      * Creates Identities for WS Security
      *
      * @param serverURL URL at which Access Manager is configured.
      */
     private static void createIdentitiesForWSSecurity(
         String serverURL,
         String deployuri
     ) throws IdRepoException, SSOException
     {
         SSOToken token = getAdminSSOToken();
         AMIdentityRepository idrepo = new AMIdentityRepository(token, "/");
         createUser(idrepo, "jsmith", "John", "Smith");
         createUser(idrepo, "jondoe", "Jon", "Doe");
         HashSet config = new HashSet();
 
         // Add WSC configuration
         config.add("SecurityMech=urn:sun:wss:security:null:UserNameToken");
         config.add("UserCredential=UserName:testuser|UserPassword:test");
         config.add("useDefaultStore=true");
         config.add("isResponseSign=true");
         createAgent(idrepo, "wsc", "WSC", "", config);
 
         // Add WSC configuration
         createAgent(idrepo, "wsp", "WSP", "", config);
 
         // Add UsernameToken profile
         createAgent(idrepo, "UserNameToken", "WSP",
             "WS-I BSP UserName Token Profile Configuration", config);
 
         // Add SAML-HolderOfKey
         config.remove("SecurityMech=urn:sun:wss:security:null:UserNameToken");
         config.remove("UserCredential=UserName:testuser|UserPassword:test");
         config.add("SecurityMech=urn:sun:wss:security:null:SAMLToken-HK");
         createAgent(idrepo, "SAML-HolderOfKey", "WSP",
             "WS-I BSP SAML Holder Of Key Profile Configuration", config);
 
         // Add SAML-SenderVouches
         config.remove("SecurityMech=urn:sun:wss:security:null:SAMLToken-HK");
         config.add("SecurityMech=urn:sun:wss:security:null:SAMLToken-SV");
         createAgent(idrepo, "SAML-SenderVouches", "WSP",
             "WS-I BSP SAML Sender Vouches Token Profile Configuration", config);
 
         // Add X509Token
         config.remove("SecurityMech=urn:sun:wss:security:null:SAMLToken-SV");
         config.add("SecurityMech=urn:sun:wss:security:null:X509Token");
         createAgent(idrepo, "X509Token", "WSP",
             "WS-I BSP X509 Token Profile Configuration", config);
 
         // Add LibertyX509Token
         config.add("TrustAuthority=LocalDisco");
         config.add("WSPEndpoint=http://wsp.com");
         config.remove("SecurityMech=urn:sun:wss:security:null:X509Token");
         config.add("SecurityMech=urn:liberty:security:2005-02:null:X509");
         createAgent(idrepo, "LibertyX509Token", "WSP",
             "Liberty X509 Token Profile Configuration", config);
 
         // Add LibertyBearerToken
         config.remove("SecurityMech=urn:liberty:security:2005-02:null:X509");
         config.add("SecurityMech=urn:liberty:security:2005-02:null:Bearer");
         createAgent(idrepo, "LibertyBearerToken", "WSP",
             "Liberty SAML Bearer Token Profile Configuration", config);
 
         // Add LibertySAMLToken
         config.remove("SecurityMech=urn:liberty:security:2005-02:null:Bearer");
         config.add("SecurityMech=urn:liberty:security:2005-02:null:SAML");
         createAgent(idrepo, "LibertySAMLToken", "WSP",
             "Liberty SAML Token Profile Configuration", config);
 
         // Add local discovery service
         config.clear();
         config.add("Name=LocalDisco");
         config.add("Type=Discovery");
         config.add("Endpoint=" + serverURL + deployuri + "/Liberty/disco");
         createAgent(idrepo, "LocalDisco", "Discovery",
             "Local Liberty Discovery Service Configuration", config);
     }
 
     private static void createUser(
         AMIdentityRepository idrepo,
         String uid,
         String gn,
         String sn
     ) throws IdRepoException, SSOException 
     {
         Map attributes = new HashMap();
         Set values = new HashSet();
         values.add(uid);
         attributes.put("uid", values);
         values = new HashSet();
         values.add(gn);
         attributes.put("givenname", values);
         values = new HashSet();
         values.add(sn);
         attributes.put("sn", values);
         values = new HashSet();
         values.add(gn + " " + sn);
         attributes.put("cn", values);
         values = new HashSet();
         values.add(uid);
         attributes.put("userPassword", values);
         AMIdentity id = idrepo.createIdentity(IdType.USER, uid, attributes);
         id.assignService("sunIdentityServerDiscoveryService",
             Collections.EMPTY_MAP);
     }
 
 
     private static void createAgent(
         AMIdentityRepository idrepo,
         String name,
         String type,
         String desc,
         Set config
     ) throws IdRepoException, SSOException 
     {
         Map attributes = new HashMap();
         Set values = new HashSet();
         values.add(name+type);
         attributes.put("uid", values);
         values = new HashSet();
         values.add(name);
         attributes.put("userpassword", values);
         values = new HashSet();
         values.add(desc);
         attributes.put("description", values);
         attributes.put("sunIdentityServerDeviceKeyValue", config);
         idrepo.createIdentity(IdType.AGENT, name+type, attributes);
     }
 
 }
