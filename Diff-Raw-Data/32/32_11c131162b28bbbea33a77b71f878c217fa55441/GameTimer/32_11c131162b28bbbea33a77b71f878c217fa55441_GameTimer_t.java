 package dk.kleistsvendsen;
 
 import android.os.Bundle;
 
 import com.google.inject.Inject;
 
 public class GameTimer implements IGameTimer {
     private ITicSource ticSource_;
     private long startTic_;
     private long pauseTic_;
     private static final String BUNDLE_IS_RUNNING = "gameTimer_isRunning";
     private static final String BUNDLE_IS_PAUSED = "gameTimer_isPaused";
     private static final String BUNDLE_PAUSE_TIC = "gameTimer_pauseTic";
     private static final String BUNDLE_START_TIC = "gameTimer_startTic";
     private enum State {
         IDLE,
         PAUSED,
         RUNNING
     }
 
     private State running_;
 
     @Inject
     public GameTimer(ITicSource ticSource) {
         ticSource_ = ticSource;
         resetTimer();
     }
 
     @Override
     public long timePlayed() {
         if (running_ == State.RUNNING) {
             return ticSource_.tic() - startTic_;
         } else {
             return pauseTic_ - startTic_;
         }
     }
 
     @Override
     public void pauseTimer() {
         if (running_ == State.RUNNING) {
             pauseTic_ = ticSource_.tic();
             running_ = State.PAUSED;
         }
     }
 
     @Override
     public void startTimer() {
         switch (running_) {
             case IDLE:
                 running_ = State.RUNNING;
                 startTic_ = ticSource_.tic();
                 break;
             case PAUSED:
                 startTic_ = ticSource_.tic() - timePlayed();
                 break;
             case RUNNING:
                 break;
         }
         running_ = State.RUNNING;
     }
 
     @Override
     public void resetTimer() {
         running_ = State.IDLE;
         startTic_ = 0;
         pauseTic_ = 0;
     }
 
     @Override
     public boolean isRunning() {
         return running_ == State.RUNNING;
     }
 
     @Override
     public void saveState(Bundle outState) {
         outState.putBoolean(BUNDLE_IS_RUNNING, running_ == State.RUNNING);
         outState.putBoolean(BUNDLE_IS_PAUSED, running_ == State.PAUSED);
         outState.putLong(BUNDLE_START_TIC, startTic_);
         outState.putLong(BUNDLE_PAUSE_TIC, pauseTic_);
     }
 
     @Override
     public void restoreState(Bundle inState) {
         if (inState.containsKey(BUNDLE_IS_RUNNING)) {
             if (inState.getBoolean(BUNDLE_IS_RUNNING)) {
                 running_ = State.RUNNING;
             }
             else if (inState.getBoolean(BUNDLE_IS_PAUSED)) {
                 running_ = State.PAUSED;
             }
             else {
                 running_ = State.IDLE;
             }
             startTic_ = inState.getLong(BUNDLE_START_TIC);
             pauseTic_ = inState.getLong(BUNDLE_PAUSE_TIC);
         }
     }
 }
