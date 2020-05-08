 /**
  * Copyright 2010 Kurtis Nusbaum
  *
  * This file is part of LinkSchedule.  
  *
  * LinkSchedule is free software: you can 
  * redistribute it and/or modify it under the terms of the GNU General Public 
  * License as published by the Free Software Foundation, either version 2 of the 
  * License, or (at your option) any later version.  
  *
  * LinkSchedule is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
  * for more details.  You should have received a copy of the GNU  General 
  * Public License along with LinkSchedule. If not, see 
  * http://www.gnu.org/licenses/.
  */
 
 package org.klnusbaum.linkschedule;
 
 import android.app.ListActivity;
 import android.content.BroadcastReceiver;
 import android.os.Bundle;
 import android.view.ViewGroup;
 import android.view.View;
 import android.view.LayoutInflater;
 import android.widget.TextView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.content.IntentFilter;
 import android.content.Intent;
 import android.content.Context;
 import android.content.res.Resources;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.net.Uri;
 import android.view.Gravity;
 import android.graphics.drawable.ColorDrawable;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class BusStopActivity extends ListActivity implements Refreshable{
 	private LinkSchedule linkSchedule;
 	private String busStop;
 	public static final String EXTRA_STOPNAME = "stop_name";
 	private TimeChangeReceiver timeChangeReceiver;
 
   @Override
   protected void onCreate(Bundle savedInstanceState){
     super.onCreate(savedInstanceState);
 		
 		busStop = getIntent().getStringExtra(EXTRA_STOPNAME);
 		TextView header = new TextView(this);
 		header.setText(busStop);
 		header.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
 		header.setTextSize(30);
 		ListView lv = getListView();
 
 		lv.addHeaderView(header, busStop, false);
 		lv.setDividerHeight(0);
 		lv.setDivider(new ColorDrawable(0x00FFFFFF));
 		
 		
 		linkSchedule = LinkSchedule.getLinkSchedule(getResources());
 		setListAdapter(new BusStopAdapter(
 			this, R.layout.stop_time_item, linkSchedule.getSnapshot(busStop)));
 
 		timeChangeReceiver = new TimeChangeReceiver(this);
 		timeChangeReceiver.registerIntents(this);
   }
 
 	@Override
 	protected void onDestroy(){
 		super.onDestroy();
 		unregisterReceiver(timeChangeReceiver);
 	}
 
 	public void refreshSchedule(){
		BusStopAdapter adapter = (BusStopAdapter)getListAdapter();
		adapter.clear();	
		for(String s: linkSchedule.getSnapshot(busStop)){
			adapter.add(s);
		}
 	}
 
 	public void resetSchedule(){
 		linkSchedule.reset();	
 	}
 
 	private class BusStopAdapter extends ArrayAdapter<String>{
     private LayoutInflater mInflater;
 		private Resources res;
 
 		public BusStopAdapter(
 			Context context, int textViewResourceId,
 			List<String> objects)
 		{
 			super(context, textViewResourceId, objects);
 			res = context.getResources();
       mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		public View getView(int position, View convertView, 
 			ViewGroup parent)
 		{
 			TextView toReturn = (TextView)mInflater.inflate(R.layout.stop_time_item, parent, false);
 			if(position == 0){
 				toReturn.setTextSize(12);
				toReturn.setText("Previous Bus: " + getItem(position));
 				toReturn.setPadding(0,0,0,0);
 			}
 			else if(position == 1){
 				toReturn.setTextSize(20);
 				toReturn.setPadding(
 					toReturn.getPaddingLeft(),
 					0,
 					toReturn.getPaddingRight(),
 					0
 				);
				toReturn.setText("Next Bus: " + getItem(position));
 				toReturn.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);
 			}
 			else{
 				toReturn.setText(getItem(position));
 			}
 				
 			return toReturn;
 		}
 
 		public boolean areAllItemsEnabled(){
 			return false;
 		}
 
 		public boolean isEnabled(int position){
 			return false;
 		}
 	}
 
 }
