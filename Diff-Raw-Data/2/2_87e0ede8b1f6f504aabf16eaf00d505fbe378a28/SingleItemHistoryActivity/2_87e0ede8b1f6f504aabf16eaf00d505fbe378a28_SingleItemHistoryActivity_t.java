 package com.goldmanalpha.dailydo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.com.goldmanalpha.dailydo.db.*;
 import com.goldmanalpha.dailydo.model.DoableItem;
 
 import java.sql.Time;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Aaron
  * Date: 1/15/12
  * Time: 5:20 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SingleItemHistoryActivity extends Activity {
 
     ListView mainList;
     DoableItemValueTableAdapter doableItemValueTableAdapter;
     Cursor cachedCursor;
     int itemId;
 
     DoableValueCursorHelper cursorHelper;
     public static String ExtraValueItemId = "itemId";
     public static String ExtraValueItemName = "itemName";
 
     private static final SimpleDateFormat short24TimeFormat = new SimpleDateFormat("HH:mm");
     private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MM/d");
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Intent intent = getIntent();
 
         itemId = intent.getIntExtra(ExtraValueItemId, 0);
         String itemName = intent.getStringExtra(ExtraValueItemName);
 
         setContentView(R.layout.single_history);
 
         TextView nameView = (TextView) findViewById(R.id.single_history_name);
         nameView.setText(itemName);
 
         mainList = (ListView) findViewById(R.id.single_history_list);
         SetupList(itemId);
         
         mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 
                 Intent intent = new Intent(SingleItemHistoryActivity.this, MainActivity.class);
 
                SetCursor(view, cachedCursor);
 
                 try {
                     Date appliesToDate = doableItemValueTableAdapter.getAppliesToDate(cachedCursor);
                     intent.putExtra(MainActivity.ExtraValueDateGetTimeLong, appliesToDate.getTime());
 
                     DoableItem item = new DoableItemTableAdapter().get(itemId);
 
                     intent.putExtra(MainActivity.ExtraValueCategoryId, item.getCategoryId());
                     
                     startActivity(intent);
                     finish();
                             
                 } catch (ParseException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     Toast.makeText(SingleItemHistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG)
                             .show();
                 }
             }
         });
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         android.view.MenuItem  item = menu.add(0, MenuItems.ToggleLongDescription, 0, "Long Description");
         item.setCheckable(true);
         return true;
     }
 
     static final class MenuItems {
         public static final int ToggleLongDescription = 0;
     }
 
     boolean showLongDescription;
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId())
         {
             case MenuItems.ToggleLongDescription:
 
                 item.setChecked(!item.isChecked());
 
                 showLongDescription = item.isChecked();
 
                 SetupList(this.itemId);
 
                 break;
         }
 
         return true;
     }
 
     private void SetupList(Integer itemId) {
 
 
         doableItemValueTableAdapter = new DoableItemValueTableAdapter();
         cachedCursor = doableItemValueTableAdapter.getItems(itemId);
         cursorHelper = new DoableValueCursorHelper(cachedCursor);
 
         startManagingCursor(cachedCursor);
 
         String[] from = new String[]{
                 DoableItemValueTableAdapter.ColAmount,
                 DoableItemValueTableAdapter.ColTeaspoons,
                 DoableItemValueTableAdapter.ColPotency,
                 DoableItemValueTableAdapter.ColDescription,
                 DoableItemValueTableAdapter.ColAppliesToDate,
                 DoableItemValueTableAdapter.ColAppliesToTime,
                 DoableItemValueTableAdapter.ColFromTime
 
         };
 
         int[] to = new int[]{
                 R.id.single_history_item_value,
                 R.id.single_history_item_tsp,
                 R.id.single_history_item_potency,
                 R.id.single_history_item_description,
                 R.id.single_history_item_date,
                 R.id.single_history_item_applies_to_time,
                 R.id.single_history_item_time_value
         };
 
         final int teaspoonColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColTeaspoons);
         final int potencyColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColPotency);
 
         final int appliesToDateColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColAppliesToDate);
         final int appliesToTimeColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColAppliesToTime);
         final int showAppliesToTimeCountColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColShowAppliesToTimeCount);
 
         final int fromTimeColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColFromTime);
         final int toTimeColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColToTime);
 
         final int valueIdColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColId);
         final int itemIdColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColItemId);
         final int descriptionColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColDescription);
         final int createdDateColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColDateCreated);
 
 
         SimpleCursorAdapter listCursorAdapter = new SimpleCursorAdapter(mainList.getContext(),
                 R.layout.single_history_item, cachedCursor, from, to);
 
         mainList.setAdapter(listCursorAdapter);
 
         listCursorAdapter.setViewBinder(
                 new SimpleCursorAdapter.ViewBinder() {
                     public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 
                         if (columnIndex == descriptionColumnIndex)
                         {
                             TextView tv = (TextView) view;
 
                             String description = cursor.getString(columnIndex);
                             
                             if (description == null || description.trim() == "")
                             {
                                  tv.setHeight(0);
                             }
                             else
                             {
                                 if (!showLongDescription)
                                 {
                                     tv.setSingleLine();
                                 }
 
                                 tv.setText(description);
                             }
 
                         }
                         
                         if (columnIndex == appliesToDateColIdx) {
                             TextView tv = (TextView) view;
 
                             String appliesToDate = cursor.getString(columnIndex);
 
                             try {
                                 Date d = doableItemValueTableAdapter.TimeStampToDate(appliesToDate);
                                 tv.setText(dateFormat.format(d));
                             } catch (ParseException e) {
                                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                 tv.setText("ERR: " + e.getMessage());
                             }
 
                             return true;
                         }
 
                         if (appliesToTimeColIdx == columnIndex) {
 
                             TextView tv = (TextView) view;
 
                             //todo:  too much leaking logic???
                             //converting to objects would be less (or too in)efficient?
                             boolean showAppliesToTime = cursor.getInt(showAppliesToTimeCountColIdx) > 0
                                     && cursorHelper.timesToShowDate(cursor) < 1;
 
                             if (showAppliesToTime) {
                                 Time t = new Time(0, 0, 0);
                                 if (cursor.isNull(appliesToTimeColIdx)) {
 
                                     try {
                                         Date crDate =
                                                 doableItemValueTableAdapter.TimeStampToDate(cursor.getString(createdDateColIdx));
 
                                         t = new Time(crDate.getHours(), crDate.getMinutes(), crDate.getSeconds());
                                     } catch (ParseException e) {
                                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                     }
                                 } else {
                                     t = doableItemValueTableAdapter.IntToTime(cursor.getInt(appliesToTimeColIdx));
                                 }
 
                                 tv.setText(short24TimeFormat.format(t));
                             } else {
                                 tv.setText("");
                             }
 
                             return true;
                         }
 
                         if (columnIndex == fromTimeColumnIndex) {
 
                             TextView tv = (TextView) view;
 
                             int timesToShowDate = cursorHelper.timesToShowDate(cursor);
 
                             if (timesToShowDate > 0) {
 
                                 boolean toShows = timesToShowDate > 1;
 
                                 int startTimeAsInt = cursor.getInt(columnIndex);
 
                                 Time t = doableItemValueTableAdapter
                                         .IntToTime(startTimeAsInt);
 
                                 String timeToShow = short24TimeFormat.format(t);
 
                                 if (toShows) {
 
                                     int endTimeAsInt = cursor.getInt(toTimeColumnIndex);
                                     t = doableItemValueTableAdapter
                                             .IntToTime(endTimeAsInt);
 
                                     timeToShow = timeToShow
                                             + " - "
                                             + short24TimeFormat.format(t)
                                             + " ("
                                             + doableItemValueTableAdapter.totalHours(startTimeAsInt, endTimeAsInt)
                                             + ")";
                                 }
 
                                 tv.setText(timeToShow);
 
                             } else {
                                 //stupid android seems to hold old values and apply them automatically when handled = true
                                 tv.setText("");
                             }
                             return true;
                         }
 
                         if (columnIndex == teaspoonColIdx) {
                             TextView tv = ((TextView) view);
 
                             if (!cursorHelper.isTeaspoons(cursor)) {
                                 tv.setText("");
                             } else {
                                 tv.setText(cursorHelper.getTeaspoons(cursor));
                             }
 
                             return true;
 
                         }
 
                         if (columnIndex == potencyColIdx) {
                             TextView tv = ((TextView) view);
 
                             if (!cursorHelper.isDrops(cursor)) {
                                 tv.setText("");
                             } else {
                                 tv.setText("p" + Integer.toString(cursor.getInt(potencyColIdx)));
                             }
 
                             return true;
 
                         }
 
                         return false;
                     }
                 }
         );
 
 
 
 
 
 
     }
 
 
     public void item_click(View view) {
         Intent intent = new Intent(this, AddItemActivity.class);
         intent.putExtra("itemId", itemId);
         startActivity(intent);
     }
 
     private void SetCursor(View view, Cursor c)
     {
         c.moveToPosition(mainList.getPositionForView(view));
     }
 
     public void single_history_item_description_click(View v)
     {
         Intent intent = new Intent(this, EditDescriptionActivity.class);
 
         SetCursor(v, cachedCursor);
         intent.putExtra(EditDescriptionActivity.ExtraValueId,
                 cursorHelper.getValueId(cachedCursor));
         startActivity(intent);
     }
 }
