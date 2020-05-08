 package com.underdusken.kulturekalendar.data.db;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import com.underdusken.kulturekalendar.data.EventItem;
 import com.underdusken.kulturekalendar.utils.EventsItemComparator;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public class DatabaseManager {
     private static final String TAG = "DatabaseManager";
 
     private static List<EventItem> cachedEventList = null;
     private static boolean hasChanged = true;
     // Database fields
     private SQLiteDatabase database;
     private MySQLiteHelper dbHelper;
     private String[] allColumnsEvents = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_EVENTS_ID,
             MySQLiteHelper.COLUMN_EVENTS_TITLE, MySQLiteHelper.COLUMN_EVENTS_CATEGORY_ID,
             MySQLiteHelper.COLUMN_EVENTS_ADDRESS, MySQLiteHelper.COLUMN_EVENTS_GEO_LAT,
             MySQLiteHelper.COLUMN_EVENTS_GEO_LON, MySQLiteHelper.COLUMN_EVENTS_DATE_START,
             MySQLiteHelper.COLUMN_EVENTS_PRICE, MySQLiteHelper.COLUMN_EVENTS_AGE_LIMIT,
             MySQLiteHelper.COLUMN_EVENTS_PLACE_NAME, MySQLiteHelper.COLUMN_EVENTS_SHOW_DATE,
             MySQLiteHelper.COLUMN_EVENTS_FAVORITE, MySQLiteHelper.COLUMN_EVENTS_BEER_PRICE,
             MySQLiteHelper.COLUMN_EVENTS_DESCRIPTION_ENG, MySQLiteHelper.COLUMN_EVENTS_DESCRIPTION_NO,
             MySQLiteHelper.COLUMN_EVENTS_IMAGE_URL, MySQLiteHelper.COLUMN_EVENTS_EVENTS_URL,
             MySQLiteHelper.COLUMN_EVENTS_IS_RECOMMENDED, MySQLiteHelper.COLUMN_EVENTS_NOTIFICATION_ID};
 
     public DatabaseManager(Context context) {
         dbHelper = new MySQLiteHelper(context);
     }
 
     public void open() throws SQLException {
         database = dbHelper.getWritableDatabase();
     }
 
     public void close() {
         dbHelper.close();
     }
 
     public boolean isLockedByAnotherThread() {
         return database.isDbLockedByOtherThreads();
     }
 
     public static List<EventItem> sortEventsByDate(List<EventItem> eventItemList) {
         Collections.sort(eventItemList, new EventsItemComparator("getDateStartMS"));
         return eventItemList;
     }
 
     // Convert from Cursor to EventItem
     private EventItem cursorToEventsItem(Cursor cursor) {
         EventItem eventItem = new EventItem();
 
         eventItem.setId(cursor.getLong(0));
         eventItem.setEventsId(cursor.getString(1));
         eventItem.setTitle(cursor.getString(2));
         eventItem.setCategoryID(cursor.getString(3));
         eventItem.setAddress(cursor.getString(4));
         eventItem.setGeoLatitude(cursor.getFloat(5));
         eventItem.setGeoLongitude(cursor.getFloat(6));
         eventItem.setDateStart(cursor.getString(7));
         eventItem.setPrice(cursor.getInt(8));
         eventItem.setAgeLimit(cursor.getInt(9));
         eventItem.setPlaceName(cursor.getString(10));
         eventItem.setShowDate(cursor.getString(11));
         eventItem.setFavorite(cursor.getInt(12) != 0);
         eventItem.setBeerPrice(cursor.getInt(13));
         eventItem.setDescriptionEnglish(cursor.getString(14));
         eventItem.setDescriptionNorwegian(cursor.getString(15));
         eventItem.setImageURL(cursor.getString(16));
         eventItem.setEventURL(cursor.getString(17));
         eventItem.setisRecomended(cursor.getInt(18) != 0);
         eventItem.setNotificationId(cursor.getInt(19));
 
         return eventItem;
     }
 
     // Get All Events from Data Base
     public synchronized List<EventItem> getAllEventsItem() {
         if (!hasChanged) {
             return new ArrayList<EventItem>(cachedEventList);
         }
         List<EventItem> eventItemList = new ArrayList<EventItem>();
         Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS, allColumnsEvents, null, null, null, null,
                 null);
 
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             EventItem eventItem = cursorToEventsItem(cursor);
             eventItemList.add(eventItem);
             cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
 
 
         if (eventItemList.size() > 0) {
             cachedEventList = eventItemList;
             hasChanged = false;
 
             return new ArrayList<EventItem>(cachedEventList);
         }
         Log.w(TAG, "Couldn't update the database.");
        return new ArrayList<EventItem>();
     }
 
     // Get All Events from Data Base
     public List<EventItem> getAllFutureEventsItem() {
         List<EventItem> eventItemList = getAllEventsItem();
         long currentTime = new Date().getTime();
         for (int i = 0; i < eventItemList.size(); i++) {
             if (eventItemList.get(i).getDateStartMS() < currentTime) {
                 eventItemList.remove(i);
                 i--;
             }
         }
         return eventItemList;
     }
 
     public List<EventItem> getAllEventsFavorites() {
         List<EventItem> eventItemList = new ArrayList<EventItem>();
 
         Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS, allColumnsEvents,
                 MySQLiteHelper.COLUMN_EVENTS_FAVORITE + "='1'", null, null, null, null);
 
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             EventItem eventItem = cursorToEventsItem(cursor);
             eventItemList.add(eventItem);
             cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
 
         return sortEventsByDate(eventItemList);
     }
 
     // Get All Events from Data Base from Id
     public List<EventItem> getAllEventsByName(long id, String name) {
         List<EventItem> eventItemList = new ArrayList<EventItem>();
 
         name = name.toLowerCase();
         Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS, allColumnsEvents,
                 "lower(" + MySQLiteHelper.COLUMN_EVENTS_TITLE + ") LIKE '%" + name + "%' AND " +
                         MySQLiteHelper.COLUMN_ID + ">" + id, null, null, null, null);
 
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             EventItem eventItem = cursorToEventsItem(cursor);
             eventItemList.add(eventItem);
             cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
 
         return sortEventsByDate(eventItemList);
     }
 
     // Get All Events from Data Base from Id
     @Deprecated
     public List<EventItem> getAllEventsItemFromId(long id) {
         List<EventItem> eventItemList = new ArrayList<EventItem>();
 
         Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS, allColumnsEvents,
                 MySQLiteHelper.COLUMN_ID + ">" + id, null, null, null, null);
 
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             EventItem eventItem = cursorToEventsItem(cursor);
             eventItemList.add(eventItem);
             cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
 
         return eventItemList;
     }
 
     public List<EventItem> getAllEventsFavoritesFromId(long id) {
         List<EventItem> eventItemList = new ArrayList<EventItem>();
 
         Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS, allColumnsEvents,
                 MySQLiteHelper.COLUMN_EVENTS_FAVORITE + "='1' AND " + MySQLiteHelper.COLUMN_ID + ">" + id,
                 null, null, null, null);
 
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             EventItem eventItem = cursorToEventsItem(cursor);
             eventItemList.add(eventItem);
             cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
 
         return eventItemList;
 
     }
 
     public EventItem getEventsItemById(long id) {
         EventItem eventItem = null;
         Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS, allColumnsEvents,
                 MySQLiteHelper.COLUMN_ID + " = " + id, null, null, null, null);
         cursor.moveToFirst();
 
         eventItem = cursorToEventsItem(cursor);
 
         cursor.close();
         return eventItem;
     }
 
     public void addEventItem(EventItem eventItem) {
         // TODO need to add verification that we have no this event in our DB
         ContentValues values = new ContentValues();
         values.put(MySQLiteHelper.COLUMN_EVENTS_ID, eventItem.getEventsId());
         values.put(MySQLiteHelper.COLUMN_EVENTS_TITLE, eventItem.getTitle());
         values.put(MySQLiteHelper.COLUMN_EVENTS_CATEGORY_ID, eventItem.getCategoryID());
         values.put(MySQLiteHelper.COLUMN_EVENTS_ADDRESS, eventItem.getAddress());
         values.put(MySQLiteHelper.COLUMN_EVENTS_GEO_LAT, eventItem.getGeoLatitude());
         values.put(MySQLiteHelper.COLUMN_EVENTS_GEO_LON, eventItem.getGeoLongitude());
         values.put(MySQLiteHelper.COLUMN_EVENTS_DATE_START, eventItem.getDateStart());
         values.put(MySQLiteHelper.COLUMN_EVENTS_PRICE, eventItem.getPrice());
         values.put(MySQLiteHelper.COLUMN_EVENTS_AGE_LIMIT, eventItem.getAgeLimit());
         values.put(MySQLiteHelper.COLUMN_EVENTS_PLACE_NAME, eventItem.getPlaceName());
         values.put(MySQLiteHelper.COLUMN_EVENTS_SHOW_DATE, eventItem.getShowDate());
         values.put(MySQLiteHelper.COLUMN_EVENTS_FAVORITE, eventItem.getFavorite());
         values.put(MySQLiteHelper.COLUMN_EVENTS_BEER_PRICE, eventItem.getBeerPrice());
         values.put(MySQLiteHelper.COLUMN_EVENTS_DESCRIPTION_ENG, eventItem.getDescriptionEnglish());
         values.put(MySQLiteHelper.COLUMN_EVENTS_DESCRIPTION_NO, eventItem.getDescriptionNorwegian());
         values.put(MySQLiteHelper.COLUMN_EVENTS_IMAGE_URL, eventItem.getImageURL());
         values.put(MySQLiteHelper.COLUMN_EVENTS_EVENTS_URL, eventItem.getEventURL());
         values.put(MySQLiteHelper.COLUMN_EVENTS_IS_RECOMMENDED, eventItem.getIsRecommended());
 
         Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS, allColumnsEvents,
                 MySQLiteHelper.COLUMN_EVENTS_ID + " = '" + eventItem.getEventsId() + "'", null, null, null,
                 null);
 
         if (cursor != null) {
             if (cursor.moveToFirst()) {
                 updateEventsItemByEventId(eventItem);
             } else {
                 database.insert(MySQLiteHelper.TABLE_EVENTS, null, values);
             }
             cursor.close();
         } else {
             database.insert(MySQLiteHelper.TABLE_EVENTS, null, values);
         }
     }
 
     public void updateEventsItemByEventId(EventItem eventItem) {
         ContentValues values = new ContentValues();
         values.put(MySQLiteHelper.COLUMN_EVENTS_ID, eventItem.getEventsId());
         values.put(MySQLiteHelper.COLUMN_EVENTS_TITLE, eventItem.getTitle());
         values.put(MySQLiteHelper.COLUMN_EVENTS_CATEGORY_ID, eventItem.getCategoryID());
         values.put(MySQLiteHelper.COLUMN_EVENTS_ADDRESS, eventItem.getAddress());
         values.put(MySQLiteHelper.COLUMN_EVENTS_GEO_LAT, eventItem.getGeoLatitude());
         values.put(MySQLiteHelper.COLUMN_EVENTS_GEO_LON, eventItem.getGeoLongitude());
         values.put(MySQLiteHelper.COLUMN_EVENTS_DATE_START, eventItem.getDateStart());
         values.put(MySQLiteHelper.COLUMN_EVENTS_PRICE, eventItem.getPrice());
         values.put(MySQLiteHelper.COLUMN_EVENTS_AGE_LIMIT, eventItem.getAgeLimit());
         values.put(MySQLiteHelper.COLUMN_EVENTS_PLACE_NAME, eventItem.getPlaceName());
         values.put(MySQLiteHelper.COLUMN_EVENTS_SHOW_DATE, eventItem.getShowDate());
         values.put(MySQLiteHelper.COLUMN_EVENTS_FAVORITE, eventItem.getFavorite());
         values.put(MySQLiteHelper.COLUMN_EVENTS_BEER_PRICE, eventItem.getBeerPrice());
         values.put(MySQLiteHelper.COLUMN_EVENTS_DESCRIPTION_ENG, eventItem.getDescriptionEnglish());
         values.put(MySQLiteHelper.COLUMN_EVENTS_DESCRIPTION_NO, eventItem.getDescriptionNorwegian());
         values.put(MySQLiteHelper.COLUMN_EVENTS_IMAGE_URL, eventItem.getImageURL());
         values.put(MySQLiteHelper.COLUMN_EVENTS_EVENTS_URL, eventItem.getEventURL());
         values.put(MySQLiteHelper.COLUMN_EVENTS_IS_RECOMMENDED, eventItem.getIsRecommended());
         values.put(MySQLiteHelper.COLUMN_EVENTS_NOTIFICATION_ID, eventItem.getNotificationId());
         database.update(MySQLiteHelper.TABLE_EVENTS, values,
                 MySQLiteHelper.COLUMN_EVENTS_ID + " = '" + eventItem.getEventsId() + "'", null);
         hasChanged = true;
     }
 
     public void updateEventsItem(long id, EventItem eventItem) {
         ContentValues values = new ContentValues();
         values.put(MySQLiteHelper.COLUMN_EVENTS_ID, eventItem.getEventsId());
         values.put(MySQLiteHelper.COLUMN_EVENTS_TITLE, eventItem.getTitle());
         values.put(MySQLiteHelper.COLUMN_EVENTS_CATEGORY_ID, eventItem.getCategoryID());
         values.put(MySQLiteHelper.COLUMN_EVENTS_ADDRESS, eventItem.getAddress());
         values.put(MySQLiteHelper.COLUMN_EVENTS_GEO_LAT, eventItem.getGeoLatitude());
         values.put(MySQLiteHelper.COLUMN_EVENTS_GEO_LON, eventItem.getGeoLongitude());
         values.put(MySQLiteHelper.COLUMN_EVENTS_DATE_START, eventItem.getDateStart());
         values.put(MySQLiteHelper.COLUMN_EVENTS_PRICE, eventItem.getPrice());
         values.put(MySQLiteHelper.COLUMN_EVENTS_AGE_LIMIT, eventItem.getAgeLimit());
         values.put(MySQLiteHelper.COLUMN_EVENTS_PLACE_NAME, eventItem.getPlaceName());
         values.put(MySQLiteHelper.COLUMN_EVENTS_SHOW_DATE, eventItem.getShowDate());
         values.put(MySQLiteHelper.COLUMN_EVENTS_FAVORITE, eventItem.getFavorite());
         values.put(MySQLiteHelper.COLUMN_EVENTS_BEER_PRICE, eventItem.getBeerPrice());
         values.put(MySQLiteHelper.COLUMN_EVENTS_DESCRIPTION_ENG, eventItem.getDescriptionEnglish());
         values.put(MySQLiteHelper.COLUMN_EVENTS_DESCRIPTION_NO, eventItem.getDescriptionNorwegian());
         values.put(MySQLiteHelper.COLUMN_EVENTS_IMAGE_URL, eventItem.getImageURL());
         values.put(MySQLiteHelper.COLUMN_EVENTS_EVENTS_URL, eventItem.getEventURL());
         values.put(MySQLiteHelper.COLUMN_EVENTS_IS_RECOMMENDED, eventItem.getIsRecommended());
         values.put(MySQLiteHelper.COLUMN_EVENTS_NOTIFICATION_ID, eventItem.getNotificationId());
 
         database.update(MySQLiteHelper.TABLE_EVENTS, values, MySQLiteHelper.COLUMN_ID + " = '" + id + "'",
                 null);
         hasChanged = true;
     }
 
     public EventItem updateEventsItemCalendar(long id, int notificationId) {
         ContentValues values = new ContentValues();
         values.put(MySQLiteHelper.COLUMN_EVENTS_NOTIFICATION_ID, notificationId);
 
         long insertId = database.update(MySQLiteHelper.TABLE_EVENTS, values,
                 MySQLiteHelper.COLUMN_ID + " = " + id, null);
 
         // Check that we add information to DB
         Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS, allColumnsEvents,
                 MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
         cursor.moveToFirst();
 
         EventItem checkEventItem = cursorToEventsItem(cursor);
 
         cursor.close();
 
         return checkEventItem;
     }
 
     public EventItem updateEventsItemFavorites(long id, boolean state) {
         ContentValues values = new ContentValues();
         values.put(MySQLiteHelper.COLUMN_EVENTS_FAVORITE, state);
 
         long insertId = database.update(MySQLiteHelper.TABLE_EVENTS, values,
                 MySQLiteHelper.COLUMN_ID + " = " + id, null);
 
         // Check that we add information to DB
         Cursor cursor = database.query(MySQLiteHelper.TABLE_EVENTS, allColumnsEvents,
                 MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
         cursor.moveToFirst();
 
         EventItem checkEventItem = cursorToEventsItem(cursor);
         cursor.close();
 
         return checkEventItem;
     }
 
 }
