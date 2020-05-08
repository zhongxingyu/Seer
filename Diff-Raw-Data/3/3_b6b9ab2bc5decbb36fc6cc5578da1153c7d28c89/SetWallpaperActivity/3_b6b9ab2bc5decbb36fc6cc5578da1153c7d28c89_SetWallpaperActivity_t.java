 package net.taviscaron.airliners.activities;
 
 import android.app.Activity;
 import android.app.WallpaperManager;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.pm.ResolveInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Helper activity which crop and set wallpaper
  * @author Andrei Senchuk
  */
 public class SetWallpaperActivity extends Activity {
     public static final String TAG = "SetWallpaperActivity";
     public static final String SET_WALLPAPER_ACTION = "net.taviscaron.airliners.SET_WALLPAPER";
     public static final String IMAGE_PATH_EXTRA = "imagePath";
     private static final int CROP_REQUEST_CODE = 0xb00b1e5;
 
     private File tempFile;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         String imagePath = getIntent().getStringExtra(IMAGE_PATH_EXTRA);
         if(!tryCropImage(imagePath)) {
             setWallpaperImageFromPath(imagePath);
             finish();
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode) {
             case CROP_REQUEST_CODE:
                 if(resultCode == RESULT_OK) {
                    setWallpaperImageFromPath(tempFile.getAbsolutePath());
                 }
                 // remove temp file
                 tempFile.delete();
                 finish();
                 break;
             default:
                 super.onActivityResult(requestCode, resultCode, data);
                 break;
         }
     }
 
     private boolean tryCropImage(String imagePath) {
         Intent intent = new Intent("com.android.camera.action.CROP");
         intent.setType("image/*");
 
         List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
 
         if(!list.isEmpty()) {
             WallpaperManager mgr = WallpaperManager.getInstance(this);
 
             int width = mgr.getDesiredMinimumWidth();
             int height = mgr.getDesiredMinimumHeight();
 
             intent.putExtra("outputX", width);
             intent.putExtra("outputY", height);
             intent.putExtra("aspectX", width);
             intent.putExtra("aspectY", height);
             intent.putExtra("scale", true);
             intent.putExtra("return-data", false);
             intent.setData(Uri.fromFile(new File(imagePath)));
 
             try {
                 tempFile = File.createTempFile("wallpaper", null);
                 intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
             } catch (IOException e) {
                 Log.d(TAG, "Can't create temp file", e);
                 return false;
             }
 
             ResolveInfo res = list.get(0);
             intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
 
             startActivityForResult(intent, CROP_REQUEST_CODE);
             return true;
         } else {
             return false;
         }
     }
 
     private void setWallpaperImageFromPath(String path) {
         try {
             Bitmap bitmap = BitmapFactory.decodeFile(path);
             if(bitmap != null) {
                 WallpaperManager.getInstance(this).setBitmap(bitmap);
             }
         } catch (IOException e) {
             Log.w(TAG, "Failed to set wallpaper", e);
         }
     }
 }
