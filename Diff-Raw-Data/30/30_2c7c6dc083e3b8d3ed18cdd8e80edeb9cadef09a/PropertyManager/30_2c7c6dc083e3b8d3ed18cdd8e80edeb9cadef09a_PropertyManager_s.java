 package Sirius.navigator.resource;
 
 /*******************************************************************************
  *
  * Copyright (c)	:	EIG (Environmental Informatics Group)
  * http://www.htw-saarland.de/eig
  * Prof. Dr. Reiner Guettler
  * Prof. Dr. Ralf Denzer
  *
  * HTWdS
  * Hochschule fuer Technik und Wirtschaft des Saarlandes
  * Goebenstr. 40
  * 66117 Saarbruecken
  * Germany
  *
  * Programmers		:	Pascal
  *
  * Project			:	WuNDA 2
  * Filename		:
  * Version			:	1.0
  * Purpose			:
  * Created			:	01.10.1999
  * History			:
  *
  *******************************************************************************/
 import javax.swing.*;
 import java.applet.*;
 import java.beans.*;
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 import org.apache.log4j.*;
 
 import Sirius.navigator.connection.ConnectionInfo;
 import Sirius.navigator.tools.BrowserControl;
 import Sirius.navigator.ui.progress.*;
 import Sirius.navigator.ui.LAFManager;
 import com.jgoodies.looks.Options;
 import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
 import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
 
 public final class PropertyManager {
 
     private final static String HEADER = "Navigator Configuration File";//NOI18N
     public final static String TRUE = "true";//NOI18N
     public final static String FALSE = "false";//NOI18N
     public final static String SORT_TOKEN_SEPARATOR = ",";//NOI18N
     public final static String SORT_NAME_TOKEN = "%name%";//NOI18N
     public final static String SORT_ID_TOKEN = "%id%";//NOI18N
     public final static int MIN_SERVER_THREADS = 3;
     public final static int MAX_SERVER_THREADS = 10;
     public final static int MIN_SEARCH_RESULTS = 20;
     public final static int MAX_SEARCH_RESULTS = 300;    //public final static String LNF_METAL = "Java Metal Look & Feel";
     //public final static String LNF_MOTIF = "Motif Look & Feel";
     //public final static String LNF_WINDOWS = "Windows Look & Feel";
     //public final static String LNF_MAC = "Apple Macintosh Look & Feel";
     //public final static String LNF_GTK = "GTK Look & Feel";
     //public final static String LNF_PLASTIC = "Plastic Look & Feel";
     // .........................................................................
     private final static Logger logger = Logger.getLogger(PropertyManager.class);
     private final static PropertyManager manager = new PropertyManager();
     //private static PropertyManager manager = null;
     private final Properties properties;
     private final ConnectionInfo connectionInfo;    // .........................................................................
     private ArrayList pluginList = null;
     private String basePath = null;
     private String pluginPath = null;
     private String searchFormPath = null;
     private String profilesPath = null;    // .........................................................................
     //private String title;
     private int width;
     private int height;
     private boolean maximizeWindow;
     private boolean advancedLayout;
     private String lookAndFeel;    //private String callserverURL;
     private String connectionClass;
     private String connectionProxyClass;
     private boolean autoLogin;
     private int maxConnections;
     private int maxSearchResults;
     private boolean sortChildren;
     private boolean sortAscending;
     private int httpInterfacePort = -1;
     /** Holds value of property connectionInfoSaveable. */
     private boolean connectionInfoSaveable;
     /** Holds value of property loadable. */
     private boolean loadable;
     /** Holds value of property saveable. */
     private boolean saveable;
     /** Holds value of property applet. */
     private boolean applet = false;
     /** Holds value of property application. */
     private boolean application = true;
     /** Holds value of property appletContext. */
     private AppletContext appletContext = null;
     /** Holds value of property sharedProgressObserver. */
     private final ProgressObserver sharedProgressObserver;
     /**
      * Holds value of property language.
      */
     private String language;
     /**
      * Holds value of property country.
      */
     private String country;
     /**
      * Holds value of property locale.
      */
     private java.util.Locale locale;
     /**
      * Holds value of property editable.
      */
     private boolean editable;
     private boolean autoClose = false;
 
     private PropertyManager() {
         this.properties = new Properties();
         this.connectionInfo = new ConnectionInfo();
         this.connectionInfo.addPropertyChangeListener(new ConnectionInfoChangeListener());
         this.sharedProgressObserver = new ProgressObserver(1000, 100);
 
         //setTitle("Wuppertaler Navigations- und Datenmanagementsystem v3.2");
         setWidth(1024);
         setHeight(768);
         setMaximizeWindow(false);
         setAdvancedLayout(false);
         setLookAndFeel(LAFManager.getManager().getDefaultLookAndFeel().getName());
 
         //this.setCallserverURL("rmi://192.168.0.12/callServer");
         setConnectionClass("Sirius.navigator.connection.RMIConnection");//NOI18N
         setConnectionProxyClass("Sirius.navigator.connection.proxy.DefaultConnectionProxyHandler");//NOI18N
         setAutoLogin(false);
         setMaxConnections(MIN_SERVER_THREADS);
         setMaxSearchResults(MIN_SEARCH_RESULTS);
         setSortChildren(false);
         setSortAscending(false);
 
         setCountry("DE");//NOI18N
         setLanguage("de");//NOI18N
 
         setLoadable(true);
         setSaveable(false);
         setConnectionInfoSaveable(false);
 
         connectionInfo.setCallserverURL("rmi://192.168.0.12/callServer");//NOI18N
         connectionInfo.setPassword("");//NOI18N
         connectionInfo.setUserDomain("");//NOI18N
         connectionInfo.setUsergroup("");//NOI18N
         connectionInfo.setUsergroupDomain("");//NOI18N
         connectionInfo.setUsername("");//NOI18N
     }
     // TITLE ===================================================================
     /*public void setTitle(String title)
     {
     if(title != null)
     {
     this.title = title;
     properties.setProperty("title",  title);
     }
     else
     {
     logger.warn("setTitle(): property 'title' is 'null'");
     }
     }
     
     public String getTitle()
     {
     return this.title;
     }*/    // SIZE ====================================================================
     public void setMaximizeWindow(String maximizeWindow) {
         if (maximizeWindow != null && (maximizeWindow.equalsIgnoreCase(TRUE) || maximizeWindow.equals("1"))) {//NOI18N
             this.setMaximizeWindow(true);
         } else if (maximizeWindow != null && (maximizeWindow.equalsIgnoreCase(FALSE) || maximizeWindow.equals("0"))) {//NOI18N
             this.setMaximizeWindow(false);
         } else {
             this.setMaximizeWindow(false);
             logger.warn("setMaximizeWindow(): invalid property 'maximizeWindow': '" + maximizeWindow + "', setting default value to '" + this.maximizeWindow + "'");//NOI18N
         }
     }
 
     /** Setter for property maximizeWindow.
      * @param maximizeWindow New value of property maximizeWindow.
      *
      */
     public void setMaximizeWindow(boolean maximizeWindow) {
         this.maximizeWindow = maximizeWindow;
         properties.setProperty("maximizeWindow", String.valueOf(maximizeWindow));//NOI18N
     }
 
     /** Getter for property maximizeWindow.
      * @return Value of property maximizeWindow.
      *
      */
     public boolean isMaximizeWindow() {
         return this.maximizeWindow;
     }
 
     public void setSize(int width, int height) {
         this.setWidth(width);
         this.setHeight(height);
     }
 
     public void setSize(String width, String height) {
         this.setWidth(width);
         this.setHeight(height);
     }
 
     public void setWidth(String width) {
         try {
             int iwidth = Integer.parseInt(width);
             this.setWidth(iwidth);
         } catch (Exception exp) {
             logger.warn("setWidth(): invalid property 'witdh': '" + exp.getMessage() + "'");//NOI18N
         }
     }
 
     public void setWidth(int width) {
         this.width = width;
         properties.setProperty("width", String.valueOf(width));//NOI18N
     }
 
     public int getWidth() {
         return this.width;
     }
 
     public void setHeight(String height) {
         try {
             int iheight = Integer.parseInt(height);
             this.setHeight(iheight);
         } catch (Exception exp) {
             logger.warn("setHeight(): invalid property 'height': '" + exp.getMessage() + "'");//NOI18N
         }
     }
 
     public void setHeight(int height) {
         this.height = height;
         properties.setProperty("height", String.valueOf(height));//NOI18N
     }
 
     public int getHeight() {
         return this.height;
     }
     // CALL_SERVER_URL ==========================================================
     /** Getter for property connectionClass.
      * @return Value of property connectionClass.
      *
      */
     public String getConnectionClass() {
         return this.connectionClass;
     }
 
     /** Setter for property connectionClass.
      * @param connectionClass New value of property connectionClass.
      *
      */
     public void setConnectionClass(String connectionClass) {
         this.connectionClass = connectionClass;
         properties.setProperty("connectionClass", this.connectionClass);//NOI18N
     }
 
     /** Getter for property connectionProxyClass.
      * @return Value of property connectionProxyClass.
      *
      */
     public String getConnectionProxyClass() {
         return this.connectionProxyClass;
     }
 
     /** Setter for property connectionProxyClass.
      * @param connectionProxyClass New value of property connectionProxyClass.
      *
      */
     public void setConnectionProxyClass(String connectionProxyClass) {
         this.connectionProxyClass = connectionProxyClass;
         properties.setProperty("connectionProxyClass", this.connectionProxyClass);//NOI18N
     }
 
     public void setAutoLogin(String autoLogin) {
         if (autoLogin != null && (autoLogin.equalsIgnoreCase(TRUE) || autoLogin.equals("1"))) {//NOI18N
             this.setAutoLogin(true);
         } else if (autoLogin != null && (autoLogin.equalsIgnoreCase(FALSE) || autoLogin.equals("0"))) {//NOI18N
             this.setAutoLogin(false);
         } else {
             this.setAutoLogin(false);
             logger.warn("setAutoLogin(): invalid property 'autoLogin': '" + autoLogin + "', setting default value to '" + this.autoLogin + "'");//NOI18N
         }
     }
 
     public void setAutoLogin(boolean autoLogin) {
         this.autoLogin = autoLogin;
         properties.setProperty("autoLogin", String.valueOf(this.autoLogin));//NOI18N
     }
 
     public boolean isAutoLogin() {
         return this.autoLogin;
     }
 
     /*public void setCallserverURL(String callserverURL)
     {
     try
     {
     this.setCallserverURL(new URL(callserverURL));
     }
     catch(Exception exp)
     {
     logger.warn("setCallserverURL(): invalid property 'callserverURL': '" + exp.getMessage() + "'");
     }
     }/*
     
     /** Setter for property callserverURL.
      * @param callserverURL New value of property callserverURL.
      *
      */
     /*public void setCallserverURL(String callserverURL)
     {
     this.callserverURL = callserverURL;
     properties.setProperty("callserverURL",  callserverURL);
     }*/
     /** Getter for property callserverURL.
      * @return Value of property callserverURL.
      *
      */
     /*public String getCallserverURL()
     {
     return this.callserverURL;
     }*/    // MAX_CONNECTIONS =========================================================
     public void setMaxConnections(String maxConnections) {
         try {
             int imaxConnections = Integer.parseInt(maxConnections);
             this.setMaxConnections(imaxConnections);
         } catch (Exception exp) {
             logger.warn("setMaxConnections(): invalid property 'maxConnections': '" + exp.getMessage() + "'");//NOI18N
         }
     }
 
     public void setMaxConnections(int maxConnections) {
         if (maxConnections < MIN_SERVER_THREADS || maxConnections > MAX_SERVER_THREADS) {
             this.maxConnections = MIN_SERVER_THREADS;
             properties.setProperty("maxConnections", String.valueOf(MIN_SERVER_THREADS));//NOI18N
             logger.warn("setMaxConnections(): invalid property 'maxConnections': '" + maxConnections + "', setting default value to '" + MIN_SERVER_THREADS + "'");//NOI18N
         } else {
             this.maxConnections = maxConnections;
             properties.setProperty("maxConnections", String.valueOf(maxConnections));//NOI18N
         }
     }
 
     public int getMaxConnections() {
         return this.maxConnections;
     }
     //MAX_SEARCH_RESULTS =======================================================
     public void setMaxSearchResults(String maxSearchResults) {
         try {
             int imaxSearchResults = Integer.parseInt(maxSearchResults);
             this.setMaxSearchResults(imaxSearchResults);
         } catch (NumberFormatException nfe) {
             logger.warn("setMaxSearchResults(): invalid property 'maxSearchResults': '" + nfe.getMessage() + "'");//NOI18N
         }
     }
 
     public void setMaxSearchResults(int maxSearchResults) {
         if (maxSearchResults < MIN_SEARCH_RESULTS || maxSearchResults > MAX_SEARCH_RESULTS) {
             this.maxSearchResults = MIN_SEARCH_RESULTS;
             properties.setProperty("maxSearchResults", String.valueOf(this.maxSearchResults));//NOI18N
             logger.warn("setMaxSearchResults(): invalid property 'maxSearchResults': '" + maxSearchResults + "', setting default value to '" + MIN_SEARCH_RESULTS + "'");//NOI18N
         } else {
             this.maxSearchResults = maxSearchResults;
             properties.setProperty("maxSearchResults", String.valueOf(maxSearchResults));//NOI18N
         }
     }
 
     public int getMaxSearchResults() {
         return this.maxSearchResults;
     }
     // AdvancedLayout ======================================================
     public void setAdvancedLayout(String advancedLayout) {
         if (advancedLayout != null && (advancedLayout.equalsIgnoreCase(TRUE) || advancedLayout.equals("1"))) {//NOI18N
             this.setAdvancedLayout(true);
         } else if (advancedLayout != null && (advancedLayout.equalsIgnoreCase(FALSE) || advancedLayout.equals("0"))) {//NOI18N
             this.setAdvancedLayout(false);
         } else {
             this.setAdvancedLayout(false);
             logger.warn("setAdvancedLayout(): invalid property 'advancedLayout': '" + advancedLayout + "', setting default value to '" + this.advancedLayout + "'");//NOI18N
         }
     }
 
     public void setAdvancedLayout(boolean advancedLayout) {
         this.advancedLayout = advancedLayout;
         properties.setProperty("advancedLayout", String.valueOf(this.advancedLayout));//NOI18N
     }
 
     public boolean isAdvancedLayout() {
         return this.advancedLayout;
     }
     //LOOK & FEEL ==============================================================
     public void setLookAndFeel(String lookAndFeelName) {
         /*if(lookAndFeel.equalsIgnoreCase(LNF_METAL) || lookAndFeel.equalsIgnoreCase(LNF_WINDOWS) || lookAndFeel.equalsIgnoreCase(LNF_MOTIF) || lookAndFeel.equalsIgnoreCase(LNF_MAC) || lookAndFeel.equalsIgnoreCase(LNF_GTK) || lookAndFeel.equalsIgnoreCase(LNF_PLASTIC))
             this.lookAndFeel = lookAndFeel;
         else if(lookAndFeel.equals("0"))
             this.lookAndFeel = LNF_METAL;
         else if(lookAndFeel.equals("1"))
             this.lookAndFeel = LNF_WINDOWS;
         else if(lookAndFeel.equals("2"))
             this.lookAndFeel = LNF_MOTIF;
         else if(lookAndFeel.equals("3"))
             this.lookAndFeel = LNF_MAC;
         else if(lookAndFeel.equals("4"))
             this.lookAndFeel = LNF_GTK;
         else if(lookAndFeel.equals("5"))
             this.lookAndFeel = LNF_PLASTIC;
          */
         
         if(LAFManager.getManager().isInstalledLookAndFeel(lookAndFeelName))
         {
             this.lookAndFeel = lookAndFeelName;
         }
         else
         {
             //this.lookAndFeel = LNF_METAL;
             this.lookAndFeel = LAFManager.getManager().getDefaultLookAndFeel().getName();
             logger.warn("setLookAndFeel(): invalid property 'lookAndFeel': '" + lookAndFeelName + "', setting default value to '" + this.lookAndFeel + "'");//NOI18N
         }
 //        try {
 //            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
 ////            Plastic3DLookAndFeel.setCurrentTheme(new com.jgoodies.looks.plastic.theme.DesertBluer());
 ////            Options.setPopupDropShadowEnabled(true);
 //
 //
 ////        Plastic3DLookAndFeel.set
 //        } catch (Exception e) {
 //        }
         properties.setProperty("lookAndFeel", this.lookAndFeel);//NOI18N
         //properties.setProperty("lookAndFeel", "HARDWIRED");
     }
 
     public String getLookAndFeel() {
         return this.lookAndFeel;
     }
     // sortChildren =============================================================
     public void setSortChildren(String sortChildren) {
         if (sortChildren != null && (sortChildren.equalsIgnoreCase(TRUE) || sortChildren.equals("1"))) {//NOI18N
             this.setSortChildren(true);
         } else if (sortChildren != null && (sortChildren.equalsIgnoreCase(FALSE) || sortChildren.equals("0"))) {//NOI18N
             this.setSortChildren(false);
         } else {
             this.setSortChildren(false);
             logger.warn("setSortChildren(): invalid property 'sortChildren': '" + sortChildren + "', setting default value to '" + this.sortChildren + "'");//NOI18N
         }
     }
 
     public void setSortChildren(boolean sortChildren) {
         this.sortChildren = sortChildren;
         properties.setProperty("sortChildren", String.valueOf(this.sortChildren));//NOI18N
     }
 
     public boolean isSortChildren() {
         return this.sortChildren;
     }
     // sortAscending ============================================================
     public void setSortAscending(String sortAscending) {
         if (sortAscending != null && (sortAscending.equalsIgnoreCase(TRUE) || sortAscending.equals("1"))) {//NOI18N
             this.setSortAscending(true);
         } else if (sortAscending != null && (sortAscending.equalsIgnoreCase(FALSE) || sortAscending.equals("0"))) {//NOI18N
             this.setSortAscending(false);
         } else {
             this.setSortAscending(false);
             logger.warn("setSortAscending(): invalid property 'sortAscending': '" + sortAscending + "', setting default value to '" + this.sortAscending + "'");//NOI18N
         }
     }
 
     public void setSortAscending(boolean sortAscending) {
         this.sortAscending = sortAscending;
         properties.setProperty("sortAscending", String.valueOf(this.sortAscending));//NOI18N
 
     }
 
     public boolean isSortAscending() {
         return this.sortAscending;
     }
 
     public void setConnectionInfoSaveable(String connectionInfoSaveable) {
         if (connectionInfoSaveable != null && (connectionInfoSaveable.equalsIgnoreCase(TRUE) || connectionInfoSaveable.equals("1"))) {//NOI18N
             this.setConnectionInfoSaveable(true);
         } else if (connectionInfoSaveable != null && (connectionInfoSaveable.equalsIgnoreCase(FALSE) || connectionInfoSaveable.equals("0"))) {//NOI18N
             this.setConnectionInfoSaveable(false);
         } else {
             this.setConnectionInfoSaveable(false);
             logger.warn("connectionInfoSaveable(): invalid property 'connectionInfoSaveable': '" + connectionInfoSaveable + "', setting default value to '" + this.connectionInfoSaveable + "'");//NOI18N
         }
     }
 
     /** Setter for property connectionInfoSaveable.
      * @param connectionInfoSaveable New value of property connectionInfoSaveable.
      *
      */
     public void setConnectionInfoSaveable(boolean connectionInfoSaveable) {
         this.connectionInfoSaveable = this.isSaveable() & connectionInfoSaveable;
         properties.setProperty("connectionInfoSaveable", String.valueOf(this.connectionInfoSaveable));//NOI18N
     }
 
     /** Getter for property connectionInfoSaveable.
      * @return Value of property connectionInfoSaveable.
      *
      */
     public boolean isConnectionInfoSaveable() {
         return this.connectionInfoSaveable;
     }
 
     public void setLoadable(String loadable) {
         if (loadable != null && (loadable.equalsIgnoreCase(TRUE) || loadable.equals("1"))) {//NOI18N
             this.setLoadable(true);
         } else if (loadable != null && (loadable.equalsIgnoreCase(FALSE) || loadable.equals("0"))) {//NOI18N
             this.setLoadable(false);
         } else {
             this.setLoadable(false);
             logger.warn("loadable(): invalid property 'loadable': '" + loadable + "', setting default value to '" + this.loadable + "'");//NOI18N
         }
     }
 
     /** Setter for property loadable.
      * @param loadable New value of property loadable.
      *
      */
     public void setLoadable(boolean loadable) {
         this.loadable = loadable;
         properties.setProperty("loadable", String.valueOf(this.loadable));//NOI18N
     }
 
     /** Getter for property loadable.
      * @return Value of property loadable.
      *
      */
     public boolean isLoadable() {
         return this.loadable;
     }
 
     public void setSaveable(String saveable) {
         if (saveable != null && (saveable.equalsIgnoreCase(TRUE) || saveable.equals("1"))) {//NOI18N
             this.setSaveable(true);
         } else if (saveable != null && (saveable.equalsIgnoreCase(FALSE) || saveable.equals("0"))) {//NOI18N
             this.setSaveable(false);
         } else {
             this.setSaveable(false);
             logger.warn("saveable(): invalid property 'saveable': '" + saveable + "', setting default value to '" + this.saveable + "'");//NOI18N
         }
     }
 
     /** Setter for property saveable.
      * @param saveable New value of property saveable.
      *
      */
     public void setSaveable(boolean saveable) {
         this.saveable = saveable;
         properties.setProperty("saveable", String.valueOf(this.saveable));//NOI18N
     }
 
     /** Getter for property saveable.
      * @return Value of property saveable.
      *
      */
     public boolean isSaveable() {
         return this.saveable;
     }
     // .........................................................................
     public ConnectionInfo getConnectionInfo() {
         return this.connectionInfo;
     }
     // .........................................................................
     private void load() {
         Enumeration keys = properties.keys();
         while (keys.hasMoreElements()) {
             String key = (String) keys.nextElement();
             this.setProperty(key, properties.getProperty(key));
         }
     }
 
     private synchronized void setProperty(String property, String value) {
         if (logger.isDebugEnabled()) {
             logger.debug("setting property '" + property + "' to '" + value + "'");//NOI18N
         /*if(property.equalsIgnoreCase("title"))
         {
         this.setTitle(value);
         }
         else*/
         }
         if (property.equalsIgnoreCase("width")) {//NOI18N
             this.setWidth(value);
         } else if (property.equalsIgnoreCase("height")) {//NOI18N
             this.setHeight(value);
         } else if (property.equalsIgnoreCase("maximizeWindow")) {//NOI18N
             this.setMaximizeWindow(value);
         } else if (property.equalsIgnoreCase("advancedLayout")) {//NOI18N
             this.setAdvancedLayout(value);
         } else if (property.equalsIgnoreCase("lookAndFeel")) {//NOI18N
             this.setLookAndFeel(value);
         } else if (property.equalsIgnoreCase("autoLogin")) {//NOI18N
             this.setAutoLogin(value);
         } else if (property.equalsIgnoreCase("connectionClass")) {//NOI18N
             this.setConnectionClass(value);
         } else if (property.equalsIgnoreCase("connectionProxyClass")) {//NOI18N
             this.setConnectionProxyClass(value);
         } else if (property.equalsIgnoreCase("maxConnections")) {//NOI18N
             this.setMaxConnections(value);
         } else if (property.equalsIgnoreCase("maxSearchResults")) {//NOI18N
             this.setMaxSearchResults(value);
         } else if (property.equalsIgnoreCase("sortChildren")) {//NOI18N
             this.setSortChildren(value);
         } else if (property.equalsIgnoreCase("sortAscending")) {//NOI18N
             this.setSortAscending(value);
         } else if (property.equalsIgnoreCase("saveable")) {//NOI18N
             this.setSaveable(value);
         } else if (property.equalsIgnoreCase("loadable")) {//NOI18N
             this.setLoadable(value);
         } else if (property.equalsIgnoreCase("language")) {//NOI18N
             this.setLanguage(value);
         } else if (property.equalsIgnoreCase("country")) {//NOI18N
             this.setCountry(value);
         } else if (property.equalsIgnoreCase("connectionInfoSaveable")) {//NOI18N
             this.setConnectionInfoSaveable(value);
         } else if (property.equalsIgnoreCase("callserverURL")) {//NOI18N
             this.connectionInfo.setCallserverURL(value);
         } else if (property.equalsIgnoreCase("password")) {//NOI18N
             this.connectionInfo.setPassword(value);
         } else if (property.equalsIgnoreCase("userDomain")) {//NOI18N
             this.connectionInfo.setUserDomain(value);
         } else if (property.equalsIgnoreCase("usergroup")) {//NOI18N
             this.connectionInfo.setUsergroup(value);
         } else if (property.equalsIgnoreCase("usergroupDomain")) {//NOI18N
             this.connectionInfo.setUsergroupDomain(value);
         } else if (property.equalsIgnoreCase("username")) {//NOI18N
             this.connectionInfo.setUsername(value);
         } else {
             //logger.warn("setProperty(): unknown property '" + property + "' = '" + value + "'");
         }
     }
 
     public void load(InputStream inStream) {
         if (this.isLoadable()) {
             try {
                 this.properties.load(inStream);
                 this.load();
             } catch (Exception exp) {
                 logger.fatal("could not load properties: " + exp.getMessage(), exp);//NOI18N
             }
         } else {
             logger.error("could not load properties: properties not loadable");//NOI18N
         }
     }
 
     public void configure() {
         this.load(this.getClass().getResourceAsStream("cfg/navigator.cfg"));//NOI18N
     }
 
     public void configure(String cfgFile, String basePath, String pluginPath, String searchFormPath, String profilesPath) throws Exception {
         this.applet = false;
         this.application = true;
 
         if (basePath != null) {
             logger.info("setting base path to '" + basePath + "'");//NOI18N
             this.basePath = basePath;
         } else {
             this.getBasePath();
         }
 
         if (pluginPath != null) {
             logger.info("setting base plugin to '" + pluginPath + "'");//NOI18N
             this.pluginPath = pluginPath;
         } else {
             this.getPluginPath();
         }
 
         if (searchFormPath != null) {
             logger.info("setting search form path to '" + searchFormPath + "'");//NOI18N
             this.searchFormPath = searchFormPath;
         } else {
             this.getSearchFormPath();
         }
 
         if (profilesPath != null) {
             logger.info("setting profiles path to '" + profilesPath + "'");//NOI18N
             this.profilesPath = profilesPath;
         } else {
             this.getProfilesPath();
         }
 
         if (cfgFile != null) {
             //try
             //{
             if (cfgFile.indexOf("http://") == 0 || cfgFile.indexOf("https://") == 0 || cfgFile.indexOf("file://") == 0) {//NOI18N
                 URL url = new URL(cfgFile);
                 this.load(url.openStream());
 
                 logger.info("config file loaded from url (assuming webstart)");//NOI18N
                 this.applet = true;
             } else {
                 File file = new File(cfgFile);
                 this.load(new BufferedInputStream(new FileInputStream(cfgFile)));
             }
         /*}
         catch(Exception exp)
         {
         logger.fatal("loading of config file '" + cfgFile + "' failed", exp);
         throw exp;
         }*/
         } else {
             throw new Exception("loading of config file '" + cfgFile + "' failed");//NOI18N
         //this.configure();
         }
 
         //\u00C4nderungen wg. Webstart HELL
         try {
             String parameter = this.properties.getProperty("plugins");//NOI18N
             setHttpInterfacePort(new Integer(properties.getProperty("httpInterfacePort", "9099")));//NOI18N
             setAutoClose(new Boolean(properties.getProperty("closeWithoutAsking", "false")));//NOI18N
 
             if (parameter != null && parameter.length() > 0) {
                 pluginList = new ArrayList();
                 StringTokenizer tokenizer = new StringTokenizer(parameter, ";");//NOI18N
                 while (tokenizer.hasMoreTokens()) {
                     String plugin = tokenizer.nextToken().trim() + "/";//NOI18N
                     logger.info("adding plugin from config file: '" + plugin + "'");//NOI18N
                     pluginList.add(pluginPath + "/" + plugin);//NOI18N
                 }
             }
         } catch (Exception except) {
             logger.fatal(except, except);
         }
 
         this.isPluginListAvailable();
     }
 
     public void configure(JApplet applet) {
         logger.debug("configure property manager (applet)");//NOI18N
         this.pluginList = new ArrayList();
         this.applet = true;
         this.application = false;
         this.appletContext = applet.getAppletContext();
 
         this.basePath = applet.getCodeBase().toString(); // + "/";
         logger.info("setting base path to '" + this.basePath + "'");//NOI18N
 
         this.pluginPath = this.basePath + "plugins/";//NOI18N
         logger.info("setting plugins path to '" + this.pluginPath + "'");//NOI18N
 
         this.searchFormPath = this.basePath + "search/";//NOI18N
         logger.info("setting search forms path to '" + this.searchFormPath + "'");//NOI18N
 
         this.readAppletParameters(applet);
     }
 
     private void readAppletParameters(JApplet applet) {
         // configfile
         String parameter = applet.getParameter("configfile");//NOI18N
         if (parameter != null && parameter.length() > 0) {
             if (logger.isDebugEnabled()) {
                 logger.debug("loading configfile from remote url '" + this.getBasePath() + parameter + "'");//NOI18N
             }
             try {
                 URL url = new URL(this.getBasePath() + parameter);
                 this.load(new BufferedInputStream(url.openStream()));
             } catch (Exception exp) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("could not load configfile, trying to load file from local filesystem\n" + exp.getMessage());//NOI18N
                 }
                 try {
                     File file = new File(parameter);
                     this.load(new BufferedInputStream(new FileInputStream(file)));
                 } catch (Exception ioexp) {
                     logger.error("could not load configfile, using default configuration\n" + ioexp.getMessage());//NOI18N
                     this.configure();
                 }
             }
         }
 
         parameter = applet.getParameter("language");//NOI18N
         if (parameter != null && parameter.length() > 0) {
             ResourceManager.getManager().setLocale(new Locale(parameter));
         }
 
         parameter = applet.getParameter("plugins");//NOI18N
         if (parameter != null && parameter.length() > 0) {
             StringTokenizer tokenizer = new StringTokenizer(parameter, ";");//NOI18N
             while (tokenizer.hasMoreTokens()) {
                 String plugin = this.pluginPath + tokenizer.nextToken().trim() + "/";//NOI18N
                 logger.info("adding plugin '" + plugin + "'");//NOI18N
                 pluginList.add(plugin);
             }
         }
     }
 
     public void save(OutputStream outStream) {
         if (this.isSaveable()) {
             try {
                 this.properties.store(outStream, HEADER);
             } catch (Exception exp) {
                 logger.fatal("could not save properties: " + exp.getMessage(), exp);//NOI18N
             }
         } else {
             logger.error("could not save properties: properties not saveable");//NOI18N
         }
     }
 
     public void print() {
         properties.list(System.out);
     }
 
     public final static PropertyManager getManager() {
         /*if(manager == null)
         {
         manager = new PropertyManager();
         }*/
 
         return manager;
     }
 
     /** Getter for property applet.
      * @return Value of property applet.
      *
      */
     public boolean isApplet() {
         return this.applet;
     }
 
     /** Getter for property application.
      * @return Value of property application.
      *
      */
     public boolean isApplication() {
         return this.application;
     }
 
     public String getBasePath() {
         if (this.basePath == null) {
             this.basePath = System.getProperty("user.home") + System.getProperty("file.separator") + ".navigator" + System.getProperty("file.separator");//NOI18N
             if (logger.isInfoEnabled()) {
                 logger.info("no base path set, setting default base path to '" + this.basePath + "'");//NOI18N
             }
             File file = new File(this.basePath);
             if (!file.exists()) {
                 logger.warn("base path does not exist, creating base path");//NOI18N
                 if (!file.mkdirs()) {
                     logger.error("could not create base path");//NOI18N
                 }
             }
         }
 
         return this.basePath;
     }
 
     public String getPluginPath() {
         if (this.pluginPath == null) {
             if (this.getBasePath().startsWith("http") || this.getBasePath().startsWith("file")) {//NOI18N
                 this.pluginPath = this.getBasePath() + "plugins/";//NOI18N
             } else {
                 this.pluginPath = this.getBasePath() + "plugins" + System.getProperty("file.separator");//NOI18N
             }
 
             logger.info("no plugin path set, setting default plugin path to '" + this.pluginPath + "'");//NOI18N
         }
 
         return this.pluginPath;
     }
 
     public String getSearchFormPath() {
         if (this.searchFormPath == null) {
             if (this.getBasePath().startsWith("http")) {//NOI18N
                 this.searchFormPath = this.basePath + "search/";//NOI18N
             } else {
                 this.searchFormPath = this.basePath + "search" + System.getProperty("file.separator");//NOI18N
             }
             logger.info("no search form path set, setting default search form path to '" + this.searchFormPath + "'");//NOI18N
         }
 
         return this.searchFormPath;
     }
 
     public String getProfilesPath() {
         if (this.profilesPath == null || this.profilesPath.equals("AUTO")) {//NOI18N
             if (this.getBasePath().startsWith("http")) {//NOI18N
                 logger.info("no profiles path set and base path == URL, settting default profiles path to user.home");//NOI18N
                 this.profilesPath = new StringBuffer().append(System.getProperty("user.home")).append(System.getProperty("file.separator")).append(".navigator").append(System.getProperty("file.separator")).append("profiles").append(System.getProperty("file.separator")).toString();//NOI18N
             } else {
                 this.profilesPath = this.basePath + "profiles" + System.getProperty("file.separator");//NOI18N
             }
 
             logger.info("no profiles form path set, setting default search form path to '" + this.profilesPath + "'");//NOI18N
 
             File file = new File(this.profilesPath);
             if (!file.exists()) {
                 logger.warn("profiles path does not exist, creating base path");//NOI18N
                 if (!file.mkdirs()) {
                     logger.error("could not create profiles path");//NOI18N
                 }
             }
         }
 
         return this.profilesPath;
     }
 
     public boolean isPluginListAvailable() {
         if (this.pluginList == null) {
             this.pluginList = new ArrayList();
             File file = new File(this.pluginPath);
             if (file.exists() && file.isDirectory()) {
                 File[] plugins = file.listFiles();
                 if (plugins != null && plugins.length > 0) {
                     for (int i = 0; i < plugins.length; i++) {
                         if (plugins[i].isDirectory()) {
                             if (!plugins[i].getName().equalsIgnoreCase("CVS")) {//NOI18N
                                 String plugin = plugins[i].getPath() + System.getProperty("file.separator");//NOI18N
                                 logger.info("adding plugin '" + plugin + "'");//NOI18N
                                 pluginList.add(plugin);
                             } else {
                                 if (logger.isDebugEnabled()) {
                                     logger.warn("plugin directory with name 'CVS' found. ignoring plugin!");//NOI18N
                                 }
                             }
                         }
                     }
                 }
             } else {
                 logger.warn("'" + this.pluginPath + "' does not exist or is no valid plugin directory");//NOI18N
             }
         }
 
         return this.pluginList.size() > 0 ? true : false;
     }
 
     public Iterator getPluginList() {
         if (this.isPluginListAvailable()) {
             return this.pluginList.iterator();
         } else {
             logger.warn("sorry, no plugins could be found");//NOI18N
             return null;
         }
     }
 
     public Properties getProperties() {
         return this.properties;
     }
 
     /** Getter for property appletContext.
      * @return Value of property appletContext.
      *
      */
     public AppletContext getAppletContext() {
         if (this.appletContext == null) {
             return BrowserControl.getControl();
         } else {
             return this.appletContext;
         }
     }
 
     /** Getter for property sharedProgressObserver.
      * @return Value of property sharedProgressObserver.
      *
      */
     public synchronized ProgressObserver getSharedProgressObserver() {
         return this.sharedProgressObserver;
     }
 
     /**
      * Getter for property language.
      * @return Value of property language.
      */
     public String getLanguage() {
         return this.language;
     }
 
     /**
      * Setter for property language.
      * @param language New value of property language.
      */
     public void setLanguage(String language) {
         if (language.trim().length() == 2) {
             this.language = language.toLowerCase();
         } else {
             logger.warn("malformed language code '" + language + "', setting to default (de)");//NOI18N
             this.language = "de";//NOI18N
         }
     }
 
     /**
      * Getter for property country.
      * @return Value of property country.
      */
     public String getCountry() {
         return this.country;
     }
 
     /**
      * Setter for property country.
      * @param country New value of property country.
      */
     public void setCountry(String country) {
         if (country.length() == 2) {
             this.country = country.toUpperCase();
         } else {
             logger.warn("malformed country code '" + country + "', setting to default (de)");//NOI18N
            this.language = "de";//NOI18N
         }
     }
 
     /**
      * Getter for property locale.
      * @return Value of property locale.
      */
     public java.util.Locale getLocale() {
         return new Locale(this.getLanguage(), this.getCountry());
     }
 
     /**
      * Setter for property locale.
      * @param locale New value of property locale.
      */
     public void setLocale(java.util.Locale locale) {
         this.setLanguage(locale.getLanguage());
         this.setCountry(locale.getCountry());
     }
 
     /**
      * Getter for property editable.
      * @return Value of property editable.
      */
     public boolean isEditable() {
         return this.editable;
     }
 
     /**
      * Setter for property editable.
      * @param editable New value of property editable.
      */
     public void setEditable(boolean editable) {
         this.editable = editable;
     }
     // .........................................................................
     private class ConnectionInfoChangeListener implements PropertyChangeListener {
 
         /** This method gets called when a bound property is changed.
          * @param evt A PropertyChangeEvent object describing the event source
          *   	and the property that has changed.
          *
          */
         public void propertyChange(PropertyChangeEvent evt) {
             //if(logger.isDebugEnabled())logger.debug("setting property '" + evt.getPropertyName() + "' to '" + evt.getNewValue() + "'");
             if (isConnectionInfoSaveable()) {
                 properties.setProperty(evt.getPropertyName(), evt.getNewValue().toString());
             }
         }
     }
     // TEST --------------------------------------------------------------------
     /*public static void main(String args[])
     {
     BasicConfigurator.configure();
     PropertyManager.getManager().logger.setLevel(Level.DEBUG);
     PropertyManager.getManager().print();
     
     //PropertyManager.getManager().setAdvancedLayout("n\u00F6");
     //PropertyManager.getManager().setTitle("TERROR");
     //PropertyManager.getManager().setProperty("maximizeWindow",  "true");
     //PropertyManager.getManager().setProperty("TERROR",  "TERROR");
     
     try
     {
     File file = new File(System.getProperty("user.home") + "\\navigator.cfg");
     BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
     PropertyManager.getManager().store(out);
     out.close();
     }
     catch(Exception exp)
     {
     exp.printStackTrace();
     }
     
     try
     {
     File file = new File(System.getProperty("user.home") + "\\navigator.cfg");
     BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
     PropertyManager.getManager().load(in);
     in.close();
     }
     catch(Exception exp)
     {
     exp.printStackTrace();
     }
     
     PropertyManager.getManager().print();
     }*/
     /*public static void main(String args[])
     {
     BasicConfigurator.configure();
     PropertyManager.getManager().logger.setLevel(Level.DEBUG);
     
     //Properties systemProperties = System.getProperties();
     //systemProperties.list(System.out);
     
     System.out.println("basePath: " + PropertyManager.getManager().getBasePath());
     System.out.println("pluginPath: " + PropertyManager.getManager().getPluginPath());
     
     if(PropertyManager.getManager().isPluginListAvailable())
     {
     System.out.println("Plugins: ");
     Iterator iterator = PropertyManager.getManager().getPluginList();
     while(iterator.hasNext())
     {
     System.out.println(iterator.next());
     }
     }
     }*/
     /**
      * Getter for property webstart.
      * @return Value of property webstart.
      */
     public boolean isWebstart() {
 
         return this.isApplet() & this.isApplication();
     }
 
     public int getHttpInterfacePort() {
         return httpInterfacePort;
     }
 
     public void setHttpInterfacePort(int httpInterfacePort) {
         this.httpInterfacePort = httpInterfacePort;
     }
 
     public boolean isAutoClose() {
         return autoClose;
     }
 
     public void setAutoClose(boolean autoClose) {
         this.autoClose = autoClose;
     }
 }
 
