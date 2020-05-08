 package com.seawolfsanctuary.tmt;
 
 import android.app.ExpandableListActivity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 
 public class DeepDiveListSavedActivity extends ExpandableListActivity {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setListAdapter(new DeepDiveListSavedAdapter(getBaseContext()));
 		ExpandableListView lv = getExpandableListView();
 		lv.setTextFilterEnabled(true);
 	}
 
 	class DeepDiveListSavedAdapter extends BaseExpandableListAdapter {
 		private Context context;
 
 		public DeepDiveListSavedAdapter(Context context) {
			context = context;
 		}
 
 		@Override
 		public Object getChild(int groupPosition, int childPosition) {
 			return null;
 		}
 
 		@Override
 		public long getChildId(int groupPosition, int childPosition) {
 			return 0;
 		}
 
 		@Override
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
 			return null;
 		}
 
 		@Override
 		public int getChildrenCount(int groupPosition) {
 			return 0;
 		}
 
 		@Override
 		public Object getGroup(int groupPosition) {
 			return null;
 		}
 
 		@Override
 		public int getGroupCount() {
 			return 0;
 		}
 
 		@Override
 		public long getGroupId(int groupPosition) {
 			return 0;
 		}
 
 		@Override
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 			return null;
 		}
 
 		@Override
 		public boolean hasStableIds() {
 			return false;
 		}
 
 		@Override
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			return false;
 		}
 
 	}
 
 }
