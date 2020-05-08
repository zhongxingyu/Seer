 package swarm.client.view.widget;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.smAppContext;
 import swarm.client.entities.smCamera;
 import swarm.client.input.smClickManager;
 import swarm.client.input.smI_ClickHandler;
 import swarm.client.managers.smCameraManager;
 import swarm.client.states.StateMachine_Base;
 import swarm.client.states.camera.Action_Camera_SetCameraTarget;
 import swarm.client.states.camera.Action_Camera_SetCameraViewSize;
 import swarm.client.states.camera.Action_Camera_SetInitialPosition;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraFloating;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.view.smE_ZIndex;
 import swarm.client.view.smViewContext;
 import swarm.client.view.tooltip.smE_ToolTipType;
 import swarm.client.view.tooltip.smToolTipConfig;
 import swarm.client.view.tooltip.smToolTipManager;
 import swarm.shared.app.smS_App;
 import swarm.shared.utils.smU_Math;
 import swarm.shared.statemachine.smA_Action;
 import swarm.shared.statemachine.smA_State;
 import swarm.shared.statemachine.smE_StateTimeType;
 import swarm.shared.statemachine.smI_StateEventListener;
 import swarm.shared.statemachine.smStateEvent;
 import swarm.shared.structs.smPoint;
 import com.google.gwt.dom.client.Touch;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HumanInputEvent;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseEvent;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.dom.client.TouchEndEvent;
 import com.google.gwt.event.dom.client.TouchEndHandler;
 import com.google.gwt.event.dom.client.TouchMoveEvent;
 import com.google.gwt.event.dom.client.TouchMoveHandler;
 import com.google.gwt.event.dom.client.TouchStartEvent;
 import com.google.gwt.event.dom.client.TouchStartHandler;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class smMagnifier extends FlowPanel implements smI_StateEventListener
 {
 	private class MagnifierButton extends smSpriteButton
 	{
 		public MagnifierButton(String spriteId)
 		{
 			super(spriteId);
 			
 			this.addStyleName("sm_zoom_button");
 		}
 	}
 	
 	private static final Logger s_logger = Logger.getLogger(smMagnifier.class.getName());
 	
 	//--- DRK > These kind of "act" like constants which is why they're formatted as such, though they aren't technically.
 	//---		They need to be derived at runtime.
 	private double SLIDER_HEIGHT = Double.NaN;
 	private double DRAGGER_START_Y = Double.NaN;
 	private double DRAGGER_HEIGHT = Double.NaN;
 	private double DRAGGER_HEIGHT_DIV_2 = Double.NaN;
 	
 	private final MagnifierButton m_zoomIn = new MagnifierButton("zoom_in");
 	private final MagnifierButton m_zoomOut = new MagnifierButton("zoom_out");
 	private final MagnifierButton m_dragger = new MagnifierButton("zoom_drag");
 	private final smButton m_slider = new smButton();
 	
 	private smA_State m_cameraState = null;
 	
 	private boolean m_underThisControl = false;
 	
 	private double m_currentRatio = 0;
 	
 	private boolean m_isMouseDown = false;
 	private double m_mouseDownOffset = 0;
 	private double m_touchY = 0;
 	
 	private final smPoint m_utilPoint = new smPoint();
 	
 	private final Action_Camera_SetCameraTarget.Args m_args_SetCameraTarget = new Action_Camera_SetCameraTarget.Args();
 	
 	private final double m_tickRatio;
 	private final double m_fadeInTime_seconds;
 	
 	private double m_baseAlpha;
 	private double m_alpha;
 	
 	private final smViewContext m_viewContext;
 
 	public smMagnifier(smViewContext viewContext, double tickCount, double fadeInTime_seconds)
 	{
 		m_viewContext = viewContext;
 		
 		m_fadeInTime_seconds = fadeInTime_seconds;
 		m_tickRatio = tickCount = 1.0 / (((double)tickCount)+1.0);
 		
 		m_alpha = m_baseAlpha = 1;
 		
 		this.addStyleName("sm_magnifier");
 		m_zoomIn.addStyleName("sm_zoom_in_button");
 		m_zoomOut.addStyleName("sm_zoom_out_button");
 		m_dragger.addStyleName("sm_zoom_drag_button");
 		m_slider.addStyleName("sm_zoom_slider");
 		
 		smE_ZIndex.MAGNIFIER.assignTo(this);
 		
 		final FlowPanel middleContainer = new FlowPanel();
 		final VerticalPanel innerContainer = new VerticalPanel();
 		
 		middleContainer.add(m_dragger);
 		middleContainer.add(m_slider);
 		
 		innerContainer.add(m_zoomIn);
 		innerContainer.add(middleContainer);
 		innerContainer.add(m_zoomOut);
 		
 		smE_ZIndex.MAGNIFIER_DRAG_BUTTON.assignTo(m_dragger);
 		
 		m_dragger.setVisible(false);
 		m_zoomOut.setEnabled(false);
 		m_zoomIn.setEnabled(false);
 		m_slider.setEnabled(false);
 		
 		smToolTipManager toolTipper = m_viewContext.toolTipMngr;
 		toolTipper.addTip(m_zoomIn, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Zoom In"));
 		toolTipper.addTip(m_zoomOut, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Zoom Out"));
 		toolTipper.addTip(m_dragger, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Drag'n'Zoom"));
 		
 		m_args_SetCameraTarget.setUserData(this.getClass());
 		
 		this.add(innerContainer);
 		
 		m_viewContext.clickMngr.addClickHandler(m_zoomIn, new smI_ClickHandler()
 		{
 			public void onClick()
 			{
 				if( !m_zoomIn.isEnabled() )  return;
 				
 				double newRatio = 0;
 				double mod = m_currentRatio % m_tickRatio;
 				if( mod > (m_tickRatio/2) )
 				{
 					newRatio = m_currentRatio - mod;
 				}
 				else
 				{
 					newRatio = (m_currentRatio - mod) - m_tickRatio;
 				}
 				
 				newRatio = smU_Math.clamp(newRatio, 0, 1);
 				
 				smMagnifier.this.setDraggerPosition(newRatio, true);
 			}
 		});
 		
 		m_viewContext.clickMngr.addClickHandler(m_zoomOut, new smI_ClickHandler()
 		{
 			public void onClick()
 			{
 				if( !m_zoomOut.isEnabled() )  return;
 				
 				double newRatio = 0;
 				double mod = m_currentRatio % m_tickRatio;
 				if( mod < (m_tickRatio/2) )
 				{
 					newRatio = (m_currentRatio - mod) + m_tickRatio;
 				}
 				else
 				{
 					newRatio = (m_currentRatio - mod) + m_tickRatio*2;
 				}
 				
 				newRatio = smU_Math.clamp(newRatio, 0, 1);
 				
 				smMagnifier.this.setDraggerPosition(newRatio, true);
 			}
 		});
 		
 		m_slider.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				double mouseY = event.getRelativeY(smMagnifier.this.m_slider.getElement());
 				smMagnifier.this.setDraggerPositionFromMouse(mouseY);
 			}
 		});
 		
 		m_dragger.addMouseDownHandler(new MouseDownHandler()
 		{
 			@Override
 			public void onMouseDown(MouseDownEvent event)
 			{
 				onDraggerMouseDown(event, event.getRelativeY(smMagnifier.this.m_slider.getElement()));
 			}
 		});
 		
 		m_dragger.addDomHandler(new TouchStartHandler()
 		{
 			@Override
 			public void onTouchStart(TouchStartEvent event)
 			{
 				if( event.getTouches().length() > 1 )  return;
 				
 				Touch touch = event.getTouches().get(0);
 				m_touchY = touch.getRelativeY(smMagnifier.this.m_slider.getElement());
 				onDraggerMouseDown(event, m_touchY);
 			}
 		}, TouchStartEvent.getType());
 		
 		
 		m_dragger.addMouseMoveHandler(new MouseMoveHandler()
 		{
 			@Override
 			public void onMouseMove(MouseMoveEvent event)
 			{
 				onDraggerMouseMove(event, event.getRelativeY(smMagnifier.this.m_slider.getElement()));
 			}
 		});
 		m_dragger.addDomHandler(new TouchMoveHandler()
 		{
 			@Override
 			public void onTouchMove(TouchMoveEvent event)
 			{
 				Touch touch = event.getTouches().get(0);
 				m_touchY = touch.getRelativeY(smMagnifier.this.m_slider.getElement());
 				onDraggerMouseMove(event, m_touchY);
 			}
 		}, TouchMoveEvent.getType());
 		
 		
 		m_dragger.addMouseUpHandler(new MouseUpHandler()
 		{
 			@Override
 			public void onMouseUp(MouseUpEvent event)
 			{
 				onDraggerMouseUp(event, event.getRelativeY(smMagnifier.this.m_slider.getElement()));
 			}
 		});
 		
 		m_dragger.addDomHandler(new TouchEndHandler()
 		{
 			@Override
 			public void onTouchEnd(TouchEndEvent event)
 			{
 				if(event.getTouches().length() == 0 )
 				{
 					onDraggerMouseUp(event, m_touchY);
 				}
 			}
 		}, TouchEndEvent.getType());
 	}
 	
 	private void onDraggerMouseDown(HumanInputEvent event, double relativeY)
 	{
 		Event.setCapture(smMagnifier.this.m_dragger.getElement());
 		event.preventDefault();
 		
 		m_isMouseDown = true;
 		
 		 //--- DRK > No idea why draggerTop has to be offset by half the dragger height...NO IDEA.
 		double draggerTop = m_dragger.getAbsoluteTop() - DRAGGER_HEIGHT_DIV_2;
 		double draggerY = (draggerTop - DRAGGER_START_Y) + DRAGGER_HEIGHT_DIV_2;
 		double mouseY = relativeY;
 		
 		setDraggerPositionFromMouse(draggerY);
 		
 		m_mouseDownOffset = draggerY - mouseY;
 	}
 	
 	private void onDraggerMouseMove(HumanInputEvent event, double relativeY)
 	{
 		if( m_isMouseDown )
 		{
 			double mouseY = relativeY;
 			smMagnifier.this.setDraggerPositionFromMouse(mouseY + m_mouseDownOffset);
 			
 			event.preventDefault();
 		}
 	}
 	
 	private void onDraggerMouseUp(HumanInputEvent event, double relativeY)
 	{
 		if( m_isMouseDown )
 		{
 			Event.releaseCapture(smMagnifier.this.m_dragger.getElement());
 			event.preventDefault();
 			
 			//--- DRK > Pretty hacky, but I can't figure out how to "forward" the event up the DOM so tooltip can get it.
 			if( event instanceof MouseUpEvent )
 			{
 				m_viewContext.toolTipMngr.onMouseUp((MouseUpEvent)event);
 			}
 			
 			double mouseY = relativeY;
 			smMagnifier.this.setDraggerPositionFromMouse(mouseY + m_mouseDownOffset);
 			
 			m_isMouseDown = false;
 		}
 	}
 	
 	private void initValues()
 	{
 		SLIDER_HEIGHT = m_slider.getOffsetHeight();
 		DRAGGER_START_Y = m_zoomIn.getElement().getOffsetHeight()-1;
 		DRAGGER_HEIGHT = 18;//m_dragger.getOffsetHeight(); TODO: UGH, can't get correct value from DOM here for some reason.
 		DRAGGER_HEIGHT_DIV_2 = DRAGGER_HEIGHT/2;
 	}
 	
 	private void setDraggerPositionFromMouse(double mouseY)
 	{
 		mouseY -= DRAGGER_HEIGHT_DIV_2;
 		double clickArea = (SLIDER_HEIGHT - DRAGGER_HEIGHT);
 		double ratio = mouseY / clickArea;
 		ratio = smU_Math.clamp(ratio, 0, 1);
 		
 		this.setDraggerPosition(ratio, true);
 	}
 	
 	private void setDraggerPositionFromCamera()
 	{
 		smCamera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		double maxZ = camera.calcMaxZ();
 		double ratio = camera.getPosition().getZ() / maxZ;
 		ratio = smU_Math.clamp(ratio, 0, 1); // window resizes can make camera be temporarily zoomed out further than its max constraint.
 		ratio = Math.sqrt(ratio);
 		
 		/*s_logger.info("ratio: " + ratio);
 		s_logger.info("z: " + camera.getPosition().getZ());
 		s_logger.info("maxZ: " + maxZ);
 		s_logger.info("-----");*/
 		
 		this.setDraggerPosition(ratio, false);
 	}
 	
 	private void setDraggerPosition(double ratio, boolean moveCamera)
 	{
 		if( m_currentRatio == 0 )
 		{
 			if( ratio > 0 )
 			{
 				m_zoomIn.setEnabled(true);
 			}
 		}
 		else if( m_currentRatio == 1 )
 		{
 			if( ratio < 1 )
 			{
 				m_zoomOut.setEnabled(true);
 			}
 		}
 		
 		if( ratio == 0 )
 		{
 			m_zoomIn.setEnabled(false);
 		}
 		else if( ratio == 1 )
 		{
 			m_zoomOut.setEnabled(false);
 		}
 		
 		double sliderPosition = (SLIDER_HEIGHT-DRAGGER_HEIGHT)*ratio;
 		sliderPosition = DRAGGER_START_Y + sliderPosition;
 		sliderPosition = Math.round(sliderPosition);
 		m_dragger.getElement().getStyle().setTop(sliderPosition, Unit.PX);
 		
 		if( moveCamera )
 		{
 			smCamera camera = m_viewContext.appContext.cameraMngr.getCamera();
 			double maxZ = camera.calcMaxZ();
 			m_utilPoint.copy(camera.getPosition());
 			m_utilPoint.setZ((ratio*ratio)*maxZ);
 			
 			boolean setTarget = false;
 			
 			if ( m_cameraState instanceof State_ViewingCell )
 			{
 				if ( ratio > 0 )
 				{
 					setTarget = true;
 				}
 			}
 			else
 			{
 				setTarget = true;
 			}
 			
 			if ( setTarget )
 			{
 				m_args_SetCameraTarget.init(m_utilPoint, false);
 				m_viewContext.stateContext.performAction(Action_Camera_SetCameraTarget.class, m_args_SetCameraTarget);
 			}
 		}
 		
 		m_currentRatio = ratio;
 	}
 	
 	private void startFadeIn()
 	{
 		m_baseAlpha = m_alpha;
 		this.setVisible(true);
 	}
 	
 	private void setAlpha(double alpha)
 	{
 		m_alpha = alpha;
		//s_logger.severe(m_baseAlpha + "   " + m_alpha +"");
 		this.getElement().getStyle().setOpacity(m_alpha);
 	}
 
 	@Override
 	public void onStateEvent(smStateEvent event)
 	{
 		switch(event.getType())
 		{
 			case DID_ENTER:
 			{
 				if( event.getState() instanceof StateMachine_Camera )
 				{
 					initValues();
 					setDraggerPositionFromCamera();
 					
 					m_dragger.setVisible(true); // only made visible here so it doesn't appear out of place initially.
 					
 					m_zoomOut.setEnabled(true);
 					m_zoomIn.setEnabled(true);
 					m_slider.setEnabled(true);
 				}
 				else if( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					m_cameraState = event.getState();
 					
 					if( m_cameraState instanceof State_CameraSnapping )
 					{
 						m_underThisControl = false;
 						m_baseAlpha = m_alpha;
 					}
 					else if( m_cameraState instanceof State_ViewingCell )
 					{
 						this.setVisible(false);
 						this.setAlpha(0);
 					}
 				}
 				break;
 			}
 			
 			case DID_EXIT:
 			{
 				if( event.getState() instanceof State_ViewingCell || event.getState() instanceof State_CameraSnapping )
 				{
 					if( !m_viewContext.stateContext.isEntered(State_CameraSnapping.class) && !m_viewContext.stateContext.isEntered(State_ViewingCell.class))
 					{
 						startFadeIn();
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_UPDATE:
 			{
 				if( event.getState() instanceof StateMachine_Camera )
 				{
 					if( !m_underThisControl )
 					{
 						if( !m_viewContext.appContext.cameraMngr.isCameraAtRest() )
 						{
 							setDraggerPositionFromCamera();
 						}
 					}
 				}
 				else if( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					if( event.getState() instanceof State_CameraSnapping )
 					{
 						State_CameraSnapping cameraSnapping = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
 						if( cameraSnapping != null && cameraSnapping.getPreviousState() != State_ViewingCell.class )
 						{
 							this.setAlpha(m_baseAlpha * (1 - cameraSnapping.getOverallSnapProgress()));
 						}
 					}
 					else if( !(event.getState() instanceof State_ViewingCell) )
 					{
 						if( m_alpha < 1 )
 						{
 							if( event.getState().isEntered() )
 							{
 								double timeMantissa = event.getState().getTimeInState(smE_StateTimeType.TOTAL) / m_fadeInTime_seconds;
 								timeMantissa = smU_Math.clamp(timeMantissa, 0, 1);
 								
 								this.setAlpha(m_baseAlpha + (1-m_baseAlpha)*timeMantissa);
 							}
 						}
 					}
 				}
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if( event.getAction() == Action_Camera_SetCameraTarget.class )
 				{
 					if( event.getActionArgs() != null )
 					{
 						if( event.getActionArgs().getUserData() != smMagnifier.class )
 						{
 							m_underThisControl = false;
 						}
 						else
 						{
 							m_underThisControl = true;
 						}
 					}
 					else
 					{
 						m_underThisControl = false;
 					}
 				}
 				else if( event.getAction()  == Action_Camera_SetCameraViewSize.class )
 				{
 					this.setDraggerPositionFromCamera();
 				}
 				else if( event.getAction()  == StateMachine_Base.OnGridUpdate.class )
 				{
 					this.setDraggerPositionFromCamera();
 				}
 				else if( event.getAction() == Action_Camera_SetInitialPosition.class )
 				{
 					this.setDraggerPositionFromCamera();
 				}
 				
 				break;
 			}
 		}
 	}
 }
