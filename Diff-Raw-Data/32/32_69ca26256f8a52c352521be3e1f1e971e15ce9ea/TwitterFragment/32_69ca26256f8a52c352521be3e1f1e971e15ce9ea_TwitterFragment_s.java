 package com.pc.programmerslife.fragments;
 
 import java.util.ArrayList;
 
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.pc.programmerslife.R;
 import com.pc.programmerslife.Tweet;
 import com.pc.programmerslife.TwitterManager;
 import com.pc.programmerslife.TwitterManager.TwitterListener;
 import com.pc.programmerslife.adapters.TwitterListAdapter;
 
 public class TwitterFragment extends SherlockListFragment implements TwitterListener {
 	private ArrayList<Tweet> tweets;
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		setHasOptionsMenu(true);
 		
 		if (savedInstanceState == null)
 			tweets = new ArrayList<Tweet>();
 		else
 			tweets = savedInstanceState.getParcelableArrayList(Tweet.EXTRA_TWEETS);
 		
 		if (tweets != null) {
 			setListAdapter(new TwitterListAdapter(getSherlockActivity(), R.layout.tweet_item_layout, tweets));
 			
			getListView().setSelector(new ColorDrawable(Color.TRANSPARENT));
			
 			ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.twitterFragment_progressBar);
 			progressBar.setVisibility(tweets.size() > 0 ? View.VISIBLE : View.INVISIBLE);
 		}
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		update();
 	}
 	
 	@Override
 	public void onDestroy() {
 		TwitterManager.getInstance().setTwitterListener(null);
 		super.onDestroy();
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.twitter_fragment, container, false);
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.twitter_fragment_menu, menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.twetterFragmentMenu_refresh) {
 			update();
 			return true;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putParcelableArrayList(Tweet.EXTRA_TWEETS, tweets);
 	}
 
 	@Override
 	public void onFinishGetTweets(ArrayList<Tweet> tweets) {
 		this.tweets.clear();
 		this.tweets.addAll(tweets);
 		
 		((TwitterListAdapter) getListAdapter()).notifyDataSetChanged();
 		
 		ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.twitterFragment_progressBar);
 		progressBar.setVisibility(View.INVISIBLE);
 	}
 
 	@Override
 	public void onFailGetTweets(Exception e) {
 		ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.twitterFragment_progressBar);
 		progressBar.setVisibility(View.INVISIBLE);
 		
 		Toast.makeText(getSherlockActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
 	}
 	
 	private void update() {
 		ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.twitterFragment_progressBar);
 		progressBar.setVisibility(View.VISIBLE);
 		
 		TwitterManager.getInstance().getTweets(this);
 	}
 }
