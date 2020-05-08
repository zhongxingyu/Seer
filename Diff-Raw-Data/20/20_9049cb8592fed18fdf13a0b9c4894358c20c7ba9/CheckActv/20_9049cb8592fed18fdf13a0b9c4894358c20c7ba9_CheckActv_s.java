 package ic.main;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ic.items.CL;
 import ic.items.Item;
 import ic.listeners.ButtonOnClickListener;
 import ic.listeners.ButtonOnTouchListener;
 import ic.listeners.CustomOnItemLongClickListener;
 import ic.utils.DBUtils;
 import ic.utils.ItemListAdapter;
 import ic.utils.MainListAdapter;
 import ic.utils.Methods;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CheckActv extends ListActivity {
 
 	/*********************************
 	 * List-related
 	 *********************************/
 	public static ItemListAdapter ilAdp;
 	
 	public static List<Item> iList;
 
 	public static CL clList;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		/********************************
 		 * 1. set_up_1
 		 * 2. Set listeners
 		 * 
 		 * 3. Initialise vars
 		 ********************************/
 		/*********************************
 		 * 1. Set up
 		 *********************************/
 		super.onCreate(savedInstanceState);
 
 		set_up_1();
 
 		/*********************************
 		 * 3. Initialise vars
 		 *********************************/
 		iList = new ArrayList<Item>();
 
 		/*********************************
 		 * 2. Set listeners
 		 *********************************/
 		set_listeners();
 		
 		set_up_2_show_list();
 		
 //		/*********************************
 //		 * 3. Initialise vars
 //		 *********************************/
 //		iList = new ArrayList<Item>();
 		
 	}//public void onCreate(Bundle savedInstanceState)
 
 	/**********************************************
 	 * <Ret>
 	 * 	false	=> iList == null
 	 **********************************************/
 	private boolean set_up_2_show_list() {
 		/*********************************
 		 * 1. Get item list
 		 * 	1. Query
 		 * 	2. Build list
 		 * 	3. Sort list
 		 * 
 		 * 2. Setup adapter
 		 * 
 		 * 3. Set adapter
 		 * 
 		 * 4. Return
 		 *********************************/
 //		/*********************************
 //		 * 1.1. Query
 //		 *********************************/
 //		DBUtils dbu = new DBUtils(this, MainActv.dbName);
 //		
 //		SQLiteDatabase rdb = dbu.getReadableDatabase();
 
 //		Cursor c = Methods.select_all_from_table(this, rdb, MainActv.tableName_items);
 //		
 //		if (c.getCount() < 1) {
 //			
 //			// debug
 //			Toast.makeText(this, "No data yet", Toast.LENGTH_SHORT).show();
 //			
 //			/********************************
 //			 * 3. Close db
 //			 ********************************/
 //			rdb.close();
 //
 //			return;
 //		}//if (c.getCount() < 1)
 		
 		/*********************************
 		 * 1.2. Build list
 		 *********************************/
 		iList = Methods.get_item_list_from_check_list(this, clList.getDb_id());
 
 		// Log
 		if (iList == null) {
 
 			Log.d("CheckActv.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "iList => Null");
 			
 			// debug
 			Toast.makeText(this,
 					"Query exception, or, no items " +
 					"for this check list, yet",
 					Toast.LENGTH_SHORT).show();
 			
 //			/********************************
 //			 * 3. Close db
 //			 ********************************/
 //			rdb.close();
 
 			return false;
 
 		} else {//if (iList == null)
 			
 			// Log
 			Log.d("CheckActv.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "iList.size(): " + iList.size());
 			
 		}//if (iList == null)
 		
 //			Log.d("CheckActv.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ "]", "iList.size(): " + iList.size());
 		
 		/*********************************
 		 * 1.3. Sort list
 		 *********************************/
 		boolean res = Methods.sort_item_list_by_serial_num(this);
 		
 		// Log
 		Log.d("CheckActv.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "res: " + res);
 		
 		
 		/*********************************
 		 * 2. Setup adapter
 		 *********************************/
 		ilAdp = new ItemListAdapter(
 				this,
 				R.layout.list_row_item_list,
 				iList
 				);
 
 		/*********************************
 		 * 3. Set adapter
 		 *********************************/
 		setListAdapter(ilAdp);
 		
 		/*********************************
 		 * 4. Return
 		 *********************************/
 		return true;
 
 	}//private void set_up_2_show_list()
 
 
 	private void set_listeners() {
 		/*********************************
 		 * 1. Button => "Add"
 		 * 2. Long click
 		 *********************************/
 		/*********************************
 		 * 1. Button => "Add"
 		 *********************************/
 		Button bt_add = (Button) findViewById(R.id.actv_check_bt_add);
 		
 		bt_add.setTag(Methods.ButtonTags.actv_check_bt_add);
 		
 		bt_add.setOnTouchListener(new ButtonOnTouchListener(this));
 		bt_add.setOnClickListener(new ButtonOnClickListener(this));
 
 		/*********************************
 		 * 2. Long click
 		 *********************************/
 		ListView lv = this.getListView();
 		
 		lv.setTag(Methods.ListTags.actv_check_lv);
 		
 		lv.setOnItemLongClickListener(new CustomOnItemLongClickListener(this));
 		
 	}//private void set_listeners()
 
 	private void set_up_1() {
 		/********************************
 		 * 1. Set up
 		 * 	1. Basics
 		 * 	2. Initialise vars
 		 * 2. Get intent values
 		 * 
 		 * 3. Get list object
 		 * 
 		 * 4. Set title
 		 * 
 		 * 5. Get item list
 		 * 
 		 * 6. Close db
 		 * 
 		 ********************************/
 		
 		setContentView(R.layout.actv_check);
 
 		this.setTitle(this.getClass().getName());
 		
 		/*********************************
 		 * 1.2. Initialise vars
 		 *********************************/
		
 		
 		
 		/*********************************
 		 * 2. Get intent values
 		 *********************************/
 		Intent i = this.getIntent();
 		
 		long list_id = i.getLongExtra(MainActv.intent_list_id, -1);
 		
 		if (list_id == -1) {
 			
 			// debug
 			Toast.makeText(this, "No intent value", Toast.LENGTH_SHORT).show();
 			
 			return;
 			
 		}//if (list_id == -1)
 		
 		// debug
 //		Toast.makeText(this, "list_id=" + list_id, Toast.LENGTH_SHORT).show();
 		
 		/*********************************
 		 * 3. Get list object
 		 *********************************/
 		clList = Methods.get_clList_from_db_id(this, list_id);
 		
 		// Log
 		Log.d("CheckActv.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "clList.getName(): " + clList.getName());
 		
 		/*********************************
 		 * 4. Set title
 		 *********************************/
 		TextView tv_title = (TextView) findViewById(R.id.actv_check_tv);
 
 		String title = clList.getName();
 		
 		if (title.equals("")) {
 			
 			tv_title.setText("No list name data");
 			
 		} else {
 			
 			tv_title.setText(title);
 			
 		}
 		
 		
 	}//private void set_up_1()
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater mi = getMenuInflater();
 		mi.inflate(R.menu.menu_actv_check, menu);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	protected void onDestroy() {
 		// TODO ꂽ\bhEX^u
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		
 		case R.id.opt_menu_actv_check_clear_status://---------------
 			
 			opt_menu_actv_check_clear_status();
 			
 			break;// case R.id.opt_menu_actv_check_clear_status
 
 		case R.id.opt_menu_actv_check_sort_list_by_status://------------
 			
 			boolean res = Methods.sort_item_list_by_status(this);
 			
 			ilAdp.notifyDataSetChanged();
 			
 			break;
 			
 		}//switch (item.getItemId())
 		
 		return super.onOptionsItemSelected(item);
 		
 	}//public boolean onOptionsItemSelected(MenuItem item)
 
 	private void opt_menu_actv_check_clear_status() {
 		// Log
 		Log.d("CheckActv.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "iList.size(): " + iList.size());
 		
 		for (Item i : CheckActv.iList) {
 			
 			i.setStatus(0);
 			
 		}
 		
 		//debug
 		String temp = "";
 		
 		for (Item i : CheckActv.iList) {
 			
 			temp += "[" + i.getDb_id() + " => " + "status=" + i.getStatus() + "]";
 			
 		}
 		
 		ilAdp.notifyDataSetChanged();
 		
 	}//private void opt_menu_actv_check_clear_status()
 
 	@Override
 	protected void onPause() {
 		// TODO ꂽ\bhEX^u
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO ꂽ\bhEX^u
 		super.onResume();
 	}
 
 	@Override
 	protected void onStart() {
 		// TODO ꂽ\bhEX^u
 		super.onStart();
 	}
 
 	@Override
 	protected void onStop() {
 		// TODO ꂽ\bhEX^u
 		super.onStop();
 	}
 
 	@Override
 	protected void onListItemClick(
 					ListView l, View v, int position, long id) {
 		/*********************************
 		 * 1. Get item
 		 * 
 		 * 2. Change status
 		 * 
 		 *********************************/
 		Item item = (Item) l.getItemAtPosition(position);
 
 		Methods.change_item_status(this, item);
 		
 		super.onListItemClick(l, v, position, id);
 		
 	}//protected void onListItemClick(ListView l, View v, int position, long id)
 
 }//public class CheckActv extends ListActivity
