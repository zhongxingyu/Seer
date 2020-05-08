 package com.jeffthefate.dmbquiz;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map.Entry;
 import java.util.TimeZone;
 
 import org.apache.commons.lang3.StringUtils;
 import org.ardverk.collection.PatriciaTrie;
 import org.ardverk.collection.StringKeyAnalyzer;
 import org.ardverk.collection.Trie;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.annotation.SuppressLint;
 import android.app.Application;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.jeffthefate.stacktrace.ExceptionHandler;
 import com.jeffthefate.stacktrace.ExceptionHandler.OnStacktraceListener;
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseFacebookUtils;
 import com.parse.ParseFile;
 import com.parse.ParseInstallation;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseTwitterUtils;
 import com.parse.SaveCallback;
 
 /**
  * Used as a holder of many values and objects for the entire application.
  * 
  * @author Jeff Fate
  */
 @SuppressLint("ShowToast")
 public class ApplicationEx extends Application implements OnStacktraceListener {
 
     private static Context app;
     private static boolean mHasConnection = false;
     private static boolean mIsActive = false;
     private static ConnectivityManager connMan;
     /**
      * Path to the application's external cache
      */
     public static String cacheLocation = null;
     private static Toast mToast;
     /**
      * Last setlist reported, broken in to an
      * {@link java.util.ArrayList ArrayList} of Strings
      */
     public static ArrayList<String> setlistList;
     /**
      * Location of notification sound for the last reported song
      */
     public static Uri notificationSound;
     private static Trie<String, SongInfo> songMap;
     /**
      * Time date format for the updated time stamp
      */
     public static SimpleDateFormat df = new SimpleDateFormat("h:mm a zzz", Locale.getDefault());
     
     private static Bitmap portraitBackgroundBitmap;
     private static Bitmap landBackgroundBitmap;
     private static Bitmap portraitSetlistBitmap;
     private static Bitmap landSetlistBitmap;
     
     private static boolean isDownloading = false;
     
     private static float textViewHeight = 0.0f;
     
     private static int parseQueries = 0;
     
     /**
      * Holds an image and audio clip that are associated with each other
      * @author Jeff Fate
      */
     private static class SongInfo {
         private int image;
         private int audio;
         
         private SongInfo(int image, int audio) {
             this.image = image;
             this.audio = audio;
         }
         
         public int getImage() {
             return image;
         }
         
         public int getAudio() {
             return audio;
         }
     }
     
     /**
      * Singleton of the SharedPreferences used by the application
      * @author Jeff
      */
     public static class SharedPreferencesSingleton {
     	private SharedPreferencesSingleton() {}
     	
     	private static SharedPreferences sharedPrefs = null;
     	
     	/**
     	 * Get an instance, creating it if necessary, of the shared preferences
     	 * object for the application
     	 * @return shared preferences object for the application's preferences
     	 */
     	public static SharedPreferences instance() {
     		if (sharedPrefs == null)
     			sharedPrefs = PreferenceManager.getDefaultSharedPreferences(app);
     		return sharedPrefs;
     	}
     }
     
     /**
      * Singleton of the DatabaseHelper used by the application
      * @author Jeff
      */
     public static class DatabaseHelperSingleton {
     	private DatabaseHelperSingleton() {}
     	
     	private static DatabaseHelper dbHelper = null;
     	
     	/**
     	 * Get an instance, creating it if necessary, of the shared preferences
     	 * object for the application
     	 * @return shared preferences object for the application's preferences
     	 */
     	public static DatabaseHelper instance() {
     		if (dbHelper == null)
     			dbHelper = DatabaseHelper.getInstance();
     		return dbHelper;
     	}
     }
     
     /**
      * Singleton of the Resources used by the application
      * @author Jeff
      */
     public static class ResourcesSingleton {
     	private ResourcesSingleton() {}
     	
     	private static Resources res = null;
     	
     	/**
     	 * Get an instance, creating it if necessary, of the shared preferences
     	 * object for the application
     	 * @return shared preferences object for the application's preferences
     	 */
     	public static Resources instance() {
     		if (res == null)
     			res = getApp().getResources();
     		return res;
     	}
     }
     
     @SuppressLint("NewApi")
 	@Override
     public void onCreate() {
         super.onCreate();
         // make sure AsyncTask is loaded in the Main thread
         // https://code.google.com/p/android/issues/detail?id=20915
         new AsyncTask<Void, Void, Void>() {
             @Override
             protected Void doInBackground(Void... params) {
                 return null;
             }
         };
         app = this;
         mToast = Toast.makeText(app, "", Toast.LENGTH_LONG);
         String state = Environment.getExternalStorageState();
         if (Environment.MEDIA_MOUNTED.equals(state)) {
             cacheLocation = getExternalCacheDir().getAbsolutePath();
             File path = new File(cacheLocation + Constants.SCREENS_LOCATION);
             if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                 path.setExecutable(true, false);
                 path.setReadable(true, false);
                 path.setWritable(true, false);
             }
             path.mkdirs();
             path = new File(cacheLocation + Constants.AUDIO_LOCATION);
             if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                 path.setExecutable(true, false);
                 path.setReadable(true, false);
                 path.setWritable(true, false);
             }
             path.mkdirs();
         }
         /*
         Parse.initialize(this, "6pJz1oVHAwZ7tfOuvHfQCRz6AVKZzg1itFVfzx2q",
                 "2ocGkdBygVyNStd8gFQQgrDyxxZJCXt3K1GbRpMD");
         */
         Parse.initialize(this, "ImI8mt1EM3NhZNRqYZOyQpNSwlfsswW73mHsZV3R",
                 "hpTbnpuJ34zAFLnpOAXjH583rZGiYQVBWWvuXsTo");
         ParseFacebookUtils.initialize("463296083721286");
         ParseTwitterUtils.initialize("xWnkCrbGRNGMVs2HDyShQ",
                 "xaDerd1mUtfmjyuANARkuvNBrQFgsVpQmhYWDjnirOw");
         ExceptionHandler.register(this);
         connMan = ((ConnectivityManager) getSystemService(
                 Context.CONNECTIVITY_SERVICE));
         NetworkInfo nInfo = connMan.getActiveNetworkInfo();
         if (nInfo == null)
             mHasConnection = false;
         else
             mHasConnection = nInfo.isConnected();
         DatabaseHelperSingleton.instance().checkUpgrade();
         //setlist = "Jun 1 2013\nDave Matthews Band\nBlossom Music Center\nCuyahoga Falls, OH\n\nDancing Nancies ->\nWarehouse\nThe Idea Of You\nBelly Belly Nice\nSave Me\nCaptain\nSeven";
         //setlistStamp = "Updated:\n8:16 PDT";
         generateSongMap();
         ParseInstallation installation = ParseInstallation.getCurrentInstallation();
         // TODO Remove if memory issues resolved
         try {
         	installation.saveEventually();
         	ApplicationEx.addParseQuery();
         } catch (RuntimeException e) {}
         String notificationType = ResourcesSingleton.instance().getString(
         		R.string.notificationtype_key);
         try {
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
             SharedPreferencesSingleton.instance().edit().putInt(
             		notificationType,
             		SharedPreferencesSingleton.instance().getBoolean(
             				notificationType, false) ? 1 : 0)
             .commit();
         else
         	SharedPreferencesSingleton.instance().edit().putInt(
             		notificationType,
             		SharedPreferencesSingleton.instance().getBoolean(
             				notificationType, false) ? 1 : 0)
             .apply();
         } catch (ClassCastException e) {}
         getSetlist();
         /*
         if (SharedPreferencesSingleton.instance().getInt(notificationType, 0) ==
         		2 && !isDownloading())
 	        downloadSongClips(DatabaseHelperSingleton.instance()
 	        		.getNotificatationsToDownload());
 	    */
     }
     /**
      * Used by other classes to get the application's global context.
      * @return the context of the application
      */
     public static Context getApp() {
         return app;
     }
     
     @Override
     public void onStacktrace(String appPackage, String packageVersion,
             String deviceModel, String androidVersion, String stacktrace) {
         ParseObject object = new ParseObject("Log");
         object.put("androidVersion", androidVersion);
         object.put("appPackage", appPackage);
         object.put("deviceModel", deviceModel);
         object.put("packageVersion", packageVersion);
         object.put("stacktrace", stacktrace);
         try {
             object.saveInBackground();
             ApplicationEx.addParseQuery();
         } catch (ExceptionInInitializerError e) {};
     }
     
     /**
      * Reports question to Parse to indicate there is an error in the question
      * or answer
      * @param questionId	identifier in the Parse class
      * @param question		question text
      * @param answer		answer text
      * @param score			current score
      */
     public static void reportQuestion(String questionId, String question,
             String answer, String score) {
         if (questionId != null && question != null && answer != null &&
                 score != null) {
             ParseObject object = new ParseObject("Report");
             object.put("questionId", questionId);
             object.put("question", question);
             object.put("answer", answer);
             object.put("score", score);
             object.saveInBackground(new SaveCallback() {
                 @Override
                 public void done(ParseException arg0) {
                 	showLongToast("Report sent, thank you");
                 	ApplicationEx.addParseQuery();
                 }
             });
         }
         else
         	showLongToast("Report sent, thank you");
     }
     
     /**
      * Set indication if there is a network connection
      * @param hasConnection true if application is reporting a connection
      */
     public static void setConnection(boolean hasConnection) {
         mHasConnection = hasConnection;
     }
     
     /**
      * Reports if the application has a network connection
      * @return true if the application reports having a connection
      */
     public static boolean getConnection() {
         return mHasConnection;
     }
     
     /**
      * Remove all cached files for this application, including directories
      */
     public void clearApplicationData() {
         File cache = getCacheDir();
         File appDir = new File(cache.getParent());
         if (appDir.exists()) {
             String[] children = appDir.list();
             for (String s : children) {
                 if (!s.equals("lib"))
                     deleteDir(new File(appDir, s));
             }
         }
     }
     
     /**
      * Helper to delete a directory in file structure
      * @param dir directory to be deleted
      * @return true if delete was successful
      */
     public static boolean deleteDir(File dir) {
         if (dir != null && dir.isDirectory()) {
             String[] children = dir.list();
             for (int i = 0; i < children.length; i++) {
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success)
                     return false;
             }
         }
         return dir.delete();
     }
     
     /**
      * Determines if the app is currently active, visible to the user
      * @return true if active, false otherwise
      */
     public static boolean isActive() {
         return mIsActive;
     }
     
     /**
      * Indicate that the application is active, visible to user
      */
     public static void setActive() {
         mIsActive = true;
     }
     
     /**
      * Indicate that the application is not active, visible to user
      */
     public static void setInactive() {
         mIsActive = false;
     }
     
     /**
      * Set preference of {@link java.util.ArrayList ArrayList} of
      * {@link java.lang.String Strings} to key
      * @param key		corresponding to this preference
      * @param answers	{@link java.util.ArrayList ArrayList} of
      * 					{@link java.lang.String Strings} to assign to the key
      */
     public static void setStringArrayPref(String key,
             ArrayList<String> answers) {
         SharedPreferences.Editor editor = SharedPreferencesSingleton.instance().edit();
         if (answers == null)
             editor.remove(key);
         else {
             JSONArray array = new JSONArray(answers);
             if (!answers.isEmpty())
                 editor.putString(key, array.toString());
             else
                 editor.putString(key, null);
         }
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
         	editor.commit();
         else
         	editor.apply();
     }
     
     /**
      * Get string array preference given a specific key
      * @param key used to find preference
      * @return an {@link java.util.ArrayList ArrayList} of
      * 		   {@link java.lang.String Strings} containing the preference
      * 		   matching the given key
      */
     public static ArrayList<String> getStringArrayPref(String key) {
         String json = SharedPreferencesSingleton.instance().getString(key, null);
         ArrayList<String> answers = null;
         if (json != null) {
             answers = new ArrayList<String>();
             try {
                 JSONArray array = new JSONArray(json);
                 for (int i = 0; i < array.length(); i++) {
                     answers.add(array.optString(i));
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
         return answers;
     }
     
     /**
      * Display an application-wide toast message, with short timeout
      * @param message string to display
      */
     public static void showShortToast(String message) {
     	if (mToast != null) {
     		mToast.setText(message);
     		mToast.setDuration(Toast.LENGTH_SHORT);
     		mToast.show();
     	}
     }
     
     /**
      * Display an application-wide toast message, with long timeout
      * @param message string to display
      */
     public static void showLongToast(String message) {
     	if (mToast != null) {
     		mToast.setText(message);
     		mToast.setDuration(Toast.LENGTH_LONG);
     		mToast.show();
     	}
     }
     
     /**
      * Display an application-wide toast message, with long timeout
      * @param messageId resource id of string to display
      */
     public static void showLongToast(int messageId) {
     	showLongToast(app.getString(messageId));
     }
     
     /**
      * Fetch the most recent setlist from the Parse service, along with the last
      * updated time and send broadcast to any receivers that there is a new
      * setlist to show.
      */
     public static void getSetlist() {
         ParseQuery setlistQuery = new ParseQuery("Setlist");
         setlistQuery.addDescendingOrder("setDate");
         setlistQuery.setLimit(1);
         //setlistQuery.setSkip(5);
         setlistQuery.findInBackground(new FindCallback() {
             @Override
             public void done(List<ParseObject> setlists, ParseException e) {
             	String setlist = "Error downloading setlist";
                 Intent intent = new Intent(Constants.ACTION_UPDATE_SETLIST);
                 if (e != null) {
                     Log.e(Constants.LOG_TAG, "Error getting setlist!", e);
                     intent.putExtra("success", false);
                 }
                 else {
                     setlist = setlists.get(0).getString("set");
                     df.setTimeZone(TimeZone.getDefault());
                     StringBuilder sb = new StringBuilder();
                     sb.append("Updated:\n");
                     sb.append(DateFormat.format(df.toLocalizedPattern(), setlists.get(0).getUpdatedAt()));
                     Editor editor = SharedPreferencesSingleton.instance()
                     		.edit();
                     editor.putString(ResourcesSingleton.instance().getString(
                     		R.string.setlist_key), setlist);
                     editor.putString(ResourcesSingleton.instance().getString(
                     		R.string.setstamp_key), sb.toString());
                     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                     	editor.commit();
                     else
                     	editor.apply();
                     parseSetlist(setlist);
                     intent.putExtra("success", true);
                 }
                 app.sendBroadcast(intent);
                 ApplicationEx.addParseQuery();
             }
         });
     }
     
     /**
      * Create a string list, separating each line of the setlist to an
      * individual string
      */
     public static void parseSetlist(String setlist) {
         setlistList = new ArrayList<String>(Arrays.asList(setlist.split("\n")));
     }
     
     /**
      * Make the URI for the audio to add to the notification
      * @param soundId resource id of the audio
      */
     public static void createNotificationUri(int soundId) {
         StringBuilder sb = new StringBuilder();
         sb.append("android.resource://");
         sb.append(app.getPackageName());
         sb.append("/");
         sb.append(soundId);
         notificationSound = Uri.parse(sb.toString());
     }
     
     /**
      * Make the URI for the audio to add to the notification
      * @param soundPath path of the audio
      */
     public static void createNotificationUri(String soundPath) {
     	File file = new File(soundPath);
     	if (file.exists())
     		notificationSound = Uri.parse(soundPath);
     	else
     		createNotificationUri(R.raw.general);
     }
     
     /**
      * Create the map that associates song titles to images and audio for the
      * notifications
      */
     private static void generateSongMap() {
         songMap = new PatriciaTrie<String, SongInfo>(StringKeyAnalyzer.CHAR);
         
         songMap.put("belly belly nice", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         songMap.put("belly full", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         songMap.put("broken things", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         songMap.put("drunken soldier", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         songMap.put("gaucho", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         songMap.put("if only", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         songMap.put("mercy", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         songMap.put("the riff", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         songMap.put("rooftop", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         songMap.put("snow outside", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         songMap.put("sweet", new SongInfo(R.drawable.away_from_the_world, R.raw.aftw));
         
         songMap.put("#35", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("alligator pie", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("baby blue", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("dive in", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("funny the way it is", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("grux", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("lying in the hands of god", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("seven", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("shake me like a monkey", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("spaceman", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("squirm", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("time bomb", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("why i am", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         songMap.put("you and me", new SongInfo(R.drawable.big_whiskey, R.raw.bw));
         
         songMap.put("american baby", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("american baby intro", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("dreamgirl", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("dream girl", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("everybody wake up", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("hello again", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("hunger for the great light", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("louisiana bayou", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("old dirt hill", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("out of my hands", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("smooth rider", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("stand up", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("steady as we go", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("stolen away on 55th & 3rd", new SongInfo(R.drawable.stand_up, R.raw.standup));
         songMap.put("you might die trying", new SongInfo(R.drawable.stand_up, R.raw.standup));
         
         songMap.put("an' another thing", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("baby", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("dodo", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("gravedigger", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("grey blue eyes", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("oh", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("save me", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("so damn lucky", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("some devil", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("stay or leave", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("too high", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("trouble", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         songMap.put("up and away", new SongInfo(R.drawable.some_devil, R.raw.somedevil));
         
         songMap.put("bartender", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         songMap.put("big eyed fish", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         songMap.put("busted stuff", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         songMap.put("captain", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         songMap.put("digging a ditch", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         songMap.put("grace is gone", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         songMap.put("grey street", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         songMap.put("kit kat jam", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         songMap.put("raven", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         songMap.put("where are you going", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         songMap.put("you never know", new SongInfo(R.drawable.busted_stuff, R.raw.bs));
         
         songMap.put("angel", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("dreams of our fathers", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("everyday", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("fool to think", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("i did it", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("if i had it all", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("mother father", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("sleep to dream her", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("so right", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("the space between", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("what you are", new SongInfo(R.drawable.everyday, R.raw.everyday));
         songMap.put("when the world ends", new SongInfo(R.drawable.everyday, R.raw.everyday));
         
         songMap.put("crush", new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         songMap.put("don't drink the water",
                 new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         songMap.put("dreaming tree", new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         songMap.put("halloween", new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         songMap.put("last stop", new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         songMap.put("pantala naga pampa",
                 new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         songMap.put("pig", new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         songMap.put("rapunzel", new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         songMap.put("spoon", new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         songMap.put("stay", new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         songMap.put("the stone", new SongInfo(R.drawable.before_these_crowded_streets, R.raw.btcs));
         
         songMap.put("#41", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("crash into me", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("cry freedom", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("drive in drive out", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("let you down", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("lie in our graves", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("proudest monkey", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("say goodbye", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("so much to say", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("too much", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("tripping billies", new SongInfo(R.drawable.crash, R.raw.crash));
         songMap.put("two step", new SongInfo(R.drawable.crash, R.raw.crash));
         
         songMap.put("#34", new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("ants marching", new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("best of whats around",
                 new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("dancing nancies", new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("jimi thing", new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("lover lay down", new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("pay for what you get",
                 new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("rhyme and reason", new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("satellite", new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("typical situation",
                 new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("warehouse", new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         songMap.put("what would you say",
                 new SongInfo(R.drawable.under_the_table_and_dreaming, R.raw.uttad));
         
         songMap.put("christmas song", new SongInfo(R.drawable.remember_two_things, R.raw.r2t));
         songMap.put("i'll back you up", new SongInfo(R.drawable.remember_two_things, R.raw.r2t));
         songMap.put("minarets", new SongInfo(R.drawable.remember_two_things, R.raw.r2t));
         songMap.put("one sweet world", new SongInfo(R.drawable.remember_two_things, R.raw.r2t));
         songMap.put("recently", new SongInfo(R.drawable.remember_two_things, R.raw.r2t));
         songMap.put("seek up", new SongInfo(R.drawable.remember_two_things, R.raw.r2t));
         songMap.put("the song that jane likes", new SongInfo(R.drawable.remember_two_things, R.raw.r2t));
         
         songMap.put("encore", new SongInfo(R.drawable.notification_large, R.raw.endofset));
         
         for (Entry<String, SongInfo> entry : songMap.entrySet()) {
         	String songName = entry.getKey();
         	SongInfo info = entry.getValue();
         	String songFile = StringUtils.remove(
         			StringUtils.remove(
         					StringUtils.remove(songName, " "), "'"), "#");
         	if (!DatabaseHelperSingleton.instance().hasNotificationSong(
         			songFile))
         		DatabaseHelperSingleton.instance().addNotification(songFile,
         				info.getImage(), info.getAudio());
         	else
         		DatabaseHelperSingleton.instance().updateNotification(songFile,
         				info.getImage(), info.getAudio());
         }
     }
     
     /**
      * Get the image that matches the given song title for the notification
      * @param songTitle	title of the song to match
      * @return id for the image resource that matches
      */
     public static int findMatchingImage(String songTitle) {
         songTitle = StringUtils.remove(songTitle, "*");
         songTitle = StringUtils.remove(songTitle, "+");
         songTitle = StringUtils.remove(songTitle, "~");
         songTitle = StringUtils.remove(songTitle, "�");
         songTitle = StringUtils.remove(songTitle, "#");
         songTitle = StringUtils.remove(songTitle, "-");
         songTitle = StringUtils.remove(songTitle, ">");
         songTitle = StringUtils.strip(songTitle);
         songTitle = StringUtils.lowerCase(songTitle, Locale.ENGLISH);
         songTitle = StringUtils.remove(songTitle, " ");
     	songTitle = StringUtils.remove(songTitle, "'");
         return DatabaseHelperSingleton.instance().getNotificationImage(
         		songTitle);
     }
     
     /**
      * Get the audio that matches the current song for the notification.
      * @param res		resources object to retrieve the audio from
      * @param songTitle	title of song to match
      * @return id for the audio that matches
      */
     public static void findMatchingAudio(String songTitle) {
         songTitle = StringUtils.remove(songTitle, "*");
         songTitle = StringUtils.remove(songTitle, "+");
         songTitle = StringUtils.remove(songTitle, "~");
         songTitle = StringUtils.remove(songTitle, "�");
         songTitle = StringUtils.remove(songTitle, "(");
         songTitle = StringUtils.strip(songTitle);
         songTitle = StringUtils.lowerCase(songTitle, Locale.ENGLISH);
         Entry<String, SongInfo> entry = songMap.select(songTitle);
         switch (SharedPreferencesSingleton.instance().getInt(
                 ResourcesSingleton.instance().getString(
                 		R.string.notificationtype_key), 0)) {
         case 0:
         	Log.i(Constants.LOG_TAG, "GENERAL AUDIO");
         	ApplicationEx.createNotificationUri(R.raw.general);
         	break;
         case 1:
         	Log.i(Constants.LOG_TAG, "ALBUM AUDIO");
         	Log.i(Constants.LOG_TAG, "SONG TITLE: " + songTitle);
         	Log.i(Constants.LOG_TAG, "ENTRY: " + entry.getKey());
         	if (songTitle.startsWith(entry.getKey()))
         		ApplicationEx.createNotificationUri(entry.getValue().getAudio());
         	else
         		ApplicationEx.createNotificationUri(R.raw.general);
         	break;
         /*
         case 2:
         	Log.i(Constants.LOG_TAG, "SONG AUDIO");
         	songTitle = StringUtils.remove(songTitle, " ");
         	songTitle = StringUtils.remove(songTitle, "'");
         	StringBuilder sb = new StringBuilder();
         	sb.append(cacheLocation);
         	sb.append(Constants.AUDIO_LOCATION);
         	sb.append(songTitle);
         	sb.append(".mp3");
         	ApplicationEx.createNotificationUri(sb.toString());
         	break;
         */
     	default:
     		ApplicationEx.createNotificationUri(R.raw.general);
         	break;
         }
     }
     
     /**
      * Resizes image for the large notification image.
      * @param res	resources object to retrieve the bitmap from
      * @param resId	id of the image to create bitmap from
      * @return bitmap to be used as the large notification image
      */
     @SuppressLint("InlinedApi")
 	public static Bitmap resizeImage(Resources res, int resId) {
         Bitmap bitmap = null;
         try {
         	bitmap = BitmapFactory.decodeResource(res, resId);
         } catch (OutOfMemoryError e) {
         	return BitmapFactory.decodeResource(res, R.drawable.notification_large);
         }
         if (resId == R.drawable.notification_large)
             return bitmap;
         double ratio = (double) ((double)bitmap.getHeight() / 
                 (double)bitmap.getWidth());
         Bitmap smallBitmap;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
             smallBitmap = Bitmap.createScaledBitmap(bitmap,
                     res.getDimensionPixelSize(
                             android.R.dimen.notification_large_icon_width), 
                     (int) (ratio < 1 ? res.getDimensionPixelSize(
                         android.R.dimen.notification_large_icon_height)*ratio :
                         res.getDimensionPixelSize(
                             android.R.dimen.notification_large_icon_height)),
                     true);
         else
             smallBitmap = Bitmap.createScaledBitmap(bitmap,
                     res.getDimensionPixelSize(
                             R.dimen.notification_large_icon_width), 
                     (int) (ratio < 1 ? res.getDimensionPixelSize(
                         R.dimen.notification_large_icon_height)*ratio :
                         res.getDimensionPixelSize(
                             R.dimen.notification_large_icon_height)),
                     true);
         return smallBitmap;
     }
     
     /**
      * Set the current drawable for login, question and stats background,
      * specific to the orientation.
      * @param backgroundDrawable	drawable for the current orientation
      */
     public static void setBackgroundBitmap(Bitmap backgroundDrawable) {
         switch(ResourcesSingleton.instance().getConfiguration().orientation) {
         case Configuration.ORIENTATION_PORTRAIT:
             ApplicationEx.portraitBackgroundBitmap = backgroundDrawable;
             break;
         case Configuration.ORIENTATION_LANDSCAPE:
             ApplicationEx.landBackgroundBitmap = backgroundDrawable;
             break;
         default:
             break;
         }
     }
     
     /**
      * Current drawable for the login, question and stats background.  Both
      * portrait and landscape are held here to reduce work when rotating.
      * @return current drawable for the login, question and stats background
      */
     public static Bitmap getBackgroundBitmap() {
         switch(ResourcesSingleton.instance().getConfiguration().orientation) {
         case Configuration.ORIENTATION_PORTRAIT:
             return ApplicationEx.portraitBackgroundBitmap;
         case Configuration.ORIENTATION_LANDSCAPE:
             return ApplicationEx.landBackgroundBitmap;
         default:
             return ApplicationEx.portraitBackgroundBitmap;
         }
     }
     
     /**
      * Set the current drawable for setlist background, specific to the
      * orientation.
      * @param setlistDrawable	drawable for the current orientation
      */
     public static void setSetlistBitmap(Bitmap setlistDrawable) {
         switch(ResourcesSingleton.instance().getConfiguration().orientation) {
         case Configuration.ORIENTATION_PORTRAIT:
             ApplicationEx.portraitSetlistBitmap = setlistDrawable;
             break;
         case Configuration.ORIENTATION_LANDSCAPE:
             ApplicationEx.landSetlistBitmap = setlistDrawable;
             break;
         default:
             break;
         }
     }
     
     /**
      * Current drawable for the setlist background.  Both portrait and landscape
      * are held here to reduce work when rotating.
      * @return current drawable for the setlist background
      */
     public static Bitmap getSetlistBitmap() {
         switch(ResourcesSingleton.instance().getConfiguration().orientation) {
         case Configuration.ORIENTATION_PORTRAIT:
             return ApplicationEx.portraitSetlistBitmap;
         case Configuration.ORIENTATION_LANDSCAPE:
             return ApplicationEx.landSetlistBitmap;
         default:
             return ApplicationEx.portraitSetlistBitmap;
         }
     }
     
     public static void downloadSongClips(List<String> songs) {
     	isDownloading = true;
     	SongDownloadTask songDownloadTask = new SongDownloadTask(songs);
     	if (Build.VERSION.SDK_INT <
                 Build.VERSION_CODES.HONEYCOMB)
         	songDownloadTask.execute();
         else
         	songDownloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
     }
     
     private static class SongDownloadTask extends AsyncTask<Void, Void, Void> {
     	List<String> songs;
     	
     	private SongDownloadTask(List<String> songs) {
     		this.songs = songs;
     	}
     	
     	@Override
         protected Void doInBackground(Void... nothing) {
     		FileOutputStream fout = null;
             File audioFile = null;
             StringBuilder sb = new StringBuilder();
     		ParseFile file = null;
     		ParseQuery query = new ParseQuery("Audio");
     		if (songs != null)
     			query.whereContainedIn("name", songs);
     		try {
 				List<ParseObject> audioList = query.find();
 				String name = "";
 				for (ParseObject audio : audioList) {
 					name = audio.getString("name");
 					DatabaseHelperSingleton.instance()
 							.songNotificationDownloaded(name, false);
 					sb.setLength(0);
 					sb.append(cacheLocation);
 					sb.append(Constants.AUDIO_LOCATION);
 					sb.append(name);
 					sb.append(".mp3");
 					audioFile = new File(sb.toString());
 					file = (ParseFile) audio.get("file");
 					try {
 						fout = new FileOutputStream(audioFile);
 						fout.write(file.getData());
 						fout.flush();
 						fout.close();
 						DatabaseHelperSingleton.instance()
 								.songNotificationDownloaded(name, true);
 					} catch (ParseException e) {
 						Log.e(Constants.LOG_TAG, "Couldn't get data from " +
 								"ParseFile " + audio.getString("name"), e);
 						if (e.getCode() == ParseException.CONNECTION_FAILED) {
 							DatabaseHelperSingleton.instance()
 									.songNotificationDownloaded(name, false);
 						}
 					} catch (FileNotFoundException e) {
 						Log.e(Constants.LOG_TAG, sb.toString() + " not found!",
 								e);
 					} catch (IOException e) {
 						Log.e(Constants.LOG_TAG, "Can't write to " +
 								sb.toString(), e);
 					}
 	    		}
 			} catch (ParseException e) {
 				Log.e(Constants.LOG_TAG, "Couldn't find audio!", e);
 			}
     		isDownloading = false;
     		return null;
     	}
     }
     
     public static boolean isDownloading() {
     	return isDownloading;
     }
     
 	public static float getTextViewHeight() {
 		return textViewHeight;
 	}
 	
 	public static void setTextViewHeight(float textViewHeight) {
 		ApplicationEx.textViewHeight = textViewHeight;
 	}
 	
 	public static int getParseQueries() {
 		return parseQueries;
 	}
 	
 	public static void setParseQueries(int newParseQueries) {
 		parseQueries = newParseQueries;
 	}
 	
 	public static void addParseQuery() {
 		parseQueries++;
 		Log.v(Constants.LOG_TAG, "ADDING API CALL: " + parseQueries);
 	}
 
 }
