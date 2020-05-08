 package net.hath.hondroid;
 
 import net.hath.hondroid.database.DatabaseCreatorActivity;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		Button btn = (Button) findViewById(R.id.main_btn);
 		btn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				startActivity(new Intent(MainActivity.this, HeroScreenActivity.class));
 			}
 		});

 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()) {
 		case R.id.menu_update:
 			deleteAndUpdateDB();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void deleteAndUpdateDB() {
 		deleteDatabase("hondb");
 		startActivity(new Intent(this, DatabaseCreatorActivity.class));
 	}
 }
