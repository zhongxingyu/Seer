 /*
  * mongobrowser - a webstart gui application for viewing, 
  *                editing and administering a Mongo Database
  * Copyright 2009-2011 MeBigFatGuy.com
  * Copyright 2009-2011 Dave Brosius
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0 
  *    
  * Unless required by applicable law or agreed to in writing, 
  * software distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and limitations 
  * under the License. 
  */
 package com.mebigfatguy.mongobrowser.dialogs;
 
 import java.awt.BorderLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Set;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.ExpandVetoException;
 import javax.swing.tree.TreePath;
 
 import com.mebigfatguy.mongobrowser.MongoContext;
 import com.mebigfatguy.mongobrowser.actions.DeleteAction;
 import com.mebigfatguy.mongobrowser.actions.NewCollectionAction;
 import com.mebigfatguy.mongobrowser.actions.NewKeyValueAction;
 import com.mebigfatguy.mongobrowser.actions.NewObjectAction;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 /**
  * the panel that holds the tree of mongo database objects
  */
 public class MongoDataPanel extends JPanel implements MongoPanel {
 
 	private static final long serialVersionUID = 1579613544693305078L;
 	private final MongoContext context;
 	private JTree tree;
 	private JMenuItem newCollectionItem;
 	private JMenuItem newObjectItem;
 	private JMenuItem newKeyValueItem;
 	private JMenuItem deleteItem;
 
 	/**
 	 * constructs the panel
 	 * 
 	 * @param ctxt
 	 *            the mediator object
 	 */
 	public MongoDataPanel(MongoContext ctxt) {
 		context = ctxt;
 		initComponents();
 		initListeners();
 	}
 
 	/**
 	 * sets up the panel when a database is connected to
 	 */
 	@Override
 	public void init() {
 		DB db = context.getDatabase();
 		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		MongoTreeNode root = (MongoTreeNode) model.getRoot();
 
 		if (db != null) {
 			Set<String> collectionNames = db.getCollectionNames();
 			for (String collectionName : collectionNames) {
 				DBCollection collection = db.getCollection(collectionName);
 				boolean readOnly = collectionName.startsWith("system.");
 				MongoTreeNode col = new MongoTreeNode(collection, readOnly);
 				root.add(col);
 				MongoTreeNode slug = new MongoTreeNode();
 				col.add(slug);
 			}
 		} else {
 			root.removeAllChildren();
 		}
 		model.nodeStructureChanged(root);
 		context.setSelectedNode(null);
 	}
 
 	/**
 	 * cleans up a panel when a database is disconnected from
 	 */
 	@Override
 	public void term() {
 		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		MongoTreeNode root = (MongoTreeNode) model.getRoot();
 		root.removeAllChildren();
 		model.nodeStructureChanged(root);
 		context.setSelectedNode(null);
 	}
 
 	/**
 	 * creates and lays out the components in this panel
 	 */
 	private void initComponents() {
 		setLayout(new BorderLayout(4, 4));
 
 		MongoTreeNode root = new MongoTreeNode(new MongoTreeNode.Root());
 		tree = new JTree(root);
 		tree.setRootVisible(false);
 		tree.setShowsRootHandles(true);
 		tree.setCellRenderer(new MongoTreeCellRenderer());
 		add(new JScrollPane(tree), BorderLayout.CENTER);
 		context.setTree(tree);
 
 		newCollectionItem = new JMenuItem(new NewCollectionAction(context));
 		newObjectItem = new JMenuItem(new NewObjectAction(context));
 		newKeyValueItem = new JMenuItem(new NewKeyValueAction(context));
 		deleteItem = new JMenuItem(new DeleteAction(context));
 	}
 
 	/**
 	 * installs the event listeners for the components on this panel
 	 */
 	private void initListeners() {
 		final JPopupMenu menu = new JPopupMenu();
 
 		tree.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				showPopup(e);
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				showPopup(e);
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				showPopup(e);
 			}
 
 			private void showPopup(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
 				if (e.isPopupTrigger()) {
 					menu.removeAll();
 					TreePath path = tree.getPathForLocation(x, y);
 					if (path == null) {
 						menu.add(newCollectionItem);
 						menu.show(tree, x, y);
 					} else {
 						MongoTreeNode node = (MongoTreeNode) path
 								.getLastPathComponent();
 						context.setSelectedNode(node);
 						if (node.getType() == MongoTreeNode.Type.Collection) {
 							if (!node.isReadOnly()) {
 								menu.add(newObjectItem);
 								menu.show(tree, x, y);
 							}
 						} else if (node.getType() == MongoTreeNode.Type.Object) {
 							if (!node.isReadOnly()) {
 								menu.add(newKeyValueItem);
 								menu.addSeparator();
 								menu.add(deleteItem);
 								menu.show(tree, x, y);
 							}
 						} else if (node.getType() == MongoTreeNode.Type.KeyValue) {
 							if (!node.isReadOnly()) {
 								MongoTreeNode.KV kv = (MongoTreeNode.KV) node
 										.getUserObject();
 								Object value = kv.getValue();
 								boolean needsSeparator = false;
 								if (value instanceof DBObject) {
 									menu.add(newKeyValueItem);
 									needsSeparator = true;
 								}
 
 								if (!kv.getKey().startsWith("_")) {
 									if (needsSeparator) {
 										menu.addSeparator();
 									}
 									menu.add(deleteItem);
 									menu.show(tree, x, y);
 								}
 							}
 						}
 					}
 				}
 			}
 		});
 
 		tree.addTreeWillExpandListener(new TreeWillExpandListener() {
 			@Override
 			public void treeWillExpand(TreeExpansionEvent event)
 					throws ExpandVetoException {
 				MongoTreeNode node = (MongoTreeNode) event.getPath()
 						.getLastPathComponent();
 				MongoTreeNode slug = (MongoTreeNode) node.getFirstChild();
 				if (slug.getType() == MongoTreeNode.Type.ExpansionSlug) {
 					node.removeAllChildren();
 					switch (node.getType()) {
 					case Collection: {
 						DBCollection collection = (DBCollection) node
 								.getUserObject();
 						DBCursor cursor = collection.find();
 						while (cursor.hasNext()) {
 							DBObject obj = cursor.next();
 							MongoTreeNode objNode = new MongoTreeNode(obj, node
 									.isReadOnly());
 							node.add(objNode);
 							slug = new MongoTreeNode();
 							objNode.add(slug);
 						}
 					}
 						break;
 
 					case Object: {
 						DBObject object = (DBObject) node.getUserObject();
 						for (String key : object.keySet()) {
 							Object value = object.get(key);
 							MongoTreeNode kv = new MongoTreeNode(
 									new MongoTreeNode.KV(key, value), node
 											.isReadOnly());
 							node.add(kv);
 							if (value instanceof DBObject) {
 								slug = new MongoTreeNode();
 								kv.add(slug);
 							}
 						}
 					}
 						break;
 
 					case KeyValue: {
 						MongoTreeNode.KV topKV = (MongoTreeNode.KV) node
 								.getUserObject();
 						DBObject object = (DBObject) topKV.getValue();
 						for (String key : object.keySet()) {
 							Object value = object.get(key);
 							MongoTreeNode kv = new MongoTreeNode(
 									new MongoTreeNode.KV(key, value), node
 											.isReadOnly());
 							node.add(kv);
 							if (value instanceof DBObject) {
 								slug = new MongoTreeNode();
 								kv.add(slug);
 							}
 						}
 					}
 						break;
 					}
 				}
 
 				DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 				model.nodeStructureChanged(node);
 			}
 
 			@Override
 			public void treeWillCollapse(TreeExpansionEvent event)
 					throws ExpandVetoException {
 				MongoTreeNode node = (MongoTreeNode) event.getPath()
 						.getLastPathComponent();
 				node.removeAllChildren();
 				MongoTreeNode slug = new MongoTreeNode();
 				node.add(slug);
 				DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 				model.nodeStructureChanged(node);
 			}
 
 		});
 
 		tree.addTreeSelectionListener(new TreeSelectionListener() {
 			@Override
 			public void valueChanged(TreeSelectionEvent tse) {
 				TreePath path = tse.getNewLeadSelectionPath();
 				if (path != null) {
 					MongoTreeNode node = (MongoTreeNode) path
 							.getLastPathComponent();
 					context.setSelectedNode(node);
 				} else {
 					context.setSelectedNode(null);
 				}
 			}
 		});
 	}
 }
