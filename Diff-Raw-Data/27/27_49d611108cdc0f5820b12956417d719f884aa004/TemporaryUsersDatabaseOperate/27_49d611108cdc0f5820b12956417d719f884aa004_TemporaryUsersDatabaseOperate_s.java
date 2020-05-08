 package com.gmail.matsushige.nfcv2.db;
 
 import android.app.AlarmManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import com.gmail.matsushige.nfcv2.Nfc_simple;
 
 import java.util.Calendar;
 import java.util.Date;
 
 public class TemporaryUsersDatabaseOperate {
 
 	private final static String TAG = "TemporaryUsersDatabaseOperate";
 	public static String tempText = "";
     private SQLiteOpenHelper temporaryUserDatabaseHelper;
 
     private static TemporaryUsersDatabaseOperate theInstance;
    public static TemporaryUsersDatabaseOperate getTheInstance(Context context){
         if(theInstance != null){
             return theInstance;
         }
         theInstance = new TemporaryUsersDatabaseOperate(context);
         return theInstance;
     }//getTheInstance
 
 
     private Context context;
     private TemporaryUsersDatabaseOperate(Context context){
        this.temporaryUserDatabaseHelper = new TemporaryUsersDatabaseHelper(this.context);
    }//TemporaryUsersDatabaseOperate
 
 	public void write(String type, String id, String regCode){
 		long regTime = Calendar.getInstance().getTimeInMillis();
 		SQLiteDatabase db = this.temporaryUserDatabaseHelper.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put("card_type", type);
 		cv.put("card_id", id);
 		cv.put("register_code", regCode);
 		cv.put("register_time", regTime);
 		cv.put("send_check", false);
 		db.insert("tempusers", null, cv);
 		db.close();
 	}
 	
 	public void read(){
 		tempText = "";
 		SQLiteDatabase db = this.temporaryUserDatabaseHelper
 				.getReadableDatabase();
 		Cursor c = db.query("tempusers", null, null, null, null, null, null);
 		c.moveToFirst();
 		for (int i = 0; i < c.getCount(); ++i) {
 			long serial = c.getLong(c.getColumnIndex("serial"));
 			String type = c.getString(c.getColumnIndex("card_type"));
 			String id = c.getString(c.getColumnIndex("card_id"));
 			String register_code = c.getString(c.getColumnIndex("register_code"));
 			long register_time = c.getLong(c.getColumnIndex("register_time"));
 			String regTime = (new Date(register_time)).toLocaleString();
 			String send_check = c.getString(c.getColumnIndex("send_check"));
 			
 			tempText += serial + " " + type + " " + id + " " + register_code + " " + regTime + " " + send_check + "\n";
 			c.moveToNext();
 		}// for
 		db.close();
 	}
 	
 	public void deleteOldData(){
 		long currentTime = Calendar.getInstance().getTimeInMillis();
 		SQLiteDatabase db = temporaryUserDatabaseHelper.getReadableDatabase();
 		String[] key = {"1"}; // send_checkが"1"であるか 
 		Cursor cursor = db.query("tempusers", null, "send_check = ?", key, null, null, null);
 		cursor.moveToFirst();
 		for(int i = 0; i < cursor.getCount(); i++){
 			long serial = cursor.getLong(cursor.getColumnIndex("serial"));
 			long register_time = cursor.getLong(cursor.getColumnIndex("register_time"));
 			long difference = currentTime - register_time;
 			if(difference > AlarmManager.INTERVAL_FIFTEEN_MINUTES){
 				String[] target = {"" + serial}; // 該当する通し番号を対象に指定
 				db.delete("tempusers", "serial = ?", target);
 				Log.d(TAG, "DELETE");
 			}else{
 				Log.d(TAG, "NOT_DELETE");
 			}// else
 			cursor.moveToNext();
 		}// for
 		cursor.close();
 		db.close();
 	}// deleteOldData
 	
 	public void checkRegisteredData(String type, String id){
 		Nfc_simple.cardOwner = "";
 		SQLiteDatabase db = this.temporaryUserDatabaseHelper.getReadableDatabase();
 		String where = "card_type = ?";
 		String[] where_arg = {type};
 		Cursor cursor = db.query("tempusers", null, where, where_arg, null, null, null);
 		while(cursor.moveToNext()){
 			String registeredId = cursor.getString(cursor.getColumnIndex("card_id"));
 			if(id.equals(registeredId)){
 				String serial = cursor.getString(cursor.getColumnIndex("serial"));
 				String regcode = cursor.getString(cursor.getColumnIndex("register_code"));
 				Nfc_simple.cardOwner = "ゲスト" + serial;
 				Nfc_simple.regCode = regcode;
 				break;
 			}// if
 		}// while
 		cursor.close();
 		db.close();
 	}// checkRegisteredData
 	
 	public static String getRegisterCode(Context context){
 		String registerCode = "";
 		SQLiteDatabase db = (new TemporaryUsersDatabaseHelper(context)).getReadableDatabase();
 		db.close();
 		return registerCode;
 	}// getRegisterCode
 }
