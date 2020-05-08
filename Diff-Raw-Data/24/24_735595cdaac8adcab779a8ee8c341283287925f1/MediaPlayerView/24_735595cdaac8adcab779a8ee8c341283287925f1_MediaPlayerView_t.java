 package org.gnuton.newshub.view;
 
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
import android.widget.TextView;
 
 import org.gnuton.newshub.R;
 import org.gnuton.newshub.utils.FontsProvider;
 
 import java.io.IOException;
 
 /**
  * Created by gnuton on 11/19/13.
  */
 public class MediaPlayerView extends LinearLayout {
     private final String TAG = MediaPlayerView.class.getName();
     final LayoutInflater mLayoutInflate;
     private View mView;
 
     private final MediaPlayer mMediaPlayer = new MediaPlayer();
     private Button mPlayPauseButton;
    private TextView mInfoLabel;
 
     public MediaPlayerView(Context context) {
         super(context);
         mLayoutInflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         initialize();
     }
 
     public MediaPlayerView(Context context, AttributeSet attrs) {
         super(context, attrs);
         mLayoutInflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         initialize();
     }
     private void initialize(){
         if (mLayoutInflate != null) {
             // Inflate into this mView
             mView = this.mLayoutInflate.inflate(R.layout.mediaplayer, this, true);
             LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                     LayoutParams.MATCH_PARENT,
                     LayoutParams.MATCH_PARENT);
             this.setLayoutParams(lp);
 
            mInfoLabel = (TextView) findViewById(R.id.mediaPlayerTrackInfo);
             mPlayPauseButton = (Button)findViewById(R.id.playPauseButton);

             if (mPlayPauseButton != null){
                 mPlayPauseButton.setOnClickListener(new playPauseButtonClickListener());
                 mPlayPauseButton.setTypeface(FontsProvider.getInstace().getTypeface("fontawesome-webfont"));
             }
         }
     }
 
     public void setMedia(final String url){
        mMediaPlayer.stop();
        mPlayPauseButton.setText(R.string.icon_play);

         if (url == null){
             return;
         }
 
         try {
            mMediaPlayer.reset();
             mMediaPlayer.setDataSource(url);
             mMediaPlayer.prepare();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private class playPauseButtonClickListener implements OnClickListener {
         @Override
         public void onClick(View view) {
             if (mMediaPlayer.isPlaying()){
                 mMediaPlayer.pause();
                 mPlayPauseButton.setText(R.string.icon_play);
             } else {
                 mMediaPlayer.start();

                 mPlayPauseButton.setText(R.string.icon_pause);
             }
         }
     }
 }
