 package com.fh.voting.activities;
 
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.fh.voting.Constants;
 import com.fh.voting.R;
 import com.fh.voting.lists.VoteListAdapter;
 import com.fh.voting.model.Vote;
 
 public class PendingVotesActivity extends DatabaseWrapperActivity {
 	private ListView lstVotes;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// customize title
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.public_votes);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
 		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.top_votes_activity);
 
 		Window window = getWindow();
 		window.setFormat(PixelFormat.RGBA_8888);
 
 		// init
 		this.lstVotes = (ListView) this.findViewById(R.id.lstVotes);
 
 		final PendingVotesActivity voteActivity = this;
 		this.lstVotes.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				Vote item = (Vote) parent.getItemAtPosition(position);
 				voteActivity.OnListItemClick(position, item);
 			}
 		});
 	}
 
 	protected void OnListItemClick(int position, Vote item) {
 		// manage my Vote
 		Bundle bundle = new Bundle();
 		bundle.putInt(Constants.bundle_vote_id, item.getId());
 		this.openActivity(PerformVoteActivity.class, bundle);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		VoteListAdapter adapter = new VoteListAdapter(this, R.layout.vote_list_item,
 				this.modelManager.getPendingVotes());
 		this.lstVotes.setAdapter(adapter);
 	};
 
 	private void openActivity(Class<?> cls, Bundle bundle) {
 		Intent intent = new Intent(PendingVotesActivity.this, cls);
 		if (bundle != null) {
 			intent.putExtras(bundle);
 		}
 		startActivity(intent);
 	}
 
 	public void onCreateClicked(View v) {
 		// exit
 		moveTaskToBack(true);
 	}
 }
