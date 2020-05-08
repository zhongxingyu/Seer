 package net.jsiq.marketing.fragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.jsiq.marketing.R;
 import net.jsiq.marketing.activity.ContentDisplayActivity;
 import net.jsiq.marketing.adapter.ContentAdapter;
 import net.jsiq.marketing.constants.URLStrings;
 import net.jsiq.marketing.model.ContentItem;
 import net.jsiq.marketing.util.LoaderUtil;
 import net.jsiq.marketing.util.MessageToast;
 import net.jsiq.marketing.util.NetworkUtils;
 import net.jsiq.marketing.view.ViewFlowHeaderView;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class ContentFragment extends SherlockFragment implements
 		OnItemClickListener {
 
 	public static final String CATALOG_ID = "catalog_id";
 
 	private Context context;
 
 	private int catalogId;
 	private List<ContentItem> contentList;
 
 	private ListView listview;
 	private ContentAdapter adapter;
 	private View loadingHintView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.context = getSherlockActivity();
 		this.contentList = new ArrayList<ContentItem>();
 		// get catalogId
 		catalogId = getArguments().getInt(CATALOG_ID);
 	}
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View convertView = inflater.inflate(R.layout.headerview_list, null);
 		listview = (ListView) convertView.findViewById(R.id.content_list);
		listview.setVisibility(View.GONE);
 		loadingHintView = convertView.findViewById(R.id.loadingHint);
		loadingHintView.setVisibility(View.VISIBLE);
 		// top show images urls
 		List<String> urls = new ArrayList<String>();
 		urls.add("http://www.chinaunicom.com.cn/images/wpBananer.jpg");
 		urls.add("http://www.chinaunicom.com.cn/images/wjtBanner.jpg");
 		urls.add("http://www.chinaunicom.com.cn/images/wswBanner.jpg");
 		// add header view
 		View headerView = new ViewFlowHeaderView(context, urls);
 		listview.addHeaderView(headerView);
 
		// TODO list addAll()
 		adapter = new ContentAdapter(context, contentList);
 		listview.setAdapter(adapter);
 		listview.setOnItemClickListener(this);
 		return convertView;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		if (NetworkUtils.isNetworkConnected(context)) {
 			String loadContentUrl = URLStrings.GET_CONTENTS_BY_CATALOGID_PAGENO
 					+ catalogId + "/1";
 			new LoadContentTask().execute(loadContentUrl);
 		} else {
 			MessageToast.showText(context, R.string.notConnected);
 		}
 	}
 
 	class LoadContentTask extends AsyncTask<String, Void, List<ContentItem>> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			adapter.clear();
 		}
 
 		@Override
 		protected List<ContentItem> doInBackground(String... params) {
 			try {
 				return LoaderUtil.loadContentItems(params[0]);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(List<ContentItem> result) {
 			super.onPostExecute(result);
 			if (result == null) {
 				MessageToast.showText(context, R.string.loadFailed);
 			} else {
 				adapter.addAll(result);
 				listview.setVisibility(View.VISIBLE);
 			}
 			loadingHintView.setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
 			long id) {
 		int contentId = contentList.get(position - 1).getContentId();
 		startContentDisplayActivityWithContentId(contentId);
 	}
 
 	private void startContentDisplayActivityWithContentId(int contentId) {
 		Intent i = new Intent("android.intent.action.ContentDisplayActivity");
 		i.putExtra(ContentDisplayActivity.CONTENT_ID, contentId);
 		startActivity(i);
 	}
 
 }
