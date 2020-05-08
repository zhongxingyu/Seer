 package com.leggo;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 import com.leggo.MainActivity.GetArticles;
 import com.leggo.parsing.AddFeedCommand;
 import com.leggo.parsing.FeedSearchCommand;
 import com.leggo.parsing.GetArticlesCommand;
 import com.leggo.parsing.GetFeedsCommand;
 import com.leggo.parsing.UnsubscribeCommand;
 
 public class ManageActivity extends Activity {
 
 	private static List<Feed> allFeeds = null;
 
	private File sdCard = Environment.getExternalStorageDirectory();
 	public static File filesDir;
 	private SharedPreferences prefs;
 	public static boolean isAdded;
 	private Context context;
 	private Vibrator myVib;
 
 	private int fontSize;
 
 	String currentAccountName;
 
 	public static boolean shouldRestart;
 	public static boolean shouldRefresh;
 	
 	public static LinearLayout feedScroll;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);

 		context = this;
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
 		Theme.setPrefTheme(this);
 
 		filesDir = new File(sdCard + "/Android/data/com.leggo/files");
 		filesDir.mkdirs();
 		currentAccountName = prefs.getString("account_selection", "default");
 		if (currentAccountName.equals("None")
 				|| currentAccountName.equals("default")) {
 			Utils.noAccountAlert(this);
 		}
 
 		else {
 			loadFeeds();
 			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 			Date now = new Date();
			TextView refreshBar = (TextView) findViewById(R.id.main_refresh_bar);
			refreshBar.setText("Last Refreshed: " + df.format(now).toString());
 			
 		}
 
 		shouldRestart = false;
 		shouldRefresh = false;
 		
 		feedScroll = (LinearLayout) findViewById(R.id.feed_list);
 
		setContentView(R.layout.activity_manage);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.manage, menu);
 		return true;
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		fontSize = Integer.parseInt(prefs.getString("fontSize", "16"));
 		
 		if (shouldRestart == true) {
 			Utils.restartActivity(this);
 			shouldRestart = false;
 		}
 		
 
 		currentAccountName = prefs
 				.getString("account_selection", "default");
 		if (currentAccountName.equals("None")
 				|| currentAccountName.equals("default")) {
 			Utils.noAccountAlert(this);
 		}
 		
 		else if (shouldRefresh == true) {
 			GetFeedsCommand refresh = new GetFeedsCommand();
 			GetFeeds get = new GetFeeds(this);
 			get.execute(refresh);
 			shouldRefresh = false;
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadFeeds() {
 		// File output = new File(filesDir, "feeds.htm");
 		if (Utils.networkAvailability(this)) // until login is taken care of
 		{
 			GetFeeds get = new GetFeeds(this);
 			GetFeedsCommand command = new GetFeedsCommand();
 			get.execute(command);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void searchFeeds(String searchText) {
 		// File output = new File(filesDir, "feeds.htm");
 		if (Utils.networkAvailability(this)) // until login is taken care of
 		{
 			SearchFeeds get = new SearchFeeds(this, searchText);
 			FeedSearchCommand command = new FeedSearchCommand();
 			get.execute(command);
 		}
 	}
 
 	public void onEnterSearchText(View v) {
 		EditText uri = (EditText) findViewById(R.id.add_feed_uri);
 		String text = uri.getText().toString();
 
 		if (text == null || text.isEmpty()) {
 			Toast warning = Toast.makeText(this,
 					"Please enter a feed url or search expression.",
 					Toast.LENGTH_SHORT);
 			warning.show();
 			return;
 		}
 
 		if (text.startsWith("http://") || text.startsWith("https://")) {
 			// if user entered a link then try to add feed
 			addFeed(text);
 		} else {
 			// assume user was trying to search for feeds...
 			searchFeeds(text);
 		}
 
 	}
 
 	public void addFeed(String feedUrl) {
 
 		String addlink;
 		try {
 			feedUrl = URLDecoder.decode(feedUrl, "UTF-8");
 			feedUrl = URLEncoder.encode(feedUrl, "UTF-8");
 			addlink = ("addRSS/?url=" + feedUrl);
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			return;
 		}
 		if (Utils.networkAvailability(this)) {
 			AddFeedCommand addMe = new AddFeedCommand(addlink);
 			AddFeed get = new AddFeed();
 			get.execute(addMe);
 
 			loadFeeds();
 			MainActivity.shouldRefresh = true;
 		} else {
 			Toast warning = Toast.makeText(this,
 					"Please connect to the internet to add a feed.",
 					Toast.LENGTH_SHORT);
 			warning.show();
 			return;
 		}
 
 	}
 
 	private void listFeeds() {
 		if (allFeeds != null) {
 			Log.d("FEEDS", "Here " + allFeeds.size());
 			feedScroll = (LinearLayout) findViewById(R.id.feed_list);
 
 			// CurrFeed Layout Params
 			TableLayout.LayoutParams currFeedParam = new TableLayout.LayoutParams(
 					TableLayout.LayoutParams.MATCH_PARENT,
 					TableLayout.LayoutParams.WRAP_CONTENT);
 
 			// Feed Name Params
 			TableRow.LayoutParams feedNameParam = new TableRow.LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
 			feedNameParam.setMargins(10, 0, 10, 0);
 
 			// Button Params
 			TableRow.LayoutParams buttonParam = new TableRow.LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 			buttonParam.setMargins(10, 10, 10, 10);
 
 			// Divider Params
 			RelativeLayout.LayoutParams dividerParam = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.MATCH_PARENT, 1);
 
 			if (((LinearLayout) feedScroll).getChildCount() > 0)
 				((LinearLayout) feedScroll).removeAllViews();
 			for (int i = 0; i < 2 * allFeeds.size(); i += 2) {
 				Feed feed = allFeeds.get(i / 2);
 				if (feed.isAdded()) {
 					myVib.vibrate(50);
 					TableLayout currFeed = new TableLayout(this);
 					TableRow tableRow1 = new TableRow(this);
 					TextView feedName = new TextView(this);
 					feedName.setId(i);
 					feedName.setText((CharSequence) (feed.getName()));
 					feedName.setTextSize(fontSize);
 					feedName.setSingleLine(true);
 					feedName.setEllipsize(TextUtils.TruncateAt.END);
 					feedName.setPadding(5, 5, 5, 5);
 					feedName.setHorizontallyScrolling(false);
 					feedName.setGravity(Gravity.LEFT);
 					feedName.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							int id = v.getId();
 							viewFeed(allFeeds.get(id / 2));
 						}
 					});
 					ImageButton unsubscribe = new ImageButton(this);
 					unsubscribe.setId(i + 1);
 					Drawable icon = getResources().getDrawable(
 							R.drawable.ic_menu_delete);
 					unsubscribe.setBackground(icon);
 					unsubscribe.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							int id = v.getId();
 							myVib.vibrate(50);
 							String unsubURL = "unsubscribe/?"
 									+ allFeeds.get((id - 1) / 2).getKey();
 							UnsubscribeCommand unsub = new UnsubscribeCommand(
 									unsubURL);
 							RemoveFeed remove = new RemoveFeed();
 							remove.execute(unsub);
 							allFeeds.remove((id - 1) / 2);
 							MainActivity.shouldRefresh = true;
 							listFeeds();
 
 						}
 					});
 					View divider = new View(this);
 					divider.setBackgroundColor(Color.parseColor("#9E9E9E"));
 
 					feedScroll.addView(currFeed, currFeedParam);
 					currFeed.addView(tableRow1, currFeedParam);
 					tableRow1.addView(feedName, feedNameParam);
 					tableRow1.addView(unsubscribe, buttonParam);
 					currFeed.addView(divider, dividerParam);
 
 				} else {
 					TableLayout currFeed = new TableLayout(this);
 					TableRow tableRow1 = new TableRow(this);
 					TextView feedName = new TextView(this);
 					feedName.setId(i);
 					feedName.setText((CharSequence) (feed.getName()));
 					feedName.setTextSize(fontSize);
 					feedName.setSingleLine(true);
 					feedName.setEllipsize(TextUtils.TruncateAt.END);
 					feedName.setPadding(5, 5, 5, 5);
 					feedName.setHorizontallyScrolling(false);
 					feedName.setGravity(Gravity.LEFT);
 					feedName.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							myVib.vibrate(50);
 							int id = v.getId();
 							viewFeed(allFeeds.get(id / 2));
 						}
 					});
 					ImageButton addFeed = new ImageButton(this);
 					addFeed.setId(i + 1);
 					Drawable icon = getResources().getDrawable(
 							R.drawable.btn_check_on);
 					addFeed.setBackground(icon);
 					addFeed.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							myVib.vibrate(50);
 							int id = v.getId();
 							String addFeedURL = allFeeds.get((id - 1) / 2)
 									.getURL();
 							addFeed(addFeedURL);
 							MainActivity.shouldRefresh = true;
 							listFeeds();
 
 						}
 					});
 					View divider = new View(this);
 					divider.setBackgroundColor(Color.parseColor("#9E9E9E"));
 
 					feedScroll.addView(currFeed, currFeedParam);
 					currFeed.addView(tableRow1, currFeedParam);
 					tableRow1.addView(feedName, feedNameParam);
 					tableRow1.addView(addFeed, buttonParam);
 					currFeed.addView(divider, dividerParam);
 				}
 
 			}
 		}
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i = null;
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			i = new Intent(this, SettingsActivity.class);
 			startActivity(i);
 			break;
 		case R.id.action_search:
 			ActionBar actionBar = getActionBar();
 			// actionBar.hide();
 			actionBar.setCustomView(R.layout.searchbar);
 			EditText search = (EditText) actionBar.getCustomView()
 					.findViewById(R.id.action_searchfield);
 			search.setOnEditorActionListener(new OnEditorActionListener() {
 
 				@Override
 				public boolean onEditorAction(TextView v, int actionId,
 						KeyEvent event) {
 					allFeeds = Feed.search(v.getText().toString(), allFeeds);
 					Log.d("FEED SEARCH", v.getText().toString());
 
 					ActionBar actionBar = getActionBar();
 					actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
 							| ActionBar.DISPLAY_SHOW_HOME);
 					return false;
 				}
 			});
 			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
 					| ActionBar.DISPLAY_SHOW_HOME);
 			// actionBar.show();
 
 			break;
 		case R.id.action_refresh:
 			LinearLayout linearLayout = (LinearLayout) findViewById(R.id.feed_list);
 			if (((LinearLayout) linearLayout).getChildCount() > 0)
 				((LinearLayout) linearLayout).removeAllViews();
 			GetFeedsCommand refresh = new GetFeedsCommand();
 			GetFeeds get = new GetFeeds(this);
 			get.execute(refresh);
 			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 			Date now = new Date();
 			TextView refreshBar = (TextView) findViewById(R.id.manage_refresh_bar);
 			refreshBar.setText("Last Refreshed: " + df.format(now).toString());
 			break;
 		}
 		return true;
 	}
 
 	public void viewFeed(Feed feed) {
 		Log.d("FEEDS", "Yes, the feed will be launched");
 		// TODO: implement this. Might need to add a new activity.
 	}
 
 	protected class SearchFeeds extends
 			AsyncTask<FeedSearchCommand, Integer, List<Feed>> {
 		Context c;
 		ProgressDialog dialog;
 		String searchText;
 
 		public SearchFeeds(Context context, String searchText) {
 			c = context;
 			dialog = new ProgressDialog(c);
 			this.searchText = searchText;
 		}
 
 		@Override
 		public void onPreExecute() {
 			dialog.setMessage("Loading Feeds");
 			dialog.show();
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected List<Feed> doInBackground(FeedSearchCommand... params) {
 			FeedSearchCommand get = params[0];
 			List<Feed> allFeeds = null;
 			try {
 				allFeeds = (List<Feed>) get.parseData(searchText);
 				Log.d("FEEDS", "In try " + allFeeds.size());
 			} catch (IOException e) {
 				Log.d("FEEDS", "IOException caught");
 				return null;
 			}
 			return allFeeds;
 		}
 
 		@Override
 		protected void onPostExecute(List<Feed> result) {
 
 			if (dialog != null && dialog.isShowing()) {
 				dialog.dismiss();
 			}
 			Log.d("FEEDS", "On Post Execute " + result.size());
 			ManageActivity.allFeeds = result;
 			listFeeds();
 
 		}
 	}
 
 	protected class GetFeeds extends
 			AsyncTask<GetFeedsCommand, Integer, List<Feed>> {
 		Context c;
 		ProgressDialog dialog;
 
 		public GetFeeds(Context context) {
 			c = context;
 			dialog = new ProgressDialog(c);
 		}
 
 		@Override
 		public void onPreExecute() {
 			dialog.setMessage("Loading Feeds");
 			dialog.show();
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected List<Feed> doInBackground(GetFeedsCommand... params) {
 			GetFeedsCommand get = params[0];
 			List<Feed> allFeeds = null;
 			try {
 				String c = prefs.getString("cookie", "default");
 				allFeeds = (List<Feed>) get.parseData(c);
 				Log.d("FEEDS", "In try " + allFeeds.size());
 			} catch (IOException e) {
 				Log.d("FEEDS", "IOException caught");
 				return null;
 			}
 			return allFeeds;
 		}
 
 		@Override
 		protected void onPostExecute(List<Feed> result) {
 			if (dialog.isShowing()) {
 				dialog.dismiss();
 			}
 			Log.d("FEEDS", "On Post Execute " + result.size());
 			ManageActivity.allFeeds = result;
 			listFeeds();
 
 		}
 	}
 
 	protected class AddFeed extends AsyncTask<AddFeedCommand, Integer, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(AddFeedCommand... params) {
 			AddFeedCommand get = params[0];
 			Boolean success = false;
 			try {
 				String c = prefs.getString("cookie", "default");
 				success = (Boolean) get.parseData(c);
 			} catch (IOException e) {
 				Log.e("FEEDS", "IOException caught", e);
 				return false;
 			}
 			return success;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean success) {
 			ManageActivity.isAdded = (boolean) success;
 			if (!success) {
 				Toast failure = Toast.makeText(getBaseContext(),
 						"This URL is invalid. Please try again.",
 						Toast.LENGTH_SHORT);
 				failure.show();
 			}
 			listFeeds();
 		}
 	}
 
 	public class RemoveFeed extends
 			AsyncTask<UnsubscribeCommand, Integer, Boolean> {
 		private boolean success = false;
 
 		@Override
 		public Boolean doInBackground(UnsubscribeCommand... params) {
 			UnsubscribeCommand unsub = params[0];
 			try {
 				String c = prefs.getString("cookie", "default");
 				success = (Boolean) unsub.parseData(c);
 			} catch (IOException e) {
 				Log.e("FEEDS", "IOException caught", e);
 				return false;
 			}
 			return (Boolean) success;
 		}
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		setContentView(R.layout.activity_manage);
 		listFeeds();
 	}
 }
