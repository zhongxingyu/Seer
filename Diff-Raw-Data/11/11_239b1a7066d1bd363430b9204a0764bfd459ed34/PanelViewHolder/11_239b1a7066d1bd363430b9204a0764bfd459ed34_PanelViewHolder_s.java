 package com.ouchadam.fang.presentation.controller;
 
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 import com.novoda.notils.android.Views;
 import com.ouchadam.fang.R;
 import com.ouchadam.fang.domain.FullItem;
 import com.ouchadam.fang.domain.item.Item;
 import com.ouchadam.fang.view.SlidingUpPanelLayout;
 
 class PanelViewHolder {
 
     private final PositionController positionController;
     private final MediaViewController mediaController;
     private final DownloadController downloadController;
     private final MainPanelController mainPanelController;
 
     public static PanelViewHolder from(SlidingUpPanelLayout panel) {
         ViewSwitcher topMediaSwitcher = Views.findById(panel, R.id.media_switcher);
         ViewSwitcher bottomMediaSwitcher = Views.findById(panel, R.id.bottom_media_switcher);
 
         ImageButton playTop = Views.findById(panel, R.id.play_top);
         ImageButton playBottom = Views.findById(panel, R.id.play_bottom);
         ImageButton pauseTop = Views.findById(panel, R.id.pause_top);
         ImageButton pauseBottom = Views.findById(panel, R.id.pause_bottom);
 
         MediaViewController mediaController = new MediaViewController(playTop, playBottom, pauseTop, pauseBottom, topMediaSwitcher, bottomMediaSwitcher);
 
         Button downloadButton = Views.findById(panel, R.id.download);
         DownloadController downloadController = new DownloadController(downloadButton);
 
         SeekBar seekBar = Views.findById(panel, R.id.seek_bar);
         TextView currentTime = Views.findById(panel, R.id.current_position);
         TextView endTime = Views.findById(panel, R.id.total_time);
         PositionController position = new PositionController(seekBar, currentTime, endTime);
 
         MainPanelController mainPanelController = new MainPanelController(panel);
 
         return new PanelViewHolder(position, mediaController, downloadController, mainPanelController);
     }
 
     PanelViewHolder(PositionController positionController, MediaViewController mediaController, DownloadController downloadController, MainPanelController mainPanelController) {
         this.mediaController = mediaController;
         this.positionController = positionController;
         this.downloadController = downloadController;
         this.mainPanelController = mainPanelController;
     }
 
     public MediaViewController mediaController() {
         return mediaController;
     }
 
     public void showCollapsed(boolean isDownloaded) {
         mediaController.showCollapsed(isDownloaded);
         downloadController.panelScopeChange(isDownloaded);
     }
 
     public void showExpanded(boolean downloaded) {
         mediaController.showExpanded(downloaded);
         positionController.panelScopeChange(downloaded);
         downloadController.panelScopeChange(downloaded);
     }
 
     public DownloadController downloadController() {
         return downloadController;
     }
 
     public PositionController positionController() {
         return positionController;
     }
 
     public void updatePlayingState(boolean playing) {
         if (playing) {
             mediaController.showPause();
         } else {
             mediaController.showPlay();
         }
     }
 
     public void updatePanel(FullItem fullItem) {
         mainPanelController.updateFrom(fullItem);
        mediaController.setMediaVisibility(fullItem);
         positionController.setInitialPosition(fullItem.getInitialPlayPosition());
     }
 
     public void close() {
         mainPanelController.close();
     }
 
     public void expand() {
         mainPanelController.expand();
     }
 
     public boolean isShowing() {
         return mainPanelController.isShowing();
     }
 
     public void setPanelSlideListener(SlidingUpPanelLayout.PanelSlideListener panelSlideListener) {
         mainPanelController.setPanelSlideListener(panelSlideListener);
     }
 
     private static class MainPanelController {
 
         private final SlidingUpPanelLayout panelLayout;
 
         private MainPanelController(SlidingUpPanelLayout panelLayout) {
             this.panelLayout = panelLayout;
         }
 
         public void updateFrom(FullItem fullItem) {
             Item item = fullItem.getItem();
             setBarTitle(item.getTitle());
             setDescription(item.getSummary());
             setBarSubtitle(fullItem.getChannelTitle());
         }
 
         private void setBarTitle(CharSequence text) {
             setTextViewText(text, R.id.bar_title);
         }
 
         private void setBarSubtitle(CharSequence text) {
             setTextViewText(text, R.id.bar_sub_title);
         }
 
         private void setDescription(CharSequence summary) {
             setTextViewText(summary, R.id.item_description);
         }
 
         private void setTextViewText(CharSequence text, int viewId) {
             TextView textView = Views.findById(panelLayout, viewId);
             textView.setText(text);
         }
 
         public void close() {
             panelLayout.collapsePane();
         }
 
         public boolean isShowing() {
             return panelLayout.isExpanded();
         }
 
         public void expand() {
             panelLayout.expandPane();
         }
 
         public void setPanelSlideListener(SlidingUpPanelLayout.PanelSlideListener panelSlideListener) {
             panelLayout.setPanelSlideListener(panelSlideListener);
         }
     }
 
 }
