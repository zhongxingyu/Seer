 package dk.kleistsvendsen;
 
 import android.os.Bundle;
 
 public interface IGameTimer {
     public long timePlayed();
 
     public void pauseTimer();
 
     public void startTimer();
 
     public void resetTimer();
 
     public boolean isRunning();
 
     public void saveState(Bundle outState);
 
     public void restoreState(Bundle inState);
 }
