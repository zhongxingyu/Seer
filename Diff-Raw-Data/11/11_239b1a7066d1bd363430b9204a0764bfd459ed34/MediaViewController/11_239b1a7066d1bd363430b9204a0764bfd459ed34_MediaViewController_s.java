 package com.ouchadam.fang.presentation.controller;
 
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ViewSwitcher;
 
 import com.novoda.notils.android.Views;
 import com.ouchadam.fang.R;
 import com.ouchadam.fang.domain.FullItem;
 
 class MediaViewController {
 
     private final ImageButton playTop;
     private final ImageButton playBottom;
     private final ImageButton pauseTop;
     private final ImageButton pauseBottom;
     private final ViewSwitcher topMediaSwitcher;
     private final ViewSwitcher bottomMediaSwitcher;
 
     MediaViewController(ImageButton playTop, ImageButton playBottom, ImageButton pauseTop, ImageButton pauseBottom, ViewSwitcher topMediaSwitcher, ViewSwitcher bottomMediaSwitcher) {
         this.playTop = playTop;
         this.playBottom = playBottom;
         this.pauseTop = pauseTop;
         this.pauseBottom = pauseBottom;
         this.topMediaSwitcher = topMediaSwitcher;
         this.bottomMediaSwitcher = bottomMediaSwitcher;
     }
 
     public void showPlay() {
         if (isInPauseState()) {
             showNext();
         }
     }
 
     public void showPause() {
         if (isInPlayState()) {
             showNext();
         }
     }
 
     private boolean isInPlayState() {
         return topMediaSwitcher.getCurrentView().getId() == R.id.play_top;
     }
 
 
     private boolean isInPauseState() {
         return topMediaSwitcher.getCurrentView().getId() == R.id.pause_top;
     }
 
    public void setMediaVisibility(FullItem fullItem) {
        topMediaSwitcher.setVisibility(fullItem.isDownloaded() ? View.VISIBLE : View.INVISIBLE);
         bottomMediaSwitcher.setVisibility(fullItem.isDownloaded() ? View.VISIBLE : View.INVISIBLE);
     }
 
     public void setPlayPauseListeners(View.OnClickListener onPlayClicked, View.OnClickListener onPauseClicked) {
         playTop.setOnClickListener(onPlayClicked);
         playBottom.setOnClickListener(onPlayClicked);
         pauseTop.setOnClickListener(onPauseClicked);
         pauseBottom.setOnClickListener(onPauseClicked);
     }
 
     public void showNext() {
         topMediaSwitcher.showNext();
         bottomMediaSwitcher.showNext();
     }
 
     public void showExpanded(boolean downloaded) {
         topMediaSwitcher.setVisibility(View.INVISIBLE);
         if (downloaded) {
             bottomMediaSwitcher.setVisibility(View.VISIBLE);
         } else {
             bottomMediaSwitcher.setVisibility(View.INVISIBLE);
         }
     }
 
     public void showCollapsed(boolean downloaded) {
         if (downloaded) {
             topMediaSwitcher.setVisibility(View.VISIBLE);
         } else {
             topMediaSwitcher.setVisibility(View.INVISIBLE);
         }
     }
 }
