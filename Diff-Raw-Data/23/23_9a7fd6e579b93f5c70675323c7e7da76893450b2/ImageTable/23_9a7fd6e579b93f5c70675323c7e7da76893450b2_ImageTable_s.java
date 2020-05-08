 package edu.grinnell.sandb.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class ImageTable {
 
 	private static final String TAG = "ImageTable";
 
 	// Database fields
 	private SQLiteDatabase database;
 	private ImageStorageHelper dbHelper;
 	private String[] allColumns = { 
 			ImageStorageHelper.COLUMN_ID,
 			ImageStorageHelper.COLUMN_ARTICLEID, 
 			ImageStorageHelper.COLUMN_URL,
 			ImageStorageHelper.COLUMN_IMAGE,
 			ImageStorageHelper.COLUMN_IMGTITLE, };
 
 	public ImageTable(Context context) {
 		dbHelper = new ImageStorageHelper(context);
 	}
 
 	public void open() throws SQLException {
 		database = dbHelper.getWritableDatabase();
 	}
 
 	public void close() {
 		dbHelper.close();
 	}
 
 	public void deleteImage(Image image) {
 		long id = image.getId();
 		Log.d(TAG, "Image deleted with id: " + id);
 		database.delete(ImageStorageHelper.TABLE_IMAGES,
 				ImageStorageHelper.COLUMN_ID + " = " + id, null);
 	}
 
 	public List<Image> getAllImages() {
 		List<Image> images = new ArrayList<Image>();
 
 		Cursor cursor = database.query(ImageStorageHelper.TABLE_IMAGES,
 				allColumns, null, null, null, null, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Image image = cursorToImage(cursor);
 			images.add(image);
 			cursor.moveToNext();
 		}
 		// Make sure to close the cursor
 		cursor.close();
 		return images;
 	}
 
 	public Image createImage(int articleID, String url, byte[] image, String imgTitle) {
 
 
 		ContentValues values = new ContentValues();
 		values.put(ImageStorageHelper.COLUMN_ARTICLEID, articleID);
 		values.put(ImageStorageHelper.COLUMN_URL, url);
 		values.put(ImageStorageHelper.COLUMN_IMAGE, image);
 		values.put(ImageStorageHelper.COLUMN_IMGTITLE, imgTitle);
 		
 		//TODO: NULL POINTER EXCEPTION HERE
 		long insertId = database.insert(ImageStorageHelper.TABLE_IMAGES, null,
 				values);
 		
 		Cursor cursor = database.query(ImageStorageHelper.TABLE_IMAGES,
 				allColumns, ImageStorageHelper.COLUMN_ID + " = " + insertId,
 				null, null, null, null);
 
 		cursor.moveToFirst();
 		Image newImage = cursorToImage(cursor);
 		cursor.close();
 		return newImage;
 	}
 
 	private Image cursorToImage(Cursor cursor) {
 
 		return new Image(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getBlob(3),
 				cursor.getString(4));
 	}
 
 	public void clearTable() {
 		database.execSQL("DROP TABLE IF EXISTS "
 				+ ImageStorageHelper.TABLE_IMAGES);
 		database.execSQL(ImageStorageHelper.DATABASE_CREATE);
 	}
 }
