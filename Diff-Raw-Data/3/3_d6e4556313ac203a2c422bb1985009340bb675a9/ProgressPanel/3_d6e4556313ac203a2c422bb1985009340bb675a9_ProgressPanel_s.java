 /**
  *
  */
 package org.selfip.bkimmel.progress;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.tree.TreePath;
 
 import org.jdesktop.swingx.JXTreeTable;
 import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
 import org.selfip.bkimmel.ui.TreeUtil;
 import org.selfip.bkimmel.ui.renderer.ProgressBarRenderer;
 
 /**
  * Displays a hierarchy of <code>ProgressMonitor</code>s in a tree table.
  * @author brad
  */
 public final class ProgressPanel extends JPanel implements ProgressMonitor {
 
 	/** Serialization version ID. */
 	private static final long serialVersionUID = 1L;
 
 	/** The <code>ProgressModel</code> describing the structure of the tree. */
 	private final ProgressModel model;  //  @jve:decl-index=0:
 
 	/** The top level <code>ProgressMonitor</code>. */
 	private ProgressMonitor monitor = null;
 
 	/**
 	 * The <code>JXTreeTable</code> in which to display the progress monitors.
 	 */
 	private JXTreeTable progressTree = null;
 
 	/**
 	 * This is the default constructor
 	 */
 	public ProgressPanel() {
 		super();
 		this.model = new ProgressModel();
 		initialize();
 	}
 
 	/**
 	 * Creates a new <code>ProgressPanel</code>.
 	 * @param title The title to apply to the root node.
 	 */
 	public ProgressPanel(String title) {
 		super();
 		this.model = new ProgressModel(title);
 		initialize();
 	}
 
 	/**
 	 * This method initializes this
 	 *
 	 * @return void
 	 */
 	private void initialize() {
 		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
 		gridBagConstraints1.fill = GridBagConstraints.BOTH;
 		gridBagConstraints1.gridy = 1;
 		gridBagConstraints1.weightx = 1.0;
 		gridBagConstraints1.weighty = 1.0;
 		gridBagConstraints1.gridx = 0;
 		this.setSize(300, 200);
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
 		gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints2.gridx = 0;
 		gridBagConstraints2.gridy = 0;
 		gridBagConstraints2.weightx = 1.0;
 		gridBagConstraints2.weighty = 0.0;
 		this.add(getProgressTree().getTableHeader(), gridBagConstraints2);
 		this.add(new JScrollPane(getProgressTree()), gridBagConstraints1);
 	}
 
 	/**
 	 * Sets a value indicating whether the root progress node is visible.
 	 * @param visible true to make the root node visible, false otherwise.
 	 */
 	public void setRootVisible(boolean visible) {
 		getProgressTree().setRootVisible(visible);
 	}
 
 	/**
 	 * Gets the top level <code>ProgressMonitor</code>.
 	 * @return The top level <code>ProgressMonitor</code>.
 	 */
 	private ProgressMonitor getProgressMonitor() {
 		if (monitor == null) {
 			monitor = new SynchronizedProgressMonitor(model.getRootNode());
 		}
 		return monitor;
 	}
 
 	/**
 	 * Provides a model for displaying the progress tree.
 	 * @author brad
 	 */
 	private static final class ProgressModel extends AbstractTreeTableModel {
 
 		/** The names of the columns in the table. */
 		private final String[] columnNames = { "Title", "Progress", "Status" };
 
 		/** The types of the columns in the table. */
 		private final Class<?>[] columnTypes = { String.class, JProgressBar.class, String.class };
 
 		/**
 		 * Creates a new <code>ProgressModel</code>.
 		 */
 		private ProgressModel() {
 			super(new Node());
 			getRootNode().setModel(this);
 		}
 
 		/**
 		 * Creates a new <code>ProgressModel</code>.
 		 * @param title The title to apply to the root node.
 		 */
 		private ProgressModel(String title) {
 			super(new Node(title));
 			getRootNode().setModel(this);
 		}
 
 		/**
 		 * Gets root <code>Node</code> of the tree.
 		 * @return The root <code>Node</code>.
 		 */
 		public Node getRootNode() {
 			return ((Node) super.root);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.jdesktop.swingx.treetable.TreeTableModel#getColumnCount()
 		 */
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#getColumnName(int)
 		 */
 		public String getColumnName(int column) {
 			return columnNames[column];
 		}
 
 		/* (non-Javadoc)
 		 * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#getHierarchicalColumn()
 		 */
 		@Override
 		public int getHierarchicalColumn() {
 			return 0;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.selfip.bkimmel.ui.treetable.AbstractTreeTableModel#getColumnClass(int)
 		 */
 		@Override
 		public Class<?> getColumnClass(int column) {
 			return columnTypes[column];
 		}
 
 		/* (non-Javadoc)
 		 * @see org.jdesktop.swingx.treetable.TreeTableModel#getValueAt(java.lang.Object, int)
 		 */
 		public Object getValueAt(Object node, int column) {
 
 			switch (column) {
 
 			case 0: // title
 				return ((Node) node).getTitle();
 
 			case 1: // progress
 				return ((Node) node).progressBar;
 
 			case 2: // status
 				return ((Node) node).getStatus();
 
 			}
 
 			return null;
 
 		}
 
 		/* (non-Javadoc)
 		 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
 		 */
 		public Object getChild(Object parent, int index) {
 			return ((Node) parent).children.get(index);
 		}
 
 		/* (non-Javadoc)
 		 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
 		 */
 		public int getChildCount(Object parent) {
 			return ((Node) parent).children.size();
 		}
 
 		/* (non-Javadoc)
 		 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
 		 */
 		public int getIndexOfChild(Object parent, Object child) {
 			return ((Node) parent).children.indexOf(child);
 		}
 
 		/**
 		 * Represents a single node in a progress tree.
 		 * @author brad
 		 */
 		private static final class Node extends AbstractProgressMonitor {
 
 			/** The <code>JProgressBar</code> for this node. */
 			private final JProgressBar progressBar = new JProgressBar();
 
 			/** The <code>List</code> of this node's children. */
 			private final List<Node> children = new ArrayList<Node>();
 
 			/**
 			 * The <code>ProgressModel</code> for which this node is a member.
 			 */
 			private ProgressModel model;
 
 			/**
 			 * The parent <code>Node</code>.
 			 */
 			private Node parent;
 
 			/**
 			 * Creates a new root <code>Node</code>.
 			 */
 			public Node() {
 				super();
 				model = null;
 				parent = null;
 			}
 
 			/**
 			 * Creates a new root <code>Node</code>.
 			 * @param title The title to use to label the node.
 			 */
 			public Node(String title) {
 				super(title);
 				model = null;
 				parent = null;
 			}
 
 			/**
 			 * Creates a new <code>Node</code>.
 			 * @param title The title to use to label the node.
 			 * @param parent The parent node.
 			 */
 			private Node(String title, Node parent) {
 				super(title);
 				assert(model != null);
 				this.model = parent.model;
 				this.parent = parent;
 			}
 
 			/**
 			 * Sets the <code>ProgressModel</code> for which this node is a
 			 * member.
 			 * @param model The <code>ProgressModel</code>.
 			 */
 			private void setModel(ProgressModel model) {
 				this.model = model;
 			}
 
 			/**
 			 * Populates the provided list with the ancestors of this node
 			 * (including this node), starting at the root.
 			 * @param path The <code>List</code> to populate.
 			 */
 			private void accumulatePath(List<Object> path) {
 				if (parent != null) {
 					parent.accumulatePath(path);
 				}
 				path.add(this);
 			}
 
 			/**
 			 * Gets the <code>TreePath</code> to this node.
 			 * @return The <code>TreePath</code> to this node.
 			 */
 			private TreePath getPath() {
 				return getPath(this);
 			}
 
 			/**
 			 * Gets the <code>TreePath</code> to the specified node.
 			 * @param node The <code>Node</code> for which to obtain the
 			 * 		<code>TreePath</code>.
 			 * @return The <code>TreePath</code> to <code>node</code>.
 			 */
 			private static TreePath getPath(Node node) {
 				List<Object> path = new ArrayList<Object>();
 				if (node != null) {
 					node.accumulatePath(path);
 				}
 				return new TreePath(path.toArray());
 			}
 
 			/* (non-Javadoc)
 			 * @see org.jmist.framework.reporting.AbstractProgressMonitor#createChildProgressMonitor(java.lang.String)
 			 */
 			public ProgressMonitor createChildProgressMonitor(String title) {
 
 				int index = children.size();
 				Node child = new Node(title, this);
 				children.add(child);
 
 				model.modelSupport.fireChildAdded(getPath(), index, child);
 
 				return child;
 
 			}
 
 			/* (non-Javadoc)
 			 * @see org.jmist.framework.reporting.AbstractProgressMonitor#notifyCancelled()
 			 */
 			@Override
 			public void notifyCancelled() {
 				super.notifyCancelled();
 				detach();
 			}
 
 			/* (non-Javadoc)
 			 * @see org.jmist.framework.reporting.AbstractProgressMonitor#notifyComplete()
 			 */
 			@Override
 			public void notifyComplete() {
 				super.notifyComplete();
 				detach();
 			}
 
 			/**
 			 * Removes this node from its parent.
 			 */
 			private void detach() {
 
 				if (parent != null) {
 
 					int index = parent.children.indexOf(this);
 					parent.children.remove(index);
 
 					model.modelSupport.fireChildRemoved(parent.getPath(), index, this);
 
 					parent = null;
 
 				}
 
 			}
 
 			/* (non-Javadoc)
 			 * @see org.jmist.framework.reporting.AbstractProgressMonitor#notifyIndeterminantProgress()
 			 */
 			@Override
 			public boolean notifyIndeterminantProgress() {
 				progressBar.setIndeterminate(true);
 				model.modelSupport.firePathChanged(getPath());
 				return super.notifyIndeterminantProgress();
 			}
 
 			/* (non-Javadoc)
 			 * @see org.jmist.framework.reporting.AbstractProgressMonitor#notifyProgress(double)
 			 */
 			@Override
 			public boolean notifyProgress(double progress) {
 				progressBar.setMaximum(100);
 				progressBar.setValue((int) (100.0 * progress));
 				model.modelSupport.firePathChanged(getPath());
 				return super.notifyProgress(progress);
 			}
 
 			/* (non-Javadoc)
 			 * @see org.jmist.framework.reporting.AbstractProgressMonitor#notifyProgress(int, int)
 			 */
 			@Override
 			public boolean notifyProgress(int value, int maximum) {
 				progressBar.setMaximum(maximum);
 				progressBar.setValue(value);
 				model.modelSupport.firePathChanged(getPath());
 				return super.notifyProgress(value, maximum);
 			}
 
 			/* (non-Javadoc)
 			 * @see org.jmist.framework.reporting.AbstractProgressMonitor#notifyStatusChanged(java.lang.String)
 			 */
 			@Override
 			public void notifyStatusChanged(String status) {
 				super.notifyStatusChanged(status);
 				model.modelSupport.firePathChanged(getPath());
 			}
 
 		} // class Node
 
 	} // class ProgressModel
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#createChildProgressMonitor(java.lang.String)
 	 */
 	public ProgressMonitor createChildProgressMonitor(String title) {
 		return getProgressMonitor().createChildProgressMonitor(title);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#isCancelPending()
 	 */
 	public boolean isCancelPending() {
 		return getProgressMonitor().isCancelPending();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyCancelled()
 	 */
 	public void notifyCancelled() {
 		getProgressMonitor().notifyCancelled();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyComplete()
 	 */
 	public void notifyComplete() {
 		getProgressMonitor().notifyComplete();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyIndeterminantProgress()
 	 */
 	public boolean notifyIndeterminantProgress() {
 		return getProgressMonitor().notifyIndeterminantProgress();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyProgress(int, int)
 	 */
 	public boolean notifyProgress(int value, int maximum) {
 		return getProgressMonitor().notifyProgress(value, maximum);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyProgress(double)
 	 */
 	public boolean notifyProgress(double progress) {
 		return getProgressMonitor().notifyProgress(progress);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyStatusChanged(java.lang.String)
 	 */
 	public void notifyStatusChanged(String status) {
 		getProgressMonitor().notifyStatusChanged(status);
 	}
 
 	/**
 	 * This method initializes progressTree
 	 * @param title
 	 *
 	 * @return org.jdesktop.swingx.JXTreeTable
 	 */
 	private JXTreeTable getProgressTree() {
 		if (progressTree == null) {
 			progressTree = new JXTreeTable(model);
 			progressTree.setRootVisible(true);
 			TreeUtil.enableAutoExpansion(progressTree);
 			ProgressBarRenderer.applyTo(progressTree);
 		}
 		return progressTree;
 	}
 
 }
