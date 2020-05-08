 /*******************************************************************************
  * Copyright (c) 2008 Siemens AG
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Kai Toedter - initial API and implementation
  *******************************************************************************/
 
 package com.siemens.ct.pm.ui.views.treeview;
 
 import java.awt.Color;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 
 import com.siemens.ct.pm.application.service.ISelectionService;
 import com.siemens.ct.pm.application.service.IViewContribution;
 import com.siemens.ct.pm.model.IPerson;
 import com.siemens.ct.pm.model.IPersonListener;
 import com.siemens.ct.pm.model.IPersonManager;
 import com.siemens.ct.pm.model.event.PersonEvent;
 
 public class TreeView implements IViewContribution, IPersonListener {
 
 	private final ImageIcon icon;
 	private final JComponent view;
 	private IPersonManager personManager;
 	private ISelectionService selectionService;
 
 	private final JTree tree;
 	private final DefaultMutableTreeNode top;
 
 	public TreeView() {
 		super();
 		icon = new ImageIcon(this.getClass().getResource("/icons/folder.png"));
 		top = new DefaultMutableTreeNode("Persons");
 
 		tree = new JTree(top);
 		view = new JScrollPane(tree);
 		view.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2,
 				2), BorderFactory.createLineBorder(Color.lightGray)));
 
 		ImageIcon folderIcon = new ImageIcon(this.getClass().getResource("/icons/folder.png"));
 		ImageIcon leafIcon = new ImageIcon(this.getClass().getResource("/icons/user.png"));
 		if (leafIcon != null) {
 			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
 			renderer.setLeafIcon(leafIcon);
 			renderer.setOpenIcon(folderIcon);
 			renderer.setClosedIcon(folderIcon);
 			tree.setCellRenderer(renderer);
 		}
 
 		tree.addTreeSelectionListener(new TreeSelectionListener() {
 			public void valueChanged(TreeSelectionEvent e) {
 				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
 						.getLastSelectedPathComponent();
 				if (node != null) {
 					Object object = node.getUserObject();
 					if (selectionService != null) {
 						selectionService.objectSelected(object);
 					}
 				}
 			}
 		});
 	}
 
 	private void createNodes(DefaultMutableTreeNode top) {
 		List<IPerson> persons = personManager.getPersons();
 
 		for (IPerson person : persons) {
 			boolean companyFound = false;
 			DefaultMutableTreeNode companyNode = null;
 			for (int i = 0; i < top.getChildCount(); i++) {
 				companyNode = (DefaultMutableTreeNode) top.getChildAt(i);
 				if (companyNode.getUserObject().toString().equals(person.getCompany())) {
 					companyFound = true;
 					break;
 				}
 			}
 			if (!companyFound) {
 				companyNode = new DefaultMutableTreeNode(person.getCompany());
 				top.add(companyNode);
 			}
 			companyNode.add(new DefaultMutableTreeNode(person));
 		}
 	}
 
 	@Override
 	public Icon getIcon() {
 		return icon;
 	}
 
 	@Override
 	public String getName() {
 		return "Tree View";
 	}
 
 	@Override
 	public JComponent getView() {
 		return view;
 	}
 
	public synchronized void removeSelectionService() {
		selectionService = null;
 	}
 
 	public synchronized void setSelectionService(ISelectionService selectionService) {
 		this.selectionService = selectionService;
 	}
 
	public synchronized void removePersonManager() {
		personManager = null;
 		top.removeAllChildren();
 		((DefaultTreeModel) tree.getModel()).reload(top);
 	}
 
 	public synchronized void setPersonManager(IPersonManager personManager) {
 		this.personManager = personManager;
 		createNodes(top);
 	}
 
 	@Override
 	public void handleEvent(PersonEvent event) {
 		if (event.getType() == PersonEvent.Type.DELETE) {
 			DefaultMutableTreeNode node = searchNode(event.getPerson());
 			if (node == null) {
 				return;
 			}
 
 			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) (node.getParent());
 			if (parentNode == null) {
 				return;
 			}
 
 			parentNode.remove(node);
 			if (parentNode.getChildCount() == 0) {
 				// delete company if there
 				// are no persons anymore
 				top.remove(parentNode);
 				((DefaultTreeModel) tree.getModel()).reload(top);
 			} else {
 				((DefaultTreeModel) tree.getModel()).reload(parentNode);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public DefaultMutableTreeNode searchNode(Object userObject) {
 		DefaultMutableTreeNode node = null;
 		Enumeration nodes = top.breadthFirstEnumeration();
 
 		while (nodes.hasMoreElements()) {
 			node = (DefaultMutableTreeNode) nodes.nextElement();
 			if (userObject == node.getUserObject()) {
 				return node;
 			}
 		}
 		return null;
 	}
 }
