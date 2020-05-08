 package com.rowan.ieee.sac14;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.FragmentManager;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.SearchManager;
 import android.app.TaskStackBuilder;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.hardware.Camera;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBarActivity;
 import android.database.Cursor;
 import android.support.v7.app.ActionBar;
 import android.support.v4.app.Fragment;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.*;
 import com.google.android.gms.maps.internal.b;
 
 import android.os.Bundle;
 import android.text.format.Time;
 import android.util.Config;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.os.Build;
 import android.view.WindowManager;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.VideoView;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 public class MainActivity extends ActionBarActivity {
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private ActionBarDrawerToggle mDrawerToggle;
 
     private CharSequence mDrawerTitle;
     CharSequence mTitle;
     private String[] mPlanetTitles;
     private final static int
             CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
     private int mId = 314159;
     WebView myWebView;
     /** Parse through unnecessary variables **/
     private static final int ACTION_TAKE_PHOTO_B = 1;
     private static final int ACTION_TAKE_PHOTO_S = 2;
     private static final int ACTION_TAKE_VIDEO = 3;
 
     private static final String BITMAP_STORAGE_KEY = "viewbitmap";
     private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
     private ImageView mImageView;
     private Bitmap mImageBitmap;
 
     private static final String VIDEO_STORAGE_KEY = "viewvideo";
     private static final String VIDEOVIEW_VISIBILITY_STORAGE_KEY = "videoviewvisibility";
     private VideoView mVideoView;
     private Uri mVideoUri;
 
     private String mCurrentPhotoPath;
 
     private static final String JPEG_FILE_PREFIX = "PHOTO_";
     private static final String JPEG_FILE_SUFFIX = ".jpg";
     private static final int READ_REQUEST_CODE = 42;
     private static final int PICKFILE_RESULT_CODE = 271828;
 
     private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
 
     public static final String defaulturl = "http://rowan.edu/clubs/ieee/sac/?app=1";
     public boolean backtwo;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mTitle = mDrawerTitle = getTitle();
         mPlanetTitles = getResources().getStringArray(R.array.planets_array);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
         // set a custom shadow that overlays the main content when the drawer opens
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         //Append header image
         View v = new ImageView(getBaseContext());
         ImageView image;
         image = new ImageView(v.getContext());
         image.setImageDrawable(v.getResources().getDrawable(R.drawable.nav_bar_header));
         image.setScaleType(ImageView.ScaleType.MATRIX);
 
         /*int height = 30;
         image.setMaxHeight(height);
         image.setMinimumHeight(height);*/
 
         mDrawerList.addHeaderView(image);
         v = new ImageView(getBaseContext());
         ImageView image2;
         image2 = new ImageView(v.getContext());
         image2.setImageDrawable(v.getResources().getDrawable(R.drawable.nav_bar_header));
         image2.setScaleType(ImageView.ScaleType.MATRIX);
         //ImageView image2 = image;
         image2.setVisibility(View.INVISIBLE);
         image2.setMinimumHeight(80);
         mDrawerList.addFooterView(image2);
         // set up the drawer's list view with items and click listener
         mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, mPlanetTitles));
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
         // enable ActionBar app icon to behave as action to toggle nav drawer
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // ActionBarDrawerToggle ties together the the proper interactions
         // between the sliding drawer and the action bar app icon
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                 R.string.drawer_open,  /* "open drawer" description for accessibility */
                 R.string.drawer_close  /* "close drawer" description for accessibility */
         ) {
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(mTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             public void onDrawerOpened(View drawerView) {
                 //Cheers((String) mDrawerTitle);
                 getActionBar().setTitle(mDrawerTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
         };
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         if (savedInstanceState == null) {
             selectItem(0);
         }
 
         //Establish the WebView
         myWebView = (WebView) findViewById(R.id.webview);
         WebSettings webSettings = myWebView.getSettings();
         webSettings.setJavaScriptEnabled(true);
         myWebView.setWebViewClient(new WebViewClient());
 
         //Load the application
         //myWebView.loadUrl("http://www.rowan.edu/clubs/ieee/a/");
         //@TODO Fix WebView so it loads, then remove this command and use onSelectItem code instead
         /** Share Image with App*/
         // Get intent, action and MIME type
         Intent intent = getIntent();
         String action = intent.getAction();
         String type = intent.getType();
         backtwo = false;
         if (Intent.ACTION_SEND.equals(action) && type != null) {
             if (type.startsWith("image/")) {
                 try {
                     handleSendImage(intent); // Handle single image being sent
                 } catch(Exception e) {
                     Cheers(e.getMessage());
                 }
             }
         } else if(intent.getStringExtra("reupload") != null) {
             backtwo = true;
             performFileSearch();
             myWebView.loadUrl(defaulturl);
         } else { /* Launched directly */
             myWebView.loadUrl(defaulturl);
         }
 
 
         /** Set the transparency of just the background
          Resources res = getResources();
          Drawable background = res.getDrawable(R.drawable.drawerbg);
          // The layout which are to have the background:
          LinearLayout layout = ((LinearLayout) );
          // Now that we have the layout and the background, we adjust the opacity
          // of the background, and sets it as the background for the layout
          background.setAlpha(160);
          layout.setBackground(background);**/
         //findViewById(R.id.left_drawer).getBackground().setAlpha(160);
 
         //Of course, if we aren't online, the app should be killed:
         if(!isOnline()) {
             Cheers("Sorry, you must be online to use this app.");
             finish();
         }
     }
     public boolean isOnline() {
         ConnectivityManager connMgr = (ConnectivityManager)
                 getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected()) {
             return true;
         } else {
             return false;
         }
     }
     void handleSendImage(Intent data) {
         //Cheers("Intent");
         Uri imageUri = (Uri) data.getParcelableExtra(Intent.EXTRA_STREAM);
         String imagePath = getRealPathFromURI(getApplicationContext(), imageUri);
 
         //Cheers("You wish to share "+imagePath+". This function isn't complete yet. Please try again later.");
         try {
             if (imageUri != null) {
                 // Update UI to reflect image being shared
 
                 Intent upload = new Intent(this, UploadPhoto.class);
 
                 upload.putExtra("path", imagePath);
                 upload.putExtra("name", imagePath);
                // Cheers(imagePath);
                 try {
                     if(imagePath.length() > 0)
                         startActivity(upload);
                     else {
                         Cheers(getResources().getString(R.string.photovoid));
                         onBackPressed();
                     }
                 } catch(Exception e) {
                     Cheers(getResources().getString(R.string.photovoid));
                     onBackPressed();
                 }
             }
         } catch(Exception e) {
             Cheers(getResources().getString(R.string.photovoid));
             onBackPressed();
         }
 
 
     }
     public String getRealPathFromURI(Context context, Uri contentUri) {
         Cursor cursor = null;
         try {
             String[] proj = { MediaStore.Images.Media.DATA };
             cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
             int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
             cursor.moveToFirst();
             return cursor.getString(column_index);
         } finally {
             if (cursor != null) {
                 cursor.close();
             }
         }
     }
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         // Check if the key event was the Back button and if there's history
         if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack() && !mDrawerLayout.isDrawerOpen(mDrawerList)) {
             myWebView.goBack();
             return true;
         } else if(keyCode == KeyEvent.KEYCODE_BACK) {
             onBackPressed();
             return true;
         }
         // If it wasn't the Back key or there's no web page history, bubble up to the default
         // system behavior (probably exit the activity)
         return super.onKeyDown(keyCode, event);
     }
     @Override
     public void onBackPressed() {
         //Cheers(backtwo+" <-");
         // super.onBackPressed();
             finish();
 
     }
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     /* Called whenever we call invalidateOptionsMenu() */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         // If the nav drawer is open, hide action items related to the content view
         boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        // menu.findItem(R.id.action_camera).setVisible(true);
         //menu.findItem(R.id.action_upload).setVisible(!drawerOpen);
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         // Handle action buttons
         switch(item.getItemId()) {
             /*case R.id.action_settings:
                 Intent i = new Intent(MainActivity.this, Settings.class);
                 startActivity(i);
                 break;
             case R.id.notify:
                 testNote("", "");
                 break;
             */
             case R.id.action_camera:
                 gallery_camera_dialog();
                 break;
             /*case R.id.action_upload:
 
                 break;
             case R.id.action_camera_pro:
                 Cheers("Not for the faint of heart");
                 try {
                 Intent camerapro = new Intent(this, CameraPro.class);
                 startActivity(camerapro);
                 } catch(Exception e) {
                     Cheers("Bad luck: "+e.getMessage());
                 }
             break;*/
 
         }
         return super.onOptionsItemSelected(item);
 
     }
     /** A safe way to get an instance of the Camera object. */
     public static Camera getCameraInstance(){
         Camera c = null;
         try {
             c = Camera.open(); // attempt to get a Camera instance
         }
         catch (Exception e){
             // Camera is not available (in use or does not exist)
         }
         return c; // returns null if camera is unavailable
     }
     public void gallery_camera_dialog() {
         // TODO Auto-generated method stub
         final Dialog custom = new Dialog(MainActivity.this);
         custom.setContentView(R.layout.gallery_camera);
         final LinearLayout openCam = (LinearLayout) custom.findViewById(R.id.camera);
         final LinearLayout openGal = (LinearLayout) custom.findViewById(R.id.gallery);
         custom.setTitle("Choose Source");
         openCam.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if(event.getAction() == MotionEvent.ACTION_DOWN)
                     openCam.setBackgroundColor(getResources().getColor(R.color.gray));
                 else if(event.getAction() == MotionEvent.ACTION_UP)
                     openCam.setBackgroundColor(getResources().getColor(R.color.white));
                 return false;
             }
         });
         openCam.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
 
                 if (isIntentAvailable(getApplicationContext(), MediaStore.ACTION_IMAGE_CAPTURE))
                     dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                 else
                     Cheers("Camera not available");
                 custom.dismiss();
             }
 
         });
         openGal.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if(event.getAction() == MotionEvent.ACTION_DOWN)
                     openGal.setBackgroundColor(getResources().getColor(R.color.gray));
                 else if(event.getAction() == MotionEvent.ACTION_UP)
                     openGal.setBackgroundColor(getResources().getColor(R.color.white));
                 return false;
             }
         });
         openGal.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                // openGal.setBackgroundColor(R.color.gray);
                 performFileSearch();
                 custom.dismiss();
             }
 
         });
         custom.show();
 
     }
      /*
     * Camera Features
      */
     //Two functions below exist for the camera feature
     public static boolean isIntentAvailable(Context context, String action) {
         final PackageManager packageManager = context.getPackageManager();
         final Intent intent = new Intent(action);
         List<ResolveInfo> list =
                 packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
         return list.size() > 0;
     }
     private void dispatchTakePictureIntent(int actionCode) {
         mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
         Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         try {
         switch(actionCode) {
             case ACTION_TAKE_PHOTO_B:
                 File f = null;
 
                 try {
                     f = setUpPhotoFile();
                     mCurrentPhotoPath = f.getAbsolutePath();
                     //Cheers("Save to: "+mCurrentPhotoPath);
                     takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    /* Cheers("Photo saved");*/
                 } catch (IOException e) {
                     e.printStackTrace();
                     f = null;
                     mCurrentPhotoPath = null;
                     Cheers("Photo not saved: "+e.getMessage());
                 }
                 break;
 
             default:
                 break;
         } // switch
         } catch(Exception e) {
             Cheers("Camera broke: "+e.getMessage());
         }
 
         startActivityForResult(takePictureIntent, actionCode);
     }
     //Return and save image
 
     private void handleSmallCameraPhoto(Intent intent) {
         Bundle extras = intent.getExtras();
         mImageBitmap = (Bitmap) extras.get("data");
         mImageView.setImageBitmap(mImageBitmap);
         mVideoUri = null;
         mImageView.setVisibility(View.VISIBLE);
         mVideoView.setVisibility(View.INVISIBLE);
     }
 
     private void handleBigCameraPhoto() {
         //Cheers("HBCP "+mCurrentPhotoPath);
         if (mCurrentPhotoPath != null) {
            // Cheers("setPic");
             //setPic();
             //Cheers("galleryAddPic");
             galleryAddPic();
             mCurrentPhotoPath = null;
         }
 
     }
     private void setPic() {
 
 		/* There isn't enough memory to open up more than a couple camera photos */
 		/* So pre-scale the target bitmap into which the file is decoded */
 
 		/* Get the size of the ImageView */
         int targetW = mImageView.getWidth();
         int targetH = mImageView.getHeight();
 
 		/* Get the size of the image */
         BitmapFactory.Options bmOptions = new BitmapFactory.Options();
         bmOptions.inJustDecodeBounds = true;
         BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
         int photoW = bmOptions.outWidth;
         int photoH = bmOptions.outHeight;
 
 		/* Figure out which way needs to be reduced less */
         int scaleFactor = 1;
         if ((targetW > 0) || (targetH > 0)) {
             scaleFactor = Math.min(photoW/targetW, photoH/targetH);
         }
 
 		/* Set bitmap options to scale the image decode target */
         bmOptions.inJustDecodeBounds = false;
         bmOptions.inSampleSize = scaleFactor;
         bmOptions.inPurgeable = true;
 
 		/* Decode the JPEG file into a Bitmap */
         Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
 
 		/* Associate the Bitmap to the ImageView */
         mImageView.setImageBitmap(bitmap);
         mVideoUri = null;
         mImageView.setVisibility(View.VISIBLE);
         mVideoView.setVisibility(View.INVISIBLE);
     }
     private void galleryAddPic() {
         Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
         //Cheers(mCurrentPhotoPath);
         File f = new File(mCurrentPhotoPath);
         Uri contentUri = Uri.fromFile(f);
         //Cheers(contentUri.getPath());
 
         mediaScanIntent.setData(contentUri);
         Cheers("Photo saved to your gallery");
         /** Go to upload manager **/
         Intent upload = new Intent(this, UploadPhoto.class);
         upload.putExtra("path", mCurrentPhotoPath);
         upload.putExtra("name", mCurrentPhotoPath);
         Cheers(mCurrentPhotoPath);
         startActivity(upload);
        // Cheers(contentUri.toString());
         this.sendBroadcast(mediaScanIntent);
     }
     private File setUpPhotoFile() throws IOException {
 
         File f = createImageFile();
         mCurrentPhotoPath = f.getAbsolutePath();
 
         return f;
     }
     private File createImageFile() throws IOException {
         // Create an image file name
         String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
         File albumF = getAlbumDir();
         File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
         return imageF;
     }
     private File getAlbumDir() {
         File storageDir = null;
 
         if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 
             storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
             //Cheers(storageDir.getAbsolutePath());
             if (storageDir != null) {
                 if (! storageDir.mkdirs()) {
                     if (! storageDir.exists()){
                         Log.d("CameraSample", "failed to create directory");
                         Cheers("Cannot create directory");
                         return null;
                     }
                 }
             }
 
         } else {
             Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
             Cheers("Cannot READ/WRITE to external storage.");
         }
 
         return storageDir;
     }
     private String getAlbumName() {
         return "SAC2014";
     }
     /** Upload Image Framework **/
     public void performFileSearch() {
 
         // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
         // browser.
         Intent intent;
         if(android.os.Build.VERSION.SDK_INT >= 19 && true) {
             intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
             intent.addCategory(Intent.CATEGORY_OPENABLE);
             intent.setType("image/*");
             // Filter to only show results that can be "opened", such as a
             // file (as opposed to a list of contacts or timezones)
 
 
             // Filter to show only images, using the image MIME data type.
             // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
             // To search for all documents available via installed storage providers,
             // it would be "*/*".
             try {
                 //Cheers("starting READ_REQUEST_CODE");
                 Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                 startActivityForResult(i, READ_REQUEST_CODE);
             } catch(Exception e) {
                 Cheers(e.getMessage());
             }
         } else {
             intent = new Intent(Intent.ACTION_GET_CONTENT);
             intent.setType("image/*");
             startActivityForResult(intent,PICKFILE_RESULT_CODE);
         }
 
 
     }
 
     /* The click listner for ListView in the navigation drawer */
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             selectItem(position);
         }
     }
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         //outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        // super.onSaveInstanceState(outState);
     }
     private void selectItem(int p) {
         int position = 0;
         if(p > 0)
             position = p - 1;
         // update the main content by replacing fragments
         /*android.app.Fragment fragment = new PlanetFragment();
         Bundle args = new Bundle();
         args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
         fragment.setArguments(args);
 
         FragmentManager fragmentManager = getFragmentManager();
         fragmentManager.beginTransaction().replace(R.id.webview, fragment).commit();
        // fragmentManager.executePendingTransactions();
        */
 
         // update selected item and title, then close the drawer
         mDrawerList.setItemChecked(position, true);
         //@TODO Format Strings
         mDrawerLayout.closeDrawer(mDrawerList);
         try {
             String baseurl = "http://rowan.edu/clubs/ieee/sac/index.php?app=1";
             //Context
             if(true) {
                 mPlanetTitles[0] = "Now";
                // mPlanetTitles[0] = getResources().getString(R.string.v);
             }
             String fiveurl = "http://rowan.edu/clubs/ieee/sac/index.php?p=registration&app=1";
             if(false) /*Registered*/ {
                 mPlanetTitles[5] = "Profile";
             } else
                 mPlanetTitles[5] = "Register";
             switch(position) {
                 case 0:
                     myWebView.loadUrl("http://rowan.edu/clubs/ieee/sac/index.php?p=home&app=1");
                     //@TODO Change this to contextual content(or leave as now)
                     //setTitle(mPlanetTitles[position]);
                     break;
                 case 1:
                     myWebView.loadUrl(baseurl+"&p=home");
                     break;
                 case 2:
                     myWebView.loadUrl(baseurl+"&p=competitions");
                     break;
                 case 3:
                     myWebView.loadUrl(baseurl+"&p=hotel");
                     break;
                 case 4:
                     myWebView.loadUrl(baseurl+"&p=schedule");
                     break;
                 case 5:
                     myWebView.loadUrl(fiveurl);
                     break;
                 case 6:
                     myWebView.loadUrl(baseurl+"&p=vote");
                     break;
                 case 7:
                     myWebView.loadUrl(baseurl+"&p=faq");
                 case 8:
                     myWebView.loadUrl(baseurl+"&p=gallery");
             }
         } catch(Exception e) {
             //Cheers("Item Selection Error: "+e.getMessage());
         }
         //Cheers("You selected item #"+position+", "+mPlanetTitles[position]);
         setTitle(mPlanetTitles[position]);
         //setTitle("BOB");
 
         try {
             refreshDrawerContents();
         } catch(Exception e) {
             //Cheers(e.getMessage()+" "+e.getLocalizedMessage());
         }
 
     }
 
     private void Cheers(String s) {
         Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
     }
 
     @Override
     public void setTitle(CharSequence title) {
         mTitle = title;
        getActionBar().setTitle(mTitle);
         // Cheers(title+" "+mTitle);
     }
 
     public void refreshDrawerContents() {
         mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, mPlanetTitles));
     }
 
     /**
      * When using the ActionBarDrawerToggle, you must call it during
      * onPostCreate() and onConfigurationChanged()...
      */
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         // Pass any configuration change to the drawer toggls
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     /**
      * Fragment that appears in the "content_frame", shows a planet
 
     public static class PlanetFragment extends android.app.Fragment {
         public static final String ARG_PLANET_NUMBER = "planet_number";
 
         public PlanetFragment() {
             // Empty constructor required for fragment subclasses
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_planet, container, false);
             //@TODO Remove frag_planet references
             int i = getArguments().getInt(ARG_PLANET_NUMBER);
             String planet = getResources().getStringArray(R.array.planets_array)[i];
 
             int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                     "drawable", getActivity().getPackageName());
             ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
             //getActivity().setTitle(planet);
             return rootView;
         }
     }*/
     @Override
     protected void onActivityResult(
             int requestCode, int resultCode, Intent data) {
         // Decide what to do based on the original request code
         //Cheers(String.valueOf(requestCode));
         switch (requestCode) {
             case CONNECTION_FAILURE_RESOLUTION_REQUEST :
             /*
              * If the result code is Activity.RESULT_OK, try
              * to connect again
              */
                 switch (resultCode) {
                     case Activity.RESULT_OK :
                     /*
                      * Try the request again
                      */
                         break;
                 }
              break;
             case ACTION_TAKE_PHOTO_B: {
                 //Cheers(resultCode+" B");
                 if (resultCode == RESULT_OK) {
                     handleBigCameraPhoto();
 
                 }
                 break;
             } // ACTION_TAKE_PHOTO_B
 
             case ACTION_TAKE_PHOTO_S: {
                 if (resultCode == RESULT_OK) {
                     handleSmallCameraPhoto(data);
                 }
                 break;
             } // switch
             case READ_REQUEST_CODE:
                 //Cheers(String.valueOf(resultCode));
                 if(resultCode == RESULT_OK){
                     try {
                         Intent upload = new Intent(this, UploadPhoto.class);
 
                         Uri selectedImage = data.getData();
                         //Cheers(selectedImage.toString());
                         try {
                         String[] filePathColumn = {MediaStore.Images.Media.DATA};
 
                         Cursor cursor = getContentResolver().query(
                                 selectedImage, filePathColumn, null, null, null);
                         cursor.moveToFirst();
 
                         int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                         String FilePath = cursor.getString(columnIndex);
                         cursor.close();
                             upload.putExtra("path", FilePath);
                             upload.putExtra("name", FilePath);
                             //Cheers(selectedImage.toString());
                             //Cheers(FilePath);
                             try {
                                 if(FilePath.length() > 0)
                                     startActivity(upload);
                                 else
                                     Cheers(getResources().getString(R.string.photovoid));
                             } catch(Exception e) {
                                 Cheers(getResources().getString(R.string.photovoid));
                             }
                         } catch(Exception e) {
                             Cheers(getResources().getString(R.string.photovoid));
                         }
 
 
                     } catch(Exception e) {
                         Cheers(getResources().getString(R.string.photovoid));
                     }
                 } else {
                     if(backtwo) {
                        // Cheers("Going back two steps!");
                         try {
                             onBackPressed();
                         } catch(Exception e) {
                             Cheers(e.getMessage()+"");
                         }
                     }
                 }
             break;
             /*case READ_REQUEST_CODE: {
                 Cheers("Data");
                 *//*try {
                    Uri uri = null;
                    *//**//*if (data != null) {
                     uri = data.getData();
                     //Log.i(TAG, "Uri: " + uri.toString());
                     //showImage(uri);
 
                     //Intent upload = new Intent(this, UploadPhoto.class);
 
                     //upload.putExtra("path", uri);
                     //upload.putExtra("name", uri);
                     Cheers(data.getData().getPath());
                     Cheers(data.getData().getHost());
                     Cheers("Chose image file "+uri+" from file storage. Sorry, this feature doesn't work yet.");
 
                         //startActivity(upload);
 
                 }*//**//*
                     } catch(Exception e) {
                         Cheers(e.getMessage());
                     }*//*
 
             }
             */
            case PICKFILE_RESULT_CODE:
                 //Cheers(String.valueOf(READ_REQUEST_CODE));
                 if(resultCode==RESULT_OK){
                     String FilePath = String.valueOf(data.getData());
                     Intent upload = new Intent(this, UploadPhoto.class);
                     upload.putExtra("path", FilePath);
                     upload.putExtra("name", FilePath);
                     Cheers("Chose image file "+FilePath+" from content getter. Sorry, this feature doesn't work yet.");
                     Cheers(data.getData().getPath());
                     Cheers(data.getData().getHost());
                     startActivity(upload);
                 }
                 break;
             default:
                 Cheers("Hi");
             break;
         }
     }
     /*
     private boolean servicesConnected() {
         // Check that Google Play services is available
         int resultCode = GooglePlayServicesUtil.
                 isGooglePlayServicesAvailable(this);
         // If Google Play services is available
         if (ConnectionResult.SUCCESS == resultCode) {
             // In debug mode, log the status
             Log.d("Location Updates",
                     "Google Play services is available.");
             // Continue
             return true;
             // Google Play services was not available for some reason
         } else {
             // Get the error code
             //int errorCode = connectionResult.getErrorCode();
             // Get the error dialog from Google Play services
             Cheers("Google Play Services Error!");
             return false;
         }
     }
     */
 
     /*** *** NOTIFICATIONS *** ***/
     public void testNote(String event, String location) {
         if(event.length() == 0)
             event = "Physics Competition";
         if(location.length() == 0)
             location = "Rowan Hall Auditorium";
         Bitmap icon = BitmapFactory.decodeResource(this.getApplicationContext().getResources(),
                 R.drawable.note);
         Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
         MediaPlayer mp = new MediaPlayer();
         mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
         NotificationCompat.Builder mBuilder =
                 new NotificationCompat.Builder(this)
                         .setSmallIcon(R.drawable.note_icon)
                         .setContentTitle("SAC Hint")
                         .setLargeIcon(icon)
                         .setWhen(System.currentTimeMillis()/* + 1000 * 60 * 30*/)
                         .setContentText(event + " - 30 minutes")
                         .setSound(soundUri);
         // Creates an explicit intent for an Activity in your app
         Intent resultIntent = new Intent(this, MainActivity.class);
 
         //Make BIG notifications
         NotificationCompat.InboxStyle inboxStyle =
                 new NotificationCompat.InboxStyle();
         inboxStyle.setBigContentTitle("SAC Hint");
         inboxStyle.addLine("The "+event+" will begin in thirty minutes.");
         inboxStyle.addLine("This will take place at "+location+".");
         inboxStyle.addLine("Please make sure you are on the bus and ready in time.");
         mBuilder.setStyle(inboxStyle);
 
         // The stack builder object will contain an artificial back stack for the
 // started Activity.
 // This ensures that navigating backward from the Activity leads out of
 // your application to the Home screen.
         TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 // Adds the back stack for the Intent (but not the Intent itself)
         stackBuilder.addParentStack(MainActivity.class);
 // Adds the Intent that starts the Activity to the top of the stack
         stackBuilder.addNextIntent(resultIntent);
         PendingIntent resultPendingIntent =
                 stackBuilder.getPendingIntent(
                         0,
                         PendingIntent.FLAG_UPDATE_CURRENT
                 );
         mBuilder.setContentIntent(resultPendingIntent);
         NotificationManager mNotificationManager =
                 (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         // mId allows you to update the notification later on.
         mNotificationManager.notify(mId, mBuilder.build());
     }
 
 }
