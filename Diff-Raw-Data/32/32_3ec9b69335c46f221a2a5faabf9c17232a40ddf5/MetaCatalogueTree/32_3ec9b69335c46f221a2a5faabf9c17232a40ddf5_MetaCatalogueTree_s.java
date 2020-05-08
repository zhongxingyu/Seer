 package Sirius.navigator.ui.tree;
 
 /*******************************************************************************
  *
  * Copyright (c)	:	EIG (Environmental Informatics Group)
  * http://www.htw-saarland.de/eig
  * Prof. Dr. Reiner Guettler
  * Prof. Dr. Ralf Denzer
  *
  * HTWdS
  * Hochschule fuer Technik und Wirtschaft des Saarlandes
  * Goebenstr. 40
  * 66117 Saarbruecken
  * Germany
  *
  * Programmers		:	Pascal
  *
  * Project			:	WuNDA 2
  * Filename		:
  * Version			:	1.0
  * Purpose			:
  * Created			:	27.04.2000
  * History			: 30.10.2001 changes by M. Derschang (vgl. MANU_NAV)
  *
  *******************************************************************************/
 import java.util.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 
 import org.apache.log4j.Logger;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.types.treenode.*;
 import Sirius.navigator.ui.status.*;
 import Sirius.navigator.method.*;
 import Sirius.navigator.plugin.PluginRegistry;
 import Sirius.navigator.plugin.interfaces.PluginSupport;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.newuser.permission.PermissionHolder;
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.cids.utils.MetaTreeNodeVisualization;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.dnd.Autoscroll;
 import java.awt.image.BufferedImage;
 
 /**
  * DefaultMetaTree ist ein Navigationsbaum
  */
 public class MetaCatalogueTree extends JTree implements StatusChangeSupport, Autoscroll {
 
     protected final Logger logger = Logger.getLogger(MetaCatalogueTree.class);
     protected final DefaultStatusChangeSupport statusChangeSupport;
     protected final ResourceManager resources;
     protected final DefaultTreeModel defaultTreeModel;
     protected final boolean useThread;
     protected final int maxThreadCount;
     protected TreeExploreThread treeExploreThread;
     protected volatile int threadCount = 0;
     private BufferedImage dragImage = null;
 
     public MetaCatalogueTree(RootTreeNode rootTreeNode, boolean editable) {
         this(rootTreeNode, editable, true, 3);
     }
 
     public MetaCatalogueTree(RootTreeNode rootTreeNode, boolean editable, boolean useThread, int maxThreadCount) {
         this.setModel(new DefaultTreeModel(rootTreeNode, true));
         this.setEditable(editable);
         this.useThread = useThread;
         this.maxThreadCount = maxThreadCount;
 
         this.statusChangeSupport = new DefaultStatusChangeSupport(this);
         this.resources = ResourceManager.getManager();
         this.defaultTreeModel = (DefaultTreeModel) this.getModel();
 
         this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
         this.setCellRenderer(new MetaTreeNodeRenderer());
         this.putClientProperty("JTree.lineStyle", "Angled");
         this.setShowsRootHandles(true);
         this.setRootVisible(false);
         this.addTreeSelectionListener(new MetaCatalogueSelectionListener());
         this.addTreeExpansionListener(new MetaCatalogueExpansionListener());
 
         if (editable) {
             InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
             ActionMap actionMap = this.getActionMap();
 
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteNode()");
             actionMap.put("deleteNode()", new AbstractAction() {
 
                 public void actionPerformed(ActionEvent e) {
                     if (logger.isDebugEnabled()) {
                         logger.debug("performing delete node action");
                     }
                     DefaultMetaTreeNode selectedNode = MetaCatalogueTree.this.getSelectedNode();
                     if (selectedNode != null && selectedNode.isLeaf()) {
                         if (MethodManager.getManager().checkPermission(selectedNode.getNode(), PermissionHolder.WRITEPERMISSION)) {
                             MethodManager.getManager().deleteNode(MetaCatalogueTree.this, selectedNode);
                         } else if (logger.isDebugEnabled()) {
                             logger.warn("actionPerformed() deleting not possible, no node selected");
                         }
                     }
                 }
             });
         }
         addMouseListener(new MouseAdapter() {
 
             public void mouseClicked(MouseEvent e) {
                 super.mouseClicked(e);
                 if (e.getClickCount() > 1) {
                     try {
                         PluginSupport map = PluginRegistry.getRegistry().getPlugin("cismap");
 
                         Vector<DefaultMetaTreeNode> v = new Vector<DefaultMetaTreeNode>();
                         DefaultMetaTreeNode[] resultNodes = getSelectedNodesArray();
                         for (int i = 0; i < resultNodes.length; ++i) {
                             logger.debug("resultNodes:" + resultNodes[i]);
                             if (resultNodes[i].getNode() instanceof MetaObjectNode) {
                                 //ObjectTreeNode otn=new ObjectTreeNode((MetaObjectNode)resultNodes[i].getNode());
                                 DefaultMetaTreeNode otn = resultNodes[i];
                                 v.add(otn);
                             }
                         }
                         if (v.size() > 0) {
                             MetaTreeNodeVisualization.getInstance().addVisualization(v);
                             //((de.cismet.cismap.navigatorplugin.CismapPlugin) map).showInMap(v, false);
                         }
 
                     } catch (Throwable t) {
                         logger.warn("Fehler beim Anzeigen mit der Karte", t);
                     }
                 }
             }
         });
     }
 
     /*public void update(TreeNode node)
     {
     this.defaultTreeModel.nodeStructureChanged(node);
     }*/
     // -------------------------------------------------------------------------
     /**
      * Expandiert alle geladenen Knoten.
      */
     public void expandAll() {
         int row = 0;
         while (row < this.getRowCount()) {
             this.expandRow(row);
             row++;
         }
     }
 
     /**
      * Schliesst alle expandierten Knoten.
      */
     public void collapseAll() {
         int row = this.getRowCount() - 1;
         while (row >= 0) {
             this.collapseRow(row);
             row--;
         }
     }
 
     public void exploreSubtree(final TreePath treePath) {
         final Object[] nodes = treePath.getPath();
         final Object rootNode = this.getModel().getRoot();
         if (rootNode != null && nodes != null && nodes.length > 1) {
             if (logger.isDebugEnabled()) {
                 logger.debug("exploring subtree: " + nodes.length);
             }
             final List<?> nodeList = Arrays.asList(nodes);
             for (final Object o : nodeList) {
                 if (!(o instanceof DefaultMetaTreeNode)) {
                     nodeList.remove(o);
                     logger.error("Node " + o + " is not instance of DefaultMetaTreeNode and hast been removed from the Collection.");
                 }
             }
             final Iterator<DefaultMetaTreeNode> childrenIterator = (Iterator<DefaultMetaTreeNode>) nodeList.iterator();
 
             // Root Node entfernen
             childrenIterator.next();
 
             SubTreeExploreThread subTreeExploreThread = new SubTreeExploreThread((DefaultMetaTreeNode) rootNode, childrenIterator);
             CismetThreadPool.execute(subTreeExploreThread);
 

            //Versuch den den refresh Bug bei dyn Knoten rauszumachen
//
//            DefaultMetaTreeNode parentNode = (DefaultMetaTreeNode) rootNode;
//            while (childrenIterator.hasNext()) {
//                DefaultMetaTreeNode child = (DefaultMetaTreeNode) childrenIterator.next();
//
//                final DefaultMetaTreeNode parent = parentNode;
//                Iterator singleChild = Arrays.asList(child).iterator();
//                try {
////                    final TreePath selectionPath = parent.explore(singleChild);
//////                    SwingUtilities.invokeAndWait(new Runnable() {
//////                        public void run() {
////                            MetaCatalogueTree.this.defaultTreeModel.nodeStructureChanged((TreeNode)parent);
////                            MetaCatalogueTree.this.expandPath(selectionPath);
////                            MetaCatalogueTree.this.setSelectionPath(selectionPath);
//////                        }
//////                    });
//                    logger.info("parent="+ parent+"  child=" + child);
//                    SubTreeExploreThread subTreeExploreThread = new SubTreeExploreThread(parent, singleChild);
//                    subTreeExploreThread.start();
//                    parentNode = child;
//
//                } catch (Exception ex) {
//                    logger.error("Fehler beim selektieren", ex);
//                }
//
////                SwingUtilities.invokeLater(treeUpdateThread);
//            }



         } else {
             logger.warn("could not explore subtree");
         }
     }
 
     // -------------------------------------------------------------------------
     /**
      * Gibt an, wieviele Knoten selektiert sind.
      * Wurde durch getSelectedNodeCount() ersetzt.
      *
      * @return Die Anzahl der selektierten Knoten oder 0.
      * @deprecated use getSelectedNodeCount().
      */
     public synchronized int getSelectionCount() {
         TreePath[] selectedPaths = this.getSelectionPaths();
 
         if (selectedPaths == null) {
             //if(logger.isDebugEnabled())logger.debug("<TREE> getSelectionCount(): none selected");
             return 0;
         }
 
         //if(logger.isDebugEnabled())logger.debug("<TREE> getSelectionCount(): " + selectedPaths);
         return selectedPaths.length;
     }
     /**
      * Gibt an, wieviele Knoten selektiert sind.
      *
      * @return Die Anzahl der selektierten Knoten oder 0.
      */
     private final Object selectionBlocker = new Object();
 
     public int getSelectedNodeCount() {
         synchronized (selectionBlocker) {
             TreePath[] selectedPaths = this.getSelectionPaths();
 
             if (selectedPaths == null || selectedPaths.length == 0) {
                 //if(logger.isDebugEnabled())logger.debug("<TREE> getSelectionCount(): none selected");
                 return 0;
             }
 
             int j = 0;
 
             for (int i = 0; i < selectedPaths.length; i++) {
                 if (!((DefaultMetaTreeNode) selectedPaths[i].getLastPathComponent()).isWaitNode()) {
                     j++;
                 }
             }
 
             if (logger.isDebugEnabled()) {
                 logger.debug("<TREE> getSelectedNodeCount(): " + j);
             }
             return j;
         }
     }
 
     public DefaultMetaTreeNode getSelectedNode() {
         synchronized (selectionBlocker) {
             Object object = this.getLastSelectedPathComponent();
 
             if (object != null && !((DefaultMetaTreeNode) object).isWaitNode() && !((DefaultMetaTreeNode) object).isRootNode()) {
                 return (DefaultMetaTreeNode) object;
             } else {
                 return null;
             }
         }
     }
 
     public Collection getSelectedNodes() {
         TreePath[] selectedPaths = this.getSelectionPaths();
 
         if (selectedPaths != null && selectedPaths.length > 0) {
             ArrayList selectedNodes = new ArrayList(selectedPaths.length);
             for (int i = 0; i < selectedPaths.length; i++) {
                 selectedNodes.add(selectedPaths[i].getLastPathComponent());
             }
 
             return selectedNodes;
         } else {
             return new LinkedList();
         }
     }
 
     public void setSelectedNodes(Collection<DefaultMutableTreeNode> selectedNodes, boolean expandTree) {
         if (logger.isDebugEnabled()) {
             logger.info("setSelectedNodes(): selecting " + selectedNodes.size() + " nodes, expanding tree: " + expandTree);
         }
         ArrayList treePaths = new ArrayList();
         this.setExpandsSelectedPaths(expandTree);
 
         for (Iterator<DefaultMutableTreeNode> iterator = selectedNodes.iterator(); iterator.hasNext();) {
             treePaths.add(new TreePath(iterator.next().getPath()));
         }
 
         if (treePaths.size() > 0) {
             this.setSelectionPaths((TreePath[]) treePaths.toArray(new TreePath[treePaths.size()]));
         } else if (logger.isDebugEnabled()) {
             logger.warn("setSelectedNodes(): collections of nodes is empty");
         }
         //vor Messe
         if (selectedNodes.size() == 0) {
             this.removeSelectionPaths(getSelectionPaths());
         }
     }
     //HELL
     private int margin = 12;
 
     public void autoscroll(Point p) {
         int realrow = getRowForLocation(p.x, p.y);
         Rectangle outer = getBounds();
         realrow = (p.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1
                 : realrow < getRowCount() - 1 ? realrow + 1 : realrow);
         scrollRowToVisible(realrow);
     }
 
     public Insets getAutoscrollInsets() {
         Rectangle outer = getBounds();
         Rectangle inner = getParent().getBounds();
         return new Insets(inner.y - outer.y + margin, inner.x - outer.x + margin, outer.height - inner.height - inner.y + outer.y + margin, outer.width - inner.width - inner.x + outer.x + margin);
     }
 
     public DefaultMetaTreeNode[] getSelectedNodesArray() {
         Collection selectedNodes = this.getSelectedNodes();
         return (DefaultMetaTreeNode[]) selectedNodes.toArray(new DefaultMetaTreeNode[selectedNodes.size()]);
     }
 
     public void addStatusChangeListener(StatusChangeListener listener) {
         this.statusChangeSupport.addStatusChangeListener(listener);
     }
 
     public void removeStatusChangeListener(StatusChangeListener listener) {
         this.statusChangeSupport.removeStatusChangeListener(listener);
     }
 
     public boolean isPathEditable(TreePath treePath) {
         return false;
     }
 
     // INNERE KLASSEN ==========================================================
     /**
      *
      */
     protected class MetaCatalogueSelectionListener implements TreeSelectionListener {
 
         public void valueChanged(TreeSelectionEvent e) {
             DefaultMetaTreeNode selectedNode = (DefaultMetaTreeNode) (e.getPath().getLastPathComponent());
             statusChangeSupport.fireStatusChange(MetaCatalogueTree.this.getSelectionCount() + " " + resources.getString("tree.catalogue.status.selected"), Status.MESSAGE_POSITION_1);
         }
     }
 
     /**
      * Dieser innere Klasse sorgt dafuer, dass an den Knoten der expandiert wurde
      * seine Children vom Server (oder aus dem Cache) geladen und angehaengt werden
      * indem die <b>explore()</b> des Knotens ausgefuehrt wird.
      * Fuer einen Knoten der bereits expandiert wurde (d.h. dessen Children bereits vom
      * Server geladen wurden) muss die Funktion <b>explore()</b> nicht mehr ausgefuehrt
      * werden.<b>
      *
      * Wenn der DefaultMetaTree Multithreading benutzen soll, wird an den expandierten
      * Knoten eine <b>DefaultMetaTreeNode</b> vom Typ <b>WaitNode</b> aengehaengt. Anschliessend
      * wird <b>nodeStructureChanged(node)</b> aufgerufen, um diesen Knoten anzuzeigen.<br>
      * Die <b>WaitNode</b> wird zwar sofort innerhalb der <b>explore</b> Funktion
      * der selektierten <b>DefaultMetaTreeNode</b> wieder entfernt (<b>removeChildren</b>)
      * aber da der <b>TreeExploreThread</b> das GUI Update asynchron zum
      * <b>EventDispatchThread</b> ausfuehrt, verschwindet die <b>WaitNode</b> im
      * TreeView erst, wenn alle Children vom Server geladen wurden.<br>
      *
      * Wenn keine Threads benutzt werden bzw. die maximale Anzahl kokurrierender
      * Threads erreicht wurde, wird <b>explore()</b> sofort ausgefuehrt und
      * das GUI bleibt blockiert, bis alle Children geladen und visualisiert wurden.
      */
     protected class MetaCatalogueExpansionListener implements TreeExpansionListener {
 
         final WaitTreeNode waitNode = new WaitTreeNode();
 
         public void treeCollapsed(TreeExpansionEvent e) {
         }
 
         public void treeExpanded(TreeExpansionEvent e) {
             final DefaultMetaTreeNode selectedNode = (DefaultMetaTreeNode) (e.getPath().getLastPathComponent());
             if (logger.isDebugEnabled()) {
                 logger.debug("treeExpanded() Expanding Node: " + selectedNode.toString());
             }
 
             if (!selectedNode.isLeaf() && !selectedNode.isExplored()) // && selectedNode.getChildCount() == 0)
             {
                 if (logger.isDebugEnabled()) {
                     logger.debug("treeExpanded() Expanding Node: " + selectedNode.toString() + " ok.");
                 }
                 if (useThread && threadCount < maxThreadCount - 1) {
                     selectedNode.add(waitNode);
                     defaultTreeModel.nodeStructureChanged(selectedNode);
 
                     treeExploreThread = new TreeExploreThread(selectedNode, defaultTreeModel);
                     CismetThreadPool.execute(treeExploreThread);
                 } else {
                     try {
                         statusChangeSupport.fireStatusChange(resources.getString("tree.catalogue.status.loading"), Status.MESSAGE_POSITION_1, Status.ICON_IGNORE, Status.ICON_BLINKING);
                         selectedNode.explore();
                         defaultTreeModel.nodeStructureChanged(selectedNode);
                         statusChangeSupport.fireStatusChange(resources.getString("tree.catalogue.status.loaded.server"), Status.MESSAGE_POSITION_1, Status.ICON_ACTIVATED, Status.ICON_DEACTIVATED);
                     } catch (Exception exp) {
                         statusChangeSupport.fireStatusChange(resources.getString("tree.catalogue.status.loading.error"), Status.MESSAGE_POSITION_1, Status.ICON_DEACTIVATED, Status.ICON_ACTIVATED);
                         logger.fatal("treeExpanded() could not load nodes", exp);
                         selectedNode.removeChildren();
                     }
                 }
             } else {
                 if (logger.isDebugEnabled()) {
                     logger.debug("treeExpanded() " + selectedNode.getNode() + "'s children loaded from cache");
                 }
                 statusChangeSupport.fireStatusChange(resources.getString("tree.catalogue.status.loaded.cache"), Status.MESSAGE_POSITION_1, Status.ICON_ACTIVATED, Status.ICON_DEACTIVATED);
             }
         }
     }
 
     /**
      * Der <b>TreeExploreThread</b> sorgt dafuer, dass die TreeNodes im Hintergrund geladen
      * werden. Innerhalb des Threads wird die <b>explore()</b> Funktion der selektierten
      * DefaultMetaTreeNode ausgefuehrt. Waehrend des Ladevorgangs der Daten vom Server
      * wird der TreeView nicht aktualisiert, da ansonsten das GUI w\u00E4hrend dieser
      * Zeit komplett blockiert wuerde (kein repaint() meher!). Deshalb geschieht die
      * eigentlich Aktualisierung des Views asynchron zum <b>EventDispatchThread</b>.<br>
      * Das bedeutet, dass das GUI nicht innerhalb des EventDispatchThreads
      * (in diesem Fall der <b>TreeExpansionEvent</b>) oder aus einem aus ihm heraus gestarteten
      * Thread (z.B. der TreeExploreThread) aktuakisiert wird. Die Aktualisierung
      * erfolgt durch <b>nodeStructureChanged(node)</b>. Diese Funktion wird in
      * einem weiteren Thread innerhalb des TreeExploreThreads ausgefuehrt, der
      * durch <b>SwingUtilities.invokeLater(runnable)</b> erst gestartet wird, wenn
      * der <b>TreeExpansionEvent</b> bzw. der <b>TreeExploreThread</b> beendet
      */
     private class TreeExploreThread extends Thread {
 
         private Runnable treeUpdateThread = null;
         private final DefaultMetaTreeNode node;
 
         public TreeExploreThread(final DefaultMetaTreeNode selectedNode, DefaultTreeModel defaultTreeModel) {
             if (logger.isDebugEnabled()) {
                 logger.debug("<THREAD>: TreeExploreThread");
             }
             threadCount++;
             node = selectedNode;
 
             treeUpdateThread = new Runnable() {
 
                 public void run() {
                     MetaCatalogueTree.this.defaultTreeModel.nodeStructureChanged(node);
                     if (logger.isDebugEnabled()) {
                         logger.debug("<THREAD>: TreeExploreThread GUI Update fertig");
                     }
                 }
             };
         }
 
         public void run() {
             try {
                 statusChangeSupport.fireStatusChange(resources.getString("tree.catalogue.status.loading"), Status.MESSAGE_POSITION_1, Status.ICON_BLINKING, Status.ICON_IGNORE);
                 node.explore();
 
 
                 //logger.fatal("/////////////HELL Hier gehts weiter");
                 Thread t = new Thread(new Runnable() {
 
                     public void run() {
                         for (int i = 0; i < defaultTreeModel.getChildCount(node); ++i) {
 //                                    try {
 //                                        Thread.sleep(100);
 //                                    } catch (InterruptedException ex) {
 //                                        ex.printStackTrace();
 //                                    }
                             try {
                                 final DefaultMetaTreeNode n = (DefaultMetaTreeNode) defaultTreeModel.getChild(node, i);
                                 //logger.fatal("HELL: "+n);
 //                                if (n != null && n.getNode() != null && n.getNode().getName() == null && n.isObjectNode()) {
 //                                    //logger.fatal("HELL: in if");
 //                                    try {
 //                                        final ObjectTreeNode on = ((ObjectTreeNode) n);
 //                                        EventQueue.invokeLater(new Runnable() {
 //
 //                                            public void run() {
 //                                                n.getNode().setName("Name wird geladen .....");
 //                                                defaultTreeModel.nodeChanged(on);
 //                                            }
 //                                        });
 //                                        if (logger.isDebugEnabled()) {
 //                                            logger.debug("caching object node");
 //                                        }
 //                                        final MetaObject metaObject = SessionManager.getProxy().getMetaObject(on.getMetaObjectNode().getObjectId(), on.getMetaObjectNode().getClassId(), on.getMetaObjectNode().getDomain());
 //                                        on.getMetaObjectNode().setObject(metaObject);
 //                                        EventQueue.invokeLater(new Runnable() {
 //
 //                                            public void run() {
 //                                                logger.debug("setze den namen des "+n+" mit null als name auf:" + metaObject.toString());
 //                                                n.getNode().setName(metaObject.toString());
 //                                                defaultTreeModel.nodeChanged(on);
 //                                            }
 //                                        });
 //
 //                                    } catch (Throwable t) {
 //                                        logger.error("could not retrieve meta object of node '" + this + "'", t);
 //                                    }
 //                                } else {
 //                                    logger.debug("n.getNode().getName()!=null: " + n.getNode().getName() + ":");
 //                                }
                             } catch (Exception e) {
                                 logger.error("Fehler beim Laden des Namen", e);
                             }
                         }
                     }
                 });
                 CismetThreadPool.execute(t);
 
 
                 // GUI Update asynchron zum EventDispatchThread
                 SwingUtilities.invokeLater(treeUpdateThread);
                 //SwingUtilities.invokeAndWait(runnable);
                 statusChangeSupport.fireStatusChange(resources.getString("tree.catalogue.status.loaded.server"), Status.MESSAGE_POSITION_1, Status.ICON_ACTIVATED, Status.ICON_DEACTIVATED);
                 threadCount--;
             } catch (Exception exp) {
                 logger.fatal("could not load nodes", exp);
                 statusChangeSupport.fireStatusChange(resources.getString("tree.catalogue.status.loading.error"), Status.MESSAGE_POSITION_1, Status.ICON_DEACTIVATED, Status.ICON_ACTIVATED);
                 node.removeChildren();
                 SwingUtilities.invokeLater(treeUpdateThread);
                 threadCount--;
             }
 
             if (logger.isDebugEnabled()) {
                 logger.debug("<THREAD> TreeExploreThread fertig");
             }
         }
     }
 
     private class SubTreeExploreThread extends Thread {
 
         private final DefaultMetaTreeNode node;
         private final Iterator<DefaultMetaTreeNode> childrenNodes;
 
         public SubTreeExploreThread(final DefaultMetaTreeNode rootNode, final Iterator<DefaultMetaTreeNode> childrenNodes) {
             this.node = rootNode;
             this.childrenNodes = childrenNodes;
         }
 
         public void run() {
             try {
                 statusChangeSupport.fireStatusChange(resources.getString("tree.catalogue.status.loading"), Status.MESSAGE_POSITION_1, Status.ICON_BLINKING, Status.ICON_IGNORE);
 
                 TreePath selectionPath = this.node.explore(this.childrenNodes);
 
                 TreeUpdateThread treeUpdateThread = new TreeUpdateThread(selectionPath);
 
                 SwingUtilities.invokeLater(treeUpdateThread);
 
                 statusChangeSupport.fireStatusChange(resources.getString("tree.catalogue.status.loaded.server"), Status.MESSAGE_POSITION_1, Status.ICON_ACTIVATED, Status.ICON_DEACTIVATED);
             } catch (Exception exp) {
                 logger.fatal("SubTreeExploreThread: could not load nodes", exp);
                 statusChangeSupport.fireStatusChange(resources.getString("tree.catalogue.status.loading.error"), Status.MESSAGE_POSITION_1, Status.ICON_DEACTIVATED, Status.ICON_ACTIVATED);
 
                 node.removeChildren();
                 MetaCatalogueTree.this.defaultTreeModel.nodeStructureChanged(node);
             }
 
             if (logger.isDebugEnabled()) {
                 logger.debug("SubTreeExploreThread: fertig");
             }
         }
 
         private class TreeUpdateThread implements Runnable {
 
             private final TreePath selectionPath;
 
             private TreeUpdateThread(final TreePath selectionPath) {
                 this.selectionPath = selectionPath;
             }
 
             public void run() {
                 MetaCatalogueTree.this.defaultTreeModel.nodeStructureChanged(node);
                 MetaCatalogueTree.this.setSelectionPath(this.selectionPath);
 //                Object o = selectionPath.getLastPathComponent();
 //                MetaCatalogueTree.this.expandPath(selectionPath);
                 MetaCatalogueTree.this.scrollPathToVisible(selectionPath);
 
                 if (logger.isDebugEnabled()) {
                     logger.debug("SubTreeExploreThread: GUI Update fertig");
                 }
             }
         }
     }
 
     public BufferedImage getDragImage() {
         return dragImage;
     }
 
     public void setDragImage(BufferedImage dragImage) {
         this.dragImage = dragImage;
     }
 }
