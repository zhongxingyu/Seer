 package com.teamholder.shopping;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class DisplayLists extends Activity implements OnClickListener {
 	
 	static final String LIST_URL = "com.teamholder.shopping.ListUrl";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         installButtonListeners();
     }
     
     private void installButtonListeners() {
     	findViewById(R.id.clean_button).setOnClickListener(this);
     	findViewById(R.id.dirty_button).setOnClickListener(this);
     	findViewById(R.id.full_button).setOnClickListener(this);
     }
 
 	@Override
 	public void onClick(View v) {
 		Intent i = new Intent(this, ShowList.class);
 		switch (v.getId()) {
 		case R.id.dirty_button:
			i.putExtra(LIST_URL, "file:///android_asset/html/dirty_dozen.html");
			break;
		case R.id.clean_button:
			i.putExtra(LIST_URL, "file:///android_asset/html/clean_15.html");
			break;
		case R.id.full_button:
			i.putExtra(LIST_URL, "file:///android_asset/html/full_list.html");
 			break;
 		default:
 			return;
 		}
 		startActivity(i);
 	}
 }
