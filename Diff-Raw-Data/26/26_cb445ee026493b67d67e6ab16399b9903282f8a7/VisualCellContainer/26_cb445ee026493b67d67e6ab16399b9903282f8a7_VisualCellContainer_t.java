 package swarm.client.view.cell;
 
 import java.util.logging.Logger;
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 import swarm.client.view.*;
 import swarm.client.view.alignment.AlignmentDefinition;
 import swarm.client.view.alignment.AlignmentRect;
 import swarm.client.view.alignment.E_AlignmentPosition;
 import swarm.client.view.alignment.E_AlignmentType;
 import swarm.client.view.tooltip.E_ToolTipMood;
 import swarm.client.view.tooltip.E_ToolTipType;
 import swarm.client.view.tooltip.ToolTip;
 import swarm.client.view.tooltip.ToolTipConfig;
 import swarm.client.view.tooltip.ToolTipManager;
 import swarm.client.view.widget.Magnifier;
 import swarm.client.states.*;
 import swarm.client.states.camera.Action_Camera_SetViewSize;
 import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Event_GettingMapping_OnResponse;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_GettingMapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.app.ClientAppConfig;
 import swarm.client.app.AppContext;
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.Camera;
 import swarm.client.managers.CameraManager;
 import swarm.client.managers.CellBufferManager;
 import swarm.client.navigation.MouseNavigator;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Cell;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.lang.Boolean;
 import swarm.shared.statemachine.A_Action;
 import swarm.shared.statemachine.A_State;
 import swarm.shared.statemachine.StateContext;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.Point;
 import swarm.shared.utils.U_Math;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.*;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class VisualCellContainer extends FlowPanel implements ResizeHandler, I_UIElement
 {
 	private static final Logger s_logger = Logger.getLogger(VisualCellContainer.class.getName());
 	
 	private static final Point s_utilPoint1 = new Point();
 	private static final Point s_utilPoint2 = new Point();
 	
 	private final Magnifier m_magnifier;
 	
 	private final FlowPanel m_cellContainerInner = new FlowPanel();
 	private final FlowPanel m_splashGlass = new FlowPanel();
 	private final FlowPanel m_scrollContainer = new FlowPanel();
 	
 	private final VisualCellCropper[] m_cropper = new VisualCellCropper[2];
 	
 	private final AlignmentDefinition m_statusAlignment = new AlignmentDefinition();
 	private final AlignmentDefinition m_snapTargetAlignment = new AlignmentDefinition();
 	
 	private boolean m_showingMappingNotFound = false;
 	
 	private final ToolTipConfig m_snapTargetConfig = new ToolTipConfig(E_ToolTipType.STATUS, m_snapTargetAlignment, "");
 	private final ToolTipConfig m_gettingAddressTipConfig = new ToolTipConfig(E_ToolTipType.STATUS, m_statusAlignment, "Resolving address...");
 	private final ToolTipConfig m_mappingNotFoundTipConfig = new ToolTipConfig(E_ToolTipType.NOTIFICATION, m_statusAlignment, "Address not found!", E_ToolTipMood.OOPS);
 	
 	private final Action_Camera_SetViewSize.Args m_args_SetCameraViewSize = new Action_Camera_SetViewSize.Args();
 	
 	private final ViewContext m_viewContext;

//	private final FpsViewer m_fpsViewer = new FpsViewer(this.getElement());
 	
 	public VisualCellContainer(ViewContext viewContext, ViewConfig config)
 	{		
 		m_viewContext = viewContext;
 		
 		m_scrollContainer.addStyleName("sm_cell_scroll_container");
 		
 		m_magnifier = new Magnifier(viewContext, config.magnifierTickCount, config.magFadeInTime_seconds);
 		
 		m_splashGlass.addStyleName("sm_splash_glass");
 		m_cellContainerInner.addStyleName("sm_cell_container_inner");
 		this.addStyleName("sm_cell_container");
 		
 		E_ZIndex.CELL_SPLASH_GLASS.assignTo(m_splashGlass);
 		
 		Window.addResizeHandler(this);
 
 		//smU_UI.toggleSelectability(this.getElement(), false);
 		
 		final LayoutPanel magnifierContainer = new LayoutPanel();
 		magnifierContainer.setSize("100%", "100%");
 		magnifierContainer.add(m_magnifier);
 		magnifierContainer.setWidgetRightWidth(m_magnifier, 8, Unit.PX, 34, Unit.PX);
 		magnifierContainer.setWidgetTopHeight(m_magnifier, 8, Unit.PX, 196, Unit.PX);
 		
 		for( int i = 0; i < m_cropper.length; i++ )
 		{
 			m_cropper[i] = new VisualCellCropper();
 			m_cellContainerInner.add(m_cropper[i]);
 		}
 		
 		this.add(magnifierContainer);
 		m_scrollContainer.add(m_cellContainerInner);
 		m_scrollContainer.add(m_splashGlass);
 		
 		this.add(m_scrollContainer);
 
 		m_statusAlignment.setPosition(E_AlignmentType.MASTER_ANCHOR_HORIZONTAL, E_AlignmentPosition.RIGHT_OR_BOTTOM);
 		m_statusAlignment.setPosition(E_AlignmentType.MASTER_ANCHOR_VERTICAL, E_AlignmentPosition.RIGHT_OR_BOTTOM);
 		m_statusAlignment.setPosition(E_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, E_AlignmentPosition.RIGHT_OR_BOTTOM);
 		m_statusAlignment.setPosition(E_AlignmentType.SLAVE_ANCHOR_VERTICAL, E_AlignmentPosition.RIGHT_OR_BOTTOM);
 		m_statusAlignment.setPadding(E_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, S_UI.ADDRESS_STATUS_TOOL_TIP_PADDING);
 		m_statusAlignment.setPadding(E_AlignmentType.SLAVE_ANCHOR_VERTICAL, S_UI.ADDRESS_STATUS_TOOL_TIP_PADDING);
 		
 		m_snapTargetAlignment.setPosition(E_AlignmentType.MASTER_ANCHOR_HORIZONTAL, E_AlignmentPosition.LEFT_OR_TOP);
 		m_snapTargetAlignment.setPosition(E_AlignmentType.MASTER_ANCHOR_VERTICAL, E_AlignmentPosition.RIGHT_OR_BOTTOM);
 		m_snapTargetAlignment.setPosition(E_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, E_AlignmentPosition.LEFT_OR_TOP);
 		m_snapTargetAlignment.setPosition(E_AlignmentType.SLAVE_ANCHOR_VERTICAL, E_AlignmentPosition.RIGHT_OR_BOTTOM);
 		m_snapTargetAlignment.setPadding(E_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, S_UI.ADDRESS_STATUS_TOOL_TIP_PADDING);
 		m_snapTargetAlignment.setPadding(E_AlignmentType.SLAVE_ANCHOR_VERTICAL, S_UI.ADDRESS_STATUS_TOOL_TIP_PADDING);
 	}
 	
 	public FlowPanel getMouseEnabledLayer()
 	{
 		return m_splashGlass;
 	}
 	
 	public FlowPanel getScrollContainer()
 	{
 		return m_scrollContainer;
 	}
 	
 	public FlowPanel getCellContainerInner()
 	{
 		return m_cellContainerInner;
 	}
 	
 	public Magnifier getMagnifier()
 	{
 		return m_magnifier;
 	}
 	
 	private void hideCroppers()
 	{
 		for( int i = 0; i < m_cropper.length; i++ )
 		{
 			m_cropper[i].setVisible(false);
 		}
 	}
 	
 	private void updateCroppers()
 	{
 		//--- DRK > If cell sub count is 1, it means we can match the bottom/right sides of the grid exactly.
 		int cellSubCount = m_viewContext.appContext.cellBufferMngr.getLowestDisplayBuffer().getSubCellCount();
 		if( cellSubCount == 0 || cellSubCount == 1 )
 		{
 			hideCroppers();
 			
 			return;
 		}
 
 		//--- DRK > If the mod operation is 0, we can also always match bottom/right sides exactly.
 		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 		if( (grid.getWidth() % cellSubCount) == 0 && (grid.getHeight() % cellSubCount) == 0 )
 		{
 			hideCroppers();
 			
 			return;
 		}
 		
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 	
 		double gridWidthInPixels = grid.calcPixelWidth();
 		double gridHeightInPixels = grid.calcPixelHeight();
 		
 		Point worldPoint = s_utilPoint1;
 		Point screenPoint = s_utilPoint2;
 
 		worldPoint.zeroOut();
 		worldPoint.inc(gridWidthInPixels, gridHeightInPixels, 0);
 		camera.calcScreenPoint(worldPoint, screenPoint);
 		double[] screenDimensions = {this.getOffsetWidth(), this.getOffsetHeight()};
 		double scaling = camera.calcDistanceRatio();
 		
 		//--- DRK > If cell size is greater than 1, and grid isn't power of two, we have to bring
 		//---		the croppers into play if we can see the bottom or right sides of the grid.
 		for( int i = 0; i < m_cropper.length; i++ )
 		{
 			double screenComponent = screenPoint.getComponent(i);
 			
 			if( screenComponent <= screenDimensions[i] )
 			{
 				m_cropper[i].setVisible(true);
 				
 				double component = screenComponent < 0 ? 0 : screenComponent;
 				component += scaling * grid.getCellPadding();
 				component -= 1; // just to make sure we don't have a pixel sliver of the next cell over.
 				
 				m_cropper[i].setPositionComponent(i, component);
 			}
 			else
 			{
 				m_cropper[i].setVisible(false);
 			}
 		}
 	}
 
 	@Override
 	public void onStateEvent(StateEvent event)
 	{
 		switch(event.getType() )
 		{
 			case DID_ENTER:
 			{
 				if( event.getState() instanceof StateMachine_Camera )
 				{
 					//--- DRK > For some reason we can't use the updateCameraViewRect method because
 					//---		offset width/height returns 0...I think it's because CSS isn't applied yet.
 					//---		So kind of a hack here...would be nice if I could fix this, so...TODO(DRK).
 					/*int width = (int) Math.round(m_viewContext.splitPanel.getCellPanelWidth());
 					double x = m_viewContext.splitPanel.getTabPanelWidth();
 					int height = RootPanel.get().getOffsetHeight();
 					int height2 = this.getElement().getClientHeight();
 					
 					m_args_SetCameraViewSize.init(width, height, true);
 					event.getContext().performAction(Action_Camera_SetViewSize.class, m_args_SetCameraViewSize);
 					
 					//--- DRK > Because width/height of "this" is still 0/0, we temporarily
 					//---		give the tool tip an override rectangle to work with.
 					m_statusAlignment.setMasterRect(new smAlignmentRect(x, 0, width, height));*/
 				}
 				
 				break;
 			}
 			
 			case DID_UPDATE:
 			{
 				if( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					this.updateCroppers();
 					
//					m_fpsViewer.update(event.getState().getLastTimeStep());
 				}
 				
 				if( event.getState() instanceof State_ViewingCell )
 				{
 					//--- DRK > Note that the above update could have caused the machine to exit this state.
 					if( event.getState()./*still*/isEntered() )
 					{
 						BufferCell viewedCell = ((State_ViewingCell) event.getState()).getCell();
 						
 						MouseNavigator navigator = m_viewContext.mouseNavigator;
 	
 						/*if( navigator.getMouseGridCoord().isEqualTo(viewedCell.getCoordinate()) )
 						{
 							smU_UI.toggleSelectability(m_cellContainerInner.getElement(), true);
 						}
 						else
 						{
 							smU_UI.toggleSelectability(m_cellContainerInner.getElement(), false);
 						}*/
 					}
 					else
 					{
 						//smU_UI.toggleSelectability(m_cellContainerInner.getElement(), false);
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_FOREGROUND:
 			{
 				if( event.getState() instanceof State_GettingMapping )
 				{
 					m_showingMappingNotFound = false;
 					
 					m_viewContext.toolTipMngr.removeTip(this);
 					m_viewContext.toolTipMngr.addTip(this, m_gettingAddressTipConfig);
 				}
 				
 				break;
 			}
 			
 			case DID_BACKGROUND:
 			{
 				if( event.getState() instanceof State_GettingMapping )
 				{
 					if( !m_showingMappingNotFound )
 					{
 						m_viewContext.toolTipMngr.removeTip(this);
 					}
 					
 					m_showingMappingNotFound = false;
 				}
 				
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if( event.getAction() == Event_GettingMapping_OnResponse.class )
 				{
 					Event_GettingMapping_OnResponse.Args args = event.getActionArgs();
 					
 					if( args.getType() != Event_GettingMapping_OnResponse.E_Type.ON_FOUND )
 					{
 						m_viewContext.toolTipMngr.removeTip(this);
 						m_viewContext.toolTipMngr.addTip(this, m_mappingNotFoundTipConfig);
 						
 						m_showingMappingNotFound = true;
 					}
 				}
 				
 				break;
 			}
 		}
 		
 		m_magnifier.onStateEvent(event);
 	}	
 	
 	public void onResize()
 	{
 		m_viewContext.scrollNavigator.onResize();
 		
 		this.updateCroppers();
 		
 		m_statusAlignment.setMasterRect(null);
 		
 		m_viewContext.toolTipMngr.onTipMove(this);
 	}
 
 	@Override
 	public void onResize(ResizeEvent event)
 	{
 //		super.onResize(event);
 //		this.onResize();
 	}
 }
