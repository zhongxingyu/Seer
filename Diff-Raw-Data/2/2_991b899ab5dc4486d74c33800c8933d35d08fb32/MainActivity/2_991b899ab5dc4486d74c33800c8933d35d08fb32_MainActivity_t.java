 package be.alfredo.colruyt;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.View;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public class MainActivity extends Activity
 {
     private static final String TAG = "MainActivity";
     private static final int ACTION_TAKE_PHOTO = 100;
     private static final String JPEG_FILE_SUFFIX = ".jpg";
     public static final String EXTRA_MESSAGE = "be.alfredo.colruyt.MESSAGE";
 
     private String mCurrentPhotoPath;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
 
     /**
      * Called when the user clicks on the button to take a picture.
      * Starts the intent for the internal camera app and saves the
      * picture in the application's cache dir.
      *
      * @param view
      */
    public void takePictureHandler(View view)
     {
         if (isIntentAvailable(this, MediaStore.ACTION_VIDEO_CAPTURE))
         {
             Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
             File f = null;
 
             try
             {
                 f = setUpPhotoFile();
                 takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                 startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
             }
             catch (IOException e)
             {
                 e.printStackTrace();
                 mCurrentPhotoPath = null;
                 f = null;
             }
         }
     }
 
     public void historyHandler(View view)
     {
         // Show history
     }
 
     /**
      * Create a temporary file to which the image will be written.
      *
      * @return Temporary file to which the image will be written
      * @throws IOException
      */
     private File setUpPhotoFile() throws IOException
     {
         File imageF = File.createTempFile("image", JPEG_FILE_SUFFIX, getCacheDir());;
         mCurrentPhotoPath = imageF.getAbsolutePath();
 
         return imageF;
     }
 
     /**
      * Intercepting callback from the camera intent
      *
      * @param requestCode
      * @param resultCode
      * @param data
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         if (requestCode == ACTION_TAKE_PHOTO)
         {
             if (resultCode == RESULT_OK)
             {
                 Intent ocrIntent = new Intent(this, OCRActivity.class);
                 ocrIntent.putExtra(EXTRA_MESSAGE, mCurrentPhotoPath);
                 startActivity(ocrIntent);
             }
             else if (resultCode == RESULT_CANCELED)
             {
                 // User cancelled the image capture
             }
             else
             {
                 // Image capture failed, advise user
             }
         }
     }
 
     /**
      * Indicates whether the specified action can be used as an intent. This
      * method queries the package manager for installed packages that can
      * respond to an intent with the specified action. If no suitable package is
      * found, this method returns false.
      * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
      *
      * @param context The application's environment.
      * @param action  The Intent action to check for availability.
      * @return True if an Intent with the specified action can be sent and
      *         responded to, false otherwise.
      */
     public static boolean isIntentAvailable(Context context, String action)
     {
         final PackageManager packageManager = context.getPackageManager();
         final Intent intent = new Intent(action);
         List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
         return list.size() > 0;
     }
 }
