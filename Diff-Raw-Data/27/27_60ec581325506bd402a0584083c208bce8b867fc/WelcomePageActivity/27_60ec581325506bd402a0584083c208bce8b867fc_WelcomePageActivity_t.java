 package com.underdusken.kulturekalendar.ui;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import com.underdusken.kulturekalendar.R;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pavelarteev
  * Date: 9/19/12
  * Time: 7:02 PM
  * To change this template use File | Settings | File Templates.
  */
 public class WelcomePageActivity extends Activity {
 
     private boolean touchClick = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.welcome_page);
 
         final View layout = findViewById(R.id.welcome_page_view);
 
         final Animation a = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
         final Animation stayAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_stay);
         a.reset();
 
         a.setAnimationListener(new Animation.AnimationListener() {
             @Override
             public void onAnimationStart(Animation animation) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void onAnimationEnd(Animation animation) {
 
                 stayAnim.reset();
                 layout.clearAnimation();
                 layout.startAnimation(stayAnim);
                 stayAnim.setAnimationListener(new Animation.AnimationListener() {
                     @Override
                     public void onAnimationStart(Animation animation) {
                         //To change body of implemented methods use File | Settings | File Templates.
                     }
 
                     @Override
                     public void onAnimationEnd(Animation animation) {
                         if (!touchClick)
                            finishWelcomePage();
                     }
 
                     @Override
                     public void onAnimationRepeat(Animation animation) {
                         //To change body of implemented methods use File | Settings | File Templates.
                     }
                 });
             }
 
             @Override
             public void onAnimationRepeat(Animation animation) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
 
         layout.clearAnimation();
         layout.startAnimation(a);
 
         layout.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View view, MotionEvent motionEvent) {
                finishWelcomePage();
                 layout.clearAnimation();
                 return true;
             }
         });
 
 
     }
 
    public void finishWelcomePage() {
         finish();
     }
 }
