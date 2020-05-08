 package com.bluebarracudas.app;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 import com.bluebarracudas.model.TFactory;
 
 /** Our entry point for the MBTA Application */
 class TApp extends JFrame implements ActionListener {
	private static final long serialVersionUID = -8356988946650943969L;

 	private final static String OPTIONS = "Options";
 
 	private final static String EXIT = "Exit";
 	private final static String LIVE_DATA = "Use live MBTA data.";
 	private final static String TEST_DATA = "Use test data";
 	private final static String FILE_DOES_NOT_EXIST = "File does not exist.";
 	private final static String BLUE = "Blue";
 	private final static String ORANGE = "Orange";
 	private final static String RED = "Red";
 
 	private final static String DEFAULT_TEST_DATA_PATH_FORMAT = "/course/cs4500f12/MBTA_test_data/2012_10_19/Test%s_2012_10_19.json";
 	private final static String FILE_DOES_NOT_EXIST_FORMAT = "Test data file %s does not exist. Using live data.";
 
 	private static final int PREF_WIDTH = 800;
 	private static final int PREF_HEIGHT = 800;
 
 	private JLayeredPane m_pJPane;
 	private TMapPanel m_pMapPanel;
 	private TControlPanel m_pControlPanel;
 	private String sBlueTestDataFilePath;
 	private String sOrangeTestDataFilePath;
 	private String sRedTestDataFilePath;
 
 	/** Default constructor */
 	TApp() {
 		sBlueTestDataFilePath   = String.format(DEFAULT_TEST_DATA_PATH_FORMAT, BLUE);
 		sOrangeTestDataFilePath = String.format(DEFAULT_TEST_DATA_PATH_FORMAT, ORANGE);
 		sRedTestDataFilePath    = String.format(DEFAULT_TEST_DATA_PATH_FORMAT, RED);
 
 		// Set our preferred size and layout
 		setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
 		setLayout(new BorderLayout());
 
 		// Create our layered pane and add it to the frame
 		m_pJPane = new JLayeredPane();
 		m_pJPane.setBounds(0, 0, PREF_WIDTH, PREF_HEIGHT);
 		add(m_pJPane);
 
 		// Create a new map panel
 		m_pMapPanel = new TMapPanel();
 		m_pMapPanel.setOpaque(true);
 		m_pMapPanel.setBackground(Color.RED);
 		m_pMapPanel.setBounds(0, -25, PREF_WIDTH, PREF_HEIGHT);
 		m_pJPane.add(m_pMapPanel, new Integer(0), 0);
 
 		// Create a new control panel
 		m_pControlPanel = new TControlPanel();
 		m_pControlPanel.setOpaque(true);
 		m_pControlPanel.setBackground(Color.LIGHT_GRAY);
 		m_pControlPanel.setBounds(20, 150, 225, 600);
 		m_pJPane.add(m_pControlPanel, new Integer(1), 0);
 
 		// Add our map panel as an observer
 		TDataSource.add(m_pMapPanel);
 		TFactory.add(m_pMapPanel);
 
 		// Set up our frame...
 		initializeMenuBar();
 
 		// Stop the application when the window is closed
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
 		pack();
 		setVisible(true);
 	}
 
 	/** Displays a dialogue for inputting test data file paths */
 	private void displayTestDataPathDialog() {
 		JTextField blueLine = new JTextField(sBlueTestDataFilePath);
 		JTextField redLine = new JTextField(sRedTestDataFilePath);
 		JTextField orangeLine = new JTextField(sOrangeTestDataFilePath);
 
 		final JComponent[] inputs = new JComponent[] { new JLabel("Blue Line"),
 				blueLine, new JLabel("Orange Line"), orangeLine,
 				new JLabel("Red Line"), redLine };
 
 		JOptionPane.showMessageDialog(null, inputs,
 				"Enter test-data json file paths", JOptionPane.PLAIN_MESSAGE);
 		sBlueTestDataFilePath = blueLine.getText();
 		sOrangeTestDataFilePath = orangeLine.getText();
 		sRedTestDataFilePath = redLine.getText();
 	}
 
 	/** Initializes the menu bar */
 	private void initializeMenuBar() {
 		JMenuBar menuBar;
 		JMenu optionsMenu;
 		ButtonGroup optionsBG;
 		JRadioButtonMenuItem liveDataRB, testDataRB;
 		JMenuItem exitButton;
 
 		// Live Data radio button
 		liveDataRB = new JRadioButtonMenuItem(LIVE_DATA);
 		liveDataRB.setSelected(true);
 		liveDataRB.addActionListener(this);
 
 		// Test Data radio button
 		testDataRB = new JRadioButtonMenuItem(TEST_DATA);
 		testDataRB.addActionListener(this);
 
 		// Exit button
 		exitButton = new JMenuItem(EXIT);
 		exitButton.addActionListener(this);
 
 		// Options button group
 		optionsBG = new ButtonGroup();
 		optionsBG.add(liveDataRB);
 		optionsBG.add(testDataRB);
 		optionsBG.add(exitButton);
 
 		// Create the Options Menu
 		optionsMenu = new JMenu(OPTIONS);
 		optionsMenu.add(liveDataRB);
 		optionsMenu.add(testDataRB);
 		optionsMenu.addSeparator();
 		optionsMenu.add(exitButton);
 
 		// Create the Menu bar.
 		menuBar = new JMenuBar();
 		menuBar.add(optionsMenu);
 
 		// Set menu bar
 		setJMenuBar(menuBar);
 	}
 
 	/** Listens for ActionEvents and handles them accordingly */
 	public void actionPerformed(ActionEvent actionEvent) {
 		String actionCommand = actionEvent.getActionCommand();
 		if (actionCommand == EXIT) {
 			TDataSource.stop();
 			System.exit(0);
 		} else if (actionCommand == LIVE_DATA) {
 			TDataSource.start();
 		} else if (actionCommand == TEST_DATA) {
 			displayTestDataPathDialog();
 			TDataSource.stop();
 			if (!getTestData()) {
 				TDataSource.start();
 			}
 		}
 	}
 
 	/** Verifies if a file exists and if not displays an error message. */
 	private boolean verifyExistance(File file) {
 		if (!file.exists()) {
 			JOptionPane.showMessageDialog(
 					this,
 					String.format(FILE_DOES_NOT_EXIST_FORMAT,
 							file.getAbsolutePath()), FILE_DOES_NOT_EXIST,
 					JOptionPane.ERROR_MESSAGE);
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	/** Gets the test data from the previously specifed file locations */
 	private boolean getTestData() {
 		File blue = new File(sBlueTestDataFilePath);
 		File orange = new File(sOrangeTestDataFilePath);
 		File red = new File(sRedTestDataFilePath);
 
 		if (verifyExistance(blue) && verifyExistance(orange)
 				&& verifyExistance(red)) {
 			TFactory.removeAllTrips();
 			TDataParser.populateFromTestData(blue);
 			TDataParser.populateFromTestData(orange);
 			TDataParser.populateFromTestData(red);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/** Our main function */
 	public static void main(String strArgs[]) {
 		// Start our listener
 		TDataSource.start();
 
 		// Simply create our JFrame
 		new TApp();
 	}
 
 }
