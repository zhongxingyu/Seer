 package com.dbstar.app.help;
 
 import java.io.InputStream;
 import java.util.List;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.dbstar.R;
 import com.dbstar.app.GDBaseActivity;
 import com.dbstar.widget.GDAdapterView;
 
 public class GDHelpActivity extends GDBaseActivity {
 	private static final String TAG = "GDHelpActivity";
 
 	private ListView mHeaderView;
 	private ListAdapter mAdapter;
 	private WebView mContentView;
 	ItemHeader[] mItems;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.help_view);
 
 		initializeView();
 
 		if (mMenuPath != null) {
 			String[] menuArray = mMenuPath.split(MENU_STRING_DELIMITER);
 			showMenuPath(menuArray);
 		}
 	}
 
 	public void initializeView() {
 		super.initializeView();
 
 		mHeaderView = (ListView) findViewById(R.id.list);
 		mContentView = (WebView) findViewById(R.id.web_view);
 
 		WebViewClient wvClient = new WebViewClient();
 		WebChromeClient chromeClient = new WebChromeClient();
 		mContentView.setWebViewClient(wvClient);
 		mContentView.setWebChromeClient(chromeClient);
 		mContentView.setBackgroundColor(Color.TRANSPARENT);
 		mContentView.getSettings().setDefaultTextEncodingName("utf-8");
 
		ItemHeader[] items = new ItemHeader[3];
 		ItemHeader item = new ItemHeader();
 		item.Title = getResources().getString(
 				R.string.help_title_remote_control);
 		item.PageUrl = "help/remotecontrol_helppage.html";
 		items[0] = item;
 
 		item = new ItemHeader();
 		item.Title = getResources().getString(R.string.help_title_settings);
 		item.PageUrl = "help/settings_helppage.html";
 		items[1] = item;
 
 		item = new ItemHeader();
 		item.Title = getResources().getString(R.string.help_title_failure);
 		item.PageUrl = "help/failure_helppage.html";
 		items[2] = item;
 
 		mItems = items;
 
 		HeaderAdapter adapter = new HeaderAdapter(this);
 		adapter.setDataset(items);
 		mHeaderView.setAdapter(adapter);
 
 		mHeaderView.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int position, long id) {
 				ItemHeader item = mItems[position];
 				String pageUrl = item.PageUrl;
 				loadPage(pageUrl);
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 
 			}
 		});
 	}
 
 	public void onStart() {
 		super.onStart();
 
 	}
 
 	void loadPage(String url) {
 		mContentView.loadUrl("file:///android_asset/" + url);
 	}
 
 	class ItemHeader {
 		public String Title;
 		public String PageUrl;
 	}
 
 	private static class HeaderAdapter extends BaseAdapter {
 		private static class HeaderViewHolder {
 			TextView title;
 		}
 
 		private LayoutInflater mInflater;
 
 		private ItemHeader[] mDataset;
 
 		public HeaderAdapter(Context context) {
 			mInflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		public void setDataset(ItemHeader[] data) {
 			mDataset = data;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			HeaderViewHolder holder;
 			View view;
 
 			if (convertView == null) {
 				view = mInflater.inflate(R.layout.help_header_item, parent,
 						false);
 				holder = new HeaderViewHolder();
 				holder.title = (TextView) view.findViewById(R.id.title);
 				view.setTag(holder);
 			} else {
 				view = convertView;
 				holder = (HeaderViewHolder) view.getTag();
 			}
 
 			holder.title.setText(mDataset[position].Title);
 
 			return view;
 		}
 
 		@Override
 		public int getCount() {
 			if (mDataset != null) {
 				return mDataset.length;
 			}
 			return 0;
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int arg0) {
 			return 0;
 		}
 	}
 
 }
