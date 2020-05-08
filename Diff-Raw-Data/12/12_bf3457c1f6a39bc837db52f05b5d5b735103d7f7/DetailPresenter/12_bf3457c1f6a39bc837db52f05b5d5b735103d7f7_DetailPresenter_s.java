 package com.qvdev.apps.twitflick.Presenter;
 
 import android.widget.Toast;
 
 import com.google.android.youtube.player.YouTubeInitializationResult;
 import com.google.android.youtube.player.YouTubePlayer;
 import com.google.android.youtube.player.YouTubePlayerSupportFragment;
 import com.qvdev.apps.twitflick.DeveloperKey;
 import com.qvdev.apps.twitflick.R;
 import com.qvdev.apps.twitflick.View.DetailView;
 import com.qvdev.apps.twitflick.api.models.BuzzingDetail;
 
 /**
  * Created by dirkwilmer on 7/29/13.
  */
 public class DetailPresenter implements YouTubePlayer.OnInitializedListener {
 
     private DetailView mDetailView;
     private YouTubePlayer mYoutubePlayer;
 
     public DetailPresenter(DetailView detailView) {
         mDetailView = detailView;
     }
 
 
     private String getVideoId(String trailerUrl) {
         String youtubeUrl = trailerUrl;
         int startIndex = youtubeUrl.lastIndexOf("=") + 1;
         String youtubeId = youtubeUrl.substring(startIndex, youtubeUrl.length());
         return youtubeId;
     }
 
     private void initYoutubeTrailerFragment() {
         if (mYoutubePlayer == null) {
             YouTubePlayerSupportFragment youTubePlayerFragment = (YouTubePlayerSupportFragment) mDetailView.getFragmentManager().findFragmentById(R.id.youtube_fragment);
             youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);
         }
     }
 
     @Override
     public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                         boolean wasRestored) {
         mYoutubePlayer = player;
         configureYoutubePlayer();
        playVideo("");
     }
 
     private void playVideo(String trailerId) {
 
         if (mYoutubePlayer == null) {
             initYoutubeTrailerFragment();
         } else {
            mYoutubePlayer.cueVideo(getVideoId(trailerId));
         }
     }
 
     private void configureYoutubePlayer() {
         mYoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
         mYoutubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION | YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
     }
 
     @Override
     public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
         Toast.makeText(mDetailView.getActivity(), "Failed to fetch video", Toast.LENGTH_LONG).show();
     }
 
     public void update(BuzzingDetail buzzingDetails) {
         mDetailView.setMovieInfo(buzzingDetails);
         playVideo(buzzingDetails.getMovie().getTrailer());
     }
 }
