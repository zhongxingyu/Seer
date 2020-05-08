 package munk.graph.gui;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.RenderedImage;
 import java.io.*;
 import java.util.*;
 import java.util.List;
 import java.util.concurrent.*;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.vecmath.Color3f;
 
 import munk.graph.IO.*;
 import munk.graph.function.*;
 import munk.graph.gui.listener.*;
 
 import com.graphbuilder.math.*;
 
 
 /**
  * A simple GUI for the 3DPlotter application.
  * 
  * @author xXx
  *
  */
 public class V2GUI {
 
 	static final Color	NORMAL_COLOR	= Color.WHITE;
 	static final Color SELECTED_COLOR = new Color(189, 214, 224);
 	
 	private static enum TYPE{PARAM, STD};
 	
 	private static final int CANVAS_INITIAL_WIDTH = 600;
 	private static final int CANVAS_INITIAL_HEIGTH = 600;
 	private static final String[] DEFAULT_BOUNDS = {"-1","1","-1","1","-1","1"};
 	
 	// GUI Variables.
 	private static V2GUI window;
 	private JFrame frame;
 	private JPanel stdFuncTab, stdFuncOuterPanel, stdFuncInnerPanel;
 	private JScrollPane stdFuncPanelWrapper;
 	private JPanel paramFuncTab, paramFuncOuterPanel, paramFuncInnerPanel;
 	private JScrollPane paramFuncPanelWrapper;
 	private JPanel canvasPanel;
 	private JTabbedPane tabbedPane;
 	private JDialog colorDialog;
 	private AppearanceOptionPanel stdEditOptionPanel;
 	private AppearanceOptionPanel paramEditOptionPanel;
 
 	// Non-GUI variables.
 	private Plotter3D plotter;
 	private int controlsWidth;
 	private int controlsHeight;
 	private List<Function> stdFuncList = new ArrayList<Function>();
 	private List<Function> paramFuncList = new ArrayList<Function>();
 	private HashMap<Function, FunctionLabel> map = new HashMap<Function, FunctionLabel>();
 	private javax.swing.Timer resizeTimer;
 	private ColorList colorList;
 	private String filePath;
 	
 	// Option variables
 	private JTextField stdFuncInput;
 	private JMenuBar menuBar;
 	private JMenu mnFile;
 	private JMenuItem mntmSaveProject, mntmLoadProject, mntmExit, mntmPrintCanvas;
 	private JMenu mnColorOptions;
 	private JMenu mnHelp;
 	private JMenuItem mntmDocumentation, mntmAbout;
 	private JMenuItem mntmImportColors, mntmExportColors, mntmAddCustomColor;
 	private JTextField inputX, inputY, inputZ;
 	private JLabel lblX, lblY, lblZ;
 	private Function selectedFunction;
 	
 	// Plotter renders
 	private ExecutorService plottingQueue = Executors.newSingleThreadExecutor(); // Only plot one at a time
 	private String	defaultImageExtension = "png";
 	
 	// Option panels (new)
  	private StdGridOptionPanel stdGridOptionPanel;
  	private ParamGridOptionPanel paramGridOptionPanel;
  	private AppearanceOptionPanel stdAppearancePanel;
  	private AppearanceOptionPanel paramAppearancePanel;
  	private JPanel paramOptionPanel;
  	private JPanel stdOptionPanel;
  	
  	private Function stdTemplateFunc;
  	private Function paramTemplateFunc;
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				// Set look-and-feel to OS default.
 				try {
 					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 				} 
 				catch (Exception e) {
 					System.out.println("Unable to load native look and feel: " + e.toString());
 				}
 				window = new V2GUI();
 				window.frame.setVisible(true);
 			}
 		});
 	}
 	
 	/**
 	 * Create the application.
 	 */
 	public V2GUI() {
 		// Initialize variables.
 		colorList = new ColorList(stdFuncList);
 		filePath = File.separator+"tmp";
 		
 		initialize();
 	}
 
 	/**
 	 * Initialize;
 	 */
 	private void initialize(){
 		// Initialize GUI components.
 		initFrame();
 		initMenuBar();
 		initTabbedPane();
 		init3Dplotter();
      	initStdFunctionTab();
      	initParamFunctionTab();
      	
      	addPlot(new String[]{"y = sin(x*5)*cos(z*5)"}, colorList.getNextAvailableColor(), DEFAULT_BOUNDS, new float[]{(float) 0.1,(float) 0.1,(float) 0.1});
 		
      	// Finish up.
      	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      	frame.setVisible(true);
      	frame.pack();
      	
      	// Set up tempate functions.
      	try {
 			paramTemplateFunc = new TemplateFunction(DEFAULT_BOUNDS, stdGridOptionPanel.getGridStepSize());
 	     	stdTemplateFunc = new TemplateFunction(DEFAULT_BOUNDS, stdGridOptionPanel.getGridStepSize());
 	     	stdGridOptionPanel.updateFuncReference(stdTemplateFunc);
 	     	paramGridOptionPanel.updateFuncReference(paramTemplateFunc);
      	} catch (ExpressionParseException e) {
 			e.printStackTrace();
 		}
      	
      	autoResize();
 	}
 	
 	private void initFrame(){
 		frame = new JFrame("Ultra Mega Epic Xtreme Plotter 3D");
 		frame.setBounds(100, 100, 1000, 1000);
      	GridBagLayout gbl = new GridBagLayout();
      	gbl.columnWidths = new int[]{10, 300, 0, 0, 0};
      	gbl.rowHeights = new int[]{2, 0, 5, 0};
      	gbl.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
      	gbl.rowWeights = new double[]{0.0, 2.0, 0.0, Double.MIN_VALUE};
      	frame.getContentPane().setLayout(gbl);
 	}
 	
 	private void initMenuBar(){
 		menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 		
 		mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 
 		mntmSaveProject = new JMenuItem("Export workspace", new ImageIcon("Icons/save.png"));
 		mntmSaveProject.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				File outputFile = GuiUtil.spawnExportDialog(filePath, frame);
 				if(outputFile != null){
 				filePath=outputFile.getPath().replace(outputFile.getName(), "");
 				try {
 					ObjectWriter.ObjectToFile(outputFile, new ZippedFunction[][]{FunctionUtil.zipFunctionList(stdFuncList),FunctionUtil.zipFunctionList(paramFuncList)});
 				} catch (IOException e) {
 					JOptionPane.showMessageDialog(frame,new JLabel("Unable to write file.",JLabel.CENTER));
 				}
 				}
 			}
 		});
 		mnFile.add(mntmSaveProject);
 
 		mntmLoadProject = new JMenuItem("Import workspace", new ImageIcon("Icons/file.png"));
 		mntmLoadProject.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				importFunctions();
 			}
 		});
 		mnFile.add(mntmLoadProject);
 		
 		mntmPrintCanvas = new JMenuItem("Save as image", new ImageIcon("Icons/png.png"));
 		mntmPrintCanvas.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				saveAsImage();
 			}
 
 			private void saveAsImage() {
 				String[][] fileEndings = {{"png"}, {"jpg", "jpeg"}, {"gif"}, {"bmp"}};
 				String[] description = {"PNG image", "JPEG image", "GIF image", "Bitmap graphic"};
 				
 				File outputFile = GuiUtil.spawnExportDialog(filePath, fileEndings, description, frame);
 				if(outputFile != null){
 					filePath=outputFile.getPath().replace(outputFile.getName(), "");
 
 					savePlotToDisk(outputFile);
 					
 				}
 			}
 		});
 		mnFile.add(mntmPrintCanvas);
 		
 		// Close application on click.
 		mntmExit = new JMenuItem("Exit", new ImageIcon("Icons/exit.png"));
 		mntmExit.addActionListener(new ActionListener(
 				) {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 		mnFile.add(mntmExit);
 		
 		mnColorOptions = new JMenu("Color Options");
 		menuBar.add(mnColorOptions);
 		
 		mntmExportColors = new JMenuItem("Export colors", new ImageIcon("Icons/save.png"));
 		mntmExportColors.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				File outputFile = GuiUtil.spawnExportDialog(filePath, frame);
 				if(outputFile != null){
 				filePath=outputFile.getPath().replace(outputFile.getName(), "");
 				try {
 					ObjectWriter.ObjectToFile(outputFile,colorList);
 				} catch (IOException e) {
 					JOptionPane.showMessageDialog(frame,new JLabel("Unable to write file.",JLabel.CENTER));
 				}
 				}
 			}
 		});
 		mnColorOptions.add(mntmExportColors);
 		
 		mntmImportColors = new JMenuItem("Import colors", new ImageIcon("Icons/file.png"));
 		mntmImportColors.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				File inputFile = GuiUtil.spawnImportDialog(filePath, frame);
 				if(inputFile != null){
 					filePath=inputFile.getPath().replace(inputFile.getName(), "");
 					try{
 						colorList = (ColorList) ObjectReader.ObjectFromFile(inputFile);
 					}
 					catch(IOException | ClassCastException | ClassNotFoundException e){
 						JOptionPane.showMessageDialog(frame,new JLabel("Unable to read color list from file.",JLabel.CENTER));
 					}
 				}
 			}
 		});
 		mnColorOptions.add(mntmImportColors);
 		
 		mntmAddCustomColor = new JMenuItem("Add custom color", new ImageIcon("Icons/settings.png"));
 		mntmAddCustomColor.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				spawnColorChooser();
 			}
 		});
 		mnColorOptions.add(mntmAddCustomColor);
 		
 		mnHelp = new JMenu("Help");
 		menuBar.add(mnHelp);
 		
 		// Open the documentation PDF.
 		mntmDocumentation = new JMenuItem("Documentation", new ImageIcon("Icons/pdf.png"));
 		mntmDocumentation.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					Desktop.getDesktop().open(new File("Files/3DPlotter.pdf"));
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 		mnHelp.add(mntmDocumentation);
 
 		// Open about pop up on click.
 		mntmAbout = new JMenuItem("About", new ImageIcon("Icons/info.png"));
 		mntmAbout.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String message = "<html> 3DPlotter is a free simple 3D graphing tool. It is currently being developed as a spare time <br> project by Michael Munch, Emil Haldrup Eriksen and Kristoffer Theis Skalmstang. Please email <br> bugs, suggestions and generel feedback to <br> <br> <center> emil.h.eriksen@gmail.com </center> </html>";
 				JLabel label = new JLabel(message);
 				JOptionPane.showMessageDialog(frame,label,"About",JOptionPane.PLAIN_MESSAGE,null);
 			}
 		});
 		mnHelp.add(mntmAbout);
 	}
 	
 	private void initTabbedPane(){
      	tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
      	GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
      	gbc_tabbedPane.insets = new Insets(0, 0, 5, 5);
      	gbc_tabbedPane.fill = GridBagConstraints.BOTH;
      	gbc_tabbedPane.gridx = 1;
      	gbc_tabbedPane.gridy = 1;
      	frame.getContentPane().add(tabbedPane, gbc_tabbedPane);
 	}
 	
 	private void init3Dplotter(){
 		canvasPanel = new JPanel();
      	plotter = new Plotter3D();
     	GridBagConstraints gbc_plotter = new GridBagConstraints();
     	gbc_plotter.gridheight = 3;
      	gbc_plotter.insets = new Insets(0, 0, 5, 5);
      	gbc_plotter.gridx = 4;
      	gbc_plotter.gridy = 1;
      	canvasPanel.add(plotter, gbc_plotter);
      	GridBagConstraints gbc_list = new GridBagConstraints();
      	gbc_list.anchor = GridBagConstraints.NORTH;
      	gbc_list.insets = new Insets(0, 0, 5, 5);
      	gbc_list.fill = GridBagConstraints.HORIZONTAL;
      	gbc_list.gridx = 1;
      	gbc_list.gridy = 2;
      	GridBagConstraints gbc_canvasPanel = new GridBagConstraints();
      	gbc_canvasPanel.fill = GridBagConstraints.BOTH;
      	gbc_canvasPanel.gridheight = 2;
      	gbc_canvasPanel.gridy = 1;
      	gbc_canvasPanel.gridx = 3;
      	frame.getContentPane().add(canvasPanel, gbc_canvasPanel);
 	}
 	
 	private void initStdFunctionTab(){
 		// The standard function tab.
      	stdFuncTab = new JPanel();
      	tabbedPane.addTab("Standard equations", stdFuncTab);
      	GridBagLayout gbl_functionPanel = new GridBagLayout();
      	gbl_functionPanel.columnWidths = new int[]{5, 25, 50, 50, 30, 25, 5, 0};
      	gbl_functionPanel.rowHeights = new int[]{10, 0, 0, 145, 0, 10, 5, 5, 0, 0, 0};
      	gbl_functionPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
      	gbl_functionPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
      	stdFuncTab.setLayout(gbl_functionPanel);
      	
      	// Function input field.
      	stdFuncInput = new JTextField();
      	GridBagConstraints gbc_stdFuncInput = new GridBagConstraints();
      	gbc_stdFuncInput.gridwidth = 5;
      	gbc_stdFuncInput.insets = new Insets(0, 0, 5, 5);
      	gbc_stdFuncInput.fill = GridBagConstraints.HORIZONTAL;
      	gbc_stdFuncInput.anchor = GridBagConstraints.NORTH;
      	gbc_stdFuncInput.gridx = 1;
      	gbc_stdFuncInput.gridy = 1;
      	stdFuncTab.add(stdFuncInput, gbc_stdFuncInput);
      	stdFuncInput.setColumns(10);
      	GuiUtil.setupUndoListener(stdFuncInput);
      	stdFuncInput.addKeyListener(new KeyAdapter() {
      		// Plot the graph.
      		
      		@Override
      		public void keyPressed(KeyEvent e) {
      			if (e.getKeyCode() == KeyEvent.VK_ENTER && stdFuncInput.isFocusOwner()) {
      				try {
 						addPlot(new String[]{stdFuncInput.getText()},stdTemplateFunc.getColor(), stdGridOptionPanel.getGridBounds(), stdGridOptionPanel.getGridStepSize());
 					} catch (ExpressionParseException e1) {
 						JOptionPane.showMessageDialog(frame,new JLabel(e1.getMessage(),JLabel.CENTER));
 					}
      			}
      			
      		}
      	});
      	stdFuncInput.addFocusListener(new FocusAdapter() {
 			
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				try {
 					setSelected(stdTemplateFunc);
 				} catch (ExpressionParseException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
      	// OptionPanel
      	stdOptionPanel = new JPanel();
      	stdOptionPanel.setBorder(BorderFactory.createEtchedBorder());
      	GridBagConstraints gbc_optionPanel = new GridBagConstraints();
      	gbc_optionPanel.fill = GridBagConstraints.HORIZONTAL;
      	gbc_optionPanel.gridwidth = 5;
      	gbc_optionPanel.insets = new Insets(0, 0, 5, 5);
      	gbc_optionPanel.gridx = 1;
      	gbc_optionPanel.gridy = 3;
      	stdFuncTab.add(stdOptionPanel, gbc_optionPanel);
      	
      	stdGridOptionPanel = new StdGridOptionPanel(DEFAULT_BOUNDS);
      	stdAppearancePanel = new AppearanceOptionPanel(colorList, map);
      	stdOptionPanel.setLayout(new BoxLayout(stdOptionPanel, BoxLayout.Y_AXIS));
      	stdOptionPanel.add(stdGridOptionPanel);
      	stdOptionPanel.add(stdAppearancePanel);
      	
      	stdGridOptionPanel.addFunctionListener(createGridOptionPanelListener());
      	
      	// The standard function list
      	stdFuncOuterPanel = new JPanel();
      	stdFuncPanelWrapper = new JScrollPane(stdFuncOuterPanel);
      	stdFuncPanelWrapper.setBorder(BorderFactory.createEtchedBorder());
      	stdFuncPanelWrapper.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      	GridBagConstraints gbc_stdFuncPanel = new GridBagConstraints();
      	gbc_stdFuncPanel.fill = GridBagConstraints.BOTH;
      	gbc_stdFuncPanel.gridwidth = 5;
      	gbc_stdFuncPanel.insets = new Insets(0, 0, 5, 5);
      	gbc_stdFuncPanel.gridx = 1;
      	gbc_stdFuncPanel.gridy = 6;
      	stdFuncTab.add(stdFuncPanelWrapper, gbc_stdFuncPanel);
      	GridBagLayout gbl_stdFuncPanel = new GridBagLayout();
      	gbl_stdFuncPanel.columnWidths = new int[]{0, 0};
      	gbl_stdFuncPanel.rowHeights = new int[]{0, 0};
      	gbl_stdFuncPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
      	gbl_stdFuncPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
      	stdFuncOuterPanel.setLayout(gbl_stdFuncPanel);
      	
      	stdFuncInnerPanel = new JPanel();
      	GridBagConstraints gbc_panel = new GridBagConstraints();
      	gbc_panel.anchor = GridBagConstraints.NORTH;
      	gbc_panel.fill = GridBagConstraints.HORIZONTAL;
      	gbc_panel.gridx = 0;
      	gbc_panel.gridy = 0;
      	stdFuncOuterPanel.add(stdFuncInnerPanel, gbc_panel);
      	stdFuncInnerPanel.setLayout(new BoxLayout(stdFuncInnerPanel, BoxLayout.Y_AXIS));
      
 	}
 
 	private FunctionListener createGridOptionPanelListener() {
 		return new FunctionListener() {
 
 			@Override
 			public void functionChanged(FunctionEvent e) {
 				Function func = e.getOldFunction();
 				if(func.getClass() != TemplateFunction.class){
 					updatePlot(func, func.getExpression(), func.getColor(), e.getStringBounds(), e.getStepsize());
 				}
 				else{
 					System.out.println("hest");
 					func.setBoundsString(e.getStringBounds());
 					func.setStepsize(e.getStepsize());
 				}
 			}
 		};
 	}
 
 	private void initParamFunctionTab(){
 		// The parametric function tab.
      	paramFuncTab = new JPanel();
      	tabbedPane.addTab("Parametric equations", paramFuncTab);
      	GridBagLayout gbl_paramFunctionPanel = new GridBagLayout();
      	gbl_paramFunctionPanel.columnWidths = new int[]{5, 25, 50, 50, 50, 25, 5, 0};
      	gbl_paramFunctionPanel.rowHeights = new int[]{5, 0, 0, 0, 0, 0, 5, 5, 0, 0, 0};
      	gbl_paramFunctionPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
      	gbl_paramFunctionPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
      	paramFuncTab.setLayout(gbl_paramFunctionPanel);
      	
      	lblX = new JLabel("x =");
      	GridBagConstraints gbc_lblX = new GridBagConstraints();
      	gbc_lblX.insets = new Insets(0, 0, 5, 5);
      	gbc_lblX.anchor = GridBagConstraints.EAST;
      	gbc_lblX.gridx = 1;
      	gbc_lblX.gridy = 1;
      	paramFuncTab.add(lblX, gbc_lblX);
      	
      	// Parametric function input fields.
      	inputX = new JTextField();
      	GridBagConstraints gbc_paramFuncInput = new GridBagConstraints();
      	gbc_paramFuncInput.gridwidth = 4;
      	gbc_paramFuncInput.insets = new Insets(0, 0, 5, 5);
      	gbc_paramFuncInput.fill = GridBagConstraints.HORIZONTAL;
      	gbc_paramFuncInput.anchor = GridBagConstraints.NORTH;
      	gbc_paramFuncInput.gridx = 2;
      	gbc_paramFuncInput.gridy = 1;
      	paramFuncTab.add(inputX, gbc_paramFuncInput);
      	inputX.setColumns(10);
      	
      	lblY = new JLabel("y =");
      	GridBagConstraints gbc_lblY = new GridBagConstraints();
      	gbc_lblY.insets = new Insets(0, 0, 5, 5);
      	gbc_lblY.anchor = GridBagConstraints.EAST;
      	gbc_lblY.gridx = 1;
      	gbc_lblY.gridy = 2;
      	paramFuncTab.add(lblY, gbc_lblY);
      	
      	inputY = new JTextField();
      	inputY.setColumns(10);
      	GridBagConstraints gbc_textField = new GridBagConstraints();
      	gbc_textField.gridwidth = 4;
      	gbc_textField.insets = new Insets(0, 0, 5, 5);
      	gbc_textField.fill = GridBagConstraints.HORIZONTAL;
      	gbc_textField.gridx = 2;
      	gbc_textField.gridy = 2;
      	paramFuncTab.add(inputY, gbc_textField);
      	
      	lblZ = new JLabel("z =");
      	GridBagConstraints gbc_lblZ = new GridBagConstraints();
      	gbc_lblZ.anchor = GridBagConstraints.EAST;
      	gbc_lblZ.insets = new Insets(0, 0, 5, 5);
      	gbc_lblZ.gridx = 1;
      	gbc_lblZ.gridy = 3;
      	paramFuncTab.add(lblZ, gbc_lblZ);
      	
      	inputZ = new JTextField();
      	inputZ.setColumns(10);
      	GridBagConstraints gbc_textField_1 = new GridBagConstraints();
      	gbc_textField_1.gridwidth = 4;
      	gbc_textField_1.insets = new Insets(0, 0, 5, 5);
      	gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
      	gbc_textField_1.gridx = 2;
      	gbc_textField_1.gridy = 3;
      	paramFuncTab.add(inputZ, gbc_textField_1);
      	
      	GuiUtil.setupUndoListener(inputX);
      	GuiUtil.setupUndoListener(inputY);
      	GuiUtil.setupUndoListener(inputZ);
      	
      	// Input detection
      	KeyListener inputListener = new KeyAdapter() {
      		// Plot the graph.
      		
      		@Override
      		public void keyPressed(KeyEvent e) {
      			String[] paramExpr = new String[]{inputX.getText(),inputY.getText(),inputZ.getText()};
      			if (e.getKeyCode() == KeyEvent.VK_ENTER && (inputX.isFocusOwner() || inputY.isFocusOwner() || inputZ.isFocusOwner())) {
      				try {
 	    				addPlot(paramExpr,paramTemplateFunc.getColor(), paramGridOptionPanel.getGridBounds(), paramGridOptionPanel.getGridStepSize());
 					} catch (ExpressionParseException e1) {
 						JOptionPane.showMessageDialog(frame,new JLabel(e1.getMessage(),JLabel.CENTER));
 					}
      			}
      		}
      	};
      	FocusListener focusListener = new FocusAdapter() {
 			
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				try {
 					setSelected(paramTemplateFunc);
 				} catch (ExpressionParseException e) {
 					e.printStackTrace();
 				}
 			}
 		};
      	inputX.addKeyListener(inputListener);
      	inputY.addKeyListener(inputListener);
      	inputZ.addKeyListener(inputListener);
      	inputX.addFocusListener(focusListener);
      	inputY.addFocusListener(focusListener);
      	inputZ.addFocusListener(focusListener);
      	
      	// OptionPanel
      	paramOptionPanel = new JPanel();
      	GridBagConstraints gbc_panel_1 = new GridBagConstraints();
      	gbc_panel_1.gridwidth = 5;
      	gbc_panel_1.insets = new Insets(0, 0, 5, 5);
      	gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
      	gbc_panel_1.gridx = 1;
      	gbc_panel_1.gridy = 5;
      	paramFuncTab.add(paramOptionPanel, gbc_panel_1);
      	paramOptionPanel.setBorder(BorderFactory.createEtchedBorder());
      	
      	paramGridOptionPanel = new ParamGridOptionPanel(DEFAULT_BOUNDS);
      	paramAppearancePanel = new AppearanceOptionPanel(colorList, map);
      	paramOptionPanel.setLayout(new BoxLayout(paramOptionPanel, BoxLayout.Y_AXIS));
      	paramOptionPanel.add(paramGridOptionPanel);
      	paramOptionPanel.add(paramAppearancePanel);
      	
      	paramGridOptionPanel.addFunctionListener(createGridOptionPanelListener());
      	
     	// The parametric function list
      	paramFuncOuterPanel = new JPanel();
      	paramFuncPanelWrapper = new JScrollPane(paramFuncOuterPanel);
      	paramFuncPanelWrapper.setBorder(BorderFactory.createEtchedBorder());
      	paramFuncPanelWrapper.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      	GridBagConstraints gbc_paramFuncPanel = new GridBagConstraints();
      	gbc_paramFuncPanel.fill = GridBagConstraints.BOTH;
      	gbc_paramFuncPanel.gridwidth = 5;
      	gbc_paramFuncPanel.insets = new Insets(0, 0, 5, 5);
      	gbc_paramFuncPanel.gridx = 1;
      	gbc_paramFuncPanel.gridy = 6;
      	paramFuncTab.add(paramFuncPanelWrapper, gbc_paramFuncPanel);
      	GridBagLayout gbl_paramFuncPanel = new GridBagLayout();
      	gbl_paramFuncPanel.columnWidths = new int[]{0, 0};
      	gbl_paramFuncPanel.rowHeights = new int[]{0, 0};
      	gbl_paramFuncPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
      	gbl_paramFuncPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
      	paramFuncOuterPanel.setLayout(gbl_paramFuncPanel);
      	
      	paramFuncInnerPanel = new JPanel();
      	GridBagConstraints gbc_panel_param = new GridBagConstraints();
      	gbc_panel_param.anchor = GridBagConstraints.NORTH;
      	gbc_panel_param.fill = GridBagConstraints.HORIZONTAL;
      	gbc_panel_param.gridx = 0;
      	gbc_panel_param.gridy = 0;
      	paramFuncOuterPanel.add(paramFuncInnerPanel, gbc_panel_param);
      	paramFuncInnerPanel.setLayout(new BoxLayout(paramFuncInnerPanel, BoxLayout.Y_AXIS));
      	
 	}
 	
 	private void autoResize(){
 		controlsWidth = frame.getWidth() - CANVAS_INITIAL_WIDTH;
 		controlsHeight = frame.getHeight() - CANVAS_INITIAL_HEIGTH;
 		tabbedPane.setPreferredSize(new Dimension(tabbedPane.getWidth(),tabbedPane.getHeight()));
 		frame.setMinimumSize(new Dimension(600, 400));
 		// Auto resize frame.
 		resizeTimer = new javax.swing.Timer(100, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				plotter.updateSize(frame.getWidth()- controlsWidth,frame.getHeight()- controlsHeight);
 				frame.pack();
 			}
 		});
 		frame.addComponentListener(new ComponentAdapter() {
 			public void componentResized(ComponentEvent e) {
 				resizeTimer.restart();
 			}
 		});
 	}
 	
 	private FunctionLabel addXYZPlot(Function newFunction) {
 		stdFuncList.add(newFunction);
 		final StdFunctionLabel label = new StdFunctionLabel(newFunction);
 		
 		label.addFocusListener(new FocusAdapter() {
 			
 			@Override
 			public void focusGained(FocusEvent e) {
 				try {
 					setSelected(label.getMother());
 				} catch (ExpressionParseException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		stdFuncInnerPanel.add(label);
 		return label;
 	}
 	
 	private FunctionLabel addParametricPlot(final Function newFunction) {
 		paramFuncList.add(newFunction);
 		
 		final ParametricFunctionLabel label = new ParametricFunctionLabel(newFunction);
 		
 		label.addFocusListener(new FocusAdapter() {
 			
 			@Override
 			public void focusGained(FocusEvent e) {
 				try {
 					setSelected(label.getMother());
 				} catch (ExpressionParseException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		paramFuncInnerPanel.add(label);
 		return label;
 	}
 	
 	/*
 	 * Add new plot.
 	 */
 	private void addPlot(String[] expr, Color3f color, String[] bounds, float[] stepSize) {
 		// Create the function.
 		try {
 			Function newFunction = FunctionUtil.createFunction(expr,color,bounds,stepSize);
 			addPlot(newFunction);
 		} catch (ExpressionParseException | IllegalEquationException | UndefinedVariableException e) {
 			String message = e.getMessage();
 			JLabel label = new JLabel(message,JLabel.CENTER);
 			JOptionPane.showMessageDialog(frame,label);
 		} 
 		
 	}
 	
 	private void addPlot(Function function) {
 		FunctionLabel label = null;
 		
 		if (function.getClass() == ParametricFunction.class) {
 			label = addParametricPlot(function);
 		} else {
 			label = addXYZPlot(function);
 		}
 		
 		label.addFunctionListener(createFunctionListener());
 		
 		map.put(function, label);
 		spawnNewPlotterThread(function);
 		frame.pack();
 	}
 	
 	private FunctionListener createFunctionListener() {
 		return new FunctionListener() {
 			
 			@Override
 			public void functionChanged(FunctionEvent e) {
 				FunctionEvent.ACTION action = e.getAction();
 				Function func = e.getOldFunction();
 				
 				if (action == FunctionEvent.ACTION.VISIBILITY) {
 					plotter.showPlot(func);
 					
 				} else if (action == FunctionEvent.ACTION.DELETE){
 					deletePlot(func);
 					
 				} else if (action == FunctionEvent.ACTION.UPDATE) {
 					updatePlot(func, e.getNewExpr(), e.getColor(), e.getStringBounds(), e.getStepsize());
 				} 
 				
 			}
 		};
 	}
 	
 	/*
 	 * Update a function.
 	 */
 	private void updatePlot(Function oldFunc, String newExpr[], Color3f newColor, String[] bounds, float[] stepSize) {
 		// Try evaluating the function.
 		try {
 			Function newFunc = FunctionUtil.createFunction(newExpr, newColor, bounds, stepSize);
 			newFunc.setView(oldFunc.getView());
 			
 			if (oldFunc.getClass() == ParametricFunction.class) {
 				paramFuncList.set(paramFuncList.indexOf(oldFunc), newFunc);
 				paramAppearancePanel.updateFuncReference(newFunc);
 				paramGridOptionPanel.updateFuncReference(newFunc);
 			} else {
 				stdFuncList.set(stdFuncList.indexOf(oldFunc), newFunc);
 				stdAppearancePanel.updateFuncReference(newFunc);
 				stdGridOptionPanel.updateFuncReference(newFunc);
 			}
 			FunctionLabel label = map.get(oldFunc);
 			label.setMother(newFunc);
 			map.remove(oldFunc);
 			map.put(newFunc, label);
 			setSelected(newFunc);
 			plotter.removePlot(oldFunc);
 			spawnNewPlotterThread(newFunc);
 			frame.pack();
 		} 
 		// Catch error.
 		catch (ExpressionParseException e) {
 			String message = e.getMessage();
 			JLabel label = new JLabel(message,JLabel.CENTER);
 			JOptionPane.showMessageDialog(frame,label);
 		} catch (IllegalEquationException e) {
 			String message = e.getMessage();
 			JLabel label = new JLabel(message,JLabel.CENTER);
 			JOptionPane.showMessageDialog(frame,label);
 		} catch (UndefinedVariableException e) {
 			String message = e.getMessage();
 			JLabel label = new JLabel(message,JLabel.CENTER);
 			JOptionPane.showMessageDialog(frame,label);
 		}
 	}
 
 	/*
 	 * Delete a function.
 	 */
 	private void deletePlot(Function f) {
 		f.cancel();
 		plotter.removePlot(f);
 		if (f.getClass() == ParametricFunction.class) {
 			int index = paramFuncList.indexOf(f);
 			paramFuncInnerPanel.remove(index);
 			paramFuncList.remove(index);
 		} else {
 			int index = stdFuncList.indexOf(f);
 			stdFuncInnerPanel.remove(index);
 			stdFuncList.remove(index);
 		}
 		
 		
 		map.remove(f);
 		frame.pack();
 	}
 
 	/*
 	 * Spawn simple color chooser.
 	 */
 	private void spawnColorChooser(){
 		if(colorDialog == null){
 		colorDialog = new JDialog();
 		colorDialog.setLocation(frame.getLocationOnScreen());
 		ColorOptionPanel colorOptionPanel = new ColorOptionPanel();
 		colorOptionPanel.addChangeListener(new ChangeListener() {
 			
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				if(e.getSource().equals("CLOSE")){
 					colorDialog.setVisible(false);
 				}
 				else if(!colorList.contains(e.getSource())){
 					colorList.add((Color3f) e.getSource());
 					stdAppearancePanel.updateColors();
 					paramAppearancePanel.updateColors();
 				}
 			}
 		});
 		colorDialog.getContentPane().add(colorOptionPanel);
 		colorDialog.pack();
 		}
 		colorDialog.setVisible(true);
 	}
 
 	/*
	 * Spawn new plotter thread.
 	 */
 	private void spawnNewPlotterThread(final Function function) {
 		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
 			
 			@Override
 			protected Void doInBackground() throws Exception {
 
 				map.get(function).setIndeterminate(true);
 				// Test of spinner.
 				// Thread.currentThread().sleep(5000);
 				plotter.plotFunction(function);
 				return null;
 			}
 			
 			@Override
 			protected void done() {
 				FunctionLabel label = map.get(function);
 				if (label != null)
 					label.setIndeterminate(false);
 			}
 			
 		};
 		plottingQueue.execute(worker);
 	}
 	
 	private void savePlotToDisk(final File outputFile) {
 		Thread t = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				RenderedImage outputImage = plotter.takeScreenshot();
 
 				try {
 					boolean canWrite = ImageIO.write(outputImage, GuiUtil.getFileExtension(outputFile), outputFile);
 					if (!canWrite) {
 						
 						File newPath = new File(outputFile.getAbsolutePath() + "." + defaultImageExtension);
 						ImageIO.write(outputImage, defaultImageExtension, newPath);
 						showMessageDialogThreadSafe("Unknown image format. Defaulted to " + defaultImageExtension);
 					}
 				} catch (IOException e) {
 					showMessageDialogThreadSafe("Unable to write to file");
 				}
 			}
 		});
 		t.start();
 		
 	}
 
 	private void showMessageDialogThreadSafe(final String message) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				JOptionPane.showMessageDialog(frame, message);
 			}
 		});
 	}
 
 	private void importFunctions() {
 		File inputFile = GuiUtil.spawnImportDialog(filePath, frame);
 		if(inputFile != null){
 			filePath=inputFile.getPath().replace(inputFile.getName(), "");
 			try{
 				ZippedFunction[][] importLists = (ZippedFunction[][]) ObjectReader.ObjectFromFile(inputFile);
 				// Determine if current workspace should be erased.
 				boolean eraseWorkspace = (map.size() == 0) ||
 						(0 == JOptionPane.showOptionDialog(frame, 
 								"Would you like to erase current workspace during import?",
 								"Import Dialog", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null));
 
 				if(eraseWorkspace){
 					for (Function f : map.keySet()) {
 						deletePlot(f);
 					}
 				}
 				//Read new functions from zipped object.
 				for(int i = 0; i < importLists.length; i++){
 					ZippedFunction[] current = importLists[i];
 					for (int j = 0; j < current.length; j++) {
 						addPlot(FunctionUtil.loadFunction(current[j]));
 					}
 				}
 			}
 			catch(IOException | ClassCastException | ClassNotFoundException | ExpressionParseException | IllegalArgumentException | UndefinedVariableException | IllegalEquationException ex){
 				JOptionPane.showMessageDialog(frame,new JLabel("Unable to import workspace from file.",JLabel.CENTER));
 			} 
 		}
 	}
 
 	private void setSelected(Function f) throws ExpressionParseException{
 		if(f != selectedFunction){
 			
 			// Deselection.
 			if(selectedFunction != null && selectedFunction == stdTemplateFunc){
 				stdFuncInput.setBackground(NORMAL_COLOR);
 			}
 			else if(selectedFunction != null && selectedFunction == paramTemplateFunc){
 				inputX.setBackground(NORMAL_COLOR);
 				inputY.setBackground(NORMAL_COLOR);
 				inputZ.setBackground(NORMAL_COLOR);
 			}
 			else if (selectedFunction != null){
 				try{
 				map.get(selectedFunction).setSelected(false);
 				}
 				catch(NullPointerException e){
 					// Do nothing.
 				}
 			}
 			selectedFunction = f;
 			
 			// Selection.
 			if(selectedFunction != null && selectedFunction == stdTemplateFunc){
 				stdFuncInput.setBackground(SELECTED_COLOR);
 				stdGridOptionPanel.updateFuncReference(selectedFunction);
 				stdAppearancePanel.updateFuncReference(selectedFunction);
 			}
 			else if(selectedFunction != null && selectedFunction == paramTemplateFunc){
 				inputX.setBackground(SELECTED_COLOR);
 				inputY.setBackground(SELECTED_COLOR);
 				inputZ.setBackground(SELECTED_COLOR);
 				paramGridOptionPanel.updateFuncReference(selectedFunction);
 				paramAppearancePanel.updateFuncReference(selectedFunction);
 			}
 			else if (selectedFunction != null){
 				FunctionLabel selectedLabel = map.get(selectedFunction);
 				if(selectedLabel.getClass() == ParametricFunctionLabel.class){
 					paramAppearancePanel.updateFuncReference(selectedFunction);
 					paramGridOptionPanel.updateFuncReference(selectedFunction);
 				}
 				else if(selectedLabel.getClass() == StdFunctionLabel.class){
 					stdAppearancePanel.updateFuncReference(selectedFunction);
 					stdGridOptionPanel.updateFuncReference(selectedFunction);
 				}
 				selectedLabel.setSelected(true);
 			}
 		}
 	}
 }
