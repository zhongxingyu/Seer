 package no.hials.muldvarp.v2;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Binder;
 import android.os.IBinder;
 import android.support.v4.content.LocalBroadcastManager;
 import java.util.ArrayList;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.domain.*;
 import no.hials.muldvarp.v2.utility.NewDownloadTask;
 import no.hials.muldvarp.v2.utility.ServerConnection;
 
 /**
  *
  * @author kristoffer
  */
 public class MuldvarpService extends Service {
     public static final String ACTION_COURSE_UPDATE       = "no.hials.muldvarp.ACTION_COURSE_UPDATE";
     public static final String ACTION_PROGRAMMES_UPDATE   = "no.hials.muldvarp.ACTION_PROGRAMMES_UPDATE";
     public static final String ACTION_UPDATE_FAILED       = "no.hials.muldvarp.ACTION_UPDATE_FAILED";
     public static final String SERVER_NOT_AVAILABLE       = "no.hials.muldvarp.SERVER_NOT_AVAILABLE";
     public static final String ACTION_ALL_UPDATE          = "no.hials.muldvarp.ACTION_ALL_UPDATE";
     public static final String ACTION_FRONTPAGE_UPDATE    = "no.hials.muldvarp.ACTION_FRONTPAGE_UPDATE";
     public static final String ACTION_NEWS_UPDATE         = "no.hials.muldvarp.ACTION_NEWS_UPDATE";
 
     private User user;
 
     public ArrayList<Domain> mProgrammes;
     public Course selectedCourse;
     public Programme selectedProgramme;
     public ArrayList<Domain> mNews;
     public Domain frontpage;
 
     // Binder given to clients
     private final IBinder mBinder = new LocalBinder();
 
     int mStartMode;       // indicates how to behave if the service is killed
     boolean mAllowRebind; // indicates whether onRebind should be used
     Integer courseId;
     SharedPreferences preferences;
     LocalBroadcastManager mLocalBroadcastManager;
     BroadcastReceiver     mReceiver;
     ServerConnection server;
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         return super.onStartCommand(intent, flags, startId);
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
//        initializeData();
         System.out.println("Service created");
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return mBinder;
     }
 
     @Override
     public boolean onUnbind(Intent intent) {
         // All clients have unbound with unbindService()
         return mAllowRebind;
     }
 
 //    @Override
 //    public void onRebind(Intent intent) {
 //        // A client is binding to the service with bindService(),
 //        // after onUnbind() has already been called
 //    }
 //
 //    @Override
 //    public void onDestroy() {
 //        // The service is no longer used and is being destroyed
 //    }
 
     /**
      * Class for clients to access. Because we know this service always runs in
      * the same process as its clients, we don't need to deal with IPC.
      */
     public class LocalBinder extends Binder {
         public MuldvarpService getService() {
             return MuldvarpService.this;
         }
     }
 
     private String getYoutubeUserUploadsURL(String user){
 
         return getString(R.string.youtubeAPIPath) + "users/" + user + "/uploads?alt=json";
     }
 
     public Domain getFrontpage() {
         return frontpage;
     }
 
     public void setFrontpage(Domain frontpage) {
         this.frontpage = frontpage;
     }
 
     public ArrayList<Domain> getmNews() {
         return mNews;
     }
 
     public void setmNews(ArrayList<Domain> mNews) {
         this.mNews = mNews;
     }
 
     public ArrayList<Domain> getmProgrammes() {
         return mProgrammes;
     }
 
     public void setmProgrammes(ArrayList<Domain> mProgrammes) {
         this.mProgrammes = mProgrammes;
     }
 
     public Course getSelectedCourse() {
         return selectedCourse;
     }
 
     public void setSelectedCourse(Course selectedCourse) {
         this.selectedCourse = selectedCourse;
     }
 
     public Programme getSelectedProgramme() {
         return selectedProgramme;
     }
 
     public void setSelectedProgramme(Programme selectedProgramme) {
         this.selectedProgramme = selectedProgramme;
     }
 
     /**
      * Method login, of class MuldvarpService.
      * This method is used to login to the server remotely. Input the username and password, and the method returns a boolean which is true if the info is correct.
      * Note: pr. 26.09.2012, this method returns true regardless of what info you input.
      * @param name
      * @param password
      * @return authentification
      */
         public boolean login(String name, String password){
         if(checkCredentials(name, password)) {
             user = new User(name, password);
             return true;
         }
         else {
             return false;
         }
     }
 
         public void reLog(User person){
             user = person;
         }
 
     /**
      * implement security here!
      * @param id
      * @param password
      * @return
      */
     public boolean checkCredentials(String username, String password){
         return true;
     }
 
    /**
     * Method getUser, of class MuldvarpService.
     * This method returns the user object reference when called.
     * Note that this can only be called when the user is already logged in, or the method will return null.
     * @return Person_v2
     */
     public User getUser(){
         return user;
     }
 
     public enum DataTypes {COURSES, VIDEOS, DOCUMENTS, PROGRAMS, ARTICLE, NEWS, QUIZ, FRONTPAGE}
 
     /**
      * Download/Update frontpage data
      */
     public void initializeData() {
         new NewDownloadTask(this,new Intent(ACTION_FRONTPAGE_UPDATE), DataTypes.FRONTPAGE, this)
                 .execute(getUrl(R.string.frontpageResPath));
         new NewDownloadTask(this,new Intent(ACTION_ALL_UPDATE), DataTypes.PROGRAMS, this)
                 .execute(getUrl(R.string.programmesResPath));
         new NewDownloadTask(this,new Intent(ACTION_NEWS_UPDATE), DataTypes.NEWS, this)
                             .execute(getUrl(R.string.newsResPath));
     }
 
     /**
      * Downloads/Updates a single domain item
      *
      * @param type
      * @param itemId
      */
     public synchronized void updateSingleItem(DataTypes type, int itemId) {
 //        if(server.checkServer()) {
             switch(type) {
                 case COURSES:
                     new NewDownloadTask(this,new Intent(ACTION_COURSE_UPDATE), type, itemId, this)
                             .execute(getUrl(R.string.courseResPath) + itemId);
                     break;
                 case PROGRAMS:
                     new NewDownloadTask(this,new Intent(ACTION_PROGRAMMES_UPDATE), type, itemId, this)
                             .execute(getUrl(R.string.programmesResPath) + itemId);
                     break;
             }
 //        }
     }
 
     public String getUrl(int resId) {
         return getString(R.string.serverPath) + getString(resId);
     }
     
 }
