 package com.github.marco9999.directtalk;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class MessageWindow extends Activity
 {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_message_window);
 
 		Intent menuintent = getIntent();
 		String hoststring = null;
 		String portstring = null;
 
 		Bundle ConnectInfo = menuintent.getExtras();
 		if (ConnectInfo != null)
 		{
 			hoststring = ConnectInfo
 					.getString("com.github.marco9999.hoststring");
 			portstring = ConnectInfo
 					.getString("com.github.marco9999.portstring");
 		}
 		else
 		{
 			Log.e("DirectTalk", "Error: intent extra's empty!");
 		}
 
		if (hoststring == null && portstring == null)
 		{
 			Log.e("DirectTalk", "Error: Host or port empty!");
 		}
 
 		TextView host = (TextView) findViewById(R.id.message_window_host);
 		TextView port = (TextView) findViewById(R.id.message_window_port);
 		host.setText(hoststring);
 		port.setText(portstring);
 
 		MessageHandlerWorker connection = new MessageHandlerWorker(hoststring, portstring);
 		connection.start();
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.message_window, menu);
 		return true;
 	}
 }
