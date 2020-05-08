 package ic.utils;
 
 
 
 import ic.items.CL;
 import ic.items.Item;
 import ic.listeners.DialogButtonOnClickListener;
 import ic.listeners.DialogButtonOnTouchListener;
 import ic.listeners.DialogListener;
 import ic.listeners.DialogOnItemClickListener;
 import ic.main.CheckActv;
 import ic.main.MainActv;
 import ic.main.R;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.net.Uri;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import android.os.AsyncTask;
 
 import org.apache.commons.lang.StringUtils;
 
 public class Methods {
 
 	static int counter;		// Used => sortFileList()
 
 	static Activity actv_methods;
 	
 	/****************************************
 	 * Enums
 	 ****************************************/
 	public static enum DialogButtonTags {
 		// Generics
 		dlg_generic_dismiss, dlg_generic_dismiss_second_dialog, dlg_generic_dismiss_third_dialog,
 
 		// dlg_register_genre.xml
 		dlg_register_genre_bt_ok,
 
 		// dlg_register_list.xml
 		dlg_register_list_bt_ok,
 		
 		// dlg_register_item.xml
 		dlg_rgstr_item_bt_ok,
 
 		// dlg_checkactv_change_serial_num_btn_ok.xml
 		dlg_checkactv_change_serial_num_btn_ok,
 		
 		
 	}//public static enum DialogButtonTags
 	
 	public static enum DialogItemTags {
 		// dlg_register.xml
 		dlg_register_lv,
 		
 		// dlg_checkactv_long_click.xml
 		dlg_checkactv_long_click_lv,
 		
 	}//public static enum DialogItemTags
 	
 	
 	public static enum ButtonTags {
 		// MainActivity.java
 		ib_up,
 		
 		// DBAdminActivity.java
 		db_manager_activity_create_table, db_manager_activity_drop_table, 
 		db_manager_activity_register_patterns,
 		
 		// thumb_activity.xml
 		thumb_activity_ib_back, thumb_activity_ib_bottom, thumb_activity_ib_top,
 		
 		// image_activity.xml
 		image_activity_back,
 		
 		// TIListAdapter.java
 		tilist_cb,
 
 		// actv_check.xml
 		actv_check_bt_add,
 		
 		
 	}//public static enum ButtonTags
 	
 	public static enum ItemTags {
 		
 		// MainActivity.java
 		dir_list,
 		
 		// ThumbnailActivity.java
 		dir_list_thumb_actv,
 		
 		// Methods.java
 		dir_list_move_files,
 		
 		// TIListAdapter.java
 		tilist_checkbox,
 		
 		
 	}//public static enum ItemTags
 
 	public static enum MoveMode {
 		// TIListAdapter.java
 		ON, OFF,
 		
 	}//public static enum MoveMode
 
 	public static enum PrefenceLabels {
 		
 		CURRENT_PATH,
 		
 		thumb_actv,
 		
 		chosen_list_item,
 		
 	}//public static enum PrefenceLabels
 
 	public static enum ListTags {
 		// MainActivity.java
 		actv_main_lv,
 
 		// CheckActv.java
 		actv_check_lv,
 
 	}//public static enum ListTags
 
 	public static enum LongClickTags {
 		// MainActivity.java
 		main_activity_list,
 		
 		
 	}//public static enum LongClickTags
 
 	
 	/****************************************
 	 * Vars
 	 ****************************************/
 	public static final int vibLength_click = 35;
 
 	static int tempRecordNum = 20;
 
 	/****************************************
 	 * Methods
 	 ****************************************/
 	public static void sortFileList(File[] files) {
 		// REF=> http://android-coding.blogspot.jp/2011/10/sort-file-list-in-order-by-implementing.html
 		/*----------------------------
 		 * 1. Prep => Comparator
 		 * 2. Sort
 			----------------------------*/
 		/*----------------------------
 		 * 1. Prep => Comparator
 			----------------------------*/
 		Comparator<? super File> filecomparator = new Comparator<File>(){
 			
 			public int compare(File file1, File file2) {
 				/*----------------------------
 				 * 1. Prep => Directory
 				 * 2. Calculate
 				 * 3. Return
 					----------------------------*/
 				
 				int pad1=0;
 				int pad2=0;
 				
 				if(file1.isDirectory())pad1=-65536;
 				if(file2.isDirectory())pad2=-65536;
 				
 				int res = pad2-pad1+file1.getName().compareToIgnoreCase(file2.getName());
 				
 				return res;
 			} 
 		 };//Comparator<? super File> filecomparator = new Comparator<File>()
 		 
 		/*----------------------------
 		 * 2. Sort
 			----------------------------*/
 		Arrays.sort(files, filecomparator);
 
 	}//public static void sortFileList(File[] files)
 
 	public static boolean sort_list_CLList(Activity actv, List<CL> cLList) {
 		/*********************************
 		 * 1. Comaparator
 		 * 2. Sort
 		 *********************************/
 		actv_methods = actv;
 		
 		/*********************************
 		 * 1. Comaparator
 		 *********************************/
 		Comparator<CL> comp = new Comparator<CL>(){
 
 			public int compare(CL c1, CL c2) {
 				/*********************************
 				 * 1. Get genre name
 				 * 2. Null?
 				 * 
 				 * 3. Genre names => Not equal?
 				 * 4. Genre names => Equal?
 				 *********************************/
 				String c1_genre_name = 
 						Methods.get_genre_name_from_genre_id(actv_methods, c1.getGenre_id());
 				
 				String c2_genre_name = 
 						Methods.get_genre_name_from_genre_id(actv_methods, c2.getGenre_id());
 				
 				/*********************************
 				 * 2. Null?
 				 *********************************/
 				if (c1_genre_name == null || c2_genre_name == null) {
 					
 					return 0;
 					
 				}
 				
 				/*********************************
 				 * 3. Genre names => Not equal?
 				 *********************************/
 				if (!c1_genre_name.equals(c2_genre_name)) {
 					
 					return c1_genre_name.compareTo(c2_genre_name);
 					
 				} else {//if (condition)
 					/*********************************
 					 * 4. Genre names => Equal?
 					 * 		=> Then sort by "created_at"
 					 *********************************/
 					
 					return (int)(c1.getCreated_at() - c2.getCreated_at());
 					
 				}//if (condition)
 				
 			}//public int compare(CL i1, CL i2)
 			
 		};//Comparator<CL> comp
 		
 		/*********************************
 		 * 2. Sort
 		 *********************************/
 		Collections.sort(cLList, comp);
 		
		// Log
		Log.d("Methods.java" + "["
				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
				+ "]", "Sort done: " + cLList.toString());
 		
 		return true;
 	}//public static boolean sort_list_CLList(Activity actv, List<CL> cLList)
 
 	public static boolean sort_item_list_by_status(Activity actv) {
 		/*********************************
 		 * 1. Comaparator
 		 * 2. Sort
 		 *********************************/
 		actv_methods = actv;
 		
 		/*********************************
 		 * 1. Comaparator
 		 *********************************/
 		Comparator<Item> comp = new Comparator<Item>(){
 
 			public int compare(Item i1, Item i2) {
 				/*********************************
 				 * 1. Get genre name
 				 * 2. Null?
 				 * 
 				 * 3. Genre names => Not equal?
 				 * 4. Genre names => Equal?
 				 *********************************/
 				int i1_status = i1.getStatus();
 				int i2_status = i2.getStatus();
 				
 				return i1_status - i2_status;
 //				return -(i1_status - i2_status);
 				
 			}//public int compare(Item i1, Item i2)
 			
 		};//Comparator<CL> comp
 		
 		/*********************************
 		 * 2. Sort
 		 *********************************/
 		Collections.sort(CheckActv.iList, comp);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "Sort done: " + CheckActv.iList.toString());
 		
 		return true;
 		
 	}//public static boolean sort_item_list_by_status(Activity actv)
 
 	public static boolean sort_item_list_by_serial_num(Activity actv) {
 		/*********************************
 		 * 1. Comaparator
 		 * 2. Sort
 		 *********************************/
 		actv_methods = actv;
 		
 		/*********************************
 		 * 1. Comaparator
 		 *********************************/
 		Comparator<Item> comp = new Comparator<Item>(){
 
 			public int compare(Item i1, Item i2) {
 				/*********************************
 				 * 1. Get genre name
 				 * 2. Null?
 				 * 
 				 * 3. Genre names => Not equal?
 				 * 4. Genre names => Equal?
 				 *********************************/
 				int i1_status = i1.getSerial_num();
 				int i2_status = i2.getSerial_num();
 				
 				return i1_status - i2_status;
 //				return -(i1_status - i2_status);
 				
 			}//public int compare(Item i1, Item i2)
 			
 		};//Comparator<CL> comp
 		
 		/*********************************
 		 * 2. Sort
 		 *********************************/
 		Collections.sort(CheckActv.iList, comp);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "Sort done: " + CheckActv.iList.toString());
 		
 		return true;
 		
 	}//public static boolean sort_item_list_by_serial_num(Activity actv)
 
 	/****************************************
 	 *
 	 * 
 	 * <Caller> 1. Methods.enterDir()
 	 * 
 	 * <Desc> 1. <Params> 1.
 	 * 
 	 * <Return> 1.
 	 * 
 	 * <Steps> 1.
 	 ****************************************/
 	public static boolean update_prefs_currentPath(Activity actv, String newPath) {
 		
 //		SharedPreferences prefs = 
 //				actv.getSharedPreferences(MainActv.prefs_current_path, MainActv.MODE_PRIVATE);
 //
 //		/*----------------------------
 //		 * 2. Get editor
 //			----------------------------*/
 //		SharedPreferences.Editor editor = prefs.edit();
 //
 //		/*----------------------------
 //		 * 3. Set value
 //			----------------------------*/
 //		editor.putString(MainActv.prefs_current_path, newPath);
 //		
 //		try {
 //			editor.commit();
 //			
 //			return true;
 //			
 //		} catch (Exception e) {
 //			
 //			// Log
 //			Log.d("Methods.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ "]", "Excption: " + e.toString());
 //			
 //			return false;
 //		}
 		return false;
 		
 	}//public static boolean update_prefs_current_path(Activity actv, Strin newPath)
 
 	/****************************************
 	 *
 	 * 
 	 * <Caller> 1. Methods.enterDir()
 	 * 
 	 * <Desc> 1. <Params> 1.
 	 * 
 	 * <Return> 1.
 	 * 
 	 * <Steps> 1.
 	 ****************************************/
 	public static boolean clear_prefs_currentPath(Activity actv, String newPath) {
 		
 //		SharedPreferences prefs = 
 //				actv.getSharedPreferences(MainActv.prefs_current_path, MainActv.MODE_PRIVATE);
 //
 //		/*----------------------------
 //		 * 2. Get editor
 //			----------------------------*/
 //		SharedPreferences.Editor editor = prefs.edit();
 //
 //		/*----------------------------
 //		 * 3. Clear
 //			----------------------------*/
 //		try {
 //			
 //			editor.clear();
 //			editor.commit();
 //			
 //			// Log
 //			Log.d("Methods.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ "]", "Prefs cleared");
 //			
 //			return true;
 //			
 //		} catch (Exception e) {
 //			
 //			// Log
 //			Log.d("Methods.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ "]", "Excption: " + e.toString());
 //			
 //			return false;
 //		}
 
 		return false;
 		
 	}//public static boolean clear_prefs_current_path(Activity actv, Strin newPath)
 
 	
 	/****************************************
 	 *
 	 * 
 	 * <Caller> 1. Methods.enterDir()
 	 * 
 	 * <Desc> 1. <Params> 1.
 	 * 
 	 * <Return> 1.
 	 * 
 	 * <Steps> 1.
 	 ****************************************/
 	public static String get_currentPath_from_prefs(Activity actv) {
 		
 //		SharedPreferences prefs = 
 //				actv.getSharedPreferences(MainActv.prefs_current_path, MainActv.MODE_PRIVATE);
 //
 //		return prefs.getString(MainActv.prefs_current_path, null);
 
 		return null;
 		
 	}//public static String get_currentPath_from_prefs(Activity actv)
 
 	public static void confirm_quit(Activity actv, int keyCode) {
 		
 		// TODO ꂽ\bhEX^u
 		if (keyCode==KeyEvent.KEYCODE_BACK) {
 			
 			AlertDialog.Builder dialog=new AlertDialog.Builder(actv);
 			
 	        dialog.setTitle("Av̏I");
 	        dialog.setMessage("AvI܂H");
 	        
 	        dialog.setPositiveButton("I",new DialogListener(actv, dialog, 0));
 	        dialog.setNegativeButton("LZ",new DialogListener(actv, dialog, 1));
 	        
 	        dialog.create();
 	        dialog.show();
 			
 		}//if (keyCode==KeyEvent.KEYCODE_BACK)
 		
 	}//public static void confirm_quit(Activity actv, int keyCode)
 
 	public static List<String> getTableList(Activity actv) {
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 
 		//=> source: http://stackoverflow.com/questions/4681744/android-get-list-of-tables : "Just had to do the same. This seems to work:"
 		String q = "SELECT name FROM " + "sqlite_master"+
 						" WHERE type = 'table' ORDER BY name";
 		
 		Cursor c = null;
 		try {
 			c = rdb.rawQuery(q, null);
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount(): " + c.getCount());
 
 		} catch (Exception e) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 		}
 		
 		// Table names list
 		List<String> tableList = new ArrayList<String>();
 		
 		// Log
 		if (c != null) {
 			c.moveToFirst();
 			
 			for (int i = 0; i < c.getCount(); i++) {
 				//
 				tableList.add(c.getString(0));
 				
 				// Log
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "c.getString(0): " + c.getString(0));
 				
 				
 				// Next
 				c.moveToNext();
 				
 			}//for (int i = 0; i < c.getCount(); i++)
 
 		} else {//if (c != null)
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c => null");
 		}//if (c != null)
 
 //		// Log
 //		Log.d("Methods.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ "]", "c.getCount(): " + c.getCount());
 //		
 		rdb.close();
 		
 		return tableList;
 		
 //		return null;
 	}//public static List<String> getTableList()
 
 	public static String[] get_column_list(Activity actv, String dbName, String tableName) {
 		/*********************************
 		 * 1. Set up db
 		 * 2. Cursor null?
 		 * 3. Get names
 		 * 
 		 * 4. Close db
 		 * 5. Return
 		 *********************************/
 		DBUtils dbu = new DBUtils(actv, dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 
 		//=> source: http://stackoverflow.com/questions/4681744/android-get-list-of-tables : "Just had to do the same. This seems to work:"
 		String q = "SELECT * FROM " + tableName;
 		
 		/*********************************
 		 * 2. Cursor null?
 		 *********************************/
 		Cursor c = null;
 		
 		try {
 			c = rdb.rawQuery(q, null);
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount(): " + c.getCount());
 
 		} catch (Exception e) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return null;
 		}
 		
 		/*********************************
 		 * 3. Get names
 		 *********************************/
 		String[] column_names = c.getColumnNames();
 		
 		/*********************************
 		 * 4. Close db
 		 *********************************/
 		rdb.close();
 		
 		/*********************************
 		 * 5. Return
 		 *********************************/
 		return column_names;
 		
 //		return null;
 	}//public static String[] get_column_list(Activity actv, String tableName)
 
 	/****************************************
 	 *		insertDataIntoDB()
 	 * 
 	 * <Caller> 
 	 * 1. private static boolean refreshMainDB_3_insert_data(Activity actv, Cursor c)
 	 * 
 	 * <Desc> 1. <Params> 1.
 	 * 
 	 * <Return> 1.
 	 * 
 	 * <Steps> 1.
 	 ****************************************/
 	private static int insertDataIntoDB(Activity actv, String tableName, Cursor c) {
 		/*----------------------------
 		 * Steps
 		 * 0. Set up db
 		 * 1. Move to first
 		 * 2. Set variables
 		 * 3. Obtain data
 		 * 4. Insert data
 		 * 5. Close db
 		 * 6. Return => counter
 			----------------------------*/
 //		/*----------------------------
 //		 * 0. Set up db
 //			----------------------------*/
 //		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 //		
 //		SQLiteDatabase wdb = dbu.getWritableDatabase();
 //		
 //		/*----------------------------
 //		 * 1. Move to first
 //			----------------------------*/
 //		c.moveToFirst();
 //
 //		/*----------------------------
 //		 * 2. Set variables
 //			----------------------------*/
 //		int counter = 0;
 //		int counter_failed = 0;
 //		
 //		/*----------------------------
 //		 * 3. Obtain data
 //			----------------------------*/
 //		for (int i = 0; i < c.getCount(); i++) {
 //
 //			String[] values = {
 //					String.valueOf(c.getLong(0)),
 //					c.getString(1),
 //					c.getString(2),
 //					String.valueOf(c.getLong(3)),
 //					String.valueOf(c.getLong(4))
 //			};
 //
 //			/*----------------------------
 //			 * 4. Insert data
 //			 * 		1. Insert data to tableName
 //			 * 		2. Record result
 //			 * 		3. Insert data to backupTableName
 //			 * 		4. Record result
 //				----------------------------*/
 //			boolean blResult = 
 //						dbu.insertData(wdb, tableName, DBUtils.cols_for_insert_data, values);
 //				
 //			if (blResult == false) {
 //				// Log
 //				Log.d("Methods.java"
 //						+ "["
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getLineNumber() + "]", "i => " + i + "/" + "c.getLong(0) => " + c.getLong(0));
 //				
 //				counter_failed += 1;
 //				
 //			} else {//if (blResult == false)
 //				counter += 1;
 //			}
 //
 //			//
 //			c.moveToNext();
 //			
 //			if (i % 100 == 0) {
 //				// Log
 //				Log.d("Methods.java" + "["
 //						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //						+ "]", "Done up to: " + i);
 //				
 //			}//if (i % 100 == 0)
 //			
 //		}//for (int i = 0; i < c.getCount(); i++)
 //		
 //		// Log
 //		Log.d("Methods.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ "]", "All data inserted: " + counter);
 //		
 //		/*----------------------------
 //		 * 5. Close db
 //			----------------------------*/
 //		wdb.close();
 //		
 //		/*----------------------------
 //		 * 6. Return => counter
 //			----------------------------*/
 //		//debug
 //		// Log
 //		Log.d("Methods.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ "]", "counter_failed(sum): " + counter_failed);
 //		
 //		return counter;
 
 		return -1;
 		
 	}//private static int insertDataIntoDB(Activity actv, Cursor c)
 
 	private static boolean insertDataIntoDB(
 			Activity actv, String tableName, String[] types, String[] values) {
 //		/*----------------------------
 //		* Steps
 //		* 1. Set up db
 //		* 2. Insert data
 //		* 3. Show message
 //		* 4. Close db
 //		----------------------------*/
 //		/*----------------------------
 //		* 1. Set up db
 //		----------------------------*/
 //		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 //		
 //		SQLiteDatabase wdb = dbu.getWritableDatabase();
 //		
 //		/*----------------------------
 //		* 2. Insert data
 //		----------------------------*/
 //		boolean result = dbu.insertData(wdb, tableName, types, values);
 //		
 //		/*----------------------------
 //		* 3. Show message
 //		----------------------------*/
 //		if (result == true) {
 //		
 //			// debug
 //			Toast.makeText(actv, "Data stored", Toast.LENGTH_SHORT).show();
 //			
 //			/*----------------------------
 //			* 4. Close db
 //			----------------------------*/
 //			wdb.close();
 //			
 //			return true;
 //			
 //		} else {//if (result == true)
 //		
 //			// debug
 //			Toast.makeText(actv, "Store data => Failed", 200).show();
 //			
 //			/*----------------------------
 //			* 4. Close db
 //			----------------------------*/
 //			wdb.close();
 //			
 //			return false;
 //		
 //		}//if (result == true)
 //		
 //		/*----------------------------
 //		* 4. Close db
 //		----------------------------*/
 
 		return false;
 		
 	}//private static boolean insertDataIntoDB()
 
 	public static boolean set_pref(Activity actv, String pref_name, String value) {
 //		SharedPreferences prefs = 
 //				actv.getSharedPreferences(pref_name, MainActv.MODE_PRIVATE);
 //
 //		/*----------------------------
 //		 * 2. Get editor
 //			----------------------------*/
 //		SharedPreferences.Editor editor = prefs.edit();
 //
 //		/*----------------------------
 //		 * 3. Set value
 //			----------------------------*/
 //		editor.putString(pref_name, value);
 //		
 //		try {
 //			editor.commit();
 //			
 //			return true;
 //			
 //		} catch (Exception e) {
 //			
 //			// Log
 //			Log.d("Methods.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ "]", "Excption: " + e.toString());
 //			
 //			return false;
 //		}
 
 		return false;
 	}//public static boolean set_pref(String pref_name, String value)
 
 	public static String get_pref(Activity actv, String pref_name, String defValue) {
 //		SharedPreferences prefs = 
 //				actv.getSharedPreferences(pref_name, MainActv.MODE_PRIVATE);
 //
 //		/*----------------------------
 //		 * Return
 //			----------------------------*/
 //		return prefs.getString(pref_name, defValue);
 		
 		return null;
 
 	}//public static boolean set_pref(String pref_name, String value)
 
 	public static boolean set_pref(Activity actv, String pref_name, int value) {
 //		SharedPreferences prefs = 
 //				actv.getSharedPreferences(pref_name, MainActv.MODE_PRIVATE);
 //
 //		/*----------------------------
 //		 * 2. Get editor
 //			----------------------------*/
 //		SharedPreferences.Editor editor = prefs.edit();
 //
 //		/*----------------------------
 //		 * 3. Set value
 //			----------------------------*/
 //		editor.putInt(pref_name, value);
 //		
 //		try {
 //			editor.commit();
 //			
 //			return true;
 //			
 //		} catch (Exception e) {
 //			
 //			// Log
 //			Log.d("Methods.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ "]", "Excption: " + e.toString());
 //			
 //			return false;
 //		}
 
 		return false;
 	}//public static boolean set_pref(String pref_name, String value)
 
 	public static int get_pref(Activity actv, String pref_name, int defValue) {
 //		SharedPreferences prefs = 
 //				actv.getSharedPreferences(pref_name, MainActv.MODE_PRIVATE);
 //
 //		/*----------------------------
 //		 * Return
 //			----------------------------*/
 //		return prefs.getInt(pref_name, defValue);
 
 		return -1;
 	}//public static boolean set_pref(String pref_name, String value)
 
 	public static Dialog dlg_template_cancel(Activity actv, int layoutId, int titleStringId,
 			int cancelButtonId, DialogButtonTags cancelTag) {
 		/*----------------------------
 		* Steps
 		* 1. Set up
 		* 2. Add listeners => OnTouch
 		* 3. Add listeners => OnClick
 		----------------------------*/
 		
 		// 
 		Dialog dlg = new Dialog(actv);
 		
 		//
 		dlg.setContentView(layoutId);
 		
 		// Title
 		dlg.setTitle(titleStringId);
 		
 		/*----------------------------
 		* 2. Add listeners => OnTouch
 		----------------------------*/
 		//
 		Button btn_cancel = (Button) dlg.findViewById(cancelButtonId);
 		
 		//
 		btn_cancel.setTag(cancelTag);
 		
 		//
 		btn_cancel.setOnTouchListener(new DialogButtonOnTouchListener(actv, dlg));
 		
 		/*----------------------------
 		* 3. Add listeners => OnClick
 		----------------------------*/
 		//
 		btn_cancel.setOnClickListener(new DialogButtonOnClickListener(actv, dlg));
 		
 		//
 		//dlg.show();
 		
 		return dlg;
 	
 	}//public static Dialog dlg_template_okCancel()
 
 	public static Dialog dlg_template_okCancel(Activity actv, int layoutId, int titleStringId,
 			int okButtonId, int cancelButtonId, DialogButtonTags okTag, DialogButtonTags cancelTag) {
 		/*----------------------------
 		* Steps
 		* 1. Set up
 		* 2. Add listeners => OnTouch
 		* 3. Add listeners => OnClick
 		----------------------------*/
 		
 		// 
 		Dialog dlg = new Dialog(actv);
 		
 		//
 		dlg.setContentView(layoutId);
 		
 		// Title
 		dlg.setTitle(titleStringId);
 		
 		/*----------------------------
 		* 2. Add listeners => OnTouch
 		----------------------------*/
 		//
 		Button btn_ok = (Button) dlg.findViewById(okButtonId);
 		Button btn_cancel = (Button) dlg.findViewById(cancelButtonId);
 		
 		//
 		btn_ok.setTag(okTag);
 		btn_cancel.setTag(cancelTag);
 		
 		//
 		btn_ok.setOnTouchListener(new DialogButtonOnTouchListener(actv, dlg));
 		btn_cancel.setOnTouchListener(new DialogButtonOnTouchListener(actv, dlg));
 		
 		/*----------------------------
 		* 3. Add listeners => OnClick
 		----------------------------*/
 		//
 		btn_ok.setOnClickListener(new DialogButtonOnClickListener(actv, dlg));
 		btn_cancel.setOnClickListener(new DialogButtonOnClickListener(actv, dlg));
 		
 		//
 		//dlg.show();
 		
 		return dlg;
 	
 	}//public static Dialog dlg_template_okCancel()
 
 	public static long getMillSeconds(int year, int month, int date) {
 		// Calendar
 		Calendar cal = Calendar.getInstance();
 		
 		// Set time
 		cal.set(year, month, date);
 		
 		// Date
 		Date d = cal.getTime();
 		
 		return d.getTime();
 		
 	}//private long getMillSeconds(int year, int month, int date)
 
 	/****************************************
 	 *	getMillSeconds_now()
 	 * 
 	 * <Caller> 
 	 * 1. ButtonOnClickListener # case main_bt_start
 	 * 
 	 * <Desc> 1. <Params> 1.
 	 * 
 	 * <Return> 1.
 	 * 
 	 * <Steps> 1.
 	 ****************************************/
 	public static long getMillSeconds_now() {
 		
 		Calendar cal = Calendar.getInstance();
 		
 		return cal.getTime().getTime();
 		
 	}//private long getMillSeconds_now(int year, int month, int date)
 
 	public static String get_TimeLabel(long millSec) {
 		
 		 SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd_HHmmss");
 		 
 		return sdf1.format(new Date(millSec));
 		
 	}//public static String get_TimeLabel(long millSec)
 
 	public static String  convert_millSeconds2digitsLabel(long millSeconds) {
 		/*----------------------------
 		 * Steps
 		 * 1. Prepare variables
 		 * 2. Build a string
 		 * 3. Return
 			----------------------------*/
 		/*----------------------------
 		 * 1. Prepare variables
 			----------------------------*/
 		int seconds = (int)(millSeconds / 1000);
 		
 		int minutes = seconds / 60;
 		
 		int digit_seconds = seconds % 60;
 		
 		/*----------------------------
 		 * 2. Build a string
 			----------------------------*/
 		StringBuilder sb = new StringBuilder();
 		
 		if (String.valueOf(minutes).length() < 2) {
 			
 			sb.append("0");
 			
 		}//if (String.valueOf(minutes).length() < 2)
 		
 		sb.append(String.valueOf(minutes));
 		sb.append(":");
 
 		if (String.valueOf(digit_seconds).length() < 2) {
 			
 			sb.append("0");
 			
 		}//if (String.valueOf(digit_seconds).length() < 2)
 
 		sb.append(String.valueOf(digit_seconds));
 		
 		/*----------------------------
 		 * 3. Return
 			----------------------------*/
 		return sb.toString();
 		
 	}//public static void  convert_millSeconds2digitsLabel()
 
 	public static String convert_millSec_to_DateLabel(long millSec) {
 		
 		 SimpleDateFormat sdf1 = new SimpleDateFormat("yy/MM/dd");
 		 
 		return sdf1.format(new Date(millSec));
 		
 	}//public static String get_TimeLabel(long millSec)
 
 	public static HashMap<String, Integer> convert_to_histogram(String[] data) {
 		/*----------------------------
 		 * 1. Get hash map
 		 * 2. Return
 			----------------------------*/
 		/*----------------------------
 		 * 1. Get hash map
 			----------------------------*/
 		HashMap<String, Integer> hm = new HashMap<String, Integer>();
 		
 		for (String item : data) {
 			
 			if (hm.get(item) == null) {
 				
 				hm.put(item, Integer.valueOf(1));
 				
 			} else {//if (hm.get(ary) == null)
 				
 				int val = hm.get(item);
 				
 				val += 1;
 				
 				hm.put(item, val);
 				
 			}//if (hm.get(ary) == null)
 			
 		}//for (String item : data)
 
 		/*----------------------------
 		 * 2. Return
 			----------------------------*/
 		return hm;
 		
 	}//public static HashMap<String, Integer> convert_to_histogram(String[] data)
 
 	public static void dlg_register(Activity actv) {
 		/*----------------------------
 		 * Steps
 		 * 1. Get a dialog
 		 * 2. List view
 		 * 3. Set listener => list
 		 * 9. Show dialog
 			----------------------------*/
 		 
 		Dialog dlg = dlg_template_cancel(actv, 
 				R.layout.dlg_register, R.string.generic_tv_register,
 				R.id.dlg_register_btn_cancel,
 				Methods.DialogButtonTags.dlg_generic_dismiss);
 		
 		/*----------------------------
 		 * 2. List view
 		 * 	1. Get view
 		 * 	1-2. Set tag to view
 		 * 
 		 * 	2. Prepare list data
 		 * 	3. Prepare adapter
 		 * 	4. Set adapter
 			----------------------------*/
 		ListView lv = (ListView) dlg.findViewById(R.id.dlg_register_lv);
 		
 		lv.setTag(Methods.DialogItemTags.dlg_register_lv);
 		
 		/*----------------------------
 		 * 2.2. Prepare list data
 			----------------------------*/
 		List<String> registerItems = new ArrayList<String>();
 		
 		registerItems.add(actv.getString(R.string.main_menu_register_list));
 		registerItems.add(actv.getString(R.string.main_menu_register_genre));
 		
 		ArrayAdapter<String> adp = new ArrayAdapter<String>(
 		
 				actv,
 				android.R.layout.simple_list_item_1,
 				registerItems
 		);
 		
 		/*----------------------------
 		 * 2.4. Set adapter
 			----------------------------*/
 		lv.setAdapter(adp);
 		
 		
 		
 		/*----------------------------
 		 * 3. Set listener => list
 			----------------------------*/
 		lv.setOnItemClickListener(
 						new DialogOnItemClickListener(
 								actv, 
 								dlg));
 		
 		/*----------------------------
 		 * 9. Show dialog
 			----------------------------*/
 		dlg.show();
 		
 	}//public static void dlg_register(Activity actv)
 
 	public static void dlg_register_genre(Activity actv, Dialog dlg) {
 		/*----------------------------
 		 * Steps
 		 * 1. Set up
 		 * 2. Add listeners => OnTouch
 		 * 3. Add listeners => OnClick
 			----------------------------*/
 		
 		// 
 		Dialog dlg2 = new Dialog(actv);
 		
 		//
 		dlg2.setContentView(R.layout.dlg_register_genre);
 		
 		// Title
 		dlg2.setTitle(R.string.dlg_register_genre_title);
 		
 		/*----------------------------
 		 * 2. Add listeners => OnTouch
 			----------------------------*/
 		//
 		Button btn_ok = 
 			(Button) dlg2.findViewById(R.id.dlg_register_genre_btn_ok);
 		Button btn_cancel = (Button) dlg2.findViewById(R.id.dlg_register_genre_btn_cancel);
 		
 		//
 		btn_ok.setTag(DialogButtonTags.dlg_register_genre_bt_ok);
 		btn_cancel.setTag(DialogButtonTags.dlg_generic_dismiss_second_dialog);
 		
 		//
 		btn_ok.setOnTouchListener(new DialogButtonOnTouchListener(actv, dlg2));
 		btn_cancel.setOnTouchListener(new DialogButtonOnTouchListener(actv, dlg2));
 		
 		/*----------------------------
 		 * 3. Add listeners => OnClick
 			----------------------------*/
 		//
 		btn_ok.setOnClickListener(
 					new DialogButtonOnClickListener(actv, dlg, dlg2));
 		btn_cancel.setOnClickListener(
 					new DialogButtonOnClickListener(actv, dlg, dlg2));
 		
 		//
 		dlg2.show();
 		
 	}//public static void dlg_register_genre(Activity actv, Dialog dlg)
 
 	public static void dlg_register_list(Activity actv, Dialog dlg) {
 		/*----------------------------
 		 * Steps
 		 * 1. Set up
 		 * 2. Add listeners => OnTouch
 		 * 3. Add listeners => OnClick
 		 * 
 		 * 4. Set spinner
 			----------------------------*/
 		
 		// 
 		Dialog dlg2 = new Dialog(actv);
 		
 		//
 		dlg2.setContentView(R.layout.dlg_register_list);
 		
 		// Title
 		dlg2.setTitle(R.string.dlg_register_list_title);
 		
 		/*----------------------------
 		 * 2. Add listeners => OnTouch
 			----------------------------*/
 		//
 		Button btn_ok = 
 			(Button) dlg2.findViewById(R.id.dlg_register_list_btn_ok);
 		Button btn_cancel = (Button) dlg2.findViewById(R.id.dlg_register_list_btn_cancel);
 		
 		//
 		btn_ok.setTag(DialogButtonTags.dlg_register_list_bt_ok);
 		btn_cancel.setTag(DialogButtonTags.dlg_generic_dismiss_second_dialog);
 		
 		//
 		btn_ok.setOnTouchListener(new DialogButtonOnTouchListener(actv, dlg2));
 		btn_cancel.setOnTouchListener(new DialogButtonOnTouchListener(actv, dlg2));
 		
 		/*----------------------------
 		 * 3. Add listeners => OnClick
 			----------------------------*/
 		//
 		btn_ok.setOnClickListener(
 					new DialogButtonOnClickListener(actv, dlg, dlg2));
 		btn_cancel.setOnClickListener(
 					new DialogButtonOnClickListener(actv, dlg, dlg2));
 		
 		/*********************************
 		 * 4. Set spinner
 		 * 	1. List
 		 * 	2. Adapter
 		 * 	3. Set adapter
 		 *********************************/
 		Spinner sp = (Spinner) dlg2.findViewById(R.id.dlg_register_list_sp);
 		
 		List<String> genreList = Methods.get_genre_list(actv);
 
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "genreList.size(): " + genreList.size());
 
 		/*********************************
 		 * 4.2. Adapter
 		 *********************************/
 		ArrayAdapter<String> adp = new ArrayAdapter<String>(
 	              actv, android.R.layout.simple_spinner_item, genreList);
 		
 		adp.setDropDownViewResource(
 				android.R.layout.simple_spinner_dropdown_item);
 		
 		/*********************************
 		 * 4.3. Set adapter
 		 *********************************/
 		sp.setAdapter(adp);
 		
 		//
 		dlg2.show();
 		
 	}//public static void dlg_register_list(Activity actv, Dialog dlg)
 
 	public static void dlg_register_item(Activity actv) {
 		/*----------------------------
 		 * Steps
 		 * 1. Get a dialog
 		 * 2. List view
 		 * 3. Set listener => list
 		 * 9. Show dialog
 			----------------------------*/
 		 
 		Dialog dlg = dlg_template_okCancel(
 						actv, R.layout.dlg_register_item,
 						R.string.dlg_rgstr_item_title,
 						
 						R.id.dlg_rgstr_item_btn_ok,
 						R.id.dlg_rgstr_item_btn_cancel,
 						
 						Methods.DialogButtonTags.dlg_rgstr_item_bt_ok,
 						Methods.DialogButtonTags.dlg_generic_dismiss
 						);
 		
 		/*----------------------------
 		 * 2. List view
 		 * 	1. Get view
 		 * 	1-2. Set tag to view
 		 * 
 		 * 	2. Prepare list data
 		 * 	3. Prepare adapter
 		 * 	4. Set adapter
 			----------------------------*/
 		/*----------------------------
 		 * 9. Show dialog
 			----------------------------*/
 		dlg.show();
 		
 	}//public static void dlg_register_item(Activity actv)
 
 	private static List<String> get_genre_list(Activity actv) {
 		/*********************************
 		 * 1. db
 		 * 2. Query
 		 * 2-2. If no entry => Return
 		 * 
 		 * 3. Build list
 		 * 
 		 *********************************/
 		
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 
 		String sql = "SELECT * FROM " + MainActv.tableName_genres;
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 		} catch (Exception e) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return null;
 		}
 		
 		/*********************************
 		 * 2-2. If no entry => Return
 		 *********************************/
 		if (c.getCount() < 1) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount() < 1");
 			
 			rdb.close();
 			
 			return null;
 			
 		}//if (c.getCount() < 1)
 		
 		
 		/*********************************
 		 * 3. Build list
 		 *********************************/
 		c.moveToFirst();
 		
 		List<String> genreList = new ArrayList<String>();
 		
 		for (int i = 0; i < c.getCount(); i++) {
 			
 			genreList.add(c.getString(3));
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getString(3): " + c.getString(3));
 			
 			c.moveToNext();
 			
 		}//for (int i = 0; i < c.getCount(); i++)
 		
 		rdb.close();
 		
 		return genreList;
 		
 	}//private static List<String> get_genre_list(Activity actv)
 
 	public static void register_genre(Activity actv, Dialog dlg, Dialog dlg2) {
 		/*********************************
 		 * 1. Get text
 		 * 2. db
 		 * 
 		 * 3. Insert data
 		 * 
 		 * 9. Close db
 		 * 
 		 *********************************/
 		EditText et = (EditText) dlg2.findViewById(R.id.dlg_register_genre_et);
 		
 		String genre_name = et.getText().toString();
 		
 		
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 		
 		
 
 		boolean res = dbu.insertData_genre(wdb, genre_name);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "res: " + res + "(" + genre_name + ")");
 		
 		/*********************************
 		 * 9. Close db
 		 *********************************/
 		wdb.close();
 		
 	}//public static void register_genre(Activity actv, Dialog dlg, Dialog dlg2)
 
 	public static void register_list(Activity actv, Dialog dlg, Dialog dlg2) {
 		/*********************************
 		 * 1. Get text
 		 * 2. db
 		 * 
 		 * 2-2. Get genre id
 		 * 
 		 * 3. Insert data
 		 * 
 		 * 9. Close db
 		 * 10. Dismiss dialogues
 		 * 
 		 * 11. Notify adapter
 		 * 
 		 *********************************/
 		/*********************************
 		 * 2-2. Get genre id
 		 *********************************/
 		Spinner sp = (Spinner) dlg2.findViewById(R.id.dlg_register_list_sp);
 		
 		String genre_name = (String) sp.getSelectedItem();
 
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "genre_name: " + genre_name);
 		
 		int genre_id = Methods.get_genre_id_from_genre_name(actv, genre_name);
 		
 		if (genre_id < 0) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "genre_id: " + genre_id);
 			
 			// debug
 			Toast.makeText(actv, "W擾ł܂", Toast.LENGTH_SHORT).show();
 			
 		}//if (genre_id < 0)
 		
 		/*********************************
 		 * 1. Get text
 		 *********************************/
 		EditText et = (EditText) dlg2.findViewById(R.id.dlg_register_list_et);
 		
 		String list_name = et.getText().toString();
 		
 		/*********************************
 		 * 2. db
 		 *********************************/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 		
 		/*********************************
 		 * 3. Insert data
 		 *********************************/
 		boolean res = dbu.insertData_list(wdb, list_name, genre_id);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "res: " + res + "(" + list_name + ")");
 		
 		/*********************************
 		 * 9. Close db
 		 *********************************/
 		wdb.close();
 		
 		/*********************************
 		 * 10. Dismiss dialogues
 		 *********************************/
 		dlg2.dismiss();
 		dlg.dismiss();
 		
 		/*********************************
 		 * 11. Notify adapter
 		 *********************************/
 		
 		
 	}//public static void register_list(Activity actv, Dialog dlg, Dialog dlg2)
 
 	public static void register_item(Activity actv, Dialog dlg) {
 		/*********************************
 		 * 1. Get text
 		 * 2. Get serial number
 		 * 
 		 * 3. Setup db
 		 * 
 		 * 4. Insert data
 		 * 
 		 * 9. Close db
 		 * 
 		 * 10. Dismiss dialog
 		 * 
 		 *********************************/
 		EditText et_text = (EditText) dlg.findViewById(R.id.dlg_rgstr_item_et_text);
 		
 		String text = et_text.getText().toString();
 		
 		/*********************************
 		 * 2. Get serial number
 		 *********************************/
 		EditText et_order = (EditText) dlg.findViewById(R.id.dlg_rgstr_item_et_order);
 		
 		int serial_num;
 		
 		if (et_order.getText().toString().equals("")) {
 			
 			serial_num = 
 					Methods.get_num_of_entries_items(actv, CheckActv.clList.getDb_id()) + 1;
 			
 		} else {//if (serial_num.equals(""))
 			
 			serial_num = Integer.parseInt(et_order.getText().toString());
 			
 		}//if (serial_num.equals(""))
 		
 		/*********************************
 		 * 3. Setup db
 		 *********************************/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 		
 		/*********************************
 		 * 4. Insert data
 		 *********************************/
 		Object[] data = {text, serial_num, CheckActv.clList.getDb_id()};
 		boolean res = dbu.insertData_item(wdb, data);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "res: " + res + "(" + (String) text + ")");
 		
 		/*********************************
 		 * 9. Close db
 		 *********************************/
 		wdb.close();
 		
 		/*********************************
 		 * 10. Dismiss dialog
 		 *********************************/
 		if (res == true) {
 
 			dlg.dismiss();
 			
 			// debug
 			Toast.makeText(actv, "Data stored", Toast.LENGTH_SHORT).show();
 
 			Methods.refresh_item_list(actv);
 			
 			return;
 			
 		} else {//if (res == true)
 			
 			// debug
 			Toast.makeText(actv, "Store data => Failed", 200).show();
 
 			return;
 			
 		}//if (res == true)
 		
 		
 		
 	}//public static void register_item(Activity actv, Dialog dlg)
 
 	/**********************************************
 	 * <Return>
 	 * -1	=> Exception in querying
 	 * >=0	=> Num of entries
 	 * @param list_id 
 	 **********************************************/
 	private static int get_num_of_entries_items(Activity actv, long list_id) {
 		/*********************************
 		 * memo
 		 *********************************/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 
 		//=> source: http://stackoverflow.com/questions/4681744/android-get-list-of-tables : "Just had to do the same. This seems to work:"
 		String q = "SELECT * FROM " + MainActv.tableName_items +
 				" WHERE " + MainActv.cols_items[2] + "='" + list_id + "'";;
 		
 		Cursor c = null;
 		
 		try {
 			c = rdb.rawQuery(q, null);
 			
 			int num_of_entries = c.getCount();
 			
 			rdb.close();
 			
 			return num_of_entries;
 			
 		} catch (Exception e) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return -1;
 			
 		}
 		
 //		return 0;
 	}//private static int get_num_of_entries_items(Activity actv)
 
 	/**********************************************
 	 * get_genre_id_from_genre_name(Activity actv, String genre_name)
 	 * 
 	 * <Return>
 	 * -1	=> Error, exception
 	 * -2	=> No entry
 	 **********************************************/
 	private static int get_genre_id_from_genre_name(Activity actv, String genre_name) {
 		/*********************************
 		 * 1. db
 		 * 2. Query
 		 *
 		 * 3. Get data
 		 * 4. Close db
 		 * 
 		 * 5. Return
 		 *********************************/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*********************************
 		 * 2. Query
 		 *********************************/
 		String sql = "SELECT * FROM " + MainActv.tableName_genres + 
 					" WHERE name='" + genre_name + "'";
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			actv.startManagingCursor(c);
 			
 		} catch (Exception e) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return -1;
 		}
 		
 		/*********************************
 		 * 2-2. If no entry => Return
 		 *********************************/
 		if (c.getCount() < 1) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount() < 1");
 			
 			rdb.close();
 			
 			return -2;
 			
 		}//if (c.getCount() < 1)
 		
 		
 		/*********************************
 		 * 3. Get data
 		 *********************************/
 		c.moveToFirst();
 		
 		int genre_id = (int) c.getLong(0);
 		
 		/*********************************
 		 * 4. Close db
 		 *********************************/
 		rdb.close();
 		
 		/*********************************
 		 * 5. Return
 		 *********************************/
 		return genre_id;
 		
 	}//private static int get_genre_id_from_genre_name(String genre_name)
 
 
 	/**********************************************
 	 * get_genre_name_from_genre_id(Activity actv, int genre_id)
 	 * 
 	 * <Return>
 	 * 	null	=> Cursor == null
 	 * 			=> c.getCount() < 1
 	 **********************************************/
 	public static String get_genre_name_from_genre_id(Activity actv, int genre_id) {
 		/*********************************
 		 * 1. db
 		 * 2. Query
 		 *
 		 * 3. Get data
 		 * 4. Close db
 		 * 
 		 * 5. Return
 		 *********************************/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*********************************
 		 * 2. Query
 		 *********************************/
 		String sql = "SELECT * FROM " + MainActv.tableName_genres + 
 					" WHERE " + android.provider.BaseColumns._ID + "='" + genre_id + "'";
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			actv.startManagingCursor(c);
 			
 		} catch (Exception e) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return null;
 		}
 		
 		/*********************************
 		 * 2-2. If no entry => Return
 		 *********************************/
 		if (c.getCount() < 1) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount() < 1");
 			
 			rdb.close();
 			
 			return null;
 			
 		}//if (c.getCount() < 1)
 		
 		
 		/*********************************
 		 * 3. Get data
 		 *********************************/
 		c.moveToFirst();
 		
 		String genre_name = (String) c.getString(3);
 		
 		/*********************************
 		 * 4. Close db
 		 *********************************/
 		rdb.close();
 		
 		/*********************************
 		 * 5. Return
 		 *********************************/
 		return genre_name;
 		
 	}//public static String get_genre_name_from_genre_id(int genre_id)
 
 	
 	public static CL get_clList_from_db_id(Activity actv, long list_id) {
 		/*********************************
 		 * 1. db
 		 * 2. Query
 		 *
 		 * 3. Get data
 		 * 4. Close db
 		 * 
 		 * 5. Return
 		 *********************************/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*********************************
 		 * 2. Query
 		 *********************************/
 		String sql = "SELECT * FROM " + MainActv.tableName_check_lists + 
 					" WHERE " + android.provider.BaseColumns._ID + "='" + list_id + "'";
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			actv.startManagingCursor(c);
 			
 		} catch (Exception e) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return null;
 		}
 		
 		/*********************************
 		 * 2-2. If no entry => Return
 		 *********************************/
 		if (c.getCount() < 1) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount() < 1");
 			
 			rdb.close();
 			
 			return null;
 			
 		}//if (c.getCount() < 1)
 		
 		
 		/*********************************
 		 * 3. Get data
 		 *********************************/
 		c.moveToFirst();
 		
 //		String genre_name = (String) c.getString(3);
 		
 		CL clList = new CL(
 				c.getString(3),
 				c.getInt(4),
 				
 				c.getLong(0),
 				c.getLong(1),
 				c.getLong(2)
 				);
 		
 		/*********************************
 		 * 4. Close db
 		 *********************************/
 		rdb.close();
 		
 		/*********************************
 		 * 5. Return
 		 *********************************/
 		return clList;
 		
 	}//public static CL get_clList_from_db_id(Activity actv, long list_id)
 
 	public static Cursor select_all_from_table(Activity actv, SQLiteDatabase rdb, String tableName) {
 		/*********************************
 		 * 
 		 *********************************/
 		/********************************
 		 * 2. Query
 		 ********************************/
 		String sql = "SELECT * FROM " + tableName;
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			actv.startManagingCursor(c);
 
 			return c;
 			
 		} catch (Exception e) {
 			// Log
 			Log.d("MainActv.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return null;
 		}
 		
 	}//public static select_all_from_table(Activity actv, String tableName)
 
 	/**********************************************
 	 * <Return>
 	 * 	null	=> Query exception
 	 * 			=> No entry
 	 **********************************************/
 	public static List<Item> get_item_list_from_check_list(
 					Activity actv, long list_id) {
 		/*********************************
 		 * 
 		 *********************************/
 		/*********************************
 		 * 1. db
 		 * 2. Query
 		 *
 		 * 3. Get data
 		 * 4. Close db
 		 * 
 		 * 5. Return
 		 *********************************/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*********************************
 		 * 2. Query
 		 *********************************/
 		String sql = "SELECT * FROM " + MainActv.tableName_items + 
 //					" WHERE " + MainActv.cols_items[5] + "='" + list_id + "'";
 				" WHERE " + MainActv.cols_items[2] + "='" + list_id + "'";
 
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			actv.startManagingCursor(c);
 			
 		} catch (Exception e) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return null;
 		}
 		
 		/*********************************
 		 * 2-2. If no entry => Return
 		 *********************************/
 		if (c.getCount() < 1) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount() < 1");
 			
 			rdb.close();
 			
 			return null;
 			
 		}//if (c.getCount() < 1)
 		
 		
 		/*********************************
 		 * 3. Get data
 		 *********************************/
 		c.moveToFirst();
 
 		List<Item> iList = new ArrayList<Item>();
 		
 		for (int i = 0; i < c.getCount(); i++) {
 			
 			iList.add(new Item(
 					c.getString(3),
 					c.getInt(4),
 					c.getLong(5),
 					
 					c.getLong(0),
 					c.getLong(1),
 					c.getLong(2)
 					));
 			
 			c.moveToNext();
 			
 		}//for (int i = 0; i < c.getCount(); i++)
 		
 		/*********************************
 		 * 4. Close db
 		 *********************************/
 		rdb.close();
 		
 		/*********************************
 		 * 5. Return
 		 *********************************/
 		return iList;
 		
 	}//public static List<Item> get_item_list_from_check_list
 
 	private static boolean refresh_item_list(Activity actv) {
 		/*********************************
 		 * 1. Build list
 		 * 
 		 * 2. Notify adapter
 		 * 
 		 * 3. Return
 		 *********************************/
 		/*********************************
 		 * 1.2. Build list
 		 *********************************/
 		if (CheckActv.iList != null) {
 		
 			CheckActv.iList.clear();
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "CheckActv.iList => Cleared");
 			
 		}//if (CheckActv.iList != null)
 		
 		CheckActv.iList.addAll(
 			Methods.get_item_list_from_check_list(
 					actv, 
 					CheckActv.clList.getDb_id()));
 
 		// Log
 		if (CheckActv.iList == null) {
 
 			Log.d("CheckActv.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "CheckActv.iList => Null");
 			
 			// debug
 			Toast.makeText(actv,
 					"Query exception, or, no items " +
 					"for this check list, yet",
 					Toast.LENGTH_SHORT).show();
 
 			return false;
 
 		} else {//if (CheckActv.iList == null)
 			
 			// Log
 			Log.d("CheckActv.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "CheckActv.iList.size(): " + CheckActv.iList.size());
 			
 		}//if (CheckActv.iList == null)
 		
 		/*********************************
 		 * 2. Notify adapter
 		 *********************************/
 		CheckActv.ilAdp = new ItemListAdapter(
 				actv,
 				R.layout.list_row_item_list,
 				CheckActv.iList
 				);
 		
 		((ListActivity) actv).setListAdapter(CheckActv.ilAdp);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "Adapter => Re-set");
 		
 //		CheckActv.ilAdp.notifyDataSetChanged();
 //
 //		// Log
 //		Log.d("Methods.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ "]", "CheckActv.ilAdp => Notified");
 		
 		/*********************************
 		 * 3. Return
 		 *********************************/
 		return true;
 
 	}//private void refresh_item_list()
 
 	public static void change_item_status(Activity actv, Item item) {
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "item.getStatus(): " + item.getStatus());
 		
 		int status = item.getStatus();
 		
 		if (status == 5) {
 			
 			item.setStatus(0);
 			
 		} else {//if (status == 5)
 		
 			item.setStatus(status + 1);
 			
 		}//if (status == 5)
 		
 		CheckActv.ilAdp.notifyDataSetChanged();
 
 		
 	}//public static void change_item_status(Activity actv, Item item)s
 
 	
 	public static void dlg_checkactv_long_click(Activity actv, int item_position) {
 		/*********************************
 		 * 1. Dialog
 		 * 2. List view
 		 * 3. Show dialog
 		 *********************************/
 		Dialog dlg = dlg_template_cancel(actv, 
 				R.layout.dlg_checkactv_long_click, R.string.dlg_checkactv_long_click_title,
 				R.id.dlg_checkactv_long_click_bt_cancel,
 				Methods.DialogButtonTags.dlg_generic_dismiss);
 
 		/*----------------------------
 		 * 2. List view
 		 * 	1. Get view
 		 * 	1-2. Set tag to view
 		 * 
 		 * 	2. Prepare list data
 		 * 	3. Prepare adapter
 		 * 	4. Set adapter
 			----------------------------*/
 		ListView lv = (ListView) dlg.findViewById(R.id.dlg_checkactv_long_click_lv);
 		
 		lv.setTag(Methods.DialogItemTags.dlg_checkactv_long_click_lv);
 		
 		/*----------------------------
 		 * 2.2. Prepare list data
 			----------------------------*/
 		List<String> long_click_items = new ArrayList<String>();
 		
 		long_click_items.add(actv.getString(R.string.dlg_checkactv_long_click_lv_edit));
 		long_click_items.add(actv.getString(R.string.dlg_checkactv_long_click_lv_change_serial_num));
 		
 		ArrayAdapter<String> adp = new ArrayAdapter<String>(
 		
 				actv,
 				android.R.layout.simple_list_item_1,
 				long_click_items
 		);
 		
 		/*----------------------------
 		 * 2.4. Set adapter
 			----------------------------*/
 		lv.setAdapter(adp);
 		
 		/*----------------------------
 		 * 3. Set listener => list
 			----------------------------*/
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "item_position: " + item_position);
 
 		
 		lv.setOnItemClickListener(
 						new DialogOnItemClickListener(
 								actv, 
 								dlg, item_position));
 		
 		/*********************************
 		 * 3. Show dialog
 		 *********************************/
 		dlg.show();
 		
 	}//public static void dlg_checkactv_long_click(Activity actv)
 
 	public static void dlg_checkactv_long_click_lv_change_serial_num(
 			Activity actv, Dialog dlg, int item_position) {
 		/*********************************
 		 * memo
 		 *********************************/
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "item_position: " + item_position);
 
 		
 		Dialog dlg2 = new Dialog(actv);
 		
 		//
 		dlg2.setContentView(R.layout.dlg_checkactv_change_serial_num);
 		
 		// Title
 		dlg2.setTitle(R.string.dlg_checkactv_change_serial_num_title);
 
 		/*********************************
 		 * Get views
 		 *********************************/
 		//
 		Button btn_ok = 
 			(Button) dlg2.findViewById(R.id.dlg_checkactv_change_serial_num_btn_ok);
 		
 		Button btn_cancel = 
 				(Button) dlg2.findViewById(R.id.dlg_checkactv_change_serial_num_btn_cancel);
 		
 		/*********************************
 		 * Set tags
 		 *********************************/
 		//
 		btn_ok.setTag(
 				Methods.DialogButtonTags.dlg_checkactv_change_serial_num_btn_ok);
 		
 		btn_cancel.setTag(
 				Methods.DialogButtonTags.dlg_generic_dismiss_second_dialog);
 		
 		/*----------------------------
 		* 2. Add listeners => OnTouch
 		----------------------------*/
 		//
 		btn_ok.setOnTouchListener(new DialogButtonOnTouchListener(actv, dlg));
 		btn_cancel.setOnTouchListener(new DialogButtonOnTouchListener(actv, dlg));
 		
 		/*----------------------------
 		* 3. Add listeners => OnClick
 		----------------------------*/
 		//
 		btn_ok.setOnClickListener(new DialogButtonOnClickListener(actv, dlg, dlg2, item_position));
 		btn_cancel.setOnClickListener(
 					new DialogButtonOnClickListener(actv, dlg, dlg2));
 		
 
 //		Dialog dlg2 = dlg_template_okCancel(
 //				actv, R.layout.dlg_checkactv_change_serial_num,
 //				R.string.dlg_checkactv_change_serial_num_title,
 //				
 //				R.id.dlg_checkactv_change_serial_num_btn_ok,
 //				R.id.dlg_checkactv_change_serial_num_btn_cancel,
 //
 //				Methods.DialogButtonTags.dlg_checkactv_change_serial_num_btn_ok,
 //				Methods.DialogButtonTags.dlg_generic_dismiss_second_dialog
 //				);
 
 
 		dlg2.show();
 
 		
 	}//public static void dlg_checkactv_long_click_lv_change_serial_num
 
 	public static boolean is_numeric(String num_string) {
 		
 //		return num_string.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+ ");
 		return num_string.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
 
 	}//public static boolean is_numeric(String num_string)
 
 	public static void checkactv_change_order(Activity actv, Dialog dlg,
 			Dialog dlg2, int item_position) {
 		/*********************************
 		 * 1. Get new position
 		 * 2. Change order
 		 * 
 		 * 3. Sort list
 		 * 4. Notify adapter
 		 * 
 		 * 5. Dismiss dialogues
 		 * 
 		 * 6. Update db
 		 *********************************/
 		
 		checkactv_change_order_1_change_order(actv, dlg, dlg2, item_position);
 		
 		checkactv_change_order_2_update_data(actv, dlg, dlg2, item_position);
 		
 //		/*********************************
 //		 * 1. Get new position
 //		 *********************************/
 //		// Log
 //		Log.d("Methods.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ "]", "item_position: " + item_position);
 //		
 //		EditText et = (EditText) dlg2.findViewById(R.id.dlg_checkactv_change_serial_num_et_new);
 //		
 ////		// debug
 ////		Toast.makeText(actv, et.getText().toString(), Toast.LENGTH_SHORT).show();
 //		
 //		int new_position = Integer.parseInt(et.getText().toString());
 //		
 //		/*********************************
 //		 * 2. Change order
 //		 *********************************/
 //		if (new_position >= item_position) {
 //			
 //			int i;
 //			
 //			// Items before the target
 ////			for (i = item_position + 1; i <= new_position; i++) {
 //			for (i = item_position + 1; i < new_position; i++) {
 //				
 //				CheckActv.iList.get(i).setSerial_num(
 //								CheckActv.iList.get(i).getSerial_num() - 1);
 //				
 //			}//for (int i = item_position + 1; i < new_position; i++)
 //			
 //			// Target
 //			CheckActv.iList.get(item_position).setSerial_num(new_position);
 //			
 //			// Increment
 ////			i += 1;
 //			
 ////			// Items before the target
 ////			for (; i < CheckActv.iList.size(); i++) {
 ////				
 ////				CheckActv.iList.get(i).setSerial_num(
 ////								CheckActv.iList.get(i).getSerial_num() + 1);
 ////				
 ////			}//for (int i = item_position + 1; i < new_position; i++)
 //			
 //		} else if (new_position < item_position) {//if (new_position >= item_position)
 //
 //			int i;
 //			
 //			for (i = new_position ; i < item_position; i++) {
 //				
 //				CheckActv.iList.get(i).setSerial_num(
 //								CheckActv.iList.get(i).getSerial_num() + 1);
 //				
 //			}//for (int i = item_position + 1; i < new_position; i++)
 //			
 //			// Target
 ////			CheckActv.iList.get(item_position).setSerial_num(new_position);
 //			CheckActv.iList.get(item_position).setSerial_num(new_position + 1);
 //			
 //		} else {//if (new_position >= item_position)
 //			
 //			
 //			
 //		}//if (new_position >= item_position)
 //		
 //		/*********************************
 //		 * 3. Sort list
 //		 *********************************/
 //		sort_item_list_by_serial_num(actv);
 //		
 //		/*********************************
 //		 * 4. Notify adapter
 //		 *********************************/
 //		CheckActv.ilAdp.notifyDataSetChanged();
 //		
 //		/*********************************
 //		 * 5. Dismiss dialogues
 //		 *********************************/
 //		dlg.dismiss();
 //		dlg2.dismiss();
 		
 	}//public static void checkactv_change_order()
 
 	public static void checkactv_change_order_1_change_order(Activity actv, Dialog dlg,
 			Dialog dlg2, int item_position) {
 		/*********************************
 		 * 1. Get new position
 		 * 2. Change order
 		 * 
 		 * 3. Sort list
 		 * 4. Notify adapter
 		 * 
 		 * 5. Dismiss dialogues
 		 * 
 		 * 6. Update db
 		 *********************************/
 		
 		/*********************************
 		 * 1. Get new position
 		 *********************************/
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "item_position: " + item_position);
 		
 		EditText et = (EditText) dlg2.findViewById(R.id.dlg_checkactv_change_serial_num_et_new);
 		
 //		// debug
 //		Toast.makeText(actv, et.getText().toString(), Toast.LENGTH_SHORT).show();
 		
 		int new_position = Integer.parseInt(et.getText().toString());
 		
 		/*********************************
 		 * 2. Change order
 		 *********************************/
 		if (new_position >= item_position) {
 			
 			int i;
 			
 			// Items before the target
 			for (i = item_position + 1; i <= new_position; i++) {
 //			for (i = item_position + 1; i < new_position; i++) {
 				
 				CheckActv.iList.get(i).setSerial_num(
 								CheckActv.iList.get(i).getSerial_num() - 1);
 				
 			}//for (int i = item_position + 1; i < new_position; i++)
 			
 			// Target
 //			CheckActv.iList.get(item_position).setSerial_num(new_position);
 			CheckActv.iList.get(item_position).setSerial_num(new_position + 1);
 			
 			// Increment
 //			i += 1;
 			
 //			// Items before the target
 //			for (; i < CheckActv.iList.size(); i++) {
 //				
 //				CheckActv.iList.get(i).setSerial_num(
 //								CheckActv.iList.get(i).getSerial_num() + 1);
 //				
 //			}//for (int i = item_position + 1; i < new_position; i++)
 			
 		} else if (new_position < item_position) {//if (new_position >= item_position)
 
 			int i;
 			
 			for (i = new_position ; i < item_position; i++) {
 				
 				CheckActv.iList.get(i).setSerial_num(
 								CheckActv.iList.get(i).getSerial_num() + 1);
 				
 			}//for (int i = item_position + 1; i < new_position; i++)
 			
 			// Target
 //			CheckActv.iList.get(item_position).setSerial_num(new_position);
 			CheckActv.iList.get(item_position).setSerial_num(new_position + 1);
 			
 		} else {//if (new_position >= item_position)
 			
 			
 			
 		}//if (new_position >= item_position)
 		
 		/*********************************
 		 * 3. Sort list
 		 *********************************/
 		sort_item_list_by_serial_num(actv);
 		
 		/*********************************
 		 * 4. Notify adapter
 		 *********************************/
 		CheckActv.ilAdp.notifyDataSetChanged();
 		
 		/*********************************
 		 * 5. Dismiss dialogues
 		 *********************************/
 		dlg.dismiss();
 		dlg2.dismiss();
 		
 	}//public static void checkactv_change_order()
 
 	public static void checkactv_change_order_2_update_data(Activity actv, Dialog dlg,
 			Dialog dlg2, int item_position) {
 		/*********************************
 		 * 6. Update db
 		 *********************************/
 
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "checkactv_change_order_2_update_data => Starts");
 		/*********************************
 		 * 6. Update db
 		 *********************************/
 		HashMap<Long, Integer> data = new HashMap<Long, Integer>();
 		
 		for (int i = 0; i < CheckActv.iList.size(); i++) {
 			
 			data.put(CheckActv.iList.get(i).getDb_id(), CheckActv.iList.get(i).getSerial_num());
 			
 		}//for (int i = 0; i < CheckActv.iList.size(); i++)
 		
 		boolean res = DBUtils.updateData_items(
 							actv, MainActv.dbName, MainActv.tableName_items, data);
 		
 		
 	}//public static void checkactv_change_order()
 
 }//public class Methods
 
