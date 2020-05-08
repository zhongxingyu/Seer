 /* MainWindow.java - Part of Task Mistress
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.TransferHandler;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 /* TODO add support for checked items; need to implement a new CellRenderer to show the additional icons. */
 
 /**
  * Implements the main window of the Task Mistress program.
  * @author anonpds <anonpds@gmail.com>
  */
 @SuppressWarnings("serial")
 public class MainWindow extends JFrame implements TreeSelectionListener, ActionListener {
 	/** Text for the button that adds a task. */
 	private static final String ADD_BUTTON_TEXT = "Add";
 
 	/** Text for the button that removes a task (tree). */
 	private static final String REMOVE_BUTTON_TEXT = "Delete";
 
 	/** Text for the button that saves all the tasks to disk. */
 	private static final String SAVE_BUTTON_TEXT = "Save";
 
 	/** Text for the button that opens another task tree. */
 	private static final String OPEN_BUTTON_TEXT = "Open";
 
 	/** Text of the settings button. */
 	private static final String SETTINGS_BUTTON_TEXT = "Settings";
 
 	/** The name of the variable that stores the window size. */
 	private static final String CONFIG_WINDOW_SIZE = "MainWindow.size";
 
 	/** Default width of the window in pixels. */
 	private static final int DEFAULT_WIDTH = 640;
 
 	/** Default height of the window in pixels. */
 	private static final int DEFAULT_HEIGHT = 400;
 
 	/** The TaskStore managed by this MainWindow. */
 	private TaskStore store;
 
 	/** Button that can be pressed to add a task. */
 	private JButton addButton;
 	
 	/** Button that can be used to remove tasks. */
 	private JButton removeButton;
 	
 	/** Button that saves the changed tasks to disk. */
 	private JButton saveButton;
 	
 	/** Button that opens another task tree in a new Task Mistress window. */
 	private JButton openButton;
 	
 	/** Button that opens the settings window. */
 	private JButton settingsButton;
 	
 	/** The tool bar which contains the action buttons. */
 	private JToolBar toolBar;
 	
 	/** Status bar that displays the status of program. */
 	private JLabel statusBar;
 	
 	/** The task view. */
 	private TaskView taskView;
 	
 	/** The tree view of the tasks. */
 	private JTree treeView;
 
 	/**
 	 * Initialises a new main window from the given TaskStore and with the given initial window size.
 	 * @param store the TaskStore tied to the window 
	 * @param width the width of the window to initialise
	 * @param height the height of the window to initialise
 	 */
 	public MainWindow(TaskStore store) {
 		this.store = store;
 
 		/* set up the window */
 		this.setTitle(TaskMistress.PROGRAM_NAME + " " + TaskMistress.PROGRAM_VERSION);
 		this.addWindowListener(new MainWindowListener(this));
 		
 		/* try to set the window size from task tree meta data */
 		Dimension d = Util.parseDimension(this.store.getVariable(CONFIG_WINDOW_SIZE));
 		if (d != null) this.setSize(d);
 		else this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
 
 		/* build the UI */
 		this.buildUI();
 	}
 
 	/**
 	 * Immediately closes the main window without saving the tasks. This should be called once the user has decided to
 	 * close the window and the tasks have already been saved (or the user has chosen not to save them).
 	 */
 	public void closeImmediately() {
 		/* write the window size */
 		this.store.setVariable(CONFIG_WINDOW_SIZE, Util.dimensionString(this.getWidth(), this.getHeight()));
 		
 		/* close the task store */
 		try {
 			this.store.close();
 		} catch (Exception e) { /* TODO error */ }
 		
 		/* make sure the task store is collected */
 		this.store = null;
 
 		/* dispose of the window */
 		this.setVisible(false);
 		this.dispose();
 	}
 
 	/**
 	 * Builds the user interface of the window. Should only be called from the constructor!
 	 * 
 	 * The current component hierarchy is as follows:
 	 * <ul>
 	 *   <li>JPanel: contains the following components in BorderLayout
 	 *   <ul>
 	 *     <li>toolBar: the program toolbar (BorderLayout.NORTH)</li>
 	 *     <li>statusBar: the status bar (BorderLayout.SOUTH)</li>
 	 *     <li>JSplitPane: split pane that with the following components 
 	 *     <ul>
 	 *       <li>taskTree: the tree view of the tasks</li>
 	 *       <li>taskView: TaskView component that displays the currently selected task</li>
 	 *     </ul></li>
 	 *   </ul>
 	 */
 	private void buildUI() {
 		this.setVisible(false);
 
 		/* set up the tool bar and its buttons */
 		this.addButton = new JButton(ADD_BUTTON_TEXT);
 		this.removeButton = new JButton(REMOVE_BUTTON_TEXT);
 		this.saveButton = new JButton(SAVE_BUTTON_TEXT);
 		this.openButton = new JButton(OPEN_BUTTON_TEXT);
 		this.settingsButton = new JButton(SETTINGS_BUTTON_TEXT);
 	
 		this.toolBar = new JToolBar();
 		this.toolBar.add(addButton);
 		this.toolBar.add(removeButton);
 		this.toolBar.add(saveButton);
 		this.toolBar.add(openButton);
 		this.toolBar.add(settingsButton);
 		
 		/* set the action listeners; the same action listener is used for all buttons */
 		this.addButton.addActionListener(this);
 		this.removeButton.addActionListener(this);
 		this.saveButton.addActionListener(this);
 		this.openButton.addActionListener(this);
 		this.settingsButton.addActionListener(this);
 		
 		/* set up the status bar */
 		this.statusBar = new JLabel("");
 		
 		/* initialise the treeView */
 		this.treeView = new JTree(this.store.getTreeModel());
 		this.treeView.setRootVisible(false);
 		this.treeView.setShowsRootHandles(true);
 		this.treeView.setDragEnabled(true);
 		this.treeView.addTreeSelectionListener(this);
 		this.treeView.addMouseListener(new TreeViewMouseListener(this));
 		this.treeView.setTransferHandler(new TreeViewTransferHandler(this));
 		this.treeView.setFocusable(true);
 		this.treeView.addKeyListener(new TreeViewKeyListener(this));
 		
 		/* TODO later:
 		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
 		renderer.setLeafIcon(new ImageIcon("res/note.gif"));
 		this.treeView.setCellRenderer(renderer);
 		*/
 		
 		/* TODO add support for this
 		this.treeView.setEditable(true);
 		*/
 
 		/* initialize the TaskView */
 		this.taskView = new TaskView();
 		
 		/* set up the split pane that contains the task tree view and editor */
 		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.treeView, this.taskView);
 		
 		/* set up the main panel and add the components*/
 		JPanel mainPanel = new JPanel(new BorderLayout());
 		mainPanel.add(this.toolBar, BorderLayout.NORTH);
 		mainPanel.add(this.statusBar, BorderLayout.SOUTH);
 		mainPanel.add(splitPane, BorderLayout.CENTER);
 		
 		/* add the components to the main window and set the window visible */
 		this.add(mainPanel);
 		this.setVisible(true);
 
 		/* adjust the split between task tree view and task editor */
 		splitPane.setDividerLocation(0.30);
 	}
 
 	/**
 	 * Returns the currently selected node in the treeView. Returns the root node if no node is selected.
 	 * @return currently selected node or root if no selection
 	 */
 	private DefaultMutableTreeNode getCurrentSelection() {
 		/* find the currently selected task; use tree root if no task selected */
 		DefaultMutableTreeNode node = this.store.getRoot();
 		TreePath path = this.treeView.getSelectionPath();
 		if (path != null) node = (DefaultMutableTreeNode) path.getLastPathComponent();
 		return node;
 	}
 		
 	/**
 	 * Finds the currently selected node and adds a child node to it. Called when the Add button in the tool bar is
 	 * activated.
 	 */
 	private void addButtonPressed() {
 		/* get the currently selected node and add a child task to it */
 		DefaultMutableTreeNode node = this.getCurrentSelection();
 		this.add(node);
 	}
 
 	/**
 	 * Adds task under the given task tree node. The task name is prompted from the user and the task is made active
 	 * in the treeView after it has been created.
 	 * @param parent the parent node under which to add the new tasks
 	 */
 	private void add(DefaultMutableTreeNode parent) {
 		/* ask the user for task name */
 		String name = JOptionPane.showInputDialog("Enter task name");
 		if (name == null) return;
 		
 		/* add the new node and inform the treeView of the changed structure */
 		DefaultMutableTreeNode newNode = this.store.add(parent, name);
 
 		/* set the added task as the current selection */
 		TreeNode[] newPath = newNode.getPath();
 		TreePath treePath = new TreePath(newPath);
 		this.treeView.setSelectionPath(treePath);
 	}
 
 	/**
 	 * Removes the currently selected task from the task tree.
 	 * Called by the tool bar button listener when the Remove button has been pressed.
 	 */
 	private void removeSelected() {
 		/* get the selection; if no selection, do nothing */
 		TreePath path = this.treeView.getSelectionPath();
 		if (path == null) return;
 
 		/* get the selected node; don't remove root node */
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
 		if (node.isRoot()) return;
 
 		/* make sure the user wants to remove the task and its children */
 		Task task = (Task) node.getUserObject();
 		int answer = JOptionPane.showConfirmDialog(this,
 		                                           "Remove '" + task.getName() + "' and its children?",
 		                                           "Confirm delete",
 		                                           JOptionPane.YES_NO_OPTION);
 		if (answer != JOptionPane.YES_OPTION) return;
 		
 		/* remove the node */
 		this.store.remove(node);
 	}
 
 	/**
 	 * Saves the task tree to disk.
 	 * Called by the tool bar button listener when the Save button has been pressed.
 	 */
 	private void saveButtonPressed() {
 		try { this.store.writeOut(); } catch (Exception e) { /* TODO errors */ }
 	}
 
 	/**
 	 * Opens another task tree in new Task Mistress window. 
 	 * Called by the tool bar button listener when the Open button has been pressed.
 	 */
 	private void openButtonPressed() {
 		/* show the path selection dialog and open the new Task Mistress window, if the user selected a path */
 		File path = TaskMistress.showPathDialog();
 		if (path != null) {
 			try { new TaskMistress(path); } catch (Exception e) { /* TODO error */ }
 		}
 	}
 
 	/**
 	 * Moves node under another node.
 	 * @param dest the destination node
 	 * @param node the node to move
 	 */
 	public void move(DefaultMutableTreeNode dest, DefaultMutableTreeNode node) {
 		try {
 			this.store.move(dest, node);
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot move node", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Class that listens to the window events of the MainWindow.
 	 * @author anonpds <anonpds@gmail.com>
 	 */
 	class MainWindowListener extends WindowAdapter {
 		/** The MainWindow this listener listens to. */
 		private MainWindow window;
 
 		/**
 		 * Constructs the listener class.
 		 * @param window the MainWindow to listen to
 		 */
 		public MainWindowListener(MainWindow window) {
 			this.window = window;
 		}
 
 		/**
 		 * Handles the event of the main window closing.
 		 * @param event the window event
 		 */
 		@Override
 		public void windowClosing(WindowEvent event) {
 			/* update the currently open task */
 			if (this.window.taskView.getTask() != null) this.window.taskView.updateText();
 			
 			/* write the data */
 			try {
 				this.window.store.writeOut();
 			} catch (Exception e) {
 				String msg = "Could not save the tasks (" + e.getMessage() + "); exit anyway?";
 				int input = JOptionPane.showConfirmDialog(this.window,
 				                                          msg,
 				                                          "Error!",
 				                                          JOptionPane.YES_NO_OPTION,
 				                                          JOptionPane.ERROR_MESSAGE);
 				if (input == JOptionPane.NO_OPTION) return;
 			}
 			this.window.closeImmediately();
 		}
 	}
 
 	/**
 	 * Handles the event of task tree selection changing.
 	 * @param event the selection event
 	 */
 	@Override
 	public void valueChanged(TreeSelectionEvent event) {
 		/* save the text of the old selection */
 		TreePath path = event.getOldLeadSelectionPath();
 		if (path != null) {
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
 			Task task = (Task) node.getUserObject();
 			if (task != null) this.taskView.updateText();
 			if (task.isDirty()) try { this.store.writeOut(node); } catch (Exception e) { /* TODO error */ }
 		}
 		
 		/* set the taskView with the Task of the new selection */
 		path = event.getNewLeadSelectionPath();
 		if (path != null) {
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
 			this.taskView.setTask((Task) node.getUserObject());
 		}
 	}
 
 	/**
 	 * Handles the action of one of the tool bar buttons being pressed.
 	 * @param event the action event
 	 */
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if (event.getSource() == this.addButton) this.addButtonPressed();
 		else if (event.getSource() == this.removeButton) this.removeSelected();
 		else if (event.getSource() == this.saveButton) this.saveButtonPressed();
 		else if (event.getSource() == this.openButton) this.openButtonPressed();
 		else if (event.getSource() == this.settingsButton) TaskMistress.showSettings();
 	}
 
 	/**
 	 * A class that listens to the mouse events of the treeView component in the MainWindow.
 	 * @author anonpds <anonpds@gmail.com>
 	 */
 	class TreeViewMouseListener extends MouseAdapter {
 		/** The MainWindow whose treeView this listener listens to. */
 		private MainWindow window;
 		
 		/**
 		 * Constructs a new listener.
 		 * @param window the window which contains the listened treeView
 		 */
 		public TreeViewMouseListener(MainWindow window) {
 			this.window = window;
 		}
 		
 		/**
 		 * Handles the event of mouse clicks on the treeView.
 		 * @param event the mouse event
 		 */
 		@Override
 		public void mouseClicked(MouseEvent event) {
 			/* calculate the treeView row in which the click occurred */
 			int row = event.getY() / this.window.treeView.getRowHeight();
 			/* get the rectangle of the row bounds; clear selection if the click is not inside any row */
 			Rectangle r = this.window.treeView.getRowBounds(row);
 			if (r == null || !r.contains(event.getX(), event.getY())) this.window.treeView.setSelectionPath(null);
 		}
 	}
 	
 	/**
 	 * A class that handles the data transfer with drag and drop events in the treeView.
 	 * @author anonpds <anonpds@gmail.com>
 	 */
 	class TreeViewTransferHandler extends TransferHandler {
 		/** The MainWindow whose data transfer is handled. */
 		private MainWindow window;
 		
 		/**
 		 * The default constructor.
 		 * @param window the MainWindow whose data transfer to handle
 		 */
 		public TreeViewTransferHandler(MainWindow window) {
 			this.window = window;
 		}
 		
 		/**
 		 * Returns the allowed actions. Only moving of elements is currently supported.
 		 * @param c the component for which to return the allowed actions (unused)
 		 */
 		@Override
 		public int getSourceActions(JComponent c) {
 			return MOVE;
 		}
 		
 		/**
 		 * Creates a new Transferable class to allow the data transfer between source and destination of the drag
 		 * and drop event.
 		 * @param source the source component for the drag and drop event
 		 */
 		@Override
 		protected Transferable createTransferable(JComponent source) {
 			/* only allow JTree as the source */
 			if (!(source instanceof JTree)) return null;
 			
 			/* set the currently selected tree component as the transferable node */
 			JTree tree = (JTree) source;
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
 			
 			/* if no selected node, return null to indicate no transfer */
 			if (node == null) return null;
 			return new TreeNodeTransferable(node);
 		}
 		
 		/**
 		 * Finishes the drag and drop event; this is where the data gets moved.
 		 * @param source source component
 		 * @param data the data to transfer
 		 * @param action the action (move, copy, cut)
 		 */
 		@Override
 		protected void exportDone(JComponent source, Transferable data, int action) {
 			/* only handle moves of TreeNodes inside JTree */
 			if (action != MOVE || !(source instanceof JTree) || !(data instanceof TreeNodeTransferable)) return;
 
 			JTree tree = (JTree) source;
 			DefaultMutableTreeNode node = ((TreeNodeTransferable)data).getNode();
 			
 			/* get the destination path; if it's null, move under root node */
 			DefaultMutableTreeNode dest = this.window.store.getRoot();
 			TreePath path = tree.getSelectionPath();
 			if (path != null) dest = (DefaultMutableTreeNode) path.getLastPathComponent();
 			
 			/* execute the move */
 			this.window.move(dest, node);
 		}
 		
 		/**
 		 * Tells whether something can be transferred through drag and drop. Always returns true, because the filtering
 		 * is done elsewhere (createTransferable and exportDone only accept particular classes as parameters).
 		 * @return always true
 		 */
 		@Override
 		public boolean canImport(TransferSupport support) {
 			return true;
 		}
 		
 		/**
 		 * No idea what this function is actually for, but doesn't work without it.
 		 * @return always returns true
 		 */
 		@Override
 		public boolean importData(TransferSupport support) {
 			return true;
 		}
 	}
 	
 	/**
 	 * Sub-class of Transferable that can transfer DefaultMutableTreeNode objects.
 	 * @author anonpds <anonpds@gmail.com>
 	 */
 	class TreeNodeTransferable implements Transferable {
 		/** The node to transfer. */
 		private DefaultMutableTreeNode node;
 		
 		/** The "flavours" of data accepted. */
 		private DataFlavor[] flavor;
 
 		/**
 		 * Default constructor.
 		 * @param node the node to transfer
 		 */
 		public TreeNodeTransferable(DefaultMutableTreeNode node) {
 			this.node = node;
 			/* create a list of the accepted data "flavours"; only DefaultMutableTreeNode classes are accepted */
 			this.flavor = new DataFlavor[1];
 			this.flavor[0] = new DataFlavor(DefaultMutableTreeNode.class, "DefaultMutableTreeNode");
 		}
 		
 		/**
 		 * Returns the node that is being transferred.
 		 * @return the transferred node
 		 */
 		public DefaultMutableTreeNode getNode() {
 			return this.node;
 		}
 
 		/**
 		 * Returns the transfer data of the specified "flavour".
 		 * @param flavor the flavour of the data to receive
 		 * @return the data object in the given flavour
 		 */
 		/* TODO is this actually needed? Or perhaps getNode() should be removed and this used instead? */
 		@Override
 		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
 			if (!this.isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
 			return node;
 		}
 
 		/**
 		 * Returns the list of accepted data "flavours".
 		 * @return list of accepted flavours
 		 */
 		@Override
 		public DataFlavor[] getTransferDataFlavors() {
 			return this.flavor;
 		}
 
 		/**
 		 * Tells whether a particular data flavour is supported.
 		 * @param flavor the flavour to dest
 		 * @return true if the flavour is supported, false if not
 		 */
 		@Override
 		public boolean isDataFlavorSupported(DataFlavor flavor) {
 			for (int i = 0; i < this.flavor.length; i++)
 				if (this.flavor[i].equals(flavor)) return true;
 			return false;
 		}
 	}
 	
 	/**
 	 * Listens to the key events of treeView.
 	 * @author anonpds <anonpds@gmail.com>
 	 */
 	class TreeViewKeyListener extends KeyAdapter {
 		/** The MainWindow in which the listened treeView resides. */
 		private MainWindow window;
 
 		/**
 		 * Default constructor.
 		 * @param window the MainWindow which contains the listened treeView.
 		 */
 		public TreeViewKeyListener(MainWindow window) {
 			this.window = window;
 		}
 
 		/**
 		 * Handles the event of a key being typed.
 		 * @param e the key event
 		 */
 		@Override
 		public void keyTyped(KeyEvent e) {
 			if (e.getKeyChar() == KeyEvent.VK_ENTER) {
 				/* enter pressed: add task */
 				DefaultMutableTreeNode node = this.window.getCurrentSelection();
 				
 				/* if shirt depressed, add as sibling, otherwise as a child */
 				if (e.isShiftDown()) node = (DefaultMutableTreeNode) node.getParent();
 				if (node != null) this.window.add(node);
 			} else if (e.getKeyChar() == KeyEvent.VK_DELETE) {
 				this.window.removeSelected();
 			}
 		}
 	}
 }
