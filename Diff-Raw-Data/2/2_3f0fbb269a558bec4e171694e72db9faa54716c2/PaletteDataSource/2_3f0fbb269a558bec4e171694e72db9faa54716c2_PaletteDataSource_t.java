 package com.tzapps.tzpalette.db;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.tzapps.common.utils.BitmapUtils;
 import com.tzapps.common.utils.MediaHelper;
 import com.tzapps.common.utils.StringUtils;
 import com.tzapps.tzpalette.Constants;
 import com.tzapps.tzpalette.R;
 import com.tzapps.tzpalette.data.PaletteData;
 import com.tzapps.tzpalette.db.PaletteDataContract.PaletteDataEntry;
 import com.tzapps.tzpalette.db.PaletteDataContract.PaletteThumbEntry;
 import com.tzapps.tzpalette.debug.MyDebug;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.ExifInterface;
 import android.net.Uri;
 import android.util.Log;
 
 public class PaletteDataSource
 {
     private static final String TAG = "PaletteDataSource";
     
     private static PaletteDataSource sInstance = null;
     
     private Context mContext;
     // Database fields
     private SQLiteDatabase db;
     private PaletteDataDbHelper dbHelper;
     private String[] paletteDataColumns = 
         {
             PaletteDataEntry._ID,
             PaletteDataEntry.COLUMN_NAME_TITLE,
             PaletteDataEntry.COLUMN_NAME_COLORS,
             PaletteDataEntry.COLUMN_NAME_UPDATED,
             PaletteDataEntry.COLUMN_NAME_IMAGEURL,
             PaletteDataEntry.COLUMN_NAME_ISFAVOURITE
         };
     
     private String[] paletteThumbColumns =
         {
             PaletteThumbEntry._ID,
             PaletteThumbEntry.COLUMN_NAME_PALETTE_ID,
             PaletteThumbEntry.COLUMN_NAME_THUMB,
             PaletteThumbEntry.COLUMN_NAME_THUMB_SMALL
         };
     
     public static PaletteDataSource getInstance(Context context)
     {
         // Use the application context, which will ensure that you 
         // don't accidentally leak an Activity's context.
         // See this article for more information: http://bit.ly/6LRzfx
         if (sInstance == null)
         {
             sInstance = new PaletteDataSource(context.getApplicationContext());
         }
         
         return sInstance;
     }
     
     private PaletteDataSource(Context context)
     {
         mContext = context;
         dbHelper = PaletteDataDbHelper.getInstance(context);
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
      * insert a new PaletteData record into db
      * 
      * @param  data         the palette data to save
      * @param  quality      the thumb quality
      * @return id           the inserted id in database
      */
     public long add(PaletteData data, int quality)
     {
         // Create a new map of values, where column names are the keys
         ContentValues values = new ContentValues();
         values.put(PaletteDataEntry.COLUMN_NAME_TITLE, data.getTitle());
         
         String colorsStr = Arrays.toString(data.getColors());
         values.put(PaletteDataEntry.COLUMN_NAME_COLORS, colorsStr);
         
         String title =  data.getTitle();
         if (StringUtils.isEmpty(title))
             title = mContext.getResources().getString(R.string.palette_title_default);
         
         values.put(PaletteDataEntry.COLUMN_NAME_TITLE, title);
         values.put(PaletteDataEntry.COLUMN_NAME_IMAGEURL, data.getImageUrl());
         
         long updated = System.currentTimeMillis();
         values.put(PaletteDataEntry.COLUMN_NAME_UPDATED, updated);
         
         int is_favourite = data.isFavourite() ? 1 : 0;
         values.put(PaletteDataEntry.COLUMN_NAME_ISFAVOURITE, is_favourite);
         
         // Insert the new row, returning the primary key values of the new row
         long insertId;
         insertId = db.insert(PaletteDataEntry.TABLE_NAME, 
                              PaletteDataEntry.COLUMN_NAME_NULLABLE, 
                              values);
         
         data.setId(insertId);
         data.setTitle(title);
         data.setUpdated(updated);
         
         // Insert the thumb into the thumb table
         addThumb(insertId, getThumb(data.getImageUrl()), quality, Constants.THUMB_MAX_SIZE, Constants.THUMB_SMALL_MAX_SIZE);
         
         if (MyDebug.LOG)
             Log.d(TAG, "PaletteData saved with id:" + insertId + " thumb quality:" + quality);
         
         return insertId;
     }
     
     private Bitmap getThumb(String imageUrl)
     {
         Uri uri = Uri.parse(imageUrl);
         Bitmap bitmap = BitmapUtils.getBitmapFromUri(mContext, uri, Constants.THUMB_MAX_SIZE);
         
         if (bitmap != null)
         {
             int orientation;
             
             /*
              * This is a quick fix on picture orientation for the picture taken
              * from the camera, as it will be always rotated to landscape 
              * incorrectly even if we take it in portrait mode...
              */
             orientation = MediaHelper.getPictureOrientation(mContext, uri);
             
             if (orientation != ExifInterface.ORIENTATION_NORMAL)
                 bitmap = BitmapUtils.getRotatedBitmap(bitmap, orientation);
         }
         
         return bitmap;
     }
     
     private void addThumb(long dataId, Bitmap bitmap, int quality, int thumbMaxSize, int thumbSmallMaxSize)
     {
         Bitmap thumb = null;
         Bitmap thumbSmall = null;
         
         // Create a new map of values, where column names are the keys
         ContentValues values = new ContentValues();
         
         values.put(PaletteThumbEntry.COLUMN_NAME_PALETTE_ID, dataId);
         
         if (bitmap.getWidth() > thumbMaxSize || bitmap.getHeight() > thumbMaxSize)
             thumb = BitmapUtils.resizeBitmapToFitFrame(bitmap, thumbMaxSize, thumbMaxSize);
         else
             thumb = bitmap;
         
         values.put(PaletteThumbEntry.COLUMN_NAME_THUMB, BitmapUtils.convertBitmapToByteArray(thumb, quality));
         
         if (bitmap.getWidth() > thumbSmallMaxSize || bitmap.getHeight() > thumbSmallMaxSize)
            thumbSmall = BitmapUtils.resizeBitmapToFitFrame(bitmap, thumbMaxSize, thumbMaxSize);
         else
             thumbSmall = bitmap;
         
         values.put(PaletteThumbEntry.COLUMN_NAME_THUMB_SMALL, BitmapUtils.convertBitmapToByteArray(thumbSmall, quality));
         
         // Insert the thumb into database...
         db.insert(PaletteThumbEntry.TABLE_NAME, null, values);
     }
     
     /**
      * Update an existing PaletteData record in db
      * 
      * @param data         the palette data to update
      * @param updateThumb  flag to indicate if update thumb data
      * @param quality      the thumb quality
      */
     public void update(PaletteData data, boolean updateThumb, int quality)
     {
         long id = data.getId();
       
         // Create a new map of values, where column names are the keys
         ContentValues values = new ContentValues();
         
         String title =  data.getTitle();
         if (StringUtils.isEmpty(title))
         {
             title = mContext.getResources().getString(R.string.palette_title_default);
             data.setTitle(title);
         }
         values.put(PaletteDataEntry.COLUMN_NAME_TITLE, title);
         
         String colorsStr = Arrays.toString(data.getColors());
         values.put(PaletteDataEntry.COLUMN_NAME_COLORS, colorsStr);
         
         long updated = System.currentTimeMillis();
         values.put(PaletteDataEntry.COLUMN_NAME_UPDATED, updated);
         
         int is_favourite = data.isFavourite() ? 1 : 0;
         values.put(PaletteDataEntry.COLUMN_NAME_ISFAVOURITE, is_favourite);
         
         if (updateThumb)
         {
             values.put(PaletteDataEntry.COLUMN_NAME_IMAGEURL, data.getImageUrl());
             updateThumb(id, getThumb(data.getImageUrl()), quality, Constants.THUMB_MAX_SIZE, Constants.THUMB_SMALL_MAX_SIZE);
         }
         
         // Issue SQL statement
         db.update(PaletteDataEntry.TABLE_NAME, 
                   values,
                   PaletteDataEntry._ID + " = " + id,
                   null);
         
         data.setUpdated(updated);
         
         if (MyDebug.LOG)
             Log.d(TAG, "PaletteData updated with id:" + id + " thumb quality:" + quality);
     }
     
     private void updateThumb(long dataId, Bitmap bitmap, int quality, int thumbMaxSize, int thumbSmallMaxSize)
     {
         Bitmap thumb = null;
         Bitmap thumbSmall = null;
         
         // Create a new map of values, where column names are the keys
         ContentValues values = new ContentValues();
         
         values.put(PaletteThumbEntry.COLUMN_NAME_PALETTE_ID, dataId);
         
         if (bitmap.getWidth() > thumbMaxSize || bitmap.getHeight() > thumbMaxSize)
             thumb = BitmapUtils.resizeBitmapToFitFrame(thumb, thumbMaxSize, thumbMaxSize);
         else
             thumb = bitmap;
         
         values.put(PaletteThumbEntry.COLUMN_NAME_THUMB, BitmapUtils.convertBitmapToByteArray(thumb, quality));
         
         if (bitmap.getWidth() > thumbSmallMaxSize || bitmap.getHeight() > thumbSmallMaxSize)
             thumbSmall = BitmapUtils.resizeBitmapToFitFrame(thumb, thumbMaxSize, thumbMaxSize);
         else
             thumbSmall = bitmap;
         
         values.put(PaletteThumbEntry.COLUMN_NAME_THUMB_SMALL, BitmapUtils.convertBitmapToByteArray(thumbSmall, quality));
         
         // Issue SQL statement
         db.update(PaletteThumbEntry.TABLE_NAME, 
                   values,
                   PaletteThumbEntry.COLUMN_NAME_PALETTE_ID + " = " + dataId,
                   null);
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
         if (MyDebug.LOG)
             Log.d(TAG, "PaletteData deleted with id:" + id);
         
         db.delete(PaletteDataEntry.TABLE_NAME, 
                   PaletteDataEntry._ID + " = " + id, 
                   null);
         
         db.delete(PaletteThumbEntry.TABLE_NAME,
                   PaletteThumbEntry.COLUMN_NAME_PALETTE_ID + " = " + id, 
                   null);
     }
     
     /**
      * Delete all PaletteData records in database
      */
     public void deleteAll()
     {
         if (MyDebug.LOG)
             Log.d(TAG, "Delete all PaletteData");
         
         db.delete(PaletteDataEntry.TABLE_NAME, null, null);
         db.delete(PaletteThumbEntry.TABLE_NAME, null, null);
     }
     
     /**
      * Get the regular thumb by the indicated palette data id
      *
      * @param id    the palette data id
      * @return the small size thumb
      */
     public Bitmap getThumbSmall(long dataId)
     {
         Bitmap bitmap = null;
         
         Cursor cursor = db.query(
                 PaletteThumbEntry.TABLE_NAME,
                 paletteThumbColumns,
                 PaletteThumbEntry.COLUMN_NAME_PALETTE_ID + " = " + dataId,
                 null,
                 null,
                 null,
                 null
                 );
         
         if (cursor.getCount() != 0)
         {
             cursor.moveToFirst();
             
             byte[] thumb = cursor.getBlob(cursor.getColumnIndexOrThrow(PaletteThumbEntry.COLUMN_NAME_THUMB_SMALL));
             bitmap = BitmapFactory.decodeByteArray(thumb, 0, thumb.length); 
         }
         else
         {
             if (MyDebug.LOG)
                 Log.d(TAG, "the palette data =" + dataId + " doesn't have a thumb_small");
         }
         
         // Make sure to close the cursor
         cursor.close();
         
         return bitmap;
     }
     
     /**
      * Get the regular thumb by the indicated palette data id
      *
      * @param id    the palette data id
      * @return the regular size thumb
      */
     public Bitmap getThumb(long dataId)
     {
         Bitmap bitmap = null;
         
         Cursor cursor = db.query(
                 PaletteThumbEntry.TABLE_NAME,
                 paletteThumbColumns,
                 PaletteThumbEntry.COLUMN_NAME_PALETTE_ID + " = " + dataId,
                 null,
                 null,
                 null,
                 null
                 );
         
         if (cursor.getCount() != 0)
         {
             cursor.moveToFirst();
             
             byte[] thumb = cursor.getBlob(cursor.getColumnIndexOrThrow(PaletteThumbEntry.COLUMN_NAME_THUMB));
             bitmap = BitmapFactory.decodeByteArray(thumb, 0, thumb.length); 
         }
         else
         {
             if (MyDebug.LOG)
                 Log.d(TAG, "the palette data =" + dataId + " doesn't have a thumb");
         }
         
         // Make sure to close the cursor
         cursor.close();
         
         return bitmap;
     }
     
     /**
      * Get a PaletteData record based on its id
      * 
      * @param id    the palette data id
      * @return the PaletteData
      */
     public PaletteData get(long id)
     {
         PaletteData data = null;
         
         Cursor cursor = db.query(
                 PaletteDataEntry.TABLE_NAME,
                 paletteDataColumns,
                 PaletteDataEntry._ID + " = " + id,
                 null,
                 null,
                 null,
                 null
                 );
         
         if (cursor.getCount() != 0)
         {
             cursor.moveToFirst();
             data = cursorToPaletteData(cursor);
         }
         else
         {
             if (MyDebug.LOG)
                 Log.e(TAG, "get palette data with id=" + id + "failed");
         }
         
         // Make sure to close the cursor
         cursor.close();
         
         return data;
     }
     
     /**
      * Get the palette data count from db
      * 
      * @return the count of palette data
      */
     public int count()
     {
         String sql = "SELECT COUNT(*) FROM " + PaletteDataEntry.TABLE_NAME;
         
         Cursor cursor = db.rawQuery(sql, null);
         cursor.moveToFirst();
         int count = cursor.getInt(0);
         cursor.close();
         
         return count;
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
                 paletteDataColumns,
                 null,
                 null,
                 null,
                 null,
                 PaletteDataEntry.COLUMN_NAME_UPDATED + " DESC"
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
         
         int is_favourite = cursor.getInt(cursor.getColumnIndexOrThrow(PaletteDataEntry.COLUMN_NAME_ISFAVOURITE));
         boolean favourite = (is_favourite != 0);
         data.setFavourite(favourite);
         
         if (MyDebug.LOG)
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
 
 
