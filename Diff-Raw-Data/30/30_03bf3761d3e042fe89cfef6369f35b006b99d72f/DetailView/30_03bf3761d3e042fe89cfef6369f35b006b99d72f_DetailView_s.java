 package com.qvdev.apps.twitflick.View;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 import com.google.android.youtube.player.YouTubePlayerSupportFragment;
 import com.google.android.youtube.player.YouTubeThumbnailView;
 import com.qvdev.apps.twitflick.DeveloperKey;
 import com.qvdev.apps.twitflick.Presenter.DetailPresenter;
 import com.qvdev.apps.twitflick.R;
 import com.qvdev.apps.twitflick.api.models.BuzzingDetail;
 
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * Created by dirkwilmer on 7/29/13.
  */
 
 public class DetailView extends YouTubePlayerSupportFragment implements Observer, View.OnClickListener {
     private DetailPresenter mDetailPresenter;
 
     private TextView mTitle;
     private TextView mSummary;
     private RatingBar mRating;
     private YouTubeThumbnailView mVideoThumbnail;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRetainInstance(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         return inflater.inflate(R.layout.fragment_main_detail, container, false);
     }
 
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         if (mDetailPresenter == null) {
             mDetailPresenter = new DetailPresenter(this);
             initLayout();
         } else {
             initLayout();
             mDetailPresenter.resumed();
         }
     }
 
     private void initLayout() {
         mTitle = (TextView) getActivity().findViewById(R.id.detail_title);
         mSummary = (TextView) getActivity().findViewById(R.id.detail_summary);
         mRating = (RatingBar) getActivity().findViewById(R.id.detail_rating);
 
         Drawable thumb = null;
         if (mVideoThumbnail != null) {
             thumb = mVideoThumbnail.getDrawable();
         }
 
         mVideoThumbnail = (YouTubeThumbnailView) getActivity().findViewById(R.id.video_thumbnail);
         mVideoThumbnail.setOnClickListener(this);
        mVideoThumbnail.initialize(DeveloperKey.DEVELOPER_KEY, mDetailPresenter);
 
         if (thumb != null) {
             mVideoThumbnail.setImageDrawable(thumb);
         }
     }
 
     public void setMovieInfo(BuzzingDetail buzzingDetail) {
         mTitle.setText(buzzingDetail.getMovie().getName());
         mSummary.setText(buzzingDetail.getMovie().getShortSynposis());
         mRating.setRating(buzzingDetail.getMovie().getRating());
     }
 
     private void trailerClicked() {
         mDetailPresenter.trailerClicked();
     }
 
     @Override
     public void update(Observable observable, Object buzzingDetails) {
         mDetailPresenter.update((BuzzingDetail) buzzingDetails);
     }
 
     @Override
     public void onClick(View view) {
         switch (view.getId()) {
             case R.id.video_thumbnail:
                 trailerClicked();
                 break;
             default:
                 break;
         }
     }
 }
