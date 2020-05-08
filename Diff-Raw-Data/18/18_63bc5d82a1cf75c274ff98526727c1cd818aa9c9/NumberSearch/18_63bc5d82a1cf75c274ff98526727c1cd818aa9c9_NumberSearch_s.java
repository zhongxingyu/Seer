 package com.example.strangers.controler;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.example.strangers.R;
 import com.example.strangers.model.User;
 
 public class NumberSearch extends Activity {
 
 	private User currentUser;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_number_search);
 		
 		Bundle bundle = getIntent().getBundleExtra("currentUserBundle");
 		this.currentUser = bundle.getParcelable("com.example.strangers.model.User");
 		
 		Log.v("user", currentUser.getLogin());
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.number_search_menu_layout, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		
 			case R.id.unregister:
 				SharedPreferences stockPreferences = getSharedPreferences("strangers", Activity.MODE_PRIVATE);
 				Editor edit = stockPreferences.edit();
 				edit.remove("registeredUser");
 				edit.apply();
 				
 				Intent intent = new Intent(this, Login.class);
 		    	startActivity(intent);
 		    	return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
