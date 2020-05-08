 package swarm.client.navigation;
 
 import java.util.logging.Logger;
 
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.event.dom.client.ScrollHandler;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.Camera;
 import swarm.client.managers.CameraManager;
 import swarm.client.managers.CellBuffer;
 import swarm.client.managers.CellBufferManager;
 import swarm.client.states.camera.Action_Camera_SetViewSize;
 import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Event_Camera_OnCellSizeFound;
 import swarm.client.states.camera.Event_GettingMapping_OnResponse;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.view.U_Css;
 import swarm.client.view.ViewContext;
 import swarm.client.view.cell.VisualCell;
 import swarm.client.view.cell.VisualCellContainer;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.lang.Boolean;
 import swarm.shared.statemachine.I_StateEventListener;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.Point;
 import swarm.shared.structs.Rect;
 import swarm.shared.utils.U_Math;
 
 public class ScrollNavigator implements I_StateEventListener
 {
 	private static final Logger s_logger = Logger.getLogger(ScrollNavigator.class.getName());
 	
 	private final Action_Camera_SnapToPoint.Args m_args_SnapToPoint = new Action_Camera_SnapToPoint.Args();
 	private final Action_Camera_SnapToCoordinate.Args m_args_SnapToCoord = new Action_Camera_SnapToCoordinate.Args();
 	private final Action_Camera_SetViewSize.Args m_args_SetCameraViewSize = new Action_Camera_SetViewSize.Args();
 	
 	private final ViewContext m_viewContext;
 	private final int m_scrollBarWidthDiv2;
 	private final double m_cellHudHeight;
 	
 	private final Panel m_scrollContainer;
 	private final Widget m_scrollContainerParent;
 	private final Panel m_scrollContainerInner;
 	private final Panel m_mouseLayer;
 	
 	private final Point m_utilPoint1 = new Point();
 	
 	private A_Grid m_currentGrid = null;
 	
 	private final Boolean m_utilBool1 = new Boolean();
 	private final Boolean m_utilBool2 = new Boolean();
 	private final Rect m_utilRect1 = new Rect();
 	
 	public ScrollNavigator(ViewContext viewContext, Panel scrollContainer, Panel scrollee, Panel mouseLayer)
 	{
 		m_viewContext = viewContext;
 		m_scrollContainer = scrollContainer;
 		m_scrollContainerParent = scrollContainer.getParent();
 		m_scrollContainerInner = scrollee;
 		m_mouseLayer = mouseLayer;
 		m_scrollBarWidthDiv2 = (int) Math.floor(((double)U_Css.getScrollBarWidth())/2);
 		m_cellHudHeight = viewContext.appConfig.cellHudHeight;
 		
 		m_args_SnapToCoord.userData = this.getClass();
 		
 		m_scrollContainer.addDomHandler(new ScrollHandler()
 		{
 			@Override
 			public void onScroll(ScrollEvent event)
 			{
 				updateCameraFromScrollBars();
 			}
 			
 		}, ScrollEvent.getType());
 		
 		this.toggleScrollBars(null);
 	}
 	
 	public int getScrollX()
 	{
 		return m_scrollContainer.getElement().getScrollLeft();
 	}
 	
 	public int getScrollY()
 	{
 		return m_scrollContainer.getElement().getScrollTop();
 	}
 	
 	public void onResize()
 	{
 		State_ViewingCell viewingState =  m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 		State_CameraSnapping snappingState = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
 		
 		this.toggleScrollBars(viewingState);
 		
 		if( snappingState != null )
 		{
 			this.updateCameraViewRect(false, false);
 			
 			A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 			grid.calcCoordTopLeftPoint(snappingState.getTargetCoordinate(), 1, m_utilPoint1);
 			
 			double cellHudHeight = m_viewContext.appConfig.cellHudHeight;
 			double viewWidth = m_viewContext.appContext.cameraMngr.getCamera().getViewWidth();
 			double viewHeight = m_viewContext.appContext.cameraMngr.getCamera().getViewHeight();
 			
 			U_CameraViewport.calcConstrainedCameraPoint(grid, snappingState.getTargetCoordinate(), m_utilPoint1, viewWidth, viewHeight, cellHudHeight, m_utilPoint1);
 			
 			this.adjustSnapTargetPoint_private(snappingState.getTargetCoordinate(), m_utilPoint1);
 			
 			m_args_SnapToCoord.init(snappingState.getTargetCoordinate(), m_utilPoint1);
 			snappingState.getParent().performAction(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoord);
 			
 			this.updateLayout(snappingState.getTargetCoordinate());
 		}
 		else if( viewingState != null )
 		{
 			this.updateCameraViewRect(false, false);
 			updateCameraFromScrollBars();
			this.updateLayout((VisualCell)viewingState.getCell().getVisualization());
 		}
 		else
 		{
 			this.updateCameraViewRect(true, false);
 		}
 	}
 	
 	private void updateCameraViewRect(boolean updateBuffer, boolean maintainApparentPosition)
 	{
 		m_args_SetCameraViewSize.init(this.getWindowWidth(), this.getWindowHeight(), updateBuffer, maintainApparentPosition);
 		m_viewContext.stateContext.performAction(Action_Camera_SetViewSize.class, m_args_SetCameraViewSize);
 	}
 	
 	private void updateCameraFromScrollBars()
 	{
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 		
 		if( viewingState == null )
 		{
 			//--- DRK > I guess when we leave viewing state and reset scroll left/top to zero,
 			//---		that fires a scroll event, so valid case here...ASSERT removed for now.
 			//smU_Debug.ASSERT(false, "Expected viewing state to be entered.");
 			
 			return;
 		}
 		
 		A_Grid grid = viewingState.getCell().getGrid();
 		
 		double minViewWidth = U_CameraViewport.calcCellWidthRequirement(grid);
 		double windowWidth = this.getWindowWidth();
 		double minViewHeight = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
 		double windowHeight = this.getWindowHeight();
 		GridCoordinate coord = viewingState.getCell().getCoordinate();
 		Point centerPoint = m_utilPoint1;
 		U_CameraViewport.calcViewWindowCenter(grid, viewingState.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
 		
 		if( windowWidth < minViewWidth )
 		{
 			int scroll = m_scrollContainer.getElement().getScrollLeft();
 			double newPos = centerPoint.getX() - (minViewWidth - windowWidth)/2 + scroll;
 			centerPoint.setX(newPos);
 		}
 		
 		if( windowHeight < minViewHeight )
 		{
 			int scroll = m_scrollContainer.getElement().getScrollTop();
 			double newPos = centerPoint.getY() - (minViewHeight - windowHeight)/2 + scroll;
 			centerPoint.setY(newPos);
 		}
 		
 		U_CameraViewport.calcConstrainedCameraPoint(grid, coord, centerPoint, windowWidth, windowHeight, m_cellHudHeight, centerPoint);
 		
 		m_args_SnapToPoint.init(centerPoint, true, false);
 		m_viewContext.stateContext.performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 	}
 	
 	private void clearScrollbarX()
 	{
 		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
 		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
 		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
 		
 		scrollerStyle.setOverflowX(Overflow.HIDDEN);
 		innerStyle.clearProperty("width");
 		mouseLayerStyle.clearProperty("width");
 		m_scrollContainer.getElement().setScrollLeft(0);
 	}
 	
 	private void clearScrollbarY()
 	{
 		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
 		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
 		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
 		
 		scrollerStyle.setOverflowY(Overflow.HIDDEN);
 		innerStyle.clearProperty("height");
 		mouseLayerStyle.clearProperty("height");
 		m_scrollContainer.getElement().setScrollTop(0);
 	}
 	
 	private void setInitialScrollbarPosX(State_ViewingCell viewingState)
 	{
 		A_Grid grid = viewingState.getCell().getGrid();
 		Point cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
 		Point centerPoint = m_utilPoint1;
 		U_CameraViewport.calcViewWindowCenter(grid, viewingState.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
 		double minViewWidth = U_CameraViewport.calcCellWidthRequirement(grid);
 		double windowWidth = this.getWindowWidth();
 		
 		double delta = (minViewWidth - windowWidth)/2;
 		double cameraPos = U_Math.clamp(cameraPoint.getX(), centerPoint.getX()-delta, centerPoint.getX()+delta);
 		double scroll = (cameraPos - (centerPoint.getX()-delta));
 		m_scrollContainer.getElement().setScrollLeft((int) Math.round(scroll));
 	}
 	
 	private void setInitialScrollbarPosY(State_ViewingCell viewingState)
 	{
 		A_Grid grid = viewingState.getCell().getGrid();
 		Point cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
 		Point centerPoint = m_utilPoint1;
 		U_CameraViewport.calcViewWindowCenter(grid, viewingState.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
 		double minViewHeight = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
 		double windowHeight = this.getWindowHeight();
 		
 		double delta = (minViewHeight - windowHeight)/2;
 		double cameraPos = U_Math.clamp(cameraPoint.getY(), centerPoint.getY()-delta, centerPoint.getY()+delta);
 		double scroll = (cameraPos - (centerPoint.getY()-delta));
 		int scrollRounded = (int) Math.round(scroll);
 		m_scrollContainer.getElement().setScrollTop(scrollRounded);
 	}
 	
 	private void toggleScrollBarX(State_ViewingCell viewingState, double minViewWidth, Point cameraPoint, Point centerPoint)
 	{
 		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
 		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
 		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
 		
 		double windowWidth = this.getWindowWidth();
 		
 		if( windowWidth < minViewWidth )
 		{			
 			scrollerStyle.setOverflowX(Overflow.SCROLL);
 			String widthProperty = minViewWidth+"px";
 			innerStyle.setProperty("width", widthProperty);
 			mouseLayerStyle.setProperty("width", widthProperty);
 		}
 		else
 		{
 			clearScrollbarX();
 		}
 	}
 	
 	private void toggleScrollBarY(State_ViewingCell viewingState, double minViewHeight, Point cameraPoint, Point centerPoint)
 	{
 		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
 		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
 		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
 		
 		double windowHeight = this.getWindowHeight();
 		
 		if( windowHeight < minViewHeight )
 		{			
 			scrollerStyle.setOverflowY(Overflow.SCROLL);
 			String heightProperty = minViewHeight+"px";
 			innerStyle.setProperty("height", heightProperty);
 			mouseLayerStyle.setProperty("height", heightProperty);
 		}
 		else
 		{
 			clearScrollbarY();
 		}
 	}
 	
 	private void toggleScrollBars(State_ViewingCell viewingState_nullable)
 	{
 		if( viewingState_nullable != null && viewingState_nullable.isEntered() )
 		{			
 			A_Grid grid = viewingState_nullable.getCell().getGrid();
 			Point cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
 			Point centerPoint = m_utilPoint1;
 			U_CameraViewport.calcViewWindowCenter(grid, viewingState_nullable.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
 			
 			double minViewWidth = U_CameraViewport.calcCellWidthRequirement(grid);
 			double minViewHeight = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
 			
 			toggleScrollBarX(viewingState_nullable, minViewWidth, cameraPoint, centerPoint);
 			toggleScrollBarY(viewingState_nullable, minViewHeight, cameraPoint, centerPoint);
 			
 			//--- DRK > Pretty sure that we have to recheck the x scroll bar to cover fringe
 			//---		cases where the appearance of the y scroll bar diminishes the view width
 			//---		to the point where the X scroll bar is in fact needed after all.
 			toggleScrollBarX(viewingState_nullable, minViewWidth, cameraPoint, centerPoint);
 		}
 		else
 		{
 			clearScrollbarX();
 			clearScrollbarY();
 		}
 	}
 	
 	private double getWindowWidth()
 	{
 		double value = this.m_scrollContainer.getElement().getClientWidth();
 		
 		if( value == 0 )
 		{
 			if( m_scrollContainer.getElement().getOffsetWidth() != 0 )
 			{
 				return 0;
 			}
 			else
 			{
 				return m_viewContext.splitPanel.getCellPanelWidth();
 			}
 		}
 		else
 		{
 			return value;
 		}
 	}
 	
 	private double getWindowHeight()
 	{
 		double value = this.m_scrollContainer.getElement().getClientHeight();
 		
 		if( value == 0 )
 		{
 			if( m_scrollContainer.getElement().getOffsetHeight() != 0 )
 			{
 				return 0;
 			}
 			else
 			{
 				return RootPanel.get().getOffsetHeight();
 			}
 		}
 		else
 		{
 			return value;
 		}
 	}
 	
 	public void getScrollableWindow(Rect rect_out)
 	{
 		this.getScrollableWindow(rect_out, m_utilBool1, m_utilBool2);
 	}
 	
 	private void getScrollableWindow(Rect rect_out, Boolean widthSmaller_out, Boolean heightSmaller_out)
 	{
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 		A_Grid grid = this.m_viewContext.appContext.gridMngr.getGrid();
 		
 		double minViewWidth = U_CameraViewport.calcCellWidthRequirement(grid);
 		double minViewHeight = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
 		rect_out.set(this.getWindowWidth(), this.getWindowHeight());
 
 		widthSmaller_out.value = false;
 		heightSmaller_out.value = false;
 		
 		if( rect_out.getWidth() < minViewWidth )
 		{
 			if( viewingState == null )
 			{
 				rect_out.incHeight(-m_scrollBarWidthDiv2*2);
 			}
 			
 			widthSmaller_out.value = true;
 		}
 
 		if( rect_out.getHeight() < minViewHeight )
 		{
 			if( viewingState == null )
 			{
 				rect_out.incWidth(-m_scrollBarWidthDiv2*2);
 			}
 
 			heightSmaller_out.value = true;
 		}
 		
 		if( !widthSmaller_out.value && rect_out.getWidth() < minViewWidth )
 		{
 			if( viewingState == null )
 			{
 				rect_out.incHeight(-m_scrollBarWidthDiv2*2);
 			}
 			
 			widthSmaller_out.value = true;
 		}
 	}
 	
 	public boolean isScrollingX()
 	{
 		if( m_currentGrid == null )  return false;
 		
 		double minViewWidth = U_CameraViewport.calcCellWidthRequirement(m_currentGrid);
 		double windowWidth = this.getWindowWidth();
 		
 		return windowWidth < minViewWidth;
 	}
 	
 	public boolean isScrollingY()
 	{
 		if( m_currentGrid == null )  return false;
 		
 		double minViewHeight = U_CameraViewport.calcCellHeightRequirement(m_currentGrid, m_cellHudHeight);
 		double windowHeight = this.getWindowHeight();
 		
 		return windowHeight < minViewHeight;
 	}
 	
 	public void adjustSnapTargetPoint(Action_Camera_SnapToCoordinate.Args args)
 	{
 		if( args.userData == this.getClass() )   return;
 		
 		this.adjustSnapTargetPoint_private(args.getTargetCoordinate(), args.getTargetPoint());
 	}
 	
 	public void adjustSnapTargetPoint_private(GridCoordinate targetCoord, Point point_out)
 	{
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 		A_Grid grid = this.m_viewContext.appContext.gridMngr.getGrid();
 		
 		this.getScrollableWindow(m_utilRect1, m_utilBool1, m_utilBool2);
 		
 		double newWindowWidth = m_utilRect1.getWidth();
 		double newWindowHeight = m_utilRect1.getHeight();
 		boolean widthSmaller = m_utilBool1.value;
 		boolean heightSmaller = m_utilBool2.value;
 
 		if( widthSmaller && heightSmaller )
 		{
 		}
 		else if( widthSmaller )
 		{
 			if( viewingState == null )
 			{
 				point_out.incY(m_scrollBarWidthDiv2);
 			}
 			
 			newWindowHeight = 0;
 			
 			U_CameraViewport.calcConstrainedCameraPoint(grid, targetCoord, point_out, newWindowWidth, newWindowHeight, m_cellHudHeight, point_out);
 		}
 		else if( heightSmaller )
 		{
 			if( viewingState == null )
 			{
 				point_out.incX(m_scrollBarWidthDiv2);
 			}
 			
 			newWindowWidth = 0;
 			
 			U_CameraViewport.calcConstrainedCameraPoint(grid, targetCoord, point_out, newWindowWidth, newWindowHeight, m_cellHudHeight, point_out);
 		}
 	}
 	
 	@Override
 	public void onStateEvent(StateEvent event)
 	{
 		switch(event.getType())
 		{
 			/*case DID_UPDATE:
 			{
 				if ( event.getState() instanceof State_ViewingCell )
 				{
 					s_logger.severe(m_viewContext.appContext.cameraMngr.getCamera().getPosition() + "");
 				}
 				
 				break;
 			}*/
 			
 			case DID_ENTER:
 			{
 				if ( event.getState() instanceof State_ViewingCell )
 				{
 					State_ViewingCell state = event.getState();
 					
 					m_currentGrid = state.getCell().getGrid();
 					
 					toggleScrollBars(state);
 					
 					boolean isScrollingX = isScrollingX();
 					boolean isScrollingY = isScrollingY();
 
 					if( isScrollingX || isScrollingY )
 					{
 						this.updateCameraViewRect(true,  true);
 						
 						if( isScrollingX )
 						{
 							this.setInitialScrollbarPosX(state);
 						}
 						
 						if( isScrollingY )
 						{
 							this.setInitialScrollbarPosY(state);
 						}
 					}
 					
 					m_scrollContainer.getElement().setScrollTop(0);
 				}
 				
 				break;
 			}
 
 			case DID_EXIT:
 			{
 				if ( event.getState() instanceof State_ViewingCell )
 				{
 					boolean isScrollingX = isScrollingX();
 					boolean isScrollingY = isScrollingY();
 					
 					toggleScrollBars((State_ViewingCell) event.getState());
 
 					if( isScrollingX || isScrollingY )
 					{
 						this.updateCameraViewRect(true, true);
 					}
 					
 					m_currentGrid = null;
 				}
 				
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if (event.getAction() == Event_Camera_OnCellSizeFound.class )
 				{
 					Event_Camera_OnCellSizeFound.Args args = event.getActionArgs();
 					
 					this.updateLayout(args.getMapping().getCoordinate());
 				}
 				
 				break;
 			}
 		}
 	}
 	
 	private void updateLayout(GridCoordinate gridCoord)
 	{
 		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
 		CellBuffer cellBuffer = cellManager.getDisplayBuffer();
 		if( cellBuffer.isInBoundsAbsolute(gridCoord) )
 		{
 			BufferCell bufferCell = cellBuffer.getCellAtAbsoluteCoord(gridCoord);
 			VisualCell visualCell = (VisualCell) bufferCell.getVisualization();
 			this.updateLayout(visualCell);		
 		}
 	}
 	
 	public void updateLayout(VisualCell visualCell)
 	{
 		BufferCell bufferCell = visualCell.getBufferCell();
 		
 		if( !bufferCell.getFocusedCellSize().isValid() )  return;
 		
 		A_Grid grid = bufferCell.getGrid();
 		this.getScrollableWindow(m_utilRect1);
 		double widthReq = U_CameraViewport.calcCellWidthRequirement(grid);
 		double heightReq = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
 		double roomToTheLeft = (m_utilRect1.getWidth() - widthReq)/2;
 		roomToTheLeft = Math.max(roomToTheLeft, 0);
 		double roomToTheTop = (m_utilRect1.getHeight() - heightReq)/2;
 		roomToTheTop = Math.max(roomToTheTop, 0);
 		
 		visualCell.setTargetLayout(bufferCell.getFocusedCellSize().getWidth(), bufferCell.getFocusedCellSize().getHeight(), (int)roomToTheLeft, (int)roomToTheTop);
 	}
 }
