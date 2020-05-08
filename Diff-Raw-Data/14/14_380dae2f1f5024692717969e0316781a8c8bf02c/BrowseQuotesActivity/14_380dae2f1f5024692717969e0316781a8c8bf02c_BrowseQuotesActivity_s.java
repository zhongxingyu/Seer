 package fr.quoteBrowser.activity;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.widget.ListView;
 import fr.quoteBrowser.Quote;
 import fr.quoteBrowser.R;
 import fr.quoteBrowser.service.QuotePager;
 
 public class BrowseQuotesActivity extends Activity implements
 		OnSharedPreferenceChangeListener {
 
 	private static final String QUOTES = "quotes";
 	private static String TAG = "quoteBrowser";
 
 	private enum LoadListAction {
 		RELOAD_PAGE, NEXT_PAGE, PREVIOUS_PAGE
 	};
 
 	private ArrayList<Quote> quotes = null;
 
 	/**
 	 * Called when the activity is first created.
 	 * 
 	 * @param savedInstanceState
 	 *            If the activity is being re-initialized after previously being
 	 *            shut down then this Bundle contains the data it most recently
 	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
 	 *            is null.</b>
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.i(TAG, "onCreate");
 		setContentView(R.layout.quote_list_layout);
 		PreferenceManager.getDefaultSharedPreferences(this)
 				.registerOnSharedPreferenceChangeListener(this);
 
 		if (savedInstanceState != null) {
 			quotes = savedInstanceState.getParcelableArrayList(QUOTES);
 		}
 		if (quotes == null) {
 			quotes = new ArrayList<Quote>();
 		}
 
 		initAdBannerView();
 
 		ListView quoteListView = (ListView) findViewById(R.id.quoteListView);
 		quoteListView.setAdapter(new QuoteAdapter(this,
 				R.layout.quote_list_item_layout, quotes));
 		if (quotes.isEmpty()) {
 			loadQuoteList(LoadListAction.NEXT_PAGE);
 		}
 
 	}
 
 	protected void initAdBannerView() {
 		WebView bannerAdView = (WebView) findViewById(R.id.bannerAdView);
 		bannerAdView.getSettings().setJavaScriptEnabled(true);
 		bannerAdView
 				.loadData(
 						"<html><body style=\"margin:0;padding:0;\"><script type=\"text/javascript\" src=\"http://ad.leadboltads.net/show_app_ad.js?section_id=593079000\"></script></body></html>",
 						"text/html", null);
 	}
 
 	protected void loadQuoteList(final LoadListAction action) {
 
 		final ProgressDialog progressDialog = ProgressDialog.show(this,
 				"Loading", "please wait", true);
 		new AsyncTask<Void, Void, List<Quote>>() {
 
 			@Override
 			protected List<Quote> doInBackground(Void... params) {
 				try {
 
 					quotes.clear();
 					switch (action) {
 					case RELOAD_PAGE:
 						quotes.addAll(QuotePager.getInstance(
 								getApplicationContext()).reloadQuotePage());
 						break;
 					case NEXT_PAGE:
 						quotes.addAll(QuotePager.getInstance(
 								getApplicationContext()).getNextQuotePage());
 						break;
 					case PREVIOUS_PAGE:
 						quotes.addAll(QuotePager.getInstance(
 								getApplicationContext()).getPreviousQuotePage());
 						break;
 					default:
 						break;
 					}
 
 				} catch (IOException e) {
 					Log.e(TAG, e.getMessage(),e);
 				}
 				return quotes;
 			}
 
 			protected void onPreExecute() {
 				super.onPreExecute();
 			}
 
 			protected void onPostExecute(List<Quote> quotes) {
 				ListView quoteListView = (ListView) findViewById(R.id.quoteListView);
 				((QuoteAdapter) quoteListView.getAdapter())
 						.notifyDataSetChanged();
 				progressDialog.dismiss();
 			}
 
 		}.execute();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.quote_list_option_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.nextQuotePageMenuOption:
 			loadQuoteList(LoadListAction.NEXT_PAGE);
 			return true;
 		case R.id.previousQuotePageMenuOption:
 			loadQuoteList(LoadListAction.PREVIOUS_PAGE);
 			return true;
 		case R.id.refreshMenuOption:
 			loadQuoteList(LoadListAction.RELOAD_PAGE);
 			return true;
 		case R.id.preferencesMenuOption:
 			Intent intent = new Intent(getApplicationContext(),
 					QuotePreferencesActivity.class);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onPause();
 		PreferenceManager.getDefaultSharedPreferences(this)
 				.unregisterOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		loadQuoteList(LoadListAction.RELOAD_PAGE);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putParcelableArrayList(QUOTES, quotes);
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (QuotePager.getInstance(getApplicationContext()).getCurrentPage() > 0) {
 			menu.findItem(R.id.previousQuotePageMenuOption).setEnabled(true);
 		} else {
 			menu.findItem(R.id.previousQuotePageMenuOption).setEnabled(false);
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 }
