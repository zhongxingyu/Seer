 package org.williamjoy.gexpense;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Set;
 
 import org.williamjoy.gexpense.model.CalendarInstanceData;
 import org.williamjoy.gexpense.util.DateHelper;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Events;
 import android.provider.CalendarContract.Instances;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 
 public class ExpenseActivity extends Activity {
 
     private long calendar_id = ExpenseConstants.CALENDAR_ID_NOT_SET;
     private LinkedList<CalendarInstanceData> instanceList = new java.util.LinkedList<CalendarInstanceData>();
 
     private CalendarInstanceAdapter mCalendarInstanceAdapter;
     private String mCurrencyUnit = "";
 
     private void loadCalendarEvents() {
         SharedPreferences sharedPrefences = PreferenceManager
                 .getDefaultSharedPreferences(this);
         int history_month = sharedPrefences.getInt("history_month", 2);
         mCurrencyUnit = sharedPrefences.getString("unit", "$");
         Set<String> titles = new LinkedHashSet<String>();
         Set<String> locations = new LinkedHashSet<String>();
        if (mCalendarInstanceAdapter != null) {
            mCalendarInstanceAdapter.setCurrencyUnit(mCurrencyUnit);
        }
         instanceList.clear();
         // Last Month to Next Month
         Calendar endTime = Calendar.getInstance();
         Calendar beginTime = (Calendar) endTime.clone();
         endTime.add(Calendar.MONTH, +1);
         beginTime.add(Calendar.MONTH, -history_month);
         long startMillis = beginTime.getTimeInMillis();
         long endMillis = endTime.getTimeInMillis();
 
         Cursor cur = null;
         ContentResolver cr = getContentResolver();
 
         // Construct the query with the desired date range.
         Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
         ContentUris.appendId(builder, startMillis);
         ContentUris.appendId(builder, endMillis);
 
         // Submit the query
         cur = cr.query(builder.build(), INSTANCE_PROJECTION,
                 Instances.CALENDAR_ID + " = ?",
                 new String[] { "" + calendar_id }, Instances.BEGIN + " ASC");
 
         SimpleDateFormat formatter = DateHelper.getDateFormat();
 
         while (cur.moveToNext()) {
             String title = null;
             CalendarInstanceData iCal = new CalendarInstanceData();
 
             long beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
             long dtstart = cur.getLong(5);
             long dtend = cur.getLong(6);
             String description = cur.getString(8);
             title = cur.getString(PROJECTION_TITLE_INDEX);
             String location = cur.getString(PROJECTION_EVENT_LOCATION_INDEX)
                     .trim();
 
             Calendar calendar = Calendar.getInstance();
             calendar.setTimeInMillis(beginVal);
             String startDate = formatter.format(calendar.getTime());
             String s[] = title.split("\\|");
             String what = s[0].trim();
             if (s.length > 1) {
                 try {
                     double m = Double.parseDouble(s[1]);
                     iCal.setMoney(m);
                 } catch (Exception e) {
                     Log.w(DEBUG_TAG, "Money Parse Error:" + title + " @"
                             + startDate);
                 }
             }
             titles.add(what);
             if (location.length() > 0)
                 locations.add(location);
             iCal.set_ID(cur.getLong(PROJECTION_EVENT_ID_INDEX));
             iCal.setTitle(what);
             iCal.setDescription(description);
             iCal.setStartDate(startDate);
             iCal.setLocation(location);
             iCal.setDtstart(dtstart);
             iCal.setDtend(dtend);
             iCal.setTimeZone(cur.getString(7));
             instanceList.add(iCal);
         }
         Editor editor = sharedPrefences.edit();
         editor.putStringSet("what", titles);
         editor.putStringSet("locations", locations);
         editor.commit();
     }
 
     public static final String[] INSTANCE_PROJECTION = new String[] {
             Instances.EVENT_ID, // 0
             Instances.BEGIN, // 1
             Instances.TITLE, // 2,
             Instances.CALENDAR_ID,// 3
             Instances.EVENT_LOCATION, // 4
             Instances.DTSTART, // 5
             Instances.DTEND, // 6
             Instances.EVENT_TIMEZONE, // 7
             Instances.DESCRIPTION // 8
     };
     private static final String DEBUG_TAG = "CALENDAR";
     private static final int PROJECTION_EVENT_ID_INDEX = 0;
     private static final int PROJECTION_BEGIN_INDEX = 1;
     private static final int PROJECTION_TITLE_INDEX = 2;
     private static final int PROJECTION_EVENT_LOCATION_INDEX = 4;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
 
         /*
          * If not selected , try to read from database first
          */
         if (this.calendar_id == ExpenseConstants.CALENDAR_ID_NOT_SET) {
             readPreferences();
         }
         /*
          * If read from database failed, pop up a single selection UI
          */
         if (this.calendar_id == ExpenseConstants.CALENDAR_ID_NOT_SET) {
             this.showPreferences();
         }
         showExpenseEventsView();
         ActionBar bar = this.getActionBar();
         bar.setDisplayShowHomeEnabled(false);
         bar.setTitle("Expense List");
        mCalendarInstanceAdapter = new CalendarInstanceAdapter(this,
                instanceList);
         mCalendarInstanceAdapter.setCurrencyUnit(mCurrencyUnit);
         ListView listView = (ListView) this.findViewById(R.id.listViewEvents);
 
         listView.setAdapter(mCalendarInstanceAdapter);
         listView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
 
                 CalendarInstanceData item = instanceList.get(instanceList
                         .size() - position - 1);
 
                 Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI,
                         item.get_ID());
                 Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
                 intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                 intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                         item.getDtstart());
                 intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                         item.getDtend());
                 startActivity(intent);
             }
         });
 
         listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
             @Override
             public boolean onItemLongClick(AdapterView<?> parent, View view,
                     int position, long id) {
                 CalendarInstanceData item = instanceList.get(instanceList
                         .size() - position - 1);
 
                 Intent intent = new Intent(getApplicationContext(),
                         EditExpenseActivity.class);
                 intent.putExtra(CalendarContract.Events._ID, item.get_ID());
                 intent.putExtra(CalendarContract.Events.TITLE, item.getTitle());
                 intent.putExtra(ExpenseConstants.ExpenseEvents.CATEGORY,
                         item.getCategory());
                 intent.putExtra(CalendarContract.Events.EVENT_LOCATION,
                         item.getEventLocation());
                 intent.putExtra(ExpenseConstants.ExpenseEvents.MONEY,
                         item.getDoubleMoney());
                 intent.putExtra(CalendarContract.Events.DTSTART,
                         item.getDtstart());
                 startActivityForResult(intent,
                         ExpenseConstants.REQUEST_CODE_NEW_EXPENSE);
                 return true;
             }
         });
     }
 
     public void startNewExpenseActivity() {
         Intent intent = new Intent(this.getApplicationContext(),
                 CreateExpenseActivity.class);
         startActivityForResult(intent,
                 ExpenseConstants.REQUEST_CODE_NEW_EXPENSE);
     }
 
     protected boolean startDeleteExpenseActivity(long eventID) {
         Uri deleteUri = null;
         deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
         int rows = getContentResolver().delete(deleteUri, null, null);
         Log.i(DEBUG_TAG, "Rows deleted: " + rows);
         return false;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.option_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case (R.id.menuItemCreate):
                 this.startNewExpenseActivity();
                 break;
             case (R.id.menuItemRefresh):
                 this.loadCalendarEvents();
                 if (mCalendarInstanceAdapter != null)
                     mCalendarInstanceAdapter.notifyDataSetChanged();
                 break;
             case (R.id.menuItemSettings):
                 this.showPreferences();
                 break;
             case (R.id.menuItemExpenseReport):
                 this.startExpenseReportActivity();
                 break;
             case R.id.menuItemHelp:
                 Uri uri = Uri.fromFile(new File(Environment
                         .getExternalStorageDirectory().getPath()
                         + "/index.html"));
                 Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                 browserIntent.setClassName("com.android.browser",
                         "com.android.browser.BrowserActivity");
                 browserIntent.setData(uri);
                 startActivity(browserIntent);
                 break;
             case R.id.menuItemPieChart:
                 this.startPieChartReportActivity();
                 break;
             case R.id.menuItemSync:
                 startActivity(new Intent(
                         android.provider.Settings.ACTION_SYNC_SETTINGS));
                 break;
             default:
                 return super.onOptionsItemSelected(item);
         }
         return true;
     }
 
     private void startPieChartReportActivity() {
         // expense type pie chart
         HashMap<String, Double> data = new HashMap<String, Double>();
         for (CalendarInstanceData instance : this.instanceList) {
             String key = instance.getCategory();
             if (key == null) {
                 key = "Other";
             }
             double sum = data.containsKey(key) ? data.get(key) : 0;
             sum += instance.getDoubleMoney();
             data.put(key, sum);
         }
         Intent intent = new Intent(getBaseContext(), GoogleChartActivity.class);
         intent.putExtra("data", data);
         startActivity(intent);
 
         // pay from pie chart
         data.clear();
         for (CalendarInstanceData instance : this.instanceList) {
             String key = instance.getPayFrom();
             if (key == null) {
                 key = "Cash";
             }
             double sum = data.containsKey(key) ? data.get(key) : 0;
             sum += instance.getDoubleMoney();
             data.put(key, sum);
         }
         intent = new Intent(getBaseContext(), GoogleChartActivity.class);
         intent.putExtra("data", data);
         startActivity(intent);
     }
 
     private void showPreferences() {
         Intent settingsActivity = new Intent(getBaseContext(),
                 Preferences.class);
 
         startActivityForResult(settingsActivity,
                 ExpenseConstants.REQUEST_CODE_CHOOSE_CALENDAR);
     }
 
     private void startExpenseReportActivity() {
         double m = 0.0;
         String lastDate = "";
         for (CalendarInstanceData instance : this.instanceList) {
             if (instance.getDoubleMoney() == 0.0)
                 continue;
             m = m + instance.getDoubleMoney();
             if (lastDate.equals(instance.getStartDate())) {
                 continue;
             } else {
                 lastDate = instance.getStartDate();
             }
         }
 
         Intent intent = new Intent(getBaseContext(), ExpenseStatsActivity.class);
 
         startActivity(intent);
 
     }
 
     private void showExpenseEventsView() {
         setContentView(R.layout.main);
         loadCalendarEvents();
     }
 
     private void readPreferences() {
         this.calendar_id = getSelectedCalendarID();
     }
 
     private long getSelectedCalendarID() {
         long cal_id = -1L;
         SharedPreferences sharedPrefenceManger = PreferenceManager
                 .getDefaultSharedPreferences(this);
 
         String cal = sharedPrefenceManger.getString(
                 ExpenseConstants.ExpenseEvents._ID,
                 ExpenseConstants.ExpenseEvents._ID + "");
         try {
             cal_id = Long.parseLong(cal);
         } catch (NumberFormatException e) {
             Log.d("GET CALENDAR ID FAILED", cal, e);
         }
         return cal_id;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case ExpenseConstants.REQUEST_CODE_CHOOSE_CALENDAR:
 
                 long ret = this.getSelectedCalendarID();
                SharedPreferences sharedPrefences = PreferenceManager
                        .getDefaultSharedPreferences(this);
                String newUnit = sharedPrefences.getString("unit", "$");
 
                if (ret != calendar_id || !newUnit.equals(mCurrencyUnit)) {
                     calendar_id = ret;
                    mCurrencyUnit = newUnit;
                     this.loadCalendarEvents();
                     if (mCalendarInstanceAdapter != null)
                         mCalendarInstanceAdapter.notifyDataSetChanged();
                 }
 
                 break;
             case ExpenseConstants.REQUEST_CODE_NEW_EXPENSE:
                 if (resultCode == Activity.RESULT_OK)
                     this.loadCalendarEvents();
                 if (mCalendarInstanceAdapter != null)
                     mCalendarInstanceAdapter.notifyDataSetChanged();
                 break;
             default:
                 break;
         }

     }
 
     static int[] colorPalette() {
         Random randomGenerator = new Random();
 
         int[] colors = new int[120];
         int index = 0;
         int red, green, blue, r = 0, g = 0, b = 0;
         while (index < colors.length) {
             red = randomGenerator.nextInt(255);
             green = randomGenerator.nextInt(255);
             blue = randomGenerator.nextInt(255);
             if (red + green + blue < 60 || red + green + blue > 750)
                 continue;
             int colorDiff = Math.abs(red - r) + Math.abs(green - g)
                     + Math.abs(blue - b);
             if (colorDiff < 120)
                 continue;
 
             colors[index++] = Color.rgb(red, green, blue);
             r = red;
             g = green;
             b = blue;
         }
         return colors;
     }
 }
