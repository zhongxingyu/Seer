 package ClassAdminFrontEnd;
 
 import javax.swing.*;
 
 import ClassAdminBackEnd.EntityType;
 import ClassAdminBackEnd.Project;
 import Frames.FrmNewNode;
 import Frames.FrmUpdateNode;
 
 import prefuse.Visualization;
 import prefuse.controls.ControlAdapter;
 import prefuse.data.Table;
 import prefuse.data.Tree;
 import prefuse.visual.VisualItem;
 
 import java.awt.event.*;
 import java.util.LinkedList;
 
 public class PopUpMenu {
 
 	JPopupMenu pMenu;
 	TreeView tview;
 	VisualItem activeItem = null;
 	Tree activeTree = null;
 	Project activeProject;
 	EntityType activeEntity;
 	JDialog frame = null;
 	LinkedList<EntityType> activeTreeLinkedList = null;
 	FrmNewNode newNode;
 	FrmUpdateNode updateNode;
 	JFrame parentFrame;
 
 	public PopUpMenu() {
 		pMenu = new JPopupMenu();
 		JMenuItem miAddChild = new JMenuItem("Add Child");
 		pMenu.add(miAddChild);
 		JMenuItem miEdit = new JMenuItem("Edit");
 		pMenu.add(miEdit);
 		JMenuItem miRemoveWC = new JMenuItem("Remove with children");
 		pMenu.add(miRemoveWC);
 		JMenuItem miRemoveWOC = new JMenuItem("Remove without children");
 		pMenu.add(miRemoveWOC);
 
 		miAddChild.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				VisualItem item = activeItem;
 				if (item.toString().contains("ode")) {
 					int p = item.getRow(); //get parent id
 					newNode.showFrmNewNode(p); //show new node form with parent in place
 				}
 			}
 		});
 
 		miEdit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (activeItem.canSetString("name")) {
 					updateNode.showFrmUpdateNode(0); // show update node form with information
 				}
 			}
 		});
 
 		miRemoveWC.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				VisualItem item = activeItem;
 
 				int i = item.getRow();
 				activeProject.getAudit().RemoveNode(item.getString("name"), true); //create audit entry
 				activeTree.removeNode(i); //remove node from front end
 
 				activeTreeLinkedList.get(i).removeDeletingChildren(); //remove node in back end
 				activeProject.updateTables(); //update front end
 
 				parentFrame.dispose(); //recreate form
 				TreeView.createEntityTypeFrm("name", activeProject);
 			}
 		});
 
 		miRemoveWOC.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				VisualItem item = activeItem;
 				int i = item.getRow(); 
 				int source = -1, target = -1;
 				Table edgeTable = activeTree.getEdgeTable();
 				
 				//get node's parent
 				for (int r = 0; r < edgeTable.getRowCount(); r++) {
 					if (edgeTable.getInt(r, 1) == i) {
 						source = edgeTable.getInt(r, 0);
 					}
 				}
 				
 				//update edges between node's children and node's parent
 				for (int r = 0; r < edgeTable.getRowCount(); r++) {
 					if (edgeTable.getInt(r, 0) == i) {
 						target = edgeTable.getInt(r, 1);
 						activeTree.removeEdge(activeTree.getEdge(edgeTable.getInt(r, 0), edgeTable.getInt(r, 1)));
 						activeTree.addEdge(source, target);
 					}
 				}
 				activeProject.getAudit().RemoveNode(item.getString("name"), false); //create audit entry
 				activeTree.removeNode(i); //remove node from front end
 
 				activeTreeLinkedList.get(i).removeSavingChildren(); //remove node from back end
 				activeProject.updateTables(); //update front end information
 				parentFrame.dispose(); //recreate form
 				TreeView.createEntityTypeFrm("name", activeProject);
 
 			}
 		});
 
 	}
 
 	public void setTreeView(TreeView treeView, Tree tree, Project project, JFrame pFrame) {
 		activeProject = project;
 		activeTreeLinkedList = activeProject.getTreeLinkedList();
 		activeTree = tree;
 		tview = treeView;
 		parentFrame = pFrame;
 		//add control listener for nodes on front end treeview
 		tview.addControlListener(new ControlAdapter() {
 			public void itemReleased(VisualItem item, MouseEvent e) {
 				activeItem = null;
 				if (e.isPopupTrigger()) {
 					if (item.canGetString("name")) {
 						pMenu.show(e.getComponent(), e.getX(), e.getY());
 						activeItem = item;
 						activeItem.getVisualization().run("filter");
 						activeEntity = activeTreeLinkedList.get(activeItem.getRow());
						newNode = new FrmNewNode(activeTree, activeProject, parentFrame, tview);
						updateNode = new FrmUpdateNode(activeProject, parentFrame, activeEntity, activeItem);
 					}
 				}
 			}
 		});
 	}
 
 }
