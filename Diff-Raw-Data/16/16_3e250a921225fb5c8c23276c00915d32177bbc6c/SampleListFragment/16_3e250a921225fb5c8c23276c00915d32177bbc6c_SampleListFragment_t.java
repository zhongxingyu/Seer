 package com.intro.compintro.innerfragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.intro.compintro.R;
 
 public class SampleListFragment extends SherlockFragment implements
 		OnScrollListener {
 
 	public static final String CONTENT_KEY = "content_key";
 
 	private String content;
 	private int lastVisibleIndex = 0;
 	private SampleAdapter adapter;
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View convertView = inflater.inflate(R.layout.headerview_list, null);
 		// get content
 		content = getArguments().getString(CONTENT_KEY);
 		//
 		ListView listview = (ListView) convertView.findViewById(R.id.news_list);
 		// add header view
 		ImageView imageview = (ImageView) inflater.inflate(
 				R.layout.listview_image_item, null);
 		imageview.setImageResource(R.drawable.biz_plugin_weather_beijin);
 		listview.addHeaderView(imageview);
 		// add footer view
 		View loadingLayout = inflater.inflate(R.layout.slip_to_padding_refresh,
 				null);
 		listview.addFooterView(loadingLayout);
 		// set adapter
 		adapter = new SampleAdapter(getSherlockActivity());
 		// get from server, do task in background
 		List<SampleItem> initItems = new ArrayList<SampleItem>();
 		for (int i = 0; i < 20; i++) {
			initItems.add(new SampleItem(content,
 					android.R.drawable.ic_menu_search));
 		}
		adapter.addAll(initItems);
 		listview.setAdapter(adapter);
 		listview.setOnScrollListener(this);
 
 		return convertView;
 	}
 
 	private class SampleItem {
 		public String tag;
 		public int iconRes;
 
 		public SampleItem(String tag, int iconRes) {
 			this.tag = tag;
 			this.iconRes = iconRes;
 		}
 	}
 
 	public class SampleAdapter extends ArrayAdapter<SampleItem> {
 
 		public SampleAdapter(Context context) {
 			super(context, 0);
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = LayoutInflater.from(getContext()).inflate(
 						R.layout.row, null);
 			}
 			ImageView icon = (ImageView) convertView
 					.findViewById(R.id.row_icon);
 			icon.setImageResource(getItem(position).iconRes);
 			TextView title = (TextView) convertView
 					.findViewById(R.id.row_title);
 			title.setText(getItem(position).tag);
 
 			return convertView;
 		}
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
		// header view considered
		lastVisibleIndex = firstVisibleItem + visibleItemCount - 2;
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		if (lastVisibleIndex == adapter.getCount()
 				&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			// get more 20 from server (do this task in other thread)
 			List<SampleItem> moreItems = new ArrayList<SampleItem>();
 			for (int i = 0; i < 20; i++) {
				moreItems.add(new SampleItem(content,
 						android.R.drawable.ic_menu_search));
 			}
			adapter.addAll(moreItems);
 			adapter.notifyDataSetChanged();
 		}
 	}
 }
