 package com.ioabsoftware.gameraven.networking;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.jsoup.Connection.Method;
 import org.jsoup.Connection.Response;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.view.ViewGroup.LayoutParams;
 import android.webkit.WebView;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 import com.ioabsoftware.gameraven.AllInOneV2;
 import com.ioabsoftware.gameraven.db.History;
 import com.ioabsoftware.gameraven.db.HistoryDBHelper;
 
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 
 /**
  * Session is used to establish and maintain GFAQs sessions, and to send GET and POST requests.
  * @author Charles Rosaaen, Insanity On A Bun Software
  *
  */
 public class Session implements HandlesNetworkResult {
 	
 	/** The root of GFAQs. */
 	public static final String ROOT = "http://www.gamefaqs.com";
 	
 	/** Holds all cookies for the session */
 	private Map<String, String> cookies = new LinkedHashMap<String, String>();
 	
 	private String lastAttemptedPath = "not set";
 	public String getLastAttemptedPath()
 	{return lastAttemptedPath;}
 	
 	private NetDesc lastAttemptedDesc = NetDesc.UNSPECIFIED;
 	public NetDesc getLastAttemptedDesc()
 	{return lastAttemptedDesc;}
 	
 	/** The latest page, with excess get data. */
 	private String lastPath = null;
 	/** Get's the path of the latest page. */
 	public String getLastPath()
 	{return lastPath;}
 	/** Get's the path of the latest page, stripped of any GET data. */
 	public String getLastPathWithoutData() {
 		if (lastPath.contains("?"))
 			return lastPath.substring(0, lastPath.indexOf('?'));
 		else
 			return lastPath;
 	}
 	
 	/** The latest description. */
 	private NetDesc lastDesc = null;
 	/** Get's the description of the latest page. */
 	public NetDesc getLastDesc()
 	{return lastDesc;}
 	
 	/** The latest Document. */
 	private Document lastDoc = null;
 	/** Get's the Document from the latest page. */
 	public Document getLastDoc()
 	{return lastDoc;}
 	
 	/** The name of the user for this session. */
 	private static String user = null;
 	/** Get's the name of the session user. */
 	public static String getUser()
 	{return user;}
 	
 	public static boolean isLoggedIn()
 	{return user != null;}
 
 	private static int userLevel = 0;
 //	public static int getUserLevel()
 //	{return userLevel;}
 	public static boolean userCanDeleteClose()
 	{return userLevel > 13;}
 	public static boolean userCanViewAMP() 
 	{return userLevel > 14;}
 	public static boolean userCanMarkMsgs()
 	{return userLevel > 19;}
 	public static boolean userCanEditMsgs()
 	{return userLevel > 24;}
 	/** Quickpost and create poll topics */
 	public static boolean userHasAdvancedPosting()
 	{return userLevel > 29;}
 	
 	public static boolean applySavedScroll;
 	public static int[] savedScrollVal;
 	
 	/** The password of the user for this session.  */
 	private String password = null;
 	
 	/** The current activity. */
 	private AllInOneV2 aio;
 
     private boolean addToHistory = true;
 	private HistoryDBHelper historyDB;
 	
 	private String initUrl = null;
 	private NetDesc initDesc = null;
     
 	
 	/**********************************************
 	 * START METHODS
 	 **********************************************/
 	
 	/**
 	 * Create a new session with no user logged in
 	 * that starts at the homepage.
 	 */
 	public Session(AllInOneV2 aioIn)
 	{
 		finalConstructor(aioIn, null, null);
 	}
 	
 	/**
 	 * Construct a new session for the specified user, 
 	 * using the specified password, that redirects to
 	 * the GFAQs homepage.
 	 * @param userIn The user for this session.
 	 * @param passwordIn The password for this session.
 	 */
 	public Session(AllInOneV2 aioIn, String userIn, String passwordIn)
 	{
 		finalConstructor(aioIn, userIn, passwordIn);
 	}
 	
 	/**
 	 * Construct a new session for the specified user, 
 	 * using the specified password, that redirects to
 	 * the GFAQs homepage.
 	 * @param userIn The user for this session.
 	 * @param passwordIn The password for this session.
 	 * @param initUrlIn The URL to load once successfully logged in.
 	 * @param initDescIn The desc to use once successfully logged in.
 	 */
 	public Session(AllInOneV2 aioIn, String userIn, String passwordIn, String initUrlIn, NetDesc initDescIn)
 	{
 		initUrl = initUrlIn;
 		initDesc = initDescIn;
 		finalConstructor(aioIn, userIn, passwordIn);
 	}
 	
 	/**
 	 * Final construction method.
 	 * @param activity The current activity
 	 * @param userIn Username, or null if no user
 	 * @param passwordIn Password, or null if no user
 	 */
 	private void finalConstructor(AllInOneV2 aioIn, String userIn, String passwordIn)
 	{
 		aio = aioIn;
 		aio.wtl("NEW SESSION");
 		aio.disableNavList();
 		
 		netManager = (ConnectivityManager) aio.getSystemService(Context.CONNECTIVITY_SERVICE);
 		
 		historyDB = new HistoryDBHelper(aio);
 		historyDB.clearTable();
         
         user = userIn;
 		password = passwordIn;
 		
 		// reset the Session unread PM and TT counters
 		AllInOneV2.getSettingsPref().edit().putInt("unreadPMCount", 0).apply();
 		AllInOneV2.getSettingsPref().edit().putInt("unreadTTCount", 0).apply();
 		
 		if (user == null) {
 			aio.wtl("session constructor, user is null, starting logged out session");
 			get(NetDesc.BOARD_JUMPER, ROOT + "/boards", null);
 			aio.setLoginName("Logged Out");
 		}
 		else {
 			aio.wtl("session constructor, user is not null, starting logged in session");
 			get(NetDesc.LOGIN_S1, ROOT + "/boards", null);
 			aio.setLoginName(user);
 		}
 	}
 	
 	/**
 	 * Builds a URL based on path.
 	 * @param path The path to build a URL off of. Can
 	 * be relative or absolute. If relative, can start
 	 * with a forward slash or not.
 	 * @return The correct absolute URL for the specified
 	 * path.
 	 */
 	private String buildURL(String path)
 	{
 		// path is absolute, return it
 		if (path.startsWith("http"))
 			return path;
 		
 		// add a forward slash to path if needed
 		if (!path.startsWith("/"))
 			path = '/' + path;
 		
 		// return absolute path
 		return ROOT + path;
 	}
 
 	private ConnectivityManager netManager;
 	public boolean hasNetworkConnection() {
 		NetworkInfo netInfo = netManager.getActiveNetworkInfo();
 		return netInfo != null && netInfo.isConnected();
 	}
 	
 	/**
 	 * Sends a GET request to a specified page.
 	 * @param caller The HandlesNetworkResult making this call.
 	 * @param desc Description of this request, to properly handle the response later.
 	 * @param path The path to send the request to.
 	 * @param data The extra data to send along, pass null if no extra data.
 	 */
 	public void get(NetDesc desc, String path, Map<String, String> data) {
 		if (hasNetworkConnection()) {
 			lastAttemptedPath = path;
 			lastAttemptedDesc = desc;
 			new NetworkTask(this, desc, Method.GET, cookies, buildURL(path), data).execute();
 		}
 		else
 			aio.noNetworkConnection();
 	}
 	
 	/**
 	 * Sends a POST request to a specified page.
 	 * @param caller The HandlesNetworkResult making this call.
 	 * @param desc Description of this request, to properly handle the response later.
 	 * @param path The path to send the request to.
 	 * @param data The extra data to send along.
 	 */
 	public void post(NetDesc desc, String path, Map<String, String> data) {
 		if (hasNetworkConnection()) {
 			new NetworkTask(this, desc, Method.POST, cookies, buildURL(path), data).execute();
 		}
 		else
 			aio.noNetworkConnection();
 	}
 	
 	public void addCookies(Map<String, String> newCookies)
 	{
 		cookies.putAll(newCookies);
 	}
 
 	@Override
 	public void preExecuteSetup(NetDesc desc) {
 		switch (desc) {
 		case AMP_LIST:
 		case TRACKED_TOPICS:
 		case BOARD:
 		case BOARD_JUMPER:
 		case TOPIC:
 		case GAME_SEARCH:
 		case BOARD_LIST:
 		case MESSAGE_DETAIL:
 		case USER_DETAIL:
 		case PM_INBOX:
 		case PM_DETAIL:
 		case MARKMSG_S1:
 		case CLOSE_TOPIC:
 		case DLTMSG_S1:
 		case LOGIN_S1:
 		case QEDIT_MSG:
 		case POSTMSG_S1:
 		case POSTTPC_S1:
 		case QPOSTMSG_S1:
 		case QPOSTTPC_S1:
 		case UNSPECIFIED:
 			aio.preExecuteSetup(desc);
 			break;
 
 		case LOGIN_S2:
 		case MARKMSG_S2:
 		case DLTMSG_S2:
 		case QPOSTMSG_S3:
 		case QPOSTTPC_S3:
 		case POSTMSG_S2:
 		case POSTMSG_S3:
 		case POSTTPC_S2:
 		case POSTTPC_S3:
 		case VERIFY_ACCOUNT_S1:
 		case VERIFY_ACCOUNT_S2:
 		case SEND_PM_S1:
 		case SEND_PM_S2:
 			break;
 		}
 	}
 
 	@Override
 	public void handleNetworkResult(Response res, NetDesc desc) {
 		aio.wtl("session hNR fired, desc: " + desc.name());
 		try {
 			aio.wtl("checking if res is null or empty");
 			if (res != null && !res.body().isEmpty()) {
 				
 				if (res.body().startsWith("internal_error")) {
 					aio.genError("Internal Server Error", res.body());
 					return;
 				}
 
 				aio.wtl("parsing res");
 				Document cleanDoc = res.parse();
 
 				aio.wtl("cloning pRes");
 				Document clonedDoc = cleanDoc.clone();
 				String resUrl = res.url().toString();
 
 				aio.wtl("checking if res does not start with root");
 				if (!resUrl.startsWith(ROOT)) {
 					AlertDialog.Builder b = new AlertDialog.Builder(aio);
 					b.setTitle("Redirected");
 					b.setMessage("The request was redirected somewhere away from GameFAQs. " +
 							"This usually happens if you're connected to a network that requires a login, " +
 							"such as a  paid-for wifi service. Click below to open the page in your browser.");
 					
 					final String path = resUrl;
 					b.setPositiveButton("Open Page In Browser", new OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
 							aio.startActivity(browserIntent);
 							aio.finish();
 						}
 					});
 					
 					b.create().show();
 					return;
 				}
 
 				aio.wtl("checking if pRes contains captcha");
 				if (!clonedDoc.select("header.page_header:contains(CAPTCHA)").isEmpty()) {
 					
 					String captcha = clonedDoc.select("iframe").outerHtml();
 					final String key = clonedDoc.getElementsByAttributeValue("name", "key").attr("value");
 					
 					AlertDialog.Builder b = new AlertDialog.Builder(aio);
 					b.setTitle("CAPTCHA Required");
 					
 					LinearLayout wrapper = new LinearLayout(aio);
 					wrapper.setOrientation(LinearLayout.VERTICAL);
 					
 					WebView web = new WebView(aio);
 					web.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 					web.loadDataWithBaseURL(resUrl, 
 							"<p>There have been multiple unsuccessful login attempts!</p>" + captcha, 
 							"text/html", 
 							null, null);
 					wrapper.addView(web);
 					
 					final EditText form = new EditText(aio);
 					form.setHint("Enter confirmation code (NOT CAPTCHA!!!) here");
 					wrapper.addView(form);
 					
 					b.setView(wrapper);
 					
 					b.setPositiveButton("Login", new OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							HashMap<String, String> loginData = new HashMap<String, String>();
 							// "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
 							loginData.put("EMAILADDR", user);
 							loginData.put("PASSWORD", password);
 							loginData.put("path", ROOT);
 							loginData.put("key", key);
 							loginData.put("recaptcha_challenge_field", form.getText().toString());
 							loginData.put("recaptcha_response_field", "manual_challenge");
 							
 							post(NetDesc.LOGIN_S2, "/user/login_captcha.html", loginData);
 						}
 					});
 					
 					b.create().show();
 					return;
 				}
 				
 				aio.wtl("status: " + res.statusCode() + ", " + res.statusMessage());
 				Element err = clonedDoc.select("h1.page-title").first();
 				if (err != null && err.text().contains("Error")) {
 					if (err.text().contains("404 Error")) {
 						aio.wtl("status code 404");
 						Elements paragraphs = clonedDoc.getElementsByTag("p");
 						aio.genError("404 Error", paragraphs.get(1).text() + "\n\n"
 								+ paragraphs.get(2).text());
 						return;
 					}
 					else if (err.text().contains("403 Error")) {
 						aio.wtl("status code 403");
 						Elements paragraphs = clonedDoc.getElementsByTag("p");
 						aio.genError("403 Error", paragraphs.get(1).text() + "\n\n"
 								+ paragraphs.get(2).text());
 						return;
 					}
 					else if (err.text().contains("401 Error")) {
 						aio.wtl("status code 401");
 						if (lastDesc == NetDesc.LOGIN_S2) {
 							skipAIOCleanup = true;
 							get(NetDesc.BOARD_JUMPER, "/boards", null);
 						} else {
 							Elements paragraphs = clonedDoc.getElementsByTag("p");
 							aio.genError("401 Error", paragraphs.get(1).text()
 									+ "\n\n" + paragraphs.get(2).text());
 						}
 						return;
 					}
 				}
 				
 				if (clonedDoc.title().equals("GameFAQs - 503 - Temporarily Unavailable")) {
 					aio.genError("503 Error", "GameFAQs is experiencing some temporary difficulties with " +
 							"the site. Probably because of something they did. Please wait a few " +
 							"seconds before refreshing this page to try again.");
 					
 					return;
 				}
 				
 				updateUserLevel(clonedDoc);
 				
 				switch (desc) {
 				case AMP_LIST:
 				case TRACKED_TOPICS:
 				case BOARD:
 				case BOARD_JUMPER:
 				case TOPIC:
 				case GAME_SEARCH:
 				case BOARD_LIST:
 				case MESSAGE_DETAIL:
 				case USER_DETAIL:
 				case PM_INBOX:
 				case PM_DETAIL:
 				case UNSPECIFIED:
 				case LOGIN_S1:
 				case LOGIN_S2:
 				case DLTMSG_S1:
 				case DLTMSG_S2:
 				case QEDIT_MSG:
 				case QPOSTMSG_S1:
 				case QPOSTMSG_S3:
 				case QPOSTTPC_S1:
 				case QPOSTTPC_S3:
 				case POSTMSG_S1:
 				case POSTMSG_S2:
 				case POSTMSG_S3:
 				case POSTTPC_S1:
 				case POSTTPC_S2:
 				case POSTTPC_S3:
 				case VERIFY_ACCOUNT_S1:
 				case VERIFY_ACCOUNT_S2:
 					aio.wtl("addToHistory = true");
 					break;
 
 				case MARKMSG_S1:
 				case MARKMSG_S2:
 				case CLOSE_TOPIC:
 				case SEND_PM_S1:
 				case SEND_PM_S2:
 					aio.wtl("addToHistory = false");
 					addToHistory = false;
 					break;
 				}
 				
 				if (addToHistory) {
 					if (lastPath != null) {
 						switch (lastDesc) {
 						case AMP_LIST:
 						case TRACKED_TOPICS:
 						case BOARD:
 						case BOARD_JUMPER:
 						case TOPIC:
 						case GAME_SEARCH:
 						case BOARD_LIST:
 						case MESSAGE_DETAIL:
 						case USER_DETAIL:
 						case PM_INBOX:
 						case PM_DETAIL:
 						case UNSPECIFIED:
 							aio.wtl("beginning history addition");
 							int[] vLoc = aio.getScrollerVertLoc();
 							historyDB.insertHistory(lastPath, lastDesc.name(), lastDoc.outerHtml(), vLoc[0], vLoc[1]);
 							aio.wtl("finished history addition");
 							break;
 							
 						case MARKMSG_S1:
 						case MARKMSG_S2:
 						case CLOSE_TOPIC:
 						case DLTMSG_S1:
 						case DLTMSG_S2:
 						case LOGIN_S1:
 						case LOGIN_S2:
 						case QEDIT_MSG:
 						case QPOSTMSG_S1:
 						case QPOSTMSG_S3:
 						case QPOSTTPC_S1:
 						case QPOSTTPC_S3:
 						case POSTMSG_S1:
 						case POSTMSG_S2:
 						case POSTMSG_S3:
 						case POSTTPC_S1:
 						case POSTTPC_S2:
 						case POSTTPC_S3:
 						case VERIFY_ACCOUNT_S1:
 						case VERIFY_ACCOUNT_S2:
 						case SEND_PM_S1:
 						case SEND_PM_S2:
 							aio.wtl("not adding to history");
 							break;
 						
 						}
 					}
 				}
 				
 				switch (desc) {
 				case AMP_LIST:
 				case TRACKED_TOPICS:
 				case BOARD:
 				case BOARD_JUMPER:
 				case TOPIC:
 				case GAME_SEARCH:
 				case BOARD_LIST:
 				case MESSAGE_DETAIL:
 				case USER_DETAIL:
 				case PM_INBOX:
 				case PM_DETAIL:
 				case UNSPECIFIED:
 				case LOGIN_S1:
 				case LOGIN_S2:
 				case QEDIT_MSG:
 				case QPOSTMSG_S1:
 				case QPOSTMSG_S3:
 				case QPOSTTPC_S1:
 				case QPOSTTPC_S3:
 				case POSTMSG_S1:
 				case POSTMSG_S2:
 				case POSTMSG_S3:
 				case POSTTPC_S1:
 				case POSTTPC_S2:
 				case POSTTPC_S3:
 				case VERIFY_ACCOUNT_S1:
 				case VERIFY_ACCOUNT_S2:
 					aio.wtl("beginning lastDesc, lastRes, etc. setting");
 					lastDesc = desc;
 					lastDoc = cleanDoc;
 					lastPath = resUrl;
 					aio.wtl("finishing lastDesc, lastRes, etc. setting");
 					break;
 					
 
 				case MARKMSG_S1:
 				case MARKMSG_S2:
 				case CLOSE_TOPIC:
 				case DLTMSG_S1:
 				case DLTMSG_S2:
 				case SEND_PM_S1:
 				case SEND_PM_S2:
 					aio.wtl("not setting lastDesc, lastRes, etc.");
 					break;
 				
 				}
 				
 				// reset history flag
 				addToHistory = true;
 
 				aio.wtl("attempting cookie addition");
 				try {
 					cookies.putAll(res.cookies());
 					aio.wtl("cookies added");
 				} catch (Exception e) {
 					aio.wtl("cookie addition failed");
 				}
 				switch (desc) {
 				case LOGIN_S1:
 					aio.wtl("session hNR determined this is login step 1");
 					String loginKey = clonedDoc.getElementsByAttributeValue("name", "key").attr("value");
 					
 					HashMap<String, String> loginData = new HashMap<String, String>();
 					// "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
 					loginData.put("EMAILADDR", user);
 					loginData.put("PASSWORD", password);
 					loginData.put("path", lastPath);
 					loginData.put("key", loginKey);
 
 					aio.wtl("finishing login step 1, sending step 2");
 					post(NetDesc.LOGIN_S2, "/user/login.html", loginData);
 					break;
 					
 				case LOGIN_S2:
 					aio.wtl("session hNR determined this is login step 2");
 					aio.setAMPLinkVisible(userCanViewAMP());
 					
 					if (initUrl != null) {
 						aio.wtl("loading previous page");
 						get(initDesc, initUrl, null);
 					}
 					else if (userCanViewAMP() && AllInOneV2.getSettingsPref().getBoolean("startAtAMP", false)) {
 						aio.wtl("loading AMP");
 						get(NetDesc.AMP_LIST, AllInOneV2.buildAMPLink(), null);
 					}
 					else {
 						aio.wtl("loading board jumper");
 						get(NetDesc.BOARD_JUMPER, "/boards", null);
 					}
 					
 					break;
 					
 				case POSTMSG_S1:
 				case QPOSTMSG_S1:
 				case QEDIT_MSG:
 					
 					//TODO: Completely replace QPOST* NetDescs with userHasAdvancedPosting()
 					
 					aio.wtl("session hNR determined this is post message step 1");
 					String msg1Key = clonedDoc.getElementsByAttributeValue("name", "key").attr("value");
 					
 					String sig;
 					if (desc == NetDesc.QEDIT_MSG)
 						sig = AllInOneV2.EMPTY_STRING;
 					else
 						sig = aio.getSig();
 					
 					HashMap<String, String> msg1Data = new HashMap<String, String>();
 					msg1Data.put("messagetext", aio.getSavedPostBody());
 					msg1Data.put("custom_sig", sig);
 					msg1Data.put("post", (userHasAdvancedPosting() ? "Post without Preview" : "Preview Message"));
 					msg1Data.put("key", msg1Key);
 					
 					Elements msg1Error = clonedDoc.getElementsContainingOwnText("There was an error posting your message:");
 					if (!msg1Error.isEmpty()) {
 						aio.wtl("there was an error in post msg step 1, ending early");
 						aio.postError(msg1Error.first().parent().parent().text());
 						aio.postExecuteCleanup(desc);
 					}
 					else {
 						aio.wtl("finishing post message step 1, sending step 2");
 						if (userHasAdvancedPosting())
 							post(NetDesc.QPOSTMSG_S3, lastPath, msg1Data);
 						else
 							post(NetDesc.POSTMSG_S2, lastPath, msg1Data);
 					}
 					break;
 					
 				case POSTMSG_S2:
 					aio.wtl("session hNR determined this is post message step 2");
 					String msg2Key = clonedDoc.getElementsByAttributeValue("name", "key").attr("value");
 					String msgPost_id = clonedDoc.getElementsByAttributeValue("name", "post_id").attr("value");
 					String msgUid = clonedDoc.getElementsByAttributeValue("name", "uid").attr("value");
 					
 					HashMap<String, String> msg2Data = new HashMap<String, String>();
 					msg2Data.put("post", "Post Message");
 					msg2Data.put("key", msg2Key);
 					msg2Data.put("post_id", msgPost_id);
 					msg2Data.put("uid", msgUid);
 					
 					Elements msg2Error = clonedDoc.getElementsContainingOwnText("There was an error posting your message:");
 					Elements msg2AutoFlag = clonedDoc.getElementsContainingOwnText("There were one or more potential problems with your message:");
 					if (!msg2Error.isEmpty()) {
 						aio.wtl("there was an error in post msg step 2, ending early");
 						aio.postError(msg2Error.first().parent().parent().text());
 						aio.postExecuteCleanup(desc);
 					}
 					else if (!msg2AutoFlag.isEmpty()) {
 						aio.wtl("autoflag got tripped in post msg step 2, showing autoflag dialog");
 						String msg = msg2AutoFlag.first().parent().parent().text();
 						showAutoFlagWarning(lastPath, msg2Data, NetDesc.POSTMSG_S3, msg);
 					}
 					else {
 						aio.wtl("finishing post message step 2, sending step 3");
 						post(NetDesc.POSTMSG_S3, lastPath, msg2Data);
 					}
 					break;
 					
 				case POSTMSG_S3:
 				case QPOSTMSG_S3:
 					aio.wtl("session hNR determined this is post message step 3 (if jumping from 1 to 3, then app is quick posting)");
 					aio.wtl("finishing post message step 3, sending step 4");
 
 					Elements msg3AutoFlag = clonedDoc.getElementsContainingOwnText("There were one or more potential problems with your message:");
 					Elements msg3Error = clonedDoc.getElementsContainingOwnText("There was an error posting your message:");
 					if (!msg3Error.isEmpty()) {
 						aio.wtl("there was an error in post msg step 3, ending early");
 						aio.postError(msg3Error.first().parent().parent().text());
 						aio.postExecuteCleanup(desc);
 					}
 					else if (!msg3AutoFlag.isEmpty()) {
 						aio.wtl("autoflag got tripped in post msg step 3, getting data and showing autoflag dialog");
 						String msg = msg3AutoFlag.first().parent().parent().text();
 						
 						String msg3Key = clonedDoc.getElementsByAttributeValue("name", "key").attr("value");
 						String msg3Post_id = clonedDoc.getElementsByAttributeValue("name", "post_id").attr("value");
 						String msg3Uid = clonedDoc.getElementsByAttributeValue("name", "uid").attr("value");
 						
 						HashMap<String, String> msg3Data = new HashMap<String, String>();
 						msg3Data.put("post", "Post Message");
 						msg3Data.put("key", msg3Key);
 						msg3Data.put("post_id", msg3Post_id);
 						msg3Data.put("uid", msg3Uid);
 						
 						showAutoFlagWarning(lastPath, msg3Data, NetDesc.POSTMSG_S3, msg);
 					}
 					else {
 						goBack(true);
 					}
 					break;
 					
 				case POSTTPC_S1:
 				case QPOSTTPC_S1:
 					aio.wtl("session hNR determined this is post topic step 1");
 					String tpc1Key = clonedDoc.getElementsByAttributeValue("name", "key").attr("value");
 					
 					HashMap<String, String> tpc1Data = new HashMap<String, String>();
 					tpc1Data.put("topictitle", aio.getSavedPostTitle());
 					tpc1Data.put("messagetext", aio.getSavedPostBody());
 					tpc1Data.put("custom_sig", aio.getSig());
 					tpc1Data.put("post", ((desc == NetDesc.POSTTPC_S1) ? "Preview Message" : "Post without Preview"));
 					tpc1Data.put("key", tpc1Key);
 					
 					if (aio.isUsingPoll()) {
 						tpc1Data.put("poll_text", aio.getPollTitle());
 						for (int x = 0; x < 10; x++) {
 							if (aio.getPollOptions()[x].length() != 0)
 								tpc1Data.put("poll_option_" + (x + 1), aio.getPollOptions()[x]);
 							else
 								x = 11;
 						}
 						tpc1Data.put("min_level", aio.getPollMinLevel());
 					}
 					
 					Elements tpc1Error = clonedDoc.getElementsContainingOwnText("There was an error posting your message:");
 					if (!tpc1Error.isEmpty()) {
 						aio.wtl("there was an error in post topic step 1, ending early");
 						aio.postError(tpc1Error.first().parent().parent().text());
 						aio.postExecuteCleanup(desc);
 					}
 					else {
 						aio.wtl("finishing post topic step 1, sending step 2");
 						post(((desc == NetDesc.QPOSTTPC_S1) ? NetDesc.QPOSTTPC_S3 : NetDesc.POSTTPC_S2), lastPath, tpc1Data);
 					}
 					break;
 					
 				case POSTTPC_S2:
 					aio.wtl("session hNR determined this is post topic step 2");
 					String tpc2Key = clonedDoc.getElementsByAttributeValue("name", "key").attr("value");
 					String tpcPost_id = clonedDoc.getElementsByAttributeValue("name", "post_id").attr("value");
 					String tpcUid = clonedDoc.getElementsByAttributeValue("name", "uid").attr("value");
 					
 					HashMap<String, String> tpc2Data = new HashMap<String, String>();
 					tpc2Data.put("post", "Post Message");
 					tpc2Data.put("key", tpc2Key);
 					tpc2Data.put("post_id", tpcPost_id);
 					tpc2Data.put("uid", tpcUid);
 					
 					if (aio.isUsingPoll()) {
 						tpc2Data.put("poll_text", aio.getPollTitle());
 						for (int x = 0; x < 10; x++) {
 							if (aio.getPollOptions()[x].length() != 0)
 								tpc2Data.put("poll_option_" + (x + 1), aio.getPollOptions()[x]);
 							else
 								x = 11;
 						}
 						tpc2Data.put("min_level", aio.getPollMinLevel());
 					}
 					
 					Elements tpc2Error = clonedDoc.getElementsContainingOwnText("There was an error posting your message:");
 					Elements tpc2AutoFlag = clonedDoc.getElementsContainingOwnText("There were one or more potential problems with your message:");
 					if (!tpc2Error.isEmpty()) {
 						aio.wtl("there was an error in post topic step 2, ending early");
 						aio.postError(tpc2Error.first().parent().parent().text());
 						aio.postExecuteCleanup(desc);
 					}
 					else if (!tpc2AutoFlag.isEmpty()) {
 						aio.wtl("autoflag got tripped in post msg step 2, showing autoflag dialog");
 						String msg = tpc2AutoFlag.first().parent().parent().text();
 						showAutoFlagWarning(lastPath, tpc2Data, NetDesc.POSTTPC_S3, msg);
 					}
 					else {
 						aio.wtl("finishing post topic step 2, sending step 3");
 						post(NetDesc.POSTTPC_S3, lastPath, tpc2Data);
 					}
 					break;
 					
 				case POSTTPC_S3:
 				case QPOSTTPC_S3:
 					aio.wtl("session hNR determined this is post topic step 3 (if jumping from 1 to 3, then app is quick posting)");
 					aio.wtl("finishing post topic step 3, sending step 4");
 
 					Elements tpc3AutoFlag = clonedDoc.getElementsContainingOwnText("There were one or more potential problems with your message:");
 					Elements tpc3Error = clonedDoc.getElementsContainingOwnText("There was an error posting your message:");
 					if (!tpc3Error.isEmpty()) {
 						aio.wtl("there was an error in post topic step 3, ending early");
 						aio.postError(tpc3Error.first().parent().parent().text());
 						aio.postExecuteCleanup(desc);
 					}
 					else if (!tpc3AutoFlag.isEmpty()) {
 						aio.wtl("autoflag got tripped in post msg step 3, getting data and showing autoflag dialog");
 						String msg = tpc3AutoFlag.first().parent().parent().text();
 						
 						String tpc3Key = clonedDoc.getElementsByAttributeValue("name", "key").attr("value");
 						String tpc3Post_id = clonedDoc.getElementsByAttributeValue("name", "post_id").attr("value");
 						String tpc3Uid = clonedDoc.getElementsByAttributeValue("name", "uid").attr("value");
 						
 						HashMap<String, String> tpc3Data = new HashMap<String, String>();
 						tpc3Data.put("post", "Post Message");
 						tpc3Data.put("key", tpc3Key);
 						tpc3Data.put("post_id", tpc3Post_id);
 						tpc3Data.put("uid", tpc3Uid);
 						
 						showAutoFlagWarning(lastPath, tpc3Data, NetDesc.POSTTPC_S3, msg);
 					}
 					else {
 						goBack(true);
 					}
 					break;
 					
 				case TOPIC:
 					aio.wtl("session hNR determined this is a topic");
 					if (!clonedDoc.select("p:contains(no longer available for viewing)").isEmpty()) {
 						Crouton.showText(aio, "The topic you selected is no longer available for viewing.", AllInOneV2.getCroutonStyle());
 						aio.wtl("topic is no longer available, treat response as a board");
 						aio.processContent(NetDesc.BOARD, clonedDoc, resUrl);
 					}
 					else {
 						aio.wtl("handle the topic in AIO");
 						aio.processContent(desc, clonedDoc, resUrl);
 					}
 					break;
 					
 				case MARKMSG_S1:
 					if (clonedDoc.select("p:contains(you selected is no longer available for viewing.)").isEmpty()) {
 						HashMap<String, String> markData = new HashMap<String, String>();
 						markData.put("reason", aio.getReportCode());
 						markData.put("key", clonedDoc.select("input[name=key]").first().attr("value"));
 						post(NetDesc.MARKMSG_S2, resUrl + "?action=mod", markData);
 					}
 					else
 						Crouton.showText(aio, "The topic has already been removed!", AllInOneV2.getCroutonStyle());
 					
 					break;
 					
 				case MARKMSG_S2:
 					//This message has been marked for moderation.
 					if (!clonedDoc.select("p:contains(This message has been marked for moderation.)").isEmpty())
 						Crouton.showText(aio, "Message marked successfully.", AllInOneV2.getCroutonStyle());
 					else
 						Crouton.showText(aio, "There was an error marking the message.", AllInOneV2.getCroutonStyle());
 					
 					refresh();
 					break;
 					
 				case DLTMSG_S1:
 					String delKey = clonedDoc.getElementsByAttributeValue("name", "key").attr("value");
 					HashMap<String, String> delData = new HashMap<String, String>();
 					delData.put("YES", "Delete this Post");
 					delData.put("key", delKey);
 
 					post(NetDesc.DLTMSG_S2, resUrl + "?action=delete", delData);
 					break;
 					
 				case DLTMSG_S2:
 					goBack(true);
 					break;
 					
 				case CLOSE_TOPIC:
 					Crouton.showText(aio, "Topic closed successfully.", AllInOneV2.getCroutonStyle());
 					goBack(true);
 					break;
 					
 
 				case SEND_PM_S1:
 					String pmKey = clonedDoc.getElementsByAttributeValue("name", "key").attr("value");
 					
 					HashMap<String, String> pmData = new HashMap<String, String>();
 					pmData.put("key", pmKey);
 					pmData.put("to", aio.savedTo);
 					pmData.put("subject", aio.savedSubject);
 					pmData.put("message", aio.savedMessage);
 					pmData.put("submit", "Send Message");
 					
 					post(NetDesc.SEND_PM_S2, "/pm/new", pmData);
 					break;
 					
 				case SEND_PM_S2:
 					if (clonedDoc.select("input[name=subject]").isEmpty()) {
 						aio.pmCleanup(true, null);
 					}
 					else {
 						String error = clonedDoc.select("form[action=/pm/new]").first().previousElementSibling().text();
 						aio.pmCleanup(false, error);
 					}
 					break;
 
 				case GAME_SEARCH:
 				case BOARD_LIST:
 				case MESSAGE_DETAIL:
 				case AMP_LIST:
 				case TRACKED_TOPICS:
 				case BOARD:
 				case BOARD_JUMPER:
 				case UNSPECIFIED:
 				case USER_DETAIL:
 				case PM_INBOX:
 				case PM_DETAIL:
 				case VERIFY_ACCOUNT_S1:
 				case VERIFY_ACCOUNT_S2:
 					aio.wtl("session hNR determined this should be handled by AIO");
 					aio.processContent(desc, clonedDoc, resUrl);
 					break;
 				}
 			}
 			else {
 				// connection failed for some reason, probably timed out
 				aio.wtl("res was null in session hNR");
 				aio.timeoutCleanup(desc);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			aio.tryCaught(res.url().toString(), desc.toString(), e, res.body());
 		}
 
 		aio.wtl("session hNR finishing, desc: " + desc.name());
 	}
 
 	private boolean skipAIOCleanup = false;
 	@Override
 	public void postExecuteCleanup(NetDesc desc) {
 		switch (desc) {
 		case AMP_LIST:
 		case TRACKED_TOPICS:
 		case BOARD:
 		case BOARD_JUMPER:
 		case TOPIC:
 		case MESSAGE_DETAIL:
 		case USER_DETAIL:
 		case PM_INBOX:
 		case PM_DETAIL:
 		case CLOSE_TOPIC:
 		case GAME_SEARCH:
 		case BOARD_LIST:
 		case UNSPECIFIED:
 			if (!skipAIOCleanup)
 				aio.postExecuteCleanup(desc);
 			
 			break;
 			
 		case LOGIN_S1:
 		case LOGIN_S2:
 		case MARKMSG_S1:
 		case MARKMSG_S2:
 		case DLTMSG_S1:
 		case DLTMSG_S2:
 		case QEDIT_MSG:
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
 		case VERIFY_ACCOUNT_S1:
 		case VERIFY_ACCOUNT_S2:
 		case SEND_PM_S1:
 		case SEND_PM_S2:
 			break;
 		}
 		
 		skipAIOCleanup = false;
 	}
 	
 	public boolean canGoBack() {
 		return historyDB.hasHistory();
 	}
 	
 	public void goBack(boolean forceReload) {
 		History h = historyDB.pullHistory();
 		
 		applySavedScroll = true;
 		savedScrollVal = h.getVertPos();
 		
 		if (forceReload || AllInOneV2.getSettingsPref().getBoolean("reloadOnBack", false)) {
 			addToHistory = false;
 			aio.wtl("going back in history, refreshing: " + h.getDesc().name() + " " + h.getPath());
 			get(h.getDesc(), h.getPath(), null);
 		}
 		else {
 			aio.wtl("going back in history: " + h.getDesc().name() + " " + h.getPath());
 			lastDesc = h.getDesc();
 			lastDoc = h.getDoc();
 			lastPath = h.getPath();
			aio.processContent(lastDesc, lastDoc, lastPath);
 		}
 	}
 	
 	public void refresh() {
 		addToHistory = false;
 		aio.wtl("refreshing: " + lastDesc.name() + " " + lastPath);
 		applySavedScroll = true;
 		savedScrollVal = aio.getScrollerVertLoc();
 		
 		int i = lastPath.indexOf('#');
 		String trimmedPath;
 		if (i != -1)
 			trimmedPath = lastPath.substring(0, i);
 		else
 			trimmedPath = lastPath;
 		
 		if (lastDesc == NetDesc.AMP_LIST)
 			trimmedPath = AllInOneV2.buildAMPLink();
 		
 		get(lastDesc, trimmedPath, null);
 	}
 	
 	private void showAutoFlagWarning(final String path, final Map<String, String> data, final NetDesc desc, String msg) {
 		AlertDialog.Builder b = new AlertDialog.Builder(aio);
 		b.setTitle("Post Warning");
 		b.setMessage(msg);
 		
 		b.setPositiveButton("Post anyway", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				post(desc, path, data);
 			}
 		});
 		
 		b.setNegativeButton("Cancel", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				aio.postExecuteCleanup(desc);
 			}
 		});
 		
 		Dialog d = b.create();
 		d.setCancelable(false);
 		
 		d.show();
 	}
 	
     private void updateUserLevel(Document doc) {
     	String sc = doc.getElementsByTag("head").first().getElementsByTag("script").html();
     	int start = sc.indexOf("UserLevel','") + 12;
     	int end = sc.indexOf('\'', start + 1);
     	userLevel = Integer.parseInt(sc.substring(start, end));
     	
 		aio.wtl("user level: " + userLevel);
     }
 }
