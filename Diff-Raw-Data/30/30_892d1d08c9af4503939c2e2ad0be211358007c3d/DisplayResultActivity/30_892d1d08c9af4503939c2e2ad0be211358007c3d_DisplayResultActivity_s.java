 package ch.bfh.evoting.votinglib;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.ListActivity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.bfh.evoting.votinglib.adapters.OptionListAdapter;
 import ch.bfh.evoting.votinglib.db.PollDbHelper;
 import ch.bfh.evoting.votinglib.entities.DatabaseException;
 import ch.bfh.evoting.votinglib.entities.Option;
 import ch.bfh.evoting.votinglib.entities.Poll;
 import ch.bfh.evoting.votinglib.util.BroadcastIntentTypes;
 import ch.bfh.evoting.votinglib.util.HelpDialogFragment;
 import ch.bfh.evoting.votinglib.util.OptionsComparator;
 
 /**
  * Activity displaying the results of a poll
  * @author Philémon von Bergen
  *
  */
 public class DisplayResultActivity extends ListActivity {
 
 	private int pollId;
 	private boolean saveToDbNeeded;
 	
 	private BroadcastReceiver redoPollReceiver;
 
 	@SuppressLint("SimpleDateFormat")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_display_result);
 		setupActionBar();
 
 		ListView lv = (ListView)findViewById(android.R.id.list);
 		LayoutInflater inflater = this.getLayoutInflater();
 
 		View header = inflater.inflate(R.layout.result_header, null, false);
 		View footer = inflater.inflate(R.layout.result_footer, null, false);
 		
 		lv.addHeaderView(header);
 		lv.addFooterView(footer);
 
 		final Poll poll = (Poll)this.getIntent().getSerializableExtra("poll");
 
 		String packageName = getApplication().getApplicationContext().getPackageName();
 		if(!packageName.equals("ch.bfh.evoting.adminapp")){
 			LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layout_action_bar);
 			linearLayout.removeView(linearLayout);
 			
 			//register a listener of messages of the admin sending the electorate
 			redoPollReceiver = new BroadcastReceiver(){
 
 				@Override
 				public void onReceive(Context context, Intent intent) {
 					LocalBroadcastManager.getInstance(DisplayResultActivity.this).unregisterReceiver(this);
 					Intent i = new Intent("ch.bfh.evoting.voterapp.CheckElectorateActivity");
 					i.putExtra("participants", intent.getSerializableExtra("participants"));
 					startActivity(i);
 				}
 			};
 			LocalBroadcastManager.getInstance(this).registerReceiver(redoPollReceiver, new IntentFilter(BroadcastIntentTypes.electorate));
 		} else {
 
 			//Set a listener on the redo button
 			Button btnRedo = (Button)findViewById(R.id.button_redo_poll);
 			btnRedo.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					Poll newPoll = new Poll();
 					newPoll.setQuestion(poll.getQuestion());
 					List<Option> newOptions = new ArrayList<Option>();
 					for(Option op : poll.getOptions()){
 						Option newOp = new Option();
 						newOp.setText(op.getText());
 						newOptions.add(newOp);
 					}
 					newPoll.setOptions(newOptions);
 
 					PollDbHelper pollDbHelper = PollDbHelper.getInstance(DisplayResultActivity.this);
 					try {
 						int pollId = (int)pollDbHelper.savePoll(newPoll);
 						Intent i = new Intent("ch.bfh.evoting.adminapp.PollDetailActivity");
 						i.putExtra("pollid", pollId);
 						startActivity(i);
 					} catch (DatabaseException e) {
 						Toast.makeText(DisplayResultActivity.this, getString(R.string.redo_impossible), Toast.LENGTH_LONG).show();
 						e.printStackTrace();
 					}
 				}
 			});
 		}
 
 		lv.addHeaderView(header);
 
 		//Get the data in the intent
 		saveToDbNeeded = this.getIntent().getBooleanExtra("saveToDb", false);
 		if(poll.getId()>=0){
 			pollId = poll.getId();
 		} else {
 			pollId = -1;
 		}
 
 		//Set GUI informations
 		TextView tvQuestion = (TextView)header.findViewById(R.id.textview_poll_question);
 		tvQuestion.setText(poll.getQuestion());
 
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 		Date resultdate = new Date(poll.getStartTime());
 
 		
 		// Do some statistics
 		
		int numberVoters = poll.getParticipants().size();
 		
 		int numberCastVotes = 0;
 		
 		for (Option option : poll.getOptions()){
 			numberCastVotes += option.getVotes();
 		}
 		
		double participation = numberVoters / numberCastVotes * 100;
 		
 		TextView tvPollTime = (TextView)header.findViewById(R.id.textview_poll_start_time);
 		tvPollTime.setText(getString(R.string.poll_start_time) + ": " + sdf.format(resultdate));
 		
 		TextView tvNumberVoters = (TextView)footer.findViewById(R.id.textview_number_voters);
		tvNumberVoters.setText(getString(R.string.number_voters) + ": " + numberVoters);
 		
 		TextView tvNumberCastVotes = (TextView)footer.findViewById(R.id.textview_number_cast_votes);
 		tvNumberCastVotes.setText(getString(R.string.number_cast_votes) + ": " + numberCastVotes);
 		
 		TextView tvParticipation = (TextView)footer.findViewById(R.id.textview_vote_participation);
		tvParticipation.setText(getString(R.string.participation) + ": " + participation);
 		
 		
 		//Order the options in descending order
 		Collections.sort(poll.getOptions(), new OptionsComparator());
 		//Create the list
 		setListAdapter(new OptionListAdapter(this, R.layout.list_item_result, poll.getOptions()));
 
 		//Save the poll to the DB if needed
 		if(saveToDbNeeded){
 			try {
 				if(pollId>=0){
 					PollDbHelper.getInstance(this).updatePoll(pollId,poll);
 				} else {
 					int newPollId = (int)PollDbHelper.getInstance(this).savePoll(poll);
 					this.pollId = newPollId;
 					poll.setId(newPollId);
 				}
 			} catch (DatabaseException e) {
 				Log.e(this.getClass().getSimpleName(), "DB error: "+e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		
 		if(packageName.equals("ch.bfh.evoting.voterapp") || !saveToDbNeeded){
 			LinearLayout ll = (LinearLayout)findViewById(R.id.layout_action_bar);
 			((LinearLayout)ll.getParent()).removeView(ll);
 		}
 
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 
 	@Override
 	public void onBackPressed() {
 		if(!this.saveToDbNeeded){
 			super.onBackPressed();
 		} else {
 			//do nothing we don't want that people reaccess to an old activity
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		if (item.getItemId() == android.R.id.home){
 			AndroidApplication.getInstance().getNetworkInterface().disconnect();
 			String packageName = getApplication().getApplicationContext().getPackageName();
 			//if ending a poll
 			if(saveToDbNeeded){
 				if(packageName.equals("ch.bfh.evoting.voterapp")){
 					Intent i = new Intent("ch.bfh.evoting.voterapp.VoterAppMainActivity");
 					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
 		                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
 		                    Intent.FLAG_ACTIVITY_NEW_TASK);
 
 					startActivity(i);
 				} else if (packageName.equals("ch.bfh.evoting.adminapp")){
 					Intent i = new Intent("ch.bfh.evoting.adminapp.AdminAppMainActivity");
 					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
 		                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
 		                    Intent.FLAG_ACTIVITY_NEW_TASK);
 
 					startActivity(i);
 				}
 				
 				
 			} else {
 				//if consulting an archive
 				startActivity(new Intent(this, ListTerminatedPollsActivity.class));
 			}
 			return true;
 		} else if (item.getItemId() == R.id.help){
 			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_display_results), getString(R.string.help_text_display_results) );
 			hdf.show( getFragmentManager( ), "help" );
 			return true;
 		}
 		return true;
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.display_results, menu);
 		return true;
 	}
 
 	
 //	BroadcastReceiver redoPollReceiver = new BroadcastReceiver(){
 //
 //		@Override
 //		public void onReceive(Context context, Intent intent) {
 //			LocalBroadcastManager.getInstance(DisplayResultActivity.this).unregisterReceiver(this);
 //			Intent i = new Intent("ch.bfh.evoting.voterapp.CheckElectorateActivity");
 //			i.putExtra("participants", intent.getSerializableExtra("participants"));
 //			startActivity(i);
 //		}
 //	};
 }
