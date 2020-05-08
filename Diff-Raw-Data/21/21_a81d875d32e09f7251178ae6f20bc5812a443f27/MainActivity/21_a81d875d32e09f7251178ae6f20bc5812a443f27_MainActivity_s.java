 package com.howfun.android.lover;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnTouchListener;
 import android.widget.Toast;
 
 import com.howfun.android.lover.view.ChocolateMask;
 import com.howfun.android.lover.view.DiamondMask;
 import com.howfun.android.lover.view.HeartMask;
 import com.howfun.android.lover.view.Mask;
 import com.howfun.android.lover.view.RoseMask;
 
 public class MainActivity extends Activity {
 
    ScreenView mScreenView = null;
 
    private Context mContext = null;
    private ScreenManger mScreenManager = null;
    private Sound mSound = null;
 
    Intent mIntentService = null;
    Intent mIntentReceiver = null;
 
    private int mMaskId = R.id.heart;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       requestWindowFeature(Window.FEATURE_NO_TITLE);
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
       init();
       findViews();
       setupListeners();
       mScreenManager.setScreenView(mScreenView);
       Utils.showMessageDlg(this, R.string.msg_welcome);
    }
 
    private void init() {
       mContext = this;
       mSound = new Sound(mContext);
       mScreenManager = new ScreenManger();
       mIntentService = new Intent("com.howfun.android.MusicService");
       mIntentReceiver = new Intent("com.howfun.android.MusicReceiver");
 
    }
 
    private void findViews() {
       mScreenView = (ScreenView) findViewById(R.id.screen_view);
    }
 
    private void setupListeners() {
       if (mScreenView != null) {
          mScreenView.setOnTouchListener(new OnTouchListener() {
 
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                   int x = (int) event.getX();
                   int y = (int) event.getY();
                   addMask(x, y);
                }
                return false;
             }
          });
       }
    }
 
    private void addMask(int x, int y) {
       Mask mask = null;
       switch (mMaskId) {
       case R.id.heart:
          mask = new HeartMask(mContext, x, y);
          mScreenManager.addMask(mask);
         mSound.play(R.raw.heart_beats, false);
          break;
       case R.id.rose:
          mask = new RoseMask(mContext, x, y);
          mScreenManager.addMask(mask);
          break;
       case R.id.chocolate:
          mask = new ChocolateMask(mContext, x, y);
          mScreenManager.addMask(mask);
          break;
       case R.id.diamond:
          mask = new DiamondMask(mContext, x, y);
          mScreenManager.addMask(mask);
          break;
       }
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.options, menu);
       return super.onCreateOptionsMenu(menu);
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       super.onOptionsItemSelected(item);
       switch (item.getItemId()) {
       case R.id.heart:
          mScreenManager.clear();
          mMaskId = R.id.heart;
          break;
       case R.id.chocolate:
          mScreenManager.clear();
          mMaskId = R.id.chocolate;
          break;
       case R.id.rose:
          mScreenManager.clear();
          mMaskId = R.id.rose;
          break;
       case R.id.diamond:
          mScreenManager.clear();
          mMaskId = R.id.diamond;
          break;
       }
       return true;
    }
 
    private void showExitPrompt() {
       Toast.makeText(this, R.string.msg_stop, Toast.LENGTH_SHORT).show();
    }
 
    private void showResumePrompt() {
       Toast.makeText(this, R.string.msg_start, Toast.LENGTH_SHORT).show();
    }
 
    @Override
    protected void onResume() {
       super.onResume();
 
       showResumePrompt();
       sendBroadcast(mIntentReceiver);
 //      startService(mIntentService);
    }
 
    @Override
    protected void onPause() {
       super.onPause();
 
       showExitPrompt();
       stopService(mIntentService);
    }
 }
