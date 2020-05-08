 package edu.mines.alterego;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.util.Pair;
 
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 
import android.widget.ListView;
import android.widget.ArrayAdapter;

 public class MainActivity extends Activity {
 
    ArrayAdapter<Pair<Integer, String>> mGameDbAdapter;

 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
		//ArrayList<String> gameList = new ArrayList<String>();
		CharacterDBHelper db = new CharacterDBHelper(this);
		ArrayList< Pair<Integer, String> > gamePairList = db.getGames();
        /*
		for( Pair<Integer, String> game : gamePairList) {
			gameList.add(game.second);
		}
        */

		mGameDbAdapter = new ArrayAdapter<Pair<Integer, String>>( this, android.R.layout.simple_list_item_1, gamePairList);
		ListView gameListView = (ListView) findViewById(R.id.game_list_view);
		gameListView.setAdapter(mGameDbAdapter);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 }
