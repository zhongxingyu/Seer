 package org.serviterobotics.friarbots;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class BotsSplashActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
         Animate();
     }
     private void Animate() {
     	TextView FRC = (TextView) findViewById(R.id.TextView_SplashFRC);
     	Animation fade1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
     	FRC.startAnimation(fade1);
     	ImageView logo = (ImageView) findViewById(R.id.ImageView_SplashLogo);
     	Animation fade2 = AnimationUtils.loadAnimation(this, R.anim.fade_in2);
     	TextView teamNum = (TextView) findViewById(R.id.TextView_TeamNum);
     	TextView teamName = (TextView) findViewById(R.id.TextView_TeamName);
     	logo.startAnimation(fade2);
     	teamNum.startAnimation(fade2);
     	teamName.startAnimation(fade2);
     	fade2.setAnimationListener(new AnimationListener() {
         	public void onAnimationEnd(Animation animation) {
         		try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
         		startActivity(new Intent(BotsSplashActivity.this, FriarBotsActivity.class));
         		BotsSplashActivity.this.finish();
         	}
         	public void onAnimationRepeat(Animation animation) {
         	}
         	public void onAnimationStart(Animation animation) {
         	}
         });
     }
     protected void onPause() {
     	super.onPause();
     	TextView logo1 = (TextView) findViewById(R.id.TextView_SplashFRC);
     	logo1.clearAnimation();
     	TextView logo2 = (TextView) findViewById(R.id.TextView_TeamNum);
     	logo2.clearAnimation();
     	ImageView logo3 = (ImageView) findViewById(R.id.ImageView_SplashLogo);
     	logo3.clearAnimation();
     	TextView logo4 = (TextView) findViewById(R.id.TextView_TeamName);
     	logo4.clearAnimation();
     }
     protected void onResume() {
     	super.onResume();
     	Animate();
     }
 }
