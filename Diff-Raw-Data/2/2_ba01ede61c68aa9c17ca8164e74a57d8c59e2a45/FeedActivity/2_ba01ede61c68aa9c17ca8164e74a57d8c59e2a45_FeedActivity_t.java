 package csci498.csmyth.lunchlist;
 
 import org.mcsoxford.rss.RSSFeed;
 import org.mcsoxford.rss.RSSItem;
 import org.mcsoxford.rss.RSSReader;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class FeedActivity extends ListActivity {
 	public static final String EXCP_TITLE = "Exception!";
 	public static final String POS_BUTTON_TXT = "OK";
 	public static final String FEED_URL = "csci498.csmyth.lunchlist.FEED_URL";
 	
 	private InstanceState state = null;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		state = (InstanceState)getLastNonConfigurationInstance();
 		
 		if (state == null) {
 			state = new InstanceState();
 			state.task = new FeedTask(this);
 			state.task.execute(getIntent().getStringExtra(FEED_URL));
 		} else {
 			if (state.task != null) {
 				state.task.attach(this);
 			}
 			if (state.feed != null) {
 				setFeed(state.feed);
 			}
 		}
 	}
 	
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		if (state.task != null) {
 			state.task.detach();
 		}
 		
 		return state;
 	}
 	
 	private void setFeed(RSSFeed feed) {
 		state.feed = feed;
 		setListAdapter(new FeedAdapter(feed));
 	}
 	
 	private void goBlooey(Throwable thrwbl) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		
 		builder
 			.setTitle(EXCP_TITLE)
 			.setMessage(thrwbl.toString())
 			.setPositiveButton(POS_BUTTON_TXT, null)
 			.show();
 	}
 	
 	private static class InstanceState {
 		RSSFeed feed = null;
 		FeedTask task = null;
 	}
 	
	private static class FeedTask extends AsyncTask<String, Void, RSSFeed> {
 		private RSSReader reader = new RSSReader();
 		private Exception excp = null;
 		private FeedActivity activity = null;
 		
 		private static final String APP_NAME = "LunchList";
 		private static final String PARSE_FEED_EXCEPTION = "Exception parsing feed";
 		
 		FeedTask(FeedActivity activity) {
 			attach(activity);
 		}
 		
 		void attach(FeedActivity activity) {
 			this.activity = activity;
 		}
 		
 		void detach() {
 			this.activity = null;
 		}
 		
 		@Override
 		public RSSFeed doInBackground(String... urls) {
 			RSSFeed result = null;
 			
 			try {
 				result = reader.load(urls[0]);
 			} catch (Exception excp) {
 				this.excp = excp;
 			}
 			return result;
 		}
 		
 		@Override
 		public void onPostExecute(RSSFeed feed) {
 			if (excp == null) {
 				activity.setFeed(feed);
 			} else {
 				Log.e(APP_NAME, PARSE_FEED_EXCEPTION, excp);
 				activity.goBlooey(excp);
 			}
 		}
 	}
 	
 	private class FeedAdapter extends BaseAdapter {
 		RSSFeed feed = null;
 		
 		FeedAdapter(RSSFeed feed) {
 			super();
 			this.feed = feed;
 		}
 		
 		public int getCount() {
 			return feed.getItems().size();
 		}
 		
 		public Object getItem(int position) {
 			return feed.getItems().get(position);
 		}
 		
 		public long getItemId(int position) {
 			return position;
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 			
 			if (row == null) {
 				LayoutInflater inflater = getLayoutInflater();	
 				row = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
 			}
 			
 			RSSItem item = (RSSItem)getItem(position);
 			((TextView)row).setText(item.getTitle());
 			return row;
 		}
 	}
 
 }
