 /**
  *
  * @author David Findley (ThinksInBits)
  * 
  * The source for this application may be found in its entirety at 
  * https://github.com/ThinksInBits/OU-Mobile-App
  * 
  * This application is published on the Google Play Store under
  * the title: OU Mobile Alpha:
  * https://play.google.com/store/apps/details?id=com.geared.ou
  * 
  * If you want to follow the official development of this application
  * then check out my Trello board for the project at:
  * https://trello.com/board/ou-app/4f1f697a28390abb75008a97
  * 
  * Please email me at: thefindley@gmail.com with questions.
  * 
  */
 
 package com.geared.ou;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 import android.preference.PreferenceManager;
 import com.geared.ou.D2LSourceGetter.SGError;
 import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
 
 /**
  *
  * This is the Application object. This class is critical to the function of
  * the application. All of the applications persistant data that activities use
  * is stored in this object. When this object is created, many of the applications
  * primary data structures are created.
  */
 public class OUApplication extends Application {
 	
 	public static final int FRAGMENT_NEWS = 1;
 	public static final int FRAGMENT_CLASSES = 2;
 	public static final int FRAGMENT_CLASS = 3;
 	public static final int FRAGMENT_GRADES = 4;
 	public static final int FRAGMENT_CONTENT = 5;
 	public static final int FRAGMENT_ROSTER = 6;
 	public static final int FRAGMENT_MAP = 7;
 	
 	
     private ClassesData classes;
     private SharedPreferences prefs;
     private D2LSourceGetter sourceGetter;
     private DbHelper dbHelper;
     private SyndFeed feed;
     private int currentFragment;
     
     @Override
     public void onCreate() {
         super.onCreate();
         classes = new ClassesData(this);
         sourceGetter = new D2LSourceGetter();
         prefs = PreferenceManager.getDefaultSharedPreferences(this);
         sourceGetter.setCredentials(prefs.getString("username", ""), prefs.getString("password", ""));
         dbHelper = new DbHelper(this);
         currentFragment = FRAGMENT_NEWS;
     }
     
     public SGError updateClasses(ClassesActivity context) //Async this function!
     {
         return classes.update(sourceGetter);
     }
     
     public SQLiteDatabase getDb()
     {
         return dbHelper.getWritableDatabase();
     }
     
     public SharedPreferences getPrefs()
     {
         return PreferenceManager.getDefaultSharedPreferences(this);
     }
     
     public void updateSGCredentials()
     {
         SharedPreferences p = getPrefs();
         sourceGetter.setCredentials(p.getString("username", ""), p.getString("password",""));
     }
     
     public ClassesData getClasses()
     {
         return classes;
     }
     
     public D2LSourceGetter getSourceGetter()
     {
         return sourceGetter;
     }
     
     public String getUser()
     {
         SharedPreferences p = getPrefs();
         return p.getString("username", "");
     }
     
     public SyndFeed getFeed() {
         return feed;
     }
     
     public void setFeed(SyndFeed feed) {
         this.feed = feed;
     }
     
     public int getCurrentFragment()
     {
     	return currentFragment;
     }
     
     public void setCurrentFragment(int f)
     {
     	currentFragment = f;
     }
 
     @Override
     public void onTerminate() {
         super.onTerminate();
         dbHelper.close();
     }
 }
