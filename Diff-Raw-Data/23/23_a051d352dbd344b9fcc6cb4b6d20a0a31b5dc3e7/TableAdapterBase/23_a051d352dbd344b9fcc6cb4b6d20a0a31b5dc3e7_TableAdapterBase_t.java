 package com.com.goldmanalpha.dailydo.db;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import com.goldmanalpha.dailydo.model.DoableBase;
 import com.goldmanalpha.dailydo.model.DoableValue;
 
 import java.sql.Time;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public abstract class TableAdapterBase<T extends DoableBase>
         extends DatabaseRoot {
 
     protected String tableName;
 
     public TableAdapterBase(String tableName) {
         this.tableName = tableName;
     }
 
     //returns id or -1 if fail
     public long save(T object) {
 
         ContentValues values = createContentValues(object);
         DoableBase b = (DoableBase) object;
         long retVal = b.getId();
 
         open();
 
         if (b.getId() == 0) {
             retVal = db.insert(tableName, null, values);
         } else {
             int rows = db.update(tableName, values, "id = " + b.getId(), null);
             if (rows == 0) {
                 throw new IndexOutOfBoundsException("Unexpected no rows affected for update of: " + object.toString());
             }
 
             if (rows > 1) {
                 throw new IndexOutOfBoundsException("Unexpected " + rows + " rows affected for update of: " + object.toString());
             }
 
         }
 
         return retVal;
     }
 
     protected ContentValues createContentValues(T object) {
         ContentValues values = new ContentValues();
 
         DoableBase b = (DoableBase) object;
         //YYYY-MM-DD HH:MM:SS.SSS
 
         if (b.getId() > 0) {
             //its an update
             values.put("id", b.getId());
         }
 
         values.put("dateModified", DateToTimeStamp(new Date()));
 
         return values;
     }
 
     public abstract T get(int id) throws ParseException;
 
     public void delete(int id) throws ParseException {
         open();
 
         String sql = "delete from " + tableName + " where id = " + id;
 
         db.execSQL(sql);
     }
 
 
     protected Cursor getSingle(int id) {
         open();
 
         Cursor cursor = db.rawQuery("select * from "
                 + tableName
                 + " where id = ?"
                 , new String[]{"" + id});
 
         return cursor;
     }
 
     public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
     protected String DateToTimeStamp(Date d) {
         return simpleDateFormat.format(d);
     }
 
     public Date TimeStampToDate(String timestamp) throws ParseException {
         return simpleDateFormat.parse(timestamp);
     }
 
     public Integer TimeToInt(Time t) {
         if (t == null) {
             //null time is only for defaults
             Date now = new Date();
             t = new Time(now.getHours(), 0, 0);
         }
 
         return t.getHours() * 10000 + t.getMinutes() * 100 + t.getSeconds();
     }
 
     public Time IntToTime(Integer time) {
         Integer seconds = time % 100;
 
         time = (time - seconds) / 100;
 
         Integer minutes = time % 100;
         Integer hours = (time - minutes) / 100;
 
         Time t = new Time(hours, minutes, seconds);
 
         return t;
     }
 
     public float totalHours(Integer time1, Integer time2) {
 
         if (time1 > time2) {
 
             float diff24 = totalHours(time1, 240000, true);
             float diff2 = totalHours(1, time2, true);
 
             float total = diff24 + diff2;
 
            return (float) Math.round(total * 10) / 10f;
         }
 
         return totalHours(time1, time2, false);
 
     }
 
     float totalHours(Integer time1, Integer time2, boolean exact) {
 
        float  hours1 = (float) Math.floor(time1/ 10000);
        float  hours2 = (float) Math.floor(time2 / 10000);
 
        float  minutes1Pct = (time1 % 10000) / 6000;
        float  minutes2Pct = (time2 % 10000) / 6000;
 
        hours1 += minutes1Pct;
        hours2 += minutes2Pct;

        float  diff = hours2 - hours1;
 
         if (exact)
            return diff;
         else
            return Math.round(diff * 10) / 10.0f;
     }
 
 
     public void setCommonValues(T val, Cursor c) {
 
         try {
             val.setDateCreated(
                     TimeStampToDate(c.getString(c.getColumnIndex("dateCreated"))));
             val.setDateModified(simpleDateFormat.parse(c.getString(c.getColumnIndex("dateModified"))));
         } catch (ParseException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 }
