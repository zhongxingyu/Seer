 package org.g_okuyama.capture;
 
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.hardware.Camera;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore.Images.Media;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.ngigroup.adstir.AdstirView;
 import com.ngigroup.adstir.AdstirTerminate;
 
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
     
     public static final int HIDDEN_WIDTH = /*64*/96; 
     public static final int HIDDEN_HEIGHT = /*48*/72;
     public static final int MARGIN_HEIGHT = 120;
 
     SurfaceHolder mHolder;
     private int mCount = 0;
     private TextView mText;
     private CameraPreview mPreview = null;
     //Beۂi0:~A1FBej
     public int mMode = 0;
     private boolean mMaskFlag = false;
     
     private ImageButton mButton = null;
     private ImageButton mMaskButton = null;
     private String mNum = null;
     private ContentResolver mResolver;
     
     private WebView mWebView = null;
     
     int mWidth = 0;
     int mHeight = 0;
     
     private AdstirView mAdstirView;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	//Log.d(TAG, "enter ContShooting#onCreate");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mNum = getString(R.string.sc_number);
         mResolver = getContentResolver();
         
         //ݒl̎擾
         String effect = ContShootingPreference.getCurrentEffect(this);
         String scene = ContShootingPreference.getCurrentSceneMode(this);
         String white = ContShootingPreference.getCurrentWhiteBalance(this);
         String size = ContShootingPreference.getCurrentPictureSize(this);
         
         //Log.d(TAG, "picsize = " + size);
         
         WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
         Display disp = wm.getDefaultDisplay();
         mWidth = disp.getWidth();
         mHeight = disp.getHeight();
         
         //requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
         SurfaceView sv = (SurfaceView)findViewById(R.id.camera);
         mHolder = sv.getHolder();
 
         //ʕ\
         int height = mHeight - MARGIN_HEIGHT;
         int width = (height / 3) * 4;
         sv.setLayoutParams(new LinearLayout.LayoutParams(width, height));
         //sv.setLayoutParams(new LinearLayout.LayoutParams(544, 320));
 
         mPreview = new CameraPreview(this);
         mPreview.setField(effect, scene, white, size, mWidth, mHeight);
         mHolder.addCallback(mPreview);
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
         mText = (TextView)findViewById(R.id.text1);
     	mText.setText(mNum + System.getProperty("line.separator") + "0");
 
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
     }
     
     private void setListener(){
         mButton = (ImageButton)findViewById(R.id.imgbtn);
         mButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				if(mPreview != null){
 					if(mMode == 0){
 						mPreview.resumePreview();
 						mMode = 1;
 					}
 					else{
 						mPreview.stopPreview();
 						mMode = 0;
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
         
         if(ContShootingPreference.isHidden(this)){
             setToHidden();
         }
         
         //adstirݒ
         LinearLayout layout = (LinearLayout)findViewById(R.id.adspace);
         mAdstirView = new AdstirView(this);
         layout.addView(mAdstirView);
 		/*
         ImageButton plus = (ImageButton)findViewById(R.id.plus);
         plus.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				if(mPreview != null){
 					mPreview.setZoom(true);
 				}
 			}
         });
         
         ImageButton minus = (ImageButton)findViewById(R.id.minus);
         minus.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				if(mPreview != null){
 					mPreview.setZoom(false);
 				}
 			}
         });
         */
         
         /*
         Button focus = (Button)findViewById(R.id.focus);
         focus.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				if(mPreview != null){
 					mPreview.changeFocus();
 				}
 			}
         });
         */    	
     }
     
     public void setToNormal(){
         LinearLayout layout = (LinearLayout)findViewById(R.id.linear);
         layout.removeView(mWebView);
         layout.setLayoutParams(new LinearLayout.LayoutParams(
                 LinearLayout.LayoutParams.WRAP_CONTENT, 
                 LinearLayout.LayoutParams.WRAP_CONTENT));
         mWebView.setWebViewClient(null);
         mWebView.destroy();
         mWebView = null;
 
         int height = mHeight - MARGIN_HEIGHT;
         int width = (height / 3) * 4;
         SurfaceView sv = (SurfaceView)findViewById(R.id.camera);
         sv.setLayoutParams(new LinearLayout.LayoutParams(width, height));
         
         displayNormalMode();
         mMaskFlag = false;
         setTitle(R.string.app_name);
     }
     
     public void setToHidden(){
         //WebView view = (WebView)findViewById(R.id.review);
         mWebView = new WebView(ContShooting.this);
         mWebView.setWebViewClient(new WebViewClient());
         mWebView.getSettings().setJavaScriptEnabled(true);
         mWebView.getSettings().setBuiltInZoomControls(true);
         LinearLayout layout = (LinearLayout)findViewById(R.id.linear);
         layout.setLayoutParams(new LinearLayout.LayoutParams(
                 LinearLayout.LayoutParams.WRAP_CONTENT, 
                 LinearLayout.LayoutParams.MATCH_PARENT, 
                 1));
         layout.addView(mWebView);
         
         if(Locale.getDefault().equals(Locale.JAPAN)){
             mWebView.loadUrl(URL_JP);
         }
         else{
             mWebView.loadUrl(URL_OTHER);
         }
 
         SurfaceView sv = (SurfaceView)findViewById(R.id.camera);
         sv.setLayoutParams(new LinearLayout.LayoutParams(HIDDEN_WIDTH, HIDDEN_HEIGHT));
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
     	            	Toast.makeText(this, R.string.sc_menu_gallery_ng, Toast.LENGTH_SHORT).show();
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
         }
     }
     
     public void count(){
     	mText.setText(mNum + System.getProperty("line.separator") + Integer.toString(++mCount));
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
     
     public void saveGallery(ContentValues values){
 		mResolver.insert(Media.EXTERNAL_CONTENT_URI, values);
     }
     
     public void setMode(int mode){
         mMode = mode;
     }
     
     public boolean isMask(){
         return mMaskFlag;
     }
     
     protected void onPause(){
         //Log.d(TAG, "enter ContShooting#onPause");    	
     	super.onPause();
     	if(mAdstirView != null){
     	    mAdstirView.stop();
     	}
     }
     
     protected void onDestroy(){
         //Log.d(TAG, "enter ContShooting#onDestroy");
     	super.onDestroy();
         new AdstirTerminate(this);
     	if(mPreview != null){
     	    mPreview.release();
     	}
     }
     
     protected void onRestart(){
     	//Log.d(TAG, "enter ContShooting#onRestart");
     	super.onRestart();
     }
     
     public void finish(){
         //Log.d(TAG, "enter ContShooting#finish");
     	/*
     	new AlertDialog.Builder(this)
     	.setTitle(R.string.pi_finish)
     	.setMessage(getString(R.string.pi_finish_confirm))
     	.setPositiveButton(R.string.pi_ok, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				System.exit(RESULT_OK);
 			}
 		})
 		.setNegativeButton(R.string.pi_ng, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				return;
 			}
 		})
 		.show();
 		*/
    	
 		System.exit(RESULT_OK);
     }
 }
