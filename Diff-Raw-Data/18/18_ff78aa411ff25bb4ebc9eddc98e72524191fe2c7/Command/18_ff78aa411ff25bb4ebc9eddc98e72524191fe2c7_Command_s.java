 package com.haakenstad.potwater.scheduler;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.format.PeriodFormat;
 import org.joda.time.format.PeriodFormatter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bjornhaa
  * Date: 11.07.13
  * Time: 21:12
  * To change this template use File | Settings | File Templates.
  */
 public abstract class Command {
 
     protected final Integer valve;
 
     protected final Integer pump;
 
     protected final DateTime start;
 
     public Command(Integer valve, Integer pump) throws Exception {
         this.valve = valve;
         this.pump = pump;
         start = new DateTime();
     }
 
     protected String formatDuration() {
         DateTime stop = new DateTime();
         Duration duration = new Duration(start, stop);
         PeriodFormatter formatter = PeriodFormat.getDefault();
         return formatter.print(duration.toPeriod());
     }
 
     public abstract void enable();
 
    public abstract void disable();
 
 }
