 package com.rumblefish.rfsdkdemo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 
 import com.rumblefish.friendlymusic.FriendlyMusic;
 import com.rumblefish.friendlymusic.api.RFAPI;
 import com.rumblefish.friendlymusic.api.RFAPI.RFAPIEnv;
 
 public class FriendlyMusicDemoActivity extends Activity {
 	
 	Button m_btnGetStarted;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    requestWindowFeature(Window.FEATURE_NO_TITLE);
 	    
 	    setContentView(R.layout.friendlymusicdemo);
 	    
 	    //configures rumble environment
         RFAPI.rumbleWithEnvironment(RFAPIEnv.RFAPIEnvProduction, "PUBLIC_KEY", "PASSWORD");
         
         
         m_btnGetStarted = (Button)this.findViewById(R.id.btnGetStarted);
         m_btnGetStarted.setEnabled(false);
         
 		m_bRunning = true;
         startCountDownThread();
 	}
 	
 	
 	public void onBtnClick(View view)
 	{
 		if(view == m_btnGetStarted)
 		{
 			Intent intent = new Intent(this, FriendlyMusic.class);
 			startActivity(intent);
 		}
 		else
 		{
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_EMAIL, "info@friendlymusic.com");
			intent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion");
			intent.putExtra(Intent.EXTRA_TEXT, "");
 
			startActivity(Intent.createChooser(intent, "Send Email"));
 		}
 	}
     
 	
 	boolean m_bRunning = false;
 	@Override
 	protected void onStart()
 	{
 		super.onStart();
 	}
 	
 	@Override
 	protected void onDestroy()
 	{
 		super.onDestroy();
 		m_bRunning = false;
 	}
 	
 	public void startCountDownThread() {
 		
 		Runnable r = (new Runnable() {
 			@Override
 			public void run() {
 				while(m_bRunning)
 				{
 					if(RFAPI.getSingleTone().isInitialized())
 					{
 						FriendlyMusicDemoActivity.this.runOnUiThread(
 							new Runnable()
 							{
 								public void run()
 								{
 									m_btnGetStarted.setEnabled(true);
 								}
 							});
 						return;
 					}
 				}
 			}
 		});
 		
 		new Thread(r).start();
 	}
 	
 	
 }
