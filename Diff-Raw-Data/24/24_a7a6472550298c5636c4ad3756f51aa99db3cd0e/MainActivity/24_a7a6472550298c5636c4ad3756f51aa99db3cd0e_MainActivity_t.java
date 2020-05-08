 package se.chalmers.dryleafsoftware.androidrally;
 
 import java.io.FileOutputStream;
 import java.io.FilterOutputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * This is the main activity, i.g. it will be the activity started when opening
  * the app on the phone.
  * 
  * @author
  * 
  */
 public class MainActivity extends Activity {
	
	private ListView gameListView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
//		Client client = new Client();
		
		gameListView = (ListView) findViewById(R.id.currentGames);
//		String[] games = client.getSavedGames();
		String[] games = {"game1", "game2", "game3"};
		ListAdapter gamesList = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, games);
		gameListView.setAdapter(gamesList);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_refresh:
 			// TODO: refresh()
 			showToaster("Refreshing");
 			return true;
 		case R.id.action_help:
 			// Todo: help someone
 			showToaster("Helping");
 			return true;
 		default:
 			return true;
 		}
 	}
 	
 	/**
 	 * Starts the chosen game
 	 * 
 	 * @param view
 	 */
 	public void startChosenGame(View view) {
		
 //		lv.get the chosen one
 //		configure the correct game to start
 //		Intent i = new Intent(getApplicationContext(), GameActivity.class);
//		i.putExtra("GAME_ID", gameListView.getClicked()))
 //		startActivity(i);
 	}
 
 	public void showToaster(CharSequence message) {
 		Context context = getApplicationContext();
 		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
 		toast.show();
 	}
 	
 
 	public void startConfiguration(View view) {
 		Intent i = new Intent(getApplicationContext(), GameConfigurationActivity.class);
 		startActivity(i);
 	}
 
 }
