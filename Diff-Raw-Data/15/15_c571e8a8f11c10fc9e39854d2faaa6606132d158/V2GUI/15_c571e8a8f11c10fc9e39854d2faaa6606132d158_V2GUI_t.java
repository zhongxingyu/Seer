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
 
 import com.graphbuilder.math.*;
 
 
 /**
  * A simple GUI for the 3DPlotter application.
  * 
  * @author xXx
  *
  */
 public class V2GUI {
 
 	private static enum TYPE{PARAM, STD};
 	
 	private static final int CANVAS_INITIAL_WIDTH = 600;
 	private static final int CANVAS_INITIAL_HEIGTH = 600;
 	private static final float[] DEFAULT_BOUNDS = {-1,1,-1,1,-1,1};
 	
 	// GUI Variables.
 	private static V2GUI window;
 	private JFrame frame;
 	private JPanel stdFuncTab, stdFuncOuterPanel, stdFuncInnerPanel;
 	private JScrollPane stdFuncPanelWrapper;
 	private JPanel paramFuncTab, paramFuncOuterPanel, paramFuncInnerPanel;
 	private JScrollPane paramFuncPanelWrapper;
 	private JPanel optionPanel, canvasPanel;
 	private JTabbedPane tabbedPane;
 	private JDialog colorDialog;
 	private EditOptionPanel stdEditOptionPanel;
 	private EditOptionPanel paramEditOptionPanel;
 
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
 	private JLabel label_1, label_2, label_3;
 	private JTextField txtXmin, txtYmin, txtZmin, txtXmax, txtYmax,  txtZmax;
 	private JMenuBar menuBar;
 	private JMenu mnFile;
 	private JMenuItem mntmSaveProject, mntmLoadProject, mntmExit, mntmPrintCanvas;
 	private JMenu mnColorOptions;
 	private JMenu mnHelp;
 	private JMenuItem mntmDocumentation, mntmAbout;
 	private JMenuItem mntmImportColors, mntmExportColors, mntmAddCustomColor;
 	private JTextField inputX, inputY, inputZ;
 	private JLabel lblX, lblY, lblZ;
 	
 	
 	// Plotter renderes
 	private ExecutorService plottingQueue = Executors.newSingleThreadExecutor(); // Only plot one at a time
 	private JLabel lblStepSize;
 	private JPanel panel;
 	private JTextField txtTmin;
 	private JLabel label_4;
 	private JTextField txtTmax;
 	private JTextField txtUmin;
 	private JLabel label_5;
 	private JTextField txtUmax;
 	private JLabel lblResolution;
 	private String	defaultImageExtension = "png";
 	private JSlider slider;
 	private JSlider paramSlider;
 	private JPanel stdSelectedPanel;
 	private JPanel paramSelectedPanel;
 	
 	// Fun
 	JPanel targetPanel;
 	
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
 		colorList = new ColorList();
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
 		
      	//TODO: TAG
 		try {
 			addPlot(new String[]{"y = sin(x*5)*cos(z*5)"}, colorList.getNextAvailableColor(stdFuncList), new float[]{-1,1,-1,1,-1,1}, GuiUtil.getStepsize(slider.getValue(), getBounds(TYPE.STD))[0]);
 		} catch (ExpressionParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
      	// Finish up.
      	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      	frame.setVisible(true);
      	frame.pack();
      	
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
 						addPlot(new String[]{stdFuncInput.getText()},colorList.getNextAvailableColor(stdFuncList), getBounds(TYPE.STD), GuiUtil.getStepsize(slider.getValue(),getBounds(TYPE.STD))[0]);
 					} catch (ExpressionParseException e1) {
 						JOptionPane.showMessageDialog(frame,new JLabel(e1.getMessage(),JLabel.CENTER));
 					}
      			}
      			
      		}
      	});
 
      	optionPanel = new JPanel();
      	optionPanel.setBorder(BorderFactory.createEtchedBorder());
      	GridBagConstraints gbc_optionPanel = new GridBagConstraints();
      	gbc_optionPanel.fill = GridBagConstraints.HORIZONTAL;
      	gbc_optionPanel.gridwidth = 5;
      	gbc_optionPanel.insets = new Insets(0, 0, 5, 5);
      	gbc_optionPanel.gridx = 1;
      	gbc_optionPanel.gridy = 3;
      	stdFuncTab.add(optionPanel, gbc_optionPanel);
      	GridBagLayout gbl_panel = new GridBagLayout();
      	gbl_panel.columnWidths = new int[]{0, 15, 15, 30, 30, 0, 0};
      	gbl_panel.rowHeights = new int[]{10, 0, 0, 0, 5, 0, 0};
      	gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
      	gbl_panel.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
      	optionPanel.setLayout(gbl_panel);
 
      	// The limit data.
      	txtXmin = new JTextField();
      	GridBagConstraints gbc_txtXmin = new GridBagConstraints();
      	gbc_txtXmin.gridwidth = 2;
      	gbc_txtXmin.fill = GridBagConstraints.HORIZONTAL;
      	gbc_txtXmin.insets = new Insets(0, 0, 5, 5);
      	gbc_txtXmin.gridx = 1;
      	gbc_txtXmin.gridy = 1;
      	optionPanel.add(txtXmin, gbc_txtXmin);
      	txtXmin.setText("" + DEFAULT_BOUNDS[0]);
      	txtXmin.setColumns(10);
 
      	label_1 = new JLabel("< x <");
      	GridBagConstraints gbc_label_1 = new GridBagConstraints();
      	gbc_label_1.insets = new Insets(0, 0, 5, 5);
      	gbc_label_1.gridx = 3;
      	gbc_label_1.gridy = 1;
      	optionPanel.add(label_1, gbc_label_1);
 
      	txtXmax = new JTextField();
      	GridBagConstraints gbc_txtXmax = new GridBagConstraints();
      	gbc_txtXmax.fill = GridBagConstraints.HORIZONTAL;
      	gbc_txtXmax.insets = new Insets(0, 0, 5, 5);
      	gbc_txtXmax.gridx = 4;
      	gbc_txtXmax.gridy = 1;
      	optionPanel.add(txtXmax, gbc_txtXmax);
      	txtXmax.setText("" + DEFAULT_BOUNDS[1]);
      	txtXmax.setColumns(10);
 
      	txtYmin = new JTextField();
      	GridBagConstraints gbc_txtYmin = new GridBagConstraints();
      	gbc_txtYmin.gridwidth = 2;
      	gbc_txtYmin.fill = GridBagConstraints.HORIZONTAL;
      	gbc_txtYmin.insets = new Insets(0, 0, 5, 5);
      	gbc_txtYmin.gridx = 1;
      	gbc_txtYmin.gridy = 2;
      	optionPanel.add(txtYmin, gbc_txtYmin);
      	txtYmin.setText("" + DEFAULT_BOUNDS[2]);
      	txtYmin.setColumns(10);
 
      	label_2 = new JLabel("< y <");
      	GridBagConstraints gbc_label_2 = new GridBagConstraints();
      	gbc_label_2.insets = new Insets(0, 0, 5, 5);
      	gbc_label_2.gridx = 3;
      	gbc_label_2.gridy = 2;
      	optionPanel.add(label_2, gbc_label_2);
 
      	txtYmax = new JTextField();
      	GridBagConstraints gbc_txtYmax = new GridBagConstraints();
      	gbc_txtYmax.fill = GridBagConstraints.HORIZONTAL;
      	gbc_txtYmax.insets = new Insets(0, 0, 5, 5);
      	gbc_txtYmax.gridx = 4;
      	gbc_txtYmax.gridy = 2;
      	optionPanel.add(txtYmax, gbc_txtYmax);
      	txtYmax.setText("" + DEFAULT_BOUNDS[3]);
      	txtYmax.setColumns(10);
 
      	txtZmin = new JTextField();
      	GridBagConstraints gbc_txtZmin = new GridBagConstraints();
      	gbc_txtZmin.gridwidth = 2;
      	gbc_txtZmin.fill = GridBagConstraints.HORIZONTAL;
      	gbc_txtZmin.insets = new Insets(0, 0, 5, 5);
      	gbc_txtZmin.gridx = 1;
      	gbc_txtZmin.gridy = 3;
      	optionPanel.add(txtZmin, gbc_txtZmin);
      	txtZmin.setText("" + DEFAULT_BOUNDS[4]);
      	txtZmin.setColumns(10);
 
      	label_3 = new JLabel("< z <");
      	GridBagConstraints gbc_label_3 = new GridBagConstraints();
      	gbc_label_3.insets = new Insets(0, 0, 5, 5);
      	gbc_label_3.gridx = 3;
      	gbc_label_3.gridy = 3;
      	optionPanel.add(label_3, gbc_label_3);
 
      	txtZmax = new JTextField();
      	GridBagConstraints gbc_txtZmax = new GridBagConstraints();
      	gbc_txtZmax.fill = GridBagConstraints.HORIZONTAL;
      	gbc_txtZmax.insets = new Insets(0, 0, 5, 5);
      	gbc_txtZmax.gridx = 4;
      	gbc_txtZmax.gridy = 3;
      	optionPanel.add(txtZmax, gbc_txtZmax);
      	txtZmax.setText("" + DEFAULT_BOUNDS[5]);
      	txtZmax.setColumns(10);
 
      	GuiUtil.setupUndoListener(txtXmin);
      	GuiUtil.setupUndoListener(txtXmax);
      	GuiUtil.setupUndoListener(txtYmin);
      	GuiUtil.setupUndoListener(txtYmax);
      	GuiUtil.setupUndoListener(txtZmin);
      	GuiUtil.setupUndoListener(txtZmax);
 
      	lblStepSize = new JLabel("Resolution");
      	lblStepSize.setHorizontalAlignment(SwingConstants.CENTER);
      	GridBagConstraints gbc_lblStepSize = new GridBagConstraints();
      	gbc_lblStepSize.anchor = GridBagConstraints.EAST;
      	gbc_lblStepSize.insets = new Insets(0, 0, 0, 5);
      	gbc_lblStepSize.gridx = 1;
      	gbc_lblStepSize.gridy = 5;
      	optionPanel.add(lblStepSize, gbc_lblStepSize);
      	
      	slider = new JSlider();
      	slider.setPreferredSize(new Dimension(100, 20));
      	GridBagConstraints gbc_slider = new GridBagConstraints();
      	gbc_slider.gridwidth = 3;
      	gbc_slider.fill = GridBagConstraints.HORIZONTAL;
      	gbc_slider.insets = new Insets(0, 0, 0, 5);
      	gbc_slider.gridx = 2;
      	gbc_slider.gridy = 5;
      	optionPanel.add(slider, gbc_slider);
 
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
      	
 		stdEditOptionPanel = new EditOptionPanel(colorList, null, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(e.getID() == 0){
 					Function[] source = (Function[]) e.getSource();
 					source[0].setColor(source[1].getColor());
 				}
 				if(e.getID() == 1){
 					Function[] source = (Function[]) e.getSource();
 					updatePlot(source[0], source[0].getExpression(), source[1].getColor(), source[1].getBounds(), source[1].getStepsize());
 				}
 			}
 		});
      	GridBagConstraints gbc_panel_1 = new GridBagConstraints();
      	gbc_panel_1.fill = GridBagConstraints.BOTH;
      	gbc_panel_1.gridwidth = 5;
      	gbc_panel_1.insets = new Insets(0, 0, 5, 5);
      	gbc_panel_1.gridx = 1;
      	gbc_panel_1.gridy = 8;
      	stdFuncTab.add(stdEditOptionPanel, gbc_panel_1);
 	}
 	
 	private void initParamFunctionTab(){
 		// The parametric function tab.
      	paramFuncTab = new JPanel();
      	tabbedPane.addTab("Parametric equations", paramFuncTab);
      	GridBagLayout gbl_paramFunctionPanel = new GridBagLayout();
      	gbl_paramFunctionPanel.columnWidths = new int[]{5, 25, 50, 50, 50, 25, 5, 0};
      	gbl_paramFunctionPanel.rowHeights = new int[]{5, 0, 0, 0, 0, 10, 5, 5, 0, 0, 0};
      	gbl_paramFunctionPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
 	    				addPlot(paramExpr,colorList.getNextAvailableColor(paramFuncList), getBounds(TYPE.PARAM), GuiUtil.getStepsize(paramSlider.getValue(),getBounds(TYPE.PARAM))[0]);
 					} catch (ExpressionParseException e1) {
 						JOptionPane.showMessageDialog(frame,new JLabel(e1.getMessage(),JLabel.CENTER));
 					}
      			}
      		}
      	};
      	inputX.addKeyListener(inputListener);
      	inputY.addKeyListener(inputListener);
      	inputZ.addKeyListener(inputListener);
      	
      	panel = new JPanel();
      	panel.setBorder(BorderFactory.createEtchedBorder());
      	GridBagConstraints gbc_panel = new GridBagConstraints();
      	gbc_panel.gridwidth = 5;
      	gbc_panel.insets = new Insets(0, 0, 5, 5);
      	gbc_panel.fill = GridBagConstraints.HORIZONTAL;
      	gbc_panel.gridx = 1;
      	gbc_panel.gridy = 5;
      	paramFuncTab.add(panel, gbc_panel);
      	GridBagLayout gbl_panel = new GridBagLayout();
      	gbl_panel.columnWidths = new int[]{0, 15, 15, 0, 30, 30, 0, 0};
      	gbl_panel.rowHeights = new int[]{10, 0, 0, 5, 0, 5, 0};
      	gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
      	gbl_panel.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
      	panel.setLayout(gbl_panel);
      	
      	txtTmin = new JTextField();
      	txtTmin.setText("0");
      	txtTmin.setColumns(10);
      	GridBagConstraints gbc_textField_param = new GridBagConstraints();
      	gbc_textField_param.gridwidth = 2;
      	gbc_textField_param.insets = new Insets(0, 0, 5, 5);
      	gbc_textField_param.gridx = 1;
      	gbc_textField_param.gridy = 1;
      	panel.add(txtTmin, gbc_textField_param);
      	
      	label_4 = new JLabel("< t <");
      	GridBagConstraints gbc_label_4 = new GridBagConstraints();
      	gbc_label_4.insets = new Insets(0, 0, 5, 5);
      	gbc_label_4.gridx = 3;
      	gbc_label_4.gridy = 1;
      	panel.add(label_4, gbc_label_4);
      	
      	txtTmax = new JTextField();
      	txtTmax.setText("2*pi");
      	txtTmax.setColumns(10);
      	GridBagConstraints gbc_textField_param_1 = new GridBagConstraints();
      	gbc_textField_param_1.gridwidth = 2;
      	gbc_textField_param_1.insets = new Insets(0, 0, 5, 5);
      	gbc_textField_param_1.gridx = 4;
      	gbc_textField_param_1.gridy = 1;
      	panel.add(txtTmax, gbc_textField_param_1);
      	
      	txtUmin = new JTextField();
      	txtUmin.setText("0");
      	txtUmin.setColumns(10);
      	GridBagConstraints gbc_textField_2 = new GridBagConstraints();
      	gbc_textField_2.gridwidth = 2;
      	gbc_textField_2.insets = new Insets(0, 0, 5, 5);
      	gbc_textField_2.gridx = 1;
      	gbc_textField_2.gridy = 2;
      	panel.add(txtUmin, gbc_textField_2);
      	
      	label_5 = new JLabel("< u <");
      	GridBagConstraints gbc_label_5 = new GridBagConstraints();
      	gbc_label_5.insets = new Insets(0, 0, 5, 5);
      	gbc_label_5.gridx = 3;
      	gbc_label_5.gridy = 2;
      	panel.add(label_5, gbc_label_5);
      	
      	txtUmax = new JTextField();
      	txtUmax.setText("2*pi");
      	txtUmax.setColumns(10);
      	GridBagConstraints gbc_textField_3 = new GridBagConstraints();
      	gbc_textField_3.gridwidth = 2;
      	gbc_textField_3.insets = new Insets(0, 0, 5, 5);
      	gbc_textField_3.gridx = 4;
      	gbc_textField_3.gridy = 2;
      	panel.add(txtUmax, gbc_textField_3);
      	
      	lblResolution = new JLabel("Resolution");
      	lblResolution.setHorizontalAlignment(SwingConstants.CENTER);
      	GridBagConstraints gbc_lblResolution = new GridBagConstraints();
      	gbc_lblResolution.anchor = GridBagConstraints.EAST;
      	gbc_lblResolution.insets = new Insets(0, 0, 5, 5);
      	gbc_lblResolution.gridx = 1;
      	gbc_lblResolution.gridy = 4;
      	panel.add(lblResolution, gbc_lblResolution);
      	
      	paramSlider = new JSlider();
      	paramSlider.setPreferredSize(new Dimension(100, 20));
      	GridBagConstraints gbc_slider_1 = new GridBagConstraints();
      	gbc_slider_1.fill = GridBagConstraints.HORIZONTAL;
      	gbc_slider_1.gridwidth = 4;
      	gbc_slider_1.insets = new Insets(0, 0, 5, 5);
      	gbc_slider_1.gridx = 2;
      	gbc_slider_1.gridy = 4;
      	panel.add(paramSlider, gbc_slider_1);
      	
      	// The parametric function list
      	paramFuncOuterPanel = new JPanel();
      	paramFuncPanelWrapper = new JScrollPane(paramFuncOuterPanel);
      	paramFuncPanelWrapper.setBorder(null);
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
      	
 		paramEditOptionPanel = new EditOptionPanel(colorList, null, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(e.getID() == 0){
 					Function[] source = (Function[]) e.getSource();
 					source[0].setColor(source[1].getColor());
 				}
 				if(e.getID() == 1){
 					Function[] source = (Function[]) e.getSource();
 					updatePlot(source[0], source[0].getExpression(), source[1].getColor(), source[1].getBounds(), source[1].getStepsize());
 				}
 			}
 		});
      	GridBagConstraints gbc_panel_1 = new GridBagConstraints();
      	gbc_panel_1.gridwidth = 5;
      	gbc_panel_1.insets = new Insets(0, 0, 5, 5);
      	gbc_panel_1.fill = GridBagConstraints.BOTH;
      	gbc_panel_1.gridx = 1;
      	gbc_panel_1.gridy = 8;
      	paramFuncTab.add(paramEditOptionPanel, gbc_panel_1);
 
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
 	
 	private void addXYZPlot(Function newFunction) {
 		stdFuncList.add(newFunction);
 		ActionListener listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Function sourceFunction = (Function) e.getSource();
 				if(e.getID() == FunctionLabel.UPDATE){
 					String newExpr = e.getActionCommand();
 					updatePlot(sourceFunction, new String[]{newExpr}, sourceFunction.getColor(), sourceFunction.getBounds(), sourceFunction.getStepsize());
 				}
 				if(e.getID() == FunctionLabel.UPDATEEDIT){
 					stdEditOptionPanel.updateFuncReference(sourceFunction);
 				}
 				if(e.getID() == FunctionLabel.DELETE){
 					deletePlot(sourceFunction);
 					stdEditOptionPanel.initMode();
 				}
 				if(e.getID() == FunctionLabel.VISIBILITY){
 					plotter.showPlot(sourceFunction);
 				}
 			}
 		};
 		StdFunctionLabel label = new StdFunctionLabel(newFunction, listener);
 		stdFuncInnerPanel.add(label);
 		map.put(newFunction, label);
 		doPlot(newFunction);
 	}
 	
 	private void addParametricPlot(Function newFunction) {
 		paramFuncList.add(newFunction);
 		
 		ActionListener listener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Function sourceFunction = (Function) e.getSource();
 				if(e.getID() == FunctionLabel.UPDATE){
 					String newExpr = e.getActionCommand();
 					updatePlot(sourceFunction, new String[]{newExpr}, sourceFunction.getColor(), sourceFunction.getBounds(), sourceFunction.getStepsize());
 				}
 				if(e.getID() == FunctionLabel.UPDATEEDIT){
 					paramEditOptionPanel.updateFuncReference(sourceFunction);
 				}
 				if(e.getID() == FunctionLabel.DELETE){
 					deletePlot(sourceFunction);
 					paramEditOptionPanel.initMode();
 				}
 				if(e.getID() == FunctionLabel.VISIBILITY){
 					plotter.showPlot(sourceFunction);
 				}
 			}
 		};
 
 		ParametricFunctionLabel label = new ParametricFunctionLabel(newFunction, listener);
 		paramFuncInnerPanel.add(label);
 		map.put(newFunction, label);
 
 		doPlot(newFunction);
 	}
 	
 	private void doPlot(Function function) {
 		spawnNewPlotterThread(function);
 		frame.pack();
 	}
 
 	/*
 	 * Add new plot.
 	 */
 	private void addPlot(String[] expr, Color3f color, float[] bounds, float stepSize) {
 		// Create the function.
 		try {
 			Function newFunction = FunctionUtil.createFunction(expr,color,bounds,stepSize);
 			if (newFunction.getClass() == ParametricFunction.class) {
 				addParametricPlot(newFunction);
 			} else {
 				addXYZPlot(newFunction);
 			}
 		} catch (ExpressionParseException | IllegalEquationException | UndefinedVariableException e) {
 			String message = e.getMessage();
 			JLabel label = new JLabel(message,JLabel.CENTER);
 			JOptionPane.showMessageDialog(frame,label);
 		} 
 		
 
 	}
 
 	/*
 	 * Update a function.
 	 */
 	private void updatePlot(Function oldFunc, String newExpr[], Color3f newColor, float[] bounds, float stepsize) {
 		// Try evaluating the function.
 		try {
 			Function newFunc = FunctionUtil.createFunction(newExpr, newColor, bounds, stepsize);
 			
 			if (oldFunc.getClass() == ParametricFunction.class) {
 				paramFuncList.set(paramFuncList.indexOf(oldFunc), newFunc);
 			} else {
 				stdFuncList.set(stdFuncList.indexOf(oldFunc), newFunc);
 			}
 			FunctionLabel label = map.get(oldFunc);
 			label.setMother(newFunc);
 			map.remove(oldFunc);
 			map.put(newFunc, label);
 			plotter.removePlot(oldFunc);
 			spawnNewPlotterThread(newFunc);
 			frame.pack();
 			
 			if (newFunc.getClass() == ParametricFunction.class) {
 				paramEditOptionPanel.updateFuncReference(newFunc);
 			} else {
 				stdEditOptionPanel.updateFuncReference(newFunc);
 			}
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
 	 * Return current bounds (set in GUI).
 	 */
 	private float[] getBounds(TYPE type) throws ExpressionParseException{
 		float[] bounds = new float[6];
 		if(type == TYPE.PARAM){
 			bounds[0] = GuiUtil.evalString(txtTmin.getText());
 			bounds[1] = GuiUtil.evalString(txtTmax.getText());
 			bounds[2] = GuiUtil.evalString(txtUmin.getText());
 			bounds[3] = GuiUtil.evalString(txtUmax.getText());
 			bounds[4] = 0;
 			bounds[5] = 0;
 		}
 		if(type == TYPE.STD){
 			bounds[0] = GuiUtil.evalString(txtXmin.getText());
 			bounds[1] = GuiUtil.evalString(txtXmax.getText());
 			bounds[2] = GuiUtil.evalString(txtYmin.getText());
 			bounds[3] = GuiUtil.evalString(txtYmax.getText());
 			bounds[4] = GuiUtil.evalString(txtZmin.getText());
 			bounds[5] = GuiUtil.evalString(txtZmax.getText());
 		}
 		return bounds;
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
					stdEditOptionPanel.updateColors();
					paramEditOptionPanel.updateColors();
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
 				map.get(function).setIndeterminate(false);
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
 				boolean eraseWorkspace = (0 == 
 						JOptionPane.showOptionDialog(frame, 
 								"Would you like to erase current workspace during import?",
 								"Import Dialog", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null));
 
 				if(eraseWorkspace){
 					for(int i = stdFuncList.size()-1; i >= 0; i--){
 						deletePlot(stdFuncList.get(i));
 					}
 					for(int i = paramFuncList.size()-1; i >= 0; i--){
 						deletePlot(paramFuncList.get(i));
 					}
 				}
 				//Read new functions from zipped object.
 				for(int i = 0; i < importLists[0].length; i++){
 					addPlot(importLists[0][i].getExpression(), importLists[0][i].getColor(), importLists[0][i].getBounds(), importLists[0][i].getStepsize());
 					stdFuncList.get(i).setSelected(importLists[0][i].isSelected());
 					stdFuncList.get(i).setVisible(importLists[0][i].isVisible());
 				}
 				for(int i = 0; i < importLists[1].length; i++){
 					addPlot(importLists[1][i].getExpression(), importLists[1][i].getColor(), importLists[1][i].getBounds(), importLists[1][i].getStepsize());
 					paramFuncList.get(i).setSelected(importLists[1][i].isSelected());
 					paramFuncList.get(i).setVisible(importLists[1][i].isVisible());
 				}
 			}
 			catch(IOException | ClassCastException | ClassNotFoundException ex){
 				JOptionPane.showMessageDialog(frame,new JLabel("Unable to import workspace from file.",JLabel.CENTER));
 			}
 		}
 	}
 }
