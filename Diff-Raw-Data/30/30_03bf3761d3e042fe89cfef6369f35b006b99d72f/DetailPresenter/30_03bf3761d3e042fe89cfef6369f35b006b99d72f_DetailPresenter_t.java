 package com.qvdev.apps.twitflick.Presenter;
 
 import android.content.Intent;
 import android.util.Log;
 
 import com.google.android.youtube.player.YouTubeInitializationResult;
 import com.google.android.youtube.player.YouTubeStandalonePlayer;
 import com.google.android.youtube.player.YouTubeThumbnailLoader;
 import com.google.android.youtube.player.YouTubeThumbnailView;
 import com.qvdev.apps.twitflick.DeveloperKey;
 import com.qvdev.apps.twitflick.View.DetailView;
 import com.qvdev.apps.twitflick.api.models.BuzzingDetail;
 
 /**
  * Created by dirkwilmer on 7/29/13.
  */
 public class DetailPresenter implements YouTubeThumbnailView.OnInitializedListener, YouTubeThumbnailLoader.OnThumbnailLoadedListener {
 
     private String mCurrentVideoId;
     private DetailView mDetailView;
     private YouTubeThumbnailLoader mThumbnailLoader;
     private BuzzingDetail mBuzzingDetails;
 
     public DetailPresenter(DetailView detailView) {
         mDetailView = detailView;
     }
 
 
     private String getVideoId(String trailerUrl) {
         int startIndex = trailerUrl.lastIndexOf("=") + 1;
         return trailerUrl.substring(startIndex, trailerUrl.length());
     }
 
 
     private void loadVideoThumbnail(String trailerId) {
         if (trailerId != null && mThumbnailLoader != null) {
             mCurrentVideoId = getVideoId(trailerId);
             try {
                 mThumbnailLoader.setVideo(mCurrentVideoId);
             } catch (IllegalStateException e) {
                 Log.d("App", "Youtube failure https://code.google.com/p/gdata-issues/issues/detail?id=5431" + e.getMessage());
             }
         }
     }
 
     public void update(BuzzingDetail buzzingDetails) {
         mBuzzingDetails = buzzingDetails;
 
         mDetailView.setMovieInfo(mBuzzingDetails);
         loadVideoThumbnail(mBuzzingDetails.getMovie().getTrailer());
     }
 
     @Override
     public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
         mThumbnailLoader = youTubeThumbnailLoader;
         mThumbnailLoader.setOnThumbnailLoadedListener(this);
     }
 
     @Override
     public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
     }
 
     @Override
     public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
     }
 
     @Override
     public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
         youTubeThumbnailView.setImageResource(android.R.drawable.ic_media_play);
     }
 
     public void trailerClicked() {
        Intent intent = YouTubeStandalonePlayer.createVideoIntent(mDetailView.getActivity(), DeveloperKey.DEVELOPER_KEY_YOUTUBE, mCurrentVideoId);
         mDetailView.startActivity(intent);
     }
 
     public void resumed() {
         if (mBuzzingDetails != null) {
             update(mBuzzingDetails);
         }
     }
 }
