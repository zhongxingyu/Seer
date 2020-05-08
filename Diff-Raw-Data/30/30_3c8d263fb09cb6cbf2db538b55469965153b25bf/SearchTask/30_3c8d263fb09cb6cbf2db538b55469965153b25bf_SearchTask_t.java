 package ifm9.tasks;
 
 import ifm9.main.MainActv;
 import ifm9.main.TNActv;
 import ifm9.utils.DBUtils;
 import ifm9.utils.Methods;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 public class SearchTask extends AsyncTask<String[], Integer, String>{
 
 	//
 	Activity actv;
 	
 	//
 	String[] search_words;
 
 	//
 	static long[] long_searchedItems;
 	
 	static String[] string_searchedItems_table_names;
 	
 	int search_mode;
 	
 	public SearchTask(Activity actv, String[] search_words) {
 		
 		this.actv = actv;
 		this.search_words = search_words;
 		
 	}//public SearchTask(Activity actv, String[] search_words)
 
 
 	public SearchTask(Activity actv) {
 		
 		this.actv = actv;
 		
 		// Log
 		Log.d("SearchTask.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "Starts => SearchTask(Activity actv)");
 		
 	}//public SearchTask(Activity actv)
 
 
 	public SearchTask(Activity actv, int search_mode) {
 		
 		this.actv = actv;
 		
 		this.search_mode = search_mode;
 		
 		string_searchedItems_table_names = null;
 
 //		// Log
 //		Log.d("SearchTask.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ "]", "Starts => SearchTask(Activity actv, int search_mode)");
 
 	}//public SearchTask(Activity actv2, int search_mode)
 	
 
 
 	@Override
 	protected String doInBackground(String[]... sw) {
 		
 		// Log
 		Log.d("SearchTask.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "sw.length=" + sw.length);
 		
//		//debug
//		for (String[] item : sw) {
//			
//			// Log
//			Log.d("SearchTask.java" + "["
//					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
//					+ "]", "item=" + item);
//			
//		}
 		
 		// Log
 		if (string_searchedItems_table_names != null) {
 			
 			Log.d("SearchTask.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", 
 					"string_searchedItems_table_names.length="
 						+ string_searchedItems_table_names.length);
 			
 		} else {//if (string_searchedItems_table_names != null)
 
 			Log.d("SearchTask.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", 
 					"string_searchedItems_table_names => Null");
 
 		}//if (string_searchedItems_table_names != null)
 		
 		if(search_mode == 0) {
 			
 			// Log
 			Log.d("SearchTask.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Calling => doInBackground_specific_table(sw)");
 			
 			return this.doInBackground_specific_table(sw);
 			
 		} else {
 
 			// Log
 			Log.d("SearchTask.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Calling => doInBackground_all_table(sw)");
 
 			return this.doInBackground_all_table(sw);
 			
 		}//if(search_mode == 0)
 		
 	}//protected String doInBackground(String[]... sw)
 
 	private String doInBackground_all_table(String[][] sw) {
 		/*----------------------------
 		 * Steps
 		 * 1. Get table names list
 		 * 1-2. Get => Table names list
 		 * 
 		 * 2. Construct data			##Add ThumbnailItem to tiLIst
 		 * 3. Close db
 		 * 4. Set up intent
 		 * 5. Return
 			----------------------------*/
 		
 		/*----------------------------
 		 * 1. Get table names list
 			----------------------------*/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*********************************
 		 * 1-2. Get => Table names list
 		 *********************************/
 //		List<String> table_names = Methods.get_table_list(actv);
//		List<String> table_names = Methods.get_table_list(actv, "IFM9");
		List<String> table_names = Methods.get_table_list(actv, "IFM9%");
 		
 		/*----------------------------
 		 * 2. Construct data
 		 * 		1. Table name
 		 * 		1-2. Declare => List<Long> searchedItems
 		 * 		2. Exec query
 		 * 		3. Search
 		 * 		4. List<Long> searchedItems => file id
 		 * 		
 		 * 		5. List<Long> searchedItems => to array
 		 * 
 		 * 		6. List<String> string_searchedItems_table_names => to array
 		 * 
 			----------------------------*/
 //		String targetTable = sw[1][0];
 		
 		List<Long> searchedItems = new ArrayList<Long>();
 		
 		List<String> searchedItems_table_names = new ArrayList<String>();
 		
 		/*----------------------------
 		 * 2.2. Exec query
 			----------------------------*/
 		for (String targetTable : table_names) {
 			
 			String sql = "SELECT * FROM " + targetTable;
 			
 			// Log
 			Log.d("SearchTask.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "targetTable: " + targetTable);
 			
 			
 			Cursor c = rdb.rawQuery(sql, null);
 			
 			actv.startManagingCursor(c);
 			
 			c.moveToFirst();
 	
 			
 			/*----------------------------
 			 * 2.3. Search
 				----------------------------*/
 			doInBackground_all_table_search(
 						c, sw[0], 
 						searchedItems, searchedItems_table_names,
 						targetTable);
 			
 		}//for (String targetTable : table_names)
 		
 		/*********************************
 		 * 2.5. List<Long> searchedItems => to array
 		 * 2.6. String[] searchedItems_table_names => to array
 		 *********************************/
 		int len = searchedItems.size();
 		
 //		long[] long_searchedItems = new long[len];
 		long_searchedItems = new long[len];
 		
 		string_searchedItems_table_names = new String[len];
 		
 		for (int i = 0; i < len; i++) {
 			
 			long_searchedItems[i] = searchedItems.get(i);
 			
 			string_searchedItems_table_names[i] = searchedItems_table_names.get(i);
 			
 		}//for (int i = 0; i < len; i++)
 		
 		/*----------------------------
 		 * 3. Close db
 			----------------------------*/
 		rdb.close();
 		
 		/*----------------------------
 		 * 5. Return
 			----------------------------*/
 		return "Search done";
 		
 	}//private String doInBackground_all_table(String[][] sw)
 
 
 	private void doInBackground_all_table_search(
 					Cursor c, String[] key_words,
 					List<Long> searchedItems,
 					List<String> searchedItems_table_names,
 					String targetTable) {
 		/*********************************
 		 * 1. No memo in the item => Next item
 		 * 2. If it matches, add to searchedItems and table_names
 		 *********************************/
 		
 		
 		for (int i = 0; i < c.getCount(); i++) {
 			
//			String memo = c.getString(6);
			String memo = c.getString(8);
 			
 			/*********************************
 			 * 1. No memo in the item => Next item
 			 *********************************/
 			if (memo == null) {
 
 				c.moveToNext();
 				
 				continue;
 				
 			}//if (memo == null)
 			
 			for (String string : key_words) {
 				
 				
 				/*********************************
 				 * 2. If it matches, add to searchedItems and table_names
 				 *********************************/
 				if (memo.matches(".*" + string + ".*")) {
 					
 					// Log
 					Log.d("SearchTask.java"
 							+ "["
 							+ Thread.currentThread().getStackTrace()[2]
 									.getLineNumber() + "]", "memo => " + memo);
 					
 				
 					/*----------------------------
 					 * 2.4. List<Long> searchedItems => file id
 						----------------------------*/
//					searchedItems.add(c.getLong(1));
					searchedItems.add(c.getLong(3));
 					
 					/*********************************
 					 * Table name
 					 *********************************/
 					searchedItems_table_names.add(targetTable);
 					
 					break;
 					
 				}//if (memo.matches(".*" + ))
 				
 			}//for (String string : sw[0])
 			
 			c.moveToNext();
 			
 		}//for (int i = 0; i < c.getCount(); i++)
 		
 	}//private void doInBackground_all_table_search()
 
 	private String doInBackground_specific_table(String[][] sw) {
 		/*----------------------------
 		 * Steps
 		 * 1. Get table names list
 		 * 2. Construct data			##Add ThumbnailItem to tiLIst
 		 * 3. Close db
 		 * 4. Set up intent
 		 * 5. Return
 			----------------------------*/
 		/*----------------------------
 		 * 1. Get table names list
 			----------------------------*/
 		DBUtils dbu = new DBUtils(actv, MainActv.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 
 		
 		String targetTable = sw[1][0];
 		
 		// Log
 		Log.d("SearchTask.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "targetTable=" + targetTable);
 		
 		List<Long> searchedItems = new ArrayList<Long>();
 		
 		/*----------------------------
 		 * 2.2. Exec query
 			----------------------------*/
 		String sql = "SELECT * FROM " + targetTable;
 		
 		// Log
 		Log.d("SearchTask.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "targetTable: " + targetTable);
 		
 		
 		Cursor c = rdb.rawQuery(sql, null);
 		
 		c.moveToFirst();
 		
 		/*----------------------------
 		 * 2.3. Search
 			----------------------------*/
 		for (int i = 0; i < c.getCount(); i++) {
 			
 //			String memo = c.getString(6);
 			String memo = c.getString(8);	// memos
 			
 			if (memo == null) {
 
 				c.moveToNext();
 				
 				continue;
 				
 			}//if (memo == null)
 			
 
 //			// Log
 //			Log.d("SearchTask.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ "]", "sw[0].length => " + sw[0].length);
 
 //			if (sw[1] != null) {
 //				
 //				Log.d("SearchTask.java" + "["
 //						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //						+ "]", "sw[1].length => " + sw[1].length);
 //				
 //			} else {//if (sw[1])
 //				
 //				Log.d("SearchTask.java" + "["
 //						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //						+ "]", "sw[1] => null");
 //				
 //			}//if (sw[1])
 			
 			for (String string : sw[0]) {
 				
 				
 				
 				if (memo.matches(".*" + string + ".*")) {
 					
 					// Log
 					Log.d("SearchTask.java"
 							+ "["
 							+ Thread.currentThread().getStackTrace()[2]
 									.getLineNumber() + "]", "memo => " + memo);
 					
 				
 					/*----------------------------
 					 * 2.4. List<Long> searchedItems => file id
 						----------------------------*/
 //					searchedItems.add(c.getLong(1));
 					searchedItems.add(c.getLong(3));
 					
 					break;
 					
 				}//if (memo.matches(".*" + ))
 				
 			}//for (String string : sw[0])
 			
 			c.moveToNext();
 			
 		}//for (int i = 0; i < c.getCount(); i++)
 		
 		
 		/*----------------------------
 		 * 2.5. List<Long> searchedItems => to array
 			----------------------------*/
 		int len = searchedItems.size();
 		
 //		long[] long_searchedItems = new long[len];
 		long_searchedItems = new long[len];
 		
 		for (int i = 0; i < len; i++) {
 			
 			long_searchedItems[i] = searchedItems.get(i);
 			
 		}//for (int i = 0; i < len; i++)
 		
 		/*----------------------------
 		 * 3. Close db
 			----------------------------*/
 		rdb.close();
 		
 //		/*----------------------------
 //		 * 4. Set up intent
 //			----------------------------*/
 //		// Log
 //		Log.d("SearchTask.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ "]", "long_searchedItems.length => " + long_searchedItems.length);
 //		
 //		Log.d("SearchTask.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ "]", "long_searchedItems[0] => " + long_searchedItems[0]);
 //		
 //		Intent i = new Intent();
 //		
 //		i.setClass(actv, ThumbnailActivity.class);
 //		
 //		i.putExtra("long_searchedItems", long_searchedItems);
 //		
 //		actv.startActivity(i);
 		
 		/*----------------------------
 		 * 5. Return
 			----------------------------*/
 		return "Search done";
 		
 	}//private String doInBackground_specific_table(String[][] sw)
 	
 
 
 	@Override
 	protected void onPostExecute(String result) {
 		// TODO ꂽ\bhEX^u
 		super.onPostExecute(result);
 
 		// debug
 //		Toast.makeText(actv, result, 2000).show();
 		// Log
 		Log.d("SearchTask.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", result);
 		
 
 		/*----------------------------
 		 * 1. Set up intent
 			----------------------------*/
 		// Log
 		Log.d("SearchTask.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "long_searchedItems.length => " + long_searchedItems.length);
 		
 		if(long_searchedItems.length > 0) {
 			
 			Log.d("SearchTask.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "long_searchedItems[0] => " + long_searchedItems[0]);
 			
 			Intent i = new Intent();
 			
 			i.setClass(actv, TNActv.class);
 			
 			i.putExtra("long_searchedItems", long_searchedItems);
 			
 			if (string_searchedItems_table_names != null &&
 					string_searchedItems_table_names.length > 0) {	
 				
 				i.putExtra(
 //						"string_searchedItems_table_names",
 						MainActv.intent_label_searchedItems_table_names,
 						string_searchedItems_table_names);
 				
 			}//if (variable == condition)
 //				i.putExtra("string_searchedItems_table_names", string_searchedItems_table_names);
 
 			// Log
 			if (string_searchedItems_table_names != null) {
 				
 				Log.d("SearchTask.java" + "["
 						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 						+ "]", 
 						"string_searchedItems_table_names.length="
 							+ string_searchedItems_table_names.length);
 				
 			} else {//if (string_searchedItems_table_names != null)
 
 				Log.d("SearchTask.java" + "["
 						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 						+ "]", 
 						"string_searchedItems_table_names => Null");
 
 			}//if (string_searchedItems_table_names != null)
 			
 //			Log.d("SearchTask.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ "]", 
 //					"string_searchedItems_table_names.length="
 //						+ string_searchedItems_table_names.length);
 			
 			/*----------------------------
 			 * 2. Start activity
 				----------------------------*/
 			actv.startActivity(i);
 			
 		} else {
 			
 			// debug
 			Toast.makeText(actv, "܂ł", 2000).show();
 		}
 
 	}//protected void onPostExecute(String result)
 	
 	
 }//public class SearchTask
