 package se.chalmers.chessfeud;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.chessfeud.model.ChessModel;
 import se.chalmers.chessfeud.utils.DbHandler;
 import se.chalmers.chessfeud.utils.Game;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * This is the activity that handles the overview of games. It binds the
  * menu_main.xml file and sets an adapter to its list objects. From here you can
  * reach the settings and statistics as well as a certain game in the list.
  * 
  * @author Henrik Alburg
  * 
  *         Copyright (c) Henrik Alburg 2012
  * 
  */
 public class MainActivity extends Activity implements OnClickListener {
 	private ImageView iLogo;
 	private ListView startedGames;
 	private DbHandler dbh;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.menu_main);
 
 		Button bStats, bSettings, bNewGame;
 		
 		dbh = DbHandler.getInstance();
 
 		bStats = (Button) findViewById(R.id.button_stats);
 		bSettings = (Button) findViewById(R.id.button_settings);
 		bNewGame = (Button) findViewById(R.id.button_newgame);
 
 		iLogo = (ImageView) findViewById(R.id.imageView1);
 		iLogo.setOnClickListener(this);
 
 		startedGames = (ListView) findViewById(R.id.list_ongoingGames);
 
 		bStats.setOnClickListener(this);
 		bSettings.setOnClickListener(this);
 		bNewGame.setOnClickListener(this);
 
 		startedGames = (ListView) findViewById(R.id.list_ongoingGames);
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		new Thread() {
 			public void run() {
 				final List<String> games = dbh.getGames();
 				
 				MainActivity.this.runOnUiThread(new Runnable() {
 					public void run() {
 						startedGames.setAdapter(new GameListAdapter(
 								MainActivity.this, R.id.list_ongoingGames,
 								games));
 					}
 				});
 
 			}
 		}.start();
 	}
 
	@Override
	public void onBackPressed() {
		setResult(1);
		finish();
	}
	
 	/* Will launch a new activity based on what is clicked. */
 	public void onClick(View v) {
 		int id = v.getId();
 
 		switch (id) {
 		case R.id.button_stats:
 			startActivity(new Intent(this, StatsActivity.class));
 			break;
 		case R.id.button_settings:
 			startActivity(new Intent(this, SettingsActivity.class));
 			break;
 		case R.id.imageView1:
 			startActivity(new Intent(this, PlayActivity.class));
 			break;
 		case R.id.button_newgame:
 			showPrompt();
 			break;
 		default:
 			Log.e("Mainactivity", "Button not binded");
 		}
 	}
 
 	/* This is temporary until the view is finished. */
 	private void showPrompt() {
 		AlertDialog.Builder prompt = new AlertDialog.Builder(this);
 		prompt.setTitle("New Game");
 		prompt.setMessage("Type in the username of the person you want to challange:");
 		final EditText input = new EditText(this);
 		prompt.setView(input);
 		prompt.setPositiveButton("Done", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				new Thread() {
 					public void run() {
 						DbHandler.getInstance().newGame(
 								input.getText().toString(),
 								new ChessModel(0).exportModel());
 					}
 				}.start();
 			}
 		});
 		prompt.show();
 	}
 
 	/*
 	 * An inner class that works as a adapter between a list och games to a list
 	 * of list_item views.
 	 */
 	private class GameListAdapter extends ArrayAdapter<String> {
 		private Context context;
 		private List<Game> gamesList;
 		private List<String> stringList;
 
 		/**
 		 * Creates an adapter between the Strings (GameInfos) object and list
 		 * items.
 		 * 
 		 * @param context
 		 *            the context of the application
 		 * @param resId
 		 *            the resource id for the list
 		 * @param l
 		 *            the list of string to be converted to GameInfos
 		 */
 		public GameListAdapter(Context context, int resId, List<String> l) {
 			super(context, resId, l);
 			this.context = context;
 			gamesList = new ArrayList<Game>();
 			stringList = new ArrayList<String>();
 			if(l != null){
 				for (int i = 0; i < l.size(); i++) {
 					gamesList.add(new Game(l.get(i), i));
 					stringList.add(l.get(i));
 				}
 			}
 		}
 
 		@Override
 		public View getView(final int position, View convertView,
 				ViewGroup parent) {
 			final Game game = gamesList.get(position);
 			final String gameString = stringList.get(position);
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View vRow = inflater.inflate(R.layout.menu_listitem, parent, false);
 
 			/* Bind all the views */
 			TextView tTurn = (TextView) vRow.findViewById(R.id.player_turn);
 			TextView blackName = (TextView) vRow
 					.findViewById(R.id.player_black);
 			TextView whiteName = (TextView) vRow
 					.findViewById(R.id.player_white);
 			TextView tNbrOfTurns = (TextView) vRow.findViewById(R.id.nbr_turns);
 
 			/* Set the binded views */
 			tTurn.setText(game.getCurrentPlayer() + "'s turn");
 			blackName.setText(game.getBlackPlayer());
 			whiteName.setText(game.getWhitePlayer());
 			tNbrOfTurns.setText("" + game.getTurns());
 
 			/* Start a PlayActivity with the right GameInfo */
 			vRow.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					Intent i = new Intent(context, PlayActivity.class);
 					i.putExtra("GameString", gameString);
 					i.putExtra("Position", position);
 					startActivity(i);
 				}
 			});
 			return vRow;
 		}
 
 	}
 
 }
