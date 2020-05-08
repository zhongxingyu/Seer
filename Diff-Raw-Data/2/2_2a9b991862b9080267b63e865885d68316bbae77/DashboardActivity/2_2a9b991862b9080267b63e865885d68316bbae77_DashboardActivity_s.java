 package edu.gatech.oad.fullhouse.findmystuff.view;
 
import android.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import edu.gatech.oad.fullhouse.findmystuff.model.Session;
 
 public class DashboardActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dashboard);
 		if(Session.instance().getLoggedInUser().isAdmin()){
 			((TextView)findViewById(R.id.lookupUserButton)).setVisibility(View.VISIBLE);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_dashboard, menu);
 		return true;
 	}
 	
 	public void viewItems(View v) {
 		Intent intent = new Intent(this, ViewItemsActivity.class);
 		startActivity(intent);
 	}
 	
 	public void addItem(View v) {
 		Intent intent = new Intent(this, AddItemActivity.class);
 		startActivity(intent);
 	}
 	
 	public void lookupUser(View v) {
 		Intent intent = new Intent(this, LookupUserActivity.class);
 		startActivity(intent);
 	}
 	
 	public void newIncident(View v) {
 		Intent intent = new Intent(this, AddIncidentActivity.class);
 		startActivity(intent);
 	}
 	
 	public void searchView(View v) {
 		Intent intent = new Intent(this, SearchViewActivity.class);
 		startActivity(intent);
 	}
 
 }
