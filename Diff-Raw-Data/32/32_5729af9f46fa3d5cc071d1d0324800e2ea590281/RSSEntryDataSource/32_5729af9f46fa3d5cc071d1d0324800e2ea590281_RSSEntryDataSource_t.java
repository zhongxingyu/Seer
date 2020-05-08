 package org.gnuton.newshub.db;
 
 import android.annotation.TargetApi;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.os.Build;
 import android.util.Log;
 
 import org.gnuton.newshub.types.RSSEntry;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 /**
  */
 public class RSSEntryDataSource extends GenericDataSource {
     private static final String TAG = RSSEntryDataSource.class.getName();
     private final Context mContext;
 
     public RSSEntryDataSource(Context context) {
         super(context);
         mContext = context;
         open();
     }
 
     @Override
     String[] allColumns() {
         return new String[] {
                 DbHelper.ID,
                 DbHelper.ENTRIES_FEEDID,
                 DbHelper.ENTRIES_TITLE,
                 DbHelper.ENTRIES_SUMMARY,
                 DbHelper.ENTRIES_URL,
                 DbHelper.ENTRIES_CONTENT,
                 DbHelper.ENTRIES_PUBLISHEDDATE,
                 DbHelper.ENTRIES_ISREAD,
                 DbHelper.ENTRIES_PODCAST_MEDIA
         };
     }
 
     @Override
     public Serializable create(String[] record) {
         // Get vars from record
         int feedId = Integer.parseInt(record[0]);
         String title = record[1];
         String summary = record[2];
         String url = record[3];
         String content = record[4];
         long publishDate = Long.parseLong(record[5]);
         int isRead = Integer.parseInt(record[6]);
         String podcastMedia = record[7];
 
         // Do not double records
         String selection = DbHelper.ENTRIES_URL + " = "+ DatabaseUtils.sqlEscapeString(url);
         List<RSSEntry> entries = this.getAll(selection, null, null, null, null);
         if (entries.size() != 0){
             Log.d(TAG, title + "is already in the DB");
             if (entries.size() > 1)
                 Log.d(TAG, "WARNING: More than an entry has same title");
             return null;
         }
         Log.d(TAG, "No similar item found in the DB. Adding new record...");
 
         // Create new record in the DB.
         ContentValues values = new ContentValues();
         values.put(DbHelper.ENTRIES_FEEDID, feedId);
         values.put(DbHelper.ENTRIES_TITLE, title);
         values.put(DbHelper.ENTRIES_SUMMARY, summary);
         values.put(DbHelper.ENTRIES_URL, url);
         values.put(DbHelper.ENTRIES_CONTENT, content);
         values.put(DbHelper.ENTRIES_PUBLISHEDDATE, publishDate);
         values.put(DbHelper.ENTRIES_ISREAD, isRead);
         values.put(DbHelper.ENTRIES_PODCAST_MEDIA, podcastMedia);
 
         long insertId = database.insert(DbHelper.TABLE_ENTRIES, null, values);
         Cursor cursor = database.query(DbHelper.TABLE_ENTRIES,
                 this.allColumns(), DbHelper.ID + " = " + insertId, null,
                 null, null, null);
         cursor.moveToFirst();
         RSSEntry entry = (RSSEntry) cursorTo(cursor);
         cursor.close();
         return entry;
     }
 
     @Override
     public void delete(Serializable record) {
         RSSEntry entry = (RSSEntry) record;
         long id = entry.id;
         Log.d(TAG, "Entry deleted with id: " + id);
         database.delete(DbHelper.TABLE_ENTRIES, DbHelper.ID + " = " + id, null);
     }
 
    public void deleteAll() {
        Log.d(TAG, "Deleting all entries...");
        database.delete(DbHelper.TABLE_ENTRIES, null, null);
    }

    public void deleteEntriesWithInvalidIDs() {
        Log.d(TAG, "Deleting all entries with invalid IDs...");
        database.delete(DbHelper.TABLE_ENTRIES, DbHelper.ENTRIES_FEEDID + " = -1", null);
    }

     public void deleteOld() {
         Log.d(TAG, "Deleting all old posts");
 
         String where = DbHelper.ENTRIES_FEEDID + " NOT IN ( SELECT " + DbHelper.ENTRIES_FEEDID +
                 " FROM "+ DbHelper.TABLE_ENTRIES +
                 " ORDER BY " + DbHelper.ENTRIES_PUBLISHEDDATE +
                 " DESC LIMIT 500)";
 
         int n = database.delete(DbHelper.TABLE_ENTRIES, where, null);
         Log.d(TAG, "Deleted n=" + String.valueOf(n));
 
 /*
         // Gets feeds
         RSSFeedDataSource fds = new RSSFeedDataSource(mContext);
         List<RSSFeed> feeds = fds.getAll();
 
 
         // Remove old stuff
         for (RSSFeed feed : feeds){
             String where = DbHelper.ENTRIES_FEEDID +"=" + feed.id +
                     " NOT IN ( " +
                     " SELECT " + DbHelper.ENTRIES_FEEDID +
                     " FROM "+ DbHelper.TABLE_ENTRIES +
                     " WHERE " + DbHelper.ENTRIES_FEEDID + "=" + feed.id +
                     " ORDER BY " + DbHelper.ENTRIES_PUBLISHEDDATE +
                     " DESC LIMIT 50)";
             Log.d(TAG, "QUERY" + where);
             int n = database.delete(DbHelper.TABLE_ENTRIES, where, null);
             Log.d(TAG, "Deleted Feed ID=" + feed.id + " n=" + String.valueOf(n));
         }
 */
     }
 
     @Override
     public List getAll(){
         return getAll(null, null, null, null, null);
     }
 
     public List getAll(String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
         Log.d(TAG, "Getting records with selection: " + selection + " GroupBy:" + groupBy);
         List<RSSEntry> entries = new ArrayList<RSSEntry>();
         Cursor cursor = database.query(DbHelper.TABLE_ENTRIES, this.allColumns(), selection, selectionArgs, groupBy, having, orderBy);
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             RSSEntry entry = (RSSEntry) cursorTo(cursor);
             entries.add(entry);
             cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
         return entries;
     }
 
     @TargetApi(Build.VERSION_CODES.FROYO)
     @Override
     Serializable cursorTo(Cursor cursor) {
         final int id = cursor.getInt(cursor.getColumnIndex(DbHelper.ID));
         final int feedId = cursor.getInt(cursor.getColumnIndex(DbHelper.ENTRIES_FEEDID));
         final String title = cursor.getString(cursor.getColumnIndex(DbHelper.ENTRIES_TITLE));
         final String summary = cursor.getString(cursor.getColumnIndex(DbHelper.ENTRIES_SUMMARY));
         final String url = cursor.getString(cursor.getColumnIndex(DbHelper.ENTRIES_URL));
         final String content = cursor.getString(cursor.getColumnIndex(DbHelper.ENTRIES_CONTENT));
         final long publishedDataLong = cursor.getLong(cursor.getColumnIndex(DbHelper.ENTRIES_PUBLISHEDDATE));
         final Boolean isRead = cursor.getInt(cursor.getColumnIndex(DbHelper.ENTRIES_ISREAD)) > 0;
         final String podcast_media = cursor.getString(cursor.getColumnIndex(DbHelper.ENTRIES_PODCAST_MEDIA));
 
         Log.d(TAG, "READ ENTRY " + String.valueOf(id) + " feedID " + String.valueOf(feedId) + " title " + String.valueOf(title));
 
 
         Calendar publishedData = new GregorianCalendar();
         publishedData.setTimeInMillis(publishedDataLong);
         //Log.d(TAG,"PUBLISHED DATE" + publishedData.toString());
 
         RSSEntry entry = new RSSEntry(id, feedId, title, summary, url, publishedData);
         entry.content = content;
         entry.isRead = isRead;
         entry.podcastMedia = podcast_media;
 
         return entry;
     }
 
     public void update(RSSEntry e) {
         Log.d(TAG, "Updating entry in DB");
 
         List<String> columnsToUpdate = e.columnsToUpdate;
 
         ContentValues args = new ContentValues();
         if (columnsToUpdate.contains(DbHelper.ENTRIES_ISREAD))
             args.put(DbHelper.ENTRIES_ISREAD, e.isRead);
         if (columnsToUpdate.contains(DbHelper.ENTRIES_CONTENT))
             args.put(DbHelper.ENTRIES_CONTENT, e.content);
 
         if (args.size() > 0)
             database.update(DbHelper.TABLE_ENTRIES, args, DbHelper.ID + "=" + e.id, null);
 
         e.columnsToUpdate.clear();
     }
 }
