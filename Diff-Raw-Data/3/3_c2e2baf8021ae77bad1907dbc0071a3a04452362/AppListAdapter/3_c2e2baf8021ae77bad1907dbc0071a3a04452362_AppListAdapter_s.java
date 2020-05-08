 package com.carolineleung.clickcontrols.styled.applist;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.carolineleung.clickcontrols.R;
 
 public class AppListAdapter extends ArrayAdapter<AppEntry> {
 	private LayoutInflater mInflater;
 
 	public AppListAdapter(Context context) {
 		super(context, android.R.layout.simple_list_item_2);
 		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	public void setData(List<AppEntry> appEntriesData) {
 		clear();
 		if (appEntriesData != null) {
 			for (AppEntry appEntry : appEntriesData) {
 				add(appEntry);
 			}
 		}
 	}
 
 	/**
 	 * Populate the items into the list
 	 */
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view;
 		if (convertView != null) {
 			view = convertView;
 		} else {
 			view = mInflater.inflate(R.layout.app_list_item_icon_text, parent, false);
 		}
 		AppEntry item = getItem(position);
 		TextView textView = (TextView) view.findViewById(R.id.app_entry_text);
 		textView.setText(item.getLabel());
		textView.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, null, null);
 		return view;
 	}
 }
