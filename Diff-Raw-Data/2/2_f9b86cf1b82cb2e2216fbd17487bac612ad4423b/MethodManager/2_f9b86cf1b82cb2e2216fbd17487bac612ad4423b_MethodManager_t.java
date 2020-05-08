 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.method;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 import Sirius.navigator.exception.ExceptionManager;
 import Sirius.navigator.tools.CloneHelper;
 import Sirius.navigator.types.iterator.TreeNodeIterator;
 import Sirius.navigator.types.iterator.TreeNodeRestriction;
 import Sirius.navigator.types.treenode.ClassTreeNode;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.navigator.ui.dialog.AboutDialog;
 import Sirius.navigator.ui.tree.MetaCatalogueTree;
 import Sirius.navigator.ui.tree.SearchResultsTree;
 
 import Sirius.server.localserver.method.Method;
 import Sirius.server.middleware.types.Link;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.permission.Permission;
 
 import org.apache.log4j.Logger;
 
 import java.beans.PropertyChangeListener;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 import de.cismet.lookupoptions.gui.OptionsDialog;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public class MethodManager {
 
     //~ Static fields/initializers ---------------------------------------------
 
     // availability
     public static final long NONE = 0;
     public static final long PURE_NODE = 1;
     public static final long CLASS_NODE = 2;
     public static final long OBJECT_NODE = 4;
     public static final long MULTIPLE = 8;
     public static final long CLASS_MULTIPLE = 16;
 
     private static final Logger logger = Logger.getLogger(MethodManager.class);
 
     private static MethodManager manager = null;
     private static final Object blocker = new Object();
 
     //~ Instance fields --------------------------------------------------------
 
     protected MetaClass[] classArray = null;
     protected MetaObject[] objectArray = null;
     protected Vector methodVector = new Vector(5, 1);
 
     protected boolean wrongParameters = false;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MethodManager object.
      */
     private MethodManager() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static final MethodManager getManager() {
         synchronized (blocker) {
             if (manager == null) {
                 manager = new MethodManager();
             }
 
             return manager;
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public static final void destroy() {
         synchronized (blocker) {
             logger.warn("destroying singelton MethodManager instance"); // NOI18N
             manager = null;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   methodID  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected Method getMethod(final String methodID) {
         Method tmpMethod = null;
 
         for (int i = 0; i < methodVector.size(); i++) {
             tmpMethod = (Method)methodVector.elementAt(i);
             if (String.valueOf(tmpMethod.getID()).equals(methodID)) {
                 return tmpMethod;
             }
         }
 
         return null;
     }
 
     /**
      * DOCUMENT ME!
      */
     public void callSpecialTreeCommand() {
         if (ComponentRegistry.getRegistry().getActiveCatalogue() instanceof SearchResultsTree) {
             if (
                 !((SearchResultsTree)ComponentRegistry.getRegistry().getActiveCatalogue()).removeResultNodes(
                             ComponentRegistry.getRegistry().getActiveCatalogue().getSelectedNodes())) {
                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                     org.openide.util.NbBundle.getMessage(
                         MethodManager.class,
                         "MethodManager.callSpecialTreeCommand().JOptionPane_anon1.message"), // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         MethodManager.class,
                         "MethodManager.callSpecialTreeCommand().JOptionPane_anon1.title"), // NOI18N
                     JOptionPane.INFORMATION_MESSAGE);
             }
         } else if (ComponentRegistry.getRegistry().getActiveCatalogue() instanceof MetaCatalogueTree) {
             final DefaultMetaTreeNode[] selectedTreeNodes = ComponentRegistry.getRegistry()
                         .getActiveCatalogue()
                         .getSelectedNodesArray();
 
             if ((selectedTreeNodes != null) && (selectedTreeNodes.length > 0)) {
                 final Node[] selectedNodes = new Node[selectedTreeNodes.length];
 
                 for (int i = 0; i < selectedTreeNodes.length; i++) {
                     selectedNodes[i] = selectedTreeNodes[i].getNode();
                 }
 
                 ComponentRegistry.getRegistry().getSearchResultsTree().setResultNodes(selectedNodes, true, null);
             } else {
                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                     org.openide.util.NbBundle.getMessage(
                         MethodManager.class,
                         "MethodManager.callSpecialTreeCommand().JOptionPane_anon2.message"), // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         MethodManager.class,
                         "MethodManager.callSpecialTreeCommand().JOptionPane_anon2.title"), // NOI18N
                     JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void showSearchResults() {
         ComponentRegistry.getRegistry().getGUIContainer().select(ComponentRegistry.SEARCHRESULTS_TREE);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void showAboutDialog() {
         final AboutDialog aboutDialog = ComponentRegistry.getRegistry().getAboutDialog();
         aboutDialog.pack();
         aboutDialog.setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
         aboutDialog.show();
     }
 
     /**
      * Shows the options dialog.
      */
     public void showOptionsDialog() {
         final OptionsDialog optionsDialog = ComponentRegistry.getRegistry().getOptionsDialog();
         optionsDialog.pack();
         optionsDialog.setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
         optionsDialog.setVisible(true);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void showQueryResultProfileManager() {
         ComponentRegistry.getRegistry()
                 .getQueryResultProfileManager()
                 .setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
         ComponentRegistry.getRegistry().getQueryResultProfileManager().show();
     }
 
     /**
      * DOCUMENT ME!
      */
     public void showPasswordDialog() {
         ComponentRegistry.getRegistry()
                 .getPasswordDialog()
                 .setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
         ComponentRegistry.getRegistry().getPasswordDialog().show();
     }
 
     /**
      * DOCUMENT ME!
      */
     public void showPluginManager() {
         ComponentRegistry.getRegistry()
                 .getPluginManager()
                 .setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
         ComponentRegistry.getRegistry().getPluginManager().show();
     }
 
     /**
      * DOCUMENT ME!
      */
     public void showSearchDialog() // throws Exception
     {
         // this.showSearchDialog(false);
 
         ComponentRegistry.getRegistry().getSearchDialog().pack();
         ComponentRegistry.getRegistry()
                 .getSearchDialog()
                 .setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
         ComponentRegistry.getRegistry().getSearchDialog().show();
     }
 
     /**
      * DOCUMENT ME!
      */
     public void showQueryProfilesManager() // throws Exception
     {
         // this.showSearchDialog(false);
 
         ComponentRegistry.getRegistry().getSearchDialog().pack();
         ComponentRegistry.getRegistry()
                 .getSearchDialog()
                 .setLocationRelativeTo(ComponentRegistry.getRegistry().getMainWindow());
         ComponentRegistry.getRegistry().getSearchDialog().showQueryProfilesManager();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public MethodAvailability getMethodAvailability() {
         long availability = NONE;
         final Collection nodes = ComponentRegistry.getRegistry().getActiveCatalogue().getSelectedNodes();
         final HashSet classKeys = new HashSet();
         int i = 0;
 
         try {
             if ((nodes != null) && (nodes.size() > 0)) {
                 final TreeNodeIterator iterator = new TreeNodeIterator(nodes, new TreeNodeRestriction());
                 while (iterator.hasNext()) {
                     final DefaultMetaTreeNode node = iterator.next();
 
                     if (node.isPureNode() && ((PURE_NODE & availability) == 0)) {
                         availability += PURE_NODE;
                     } else if (node.isClassNode() && ((CLASS_NODE & availability) == 0)) {
                         availability += CLASS_NODE;
                         classKeys.add(((ClassTreeNode)node).getKey());
                     } else if (node.isObjectNode() && ((OBJECT_NODE & availability) == 0)) {
                         availability += OBJECT_NODE;
                         classKeys.add(((ObjectTreeNode)node).getMetaClass().getKey());
                     }
                 }
 
                 i++;
             }
         } catch (Throwable t) {
             logger.error("getAvailability() could not comute availabilty", t); // NOI18N
             availability = 0;
         }
 
         if (i > 0) {
             availability += MULTIPLE;
         }
 
         if (classKeys.size() > 1) {
             availability += CLASS_MULTIPLE;
         }
 
         return new MethodAvailability(classKeys, availability);
     }

     /**
      * DOCUMENT ME!
      *
      * @param  resultNodes  DOCUMENT ME!
      * @param  append       DOCUMENT ME!
      */
     public void showSearchResults(final Node[] resultNodes,
             final boolean append) {
         showSearchResults(resultNodes, append, null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  resultNodes  DOCUMENT ME!
      * @param  append       DOCUMENT ME!
      * @param  listener     DOCUMENT ME!
      */
     public void showSearchResults(final Node[] resultNodes,
             final boolean append,
             final PropertyChangeListener listener) {
         if ((resultNodes == null) || (resultNodes.length < 1)) {
             JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getSearchDialog(),
                 org.openide.util.NbBundle.getMessage(
                     MethodManager.class,
                     "MethodManager.showSearchResults(Node[],boolean).JOptionPane_anon.message"), // NOI18N
                 org.openide.util.NbBundle.getMessage(
                     MethodManager.class,
                     "MethodManager.showSearchResults(Node[],boolean).JOptionPane_anon.title"), // NOI18N
                 JOptionPane.WARNING_MESSAGE);
         } else {
             ComponentRegistry.getRegistry().getSearchResultsTree().setResultNodes(resultNodes, append, listener);
             this.showSearchResults();
         }
     }
 
     // Tree Operationen ........................................................
 
     /**
      * destinationNode = parentNode.
      *
      * @param   metaTree         DOCUMENT ME!
      * @param   destinationNode  DOCUMENT ME!
      * @param   sourceNode       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean updateNode(final MetaCatalogueTree metaTree,
             final DefaultMetaTreeNode destinationNode,
             final DefaultMetaTreeNode sourceNode) {
         try {
             if (logger.isInfoEnabled()) {
                 logger.info("updateNode() updating node " + sourceNode); // NOI18N
             }
             // zuerst l\u00F6schen
             SessionManager.getProxy().deleteNode(sourceNode.getNode());
 
             // dann neu einf\u00FCgen
             this.addNode(metaTree, destinationNode, sourceNode);
 
             return true;
         } catch (Exception exp) {
             logger.error("deleteNode() could not update node " + sourceNode, exp); // NOI18N
             // XXX i18n
             ExceptionManager.getManager()
                     .showExceptionDialog(
                         ExceptionManager.WARNING,
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.updateNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).ExceptionManager_anon.title"), // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.updateNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).ExceptionManager_anon.message",
                             sourceNode), // NOI18N
                         exp);
         }
 
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaTree    DOCUMENT ME!
      * @param   sourceNode  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean deleteNode(final MetaCatalogueTree metaTree, final DefaultMetaTreeNode sourceNode) {
         if (JOptionPane.YES_NO_OPTION
                     == JOptionPane.showOptionDialog(
                         ComponentRegistry.getRegistry().getMainWindow(),
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.deleteNode(MetaCatalogueTree,DefaultMetaTreeNode).JOptionPane_anon.message",
                             new Object[] { String.valueOf(sourceNode) }),                                              // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.deleteNode(MetaCatalogueTree,DefaultMetaTreeNode).JOptionPane_anon.title"), // NOI18N
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE,
                         null,
                         new String[] {
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.deleteNode(MetaCatalogueTree,DefaultMetaTreeNode).JOptionPane_anon.option.commit"), // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.deleteNode(MetaCatalogueTree,DefaultMetaTreeNode).JOptionPane_anon.option.cancel")
                         },                                                                                             // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.deleteNode(MetaCatalogueTree,DefaultMetaTreeNode).JOptionPane_anon.option.commit"))) // NOI18N
         {
             try {
                 if (logger.isInfoEnabled()) {
                     logger.info("deleteNode() deleting node " + sourceNode);                                           // NOI18N
                 }
 
                 ComponentRegistry.getRegistry()
                         .getMainWindow()
                         .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
                 SessionManager.getProxy().deleteNode(sourceNode.getNode());
                 if (sourceNode.isObjectNode()) {
                     if (logger.isDebugEnabled()) {
                         logger.debug("deleting object node's meta object"); // NOI18N
                     }
                     final MetaObject MetaObject = ((ObjectTreeNode)sourceNode).getMetaObject();
                     SessionManager.getProxy().deleteMetaObject(MetaObject, MetaObject.getDomain());
                 }
                 ComponentRegistry.getRegistry()
                         .getMainWindow()
                         .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
 
                 this.deleteTreeNode(metaTree, sourceNode);
                 return true;
             } catch (Exception exp) {
                 logger.error("deleteNode() could not delete node " + sourceNode, exp); // NOI18N
                 ComponentRegistry.getRegistry()
                         .getMainWindow()
                         .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
 
                 ExceptionManager.getManager()
                         .showExceptionDialog(
                             ExceptionManager.WARNING,
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.deleteNode(MetaCatalogueTree,DefaultMetaTreeNode).ExceptionManager_anon.title"), // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.deleteNode(MetaCatalogueTree,DefaultMetaTreeNode).ExceptionManager_anon.message",
                                 sourceNode), // NOI18N
                             exp);
             }
         }
 
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaTree         DOCUMENT ME!
      * @param   destinationNode  DOCUMENT ME!
      * @param   sourceNode       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean addNode(final MetaCatalogueTree metaTree,
             final DefaultMetaTreeNode destinationNode,
             final DefaultMetaTreeNode sourceNode) {
         return this.addOrLinkNode(metaTree, destinationNode, sourceNode, false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaTree         DOCUMENT ME!
      * @param   destinationNode  DOCUMENT ME!
      * @param   sourceNode       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean copyNode(final MetaCatalogueTree metaTree,
             final DefaultMetaTreeNode destinationNode,
             final DefaultMetaTreeNode sourceNode) {
         if (logger.isInfoEnabled()) {
             logger.info("copy node " + sourceNode + " -> " + destinationNode);                                                                       // NOI18N
         }
         if (JOptionPane.YES_NO_OPTION
                     == JOptionPane.showOptionDialog(
                         ComponentRegistry.getRegistry().getMainWindow(),
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.copyNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.message",
                             sourceNode,
                             destinationNode),                                                                                                        // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.copyNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.title"),             // NOI18N
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE,
                         null,
                         new String[] {
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.copyNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.option.commit"), // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.copyNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.option.cancel")
                         },                                                                                                                           // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.copyNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.option.commit")))    // NOI18N
         {
             try {
                 // copy node
                 final DefaultMetaTreeNode sourceNodeCopy = (DefaultMetaTreeNode)CloneHelper.clone(sourceNode);
 
                 if (sourceNode instanceof ObjectTreeNode) {
                     final MetaObject oldMetaObject = ((ObjectTreeNode)sourceNodeCopy).getMetaObject();
                     // oldMetaObject.setPrimaryKey(new Integer(-1));
                     oldMetaObject.setPrimaryKeysNull();
 
                     if (logger.isInfoEnabled()) {
                         logger.info("copy node(): copy meta object: " + oldMetaObject.getName()); // NOI18N
                     }
                     final MetaObject newMetaObject = SessionManager.getProxy()
                                 .insertMetaObject(oldMetaObject, sourceNodeCopy.getDomain());
 
                     // neues objekt zuweisen
                     ((ObjectTreeNode)sourceNodeCopy).setMetaObject(newMetaObject);
                 }
 
                 if (this.addNode(metaTree, destinationNode, sourceNodeCopy)) {
                     this.addTreeNode(metaTree, destinationNode, sourceNodeCopy);
                     return true;
                 }
             } catch (Exception exp) {
                 logger.error("could not create copy of node " + sourceNode, exp);                                                                 // NOI18N
                 ExceptionManager.getManager()
                         .showExceptionDialog(
                             ExceptionManager.WARNING,
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.copyNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).ExceptionManager_anon.title"), // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.copyNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).ExceptionManager_anon.message",
                                 sourceNode),                                                                                                      // NOI18N
                             exp);
             }
         }
 
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaTree         DOCUMENT ME!
      * @param   destinationNode  DOCUMENT ME!
      * @param   sourceNode       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean moveNode(final MetaCatalogueTree metaTree,
             final DefaultMetaTreeNode destinationNode,
             final DefaultMetaTreeNode sourceNode) {
         if (logger.isInfoEnabled()) {
             logger.info("move node " + sourceNode + " -> " + destinationNode);                                                                       // NOI18N
         }
         if (JOptionPane.YES_NO_OPTION
                     == JOptionPane.showOptionDialog(
                         ComponentRegistry.getRegistry().getMainWindow(),
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.moveNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.message",
                             sourceNode,
                             destinationNode),                                                                                                        // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.moveNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.title"),             // NOI18N
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE,
                         null,
                         new String[] {
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.moveNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.option.commit"), // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.moveNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.option.cancel")
                         },                                                                                                                           // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.moveNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.option.commit")))    // NOI18N
         {
             try {
                 final DefaultMetaTreeNode sourceParentNode = (DefaultMetaTreeNode)sourceNode.getParent();
 
                 ComponentRegistry.getRegistry()
                         .getMainWindow()
                         .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
                 SessionManager.getProxy().deleteLink(sourceParentNode.getNode(), sourceNode.getNode());
                 this.deleteTreeNode(metaTree, sourceNode);
 
                 SessionManager.getProxy().addLink(destinationNode.getNode(), sourceNode.getNode());
                 this.addTreeNode(metaTree, destinationNode, sourceNode);
                 // destinationNode.explore();
                 ComponentRegistry.getRegistry()
                         .getMainWindow()
                         .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
             } catch (Exception exp) {
                 logger.error("addNode() could not add node"); // NOI18N
                 ComponentRegistry.getRegistry()
                         .getMainWindow()
                         .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
 
                 ExceptionManager.getManager()
                         .showExceptionDialog(
                             ExceptionManager.WARNING,
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.moveNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).ExceptionManager_anon.title"), // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.moveNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).ExceptionManager_anon.message",
                                 sourceNode), // NOI18N
                             exp);
             }
         }
 
         return false;
     }
     /**
      * TreeNode Merhoden.
      *
      * @param  metaTree         DOCUMENT ME!
      * @param  destinationNode  DOCUMENT ME!
      * @param  sourceNode       DOCUMENT ME!
      */
     public void addTreeNode(final MetaCatalogueTree metaTree,
             final DefaultMetaTreeNode destinationNode,
             final DefaultMetaTreeNode sourceNode) {
         if (destinationNode.isLeaf()) {
             if (logger.isDebugEnabled()) {
                 logger.debug("addTreeNode() destinationNode " + destinationNode + " is leaf"); // NOI18N
             }
             destinationNode.setLeaf(false);
         }
 
         // int childCount = destinationNode.getChildCount();
         destinationNode.add(sourceNode);
 
         final int pos = destinationNode.getIndex(sourceNode);
 
         ((DefaultTreeModel)metaTree.getModel()).nodesWereInserted(destinationNode, new int[] { pos });
 
         // aufklappen ...
         destinationNode.setExplored(true);
         metaTree.setSelectionPath(new TreePath(sourceNode.getPath()));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  metaTree    DOCUMENT ME!
      * @param  sourceNode  DOCUMENT ME!
      */
     public void deleteTreeNode(final MetaCatalogueTree metaTree, final DefaultMetaTreeNode sourceNode) {
         final DefaultMetaTreeNode sourceParentNode = (DefaultMetaTreeNode)sourceNode.getParent();
         final Object[] removedChildren = new Object[] { sourceNode };
 
         if (logger.isDebugEnabled()) {
             logger.debug("removing child node '" + sourceNode + "' from parent node '" + sourceParentNode + "'"); // NOI18N
         }
         final int[] childIndices = this.getChildIndices(sourceParentNode, sourceNode);
         sourceNode.removeFromParent();
 
         ((DefaultTreeModel)metaTree.getModel()).nodesWereRemoved(sourceParentNode, childIndices, removedChildren);
     }
 
     /**
      * Hilfsmethoden ...........................................................
      *
      * @param   metaTree         DOCUMENT ME!
      * @param   destinationNode  DOCUMENT ME!
      * @param   sourceNode       DOCUMENT ME!
      * @param   linkOnly         DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean addOrLinkNode(final MetaCatalogueTree metaTree,
             final DefaultMetaTreeNode destinationNode,
             final DefaultMetaTreeNode sourceNode,
             final boolean linkOnly) {
         try {
             if (linkOnly) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("addOrLinkNode(): linking  node: " + sourceNode); // NOI18N
                 }
 
                 ComponentRegistry.getRegistry()
                         .getMainWindow()
                         .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
                 SessionManager.getProxy().addLink(destinationNode.getNode(), sourceNode.getNode());
                 ComponentRegistry.getRegistry()
                         .getMainWindow()
                         .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
             } else {
                 Link link;
                 if (destinationNode != null) {
                     link = new Link(destinationNode.getID(), destinationNode.getDomain());
                 } else {
                     logger.warn("addNode(): node '" + sourceNode + "' has no parent node'"); // NOI18N
                     link = new Link(-1, sourceNode.getDomain());
                 }
 
                 if (logger.isDebugEnabled()) {
                     logger.debug("addOrLinkNode(): adding node: " + sourceNode); // NOI18N
                 }
 
                 ComponentRegistry.getRegistry()
                         .getMainWindow()
                         .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
                 final Node node = SessionManager.getProxy().addNode(sourceNode.getNode(), link);
                 node.setPermissions(destinationNode.getNode().getPermissions());
                 ComponentRegistry.getRegistry()
                         .getMainWindow()
                         .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
                 sourceNode.setNode(node);
             }
 
             // this.addTreeNode(metaTree, destinationNode, sourceNode);
             return true;
         } catch (ConnectionException cexp) {
             logger.error("addOrLinkNode() could not add node " + sourceNode, cexp); // NOI18N
             ComponentRegistry.getRegistry()
                     .getMainWindow()
                     .setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
 
             ExceptionManager.getManager()
                     .showExceptionDialog(
                         ExceptionManager.WARNING,
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.addOrLinkNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode,boolean).ExceptionManager_anon.title"), // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.addOrLinkNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode,boolean).ExceptionManager_anon.message",
                             sourceNode), // NOI18N
                         cexp);
         }
 
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaTree         DOCUMENT ME!
      * @param   destinationNode  DOCUMENT ME!
      * @param   sourceNode       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean linkNode(final MetaCatalogueTree metaTree,
             final DefaultMetaTreeNode destinationNode,
             final DefaultMetaTreeNode sourceNode) {
         if (logger.isInfoEnabled()) {
             logger.info("link node " + sourceNode + " -> " + destinationNode);                                                                       // NOI18N
         }
         if (JOptionPane.YES_NO_OPTION
                     == JOptionPane.showOptionDialog(
                         ComponentRegistry.getRegistry().getMainWindow(),
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.linkNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.message",
                             sourceNode,
                             destinationNode),                                                                                                        // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.linkNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.title"),             // NOI18N
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE,
                         null,
                         new String[] {
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.linkNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.option.commit"), // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.linkNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.option.cancel")
                         },                                                                                                                           // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MethodManager.class,
                             "MethodManager.linkNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).JOptionPane_anon.option.commit")))    // NOI18N
         {
             try {
                 // copy node
                 final DefaultMetaTreeNode sourceNodeCopy = (DefaultMetaTreeNode)CloneHelper.clone(sourceNode);
                 if (this.addOrLinkNode(metaTree, destinationNode, sourceNodeCopy, true)) {
                     this.addTreeNode(metaTree, destinationNode, sourceNodeCopy);
                     return true;
                 }
             } catch (CloneNotSupportedException cnse) {
                 logger.error("could not create copy of linked node " + sourceNode, cnse);                                                         // NOI18N
                 ExceptionManager.getManager()
                         .showExceptionDialog(
                             ExceptionManager.WARNING,
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.linkNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).ExceptionManager_anon.title"), // NOI18N
                             org.openide.util.NbBundle.getMessage(
                                 MethodManager.class,
                                 "MethodManager.linkNode(MetaCatalogueTree,DefaultMetaTreeNode,DefaultMetaTreeNode).ExceptionManager_anon.message",
                                 sourceNode),                                                                                                      // NOI18N
                             cnse);
             }
         }
 
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   sourceParentNode  DOCUMENT ME!
      * @param   sourceNode        DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private int[] getChildIndices(final TreeNode sourceParentNode, final TreeNode sourceNode) {
         for (int i = 0; i < sourceParentNode.getChildCount(); i++) {
             if (sourceParentNode.getChildAt(i).equals(sourceNode)) {
                 return new int[] { i };
             }
         }
 
         logger.warn("getChildIndices() child index of node " + sourceNode + " not found in parent node "
                     + sourceParentNode); // NOI18N
         return new int[] { -1 };
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   node        DOCUMENT ME!
      * @param   permission  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean checkPermission(final Node node, final Permission permission) {
         boolean hasPermission = false;
 
         try {
             final String key = SessionManager.getSession().getUser().getUserGroup().getKey().toString();
             hasPermission = node.getPermissions().hasPermission(key, permission);
 
             if (logger.isDebugEnabled()) {
                 logger.debug("Permissions for node" + node + "   " + node.getPermissions() + "  with key" + key); // NOI18N
             }
         } catch (Exception exp) {
             logger.error("checkPermission(): could not check permission '" + permission + "' of node '" + node + "'",
                 exp);                                                                                             // NOI18N
             hasPermission = false;
         }
 
         if (!hasPermission) {
             JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                 org.openide.util.NbBundle.getMessage(
                     MethodManager.class,
                     "MethodManager.checkPermission(Node,Permission).JOptionPane_anon.message"), // NOI18N
                 org.openide.util.NbBundle.getMessage(
                     MethodManager.class,
                     "MethodManager.checkPermission(Node,Permission).JOptionPane_anon.title"), // NOI18N
                 JOptionPane.INFORMATION_MESSAGE);
         }
 
         return hasPermission;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   node        DOCUMENT ME!
      * @param   permission  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean checkPermission(final MetaObjectNode node, final Permission permission) {
         boolean hasPermission = false;
 
         try {
             final String key = SessionManager.getSession().getUser().getUserGroup().getKey().toString();
             final MetaClass c = SessionManager.getProxy().getMetaClass(node.getClassId(), node.getDomain());
 
             // wenn MON dann editieren wenn Rechte am Knoten und and der Klasse
             hasPermission = c.getPermissions().hasPermission(key, permission);
             hasPermission &= node.getPermissions().hasPermission(key, permission);
             // und am Objekt
             hasPermission &= node.getObject().getBean().hasObjectWritePermission(SessionManager.getSession().getUser());
 
             if (logger.isDebugEnabled()) {
                 logger.debug("Check ClassPermissions for node" + node + "   " + c.getPermissions() + "  with key"
                             + key); // NOI18N
             }
         } catch (Exception exp) {
             logger.error("checkPermission(): could not check permission '" + permission + "' of node '" + node + "'",
                 exp);               // NOI18N
             hasPermission = false;
         }
 
         if (!hasPermission) {
             JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                 org.openide.util.NbBundle.getMessage(
                     MethodManager.class,
                     "MethodManager.checkPermission(MetaObjectNode,Permission).JOptionPane_anon.message"), // NOI18N
                 org.openide.util.NbBundle.getMessage(
                     MethodManager.class,
                     "MethodManager.checkPermission(MetaObjectNode,Permission).JOptionPane_anon.title"), // NOI18N
                 JOptionPane.INFORMATION_MESSAGE);
         }
 
         return hasPermission;
     }
 
     /**
      * Durchsucht ein MetaObject nach leeren Attributen.
      *
      * @param   MetaObject  das MetaObject, dass durchsucht werden soll
      *
      * @return  der Name des leeren Attributs oder null falls kein leeres Attribut gefunden wurde
      */
     public String findEmptyAttributes(final MetaObject MetaObject) {
         return this.findEmptyAttributes(MetaObject.getAttributes().values().iterator());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   attributeIterator  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String findEmptyAttributes(final Iterator attributeIterator) {
         String attributeName = null;
 
         while (attributeIterator.hasNext() && (attributeName == null)) {
             final Sirius.server.localserver.attribute.Attribute attribute =
                 (Sirius.server.localserver.attribute.Attribute)attributeIterator.next();
             if (!attribute.isPrimaryKey() && attribute.isOptional()) {
                 if (attribute.getValue() != null) {
                     if (attribute.getValue() instanceof MetaObject) {
                         attributeName = this.findEmptyAttributes((MetaObject)attribute.getValue());
                     }
                 } else                          // if (attribute.referencesObject())/* if attribute.value == null*/
                 {
                     try {
                         if (logger.isDebugEnabled()) {
                             logger.debug("looking for default value for mandantory attribute '" + attribute.getName()
                                         + "'"); // NOI18N
                         }
 
                         // Woraround Anfang
                         if (attribute.isOptional()) {
                             if (logger.isDebugEnabled()) {
                                 logger.debug(attribute.getName() + "is optional. Set it to null"); // NOI18N
                             }
                             attribute.setValue(null);
                             attributeName = null;
                         }
                         // Workaround Ende
                     } catch (Exception exp) {
                         logger.error("could net set default value of attribute '" + attribute.getName() + "'", exp); // NOI18N
                         attributeName = attribute.getName();
                     }
                 }
             }
         }
 
         return attributeName;
     }
 }
