 package com.zjuhjz.yacleaner;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.zjuhjz.yacleaner.customclass.AutoStartAppListAdapter;
 import com.zjuhjz.yacleaner.customclass.ProcessListAdapter;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.support.v4.app.FragmentActivity;
 
 public class AutoStartAppList extends ListFragment implements OnItemClickListener {
 	AutoStartAppListAdapter autoStartAppListAdapter;
 	List<HashMap<String,String>> appInfoList;
 	private static final String TAG = "yacleanerlog";
 	AutoStartInfo  autoStartInfo ;
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setHasOptionsMenu(true);
 		//this.getListView().setOnItemClickListener(this);
 		PackageManager packageManager = getActivity().getPackageManager();
 		HashMap<String,String> appInfo = null;
 		appInfoList = new ArrayList<HashMap<String,String>>();
 		autoStartInfo = new AutoStartInfo(getActivity());
 		this.getListView().setOnItemClickListener(this);
         showAppInfo();
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
 				showAppInfo();
 			}
 		});
 		
 		return view;
 	}
 	
 	private void showAppInfo(){
 		Context context = getActivity();
 		
 		autoStartAppListAdapter = new AutoStartAppListAdapter(context,
 				autoStartInfo.intentsInfoList, R.layout.autostart_app_list_item,
 				new String[] { "IntentName" }, new int[] {
 						R.id.autostart_app_name});
 		setListAdapter(autoStartAppListAdapter);
 	}
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 		FragmentManager fragmentManager =  getFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		IntentsAppList intentsAppList = new IntentsAppList(autoStartInfo.intentsInfoList,this.getActivity()); 
 		fragmentTransaction.add(android.R.id.content, intentsAppList);
 		fragmentTransaction.addToBackStack(null);;
 		fragmentTransaction.commit();
 	}
 }
