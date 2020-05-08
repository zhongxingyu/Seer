 package swarm.client.view.cell;
 
 import java.util.logging.Logger;
 
 
 import swarm.client.view.*;
 import swarm.client.view.alignment.smAlignmentDefinition;
 import swarm.client.view.alignment.smAlignmentRect;
 import swarm.client.view.alignment.smE_AlignmentPosition;
 import swarm.client.view.alignment.smE_AlignmentType;
 import swarm.client.view.tooltip.smE_ToolTipMood;
 import swarm.client.view.tooltip.smE_ToolTipType;
 import swarm.client.view.tooltip.smToolTip;
 import swarm.client.view.tooltip.smToolTipConfig;
 import swarm.client.view.tooltip.smToolTipManager;
 import swarm.client.view.widget.smMagnifier;
 import swarm.client.states.*;
 import swarm.client.states.camera.Action_Camera_SetCameraViewSize;
 import swarm.client.states.camera.Event_GettingMapping_OnResponse;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_GettingMapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.app.smClientAppConfig;
 import swarm.client.app.smAppContext;
 import swarm.client.entities.smBufferCell;
 import swarm.client.entities.smCamera;
 import swarm.client.managers.smCellBufferManager;
 import swarm.client.navigation.smMouseNavigator;
 import swarm.shared.app.smS_App;
 import swarm.shared.entities.smA_Grid;
 import swarm.shared.statemachine.smA_Action;
 import swarm.shared.statemachine.smA_State;
 import swarm.shared.statemachine.smStateEvent;
 import swarm.shared.structs.smPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Overflow;
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
 
 public class smVisualCellContainer extends FlowPanel implements ResizeHandler, smI_UIElement
 {
 	private static final Logger s_logger = Logger.getLogger(smVisualCellContainer.class.getName());
 	
 	private static final smPoint s_utilPoint1 = new smPoint();
 	private static final smPoint s_utilPoint2 = new smPoint();
 	
 	private final smMagnifier m_magnifier;
 	
 	private final FlowPanel m_cellContainerInner = new FlowPanel();
 	private final FlowPanel m_splashGlass = new FlowPanel();
 	
 	private final smVisualCellCropper[] m_cropper = new smVisualCellCropper[2];
 	
 	private final smAlignmentDefinition m_statusAlignment = new smAlignmentDefinition();
 	
 	private boolean m_showingMappingNotFound = false;
 	
 	private final smToolTipConfig m_gettingAddressTipConfig = new smToolTipConfig(smE_ToolTipType.STATUS, m_statusAlignment, "Resolving address...");
 	private final smToolTipConfig m_mappingNotFoundTipConfig = new smToolTipConfig(smE_ToolTipType.NOTIFICATION, m_statusAlignment, "Address not found!", smE_ToolTipMood.OOPS);
 	
 	private final Action_Camera_SetCameraViewSize.Args m_args_SetCameraViewSize = new Action_Camera_SetCameraViewSize.Args();
 	
 	private final smViewContext m_viewContext;
 	
 	public smVisualCellContainer(smViewContext viewContext, smViewConfig config)
 	{
 		m_viewContext = viewContext;
 		
 		m_magnifier = new smMagnifier(viewContext, config.magnifierTickCount, config.magFadeInTime_seconds);
 		
 		m_splashGlass.addStyleName("sm_splash_glass");
 		m_cellContainerInner.addStyleName("sm_cell_container_inner");
 		this.addStyleName("sm_cell_container");
 		
 		smE_ZIndex.CELL_SPLASH_GLASS.assignTo(m_splashGlass);
 		
 		Window.addResizeHandler(this);
 
 		//smU_UI.toggleSelectability(this.getElement(), false);
 		
 		final LayoutPanel magnifierContainer = new LayoutPanel();
 		magnifierContainer.setSize("100%", "100%");
 		magnifierContainer.add(m_magnifier);
 		magnifierContainer.setWidgetRightWidth(m_magnifier, 8, Unit.PX, 34, Unit.PX);
 		magnifierContainer.setWidgetTopHeight(m_magnifier, 8, Unit.PX, 196, Unit.PX);
 		
 		for( int i = 0; i < m_cropper.length; i++ )
 		{
 			m_cropper[i] = new smVisualCellCropper();
 			m_cellContainerInner.add(m_cropper[i]);
 		}
 		
 		this.add(magnifierContainer);
 		this.add(m_cellContainerInner);
 		this.add(m_splashGlass);
 
 		m_statusAlignment.setPosition(smE_AlignmentType.MASTER_ANCHOR_HORIZONTAL, smE_AlignmentPosition.RIGHT_OR_BOTTOM);
 		m_statusAlignment.setPosition(smE_AlignmentType.MASTER_ANCHOR_VERTICAL, smE_AlignmentPosition.RIGHT_OR_BOTTOM);
 		m_statusAlignment.setPosition(smE_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, smE_AlignmentPosition.RIGHT_OR_BOTTOM);
 		m_statusAlignment.setPosition(smE_AlignmentType.SLAVE_ANCHOR_VERTICAL, smE_AlignmentPosition.RIGHT_OR_BOTTOM);
 		m_statusAlignment.setPadding(smE_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, smS_UI.ADDRESS_STATUS_TOOL_TIP_PADDING);
 		m_statusAlignment.setPadding(smE_AlignmentType.SLAVE_ANCHOR_VERTICAL, smS_UI.ADDRESS_STATUS_TOOL_TIP_PADDING);
 	}
 	
 	public FlowPanel getMouseEnabledLayer()
 	{
 		return m_splashGlass;
 	}
 	
 	public FlowPanel getCellContainerInner()
 	{
 		return m_cellContainerInner;
 	}
 	
 	public smMagnifier getMagnifier()
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
 		int cellSubCount = m_viewContext.appContext.cellBufferMngr.getDisplayBuffer().getSubCellCount();
 		if( cellSubCount == 0 || cellSubCount == 1 )
 		{
 			hideCroppers();
 			
 			return;
 		}
 
 		//--- DRK > If the mod operation is 0, we can also always match bottom/right sides exactly.
 		smA_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 		if( (grid.getWidth() % cellSubCount) == 0 && (grid.getHeight() % cellSubCount) == 0 )
 		{
 			hideCroppers();
 			
 			return;
 		}
 		
 		smCamera camera = m_viewContext.appContext.cameraMngr.getCamera();
 	
 		double gridWidthInPixels = grid.calcPixelWidth();
 		double gridHeightInPixels = grid.calcPixelHeight();
 		
 		smPoint worldPoint = s_utilPoint1;
 		smPoint screenPoint = s_utilPoint2;
 
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
 	public void onStateEvent(smStateEvent event)
 	{
 		switch(event.getType() )
 		{
 			case DID_ENTER:
 			{
 				if( event.getState() instanceof StateMachine_Camera )
 				{
 					//--- DRK > For some reason we can't use the updateCameraViewRect method because
 					//---		offset width/height return 0...I think it's because CSS isn't applied yet.
 					//---		So kind of a hack here...would be nice if I could fix this, so...TODO(DRK).
					double width = m_viewContext.splitPanel.getCellPanelWidth();
 					double x = m_viewContext.splitPanel.getTabPanelWidth();
					double height = RootPanel.get().getOffsetHeight();
 					
 					m_args_SetCameraViewSize.set(width, height);
 					
 					event.getContext().performAction(Action_Camera_SetCameraViewSize.class, m_args_SetCameraViewSize);
 					
 					//--- DRK > Because width/height of "this" is still 0/0, we temporarily
 					//---		give the tool tip an override rectangle to work with.
 					m_statusAlignment.setMasterRect(new smAlignmentRect(x, 0, width, height));
 					
 					//--- DRK > This used to be uncommented, but new set up means width/height of this container is still 0 at this point.
 					//updateCameraViewRect();
 				}
 				else if ( event.getState() instanceof State_ViewingCell )
 				{
 					//TODO: Make sure cell exits cleanly, somehow.
 					
 					//smVisualCell viewedVisualCell = (smVisualCell) m_viewedCell.getVisualization();
 					
 					//smU_UI.toggleSelectability(m_cellContainerInner.getElement(), false);
 					
 					//viewedVisualCell.getElement().blur(); // TODO: This doesn't work to clear selection of cell contents if you navigate away.
 					
 					//m_viewedCell = null;
 				}
 				
 				break;
 			}
 			
 			case DID_UPDATE:
 			{
 				if( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					this.updateCroppers();
 				}
 				
 				if( event.getState() instanceof State_ViewingCell )
 				{
 					//--- DRK > Note that the above update could have caused the machine to exit this state.
 					if( event.getState()./*still*/isEntered() )
 					{
 						smBufferCell viewedCell = ((State_ViewingCell) event.getState()).getCell();
 						
 						smMouseNavigator navigator = m_viewContext.mouseNavigator;
 	
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
 	
 	private void updateCameraViewRect()
 	{
		m_args_SetCameraViewSize.set((double)this.getOffsetWidth(), (double)this.getOffsetHeight());
 		m_viewContext.stateContext.performAction(Action_Camera_SetCameraViewSize.class, m_args_SetCameraViewSize);
 	}
 	
 	public void onResize()
 	{
 		updateCameraViewRect();
 		
 		this.updateCroppers();
 		
 		m_statusAlignment.setMasterRect(null);
 		
 		m_viewContext.toolTipMngr.onTipMove(this);
 	}
 
 	@Override
 	public void onResize(ResizeEvent event)
 	{
 		this.onResize();
 	}
 }
