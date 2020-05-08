 package com.way.xmpp;
 
 import com.way.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class XMPPSetup extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		Intent intent = new Intent();
 		intent.setAction("com.way.xmpp.XMPP_SERVICE");
		this.startActivity(intent);
 	}
 
 	
 }
