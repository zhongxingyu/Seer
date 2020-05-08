 package com.gdtm.app.adapter;
 
 import java.util.ArrayList;
 
 import com.gdtm.app.R;
 import com.gdtm.app.activity.CLDetailActivity;
 import com.gdtm.app.activity.CCDetailActivity;
 import com.gdtm.app.helper.DatabaseHelper;
 import com.gdtm.app.pojo.CLDataPojo;
 import com.gdtm.app.pojo.CLSubDataPojo;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.CheckedTextView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author Nari Kim Shin (wassupnari@gmail.com)
  */
 
 public class CLExpandableListAdapter extends BaseExpandableListAdapter {
 
 	private ArrayList<String> mGroupItem;
 	private ArrayList<String> mTempChild;
 	private ArrayList<Object> mChildItem = new ArrayList<Object>();
 
 	private LayoutInflater mInflater;
 	private Activity mActivity;
 
 	private TextView text;
 	private TextView mDone;
 
 	private DatabaseHelper mDB;
 
 	private CLDataPojo mDataList;
 	private CLSubDataPojo mData;
 
 	public CLExpandableListAdapter(ArrayList<String> grList, ArrayList<Object> childItem) {
 
 		mGroupItem = grList;
 		mChildItem = childItem;
 	}
 
 	public void setInflater(LayoutInflater inflater, Activity activity) {
 
 		mInflater = inflater;
 		mActivity = activity;
 		mDB = new DatabaseHelper(mActivity);
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
 	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
 			View convertView, ViewGroup parent) {
 
 		mTempChild = (ArrayList<String>) mChildItem.get(groupPosition);
 
 		if (convertView == null) {
 			// LayoutInflater inflater = null;
 			convertView = mInflater.inflate(R.layout.adapter_list_child_row, null);
 		}
 		text = (TextView) convertView.findViewById(R.id.child_row);
 		text.setText(mTempChild.get(childPosition));
 
 		mDone = (TextView) convertView.findViewById(R.id.child_row_done);
 		boolean isComplete = mDB.getUserCLData(groupPosition).getSubData().get(childPosition)
 				.getComplete();
 
 		if (isComplete) {
 			mDone.setVisibility(View.VISIBLE);
 		} else {
 			mDone.setVisibility(View.GONE);
 		}
 
 		convertView.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				Intent speechView = new Intent(mActivity, CLDetailActivity.class);
 				speechView.putExtra("cl_group_id", groupPosition);
 				speechView.putExtra("cl_child_id", childPosition);
 				speechView.putExtra("cl_detail_title", mTempChild.get(childPosition));
 				mActivity.startActivity(speechView);
 			}
 		});
 		return convertView;
 	}
 
 	@Override
 	public int getChildrenCount(int groupPosition) {
 
 		return ((ArrayList<String>) mChildItem.get(groupPosition)).size();
 	}
 
 	@Override
 	public Object getGroup(int groupPosition) {
 
 		return null;
 	}
 
 	@Override
 	public int getGroupCount() {
 
 		return mGroupItem.size();
 	}
 
 	@Override
 	public long getGroupId(int groupPosition) {
 
 		return 0;
 	}
 
 	@Override
 	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup arg3) {
 
 		mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		if (convertView == null) {
 			convertView = mInflater.inflate(R.layout.adapter_list_group_row, null);
 		}
 		TextView text = (TextView) convertView.findViewById(R.id.group_row);
 		text.setText(mGroupItem.get(groupPosition));
 		TextView complete = (TextView) convertView.findViewById(R.id.group_complete);
		
		mDataList = mDB.getUserCLData(groupPosition);
 		if (CalculateCompletion(groupPosition)) {
 			mDataList.setComplete(true);
 			complete.setVisibility(View.VISIBLE);
 		} else {
			mDataList.setComplete(false);
 			complete.setVisibility(View.GONE);
 		}
		mDB.updateCL(mDataList);
 
 		// ((CheckedTextView)
 		// convertView).setText(mGroupItem.get(groupPosition));
 		// ((CheckedTextView) convertView).setChecked(isExpanded);
 		return convertView;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 
 		return false;
 	}
 
 	@Override
 	public boolean isChildSelectable(int arg0, int arg1) {
 
 		return false;
 	}
 
 	@Override
 	public void onGroupCollapsed(int groupPosition) {
 
 		super.onGroupCollapsed(groupPosition);
 	}
 
 	@Override
 	public void onGroupExpanded(int groupPosition) {
 
 		super.onGroupExpanded(groupPosition);
 	}
 
 	public boolean CalculateCompletion(int groupPosition) {
 		boolean isComplete = false;
 		int count = 0;
 		ArrayList<CLSubDataPojo> dataList = mDB.getUserCLData(groupPosition).getSubData();
 		for (CLSubDataPojo data : dataList) {
 			if (data.getComplete()) {
 				count += 1;
 			}
 		}
 		switch (groupPosition) {
 		case 0:
 			if (count >= 3) {
 				isComplete = true;
 			}
 			break;
 		case 1:
 			if (count >= 2) {
 				isComplete = true;
 			}
 			break;
 		case 2:
 			if (count >= 3) {
 				isComplete = true;
 			}
 			break;
 		case 3:
 			if (count >= 2 && dataList.get(0).getComplete()) {
 				isComplete = true;
 			}
 			break;
 		case 4:
 			if (count >= 3) {
 				isComplete = true;
 			}
 			break;
 		case 5:
 			if (count >= 1) {
 				isComplete = true;
 			}
 			break;
 		case 6:
 			if (count >= 2) {
 				isComplete = true;
 			}
 			break;
 		case 7:
 			if (count >= 3 && (dataList.get(0).getComplete() || dataList.get(1).getComplete())) {
 				isComplete = true;
 			}
 			break;
 		case 8:
 			if (count >= 1) {
 				isComplete = true;
 			}
 			break;
 		case 9:
 			if ((dataList.get(0).getComplete() && dataList.get(1).getComplete())
 					|| dataList.get(2).getComplete() || dataList.get(3).getComplete()
 					|| dataList.get(4).getComplete() || dataList.get(5).getComplete()
 					|| dataList.get(6).getComplete() || dataList.get(7).getComplete()) {
 				isComplete = true;
 			}
 			break;
 		}
 
 		return isComplete;
 	}
 
 }
