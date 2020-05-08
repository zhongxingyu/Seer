 package dk.kleistsvendsen;
 
 import com.google.inject.Inject;
 
 public class GameTimer implements IGameTimer {
     private ITicSource ticSource_;
     private long startTic_;
     private long pauseTic_;
 
     private enum State {
         IDLE,
         PAUSED,
         RUNNING
     }
 
     private State running_;
 
     @Inject
     public GameTimer(ITicSource ticSource) {
        running_ = State.IDLE;
         ticSource_ = ticSource;
        startTic_ = 0;
        pauseTic_ = 0;
     }
 
     public long tic() {
         return ticSource_.tic();
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

     }
 }
