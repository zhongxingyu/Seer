 package swarm.client.navigation;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import com.google.gwt.dom.client.Document;
 
 import swarm.client.app.AppContext;
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.Camera;
 import swarm.client.input.BrowserHistoryManager;
 import swarm.client.input.BrowserAddressManager;
 import swarm.client.managers.CameraManager;
 import swarm.client.managers.CellBufferManager;
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
 import swarm.client.view.ViewContext;
 import swarm.client.view.tooltip.ToolTipManager;
 import swarm.client.view.widget.Magnifier;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.json.A_JsonFactory;
 import swarm.shared.json.I_JsonObject;
 import swarm.shared.statemachine.A_Action;
 import swarm.shared.statemachine.A_State;
 import swarm.shared.statemachine.I_StateEventListener;
 import swarm.shared.statemachine.StateContext;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.CellAddress;
 import swarm.shared.structs.CellAddressMapping;
 import swarm.shared.structs.E_CellAddressParseError;
 import swarm.shared.structs.E_GetCellAddressMappingError;
 import swarm.shared.structs.GetCellAddressMappingResult;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.Point;
 import swarm.shared.utils.ListenerManager;
 
 /**
  * Responsible for piping user navigation to the state machine via the browser back/forward/refresh buttons and address bar.
  * 
  * @author Doug
  *
  */
 public class BrowserNavigator implements I_StateEventListener
 {
 	public interface I_StateChangeListener
 	{
 		void onStateChange();
 	}
 	
 	private static final Logger s_logger = Logger.getLogger(BrowserNavigator.class.getName());
 	
 	private final static String FLOATING_STATE_PATH = "/";
 	
 	private final HistoryStateManager m_historyManager;
 	private final BrowserAddressManager m_addressManager;
 
 	private Event_Camera_OnAddressResponse.Args 		m_args_OnAddressResponse	= null;
 	private Event_GettingMapping_OnResponse.Args	 	m_args_OnMappingResponse	= null;
 	private final Action_Camera_SnapToPoint.Args		m_args_SnapToPoint			= new Action_Camera_SnapToPoint.Args();
 	private final Action_Camera_SnapToAddress.Args 		m_args_SnapToAddress		= new Action_Camera_SnapToAddress.Args();
 	private final Action_Camera_SnapToCoordinate.Args 	m_args_SnapToCoordinate		= new Action_Camera_SnapToCoordinate.Args();
 
 	private Class<? extends A_Action> m_lastSnapAction = null;
 	private boolean m_pushHistoryStateForFloating = true;
 	private boolean m_receivedFloatingStateEntered = false;
 	private boolean m_stateAlreadyPushedForViewingExit = false;
 	
 	private double m_lastTimeFloatingStateSet = 0;
 	
 	private final HistoryStateManager.I_Listener m_historyStateListener;
 	
 	private final double m_floatingHistoryUpdateRate;
 	
 	private final CameraManager m_cameraMngr;
 	private final StateContext m_stateContext;
 	private final ViewContext m_viewContext;
 	
 	private final Point m_utilPoint1 = new Point();
 	
 	private final ListenerManager<I_StateChangeListener> m_listenerManager = new ListenerManager<I_StateChangeListener>();
 	
 	public BrowserNavigator(ViewContext viewContext, String defaultPageTitle, double floatingHistoryUpdateRate_seconds)
 	{
 		m_viewContext = viewContext;
 		m_stateContext = m_viewContext.stateContext;
 		m_cameraMngr = m_viewContext.appContext.cameraMngr;
 		
 		m_floatingHistoryUpdateRate = floatingHistoryUpdateRate_seconds;
 		
 		m_args_SnapToCoordinate.set(this.getClass());
 		m_args_SnapToPoint.set(this.getClass());
 		
 		m_historyStateListener = new HistoryStateManager.I_Listener()
 		{
 			@Override
 			public void onStateChange(String url, HistoryState state)
 			{
 				//TODO: The 'repushState' conditions here are fringe cases that haven't been tested, and will most likely
 				//		not do anything useful. They hit if, for example, a blocking modal dialog is up (e.g. connection error)
 				//		and the user hits the browser back button to go to a previous cell. Currently the modal will prevent the action
 				//		from being performed...in order to repush that history state, we'd need to track the last history state here manually
 				//		because 'url' parameter points to the destination cell...that's arguably a broken UX though...best option might be to
 				//		somehow dismiss the modal in this specific case, but there could be other reasons that the snap action is not performable...
 				//
 				//		sticky situation!
 				if( state == null )
 				{
 					U_Debug.ASSERT(false, "History state shouldn't be null.");
 					
 					return;
 				}
 				
 				if( state.getMapping() != null )
 				{
 					m_args_SnapToCoordinate.init(state.getMapping().getCoordinate());
 					
 					if( !m_stateContext.isPerformable(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoordinate) )
 					{
 						m_historyManager./*re*/pushState(url, state.getMapping());
 					}
 					else
 					{
 						m_stateContext.perform(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoordinate);
 					}
 				}
 				else if( state.getPoint() != null )
 				{
 					if( !m_stateContext.isPerformable(Action_Camera_SnapToPoint.class) )
 					{
 						m_historyManager./*re*/pushState(url, state.getPoint());
 					}
 					else
 					{
 						//--- DRK > Always try to set camera's initial position first.  This can essentially only be done
 						//---		at app start up.  The rest of the time it will fail and we'll set camera target normally.
 						/*m_args_SetInitialPosition.init(state.getPoint());
 						if( m_stateContext.performAction(Action_Camera_SetInitialPosition.class, m_args_SetInitialPosition) )
 						{
 							//s_logger.info("SETTING INITIAL POINT: " + state.getPoint());
 						}
 						else*/
 						{
 							A_State cameraMachine = m_stateContext.getEntered(StateMachine_Camera.class);
 							boolean instant = cameraMachine != null && cameraMachine.getUpdateCount() == 0;
 							m_args_SnapToPoint.init(state.getPoint(), instant, true);
 							m_stateContext.perform(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 							
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
 				
 				dispatchStateChange();
 			}
 		};
 		
 		m_addressManager = new BrowserAddressManager();
 		m_historyManager = new HistoryStateManager(m_viewContext.appContext.jsonFactory, defaultPageTitle, m_historyStateListener, m_addressManager);
 	}
 	
 	public void addStateChangeListener(I_StateChangeListener listener)
 	{
 		m_listenerManager.addListenerToBack(listener);
 	}
 	
 	private void dispatchStateChange()
 	{
 		ArrayList<I_StateChangeListener> m_listeners = m_listenerManager.getListeners();
 		for( int i = 0; i < m_listeners.size(); i++ )
 		{
 			m_listeners.get(i).onStateChange();
 		}
 	}
 	
 	public void go(int offset)
 	{
 		m_historyManager.go(offset);
 	}
 	
 	private CellAddress getBrowserCellAddress()
 	{
 		String path = m_addressManager.getCurrentPath();
 		CellAddress address = new CellAddress(path);
 		E_CellAddressParseError parseError = address.getParseError();
 		
 		if( parseError != E_CellAddressParseError.EMPTY )
 		{
 			return address;
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	private void setViewingEnterHistory(CellAddress address, CellAddressMapping mapping)
 	{
 		if( m_stateAlreadyPushedForViewingExit )
 		{
 			m_historyManager.setState(address, mapping);
 		}
 		else
 		{
 			m_historyManager.pushState(address, mapping);
 		}
 	}
 	
 	@Override
 	public void onStateEvent(StateEvent event)
 	{
 		switch(event.getType())
 		{
 			case DID_ENTER:
 			{
 				if ( event.getState() instanceof StateMachine_Camera )
 				{
 					HistoryState currentHistoryState = m_historyManager.getCurrentState();
 					CellAddress address = getBrowserCellAddress();
 					
 					if( currentHistoryState == null || currentHistoryState.isEmpty() )
 					{
 						if( address != null )
 						{
 							m_pushHistoryStateForFloating = false;
 							m_stateAlreadyPushedForViewingExit = true;
 							
 							m_historyManager.setState(address, new HistoryState()); // set empty state
 							m_args_SnapToAddress.init(address);
 							event.getContext().perform(Action_Camera_SnapToAddress.class, m_args_SnapToAddress);
 						}
 						else
 						{
 							//--- DRK > Just make sure here that the address bar is "clean".
 							//---		This should get rid of things like url parameters, hash tags, etc.,
 							//---		if for some strange reason the user put them there.
 							m_pushHistoryStateForFloating = false;
 							m_historyManager.setState(FLOATING_STATE_PATH, m_cameraMngr.getCamera().getPosition());
 							
 							//--- DRK > Give the camera a little bump to let new users know they're in a 3d environment.
 							m_utilPoint1.copy(m_cameraMngr.getCamera().getPosition());
 							m_utilPoint1.incZ(-m_viewContext.config.initialBumpDistance);
 							m_args_SnapToPoint.init(m_utilPoint1, false, false);
 							event.getContext().perform(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 						}
 					}
 					
 					//--- DRK > This case manually fires a state change event when we're coming from a different domain,
 					//---		or if we're refreshing the page.
 					else
 					{
 						String path;
 						if( address == null )
 						{
 							path = FLOATING_STATE_PATH;
 						}
 						else
 						{
 							path = address.getCasedRawLeadSlash();
 							Document.get().setTitle(path); // manually doing this because for some reason history api sometimes won't set the title
 						}
 						
 						m_historyStateListener.onStateChange(path, currentHistoryState);
 					}
 				}
 				else if( event.getState() instanceof State_ViewingCell )
 				{
 					State_ViewingCell viewingState = event.getState();
 					
 					if( m_lastSnapAction == null )
 					{
 						//--- DRK > This case implies that browser navigation (pressing forward/backward)
 						//---		was the cause of entering this state.
 						//---		There is a case where you can manually snap to a cell, get there, but 
 						//---		address hasn't come in yet, then you navigate away, then the address comes in.  If you press the back
 						//---		button to return to the cell, the address won't show unless we manually set it here.
 						
 						BufferCell cell = viewingState.getCell();
 						CellAddress address = cell.getAddress();
 						if( address != null )
 						{
 							m_historyManager.setState(address, new CellAddressMapping(cell.getCoordinate()));
 						}
 					}
 					else
 					{
 						if( m_lastSnapAction == Action_Camera_SnapToCoordinate.class )
 						{
 							U_Debug.ASSERT(m_args_OnMappingResponse == null, "sm_bro_nav_112389");
 							
 							boolean pushEmptyState = false;
 							BufferCell cell = viewingState.getCell();
 							CellAddressMapping mapping = new CellAddressMapping(cell.getCoordinate());
 							if( m_args_OnAddressResponse != null )
 							{
 								if( m_args_OnAddressResponse.getType() == Event_Camera_OnAddressResponse.E_Type.ON_FOUND )
 								{
 									this.setViewingEnterHistory(m_args_OnAddressResponse.getAddress(), mapping);
 								}
 								else
 								{
 									pushEmptyState = true;
 								}
 							}
 							else
 							{
 								if( cell.getAddress() != null )
 								{
 									this.setViewingEnterHistory(cell.getAddress(), mapping);
 								}
 								else
 								{
 									pushEmptyState = true;
 								}
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
 							//--- DRK > This assert gets hit with direct navigation to a cell with a new page view.
 							//---		Commenting it out cause I can't figure out what it's checking for.
 							//U_Debug.ASSERT(m_args_OnAddressResponse == null, "sm_bro_nav_112387");
 							
 							if( m_args_OnMappingResponse != null )
 							{
 								if( m_args_OnMappingResponse.getType() == Event_GettingMapping_OnResponse.E_Type.ON_FOUND )
 								{
 									if( m_historyManager.getCurrentState() == null )
 									{
 										CellAddress address = this.getBrowserCellAddress();
 										if( address != null )
 										{
 											m_historyManager.setState(address, m_args_OnMappingResponse.getMapping());
 										}
 										else
 										{
 											U_Debug.ASSERT(false, "with current history state null with last snap action being to address, browser address should have existed");
 										}
 									}
 									else
 									{
 										this.setViewingEnterHistory(m_args_OnMappingResponse.getAddress(), m_args_OnMappingResponse.getMapping());
 									}
 								}
 								else
 								{
 									U_Debug.ASSERT(false, "sm_bro_nav_asaswwewe");
 								}
 							}
 							else
 							{
 								U_Debug.ASSERT(false, "sm_bro_nav_87654332");
 							}
 						}
 						else
 						{
 							U_Debug.ASSERT(false, "sm_bro_nav_193498");
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
 							StateMachine_Camera machine = m_stateContext.getEntered(StateMachine_Camera.class);
 							
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
 						A_State state = m_stateContext.getEntered(StateMachine_Camera.class);
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
 						
 						State_ViewingCell viewingState = event.getContext().getEntered(State_ViewingCell.class);
 
 						if( viewingState.getCell().getCoordinate().isEqualTo(args.getMapping().getCoordinate()) )
 						if( args.getType() == Event_Camera_OnAddressResponse.E_Type.ON_FOUND )
 						{
 							m_historyManager.setState(args.getAddress(), args.getMapping());
 						}
 					}
 					else
 					{
 						U_Debug.ASSERT(false, "sm_nav_1");
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
 					
 					if( event.getAction() == Action_Camera_SnapToCoordinate.class )
 					{
 						Action_Camera_SnapToCoordinate.Args args = event.getActionArgs();
 						
						Object userData = event.getActionArgs().values;
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
 						
 						//--- DRK > TODO: Kinda hacky...
 						if( args.historyShouldIgnore )
 						{
 							//--- DRK > Used to have this line here...because multiple "snap to coord" calls can be made after a snap to address now
 							//---		in order to reposition the window and whatnot
 							//m_lastSnapAction = Action_Camera_SnapToAddress.class;
 						}
 						else
 						{
 							m_lastSnapAction = event.getAction();
 							m_args_OnMappingResponse = null;
 						}
 					}
 					else if( event.getAction() == Action_Camera_SnapToAddress.class )
 					{
 						m_args_OnMappingResponse = null;
 						
 						Action_Camera_SnapToAddress.Args args = event.getActionArgs();
 						
 						if( args.onlyCausedRefresh() )
 						{
 							m_lastSnapAction = null;
 							
 							return;
 						}
 						
 						m_lastSnapAction = event.getAction();
 					}
 					
 					
 				}
 				else if( event.getAction() == Action_Camera_SnapToPoint.class )
 				{
 					State_CameraFloating floatingState = m_stateContext.getEntered(State_CameraFloating.class);
 					
 					m_pushHistoryStateForFloating = true;
 					
 					if( floatingState != null )
 					{
 						Action_Camera_SnapToPoint.Args args = event.getActionArgs();
 						
 						if( args != null )
 						{
 							m_pushHistoryStateForFloating = args.get() != this.getClass();
 							
 							if( m_receivedFloatingStateEntered )
 							{
 								Point point = args.getPoint();
 								
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
 	
 	public boolean hasBack()
 	{
 		return m_historyManager.hasBack();
 	}
 	
 	public boolean hasForward()
 	{
 		return m_historyManager.hasForward();
 	}
 	
 	public void setPositionForFloatingState(A_State state, Point point, boolean force)
 	{
 		double timeInState = state.getTotalTimeInState();
 		if( force || timeInState - m_lastTimeFloatingStateSet >= m_floatingHistoryUpdateRate )
 		{
 			m_historyManager.setState(FLOATING_STATE_PATH, point);
 			
 			m_lastTimeFloatingStateSet = timeInState;
 		}
 	}
 }
