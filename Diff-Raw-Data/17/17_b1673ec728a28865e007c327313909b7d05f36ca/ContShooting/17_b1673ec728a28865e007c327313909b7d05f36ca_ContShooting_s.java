 package org.g_okuyama.capture;
 
 import java.io.File;
 import java.util.List;
 import java.util.Locale;
 
 import com.ad_stir.interstitial.AdstirInterstitial.AdstirInterstitialDialogListener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Canvas;
 import android.hardware.SensorManager;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore.Images.Media;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.OrientationEventListener;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.animation.RotateAnimation;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class ContShooting extends Activity {
     private static final String TAG = "ContShooting";
     public static final String URL_JP = "http://www.yahoo.co.jp";
     public static final String URL_OTHER = "http://www.yahoo.com";
     
     static final int MENU_DISP_GALLERY = 1;
     static final int MENU_DISP_SETTING = 2;
 
     static final int REQUEST_CODE = 1;
     static final int RESPONSE_COLOR_EFFECT = 1;
     static final int RESPONSE_SCENE_MODE = 2;
     static final int RESPONSE_WHITE_BALANCE = 3;
     static final int RESPONSE_PICTURE_SIZE = 4;
     static final int RESPONSE_SHOOT_NUM = 5;
     static final int RESPONSE_INTERVAL = 6;
     static final int RESPONSE_HIDDEN_SIZE = 7;
     
     SurfaceHolder mHolder;
     SurfaceView mSurface;
     Canvas mCanvas;
     private int mCount = 0;
     private TextView mText;
     private CameraPreview mPreview = null;
     //Beۂi0:~A1FBej
     public int mMode = 0;
     private boolean mMaskFlag = false;
     private boolean mSleepFlag = false;
     private boolean mAutoFlag = false;
     
     private OverlayView mOverlay;
     
     private ImageButton mButton = null;
     private ImageButton mMaskButton = null;
     private ImageButton mFocusButton = null;
     
     private SeekBar mEVSeekBar = null;
     private ImageView mEVOut = null;
     private ImageView mEVIn = null;
 
     private SeekBar mZoomSeekBar = null;
     private ImageView mZoomOut = null;
     private ImageView mZoomIn = null;
     
     //private String mNum = null;
     private ContentResolver mResolver;
     
     OrientationEventListener mOrientationListener;
     int mDegree = 0;
     int mPrevTarget = 0;
     
     private WebView mWebView = null;
     //Ŝ̉ʃTCY
     int mWidth = 0;
     int mHeight = 0;
     
     int mHiddenSizeIdx = 0;
     
     //ad
     com.ad_stir.interstitial.AdstirInterstitial mInterstitial;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	//Log.d(TAG, "enter ContShooting#onCreate");
         super.onCreate(savedInstanceState);
         //for 2.5
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         setContentView(R.layout.main);
         
         mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
             @Override
             public void onOrientationChanged(int orientation) {
                 int degree = 0;
                 if ( orientation > 0 && orientation <= 45) {
                     degree = 0;
                 } else
                 if ( orientation > 45 && orientation <= 135) {
                     degree = 90;
                 } else
                 if ( orientation > 135 && orientation <= 225) {
                     degree = 180;
                 } else
                 if ( orientation > 225 && orientation <= 315) {
                     degree = 270;
                 } else { 
                     degree = 0;
                 }
                 
                 if (mDegree != degree) {
                     mDegree = degree;
                     //ɉă{^]
                     rotate(degree);
                 }
             }
         };
         mOrientationListener.enable();
         
         /*mNum = getString(R.string.sc_number);*/
         mResolver = getContentResolver();
         
         //ݒl̎擾
         String effect = ContShootingPreference.getCurrentEffect(this);
         String scene = ContShootingPreference.getCurrentSceneMode(this);
         String white = ContShootingPreference.getCurrentWhiteBalance(this);
         String size = ContShootingPreference.getCurrentPictureSize(this);
         mHiddenSizeIdx = Integer.parseInt(ContShootingPreference.getCurrentHiddenSize(this));
         
         //Log.d(TAG, "picsize = " + size);
         
         WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
         Display disp = wm.getDefaultDisplay();
         mWidth = disp.getWidth();
         mHeight = disp.getHeight();
         
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
         mSurface = (SurfaceView)findViewById(R.id.camera);
         mHolder = mSurface.getHolder();
 
         mPreview = new CameraPreview(this);
         mPreview.setField(effect, scene, white, size, mWidth, mHeight);
         mHolder.addCallback(mPreview);
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
         mText = (TextView)findViewById(R.id.text1);
     	mText.setText(/*mNum + System.getProperty("line.separator") + */"0" + " ");
 
     	//Aʖݒ
         String num = ContShootingPreference.getCurrentShootNum(this);
         if(!num.equals("0")){
             mPreview.setShootNum(Integer.valueOf(num));
         }
         
         //AʊԊuݒ
         String interval = ContShootingPreference.getCurrentInterval(this);
         if(!interval.equals("0")){
             mPreview.setInterval(Integer.valueOf(interval));
         }
 
         //register UI Listener
     	setListener();
     	
     	if(ContShootingPreference.isAutoShoot(this)){
     		mAutoFlag = true;
     	}
     	
     	mInterstitial = new com.ad_stir.interstitial.AdstirInterstitial("MEDIA-e6dc95a",2);
     	mInterstitial.load();
     }
     
     private void rotate(int degree){
         ImageButton[] btns = {mButton, mMaskButton, mFocusButton};
         
         int target = 0;
         if(degree == 0){
         	target = isPhone() ? 0 : 90;
         }
         else if(degree == 90){
         	target = isPhone() ? -90 : 0;
         }
         else if(degree == 180){
         	target = isPhone() ? 180 : -90;
         }
         else if(degree == 270){
         	target = isPhone() ? 90 : 180;
         }
 
         for(ImageButton btn : btns){
             if(btn.equals(mMaskButton) && mMode == 1){
                 continue;
             }
             RotateAnimation rotate = new RotateAnimation(mPrevTarget, target, btn.getWidth()/2, btn.getHeight()/2);
             rotate.setDuration(500);
             rotate.setFillAfter(true);
             btn.startAnimation(rotate);
         }
         
         RotateAnimation rotatetxt = new RotateAnimation(mPrevTarget, target, mText.getWidth()/2, mText.getHeight()/2);
         rotatetxt.setDuration(500);
         rotatetxt.setFillAfter(true);
         mText.startAnimation(rotatetxt);
         
         ImageView[] imgs = {mZoomIn, mZoomOut, mEVIn, mEVOut};
         
         for(ImageView img: imgs){
             RotateAnimation rotateimg = new RotateAnimation(mPrevTarget, target, img.getWidth()/2, img.getHeight()/2);
             rotateimg.setDuration(500);
             rotateimg.setFillAfter(true);
             img.startAnimation(rotateimg);
         }
         
         mPrevTarget = target;
     }
     
     private void setListener(){
         mButton = (ImageButton)findViewById(R.id.imgbtn);
         mButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				if(mPreview != null){
 					if(mMode == 0){
 						start();
 					}
 					else{
 						stop();
 					}
 				}
 			}
         });
         
         mMaskButton = (ImageButton)findViewById(R.id.mask_imgbtn);
         mMaskButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				if(mPreview != null){
                     if(mMaskFlag){
                         setToNormal();
                     }
                     else{
                         setToHidden();
                     }
 				}
 			}
         });
         
         mFocusButton = (ImageButton)findViewById(R.id.focusbtn);
         mFocusButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 			    displayFocus();
 			    
 				if(mPreview != null){
 				    mPreview.doAutoFocus();
 				}
 			}
         });
         
         if(ContShootingPreference.isHidden(this)){
             setToHidden();
         }
         
         //seekbar for EV
         mEVSeekBar = (SeekBar)findViewById(R.id.ev_seek);
        /*
        int inc = mSeekBar.getKeyProgressIncrement();
        Log.d(TAG, "increment = " + inc);
        mSeekBar.setKeyProgressIncrement(50);
        inc = mSeekBar.getKeyProgressIncrement();
        Log.d(TAG, "increment = " + inc);
        */
         mEVSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
             public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
             	//10̔{łȂƂ͉Ȃ
             	//if(!(progress % 10 == 0)){
             	//return;
             	//}
             	//->Ał͂Ȃʂ̏ꏊ^bvƂɏƓxύXłȂߍ폜
             	
 				if(mPreview != null){
 	                mPreview.setExposureValue(progress);
 				}
             }
 
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
             
         });
         
         mEVIn = (ImageView)findViewById(R.id.ev_in);
         mEVOut = (ImageView)findViewById(R.id.ev_out);
         
         //seekbar for zoom
         mZoomSeekBar = (SeekBar)findViewById(R.id.zoom_seek);
         mZoomSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
             public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                //Log.d(TAG, "progress = " + progress);
 				if(mPreview != null){
 	                mPreview.setZoom(progress);
 				}
             }
 
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
             
         });
         
         mZoomIn = (ImageView)findViewById(R.id.zoom_in);
         mZoomOut = (ImageView)findViewById(R.id.zoom_out);
 
         //`pViewǉ
         mOverlay = new OverlayView(mPreview, this);
         FrameLayout frame = (FrameLayout)findViewById(R.id.camera_parent);
         frame.addView(mOverlay, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
     }
     
     void start(){
 		mPreview.resumeShooting();
 		mMode = 1;
         //tH[JX{^Ȃ
 		//for 2.7 BełtH[JXł悤ɂ
         //mFocusButton.setVisibility(View.INVISIBLE);
 
 		//Aj[VNAĂłȂvisibilityłȂ߃NA
         mMaskButton.clearAnimation();
         mMaskButton.setVisibility(View.INVISIBLE);
         
         if(mPreview.isEVSupported() || mPreview.isZoomSupported()){
         	FrameLayout ev = (FrameLayout)findViewById(R.id.settings_layout);
         	ev.setVisibility(View.INVISIBLE);
         }
     }
     
     void stop(){
 		mPreview.stopShooting();
 		mMode = 0;
         //tH[JX{^悤ɂ
         //mFocusButton.setVisibility(View.VISIBLE);
         mMaskButton.clearAnimation();
         mMaskButton.setVisibility(View.VISIBLE);
         if(mDegree != 0){
             RotateAnimation rotate = new RotateAnimation(
                     0, 
                     mPrevTarget, 
                     mMaskButton.getWidth()/2, 
                     mMaskButton.getHeight()/2);
             rotate.setDuration(0);
             rotate.setFillAfter(true);
             mMaskButton.startAnimation(rotate);
         }
         
         if(mPreview.isEVSupported() || mPreview.isZoomSupported()){
         	FrameLayout ev = (FrameLayout)findViewById(R.id.settings_layout);
         	ev.setVisibility(View.VISIBLE);
         }
     }
     
     void clearCanvas(){
         enableMask();
 
         if(mOverlay != null){
             mOverlay.clearCanvas();
         }        
     }
     
     void displayFocus(){
         disableMask();
         
         if(mOverlay != null){
             mOverlay.displayFocus();
         }
     }
     
     void enableMask(){
         //B[h{^\(Beȉ)
         if(mMode == 0){
             mMaskButton.clearAnimation();
             mMaskButton.setVisibility(View.VISIBLE);
             if(mDegree != 0){
                 RotateAnimation rotate = new RotateAnimation(
                         0, 
                         mPrevTarget, 
                         mMaskButton.getWidth()/2, 
                         mMaskButton.getHeight()/2);
                 rotate.setDuration(0);
                 rotate.setFillAfter(true);
                 mMaskButton.startAnimation(rotate);
             }
         }        
     }
     
     void disableMask(){
         //B[h{^Ȃ
         mMaskButton.clearAnimation();
         mMaskButton.setVisibility(View.INVISIBLE);
     }
     
     public void setToNormal(){
         FrameLayout layout = (FrameLayout)findViewById(R.id.linear);
         layout.removeView(mWebView);
         layout.setLayoutParams(new FrameLayout.LayoutParams(
                 FrameLayout.LayoutParams.MATCH_PARENT, 
                 FrameLayout.LayoutParams.WRAP_CONTENT));
         mWebView.setWebViewClient(null);
         mWebView.destroy();
         mWebView = null;
 
         FrameLayout frame = (FrameLayout)findViewById(R.id.camera_parent);
         frame.setLayoutParams(new FrameLayout.LayoutParams(
                 FrameLayout.LayoutParams.MATCH_PARENT,
                 FrameLayout.LayoutParams.WRAP_CONTENT));
         //frame.setLayoutParams(new FrameLayout.LayoutParams(mPrevWidth, mPrevHeight, Gravity.CENTER_HORIZONTAL));
 
         displayNormalMode();
         mMaskFlag = false;
         setTitle(R.string.app_name);
     }
     
     public void setToHidden(){
         mWebView = new WebView(ContShooting.this);
         mWebView.setWebViewClient(new WebViewClient());
         mWebView.getSettings().setJavaScriptEnabled(true);
         mWebView.getSettings().setBuiltInZoomControls(true);
         mWebView.getSettings().setAppCacheEnabled(false);
         FrameLayout layout = (FrameLayout)findViewById(R.id.linear);
         layout.setLayoutParams(new FrameLayout.LayoutParams(
                 FrameLayout.LayoutParams.MATCH_PARENT, 
                 FrameLayout.LayoutParams.WRAP_CONTENT, 
                 1));
         layout.addView(mWebView);
         
         if(Locale.getDefault().equals(Locale.JAPAN)){
             mWebView.loadUrl(URL_JP);
         }
         else{
             mWebView.loadUrl(URL_OTHER);
         }
 
     	FrameLayout frame = (FrameLayout)findViewById(R.id.camera_parent);
     	/*
         int hide_height = mHeight / 6;
         int hide_width = hide_height / 3 * 4;
         */
     	/*
     	 * B[h̃vr[TCYݒ
     	 * =1/4, =1/6, =1*1
     	 */
         int denom = 4;
         if(mHiddenSizeIdx == 2){
             denom = 6;
         }
         int hide_width = mWidth / denom;
         int hide_height = hide_width / 3 * 4;
 
         if(mHiddenSizeIdx == 3){
             hide_height = 1;
             hide_width = 1;            
         }
         frame.setLayoutParams(new FrameLayout.LayoutParams(hide_width, hide_height, Gravity.BOTTOM));
         
         displayHideMode();
         mMaskFlag = true;
         setTitle(R.string.sc_hidden);        
 }
     
     public void onStart(){
     	//Log.d(TAG, "enter ContShooting#onStart");
     	
         super.onStart();
         if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
             new AlertDialog.Builder(this)
             .setTitle(R.string.sc_alert_title)
             .setMessage(getString(R.string.sc_alert_sd))
             .setPositiveButton(R.string.sc_alert_ok, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                     System.exit(RESULT_OK);
                 }
             })
             .show();
         }
     }
     
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         //IvVj[(M[)
         MenuItem prefGallery = menu.add(0, MENU_DISP_GALLERY, 0, R.string.sc_menu_gallery);
         prefGallery.setIcon(android.R.drawable.ic_menu_gallery);
 
         //IvVj[(ݒ)
         MenuItem prefSetting = menu.add(0, MENU_DISP_SETTING, 0, R.string.sc_menu_setting);
         prefSetting.setIcon(android.R.drawable.ic_menu_preferences);
 
         return true;
     }
     
     //IvVj[ĨXi
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case MENU_DISP_GALLERY:        	
         	startGallery();
             break;
             
         case MENU_DISP_SETTING:
             displaySettings();
         	break;
             
         default:
             //Ȃ
         }
 
         return true;
     }
     
     private void startGallery(){
     	//M[ւintent
     	//Intent intent = new Intent(Intent.ACTION_PICK);
     	//intent.setType("image/*");
     	//startActivityForResult(intent, REQUEST_PICK_CONTACT);
     	//startActivity(intent);
     	
     	// M[\
     	Intent intent = null;
     	try{
     	    // for Honeycomb
     	    intent = new Intent();
     	    intent.setClassName("com.android.gallery3d", "com.android.gallery3d.app.Gallery");
     	    startActivity(intent);
     	    return;
     	}
     	catch(Exception e){
     	    try{
     	        // for Recent device
     	        intent = new Intent();
     	        intent.setClassName("com.cooliris.media", "com.cooliris.media.Gallery");
     	        startActivity(intent);
     	    }
     	    catch(ActivityNotFoundException e1){
     	        try
     	        {
     	            // for Other device except HTC
     	            intent = new Intent(Intent.ACTION_VIEW);
     	            intent.setData(Uri.parse("content://media/external/images/media"));
     	            startActivity(intent);
     	        }
     	        catch (ActivityNotFoundException e2){
     	        	try{
     	        		// for HTC
     	        		intent = new Intent();
     	        		intent.setClassName("com.htc.album", "com.htc.album.AlbumTabSwitchActivity");
     	        		startActivity(intent);
     	        	}
     	        	catch(ActivityNotFoundException e3){
         	        	try{
         	        		// for HTC
         	        		intent = new Intent();
         	        		intent.setClassName("com.htc.album", "com.htc.album.AlbumMain.ActivityMainDropList");
         	        		startActivity(intent);
         	        	}
         	        	catch(ActivityNotFoundException e4){
         	            	Toast.makeText(this, R.string.sc_menu_gallery_ng, Toast.LENGTH_SHORT).show();
         	        	}
     	        	}
     	        }
     	    }
     	}
     }
     
     private void displaySettings(){
         Intent pref_intent = new Intent(this, ContShootingPreference.class);
 
         //Fݒ̃Xg쐬
         List<String> effectList = null;
         if(mPreview != null){
             effectList = mPreview.getEffectList();
         }
         if(effectList != null){
         	//Log.d(TAG, "effect = " + (String[])effectList.toArray(new String[0]));
             pref_intent.putExtra("effect", (String[])effectList.toArray(new String[0]));
         }
 
         //V[
         List<String> sceneList = null;
         if(mPreview != null){
             sceneList = mPreview.getSceneModeList();
         }
         if(sceneList != null){
             //Log.d(TAG, "scene = " + (String[])sceneList.toArray(new String[0]));
             pref_intent.putExtra("scene", (String[])sceneList.toArray(new String[0]));
         }
 
         //zCgoX
         List<String> whiteList = null;
         if(mPreview != null){
             whiteList = mPreview.getWhiteBalanceList();
         }
         if(whiteList != null){
             //Log.d(TAG, "white = " + (String[])whiteList.toArray(new String[0]));
             pref_intent.putExtra("white", (String[])whiteList.toArray(new String[0]));
         }
         
         //摜TCY
         List<String> sizeList = null;
         if(mPreview != null){
             sizeList = mPreview.getSizeList();
         }
         if(sizeList != null){
             //Log.d(TAG, "size = " + (String[])sizeList.toArray(new String[0]));
             pref_intent.putExtra("size", (String[])sizeList.toArray(new String[0]));
         }
         
         startActivityForResult(pref_intent, REQUEST_CODE);
     }
     
     public void onActivityResult(int requestCode, int resultCode, Intent data){
         if(data == null){
             return;
         }
         
         if(requestCode == REQUEST_CODE){
             if(resultCode == RESPONSE_COLOR_EFFECT){
                 if(mPreview != null){
                     mPreview.setColorValue(data.getStringExtra("effect"));
                 }            	
             }
             if(resultCode == RESPONSE_SCENE_MODE){
             	if(mPreview != null){
                     mPreview.setSceneValue(data.getStringExtra("scene"));
                 }            	            	
             }
             if(resultCode == RESPONSE_WHITE_BALANCE){
             	if(mPreview != null){
                     mPreview.setWhiteValue(data.getStringExtra("white"));
                 }            	
             }
             if(resultCode == RESPONSE_PICTURE_SIZE){
                 if(mPreview != null){
                     mPreview.setSizeValue(data.getIntExtra("size", 0));
                 }
             }
             if(resultCode == RESPONSE_SHOOT_NUM){
                 if(mPreview != null){
                     mPreview.setShootNum(data.getIntExtra("shoot", 0));
                 }
             }
             if(resultCode == RESPONSE_INTERVAL){
                 if(mPreview != null){
                     mPreview.setInterval(data.getIntExtra("interval", 0));
                 }
             }
             if(resultCode == RESPONSE_HIDDEN_SIZE){
                 //B[hTCYݒ
                 mHiddenSizeIdx = data.getIntExtra("hidden_size", 0);
                 //B[h̏ꍇ́AU߂
                 if(mMaskFlag){
                     setToNormal();
                 }
             }
         }
     }
     
     public void count(){
     	mText.setText(/*mNum + System.getProperty("line.separator") + */Integer.toString(++mCount) + " ");
     }
     
     public void displayStart(){
     	mButton.setImageResource(R.drawable.start);
     }
     
     public void displayStop(){
         mButton.setImageResource(R.drawable.stop);
     }
     
     void displayHideMode(){
         mMaskButton.setImageResource(R.drawable.scale_up);
     }
     
     void displayNormalMode(){
         mMaskButton.setImageResource(R.drawable.scale_down);
     }
     
     void invisibleZoom(){
        FrameLayout zoom = (FrameLayout)findViewById(R.id.zoom_layout);
         zoom.setVisibility(View.INVISIBLE);
     }
     
     void invisibleExposureView(){
        FrameLayout ev = (FrameLayout)findViewById(R.id.ev_layout);
         ev.setVisibility(View.INVISIBLE);
     }
     
     public void saveGallery(ContentValues values){
 		mResolver.insert(Media.EXTERNAL_CONTENT_URI, values);
     }
     
     public void setMode(int mode){
         mMode = mode;
     }
     
     public boolean isMask(){
         return mMaskFlag;
     }
     
     private boolean isPhone(){
     	Context context = getApplicationContext();
     	Resources r = context.getResources();
     	Configuration configuration = r.getConfiguration();
     	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
     		if ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
     				< Configuration.SCREENLAYOUT_SIZE_LARGE) {
     			return true;
     		}
 
     	} else {
     		if (configuration.smallestScreenWidthDp < 600) {
     			return true;
     		}
     	}
     	
     	return false;
     }
     
     public int getDegree(){
         return mDegree;
     }
     
     void shootIfAuto(){
     	//NAʐݒ̂ƂɘA
     	if(mAutoFlag){
     		start();
     		mAutoFlag = false;
     	}
     }
     
     protected void onPause(){
         //Log.d(TAG, "enter ContShooting#onPause");    	
     	super.onPause();
     
     	if(mSleepFlag){
             getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
             mSleepFlag = false;
     	}
     	
         //AṽLbV폜
         deleteCache(getCacheDir());
     }
     
     protected void onResume(){
         super.onResume();
 
         if(ContShootingPreference.isSleepMode(this)){
             if(!mSleepFlag){
                 getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                 mSleepFlag = true;                
             }
         }
         else{
             if(mSleepFlag){
                 getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                 mSleepFlag = false;                
             }
         }
     }
     
     protected void onDestroy(){
         //Log.d(TAG, "enter ContShooting#onDestroy");
     	super.onDestroy();
 
     	if(mPreview != null){
     	    mPreview.release();
     	}
     	
         mOrientationListener.disable();
     }
     
     protected void onRestart(){
     	//Log.d(TAG, "enter ContShooting#onRestart");
     	super.onRestart();
     }
     
     @Override
     public void onBackPressed(){
     	//C^[XeBVL\(OKŃAvI)
     	//mInterstitial.showInterstitial(this);
     	mInterstitial.setDialogText(getString(R.string.sc_finish_title));
     	mInterstitial.setPositiveButtonText(getString(R.string.sc_finish_ok));
     	mInterstitial.setNegativeButtonText(getString(R.string.sc_finish_cancel));
     	mInterstitial.setDialoglistener(new AdstirInterstitialDialogListener(){
 			@Override
 			public void onCancel() {
 			}
 
 			@Override
 			public void onNegativeButtonClick() {
 				return;
 			}
 
 			@Override
 			public void onPositiveButtonClick() {
 				finish();
 			}
     	});
     	mInterstitial.showDialog(this);
     }
     
     //delete since ver4.0
     /*
     public void finish(){
 
         //AṽLbV폜
         //deleteCache(getCacheDir());
     	
 		System.exit(RESULT_OK);
     }
      */
     
     public static boolean deleteCache(File dir) {
         if(dir==null) {
             return false;
         }
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i = 0; i < children.length; i++) {
                 boolean success = deleteCache(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
         return dir.delete();
     }
 }
