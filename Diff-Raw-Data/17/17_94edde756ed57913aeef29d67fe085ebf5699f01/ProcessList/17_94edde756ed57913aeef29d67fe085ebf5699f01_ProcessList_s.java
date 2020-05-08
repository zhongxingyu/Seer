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
 import android.widget.Button;
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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 import com.zjuhjz.yacleaner.tool.*;
 
 
 public class ProcessList extends ListFragment {

 	private static List<RunningAppProcessInfo> procList = null;
 	private static MemoryInfo mi = new MemoryInfo();
 	YAMemoryInfo yaMemoryInfo;
 	static final int POPULATE_ID = Menu.FIRST;
 	static final int CLEAR_ID = Menu.FIRST + 1;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// setContentView(R.layout.activity_process_list);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setHasOptionsMenu(true);
 		yaMemoryInfo = new YAMemoryInfo(getActivity());
 		getProcessInfo();
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
 				getProcessInfo();
 				showProcessInfo();
 			}
 		});
 
 		return view;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case POPULATE_ID:
 			getProcessInfo();
 			showProcessInfo();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void killAllProcesses() {
 		Context context = getActivity();
 		ActivityManager activityManager = (ActivityManager) context
 				.getSystemService(Context.ACTIVITY_SERVICE);
 		for (Iterator<HashMap<String, String>> iterator = yaMemoryInfo.processInfoList.iterator(); iterator
 				.hasNext();) {
			HashMap<String, String> processitem = iterator.next();
 			activityManager.killBackgroundProcesses(processitem
 					.get("package_name"));
 		}
 	}
 
 	public void showProcessInfo() {

 		Context context = getActivity();
		YAMemoryInfo yaMemoryInfo = new YAMemoryInfo(context);
 		yaMemoryInfo.refresh();
 		
 		//TODO improve "string from"
 		SimpleAdapter simpleAdapter = new SimpleAdapter(context, yaMemoryInfo.processInfoList,
 				R.layout.process_list_item, new String[] { "app_name" },
 				new int[] { R.id.process_name });
 		
 		setListAdapter(simpleAdapter);
 		long availableMegs = mi.availMem / 1048576L;
 		TextView textview = (TextView) getView().findViewById(
 				R.id.total_process_num);
 
 		textview.setText("Total Process Num: "
 				+ Integer.toString(yaMemoryInfo.processInfoList.size()) + "\nfree RAM:"
 				+ availableMegs + " MB");
 		// total ram status
 		String result = null;
 		
 //		try {
 //			String[] args = { "/system/bin/cat", "/proc/meminfo" };
 //			result = cmdexe.run(args, "/system/bin/");
 //			Log.i("result", "result=" + result);
 //		} catch (IOException ex) {
 //			ex.printStackTrace();
 //		}
 		//textview.setText(result);
 		// long totalMegs = mi.totalMem / 1048576L;
 	}
 
 	public int getProcessInfo() {
 		Context ctext = getActivity();
 		ActivityManager activityManager = (ActivityManager) ctext
 				.getSystemService(Context.ACTIVITY_SERVICE);
 		procList = activityManager.getRunningAppProcesses();
 		activityManager.getMemoryInfo(mi);
 		return procList.size();
 	}
 
 }
