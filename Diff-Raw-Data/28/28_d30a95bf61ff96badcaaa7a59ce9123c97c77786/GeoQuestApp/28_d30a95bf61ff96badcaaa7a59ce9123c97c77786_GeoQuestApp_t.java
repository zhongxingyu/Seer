 package edu.bonn.mobilegaming.geoquest;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.XPath;
 import org.dom4j.io.SAXReader;
 
 import android.app.Activity;
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
import android.location.Location;
 import android.media.MediaPlayer;
 import android.net.ConnectivityManager;
 import android.os.Environment;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.android.maps.MapView;
 import com.qeevee.gq.res.ResourceManager;
 
 import edu.bonn.mobilegaming.geoquest.adaptioninterfaces.AdaptionEngineInterface;
 import edu.bonn.mobilegaming.geoquest.gameaccess.GameDataManager;
 import edu.bonn.mobilegaming.geoquest.gameaccess.GameItem;
 import edu.bonn.mobilegaming.geoquest.gameaccess.GeoQuestServerProxy;
 import edu.bonn.mobilegaming.geoquest.gameaccess.RepositoryItem;
 import edu.bonn.mobilegaming.geoquest.ui.InteractionBlocker;
 
 public class GeoQuestApp extends Application implements InteractionBlocker {
 
     public static boolean useAdaptionEngine = false;
     private static boolean adaptionEngineLibAvailable = false;
     public static AdaptionEngineInterface adaptionEngine;
 
     private MapView googleMap;
     private boolean isInGame = false;
 
     public void setGoogleMap(com.google.android.maps.MapView value) {
 	this.googleMap = value;
     }
 
     public MapView getGoogleMap() {
 	return this.googleMap;
     }
 
     public static final int DIALOG_ID_START_GAME = 0;
     public static final int DIALOG_ID_DOWNLOAD_GAME = 1;
     public static final int DIALOG_ID_DOWNLOAD_REPO_DATA = 2;
 
     public static ExecutorService singleThreadExecutor;
     private static final String TAG = "GeoQuestApp";
     public static final String MAIN_PREF_FILE_NAME = "GeoQuestPreferences";
     public static final String GQ_MANUAL_LOCATION_PROVIDER = "GeoQuest Manual Location Provider";
     private static GeoQuestApp theApp = null;
 
     private ArrayList<Activity> activities = new ArrayList<Activity>();
     private static File currentGameDir = null;
 
     private static MissionOrToolActivity currentActivity;
 
     public static MissionOrToolActivity getCurrentActivity() {
 	return currentActivity;
     }
 
     /**
      * @param currentActivity
      *            the currently activated mission or tool activity
      */
     public static void setCurrentActivity(MissionOrToolActivity activity) {
 	currentActivity = activity;
     }
 
     public static GeoQuestApp getInstance() {
 	return theApp;
     }
 
     @Override
     public void onCreate() {
 	super.onCreate();
 	theApp = this;
 	singleThreadExecutor = Executors.newFixedThreadPool(1);
 
 	// TODO: initialize all preferences to their defaults if they are not
 	// already set
 	SharedPreferences prefs = PreferenceManager
 		.getDefaultSharedPreferences(this);
 	if (!prefs.contains(Preferences.PREF_KEY_SERVER_URL)) {
 	    prefs.edit().putString(Preferences.PREF_KEY_SERVER_URL,
 				   getString(R.string.geoquest_server_url))
 		    .commit();
 	}
 	includeAdaptionEngine();
     }
 
     public void addActivity(Activity newActivityOfThisApp) {
 	if (!activities.contains(newActivityOfThisApp))
 	    activities.add(newActivityOfThisApp);
 	// if (isGameActivity(newActivityOfThisApp) && newActivityOfThisApp
 	// instanceof MissionOrToolActivity)
 	// setCurrentActivity((MissionOrToolActivity)newActivityOfThisApp);
     }
 
     void removeActivity(Activity finishedActivity) {
 	if (activities.contains(finishedActivity))
 	    activities.remove(finishedActivity);
     }
 
     @Override
     public void onLowMemory() {
 	super.onLowMemory();
     }
 
     @Override
     public void onTerminate() {
 	singleThreadExecutor.shutdown();
 	terminateApp();
 	super.onTerminate();
     }
 
     public static Context getContext() {
 	return theApp;
     }
 
     /**
      * TODO: rework to serverAvailable().
      * 
      * @return
      */
     public boolean isOnline() {
 	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	return cm.getActiveNetworkInfo() != null
 		&& cm.getActiveNetworkInfo().isAvailable()
 		&& cm.getActiveNetworkInfo().isConnected();
     }
 
     /**
      * Really stops the app and all its activities.
      * 
      * Should be invoked when user explicitly selects menu item etc.
      * 
      * Should NOT be invoked e.g. when Home Button is pressed.
      * 
      */
     public void terminateApp() {
 	Activity[] allActivities = new Activity[activities.size()];
 	activities.toArray(allActivities);
 	for (int i = 0; i < allActivities.length; i++) {
 	    activities.remove(allActivities[i]);
 	    allActivities[i].finish();
 	}
 	cleanMediaPlayer();
 	System.exit(0);
     }
 
     public static void setRecentGame(String recentRepo,
 				     String recentGame,
 				     String recentGameFileName) {
 	SharedPreferences prefs = theApp
 		.getSharedPreferences(GeoQuestApp.MAIN_PREF_FILE_NAME,
 				      Context.MODE_PRIVATE);
 	SharedPreferences.Editor editor = prefs.edit();
 	editor.putString(Preferences.PREF_KEY_LAST_USED_REPOSITORY,
 			 recentRepo);
 	editor.putString(Preferences.PREF_KEY_LAST_PLAYED_GAME_NAME,
 			 recentGame);
 	editor.putString(Preferences.PREF_KEY_LAST_PLAYED_GAME_FILE_NAME,
 			 recentGameFileName);
 	editor.commit();
     }
 
     public static String getRecentRepo() {
 	return theApp.getSharedPreferences(GeoQuestApp.MAIN_PREF_FILE_NAME,
 					   Context.MODE_PRIVATE)
 		.getString(Preferences.PREF_KEY_LAST_USED_REPOSITORY,
 			   theApp.getText(R.string.local_default_repository)
 				   .toString());
     }
     public static int getRecentSortingMode(){
     	return theApp
     			.getSharedPreferences(GeoQuestApp.MAIN_PREF_FILE_NAME,
     					      Context.MODE_PRIVATE)
     			.getInt(Preferences.PREF_KEY_SORTING_MODE, GameItem.SORT_GAMELIST_BY_DEFAULT);
     }
 
     public static String getRecentGameFileName() {
 	return theApp.getSharedPreferences(GeoQuestApp.MAIN_PREF_FILE_NAME,
 					   Context.MODE_PRIVATE)
 		.getString(Preferences.PREF_KEY_LAST_PLAYED_GAME_FILE_NAME,
 			   null);
     }
 
     public static String getRecentGame() {
 	return theApp.getSharedPreferences(GeoQuestApp.MAIN_PREF_FILE_NAME,
 					   Context.MODE_PRIVATE)
 		.getString(Preferences.PREF_KEY_LAST_PLAYED_GAME_NAME,
 			   null);
     }
 
     /**
      * 
      * @return the directory in which all files of the currently loaded (and
      *         played) game are stored.
      */
     public static File getCurrentGameDir() {
 	return currentGameDir;
     }
 
     public static void setCurrentGameDir(File currentGameDir) {
 	GeoQuestApp.currentGameDir = currentGameDir;
     }
 
     public static List<String> getRepositoryNamesAsList() {
 	return new ArrayList<String>(repositoryItems.keySet());
     }
 
     public static List<String> getNotEmptyRepositoryNamesAsList() {
 	List<String> namesOfNonEmptyRepos = new ArrayList<String>();
 	for (Iterator<RepositoryItem> iterator = repositoryItems.values()
 		.iterator(); iterator.hasNext();) {
 	    RepositoryItem curRepoItem = iterator.next();
 	    if (curRepoItem.gameNames().size() > 0)
 		namesOfNonEmptyRepos.add(curRepoItem.getName());
 	}
 	//sort repolist by name
 	Collections.sort(namesOfNonEmptyRepos);
 	return namesOfNonEmptyRepos;
     }
 
     public static List<String> getLocalRepositories() {
 	return Arrays.asList(getLocalRepoDir(null).list());
     }
 
     public static List<String> getGameNamesForRepository(String repositoryName) {
 	if (repositoryItems == null)
 	    return new ArrayList<String>();
 	RepositoryItem repoItem = repositoryItems.get(repositoryName);
 	if (repoItem == null) {
 	    Log.e(TAG,
 		  "Error: repoitem is null");
 	}
	currentSortMode = getRecentSortingMode();
 	if(currentSortMode == GameItem.SORT_GAMELIST_BY_DISTANCE){
 		GeoQuestLocationListener locationListener = new GeoQuestLocationListener(getContext());
 		locationListener.connect();
 		Location aktLocation = locationListener.getLastLocation();
 		locationListener.disconnect();
 		if(aktLocation == null){
 			Toast.makeText(getContext(), R.string.error_getting_device_location, Toast.LENGTH_SHORT).show();
 		}
 		else{
 			repoItem.sortGameItemsBy(currentSortMode, aktLocation);
 		}	
 	}
 	else{
 		repoItem.sortGameItemsBy(currentSortMode);
 	}
 	
 	return repoItem.gameNames();
     }
 
     public static List<String> getLocalGamesInRepository(String repositoryName) {
 	return Arrays.asList(getLocalRepoDir(repositoryName).list());
     }
 
     public static CharSequence[] getRepositoryNames() {
 	CharSequence[] resultArray = new CharSequence[repositoryItems.size()];
 	int i = 0;
 	for (Iterator<String> iterator = repositoryItems.keySet().iterator(); iterator
 		.hasNext();) {
 	    resultArray[i++] = (CharSequence) iterator.next();
 	}
 	return resultArray;
     }
 
     private static Map<String, RepositoryItem> repositoryItems = new HashMap<String, RepositoryItem>();
     private static File runningGameDir;
     private boolean repoDataAvailable = false;
     public static int currentSortMode = GameItem.SORT_GAMELIST_BY_DEFAULT;
     private org.osmdroid.views.MapView osmap;
 
     public org.osmdroid.views.MapView getOsmap() {
 	return osmap;
     }
 
     public void setOSMap(org.osmdroid.views.MapView osmap) {
 	this.osmap = osmap;
     }
 
     public static boolean loadRepoData(GeoQuestProgressHandler handler) {
 //	boolean result = loadRepoDataFromServer(handler);
 //	result |= loadRepoDataFromClient(handler);
 	boolean result = loadRepoDataFromClient(handler);
 
 	handler.sendEmptyMessage(GeoQuestProgressHandler.MSG_FINISHED);
 
 	if (getInstance() != null) {
 	    getInstance().setRepoDataAvailable(result);
 	}
 
 	return result;
     }
 
     private static
 	    boolean
 	    loadRepoDataFromServer(final GeoQuestProgressHandler progressHandler) {
 	boolean success = false;
 	repositoryItems.clear();
 	if (!GeoQuestApp.getInstance().isOnline())
 	    return false;
 	try {
 	    GeoQuestServerProxy server = GeoQuestServerProxy.getInstance();
 	    Document doc = server.getRepoMetadata(progressHandler);
 	    if (doc == null)
 		return false; // if no xml document is returned stop and return
 	    // false. Then no repository items are created.
 
 	    // create internal representation of repositories:
 	    XPath xpathSelector = DocumentHelper.createXPath("//repository");
 	    @SuppressWarnings("unchecked")
 	    List<Element> nodesFromRemoteRepo = xpathSelector.selectNodes(doc);
 
 	    Message msg = new Message();
 
 	    File localRepoDir = GameDataManager.getLocalRepoDir(null);
 
 	    if (localRepoDir != null && localRepoDir.list() != null
 		    && nodesFromRemoteRepo != null) {
 		msg.arg1 = nodesFromRemoteRepo.size()
 			+ GameDataManager.getLocalRepoDir(null).list().length;
 		// we also need some space for local loading progress
 	    } else if (localRepoDir != null && localRepoDir.list() != null) {
 		msg.arg1 = localRepoDir.list().length;
 		Message errorMsg = new Message();
 		errorMsg.arg1 = R.string.error_fetching_remote_repo_list;
 		errorMsg.what = GeoQuestProgressHandler.MSG_ABORT_BY_ERROR;
 		progressHandler.sendMessage(errorMsg);
 	    } else if (nodesFromRemoteRepo != null) {
 		msg.arg1 = nodesFromRemoteRepo.size();
 	    }
 	    msg.arg2 = R.string.start_text_game_list_progress_process_text;
 	    msg.what = GeoQuestProgressHandler.MSG_TELL_MAX_AND_TITLE;
 	    progressHandler.sendMessage(msg);
 
 	    RepositoryItem curRepoItem;
 	    GameItem curGameItem;
 	    for (@SuppressWarnings("rawtypes")
 	    Iterator repoIterator = nodesFromRemoteRepo.iterator(); repoIterator
 		    .hasNext();) {
 		progressHandler
 			.sendEmptyMessage(GeoQuestProgressHandler.MSG_PROGRESS);
 		Element repoNode = (Element) repoIterator.next();
 		String repoName = repoNode.attributeValue("name");
 		if (repoName == null) {
 		    SharedPreferences prefs = PreferenceManager
 			    .getDefaultSharedPreferences(getContext());
 		    Log.d(TAG,
 			  "loadRepoDataFromServer(): Repository name missing on host "
 				  + prefs.getString(Preferences.PREF_KEY_SERVER_URL,
 						    getContext()
 							    .getString(R.string.geoquest_server_url)));
 		} else {
 		    // create RepositoryItem:
 		    curRepoItem = new RepositoryItem(repoName);
 		    curRepoItem.setOnServer();
 		    repositoryItems.put(repoName,
 					curRepoItem);
 
 		    // create GameItems for current repository:
 		    @SuppressWarnings("rawtypes")
 		    List gameNodes = repoNode.elements("game");
 		    for (@SuppressWarnings("rawtypes")
 		    Iterator gameIterator = gameNodes.iterator(); gameIterator
 			    .hasNext();) {
 			Element gameNode = (Element) gameIterator.next();
 			curGameItem = GameItem
 				.createFromRepoListGameNode(gameNode,
 							    curRepoItem);
 			if (curGameItem != null)
 			    curRepoItem.addGame(curGameItem);
 		    }
 		}
 	    }
 	    success = true;
 
 	} catch (Exception e) {
 	    Log.d(TAG,
 		  "getServerRepositories() : " + e);
 	    e.printStackTrace();
 	    success = false;
 	    File repoDir = GameDataManager.getLocalRepoDir(null);
 	    if (repoDir != null && repoDir.list() != null) {
 		Message setMaxToLocal = new Message();
 		setMaxToLocal.arg1 = repoDir.list().length;
 		setMaxToLocal.what = GeoQuestProgressHandler.MSG_TELL_MAX;
 		progressHandler.sendMessage(setMaxToLocal);
 	    }
 	}
 
 	return success;
     }
 
     private static boolean
 	    loadRepoDataFromClient(GeoQuestProgressHandler handler) {
 	boolean success = false;
 
 	try {
 	    String[] localRepoNames = GameDataManager.getLocalRepoDir(null)
 		    .list();
 
 	    RepositoryItem curRepoItem;
 	    if (localRepoNames == null)
 		return success;
 	    for (int i = 0; i < localRepoNames.length; i++) {
 		handler.sendEmptyMessage(GeoQuestProgressHandler.MSG_PROGRESS);
 		boolean alsoOnServer = false;
 		for (Iterator<Map.Entry<String, RepositoryItem>> iterator = repositoryItems
 			.entrySet().iterator(); iterator.hasNext();) {
 		    Map.Entry<String, RepositoryItem> entry = iterator.next();
 		    if (entry.getKey().equals(localRepoNames[i])) {
 			alsoOnServer = true;
 			success |= completeLocalRepoData(entry.getValue());
 			break;
 		    }
 		}
 
 		if (!alsoOnServer) {
 		    curRepoItem = new RepositoryItem(localRepoNames[i]);
 		    success |= completeLocalRepoData(curRepoItem);
 		    curRepoItem.setOnClient();
 		    repositoryItems.put(localRepoNames[i],
 					curRepoItem);
 		}
 	    }
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Message msg = new Message();
 	    msg.arg1 = R.string.error_fetching_local_repo_list;
 	    msg.what = GeoQuestProgressHandler.MSG_ABORT_BY_ERROR;
 	    handler.sendMessage(msg);
 	    success = false;
 	}
 
 	return success;
     }
 
     /**
      * Adds games which are only found locally and enhance game info for games
      * which are found both server- and client-side.
      * 
      * @param repoItem
      * @return true iff there is at least one additional game found locally.
      */
     private static boolean completeLocalRepoData(RepositoryItem repoItem) {
 	boolean additionalGameLocallyFound = false;
 	repoItem.setOnClient();
 	File localRepoDir = GameDataManager.getLocalRepoDir(repoItem.getName());
 	File[] gameDirs = localRepoDir.listFiles(new FileFilter() {
 
 	    public boolean accept(File file) {
 		if (!file.exists() || !file.isDirectory())
 		    return false;
 		String[] gameFileNames = file.list();
 		for (int i = 0; i < gameFileNames.length; i++) {
 		    if (gameFileNames[i].equals("game.xml"))
 			return true;
 		}
 		return false;
 	    }
 
 	});
 
 	SAXReader xmlReader = new SAXReader();
 	for (int i = 0; i < gameDirs.length; i++) {
 	    try {
 		File localGameFile = gameDirs[i].listFiles(new FileFilter() {
 		    public boolean accept(File pathname) {
 			return pathname.getName().equals("game.xml");
 		    }
 		})[0];
 		Document doc = xmlReader.read(localGameFile);
 		XPath xpathSelector = DocumentHelper.createXPath("//game");
 		Element gameNode = (Element) xpathSelector.selectSingleNode(doc);
 		String localGameName = gameNode.attributeValue("name");
 
 		if (repoItem.gameNames().contains(localGameName)) {
 		    // local game also on server => check for update:
 		    GameItem existingGameItem = repoItem
 			    .getGameItem(localGameName);
 		    existingGameItem.setOnClient(true);
 		    existingGameItem.setLastmodifiedClientSide(localGameFile
 			    .lastModified());
 		} else {
 		    // game only locally found, i.e. this is an additional game:
 		    repoItem.addGame(GameItem
 			    .createFromGameFileGameNode(gameNode,
 							localGameFile
 								.lastModified(),
 							gameDirs[i].getName(),
 							repoItem));
 		    additionalGameLocallyFound = true;
 		}
 	    } catch (DocumentException e) {
 		Log.d(TAG,
 		      e.toString());
 	    }
 
 	}
 
 	return additionalGameLocallyFound;
     }
 
     public static void showMessage(CharSequence text) {
 	Toast.makeText(GeoQuestApp.getContext(),
 		       text,
 		       Toast.LENGTH_SHORT).show();
     }
 
     /**
      * Creates the URL to the game zip file on the server.
      * 
      * @param repositoryName
      *            the plain name of the repository, e.g. "default",
      *            "stattreisen" etc.
      * @param gameName
      *            the plain file name of the game's zip file, but without the
      *            extension ".zip", e.g. "wccb" or "tauftour".
      * @return
      */
     public static URL makeGameURL(CharSequence repositoryName,
 				  String gameName) {
 	URL url = null;
 
 	SharedPreferences prefs = PreferenceManager
 		.getDefaultSharedPreferences(getContext());
 
 	String gamelistURL = prefs
 		.getString(Preferences.PREF_KEY_SERVER_URL,
 			   GeoQuestApp.getContext()
 				   .getString(R.string.geoquest_server_url))
 		+ "/" + "repositories";
 
 	try {
 	    url = new URL(gamelistURL + "/" + repositoryName + "/games/"
 		    + gameName + ".zip");
 	} catch (MalformedURLException e) {
 	    Log.d(TAG,
 		  "MalformedURLException: " + gamelistURL + "/"
 			  + repositoryName + "/games/" + gameName + ".zip");
 	} catch (Exception e) {
 	    Log.d(TAG,
 		  "Exception: " + gamelistURL + "/" + repositoryName
 			  + "/games/" + gameName + ".zip");
 	}
 	return url;
     }
 
     public void endGame() {
 	stopAudio();
 	Mission.clean();
 	HotspotOld.clean();
 	Variables.clean();
 	// TODO use separate list of game activities
 	Activity[] allActivities = new Activity[activities.size()];
 	activities.toArray(allActivities);
 	for (int i = 0; i < allActivities.length; i++) {
 	    if (isGameActivity(allActivities[i])) {
 		activities.remove(allActivities[i]);
 		allActivities[i].finish();
 	    }
 	}
 	setInGame(false);
 	setRunningGameDir(null);
 	cleanMediaPlayer();
     }
 
     private boolean isGameActivity(Activity activity) {
 	@SuppressWarnings("rawtypes")
 	Class actClass = activity.getClass();
 	if (actClass.equals(Start.class)
 		|| actClass.equals(RepoListActivity.class)
 		|| actClass.equals(GameListActivity.class))
 	    return false;
 	else
 	    return true;
     }
 
     public static GameItem getGameItem(CharSequence repoName,
 				       String gameName) {
 	return repositoryItems.get(repoName).getGameItem(gameName);
     }
 
     public static File getGameXMLFile(CharSequence repoName,
 				      String gameFileName) {
 	return new File(getLocalRepoDir(repoName), gameFileName + "/game.xml");
 	// TODO deal with the case that thie game xml file does not exist or
 	// even the game dir.
     }
 
     public static void setRunningGameDir(File dir) {
 	GeoQuestApp.runningGameDir = dir;
     }
 
     public static File getRunningGameDir() {
 	return GeoQuestApp.runningGameDir;
     }
 
     public static File getGameRessourceFile(String ressourceFilePath) {
 	String resourcePath = getRunningGameDir().getAbsolutePath() + "/"
 		+ ressourceFilePath;
 	File file = new File(resourcePath);
 	if (file.exists() && file.canRead())
 	    return file;
 	else
 	    throw new IllegalArgumentException(
 		    "No ressource file found at path \"" + resourcePath + "\".");
     }
 
     // SOUND STUFF FOLLOWS:
 
     private static MediaPlayer mPlayer = null;
 
     public static void cleanMediaPlayer() {
 	if (mPlayer != null && mPlayer.isLooping()) {
 	    Log.d(TAG,
 		  "MediaPlayer Resources were cleaned");
 	    mPlayer.stop();
 	    mPlayer.release();
 	}
     }
 
     public static void stopMediaPlayer() {
 	if (mPlayer != null && mPlayer.isPlaying()) {
 	    Log.d(TAG,
 		  "MediaPlayer was stoped");
 	    mPlayer.stop();
 	}
     }
 
     public static boolean mediaPlayerIsPlaying() {
 	if (mPlayer != null) {
 	    return mPlayer.isPlaying();
 	}
 	return false;
     }
 
     /**
      * Plays a resource sound file either blocking or non-blocking regarding the
      * user interaction options on the currently active mission or tool.
      * 
      * @param path
      *            is relative as specified in the game.xml (e.g.
      *            "sounds/beep.mp3").
      * @param blocking
      *            determines whether the interaction is blocked until the media
      *            file has been played completely.
      * @return false if player could not start for some reason.
      */
     public static boolean playAudio(String path,
 				    boolean blocking) {
 	stopAudio();
 	mPlayer = new MediaPlayer();
 	try {
 	    mPlayer.setDataSource(ResourceManager.getResourcePath(path));
 	    mPlayer.prepare();
 	    mPlayer.start();
 	    if (blocking)
 		blockInteractionOnCurrentActivityByMediaPlayer();
 	} catch (IllegalArgumentException e) {
 	    Log.e(TAG,
 		  "Could not start Media Player. " + e);
 	    return false;
 	} catch (IllegalStateException e) {
 	    Log.e(TAG,
 		  "Could not start Media Player. " + e);
 	    return false;
 	} catch (IOException e) {
 	    Log.e(TAG,
 		  "Could not start Media Player. " + e);
 	    return false;
 	}
 	return true;
     }
 
     public static void stopAudio() {
 	if (mPlayer != null) {
 	    if (mPlayer.isPlaying()) {
 		mPlayer.stop();
 	    }
 	    mPlayer.reset();
 	}
     }
 
     /**
      * Blocks the user interaction on the currently active mission or tool until
      * the media player signals completion. This is used for example to prevent
      * the user to click on proceed buttons before the audio file has not
      * completely played yet.
      */
     public static void blockInteractionOnCurrentActivityByMediaPlayer() {
 	final BlockableAndReleasable releaseCallBack = getCurrentActivity()
 		.blockInteraction(getInstance());
 	mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 
 	    public void onCompletion(MediaPlayer mp) {
 		// TODO Auto-generated method stub
 		releaseCallBack.releaseInteraction(getInstance());
 	    }
 	});
     }
 
     public boolean isInGame() {
 	return isInGame;
     }
 
     /*
      * TODO: Sollte dies nicht eine "live" Überprüfung der Verbindung zum Server
      * durchführen? (hm)
      */
     public boolean isRepoDataLoaded() {
 	return repoDataAvailable;
     }
 
     public static void resetAdaptionEngine() {
 	if (adaptionEngineLibAvailable) {
 	    Log.d(TAG,
 		  "Resetting AdaptionEngine.");
 	    useAdaptionEngine = true;
 	}
     }
 
     public void setInGame(boolean isInGame) {
 	this.isInGame = isInGame;
     }
 
     public void setRepoDataAvailable(boolean repoDataAvailable) {
 	this.repoDataAvailable = repoDataAvailable;
     }
 
     private static void includeAdaptionEngine() {
 	try {
 	    Class<?> adaptionEngineClass = Class
 		    .forName("edu.bonn.mobilegaming.geoquest.adaptionengine.AdaptionEngine");
 	    Object obj = adaptionEngineClass.newInstance();
 	    if (obj instanceof AdaptionEngineInterface) {
 		adaptionEngine = (AdaptionEngineInterface) obj;
 		useAdaptionEngine = true;
 		adaptionEngineLibAvailable = true;
 		Log.d(TAG,
 		      "AdaptionEngine was successfully integrated.");
 	    } else {
 		throw new Exception(
 			"Couldn't create instance of AdaptionEngine");
 	    }
 	} catch (Exception e) {
 	    Log.d(TAG,
 		  "AdaptionEngine wasn't integrated.");
 
 	}
     }
 
     /**
      * 
      * @param repositoryName
      *            the name of the repository directory you want the File to
      *            (optional parameter). If null is given, the root directory of
      *            all local game repositories is returned.
      * @return either the directory of the goven game repository or (if
      *         repositoryName is null) the root directory of all game
      *         repositories.
      * @deprecated use {@link GameDataManager#getLocalRepoDir(CharSequence)}
      *             instead.
      */
     static File getLocalRepoDir(CharSequence repositoryName) {
 	String relativePath = repoBaseDirPath();
 	File repoDir;
 	if (repositoryName == null) {
 	    repoDir = new File(Environment.getExternalStorageDirectory(),
 		    relativePath);
 	} else {
 	    repoDir = new File(Environment.getExternalStorageDirectory(),
 		    relativePath + repositoryName + "/");
 	}
 	return repoDir;
     }
 
     /**
      * @deprecated
      * 
      * @return the path of the directory containing all repositories.
      */
     static String repoBaseDirPath() {
 	return "/Android/data/" + getInstance().getPackageName()
 		+ "/repositories/";
     }
 
     public void refreshMapDisplay() {
 	if (googleMap != null)
 	    googleMap.postInvalidate();
 
 	if (osmap != null)
 	    osmap.postInvalidate();
     }
 
     // TODO -- Sabine -- location-Methoden löschen
     // public Location getLastKnownLocation() {
     // return lastKnownLocation;
     // }
     //
     // public void setLastKnownLocation(Location loc) {
     // this.lastKnownLocation = loc;
     // }
 }
