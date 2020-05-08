 /**
  * 
  */
 package com.emlynoregan.statemachine;
 
 import java.util.AbstractMap.SimpleEntry;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * @author emlyn
  *
  */
 public class StateMachine
 {
 	public static class StringBase implements Comparable<StringBase>  
 	{
 		private String _value;
 		
 		public StringBase(String aValue)
 		{
 			_value = aValue;
 		}
 		
 		public String getValue()
 		{
 			return _value;
 		}
 
         @Override  
         public int compareTo(StringBase other)  
         {  
             return _value.compareTo(other._value);  
         }  
           
         @Override  
         public boolean equals(Object other)  
         {  
             return (other != null) && (getClass() == other.getClass()) &&   
                 _value.equals(((StringBase)other)._value);  
         }  
           
         @Override  
         public int hashCode()  
         {  
             return _value.hashCode();  
         }  
           
         @Override  
         public String toString()  
         {  
             return _value;  
         }  
 	}
 	
 	public static class State extends StringBase 
 	{
 		public State(String aValue)
 		{
 			super(aValue);
 		}
 		
 		public State CloneState()
 		{
 			return new State(this.getValue());
 		}
 	}
 	
 	public static class Condition extends StringBase 
 	{
 		public Condition(String aValue)
 		{
 			super(aValue);
 		}
 
 		
 		public Condition CloneCondition()
 		{
 			return new Condition(this.getValue());
 		}
 	}
 	
 	public interface IStateChange
 	{
		public void OnNewState(State newState);
 	}
 	
 	private ConcurrentHashMap<SimpleEntry<State, Condition>, State> _transitions;
 	private ConcurrentLinkedQueue<Condition> _raisedConditions;
 	private State _currentState;
 	private State _stopState;
 	private Object _syncObj;
 	private Object _processRaisedConditionSyncObj;
 	private Object _disposeSyncObj;
 	private boolean _disposing;
 	private boolean _started;
 	private Timer _timer;
 	private IStateChange _stateChangeHandler;
 	
 	public static String StopConditionStringName = "__STOP__";
 	public static String ProceedConditionStringName = "__PROCEED__";
 	public static String TimerConditionStringName = "__TIMER__";
 	
 	private State CloneCurrentState()
 	{
 		State retval = null;
 		
 		synchronized(_syncObj)
 		{
 			if (_currentState != null)
 			{
 				retval = _currentState.CloneState();
 			}
 		}
 		
 		return retval;
 	}
 	
 	private void ProcessRaisedCondition()
 	{
 		if (!_disposing)
 		{
 			final StateMachine lfinalThis = this;
 			
 			new Thread
 			(
 				new Runnable() 
 				{
 					@Override
 					public void run() 
 					{
 						synchronized (lfinalThis._processRaisedConditionSyncObj)
 						{
 							Condition lraisedCondition = null;
 							State lcurrentState;
 							State ltargetState = null;
 							
 							synchronized (lfinalThis._syncObj) 
 							{
 								lraisedCondition = lfinalThis._raisedConditions.poll();
 								lcurrentState = lfinalThis.CloneCurrentState();
 							}
 							
 							if (
 								lraisedCondition != null &&
 								lcurrentState != null
 								)
 							{
 								ltargetState = lfinalThis._transitions.get(new SimpleEntry<State, Condition>(lcurrentState, lraisedCondition));
 								
 								if (ltargetState != null)
 								{
 									synchronized(lfinalThis._syncObj)
 									{
 										lfinalThis.StopTimer();
 										
 										_currentState = ltargetState;
 									}
 									
 									if (ltargetState.equals(_stopState))
 									{
 										synchronized (lfinalThis._disposeSyncObj) {
 											_disposeSyncObj.notifyAll();
 										}
 									}
 											
 									
 									if (lfinalThis._stateChangeHandler != null)
 									{
 										final State lfinalTargetState = ltargetState;
 										new Thread
 										(
 											new Runnable() 
 											{
 												@Override
 												public void run() 
 												{
 													try
 													{
 														lfinalThis._stateChangeHandler.OnNewState(lfinalTargetState);
 													}
 													catch(Exception ex)
 													{
 														ex.printStackTrace();
 													}
 												}
 											}
 										).start();
 									}
 								}
 							}
 						}
 					}
 				}
 			).start();
 		}
 	}
 
 	public StateMachine(ConcurrentHashMap<SimpleEntry<State, Condition>, State> aTransitions, State aStart, State aStop, IStateChange aStateChangeHandler)
 	{
 		if (aTransitions == null)
 			throw new IllegalArgumentException("aTransitions cannot be null");
 		if (aStart == null)
 			throw new IllegalArgumentException("aStart cannot be null");
 		if (aStop == null)
 			throw new IllegalArgumentException("aStop cannot be null");
 		if (aStateChangeHandler == null)
 			throw new IllegalArgumentException("aStateChangeHandler cannot be null");
 		
 		_started = false;
 		_disposing = false;
 		_transitions = aTransitions;
 		_raisedConditions = new ConcurrentLinkedQueue<StateMachine.Condition>();
 		_currentState = aStart;
 		_stopState = aStop;
 		_syncObj = new Object();
 		_processRaisedConditionSyncObj = new Object();
 		_disposeSyncObj = new Object();
 		_timer = new Timer();
 		_stateChangeHandler = aStateChangeHandler;
 	}
 	
 	public void Start()
 	{
 		if (!_started)
 		{
 			_started = true;
 			
 			new Thread
 			(
 				new Runnable() 
 				{
 					@Override
 					public void run() 
 					{
 						try
 						{
 							_stateChangeHandler.OnNewState(CloneCurrentState());
 						}
 						catch(Exception ex)
 						{
 							ex.printStackTrace();
 						}
 					}
 				}
 			).start();
 		}
 	}
 	
 	public void RaiseCondition(Condition aCondition)
 	{
 		synchronized (_syncObj) 
 		{
 			_raisedConditions.add(aCondition);
 		}
 		
 		ProcessRaisedCondition();
 	}
 	
 	public State getCurrentState()
 	{
 		return CloneCurrentState();
 	}
 	
 	public static Condition getStopCondition()
 	{
 		return new Condition(StateMachine.StopConditionStringName);
 	}
 
 	public static Condition getProceedCondition()
 	{
 		return new Condition(StateMachine.ProceedConditionStringName);
 	}
 
 	public static Condition getTimerCondition()
 	{
 		return new Condition(StateMachine.TimerConditionStringName);
 	}
 	
 	public void SetTimer(long aDelayMilliseconds)
 	{
 		synchronized (_syncObj) 
 		{
 			_timer.schedule
 			(
 				new TimerTask() {
 					
 					@Override
 					public void run() {
 						RaiseCondition(StateMachine.getTimerCondition());
 					}
 				}, 
 				aDelayMilliseconds
 			);
 		}
 	}
 	
 	// must be called inside syncobj lock
 	private void StopTimer()
 	{
 		_timer.cancel();
 		_timer = new Timer();
 	}
 	
 	public void Dispose()
 	{
 		if (_started)
 		{
 			if (_stopState != null)
 			{
 				RaiseCondition(getStopCondition());
 				
 				State lcurrentState = CloneCurrentState();
 				
 				if (!lcurrentState.equals(_stopState))
 				{
 					synchronized(_disposeSyncObj)
 					{
 						try {
 							_disposeSyncObj.wait(1000);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 			
 			_disposing = true;
 			
 			synchronized (_syncObj)
 			{
 				StopTimer();
 			}
 		}
 	}
 }
