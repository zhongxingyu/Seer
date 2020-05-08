 package activity.newsfeed;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import util.GetRequest;
 import activity.newsfeed.PullAndLoadListView.OnLoadMoreListener;
 import activity.newsfeed.PullToRefreshListView.OnRefreshListener;
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.ebay.ebayfriend.R;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 
 public class NewsFeedFragment extends Fragment {
 
 	private PullAndLoadListView lv;
 	private NewsFeedItemAdapter adapter;
 	protected ImageLoader imageLoader = ImageLoader.getInstance();
 	Activity activity;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.newsfeed, container, false);
 		TextView windowTitleView = (TextView) getActivity().findViewById(
 				R.id.window_title);
		windowTitleView.setText("News Feeding");
 		this.activity = getActivity();
 		List<NewsFeedItem> itemList = new ArrayList<NewsFeedItem>();
 		lv = (PullAndLoadListView) view.findViewById(R.id.listview);
 		imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
 		adapter = new NewsFeedItemAdapter(getActivity(), itemList, imageLoader,
 				lv);
 		lv.setOnRefreshListener(new OnRefreshListener() {
 			@Override
 			public void onRefresh() {
 				new RefreshNewsFeedTask(adapter, lv).execute();
 			}
 		});
 		lv.setOnLoadMoreListener(new OnLoadMoreListener() {
 
 			@Override
 			public void onLoadMore() {
 				new MoreNewsFeedTask(adapter, lv).execute();
 			}
 		});
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long id) {
 				Intent intent = new Intent(getActivity(), ReplyActivity.class);
 				intent.putExtra("currentNewsFeed", adapter.getItemList().get(position - 1));
 				getActivity().startActivity(intent);
 			}
 		});
 		lv.setLongClickable(true);
 		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				LayoutInflater inflater = activity.getLayoutInflater();
 				View layout = inflater.inflate(R.layout.like_toast, null);
 				Toast toast = new Toast(activity);
 				int[] location = new int[2];
 				view.getLocationOnScreen(location);
 		        toast.setGravity(Gravity.CENTER, location[0], location[1] - 200);
 		        toast.setDuration(Toast.LENGTH_SHORT);
 		        toast.setView(layout);
 		        toast.show();
 				return true;
 			}
 		});
 		lv.setAdapter(adapter);
 		new RefreshNewsFeedTask(adapter, lv).execute();
 		return view;
 	}
 
 	private class RefreshNewsFeedTask extends AsyncTask<Void, Void, List<NewsFeedItem>> {
 		private NewsFeedItemAdapter adapter;
 		private PullAndLoadListView lv;
 		
 		public RefreshNewsFeedTask(NewsFeedItemAdapter adapter, PullAndLoadListView lv) {
 			this.adapter = adapter;
 			this.lv = lv;
 		}
 		@Override
 		protected List<NewsFeedItem> doInBackground(Void... params) {
 			return getNewsFeedList(0);
 		}
 
 		@Override
 		protected void onPostExecute(List<NewsFeedItem> result) {
 			super.onPostExecute(result);
 			adapter.updateList(result);
 			adapter.resetPage();
 			lv.onRefreshComplete();
 		}
 	}
 	
 	private class MoreNewsFeedTask extends AsyncTask<Void, Void, List<NewsFeedItem>> {
 		private NewsFeedItemAdapter adapter;
 		private PullAndLoadListView lv;
 		
 		public MoreNewsFeedTask(NewsFeedItemAdapter adapter, PullAndLoadListView lv) {
 			this.adapter = adapter;
 			this.lv = lv;
 		}
 		@Override
 		protected List<NewsFeedItem> doInBackground(Void... params) {
 			int currentPage = adapter.getCurrentPage() + 1;
 			List<NewsFeedItem> fetchList = getNewsFeedList(currentPage);
 			if(fetchList.size() > 0) adapter.incrementCurrentPage();
 			return fetchList;
 		}
 
 		@Override
 		protected void onPostExecute(List<NewsFeedItem> result) {
 			super.onPostExecute(result);
 			List<NewsFeedItem> currentList = adapter.getItemList();
 			for(NewsFeedItem item: result){
 				currentList.add(item);
 			}
 			adapter.notifyDataSetChanged();
 			int index = lv.getFirstVisiblePosition();
 			View v = lv.getChildAt(0);
 			int top = (v == null) ? 0 : v.getTop();
 
 			lv.setSelectionFromTop(index, top);
 			if (result.size() == 0){
 				lv.notifyNoMore();
 			}
 			lv.onLoadMoreComplete();
 		}
 	}
 	
 	private List<NewsFeedItem> getNewsFeedList(int page){
 		List<NewsFeedItem> list = new ArrayList<NewsFeedItem>();
 		String getURL = Constants.GET_NEWSFEED_URL_PREFIX + page;
 		GetRequest getRequest = new GetRequest(getURL);
 		String jsonResult = getRequest.getContent();
 		Log.v("NewsFeedFragment", "Request URL: " + getURL);
 		Log.v("NewsFeedFragment", "Response: " + jsonResult);
 		if (jsonResult == null) {
 			Log.e("NewsFeedFragment", "Json Parse Error");
 		} else {
 			try {
 				JSONArray itemArray = new JSONArray(jsonResult);
 				for (int i = 0; i < itemArray.length(); i++) {
 					JSONObject item = itemArray.getJSONObject(i);
 					String imageURL = item.getString("picture");
 					String voiceURL = item.getString("voice");
 					JSONObject person = item.getJSONObject("author");
 					String portraitURL = person.getString("portrait");
 					String authorName = person.getString("name");
 					String commentsURL = item.getString("comments");
 					String goodURL = item.getString("good");
 					NewsFeedItem newsFeedItem = new NewsFeedItem(imageURL,
 							portraitURL, authorName, voiceURL, commentsURL, goodURL);
 					list.add(newsFeedItem);
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 				Log.e("NewsFeedFragment", "Parse Json Error");
 			}
 		}
 		return list;
 	}
 }
