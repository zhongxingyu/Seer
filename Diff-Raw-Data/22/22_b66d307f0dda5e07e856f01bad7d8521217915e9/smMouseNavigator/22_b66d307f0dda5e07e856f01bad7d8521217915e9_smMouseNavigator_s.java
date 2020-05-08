 package swarm.client.navigation;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.smAppContext;
 import swarm.client.entities.smCamera;
 import swarm.client.entities.smBufferCell;
 import swarm.client.input.smBrowserAddressManager;
 import swarm.client.input.smMouse;
 import swarm.client.input.smMouseEvent;
 import swarm.client.managers.smCameraManager;
 import swarm.client.managers.smGridManager;
 import swarm.client.states.StateContainer_Base;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraFloating;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.view.smI_UIElement;
 import swarm.client.view.smS_UI;
 import swarm.client.view.smU_Css;
 import swarm.client.view.smViewContext;
 import swarm.client.view.cell.smVisualCell;
 import swarm.shared.utils.smU_Math;
 import swarm.shared.app.smS_App;
 import swarm.shared.debugging.smU_Debug;
 import swarm.shared.entities.smA_Grid;
 import swarm.shared.statemachine.smA_Action;
 import swarm.shared.statemachine.smA_State;
 import swarm.shared.statemachine.smStateContext;
 import swarm.shared.statemachine.smStateEvent;
 import swarm.shared.structs.smCellAddress;
 import swarm.shared.structs.smE_GetCellAddressMappingError;
 import swarm.shared.structs.smGetCellAddressMappingResult;
 import swarm.shared.structs.smGridCoordinate;
 import swarm.shared.structs.smPoint;
 import swarm.shared.structs.smTolerance;
 import swarm.shared.structs.smVector;
 import swarm.shared.structs.smVelocitySmoother;
 
 import com.google.gwt.user.client.ui.Panel;
 
 /**
  * ...
  * @author
  */
 public class smMouseNavigator implements smI_UIElement, smMouse.I_Listener
 {
 	private static final Logger s_logger = Logger.getLogger(smMouseNavigator.class.getName());
 	
 	private static final double SCROLL_CAP = 10;
 	private static final double BASE_SCROLL_SCALE = 10;
 	private static final smTolerance MOUSE_TOLERANCE = new smTolerance(smTolerance.DEFAULT);
 	
 	private final smPoint m_mouseZoomPoint2d = new smPoint();
 	private final smPoint m_mouseZoomPoint3d = new smPoint();
 	private boolean m_isZooming = false;
 	
 	private StateMachine_Camera m_cameraMachine = null;
 	private smA_State m_cameraState = null;
 	
 	private final smPoint m_utilPoint1 = new smPoint();
 	private final smPoint m_utilPoint2 = new smPoint();
 	private final smVector m_utilVector = new smVector();
 	
 	private final smPoint m_grabPoint = new smPoint();
 	private final smPoint m_lastWorldPoint = new smPoint();
 	private final smVelocitySmoother m_flickSmoother = new smVelocitySmoother(smS_UI.FLICK_SMOOTHING_SAMPLE_COUNT);
 
 	private final Action_Camera_SnapToPoint.Args m_args_SnapToPoint = new Action_Camera_SnapToPoint.Args();
 	private final Action_Camera_SnapToCoordinate.Args m_args_SnapToCoord = new Action_Camera_SnapToCoordinate.Args();
 	
 	private final smGridCoordinate m_mouseGridCoord = new smGridCoordinate();
 	private boolean m_isMouseTouchingSnappableCell = false;
 	
 	private final smMouse m_mouse;
 	private boolean m_mouseWentDownOnViewedCell = false;
 	
 	private final smGridManager m_gridMngr;
 	private final smCameraManager m_cameraMngr;
 	private final smViewContext m_viewContext;
 	
 	public smMouseNavigator(smViewContext viewContext, smMouse mouse)
 	{
 		m_viewContext = viewContext;
 		m_gridMngr = m_viewContext.appContext.gridMngr;
 		m_cameraMngr = m_viewContext.appContext.cameraMngr;
 		
 		m_mouse = mouse;
 		
 		m_mouse.setListener(this);
 	}
 	
 	public smMouse getMouse()
 	{
 		return m_mouse;
 	}
 	
 	public smGridCoordinate getMouseGridCoord()
 	{
 		return m_mouseGridCoord;
 	}
 	
 	public boolean isMouseTouchingSnappableCell()
 	{
 		return m_isMouseTouchingSnappableCell && m_mouse.isMouseOver();
 	}
 	
 	public void onMouseEvent(smMouseEvent event)
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
 					smBufferCell cell = ((State_ViewingCell) m_cameraState).getCell();
 					smA_Grid grid = cell.getGrid();
 					
 					mousePointToWorld(m_utilPoint1);
 					
 					if( smU_CameraViewport.isPointInViewport(grid, cell.getCoordinate(), m_utilPoint1, m_viewContext.appConfig.cellHudHeight, grid.getCellPadding()))
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
 					mouseClicked();
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
 						m_cameraMachine.performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 					}
 				}
 				
 				break;
 			}
 			
 			case MOUSE_SCROLLED:
 			{
 				//--- Don't allow zoom out if mouse is over a viewed cell.
 				if ( m_cameraState instanceof State_ViewingCell )
 				{
 					//if( m_viewContext.scrollNavigator.isScrolling() )  return;
 					
 					smBufferCell cell = ((State_ViewingCell) m_cameraState).getCell();
 					smA_Grid grid = cell.getGrid();
 					
 					mousePointToWorld(m_utilPoint2);
 					
 					if( smU_CameraViewport.isPointInViewport(grid, cell.getCoordinate(), m_utilPoint2, m_viewContext.appConfig.cellHudHeight, grid.getCellPadding()))
 					{
 						return;
 					}
 				}
 				
 				smCamera camera = m_cameraMngr.getCamera();
 				smPoint cameraPosition = m_utilPoint1;
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
 					
 					double scrollDelta = smU_Math.clamp(event.getScrollDelta(), -SCROLL_CAP, SCROLL_CAP);
 					scrollDelta *= BASE_SCROLL_SCALE;
 					
 					// TODO: Don't really like this zoom behavior...have to scale zom bsaed on distance somehow though.
 					scrollDelta += smU_Math.sign(scrollDelta) * camera.getPosition().getZ() * .5;
 					
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
 						m_cameraMachine.performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
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
 						m_cameraMachine.performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 					}
 				}
 				
 				break;
 			}
 		}
 	}
 	
 	private void mousePointToWorld(smPoint point_out)
 	{
 		this.screenToWorld(m_mouse.getMousePoint(), point_out);
 	}
 	
 	private void screenToWorld(smPoint screenPoint, smPoint point_out)
 	{
 		smCamera camera = m_cameraMngr.getCamera();
 		
 		camera.calcWorldPoint(screenPoint, point_out);
 	}
 	
 	private void updateMouseGridCoord()
 	{
 		smA_Grid grid = m_gridMngr.getGrid();
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
 	
 	private void mouseClicked()
 	{
 		updateMouseGridCoord();
 		
 		if( !m_isMouseTouchingSnappableCell )  return;
 		
 		smA_Grid grid = m_gridMngr.getGrid();
 		mousePointToWorld(m_utilPoint1);
 		
 		double cellHudHeight = m_viewContext.appConfig.cellHudHeight;
 		double viewWidth = m_cameraMngr.getCamera().getViewWidth();
 		double viewHeight = m_cameraMngr.getCamera().getViewHeight();
 		
 		smU_CameraViewport.calcConstrainedCameraPoint(grid, m_mouseGridCoord, m_utilPoint1, viewWidth, viewHeight, cellHudHeight, m_utilPoint2);
 		
 		m_args_SnapToCoord.init(m_mouseGridCoord, m_utilPoint2);
 		m_cameraMachine.performAction(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoord);
 	}
 	
 	private void updateMouse()
 	{
 		smCamera camera = m_cameraMngr.getCamera();
 
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
 				smPoint mouseDown3d = m_grabPoint;
 				smPoint mouseCurrent3d = m_utilPoint1;
 				smPoint camera3d = camera.getPosition();
 				
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
 					m_viewContext.stateContext.performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
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
 					m_cameraMachine.performAction(Action_Camera_SnapToPoint.class);
 				}
 				else if ( m_cameraState instanceof State_ViewingCell )
 				{
 					if( !m_mouseWentDownOnViewedCell )
 					{
 						m_cameraMachine.performAction(Action_Camera_SnapToPoint.class);
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
 	
 	public void onStateEvent(smStateEvent event)
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
