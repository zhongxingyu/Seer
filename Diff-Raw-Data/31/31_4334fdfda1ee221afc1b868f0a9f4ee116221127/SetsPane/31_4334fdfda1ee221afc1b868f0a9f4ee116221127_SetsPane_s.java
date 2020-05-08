 package edu.ucsf.rbvi.setsApp.internal;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreePath;
 
 import org.cytoscape.application.swing.CytoPanelComponent;
 import org.cytoscape.application.swing.CytoPanelName;
 import org.cytoscape.model.CyColumn;
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyIdentifiable;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.service.util.AbstractCyActivator;
 import org.cytoscape.session.events.SessionLoadedEvent;
 import org.cytoscape.session.events.SessionLoadedListener;
 import org.cytoscape.view.model.CyNetworkViewManager;
 import org.cytoscape.work.TaskIterator;
 import org.cytoscape.work.TaskManager;
 import org.osgi.framework.BundleContext;
 
 import edu.ucsf.rbvi.setsApp.internal.tasks.CopyCyIdTask;
 import edu.ucsf.rbvi.setsApp.internal.tasks.CreateSetTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.RenameSetTask;
 import edu.ucsf.rbvi.setsApp.internal.tasks.SetChangedEvent;
 import edu.ucsf.rbvi.setsApp.internal.tasks.SetChangedListener;
 import edu.ucsf.rbvi.setsApp.internal.tasks.SetsManager;
 
 public class SetsPane extends JPanel implements CytoPanelComponent, SetChangedListener, SessionLoadedListener {
 	private JButton createSet, newSetFromAttribute, union, intersection, difference;
 	private ButtonGroup select;
 	private JRadioButton selectNodes, selectEdges;
 	private JTree setsTree;
 	private DefaultTreeModel treeModel;
 	private DefaultMutableTreeNode sets;
 	private JScrollPane scrollPane;
 	private BundleContext bundleContext;
 	private SetsManager mySets;
 	private CyNetworkManager networkManager;
 	private CyNetworkViewManager networkViewManager;
 	private CreateSetTaskFactory createSetTaskFactory;
 	private TaskManager taskManager;
 	private HashMap<String, DefaultMutableTreeNode> setsNode;
 	private HashMap<String, HashMap<Long, DefaultMutableTreeNode>> cyIdNode;
 	public static final String tablePrefix = "setsApp:";
 	
 	public SetsPane(BundleContext bc) {
 		bundleContext = bc;
 		mySets = new SetsManager(this);
 		createSetTaskFactory = new CreateSetTaskFactory(mySets);
 		networkManager = (CyNetworkManager) getService(CyNetworkManager.class);
 		networkViewManager = (CyNetworkViewManager) getService(CyNetworkViewManager.class);
 		taskManager = (TaskManager) getService(TaskManager.class);
 
 		setPreferredSize(new Dimension(500,600));
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		
 		select = new ButtonGroup();
 		selectNodes = new JRadioButton("Nodes");
 		selectNodes.setSelected(true);
 		selectEdges = new JRadioButton("Edges");
 		select.add(selectNodes);
 		select.add(selectEdges);
 		createSet = new JButton("Create Set");
 		createSet.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				if (selectNodes.isSelected())
 					taskManager.execute(createSetTaskFactory.createTaskIterator(null, networkViewManager, CyIdType.NODE));
 				if (selectEdges.isSelected())
 					taskManager.execute(createSetTaskFactory.createTaskIterator(null, networkViewManager, CyIdType.EDGE));
 			}
 		});
 		newSetFromAttribute = new JButton("Create Set From Attributes");
 		newSetFromAttribute.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				CyNetwork network = null;
 				for (CyNetwork n: networkManager.getNetworkSet())
 					if (n.getRow(n).get(CyNetwork.SELECTED, Boolean.class)) network = n;
 				if (network != null) {
 					if (selectNodes.isSelected())
 						taskManager.execute(createSetTaskFactory.createTaskIterator(network, CyIdType.NODE));
 					if (selectEdges.isSelected())
 						taskManager.execute(createSetTaskFactory.createTaskIterator(network, CyIdType.EDGE));
 				}
 			}
 		});
 		union = new JButton("Union");
 		union.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				taskManager.execute(createSetTaskFactory.createTaskIterator(selectNodes.isSelected() ? CyIdType.NODE : CyIdType.EDGE, SetOperations.UNION));
 			}
 		});
 		intersection = new JButton("Intersection");
 		intersection.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				taskManager.execute(createSetTaskFactory.createTaskIterator(selectNodes.isSelected() ? CyIdType.NODE : CyIdType.EDGE, SetOperations.INTERSECT));
 			}
 		});
 		difference = new JButton("Set Difference");
 		difference.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				taskManager.execute(createSetTaskFactory.createTaskIterator(selectNodes.isSelected() ? CyIdType.NODE : CyIdType.EDGE, SetOperations.DIFFERENCE));
 			}
 		});
 		
 		sets = new DefaultMutableTreeNode("Sets");
 		setsTree = new JTree(sets);
 		setsTree.addMouseListener(new MouseAdapter() {
 			private void popupEvent(MouseEvent e) {
 				int x = e.getX();
 				int y = e.getY();
 				JTree tree = (JTree)e.getSource();
 				TreePath path = tree.getPathForLocation(x, y);
 				if (path == null)
 					return;	
 
 				tree.setSelectionPath(path);
 				path.getLastPathComponent();
 				final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
 
 				JPopupMenu popup = new JPopupMenu();
 				if (node.isLeaf()) {
 					final DefaultMutableTreeNode setNode = (DefaultMutableTreeNode) node.getParent();
 					final CyIdentifiable selectecCyId = ((NodeInfo) node.getUserObject()).cyId;
 					final String thisSetName = setNode.getUserObject().toString();
 					
 					JMenuItem copy = new JMenuItem("Copy to...");
 					JMenuItem move = new JMenuItem("Move to...");
 					JMenuItem delete = new JMenuItem("Delete");
 					copy.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							taskManager.execute(new TaskIterator(new CopyCyIdTask(mySets, selectecCyId)));
 						}
 					});
 					move.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
							taskManager.execute(new TaskIterator(new CopyCyIdTask(mySets, selectecCyId)));
							mySets.removeFromSet(thisSetName, selectecCyId);
 						}
 					});
 					delete.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							mySets.removeFromSet(thisSetName, selectecCyId);
 						}
 					});
 					popup.add(copy);
 					popup.add(move);
 					popup.add(delete);
 				}
 				else if (!node.isRoot()) {
 					JMenuItem add = new JMenuItem("Add...");
 					JMenuItem delete = new JMenuItem("Delete");
 					JMenuItem rename = new JMenuItem("Rename");
 					add.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							// TODO Auto-generated method stub
 							
 						}
 					});
 					delete.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							mySets.removeSet(node.getUserObject().toString());
 						}
 					});
 					rename.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							taskManager.execute(new TaskIterator(new RenameSetTask(mySets, (String) node.getUserObject())));
 						}
 					});
 					popup.add(delete);
 					popup.add(rename);
 				}
 				popup.show(tree, x, y);
 			}
 			public void mousePressed(MouseEvent e) {
 				if (e.isPopupTrigger()) popupEvent(e);
 			}
 			public void mouseReleased(MouseEvent e) {
 				if (e.isPopupTrigger()) popupEvent(e);
 			}
 		});
 		treeModel = (DefaultTreeModel) setsTree.getModel();
 		setsTree.setCellRenderer(new SetIconRenderer());
 		scrollPane = new JScrollPane(setsTree);
 		setsNode = new HashMap<String, DefaultMutableTreeNode>();
 		cyIdNode = new HashMap<String, HashMap<Long, DefaultMutableTreeNode>>();
 		
 		add(selectNodes);
 		add(selectEdges);
 		add(createSet);
 		add(newSetFromAttribute);
 		add(scrollPane);
 		add(union);
 		add(intersection);
 		add(difference);
 	}
 	
 	public SetsManager getSetsManager() {return mySets;}
 	
 	private Object getService(Class<?> serviceClass) {
 		return bundleContext.getService(bundleContext.getServiceReference(serviceClass.getName()));
     }
 	
 	private static final long serialVersionUID = -3152025163466058952L;
 
 	public Component getComponent() {
 		return this;
 	}
 
 	public CytoPanelName getCytoPanelName() {
 		return CytoPanelName.WEST;
 	}
 
 	public Icon getIcon() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getTitle() {
 		return "Sets";
 	}
 	
 	public synchronized void setCreated(SetChangedEvent event) {
 		DefaultMutableTreeNode thisSet = new DefaultMutableTreeNode(event.getSetName());
 		setsNode.put(event.getSetName(), thisSet);
 		HashMap<Long, DefaultMutableTreeNode> setNodesMap = new HashMap<Long, DefaultMutableTreeNode>();
 		CyNetwork cyNetwork = mySets.getCyNetwork(event.getSetName());
 		CyTable nodeTable = cyNetwork.getDefaultNodeTable();
 		CyTable edgeTable = cyNetwork.getDefaultEdgeTable();
 		Iterator<? extends CyIdentifiable> iterator = (Iterator<? extends CyIdentifiable>) mySets.getSet(event.getSetName()).getElements();
 		while (iterator.hasNext()) {
 			CyIdentifiable cyId = iterator.next();
 			String cyIdName = "???";
 			if (nodeTable.rowExists(cyId.getSUID()))
 				cyIdName = nodeTable.getRow(cyId.getSUID()).get("name", String.class);
 			if (edgeTable.rowExists(cyId.getSUID()))
 				cyIdName = edgeTable.getRow(cyId.getSUID()).get("name", String.class);
 			DefaultMutableTreeNode thisNode = new DefaultMutableTreeNode(new NodeInfo(cyIdName, cyId));
 			thisSet.add(thisNode);
 			setNodesMap.put(cyId.getSUID(), thisNode);
 		}
 		setsNode.put(event.getSetName(), thisSet);
 		cyIdNode.put(event.getSetName(), setNodesMap);
 		treeModel.insertNodeInto(thisSet, sets, sets.getChildCount());
 	//	sets.add(thisSet);
 		CyTable networkTable = cyNetwork.getDefaultNetworkTable();
 		if (networkTable.getColumn(tablePrefix + event.getSetName()) == null) {
 			networkTable.createListColumn(tablePrefix + event.getSetName(), Long.class, false);
 			ArrayList<Long> suidSet = new ArrayList<Long>();
 			iterator = (Iterator<? extends CyIdentifiable>) mySets.getSet(event.getSetName()).getElements();
 			while (iterator.hasNext())
 				suidSet.add(iterator.next().getSUID());
 			networkTable.getRow(cyNetwork.getSUID()).set(tablePrefix + event.getSetName(), suidSet);
 		}
 	}
 
 	public void setRemoved(SetChangedEvent event) {
 		CyNetworkManager manager = (CyNetworkManager) getService(CyNetworkManager.class);
 		for (CyNetwork cyNetwork: manager.getNetworkSet()) {
 			CyTable networkTable = cyNetwork.getDefaultNetworkTable();
 			if (networkTable != null && networkTable.getColumn(tablePrefix + event.getSetName()) != null)
 				networkTable.deleteColumn(tablePrefix + event.getSetName());
 		}
 		treeModel.removeNodeFromParent(setsNode.get(event.getSetName()));
 		setsNode.remove(event.getSetName());
 		cyIdNode.remove(event.getSetName());
 	}
 
 	public void handleEvent(SessionLoadedEvent event) {
 		mySets.reset();
 		while (sets.getChildCount() > 0) {
 			treeModel.removeNodeFromParent((MutableTreeNode) sets.getLastChild());
 		}
 		CyNetworkManager nm = (CyNetworkManager) getService(CyNetworkManager.class);
 		Iterator<CyNetwork> networks = nm.getNetworkSet().iterator();
 		while (networks.hasNext()) {
 			CyNetwork cyNetwork = networks.next();
 			CyTable cyTable = cyNetwork.getDefaultNetworkTable();
 			Iterator<CyColumn> cyColumns = cyTable.getColumns().iterator();
 			while (cyColumns.hasNext()) {
 				CyColumn c = cyColumns.next();
 				String colName = c.getName();
 				if (colName.length() >= 9 && colName.substring(0, 8).equals(tablePrefix)) {
 					ArrayList<CyNode> cyNodes = null;
 					ArrayList<CyEdge> cyEdges = null;
 					String loadedSetName = colName.substring(8);
 					Iterator<List> suidIterator = c.getValues(List.class).iterator();
 					Iterator<Long> suids = suidIterator.next().iterator();
 					while (suids.hasNext()) {
 						long suid = suids.next();
 						CyNode thisNode = cyNetwork.getNode(suid);
 						CyEdge thisEdge = cyNetwork.getEdge(suid);
 						if (thisNode != null) {
 							if (cyNodes != null) 
 								cyNodes.add(thisNode);
 							else {
 								cyNodes = new ArrayList<CyNode>();
 								cyNodes.add(thisNode);
 							}
 						}
 						if (thisEdge != null) {
 							if (cyEdges != null)
 								cyEdges.add(thisEdge);
 							else {
 								cyEdges = new ArrayList<CyEdge>();
 								cyEdges.add(thisEdge);
 							}
 						}
 					}
 					mySets.createSet(loadedSetName, cyNetwork, cyNodes, cyEdges);
 				//	taskManager.execute(createSetTaskFactory.createTaskIterator(loadedSetName, cyNetwork, cyNodes, cyEdges));
 				}
 			}
 		}
 	}
 	
 	private class SetIconRenderer extends DefaultTreeCellRenderer {
 
 		private static final long serialVersionUID = -4782376042373670468L;
 		private boolean iconsOk = false;
 		private Icon setsIcon = null, nodeSetIcon = null, edgeSetIcon = null, nodeIcon = null, edgeIcon = null;
 		
 		public SetIconRenderer() {
 			URL myUrl = SetsPane.class.getResource("images/node_set.png");
 			if (myUrl != null) nodeSetIcon = new ImageIcon(myUrl);
 			myUrl = SetsPane.class.getResource("images/edge_set.png");
 			if (myUrl != null) edgeSetIcon = new ImageIcon(myUrl);
 			myUrl = SetsPane.class.getResource("images/sets.png");
 			if (myUrl != null) setsIcon = new ImageIcon(myUrl);
 			myUrl = SetsPane.class.getResource("images/edge.png");
 			if (myUrl != null) edgeIcon = new ImageIcon(myUrl);
 			myUrl = SetsPane.class.getResource("images/node.png");
 			if (myUrl != null) nodeIcon = new ImageIcon(myUrl);
 			if (nodeSetIcon != null && edgeSetIcon != null && setsIcon != null && edgeIcon != null && nodeIcon != null)
 				iconsOk = true;
 		}
 		
 		public Component getTreeCellRendererComponent(
 				JTree tree,
 				Object value,
 				boolean sel,
 				boolean expanded,
 				boolean leaf,
 				int row,
 				boolean hasFocus) {
 			super.getTreeCellRendererComponent(
 					tree, value, sel,
 					expanded, leaf, row,
 					hasFocus);
 			if (iconsOk) {
 				CyIdType type = getCyIdType(value);
 				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
 				if (node.isRoot()) {setIcon(setsIcon);}
 				else if (leaf) {
 					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
 					if (parent != null) {
 						CyIdType parentType = getCyIdType(parent);
 						if (parentType == CyIdType.NODE) setIcon(nodeIcon);
 						else if (parentType == CyIdType.EDGE) setIcon(edgeIcon);
 					}
 				}
 				else {
 					if (type == CyIdType.NODE) setIcon(nodeSetIcon);
 					else if (type == CyIdType.EDGE) setIcon(edgeSetIcon);
 				}
 			}
 			return this;
 		}
 		
 		private CyIdType getCyIdType(Object o) {
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
 			String nodeName = node.getUserObject().toString();
 			if (mySets.isInSetsManager(nodeName))
 				return mySets.getType(nodeName);
 			else
 				return null;
 		}
 	}
 
 	public void setChanged(SetChangedEvent event) {
 		CyNetwork cyNetwork = mySets.getCyNetwork(event.getSetName());
 		Iterator<? extends CyIdentifiable> iterator = (Iterator<? extends CyIdentifiable>) mySets.getSet(event.getSetName()).getElements();
 		List<CyIdentifiable> added = (List<CyIdentifiable>) event.getCyIdsAdded(),
 				removed = (List<CyIdentifiable>) event.getCyIdsRemoved();
 		DefaultMutableTreeNode setNode = setsNode.get(event.getSetName());
 		CyTable nodeTable = cyNetwork.getDefaultNodeTable();
 		CyTable edgeTable = cyNetwork.getDefaultEdgeTable();
 		if (added != null)
 			for (CyIdentifiable node: added) {
 				String cyIdName = null;
 				if (nodeTable.rowExists(node.getSUID()))
 					cyIdName = nodeTable.getRow(node.getSUID()).get("name", String.class);
 				if (edgeTable.rowExists(node.getSUID()))
 					cyIdName = edgeTable.getRow(node.getSUID()).get("name", String.class);
 				DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(new NodeInfo(cyIdName, node));
 				cyIdNode.get(event.getSetName()).put(node.getSUID(), newTreeNode);
 				treeModel.insertNodeInto(newTreeNode, setNode, setNode.getChildCount());
 			}
 		if (removed != null)
 			for (CyIdentifiable node: removed) {
 				treeModel.removeNodeFromParent(cyIdNode.get(event.getSetName()).get(node.getSUID()));
 				cyIdNode.get(event.getSetName()).remove(node.getSUID());
 			}
 	//	sets.add(thisSet);
 		CyTable networkTable = cyNetwork.getDefaultNetworkTable();
 		networkTable.deleteColumn(tablePrefix + event.getSetName());
 		networkTable.createListColumn(tablePrefix + event.getSetName(), Long.class, false);
 		ArrayList<Long> suidSet = new ArrayList<Long>();
 		iterator = (Iterator<? extends CyIdentifiable>) mySets.getSet(event.getSetName()).getElements();
 		while (iterator.hasNext())
 			suidSet.add(iterator.next().getSUID());
 		networkTable.getRow(cyNetwork.getSUID()).set(tablePrefix + event.getSetName(), suidSet);
 	}
 	
 	private class NodeInfo {
 		public String label;
 		public CyIdentifiable cyId;
 		
 		public NodeInfo(String name, CyIdentifiable s) {
 			label = name;
 			cyId = s;
 		}
 		public String toString() {
 			return label;
 		}
 	}
 
 	public void setRenamed(SetChangedEvent event) {
 		setsNode.put(event.getSetName(), setsNode.get(event.getOldSetName()));
 		setsNode.remove(event.getOldSetName());
 		cyIdNode.put(event.getSetName(), cyIdNode.get(event.getOldSetName()));
 		cyIdNode.remove(event.getOldSetName());
 		setsNode.get(event.getSetName()).setUserObject(event.getSetName());
 		
 		CyNetwork cyNetwork = mySets.getCyNetwork(event.getSetName());
 		CyTable networkTable = cyNetwork.getDefaultNetworkTable();
 		networkTable.deleteColumn(tablePrefix + event.getOldSetName());
 		networkTable.createListColumn(tablePrefix + event.getSetName(), Long.class, false);
 		ArrayList<Long> suidSet = new ArrayList<Long>();
 		Iterator<? extends CyIdentifiable> iterator = (Iterator<? extends CyIdentifiable>) mySets.getSet(event.getSetName()).getElements();
 		while (iterator.hasNext())
 			suidSet.add(iterator.next().getSUID());
 		networkTable.getRow(cyNetwork.getSUID()).set(tablePrefix + event.getSetName(), suidSet);
 	}
 }
