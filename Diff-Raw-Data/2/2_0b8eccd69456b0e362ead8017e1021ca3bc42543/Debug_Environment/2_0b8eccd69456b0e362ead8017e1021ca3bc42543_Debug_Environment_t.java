 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.text.NumberFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 class CompileThread extends Thread {
 	private Debug_Environment debugEnvironment;
 	private boolean shouldExecute;
 	public static final String COMPILETHREADSTRING = "Compilation";
 	private static int threadNum = 0;
 
 	public CompileThread(Debug_Environment debugEnv, boolean shouldExecute) {
 		super(COMPILETHREADSTRING + " " + threadNum++);
 		this.debugEnvironment = debugEnv;
 		this.shouldExecute = shouldExecute;
 	}
 
 	public void run() {
 		Debugger.hitStopWatch(Thread.currentThread().getName());
 		this.debugEnvironment.getEnvironment().reset();
 		Parser.clearPreparseLists();
 		boolean quickflag = true;
 		for (int i = 0; i < this.debugEnvironment.getScriptElements().size(); i++) {
 			Debug_ScriptElement element = this.debugEnvironment.getScriptElements().get(i);
 			element.saveFile();
 			if (!element.compile()) {
 				quickflag = false;
 				this.debugEnvironment.setTitleAt(i + 1, element.getName());
 			}
 		}
 		if (!quickflag) {
 			this.debugEnvironment.setStatus("One or more files had errors during compilation.");
 			return;
 		}
 		Vector<Exception> exceptions = Parser.parseElements(this.debugEnvironment.getEnvironment());
 		if (exceptions.size() == 0) {
 			this.debugEnvironment.canExecute(true);
 			this.debugEnvironment.setStatus("All files compiled successfully.");
 			Debugger.hitStopWatch(Thread.currentThread().getName());
 			assert Debugger.addSnapNode("Compile successful", this.debugEnvironment.getEnvironment());
 			if (this.shouldExecute) {
 				ExecutionThread thread = new ExecutionThread(this.debugEnvironment);
 				thread.start();
 			}
 			//debugEnvironment.report();
 			return;
 		} else {
 			Debugger.hitStopWatch(Thread.currentThread().getName());
 			this.debugEnvironment.setStatus("One or more files had errors during compilation.");
 			this.debugEnvironment.addExceptions(exceptions);
 		}
 	}
 }
 
 public class Debug_Environment extends JFrame implements ActionListener, ChangeListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8190546125680224912L;
 
 	public static void initialize() {
 		Debug_TreeNode.addPrecached(DebugString.ELEMENTS, "Elements");
 		Debug_TreeNode.addPrecached(DebugString.SCRIPTGROUPPARENTHETICAL, "Script Group (parenthetical)");
 		Debug_TreeNode.addPrecached(DebugString.SCRIPTGROUPCURLY, "Script Group (curly)");
 		Debug_TreeNode.addPrecached(DebugString.NUMERICSCRIPTVALUESHORT, "Numeric Script-Value (short)");
 		Debug_TreeNode.addPrecached(DebugString.NUMERICSCRIPTVALUEINT, "Numeric Script-Value (int)");
 		Debug_TreeNode.addPrecached(DebugString.NUMERICSCRIPTVALUELONG, "Numeric Script-Value (long)");
 		Debug_TreeNode.addPrecached(DebugString.NUMERICSCRIPTVALUEFLOAT, "Numeric Script-Value (float)");
 		Debug_TreeNode.addPrecached(DebugString.NUMERICSCRIPTVALUEDOUBLE, "Numeric Script-Value (double)");
 		Debug_TreeNode.addPrecached(DebugString.PERMISSIONNULL, "Permission: null");
 		Debug_TreeNode.addPrecached(DebugString.PERMISSIONPRIVATE, "Permission: private");
 		Debug_TreeNode.addPrecached(DebugString.PERMISSIONPROTECTED, "Permission: protected");
 		Debug_TreeNode.addPrecached(DebugString.PERMISSIONPUBLIC, "Permission: public");
 		Debug_TreeNode.addPrecached(DebugString.SCRIPTLINE, "Script Line: ");
 		Debug_TreeNode.addPrecached(DebugString.SCRIPTOPERATOR, "Script Operator: ");
 		Debug_TreeNode.addPrecached(DebugString.ORIGINALSTRING, "Original String: '");
 		Debug_TreeNode.addPrecached(DebugString.REFERENCEDELEMENTNULL, "Referenced Element: null");
 		Debug_TreeNode.addPrecached(DebugString.OUTPUTTREE, "Output Tree");
 	}
 
 	public static void main(String[] args) {
 		new Debug_Environment(800, 800);
 	}
 
 	private JSplitPane vertSplitPane;
 	private JTabbedPane tabbedPane, filteredPanes;
 	private JLabel status;
 	private JMenuItem newFile, openFile, closeFile, saveFile, saveFileAs, exit,
 			reset, report;
 	private JMenuItem compile, execute, compileAndRun;
 	private JMenuItem undo, redo;
 	private JMenuItem removeTab, clearTab, createListener, renameTab;
 	private JMenuItem addException, addExceptionFromList, addIgnore, addIgnoreFromList,
 			removeException, removeIgnore;
 	private JMenuItem addAssertionFailure, removeAssertionFailure;
 	private JRadioButtonMenuItem exceptionsMode, ignoreMode;
 	private JMenuBar menuBar;
 	private JMenu parserMenu, editMenu, listenerMenu;
 	private java.util.List<Debug_ScriptElement> scriptElements;
 	private Debug_Listener filtering;
 	private Map<String, java.util.List<Debug_Listener>> filteredOutputMap = new HashMap<String, java.util.List<Debug_Listener>>();
 	private String priorityExecutingClass;
 	private java.util.List<String> exceptions, ignores, allThreads;
 
 	private Map<String, Integer> exceptionMap = new HashMap<String, Integer>();
 
 	private ScriptEnvironment environment;
 
 	public Debug_Environment(int width, int height) {
 		super("RFE Debugger");
 		Debugger.setDebugger(this);
 		initialize();
 		this.scriptElements = new LinkedList<Debug_ScriptElement>();
 		this.exceptions = new LinkedList<String>();
 		this.ignores = new LinkedList<String>();
 		this.allThreads = new LinkedList<String>();
 		this.allThreads.add("AWT-EventQueue-0");
 		this.allThreads.add(CompileThread.COMPILETHREADSTRING);
 		this.allThreads.add(ExecutionThread.EXECUTIONTHREADSTRING);
 		this.allThreads.add(PolygonPipeline.POLYGONPIPELINESTRING);
 		this.allThreads.add(SplitterThread.SPLITTERTHREADSTRING);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(width, height);
 		// Menu bar
 		this.getContentPane().setLayout(new BorderLayout());
 		this.getContentPane().add(this.status = new JLabel(" Ready"), BorderLayout.SOUTH);
 		this.getContentPane().add(this.tabbedPane = new JTabbedPane());
 		this.tabbedPane.addChangeListener(this);
 		this.menuBar = new JMenuBar();
 		this.setJMenuBar(this.menuBar);
 		JMenu fileMenu = new JMenu("File");
 		this.menuBar.add(fileMenu);
 		fileMenu.setMnemonic('F');
 		this.editMenu = new JMenu("Edit");
 		this.menuBar.add(this.editMenu);
 		this.editMenu.setMnemonic('E');
 		this.parserMenu = new JMenu("Parser");
 		this.parserMenu.setMnemonic('P');
 		this.menuBar.add(this.parserMenu);
 		fileMenu.add(this.newFile = new JMenuItem("New Script", 'N'));
 		fileMenu.add(this.openFile = new JMenuItem("Open Script...", 'O'));
 		fileMenu.add(this.closeFile = new JMenuItem("Close Script", 'C'));
 		fileMenu.add(this.saveFile = new JMenuItem("Save Script", 'S'));
 		fileMenu.add(this.saveFileAs = new JMenuItem("Save Script As...", 'A'));
 		fileMenu.add(this.report = new JMenuItem("Report", 'T'));
 		fileMenu.add(this.reset = new JMenuItem("Reset"));
 		fileMenu.add(this.exit = new JMenuItem("Exit", 'X'));
 		this.editMenu.add(this.undo = new JMenuItem("Undo", 'U'));
 		this.editMenu.add(this.redo = new JMenuItem("Redo", 'R'));
 		this.parserMenu.add(this.compile = new JMenuItem("Compile", 'C'));
 		this.parserMenu.add(this.execute = new JMenuItem("Execute", 'X'));
 		this.parserMenu.add(this.compileAndRun = new JMenuItem("Compile and Run", 'R'));
 		this.execute.setEnabled(false);
 		JMenu debugMenu = new JMenu("Debugger");
 		this.menuBar.add(debugMenu);
 		debugMenu.add(this.exceptionsMode = new JRadioButtonMenuItem("Lazy Filter Mode"));
 		debugMenu.add(this.ignoreMode = new JRadioButtonMenuItem("Greedy Filter Mode"));
 		debugMenu.addSeparator();
 		debugMenu.add(this.addException = new JMenuItem("Add Exception"));
 		debugMenu.add(this.addExceptionFromList = new JMenuItem("Add Exception From List..."));
 		debugMenu.add(this.removeException = new JMenuItem("Remove Exception..."));
 		debugMenu.addSeparator();
 		debugMenu.add(this.addIgnore = new JMenuItem("Add to Ignore List"));
 		debugMenu.add(this.addIgnoreFromList = new JMenuItem("Add Ignore From List..."));
 		debugMenu.add(this.removeIgnore = new JMenuItem("Remove Ignore..."));
 		debugMenu.addSeparator();
 		debugMenu.add(this.addAssertionFailure = new JMenuItem("Add Assertion Failure"));
 		debugMenu.add(this.removeAssertionFailure = new JMenuItem("Remove Assertion Failure"));
 		this.listenerMenu = new JMenu("Listener");
 		this.menuBar.add(this.listenerMenu);
 		this.listenerMenu.setMnemonic('L');
 		this.listenerMenu.add(this.createListener = new JMenuItem("Create Listener...", 'C'));
 		this.listenerMenu.add(this.renameTab = new JMenuItem("Rename Tab...", 'N'));
 		this.listenerMenu.add(this.clearTab = new JMenuItem("Clear Tab", 'C'));
 		this.listenerMenu.add(this.removeTab = new JMenuItem("Remove Tab", 'R'));
 		ButtonGroup group = new ButtonGroup();
 		group.add(this.exceptionsMode);
 		group.add(this.ignoreMode);
 		this.exceptionsMode.setSelected(true);
 		// Set up our debug spew and script tabs
 		this.tabbedPane.add("Debug Output", this.filteredPanes = new JTabbedPane());
 		// Accelerators
 		this.clearTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
 		this.removeTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
 		this.newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
 		this.openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
 		this.closeFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
 		this.saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 		this.saveFileAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
 		this.exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
 		this.undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
 		this.redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
 		this.compile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
 		this.execute.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
 		this.report.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
 		this.compileAndRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.CTRL_MASK));
 		// Listeners
 		this.reset.addActionListener(this);
 		this.renameTab.addActionListener(this);
 		this.report.addActionListener(this);
 		this.filteredPanes.addChangeListener(this);
 		this.clearTab.addActionListener(this);
 		this.removeTab.addActionListener(this);
 		this.createListener.addActionListener(this);
 		this.newFile.addActionListener(this);
 		this.openFile.addActionListener(this);
 		this.closeFile.addActionListener(this);
 		this.saveFile.addActionListener(this);
 		this.saveFileAs.addActionListener(this);
 		this.exit.addActionListener(this);
 		this.undo.addActionListener(this);
 		this.redo.addActionListener(this);
 		this.compile.addActionListener(this);
 		this.execute.addActionListener(this);
 		this.compileAndRun.addActionListener(this);
 		this.addException.addActionListener(this);
 		this.addExceptionFromList.addActionListener(this);
 		this.addIgnoreFromList.addActionListener(this);
 		this.addIgnore.addActionListener(this);
 		this.removeException.addActionListener(this);
 		this.removeIgnore.addActionListener(this);
 		this.addAssertionFailure.addActionListener(this);
 		this.removeAssertionFailure.addActionListener(this);
 		// Open all valid scripts in our working directory
 		File folder = new File(".");
 		ExtensionFilter filter = new ExtensionFilter();
 		filter.addExtension("RiffScript");
 		File[] files = folder.listFiles(filter);
 		for (File file : files) {
 			if (file.isFile()) {
 				this.addReferenced(new Debug_ScriptElement(this, file));
 			}
 		}
 		// Finally, show the window.
 		this.environment = new ScriptEnvironment();
 		this.setVisible(true);
 	}
 
 	// Listeners
 	public void actionPerformed(ActionEvent event) {
 		if (event.getActionCommand().equals("Exit")) {
 			java.util.List<Debug_ScriptElement> removedElements = new LinkedList<Debug_ScriptElement>();
 			int index = this.tabbedPane.getSelectedIndex();
 			for (; this.scriptElements.size() > 0;) {
 				Debug_ScriptElement element = this.scriptElements.get(0);
 				if (!element.closeFile()) {
 					for (Debug_ScriptElement added : removedElements) {
 						this.scriptElements.add(0, added);
 						this.tabbedPane.add(added, 1);
 					}
 					this.tabbedPane.setSelectedIndex(index);
 					return;
 				}
 				this.tabbedPane.remove(1);
 				this.scriptElements.remove(0);
 				removedElements.add(element);
 			}
 			System.exit(0);
 		} else if (event.getSource().equals(this.newFile)) {
 			this.addReferenced(new Debug_ScriptElement(this, (String) null));
 		} else if (event.getSource().equals(this.openFile)) {
 			Debug_ScriptElement element = new Debug_ScriptElement(this);
 			if (element.isValid()) {
 				this.addReferenced(element);
 			}
 		} else if (event.getSource().equals(this.closeFile)) {
 			if (((Debug_ScriptElement) this.scriptElements.get(this.tabbedPane.getSelectedIndex() - 1)).closeFile()) {
 				int index = this.tabbedPane.getSelectedIndex();
 				this.tabbedPane.remove(index);
 				this.scriptElements.remove(index - 1);
 			}
 		} else if (event.getSource().equals(this.renameTab)) {
 			Object text = JOptionPane.showInputDialog(null, "Insert new output name", "Rename Output", JOptionPane.QUESTION_MESSAGE, null, null, this.filteredPanes.getTitleAt(this.filteredPanes.getSelectedIndex()));
 			if (text != null) {
 				this.filteredPanes.setTitleAt(this.filteredPanes.getSelectedIndex(), text.toString());
 			}
 		} else if (event.getSource().equals(this.createListener)) {
 			((Debug_Listener) this.filteredPanes.getSelectedComponent()).promptCreateListener();
 		} else if (event.getSource().equals(this.saveFile)) {
 			this.scriptElements.get(this.tabbedPane.getSelectedIndex() - 1).saveFile();
 		} else if (event.getSource().equals(this.reset)) {
 			this.reset();
 		} else if (event.getSource().equals(this.clearTab)) {
 			((Debug_Listener) this.filteredPanes.getSelectedComponent()).clearTab();
 		} else if (event.getSource().equals(this.removeTab)) {
 			((Debug_Listener) this.filteredPanes.getSelectedComponent()).removeTab();
 			this.filteredOutputMap.get(((Debug_Listener) this.filteredPanes.getSelectedComponent()).getThreadName()).remove(this.filteredPanes.getSelectedComponent());
 			this.filteredPanes.remove(this.filteredPanes.getSelectedComponent());
 		} else if (event.getSource().equals(this.saveFileAs)) {
 			this.scriptElements.get(this.tabbedPane.getSelectedIndex() - 1).saveFileAs();
 		} else if (event.getSource().equals(this.undo)) {
 			this.scriptElements.get(this.tabbedPane.getSelectedIndex() - 1).undo();
 		} else if (event.getSource().equals(this.report)) {
 			this.report();
 		} else if (event.getSource().equals(this.addException)) {
 			Object string = null;
 			if (this.filteredPanes.getSelectedComponent() != null) {
 				string = ((Debug_Listener) this.filteredPanes.getSelectedComponent()).getThreadName();
 			}
 			Object text = JOptionPane.showInputDialog(null, "Insert the thread name to add to the exceptions list", "Adding to Exceptions List", JOptionPane.PLAIN_MESSAGE, null, null, string);
 			if (text == null) {
 				return;
 			}
 			this.exceptions.add(text.toString());
 		} else if (event.getSource().equals(this.addExceptionFromList)) {
 			if (this.allThreads == null || this.allThreads.size() == 0) {
 				JOptionPane.showMessageDialog(null, "There are no threads in the selection list.", "Empty Thread List", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 			Object text = JOptionPane.showInputDialog(null, "Select the thread name to add to the exceptions list", "Adding to Exceptions List", JOptionPane.PLAIN_MESSAGE, null, this.allThreads.toArray(), null);
 			if (text == null) {
 				return;
 			}
 			this.exceptions.add(text.toString());
 		} else if (event.getSource().equals(this.removeException)) {
 			if (this.exceptions == null || this.exceptions.size() == 0) {
 				JOptionPane.showMessageDialog(null, "No threads to remove from exceptions list.", "Empty Exception List", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 			Object text = JOptionPane.showInputDialog(null, "Select the thread name to remove from the exceptions list", "Removing Exception", JOptionPane.PLAIN_MESSAGE, null, this.exceptions.toArray(), null);
 			if (text == null) {
 				return;
 			}
 			this.exceptions.remove(text.toString());
 		} else if (event.getSource().equals(this.addIgnore)) {
 			Object string = null;
 			if (this.filteredPanes.getSelectedComponent() != null) {
 				string = ((Debug_Listener) this.filteredPanes.getSelectedComponent()).getThreadName();
 			}
 			Object text = JOptionPane.showInputDialog(null, "Insert the thread name to add to the ignore list", "Adding to Ignore List", JOptionPane.PLAIN_MESSAGE, null, null, string);
 			if (text == null) {
 				return;
 			}
 			this.ignores.add(text.toString());
 		} else if (event.getSource().equals(this.addIgnoreFromList)) {
 			if (this.allThreads == null || this.allThreads.size() == 0) {
 				JOptionPane.showMessageDialog(null, "There are no threads in the selection list.", "Empty Thread List", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 			Object text = JOptionPane.showInputDialog(null, "Select the thread name to add to the ignore list", "Adding to Ignore List", JOptionPane.PLAIN_MESSAGE, null, this.allThreads.toArray(), null);
 			if (text == null) {
 				return;
 			}
 			this.ignores.add(text.toString());
 		} else if (event.getSource().equals(this.removeIgnore)) {
 			if (this.ignores == null || this.ignores.size() == 0) {
 				JOptionPane.showMessageDialog(null, "No threads to remove from ignore list.", "Empty Ignore List", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 			Object text = JOptionPane.showInputDialog(null, "Select the thread name to remove from the ignore list", "Removing Ignore", JOptionPane.PLAIN_MESSAGE, null, this.ignores.toArray(), null);
 			if (text == null) {
 				return;
 			}
 			this.ignores.remove(text.toString());
 		} else if (event.getSource().equals(this.addAssertionFailure)) {
 
 		} else if (event.getSource().equals(this.redo)) {
 			this.scriptElements.get(this.tabbedPane.getSelectedIndex() - 1).redo();
 		} else if (event.getSource().equals(this.compile) || event.getSource().equals(this.compileAndRun)) {
 			this.setStatus("Compiling...");
 			this.execute.setEnabled(false);
 			CompileThread thread = new CompileThread(this, event.getActionCommand().equals("Compile and Run"));
 			thread.start();
 		} else if (event.getSource().equals(this.execute)) {
 			ExecutionThread thread = new ExecutionThread(this);
 			thread.start();
 		}
 	}
 
 	public void addExceptions(java.util.List<Exception> exceptions) {
 		for (Exception rawEx : exceptions) {
 			if (rawEx instanceof Exception_Nodeable && !((Exception_Nodeable) rawEx).isAnonymous()) {
 				Exception_Nodeable ex = (Exception_Nodeable) rawEx;
 				this.getReferenced(ex.getFilename()).addException(ex);
 				this.tabbedPane.setTitleAt(this.scriptElements.indexOf(this.getReferenced(ex.getFilename())) + 1, this.getReferenced(ex.getFilename()).getName());
 			} else {
 				this.scriptElements.get(0).addException(rawEx);
 			}
 		}
 	}
 
 	public void addNode(Debug_TreeNode node) {
 		this.addNode(node, false);
 	}
 
 	public void addNode(Debug_TreeNode node, boolean setAsCurrent) {
 		if (this.isIgnoringThisThread()) {
 			return;
 		}
 		if (this.filteredOutputMap.get(Thread.currentThread().getName()) == null || this.filteredOutputMap.get(Thread.currentThread().getName()).size() == 0) {
 			this.filteredOutputMap.put(Thread.currentThread().getName(), new Vector<Debug_Listener>());
 			this.filteredOutputMap.get(Thread.currentThread().getName()).add(new Debug_Listener(Thread.currentThread().getName(), this, null, Thread.currentThread().getName()));
 			this.filteredPanes.add(Thread.currentThread().getName(), this.filteredOutputMap.get(Thread.currentThread().getName()).get(0));
 		}
 		for (Debug_Listener listener : this.filteredOutputMap.get(Thread.currentThread().getName())) {
 			if (listener.isCapturing()) {
 				if (!listener.getTreePanel().getFilter().isListening()) {
 					listener.getTreePanel().getFilter().sniffNode(node);
 					if (listener.getTreePanel().getFilter().isListening()) {
 						this.addNodeToOutput(node, setAsCurrent, listener);
 					}
 				} else {
 					this.addNodeToOutput(node, setAsCurrent, listener);
 				}
 				return;
 			}
 		}
 		for (Debug_Listener listener : this.filteredOutputMap.get(Thread.currentThread().getName())) {
 			this.addNodeToOutput(node.duplicate(), setAsCurrent, listener);
 		}
 	}
 
 	private void addNodeToOutput(Debug_TreeNode node, boolean setAsCurrent, Debug_Listener listener) {
 		if (!listener.getTreePanel().getFilter().isListening()) {
 			listener.getTreePanel().getFilter().sniffNode(node);
 			if (!listener.getTreePanel().getFilter().isListening()) {
 				return;
 			}
 			node = new Debug_TreeNode_Orphaned(listener.getSource().getTreePanel().getLastNodeAdded());
 		}
 		if (listener.getTreePanel().getCurrentNode().hasChildren() && listener.getTreePanel().getCurrentNode().getLastChild().getGroup() != null && node.getGroup() != null && listener.getTreePanel().getCurrentNode().getLastChild().getGroup().equals(node.getGroup())) {
 			if (listener.getTreePanel().getCurrentNode().getLastChild().getData().equals(node.getGroup())) {
 				listener.getTreePanel().getCurrentNode().getLastChild().addChild(node);
 				node.setPracticalParent(listener.getTreePanel().getCurrentNode());
 				if (setAsCurrent) {
 					listener.getTreePanel().setAsCurrent(node);
 				}
 			} else {
 				Debug_TreeNode lastNode = listener.getTreePanel().getCurrentNode().removeLastChild();
 				Debug_TreeNode groupNode = new Debug_TreeNode(node.getGroupCode(), node.getGroupCode());
 				groupNode.addChild(lastNode);
 				groupNode.addChild(node);
 				lastNode.setPracticalParent(listener.getTreePanel().getCurrentNode());
 				node.setPracticalParent(listener.getTreePanel().getCurrentNode());
 				listener.getTreePanel().addNode(groupNode);
 				if (setAsCurrent) {
 					listener.getTreePanel().setAsCurrent(node);
 				}
 			}
 			return;
 		}
 		listener.getTreePanel().addNode(node);
 		if (setAsCurrent) {
 			listener.getTreePanel().setAsCurrent(node);
 		}
 	}
 
 	public Debug_Listener addOutputListener(Debug_Listener source, Object filter) {
 		if (filter == null || "".equals(filter)) {
 			return null;
 		}
 		if (this.isFilterUsed(filter, source.getThreadName()) != null) {
 			JOptionPane.showMessageDialog(this, "An output listener has an identical filter to the one provided.", "Listener Already Exists", JOptionPane.INFORMATION_MESSAGE);
 			this.focusOnOutput(this.isFilterUsed(filter, source.getThreadName()));
 			return null;
 		}
 		Debug_Listener output = new Debug_Listener(source.getThreadName(), this, source, filter.toString());
 		output.getTreePanel().getFilter().addFilter(filter);
 		output.getTreePanel().refresh();
 		source.addChildOutput(output);
 		this.filteredPanes.add(filter.toString(), output);
 		this.filteredPanes.setSelectedIndex(this.filteredPanes.getComponentCount() - 1);
 		this.filteredOutputMap.get(source.getThreadName()).add(output);
 		return output;
 	}
 
 	public void addReferenced(Debug_ScriptElement element) {
 		this.tabbedPane.add(element.getName(), element);
 		this.scriptElements.add(element);
 		this.tabbedPane.setSelectedIndex(this.tabbedPane.getComponents().length - 1);
 	}
 
 	public void canExecute(boolean value) {
 		this.execute.setEnabled(value);
 	}
 
 	public void closeNode() {
 		if (this.isIgnoringThisThread()) {
 			return;
 		}
 		for (Debug_Listener listener : this.filteredOutputMap.get(Thread.currentThread().getName())) {
 			if (listener.isCapturing() && listener.getTreePanel().getFilter().isListening()) {
 				listener.getTreePanel().closeNode();
 				return;
 			}
 		}
 		for (Debug_Listener listener : this.filteredOutputMap.get(Thread.currentThread().getName())) {
 			if (listener.getTreePanel().getFilter().isListening()) {
 				listener.getTreePanel().closeNode();
 			}
 		}
 	}
 
 	public void closeNodeTo(Object string) {
 		if (this.isIgnoringThisThread()) {
 			return;
 		}
 		for (Debug_Listener listener : this.filteredOutputMap.get(Thread.currentThread().getName())) {
 			while (listener.getTreePanel().getFilter().isListening() && !listener.getTreePanel().getCurrentNode().getData().equals(string)) {
 				listener.getTreePanel().closeNode();
 			}
 		}
 	}
 
 	public boolean ensureCurrentNode(Object obj) {
 		if (!this.reset.isEnabled()) {
 			return true;
 		}
 		if (this.isInExceptionsMode() && !this.exceptions.contains(Thread.currentThread().getName())) {
 			return true;
 		}
 		if (this.ignores.contains(Thread.currentThread().getName())) {
 			return true;
 		}
 		return this.getUnfilteredCurrentNode().getData().equals(obj);
 	}
 
 	public void focusOnOutput(Debug_Listener output) {
 		assert output != null;
 		this.filteredPanes.setSelectedIndex(this.filteredPanes.indexOfComponent(output));
 	}
 
 	public ScriptEnvironment getEnvironment() {
 		return this.environment;
 	}
 
 	public Debug_Listener getFilteringOutput() {
 		return this.filtering;
 	}
 
 	public Debug_TreeNode getLastNodeAdded() {
 		return this.getUnfilteredOutput().getTreePanel().getLastNodeAdded();
 	}
 
 	public String getPriorityExecutingClass() {
 		return this.priorityExecutingClass;
 	}
 
 	public Debug_ScriptElement getReferenced(String name) {
 		for (Debug_ScriptElement element : this.scriptElements) {
 			if (element.getFilename().equals(name)) {
 				return element;
 			}
 		}
 		throw new Exception_InternalError("Script element not found: " + name);
 	}
 
 	public java.util.List<Debug_ScriptElement> getScriptElements() {
 		return this.scriptElements;
 	}
 
 	public Debug_TreeNode getUnfilteredCurrentNode() {
 		return this.getUnfilteredOutput().getTreePanel().getCurrentNode();
 	}
 
 	public Debug_Listener getUnfilteredOutput() {
 		return this.filteredOutputMap.get(Thread.currentThread().getName()).get(0);
 	}
 
 	public Debug_Listener isFilterUsed(Object filter, String threadName) {
 		for (Debug_Listener listener : this.filteredOutputMap.get(threadName)) {
 			if (!listener.isUnfiltered() && listener.getTreePanel().getFilter().isFilterUsed(filter)) {
 				return listener;
 			}
 		}
 		return null;
 	}
 
 	public boolean isIgnoringThisThread() {
 		if (!this.reset.isEnabled()) {
 			return true;
 		}
 		String currentThread = Thread.currentThread().getName();
 		if (this.isInExceptionsMode()) {
 			for (String thread : this.exceptions) {
 				if (currentThread.contains(thread)) {
 					return false;
 				}
 			}
 			return true;
 		} else {
 			for (String thread : this.ignores) {
 				if (currentThread.contains(thread)) {
 					return true;
 				}
 			}
 			return false;
 		}
 	}
 
 	public boolean isInExceptionsMode() {
 		return this.exceptionsMode.isSelected();
 	}
 
 	public boolean isResetting() {
 		return !this.reset.isEnabled();
 	}
 
 	// Node stuff
 	public void openNode(Debug_TreeNode node) {
 		this.addNode(node, true);
 	}
 
 	// Deprecated, deprecated, deprecated...
 	public boolean printDebug(String category, Object obj) {
 		return true;
 	}
 
 	public void removeListenerListener(Debug_Listener listener) {
 		this.filteredPanes.removeTabAt(this.filteredPanes.getSelectedIndex());
 		this.filteredOutputMap.get(listener.getThreadName()).remove(listener);
 	}
 
 	//Template Preparsing
 	public void report() {
 		System.out.println("Performance Report");
 		NumberFormat nf = NumberFormat.getInstance();
 		System.out.println("Maximum Memory Available: " + nf.format(Runtime.getRuntime().maxMemory()) + " bytes (" + Debugger.getAllocationPercentage() + "% allocated)");
 		System.out.println("Used Memory Before GC: " + nf.format(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " bytes (" + Debugger.getFreePercentage() + "% free)");
 		System.gc();
 		System.out.println("Used Memory After GC : " + nf.format(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " bytes (" + Debugger.getFreePercentage() + "% free)");
 		System.out.println("Used Memory: " + nf.format(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " bytes");
 		System.out.println("Free Memory: " + nf.format(Runtime.getRuntime().freeMemory()) + " bytes");
 		Debug_TreeNode.report();
 	}
 
 	public void reset() {
 		this.reset.setEnabled(false);
 		this.filteredPanes.setSelectedIndex(0);
 		int children = this.filteredPanes.getComponentCount();
 		for (int i = 0; i < children; i++) {
 			((Debug_Listener) this.filteredPanes.getComponent(i)).removeTab();
 		}
 		this.filteredPanes.removeAll();
 		this.filteredOutputMap.clear();
 		Debug_TreeNode.reset();
 		System.gc();
 		this.reset.setEnabled(true);
 	}
 
 	public void resetTitle(Debug_ScriptElement element) {
 		this.tabbedPane.setTitleAt(this.scriptElements.indexOf(element) + 1, element.getName());
 	}
 
 	public void setCanRedo(boolean canRedo) {
 		this.redo.setEnabled(canRedo);
 	}
 
 	public void setCanUndo(boolean canUndo) {
 		this.undo.setEnabled(canUndo);
 	}
 
 	public void setChanged(boolean changed) {
 		this.saveFile.setEnabled(changed);
 		this.tabbedPane.setTitleAt(this.tabbedPane.getSelectedIndex(), this.scriptElements.get(this.tabbedPane.getSelectedIndex() - 1).getName());
 	}
 
 	public void setExceptionsMode(boolean value) {
 		this.exceptionsMode.setSelected(value);
 		this.ignoreMode.setSelected(!value);
 	}
 
 	public void setFilteringOutput(Debug_Listener output) {
 		this.filtering = output;
 	}
 
 	public void setPriorityExecutingClass(String template) {
 		this.priorityExecutingClass = template;
 	}
 
 	public void setStatus(String text) {
 		this.status.setText(" " + text);
 	}
 
 	public void setTitleAt(int i, String title) {
 		this.tabbedPane.setTitleAt(i, title);
 	}
 
 	public void showReferenced(Debug_ScriptElement element) {
 		this.tabbedPane.setSelectedIndex(this.scriptElements.indexOf(element) + 1);
 	}
 
 	public void stateChanged(ChangeEvent e) {
 		if (this.tabbedPane.getSelectedIndex() == 0) {
 			this.closeFile.setEnabled(false);
 			this.saveFile.setEnabled(false);
 			this.saveFileAs.setEnabled(false);
 			this.editMenu.setEnabled(false);
 			this.listenerMenu.setEnabled(true);
 		} else {
 			this.listenerMenu.setEnabled(false);
 			this.editMenu.setEnabled(true);
 			this.saveFileAs.setEnabled(true);
 			this.closeFile.setEnabled(true);
 			this.setChanged(this.scriptElements.get(this.tabbedPane.getSelectedIndex() - 1).hasChanged());
 			this.setCanUndo(this.scriptElements.get(this.tabbedPane.getSelectedIndex() - 1).canUndo());
 			this.setCanRedo(this.scriptElements.get(this.tabbedPane.getSelectedIndex() - 1).canRedo());
 		}
 	}
 }
 
 class Debugger {
 	// Debug_Environment fxns
 	private static Map<String, Long> stopWatches = new HashMap<String, Long>();
 	private static Debug_Environment debugger;
 
 	public synchronized static boolean addCollectionNode(Object group, Collection list) {
 		if (list.size() == 0) {
 			return true;
 		}
 		Iterator iter = list.iterator();
 		while (iter.hasNext()) {
 			addNode(iter.next());
 		}
 		return true;
 	}
 
 	public synchronized static boolean addMapNode(Object group, Map map) {
 		if (map.size() == 0) {
 			return true;
 		}
 		Iterator iter = map.entrySet().iterator();
 		while (iter.hasNext()) {
 			Map.Entry entry = (Map.Entry) iter.next();
 			addSnapNode(entry.getKey().toString(), entry.getValue());
 		}
 		return true;
 	}
 
 	public static boolean addNode(Object o) {
 		return addNode(null, o);
 	}
 
 	public static boolean addNode(Object group, Object o) {
 		if (getDebugger().isIgnoringThisThread()) {
 			return true;
 		}
 		if (o == null) {
 			getDebugger().addNode(new Debug_TreeNode(group, "null"));
 			return true;
 		}
 		if (o instanceof Nodeable) {
 			addNodeableNode(group, (Nodeable) o);
 			if (o instanceof Exception) {
 				String exceptionName;
 				if (o instanceof Exception_Nodeable) {
 					exceptionName = ((Exception_Nodeable) o).getName();
 				} else if (o instanceof Exception_InternalError) {
 					exceptionName = ((Exception_InternalError) o).getMessage();
 				} else {
 					exceptionName = "Exception";
 				}
 				getDebugger().getUnfilteredOutput().getHotspotPanel().createHotspot(getDebugger().getLastNodeAdded(), exceptionName);
 			}
 			return true;
 		}
 		if (o instanceof Collection) {
 			return addCollectionNode(group, (Collection) o);
 		}
 		if (o instanceof Map) {
 			return addMapNode(group, (Map) o);
 		}
 		getDebugger().addNode(new Debug_TreeNode(group, o));
 		if (o instanceof Exception) {
 			String exceptionName;
 			if (o instanceof Exception_Nodeable) {
 				exceptionName = ((Exception_Nodeable) o).getName();
 			} else if (o instanceof Exception_InternalError) {
 				exceptionName = ((Exception_InternalError) o).getMessage();
 			} else {
 				exceptionName = "Exception";
 			}
 			getDebugger().getUnfilteredOutput().getHotspotPanel().createHotspot(getDebugger().getLastNodeAdded(), exceptionName);
 		}
 		return true;
 	}
 
 	public static boolean addNodeableNode(Object group, Nodeable nodeable) {
 		nodeable.nodificate();
 		return true;
 	}
 
 	public static boolean addSnapNode(Object name, Object o) {
 		return addSnapNode(null, name, o);
 	}
 
 	public static boolean addSnapNode(Object group, Object name, Object o) {
 		openNode(group, name);
 		addNode(o);
 		closeNode();
 		return true;
 	}
 
 	public static boolean atFullAllocation() {
 		return getAllocationPercentage() == 100;
 	}
 
 	public static boolean closeNode() {
 		if (getDebugger().isIgnoringThisThread()) {
 			return true;
 		}
 		getDebugger().closeNode();
 		return true;
 	}
 
 	public static boolean closeNode(Object string) {
 		addNode(string);
 		closeNode();
 		return true;
 	}
 
 	public static boolean closeNode(Object string, Object object) {
 		addSnapNode(string, object);
 		closeNode();
 		return true;
 	}
 
 	public static boolean closeNodeTo(Object string) {
 		getDebugger().closeNodeTo(string);
 		return true;
 	}
 
 	public static boolean ensureCurrentNode(Object string) {
 		return getDebugger().ensureCurrentNode(string);
 	}
 
 	public static int getAllocationPercentage() {
 		return (int) ((((double) Runtime.getRuntime().totalMemory()) / ((double) Runtime.getRuntime().maxMemory())) * 100);
 	}
 
 	public static Debug_Environment getDebugger() {
 		return debugger;
 	}
 
 	public static int getFreePercentage() {
 		return (int) ((((double) Runtime.getRuntime().freeMemory()) / ((double) Runtime.getRuntime().totalMemory())) * 100);
 	}
 
 	public static Debug_TreeNode getLastNodeAdded() {
 		return getDebugger().getLastNodeAdded();
 	}
 
 	public static String getPriorityExecutingClass() {
 		return getDebugger().getPriorityExecutingClass();
 	}
 
 	public static String getString(DebugString value) {
 		return Debug_TreeNode.getPrecached(value).toString();
 	}
 
 	public static void hitStopWatch(String name) {
 		if (stopWatches.get(name) == null) {
 			stopWatches.put(name, new Long(System.currentTimeMillis()));
 			return;
 		}
 		System.out.println("StopWatcher: " + name + " executed in " + (((double) (System.currentTimeMillis() - stopWatches.get(name).longValue())) / 1000) + " seconds");
 		stopWatches.remove(name);
 	}
 
 	public static boolean isResetting() {
 		return getDebugger().isResetting();
 	}
 
 	// Noding functions.
 	public static boolean openNode(Object string) {
 		openNode(null, string);
 		return true;
 	}
 
 	public static boolean openNode(Object group, Object string) {
 		if (getDebugger().isIgnoringThisThread()) {
 			return true;
 		}
 		getDebugger().openNode(new Debug_TreeNode(group, string));
 		return true;
 	}
 
 	public static boolean printDebug(String category) {
 		String slash = "";
 		int offset = 0;
 		if (category.charAt(0) == '/') {
 			slash = "/";
 			offset++;
 		}
 		String[] categoryArray = category.split("/");
 		return printDebug(category, "(" + slash + categoryArray[1 + offset] + ":" + categoryArray[0 + offset] + ")");
 	}
 
 	public static boolean printDebug(String category, Collection list) {
 		return printDebug(category, RiffToolbox.displayList(list));
 	}
 
 	public static boolean printDebug(String category, Object obj) {
 		return getDebugger().printDebug(category, obj);
 	}
 
 	public static void printException(Exception ex) {
 		System.out.println(ex);
 		if (ex instanceof Exception_Nodeable || ex instanceof Exception_InternalError) {
 			assert addNode("Exceptions and Errors", ex);
 		} else {
 			assert addSnapNode("Exceptions and Errors", "Exception", ex);
 		}
 	}
 
 	public static void report() {
 		getDebugger().report();
 	}
 
 	public static void reset() {
 		getDebugger().reset();
 	}
 
 	public static void setDebugger(Debug_Environment debugger) {
 		if (debugger == null) {
			Debugger.debugger = debugger;
 		}
 	}
 
 	public static void setExceptionsMode(boolean value) {
 		getDebugger().setExceptionsMode(value);
 	}
 
 	public static void setPriorityExecutingClass(String name) {
 		getDebugger().setPriorityExecutingClass(name);
 	}
 }
 
 enum DebugString {
 	ELEMENTS,
 	SCRIPTGROUPPARENTHETICAL,
 	SCRIPTGROUPCURLY,
 	NUMERICSCRIPTVALUESHORT,
 	NUMERICSCRIPTVALUEINT,
 	NUMERICSCRIPTVALUELONG,
 	NUMERICSCRIPTVALUEFLOAT,
 	NUMERICSCRIPTVALUEDOUBLE,
 	PERMISSIONNULL,
 	PERMISSIONPRIVATE,
 	PERMISSIONPROTECTED,
 	PERMISSIONPUBLIC,
 	SCRIPTLINE,
 	SCRIPTOPERATOR,
 	ORIGINALSTRING,
 	REFERENCEDELEMENTNULL,
 	OUTPUTTREE
 }
 
 class ExecutionThread extends Thread {
 	private Debug_Environment debugEnvironment;
 	public static final String EXECUTIONTHREADSTRING = "Script Execution";
 	private static int threadNum = 0;
 
 	public ExecutionThread(Debug_Environment debugEnv) {
 		super(EXECUTIONTHREADSTRING + " " + threadNum++);
 		this.debugEnvironment = debugEnv;
 	};
 
 	public void run() {
 		Debugger.hitStopWatch(Thread.currentThread().getName());
 		this.debugEnvironment.getEnvironment().execute();
 		Debugger.hitStopWatch(Thread.currentThread().getName());
 	}
 }
