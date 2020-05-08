 package com.example.intentservice04;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	public static String TAG = "com.example.intentservice04";
 	private TextView tv_msg;
 	private Intent intent;
 	
 	public Handler handler = new Handler(){
 		
 		@Override
 		public void handleMessage( Message msg ){
 			Log.i(TAG, "MainActivity::handleMessage()");
 			// get data from msg
 			
 			String result = msg.getData().getString( "result" );
 			tv_msg.setText( result );
			Log.d(TAG, "XXX " + result );
 			super.handleMessage( msg );
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.i( TAG, "MainActivity::onCreate()");
 		setContentView(R.layout.activity_main);
 		
 		// init
 		tv_msg = (TextView) findViewById( R.id.tv_msg );
 
 		intent = new Intent( this, HiddenService.class );
 		intent.putExtra( "handler" , new Messenger( this.handler ));
 		this.startService( intent );
 	}
 }
