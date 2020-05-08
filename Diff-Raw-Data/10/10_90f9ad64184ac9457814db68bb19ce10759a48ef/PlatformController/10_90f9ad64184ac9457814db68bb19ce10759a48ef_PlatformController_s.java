 package org.bh.platform;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import org.bh.gui.swing.BHButton;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.gui.swing.BHMenuItem;
 import org.bh.gui.swing.IBHComponent;
 import org.bh.platform.actionkeys.PlatformActionKey;
 import org.bh.platform.i18n.BHTranslator;
 
 /**
  * The Platform Controller handles a) start up of the application b) main
  * application flow c) all events which are fired by platform controls (e.g.
  * toolbar-button klicks or menu klicks)
  * 
  * 
  * 
  * @author Alexander Schmalzhaf
  * @author Patrick Tietze
  * 
  * @version 0.1 2009/12/22 Alexander Schmalzhaf
  */
 
 public class PlatformController {
 
 	private BHMainFrame bhmf;
 
 	public PlatformController() {
 
 		// start mainFrame
 		bhmf = new BHMainFrame(BHTranslator.getInstance().translate("title"));
 
 		// handle events...
 
 		PlatformActionListener pal = new PlatformActionListener();
 
 		// add ActionListener to...
 		// .. the toolbar
 		for (BHButton button : BHButton.getPlatformButtons()) {
 			button.addActionListener(pal);
 		}
 
 		// ...the menu
 		for (BHMenuItem menuItem : BHMenuItem.getPlatformMenuItems()) {
 			menuItem.addActionListener(pal);
 		}
 
 	}
 
 	/**
 	 * The PlatformActionListener handles all actions that are fired by a button
 	 * etc. of the platform.
 	 */
 	class PlatformActionListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent aEvent) {
 
 			// get actionKey of fired action
 			PlatformActionKey actionKey = null;
 			if (((IBHComponent) aEvent.getSource()) instanceof BHMenuItem)
 				actionKey = ((BHMenuItem) aEvent.getSource()).getActionKey();
 
 			if (((IBHComponent) aEvent.getSource()) instanceof BHButton)
 				// actionKey = ((BHButton)aEvent.getSource());
 
 				System.out
 						.println(((IBHComponent) aEvent.getSource()).getKey());
 
 			switch (actionKey) {
 			case FILENEW:
 				System.out.println("FILENEW gefeuert");
 				break;
 			case FILEOPEN:
 				System.out.println("FILEOPEN gefeuert");
				bhmf.getChooser().showOpenDialog(bhmf);
 				break;
 			default:
 				System.out.println("Irgendwas gefeuert...");
 				break;
 			}
 		}
 
 	}
 
 	// @Override
 	// public void mouseEntered(MouseEvent e) {
 	// this.bhStatusBar.setToolTip(getToolTip());
 	// //BHStatusBar.setValidationToolTip(new JLabel("Test"));
 	// }
 	// @Override
 	// public void mouseExited(MouseEvent e) {
 	// this.bhStatusBar.setToolTip("");
 	// }
 	//	
 	//	
 	//	
 	// @Override
 	// public void actionPerformed(ActionEvent actionEvent) {
 	// String cmd = actionEvent.getActionCommand();
 	//	        
 	// // Handle each button.
 	// if (cmd.equals("addP")) { //add project button clicked
 	// System.out.println("add project");
 	// BHTree.addProject("New Project " + BHTree.getNodeSuffix());
 	// BHMainFrame.addContentForms(new BHFormsPanel());
 	//	    	
 	// } else if(cmd.equals("addS")){
 	// BHTree.addScenario("New Scenario");
 	// BHMainFrame.addContentFormsAndChart(new BHFormsPanel(), new JPanel());
 	// } else if (cmd.equals("remove")) {
 	// //Remove button clicked
 	// BHTree.removeCurrentNode();
 	//            
 	// } else if (cmd.equals("delete")) {
 	// //Clear button clicked.
 	// BHTree.clear();
 	// BHTree.setNodeSuffix();
 	// } else if(cmd.equals("open")){
 	// fc = new JFileChooser();
 	// fc.setPreferredSize(new Dimension(600,400));
 	// fc.showOpenDialog(this);
 	//        	
 	// }
 	//	       
 	// //Action of Tree
 	// public static DefaultMutableTreeNode addScenario(Object child) {
 	//	    	
 	// TreePath parentPath = tree.getSelectionPath();
 	// System.out.println(parentPath);
 	//	        
 	// DefaultMutableTreeNode parentNode =
 	// (DefaultMutableTreeNode)(parentPath.getPathComponent(1));
 	//	        
 	// DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
 	//	        
 	// //It is key to invoke this on the TreeModel, and NOT
 	// DefaultMutableTreeNode
 	// treeModel.insertNodeInto(childNode, parentNode,
 	// parentNode.getChildCount());
 	//
 	// //Make sure the user can see the lovely new node.
 	// tree.scrollPathToVisible(new TreePath(childNode.getPath()));
 	//	        
 	// return childNode;
 	// }
 	//	    
 	// public static DefaultMutableTreeNode addProject(Object child) {
 	//	    	
 	// DefaultMutableTreeNode parentNode = rootNode;
 	//	        
 	// DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
 	//	       
 	// //It is key to invoke this on the TreeModel, and NOT
 	// DefaultMutableTreeNode
 	// treeModel.insertNodeInto(childNode, parentNode,
 	// parentNode.getChildCount());
 	//
 	// //Make sure the user can see the lovely new node.
 	// tree.scrollPathToVisible(new TreePath(childNode.getPath()));
 	//	        
 	// return childNode;
 	// }
 	//	    
 	// /** Remove the currently selected node. */
 	// public static void removeCurrentNode() {
 	// TreePath currentSelection = tree.getSelectionPath();
 	// if (currentSelection != null) {
 	// DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
 	// (currentSelection.getLastPathComponent());
 	// MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
 	// if (parent != null) {
 	// treeModel.removeNodeFromParent(currentNode);
 	// return;
 	// }
 	// }
 	//
 	// // Either there was no selection, or the root was selected.
 	// toolkit.beep();
 	// }
 	//	    
 	// //TreeListener
 	// class MyTreeModelListener implements TreeModelListener {
 	// public void treeNodesChanged(TreeModelEvent e) {
 	// DefaultMutableTreeNode node;
 	// node = (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());
 	//
 	// /*
 	// * If the event lists children, then the changed
 	// * node is the child of the node we've already
 	// * gotten. Otherwise, the changed node and the
 	// * specified node are the same.
 	// */
 	//
 	// int index = e.getChildIndices()[0];
 	// node = (DefaultMutableTreeNode)(node.getChildAt(index));
 	//
 	// System.out.println("The user has finished editing the node.");
 	// System.out.println("New value: " + node.getUserObject());
 	// }
 	// public void treeNodesInserted(TreeModelEvent e) {
 	// }
 	// public void treeNodesRemoved(TreeModelEvent e) {
 	// }
 	// public void treeStructureChanged(TreeModelEvent e) {
 	// }
 	// }
 	//	    
 	//	    
 	// }
 
 }
