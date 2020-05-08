 package ualberta.g12.adventurecreator.data;
 
 import android.app.Application;
 import android.content.Context;
 
 import ualberta.g12.adventurecreator.controllers.FragmentController;
 import ualberta.g12.adventurecreator.controllers.StoryController;
 import ualberta.g12.adventurecreator.controllers.StoryListController;
 import ualberta.g12.adventurecreator.online.OnlineHelper;
 
 /**
  * Application object for the Application. Used as a static singleton throughout
  * other activities which returns other singletons
  */
 public class AdventureCreator extends Application {
 
     private static final String TAG = "AdventureCreatorApplication";
     private static boolean DEBUG = true;
 
     // So many singletons!
     private transient static StoryController storyController = null;
     private transient static FragmentController fragmentController = null;
     private transient static StoryList storyList;
     private transient static StoryListController storyListController = null;
     private transient static OfflineIOHelper offlineIOHelper = null;
     private transient static OnlineHelper onlineHelper = null;
 
     private static Context context;
 
     @Override
     public void onCreate() {
         super.onCreate();
         context = getApplicationContext();
     }
 
     /**
      * Returns the StoryList Singleton. If one currently doesn't exist, we try
      * and load it from the OfflineIOHelper. If that doesn't work, we create a
      * new StoryList and return that.
      * 
      * @return the StoryList singleton
      */
     public static StoryList getStoryList() {
         if (storyList == null) {
            //storyList = getOfflineIOHelper().loadOfflineStories();
             if (storyList == null) {
                 storyList = new StoryList();
             }
         }
         return storyList;
     }
 
     /**
      * Returns the StoryListController Singleton. If one doesn't exist, we
      * create one off of our StoryList singleton
      * 
      * @return the StoryListController singleton
      */
     public static StoryListController getStoryListController() {
         if (storyListController == null) {
             storyListController = new StoryListController(getStoryList(), getOfflineIOHelper());
         }
         return storyListController;
     }
 
     /**
      * Returns the StoryController Singleton. If one doesn't exist, we
      * initialize it and then return
      * 
      * @return the StoryController Singleton
      */
     public static StoryController getStoryController() {
         if (storyController == null) {
             storyController = new StoryController();
         }
         return storyController;
     }
 
     /**
      * Returns the FragmentController Singleton. If one doesn't exist, we
      * initialize it and then return it.
      * 
      * @return the FragmentController Singleton
      */
     public static FragmentController getFragmentController() {
         if (fragmentController == null) {
             fragmentController = new FragmentController();
         }
         return fragmentController;
     }
 
     /**
      * Returns the OfflineIOHelper Singleton. If one doesn't exist, we
      * initialize it and then return it
      * 
      * @return the OfflineIOHelper Singleton
      */
     public static OfflineIOHelper getOfflineIOHelper() {
         if (offlineIOHelper == null) {
             offlineIOHelper = new OfflineIOHelper(context);
         }
         return offlineIOHelper;
     }
 
     // TODO: Implement getters for all of the singletons
 
     /** Returns the OnlineHelper Singleton. If one doesn't exist, we make it */
     public static OnlineHelper getOnlineHelper() {
         if(onlineHelper == null){
             onlineHelper = new OnlineHelper();
         }
         return onlineHelper;
     }
 }
