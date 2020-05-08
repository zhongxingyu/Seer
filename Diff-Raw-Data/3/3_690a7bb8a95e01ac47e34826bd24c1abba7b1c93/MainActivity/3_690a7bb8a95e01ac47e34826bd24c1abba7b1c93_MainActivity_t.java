 package fi.dy.esav.GrafiikkaTest;
 
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Layout;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.AlphaAnimation;
 
 public class MainActivity extends Activity {
 
 	BallView bw;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 		View l = findViewById(R.id.main_layout);
 		
 		AlphaAnimation anim = new AlphaAnimation(0, 0);
 		anim.setDuration(0);
 		l.startAnimation(anim);
 
 		bw = (BallView) findViewById(R.id.ballview);
 		bw.startDraw();
 		
 		anim = new AlphaAnimation(0f, 1f);
 		anim.setDuration(700);
 		l.startAnimation(anim);
 	}
 	
 	@Override
 	protected void onStop() {
 		bw.pauseDraw();
        super.onStop();;
 	}
 	
 	@Override
 	protected void onDestroy() {
 		bw.stopDraw();
        super.onDestroy();
 	}
 }
