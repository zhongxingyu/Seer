 package com.example.seniordesignapp;
 
 import java.util.ArrayList;
 
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.widget.PopupWindow;
 
 public class HomePageFragment extends Fragment {
 
 	ExpandableListView lv;
 	ToggleButton tButton;
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
     private Cursor mCursor;
     private static String TAG = "HomePageFramentDynamicLog";
 	//String timeStamp;
 	private String[] groups;
 	private Button refreshButton;
 	class MyExpandableListAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener, OnClickListener {
 
 		private String[] groups= setGroupData();
 		private String[][] children = setChildGroupData();
 		private ArrayList<String> childItem; 
 		
 		public String[] setGroupData() {// WIP - Hard Code for Now..
 			
 			ArrayList<String> groupItem = new ArrayList<String>();
 			childItem = new ArrayList<String>();
 			//ArrayList<String> childItem = new ArrayList<String>();
 			String timeStamp;
 			String fname;
 			String GL;
 			String item;
 			mDbHelper = new DatabaseHelper(getActivity());
  			mDb = mDbHelper.getWritableDatabase();
  			String sql = "SELECT * FROM foodGPS  ORDER BY GPS_time DESC LIMIT 3";
 			mCursor = mDb.rawQuery(sql,null);
 		
 /*			if (mCursor.getCount() > 0) { // now it is taking the first
  				// match
  				// WIP fix later
 				mCursor.moveToFirst();
  				timeStamp = mCursor.getString(mCursor.getColumnIndex("GPS_time"));
  				groupItem.add(timeStamp);
  			} else {
  				Log.d(TAG,"does not exist in food database! Add it");
  				//groupItem.add("");
  			}
 			*/
 			
 			if(mCursor.getCount()>0){ //now it is taking the first match WIP fix later
 				Log.d(TAG,"Adding items to Dynamic Log");
 				mCursor.moveToFirst();
 				timeStamp = mCursor.getString(mCursor.getColumnIndex("GPS_time"));
 				timeStamp = timeStamp.substring(4, 6) + "/" + 
 							timeStamp.substring(6, 8) + " "+ 
 							timeStamp.substring(8, 10) + ":" + 
 							timeStamp.substring(10, 12);
 				fname = mCursor.getString(mCursor.getColumnIndex("food_name"));
 				fname = fname.substring(0, 18)  + "...";
 				GL = mCursor.getString(mCursor.getColumnIndex("GL"));
 				item = timeStamp + " Had" + fname;
  				groupItem.add(item);
  				childItem.add("GL = " + GL);
 					while (!mCursor.isLast()) {
 						mCursor.moveToNext();
 						timeStamp = mCursor.getString(mCursor.getColumnIndex("GPS_time"));
 						timeStamp = timeStamp.substring(4, 6) + "/" + 
 									timeStamp.substring(6, 8) + " "+ 
 									timeStamp.substring(8, 10) + ":" + 
 									timeStamp.substring(10, 12);
 						fname = mCursor.getString(mCursor.getColumnIndex("food_name"));
 						fname = fname.substring(0, 18)  + "...";
 						GL = mCursor.getString(mCursor.getColumnIndex("GL"));
 						item = timeStamp + " Had" + fname;
 		 				groupItem.add(item);
 		 				childItem.add("GL = " + GL);
 					}
 					
 			}
 			else{
 				Log.d(TAG,"Dynamic Log is empty");
 				groupItem.add("Empty");
 				childItem.add("Empty");
 				showToastMessage("Nothing is in the database...");
 			}
 			
 			
 			
 			
 		/*	groupItem.add("11:35 Ate 1 Hamburger");
 			groupItem.add("4:00 Biked for 20 minutes");
 			groupItem.add("5:21 Climed 2 flights of stairs");*/
 
 			String[] stockArr = new String[groupItem.size()];
 			stockArr = groupItem.toArray(stockArr);
 
 			return stockArr;
 		}
 
 		
 		
 		
 		public String[][] setChildGroupData() { // WIP - hard coding
 		// /**
 		// * Add Data For activity1
 		// */
 		// ArrayList<Object> childItem = new ArrayList<Object>();
 		// ArrayList<String> child = new ArrayList<String>();
 		// child.add("Accelerometer Data");
 		// child.add("9:30 - 10:00");
 		// childItem.add(child);
 		//
 		// /**
 		// * Add Data For activity2
 		// */
 		// child = new ArrayList<String>();
 		// child.add("Manual Input Data");
 		// child.add("11:35");
 		// childItem.add(child);
 		// /**
 		// * Add Data For activity3
 		// */
 		// child = new ArrayList<String>();
 		// child.add("Accelerometer Data");
 		// child.add("3:40 - 4:00");
 		// childItem.add(child);
 		// /**
 		// * Add Data For activity4
 		// */
 		// child = new ArrayList<String>();
 		// child.add("Accelerometer Data");
 		// child.add("5:20 - 5:21");
 		// childItem.add(child);
 		//
 			
 			String[][] storeChild = new String[childItem.size()][1];
 			
 			for (int i = 0; i < childItem.size(); i++ ){
 				storeChild[i][0] = childItem.get(i);
 			}
 			
 /*			String[][] childItem = { //{ "Accelerometer Data", "9:30 - 10:00" },
 					{ "Voice Input Data", "11:35" },
 					{ "Accelerometer Data", "3:40 - 4:00" },
 					{ "Accelerometer Data", "5:20 - 5:21" } };
 			*/
 			return storeChild; 
 			//return null;
 		}
 
 		// public boolean onCreateOptionsMenu(Menu menu) {
 		// getMenuInflater().inflate(R.menu.activity_home_page, menu);
 		// return true;
 		// }
 
 		public Object getChild(int groupPosition, int childPosition) {
 			return children[groupPosition][childPosition];
 		}
 
 		public long getChildId(int groupPosition, int childPosition) {
 			return childPosition;
 		}
 
 		public int getChildrenCount(int groupPosition) {
 			return children[groupPosition].length;
 		}
 
 		public TextView getGenericView() {
 			// Layout parameters for the ExpandableListView
 			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
 					ViewGroup.LayoutParams.MATCH_PARENT, 64);
 
 			TextView textView = new TextView(
 					HomePageFragment.this.getActivity());
 			textView.setLayoutParams(lp);
 			// Center the text vertically
 			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
 			// Set the text starting position
 			textView.setPadding(36, 0, 0, 0);
 			
 			return textView;
 		}
 
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
 			View childView = convertView;
 			if (childView == null) {
 				LayoutInflater vi = (LayoutInflater) getActivity()
 						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				childView = vi.inflate(R.layout.childrow, null);
 			}
 			TextView childText = (TextView) childView
 					.findViewById(R.id.child_name);
 			if (childText != null) {
 				childText.setText(getChild(groupPosition, childPosition)
 						.toString());
 			}
 
 			return childText;
 
 		}
 
 		public Object getGroup(int groupPosition) {
 			return groups[groupPosition];
 		}
 
 		public int getGroupCount() {
 			return groups.length;
 		}
 
 		public long getGroupId(int groupPosition) {
 			return groupPosition;
 		}
 
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 			View parentView = convertView;
 			if (parentView == null) {
 				LayoutInflater vi = (LayoutInflater) getActivity()
 						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				parentView = vi.inflate(R.layout.grouprow, null);
 			}
 			TextView parentText = (TextView) parentView
 					.findViewById(R.id.group_name);
 			if (parentText != null) {
 				parentText.setText(getGroup(groupPosition).toString());
 			}
 
 			return parentText;
 
 		}
 
 		public boolean hasStableIds() {
 			return true;
 		}
 
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			return true;
 		}
 
 
 
 
 		@Override
 		public boolean onChildClick(ExpandableListView parent, View v,
 				int groupPosition, int childPosition, long id) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 
 
 
 		@Override
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			
 			
 		}
 
 
 	}
 
 	Gauge meter; 
 	//Button add;
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		if (container == null) {
 			// We have different layouts, and in one of them this
 			// fragment's containing frame doesn't exist. The fragment
 			// may still be created from its saved state, but there is
 			// no reason to try to create its view hierarchy because it
 			// won't be displayed. Note this is not needed -- we could
 			// just run the code below, where we would create and return
 			// the view hierarchy; it would just never be used.
 			return null;
 		}
 
 		LinearLayout mlinearLayout = (LinearLayout) inflater.inflate(
 				R.layout.activity_home_page, container, false);

 		//meter = (Gauge) mlinearLayout.findViewById(R.id.meter);
 		//meter.setValue(30);
 		
 		//meter.setValue(temp);
 		//meter.setOnClickListener(this);
 		
 		//add = (Button) mlinearLayout.findViewById(R.id.add);
 		//add.setOnClickListener(this);
 		
 		tButton = (ToggleButton) mlinearLayout.findViewById(R.id.toggleButton1);
 
 		tButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 
 				// if(isChecked){
 				// tvStateofToggleButton.setText("ON");
 				// }else{
 				// tvStateofToggleButton.setText("OFF");
 				// }
 
 			}
 		});
 
 		lv = (ExpandableListView) mlinearLayout.findViewById(R.id.log_list);
 		MyExpandableListAdapter expandableAdapter = new MyExpandableListAdapter();
 		lv.setAdapter(expandableAdapter);
 		lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
 		PopupWindow pw;	
 			@Override
 			public boolean onChildClick(ExpandableListView parent, View v,
 					int groupPosition, int childPosition, long id) {
 				showToastMessage("Work! + ParentID =" + groupPosition + " ChildID = " + childPosition );
 				
 				
 		        //We need to get the instance of the LayoutInflater, use the context of this activity
 		        LayoutInflater inflater = (LayoutInflater) getActivity()
 		                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		        //Inflate the view from a predefined XML layout
 		        View layout = inflater.inflate(R.layout.popup_layout,
 		                (ViewGroup) getActivity().findViewById(R.id.popup_element));
 				 // create a 300px width and 470px height PopupWindow
 		        pw = new PopupWindow(layout, 375, 600, true);
 		        // display the popup in the center
 		        pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
 		        Button cancelButton = (Button) layout.findViewById(R.id.end_data_send_button);
 		        
 		        cancelButton.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						// TODO Auto-generated method stub
 						pw.dismiss();
 					}
 				});
 		        
 				return false;
 			}
 		});
 		refreshButton = (Button) mlinearLayout.findViewById(R.id.testing);
 		refreshButton.setOnClickListener( new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(getActivity(), MainActivity.class );
 				showToastMessage("Refreshing");
 				startActivity(i);
 				//finish();
 				
 			}
 		});
 
 		return mlinearLayout;
 	}
 
 	
 
 	float temp = (float) 20;
 	
 /*	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.add:
 			temp = temp + 20;
 			meter.setValue(temp);
 			//showToastMessage("It works!!!!");
 			showToastMessage("=" + meter.getValue() );
 			break;
 		}
 		return;
 		
 	}*/
 	
 	/* Toast Method */
 	void showToastMessage(String message) {
 		  Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
 	}
 
 }
 	 
 
