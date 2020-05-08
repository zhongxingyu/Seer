 /**
  * 
  */
 package com.xmedic.troll;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.xmedic.troll.service.TrollService;
 import com.xmedic.troll.service.db.TrollServiceSqlLite;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.TranslateAnimation;
 
 /**
  * @author vincentas
  *
  */
 public class SplashScreenActivity extends Activity {
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
         setContentView(R.layout.splashscreen);
                 
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
 			
 			@Override
 			public void run() {
 				startActivity(new Intent(SplashScreenActivity.this, HomeScreenActiity.class));				
 			}
 		}, 3000);
         
        findViewById(R.id.yo).setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				timer.cancel();
				startActivity(new Intent(SplashScreenActivity.this, HomeScreenActiity.class));
			}
		});
                
         Display display = getWindowManager().getDefaultDisplay(); 
         int width = display.getWidth();
         int height = display.getHeight();
         
         View truckImage = findViewById(R.id.movingtruck);
         TranslateAnimation animation = new TranslateAnimation(0, -width, 0, 0);
         animation.setFillAfter(true);
         animation.setDuration(3000);
         animation.setInterpolator(new AccelerateInterpolator());
         truckImage.startAnimation(animation);
 
         new TrollServiceSqlLite(this).getCity("1");
     }
 }
