 package swarm.shared.statemachine;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import swarm.client.states.code.State_EditingCodeBlocker;
 import swarm.shared.debugging.smU_Debug;
 
 public class smStateContext
 {
 	private static final Logger s_logger = Logger.getLogger(smStateContext.class.getName());
 	
 	private final smA_State m_rootState;
 	
 	private final ArrayList<smI_StateEventListener> m_listeners = new ArrayList<smI_StateEventListener>();
 	private final ArrayList<smStateEvent> m_eventQueue = new ArrayList<smStateEvent>();
 	private int m_eventQueueIndex = 0;
 	
 	private boolean m_processEventQueue_hasEntered = false;
 	
 	private int m_queueEvent_recursionDepth = 0;
 	
 	private final HashMap<Class<? extends smA_State>, smA_State> m_stateRegistry = new HashMap<Class<? extends smA_State>, smA_State>();
 	private final HashMap<Class<? extends smA_Action>, smA_Action> m_actionRegistry = new HashMap<Class<? extends smA_Action>, smA_Action>();
 	
 	public smStateContext(smA_State rootState, smI_StateEventListener stateEventListener)
 	{
 		m_rootState = rootState;
 		
 		this.addListener(stateEventListener);
 		
 		registerState(m_rootState);
 	}
 	
 	public smA_State getRootState()
 	{
 		return m_rootState;
 	}
 	
 	public void didEnter()
 	{		
 		m_rootState.didEnter_internal(null);
 	}
 	
 	public void didForeground()
 	{
 		m_rootState.didForeground_internal(null, null);
 	}
 	
 	public void didUpdate(double timeStep)
 	{
 		m_rootState.update_internal(timeStep);
 	}
 	
 	void registerAction(Class<? extends smA_State> association, smA_Action action)
 	{
 		m_actionRegistry.put(action.getClass(), action);
 		
 		action.m_context = this;
 		action.m_association = association;
 	}
 	
 	smA_Action getAction(Class<? extends smA_Action> T)
 	{
 		smA_Action registeredAction = m_actionRegistry.get(T);
 	
 		if ( registeredAction != null )
 		{
 			if ( registeredAction.m_state != null )
 			{
 				smU_Debug.ASSERT(false, "Action reuse.");
 			}
 			
 			return registeredAction;
 		}
 		
 		return null;
 	}
 
 	private smA_State getEnteredStateForAction(Class<? extends smA_Action> T)
 	{
 		smA_Action action = getAction(T);
 		
 		if( action != null )
 		{
 			Class<? extends smA_State> state_T = action.getStateAssociation();
 			
 			smA_State state = this.getEnteredState(state_T);
 			
 			return state;
 		}
 		
 		return null;
 	}
 	
 	public boolean performAction(Class<? extends smA_Action> T)
 	{
 		return performAction(T, null);
 	}
 	
 	public boolean performAction(Class<? extends smA_Action> T, smA_ActionArgs args)
 	{
 		smA_State state = getEnteredStateForAction(T);
 		
 		return state.performAction(T, args);
 	}
 	
 	public boolean isActionPerformable(Class<? extends smA_Action> T)
 	{
 		return isActionPerformable(T, null);
 	}
 	
 	public boolean isActionPerformable(Class<? extends smA_Action> T, smA_ActionArgs args)
 	{
 		smA_State state = getEnteredStateForAction(T);
 		
 		return isActionPerformable_private(state, T, args);
 	}
 	
 	private boolean isActionPerformable_private(smA_State state, Class<? extends smA_Action> T, smA_ActionArgs args)
 	{
 		if( state == null )
 		{
 			return false;
 		}
 		else
 		{
 			return state.isActionPerfomable(T, args);
 		}
 	}
 	
 	public <T extends smA_State> T getEnteredState(Class<? extends smA_State> T)
 	{
 		smA_State registeredState = m_stateRegistry.get(T);
 		if ( registeredState != null )
 		{
 			if ( registeredState.isEntered() )
 			{
 				return (T) registeredState;
 			}
 		}
 		
 		return null;
 	}
 	
 	public boolean isForegrounded(Class<? extends smA_State> T)
 	{
 		return getForegroundedState(T) != null;
 	}
 	
 	public boolean isEntered(Class<? extends smA_State> T)
 	{
 		return getEnteredState(T) != null;
 	}
 	
 	public void registerState(smA_State state)
 	{
 		m_stateRegistry.put(state.getClass(), state);
 		state.m_context = this;
 		
 		state.onRegistered();
 	}
 	
 	public <T extends smA_State> T getForegroundedState(Class<? extends smA_State> T)
 	{
 		smA_State registeredState = m_stateRegistry.get(T);
 		if ( registeredState != null )
 		{
 			if ( registeredState.isForegrounded() )
 			{
 				return (T) registeredState;
 			}
 		}
 		
 		return null;
 	}
 	
 	protected smA_State getInstance(Class<? extends smA_State> T)
 	{
 		smA_State registeredState = m_stateRegistry.get(T);
 		if ( registeredState != null )
 		{
 			if ( registeredState.isEntered() )
 			{
 				smU_Debug.ASSERT(false, "Tried to reuse state instance.");
 			}
 			
 			return registeredState;
 		}
 
 		smU_Debug.ASSERT(false, "No state instance registered.");
 		
 		return null;
 	}
 	
 	void beginBatch()
 	{
 		m_queueEvent_recursionDepth++;
 	}
 	
 	void endBatch()
 	{
 		processEventQueue();
 	}
 	
 	void queueEvent(smStateEvent newEvent)
 	{
 		boolean findAntiEvents = false;
 
 		switch( newEvent.getType() )
 		{
 			case DID_EXIT:
 			case DID_BACKGROUND:
 			{
 				findAntiEvents = true;
 			}
 		}
 		
 		boolean antiMatterExplosion = false;
 		
 		if( findAntiEvents )
 		{
 			for( int i = m_eventQueue.size()-1; i >= m_eventQueueIndex; i-- )
 			{
 				smStateEvent pastEvent = m_eventQueue.get(i);
 				
 				if( pastEvent == null )
 				{
 					continue;
 				}
 				
 				if( pastEvent.m_state == newEvent.getState() )
 				{
 					if( newEvent.getType() == smE_StateEventType.DID_EXIT )
 					{
 						if( pastEvent.getType() == smE_StateEventType.DID_ENTER )
 						{
 							antiMatterExplosion = true;
 						}
 						/*else if( pastEvent.getType() == smE_StateEventType.DID_PERFORM_ACTION )
 						{
 							antiMatterExplosion = true;
 						}*/
 					}
 					else if( newEvent.getType() == smE_StateEventType.DID_BACKGROUND )
 					{
 						if( pastEvent.getType() == smE_StateEventType.DID_FOREGROUND )
 						{
 							antiMatterExplosion = true;
 						}
 						/*else if( pastEvent.getType() == smE_StateEventType.DID_PERFORM_ACTION )
 						{
 							if( !pastEvent.m_action.isPerformableInBackground() )
 							{
 								antiMatterExplosion = true;
 							}
 						}*/
 					}
 				}
 				
 				if( antiMatterExplosion )
 				{
 					m_eventQueue.set(i, null);
 					
 					break;
 				}
 			}
 		}
 		
 		if( !antiMatterExplosion )
 		{
 			m_eventQueue.add(newEvent);
 		}
 		
 		m_queueEvent_recursionDepth++;
 	}
 	
 	void addListener(smI_StateEventListener listener)
 	{
 		m_listeners.add(listener);
 	}
 	
 	void removeListener(smI_StateEventListener listener)
 	{
 		for( int i = m_listeners.size()-1; i >= 0; i-- )
 		{
 			if( m_listeners.get(i) == listener )
 			{
 				if( !m_processEventQueue_hasEntered )
 				{
 					m_listeners.remove(i);
 				}
 				else
 				{
 					m_listeners.set(i, null);
 				}
 				
 				break;
 			}
 		}
 	}
 	
 	private void cleanListeners()
 	{
 		smU_Debug.ASSERT(!m_processEventQueue_hasEntered);
 		
 		for( int i = m_listeners.size()-1; i >= 0; i-- )
 		{
 			if( m_listeners.get(i) == null )
 			{
 				m_listeners.remove(i);
 				
 				break;
 			}
 		}
 	}
 	
 	void processEventQueue()
 	{
 		m_queueEvent_recursionDepth--;
 		
 		if( m_queueEvent_recursionDepth > 0 )
 		{
 			return;
 		}
 		
 		boolean firstEntryIntoMethod = false;
 		
 		if( !m_processEventQueue_hasEntered )
 		{
 			m_eventQueueIndex = 0;
 			
 			firstEntryIntoMethod = true;
 
 			m_processEventQueue_hasEntered = true;
 		}
 		
 		if( !firstEntryIntoMethod )
 		{
 			return;
 		}
 		
 		if( m_listeners.size() == 0 )
 		{
 			m_eventQueue.clear();
 			m_eventQueueIndex = 0;
 			
 			m_processEventQueue_hasEntered = false;
 			
 			return;
 		}
 		
 		//--- DRK > NOTE: Keeping the indices as members of this class is useless now,
 		//---				but at some point was necessary as recursive calls to this method were possible.
 		//---				Just keeping them as members for now because this class is a house of cards.
 		while( m_eventQueueIndex < m_eventQueue.size() )
 		{
 			smStateEvent event = m_eventQueue.get(m_eventQueueIndex);
 			
 			if( event == null )
 			{
 				m_eventQueueIndex++;
 				
 				continue;
 			}
 
 			if( event.m_listenerIndex == smStateEvent.UNINITIALIZED_LISTENER_INDEX )
 			{
 				event.m_listenerIndex = m_listeners.size()-1;
 			}
 			
 		//	s_logger.severe(event.getType() + " " + event.getState());
 			
 			while( event.m_listenerIndex >= 0 )
 			{
 				int listenerIndex = event.m_listenerIndex;
 				event.m_listenerIndex--;
 				
 				if( event.m_listenerIndex == -1 )
 				{
 					m_eventQueueIndex++;
 				}
 				
 				//--- DRK > Listener at this index can be null if it was removed while m_processEventQueue_hasEntered == true.
 				smI_StateEventListener listener = m_listeners.get(listenerIndex);
 				if( listener != null )
 				{
 					event.dispatch(listener);
 				}
 
 				//--- DRK > If we still have listeners to go, and it appears that this event was cancelled out inside
 				//---		the last dispatch, we continue our merry way along the event queue.
 				if( event.m_listenerIndex >= 0 )
 				{
 					if( m_eventQueue.get(m_eventQueueIndex) == null )
 					{
 						break;
 					}
 				}
 			}
 		}
 		
 		if( m_eventQueueIndex >= m_eventQueue.size() )
 		{
 			smU_Debug.ASSERT(m_eventQueueIndex == m_eventQueue.size() );
 			
 			m_eventQueue.clear();
 			m_eventQueueIndex= 0;
 		}
 		
 		if( firstEntryIntoMethod )
 		{
 			m_processEventQueue_hasEntered = false;
 			
 			cleanListeners();
 		}
 	}
 }
