 package illinois.sweng.sctracker;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class PlayerListActivity extends ListActivity {
 
 	private TrackerDatabaseAdapter mDatabaseAdapter;
 	private Cursor mPlayerCursor;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		ListView listView = getListView();
 		listView.setTextFilterEnabled(true);
 		listView.setOnItemClickListener(new PlayerListClickListener());
 		
 		mDatabaseAdapter = new TrackerDatabaseAdapter(this);
 		mDatabaseAdapter.open();
 		
 		mPlayerCursor = mDatabaseAdapter.getAllPlayers();
 		startManagingCursor(mPlayerCursor);
 		
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
 				R.layout.playerlistrow, mPlayerCursor, fields, textViews);
 		
 		setListAdapter(cursorAdapter);
 	}
 	
 	private void showPlayerStatus(Intent i) {
 		i.setClass(this, PlayerStatusActivity.class);
 		startActivity(i);
 	}
 	
 	private class PlayerListClickListener implements AdapterView.OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			mPlayerCursor.moveToPosition(position);
 			Intent i = new Intent();
 			Resources res = getResources();
 			
 			String rowKey = res.getString(R.string.keyRowID);
 			putIntExtra(rowKey, i);
 			
 			String pkKey = res.getString(R.string.keyPK);
 			putLongExtra(pkKey, i);
 			
 			String pictureKey = res.getString(R.string.keyPicture);
 			putStringExtra(pictureKey, i);
 			
 			String handleKey = res.getString(R.string.keyHandle);
 			putStringExtra(handleKey, i);
 			
 			String nameKey = res.getString(R.string.keyName);
 			putStringExtra(nameKey, i);
 			
 			String raceKey = res.getString(R.string.keyRace);
 			putStringExtra(raceKey, i);
 			
 			String teamKey = res.getString(R.string.keyTeam);
 			int teamIndex = mPlayerCursor.getColumnIndexOrThrow(teamKey);
 			int team = mPlayerCursor.getInt(teamIndex);
 			// TODO look up the team, not just send the team ID
 			i.putExtra(teamKey, team + "");
 			
 			String nationalityKey = res.getString(R.string.keyNationality);
 			putStringExtra(nationalityKey, i);
 			
 			String eloKey = res.getString(R.string.keyELO);
 			putIntExtra(eloKey, i);
 			
 			showPlayerStatus(i);
 		}
 		
 		private void putIntExtra(String key, Intent i) {
 			int index = mPlayerCursor.getColumnIndexOrThrow(key);
 			int rowID = mPlayerCursor.getInt(index);
 			i.putExtra(key, rowID);
 		}
 		
 		private void putStringExtra(String key, Intent i) {
 			int index = mPlayerCursor.getColumnIndexOrThrow(key);
 			String name = mPlayerCursor.getString(index);
 			i.putExtra(key, name);
 		}
 		
 		private void putLongExtra(String key, Intent i) {
 			int index = mPlayerCursor.getColumnIndexOrThrow(key);
 			long pk = mPlayerCursor.getLong(index);
 			i.putExtra(key, pk);
 		}
 	}
 }
