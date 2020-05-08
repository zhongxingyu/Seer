 package com.xmedic.troll;
 
 import com.xmedic.troll.components.ScrollableImageView;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 
 public class TrucknrollAndroidActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         setContentView(R.layout.main);
         
         Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         
         
         Button button = (Button)findViewById(R.id.button1);
         final ScrollableImageView imageView = (ScrollableImageView)findViewById(R.id.map);
         imageView.setScreenSize(d.getWidth(), d.getHeight());
         button.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
				imageView.moveTo(-202, -462);
 			}
 		});
         
     }
 }
