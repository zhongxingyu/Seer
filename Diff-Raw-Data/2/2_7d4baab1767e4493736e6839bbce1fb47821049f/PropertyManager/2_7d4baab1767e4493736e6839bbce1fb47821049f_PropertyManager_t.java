 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.resource;
 
 /**
  * *****************************************************************************
  *
  * Copyright (c) : EIG (Environmental Informatics Group) http://www.htw-saarland.de/eig Prof. Dr. Reiner Guettler Prof.
  * Dr. Ralf Denzer
  *
  * HTWdS Hochschule fuer Technik und Wirtschaft des Saarlandes Goebenstr. 40 66117 Saarbruecken Germany
  *
  * Programmers : Pascal
  *
  * Project : WuNDA 2 Filename : Version : 1.0 Purpose : Created : 01.10.1999 History :
  *
  ******************************************************************************
  */
 import Sirius.navigator.connection.ConnectionInfo;
 import Sirius.navigator.ui.LAFManager;
 import Sirius.navigator.ui.progress.*;
 
 import org.apache.log4j.*;
 
 import java.applet.*;
 
 import java.beans.*;
 
 import java.io.*;
 
 import java.net.*;
 
 import java.util.*;
 
 import javax.swing.*;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public final class PropertyManager {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger logger = Logger.getLogger(PropertyManager.class);
     private static final PropertyManager manager = new PropertyManager();
     private static final String HEADER = "Navigator Configuration File";
     public static final String TRUE = "true";
     public static final String FALSE = "false";
     public static final String SORT_TOKEN_SEPARATOR = ",";
     public static final String SORT_NAME_TOKEN = "%name%";
     public static final String SORT_ID_TOKEN = "%id%";
     public static final int MIN_SERVER_THREADS = 3;
     public static final int MAX_SERVER_THREADS = 10;
     public static final String FX_HTML_RENDERER = "fxWebView";
     public static final String CALPA_HTML_RENDERER = "calpa";
     public static final String FLYING_SAUCER_HTML_RENDERER = "flyingSaucer";
 
     //~ Instance fields --------------------------------------------------------
 
     private final Properties properties;
     private final ConnectionInfo connectionInfo;
     private ArrayList pluginList = null;
     private String basePath = null;
     private String pluginPath = null;
     private String searchFormPath = null;
     private String profilesPath = null;
     private int width;
     private int height;
     private boolean maximizeWindow;
     private boolean advancedLayout;
     private String lookAndFeel;
     private String connectionClass;
     private String connectionProxyClass;
     private boolean autoLogin;
     private int maxConnections;
     private boolean sortChildren;
     private boolean sortAscending;
     private int httpInterfacePort = -1;
     private boolean connectionInfoSaveable;
     private boolean loadable;
     private boolean saveable;
     private boolean applet = false;
     private boolean application = true;
     private AppletContext appletContext = null;
     private final ProgressObserver sharedProgressObserver;
     private boolean editable;
     private boolean autoClose = false;
     /**
      * DOCUMENT ME!
      *
      * @deprecated  use {@link descriptionPaneHtmlRenderer} instead
      */
     @Deprecated
     private boolean useFlyingSaucer = false;
     /**
      * DOCUMENT ME!
      *
      * @deprecated  use {@link descriptionPaneHtmlRenderer} instead
      */
     @Deprecated
     private boolean useWebView = false;
     private String descriptionPaneHtmlRenderer = null;
     private boolean enableSearchDialog = false;
     private boolean usePainterCoolPanel = true;
     private transient String proxyURL;
     private transient String proxyUsername;
     private transient String proxyPassword;
     private transient String proxyDomain;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new PropertyManager object.
      */
     private PropertyManager() {
         this.properties = new Properties();
         this.connectionInfo = new ConnectionInfo();
         this.connectionInfo.addPropertyChangeListener(new ConnectionInfoChangeListener());
         this.sharedProgressObserver = new ProgressObserver(1000, 100);
 
         setWidth(1024);
         setHeight(768);
         setMaximizeWindow(false);
         setAdvancedLayout(false);
         setLookAndFeel(LAFManager.getManager().getDefaultLookAndFeel().getName());
 
         setConnectionClass("Sirius.navigator.connection.RMIConnection");                            // NOI18N
         setConnectionProxyClass("Sirius.navigator.connection.proxy.DefaultConnectionProxyHandler"); // NOI18N
         setAutoLogin(false);
         setMaxConnections(MIN_SERVER_THREADS);
         setSortChildren(false);
         setSortAscending(false);
 
         setLoadable(true);
         setSaveable(false);
         setConnectionInfoSaveable(false);
 
         setUseFlyingSaucer(false);
         setUseWebView(false);
         setDescriptionPaneHtmlRenderer(PropertyManager.CALPA_HTML_RENDERER);
         setEnableSearchDialog(false);
 
         connectionInfo.setCallserverURL("rmi://192.168.0.12/callServer"); // NOI18N
         connectionInfo.setPassword("");                                   // NOI18N
         connectionInfo.setUserDomain("");                                 // NOI18N
         connectionInfo.setUsergroup("");                                  // NOI18N
         connectionInfo.setUsergroupDomain("");                            // NOI18N
         connectionInfo.setUsername("");                                   // NOI18N
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  maximizeWindow  DOCUMENT ME!
      */
     public void setMaximizeWindow(final String maximizeWindow) {
         if ((maximizeWindow != null) && (maximizeWindow.equalsIgnoreCase(TRUE) || maximizeWindow.equals("1"))) {
             this.setMaximizeWindow(true);
         } else if ((maximizeWindow != null) && (maximizeWindow.equalsIgnoreCase(FALSE) || maximizeWindow.equals("0"))) {
             this.setMaximizeWindow(false);
         } else {
             this.setMaximizeWindow(false);
             logger.warn("setMaximizeWindow(): invalid property 'maximizeWindow': '" + maximizeWindow
                         + "', setting default value to '" + this.maximizeWindow + "'");
         }
     }
 
     /**
      * Setter for property maximizeWindow.
      *
      * @param  maximizeWindow  New value of property maximizeWindow.
      */
     public void setMaximizeWindow(final boolean maximizeWindow) {
         this.maximizeWindow = maximizeWindow;
         properties.setProperty("maximizeWindow", String.valueOf(maximizeWindow)); // NOI18N
     }
 
     /**
      * Getter for property maximizeWindow.
      *
      * @return  Value of property maximizeWindow.
      */
     public boolean isMaximizeWindow() {
         return this.maximizeWindow;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  width   DOCUMENT ME!
      * @param  height  DOCUMENT ME!
      */
     public void setSize(final int width, final int height) {
         this.setWidth(width);
         this.setHeight(height);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  width   DOCUMENT ME!
      * @param  height  DOCUMENT ME!
      */
     public void setSize(final String width, final String height) {
         this.setWidth(width);
         this.setHeight(height);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  width  DOCUMENT ME!
      */
     public void setWidth(final String width) {
         try {
             final int iwidth = Integer.parseInt(width);
             this.setWidth(iwidth);
         } catch (Exception exp) {
             logger.warn("setWidth(): invalid property 'witdh': '" + exp.getMessage() + "'"); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  width  DOCUMENT ME!
      */
     public void setWidth(final int width) {
         this.width = width;
         properties.setProperty("width", String.valueOf(width)); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getWidth() {
         return this.width;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  height  DOCUMENT ME!
      */
     public void setHeight(final String height) {
         try {
             final int iheight = Integer.parseInt(height);
             this.setHeight(iheight);
         } catch (Exception exp) {
             logger.warn("setHeight(): invalid property 'height': '" + exp.getMessage() + "'"); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  height  DOCUMENT ME!
      */
     public void setHeight(final int height) {
         this.height = height;
         properties.setProperty("height", String.valueOf(height)); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getHeight() {
         return this.height;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getProxyURL() {
         return proxyURL;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getProxyDomain() {
         return proxyDomain;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getProxyPassword() {
         return proxyPassword;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getProxyUsername() {
         return proxyUsername;
     }
 
     /**
      * Getter for property connectionClass.
      *
      * @return  Value of property connectionClass.
      */
     public String getConnectionClass() {
         return this.connectionClass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  proxyDomain  DOCUMENT ME!
      */
     public void setProxyDomain(final String proxyDomain) {
         this.proxyDomain = proxyDomain;
         properties.setProperty("navigator.proxy.domain", proxyDomain); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  proxyPassword  DOCUMENT ME!
      */
     public void setProxyPassword(final String proxyPassword) {
         this.proxyPassword = proxyPassword;
         properties.setProperty("navigator.proxy.password", proxyPassword); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  proxyURL  DOCUMENT ME!
      */
     public void setProxyURL(final String proxyURL) {
         this.proxyURL = proxyURL;
         properties.setProperty("navigator.proxy.url", proxyURL); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  proxyUsername  DOCUMENT ME!
      */
     public void setProxyUsername(final String proxyUsername) {
         this.proxyUsername = proxyUsername;
         properties.setProperty("navigator.proxy.username", proxyUsername); // NOI18N
     }
 
     /**
      * Setter for property connectionClass.
      *
      * @param  connectionClass  New value of property connectionClass.
      */
     public void setConnectionClass(final String connectionClass) {
         this.connectionClass = connectionClass;
         properties.setProperty("connectionClass", this.connectionClass); // NOI18N
     }
 
     /**
      * Getter for property connectionProxyClass.
      *
      * @return  Value of property connectionProxyClass.
      */
     public String getConnectionProxyClass() {
         return this.connectionProxyClass;
     }
 
     /**
      * Setter for property connectionProxyClass.
      *
      * @param  connectionProxyClass  New value of property connectionProxyClass.
      */
     public void setConnectionProxyClass(final String connectionProxyClass) {
         this.connectionProxyClass = connectionProxyClass;
         properties.setProperty("connectionProxyClass", this.connectionProxyClass); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  autoLogin  DOCUMENT ME!
      */
     public void setAutoLogin(final String autoLogin) {
         if ((autoLogin != null) && (autoLogin.equalsIgnoreCase(TRUE) || autoLogin.equals("1"))) {
             this.setAutoLogin(true);
         } else if ((autoLogin != null) && (autoLogin.equalsIgnoreCase(FALSE) || autoLogin.equals("0"))) {
             this.setAutoLogin(false);
         } else {
             this.setAutoLogin(false);
             logger.warn("setAutoLogin(): invalid property 'autoLogin': '" + autoLogin + "', setting default value to '"
                         + this.autoLogin + "'");
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  autoLogin  DOCUMENT ME!
      */
     public void setAutoLogin(final boolean autoLogin) {
         this.autoLogin = autoLogin;
         properties.setProperty("autoLogin", String.valueOf(this.autoLogin)); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isAutoLogin() {
         return this.autoLogin;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  maxConnections  DOCUMENT ME!
      */
     public void setMaxConnections(final String maxConnections) {
         try {
             final int imaxConnections = Integer.parseInt(maxConnections);
             this.setMaxConnections(imaxConnections);
         } catch (Exception exp) {
             logger.warn("setMaxConnections(): invalid property 'maxConnections': '" + exp.getMessage() + "'"); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  maxConnections  DOCUMENT ME!
      */
     public void setMaxConnections(final int maxConnections) {
         if ((maxConnections < MIN_SERVER_THREADS) || (maxConnections > MAX_SERVER_THREADS)) {
             this.maxConnections = MIN_SERVER_THREADS;
             properties.setProperty("maxConnections", String.valueOf(MIN_SERVER_THREADS));
             logger.warn("setMaxConnections(): invalid property 'maxConnections': '" + maxConnections
                         + "', setting default value to '" + MIN_SERVER_THREADS + "'");
         } else {
             this.maxConnections = maxConnections;
             properties.setProperty("maxConnections", String.valueOf(maxConnections)); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getMaxConnections() {
         return this.maxConnections;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  advancedLayout  DOCUMENT ME!
      */
     public void setAdvancedLayout(final String advancedLayout) {
         if ((advancedLayout != null) && (advancedLayout.equalsIgnoreCase(TRUE) || advancedLayout.equals("1"))) {
             this.setAdvancedLayout(true);
         } else if ((advancedLayout != null) && (advancedLayout.equalsIgnoreCase(FALSE) || advancedLayout.equals("0"))) {
             this.setAdvancedLayout(false);
         } else {
             this.setAdvancedLayout(false);
             logger.warn("setAdvancedLayout(): invalid property 'advancedLayout': '" + advancedLayout
                         + "', setting default value to '" + this.advancedLayout + "'");
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  advancedLayout  DOCUMENT ME!
      */
     public void setAdvancedLayout(final boolean advancedLayout) {
         this.advancedLayout = advancedLayout;
         properties.setProperty("advancedLayout", String.valueOf(this.advancedLayout)); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isAdvancedLayout() {
         return this.advancedLayout;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  lookAndFeelName  DOCUMENT ME!
      */
     public void setLookAndFeel(final String lookAndFeelName) {
         if (LAFManager.getManager().isInstalledLookAndFeel(lookAndFeelName)) {
             this.lookAndFeel = lookAndFeelName;
         } else {
             // this.lookAndFeel = LNF_METAL;
             this.lookAndFeel = LAFManager.getManager().getDefaultLookAndFeel().getName();
             logger.warn("setLookAndFeel(): invalid property 'lookAndFeel': '" + lookAndFeelName
                         + "', setting default value to '" + this.lookAndFeel + "'");
         }
         properties.setProperty("lookAndFeel", this.lookAndFeel); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getLookAndFeel() {
         return this.lookAndFeel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  sortChildren  DOCUMENT ME!
      */
     public void setSortChildren(final String sortChildren) {
         if ((sortChildren != null) && (sortChildren.equalsIgnoreCase(TRUE) || sortChildren.equals("1"))) {
             this.setSortChildren(true);
         } else if ((sortChildren != null) && (sortChildren.equalsIgnoreCase(FALSE) || sortChildren.equals("0"))) {
             this.setSortChildren(false);
         } else {
             this.setSortChildren(false);
             logger.warn("setSortChildren(): invalid property 'sortChildren': '" + sortChildren
                         + "', setting default value to '" + this.sortChildren + "'");
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  sortChildren  DOCUMENT ME!
      */
     public void setSortChildren(final boolean sortChildren) {
         this.sortChildren = sortChildren;
         properties.setProperty("sortChildren", String.valueOf(this.sortChildren)); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isSortChildren() {
         return this.sortChildren;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  sortAscending  DOCUMENT ME!
      */
     public void setSortAscending(final String sortAscending) {
         if ((sortAscending != null) && (sortAscending.equalsIgnoreCase(TRUE) || sortAscending.equals("1"))) {
             this.setSortAscending(true);
         } else if ((sortAscending != null) && (sortAscending.equalsIgnoreCase(FALSE) || sortAscending.equals("0"))) {
             this.setSortAscending(false);
         } else {
             this.setSortAscending(false);
             logger.warn("setSortAscending(): invalid property 'sortAscending': '" + sortAscending
                         + "', setting default value to '" + this.sortAscending + "'");
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  sortAscending  DOCUMENT ME!
      */
     public void setSortAscending(final boolean sortAscending) {
         this.sortAscending = sortAscending;
         properties.setProperty("sortAscending", String.valueOf(this.sortAscending)); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isSortAscending() {
         return this.sortAscending;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  connectionInfoSaveable  DOCUMENT ME!
      */
     public void setConnectionInfoSaveable(final String connectionInfoSaveable) {
         if ((connectionInfoSaveable != null)
                     && (connectionInfoSaveable.equalsIgnoreCase(TRUE) || connectionInfoSaveable.equals("1"))) {
             this.setConnectionInfoSaveable(true);
         } else if ((connectionInfoSaveable != null)
                     && (connectionInfoSaveable.equalsIgnoreCase(FALSE) || connectionInfoSaveable.equals("0"))) {
             this.setConnectionInfoSaveable(false);
         } else {
             this.setConnectionInfoSaveable(false);
             logger.warn("connectionInfoSaveable(): invalid property 'connectionInfoSaveable': '"
                         + connectionInfoSaveable + "', setting default value to '" + this.connectionInfoSaveable + "'");
         }
     }
 
     /**
      * Setter for property connectionInfoSaveable.
      *
      * @param  connectionInfoSaveable  New value of property connectionInfoSaveable.
      */
     public void setConnectionInfoSaveable(final boolean connectionInfoSaveable) {
         this.connectionInfoSaveable = this.isSaveable() & connectionInfoSaveable;
         properties.setProperty("connectionInfoSaveable", String.valueOf(this.connectionInfoSaveable)); // NOI18N
     }
 
     /**
      * Getter for property connectionInfoSaveable.
      *
      * @return  Value of property connectionInfoSaveable.
      */
     public boolean isConnectionInfoSaveable() {
         return this.connectionInfoSaveable;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  loadable  DOCUMENT ME!
      */
     public void setLoadable(final String loadable) {
         if ((loadable != null) && (loadable.equalsIgnoreCase(TRUE) || loadable.equals("1"))) {
             this.setLoadable(true);
         } else if ((loadable != null) && (loadable.equalsIgnoreCase(FALSE) || loadable.equals("0"))) {
             this.setLoadable(false);
         } else {
             this.setLoadable(false);
             logger.warn("loadable(): invalid property 'loadable': '" + loadable + "', setting default value to '"
                         + this.loadable + "'");
         }
     }
 
     /**
      * Setter for property loadable.
      *
      * @param  loadable  New value of property loadable.
      */
     public void setLoadable(final boolean loadable) {
         this.loadable = loadable;
         properties.setProperty("loadable", String.valueOf(this.loadable)); // NOI18N
     }
 
     /**
      * Getter for property loadable.
      *
      * @return  Value of property loadable.
      */
     public boolean isLoadable() {
         return this.loadable;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  saveable  DOCUMENT ME!
      */
     public void setSaveable(final String saveable) {
         if ((saveable != null) && (saveable.equalsIgnoreCase(TRUE) || saveable.equals("1"))) {
             this.setSaveable(true);
         } else if ((saveable != null) && (saveable.equalsIgnoreCase(FALSE) || saveable.equals("0"))) {
             this.setSaveable(false);
         } else {
             this.setSaveable(false);
             logger.warn("saveable(): invalid property 'saveable': '" + saveable + "', setting default value to '"
                         + this.saveable + "'");
         }
     }
 
     /**
      * Setter for property saveable.
      *
      * @param  saveable  New value of property saveable.
      */
     public void setSaveable(final boolean saveable) {
         this.saveable = saveable;
         properties.setProperty("saveable", String.valueOf(this.saveable)); // NOI18N
     }
 
     /**
      * Getter for property saveable.
      *
      * @return  Value of property saveable.
      */
     public boolean isSaveable() {
         return this.saveable;
     }
 
     /**
      * Setter for property useFlyingSaucer.
      *
      * @param  useFlyingSaucer  String containing 'true'/'false' or '1'/'0'.
      */
     public void setUseFlyingSaucer(final String useFlyingSaucer) {
         if ((useFlyingSaucer != null) && (useFlyingSaucer.equalsIgnoreCase(TRUE) || useFlyingSaucer.equals("1"))) {
             this.setUseFlyingSaucer(true);
         } else if ((useFlyingSaucer != null)
                     && (useFlyingSaucer.equalsIgnoreCase(FALSE) || useFlyingSaucer.equals("0"))) {
             this.setUseFlyingSaucer(false);
         } else {
             this.setUseFlyingSaucer(false);
             logger.warn("setUseFlyingSaucer(): invalid property 'useFlyingSaucer': '" + useFlyingSaucer
                         + "', setting default value to '"
                         + this.useFlyingSaucer + "'");
         }
     }
 
     /**
      * Setter for property useFlyingSaucer.
      *
      * @param  useFlyingSaucer  DOCUMENT ME!
      */
     public void setUseFlyingSaucer(final boolean useFlyingSaucer) {
         this.useFlyingSaucer = useFlyingSaucer;
         if (useFlyingSaucer) {
             setDescriptionPaneHtmlRenderer(PropertyManager.FLYING_SAUCER_HTML_RENDERER);
         }
         properties.setProperty("setUseFlyingSaucer", String.valueOf(this.useFlyingSaucer)); // NOI18N
     }
 
     /**
      * Getter for property useFlyingSaucer.
      *
      * @return      Value of property useFlyingSaucer.
      *
      * @deprecated  use {@link getDescriptionPaneHtmlRenderer()} instead
      */
     @Deprecated
     public boolean isUseFlyingSaucer() {
         return this.useFlyingSaucer;
     }
 
     /**
      * Setter for property useWebView.
      *
      * @param  useWebView  String containing 'true'/'false' or '1'/'0'.
      */
     public void setUseWebView(final String useWebView) {
         if ((useWebView != null) && (useWebView.equalsIgnoreCase(TRUE) || useWebView.equals("1"))) {
             this.setUseWebView(true);
         } else if ((useWebView != null)
                     && (useWebView.equalsIgnoreCase(FALSE) || useWebView.equals("0"))) {
             this.setUseWebView(false);
         } else {
             this.setUseWebView(false);
             logger.warn("setUseWebView(): invalid property 'useWebView': '" + useWebView
                         + "', setting default value to '"
                         + this.useWebView + "'");
         }
     }
 
     /**
      * Setter for property useWebView.
      *
      * @param  useWebView  New value of property useWebView.
      */
     public void setUseWebView(final boolean useWebView) {
         this.useWebView = useWebView;
         if (useWebView) {
             setDescriptionPaneHtmlRenderer(PropertyManager.FX_HTML_RENDERER);
         }
         properties.setProperty("useWebView", String.valueOf(this.useWebView)); // NOI18N
     }
 
     /**
      * Getter for property useWebView.
      *
      * @return      Value of property useWebView.
      *
      * @deprecated  use {@link getDescriptionPaneHtmlRenderer()} instead
      */
     @Deprecated
     public boolean isUseWebView() {
         return this.useWebView;
     }
 
     /**
      * Setter for property enableSearchDialog.
      *
      * @param  enableSearchDialog  String containing 'true'/'false' or '1'/'0'.
      */
     public void setEnableSearchDialog(final String enableSearchDialog) {
         if ((enableSearchDialog != null)
                     && (enableSearchDialog.equalsIgnoreCase(TRUE) || enableSearchDialog.equals("1"))) {
             this.setEnableSearchDialog(true);
         } else if ((enableSearchDialog != null)
                     && (enableSearchDialog.equalsIgnoreCase(FALSE) || enableSearchDialog.equals("0"))) {
             this.setEnableSearchDialog(false);
         } else {
             this.setEnableSearchDialog(false);
             logger.warn("setEnableSearchDialog(): invalid property 'enableSearchDialog': '" + enableSearchDialog
                         + "', setting default value to '"
                         + this.enableSearchDialog + "'");
         }
     }
 
     /**
      * Setter for property enableSearchDialog.
      *
      * @param  enableSearchDialog  New value of property enableSearchDialog.
      */
     public void setEnableSearchDialog(final boolean enableSearchDialog) {
         this.enableSearchDialog = enableSearchDialog;
         properties.setProperty("enableSearchDialog", String.valueOf(this.enableSearchDialog)); // NOI18N
     }
 
     /**
      * Getter for property enableSearchDialog.
      *
      * @return  Value of property enableSearchDialog.
      */
     public boolean isEnableSearchDialog() {
         return this.enableSearchDialog;
     }
 
     /**
      * .........................................................................
      *
      * @return  DOCUMENT ME!
      */
     public ConnectionInfo getConnectionInfo() {
         return this.connectionInfo;
     }
 
     /**
      * .........................................................................
      */
     private void load() {
         final Enumeration keys = properties.keys();
         while (keys.hasMoreElements()) {
             final String key = (String)keys.nextElement();
             this.setProperty(key, properties.getProperty(key));
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  property  DOCUMENT ME!
      * @param  value     DOCUMENT ME!
      */
     private synchronized void setProperty(final String property, final String value) {
         if (logger.isDebugEnabled()) {
             logger.debug("setting property '" + property + "' to '" + value + "'"); // NOI18N
             /*if(property.equalsIgnoreCase("title"))
              * { this.setTitle(value); }else*/
         }
         if (property.equalsIgnoreCase("width")) {                                              // NOI18N
             this.setWidth(value);
         } else if (property.equalsIgnoreCase("height")) {                                      // NOI18N
             this.setHeight(value);
         } else if (property.equalsIgnoreCase("maximizeWindow")) {                              // NOI18N
             this.setMaximizeWindow(value);
         } else if (property.equalsIgnoreCase("advancedLayout")) {                              // NOI18N
             this.setAdvancedLayout(value);
         } else if (property.equalsIgnoreCase("lookAndFeel")) {                                 // NOI18N
             this.setLookAndFeel(value);
         } else if (property.equalsIgnoreCase("autoLogin")) {                                   // NOI18N
             this.setAutoLogin(value);
         } else if (property.equalsIgnoreCase("connectionClass")) {                             // NOI18N
             this.setConnectionClass(value);
         } else if (property.equalsIgnoreCase("connectionProxyClass")) {                        // NOI18N
             this.setConnectionProxyClass(value);
         } else if (property.equalsIgnoreCase("maxConnections")) {                              // NOI18N
             this.setMaxConnections(value);
         } else if (property.equalsIgnoreCase("sortChildren")) {                                // NOI18N
             this.setSortChildren(value);
         } else if (property.equalsIgnoreCase("sortAscending")) {                               // NOI18N
             this.setSortAscending(value);
         } else if (property.equalsIgnoreCase("saveable")) {                                    // NOI18N
             this.setSaveable(value);
         } else if (property.equalsIgnoreCase("loadable")) {                                    // NOI18N
             this.setLoadable(value);
         } else if (property.equalsIgnoreCase("connectionInfoSaveable")) {                      // NOI18N
             this.setConnectionInfoSaveable(value);
         } else if (property.equalsIgnoreCase("callserverURL")) {                               // NOI18N
             this.connectionInfo.setCallserverURL(value);
         } else if (property.equalsIgnoreCase("password")) {                                    // NOI18N
             this.connectionInfo.setPassword(value);
         } else if (property.equalsIgnoreCase("userDomain")) {                                  // NOI18N
             this.connectionInfo.setUserDomain(value);
         } else if (property.equalsIgnoreCase("usergroup")) {                                   // NOI18N
             this.connectionInfo.setUsergroup(value);
         } else if (property.equalsIgnoreCase("usergroupDomain")) {                             // NOI18N
             this.connectionInfo.setUsergroupDomain(value);
         } else if (property.equalsIgnoreCase("username")) {                                    // NOI18N
             this.connectionInfo.setUsername(value);
         } else if (property.equalsIgnoreCase("useFlyingSaucer")) {                             // NOI18N
             logger.warn(
                 "Property useFlyingSaucer is deprecated and should be replaced with Property " // NOI18N
                         + "navigator.descriptionPane.htmlRenderer=fylingSaucer");              // NOI18N
             this.setUseFlyingSaucer(value);
         } else if (property.equalsIgnoreCase("useWebView")) {                                  // NOI18N
             logger.warn(
                 "Property useWebView is deprecated and should be replaced with Property "      // NOI18N
                        + "navigator.descriptionPane.htmlRenderer=fxWebView");                 // NOI18N
             this.setUseWebView(value);
         } else if (property.equalsIgnoreCase("enableSearchDialog")) {                          // NOI18N
             this.setEnableSearchDialog(value);
         } else if (property.equals("navigator.proxy.url")) {
             this.setProxyURL(value);
         } else if (property.equals("navigator.proxy.username")) {
             this.setProxyUsername(value);
         } else if (property.equals("navigator.proxy.password")) {
             this.setProxyPassword(value);
         } else if (property.equals("navigator.proxy.domain")) {
             this.setProxyDomain(value);
         } else if (property.equals("navigator.descriptionPane.htmlRenderer")) {
             this.setDescriptionPaneHtmlRenderer(value);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  inStream  DOCUMENT ME!
      */
     public void load(final InputStream inStream) {
         if (this.isLoadable()) {
             try {
                 this.properties.load(inStream);
                 this.load();
             } catch (Exception exp) {
                 logger.fatal("could not load properties: " + exp.getMessage(), exp); // NOI18N
             }
         } else {
             logger.error("could not load properties: properties not loadable");      // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void configure() {
         this.load(this.getClass().getResourceAsStream("cfg/navigator.cfg")); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   cfgFile         DOCUMENT ME!
      * @param   basePath        DOCUMENT ME!
      * @param   pluginPath      DOCUMENT ME!
      * @param   searchFormPath  DOCUMENT ME!
      * @param   profilesPath    DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public void configure(final String cfgFile,
             final String basePath,
             final String pluginPath,
             final String searchFormPath,
             final String profilesPath) throws Exception {
         this.applet = false;
         this.application = true;
 
         if (basePath != null) {
             logger.info("setting base path to '" + basePath + "'"); // NOI18N
             this.basePath = basePath;
         } else {
             this.getBasePath();
         }
 
         if (pluginPath != null) {
             logger.info("setting base plugin to '" + pluginPath + "'"); // NOI18N
             this.pluginPath = pluginPath;
         } else {
             this.getPluginPath();
         }
 
         if (searchFormPath != null) {
             logger.info("setting search form path to '" + searchFormPath + "'"); // NOI18N
             this.searchFormPath = searchFormPath;
         } else {
             this.getSearchFormPath();
         }
 
         if (profilesPath != null) {
             logger.info("setting profiles path to '" + profilesPath + "'"); // NOI18N
             this.profilesPath = profilesPath;
         } else {
             this.getProfilesPath();
         }
 
         if (cfgFile != null) {
             if ((cfgFile.indexOf("http://") == 0) || (cfgFile.indexOf("https://") == 0)
                         || (cfgFile.indexOf("file:/") == 0)) {
                 final URL url = new URL(cfgFile);
                 this.load(url.openStream());
 
                 logger.info("config file loaded from url (assuming webstart)");     // NOI18N
                 this.applet = true;
             } else {
                 final File file = new File(cfgFile);
                 this.load(new BufferedInputStream(new FileInputStream(cfgFile)));
             }
         } else {
             throw new Exception("loading of config file '" + cfgFile + "' failed"); // NOI18N
         }
 
         try {
             final String parameter = this.properties.getProperty("plugins");
             setHttpInterfacePort(new Integer(properties.getProperty("httpInterfacePort", "9099")));
             setAutoClose(Boolean.valueOf(properties.getProperty("closeWithoutAsking", "false")));
 
             if ((parameter != null) && (parameter.length() > 0)) {
                 pluginList = new ArrayList();
                 final StringTokenizer tokenizer = new StringTokenizer(parameter, ";");
                 while (tokenizer.hasMoreTokens()) {
                     final String plugin = tokenizer.nextToken().trim() + "/";
                     logger.info("adding plugin from config file: '" + plugin + "'");
                     pluginList.add(pluginPath + "/" + plugin);
                 }
             }
         } catch (Exception except) {
             logger.fatal(except, except);
         }
 
         this.isPluginListAvailable();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  applet  DOCUMENT ME!
      */
     public void configure(final JApplet applet) {
         if (logger.isDebugEnabled()) {
             logger.debug("configure property manager (applet)");
         }
         this.pluginList = new ArrayList();
         this.applet = true;
         this.application = false;
         this.appletContext = applet.getAppletContext();
 
         this.basePath = applet.getCodeBase().toString();             // + "/";
         logger.info("setting base path to '" + this.basePath + "'"); // NOI18N
 
         this.pluginPath = this.basePath + "plugins/";                     // NOI18N
         logger.info("setting plugins path to '" + this.pluginPath + "'"); // NOI18N
 
         this.searchFormPath = this.basePath + "search/";                           // NOI18N
         logger.info("setting search forms path to '" + this.searchFormPath + "'"); // NOI18N
 
         this.readAppletParameters(applet);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  applet  DOCUMENT ME!
      */
     private void readAppletParameters(final JApplet applet) {
         // configfile
         String parameter = applet.getParameter("configfile");                                                      // NOI18N
         if ((parameter != null) && (parameter.length() > 0)) {
             if (logger.isDebugEnabled()) {
                 logger.debug("loading configfile from remote url '" + this.getBasePath() + parameter + "'");       // NOI18N
             }
             try {
                 final URL url = new URL(this.getBasePath() + parameter);
                 this.load(new BufferedInputStream(url.openStream()));
             } catch (Exception exp) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("could not load configfile, trying to load file from local filesystem\n"
                                 + exp.getMessage());
                 }
                 try {
                     final File file = new File(parameter);
                     this.load(new BufferedInputStream(new FileInputStream(file)));
                 } catch (Exception ioexp) {
                     logger.error("could not load configfile, using default configuration\n" + ioexp.getMessage()); // NOI18N
                     this.configure();
                 }
             }
         }
 
         parameter = applet.getParameter("plugins"); // NOI18N
         if ((parameter != null) && (parameter.length() > 0)) {
             final StringTokenizer tokenizer = new StringTokenizer(parameter, ";");
             while (tokenizer.hasMoreTokens()) {
                 final String plugin = this.pluginPath + tokenizer.nextToken().trim() + "/";
                 logger.info("adding plugin '" + plugin + "'");
                 pluginList.add(plugin);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  outStream  DOCUMENT ME!
      */
     public void save(final OutputStream outStream) {
         if (this.isSaveable()) {
             try {
                 this.properties.store(outStream, HEADER);
             } catch (Exception exp) {
                 logger.fatal("could not save properties: " + exp.getMessage(), exp); // NOI18N
             }
         } else {
             logger.error("could not save properties: properties not saveable");      // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void print() {
         properties.list(System.out);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static PropertyManager getManager() {
         return manager;
     }
 
     /**
      * Getter for property applet.
      *
      * @return  Value of property applet.
      */
     public boolean isApplet() {
         return this.applet;
     }
 
     /**
      * Getter for property application.
      *
      * @return  Value of property application.
      */
     public boolean isApplication() {
         return this.application;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getBasePath() {
         if (this.basePath == null) {
             this.basePath = System.getProperty("user.home") + System.getProperty("file.separator") + ".navigator"
                         + System.getProperty("file.separator");
             logger.info("no base path set, setting default base path to '" + this.basePath + "'");
 
             final File file = new File(this.basePath);
             if (!file.exists()) {
                 logger.warn("base path does not exist, creating base path"); // NOI18N
                 if (!file.mkdirs()) {
                     logger.error("could not create base path");              // NOI18N
                 }
             }
         }
 
         return this.basePath;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getPluginPath() {
         if (this.pluginPath == null) {
             if (this.getBasePath().startsWith("http") || this.getBasePath().startsWith("file")) {        // NOI18N
                 this.pluginPath = this.getBasePath() + "plugins/";                                       // NOI18N
             } else {
                 this.pluginPath = this.getBasePath() + "plugins" + System.getProperty("file.separator"); // NOI18N
             }
 
             logger.info("no plugin path set, setting default plugin path to '" + this.pluginPath + "'"); // NOI18N
         }
 
         return this.pluginPath;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getSearchFormPath() {
         if (this.searchFormPath == null) {
             if (this.getBasePath().startsWith("http")) {                                                               // NOI18N
                 this.searchFormPath = this.basePath + "search/";                                                       // NOI18N
             } else {
                 this.searchFormPath = this.basePath + "search" + System.getProperty("file.separator");                 // NOI18N
             }
             logger.info("no search form path set, setting default search form path to '" + this.searchFormPath + "'"); // NOI18N
         }
 
         return this.searchFormPath;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getProfilesPath() {
         if ((this.profilesPath == null) || this.profilesPath.equals("AUTO")) {
             if (this.getBasePath().startsWith("http")) {
                 logger.info("no profiles path set and base path == URL, settting default profiles path to user.home");
                 this.profilesPath = new StringBuffer().append(System.getProperty("user.home"))
                             .append(System.getProperty("file.separator"))
                             .append(".navigator")
                             .append(System.getProperty("file.separator"))
                             .append("profiles")
                             .append(System.getProperty("file.separator"))
                             .toString();
             } else {
                 this.profilesPath = this.basePath + "profiles" + System.getProperty("file.separator"); // NOI18N
             }
 
             logger.info("no profiles form path set, setting default search form path to '" + this.profilesPath + "'"); // NOI18N
 
             final File file = new File(this.profilesPath);
             if (!file.exists()) {
                 logger.warn("profiles path does not exist, creating base path"); // NOI18N
                 if (!file.mkdirs()) {
                     logger.error("could not create profiles path");              // NOI18N
                 }
             }
         }
 
         return this.profilesPath;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isPluginListAvailable() {
         if (this.pluginList == null) {
             this.pluginList = new ArrayList();
             final File file = new File(this.pluginPath);
             if (file.exists() && file.isDirectory()) {
                 final File[] plugins = file.listFiles();
                 if ((plugins != null) && (plugins.length > 0)) {
                     for (int i = 0; i < plugins.length; i++) {
                         if (plugins[i].isDirectory()) {
                             if (!plugins[i].getName().equalsIgnoreCase("CVS")) {
                                 final String plugin = plugins[i].getPath() + System.getProperty("file.separator");
                                 logger.info("adding plugin '" + plugin + "'");
                                 pluginList.add(plugin);
                             } else {
                                 if (logger.isDebugEnabled()) {
                                     logger.warn("plugin directory with name 'CVS' found. ignoring plugin!"); // NOI18N
                                 }
                             }
                         }
                     }
                 }
             } else {
                 logger.warn("'" + this.pluginPath + "' does not exist or is no valid plugin directory");     // NOI18N
             }
         }
 
         return (this.pluginList.size() > 0) ? true : false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Iterator getPluginList() {
         if (this.isPluginListAvailable()) {
             return this.pluginList.iterator();
         } else {
             logger.warn("sorry, no plugins could be found"); // NOI18N
             return null;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Properties getProperties() {
         return this.properties;
     }
 
     /**
      * Getter for property sharedProgressObserver.
      *
      * @return  Value of property sharedProgressObserver.
      */
     public synchronized ProgressObserver getSharedProgressObserver() {
         return this.sharedProgressObserver;
     }
 
     /**
      * Getter for property editable.
      *
      * @return  Value of property editable.
      */
     public boolean isEditable() {
         return this.editable;
     }
 
     /**
      * Setter for property editable.
      *
      * @param  editable  New value of property editable.
      */
     public void setEditable(final boolean editable) {
         this.editable = editable;
     }
 
     /**
      * Getter for property webstart.
      *
      * @return  Value of property webstart.
      */
     public boolean isWebstart() {
         return this.isApplet() & this.isApplication();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getHttpInterfacePort() {
         return httpInterfacePort;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  httpInterfacePort  DOCUMENT ME!
      */
     public void setHttpInterfacePort(final int httpInterfacePort) {
         this.httpInterfacePort = httpInterfacePort;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isAutoClose() {
         return autoClose;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  autoClose  DOCUMENT ME!
      */
     public void setAutoClose(final boolean autoClose) {
         this.autoClose = autoClose;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  htmlRenderer  DOCUMENT ME!
      */
     public void setDescriptionPaneHtmlRenderer(final String htmlRenderer) {
         this.descriptionPaneHtmlRenderer = htmlRenderer;
         if (descriptionPaneHtmlRenderer.equals(PropertyManager.FLYING_SAUCER_HTML_RENDERER)) {
 //            this.setUseFlyingSaucer(true);
 //            this.setUseWebView(false);
         } else if (descriptionPaneHtmlRenderer.equals(PropertyManager.FX_HTML_RENDERER)) {
 //            this.setUseWebView(true);
 //            this.setUseFlyingSaucer(false);
         } else {
 //            this.setUseFlyingSaucer(false);
 //            this.setUseWebView(false);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getDescriptionPaneHtmlRenderer() {
         return this.descriptionPaneHtmlRenderer;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * .........................................................................
      *
      * @version  $Revision$, $Date$
      */
     private class ConnectionInfoChangeListener implements PropertyChangeListener {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * This method gets called when a bound property is changed.
          *
          * @param  evt  A PropertyChangeEvent object describing the event source and the property that has changed.
          */
         @Override
         public void propertyChange(final PropertyChangeEvent evt) {
             if (isConnectionInfoSaveable()) {
                 properties.setProperty(evt.getPropertyName(), evt.getNewValue().toString());
             }
         }
     }
 }
