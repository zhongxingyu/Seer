 package com.leggo;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.app.ActionBar.LayoutParams;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.leggo.Feed;
 import com.leggo.Feed.FeedSearchResult;
 import com.leggo.parsing.AddFeedCommand;
 import com.leggo.parsing.GetFeedsCommand;
 import com.leggo.parsing.UnsubscribeCommand;
 
 public class ManageActivity extends Activity {
 	boolean atom = false;
 	boolean rss = false;
 	private static List<Feed> allFeeds = null;
 	private File sdCard = Environment.getExternalStorageDirectory();
 	public static File filesDir;
 	private SharedPreferences prefs;
 	public static boolean isAdded;
 	private Context context;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		context = this;
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(context);
 
 		Theme.setPrefTheme(this);
 
 		filesDir = new File(sdCard + "/Android/data/com.leggo/files");
 		filesDir.mkdirs();
 		loadFeeds();
 		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 		Date now = new Date();
 
 		setContentView(R.layout.activity_manage);
 
 		TextView refreshBar = (TextView) findViewById(R.id.manage_refresh_bar);
 		refreshBar.setText(df.format(now).toString());
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.manage, menu);
 		return true;
 	}
 
 	private boolean networkAvailability() {
 		ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo active = CM.getActiveNetworkInfo();
 		return active != null && active.isConnected();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadFeeds() {
 		// File output = new File(filesDir, "feeds.htm");
 		if (networkAvailability()) // until login is taken care of
 		{
 			GetFeeds get = new GetFeeds(this);
 			GetFeedsCommand command = new GetFeedsCommand();
 			get.execute(command);
 		}
 	}
 
 	public void addFeed(View v) {
 		// TODO: change to use Tim's code.
 		EditText uri = (EditText) findViewById(R.id.add_feed_uri);
 		String text = uri.getText().toString();
 		if (text == null || text.isEmpty()) {
 			Toast warning = Toast.makeText(this, "Please enter a feed url.", Toast.LENGTH_SHORT);
 			warning.show();
 			return;
 		} else if (atom == false && rss == false) {
 			Toast warning = Toast.makeText(this, "Please select a feed type and try again.", Toast.LENGTH_SHORT);
 			warning.show();
 			return;
 		}
 		String addlink;
 		if (atom) {
 			try {
 				addlink = ("addAtom/?url=" + URLEncoder.encode(text, "UTF-8"));
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				return;
 			}
 		} else {
 			try {
 				addlink = ("addRSS/?url=" + URLEncoder.encode(text, "UTF-8"));
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				return;
 			}
 		}
 		if (networkAvailability()) {
 			AddFeedCommand addMe = new AddFeedCommand(addlink);
 			AddFeed get = new AddFeed();
 			get.execute(addMe);
 
 			loadFeeds();
 		} else {
 			Toast warning = Toast.makeText(this, "Please connect to the internet to add a feed.", Toast.LENGTH_SHORT);
 			warning.show();
 			return;
 		}
 
 	}
 
 	public void onRadioButtonClicked(View view) {
 		// Is the button now checked?
 		boolean checked = ((RadioButton) view).isChecked();
 
 		// Check which radio button was clicked
 		switch (view.getId()) {
 		case R.id.radio_atom:
 			if (checked) setAtom();
 			break;
 		case R.id.radio_rss:
 			if (checked) setRSS();
 			break;
 		}
 	}
 
 	private void listFeeds() {
 		if (allFeeds != null) {
 			Log.d("FEEDS", "Here " + allFeeds.size());
 			LinearLayout feedScroll = (LinearLayout) findViewById(R.id.feed_list);
 			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.10f);
 			LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.90f);
 			LinearLayout linearLayout = (LinearLayout) findViewById(R.id.feed_list);
 			if (((LinearLayout) linearLayout).getChildCount() > 0) ((LinearLayout) linearLayout).removeAllViews();
 			for (int i = 0; i < 2 * allFeeds.size(); i += 2) {
 				LinearLayout currFeed = new LinearLayout(this);
 				currFeed.setOrientation(LinearLayout.HORIZONTAL);
 				Button feedName = new Button(this);
 				feedName.setId(i);
 				feedName.setText((CharSequence) (allFeeds.get(i / 2).getName()));
 				feedName.setBackground(getResources().getDrawable(R.drawable.roundbutton));
 				feedName.setGravity(Gravity.LEFT);
 				feedName.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						int id = v.getId();
 						if (id == R.id.add_feed) addFeed(v);
 						else if (id % 2 == 0) viewFeed(allFeeds.get(id / 2));
 					}
 				});
 				feedName.setLayoutParams(param);
 				ImageButton unsubscribe = new ImageButton(this);
 				unsubscribe.setId(i + 1);
 				Drawable icon = getResources().getDrawable(R.drawable.ic_menu_delete);
 				unsubscribe.setBackground(icon);
 				unsubscribe.setLayoutParams(param2);
 				unsubscribe.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						int id = v.getId();
 						if (id == R.id.add_feed) addFeed(v);
 						else if (id % 2 == 1){
 							String unsubURL = "unsubscribe/?" + allFeeds.get((id-1)/2).getKey();
 							UnsubscribeCommand unsub = new UnsubscribeCommand(unsubURL);
 							RemoveFeed remove = new RemoveFeed();
 							remove.execute(unsub);
 						}
 					}
 				});
 				currFeed.addView(feedName);
 				currFeed.addView(unsubscribe);
 				feedScroll.addView(currFeed);
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
 			EditText search = (EditText) actionBar.getCustomView().findViewById(R.id.action_searchfield);
 			search.setOnEditorActionListener(new OnEditorActionListener() {
 
 				@Override
 				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 					List<FeedSearchResult> results = Feed.search(v.getText().toString(), allFeeds);
 					Log.d("LOLOL", v.getText().toString());
 					LinearLayout linearLayout = (LinearLayout) findViewById(R.id.feed_list);
 					if (((LinearLayout) linearLayout).getChildCount() > 0) ((LinearLayout) linearLayout).removeAllViews();
 					LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.10f);
 					LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.90f);
 					for (int i = 0; i < 2 * results.size(); i += 2) {
 						LinearLayout feedScroll = linearLayout;
 						LinearLayout currfeed = new LinearLayout(getBaseContext());
 						currfeed.setOrientation(LinearLayout.HORIZONTAL);
 						Button feedName = new Button(getBaseContext());
 						feedName.setId(i);
 						feedName.setText((CharSequence) (results.get(i / 2).feed.getName()));
 						feedName.setTextColor(Color.parseColor("#000000"));
 						feedName.setBackground(getResources().getDrawable(R.drawable.roundbutton));
 						feedName.setGravity(Gravity.LEFT);
 						feedName.setOnClickListener(new View.OnClickListener() {
 							public void onClick(View v) {
 								int id = v.getId();
 								if (id % 2 == 0)
 								;
 								// replace with call for view;
 							}
 						});
 						feedName.setLayoutParams(param);
 						ImageButton unsubscribe = new ImageButton(getBaseContext());
 						unsubscribe.setId(i + 1);
 						Drawable icon = getResources().getDrawable(R.drawable.ic_menu_delete);
 						unsubscribe.setBackground(icon);
 						unsubscribe.setLayoutParams(param2);
 						unsubscribe.setOnClickListener(new View.OnClickListener() {
 							public void onClick(View v) {
 								int id = v.getId();
 								if (id == R.id.add_feed) addFeed(v);
 								else if (id % 2 == 1)
								;
								// replace with object call
								// allFeeds.get((id-1)/2).unsubscribe();
 							}
 						});
 						currfeed.addView(feedName);
 						currfeed.addView(unsubscribe);
 						feedScroll.addView(currfeed);
 
 					}
 					ActionBar actionBar = getActionBar();
 					actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_HOME);
 					return false;
 				}
 			});
 			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
 			// actionBar.show();
 
 			break;
 		case R.id.action_refresh:
 			LinearLayout linearLayout = (LinearLayout) findViewById(R.id.feed_list);
 			if (((LinearLayout) linearLayout).getChildCount() > 0) ((LinearLayout) linearLayout).removeAllViews();
 			GetFeedsCommand refresh = new GetFeedsCommand();
 			GetFeeds get = new GetFeeds(this);
 			get.execute(refresh);
 			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 			Date now = new Date();
 			TextView refreshBar = (TextView) findViewById(R.id.manage_refresh_bar);
 			refreshBar.setText(df.format(now).toString());
 			break;
 		}
 		return true;
 	}
 
 	public void setAtom() {
 		rss = false;
 		atom = true;
 	}
 
 	public void setRSS() {
 		atom = false;
 		rss = true;
 	}
 
 	public void viewFeed(Feed feed) {
 		Log.d("FEEDS", "Yes, the feed will be launched");
 		// TODO: implement this. Might need to add a new activity.
 	}
 
 	protected class GetFeeds extends AsyncTask<GetFeedsCommand, Integer, List<Feed>> {
 		Context c;
 		ProgressDialog dialog;
 		
 		public GetFeeds(Context context){
 			c = context;
 			dialog = new ProgressDialog(c);
 		}
 		
 		
 		@Override
 		public void onPreExecute(){
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
 			if(dialog.isShowing()) {
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
 				Toast failure = Toast.makeText(getBaseContext(), "This URL is invalid. Please try again.", Toast.LENGTH_SHORT);
 				failure.show();
 			}
 		}
 	}
 	
 	public class RemoveFeed extends AsyncTask<UnsubscribeCommand, Integer, Boolean>{
 		private boolean success = false;
 		
 		@Override
 		public Boolean doInBackground(UnsubscribeCommand... params){
 			UnsubscribeCommand unsub = params[0];
 			try{
 				String c = prefs.getString("cookie", "default");
 				success = (Boolean) unsub.parseData(c);
 			} catch(IOException e) {
 				Log.e("FEEDS", "IOException caught", e);
 				return false;
 			}
 			return (Boolean) success;
 		}
		
		@Override
		public void onPostExecute(Boolean success){
			listFeeds();
		}
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig){
 		super.onConfigurationChanged(newConfig);
 		setContentView(R.layout.activity_manage);
 		listFeeds();
 	}
 }
