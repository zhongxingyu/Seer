 package swarm.client.navigation;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.AppContext;
 import swarm.client.entities.Camera;
 import swarm.client.entities.BufferCell;
 import swarm.client.input.BrowserAddressManager;
 import swarm.client.input.Mouse;
 import swarm.client.input.MouseEvent;
 import swarm.client.managers.CameraManager;
 import swarm.client.managers.GridManager;
 import swarm.client.states.StateContainer_Base;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraFloating;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.view.I_UIElement;
 import swarm.client.view.S_UI;
 import swarm.client.view.U_Css;
 import swarm.client.view.ViewContext;
 import swarm.client.view.cell.VisualCell;
 import swarm.client.view.cell.VisualCellHudInner;
 import swarm.shared.utils.U_Math;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.statemachine.A_Action;
 import swarm.shared.statemachine.A_State;
 import swarm.shared.statemachine.StateContext;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.CellAddress;
 import swarm.shared.structs.E_GetCellAddressMappingError;
 import swarm.shared.structs.GetCellAddressMappingResult;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.Point;
 import swarm.shared.structs.Tolerance;
 import swarm.shared.structs.Vector;
 import swarm.shared.structs.VelocitySmoother;
 
 import com.google.gwt.user.client.ui.Panel;
 
 /**
  * ...
  * @author
  */
 public class MouseNavigator implements I_UIElement, Mouse.I_Listener
 {
 	private static final Logger s_logger = Logger.getLogger(MouseNavigator.class.getName());
 	
 	private static final double SCROLL_CAP = 10;
 	private static final double BASE_SCROLL_SCALE = 10;
 	private static final Tolerance MOUSE_TOLERANCE = new Tolerance(Tolerance.DEFAULT);
 	
 	private final Point m_mouseZoomPoint2d = new Point();
 	private final Point m_mouseZoomPoint3d = new Point();
 	private boolean m_isZooming = false;
 	
 	private StateMachine_Camera m_cameraMachine = null;
 	private A_State m_cameraState = null;
 	
 	private final Point m_utilPoint1 = new Point();
 	private final Point m_utilPoint2 = new Point();
 	private final Vector m_utilVector = new Vector();
 	
 	private final Point m_grabPoint = new Point();
 	private final Point m_lastWorldPoint = new Point();
 	private final VelocitySmoother m_flickSmoother = new VelocitySmoother(S_UI.FLICK_SMOOTHING_SAMPLE_COUNT);
 
 	private final Action_Camera_SnapToPoint.Args m_args_SnapToPoint = new Action_Camera_SnapToPoint.Args();
 	private final Action_Camera_SnapToCoordinate.Args m_args_SnapToCoord = new Action_Camera_SnapToCoordinate.Args();
 	
 	private final GridCoordinate m_mouseGridCoord = new GridCoordinate();
 	private boolean m_isMouseTouchingSnappableCell = false;
 	
 	private final Mouse m_mouse;
 	private boolean m_mouseWentDownOnViewedCell = false;
 	
 	private final GridManager m_gridMngr;
 	private final CameraManager m_cameraMngr;
 	private final ViewContext m_viewContext;
 	
 	public MouseNavigator(ViewContext viewContext, Mouse mouse)
 	{
 		m_viewContext = viewContext;
 		m_gridMngr = m_viewContext.appContext.gridMngr;
 		m_cameraMngr = m_viewContext.appContext.cameraMngr;
 		
 		m_mouse = mouse;
 		
 		m_mouse.setListener(this);
 	}
 	
 	public Mouse getMouse()
 	{
 		return m_mouse;
 	}
 	
 	public GridCoordinate getMouseGridCoord()
 	{
 		return m_mouseGridCoord;
 	}
 	
 	public boolean isMouseTouchingSnappableCell()
 	{
 		return m_isMouseTouchingSnappableCell && m_mouse.isMouseOver();
 	}
 	
 	public void onMouseEvent(MouseEvent event)
 	{
 		switch( event.getType())
 		{
 			case MOUSE_MOVE:
 			{
 				//updateMouse();
 				
 				break;
 			}
 			
 			case MOUSE_DOWN:
 			{
 				if ( m_cameraState instanceof State_ViewingCell )
 				{
 					BufferCell cell = ((State_ViewingCell) m_cameraState).getCell();
 					A_Grid grid = cell.getGrid();
 					
 					mousePointToWorld(m_utilPoint1);
 					
 					if( U_CameraViewport.isPointInViewport(grid, cell.getCoordinate(), m_utilPoint1, m_viewContext.appConfig.cellHudHeight, grid.getCellPadding()))
 					{
 						m_mouseWentDownOnViewedCell = true;
 						
 						return;
 					}
 				}
 				
 				m_flickSmoother.clear();
 				mousePointToWorld(m_grabPoint);
 				m_lastWorldPoint.copy(m_cameraMngr.getCamera().getPosition());
 				
 				m_mouseWentDownOnViewedCell = false;
 				
 				break;
 			}
 			
 			/*case MOUSE_OVER:
 			{
 				if( m_mouseWasDownWhenWentOut )
 				{
 					m_isMouseDown = true;
 					m_mouseWasDownWhenWentOut = false;
 				}
 				
 				break;
 			}*/
 			
 			case MOUSE_OUT:
 			{
 				break;
 				/*if( !m_mouse.wasMouseJustDown() )
 				{
 					break;
 				}*/
 			}
 			case MOUSE_UP:
 			{
 				//m_scrollArrow.hide();
 				
 				if ( !m_mouse.hasMouseStrayedWhileDown() )
 				{
 					onMouseClick();
 				}
 				else if ( m_cameraState instanceof State_CameraFloating )
 				{
 					m_flickSmoother.calcVelocity(m_utilVector);
 				
 					
 					if( !m_utilVector.isZeroLength(null) )
 					{
 						m_utilVector.scaleByNumber(3);
 						m_utilVector.setZ(0.0);
 						m_utilPoint1.copy(m_cameraMngr.getTargetPosition());
 						m_utilPoint1.translate(m_utilVector);
 						
 						m_args_SnapToPoint.init(m_utilPoint1, false, true);
 						m_cameraMachine.perform(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 					}
 					else
 					{
 						//--- DRK(TODO): Hacky...mouse navigator shouldn't know about browser navigator.
 						m_viewContext.browserNavigator.setPositionForFloatingState(m_cameraState, m_cameraMngr.getCamera().getPosition(), true);
 					}
 				}
 				
 				break;
 			}
 			
 			case MOUSE_SCROLLED:
 			{
 				//--- Don't allow zoom out if mouse is over a viewed cell.
 				if ( m_cameraState instanceof State_ViewingCell )
 				{
 					ScrollNavigator scrollNavigator = m_viewContext.scrollNavigator;
 					
 					if( scrollNavigator.isScrollingX() || scrollNavigator.isScrollingY() )  return;
 					
 					BufferCell cell = ((State_ViewingCell) m_cameraState).getCell();
 					A_Grid grid = cell.getGrid();
 					
 					mousePointToWorld(m_utilPoint2);
 					
 					double extraPadding = 0;
 					
 					if( scrollNavigator.isScrollingX() || scrollNavigator.isScrollingY() )
 					{
 						extraPadding = m_viewContext.config.extraScrollArea;
 					}
 					
 					if( U_CameraViewport.isPointInViewport(grid, cell.getCoordinate(), m_utilPoint2, m_viewContext.appConfig.cellHudHeight, extraPadding))
 					{
 						return;
 					}
 				}
 				
 				Camera camera = m_cameraMngr.getCamera();
 				Point cameraPosition = m_utilPoint1;
 				cameraPosition.copy(camera.getPosition());
 				
 				if ( cameraPosition.getZ() == 0 && event.getScrollDelta() > 0 )
 				{
 					m_isZooming = false;
 					
 					return;
 				}
 				
 				if ( event.getScrollDelta() < 0 )
 				{
 					m_isZooming = false;
 				}
 				
 				if ( m_cameraState instanceof State_CameraFloating || m_cameraState instanceof State_ViewingCell || m_cameraState instanceof State_CameraSnapping)
 				{
 					if ( !m_isZooming )
 					{
 						if ( cameraPosition.getZ() > 0 && event.getScrollDelta() > 0 )
 						{
 							m_isZooming = true;
 							
 							m_mouseZoomPoint2d.copy(m_mouse.getMousePoint());
 							mousePointToWorld(m_mouseZoomPoint3d);
 						}
 					}
 					
 					double scrollDelta = U_Math.clamp(event.getScrollDelta(), -SCROLL_CAP, SCROLL_CAP);
 					scrollDelta *= BASE_SCROLL_SCALE;
 					
 					// TODO: Don't really like this zoom behavior...have to scale zom bsaed on distance somehow though.
 					scrollDelta += U_Math.sign(scrollDelta) * camera.getPosition().getZ() * .5;
 					
 				//	trace(event.getScrollDelta(), scrollDelta, camera.getPosition().getZ());
 					
 					if ( m_isZooming )
 					{
 						m_mouseZoomPoint3d.calcDifference(cameraPosition, m_utilVector);
 						double lengthSquared = m_utilVector.calcLengthSquared();
 						m_utilVector.normalize();
 						m_utilVector.scaleByNumber(scrollDelta);
 						
 						if ( m_utilVector.calcLengthSquared() > lengthSquared )
 						{
 							m_utilVector.setLength(Math.sqrt(lengthSquared));
 						}
 						
 						//trace(m_utilVector);
 						
 						cameraPosition.add(m_utilVector);
 						
 						/*if( !(oldCameraState instanceof State_CameraSnapping) )
 						{
 							cameraPosition.setX(StateMachine_CameraController.IGNORED_COMPONENT);
 							cameraPosition.setY(StateMachine_CameraController.IGNORED_COMPONENT);
 						}*/
 						
 						m_args_SnapToPoint.init(cameraPosition, false, true);
 						m_cameraMachine.perform(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 					}
 					else
 					{
 						/*smPoint cameraPointProjected = m_utilPoint1;
 						cameraPointProjected.copy(camera.getPosition());
 						cameraPointProjected.setZ(0);
 						mousePointInWorld.calcDifference(cameraPointProjected, m_utilVector);
 						//trace(m_utilVector);*/
 						
 						cameraPosition.incZ( -scrollDelta);
 						//cameraPosition.add(m_utilVector);
 						
 						/*if( !(oldCameraState instanceof State_CameraSnapping) )
 						{
 							cameraPosition.setX(StateMachine_CameraController.IGNORED_COMPONENT);
 							cameraPosition.setY(StateMachine_CameraController.IGNORED_COMPONENT);
 						}*/
 						
 						m_args_SnapToPoint.init(cameraPosition, false, true);
 						m_cameraMachine.perform(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 					}
 				}
 				
 				break;
 			}
 		}
 	}
 	
 	private void mousePointToWorld(Point point_out)
 	{
 		this.screenToWorld(m_mouse.getMousePoint(), point_out);
 	}
 	
 	private void screenToWorld(Point screenPoint, Point point_out)
 	{
 		Camera camera = m_cameraMngr.getCamera();
 		
 		camera.calcWorldPoint(screenPoint, point_out);
 	}
 	
 	private void updateMouseGridCoord()
 	{
 		A_Grid grid = m_gridMngr.getGrid();
 		double gridWidth = grid.calcPixelWidth();
 		double gridHeight = grid.calcPixelHeight();
 		
 		mousePointToWorld(m_utilPoint2);
 		
 		if ( m_utilPoint2.getX() < 0 || m_utilPoint2.getX() > gridWidth )
 		{
 			m_mouseGridCoord.set(-1, -1);
 
 			m_isMouseTouchingSnappableCell = false;
 			
 			return;
 		}
 		
 		if ( m_utilPoint2.getY() < 0 || m_utilPoint2.getY() > gridHeight )
 		{
 			m_mouseGridCoord.set(-1, -1);
 
 			m_isMouseTouchingSnappableCell = false;
 			
 			return;
 		}
 		
 		//double cellSizePlusSpacing = smS_App.CELL_PLUS_SPACING_PIXEL_COUNT;
 		
 		//double modX = m_utilPoint2.getX() % cellSizePlusSpacing;
 		//double modY = m_utilPoint2.getY() % cellSizePlusSpacing;
 		
 		m_mouseGridCoord.setWithPoint(m_utilPoint2, grid.getCellWidth() + grid.getCellPadding(), grid.getCellHeight() + grid.getCellPadding());
 		
 		/*if ( modX > smS_App.CELL_PIXEL_COUNT || modY > smS_App.CELL_PIXEL_COUNT )
 		{
 			m_isMouseTouchingCell = false;
 		}*/
 		
 		/*smGridCoordinate viewedOrTargetCoord = null;
 		
 		if( m_cameraState instanceof State_ViewingCell )
 		{
 			smBufferCell viewedCell = ((State_ViewingCell)m_cameraState).getCell();
 			
 			viewedOrTargetCoord = viewedCell.getCoordinate();
 		}
 		else if( m_cameraState instanceof State_CameraSnapping )
 		{
 			viewedOrTargetCoord = ((State_CameraSnapping)m_cameraState).getTargetCoordinate();
 		}
 		
 		if( viewedOrTargetCoord != null && viewedOrTargetCoord.isEqualTo(m_mouseGridCoord) )
 		{
 			m_isMouseTouchingSnappableCell = false;
 			return;
 		}*/
 		
 		m_isMouseTouchingSnappableCell = true;
 	}
 	
 	private void backOff()
 	{
 		m_utilPoint1.copy(m_viewContext.appContext.cameraMngr.getCamera().getPosition());
 		m_utilPoint1.incZ(m_viewContext.appConfig.backOffDistance);
 		
 		m_args_SnapToPoint.init(m_utilPoint1, false, true);
 		m_viewContext.stateContext.perform(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 	}
 	
 	private void onMouseClick()
 	{
 		boolean viewing = m_viewContext.stateContext.isEntered(State_ViewingCell.class);
 		
 		updateMouseGridCoord();
 		
 		if( viewing )
 		{
 			backOff();
 			return;
 		}		
 		
 		if( !m_isMouseTouchingSnappableCell )
 		{
 			if( viewing )  backOff();
 			
 			return;
 		}
 		
 		A_Grid grid = m_gridMngr.getGrid();
 		
 		//--- DRK > Currently always snapping to top/left corner.
 		//---		Using mouse point was used in the past, and allowed
 		//---		you to snap to constrained parts of the cell if window
 		//---		was too small to see whole cell...it's judged as bad UX
 		//---		as of this writing however.
 		//mousePointToWorld(m_utilPoint1);
 		grid.calcCoordTopLeftPoint(m_mouseGridCoord, 1, m_utilPoint1);
 		
 		double cellHudHeight = m_viewContext.appConfig.cellHudHeight;
 		double viewWidth = m_cameraMngr.getCamera().getViewWidth();
 		double viewHeight = m_cameraMngr.getCamera().getViewHeight();
 		U_CameraViewport.calcConstrainedCameraPoint(grid, m_mouseGridCoord, m_utilPoint1, viewWidth, viewHeight, cellHudHeight, m_utilPoint2);
 		
 		m_args_SnapToCoord.init(m_mouseGridCoord, m_utilPoint2);
 		m_args_SnapToCoord.causeRefresh(false);
 		
 		if( !m_cameraMachine.perform(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoord) )
 		{
 			if( viewing )
 			{
 				backOff();
 			}
 		}
 	}
 	
 	private void updateMouse()
 	{
 		Camera camera = m_cameraMngr.getCamera();
 
 		// DRK > NOTE: This must be updated every frame, even with mouse still, mouse can still be moving in the world.
 		updateMouseGridCoord();
 		
 		if( m_cameraMachine.getUpdateCount() % 15 == 0 )
 		{
 			//s_logger.info("" + smS_App.CELL_SPACING_PIXEL_COUNT * camera.calcDistanceRatio());
 		}
 		
 		if ( m_mouse.isMouseDown() )
 		{
 			if ( m_cameraState instanceof State_CameraFloating )
 			{
 				Point mouseDown3d = m_grabPoint;
 				Point mouseCurrent3d = m_utilPoint1;
 				Point camera3d = camera.getPosition();
 				
 				//camera.calcWorldPoint(m_mouse.getMouseDownPoint(), mouseDown3d);
 				camera.calcWorldPoint(m_mouse.getMousePoint(), mouseCurrent3d);
 				
 				mouseCurrent3d.calcDifference(mouseDown3d, m_utilVector);
 				m_utilVector.negate();
 				
 				m_utilPoint2.copy(camera3d);
 				m_utilPoint2.translate(m_utilVector);
 				
 				//m_utilPoint2.setZ(StateMachine_CameraController.IGNORED_COMPONENT);
 				
 				//s_logger.info(m_utilVector.toString());
 		
 				
 				//TODO: m_mouse.isMoving is busted.
 				if( m_utilVector.calcLengthSquared() > 0)
 				{
 					m_args_SnapToPoint.init(m_utilPoint2, true, true);
 					m_viewContext.stateContext.perform(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 				}
 				
 				m_utilPoint2.calcDifference(m_lastWorldPoint, m_utilVector);
 				m_flickSmoother.addVelocity(m_utilVector);
 				m_lastWorldPoint.copy(m_utilPoint2);
 				
 				//if ( m_mouse.hasMouseStrayedWhileDown() && !m_mouse.isMouseMoving() )
 				{
 					//m_scrollArrow.show();
 				}
 				
 				/*if ( m_scrollArrow.isShowing() )
 				{
 					m_scrollArrow.graphics.clear();
 					m_scrollArrow.graphics.lineStyle(3, 0, .5);
 					m_scrollArrow.graphics.moveTo(m_mouseCursor.getX(), m_mouseCursor.getY() );
 					m_scrollArrow.graphics.lineTo(m_mouseDownPoint.getX(), m_mouseDownPoint.getY());
 					
 					m_mouseCursor.calcDifference(m_mouseDownPoint, m_utilVector);
 					m_utilVector.setLength(10);
 					m_utilVector.rotate(Math.PI / 4);
 					m_scrollArrow.graphics.moveTo(m_mouseDownPoint.getX(), m_mouseDownPoint.getY());
 					smPoint drawPoint = m_mouseDownPoint.clone();
 					drawPoint.add(m_utilVector);
 					m_scrollArrow.graphics.lineTo(drawPoint.getX(), drawPoint.getY());
 					m_utilVector.rotate( -Math.PI / 2)
 					drawPoint.copy(m_mouseDownPoint);
 					drawPoint.add(m_utilVector);
 					m_scrollArrow.graphics.moveTo(m_mouseDownPoint.getX(), m_mouseDownPoint.getY());
 					m_scrollArrow.graphics.lineTo(drawPoint.getX(), drawPoint.getY());
 				}*/
 			}
 			
 			if( m_mouse.hasMouseStrayedWhileDown() )
 			{
 				if ( m_cameraState instanceof State_CameraSnapping )
 				{
 					m_cameraMachine.perform(Action_Camera_SnapToPoint.class);
 				}
 				else if ( m_cameraState instanceof State_ViewingCell )
 				{
 					if( !m_mouseWentDownOnViewedCell )
 					{
 						m_cameraMachine.perform(Action_Camera_SnapToPoint.class);
 					}
 				}
 			}
 		}
 		
 		if ( this.m_isZooming )
 		{
 			if ( !m_mouse.getMousePoint().isEqualTo(m_mouseZoomPoint2d, MOUSE_TOLERANCE) )
 			{
 				this.m_isZooming = false;
 			}
 		}
 	}
 	
 	public void onStateEvent(StateEvent event)
 	{
 		switch(event.getType())
 		{
 			case DID_ENTER:
 			{
 				if ( event.getState() instanceof StateMachine_Camera )
 				{
 					m_cameraMachine = (StateMachine_Camera) event.getState();
 				}
 				else if ( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					if( !event.getState().isTransparent() )
 					{
 						m_cameraState = event.getState();
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_FOREGROUND:
 			{
 				if ( event.getState() instanceof StateMachine_Camera )
 				{
 					//m_cellManager.sync(smCellBufferManager.getInstance()); // make sure cells are in correct initial position before rendering
 				}
 				break;
 			}
 			
 			case DID_EXIT:
 			{
 				if ( event.getState() instanceof StateMachine_Camera )
 				{
 					m_cameraMachine = null;
 				}
 				else if ( event.getState() instanceof State_CameraFloating )
 				{
 					m_isZooming = false; // just a catch all....can only zoom while floating...zooming could even be its own state maybe
 					//m_scrollArrow.hide();
 				}
 				
 				break;
 			}
 			
 			case DID_UPDATE:
 			{
 				if ( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					//m_scrollArrow.update(state.getLastTimeStep());
 					this.update();
 				}
 				
 				break;
 			}
 		}
 	}
 
 	private void update()
 	{
 		m_mouse.update();
 		updateMouse();
 	}
 }
