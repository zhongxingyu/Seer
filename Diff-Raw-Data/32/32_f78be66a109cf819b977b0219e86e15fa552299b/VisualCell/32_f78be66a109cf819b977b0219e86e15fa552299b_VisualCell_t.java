 package swarm.client.view.cell;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import swarm.client.app.AppContext;
 import swarm.client.entities.I_BufferCellListener;
 import swarm.client.view.E_ZIndex;
 import swarm.client.view.S_UI;
 import swarm.client.view.U_Css;
 import swarm.client.view.sandbox.SandboxManager;
 import swarm.client.view.tabs.code.I_CodeLoadListener;
 import swarm.client.view.widget.UIBlocker;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.utils.U_Bits;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.E_CodeSafetyLevel;
 import swarm.shared.entities.E_CodeType;
 import swarm.shared.structs.Code;
 import swarm.shared.structs.MutableCode;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 
 public class VisualCell extends AbsolutePanel implements I_BufferCellListener
 {
 	private static final Logger s_logger = Logger.getLogger(VisualCell.class.getName());
 	
 	//private static final String SPINNER_HTML = "<img src='/r.img/spinner.gif?v=1' />";
 	
 	private static int s_currentId = 0;
 	
 	private static final class CodeLoadListener implements I_CodeLoadListener
 	{
 		private final VisualCell m_this;
 		
 		CodeLoadListener(VisualCell thisArg)
 		{
 			m_this = thisArg;
 		}
 		
 		@Override
 		public void onCodeLoad()
 		{
 			m_this.setStatusHtml(null, false);
 		}
 	}
 	
 	private int m_id;
 	private final AbsolutePanel m_contentPanel = new AbsolutePanel();
 	//private final AbsolutePanel //m_backgroundPanel = new AbsolutePanel();
 	private final UIBlocker	m_statusPanel = new UIBlocker();
 	private final AbsolutePanel m_glassPanel = new AbsolutePanel();
 	//private final smCellSpinner m_spinner	= new smCellSpinner(smS_UI.SPINNER_ROTATION_RATE);
 	private final I_CellSpinner m_spinner;
 	private final MutableCode m_utilCode = new MutableCode(E_CodeType.values());
 	
 	//private final ArrayList<Image> //m_backgroundImages = new ArrayList<Image>();
 	private int m_currentImageIndex = -1;
 	
 	//private static final String CELL_PLUS_SPACING_PIXEL_COUNT = smS_App.CELL_PLUS_SPACING_PIXEL_COUNT + "px";
 	//private static final String CELL_PIXEL_COUNT = smS_App.CELL_PIXEL_COUNT + "px";
 	
 	private int m_subCellDimension = -1;
 	private int m_width = 0;
 	private int m_height = 0;
 	private int m_padding = 0;
 	
 	private boolean m_isValidated = false;
 	
 	private final CodeLoadListener m_codeLoadListener = new CodeLoadListener(this);
 	
 	private final SandboxManager m_sandboxMngr;
 	
 	private E_CodeSafetyLevel m_codeSafetyLevel;
 	
	private boolean m_isFocused = false;
	
 	public VisualCell(I_CellSpinner spinner, SandboxManager sandboxMngr)
 	{
 		m_spinner = spinner;
 		m_sandboxMngr = sandboxMngr;
 		m_id = s_currentId; s_currentId++;
 		
 		this.addStyleName("visual_cell");
 		m_glassPanel.addStyleName("sm_cell_glass");
 
 		//m_backgroundPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
 		m_statusPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
 		m_glassPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
 		
 		E_ZIndex.CELL_STATUS.assignTo(m_statusPanel);
 		E_ZIndex.CELL_GLASS.assignTo(m_glassPanel);
 		E_ZIndex.CELL_CONTENT.assignTo(m_contentPanel);
 		
 		m_statusPanel.setVisible(false);
 		
 		/*int maxImages = smS_App.MAX_CELL_IMAGES;
 		int currentBit = 1;
 		for( int i = 0; i <= 1; i++ )
 		{
 			Image image = new Image();
 			image.setUrl("/r.img/cell_size_" + currentBit + ".png");
 			image.getElement().getStyle().setDisplay(Display.NONE);
 			//m_backgroundImages.add(image);
 			//m_backgroundPanel.add(image);
 			
 			currentBit <<= 1;
 		}*/
 		
 		//smU_UI.toggleSelectability(//m_backgroundPanel.getElement(), false);
 		this.allowUserSelect(false);
 		
 		//this.add(//m_backgroundPanel);
 		this.add(m_contentPanel);
 		this.add(m_statusPanel);
 		this.add(m_glassPanel);
 	}
 	
 	int getId()
 	{
 		return m_id;
 	}
 	
 	private void allowUserSelect(boolean selectable)
 	{
 		if( selectable )
 		{
 			m_contentPanel.getElement().getStyle().setProperty("userSelect", "text");
 			m_contentPanel.getElement().getStyle().setProperty("msUserSelect", "text");
 			m_contentPanel.getElement().getStyle().setProperty("WebkitUserSelect", "text");
 			m_contentPanel.getElement().getStyle().setProperty("OUserSelect", "text");
 			m_contentPanel.getElement().getStyle().setProperty("MozUserSelect", "text");
 			
 		}
 		else
 		{
 			m_contentPanel.getElement().getStyle().setProperty("userSelect", "none");
 			m_contentPanel.getElement().getStyle().setProperty("msUserSelect", "none");
 			m_contentPanel.getElement().getStyle().setProperty("WebkitUserSelect", "none");
 			m_contentPanel.getElement().getStyle().setProperty("OUserSelect", "none");
 			m_contentPanel.getElement().getStyle().setProperty("MozUserSelect", "none");
 		}
 	}
 	
 	public void update(double timeStep)
 	{
 		if( m_spinner.asWidget().getParent() != null )
 		{
 			m_spinner.update(timeStep);
 		}
 	}
 	
 	void validate()
 	{
 		if( m_isValidated )  return;
 		
 		if( m_currentImageIndex != -1 )
 		{
 			//m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.NONE);
 		}
 		
 		if( m_subCellDimension == 1 )
 		{
 			this.setSize(m_width+m_padding + "px", m_height+m_padding + "px");
 			//m_backgroundPanel.setSize(m_width+m_padding + "px", m_height+m_padding + "px");
 			
 			m_currentImageIndex = 0;
 			//m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.BLOCK);
 			
 			//--- DRK > Rare case of jumping from beyond max imaged zoom to all the way to cell size 1,
 			//---		but could technically happen with bad frame rate or something, so clearing this here just in case.
 			//m_backgroundPanel.getElement().getStyle().clearBackgroundColor();
 			
 			m_contentPanel.addStyleName("visual_cell_content");
 		}
 		else if( m_subCellDimension > 1 )
 		{
 			this.setStatusHtml(null, false); // shouldn't have to be done, but what the hell.
 			
 			this.setSize(m_width+"px", m_height+"px");
 			//m_backgroundPanel.setSize(m_width+"px", m_height+"px");
 			
 			if( m_subCellDimension > S_CommonApp.MAX_IMAGED_CELL_SIZE )
 			{				
 				//m_backgroundPanel.getElement().getStyle().setBackgroundColor("white");
 			}
 			else
 			{
 				//m_backgroundPanel.getElement().getStyle().clearBackgroundColor();
 				
 				m_currentImageIndex = U_Bits.calcBitPosition(m_subCellDimension);
 				//m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.BLOCK);
 			}
 			
 			m_contentPanel.removeStyleName("visual_cell_content");
 		}
 		
 		m_isValidated = true;
 	}
 	
 	public void onCreate(int width, int height, int padding, int subCellDimension)
 	{
 		 //--- DRK > NOTE: for some reason this gets reset somehow...at least in hosted mode, so can't put it in constructor.
 		this.getElement().getStyle().setPosition(Position.ABSOLUTE);
 		
		m_isFocused = false;
 		m_isValidated = false;
 		m_subCellDimension = subCellDimension;
 		m_width = width;
 		m_height = height;
 		m_padding = padding;
 		m_currentImageIndex = -1;
 		
 		m_contentPanel.setSize(m_width+"px", m_height+"px");
 		m_statusPanel.setSize(m_width+"px", m_height+"px");
 	}
 	
 	public void onDestroy()
 	{
		m_isFocused = false;
 		m_subCellDimension = -1;
 		
 		if( m_currentImageIndex != -1 )
 		{
 			//m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.NONE);
 		}
 
 		if( !E_CodeSafetyLevel.isStatic(m_codeSafetyLevel) )
 		{
 			this.insertSafeHtml("");
 		}
 		
 		m_codeSafetyLevel = null;
 		
 		this.pushDown();
 	}
 	
 	@Override
 	public void onFocusGained()
 	{
		m_isFocused = true;
		
 		E_ZIndex.CELL_FOCUSED.assignTo(this);
 		
 		this.m_glassPanel.setVisible(false);
 		this.addStyleName("visual_cell_focused");
 
 		this.allowUserSelect(true);
 		
 		m_sandboxMngr.allowScrolling(m_contentPanel.getElement(), true);
 	}
 	
 	@Override
 	public void onFocusLost()
 	{
		m_isFocused = false;
		
 		this.m_glassPanel.setVisible(true);
 		
 		this.removeStyleName("visual_cell_focused");
 		
 		this.allowUserSelect(false);
 		
 		/*if( m_sandboxMngr.isRunning() )
 		{
 			m_sandboxMngr.stop(m_contentPanel.getElement());
 		}*/
 		
 		m_sandboxMngr.allowScrolling(m_contentPanel.getElement(), false);
 	}
 	
 	public void popUp()
 	{
		//--- DRK > Added this conditional because for fringe case of instant snap,
		//--- 		onFocusGained can be called before popUp. I thought this case wasn't
		//---		a problem before, but may not have tested it, or something might have changed.
		//---		It does make sense that it's needed though, because onFocusGained call originates
		//---		in state machine, and popUp is invoked from a UI handler of the state event, so comes later.
		if( !m_isFocused )
		{
			E_ZIndex.CELL_POPPED.assignTo(this);
		}
 	}
 	
 	public void pushDown()
 	{
 		this.getElement().getStyle().clearZIndex();
 	}
 
 	@Override
 	public void onCellRecycled(int cellSize)
 	{
		m_isFocused = false;
 		if( cellSize != m_subCellDimension )
 		{
 			m_isValidated = false;
 		}
 
 		m_subCellDimension = cellSize;
 
 		this.insertSafeHtml("");
 		
 		this.pushDown();
 	}
 	
 	@Override
 	public void onError(E_CodeType eType)
 	{
 		//TODO: These are placeholder error messages...should have some prettier error text, or maybe nothing at all?
 		
 		switch( eType )
 		{
 			case SPLASH:
 			{
 				this.setStatusHtml(null, false);
 				this.showEmptyContent();
 				
 				break;
 			}
 			
 			case COMPILED:
 			{
 				this.setStatusHtml("Problem contacting server.", false);
 				
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void setCode(Code code, String cellNamespace)
 	{
 		this.setStatusHtml(null, false);
 		
 		/*if( m_sandboxMngr.isRunning() )
 		{
 			m_sandboxMngr.stop(m_contentPanel.getElement());
 		}*/
 		
 		m_codeSafetyLevel = code.getSafetyLevel();
 		
 		m_sandboxMngr.start(m_contentPanel.getElement(), code, cellNamespace, m_codeLoadListener);
 	}
 	
 	public E_CodeSafetyLevel getCodeSafetyLevel()
 	{
 		return m_codeSafetyLevel;
 	}
 	
 	private void insertSafeHtml(String html)
 	{
 		m_utilCode.setRawCode(html);
 		m_utilCode.setSafetyLevel(E_CodeSafetyLevel.NO_SANDBOX_STATIC);
 		
 		this.setCode(m_utilCode, "");
 	}
 	
 	UIBlocker getBlocker()
 	{
 		return m_statusPanel;
 	}
 
 	@Override
 	public void showLoading()
 	{
 		if( m_spinner.asWidget().getParent() == null )
 		{
 			m_spinner.reset();
 			
 			this.m_statusPanel.setContent(m_spinner.asWidget());
 		}
 	}
 
 	@Override
 	public void showEmptyContent()
 	{
 		this.insertSafeHtml("");
 	}
 	
 	private void setStatusHtml(String text, boolean forLoading)
 	{
 		m_statusPanel.setHtml(text);
 	}
 
 	@Override
 	public void clearLoading()
 	{
 		if( m_spinner.asWidget().getParent() != null )
 		{
 			this.m_statusPanel.setContent(null);
 		}
 	}
 }
