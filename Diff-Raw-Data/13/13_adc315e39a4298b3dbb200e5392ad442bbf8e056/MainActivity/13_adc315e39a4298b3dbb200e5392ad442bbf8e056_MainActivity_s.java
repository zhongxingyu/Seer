 package com.leggo;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.text.TextUtils;
 
 import com.leggo.parsing.GetArticlesCommand;
 import com.leggo.parsing.GetFeedsCommand;
 import com.leggo.parsing.MarkReadCommand;
 import com.leggo.parsing.MarkUnreadCommand;
 import com.sothree.slidinguppanel.SlidingUpPanelLayout;
 
 @SuppressLint("SimpleDateFormat")
 public class MainActivity extends Activity {
 
 	public static final String TAG = "MainActivity";
 
 	public static Context context;
 
 	float density;
 	int panelHeight;
 
 	private SharedPreferences prefs;
 
 	private int fontSize;
 
 	private String currentAccountName;
 	public static List<Article> articles;
 
 	public static boolean shouldRestart;
 	public static boolean shouldRefresh;
 
 	public static LinearLayout articleScroll;
 
 	protected static Vibrator myVib;
 	protected static SlidingUpPanelLayout panel;
 
 	protected static List<Article> markReadList;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		context = this;
 		myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(context);
 
 		fontSize = 16;
 
 		Theme.setPrefTheme(this);
 
 		setContentView(R.layout.activity_main);
 		density = context.getResources().getDisplayMetrics().density;
 
 		panelHeight = 0;
 		panel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
 		panel.setPanelHeight(panelHeight);
 		panel.setSlidingEnabled(false);
 
 		shouldRestart = false;
 		shouldRefresh = false;
 
 		
 		
 		articleScroll = (LinearLayout) findViewById(R.id.article_list);
 
 		markReadList = new ArrayList<Article>();
 
 		if (Utils.networkAvailability(this) == true) {
 			// If there is no account, send user to settings
 
 			currentAccountName = prefs
 					.getString("account_selection", "default");
 
 			if (currentAccountName.equals("None")
 					|| currentAccountName.equals("default")) {
 				Utils.noAccountAlert(this);
 			} else {
 				loadArticles();
 				SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 				Date now = new Date();
 				TextView refreshBar = (TextView) findViewById(R.id.main_refresh_bar);
 				refreshBar.setText("Last Refreshed: "
 						+ df.format(now).toString());
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		ActionBar action = getActionBar();
 		action.show();
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i = null;
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			i = new Intent(this, SettingsActivity.class);
 			startActivity(i);
 			break;
 		case R.id.action_manage:
 			i = new Intent(this, ManageActivity.class);
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
 					articles = Article.search(v.getText().toString(), articles);
 					listArticles();
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
 			if(Utils.networkAvailability(this)){
 				GetArticlesCommand refresh = new GetArticlesCommand();
 				GetArticles get = new GetArticles(this);
 				SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 				get.execute(refresh);
 				Date now = new Date();
 				TextView refreshBar = (TextView) findViewById(R.id.main_refresh_bar);
 				refreshBar.setText("Last Refreshed: " + df.format(now).toString());
 				}
 			break;
 		}
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
 
 		currentAccountName = prefs.getString("account_selection", "default");
 		if (currentAccountName.equals("None")
 				|| currentAccountName.equals("default")) {
 			Utils.noAccountAlert(this);
 		}
 
 		else if (shouldRefresh == true) {
 			GetArticlesCommand refresh = new GetArticlesCommand();
 			GetArticles get = new GetArticles(this);
 			get.execute(refresh);
 			shouldRefresh = false;
 		}
 
 		// Check for network every time activity is resumed
 		if (Utils.networkAvailability(this) == false) {
 			Utils.noNetworkAlert(this);
 		}
 	}
 
 	private void loadArticles() {
 		if (Utils.networkAvailability(this)) // until login is taken care of
 		{
 			GetArticles get = new GetArticles(this);
 			GetArticlesCommand command = new GetArticlesCommand();
 			get.execute(command);
 		}
 	}
 
 	public void listArticles() {
 		if (articles != null) {
 			Log.d("ARTICLES", "Here " + articles.size());
 
 			if ((articleScroll).getChildCount() > 0) // clear list of articles
 				(articleScroll).removeAllViews();
 
 			// CurrArticle Layout Params
 			TableLayout.LayoutParams currArticleParam = new TableLayout.LayoutParams(
 					TableLayout.LayoutParams.MATCH_PARENT,
 					TableLayout.LayoutParams.WRAP_CONTENT);
 
 			// Article Name Params
 			TableRow.LayoutParams articleNameParam = new TableRow.LayoutParams(
 					TableRow.LayoutParams.WRAP_CONTENT,
 					TableRow.LayoutParams.WRAP_CONTENT, 1f);
 			articleNameParam.setMargins(10, 0, 10, 0);
 
 			// Feed Name Params
 			TableLayout.LayoutParams feedNameParam = new TableLayout.LayoutParams(
 					RelativeLayout.LayoutParams.MATCH_PARENT,
 					RelativeLayout.LayoutParams.WRAP_CONTENT);
 			feedNameParam.setMargins(10, 1, 10, 5);
 
 			// Read Button Params
 			TableRow.LayoutParams readButtonParam = new TableRow.LayoutParams(
 					TableRow.LayoutParams.WRAP_CONTENT,
 					TableRow.LayoutParams.WRAP_CONTENT);
 			readButtonParam.setMargins(10, 10, 10, 10);
 
 			// Divider Params
 			RelativeLayout.LayoutParams dividerParam = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.MATCH_PARENT, 1);
 
 			for (int i = 0; i < 2 * articles.size(); i += 2) {
 				TableLayout currArticle = new TableLayout(this);
 				TableRow tableRow1 = new TableRow(this);
 				// currArticle.setBackground(this.getResources().getDrawable(R.drawable.box));
 				TextView articleName = new TextView(this);
 				articleName.setId(i);
 				articleName.setText((CharSequence) (articles.get(i / 2)
 						.getTitle()));
 				articleName.setBackgroundResource(0);
 				articleName.setTextSize(fontSize);
 				articleName.setMaxLines(2);
 				articleName.setEllipsize(TextUtils.TruncateAt.END);
 				articleName.setGravity(Gravity.LEFT);
 				articleName.setPadding(5, 5, 5, 5);
 				articleName.setHorizontallyScrolling(false);
 				articleName.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						int id = v.getId();
 						if (id % 2 == 0) {
 							vibrate();
 							Article curr = articles.get(id / 2);
 							Intent browserIntent = new Intent(
 									Intent.ACTION_VIEW,
 									Uri.parse(curr.getURL()));
 							startActivity(browserIntent);
 							boolean autoMarkAsRead = prefs.getBoolean(
 									"autoMarkAsRead", false);
 							if (autoMarkAsRead && !curr.isRead()) {
 								MarkReadCommand mark = new MarkReadCommand(curr
 										.getKey());
 								MarkRead marktask = new MarkRead();
 								marktask.execute(mark);
 								curr.setRead(true);
 								articles.set(id / 2, curr);
 								ImageButton associated = (ImageButton) findViewById(id + 1);
 								associated.setBackground(getBaseContext()
 										.getResources().getDrawable(
 												R.drawable.btn_check_on));
 							}
 						}
 					}
 				});
 
 				TextView feedName = new TextView(this);
 				feedName.setId(i + 1000);
 				feedName.setText((CharSequence) (articles.get(i / 2).getFeed()
 						.getName()));
 				feedName.setTextSize(10);
 				feedName.setGravity(Gravity.RIGHT);
 				feedName.setTextColor(Color.parseColor("#616161"));
 
 				ImageButton readButton = new ImageButton(this);
 				readButton.setId(i + 1);
 				readButton.setBackground(getBaseContext().getResources()
 						.getDrawable(R.drawable.btn_check_off));
 				readButton.setScaleType(ScaleType.FIT_CENTER);
 
 				readButton.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						int id = v.getId();
 						if (id % 2 == 1) {
 							vibrate();
 							Article curr = articles.get(id / 2);
 							if (panelHeight == 0) {
 								/*
 								 * LinearLayout panelLayout = (LinearLayout)
 								 * findViewById(R.id.panel_layout);
 								 * 
 								 * RelativeLayout.LayoutParams dividerParam =
 								 * new RelativeLayout.LayoutParams(1,
 								 * RelativeLayout.LayoutParams.MATCH_PARENT);
 								 * View divider = new View(getBaseContext());
 								 * divider
 								 * .setBackgroundColor(Color.parseColor("#9E9E9E"
 								 * ));
 								 */
 
 								Button cancel = (Button) findViewById(R.id.cancel_button);
 								cancel.setOnClickListener(new View.OnClickListener() {
 									public void onClick(View v) {
 										panelHeight = 0;
 										panel.setPanelHeight(panelHeight);
 										if (markReadList != null) {
 											resetIcons();
 											markReadList.clear();
 										}
 									}
 								});
 
 								Button ok = (Button) findViewById(R.id.ok_button);
 								ok.setOnClickListener(new View.OnClickListener() {
 									public void onClick(View v) {
 										panelHeight = 0;
 										panel.setPanelHeight(panelHeight);
 										if (markReadList != null)
 											markAllAsRead();
 									}
 								});
 								// panelLayout.addView(divider, dividerParam);
 
 								panelHeight = (int) (68 * density + 0.5f);
 								panel.setPanelHeight(panelHeight);
 								panel.setShadowDrawable(getBaseContext()
 										.getResources().getDrawable(
 												R.drawable.above_shadow));
 							}
 							if (!curr.isRead()) {
 								curr.setRead(true);
 								markReadList.add(curr);
 								articles.set(id / 2, curr);
 								v.setBackground(getBaseContext().getResources()
 										.getDrawable(R.drawable.btn_check_on));
 							} else {
 								markReadList.remove(curr);
 								curr.setRead(false);
 								articles.set(id / 2, curr);
 								v.setBackground(getBaseContext().getResources()
 										.getDrawable(R.drawable.btn_check_off));
 								if (markReadList.isEmpty()) {
 									panelHeight = 0;
 									panel.setPanelHeight(panelHeight);
 								}
 							}
 							TextView panelText = (TextView) findViewById(R.id.popup_text);
 							panelText.setGravity(Gravity.CENTER_HORIZONTAL);
 							panelText.setPadding((int) (125 * density * 0.5f),
 									(int) (34 * density * 0.5f),
 									(int) (125 * density * 0.5f), 0);
 							panelText.setText(markReadList.size()
 									+ " Articles Selected");
 
 						}
 					}
 				});
 
 				View divider = new View(this);
 				divider.setBackgroundColor(Color.parseColor("#9E9E9E"));
 
 				articleScroll.addView(currArticle, currArticleParam);
 				currArticle.addView(tableRow1, currArticleParam);
 				tableRow1.addView(readButton, readButtonParam);
 				tableRow1.addView(articleName, articleNameParam);
 				currArticle.addView(feedName, feedNameParam);
 				currArticle.addView(divider, dividerParam);
 
 			}
 		}
 	}
 
 	public Object testGetArticles() {
 		Object result = null;
 		try {
 			File dir = this.getFilesDir();
 			String fileName = "simplecta_all.htm";
 			String testFile = new File(dir + File.separator + fileName)
 					.getAbsolutePath();
 			GetArticlesCommand cmd = new GetArticlesCommand();
 			result = cmd.testFromFile(testFile);
 		} catch (Throwable ex) {
 			Log.e(TAG, ex.getMessage(), ex);
 		}
 		return result;
 	}
 
 	public Object testGetFeeds() {
 		Object result = null;
 		try {
 			File dir = this.getFilesDir();
 			String fileName = "simplecta_feeds.htm";
 			String testFile = new File(dir + File.separator + fileName)
 					.getAbsolutePath();
 			GetFeedsCommand cmd = new GetFeedsCommand();
 			result = cmd.testFromFile(testFile);
 		} catch (Throwable ex) {
 			Log.e(TAG, ex.getMessage(), ex);
 		}
 		return result;
 	}
 
 	public Object testGetRemoteArticles() {
 		Object result = null;
 		try {
 			GetArticlesCommand cmd = new GetArticlesCommand();
 			String c = prefs.getString("cookie", "default");
 			result = cmd.parseData(c);
 			Log.d(TAG, "RESULT:" + result);
 		} catch (Throwable ex) {
 			Log.e(TAG, ex.getMessage(), ex);
 		}
 		return result;
 	}
 
 	protected class GetArticles extends
 			AsyncTask<GetArticlesCommand, Integer, List<Article>> {
 		private Context c;
 		private ProgressDialog dialog;
 
 		public GetArticles(Context context) {
 			c = context;
 			dialog = new ProgressDialog(c);
 			dialog.setMessage("Loading Articles");
 			dialog.show();
 		}
 
 		@Override
 		protected void onPreExecute() {
 
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected List<Article> doInBackground(GetArticlesCommand... params) {
 			GetArticlesCommand get = params[0];
 			List<Article> articles = null;
 			try {
 				String c = prefs.getString("cookie", "default");
 				articles = (List<Article>) get.parseData(c);
 				Log.d("ARTICLES", "In try " + articles.size());
 			} catch (IOException e) {
 				Log.d("ARTICLES", "IOException caught");
 				return null;
 			}
 			return articles;
 		}
 
 		@Override
 		protected void onPostExecute(List<Article> result) {
 			if (dialog.isShowing()) {
 				dialog.dismiss();
 			}
 
 			Log.d("ARTICLES", "On Post Execute " + result.size());
 			MainActivity.articles = result;
 
 			if (articles == null) {
 				Utils.timeOutAlert((Activity) context);
 			} else {
 
 				listArticles();
 			}
 		}
 	}
 
 	protected class MarkRead extends
 			AsyncTask<MarkReadCommand, Integer, Boolean> {
 		@Override
 		protected Boolean doInBackground(MarkReadCommand... params) {
 			MarkReadCommand mark = params[0];
 			try {
 				String c = prefs.getString("cookie", "default");
 				mark.parseData(c);
 				Log.d("ARTICLES", "In try " + articles.size());
 			} catch (IOException e) {
 				Log.d("ARTICLES", "IOException caught");
 				return false;
 			}
 			return true;
 
 		}
 	}
 
 	protected class MarkUnread extends
 			AsyncTask<MarkUnreadCommand, Integer, Boolean> {
 		@Override
 		protected Boolean doInBackground(MarkUnreadCommand... params) {
 			MarkUnreadCommand mark = params[0];
 			try {
 				String c = prefs.getString("cookie", "default");
 				mark.parseData(c);
 				Log.d("ARTICLES", "In try " + articles.size());
 			} catch (IOException e) {
 				Log.d("ARTICLES", "IOException caught");
 				return false;
 			}
 			return true;
 
 		}
 	}
 
 	//@Override
	/*public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_main);
		Log.d("CONFIGURATiONCHANGE", articles.size() + " articles");
		listArticles();
		panelHeight = 0;
		panel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		panel.setPanelHeight(panelHeight);
		panel.setSlidingEnabled(false);
	} */
 
 	public void markAllAsRead() {
 		for (Article article : markReadList) {
 			int index = articles.indexOf(article);
 			String key = article.getKey();
 			MarkReadCommand mark = new MarkReadCommand(key);
 			MarkRead marktask = new MarkRead();
 			marktask.execute(mark);
 			articles.remove(index);
 
 		}
 		markReadList.clear();
 		listArticles();
 	}
 
 	private void resetIcons() {
 		for (Article article : markReadList) {
 			int index = articles.indexOf(article);
 			int id = (index * 2) + 1;
 
 			article.setRead(false);
 			articles.set(index, article);
 			ImageButton curr = (ImageButton) findViewById(id);
 			curr.setBackground(getResources().getDrawable(
 					R.drawable.btn_check_off));
 		}
 
 	}
 
 	private void vibrate() {
 		if (myVib != null && prefs.getBoolean("vibrateMode", false))
 			myVib.vibrate(50);
 	}
 
 }
