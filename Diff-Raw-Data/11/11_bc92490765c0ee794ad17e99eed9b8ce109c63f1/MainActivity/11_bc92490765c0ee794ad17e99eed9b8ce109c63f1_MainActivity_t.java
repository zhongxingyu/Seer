 package com.example.djhero;
 
 import android.os.Bundle;
 import android.app.Activity;
import android.util.Log;
 import android.view.Menu;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		songList songlist = new songList("Shake it:Moe:20|Why can't you love me:Gursimran:40");
		songlist.getListNames();
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
