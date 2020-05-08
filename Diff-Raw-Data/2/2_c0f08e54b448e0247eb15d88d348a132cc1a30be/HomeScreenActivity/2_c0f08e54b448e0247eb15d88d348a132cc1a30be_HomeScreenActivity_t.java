 package tournament.brackets;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 public class HomeScreenActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home_screen);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.home_screen, menu);
 		return true;
 	}
 
     public void quickMatch(View view) {
         Intent intent = new Intent(this, QuickMatchActivity.class);
         startActivity(intent);
     }
 	
	public void viewMatches(View view) {
 		Intent intent = new Intent(this, ItemListActivity.class);
 		startActivity(intent);
 	}
 
 }
