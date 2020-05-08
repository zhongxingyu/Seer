 package com.ioabsoftware.DroidFAQs.Networking;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.jsoup.Connection.Method;
 import org.jsoup.Connection.Response;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.widget.Toast;
 
 import com.ioabsoftware.DroidFAQs.AllInOneV2;
 import com.ioabsoftware.DroidFAQs.HandlesNetworkResult;
 import com.ioabsoftware.DroidFAQs.HandlesNetworkResult.NetDesc;
 
 /**
  * Session is used to establish and maintain GFAQs sessions, and to send GET and POST requests.
  * @author Charles Rosaaen, Insanity On A Bun Software
  *
  */
 public class Session implements HandlesNetworkResult {
 	
 	/** The root of GFAQs. */
 	public static final String ROOT = "http://m.gamefaqs.com";
 	
 	/** Holds all cookies for the session */
 	private Map<String, String> cookies = new LinkedHashMap<String, String>();
 	
 	private String lastAttemptedPath;
 	public String getLastAttemptedPath()
 	{return lastAttemptedPath;};
 	
 	private NetDesc lastAttemptedDesc;
 	public NetDesc getLastAttemptedDesc()
 	{return lastAttemptedDesc;};
 	
 	/** The latest page, without excess get data. */
 	private String lastPathWithoutData = null;
 	/** Get's the path of the latest page, stripped of any GET data. */
 	public String getLastPathWithoutData()
 	{return lastPathWithoutData;}
 	
 	/** The latest page, with excess get data. */
 	private String lastPath = null;
 	/** Get's the path of the latest page. */
 	public String getLastPath()
 	{return lastPath;}
 	
 	/** The latest description. */
 	private NetDesc lastDesc = null;
 	/** Get's the description of the latest page. */
 	public NetDesc getLastDesc()
 	{return lastDesc;}
 	
 	/** The latest Response. */
 	private Response lastRes = null;
 	/** Get's the response from the latest page. */
 	public Response getLastRes()
 	{return lastRes;}
 	
 	/** The name of the user for this session. */
 	private static String user = null;
 	/** Get's the name of the session user. */
 	public static String getUser()
 	{return user;}
 	public static boolean isLoggedIn()
 	{return user != null;}
 	
 	public static boolean applySavedScroll;
 	public static int savedScrollVal;
 	
 	/** The password of the user for this session.  */
 	private String password = null;
 	
 	/** The current activity. */
 	private AllInOneV2 aio;
 	
     private boolean addToHistory = true;
 	private LinkedList<History> history;
 	
 	private String key = null;
 	private boolean needToSetNavList = true;
     
 	
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
 	 * Final construction method.
 	 * @param activity The current activity
 	 * @param userIn Username, or null if no user
 	 * @param passwordIn Password, or null if no user
 	 * @param path The final page to end at, or null if ROOT
 	 */
 	private void finalConstructor(AllInOneV2 aioIn, String userIn, String passwordIn)
 	{
 		aio = aioIn;
 		aio.disableNavList();
 		
         history = new LinkedList<History>();
 		
 		String path = ROOT + "/boards";
 		
 		if (userIn == null) {
 			user = null;
 			password = null;
 			get(NetDesc.BOARD_JUMPER, path, null);
 		}
 		else {
 			user = userIn;
 			password = passwordIn;
 			get(NetDesc.LOGIN_S1, path, null);
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
 	
 	public void devCheckForUpdate() {
 		new NetworkTask(this, NetDesc.DEV_UPDATE_CHECK, Method.GET, 
 				new HashMap<String, String>(), "http://freetexthost.com/zknqchrb4q", null).execute();
 	}
 	
 	/**
 	 * Sends a GET request to a specified page.
 	 * @param caller The HandlesNetworkResult making this call.
 	 * @param desc Description of this request, to properly handle the response later.
 	 * @param path The path to send the request to.
 	 * @param data The extra data to send along, pass null if no extra data.
 	 */
 	public void get(NetDesc desc, String path, Map<String, String> data)
 	{
 		lastAttemptedPath = path;
 		lastAttemptedDesc = desc;
 		if (data != null)
 			new NetworkTask(this, desc, Method.GET, cookies, buildURL(path), data).execute();
 		else
 			new NetworkTask(this, desc, Method.GET, cookies, buildURL(path), null).execute();
 	}
 	
 	/**
 	 * Sends a POST request to a specified page.
 	 * @param caller The HandlesNetworkResult making this call.
 	 * @param desc Description of this request, to properly handle the response later.
 	 * @param path The path to send the request to.
 	 * @param data The extra data to send along.
 	 */
 	public void post(NetDesc desc, String path, Map<String, String> data)
 	{
 		if (data.containsKey("key")) {
 			if (key == null) {
 				key = data.get("key");
 				aio.wtl("setting key: " + key);
 			}
 			else
 				aio.wtl("saved / new key: " + key + " / " + data.get("key"));
 		}
 		new NetworkTask(this, desc, Method.POST, cookies, buildURL(path), data).execute();
 	}
 	
 	public void addCookies(Map<String, String> newCookies)
 	{
 		cookies.putAll(newCookies);
 	}
 
 	@Override
 	public void preExecuteSetup(NetDesc desc) {
 		switch (desc) {
 		case AMP_LIST:
 		case BOARD:
 		case BOARD_JUMPER:
 		case TOPIC:
 		case GAME_SEARCH:
 		case BOARD_LIST:
 		case USER_DETAIL:
 		case PM_INBOX:
 		case READ_PM:
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
 
 		case DEV_UPDATE_CHECK:
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
 			if (res != null) {
 				
 				if (desc == NetDesc.DEV_UPDATE_CHECK) {
 					aio.wtl("session hNR has determined this is an update check");
 					aio.processContent(res, desc);
 					return;
 				}
 				
 				Document pRes = res.parse();
 				
				if (res.statusCode() == 403) {
 					Elements paragraphs = pRes.getElementsByTag("p");
 					aio.fourOhError(403, paragraphs.get(1).text() + "\n\n" + paragraphs.get(2).text());
 					return;
 				}
 				
				if (res.statusCode() == 401) {
 					if (lastDesc == NetDesc.LOGIN_S2) {
 						skipAIOCleanup = true;
 						get(NetDesc.BOARD_JUMPER, ROOT + "/boards", null);
 					}
 					else {
 						Elements paragraphs = pRes.getElementsByTag("p");
 						aio.fourOhError(401, paragraphs.get(1).text() + "\n\n" + paragraphs.get(2).text());
 					}
 					return;
 				}
 				
 				switch (desc) {
 				case AMP_LIST:
 				case BOARD:
 				case BOARD_JUMPER:
 				case TOPIC:
 				case GAME_SEARCH:
 				case BOARD_LIST:
 				case USER_DETAIL:
 				case PM_INBOX:
 				case READ_PM:
 				case UNSPECIFIED:
 				case DEV_UPDATE_CHECK:
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
 						case BOARD:
 						case BOARD_JUMPER:
 						case TOPIC:
 						case GAME_SEARCH:
 						case BOARD_LIST:
 						case USER_DETAIL:
 						case PM_INBOX:
 						case READ_PM:
 						case UNSPECIFIED:
							aio.wtl("beginning history addition");
 							int i = lastPath.indexOf('#');
 							String trimmedPath;
 							if (i != -1)
 								trimmedPath = lastPath.substring(0, i);
 							else
 								trimmedPath = lastPath;
 							
 							// if there's no history, just add the previous page
 							if (history.isEmpty()) {
 								aio.wtl("adding to history, trimmedPath: " + trimmedPath + ", lastDesc: " + lastDesc.name());
 								aio.wtl("current desc is: " + desc.name());
 								history.add(new History(trimmedPath, lastDesc, lastRes, aio.getScrollerVertLoc()));
 							}
 							// if there is history, make sure we're not duplicating the last entry
 							else if (!(history.get(history.size() - 1).getPath().equals(lastPath))) {
 								aio.wtl("adding to history, trimmedPath: " + trimmedPath + ", lastDesc: " + lastDesc.name());
 								aio.wtl("current desc is: " + desc.name());
 								history.add(new History(trimmedPath, lastDesc, lastRes, aio.getScrollerVertLoc()));
 							}
							aio.wtl("finished history addition");
 							break;
 							
 						case DEV_UPDATE_CHECK:
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
 				case BOARD:
 				case BOARD_JUMPER:
 				case TOPIC:
 				case GAME_SEARCH:
 				case BOARD_LIST:
 				case USER_DETAIL:
 				case PM_INBOX:
 				case READ_PM:
 				case UNSPECIFIED:
 				case DEV_UPDATE_CHECK:
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
 					lastRes = res;
 					lastPath = res.url().toString();
 					lastPathWithoutData = stripExtraDataFromURL(lastPath);
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

				aio.wtl("adding cookies");
 				cookies.putAll(res.cookies());
 				
 				switch (desc) {
 				case LOGIN_S1:
 					aio.wtl("session hNR determined this is login step 1");
 					String loginKey = pRes.getElementsByAttributeValue("name", "key").attr("value");
 					
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
 					if (AllInOneV2.getSettingsPref().getBoolean("startAtAMP", false))
 						get(NetDesc.AMP_LIST, AllInOneV2.buildAMPLink(), null);
 					else
 						get(NetDesc.BOARD_JUMPER, ROOT + "/boards", null);
 					
 					break;
 					
 				case POSTMSG_S1:
 				case QPOSTMSG_S1:
 				case QEDIT_MSG:
 					
 					aio.wtl("session hNR determined this is post message step 1");
 					String msg1Key = pRes.getElementsByAttributeValue("name", "key").attr("value");
 					
 					String sig;
 					if (desc == NetDesc.QEDIT_MSG)
 						sig = "";
 					else
 						sig = aio.getSig();
 					
 					HashMap<String, String> msg1Data = new HashMap<String, String>();
 					msg1Data.put("messagetext", aio.getSavedPostBody());
 					msg1Data.put("custom_sig", sig);
 					msg1Data.put("post", ((desc == NetDesc.POSTMSG_S1) ? "Preview Message" : "Post without Preview"));
 					msg1Data.put("key", msg1Key);
 					
 					Elements msg1Error = pRes.getElementsContainingOwnText("There was an error posting your message:");
 					if (!msg1Error.isEmpty()) {
 						aio.wtl("there was an error in post msg step 1, ending early");
 						aio.postError(msg1Error.first().parent().parent().text());
 						aio.postExecuteCleanup(desc);
 					}
 					else {
 						aio.wtl("finishing post message step 1, sending step 2");
 						if (desc == NetDesc.POSTMSG_S1)
 							post(NetDesc.POSTMSG_S2, lastPath, msg1Data);
 						else
 							post(NetDesc.QPOSTMSG_S3, lastPath, msg1Data);
 					}
 					break;
 					
 				case POSTMSG_S2:
 					aio.wtl("session hNR determined this is post message step 2");
 					String msg2Key = pRes.getElementsByAttributeValue("name", "key").attr("value");
 					String msgPost_id = pRes.getElementsByAttributeValue("name", "post_id").attr("value");
 					String msgUid = pRes.getElementsByAttributeValue("name", "uid").attr("value");
 					
 					HashMap<String, String> msg2Data = new HashMap<String, String>();
 					msg2Data.put("post", "Post Message");
 					msg2Data.put("key", msg2Key);
 					msg2Data.put("post_id", msgPost_id);
 					msg2Data.put("uid", msgUid);
 					
 					Elements msg2Error = pRes.getElementsContainingOwnText("There was an error posting your message:");
 					Elements msg2AutoFlag = pRes.getElementsContainingOwnText("There were one or more potential problems with your message:");
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
 
 					Elements msg3AutoFlag = pRes.getElementsContainingOwnText("There were one or more potential problems with your message:");
 					Elements msg3Error = pRes.getElementsContainingOwnText("There was an error posting your message:");
 					if (!msg3Error.isEmpty()) {
 						aio.wtl("there was an error in post msg step 3, ending early");
 						aio.postError(msg3Error.first().parent().parent().text());
 						aio.postExecuteCleanup(desc);
 					}
 					else if (!msg3AutoFlag.isEmpty()) {
 						aio.wtl("autoflag got tripped in post msg step 3, getting data and showing autoflag dialog");
 						String msg = msg3AutoFlag.first().parent().parent().text();
 						
 						String msg3Key = pRes.getElementsByAttributeValue("name", "key").attr("value");
 						String msg3Post_id = pRes.getElementsByAttributeValue("name", "post_id").attr("value");
 						String msg3Uid = pRes.getElementsByAttributeValue("name", "uid").attr("value");
 						
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
 					String tpc1Key = pRes.getElementsByAttributeValue("name", "key").attr("value");
 					
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
 					
 					Elements tpc1Error = pRes.getElementsContainingOwnText("There was an error posting your message:");
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
 					String tpc2Key = pRes.getElementsByAttributeValue("name", "key").attr("value");
 					String tpcPost_id = pRes.getElementsByAttributeValue("name", "post_id").attr("value");
 					String tpcUid = pRes.getElementsByAttributeValue("name", "uid").attr("value");
 					
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
 					
 					Elements tpc2Error = pRes.getElementsContainingOwnText("There was an error posting your message:");
 					Elements tpc2AutoFlag = pRes.getElementsContainingOwnText("There were one or more potential problems with your message:");
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
 
 					Elements tpc3AutoFlag = pRes.getElementsContainingOwnText("There were one or more potential problems with your message:");
 					Elements tpc3Error = pRes.getElementsContainingOwnText("There was an error posting your message:");
 					if (!tpc3Error.isEmpty()) {
 						aio.wtl("there was an error in post topic step 3, ending early");
 						aio.postError(tpc3Error.first().parent().parent().text());
 						aio.postExecuteCleanup(desc);
 					}
 					else if (!tpc3AutoFlag.isEmpty()) {
 						aio.wtl("autoflag got tripped in post msg step 3, getting data and showing autoflag dialog");
 						String msg = tpc3AutoFlag.first().parent().parent().text();
 						
 						String tpc3Key = pRes.getElementsByAttributeValue("name", "key").attr("value");
 						String tpc3Post_id = pRes.getElementsByAttributeValue("name", "post_id").attr("value");
 						String tpc3Uid = pRes.getElementsByAttributeValue("name", "uid").attr("value");
 						
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
					if (!pRes.select("p:contains(no longer available)").isEmpty()) {
 						Toast.makeText(aio, "The topic you selected is no longer available for viewing.", Toast.LENGTH_SHORT).show();
 						aio.wtl("topic is no longer available, treat response as a board");
 						aio.processContent(res, NetDesc.BOARD);
 					}
 					else {
 						aio.wtl("handle the topic in AIO");
 						aio.processContent(res, desc);
 					}
 					break;
 					
 				case MARKMSG_S1:
 					HashMap<String, String> markData = new HashMap<String, String>();
 					markData.put("reason", aio.getReportCode());
 					markData.put("key", pRes.getElementsByAttributeValue("name", "key").first().attr("value"));
 					post(NetDesc.MARKMSG_S2, res.url() + "?action=mod", markData);
 					break;
 					
 				case MARKMSG_S2:
 					//This message has been marked for moderation.
 					if (!pRes.select("p:contains(This message has been marked for moderation.)").isEmpty())
 						Toast.makeText(aio, "Message marked successfully.", Toast.LENGTH_SHORT).show();
 					else
 						Toast.makeText(aio, "There was an error marking the message.", Toast.LENGTH_SHORT).show();
 					break;
 					
 				case DLTMSG_S1:
 					String delKey = pRes.getElementsByAttributeValue("name", "key").attr("value");
 					HashMap<String, String> delData = new HashMap<String, String>();
 					delData.put("YES", "Delete this Post");
 					delData.put("key", delKey);
 
 					post(NetDesc.DLTMSG_S2, res.url().toString() + "?action=delete", delData);
 					break;
 					
 				case DLTMSG_S2:
 					goBack(true);
 					break;
 					
 				case CLOSE_TOPIC:
 					Toast.makeText(aio, "Topic closed successfully.", Toast.LENGTH_SHORT).show();
 					goBack(true);
 					break;
 					
 
 				case SEND_PM_S1:
 					String pmKey = pRes.getElementsByAttributeValue("name", "key").attr("value");
 					
 					HashMap<String, String> pmData = new HashMap<String, String>();
 					pmData.put("key", pmKey);
 					pmData.put("to", aio.savedTo);
 					pmData.put("subject", aio.savedSubject);
 					pmData.put("message", aio.savedMessage);
 					pmData.put("submit", "Send Message");
 					
 					post(NetDesc.SEND_PM_S2, "http://m.gamefaqs.com/pm/new", pmData);
 					break;
 					
 				case SEND_PM_S2:
 					if (pRes.select("input[name=subject]").isEmpty()) {
 						aio.pmCleanup(true, null);
 					}
 					else {
 						String error = pRes.select("form[action=/pm/new]").first().previousElementSibling().text();
 						aio.pmCleanup(false, error);
 					}
 					break;
 
 				case DEV_UPDATE_CHECK:
 				case GAME_SEARCH:
 				case BOARD_LIST:
 				case AMP_LIST:
 				case BOARD:
 				case BOARD_JUMPER:
 				case UNSPECIFIED:
 				case USER_DETAIL:
 				case PM_INBOX:
 				case READ_PM:
 				case VERIFY_ACCOUNT_S1:
 				case VERIFY_ACCOUNT_S2:
 					if (needToSetNavList) {
 						aio.setNavList(isLoggedIn());
 						needToSetNavList = false;
 					}
 					
 					aio.wtl("session hNR determined this should be handled by AIO");
 					aio.processContent(res, desc);
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
 			aio.tryCaught(res.url().toString(), ExceptionUtils.getStackTrace(e), res.body());
 		}
 
 		aio.wtl("session hNR finishing, desc: " + desc.name());
 	}
 
 	private boolean skipAIOCleanup = false;
 	@Override
 	public void postExecuteCleanup(NetDesc desc) {
 		switch (desc) {
 		case AMP_LIST:
 		case BOARD:
 		case BOARD_JUMPER:
 		case TOPIC:
 		case USER_DETAIL:
 		case PM_INBOX:
 		case READ_PM:
 		case MARKMSG_S2:
 		case CLOSE_TOPIC:
 		case GAME_SEARCH:
 		case BOARD_LIST:
 		case UNSPECIFIED:
 			if (!skipAIOCleanup)
 				aio.postExecuteCleanup(desc);
 			
 			break;
 			
 		case DEV_UPDATE_CHECK:
 		case LOGIN_S1:
 		case LOGIN_S2:
 		case MARKMSG_S1:
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
 	
 	public static String stripExtraDataFromURL(String s) {
 		if (s.contains("?"))
 			s = s.substring(0, s.indexOf('?'));
 		return s;
 	}
 	
 	public boolean canGoBack() {
 		return !history.isEmpty();
 	}
 	
 	public void goBack(boolean forceReload) {
 		History h = history.removeLast();
 		
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
 			lastRes = h.getRes();
 			lastPath = h.getRes().url().toString();
 			aio.processContent(h.getRes(), h.getDesc());
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
 	
 }
