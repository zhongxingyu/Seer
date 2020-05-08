 
 package com.albaniliu.chuangxindemo;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.albaniliu.chuangxindemo.data.FInode;
 import com.albaniliu.chuangxindemo.data.RandomDataSource;
 import com.albaniliu.chuangxindemo.util.Downloader;
 import com.albaniliu.chuangxindemo.widget.LargePicGallery;
 import com.albaniliu.chuangxindemo.widget.LargePicGallery.SingleTapListner;
 import com.albaniliu.chuangxindemo.widget.SlideShow;
 import com.albaniliu.chuangxindemo.widget.ViewPagerAdapter;
 
 public class ImageShow extends Activity {
     private static final String TAG = "ImageShow";
     private static final int GET_NODE_DONE = 1;
     private ViewPagerAdapter mAdapter;
     private LinearLayout mFlowBar;
     private TextView mFooter;
     private LargePicGallery mPager;
     private String mInodeDes = "3,3";
     private int mCurrentIndex = 0;
     private SlideShow mSlideshow;
     private boolean mSlideShowMode = false;
     private RandomDataSource mRandomDataSource;
 
     public static class ShowingNode {
         private String mPath;
         private String mName;
         private String mContent;
 
         public ShowingNode(String path, String name, String content) {
             mPath = path;
             mName = name;
             mContent = content;
         }
 
         public String getPath() {
             return mPath;
         }
 
         public String getName() {
             return mName;
         }
 
         public String getContent() {
             return mContent;
         }
     }
 
     private Handler mHanler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case GET_NODE_DONE:
                     ArrayList<ShowingNode> nodes = parseFilesPath(mCurrentInode);
                     mRandomDataSource = new RandomDataSource(nodes, mCurrentIndex);
                     if (mSlideShowMode) {
                         mSlideshow.setDataSource(mRandomDataSource);
                     } else {
                         mAdapter = new ViewPagerAdapter(nodes, mListener);
                         mPager.setAdapter(mAdapter);
                         mFooter.setText(mAdapter.getName(mCurrentIndex));
                     }
                     break;
 
                 default:
                     break;
             }
         }
     };
     private Downloader mDownloadService;
     private FInode mCurrentInode;
 
     private ServiceConnection mServiceConnection = new ServiceConnection() {
         // 当我bindService时，让TextView显示MyService里getSystemTime()方法的返回值
         public void onServiceConnected(ComponentName name, IBinder service) {
             mDownloadService = ((Downloader.MyBinder) service).getService();
             Log.v(TAG, Boolean.toString(mDownloadService.isFinished()));
             mCurrentInode = mDownloadService.getLeaf(mInodeDes);
             mHanler.sendEmptyMessage(GET_NODE_DONE);
         }
 
         public void onServiceDisconnected(ComponentName name) {
             // TODO Auto-generated method stub
 
         }
     };
 
     private SingleTapListner mListener = new SingleTapListner() {
         @Override
         public void onSingleTapUp() {
             toggleFlowBar();
         }
     };
 
     private Runnable mToggleRunnable = new Runnable() {
         @Override
         public void run() {
             toggleFlowBar();
         }
     };
 
     private void toggleFlowBar() {
         int delta = mFlowBar.getHeight();
         mHanler.removeCallbacks(mToggleRunnable);
 
         if (mFlowBar.getVisibility() == View.INVISIBLE
                 || mFlowBar.getVisibility() == View.GONE) {
             mFlowBar.setVisibility(View.VISIBLE);
             mFooter.setVisibility(View.VISIBLE);
             Animation anim = new TranslateAnimation(0, 0, -delta, 0);
             anim.setDuration(300);
             mFlowBar.startAnimation(anim);
             Animation animDown = new TranslateAnimation(0, 0, delta, 0);
             animDown.setDuration(300);
             mFooter.startAnimation(animDown);
             mHanler.postDelayed(mToggleRunnable, 5000);
         } else {
             mFlowBar.setVisibility(View.INVISIBLE);
             mFooter.setVisibility(View.INVISIBLE);
             Animation anim = new TranslateAnimation(0, 0, 0, -delta);
             anim.setDuration(300);
             mFlowBar.startAnimation(anim);
 
             Animation animDown = new TranslateAnimation(0, 0, 0, delta);
             animDown.setDuration(300);
             mFooter.startAnimation(animDown);
         }
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.largepic);
 
         mSlideshow = (SlideShow) findViewById(R.id.slideshow);
         mSlideShowMode = getIntent().getBooleanExtra("slideshow", false);
         if (mSlideShowMode) {
             mSlideshow.setVisibility(View.VISIBLE);
             mSlideshow.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     finish();
                 }
             });
         } else {
             mSlideshow.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     mSlideshow.setVisibility(View.GONE);
                 }
             });
         }
 
         mPager = (LargePicGallery) findViewById(R.id.photo_flow);
         mPager.setOffscreenPageLimit(1);
         mPager.setPageMargin(20);
         mPager.setHorizontalFadingEdgeEnabled(true);
         mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 mFooter.setText(mAdapter.getName(position));
             }
         });
         mFlowBar = (LinearLayout) findViewById(R.id.flow_bar);
         mFooter = (TextView) findViewById(R.id.footer_bar);
 
         Intent i = new Intent();
         i.setClass(this, Downloader.class);
         this.bindService(i, mServiceConnection, BIND_AUTO_CREATE);
        mInodeDes = getIntent().getStringExtra("inode");
         if (!mSlideShowMode) {
             mCurrentIndex  = getIntent().getIntExtra("index", 0);
         } else {
             mCurrentIndex = 0;
         }
         
         mHanler.postDelayed(mToggleRunnable, 5000);
     }
     
     @Override
     public void onResume() {
         super.onResume();
         mPager.setCurrentItem(mCurrentIndex);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         // getMenuInflater().inflate(R.menu.activity_image_show, menu);
         return true;
     }
 
     public void onBackClick(View view) {
         onBackPressed();
     }
 
     public void onSlideClick(View view) {
         mSlideshow.setDataSource(mRandomDataSource);
         mSlideshow.setVisibility(View.VISIBLE);
     }
 
     private ArrayList<ShowingNode> parseFilesPath(FInode inode) {
         ArrayList<ShowingNode> nodes = new ArrayList<ShowingNode>();
         JSONArray array = inode.getDirs();
         if (array != null) {
             int count  = array.length();
             for (int i = 0; i < count; ++i) {
                 JSONObject obj;
                 try {
                     obj = array.getJSONObject(i);
                     String path = obj.getString("path");
                     String name = obj.getString("name");
                     String content = obj.getString("content");
                     int start = path.lastIndexOf('/') + 1;
                     String nodePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liangdemo1/" + path.substring(start);
                     nodes.add(new ShowingNode(nodePath, name, content));
                 } catch (JSONException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }
         
         return nodes;
     }
 }
