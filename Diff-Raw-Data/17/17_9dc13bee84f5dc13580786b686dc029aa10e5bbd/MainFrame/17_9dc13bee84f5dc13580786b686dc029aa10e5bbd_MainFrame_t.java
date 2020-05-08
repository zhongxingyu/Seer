 package View;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.Image;
 import java.awt.Toolkit;
import java.awt.event.ItemListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 import net.miginfocom.swing.MigLayout;
 import Controller.AdvancedParametersListener;
 import Controller.EditAirportListener;
 import Controller.EditObstacleListener;
 import Controller.EditRunwayListener;
 import Controller.ExitListener;
 import Controller.NewAirportListener;
 import Controller.NewObstacleListener;
 import Controller.OpenAirportListener;
 import Controller.OpenObstacleListener;
 import Controller.OpenRecentAirportListener;
 import Controller.OpenRecentObstacleListener;
 import Controller.PrintCalculationsListener;
 import Controller.RemoveObstacleListener;
 import Controller.SaveAirportListener;
 import Controller.SaveObstacleListener;
 import Controller.SelectPhysicalRunwayListener;
 import Controller.SelectRunwayListener;
 import Controller.SelectViewListener;
 import Controller.ShowAboutListener;
 import Controller.ShowAddressBookListener;
 import Controller.ShowEmailDialogListener;
 import Controller.ShowHelpListener;
 import Model.AddressBook;
 import Model.Airport;
 import Model.AirportObserver;
 import Model.LoadXMLFile;
 import Model.Obstacle;
 import Model.PhysicalRunway;
 import Model.Runway;
 
 
 @SuppressWarnings("serial")
 public class MainFrame extends JFrame implements AirportObserver{
 
 	private static final int MAX_HISTORY = 10;
 	private static final int HISTORY_TO_SHOW = 5;
 	
 	private JPanel contentPane;
 	private JSplitPane rightSplitPane;
 	private JTable OriginalParametersTable;
 	private JTable RedeclaredParametersTable;
 	private JTable ObstacleDetailsTable;
 	private JTable AdvancedParametersTable;
 	private JLabel lblAirportName;
 	private JComboBox currentRunwayCombo;
 	private final ButtonGroup topPanelButtonGroup = new ButtonGroup();
 	private final ButtonGroup bottomPanelButtonGroup = new ButtonGroup();
 	private final ButtonGroup physicalRunwayButtonGroup = new ButtonGroup();
 
 	TopView topTopView;
 
 	private Airport airport;
 	private AddressBook addressBook;
 
 	private static List<String> recentAirports;
 	private static List<String> recentObstacles;
 
 	private Vector<AirportObserver> airportObservers;
 	private JMenu physicalRunwayMenu;
 
 	private JMenu mnOpenRecentAirports;
 	private JMenu mnOpenRecentObstacles;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainFrame frame = new MainFrame();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public MainFrame() {
 		this.setIconImage(Toolkit.getDefaultToolkit().getImage("data/Icons/AirplaneIcon.png"));
 		
 		addressBook = new AddressBook();
 		LoadXMLFile lf = new LoadXMLFile();
 		if(!addressBook.addContacts(lf.silentLoadContacts("data/AddressBook.xml"))) System.out.println("Contact Load failed");
 
 		rightSplitPane = new JSplitPane();
 
 		airport = new Airport("");
 		airportObservers = new Vector<AirportObserver>();
 		airportObservers.add(this);
 
 		setTitle("SEG Group 9 - Runway Redeclaration System");
 
 		try {
 			UIManager.setLookAndFeel(
 					UIManager.getSystemLookAndFeelClassName());
 		} catch (UnsupportedLookAndFeelException ex) {
 			System.out.println("Unable to load native look and feel");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 
 
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		setBounds(100, 100, 800, 645);
 
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 
 		JMenu mnFile = new JMenu("File");
 		mnFile.setMnemonic('f');
 		menuBar.add(mnFile);
 
 		JMenu mnAirport = new JMenu("New");
 		mnAirport.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/NewIcon.png")));
 		mnAirport.setMnemonic('n');
 		mnFile.add(mnAirport);
 
 		JMenuItem mntmNewMenuItem = new JMenuItem("Airport");
 		mntmNewMenuItem.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/AirplaneIcon.png")));
 		mntmNewMenuItem.setMnemonic('a');
 		NewAirportListener nal = new NewAirportListener(airport, airportObservers);
 		mntmNewMenuItem.addActionListener(nal);
 		airportObservers.add(nal);
 		mnAirport.add(mntmNewMenuItem);
 
 		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Obstacle");
 		mntmNewMenuItem_1.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/ObstacleIcon.png")));
 		mntmNewMenuItem_1.setMnemonic('o');
 		NewObstacleListener nol = new NewObstacleListener(airport, airportObservers);
 		mntmNewMenuItem_1.addActionListener(nol);
 		airportObservers.add(nol);
 		mnAirport.add(mntmNewMenuItem_1);
 
 		JMenu mnNewMenu = new JMenu("Open");
 		mnNewMenu.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/OpenIcon.png")));
 		mnNewMenu.setMnemonic('o');
 		mnFile.add(mnNewMenu);
 
 		JMenuItem mntmAirport = new JMenuItem("Airport");
 		mntmAirport.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/AirplaneIcon.png")));
 		mntmAirport.setMnemonic('a');
 		OpenAirportListener oal = new OpenAirportListener(airport, airportObservers);
 		mntmAirport.addActionListener(oal);
 		airportObservers.add(oal);
 		mnNewMenu.add(mntmAirport);
 
 		JMenuItem mntmObstacle = new JMenuItem("Obstacle");
 		mntmObstacle.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/ObstacleIcon.png")));
 		mntmObstacle.setMnemonic('o');
 		OpenObstacleListener ool = new OpenObstacleListener(airport, airportObservers);
 		mntmObstacle.addActionListener(ool);
 		airportObservers.add(ool);
 		mnNewMenu.add(mntmObstacle);
 
 		JMenu mnOpenRecent = new JMenu("Open Recent");
 		mnOpenRecent.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/OpenIcon.png")));
 		mnOpenRecent.setMnemonic('r');
 		mnFile.add(mnOpenRecent);
 
 		//Reads in persistent recent files
 		try {
 			this.loadRecentFiles(HISTORY_TO_SHOW);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		mnOpenRecentAirports = new JMenu("Airport");
 		mnOpenRecentAirports.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/AirplaneIcon.png")));
 		mnOpenRecentAirports.setMnemonic('a');
 		mnOpenRecent.add(mnOpenRecentAirports);
 
 		populateRecentAirports();
 
 		mnOpenRecentObstacles = new JMenu("Obstacle");
 		mnOpenRecentObstacles.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/ObstacleIcon.png")));
 		mnOpenRecentObstacles.setMnemonic('o');
 		mnOpenRecent.add(mnOpenRecentObstacles);
 
 		populateRecentObstacles();
 
 
 		JMenu mnSave = new JMenu("Save");
 		mnSave.setMnemonic('s');
 		mnSave.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/SaveIcon.png")));
 		mnFile.add(mnSave);
 
 		JMenuItem mntmNewMenuItem_4 = new JMenuItem("Airport");
 		mntmNewMenuItem_4.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/AirplaneIcon.png")));
 		mntmNewMenuItem_4.setMnemonic('a');
 		SaveAirportListener sal = new SaveAirportListener(airport, airportObservers);
 		mntmNewMenuItem_4.addActionListener(sal);
 		airportObservers.add(sal);
 		mnSave.add(mntmNewMenuItem_4);
 
 		JMenuItem mntmNewMenuItem_5 = new JMenuItem("Obstacle");
 		mntmNewMenuItem_5.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/ObstacleIcon.png")));
 		mntmNewMenuItem_5.setMnemonic('o');
 		SaveObstacleListener sol = new SaveObstacleListener(airport);
 		mntmNewMenuItem_5.addActionListener(sol);
 		airportObservers.add(sol);
 		mnSave.add(mntmNewMenuItem_5);
 
 		JSeparator separator_1 = new JSeparator();
 		mnFile.add(separator_1);
 
 		ExitListener exitListener = new ExitListener(airport);
 		JMenuItem mntmExit = new JMenuItem("Exit");
 		mntmExit.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/ExitIcon.png")));
 		mntmExit.setMnemonic('x');
 		mntmExit.addActionListener(exitListener);
 		mnFile.add(mntmExit);
 		airportObservers.add(exitListener);
 
 		addWindowListener(exitListener);
 
 		JMenu mnEdit = new JMenu("Edit");
 		mnEdit.setMnemonic('e');
 		menuBar.add(mnEdit);
 
 		JMenuItem mntmNewMenuItem_6 = new JMenuItem("Physical Runway");
 		mntmNewMenuItem_6.setMnemonic('r');
 		EditRunwayListener erl = new EditRunwayListener(airport, airportObservers);
 		mntmNewMenuItem_6.addActionListener(erl);
 		mnEdit.add(mntmNewMenuItem_6);
 		airportObservers.add(erl);
 
 		JMenuItem mntmAirport_1 = new JMenuItem("Airport");
 		mntmAirport_1.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/AirplaneIcon.png")));
 		mntmAirport_1.setMnemonic('a');
 		EditAirportListener eal = new EditAirportListener(airport, airportObservers);
 		airportObservers.add(eal);
 		mntmAirport_1.addActionListener(eal);
 		mnEdit.add(mntmAirport_1);
 
 		JMenuItem mntmNewMenuItem_7 = new JMenuItem("Obstacle");
 		mntmNewMenuItem_7.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/ObstacleIcon.png")));
 		mntmNewMenuItem_7.setMnemonic('o');
 		EditObstacleListener eol = new EditObstacleListener(airport, airportObservers);
 		mntmNewMenuItem_7.addActionListener(eol);
 		airportObservers.add(eol);
 		mnEdit.add(mntmNewMenuItem_7);
 
 		JSeparator separator = new JSeparator();
 		mnEdit.add(separator);
 
 		physicalRunwayMenu = new JMenu("Select Physical Runway");
 		physicalRunwayMenu.setMnemonic('s');
 		mnEdit.add(physicalRunwayMenu);
 
 		generatePhysicalRunwayRadioButtons(physicalRunwayMenu);
 
 		JMenuItem mntmPositionObstacle = new JMenuItem("Advanced Parameters");
 		mntmPositionObstacle.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/AdvancedIcon.png")));
 		mntmPositionObstacle.setMnemonic('p');
 		AdvancedParametersListener opl = new AdvancedParametersListener(airport, airportObservers);
 		mntmPositionObstacle.addActionListener(opl);
 		airportObservers.add(opl);
 
 		JMenuItem mntmRemoveObstacle = new JMenuItem("Remove Obstacle");
 		mntmRemoveObstacle.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/RemoveObstacleIcon.png")));
 		RemoveObstacleListener rol = new RemoveObstacleListener(airport, airportObservers);
 		mntmRemoveObstacle.addActionListener(rol);
 		airportObservers.add(rol);
 		mntmRemoveObstacle.setMnemonic('m');
 		mnEdit.add(mntmRemoveObstacle);
 		mnEdit.add(mntmPositionObstacle);
 
 		JMenu mnView = new JMenu("View");
 		mnView.setMnemonic('v');
 		menuBar.add(mnView);
 
 		JMenu mnTopPanel = new JMenu("Top Panel");
 		mnTopPanel.setMnemonic('t');
 		mnView.add(mnTopPanel);
 
 		JRadioButtonMenuItem rdbtnmntmTopPanelTopView = new JRadioButtonMenuItem("Top View");
 		topPanelButtonGroup.add(rdbtnmntmTopPanelTopView);
 		mnTopPanel.add(rdbtnmntmTopPanelTopView);
 		topTopView = new TopView(airport);
 		rdbtnmntmTopPanelTopView.addActionListener(new SelectViewListener(rightSplitPane, topTopView, true));
 		rightSplitPane.setLeftComponent(topTopView);
 		airportObservers.add(topTopView);
 
 
 		JRadioButtonMenuItem rdbtnmntmTopPanelSideView = new JRadioButtonMenuItem("Side View");
 		topPanelButtonGroup.add(rdbtnmntmTopPanelSideView);
 		mnTopPanel.add(rdbtnmntmTopPanelSideView);
 		SideView topSideView = new SideView(airport); 
 		rdbtnmntmTopPanelSideView.addActionListener(new SelectViewListener(rightSplitPane, topSideView, true));
 		airportObservers.add(topSideView);
 
 		JRadioButtonMenuItem rdbtnmntmTopPanelCalculations = new JRadioButtonMenuItem("Calculations");
 		topPanelButtonGroup.add(rdbtnmntmTopPanelCalculations);
 		mnTopPanel.add(rdbtnmntmTopPanelCalculations);
 		CalculationsView topCalcView = new CalculationsView(airport);
 		rdbtnmntmTopPanelCalculations.addActionListener(new SelectViewListener(rightSplitPane, topCalcView, true));
 		airportObservers.add(topCalcView);
 		topCalcView.setEditable(false);
 		topCalcView.setWrapStyleWord(true);
 
 		JRadioButtonMenuItem rdbtnmntmTopPanelNone = new JRadioButtonMenuItem("None");
 		topPanelButtonGroup.add(rdbtnmntmTopPanelNone);
 		mnTopPanel.add(rdbtnmntmTopPanelNone);
 		rdbtnmntmTopPanelNone.addActionListener(new SelectViewListener(rightSplitPane, null, true));
 
 		rdbtnmntmTopPanelTopView.setSelected(true);
 
 		JMenu mnNewMenu_1 = new JMenu("Bottom Panel");
 		mnNewMenu_1.setMnemonic('b');
 		mnView.add(mnNewMenu_1);
 
 		JRadioButtonMenuItem rdbtnmntmBottomPanelTopView = new JRadioButtonMenuItem("Top View");
 		bottomPanelButtonGroup.add(rdbtnmntmBottomPanelTopView);
 		mnNewMenu_1.add(rdbtnmntmBottomPanelTopView);
 		TopView bottomTopView = new TopView(airport); 
 		rdbtnmntmBottomPanelTopView.addActionListener(new SelectViewListener(rightSplitPane, bottomTopView, false));
 		airportObservers.add(bottomTopView);
 
 		JRadioButtonMenuItem rdbtnmntmBottomPanelSideView = new JRadioButtonMenuItem("Side View");
 		rdbtnmntmBottomPanelSideView.setSelected(true);
 		bottomPanelButtonGroup.add(rdbtnmntmBottomPanelSideView);
 		mnNewMenu_1.add(rdbtnmntmBottomPanelSideView);
 		SideView bottomSideView = new SideView(airport);
 		rdbtnmntmBottomPanelSideView.addActionListener(new SelectViewListener(rightSplitPane, bottomSideView, false));
 		airportObservers.add(bottomSideView);
 		rightSplitPane.setRightComponent(bottomSideView);
 		bottomSideView.setVisible(true);
 
 		JRadioButtonMenuItem rdbtnmntmBottomPanelCalculations = new JRadioButtonMenuItem("Calculations");
 		bottomPanelButtonGroup.add(rdbtnmntmBottomPanelCalculations);
 		mnNewMenu_1.add(rdbtnmntmBottomPanelCalculations);
 		CalculationsView bottomCalcView = new CalculationsView(airport);
 		rdbtnmntmBottomPanelCalculations.addActionListener(new SelectViewListener(rightSplitPane, bottomCalcView, false));
 		airportObservers.add(bottomCalcView);
 		bottomCalcView.setEditable(false);
 		bottomCalcView.setWrapStyleWord(true);
 
 		JRadioButtonMenuItem rdbtnmntmBottomPanelNone = new JRadioButtonMenuItem("None");
 		bottomPanelButtonGroup.add(rdbtnmntmBottomPanelNone);
 		mnNewMenu_1.add(rdbtnmntmBottomPanelNone);
 		rdbtnmntmBottomPanelNone.addActionListener(new SelectViewListener(rightSplitPane, null, false));
 
 		JMenu mnEmail = new JMenu("Email");
 		mnEmail.setMnemonic('m');
 		menuBar.add(mnEmail);
 
 		JMenuItem mntmSendEmail = new JMenuItem("Email Calculations");
 		mntmSendEmail.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/MailIcon.png")));
 		mntmSendEmail.setMnemonic('e');
 		ShowEmailDialogListener sedl = new ShowEmailDialogListener(addressBook, airport);
 		airportObservers.add(sedl);
 		mntmSendEmail.addActionListener(sedl);
 		mnEmail.add(mntmSendEmail);
 
 		JMenuItem mntmAddressBook = new JMenuItem("Address book");
 		mntmAddressBook.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/AddressBookIcon.png")));
 		mntmAddressBook.setMnemonic('a');
 		mntmAddressBook.addActionListener(new ShowAddressBookListener(addressBook));
 		mnEmail.add(mntmAddressBook);
 		
 		JMenu mnPrint = new JMenu("Print");
 		mnPrint.setMnemonic('p');
 		menuBar.add(mnPrint);
 		
 		JMenuItem mntmPrintCalculations = new JMenuItem("Print Calculations");
 		mntmPrintCalculations.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/PrintIcon.png")));
 		mnPrint.add(mntmPrintCalculations);
 		mntmPrintCalculations.setMnemonic('p');
 		PrintCalculationsListener pcl = new PrintCalculationsListener(airport);
 		mntmPrintCalculations.addActionListener(pcl);
 		airportObservers.add(pcl);
 		
 		JMenu mnHelp = new JMenu("Help");
 		mnHelp.setMnemonic('h');
 		menuBar.add(mnHelp);
 
 		JMenuItem mntmShowHelp = new JMenuItem("Show help");
 		mntmShowHelp.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/HelpIcon.png")));
 		mntmShowHelp.setMnemonic('s');
 		mnHelp.add(mntmShowHelp);
 		mntmShowHelp.addActionListener(new ShowHelpListener());
 
 		JMenuItem mntmAbout = new JMenuItem("About ");
 		mntmAbout.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("data/Icons/AboutIcon.png")));
 		mntmAbout.setMnemonic('a');
 		mnHelp.add(mntmAbout);
 		mntmAbout.addActionListener(new ShowAboutListener());
 
 
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new MigLayout("", "[220px][grow]", "[grow]"));
 
 		JPanel leftPanel = new JPanel();
 		contentPane.add(leftPanel, "cell 0 0,alignx center,aligny top");
 		leftPanel.setLayout(new MigLayout("", "[275px,grow]", "[grow][][][][][grow]"));
 
 		JPanel panel = new JPanel();
 		leftPanel.add(panel, "cell 0 0,grow");
 		panel.setLayout(new MigLayout("", "[77px][grow]", "[14px][]"));
 
 		JLabel lblCurrentAirport = new JLabel("Current Airport:");
 		lblCurrentAirport.setHorizontalAlignment(SwingConstants.RIGHT);
 		panel.add(lblCurrentAirport, "cell 0 0,alignx left,aligny top");
 
 		lblAirportName = new JLabel("<untitled>");
 		panel.add(lblAirportName, "cell 1 0");
 
 		JLabel lblCurrentRunway = new JLabel("Current Runway:");
 		panel.add(lblCurrentRunway, "cell 0 1,alignx trailing");
 
 		currentRunwayCombo = new JComboBox();
 		panel.add(currentRunwayCombo, "cell 1 1,growx");
 		SelectRunwayListener srl = new SelectRunwayListener(airport, airportObservers);
 		currentRunwayCombo.addItemListener(srl);
 		airportObservers.add(srl);
 
 
 		JPanel leftTopPanel = new JPanel();
 		leftTopPanel.setBorder(new TitledBorder(null, "Original Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		leftPanel.add(leftTopPanel, "cell 0 1,grow");
 		leftTopPanel.setLayout(new BorderLayout(0, 0));
 
 		OriginalParametersTable = new JTable();
 		OriginalParametersTable.setEnabled(false);
 		OriginalParametersTable.setRowSelectionAllowed(false);
 		OriginalParametersTable.setModel(new DefaultTableModel(
 				new Object[][] {
 						{"TORA", ""},
 						{"TODA", ""},
 						{"ASDA", ""},
 						{"LDA", ""},
 						{"DT",""}
 				},
 				new String[] {
 						"New column", "New column"
 				}
 				));
 		leftTopPanel.add(OriginalParametersTable, BorderLayout.CENTER);
 		OriginalParametersTable.getColumnModel().getColumn(0).setPreferredWidth(150);
 
 		JPanel leftMiddlePanel = new JPanel();
 		leftMiddlePanel.setBorder(new TitledBorder(null, "Redeclared Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		leftPanel.add(leftMiddlePanel, "cell 0 2,grow");
 		leftMiddlePanel.setLayout(new BorderLayout(0, 0));
 
 		RedeclaredParametersTable = new JTable();
 		RedeclaredParametersTable.setEnabled(false);
 		RedeclaredParametersTable.setRowSelectionAllowed(false);
 		RedeclaredParametersTable.setModel(new DefaultTableModel(
 				new Object[][] {
 						{"TORA(R)", ""},
 						{"TODA(R)", ""},
 						{"ASDA(R)", ""},
 						{"LDA(R)", ""},
 						{"DT",""}
 				},
 				new String[] {
 						"New column", "New column"
 				}
 				));
 		leftMiddlePanel.add(RedeclaredParametersTable, BorderLayout.CENTER);
 		RedeclaredParametersTable.getColumnModel().getColumn(0).setPreferredWidth(150);
 
 		JPanel leftUpperBottomPanel = new JPanel();
 		leftUpperBottomPanel.setBorder(new TitledBorder(null, "Obstacle Details", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		leftPanel.add(leftUpperBottomPanel, "cell 0 3,grow");
 		leftUpperBottomPanel.setLayout(new BorderLayout(0, 0));
 
 		ObstacleDetailsTable = new JTable();
 		ObstacleDetailsTable.setEnabled(false);
 		ObstacleDetailsTable.setRowSelectionAllowed(false);
 		ObstacleDetailsTable.setModel(new DefaultTableModel(
 				new Object[][] {
 						{"Name", ""},
 						{"Height", ""},
 						{"Distance from Threshold", ""},
 						{"Distance from Centre Line", ""},
 						{"Closest To", ""},
 				},
 				new String[] {
 						"Property", "Value"
 				}
 				));
 		leftUpperBottomPanel.add(ObstacleDetailsTable);
 		ObstacleDetailsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
 
 		JPanel leftLowerBottomPanel = new JPanel();
 		leftLowerBottomPanel.setBorder(new TitledBorder(null, "Advanced Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		leftPanel.add(leftLowerBottomPanel, "cell 0 4,grow");
 		leftLowerBottomPanel.setLayout(new BorderLayout(0, 0));
 
 		AdvancedParametersTable = new JTable();
 		AdvancedParametersTable.setEnabled(false);
 		AdvancedParametersTable.setRowSelectionAllowed(false);
 		AdvancedParametersTable.setModel(new DefaultTableModel(
 				new Object[][] {
 						{"RESA", ""},
 						{"Stopway", ""},
 						{"Blast Allowance", ""},
 						{"Angle of Slope", ""},
 						{"Strip Width", ""},
 				},
 				new String[] {
 						"Property", "Value"
 				}
 				));
 		leftLowerBottomPanel.add(AdvancedParametersTable);
 
 		AdvancedParametersTable.getColumnModel().getColumn(0).setPreferredWidth(150);
 
 
 		contentPane.add(rightSplitPane, "cell 1 0,grow");
 		rightSplitPane.setResizeWeight(0.5);
 		rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 
 		try {
 			loadRecentFiles(HISTORY_TO_SHOW);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void loadRecentFiles(int numberToRead) throws IOException{
 		recentAirports = new Vector<String>();
 
 		FileInputStream fis = new FileInputStream("data/recentAirports");
 		InputStreamReader in = new InputStreamReader(fis);
 		BufferedReader br = new BufferedReader(in);
 
 		for(int i = 0; i < numberToRead; i++){
 			String line = br.readLine();
 			if (line != null){
 				recentAirports.add(line);		
 			}
 			else break;
 		}
 
 
 		recentObstacles = new Vector<String>();
 
 		fis = new FileInputStream("data/recentObstacles");
 		in = new InputStreamReader(fis);
 		br = new BufferedReader(in);
 
 		for(int i = 0; i < numberToRead; i++){
 			String line = br.readLine();
 			if (line != null){
 				recentObstacles.add(line);
 			}
 			else break;
 		}
 
 	}
 
 	public static void saveRecentFile(Airport airport, String filename) throws IOException{
 		filename = filename.replaceAll("\\\\", "/");
 		
 		if(recentAirports.contains(filename)){
 			recentAirports.remove(filename);
 			recentAirports.add(0, filename);
 		}else{
 			recentAirports.add(0, filename);
 			if(recentAirports.size() > MAX_HISTORY) recentAirports.remove(MAX_HISTORY + 1);
 		}
 
 		FileWriter fw = new FileWriter("data/recentAirports");
 		BufferedWriter bw = new BufferedWriter(fw);
 
 		for (String string: recentAirports){
 			bw.write(string + "\n");
 		}
 		bw.flush();
 	}
 
 	public static void saveRecentFile(Obstacle obstacle, String filename) throws IOException{
 		filename = filename.replaceAll("\\\\", "/");
 		
 		if(recentObstacles.contains(filename)){
 			recentObstacles.remove(filename);
 			recentObstacles.add(0, filename);
 		}else{
 			recentObstacles.add(0, filename);
 			if(recentObstacles.size() > MAX_HISTORY) recentObstacles.remove(MAX_HISTORY + 1);
 		}
 
 		FileWriter fw = new FileWriter("data/recentObstacles");
 		BufferedWriter bw = new BufferedWriter(fw);
 
 		for (String string: recentObstacles){
 			bw.write(string + "\n");
 		}
 		bw.flush();
 
 	}
 
 	public Airport getAirport(){
 		return airport;
 	}
 
 	@Override
 	public void updateAirport(Airport airport) {
 		this.airport = airport;
 		if(airport.getName().equals("")){
 			lblAirportName.setText("<untitled>");
 		}else{
 			lblAirportName.setText(airport.getName());
 		}	
 		generateRunwayComboBox(currentRunwayCombo);
 		generatePhysicalRunwayRadioButtons(physicalRunwayMenu);
 		//currentRunwayCombo.setSelectedIndex(0);
 		updateTables();
 		
 		populateRecentAirports();
 	}
 
	private synchronized void generateRunwayComboBox(JComboBox currentRunwayComboBox){
 		if ((airport.getCurrentPhysicalRunway() != null) && 
 				(airport.getCurrentPhysicalRunway().getRunway(0) != currentRunwayCombo.getItemAt(0))){
 			PhysicalRunway r = airport.getCurrentPhysicalRunway();
			
			ItemListener itemListener = currentRunwayComboBox.getItemListeners()[0];
			
			currentRunwayComboBox.removeItemListener(itemListener);
			
 			currentRunwayComboBox.removeAllItems();
 			currentRunwayComboBox.addItem(r.getRunway(0));
			currentRunwayComboBox.addItem(r.getRunway(1));	
			
			currentRunwayComboBox.addItemListener(itemListener);
 		}
		
 	}
 
 	private void  generatePhysicalRunwayRadioButtons(JMenu parentMenuItem){
 		parentMenuItem.removeAll();
 		for(PhysicalRunway r : airport.getPhysicalRunways()){
 			JRadioButtonMenuItem rdbtnmntmlr = new JRadioButtonMenuItem(r.getId());
 			physicalRunwayButtonGroup.add(rdbtnmntmlr);
 			parentMenuItem.add(rdbtnmntmlr);
 			rdbtnmntmlr.addActionListener(new SelectPhysicalRunwayListener(airport, r, airportObservers)); 
 			if (airport.getCurrentPhysicalRunway() == r) rdbtnmntmlr.setSelected(true);
 		}
 	}
 
 	private void updateTables(){
 
 		if(airport.getCurrentRunway() != null){
 			Runway runway = airport.getCurrentRunway();
 
 			TableModel original = OriginalParametersTable.getModel();
 			original.setValueAt(runway.getTORA(Runway.DEFAULT) + "m", 0, 1);
 			original.setValueAt(runway.getTODA(Runway.DEFAULT) + "m", 1, 1);
 			original.setValueAt(runway.getASDA(Runway.DEFAULT) + "m", 2, 1);
 			original.setValueAt(runway.getLDA(Runway.DEFAULT) + "m", 3, 1);
 			original.setValueAt(runway.getDisplacedThreshold(Runway.DEFAULT) + "m", 4, 1);
 
 			TableModel redeclared = RedeclaredParametersTable.getModel();
 			redeclared.setValueAt(runway.getTORA(Runway.REDECLARED) + "m", 0, 1);
 			redeclared.setValueAt(runway.getTODA(Runway.REDECLARED) + "m", 1, 1);
 			redeclared.setValueAt(runway.getASDA(Runway.REDECLARED) + "m", 2, 1);
 			redeclared.setValueAt(runway.getLDA(Runway.REDECLARED) + "m", 3, 1);
 			redeclared.setValueAt(runway.getDisplacedThreshold(Runway.REDECLARED) + "m", 4, 1);
 		} else {
 			TableModel original = OriginalParametersTable.getModel();
 			original.setValueAt("", 0, 1);
 			original.setValueAt("", 1, 1);
 			original.setValueAt("", 2, 1);
 			original.setValueAt("", 3, 1);
 			original.setValueAt("", 4, 1);
 
 			TableModel redeclared = RedeclaredParametersTable.getModel();
 			redeclared.setValueAt("", 0, 1);
 			redeclared.setValueAt("", 1, 1);
 			redeclared.setValueAt("", 2, 1);
 			redeclared.setValueAt("", 3, 1);
 			redeclared.setValueAt("", 4, 1);
 		}
 
 		if(airport.getCurrentPhysicalRunway() != null && airport.getCurrentPhysicalRunway().getObstacle() != null){
 			PhysicalRunway physicalRunway = airport.getCurrentPhysicalRunway();
 			Obstacle obstacle = physicalRunway.getObstacle();
 
 			TableModel obstacleTab = ObstacleDetailsTable.getModel();
 			obstacleTab.setValueAt(obstacle.getName(), 0, 1);
 			obstacleTab.setValueAt(obstacle.getHeight(), 1, 1);
 			obstacleTab.setValueAt(physicalRunway.getDistanceAwayFromThreshold(), 2, 1);
 			obstacleTab.setValueAt(physicalRunway.getDistanceAwayFromCenterLine(), 3, 1);
 			obstacleTab.setValueAt(physicalRunway.closeTo(), 4, 1);
 		} else {
 			TableModel obstacleTab = ObstacleDetailsTable.getModel();
 			obstacleTab.setValueAt("", 0, 1);
 			obstacleTab.setValueAt("", 1, 1);
 			obstacleTab.setValueAt("", 2, 1);
 			obstacleTab.setValueAt("", 3, 1);	
 			obstacleTab.setValueAt("", 4, 1);
 		}
 
 		if(airport.getCurrentPhysicalRunway() != null){
 			PhysicalRunway physicalRunway = airport.getCurrentPhysicalRunway();
 
 			TableModel advancedParametersTab = AdvancedParametersTable.getModel();
 			advancedParametersTab.setValueAt(physicalRunway.getRESA(), 0, 1);
 			advancedParametersTab.setValueAt(physicalRunway.getStopway(), 1, 1);
 			advancedParametersTab.setValueAt(physicalRunway.getBlastAllowance(), 2, 1);
 			advancedParametersTab.setValueAt(physicalRunway.getAngleOfSlope(), 3, 1);
 			advancedParametersTab.setValueAt(physicalRunway.getRunwayStripWidth(), 4, 1);
 		} else {
 			TableModel advancedParametersTab = AdvancedParametersTable.getModel();
 			advancedParametersTab.setValueAt("", 0, 1);
 			advancedParametersTab.setValueAt("", 1, 1);
 			advancedParametersTab.setValueAt("", 2, 1);
 			advancedParametersTab.setValueAt("", 3, 1);
 			advancedParametersTab.setValueAt("", 4, 1);
 		}
 	}
 
 	private void populateRecentAirports(){
 		
 		mnOpenRecentAirports.removeAll();
 		//Populate list
 		for(String s : recentAirports){
 			JMenuItem mntmRecentAirport = new JMenuItem(s);
 			mnOpenRecentAirports.add(mntmRecentAirport);
 			OpenRecentAirportListener oral = new OpenRecentAirportListener(s, airportObservers);
 			mntmRecentAirport.addActionListener(oral);
 		}
 		
 	}
 
 	private void populateRecentObstacles(){
 		
 		mnOpenRecentObstacles.removeAll();
 		//Populate list
 		for(String s : recentObstacles){
 			JMenuItem mntmRecentObstacle = new JMenuItem(s);
 			mnOpenRecentObstacles.add(mntmRecentObstacle);
 			OpenRecentObstacleListener orol = new OpenRecentObstacleListener(s, airport, airportObservers);
 			mntmRecentObstacle.addActionListener(orol);
 			airportObservers.add(orol);
 		}
 	}
 
 }
 
