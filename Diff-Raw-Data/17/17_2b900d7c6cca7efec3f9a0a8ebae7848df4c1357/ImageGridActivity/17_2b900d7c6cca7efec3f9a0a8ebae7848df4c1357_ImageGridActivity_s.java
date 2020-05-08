 
 package com.albaniliu.chuangxindemo.ui.home;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.ScaleAnimation;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.albaniliu.chuangxindemo.ImageShow;
 import com.albaniliu.chuangxindemo.R;
 import com.albaniliu.chuangxindemo.util.HTTPClient;
 import com.albaniliu.chuangxindemo.util.ResourceUtils;
 import com.albaniliu.chuangxindemo.util.Utils;
 
 import static com.albaniliu.chuangxindemo.ui.home.HomeActivity.*;
 
 public class ImageGridActivity extends Activity implements View.OnClickListener {
     private static String TAG = "ImageGridActivity";
 
    private Thread downloadThread;

     public static final int MSG_CHECK_HOME_RESOURCE_LOADING = 1001;
 
     public static int DEFAULT_BANNER_COUNT = 5;
 
     public static final String Id = "ImageGridActivity";
 
     private LinearLayout classfiView;
 
     private boolean mPopupVisible = false;
     private LinearLayout mPopup;
     private Button mMenuBtn;
     private ProgressDialog  dialog;
 
     private ScaleAnimation mInAnimation;
     private ScaleAnimation mOutAnimation;
 
     private Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             if (msg.what == MSG_DOWNLOAD_FINISHED) {
                 setDefaultClassfiView();
                 dialog.dismiss();
                 classfiView.setVisibility(View.VISIBLE);
             } else if (msg.what == MSG_DOWNLOAD_FAILED) {
                 Toast.makeText(getBaseContext(), "下载失败", Toast.LENGTH_LONG).show();
             }
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         Log.i("HomeActivity", "onCreate");
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.home_activity);
         ResourceUtils.setContext(this);
         classfiView = (LinearLayout) this.findViewById(R.id.classfi_view);
         setDefaultClassfiView();
 
         mPopup = (LinearLayout) findViewById(R.id.menu_pop_up);
         int popupButtonCount = mPopup.getChildCount();
         for (int i = 0; i < popupButtonCount; i++) {
             mPopup.getChildAt(i).setOnClickListener(this);
         }
         mMenuBtn = (Button) findViewById(R.id.menu_btn);
 
//        downloadThread = new DownloadThread();
//        downloadThread.start();
//        
//        if (dialog == null) {
//            dialog = new ProgressDialog(this);
//            dialog.setTitle("please wait");
//            dialog.setMessage("downloading!!");
//        }
//        dialog.show();
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
                     }
                 });
             }
             mPopup.setAnimation(null);
             mPopup.startAnimation(mOutAnimation);
         }
     }
 
     private void setDefaultClassfiView() {
         classfiView.removeAllViews();
         for (int i = 0; i < 7; i++) {
             setDefaultClassfiLine(i + 1);
         }
     }
 
     private void setDefaultClassfiLine(final int line) {
         LinearLayout classfiLine = (LinearLayout) getLayoutInflater().inflate(
                 R.layout.classfi_line, null);
         int screenWidth = getWindow().getWindowManager().getDefaultDisplay()
                 .getWidth();
         int screenHeight = getWindow().getWindowManager().getDefaultDisplay()
                 .getHeight();
         int num = 2;
         if (screenWidth > screenHeight)
             num = 4;
         for (int i = 0; i < num; i++) {
             LinearLayout classfiImage = (LinearLayout) getLayoutInflater().inflate(
                     R.layout.classfi_image, null);
             FrameLayout frame = (FrameLayout) classfiImage.findViewById(R.id.left);
             LinearLayout des = (LinearLayout) classfiImage.findViewById(R.id.des_layout);
             des.setVisibility(View.GONE);
             classfiLine.addView(classfiImage);
             final int index = i;
             frame.setOnClickListener(new View.OnClickListener() {
                 
                 @Override
                 public void onClick(View v) {
                    if (downloadThread.isAlive()) {
                        dialog.show();
                        return;
                    }
                     Intent intent = new Intent();
                     intent.putExtra("line", line);
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
         switch (id) {
             case R.id.menu_refresh:
                 break;
             case R.id.menu_more:
                 break;
             case R.id.whole:
                 hidePopup();
                 break;
         }
 
     }
 }
