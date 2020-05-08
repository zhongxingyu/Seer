 package com.example.komunikator;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class ConversationActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_conversation);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		Connection c= new Connection();
 		c.sendMessage("marcin", "messageJson");
 		TextView t = (TextView) findViewById(R.id.txt);
		//t.append(c.getNewMessages("marcin"));
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.conversation, menu);
 		return true;
 	}
 
 }
