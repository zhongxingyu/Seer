 package swarm.client.view.cell;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.logging.Logger;
 
 import swarm.client.app.A_ClientApp;
 import swarm.client.app.AppContext;
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.Camera;
 import swarm.client.entities.ClientGrid;
 import swarm.client.entities.I_CellVisualization;
 import swarm.client.managers.CameraManager;
 import swarm.client.managers.CellBuffer;
 import swarm.client.managers.CellBufferManager;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.view.E_ZIndex;
 import swarm.client.view.S_UI;
 import swarm.client.view.U_Css;
 import swarm.client.view.U_View;
 import swarm.client.view.ViewContext;
 import swarm.client.view.cell.Cell1ImageLoader.Cell1Proxy;
 import swarm.client.view.cell.MetaImageLoader.MetaImageProxy;
 import swarm.client.view.sandbox.SandboxManager;
 import swarm.client.view.tabs.code.I_CodeLoadListener;
 import swarm.client.view.widget.UIBlocker;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.utils.U_Bits;
 import swarm.shared.utils.U_Math;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.entities.E_CodeSafetyLevel;
 import swarm.shared.entities.E_CodeType;
 import swarm.shared.structs.Code;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.MutableCode;
 import swarm.shared.structs.Point;
 import swarm.shared.structs.Rect;
 
 import com.dougkoellmer.client.app.ClientApp;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 
 public class VisualCell extends AbsolutePanel implements I_CellVisualization
 {
 	private static final double DISABLE_TIMER = -1.0;
 	private static final double ENABLE_TIMER = 0.0;
 	
 	
 	public static interface I_EventListener
 	{
 		void onCodeLoaded(VisualCell cell);
 		void onMetaImageLoaded();
 		void onMetaImageRendered();
 		void onPopped(VisualCell cell);
 	}
 	static enum LayoutState
 	{
 		NOT_CHANGING,
 		CHANGING_FROM_TIME,
 		CHANGING_FROM_SNAP;
 	};
 	
 	private static final Logger s_logger = Logger.getLogger(VisualCell.class.getName());
 	
 	private static int s_currentId = 0;
 	
 	private static final class CodeLoadListener implements I_CodeLoadListener
 	{
 		private final VisualCell m_this;
 		
 		CodeLoadListener(VisualCell thisArg)
 		{
 			m_this = thisArg;
 		}
 		
 		@Override public void onCodeLoad()
 		{
 			m_this.clearStatusHtml();
 		}
 	}
 	
 	private final CellImageProxy.I_Listener m_metaLoadListener = new CellImageProxy.I_Listener()
 	{
 		@Override public void onRendered(CellImageProxy entry)
 		{
 			if( entry != m_metaImageProxy )  return; // should never happen.
 			
 			ensureFadedIn();
 			m_eventListener.onMetaImageRendered();
 		}
 		
 		@Override public void onLoaded(CellImageProxy entry)
 		{
 			if( entry != m_metaImageProxy )  return; // should never happen.
 			
 			m_contentPanel.setVisible(true);
 			m_eventListener.onMetaImageLoaded();
 		}
 
 		@Override public void onLoadFailed(CellImageProxy entry)
 		{
 		}
 	};
 	
 	private final CellImageProxy.I_Listener m_cell1LoadListener = new CellImageProxy.I_Listener()
 	{
 		@Override public void onRendered(CellImageProxy entry)
 		{
 			if( entry != m_cell1Proxy )  return; // should never happen.
 			
 			ensureFadedIn();
 		}
 		
 		@Override public void onLoaded(CellImageProxy proxy)
 		{
 			onLoadOrFail(proxy);
 		}
 		
 		@Override public void onLoadFailed(CellImageProxy proxy)
 		{
 			onLoadOrFail(proxy);
 		}
 		
 		private void onLoadOrFail(CellImageProxy proxy)
 		{
 			if( proxy != m_cell1Proxy )  return; // should never happen.
 			
 			setDefaultZIndex();
 			VisualCell.this.setVisible(true);
 			m_contentPanel.setVisible(true);
 		}
 	};
 	
 	private static class QueuedSetCode
 	{
 		private final Code m_code;
 		private final String m_namespace;
 		
 		public QueuedSetCode(Code code, String namespace)
 		{
 			m_code = code;
 			m_namespace = namespace;
 		}
 	}
 	
 	private I_EventListener m_eventListener;
 	private int m_id;
 	private final AbsolutePanel m_contentPanel = new AbsolutePanel();
 	private final UIBlocker	m_statusPanel = new UIBlocker();
 	private final AbsolutePanel m_glassPanel = new AbsolutePanel();
 	private final I_CellSpinner m_spinner;
 	private final MutableCode m_utilCode = new MutableCode(E_CodeType.values());
 	private BufferCell m_bufferCell = null;
 	
 	private int m_subCellDimension = -1;
 	private int m_width = 0;
 	private int m_height = 0;
 	private int m_padding = 0;
 	
 	private int m_defaultWidth = 0;
 	private int m_defaultHeight = 0;
 	private int m_baseWidth = 0;
 	private int m_baseHeight = 0;
 	private int m_targetWidth = 0;
 	private int m_targetHeight = 0;
 	
 	private int m_targetXOffset = 0;
 	private int m_targetYOffset = 0;
 	private int m_xOffset = 0;
 	private int m_yOffset = 0;
 	private int m_baseXOffset = 0;
 	private int m_baseYOffset = 0;
 	
 	private int m_startingXOffset = 0;
 	private int m_startingYOffset = 0;
 	
 	private double m_baseChangeValue = 0;
 	
 	private boolean m_isValidated = false;
 	
 	private final CodeLoadListener m_codeLoadListener = new CodeLoadListener(this);
 	
 	private final SandboxManager m_sandboxMngr;
 	private final CameraManager m_cameraMngr;
 	
 	private E_CodeSafetyLevel m_codeSafetyLevel;
 	
 	private boolean m_isSnapping = false;
 	private boolean m_isFocused = false;
 	private boolean m_isPoppedUp = false;
 	private LayoutState m_layoutState = LayoutState.NOT_CHANGING;
 	private final double m_sizeChangeTime;
 	private final double m_retractionEasing;
 	
 	private double m_totalTimeSinceCreation = 0.0;
 	
 	private boolean m_fadingIn = false;
 	
 	
 	
 	
 	private int m_zIndex_default;
 	private int m_zIndex;
 	
 	private QueuedSetCode m_queuedCode = null;
 	
 	//--- DRK > Just don't want to tempt fate with browser reflows or anything so managing this myself.
 	private boolean m_visible = true;
 	
 	private boolean m_hasSetCodeYet = false;
 	
 	private double m_clearLoadingTimer = 0.0;
 	private double m_timeSinceFirstCodeSet = 0.0;
 	
 	private static final class CustomObscured extends ClientGrid.Obscured
 	{
 		private int m_obscuredCount = 0;
 		private final VisualCell m_cell;
 		
 		CustomObscured(VisualCell thisCell)
 		{
 			m_cell = thisCell;
 		}
 		
 		public void reset()
 		{
 			m_obscuredCount = 0;
 		}
 		
 		@Override public boolean stopOnCurrentObscuringCell()
 		{
 			CellBufferManager bufferMngr = m_cell.m_appContext.cellBufferMngr;
 			int index = U_Bits.calcBitPosition(this.subCellCount);
 			CellBuffer buffer = bufferMngr.getDisplayBuffer(index);
 			BufferCell cell = buffer.getCellAtAbsoluteCoord(this.m, this.n);
 			
 			if( cell == null ) return false;
 			
 			VisualCell visualCell = (VisualCell) cell.getVisualization();
 			
 			if( visualCell.isLoaded() )
 			{
 				m_obscuredCount++;
 			}
 			
 			return false;
 		}
 	}
 	
 	private final CustomObscured m_obscured = new CustomObscured(this);
 	private final AppContext m_appContext;
 	private final ViewContext m_viewContext;
 	
 	private final MetaImageLoader m_metaImageLoader;
 	private MetaImageLoader.MetaImageProxy m_metaImageProxy;
 	private boolean m_isMetaProbablyCachedInMemory = false;
 	
 	private final Cell1ImageLoader m_cell1ImageLoader;
 	private Cell1Proxy m_cell1Proxy;
 	
 	public VisualCell(ViewContext viewContext, I_CellSpinner spinner, SandboxManager sandboxMngr, CameraManager cameraMngr)
 	{
 		m_metaImageLoader = viewContext.metaImageLoader;
 		m_cell1ImageLoader = viewContext.cell1ImageLoader;
 		m_viewContext = viewContext;
 		m_appContext = viewContext.appContext;
 		m_retractionEasing = viewContext.config.cellRetractionEasing;
 		m_spinner = spinner;
 		m_cameraMngr = cameraMngr;
 		m_sandboxMngr = sandboxMngr;
 		m_sizeChangeTime = viewContext.config.cellSizeChangeTime_seconds;
 		m_id = s_currentId;
 		s_currentId++;
 		
 		this.addStyleName("visual_cell");
 		m_glassPanel.addStyleName("sm_cell_glass");
 
 		//m_backgroundPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		m_statusPanel.getElement().getStyle().setPosition(Position.RELATIVE);
		m_statusPanel.getElement().getStyle().setTop(-100, Unit.PCT);
 		m_glassPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
 		
 		m_statusPanel.setZIndex(E_ZIndex.CELL_STATUS.get());
 		E_ZIndex.CELL_GLASS.assignTo(m_glassPanel);
 		E_ZIndex.CELL_CONTENT.assignTo(m_contentPanel);
 		
 		m_statusPanel.setVisible(false);
 		
 		m_contentPanel.addStyleName("visual_cell_content");
 		
 //		this.getElement().getStyle().setOpacity(.5);
 		
 		U_Css.allowUserSelect(m_contentPanel.getElement(), false);
 		
 		this.add(m_contentPanel);
 		this.add(m_statusPanel);
 		this.add(m_glassPanel);
 	}
 	
 	@Override public void setVisible(boolean value)
 	{
 		if( value == m_visible )  return;
 		
 //		if( m_subCellDimension > 1 )
 //		{
 //			if( !value  )
 //			{
 //				s_logger.severe("visible false " + m_bufferCell.getCoordinate());
 //			}
 //			else
 //			{
 //				s_logger.severe("visible true " + m_bufferCell.getCoordinate());
 //			}
 //		}
 		
 		super.setVisible(value);
 		
 		m_visible = value;
 	}
 	
 	@Override public boolean isVisible()
 	{
 		return m_visible;
 	}
 	
 	public void setCodeListener(I_EventListener listener)
 	{
 		m_eventListener = listener;
 	}
 	
 	public BufferCell getBufferCell()
 	{
 		return m_bufferCell;
 	}
 	
 	LayoutState getSizeChangeState()
 	{
 		return m_layoutState;
 	}
 	
 	int getId()
 	{
 		return m_id;
 	}
 	
 	public int getWidth()
 	{
 		return m_width;
 	}
 	
 	int getHeight()
 	{
 		return m_height;
 	}
 	
 	private void fadeIn(double time)
 	{
 		if( !m_fadingIn || !m_hasSetCodeYet )  return;
 		
 		double alpha = time / m_viewContext.config.cellFadeInTime;
 
 //		alpha = Math.sqrt(alpha);
 		m_contentPanel.getElement().getStyle().setOpacity(alpha);
 		
 		if( alpha >= 1.0 )
 		{
 			m_fadingIn = false;
 		}
 	}
 	
 	private void ensureFadedOut()
 	{
 		m_contentPanel.getElement().getStyle().setOpacity(0.0);
 	}
 	
 	private void ensureFadedIn()
 	{
 		m_contentPanel.getElement().getStyle().setOpacity(1.0);
 	}
 	
 	public void update(double timeStep, int subCellCount_highest)
 	{
 		m_totalTimeSinceCreation += timeStep;
 		
 		if( m_hasSetCodeYet )
 		{
 			m_timeSinceFirstCodeSet += timeStep;
 		}
 		
 		if( m_metaImageProxy != null )
 		{
 			if( m_metaImageProxy.getState().ordinal() >= E_ImageLoadState.RENDERING.ordinal() )
 			{
 				fadeIn(m_metaImageProxy.getTimeRendering());
 			}
 		}
 		else if( m_subCellDimension == 1 )
 		{
 			fadeIn(m_timeSinceFirstCodeSet);
 			
 			if( m_hasSetCodeYet && m_clearLoadingTimer >= ENABLE_TIMER )
 			{
 				m_clearLoadingTimer += timeStep;
 				
 				if( m_clearLoadingTimer >= m_viewContext.config.cellFadeInTime )
 				{
 					clearLoading_private();
 				}
 			}
 		}
 		
 //		double timeToDeath = m_bufferCell.getTimeToDeath();
 //		if( timeToDeath >= 0 )
 //		{
 //			if( m_subCellDimension > subCellCount_highest )
 //			{
 //				double alpha = timeToDeath / m_bufferCell.getTotalDeathTime();
 //				this.getElement().getStyle().setOpacity(alpha);
 //			}
 //			else
 //			{
 //				this.getElement().getStyle().setOpacity(1.0);
 //			}
 //		}
 		
 		if( m_spinner.asWidget().getParent() != null )
 		{
 //			s_logger.severe(timeStep+"");
 			m_spinner.update(timeStep);
 		}
 
 		if( this.m_layoutState == LayoutState.CHANGING_FROM_SNAP )
 		{
 //			s_logger.severe("update: changing from snap");
 			double snapProgress = m_cameraMngr.getWeightedSnapProgress();
 			//s_logger.severe("cell: " + " " + m_baseChangeValue + " " + snapProgress + " ");
 			double mantissa = m_baseChangeValue == 1 ? 1 : (snapProgress - m_baseChangeValue) / (1-m_baseChangeValue);
 			mantissa = U_Math.clampMantissa(mantissa);
 				
 			this.updateLayout(mantissa, mantissa);
 		}
 		else if( this.m_layoutState == LayoutState.CHANGING_FROM_TIME )
 		{
 //			s_logger.severe("update: changing from time");
 			m_baseChangeValue += timeStep;
 			double mantissa = m_baseChangeValue / m_sizeChangeTime;
 			mantissa = U_Math.clampMantissa(mantissa);
 			
 			double easingMultiplier = 2.25;
 			
 			double retractionEasingX = m_retractionEasing - 1;
 			retractionEasingX += m_baseWidth / m_defaultWidth;
 			retractionEasingX *= easingMultiplier;
 			double mantissaX = U_View.easeMantissa(mantissa, retractionEasingX);
 
 			double retractionEasingY = m_retractionEasing - 1;
 			retractionEasingY += m_baseHeight / m_defaultHeight;
 			retractionEasingY *= easingMultiplier;
 			double mantissaY = U_View.easeMantissa(mantissa, retractionEasingY);
 			
 //			s_logger.severe("START");
 //			s_logger.severe(m_retractionEasing + " " + retractionEasingX + " " + retractionEasingY);
 //			s_logger.severe(mantissa + " " + mantissaX + " " + mantissaY);
 //			s_logger.severe("END");
 			
 			this.updateLayout(mantissaX, mantissaY);
 		}
 		
 //		if( m_subCellDimension == 16 && m_bufferCell.getCoordinate().isEqualTo(1, 1) )
 //		{
 //			s_logger.severe(m_contentPanel.isVisible()+"");
 //		}
 	}
 	
 	private void updateLayout(double progressMantissaX, double progressMantissaY)
 	{
 		double widthDelta = (m_targetWidth - m_baseWidth) * progressMantissaX;
 		m_width = (int) (m_baseWidth + widthDelta);
 		
 		double heightDelta = (m_targetHeight - m_baseHeight) * progressMantissaY;
 		m_height = (int) (m_baseHeight + heightDelta);
 		
 		double xOffsetDelta = (m_targetXOffset - m_baseXOffset) * progressMantissaX;
 		m_xOffset = (int) (m_baseXOffset + xOffsetDelta);
 		
 		double yOffsetDelta = (m_targetYOffset - m_baseYOffset) * progressMantissaY;
 		m_yOffset = (int) (m_baseYOffset + yOffsetDelta);
 		
 		//s_logger.severe(m_xOffset + " " + m_targetXOffset + " " + m_baseXOffset);
 		
 		if( progressMantissaX >= 1 )
 		{
 			this.ensureTargetLayout();
 		}
 		else
 		{
 			this.flushLayout();
 		}
 	}
 	
 	void validate()
 	{
 		if( m_isValidated )  return;
 		
 		if( m_subCellDimension == 1 )
 		{
 			this.flushLayout();
 //			this.getElement().getStyle().setPaddingRight(m_padding, Unit.PX);
 //			this.getElement().getStyle().setPaddingBottom(m_padding, Unit.PX);
 			//m_backgroundPanel.setSize(m_width+m_padding + "px", m_height+m_padding + "px");
 			
 			//--- DRK > Rare case of jumping from beyond max imaged zoom to all the way to cell size 1,
 			//---		but could technically happen with bad frame rate or something, so clearing this here just in case.
 			//m_backgroundPanel.getElement().getStyle().clearBackgroundColor();
 			
 			m_contentPanel.addStyleName("visual_cell_content");
 		}
 		else if( m_subCellDimension > 1 )
 		{
 //			U_Debug.ASSERT(false, "not implemented");
 			this.clearStatusHtml(); // shouldn't have to be done, but what the hell.
 			
 			this.setSize(m_width+"px", m_height+"px");
 			this.getElement().getStyle().clearPaddingRight();
 			this.getElement().getStyle().clearPaddingBottom();
 			//m_backgroundPanel.setSize(m_width+"px", m_height+"px");
 			
 			if( m_subCellDimension > S_CommonApp.MAX_IMAGED_CELL_SIZE )
 			{				
 				//m_backgroundPanel.getElement().getStyle().setBackgroundColor("white");
 			}
 			else
 			{
 				//m_backgroundPanel.getElement().getStyle().clearBackgroundColor();
 			}
 			
 			m_contentPanel.removeStyleName("visual_cell_content");
 		}
 		
 		m_isValidated = true;
 	}
 	
 	void setMetaSize(double width, double height)
 	{
 		m_width = m_defaultWidth = (int) width;
 		m_height = m_defaultHeight = (int) height;
 		
 		setSize(width+"px", height+"px");
 	}
 	
 	void crop(int thisX, int thisY, int windowWidth, int windowHeight)
 	{
 //		int totalWidth = m_width+m_padding;
 //		int totalHeight = m_height+m_padding;
 		int totalWidth = m_width;
 		int totalHeight = m_height;
 		thisX -= m_xOffset;
 		thisY -= m_yOffset;
 		
 		int overflow = (thisX + totalWidth) - windowWidth;
 		
 		if( overflow > 0 )
 		{
 			totalWidth -= overflow;
 		}
 		
 		overflow = (thisY + totalHeight) - windowHeight;
 		
 		if( overflow > 0)
 		{
 			totalHeight -= overflow;
 		}
 		
 		totalWidth = totalWidth >= 0 ? totalWidth : 0;
 		totalHeight = totalHeight >= 0 ? totalHeight : 0;
 		
 		m_contentPanel.setSize(m_defaultWidth + "px", m_defaultHeight + "px");
 		m_statusPanel.setSize(m_defaultWidth + "px", m_defaultHeight + "px");
 		this.setSize(totalWidth + "px", totalHeight + "px");
 	}
 	
 	void removeCrop()
 	{
 		this.flushLayout();
 		m_contentPanel.getElement().getStyle().clearWidth();
 		m_contentPanel.getElement().getStyle().clearHeight();
 		m_statusPanel.getElement().getStyle().clearWidth();
 		m_statusPanel.getElement().getStyle().clearHeight();
 	}
 	
 	private void flushLayout()
 	{
 		this.setSize(m_width + "px", m_height + "px");
 	}
 	
 	@Override public void onRevealed()
 	{
 		m_obscured.m_obscuredCount--;
 		
 		if( m_obscured.m_obscuredCount <= 0 )
 		{
 			this.setVisible(true);
 			setDefaultZIndex();
 		}
 	}
 	
 	public void onCreate(BufferCell bufferCell, int width, int height, int padding, int subCellDimension, int highestPossibleSubCellCount, boolean justRemovedMetaCountOverride)
 	{
 //		if( subCellDimension == 1 && bufferCell.getCoordinate().isEqualTo(16, 14) )
 //		{
 //			s_logger.severe("MADE THE CELL");
 //		}
 //		else
 //		{
 //			s_logger.severe("MADE cell>1");
 //		}
 		
 //		if( subCellDimension == 1 )
 //		{
 //			s_logger.severe("MADE THE CELL");
 //		}
 		
 		if( subCellDimension == 1 )
 		{
 			this.getElement().getStyle().clearLeft();
 			this.getElement().getStyle().clearTop();
 		}
 		else
 		{
 			final String transformProperty = m_viewContext.appContext.platformInfo.getTransformProperty();
 			this.getElement().getStyle().clearProperty(transformProperty);
 		}
 		
 		
 		m_bufferCell = bufferCell;
 		clearStatusHtmlForSure();
 		m_statusPanel.removeConstraints(width, height);
 		
 		Camera camera = m_cameraMngr.getCamera();
 		double deltaZ = camera.getPosition().getZ() - camera.getPrevPosition().getZ();
 		boolean couldBeZoomingInFromMeta = deltaZ < 0.0 || justRemovedMetaCountOverride;
 		
 		onCreatedOrRecycled(width, height, padding, subCellDimension);
 		
 		int bitPosition = U_Bits.calcBitPosition(m_subCellDimension);
 		m_zIndex_default = E_ZIndex.values()[E_ZIndex.CELL_1.ordinal() - bitPosition].get();
 		
 		if( subCellDimension == 1  )
 		{
 			setZIndex(E_ZIndex.CELL_1_LOADING.get());
 		}
 		else
 		{
 			setDefaultZIndex();
 		}
 		
 		this.showEmptyContent();
 
 		if( couldBeZoomingInFromMeta )
 		{
 			//--- DRK > Hmm with reveal logic now I'm not sure this is needed.
 			if( subCellDimension == 1 )
 			{
 				m_fadingIn = false;
 				ensureFadedIn();
 			}
 		}
 		else
 		{
 			m_fadingIn = true;
 			ensureFadedOut();
 		}
 		
 		//--- DRK > Originally was just doing this for the zoom in case, but technically
 		//---		we could zoom in then pan real fast and create a cell_1 that is still covered
 		//---		by a meta on death row.
 		boolean obscuredAsCell1 = false;
 		if( subCellDimension == 1  )
 		{
 			ClientGrid grid = (ClientGrid) bufferCell.getGrid();
 			GridCoordinate coord = bufferCell.getCoordinate();
 			m_obscured.reset();
 			grid.isObscured(coord.getM(), coord.getN(), 1, highestPossibleSubCellCount, m_obscured);
 			
 			if( m_obscured.m_obscuredCount > 0 )
 			{
 				m_fadingIn = false;
 				ensureFadedIn();
 				obscuredAsCell1 = true;
 			}
 		}
 		
 		setVisible(!obscuredAsCell1);
 		
 //		if( subCellDimension > 1 )
 //		{
 //			this.getElement().getStyle().setOpacity(0.0);
 //		}
 //		else
 //		{
 //			this.getElement().getStyle().setOpacity(1.0);
 //		}
 	}
 	
 	public int getZIndex()
 	{
 		return m_zIndex;
 	}
 	
 	private void setZIndex(int value)
 	{
 		if( value == m_zIndex )  return;
 		
 		m_zIndex = value;
 		
 		this.getElement().getStyle().setZIndex(m_zIndex);
 	}
 	
 	public void setDefaultZIndex()
 	{
 		//--- DRK > Little sloppy but a failsafe against bad z-index logic.
 		if( m_isFocused || m_isSnapping )  return;
 		
 		this.setZIndex(m_zIndex_default);
 	}
 	
 	private void onCreatedOrRecycled(int width, int height, int padding, int subCellDimension)
 	{		
 		m_isMetaProbablyCachedInMemory = false;
 		m_timeSinceFirstCodeSet = 0.0;
 		m_totalTimeSinceCreation = 0.0;
 		m_clearLoadingTimer = DISABLE_TIMER;
 		m_hasSetCodeYet = false;
 		m_isPoppedUp = false;
 		m_queuedCode = null;
 		m_layoutState = LayoutState.NOT_CHANGING;
 		m_isFocused = false;
 		m_isValidated = false;
 		m_subCellDimension = subCellDimension;
 		
 		m_targetWidth = m_defaultWidth = m_baseWidth = m_width = width;
 		m_targetHeight = m_defaultHeight = m_baseHeight = m_height = height;
 		m_targetXOffset = m_baseXOffset = m_xOffset = 0;
 		m_targetYOffset = m_baseYOffset = m_yOffset = 0;
 		m_padding = padding;
 
 		this.setScrollMode(E_ScrollMode.NOT_SCROLLING);
 		this.removeCrop();
 		
 		if( m_subCellDimension == 1 )
 		{
 			this.getElement().getStyle().setBackgroundColor("#ffffff");
 //			this.getElement().getStyle().setBackgroundColor("#ff0000");
 //			m_contentPanel.getElement().getStyle().setBackgroundColor("#00ff00");
 		}
 		else
 		{
 			this.getElement().getStyle().clearBackgroundColor();
 		}
 	}
 	
 	public int calcNaturalHeight()
 	{
 		int naturalHeight = m_contentPanel.getElement().getScrollHeight();
 		naturalHeight = naturalHeight < m_defaultHeight ? m_defaultHeight : naturalHeight;
 		
 		return naturalHeight;
 	}
 	
 	public int calcNaturalWidth()
 	{
 		int naturalWidth = m_contentPanel.getElement().getScrollWidth();
 		naturalWidth = naturalWidth < m_defaultWidth ? m_defaultWidth : naturalWidth;
 		
 		return naturalWidth;
 	}
 	
 	public void setTargetLayout(int width, int height, int xOffset, int yOffset, int windowWidth, int windowHeight, int scrollX, int scrollY)
 	{
 		width = width < m_defaultWidth ? m_defaultWidth : width;
 		height = height < m_defaultHeight ? m_defaultHeight : height;
 		
 		m_baseXOffset = m_xOffset;
 		m_baseYOffset = m_yOffset;
 		m_baseWidth = this.m_width;
 		m_baseHeight = this.m_height;
 
 		m_targetWidth = width;
 		m_targetHeight = height;
 		
 		m_targetXOffset = xOffset;
 		m_targetYOffset = yOffset;
 		
 		if( this.m_isSnapping )
 		{
 			m_baseChangeValue = m_cameraMngr.getWeightedSnapProgress();
 			m_layoutState = LayoutState.CHANGING_FROM_SNAP;
 			
 			constrainStatusBlocker(windowWidth, windowHeight, scrollX, scrollY);
 		}
 		else if( this.m_isFocused )
 		{
 			this.ensureTargetLayout();
 			
 			m_startingXOffset = m_xOffset;
 			m_startingYOffset = m_yOffset;
 			
 			constrainStatusBlocker(windowWidth, windowHeight, scrollX, scrollY);
 		}
 	}
 	
 	private int calcSizeConstraint(int defaultSize, int scroll, int windowSize, int cellSize)
 	{
 		int size = defaultSize;
 		int subtraction = m_padding - scroll;
 		subtraction = subtraction < 0 ? 0 : subtraction;
 		size = windowSize - subtraction;
 		
 		int total = scroll + windowSize - m_padding;
 		
 		if( total > cellSize )
 		{
 			size -= (total - cellSize);
 		}
 		
 		return size;
 	}
 	
 	public void constrainStatusBlocker(int windowWidth, int windowHeight, int scrollX, int scrollY)
 	{
 		boolean constrain = false;
 		int top = 0, left = 0, width = m_width, height = m_height;
 		
 		if( scrollX > m_padding )
 		{
 			constrain = true;
 			left = scrollX - m_padding;
 		}
 		
 		if( scrollY > m_padding )
 		{
 			constrain = true;
 			top = scrollY - m_padding;
 		}
 		
 		if( m_width + m_padding > windowWidth )
 		{
 			constrain = true;
 			
 			width = calcSizeConstraint(width, scrollX, windowWidth, m_width);
 		}
 		
 		if( m_height + m_padding > windowHeight )
 		{
 			constrain = true;
 
 			height = calcSizeConstraint(height, scrollY, windowHeight, m_height);
 		}
 		
 		if( constrain )
 		{
 			m_statusPanel.constrain(top, left, width, height);
 		}
 		else
 		{
 			m_statusPanel.removeConstraints(m_defaultWidth, m_defaultHeight);
 		}
 	}
 	
 	private void ensureTargetLayout()
 	{
 //		s_logger.severe("ensuring target layout");;
 		
 		//--- If we get the focused cell size while viewing cell,
 		//--- we don't make user wait, and just instantly expand it.
 		//--- Maybe a little jarring, but should be fringe case.
 		m_baseWidth = m_width = m_targetWidth;
 		m_baseHeight = m_height = m_targetHeight;
 		m_baseXOffset = m_xOffset = m_targetXOffset;
 		m_baseYOffset = m_yOffset = m_targetYOffset;
 		m_layoutState = LayoutState.NOT_CHANGING;
 		
 		this.flushLayout();
 	}
 	
 	public void onDestroy()
 	{
 //		if( m_subCellDimension == 1 && m_bufferCell.getCoordinate().isEqualTo(16, 14) )
 //		{
 //			s_logger.severe("DESTROYED THE CELL");
 //		}
 		
 		if( m_metaImageProxy != null )
 		{
 			m_metaImageProxy.onDettached();
 			m_metaImageProxy = null;
 		}
 		
 		if( m_cell1Proxy != null )
 		{
 			m_cell1Proxy.onDettached();
 			m_cell1Proxy = null;
 		}
 		
 		m_bufferCell = null;
 		m_isFocused = false;
 		m_subCellDimension = -1;
 		m_isPoppedUp = false;
 
 		if( m_codeSafetyLevel != null && !m_codeSafetyLevel.isStatic() )
 		{
 			this.insertSafeHtml("");
 		}
 		
 		m_codeSafetyLevel = null;
 		
 		this.pushDown();
 	}
 	
 	void setScrollMode(E_ScrollMode mode)
 	{
 		if( mode == E_ScrollMode.SCROLLING_FOCUSED || mode == E_ScrollMode.NOT_SCROLLING )
 		{
 			this.getElement().getStyle().setPosition(Position.ABSOLUTE);
 		}
 		else
 		{
 			this.getElement().getStyle().setPosition(Position.FIXED);
 		}
 	}
 	
 	@Override public void onFocusGained()
 	{
 		this.setVisible(true);
 		
 		m_isSnapping = false;
 		m_isFocused = true;
 		
 		setZIndex(E_ZIndex.CELL_FOCUSED.get());
 		this.ensureTargetLayout();
 		
 		m_startingXOffset = m_xOffset;
 		m_startingYOffset = m_yOffset;
 		
 		this.m_glassPanel.setVisible(false);
 		this.addStyleName("visual_cell_focused");
 
 		U_Css.allowUserSelect(m_contentPanel.getElement(), true);
 		
 		m_sandboxMngr.allowScrolling(m_contentPanel.getElement(), true);
 	}
 	
 	@Override public void onFocusLost()
 	{
 		m_isSnapping = false; // just in case.
 		m_isFocused = false;
 		m_isPoppedUp = false; // just in case...perhaps not technically accurate (still popped z-index-wise while fader is fading out)...but more convenient for use case for hud for now
 		m_statusPanel.removeConstraints(m_defaultWidth, m_defaultHeight);
 		
 		this.m_glassPanel.setVisible(true);
 		this.removeStyleName("visual_cell_focused");
 		U_Css.allowUserSelect(m_contentPanel.getElement(), false);
 		m_sandboxMngr.allowScrolling(m_contentPanel.getElement(), false);
 		setZIndex(E_ZIndex.CELL_POPPED.get());
 		
 		/*if( m_sandboxMngr.isRunning() )
 		{
 			m_sandboxMngr.stop(m_contentPanel.getElement());
 		}*/
 		
 		this.setToTargetSizeDefault();
 	}
 	
 	public int getXOffset()
 	{
 		return m_xOffset;
 	}
 	
 	public int getYOffset()
 	{
 		return m_yOffset;
 	}
 	
 	public int getStartingXOffset()
 	{
 		return m_startingXOffset;
 	}
 	
 	public int getStartingYOffset()
 	{
 		return m_startingYOffset;
 	}
 	
 	public void calcTopLeft(Point point_out)
 	{
 		A_Grid grid = m_bufferCell.getGrid();
 		m_bufferCell.getCoordinate().calcPoint(point_out, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
 		point_out.inc(m_xOffset, m_yOffset, 0);
 	}
 	
 	public void calcTargetTopLeft(Point point_out)
 	{
 		A_Grid grid = m_bufferCell.getGrid();
 		m_bufferCell.getCoordinate().calcPoint(point_out, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
 		point_out.inc(m_targetXOffset, m_targetYOffset, 0);
 	}
 	
 	public int getTargetWidth()
 	{
 		return m_targetWidth;
 	}
 	
 	public int getTargetHeight()
 	{
 		return m_targetHeight;
 	}
 	
 	private void setToTargetSizeDefault()
 	{
 //		s_logger.severe("setting to default size");
 		m_layoutState = LayoutState.CHANGING_FROM_TIME;
 		m_baseChangeValue = 0;
 		this.setTargetLayout(m_defaultWidth, m_defaultHeight, 0, 0, 0, 0, 0, 0);
 	}
 	
 	public void popUp()
 	{
 		m_isPoppedUp = true;
 		ensureFadedIn();
 		this.setVisible(true);
 		m_fadingIn = false;
 		
 		//--- DRK > Added this conditional because for fringe case of instant snap,
 		//--- 		onFocusGained can be called before popUp. I thought this case wasn't
 		//---		a problem before, but may not have tested it, or something might have changed.
 		//---		It does make sense that it's needed though, because onFocusGained call originates
 		//---		in state machine, and popUp is invoked from a UI handler of the state event,
 		//---		so comes later if the snap is instant.
 		if( !m_isFocused )
 		{			
 			setZIndex(E_ZIndex.CELL_POPPED.get());
 			m_isSnapping = true;
 		}
 		
 		if( m_eventListener != null )  m_eventListener.onPopped(this);
 	}
 	
 	public void cancelPopUp()
 	{
 		m_isPoppedUp = false;
 		boolean wasSnapping = m_isSnapping;
 		m_isSnapping = false;
 		
 		//--- DRK > The || wasSnapping was previously not here. Added because the snap animation, I guess due to
 		//---		some rounding error (and it was only reliably reproducible in debug mode where the time steps are
 		//---		pretty large), could complete before actually getting to the viewing cell state,
 		//---		thus changing layout state to NOT_CHANGING and preventing us from animating back to the default cell size.
 		if( m_layoutState == LayoutState.CHANGING_FROM_SNAP || wasSnapping)
 		{
 			this.setToTargetSizeDefault();
 			
 			U_Debug.ASSERT(wasSnapping, "expected cell to know it was snapping");
 		}
 	}
 	
 	public void pushDown()
 	{
 		setZIndex(E_ZIndex.CELL_PUSHED_BACK_DOWN.get());
 		m_isPoppedUp = false;
 	}
 	
 	@Override
 	public void onError(E_CodeType eType)
 	{
 		//TODO: These are placeholder error messages...should have some prettier error text, or maybe nothing at all?
 		
 		switch( eType )
 		{
 			case SPLASH:
 			{
 				this.clearStatusHtml();
 				this.showEmptyContent();
 				
 				break;
 			}
 			
 			case COMPILED:
 			{
 				this.setStatusHtml("Problem contacting server.", false);
 //				this.showEmptyContent();
 				
 				break;
 			}
 		}
 	}
 	
 	public boolean flushQueuedCode()
 	{
 		if( m_queuedCode != null )
 		{
 			if( !m_bufferCell.isOnDeathRow() )
 			{
 				setCode_private(m_queuedCode.m_code, m_queuedCode.m_namespace);
 				m_queuedCode = null;
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 	
 	@Override public void setCodeAfterFocusLost(Code code, String cellNamespace)
 	{
 		setCode_private(code, cellNamespace);
 	}
 	
 	@Override public void setCode(Code code, String cellNamespace)
 	{
 		boolean snappingOrFocused = m_subCellDimension == 1 && (m_isSnapping || m_isFocused);
 		
 		if( m_subCellDimension > 1 )
 		{
 			m_metaImageProxy = m_metaImageLoader.preLoad(code.getRawCode());
 			
 			m_isMetaProbablyCachedInMemory = m_metaImageProxy.isLoaded();
 		}
 		
 		if( !snappingOrFocused )//|| !alreadyRenderedMeta )
 		{
 			m_queuedCode = new QueuedSetCode(code, cellNamespace);
 			
 			return;
 		}
 		
 		setCode_private(code, cellNamespace);
 	}
 	
 	private void setCode_commonPreInit(Code code)
 	{
 		if( m_isSnapping || m_isFocused || m_subCellDimension != 1 )
 		{
 			this.setVisible(true); // just in case, not needed in all cases.
 		}
 		
 		this.clearStatusHtml();
 		
 		m_queuedCode = null;
 		
 		if( !m_hasSetCodeYet )
 		{
 			if( m_subCellDimension == 1 && !m_isSnapping && !m_isFocused )
 			{
 				//--- DRK > Now handled in cell1 load callback.
 //				setDefaultZIndex();
 			}
 		}
 		
 		m_hasSetCodeYet = true;
 		
 		m_codeSafetyLevel = code.getSafetyLevel();
 		
 		if( m_codeSafetyLevel == E_CodeSafetyLevel.META_IMAGE )
 		{
 			m_contentPanel.setVisible(false);
 		}
 	}
 	
 	public boolean isMetaImageProbablyInMemory()
 	{
 		return m_isMetaProbablyCachedInMemory;
 	}
 
 	private void setCode_private(Code code, String cellNamespace)
 	{
 		setCode_commonPreInit(code);
 		
 		if( m_codeSafetyLevel == E_CodeSafetyLevel.META_IMAGE )
 		{
 			m_sandboxMngr.stop(m_contentPanel.getElement());
 			
 //			Camera camera = m_cameraMngr.getCamera();
 //			double deltaZ = camera.getPosition().getZ() - camera.getPrevPosition().getZ();
 			
 			setCode_meta(code);
 		}
 		else
 		{
 			//--- DRK > Null check shouldn't be needed but who knows.
 			boolean loadProxy = !m_isFocused && m_cell1Proxy == null;
 			Element hostElement = m_contentPanel.getElement();
 			
 			if( loadProxy )
 			{
 				m_cell1Proxy = m_cell1ImageLoader.getProxy(m_bufferCell.getCoordinate().getM(), m_bufferCell.getCoordinate().getN());
 				
 				if( !m_cell1Proxy.isLoaded() )
 				{
 					hostElement = DOM.createDiv();
 					hostElement.getStyle().setWidth(100, Unit.PCT);
 					hostElement.getStyle().setHeight(100, Unit.PCT);
 					
 					m_sandboxMngr.stop(m_contentPanel.getElement());
 					m_contentPanel.getElement().appendChild(hostElement);
 				}
 				
 				m_cell1Proxy.onAttached();
 			}
 			
 			setCode_nonMeta(hostElement, code, cellNamespace);
 			
 			if( loadProxy )
 			{
 				m_cell1ImageLoader.load(m_cell1Proxy, hostElement, m_cell1LoadListener);
 			}
 		}
 		
 		if( m_eventListener != null )  m_eventListener.onCodeLoaded(this);
 	}
 	
 	private void setCode_nonMeta(Element hostElement, Code code, String cellNamespace)
 	{
 		m_contentPanel.setVisible(true);
 		
 		m_sandboxMngr.start(hostElement, code, cellNamespace, m_codeLoadListener);
 	}
 	
 	private void setCode_meta(Code code)
 	{
 		m_contentPanel.setVisible(false);
 		
 		//--- DRK > This now at most just removes/stops any other attached code.
 		//---		Before it handled actually inserting the meta image...now handled manually (below as of this writing).
 		m_sandboxMngr.start(m_contentPanel.getElement(), code, null, m_codeLoadListener);
 		
 		m_contentPanel.getElement().appendChild(m_metaImageProxy.getElement());
 		m_metaImageProxy.onAttached();
 		m_metaImageLoader.load(m_metaImageProxy, m_metaLoadListener);
 	}
 	
 	public E_ImageLoadState getImageLoadState()
 	{
 		if( m_codeSafetyLevel != E_CodeSafetyLevel.META_IMAGE )  return E_ImageLoadState.NOT_SET;
 
 		return m_metaImageProxy != null && m_metaImageProxy.isAttached() ? m_metaImageProxy.getState() : E_ImageLoadState.NOT_SET;
 	}
 	
 	public E_CodeSafetyLevel getCodeSafetyLevel()
 	{
 		return m_codeSafetyLevel;
 	}
 	
 	private void insertSafeHtml(String html)
 	{
 		m_utilCode.setRawCode(html);
 		m_utilCode.setSafetyLevel(E_CodeSafetyLevel.NO_SANDBOX_STATIC);
 		
 		this.setCode_nonMeta(m_contentPanel.getElement(), m_utilCode, "");
 	}
 	
 	UIBlocker getBlocker()
 	{
 		return m_statusPanel;
 	}
 	
 	private boolean isSpinnerBehind()
 	{
 		return m_subCellDimension == 1 && m_spinner.asWidget().getParent() != null && m_statusPanel.getZIndex() == E_ZIndex.CELL_STATUS_BEHIND.get();
 	}
 
 	@Override
 	public void showLoading()
 	{
 		if( m_spinner.asWidget().getParent() == null )
 		{
 			m_spinner.reset();
 			
 			this.m_statusPanel.setContent(m_spinner.asWidget());
 			
 			if( m_subCellDimension == 1 && !m_hasSetCodeYet )
 			{
 				m_statusPanel.setZIndex(E_ZIndex.CELL_STATUS_BEHIND.get());
 			}
 			else
 			{
 				m_statusPanel.setZIndex(E_ZIndex.CELL_STATUS.get());
 			}
 		}
 		
 //		showEmptyContent();
 	}
 
 	@Override
 	public void showEmptyContent()
 	{
 		this.insertSafeHtml("");
 	}
 	
 	private void setStatusHtml(String text, boolean forLoading)
 	{
 		m_statusPanel.setZIndex(E_ZIndex.CELL_STATUS.get());
 		m_statusPanel.setHtml(text);
 	}
 	
 	private void clearStatusHtml()
 	{
 		if( m_spinner.asWidget().getParent() == null )
 		{
 			clearStatusHtmlForSure();
 		}
 		else
 		{
 			clearLoading();
 		}
 	}
 	
 	private void clearStatusHtmlForSure()
 	{
 		this.setStatusHtml(null, false);
 	}
 
 	@Override
 	public void clearLoading()
 	{
 		if( isSpinnerBehind() )
 		{
 			m_clearLoadingTimer = ENABLE_TIMER;
 		}
 		else
 		{
 			clearLoading_private();
 		}
 	}
 	
 	private void clearLoading_private()
 	{
 		m_clearLoadingTimer = DISABLE_TIMER;
 		
 		if( m_spinner.asWidget().getParent() != null )
 		{
 			this.m_statusPanel.setContent(null);
 		}
 	}
 	
 	@Override public boolean isFullyDisplayed()
 	{
 		return m_visible && !m_fadingIn;
 	}
 
 	@Override public boolean isLoaded()
 	{
 		if( m_queuedCode != null )  return false;
 		
 		if( m_subCellDimension == 1 )
 		{
 			//--- DRK > Not sure what to do when this proxy is null so returning not loaded for now.
 //			if( m_cell1Proxy == null )
 //			{
 //				return true;
 //			}
 //			else
 			{
 				return m_cell1Proxy != null && m_cell1Proxy.isAttached() && m_cell1Proxy.isLoaded();
 			}
 		}
 		else
 		{
 			return m_metaImageProxy != null && m_metaImageProxy.isAttached() && m_metaImageProxy.isLoaded();
 		}
 	}
 	
 	@Override public void onSavedFromDeathSentence()
 	{
 		//--- DRK > Currently not fading out so don't need this.
 //		m_contentPanel.getElement().getStyle().setOpacity(1.0);
 	}
 
 	public boolean isPoppedUp()
 	{
 		return m_isPoppedUp;
 	}
 
 	@Override public void onSnappedTo()
 	{
 		m_isSnapping = true;
 	}
 }
