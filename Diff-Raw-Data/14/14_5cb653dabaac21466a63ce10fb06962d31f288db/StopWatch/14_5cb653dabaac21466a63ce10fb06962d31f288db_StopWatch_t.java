 /**
  * Copyright 2007 Jason Pelzer.
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
 package com.pelzer.util;
 
 import java.util.Date;
 
 /**
  * This is a quick utility to aid in getting timing info for debugging/etc. Basic usage is:<br>
  * <code>
  *   StopWatch watch = new StopWatch();
  *   watch.start(); // Calls an implicit reset();
  *   sleep(1000);
  *   System.out.println(watch.getElapsed().toString); // Should be something like "00:00:01:00";
  *   sleep(2000);
  *   watch.stop();
  *   sleep(2000);
  *   System.out.println(watch.getElapsed().toString); // Should be something like "00:00:03:00";
  * </code>
  */
 public class StopWatch {
   private long start = 0;
   private long stop = 0;
   private boolean running = false;
 
   /** Creates a new stopwatch. You must call {@link #start()} to begin timing.*/
   public StopWatch() {
    this(false);
  }

  /** Creates a new stopwatch and starts it. */
  public StopWatch(boolean running){
    if(running)
      start();
    else
      reset();
   }
 
   /** Resets elapsed time to zero and stops the clock ticking. */
   public void reset() {
     start = 0;
     stop = 0;
     running = false;
   }
 
   /** If the clock is not ticking, resets the elapsed time {@link #reset()} and then starts the clock. */
   public void start() {
     if (isRunning())
       return;
     reset();
     start = new Date().getTime();
     running = true;
   }
 
   /** Stops the clock if it is running. */
   public void stop() {
     if (!isRunning())
       return;
     stop = new Date().getTime();
     running = false;
   }
 
   /** Can be called at any time, returns a SMPTE object with 1000 frames/sec (corresponding to milliseconds) 
    * representing the time elapsed since start() is called. If start has not been called, returns 0. If
    * stop has not been called, returns the current elapsed time. Once stop is called, the elapsed time no
    * longer increments, and is frozen. */
   public Timecode getElapsed() {
     if (isRunning())
       stop = new Date().getTime();
     long durationMillis = stop - start;
     if (start == 0)
       durationMillis = 0;
     Timecode elapsed = new Timecode(Timecode.Type.TYPE_AUDIO_CD);
     elapsed.setFramesPerSecond(1000);
     elapsed.setFrames((int) durationMillis);
     elapsed.normalize();
     return elapsed;
   }
 
   /** @returns true if the clock is running. */
   public boolean isRunning() {
     return running;
   }
 
}
