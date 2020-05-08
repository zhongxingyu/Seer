 package ru.rutube.RutubeApp;
 
 import android.app.Activity;
 import android.content.Context;
 import android.support.v4.app.FragmentActivity;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.Fields;
 import com.google.analytics.tracking.android.MapBuilder;
 
 import ru.rutube.RutubeAPI.RutubeApp;
 
 /**
  * Created by oleg on 7/30/13.
  */
 public class MainApplication extends RutubeApp {
 
     public static void mainActivityStart(Activity activity) {
         EasyTracker tracker = getTracker(activity);
         tracker.set(Fields.SCREEN_NAME, "Main screen");
         tracker.activityStart(activity);
     }
 
     public static void activityStop(Activity activity) {
         getTracker(activity).activityStop(activity);
     }
 
     protected static EasyTracker getTracker(Context context) {
         return EasyTracker.getInstance(context);
     }
 
     public static void searchActivityStart(Activity activity, String searchQuery) {
         EasyTracker tracker = getTracker(activity);
         tracker.set(Fields.SCREEN_NAME, "Search screen");
         tracker.activityStart(activity);
         tracker.send(MapBuilder
                .createEvent("stats", "open_search", searchQuery, null)
                 .build());
     }
 
     public static void playerActivityStart(Activity activity, String videoUrl) {
         EasyTracker tracker = getTracker(activity);
         tracker.set(Fields.SCREEN_NAME, "Video page screen");
         tracker.activityStart(activity);
         tracker.send(MapBuilder
                .createEvent("stats", "open_player", videoUrl, null)
                 .build());
     }
 
     public static void loginDialogStart(Activity activity) {
         EasyTracker tracker = getTracker(activity);
         tracker.send(MapBuilder.createEvent("ui_actions", "login", "open", null).build());
     }
 
     public static void loginDialogSuccess(Activity activity) {
         EasyTracker tracker = getTracker(activity);
         tracker.send(MapBuilder.createEvent("ui_actions", "login", "success", null).build());
     }
 
     public static void loginDialogFailed(Activity activity) {
         EasyTracker tracker = getTracker(activity);
         tracker.send(MapBuilder.createEvent("ui_actions", "login", "failed", null).build());
     }
 
 
     public static void cardClick(Activity activity, String tag) {
         EasyTracker tracker = getTracker(activity);
         tracker.send(MapBuilder.createEvent("ui_actions", "card_click", tag, null).build());
     }
 
     public static void cardClick(Activity activity) {
         cardClick(activity, "card");
     }
 
     public static void relatedCardClick(Activity activity, String tag) {
         EasyTracker tracker = getTracker(activity);
         tracker.send(MapBuilder.createEvent("ui_actions", "related_click", tag, null).build());
 
     }
 }
