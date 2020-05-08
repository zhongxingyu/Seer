 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 
 import java.util.*;
 
 /**
  * The Timer class provides facilities for defining named performance
  * characters and using them to measure interesting intervals.  Each
  * timer can be used to measure a series of intervals and extract
  * information such as average, minimum, and maximum length of the
  * intervals.  Although anonymous timers can be created  with
  * is{@code new Timer()}, the most common usage is to use named
  * timers created with {@code Timer.getNamedTimer}.
  */
 public class Timer {
     // Keeps track of all of the named timers, so we can reset all of them,
     // generate statistics, etc.
     protected static HashMap<String,Timer> namedTimers =
             new HashMap<String,Timer>();
 
     // System.nanoTime the last time {@code start} was invoked.
     protected long startTime = 0;
 
     // Total number of intervals that have completed so far.
     protected int numIntervals = 0;
 
     // Minimum and maximum intervals seen so far (same units as
     // System.nanoTime).
     protected long minInterval, maxInterval;
 
     // Total time spent in intervals for this timer, in nanoseconds.
     protected double totalNs;
 
     // Sum over all intervals of the interval length (in nanoseconds)
     // squared.  Used to compute standard deviations.
     protected double totalSquaredNs;
 
     /**
      * Begin timing a new interval.
      */
     public void start() {
         startTime = System.nanoTime();
     }
 
     /**
      * Begin timing a new interval, with the start time specified explicitly.
      * @param time                 Use this as the starting time for the
      *                             interval (measured in nanoseconds using
      *                             the same scale as System.nanoTime).
      */
     public void start(long time) {
         startTime = time;
     }
 
     /**
      * End the current interval.
      */
     public void stop() {
         stop(System.nanoTime());
     }
 
     /**
      * End the current interval, using an explicitly specify ending time.
      * @param time                 Use this as the ending time for the
      *                             interval (measured in nanoseconds using
      *                             the same scale as System.nanoTime).
      */
     public void stop(long time) {
         long interval = time - startTime;
         if (numIntervals == 0) {
             minInterval = maxInterval = interval;
             totalNs = interval;
             totalSquaredNs = totalNs*totalNs;
         } else {
             if (interval < minInterval) {
                 minInterval = interval;
             }
             if (interval > maxInterval) {
                 maxInterval = interval;
             }
             double d = interval;
             totalNs += d;
             totalSquaredNs += d*d;
         }
        numIntervals ++;
     }
 
     /**
      * Clear all of the internal statistics recorded by the timer so far.
      */
     public void reset() {
         numIntervals = 0;
     }
 
     /**
      * Return the average interval length for this timer.
      * @return                     Average length of all intervals recorded
      *                             since the last time this timer was reset,
      *                             in nanoseconds.
      */
     public double getAverage() {
         return totalNs/numIntervals;
     }
 
     /**
      * Return the total number of intervals recorded for this timer.
      * @return                     The number of times {@code stop} has been
      *                             invoked for this timer since the last
      *                             time it was reset.
      */
     public int getCount() {
         return numIntervals;
     }
 
     /**
      * Return the length of the longest interval for this timer.
      * @return                     Length in nanoseconds of the longest
      *                             interval recorded since the last time
      *                             this timer was reset.
      */
     public long getLongestInterval() {
         return maxInterval;
     }
 
     /**
      * Return the length of the shortest interval for this timer.
      * @return                     Length in nanoseconds of the shortest
      *                             interval recorded since the last time
      *                             this timer was reset.
      */
     public long getShortestInterval() {
         return minInterval;
     }
 
     /**
      * Return the standard deviation of all the intervals so far for
      * this timer.
      * @return                     Standard deviation of the intervals,
      *                             in nanoseconds.
      */
     public double getStdDeviation() {
         double average = totalNs/numIntervals;
         return Math.sqrt(totalSquaredNs/numIntervals
                 - average*average);
     }
 
     /**
      * This method provides the normal mechanism for creating a new timer.
      * All timers created via this method are entered into an internal table,
      * which can be used to manipulate the timers later.
      * @param name                 Name for the timer; will be used to
      *                             identify the timer when statistics are
      *                             generated.
      * @return                     A Timer object: if this is the first call
      *                             to this method with {@code name} then
      *                             the result is a new Timer object.  Otherwise
      *                             it is the same Timer object returned in the
      *                             previous call with the same {@code name}.
      */
     public static synchronized Timer getNamedTimer(String name) {
         Timer timer = namedTimers.get(name);
         if (timer != null) {
             return timer;
         }
         timer = new Timer();
         namedTimers.put(name, timer);
         return timer;
     }
 
     /**
      * Reset all of the timers created by getNamedTimer.
      */
     public static synchronized void resetAll() {
         for (Timer timer: namedTimers.values()) {
             timer.reset();
         }
     }
 
     /**
      * Generates an array of datasets containing usage statistics for
      * all of the named timers.
      * @param scale                Each of the time values in the result
      *                             dataset is divided by this number;
      *                             1.0 means times are measured in nanoseconds,
      *                             1000 means times are measured in
      *                             microseconds, and so on.
      * @param format               Used to convert {@code double} time values
      *                             (after scaling) to strings for inclusion
      *                             in the result data set.  Example: "%.2fus".
      * @return                     The return value is a collection containing
      * one Dataset for each Timer created by getNamedTimer, except that a Timer
      * is skipped if its {@code stop} method has never been invoked.  Each
      * dataset contains the following elements:
      *   average:                  Average interval length for this timer.
      *   intervals:                Number of times {@code start} and
      *                             @code stop} have been invoked for this
      *                             timer.
      *   maximum:                  Length of the longest interval recorded
      *                             for this timer.
      *   minimum:                  Length of the shortest interval recorded
      *                             for this timer.
      *   name:                     Name passed to getNamedTimer when the
      *                             timer was created.
      *   standardDeviation:        The standard deviation of the interval
      *                             length for this timer.
      */
     public static synchronized ArrayList<Dataset> getStatistics(double scale,
             String format) {
         ArrayList<Dataset> result = new ArrayList<Dataset>();
         for (String name : namedTimers.keySet()) {
             Timer timer = namedTimers.get(name);
             if (timer.numIntervals == 0) {
                 continue;
             }
             double average = timer.totalNs/timer.numIntervals;
             double deviation = Math.sqrt(
                     timer.totalSquaredNs/timer.numIntervals
                     - average*average);
             result.add(new Dataset("name", name,
                     "intervals", Integer.toString(timer.numIntervals),
                     "average", String.format(format, average/scale),
                     "minimum", String.format(format, timer.minInterval/scale),
                     "maximum", String.format(format, timer.maxInterval/scale),
                     "standardDeviation", String.format(format,
                     deviation/scale)));
 
         }
         return result;
     }
 
     /**
      * Forget about all of the existing named timers; clear the internal
      * table so that future calls to Timer.getNamedTimer will create new
      * timers.  This method is used primarily during testing.
      */
     public static synchronized void forgetNamedTimers() {
         namedTimers.clear();
     }
 
     /**
      * Create a timer named "noop" and accumulate statistics in it
      * about the overhead for the timer start and stop methods (i.e.,
      * measure the elapsed time for a timer start followed immediately
      * by a timer stop).
      */
     public static void measureNoopTime() {
         Timer timer = getNamedTimer("noop");
         for (int i = 0; i <10; i++) {
             timer.start(); timer.stop();
         }
     }
 }
