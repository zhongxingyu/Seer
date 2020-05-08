 // Copyright (c) 2012 The Chromium Authors. All rights reserved.
 // Copyright (c) 2012-2013, The Linux Foundation. All rights reserved.
 //
 // Use of this source code is governed by a BSD-style license that can be
 // found in the LICENSE file.
 
 package com.mogoweb.browser.web;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.chromium.base.CalledByNative;
 import org.chromium.chrome.browser.ChromeHttpAuthHandler;
 import org.chromium.chrome.browser.ChromeWebContentsDelegateAndroid;
 import org.chromium.chrome.browser.ContentViewUtil;
 import org.chromium.chrome.browser.TabBase;
 import org.chromium.content.browser.ContentView;
 import org.chromium.content.browser.LoadUrlParams;
 import org.chromium.content.browser.WebContentsObserverAndroid;
 import org.chromium.content.common.CleanupReference;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.text.TextUtils;
 
 import com.mogoweb.browser.HttpAuthenticationDialog;
 import com.mogoweb.browser.Intention;
 import com.mogoweb.browser.Tab;
 import com.mogoweb.browser.preferences.BrowserPreferences;
 import com.mogoweb.browser.utils.Logger;
 
 /**
  * The basic Java representation of a tab.  Contains and manages a {@link ContentView}.
  */
 public class WebTab extends TabBase implements Tab {
 
     // The following constant matches the one in
     // content/public/common/page_transition_types.h.
     // Add more if you need them.
     public static final int PAGE_TRANSITION_RELOAD = 8;
 
     private String mUrl = "";
     private final Context mContext;
     private Matrix bitmapMatrix;
     private boolean mLoaded;
     private boolean mUseDesktopUserAgent;
     private int mNativeWebContents;
     private final List<Tab.Listener> mListeners = new ArrayList<Tab.Listener>();
     private ClientDelegate mClientDelegate;
     private WebContentsObserverAndroid mWebContentsObserver;
 
     // http auth
     private HttpAuthenticationDialog mHttpAuthenticationDialog;
 
     //progress
     private boolean mbMainFrameStartedLoading = false;
     private int mProgress = 0;
 
     // Restored variables
     private byte[] mRestoredState;
     private String mRestoredTitle;
     private String mRestoredSnapshotFilename;
     private Bitmap mBitmap;
 
     boolean mIsTabSaved = false;
 
     private ChromeWebContentsDelegateAndroid mWebContentsDelegate;
     private ContentView mContentView;
     private WebSettings mSettings; // settings for web
     private int mNativeWebTab;
     private CleanupReference mCleanupReference;
 
     public interface ClientDelegate {
         boolean addNewContents(int nativeSourceWebContents, int nativeWebContents,
                 int disposition, Rect initialPosition, boolean userGesture);
     }
 
     public WebTab(Activity activity, Intention args) {
         super(WebApplicationGlue.getWindowAndroid());
 
         mContext = activity;
         mNativeWebContents = args.mNativeWebContents;
         mRestoredState = args.mState;
         mRestoredTitle = args.mTitle != null ? args.mTitle : "";
         mRestoredSnapshotFilename = args.mSnapshotFilename;
 
         init(mContext);
         setupContentsandLoadUrl();
     }
 
     public void setClientDelegate(ClientDelegate delegate) {
         mClientDelegate = delegate;
     }
 
     //Tab implementation
     @Override
     public void onActivityPause() {
         if (mLoaded)
             getContentView().onActivityPause();
     }
 
     @Override
     public void onActivityResume() {
         if (mLoaded)
             getContentView().onActivityResume();
     }
 
     @Override
     public void onShow() {
         if(!isNativeActive())
             restoreContentLayer();
 
         if (mLoaded)
             getContentView().onShow();
     }
 
     @Override
     public void onHide() {
         if (mLoaded)
             getContentView().onHide();
     }
 
     @Override
     public void loadUrl(String url) {
         Logger.debug("loadUrl " + url);
         android.util.Log.d("Pageload", "PageLoadStarted:startLoadingURL: " + url);
         mUrl = url;
 
         if (mLoaded) {
             loadUrlWithSanitization(url);
         }
     }
 
     @Override
     public void goBack() {
         if (mLoaded)
             getContentView().goBack();
     }
 
     @Override
     public void goForward() {
         if (mLoaded)
             getContentView().goForward();
     }
 
     @Override
     public boolean canGoBack() {
         return (mLoaded && getContentView().canGoBack());
     }
 
     @Override
     public boolean canGoForward() {
         return (mLoaded && getContentView().canGoForward());
     }
 
     /**
      * @return WebTab's serialized state. Return mRestoredState, if mNativeTab
      * has not yet been created through setupContentsandLoadUrl().
      */
     @Override
     public byte[] getState() {
         return (mNativeWebTab == 0 ? mRestoredState : nativeGetOpaqueState(mNativeWebTab));
     }
 
     @Override
     public Embodiment getEmbodiment() {
        return Embodiment.E_Web;
     }
 
     @Override
     public void stopLoading() {
         if (mLoaded)
             getContentView().stopLoading();
     }
 
     @Override
     public int getCurrentLoadProgress() {
         if (!mLoaded)
             return 0;
         return mProgress;
     }
 
     @Override
     public void reload() {
         if (mLoaded)
             getContentView().reload();
     }
 
     @Override
     public String getUrl() {
         return mUrl;
     }
 
     @Override
     public void setUrl(String url) {
         mUrl = url;
     }
 
     /**
      * @return page title of the selected navigation entry. Return mRestoredTitle,
      * if mContentView has not yet been created through setupContentsandLoadUrl().
      */
     @Override
     public String getTitle() {
         return (getContentView() == null ? mRestoredTitle : getContentView().getTitle());
     }
 
     @Override
     public boolean getUseDesktopUserAgent() {
         return mLoaded ? getContentView().getUseDesktopUserAgent() : mUseDesktopUserAgent;
     }
 
     @Override
     public void setUseDesktopUserAgent(boolean value) {
         if (!mLoaded) {
             mUseDesktopUserAgent = value;
         } else if (mUseDesktopUserAgent != value) {
             mUseDesktopUserAgent = value;
             // Set useDesktopUserAgent and reload the page
             if (mUseDesktopUserAgent != getContentView().getUseDesktopUserAgent())
                 getContentView().setUseDesktopUserAgent(mUseDesktopUserAgent, true);
         }
     }
 
     @Override
     public void addListener(Tab.Listener li) {
         mListeners.add(li);
     }
 
     @Override
     public void removeListener(Tab.Listener li) {
         mListeners.remove(li);
     }
 
     @Override
     public Bitmap getSnapshot(int width, int height) {
         FileInputStream fis;
         // Ensure we always return a bitmap
         Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
 
         try {
             if (getContentView() != null) {
                 // Get page snapshot
                 bitmap = getContentView().getBitmap(width, height);
                 // Remove snapshot file if it exists
                 if (mRestoredSnapshotFilename != null) {
                     File file = mContext.getFileStreamPath(mRestoredSnapshotFilename);
                     if (file.exists())
                         file.delete();
                     mRestoredSnapshotFilename = null;
                 }
             } else {
                 // Restore snapshot from file
                 if (mRestoredSnapshotFilename != null) {
                     fis = mContext.openFileInput(mRestoredSnapshotFilename);
                     if (fis != null) {
                         Bitmap tempBitmap = BitmapFactory.decodeStream(fis);
                         if (tempBitmap != null ) {
                             bitmap = Bitmap.createScaledBitmap(tempBitmap,
                                     width, height, false);
                         }
                         fis.close();
                     }
                 }
             }
             return bitmap;
         }
         catch (IOException e) {
             e.printStackTrace();
             Logger.error("Exception = " + e.toString());
             return bitmap;
         }
     }
 
     private void saveTabState() {
         mRestoredState = getState();
         mRestoredTitle = getTitle();
         int width, height;
         width =  mContentView.getWidth();
         height = mContentView.getHeight();
         mBitmap = getSnapshot(width, height);
     }
 
     public void killContentLayer() {
         Logger.debug("killContentLayer: Url"+ mUrl);
         if (isNativeActive()) {
             saveTabState();
             onHide();
             destroy();
             mIsTabSaved = true;
         }
     }
 
     public void restoreContentLayer() {
 
         if(!mIsTabSaved)
             return;
 
         Logger.debug("restoreContentLayer");
 
         init(mContext);
         setupContentsandLoadUrl();
     }
 
     public boolean isNativeActive() {
         return mLoaded;
     }
 
     /**
      * Should be called when the tab is no longer needed.  Once this is called this tab should not
      * be used.
      */
     @Override
     public void destroy() {
         destroyContentView();
         if (mNativeWebTab != 0) {
             mCleanupReference.cleanupNow();
             mNativeWebTab = 0;
         }
     }
 
     /**
      * @param context              The Context the view is running in.
      */
     private void init(Context context) {
         // Build the WebContents and the ContentView/ContentViewCore
         if (mNativeWebContents == 0)
             mNativeWebContents = ContentViewUtil.createNativeWebContents(false);
         mContentView = ContentView.newInstance(context, mNativeWebContents, getWindowAndroid(),
                 ContentView.PERSONALITY_CHROME);
         mNativeWebTab = nativeInit(mNativeWebContents, getWindowAndroid().getNativePointer());
 
         mSettings = new WebSettings(context, mNativeWebContents, true);
 
         // load websettings from BrowserPreferences
         BrowserPreferences prefs = BrowserPreferences.getInstance();
         mSettings.setJavaScriptEnabled(prefs.getJavaScriptEnabled());
         mSettings.setJavaScriptCanOpenWindowsAutomatically(prefs.getPopupsEnabled());
         mSettings.setUserAgentString(prefs.getUserAgentString());
 
         // Build the WebContentsDelegate
         mWebContentsDelegate = new TabBaseChromeWebContentsDelegateAndroid();
         nativeInitWebContentsDelegate(mNativeWebTab, mWebContentsDelegate);
 
         mWebContentsObserver = new WebContentsObserverAndroid(mContentView.getContentViewCore()) {
             @Override
             public void didFailLoad(boolean isProvisionalLoad, boolean isMainFrame, int errorCode,
                 String description, String failingUrl) {
                 for (Tab.Listener li : mListeners)
                     li.didFailLoad(isProvisionalLoad, isMainFrame, errorCode, description, failingUrl);
             }
 
             @Override
             public void didStartProvisionalLoadForFrame(long frameId,
                 long parentFrameId, boolean isMainFrame, String validatedUrl,
                 boolean isErrorPage, boolean isIframeSrcdoc) {
                 if (isMainFrame) {
                     mbMainFrameStartedLoading = true;
                     mProgress = 0;
                 }
                 // Notify the listeners
                 for (Tab.Listener li : mListeners)
                     li.onLoadStarted(isMainFrame);
             }
 
             @Override
             public void didFinishLoad(long frameId, String validatedUrl,
                     boolean isMainFrame) {
                 if (isMainFrame) {
                     mbMainFrameStartedLoading = false;
                     mProgress = 0;
                 }
                 // Notify the listeners
                 for (Tab.Listener li : mListeners)
                     li.onLoadStopped(isMainFrame);
             }
         };
 
         // To be called after everything is initialized.
         mCleanupReference = new CleanupReference(this,
                 new DestroyRunnable(mNativeWebTab));
     }
 
     private void setupContentsandLoadUrl() {
         Logger.info("setupContentsandLoadUrl");
         boolean bOpenedByEngine = (mNativeWebContents != 0);
 
         // Build the WebContents and the ContentView/ContentViewCore
         if (!bOpenedByEngine) {
             Logger.debug("creating webcontents");
         }
 
         Logger.debug("creating tab");
 
         // Restore Tab state
         if (mRestoredState != null) {
             if (nativeRestoreState(mNativeWebTab, mRestoredState)) {
                 // The onTitleUpdated callback normally happens when a page is
                 // loaded, but is optimized out in the restore state case because
                 // the title is already restored. See WebContentsImpl::UpdateTitleForEntry.
                 // So we call the callback explicitly here.
                 mWebContentsDelegate.onTitleUpdated();
             } else {
                 Logger.error("WebTab.setupContentsandLoadUrl: Unable to restore state");
             }
             mRestoredState = null;
         }
 
         // add the contentview to the tab
         getContentView().requestFocus();
 
         loadUrlWithSanitization(mUrl);
 
         mLoaded = true;
     }
 
     /**
      * Navigates this Tab's {@link ContentView} to a sanitized version of {@code url}.
      * @param url The potentially unsanitized URL to navigate to.
      */
     public void loadUrlWithSanitization(String url) {
         if (url == null || "".equals(url))
             return;
 
         // Sanitize the URL.
         url = sanitizeUrl(url);
 
         // Invalid URLs will just return empty.
         if (TextUtils.isEmpty(url)) return;
 
         if (TextUtils.equals(url, getContentView().getUrl())) {
             getContentView().reload();
         } else {
             Logger.debug("loading url " + url);
             LoadUrlParams params = new LoadUrlParams(url);
             params.setOverrideUserAgent(mUseDesktopUserAgent ?
                                             LoadUrlParams.UA_OVERRIDE_TRUE :
                                             LoadUrlParams.UA_OVERRIDE_FALSE);
             getContentView().loadUrl(params);
         }
     }
 
     // FIXME: need to use URLFixer in chrome/net
     private static String sanitizeUrl(String url) {
         if (url == null) return url;
         if (url.startsWith("www.") || url.indexOf(":") == -1) url = "http://" + url;
         return url;
     }
 
     @CalledByNative
     public void onReceivedHttpAuthRequest(final ChromeHttpAuthHandler httpAuthHandler, String host, String realm) {
         mHttpAuthenticationDialog = new HttpAuthenticationDialog(mContext, host, realm);
         mHttpAuthenticationDialog.setOkListener(new HttpAuthenticationDialog.OkListener() {
             public void onOk(String host, String realm, String username, String password) {
                 httpAuthHandler.proceed(username, password);
                 mHttpAuthenticationDialog = null;
             }
         });
         mHttpAuthenticationDialog.setCancelListener(new HttpAuthenticationDialog.CancelListener() {
             public void onCancel() {
                 httpAuthHandler.cancel();
                 mHttpAuthenticationDialog = null;
             }
         });
         mHttpAuthenticationDialog.show();
     }
 
 //    @CalledByNative
 //    public void showContextMenu(String linkUrl, int mediaType) {
 //        Logger.debug("showContextMenu URL : " + linkUrl + " mediaType : " + mediaType );
 //        for (Tab.Listener li : mListeners)
 //            li.showContextMenu(linkUrl);
 //    }
 
     public void handleExternalProtocol(String url) {
         Logger.debug("handleExternalProtocol URL : " + url);
         if (url != null) {
             Intent intent = null;
             if (url.startsWith("tel:") || url.startsWith("market:")) {
                 intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                 mContext.startActivity(intent);
             }
         }
     }
 
     // Method added for Instrumentation Testing
     @Override
     public ContentView getContentView() {
         return mContentView;
     }
 
     private void destroyContentView() {
         if (mContentView == null) return;
 
         mContentView.destroy();
         mContentView = null;
         mNativeWebContents = 0;
         mLoaded = false;
     }
 
     // Can be called from any thread.
     public WebSettings getSettings() {
         return mSettings;
     }
 
     @Override
     public String getSnapshotFilename() {
         return mRestoredSnapshotFilename;
     }
 
     private static final class DestroyRunnable implements Runnable {
         private final int mNativeWebTab;
         private DestroyRunnable(int nativeWebTab) {
             mNativeWebTab = nativeWebTab;
         }
         @Override
         public void run() {
             nativeDestroy(mNativeWebTab);
         }
     }
 
     private class TabBaseChromeWebContentsDelegateAndroid
             extends ChromeWebContentsDelegateAndroid {
         @Override
         public void onLoadProgressChanged(int progress) {
             if (mbMainFrameStartedLoading) {
                 Logger.debug("onLoadProgressChanged progress :" + progress);
                 mProgress = progress;
                 // Send progress only for main frame
                 for (Tab.Listener li : mListeners)
                     li.onLoadProgressChanged(progress);
             }
         }
 
         @Override
         public void onUpdateUrl(String url) {
             setUrl(url);
 
             for (Tab.Listener li : mListeners)
                 li.onUpdateUrl(url);
         }
 
         @Override
         public void onLoadStarted() {
 //            mIsLoading = true;
         }
 
         @Override
         public void onLoadStopped() {
 //            mIsLoading = false;
         }
 
         @Override
         public boolean addNewContents(int nativeSourceWebContents, int nativeWebContents,
                 int disposition, Rect initialPosition, boolean userGesture) {
             if (mClientDelegate != null) {
                 return mClientDelegate.addNewContents(nativeSourceWebContents, nativeWebContents,
                         disposition, null, userGesture);
             }
             return false;
         }
     }
 
     private native int nativeInit(int webContentsPtr, int windowAndroidPtr);
     private static native void nativeDestroy(int nativeWebTab);
     private native void nativeInitWebContentsDelegate(int nativeWebTab,
             ChromeWebContentsDelegateAndroid chromeWebContentsDelegateAndroid);
     private native String nativeFixupUrl(int nativeWebTab, String url);
 
     // Returns null if save state fails.
     private native byte[] nativeGetOpaqueState(int nativeWebTab);
 
     // Returns false if restore state fails.
     private native boolean nativeRestoreState(int nativeWebTab, byte[] state);
 }
