 package com.ciplogic.allelon.songname;
 
 import com.ciplogic.allelon.player.AvailableStream;
 
 public class CurrentSongNameProvider {
     private CurrentSongNameChangeListener currentSongNameChangeListener;
     private volatile String currentTitle;
     private CurrentSongFetcherThread currentSongFetcherThread;
 
     public void setUrl(String url) {
        currentTitle = "..."; // when the URL changes, we need to reload the title.
 
         if (currentSongFetcherThread != null) {
             currentSongFetcherThread.stop();
             currentSongFetcherThread = null;
         }
 
         if (url != null) {
             String titleUrl = AvailableStream.fromUrl(url).getTitleUrl();
             currentSongFetcherThread = new CurrentSongFetcherThread(this, titleUrl);
         }
     }
 
     public String getCurrentTitle() {
         return currentTitle;
     }
 
     public CurrentSongNameChangeListener getCurrentSongNameChangeListener() {
         return currentSongNameChangeListener;
     }
 
     public void setCurrentSongNameChangeListener(CurrentSongNameChangeListener currentSongNameChangeListener) {
         this.currentSongNameChangeListener = currentSongNameChangeListener;
     }
 
     public void setFetchedTitle(String title) {
         String oldTitle = currentTitle;
         currentTitle = title;
 
         if (!areStringsEquals(oldTitle, currentTitle)) {
             if (currentSongNameChangeListener != null) {
                 currentSongNameChangeListener.onTitleChange(currentTitle);
             }
         }
     }
 
     private boolean areStringsEquals(String oldTitle, String currentTitle1) {
         if (oldTitle == null) {
             return currentTitle1 == null;
         }
 
         return oldTitle.equals(currentTitle1);
     }
 }
