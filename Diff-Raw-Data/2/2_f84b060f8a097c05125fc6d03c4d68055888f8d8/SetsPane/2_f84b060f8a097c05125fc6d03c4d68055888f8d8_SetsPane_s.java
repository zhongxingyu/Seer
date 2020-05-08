 package edu.ucsf.rbvi.setsApp.internal;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 import javax.swing.plaf.basic.BasicComboBoxRenderer;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreePath;
 
 import org.cytoscape.application.CyApplicationManager;
 import org.cytoscape.application.swing.CytoPanelComponent;
 import org.cytoscape.application.swing.CytoPanelName;
 import org.cytoscape.model.CyColumn;
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyIdentifiable;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.model.CyTableUtil;
 import org.cytoscape.session.events.SessionLoadedEvent;
 import org.cytoscape.session.events.SessionLoadedListener;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.view.model.CyNetworkViewManager;
 import org.cytoscape.work.TaskIterator;
 import org.cytoscape.work.TaskManager;
 import org.osgi.framework.BundleContext;
 
 import edu.ucsf.rbvi.setsApp.internal.events.SetChangedEvent;
 import edu.ucsf.rbvi.setsApp.internal.events.SetChangedListener;
 import edu.ucsf.rbvi.setsApp.internal.tasks.CopyCyIdTask;
 import edu.ucsf.rbvi.setsApp.internal.tasks.CreateSetTaskFactory;
 import edu.ucsf.rbvi.setsApp.internal.tasks.MoveCyIdTask;
 import edu.ucsf.rbvi.setsApp.internal.tasks.MoveSetTask;
 import edu.ucsf.rbvi.setsApp.internal.tasks.RemoveSetTask;
 import edu.ucsf.rbvi.setsApp.internal.tasks.RenameSetTask;
 import edu.ucsf.rbvi.setsApp.internal.tasks.SetsManager;
 import edu.ucsf.rbvi.setsApp.internal.tasks.SetOperationsTask;
 import edu.ucsf.rbvi.setsApp.internal.tasks.WriteSetToFileTask;
 import edu.ucsf.rbvi.setsApp.internal.tasks.WriteSetToFileTask2;
 import edu.ucsf.rbvi.setsApp.internal.tasks.CreateSetFromFileTask2;
 
 public class SetsPane extends JPanel implements CytoPanelComponent, SetChangedListener, SessionLoadedListener {
 	private JButton importSet, createSet, newSetFromAttribute, union, intersection, difference, exportSet;
 	private JPanel modePanel, createSetPanel, filePanel, setOpPanel;
 	private ButtonGroup select;
 	private JRadioButton selectNodes, selectEdges;
 	private JTree setsTree, nodesTree, edgesTree;
 	private DefaultTreeModel treeModel, nodesTreeModel, edgesTreeModel;
 	private DefaultMutableTreeNode sets, nodesSet, edgesSet;
 	private JScrollPane scrollPane, nodesPane, edgesPane;
 	private JPopupMenu selectSetCreation;
 	private JMenuItem setsFNodes, setsFEdges, setsFNodeA, setsFEdgeA;
 	private BundleContext bundleContext;
 	private SetsManager mySets;
 	private CyNetworkManager networkManager;
 	private CyNetworkViewManager networkViewManager;
 	private CreateSetTaskFactory createSetTaskFactory;
 	private TaskManager taskManager;
 	private HashMap<String, DefaultMutableTreeNode> setsNode;
 	private HashMap<String, HashMap<Long, DefaultMutableTreeNode>> cyIdNode;
 	private JFileChooser chooseImport;
 	private String set1, set2;
 	public static final String tablePrefix = "setsApp:";
 	
 	private class PartialDisableComboBox extends JComboBox {
 		 private static final long serialVersionUID = -1690671707274328126L;
 		 
 		 private ArrayList<Boolean> itemsState = new ArrayList<Boolean>();
 		 
 		 public PartialDisableComboBox() {
 		  super();
 		  this.setRenderer(new BasicComboBoxRenderer() {
 		   private static final long serialVersionUID = -2774241371293899669L;
 		   @Override
 		   public Component getListCellRendererComponent(JList list, Object value, 
 		     int index, boolean isSelected, boolean cellHasFocus) {
 		    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 		    boolean disabled = index >= 0 && index < itemsState.size() && !itemsState.get(index);
 		    c.setEnabled(!disabled);
 		    c.setFocusable(!disabled);
 		    return c;
 		   }
 		  });
 		 }
 		 
 		 @Override
 		 public void addItem(Object item) {
 		  this.addItem(item, true);
 		 }
 		 
 		 public void addItem(Object item, boolean enabled) {
 		  super.addItem(item);
 		  itemsState.add(enabled);
 		 }
 		 
 		 @Override
 		 public void insertItemAt(Object item, int index) {
 		  this.insertItemAt(item, index, true);
 		 }
 
 		 public void insertItemAt(Object item, int index, boolean enabled) {
 		  super.insertItemAt(item, index);
 		  itemsState.add(index, enabled);
 		 }
 		 
 		 @Override
 		 public void removeAllItems() {
 		  super.removeAllItems();
 		  itemsState.clear();
 		 }
 		 
 		 @Override
 		 public void removeItemAt(int index) {
 		  if (index < 0 || index >= itemsState.size()) throw new IllegalArgumentException("Item Index out of Bounds!");
 		  super.removeItemAt(index);
 		  itemsState.remove(index);
 		 }
 		 
 		 @Override
 		 public void removeItem(Object item) {
 		  for (int q = 0; q < this.getItemCount(); q++) {
 		   if (this.getItemAt(q) == item) itemsState.remove(q);
 		  }
 		  super.removeItem(item);
 		 }
 		 
 		 @Override
 		 public void setSelectedIndex(int index) {
 		  if (index < 0 || index >= itemsState.size()) throw new IllegalArgumentException("Item Index out of Bounds!");
 		  if (itemsState.get(index)) super.setSelectedIndex(index);
 		 }
 		 
 		 public void setItemEnabled(int index, boolean enabled) {
 		  if (index < 0 || index >= itemsState.size()) throw new IllegalArgumentException("Item Index out of Bounds!");
 		  itemsState.set(index, enabled);
 		 }
 		 
 		 public boolean isItemEnabled(int index) {
 		  if (index < 0 || index >= itemsState.size()) throw new IllegalArgumentException("Item Index out of Bounds!");
 		  return itemsState.get(index);
 		 }
 		}
 	
 	public SetsPane(BundleContext bc, SetsManager thisSet) {
 		bundleContext = bc;
 		mySets = thisSet;
 		mySets.addSetChangedListener(this);
 		createSetTaskFactory = new CreateSetTaskFactory(mySets);
 		networkManager = (CyNetworkManager) getService(CyNetworkManager.class);
 		networkViewManager = (CyNetworkViewManager) getService(CyNetworkViewManager.class);
 		taskManager = (TaskManager) getService(TaskManager.class);
 		chooseImport = new JFileChooser();
 		
 		setPreferredSize(new Dimension(500,600));
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		
 		select = new ButtonGroup();
 		selectNodes = new JRadioButton("Nodes");
 		selectNodes.setSelected(true);
 		selectEdges = new JRadioButton("Edges");
 		select.add(selectNodes);
 		select.add(selectEdges);
 		importSet = new JButton("Import Set From File");
 		importSet.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
 		importSet.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				if (JFileChooser.APPROVE_OPTION == chooseImport.showOpenDialog(SetsPane.this)){
 					File f = chooseImport.getSelectedFile();
 					taskManager.execute(new TaskIterator(new CreateSetFromFileTask2(mySets,networkManager, f)));
 				}
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
 		union.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
 		union.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				taskManager.execute(new TaskIterator(new SetOperationsTask(mySets,set1,set2,SetOperations.UNION)));
 			//	mySets.union(set1 + " union " + set2, set1, set2);
 			//	taskManager.execute(createSetTaskFactory.createTaskIterator(selectNodes.isSelected() ? CyIdType.NODE : CyIdType.EDGE, SetOperations.UNION));
 			}
 		});
 		union.setEnabled(false);
 		intersection = new JButton("Intersection");
 		intersection.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
 		intersection.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				taskManager.execute(new TaskIterator(new SetOperationsTask(mySets,set1,set2,SetOperations.INTERSECT)));
 			//	mySets.intersection(set1 + " intersection " + set2, set1, set2);
 			//	taskManager.execute(createSetTaskFactory.createTaskIterator(selectNodes.isSelected() ? CyIdType.NODE : CyIdType.EDGE, SetOperations.INTERSECT));
 			}
 		});
 		intersection.setEnabled(false);
 		difference = new JButton("Difference");
 		difference.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
 		difference.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				taskManager.execute(new TaskIterator(new SetOperationsTask(mySets,set2,set1,SetOperations.DIFFERENCE)));
 			//	mySets.difference(set1 + " difference " + set2, set2, set1);
 			//	taskManager.execute(createSetTaskFactory.createTaskIterator(selectNodes.isSelected() ? CyIdType.NODE : CyIdType.EDGE, SetOperations.DIFFERENCE));
 			}
 		});
 		difference.setEnabled(false);
 		exportSet = new JButton("Export Set to File");
 		exportSet.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
 		exportSet.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				if (JFileChooser.APPROVE_OPTION == chooseImport.showSaveDialog(SetsPane.this)) {
 					File f = chooseImport.getSelectedFile();
 					taskManager.execute(new TaskIterator(new WriteSetToFileTask2(mySets,set1,f)));
 				}
 			}
 		});
 		exportSet.setEnabled(false);
 		
 		sets = new DefaultMutableTreeNode("Sets"/* new NodeInfo("Sets","Sets") */);
 		setsTree = new JTree(sets);
 		setsTree.setRootVisible(false);
 		setsTree.setShowsRootHandles(true);
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
 				if (! node.isRoot() && ((NodeInfo) node.getUserObject()).cyId != null) {
 					final DefaultMutableTreeNode setNode = (DefaultMutableTreeNode) node.getParent();
 					final CyIdentifiable selectecCyId = ((NodeInfo) node.getUserObject()).cyId;
 					final String thisSetName = ((NodeInfo) setNode.getUserObject()).setName;
 					
 					JMenuItem select = new JMenuItem("Select");
 					JMenuItem unselect = new JMenuItem("Unselect");
 					JMenuItem copy = new JMenuItem("Copy to...");
 					JMenuItem move = new JMenuItem("Move to...");
 					JMenuItem delete = new JMenuItem("Remove from Set");
 					select.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							displaySelectedCyIds(thisSetName, selectecCyId.getSUID());
 						}
 					});
 					unselect.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							unDisplaySelectedCyIds(thisSetName, selectecCyId.getSUID());
 						}
 					});
 					copy.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							taskManager.execute(new TaskIterator(new CopyCyIdTask(mySets, selectecCyId)));
 						}
 					});
 					move.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							taskManager.execute(new TaskIterator(new MoveCyIdTask(mySets, thisSetName, selectecCyId)));
 						}
 					});
 					delete.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							try {
 								mySets.removeFromSet(thisSetName, selectecCyId);
 							} catch (Exception e1) {
 								// TODO Auto-generated catch block
 								e1.printStackTrace();
 							}
 						}
 					});
 					popup.add(select);
 					popup.add(unselect);
 					popup.add(copy);
 					popup.add(move);
 					popup.add(delete);
 				}
 				else {
 					JMenuItem select = new JMenuItem("Select");
 					JMenuItem unselect = new JMenuItem("Unselect");
 					JMenuItem delete = new JMenuItem("Remove Set");
 					JMenuItem rename = new JMenuItem("Rename");
 					JMenuItem move = new JMenuItem("Move set to different network");
 
 					select.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							String setName = ((NodeInfo) node.getUserObject()).setName;
 							displaySelectedCyIds(setName, null);
 						}
 					});
 					unselect.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							String setName = ((NodeInfo) node.getUserObject()).setName;
 							unDisplaySelectedCyIds(setName, null);
 						}
 					});
 					rename.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							taskManager.execute(new TaskIterator(new RenameSetTask(mySets, ((NodeInfo) node.getUserObject()).setName)));
 						}
 					});
 					delete.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							taskManager.execute(new TaskIterator(new RemoveSetTask(mySets, ((NodeInfo) node.getUserObject()).setName)));
 						//	mySets.removeSet(((NodeInfo) node.getUserObject()).setName);
 						}
 					});
 					move.addActionListener(new ActionListener() {
 						
 						public void actionPerformed(ActionEvent e) {
 							CyNetworkManager networkManager = (CyNetworkManager) getService(CyNetworkManager.class);
 							taskManager.execute(new TaskIterator(new MoveSetTask(mySets, networkManager.getNetworkSet(), ((NodeInfo) node.getUserObject()).setName)));
 						}
 					});
 					popup.add(select);
 					popup.add(unselect);
 					popup.add(delete);
 					popup.add(rename);
 					popup.add(move);
 				}
 				popup.show(tree, x, y);
 			}
 			public void enableOperationsButton(boolean b) {
 				intersection.setEnabled(b);
 				union.setEnabled(b);
 				difference.setEnabled(b);
 			}
 			public void mousePressed(MouseEvent e) {
 				if (e.isPopupTrigger()) popupEvent(e);
 				else {
 					if (getSetsSelectedFromTree(e))
 						enableOperationsButton(true);
 					else enableOperationsButton(false);
 					if (singleSetSelected(e))
 						exportSet.setEnabled(true);
 					else exportSet.setEnabled(false);
 				}
 			}
 			public void mouseReleased(MouseEvent e) {
 				if (e.isPopupTrigger()) popupEvent(e);
 				else {
 					if (getSetsSelectedFromTree(e))
 						enableOperationsButton(true);
 					else enableOperationsButton(false);
 					if (singleSetSelected(e))
 						exportSet.setEnabled(true);
 					else exportSet.setEnabled(false);
 				}
 			}
 			private boolean singleSetSelected(MouseEvent e) {
 				JTree tree = (JTree) e.getSource();
 				TreePath path[] = tree.getSelectionPaths();
 				if (path != null && path.length == 1) {
 					DefaultMutableTreeNode node1 = (DefaultMutableTreeNode) path[0].getLastPathComponent();
 					if (! node1.isRoot() && ((NodeInfo) node1.getUserObject()).cyId == null) return true;
 					return false;
 				}
 				else return false;
 			}
 			private boolean getSetsSelectedFromTree(MouseEvent e) {
 				JTree tree = (JTree) e.getSource();
 				TreePath path[] = tree.getSelectionPaths();
 				if (path != null && path.length == 1) {
 					DefaultMutableTreeNode node1 = (DefaultMutableTreeNode) path[0].getLastPathComponent();
 					if (! node1.isRoot() && ((NodeInfo) node1.getUserObject()).cyId == null)
 						set1 = ((NodeInfo) node1.getUserObject()).setName;
 					return false;
 				}
 				else if (path != null && path.length == 2) {
 					DefaultMutableTreeNode node1 = (DefaultMutableTreeNode) path[0].getLastPathComponent();
 					DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) path[1].getLastPathComponent();
 					if (! node1.isRoot() && ! node2.isRoot() && ((NodeInfo) node1.getUserObject()).cyId == null &&  ((NodeInfo) node2.getUserObject()).cyId == null) {
 						if (set1.equals(((NodeInfo) node1.getUserObject()).setName)) {
 							set1 = ((NodeInfo) node1.getUserObject()).setName;
 							set2 = ((NodeInfo) node2.getUserObject()).setName;
 						}
 						else {
 							set1 = ((NodeInfo) node2.getUserObject()).setName;
 							set2 = ((NodeInfo) node1.getUserObject()).setName;
 						}
 						if (mySets.getType(set1) == mySets.getType(set2))
 							return true;
 						else return false;
 					}
 					else return false;
 				}
 				else return false;
 			}
 			private void displaySelectedCyIds(String setName, Long cyId) {
 				CyNetwork curNetwork = mySets.getCyNetwork(setName);
 				CyTable curTable = null;
 				if (mySets.getType(setName) == CyIdType.NODE)
 					curTable = curNetwork.getDefaultNodeTable();
 				if (mySets.getType(setName) == CyIdType.EDGE)
 					curTable = curNetwork.getDefaultEdgeTable();
 				if (curTable != null)
 					for (Long suid: curTable.getPrimaryKey().getValues(Long.class))
 						curTable.getRow(suid).set(CyNetwork.SELECTED, 
 								cyId == null ? mySets.isInSet(setName, suid): suid == cyId);
 				CyNetworkViewManager nvm = (CyNetworkViewManager) getService(CyNetworkViewManager.class);
 				for (CyNetworkView networkView: nvm.getNetworkViewSet())
 					if (networkView.getModel() == curNetwork)
 						networkView.updateView();
 			}
 			private void unDisplaySelectedCyIds(String setName, Long cyId) {
 				CyNetwork curNetwork = mySets.getCyNetwork(setName);
 				CyTable curTable = null;
 				if (mySets.getType(setName) == CyIdType.NODE)
 					curTable = curNetwork.getDefaultNodeTable();
 				if (mySets.getType(setName) == CyIdType.EDGE)
 					curTable = curNetwork.getDefaultEdgeTable();
 				if (curTable != null) {
 					if (cyId != null)
 						curTable.getRow(cyId).set(CyNetwork.SELECTED, false);
 					else
 						for (CyIdentifiable suid: mySets.getSet(setName).getElements())
 								curTable.getRow(suid.getSUID()).set(CyNetwork.SELECTED, false);
 				}
 				CyNetworkViewManager nvm = (CyNetworkViewManager) getService(CyNetworkViewManager.class);
 				for (CyNetworkView networkView: nvm.getNetworkViewSet())
 					if (networkView.getModel() == curNetwork)
 						networkView.updateView();
 			}
 		});
 		treeModel = (DefaultTreeModel) setsTree.getModel();
 		setsTree.setCellRenderer(new SetIconRenderer());
 		scrollPane = new JScrollPane(setsTree);
 		setsNode = new HashMap<String, DefaultMutableTreeNode>();
 		cyIdNode = new HashMap<String, HashMap<Long, DefaultMutableTreeNode>>();
 		
 	//	nodesSet = new DefaultMutableTreeNode("Nodes");
 	//	edgesSet = new DefaultMutableTreeNode("Edges");
 	//	nodesTree = new JTree(nodesSet);
 	//	nodesTree.setCellRenderer(new SetIconRenderer());
 	//	edgesTree = new JTree(edgesSet);
 	//	edgesTree.setCellRenderer(new SetIconRenderer());
 	//	nodesTreeModel = (DefaultTreeModel) nodesTree.getModel();
 	//	edgesTreeModel = (DefaultTreeModel) edgesTree.getModel();
 	//	nodesPane = new JScrollPane(nodesTree);
 	//	edgesPane = new JScrollPane(edgesTree);
 
 		final int BS = 8;
 		modePanel = new JPanel(new BorderLayout(BS, BS));
 		//modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.X_AXIS));
 		modePanel.setBorder(BorderFactory.createEmptyBorder(BS, BS, BS, BS));
 		
 		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, BS, 0));
 		topPanel.setBorder(BorderFactory.createTitledBorder("New Sets"));
 		final String noneSelected = "",
 				selectNodes = "selected nodes",
 				selectEdges = "selected edges",
 				attrNodes = "node attributes",
 				attrEdges = "edge attributes";
 		final String [] selectOptions = {noneSelected, selectNodes, selectEdges, attrNodes, attrEdges};
 		final PartialDisableComboBox createSetsFromSelected = new PartialDisableComboBox();
 		createSetsFromSelected.addItem(selectOptions[0], false);
 		createSetsFromSelected.addItem(selectOptions[1], false);
 		createSetsFromSelected.addItem(selectOptions[2], false);
 		createSetsFromSelected.addItem(selectOptions[3], false);
 		createSetsFromSelected.addItem(selectOptions[4], false);
 		createSetsFromSelected.setSelectedIndex(0);
 		createSetsFromSelected.addPopupMenuListener(new PopupMenuListener() {
 			
 			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
 				CyApplicationManager appManager = (CyApplicationManager) getService(CyApplicationManager.class);
 				CyNetwork curNetwork = appManager.getCurrentNetwork();
 				if (curNetwork != null) {
 					List<CyNode> nodes = CyTableUtil.getNodesInState(curNetwork, CyNetwork.SELECTED, true);
 					List<CyEdge> edges = CyTableUtil.getEdgesInState(curNetwork, CyNetwork.SELECTED, true);
 					if (!nodes.isEmpty())
 						createSetsFromSelected.setItemEnabled(1, true);
 					else
 						createSetsFromSelected.setItemEnabled(1, false);
 					if (!edges.isEmpty())
 						createSetsFromSelected.setItemEnabled(2, true);
 					else
 						createSetsFromSelected.setItemEnabled(2, false);
 					CyTable nodesTable = curNetwork.getDefaultNodeTable(),
 							edgesTable = curNetwork.getDefaultEdgeTable();
 					if (!nodesTable.getColumns().isEmpty())
 						createSetsFromSelected.setItemEnabled(3, true);
 					if (!edgesTable.getColumns().isEmpty())
 						createSetsFromSelected.setItemEnabled(4, true);
 				}
 				else {
 					for (int i = 1; i < selectOptions.length; i++)
 						createSetsFromSelected.setItemEnabled(i, false);
 				}
 			}
 			
 			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			public void popupMenuCanceled(PopupMenuEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		createSetsFromSelected.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				JComboBox selectedStuff = (JComboBox) e.getSource();
 				String selectedType = (String) selectedStuff.getSelectedItem();
 				selectedStuff.setPopupVisible(false);
 				CyApplicationManager appManager = (CyApplicationManager) getService(CyApplicationManager.class);
 				if (selectedType.equals(selectNodes))
 					taskManager.execute(createSetTaskFactory.createTaskIterator(null, networkViewManager, CyIdType.NODE));
 				if (selectedType.equals(selectEdges))
 					taskManager.execute(createSetTaskFactory.createTaskIterator(null, networkViewManager, CyIdType.EDGE));
 				if (selectedType.equals(attrNodes))
 					taskManager.execute(createSetTaskFactory.createTaskIterator(appManager.getCurrentNetwork(), CyIdType.NODE));
 				if (selectedType.equals(attrEdges))
 					taskManager.execute(createSetTaskFactory.createTaskIterator(appManager.getCurrentNetwork(), CyIdType.EDGE));
 			}
 		});
 		
 		topPanel.add(new Label("Create set from:"));
 		topPanel.add(createSetsFromSelected);
 	//	topPanel.add(createSet);
 		
 		JPanel btmPanel = new JPanel(new BorderLayout(BS, BS));
 
 		JPanel buttons1 = new JPanel(new FlowLayout(FlowLayout.CENTER, BS, 0));
 		buttons1.setBorder(BorderFactory.createTitledBorder("Set Operations"));
 		adjustWidth(new JButton[] {union, intersection, difference});
 		buttons1.add(union);
 		buttons1.add(intersection);
 		buttons1.add(difference);
 
 		JPanel buttons2 = new JPanel(new FlowLayout(FlowLayout.CENTER, BS, 0));
 		buttons2.setBorder(BorderFactory.createTitledBorder("Import/Export Sets to File"));
 		adjustWidth(new JButton[] {importSet, exportSet});
 		buttons2.add(importSet);
 		buttons2.add(exportSet);
 
 		btmPanel.add(buttons1, BorderLayout.NORTH);
 		btmPanel.add(buttons2, BorderLayout.SOUTH);
 
 		modePanel.add(topPanel, BorderLayout.NORTH);
 		modePanel.add(scrollPane, BorderLayout.CENTER);
 		modePanel.add(btmPanel, BorderLayout.SOUTH);
 		add(modePanel);
 
 	}
 
 	private void adjustWidth(JComponent[] components) {
 		Dimension dim = components[0].getPreferredSize();
 		int width = dim.width;
 		for (int i = 1; i < components.length; i++) {
 			dim = components[i].getPreferredSize();
 			if (dim.width > width) {
 				width = dim.width;
 			}
 		}
 		for (final JComponent cbx : components) {
 			dim = cbx.getPreferredSize();
 			dim.width = width;
 			cbx.setPreferredSize(dim);
 		}
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
 	
 	private void exportToAttribute(String name) {
 		CyTable table = null;
 		CyNetwork network = mySets.getCyNetwork(name);
 		String colName = tablePrefix + name;
 		if (mySets.getType(name) == CyIdType.NODE)
 			table = network.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
 		if (mySets.getType(name) == CyIdType.EDGE)
 			table = network.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
 		if (table != null && table.getColumn(colName) == null) {
 			table.createColumn(colName, Boolean.class, false);
 			for (CyIdentifiable cyId: mySets.getSet(name).getElements())
 				table.getRow(cyId.getSUID()).set(colName, true);
 		}
 	}
 	
 /*	private void importFromAttribute(String loadedSetName) {
 		CyNetwork cyNetwork = mySets.getCyNetwork(loadedSetName);
 		List<CyNode> cyNodes = null;
 		List<CyEdge> cyEdges = null;
 		cyNodes = CyTableUtil.getNodesInState(cyNetwork, loadedSetName, true);
 		cyEdges = CyTableUtil.getEdgesInState(cyNetwork, loadedSetName, true);
 		if (cyNodes != null && cyNodes.size() == 0) cyNodes = null;
 		if (cyEdges != null && cyEdges.size() == 0) cyEdges = null;
 		mySets.createSet(loadedSetName, cyNetwork, cyNodes, cyEdges);
 	} */
 	
 	public synchronized void setCreated(SetChangedEvent event) {
 		DefaultMutableTreeNode thisSet = new DefaultMutableTreeNode(event.getSetName());
 		HashMap<Long, DefaultMutableTreeNode> setNodesMap = new HashMap<Long, DefaultMutableTreeNode>();
 		CyNetwork cyNetwork = mySets.getCyNetwork(event.getSetName());
 		CyTable nodeTable = cyNetwork.getDefaultNodeTable();
 		CyTable edgeTable = cyNetwork.getDefaultEdgeTable();
 		Collection<? extends CyIdentifiable> cyIds = mySets.getSet(event.getSetName()).getElements();
 		for (CyIdentifiable cyId: cyIds) {
 			String cyIdName = "???";
 			if (nodeTable.rowExists(cyId.getSUID()))
 				cyIdName = nodeTable.getRow(cyId.getSUID()).get(CyNetwork.NAME, String.class);
 			if (edgeTable.rowExists(cyId.getSUID()))
 				cyIdName = edgeTable.getRow(cyId.getSUID()).get(CyNetwork.NAME, String.class);
 			DefaultMutableTreeNode thisNode = new DefaultMutableTreeNode(new NodeInfo(cyIdName, cyId));
 			thisSet.add(thisNode);
 			setNodesMap.put(cyId.getSUID(), thisNode);
 		}
 		setsNode.put(event.getSetName(), thisSet);
 		cyIdNode.put(event.getSetName(), setNodesMap);
 		thisSet.setUserObject(new NodeInfo(event.getSetName() + " (" + thisSet.getChildCount() + ")", event.getSetName()));
 		treeModel.insertNodeInto(thisSet, sets, sets.getChildCount());
 		setsTree.expandPath(new TreePath(sets.getPath()));
 		exportToAttribute(event.getSetName());
 	}
 
 	public void setRemoved(SetChangedEvent event) {
 		String setTableName = tablePrefix + event.getSetName();
 		CyNetwork cyNetwork = event.getCyNetwork();
 		CyTable nodeTable = cyNetwork.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
 		CyTable edgeTable = cyNetwork.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
 		if (nodeTable.getColumn(setTableName) != null)
 			nodeTable.deleteColumn(setTableName);
 		if (edgeTable.getColumn(setTableName) != null)
 			edgeTable.deleteColumn(setTableName);
 		treeModel.removeNodeFromParent(setsNode.get(event.getSetName()));
 		setsNode.remove(event.getSetName());
 		cyIdNode.remove(event.getSetName());
 	}
 
 	public void handleEvent(SessionLoadedEvent event) {
 		mySets.reset();
 		exportSet.setEnabled(false);
 		while (sets.getChildCount() > 0) {
 			treeModel.removeNodeFromParent((MutableTreeNode) sets.getLastChild());
 		}
 		CyNetworkManager nm = (CyNetworkManager) getService(CyNetworkManager.class);
 		java.util.Set<CyNetwork> networks = nm.getNetworkSet();
 		CyTable cyTable;
 		Collection<CyColumn> cyColumns;
 		for (CyNetwork cyNetwork: networks) {
 			cyTable = cyNetwork.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
 			cyColumns = cyTable.getColumns();
 			for (CyColumn c: cyColumns) {
 				String colName = c.getName();
 				if (colName.length() >= 9 && colName.substring(0, 8).equals(tablePrefix)) {
 					String loadedSetName = colName.substring(8);
 					List<CyNode> cyNodes = new ArrayList<CyNode>();
 					for (Long suid: cyTable.getPrimaryKey().getValues(Long.class))
 						if (cyTable.getRow(suid).get(colName, Boolean.class))
 							cyNodes.add(cyNetwork.getNode(suid));
 					if (cyNodes != null && cyNodes.size() == 0) cyNodes = null;
 					try {
 						mySets.createSet(loadedSetName, cyNetwork, cyNodes, null);
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			cyTable = cyNetwork.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
 			cyColumns = cyTable.getColumns();
 			for (CyColumn c: cyColumns) {
 				String colName = c.getName();
 				if (colName.length() >= 9 && colName.substring(0, 8).equals(tablePrefix)) {
 					String loadedSetName = colName.substring(8);
 					List<CyEdge> cyEdges = new ArrayList<CyEdge>();
 					for (Long suid: cyTable.getPrimaryKey().getValues(Long.class))
 						if (cyTable.getRow(suid).get(colName, Boolean.class))
 							cyEdges.add(cyNetwork.getEdge(suid));
 					if (cyEdges != null && cyEdges.size() == 0) cyEdges = null;
 					try {
 						mySets.createSet(loadedSetName, cyNetwork, null, cyEdges);
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 	
 	private class SetIconRenderer extends DefaultTreeCellRenderer {
 
 		private static final long serialVersionUID = -4782376042373670468L;
 		private boolean iconsOk = false;
 		private Icon setsIcon = null, nodeSetIcon = null, edgeSetIcon = null, nodeIcon = null, edgeIcon = null;
 		
 		public SetIconRenderer() {
 			URL myUrl = SetsPane.class.getResource("/images/node_set.png");
 			if (myUrl != null) nodeSetIcon = new ImageIcon(myUrl);
 			myUrl = SetsPane.class.getResource("/images/edge_set.png");
 			if (myUrl != null) edgeSetIcon = new ImageIcon(myUrl);
 			myUrl = SetsPane.class.getResource("/images/sets.png");
 			if (myUrl != null) setsIcon = new ImageIcon(myUrl);
 			myUrl = SetsPane.class.getResource("/images/edge.png");
 			if (myUrl != null) edgeIcon = new ImageIcon(myUrl);
 			myUrl = SetsPane.class.getResource("/images/node.png");
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
 			CyIdType type = getCyIdType(value);
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
 			if (iconsOk) {
 				if (node.isRoot()) {setIcon(setsIcon);}
 				else if (((NodeInfo) node.getUserObject()).cyId != null) {
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
 			setPreferredSize(new Dimension(600, getPreferredSize().height));
 			validate();
 			repaint();
 			return this;
 		}
 		
 		private CyIdType getCyIdType(Object o) {
 			DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
 			String nodeName = null;
 			if (node.isRoot());
 			else if (((NodeInfo) node.getUserObject()).cyId != null)
 				nodeName = ((NodeInfo) node.getUserObject()).label;
 			else
 				nodeName = ((NodeInfo) node.getUserObject()).setName;
 			if (nodeName != null && mySets.isInSetsManager(nodeName))
 				return mySets.getType(nodeName);
 			else
 				return null;
 		}
 	}
 
 	public void setChanged(SetChangedEvent event) {
 		CyNetwork cyNetwork = mySets.getCyNetwork(event.getSetName());
 		List<CyIdentifiable> added = (List<CyIdentifiable>) event.getCyIdsAdded(),
 				removed = (List<CyIdentifiable>) event.getCyIdsRemoved();
 		DefaultMutableTreeNode setNode = setsNode.get(event.getSetName());
 		CyTable nodeTable = cyNetwork.getDefaultNodeTable(),
 				edgeTable = cyNetwork.getDefaultEdgeTable(),
 				nodeHiddenTable = cyNetwork.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS),
 				edgeHiddenTable = cyNetwork.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
 		String setTableName = tablePrefix + event.getSetName();
 		if (added != null)
 			for (CyIdentifiable node: added) {
 				String cyIdName = null;
 				if (cyNetwork.getNode(node.getSUID()) != null) {
 					cyIdName = nodeTable.getRow(node.getSUID()).get(CyNetwork.NAME, String.class);
 					nodeHiddenTable.getRow(node.getSUID()).set(setTableName, true);
 				}
 				if (cyNetwork.getEdge(node.getSUID()) != null) {
 					cyIdName = edgeTable.getRow(node.getSUID()).get(CyNetwork.NAME, String.class);
 					edgeHiddenTable.getRow(node.getSUID()).set(setTableName, true);
 				}
 				DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(new NodeInfo(cyIdName, node));
 				cyIdNode.get(event.getSetName()).put(node.getSUID(), newTreeNode);
 				treeModel.insertNodeInto(newTreeNode, setNode, setNode.getChildCount());
 			}
 		if (removed != null)
 			for (CyIdentifiable node: removed) {
 				if (cyNetwork.getNode(node.getSUID()) != null)
 					nodeHiddenTable.getRow(node.getSUID()).set(setTableName, false);
 				if (cyNetwork.getEdge(node.getSUID()) != null)
 					edgeHiddenTable.getRow(node.getSUID()).set(setTableName, false);
 				treeModel.removeNodeFromParent(cyIdNode.get(event.getSetName()).get(node.getSUID()));
 				cyIdNode.get(event.getSetName()).remove(node.getSUID());
 			}
 		setNode.setUserObject(new NodeInfo(event.getSetName() + " (" + setNode.getChildCount() + ")", event.getSetName()));
 	}
 	
 	private class NodeInfo {
 		public String label;
 		public CyIdentifiable cyId;
 		public String setName;
 		
 		public NodeInfo(String name, CyIdentifiable s) {
 			label = name;
 			cyId = s;
 			setName = null;
 		}
 		public NodeInfo(String name, String setName) {
 			label = name;
 			this.setName = setName;
 			cyId = null;
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
 		setsNode.get(event.getSetName()).setUserObject(new NodeInfo(event.getSetName() + " (" + setsNode.get(event.getSetName()).getChildCount() + ")", event.getSetName()));
 		
 		CyNetwork cyNetwork = mySets.getCyNetwork(event.getSetName());
 		CyTable networkTable = null;
 		if (mySets.getType(event.getSetName()) == CyIdType.NODE)
 			networkTable = cyNetwork.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
 		if (mySets.getType(event.getSetName()) == CyIdType.EDGE)
 			networkTable = cyNetwork.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
 		if (networkTable != null) {
 			networkTable.deleteColumn(tablePrefix + event.getOldSetName());
 			networkTable.createColumn(tablePrefix + event.getSetName(), Boolean.class, false);
 			for (Long suid: networkTable.getPrimaryKey().getValues(Long.class))
 				networkTable.getRow(suid).set(tablePrefix + event.getSetName(), mySets.isInSet(event.getSetName(), suid));
 		}
 	}
 }
