 package games.distetris.presentation;
 
 import games.distetris.domain.CtrlDomain;
 import games.distetris.domain.WaitingRoom;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.KeyEvent;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class JoinGameWaiting extends Activity {
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 
 			Bundle b = msg.getData();
 			String type = b.getString("type");
 
 			if (type.equals("WAITINGROOM")) {
 				updateWaitingRoom(b);
 			} else if (type.equals("STARTGAME")) {
 				startGame();
 			} else if (type.equals("SHUTDOWN")) {
 				Toast.makeText(getBaseContext(), "The server closed the connection", Toast.LENGTH_SHORT).show();
 				finish();
 			}
 
 		}
 	};
 
 	/**
 	 * Reference: http://developer.android.com/images/activity_lifecycle.png
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.gamewaiting);
 
 		Bundle bundle = this.getIntent().getExtras();
 		String serverName = bundle.getString("NAME");
 		String serverIP = bundle.getString("IP");
 		int serverPort = bundle.getInt("PORT");
 		
 		try {
 			CtrlDomain.getInstance().setHandlerUI(handler);
 			CtrlDomain.getInstance().serverTCPConnect(serverIP, serverPort);
 		} catch (Exception e) {
 			Toast.makeText(getBaseContext(), "Couldn't connect to the server "+serverName, Toast.LENGTH_SHORT).show();
 			finish();
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			CtrlDomain.getInstance().serverTCPDisconnect();
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * Parse a newly received WaitingRoom class
 	 * 
 	 * @param b
 	 *            Bundle containing the WaitingRoom class
 	 */
 	protected void updateWaitingRoom(Bundle b) {
 
 		WaitingRoom room = (WaitingRoom) b.getSerializable("room");
 
 		TextView tv;
 		tv = (TextView) findViewById(R.id.WaitingServername);
 		tv.setText(String.valueOf(room.name));
 		tv = (TextView) findViewById(R.id.WaitingNumTeams);
 		tv.setText(String.valueOf(room.numTeams));
 		tv = (TextView) findViewById(R.id.WaitingNumTurns);
 		tv.setText(String.valueOf(room.numTurns));
 
 		tv = (TextView) findViewById(R.id.PlayerID);
 		tv.setText(String.valueOf(room.currentPlayerID));
 		tv = (TextView) findViewById(R.id.TeamID);
 		tv.setText(String.valueOf(room.currentTeamID));
 	
 		LinearLayout ll = (LinearLayout) findViewById(R.id.Players);
 		
 		for (int i = 0; i < room.players.size(); i++) {
 			View child = getLayoutInflater().inflate(R.layout.row_player, null);
 
 			tv = (TextView) child.findViewById(R.id.Team);
 			tv.setText(String.valueOf(room.players.get(i).team));
 			tv = (TextView) child.findViewById(R.id.Player);
 			tv.setText(String.valueOf(room.players.get(i).name));
 			ll.addView(child);
 		}
 	}
 
 	/**
 	 * Change the view to Game because the server started the game
 	 */
 	protected void startGame() {
 		Intent i = new Intent();
 		i.setClass(getBaseContext(), Game.class);
 		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(i);
 		finish();
 	}
 }
 
