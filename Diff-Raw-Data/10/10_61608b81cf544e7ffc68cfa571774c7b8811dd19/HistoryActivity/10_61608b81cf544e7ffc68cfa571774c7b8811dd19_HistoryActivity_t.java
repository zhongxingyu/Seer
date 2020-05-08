 package com.pwr.zpi;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.pwr.zpi.adapters.RunAdapter;
 import com.pwr.zpi.database.Database;
 import com.pwr.zpi.database.entity.SingleRun;
 import com.pwr.zpi.listeners.GestureListener;
 import com.pwr.zpi.listeners.MyGestureDetector;
 import com.pwr.zpi.utils.Pair;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.GestureDetector;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
import android.widget.RelativeLayout;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 
 public class HistoryActivity extends Activity implements GestureListener,
 		OnItemClickListener {
 
 	GestureDetector gestureDetector;
 	private View.OnTouchListener gestureListener;
 	private ListView listViewThisWeek;
 	private ListView listViewThisMonth;
 	private ListView listViewAll;
 
 	private static final String TAB_SPEC_1_TAG = "TabSpec1";
 	private static final String TAB_SPEC_2_TAG = "TabSpec2";
 	private static final String TAB_SPEC_3_TAG = "TabSpec3";
 	public static final String ID_TAG = "id";
 	List<SingleRun> run_data;
 	
 	private TabHost tabHost;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.history_activity);
 
 		addListeners();
 
 		//changing order brakes the proper work of context menu
 		tabHost = (TabHost) findViewById(R.id.tabhostHistory);
 		tabHost.setup();
 		TabSpec tabSpecs = tabHost.newTabSpec(TAB_SPEC_1_TAG);
 		tabSpecs.setContent(R.id.tabThisWeek);
 		tabSpecs.setIndicator(getResources().getString(R.string.this_week));
 		tabHost.addTab(tabSpecs);
 		tabSpecs = tabHost.newTabSpec(TAB_SPEC_2_TAG);
 		tabSpecs.setContent(R.id.tabThisMonth);
 		tabSpecs.setIndicator(getResources().getString(R.string.this_month));
 		tabHost.addTab(tabSpecs);
 		tabSpecs = tabHost.newTabSpec(TAB_SPEC_3_TAG);
 		tabSpecs.setContent(R.id.tabAll);
 		tabSpecs.setIndicator(getResources().getString(R.string.all));
 		tabHost.addTab(tabSpecs);
 
 		// TODO get from database
 		// addMockData();
 		// run_data = new SingleRun[11];
 
 		listViewThisWeek = (ListView) findViewById(R.id.listViewThisWeek);
 		listViewThisMonth = (ListView) findViewById(R.id.listViewThisMonth);
 		listViewAll = (ListView) findViewById(R.id.listViewAll);
 
 		run_data = readfromDB();
 
 		RunAdapter adapter = new RunAdapter(this,
 				R.layout.history_run_list_item, run_data);
 		listViewThisWeek.setAdapter(adapter);
 		listViewThisMonth.setAdapter(adapter);
 		listViewAll.setAdapter(adapter);
 		listViewThisWeek.setOnItemClickListener(this);
 		listViewThisMonth.setOnItemClickListener(this);
 		listViewAll.setOnItemClickListener(this);
 		registerForContextMenu(listViewThisWeek);
 		registerForContextMenu(listViewThisMonth);
 		registerForContextMenu(listViewAll);
 	}
 
 	private List<SingleRun> readfromDB() {
 		Database db = new Database(this);
 		List<SingleRun> runs;
 		runs = db.getAllRuns();
		if (runs == null) {
			runs = new ArrayList<SingleRun>();
			RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayoutNoRunHistory);
			rl.setVisibility(View.VISIBLE);
			tabHost.setVisibility(View.GONE);
			prepareGestureListener();
		}
 		return runs;
 	}
 
 	private void prepareGestureListener() {
 		// Gesture detection
 		gestureDetector = new GestureDetector(this, new MyGestureDetector(this,
 				false, false, true, false));
 		gestureListener = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				return gestureDetector.onTouchEvent(event);
 			}
 		};
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return gestureListener.onTouch(null, event);
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		overridePendingTransition(R.anim.in_right_anim, R.anim.out_right_anim);
 
 	}
 
 	@Override
 	public void onLeftToRightSwipe() {
 
 		finish();
 		overridePendingTransition(R.anim.in_right_anim, R.anim.out_right_anim);
 	}
 
 	@Override
 	public void onRightToLeftSwipe() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUpToDownSwipe() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onDownToUpSwipe() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void addListeners() {
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapter, View view, int position,
 			long id) {
 		Intent intent = new Intent(HistoryActivity.this,
 				SingleRunHistoryActivity.class);
 		SingleRun selectedValue = (SingleRun) adapter
 				.getItemAtPosition(position);
 		intent.putExtra(ID_TAG, selectedValue.getRunID());
 		startActivity(intent);
 		overridePendingTransition(R.anim.in_left_anim, R.anim.out_left_anim);
 		Log.e("S", "not started");
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		switch (v.getId()) {
 		case R.id.listViewAll:
 		case R.id.listViewThisMonth:
 		case R.id.listViewThisWeek:
 			MenuInflater inflater = getMenuInflater();
 			inflater.inflate(R.menu.context_menu, menu);
 			menu.setHeaderTitle(R.string.menu_ctx_actions);
 			break;
 		default:
 			break;
 		}
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.delete_action_menuitem:
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 			RunAdapter adapter = null; //when adapters will change use interface here ;)
 			switch (tabHost.getCurrentTab()) {
 			case 0:
 				adapter = (RunAdapter) listViewThisWeek.getAdapter();
 				break;
 			case 1:
 				adapter = (RunAdapter) listViewThisMonth.getAdapter();
 				break;
 			case 2:
 				adapter = (RunAdapter) listViewAll.getAdapter();
 				break;
 			default:
 				break;
 			}
 			if (adapter != null) {
 				SingleRun toDelete = adapter.getItem(info.position);
 				adapter.remove(toDelete);
 				Database db = new Database(this);
 				db.deleteRun(toDelete.getRunID());
 				db.close();
 			}
 			break;
 		default:
 			break;
 		}
 		return super.onContextItemSelected(item);
 	}
 }
