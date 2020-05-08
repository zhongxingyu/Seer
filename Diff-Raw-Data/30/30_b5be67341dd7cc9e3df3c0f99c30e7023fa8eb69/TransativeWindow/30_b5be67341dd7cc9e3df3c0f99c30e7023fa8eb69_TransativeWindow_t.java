 package gui;
 
 import io.ModelIO;
 import util.Util;
 
 import java.awt.Color;
 import java.awt.event.*;
 import java.io.File;
 import javax.swing.*;
 
 /**
  * Creates a new, interactive transative window for use with the TDM
  * project.
  * 
  * @author Andrew Vaughan, Matthew Hausknecht
  * @version February 6, 2008
  */
 public class TransativeWindow implements ActionListener, ItemListener {
 	
 	/**
 	 * The title of the current application.
 	 */
 	public static final String APP_TITLE = "Force Composition Model";
 	
 	/**
 	 * The version of the current application.
 	 */
	public static final String APP_VER = " 7.5.6";
 	
 	/**
 	 * Strings for the Menu Names
 	 */
 	public static final String FILE_MENU				= "File";
 	public static final String MODELS_MENU				= "Models";
 	public static final String ACTIONS_MENU				= "Actions";
 	public static final String HELP_MENU				= "Help";
 	
 	/**
 	 * Strings for the GUI Menu Items
 	 */
 	public static final String SAVE_STRING 				= "Save Model...";
 	public static final String OPEN_STRING 				= "Open Model...";
 	public static final String SAVE_IMAGE_STRING 		= "Save as Image...";
 	public static final String EXIT_STRING 				= "Exit";
 	public static final String ABOUT_STRING 			= "About";
 	public static final String HELP_STRING 				= "Help";
 	public static final String TWO_PREMISE_STRING		= "New 2-Premise Model";
 	public static final String THREE_PREMISE_STRING		= "New 3-Premise Model";
 	public static final String CUSTOM_MODEL_STRING		= "New Custom Model";
 	public static final String EXPLORE_STRING			= "Explore";
 	public static final String VISUALIZE_STRING			= "Visualize";
 	public static final String MAGNITUDE_STRING			= "Show Magnitudes";
  	public static final String TCR_STRING				= "Show TCR";
 	public static final String CONSTRAIN_STRING			= "Show Constraints";
 	
 	static final int MAX_NUM_PREMISES					= 25;
 	
 	static final String TDM_FILE_EXTENSION				= ".tdm";
 	
 	/**
 	 * The main window container.
 	 */
 	public JFrame window;
 	
 	/**
 	 * The panel that contains all of the other panels.
 	 */
 	public ContainerPanel cPanel;
 		
 	public JMenuBar menuBar;
 	private JCheckBoxMenuItem mnuMag;
 	private JCheckBoxMenuItem mnuTCR;
 	private JCheckBoxMenuItem mnuShowConstrain;
 	
 //---------------------------------------------------------------------------------------
 // 	PUBLIC METHODS
 	
 	
 	/**
 	 * Creates a new window for holding transative models.
 	 */
 	public TransativeWindow() {		
 		initializeGUI();
 	}
 	
 	
 //---------------------------------------------------------------------------------------
 // 	EVENT HANDLERS
 	
 	
 	/**
 	 * Handles all regular-menu events.
 	 * 
 	 * @param e the event to process
 	 */
 	public void actionPerformed(ActionEvent e) {
         String source = ((JMenuItem)(e.getSource())).getText();
         
         // If they chose the "Save Model..." menu item
         if (source.equals(SAVE_STRING)) {
 			saveModel();
 			
         // If they chose the "Open Model..." menu item
         } else if (source.equals(OPEN_STRING)) {
 			openModel();
         	
         // If they chose the "Save as Image..." menu item
     	} else if (source.equals(SAVE_IMAGE_STRING)) {
 			saveImage();
         	
         // If they chose the "Exit" menu item
         } else if (source.equals(EXIT_STRING)) {
         	System.exit(1);
         	
         // If they chose the "About" menu item
         } else if (source.equals(ABOUT_STRING)) {
         	showAboutBox();
         	
 		// If they chose the "Help" menu item
         } else if (source.equals(HELP_STRING)) {
         	new HelpWindow();
         	
         // If they chose any of the "New 2-Premise Model" menu item
         } else if (source.equals(TWO_PREMISE_STRING)) {
 			createNewModel(2);
         	
         // If they chose any of the "New 3-Premise Model" menu item
 	    } else if (source.equals(THREE_PREMISE_STRING)) {
 			createNewModel(3);
 	    	
 	    // If they chose any of the "New Custom Model" menu item
 	    } else if (source.equals(CUSTOM_MODEL_STRING)) {
 			String returned = (String)(JOptionPane.showInputDialog(window, "Number of required premises " + 
 					"(less than 25):", "Create Custom Model", JOptionPane.QUESTION_MESSAGE, null, null, "4"));
     		
 			if (returned == null) return;
     		
 			int numPremises = Integer.parseInt(returned);
     		
     		if (numPremises > MAX_NUM_PREMISES)
     			JOptionPane.showMessageDialog(window, "Please enter a premise count less than 25.", 
     				"Invalid Premise Count", JOptionPane.ERROR_MESSAGE);
     		else
 				createNewModel(numPremises);
 	    
         // If they chose the "Explore..." menu item
 		} else if (source.equals(EXPLORE_STRING)) {
 	    	new ExploreWindow(window.getX() + window.getWidth(), window.getY(), cPanel, menuBar);			
   		
 		} else if (source.equals(VISUALIZE_STRING)) {
   			new VisualizeWindow(0, 0, cPanel, menuBar); 
   		}
     }
     
 	/**
 	 * Handles all specialty menu events.
 	 * 
 	 * @param e the event to process
 	 */
     public void itemStateChanged(ItemEvent e) {
     	String source = ((JMenuItem)e.getItemSelectable()).getText();
     		
     	if (source.equals(MAGNITUDE_STRING)) {
     		cPanel.setMagnitudes(e.getStateChange() == ItemEvent.SELECTED);
     		cPanel.repaintAll();
     		
     	} else if (source.equals(TCR_STRING)) {
     		cPanel.setTCR(e.getStateChange() == ItemEvent.SELECTED);
     		cPanel.repaintAll();
     		
     	} else if (source.equals(CONSTRAIN_STRING)) {
     		cPanel.setShowConstrain(e.getStateChange() == ItemEvent.SELECTED);
     	} 
     }
     
     
     
 //---------------------------------------------------------------------------------------
 //	ACCESSORS/MUTATORS
 	
 	
 	/**
 	 * Returns whether or not the magnitudes are shown currently.
 	 * 
 	 * @return true if the magnitudes are shown, false if not
 	 */
 	public boolean isMagnitude() {
 		return cPanel.isMagnitude();
 	}
 	
 	/**
 	 * Returns whether or not the TCR is currently shown
 	 * 
 	 * @return true if TCR is shown, false if not
 	 */
 	public boolean isTCR() {
 		return cPanel.isTCR();
 	}
 	
 	
 //---------------------------------------------------------------------------------------
 // 	PRIVATE METHODS
 	
 	/**
 	 * Brings up a file saving dialog menu
 	 */
 	private void saveModel ()
 	{
 		JFileChooser chooser = Util.chooseFile (TDM_FILE_EXTENSION, "Transitive Dynamics Models (*" +
 				TDM_FILE_EXTENSION + ")");
 		int returned = chooser.showSaveDialog(window);
     	
     	if (returned == JFileChooser.APPROVE_OPTION) {
     		String absPath = chooser.getSelectedFile().getAbsolutePath();
     		if (!absPath.toLowerCase().endsWith(TDM_FILE_EXTENSION)) absPath += TDM_FILE_EXTENSION;
     		
     		ModelIO.saveFile(this, new File(absPath));
     	}
 	}
 	
 	/**
 	 * Loads a TDM file
 	 */
 	private void openModel ()
 	{
 		JFileChooser chooser = Util.chooseFile (TDM_FILE_EXTENSION, "Transitive Dynamics Models (*" + 
 				TDM_FILE_EXTENSION + ")");
 		int returned = chooser.showOpenDialog(window);
     	
     	if (returned == JFileChooser.APPROVE_OPTION) {
     		ModelIO.loadFile(this, chooser.getSelectedFile());
     	}
 	}
 	
 	/**
 	 * Saves an image of the current model
 	 */
 	private void saveImage ()
 	{
 		JFileChooser chooser = Util.chooseFile (".png", "PNG Image Files (*.png)");
 		int returned = chooser.showSaveDialog(window);
     	
 		if (returned == JFileChooser.APPROVE_OPTION) {
     		String absPath = chooser.getSelectedFile().getAbsolutePath();
     		if (!absPath.toLowerCase().endsWith(".png")) absPath += ".png";
     		
     		ModelIO.saveImage(cPanel, absPath);
     	}
 	}
 	
 	/**
 	 * Creates a new model
 	 * 
 	 * @param numPremises The number of premises for the new model.
 	 */
 	private void createNewModel (int numPremises)
 	{
 		int response = JOptionPane.showConfirmDialog(window, "Are you sure you want to clear the " +
 			"current setup and\ncreate a new model?\n\nUnsaved changes will NOT be saved.", 
 			"Create a new model?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 			
     	if (response == JOptionPane.YES_OPTION) 
 		{ 
 			cPanel.createModel(numPremises);
 			int width = 300 + 100 * numPremises;
 			int height = 90 + 80 * numPremises;
 			window.setSize(width, height); 
 		}
 	}
 	
 	/**
 	 * Sets up and initializes the window and its children.
 	 */
 	private void initializeGUI() {
 		cPanel = new ContainerPanel();
 		
 		// Setup the window
 		window = new JFrame(APP_TITLE + " v" + APP_VER);
 		
 		window.setSize(500, 250);
         window.setBackground(Color.white);
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         // Add the menu
         menuBar = new JMenuBar();
         initializeMenus(menuBar);
         
         // Add the components
         window.setJMenuBar(menuBar);
         window.add(cPanel);
         
         // Show the created window
         window.setVisible(true);
 	}
 	
 	
 	/**
 	 * Creates the main menu for the current project.
 	 * 
 	 * @param menu the JMenuBar object to use
 	 */
 	private void initializeMenus (JMenuBar menu) {
 		initializeFileMenu(menu);
 		initializeModelsMenu(menu);
 		initializeActionsMenu(menu);
 		initializeHelpMenu (menu);
 	}
 	
 	/*
 	 * Initialize the File Menu
 	 */
 	private void initializeFileMenu (JMenuBar menu)
 	{
 		JMenu subMenu = new JMenu(FILE_MENU);
         subMenu.setMnemonic(KeyEvent.VK_F);
         menu.add(subMenu);
         
         addMenuItem(subMenu, SAVE_STRING, KeyEvent.VK_S, true);
         addMenuItem(subMenu, OPEN_STRING, KeyEvent.VK_O, true);
         
         subMenu.addSeparator();
         
         addMenuItem(subMenu, SAVE_IMAGE_STRING, KeyEvent.VK_I, true);
         
         subMenu.addSeparator();
         
 		JMenuItem menuItem = new JMenuItem(EXIT_STRING, KeyEvent.VK_X);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
 		menuItem.addActionListener(this);
         subMenu.add(menuItem);
 	}
 	
 	/*
 	 * Initialize the Models Menu
 	 */
 	private void initializeModelsMenu (JMenuBar menu)
 	{
 		JMenu subMenu = new JMenu(MODELS_MENU);
 		subMenu.setMnemonic(KeyEvent.VK_M);
 		menu.add(subMenu);
 		
 		mnuMag = new JCheckBoxMenuItem(MAGNITUDE_STRING, false);
 		mnuMag.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
 		mnuMag.addItemListener(this);
 		subMenu.add(mnuMag);
 		
 		mnuTCR = new JCheckBoxMenuItem(TCR_STRING, false);
 		mnuTCR.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
 		mnuTCR.addItemListener(this);
 		subMenu.add(mnuTCR);
 		
 		mnuShowConstrain = new JCheckBoxMenuItem(CONSTRAIN_STRING, false);
 		mnuShowConstrain.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
 		mnuShowConstrain.addItemListener(this);
 		subMenu.add(mnuShowConstrain);
 		
 		subMenu.addSeparator();
 		
 		addMenuItem(subMenu, TWO_PREMISE_STRING, KeyEvent.VK_2, false);
 		addMenuItem(subMenu, THREE_PREMISE_STRING, KeyEvent.VK_3, false);
 		addMenuItem(subMenu, CUSTOM_MODEL_STRING, KeyEvent.VK_C, false);
 	}
 	
 	/*
 	 * Initialize the Models Menu
 	 */
 	private void initializeActionsMenu (JMenuBar menu)
 	{
 		JMenu subMenu = new JMenu(ACTIONS_MENU);
 		subMenu.setMnemonic(KeyEvent.VK_A);
 		menu.add(subMenu);
 		
 		addMenuItem(subMenu, EXPLORE_STRING, KeyEvent.VK_X, true);
 		
 		addMenuItem(subMenu, VISUALIZE_STRING, KeyEvent.VK_V, true);
 	}
 	
 	/*
 	 * Initialize the Help Menu
 	 */
 	private void initializeHelpMenu (JMenuBar menu)
 	{
 		menu.add(Box.createHorizontalGlue());
 		
 		JMenu subMenu = new JMenu(HELP_STRING);
 		subMenu.setMnemonic(KeyEvent.VK_H);
 		menu.add(subMenu);
 		
 		JMenuItem menuItem = new JMenuItem(HELP_STRING, KeyEvent.VK_H);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
 		menuItem.addActionListener(this);
 		subMenu.add(menuItem);
 		
 		menuItem = new JMenuItem(ABOUT_STRING, KeyEvent.VK_A);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.CTRL_MASK));
 		menuItem.addActionListener(this);
 		subMenu.add(menuItem);
 	}
 	
 	/**
 	 * Creates a new menu item and adds it to the given menu.
 	 * 
 	 * @param mnu the menu to add it to
 	 * @param txt the text to put in the menu
 	 * @param key the mnemonic for the menu item
 	 * @param accel whether or not to make a CTRL accelerator
 	 */
 	private void addMenuItem(JMenu mnu, String txt, int key, boolean accel) {
 		JMenuItem menuItem = new JMenuItem(txt, key);
 		
 		if (accel) menuItem.setAccelerator(KeyStroke.getKeyStroke(key, ActionEvent.CTRL_MASK));
         
 		menuItem.addActionListener(this);
         mnu.add(menuItem);
 	}
 	
 	public void setMagnitudes(boolean mag) {
 		cPanel.setMagnitudes(mag);
 		mnuMag.setState(mag);
 	}
 	
 	public void setTCR(boolean tcr) {
 		cPanel.setTCR(tcr);
 		mnuTCR.setState(tcr);
 	}
 	
 	public void setShowConstrain (boolean shwConstrain) {
 		cPanel.setShowConstrain(shwConstrain);
 		mnuShowConstrain.setState(shwConstrain);
 	}
 	
 	private void showAboutBox() {
 		String title = APP_TITLE + " v" + APP_VER;
 		
 		String dialog = "<HTML><SPAN STYLE=\"font-family: Verdana; font-size: 14;\">" +
 		                  APP_TITLE + " v" + APP_VER +
 		                "</SPAN><HR><BR>" + 
 		                "<SPAN STYLE=\"font-family: Verdana; font-size: 10; font-weight: plain;\">" +
 		                  "Created by Andrew Vaughan<BR>Modified by Matthew Hausknecht<BR>Under Supervision of Phillip Wolff, PhD<BR><BR><I>Emory University, Atlanta, GA<I>" +
 		                "</SPAN></HTML>";
 		
 		JOptionPane.showMessageDialog(window, dialog, title, JOptionPane.INFORMATION_MESSAGE);
 	}
 }
