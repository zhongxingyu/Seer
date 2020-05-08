<<<<<<< HEAD
 package ca.skule.froshapplication;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		getActionBar().setDisplayShowTitleEnabled(false);
 		return true;
 	}
 
 	public void onMenuClick (MenuItem Item){
 		String id = Item.getTitle().toString();
 		if (id.equalsIgnoreCase("Schedule")){
 			Intent intent = new Intent (this, DayListActivity.class);
 			startActivity (intent);
 		}
 		else if (id.equalsIgnoreCase("Map")){
 			Intent intent = new Intent (this, MapActivity.class);
 			startActivity(intent);
 		}
 	}
}
 
=======
 package ca.skule.froshapplication;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		getActionBar().setDisplayShowTitleEnabled(false);
 		return true;
 	}
 
 	public void onMenuClick (MenuItem Item){
 		String id = Item.getTitle().toString();
 		if (id.equalsIgnoreCase("Schedule")){
			// Point to Schedule
 		}
 		else if (id.equalsIgnoreCase("Map")){
 			Intent intent = new Intent (this, MapActivity.class);
 			startActivity(intent);
 		}
 		else if (id.equalsIgnoreCase("list")){
 			// Point to Building List Activity
 			Intent intent = new Intent (this,ListsActivity.class);
 			startActivity(intent);
 		}
 	}
 }
 
>>>>>>> 38eaaa7d467ea52c65dcd28627ccc3a62c86f305
