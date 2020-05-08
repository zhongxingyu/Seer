 package com.scarredions.stab;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class STPersonListAdapter extends BaseAdapter {
 
 	private final String[] items = {"1", "2", "3"};
 	private Context mContext;
 	
 	public STPersonListAdapter(Context c) {
 		mContext = c;
 	}
 	
 	public int getCount() {
 		return items.length;
 	}
 
 	public Object getItem(int position) {
 		return items[position];
 	}
 
 	public long getItemId(int position) {
 		return items[position].hashCode();
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv = new TextView(mContext);
		tv.setText(items[position]);
		tv.setHeight(100);
		tv.setWidth(100);
		tv.setGravity(android.view.Gravity.CENTER);
		return tv;
 	}
 
 }
