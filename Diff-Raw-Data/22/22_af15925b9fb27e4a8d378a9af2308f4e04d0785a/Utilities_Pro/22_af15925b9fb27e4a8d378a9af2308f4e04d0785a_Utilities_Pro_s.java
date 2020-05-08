 package com.git.ifly6.UtilitiesPro3;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.FileDialog;
 import java.awt.Font;
 import java.awt.TextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Scanner;
 
 import javax.swing.Box;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.text.DefaultEditorKit;
 
 import com.apple.eawt.Application;
 
 /**
  * Main Class for Utilities Pro 3.x. It initialises the GUI and contains all relevant pieces of data
  * fundamental to the execution of the programme. Furthermore, it contains all necessary
  * ActionListeners and GUI related methods (basically integrating the older Utilities_Pro Interface,
  * Parameters, and Utilities_Pro classes from the last major version of Utilities Pro-2.x)
  * 
  * @author ifly6
  * @version 3.x
  */
 public class Utilities_Pro {
 
 	/**
 	 * Remembers all commands done in the session.
 	 * 
 	 * @since 3.2
 	 */
 	static ArrayList<String> history = new ArrayList<String>();
 
 	/**
 	 * The number which tells us where we are looking in the ArrayList.
 	 * 
 	 * @since 3.2
 	 */
 	static int recall = history.size();
 
 	/**
 	 * Current Directory we are in. Change using our implementation of the CD command, located in
 	 * TextCommands.
 	 * 
 	 * @since 3.0_dev09.03
 	 */
 	public static String currentDir = System.getProperty("user.dir");
 
 	/**
 	 * List of all the internal commands inside a String Array. All unused commands should be stated
 	 * as nulls.
 	 */
 	public static ArrayList<String> commText = new ArrayList<String>();
 
 	/**
 	 * Used for greeting the user. It should be replaced from Unknown to the iNet name of the user
 	 * inside Utilities_Pro.Main
 	 */
 	protected static String computername = "Unknown";
 
 	/**
 	 * Used in the all following File systems, as the user name of the user is not the same
 	 * throughout all computers.
 	 */
 	public static String userName = System.getProperty("user.name");
 
 	/**
 	 * The place to put any files we download. For all OSX computers, it should be exactly the same.
 	 */
 	static String Downloads_DIR = "/Users/" + userName + "/Downloads/";
 
 	/**
 	 * TextField for the input of commands. When command engine is run, it retrieves the contents of
 	 * this field, then processes it.
 	 */
 	private static TextField inputField;
 
 	/**
 	 * The Keyword is like "Sandy Bridge". There is a defined list of them. For 3.x, its is 3.0)
 	 * iceland, 3.1) iceberg, 3.2) icepool, 3.3) skyfall, 3.4) icefield, 3.5) everest, 3.6) icemont,
 	 * 3.7) icewell, 3.8) icedtea
 	 */
 	static String keyword = "skyfall";
 
 	/**
 	 * JTextArea for the output of the log. Receives strings to append to the log from the method
 	 * "log(String)"
 	 */
 	private static JTextArea logText;
 
 	/**
 	 * JTextArea for the output of the programme. Combines the Error and Output Streams into one
 	 * field.
 	 */
 	private static JTextArea outText;
 
 	/**
 	 * Process is declared here to allow other classes to terminate that process should it be
 	 * necessary.
 	 */
 	static Process process;
 
 	/**
 	 * Runtime Handler. Can be called from anywhere to execute a String[]. When we finish a system
 	 * to return a Process, this shared resource will be removed. However, as it appears that it is
 	 * not happening, it will likely never be removed.
 	 */
 	static Runtime rt = Runtime.getRuntime();
 
 	/**
 	 * A place in ~/Library/Application Support/ where we store all of our configuration files.
 	 */
 	static String UtilitiesPro_DIR = "/Users/" + userName
 			+ "/Library/Application Support/Utilities Pro";
 
 	/**
 	 * Naming system is: |major|.|minor|_|revision| or |major|.|minor|_|dev|<#> For the development
 	 * number, it follows |major|.|minor|, but with no revisions.
 	 */
	public static String version = "3.2";
 
 	/**
 	 * As it deals with the GUI's implementation (JTextArea), Java forces its location to be inside
 	 * the GUI's declaration class.
 	 * 
 	 * @author ifly6
 	 * @since 3.0_dev02
 	 * @param which
 	 *            - integer value, determines which JTextArea to clear (1, outText; 2, logText; 3,
 	 *            inputField)
 	 */
 	public static void clearText(int which) {
 		if (which == 1) {
 			outText.setText(null);
 		}
 		if (which == 2) {
 			logText.setText(null);
 		}
 		if (which == 3) {
 			inputField.setText(null);
 		}
 	}
 
 	/**
 	 * @since 3.0_dev07
 	 * @param in
 	 *            - String to append with the bash prompt to JTextArea outText. Also appends to
 	 *            logText.
 	 */
 	public static void command(String in) {
 
 		// Get Name of Current Directory (as we now use Canonical names)
 		String[] directories = currentDir.split("/");
 		int temp = (directories.length) - 1;
 
 		try {
 			outText.append("\n" + computername + ":" + directories[temp]
 					+ " $ " + in);
 			log(computername + ":" + directories[temp] + " $ " + in);
 		} catch (ArrayIndexOutOfBoundsException e) {
 			outText.append("\n" + computername + ": $ " + in);
 			log(computername + ": $ " + in);
 		}
 	}
 
 	/**
 	 * @since 3.0_dev05
 	 * @return a string with the contents of TextArea inputField
 	 */
 	public static String getInputField() {
 		String contents = inputField.getText();
 		return contents;
 	}
 
 	/**
 	 * @since 3.0_dev02
 	 * @return JTextArea logText
 	 */
 	public static JTextArea getLogText() {
 		return logText;
 	}
 
 	/**
 	 * @since 3.0_dev02
 	 * @return JTextArea OutText
 	 */
 	public static JTextArea getOutText() {
 		return outText;
 	}
 
 	/**
 	 * @since 2.2_01
 	 * @param in
 	 *            - String to append (with a date) into the JTextArea logText
 	 */
 	public static void log(String in) {
 		logText.append("\n" + new Date() + " " + in);
 	}
 
 	/**
 	 * Launch the application. Executes on a pipeline, going first to read the GUI configuration
 	 * file, with the Look and Feel of the GUI. Then initialises the GUI.
 	 * 
 	 * @param inputArgs
 	 *            - there are no command-line arguments.
 	 */
 	@SuppressWarnings("deprecation")
 	public static void main(String[] inputArgs) {
 
 		// Set Properties before GUI Calls
 		System.setProperty("apple.laf.useScreenMenuBar", "true");
 		System.setProperty("com.apple.mrj.application.apple.menu.about.name",
 				"Utilities Pro");
 
 		// Handling Mac Toolbar System
 		Application macApp = Application.getApplication();
 		MacHandler macAdapter = new MacHandler();
 		macApp.addApplicationListener(macAdapter);
 		macApp.setEnabledPreferencesMenu(true);
 
 		// Read Configuration
 		String look = "Default";
 		try {
 			FileReader configRead = new FileReader(UtilitiesPro_DIR
 					+ "/config.txt");
 			Scanner scan = new Scanner(configRead);
 
 			// String = Lines in Order.
 			look = scan.nextLine();
 			scan.close(); // Close Scanner
 
 		} catch (FileNotFoundException e1) {
 			// If Configuration is not found, Do this stuff.
 			try {
 				UIManager.setLookAndFeel(UIManager
 						.getSystemLookAndFeelClassName());
 			} catch (ClassNotFoundException e) {
 			} catch (InstantiationException e) {
 			} catch (IllegalAccessException e) {
 			} catch (UnsupportedLookAndFeelException e) {
 			}
 		}
 
 		// GUI Look and Feel
 		if (look.equals("CrossPlatformLAF")) {
 			try {
 				UIManager.setLookAndFeel(UIManager
 						.getCrossPlatformLookAndFeelClassName());
 			} catch (ClassNotFoundException e) {
 			} catch (InstantiationException e) {
 			} catch (IllegalAccessException e) {
 			} catch (UnsupportedLookAndFeelException e) {
 			}
 		} else {
 			try {
 				UIManager.setLookAndFeel(UIManager
 						.getSystemLookAndFeelClassName());
 			} catch (ClassNotFoundException e) {
 			} catch (InstantiationException e) {
 			} catch (IllegalAccessException e) {
 			} catch (UnsupportedLookAndFeelException e) {
 			}
 		}
 
 		// Create Configuration Directory
 		Utilities_Pro.mkdir();
 
 		// Populate the List of Internal Commands
 		setCommands();
 
 		// Get the name of the Computer
 		try {
 			computername = InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException e) {
 			try {
 				computername = InetAddress.getLocalHost().getHostName();
 			} catch (UnknownHostException e1) {
 				System.out.println("Localhost Name-Get failed.");
 			}
 		}
 
 		// Launch the GUI.
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Utilities_Pro window = new Utilities_Pro();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Used to create (if necessary) all folders for Utilities Pro. Creates ~/Library/Application
 	 * Support/Utilities Pro folder and verifies that ~/Downloads exists. This programme should be
 	 * run on a Mac, as both are only applicable under the File Structure of one (or very similar
 	 * Linux distributions)
 	 * 
 	 * @author ifly6
 	 * @since 2.2_01
 	 */
 	public static void mkdir() {
 		File folder = new File(UtilitiesPro_DIR);
 		folder.mkdirs();
 		folder = new File(Downloads_DIR);
 		folder.mkdirs();
 	}
 
 	/**
 	 * Added in version 2.2_02 of Utilities Pro. Replaces 'append'. Difference is that it
 	 * automatically formats the text.
 	 * 
 	 * @since 2.2_02
 	 * @param in
 	 *            - String to append (with a space) into the JTextArea outText
 	 * @see com.me.ifly6.UtilitiesPro2.ConsoleIf
 	 */
 	public static void out(String in) {
 		outText.append("\n " + in);
 		Utilities_Pro.getOutText().setCaretPosition(
 				getOutText().getDocument().getLength());
 	}
 
 	/**
 	 * Sets the arrayList of commands, as they are not hardcoded. This saves us a lot of problems.
 	 * Don't remove it.
 	 * 
 	 * @since 3.0_dev08
 	 */
 	private static void setCommands() {
 		commText.add("/changelog");
 		commText.add("/about");
 		commText.add("/help");
 		commText.add("/clear");
 		commText.add("/licence");
 		commText.add("/save");
 		commText.add("/saveLog");
 		commText.add("/generateConfig");
 		commText.add("/sysInfo");
 		commText.add("/mindterm");
 		commText.add("/execscript");
 		commText.add("/terminate");
 		commText.add("/quit");
 	}
 
 	private JFrame frame;
 
 	/**
 	 * Create instance of the application.
 	 */
 	public Utilities_Pro() {
 		initialize();
 	}
 
 	/**
 	 * This system starts the main GUI for the programme. It also contains all GUI data for the
 	 * programme, causing a necessity for the method getters and setters which are evident below.
 	 * 
 	 * @param frame
 	 *            - JFrame for the programme
 	 * @param panel
 	 *            - Panel for the Utilities_Pro's Tab
 	 * @param scrollPane_logText
 	 *            - Pane for the Logging Tab
 	 * @param outText
 	 *            - JTextArea for the Utilities_Pro's output
 	 * @param logText
 	 *            - JTextArea for the Logging's output
 	 * @param inputField
 	 *            - TextField (AWT) for input into the programme
 	 */
 	private void initialize() {
 		frame = new JFrame();
 
 		frame.setBounds(0, 0, 670, 735);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setTitle("Utilities Pro " + version);
 
 		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
 		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
 
 		JPanel panel = new JPanel();
 		tabbedPane.addTab("Console", null, panel, null);
 		panel.setLayout(new BorderLayout(0, 0));
 
 		inputField = new TextField();
 		inputField.setFont(new Font("Monaco", Font.PLAIN, 11));
 		panel.add(inputField, BorderLayout.SOUTH);
 		inputField.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				int keyCode = e.getKeyCode();
 				if (keyCode == KeyEvent.VK_ENTER) {
 					TextCommands.processInputField();
 				}
				if (keyCode == KeyEvent.VK_UP) {
					recall--;
					inputField.setText(history.get(recall));
				}
				if (keyCode == KeyEvent.VK_DOWN) {
					recall++;
					inputField.setText(history.get(recall));
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 			}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 		});
 
 		outText = new JTextArea();
 		outText.setEditable(false);
 		outText.setFont(new Font("Monaco", Font.PLAIN, 11));
 		JScrollPane scrollPane_outPane = new JScrollPane(outText);
 		scrollPane_outPane.setViewportBorder(new EmptyBorder(5, 5, 5, 5));
 		panel.add(scrollPane_outPane, BorderLayout.CENTER);
 
 		logText = new JTextArea();
 		logText.setEditable(false);
 		logText.setFont(new Font("Monaco", Font.PLAIN, 11));
 		JScrollPane scrollPane_logText = new JScrollPane(logText);
 		tabbedPane.addTab("Log", null, scrollPane_logText,
 				"Shows a dynamic log of all functions run.");
 
 		final FileDialog fileDialog = new FileDialog(frame);
 
 		JMenuBar menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 
 		JMenuItem mntmOpenConfig = new JMenuItem("Open Configuration Folder");
 		mntmOpenConfig.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				command("File>Open Configuration Folder");
 				FileCommands.configManage(1);
 			}
 		});
 		mnFile.add(mntmOpenConfig);
 
 		JMenuItem mntmDeleteConfig = new JMenuItem("Delete Configuration");
 		mntmDeleteConfig.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("File>Delete Configuration");
 				FileCommands.configManage(2);
 			}
 		});
 		mnFile.add(mntmDeleteConfig);
 
 		JSeparator separator = new JSeparator();
 		mnFile.add(separator);
 
 		JMenuItem mntmExportConsole = new JMenuItem("Export Console\n");
 		mntmExportConsole.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("File>Export Utilities_Pro");
 				FileCommands.export(1);
 			}
 		});
 		mnFile.add(mntmExportConsole);
 
 		JMenuItem mntmConsoleLog = new JMenuItem("Export Log");
 		mntmConsoleLog.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("File>Export Log");
 				FileCommands.export(2);
 			}
 		});
 		mnFile.add(mntmConsoleLog);
 
 		JMenu mnEdit = new JMenu("Edit");
 		menuBar.add(mnEdit);
 
 		JMenuItem mntmCut = new JMenuItem(new DefaultEditorKit.CutAction());
 		mntmCut.setText("Cut");
 		mntmCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
 				ActionEvent.META_MASK));
 		mnEdit.add(mntmCut);
 
 		JMenuItem mntmCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
 		mntmCopy.setText("Copy");
 		mntmCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
 				ActionEvent.META_MASK));
 		mnEdit.add(mntmCopy);
 
 		JMenuItem mntmPaste = new JMenuItem(new DefaultEditorKit.PasteAction());
 		mntmPaste.setText("Paste");
 		mntmPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
 				ActionEvent.META_MASK));
 		mnEdit.add(mntmPaste);
 
 		JSeparator separator_4 = new JSeparator();
 		mnEdit.add(separator_4);
 
 		JMenuItem mntmClearConsole = new JMenuItem("Clear Console");
 		mntmClearConsole.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// There is no need for command log here.
 				EditCommands.consoleClear();
 			}
 		});
 		mnEdit.add(mntmClearConsole);
 
 		JMenuItem mntmClearLog = new JMenuItem("Clear Log");
 		mntmClearLog.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// There is no need for command log here.
 				EditCommands.logClear();
 			}
 		});
 		mnEdit.add(mntmClearLog);
 
 		JMenu mnScripts = new JMenu("Scripts");
 		menuBar.add(mnScripts);
 
 		JMenuItem mntmPurge = new JMenuItem("Purge Memory");
 		mntmPurge.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Scripts>Purge Memory");
 				ScriptCommands.purge();
 			}
 		});
 		mnScripts.add(mntmPurge);
 
 		JMenuItem mntmRestartAirport = new JMenuItem("Restart Airport");
 		mntmRestartAirport.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Scripts>Restart Airport");
 				ScriptCommands.wireless();
 			}
 		});
 		mnScripts.add(mntmRestartAirport);
 
 		JSeparator separator_1 = new JSeparator();
 		mnScripts.add(separator_1);
 
 		JMenuItem mntmSystemInfo = new JMenuItem("System Information");
 		mntmSystemInfo.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Scripts>System Information");
 				ScriptCommands.readout();
 			}
 		});
 		mnScripts.add(mntmSystemInfo);
 
 		JMenuItem mntmDownloadMindterm = new JMenuItem("Download Mindterm");
 		mntmDownloadMindterm.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Scripts>Download Mindterm");
 				ScriptCommands.mindterm();
 			}
 		});
 		mnScripts.add(mntmDownloadMindterm);
 
 		JSeparator separator_5 = new JSeparator();
 		mnScripts.add(separator_5);
 
 		JMenuItem mntmLoadAndExecute = new JMenuItem("Load and Execute Script");
 		mntmLoadAndExecute.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				command("Scripts>Load and Exec Script");
 				fileDialog.setVisible(true);
 				File selScript = new File(fileDialog.getDirectory()
 						+ fileDialog.getFile());
 				try {
 					ExecEngine.scriptEngine(selScript.getCanonicalPath());
 				} catch (IOException e) {
 					out("Script Load Failed.");
 					log("Script Load Failed for: " + selScript);
 				}
 			}
 		});
 		mnScripts.add(mntmLoadAndExecute);
 
 		JMenu mnCommand = new JMenu("Command");
 		menuBar.add(mnCommand);
 
 		JMenuItem mntmTerminateUtilitiesPro = new JMenuItem(
 				"Terminate Utilities Pro Process");
 		mntmTerminateUtilitiesPro.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Command>Terminate Utilities Pro Process");
 				CommandCommands.terminateUtility();
 			}
 		});
 		mntmTerminateUtilitiesPro.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_BACK_SPACE, ActionEvent.META_MASK));
 		mnCommand.add(mntmTerminateUtilitiesPro);
 
 		JMenuItem mntmTerminateArbitraryProcess = new JMenuItem(
 				"Terminate Arbitrary Process");
 		mntmTerminateArbitraryProcess.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Command>Terminate Arbitrary Process");
 				CommandCommands.terminateChoose();
 			}
 		});
 		mnCommand.add(mntmTerminateArbitraryProcess);
 
 		JSeparator separator_3 = new JSeparator();
 		mnCommand.add(separator_3);
 
 		JMenuItem mntmBombard = new JMenuItem("Bombard");
 		mntmBombard.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Command>Bombard");
 				CommandCommands.bombard();
 			}
 		});
 		mnCommand.add(mntmBombard);
 
 		Component horizontalGlue = Box.createHorizontalGlue();
 		menuBar.add(horizontalGlue);
 
 		JMenu mnHelp = new JMenu("Help");
 		menuBar.add(mnHelp);
 
 		JMenuItem mntmAbout = new JMenuItem("About");
 		mntmAbout.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Help>About");
 				HelpCommands.about();
 			}
 		});
 		mnHelp.add(mntmAbout);
 
 		JMenuItem mntmHelp = new JMenuItem("Utilities Pro Help");
 		mntmHelp.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Help>Utilities Pro Help");
 				HelpCommands.helpList();
 			}
 		});
 		mnHelp.add(mntmHelp);
 
 		JMenuItem mntmBashHelp = new JMenuItem("Bash Help");
 		mntmBashHelp.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Help>Bash Help");
 				HelpCommands.bashHelp();
 			}
 		});
 		mnHelp.add(mntmBashHelp);
 
 		JSeparator separator_2 = new JSeparator();
 		mnHelp.add(separator_2);
 
 		JMenuItem mntmUpdate = new JMenuItem("Update");
 		mntmUpdate.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				command("Help>Update");
 				HelpCommands.update();
 			}
 		});
 		mnHelp.add(mntmUpdate);
 
 		// MacAddrLog init = new MacAddrLog();
 		// init.parameters();
 
 		String greet = "Welcome, " + userName + ", to Utilities Pro - "
 				+ version + " '" + keyword + "'\n===========";
 		outText.append(greet);
 	}
 
 	protected static void addToHistory(String input) {
 		history.add(input);
 		recall = history.size();
 	}
 }
