 package com.tzapps.tzpalette.data;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.tzapps.tzpalette.R;
 import com.tzapps.tzpalette.db.PaletteDataDbHelper;
 import com.tzapps.tzpalette.db.PaletteDataContract.PaletteDataEntry;
 import com.tzapps.utils.BitmapUtils;
 import com.tzapps.utils.StringUtils;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 public class PaletteDataSource
 {
     private static final String TAG = "PaletteDataSource";
     
     private Context mContext;
     // Database fields
     private SQLiteDatabase db;
     private PaletteDataDbHelper dbHelper;
     private String[] allColumns = 
         {
             PaletteDataEntry._ID,
             PaletteDataEntry.COLUMN_NAME_TITLE,
             PaletteDataEntry.COLUMN_NAME_COLORS,
             PaletteDataEntry.COLUMN_NAME_UPDATED,
             PaletteDataEntry.COLUMN_NAME_IMAGEURL,
             PaletteDataEntry.COLUMN_NAME_THUMB
         };
     
     public PaletteDataSource(Context context)
     {
         mContext = context;
         dbHelper = new PaletteDataDbHelper(context);
     }
     
     public void open(boolean writable)
     {
         if (writable)
             db = dbHelper.getWritableDatabase();
         else
             db = dbHelper.getReadableDatabase();
     }
     
     public void close()
     {
         dbHelper.close();
     }
     
     /**
     * Save a new PaletteData record into db
      * 
      * @param data         the palette data to save
      */
    public void save(PaletteData data)
     {
         // Create a new map of values, where column names are the keys
         ContentValues values = new ContentValues();
         values.put(PaletteDataEntry.COLUMN_NAME_TITLE, data.getTitle());
         
         String colorsStr = Arrays.toString(data.getColors());
         values.put(PaletteDataEntry.COLUMN_NAME_COLORS, colorsStr);
         
         String title =  data.getTitle();
        
         if (StringUtils.isEmpty(title))
            mContext.getResources().getString(R.string.palette_title_default);
         
        values.put(PaletteDataEntry.COLUMN_NAME_TITLE, data.getTitle());
         values.put(PaletteDataEntry.COLUMN_NAME_IMAGEURL, data.getImageUrl());
         
         long updated = System.currentTimeMillis();
         values.put(PaletteDataEntry.COLUMN_NAME_UPDATED, updated);
         
         Bitmap thumb = data.getThumb();
         thumb = BitmapUtils.resizeBitmapToFitFrame(thumb, 500, 500);
         values.put(PaletteDataEntry.COLUMN_NAME_THUMB, BitmapUtils.convertBitmapToByteArray(thumb));
         
         // Insert the new row, returning the primary key values of the new row
         long insertId;
         insertId = db.insert(PaletteDataEntry.TABLE_NAME, 
                              PaletteDataEntry.COLUMN_NAME_NULLABLE, 
                              values);
         
         data.setId(insertId);
         data.setUpdated(updated);
         
         Log.d(TAG, "PaletteData saved with id:" + insertId);
     }
     
     /**
      * Update an existing PaletteData record in db
      * 
      * @param data         the palette data to update
      * @param updateThumb  flag to indicate if update thumb data
      */
     public void update(PaletteData data, boolean updateThumb)
     {
         long id = data.getId();
       
         // Create a new map of values, where column names are the keys
         ContentValues values = new ContentValues();
        values.put(PaletteDataEntry.COLUMN_NAME_TITLE, data.getTitle());
         
         String colorsStr = Arrays.toString(data.getColors());
         values.put(PaletteDataEntry.COLUMN_NAME_COLORS, colorsStr);
         
         long updated = System.currentTimeMillis();
         values.put(PaletteDataEntry.COLUMN_NAME_UPDATED, updated);
         
         if (updateThumb)
         {
             values.put(PaletteDataEntry.COLUMN_NAME_IMAGEURL, data.getImageUrl());
             Bitmap thumb = data.getThumb();
             thumb = BitmapUtils.resizeBitmapToFitFrame(thumb, 500, 500);
             values.put(PaletteDataEntry.COLUMN_NAME_THUMB, BitmapUtils.convertBitmapToByteArray(thumb));
         }
         
         // Issue SQL statement
         db.update(PaletteDataEntry.TABLE_NAME, 
                   values,
                   PaletteDataEntry._ID + " = " + id,
                   null);
         
         data.setUpdated(updated);
         
         Log.d(TAG, "PaletteData updated with id:" + id);
     }
     
     /**
      * Delete a PaletteData record from db
      * 
      * @param data the palette data to delete
      */
     public void delete(PaletteData data)
     {
         delete(data.getId());
     }
     
     /**
      * Delete a PaletteData record based on its id
      * 
      * @param id the palette data id
      */
     public void delete(long id)
     {
         Log.d(TAG, "PaletteData deleted with id:" + id);
         
         db.delete(PaletteDataEntry.TABLE_NAME, 
                   PaletteDataEntry._ID + " = " + id, 
                   null);
     }
     
     /**
      * Get all PaletteData records from db
      * 
      * @return the array list with all PaletteData records
      */
     public List<PaletteData> getAllPaletteData()
     {
         List<PaletteData> dataList = new ArrayList<PaletteData>();
         
         Cursor cursor = db.query(
                 PaletteDataEntry.TABLE_NAME,
                 allColumns,
                 null,
                 null,
                 null,
                 null,
                 null
                 );
         
         cursor.moveToFirst();
         while (!cursor.isAfterLast())
         {
             PaletteData data = cursorToPaletteData(cursor);
             dataList.add(data);
             cursor.moveToNext();
         }
         
         // Make sure to close the cursor
         cursor.close();
         return dataList;
     }
     
     private PaletteData cursorToPaletteData(Cursor cursor)
     {
         PaletteData data = new PaletteData();
          
         long id = cursor.getLong(cursor.getColumnIndexOrThrow(PaletteDataEntry._ID));
         data.setId(id);
         
         String title = cursor.getString(cursor.getColumnIndexOrThrow(PaletteDataEntry.COLUMN_NAME_TITLE));
         data.setTitle(title);
         
         String colorStr = cursor.getString(cursor.getColumnIndexOrThrow(PaletteDataEntry.COLUMN_NAME_COLORS));
         data.addColors(convertColorStrToColors(colorStr), /*reset*/true);
         
         long updated = cursor.getLong(cursor.getColumnIndexOrThrow(PaletteDataEntry.COLUMN_NAME_UPDATED));
         data.setUpdated(updated);
         
         String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(PaletteDataEntry.COLUMN_NAME_IMAGEURL));
         data.setImageUrl(imageUrl);
         
         byte[] thumb = cursor.getBlob(cursor.getColumnIndexOrThrow(PaletteDataEntry.COLUMN_NAME_THUMB));
         
         if (thumb != null)
             data.setThumb(BitmapFactory.decodeByteArray(thumb, 0, thumb.length));
                     
         Log.d(TAG, "PaletteData fetched from db: " + data.toString());
         
         return data;
     }
     
     private int[] convertColorStrToColors(String colorsStr)
     {
         String[] items = colorsStr.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "").split(",");
         
         int[] colors = new int[items.length];
         
         for (int i = 0; i < items.length; i++)
         {
             try
             {
                 colors[i] = Integer.parseInt(items[i]);
             }
             catch (NumberFormatException nfe) {};
         }
         
         return colors;
     }
     
 }
 
 
