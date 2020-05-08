 package com.zjuhjz.yacleaner;
 
 //import com.example.android.supportv4.R;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.MenuItemCompat;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.MemoryInfo;
 import android.app.ActivityManager.RunningAppProcessInfo;
 import com.zjuhjz.yacleaner.customclass.ProcessListAdapter;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import android.content.Context;
 
 public class ProcessList extends ListFragment implements OnItemClickListener {
 	private static final String TAG = "yacleanerlog";
 
 	//private static List<RunningAppProcessInfo> procList = null;
 	YAMemoryInfo yaMemoryInfo;
 	static final int POPULATE_ID = Menu.FIRST;
 	static final int CLEAR_ID = Menu.FIRST + 1;
 	ProcessListAdapter simpleAdapter;
 
 	//white list
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// load white list
 		
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setHasOptionsMenu(true);
 		yaMemoryInfo = new YAMemoryInfo(getActivity());
 		this.getListView().setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position,
 					long id) {
 				// Do something in response to the click
 			}
 		});
 		this.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		this.getListView().setOnItemClickListener(this);
 		showProcessInfo();
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		MenuItem populateItem = menu.add(Menu.NONE, POPULATE_ID, 0, "Populate");
 		populateItem.setIcon(R.drawable.ic_menu_refresh);
 		MenuItemCompat.setShowAsAction(populateItem,
 				MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// super.onCreateView(inflater, container, savedInstanceState);
 		View view = inflater.inflate(R.layout.activity_process_list, container,
 				false);
 		final View button = view.findViewById(R.id.clean);
 		button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				killAllProcesses();
 				showProcessInfo();
 			}
 		});
 
 		return view;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case POPULATE_ID:
 			showProcessInfo();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	@Override
 	public void onPause(){
		yaMemoryInfo.saveWhiteList();
 		super.onPause();
 	}
 	
 	public void killAllProcesses() {
 		Context context = getActivity();
 		ActivityManager activityManager = (ActivityManager) context
 				.getSystemService(Context.ACTIVITY_SERVICE);
 		HashMap<String, String> processitem;
 		for (Iterator<HashMap<String, String>> iterator = yaMemoryInfo.processInfoList
 				.iterator(); iterator.hasNext();) {
 			processitem = iterator.next();
 			activityManager.killBackgroundProcesses(processitem
 					.get("package_name"));
 		}
 	}
 
 	public void showProcessInfo() {
 		// initial and refresh memoryinfo
 		Context context = getActivity();
 		yaMemoryInfo.refresh();
 
 		// TODO improve "string from"
 		simpleAdapter = new ProcessListAdapter(context,
 				yaMemoryInfo.processInfoList, R.layout.process_list_item,
 				new String[] { "app_name", "memory_usage" }, new int[] {
 						R.id.process_name, R.id.process_memory });
 
 		setListAdapter(simpleAdapter);
 		TextView textview = (TextView) getView().findViewById(
 				R.id.total_process_num);
 
 		textview.setText("Total Process Num: "
 				+ Integer.toString(yaMemoryInfo.processInfoList.size())
 				+ "\nfree RAM:" + yaMemoryInfo.availableMemory + " MB");
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
 			long arg3) {
 		Log.d("yacleanerdebug", position + "");
 		arg1.setSelected(true);
 		simpleAdapter.notifyDataSetChanged();
 	}
 
 }
