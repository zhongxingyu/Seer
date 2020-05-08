 package io.debugged;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import io.debugged.Debugged.DebuggedCatcher;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	Handler handler = new Handler();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		Debugged.init(this, "26a0106e-fc48-40a3-b595-fdefc30f687f");
 
 		Debugged.catchForceClose(new DebuggedCatcher() {
 
 			@Override
 			public void execute(String url) {
 				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 				startActivity(browserIntent);
 			}
 
 		});
 		
 		handler.postDelayed(new Runnable() {
 
 			public void run() {
				try {
					Integer.parseInt("Startup Weekend Milwaukee");        	
				} catch (Exception e) {
					CustomData customData = new CustomData();
					customData.put("note", "Demo-ing custom (non force closing) reports");
					
					Debugged.report(e, customData);
				}
 			}
 			
 		}, 5000);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 }
