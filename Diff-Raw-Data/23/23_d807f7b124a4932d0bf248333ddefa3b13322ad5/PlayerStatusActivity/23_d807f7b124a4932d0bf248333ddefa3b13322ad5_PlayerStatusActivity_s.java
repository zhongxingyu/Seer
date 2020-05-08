 package illinois.sweng.sctracker;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 public class PlayerStatusActivity extends Activity {
 	static String TAG = "playerStatusActivity";
 	// String data;
 	// JSONObject player;
 	// JSONObject playerData;
 	TrackerDatabaseAdapter mDBAdapter;
 
 	String handle = "";
 	Object picture = "";
 	String name = "";
 	String race = "";
 	String team = "";
 	String nationality = "";
 	String elo;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.playerstatus);
 
 		// Bundle extras = getIntent().getExtras();
 		// if(extras == null)
 		// return; // might want to log here - indicate extras was never
 		// assigned any data
 
 		// else {
 		// data = extras.getString("player");
 		// try{
 		// player = new JSONObject(data);
 		// playerData = player.getJSONObject("fields");
 		//
 		// handle = playerData.getString("handle").toString();
 		// //picture = playerData.getJSONObject("picture").toString();
 		// name = playerData.getString("name").toString();
 		// race = playerData.getString("race").toString();
 		// team = playerData.getString("team").toString();
 		// nationality = playerData.getString("nationality").toString();
 		// elo = playerData.getInt("elo") + "";
 		//
 		// } catch (JSONException e){
 		// e.printStackTrace();
 		// }
 		mDBAdapter = new TrackerDatabaseAdapter(this);
 		mDBAdapter.open();
 		Cursor player = mDBAdapter.getPlayer(1);
 
 		if (player.moveToFirst()) {
 			handle = player.getString(2);
 			name = player.getString(3);
 			race = player.getString(4);
 			team = player.getString(5);
 			nationality = player.getString(6);
 			elo = player.getString(7);
 
			TextView t = (TextView) findViewById(R.id.textView1);
 			t.append(handle);
			t = (TextView) findViewById(R.id.textView2);
 			t.append(name);
			t = (TextView) findViewById(R.id.textView3);
 			t.append(race);
			t = (TextView) findViewById(R.id.textView4);
 			t.append(team);
			t = (TextView) findViewById(R.id.textView5);
 			t.append(nationality);
			t = (TextView) findViewById(R.id.textView6);
 			t.append(elo);
 		} else {
 			Log.d("TAG", "OH GOD EMPTY CURSOR");
 		}
 
 		mDBAdapter.close();
 	}
 }
