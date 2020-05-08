 package swarm.client.view.cell;
 
 import swarm.client.entities.Camera;
 import swarm.client.input.I_ClickHandler;
 import swarm.client.navigation.BrowserNavigator.I_StateChangeListener;
 import swarm.client.states.camera.Action_Camera_SnapToAddress;
 import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Action_ViewingCell_Refresh;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.states.code.Action_EditingCode_Preview;
 import swarm.client.states.code.Action_EditingCode_Save;
 import swarm.client.view.ViewContext;
 import swarm.client.view.tooltip.E_ToolTipType;
 import swarm.client.view.tooltip.ToolTipConfig;
 import swarm.client.view.tooltip.ToolTipManager;
 import swarm.client.view.widget.SpriteButton;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.statemachine.I_StateEventListener;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.Point;
 
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 
 public class VisualCellHudInner extends HorizontalPanel implements I_StateEventListener
 {
 	private class smHudButton extends SpriteButton
 	{
 		private smHudButton(String spriteId)
 		{
 			super(spriteId);
 			
 			this.addStyleName("sm_hud_button");
 		}
 	}
 	
 	private static final Point s_utilPoint1 = new Point();
 	
 	private final Action_Camera_SnapToPoint.Args m_args_SetCameraTarget = new Action_Camera_SnapToPoint.Args();
 	
 	private final HorizontalPanel m_leftDock = new HorizontalPanel();
 	private final HorizontalPanel m_rightDock = new HorizontalPanel();
 	
 	private final smHudButton m_back		= new smHudButton("back");
 	private final smHudButton m_forward		= new smHudButton("forward");
 	private final smHudButton m_refresh		= new smHudButton("refresh");
 	private final smHudButton m_close		= new smHudButton("close");
 
 	private boolean m_waitingForBeingRefreshableAgain = false;
 	
 	private final ViewContext m_viewContext;
 	
 	VisualCellHudInner(ViewContext viewContext)
 	{
 		m_viewContext = viewContext;
 		
 		this.setWidth("100%");
 		
 		this.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		m_leftDock.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		m_rightDock.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		
 		m_leftDock.add(m_back);
 		m_leftDock.add(m_forward);
 		m_leftDock.add(m_refresh);
 		
 		m_rightDock.add(m_close);
 		
 		this.add(m_leftDock);
 		this.add(m_rightDock);
 		
 		this.setCellHorizontalAlignment(m_rightDock, HasHorizontalAlignment.ALIGN_RIGHT);
 		
 		m_viewContext.browserNavigator.addStateChangeListener(new I_StateChangeListener()
 		{
 			@Override
 			public void onStateChange()
 			{
 				updateHistoryButtons();
 			}
 		});
 		
 		m_viewContext.clickMngr.addClickHandler(m_back, new I_ClickHandler()
 		{
 			@Override
 			public void onClick(int x, int y)
 			{
 				if( !m_back.isEnabled() )  return;
 				
 				m_viewContext.browserNavigator.go(-1);
 				
 				updateHistoryButtons();
 			}
 		});
 		
 		m_viewContext.clickMngr.addClickHandler(m_forward, new I_ClickHandler()
 		{
 			@Override
 			public void onClick(int x, int y)
 			{
 				if( !m_forward.isEnabled() )  return;
 				
 				m_viewContext.browserNavigator.go(1);
 				
 				updateHistoryButtons();
 			}
 		});
 		
 		m_viewContext.clickMngr.addClickHandler(m_refresh, new I_ClickHandler()
 		{
 			@Override
 			public void onClick(int x, int y)
 			{
 				if( !m_refresh.isEnabled() )  return;
 				
 				VisualCellHudInner.this.m_viewContext.cellMngr.clearAlerts();
 				
 				m_viewContext.stateContext.perform(Action_ViewingCell_Refresh.class);
 			}
 		});
 		
 		m_viewContext.clickMngr.addClickHandler(m_close, new I_ClickHandler()
 		{
 			@Override
 			public void onClick(int x, int y)
 			{
 				if( !m_close.isEnabled() )  return;
 
 				State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 				if( viewingState == null )
 				{
 					s_utilPoint1.copy(VisualCellHudInner.this.m_viewContext.appContext.cameraMngr.getCamera().getPosition());
 				}
 				else
 				{
 					A_Grid grid = viewingState.getCell().getGrid();
 					viewingState.getTargetCoord().calcCenterPoint(s_utilPoint1, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
 				}
 				
 				s_utilPoint1.incZ(VisualCellHudInner.this.m_viewContext.appConfig.backOffDistance);
 				
 				m_args_SetCameraTarget.init(s_utilPoint1, false, true);
 				m_viewContext.stateContext.perform(Action_Camera_SnapToPoint.class, m_args_SetCameraTarget);
 			}
 		});
 		
 		ToolTipManager toolTipper = m_viewContext.toolTipMngr;
 		toolTipper.addTip(m_back, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Go back."));
 		toolTipper.addTip(m_forward, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Go forward."));
 		toolTipper.addTip(m_refresh, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Refresh this cell."));
 		toolTipper.addTip(m_close, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Back off."));
 	}
 	
 	private void updateCloseButton()
 	{
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 		State_CameraSnapping snappingState = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
 		
 		if( viewingState != null || snappingState != null && snappingState.getPreviousState() == State_ViewingCell.class )
 		{
 			m_close.setEnabled(true);
 		}
 		else
 		{
 			m_close.setEnabled(false);
 		}
 	}
 	
 	private void updateRefreshButton()
 	{
 		boolean canRefresh = m_viewContext.stateContext.isPerformable(Action_ViewingCell_Refresh.class);
 		m_refresh.setEnabled(canRefresh);
 		
 		if( !canRefresh )
 		{
 			m_waitingForBeingRefreshableAgain = true;
 		}
 		else
 		{
 			m_waitingForBeingRefreshableAgain = false;
 		}
 	}
 	
 	private void updateHistoryButtons()
 	{
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 		State_CameraSnapping snappingState = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
 		
 		if( viewingState != null || snappingState != null && snappingState.getPreviousState() == State_ViewingCell.class )
 		{
 			m_back.setEnabled(m_viewContext.browserNavigator.hasBack());
 			m_forward.setEnabled(m_viewContext.browserNavigator.hasForward());
 		}
 		else
 		{
 			m_back.setEnabled(false);
 			m_forward.setEnabled(false);
 		}
 	}
 
 	@Override
 	public void onStateEvent(StateEvent event)
 	{
 		switch( event.getType() )
 		{
 			case DID_ENTER:
 			{
 				if( event.getState() instanceof State_ViewingCell || event.getState() instanceof State_CameraSnapping )
 				{
 					this.updateCloseButton();
 					this.updateHistoryButtons();
 				}
 				
 				break;
 			}
 			
 			case DID_FOREGROUND:
 			{
 				if( event.getState() instanceof State_ViewingCell )
 				{
 					this.updateHistoryButtons();
 					this.updateRefreshButton();
 				}
 				
 				break;
 			}
 			
 			case DID_UPDATE:
 			{
 				if( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					if( event.getState() instanceof State_ViewingCell )
 					{
 						State_ViewingCell viewingState = (State_ViewingCell) event.getState();
 						if( m_waitingForBeingRefreshableAgain )
 						{
 							if( viewingState.isForegrounded() )
 							{
 								updateRefreshButton();
 							}
 						}
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_EXIT:
 			{
 				if( event.getState() instanceof State_ViewingCell )
 				{
 					m_waitingForBeingRefreshableAgain = false;
 					
 					this.updateCloseButton();
 					this.updateHistoryButtons();
 					this.updateRefreshButton();
 				}
 				
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if((	event.getAction() == Action_ViewingCell_Refresh.class		||
 						event.getAction() == Action_EditingCode_Save.class			||
 						event.getAction() == Action_EditingCode_Preview.class		||
 			
 						//--- DRK > These two cover the case of if we snap to a cell that we're already visiting.
 						//---		This effectively refreshes the cell in this case.
 						event.getAction() == Action_Camera_SnapToAddress.class		||
 						event.getAction() == Action_Camera_SnapToCoordinate.class	))
 				{
 					updateRefreshButton();
 				}
 					
 				break;
 			}
 		}
 	}
 }
