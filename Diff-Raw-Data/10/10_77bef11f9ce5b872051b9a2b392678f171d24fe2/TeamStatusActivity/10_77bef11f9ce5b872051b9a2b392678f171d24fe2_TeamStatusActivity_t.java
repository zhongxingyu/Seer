 package illinois.sweng.sctracker;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class TeamStatusActivity extends ListActivity{
 	static String TAG = "teamStatusActivity";
 	DBAdapter mDBAdapter;
 	
 	String name = "";
 	String teamTag = "";
 	int rowID = -1;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 //		setContentView(R.layout.teamstatus);
 		
 		getDataFromIntent();
 		
 		ListView listView = getListView();
 		listView.setTextFilterEnabled(true);
 		//TODO: Setup listener
 		
 		mDBAdapter = new DBAdapter(this);
 		mDBAdapter.open();
 		
 		
 
 			Cursor players = mDBAdapter.getPlayersByTeam(rowID);
 			
 //			TextView t = (TextView)findViewById(R.id.textView1);
 //			t.append("Team Name: " + name);
 //			t = (TextView)findViewById(R.id.textView2);
 //			t.append("Tag: " + teamTag);
 			
 			startManagingCursor(players);
 			String fields[] = 	{
 					TrackerDatabaseAdapter.KEY_HANDLE,
 					TrackerDatabaseAdapter.KEY_RACE,
 					TrackerDatabaseAdapter.KEY_ROWID,
 					TrackerDatabaseAdapter.KEY_PK,
 					TrackerDatabaseAdapter.KEY_PICTURE,
 					TrackerDatabaseAdapter.KEY_NAME,
 					TrackerDatabaseAdapter.KEY_TEAM,
 					TrackerDatabaseAdapter.KEY_NATIONALITY,
 					TrackerDatabaseAdapter.KEY_ELO
 								};
 			int textViews[] = {R.id.playerListName, R.id.playerListRace};
 			
 			CursorAdapter cursorAdapter = new SimpleCursorAdapter(this, 
 					R.layout.playerlistrow, players, fields, textViews);
 			
 			setListAdapter(cursorAdapter);
 			
 					
 		mDBAdapter.close();
 	}
 	
 	private void getDataFromIntent(){
 		Intent intent = getIntent();
 		Resources res = getResources();
 		
 		String tagKey = res.getString(R.string.keyTag);
 		teamTag = intent.getStringExtra(tagKey);
 		
 		String nameKey = res.getString(R.string.keyName);
 		name = intent.getStringExtra(nameKey);
 		
 		String rowIDKey = res.getString(R.string.keyRowID);
 		rowID = intent.getIntExtra(rowIDKey, -1);
 		
 	}
 
 }
