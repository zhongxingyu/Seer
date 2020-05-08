 /**
  * Copyright 2013 MicMun
  *
  * This program is free software: you can redistribute it and/or modify it under 
  * the terms of the GNU >General Public License as published by the 
  * Free Software Foundation, either version 3 of the License, or >
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; >without even the implied warranty of MERCHANTABILITY 
  * or FITNESS FOR A PARTICULAR PURPOSE. >See the GNU General Public License 
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along with 
  * this program. If not, see >http://www.gnu.org/licenses/.
  */
 package de.micmun.android.miwotreff.utils;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.MenuItem;
 
 import com.devspark.appmsg.AppMsg;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import de.micmun.android.miwotreff.MainActivity;
 import de.micmun.android.miwotreff.R;
 
 /**
  * Loads the program from the website and save it in the database.
  *
  * @author MicMun
  * @version 1.0, 13.01.2013
  */
 public class ProgramLoader extends AsyncTask<Void, Void, Integer> {
    private final String TAG = "MiWoTreff.ProgramLoader";
    private final Context mCtx;
    Drawable btnRefresStaticDrawable = null;
    private ArrayList<LoaderListener> listener = new ArrayList<LoaderListener>();
    private ConnectivityManager mConManager;
    private MenuItem btnRefresh = null;
    private int counter;
 
    public ProgramLoader(Context ctx, MenuItem mi) {
       super();
       mCtx = ctx;
       btnRefresh = mi;
       btnRefresStaticDrawable = btnRefresh.getIcon();
       mConManager = (ConnectivityManager) ctx.getSystemService(Context
             .CONNECTIVITY_SERVICE);
    }
 
    /**
     * Returns <code>true</code>, if you are connected to the internet.
     *
     * @return <code>true</code>, if connected to the internet.
     */
    private boolean isOnline() {
       boolean ret = false;
       NetworkInfo ni = mConManager.getActiveNetworkInfo();
 
       if (ni != null && ni.isConnected() && !ni.isRoaming())
          ret = true;
 
       return ret;
    }
 
    /**
     * @see android.os.AsyncTask#doInBackground(Object[])
     */
    @Override
    protected Integer doInBackground(Void... params) {
       publishProgress();
 
       if (!isOnline()) {
          Log.e(TAG, "No Internet connection!");
          AppMsg.makeText((MainActivity) mCtx, R.string.error_pl_noconnect, AppMsg.STYLE_ALERT).show();
          return 1;
       } else {
          HtmlParser parser = new HtmlParser();
          String table = parser
                .getHtmlFromUrl("http://www.gemeinschaft-muenchen.de/index.php?id=7&no_cache=1");
          if (table == null) {
             Log.e(TAG, "Can't fetch program!");
             AppMsg.makeText((MainActivity) mCtx, R.string.error_pl_fetch, AppMsg.STYLE_ALERT).show();
             return 1;
          } else {
             ArrayList<HashMap<String, Object>> prog = parser.getProg(table);
             if (prog == null) {
                Log.e(TAG, "No data!");
                AppMsg.makeText((MainActivity) mCtx, R.string.error_pl_nodata,
                      AppMsg.STYLE_ALERT).show();
                return 1;
             } else {
                counter = 0;
 
                for (HashMap<String, Object> m : prog) {
                   long date = ((Date) m.get(DBConstants.KEY_DATUM)).getTime();
                   String topic = (String) m.get(DBConstants.KEY_THEMA);
                   String person = (String) m.get(DBConstants.KEY_PERSON);
                   String[] selArgs = {String.valueOf(date)};
 
                   // Prepare values for insert or update
                   ContentValues values = new ContentValues();
                   values.put(DBConstants.KEY_DATUM, date);
                   values.put(DBConstants.KEY_THEMA, topic);
                   values.put(DBConstants.KEY_PERSON, person);
                   values.put(DBConstants.KEY_EDIT, 0);
 
                   // Query, if date exists
                   Uri uri = Uri.withAppendedPath(DBConstants
                         .TABLE_CONTENT_URI, DBConstants.DATE_QUERY);
                   Cursor c = mCtx.getContentResolver().query(uri, null, null,
                         selArgs, null);
 
                   if (c == null || c.getCount() <= 0) { // if not exists
                      // Insert
                      mCtx.getContentResolver()
                            .insert(DBConstants.TABLE_CONTENT_URI, values);
                      counter++;
                   } else { // exists
                      c.moveToFirst();
                      int edit = c.getInt(c.getColumnIndex(DBConstants
                            .KEY_EDIT));
                      int id = c.getInt(c.getColumnIndex(DBConstants._ID));
 
                      if (edit == 0) { // if not edited yet
                         // Update
                         uri = Uri.withAppendedPath(DBConstants
                               .TABLE_CONTENT_URI, String.valueOf(id));
                         mCtx.getContentResolver().update(uri, values,
                               null, null);
                      }
                   }
                  c.close();
                }
             }
          }
       }
       return 0;
    }
 
    /**
     * @see AsyncTask#onProgressUpdate(Object[])
     */
    @Override
    protected void onProgressUpdate(Void... progress) {
       btnRefresh.setIcon((mCtx.getResources()
             .getDrawable(R.drawable.ic_action_refresh_anim)));
       btnRefresh.setEnabled(false);
       AnimationDrawable frameAnimation = (AnimationDrawable) btnRefresh
             .getIcon();
       if (frameAnimation != null)
          frameAnimation.start();
    }
 
    /**
     * @see android.os.AsyncTask#onPostExecute
     */
    @Override
    protected void onPostExecute(Integer result) {
       btnRefresh.setIcon(btnRefresStaticDrawable);
       btnRefresh.setEnabled(true);
       Log.d(TAG, "result = " + result);
 
       if (result == 0) {
          notifyLoaderListener();
       }
    }
 
    /**
     * Adds a LoaderListener to the list of listener.
     *
     * @param l {@link LoaderListener} to add.
     */
    public void addLoaderListener(LoaderListener l) {
       listener.add(l);
    }
 
    /**
     * Removes a LoaderListener from the list of listener.
     *
     * @param l {@link LoaderListener} to remove.
     */
    public void removeLoaderListener(LoaderListener l) {
       listener.remove(l);
    }
 
    /**
     * Notifies all listener.
     */
    protected void notifyLoaderListener() {
       for (LoaderListener l : listener) {
          Log.d(TAG, l.toString());
          l.update(counter);
       }
    }
 }
