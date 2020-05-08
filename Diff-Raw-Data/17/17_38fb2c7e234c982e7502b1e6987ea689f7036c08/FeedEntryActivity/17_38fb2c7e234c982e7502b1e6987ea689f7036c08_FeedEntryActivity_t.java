 package com.yairkukielka.feedhungry;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings.LayoutAlgorithm;
 import android.webkit.WebSettings.PluginState;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.android.volley.Request.Method;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.Response.Listener;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.JsonArrayRequest;
 import com.android.volley.toolbox.NetworkImageView;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.Extra;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.yairkukielka.feedhungry.app.MyVolley;
 import com.yairkukielka.feedhungry.feedly.Entry;
 import com.yairkukielka.feedhungry.network.JsonCustomRequest;
 import com.yairkukielka.feedhungry.toolbox.DateUtils;
 import com.yairkukielka.feedhungry.toolbox.NetworkUtils;
 
 @EActivity(R.layout.feed_entry_layout)
 public class FeedEntryActivity extends SherlockFragmentActivity {
 	private static final String TAG = FeedEntryActivity.class.getSimpleName();
 	private static final String ENTRY_PATH = "/v3/entries/";
 	private static final String MARKERS_PATH = "/v3/markers";
 	private static final String MARK_READ = "markAsRead";
 	private static final String MARK_KEEP_UNREAD = "keepUnread";
 	private static final String ENTRIES_IDS = "entryIds";
 	private static final String ENTRY_ID = "entryId";
 	private static final String ENTRY_TITLE = "entryTitle";
 	private static final String ENTRY_AUTHOR = "entryAuthor";
 	private static final String ENTRY_DATE = "entryDate";
 	public static final String ACCESS_TOKEN = "accessToken";
 	private static final String STREAM_TITLE = "streamTitle";
 	private static final String BY = " by ";
 	private static final String encoding = "utf-8";
 	private static final int WEBVIEW_TEXT_SIZE = 18;
 	private static final String TEXT_HTML = "text/html";
 	private static final String HTML_OPEN_TAG = "<html>";
 	private static final String BODY_CLOSE_HTML_CLOSE_TAGS = "</body></html>";
 	private static final String HTML_BODY_TAG = "<body>";
 	private static final String HTML_HEAD = "<head>"
 			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
 			+ "<style>@font-face {font-family: 'myFont';src: url('file:///android_asset/fonts/Roboto-Light.ttf');}"
 			+ "body {font-family: 'myFont';line-height:150%;}a:link {color:#70B002;}img{max-width: 100%; width:auto; height: auto;}"
 			+ "iframe{max-width: 100%; width:auto; height: auto;}</style></head>";
 	private static final String DIV_PREFIX = "<div style='background-color:transparent;padding: 10px;color:#888;font-family: myFont';>";
 	private static final String DIV_SUFIX = "</div>";
 	// the action bar menu
 	private Menu actionBarmenu;
 	// the feed entry
 	private Entry entry;
 	// animation to show the feed content after the loading fragment shows
 	private Animation webviewContentPushUpAnimation;
 	// animation to show the title fading in
 	private Animation titleFadeInAnimation;
 	// fragment that shows while loading the entry content
 	// private Fragment loadingFragment;
 	@Extra(ACCESS_TOKEN)
 	String accessToken;
 	@Extra(ENTRY_ID)
 	String entryId;
 	@Extra(ENTRY_AUTHOR)
 	String entryAuthor;
 	@Extra(ENTRY_DATE)
 	String entryDate;
 	@Extra(ENTRY_TITLE)
 	String entryTitle;
 	@Extra(STREAM_TITLE)
 	String streamTitle;
 
 	/** title text view */
 	@ViewById(R.id.entry_title)
 	TextView tvTitle;
 	/** date text view */
 	@ViewById(R.id.entry_date)
 	TextView tvDate;
 	/** author text view */
 	@ViewById(R.id.entry_author)
 	TextView tvAuthor;
 	@ViewById(R.id.entry_webview)
 	WebView webView;
 	@ViewById(R.id.transparent_view)
 	View transparentView;
 
 	@ViewById(R.id.feed_entry_title_layout)
 	RelativeLayout titleLayout;
 	@ViewById(R.id.entry_bg_image_view)
 	NetworkImageView bgImage;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@SuppressLint("SetJavaScriptEnabled")
 	@AfterViews
 	void afterViews() {
 
 		// getSupportActionBar().setBackgroundDrawable(null);
 		webView.setBackgroundColor(0x00000000);
 		View.OnClickListener onClickListener = getTitleOnClickListener();
 		titleLayout.setOnClickListener(onClickListener);
 		// action bar icon navagable up
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		// animate webview content
 		webviewContentPushUpAnimation = AnimationUtils.loadAnimation(this, R.anim.push_up_in);
 		titleFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_title);
 		// loadingFragment = new LoadingFragment_();
 		// FragmentManager fragmentManager = getSupportFragmentManager();
 		// https://code.google.com/p/android/issues/detail?id=42601
 		// fragmentManager.beginTransaction().replace(R.id.frame_webview,
 		// loadingFragment).attach(loadingFragment)
 		// .addToBackStack(null).commit();
 
 		if (streamTitle == null) {
 			streamTitle = "";
 		}
 		setTitle(streamTitle);
 		tvTitle.setText(entryTitle);
 
 		if (entryAuthor != null) {
 			tvAuthor.setText(BY + entryAuthor);
 		}
 		if (entryDate != null) {
 			tvDate.setText(entryDate);
 		}
 		titleLayout.setAnimation(titleFadeInAnimation);
 
 		loadPage();
 	}
 
 	private void loadPage() {
 		RequestQueue queue = MyVolley.getRequestQueue();
 		try {
 			JsonArrayRequest myReq = NetworkUtils
 					.getJsonArrayRequest(MainActivity.ROOT_URL + ENTRY_PATH + URLEncoder.encode(entryId, encoding),
 							createMyReqSuccessListener(), createMyReqErrorListener(ERROR_LISTNENER_ORIGIN.LOADING),
 							accessToken);
 			queue.add(myReq);
 		} catch (UnsupportedEncodingException uex) {
 			Log.e(TAG, "Error encoding entryId URL");
 		}
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 	}
 
 	private Response.Listener<JSONArray> createMyReqSuccessListener() {
 		return new Response.Listener<JSONArray>() {
 			@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
 			@Override
 			public void onResponse(JSONArray response) {
 				try {
 					if (response.length() > 0) {
 						JSONObject jentry = (JSONObject) response.get(0);
 						entry = new Entry(jentry, FeedEntryActivity.this);
 
 						tvTitle.setText(entry.getTitle());
 						if (entry.getAuthor() != null) {
 							tvAuthor.setText(BY + entry.getAuthor());
 						}
 						if (entry.getPublished() != null) {
 							try {
 								tvDate.setText(DateUtils.dateToString(entry.getPublished()));
 							} catch (IllegalArgumentException ie) {
 							}
 						}
 						if (entry.isSaved()) {
 							// paint the star
 							supportInvalidateOptionsMenu();
 						}
 						if (entry.getVisual() != null) {
 							bgImage.setImageUrl(entry.getVisual(), MyVolley.getImageLoader());
 							bgImage.setAnimation(titleFadeInAnimation);
 							// height of the transparent view is 10dp
 							transparentView.getLayoutParams().height = (int) dipToPixels(FeedEntryActivity.this, 10);
 							// this animation is only for when there is an image
 							webView.setAnimation(webviewContentPushUpAnimation);
 						}
 						webView.getSettings().setJavaScriptEnabled(true);
 						webView.getSettings().setBuiltInZoomControls(true);
 						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 							webView.getSettings().setDisplayZoomControls(false);
 						}
 						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
 							webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.TEXT_AUTOSIZING);
 						} else {
 							webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
 							// for HTML5 videos
 							webView.getSettings().setPluginState(PluginState.ON);
 							webView.getSettings().setDomStorageEnabled(true);
 						}
 						webView.getSettings().setDefaultFontSize(WEBVIEW_TEXT_SIZE);
 						webView.setWebChromeClient(new WebChromeClient());
 						webView.setWebViewClient(new WebViewClient() {
 
 							@Override
 							public boolean shouldOverrideUrlLoading(WebView view, String url) {
 								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 								startActivity(intent);
 								return true;
 							}
 
 							@Override
 							public void onPageFinished(WebView view, String url) {
 								// FragmentManager fragmentManager =
 								// getSupportFragmentManager();
 								// if
 								// (fragmentManager.findFragmentById(loadingFragment.getId())
 								// != null) {
 								// fragmentManager.beginTransaction().detach(loadingFragment)
 								// .commitAllowingStateLoss();
 								// }
 								// mark entry as read
 								markEntry(MARK_READ, getSuccessListener(null));
 							}
 						});
 						loadEntryInInnerBrowser(entry.getContent());
 					}
 				} catch (JSONException e) {
 					Log.e(TAG, "Error parsing feed entry");
 					Log.e(TAG, e.getMessage());
 					// showErrorDialog(getResources().getString(R.string.error_loading_entry));
 				}
 			}
 		};
 	}
 	public static float dipToPixels(Context context, float dipValue) {
 	    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
 	    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
 	}
 	
 
 	/**
 	 * Back button pressed. Go back to the article list.
 	 */
 	@Override
 	public void onBackPressed() {
 		finish();
 		overridePendingTransition(R.anim.open_main, R.anim.close_next);
 		super.onBackPressed();
 	}
 
 	/**
 	 * Loads the entry in the internal browser
 	 */
 	private void loadEntryInInnerBrowser(String content) {
 		webView.loadDataWithBaseURL("file:///android_asset/", getHtmlData(content), TEXT_HTML, encoding, null);
 	}
 
 	private String getHtmlData(String data) {
 		StringBuilder sBuilder = new StringBuilder(HTML_OPEN_TAG).append(HTML_HEAD).append(HTML_BODY_TAG)
 				.append(DIV_PREFIX).append(data).append(DIV_SUFIX).append(BODY_CLOSE_HTML_CLOSE_TAGS);
 		return sBuilder.toString();
 	}
 
 	private Response.ErrorListener createMyReqErrorListener(final ERROR_LISTNENER_ORIGIN origin) {
 		return new Response.ErrorListener() {
 			@Override
 			public void onErrorResponse(VolleyError error) {
 				switch (origin) {
 				case LOADING:
 					Log.e(TAG, "Error loading entry");
 					break;
 				case SAVING:
 					Log.e(TAG, "Error saving entry");
 					break;
 				case MARKING:
 					Log.e(TAG, "Error marking entry");
 					break;
 
 				default:
 					break;
 				}
 				if (error != null && error.getMessage() != null) {
 					Log.e(TAG, error.getMessage());
 				}
 			}
 		};
 	}
 
 	enum ERROR_LISTNENER_ORIGIN {
 		LOADING, SAVING, MARKING;
 	}
 
 	/**
 	 * Title onclick listener
 	 * 
 	 * @return View.OnClickListener
 	 */
 	private View.OnClickListener getTitleOnClickListener() {
 		return new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				switch (v.getId()) {
 				case R.id.feed_entry_title_layout:
 					// case R.id.entry_title:
					if (entry != null && entry.getUrl() != null) {
 						openEntryInBrowser(entry);
 					}
 					break;
 				default:
 					break;
 				}
 			}
 		};
 	}
 
 	/**
 	 * Open browser after clicking the title
 	 */
 	private void openEntryInBrowser(Entry entry) {
 		if (entry.getUrl() != null) {
 			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getUrl()));
 			startActivity(browserIntent);
 		}
 	}
 
 	@Override
 	public void setTitle(CharSequence title) {
 		getSupportActionBar().setTitle(title);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.entry, menu);
 		actionBarmenu = menu;
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			overridePendingTransition(R.anim.open_main, R.anim.close_next);
 			return true;
 		case R.id.action_share:
 			shareEntry();
 			return true;
 		case R.id.action_mark_saved:
 			saveOrUnsaveEntry();
 			return true;
 		case R.id.action_mark_unread:
 			markEntry(MARK_KEEP_UNREAD, getSuccessListener(getResources().getString(R.string.kept_as_unread)));
 			return true;
 		case R.id.action_open_in_browser:
 			openEntryInBrowser(entry);
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/* Called whenever we call invalidateOptionsMenu() */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (entry != null && entry.isSaved()) {
 			// paint the star
 			MenuItem mItem = actionBarmenu.findItem(R.id.action_mark_saved);
 			mItem.setIcon(getResources().getDrawable(R.drawable.star_big_on));
 			mItem.setTitle(getResources().getString(R.string.mark_unsaved));
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	/**
 	 * Save or unsave an entry
 	 */
 	private void saveOrUnsaveEntry() {
 		int method = Method.PUT;
 		String successMessage;
 		if (entry.isSaved()) {
 			method = Method.DELETE;
 			successMessage = getResources().getString(R.string.unsaved_article);
 			entry.setSaved(false);
 		} else {
 			successMessage = getResources().getString(R.string.saved_article);
 			entry.setSaved(true);
 		}
 		supportInvalidateOptionsMenu();
 
 		SharedPreferences sprPreferences = getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
 		String accessToken = sprPreferences.getString(MainActivity.SHPREF_KEY_ACCESS_TOKEN, null);
 		String userId = sprPreferences.getString(MainActivity.SHPREF_KEY_USERID_TOKEN, null);
 
 		// send request
 		RequestQueue queue = MyVolley.getRequestQueue();
 		try {
 			JSONObject jsonRequest = new JSONObject();
 			JSONArray entries = new JSONArray();
 			entries.put(entry.getId());
 			jsonRequest.put(ENTRIES_IDS, entries);
 			if (accessToken != null && userId != null) {
 				String userIdEncoded = URLEncoder.encode("/" + userId.toString() + "/tag/", MainActivity.UTF_8);
 				// String userIdEncoded = "/" +
 				// URLEncoder.encode(userId.toString(), MainActivity.UTF_8)+
 				// "/tag/";
 				StringBuilder url = new StringBuilder();
 				url.append(MainActivity.ROOT_URL).append(MainActivity.TAGS_PATH).append("/user").append(userIdEncoded)
 						.append(MainActivity.GLOBAL_SAVED);
 				JsonCustomRequest myReq = NetworkUtils.getJsonCustomRequest(method, url.toString(), jsonRequest,
 						getSuccessListener(successMessage), createMyReqErrorListener(ERROR_LISTNENER_ORIGIN.SAVING),
 						accessToken);
 				queue.add(myReq);
 			}
 		} catch (JSONException uex) {
 			Log.e(TAG, "JSONException marking as read/unread entry");
 		} catch (UnsupportedEncodingException uex) {
 			Log.e(TAG, "Error encoding URL when saving/unsaving entry");
 		}
 
 	}
 
 	/**
 	 * Mark entry as read or unread
 	 * 
 	 * @param markOrUnmark
 	 *            read or unread
 	 * @param successListener
 	 *            successListener
 	 */
 	private void markEntry(String markOrUnmark, Listener<JSONObject> successListener) {
 		RequestQueue queue = MyVolley.getRequestQueue();
 		try {
 			if (markOrUnmark.equals(MARK_READ)) {
 				entry.setUnread(false);
 			} else {
 				entry.setUnread(true);
 			}
 			JSONObject jsonRequest = new JSONObject();
 			jsonRequest.put("action", markOrUnmark);
 			jsonRequest.put("type", "entries");
 			JSONArray entries = new JSONArray();
 			entries.put(entryId);
 			jsonRequest.put("entryIds", entries);
 			JsonCustomRequest myReq = NetworkUtils.getJsonCustomRequest(Method.POST, MainActivity.ROOT_URL
 					+ MARKERS_PATH, jsonRequest, successListener,
 					createMyReqErrorListener(ERROR_LISTNENER_ORIGIN.MARKING), accessToken);
 			queue.add(myReq);
 		} catch (JSONException uex) {
 			Log.e(TAG, "Error marking read or unread");
 		}
 	}
 
 	private Response.Listener<JSONObject> getSuccessListener(final String message) {
 		return new Response.Listener<JSONObject>() {
 			@Override
 			public void onResponse(JSONObject response) {
 				if (message != null) {
 					Toast.makeText(FeedEntryActivity.this, message, Toast.LENGTH_SHORT).show();
 				}
 			}
 		};
 	}
 
 	private static final String TEXT_PLAIN = "text/plain";
 
 	/**
 	 * Share entry
 	 */
 	private void shareEntry() {
 		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
 		sharingIntent.setType(TEXT_PLAIN);
 		String shareBody = entry.getUrl();
 		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, entry.getTitle());
 		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
 		startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_text)));
 	}
 
 }
