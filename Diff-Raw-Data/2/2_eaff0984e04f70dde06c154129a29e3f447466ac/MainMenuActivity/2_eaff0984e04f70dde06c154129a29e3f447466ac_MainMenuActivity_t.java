 /**
  * @file ui.MainMenuActivity.java
  *
  * Riderアプリケーションメインメニュー Activity
  *
  * @version 0.0.1
  *
  * @since 2012/02/07
  * @date  2012/02/07
  */
 
 package com.android.rider.ui;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.widget.FrameLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 import com.android.rider.R;
 
 public class MainMenuActivity extends Activity implements OnSeekBarChangeListener, AnimationListener{
 
     /** ログ出力用TAG. */
     private static final String TAG = "MainMenuActivity";
 
     /** アニメーション用レイアウト. */
     private FrameLayout mAnimLayout = null;
 
     /** RiderBall用シークバー. */
     private SeekBar mRiderSeekBar = null;
 
     /** アニメーション. */
     private Animation mAnimation = null;
     /** アニメーション表示時間. */
    private static final int ANIMATION_TIME = 500;
 
     /** Activity 起動.
      *
      * リソースからメインメニュー画面を取得し、表示する。\n
      */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         Log.i(TAG, "onCreate(Bundle savedInstanceState) start");
         super.onCreate(savedInstanceState);
 
         /** タイトル非表示 */
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         setContentView(R.layout.main_menu);
         Log.i(TAG, "onCreate(Bundle savedInstanceState) finish");
     }
 
     /* Activity 再開.
      *
      * リスナー設定、及びアニメーション設定を行う。
      */
     @Override
     protected void onResume() {
         Log.i(TAG, "onResume() start");
 
         /** アニメーション用レイアウトを非表示 */
         if(mAnimLayout == null) {
             mAnimLayout = (FrameLayout) findViewById(R.id.frameLayout2);
         }
         mAnimLayout.setVisibility(View.INVISIBLE);
 
         /** シークバーにリスナーを設定 */
         if(mRiderSeekBar == null) {
             mRiderSeekBar = (SeekBar) findViewById(R.id.seekbar);
             mRiderSeekBar.setOnSeekBarChangeListener(this);
         }
         mRiderSeekBar.setProgress(0);
 
         /** アニメーション設定 */
         if(mAnimation == null) {
             mAnimation = new AlphaAnimation(0.0f, 1.0f);
             mAnimation.setDuration(ANIMATION_TIME);
             mAnimation.setAnimationListener(this);
         }
         super.onResume();
         Log.i(TAG, "onResume() finish");
     }
 
     /** シークバー変化コールバック.
      *
      * シークバー状態が変化した際にコールされる\n
      *
      * @param seekBar   シークバー
      * @param progress  プログレス
      * @param fromUser  ユーザー操作
      *
      */
     @Override
     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         if(progress >= 95) {
             mAnimLayout.setVisibility(View.VISIBLE);
             mAnimLayout.startAnimation(mAnimation);
         }
 
     }
 
     /** アニメーション終了.
      *
      * アニメーション終了時にコールされる。\n
      * RiderBallActivity の起動を行う。\n
      */
     @Override
     public void onAnimationEnd(Animation arg0) {
         Intent intent = new Intent();
         intent.setClass(this, RiderBallActivity.class);
         startActivity(intent);
     }
 
     // 未使用
     @Override
     public void onStartTrackingTouch(SeekBar arg0) {}
     // 未使用
     @Override
     public void onStopTrackingTouch(SeekBar arg0) {}
     // 未使用
     @Override
     public void onAnimationRepeat(Animation arg0) {}
     // 未使用
     @Override
     public void onAnimationStart(Animation arg0) {}
 
 
 }
