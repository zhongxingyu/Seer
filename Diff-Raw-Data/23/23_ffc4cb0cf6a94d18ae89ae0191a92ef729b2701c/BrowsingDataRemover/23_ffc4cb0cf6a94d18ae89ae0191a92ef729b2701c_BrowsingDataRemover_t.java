 package com.mogoweb.browser;
 
 import org.chromium.base.CalledByNative;
 
 import android.app.ProgressDialog;
 import android.content.Context;
import android.os.Handler;
import android.os.Message;
 
 import com.mogoweb.browser.utils.Logger;
 
 public class BrowsingDataRemover {
 
     private static final int CLEAR_HISTORY = 0;
     private static final int CLEAR_CACHE = 1;
     private static final int CLEAR_COOKIES_AND_SITE_DATA = 2;
     private static final int CLEAR_PASSWORDS = 3;
     private static final int CLEAR_FORM_DATA = 4;
 
     // mask copied from browsing_data_remover.h
     private static final int REMOVE_APPCACHE = 1 << 0;
     private static final int REMOVE_CACHE = 1 << 1;
     private static final int REMOVE_COOKIES = 1 << 2;
     private static final int REMOVE_DOWNLOADS = 1 << 3;
     private static final int REMOVE_FILE_SYSTEMS = 1 << 4;
     private static final int REMOVE_FORM_DATA = 1 << 5;
     // In addition to visits, REMOVE_HISTORY removes keywords and last session.
     private static final int REMOVE_HISTORY = 1 << 6;
     private static final int REMOVE_INDEXEDDB = 1 << 7;
     private static final int REMOVE_LOCAL_STORAGE = 1 << 8;
     private static final int REMOVE_PLUGIN_DATA = 1 << 9;
     private static final int REMOVE_PASSWORDS = 1 << 10;
     private static final int REMOVE_WEBSQL = 1 << 11;
     private static final int REMOVE_SERVER_BOUND_CERTS = 1 << 12;
     private static final int REMOVE_CONTENT_LICENSES = 1 << 13;
     private static final int REMOVE_SHADER_CACHE = 1 << 14;
     // The following flag is used only in tests. In normal usage, hosted app
     // data is controlled by the REMOVE_COOKIES flag, applied to the
     // protected-web origin.
     private static final int REMOVE_HOSTED_APP_DATA_TESTONLY = 1 << 31;
 
     // "Site data" includes cookies, appcache, file systems, indexedDBs, local
     // storage, webSQL, shader, and plugin data.
     private static final int REMOVE_SITE_DATA = REMOVE_APPCACHE | REMOVE_COOKIES | REMOVE_FILE_SYSTEMS |
                        REMOVE_INDEXEDDB | REMOVE_LOCAL_STORAGE |
                        REMOVE_PLUGIN_DATA | REMOVE_WEBSQL |
                        REMOVE_SERVER_BOUND_CERTS;
     // "cached data" includes the http cache and the shader cache.
     private static final int REMOVE_CACHED_DATA = REMOVE_CACHE | REMOVE_SHADER_CACHE;
 
     private Context mContext;
     private ProgressDialog mWaitingDialog = null;
 
     public static BrowsingDataRemover getInstance(Context context) {
        return new BrowsingDataRemover(context);
     }
 
     private BrowsingDataRemover(Context context) {
         mContext = context;
     }
 
     public void clearData(boolean[] selected) {
         mWaitingDialog = ProgressDialog.show(mContext,
                 mContext.getString(R.string.clear_browsing_data_progress_title),
                 mContext.getString(R.string.clear_browsing_data_progress_message));
 
         int mask = 0;
 
         for (int i = 0; i < selected.length; i++) {
             switch (i) {
             case CLEAR_HISTORY:
                 mask |= REMOVE_HISTORY;
                 break;
             case CLEAR_CACHE:
                 mask |= REMOVE_CACHE;
                 break;
             case CLEAR_COOKIES_AND_SITE_DATA:
                 mask |= REMOVE_SITE_DATA;
                 break;
             case CLEAR_PASSWORDS:
                 mask |= REMOVE_PASSWORDS;
                 break;
             case CLEAR_FORM_DATA:
                 mask |= REMOVE_FORM_DATA;
                 break;
             default:
                 Logger.warn("unkown clear data type: " + i);
                 break;
             }
         }
 
         nativeClearData(mask);
     }
 
     @CalledByNative
     public void onBrowsingDataRemoverDone() {
        mHandler.sendEmptyMessage(0);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
             mWaitingDialog.dismiss();
         }
    };
 
     private native void nativeClearData(int removeDataMask);
 }
