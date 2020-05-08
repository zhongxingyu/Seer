 package Frames;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.text.BadLocationException;
 
 import org.imgscalr.Scalr;
 import org.tmatesoft.sqljet.core.SqlJetException;
 
 import ClassAdminBackEnd.EntityType;
 import ClassAdminBackEnd.FileHandler;
 import ClassAdminBackEnd.Global;
 import ClassAdminBackEnd.Project;
 import ClassAdminBackEnd.SuperEntityPointer;
 import ClassAdminBackEnd.UnsupportedFileTypeException;
 import ClassAdminFrontEnd.BackgroundGradientPanel;
 import ClassAdminFrontEnd.BlurBackground;
 import ClassAdminFrontEnd.BoxPlotFrame;
 import ClassAdminFrontEnd.Database;
 import ClassAdminFrontEnd.FadePanel;
 import ClassAdminFrontEnd.FrmTable;
 import ClassAdminFrontEnd.GradientMenuBar;
 import ClassAdminFrontEnd.HistogramFrame;
 import ClassAdminFrontEnd.ImagePanel;
 import ClassAdminFrontEnd.MenuImagePanel;
 import ClassAdminFrontEnd.ReflectionButton;
 import ClassAdminFrontEnd.ReflectionButtonWithLabel;
 import ClassAdminFrontEnd.ReflectionImagePanel;
 import ClassAdminFrontEnd.ScatterPlotFrame;
 import ClassAdminFrontEnd.ShadowPanel;
 import ClassAdminFrontEnd.ThreeStopGradientPanel;
 import ClassAdminFrontEnd.TreeView;
 import Rule.frmRule;
 
 public class Frame extends JFrame implements ActionListener {
 
 	private JPanel contentPane;
 	private FadePanel homePanel, workspacePanel, navBar, tabBar, infoPanel, scatterplotInfoPanel, recentDocsPanel, searchPanel;
 	private ThreeStopGradientPanel bottomPanel;
 	private BackgroundGradientPanel backgroundPanel;
 	private GradientMenuBar menuBarWindows;
 	private JMenuBar menuBarMAC;
 	private ReflectionImagePanel containerSelectTask, containerRecentDocs;
 	private MenuImagePanel studentsViewArrowOut, studentsViewArrowIn;
 	private ImagePanel boxChartImage, histogramChartImage, scatterplotChartImage, studentPhoto, searchImage;
 	private JFileChooser filechooser;
 	private JFrame frame = this;
 	private File currentFilePath;
 	private int tabCount = -1;
 	private FrmTable table;
 	private JTabbedPane tabbedPane;
 	private FileHandler fileHandler;
 	private BlurBackground blur;
 	private ReflectionButton homeButton, importButton, exportButton, studentsButton, histogramButton, boxButton, scatterButton,
 			conditionalFormatButton, bordercaseButton, addRowButton, homeImportButton, homeStudents, ButtonWorkspace, filterButton,
 			maxValButton, rulesButton, homeRapidAssessment, treeButton;
 	private FadePanel homeInfoPanel, importInfoPanel, exportInfoPanel, studentsInfoPanel, histogramInfoPanel, boxplotInfoPanel,
 			conditionalFormattingInfoPanel, bordercaseInfoPanel, addRowInfoPanel, filterInfoPanel, maxValInfoPanel, rulesInfoPanel, buildInfoPanel;
 	private ShadowPanel studentPanel;
 	private ReflectionButtonWithLabel[] buttonArray;
 
 	private Database db;
 
 	private int HOME_SPACE_LEFT_X;
 	private int HOME_SPACE_Y;
 	private int HOME_BOTTOM_SPACE_Y;
 	private int HOME_SPACE_RIGHT_X;
 
 	private static String currentOs;
 	private static String MAC_OS = "MAC";
 	private static String WIN_OS = "WINDOWS";
 
 	public class TabButton extends JPanel {
 
 		private String text;
 		private JLabel label;
 		private JLabel button;
 		private TabButton tabbutton = this;
 
 		public TabButton(String _text) {
 
 			// create label with file name for tab
 			text = _text;
 			label = new JLabel(text);
 			add(label);
 
 			// create close button
 			button = new JLabel("x");
 			// button.setBorder(new EmptyBorder(1,1,1,1));
 			add(button);
 			button.setForeground(Color.white);
 
 			// set this panel with label and close button to transparent
 			this.setOpaque(false);
 			this.setBorder(null);
 
 			// close tab action
 			button.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent e) {
 					tabbedPane.remove(tabbedPane.indexOfTabComponent(tabbutton));
 					tabCount--;
 					if (tabCount == -1) {
 						setNavButtonsDisabled();
 					}
 
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {
 					button.setForeground(Color.darkGray);
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 					button.setForeground(Color.white);
 				}
 
 			});
 		}
 	}
 
 	class SelectionListener implements ListSelectionListener {
 		JTable table;
 
 		SelectionListener(JTable table) {
 			this.table = table;
 		}
 
 		public void valueChanged(ListSelectionEvent e) {
 			if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
 				System.out.println("listen");
 			} else if (e.getSource() == table.getColumnModel().getSelectionModel() && table.getColumnSelectionAllowed()) {
 				System.out.println("listen2");
 			}
 			if (e.getValueIsAdjusting()) {
 				System.out.println("The mouse button has not yet been released");
 			}
 		}
 
 	}
 
 	/*
 	 * Method to create all frame contents
 	 */
 	public Frame() throws SqlJetException, IOException {
 
 		// set frame title
 		setTitle("UP Admin");
 
 		// get OS
 		determineOS();
 
 		// frame setup
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 1200, 700);
 
 		Image icon = Toolkit.getDefaultToolkit().getImage("Logo.png");
 		this.setIconImage(icon);
 
 		// Get the size of the screen
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		// Determine the new location of the window
 		int w = getSize().width;
 		int h = getSize().height;
 		int x = (dim.width - w) / 2;
 		int y = (dim.height - h) / 2;
 		// Move the window
 		setLocation(x, y);
 
 		// maximize window
 		// setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
 
 		// create content pane
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setBackground(new Color(0x212121));
 		contentPane.setLayout(null);
 
 		// check OS and setup accordingly
 		if (currentOs == MAC_OS) {
 			setupMAC();
 		} else {
 			setupWindows();
 		}
 
 		// create little bottom bar of home screen
 		bottomPanel = new ThreeStopGradientPanel(new Color(0xA1A1A1), new Color(0x696969), new Color(0x000000), contentPane);
 		bottomPanel.setSize(getWidth() - HOME_SPACE_RIGHT_X, 12);
 		bottomPanel.setBounds(HOME_SPACE_LEFT_X, getHeight() - HOME_BOTTOM_SPACE_Y, bottomPanel.getWidth(), bottomPanel.getHeight());
 		contentPane.add(bottomPanel);
 
 		setupHomeScreen();
 		setupWorkspaceScreen();
 
 		this.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent we) {
 				if (db != null)
 					db.closeDatabase();
 			}
 		});
 		// frame resize listener adjust components accordingly
 		this.addComponentListener(new ComponentListener() {
 
 			@Override
 			public void componentHidden(ComponentEvent arg0) {
 
 			}
 
 			@Override
 			public void componentMoved(ComponentEvent arg0) {
 
 			}
 
 			// resizes components with screen
 			@Override
 			public void componentResized(ComponentEvent arg0) {
 
 				bottomPanel.setBounds(HOME_SPACE_LEFT_X, frame.getHeight() - HOME_BOTTOM_SPACE_Y, frame.getWidth() - HOME_SPACE_RIGHT_X, 12);
 
 				if (currentOs != MAC_OS) {
 					backgroundPanel.setBounds(HOME_SPACE_LEFT_X, menuBarWindows.getHeight(), frame.getWidth() - HOME_SPACE_RIGHT_X,
 							frame.getHeight() - HOME_SPACE_Y - menuBarWindows.getHeight());
 				} else {
 
 					backgroundPanel.setSize(frame.getWidth() - HOME_SPACE_RIGHT_X, frame.getHeight() - HOME_SPACE_Y);
 					backgroundPanel.rerenderBackground();
 
 					bottomPanel.rerenderBackground();
 				}
 
 				workspacePanel.setBounds(0, 0, backgroundPanel.getWidth(), backgroundPanel.getHeight());
 
 				if (currentOs != MAC_OS) {
 					menuBarWindows.setBounds(0, 0, getWidth(), 30);
 				}
 
 				navBar.setBounds(0, backgroundPanel.getHeight() - 40 - 40, getWidth(), 80);
 				workspacePanel.add(navBar);
 
 				if (tabbedPane != null) {
 					tabbedPane.setBounds(20, 20, workspacePanel.getWidth() - 40, workspacePanel.getHeight() - 40 - navBar.getHeight());
 				}
 				if (tabBar != null) {
 					tabBar.setBounds(0, 0, frame.getWidth(), frame.getHeight());
 					tabBar.setLayout(null);
 				}
 				if (boxChartImage != null) {
 					boxChartImage.setBounds(tabBar.getWidth() - 70, 15, 50, 40);
 				}
 				if (histogramChartImage != null) {
 					histogramChartImage.setBounds(tabBar.getWidth() - 105, 15, 50, 40);
 				}
 				if (scatterplotChartImage != null) {
 					scatterplotChartImage.setBounds(tabBar.getWidth() - 140, 15, 50, 40);
 				}
 				if (infoPanel != null) {
 					infoPanel.setBounds(0, workspacePanel.getHeight() - 112, getWidth(), 43);
 				}
 				if (studentPanel != null) {
 					studentPanel.setBounds(frame.getWidth() - 45, 45, 250, getHeight() - 20);
 					studentPanel.setNewX(getWidth() - 45);
 					studentPanel.setOldX(getWidth() - 250);
 				}
 				if (searchPanel != null) {
 					searchPanel.setBounds(workspacePanel.getWidth() - 170, 10, 150, 30);
 				}
 
 			}
 
 			@Override
 			public void componentShown(ComponentEvent arg0) {
 
 			}
 		});
 
 		// get current filehandler (ClassAdminBackEnd)
 		fileHandler = FileHandler.get();
 
 		createRecentDocsDB();
 		createRecentDocsView();
 	}
 
 	/*
 	 * Method to setup frame positioning and menu according to Windows OS
 	 */
 	public void setupWindows() {
 		// create menubar
 		menuBarWindows = new GradientMenuBar();
 		menuBarWindows.setBounds(0, 0, getWidth(), 30);
 		contentPane.add(menuBarWindows);
 
 		// create menu
 		// FILE
 		JMenu mFile = new JMenu("File");
 		JMenu mNew = new JMenu("New");
 		JMenuItem miSpreadsheet = new JMenuItem("Spreadsheet");
 		JMenuItem miRapidAssessment = new JMenuItem("Rapid Assessment");
 		JMenu mRecent = new JMenu("Recent");
 		JMenuItem miClose = new JMenuItem("Close");
 		JMenuItem miCloseAll = new JMenuItem("Close All");
 		JSeparator sfile = new JSeparator();
 		JMenuItem miImport = new JMenuItem("Import");
 		JMenuItem miExport = new JMenuItem("Export");
 		JSeparator sfile2 = new JSeparator();
 		JMenuItem miExit = new JMenuItem("Exit");
 
 		mFile.setForeground(Color.white);
 
 		menuBarWindows.add(mFile);
 		mFile.add(mNew);
 		mNew.add(miSpreadsheet);
 		mNew.add(miRapidAssessment);
 		mFile.add(mRecent);
 		mFile.add(miClose);
 		mFile.add(miCloseAll);
 		mFile.add(sfile);
 		mFile.add(miImport);
 		mFile.add(miExport);
 		mFile.add(sfile2);
 		mFile.add(miExit);
 
 		// PROJECT
 		JMenu mProject = new JMenu("Project");
 		JMenuItem miConditionalFormatting = new JMenuItem("Conditional Formatting");
 		JMenuItem miBordercases = new JMenuItem("Bordercases");
 		JMenuItem miRules = new JMenuItem("Rules");
 		JSeparator sproject = new JSeparator();
 		JMenuItem miAddRow = new JMenuItem("Rules");
 		JMenuItem miAddMaxValues = new JMenuItem("Add Max Values");
 		JSeparator sproject2 = new JSeparator();
 		JMenuItem miFilter = new JMenuItem("Filter");
 		JMenuItem miViewStudent = new JMenuItem("View Selected Student");
 		JSeparator sproject3 = new JSeparator();
 		JMenu mGraph = new JMenu("View Graph");
 		JMenuItem miHistogram = new JMenuItem("Histogram");
 		JMenuItem miBoxPlot = new JMenuItem("Box Plot");
 		JMenuItem miScatterPlot = new JMenuItem("Histogram");
 
 		menuBarWindows.add(mProject);
 		mProject.add(miConditionalFormatting);
 		mProject.add(miBordercases);
 		mProject.add(miRules);
 		mProject.add(sproject);
 		mProject.add(miAddRow);
 		mProject.add(miAddMaxValues);
 		mProject.add(sproject2);
 		mProject.add(miFilter);
 		mProject.add(miViewStudent);
 		mProject.add(sproject3);
 		mProject.add(mGraph);
 		mGraph.add(miHistogram);
 		mGraph.add(miBoxPlot);
 		mGraph.add(miScatterPlot);
 
 		// SETTINGS
 		JMenu mSettings = new JMenu("Settings");
 		menuBarWindows.add(mSettings);
 
 		mFile.setForeground(Color.white);
 		mProject.setForeground(Color.white);
 		mSettings.setForeground(Color.white);
 		
 		miExit.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				frame.dispose();
 			}
 		});
 
 		// setup space constants
 		HOME_SPACE_LEFT_X = 3;
 		HOME_SPACE_Y = 55;
 		HOME_BOTTOM_SPACE_Y = 53;
 		HOME_SPACE_RIGHT_X = 22;
 
 		// create background gradient panel
 		backgroundPanel = new BackgroundGradientPanel(contentPane);
 		backgroundPanel.setSize(getWidth() - HOME_SPACE_RIGHT_X, getHeight() - HOME_SPACE_Y - menuBarWindows.getHeight());
 		backgroundPanel.setBounds(HOME_SPACE_LEFT_X, menuBarWindows.getHeight(), backgroundPanel.getWidth(), backgroundPanel.getHeight());
 		backgroundPanel.setLayout(null);
 		contentPane.add(backgroundPanel);
 	}
 
 	/*
 	 * Method to setup frame positioning and menu according to MAC OS
 	 */
 	public void setupMAC() {
 		// create menubar
 		menuBarMAC = new JMenuBar();
 		contentPane.add(menuBarMAC);
 		setJMenuBar(menuBarMAC);
 
 		// create menu
 		// FILE
 		JMenu mFile = new JMenu("File");
 		JMenu mNew = new JMenu("New");
 		JMenuItem miSpreadsheet = new JMenuItem("Spreadsheet");
 		JMenuItem miRapidAssessment = new JMenuItem("Rapid Assessment");
 		JMenuItem miOpen = new JMenuItem("Open");
 		JMenu mRecent = new JMenu("Recent");
 		JMenuItem miClose = new JMenuItem("Close");
 		JMenuItem miCloseAll = new JMenuItem("Close All");
 		JSeparator sfile = new JSeparator();
 		JMenuItem miImport = new JMenuItem("Import");
 		JMenuItem miExport = new JMenuItem("Export");
 		JSeparator sfile2 = new JSeparator();
 		JMenuItem miExit = new JMenuItem("Exit");
 
 		menuBarMAC.add(mFile);
 		mFile.add(mNew);
 		mNew.add(miSpreadsheet);
 		mNew.add(miRapidAssessment);
 		mFile.add(miOpen);
 		mFile.add(mRecent);
 		mFile.add(miClose);
 		mFile.add(miCloseAll);
 		mFile.add(sfile);
 		mFile.add(miImport);
 		mFile.add(miExport);
 		mFile.add(sfile2);
 		mFile.add(miExit);
 
 		// PROJECT
 		JMenu mProject = new JMenu("Project");
 		JMenuItem miConditionalFormatting = new JMenuItem("Conditional Formatting");
 		JMenuItem miBordercases = new JMenuItem("Bordercases");
 		JMenuItem miRules = new JMenuItem("Rules");
 		JSeparator sproject = new JSeparator();
 		JMenuItem miAddRow = new JMenuItem("Rules");
 		JMenuItem miAddMaxValues = new JMenuItem("Add Max Values");
 		JSeparator sproject2 = new JSeparator();
 		JMenuItem miFilter = new JMenuItem("Filter");
 		JMenuItem miViewStudent = new JMenuItem("View Selected Student");
 		JSeparator sproject3 = new JSeparator();
 		JMenu mGraph = new JMenu("View Graph");
 		JMenuItem miHistogram = new JMenuItem("Histogram");
 		JMenuItem miBoxPlot = new JMenuItem("Box Plot");
 		JMenuItem miScatterPlot = new JMenuItem("Histogram");
 
 		menuBarMAC.add(mProject);
 		mProject.add(miConditionalFormatting);
 		mProject.add(miBordercases);
 		mProject.add(miRules);
 		mProject.add(sproject);
 		mProject.add(miAddRow);
 		mProject.add(miAddMaxValues);
 		mProject.add(sproject2);
 		mProject.add(miFilter);
 		mProject.add(miViewStudent);
 		mProject.add(sproject3);
 		mProject.add(mGraph);
 		mGraph.add(miHistogram);
 		mGraph.add(miBoxPlot);
 		mGraph.add(miScatterPlot);
 		
 		//menu actions
 
 		// setup space constants
 		HOME_SPACE_LEFT_X = 3;
 		HOME_SPACE_Y = 41;
 		HOME_BOTTOM_SPACE_Y = 39;
 		HOME_SPACE_RIGHT_X = 6;
 
 		// create background gradient panel
 		backgroundPanel = new BackgroundGradientPanel(contentPane);
 		backgroundPanel.setSize(getWidth() - HOME_SPACE_RIGHT_X, getHeight() - HOME_SPACE_Y);
 		backgroundPanel.setBounds(HOME_SPACE_LEFT_X, 0, backgroundPanel.getWidth(), backgroundPanel.getHeight());
 		backgroundPanel.setLayout(null);
 		contentPane.add(backgroundPanel);
 	}
 
 	/*
 	 * Setup first screen
 	 */
 	public void setupHomeScreen() throws SqlJetException, IOException {
 
 		// create transparent panel to contain all menu icons
 		homePanel = new FadePanel(false, 200, 100);
 		homePanel.setBounds(0, 0, backgroundPanel.getWidth(), backgroundPanel.getHeight());
 		backgroundPanel.add(homePanel);
 		homePanel.setLayout(null);
 
 		// create glass pane to display blur on for file import and xport
 		blur = new BlurBackground(this);
 		this.setGlassPane(blur);
 		blur.setBounds(0, 0, getWidth(), getHeight());
 
 		// add title bars and recent docs container
 		containerSelectTask = new ReflectionImagePanel(ImageIO.read(getClass().getResource(
 				"/ClassAdminFrontEnd/resources/UPAdminHomeSelectTask.png")));
 		containerRecentDocs = new ReflectionImagePanel(ImageIO.read(getClass().getResource(
 				"/ClassAdminFrontEnd/resources/UPAdminHomeRecentDocs.png")));
 		recentDocsPanel = new FadePanel(false, 200, 200);
 
 		containerSelectTask.setBounds(117, 25, 953, 88);
 		containerRecentDocs.setBounds(117, 366, 953, 81);
 		recentDocsPanel.setBounds(160, containerRecentDocs.getHeight() + containerRecentDocs.getY() + 10, containerRecentDocs.getWidth(),
 				100);
 		recentDocsPanel.setLayout(null);
 
 		homePanel.add(containerSelectTask);
 		homePanel.add(containerRecentDocs);
 		homePanel.add(recentDocsPanel);
 
 		// create home buttons
 		homeImportButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/HomeImport.png")));
 		ButtonWorkspace = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/HomeWorkspace.png")));
 		homeStudents = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/HomeStudents.png")));
 		homeRapidAssessment = new ReflectionButton(ImageIO.read(getClass().getResource(
 				"/ClassAdminFrontEnd/resources/HomeRapidAssessment.png")));
 
 		homeImportButton.setBounds(163, 139, 200, 100);
 		ButtonWorkspace.setBounds(155, 235, 200, 100);
 		homeStudents.setBounds(554, 140, 180, 100);
 		homeRapidAssessment.setBounds(554, 235, 250, 100);
 
 		homePanel.add(homeImportButton);
 		homePanel.add(ButtonWorkspace);
 		homePanel.add(homeStudents);
 		homePanel.add(homeRapidAssessment);
 
 		// add listener to go to workspace screen
 		ButtonWorkspace.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				homeToWorkspaceTransition();
 			}
 		});
 
 		// add listener to for import dialog
 		homeImportButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				try {
 					openFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (BadLocationException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		homeRapidAssessment.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!homeRapidAssessment.isDisabled()) {
 
 				}
 
 			}
 		});
 
 
 		// fade in containers on program launch
 		recentDocsPanel.fadeIn();
 		homePanel.fadeIn();
 	}
 
 	/*
 	 * Setup main screen for working (with spreadsheets etc)
 	 */
 	private void setupWorkspaceScreen() throws IOException {
 		// create background pane for workspace screen
 		workspacePanel = new FadePanel(false, 200, 200);
 		workspacePanel.setBounds(0, 0, backgroundPanel.getWidth(), backgroundPanel.getHeight());
 		backgroundPanel.add(workspacePanel);
 		workspacePanel.setLayout(null);
 
 		// create navigation bar
 		navBar = new FadePanel(true, 800, 400);
 		navBar.setBounds(0, workspacePanel.getHeight() - 40 - 40, getWidth(), 80);
 		workspacePanel.add(navBar);
 		navBar.setLayout(null);
 
 		// create transparent panel on which info bubbles will be shown
 		infoPanel = new FadePanel(false, 200, 200);
 		infoPanel.setBounds(0, workspacePanel.getHeight() - 112, getWidth(), 43);
 		workspacePanel.add(infoPanel);
 		infoPanel.setLayout(null);
 		infoPanel.fadeIn();
 
 		// create search text field in right hand corner
 		searchPanel = new FadePanel(false, 200, 200);
 		searchPanel.setBounds(workspacePanel.getWidth() - 170, 10, 150, 30);
 		searchPanel.setLayout(null);
 		workspacePanel.add(searchPanel);
 
 		JTextField searchBox = new JTextField();
 		searchBox.setBounds(25, 5, 124, 25);
 		searchPanel.add(searchBox);
 
 		searchImage = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Search.png")));
 		searchImage.setBounds(0, 8, 30, 30);
 		searchPanel.add(searchImage);
 
 		searchPanel.fadeIn();
 
 		// create student panel on side
 		createStudentView();
 
 		// create buttons on navigation bar and add their respective mouse
 		// listeners
 		homeButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Home.png")));
 		importButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Import.png")));
 		exportButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Export.png")));
 		studentsButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Students.png")));
 		treeButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Tree.png")));
 		histogramButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Histogram.png")));
 		boxButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Box.png")));
 		scatterButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Scatter.png")));
 		conditionalFormatButton = new ReflectionButton(ImageIO.read(getClass().getResource(
 				"/ClassAdminFrontEnd/resources/ConditionalFormattingAdd.png")));
 		bordercaseButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/BordercaseAdd.png")));
 		addRowButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/AddRow.png")));
 		filterButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Filter.png")));
 		maxValButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/maxValue.png")));
 		rulesButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/Rules2.png")));
 
 		homeButton.setBounds(8, 8, 68, 80);
 		importButton.setBounds(75, 8, 68, 80);
 		exportButton.setBounds(135, 8, 68, 80);
 		studentsButton.setBounds(200, 8, 68, 80);
 		treeButton.setBounds(268, 11, 68, 80);
 		histogramButton.setBounds(335, 12, 68, 80);
 		boxButton.setBounds(405, 12, 68, 80);
 		scatterButton.setBounds(473, 12, 68, 80);
 		conditionalFormatButton.setBounds(537, 8, 68, 80);
 		bordercaseButton.setBounds(597, 8, 68, 80);
 		addRowButton.setBounds(657, 8, 68, 80);
 		filterButton.setBounds(715, 13, 68, 80);
 		maxValButton.setBounds(774, 11, 68, 80);
 		rulesButton.setBounds(830, 10, 68, 80);
 
 		navBar.add(homeButton);
 		navBar.add(importButton);
 		navBar.add(exportButton);
 		navBar.add(studentsButton);
 		navBar.add(treeButton);
 		navBar.add(histogramButton);
 		navBar.add(boxButton);
 		navBar.add(scatterButton);
 		navBar.add(conditionalFormatButton);
 		navBar.add(bordercaseButton);
 		navBar.add(addRowButton);
 		navBar.add(filterButton);
 		navBar.add(maxValButton);
 		navBar.add(rulesButton);
 
 		setNavButtonsDisabled();
 
 		// create info bubbles panel
 		homeInfoPanel = new FadePanel(false, 200, 200);
 		importInfoPanel = new FadePanel(false, 200, 200);
 		exportInfoPanel = new FadePanel(false, 200, 200);
 		studentsInfoPanel = new FadePanel(false, 200, 200);
 		buildInfoPanel = new FadePanel(false, 200, 200); 
 		histogramInfoPanel = new FadePanel(false, 200, 200);
 		boxplotInfoPanel = new FadePanel(false, 200, 200);
 		scatterplotInfoPanel = new FadePanel(false, 200, 200);
 		conditionalFormattingInfoPanel = new FadePanel(false, 200, 200);
 		bordercaseInfoPanel = new FadePanel(false, 200, 200);
 		addRowInfoPanel = new FadePanel(false, 200, 200);
 		filterInfoPanel = new FadePanel(false, 200, 200);
 		maxValInfoPanel = new FadePanel(false, 200, 200);
 		rulesInfoPanel = new FadePanel(false, 200, 200);
 		buildInfoPanel = new FadePanel(false, 200, 200);
 
 		homeInfoPanel.setBounds(8, 0, 62, infoPanel.getHeight());
 		importInfoPanel.setBounds(67, 0, 62, infoPanel.getHeight());
 		exportInfoPanel.setBounds(135, 0, 62, infoPanel.getHeight());
 		studentsInfoPanel.setBounds(175, 0, 125, infoPanel.getHeight());
 		buildInfoPanel.setBounds(232, 0, 140, infoPanel.getHeight());
 		histogramInfoPanel.setBounds(315, 0, 125, infoPanel.getHeight());
 		boxplotInfoPanel.setBounds(400, 0, 125, infoPanel.getHeight());
 		scatterplotInfoPanel.setBounds(457, 0, 125, infoPanel.getHeight());
 		conditionalFormattingInfoPanel.setBounds(504, 0, 129, infoPanel.getHeight());
 		bordercaseInfoPanel.setBounds(560, 0, 129, infoPanel.getHeight());
 		addRowInfoPanel.setBounds(650, 0, 129, infoPanel.getHeight());
 		filterInfoPanel.setBounds(697, 0, 129, infoPanel.getHeight());
 		maxValInfoPanel.setBounds(738, 0, 129, infoPanel.getHeight());
 		rulesInfoPanel.setBounds(820, 0, 129, infoPanel.getHeight());
 
 		homeInfoPanel.setLayout(null);
 		importInfoPanel.setLayout(null);
 		exportInfoPanel.setLayout(null);
 		studentsInfoPanel.setLayout(null);
 		buildInfoPanel.setLayout(null);
 		histogramInfoPanel.setLayout(null);
 		boxplotInfoPanel.setLayout(null);
 		scatterplotInfoPanel.setLayout(null);
 		conditionalFormattingInfoPanel.setLayout(null);
 		bordercaseInfoPanel.setLayout(null);
 		addRowInfoPanel.setLayout(null);
 		filterInfoPanel.setLayout(null);
 		maxValInfoPanel.setLayout(null);
 		rulesInfoPanel.setLayout(null);
 
 		infoPanel.add(homeInfoPanel);
 		infoPanel.add(importInfoPanel);
 		infoPanel.add(exportInfoPanel);
 		infoPanel.add(studentsInfoPanel);
 		infoPanel.add(buildInfoPanel);
 		infoPanel.add(histogramInfoPanel);
 		infoPanel.add(boxplotInfoPanel);
 		infoPanel.add(scatterplotInfoPanel);
 		infoPanel.add(conditionalFormattingInfoPanel);
 		infoPanel.add(bordercaseInfoPanel);
 		infoPanel.add(addRowInfoPanel);
 		infoPanel.add(filterInfoPanel);
 		infoPanel.add(maxValInfoPanel);
 		infoPanel.add(rulesInfoPanel);
 
 		// create info bubble image
 		ImagePanel infoBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/HomeInfo.png")));
 		ImagePanel importBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/ImportInfo.png")));
 		ImagePanel exportBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/ExportInfo.png")));
 		ImagePanel studentsBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/StudentsInfo.png")));
 		ImagePanel buildBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/InfoStructureModule.png")));
 		ImagePanel histogramBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/InfoHistogram.png")));
 		ImagePanel boxplotBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/InfoBoxPlot.png")));
 		ImagePanel scatterplotBubble = new ImagePanel(ImageIO.read(getClass().getResource(
 				"/ClassAdminFrontEnd/resources/InfoScatterPlot.png")));
 		ImagePanel conditionalFormattingBubble = new ImagePanel(ImageIO.read(getClass().getResource(
 				"/ClassAdminFrontEnd/resources/InfoconditionalFormatting.png")));
 		ImagePanel bordercaseBubble = new ImagePanel(ImageIO.read(getClass()
 				.getResource("/ClassAdminFrontEnd/resources/InfoBordercase.png")));
 		ImagePanel addRowBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/InfoAddRow.png")));
 		ImagePanel filterBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/InfoAddFilter.png")));
 		ImagePanel maxValBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/InfoAddMaxValues.png")));
 		ImagePanel rulesBubble = new ImagePanel(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/InfoAddRule.png")));
 
 		infoBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		importBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		exportBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		studentsBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		buildBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		histogramBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		boxplotBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		scatterplotBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		conditionalFormattingBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		bordercaseBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		addRowBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		filterBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		maxValBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 		rulesBubble.setBounds(0, 0, infoPanel.getWidth(), infoPanel.getHeight());
 
 		infoBubble.setLayout(null);
 		importBubble.setLayout(null);
 		exportBubble.setLayout(null);
 		studentsBubble.setLayout(null);
 		buildBubble.setLayout(null);
 		histogramBubble.setLayout(null);
 		boxplotBubble.setLayout(null);
 		scatterplotBubble.setLayout(null);
 		conditionalFormattingBubble.setLayout(null);
 		bordercaseBubble.setLayout(null);
 		addRowBubble.setLayout(null);
 		filterBubble.setLayout(null);
 		rulesBubble.setLayout(null);
 		maxValBubble.setLayout(null);
 
 		homeInfoPanel.add(infoBubble);
 		importInfoPanel.add(importBubble);
 		exportInfoPanel.add(exportBubble);
 		studentsInfoPanel.add(studentsBubble);
 		buildInfoPanel.add(buildBubble);
 		histogramInfoPanel.add(histogramBubble);
 		boxplotInfoPanel.add(boxplotBubble);
 		scatterplotInfoPanel.add(scatterplotBubble);
 		conditionalFormattingInfoPanel.add(conditionalFormattingBubble);
 		bordercaseInfoPanel.add(bordercaseBubble);
 		addRowInfoPanel.add(addRowBubble);
 		filterInfoPanel.add(filterBubble);
 		maxValInfoPanel.add(maxValBubble);
 		rulesInfoPanel.add(rulesBubble);
 
 		homeButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				try {
 					workspaceToHomeTransition();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				homeInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				homeInfoPanel.fadeOut();
 			}
 		});
 
 		importButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				try {
 					openFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (BadLocationException e) {
 					e.printStackTrace();
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				importInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				importInfoPanel.fadeOut();
 			}
 		});
 
 		exportButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				try {
 					saveFileAs();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				exportInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				exportInfoPanel.fadeOut();
 			}
 		});
 
 		studentsButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!studentsButton.isDisabled()) {
 					table.getTable().getSelectedRow();
 					TreeView.createStudentFrm("name",table.getData().get(table.getTable().getSelectedRow()).get(0));
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				studentsInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				studentsInfoPanel.fadeOut();
 			}
 		});
		
		treeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				if (!treeButton.isDisabled()) {
					TreeView.createEntityTypeFrm("name", Global.getGlobal().getActiveProject());					
				}
			}

			public void mouseEntered(MouseEvent arg0) {
				
			}

			public void mouseExited(MouseEvent arg0) {
				
			}
		});
 
 		treeButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!treeButton.isDisabled()) {
 					table.getTable().getSelectedRow();
 
 					// kom by build tree btn
 					TreeView.createEntityTypeFrm("name", Global.getGlobal().getActiveProject());
 					// moet hier bly
 					// TreeView.createStudentFrm("name",
 					// table.getData().get(table.getTable().getSelectedRow()).get(0));
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				buildInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				buildInfoPanel.fadeOut();
 			}
 		});
 		
 		histogramButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!histogramButton.isDisabled()) {
 					HistogramFrame x = new HistogramFrame(Global.getGlobal().getActiveProject());
 					Global.getGlobal().getActiveProject().addhistogramcharts(x);
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				histogramInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				histogramInfoPanel.fadeOut();
 			}
 		});
 
 		boxButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!boxButton.isDisabled()) {
 					BoxPlotFrame x = new BoxPlotFrame();
 					x.createBoxPlotFrame();
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				boxplotInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				boxplotInfoPanel.fadeOut();
 			}
 		});
 
 		scatterButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!scatterButton.isDisabled()) {
 					ScatterPlotFrame x = new ScatterPlotFrame(Global.getGlobal().getActiveProject());// project);
 					Global.getGlobal().getActiveProject().addscattercharts(x);
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				scatterplotInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				scatterplotInfoPanel.fadeOut();
 			}
 		});
 
 		conditionalFormatButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!conditionalFormatButton.isDisabled()) {
 					ConditionalFormattingFrame conditionalformatFrame = new ConditionalFormattingFrame(table);
 					conditionalformatFrame.setVisible(true);
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				conditionalFormattingInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				conditionalFormattingInfoPanel.fadeOut();
 			}
 		});
 
 		bordercaseButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!bordercaseButton.isDisabled()) {
 					BordercaseFrame bordercaseFrame = new BordercaseFrame(table);
 					bordercaseFrame.setVisible(true);
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				bordercaseInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				bordercaseInfoPanel.fadeOut();
 			}
 		});
 
 		addRowButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!addRowButton.isDisabled()) {
 					EntityType testHead = table.project.getHeadEntityType();
 					LinkedList<EntityType> list = testHead.getSubEntityType();
 
 					for (int x = 0; x < list.size(); x++) {
 						table.createEntities(list.get(x), new SuperEntityPointer(table.project.getHead()));
 					}
 
 					table.data = table.project.getHead().getDataLinkedList();
 
 					/*
 					 * tableModel.addRow(new Object[] { txtField1.getText(),
 					 * txtField1.getText() });
 					 */
 
 					Object[] temp = new Object[table.data.get(0).size()];
 
 					for (int y = 0; y < table.data.get(0).size(); y++) {
 						temp[y] = table.data.getLast().get(y).getValue();
 					}
 
 					table.tableModel.addRow(temp);
 					table.repaint();
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				addRowInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				addRowInfoPanel.fadeOut();
 			}
 		});
 
 		filterButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!filterButton.isDisabled()) {
 					FilterFrame filterframe = new FilterFrame(table);
 					filterframe.setVisible(true);
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				filterInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				filterInfoPanel.fadeOut();
 			}
 		});
 
 		maxValButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				if (!maxValButton.isDisabled()) {
 					SetMaxValueFrame maxframe = new SetMaxValueFrame(table);
 					maxframe.setVisible(true);
 				}
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				maxValInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				maxValInfoPanel.fadeOut();
 			}
 		});
 
 		rulesButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				frmRule rules = new frmRule(Global.getGlobal().getActiveProject());
 				rules.setVisible(true);
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				rulesInfoPanel.fadeIn();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				rulesInfoPanel.fadeOut();
 			}
 		});
 
 	}
 
 	/*
 	 * Method with handles file import actions, blur background
 	 */
 	public void openFile() throws IOException, BadLocationException {
 
 		File file;
 		// set the file extentions that may be chosen
 		// FileFilter fileFilter = new
 		// FileNameExtensionFilter("Supported files types: pdat, csv", "pdat",
 		// "csv");
 
 		blur.fadeIn();
 		// Create a file chooser
 		filechooser = new JFileChooser();
 		if (currentFilePath != null) {
 			filechooser.setCurrentDirectory(currentFilePath);
 		}
 
 		// add the filter to the file chooser
 		// filechooser.addChoosableFileFilter(fileFilter);
 
 		// shows the dialog, return value specifies file
 		int returnVal = filechooser.showOpenDialog(this);
 
 		// if the chosen file is valid
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			file = filechooser.getSelectedFile();
 			currentFilePath = filechooser.getSelectedFile();
 			blur.fadeOut();
 			createTab(file);
 			homeToWorkspaceTransition();
 			tabBar.fadeIn();
 
 			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 			Date date = new Date();
 			if (db != null) {
 				if (db.alreadyContains(file.getName(), file.getAbsolutePath())) {
 					db.updateRecentDocument(file.getName(), file.getAbsolutePath(), dateFormat.format(date));
 				} else {
 					db.addRecentDoc(file.getName(), file.getAbsolutePath(), dateFormat.format(date));
 				}
 			}
 		} else {
 			blur.fadeOut();
 		}
 	}
 
 	/*
 	 * Method with handles file import actions from the recent documents button
 	 */
 	public void openRecentFile(File _file) throws IOException, BadLocationException {
 
 		File file = _file;
 
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		Date date = new Date();
 		if (db != null) {
 			if (db.alreadyContains(file.getName(), file.getAbsolutePath())) {
 				db.updateRecentDocument(file.getName(), file.getAbsolutePath(), dateFormat.format(date));
 			} else {
 				db.addRecentDoc(file.getName(), file.getAbsolutePath(), dateFormat.format(date));
 			}
 		}
 
 		createTab(file);
 		homeToWorkspaceTransition();
 		tabBar.fadeIn();
 
 	}
 
 	/*
 	 * Method which handles file export
 	 */
 	public void saveFileAs() throws IOException {
 
 		File file;
 		// set the file extentions that may be chosen
 		FileFilter filter = new FileNameExtensionFilter("Supported files types: pdat, csv", "pdat", "csv");
 
 		// Create a file chooser
 		final JFileChooser filechooser = new JFileChooser();
 		// remove the "All Files" type
 		// filechooser.setAcceptAllFileFilterUsed(false);
 		// add the filter to the file chooser
 		filechooser.addChoosableFileFilter(filter);
 
 		// shows the dialog, return value specifies file
 		int returnVal = filechooser.showSaveDialog(this);
 
 		// if the chosen file is valid
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			file = filechooser.getSelectedFile();
 			try {
 				FileHandler.get().saveFile(file.getAbsolutePath(), Global.getGlobal().getActiveProject());
 			} catch (UnsupportedFileTypeException e) {
 				e.printStackTrace();
 			}
 		} else {
 
 		}
 	}
 
 	/*
 	 * function to determine OS that is currently running
 	 */
 	public static void determineOS() {
 		currentOs = System.getProperty("os.name").toUpperCase();
 		if (currentOs.contains("MAC")) {
 			currentOs = MAC_OS;
 		} else if (currentOs.contains("WINDOWS")) {
 			currentOs = WIN_OS;
 		} else {
 			currentOs = null;
 		}
 	}
 
 	/*
 	 * create a new Tab when a new file is imported
 	 */
 	public void createTab(File file) {
 
 		// set selected index to new file opened
 		// tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
 		setNavButtonsEnabled();
 
 		studentPanel.moveIn();
 
 		// set selected table
 		// table = Global.getGlobal().getActiveProject().getTables().get(0);
 
 		try {
 			Global.getGlobal().addProject(new Project());
 			fileHandler.openFile(file.getAbsolutePath(), Global.getGlobal().getActiveProject());
 		} catch (UnsupportedFileTypeException e) {
 			e.printStackTrace();
 		}
 		// create table on panel
 
 		table = new FrmTable(Global.getGlobal().getActiveProject().getHead().getHeaders(), Global.getGlobal().getActiveProject().getHead()
 				.getDataLinkedList(), Global.getGlobal().getActiveProject());
 
 		// listener for changes of selection in table
 		table.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				showStudent();
 			}
 		});
 
 		// create tabbedPane
 		if (tabbedPane == null) {
 			tabbedPane = new JTabbedPane();
 			tabbedPane.setBounds(20, 20, workspacePanel.getWidth() - 40, workspacePanel.getHeight() - 40 - navBar.getHeight());
 
 			tabbedPane.addChangeListener(new ChangeListener() {
 				// This method is called whenever the selected tab changes
 				@Override
 				public void stateChanged(ChangeEvent arg0) {
 					Global.getGlobal().setActiveProjectIndex(tabbedPane.getSelectedIndex() + 1);
 					if(Global.getGlobal().getActiveProject().getTables().size() > 0)
 					table = Global.getGlobal().getActiveProject().getTables().get(0);
 				}
 			});
 		}
 
 		// create panel on which tabbedPane will be
 		if (tabBar == null) {
 			tabBar = new FadePanel(false, 800, 400);
 			tabBar.setBounds(0, 0, frame.getWidth(), frame.getHeight());
 			tabBar.setLayout(null);
 		}
 		tabBar.add(tabbedPane);
 
 		workspacePanel.add(tabBar);
 
 		// put panel with table on a new tab
 		tabbedPane.addTab(file.getName(), table);
 
 		tabCount++;
 		tabbedPane.setTabComponentAt(tabCount, new TabButton(file.getName()));
 		// tabbedPane.add(table, tabCount);
 	}
 
 	/*
 	 * Function to simulate transitions from home screen to workspace screen
 	 */
 	public void homeToWorkspaceTransition() {
 		homePanel.fadeOut();
 		workspacePanel.fadeIn();
 		navBar.fadeIn();
 		frame.remove(blur);
 		recentDocsPanel.fadeOut();
 	}
 
 	/*
 	 * Function to simulate transitions from workspace screen to home screen
 	 */
 	public void workspaceToHomeTransition() throws IOException {
 		homePanel.fadeIn();
 		workspacePanel.fadeOut();
 		navBar.fadeOut();
 
 		if (studentPanel != null) {
 			studentPanel.setVisible(false);
 		}
 
 		recentDocsPanel.fadeIn();
 		createRecentDocsView();
 
 	}
 
 	/*
 	 * Method to create student side panel on right side of workspace panel
 	 */
 	public void createStudentView() {
 
 		studentPanel = new ShadowPanel(getWidth() - 45, 45, getWidth() - 250, 45);
 		studentPanel.setBounds(getWidth(), 40, 250, getHeight() - 20);
 		try {
 
 			studentsViewArrowOut = new MenuImagePanel(ImageIO.read(getClass().getResource(
 					"/ClassAdminFrontEnd/resources/studentsViewArrowOut.png")));
 			studentsViewArrowIn = new MenuImagePanel(ImageIO.read(getClass().getResource(
 					"/ClassAdminFrontEnd/resources/studentsViewArrowIn.png")));
 
 			studentsViewArrowOut.setBounds(3, 3, 20, 20);
 			studentsViewArrowIn.setBounds(3, 3, 20, 20);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		studentPanel.add(studentsViewArrowOut);
 		studentPanel.add(studentsViewArrowIn);
 
 		studentPanel.setLayout(null);
 
 		studentPanel.setShown(false);
 		studentsViewArrowIn.setVisible(false);
 
 		backgroundPanel.setLayer(studentPanel, 300);
 		backgroundPanel.add(studentPanel);
 		studentPanel.setVisible(false);
 
 		studentsViewArrowOut.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				studentPanel.moveOut();
 				studentsViewArrowOut.setVisible(false);
 				studentsViewArrowIn.setVisible(true);
 			}
 		});
 
 		studentsViewArrowIn.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				studentPanel.moveIn();
 				studentsViewArrowIn.setVisible(false);
 				studentsViewArrowOut.setVisible(true);
 			}
 		});
 	}
 
 	/*
 	 * Handles image on studentPanel
 	 */
 	public void showStudent() {
 		/*
 		 * String[] headers = Global.getGlobal().getActiveProject().getHead()
 		 * .getHeaders(); for (int i = 0; i < headers.length; i++) { //
 		 * System.out.println(headers[i]); }
 		 * 
 		 * int row = table.getTable().getSelectedRow(); int colCount =
 		 * table.getTable().getColumnCount();
 		 * 
 		 * String[] info = new String[colCount];
 		 * 
 		 * for (int i = 0; i < colCount; i++) { info[i] =
 		 * table.getTable().getValueAt(row, i).toString();
 		 * System.out.println(info[i]); }
 		 */
 		try {
 			BufferedImage photo = (ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/StudentPhotos/10092120.JPG")));
 			photo = Scalr.resize(photo, 150);
 			studentPhoto = new ImagePanel(photo);
 			studentPanel.add(studentPhoto);
 			studentPhoto.setBounds(57, 50, photo.getWidth(), photo.getHeight());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void createRecentDocsDB() throws SqlJetException {
 		db = new Database();
 		db.openDatabase();
 
 	}
 
 	public void createRecentDocsView() throws IOException {
 
 		recentDocsPanel.removeAll();
 
 		ReflectionButtonWithLabel[] buttonArray = new ReflectionButtonWithLabel[10];
 		String[] filenamesarray = db.getDocumentNames();
 		String[] filepathsarray = db.getDocumentPaths();
 		int count = db.getDocumentCount();
 
 		int i = 0;
 		int m = 0;
 		
 		//restrict recent docs to 7
 		while ((i < count) && (m < 7))  {
 
 			BufferedImage icon = null;
 
 			if (filenamesarray[i].endsWith("csv")) {
 				icon = ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/csvsmall.png"));
 			} else if (filenamesarray[i].endsWith("pdat")) {
 				icon = ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/pdatsmall.png"));
 			} else {
 				icon = ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/xlssmall.png"));
 			}
 
 			buttonArray[i] = new ReflectionButtonWithLabel(icon, filenamesarray[i], new Color(0xD6D6D6), new Color(0xFAFAFA),
 					filepathsarray[i]);
 			buttonArray[i].setBounds(8 + (80 * i), 8, 68, 95);
 
 			buttonArray[i].addActionListener(this);
 
 			buttonArray[i].setToolTipText(filepathsarray[i]);
 
 			recentDocsPanel.add(buttonArray[i]);
 
 			i++;
 			m++;
 		}
 	}
 
 	/*
 	 * Enables buttons that has been disabled because of no spreadsheet
 	 */
 	public void setNavButtonsEnabled() {
 		studentsButton.setEnabled();
 		histogramButton.setEnabled();
 		boxButton.setEnabled();
 		scatterButton.setEnabled();
 		exportButton.setEnabled();
 		conditionalFormatButton.setEnabled();
 		bordercaseButton.setEnabled();
 		addRowButton.setEnabled();
 		filterButton.setEnabled();
 		maxValButton.setEnabled();
 		rulesButton.setEnabled();
 		treeButton.setEnabled();
 
 		searchPanel.fadeIn();
 	}
 
 	/*
 	 * Disables buttons which cannot be used without a spreadsheet (export,
 	 * students, graphs...)
 	 */
 	public void setNavButtonsDisabled() {
 		if (studentsButton != null) {
 			studentsButton.setDisabled();
 		}
 		if (histogramButton != null) {
 			histogramButton.setDisabled();
 		}
 		if (boxButton != null) {
 			boxButton.setDisabled();
 		}
 		if (scatterButton != null) {
 			scatterButton.setDisabled();
 		}
 		if (exportButton != null) {
 			exportButton.setDisabled();
 		}
 		if (conditionalFormatButton != null) {
 			conditionalFormatButton.setDisabled();
 		}
 		if (bordercaseButton != null) {
 			bordercaseButton.setDisabled();
 		}
 		if (addRowButton != null) {
 			addRowButton.setDisabled();
 		}
 		if (filterButton != null) {
 			filterButton.setDisabled();
 		}
 		if (maxValButton != null) {
 			maxValButton.setDisabled();
 		}
 		if (rulesButton != null) {
 			rulesButton.setDisabled();
 		}
 		if (treeButton != null) {
 			treeButton.setDisabled();
 		}
 		if (searchPanel != null) {
 			searchPanel.fadeOut();
 		}
 		if (studentPanel != null) {
 			studentPanel.setVisible(false);
 		}
 
 	}
 
 	// action for recent docs buttons
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() instanceof ReflectionButtonWithLabel) {
 			File testFile = new File(((ReflectionButtonWithLabel) e.getSource()).getPath());
 			if (testFile.exists()) {
 				try {
 					openRecentFile(testFile);
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				} catch (BadLocationException e1) {
 					e1.printStackTrace();
 				}
 			} else {
 				recentDocsPanel.remove(((ReflectionButtonWithLabel) e.getSource()));
 				db.deleteRecentDocuments(((ReflectionButtonWithLabel) e.getSource()).getPath());
 
 				JOptionPane.showMessageDialog(frame, "File seems to be missing from last directory location, removing shortcut.",
 						"File Missing", JOptionPane.ERROR_MESSAGE);
 
 				recentDocsPanel.revalidate();
 				recentDocsPanel.repaint();
 			}
 		}
 
 	}
 
 }
