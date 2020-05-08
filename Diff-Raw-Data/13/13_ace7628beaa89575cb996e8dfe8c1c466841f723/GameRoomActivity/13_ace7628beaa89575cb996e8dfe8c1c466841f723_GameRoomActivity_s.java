 package ca.polymtl.inf8405.sevenwonders;
 
 import ca.polymtl.inf8405.sevenwonders.api.GameState;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 
 import org.apache.thrift.TException;
 
 public class GameRoomActivity extends Activity{
 
 	public static final String MESSAGE_GAME_BEGIN = "begin";
 
 	public GameRoomActivity() {
 		Receiver.getInstance().addObserver(new ApiDelegate());
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_game_room);
 
 		// Create a list view of game room
 		final ListView listView = (ListView) findViewById(R.id.listView);
 		adapter_ = new ArrayAdapter<String>(this, R.layout.gameroom_item,players_);
 		listView.setAdapter(adapter_);
 		listView.setTextFilterEnabled(true);
 
 		Object[] connectedUsers = (Object[]) getIntent().getSerializableExtra(ListGameRoomActivity.CONNECTED_MESSAGE);
 		for( Object user: connectedUsers ){
 			players_.add((String)user);
 		}
 

 		adapter_.notifyDataSetChanged();
 	}
 
 	public void play(View view) {
 		try {
 			Sender.getInstance().s_start();
 		} catch( TException e ) {
 			Log.wtf("game room activity", e.getMessage());
 		}
 	}
 
 	private class ApiDelegate extends Api {
 		@Override public void c_joined(final String user) throws TException {
 			runOnUiThread( new Runnable() {
 				@Override
 				public void run() {
 					players_.add(user);
 					adapter_.notifyDataSetChanged();
 				}
 			});
 		}
 
 		@Override public void c_begin(final GameState state) throws TException {
 			runOnUiThread( new Thread( new Runnable() {
 				@Override
 				public void run() {
 					Intent intent = new Intent(GameRoomActivity.this, GameScreenActivity.class);
 					intent.putExtra(MESSAGE_GAME_BEGIN, state);
 					startActivity(intent);
 				}
 			}));
 		}
 	}
 
 	private static ArrayAdapter adapter_;
 	private static ArrayList<String> players_ = new ArrayList<String>();
 }
