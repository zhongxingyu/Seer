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
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.text.DateFormat;
 import java.util.Date;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
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
 
 	/** The TaskStore managed by this MainWindow. */
 	private TaskStore store;
 
 	private JButton addButton;
 	private JButton removeButton;
 	private JButton saveButton;
 	private JButton openButton;
 	private JToolBar toolBar;
 	private JLabel statusBar;
 	private JLabel editorBar;
 	private TaskEditor editor;
 	private JTree treeView;
 
 	/**
 	 * Initialises a new main window from the given TaskStore.
 	 * @param store the TaskStore tied to the window 
 	 */
 	public MainWindow(TaskStore store) {
 		this(store, 640, 480);
 	}
 
 	/**
 	 * Initialises a new main window from the given TaskStore and with the given initial window size.
 	 * @param store the TaskStore tied to the window 
 	 * @param width the width of the window to initialise
 	 * @param height the height of the window to initialise
 	 */
 	public MainWindow(TaskStore store, int width, int height) {
 		this.store = store;
 
 		/* set up the window */
 		this.addWindowListener(new MainWindowListener(this));
 		this.setSize(width, height);
 
 		/* build the UI */
 		this.buildUI();
 	}
 
 	/**
 	 * Immediately closes the main window without saving the tasks. This should be called once the user has decides to
 	 * close the window and the tasks have already been saved (or the user has chosen not to save them).
 	 */
 	public void close() {
 		this.setVisible(false);
 		this.dispose();
 		
 		/* make sure the task store is collected */
 		this.store = null;
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
 	 *       <li>editorPane: JPanel that contains the following components in BoxLayout
 	 *       <ul>
 	 *         <li>editorBar: the editor status bar</li>
 	 *         <li>editor: the task editor</li>
 	 *       </ul></li>
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
 	
 		this.toolBar = new JToolBar();
 		this.toolBar.add(addButton);
 		this.toolBar.add(removeButton);
 		this.toolBar.add(saveButton);
 		this.toolBar.add(openButton);
 		
 		this.addButton.addActionListener(this);
 		this.removeButton.addActionListener(this);
 		this.saveButton.addActionListener(this);
 		this.openButton.addActionListener(this);
 		
 		/* set up the status bar */
 		this.statusBar = new JLabel("");
 		
 		/* set up the JPanel that contains the editor and its status bar */
 		this.editorBar = new JLabel("No task selected");
 		this.editor = new TaskEditor();
 		
 		JPanel editorPanel = new JPanel(new BorderLayout());
 		//editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.PAGE_AXIS));
 		editorPanel.add(this.editorBar, BorderLayout.NORTH);
 		editorPanel.add(new JScrollPane(this.editor), BorderLayout.CENTER);
 		
 		/* set up the split pane that contains the task tree view and editor */
 		this.treeView = new JTree(new DefaultTreeModel(this.store.getRoot()));
 		this.treeView.setRootVisible(false);
 		this.treeView.setShowsRootHandles(true);
 		this.treeView.addTreeSelectionListener(this);
 		this.treeView.addMouseListener(new TreeViewMouseListener(this));
 		/* TODO later
 		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
 		renderer.setLeafIcon(new ImageIcon("res/note.gif"));
 		this.treeView.setCellRenderer(renderer); */
 		/* TODO add support for these:
 		this.treeView.setEditable(true);
 		this.treeView.setDragEnabled(true);
 		*/
 		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.treeView, editorPanel);
 		
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
 	 * Sets the node which is currently displayed in the editor.
 	 * @param node the node to display in the editor
 	 */
 	private void setEditorNode(DefaultMutableTreeNode node) {
 		Task task = (Task) node.getUserObject();
 		if (task != null) {
 			/* format the date for editor status bar */
 			Date date = new Date(task.getCreationTime());
 			DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
 			this.editorBar.setText("Created: " + format.format(date));
 
 			/* set the editor text and make it editable */
 			this.editor.setText(task.getText());
 			this.editor.setEditable(true);
 		} else {
 			/* no node selected; make the editor uneditable */
 			this.editorBar.setText("No task selected");
 			this.editor.setText("");
 			this.editor.setEditable(false);
 		}
 		this.validate();
 	}
 
 	private void addButtonPressed() {
 		/* ask the user for task name */
 		String name = JOptionPane.showInputDialog("Enter task name");
 		if (name == null) return;
 		
 		TreePath path = this.treeView.getSelectionPath();
 		DefaultMutableTreeNode node = this.store.getRoot();
 		if (path != null) node = (DefaultMutableTreeNode) path.getLastPathComponent();
 		Task task = (Task) node.getUserObject();
 		/* DEBUG */ if (task != null) System.out.println("Adding " + task.getName() + ", " + name);
 		DefaultMutableTreeNode newNode = this.store.addChild(node, name);
 		/* DEBUG */ this.store.print();
 		((DefaultTreeModel)this.treeView.getModel()).reload(node);
 		
 		TreeNode[] newPath = newNode.getPath();
 		TreePath treePath = new TreePath(newPath);
 		this.treeView.setSelectionPath(treePath);
 		this.treeView.expandPath(treePath);
 	}
 
 	private void removeButtonPressed() {
 		TreePath path = this.treeView.getSelectionPath();
 		if (path == null) return;
 
 		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
 		if (node.isRoot()) return;
 
 		TreeNode parent = node.getParent();
 		this.store.remove(node);
 		((DefaultTreeModel)this.treeView.getModel()).reload(parent);
 	}
 
 	private void saveButtonPressed() {
 		try { this.store.writeOut(); } catch (Exception e) { /* TODO error */ }
 	}
 
 	/** The Open button in the tool bar was pressed; open another task tree. */
 	private void openButtonPressed() {
 		File path = TaskMistress.showPathDialog();
 		if (path != null) {
 			try { new TaskMistress(path); } catch (Exception e) { /* TODO error */ }
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
 			this.window.close();
 		}
 	}
 
 	/**
 	 * Handles the event of task tree selection changing.
 	 * @param event the selection event
 	 */
 	@Override
 	public void valueChanged(TreeSelectionEvent event) {
 		/* save the text of the old selection */
 		/* TODO track changes */
 		TreePath path = event.getOldLeadSelectionPath();
 		if (path != null) {
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
 			Task task = (Task) node.getUserObject();
 			if (task != null) task.setText(this.editor.getText());
 			/* TODO save the node here; it's better to save them one at a time than all at once */ 
 		}
 		
 		/* set the editor with the text of the new selection */
 		path = event.getNewLeadSelectionPath();
 		if (path != null) {
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
 			this.setEditorNode(node);
 		}
 	}
 
 	/**
 	 * Handles the action of one of the tool bar buttons being pressed.
 	 * @param event the action event
 	 */
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if (event.getSource() == this.addButton) this.addButtonPressed();
 		else if (event.getSource() == this.removeButton) this.removeButtonPressed();
 		else if (event.getSource() == this.saveButton) this.saveButtonPressed();
 		else if (event.getSource() == this.openButton) this.openButtonPressed();
 	}
 	
 	class TreeViewMouseListener extends MouseAdapter {
 		private MainWindow window;
 		public TreeViewMouseListener(MainWindow window) {
 			this.window = window;
 		}
 		@Override
 		public void mouseClicked(MouseEvent event) {
 			int row = event.getY() / this.window.treeView.getRowHeight();
 			Rectangle r = this.window.treeView.getRowBounds(row);
 			if (r == null || !r.contains(event.getX(), event.getY())) this.window.treeView.setSelectionPath(null);
 		}
 	}
 }
