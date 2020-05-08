 package org.canthack.tris.oyver;
 
 import java.util.Queue;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class VoteListActivity extends Activity {
 	private DialogInterface.OnClickListener clearAllClickListener;
 	private ListView lv;
 	private Queue<Vote> votes;
 	private ArrayAdapter<Vote> arrayAdapter;
 	private MenuItem clearAllItem;
 	
 	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.d("Votes queue changed listener", "Received event");
 			runOnUiThread(new Runnable(){
 				@Override
 				public void run() {
 					setAdapter();
 				}
 			});
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_vote_list);
 
 		votes = ((OyVerApp)getApplication()).votes;
 		lv = (ListView) findViewById(R.id.list);
 
 		//for when the queue changes..
 		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
 				new IntentFilter("voteQueueUpdated"));
 
 		clearAllClickListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which){
 				case DialogInterface.BUTTON_POSITIVE:
 					deleteAllVotes();
 					break;
 				}
 			}
 		};
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				promptDeleteVote((Vote)parent.getAdapter().getItem(position));
 			}
 		});
 	}
 
 	protected void promptDeleteVote(final Vote item) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which){
 				case DialogInterface.BUTTON_POSITIVE:
 					try{votes.remove(item);}catch(Exception e){}
 					setAdapter();
 					break;
 				}
 			}
 		};
 		builder.setMessage(R.string.are_you_sure_clear).setPositiveButton(R.string.yes, listener)
 		.setNegativeButton(R.string.no, listener).show();
 	}
 
 	private void setAdapter() {
 		Vote[] voteArray = ((OyVerApp)getApplication()).votes.toArray(new Vote[0]);
 		arrayAdapter = new ArrayAdapter<Vote>(this, android.R.layout.simple_list_item_1, voteArray);
 		lv.setAdapter(arrayAdapter);
		clearAllItem.setVisible(voteArray.length > 0);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.vote_list, menu);
 		this.clearAllItem = (MenuItem) menu.findItem(R.id.action_clear_all);
		setAdapter(); //Do this now so that the menu item already exsits.
 		return true;
 	}
 
 	//Handles menu clicks
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.action_clear_all:
 			clearAll();
 			return true;
 		default:
 			break;
 
 		}
 		return false;
 	}
 
 	private void clearAll() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.are_you_sure_clear_all).setPositiveButton(R.string.yes, clearAllClickListener)
 		.setNegativeButton(R.string.no, clearAllClickListener).show();
 	}
 
 	private void deleteAllVotes() {
 		Log.i("Vote List", "Clearing all votes");
 		votes.clear();
 		setAdapter();
 	}
 
 	@Override
 	public void onDestroy(){
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
 		super.onDestroy();
 	}
 }
