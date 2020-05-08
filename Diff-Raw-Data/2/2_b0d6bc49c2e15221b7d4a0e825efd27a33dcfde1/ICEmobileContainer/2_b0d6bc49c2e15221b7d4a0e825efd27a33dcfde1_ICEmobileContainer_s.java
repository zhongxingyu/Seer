 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.icemobile.client.blackberry;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import javax.microedition.io.InputConnection;
 import javax.microedition.io.file.FileSystemRegistry;
 
 import org.icemobile.client.blackberry.authentication.AuthenticatingProtocolHandler;
 import org.icemobile.client.blackberry.authentication.AuthenticationManager;
 import org.icemobile.client.blackberry.options.BlackberryOptionsProperties;
 import org.icemobile.client.blackberry.options.BlackberryOptionsProvider;
 import org.icemobile.client.blackberry.push.PushAgent;
 import org.icemobile.client.blackberry.script.audio.AudioRecorderLauncher;
 import org.icemobile.client.blackberry.script.camera.VideoController;
 import org.icemobile.client.blackberry.script.camera.WidgetCameraController;
 import org.icemobile.client.blackberry.script.debug.JavascriptDebugger;
 import org.icemobile.client.blackberry.script.scan.QRCodeScanner;
 import org.icemobile.client.blackberry.script.test.ScriptableTest;
 import org.icemobile.client.blackberry.script.upload.AjaxUpload;
 import org.icemobile.client.blackberry.script.upload.ScriptResultReader;
 import org.icemobile.client.blackberry.utils.HistoryManager;
 import org.icemobile.client.blackberry.utils.ResultHolder;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import net.rim.blackberry.api.options.OptionsManager;
 import net.rim.device.api.browser.field.RenderingOptions;
 import net.rim.device.api.browser.field.RenderingSession;
 import net.rim.device.api.browser.field2.BrowserField;
 import net.rim.device.api.browser.field2.BrowserFieldConfig;
 import net.rim.device.api.browser.field2.BrowserFieldConnectionManager;
 import net.rim.device.api.browser.field2.BrowserFieldController;
 import net.rim.device.api.browser.field2.BrowserFieldCookieManager;
 import net.rim.device.api.browser.field2.BrowserFieldHistory;
 import net.rim.device.api.browser.field2.BrowserFieldListener;
 import net.rim.device.api.browser.field2.BrowserFieldRequest;
 import net.rim.device.api.browser.field2.ProtocolController;
 import net.rim.device.api.io.IOUtilities;
 import net.rim.device.api.io.http.HttpHeaders;
 import net.rim.device.api.script.ScriptEngine;
 import net.rim.device.api.system.ApplicationManager;
 import net.rim.device.api.system.CoverageInfo;
 import net.rim.device.api.system.DeviceInfo;
 import net.rim.device.api.system.EncodedImage;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.system.SystemListener;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BitmapField;
 import net.rim.device.api.ui.container.MainScreen;
 
 import net.rim.blackberry.api.messagelist.*;
 
 /**
  * This class extends the UiApplication class, providing a
  * graphical user interface.
  */
 public class ICEmobileContainer extends UiApplication implements SystemListener,
                                                                          ContainerController {
 
     // Default, compile time, initial page. This value is passed to BlackberryOptionsProperties
     // during construction to be used if the Properties object is new
     public static final String HOME_URL = "http://www.icemobile.org/demos.html";
 
     public static final int HISTORY_SIZE = 10;
 
     // Some startup branding options
     private static final String LOADING_IMAGE = "ICEfaces-360x480.gif";
 
     // Logging GUID code moved to Logger class
 
     // Essential Webkit browser member variables 
     private BrowserField mBrowserField;
     private ScriptEngine mScriptEngine;
     private BrowserFieldCookieManager mBrowserCookieManager;
     private BrowserFieldHistory mBrowserHistory;
     private BrowserFieldController mBrowserController;
     private String mInitialScript;
     private RenderingSession mRenderingSession;
 
     // Blackberry Options variables 
     private BlackberryOptionsProperties mOptionsProperties;
     private BlackberryOptionsProvider mOptionsProvider;
 
     // load screen variables
     private MainScreen mMainScreen;
     private EncodedImage mLoadingImage;
 
     // Load screen vars
     private BitmapField mLoadingField = new BitmapField();
     private MainScreen mLoadingScreen;
 
     // Hashtable of pending responses for asynchronous update
     private Hashtable mPendingResponses = new Hashtable();
 
     // if true, the application will reload the page on Container activation. 
     // Set from the options menu. 
     private boolean mReloadOnActivate;
     private HistoryManager mHistoryManager;
 
     // RIM public infrastructure PUSH agent
     private PushAgent mPushAgent;
 
     //    private PushServiceListener mPushServiceListener;
     private static ApplicationIndicator mAppIndicator;
 
     // Keeping track of the home page
     private String mCurrentHome;
 
     // Keeping track of page load times
     private String mCurrentlyLoadingDocument;
     private long mDocumentStartTime;
 
     private boolean mRealDevice = !DeviceInfo.isSimulator();
 
     private Element mScriptElement;
     private AuthenticationManager mAuthenticationManager = new AuthenticationManager(this);
 
 
     private Runnable mParkScriptRunner = new Runnable() {
         public void run() {
 
             try {
                 if (mParkScript != null && mRealDevice) {
                     mScriptEngine.executeScript(mParkScript, null);
                     Logger.TRACE("ICEmobile - parkScript success: " + mParkScript);
                 }
             } catch (Throwable t) {
                 Logger.DEBUG("ICEmobile - ParkScript Exception is: " + t);
             }
         }
     };
 
     private String mParkScript;
     private String mPauseScript;
 
 
     /**
      * Main Entry point
      */
     public static void main(String[] args) {
 
         ICEmobileContainer iceMobile = new ICEmobileContainer();
         // if system startup is still in progress when this application is run 
         if (ApplicationManager.getApplicationManager().inStartup()) {
             iceMobile.addSystemListener(iceMobile);
         } else {
             iceMobile.eulaCheck();
         }
 
         iceMobile.enterEventDispatcher();
     }
 
     public ICEmobileContainer() {
 
         EventLogger.clearLog();
         EventLogger.register(GUID, "ICE", EventLogger.VIEWER_STRING);
         EventLogger.setMinimumLevel(EventLogger.DEBUG_INFO);
         optionsChanged();
     }
 
 
     /**
      * System callback method for powerup. This indicates device is up.
      */
     public void powerUp() {
         removeSystemListener(this);
         eulaCheck();
     }
 
     /**
      * Check if the eula has been read. If so go on to init().
      * Otherwise let the EulaManager show the screen. By convention
      * the init() method will be called from the 'ok' button to
      * guarantee the eula has been ok'd.
      */
     private void eulaCheck() {
 
         if (!mOptionsProperties.isEulaViewed()) {
             Logger.DEBUG("ICEmobile - Launching eula viewer");
             EulaManager em = new EulaManager(this);
             em.show();
         } else {
             init();
         }
     }
 
 
     /**
      * Initialize the Container. This method initializes the BrowserField settings for future operation
      * and loads the option system to setup the correct start page and settings.  This method should
      * be called from the EventThread once the device is initialized after the eulaCheck has been
      * performed.
      */
     public void init() {
 
         mHistoryManager = new HistoryManager(HISTORY_SIZE);
 
         try {
 
             mMainScreen = new ApplicationScreen(this);
             pushScreen(mMainScreen);
 
             // Add loading screen and display ASAP
             mLoadingImage = EncodedImage.getEncodedImageResource(LOADING_IMAGE);
 
             if (mLoadingImage != null) {
                 // If a loading image exists, add it to the loading field and push it onto the screen stack.
                 mLoadingField.setImage(mLoadingImage);
                 mLoadingScreen = new MainScreen();
                 mLoadingScreen.add(mLoadingField);
                 pushScreen(mLoadingScreen);
             }
 
             mInitialScript = readLocalFileSystem(getClass(), "blackberry-interface.js");
 
             // This should put an error screen on the page. No sense continuing if the javascript isn't found
             if (mInitialScript == null) {
                 Logger.ERROR("interface.js NOT found");
                 return;
             } else {
                 Logger.DEBUG("local js - length: " + mInitialScript.length());
             }
 
 
             // Set up the browser/renderer.
             mRenderingSession = RenderingSession.getNewInstance();
             mRenderingSession.getRenderingOptions().setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.JAVASCRIPT_ENABLED, true);
             mRenderingSession.getRenderingOptions().setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.JAVASCRIPT_LOCATION_ENABLED, true);
             // Enable nice-looking BlackBerry browser field.
             mRenderingSession.getRenderingOptions().setProperty(RenderingOptions.CORE_OPTIONS_GUID, 17000, true);
 
             mOptionsProvider = new BlackberryOptionsProvider(this);
             OptionsManager.registerOptionsProvider(mOptionsProvider);
 
 
             setupBrowserGui();
 
 
             // Push registration will be performed here via one of two 
             // mechanisms
             //            setupPushListener();
             if (mRealDevice) {
                 mPushAgent = new PushAgent();
             }
 
             ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
             EncodedImage image = EncodedImage.getEncodedImageResource("icemobile-icon-32x32.png");
             ApplicationIcon icon = new ApplicationIcon(image);
 
             ApplicationIndicator indicator = reg.register(icon, true, false);
             mAppIndicator = reg.getApplicationIndicator();
             //			mAppIndicator.set ( )
 
             mBrowserField.addListener(new BrowserFieldListener() {
                 public void documentLoaded(BrowserField field,
                                            Document document) {
 
                     try {
 //                    	Logger.TRACE(System.currentTimeMillis(), mStartTime,  "DocumentLoaded: " + document.getBaseURI() );
 
                         final String uri = document.getBaseURI();
                         if (uri.indexOf("about:blank") == -1) {
 
                             if (uri != null && uri.equals(mCurrentlyLoadingDocument)) {
                                 Logger.TIME(mDocumentStartTime, "ICEmobile loading page: " + uri);
                             }
 
                             mScriptEngine = mBrowserField.getScriptEngine();
                             mBrowserCookieManager.setCookie(uri, "com.icesoft.user-agent=HyperBrowser/1.0");
 
                             if (mLoadingScreen != null) {
                                 popScreen(mLoadingScreen);
                                 mLoadingScreen = null;
                             }
 
                             if (mScriptEngine != null) {
 
                                 invokeLater(new Runnable() {
                                     public void run() {
                                         try {
 
                                             Document document = mBrowserField.getDocument();
                                             mScriptElement = document.createElement("script");
                                             mScriptElement.setTextContent(mInitialScript);
                                             //append to head
                                             Element docElement = document.getDocumentElement();
                                             Node child = docElement.getFirstChild();
                                             while (child != null && (!child.getNodeName().equalsIgnoreCase("head"))) {
                                                 child = child.getNextSibling();
                                             }
                                             if (child != null) {
                                                 child.appendChild(mScriptElement);
                                             }
 
                                             // TODO: convert to single dispatcher instance
                                             mScriptEngine.addExtension("icefaces.shootPhoto",
                                                                        new WidgetCameraController(ICEmobileContainer.this));
                                             mScriptEngine.addExtension("icefaces.test",
                                                                        new ScriptableTest());
                                             mScriptEngine.addExtension("icefaces.ajaxUpload",
                                                                        new AjaxUpload(ICEmobileContainer.this));
                                             mScriptEngine.addExtension("icefaces.getResult",
                                                                        new ScriptResultReader(ICEmobileContainer.this));
                                             mScriptEngine.addExtension("icefaces.shootVideo",
                                                                        new VideoController(ICEmobileContainer.this));
                                             mScriptEngine.addExtension("icefaces.scan",
                                                                        new QRCodeScanner(ICEmobileContainer.this));
                                             mScriptEngine.addExtension("icefaces.logInContainer",
                                                                        new JavascriptDebugger());
                                             mScriptEngine.addExtension("icefaces.recordAudio",
                                                                        new AudioRecorderLauncher(ICEmobileContainer.this));
 
                                             Logger.DEBUG("ICEmobile - native script executed");
                                             invokeLater(mParkScriptRunner);
 
 
                                         } catch (Throwable t) {
                                             Logger.ERROR("ICEmobile - Error executing startup scripts: " + t + ", document: " + uri);
                                         }
                                     }
                                 });
                             } else {
                                 Logger.DEBUG("ICEmobile - NO SCRIPT ENGINE (ok at startup)");
                             }
 
                             mHistoryManager.addLocation(uri);
                             ((ApplicationScreen) mMainScreen).updateHistoryMenus();
 
                         }
                     } catch (Exception e) {
                         Logger.ERROR("Exception in DocumentLoad: " + e);
                     }
                 }
 
                 // public void documentAborted( BrowserField field, Document document ) { 
                 //					
                 // }
                 //// Only change the script engine when a document is loaded.
                 public void documentCreated(BrowserField field,
                                             ScriptEngine scriptEngine,
                                             Document document) {
                     mCurrentlyLoadingDocument = document.getBaseURI();
                     mDocumentStartTime = System.currentTimeMillis();
                 }
                 //	
 
                 public void documentError(BrowserField field,
                                           Document document) {
                     Logger.ERROR("ICEmobile - Error in document: " + document.getBaseURI());
                 }
 
                 public void documentUnloading(BrowserField field,
                                               Document document) {
                     Logger.DEBUG("Document unloading??? ");
                 }
                 //				
                 //  public void downloadProgress (BrowserField field, ContentReadEvent readEvent) {					
                 //      System.out.println("--------------------------Downloaded: " + readEvent.getItemsRead() + " of " + readEvent.getItemsToRead() );			
                 //				
                 //  }
                 //				 
                 //				
             });
 
 
             loadPage(mCurrentHome);
         } catch (Throwable e) {
             Logger.ERROR("Error loading initial page: " + e);
             Logger.DIALOG("ICEmobile - exception during init: " + e);
         }
     }
 
     private void setupBrowserGui() {
         // Perform BrowserField configuration
 
         BrowserFieldConfig bfc = new BrowserFieldConfig();
         bfc.setProperty(BrowserFieldConfig.ENABLE_COOKIES, Boolean.TRUE);
         bfc.setProperty(BrowserFieldConfig.ERROR_HANDLER, new FieldErrorHandler(mBrowserField));
         bfc.setProperty(BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE);
 
         mBrowserField = new BrowserField(bfc);
         mBrowserHistory = mBrowserField.getHistory();
         mBrowserController = mBrowserField.getController();
         AuthenticatingProtocolHandler browserAuthHandler =
                 new AuthenticatingProtocolHandler(mAuthenticationManager, mBrowserField);
         ((ProtocolController) mBrowserController).setNavigationRequestHandler("http", browserAuthHandler);
         ((ProtocolController) mBrowserController).setResourceRequestHandler("http", browserAuthHandler);
 
         mBrowserCookieManager = mBrowserField.getCookieManager();
         mMainScreen.add(mBrowserField);
     }
 
     /**
      * Construct the custom script extensions based on capabilities flags.
      */
     private void instantiateScriptExtensions() {
 
     }
 
     /**
      * Log a list of the device capabilities and define the File System capabilities
      * in the FileUtils class.
      */
     private void enumerateStorageLocations() {
         String root = null;
         Enumeration i = FileSystemRegistry.listRoots();
         while (i.hasMoreElements()) {
             root = (String) i.nextElement();
             Logger.DEBUG("File device: " + root);
         }
     }
 
 
     public HistoryManager getHistoryManager() {
         return mHistoryManager;
     }
 
     /**
      * Back function
      */
     public void back() {
         if (!
                     mBrowserField.back()) {
             Logger.DEBUG("ICEmobile - BrowserField indicates no page to go back to...");
         }
     }
 
 
     // ----------------   Asynchronous callback methods for javascript extensions to manipulate the DOM. 
 
     public void resetPushAgent() {
 
         mPushAgent.shutdown();
         mPushAgent = new PushAgent();
         Logger.DEBUG("ICEmobile - PushAgent reset");
     }
 
 
     /**
      * Post a request to the server on behalf of javascript extensions. The BrowserField used
      * to load the intitial request must be used for all subsequent requests in order to maintain
      * the correct cookie setup.
      */
     public InputConnection postRequest(String actionField, String request,
                                        HttpHeaders headers) throws Exception {
 
         BrowserFieldConnectionManager bfconman = mBrowserField.getConnectionManager();
         BrowserFieldRequest bfr = new BrowserFieldRequest(actionField, request.getBytes(), headers);
         return bfconman.makeRequest(bfr);
     }
 
     public String getCurrentURL() {
         return mBrowserField.getDocumentUrl();
     }
 
     /**
      * Get the quickURL. The quick URL is a persisted value used in the URL
      * navigation menu. If it hasn't been defined, use the current URL.
      */
     public String getQuickURL() {
         String u = mOptionsProperties.getQuickURL();
         if (u == null) {
             u = getCurrentURL();
         }
         return u;
     }
 
     /**
      * Set the QuickURL. This will persist the value in flash as well
      * as load the URL.
      *
      * @param url URL to visit
      */
     public void setQuickURL(String url) {
         if (url != null && url.trim().length() > 0) {
             loadPage(url);
             mOptionsProperties.setQuickURL(url);
             mOptionsProperties.save();
         }
     }
 
     /**
      * Set the state of the notification Icon in the application
      *
      * @param show true to show the icon.
      */
     public static void showNotificationIcon(boolean show) {
         mAppIndicator.setVisible(show);
     }
 
 
     /**
      * Insert a filename hidden field before a given id. It's necessary to do this in the event there
      * are more than one file inserting components on the page.
      *
      * @param id       The id of an element to insert the filename before
      * @param filename the name of the file in the local filesystem.
      */
     public void insertHiddenFilenameScript(final String id,
                                            final String filename) {
 
         if (filename != null && filename.length() > 0) {
 
             String updateScript = "ice.addHidden('" + id + "', '" + id + "', '" + filename + "', 'file' );";
             insertHiddenScript(updateScript);
         } else {
             Logger.ERROR("ICEmobile - Captured filename is invalid ");
         }
     }
 
     /**
      * @param id       The id of an element to insert the filename before
      * @param qrResult the scanned result
      */
     public void insertQRCodeScript(final String id, final String qrResult) {
 
         if (qrResult != null && qrResult.length() > 0) {
             String updateScript = "ice.addHidden('" + id + "', '" + id + "', ' " + qrResult + "', 'text' );";
             insertHiddenScript(updateScript);
             Logger.DEBUG("ICEmobile - QRCode text inserted");
 
         } else {
             Logger.ERROR("ICEmobile - Invalid qrCode scan result");
         }
     }
 
     /**
      * @param id       The id of an element to insert the filename before
      * @param qrResult the scanned result
      */
     public void insertHiddenScript(String script) {
 
         try {
             mScriptEngine.executeScript(script, null);
         } catch (Throwable e) {
             Logger.ERROR("ICEmobile - Error inserting field: " + e);
         }
     }
 
     /**
      * Handle the result of a POST in an AJAX context. The holder of the result
      *
      * @param resultKey The key into the result hash
      */
     public void processResult(String resultKey, ResultHolder holder) {
 
         mPendingResponses.put(resultKey, holder);
         String updateScript = "ice.handleResponse(icefaces.getResult('" + resultKey + "')," +
                                       ((mRealDevice) ? "false" : "true") + " ); ";
         try {
             if (mScriptEngine != null) {
                 mScriptEngine.executeScript(updateScript, null);
             } else {
                 Logger.ERROR("ICEmobile Null ScriptEngine handling Ajax Response?");
             }
 
         } catch (Throwable t) {
 
             if (t.getMessage().indexOf("DOM") == -1) {
                 Logger.ERROR("ICEmobile - Error handling AJAX response: " + t);
             } else {
                 Logger.ERROR("DOM exception still occurs");
             }
         }
     }
 
     public ResultHolder getPendingResponse(String responseKey) {
         ResultHolder value = (ResultHolder) mPendingResponses.get(responseKey);
         mPendingResponses.remove(responseKey);
         return value;
     }
 
 
     /**
      * call into javascript to define an image thumbnail
      *
      * @param id               Id of the image source to update.
      * @param base64ImageBytes
      */
     public void insertThumbnail(String id, String base64ImageBytes) {
 
         if (id != null && id.length() > 0) {
 
             String thumbId = id + "-thumb";
             String updateScript = "ice.setThumbnail('" + thumbId + "' , 'data:image/jpg;base64," +
                                           base64ImageBytes + "');";
             try {
                 mScriptEngine.executeScript(updateScript, null);
             } catch (Throwable e) {
                 Logger.ERROR("ICEmobile - Exception inserting thumbnail image: " + e);
             }
         } else {
             Logger.ERROR("ICEmobile - Insert thumbnail - invalid id: " + id);
         }
     }
 
     /**
      * Load an URL. Page will be loaded in the event thread
      *
      * @param url the page to load.
      */
     public void loadPage(final String url) {
 
         if (checkNetworkAvailability()) {
             navigateUsingFieldController(url);
 //        	navigateUsingField(url);
         } else {
             Logger.DIALOG("Network Unavailable");
         }
     }
 
     private void navigateUsingFieldController(String url) {
 
         try {
             BrowserFieldConnectionManager bfconman = mBrowserField.getConnectionManager();
             BrowserFieldRequest request = new BrowserFieldRequest(url);
             mBrowserField.requestContent(request);
 
         } catch (Exception e) {
             Logger.ERROR("ICEmobile - exception requesting content: " + e);
         }
     }
 
     private void navigateUsingField(String url) {
 
         try {
             mBrowserCookieManager.setCookie(url, "com.icesoft.user-agent=HyperBrowser/1.0");
             mBrowserField.requestContent(url);
         } catch (Exception e) {
             Logger.ERROR("ICEmobile - exception requesting content: " + e);
         }
     }
 
     /**
      * Suitable for the options page where we have exited the app and want
      * to reload when we rejoin.
      */
     public void reloadApplicationOnReentry() {
         mReloadOnActivate = true;
     }
 
     /**
      * Callback for options change. DO NOTHING LONG HERE.
      */
     public void optionsChanged() {
 
         mOptionsProperties = BlackberryOptionsProperties.fetch();
         mCurrentHome = mOptionsProperties.getHomeURL();
 
 //        Logger.DEBUG("optionsChanged - useEmail: " + mOptionsProperties.isUsingEmailNotification());
 
         String argument;
         if (mOptionsProperties.isUsingEmailNotification()) {
             argument = "('mail:" +
                                mOptionsProperties.getEmailNotification() + "');";
         } else {
             argument = "('bpns:" +
                                Integer.toHexString(DeviceInfo.getDeviceId()).toUpperCase()
                                + "');";
         }
 
         // Use either an email notification (if desired) or the 
         // RIM push version
        mParkScript = "ice.push.parkInactivePushIds" + argument;
 
         mPauseScript = "try { ice.push.connection.pauseConnection(); " +
                                "icefaces.logInContainer('ice.push.connection.pauseConnection success'); " +
                                " } catch (e) { icefaces.logInContainer('ice.js - Exception pausing Connection: ' + e); }";
 
         invokeLater(mParkScriptRunner);
     }
 
     /**
      * Suitable for the Menu system when we want to reload the page
      * immediately from the application
      */
     public void reloadHomePage() {
         loadPage(mOptionsProperties.getHomeURL());
     }
 
     /**
      * Reload the current page.
      */
     public void reloadCurrentPage() {
 //    	mBrowserField.refresh(); 
         mBrowserHistory.refresh();
     }
 
     public void clearAuthorizationCache() {
         mAuthenticationManager.clearAuthorizationCache();
     }
 
 
     /**
      * UIApplication activate override.
      */
     public void activate() {
 
         // method gets called several times which is not described in docs on activate();
         if (isForeground()) {
             // turn off the notification indicator if we're revisiting the app. 
             // However, it wont be defined if this is the first time entry. 
             if (mAppIndicator != null) {
                 showNotificationIcon(false);
             }
 
             if (mReloadOnActivate && checkNetworkAvailability()) {
                 loadPage(mCurrentHome);
                 mReloadOnActivate = false;
             }
 
             try {
                 if (mScriptEngine != null && mRealDevice) {
                     mScriptEngine.executeScript("if (ice.push) { ice.push.connection.resumeConnection(); icefaces.logInContainer('ice.push.connection.resumeConnection success'); }", null);
                 }
             } catch (Throwable t) {
                 Logger.ERROR("ICEmobile - resumeScript exception: " + t);
             }
         }
     }
 
     public void deactivate() {
 
         if (mPauseScript != null && mScriptEngine != null && mRealDevice) {
             try {
                 mScriptEngine.executeScript(mPauseScript, null);
 
             } catch (Throwable t) {
                 Logger.ERROR("ICEmobile - Exception pausing Connection: " + t);
             }
         }
     }
 
 
     // -------------------  Utility methods ---------------------------------
 
 
     /**
      * fetch the contents of a file as a resource
      *
      * @param name name of resource
      * @return Contents of file
      */
     public String readLocalFileSystem(Class clazz, String name) {
 
         InputStream rStream = null;
         String returnVal = null;
         try {
             rStream = clazz.getResourceAsStream(name);
 
             if (rStream != null) {
                 byte data[] = IOUtilities.streamToBytes(rStream);
                 returnVal = new String(data);
             }
         } catch (IOException ioe) {
             Logger.ERROR("Exception reading resource: " + ioe);
         } finally {
 
             try {
                 if (rStream != null) {
                     rStream.close();
                 }
             } catch (Exception e) {
             }
         }
         return returnVal;
     }
 
     /**
      * The eula has been accepted, persist the change.
      */
     public void acceptEula() {
         mOptionsProperties.setEulaViewed(true);
         mOptionsProperties.save();
     }
 
     /**
      * Check network Availability. Returns false if none of the available transports
      * show sufficient coverage to operate. Should be checked before attempting
      * to load pages, to prevent an endless black screen.
      */
     private boolean checkNetworkAvailability() {
 
         if (mRealDevice) {
             return (CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT) ||
                             CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_BIS_B) ||
                             CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_MDS));
         } else {
             return true;
         }
     }
 
     // -------------------- System event methods ----------------------------
 
     public void batteryGood() {
         Logger.DEBUG("BATTERY GOOD");
     }
 
     public void batteryLow() {
         Logger.DEBUG("BATTERY LOW");
     }
 
     public void batteryStatusChange(int arg0) {
         Logger.DEBUG("BATTERY STATUS: " + arg0);
     }
 
     public void powerOff() {
         Logger.DEBUG("POWER OFF");
     }
 
     public void testJavascript() {
         if (mScriptEngine != null) {
             try {
                 mScriptEngine.executeScript("ice.test();", null);
                 Logger.DEBUG("ice.test - run without exception");
             } catch (Exception e) {
                 Logger.ERROR("ice.test - exception testing namespace: " + e);
             }
         } else {
             Logger.DEBUG("ice.test - script engine is null!");
         }
     }
 }
 
