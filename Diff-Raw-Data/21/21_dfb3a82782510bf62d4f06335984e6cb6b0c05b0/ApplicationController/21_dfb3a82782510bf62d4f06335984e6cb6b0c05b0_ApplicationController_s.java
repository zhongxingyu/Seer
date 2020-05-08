 package com.josephblough.fluchallenge;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.josephblough.fluchallenge.activities.FluPodcasts;
 import com.josephblough.fluchallenge.data.Feed;
 import com.josephblough.fluchallenge.data.FluReport;
 import com.josephblough.fluchallenge.data.PodcastFeedEntry;
 import com.josephblough.fluchallenge.data.SyndicatedFeed;
 
 import android.app.Application;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnBufferingUpdateListener;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.util.Log;
 import android.widget.Toast;
 
 public class ApplicationController extends Application implements OnCompletionListener, OnPreparedListener, OnBufferingUpdateListener {
 
     private final static String TAG = "ApplicationController";
 
     public FluReport fluReport;
     public Feed fluUpdatesFeed;
     public Feed fluPodcastsFeed;
     public Map<Integer, SyndicatedFeed> syndicatedFeeds = new HashMap<Integer, SyndicatedFeed>();
     public Integer currentlyPlayingPodcast = null;
     public FluPodcasts activityToUpdateOnPlayCompletion = null;
 
     private MediaPlayer player = null;
 
     public void onCreate() {
 	super.onCreate();
 	
 	//Do Application initialization over here
 	Log.d(TAG, "onCreate");
 	player = new MediaPlayer();
 	player.setOnCompletionListener(this);
 	player.setOnPreparedListener(this);
 	player.setOnBufferingUpdateListener(this);
     }
 
     @Override
     public void onTerminate () {
 	if (player != null) {
 	    if (player.isPlaying()) {
 		player.stop();
 	    }
 	    player.release();
 	}
     }
     
     public void playPodcast(final int position) {
 	if (fluPodcastsFeed != null && fluPodcastsFeed.items != null && fluPodcastsFeed.items.size() > 0) {
 	    try {
 		currentlyPlayingPodcast = position;
 		PodcastFeedEntry entry = (PodcastFeedEntry)fluPodcastsFeed.items.get(position);
 		Log.d(TAG, "Downloading " + entry.mp3url);
 		player.reset();
 		player.setDataSource(entry.mp3url);
 		player.prepare();
 	    }
 	    catch (Exception e) {
 		Log.e(TAG, e.getMessage(), e);
 	    }
 	}
     }
     
     public void stopPodcast() {
 	currentlyPlayingPodcast = null;
 	if (player.isPlaying()) {
 	    player.stop();
 	}
 	
 	/*if (activityToUpdateOnPlayCompletion != null) {
 	    activityToUpdateOnPlayCompletion.refreshList();
 	}*/
     }
 
     public void onPrepared(MediaPlayer player) {
 	if (currentlyPlayingPodcast != null) {
 	    player.start();
 	}
     }
 
     public void onCompletion(MediaPlayer player) {
 	currentlyPlayingPodcast = null;
 	
 	if (activityToUpdateOnPlayCompletion != null) {
 	    activityToUpdateOnPlayCompletion.refreshList();
 	}
     }
     
     public PodcastFeedEntry getCurrentlyPlayingPodcast() {
 	if (currentlyPlayingPodcast == null)
 	    return null;
 	
 	return (PodcastFeedEntry)fluPodcastsFeed.items.get(currentlyPlayingPodcast);
     }
 
     public void onBufferingUpdate(MediaPlayer player, int percent) {
 	Toast.makeText(this, "Buffering complete: " + percent + "%", Toast.LENGTH_SHORT);
     }
 }
