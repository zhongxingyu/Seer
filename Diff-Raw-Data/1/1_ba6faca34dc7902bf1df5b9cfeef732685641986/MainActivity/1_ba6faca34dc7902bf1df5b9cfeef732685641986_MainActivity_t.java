 package br.com.thiagopagonha.psnapi;
 
 import android.app.ActivityGroup;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.widget.TabHost;
 
 public class MainActivity extends ActivityGroup {
 
 	static TabHost tabHost;
 	static int tab = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		 setContentView(R.layout.activity_tabs);
 
 		Resources res = getResources();
 		tabHost = (TabHost)findViewById(R.id.tabhost);
 		tabHost.setup(this.getLocalActivityManager());
 		TabHost.TabSpec spec;
 		Intent intent;
 
 		// Adiciona Tab #1
 		intent = new Intent().setClass(this, FriendActivity.class);
 		spec = tabHost.newTabSpec("0").setIndicator(getString(R.string.friends), res.getDrawable(R.drawable.friends)).setContent(intent);
 		tabHost.addTab(spec);
 
 		// Adiciona Tab #2
 		intent = new Intent(this, MessageActivity.class);
 		spec = tabHost.newTabSpec("1").setIndicator(getString(R.string.history), res.getDrawable(R.drawable.clock)).setContent(intent);
 		tabHost.addTab(spec);
 
		tabHost.setCurrentTab(1);
 		tabHost.setCurrentTab(0);
 
 	}
 
 }
