 
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
 
     // So many singletons!
     private static StoryController storyController = null;
     private static FragmentController fragmentController = null;
     private static StoryList storyList;
     private static StoryListController storyListController = null;
     private static OfflineIOHelper offlineIOHelper = null;
     private static OnlineHelper onlineHelper = null;
 
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
      * @return StoryList singleton
      */
     public static StoryList getStoryList() {
         if (storyList == null) {
            // storyList = getOfflineIOHelper().loadOfflineStories();
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
      * @return StoryListController singleton
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
      * @return StoryController Singleton
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
      * @return FragmentController Singleton
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
      * @return OfflineIOHelper Singleton
      */
     public static OfflineIOHelper getOfflineIOHelper() {
         if (offlineIOHelper == null) {
             offlineIOHelper = new OfflineIOHelper(context);
         }
         return offlineIOHelper;
     }
 
     /**
      * Returns the OnlineHelper Singleton. If one doesn't exist, we make it.
      * 
      * @return OnlineHelper
      */
     public static OnlineHelper getOnlineHelper() {
         if (onlineHelper == null) {
             onlineHelper = new OnlineHelper();
         }
         return onlineHelper;
     }
 }
