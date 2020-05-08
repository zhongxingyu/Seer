 package swarm.client.navigation;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.smAppContext;
 import swarm.client.entities.smBufferCell;
 import swarm.client.entities.smCamera;
 import swarm.client.input.smBrowserHistoryManager;
 import swarm.client.input.smBrowserAddressManager;
 import swarm.client.managers.smCameraManager;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Action_Camera_SetInitialPosition;
 import swarm.client.states.camera.Action_Camera_SnapToAddress;
 import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
 import swarm.client.states.camera.Event_Camera_OnAddressResponse;
 import swarm.client.states.camera.Event_GettingMapping_OnResponse;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraFloating;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.view.tooltip.smToolTipManager;
 import swarm.client.view.widget.smMagnifier;
 import swarm.shared.debugging.smU_Debug;
 import swarm.shared.json.smA_JsonFactory;
 import swarm.shared.json.smI_JsonObject;
 import swarm.shared.statemachine.smA_Action;
 import swarm.shared.statemachine.smA_State;
 import swarm.shared.statemachine.smE_StateTimeType;
 import swarm.shared.statemachine.smI_StateEventListener;
 import swarm.shared.statemachine.smStateContext;
 import swarm.shared.statemachine.smStateEvent;
 import swarm.shared.structs.smCellAddress;
 import swarm.shared.structs.smCellAddressMapping;
 import swarm.shared.structs.smE_CellAddressParseError;
 import swarm.shared.structs.smE_GetCellAddressMappingError;
 import swarm.shared.structs.smGetCellAddressMappingResult;
 import swarm.shared.structs.smGridCoordinate;
 import swarm.shared.structs.smPoint;
 
 /**
  * Responsible for piping user navigation to the state machine via the browser back/forward/refresh buttons and address bar.
  * 
  * @author Doug
  *
  */
 public class smBrowserNavigator implements smI_StateEventListener
 {
 	private static final Logger s_logger = Logger.getLogger(smBrowserNavigator.class.getName());
 	
 	private final static String FLOATING_STATE_PATH = "/";
 	
 	private final smHistoryStateManager m_historyManager;
 	private final smBrowserAddressManager m_addressManager;
 
 	private Event_Camera_OnAddressResponse.Args 		m_args_OnAddressResponse	= null;
 	private Event_GettingMapping_OnResponse.Args	 	m_args_OnMappingResponse	= null;
 	private final Action_Camera_SnapToPoint.Args		m_args_SetCameraTarget		= new Action_Camera_SnapToPoint.Args();
 	private final Action_Camera_SetInitialPosition.Args	m_args_SetInitialPosition 	= new Action_Camera_SetInitialPosition.Args();
 	private final Action_Camera_SnapToAddress.Args 		m_args_SnapToAddress		= new Action_Camera_SnapToAddress.Args();
 	private final Action_Camera_SnapToCoordinate.Args 	m_args_SnapToCoordinate		= new Action_Camera_SnapToCoordinate.Args();
 
 	private Class<? extends smA_Action> m_lastSnapAction = null;
 	private boolean m_pushHistoryStateForFloating = true;
 	private boolean m_receivedFloatingStateEntered = false;
 	private boolean m_stateAlreadyPushedForViewingExit = false;
 	
 	private double m_lastTimeFloatingStateSet = 0;
 	
 	private final smHistoryStateManager.I_Listener m_historyStateListener;
 	
 	private final double m_floatingHistoryUpdateRate;
 	
 	private final smCameraManager m_cameraMngr;
 	private final smStateContext m_stateContext;
 	
 	public smBrowserNavigator(smStateContext stateContext, smCameraManager cameraMngr, smA_JsonFactory jsonFactory, String defaultPageTitle, double floatingHistoryUpdateRate_seconds)
 	{
 		m_stateContext = stateContext;
 		m_cameraMngr = cameraMngr;
 		
 		m_floatingHistoryUpdateRate = floatingHistoryUpdateRate_seconds;
 		
 		m_args_SnapToCoordinate.userData = this.getClass();
 		m_args_SetCameraTarget.userData = this.getClass();
 		
 		m_historyStateListener = new smHistoryStateManager.I_Listener()
 		{
 			@Override
 			public void onStateChange(String path, smHistoryState state)
 			{
 				if( state == null )
 				{
 					smU_Debug.ASSERT(false, "History state shouldn't be null.");
 					
 					return;
 				}
 				
 				if( state.getMapping() != null )
 				{
 					m_args_SnapToCoordinate.init(state.getMapping().getCoordinate());
 					
 					if( !m_stateContext.isActionPerformable(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoordinate) )
 					{
 						m_historyManager./*re*/pushState(path, state.getMapping());
 					}
 					else
 					{
 						m_stateContext.performAction(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoordinate);
 					}
 				}
 				else if( state.getPoint() != null )
 				{
 					if( !m_stateContext.isActionPerformable(Action_Camera_SnapToPoint.class) )
 					{
 						m_historyManager./*re*/pushState(path, state.getPoint());
 					}
 					else
 					{
 						//--- DRK > Always try to set camera's initial position first.  This can essentially only be done
 						//---		at app start up.  The rest of the time it will fail and we'll set camera target normally.
 						m_args_SetInitialPosition.init(state.getPoint());
 						if( m_stateContext.performAction(Action_Camera_SetInitialPosition.class, m_args_SetInitialPosition) )
 						{
 							//s_logger.info("SETTING INITIAL POINT: " + state.getPoint());
 						}
 						else
 						{
 							m_args_SetCameraTarget.init(state.getPoint(), false, true);
 							m_stateContext.performAction(Action_Camera_SnapToPoint.class, m_args_SetCameraTarget);
 							
 							//s_logger.info("SETTING TARGET POINT: " + state.getPoint());
 						}
 					}
 				}
 				else
 				{
 					//--- DRK > Should mean that we navigated to b33hive.net/blahblah from other domain,
 					//---		pressed backwards (or whatever) while still snapping to blahblah, then pressed forward
 					//---		once again.
 				}
 			}
 		};
 		
 		m_historyManager = new smHistoryStateManager(jsonFactory, defaultPageTitle, m_historyStateListener);
 		
 		m_addressManager = new smBrowserAddressManager();
 	}
 	
 	public void go(int offset)
 	{
 		m_historyManager.go(offset);
 	}
 	
 	private smCellAddress getBrowserCellAddress()
 	{
 		String path = m_addressManager.getCurrentPath();
 		smCellAddress address = new smCellAddress(path);
 		smE_CellAddressParseError parseError = address.getParseError();
 		
 		if( parseError != smE_CellAddressParseError.EMPTY )
 		{
 			return address;
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	@Override
 	public void onStateEvent(smStateEvent event)
 	{
 		switch(event.getType())
 		{
 			case DID_ENTER:
 			{
 				if ( event.getState() instanceof StateMachine_Camera )
 				{
 					smHistoryState currentHistoryState = m_historyManager.getCurrentState();
 					smCellAddress address = getBrowserCellAddress();
 					
 					if( currentHistoryState == null || currentHistoryState.isEmpty() )
 					{
 						if( address != null )
 						{
 							m_pushHistoryStateForFloating = false;
 							m_stateAlreadyPushedForViewingExit = true;
 							
 							m_historyManager.setState(address, new smHistoryState()); // set empty state
 							m_args_SnapToAddress.init(address);
 							event.getState().performAction(Action_Camera_SnapToAddress.class, m_args_SnapToAddress);
 						}
 						else
 						{
 							//--- DRK > Just make sure here that the address bar is "clean".
 							//---		This should get rid of things like url parameters, hash tags, etc.,
 							//---		if for some strange reason the user put them there.
 							m_pushHistoryStateForFloating = false;
 							m_historyManager.setState(FLOATING_STATE_PATH, m_cameraMngr.getCamera().getPosition());
 						}
 					}
 					
 					//--- DRK > This case essentially manually fires a state change event when we're coming from a different domain.
 					else
 					{
 						String path = address == null ? FLOATING_STATE_PATH : address.getCasedRawAddressLeadSlash();
 						
 						m_historyStateListener.onStateChange(path, currentHistoryState);
 					}
 				}
 				else if( event.getState() instanceof State_ViewingCell )
 				{					
 					if( m_lastSnapAction == null )
 					{
 						//--- DRK > This case implies that browser navigation (pressing forward/backward)
 						//---		was the cause of entering this state.
 						//---		There is a case where you can manually snap to a cell, get there, but 
 						//---		address hasn't come in yet, then you navigate away, then the address comes in.  If you press the back
 						//---		button to return to the cell, the address won't show unless we manually set it here.
 						State_ViewingCell state = event.getState();
 						smBufferCell cell = state.getCell();
 						smCellAddress address = cell.getCellAddress();
 						if( address != null )
 						{
 							m_historyManager.setState(address, new smCellAddressMapping(cell.getCoordinate()));
 						}
 					}
 					else
 					{
 						if( m_lastSnapAction == Action_Camera_SnapToCoordinate.class )
 						{
 							smU_Debug.ASSERT(m_args_OnMappingResponse == null, "sm_bro_nav_112387");
 							
 							boolean pushEmptyState = false;
 							smCellAddressMapping mapping = null;
 							if( m_args_OnAddressResponse != null )
 							{
 								mapping = m_args_OnAddressResponse.getMapping();
 							
 								if( m_args_OnAddressResponse.getType() == Event_Camera_OnAddressResponse.E_Type.ON_FOUND )
 								{
 									smHistoryState state = new smHistoryState(m_args_OnAddressResponse.getMapping());
 									
 									if( m_stateAlreadyPushedForViewingExit )
 									{
 										m_historyManager.setState(m_args_OnAddressResponse.getAddress(), state);
 									}
 									else
 									{
 										m_historyManager.pushState(m_args_OnAddressResponse.getAddress(), state);
 									}
 								}
 								else
 								{
 									pushEmptyState = true;
 								}
 							}
 							else
 							{
 								mapping = new smCellAddressMapping(((State_ViewingCell)event.getState()).getCell().getCoordinate());
 								
 								pushEmptyState = true;
 							}
 							
 							if( pushEmptyState )
 							{
 								if( m_stateAlreadyPushedForViewingExit )
 								{
 									m_historyManager.setState(FLOATING_STATE_PATH, mapping);
 								}
 								else
 								{
 									m_historyManager.pushState(FLOATING_STATE_PATH, mapping);
 								}
 							}
 						}
 						else if( m_lastSnapAction == Action_Camera_SnapToAddress.class )
 						{
 							smU_Debug.ASSERT(m_args_OnAddressResponse == null, "sm_bro_nav_112387");
 							
 							if( m_args_OnMappingResponse != null )
 							{
 								if( m_args_OnMappingResponse.getType() == Event_GettingMapping_OnResponse.E_Type.ON_FOUND )
 								{
 									if( m_historyManager.getCurrentState() == null )
 									{
 										smCellAddress address = this.getBrowserCellAddress();
 										if( address != null )
 										{
 											m_historyManager.setState(address, m_args_OnMappingResponse.getMapping());
 										}
 										else
 										{
 											smU_Debug.ASSERT(false, "with current history state null with last snap action being to address, browser address should have existed");
 										}
 									}
 									else
 									{
 										if( m_stateAlreadyPushedForViewingExit )
 										{
 											m_historyManager.setState(m_args_OnMappingResponse.getAddress(), m_args_OnMappingResponse.getMapping());
 										}
 										else
 										{
 											m_historyManager.pushState(m_args_OnMappingResponse.getAddress(), m_args_OnMappingResponse.getMapping());
 										}
 									}
 								}
 								else
 								{
 									smU_Debug.ASSERT(false, "sm_bro_nav_asaswwewe");
 								}
 							}
 							else
 							{
 								smU_Debug.ASSERT(false, "sm_bro_nav_87654332");
 							}
 						}
 						else
 						{
 							smU_Debug.ASSERT(false, "sm_bro_nav_193498");
 						}
 					}
 					
 					m_args_OnAddressResponse = null;
 					m_args_OnMappingResponse = null;
 					m_lastSnapAction = null;
 					m_stateAlreadyPushedForViewingExit = false;
 				}
 				else if( event.getState() instanceof State_CameraFloating )
 				{
 					m_receivedFloatingStateEntered = true;
 					m_lastTimeFloatingStateSet = 0;
 					
 					if( m_historyManager.getCurrentState() == null )
 					{
 						m_historyManager.setState(FLOATING_STATE_PATH, m_cameraMngr.getCamera().getPosition());
 					}
 					else
 					{
 						if( m_pushHistoryStateForFloating )
 						{
 							StateMachine_Camera machine = m_stateContext.getEnteredState(StateMachine_Camera.class);
 							
 							if( m_stateAlreadyPushedForViewingExit || event.getState().getPreviousState() == State_CameraSnapping.class )
 							{
 								m_historyManager.setState(FLOATING_STATE_PATH, m_cameraMngr.getTargetPosition());
 							}
 							else
 							{
 								m_historyManager.pushState(FLOATING_STATE_PATH, m_cameraMngr.getTargetPosition());
 							}
 						}
 					}
 				}
 				else if( event.getState() instanceof State_CameraSnapping )
 				{
 					if( event.getState().getPreviousState() == State_ViewingCell.class )
 					{
 						//StateMachine_Camera machine = smA_State.getEnteredInstance(StateMachine_Camera.class);
 						
 						if( m_lastSnapAction != null )
 						{
 							m_historyManager.pushState(FLOATING_STATE_PATH, m_cameraMngr.getCamera().getPosition());
 							
 							m_stateAlreadyPushedForViewingExit = true;
 						}
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_UPDATE:
 			{
 				if( event.getState() instanceof State_CameraFloating )
 				{
 					if( event.getState().isEntered() ) // just to make sure
 					{
 						if( m_cameraMngr.didCameraJustComeToRest() )
 						{
 							this.setPositionForFloatingState(event.getState(), m_cameraMngr.getCamera().getPosition(), true);
 						}
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_EXIT:
 			{
 				if( event.getState() instanceof State_CameraFloating )
 				{
 					//--- DRK > A null snap action implies that a browser back/forward event initiated the state change,
 					//---		not a user action within the app...in this case, it's too late to set the "floating" history
 					//---		state because we're already in the previous or next history state. Unfortunately the history
 					//---		API doesn't tell us when we *will* change history states, only that we already have.
 					if( m_lastSnapAction != null )
 					{
 						//--- DRK > Kind of a hack here to prevent the browser URL path from being temporarily cleared when
 						//---		you navigate from a different page...if this check isn't here, the path goes like
 						//---		mydomain.com/mypath, mydomain.com while snapping, then mydomain.com/mypath again.
 						//---		It would be better if the statemachine didn't enter the floating state initially.
 						smA_State state = m_stateContext.getEnteredState(StateMachine_Camera.class);
 						if( state != null && state.getUpdateCount() > 0 ) // dunno why it would be null, just being paranoid before a deploy
 						{
 							this.setPositionForFloatingState(event.getState(), m_cameraMngr.getCamera().getPosition(), true);
 						}
 						
 						m_receivedFloatingStateEntered = false;
 					}
 				}
 				
 				break;
 			}
 
 			case DID_PERFORM_ACTION:
 			{
 				if( event.getAction() == Event_Camera_OnAddressResponse.class )
 				{
 					Event_Camera_OnAddressResponse.Args args = event.getActionArgs();
 					
 					if( event.getContext().isEntered(State_CameraSnapping.class) )
 					{
 						m_args_OnAddressResponse = args;
 					}
 					else if( event.getContext().isEntered(State_ViewingCell.class) )
 					{
 						m_args_OnAddressResponse = null;
 						
 						State_ViewingCell viewingState = event.getContext().getEnteredState(State_ViewingCell.class);
 
 						if( viewingState.getCell().getCoordinate().isEqualTo(args.getMapping().getCoordinate()) )
 						if( args.getType() == Event_Camera_OnAddressResponse.E_Type.ON_FOUND )
 						{
 							m_historyManager.setState(args.getAddress(), args.getMapping());
 						}
 					}
 					else
 					{
 						smU_Debug.ASSERT(false, "sm_nav_1");
 					}
 				}
 				else if( event.getAction() == Event_GettingMapping_OnResponse.class )
 				{
 					Event_GettingMapping_OnResponse.Args args = event.getActionArgs();
 
 					m_args_OnMappingResponse = args;
 					
 					if( args.getType() != Event_GettingMapping_OnResponse.E_Type.ON_FOUND )
 					{
 						//--- DRK > This takes care of the case where a user navigates to a cell with a valid address format
 						//---		through the url bar, but the address can't be resolved, so we just wipe the url bar completely.
 						m_historyManager.setState(FLOATING_STATE_PATH, m_cameraMngr.getCamera().getPosition());
 					}
 				}
 				else if( event.getAction() == Action_Camera_SnapToAddress.class ||
 						 event.getAction() == Action_Camera_SnapToCoordinate.class )
 				{
 					m_args_OnAddressResponse = null;
 					m_args_OnMappingResponse = null;
 					
 					if( event.getAction() == Action_Camera_SnapToCoordinate.class )
 					{
 						Action_Camera_SnapToCoordinate.Args args = event.getActionArgs();
 						
 						Object userData = event.getActionArgs().userData;
 						if( userData == this.getClass() ) // signifies that snap was because of browser navigation event.
 						{
 							m_lastSnapAction = null;
 							
 							return;
 						}
 						
 						if( args.onlyCausedRefresh() )
 						{
 							m_lastSnapAction = null;
 							
 							return;
 						}
 					}
 					else if( event.getAction() == Action_Camera_SnapToAddress.class )
 					{
 						Action_Camera_SnapToAddress.Args args = event.getActionArgs();
 						
 						if( args.onlyCausedRefresh() )
 						{
 							m_lastSnapAction = null;
 							
 							return;
 						}
 					}
 					
 					m_lastSnapAction = event.getAction();
 				}
 				else if( event.getAction() == Action_Camera_SnapToPoint.class )
 				{
 					State_CameraFloating floatingState = m_stateContext.getEnteredState(State_CameraFloating.class);
 					
 					m_pushHistoryStateForFloating = true;
 					
 					if( floatingState != null )
 					{
 						Action_Camera_SnapToPoint.Args args = event.getActionArgs();
 						
 						if( args != null )
 						{
 							m_pushHistoryStateForFloating = args.userData != this.getClass();
 							
 							if( m_receivedFloatingStateEntered )
 							{
 								smPoint point = args.getPoint();
 								
 								if( point != null )
 								{
 									//setPositionForFloatingState(floatingState, point, false);
 								}
 							}
 						}
 					}
 					else
 					{
 						//--- DRK > Above action can now be performed and not cause floating state to be entered, so commented out for now.
 						//smU_Debug.ASSERT(false, "floating state should have been entered.");
 					}
 				}
 				
 				break;
 			}
 		}
 	}
 	
	public void setPositionForFloatingState(smA_State state, smPoint point, boolean force)
 	{
 		double timeInState = state.getTimeInState(smE_StateTimeType.TOTAL);
 		if( force || timeInState - m_lastTimeFloatingStateSet >= m_floatingHistoryUpdateRate )
		{s_logger.severe("ERERER");
 			m_historyManager.setState(FLOATING_STATE_PATH, point);
 			
 			m_lastTimeFloatingStateSet = timeInState;
 		}
 	}
 }
