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
 * $Id: AMSetupServlet.java,v 1.36 2008-01-15 07:53:59 qcheng Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 
 import com.iplanet.am.util.AdminUtils;
 import com.iplanet.services.ldap.DSConfigMgr;
 import com.iplanet.services.naming.WebtopNaming;
 import com.iplanet.services.util.Crypt;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.ldap.LDAPServiceException;
 import com.sun.identity.authentication.UI.LoginLogoutMapping;
 import com.sun.identity.authentication.config.AMAuthenticationManager;
 import com.sun.identity.authentication.internal.server.SMSAuthModule;
 import com.sun.identity.common.DebugPropertiesObserver;
 import com.sun.identity.common.configuration.ConfigurationObserver;
 import com.sun.identity.common.configuration.ConfigurationException;
 import com.sun.identity.common.configuration.ServerConfiguration;
 import com.sun.identity.common.configuration.UnknownPropertyNameException;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.policy.PolicyException;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.encode.Base64;
 import com.sun.identity.sm.AttributeSchema;
 import com.sun.identity.sm.OrganizationConfigManager;
 import com.sun.identity.sm.ServiceConfig;
 import com.sun.identity.sm.ServiceConfigManager;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.SMSException;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
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
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import java.security.AccessController;
 import java.security.SecureRandom;
 import java.util.Enumeration;
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
     private final static String LEGACY_PROPERTIES = "legacy";
 
     final static String BOOTSTRAP_EXTRA = "bootstrap";    
     final static String BOOTSTRAP_FILE_LOC = "bootstrap.file";
 
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
         isConfiguredFlag = overrideAMC == null || 
             overrideAMC.equalsIgnoreCase("false");
         
         if ((!isConfiguredFlag) && (servletCtx != null)) {
             try {
                 isConfiguredFlag = Bootstrap.load(getBootstrap(), false);
             } catch (ConfiguratorException e) {
                  Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "AMSetupServlet.checkConfigProperties: " +
                     "Exception in getting bootstrap information", e);
             } catch (Exception e) {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "AMSetupServlet.checkConfigProperties", e);
             }
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
         HttpServletRequestWrapper req = new HttpServletRequestWrapper(request);
         HttpServletResponseWrapper res = new HttpServletResponseWrapper(
             response);
         return processRequest(req, res);
     }
     
     public static boolean processRequest(
         IHttpServletRequest request,
         IHttpServletResponse response
     ) {
         /*
          * This logic needs refactoring later. setServiceConfigValues()
          * attempts to check if directory is up and makes a call
          * back to this class. The implementation'd
          * be cleaner if classes&methods are named better and separated than 
          * intertwined together.
          */
         ServicesDefaultValues.setServiceConfigValues(request);
         Map map = ServicesDefaultValues.getDefaultValues();
 
         try {
             Map bootstrapRes = createBootstrapResource(false);
             isConfiguredFlag = Bootstrap.load(
                 Bootstrap.createBootstrapResource(bootstrapRes, true), true) || 
                 configure(map);
             if (isConfiguredFlag) {
                 postInitialize(getAdminSSOToken());
             }
             LoginLogoutMapping.setProductInitialized(isConfiguredFlag);
             
             if (isConfiguredFlag) {
                 boolean legacy = ServerConfiguration.isLegacy();
                 bootstrapRes = createBootstrapResource(legacy);
                 String fileBootstrap = getBootStrapFile();
                 String url = Bootstrap.create(fileBootstrap, bootstrapRes, 
                     legacy);
 
                 if (!legacy) {
                     createExtraBootstrapFile(url);
                     
                     // this is to store the bootstrap location
                     SSOToken adminToken = (SSOToken)AccessController.
                         doPrivileged(AdminTokenAction.getInstance());
                     Map mapBootstrap = new HashMap(2);
                     Set set = new HashSet(2);
                     set.add(fileBootstrap);
                     mapBootstrap.put(BOOTSTRAP_FILE_LOC, set);
                     ServerConfiguration.setServerInstance(adminToken,
                         SystemProperties.getServerInstanceName(), mapBootstrap);
                 }
             }
         } catch (Exception e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processRequest: error.", e);
             e.printStackTrace();
         }
         return isConfiguredFlag;
     }
     
     private static boolean configure(Map map) {
         boolean configured = false;
         try {
             String basedir = (String)map.get(
                 SetupConstants.CONFIG_VAR_BASE_DIR);
             File baseDirectory = new File(basedir);
             if (!baseDirectory.exists()) {
                 baseDirectory.mkdir();
             } else {
                 SetupProgress.reportStart("emb.checkingbasedir",basedir);
                 File bootstrapFile = new File(basedir+"/"+BOOTSTRAP_EXTRA);
                 File opendsDir = new File(basedir+"/opends");
                 if (bootstrapFile.exists() || opendsDir.exists()) {
                     SetupProgress.reportEnd("emb.basedirfailed", null);
                     throw new ConfiguratorException(
                         "Base directory specified :"+
                         basedir+
                         " cannot be used - has preexisting config data.");
                 }
                 SetupProgress.reportEnd("emb.success", null);
             }
             boolean isDITLoaded = ((String)map.get(
                 SetupConstants.DIT_LOADED)).equals("true");
                 
             String dataStore = (String)map.get(
                 SetupConstants.CONFIG_VAR_DATA_STORE);
             boolean embedded = 
                   dataStore.equals(SetupConstants.SMS_EMBED_DATASTORE);
             boolean isDSServer = false;
             boolean isADServer = false;
             if (embedded) {
                 isDSServer = true;
             } else { // Keep old behavior for now.
                 isDSServer = dataStore.equals(SetupConstants.SMS_DS_DATASTORE);
                 isADServer = dataStore.equals(SetupConstants.SMS_AD_DATASTORE);
             }
 
             if (embedded == true) {
                 // (i) install, configure and start an embedded instance.
                 // or
                 // (ii) install, configure, and replicate embedded instance
                 try {
                     EmbeddedOpenDS.setup(map, servletCtx);
                     // Now create the AMSetupDSConfig instance.Abort on failure
                     AMSetupDSConfig dsConfig = AMSetupDSConfig.getInstance();
                     if (!dsConfig.isDServerUp()) {
                         Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                          "AMSetupServlet.processRequest:OpenDS conn failed.");
                         return false;
                     }
 
                     // Determine if DITLoaded flag needs to be set : multi instance
                     if (EmbeddedOpenDS.isMultiServer(map)) {
                         // Replication 
                         // Temporary fix until OpenDS auto-loads schema
                         boolean loadSDKSchema = (isDSServer) ? ((String)map.get(
                           SetupConstants.CONFIG_VAR_DS_UM_SCHEMA)).equals(
                           "sdkSchema") : false;
                         List schemaFiles = getSchemaFiles(dataStore, 
                                                            loadSDKSchema);
                         writeSchemaFiles(basedir, schemaFiles);
                         EmbeddedOpenDS.setupReplication(map);
                         isDITLoaded = true;
                     }
                 } catch (Exception ex) {
                     Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                         "AMSetupServlet.configure: embedded = true", ex);
                     ex.printStackTrace();
                     throw new ConfiguratorException(
                         "Error setting up embedded ds:ex="+ex);
                 }
             }
 
             if ((isDSServer || isADServer ) && !isDITLoaded) {
                 boolean loadSDKSchema = (isDSServer) ? ((String)map.get(
                     SetupConstants.CONFIG_VAR_DS_UM_SCHEMA)).equals(
                         "sdkSchema") : false;
                 List schemaFiles = getSchemaFiles(dataStore, loadSDKSchema);
                 writeSchemaFiles(basedir, schemaFiles);
             }
 
             Map mapFileNameToConfig = initializeConfigProperties();
             String strAMConfigProperties = (String)
                 mapFileNameToConfig.get(SetupConstants.AMCONFIG_PROPERTIES);
             String strServerConfigXML = (String)mapFileNameToConfig.get(
                 SystemProperties.CONFIG_FILE_NAME);
             Properties propAMConfig = ServerConfiguration.getProperties(
                 strAMConfigProperties);
             
             String serverURL = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_URL);
             String deployuri = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_URI);
             String serverInstanceName = serverURL + deployuri;
             reInitConfigProperties(serverInstanceName,
                 propAMConfig, strServerConfigXML);
             SSOToken adminSSOToken = getAdminSSOToken();
 
             if (!isDITLoaded) {
                 RegisterServices regService = new RegisterServices();
                 regService.registers(adminSSOToken);
                 processDataRequests("/WEB-INF/template/sms");
             }
 
             if (ServerConfiguration.isLegacy(adminSSOToken)) {
                 Map mapProp = ServerConfiguration.getDefaultProperties();
                 mapProp.putAll(propAMConfig);
                 appendLegacyProperties(mapProp);
                 Properties tmp = new Properties();
                 tmp.putAll(mapProp);
                 SystemProperties.initializeProperties(tmp, true, false);
 
                 writeToFile(basedir + "/" + SetupConstants.AMCONFIG_PROPERTIES,
                     mapToString(mapProp));
                 writeToFile(basedir + "/serverconfig.xml", strServerConfigXML);
                 String hostname = (String)map.get(
                     SetupConstants.CONFIG_VAR_SERVER_HOST);
                 updatePlatformServerList(serverURL + deployuri, hostname);
             } else {
                 try {
                     ServerConfiguration.createDefaults(adminSSOToken);
                     ServerConfiguration.createServerInstance(adminSSOToken, 
                         serverInstanceName,
                         ServerConfiguration.getPropertiesSet(
                             strAMConfigProperties),
                         strServerConfigXML);
                 } catch (UnknownPropertyNameException ex) {
                     // ignore, property names are valid because they are
                     // gotten from template.
                 }
 
                 ServiceConfigManager scm = new ServiceConfigManager(
                     Constants.SVC_NAME_PLATFORM, (SSOToken)AccessController.
                         doPrivileged(AdminTokenAction.getInstance()));
                 scm.addListener(ConfigurationObserver.getInstance());
             }
 
             // Embedded :get our serverid and configure embedded idRepo
             if (embedded == true) {
                 try {
                     String serverID = WebtopNaming.getAMServerID();
                     String entry = 
                       map.get(SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_HOST)+
                       ":"+
                       map.get(SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_PORT)+
                      "|"+ serverID;
                     String orgName = (String) 
                             map.get(SetupConstants.SM_CONFIG_ROOT_SUFFIX);
                     updateEmbeddedIdRepo(orgName, "embedded", entry);
                 } catch (Exception ex) {
                     Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                         "EmbeddedDS : failed to setup serverid", ex);
                     throw new ConfiguratorException(
                         "EmbeddedDS : failed to gsetup serverid;ex="+ex);
                 }
            } 
             SystemProperties.setServerInstanceName(serverInstanceName);
 
 
             handlePostPlugins(adminSSOToken);
 
             postInitialize(adminSSOToken);
             /*
              * requiring the keystore.jks file in OpenSSO workspace. The
              * createIdentitiesForWSSecurity is for the JavaEE/NetBeans 
              * integration that we had done.
              */
            createPasswordFiles(basedir, deployuri);
             createIdentitiesForWSSecurity(serverURL, deployuri);
             isConfiguredFlag = true;
             configured = true;
         } catch (FileNotFoundException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
         } catch (ConfigurationException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
         } catch (SecurityException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
         } catch (LDAPServiceException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
         } catch (IOException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
         } catch (SMSException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
         } catch (PolicyException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
         } catch (ConfiguratorException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
         } catch (SSOException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
         } catch (IdRepoException idrepoe) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", idrepoe);
             idrepoe.printStackTrace();
         }
         return configured;
     }
     
     private static void appendLegacyProperties(Map prop) {
         ResourceBundle res = ResourceBundle.getBundle(LEGACY_PROPERTIES);
         for (Enumeration i = res.getKeys(); i.hasMoreElements(); ) {
             String key = (String)i.nextElement();
             prop.put(key, (String)res.getString(key));
         }
     }
 
     private static void postInitialize(SSOToken adminSSOToken)
         throws SSOException, SMSException {
         AMAuthenticationManager.reInitializeAuthServices();
         
         AMIdentityRepository.clearCache();
         ServiceManager svcMgr = new ServiceManager(adminSSOToken);
         svcMgr.clearCache();
         LoginLogoutMapping lmp = new LoginLogoutMapping();
         lmp.initializeAuth(servletCtx);
         LoginLogoutMapping.setProductInitialized(true);
     }
     
     private static Map createBootstrapResource(boolean legacy)
         throws IOException {
         Map initMap = new HashMap();
         Map map = ServicesDefaultValues.getDefaultValues();
         String serverURL = (String)map.get(
             SetupConstants.CONFIG_VAR_SERVER_URL);
         String basedir = (String)map.get(
             SetupConstants.CONFIG_VAR_BASE_DIR);
         String deployuri = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_URI);
 
         String dataStore = (String)map.get(
             SetupConstants.CONFIG_VAR_DATA_STORE);
         if (legacy) {
             initMap.put(Bootstrap.FF_BASE_DIR,
                 basedir);
         } else if (dataStore.equals(SetupConstants.SMS_FF_DATASTORE)) {
             initMap.put(Bootstrap.PROTOCOL, Bootstrap.PROTOCOL_FILE);
             initMap.put(Bootstrap.FF_BASE_DIR,
                 basedir + deployuri + "/" + SMS_STR );
             initMap.put(Bootstrap.PWD, 
                 map.get(SetupConstants.CONFIG_VAR_ADMIN_PWD));
             initMap.put(Bootstrap.BASE_DIR, basedir);
         } else {
             initMap.put(Bootstrap.PROTOCOL, Bootstrap.PROTOCOL_LDAP);
             initMap.put(Bootstrap.DS_HOST, 
                 map.get(SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_HOST));
             initMap.put(Bootstrap.DS_PORT, 
                 map.get(SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_PORT));
             initMap.put(Bootstrap.DS_BASE_DN,
                 map.get(SetupConstants.CONFIG_VAR_ROOT_SUFFIX));
             initMap.put(Bootstrap.DS_MGR,
                 map.get(SetupConstants.CONFIG_VAR_DS_MGR_DN));
             initMap.put(Bootstrap.PWD, 
                 map.get(SetupConstants.CONFIG_VAR_ADMIN_PWD));
             initMap.put(Bootstrap.DS_PWD, 
                 map.get(SetupConstants.CONFIG_VAR_DS_MGR_PWD));
         }
         
         if (dataStore.equals(SetupConstants.SMS_EMBED_DATASTORE)) {
             initMap.put(Bootstrap.EMBEDDED_DS, basedir + "/" +
                 SetupConstants.SMS_OPENDS_DATASTORE);
         }
         
         initMap.put(Bootstrap.SERVER_INSTANCE, serverURL + deployuri);
         return initMap;
     }
     
     private static void createExtraBootstrapFile(String url)
         throws IOException {
         Map map = ServicesDefaultValues.getDefaultValues();
         String basedir = (String)map.get(
             SetupConstants.CONFIG_VAR_BASE_DIR);
         String deployuri = (String)map.get(
             SetupConstants.CONFIG_VAR_SERVER_URI);
         String fileName = basedir + "/" + BOOTSTRAP_EXTRA; 
         writeToFile(fileName, url);
 
         // Do "chmod" only if it is on UNIX/Linux platform
         if (System.getProperty("path.separator").equals(":")) {
             Runtime.getRuntime().exec("chmod 400" + fileName);
         }
     }
 
     static String getBootstrap() {
         String res = null;
         String bootstrap = getBootStrapFile();
         FileReader frdr = null;
         
         try {
             String configDir = getPresetConfigDir();
             boolean preConfigured = (configDir != null) &&
                 (configDir.length() == 0);
             if (!preConfigured) {
                 bootstrap = Bootstrap.readFile(bootstrap) + "/bootstrap";
             }
 
             File test = new File(bootstrap);
             if (test.exists()) {
                 frdr = new FileReader(bootstrap);
                 BufferedReader brdr = new BufferedReader(frdr);
                 res = brdr.readLine();
             } else {
                 res = getBootStrapFile();
                 if (preConfigured) {
                     if (res.endsWith("/bootstrap")) {
                         int idx = res.lastIndexOf("/bootstrap");
                         res = res.substring(0, idx);
                     }
                 } else {
                     res = Bootstrap.readFile(res);
                 }
             }
         } catch (FileNotFoundException e) {
             // ignore bootstrap file is missing if war is not configured.
         } catch (IOException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.getBootStrap: error", e);
             System.err.println(e.getMessage());
         } finally {
             if (frdr != null) {
                 try {
                     frdr.close();
                 } catch (IOException e) {
                     //ignore
                 }
             }
         }
         return res;
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
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.getConfigPluginClasses: error", e);
             e.printStackTrace();
         } catch (InstantiationException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.getConfigPluginClasses: error", e);
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.getConfigPluginClasses: error", e);
             e.printStackTrace();
         } catch (MissingResourceException e) {
             //ignore if there are no configurator plugins.
         }
         return plugins;
     }
 
     private static void reInitConfigProperties(
         String serverName,
         Properties prop,
         String strServerConfigXML
     ) throws FileNotFoundException, SMSException, IOException, SSOException,
         LDAPServiceException {
         SystemProperties.initializeProperties(prop, true, false);
         Crypt.reinitialize();
         initDSConfigMgr(strServerConfigXML);
         AdminUtils.initialize();
         SMSAuthModule.initialize();
         SystemProperties.initializeProperties(prop, true, true);
         DebugPropertiesObserver.getInstance().notifyChanges();
         
         List plugins = getConfigPluginClasses();
         Map map = ServicesDefaultValues.getDefaultValues();
         String basedir = (String)map.get(SetupConstants.CONFIG_VAR_BASE_DIR);
         for (Iterator i = plugins.iterator(); i.hasNext(); ) {
             ConfiguratorPlugin plugin = (ConfiguratorPlugin)i.next();
             plugin.reinitConfiguratioFile(basedir);
         }
     }
 
     public static String getPresetConfigDir() {
         String configDir = null;
         try {
             ResourceBundle rb = ResourceBundle.getBundle(
                 SetupConstants.BOOTSTRAP_PROPERTIES_FILE);
             configDir = rb.getString(SetupConstants.PRESET_CONFIG_DIR);
             
             if ((configDir != null) && (configDir.length() > 0)) {
                 String realPath = getNormalizedRealPath(servletCtx);
                 if (realPath != null) {
                     configDir = configDir.replaceAll(
                         SetupConstants.TAG_REALPATH, realPath);
                 } else {
                     throw new ConfiguratorException(
                         "cannot get configuration path");
                 }
             }
         } catch (MissingResourceException e) {
             //ignored because bootstrap properties file maybe absent.
         }
         return configDir;
     }
 
     /**
      * Returns location of the bootstrap file.
      *
      * @return Location of the bootstrap file. Returns null if the file
      *         cannot be located 
      * @throws ConfiguratorException if servlet context is null or deployment
      *         application real path cannot be determined.
      */
     static String getBootStrapFile()
         throws ConfiguratorException {
         String bootFile = null;
         if (servletCtx != null) {
             String path = getNormalizedRealPath(servletCtx);
             if (path != null) {
                 String configDir = getPresetConfigDir();
 
                 if ((configDir != null) && (configDir.length() > 0)) {
                     bootFile = configDir + "/bootstrap";
                 } else {
                     bootFile = System.getProperty("user.home") + "/" +
                         SetupConstants.CONFIG_VAR_BOOTSTRAP_BASE_DIR + "/" +
                         SetupConstants.CONFIG_VAR_BOOTSTRAP_BASE_PREFIX + path;
                 }
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
     private static Map initializeConfigProperties()
         throws SecurityException, IOException {
         Map mapFileNameToContent = new HashMap();
         List dataFiles = getTagSwapConfigFiles();
         
         Map map = ServicesDefaultValues.getDefaultValues();
         String basedir = (String)map.get(
             SetupConstants.CONFIG_VAR_BASE_DIR);
        
         try {
             String deployuri = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_URI);
             File fhm = new File(basedir + deployuri + "/" + SMS_STR);
             fhm.mkdirs();
         } catch (SecurityException e){
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.initializeConfigProperties", e);
             throw e;
         }
         
         for (Iterator i = dataFiles.iterator(); i.hasNext(); ) {
             String file = (String)i.next();
             StringBuffer sbuf = readFile(file);
             
             int idx = file.lastIndexOf("/");
             String absFile = (idx != -1) ? file.substring(idx+1) : file;
             
             if (absFile.equalsIgnoreCase(SetupConstants.AMCONFIG_PROPERTIES)) {
                 String dbOption = 
                     (String)map.get(SetupConstants.CONFIG_VAR_DATA_STORE);
                 boolean embedded = 
                     dbOption.equals(SetupConstants.SMS_EMBED_DATASTORE);
                 boolean dbSunDS = false;
                 boolean dbMsAD  = false;
                 if (embedded) {
                     dbSunDS = true;
                 } else { // Keep old behavior for now.
                     dbSunDS = dbOption.equals(SetupConstants.SMS_DS_DATASTORE);
                     dbMsAD  = dbOption.equals(SetupConstants.SMS_AD_DATASTORE);
                 }
 
                 if (dbSunDS || dbMsAD) {
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
             
             String swapped = ServicesDefaultValues.tagSwap(sbuf.toString());
             
             if (absFile.equalsIgnoreCase(SetupConstants.AMCONFIG_PROPERTIES) ||
                 absFile.equalsIgnoreCase(SystemProperties.CONFIG_FILE_NAME)
             ) {
                 mapFileNameToContent.put(absFile, swapped);
             } else {
                 writeToFile(basedir + "/" + absFile, swapped);
             }
         }
         return mapFileNameToContent;
     }
     
     private static StringBuffer readFile(String file) 
         throws IOException {
         InputStreamReader fin = null;
         StringBuffer sbuf = new StringBuffer();
         
         try {
             fin = new InputStreamReader(servletCtx.getResourceAsStream(file));
             char[] cbuf = new char[1024];
             int len;
             while ((len = fin.read(cbuf)) > 0) {
                 sbuf.append(cbuf, 0, len);
             }
         } finally {
             if (fin != null) {
                 try {
                     fin.close();
                 } catch (Exception ex) {
                     //No handling requried
                 }
             }
         }
         return sbuf;
     }
     
     static void writeToFile(String fileName, String content) 
         throws IOException {
         FileWriter fout = null;
         try {
             fout = new FileWriter(fileName);
             fout.write(content);
         } catch (IOException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.writeToFile", e);
             throw e;
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
       * Returns a unused port on a given host.
       *    @param hostname (eg localhost)
       *    @param start: starting port number to check (eg 389).
       *    @param incr : port number increments to check (eg 1000).
       *    @return available port num if found. -1 of not found.
       */
     static public int getUnusedPort(String hostname, int start, int incr)
     {
         int defaultPort = -1;
         for (int i=start;i<65500 && (defaultPort == -1);i+=incr) {
             if (canUseAsPort(hostname, i))
             {
                 defaultPort = i;
             }
         }
         return defaultPort;
     }
     
     /**
       * Checks whether the given host:port is currenly under use.
       *    @param hostname (eg localhost)
       *    @param incr : port number.
       *    @return  true if not in use, false if in use.
       */
     public static boolean canUseAsPort(String hostname, int port)
     {
         boolean canUseAsPort = false;
         ServerSocket serverSocket = null;
         try {
             InetSocketAddress socketAddress =
                 new InetSocketAddress(hostname, port);
             serverSocket = new ServerSocket();
             //if (!isWindows()) {
               //serverSocket.setReuseAddress(true);
             //}
             serverSocket.bind(socketAddress);
             canUseAsPort = true;
      
             serverSocket.close();
        
             Socket s = null;
             try {
               s = new Socket();
               s.connect(socketAddress, 1000);
               canUseAsPort = false;
        
             } catch (Throwable t) {
             }
             finally {
               if (s != null) {
                 try {
                   s.close();
                 } catch (Throwable t)
                 {
                 }
               }
             }
      
      
         } catch (IOException ex) {
           canUseAsPort = false;
         } finally {
             try {
                 if (serverSocket != null) {
                     serverSocket.close();
             }
             } catch (Exception ex) { }
         }
      
         return canUseAsPort;
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
 
         boolean embedded = 
               dataStore.equals(SetupConstants.SMS_EMBED_DATASTORE);
         boolean isDSServer = false;
         boolean isADServer = false;
         if (embedded) {
             isDSServer = true;
         } else { // Keep old behavior for now.
             isDSServer = dataStore.equals(SetupConstants.SMS_DS_DATASTORE);
             isADServer = dataStore.equals(SetupConstants.SMS_AD_DATASTORE);
         }
 
         if (embedded) {
             strFiles = rb.getString(
                            SetupConstants.OPENDS_SMS_PROPERTY_FILENAME); 
         } else if (isDSServer) {
             if (sdkSchema) {
                 strFiles = rb.getString(SetupConstants.SDK_PROPERTY_FILENAME);
             } else {
                 strFiles = rb.getString(
                     SetupConstants.DS_SMS_PROPERTY_FILENAME);
             }
         } else if (isADServer) {
             strFiles = rb.getString(SetupConstants.AD_SMS_PROPERTY_FILENAME);
         } else {
             strFiles = rb.getString(
                 SetupConstants.OPENDS_SMS_PROPERTY_FILENAME); 
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
                     "AMSetupServlet.writeSchemaFiles: " +
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
                "/WEB-INF/template/keystore/keystore.jks");
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
       * Update Embedded Idrepo instance with new embedded opends isntance.
       */
     private static void  updateEmbeddedIdRepo(String orgName, 
                          String configName, String entry
     ) throws SMSException, SSOException 
     {
         SSOToken token = (SSOToken) AccessController
                  .doPrivileged(AdminTokenAction.getInstance());
         ServiceConfigManager scm = new ServiceConfigManager(token,
                  IdConstants.REPO_SERVICE, "1.0");
         ServiceConfig sc = scm.getOrganizationConfig(orgName, null);
         ServiceConfig subConfig = sc.getSubConfig(configName);
         Map configMap = subConfig.getAttributes();
         Set vals = (Set)configMap.get("sun-idrepo-ldapv3-config-ldap-server");
         vals.add(entry);
         HashMap mp = new HashMap(2);
         mp.put("sun-idrepo-ldapv3-config-ldap-server", vals);
         subConfig.setAttributes(mp);
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
         if (!isInPlatformList(values, serverURL)) {
             String instanceName = getNextAvailableServerId(values);
             values.add(serverURL + "|" + instanceName);
             as.setDefaultValues(values);
 
             // Update Organization Aliases
             OrganizationConfigManager ocm =
                 new OrganizationConfigManager(token, "/");
         
             Map attrs = ocm.getAttributes("sunIdentityRepositoryService");
             Set origValues = (Set)attrs.get("sunOrganizationAliases");
             if (!origValues.contains(hostName)) {
                 values = new HashSet();
                 values.add(hostName);
                 ocm.addAttributeValues("sunIdentityRepositoryService",
                     "sunOrganizationAliases", values);
             }
         }
     }
 
     private static String getNextAvailableServerId(Set values) {
         int instanceNumber = 1;
         int maxNumber = 1;
 
         for (Iterator items = values.iterator(); items.hasNext();) {
             String item = (String) items.next();
             int index1 = item.indexOf('|');
 
             if (index1 != -1) {
                 int index2 = item.indexOf('|', index1 + 1);
                 item = (index2 == -1) ? item.substring(index1 + 1) :
                     item.substring(index1 + 1, index2);
 
                 try {
                     int n = Integer.parseInt(item);
                     if (n > maxNumber) {
                         maxNumber = n;
                     }
                 } catch (NumberFormatException nfe) {
                     // Ignore and continue
                 }
             }
         }
         String instanceName = Integer.toString(maxNumber + 1);
         
         if (instanceName.length() == 1) {
             instanceName = "0" + instanceName;
         }
         
         return instanceName;
     }
     
     private static boolean isInPlatformList(Set values, String hostname) {
         boolean found = false;
         for (Iterator items = values.iterator(); items.hasNext() && !found;) {
             String item = (String) items.next();
             int idx = item.indexOf('|');
             if (idx != -1) {
                 String svr = item.substring(0, idx);
                 found = svr.equals(hostname);
             }
         }
         return found;
     }
     
     private static boolean isAgentServiceLoad(SSOToken token) {
         try {
             ServiceSchemaManager ssm = new ServiceSchemaManager(
                 "AgentService", token);
             return (ssm != null);
         } catch (SSOException ex) {
             return false;
         } catch (SMSException ex) {
             return false;
         }
     }
 
     /**
      * Creates Identities for WS Security
      *
      * @param serverURL URL at which Access Manager is configured.
      */
     private static void createIdentitiesForWSSecurity(
         String serverURL,
         String deployuri
     ) throws IdRepoException, SSOException {
         SSOToken token = getAdminSSOToken();
         
         if (!isAgentServiceLoad(token)) {
             return;
         }
         AMIdentityRepository idrepo = new AMIdentityRepository(token, "/");
         //createUser(idrepo, "jsmith", "John", "Smith");
         //createUser(idrepo, "jondoe", "Jon", "Doe");
         Map config = new HashMap();
 
         // Add WSC configuration
         config.put("sunIdentityServerDeviceStatus","Active");
         config.put("SecurityMech","urn:sun:wss:security:null:UserNameToken");
         config.put("UserCredential","UserName:testuser|UserPassword:test");
         config.put("AgentType","WSCAgent");
         createAgent(idrepo, "wsc", "WSC", "", config);
 
         // Add WSP configuration
         config.remove("AgentType");
         config.put("AgentType","WSPAgent");
         createAgent(idrepo, "wsp", "WSP", "", config);
 
         /*
         // Add UsernameToken profile
         createAgent(idrepo, "UserNameToken", "WSP",
             "WS-I BSP UserName Token Profile Configuration", config);
 
         // Add SAML-HolderOfKey
         config.remove("SecurityMech");
         config.remove("UserCredential");
         config.put("SecurityMech","urn:sun:wss:security:null:SAMLToken-HK");
         createAgent(idrepo, "SAML-HolderOfKey", "WSP",
             "WS-I BSP SAML Holder Of Key Profile Configuration", config);
 
         // Add SAML-SenderVouches
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:sun:wss:security:null:SAMLToken-SV");
         createAgent(idrepo, "SAML-SenderVouches", "WSP",
             "WS-I BSP SAML Sender Vouches Token Profile Configuration", config);
 
         // Add X509Token
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:sun:wss:security:null:X509Token");
         createAgent(idrepo, "X509Token", "WSP",
             "WS-I BSP X509 Token Profile Configuration", config);
 
         // Add LibertyX509Token
         config.put("TrustAuthority","LocalDisco");
         config.put("WSPEndpoint","http://wsp.com");
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:liberty:security:2005-02:null:X509");
         createAgent(idrepo, "LibertyX509Token", "WSP",
             "Liberty X509 Token Profile Configuration", config);
 
         // Add LibertyBearerToken
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:liberty:security:2005-02:null:Bearer");
         createAgent(idrepo, "LibertyBearerToken", "WSP",
             "Liberty SAML Bearer Token Profile Configuration", config);
 
         // Add LibertySAMLToken
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:liberty:security:2005-02:null:SAML");
         createAgent(idrepo, "LibertySAMLToken", "WSP",
             "Liberty SAML Token Profile Configuration", config);
 
         // Add local discovery service
         config.clear();
         config.put("AgentType","DiscoveryAgent");
         config.put("Endpoint", serverURL + deployuri + "/Liberty/disco");
         createAgent(idrepo, "LocalDisco", "Discovery",
             "Local Liberty Discovery Service Configuration", config);*/
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
         Map config
     ) throws IdRepoException, SSOException 
     {
         Map attributes = new HashMap();
 
         Set values = new HashSet();
         values.add(name);
         attributes.put("userpassword", values);
 
         for (Iterator i = config.keySet().iterator(); i.hasNext(); ) {
             String key = (String)i.next();
             String value = (String)config.get(key);
             values = new HashSet();
             values.add(value);
             attributes.put(key, values);
         }
         
         idrepo.createIdentity(IdType.AGENTONLY, name, attributes);
     }
     
     private static void initDSConfigMgr(String str) 
         throws LDAPServiceException {
         ByteArrayInputStream bis = null;
         try {
             bis = new ByteArrayInputStream(str.getBytes());
             DSConfigMgr.initInstance(bis);
         } finally {
             if (bis != null) {
                 try {
                     bis.close();
                 } catch (IOException e) {
                     //ignore
                 }
             }
         }
     }
 
     private static String mapToString(Map map) {
         StringBuffer buff = new StringBuffer();
         for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
             String key = (String)i.next();
             buff.append(key).append("=").append((String)map.get(key))
                 .append("\n");
         }
         return buff.toString();
     }
 }
