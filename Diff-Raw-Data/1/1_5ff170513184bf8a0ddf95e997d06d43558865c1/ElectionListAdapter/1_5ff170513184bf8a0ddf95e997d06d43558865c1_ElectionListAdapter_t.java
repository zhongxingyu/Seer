 package cc.rainwave.android;
 
 import java.io.IOException;
 
 import cc.rainwave.android.api.Session;
 import cc.rainwave.android.api.types.RainwaveException;
 import cc.rainwave.android.api.types.Song;
 import cc.rainwave.android.api.types.VoteResult;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class ElectionListAdapter extends BaseAdapter {
 	private static final String TAG = "ElectionListAdapter";
 	
 	private View mViews[];
 	
 	private Song mSongs[];
 	
 	private Context mContext;
 	
 	private Session mSession;
 	
 	private CountdownTask mCountdownTask;
 	
 	private VoteTask mVoteTask;
 	
 	public ElectionListAdapter(Context ctx, Session session, Song songs[]) {
 		mContext = ctx;
 		mSession = session;
 		mSongs = songs;
 		mViews = new View[mSongs.length];
 	}
 	
 	public void startCountdown(int i) {
 		if(hasVoted()) return;
 		
 		if(mCountdownTask == null) {
 			mCountdownTask = new CountdownTask(i);
 			mCountdownTask.execute();
 			return;
 		}
 		
 		if(mCountdownTask.getSelection() == i) {
 			mCountdownTask.cancel(true);
 			submitVote(i);
 		}
 		else {
 			int old = mCountdownTask.getSelection();
 			mCountdownTask.cancel(true);
 			mCountdownTask = new CountdownTask(i);
 			mCountdownTask.execute();
 			setRating(old);
 		}
 	}
 	
 	public boolean hasVoted() {
 		return mVoteTask != null;
 	}
 
 	@Override
 	public int getCount() {
 		return (mSongs == null) ? 0 : mSongs.length;
 	}
 
 	@Override
 	public Object getItem(int i) {
 		return (mSongs == null) ? null : mSongs[i];
 	}
 
 	@Override
 	public long getItemId(int i) {
 		return (mSongs == null) ? -1 : mSongs[i].song_id;
 	}
 	
 	@Override
 	public View getView(int i, View convertView, ViewGroup parent) {
 		if(convertView == null) {
 			Song s = mSongs[i];
 			LayoutInflater inflater = LayoutInflater.from(mContext);
 			convertView = inflater.inflate(R.layout.item_song, null);
 			
 			((TextView)convertView.findViewById(R.id.election_songTitle)).setText(s.song_title);
 			((TextView)convertView.findViewById(R.id.election_songAlbum)).setText(s.album_name);
 			((TextView)convertView.findViewById(R.id.election_songArtist)).setText(s.collapseArtists());
 			
 			reflectSong(((CountdownView)convertView.findViewById(R.id.election_songRating)), s);
 		}
 		
 		mViews[i] = convertView;
 		return convertView;
 	}
 	
 	private CountdownView getCountdownView(int i) {
 		return (CountdownView) mViews[i].findViewById(R.id.election_songRating);
 	}
 	
 	private void reflectSong(CountdownView v, Song s) {
 		v.setBoth(s.song_rating_user, s.song_rating_avg);
 		v.setAlternateText(R.string.label_unrated);
 	}
 	
 	private void setRating(int i) {
 		reflectSong(getCountdownView(i), mSongs[i]);
 	}
 	
 	private void setVoting(int i) {
 		CountdownView cnt = getCountdownView(i);
 		cnt.setBoth(0, 0);
 		cnt.setAlternateText(R.string.label_voting);
 	}
 	
 	private void setVoted(int i) {
 		CountdownView cnt = getCountdownView(i);
 		cnt.setBoth(0, 0);
 		cnt.setAlternateText(R.string.label_voted);
 	}
 	
 	public void submitVote(int selection) {
 		mVoteTask = new VoteTask(selection);
 		mVoteTask.execute(mSongs[selection]);
 		setVoting(selection);
 	}
 	
 	private class VoteTask extends AsyncTask<Song, Integer, Boolean> {
 		
 		private CountdownView mCountdown;
 		
 		private Song mSong;
 		
 		private int mSelection;
 		
 		public VoteTask(int selection) {
 			mCountdown = getCountdownView(selection);
 			mSelection = selection;
 		}
 		
 		protected Boolean doInBackground(Song...params) {
 			mSong = params[0];
 			
 			try {
 				VoteResult result = mSession.vote(mSong.elec_entry_id);
 				return true;
 			} catch (IOException e) {
 				Rainwave.showError(ElectionListAdapter.this.mContext, e);
 				Log.e(TAG, "IO Error: " + e);
 			} catch (RainwaveException e) {
 				Rainwave.showError(ElectionListAdapter.this.mContext, e);
 				Log.e(TAG, "API Error: " + e);
 			}
 			
 			return false;
 		}
 		
 		protected void onPostExecute(Boolean result) {
 			if(result) {
 				setVoted(mSelection);
 			}
 			else {
 				reflectSong(mCountdown, mSong);
 				mVoteTask = null;
 			}
 		}
 	}
 	
 	private class CountdownTask extends AsyncTask<Integer, Integer, Boolean> {
 		private int mSelection;
 		
 		private CountdownView mCountdownView;
 		
 		private Song mSong;
 		
 		public CountdownTask(int selection) {
 			mSelection = selection;
 			View v = ElectionListAdapter.this.mViews[mSelection];
 			mCountdownView = (CountdownView) v.findViewById(R.id.election_songRating);
 			mSong = mSongs[selection];
 		}
 		
 		@Override
 		protected Boolean doInBackground(Integer ...params) {
 			mCountdownView.setMax(5.0f);
 			mCountdownView.setBoth(5.0f, 0.0f);
 			mCountdownView.setShowValue(true);
 			mCountdownView.setAlternateText(R.string.label_voting);
 			while(mCountdownView.getPrimary() > 0) {
 				mCountdownView.decrementPrimary(0.1f);
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					return false;
 				}
 			}
 			
 			return true;
 		}
 		
 		protected void onPostExecute(Boolean result) {
 			if(result == true) {
 				submitVote(mSelection);
 			}
 			else {
 				reflectSong(mCountdownView, mSong);
 			}
			mCountdownTask = null;
 		}
 		
 		public int getSelection() {
 			return mSelection;
 		}
 	}
 
 }
