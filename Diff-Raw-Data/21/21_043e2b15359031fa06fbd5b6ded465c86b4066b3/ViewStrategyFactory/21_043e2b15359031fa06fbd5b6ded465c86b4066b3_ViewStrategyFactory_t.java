 package ch.ffhs.esa.lifeguard.ui;
 
import java.util.EnumMap;

import ch.ffhs.esa.lifeguard.alarm.state.AlarmStateId;

 public class ViewStrategyFactory
 {
     private static class Entry
     {
         private AlarmStateId id;
         private ViewStateStrategy strategy;
 
         public Entry (AlarmStateId id, ViewStateStrategy s)
         {
             this.id = id;
             this.strategy = s;
         }
 
         public AlarmStateId getId ()
         { return id; }
 
         public ViewStateStrategy getStrategy ()
         { return strategy; }
     }
 
     private Entry [] entries = {
         new Entry (AlarmStateId.INIT, new InitialView ()),
         new Entry (AlarmStateId.TICKING, new TickingView ()),
        new Entry (AlarmStateId.ALARMING, new AlarmingView ()),
        new Entry (AlarmStateId.AWAITING, new AwaitingView ()),
         new Entry (AlarmStateId.CONFIRMED, new RescuedView ())
     };
 
     private EnumMap<AlarmStateId, ViewStateStrategy> strategies
        = new EnumMap<AlarmStateId, ViewStateStrategy> (AlarmStateId.class);
 
    public ViewStrategyFactory ()
     { buildFactory (); }
 
     public ViewStateStrategy create (AlarmStateId which)
     {
         ViewStateStrategy strategy = strategies.get (which);
         if (strategy == null) {
             throw new IllegalArgumentException (
                     "There is no view strategy for the given alarm state id");
         }
 
         return strategy;
     }
 
     private void buildFactory ()
     {
         for (Entry entry : entries) {
             strategies.put (entry.getId (), entry.getStrategy ());
         }
     }
 }
