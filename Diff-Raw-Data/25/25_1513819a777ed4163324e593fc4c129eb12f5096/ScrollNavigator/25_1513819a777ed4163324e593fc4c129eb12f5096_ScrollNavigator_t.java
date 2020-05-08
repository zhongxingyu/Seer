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
 import swarm.client.managers.CellAddressManager;
 import swarm.client.managers.CellBuffer;
 import swarm.client.managers.CellBufferManager;
 import swarm.client.managers.CellSizeManager;
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
 import swarm.shared.structs.CellAddressMapping;
 import swarm.shared.structs.CellSize;
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
 	private final Panel m_scrollContainerInner;
 	private final Panel m_mouseLayer;
 	
 	private final Point m_utilPoint1 = new Point();
 	private final Point m_utilPoint2 = new Point();
 	private final Point m_utilPoint3 = new Point();
 	
 	private final CellAddressMapping m_utilMapping = new CellAddressMapping();
 	private final Boolean m_utilBool1 = new Boolean();
 	private final Boolean m_utilBool2 = new Boolean();
 	private final Rect m_utilRect1 = new Rect();
 	private final Rect m_utilRect2 = new Rect();
 	private final CellSize m_utilCellSize = new CellSize();
 	
 	public ScrollNavigator(ViewContext viewContext, Panel scrollContainer, Panel scrollee, Panel mouseLayer)
 	{
 		m_viewContext = viewContext;
 		m_scrollContainer = scrollContainer;
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
 				State_ViewingCell viewingState =  m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 				if( viewingState != null )
 				{
 					setTargetLayout((VisualCell)viewingState.getCell().getVisualization());
 					m_viewContext.cellMngr.updateCellTransforms(0.0);
 				}
 				else
 				{
 					//--- DRK > I guess when we leave viewing state and reset scroll left/top to zero,
 					//---		that fires a scroll event, so valid case here...ASSERT removed for now.
 					//smU_Debug.ASSERT(false, "Expected viewing state to be entered.");
 				}
 			}
 			
 		}, ScrollEvent.getType());
 		
 		this.toggleScrollBars(null);
 	}
 	
 	public Panel getScrollContainer()
 	{
 		return m_scrollContainer;
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
 		
 		if( viewingState != null || snappingState != null )
 		{
 			//s_logger.severe(m_viewContext.appContext.cameraMngr.getCamera().getPosition()+"");
 			
 			GridCoordinate coord = null;
 			
 			if( viewingState != null )
 			{
 				coord = viewingState.getCell().getCoordinate();
 			}
 			else
 			{
 				coord = snappingState.getTargetCoord();
 			}
 			
 			this.updateCameraViewRect(false, false);
 			
 			A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 			grid.calcCoordTopLeftPoint(coord, 1, m_utilPoint1);
 			
 			double cellHudHeight = m_viewContext.appConfig.cellHudHeight;
 			double viewWidth = m_viewContext.appContext.cameraMngr.getCamera().getViewWidth();
 			double viewHeight = m_viewContext.appContext.cameraMngr.getCamera().getViewHeight();
 			
 			U_CameraViewport.calcConstrainedCameraPoint(grid, coord, m_utilPoint1, viewWidth, viewHeight, cellHudHeight, m_utilPoint1);
 			
 			this.adjustTargetSnapPoint_private(coord, m_utilPoint1);
 			
 			this.setTargetLayout(coord);
 			
 			if( viewingState == null )
 			{
 				m_args_SnapToCoord.init(coord, m_utilPoint1);
 				snappingState.getParent().performAction(Action_Camera_SnapToCoordinate.class, m_args_SnapToCoord);
 			}
 			else
 			{
 				m_args_SnapToPoint.init(m_utilPoint1, true, false);
 				viewingState.getParent().performAction(Action_Camera_SnapToPoint.class, m_args_SnapToPoint);
 			}
 			
 			//s_logger.severe(m_viewContext.appContext.cameraMngr.getCamera().getPosition()+"");
 		}
 		/*else if( viewingState != null )
 		{
 			this.updateCameraViewRect(true, false);
 			//updateCameraFromScrollBars();
 			this.setTargetLayout((VisualCell)viewingState.getCell().getVisualization());
 		}*/
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
 	
 	/*private void updateCameraFromScrollBars()
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
 	}*/
 	
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
 	
 	private void putScrollBarX(double size)
 	{
 		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
 		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
 		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
 		
 		scrollerStyle.setOverflowX(Overflow.SCROLL);
 		String widthProperty = size+"px";
 		innerStyle.setProperty("width", widthProperty);
 		mouseLayerStyle.setProperty("width", widthProperty);
 	}
 	
 	private void putScrollBarY(double size)
 	{
 		Style scrollerStyle = this.m_scrollContainer.getElement().getStyle();
 		Style innerStyle = this.m_scrollContainerInner.getElement().getStyle();
 		Style mouseLayerStyle = this.m_mouseLayer.getElement().getStyle();
 		
 		scrollerStyle.setOverflowY(Overflow.SCROLL);
 		String heightProperty = size+"px";
 		innerStyle.setProperty("height", heightProperty);
 		mouseLayerStyle.setProperty("height", heightProperty);
 	}
 	
 	private void toggleScrollBars(State_ViewingCell viewingState_nullable)
 	{
 		if( viewingState_nullable != null && viewingState_nullable.isEntered() )
 		{			
 			//A_Grid grid = viewingState_nullable.getCell().getGrid();
 			GridCoordinate targetCoord = viewingState_nullable.getTargetCoord();
 			//Point cameraPoint = m_viewContext.appContext.cameraMngr.getCamera().getPosition();
 			//Point centerPoint = m_utilPoint1;
 			//U_CameraViewport.calcViewWindowCenter(grid, viewingState_nullable.getCell().getCoordinate(), m_cellHudHeight, centerPoint);
 			
 			this.calcWindowLayout(targetCoord, m_utilRect1, m_utilRect2, m_utilBool1, m_utilBool2);
 	
 			if( m_utilBool1.value )
 			{
 				putScrollBarX(m_utilRect2.getWidth());
 			}
 			else
 			{
 				clearScrollbarX();
 			}
 			
 			if( m_utilBool2.value )
 			{
 				putScrollBarY(m_utilRect2.getHeight());
 			}
 			else
 			{
 				clearScrollbarY();
 			}
 		}
 		else
 		{
 			clearScrollbarX();
 			clearScrollbarY();
 		}
 	}
 	
 	private double getWindowWidthSansScroll()
 	{
 		return m_scrollContainer.getElement().getOffsetWidth();
 	}
 	
 	private double getWindowHeightSansScroll()
 	{
 		return m_scrollContainer.getElement().getOffsetHeight();
 	}
 	
 	public double getWindowWidth()
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
 	
 	public double getWindowHeight()
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
 	
 	private void calcCellSizeRequirement(GridCoordinate coord, Rect rect_out)
 	{
 		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 		CellSizeManager cellSizeMngr = m_viewContext.appContext.cellSizeMngr;
 		m_utilMapping.getCoordinate().copy(coord);
 		
 		if( cellSizeMngr.getCellSizeFromLocalSource(m_utilMapping, m_utilCellSize) )
 		{
 			rect_out.set(m_utilCellSize.getWidth(), m_utilCellSize.getHeight());
 		}
 		else
 		{
 			rect_out.set(grid.getCellWidth(), grid.getCellHeight());
 		}
 	}
 	
 	public void calcScrollWindowRect(GridCoordinate coord, Rect rect_out)
 	{
 		this.calcWindowLayout(coord, rect_out, m_utilRect2, m_utilBool1, m_utilBool2);
 	}
 	
 	private void calcWindowLayout(GridCoordinate coord, Rect window_out, Rect cell_out, Boolean widthSmaller_out, Boolean heightSmaller_out)
 	{
 		A_Grid grid = this.m_viewContext.appContext.gridMngr.getGrid();
 		
 		this.calcCellSizeRequirement(coord, cell_out);
 		double cellWidthReq = U_CameraViewport.calcCellWidthRequirement(grid, cell_out.getWidth());
 		double cellHeightReq = U_CameraViewport.calcCellHeightRequirement(grid, cell_out.getHeight(), m_cellHudHeight);
 		cell_out.set(cellWidthReq, cellHeightReq);
 		
 		window_out.set(this.getWindowWidthSansScroll(), this.getWindowHeightSansScroll());
 
 		widthSmaller_out.value = false;
 		heightSmaller_out.value = false;
 		
 		if( window_out.getWidth() < cellWidthReq )
 		{
 			window_out.incHeight(-m_scrollBarWidthDiv2*2);
 			widthSmaller_out.value = true;
 		}
 
 		if( window_out.getHeight() < cellHeightReq )
 		{
 			window_out.incWidth(-m_scrollBarWidthDiv2*2);
 			heightSmaller_out.value = true;
 		}
 		
 		if( !widthSmaller_out.value && window_out.getWidth() < cellWidthReq )
 		{
 			window_out.incHeight(-m_scrollBarWidthDiv2*2);			
 			widthSmaller_out.value = true;
 		}
 	}
 	
 	public boolean isScrollingX()
 	{
 		return this.m_scrollContainer.getElement().getScrollWidth() > this.m_scrollContainer.getElement().getClientWidth();
 		/*if( m_currentGrid == null )  return false;
 		
 		double minViewWidth = U_CameraViewport.calcCellWidthRequirement(m_currentGrid);
 		double windowWidth = this.getWindowWidth();
 		
 		return windowWidth < minViewWidth;*/
 	}
 	
 	public boolean isScrollingY()
 	{
 		return this.m_scrollContainer.getElement().getScrollHeight() > this.m_scrollContainer.getElement().getClientHeight();
 		
 		/*if( m_currentGrid == null )  return false;
 		
 		double minViewHeight = U_CameraViewport.calcCellHeightRequirement(m_currentGrid, m_cellHudHeight);
 		double windowHeight = this.getWindowHeight();
 		
 		return windowHeight < minViewHeight;*/
 	}
 	
 	public void adjustTargetSnapPoint(Action_Camera_SnapToCoordinate.Args args)
 	{
 		if( args.userData == this.getClass() )   return;
 		
 		this.adjustTargetSnapPoint_private(args.getTargetCoordinate(), args.getTargetPoint());
 	}
 	
 	public void adjustTargetSnapPoint_private(GridCoordinate targetCoord, Point point_out)
 	{
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		if( viewingState != null )
		{
			if( !targetCoord.isEqualTo(viewingState.getCell().getCoordinate()) )
			{
				viewingState = null;
			}
		}
		
 		A_Grid grid = this.m_viewContext.appContext.gridMngr.getGrid();
 		
 		this.calcWindowLayout(targetCoord, m_utilRect1, m_utilRect2, m_utilBool1, m_utilBool2);
 		
 		double newWindowWidth = m_utilRect1.getWidth();
 		double newWindowHeight = m_utilRect1.getHeight();
 		boolean widthSmaller = m_utilBool1.value;
 		boolean heightSmaller = m_utilBool2.value;
 
 		if( widthSmaller && heightSmaller )
 		{
 			if( viewingState == null )
 			{
 				double windowWidthSansScroll = this.getWindowWidthSansScroll();
 				double windowHeightSansScroll = this.getWindowHeightSansScroll();
 				double defaultCellWidthReq = U_CameraViewport.calcCellWidthRequirement(grid);
 				double defaultCellHeightReq = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
 				
 				if( windowHeightSansScroll > defaultCellHeightReq )
 				{
 					targetCoord.calcPoint(m_utilPoint3, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
 					double paddingTop = U_CameraViewport.calcCellPaddingTop(grid, m_cellHudHeight);
 					m_utilPoint3.incY(-paddingTop);
 					m_utilPoint3.incY(newWindowHeight/2);
 					m_utilPoint3.incY(m_scrollBarWidthDiv2);
 					
 					double offset = Math.min((m_utilRect2.getHeight() - defaultCellHeightReq)/2, (newWindowHeight - defaultCellHeightReq)/2);
 					offset = Math.max(0, offset);
 					m_utilPoint3.incY(-offset);
 					point_out.setY(m_utilPoint3.getY());
 				}
 				
 				if( windowWidthSansScroll > defaultCellWidthReq )
 				{
 					targetCoord.calcPoint(m_utilPoint3, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
 					double paddingLeft = grid.getCellPadding();
 					m_utilPoint3.incX(-paddingLeft);
 					m_utilPoint3.incX(newWindowWidth/2);
 					m_utilPoint3.incX(m_scrollBarWidthDiv2);
 
 					double offset = Math.min((m_utilRect2.getWidth() - defaultCellWidthReq)/2, (newWindowWidth - defaultCellWidthReq)/2);
 					offset = Math.max(0, offset);
 					m_utilPoint3.incX(-offset);
 					point_out.setX(m_utilPoint3.getX());
 				}
 			}
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
 			case DID_UPDATE:
 			{
 				if ( event.getState() instanceof State_ViewingCell )
 				{
 					//if( event.getState().getUpdateCount() % 5 == 0 )
 						//s_logger.severe(m_viewContext.appContext.cameraMngr.getCamera().getPosition() + "");
 				}
 				
 				break;
 			}
 			
 			case DID_ENTER:
 			{
 				if ( event.getState() instanceof State_ViewingCell )
 				{
 					State_ViewingCell state = event.getState();
 					
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
 					m_scrollContainer.getElement().setScrollLeft(0);
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
						boolean maintainApparentPosition = !event.getContext().isEntered(State_CameraSnapping.class);
						this.updateCameraViewRect(true, maintainApparentPosition);
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if (event.getAction() == Event_Camera_OnCellSizeFound.class )
 				{
 					this.onResize();
 				}
 				
 				break;
 			}
 		}
 	}
 	
 	private void setTargetLayout(GridCoordinate gridCoord)
 	{
 		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
 		CellBuffer cellBuffer = cellManager.getDisplayBuffer();
 		if( cellBuffer.isInBoundsAbsolute(gridCoord) )
 		{
 			BufferCell bufferCell = cellBuffer.getCellAtAbsoluteCoord(gridCoord);
 			VisualCell visualCell = (VisualCell) bufferCell.getVisualization();
 			this.setTargetLayout(visualCell);		
 		}
 	}
 	
 	public void calcTargetLayout(CellSize cellSize, GridCoordinate targetCoord, Point topLeftOffset_out, Rect size_out)
 	{
 		this.calcScrollWindowRect(targetCoord, m_utilRect1);
 		
 		this.calcTargetLayout(cellSize, m_utilRect1, topLeftOffset_out, size_out);
 		//s_logger.severe(topLeftOffset_out + " " + size_out);
 	}
 	
 	private void calcTargetLayout(CellSize cellSize, Rect windowSize, Point topLeftOffset_out, Rect size_out)
 	{
 		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 		double defaultWidthReq = U_CameraViewport.calcCellWidthRequirement(grid);
 		double defaultHeightReq = U_CameraViewport.calcCellHeightRequirement(grid, m_cellHudHeight);
 		double roomToTheLeft = (windowSize.getWidth() - defaultWidthReq)/2;
 		roomToTheLeft = Math.max(roomToTheLeft, 0);
 		double roomToTheTop = (windowSize.getHeight() - defaultHeightReq)/2;
 		roomToTheTop = Math.max(roomToTheTop, 0);
 		
 		int targetWidth = cellSize.getWidth();
 		int targetHeight = cellSize.getHeight();
 		int defaultWidth = grid.getCellWidth();
 		int defaultHeight = grid.getCellHeight();
 		
 		int sideWidth = (targetWidth - defaultWidth)/2;
 		int xOffset = (int) -Math.min(sideWidth, roomToTheLeft);
 		int topHeight = (targetHeight - defaultHeight)/2;
 		int yOffset = (int) -Math.min(topHeight, roomToTheTop);
 		
 		topLeftOffset_out.set(xOffset, yOffset, 0);
 		size_out.set(targetWidth, targetHeight);
 	}
 	
 	public void setTargetLayout(VisualCell visualCell)
 	{
 		BufferCell bufferCell = visualCell.getBufferCell();
 		
 		if( !bufferCell.getFocusedCellSize().isValid() )  return;
 		
 		this.calcTargetLayout(bufferCell.getFocusedCellSize(), bufferCell.getCoordinate(), m_utilPoint2, m_utilRect1);
 		m_utilPoint2.incX(-this.getScrollX());
 		m_utilPoint2.incY(-this.getScrollY());
 		
 		visualCell.setTargetLayout((int)m_utilRect1.getWidth(), (int)m_utilRect1.getHeight(), (int)m_utilPoint2.getX(), (int)m_utilPoint2.getY());
 	}
 }
