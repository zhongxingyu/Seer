 package no.hials.muldvarp.v2;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Binder;
 import android.os.IBinder;
 import android.support.v4.content.LocalBroadcastManager;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.database.MuldvarpDataSource;
 import no.hials.muldvarp.v2.domain.*;
 import no.hials.muldvarp.v2.utility.DownloadTask;
 import no.hials.muldvarp.v2.utility.DummyDataProvider;
 import no.hials.muldvarp.v2.utility.NewDownloadTask;
 import no.hials.muldvarp.v2.utility.ServerConnection;
 
 /**
  *
  * @author kristoffer
  */
 public class MuldvarpService extends Service {
     public static final String ACTION_PEOPLE_UPDATE       = "no.hials.muldvarp.ACTION_PEOPLE_UPDATE";
     public static final String ACTION_COURSE_UPDATE       = "no.hials.muldvarp.ACTION_COURSE_UPDATE";
     public static final String ACTION_SINGLECOURSE_UPDATE = "no.hials.muldvarp.ACTION_SINGLECOURSE_UPDATE";
     public static final String ACTION_LIBRARY_UPDATE      = "no.hials.muldvarp.ACTION_LIBRARY_UPDATE";
     public static final String ACTION_PROGRAMMES_UPDATE   = "no.hials.muldvarp.ACTION_PROGRAMMES_UPDATE";
     public static final String ACTION_PROGRAMMES_LOAD     = "no.hials.muldvarp.ACTION_PROGRAMMES_LOAD";
     public static final String ACTION_UPDATE_FAILED       = "no.hials.muldvarp.ACTION_UPDATE_FAILED";
     public static final String SERVER_NOT_AVAILABLE       = "no.hials.muldvarp.SERVER_NOT_AVAILABLE";
     public static final String ACTION_ARTICLE_UPDATE      = "no.hials.muldvarp.ACTION_ARTICLE_UPDATE";
     public static final String ACTION_NEWS_UPDATE         = "no.hials.muldvarp.ACTION_NEWS_UPDATE";
     public static final String ACTION_ALL_UPDATE          = "no.hials.muldvarp.ACTION_ALL_UPDATE";
     public static final String ACTION_ALL_UPDATING        = "no.hials.muldvarp.ACTION_ALL_UPDATING";
     public static final String ACTION_VIDEO_UPDATE        = "no.hials.muldvarp.ACTION_VIDEO_UPDATE";
     public static final String ACTION_QUIZ_UPDATE         = "no.hials.muldvarp.ACTION_QUIZ_UPDATE";
 
     private User user;
 
     private MuldvarpDataSource mds = new MuldvarpDataSource(this);
     public ArrayList<Domain> mProgrammes;
     public ArrayList<Domain> mCourses;
     public ArrayList<Domain> mQuizzes;
     public ArrayList<Domain> mDocuments;
     public ArrayList<Domain> mArticles;
     public ArrayList<Domain> mVideos;
     public ArrayList<Domain> mNews;
     public Domain mArticle;
     public ArrayList<Programme> mDomainItems;
 
     // Binder given to clients
     private final IBinder mBinder = new LocalBinder();
 
     int mStartMode;       // indicates how to behave if the service is killed
     boolean mAllowRebind; // indicates whether onRebind should be used
     Integer courseId;
     SharedPreferences preferences;
     LocalBroadcastManager mLocalBroadcastManager;
     BroadcastReceiver     mReceiver;
     int initilizeDataCounter;
     ServerConnection server;
     private String header;
 
     @Override
     public void onCreate() {
         // The service is being created
         super.onCreate();
         mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
         server = new ServerConnection(this);
         mDomainItems = new ArrayList<Programme>();
         mNews = new ArrayList<Domain>();
         mProgrammes = new ArrayList<Domain>();
         mCourses = new ArrayList<Domain>();
         mQuizzes = new ArrayList<Domain>();
         mDocuments = new ArrayList<Domain>();
         mArticles = new ArrayList<Domain>();
         mVideos = new ArrayList<Domain>();
         mNews = new ArrayList<Domain>();
         mArticle = new Domain();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         return super.onStartCommand(intent, flags, startId);
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
 
     @Override
     public void onRebind(Intent intent) {
         // A client is binding to the service with bindService(),
         // after onUnbind() has already been called
     }
 
     @Override
     public void onDestroy() {
         // The service is no longer used and is being destroyed
     }
 
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
     
     public ArrayList<Programme> getProgrammes(){
         return mDomainItems;
     }
 
     public Domain getmArticle() {
         return mArticle;
     }
 
     public void setmArticle(Domain mArticle) {
         this.mArticle = mArticle;
     }
 
     public ArrayList<Domain> getmArticles() {
         return mArticles;
     }
 
     public void setmArticles(ArrayList<Domain> mArticles) {
         this.mArticles = mArticles;
     }
 
     public ArrayList<Domain> getmCourses() {
         return mCourses;
     }
 
     public void setmCourses(ArrayList<Domain> mCourses) {
         this.mCourses = mCourses;
     }
 
     public ArrayList<Domain> getmDocuments() {
         return mDocuments;
     }
 
     public void setmDocuments(ArrayList<Domain> mDocuments) {
         this.mDocuments = mDocuments;
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
 
     public ArrayList<Domain> getQuizzes() {
         if(mQuizzes == null) {
             mQuizzes = new ArrayList<Domain>();
         }
         
         return mQuizzes;
     }
 
     public void setQuizzes(ArrayList<Domain> mQuizzes) {
         this.mQuizzes = mQuizzes;
     }
 
     public ArrayList<Domain> getmVideos() {
         return mVideos;
     }
 
     public void setmVideos(ArrayList<Domain> mVideos) {
         this.mVideos = mVideos;
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
 
     /**
      * Method requested, of class MuldvarpService.
      * This method updates the requested part of the local database from the server.
      * The request argument indicates which part of the database will be updated.
      * @param requested
      */
     public enum DataTypes {COURSES, VIDEOS, DOCUMENTS, PROGRAMS, ARTICLE, NEWS, QUIZ}
 
     /**
      * Updates all items related to some other item
      *
      * @param type
      * @param id
      */
     public synchronized void update(DataTypes type, int id) {
         if(true) {
             switch(type) {
                 case COURSES:
                     new NewDownloadTask(this,new Intent(ACTION_COURSE_UPDATE), type, id, this)
                             .execute(getUrl(R.string.programmeCourseResPath) + id);
                     break;
                 case VIDEOS:
                     new NewDownloadTask(this,new Intent(ACTION_VIDEO_UPDATE), type, this)
                             .execute(getUrl(R.string.videoResPath));
                     break;
                 case DOCUMENTS:
                     new NewDownloadTask(this,new Intent(ACTION_LIBRARY_UPDATE), type, this)
                             .execute(getUrl(R.string.libraryResPath));
                     break;
                 case PROGRAMS:
                     new NewDownloadTask(this,new Intent(ACTION_PROGRAMMES_UPDATE), type, this)
                             .execute(getUrl(R.string.programmesResPath));
                     break;
                 case NEWS:
                     new NewDownloadTask(this,new Intent(ACTION_NEWS_UPDATE), type, id, this)
                             .execute(getUrl(R.string.newsResPath));
                     break;
                 case QUIZ:
                     new NewDownloadTask(this,new Intent(ACTION_QUIZ_UPDATE), type, id, this)
                            .execute(getUrl(R.string.quizResPath));
 //                    ArrayList<Domain> as = DummyDataProvider.getQuizList();
 //                    for (int i = 0; i < as.size(); i++) {
 //                        mds.open();
 //                        long insertid = mds.insertQuiz((Quiz)as.get(i));
 //                        mds.addTopQuiz(insertid);
 //                    }
                     break;
             }
         }
     }
 
     /**
      * Download/Update frontpage data
      */
     public void initializeData() {
         initilizeDataCounter = 0;
 //        new DownloadTask(this,new Intent(ACTION_ALL_UPDATING), DataTypes.COURSES)
 //                .execute(getUrl(R.string.courseResPath));
         new NewDownloadTask(this,new Intent(ACTION_ALL_UPDATING), DataTypes.PROGRAMS, this)
                 .execute(getUrl(R.string.programmesResPath));
         new NewDownloadTask(this,new Intent(ACTION_ALL_UPDATING), DataTypes.VIDEOS, this)
                 .execute(getUrl(R.string.videoResPath));
         new NewDownloadTask(this,new Intent(ACTION_ALL_UPDATING), DataTypes.NEWS, this)
                 .execute(getUrl(R.string.newsResPath));
         new NewDownloadTask(this,new Intent(ACTION_ALL_UPDATING), DataTypes.DOCUMENTS, this)
                 .execute(getUrl(R.string.libraryResPath));
         new NewDownloadTask(this,new Intent(ACTION_ALL_UPDATING), DataTypes.DOCUMENTS, this)
                             .execute(getUrl(R.string.quizResPath));
 
         // We use this to send broadcasts within our local process.
         mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
          // We are going to watch for interesting local broadcasts.
         IntentFilter filter = new IntentFilter();
         filter.addAction(ACTION_ALL_UPDATING);
         mReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 System.out.println("Got onReceive in BroadcastReceiver " + intent.getAction());
                 initilizeDataCounter++;
                 if(initilizeDataCounter == 4) {
                     LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_ALL_UPDATE));
                     initilizeDataCounter = 0;
                 }
             }
         };
         mLocalBroadcastManager.registerReceiver(mReceiver, filter);
     }
 
     /**
      * Downloads/Updates a single domain item
      *
      * @param type
      * @param itemId
      */
     public synchronized void updateSingleItem(DataTypes type, int itemId) {
         if(server.checkServer()) {
             switch(type) {
                 case ARTICLE:
                     new DownloadTask(this,new Intent(ACTION_ARTICLE_UPDATE), type)
                             .execute(getUrl(R.string.articleResPath) + itemId);
                     break;
             }
         }
     }
 
     public String getUrl(int resId) {
         return getString(R.string.serverPath) + getString(resId);
     }
 
     public void updateDatabase(List<Domain> data){
         //Update the database
     }
 
 
 //    /**
 //     * This function creates an ArrayList of ListItems from a JSONArray represented
 //     * by a String. Currently supports Video, Programmes, Course.
 //     *
 //     * @param jsonString String value of JSONArray
 //     * @param type The type of JSONArray represented by it's cache name
 //     * @param context The application context to get the cache String name
 //     * @return ArrayList<ListItem>
 //     */
 //    public static ArrayList<ListItem> createListItemsFromJSONString(String jsonString, DataTypes type, Context context) {
 //        ArrayList itemList = new ArrayList();
 //
 //        try {
 //            System.out.println("WebResourceUtilities: Printing JSONString: " + jsonString);
 //            JSONArray jArray = new JSONArray(jsonString);
 //
 //            if (type == DataTypes.COURSES
 //                    || type.equals(context.getString(R.string.videoBookmarks))
 //                    || type.equals(context.getString(R.string.cacheVideoStudentList))
 //                    || type.equals(context.getString(R.string.cacheCourseVideoList))) {
 //
 //                //Video BS her
 //                System.out.println("WebresourceUtilities: Array length: " + jArray.length());
 //                for (int i = 0; i < jArray.length(); i++) {
 //                    JSONObject currentObject = jArray.getJSONObject(i);
 //                    itemList.add(new Video(currentObject.getString("id"),
 //                            currentObject.getString("videoName"),
 //                            currentObject.getString("videoDetail"),
 //                            currentObject.getString("videoDescription"),
 //                            currentObject.getString("videoType"),
 //                            null,
 //                            currentObject.getString("videoURI")));
 //                }
 //
 //            } else if (type.equals(context.getString(R.string.cacheCourseList))) {
 //
 //                //Course BS her
 //                for (int i = 0; i < jArray.length(); i++) {
 //
 //                    JSONObject currentObject = jArray.getJSONObject(i);
 //
 //                    itemList.add(new Course(currentObject.getInt("id"),
 //                            currentObject.getString("name"),
 //                            currentObject.getString("detail"),
 //                            currentObject.getString("detail"),
 //                            "Course",
 //                            null));
 //
 //                }
 //            } else if (type.equals(context.getString(R.string.cacheProgrammeList))) {
 //
 //                //Course BS her
 //                for (int i = 0; i < jArray.length(); i++) {
 //
 //                    JSONObject currentObject = jArray.getJSONObject(i);
 //
 //                    itemList.add(new Programme(currentObject.getString("id"),
 //                            currentObject.getString("name"),
 //                            currentObject.getString("detail"),  //no small description
 //                            currentObject.getString("detail"),
 //                            "Programme", //no type
 //                            null)); //no bitmap (yet)
 //
 //                }
 //            } else {
 //
 //                System.out.println("WebResourceUtilities: It was null. or irrelevant");
 //            }
 //        } catch (Exception ex) {
 //            System.out.println("WebResourceUtilities: Failed to convert String of alleged type " + type );
 //            ex.printStackTrace();
 //        }
 //
 //        return itemList;
 //    }
 
 
     
 }
