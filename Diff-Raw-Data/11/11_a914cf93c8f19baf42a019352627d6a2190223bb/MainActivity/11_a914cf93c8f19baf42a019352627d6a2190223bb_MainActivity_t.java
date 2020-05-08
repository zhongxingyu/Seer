 package com.goldmanalpha.dailydo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.com.goldmanalpha.dailydo.db.DoableItemValueTableAdapter;
 import com.goldmanalpha.androidutility.DateHelper;
 import com.goldmanalpha.androidutility.DayOnlyDate;
 import com.goldmanalpha.androidutility.EnumHelper;
 import com.goldmanalpha.androidutility.PickOneList;
 import com.goldmanalpha.dailydo.model.DoableBase;
 import com.goldmanalpha.dailydo.model.DoableValue;
 import com.goldmanalpha.dailydo.model.TeaSpoons;
 import com.goldmanalpha.dailydo.model.UnitType;
 
 import java.sql.Time;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 public class MainActivity extends Activity {
 
     private TextView mDateDisplay;
     Date mDisplayingDate;
     DoableItemValueTableAdapter doableItemValueTableAdapter;
 
     private static final SimpleDateFormat shortMonthDateFormat = new SimpleDateFormat("MMM-dd");
     private static final SimpleDateFormat short24TimeFormat = new SimpleDateFormat("HH-mm");
 
     HashMap<Integer, Boolean> usesTime1Map = new HashMap<Integer, Boolean>();
 
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         DoableBase.setContext(getApplicationContext());
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
 
         updateDisplayDate(new DayOnlyDate());
 
     }
 
     boolean setupDate = false;
     SimpleCursorAdapter listCursorAdapter;
 
     private void SetupList2(Date date) {
         cachedCursor = doableItemValueTableAdapter.getItems(date);
         startManagingCursor(cachedCursor);
 
         listCursorAdapter.changeCursor(cachedCursor);
     }
 
     ListView myList;
     Cursor cachedCursor;
     int valueIdColumnIndex;
     int itemIdColumnIndex;
 
 
     int fromTimeColumnIndex;
     int toTimeColumnIndex;
     int lastFromTimedColumnIndex;
     int lastToTimeColumnIndex;
 
     int teaspoonColIdx;
     int lastTeaspoonColIdx;
     int unitTypeColIdx;
     final String usesTeaspoonsType = UnitType.tsp.toString();
 
     private void SetupList(Date date) {
 
         if (setupDate) {
             SetupList2(date);
             return;
         }
 
         setupDate = true;
 
         doableItemValueTableAdapter = new DoableItemValueTableAdapter(this);
         cachedCursor = doableItemValueTableAdapter.getItems(date);
 
         valueIdColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColId);
         itemIdColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColItemId);
 
         startManagingCursor(cachedCursor);
 
         String[] from = new String[]{DoableItemValueTableAdapter.ColItemName,
                 DoableItemValueTableAdapter.ColUnitType,
                 DoableItemValueTableAdapter.ColAmount,
                 DoableItemValueTableAdapter.ColTeaspoons,
                 DoableItemValueTableAdapter.ColLastAppliesToDate,
                 DoableItemValueTableAdapter.ColLastAmount,
                 DoableItemValueTableAdapter.ColLastTeaspoons,
                 DoableItemValueTableAdapter.ColFromTime,
                 DoableItemValueTableAdapter.ColLastFromTime,
                 DoableItemValueTableAdapter.ColToTime,
                 DoableItemValueTableAdapter.ColLastToTime
         };
 
         int[] to = new int[]{R.id.list_name, R.id.list_unit_type,
                 R.id.amount, R.id.list_teaspoons,
                 R.id.list_lastDate, R.id.list_lastAmount,
                 R.id.list_lastTeaspoons, R.id.list_time1_value,
                 R.id.list_lastTime1, R.id.list_time2_value, R.id.list_lastTime2
         };
 
         teaspoonColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColTeaspoons);
         lastTeaspoonColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColLastTeaspoons);
         unitTypeColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColUnitType);
         final int lastAppliesToDateColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColLastAppliesToDate);
         final int lastTeaspoonsColIdx = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColLastTeaspoons);
 
         lastFromTimedColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColLastFromTime);
         fromTimeColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColFromTime);
         toTimeColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColToTime);
         lastToTimeColumnIndex = cachedCursor.getColumnIndex(DoableItemValueTableAdapter.ColLastToTime);
 
         myList = (ListView) findViewById(R.id.main_list);
 
         listCursorAdapter = new SimpleCursorAdapter(myList.getContext(),
                 R.layout.main_list_item, cachedCursor, from, to);
 
         myList.setAdapter(listCursorAdapter);
 
         listCursorAdapter.setViewBinder(
                 new SimpleCursorAdapter.ViewBinder() {
                     public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 
                         boolean returnValue = false;
                         if (columnIndex == fromTimeColumnIndex
                                 || columnIndex == lastFromTimedColumnIndex
                                 || columnIndex == toTimeColumnIndex
                                 || columnIndex == lastToTimeColumnIndex
                                 ) {
 
                             returnValue = true;
 
                             TextView tv = (TextView) view;
 
                             int timesToShowDate = timesToShowDate(cursor);
 
                             if (timesToShowDate > 1 &&
                                     (columnIndex == fromTimeColumnIndex || columnIndex == toTimeColumnIndex))
                             {
                                 int itemId = cursor.getInt(itemIdColumnIndex);
 
                                 boolean editFirst = !usesTime1Map.containsKey(itemId) || usesTime1Map.get(itemId);
 
                                 if ((columnIndex == fromTimeColumnIndex && editFirst)
                                         || (columnIndex == toTimeColumnIndex && !editFirst)
                                         )
                                     tv.setShadowLayer(3, 3, 3, Color.GREEN);
                                 else
                                     tv.setShadowLayer(0,0,0, Color.BLACK);
                             }
                             else
                                 tv.setShadowLayer(0,0,0, Color.BLACK);
 
 

                                 //hide dash if we don't have 2 dates
                                 int dashId = 0;
 
                                 if (columnIndex == fromTimeColumnIndex) {
                                     dashId = R.id.list_time_separator;
                                 }
 
                                 if (columnIndex == lastFromTimedColumnIndex) {
                                     dashId = R.id.list_lastTimeSeparator;
                                 }
 
                                 if (dashId != 0) {
 
                                     TextView dashView = (TextView)
                                             ((ViewGroup) tv.getParent()).findViewById(dashId);
 
                                    if (timesToShowDate < 2)
                                        dashView.setText("");
                                    else
                                        dashView.setText(" - ");
                                 }

 
                             boolean hasValue = hasValue(cursor);
                             boolean fromShows = (columnIndex == lastFromTimedColumnIndex
                                     || (columnIndex == fromTimeColumnIndex && hasValue))
                                     && timesToShowDate > 0;
 
                             boolean toShows = (columnIndex == lastToTimeColumnIndex
                                     || (columnIndex == toTimeColumnIndex && hasValue))
                                     && timesToShowDate > 1;
 
                             if (fromShows || toShows) {
 
                                 Time t = doableItemValueTableAdapter
                                         .IntToTime(cursor.getInt(columnIndex));
 
                                 tv.setText(short24TimeFormat.format(t));
                             } else {
                                 //stupid android seems to hold old values and apply them automatically when handled = true
                                 tv.setText("");
                             }
                         }
 
                         if (columnIndex == teaspoonColIdx) {
                             TextView tv = ((TextView) view);
 
                             if (!isTeaspoons(cursor)) {
                                 tv.setText("");
                                 returnValue = true;
                             } else {
 
                                 tv.setText(getTeaspoonsForCursorPosition(cursor));
                                 returnValue = true;
                             }
                         }
 
                         if (columnIndex == lastTeaspoonsColIdx) {
                             TextView tv = ((TextView) view);
 
                             if (!isTeaspoons(cursor)) {
                                 tv.setText("");
                                 returnValue = true;
                             }
                         }
 
                         if (columnIndex == lastAppliesToDateColIdx) {
                             returnValue = true;
 
                             ApplyLastAppliesToDateBind((TextView) view, cursor, columnIndex);
                         }
 
                         return returnValue;
                     }
                 }
         );
 
         myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view,
                                     int position, long id) {
 
                 if (view instanceof TextView) {
 
                     // When clicked, show a toast with the TextView text
                     Toast.makeText(getApplicationContext(),
                             ((TextView) view).getText(),
                             Toast.LENGTH_SHORT).show();
 
                 }
 
             }
         });
     }
 
     private void ApplyLastAppliesToDateBind(TextView view, Cursor cursor, int columnIndex) {
         //format the date
 
         String lastAppliesToDate = cursor.getString(columnIndex);
 
         if (lastAppliesToDate == null) {
             return;
         }
 
         try {
             Date d = DoableItemValueTableAdapter.simpleDateFormat.parse(lastAppliesToDate);
 
             TextView tv = (TextView) view;
 
             tv.setText(shortMonthDateFormat.format(d));
 
         } catch (ParseException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 
     boolean isTeaspoons(Cursor cursor) {
 
         return cursor.getString(unitTypeColIdx).equals(usesTeaspoonsType);
     }
 
     UnitType unitType(Cursor cursor) {
         String unitType = cursor.getString(unitTypeColIdx);
         return UnitType.valueOf(unitType);
     }
 
     //0, 1, or 2 times will show depending on unit type
     int timesToShowDate(Cursor cursor) {
         UnitType unitType = unitType(cursor);
 
         return unitType == UnitType.timeSpan ? 2 : (unitType == UnitType.time ? 1 : 0);
     }
 
     public final static TeaSpoons defaultTeaspoons = TeaSpoons.eighth;
 
     class ValueIdentifier {
         public int ValueId;
         public int ItemId;
 
         @Override
         public String toString() {
 
             return "ItemId: " + ItemId + " ValueId: " + ValueId;
         }
     }
 
     public boolean hasValue(Cursor c) {
         return cachedCursor.getInt(valueIdColumnIndex) > 0;
     }
 
     void moveCursorToCurrentRow(View view) {
         GetValueIds(view);
     }
 
     //beneficial side effect - sets cachedCursor to proper position
     public ValueIdentifier GetValueIds(View view) {
         if (cachedCursor.moveToPosition(myList.getPositionForView(view))) {
 
             ValueIdentifier vi = new ValueIdentifier();
 
             vi.ValueId = cachedCursor.getInt(valueIdColumnIndex);
             vi.ItemId = cachedCursor.getInt(itemIdColumnIndex);
 
             return vi;
         }
 
         return null;
     }
 
     public void nameClick(View view) {
         ValueIdentifier ids = GetValueIds(view);
 
         Toast.makeText(getApplicationContext(),
                 ids.toString() + " " + ((TextView) view).getText(),
                 Toast.LENGTH_SHORT).show();
 
     }
 
 
     public void unit_type_click(View v) {
         ValueIdentifier ids = GetValueIds(v);
 
         Toast.makeText(getApplicationContext(),
                 ids.toString() + " " + ((TextView) v).getText(),
                 Toast.LENGTH_SHORT).show();
 
     }
 
 
     int teaspoonsClickValueId;
 
     public void teaspoons_click(View v) {
 
         teaspoonsClickValueId = this.GetValueIds(v).ValueId;
 
         Intent intent = new Intent(this, PickOneList.class);
 
         intent.putExtra(PickOneList.Title, "Pick Unit Teaspoon Size");
 
         intent.putExtra(PickOneList.SelectedItem, ((TextView) v).getText());
 
         intent.putExtra(PickOneList.Choices,
                 EnumHelper.EnumNameToStringArray(TeaSpoons.values(), 1));
 
 
         startActivityForResult(intent, IntentRequestCodes.TeaspoonSelection);
     }
 
     class IntentRequestCodes {
         public static final int TeaspoonSelection = 1;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
 
 
         if (resultCode == RESULT_OK &&
                 requestCode == IntentRequestCodes.TeaspoonSelection
                 ) {
             try {
 
                 String setToTeaspoons = data.getStringExtra(PickOneList.SelectedItem);
 
                 DoableValue value = doableItemValueTableAdapter
                         .get(teaspoonsClickValueId);
 
 
                 if (!value.getTeaspoons().toString().equals(setToTeaspoons)) {
                     value.setTeaspoons(TeaSpoons.valueOf(setToTeaspoons));
 
                     doableItemValueTableAdapter.save(value);
                 }
 
                 SetupList(new DayOnlyDate(this.mDisplayingDate));
 
             } catch (ParseException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 
                 Toast.makeText(this, "Error saving tsp: " + e.getMessage(), Toast.LENGTH_LONG).show();
             }
 
         }
 
 
     }
 
 
     String getTeaspoonsForCursorPosition(Cursor c) {
 
 
         String setTeaspoons = c.getString(this.teaspoonColIdx);
         String lastTeaspoons = c.getString(this.lastTeaspoonColIdx);
 
         String unset = TeaSpoons.unset.toString();
 
         if (setTeaspoons == null || unset.equals(setTeaspoons)) {
             if (lastTeaspoons != null && !unset.equals(lastTeaspoons)) {
                 return lastTeaspoons;
 
             }
         } else {
             return setTeaspoons;
         }
 
 
         return defaultTeaspoons.toString();
 
     }
 
     public void time1_click(View v) {
 
         moveCursorToCurrentRow(v);
 
         if (timesToShowDate(cachedCursor) > 1) {
             TextView otherTv = (TextView) ((ViewGroup) v.getParent()).findViewById(R.id.list_time2_value);
             otherTv.setShadowLayer(0, 0, 0, Color.RED);
 
             TextView tv = (TextView) v;
             tv.setShadowLayer(3, 3, 3, Color.GREEN);
 
             usesTime1Map.put(GetValueIds(v).ItemId, true);
         }
 
     }
 
     public void time2_click(View v) {
 
         TextView otherTv = (TextView) ((ViewGroup) v.getParent()).findViewById(R.id.list_time1_value);
         otherTv.setShadowLayer(0, 0, 0, Color.RED);
 
 
         TextView tv = (TextView) v;
         tv.setShadowLayer(3, 3, 3, Color.GREEN);
 
         usesTime1Map.put(GetValueIds(v).ItemId, false);
 
     }
 
     public void add_click(View v) throws ParseException {
         ValueIdentifier ids = GetValueIds(v);
         TextView tv = (TextView) v;
 
         int addAmount = 0;
 
         int bigAdd = 5;
         int smallAdd = 1;
 
         if (timesToShowDate(cachedCursor) > 0) {
             bigAdd = 60;
             smallAdd = 5;
         }
 
         switch (tv.getId()) {
             case R.id.big_minus:
                 addAmount = -bigAdd;
                 break;
             case R.id.big_plus:
                 addAmount = +bigAdd;
                 break;
             case R.id.plus:
                 addAmount = smallAdd;
                 break;
             case R.id.minus:
                 addAmount = -smallAdd;
                 break;
             default:
                 Toast.makeText(getApplicationContext(),
                         "Unexpected source for add_click", Toast.LENGTH_LONG)
                         .show();
         }
 
         if (addAmount != 0) {
 
             DoableValue value = doableItemValueTableAdapter
                     .get(ids.ValueId);
 
             //its a new value:
             if (value.getDoableItemId() == 0) {
                 value.setDoableItemId(ids.ItemId);
             }
 
             //if its tsp, make sure there's a tsp type set
             if (value.getTeaspoons() == TeaSpoons.unset && isTeaspoons(cachedCursor)) {
                 value.setTeaspoons(
                         TeaSpoons.valueOf(
                                 this.getTeaspoonsForCursorPosition(cachedCursor)));
             }
 
             int timesToShow = timesToShowDate(cachedCursor);
 
             if (value.getId() == 0) {
                 if (timesToShow > 0) {
 
                     int sqlFromTime = cachedCursor.getInt(lastFromTimedColumnIndex);
                     value.setFromTime(doableItemValueTableAdapter.IntToTime(sqlFromTime));
 
                     if (timesToShow > 1) {
                         int sqlToTime = cachedCursor.getInt(lastToTimeColumnIndex);
                         value.setToTime(doableItemValueTableAdapter.IntToTime(sqlToTime));
                     }
 
                 } else {
                     //its a new value, start with last value used
                     value.setAmount(cachedCursor.getFloat(cachedCursor.getColumnIndex(
                             DoableItemValueTableAdapter.ColLastAmount)));
                 }
 
 
             } else {
                 if (timesToShow > 0) {
 
                     Boolean usesTime1 = !usesTime1Map.containsKey(ids.ItemId) || usesTime1Map.get(ids.ItemId);
 
 
                     Time timeToChange = usesTime1 ? value.getFromTime() : value.getToTime();
 
                     if (timeToChange == null) {
                         Date now = new Date();
                         timeToChange = new Time(now.getHours(), 0, 0);
                     }
 
                     Date newTime = DateHelper.addMinutes(timeToChange, addAmount);
 
                     Time setToTime = new Time(newTime.getHours(), newTime.getMinutes(), newTime.getSeconds());
 
                     if (usesTime1)
                         value.setFromTime(setToTime);
                     else
                         value.setToTime(setToTime);
 
                 } else {
                     value.setAmount(value.getAmount() + addAmount);
                 }
             }
 
             value.setAppliesToDate(this.mDisplayingDate);
 
 
             doableItemValueTableAdapter.save(value);
 
 
             cachedCursor.requery();
         }
 
 
     }
 
     public void nextDayClick(View v) {
 
         doableItemValueTableAdapter.recalcDisplayOrder();
         updateDisplayDate(addDays(mDisplayingDate, 1));
     }
 
 
     public void prevDayClick(View v) {
         doableItemValueTableAdapter.recalcDisplayOrder();
         updateDisplayDate(addDays(mDisplayingDate, -1));
     }
 
     public void addItemClick(View v) {
         startActivity(new Intent(this, AddItemActivity.class));
     }
 
     private void updateDisplayDate(Date date) {
 
         mDisplayingDate = date;
         SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy");
         mDateDisplay.setText(format.format(date));
 
         SetupList(new DayOnlyDate(date));
     }
 
     Date addDays(Date date, int days) {
         Calendar c = Calendar.getInstance();
         c.setTime(date);
         c.add(Calendar.DATE, days);  // number of days to add
         return c.getTime();  // dt is now the new date
     }
 
 
 }
