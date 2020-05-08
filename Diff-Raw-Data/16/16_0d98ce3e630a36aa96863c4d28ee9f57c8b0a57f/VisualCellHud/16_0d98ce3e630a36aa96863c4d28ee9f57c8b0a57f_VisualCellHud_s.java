 package swarm.client.view.cell;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.ClientAppConfig;
 import swarm.client.app.AppContext;
 import swarm.client.entities.Camera;
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.E_CodeStatus;
 import swarm.client.input.ClickManager;
 import swarm.client.input.I_ClickHandler;
 import swarm.client.managers.CameraManager;
 import swarm.client.managers.CellBuffer;
 import swarm.client.managers.CellBufferManager;
 import swarm.client.navigation.BrowserNavigator;
 import swarm.client.navigation.BrowserNavigator.I_StateChangeListener;
 import swarm.client.navigation.U_CameraViewport;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Action_Camera_SetViewSize;
 import swarm.client.states.camera.Action_Camera_SnapToAddress;
 import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
 import swarm.client.states.camera.Action_ViewingCell_Refresh;
 import swarm.client.states.camera.Event_Camera_OnCellSizeFound;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraFloating;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_GettingMapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.states.code.Action_EditingCode_Preview;
 import swarm.client.states.code.Action_EditingCode_Save;
 import swarm.client.view.E_ZIndex;
 import swarm.client.view.I_UIElement;
 import swarm.client.view.U_Css;
 import swarm.client.view.ViewContext;
 import swarm.client.view.tooltip.E_ToolTipType;
 import swarm.client.view.tooltip.ToolTipConfig;
 import swarm.client.view.tooltip.ToolTipManager;
 import swarm.client.view.widget.SpriteButton;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.entities.E_CodeType;
 import swarm.shared.statemachine.A_Action;
 import swarm.shared.statemachine.A_State;
 import swarm.shared.statemachine.E_StateTimeType;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.CellAddressMapping;
 import swarm.shared.structs.CellSize;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.Point;
 import swarm.shared.structs.Rect;
 import swarm.shared.structs.Vector;
 import swarm.shared.utils.U_Math;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.event.dom.client.ScrollHandler;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
 
 public class VisualCellHud extends FlowPanel implements I_UIElement
 {
 	private static final Logger s_logger = Logger.getLogger(VisualCellHud.class.getName());
 	
 	private static final Point s_utilPoint1 = new Point();
 	private static final Point s_utilPoint2 = new Point();
 	private static final Vector s_utilVector = new Vector();
 	
 	private final ViewContext m_viewContext;
 	
 	private final ClientAppConfig m_appConfig;
 	
 	private final double m_minWidth = 176;// TODO: GHETTO
 	private double m_width = 0;
 	private double m_baseWidth = 0;
 	private double m_targetWidth = 0;
 	private double m_baseWidthProgress = 0;
 	
 	private double m_basePositionProgress = 0;
 	private final Point m_position = new Point();
 	private final Point m_basePosition = new Point();
 	private final Point m_targetPosition = new Point();
 	
 	private double m_baseAlpha;
 	private double m_alpha;
 	private final double m_fadeOutTime_seconds;
 	
 	private final CellAddressMapping m_utilMapping = new CellAddressMapping();
 	private final Rect m_utilRect = new Rect();
 	private final CellSize m_utilCellSize = new CellSize();
 	
 	private final GridCoordinate m_lastTargetCoord = new GridCoordinate();
 	
 	private final VisualCellHudInner m_innerContainer;
 	
 	private double m_height;
 	
 	private int m_scrollX;
 	private int m_scrollY;
 	
 	public VisualCellHud(ViewContext viewContext, ClientAppConfig appConfig)
 	{
 		m_innerContainer = new VisualCellHudInner(viewContext);
 		
 		m_viewContext = viewContext;
 		m_height = m_viewContext.appConfig.cellHudHeight;
 		m_appConfig = appConfig;
 		m_fadeOutTime_seconds = m_viewContext.config.hudFadeOutTime_seconds;
 		
 		m_alpha = m_baseAlpha = 0.0;
 		
 		this.addStyleName("sm_cell_hud");
 		
 		E_ZIndex.CELL_HUD.assignTo(this);
 		
 		this.setVisible(false);
 		this.getElement().getStyle().setProperty("minWidth", m_minWidth + "px");
 		U_Css.setTransformOrigin(this.getElement(), "0% 0%");
 		
 		this.add(m_innerContainer);
 		
 		m_viewContext.scrollNavigator.getScrollContainer().addDomHandler(new ScrollHandler()
 		{
 			@Override
 			public void onScroll(ScrollEvent event)
 			{
 				State_ViewingCell viewingState =  m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 				if( viewingState != null )
 				{
 					A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 					VisualCell visualCell = (VisualCell) viewingState.getCell().getVisualization();
 					visualCell.calcTopLeft(s_utilPoint1);
 					s_utilPoint1.incY(calcYOffsetFromCellTop(grid));
 					s_utilPoint1.incX(calcScrollXOffset(grid));
 					
 					setTargetPosition(s_utilPoint1);
 					ensureTargetPosition();
 					flushPosition();
 					
 					m_scrollX = m_viewContext.scrollNavigator.getScrollX();
 					m_scrollY = m_viewContext.scrollNavigator.getScrollY();
 				}
 				else
 				{
 					//--- DRK > I guess when we leave viewing state and reset scroll left/top to zero,
 					//---		that fires a scroll event, so valid case here...ASSERT removed for now.
 					//smU_Debug.ASSERT(false, "Expected viewing state to be entered.");
 				}
 			}
 			
 		}, ScrollEvent.getType());
 	}
 	
 	private void setAlpha(double alpha)
 	{
 		m_alpha = alpha <= 0 ? 0 : alpha;
 		this.getElement().getStyle().setOpacity(m_alpha);
 	}
 	
 	private double calcViewWidth(GridCoordinate coord_nullable, Camera camera, A_Grid grid)
 	{
 		double viewWidth;
 		
 		if( coord_nullable != null )
 		{
 			m_viewContext.scrollNavigator.calcScrollWindowRect(coord_nullable, m_utilRect);
 			viewWidth = m_utilRect.getWidth();
 		}
 		else
 		{
 			viewWidth = m_viewContext.scrollNavigator.getWindowWidth();
 		}
 			
 		viewWidth -= U_CameraViewport.getViewPadding(grid)*2;
 		
 		return viewWidth;
 	}
 	
 	private double calcScrollXOffset(A_Grid grid)
 	{
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 		
 		if( viewingState == null )  return 0.0;
 		
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		Element scrollElement = this.getParent().getElement();
 		double scrollX = scrollElement.getScrollLeft();
 		double toReturn = 0;
 		
 		double viewWidth = calcViewWidth(viewingState.getCell().getCoordinate(), camera, grid);
 		double hudWidth = m_width;
 		if( hudWidth > viewWidth )
 		{
 			double scrollWidth = scrollElement.getScrollWidth();
 			double clientWidth = scrollElement.getClientWidth();
 			double diff = (hudWidth - viewWidth) +  U_CameraViewport.getViewPadding(grid)/2.0;
 			double scrollRatio = scrollX / (scrollWidth-clientWidth);
 			toReturn -= diff * scrollRatio;
 		}
 		else
 		{
 			toReturn = scrollX*2;
 		}
 		
 		return toReturn;
 	}
 
 	
 	@Override
 	public void onStateEvent(StateEvent event)
 	{
 		switch( event.getType() )
 		{
 			case DID_ENTER:
 			{
 				if( event.getState() instanceof State_CameraSnapping )
 				{
 					if( event.getState().getPreviousState() != State_ViewingCell.class )
 					{						
 						this.setVisible(true);
 						m_baseAlpha = m_alpha;
 					}
 				}
 				else if( event.getState() instanceof State_ViewingCell )
 				{
 					this.setAlpha(1);
 					m_baseAlpha = m_alpha;
 					
 					this.ensureTargetWidth();
 					this.flushWidth();
 					
 					this.ensureTargetPosition();
 					this.flushPosition();
 				}
 				else if( event.getState() instanceof State_CameraFloating )
 				{
 					m_baseAlpha = m_alpha;
 					
 					A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 					this.setTargetWidth(null, grid.getCellWidth());
 					
 					m_lastTargetCoord.calcPoint(s_utilPoint2, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
 					s_utilPoint2.incY(calcYOffsetFromCellTop(grid));
 					this.setTargetPosition(s_utilPoint2);
 				}
 				
 				break;
 			}
 			
 			case DID_UPDATE:
 			{
 				if( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					if( event.getState() instanceof State_CameraSnapping )
 					{
 						State_CameraSnapping cameraSnapping = event.getState();
 						if( cameraSnapping.isEntered() )
 						{
 							if( cameraSnapping.getPreviousState() != State_ViewingCell.class )
 							{
 								double progress = cameraSnapping.getOverallSnapProgress();
 								this.setAlpha(m_baseAlpha + (1-m_baseAlpha) * progress);
 							}
 							
 							this.updateWidth();
 							this.updatePosition();
 						}
 					}
 					else if( event.getState() instanceof State_CameraFloating )
 					{
 						if( m_alpha <= 1 )
 						{
 							if( event.getState().isEntered() )
 							{
 								if( m_alpha > 0 )
 								{
 									A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 									double timeMantissa = event.getState().getTimeInState(E_StateTimeType.TOTAL) / m_fadeOutTime_seconds;
 									timeMantissa = U_Math.clamp(timeMantissa, 0, 1);
 									this.setAlpha(m_baseAlpha * (1-timeMantissa));
 									
 									this.updateWidth();
 									this.updatePosition();
 									
 									if( m_alpha <= 0 )
 									{
 										this.setVisible(false);
 									}
 								}
 							}
 						}
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_EXIT:
 			{
 				
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if( event.getAction() == Action_Camera_SetViewSize.class )
 				{
 					State_ViewingCell viewingState = event.getContext().getEnteredState(State_ViewingCell.class);
 					State_CameraSnapping snappingState = event.getContext().getEnteredState(State_CameraSnapping.class);
 					
 					if( viewingState != null )
 					{
 						this.setTargetWidth(viewingState.getCell().getCoordinate());
 						this.ensureTargetWidth();
 						this.flushWidth();
 						
 						Action_Camera_SetViewSize.Args args = event.getActionArgs();
 						if( args.updateBuffer() )
 						{
 							this.setPositionInstantly(viewingState.getCell().getCoordinate(), true);
 						}
 					}
 					else if( snappingState != null )
 					{
 						this.setTargetWidth(snappingState.getTargetCoord());
 						this.setTargetPosition(snappingState.getTargetCoord());
 					}
 				}
 				else if( event.getAction() == Action_Camera_SnapToPoint.class )
 				{
 					State_ViewingCell state = event.getContext().getEnteredState(State_ViewingCell.class);
 					
 					if( state != null )
 					{
 						Action_Camera_SnapToPoint.Args args = event.getActionArgs();
 						if( args.isInstant() )
 						{
 							this.setPositionInstantly(state.getCell().getCoordinate(), true);
 						}
 					}
 				}
 				else if( event.getAction() == Action_Camera_SnapToCoordinate.class )
 				{
 					Action_Camera_SnapToCoordinate.Args args = event.getActionArgs();
 					m_lastTargetCoord.copy(args.getTargetCoordinate());
 					
 					if( m_alpha <= 0 )
 					{
 						//--- Ensure that cell hud is the default size of grid cells.
 						A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 						this.setTargetWidth(args.getTargetCoordinate(), grid.getCellWidth());
 						this.ensureTargetWidth();
 						this.flushWidth();
 
 						this.setPositionInstantly(args.getTargetCoordinate(), false);
 					}
 
 					this.setTargetWidth(args.getTargetCoordinate());
 					this.setTargetPosition(args.getTargetCoordinate());
 				}
 				else if( event.getAction() == Event_Camera_OnCellSizeFound.class )
 				{
 					Event_Camera_OnCellSizeFound.Args args = event.getActionArgs();
 					
 					this.setTargetWidth(args.getMapping().getCoordinate(), args.getCellSize().getWidth());
 					
 					if( event.getContext().isEntered(State_ViewingCell.class) )
 					{
 						this.ensureTargetWidth();
 						this.flushWidth();
 						
 						this.ensureTargetPosition();
 						this.flushPosition();
 					}
 				}
 				
 				break;
 			}
 		}
 		
 		m_innerContainer.onStateEvent(event);
 	}
 	
 	private void updateWidth()
 	{
 		if( m_width == m_targetWidth )  return;
 		
 		double mantissa = 0;
 		
 		State_CameraFloating floatingState = m_viewContext.stateContext.getEnteredState(State_CameraFloating.class);
 		if( floatingState != null )
 		{
 			m_baseWidthProgress += floatingState.getLastTimeStep();
 			mantissa = m_baseWidthProgress / m_viewContext.config.cellSizeChangeTime_seconds;
 		}
 		else
 		{
 			double snapProgress = m_viewContext.appContext.cameraMngr.getWeightedSnapProgress();
 			//s_logger.severe("hud: " + " " + m_baseWidthProgress + " " + snapProgress + " ");
 			mantissa = m_baseWidthProgress == 1 ? 1 : (snapProgress - m_baseWidthProgress) / (1-m_baseWidthProgress);
 		}
 		
		mantissa = U_Math.clampMantissa(mantissa);
 		double widthDelta = (m_targetWidth - m_baseWidth) * mantissa;
 		m_width = (int) (m_baseWidth + widthDelta);
 		
 		if( mantissa >= 1 )
 		{
 			this.ensureTargetWidth();
 		}
 		
 		this.flushWidth();
 	}
 	
 	private void flushWidth()
 	{
 		this.getElement().getStyle().setWidth(m_width, Unit.PX);
 	}
 	
 	private void setTargetWidth(GridCoordinate coord)
 	{
 		m_utilMapping.getCoordinate().copy(coord);
 		if( m_viewContext.appContext.cellSizeMngr.getCellSizeFromLocalSource(m_utilMapping, m_utilCellSize) )
 		{
 			this.setTargetWidth(coord, m_utilCellSize.getWidth());
 		}
 		else
 		{
 			A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 			//this.setTargetWidth(grid.getCellWidth());
 		}
 	}
 	
 	private void setTargetWidth(GridCoordinate coord_nullable, double hudWidth)
 	{
 		if( m_viewContext.stateContext.isEntered(State_CameraFloating.class) )
 		{
 			m_baseWidthProgress = 0;
 		}
 		else
 		{
 			m_baseWidthProgress = m_viewContext.appContext.cameraMngr.getWeightedSnapProgress();
 		}
 		
 		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		double viewWidth = calcViewWidth(coord_nullable, camera, grid);		
 		m_targetWidth = Math.min(viewWidth, hudWidth);
 		m_targetWidth = Math.max(m_targetWidth, m_minWidth);
 		m_baseWidth = m_width;
 	}
 	
 	private void ensureTargetWidth()
 	{
 		m_width = m_baseWidth = m_targetWidth;
 	}
 	
 	private double calcYOffsetFromCellTop(A_Grid grid)
 	{
 		return -(grid.getCellPadding() + this.m_height);
 	}
 	
 	private void calcPosition(GridCoordinate targetCoord, Point point_out, boolean forTargetLayout)
 	{
 		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 		targetCoord.calcPoint(point_out, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
 		point_out.incY(this.calcYOffsetFromCellTop(grid));
 		
 		if( forTargetLayout )
 		{
 			m_utilMapping.getCoordinate().copy(targetCoord);
 			if( m_viewContext.appContext.cellSizeMngr.getCellSizeFromLocalSource(m_utilMapping, m_utilCellSize) )
 			{
 				m_viewContext.scrollNavigator.calcTargetLayout(m_utilCellSize, targetCoord, s_utilPoint2, m_utilRect);
 				point_out.add(s_utilPoint2);
 			}
 		}
 	}
 	
 	private void setTargetPosition(GridCoordinate targetCoord)
 	{
 		calcPosition(targetCoord, s_utilPoint1, true);
 		
 		this.setTargetPosition(s_utilPoint1);
 	}
 	
 	private void setTargetPosition(Point worldPoint)
 	{
 		if( m_viewContext.stateContext.isEntered(State_CameraFloating.class) )
 		{
 			m_basePositionProgress = 0;
 		}
 		else
 		{
 			m_basePositionProgress = m_viewContext.appContext.cameraMngr.getWeightedSnapProgress();
 		}
 		
 		m_basePosition.copy(m_position);
 		m_targetPosition.copy(worldPoint);
 	}
 	
 	private void setPositionInstantly(GridCoordinate targetCoord, boolean forTargetLayout)
 	{
 		calcPosition(targetCoord, s_utilPoint1, forTargetLayout);
 		
 		this.setPositionInstantly(s_utilPoint1);
 	}
 	
 	private void setPositionInstantly(Point worldPoint)
 	{
 		this.setTargetPosition(worldPoint);
 		this.ensureTargetPosition();
 		this.flushPosition();
 	}
 	
 	private void ensureTargetPosition()
 	{
 		m_basePosition.copy(m_targetPosition);
 		m_position.copy(m_targetPosition);
 	}
 	
 	private void updatePosition()
 	{
 		if( m_position.isEqualTo(m_targetPosition, null) )
 		{
 			//--- DRK > Still have to flush position cause the 2d screen projection
 			//---		is still moving, even if the world position is stationary.
 			this.flushPosition();
 		}
 		
 		double mantissa = 0;
 		
 		State_CameraFloating floatingState = m_viewContext.stateContext.getEnteredState(State_CameraFloating.class);
 		if( floatingState != null )
 		{
 			m_basePositionProgress += floatingState.getLastTimeStep();
 			mantissa = m_basePositionProgress / m_viewContext.config.cellSizeChangeTime_seconds;
 		}
 		else
 		{
 			double snapProgress = m_viewContext.appContext.cameraMngr.getWeightedSnapProgress();
 			mantissa = m_basePositionProgress == 1 ? 1 : (snapProgress - m_basePositionProgress) / (1-m_basePositionProgress);
 		}
 		
		mantissa = U_Math.clampMantissa(mantissa);
 		m_targetPosition.calcDifference(m_basePosition, s_utilVector);
 		s_utilVector.scaleByNumber(mantissa);
 		m_position.copy(m_basePosition);
 		m_position.add(s_utilVector);
 		
 		if( mantissa >= 1 )
 		{
 			this.ensureTargetPosition();
 		}
 		
 		this.flushPosition();
 	}
 	
 	private void flushPosition()
 	{
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		camera.calcScreenPoint(m_position, s_utilPoint1);
 		s_utilPoint1.round();
 		
 		boolean has3dTransforms = m_viewContext.appContext.platformInfo.has3dTransforms();
 		String translation = U_Css.createTranslateTransform(s_utilPoint1.getX(), s_utilPoint1.getY(), has3dTransforms);
 		
 		double scaling = m_viewContext.cellMngr.getLastScaling();
 		String scaleProperty = U_Css.createScaleTransform(scaling, has3dTransforms);
 		U_Css.setTransform(this.getElement(), translation + " " + scaleProperty);
 	}
 }
