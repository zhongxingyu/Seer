 package ifm9.utils;
 
 import ifm9.items.TI;
 import ifm9.listeners.DialogButtonOnClickListener;
 import ifm9.listeners.DialogButtonOnTouchListener;
 import ifm9.listeners.DialogListener;
 import ifm9.main.MainActv;
 import ifm9.main.R;
 import ifm9.main.TNActv;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
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
 import android.widget.TextView;
 import android.widget.Toast;
 
 import android.os.AsyncTask;
 
 import org.apache.commons.lang.StringUtils;
 
 public class Methods {
 
 	static int counter;		// Used => sortFileList()
 	
 	
 	/****************************************
 	 * Enums
 	 ****************************************/
 	public static enum DialogTags {
 		// Generics
 		dlg_generic_dismiss, dlg_generic_dismiss_second_dialog,
 		
 		
 		// dlg_create_folder.xml
 		dlg_create_folder_ok, dlg_create_folder_cancel,
 
 		// dlg_input_empty.xml
 		dlg_input_empty_reenter, dlg_input_empty_cancel,
 		
 		// dlg_confirm_create_folder.xml
 		dlg_confirm_create_folder_ok, dlg_confirm_create_folder_cancel,
 
 		// dlg_confirm_remove_folder.xml
 		dlg_confirm_remove_folder_ok, dlg_confirm_remove_folder_cancel,
 
 		// dlg_drop_table.xml
 		dlg_drop_table_btn_cancel, dlg_drop_table,
 		
 		// dlg_confirm_drop.xml
 		dlg_confirm_drop_table_btn_ok, dlg_confirm_drop_table_btn_cancel,
 		
 		// dlg_add_memos.xml
 		dlg_add_memos_bt_add, dlg_add_memos_bt_cancel, dlg_add_memos_bt_patterns,
 		dlg_add_memos_gv,
 
 		// dlg_move_files.xml
 		dlg_move_files_move, dlg_move_files,
 		
 		// dlg_confirm_move_files.xml	=> ok, cancel, dlg tag
 		dlg_confirm_move_files_ok, dlg_confirm_move_files_cancel, dlg_confirm_move_files,
 
 		// dlg_item_menu.xml
 		dlg_item_menu_bt_cancel, dlg_item_menu,
 
 		// dlg_create_table.xml
 		dlg_create_table_bt_create,
 
 		// dlg_memo_patterns.xml
 		dlg_memo_patterns,
 		
 		// dlg_register_patterns.xml
 		dlg_register_patterns_register,
 
 		// dlg_search.xml
 		dlg_search, dlg_search_ok,
 		
 		
 	}//public static enum DialogTags
 	
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
 		
 	}//public static enum ListTags
 
 	
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
 
 	/****************************************
 	 *
 	 * 
 	 * <Caller> 1. MainActv.onListItemClick()
 	 * 
 	 * <Desc> 1. <Params> 1.
 	 * 
 	 * <Return> 1.
 	 * 
 	 * <Steps> 1.
 	 ****************************************/
 	public static void enterDir(Activity actv, File newDir) {
 		/*----------------------------
 		 * Steps
 		 * 1. Update the current path
 		 * 2. Refresh list view
 		 * 3. Update path view
 		 * 
 		 * 
 			----------------------------*/
 		/*----------------------------
 		 * 1. Update the current path
 			----------------------------*/
 		
 		MainActv.dirPath_current = newDir.getAbsolutePath();
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "MainActv.dirPath_current: " + MainActv.dirPath_current);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "Calling: Methods.update_prefs_currentPath(actv, MainActv.dirPath_current)");
 		
 		
 		Methods.update_prefs_currentPath(actv, MainActv.dirPath_current);
 		
 		/*----------------------------
 		 * 2. Refresh list view
 			----------------------------*/
 		refreshListView(actv);
 		
 		/*----------------------------
 		 * 3. "Up" button => Enable
 			----------------------------*/
 		ImageButton ib = (ImageButton) actv.findViewById(R.id.v1_bt_up);
 		
 		if (!ib.isEnabled()) {
 			
 			ib.setEnabled(true);
 			
 			ib.setImageResource(R.drawable.ifm8_up);
 			
 			
 		}//if (!ib.isEnabled())
 		
 		/*----------------------------
 		 * 3. Update path view
 			----------------------------*/
 		updatePathLabel(actv);
 		
 		
 	}//public static void enterDir(Activity actv, File newDir)
 
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
 		
 		SharedPreferences prefs = 
 				actv.getSharedPreferences(MainActv.prefs_current_path, MainActv.MODE_PRIVATE);
 
 		/*----------------------------
 		 * 2. Get editor
 			----------------------------*/
 		SharedPreferences.Editor editor = prefs.edit();
 
 		/*----------------------------
 		 * 3. Set value
 			----------------------------*/
 		editor.putString(MainActv.prefs_current_path, newPath);
 		
 		try {
 			editor.commit();
 			
 			return true;
 			
 		} catch (Exception e) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Excption: " + e.toString());
 			
 			return false;
 		}
 
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
 		
 		SharedPreferences prefs = 
 				actv.getSharedPreferences(MainActv.prefs_current_path, MainActv.MODE_PRIVATE);
 
 		/*----------------------------
 		 * 2. Get editor
 			----------------------------*/
 		SharedPreferences.Editor editor = prefs.edit();
 
 		/*----------------------------
 		 * 3. Clear
 			----------------------------*/
 		try {
 			
 			editor.clear();
 			editor.commit();
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Prefs cleared");
 			
 			return true;
 			
 		} catch (Exception e) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Excption: " + e.toString());
 			
 			return false;
 		}
 
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
 		
 		SharedPreferences prefs = 
 				actv.getSharedPreferences(MainActv.prefs_current_path, MainActv.MODE_PRIVATE);
 
 		return prefs.getString(MainActv.prefs_current_path, null);
 		
 	}//public static String get_currentPath_from_prefs(Activity actv)
 
 	
 	/****************************************
 	 * 		refreshListView(Activity actv)
 	 * 
 	 * <Caller> 
 	 * 1. Methods.enterDir()
 	 * 
 	 * <Desc> 
 	 * 1. 
 	 * 
 	 * 
 	 * <Params> 1.
 	 * 
 	 * <Return> 1.
 	 * 
 	 * <Steps> 1.
 	 ****************************************/
 	public static void refreshListView(Activity actv) {
 		/*----------------------------
 		 * Steps
 		 * 1. Get currentPath
 		 * 2. Get file array
 		 * 3. Sort file array
 		 * 
 		 * 4. Add file names to list
 		 * 5. Notify adapter of changes
 		 * 6. Update image buttons
 		 * 
 		 * 
 		 * 1. Get file list
 		 * 1-2. Sort list
 		 * 2. Clear => ImageFileManager8Activity.file_names
 		 * 3. Add file names to => ImageFileManager8Activity.file_names
 		 * 4. Notify adapter of changes
 		 * 
 		 * 
 		 * 
 		 * 5. Update image buttons
 			----------------------------*/
 		/*----------------------------
 		 * 1. Get currentPath
 			----------------------------*/
 		String currentPath = Methods.get_currentPath_from_prefs(actv);
 		
 		if (currentPath == null) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Methods.get_currentPath_from_prefs(actv) => null");
 			
 			return;
 			
 		}//if (currentPath == null)
 		// 
 		/*----------------------------
 		 * 2. Get file array
 			----------------------------*/
 		File currentDir = new File(currentPath);
 		
 		File[] files = currentDir.listFiles();
 		
 		/*----------------------------
 		 * 3. Sort file array
 			----------------------------*/
 		sortFileList(files);
 		
 		/*----------------------------
 		 * 4. Add file names to list
 			----------------------------*/
 		// Reset file_names
 		if(MainActv.file_names != null) {
 			
 			MainActv.file_names.clear();
 			
 		} else {
 			
 			MainActv.file_names = new ArrayList<String>();
 			
 		}
 		
 		// Add names
 		for (File item : files) {
 			
 			MainActv.file_names.add(item.getName());
 			
 		}
 		
 		/*----------------------------
 		 * 5. Notify adapter of changes
 			----------------------------*/
 		if (MainActv.adapter != null) {
 			
 			MainActv.adapter.notifyDataSetChanged();
 			
 		} else {//if (condition)
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "MainActv.adapter => null");
 			
 			// debug
 			Toast.makeText(actv, "MainActv.adapter => null", 3000).show();
 
 		}//if (condition)
 		
 		/*----------------------------
 		 * 6. Update image buttons
 			----------------------------*/
 		update_image_buttons(actv, currentPath);
 		
 		
 	}//private static void refreshListView()
 
 	private static void update_image_buttons(Activity actv, String currentPath) {
 		
 		ImageButton ib = (ImageButton) actv.findViewById(R.id.v1_bt_up);
 		
 		if (currentPath.equals(MainActv.dirPath_base)) {
 			
 			ib.setImageResource(R.drawable.ifm8_up_disenabled);
 			ib.setEnabled(false);
 			
 		} else {//if (currentPath.equals(MainActv.dirPath_base))
 
 			ib.setImageResource(R.drawable.ifm8_up);
 			ib.setEnabled(true);
 			
 		}//if (currentPath.equals(MainActv.dirPath_base))
 		
 	}//private static void update_image_buttons()
 
 	/****************************************
 	 *
 	 * 
 	 * <Caller> 1.  Methods.enterDir(this, target)
 	 * 
 	 * <Desc> 1. <Params> 1.
 	 * 
 	 * <Return> 1.
 	 * 
 	 * <Steps> 1.
 	 ****************************************/
 	public static void updatePathLabel(Activity actv) {
 		// 
 		TextView tv = (TextView) actv.findViewById(R.id.v1_tv_dir_path);
 		
 		tv.setText(convert_prefs_into_path_label(actv));
 		
 	}//public static void updatePathLabel(Activity actv)
 
 	/****************************************
 	 *
 	 * 
 	 * <Caller> 1. Methods.updatePathLabel(Activity actv)
 	 * 
 	 * <Desc> 1. <Params> 1.
 	 * 
 	 * <Return> 1.
 	 * 
 	 * <Steps> 1.
 	 ****************************************/
 //	public static String getCurrentPathLabel(Activity actv) {
 	public static String convert_prefs_into_path_label(Activity actv) {
 		/*----------------------------
 		 * 1. Prep => pathArray, currentBaseDirName
 		 * 2. Detect loation of "IFM8"
 		 * 3. Build path label
 			----------------------------*/
 		
 		String currentPath = Methods.get_currentPath_from_prefs(actv);
 		
 		String[] pathArray = currentPath.split(File.separator);
 		
 		String currentBaseDirName = pathArray[pathArray.length - 1];
 		
 		/*----------------------------
 		 * 2. Detect loation of "IFM8"
 			----------------------------*/
 		int location = -1;
 		
 		for (int i = 0; i < pathArray.length; i++) {
 			if (pathArray[i].equals(MainActv.dirName_base)) {
 				location = i;
 				break;
 			}//if (pathArray[i].equals(ImageFileManager8Activity.baseDirName))
 		}//for (int i = 0; i < pathArray.length; i++)
 		
 		/*----------------------------
 		 * 3. Build path label
 			----------------------------*/
 		//REF=> http://stackoverflow.com/questions/4439595/how-to-create-a-sub-array-from-another-array-in-java
 		String[] newPath = Arrays.copyOfRange(pathArray, location, pathArray.length);
 		
 		String s_newPath = StringUtils.join(newPath, File.separator);
 		
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "s_newPath => " + s_newPath);
 		
 		return s_newPath;
 		
 	}//public static String getCurrentPathLabel(Activity actv)
 
 	public static void upDir(Activity actv) {
 		/*----------------------------
 		 * Steps
 		 * 1. Get the current path from preference
 		 * 2. Is the current path "roof"?
 		 * 3. Go up the path
 		 * 3-2. New path => Equal to base dir path?
 		 * 4. Refresh list
 		 * 5. Update path view
 			----------------------------*/
 		/*----------------------------
 		 * 1. Get the current path from preference
 			----------------------------*/
 		String currentPath = Methods.get_currentPath_from_prefs(actv);
 		
 		/*----------------------------
 		 * 2. Is the current path "roof"?
 			----------------------------*/
 		if (currentPath.equals(MainActv.dirPath_base)) {
 			
 			// debug
 			Toast.makeText(actv, "gbvEtH_ɂ܂", 2000).show();
 		
 			return;
 		}//if (ImageFileManager8Activity.currentDirPath == ImageFileManager8Activity.baseDirPath)
 		
 		/*----------------------------
 		 * 3. Update the current path
 			----------------------------*/
 		Methods.update_prefs_currentPath(actv, new File(currentPath).getParent());
 		
 //		ImageFileManager8Activity.currentDirPath = 
 //						(new File(ImageFileManager8Activity.currentDirPath))
 		
 //		File f = new File(ImageFileManager8Activity.currentDirPath);
 //		
 //		ImageFileManager8Activity.currentDirPath = f.getParent();
 //		
 //		Methods.toastAndLog(actv, "f.getParent() => " + f.getParent(), 3000);
 		
 //		/*----------------------------
 //		 * 3-2. New path => Equal to base dir path?
 //			----------------------------*/
 //		
 //		
 		/*----------------------------
 		 * 4. Refresh list
 			----------------------------*/
 		Methods.refreshListView(actv);
 		
 		/*----------------------------
 		 * 5. Update path view
 			----------------------------*/
 		Methods.updatePathLabel(actv);
 		
 	}//public static void upDir(Activity actv)
 
 	public static void startThumbnailActivity(Activity actv, String targetFileName) {
 		/*----------------------------
 		 * Steps
 		 * 1. "list.txt"?
 		 * 2. If yes, start activity
 			----------------------------*/
 		if (!targetFileName.equals(MainActv.listFileName)) {
 			
 			// debug
 			Toast.makeText(actv, "list.txt ł͂܂", 2000).show();
 			
 			return;
 		}//if (!target.getName().equals(ImageFileManager8Activity.listFileName))
 		
 		/*----------------------------
 		 * 2. If yes, start activity
 			----------------------------*/
 		Intent i = new Intent();
 		
 		i.setClass(actv, TNActv.class);
 		
 		i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 		
 		actv.startActivity(i);
 		
 	}//public static void startThumbnailActivity(Activity actv, File target)
 
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
 
 	/****************************************
 	 *
 	 * 
 	 * <Caller> 
 	 * 1. TNActv.set_list()
 	 * 
 	 * <Desc> 1. <Params> 1.
 	 * 
 	 * <Return> 1.
 	 * 
 	 * <Steps> 1.
 	 ****************************************/
 	public static List<TI> getAllData(Activity actv, String tableName) {
 		/*----------------------------
 		 * Steps
 		 * 0. Table exists?
 		 * 1. DB setup
 		 * 2. Get data
 		 * 		2.1. Get cursor
 		 * 		2.2. Add to list
 		 * 
 		 * 9. Close db
 		 * 10. Return value
 			----------------------------*/
 		
 		/*----------------------------
 		 * 1. DB setup
 			----------------------------*/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*----------------------------
 		 * 0. Table exists?
 			----------------------------*/
 		boolean res = dbu.tableExists(rdb, tableName);
 		
 		if (res == false) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "getAllData() => Table doesn't exist: " + tableName);
 			
 			rdb.close();
 			
 			return null;
 			
 		}//if (res == false)
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "rdb.getPath(): " + rdb.getPath());
 		
 		
 		
 		/*----------------------------
 		 * 2. Get data
 		 * 		2.1. Get cursor
 		 * 		2.2. Add to list
 			----------------------------*/
 		//
 		String sql = "SELECT * FROM " + tableName;
 		
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
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "c.getCount() => " + c.getCount());
 
 		/*----------------------------
 		 * 2.2. Add to list
 			----------------------------*/
 		c.moveToNext();
 		
 		List<TI> tiList = new ArrayList<TI>();
 		
