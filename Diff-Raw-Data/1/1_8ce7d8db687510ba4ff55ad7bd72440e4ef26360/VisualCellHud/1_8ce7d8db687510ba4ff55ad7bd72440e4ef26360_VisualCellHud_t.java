 package swarm.client.view.cell;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.ClientAppConfig;
 import swarm.client.entities.Camera;
 import swarm.client.navigation.ScrollNavigator.I_ScrollListener;
 import swarm.client.navigation.U_CameraViewport;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Action_Camera_SetViewSize;
 import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
 import swarm.client.states.camera.Event_Camera_OnCellSizeFound;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraFloating;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.view.E_ZIndex;
 import swarm.client.view.I_UIElement;
 import swarm.client.view.U_Css;
 import swarm.client.view.U_View;
 import swarm.client.view.ViewContext;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.statemachine.E_StateEventType;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.CellAddressMapping;
 import swarm.shared.structs.CellSize;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.Point;
 import swarm.shared.structs.Rect;
 import swarm.shared.structs.Vector;
 import swarm.shared.utils.U_Math;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.FlowPanel;
 
 public class VisualCellHud extends FlowPanel implements I_UIElement
 {
 	private static final Logger s_logger = Logger.getLogger(VisualCellHud.class.getName());
 	
 	private static final Point s_utilPoint1 = new Point();
 	private static final Point s_utilPoint2 = new Point();
 	private static final Vector s_utilVector = new Vector();
 	
 	private final ViewContext m_viewContext;
 	
 	private final double m_minWidth = 176;// TODO: GHETTO
 	private double m_width = 0;
 	private double m_baseWidth = 0;
 	private double m_targetWidth = 0;
 	private double m_baseWidthProgress = 0;
 	
 	private double m_basePositionProgress = 0;
 	private final Point m_position = new Point();
 	private final Point m_basePosition = new Point();
 	private final Point m_targetPosition = new Point();
 	private final Point m_screenPosition = new Point();
 	
 	private double m_baseAlpha;
 	private double m_alpha;
 	private final double m_fadeOutTime_seconds;
 	
 	private final CellAddressMapping m_utilMapping = new CellAddressMapping();
 	private final Rect m_utilRect = new Rect();
 	private final CellSize m_utilCellSize = new CellSize();
 	
 	private final GridCoordinate m_lastTargetCoord = new GridCoordinate();
 	
 	private final FlowPanel m_actualHud;
 	
 	private final VisualCellHudInner m_innerContainer;
 	
 	private double m_height;
 	
 	private int m_scrollX;
 	private int m_scrollY;
 	
 	public VisualCellHud(ViewContext viewContext, ClientAppConfig appConfig)
 	{
 		m_innerContainer = new VisualCellHudInner(viewContext);
 		
 		m_viewContext = viewContext;
 		m_height = m_viewContext.appConfig.cellHudHeight;
 		m_fadeOutTime_seconds = m_viewContext.config.hudFadeOutTime_seconds;
 		
 		m_alpha = m_baseAlpha = 0.0;
		this.setAlpha(0.0);
 		
 		this.getElement().getStyle().setOverflow(Overflow.HIDDEN);
 		this.getElement().getStyle().setPosition(Position.FIXED);
 		this.getElement().getStyle().setHeight(47, Unit.PX);
 		
 		m_actualHud = new FlowPanel();		
 		m_actualHud.addStyleName("sm_cell_hud");
 		
 		E_ZIndex.CELL_HUD.assignTo(this);
 		
 		this.setVisible(false);
 		m_actualHud.getElement().getStyle().setProperty("minWidth", m_minWidth + "px");
 		U_Css.setTransformOrigin(this.getElement(), "0% 0%");
 		
 		m_actualHud.add(m_innerContainer);
 		this.add(m_actualHud);
 		
 		m_viewContext.scrollNavigator.addScrollListener(new I_ScrollListener()
 		{
 			@Override
 			public void onScroll()
 			{
 				State_ViewingCell viewingState =  m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 				if( viewingState != null )
 				{
 					A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 					m_scrollX = m_viewContext.scrollNavigator.getScrollX();
 					m_scrollY = m_viewContext.scrollNavigator.getScrollY();
 					
 					VisualCellHud.this.setPositionInstantly(viewingState.getTargetCoord(), true);
 					VisualCellHud.this.setWidthInstantly(viewingState.getTargetCoord());
 					
 					if( m_scrollY >= grid.getCellPadding() )
 					{
 						VisualCellHud.this.setAlpha(.75);
 					}
 					else
 					{
 						VisualCellHud.this.setAlpha(1);
 					}
 				}
 				else
 				{
 					//--- DRK > I guess when we leave viewing state and reset scroll left/top to zero,
 					//---		that fires a scroll event, so valid case here...ASSERT removed for now.
 					//smU_Debug.ASSERT(false, "Expected viewing state to be entered.");
 				}
 			}
 		});
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
 		
 		if( viewingState == null )
 		{
 			return 0;
 		}
 		
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		Element scrollElement = this.getParent().getElement();
 		
 		double viewWidth = calcViewWidth(viewingState.getCell().getCoordinate(), camera, grid);
 		double hudWidth = m_width;
 		if( hudWidth > viewWidth )
 		{
 			double scrollWidth = scrollElement.getScrollWidth();
 			double clientWidth = scrollElement.getClientWidth();
 			double diff = (hudWidth - viewWidth);// +  U_CameraViewport.getViewPadding(grid);
 			double scrollRatio = m_scrollX / (scrollWidth-clientWidth);
 			return -(diff * scrollRatio);
 		}
 		else
 		{
 			return 0.0;
 		}
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
 					else
 					{
 						this.flushWidth(); // get rid of cropping.
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
 					resetScrollValues();
 					
 					m_baseAlpha = m_alpha;
 					
 					this.flushWidth(); // get rid of cropping.
 					
 					A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 					m_lastTargetCoord.calcPoint(s_utilPoint2, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
 					s_utilPoint2.incY(calcYOffsetFromCellTop(grid));
 					this.setTargetPosition(s_utilPoint2);
 					this.setTargetWidth(null, grid.getCellWidth(), false);
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
 									double timeMantissa = event.getState().getTotalTimeInState() / m_fadeOutTime_seconds;
 									timeMantissa = U_Math.clamp(timeMantissa, 0, 1);
 									timeMantissa = Math.sqrt(timeMantissa);
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
 				if( event.isFor(State_ViewingCell.class) )
 				{
 					m_actualHud.getElement().getStyle().clearWidth();
 				}
 				
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
 						Action_Camera_SetViewSize.Args args = event.getActionArgs();
 						if( args.updateBuffer() )
 						{
 							this.setPositionInstantly(viewingState.getCell().getCoordinate(), true);
 							this.setWidthInstantly(viewingState.getCell().getCoordinate());
 						}
 					}
 					else if( snappingState != null )
 					{
 						this.setTargetPosition(snappingState.getTargetCoord());
 						this.setTargetWidth(snappingState.getTargetCoord());
 					}
 				}
 				else if( event.getAction() == Action_Camera_SnapToPoint.class )
 				{
 					State_ViewingCell viewingState = event.getContext().getEnteredState(State_ViewingCell.class);
 					
 					if( viewingState != null )
 					{
 						Action_Camera_SnapToPoint.Args args = event.getActionArgs();
 						if( args.isInstant() )
 						{
 							this.setPositionInstantly(viewingState.getCell().getCoordinate(), true);
 							this.setWidthInstantly(viewingState.getCell().getCoordinate());
 						}
 					}
 				}
 				else if( event.getAction() == Action_Camera_SnapToCoordinate.class )
 				{
 					Action_Camera_SnapToCoordinate.Args args = event.getActionArgs();
 					m_lastTargetCoord.copy(args.getTargetCoordinate());
 					
 					if( m_alpha <= 0 )
 					{
 						this.setPositionInstantly(args.getTargetCoordinate(), false);
 						
 						//--- Ensure that cell hud is the default size of grid cells.
 						A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 						this.setTargetWidth(args.getTargetCoordinate(), grid.getCellWidth(), true);
 						this.ensureTargetWidth();
 						this.flushWidth();
 					}
 
 					this.setTargetPosition(args.getTargetCoordinate());
 					this.setTargetWidth(args.getTargetCoordinate());
 				}
 				else if( event.getAction() == Event_Camera_OnCellSizeFound.class )
 				{
 					Event_Camera_OnCellSizeFound.Args args = event.getActionArgs();
 					
 					if( event.getContext().isEntered(State_ViewingCell.class) )
 					{
 						this.ensureTargetPosition();
 						this.flushPosition();
 						
 						this.setTargetWidth(args.getMapping().getCoordinate(), args.getCellSize().getWidth(), true);
 						this.ensureTargetWidth();
 						this.flushWidth();
 					}
 					else
 					{
 						this.setTargetWidth(args.getMapping().getCoordinate(), args.getCellSize().getWidth(), true);
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
 			mantissa = U_Math.clampMantissa(mantissa);
 			mantissa = U_View.easeMantissa(mantissa, m_viewContext.config.cellRetractionEasing);
 		}
 		else
 		{
 			double snapProgress = m_viewContext.appContext.cameraMngr.getWeightedSnapProgress();
 			//s_logger.severe("hud: " + " " + m_baseWidthProgress + " " + snapProgress + " ");
 			mantissa = m_baseWidthProgress == 1 ? 1 : (snapProgress - m_baseWidthProgress) / (1-m_baseWidthProgress);
 			mantissa = U_Math.clampMantissa(mantissa);
 		}
 		
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
 		double cropperWidth = m_width;
 		
 		if( m_viewContext.stateContext.isEntered(State_ViewingCell.class) )
 		{
 			A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 			double viewWidth = m_viewContext.scrollNavigator.getWindowWidth();
 			double x = m_screenPosition.getX() - grid.getCellPadding();
 			double overflow = (x + (cropperWidth + grid.getCellPadding())) - viewWidth;
 			
 			if( overflow > 0 )
 			{
 				cropperWidth -= overflow;
 			}
 		}
 		
 		m_actualHud.getElement().getStyle().setWidth(m_width, Unit.PX);
 		this.getElement().getStyle().setWidth(cropperWidth, Unit.PX);
 	}
 	
 	private void setTargetWidth(GridCoordinate coord)
 	{
 		m_utilMapping.getCoordinate().copy(coord);
 		if( m_viewContext.appContext.cellSizeMngr.getCellSizeFromLocalSource(m_utilMapping, m_utilCellSize) )
 		{
 			this.setTargetWidth(coord, m_utilCellSize.getWidth(), true);
 		}
 		else
 		{
 			A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 			//this.setTargetWidth(grid.getCellWidth());
 		}
 	}
 	
 	private void setTargetWidth(GridCoordinate coord_nullable, double hudWidth, boolean constrainByViewWidth)
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
 		
 		if( constrainByViewWidth )
 		{
 			double viewWidth = calcViewWidth(coord_nullable, camera, grid);		
 			m_targetWidth = Math.min(viewWidth, hudWidth);
 		}
 		else
 		{
 			m_targetWidth = hudWidth;
 		}
 		
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
 	
 	private void resetScrollValues()
 	{
 		m_scrollX = m_scrollY = 0;
 	}
 	
 	private void calcPosition(GridCoordinate targetCoord, Point point_out, boolean forTargetLayout)
 	{
 		if( !m_viewContext.stateContext.isEntered(State_ViewingCell.class) )
 		{
 			resetScrollValues();
 		}
 		else
 		{
 		}
 		
 		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 		targetCoord.calcPoint(point_out, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
 		point_out.incY(this.calcYOffsetFromCellTop(grid));
 		
 		point_out.incX(this.calcScrollXOffset(grid));
 		
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
 	
 	private void setWidthInstantly(GridCoordinate targetCoord)
 	{
 		this.setTargetWidth(targetCoord);
 		this.ensureTargetWidth();
 		this.flushWidth();
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
 			
 			return;
 		}
 		
 		double mantissa = 0;
 		
 		State_CameraFloating floatingState = m_viewContext.stateContext.getEnteredState(State_CameraFloating.class);
 		if( floatingState != null )
 		{
 			m_basePositionProgress += floatingState.getLastTimeStep();
 			mantissa = m_basePositionProgress / m_viewContext.config.cellSizeChangeTime_seconds;
 			mantissa = U_Math.clampMantissa(mantissa);
 			mantissa = U_View.easeMantissa(mantissa, m_viewContext.config.cellRetractionEasing);
 		}
 		else
 		{
 			double snapProgress = m_viewContext.appContext.cameraMngr.getWeightedSnapProgress();
 			mantissa = m_basePositionProgress == 1 ? 1 : (snapProgress - m_basePositionProgress) / (1-m_basePositionProgress);
 			mantissa = U_Math.clampMantissa(mantissa);
 		}
 		
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
 		camera.calcScreenPoint(m_position, m_screenPosition);
 		//s_utilPoint1.inc(m_scrollX, m_scrollY, 0);
 		m_screenPosition.round();
 		
 		//s_logger.severe(m_screenPosition + "");
 		
 		boolean has3dTransforms = m_viewContext.appContext.platformInfo.has3dTransforms();
 		String translation = U_Css.createTranslateTransform(m_screenPosition.getX(), m_screenPosition.getY(), has3dTransforms);
 		
 		double scaling = m_viewContext.cellMngr.getLastScaling();
 		String scaleProperty = U_Css.createScaleTransform(scaling, has3dTransforms);
 		U_Css.setTransform(this.getElement(), translation + " " + scaleProperty);
 	}
 }
