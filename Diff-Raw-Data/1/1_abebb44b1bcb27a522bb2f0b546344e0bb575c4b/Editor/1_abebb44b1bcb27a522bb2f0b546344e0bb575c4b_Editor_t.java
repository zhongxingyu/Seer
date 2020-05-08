 package gui;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 
 import tape.Tape;
 
 import machine.Machine;
 import machine.brainfuck.BrainfuckMachine;
 import machine.turing.*;
 import gui.RunWindow.*;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Event;
 import java.awt.Toolkit;
 import java.awt.event.*;
 import java.io.*;
 import java.util.ArrayList;
 
 /** This class represents an editor for Turing machines.
  * 
  * @author Vanessa Baier, Nils Breyer, Sven Schuster, Philipp Neumann, David Wille
  * 
  */
 
 public class Editor extends JFrame implements ActionListener, ItemListener {
 	private static final long serialVersionUID = -2288629542566838685L;
 	/**
 	 * The name of the application.
 	 */
 	public static final String APP_NAME = "TuringBrain IDE";
 	/**
 	 * The version of the application.
 	 */
 	public static final String APP_VERSION = "0.8.3";
 	/**
 	 * The authors of the application.
 	 */
 	public static final String APP_AUTHORS = "Im Rahmen des Teamprojekts 2011 entstanden.\n\nInstitut für Programmierung\nund Reaktive Systeme\n\nBetreuer: Matthias Hagner\n\nVanessa Baier,\n Nils Breyer,\n Phillipp Neumann,\n Sven Schuster,\n David Wille";
 
 	/**
 	 * The machine currently open in the editor.
 	 */
 	protected Machine currentMachine;
 	private File currentFile = null;
 	private String lastDir = ".";
 	private boolean delay = true;
 	private String tapeStyle = "default";
 	private SimulationWindow simulationWindow = null;
 	
 	private JMenu newSubmenu;
 	private JMenuItem newBFAction;
 	private JMenuItem newTMAction;
 	private JMenuItem openAction;
 	private JMenuItem saveAction;
 	private JMenuItem saveAsAction;
 	private JMenuItem exportLatexAction;
 	private JMenuItem exitAction;
 	private JMenuItem runAction;
 	private JMenuItem organizeRobotsAction;
 	private JMenuItem aboutAction;
 	private JCheckBoxMenuItem toggleDelayAction;
 	private ArrayList<JRadioButtonMenuItem> tapeStyleMenuItems = new ArrayList<JRadioButtonMenuItem>();
 	private JMenu tapeStyleSubmenu;
 	private JMenuBar menuBar;
 	private JMenu fileMenu;
 	private JMenu simulationMenu;
 	private JMenu helpMenu;
 
 	/**
 	 * Constructs the Editor window with all actionListeners and a basic setup.
 	 */
 	public Editor() {
 		setTitle(APP_NAME);
 		setSize(800, 800);
 		setMinimumSize(new Dimension(600, 400));
 
 		initEditor();
 
 		menuBar.add(fileMenu);
 		menuBar.add(simulationMenu);
 		menuBar.add(helpMenu);
 
 		// disable actions
 		saveAction.setEnabled(false);
 		saveAsAction.setEnabled(false);
 		exportLatexAction.setEnabled(false);
 		runAction.setEnabled(false);		
 		toggleDelayAction = new JCheckBoxMenuItem("Delay");
 		toggleDelayAction.setSelected(true);
 		toggleDelayAction.addItemListener(this);
 
 		// add menu subitems
 		fileMenu.add(newSubmenu);
 		fileMenu.add(openAction);
 		fileMenu.add(saveAction);
 		fileMenu.add(saveAsAction);
 		fileMenu.add(new JSeparator());
 		fileMenu.add(exportLatexAction);
 		fileMenu.add(new JSeparator());
 		fileMenu.add(exitAction);
 		simulationMenu.add(runAction);
 		simulationMenu.add(toggleDelayAction);
 		simulationMenu.add(tapeStyleSubmenu);
 		simulationMenu.add(new JSeparator());
 		simulationMenu.add(organizeRobotsAction);
 		helpMenu.add(aboutAction);
 
 		// menu shortcuts
 		newTMAction.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		newBFAction.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK ));
 		openAction.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		saveAction.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		exitAction.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		runAction.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 	}
 	
 	/**
 	 * Checks if an object is contained in an array
 	 * @param array The array to search in
 	 * @param object The object to be searched
 	 * @return True if the object is contained in the array, false if not
 	 */
 	public static boolean contains(Object[] array, Object object) {
 		for (Object o: array) {
 			if (o == object)  {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * The editor main, which initializes a new editor window.
 	 * @param args Command line arguments
 	 */
 	public static void main(String[] args) {
 		// try to set look for Linux and Mac OS X
 		try {
 			if (System.getProperties().getProperty("os.name").equals("Linux")) {
 				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
 			}
 			else if (System.getProperties().getProperty("os.name").equals("Mac OS X")) {
 				System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
 				System.setProperty("apple.laf.useScreenMenuBar", "true");
 			}
 		} 
 		catch (Exception e) {
 		}
 
 		Editor mainWindow = new Editor();
 		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainWindow.setVisible(true);
 
 		//mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
 	}
 
 	/**
 	 * Initializes the editor.
 	 */
 	public void initEditor() {
 		// create menu bar
 		menuBar = new JMenuBar();
 
 		// set menu bar
 		setJMenuBar(menuBar);
 
 		// create File->New menu items
 		newSubmenu = new JMenu("New");
 		newTMAction = new JMenuItem("Turing machine...");
 		newBFAction = new JMenuItem("Brainfuck program");
 		newSubmenu.add(newTMAction);
 		newSubmenu.add(newBFAction);
 
 		// create File menu items
 		openAction = new JMenuItem("Open...");
 		saveAction = new JMenuItem("Save");
 		saveAsAction = new JMenuItem("Save As...");
 		exportLatexAction = new JMenuItem("Export as LaTeX");
 		exitAction = new JMenuItem("Exit");
 		
 		// create Simulation->Tape style submenu items
 		tapeStyleSubmenu = new JMenu("Tape style");
 		File stylesDir = new File("tape/images/styles");
 		for (File style : stylesDir.listFiles()) {
 			if (style.isDirectory() == true) {
 				JRadioButtonMenuItem styleMenuItem = new JRadioButtonMenuItem(style.getName());
 				styleMenuItem.setSelected(style.getName().equals("default"));
 				styleMenuItem.addItemListener(this);
 				this.tapeStyleMenuItems.add(styleMenuItem);
 				tapeStyleSubmenu.add(styleMenuItem);
 			}
 		}
 
 		newSubmenu.add(newTMAction);
 		newSubmenu.add(newBFAction);
 		
 		// create Simulation menu items
 		runAction = new JMenuItem("Run...");
 		organizeRobotsAction = new JMenuItem("Organize robots...");
 		aboutAction = new JMenuItem("About...");
 
 		// create menu items
 		fileMenu = new JMenu("File");
 		simulationMenu = new JMenu("Simulation");
 		helpMenu = new JMenu("Help");
 
 		// init actionListener
 		newTMAction.addActionListener(this);
 		newBFAction.addActionListener(this);
 		openAction.addActionListener(this);
 		saveAction.addActionListener(this);
 		saveAsAction.addActionListener(this);
 		exportLatexAction.addActionListener(this);
 		exitAction.addActionListener(this);
 		runAction.addActionListener(this);
 		organizeRobotsAction.addActionListener(this);
 		aboutAction.addActionListener(this);
 	}
 
 	/**
 	 * Creates a new file.
 	 * @param type The machine type to be created
 	 */
 	public void newFile(Machine.MachineType type) {
 		this.closeCurrentFile();
 		switch(type) {
 		case TuringMachine:
 			gui.turing.NewTMDialogue newTMDialogue = new gui.turing.NewTMDialogue();
 			gui.turing.NewTMDialogue.ReturnValue returnValue = newTMDialogue.showDialogue();
 			switch(returnValue){
 			case CREATE:
 				this.currentMachine = new TuringMachine(newTMDialogue.getMachinesName(),newTMDialogue.getNumberOfTapes());
 				this.loadEditor();
 				break;
 			case CANCEL:
 				break;
 			}
 			break;
 		case BrainfuckMachine:
 			this.currentMachine = new BrainfuckMachine();
 			this.loadEditor();
 			break;
 		}
 	}
 
 	/**
 	 * Opens a file.
 	 */
 	public void openFile() {
 		this.closeCurrentFile();
 
 		final JFileChooser fc = new JFileChooser();
 		// set current directory for file chooser
 		try {
 			File currentDirectory = new File(this.lastDir);
 			fc.setCurrentDirectory(currentDirectory);
 		}
 		catch (Throwable e) {
 		}
 
 		// set xml filter for file chooser
 		fc.setFileFilter (new FileFilter() {
 			public boolean accept(File f) {
 				return f.isDirectory() || f.getName().toLowerCase().endsWith(BrainfuckMachine.FILE_EXTENSION);
 			}
 			public String getDescription() {
 				return "Brainfuck programs (" + BrainfuckMachine.FILE_EXTENSION + ")";
 			}
 		});
 		fc.setFileFilter (new FileFilter() {
 			public boolean accept(File f) {
 				return f.isDirectory() || f.getName().toLowerCase().endsWith(TuringMachine.FILE_EXTENSION);
 			}
 			public String getDescription() {
 				return "Turing machines (" + TuringMachine.FILE_EXTENSION + ")";
 			}
 		});
 		fc.setFileFilter (new FileFilter() {
 			public boolean accept(File f) {
 				return f.isDirectory() || f.getName().toLowerCase().endsWith(TuringMachine.FILE_EXTENSION) || f.getName().toLowerCase().endsWith(BrainfuckMachine.FILE_EXTENSION);
 			}
 			public String getDescription() {
 				return "All supported files (" + TuringMachine.FILE_EXTENSION + ", " + BrainfuckMachine.FILE_EXTENSION + ")";
 			}
 		});
 
 		int retVal = fc.showOpenDialog(null);
 		if (retVal == JFileChooser.APPROVE_OPTION) {
 			File selectedFile = fc.getSelectedFile();
 			try {
 				this.lastDir = selectedFile.getCanonicalPath();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			Machine machine = null;
 			if(selectedFile.getName().toLowerCase().endsWith(TuringMachine.FILE_EXTENSION)) {
 				machine = new TuringMachine();
 			}
 			else if(selectedFile.getName().toLowerCase().endsWith(BrainfuckMachine.FILE_EXTENSION)) {
 				machine = new BrainfuckMachine();
 			}
 			else {
 				ErrorDialog.showError("The file '" + selectedFile.getName() + "' couldn't be openend, because the filetype is not supported.");
 				return;
 			}
 			try {
 				machine.load(selectedFile.getAbsolutePath());
 				this.currentMachine = machine;
 				this.currentFile = new File(selectedFile.getAbsolutePath());
 				this.loadEditor();
 			}
 			catch(Exception e) {
 				ErrorDialog.showError("The file '" + selectedFile.getName() + "' couldn't be openend, because the file is corrupt.", e);
 			}
 		}
 	}
 
 	/**
 	 * Saves a file.
 	 */
 	public void saveFile() {
 		if (!(this.currentFile == null)) {
 			try {
 				this.currentMachine.save(this.currentFile.getAbsolutePath());
 			} catch (IOException e) {
 				ErrorDialog.showError("Saving the file '" + this.currentFile.getName() + "' failed because of an I/O error.", e);
 			}
 			catch (RuntimeException e){
 				ErrorDialog.showError("Saving the file '" + this.currentFile.getName() + "' failed because of an unkown error.", e);
 			}
 		}
 		else {
 			this.saveAsFile();
 		}
 	}
 
 	/**
 	 * Saves a file under a certain name.
 	 */
 	public void saveAsFile() {
 		final JFileChooser fc = new JFileChooser();
 		// set current directory for file chooser
 		try {
 			File currentDirectory = new File(this.lastDir);
 			fc.setCurrentDirectory(currentDirectory);
 		}
 		catch (Throwable e) {
 		}
 
 		// set xml filter for file chooser
 		fc.setFileFilter (new FileFilter() {
 			public boolean accept(File f) {
 				return f.isDirectory() || f.getName().toLowerCase().endsWith(currentMachine.getFileExtension());
 			}
 			public String getDescription() {
 				return currentMachine.getFileExtension();
 			}
 		});
 
 		File f = null;
 		if (this.currentFile == null) {
 			f = new File(this.currentMachine.getName() + this.currentMachine.getFileExtension());
 		}
 		else {
 			f = this.currentFile;
 		}
 		fc.setSelectedFile(f);
 		int retVal = fc.showSaveDialog(null);
 		if (retVal == JFileChooser.APPROVE_OPTION) {
 			File selectedFile = fc.getSelectedFile();
 			try {
 				this.lastDir = selectedFile.getCanonicalPath();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			if (selectedFile.exists()){
 				int result = JOptionPane.showConfirmDialog(null, "Do you want to override the file?", "Override", JOptionPane.YES_NO_OPTION);
 				if (result == JOptionPane.NO_OPTION) {
 					saveAsFile();
 					return;
 				} 
 			}
 			try { 
 				this.currentMachine.save(selectedFile.getPath());
 			} catch (IOException e) {
 				ErrorDialog.showError("Saving the file '" + selectedFile.getName() + "' failed because of an I/O error.", e);
 			}
 			catch (RuntimeException e){
 				ErrorDialog.showError("Saving the file '" + selectedFile.getName() + "' failed because of an unkown error.", e);
 			}
 		}
 	}
 
 	/**
 	 * Exports the Turing machine to LaTeX.
 	 */
 	public void exportLatex() {
 		JOptionPane.showMessageDialog(null, "Not implemented yet!");
 	}
 
 	/**
 	 * Closes the editor.
 	 */
 	public void exitEditor() {
 		this.closeCurrentFile();
 		System.exit(0);
 	}
 
 	/**
 	 * Opens the run window.
 	 */
 	public void runSimulation() {
 		RunWindow runWindow = new RunWindow(currentMachine);
 		runWindow.setLocationRelativeTo(null);
 
 		ReturnValue returnValue = runWindow.showDialog();
 		if (returnValue == ReturnValue.RUN) {
 			this.simulationWindow = new SimulationWindow(this.currentMachine, this);
 			this.applySimulationSettings();
 		}
 	}
 
 	/**
 	 * Shows the window to add and remove robots.
 	 */
 	public void organizeRobots() {
 		OrganizeRobots organizeRobotsWindow = new OrganizeRobots();
 		organizeRobotsWindow.setLocationRelativeTo(null);
 		organizeRobotsWindow.showDialog();
 	}
 
 	public void setEditable(boolean editable) {
 		this.currentMachine.getEditor().setEditable(editable);
 		this.runAction.setEnabled(editable);
 		this.openAction.setEnabled(editable);
 		this.newBFAction.setEnabled(editable);
 		this.newTMAction.setEnabled(editable);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == newTMAction) {
 			newFile(Machine.MachineType.TuringMachine);
 		}
 		else if (e.getSource() == newBFAction) {
 			newFile(Machine.MachineType.BrainfuckMachine);
 		}
 		else if (e.getSource() == openAction) {
 			openFile();
 		}
 		else if (e.getSource() == saveAction) {
 			saveFile();
 		}
 		else if (e.getSource() == saveAsAction) {
 			saveAsFile();
 		}
 		else if (e.getSource() == exportLatexAction) {
 			exportLatex();
 		}
 		else if (e.getSource() == runAction) {
 			runSimulation();
 		}
 		else if (e.getSource() == organizeRobotsAction) {
 			organizeRobots();
 		}
 		else if (e.getSource() == aboutAction) {
 			AboutDialog about = new AboutDialog();
 			about.setVisible(true);
 		}
 		else if (e.getSource() == openAction) {
 			openFile();
 		}
 		else if (e.getSource() == exitAction) {
 			exitEditor();
 		}
 	}
 
 	/**
 	 * Loads and prepares the editor for the current machine
 	 */
 	public void loadEditor() {
 		if (!(this.currentFile == null)) {
 			this.setTitle(this.currentFile.getName() + " - " + APP_NAME);
 		}
 		else {
 			this.setTitle(APP_NAME);
 		}
 
 		this.add(this.currentMachine.getEditor());
 
 		int menupos = 1;
 		for (JMenu menu : this.currentMachine.getEditor().getMenus()) {
 			this.menuBar.add(menu, menupos);
 			menupos++;
 		}
 
 		saveAction.setEnabled(true);
 		saveAsAction.setEnabled(true);
 		runAction.setEnabled(true);
 		exportLatexAction.setEnabled(false);
 
 		validate();
 	}
 
 	/**
 	 * Closes the current machine. Must be run before a new machine can be loaded.
 	 */
 	public void closeCurrentFile() {
 		if (this.currentMachine != null) {
 			int result = JOptionPane.showConfirmDialog(null, "Do you want to save the current machine?", "Close file", JOptionPane.YES_NO_OPTION);
 			if (result == JOptionPane.YES_OPTION) {
 				this.saveFile();
 			}
 
 			this.currentFile = null;
 			this.simulationWindow = null;
 			this.setTitle(APP_NAME);
 
 			saveAction.setEnabled(false);
 			saveAsAction.setEnabled(false);
 			runAction.setEnabled(false);
 			exportLatexAction.setEnabled(false);
 
 			for (JMenu menu : this.currentMachine.getEditor().getMenus()) {
 				this.menuBar.remove(menu);
 			}
 			this.remove(this.currentMachine.getEditor());
			this.validate();
 			this.repaint();
 			this.currentMachine = null;
 		}
 	}
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		if (e.getSource() == toggleDelayAction) {
 			this.delay = this.toggleDelayAction.getState();
 		}
 		else if (contains(this.tapeStyleMenuItems.toArray(), e.getSource())) {
 			for (JRadioButtonMenuItem item : this.tapeStyleMenuItems) {
 				item.removeItemListener(this);
 			}
 			
 			for (Component comp : this.tapeStyleMenuItems) {
 				JRadioButtonMenuItem menu = (JRadioButtonMenuItem) comp;
 				menu.setSelected(false);
 			}
 			JRadioButtonMenuItem style = (JRadioButtonMenuItem) e.getSource();
 			style.setSelected(true);
 			this.tapeStyle = style.getText();
 			
 			for (JRadioButtonMenuItem item : this.tapeStyleMenuItems) {
 				item.addItemListener(this);
 			}
 		}
 		
 		this.applySimulationSettings();
 	}
 	
 	private void applySimulationSettings() {
 		if (this.simulationWindow != null) {
 			this.simulationWindow.setDelay(this.delay);
 			this.simulationWindow.setTapeStyle(this.tapeStyle);
 		}
 
 	}
 }
