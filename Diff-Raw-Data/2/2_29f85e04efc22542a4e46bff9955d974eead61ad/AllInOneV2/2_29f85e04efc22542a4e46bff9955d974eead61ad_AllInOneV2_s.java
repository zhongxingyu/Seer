 package com.ioabsoftware.DroidFAQs;
 
 import java.io.File;
 import java.net.URLDecoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.holoeverywhere.app.Activity;
 import org.holoeverywhere.app.AlertDialog;
 import org.holoeverywhere.app.Dialog;
 import org.jsoup.Connection.Response;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.TextNode;
 import org.jsoup.select.Elements;
 
 import android.annotation.SuppressLint;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.DialogInterface.OnShowListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.HapticFeedbackConstants;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 import com.actionbarsherlock.widget.SearchView;
 import com.handmark.pulltorefresh.library.PullToRefreshBase;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
 import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
 import com.ioabsoftware.DroidFAQs.HandlesNetworkResult.NetDesc;
 import com.ioabsoftware.DroidFAQs.Networking.Session;
 import com.ioabsoftware.DroidFAQs.Views.BoardView;
 import com.ioabsoftware.DroidFAQs.Views.BoardView.BoardViewType;
 import com.ioabsoftware.DroidFAQs.Views.HeaderView;
 import com.ioabsoftware.DroidFAQs.Views.LastPostView;
 import com.ioabsoftware.DroidFAQs.Views.MessageView;
 import com.ioabsoftware.DroidFAQs.Views.PMDetailView;
 import com.ioabsoftware.DroidFAQs.Views.PMView;
 import com.ioabsoftware.DroidFAQs.Views.TopicView;
 import com.ioabsoftware.DroidFAQs.Views.TopicView.TopicViewType;
 import com.ioabsoftware.DroidFAQs.Views.UserDetailView;
 import com.ioabsoftware.gameraven.R;
 
 public class AllInOneV2 extends Activity implements OnNavigationListener {
 	
 	private static boolean needToCheckForUpdate = true;
 	private static boolean isReleaseBuild = true;
 	
 	public static final int CHANGE_LOGGED_IN_DIALOG = 100;
 	public static final int NEW_VERSION_DIALOG = 101;
 	public static final int SEND_PM_DIALOG = 102;
 	public static final int MESSAGE_ACTION_DIALOG = 103;
 	public static final int REPORT_MESSAGE_DIALOG = 104;
 	
 	private static final String ACCOUNTS_PREFNAME = "com.ioabsoftware.DroidFAQs.Accounts";
 	private static final String SALT = "RIP Man fan died at the room shot up to 97 degrees";
 	
 	public static String defaultSig;
 	
 	/** current session */
 	private Session session = null;
 	public Session getSession()
 	{return session;}
 	
 	private String boardID;
 	public String getBoardID()
 	{return boardID;}
 	private String topicID;
 	public String getTopicID()
 	{return topicID;}
 	private String messageID;
 	public String getMessageID()
 	{return messageID;}
 	private String postPostUrl;
 	public String getPostPostUrl()
 	{return postPostUrl;}
 	
 	private String savedPostBody;
 	public String getSavedPostBody()
 	{return savedPostBody;}
 	private String savedPostTitle;
 	public String getSavedPostTitle()
 	{return savedPostTitle;}
 	
 	/** preference object for global settings */
 	private static SharedPreferences settings = null;
 	public static SharedPreferences getSettingsPref()
 	{return settings;}
 	
 	/** list of accounts (username, password) */
 	private static AccountPreferences accounts = null;
 	public static AccountPreferences getAccounts()
 	{return accounts;}
 	
 	private LinearLayout titleWrapper;
 	private EditText postTitle;
 	private EditText postBody;
 	
 	private TextView titleCounter;
 	private TextView bodyCounter;
 	
 	private Button postButton;
 	private Button cancelButton;
 	
 	private LinearLayout postWrapper;
 	
 	private PullToRefreshScrollView contentPTR;
 	private ScrollView contentScroller;
 	private LinearLayout content;
 	private WebView adView;
 	
 	private ActionBar aBar;
 	private MenuItem refreshIcon;
 	private MenuItem postIcon;
 	private MenuItem addFavIcon;
 	private MenuItem remFavIcon;
 	private MenuItem searchIcon;
 	private MenuItem topicListIcon;
     private String[] navList;
 
     private String tlUrl;
     
 	private enum PostMode {ON_BOARD, ON_TOPIC, NEW_PM};
 	private PostMode pMode;
     
     private TextView title;
     private Button firstPage, prevPage, nextPage, lastPage;
     private String firstPageUrl, prevPageUrl, nextPageUrl, lastPageUrl;
     private NetDesc pageJumperDesc;
     private TextView pageLabel;
     
     private View pageJumperWrapper;
 	
 	public int getScrollerVertLoc() {
 		return contentScroller.getScrollY();}
 	
 	private final ClickListener cl = new ClickListener();
 	
 	private static boolean usingLightTheme;
 	public static boolean getUsingLightTheme() {return usingLightTheme;}
 	
 	/**********************************************
 	 * START METHODS
 	 **********************************************/
 	
 
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		settings = PreferenceManager.getDefaultSharedPreferences(this);
         
 		usingLightTheme = settings.getBoolean("useLightTheme", false);
         if (usingLightTheme) {
         	setTheme(R.style.MyThemes_LightTheme);
         }
         
     	super.onCreate(savedInstanceState);
     	
     	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         
 //        getSherlock().setProgressBarIndeterminateVisibility(false);
         addonSherlock().setProgressBarIndeterminateVisibility(false);
     	
         setContentView(R.layout.allinonev2);
         
         String nSlashA = settings.getString("defaultAccount", "N/A");
         settings.edit().putString("defaultAccount", nSlashA).commit();
         
         boolean enablePTR = settings.getBoolean("enablePTR", false);
         settings.edit().putBoolean("enablePTR", enablePTR).commit();
         
         aBar = getSupportActionBar();
         aBar.setDisplayShowHomeEnabled(false);
         aBar.setDisplayShowTitleEnabled(false);
         
     	wtl("logging started from onCreate");
     	
     	wtl("getting accounts");
         accounts = new AccountPreferences(getApplicationContext(), ACCOUNTS_PREFNAME, SALT, false);
 
     	wtl("getting all the views");
         
         contentPTR = (PullToRefreshScrollView) findViewById(R.id.aioScroller);
         contentPTR.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
 			@Override
 			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
 				refreshClicked(contentPTR);
 			}
 		});
         
         contentScroller = contentPTR.getRefreshableView();
         content = new LinearLayout(this);
         content.setOrientation(LinearLayout.VERTICAL);
         contentScroller.addView(content);
 
         titleWrapper  = (LinearLayout) findViewById(R.id.aioTitleWrapper);
         postTitle = (EditText) findViewById(R.id.aioPostTitle);
         postBody = (EditText) findViewById(R.id.aioPostBody);
         titleCounter = (TextView) findViewById(R.id.aioTitleCounter);
         bodyCounter = (TextView) findViewById(R.id.aioBodyCounter);
         
         title = (TextView) findViewById(R.id.aioTitle);
         title.setSelected(true);
         
         pageJumperWrapper = findViewById(R.id.aioPageJumperWrapper);
         firstPage = (Button) findViewById(R.id.aioFirstPage);
         firstPage.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				session.get(pageJumperDesc, firstPageUrl, null);
 			}
 		});
         prevPage = (Button) findViewById(R.id.aioPreviousPage);
         prevPage.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				session.get(pageJumperDesc, prevPageUrl, null);
 			}
 		});
         nextPage = (Button) findViewById(R.id.aioNextPage);
         nextPage.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				session.get(pageJumperDesc, nextPageUrl, null);
 			}
 		});
         lastPage = (Button) findViewById(R.id.aioLastPage);
         lastPage.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				session.get(pageJumperDesc, lastPageUrl, null);
 			}
 		});
         pageLabel = (TextView) findViewById(R.id.aioPageLabel);
         
         postTitle.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {
 				String escapedTitle = StringEscapeUtils.escapeHtml4(postTitle.getText().toString());
 				titleCounter.setText(escapedTitle.length() + "/80");
 			}
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				
 			}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 	    });
         
         postBody.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {
 				String escapedBody = StringEscapeUtils.escapeHtml4(postBody.getText().toString());
 				// GFAQs adds 13(!) characters onto bodies when they have a sig, apparently.
 				int length = escapedBody.length() + getSig().length() + 13;
 				bodyCounter.setText(length + "/4096 (includes sig)");
 			}
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				
 			}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 	    });
         
         postButton = (Button) findViewById(R.id.aioPostDo);
         cancelButton = (Button) findViewById(R.id.aioPostCancel);
         
         postWrapper = (LinearLayout) findViewById(R.id.aioPostWrapper);
 
     	wtl("creating default sig");
 		defaultSig = "This post made using GameRaven *grver*\n<i>Insanity On A Bun Software - Pushing the bounds of software sanity</i>";
 
     	wtl("getting css directory");
 		File cssDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gameraven");
     	if (!cssDirectory.exists()) {
         	wtl("css directory does not exist, creating");
     		cssDirectory.mkdir();
     	}
     	
     	wtl("setting check for update flag");
     	needToCheckForUpdate = true;
 		
 		wtl("onCreate finishing");
     }
 	
 	public void disableNavList() {
 		aBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 	}
 	
 	public void setNavList(boolean isLoggedIn) {
 		if (isLoggedIn)
 			navList = getResources().getStringArray(R.array.navListLoggedIn);
 		else
 			navList = getResources().getStringArray(R.array.navListLoggedOut);
 
         Context context = aBar.getThemedContext();
         ArrayAdapter<CharSequence> list;
         
         if (isLoggedIn)
         	list = ArrayAdapter.createFromResource(context, R.array.navListLoggedIn, R.layout.sherlock_spinner_item);
         else
         	list = ArrayAdapter.createFromResource(context, R.array.navListLoggedOut, R.layout.sherlock_spinner_item);
         
         list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
         
         aBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
         aBar.setListNavigationCallbacks(list, this);
 	}
 	
 	@Override
     public boolean onSearchRequested() {
     	if (searchIcon.isVisible())
     		searchIcon.expandActionView();
 		return false;
     }
 	
 	/** Adds menu items */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.menu_main, menu);
         searchIcon = menu.getItem(0);
         addFavIcon = menu.getItem(1);
         remFavIcon = menu.getItem(2);
         topicListIcon = menu.getItem(3);
         postIcon = menu.getItem(4);
         refreshIcon = menu.getItem(5);
         
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         SearchView searchView = (SearchView) searchIcon.getActionView();
         if (null != searchView )
         {
             searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));  
         }
 
         SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() 
         {
             public boolean onQueryTextChange(String newText) 
             {
                 // just do default
                 return false;
             }
 
             public boolean onQueryTextSubmit(String query) 
             {
       	      HashMap<String, String> data = new HashMap<String, String>();
       	      if (session.getLastDesc() == NetDesc.BOARD) {
       	    	  wtl("searching board for query");
       	    	  data.put("search", query);
           	      session.get(NetDesc.BOARD, session.getLastPathWithoutData(), data);
       	      }
       	      else if (session.getLastDesc() == NetDesc.BOARD_JUMPER || session.getLastDesc() == NetDesc.GAME_SEARCH) {
       	    	  wtl("searching for games");
       	    	  data.put("game", query);
           	      session.get(NetDesc.GAME_SEARCH, "/search/index.html", data);
       	      }
                 return true;
             }
         };
         searchView.setOnQueryTextListener(queryTextListener);
         
         return true;
     }
     
     /** fires when a menu option is selected */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.search:
         	searchIcon.expandActionView();
         	return true;
         	
         case R.id.addFav:
         	String addFavUrl = session.getLastPath();
         	if (addFavUrl.contains("remfav"))
         		addFavUrl = addFavUrl.replace("remfav", "addfav");
         	else if (addFavUrl.indexOf('?') != -1)
         		addFavUrl += "&action=addfav";
         	else
         		addFavUrl += "?action=addfav";
         	
         	session.get(NetDesc.BOARD, addFavUrl, null);
         	return true;
         	
         case R.id.remFav:
         	String remFavUrl = session.getLastPath();
         	if (remFavUrl.contains("addfav"))
         		remFavUrl = remFavUrl.replace("addfav", "remfav");
         	else if (remFavUrl.indexOf('?') != -1)
         		remFavUrl += "&action=remfav";
         	else
         		remFavUrl += "?action=remfav";
         	
         	session.get(NetDesc.BOARD, remFavUrl, null);
         	return true;
         	
         case R.id.topiclist:
         	session.get(NetDesc.BOARD, tlUrl, null);
         	return true;
         	
         case R.id.post:
         	if (pMode == PostMode.ON_BOARD)
         		postOnBoardSetup();
         	else if (pMode == PostMode.ON_TOPIC)
             	postOnTopicSetup();
         	else if (pMode == PostMode.NEW_PM)
         		pmSetup("", "", "");
         	
         	return true;
         	
         case R.id.refresh:
         	if (session.getLastPath() == null) {
     			if (Session.isLoggedIn())
     				session = new Session(this, Session.getUser(), accounts.getString(Session.getUser()));
     	    	else
     	    		session = new Session(this);
     		}
     		else
     			session.refresh();
         	return true;
         	
         case R.id.changeAccount:
         	showDialog(CHANGE_LOGGED_IN_DIALOG);
         	return true;
         	
         case R.id.changeSettings:
         	startActivity(new Intent(this, SettingsMain.class));
         	return true;
         	
         case R.id.openInBrowser:
         	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(session.getLastPath()));
 			startActivity(browserIntent);
         	return true;
         	
         case R.id.exit:
         	finish();
         	return true;
         	
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 	
 	@Override
     public void onResume() {
     	wtl("onResume fired");
     	super.onResume();
 		
 		if (settings.getBoolean("enablePTR", false))
 			contentPTR.setMode(Mode.BOTH);
 		else
 	        contentPTR.setMode(Mode.DISABLED);
 		
 		if (session != null && settings.getBoolean("reloadOnResume", false)) {
     		wtl("session exists, reload on resume is true, refreshing page");
     		session.refresh();
     	}
     	
     	if (session == null) {
     		wtl("creating a new session");
     		String defaultAccount = settings.getString("defaultAccount", "");
     		if (accounts.containsKey(defaultAccount))
     			session = new Session(this, defaultAccount, accounts.getString(defaultAccount));
     		else
     			session = new Session(this);
     	}
 		
 		if (needToCheckForUpdate && isReleaseBuild) {
 			needToCheckForUpdate = false;
 			wtl("starting update check");
 			session.devCheckForUpdate();
 		}
 		
 		title.setSelected(true);
         
 		wtl("onResume finishing");
     }
 	
 	public void postError(String msg) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(msg);
 		builder.setTitle("There was a problem with your post...");
 		builder.setPositiveButton("Ok", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		builder.create().show();
 
 		uiCleanup();
 	}
 	
 	public void fourOhError(int errorNum, String msg) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		msg = msg.replace("..", ".");
 		builder.setMessage(msg);
 		builder.setTitle(errorNum + " Error");
 		builder.setPositiveButton("Ok", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		builder.create().show();
 
 		uiCleanup();
 	}
 	
 	public void timeoutCleanup(NetDesc desc) {
 		if (desc != NetDesc.DEV_UPDATE_CHECK) {
 			String msg = "timeout msg unset";
 			switch (desc) {
 			case LOGIN_S1:
 			case LOGIN_S2:
 				msg = "Login timed out, refresh to try again.";
 				break;
 			case POSTMSG_S1:
 			case POSTMSG_S2:
 			case POSTMSG_S3:
 			case POSTTPC_S1:
 			case POSTTPC_S2:
 			case POSTTPC_S3:
 			case QPOSTMSG_S1:
 			case QPOSTMSG_S3:
 			case QPOSTTPC_S1:
 			case QPOSTTPC_S3:
 				msg = "Post timed out. After refreshing, check to see if your post made it through before attempting to post again.";
 				break;
 			default:
 				msg = "Connection timed out, refresh to try again.";
 				break;
 
 			}
 			AlertDialog.Builder b = new AlertDialog.Builder(this);
 			b.setTitle("Timeout");
 			b.setMessage(msg);
 			b.setPositiveButton("Refresh", new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					refreshClicked(new View(AllInOneV2.this));
 				}
 			});
 			b.setNegativeButton("Dismiss", new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 				}
 			});
 			b.create().show();
 
 			uiCleanup();
 		}
 	}
 
 	private void uiCleanup() {
 		addonSherlock().setProgressBarIndeterminateVisibility(false);
 		if (refreshIcon != null)
 			refreshIcon.setVisible(true);
 		if (postWrapper.getVisibility() == View.VISIBLE) {
 			postButton.setEnabled(true);
 			cancelButton.setEnabled(true);
 			if (postIcon != null)
 				postIcon.setVisible(true);
 		}
 	}
 	
 	public void preExecuteSetup(NetDesc desc) {
 		wtl("GRAIO dPreES fired --NEL, desc: " + desc.name());
 		addonSherlock().setProgressBarIndeterminateVisibility(true);
 		if (refreshIcon != null)
 			refreshIcon.setVisible(false);
 		if (searchIcon != null)
 			searchIcon.setVisible(false);
 		if (postIcon != null)
 			postIcon.setVisible(false);
 		if (addFavIcon != null)
 			addFavIcon.setVisible(false);
 		if (remFavIcon != null)
 			remFavIcon.setVisible(false);
 		if (topicListIcon != null)
 			topicListIcon.setVisible(false);
 		
 		if (desc != NetDesc.POSTMSG_S1 && desc != NetDesc.POSTTPC_S1 &&
 			desc != NetDesc.QPOSTMSG_S1 && desc != NetDesc.QPOSTTPC_S1 &&
 			desc != NetDesc.QEDIT_MSG)
 				postCleanup();
 	}
 	
 	public void postCleanup() {
 		if (postWrapper.getVisibility() == View.VISIBLE) {
 			wtl("postCleanup fired --NEL");
 			postWrapper.setVisibility(View.GONE);
 			postBody.setText(null);
 			postTitle.setText(null);
 			messageID = null;
 			postPostUrl = null;
 			content.requestFocus();
 			
 			((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).
 									hideSoftInputFromWindow(postBody.getWindowToken(), 0);
 		}
 	}
 	
 	/********************************************
 	 * START HNR
 	 * ******************************************/
 	
 	@SuppressLint("SetJavaScriptEnabled")
 	public void processContent(Response res, NetDesc desc) {
 		
 		wtl("GRAIO hNR fired, desc: " + desc.name());
 		
 		contentPTR.setMode(Mode.DISABLED);
 		contentScroller.scrollTo(0, 0);
 
 		searchIcon.setVisible(false);
 		searchIcon.collapseActionView();
 		
 		postIcon.setVisible(false);
 		
 		addFavIcon.setVisible(false);
 		remFavIcon.setVisible(false);
 		
 		topicListIcon.setVisible(false);
 		
 		
 		try {
 			if (res != null) {
 				wtl("res is not null");
 
 				wtl("parsing res");
 				Document pRes = res.parse();
 				
 				if (desc == NetDesc.DEV_UPDATE_CHECK) {
 					wtl("GRAIO hNR determined this is update check response");
 					
 					String version = pRes.getElementById("contentsinner").ownText();
 					version = version.trim();
 					wtl("latest version is " + version);
 					if (!version.equals(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName)) {
 						wtl("version does not match current version, showing update dialog");
 						showDialog(NEW_VERSION_DIALOG);
 					}
 					
 					wtl("GRAIO hNR finishing via return, desc: " + desc.name());
 					return;
 				}
 
 				wtl("setting board, topic, message id to null");
 				boardID = null;
 				topicID = null;
 				messageID = null;
 				
 				content.removeAllViews();
 				
 				Element tbody;
 				Element pj = null;
 				String headerTitle;
 				String firstPage = null;
 				String prevPage = null;
 				String currPage = "1";
 				String pageCount = "1";
 				String nextPage = null;
 				String lastPage = null;
 				
 				boolean usingUserPanel;
 				usingUserPanel = !pRes.getElementsByClass("user_panel").isEmpty();
 				int[] pNums;
 				
 				Element pmInboxLink = pRes.select("a[href=/pm/]").first();
 				if (pmInboxLink != null) {
 					if (!pmInboxLink.text().equals("Inbox")) {
 						Toast.makeText(this, "You have unread PM(s)", Toast.LENGTH_SHORT).show();
 					}
 				}
 				
 				switch (desc) {
 				case BOARD_JUMPER:
 				case LOGIN_S2:
 					updateHeaderNoJumper("Board Jumper", NetDesc.BOARD_JUMPER);
 					content.addView(new HeaderView(this, "Announcements"));
 					
 					searchIcon.setVisible(true);
 					
 					processBoards(pRes, true);
 					break;
 					
 				case BOARD_LIST:
 					updateHeaderNoJumper(pRes.getElementsByTag("th").get(4).text(), NetDesc.BOARD_LIST);
 					processBoards(pRes, true);
 					break;
 					
 				case PM_INBOX:
 					tbody = pRes.getElementsByTag("tbody").first();
 
 					headerTitle = Session.getUser() + "'s PM Inbox";
 					
 					if (tbody != null) {
 						pj = tbody.parent().nextElementSibling();
 						
 						if (pj != null && pj.hasClass("foot")) {
 							String pjText = pj.text();
 							//Page 1 of 4  Next Page 
 							int currPageStart = pjText.indexOf("Page ") + 5;
 							int ofIndex = pjText.indexOf(" of ");
 							currPage = pjText.substring(currPageStart, ofIndex);
 							int pageCountEnd = pjText.indexOf('\u00A0', ofIndex);
 							if (pageCountEnd == -1)
 								pageCountEnd = pjText.length();
 							pageCount = pjText.substring(ofIndex + 4, pageCountEnd);
 							pageCount.trim();
 							int currPageNum = Integer.parseInt(currPage);
 							int pageCountNum = Integer.parseInt(pageCount);
 							
 							if (currPageNum > 1) {
 								firstPage = "/pm/";
 								prevPage = "/pm/?page=" + (currPageNum - 2);
 							}
 							if (currPageNum != pageCountNum) {
 								nextPage = "/pm/?page=" + currPageNum;
 								lastPage = "/pm/?page=" + (pageCountNum - 1);
 							}
 						}
 						
 						updateHeader(headerTitle, firstPage, prevPage, currPage, 
 									 pageCount, nextPage, lastPage, NetDesc.PM_INBOX);
 						
 						for (Element row : tbody.getElementsByTag("tr")) {
 							Elements cells = row.children();
 							// [icon] [sender] [subject] [time] [check]
 							boolean isOld = true;
 							if (cells.get(0).children().first().attr("alt").equals("New"))
 								isOld = false;
 							String sender = cells.get(1).text();
 							Element subjectLinkElem = cells.get(2).children().first();
 							String subject = subjectLinkElem.text();
 							String link = subjectLinkElem.attr("href");
 							String time = cells.get(3).text();
 							
 							PMView pm = new PMView(this, subject, sender, time, link);
 							pm.setOnClickListener(cl);
 							
 							if (isOld)
 								pm.setOld();
 							
 							content.addView(pm);
 						}
 					}
 					else {
 						updateHeaderNoJumper(headerTitle, NetDesc.PM_INBOX);
 						content.addView(new HeaderView(this, "You have no private messages at this time."));
 					}
 					
 					postIcon.setVisible(true);
 					pMode = PostMode.NEW_PM;
 					break;
 //					
 				case READ_PM:
 					headerTitle = pRes.select("h2.title").first().text();
 					String pmTitle = headerTitle;
 					if (!pmTitle.startsWith("Re: "))
 						pmTitle = "Re: " + pmTitle;
 
 					String pmMessage = pRes.select("div.message").first().outerHtml();
 					
 					Element foot = pRes.select("div.foot").first();
 					foot.getElementsByTag("div").get(1).remove();
 					String pmFoot = foot.outerHtml();
 					
 					//Sent by Barihhl to Corrupt_Power
 					String footText = foot.text();
 					String sender = footText.substring(8, footText.toLowerCase(Locale.US).indexOf
 													(" to " + Session.getUser().toLowerCase(Locale.US)));
 					
 					updateHeaderNoJumper(pmTitle, NetDesc.READ_PM);
 					
 					PMDetailView pmd = new PMDetailView(this, sender, pmTitle, pmMessage + pmFoot);
 					pmd.setOnClickListener(cl);
 					content.addView(pmd);
 					break;
 					
 				case AMP_LIST:
 					wtl("GRAIO hNR determined this is an amp response");
 					
 					tbody = pRes.getElementsByTag("tbody").first();
 					
 					headerTitle = Session.getUser() + "'s Active Messages";
 					pj = pRes.getElementsByClass("pages").first();
 					
 					pNums = getPageNums(pRes, pj, usingUserPanel);
 					String amp = buildAMPLink();
 					
 					if (pNums[0] > 1) {
 						firstPage = amp;
 						prevPage = amp + "&page=" + (pNums[0] - 2);
 					}
 					if (pNums[0] != pNums[1]) {
 						nextPage =  amp + "&page=" + pNums[0];
 						lastPage =  amp + "&page=" + (pNums[1] - 1);
 					}
 					
 					updateHeader(headerTitle, firstPage, prevPage, Integer.toString(pNums[0]), 
 								 Integer.toString(pNums[1]), nextPage, lastPage, NetDesc.AMP_LIST);
 					
 					if (!tbody.children().isEmpty()) {
 						for (Element row : tbody.children()) {
 							// [board] [title] [msg] [last post] [your last post]
 							Elements cells = row.children();
 							String board = cells.get(0).text();
 							Element titleLinkElem = cells.get(1).children()
 									.first();
 							String title = titleLinkElem.text();
 							String link = titleLinkElem.attr("href");
 							String mCount = cells.get(2).text();
 							Element lPostLinkElem = cells.get(3).children()
 									.first();
 							String lPost = lPostLinkElem.text();
 							String lPostLink = lPostLinkElem.attr("href");
 
 							TopicView topic = new TopicView(this, title, board,
 									lPost, mCount, link, TopicViewType.NORMAL);
 
 							topic.setOnClickListener(cl);
 							topic.setOnLongClickListener(cl);
 
 							LastPostView lp = (LastPostView) topic
 									.findViewById(R.id.tvLastPostLink);
 							lp.setUrl(lPostLink);
 							lp.setOnClickListener(cl);
 
 							content.addView(topic);
 						}
 					}
 					else {
 						content.addView(new HeaderView(this, "You have no active messages at this time."));
 					}
 					
 					wtl("amp response block finished");
 					break;
 					
 				case BOARD:
 					wtl("GRAIO hNR determined this is a board response");
 					
 					wtl("setting board id");
 					getBoardID(res.url().toString());
 					
 					boolean isSplitList = false;
 					if (pRes.getElementsByTag("th").first() != null) {
 						if (pRes.getElementsByTag("th").first().text().equals("Board Title")) {
 							wtl("is actually a split board list");
 							
 							updateHeaderNoJumper(pRes.getElementsByClass("head").first().text(), NetDesc.BOARD);
 							
 							processBoards(pRes, false);
 							
 							isSplitList = true;
 						}
 					}
 					
 					if (!isSplitList) {
 						String url = res.url().toString();
 						String searchQuery = "";
 						String searchPJAddition = "";
 						if (url.contains("search=")) {
 							wtl("board search url: " + url);
 							searchQuery = url.substring(url.indexOf("search=") + 7);
 							int i = searchQuery.indexOf('&');
 							if (i != -1)
 								searchQuery.replace(searchQuery.substring(i), "");
 							
 							searchPJAddition = "&search=" + searchQuery;
 							searchQuery = URLDecoder.decode(searchQuery);
 						}
 						
 						if (searchQuery.equals("")) {
 							headerTitle = pRes.getElementsByClass("head").first().text();
 						}
 						else {
 							headerTitle = pRes.getElementsByClass("head").first().text() + " (search: " + searchQuery + ")";
 						}
 						
 						pNums = getPageNums(pRes, pj, usingUserPanel);
 						
 						if (pNums[0] > 1) {
 							firstPage = "boards/" + boardID+ "?page=0" + searchPJAddition;
 							prevPage = "boards/" + boardID + "?page=" + (pNums[0] - 2) + searchPJAddition;
 						}
 						if (pNums[0] != pNums[1]) {
 							nextPage =  "boards/" + boardID + "?page=" + pNums[0] + searchPJAddition;
 							lastPage =  "boards/" + boardID + "?page=" + (pNums[1] - 1) + searchPJAddition;
 						}
 						
 						updateHeader(headerTitle, firstPage, prevPage, Integer.toString(pNums[0]), 
 								Integer.toString(pNums[1]), nextPage, lastPage, NetDesc.BOARD);
 						
 						searchIcon.setVisible(true);
 						
 						if (Session.isLoggedIn()) {
 							String favtext;
 							if (usingUserPanel) 
 								favtext = pRes.getElementsByClass("links").first().text().toLowerCase();
 							else 
 								favtext = pRes.getElementsByClass("user").first().text().toLowerCase();
 							
 							if (favtext.contains("add to favorites"))
 								addFavIcon.setVisible(true);
 							else if (favtext.contains("remove favorite"))
 								remFavIcon.setVisible(true);
 
 							updatePostingRights(pRes, false, usingUserPanel);
 						}
 
 						wtl("updating user level");
 						updateUserLevel(pRes);
 						
 						Element table = pRes.getElementsByTag("table").first();
 						if (table != null) {
 							
 							table.getElementsByTag("col").get(2).remove();
 							table.getElementsByTag("th").get(2).remove();
 							table.getElementsByTag("col").get(0).remove();
 							table.getElementsByTag("th").get(0).remove();
 							
 							wtl("board row parsing start");
 							boolean skipFirst = true;
 							for (Element row : table.getElementsByTag("tr")) {
 								if (!skipFirst) {
 									Elements cells = row.getElementsByTag("td");
 									// cells = [image] [title] [author] [post count] [last post]
 									String tImg = cells.get(0).child(0).attr("src");
 									Element titleLinkElem = cells.get(1).child(0);
 									String title = titleLinkElem.text();
 									String tUrl = titleLinkElem.attr("href");
 									String tc = cells.get(2).text();
 									Element lPostLinkElem = cells.get(4).child(0);
 									String lastPost = lPostLinkElem.text();
 									String lpUrl = lPostLinkElem.attr("href");
 									String mCount = cells.get(3).text();
 									
 									TopicViewType type = TopicViewType.NORMAL;
 									if (!tImg.endsWith("topic.gif")) {
 										if (tImg.endsWith("closed.gif"))
 											type = TopicViewType.LOCKED;
 										else if (tImg.endsWith("archived.gif"))
 											type = TopicViewType.ARCHIVED;
 										else if (tImg.endsWith("poll.gif"))
 											type = TopicViewType.POLL;
 										else if (tImg.endsWith("sticky.gif"))
 											type = TopicViewType.PINNED;
 									}
 									
 									TopicView topic = new TopicView(this, title, tc, lastPost, mCount, tUrl, type);
 									topic.setOnClickListener(cl);
 									LastPostView lp = (LastPostView) topic.findViewById(R.id.tvLastPostLink);
 									lp.setUrl(lpUrl);
 									lp.setOnClickListener(cl);
 									
 									content.addView(topic);
 								}
 								else
 									skipFirst = false;
 							}
 							wtl("board row parsing end");
 						}
 						else {
 							content.addView(new HeaderView(this, "There are no topics at this time."));
 						}
 					}
 					
 					wtl("board response block finished");
 					break;
 					
 				case TOPIC:
 					getBoardID(res.url().toString());
 					getTopicID(res.url().toString());
 
 					tlUrl = "boards/" + boardID;
 					wtl(tlUrl);
 					topicListIcon.setVisible(true);
 					
 					headerTitle = pRes.getElementsByClass("title").first().text();
 
 					pNums = getPageNums(pRes, pj, usingUserPanel);
 					
 					if (pNums[0] > 1) {
 						firstPage = "boards/" + boardID + "/" + topicID;
 						prevPage = "boards/" + boardID + "/" + topicID + "?page=" + (pNums[0] - 2);
 					}
 					if (pNums[0] != pNums[1]) {
 						nextPage =  "boards/" + boardID + "/" + topicID + "?page=" + pNums[0];
 						lastPage =  "boards/" + boardID + "/" + topicID + "?page=" + (pNums[1] - 1);
 					}
 					
 					updateHeader(headerTitle, firstPage, prevPage, Integer.toString(pNums[0]), 
 							Integer.toString(pNums[1]), nextPage, lastPage, NetDesc.TOPIC);
 					
 					if (Session.isLoggedIn()) {
 						updatePostingRights(pRes, true, usingUserPanel);
 					}
 					
 					updateUserLevel(pRes);
 					
 					Elements rows = pRes.getElementsByTag("table").first().getElementsByTag("tr");
 					int rowCount = rows.size();
 					
 					MessageView message = null;
 					for (int x = 0; x < rowCount; x++) {
 						List<TextNode> textNodes = rows.get(x).child(0).child(0).textNodes();
 						Elements elements = rows.get(x).child(0).child(0).children();
 						
 						String userTitles = "";
 						String postTimeText = "";
 						
 						int textNodesSize = textNodes.size();
 						for (int y = 0; y < textNodesSize; y++) {
 							String text = textNodes.get(y).text();
 							if (text.startsWith("Posted"))
 								postTimeText = text;
 							else if (text.contains("(")) {
 								userTitles += " (" + text.substring(text.indexOf('(') + 1, text.lastIndexOf(')')) + ")";
 							}
 						}
 						
 						String user = null;
 						String postNum = null;
 						String mID = null;
 						int elementsSize = elements.size();
 						for (int y = 0; y < elementsSize; y++) {
 							if (y == 0)
 								user = elements.get(y).text();
 							else if (y == 1)
 								postNum = elements.get(y).attr("name");
 							else if (elements.get(y).text().equals("message detail")) {
 								String detailLink = elements.get(y).attr("href");
 								int z = detailLink.lastIndexOf('/') + 1;
 								mID = detailLink.substring(z);
 							}
 						}
 						
 						//Posted 11/15/2012 11:20:27&nbsp;AM | 
 						int endPoint = postTimeText.indexOf('|') - 1;
 						if (endPoint < 0) endPoint = postTimeText.length();
 						String postTime = postTimeText.substring(7, endPoint);
 						
 						x++;
 						
 						message = new MessageView(this, user, userTitles, postNum, postTime,
 												  rows.get(x), boardID, topicID, mID);
 						message.setOnClickListener(cl);
 						
 						content.addView(message);
 					}
 					
 					final MessageView lastMessage = message;
 					if (goToLastPost && !Session.applySavedScroll) {
 						contentPTR.post(new Runnable() {
 					        @Override
 					        public void run() {
 					        	contentPTR.getRefreshableView().smoothScrollTo(0, lastMessage.getTop());
 					        }
 					    });
 					}
 					
 					break;
 					
 				case USER_DETAIL:
 					Elements udRows = pRes.getElementsByTag("tbody").first().children();
 					String name = null;
 					String ID = null;
 					String level = null;
 					String creation = null;
 					String lVisit = null;
 					String sig = null;
 					String karma = null;
 					String AMP = null;
 					for (Element row : udRows) {
 						String label = row.child(0).text().toLowerCase();
 						if (label.equals("user name"))
 							name = row.child(1).text();
 						else if (label.equals("user id"))
 							ID = row.child(1).text();
 						else if (label.equals("board user level"))
 							level = row.child(1).html();
 						else if (label.equals("account created"))
 							creation = row.child(1).text();
 						else if (label.equals("last visit"))
 							lVisit = row.child(1).text();
 						else if (label.equals("signature"))
 							sig = row.child(1).html();
 						else if (label.equals("karma"))
 							karma = row.child(1).text();
 						else if (label.equals("active messages posted"))
 							AMP = row.child(1).text();
 					}
 					
 					updateHeaderNoJumper(name + "'s Details", NetDesc.USER_DETAIL);
 					
 					UserDetailView userDetail = new UserDetailView(this, name, ID, level, creation, lVisit, sig, karma, AMP);
 					content.addView(userDetail);
 					break;
 //					
 //				case MSG_DETAIL:
 //					//aBar.setTitle("Message Detail");
 //					abTitle.setText("Message Detail");
 //					
 //					getBoardID(res.url().toString());
 //					getTopicID(res.url().toString());
 //					getMessageID(res.url().toString());
 //					
 //					String key = pRes.getElementsByAttributeValue("name", "key").first().attr("value");
 //					
 //					data.append(pRes.getElementsByClass("body").get(1).outerHtml());
 //					
 //					data.append("<table class=\"gameraven_table\">");
 //					
 //					if (!pRes.select("h2:containsOwn(Edit this Message)").isEmpty()) {
 //						data.append("<tr><td><p>You can edit your message within one hour of posting to fix any errors or add additional " +
 //								"information.</p><a href=\"GAMERAVEN_EDIT_POST\">Edit post</a></td></tr>");
 //					}
 //
 //					if (!pRes.select("h2:containsOwn(Delete this Message)").isEmpty()) {
 //						data.append("<tr><td><p>You can delete your message from the boards, however, bear in mind that a record of your " +
 //								"deletion will be kept in the database. You can either delete your entire topic, if yours is the first and " +
 //								"only post, or simply overwrite your message with a note saying that you chose to delete the message.</p>" +
 //								"<a href=\"GAMERAVEN_DELETE_POST" + key + "\">Delete post</a></td></tr>");
 //					}
 //
 //					if (!pRes.select("h2:containsOwn(Close this Topic)").isEmpty()) {
 //						data.append("<tr><td><p>As nobody has posted in this topic for the past 5 minutes, you have the option of closing " +
 //								"it, making it impossible to post any additional messages.</p><a href=\"GAMERAVEN_CLOSE_TOPIC" + key + 
 //								"\">Close topic</a></td></tr>");
 //					}
 //					
 //					if (!pRes.getElementsContainingText(
 //							"Report This Message to the Moderators").isEmpty()) {
 //						data.append("<tr><th>You have the ability to report messages to the moderators as abusive. This function allows you " + 
 //								"to report Terms of Use violations and Board Etiquette issues to the moderators, who then will take " +
 //								"appropriate action on it as they reach this message in the queue. The reporting function is anonymous; " +
 //								"only the moderators will see who has marked this message, and that information is deleted when the " +
 //								"moderator takes action. Please note that abuse of this form could result in karma loss or termination " +
 //								"of your account.");
 //						data.append("</th></tr><tr><td>");
 //						data.append("<a href=\"GAMERAVEN_MARK_POST" + "1?" + key + 
 //								"\">Report Offensive: Sexually explicit, racism, threats, pornography</a>");
 //						data.append("</td></tr><tr><td>");
 //						data.append("<a href=\"GAMERAVEN_MARK_POST" + "5?" + key +
 //								"\">Report Advertising: Spam, \"Make Money Fast\", referrer codes</a>");
 //						data.append("</td></tr><tr><td>");
 //						data.append("<a href=\"GAMERAVEN_MARK_POST" + "6?" + key +
 //								"\">Report Illegal Activities: Copyright violations, online game cheats</a>");
 //						data.append("</td></tr><tr><td>");
 //						data.append("<a href=\"GAMERAVEN_MARK_POST" + "18?" + key +
 //								"\">Report Spoiler with no Warning: Revealing critical plot details with no warning</a>");
 //						data.append("</td></tr><tr><td>");
 //						data.append("<a href=\"GAMERAVEN_MARK_POST" + "22?" + key +
 //								"\">Report Harassment/Privacy: Posting personal information, repeated harassment and bullying</a>");
 //						data.append("</td></tr><tr><td>");
 //						data.append("<a href=\"GAMERAVEN_MARK_POST" + "2?" + key +
 //								"\">Report Censor Bypassing: Not properly obscuring offensive/banned words</a>");
 //						data.append("</td></tr><tr><td>");
 //						data.append("<a href=\"GAMERAVEN_MARK_POST" + "3?" + key +
 //								"\">Report Trolling: Provoking other users to respond inappropriately</a>");
 //						data.append("</td></tr><tr><td>");
 //						data.append("<a href=\"GAMERAVEN_MARK_POST" + "4?" + key +
 //								"\">Report Flaming: Insulting other board users</a>");
 //						data.append("</td></tr><tr><td>");
 //						data.append("<a href=\"GAMERAVEN_MARK_POST" + "20?" + key +
 //								"\">Report Disruptive Posting: ALL CAPS, large blank posts, hard-to-read posts, mass bumping</a>");
 //						data.append("</td></tr></table>");
 //					}
 //					
 //					Element postBody = pRes.getElementsByClass("author").get(1).nextElementSibling();
 //					
 //					wtl("cloning body");
 //					Element clonedBody = postBody.clone();
 //					wtl("body cloned finished, checking for poll to remove");
 //					if (!clonedBody.getElementsByClass("board_poll").isEmpty()) {
 //						wtl("post has a poll, removing");
 //						clonedBody.getElementsByClass("board_poll").first().remove();
 //					}
 //
 //					wtl("poll check finished, getting html");
 //					String finalBody = clonedBody.html();
 //
 //					wtl("get html finished, finding sig seperator");
 //					finalBody = finalBody.replace("<span class=\"fspoiler\">", "<spoiler>").replace("</span>", "</spoiler>");
 //					
 //					while (finalBody.contains("<a href")) {
 //						int start = finalBody.indexOf("<a href");
 //						int end = finalBody.indexOf(">", start) + 1;
 //						finalBody = finalBody.replace(finalBody.substring(start, end), "");
 //					}
 //					finalBody = finalBody.replace("</a>", "");
 //					if (finalBody.endsWith("<br />"))
 //						finalBody = finalBody.substring(0, finalBody.length() - 6);
 //					finalBody = finalBody.replace("\n", "");
 //					finalBody = finalBody.replace("<br />", "\n");
 //					
 //					finalBody = StringEscapeUtils.unescapeHtml4(finalBody);
 //					
 //					quoteBodies.add(finalBody);
 //					
 //					break;
 //					
 				case GAME_SEARCH:
 					wtl("GRAIO hNR determined this is a game search response");
 
 					String url = res.url().toString();
 					wtl("game search url: " + url);
 					
 					String searchQuery = url.substring(url.indexOf("game=") + 5);
 					int i = searchQuery.indexOf("&");
 					if (i != -1)
 						searchQuery = searchQuery.replace(searchQuery.substring(i), "");
 					
 					int pageIndex = url.indexOf("page=");
 					if (pageIndex != -1) {
 						currPage = url.substring(pageIndex + 5);
 						i = currPage.indexOf("&");
 						if (i != -1)
 							currPage = currPage.replace(currPage.substring(i), "");
 					}
 					else {
 						currPage = "0";
 					}
 
 					int currPageNum = Integer.parseInt(currPage);
 					
 					Element nextPageElem = null;
 					
 					if (!pRes.getElementsContainingOwnText("Next Page").isEmpty())
 						nextPageElem = pRes.getElementsContainingOwnText("Next Page").first();
 					
 					pageCount = "page count not available";
 					if (nextPageElem != null) {
 						nextPage = "/search/index.html?game=" + searchQuery + "&page=" + (currPageNum + 1);
 					}
 					if (currPageNum > 0) {
 						prevPage = "/search/index.html?game=" + searchQuery + "&page=" + (currPageNum - 1);
 						firstPage = "/search/index.html?game=" + searchQuery + "&page=0";
 					}
 					
 					headerTitle = "Searching games: " + URLDecoder.decode(searchQuery) + "";
 					
 					updateHeader(headerTitle, firstPage, prevPage, Integer.toString(currPageNum + 1), 
 								 pageCount, nextPage, lastPage, NetDesc.GAME_SEARCH);
 					
 					searchIcon.setVisible(true);
 					
 					Elements gameSearchTables = pRes.getElementsByTag("table");
 					int tCount = gameSearchTables.size();
 					int tCounter = 0;
 					if (!gameSearchTables.isEmpty()) {
 						for (Element table : gameSearchTables) {
 							tCounter++;
 							if (tCounter < tCount)
 								content.addView(new HeaderView(this, "Best Matches"));
 							else
 								content.addView(new HeaderView(this, "Good Matches"));
 							
 							String prevPlatform = "";
 							
 							wtl("board row parsing start");
 							for (Element row : table.getElementsByTag("tr")) {
 								Elements cells = row.getElementsByTag("td");
 								// cells = [platform] [title] [faqs] [codes] [saves] [revs] [mygames] [q&a] [pics] [vids] [board]
 								String platform = cells.get(0).text();
 								String bName = cells.get(1).text();
 								String bUrl = cells.get(10).child(0).attr("href");
 								
 								if (platform.codePointAt(0) == ('\u00A0')) {
 									platform = prevPlatform;
 								}
 								else {
 									prevPlatform = platform;
 								}
 								
 								BoardView bv = new BoardView(this, platform, bName, bUrl);
 								bv.setOnClickListener(cl);
 								content.addView(bv);
 							}
 							
 							wtl("board row parsing end");
 						}
 					}
 					else {
 						content.addView(new HeaderView(this, "No results."));
 					}
 					
 					wtl("game search response block finished");
 					break;
 					
 				default:
 					wtl("GRAIO hNR determined response type is unhandled");
 					//aBar.setTitle("Page unhandled - " + res.url().toString());
 					title.setText("Page unhandled - " + res.url().toString());
 					break;
 				}
 				
 				adView = new WebView(this);
 		        adView.getSettings().setJavaScriptEnabled(settings.getBoolean("enableJS", true));
 		        
 				content.addView(adView);
 				
 				String bgcolor;
 				if (usingLightTheme)
 		        	bgcolor = "#ffffff";
 				else
 					bgcolor = "#000000";
 				
 				StringBuilder adBuilder = new StringBuilder();
 				adBuilder.append("<html><body bgcolor=\"" + bgcolor + "\">");
 				for (Element e : pRes.getElementsByClass("ad")) {
 					adBuilder.append(e.outerHtml());
 				}
 				adBuilder.append("</body></html>");
 				wtl(adBuilder.toString());
 				adView.loadDataWithBaseURL(session.getLastPath(), adBuilder.toString(), "text/html", "iso-8859-1", null);
 
 				if (settings.getBoolean("enablePTR", false))
 					contentPTR.setMode(Mode.BOTH);
 				
 				wtl("applysavedscroll: " + Session.applySavedScroll + ". savedscrollval: " + Session.savedScrollVal);
 				if (Session.applySavedScroll) {
 					contentPTR.post(new Runnable() {
 				        @Override
 				        public void run() {
 				        	contentPTR.getRefreshableView().scrollTo(0, Session.savedScrollVal);
 				        }
 				    });
 					Session.applySavedScroll = false;
 				}
 				
 			}
 			else {
 				wtl("res is null");
 				content.addView(new HeaderView(this, "You broke it. Somehow processContent was called with a null response."));
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			tryCaught(res.url().toString(), ExceptionUtils.getStackTrace(e), res.body());
 			if (session.canGoBack())
 				session.goBack(false);
 		}
 		catch (StackOverflowError e) {
 			e.printStackTrace();
 			tryCaught(res.url().toString(), ExceptionUtils.getStackTrace(e), res.body());
 			if (session.canGoBack())
 				session.goBack(false);
 		}
 		
 		if (contentPTR.isRefreshing())
 			contentPTR.onRefreshComplete();
 			
 		wtl("GRAIO hNR finishing");
 	}
 	
 	/***********************************
 	 * END HNR
 	 * *********************************/
 
 	private void processBoards(Document pRes, boolean includeBoardCategories) {
 		Elements homeTables = pRes.getElementsByTag("table");
 		
 		boolean skippedFirst = false;
 		for (Element row : homeTables.first().getElementsByTag("tr")) {
 			if (skippedFirst) {
 				if (row.hasClass("head")) {
 					content.addView(new HeaderView(this, row.text()));
 				}
 				else {
 					// [title + link] [topics] [msgs] [last post]
 					Elements cells = row.children();
 					
 					String title = cells.get(0).children().first().text();
 					
 					String boardDesc = null;
 					if (!cells.get(0).textNodes().isEmpty())
 						boardDesc = cells.get(0).textNodes().get(0).text();
 					
 					String link = cells.get(0).children().first().attr("href");
 					
 					String tCount = null;
 					String mCount = null;
 					String lPost = null;
 					
 					BoardViewType bvt;
 					
 					if (cells.size() > 3) {
 						tCount = cells.get(1).text();
 						mCount = cells.get(2).text();
 						lPost = cells.get(3).text();
 						
 						bvt = BoardViewType.NORMAL;
 					}
 					else
 						bvt = BoardViewType.SPLIT;
 					
 					BoardView board = new BoardView(this, title, boardDesc, lPost, tCount, mCount, link, bvt);
 					board.setOnClickListener(cl);
 					
 					content.addView(board);
 				}
 			}
 			else {
 				skippedFirst = true;
 			}
 		}
 		
 		if (includeBoardCategories) {
 			int rowX = 0;
 			for (Element row : homeTables.get(1).getElementsByTag("tr")) {
 				rowX++;
 				if (rowX > 2) {
 					Element cell = row.children().get(0);
 					String title = cell.children().first().text();
 					String boardDesc = cell.textNodes().get(0).text();
 					String link = cell.children().first().attr("href");
 					BoardView board = new BoardView(this, title, boardDesc,
 							null, null, null, link, BoardViewType.LIST);
 					board.setOnClickListener(cl);
 					content.addView(board);
 				} else {
 					if (rowX == 1) {
 						content.addView(new HeaderView(this,
 								"Message Board Categories"));
 					}
 				}
 			}
 		}
 	}
 	
 	private int[] getPageNums(Document pRes, Element pj, boolean usingUserPanel) {
 		int[] nums = {1, 1};
 		
 		if (usingUserPanel)
 			pj = pRes.getElementsByClass("u_pagenav").first();
 		else
 			pj = pRes.getElementsByClass("pages").first();
 		
 		if (pj != null) {
 			String pjText = pj.text();
 			//Page 1 of 3 | Next | Last
 			//First | Page 2 of 3 | Last
 			//First | Previous | Page 3 of 3
 			int ofIndex = pjText.indexOf(" of ");
 			int currPageStart = pjText.indexOf(" | Page ");
 			int currOffset = 8;
 			
 			if (currPageStart == -1) {
 				currPageStart = pjText.indexOf("Page ");
 				currOffset = 5;
 			}
 			
 			String currPage = pjText.substring(currPageStart + currOffset, ofIndex);
 			
 			int pageCountEnd = pjText.indexOf(' ', ofIndex + 4);
 			if (pageCountEnd == -1)
 				pageCountEnd = pjText.length();
 			String pageCount = pjText.substring(ofIndex + 4, pageCountEnd);
 			pageCount.trim();
 			nums[0] = Integer.parseInt(currPage);
 			nums[1] = Integer.parseInt(pageCount);
 		}
 		
 		return nums;
 	}
 	
 	private void updatePostingRights(Document pRes, boolean onTopic, boolean usingUserPanel) {
 		if (onTopic) {
 			if (usingUserPanel) {
 				if (pRes.getElementsByClass("links").first().text().contains("Post new message")) {
 					postIcon.setVisible(true);
 					pMode = PostMode.ON_TOPIC;
 				}
 			}
 			else {
 				if (pRes.getElementsByClass("user").first().text().contains("Post New Message")) {
 					postIcon.setVisible(true);
 					pMode = PostMode.ON_TOPIC;
 				}
 			}
 		}
 		else {
 			if (usingUserPanel) {
 				if (pRes.getElementsByClass("links").first().text().contains("Post a new topic")) {
 					postIcon.setVisible(true);
 					pMode = PostMode.ON_BOARD;
 				}
 			}
 			else {
 				if (pRes.getElementsByClass("user").first().text().contains("New Topic")) {
 					postIcon.setVisible(true);
 					pMode = PostMode.ON_BOARD;
 				}
 			}
 		}
 	}
 	
 	public void postExecuteCleanup(NetDesc desc) {
 		wtl("GRAIO dPostEC --NEL, desc: " + desc.name());
 		addonSherlock().setProgressBarIndeterminateVisibility(false);
 		if (refreshIcon != null)
 			refreshIcon.setVisible(true);
 		if (desc == NetDesc.BOARD || desc == NetDesc.TOPIC)
 			postCleanup();
 	}
 	
 	private boolean goToLastPost = false;
 	public class ClickListener implements View.OnClickListener, View.OnLongClickListener {
 		@Override
 		public void onClick(View v) {
 			if (BoardView.class.isInstance(v)) {
 				BoardView bv = (BoardView) v;
 				if (bv.getType() == BoardViewType.LIST) {
 					session.get(NetDesc.BOARD_LIST, bv.getUrl(), null);
 				}
 				else {
 					session.get(NetDesc.BOARD, bv.getUrl(), null);
 				}
 			}
 			else if (TopicView.class.isInstance(v)) {
 				goToLastPost = false;
 				session.get(NetDesc.TOPIC, ((TopicView)v).getUrl(), null);
 			}
 			else if (LastPostView.class.isInstance(v)) {
 				goToLastPost = true;
 				session.get(NetDesc.TOPIC, ((LastPostView)v).getUrl(), null);
 			}
 			else if (PMView.class.isInstance(v)) {
 				session.get(NetDesc.READ_PM, ((PMView)v).getUrl(), null);
 			}
 			else if (PMDetailView.class.isInstance(v)) {
 				pmSetup(((PMDetailView)v).getSender(), ((PMDetailView)v).getTitle(), "");
 			}
 		}
 
 		
 		@Override
 		public boolean onLongClick(View v) {
 			String url = ((TopicView)v).getUrl();
 			url = url.substring(0, url.lastIndexOf('/'));
 			session.get(NetDesc.BOARD, url, null);
 			return true;
 		}
 	}
 	
 	private void updateHeader(String titleIn, String firstPageIn, String prevPageIn, String currPage, 
 							  String pageCount, String nextPageIn, String lastPageIn, NetDesc desc) {
 		
 		title.setText(titleIn);
 		
 		if (currPage.equals("-1")) {
 			pageJumperWrapper.setVisibility(View.GONE);
 		}
 		else {
 			pageJumperWrapper.setVisibility(View.VISIBLE);
 			pageJumperDesc = desc;
 			pageLabel.setText(currPage + " / " + pageCount);
 			
 			if (firstPageIn != null) {
 				firstPageUrl = firstPageIn;
 				firstPage.setEnabled(true);
 			}
 			else {
 				firstPage.setEnabled(false);
 			}
 			
 			if (prevPageIn != null) {
 				prevPageUrl = prevPageIn;
 				prevPage.setEnabled(true);
 			}
 			else {
 				prevPage.setEnabled(false);
 			}
 			
 			if (nextPageIn != null) {
 				nextPageUrl = nextPageIn;
 				nextPage.setEnabled(true);
 			}
 			else {
 				nextPage.setEnabled(false);
 			}
 			
 			if (lastPageIn != null) {
 				lastPageUrl = lastPageIn;
 				lastPage.setEnabled(true);
 			}
 			else {
 				lastPage.setEnabled(false);
 			}
 		}
 	}
 	
 	private void updateHeaderNoJumper(String title, NetDesc desc) {
 		updateHeader(title, null, null, "-1", "-1", null, null, desc);
 	}
 	
 	private MessageView clickedMsg;
 	public void messageMenuClicked(MessageView msg) {
 		clickedMsg = msg;
 		showDialog(MESSAGE_ACTION_DIALOG);
 	}
 	
 	public void editPostSetup(String msg, String msgID) {
 		postBody.setText(msg);
 		messageID = msgID;
 		postOnTopicSetup();
 	}
 	
 	public void quoteSetup(String user, String msg) {
 		wtl("quoteSetup fired");
 		String cite = "<cite>" + user + " posted...</cite>\n";
 		String body = "<quote>" + msg + "</quote>\n\n";
 		
 		if (postWrapper.getVisibility() != View.VISIBLE) {
 			postBody.setText(cite + body);
 			postOnTopicSetup();
 		}
 		else {
 			postBody.append(cite + body);
 			postBody.setSelection(postBody.getText().length());
 		}
 		wtl("quoteSetup finishing");
 	}
 	
 	public void postOnTopicSetup() {
 		wtl("postOnTopicSetup fired --NEL");
 		titleWrapper.setVisibility(View.GONE);
 		postWrapper.setVisibility(View.VISIBLE);
 		postButton.setEnabled(true);
 		cancelButton.setEnabled(true);
 		postBody.requestFocus();
 		postBody.setSelection(postBody.getText().length());
 		postPostUrl = session.getLastPath();
 		if (postPostUrl.contains("#"))
 			postPostUrl = postPostUrl.substring(0, postPostUrl.indexOf('#'));
 	}
 	
 	public void postOnBoardSetup() {
 		wtl("postOnBoardSetup fired --NEL");
 		titleWrapper.setVisibility(View.VISIBLE);
 		postWrapper.setVisibility(View.VISIBLE);
 		postButton.setEnabled(true);
 		cancelButton.setEnabled(true);
 		postTitle.requestFocus();
 		postPostUrl = session.getLastPath();
 		if (postPostUrl.contains("#"))
 			postPostUrl = postPostUrl.substring(0, postPostUrl.indexOf('#'));
 	}
 	
 	public void postCancel(View view) {
 		wtl("postCancel fired --NEL");
 		postCleanup();
 	}
 	
 	public void postDo(View view) {
 		int escapedTitleLength = StringEscapeUtils.escapeHtml4(postTitle.getText().toString()).length();
 		int escapedBodyLength = StringEscapeUtils.escapeHtml4(postBody.getText().toString()).length();
 		
 		wtl("postDo fired");
 		if (titleWrapper.getVisibility() == View.VISIBLE) {
 			wtl("posting on a board");
 			// posting on a board
 			if (postTitle.length() > 4) {
 				if (escapedTitleLength < 81) {
 					if (postBody.length() > 0) {
 						if (escapedBodyLength < 4097) {
 							String path = "http://m.gamefaqs.com/boards/post.php?board=" + boardID;
 							int i = path.indexOf('-');
 							path = path.substring(0, i);
 							wtl("post path: " + path);
 							savedPostBody = postBody.getText().toString();
 							wtl("saved post body: " + savedPostBody);
 							savedPostTitle = postTitle.getText().toString();
 							wtl("saved post title: " + savedPostTitle);
 							wtl("sending topic");
 							postButton.setEnabled(false);
 							cancelButton.setEnabled(false);
 							if (userLevel < 30)
 								session.get(NetDesc.POSTTPC_S1, path, null);
 							else
 								session.get(NetDesc.QPOSTTPC_S1, path, null);
 						}
 						else {
 							wtl("post body is too long");
 							Toast.makeText(this, "Post body is too long.", Toast.LENGTH_SHORT).show();
 						}
 					}
 					else {
 						wtl("post body is empty");
 						Toast.makeText(this, "Post body can't be empty.", Toast.LENGTH_SHORT).show();
 					}
 				}
 				else {
 					wtl("post title is too long");
 					Toast.makeText(this, "Topic title length must be less than 80 characters.", Toast.LENGTH_SHORT).show();
 				}
 			}
 			else {
 				wtl("post title is too short");
 				Toast.makeText(this, "Topic title length must be greater than 4 displayed characters.", Toast.LENGTH_SHORT).show();
 			}
 		}
 		
 		else {
 			// posting on a topic
 			wtl("posting on a topic");
 			if (postBody.length() > 0) {
 				if (escapedBodyLength < 4097) {
 					String path = "http://m.gamefaqs.com/boards/post.php?board=" + boardID + "&topic=" + topicID;
 					if (messageID != null)
 						path += "&message=" + messageID;
 					
 					wtl("post path: " + path);
 					savedPostBody = postBody.getText().toString();
 					wtl("saved post body: " + savedPostBody);
 					wtl("sending post");
 					postButton.setEnabled(false);
 					cancelButton.setEnabled(false);
 					if (messageID != null)
 						session.get(NetDesc.QEDIT_MSG, path, null);
 					else if (userLevel < 30)
 						session.get(NetDesc.POSTMSG_S1, path, null);
 					else
 						session.get(NetDesc.QPOSTMSG_S1, path, null);
 				}
 				else {
 					wtl("post body is too long");
 					Toast.makeText(this, "Post body is too long.", Toast.LENGTH_SHORT).show();
 				}
 			}
 			else {
 				wtl("post body is empty");
 				Toast.makeText(this, "Post body can't be empty.", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 	
     private String reportCode;
     public String getReportCode() {return reportCode;}
     /** creates dialogs */
     @Override
     protected Dialog onCreateDialog(int id) {
     	Dialog dialog = null;
     	
     	switch (id) {
     	case CHANGE_LOGGED_IN_DIALOG:
     		dialog = createChangeLoggedInDialog();
     		break;
     		
     	case NEW_VERSION_DIALOG:
     		dialog = createNewVerDialog();
     		break;
     		
     	case SEND_PM_DIALOG:
     		dialog = createSendPMDialog();
     		break;
     		
     	case MESSAGE_ACTION_DIALOG:
     		AlertDialog.Builder msgActionBuilder = new AlertDialog.Builder(this);
 			msgActionBuilder.setTitle("Message Actions");
 			int arrayToUse;
 			if (Session.isLoggedIn()) {
 				if (Session.getUser().toLowerCase().equals(clickedMsg.getUser().toLowerCase())) {
 					if (userLevel < 30)
 						arrayToUse = R.array.msgMenuLoggedInAsPosterNotEditable;
 					else
 						arrayToUse = R.array.msgMenuLoggedInAsPosterEditable;
 				}
 				else {
 					arrayToUse = R.array.msgMenuLoggedIn;
 				}
 			}
 			else {
 				arrayToUse = R.array.msgMenuNotLoggedIn;
 			}
 			
 			final String[] options = getResources().getStringArray(arrayToUse);
 			msgActionBuilder.setItems(options, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					String selected = options[which];
 					if (selected.equals("Quote")) {
 						quoteSetup(clickedMsg.getUser(), clickedMsg.getMessageForQuoting());
 					}
 					else if (selected.equals("Edit")) {
 						editPostSetup(clickedMsg.getMessageForEditing(), clickedMsg.getMessageID());
 					}
 					else if (selected.equals("Delete")) {
 						session.get(NetDesc.DLTMSG_S1, clickedMsg.getMessageDetailLink(), null);
 					}
 					
 					else if (selected.equals("Report")) {
 						showDialog(REPORT_MESSAGE_DIALOG);
 					}
 					else if (selected.equals("User Details")) {
 						session.get(NetDesc.USER_DETAIL, clickedMsg.getUserDetailLink(), null);
 					}
 					else {
 						Toast.makeText(AllInOneV2.this, "not recognized: " + selected, Toast.LENGTH_SHORT).show();
 					}
 					
 					dialog.dismiss();
 				}
 			});
     		
 			msgActionBuilder.setNegativeButton("Cancel", new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 			
 			dialog = msgActionBuilder.create();
     		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
 				public void onDismiss(DialogInterface dialog) {
 					removeDialog(MESSAGE_ACTION_DIALOG);
 				}
 			});
     		
     		break;
     		
     	case REPORT_MESSAGE_DIALOG:
     		AlertDialog.Builder reportMsgBuilder = new AlertDialog.Builder(this);
     		reportMsgBuilder.setTitle("Report Message");
     		
     		final String[] reportOptions = getResources().getStringArray(R.array.msgReportReasons);
     		reportMsgBuilder.setItems(reportOptions, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					reportCode = getResources().getStringArray(R.array.msgReportCodes)[which];
 					session.get(NetDesc.MARKMSG_S1, clickedMsg.getMessageDetailLink(), null);
 					
 					dialog.dismiss();
 				}
 			});
     		
     		reportMsgBuilder.setNegativeButton("Cancel", new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 			
 			dialog = reportMsgBuilder.create();
     		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
 				public void onDismiss(DialogInterface dialog) {
 					removeDialog(REPORT_MESSAGE_DIALOG);
 				}
 			});
     		
     		break;
     		
     	}
     	
     	return dialog;
     }
 
     private LinearLayout pmSending;
 	private Dialog createSendPMDialog() {
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		LayoutInflater inflater = getLayoutInflater();
 		final View v = inflater.inflate(R.layout.sendpm, null);
 		b.setView(v);
 		b.setTitle("Send Private Message");
 		b.setCancelable(false);
 		
 		final EditText to = (EditText) v.findViewById(R.id.spTo);
 		final EditText subject = (EditText) v.findViewById(R.id.spSubject);
 		final EditText message = (EditText) v.findViewById(R.id.spMessage);
 		pmSending = (LinearLayout) v.findViewById(R.id.spFootWrapper);
 		
 		to.setText(savedTo);
 		subject.setText(savedSubject);
 		message.setText(savedMessage);
 		
 		b.setPositiveButton("Send", null);
 		b.setNegativeButton("Cancel", null);
 		
 		final AlertDialog d = b.create();
 		d.setOnShowListener(new OnShowListener() {
 			@Override
 			public void onShow(DialogInterface dialog) {
 				d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
 		            @Override
 		            public void onClick(View v) {
 						String toContent = to.getText().toString();
 						String subjectContent = subject.getText().toString();
 						String messageContent = message.getText().toString();
 						
 						if (!toContent.equals("")) {
 							if (!subjectContent.equals("")) {
 								if (!messageContent.equals("")) {
 									savedTo = toContent;
 									savedSubject = subjectContent;
 									savedMessage = messageContent;
 									
 									pmSending.setVisibility(View.VISIBLE);
 									
 									session.get(NetDesc.SEND_PM_S1, "http://m.gamefaqs.com/pm/new", null);
 									
 								}
 								else
 									Toast.makeText(AllInOneV2.this, "The message can't be empty.", Toast.LENGTH_SHORT).show();
 							}
 							else
 								Toast.makeText(AllInOneV2.this, "The subject can't be empty.", Toast.LENGTH_SHORT).show();
 						}
 						else
 							Toast.makeText(AllInOneV2.this, "The recepient can't be empty.", Toast.LENGTH_SHORT).show();
 					}
 		        });
 			}
 		});
   	
 		d.setOnDismissListener(new DialogInterface.OnDismissListener() {
 			public void onDismiss(DialogInterface dialog) {
 				pmSending = null;
 				removeDialog(SEND_PM_DIALOG);
 			}
 		});
 		return d;
 	}
 
 	private Dialog createNewVerDialog() {
 		AlertDialog.Builder newVersion = new AlertDialog.Builder(this);
 		newVersion.setTitle("New Version of GameRaven found!");
 		newVersion.setMessage("Would you like to go to the download page for the new version?");
 		newVersion.setPositiveButton("Yes", new OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.evernote.com/shard/s252/sh/b680bb2b-64a1-426d-a98d-6cbfb846a883/75eebb4db64c6e1769dd2d0ace487a88")));
 			}
 		});
 		newVersion.setNegativeButton("No", new OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dismissDialog(NEW_VERSION_DIALOG);
 			}
 		});
 		return newVersion.create();
 	}
 
 	private Dialog createChangeLoggedInDialog() {
 		AlertDialog.Builder accountChanger = new AlertDialog.Builder(this);
 		
 		String[] keys = accounts.getKeys();
 		
 		final String[] usernames = new String[keys.length + 1];
 		usernames[0] = "Log Out";
 		for (int i = 1; i < usernames.length; i++)
 			usernames[i] = keys[i - 1].toString();
 		
 		final String currUser = Session.getUser();
 		int selected = 0;
 		
 		for (int x = 1; x < usernames.length; x++)
 		{
 			if (usernames[x].equals(currUser)) 
 				selected = x;
 		}
 		
 		accountChanger.setTitle("Pick an Account");
 		accountChanger.setSingleChoiceItems(usernames, selected, new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int item) {
 		    	if (item == 0)
 		    		session = new Session(AllInOneV2.this);
 		    	
 		    	else
 		    	{
 			        String selUser = usernames[item].toString();
 			    	if (!selUser.equals(currUser))
 			        {
 			        	session = new Session(AllInOneV2.this, selUser, accounts.getString(selUser));
 			        }
 			    }
 
 		    	dismissDialog(CHANGE_LOGGED_IN_DIALOG);
 		    }
 		});
 		
 		accountChanger.setNegativeButton("Cancel", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		
 		
 		Dialog d = accountChanger.create();
 		d.setOnDismissListener(new DialogInterface.OnDismissListener() {
 			public void onDismiss(DialogInterface dialog) {
 				removeDialog(CHANGE_LOGGED_IN_DIALOG);
 			}
 		});
 		return d;
 	}
 	
 	public String savedTo, savedSubject, savedMessage;
     public void pmSetup(String toIn, String subjectIn, String messageIn) {
     	if (toIn != null && !toIn.equals("null"))
     		savedTo = toIn;
     	else
     		savedTo = "";
     	
     	if (subjectIn != null && !subjectIn.equals("null"))
     		savedSubject = subjectIn;
     	else
     		savedSubject = "";
     	
     	if (messageIn != null && !messageIn.equals("null"))
     		savedMessage = messageIn;
     	else
     		savedMessage = "";
     	
     	savedTo = URLDecoder.decode(savedTo);
     	savedSubject = URLDecoder.decode(savedSubject);
     	savedMessage = URLDecoder.decode(savedMessage);
     	
     	showDialog(SEND_PM_DIALOG);
     }
     
     public void pmCleanup(boolean wasSuccessful, String error) {
     	if (wasSuccessful) {
     		Toast.makeText(this, "PM sent.", Toast.LENGTH_SHORT).show();
         	dismissDialog(SEND_PM_DIALOG);
     	}
     	else {
     		Toast.makeText(this, error, Toast.LENGTH_LONG).show();
     		pmSending.setVisibility(View.GONE);
     	}
     }
 
 	
 	public void refreshClicked(View view) {
 		wtl("refreshClicked fired --NEL");
 		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
 		if (session.getLastPath() == null) {
 			if (Session.isLoggedIn())
 				session = new Session(this, Session.getUser(), accounts.getString(Session.getUser()));
 	    	else
 	    		session = new Session(this);
 		}
 		else
 			session.refresh();
 	}
 	
 	public String getSig() {
     	String sig = "";
     	
     	if (session != null) {
 			if (Session.isLoggedIn())
 				sig = settings.getString("customSig" + Session.getUser(), "");
 		}
     	
     	if (sig.equals(""))
     		sig = settings.getString("customSig", "");
     	
 		if (sig.equals(""))
     		sig = defaultSig;
 		
 		try {
 			sig = sig.replace("*grver*", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
 		} catch (NameNotFoundException e) {
 			sig = sig.replace("*grver*", "");
 			e.printStackTrace();
 		}
 		
 		return sig;
     }
 	
 	private static SimpleDateFormat timingFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS z", Locale.US);
     public void wtl(String msg) {
     	if (!isReleaseBuild) {
 			msg = msg.replaceAll("\\\\n", "(nl)");
 			msg = timingFormat.format(new Date()) + "// " + msg;
 			Log.d("logger", msg);
 		}
     }
     
 
 
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		String jumpingTo = navList[itemPosition];
 		if (jumpingTo.equals("Jump To"))
 			wtl("resetting nav jumper");
 		else if (jumpingTo.equals("Board Jumper"))
 			session.get(NetDesc.BOARD_JUMPER, "/boards", null);
 		else if (jumpingTo.equals("AMP List"))
 			session.get(NetDesc.AMP_LIST, buildAMPLink(), null);
 		else if (jumpingTo.equals("PM Inbox"))
 			session.get(NetDesc.PM_INBOX, "/pm/", null);
 		else
 			Toast.makeText(this, "jump id not recognized: " + jumpingTo, Toast.LENGTH_SHORT).show();
 		
 		aBar.setSelectedNavigationItem(0);
 		
 		return true;
 	}
 	
 	public void tryCaught(String url, String stacktrace, String source) {
 		final String emailMsg = "\n\nURL:\n" + url + "\n\nStack trace:\n" + stacktrace + "\n\nPage source:\n" + source;
 		
     	AlertDialog.Builder b = new AlertDialog.Builder(this);
     	b.setTitle("Error");
     	b.setMessage("You've run into a bug! Would you like to email debug information to the developer? The email will contain " + 
     				 "details on the crash itself, the url the server responded with, and the source for the page. " +
     				 "If so, please include a brief comment below on what you were trying to do.");
     	
     	final EditText input = new EditText(this);
     	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
     	        LinearLayout.LayoutParams.MATCH_PARENT,
     	        LinearLayout.LayoutParams.MATCH_PARENT);
     	input.setLayoutParams(lp);
     	input.setHint("Enter comment here...");
     	b.setView(input);
     	
     	b.setNegativeButton("Cancel", new Dialog.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
     	b.setPositiveButton("Email to dev", new Dialog.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Intent i = new Intent(Intent.ACTION_SEND);
 				i.setType("message/rfc822");
 				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"ioabsoftware@gmail.com"});
 				i.putExtra(Intent.EXTRA_SUBJECT, "GameRaven Error Report");
 				i.putExtra(Intent.EXTRA_TEXT   , "Comment:\n" + input.getText() + emailMsg);
 				try {
 				    startActivity(Intent.createChooser(i, "Send mail..."));
 				} catch (android.content.ActivityNotFoundException ex) {
 				    Toast.makeText(AllInOneV2.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
     	b.create().show();
     }
 	
 	private void getBoardID(String url) {
 		wtl("getBoardID fired");
 		// board example: http://m.gamefaqs.com/boards/400-current-events
 		String boardUrl = url.substring(29);
 		
 		int i = boardUrl.indexOf('/');
 		if (i != -1) {
 			String replacer = boardUrl.substring(i);
 			boardUrl = boardUrl.replace(replacer, "");
 		}
 		
 		i = boardUrl.indexOf('?');
 		if (i != -1) {
 			String replacer = boardUrl.substring(i);
 			boardUrl = boardUrl.replace(replacer, "");
 		}
 		i = boardUrl.indexOf('#');
 		if (i != -1) {
 			String replacer = boardUrl.substring(i);
 			boardUrl = boardUrl.replace(replacer, "");
 		}
 		
 		boardID = boardUrl;
 		wtl("boardID: " + boardID);
 		wtl("getBoardID finishing");
 	}
 	
 	private void getTopicID(String url) {
 		wtl("getTopicID fired");
 		// topic example: http://m.gamefaqs.com/boards/400-current-events/64300205
 		String topicUrl = url.substring(url.indexOf('/', 29) + 1);
 		int i = topicUrl.indexOf('/');
 		if (i != -1) {
 			String replacer = topicUrl.substring(i);
 			topicUrl = topicUrl.replace(replacer, "");
 		}
 		i = topicUrl.indexOf('?');
 		if (i != -1) {
 			String replacer = topicUrl.substring(i);
 			topicUrl = topicUrl.replace(replacer, "");
 		}
 		i = topicUrl.indexOf('#');
 		if (i != -1) {
 			String replacer = topicUrl.substring(i);
 			topicUrl = topicUrl.replace(replacer, "");
 		}
 		topicID = topicUrl;
 		wtl("topicID: " + topicID);
 		wtl("getTopicID finishing");
 	}
 	
 
     
     private int userLevel = 0;
     private void updateUserLevel(Document doc) {
    	String sc = doc.getElementsByTag("script").first().html();
     	int start = sc.indexOf("UserLevel','") + 12;
     	int end = sc.indexOf('\'', start + 1);
     	userLevel = Integer.parseInt(sc.substring(start, end));
     	
 		wtl("user level: " + userLevel);
     }
     
     @Override
 	public void onBackPressed() {
     	if (searchIcon.isActionViewExpanded()) {
     		searchIcon.collapseActionView();
     	}
     	else if (postWrapper.getVisibility() == View.VISIBLE) {
     		postCleanup();
     	}
     	else {
     	    goBack();
     	}
 	}
 
 	private void goBack() {
 		if (session.canGoBack()) {
 			wtl("back pressed, history exists, going back");
 			session.goBack(false);
 		}
 		else {
 			wtl("back pressed, no history, exiting app");
 			session = null;
 		    this.finish();
 		}
 	}
 	
 	public static String buildAMPLink() {
 		return "/boards/myposts.php?lp=" + settings.getString("ampSortOption", "-1");
 	}
 }
