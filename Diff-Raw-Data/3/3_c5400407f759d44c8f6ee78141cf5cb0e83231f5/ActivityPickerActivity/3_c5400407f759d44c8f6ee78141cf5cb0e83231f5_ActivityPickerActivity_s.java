 /**
  * @author AnderWeb <anderweb@gmail.com>
  *
  */
 package com.android.launcher;
 
 import java.util.ArrayList;
 import java.util.List;
 import android.app.ExpandableListActivity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 
 public class ActivityPickerActivity extends ExpandableListActivity {
 	private PackageManager mPackageManager;
     MyExpandableListAdapter mAdapter;
     private final class LoadingTask extends AsyncTask<Void, Void, Void> {
         @Override
         public void onPreExecute() {
             setProgressBarIndeterminateVisibility(true);
             setTitle(getResources().getString(R.string.pref_label_activities_loading));
         }
         @Override
         public Void doInBackground(Void... params) {
             List<PackageInfo> list = mPackageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
             
             for(PackageInfo item : list) {
             	if(item.activities!=null){
 					AppInfoWrapper pkg=new AppInfoWrapper(item);
 					int groupId=mAdapter.addGroup(pkg);
     	        	for(int i=0; i< item.activities.length;i++){
     	        		mAdapter.addChildForGroup(groupId,item.activities[i]);
     	        	}
             	}
             }
 			return null;
         }
         @Override
         public void onPostExecute(Void result) {
             setProgressBarIndeterminateVisibility(false);
             setListAdapter(mAdapter);
             setTitle(getResources().getString(R.string.pref_label_activities));
         }
     }
 
     @Override
     protected void onCreate(Bundle savedState) {
         super.onCreate(savedState);
         mAdapter= new MyExpandableListAdapter();
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.activity_list);
         getExpandableListView().setTextFilterEnabled(true);
         mPackageManager = getPackageManager();
         // Start loading the data
         new LoadingTask().execute();
     }
 	@Override
 	public boolean onChildClick(ExpandableListView parent, View v,
 			int groupPosition, int childPosition, long id) {
         ActivityInfo info = (ActivityInfo) getExpandableListAdapter().getChild(groupPosition, childPosition);
         Intent intent=new Intent();
         intent.putExtra("activityInfo", info);
         setResult(RESULT_OK,intent);
         finish();
         return true;
 	}
 	/**
 	 * ExpandableListAdapter to handle packages and activities
 	 * @author adw
 	 *
 	 */
     public class MyExpandableListAdapter extends BaseExpandableListAdapter {
         private ArrayList<AppInfoWrapper> groups;
         private ArrayList<ArrayList<ActivityInfo>> children;
         
         public MyExpandableListAdapter() {
 			super();
 			groups=new ArrayList<AppInfoWrapper>();
 			children=new ArrayList<ArrayList<ActivityInfo>>();
 		}
         public int addGroup(AppInfoWrapper pkg){
         	int ret=groups.size();
         	groups.add(pkg);
         	children.add(new ArrayList<ActivityInfo>());
         	return ret;
         }
         public void addChildForGroup(int group,ActivityInfo activ){
         	children.get(group).add(activ);
         }
         public ActivityInfo getChild(int groupPosition, int childPosition) {
             //return children[groupPosition][childPosition];
         	return children.get(groupPosition).get(childPosition);
         }
 
         public long getChildId(int groupPosition, int childPosition) {
             return childPosition;
         }
 
         public int getChildrenCount(int groupPosition) {
             //return children[groupPosition].length;
         	return children.get(groupPosition).size();
         }
 
         public TextView getGenericView() {
             // Layout parameters for the ExpandableListView
             AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                     ViewGroup.LayoutParams.FILL_PARENT, 64);
 
             TextView textView = new TextView(ActivityPickerActivity.this);
             textView.setLayoutParams(lp);
             // Center the text vertically
             textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
             // Set the text starting position
             textView.setPadding(36, 0, 0, 0);
             return textView;
         }
 
         public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                 View convertView, ViewGroup parent) {
             TextView textView = getGenericView();
             ActivityInfo activity=getChild(groupPosition, childPosition);
            textView.setText(activity.name);
             return textView;
         }
 
         public AppInfoWrapper getGroup(int groupPosition) {
             //return groups[groupPosition];
         	return groups.get(groupPosition);
         }
 
         public int getGroupCount() {
             //return groups.length;
         	return groups.size();
         }
 
         public long getGroupId(int groupPosition) {
             return groupPosition;
         }
 
         public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                 ViewGroup parent) {
             TextView textView = getGenericView();
             AppInfoWrapper info=getGroup(groupPosition);
             textView.setText(info.getInfo().applicationInfo.loadLabel(mPackageManager));
             textView.setCompoundDrawablesWithIntrinsicBounds(info.getImage(), null, null, null);
             return textView;
         }
 
         public boolean isChildSelectable(int groupPosition, int childPosition) {
             return true;
         }
 
         public boolean hasStableIds() {
             return true;
         }
     }
     private final class AppInfoWrapper {
         private PackageInfo mInfo;
         private Drawable icon;
         public AppInfoWrapper(PackageInfo info) {
             mInfo = info;
             icon=Utilities.createIconThumbnail(mInfo.applicationInfo.loadIcon(mPackageManager),ActivityPickerActivity.this);
         }
 
         @Override
         public String toString() {
             return mInfo.applicationInfo.loadLabel(mPackageManager).toString();
         }
 
         public PackageInfo getInfo() {
             return mInfo;
         }
         public Drawable getImage(){
         	return icon;
         }
     }    
 }
