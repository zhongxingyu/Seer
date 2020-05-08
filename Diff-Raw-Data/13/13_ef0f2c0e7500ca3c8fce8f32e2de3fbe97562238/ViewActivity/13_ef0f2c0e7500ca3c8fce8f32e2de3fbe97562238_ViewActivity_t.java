 package com.kevin.physicsemulations;
 
 import android.app.*;
 import android.os.*;
 import android.view.*;
 import android.widget.*;
 
 public class ViewActivity extends Activity
 {
	
 	@Override
 	public void onCreate(Bundle sis){
 		super.onCreate(sis);
 		ActionBar ab=getActionBar();
 		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		SpinnerAdapter sa=ArrayAdapter.createFromResource(this,R.array.categories,R.layout.spinner_text);
 		ab.setListNavigationCallbacks(sa, new ActionBar.OnNavigationListener(){
 			public boolean onNavigationItemSelected(int p1, long p2){
 				Toast.makeText(ViewActivity.this,""+p1,Toast.LENGTH_SHORT).show();
 				return false;
 			}			
 		});
 		ab.setTitle("");
 		ab.setSelectedNavigationItem(getIntent().getIntExtra("pos",0));
		ab.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present
 		getMenuInflater().inflate(R.menu.main_list, menu);
 		return true;
 	}
 	
 }
