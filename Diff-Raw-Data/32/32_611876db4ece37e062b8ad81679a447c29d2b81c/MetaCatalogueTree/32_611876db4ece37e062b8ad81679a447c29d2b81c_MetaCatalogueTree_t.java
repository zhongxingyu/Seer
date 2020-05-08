 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.ui.tree;
 
 /*******************************************************************************
  *
  * Copyright (c)        :       EIG (Environmental Informatics Group)
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
  * Programmers          :       Pascal
  *
  * Project                      :       WuNDA 2
  * Filename             :
  * Version                      :       1.0
  * Purpose                      :
  * Created                      :       27.04.2000
  * History                      : 30.10.2001 changes by M. Derschang (vgl. MANU_NAV)
  *
  *******************************************************************************/
 import Sirius.navigator.method.*;
 import Sirius.navigator.types.treenode.*;
 import Sirius.navigator.ui.status.*;
 
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.newuser.permission.PermissionHolder;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.dnd.Autoscroll;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 
 import de.cismet.cids.navigator.utils.MetaTreeNodeVisualization;
 
 import de.cismet.tools.CismetThreadPool;
 
 /**
  * DefaultMetaTree ist ein Navigationsbaum.
  *
  * @version  $Revision$, $Date$
  */
 public class MetaCatalogueTree extends JTree implements StatusChangeSupport, Autoscroll {
 
     //~ Instance fields --------------------------------------------------------
 
     protected final Logger logger = Logger.getLogger(MetaCatalogueTree.class);
     protected final DefaultStatusChangeSupport statusChangeSupport;
     protected final DefaultTreeModel defaultTreeModel;
     protected final boolean useThread;
     protected final int maxThreadCount;
     protected TreeExploreThread treeExploreThread;
     protected volatile int threadCount = 0;
     private BufferedImage dragImage = null;
     /**
      * Gibt an, wieviele Knoten selektiert sind.
      *
      * @return  Die Anzahl der selektierten Knoten oder 0.
      */
     private final Object selectionBlocker = new Object();
     // HELL
     private int margin = 12;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MetaCatalogueTree object.
      *
      * @param  rootTreeNode  DOCUMENT ME!
      * @param  editable      DOCUMENT ME!
      */
     public MetaCatalogueTree(final RootTreeNode rootTreeNode, final boolean editable) {
         this(rootTreeNode, editable, true, 3);
     }
 
     /**
      * Creates a new MetaCatalogueTree object.
      *
      * @param  rootTreeNode    DOCUMENT ME!
      * @param  editable        DOCUMENT ME!
      * @param  useThread       DOCUMENT ME!
      * @param  maxThreadCount  DOCUMENT ME!
      */
     public MetaCatalogueTree(final RootTreeNode rootTreeNode,
             final boolean editable,
             final boolean useThread,
             final int maxThreadCount) {
         this.setModel(new DefaultTreeModel(rootTreeNode, true));
         this.setEditable(editable);
         this.useThread = useThread;
         this.maxThreadCount = maxThreadCount;
 
         this.statusChangeSupport = new DefaultStatusChangeSupport(this);
         this.defaultTreeModel = (DefaultTreeModel)this.getModel();
 
         this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
         this.setCellRenderer(new MetaTreeNodeRenderer());
         this.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N
         this.setShowsRootHandles(true);
         this.setRootVisible(false);
         this.addTreeSelectionListener(new MetaCatalogueSelectionListener());
         this.addTreeExpansionListener(new MetaCatalogueExpansionListener());
 
         if (editable) {
             final InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
             final ActionMap actionMap = this.getActionMap();
 
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteNode()"); // NOI18N
             actionMap.put("deleteNode()", new AbstractAction() {                         // NOI18N
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         if (logger.isDebugEnabled()) {
                             logger.debug("performing delete node action");                                // NOI18N
                         }
                         final DefaultMetaTreeNode selectedNode = MetaCatalogueTree.this.getSelectedNode();
                         if ((selectedNode != null) && selectedNode.isLeaf()) {
                             if (MethodManager.getManager().checkPermission(
                                             selectedNode.getNode(),
                                             PermissionHolder.WRITEPERMISSION)) {
                                 MethodManager.getManager().deleteNode(MetaCatalogueTree.this, selectedNode);
                             } else if (logger.isDebugEnabled()) {
                                 logger.warn("actionPerformed() deleting not possible, no node selected"); // NOI18N
                             }
                         }
                     }
                 });
         }
         addMouseListener(new MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final MouseEvent e) {
                     super.mouseClicked(e);
                     if (e.getClickCount() > 1) {
                         try {
 //                        PluginSupport map = PluginRegistry.getRegistry().getPlugin("cismap");
 
                             final Vector<DefaultMetaTreeNode> v = new Vector<DefaultMetaTreeNode>();
                             final DefaultMetaTreeNode[] resultNodes = getSelectedNodesArray();
                             for (int i = 0; i < resultNodes.length; ++i) {
                                 if (logger.isDebugEnabled()) {
                                     logger.debug("resultNodes:" + resultNodes[i]); // NOI18N
                                 }
                                 if (resultNodes[i].getNode() instanceof MetaObjectNode) {
                                     // ObjectTreeNode otn=new ObjectTreeNode((MetaObjectNode)resultNodes[i].getNode());
                                     final DefaultMetaTreeNode otn = resultNodes[i];
                                     v.add(otn);
                                 }
                             }
                             if (v.size() > 0) {
                                 MetaTreeNodeVisualization.getInstance().addVisualization(v);
                                 // ((de.cismet.cismap.navigatorplugin.CismapPlugin) map).showInMap(v, false);
                             }
                         } catch (Throwable t) {
                             logger.warn("Error of displaying map", t); // NOI18N
                         }
                     }
                 }
             });
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /*public void update(TreeNode node)
      * { this.defaultTreeModel.nodeStructureChanged(node);}*/
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
 
     /**
      * DOCUMENT ME!
      *
      * @param  treePath  DOCUMENT ME!
      */
     public void exploreSubtree(final TreePath treePath) {
         final Object[] nodes = treePath.getPath();
         final Object rootNode = this.getModel().getRoot();
         if ((rootNode != null) && (nodes != null) && (nodes.length > 1)) {
             if (logger.isDebugEnabled()) {
                 logger.debug("exploring subtree: " + nodes.length);                                                      // NOI18N
             }
             final List<?> nodeList = Arrays.asList(nodes);
             for (final Object o : nodeList) {
                 if (!(o instanceof DefaultMetaTreeNode)) {
                     nodeList.remove(o);
                     logger.error("Node " + o
                                 + " is not instance of DefaultMetaTreeNode and hast been removed from the Collection."); // NOI18N
                 }
             }
             final Iterator<DefaultMetaTreeNode> childrenIterator = (Iterator<DefaultMetaTreeNode>)nodeList.iterator();
 
             // Root Node entfernen
             childrenIterator.next();
 
             final SubTreeExploreThread subTreeExploreThread = new SubTreeExploreThread((DefaultMetaTreeNode)rootNode,
                     childrenIterator);
             CismetThreadPool.execute(subTreeExploreThread);
         } else {
             logger.warn("could not explore subtree"); // NOI18N
         }
     }
 
     // -------------------------------------------------------------------------
     /**
      * Gibt an, wieviele Knoten selektiert sind. Wurde durch getSelectedNodeCount() ersetzt.
      *
      * @return      Die Anzahl der selektierten Knoten oder 0.
      *
      * @deprecated  use getSelectedNodeCount().
      */
     @Override
     public synchronized int getSelectionCount() {
         final TreePath[] selectedPaths = this.getSelectionPaths();
 
         if (selectedPaths == null) {
             // if(logger.isDebugEnabled())logger.debug("<TREE> getSelectionCount(): none selected");
             return 0;
         }
 
         // if(logger.isDebugEnabled())logger.debug("<TREE> getSelectionCount(): " + selectedPaths);
         return selectedPaths.length;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getSelectedNodeCount() {
         synchronized (selectionBlocker) {
             final TreePath[] selectedPaths = this.getSelectionPaths();
 
             if ((selectedPaths == null) || (selectedPaths.length == 0)) {
                 // if(logger.isDebugEnabled())logger.debug("<TREE> getSelectionCount(): none selected");
                 return 0;
             }
 
             int j = 0;
 
             for (int i = 0; i < selectedPaths.length; i++) {
                 if (!((DefaultMetaTreeNode)selectedPaths[i].getLastPathComponent()).isWaitNode()) {
                     j++;
                 }
             }
 
             if (logger.isDebugEnabled()) {
                 logger.debug("<TREE> getSelectedNodeCount(): " + j); // NOI18N
             }
             return j;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public DefaultMetaTreeNode getSelectedNode() {
         synchronized (selectionBlocker) {
             final Object object = this.getLastSelectedPathComponent();
 
             if ((object != null) && !((DefaultMetaTreeNode)object).isWaitNode()
                         && !((DefaultMetaTreeNode)object).isRootNode()) {
                 return (DefaultMetaTreeNode)object;
             } else {
                 return null;
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Collection getSelectedNodes() {
         final TreePath[] selectedPaths = this.getSelectionPaths();
 
         if ((selectedPaths != null) && (selectedPaths.length > 0)) {
             final ArrayList selectedNodes = new ArrayList(selectedPaths.length);
             for (int i = 0; i < selectedPaths.length; i++) {
                 selectedNodes.add(selectedPaths[i].getLastPathComponent());
             }
 
             return selectedNodes;
         } else {
             return new LinkedList();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  selectedNodes  DOCUMENT ME!
      * @param  expandTree     DOCUMENT ME!
      */
     public void setSelectedNodes(final Collection<DefaultMutableTreeNode> selectedNodes, final boolean expandTree) {
         if (logger.isDebugEnabled()) {
             logger.info("setSelectedNodes(): selecting " + selectedNodes.size() + " nodes, expanding tree: "
                         + expandTree); // NOI18N
         }
         final ArrayList treePaths = new ArrayList();
         this.setExpandsSelectedPaths(expandTree);
 
         for (final Iterator<DefaultMutableTreeNode> iterator = selectedNodes.iterator(); iterator.hasNext();) {
             final DefaultMutableTreeNode next = iterator.next();
             if (next != null) {
                 treePaths.add(new TreePath(next.getPath()));
             }
         }
 
         if (treePaths.size() > 0) {
             this.setSelectionPaths((TreePath[])treePaths.toArray(new TreePath[treePaths.size()]));
         } else if (logger.isDebugEnabled()) {
             logger.warn("setSelectedNodes(): collections of nodes is empty"); // NOI18N
         }
         // vor Messe
         if (selectedNodes.size() == 0) {
             this.removeSelectionPaths(getSelectionPaths());
         }
     }
 
     @Override
     public void autoscroll(final Point p) {
         int realrow = getRowForLocation(p.x, p.y);
         final Rectangle outer = getBounds();
         realrow = (((p.y + outer.y) <= margin) ? ((realrow < 1) ? 0 : (realrow - 1))
                                                : ((realrow < (getRowCount() - 1)) ? (realrow + 1) : realrow));
         scrollRowToVisible(realrow);
     }
 
     @Override
     public Insets getAutoscrollInsets() {
         final Rectangle outer = getBounds();
         final Rectangle inner = getParent().getBounds();
         return new Insets(inner.y - outer.y + margin,
                 inner.x
                         - outer.x
                         + margin,
                 outer.height
                         - inner.height
                         - inner.y
                         + outer.y
                         + margin,
                 outer.width
                         - inner.width
                         - inner.x
                         + outer.x
                         + margin);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public DefaultMetaTreeNode[] getSelectedNodesArray() {
         final Collection selectedNodes = this.getSelectedNodes();
         return (DefaultMetaTreeNode[])selectedNodes.toArray(new DefaultMetaTreeNode[selectedNodes.size()]);
     }
 
     @Override
     public void addStatusChangeListener(final StatusChangeListener listener) {
         this.statusChangeSupport.addStatusChangeListener(listener);
     }
 
     @Override
     public void removeStatusChangeListener(final StatusChangeListener listener) {
         this.statusChangeSupport.removeStatusChangeListener(listener);
     }
 
     @Override
     public boolean isPathEditable(final TreePath treePath) {
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public BufferedImage getDragImage() {
         return dragImage;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  dragImage  DOCUMENT ME!
      */
     public void setDragImage(final BufferedImage dragImage) {
         this.dragImage = dragImage;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected class MetaCatalogueSelectionListener implements TreeSelectionListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void valueChanged(final TreeSelectionEvent e) {
             final DefaultMetaTreeNode selectedNode = (DefaultMetaTreeNode)(e.getPath().getLastPathComponent());
             statusChangeSupport.fireStatusChange(
                 org.openide.util.NbBundle.getMessage(
                     MetaCatalogueTree.class,
                     "MetaCatalogueTree.valueChanged().objectsSelected",
                     new Object[] { MetaCatalogueTree.this.getSelectionCount() }), // NOI18N
                 Status.MESSAGE_POSITION_1);
         }
     }
 
     /**
      * Dieser innere Klasse sorgt dafuer, dass an den Knoten der expandiert wurde seine Children vom Server (oder aus
      * dem Cache) geladen und angehaengt werden indem die <b>explore()</b> des Knotens ausgefuehrt wird. Fuer einen
      * Knoten der bereits expandiert wurde (d.h. dessen Children bereits vom Server geladen wurden) muss die Funktion
      * <b>explore()</b> nicht mehr ausgefuehrt werden.
      *
      * <p> <b>Wenn der DefaultMetaTree Multithreading benutzen soll, wird an den expandierten Knoten eine <b>
      * DefaultMetaTreeNode</b> vom Typ <b>WaitNode</b> aengehaengt. Anschliessend wird <b>nodeStructureChanged(node)</b>
      * aufgerufen, um diesen Knoten anzuzeigen.<br>
      * Die <b>WaitNode</b> wird zwar sofort innerhalb der <b>explore</b> Funktion der selektierten <b>
      * DefaultMetaTreeNode</b> wieder entfernt (<b>removeChildren</b>) aber da der <b>TreeExploreThread</b> das GUI
      * Update asynchron zum <b>EventDispatchThread</b> ausfuehrt, verschwindet die <b>WaitNode</b> im TreeView erst,
      * wenn alle Children vom Server geladen wurden.<br>
      * </b></p>
      *
      * <p>Wenn keine Threads benutzt werden bzw. die maximale Anzahl kokurrierender Threads erreicht wurde, wird <b>
      * explore()</b> sofort ausgefuehrt und das GUI bleibt blockiert, bis alle Children geladen und visualisiert wurden.
      * </p>
      *
      * @version  $Revision$, $Date$
      */
     protected class MetaCatalogueExpansionListener implements TreeExpansionListener {
 
         //~ Instance fields ----------------------------------------------------
 
         final WaitTreeNode waitNode = new WaitTreeNode();
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void treeCollapsed(final TreeExpansionEvent e) {
         }
 
         @Override
         public void treeExpanded(final TreeExpansionEvent e) {
             final DefaultMetaTreeNode selectedNode = (DefaultMetaTreeNode)(e.getPath().getLastPathComponent());
             if (logger.isDebugEnabled()) {
                 logger.debug("treeExpanded() Expanding Node: " + selectedNode.toString()); // NOI18N
             }
 
             if (!selectedNode.isLeaf() && !selectedNode.isExplored())                                   // && selectedNode.getChildCount() == 0)
             {
                 if (logger.isDebugEnabled()) {
                     logger.debug("treeExpanded() Expanding Node: " + selectedNode.toString() + " ok."); // NOI18N
                 }
                 if (useThread) {
                     new SwingWorker<Void, Void>() {
 
                             @Override
                             protected Void doInBackground() throws Exception {
                                 selectedNode.add(waitNode);
                                 defaultTreeModel.nodeStructureChanged(selectedNode);
 
                                 // warten bis genug andere Threads durch sind
                                 while (threadCount >= maxThreadCount) {
                                     Thread.sleep(100);
                                 }
                                 return null;
                             }
 
                             @Override
                             protected void done() {
                                 treeExploreThread = new TreeExploreThread(selectedNode, defaultTreeModel);
                                 CismetThreadPool.execute(treeExploreThread);
                             }
                         }.execute();
                 } else {
                     try {
                         statusChangeSupport.fireStatusChange(
                             org.openide.util.NbBundle.getMessage(
                                 MetaCatalogueTree.class,
                                 "MetaCatalogueTree.treeExpanded().loadingObjects"), // NOI18N
                             Status.MESSAGE_POSITION_1,
                             Status.ICON_IGNORE,
                             Status.ICON_BLINKING);
                         selectedNode.explore();
                         defaultTreeModel.nodeStructureChanged(selectedNode);
                         statusChangeSupport.fireStatusChange(
                             org.openide.util.NbBundle.getMessage(
                                 MetaCatalogueTree.class,
                                 "MetaCatalogueTree.treeExpanded().dataLoadedFromServer"), // NOI18N
                             Status.MESSAGE_POSITION_1,
                             Status.ICON_ACTIVATED,
                             Status.ICON_DEACTIVATED);
                     } catch (Exception exp) {
                         statusChangeSupport.fireStatusChange(
                             org.openide.util.NbBundle.getMessage(
                                 MetaCatalogueTree.class,
                                 "MetaCatalogueTree.treeExpanded().loadingError"), // NOI18N
                             Status.MESSAGE_POSITION_1,
                             Status.ICON_DEACTIVATED,
                             Status.ICON_ACTIVATED);
                         logger.fatal("treeExpanded() could not load nodes", exp); // NOI18N
                         selectedNode.removeChildren();
                     }
                 }
             } else {
                 if (logger.isDebugEnabled()) {
                     logger.debug("treeExpanded() " + selectedNode.getNode() + "'s children loaded from cache"); // NOI18N
                 }
                 statusChangeSupport.fireStatusChange(
                     org.openide.util.NbBundle.getMessage(
                         MetaCatalogueTree.class,
                         "MetaCatalogueTree.treeExpanded().dataLoadedFromCache"), // NOI18N
                     Status.MESSAGE_POSITION_1,
                     Status.ICON_ACTIVATED,
                     Status.ICON_DEACTIVATED);
             }
         }
     }
 
     /**
      * Der <b>TreeExploreThread</b> sorgt dafuer, dass die TreeNodes im Hintergrund geladen werden. Innerhalb des
      * Threads wird die <b>explore()</b> Funktion der selektierten DefaultMetaTreeNode ausgefuehrt. Waehrend des
      * Ladevorgangs der Daten vom Server wird der TreeView nicht aktualisiert, da ansonsten das GUI w\u00E4hrend dieser
      * Zeit komplett blockiert wuerde (kein repaint() meher!). Deshalb geschieht die eigentlich Aktualisierung des Views
      * asynchron zum <b>EventDispatchThread</b>.<br>
      * Das bedeutet, dass das GUI nicht innerhalb des EventDispatchThreads (in diesem Fall der <b>
      * TreeExpansionEvent</b>) oder aus einem aus ihm heraus gestarteten Thread (z.B. der TreeExploreThread)
      * aktuakisiert wird. Die Aktualisierung erfolgt durch <b>nodeStructureChanged(node)</b>. Diese Funktion wird in
      * einem weiteren Thread innerhalb des TreeExploreThreads ausgefuehrt, der durch <b>
      * SwingUtilities.invokeLater(runnable)</b> erst gestartet wird, wenn der <b>TreeExpansionEvent</b> bzw. der <b>
      * TreeExploreThread</b> beendet
      *
      * @version  $Revision$, $Date$
      */
     private class TreeExploreThread extends Thread {
 
         //~ Instance fields ----------------------------------------------------
 
         private Runnable treeUpdateThread = null;
         private final DefaultMetaTreeNode node;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new TreeExploreThread object.
          *
          * @param  selectedNode      DOCUMENT ME!
          * @param  defaultTreeModel  DOCUMENT ME!
          */
         public TreeExploreThread(final DefaultMetaTreeNode selectedNode, final DefaultTreeModel defaultTreeModel) {
             if (logger.isDebugEnabled()) {
                 logger.debug("<THREAD>: TreeExploreThread"); // NOI18N
             }
             threadCount++;
             node = selectedNode;
 
             treeUpdateThread = new Runnable() {
 
                     @Override
                     public void run() {
                         MetaCatalogueTree.this.defaultTreeModel.nodeStructureChanged(node);
                         if (logger.isDebugEnabled()) {
                             logger.debug("<THREAD>: TreeExploreThread GUI update done"); // NOI18N
                         }
                     }
                 };
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void run() {
             try {
                 statusChangeSupport.fireStatusChange(
                     org.openide.util.NbBundle.getMessage(
                         MetaCatalogueTree.class,
                         "MetaCatalogueTree.TreeExploreThread.loadingObjects"), // NOI18N
                     Status.MESSAGE_POSITION_1,
                     Status.ICON_BLINKING,
                     Status.ICON_IGNORE);
                 node.explore();
 
                 // logger.fatal("/////////////HELL Hier gehts weiter");
                 final Thread t = new Thread(new Runnable() {
 
                             @Override
                             public void run() {
                                 for (int i = 0; i < defaultTreeModel.getChildCount(node); ++i) {
 //                                    try {
 //                                        Thread.sleep(100);
 //                                    } catch (InterruptedException ex) {
 //                                        ex.printStackTrace();
 //                                    }
                                     try {
                                         final DefaultMetaTreeNode n = (DefaultMetaTreeNode)defaultTreeModel.getChild(
                                                 node,
                                                 i);
                                         // logger.fatal("HELL: "+n); if (n != null && n.getNode() != null &&
                                         // n.getNode().getName() == null && n.isObjectNode()) { //logger.fatal("HELL: in
                                         // if"); try { final ObjectTreeNode on = ((ObjectTreeNode) n);
                                         // EventQueue.invokeLater(new Runnable() {
                                         //
                                         // public void run() { n.getNode().setName("Name wird geladen .....");
                                         // defaultTreeModel.nodeChanged(on); } }); if (logger.isDebugEnabled()) {
                                         // logger.debug("caching object node"); } final MetaObject metaObject =
                                         // SessionManager.getProxy().getMetaObject(on.getMetaObjectNode().getObjectId(),
                                         // on.getMetaObjectNode().getClassId(), on.getMetaObjectNode().getDomain());
                                         // on.getMetaObjectNode().setObject(metaObject); EventQueue.invokeLater(new
                                         // Runnable() {
                                         //
                                         // public void run() { logger.debug("setze den namen des "+n+" mit null als name
                                         // auf:" + metaObject.toString());
                                         // n.getNode().setName(metaObject.toString());
                                         // defaultTreeModel.nodeChanged(on); } });
                                         //
                                         // } catch (Throwable t) { logger.error("could not retrieve meta object of node
                                         // '" + this + "'", t); } } else { logger.debug("n.getNode().getName()!=null:
                                         // " + n.getNode().getName() + ":"); }
                                     } catch (Exception e) {
                                         logger.error("Error while loading name", e); // NOI18N
                                     }
                                 }
                             }
                         });
                 CismetThreadPool.execute(t);
 
                 // GUI Update asynchron zum EventDispatchThread
                 SwingUtilities.invokeLater(treeUpdateThread);
                 // SwingUtilities.invokeAndWait(runnable);
                 statusChangeSupport.fireStatusChange(
                     org.openide.util.NbBundle.getMessage(
                         MetaCatalogueTree.class,
                         "MetaCatalogueTree.TreeExploreThread.dataLoadedFromServer"), // NOI18N
                     Status.MESSAGE_POSITION_1,
                     Status.ICON_ACTIVATED,
                     Status.ICON_DEACTIVATED);
                 threadCount--;
             } catch (Exception exp) {
                 logger.fatal("could not load nodes", exp); // NOI18N
                 statusChangeSupport.fireStatusChange(
                     org.openide.util.NbBundle.getMessage(
                         MetaCatalogueTree.class,
                         "MetaCatalogueTree.TreeExploreThread.loadingError"), // NOI18N
                     Status.MESSAGE_POSITION_1,
                     Status.ICON_DEACTIVATED,
                     Status.ICON_ACTIVATED);
                 node.removeChildren();
                 SwingUtilities.invokeLater(treeUpdateThread);
                 threadCount--;
             }
 
             if (logger.isDebugEnabled()) {
                 logger.debug("<THREAD> TreeExploreThread done"); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class SubTreeExploreThread extends Thread {
 
         //~ Instance fields ----------------------------------------------------
 
         private final DefaultMetaTreeNode node;
         private final Iterator<DefaultMetaTreeNode> childrenNodes;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new SubTreeExploreThread object.
          *
          * @param  rootNode       DOCUMENT ME!
          * @param  childrenNodes  DOCUMENT ME!
          */
         public SubTreeExploreThread(final DefaultMetaTreeNode rootNode,
                 final Iterator<DefaultMetaTreeNode> childrenNodes) {
             this.node = rootNode;
             this.childrenNodes = childrenNodes;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void run() {
             try {
                 statusChangeSupport.fireStatusChange(
                     org.openide.util.NbBundle.getMessage(
                         MetaCatalogueTree.class,
                         "MetaCatalogueTree.SubTreeExploreThread.loadingObjects"), // NOI18N
                     Status.MESSAGE_POSITION_1,
                     Status.ICON_BLINKING,
                     Status.ICON_IGNORE);
 
                 final TreePath selectionPath = this.node.explore(this.childrenNodes);
 
                 final TreeUpdateThread treeUpdateThread = new TreeUpdateThread(selectionPath);
 
                 SwingUtilities.invokeLater(treeUpdateThread);
 
                 statusChangeSupport.fireStatusChange(
                     org.openide.util.NbBundle.getMessage(
                         MetaCatalogueTree.class,
                         "MetaCatalogueTree.SubTreeExploreThread.dataLoadedFromServer"), // NOI18N
                     Status.MESSAGE_POSITION_1,
                     Status.ICON_ACTIVATED,
                     Status.ICON_DEACTIVATED);
             } catch (Exception exp) {
                 logger.fatal("SubTreeExploreThread: could not load nodes", exp); // NOI18N
                 statusChangeSupport.fireStatusChange(
                     org.openide.util.NbBundle.getMessage(
                         MetaCatalogueTree.class,
                         "MetaCatalogueTree.SubTreeExploreThread.loadingError"), // NOI18N
                     Status.MESSAGE_POSITION_1,
                     Status.ICON_DEACTIVATED,
                     Status.ICON_ACTIVATED);
 
                 node.removeChildren();
                 MetaCatalogueTree.this.defaultTreeModel.nodeStructureChanged(node);
             }
 
             if (logger.isDebugEnabled()) {
                 logger.debug("SubTreeExploreThread: done"); // NOI18N
             }
         }
 
         //~ Inner Classes ------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @version  $Revision$, $Date$
          */
         private class TreeUpdateThread implements Runnable {
 
             //~ Instance fields ------------------------------------------------
 
             private final TreePath selectionPath;
 
             //~ Constructors ---------------------------------------------------
 
             /**
              * Creates a new TreeUpdateThread object.
              *
              * @param  selectionPath  DOCUMENT ME!
              */
             private TreeUpdateThread(final TreePath selectionPath) {
                 this.selectionPath = selectionPath;
             }
 
             //~ Methods --------------------------------------------------------
 
             @Override
             public void run() {
                 MetaCatalogueTree.this.defaultTreeModel.nodeStructureChanged(node);
                 MetaCatalogueTree.this.setSelectionPath(this.selectionPath);
 //                Object o = selectionPath.getLastPathComponent();
 //                MetaCatalogueTree.this.expandPath(selectionPath);
                 MetaCatalogueTree.this.scrollPathToVisible(selectionPath);
 
                 if (logger.isDebugEnabled()) {
                     logger.debug("SubTreeExploreThread: GUI Update done"); // NOI18N
                 }
             }
         }
     }
 }
