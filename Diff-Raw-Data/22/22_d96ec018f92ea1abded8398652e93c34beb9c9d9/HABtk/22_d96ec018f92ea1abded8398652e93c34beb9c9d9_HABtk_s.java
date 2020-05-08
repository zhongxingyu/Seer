 package com.aerodynelabs.habtk;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.noos.xing.mydoggy.ContentManager;
 import org.noos.xing.mydoggy.ToolWindow;
 import org.noos.xing.mydoggy.ToolWindowAnchor;
 import org.noos.xing.mydoggy.ToolWindowType;
 import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
 import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyMultiSplitContentManagerUI;
 import org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel;
 
 import com.aerodynelabs.habtk.help.HelpWindow;
 import com.aerodynelabs.habtk.prediction.Predictor;
 import com.aerodynelabs.habtk.ui.AboutDialog;
 import com.aerodynelabs.habtk.ui.PredictionPanel;
 import com.aerodynelabs.habtk.ui.TerminalPanel;
 import com.aerodynelabs.habtk.ui.TrackingPanel;
 
 /**
  * The main class of HABtk
  * @author eharstad
  * 
  */
 public class HABtk {
 	
 	private static final String VERSION = "0.01 Alpha";
 	
 	private static final Logger debugLog = Logger.getLogger("Debug");
 	private static FileHandler logFile;
 	private static SimpleFormatter logFormatter;
 	
 	private static JFrame window;
 	private static MyDoggyToolWindowManager windowManager;
 	private static ContentManager contentManager;
 	
 	private static BalloonFlight flight = new BalloonFlight();
 	
 	/**
 	 * Create GUI Components
 	 */
 	private static void setup() {
 		// Configure window
 		window = new JFrame("HABtk - " + VERSION);
 		window.setMinimumSize(new Dimension(800, 600));
 		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		// Create components
 		JMenuBar menuBar = new JMenuBar();
 		JMenu fileMenu = new JMenu("File");
 		JMenu helpMenu = new JMenu("Help");
 		menuBar.add(fileMenu);
 		menuBar.add(helpMenu);
 		window.setJMenuBar(menuBar);
 		
 		JMenuItem fileNewFlightItem = new JMenuItem("New Flight");
 		fileNewFlightItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Predictor flightPredictor = Predictor.create();
 				if(flightPredictor != null) flightPredictor.save();
 				flight.setPredictor(flightPredictor);
 			}
 		});
 		fileMenu.add(fileNewFlightItem);
 		
 		JMenuItem fileLoadFlightItem = new JMenuItem("Load Flight");
 		fileLoadFlightItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Predictor flightPredictor = Predictor.load();
 				flight.setPredictor(flightPredictor);
 			}
 		});
 		fileMenu.add(fileLoadFlightItem);
 		
 		fileMenu.add(new JSeparator());
 		
 		JMenuItem fileSaveItem = new JMenuItem("Save Map");
 		fileSaveItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//TODO save map action
 			}
 		});
 		fileMenu.add(fileSaveItem);
 		
 		JMenuItem filePrintItem = new JMenuItem("Print Map");
 		filePrintItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//TODO print map action
 			}
 		});
 		fileMenu.add(filePrintItem);
 		
 		fileMenu.add(new JSeparator());
 		JMenuItem fileExitItem = new JMenuItem("Exit");
 		fileExitItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				window.setVisible(false);
 				exit();
 			}
 		});
 		fileMenu.add(fileExitItem);
 		
 		JMenuItem helpHelpItem = new JMenuItem("Help Contents");
 		helpHelpItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				HelpWindow.showHelp();
 			}
 		});
 		helpMenu.add(helpHelpItem);
 		
 		JMenuItem helpUpdateItem = new JMenuItem("Check for Updates");
 		helpUpdateItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//TODO help update action
 				new Updater();
 			}
 		});
 		helpMenu.add(helpUpdateItem);
 		
 		JMenuItem helpAboutItem = new JMenuItem("About");
 		helpAboutItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new AboutDialog(window);
 			}
 		});
 		helpMenu.add(helpAboutItem);
 		
 		windowManager = new MyDoggyToolWindowManager();
 		contentManager = windowManager.getContentManager();
 		contentManager.setContentManagerUI(new MyDoggyMultiSplitContentManagerUI());
 		
 		windowManager.registerToolWindow("Log", "Log", null, new TerminalPanel(), ToolWindowAnchor.BOTTOM);
 		windowManager.registerToolWindow("Tracking", "Tracking", null, new TrackingPanel(), ToolWindowAnchor.LEFT);
		windowManager.registerToolWindow("Prediction", "Prediction", null, new PredictionPanel(windowManager), ToolWindowAnchor.LEFT);
 		for(ToolWindow win : windowManager.getToolWindows()) win.setAvailable(true);
 		windowManager.getToolWindow("Log").setType(ToolWindowType.SLIDING);
 		
 		window.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowStateChanged(WindowEvent e) {
 				System.out.println("State change");
 			}
 			
 			@Override
 			public void windowIconified(WindowEvent e) {
 				System.out.println("Iconified");
 			}
 			
 			@Override
 			public void windowOpened(WindowEvent e) {
 				try {
 					File workspace = new File("workspace.xml");
 					if(workspace.exists()) {
 						FileInputStream in = new FileInputStream(workspace);
 						windowManager.getPersistenceDelegate().apply(in);
 						in.close();
 					}
 				} catch(Exception e1) {
 					debugLog.log(Level.SEVERE, "Exception", e);
 				}
 			}
 			
 			@Override
 			public void windowClosing(WindowEvent e) {
 				exit();
 			}
 		});
 		
 		window.getContentPane().add(windowManager);
 		window.pack();
 		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
 	}
 	
 	/**
 	 * Save state and exit
 	 */
 	private static void exit() {
 		try {
 			FileOutputStream out = new FileOutputStream("workspace.xml");
 			windowManager.getPersistenceDelegate().save(out);
 			out.close();
 		} catch(Exception e1) {
 			debugLog.log(Level.SEVERE, "Exception", e1);
 		}
 		window.dispose();
 		System.exit(0);
 	}
 	
 	/**
 	 * Create a global logger
 	 * @param level Severity threshold
 	 */
 	private static void setupLogger(int level) {
 		switch(level) {
 			case 0:
 				debugLog.setLevel(Level.ALL);
 				break;
 			case 1:
 				debugLog.setLevel(Level.SEVERE);
 				break;
 			case 2:
 				debugLog.setLevel(Level.WARNING);
 				break;
 			case 3:
 				debugLog.setLevel(Level.INFO);
 				break;
 			default:
 				debugLog.setLevel(Level.OFF);
 		}
 		
 		try {
 			logFile = new FileHandler("log.txt");
 		} catch(Exception e) {
 			debugLog.log(Level.SEVERE, "Exception", e);
 		}
 		logFormatter = new SimpleFormatter();
 		logFile.setFormatter(logFormatter);
 		debugLog.addHandler(logFile);
 	}
 
 	/**
 	 * Main method
 	 * @param args Command line arguments
 	 */
 	public static void main(String[] args) {
 		if(args.length == 0) {
 			setupLogger(9);
 		} else {
 			setupLogger(Integer.parseInt(args[0]));
 		}
 	
 		JFrame.setDefaultLookAndFeelDecorated(true);
 		JDialog.setDefaultLookAndFeelDecorated(true);
 		
 		new HABtk();
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				public void run() {
 					try {
 						UIManager.setLookAndFeel(new SubstanceTwilightLookAndFeel());
 					} catch (UnsupportedLookAndFeelException e) {
 						e.printStackTrace();
 					}
 					setup();
 					
 					window.setVisible(true);
 				}
 			});
 		} catch(Exception e) {
 			debugLog.log(Level.SEVERE, "Exception", e);
 		}
 	}
 
 }
