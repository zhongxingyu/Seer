 package com.leggo;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import com.leggo.Article.ArticleSearchResult;
 import com.leggo.parsing.GetArticlesCommand;
 import com.leggo.parsing.GetFeedsCommand;
 
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ActionBar.LayoutParams;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
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
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	public static final String TAG = "MainActivity";
 
 	public static Context context;
 
 	private SharedPreferences prefs;
 	private SharedPreferences.Editor editor;
 
 	private String currentAccountName;
 	public static List<Article> articles;
 	
 	public static boolean shouldRestart;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		context = this;
 
         prefs = PreferenceManager.getDefaultSharedPreferences(context);
         editor = prefs.edit();
         
         
         Theme.setPrefTheme(this);
         
 		loadArticles();
 		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 		Date now = new Date();
 
 		setContentView(R.layout.activity_main);
 		
 		TextView refreshBar = (TextView) findViewById(R.id.main_refresh_bar);
 		refreshBar.setText(df.format(now).toString());
 		
 		shouldRestart = false;
 
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
 					List<ArticleSearchResult> results = Article.search(v
 							.getText().toString(), articles);
					articles = Article.GetArticles(results);
					listArticles();
					/*Log.d("LOLOL", v.getText().toString());
 					LinearLayout linearLayout = (LinearLayout) findViewById(R.id.article_list);
 					if (((LinearLayout) linearLayout).getChildCount() > 0)
 						((LinearLayout) linearLayout).removeAllViews();
 					LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
 							LayoutParams.MATCH_PARENT,
 							LayoutParams.WRAP_CONTENT, 0.10f);
 					LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(
 							LayoutParams.MATCH_PARENT,
 							LayoutParams.WRAP_CONTENT, 0.90f);
 					for (int i = 0; i < 2 * results.size(); i += 2) {
 						LinearLayout articleScroll = (LinearLayout) findViewById(R.id.article_list);
 						LinearLayout currArticle = new LinearLayout(
 								getBaseContext());
 						currArticle.setOrientation(LinearLayout.HORIZONTAL);
 						currArticle.setPadding(5, 5, 5, 5);
 						Button articleName = new Button(getBaseContext());
 						articleName.setId(i);
 						articleName.setText((CharSequence) (results.get(i / 2).article
 								.getTitle()));
 						articleName.setGravity(Gravity.LEFT);
 						articleName.setBackground(getBaseContext()
 								.getResources().getDrawable(
 										R.drawable.textlines));
 						articleName
 								.setOnClickListener(new View.OnClickListener() {
 									public void onClick(View v) {
 										int id = v.getId();
 										if (id % 2 == 0) {
 											Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articles.get(id/2).getURL()));
 											startActivity(browserIntent);
 										}
 									}
 								});
 						ImageButton peek = new ImageButton(getBaseContext());					
 						peek.setId(i + 1);
 						peek.setBackground(getBaseContext().getResources()
 								.getDrawable(R.drawable.ic_read));
 						peek.setOnClickListener(new View.OnClickListener() {
 							public void onClick(View v) {
 								int id = v.getId();
 								if (id % 2 == 1) {
 									Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articles.get(id/2).getURL()));
 									startActivity(browserIntent);
 
 								}
 							}
 						});
 						peek.setLayoutParams(param2);
 						currArticle.addView(articleName);
 						currArticle.addView(peek);
 						articleScroll.addView(currArticle);
 
 					}
					*/
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
 			GetArticlesCommand refresh = new GetArticlesCommand();
 			GetArticles get = new GetArticles(this);
 			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 			get.execute(refresh);
 			Date now = new Date();
 			TextView refreshBar = (TextView) findViewById(R.id.main_refresh_bar);
 			refreshBar.setText(df.format(now).toString());
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		if (shouldRestart == true)
 		{
 			Utils.restartActivity(this);
 			shouldRestart = false;
 		}
 		
 		// Check for network every time activity is resumed
 		if (Utils.networkAvailability(this) == false) 
 		{
 			Utils.noNetworkAlert(this);
 		} 
 		else {
 			AccountManager accountManager = AccountManager
 					.get(getApplicationContext());
 			Account[] accounts = accountManager
 					.getAccountsByType("com.google");
 			
 			// If for some reason, your account was deleted, set account to None
 			if (accounts.length == 0)
 			{
 				editor.putString("account_selection", "None");
 				editor.commit();
 			}
 			
 			currentAccountName = prefs
 					.getString("account_selection", "default");
 			
 
 			Toast.makeText(context,
 					"Currently logged in to " + currentAccountName,
 					Toast.LENGTH_SHORT).show();
 			
 			// If there is no account, send user to settings
 			if (currentAccountName.equals("None")
 					|| currentAccountName.equals("default")) {
 				Utils.noAccountAlert(this);
 			} 
 			else {
 				// Find index where account is
 				int accountIndex = 0;
 				for (Account account : accounts) {
 					if (account.name.equals(currentAccountName)) {
 						// Found account, revalidate here
 					}
 					accountIndex++;
 				}
 
 				if (accountIndex < accounts.length) {
 					// Account doesn't exist anymore for some reason.
 					editor.putString("account_selection", "None");
 					editor.commit();
 					Utils.noAccountAlert(this);
 				}
 			}
 
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
 			LinearLayout articleScroll = (LinearLayout) findViewById(R.id.article_list);
			if ((articleScroll).getChildCount() > 0) //clear list of articles
				(articleScroll).removeAllViews();
 			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
 					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.10f);
 			LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(
 					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.90f);
 			for (int i = 0; i < 2 * articles.size(); i += 2) {
 				LinearLayout currArticle = new LinearLayout(this);
 				currArticle.setOrientation(LinearLayout.HORIZONTAL);
 				currArticle.setPadding(100000, 100000, 100000, 100000);
 				currArticle.setBackground(this.getResources().getDrawable(
 						R.drawable.box));
 				Button articleName = new Button(this);
 				articleName.setId(i);
 				articleName.setText((CharSequence) (articles.get(i / 2)
 						.getTitle()));
 				articleName.setBackground(getResources().getDrawable(
 						R.drawable.roundbutton));
 				articleName.setGravity(Gravity.LEFT);
 				articleName.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						int id = v.getId();
 						if (id % 2 == 0) {
 							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articles.get(id/2).getURL()));
 							startActivity(browserIntent);
 						}
 					}
 				});
 				articleName.setLayoutParams(param);
 				ImageButton read = new ImageButton(this);
 				read.setId(i + 1);
 				read.setBackground(getBaseContext().getResources().getDrawable(
 						R.drawable.ic_read));
 				read.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						int id = v.getId();
 						if (id % 2 == 1) {
 							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articles.get(id/2).getURL()));
 							startActivity(browserIntent);
 						}
 					}
 				});
 				read.setLayoutParams(param2);
 				currArticle.addView(articleName);
 				currArticle.addView(read);
 				articleScroll.addView(currArticle);
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
 
 	protected class GetArticles extends AsyncTask<GetArticlesCommand, Integer, List<Article>> {
 		private Context c;
 		private ProgressDialog dialog;
 		public GetArticles(Context context){
 			c = context;
 			dialog = new ProgressDialog(c);
 			dialog.setMessage("Loading Articles");
 			dialog.show();
 		}
 		
 		@Override
 		protected void onPreExecute(){
 			
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
 			listArticles();
 		}
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig){
 		super.onConfigurationChanged(newConfig);
 		setContentView(R.layout.activity_main);
 		listArticles();
 	}
 
 }
