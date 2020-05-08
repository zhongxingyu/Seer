 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.*;
 import java.io.FileWriter.*;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Stack;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.JToolTip;
 import javax.swing.JViewport;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.*;
 import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
 import javax.swing.text.Highlighter.Highlight;
 import javax.swing.undo.AbstractUndoableEdit;
 import javax.swing.undo.UndoManager;
 
 public class guiPanel extends JPanel	 implements UndoableEditListener {
 
 	// C++ source file this application has the focus on currently
 	// When tab focus changes this variable should refer to the file the tab contains
 	String currentCppSourceFile;
 
 	private static final long serialVersionUID = 1L;
 
 	//JMenuItems for File
 	// private JMenuItem newFile;
 	// private JMenuItem openFile;
 	// private JMenuItem saveFile;
 	// private JMenuItem saveAsFile;
 	// private JMenuItem run;
 	// private JMenuItem exit;
 
 	//Menu items for Edit
 	private JMenuItem undo;
 	private JMenuItem redo;
 
 	// private JButton pasteButton;
 
 	private DataDisplay tabbedPane3;
 
 
 	private Editor editor;
 	private JButton jb_undo, jb_redo;
 
 //	private JTextArea area;
 	private UndoManager undoredo;
 	private Controller controller;
 	
 	//TODO: need functionality to keep track of what action was just done
 
 	//Menu Items for Help
 	//private JMenuItem tutorial;
 
 	private TextLineNumber tln;
 	
 	
 	// highlight history
 	private Stack<Object> highlightStack = new Stack<Object>();
 	
 	//constructor
 	public guiPanel(){
 
 		JMenuBar topBar = new JMenuBar(); //menu bar to hold the menus
 	
 		JMenu fileMenu = new JMenu("File");
 		JMenu editMenu = new JMenu("Edit");
 		//JMenu styleMenu = new JMenu("Style");
 		//JMenu helpMenu = new JMenu("Help");
 		JMenu runMenu = new JMenu("Run");
 		currentCppSourceFile = null;
 		
 		//menu items for "Edit"
 
 		undoredo = new UndoManager();
 
 		
 		//help menu
 	//	tutorial = new JMenuItem("Tutorial");
 		
 		
 		
 		
 		//add the menus to the menu bar
 		topBar.add(fileMenu);
 		topBar.add(editMenu);
 		topBar.add(runMenu);
 		//topBar.add(styleMenu);
 	//	topBar.add(helpMenu);
 
 		//helpMenu.add(tutorial);
 
 		JPanel	myPanel = new JPanel();
 
 		JTextArea area = new JTextArea();
 		JTextArea area2 = new JTextArea();
 		JTextArea area3 = new JTextArea();
 		//set up the area to be put into the scroll pane
 		area.setColumns(30);
 		area.setRows(10);
 		area.setEditable(true);
 
 		JToolBar editToolBar = new JToolBar();
 
 		//add edit buttons to menu
 		// TODO: get undo/redo working, need undomanager and listner; look at swing.undo
 		editMenu.add(new UndoAction("Undo"));
 		editMenu.add(new RedoAction("Redo"));
 		editMenu.addSeparator();
 		editMenu.add(area.getActionMap().get(DefaultEditorKit.cutAction));
 		editMenu.add(area.getActionMap().get(DefaultEditorKit.copyAction));
 		editMenu.add(area.getActionMap().get(DefaultEditorKit.pasteAction));
 		editMenu.addSeparator();
 		editMenu.add(area.getActionMap().get(DefaultEditorKit.selectAllAction));
 
 		// Make buttons look nicer
 		Action a;
 		a = area.getActionMap().get(DefaultEditorKit.cutAction);
 		a.putValue(Action.SMALL_ICON, new ImageIcon("Images/edit_cut.png"));
 		a.putValue(Action.NAME, "Cut");
 
 		a = area.getActionMap().get(DefaultEditorKit.copyAction);
 		a.putValue(Action.SMALL_ICON, new ImageIcon("Images/copy.png"));
 		a.putValue(Action.NAME, "Copy");
 
 		a = area.getActionMap().get(DefaultEditorKit.pasteAction);
 		a.putValue(Action.SMALL_ICON, new ImageIcon("Images/paste.png"));
 		a.putValue(Action.NAME, "Paste");
 
 		a = area.getActionMap().get(DefaultEditorKit.selectAllAction);
 		a.putValue(Action.NAME, "Select All");
 
 		Action copyAction = new DefaultEditorKit.CopyAction();
 		copyAction.putValue(Action.SMALL_ICON, new ImageIcon("Images/copy.png"));
 		copyAction.putValue(Action.NAME, ""); 
 
 		Action pasteAction = new DefaultEditorKit.PasteAction();
 		pasteAction.putValue(Action.SMALL_ICON, new ImageIcon("Images/paste.png"));
 		pasteAction.putValue(Action.NAME, "");
 
 		Action cutAction = new DefaultEditorKit.CutAction();
 		cutAction.putValue(Action.SMALL_ICON, new ImageIcon("Images/edit_cut.png"));
 		cutAction.putValue(Action.NAME, "");
 
 		// To Do: Get style menu working
 		/*Action action = new StyledEditorKit.BoldAction();
 		action.putValue(Action.NAME, "Bold");
 		styleMenu.add(action); 
 
 		action = new StyledEditorKit.ItalicAction();
 		action.putValue(Action.NAME, "Italic");
 		styleMenu.add(action);
 
 		action = new StyledEditorKit.UnderlineAction();
 		action.putValue(Action.NAME, "Underline");
 		styleMenu.add(action);*/
 
 		// -------------------------------------------------------------------
 		// 3 main gui components of citrin
 		JTabbedPane program = new JTabbedPane();
 		JTabbedPane tabbedPane2 = new JTabbedPane();
 		// JTabbedPane tabbedPane3 = new JTabbedPane();
 		tabbedPane3 = new DataDisplay(new Dimension(125,93) ); 
 
 		area2.setColumns(30);
 		area2.setRows(10);
 		area2.setEditable(true);
 
 		area3.setColumns(30);
 		area3.setRows(10);
 		area3.setEditable(false);
 
 		Console console = new Console();
 		console.setEditable(false);
 		editor = new Editor();
 
 		JScrollPane scrollPane = new JScrollPane(editor);
 		
 		//add line numbers to editor
 		tln = new TextLineNumber(editor);
 		scrollPane.setRowHeaderView(tln);
 		
 		JScrollPane scrollPane2 = new JScrollPane(console);
 
 		JScrollPane scrollPane3 = new JScrollPane(area3);
 
 		JScrollPane scrollPane4 =  new JScrollPane(tabbedPane3);
 		//tabbedPane.addTab("Program", null);
 		program.add("Program", scrollPane);
 	//	program.setName("*Program");
 
 		//tabbedPane2.addTab("Console", null);
 		tabbedPane2.add("Console", scrollPane2);
 
 		//tabbedPane2.add("copy.gif", new ImageIcon("copy.gif"), 0);
 
 		// tabbedPane3.addTab("States", null);
 		// tabbedPane3.add("States", scrollPane3);
 
 		fileMenu.add(new OpenAction("Open", editor));
 		fileMenu.addSeparator();
 		fileMenu.add(new SaveAction("Save", editor, true));
 		fileMenu.add(new SaveAction("SaveAs", editor, false));
 		fileMenu.addSeparator();
 		fileMenu.add(new ExitAction());
 
 		runMenu.add(new RunAllAction("RunAll", console)); 
 		runMenu.add(new StepAction("RunStep", console));
 		runMenu.add(new RunNumOfStepsAction(console));
 		runMenu.add(new RunBreakPoint(console));
 
 		JButton open = new JButton(new OpenAction("", editor));
 		open.setToolTipText("Open");
 		
 		//buttons for cut, copy, paste
 		JButton cut = new JButton(cutAction);
 		cut.setToolTipText("Cut (Ctrl+X)");
 		JButton copy = new JButton(copyAction);
 		copy.setToolTipText("Copy (Ctrl+C)");
 		JButton paste =  new JButton(pasteAction);
 		paste.setToolTipText("Paste (Ctrl+V)");
 		
 		//button to stop the interpreter
 		Action stopAction = new StopRunAction();
 		JButton stopRunButton = new JButton(stopAction);
 		stopRunButton.setToolTipText("Terminate");
 		
 		//button for runAll
 		Action runAllAction = new RunAllAction("", console);
 		JButton runAll = new JButton(runAllAction);
 		runAll.setToolTipText("Run All (F11)");
 		runAllAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F11"));
 		runAll.getActionMap().put("runAll", runAllAction);
 		runAll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) runAllAction.getValue(Action.ACCELERATOR_KEY), "runAll")
 ;
 		//button for runStep
 		Action runStepAction = new StepAction("", console);
 		JButton runStep = new JButton(runStepAction);
 		runStep.setToolTipText("Run Step (F1)");
 		runStepAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
 		runStep.getActionMap().put("runStep", runStepAction);
 		runStep.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) runStepAction.getValue(Action.ACCELERATOR_KEY), "runStep")
 ;
 		
 		//button for save
 		Action saveAction = new SaveAction("", editor,true);
 		saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
 		JButton save = new JButton(saveAction);
 		save.getActionMap().put("save", saveAction);
 		save.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) saveAction.getValue(Action.ACCELERATOR_KEY), "save")
 ;
 		save.setToolTipText("Save (Ctrl+S)");
 
 		Action undoAction = new UndoAction("");
 		undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
 		jb_undo = new JButton(undoAction);
 		jb_undo.getActionMap().put("undo", undoAction);
 		jb_undo.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) undoAction.getValue(Action.ACCELERATOR_KEY), "undo")
 ;
 		jb_undo.setToolTipText("Undo (Ctrl+Z)");
 		
 		Action redoAction = new RedoAction("");
 		redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
 		jb_redo = new JButton(redoAction);
 		jb_redo.getActionMap().put("redo", redoAction);
 		jb_redo.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) redoAction.getValue(Action.ACCELERATOR_KEY), "redo")
 ;
 		jb_redo.setToolTipText("Redo (Ctrl+Y)");
 		editToolBar.add(open);
 		editToolBar.add(cut);
 		editToolBar.add(copy);
 		editToolBar.add(paste);
 		editToolBar.add(runAll);
 		editToolBar.add(runStep);
 		editToolBar.add(save);
 		editToolBar.add(jb_undo);
 		editToolBar.add(jb_redo);
 		editToolBar.add(stopRunButton);
 		
 
 		JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane4, tabbedPane2);
 
 		splitPane2.setOneTouchExpandable(true);
 		splitPane2.setDividerLocation(400);
 
 		// JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, splitPane2);
 		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, program, splitPane2);
 
 		splitPane.setOneTouchExpandable(true);
 		splitPane.setDividerLocation(450);
 
 		//Provide minimum sizes for the two components in the split pane
 		Dimension minimumSize = new Dimension(200, 10);
 		scrollPane.setMinimumSize(minimumSize);
 		scrollPane2.setMinimumSize(minimumSize);
 		scrollPane3.setMinimumSize(new Dimension(10, 10));
 
 		myPanel.setLayout(new BorderLayout());
 		myPanel.setPreferredSize(new Dimension(500, 600));
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(800, 650));
 
 
 		myPanel.add("Center",splitPane);
 		myPanel.add("North", editToolBar);
 		//myPanel.setLocation(0, 0); //This line doesn't seem to be necessary.
 		add("Center", myPanel);
 		add("North", topBar);
 
 		//setLocation(0,0); //This line doesn't seem to be necessary.
 
 		controller = new Controller(console, this);	
 		editor.getDocument().addUndoableEditListener(this);
 		//undo.addActionListener(this);
 		//redo.addActionListener(this);
 		//jb_undo.addActionListener(this);
 		//jb_redo.addActionListener(this);
 		
 
 		runMenu.add(new RunTimed("RunTimed", console));
 	}
 
 	private static void createAndShowGUI(){
 		// Create the container	
 		JFrame frame = new JFrame("CITRIN", null);
 
 		// Quit the application when this frame is closed:
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Create and add the content
 		guiPanel panel = new guiPanel();
 		frame.setLayout(new BorderLayout());
 		frame.add(panel);
 
 		// Display the window
 		frame.pack(); // adapt the frame size to its content
 		frame.setLocation(300, 20); // in pixels
 		frame.setVisible(true); // Now the frame will appear on screen
 		// Quit the application when this frame is closed:
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//add a window listener
 		frame.addWindowListener(new WindowAdapter(){
 			//save the user preferences as the window closes
 			public void windowClosing(WindowEvent e){
 
 				System.exit(0);
 			}
 		});
 		
 		
 
 	}
 
 
 	public static void main(String args[]){
 
 		// Set the look and feel to that of the system
 		try { 
 			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() ); 
 		}
 		catch ( Exception e ) { 
 			System.err.println( e ); 
 		}
 
 		try { 
 			javax.swing.SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					createAndShowGUI();
 				}
 				});
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	@SuppressWarnings("serial")
 	class RunTimed  extends AbstractAction{
 		JTextComponent display;
 		String interpretation = "";
 		long now;
 		long mTimeElapsed = System.currentTimeMillis();
 		public RunTimed(String label, JTextComponent display) {
 			super("RunTimed", new ImageIcon("Images/run.png"));
 			this.display = display;
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			save(editor, true);
 			runstep s = new runstep();
 			Thread thr = new Thread(s);
       	    thr.start();	
 				  
 			}
 				
 		}
 		
 	
 	
 	class runstep implements Runnable{
 
 		public void run(long time) {
 			// TODO Auto-generated method stub
 			boolean firstRun = false;
 			do{
 				if(firstRun){
 					try {
 				
 						Thread.sleep(time);
 	
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					}
 				}
 			synchronized(controller) {
 				if(!controller.isInterpreting() && !firstRun){
					resetCitrin(1);
 
 					firstRun = true;
 				}
 				else {
 					if(controller.isInterpreting()){
 					controller.addSteps(1);
 					}
 
 				}
 			}
 			}while(controller.isInterpreting());
 			
 			Thread.currentThread().interrupt();	
     	}
 		@Override
 		public void run() {
 			//String s = JOptionPane.showInputDialog("Enter number of seconds:");
 			//if(s.contains(".")){
 				
 			//}
 			
 			double time = Double.parseDouble(JOptionPane.showInputDialog("Enter number of seconds:"));
 			time = time*1000;
 			long s = (long) time;
 			run(s);
 		}
 	}
 	@Override
 	public void undoableEditHappened(UndoableEditEvent edit) {
 		undoredo.addEdit(edit.getEdit());
 		
 	}
 	
 	//highlights one line
 	public void HighlightLine(int lineNumber, Color c){
 		DefaultHighlightPainter color = new DefaultHighlightPainter(c);
 		int beginning = 0;
 		int end = 0;
 		try {
 			beginning = editor.getLineStartOffset(lineNumber-1);
 		} catch (BadLocationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			end = editor.getLineEndOffset(lineNumber-1);
 		} catch (BadLocationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		try {
 			Object tag = editor.getHighlighter().addHighlight(beginning, end, color);
 			highlightStack.add(tag);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void clearHighlight(){
 		editor.getHighlighter().removeAllHighlights();
 		highlightStack.clear();
 	}
 	
 	public void clearMostRecentHighlight(){
 		if(highlightStack.size()>0){
 			editor.getHighlighter().removeHighlight(highlightStack.lastElement());
 			highlightStack.pop();
 		}
 	}
 	
 	public void centerOnLine(int lineNum){
 		int beginning;
 		try {
 			beginning = editor.getLineStartOffset(lineNum-1);
 			editor.setCaretPosition(beginning);
 			centerLineInScrollPane(editor);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	
 	
 	public static void centerLineInScrollPane(JTextComponent component)
 	{
 	    Container container = SwingUtilities.getAncestorOfClass(JViewport.class, component);
 	    if (container == null) return;
 
 	    try{
 	        Rectangle r = component.modelToView(component.getCaretPosition());
 	        JViewport viewport = (JViewport)container;
 
 	        int extentWidth = viewport.getExtentSize().width;
 	        int viewWidth = viewport.getViewSize().width;
 
 	        int x = Math.max(0, r.x - (extentWidth / 2));
 	        x = Math.min(x, viewWidth - extentWidth);
 
 	        int extentHeight = viewport.getExtentSize().height;
 	        int viewHeight = viewport.getViewSize().height;
 
 	        int y = Math.max(0, r.y - (extentHeight / 2));
 	        y = Math.min(y, viewHeight - extentHeight);
 
 	        viewport.setViewPosition(new Point(x, y));
 	    }
 	    catch(BadLocationException ble) {}
 	}
 	
 //	@Override
 /*	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == undo || e.getSource() == jb_undo){
 			if(undoredo.canUndo()){
 				undoredo.undo();
 			}		
 		}
 		else if(e.getSource() == redo || e.getSource() == jb_redo){
 			if(undoredo.canRedo()){
 			undoredo.redo();
 			}
 		}
 	}*/
 	
 	class UndoAction extends AbstractAction{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public UndoAction(String label){
 			super(label, new ImageIcon("Images/undo.png"));
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			// TODO Auto-generated method stub
 			undoredo.undo();
 		}
 		
 	}
 	
 	class RedoAction extends AbstractAction{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public RedoAction(String label){
 			super(label, new ImageIcon("Images/redo.png"));
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			// TODO Auto-generated method stub
 			undoredo.redo();
 		}
 		
 	}
 	
 	/*
 	public void  saveFileAs() throws IOException	{
 			String saveFileName = JOptionPane.showInputDialog("Save File As");
 			System.out.print(saveFileName);
 	}
 	*/
 
 
 	/*@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == exit){
 			System.exit(0);
 		}
 	}*/
 
 
 // ----------------------------------------------------------------------------
 // Callback Classes 
 //
 // TODO : 
 //
 //		- NewFile Action
 //		- Error checking such as making sure the file exists before saving
 //
 // ToConsider : 
 //
 //		 SaveAction and OpenAction should  resides in a different class since
 //		 those actions should be more related to some editing session type class
 //		 rather than this main application class
 //
 //		 All these functions are using currentCppSourceFile. Hard to keep track
 //		 of if multiple editing session should be implemented.
 
 	// An action to simply exit the app
 	@SuppressWarnings("serial")
 	class ExitAction extends AbstractAction {
 
 		public ExitAction() {
 			super("Exit");
 		}
 
 		public void actionPerformed(ActionEvent ev) {
 				System.exit(0);
 		}
 
 	}
 
 	// An action that saves the document to a file : Supports Save and SaveAs actoins 
 	/*class SaveActionButton extends AbstractAction {
 
 		// PossibleBugSource : Saving to class member variable currentCppSourceFile
 
 		JTextComponent textComponent;
 		//boolean saveToCurrentFile;
 
 		// label				... label to show on the view
 		// textComponent		... the view and model that keeps and show the text data
 		// saveToCurrentFile	... false => prompts the user for the file, true => save to curent file
 		public SaveActionButton(String label, JTextComponent textComponent){//, boolean saveToCurrentFile) {
 			super("", new ImageIcon("saved.gif"));
 			this.textComponent = textComponent;
 		//	this.saveToCurrentFile = saveToCurrentFile;
 			
 		}
 
 		public void actionPerformed(ActionEvent ev) {
 			undoredo.discardAllEdits();
 			File file;
 		/*	if ( ! saveToCurrentFile ) {
 			JFileChooser chooser = new JFileChooser();
 			if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
 				return;
 			file = chooser.getSelectedFile();
 			if (file == null)
 				return;
 			} 
 			else {*/
 		/*		if(currentCppSourceFile != null){
 					file = new File(currentCppSourceFile);
 				}
 				else{
 					JFileChooser chooser = new JFileChooser();
 					if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
 						return;
 					file = chooser.getSelectedFile();
 					if (file == null)
 						return;
 					} 
 				//}
 		//	}
 
 			FileWriter writer = null;
 			try {
 				writer = new FileWriter(file);
 				textComponent.write(writer);
 			} catch (IOException ex) {
 			JOptionPane.showMessageDialog(null,
 				"File Not Saved", "ERROR", JOptionPane.ERROR_MESSAGE);
 			} finally {
 				if (writer != null) {
 					try {
 						writer.close();
 					} catch (IOException x) {
 					}
 				}
 			}
 		}
 
 	}*/
 
 	// An action that saves the document to a file : Supports Save and SaveAs actions 
 	@SuppressWarnings("serial")
 	class SaveAction extends AbstractAction {
 		// PossibleBugSource : Saving to class member variable currentCppSourceFile
 
 				JTextComponent textComponent;
 				boolean saveToCurrentFile;
 
 				// label				... label to show on the view
 				// textComponent		... the view and model that keeps and show the text data
 				// saveToCurrentFile	... false => prompts the user for the file, true => save to curent file
 				public SaveAction(String label, JTextComponent textComponent, boolean saveAs){//, boolean saveToCurrentFile) {
 					super(label, new ImageIcon("Images/save.png"));
 					this.textComponent = textComponent;
 					saveToCurrentFile = saveAs;
 				//	this.saveToCurrentFile = saveToCurrentFile;
 					
 				}
 
 				public void actionPerformed(ActionEvent ev) {
 
 					File file;
 				/*	if ( ! saveToCurrentFile ) {
 					JFileChooser chooser = new JFileChooser();
 					if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
 						return;
 					file = chooser.getSelectedFile();
 					if (file == null)
 						return;
 					} 
 					else {*/
 						if(currentCppSourceFile != null && saveToCurrentFile){
 							file = new File(currentCppSourceFile);
 						}
 						else{
 							JFileChooser chooser = new JFileChooser();
 							if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
 								return;
 							file = chooser.getSelectedFile();
 							currentCppSourceFile = file.toString();
 							undoredo.discardAllEdits();
 							} 
 						//}
 				//	}
 
 					FileWriter writer = null;
 					try {
 						writer = new FileWriter(file);
 						textComponent.write(writer);
 					} catch (IOException ex) {
 					JOptionPane.showMessageDialog(null,
 						"File Not Saved", "ERROR", JOptionPane.ERROR_MESSAGE);
 					} finally {
 						if (writer != null) {
 							try {
 								writer.close();
 							} catch (IOException x) {
 							}
 						}
 					}
 				}
 	}
 
 	public void save(JTextComponent textComponent, boolean saveAs){//, boolean saveToCurrentFile) {
 
 
 		File file;
 	/*	if ( ! saveToCurrentFile ) {
 		JFileChooser chooser = new JFileChooser();
 		if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
 			return;
 		file = chooser.getSelectedFile();
 		if (file == null)
 			return;
 		} 
 		else {*/
 			if(currentCppSourceFile != null && saveAs){
 				file = new File(currentCppSourceFile);
 			}
 			else{
 				JFileChooser chooser = new JFileChooser();
 				if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
 					return;
 				file = chooser.getSelectedFile();
 				currentCppSourceFile = file.toString();
 				undoredo.discardAllEdits();
 				} 
 			//}
 	//	}
 
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(file);
 			textComponent.write(writer);
 		} catch (IOException ex) {
 		JOptionPane.showMessageDialog(null,
 			"File Not Saved", "ERROR", JOptionPane.ERROR_MESSAGE);
 		} finally {
 			if (writer != null) {
 				try {
 					writer.close();
 				} catch (IOException x) {
 				}
 			}
 	}
 }
 	
 	// An action that opens an existing file
 	public class OpenAction extends AbstractAction implements UndoableEditListener{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		JTextComponent textComponent;
 
 		// textComponent ... this action opens a file into this textComponent
 		public OpenAction(String label, JTextComponent textComponent) {
 			super(label, new ImageIcon("Images/open_folder_green.png"));
 			this.textComponent = textComponent;
 		}
 
 		// Query user for a filename and attempt to open and read the file into
 		// the text component.
 		public void actionPerformed(ActionEvent ev) {
 			JFileChooser chooser = new JFileChooser();
 			if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
 			return;
 			File file = chooser.getSelectedFile();
 			if (file == null)
 			return;
 
 			FileReader reader = null;
 			try {
 				reader = new FileReader(file);
 				textComponent.read(reader, null);
 				currentCppSourceFile = file.getPath();
 			} catch (IOException ex) {
 				JOptionPane.showMessageDialog(null,
 				"File Not Found", "ERROR", JOptionPane.ERROR_MESSAGE);
 			} finally {
 				if (reader != null) {
 					try {
 						reader.close();
 					} catch (IOException x) {
 					}
 					//Set text component to focus
 					textComponent.requestFocus();
 					
 					//hack to update line numbers
 					textComponent.setCaretPosition(textComponent.getDocument().getLength());
 					textComponent.setCaretPosition(0);
 					textComponent.getDocument().addUndoableEditListener(this);
 					//textComponent.getDocument().addUndoableEditListener( this);
 				}
 				
 			}
 		}
 
 		@Override
 		public void undoableEditHappened(UndoableEditEvent edit) {
 			// TODO Auto-generated method stub
 			undoredo.addEdit(edit.getEdit());
 			
 		}
 	}
 	
 	// An action that runs the interpreter on all the contents of the current cpp source file
 	@SuppressWarnings("serial")
 	class RunAllAction extends AbstractAction {
 		JTextComponent display;
 		String interpretation = "";
 		
 		public RunAllAction(String label, JTextComponent display) {
 			super(label, new ImageIcon("Images/run.png"));
 			this.display = display;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			save(editor, true);
 			// run interpreter on currentCppSourceFile
 
 			// Start interpreter in new thread
 			// TODO: hmmmm is this the best way
 			synchronized(controller){
 				if(!controller.isInterpreting()){
 					resetCitrin(null);
 				}
 				else{
 					controller.continueRun();
 				}
 			}
 		}
 	}
 
 	// An action that runs the interpreter on all the contents of the current cpp source file
 	@SuppressWarnings("serial")
 	class StepAction extends AbstractAction {
 		JTextComponent display;
 		String interpretation = "";
 
 		public StepAction(String label, JTextComponent display) {
 			super(label, new ImageIcon("Images/step.gif"));
 			this.display = display;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			// run interpreter on currentCppSourceFile
 			save(editor, true);
 			synchronized(controller){
 
 				if(!controller.isInterpreting()){
 					resetCitrin(1);
 				}
 				else{
 					controller.addSteps(1);
 				}
 			}
 
 		}
 
 	}
 	
 	
 	@SuppressWarnings("serial")
 	class RunNumOfStepsAction extends AbstractAction {
 		JTextComponent display;
 		String interpretation = "";
 
 		public RunNumOfStepsAction(JTextComponent display) {
 			super("Run Multiple Steps");
 			this.display = display;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// run interpreter on currentCppSourceFile
 			save(editor, true);
 			int steps;
 			int input = Integer.parseInt(JOptionPane.showInputDialog("Enter number of steps to run"));
 			input = input-1;
 			synchronized(controller){
 					Interpreter i;
 					SymbolTableNotifier stab = new SymbolTableNotifier(); 
 					stab.addObserver( tabbedPane3 );
 					controller.clearConsole();
 					controller.setInterpretingStart();
 					new Thread(i = new Interpreter(controller,currentCppSourceFile,1, stab)).start();
 					controller.addInterpreter(i);
 
 					controller.addSteps(input);
 			}
 
 		}
 
 	}
 	
 	@SuppressWarnings("serial")
 	class RunBreakPoint extends AbstractAction {
 		JTextComponent display;
 		String interpretation = "";
 
 		public RunBreakPoint(JTextComponent display) {
 			super("Run to Breakpoint");
 			this.display = display;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		// run interpreter on currentCppSourceFile
 		save(editor, true);
 		int input = Integer.parseInt(JOptionPane.showInputDialog("Run to which line"));
 			synchronized(controller){
 				if(!controller.isInterpreting()){
 					resetCitrin(input);
 				}
 				else{
 					controller.runToBreak(input);
 				}
 			}
 		}
 	}
 	
 
 	@SuppressWarnings("serial")
 	class StopRunAction extends AbstractAction {
 
 
 		public StopRunAction() {
 			super("", new ImageIcon("Images/stop.png"));
 
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			//stop interpreter
 
 			synchronized(controller){
 					controller.stopInterpreter();
 			}
 
 		}
 
 	}
 
 	void resetCitrin(Integer breakPoint)
 	{
 
 		SymbolTableNotifier stab = new SymbolTableNotifier(); 
 		stab.addObserver( tabbedPane3 );
 		controller.clearConsole();
 		controller.setInterpretingStart();
 		Interpreter i = null;
 		if ( breakPoint != null )
 			i = new Interpreter(controller,currentCppSourceFile,breakPoint, stab);
 		else 
 			i = new Interpreter(controller,currentCppSourceFile,-1, stab);
 
 		controller.addInterpreter(i);
 
 		Thread t = new Thread(i);
 		CitrinInterrupter.getInstance().addThread(t);
 
 		t.start();
 
 		if (breakPoint!=null)
 			i.setBreakPoint(breakPoint);
 	}
 
 }
