 package io.usersource.annoplugin;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ResolveInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Environment;
 import android.util.Base64;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Toast;
 import io.usersource.anno.AnnoDrawActivity;
 import io.usersource.anno.CommunityActivity;
 import io.usersource.anno.IntroActivity;
 import io.usersource.anno.OptionFeedbackActivity;
 import io.usersource.annoplugin.utils.*;
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaPlugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * Anno Cordova Plugin, provide series functions that can't be done in JavaScript, or done better in native code than Javascript
  *
  * @author David Lee
  */
 public class AnnoCordovaPlugin extends CordovaPlugin
 {
   // plugin action names.
   public static final String EXIT_CURRENT_ACTIVITY = "exit_current_activity";
   public static final String EXIT_INTRO = "exit_intro";
   public static final String PROCESS_IMAGE_AND_APPINFO = "process_image_and_appinfo";
   public static final String GET_RECENT_APPLIST = "get_recent_applist";
   public static final String GET_SCREENSHOT_PATH = "get_screenshot_path";
   public static final String GET_ANNO_SCREENSHOT_PATH = "get_anno_screenshot_path";
   public static final String SHOW_TOAST = "show_toast";
   public static final String GOTO_ANNO_HOME = "goto_anno_home";
   public static final String START_ACTIVITY = "start_activity";
   public static final String CLOSE_SOFTKEYBOARD = "close_softkeyboard";
   public static final String SHOW_SOFTKEYBOARD = "show_softkeyboard";
 
   // activity names
   public static final String ACTIVITY_INTRO = "Intro";
   public static final String ACTIVITY_FEEDBACK = "Feedback";
 
   private static final int COMPRESS_QUALITY = 40;
 
   @Override
   public boolean execute(String action, JSONArray args,
                          CallbackContext callbackContext) throws JSONException
   {
     if (EXIT_CURRENT_ACTIVITY.equals(action)) {
       exitCurrentActivity(args, callbackContext);
       return true;
     }
     else if (EXIT_INTRO.equals(action)) {
       exitIntro(args, callbackContext);
       return true;
     }
     else if (PROCESS_IMAGE_AND_APPINFO.equals(action)) {
       processImageAndAppInfo(args, callbackContext);
       return true;
     }
     else if (GET_RECENT_APPLIST.equals(action)) {
       getRecentTasks(args, callbackContext);
       return true;
     }
     else if (GET_SCREENSHOT_PATH.equals(action)) {
       AnnoDrawActivity annoDrawActivity = (AnnoDrawActivity)this.cordova.getActivity();
       callbackContext.success(annoDrawActivity.getScreenshotPath()+"|"+annoDrawActivity.getLevel()+"|"+AnnoUtils.isAnno(annoDrawActivity.getPackageName()));
       return true;
     }
     else if (GET_ANNO_SCREENSHOT_PATH.equals(action)) {
       Activity activity = this.cordova.getActivity();
 
       String appLocation = AnnoUtils.getDataLocation();
       String screenshotDirName = AnnoUtils.getScreenshotDirName();
       String screenshotDirPath = new File(appLocation, screenshotDirName)
               .getAbsolutePath();
 
       callbackContext.success(screenshotDirPath);
       return true;
     }
     else if (SHOW_TOAST.equals(action)) {
       showToastMessage(args);
       callbackContext.success();
       return true;
     }
     else if (GOTO_ANNO_HOME.equals(action)) {
       Activity activity = this.cordova.getActivity();
       activity.finish();
 
       Intent intent = new Intent(activity, CommunityActivity.class);
       activity.startActivity(intent);
       callbackContext.success();
       return true;
     }
     else if (START_ACTIVITY.equals(action)) {
       String activityName = args.getString(0);
       boolean closeCurrentActivity = args.getBoolean(1);
 
       Activity activity = this.cordova.getActivity();
 
       if (closeCurrentActivity)
       {
         activity.finish();
       }
 
       Intent intent = null;
       if (activityName.equalsIgnoreCase(ACTIVITY_INTRO))
       {
         intent = new Intent(activity, IntroActivity.class);
       }
       else if (activityName.equalsIgnoreCase(ACTIVITY_FEEDBACK))
       {
         intent = new Intent(activity, OptionFeedbackActivity.class);
       }
 
       activity.startActivity(intent);
       callbackContext.success();
 
       return true;
     }
     else if (CLOSE_SOFTKEYBOARD.equals(action)) {
 
       AnnoDrawActivity activity = (AnnoDrawActivity)this.cordova.getActivity();
       InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
       imm.hideSoftInputFromWindow(activity.getAppView().getWindowToken(), 0);
 
       callbackContext.success();
 
       return true;
     }
     else if (SHOW_SOFTKEYBOARD.equals(action)) {
 
      CommunityActivity activity = (CommunityActivity)this.cordova.getActivity();
       InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.showSoftInput(activity.getAppView(), InputMethodManager.SHOW_FORCED);
 
       callbackContext.success();
 
       return true;
     }
 
 
     return false;
   }
 
   /**
    * exit current activity
    * @param args
    * @param callbackContext
    */
   private void exitCurrentActivity(JSONArray args, CallbackContext callbackContext) {
     this.cordova.getActivity().finish();
   }
 
   /**
    * Exit intro and start feedbackedit with a screenshot.
    *
    * @param args
    * @param callbackContext
    */
   private void exitIntro(JSONArray args, CallbackContext callbackContext) {
     Activity activity = this.cordova.getActivity();
     activity.finish();
 
     String packageName = activity.getPackageName();
 
     Intent intent = new Intent(Intent.ACTION_SEND);
     intent.setClassName(packageName,
             "io.usersource.anno.AnnoDrawActivity");
     intent.setType("image/*");
     // set this flag for FeedbackEditActivity to know it's practice.
     intent.putExtra(AnnoUtils.INTENT_EXTRA_IS_PRACTICE, true);
     FileOutputStream fos = null;
     InputStream is = null;
     String filePath = "";
 
     try {
       File screenshotDir = new File(
               Environment
                       .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
               "Screenshots");
       if (!screenshotDir.exists()) {
         if (!screenshotDir.mkdirs()) {
           throw new IOException("Failed to create directory "
                   + screenshotDir.getAbsolutePath());
         }
       }
 
       File screenshotPath = new File(screenshotDir,
               AnnoUtils.generateScreenshotName());
       fos = new FileOutputStream(screenshotPath);
 
       is = activity.getAssets().open(
               "www/anno/pages/intro/css/images/defaultsht.jpg");
       byte b[] = new byte[is.available()];
       is.read(b);
 
       fos.write(b);
       filePath = screenshotPath.getAbsolutePath();
     } catch (Exception e) {
       // dummy
     } finally {
       if (is != null) {
         try {
           is.close();
         } catch (IOException e) {
         }
       }
 
       if (fos != null) {
         try {
           fos.flush();
           fos.close();
         } catch (IOException e) {
         }
       }
     }
 
     File imageFile = new File(filePath);
     Uri imageUri = Uri.parse("file://" + imageFile.getPath());
     intent.putExtra(Intent.EXTRA_STREAM, imageUri);
 
     if (activity instanceof AnnoDrawActivity
             || activity instanceof IntroActivity
             || activity instanceof CommunityActivity) {
       // current app is standalone anno, or anno plugin activity.
       intent.putExtra(AnnoUtils.LEVEL, 1);
     } else {
       // current app is 3rd.
       intent.putExtra(AnnoUtils.LEVEL, 0);
     }
 
     activity.startActivity(intent);
   }
 
   /**
    * save screenshot to anno data folder and compress.
    * @param args
    * @param callbackContext
    */
   private void processImageAndAppInfo(final JSONArray args, final CallbackContext callbackContext)
   {
     cordova.getThreadPool().execute(new Runnable() {
       public void run()
       {
         JSONObject result = new JSONObject();
         try
         {
           JSONObject imageAttrs = doSaveImage(args);
           result.put("imageAttrs", imageAttrs);
 
           JSONObject appInfo = getAppInfo();
           result.put("appInfo", appInfo);
           result.put("success", true);
 
           callbackContext.success(result);
         }
         catch (Exception e)
         {
           try
           {
             result.put("success", false);
             result.put("message", "An error occurred when processing image: "+ e.getMessage());
           }
           catch (JSONException dummy)
           {
 
           }
 
           callbackContext.error(result);
         }
 
       }
     });
   }
 
   private JSONObject doSaveImage(JSONArray args) throws Exception
   {
     String base64Str = "";
     JSONObject jso = new JSONObject();
 
     if (args.length() >0)
     {
       base64Str = args.getString(0);
     }
 
     AnnoDrawActivity annoDrawActivity = (AnnoDrawActivity)this.cordova.getActivity();
 
     String appLocation = AnnoUtils.getDataLocation();
     String screenshotDirName = AnnoUtils.getScreenshotDirName();
     String screenshotDirPath = new File(appLocation, screenshotDirName)
             .getAbsolutePath();
     AnnoUtils.mkdirs(annoDrawActivity, screenshotDirPath);
     //checkEnoughSpace(bitmap.getByteCount());
 
     String imageKey = generateUniqueImageKey();
     FileOutputStream out = new FileOutputStream(new File(screenshotDirPath,
             imageKey));
 
     Bitmap bmp;
     if (base64Str.length() >0)
     {
       byte bytes[] = Base64.decode(base64Str, Base64.DEFAULT);
       bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
     }
     else
     {
       bmp = BitmapFactory.decodeFile(annoDrawActivity.getScreenshotPath());
     }
 
     bmp.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, out);
     out.close();
 
     jso.put("imageKey", imageKey);
     jso.put("screenshotPath", screenshotDirPath);
 
     return jso;
   }
 
   private String generateUniqueImageKey()
   {
     return UUID.randomUUID().toString();
   }
 
   /**
    * get app Info, app name, source, version, level
    * @return JSONObject
    * @throws Exception
    */
   private JSONObject getAppInfo() throws Exception
   {
     JSONObject result = new JSONObject();
     AnnoDrawActivity annoDrawActivity = (AnnoDrawActivity)this.cordova.getActivity();
 
     String source, appName, appVersion;
     if (AnnoUtils.isAnno(annoDrawActivity.getPackageName()) && annoDrawActivity.getLevel() != 2)
     {
       source = AnnoUtils.ANNO_SOURCE_STANDALONE;
       appName = AnnoUtils.UNKNOWN_APP_NAME;
     }
     else
     {
       source = AnnoUtils.ANNO_SOURCE_PLUGIN;
       appName = AnnoUtils.getAppName(annoDrawActivity);
     }
 
     appVersion = AnnoUtils.getAppVersion(annoDrawActivity);
 
     result.put("source", source);
     result.put("appName", appName);
     result.put("appVersion", appVersion);
     result.put("level", annoDrawActivity.getLevel());
 
     return result;
   }
 
   /**
    * get recent app list
    * @param args
    * @param callbackContext
    * @return
    * @throws JSONException
    */
   private void getRecentTasks(JSONArray args, CallbackContext callbackContext) throws JSONException
   {
     AnnoDrawActivity annoDrawActivity = (AnnoDrawActivity)this.cordova.getActivity();
     int taskNum = args.getInt(0);
 
     ActivityManager mActivityManager = (ActivityManager)annoDrawActivity.getSystemService(Context.ACTIVITY_SERVICE);
     // Print Recent Running Tasks
     JSONArray jsonArray = new JSONArray();
     List<ActivityManager.RecentTaskInfo> recentTasks = mActivityManager.getRecentTasks(
             taskNum, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
     for (ActivityManager.RecentTaskInfo rti : recentTasks) {
       Intent intent = rti.baseIntent;
 
       try
       {
         ResolveInfo resolveInfo = annoDrawActivity.getPackageManager().resolveActivity(
                 intent, 0);
         jsonArray.put(resolveInfo.loadLabel(annoDrawActivity.getPackageManager()).toString());
       }
       catch (Exception dummy)
       {
 
       }
 
     }
 
     callbackContext.success(jsonArray);
   }
 
   private void showToastMessage(JSONArray args) throws JSONException
   {
     String message = args.getString(0);
     Activity activity = this.cordova.getActivity();
     Toast toast = Toast.makeText(activity,
             message, Toast.LENGTH_SHORT);
     toast.show();
   }
 }
