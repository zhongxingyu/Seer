 package GUI;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 
 import TuringMachine.*;
 import Tape.*;
 import GUI.RunWindow.*;
 import GUI.Brainfuck.*;
import Brainfuck.*;
 
 import java.awt.Toolkit;
 import java.awt.event.*;
 import java.io.*;
 
 /** This class represents an editor for Turing machines
  * 
  * @author David Wille
  * 
  */
 public class Editor extends JFrame implements ActionListener {
 	static final long serialVersionUID = -3667258249137827980L;
 	static final String appName = "Turing Simulator";
 	protected TuringMachine currentMachine;
 	private BrainfuckEditor brainfuckEditor; 
 	private JMenu newSubmenu;
 	private JMenuItem newBFAction;
 	private JMenuItem newTMAction;
 	private JMenuItem openAction;
 	private JMenuItem saveAction;
 	private JMenuItem saveAsAction;
 	private JMenuItem exportLatexAction;
 	private JMenuItem exitAction;
 	private JMenuItem runAction;
 	private JMenuBar menuBar;
 	private JMenu fileMenu;
 	private JMenu simulationMenu;
 	private final JFileChooser fc = new JFileChooser();
 	
 	private int currentFileType;
 	private final int FILETYPE_TM = 0;
 	private final int FILETYPE_BF = 1;	
 	
 	/**
 	 * Constructs the Editor window with all actionListeners and a basic setup
 	 */
 	public Editor() {
 		setTitle("Editor");
 		setSize(800, 800);
 		
 		initEditor();
 		
 		// set current directory for file chooser
 		try {
 			File currentDirectory = new File(new File(".").getCanonicalPath());
     		fc.setCurrentDirectory(currentDirectory);
 		}
 		catch (IOException e) {
 		}
 		
 		// set xml filter for file chooser
 		fc.setFileFilter (new FileFilter() {
 			public boolean accept(File f) {
 				return f.isDirectory() || f.getName().toLowerCase().endsWith( ".bf" );
 			}
 			public String getDescription() {
 				return "*.bf";
 			}
 		});
 		fc.setFileFilter (new FileFilter() {
 			public boolean accept(File f) {
 				return f.isDirectory() || f.getName().toLowerCase().endsWith( ".xml" );
 			}
 			public String getDescription() {
 				return "*.xml";
 			}
 		});
 		
 		menuBar.add(fileMenu);
 		menuBar.add(simulationMenu);
 		
 		// disable actions
 		saveAction.setEnabled(false);
 		saveAsAction.setEnabled(false);
 		exportLatexAction.setEnabled(false);
 		runAction.setEnabled(false);
         
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
 		
 		// menu shortcuts
 		newTMAction.setAccelerator(KeyStroke.getKeyStroke('T', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		newBFAction.setAccelerator(KeyStroke.getKeyStroke('B', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		openAction.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		saveAction.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		exitAction.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		runAction.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 	}
 	
 	/**
 	 * The editor main, which initializes a new editor window
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// try to set look for Linux and Mac OS X
 		try {
 			if (System.getProperties().getProperty("os.name").equals("Linux")) {
 				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
 			}
 			else if (System.getProperties().getProperty("os.name").equals("Mac OS X")) {
 				System.setProperty("com.apple.mrj.application.apple.menu.about.name", Editor.appName);
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
 	 * Initializes the editor
 	 */
 	public void initEditor() {
 		// create menu bar
 		menuBar = new JMenuBar();
 				
 		// set menu bar
 		setJMenuBar(menuBar);
 		
 		// create submenu items
 		newSubmenu = new JMenu("New..");
 		newTMAction = new JMenuItem("New TM");
 		newBFAction = new JMenuItem("New BF");
 		newSubmenu.add(newTMAction);
 		newSubmenu.add(newBFAction);
 		
 		// create menu subitems
 		openAction = new JMenuItem("Open");
 		saveAction = new JMenuItem("Save");
 		saveAsAction = new JMenuItem("Save As...");
 		exportLatexAction = new JMenuItem("Export as LaTeX");
 		exitAction = new JMenuItem("Exit");
 		runAction = new JMenuItem("Run");
 		
 		// create menu items
 		fileMenu = new JMenu("File");
 		simulationMenu = new JMenu("Simulation");
 		
 		// init actionListener
 		newTMAction.addActionListener(this);
 		newBFAction.addActionListener(this);
 		openAction.addActionListener(this);
 		saveAsAction.addActionListener(this);
 		exportLatexAction.addActionListener(this);
 		exitAction.addActionListener(this);
 		runAction.addActionListener(this);
 	}
 	
 	/**
 	 * Creates a new file
 	 */
 	public void newFile() {
 		resetEditor();
 		saveAction.setEnabled(true);
 		saveAsAction.setEnabled(true);
 		runAction.setEnabled(true);
 		switch(currentFileType) {
 			case FILETYPE_TM:
 				exportLatexAction.setEnabled(true);
 				JOptionPane.showMessageDialog(null, "Not implemented yet!");
 				break;
 			case FILETYPE_BF: 
 				brainfuckEditor = new BrainfuckEditor();
 				exportLatexAction.setEnabled(false);
				brainfuckEditor.reset();
 				add(brainfuckEditor);
 				validate();
 				break;
 		}
 	}
 	
 	/**
 	 * Opens a file
 	 */
 	public void openFile() {
 		int retVal = fc.showOpenDialog(null);
 		resetEditor();
 		if (retVal == JFileChooser.APPROVE_OPTION) {
 			File selectedFile = fc.getSelectedFile();
 			if(selectedFile.getName().toLowerCase().endsWith( ".xml" )) {
 				try {
 					currentMachine = TuringMachine.loadFromXML(selectedFile.getPath());
 					saveAction.setEnabled(true);
 					saveAsAction.setEnabled(true);
 					exportLatexAction.setEnabled(true);
 					runAction.setEnabled(true);
 					currentFileType = FILETYPE_TM;
 				}
 				catch (Exception e) {
 					ErrorDialog.showError("The file '" + selectedFile.getName() + "' couldn't be openend, because the file is corrupt.", e);
 				}
 			}
 			else if(selectedFile.getName().toLowerCase().endsWith( ".bf" )) {
 				try {
 					brainfuckEditor = new BrainfuckEditor();
 					brainfuckEditor.openFile(selectedFile);
 					add(brainfuckEditor);
 					saveAction.setEnabled(true);
 					saveAsAction.setEnabled(true);
 					runAction.setEnabled(true);
 					exportLatexAction.setEnabled(false);
 					validate();
 					currentFileType = FILETYPE_BF;
 				}
 				catch(Exception e) {
 					ErrorDialog.showError("The file '" + selectedFile.getName() + "' couldn't be openend, because the file is corrupt.", e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Saves a file
 	 */
 	public void saveFile() {
 		// TODO save
 		switch(currentFileType) {
 			case FILETYPE_TM: 
 				JOptionPane.showMessageDialog(null, "Not implemented yet!");
 				break;
 			case FILETYPE_BF: 
 				JOptionPane.showMessageDialog(null, "Not implemented yet!");
 				break;
 		}
 	}
 	
 	/**
 	 * Saves a file under a certain name
 	 */
 	public void saveAsFile() {
 		int retVal = fc.showSaveDialog(null);
 		if (retVal == JFileChooser.APPROVE_OPTION) {
 			File selectedFile = fc.getSelectedFile();
 			try { //TODO: check if the file already exists and prompt if to save anyway
 				switch(currentFileType) {
 				case FILETYPE_TM:
 					this.currentMachine.saveXML(selectedFile.getPath());
 					break;
 				case FILETYPE_BF:
 					this.brainfuckEditor.saveFile(selectedFile.getPath());
 					break;
 				}
 				
 			} catch (IOException e) {
 			    ErrorDialog.showError("Saving the file '" + selectedFile.getName() + "' failed because of an I/O error.", e);
 			}
 			catch (RuntimeException e){
 			    ErrorDialog.showError("Saving the file '" + selectedFile.getName() + "' failed because of an unkown error.", e);
 			}
 		}
 	}
 	
 	/**
 	 * Exports the Turing machine to LaTeX
 	 */
 	public void exportLatex() {
 		JOptionPane.showMessageDialog(null, "Not implemented yet!");
 	}
 	
 	/**
 	 * Closes the editor
 	 */
 	public void exitEditor() {
 		System.exit(0);
 	}
 	
 	/**
 	 * Opens the run window
 	 */
 	public void runSimulation() {
 		RunWindow runWindow = new RunWindow(currentMachine);
 		runWindow.setLocationRelativeTo(null);
 		
 		ReturnValue returnValue = runWindow.showDialog();
 		if (returnValue == ReturnValue.RUN) {
 			simulate();
 		}
 	}
 	
 	/**
 	 * Simulates the Turing machine
 	 */
 	public void simulate() {
 		System.out.println(this.currentMachine);		
 		//create tapes and write input
 		try {
 			this.currentMachine.initTapes();
 		}
 		catch (TapeException e){
 			try {
 				this.currentMachine.shutdownTapes();
 			} catch (TapeException e1) {
 				System.out.println("Warning: The tapes couldn't be shutdown correctly.");
 				e1.printStackTrace();
 			}
 		    ErrorDialog.showError("The initialization of the tapes failed because of a tape exception.", e);
 		   
 		    return;
 		}
 		catch (RuntimeException e){
 			try {
 				this.currentMachine.shutdownTapes();
 			} catch (TapeException e1) {
 				System.out.println("Warning: The tapes couldn't be shutdown correctly.");
 				e1.printStackTrace();
 			}
 		    ErrorDialog.showError("The initialization of the tapes failed because of an undefined exception.", e);
 
 		    return;
 		}
 		
 		//simulate
 		try {
 			Simulation sim = new Simulation(this.currentMachine);
 			sim.runMachine();
 		}
 		catch (TapeException e){
 		    ErrorDialog.showError("The simulation failed because of a tape exception.", e);
 		}
 		catch (RuntimeException e){
 		    ErrorDialog.showError("The simulation failed because of an undefined exception.", e);
 		}
 		finally {
 			try {
 				this.currentMachine.shutdownTapes();
 			} catch (TapeException e) {
 			    ErrorDialog.showError("Warning: The tapes could't be shutdown correctly.", e);
 			}
 		}
 	}
 
 	/**
 	 * Responds to a clicked button
 	 */
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == newTMAction) {
 			currentFileType = FILETYPE_TM;
 			newFile();
 		}
 		else if (e.getSource() == newBFAction) {
 			currentFileType = FILETYPE_BF;
 			newFile();
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
 		else if (e.getSource() == exitAction) {
 			exitEditor();
 		}
 	}
 	
 	public void resetEditor() {
 		try {
 			remove(brainfuckEditor);
 		}
 		catch(Exception e) {
 			
 		}
 		validate();
 	}
 }
