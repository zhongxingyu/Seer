 package xephyrus.sam.core;
 
 public class DefaultMachineCycleController<S extends Enum, P extends Payload>
   implements MachineCycleController
 {
   public DefaultMachineCycleController (StateMachine<S,P> machine)
   {
     _machine = machine;
   }
 
   @Override
   public boolean shouldProcess ()
   {
     return _machine.getProcessingQueue().isReady();
   }
 
   @Override
   public boolean shouldYield ()
   {
     return !shouldProcess();
   }
 
   @Override
   public long getYieldTime ()
   {
    return _yieldTime;
   }
 
   public void setYieldTime (Long yieldTime)
   {
     _yieldTime = yieldTime;
   }
 
   private StateMachine<S,P> _machine;
   private Long _yieldTime;
 }
