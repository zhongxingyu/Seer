 package vibe;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSplitPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * The main application frame.
  * Contains all other components.
  * @author Ryan Norris
  *
  */
 public class MainFrame extends JFrame {
 
 	/**
 	 * Container for the whole UI.
 	 */
 	private Container mContainer;
 	
 	/**
 	 * The outside JSplitPane for the code edit boxes.
 	 * Contains the HighLevelTextPane and the
 	 * other SplitPane.
 	 */
 	private JSplitPane mEditPane;
 	
 	/**
 	 * The inner JSplitPane for the code edit boxes.
 	 * Contains the EAR and EF TextPanes.
 	 */
 	private JSplitPane mEARAndEFPane;
 	
 	/**
 	 * Text box for entering high level code.
 	 */
 	private CodePane mHighLevelTextPane;
 	
 	/**
 	 * Text box for entering EAR code.
 	 */
 	private CodePane mEARTextPane;
 	
 	/**
 	 * Text box for entering EF code.
 	 */
 	private CodePane mEFTextPane;
 	
 	/**
 	 * The menu bar at the top of the application. (File etc...)
 	 */
 	private JMenuBar mMenuBar;
 	
 	/**
 	 * Toolbar running along the top of the application just below the menu bar. <br/>
 	 * Contains useful buttons (compile/play/pause/stop/step) and tempo slider.
 	 */
 	private JPanel mToolBar;
 	
 	/**
 	 * Slider to control the tempo of the code playback.
 	 */
 	private JSlider mTempoSlider;
 	
 	/**
 	 * Minimum selectable tempo for slider.
 	 */
 	private final static int MIN_TEMPO = 30;
 	
 	/**
 	 * Initial value for tempo slider.
 	 */
 	private final static int INITIAL_TEMPO = 130;
 	
 	/**
 	 * Maximum selectable tempo for slider.
 	 */
 	private final static int MAX_TEMPO = 1000;
 	
 	/**
 	 * Visualiser for Parser memory.
 	 */
 	private MemoryVisualiser mMemoryVisualiser;
 	
 	/**
 	 * The input/output console
 	 */
 	private Console mConsole;
 	
 	/**
 	 * Controller to handle all events on this window.
 	 */
 	private VibeController mController;
 	
 	public MainFrame() {
 		initComponents();
 		this.setTitle("VIBE Earfuck IDE");
 	}
 	
 	/**
 	 * UI setup to be done AFTER made visible.
 	 */
 	private void setupUI() {
 		mController.setMode(VibeController.VibeMode.EAR);
 		mEditPane.setDividerLocation(0.333);
 		mEARAndEFPane.setDividerLocation(0.5);
 		
 		Dimension size = mMemoryVisualiser.getSize();
 		//size.width = this.getWidth() - mConsole.getWidth();
 		//mMemoryVisualiser.setSize(size);
 		mMemoryVisualiser.setHorizontalAlignment(SwingConstants.CENTER);
 	}
 	
 	/**
 	 * Should be called when the user wants the window to appear.
 	 */
 	public void create() {
 		setVisible(true);
 		setupUI();
 	}
 	
 	/**
 	 * Initialises all components. </br>
 	 * Called from constructor, so should not contain any initialisation
 	 * that requires the components to be visible.
 	 */
 	private void initComponents() {
 		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 		
 		mController = new VibeController(this);
 		
 		mContainer = getContentPane();
 		
 		//Create text panes
 		JPanel noWrapPanel;
 		JScrollPane scrollPane1, scrollPane2, scrollPane3;
 		mHighLevelTextPane = new CodePane();
 		mHighLevelTextPane.setDivider('\n');
 		noWrapPanel = new JPanel(new BorderLayout());
 		noWrapPanel.add(mHighLevelTextPane);
 		scrollPane1 = new JScrollPane(noWrapPanel);
 		
 		mEARTextPane = new CodePane();
 		mEARTextPane.setDivider('\n');
 		noWrapPanel = new JPanel(new BorderLayout());
 		noWrapPanel.add(mEARTextPane);
 		scrollPane2 = new JScrollPane(noWrapPanel);
 		
 		mEFTextPane = new CodePane();
 		scrollPane3 = new JScrollPane(mEFTextPane);
 		scrollPane3.setHorizontalScrollBarPolicy(
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		
 		//Create split panes
 		mEARAndEFPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 					scrollPane2,scrollPane3);
 		mEditPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 					scrollPane1,mEARAndEFPane);
 		mEARAndEFPane.setContinuousLayout(true);
 		mEditPane.setContinuousLayout(true);
 		
 		//Add to container
 		mContainer.add(mEditPane,BorderLayout.CENTER);
 		
 		//Set window size
 		Rectangle rect = this.getBounds();
 		rect.height = 480;
 		rect.width = 640;
 		this.setBounds(rect);
 		
 		//Create menu bar
 		mMenuBar = new JMenuBar();
 		JMenu menu = new JMenu("File");
 		
 		JMenuItem menuItem;
 		
 		menuItem = new JMenuItem("New");
 		menuItem.addActionListener(mController);
 		menuItem.setActionCommand("new");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem("Open");
 		menuItem.addActionListener(mController);
 		menuItem.setActionCommand("open");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem("Save");
 		menuItem.addActionListener(mController);
 		menuItem.setActionCommand("save");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem("Save as");
 		menuItem.addActionListener(mController);
 		menuItem.setActionCommand("saveas");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem("Exit");
 		menuItem.addActionListener(mController);
 		menuItem.setActionCommand("exit");
 		menu.add(menuItem);
 		
 		mMenuBar.add(menu);
 		
 		setJMenuBar(mMenuBar);
 		
 		//Create tool bar
 		mToolBar = new JPanel();
 		mContainer.add(mToolBar,BorderLayout.NORTH);
 		JButton button = new JButton();
 		button.setText("Compile");
 		button.setActionCommand("compile");
 		button.addActionListener(mController);
 		mToolBar.add(button);
 		
 		button = new JButton();
 		button.setText("|>");
 		button.setActionCommand("play");
 		button.addActionListener(mController);
 		mToolBar.add(button);
 		
 		button = new JButton();
 		button.setText("||");
 		button.setActionCommand("pause");
 		button.addActionListener(mController);
 		button.setEnabled(false);
 		mToolBar.add(button);
 		
 		button = new JButton();
 		button.setText("O");
 		button.setActionCommand("stop");
 		button.addActionListener(mController);
 		button.setEnabled(false);
 		mToolBar.add(button);
 		
 		button = new JButton();
 		button.setText("->");
 		button.setActionCommand("step");
 		button.addActionListener(mController);
 		mToolBar.add(button);
 		
 		//Tempo slider frame
 		JPanel frame = new JPanel();
 		frame.setLayout(new BoxLayout(frame,BoxLayout.PAGE_AXIS));
 		
 		mTempoSlider = new JSlider(MIN_TEMPO,MAX_TEMPO,INITIAL_TEMPO);
 		mTempoSlider.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				//Set the label text
 				//All in one line, because I'm awesome and love unreadable code <_<
 				((JLabel) ((JPanel) mToolBar.getComponent(5)).getComponent(0)).setText(
 						"Tempo: "+String.valueOf(mTempoSlider.getValue()));
 			}
 		});
 		JLabel label = new JLabel();
 		label.setAlignmentY(CENTER_ALIGNMENT);
 		label.setText("Tempo: "+String.valueOf(mTempoSlider.getValue()));
 		frame.add(label);
 		frame.add(mTempoSlider);
 		mToolBar.add(frame);
 		
 		//SOUTH SECTION
 		frame = new JPanel();
 		frame.setLayout(new BoxLayout(frame,BoxLayout.LINE_AXIS));
 		
 		//Create the Memory Visualiser
 		mMemoryVisualiser = new MemoryVisualiser(mController.getParser());
 		frame.add(mMemoryVisualiser);
 		
 		//Create the console
 		mConsole = new Console();
 		mController.getParser().setIoManager(mConsole);
 		noWrapPanel = new JPanel(new BorderLayout());
 		noWrapPanel.add(mConsole);
 		scrollPane1 = new JScrollPane(noWrapPanel);
 		Dimension size = new Dimension(150,100);
 		scrollPane1.setPreferredSize(size);
 		
 		frame.add(scrollPane1);
 		
 		mContainer.add(frame,BorderLayout.SOUTH);
 	}
 	
 	/**
 	 * Returns the contents of the EAR code pane.
 	 * @return
 	 */
 	public String getEARCode() {
 		return mEARTextPane.getText();
 	}
 	
 	/**
 	 * Sets the contents of the EAR code pane.
 	 * @param text
 	 */
 	public void setEARCode(String text) {
 		mEARTextPane.setText(text);
 	}
 	
 	/**
 	 * Returns the contents of the high level code pane.
 	 * @return
 	 */
 	public String getHighLevelCode() {
 		return mHighLevelTextPane.getText();
 	}
 	
 	/**
 	 * Sets the contents of the high level code pane.
 	 * @param text
 	 */
 	public void setHighLevelCode(String text) {
 		mHighLevelTextPane.setText(text);
 	}
 	
 	/**
 	 * Returns the contents of the EF code pane.
 	 * @return
 	 */
 	public String getEFCode() {
 		return mEFTextPane.getText();
 	}
 	
 	/**
 	 * Sets the contents of the EF code pane.
 	 * @param text
 	 */
 	public void setEFCode(String text) {
 		mEFTextPane.setText(text);
 	}
 	
 	/**
 	 * Returns the EF text pane itself. <br/>
 	 * Used by the ParserWorker & StepForwardWorker for code highlighting.
 	 * @return
 	 */
 	public CodePane getEFTextPane() {
 		return mEFTextPane;
 	}
 	
 	/**
 	 * Returns the EAR text pane itself. <br/>
 	 * Used by the ParserWorker & StepForwardWorker for code highlighting.
 	 * @return
 	 */
 	public CodePane getEARTextPane() {
 		return mEARTextPane;
 	}
 	
 	/**
 	 * Returns the High Level text pane itself. <br/>
 	 * Used by the ParserWorker & StepForwardWorker for code highlighting.
 	 * @return
 	 */
 	public CodePane getHighLevelTextPane() {
 		return mHighLevelTextPane;
 	}
 	
 	/**
 	 * Sets whether the code is editable. (Only the code of the current mode
 	 * is ever editable)
 	 * @param editable
 	 */
 	public void setCodeEditable(boolean editable) {
 		VibeController.VibeMode mode = mController.getMode();
 		if (mode==VibeController.VibeMode.EF) {
 			mEFTextPane.setEditable(editable);
 		}
 		else if (mode==VibeController.VibeMode.EAR) {
 			mEARTextPane.setEditable(editable);
 		}
 		else if (mode==VibeController.VibeMode.HIGHLEVEL) {
 			mHighLevelTextPane.setEditable(editable);
 		}
 	}
 	
 	/**
 	 * Gets the user selected tempo from the tempo slider.
 	 * @return
 	 */
 	public int getTempo() {
 		return mTempoSlider.getValue();
 	}
 	
 	/**
 	 * Sets buttons enabled or disabled. <br/>
 	 * @param index 0 = compile, 1 = play, 2 = pause, 3 = stop, 4 = step
 	 * @param enabled true/false
 	 */
 	public void setButtonEnabled(int index, boolean enabled) {
 		JButton button = (JButton) mToolBar.getComponent(index);
 		button.setEnabled(enabled);
 	}
 	
 	/**
 	 * Sets up the edit pane for the given mode. <br/>
 	 * Sets the correct text pane to editable and hides irrelevant panes.
 	 * @param mode the VibeMode we're setting up for
 	 */
 	public void setupEditPane(VibeController.VibeMode mode) {
 		JScrollPane leftPane = (JScrollPane) mEditPane.getComponent(0);
 		JScrollPane midPane = (JScrollPane) mEARAndEFPane.getComponent(0);
 		
 		mEFTextPane.setEditable(false);
 		mEARTextPane.setEditable(false);
 		mHighLevelTextPane.setEditable(false);
 		
		
 		if (mode==VibeController.VibeMode.EAR) {
 			leftPane.setVisible(false);
 			midPane.setVisible(true);
 			mEARAndEFPane.setDividerSize(5);
 			mEditPane.setDividerSize(0);
 			mEARTextPane.setEditable(true);
 		}
 		if (mode==VibeController.VibeMode.EF) {
 			leftPane.setVisible(false);
 			midPane.setVisible(false);
 			mEARAndEFPane.setDividerSize(0);
 			mEditPane.setDividerSize(0);
 			mEFTextPane.setEditable(true);
 		}
 		if (mode==VibeController.VibeMode.HIGHLEVEL) {
 			leftPane.setVisible(true);
 			midPane.setVisible(true);
 			mEARAndEFPane.setDividerSize(5);
 			mEditPane.setDividerSize(5);
 			mHighLevelTextPane.setEditable(true);
 		}
		
		int totalWidth = mEditPane.getWidth();
		mEARAndEFPane.setDividerLocation(totalWidth/3);
		mEditPane.setDividerLocation(totalWidth/3);
		
 	}
 	
 	public void updateMemoryVisualiser() {
 		mMemoryVisualiser.update();
 	}
 }
