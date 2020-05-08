 package com.robonobo.gui.components;
 
 import java.awt.Dimension;
 
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.tree.*;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.gui.frames.RobonoboFrame;
 import com.robonobo.gui.model.SelectableTreeNode;
 
 /**
  * Whenever part of the tree is expanded or collapsed, it updates its max size so that the layout reflows properly. Also
  * remembers selection when collapsed & expanded
  * 
  * @author macavity
  * 
  */
 public class LeftSidebarTree extends JTree implements LeftSidebarComponent {
 	protected TreePath selTreePath;
 	protected RobonoboFrame frame;
 
 	public LeftSidebarTree(TreeModel newModel, RobonoboFrame f) {
 		super(newModel);
 		this.frame = f;
		// Single click expands/collapses
		setToggleClickCount(1);
 		// Fill up width, but not height
 		setMaximumSize(new Dimension(Integer.MAX_VALUE, getMaximumSize().height));
 		addTreeExpansionListener(new TreeExpansionListener() {
 			public void treeExpanded(TreeExpansionEvent event) {
 				updateMaxSize();
 				// If necessary, reselect our node
 				if (selTreePath != null && event.getPath().isDescendant(selTreePath))
 					setSelectionPath(selTreePath);
 			}
 
 			public void treeCollapsed(TreeExpansionEvent event) {
 				updateMaxSize();
 			}
 		});
 	}
 
 	void updateMaxSize() {
 		// Fill width, but not height
 		setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
 	}
 
 	@Override
 	public void setSelectionPath(TreePath path) {
 		if (path != null) {
 			Object lastComp = path.getLastPathComponent();
 			if (lastComp != null && lastComp instanceof SelectableTreeNode) {
 				SelectableTreeNode stn = (SelectableTreeNode) lastComp;
 				if (stn.wantSelect())
 					selTreePath = path;
 			}
 		}
 		super.setSelectionPath(path);
 	}
 
 	@Override
 	public void relinquishSelection() {
 		((SelectionModel) getSelectionModel()).reallyClearSelection();
 		selTreePath = null;
 	}
 
 	public boolean isSelectedNode(SelectableTreeNode node) {
 		return (selTreePath != null) && (selTreePath.equals(new TreePath(node.getPath())));
 	}
 	
 	/**
 	 * Stop Swing from deselecting us at its twisted whim
 	 */
 	class SelectionModel extends DefaultTreeSelectionModel {
 		@Override
 		public void clearSelection() {
 			// Do nothing
 		}
 
 		public void reallyClearSelection() {
 			super.clearSelection();
 		}
 	}
 }
