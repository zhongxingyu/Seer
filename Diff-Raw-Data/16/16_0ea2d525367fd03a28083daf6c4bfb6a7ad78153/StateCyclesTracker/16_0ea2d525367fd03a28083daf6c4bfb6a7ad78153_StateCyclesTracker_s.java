 package xephyrus.sam.core.trackers;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import xephyrus.sam.core.Payload;
 import xephyrus.sam.core.StateCycleListener;
 import xephyrus.sam.core.StateMachine.StateCycles;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class StateCyclesTracker<S extends Enum, P extends Payload>
   implements StateCycleListener<S,P>
 {
   @Override
   public void beforeStateWork (S state, P payload)
   {
     incCount(StateCycles.BEFORE);
   }
 
   @Override
   public void afterStateWork (S state, P payload)
   {
     incCount(StateCycles.AFTER);
   }
 
   @Override
  public void errorStateWork (S state, P payload, Exception error)
   {
     incCount(StateCycles.ERROR);
     _log.error("Received " + error,error);
   }
 
   public Integer getCount (StateCycles cycle)
   {
     Integer count = _counts.get(cycle);
     return (count != null ? count : 0);
   }
 
   public void incCount (StateCycles cycle)
   {
     Integer count = _counts.get(cycle);
     count = (count != null ? count + 1 : 1);
     _counts.put(cycle,count);
     _log.info(cycle + " count is now " + count);
   }
 
   private Map<StateCycles,Integer> _counts = new HashMap<StateCycles,Integer>();
   private Logger _log = LogManager.getLogger(getClass());
 }
