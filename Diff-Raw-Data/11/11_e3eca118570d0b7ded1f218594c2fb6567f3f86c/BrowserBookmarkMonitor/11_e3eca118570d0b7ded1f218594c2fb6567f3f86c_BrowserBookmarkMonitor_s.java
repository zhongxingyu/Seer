 package com.parent.management.monitor;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.provider.Browser;
 import android.util.Log;
 
 import com.parent.management.ManagementApplication;
 import com.parent.management.db.ManagementProvider;
 
 public class BrowserBookmarkMonitor extends Monitor {
     private static final String TAG = ManagementApplication.getApplicationTag() + "." +
             BrowserBookmarkMonitor.class.getSimpleName();
 
     public BrowserBookmarkMonitor(Context context) {
         super(context);
     }
 
     @Override
     public void startMonitoring() {
         this.monitorStatus = true;
     }
 
     @Override
     public void stopMonitoring() {
         this.monitorStatus = false;
     }
 
     @Override
     public JSONArray extractDataForSend() {
         String[] browserBookmarkProj = new String[] {
                 Browser.BookmarkColumns.TITLE,
                 Browser.BookmarkColumns.URL,
                 Browser.BookmarkColumns.VISITS,
                 Browser.BookmarkColumns.DATE };
         String browserBookmarkSel = Browser.BookmarkColumns.BOOKMARK + " = 1";
         String orderBy = Browser.BookmarkColumns.VISITS + " DESC"; 
         Cursor browserBookmarkCur = ManagementApplication.getContext().getContentResolver().query(
                 Browser.BOOKMARKS_URI,
                 browserBookmarkProj, browserBookmarkSel, null, orderBy);
 
         if (browserBookmarkCur == null) {
             Log.v(TAG, "open browserHistory native failed");
             return null;
         }
         
         JSONArray data = new JSONArray();
         
         try {
             if (browserBookmarkCur.moveToFirst() && browserBookmarkCur.getCount() > 0) {
                 while (browserBookmarkCur.isAfterLast() == false) {
                     String url = browserBookmarkCur.getString(
                             browserBookmarkCur.getColumnIndex(Browser.BookmarkColumns.URL));
                     String title = browserBookmarkCur.getString(
                             browserBookmarkCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                     int visit_count = browserBookmarkCur.getInt(
                             browserBookmarkCur.getColumnIndex(Browser.BookmarkColumns.VISITS));
                     long last_visit = browserBookmarkCur.getLong(
                             browserBookmarkCur.getColumnIndex(Browser.BookmarkColumns.DATE));
                     JSONObject raw = new JSONObject();
                     raw.put(ManagementProvider.BrowserBookmark.URL, url);
                     raw.put(ManagementProvider.BrowserBookmark.TITLE, title);
                     raw.put(ManagementProvider.BrowserBookmark.VISIT_COUNT, visit_count);
                     raw.put(ManagementProvider.BrowserBookmark.LAST_VISIT, last_visit);
 
                     data.put(raw);
                     browserBookmarkCur.moveToNext();
                 }
             }
         } catch (JSONException e) {
             Log.e(TAG, "Invalid json parameters: " + e.getMessage());
         }
         
         if (null != browserBookmarkCur) {
             browserBookmarkCur.close();
         }
 
         return data;
     }
 
     @Override
     public void updateStatusAfterSend(JSONArray failedList) {
     	// do nothing 
     }
 
 }
