 package xephyrus.sam.support;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import xephyrus.sam.core.Payload;
 import xephyrus.sam.core.StateCycleListener;
 import xephyrus.sam.core.StateMachine;
 
 public class StateCycleLogger<S extends Enum,P extends Payload>
   implements StateCycleListener<S,P>
 {
   @Override
   public void beforeStateWork (S state, P payload)
   {
     getLog(state,payload).log(getBeforeLevel(),"Payload " + payload.getId() + " invoking state " +
         state);
   }
 
   @Override
   public void afterStateWork (S state, P payload)
   {
     getLog(state,payload).log(getAfterLevel(),"Payload " + payload.getId() + " now in state " +
         state);
   }
 
   @Override
  public void errorStateWork (S state, P payload, Exception error)
   {
     getLog(state,payload).log(getErrorLevel(),"Payload " + payload.getId() + " failed in state " +
         state,error);
   }
 
   public Level getBeforeLevel ()
   {
     return _beforeLevel;
   }
 
   public void setBeforeLevel (Level beforeLevel)
   {
     _beforeLevel = beforeLevel;
   }
 
   public Level getAfterLevel ()
   {
     return _afterLevel;
   }
 
   public void setAfterLevel (Level afterLevel)
   {
     _afterLevel = afterLevel;
   }
 
   public Level getErrorLevel ()
   {
     return _errorLevel;
   }
 
   public void setErrorLevel (Level errorLevel)
   {
     _errorLevel = errorLevel;
   }
 
   public void setAllLevels (Level level)
   {
     setBeforeLevel(level);
     setAfterLevel(level);
     setErrorLevel(level);
   }
 
   public Logger getLog ()
   {
     return _log;
   }
 
   public void setLog (Logger log)
   {
     _log = log;
   }
 
   protected Logger getLog (S state, P payload)
   {
     if (_log == null)
     {
       _log = LogManager.getLogger(StateMachine.class.getCanonicalName() +
           "<" + state.getClass().getSimpleName() + "," + payload.getClass().getSimpleName() + ">");
     }
     return _log;
   }
 
   private Logger _log = null;
   private Level _beforeLevel = Level.TRACE;
   private Level _afterLevel = Level.TRACE;
   private Level _errorLevel = Level.ERROR;
 }
