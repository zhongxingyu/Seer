 package com.galineer.suzy.accountsim;
 
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 
 class ScheduledEvent implements Comparable<ScheduledEvent> {
   private final AccountEvent event;
   private final DateTime lastTime;
   private final Period frequency;
 
   private DateTime nextTime;
   private double tiebreaker;
 
   ScheduledEvent(AccountEvent event, DateTime firstTime,
                  DateTime lastTime, Period frequency)
       throws IllegalArgumentException, ArithmeticException {
 
     this.checkEvent(event);
     this.checkFirstTime(firstTime);
     this.checkFrequency(firstTime, frequency);
     this.checkLastTime(firstTime, lastTime);
 
     this.event = event;
     this.nextTime = firstTime;
     this.lastTime = lastTime;
     this.frequency = frequency;
     this.tiebreaker = Math.random();
   }
 
   ScheduledEvent(AccountEvent event, DateTime firstTime,
                  Period frequency, int numRepeats)
       throws IllegalArgumentException, ArithmeticException {
 
     this.checkEvent(event);
     this.checkFirstTime(firstTime);
     this.checkFrequency(firstTime, frequency);
     this.checkNumRepeats(numRepeats, frequency);
     DateTime lastTime = this.calculateLastTime(
         firstTime, frequency, numRepeats);
     this.checkLastTime(firstTime, lastTime);
 
     this.event = event;
     this.nextTime = firstTime;
     this.lastTime = lastTime;
     this.frequency = frequency;
     this.tiebreaker = Math.random();
   }
 
   private void checkEvent(AccountEvent event)
       throws IllegalArgumentException {
 
     if (event == null) {
       throw new IllegalArgumentException("Attempted to schedule null event");
     }
   }
 
   private void checkFirstTime(DateTime firstTime)
       throws IllegalArgumentException {
 
     if (firstTime == null) {
       throw new IllegalArgumentException(
           "First time for ScheduledEvent must not be null");
     }
   }
 
   private void checkFrequency(DateTime firstTime, Period frequency)
       throws IllegalArgumentException, ArithmeticException {
 
     if (frequency == null) {
       return;
     }
     DateTime secondTime = firstTime.plus(frequency);
    if (secondTime.compareTo(firstTime) <= 0) {
       throw new IllegalArgumentException(
           "Frequency for ScheduledEvent must be a positive period or null");
     }
   }
 
   private void checkNumRepeats(int numRepeats, Period frequency)
       throws IllegalArgumentException {
 
     if (numRepeats < 1) {
       throw new IllegalArgumentException("Event specified " + numRepeats +
           " repetitions; number of repetitions must be at least 1");
     }
     if (numRepeats > 1 && frequency == null) {
       throw new IllegalArgumentException(
           "Frequency for repeating event cannot be null");
     }
   }
 
   private DateTime calculateLastTime(
       DateTime firstTime, Period frequency, int numRepeats)
       throws ArithmeticException {
 
     DateTime lastTime = firstTime;
     for (int i = 1; i < numRepeats; i++) {
       lastTime = lastTime.plus(frequency);
     }
     return lastTime;
   }
 
   private void checkLastTime(DateTime firstTime, DateTime lastTime)
       throws IllegalArgumentException {
 
     if (lastTime == null) {
       throw new IllegalArgumentException(
           "Last time for ScheduledEvent must not be null");
     }
     if (!firstTime.getChronology().equals(lastTime.getChronology())) {
       throw new IllegalArgumentException(
           "First and last times of ScheduledEvent use different chronologies");
     }
     if (lastTime.compareTo(firstTime) < 0) {
       throw new IllegalArgumentException(
           "Last time for ScheduledEvent must not be before firstTime");
     }
   }
 
   public int compareTo(ScheduledEvent other)
       throws NullPointerException, ArithmeticException {
 
     if (other == null) {
       throw new NullPointerException();
     }
     if (this.equals(other)) {
       return 0;
     }
     int tiebreakerVal = (int)(Math.signum(this.tiebreaker - other.tiebreaker));
     if (tiebreakerVal == 0) {
       throw new ArithmeticException(
           "Two different ScheduledEvents had the same random tiebreaker");
     }
     if (this.nextTime == null && other.nextTime == null) {
       return tiebreakerVal;
     } else if (this.nextTime == null) {
       return 1;
     } else if (other.nextTime == null) {
       return -1;
     } else {
       int result = this.nextTime.compareTo(other.nextTime);
       if (result == 0) {
         result = tiebreakerVal;
       }
       return result;
     }
   }
 
   AccountEvent getEvent() {
     return this.event;
   }
 
   DateTime getNextTime() {
     return this.nextTime;
   }
 
   void updateNextTime() {
     if (this.nextTime == null) {
       return;
     }
 
     // Update the tiebreaker because the nextTime field is changing
     this.tiebreaker = Math.random();
 
     if (this.frequency == null) {
       this.nextTime = null;
     } else {
       try {
         this.nextTime = this.nextTime.plus(this.frequency);
         if (this.lastTime.compareTo(this.nextTime) < 0) {
           this.nextTime = null;
         }
       } catch (ArithmeticException e) {
         this.nextTime = null;
       }
     }
   }
 }