//		for (int i = 0; i < c.getCount(); i++) {
		for (int i = 0; i < c.getCount() / 200; i++) {
 
 			TI ti = new TI(
 					c.getLong(1),	// file_id
 					c.getString(2),	// file_path
 					c.getString(3),	// file_name
 					c.getLong(4),	// date_added
 //					c.getLong(5)		// date_modified
 					c.getLong(5),		// date_modified
 					c.getString(6),	// memos
 					c.getString(7)	// tags
 			);
 	
 			// Add to the list
 			tiList.add(ti);
 			
 			//
 			c.moveToNext();
 			
 		}//for (int i = 0; i < c.getCount(); i++)
 		
 		
 		
 		/*----------------------------
 		 * 9. Close db
 			----------------------------*/
 		rdb.close();
 		
 		/*----------------------------
 		 * 10. Return value
 			----------------------------*/
 		return tiList;
 		
 	}//public static List<ThumbnailItem> getAllData
 
 	public static String convert_path_into_table_name(Activity actv) {
 		/*----------------------------
 		 * Steps
 		 * 1. Get table name => Up to the current path
 		 * 2. Add name => Target folder name
 			----------------------------*/
 		String tableName = null;
 		StringBuilder sb = new StringBuilder();
 
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "convert_prefs_into_path_label(actv): " + convert_prefs_into_path_label(actv));
 		
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "MainActv.dbName: " + MainActv.dbName);
 		
 //		if(convert_prefs_into_path_label(actv).equals(MainActv.dbName)) {
 		if(convert_prefs_into_path_label(actv).equals(MainActv.dirName_base)) {
 			
 			tableName = convert_prefs_into_path_label(actv);
 			
 		} else {
 			
 			String[] currentPathArray = convert_prefs_into_path_label(actv).split(File.separator);
 			
 			if (currentPathArray.length > 1) {
 				
 				tableName = StringUtils.join(currentPathArray, "__");
 				
 			} else {//if (currentPathArray.length > 1)
 				
 				sb.append(currentPathArray[0]);
 				
 			}//if (currentPathArray.length > 1)
 			
 		}//if(getCurrentPathLabel(actv).equals(ImageFileManager8Activity.baseDirName))
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "tableName => " + tableName);
 		
 		
 		return tableName;
 	}//public static String convert_path_into_table_name(Activity actv)
 
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
 	}//public static List<String> getTableList()
 
 	public static boolean refreshMainDB(Activity actv) {
 		/*----------------------------
 		 * Steps
 		 * 1. Set up DB(writable)
 		 * 2. Table exists?
 		 * 2-1. If no, then create one
 		 * 3. Execute query for image files
 
 		 * 4. Insert data into db
 		 * 5. Update table "refresh_log"
 		 * 
 		 * 9. Close db
 		 * 10. Return
 			----------------------------*/
 		/*----------------------------
 		 * 1. Set up DB(writable)
 			----------------------------*/
 		//
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		//
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 
 		/*----------------------------
 		 * 2. Table exists?
 		 * 2-1. If no, then create one
 		 * 		1. baseDirName
 		 * 		2. backupTableName
 			----------------------------*/
 		boolean res = refreshMainDB_1_set_up_table(wdb, dbu);
 		
 		/*----------------------------
 		 * 3. Execute query for image files
 			----------------------------*/
 		Cursor c = refreshMainDB_2_exec_query(actv, wdb, dbu);
 		
 		/*----------------------------
 		 * 4. Insert data into db
 			----------------------------*/
 		int numOfItemsAdded;
 		
 		if (c.getCount() < 1) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Query result: 0");
 			
 			numOfItemsAdded = 0;
 			
 		} else {//if (c.getCount() < 1)
 			
 			numOfItemsAdded = refreshMainDB_3_insert_data(actv, wdb, dbu, c);
 			
 		}//if (c.getCount() < 1)
 		
 		/*----------------------------
 		 * 9. Close db
 			----------------------------*/
 		wdb.close();
 		
 		/*----------------------------
 		 * 10. Return
 			----------------------------*/
 		return true;
 		
 	}//public static boolean refreshMainDB(Activity actv)
 
 	private static int refreshMainDB_3_insert_data(Activity actv, SQLiteDatabase wdb, DBUtils dbu, Cursor c) {
 		/*----------------------------
 		 * 4. Insert data into db
 			----------------------------*/
 		int numOfItemsAdded = insertDataIntoDB(actv, MainActv.dirName_base, c);
 			
 //		int numOfItemsAdded = -1;
 		
 		/*----------------------------
 		 * 5. Update table "refresh_log"
 			----------------------------*/
 		c.moveToPrevious();
 		
 		long lastItemDate = c.getLong(3);
 		
 		updateRefreshLog(actv, wdb, dbu, lastItemDate, numOfItemsAdded);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "c.getLong(3) => " + c.getLong(3));
 		
 
 		return numOfItemsAdded;
 		
 	}//private static int refreshMainDB_3_insert_data(Cursor c)
 
 	private static Cursor refreshMainDB_2_exec_query(Activity actv, SQLiteDatabase wdb, DBUtils dbu) {
 		/*----------------------------
 		 * 3. Execute query for image files
 		 * 		1. ContentResolver
 		 * 		2. Uri
 		 * 		3. proj
 		 * 		4. Last refreshed date
 		 * 		5. Execute query
 			----------------------------*/
 		/*----------------------------
 		 * 3.1. ContentResolver, Uri, proj
 			----------------------------*/
 		ContentResolver cr = actv.getContentResolver();
 		
         Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
         
 		String[] proj = DBUtils.proj;
 
 		/*----------------------------
 		 * 3.4. Last refreshed date
 			----------------------------*/
 		long lastRefreshedDate = 0;		// Initial value => 0
 
 		boolean result = dbu.tableExists(wdb, MainActv.tableName_refreshLog);
 		
 		if (result != false) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Table exists: " + MainActv.tableName_refreshLog);
 			
 			
 			// REF=> http://www.accessclub.jp/sql/10.html
 			String sql = "SELECT * FROM refresh_log ORDER BY " + android.provider.BaseColumns._ID + " DESC";
 			
 			Cursor tempC = wdb.rawQuery(sql, null);
 			
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "tempC.getCount() => " + tempC.getCount());
 	
 			if (tempC.getCount() > 0) {
 				
 				tempC.moveToFirst();
 				
 				lastRefreshedDate = tempC.getLong(1);
 				
 				// Log
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", 
 						"lastRefreshedDate => " + String.valueOf(lastRefreshedDate) +
 						" (I will refresh db based on this date!)");
 				
 			}//if (tempC.getCount() > 0)
 		} else {//if (result != false)
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Table doesn't exist: " + MainActv.tableName_refreshLog);
 			
 			// Create one
 			result = dbu.createTable(
 											wdb, 
 											MainActv.tableName_refreshLog, 
 											DBUtils.cols_refresh_log, 
 											DBUtils.col_types_refresh_log);
 			
 			if (result == true) {
 				// Log
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "Table created: " + MainActv.tableName_refreshLog);
 				
 			} else {//if (result == true)
 				// Log
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "Create table failed: " + MainActv.tableName_refreshLog);
 				
 			}//if (result == true)
 			
 		}//if (result != false)
 		
 		/*----------------------------
 		 * 3.5. Execute query
 			----------------------------*/
 		// REF=> http://blog.csdn.net/uoyevoli/article/details/4970860
 		Cursor c = actv.managedQuery(
 											uri, 
 											proj,
 											MediaStore.Images.Media.DATE_ADDED + " > ?",
 											new String[] {String.valueOf(lastRefreshedDate)},
 											null);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "Last refreshed (in sec): " + String.valueOf(lastRefreshedDate / 1000));
 
         actv.startManagingCursor(c);
         
         // Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "c.getCount() => " + c.getCount());
 
 		return c;
 		
 	}//private static Cursor refreshMainDB_2_exec_query()
 
 	private static boolean refreshMainDB_1_set_up_table(SQLiteDatabase wdb, DBUtils dbu) {
 		/*----------------------------
 		 * 2-1.1. baseDirName
 			----------------------------*/
 		String tableName = MainActv.dirName_base;
 		boolean result = dbu.tableExists(wdb, tableName);
 		
 		// If the table doesn't exist, create one
 		if (result == false) {
 
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Table doesn't exist: " + tableName);
 			
 			result = 
 					dbu.createTable(wdb, tableName, DBUtils.cols, DBUtils.col_types);
 			
 			if (result == false) {
 
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "Can't create a table: "+ tableName);
 				
 				return false;
 				
 			} else {//if (result == false)
 				
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "Table created: "+ tableName);
 				
 				return true;
 				
 			}//if (result == false)
 
 		} else {//if (result == false)
 			
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Table exists: "+ tableName);
 
 			return true;
 			
 		}//if (result == false)
 	}//private static boolean refreshMainDB_1_set_up_table(SQLiteDatabase wdb, DBUtils dbu)
 
 	public static boolean refreshMainDB_async(Activity actv, AsyncTask asy) {
 		/*----------------------------
 		 * Steps
 		 * 1. Set up DB(writable)
 		 * 2. Table exists?
 		 * 2-1. If no, then create one
 		 * 3. Execute query for image files
 
 		 * 4. Insert data into db
 		 * 5. Update table "refresh_log"
 		 * 
 		 * 9. Close db
 		 * 10. Return
 			----------------------------*/
 		/*----------------------------
 		 * 1. Set up DB(writable)
 			----------------------------*/
 		//
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		//
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 
 		/*----------------------------
 		 * 2. Table exists?
 		 * 2-1. If no, then create one
 		 * 		1. baseDirName
 		 * 		2. backupTableName
 			----------------------------*/
 		boolean res = refreshMainDB_async_1_set_up_table(wdb, dbu);
 		
 		/*----------------------------
 		 * 3. Execute query for image files
 			----------------------------*/
 		Cursor c = refreshMainDB_async_2_exec_query(actv, wdb, dbu);
 		
 		/*----------------------------
 		 * 4. Insert data into db
 			----------------------------*/
 		int numOfItemsAdded;
 		
 		if (c.getCount() < 1) {
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Query result: 0");
 			
 			numOfItemsAdded = 0;
 			
 		} else {//if (c.getCount() < 1)
 			
 			numOfItemsAdded = refreshMainDB_async_3_insert_data(actv, wdb, dbu, c, asy);
 			
 		}//if (c.getCount() < 1)
 		
 		/*----------------------------
 		 * 9. Close db
 			----------------------------*/
 		wdb.close();
 		
 		/*----------------------------
 		 * 10. Return
 			----------------------------*/
 		return true;
 		
 	}//public static boolean refreshMainDB(Activity actv)
 
 	private static int refreshMainDB_async_3_insert_data(
 										Activity actv, SQLiteDatabase wdb, DBUtils dbu, Cursor c, AsyncTask asy) {
 		/*----------------------------
 		 * 4. Insert data into db
 			----------------------------*/
 		int numOfItemsAdded = insertDataIntoDB_async(actv, MainActv.dirName_base, c, asy);
 
 //		int numOfItemsAdded = -1;
 		
 		/*----------------------------
 		 * 5. Update table "refresh_log"
 			----------------------------*/
 		c.moveToPrevious();
 		
 		long lastItemDate = c.getLong(3);
 		
 		updateRefreshLog(actv, wdb, dbu, lastItemDate, numOfItemsAdded);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "c.getLong(3) => " + c.getLong(3));
 		
 
 		return numOfItemsAdded;
 		
 	}//private static int refreshMainDB_3_insert_data(Cursor c)
 
 	private static Cursor refreshMainDB_async_2_exec_query(Activity actv, SQLiteDatabase wdb, DBUtils dbu) {
 		/*----------------------------
 		 * 3. Execute query for image files
 		 * 		1. ContentResolver
 		 * 		2. Uri
 		 * 		3. proj
 		 * 		4. Last refreshed date
 		 * 		5. Execute query
 			----------------------------*/
 		/*----------------------------
 		 * 3.1. ContentResolver, Uri, proj
 			----------------------------*/
 		ContentResolver cr = actv.getContentResolver();
 		
         Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
         
 		String[] proj = DBUtils.proj;
 
 		/*----------------------------
 		 * 3.4. Last refreshed date
 			----------------------------*/
 		long lastRefreshedDate = 0;		// Initial value => 0
 
 		boolean result = dbu.tableExists(wdb, MainActv.tableName_refreshLog);
 		
 		if (result != false) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Table exists: " + MainActv.tableName_refreshLog);
 			
 			
 			// REF=> http://www.accessclub.jp/sql/10.html
 			String sql = "SELECT * FROM refresh_log ORDER BY " + android.provider.BaseColumns._ID + " DESC";
 			
 			Cursor tempC = wdb.rawQuery(sql, null);
 			
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "tempC.getCount() => " + tempC.getCount());
 	
 			if (tempC.getCount() > 0) {
 				
 				tempC.moveToFirst();
 				
 				lastRefreshedDate = tempC.getLong(1);
 				
 				// Log
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", 
 						"lastRefreshedDate => " + String.valueOf(lastRefreshedDate) +
 						" (I will refresh db based on this date!)");
 				
 			}//if (tempC.getCount() > 0)
 		} else {//if (result != false)
 			
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Table doesn't exist: " + MainActv.tableName_refreshLog);
 			
 			// Create one
 			result = dbu.createTable(
 											wdb, 
 											MainActv.tableName_refreshLog, 
 											DBUtils.cols_refresh_log, 
 											DBUtils.col_types_refresh_log);
 			
 			if (result == true) {
 				// Log
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "Table created: " + MainActv.tableName_refreshLog);
 				
 			} else {//if (result == true)
 				// Log
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "Create table failed: " + MainActv.tableName_refreshLog);
 				
 			}//if (result == true)
 			
 		}//if (result != false)
 		
 		/*----------------------------
 		 * 3.5. Execute query
 			----------------------------*/
 		// REF=> http://blog.csdn.net/uoyevoli/article/details/4970860
 		Cursor c = actv.managedQuery(
 											uri, 
 											proj,
 											MediaStore.Images.Media.DATE_ADDED + " > ?",
 											new String[] {String.valueOf(lastRefreshedDate)},
 											null);
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "Last refreshed (in sec): " + String.valueOf(lastRefreshedDate / 1000));
 
         actv.startManagingCursor(c);
         
         // Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "c.getCount() => " + c.getCount());
 
 		return c;
 		
 	}//private static Cursor refreshMainDB_2_exec_query()
 
 	private static boolean refreshMainDB_async_1_set_up_table(SQLiteDatabase wdb, DBUtils dbu) {
 		/*----------------------------
 		 * 2-1.1. baseDirName
 			----------------------------*/
 		String tableName = MainActv.dirName_base;
 		boolean result = dbu.tableExists(wdb, tableName);
 		
 		// If the table doesn't exist, create one
 		if (result == false) {
 
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Table doesn't exist: " + tableName);
 			
 			result = 
 					dbu.createTable(wdb, tableName, DBUtils.cols, DBUtils.col_types);
 			
 			if (result == false) {
 
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "Can't create a table: "+ tableName);
 				
 				return false;
 				
 			} else {//if (result == false)
 				
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "Table created: "+ tableName);
 				
 				return true;
 				
 			}//if (result == false)
 
 		} else {//if (result == false)
 			
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Table exists: "+ tableName);
 
 			return true;
 			
 		}//if (result == false)
 	}//private static boolean refreshMainDB_1_set_up_table(SQLiteDatabase wdb, DBUtils dbu)
 
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
 		/*----------------------------
 		 * 0. Set up db
 			----------------------------*/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 		
 		/*----------------------------
 		 * 1. Move to first
 			----------------------------*/
 		c.moveToFirst();
 
 		/*----------------------------
 		 * 2. Set variables
 			----------------------------*/
 		int counter = 0;
 		int counter_failed = 0;
 		
 		/*----------------------------
 		 * 3. Obtain data
 			----------------------------*/
 		for (int i = 0; i < c.getCount(); i++) {
 
 			String[] values = {
 					String.valueOf(c.getLong(0)),
 					c.getString(1),
 					c.getString(2),
 					String.valueOf(c.getLong(3)),
 					String.valueOf(c.getLong(4))
 			};
 
 			/*----------------------------
 			 * 4. Insert data
 			 * 		1. Insert data to tableName
 			 * 		2. Record result
 			 * 		3. Insert data to backupTableName
 			 * 		4. Record result
 				----------------------------*/
 			boolean blResult = 
 						dbu.insertData(wdb, tableName, DBUtils.cols_for_insert_data, values);
 				
 			if (blResult == false) {
 				// Log
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "i => " + i + "/" + "c.getLong(0) => " + c.getLong(0));
 				
 				counter_failed += 1;
 				
 			} else {//if (blResult == false)
 				counter += 1;
 			}
 
 			//
 			c.moveToNext();
 			
 			if (i % 100 == 0) {
 				// Log
 				Log.d("Methods.java" + "["
 						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 						+ "]", "Done up to: " + i);
 				
 			}//if (i % 100 == 0)
 			
 		}//for (int i = 0; i < c.getCount(); i++)
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "All data inserted: " + counter);
 		
 		/*----------------------------
 		 * 5. Close db
 			----------------------------*/
 		wdb.close();
 		
 		/*----------------------------
 		 * 6. Return => counter
 			----------------------------*/
 		//debug
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "counter_failed(sum): " + counter_failed);
 		
 		return counter;
 		
 	}//private static int insertDataIntoDB(Activity actv, Cursor c)
 
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
 	private static int insertDataIntoDB_async(Activity actv, String tableName, Cursor c, AsyncTask asy) {
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
 		/*----------------------------
 		 * 0. Set up db
 			----------------------------*/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 		
 		/*----------------------------
 		 * 1. Move to first
 			----------------------------*/
 		c.moveToFirst();
 
 		/*----------------------------
 		 * 2. Set variables
 			----------------------------*/
 		int counter = 0;
 		int counter_failed = 0;
 		
 		/*----------------------------
 		 * 3. Obtain data
 			----------------------------*/
 		for (int i = 0; i < c.getCount(); i++) {
 
 			String[] values = {
 					String.valueOf(c.getLong(0)),
 					c.getString(1),
 					c.getString(2),
 					String.valueOf(c.getLong(3)),
 					String.valueOf(c.getLong(4))
 			};
 
 			/*----------------------------
 			 * 4. Insert data
 			 * 		1. Insert data to tableName
 			 * 		2. Record result
 			 * 		3. Insert data to backupTableName
 			 * 		4. Record result
 				----------------------------*/
 			boolean blResult = 
 						dbu.insertData(wdb, tableName, DBUtils.cols_for_insert_data, values);
 				
 			if (blResult == false) {
 				// Log
 				Log.d("Methods.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber() + "]", "i => " + i + "/" + "c.getLong(0) => " + c.getLong(0));
 				
 				counter_failed += 1;
 				
 			} else {//if (blResult == false)
 				counter += 1;
 			}
 
 			//
 			c.moveToNext();
 			
 			if (i % 100 == 0) {
 				// Log
 				Log.d("Methods.java" + "["
 						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 						+ "]", "Done up to: " + i);
 				
 			}//if (i % 100 == 0)
 			
 		}//for (int i = 0; i < c.getCount(); i++)
 		
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "All data inserted: " + counter);
 		
 		/*----------------------------
 		 * 5. Close db
 			----------------------------*/
 		wdb.close();
 		
 		/*----------------------------
 		 * 6. Return => counter
 			----------------------------*/
 		//debug
 		// Log
 		Log.d("Methods.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "counter_failed(sum): " + counter_failed);
 		
 		return counter;
 		
 	}//private static int insertDataIntoDB(Activity actv, Cursor c)
 
 	private static boolean updateRefreshLog(
 				Activity actv, SQLiteDatabase wdb, 
 				DBUtils dbu, long lastItemDate, int numOfItemsAdded) {
 		/*----------------------------
 		* Steps
 		* 1. Table exists?
 		* 2. If no, create one
 		* 2-2. Create table failed => Return
 		* 3. Insert data
 		----------------------------*/
 		/*----------------------------
 		 * 1. Table exists?
 			----------------------------*/
 		String tableName = MainActv.tableName_refreshLog;
 		
 		if(!dbu.tableExists(wdb, tableName)) {
 		
 			Log.d("Methods.java" + "["
 			+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 			+ "]", "Table doesn't exitst: " + tableName);
 		
 			/*----------------------------
 			* 2. If no, create one
 			----------------------------*/
 			if(dbu.createTable(wdb, tableName, 
 				DBUtils.cols_refresh_log, DBUtils.col_types_refresh_log)) {
 				
 				//toastAndLog(actv, "Table created: " + tableName, 3000);
 				
 				// Log
 				Log.d("Methods.java"
 				+ "["
 				+ Thread.currentThread().getStackTrace()[2]
 				.getLineNumber() + "]", "Table created: " + tableName);
 			
 			} else {//if
 				/*----------------------------
 				* 2-2. Create table failed => Return
 				----------------------------*/
 				//toastAndLog(actv, "Create table failed: " + tableName, 3000);
 				
 				// Log
 				Log.d("Methods.java"
 				+ "["
 				+ Thread.currentThread().getStackTrace()[2]
 				.getLineNumber() + "]", "Create table failed: " + tableName);
 				
 				
 				return false;
 			
 			}//if
 		
 		} else {//if(dbu.tableExists(wdb, ImageFileManager8Activity.refreshLogTableName))
 		
 			//toastAndLog(actv, "Table exitsts: " + tableName, 2000);
 			
 			// Log
 			Log.d("Methods.java" + "["
 			+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 			+ "]", "Table exitsts: " + tableName);
 		
 		
 		}//if(dbu.tableExists(wdb, ImageFileManager8Activity.refreshLogTableName))
 		
 		/*----------------------------
 		* 3. Insert data
 		----------------------------*/
 		try {
 			dbu.insertData(
 							wdb, 
 							tableName, 
 							DBUtils.cols_refresh_log, 
 							new long[] {lastItemDate, (long) numOfItemsAdded}
 			);
 			
 			return true;
 			
 		} catch (Exception e) {
 			// Log
 			Log.d("Methods.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Insert data failed");
 			
 			return false;
 		}
 		
 	}//private static boolean updateRefreshLog(SQLiteDatabase wdb, long lastItemDate)
 
 }//public class Methods
