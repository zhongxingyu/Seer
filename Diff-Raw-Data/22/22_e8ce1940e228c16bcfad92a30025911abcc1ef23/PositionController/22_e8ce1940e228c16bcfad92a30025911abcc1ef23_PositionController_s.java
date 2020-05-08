 package com.ouchadam.fang.presentation.panel;
 
 import android.view.View;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 import com.ouchadam.fang.domain.PodcastPosition;
 
 import java.util.concurrent.TimeUnit;
 
 class PositionController {
 
     private final SeekBar seekBar;
     private final TextView currentTime;
     private final TextView endTime;
 
     PositionController(SeekBar seekBar, TextView currentTime, TextView endTime) {
         this.seekBar = seekBar;
         this.currentTime = currentTime;
         this.endTime = endTime;
     }
 
     public void update(boolean positionChanging, PodcastPosition position) {
         seekBar.setMax(position.getDuration());
         if (!positionChanging) {
             seekBar.setProgress(position.value());
         }
         currentTime.setText(toTimeString(position.value()));
         endTime.setText(toTimeString(position.getDuration()));
     }
 
     private String toTimeString(long millis) {
         return String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) -
                 TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
     }
 
     public void setSeekChangedListener(SeekBar.OnSeekBarChangeListener seekListener) {
         seekBar.setOnSeekBarChangeListener(seekListener);
     }
 
     public void setInitialPosition(PodcastPosition position) {
         seekBar.setProgress(position.value());
         seekBar.setMax(position.getDuration());
         currentTime.setText(toTimeString(position.value()));
     }
 
     public void panelScopeChange(boolean downloaded) {
         setBooleanVisibility(seekBar, downloaded);
         setBooleanVisibility(currentTime, downloaded);
         setBooleanVisibility(endTime, downloaded);
     }
 
     private void setBooleanVisibility(View view, boolean visible) {
         view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
     }
 
 }
