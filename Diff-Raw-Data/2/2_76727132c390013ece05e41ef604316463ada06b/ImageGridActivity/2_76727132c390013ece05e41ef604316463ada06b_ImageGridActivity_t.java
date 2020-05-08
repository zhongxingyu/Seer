 
 package com.albaniliu.chuangxindemo.ui.home;
 
 import static com.albaniliu.chuangxindemo.ui.home.HomeActivity.MSG_DOWNLOAD_FAILED;
 import static com.albaniliu.chuangxindemo.ui.home.HomeActivity.MSG_DOWNLOAD_FINISHED;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.ScaleAnimation;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.albaniliu.chuangxindemo.ImageShow;
 import com.albaniliu.chuangxindemo.R;
 import com.albaniliu.chuangxindemo.data.FInode;
 import com.albaniliu.chuangxindemo.util.Downloader;
 import com.albaniliu.chuangxindemo.util.HTTPClient;
 import com.albaniliu.chuangxindemo.util.ResourceUtils;
 import com.albaniliu.chuangxindemo.util.Utils;
 
 public class ImageGridActivity extends Activity implements View.OnClickListener {
     private static String TAG = "ImageGridActivity";
 
     public static final int MSG_CHECK_HOME_RESOURCE_LOADING = 1001;
 
     public static int DEFAULT_BANNER_COUNT = 5;
 
     public static final String Id = "ImageGridActivity";
 
     private LinearLayout classfiView;
 
     private boolean mPopupVisible = false;
     private LinearLayout mPopup;
     private ImageButton mMenuBtn;
     private ProgressDialog  dialog;
 
     private ScaleAnimation mInAnimation;
     private ScaleAnimation mOutAnimation;
     
     private Downloader downloader;
     private String inodePath;
     private FInode currentInode;
     private JSONArray allImages;
     private int totalIndex;
 
     private Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             if (msg.what == MSG_DOWNLOAD_FINISHED) {
                 setDefaultClassfiView();
                 classfiView.setVisibility(View.VISIBLE);
             } else if (msg.what == MSG_DOWNLOAD_FAILED) {
                 Toast.makeText(getBaseContext(), "下载失败", Toast.LENGTH_LONG).show();
             }
         }
     };
     
     private ServiceConnection mServiceConnection = new ServiceConnection() {
         //当我bindService时，让TextView显示MyService里getSystemTime()方法的返回值   
         public void onServiceConnected(ComponentName name, IBinder service) {  
             // TODO Auto-generated method stub
         	downloader = ((Downloader.MyBinder)service).getService();
         	Log.v(TAG, Boolean.toString(downloader.isFinished()));
         	if (downloader.isFinished()) {
 	        	currentInode = downloader.getLeaf(inodePath);
 	        	mHandler.sendEmptyMessageDelayed(MSG_DOWNLOAD_FINISHED, 200);
         	}
         }
         
         public void onServiceDisconnected(ComponentName name) {  
             // TODO Auto-generated method stub  
               
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         Log.i("HomeActivity", "onCreate");
         super.onCreate(savedInstanceState);
         Bundle extras = getIntent().getExtras();
         inodePath = extras.getString("inode_path");
         currentInode = new FInode();
         this.setContentView(R.layout.home_activity);
         ResourceUtils.setContext(this);
         classfiView = (LinearLayout) this.findViewById(R.id.classfi_view);
         setDefaultClassfiView();
 
         mPopup = (LinearLayout) findViewById(R.id.menu_pop_up);
         int popupButtonCount = mPopup.getChildCount();
         for (int i = 0; i < popupButtonCount; i++) {
             mPopup.getChildAt(i).setOnClickListener(this);
         }
         mMenuBtn = (ImageButton) findViewById(R.id.menu_btn);
 
         Intent i  = new Intent();
         i.setClass(ImageGridActivity.this, Downloader.class);
         this.bindService(i, mServiceConnection, BIND_AUTO_CREATE);
         
     }
 
     public void onMenuClick(View view) {
         if (mPopupVisible) {
             hidePopup();
         } else {
             showPopup();
         }
     }
 
     private void showPopup() {
         if (!mPopupVisible) {
             mPopupVisible = true;
             mMenuBtn.setImageResource(R.drawable.titlebar_icon_more_hl);
             if (mInAnimation == null) {
                 mInAnimation = new ScaleAnimation(
                         0, 1, 0, 1, mPopup.getWidth() - Utils.dip2px(this, 19), 0);
                 mInAnimation.setDuration(300);
                 mInAnimation.setInterpolator(this, android.R.anim.accelerate_interpolator);
                 mInAnimation.setAnimationListener(new AnimationListener() {
 
                     @Override
                     public void onAnimationStart(Animation animation) {
                         mPopup.setVisibility(View.VISIBLE);
                     }
 
                     @Override
                     public void onAnimationRepeat(Animation animation) {
 
                     }
 
                     @Override
                     public void onAnimationEnd(Animation animation) {
                         mPopup.setVisibility(View.VISIBLE);
                     }
                 });
             }
             mPopup.setAnimation(null);
             mPopup.startAnimation(mInAnimation);
         }
     }
 
     private void hidePopup() {
         if (mPopupVisible) {
             mPopupVisible = false;
             mMenuBtn.setImageResource(R.drawable.titlebar_icon_more);
             if (mOutAnimation == null) {
                 mOutAnimation = new ScaleAnimation(
                         1, 0, 1, 0, mPopup.getWidth() - Utils.dip2px(this, 19), 0);
                 mOutAnimation.setDuration(300);
                 mOutAnimation.setInterpolator(this, android.R.anim.accelerate_interpolator);
                 mOutAnimation.setAnimationListener(new AnimationListener() {
                     @Override
                     public void onAnimationStart(Animation animation) {
 
                     }
 
                     @Override
                     public void onAnimationRepeat(Animation animation) {
 
                     }
 
                     @Override
                     public void onAnimationEnd(Animation animation) {
                         mPopup.setVisibility(View.INVISIBLE);
                         mMenuBtn.setImageResource(R.drawable.more_btn_selector);
                     }
                 });
             }
             mPopup.setAnimation(null);
             mPopup.startAnimation(mOutAnimation);
         }
     }
 
     private void setDefaultClassfiView() {
         classfiView.removeAllViews();
         totalIndex = 0;
         int line = 0;
         allImages = currentInode.getDirs();
         Log.v(TAG, allImages.toString());
         while (totalIndex < allImages.length()) {
             setDefaultClassfiLine(line++);
         }
     }
 
     private void setDefaultClassfiLine(final int line) {
         LinearLayout classfiLine = (LinearLayout) getLayoutInflater().inflate(
                 R.layout.classfi_line, null);
         int screenWidth = getWindow().getWindowManager().getDefaultDisplay()
                 .getWidth();
         int screenHeight = getWindow().getWindowManager().getDefaultDisplay()
                 .getHeight();
         int num = 3;
         int padding = 10;
         if (screenWidth > screenHeight) {
         	num = 4;
         	padding = 12;
         }
         classfiLine.setPadding(padding, 1, padding, 1);
         for (int i = 0; i < num && totalIndex < allImages.length(); i++, totalIndex++) {
             LinearLayout classfiImage = (LinearLayout) getLayoutInflater().inflate(
                     R.layout.grid_classfi_image, null);
             FrameLayout frame = (FrameLayout) classfiImage.findViewById(R.id.left);
             LinearLayout des = (LinearLayout) classfiImage.findViewById(R.id.des_layout);
             des.setVisibility(View.GONE);
             
             try {
             	JSONObject obj = (JSONObject) allImages.get(totalIndex);
 	            ImageView image = (ImageView) classfiImage.findViewById(R.id.image_left);
 	            Log.v(TAG, obj.getString("attrib"));
 	            if (!obj.getString("attrib").equals("image")) {
 	            	continue;
 	            }
 	            String coverPath = obj.getString("path");
 	            Log.v(TAG, coverPath);
                 String coverName = coverPath.substring(coverPath.lastIndexOf('/') + 1);
 	            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liangdemo1/"
 	                    + coverName;
 	            Bitmap bitmap = Utils.createBitmapByFilePath(fileName, 200);
 	            image.setImageBitmap(bitmap);
 	            
 	            TextView txt = (TextView) classfiImage.findViewById(R.id.des);
 				txt.setText(obj.getString("name"));
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
             
             classfiLine.addView(classfiImage);
             final int index = num * line + i;
             frame.setOnClickListener(new View.OnClickListener() {
                 
                 @Override
                 public void onClick(View v) {
                     mPopup.setVisibility(View.INVISIBLE);
                     mPopupVisible = false;
                     Intent intent = new Intent();
                     intent.putExtra("inode", inodePath);
                     intent.putExtra("index", index);
                     intent.setClass(getApplicationContext(), ImageShow.class);
                     startActivity(intent);
                 }
             });
         }
 
         classfiView.addView(classfiLine);
     }
 
     class DownloadThread extends Thread {
         public void run() {
             try {
                 JSONArray allDir = HTTPClient.getJSONArrayFromUrl(HTTPClient.URL_INDEX);
                 for (int i = 0; i < allDir.length(); i++) {
                     JSONObject obj = (JSONObject) allDir.get(i);
                     Log.v(TAG, obj.getString("id"));
                 }
                 mHandler.sendEmptyMessage(MSG_DOWNLOAD_FINISHED);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void onClick(View v) {
         int id = v.getId();
         hidePopup();
         switch (id) {
             case R.id.menu_refresh:
                 if (downloader != null) {
                     downloader.refreshForce();
                     dialog.show();
                 }
                 break;
             case R.id.menu_more:
                 Intent intent = new Intent();
                intent.putExtra("slideshow", true);
                 intent.putExtra("inode", inodePath);
                 intent.setClass(getApplicationContext(), ImageShow.class);
                 startActivity(intent); 
                 break;
             case R.id.whole:
                 break;
         }
 
     }
 }
