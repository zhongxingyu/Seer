 package eltoraz.pug.android;
 
 import eltoraz.pug.Game;
 import eltoraz.pug.Person;
 
 import java.util.*;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.*;
 
 // For debugging.
 //import android.util.Log;
 
 /**
  * This <code>Activity</code> displays a list of the Games the user has searched for.
  * For best results, store a list of Games in the <code>Intent</code> used to start this <code>Activity</code>.
  * @author Kevin Frame
  * @author Bill Jameson
  * @version 0.9
  * @reference http://developer.android.com/resources/tutorials/views/hello-listview.html
  */
 public class ListGameActivity extends ListActivity {
 	private Person user;
 	private ArrayList<Game> games;
 	private ArrayList<String> gameText;
 	
 	private ListView lv;
 	
 	static final int EDIT_REQUEST = 2; 
 
 	/**
 	 * The <code>onCreate</code> method is called when this <code>Activity</code> is first
 	 *  created. It captures the UI elements and sets default functionality.
 	 * @param savedInstanceState <code>Bundle</code>
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Note: the main activity should pass a list of games in the Intent along with the user.
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			user = (Person) extras.get("user");
 			games = (ArrayList<Game>) extras.get("games");
 		}
 		else {			// This should theoretically never be called
 			user = new Person();
 			games = new ArrayList<Game>();
 		}
 		
 		gameText = new ArrayList<String>();
 		for (Game g : games) {
 			gameText.add(listItem(g));
 		}
 		
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, gameText));
 		
 		lv = getListView();
 		lv.setTextFilterEnabled(true);
 		
 		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				// When clicked, join the Game and show a toast with the TextView text
 				String g = ((TextView) view).getText().toString();
 				if (user.getId() == games.get(gameText.indexOf(g)).getOwner().getId()) {
 					Intent intent = new Intent(getApplicationContext(), EditGameActivity.class);
 					intent.putExtra("user", user);
 					intent.putExtra("game", games.get(gameText.indexOf(g)));
 					startActivityForResult(intent, EDIT_REQUEST);
 				}
 				else {
					PugNetworkInterface.leaveGame(user.getId(), games.get(gameText.indexOf(g)).getId());
 					Toast.makeText(getApplicationContext(), "Joined Game!", Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Callback used to retrieve objects returned from Activities.
 	 * Here, update the listing of an edited game.
 	 * @param requestCode <code>int</code> Unique ID for the Activity returning the result.
 	 * @param resultCode <code>int</code> Status indicator for the termination of the Activity.
 	 * @param data <code>Intent</code> Container with the returned data.
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == Activity.RESULT_OK && requestCode == EDIT_REQUEST) {
 			if (data.hasExtra("game")) {
 				Bundle extras = data.getExtras();
 				Game oldGame = (Game) extras.get("old_game");
 				Game game = (Game) extras.get("game");
 				int pos = gameText.indexOf(listItem(oldGame));
 				games.remove(pos);
 				games.add(pos, game);
 				TextView tv = (TextView) lv.getChildAt(pos);
 				tv.setText(listItem(game));
 			}
 		}
 	}
 	
 	/**
 	 * Retrieve a human-readable representation of a Game to be displayed in a list.
 	 * @param g <code>Game</code> to be displayed
 	 * @return <code>String</code> representing the Game
 	 */
 	private String listItem(Game g) {
 		String game = "";
 		game += g.getDate().getTime().toLocaleString();
 		game += "\n";
 		game += g.getCreator().getName();
 		game += "\n";
 		game += g.getLocation().getAddress();
 		game += "\n";
 		game += g.getGameType().toString();
 		game += "\n";
 		game += g.getDescription();
 		
 		return game;
 	}
 }
