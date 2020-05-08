 package com.github.super8.behavior;
 
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 
 import com.github.super8.db.LibraryManager;
 import com.google.inject.Inject;
 
 public class HomeScreenPresenter implements Presenter<ActsAsHomeScreen>, OnDrawerCloseListener {
 
   public enum State {
     WATCHLIST_EMPTY, WATCHLIST_AVAILABLE, RECORD, PLAY;
   }
 
   private ActsAsHomeScreen homeScreen;
   private LibraryManager library;
   private State state = State.WATCHLIST_EMPTY;
 
   @Inject
   public HomeScreenPresenter(LibraryManager library) {
     this.library = library;
   }
 
   @Override
   public void bind(ActsAsHomeScreen behavior) {
     this.homeScreen = behavior;
 
     powerOff();
   }
 
   public void powerOff() {
     homeScreen.hideSlidingDrawer();
     homeScreen.disableControlPanel();
     if (library.hasWatchlistItems()) {
       homeScreen.showWatchlistView();
       state = State.WATCHLIST_AVAILABLE;
     } else {
       homeScreen.showWatchlistEmptyView();
       state = State.WATCHLIST_EMPTY;
     }
   }
 
   public void powerOn() {
     homeScreen.showSlidingDrawer();
     homeScreen.enableControlPanel();
     if (library.hasSuggestions()) {
       enterPlaybackMode();
     } else {
       enterRecordingMode();
     }
   }
 
   public State getState() {
     return state;
   }
 
   public void enterRecordingMode() {
     homeScreen.showRecordView();
     state = State.RECORD;
   }
 
   public void enterPlaybackMode() {
     homeScreen.showPlayView();
     homeScreen.loadMovieSuggestion();
     state = State.PLAY;
   }
 
   public void onMovieSuggestionAvailable() {
     homeScreen.showSlidingDrawer();
     // homeScreen.openSlidingDrawer();
   }
 
   @Override
   public void onDrawerClosed() {
    if (state == State.PLAY) {
      homeScreen.loadMovieSuggestion();
    } else if (state == State.RECORD && library.hasSuggestions()) {
      enterPlaybackMode();
     }
   }
 }
