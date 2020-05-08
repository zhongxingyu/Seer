 package org.ph7;
 
 import java.util.ArrayList;
 import java.util.Date;
 import com.google.android.maps.GeoPoint;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.util.Log;
 
 public class Util {
 	public static Bitmap getBitmap (String path, int height) {
 		Bitmap bm = BitmapFactory.decodeFile(path);
 		Matrix matrix = new Matrix ();
 		float ratio = ((float)height)/bm.getHeight();
 		matrix.postScale(ratio, ratio);
 		matrix.postRotate(90);
 		Bitmap rbm = Bitmap.createBitmap(bm, 0, 0,
 				bm.getWidth(), bm.getHeight(), matrix, true);
 		return rbm;
 	}
 	
 	public static Issue getIssue (Context context, int issueId) {
 		DbOpenHelper dbHelper = new DbOpenHelper(context);
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		String[] columns = { "location", "issue_type", "image_filename",
 				"local_id", "created_time", "latitude", "longitude", "accuracy", "comment"};
 		String[] args = { String.valueOf(issueId) };
 		Cursor cursor = null;
 		Issue issue = new Issue();
 		try {
 			cursor = db.query(DbOpenHelper.REPORTS_TABLE_NAME, columns,
 					"local_id = ?", args, null, null, null);
 		} catch (Exception e) {
 			Log.d("TEST", e.getMessage());
 		}
 
 		if (cursor == null)
 			return null;
 
 		try {
 			cursor.moveToNext();
 			String[] items = context.getResources().getStringArray(R.array.issue_items);
 
 			int index = cursor.getInt(1);
 			long ldate = cursor.getLong(4);
 			issue.comment = cursor.getString(8) == null ? "" : cursor.getString(8);
 			Date date = new Date(ldate);
 			issue.date = date;			
 			issue.latitude = cursor.getFloat(5);
 			issue.longitude = cursor.getFloat(6);
 			issue.accuracy = cursor.getFloat(7);
			issue.type = items[index];
 			
 		} catch (Exception e) {
 			Log.d("TEST", e.getMessage());
 
 		} finally {
 			cursor.close();
 		}
 		return issue;
 	}
 	
 	public static ArrayList<Issue> getIssueList(Context context) {
 		ArrayList<Issue> issues =  new ArrayList<Issue>();
 		DbOpenHelper helper = new DbOpenHelper(context);
 		SQLiteDatabase db =helper.getReadableDatabase(); 
 		Cursor c = db.rawQuery(
 				"SELECT location, issue_type, image_filename, local_id, longitude, latitude, accuracy FROM "
 						+ DbOpenHelper.REPORTS_TABLE_NAME, null);
 		if (c == null)
 			return null;
 
 		try {
 			while (c.moveToNext()) {
 				String[] items = context.getResources().getStringArray(
 						R.array.issue_items);
 
 				Issue issue = new Issue();
 				int index = c.getInt(1);
 				issue.location = c.getString(0);
 				issue.type = items[index];
 				issue.imagePath = c.getString(2);
 				issue.id = c.getInt(3);
 				issue.longitude = c.getDouble(4);
 				issue.latitude = c.getDouble(5);
 				issue.accuracy = c.getDouble(6);
 				issues.add(issue);
 			}
 		} finally {
 			c.close();
 		}
 		return issues;
 	}
 
 	public static GeoPoint getGeoPoint (Issue issue) {
 		int latitude = (int)(issue.latitude * 1e6);
 		int longitude = (int)(issue.longitude * 1e6);
 		return new GeoPoint(latitude, longitude);
 	}
 	
 	
 }
