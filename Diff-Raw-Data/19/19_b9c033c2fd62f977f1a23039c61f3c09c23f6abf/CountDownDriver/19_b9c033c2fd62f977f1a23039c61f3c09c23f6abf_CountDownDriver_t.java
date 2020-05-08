 /**
  * Copyright 2010 Sean L. Mooney
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  *
  */
 package com.mooney_ware.gio.model;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.os.Handler;
 import android.text.format.Time;
 
 /**
  * A class to run the countdown ticking. Essentially, this is the main spring of
  * a pocket watch.
  * 
  * @author Sean Mooney
  * 
  */
 public class CountDownDriver {
 
     /**
      * Store the target date, so we can always compute how many much longer
      * there is until deadline.
      */
     private Time mTarget;
 
     private final Handler mTickHandler = new Handler();
     private final Runnable mTickRunner = new Runnable() {
         @Override
         public void run() {
             tick();
         }
     };
 
     /**
      * An object to synchronize one when scheduling the mTickRunner();
      */
     private final Object tickLock = new Object();
 
     /**
      * Even though single accesses are atomic, multiple accesses are not.
      * Synchronize a if read/flip state type access with the {@link #tickLock};
      */
     private AtomicBoolean mIsTicking = new AtomicBoolean(false);
 
     /**
      * The observer to display the countdown state. If the need arises, this
      * could always be a list of observers.
      */
     private CountdownListener mCountdownListener = null;
 
     public CountDownDriver(Time target) {
         mTarget = target;
 
         start();
     }
 
     /**
      * Start the system ticking.
      */
     public void start() {
         synchronized (tickLock) {
             if (!mIsTicking.get()) {
                 mIsTicking.set(true);
                 mTickHandler.post(mTickRunner);
             }
         }
     }
 
     /**
      * Stop the system from ticking.
      */
     public void stop() {
         synchronized (tickLock) {
             if (mIsTicking.get()) {
                 mIsTicking.set(false);
                 mTickHandler.removeCallbacks(mTickRunner);
             }
         }
     }
 
     void tick() {
         // FIXME Timezone issues.
         Time now = new Time();
         now.setToNow();
 
         // pull a local reference.
         Time tgt = mTarget;
 
         if (now.after(tgt)) {
             // We're after the target. Count down is done.
             notifyListeners(0, 0, 0, 0);
         } else {
             int diffDays, diffHours, diffMinutes, diffSeconds;
 
             diffDays = tgt.yearDay - now.yearDay;
             diffHours = tgt.hour - now.hour;
             diffMinutes = tgt.minute - now.minute;
             diffSeconds = tgt.second - now.second;
 
             int[] normDiff = normalizeDifference(diffDays, diffHours,
                     diffMinutes, diffSeconds);
 
             notifyListeners(normDiff[0], normDiff[1], normDiff[2], normDiff[3]);
 
             synchronized (tickLock) {
                 if (mIsTicking.get()) {
                     // TODO: Tick on exact seconds. This could be off
                     // by almost a second.
                     mTickHandler.postDelayed(mTickRunner, 1000);
                 }
             }
         }
     }
 
     /**
      * Normalize the difference represented by the fields to not have any
      * negative numbers.
      * 
      * @param days
      * @param hours
      * @param minutes
      * @param seconds
      * @return an array which is ordered by days, hours, minutes, seconds
      */
     // TODO TEST THIS
     private int[] normalizeDifference(int days, int hours, int minutes,
             int seconds) {
         int[] normalValues = new int[4];
 
        final int SEC_PER_MIN = 60;
        final int MIN_PER_HOUR = 60;
        final int HOUR_PER_DAY = 24;

        if (seconds < 0) {
            seconds += SEC_PER_MIN;
            minutes--;
        }

        if (minutes < 0) {
            minutes += MIN_PER_HOUR;
            hours--;
        }

        if (hours < 0) {
            hours += HOUR_PER_DAY;
            days--;
        }

         // FIXME:
         normalValues[0] = days;
         normalValues[1] = hours;
         normalValues[2] = minutes;
         normalValues[3] = seconds;
 
         return normalValues;
     }
 
     /**
      * Register a listener for notification on tick.
      * 
      * This will remove the previously registered listener from notification, if
      * one was set.
      * 
      * @param listener
      */
     public void registerListener(CountdownListener listener) {
         this.mCountdownListener = listener;
     }
 
     void notifyListeners(int days, int hours, int minutes, int seconds){
         if(mCountdownListener != null){
             mCountdownListener.onTick(days, hours, minutes, seconds);
         }
     }
 }
