 /* 
  * Copyright 2013 Wouter Pinnoo
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package eu.pinnoo.garbagecalendar.ui;
 
 import eu.pinnoo.garbagecalendar.ui.preferences.PreferenceActivity;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import eu.pinnoo.garbagecalendar.R;
 import eu.pinnoo.garbagecalendar.data.AreaType;
 import eu.pinnoo.garbagecalendar.data.Collection;
 import eu.pinnoo.garbagecalendar.data.CollectionsData;
 import eu.pinnoo.garbagecalendar.data.LocalConstants;
 import eu.pinnoo.garbagecalendar.data.Type;
 import eu.pinnoo.garbagecalendar.data.UserData;
 import eu.pinnoo.garbagecalendar.data.caches.AddressCache;
 import eu.pinnoo.garbagecalendar.data.caches.CollectionCache;
 import eu.pinnoo.garbagecalendar.data.caches.UserAddressCache;
 import eu.pinnoo.garbagecalendar.ui.widget.WidgetProvider;
 import eu.pinnoo.garbagecalendar.util.Network;
 import eu.pinnoo.garbagecalendar.util.parsers.CalendarParser;
 import eu.pinnoo.garbagecalendar.util.tasks.CacheTask;
 import eu.pinnoo.garbagecalendar.util.tasks.ParserTask;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
 
 /**
  *
  * @author Wouter Pinnoo <pinnoo.wouter@gmail.com>
  */
 public class CollectionListActivity extends AbstractSherlockActivity implements PullToRefreshAttacher.OnRefreshListener {
 
     private boolean loading = false;
     private PullToRefreshAttacher attacher;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         getSupportActionBar().setDisplayHomeAsUpEnabled(false);
         getSupportActionBar().setHomeButtonEnabled(false);
 
         AddressCache.initialize(this);
         CollectionCache.initialize(this);
         UserAddressCache.initialize(this);
 
         clearCachedIfRequired();
 
         attacher = PullToRefreshAttacher.get(this);
         PullToRefreshLayout ptrLayout = (PullToRefreshLayout) findViewById(R.id.main_table_scrollview);
         ptrLayout.setPullToRefreshAttacher(attacher, (PullToRefreshAttacher.OnRefreshListener) this);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (!loading) {
             if (getSharedPreferences("PREFERENCE", Activity.MODE_PRIVATE).getBoolean(LocalConstants.CacheName.COL_REFRESH_NEEDED.toString(), true)
                     || !CollectionsData.getInstance().isSet()) {
                 initializeCacheAndLoadData();
             } else {
                 createGUI();
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.col_list_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     public void initializeCacheAndLoadData() {
         new CacheTask(this, getString(R.string.loadingCalendar)) {
             @Override
             protected void onPreExecute() {
                 super.onPreExecute();
                 loading = true;
             }
 
             @Override
             protected void onPostExecute(Integer[] result) {
                 super.onPostExecute(result);
                 loading = false;
                 checkAddress();
             }
         }.execute(CollectionsData.getInstance(), UserData.getInstance());
     }
 
     public void checkAddress() {
         if (UserData.getInstance().isSet()) {
             loadCollections(UserData.getInstance().isChanged(), false);
         } else {
             loading = true;
             new AlertDialog.Builder(this)
                     .setTitle(getString(R.string.yourLocation))
                     .setMessage(getString(R.string.setAddress))
                     .setCancelable(false)
                     .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     dialog.dismiss();
                     loading = false;
                     Intent i = new Intent(getBaseContext(), PreferenceActivity.class);
                     startActivity(i);
                 }
             })
                     .setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     loading = false;
                     finish();
                 }
             })
                     .create().show();
         }
     }
 
     public void loadCollections(boolean force, boolean isPullToRefresh) {
         if (!force && CollectionsData.getInstance().isSet()) {
             createGUI();
         } else {
             if (!UserData.getInstance().isSet()) {
                 return;
             }
             if (Network.networkAvailable(this)) {
                 new ParserTask(this, getString(R.string.loadingCalendar), !isPullToRefresh) {
                     @Override
                     protected void onPreExecute() {
                         super.onPreExecute();
                         loading = true;
                     }
 
                     @Override
                     protected void onPostExecute(Integer[] result) {
                         super.onPostExecute(result);
                         loading = false;
                         attacher.setRefreshComplete();
                         UserData.getInstance().changeCommitted();
                         createGUI();
                     }
                 }.execute(new CalendarParser());
             } else {
                 loading = true;
                 if (isPullToRefresh) {
                     loading = false;
                     attacher.setRefreshComplete();
                     Toast.makeText(getApplicationContext(), getString(R.string.needConnection), Toast.LENGTH_SHORT).show();
                 } else {
                     new AlertDialog.Builder(this)
                             .setTitle(getString(R.string.noInternetConnection))
                             .setMessage(getString(R.string.needConnection))
                             .setCancelable(false)
                             .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                             loading = false;
                             attacher.setRefreshComplete();
                             finish();
                         }
                     })
                             .create().show();
                 }
             }
         }
     }
 
     private void createGUI() {
         Log.d(LocalConstants.LOG, "Everything done, creating UI!");
         TableLayout table = (TableLayout) findViewById(R.id.main_table);
         table.removeViews(0, table.getChildCount());
 
         List<Collection> collections = CollectionsData.getInstance().getCollections();
         Iterator<Collection> it = collections.iterator();
         int i = 0;
         while (it.hasNext()) {
             Collection col = it.next();
             Calendar lastDayToBeShown = Calendar.getInstance();
             lastDayToBeShown.add(Calendar.DATE, -1);
             if (col.getDate().before(lastDayToBeShown.getTime())) {
                 continue;
             }
 
             addTableRow(col, i);
             i++;
         }
         updateAllWidgets();
     }
 
     private String beautifyDate(Date date) {
         Date today = Calendar.getInstance().getTime();
         int daysBetween = (int) ((date.getTime() - today.getTime()) / (24 * 60 * 60 * 1000));
 
         if (daysBetween < 1) {
             return LocalConstants.DateFormatType.MAIN_TABLE.getDateFormatter(this).format(date)
                     + " ("
                     + getString(R.string.today)
                     + ")";
         } else if (daysBetween == 1) {
             return LocalConstants.DateFormatType.MAIN_TABLE.getDateFormatter(this).format(date)
                     + " ("
                     + getString(R.string.tomorrow)
                     + ")";
         } else if (daysBetween < 6) {
             return LocalConstants.DateFormatType.MAIN_TABLE.getDateFormatter(this).format(date)
                     + " ("
                     + getString(R.string.thisweek)
                     + " "
                     + LocalConstants.DateFormatType.WEEKDAY.getDateFormatter(this).format(date)
                     + ")";
         } else if (daysBetween < 13) {
             return LocalConstants.DateFormatType.MAIN_TABLE.getDateFormatter(this).format(date)
                     + " ("
                     + getString(R.string.nextweek)
                     + " "
                     + LocalConstants.DateFormatType.WEEKDAY.getDateFormatter(this).format(date)
                     + ")";
         } else {
             return LocalConstants.DateFormatType.MAIN_TABLE.getDateFormatter(this).format(date);
         }
     }
 
     private void addTableRow(Collection col, int rowNumber) {
         String date = beautifyDate(col.getDate());
        int backgroundColor = rowNumber % 2 == 0 ? getResources().getColor(R.color.table_even_row) : getResources().getColor(R.color.table_even_row);
 
         LayoutInflater inflater = getLayoutInflater();
         TableLayout tl = (TableLayout) findViewById(R.id.main_table);
         TableRow tr = (TableRow) inflater.inflate(R.layout.main_table_row, tl, false);
 
         TextView labelDate = (TextView) tr.findViewById(R.id.main_row_date);
         labelDate.setText(date);
         labelDate.setPadding(5, 5, 5, 5);
         labelDate.setTextColor(Color.BLACK);
 
         tr.setBackgroundColor(backgroundColor);
         tr.setOnClickListener(new TableRowListener(col));
 
         AreaType currentAreaType = UserData.getInstance().getAddress().getSector().getType();
 
         boolean hasType = col.hasType(Type.REST);
         TextView labelRest = (TextView) tr.findViewById(R.id.main_row_rest);
         labelRest.setText(hasType ? Type.REST.shortStrValue(this) : "");
         labelRest.setPadding(1, 5, 5, 5);
         labelRest.setBackgroundColor(hasType ? Type.REST.getColor(this, currentAreaType) : backgroundColor);
 
         hasType = col.hasType(Type.GFT);
         TextView labelGFT = (TextView) tr.findViewById(R.id.main_row_gft);
         labelGFT.setText(hasType ? Type.GFT.shortStrValue(this) : "");
         labelGFT.setPadding(1, 5, 5, 5);
         labelGFT.setBackgroundColor(hasType ? Type.GFT.getColor(this, currentAreaType) : backgroundColor);
 
         hasType = col.hasType(Type.PMD);
         TextView labelPMD = (TextView) tr.findViewById(R.id.main_row_pmd);
         labelPMD.setText(hasType ? Type.PMD.shortStrValue(this) : "");
         labelPMD.setPadding(1, 5, 5, 5);
         labelPMD.setBackgroundColor(hasType ? Type.PMD.getColor(this, currentAreaType) : backgroundColor);
 
         hasType = col.hasType(Type.PK);
         TextView labelPK = (TextView) tr.findViewById(R.id.main_row_pk);
         labelPK.setText(hasType ? Type.PK.shortStrValue(this) : "");
         labelPK.setPadding(1, 5, 5, 5);
         labelPK.setBackgroundColor(hasType ? Type.PK.getColor(this, currentAreaType) : backgroundColor);
 
         hasType = col.hasType(Type.GLAS);
         TextView labelGlas = (TextView) tr.findViewById(R.id.main_row_glas);
         labelGlas.setText(hasType ? Type.GLAS.shortStrValue(this) : "");
         labelGlas.setPadding(1, 5, 5, 5);
         labelGlas.setBackgroundColor(hasType ? Type.GLAS.getColor(this, currentAreaType) : backgroundColor);
 
         tl.addView(tr);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.preferences:
                 Intent intent = new Intent();
                 intent.setClass(this, PreferenceActivity.class);
                 startActivityForResult(intent, 0);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onRefreshStarted(View view) {
         loadCollections(true, true);
     }
 
     private void updateAllWidgets() {
         AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
         int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
         if (appWidgetIds.length > 0) {
             new WidgetProvider().onUpdate(this, appWidgetManager, appWidgetIds);
         }
     }
 }
