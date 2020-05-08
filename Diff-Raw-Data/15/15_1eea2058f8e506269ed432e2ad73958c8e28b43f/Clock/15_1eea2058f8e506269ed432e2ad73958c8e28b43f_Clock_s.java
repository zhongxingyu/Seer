 package com.icbat.game.tradesong;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Timer;
 import com.icbat.game.tradesong.observation.NotificationManager;
 import com.icbat.game.tradesong.observation.notifications.DayEndedNotification;
 import com.icbat.game.tradesong.observation.watchers.EndOfDayWatcher;
 
 /**
  * @author icbat
  */
 public class Clock extends NotificationManager {
     /** Length of a day in seconds. Currently set to 12 minutes.
      * Expecting to want to refactor this out at some point */
     private static final int dayLengthInMinutes = 5;
    public static final int SECONDS_TO_MINUTE = 1;
     /** Current time in minutes. Initialized to 0 every time you startDay */
     private int currentTime = 0;
     private final Timer dayTimer = new Timer();
 
     public Clock() {
         this.addWatcher(new EndOfDayWatcher());
     }
 
     public void startDay() {
         Gdx.app.debug("clock","day started!");
         currentTime = 0;
         dayTimer.scheduleTask(new dayEndTask(), dayLengthInMinutes * SECONDS_TO_MINUTE);
         dayTimer.scheduleTask(new countUpTask(), 0, SECONDS_TO_MINUTE, dayLengthInMinutes * SECONDS_TO_MINUTE);
         Tradesong.contractGenerator.makeDailyContracts();
     }
 
     public int getTime() {
         return currentTime;
     }
 
     private class dayEndTask extends Timer.Task {
 
         @Override
         public void run() {
             Gdx.app.debug("clock says", "day ended!");
             notifyWatchers(new DayEndedNotification());
         }
     }
 
     private class countUpTask extends Timer.Task {
 
         @Override
         public void run() {
             currentTime += 1;
 
         }
     }
 }
