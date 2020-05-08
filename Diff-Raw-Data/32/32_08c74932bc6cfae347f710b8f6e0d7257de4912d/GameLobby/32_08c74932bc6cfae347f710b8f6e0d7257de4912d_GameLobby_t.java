 package de.cbrn35.apmemory.lobby;
 
 import java.util.ArrayList;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.client.methods.HttpGet;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import de.cbrn35.apmemory.C;
 import de.cbrn35.apmemory.Game;
 import de.cbrn35.apmemory.HttpAsyncTask;
 import de.cbrn35.apmemory.Player;
 import de.cbrn35.apmemory.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources.NotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class GameLobby extends Activity {
 	Game game = null;
 	Player player = null;
 	ArrayList<Player> players = new ArrayList<Player>();
 	private Handler refreshHandler = new Handler();
 	private boolean isInLoop = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gamelobby);
 		
 		Log.i(C.LOGTAG, getIntent().getExtras().toString());
 		
 		if(getIntent().hasExtra("data") && getIntent().hasExtra("gameid")) {
 			// private game
 			try {
 				JSONObject data = new JSONObject(getIntent().getExtras().getString("data"));
 				if(data.has("data") && data.getString("data").equals("check_password")) {
 					LayoutInflater factory = LayoutInflater.from(this);
 				    final View textEntryView = factory.inflate(R.layout.dialog_join_private, null);
 					AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
 				    alert.setTitle(getResources().getString(R.string.dialog_join_private_title));  
 					alert.setMessage(getResources().getString(R.string.dialog_join_private_msg));                
 					alert.setView(textEntryView);
 
 					alert.setPositiveButton(getResources().getString(R.string.dialog_ok_txt), new DialogInterface.OnClickListener() {  
 						public void onClick(DialogInterface dialog, int whichButton) {
 					        EditText ed_password = (EditText) textEntryView.findViewById(R.id.ed_join_private_password);
 					        String password = ed_password.getText().toString();
 					        int gameId;
 							try {
 								gameId = new JSONObject(getIntent().getExtras().getString("gameid")).getInt("data");
 								Player p = new PlayerSQLiteDAO(GameLobby.this).getPlayer();
 						        HttpGet joinPrivateGet = new HttpGet(C.URL+"?action=join_game&user="+p.username+"&gameid="+gameId+"&password="+password);
 						    	HttpAsyncTask joinPrivateTask = new HttpAsyncTask(joinPrivateGet, GameLobby.this, null, false);
 						    	joinPrivateTask.execute();
 						    	JSONObject response = joinPrivateTask.get();
 						    	if(response.has("data")) {
 						    		game = new Game(response.getJSONObject("data"));
 						    		player = new PlayerSQLiteDAO(GameLobby.this).getPlayer();
 						    		initGameLobby();
 								} else {
 									finish();
 								}
 							} catch (JSONException e) {
 								finish();
 								e.printStackTrace();
 							} catch (InterruptedException e) {
 								finish();
 								e.printStackTrace();
 							} catch (ExecutionException e) {
 								finish();
 								e.printStackTrace();
 							}
 					        return;                  
 					    }  
 					});  
 
 					alert.setNegativeButton(getResources().getString(R.string.dialog_cancel_txt), new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							finish();
 					        return;   
 					    }
 					});
 					alert.show();
 					return;
 				}
 			} catch (JSONException e1) {
 				e1.printStackTrace();
 			}
 		}
 		
 		if(getIntent().hasExtra("data") || getIntent().hasExtra("gameid")) {
 			player = new PlayerSQLiteDAO(this).getPlayer();
 			
 			try {
 				int gameid = 0;
 				if(getIntent().hasExtra("gameid")) {
 					gameid = new JSONObject(getIntent().getExtras().getString("gameid")).getInt("data");
 					HttpGet joinGet = new HttpGet(C.URL + "?action=join_game&user=" + Uri.encode(player.username) + "&gameid=" + gameid);
 					HttpAsyncTask joinTask = new HttpAsyncTask(joinGet, this, null, true);
 					joinTask.execute();
 					JSONObject result = joinTask.get();
 					game = new Game(result.getJSONObject("data"));
 				} else if(getIntent().hasExtra("data")) {
 					gameid = new JSONObject(getIntent().getExtras().getString("data")).getInt("data");
 					HttpGet getGame = new HttpGet(C.URL+"?action=get_game&gameid="+gameid);
 					HttpAsyncTask getTask = new HttpAsyncTask(getGame, this, null, true);
 					getTask.execute();
 					JSONObject result = getTask.get();
 					game = new Game(result.getJSONObject("data"));
 				}
 				initGameLobby();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		} else {
 			Toast.makeText(this, getResources().getString(R.string.err_no_game), Toast.LENGTH_LONG).show();
 			finish();
 		}
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		registerForContextMenu((ListView)findViewById(R.id.gamelobby_players));
 	}
 	
 	@Override
 	public void onBackPressed() {
 		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
         alertbox.setTitle(getResources().getString(R.string.gamelobby_leave_title));
         alertbox.setMessage(getResources().getString(R.string.gamelobby_leave_msg));
 
         alertbox.setPositiveButton(getResources().getString(R.string.gamelobby_leave_yes),
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface arg0, int arg1) {
                         leaveGame();
                     }
                 });
 
         alertbox.setNeutralButton(getResources().getString(R.string.gamelobby_leave_no),
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface arg0, int arg1) {
                     }
                 });
 
         alertbox.show();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.gamelobby_menu, menu);
 		Player p = new PlayerSQLiteDAO(this).getPlayer();
 		// Options-Menü-Eintrag "Spiel starten" gegebenenfalls deaktivieren
 		if(p.id != game.creator.id) {
 			menu.findItem(R.id.gamelobby_start).setEnabled(false);
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.gamelobby_start:
 			Intent success = new Intent(this, InGame.class);
 			success.putExtra("gameid", game.id);
 			success.putExtra("playerid", player.id);
			HttpGet startGet = new HttpGet(C.URL+"?action=start_game&gameid="+game.id);
			new HttpAsyncTask(startGet, this, success, true).execute();
 			refreshHandler.removeCallbacks(runnableRefresh);
 			break;
 		case R.id.gamelobby_leave:
 			leaveGame();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		if(player.id == game.creator.id) {
 			super.onCreateContextMenu(menu, v, menuInfo);
 			getMenuInflater().inflate(R.menu.gamelobby_context_menu, menu);
 		}
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 		  	case R.id.gamelobby_context_kick:
 		  		View v = info.targetView;
 		  		int playerId = Integer.parseInt(((TextView)v.findViewById(R.id.player_id)).getText().toString());
 		  		if(playerId != player.id) {
 			  		HttpGet kickGet = new HttpGet(C.URL+"?action=kick_player&gameid="+game.id+"&creator="+game.creator.id+"&player="+playerId);
 			  		HttpAsyncTask kickTask = new HttpAsyncTask(kickGet, this, null, false);
 			  		kickTask.execute();
 					try {
 						JSONObject response = kickTask.get();
 						if(response.getInt("error") == 0) {
 							Toast.makeText(this, getResources().getString(R.string.gamelobby_player_kicked), Toast.LENGTH_LONG).show();
 							refreshPlayers();
 						} else {
 							Toast.makeText(this, getResources().getString(R.string.gamelobby_player_kick_failed), Toast.LENGTH_LONG).show();
 						}
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} catch (ExecutionException e) {
 						e.printStackTrace();
 					} catch (NotFoundException e) {
 						e.printStackTrace();
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 		  		} else {
 		  			Toast.makeText(this, getResources().getString(R.string.gamelobby_player_kick_failed), Toast.LENGTH_LONG).show();
 		  		}
 		  		return true;
 		  	default:
 		  		return super.onContextItemSelected(item);
 		}
 	}
 	
 	public void refreshPlayers() {
 		ListView lv_players = (ListView)findViewById(R.id.gamelobby_players);
 		
		HttpGet getGame = new HttpGet(C.URL + "?action=get_game&gameid="+game.id);
		HttpAsyncTask getGameTask = new HttpAsyncTask(getGame, this, null, false);
		getGameTask.execute();
		
 		HttpGet getPlayers = new HttpGet(C.URL + "?action=list_players&gameid=" + game.id);
 		HttpAsyncTask listPlayersTask = new HttpAsyncTask(getPlayers, this, null, false);
 		listPlayersTask.execute();
 		
 		try {
			JSONObject gameResult = getGameTask.get();
			if(gameResult.getInt("error") == 0) {
				this.game = new Game(gameResult.getJSONObject("data"));
				if(game.status == 1) {
					Toast.makeText(this, getResources().getString(R.string.gamelobby_start_msg), Toast.LENGTH_LONG).show();
					Intent success = new Intent(this, InGame.class);
					success.putExtra("gameid", game.id);
					success.putExtra("playerid", player.id);
					refreshHandler.removeCallbacks(runnableRefresh);
					isInLoop = false;
					startActivity(success);
					return;
				}
			}
			
 			JSONObject result = listPlayersTask.get();
 			if(result.getInt("error") > 0) {
 				// Spiel-Ersteller hat das Spiel verlassen => Zurück in Lobby
 				Toast.makeText(this, getResources().getString(R.string.err_creator_left), Toast.LENGTH_LONG).show();
 				refreshHandler.removeCallbacks(runnableRefresh);
 				isInLoop = false;
 				finish();
 				return;
 			}
 			JSONArray jPlayers = result.getJSONObject("data").getJSONArray("players");
 			players.clear();
 			
 			boolean iAmIn = false;
 			
 			for(int i = 0; i < jPlayers.length(); i++) {
 				Player p = new Player(jPlayers.getJSONObject(i));
 				if(p.id == player.id) {
 					iAmIn = true;
 				}
 				players.add(p);
 			}
 			
 			if(!iAmIn) {
 				Toast.makeText(this, getResources().getString(R.string.gamelobby_you_were_kicked), Toast.LENGTH_LONG).show();
 				refreshHandler.removeCallbacks(runnableRefresh);
 				finish();
 			}
 			
 			lv_players.setAdapter(new PlayerAdapter(this, R.layout.player_list_item, players));
 			lv_players.forceLayout();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void leaveGame() {
 		refreshHandler.removeCallbacks(runnableRefresh);
 		if(game != null) {
 			HttpGet getLeave = new HttpGet(C.URL + "?action=leave_game&user=" + player.username + "&gameid=" + game.id);
 			HttpAsyncTask leaveTask = new HttpAsyncTask(getLeave, this, null, false);
 			leaveTask.execute();
 		}
 		finish();
 	}
 	
 	public void initGameLobby() {
 		TextView tv_name = (TextView)findViewById(R.id.gamelobby_name);
 		ImageView iv_size = (ImageView)findViewById(R.id.gamelobby_size_image);
 		
 		tv_name.setText(game.name);
 		switch(game.gameSize) {
 		case 8:
 			iv_size.setImageResource(R.drawable.game_size_8);
 			break;
 		case 16:
 			iv_size.setImageResource(R.drawable.game_size_16);
 			break;
 		case 32:
 			iv_size.setImageResource(R.drawable.game_size_32);
 			break;
 		}
 		
 		isInLoop = true;
 		refreshHandler.postDelayed(runnableRefresh, 100);
 	}
 	
 	private Runnable runnableRefresh = new Runnable() {
 		public void run() {
 			refreshPlayers();
 			if(isInLoop) {
 				refreshHandler.postDelayed(this, 7500);
 			}
 		}
 	};
 }
