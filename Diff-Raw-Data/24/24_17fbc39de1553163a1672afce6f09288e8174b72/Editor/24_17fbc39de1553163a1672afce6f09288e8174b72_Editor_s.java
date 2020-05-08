 package GUI;
 import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 
 import TuringMachine.*;
 import Tape.*;
 
 import java.awt.Toolkit;
 import java.awt.event.*;
 import java.io.*;
 import java.util.ArrayList;
 
 /** This class represents an editor for Turing machines
  * 
  * @author David Wille
  * 
  */
 public class Editor extends JFrame implements ActionListener {
 	static final long serialVersionUID = -3667258249137827980L;
 	static final String appName = "Turing Simulator";
 	protected TuringMachine currentMachine;
 	private JMenuItem newAction;
 	private JMenuItem openAction;
 	private JMenuItem saveAction;
 	private JMenuItem exportLatexAction;
 	private JMenuItem exitAction;
 	private JMenuItem runAction;
 	private JMenuItem testAction;
 	private JMenuBar menuBar;
 	private JMenu fileMenu;
 	private JMenu simulationMenu;
 	private final JFileChooser fc = new JFileChooser();
 	
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
 		exportLatexAction.setEnabled(false);
 		runAction.setEnabled(false);
         
 		// add menu subitems
 		fileMenu.add(newAction);
 		fileMenu.add(openAction);
 		fileMenu.add(saveAction);
 		fileMenu.add(new JSeparator());
 		fileMenu.add(exportLatexAction);
 		fileMenu.add(new JSeparator());
 		fileMenu.add(exitAction);
 		simulationMenu.add(runAction);
 		simulationMenu.add(testAction);
 		
 		// menu shortcuts
 		newAction.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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
 		// try to set look for Linux
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
 	
 	public void initEditor() {
 		// create menu bar
 		menuBar = new JMenuBar();
 				
 		// set menu bar
 		setJMenuBar(menuBar);
 		
 		// create menu subitems
 		newAction = new JMenuItem("New");
 		openAction = new JMenuItem("Open");
 		saveAction = new JMenuItem("Save");
 		exportLatexAction = new JMenuItem("Export as LaTeX");
 		exitAction = new JMenuItem("Exit");
 		runAction = new JMenuItem("Run");
 		testAction = new JMenuItem("Test");
 		
 		// create menu items
 		fileMenu = new JMenu("File");
 		simulationMenu = new JMenu("Simulation");
 		
 		// init actionListener
 		newAction.addActionListener(this);
 		openAction.addActionListener(this);
 		saveAction.addActionListener(this);
 		exportLatexAction.addActionListener(this);
 		exitAction.addActionListener(this);
 		runAction.addActionListener(this);
 		testAction.addActionListener(this);
 	}
 	
 	public void newFile() {
 		JOptionPane.showMessageDialog(null, "Not implemented yet!");
 		saveAction.setEnabled(true);
 		exportLatexAction.setEnabled(true);
 		runAction.setEnabled(true);
 	}
 	
 	public void openFile() {
 		int retVal = fc.showOpenDialog(null);
 		if (retVal == JFileChooser.APPROVE_OPTION) {
 				File selectedFile = fc.getSelectedFile();
 			try {
 				currentMachine = TuringMachine.loadFromXML(selectedFile.getName());
 			}
 			catch (Exception e) {
 				ErrorDialog.showError("The file '" + selectedFile.getName() + "' couldn't be openend, because the file is corrupt.", e);
 			}
 			saveAction.setEnabled(true);
 			exportLatexAction.setEnabled(true);
 			runAction.setEnabled(true);
 		}
 	}
 	
 	public void saveFile() {
 		int retVal = fc.showSaveDialog(null);
 		if (retVal == JFileChooser.APPROVE_OPTION) {
 			JOptionPane.showMessageDialog(null, "Saved successfully!");
 		}
 		//InOut.writeXMLFromFile(selectedFile.getName(), currentMachine);
 	}
 	
 	public void exportLatex() {
 		JOptionPane.showMessageDialog(null, "Not implemented yet!");
 	}
 	
 	public void exitEditor() {
 		System.exit(0);
 	}
 	
 	public void runSimulation() {
 		RunWindow runWindow = new RunWindow(currentMachine);
 		runWindow.setVisible(true);
 		runWindow.setLocationRelativeTo(null);
 	}
 	
 	public void simulate() {
		System.out.println(this.currentMachine);
		ArrayList<Tape> tapes = new ArrayList<Tape>();
		
 		//create tapes and write input
 		try {
 			//TODO: remove old code
 //			for (int i=0; i < this.currentMachine.getTapes(); i++) {
 //				String type = JOptionPane.showInputDialog("Enter 'LEGO' or 'PC':");
 //				if (type.equals("LEGO")) {
 //					MasterRobot ips_03 = new MasterRobot("IPS_03", "00:16:53:13:53:BB");
 //					SlaveRobot nxt_03 = new SlaveRobot("NXT_03", "00:16:53:0F:DB:8E");
 //					Tape tape_lego = new LEGOTape(ips_03, nxt_03);
 //					tape_lego.init();
 //					tapes.add(tape_lego);
 //					this.writeInputWordToTape(tape_lego, this.currentMachine.getInitial().get(i));
 //				}
 //				else if (type.equals("PC")) {
 //					Tape tape_console = new ConsoleTape();
 //					tape_console.init();
 //					tapes.add(tape_console);
 //					this.writeInputWordToTape(tape_console, this.currentMachine.getInitial().get(i));
 //				}
 //				else {
 //				    ErrorDialog.showError("If you are too stupid to enter one of the words 'LEGO' or 'PC', you shouldn't use this program.");
 //					return;
 //				}
 //			}
 			this.currentMachine.initTapes();
 		}
 		catch (TapeException e){
 		    ErrorDialog.showError("The initialization of the tapes failed because of a Tape exception (" + e.getMessage() + ").", e);
 		    return;
 		}
 		catch (RuntimeException e){
 		    ErrorDialog.showError("The initialization of the tapes failed because of an undefined exception.", e);
 		    return;
 		}
 		
 		//simulate
 		try {
 			Simulation sim = new Simulation(this.currentMachine);
 			sim.runMachine();
 		}
 		catch (TapeException e){
 		    ErrorDialog.showError("The simulation failed because of a Tape exception (" + e.getMessage() + ").", e);
 		}
 		catch (RuntimeException e){
 		    ErrorDialog.showError("The simulation failed because of an undefined exception.", e);
 		}
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == newAction) {
 			newFile();
 		}
 		else if (e.getSource() == openAction) {
 			openFile();
 		}
 		else if (e.getSource() == saveAction) {
 			saveFile();
 		}
 		else if (e.getSource() == exportLatexAction) {
 			exportLatex();
 		}
 		else if (e.getSource() == runAction) {
 			runSimulation();
 		}
 		else if (e.getSource() == testAction) {
 			simulate();
 		}
 	}
 	
 }
