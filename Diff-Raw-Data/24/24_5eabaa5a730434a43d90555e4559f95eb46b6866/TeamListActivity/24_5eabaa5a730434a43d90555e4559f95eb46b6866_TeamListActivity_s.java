 package illinois.sweng.sctracker;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 
 public class TeamListActivity extends ListActivity {
 	static String TAG = "teamsActivity";
 
 	private DBAdapter mDBAdapter;
 	private Cursor mTeamCursor;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		ListView listView = getListView();
 		listView.setTextFilterEnabled(true);
 		listView.setOnItemClickListener(new TeamListClickListener());
 		
 		mDBAdapter = new DBAdapter(this);
 		mDBAdapter.open();
 		
 		mTeamCursor = mDBAdapter.getAllTeams();
 		startManagingCursor(mTeamCursor);
 		
 		String fields[] = 	{
 								DBAdapter.KEY_NAME, 
 								DBAdapter.KEY_TAG
 							};
 		int textViews[] = {R.id.teamListName, R.id.teamListTag};
 		
 		CursorAdapter cursorAdapter = new SimpleCursorAdapter(this,
 				R.layout.teamlistrow, mTeamCursor, fields, textViews);
 		
 		setListAdapter(cursorAdapter);
 	}
 	
 	private void showTeamStatus(Intent i){
 		i.setClass(this, TeamStatusActivity.class);
 		startActivity(i);
 	}
 	
 	private class TeamListClickListener implements AdapterView.OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			mTeamCursor.moveToPosition(position);
 			Intent i = new Intent();
 			Resources res = getResources();
 			
 			String rowKey = res.getString(R.string.keyRowID);
 			putIntExtra(rowKey, i);
 			
 			String pkKey = res.getString(R.string.keyPK);
 			putLongExtra(pkKey, i);
 			
 			String tagKey = res.getString(R.string.keyTag);
 			putStringExtra(tagKey, i);
 			
 			String nameKey = res.getString(R.string.keyName);
 			putStringExtra(nameKey, i);
 			
 			showTeamStatus(i);			
 		}
 		
 		private void putIntExtra(String key, Intent i){
 			int index = mTeamCursor.getColumnIndexOrThrow(key);
 			int rowID = mTeamCursor.getInt(index);
 			i.putExtra(key, rowID);
 		}
 		
 		private void putStringExtra(String key, Intent i) {
 			int index = mTeamCursor.getColumnIndexOrThrow(key);
 			String name = mTeamCursor.getString(index);
 			Log.d("String Extra", name);
 			i.putExtra(key, name);
 		}
 		
 		private void putLongExtra(String key, Intent i) {
 			int index = mTeamCursor.getColumnIndexOrThrow(key);
 			long pk = mTeamCursor.getLong(index);
 			i.putExtra(key, pk);
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mDBAdapter.close();
 	}
 }
