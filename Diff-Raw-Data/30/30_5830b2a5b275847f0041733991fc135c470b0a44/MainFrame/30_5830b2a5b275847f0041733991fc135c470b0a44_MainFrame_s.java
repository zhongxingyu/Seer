 /*
  * Emperior
  * Copyright 2010 and beyond, Marvin Steinberg.
  *
  * caps is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License version 3
  * as published by the Free Software Foundation.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  */
 
 
 
 import java.awt.BorderLayout;
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 
 import java.awt.AWTEvent;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Set;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JToolBar;
 import javax.swing.JButton;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.undo.UndoManager;
 
 import jsyntaxpane.DefaultSyntaxKit;
 import javax.swing.JTextPane;
 
 public class MainFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private JPanel mainPanel = null;
 	public static FileTree jTree = null;
 	private JMenuBar menu = null;
 	public static JTabbedPane jTabbedPane = null;
 	static HashMap<String, Boolean> openedFiles = new HashMap<String, Boolean>(); // @jve:decl-index=0:
 	static HashMap<String, JEditorPane> editors = new HashMap<String, JEditorPane>();
 	static HashMap<String, UndoManager> undoManagers = new HashMap<String, UndoManager>();
 	private JToolBar jToolBar = null;
 	private JTextPane jConsoleTextPane = null;
 	private JScrollPane jConsoleScrollPane = null;
 	private JButton pauseResumeButton;
 	private boolean isPaused = false;
 	private SearchDialog searchDialog;
 	private String jUnitOutput = "";  //  @jve:decl-index=0:
 	private String batFilePath = "testbatchfiles";  //  @jve:decl-index=0:
 	private String experimentFilesFolderPath = "";  //  @jve:decl-index=0:
 	private String runBatFilePath = "runbatchfiles";
 	public static ConsolePane consolePane;
 	public static HashMap<String, String> editorContentType = new HashMap<String, String>();  //  @jve:decl-index=0:
 	public static boolean actionPerformed = false;
 	public static ImageIcon unsavedIcon = new ImageIcon(MainFrame.class.getResource("/files/icons/unsaved-icon.png"));  //  @jve:decl-index=0:
 	
 	
 	
 	/**
 	 * This is the default constructor
 	 */
 	public MainFrame() {
 		super();
 		
 	}
 	
 	public void init(){
 		initialize();
 		this.setExtendedState(Frame.MAXIMIZED_BOTH);
 		setGlobalListeners();
 		setEditorContentTypes();
 	}
 
 	
 	public String getExperimentFilesFolderPath() {
 		return experimentFilesFolderPath;
 	}
 
 	public void setExperimentFilesFolderPath(String experimentFilesFolderPath) {
 		this.experimentFilesFolderPath = experimentFilesFolderPath;
 	}
 
 	public String getBatFilePath() {
 		return batFilePath;
 	}
 
 	public void setBatFilePath(String batFilePath) {
 		this.batFilePath = batFilePath;
 	}
 	
 	public String getRunBatFilePath() {
 		return runBatFilePath;
 	}
 
 	public void setRunBatFilePath(String runBatFilePath) {
 		this.runBatFilePath = runBatFilePath;
 	}
 
 	private void setEditorContentTypes() {
 		editorContentType.put("java", "text/java");
 		editorContentType.put("groovy", "text/groovy");
 	}
 
 	private void setGlobalListeners() {
 		Toolkit.getDefaultToolkit().getSystemEventQueue().push(
 				new EventQueue() {
 					@Override
 					protected void dispatchEvent(AWTEvent event) {
 						if (event instanceof KeyEvent) {
 							KeyEvent keyEvent = (KeyEvent) event;
 
 							// Strg + S
 							if ((keyEvent.getID() == KeyEvent.KEY_PRESSED)
 									&& ((keyEvent).getKeyCode() == KeyEvent.VK_S)
 									&& (keyEvent.getModifiers() == 2)) {
 								saveSelectedFile();
 								Main.removeTabbedPaneIcon();
 							}
 						}
 						super.dispatchEvent(event);
 					}
 				});
 
 	}
 
 
 	
 	protected void saveFile(String filePath, String fileContent) {
 		try {
 			Main.addLineToLogFile("[File] saving: " + filePath);
 			// Create file
 			FileWriter fstream = new FileWriter(filePath);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(fileContent);
 			// Close the output stream
 			out.close();
 			
 			
 		} catch (Exception e) {// Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 
 	protected void saveAllFiles() {
 		Set<String> openedFilesNames = openedFiles.keySet();
 		JEditorPane editor;
 		for (String openedFileName : openedFilesNames) {
 			editor = editors.get(openedFileName);
 			saveFile(openedFileName, editor.getText());
 		}
 		
 		for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
 			jTabbedPane.setIconAt(i, null);
 		}
 		
 		try {
 			Main.copyFiles(openedFilesNames, Main.getTargetLocation());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void saveSelectedFile() {
 		if (jTabbedPane.getTabCount() > 0) {
 			int selectedTab = jTabbedPane.getSelectedIndex();
 			String filePath = jTabbedPane.getToolTipTextAt(selectedTab);
 			JEditorPane editor = editors.get(filePath);
 			saveFile(filePath, editor.getText());
 			
 			jTabbedPane.setIconAt(selectedTab, null);
 					
 			try {
 				Main.copyFile(filePath, Main.getTargetLocation());
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 		this.setSize(938, 462);
 		this.setJMenuBar(getMenu());
 		this.setMenuBarItems();
 		this.setContentPane(getMainPanel());
 		// this.setTabbedPanelItems();
 		this.setTitle("Emperior - Emperical Editor");
 	}
 
 	public static void setTabbedPanelItems(String filePath, String panelName,
 			String sourceCode) {
 		if (openedFiles.get(filePath) == null) {
 			JComponent panel1 = makeTextPanel(filePath, sourceCode);
 			jTabbedPane.addTab(panelName, null, panel1, filePath);
 			// jTabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
 			jTabbedPane.setSelectedIndex(jTabbedPane.getTabCount() - 1);
 			openedFiles.put(filePath, true);
 			
 		}
 	}
 	
 
 
 	protected static JComponent makeTextPanel(String filePath, String sourceCode) {
 
 		JPanel c = new JPanel(false);
 		c.setLayout(new BorderLayout());
 		DefaultSyntaxKit.initKit();
 
 		final JEditorPane codeEditor = new JEditorPane();
 		JScrollPane scrPane = new JScrollPane(codeEditor);
 		c.add(scrPane, BorderLayout.CENTER);
 		c.doLayout();
 
 		codeEditor.setContentType(editorContentType.get(Main.getFileExtension(filePath)));
 
 		codeEditor.setFont(new Font("Inconsolata",Font.PLAIN,11));
 		
 		codeEditor.setText(sourceCode);
 		
 		UndoManager undoManager = new UndoManager();
 		
 		codeEditor.getDocument().addUndoableEditListener(undoManager);
 		
 		undoManagers.put(filePath, undoManager);
 
 		DefaultSyntaxKit de = (DefaultSyntaxKit) codeEditor.getEditorKit();
 		de.setProperty("DEFAULT_EDIT_MENU", "find");
 		de.setProperty("PopupMenu", "find , - , toggle-comments");
 		codeEditor.addKeyListener(new KeyListener(){
 
 			@Override
 			public void keyPressed(KeyEvent e) {}
 
 			@Override
 			public void keyReleased(KeyEvent e) {}
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 				actionPerformed = true;	
 				int index = jTabbedPane.getSelectedIndex();
 				jTabbedPane.setIconAt(index, unsavedIcon);
 			}
 			
 		});
 		
 		editors.put(filePath, codeEditor);
 
 		return c;
 	}
 
 	private void setMenuBarItems() {
 		JMenu fileMenu = new JMenu("File");
 		JMenuItem exit = new JMenuItem("Exit");
 		menu.add(fileMenu);
 		fileMenu.add(exit);
 
 	}
 
 	/**
 	 * This method initializes mainPanel
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getMainPanel() {
 		if (mainPanel == null) {
 			mainPanel = new JPanel();
 			mainPanel.setLayout(new BorderLayout());
 			mainPanel.add(getFileTree(), BorderLayout.WEST);
 			mainPanel.add(getJTabbedPane(), BorderLayout.CENTER);
 			mainPanel.add(getJToolBar(), BorderLayout.NORTH);
 			mainPanel.add(getConsolePane(), BorderLayout.SOUTH);
 		}
 		return mainPanel;
 	}
 
 	public ConsolePane getConsolePane() {
 		if (consolePane == null) {
 			consolePane = new ConsolePane();
 		}
 		return consolePane;
 	}
 
 	/**
 	 * This method initializes jTree
 	 * 
 	 * @return javax.swing.JTree
 	 */
 	public FileTree getFileTree() {
 		if (jTree == null) {
 			System.out.println(": " + experimentFilesFolderPath);
 			if(experimentFilesFolderPath.equals(""))
 				jTree = new FileTree(new File("application"));
 			else
 				jTree = new FileTree(new File(experimentFilesFolderPath));
 		}
 		return jTree;
 	}
 
 	/**
 	 * This method initializes menu
 	 * 
 	 * @return javax.swing.JMenuBar
 	 */
 	private JMenuBar getMenu() {
 		if (menu == null) {
 			menu = new JMenuBar();
 		}
 		return menu;
 	}
 
 	/**
 	 * This method initializes jTabbedPane
 	 * 
 	 * @return javax.swing.JTabbedPane
 	 */
 	private JTabbedPane getJTabbedPane() {
 		if (jTabbedPane == null) {
 			jTabbedPane = new JTabbedPane();
 			jTabbedPane.addChangeListener(new ChangeListener() {
 
 				@Override
 				public void stateChanged(ChangeEvent e) {
 					int selectedIndex = jTabbedPane.getSelectedIndex();
 					if(selectedIndex != -1){
 						String fileName = jTabbedPane
 						.getToolTipTextAt(selectedIndex);
 						Main.addLineToLogFile("[File] viewing: " + fileName);
 					}
 				}
 
 			});
 
 			jTabbedPane.addMouseListener(new MouseListener() {
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					// check for right mouse click:
 					if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
 						removeTab();
 					}
 
 				}
 				
 				@Override
 				public void mouseEntered(MouseEvent e) {}
 
 				@Override
 				public void mouseExited(MouseEvent e) {}
 
 				@Override
 				public void mousePressed(MouseEvent e) {}
 
 				@Override
 				public void mouseReleased(MouseEvent e) {}
 			});
 		}
 		return jTabbedPane;
 	}
 
 	protected void removeTab() {
 		int selectedTab = jTabbedPane.getSelectedIndex();
 		if(selectedTab!=-1){
 			String fileName = jTabbedPane.getToolTipTextAt(selectedTab);
 			Main.addLineToLogFile("[File] closing: " + fileName);
 			openedFiles.remove(fileName);
 			editors.remove(fileName);
 			jTabbedPane.remove(selectedTab);
 			undoManagers.remove(fileName);
 		}
 	}
 
 	private void createToolBarButtons() {
 
 		JButton saveActiveFileButton = new JButton(new ImageIcon(getClass()
 				.getResource("/files/icons/filesave-icon.png")));
 		saveActiveFileButton.setToolTipText("Save active File");
 		saveActiveFileButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				saveSelectedFile();
 			}
 
 		});
 		
 		
 		
 		jToolBar.add(saveActiveFileButton);
 
 		JButton saveAllButton = new JButton(new ImageIcon(getClass()
 				.getResource("/files/icons/save-all-icon.png")));
 		saveAllButton.setToolTipText("Save all opened Files");
 		saveAllButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				saveAllFiles();
 			}
 
 		});
 
 		jToolBar.add(saveAllButton);
 
 		jToolBar.addSeparator();
 
 		pauseResumeButton = new JButton(new ImageIcon(getClass().getResource(
 				"/files/icons/pause-icon.png")));
 		pauseResumeButton.setToolTipText("Pause Experiment");
 		pauseResumeButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				pauseResume();
 			}
 
 		});
 
 		jToolBar.add(pauseResumeButton);
 
 		jToolBar.addSeparator();
 
 		JButton testButton = new JButton(new ImageIcon(getClass().getResource(
 				"/files/icons/run-icon.png")));
 		testButton.setToolTipText("Test Project");
 		testButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				testProject();
 			}
 
 		});
 
 		jToolBar.add(testButton);
 		
 		JButton runButton = new JButton(new ImageIcon(getClass().getResource(
 			"/files/icons/run-icon.png")));
 		runButton.setToolTipText("Run Project");
 		runButton.addActionListener(new ActionListener() {
 		
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				runProject();
 			}
 		
 		});
 		
 		jToolBar.add(runButton);
 
 
 		jToolBar.addSeparator();
 		
 		JButton searchButton = new JButton(new ImageIcon(getClass().getResource(
 				"/files/icons/search-icon.png")));
 		searchButton.setToolTipText("Search");
 		searchButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				openSearchDialog();
 			}
 
 		});
 
 		jToolBar.add(searchButton);
 		
 		jToolBar.addSeparator();
 		
 		JButton undoButton = new JButton(new ImageIcon(getClass().getResource(
 		"/files/icons/undo.png")));
 		undoButton.setToolTipText("Undo");
 		undoButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				undoAction();
 			}
 
 		});
 		
 		jToolBar.add(undoButton);
 		
 		
 		JButton redoButton = new JButton(new ImageIcon(getClass().getResource(
 		"/files/icons/redo.png")));
 		redoButton.setToolTipText("Redo");
 		redoButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				redoAction();
 			}
 
 		});
 		
 		jToolBar.add(redoButton);
 		
 		jToolBar.addSeparator();
 		
 		JButton nextTaskButton = new JButton(new ImageIcon(getClass().getResource(
 		"/files/icons/next_task.png")));
 		nextTaskButton.setToolTipText("Next Task");
 		nextTaskButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				nextTask();
 			}
 
 		});
 				
 		jToolBar.add(nextTaskButton);
 	}
 
 	
 
 	protected void runProject() {
 			new Thread(){
 				public void run(){
 					try {
 						Main.backupCompleteProject();
 						Main.addLineToLogFile("[Project] run");
 						consolePane.setText("Please wait - project is currently compiled");
 						Process p = null;
 						
 						String runBatch = runBatFilePath + File.separator + Main.tasks.get(Main.activeTask) + "_" + Main.tasktypes.get(Main.activeType) + "_run.sh";
 						
 						if(Main.operatingSystem.equals("Linux"))
 						{
 							File runbatTmp = new File(runBatch);
 							p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", runbatTmp.getAbsolutePath()});
 						}
 						else
 							p = Runtime.getRuntime().exec("cmd.exe /c "+runBatch );
 						
 						ProcessOutput po = new ProcessOutput(p, consolePane);
 						po.start();
 						p.waitFor();
 						po.done();
 						po.join();
 				
 						Main.addLineToLogFile("[Project] run finished");
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}.start();
 			
 						
 //			jUnitOutput = "JUnit Output:\n";
 			
 			
 //			displayCompileInfoInConsole();
 //			if(!jUnitOutput.equals("JUnit Output:\n"))
 //				consolePane.addText(jUnitOutput);
 			
 		
 	}
 
 	protected void openSearchDialog() {
 		searchDialog = new SearchDialog(this, "Search");
 
 	}
 
 	protected void pauseResume() {
 		// resume
 		if (isPaused) {
 			isPaused = false;
 			pauseResumeButton.setIcon(new ImageIcon(getClass().getResource(
 					"/files/icons/pause-icon.png")));
 			setStatusForComponents(true);
 			Main.addLineToLogFile("[Resume] Emperior");
 
 		}
 		// pause
 		else {
 			isPaused = true;
 			pauseResumeButton.setIcon(new ImageIcon(getClass().getResource(
 					"/files/icons/resume-icon.png")));
 			setStatusForComponents(false);
 			Main.addLineToLogFile("[Pause] Emperior");
 		}
 
 	}
 
 	public void setStatusForComponents(boolean enable) {
 		int selectedTab = jTabbedPane.getSelectedIndex();
 		if (selectedTab != -1) {
 			String filePath = jTabbedPane.getToolTipTextAt(selectedTab);
 			JEditorPane editor = editors.get(filePath);
 			editor.setEnabled(enable);
 		}
 
 		jTree.setEnabled(enable);
 		jTabbedPane.setEnabled(enable);
 
 	}
 	
 	
 
 	protected void testProject() {
 		try {
 			String testpath = batFilePath + File.separator + Main.tasks.get(Main.activeTask) + "_" + Main.tasktypes.get(Main.activeType) + "_test.sh";
 			Main.backupCompleteProject();
 			Main.addLineToLogFile("[TestCase] run");
 			Process p = null;
 			if(Main.operatingSystem.equals("Linux"))
 			{
 				File batTmp = new File(testpath);
 				p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", batTmp.getAbsolutePath()});
 			}
 			else
 				p = Runtime.getRuntime().exec("cmd.exe /c "+testpath);
 			
 			System.out.println("Running the script");
 
 			
 			
 
 			
 //			jUnitOutput = "JUnit Output:\n";
 			
 //			BufferedInputStream buffer = new BufferedInputStream(p
 //					.getInputStream());
 //
 //			BufferedReader commandResult = new BufferedReader(
 //					new InputStreamReader(buffer));
 //			String line = "";
 
 //			while ((line = commandResult.readLine()) != null) {
 //				parseJUnitOutput(line);
 //			}
 			p.waitFor();
 			
 			
 
 			displayCompileInfoInConsole();
 //			if(!jUnitOutput.equals("JUnit Output:\n"))
 //				consolePane.addText(jUnitOutput);
 			Main.addLineToLogFile("[TestCase] finished");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static void displayCompileInfoInConsole() {
 		String compileOutput = Main.readInFile(Main.logDir + File.separator + "compileOutput.log");
 		consolePane.setText("Compiler Output:");
 		consolePane.addText(compileOutput);
 	}
 
 	private void parseJUnitOutput(String line) {
 		if (line.matches("Time:(.)*")) {
 			String time = line.substring(line.indexOf(":") + 1);
 			Main.addLineToLogFile("[TestCase] time:" + time);
 		} else if (line.matches("OK \\((.)*\\)")) {
 			String amountOfTestCases = line.replaceAll(
 					"(OK |\\(|\\)| test| tests)", "");
 			Main.addLineToLogFile("[TestCase] successfull: "
 					+ amountOfTestCases + " test(s)");
 		} else if (line.matches("Tests run:(.)*")) {
 			String[] split = line.split(",");
 			String testsRun = split[0].replaceAll("Tests run: ", "");
 			String testsFailed = split[1].replaceAll("  Failures: ", "");
 			String testsErrors = split[2].replaceAll("  Errors: ", "");
 			Main.addLineToLogFile("[TestCase] unsuccessfull:" + " Tests run ("
 					+ testsRun + ") - Tests failed (" + testsFailed
 					+ ") - Test Errors (" + testsErrors + ")");
 		}
 		jUnitOutput += line+"\n";
 	}
 
 	/**
 	 * This method initializes jToolBar
 	 * 
 	 * @return javax.swing.JToolBar
 	 */
 	private JToolBar getJToolBar() {
 		if (jToolBar == null) {
 			jToolBar = new JToolBar();
 			jToolBar.setFloatable(false);
 			jToolBar.setBorder(BorderFactory
 					.createEtchedBorder(EtchedBorder.LOWERED));
 			jToolBar.setRollover(true);
 			createToolBarButtons();
 
 		}
 		return jToolBar;
 	}
 
 	/**
 	 * This method initializes jConsoleTextPane
 	 * 
 	 * @return javax.swing.JTextPane
 	 */
 	private JTextPane getJConsoleTextPane() {
 		if (jConsoleTextPane == null) {
 			jConsoleTextPane = new JTextPane();
 		}
 
 		return jConsoleTextPane;
 	}
 
 	/**
 	 * This method initializes jConsoleScrollPane
 	 * 
 	 * @return javax.swing.JScrollPane
 	 */
 	private JScrollPane getJConsoleScrollPane() {
 		if (jConsoleScrollPane == null) {
 			jConsoleScrollPane = new JScrollPane();
 		}
 		jConsoleScrollPane.add(getJConsoleTextPane());
 		return jConsoleScrollPane;
 	}
 
 	
 	private void undoAction(){
 		
 		int index = this.jTabbedPane.getSelectedIndex();
 		
 		if(index != -1){
 			String fileName = jTabbedPane.getToolTipTextAt(index);
 			UndoManager undo = this.undoManagers.get(fileName);
 			if(undo.canUndo())
 				undo.undo();
 		}
 		
 	}
 	
 	private void redoAction(){
 		
 		int index = this.jTabbedPane.getSelectedIndex();
 		
 		if(index != -1){
 			String fileName = jTabbedPane.getToolTipTextAt(index);
 			UndoManager undo = this.undoManagers.get(fileName);
 			if(undo.canRedo())
 				undo.redo();
 		}
 		
 	}
 	
 	private void nextTask(){
 		
 		this.saveAllFiles();
 		
 		jTabbedPane.removeAll();
 		
 		if(Main.activeTask == Main.tasks.size() - 1){
 			Main.activeTask = 0;
 			
 			if(Main.activeType == Main.tasktypes.size() - 1)
 				Main.activeType = 0;
 			else
 				Main.activeType++;
 			
 		}else {
 			Main.activeTask++;
 		}
 		
 		
 		
 		jTree = new FileTree(new File(Main.experimentFilesFolder + File.separator + Main.tasks.get(Main.activeTask) + "_" + Main.tasktypes.get(Main.activeType)));
 		mainPanel = null;
 		menu = null;
 		init();
 		
 		Main.addLineToLogFile("[Task] change task to: " + Main.tasks.get(Main.activeTask) + "_" + Main.tasktypes.get(Main.activeType));
 		Main.initLogging();
 	}
 	
 } // @jve:decl-index=0:visual-constraint="10,10"
 
 
 
