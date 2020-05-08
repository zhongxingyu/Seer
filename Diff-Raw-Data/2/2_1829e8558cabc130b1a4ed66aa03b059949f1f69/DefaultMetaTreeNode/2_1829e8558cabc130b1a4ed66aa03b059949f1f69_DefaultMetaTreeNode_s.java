 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.types.treenode;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.ui.tree.MetaCatalogueTree;
 
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.permission.Permission;
 
 import Sirius.util.NodeComparator;
 
 import org.apache.log4j.Logger;
 
 import java.util.Enumeration;
 import java.util.Iterator;
 
 import javax.swing.ImageIcon;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public abstract class DefaultMetaTreeNode extends DefaultMutableTreeNode // implements Comparable
 {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(DefaultMetaTreeNode.class);
 
     //~ Instance fields --------------------------------------------------------
 
     protected boolean explored = false;
     protected boolean selected = false;
     protected boolean enabled = true;
     /** Holds value of property changed. */
     private boolean changed;
     /** Holds value of property new_node. */
     private boolean new_node;
 
     private final transient NodeComparator nodeComparator;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Dieser Konstruktor erzeugt eine RootNode ohne Children. Er ist zu Verwendung im SearchTree gedacht, wenn noch
      * keine Suche durgefuehrt wurde und noch keine Knoten angezeigt werden koennen. Er sollte nicht angezeigt werden.
      * (JTree.setRootVisible(false);)
      *
      * @param  node  DOCUMENT ME!
      */
     public DefaultMetaTreeNode(final Node node) {
         super(node);
 
         nodeComparator = new NodeComparator();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  selected  DOCUMENT ME!
      */
     public void setSelected(final boolean selected) {
         this.selected = enabled & selected;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isSelected() {
         return this.selected;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  selected  DOCUMENT ME!
      */
     public void selectSubtree(final boolean selected) {
         final Enumeration enu = this.breadthFirstEnumeration();
 
         while (enu.hasMoreElements()) {
             ((DefaultMetaTreeNode)enu.nextElement()).setSelected(selected);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Node getNode() {
         if (this.userObject != null) {
             return (Node)this.userObject;
         }
 
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  node  DOCUMENT ME!
      */
     public void setNode(final Node node) {
         this.setUserObject(node);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  leaf  DOCUMENT ME!
      */
     public void setLeaf(final boolean leaf) {
         this.getNode().setLeaf(leaf);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public Node[] getChildren() throws Exception {
         final Node node = this.getNode();
 
         final Node[] c = SessionManager.getProxy().getChildren(node, SessionManager.getSession().getUser());
        if (node.isDynamic() && node.isSqlSort()) {
             return c;
         }
 
         return Sirius.navigator.tools.NodeSorter.sortNodes(c);
     }
 
     /**
      * Performs a hard refresh by removing all children, fetching them again from the database and then adding them.
      * Make sure to notify the tree model about the node change.<br/>
      * <br/>
      * <b>NOTE:</b>This operation is preferably used if the node's children are created dynamically and the SQL sort
      * property is true. If these conditions are not met consider to do a soft refresh.
      */
     public void refreshChildren() {
         try {
             final Node[] dbChildren = getChildren();
             removeAllChildren();
 
             for (final Node node : dbChildren) {
                 add(MetaCatalogueTree.createTreeNode(node));
             }
         } catch (final Exception e) {
             LOG.warn("cannot refresh node: " + this, e); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   toRemove  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int removeNode(final DefaultMetaTreeNode toRemove) {
         final int index = getIndex(toRemove);
 
         if (index >= 0) {
             remove(index);
         }
 
         return index;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   toAdd  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     public int insertNode(final DefaultMetaTreeNode toAdd) {
         final Node newNode = toAdd.getNode();
 
         assert newNode != null : "received DefaultMetaTreeNode without backing Node: " + toAdd; // NOI18N
 
         for (int i = 0; i < children.size(); ++i) {
             final TreeNode tn = getChildAt(i);
             if (tn instanceof DefaultMetaTreeNode) {
                 final DefaultMetaTreeNode dmtn = (DefaultMetaTreeNode)tn;
                 final Node child = dmtn.getNode();
 
                 assert child != null : "found DefaultMetaTreeNode without backing Node: " + dmtn; // NOI18N
 
                 if (nodeComparator.compare(child, newNode) > 0) {
                     insert(toAdd, i);
 
                     return i;
                 }
             } else {
                 throw new IllegalStateException("Illegal child: " + tn); // NOI18N
             }
         }
 
         // the node was not inserted, so we insert it at the end
         add(toAdd);
 
         return getChildCount() - 1;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  enabled  DOCUMENT ME!
      */
     public void setEnabled(final boolean enabled) {
         this.enabled = enabled;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isEnabled() {
         return this.enabled;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract TreeNodeLoader getTreeNodeLoader();
 
     /**
      * Gibt an, ober dieser Knoten bereits expandiert wurde, bzw. ob seine Children schon vom Server geladen wurden.
      *
      * @return  DOCUMENT ME!
      */
     public boolean isExplored() {
         return explored;
     }
 
     /**
      * Ueberschreibt die Funktion isLeaf() in MutableTreeNode.
      *
      * @return  true/false
      */
     @Override
     public boolean isLeaf() {
         return (this.getUserObject() != null) ? this.getNode().isLeaf() : true;
     }
 
     @Override
     public boolean getAllowsChildren() {
         return !this.isLeaf();
     }
 
     /**
      * Liefert eine String Repraesentation dieser TreeNode.
      *
      * @return  Der Name des userObjects.
      */
     @Override
     public abstract String toString();
 
     /**
      * Gibt an, ob diese TreeNode eine RootNode ist.<br>
      * Eine RootNode ist ein spezieller Typ von DefaultMetaTreeNode. Unter eine RootNode werden alle anderen Knoten
      * angehaengt. Pro MetaTree gibt es nur eine RootNode.
      *
      * @return  true/false
      */
     public abstract boolean isRootNode();
 
     /**
      * Gibt an, ob diese TreeNode eine WaitNode ist.<br>
      * Eine WaitNode ist ein spezieller Typ von DefaultMetaTreeNode. Die WaitNode wird dann angezeigt, wenn ein Knoten
      * innerhalb eines Threads expandiert wurde. Die WaitNode wird zwar direkt nach dem Aufruf der Methode getChildren
      * wieder entfernt, da aber der Thread den View des MetaTree erst wieder aktualisert wenn alle Children geladen
      * wurden (asynchrones Update), wird diese WaitNode solange angezeigt, wie der Thread l\u00E4uft. Wird der Knoten
      * nicht innerhalb eines Threads angezeigt, wird auch die WaitNode nicht angezeigt.
      *
      * @return  true/false
      */
     public abstract boolean isWaitNode();
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract boolean isPureNode();
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract boolean isClassNode();
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract boolean isObjectNode();
 
     /**
      * Expandiert diesen Knoten und uerberprueft, ob dessen Kinder bereits vom Server geladen wurden. Hat der Knoten
      * noch keine Kinder und ist er kein Blatt (!isLeaf()), werden die Children vom Server geladen.
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public abstract void explore() throws Exception;
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public void exploreAll() throws Exception {
         if (!this.isLeaf()) {
             if (LOG.isDebugEnabled()) {
                 LOG.warn("exploring all children of node '" + this + "'"); // NOI18N
             }
             if (!this.isExplored()) {
                 this.explore();
             }
 
             final Enumeration e = this.children();
             while (e.hasMoreElements()) {
                 ((DefaultMetaTreeNode)e.nextElement()).exploreAll();
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   childrenIterator  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public TreePath explore(final Iterator<DefaultMetaTreeNode> childrenIterator) throws Exception {
         if (childrenIterator.hasNext()) {
             if (!this.isLeaf()) {
                 if (!this.isExplored()) {
                     this.explore();
                 }
                 final Enumeration<DefaultMetaTreeNode> childrenEnumeration = children();
                 final DefaultMetaTreeNode childNode = childrenIterator.next();
                 while (childrenEnumeration.hasMoreElements()) {
                     final DefaultMetaTreeNode thisChildNode = childrenEnumeration.nextElement();
                     if (thisChildNode.getID() > -1) {
                         if (thisChildNode.getID() == childNode.getID()) {
                             return thisChildNode.explore(childrenIterator);
                         }
                     } else {
                         final String thisChildNodeString = thisChildNode.toString();
                         if (thisChildNodeString != null) {
                             if (thisChildNodeString.equals(childNode.toString())) {
                                 return thisChildNode.explore(childrenIterator);
                             }
                         } else {
                             LOG.warn("Fixme: thisChildNodeString is null!");
                         }
                     }
                 }
 
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("explore(): child node '" + childNode + "' not found"); // NOI18N
                 }
                 final TreePath fallback = handleNotMatchingNodeFound();
                 if (fallback != null) {
                     return fallback;
                 }
             }
         }
 
         return new TreePath(getPath());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private TreePath handleNotMatchingNodeFound() {
         final Enumeration<DefaultMetaTreeNode> childrenEnum = children();
         if (childrenEnum.hasMoreElements()) {
             final DefaultMetaTreeNode fallbackCandidate = childrenEnum.nextElement();
 
             return new TreePath(fallbackCandidate.getPath());
         }
 
         return null;
     }
 
     /**
      * Entfernt alle Children dieser Node und setzt ihren status zurueck;
      */
     public void removeChildren() {
         if (LOG.isDebugEnabled()) {
             LOG.debug("removing children"); // NOI18N
         }
         this.removeAllChildren();
         this.explored = false;
     }
 
     /**
      * Liefert die Beschreibung (bzw. den URL zur Beschreibung) der selektierten TreeNode.
      *
      * @return  URL String der Beschreibung oder "wird geladen ...", wenn WaitNode.
      */
     public abstract String getDescription();
 
     /**
      * Vergleicht die DefaultMetaTreeNode mit einer Sirius Node, und liefert true, falls diese die gleichen Daten
      * enthalten.
      *
      * @param       node  DOCUMENT ME!
      *
      * @return      true oder false.
      *
      * @deprecated  use <code>equals(Node node)</code>
      */
     public abstract boolean equalsNode(Node node);
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract ImageIcon getOpenIcon();
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract ImageIcon getClosedIcon();
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract ImageIcon getLeafIcon();
 
     /**
      * DOCUMENT ME!
      *
      * @param   node  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract boolean equals(DefaultMetaTreeNode node);
 
     /**
      * DOCUMENT ME!
      *
      * @param   node  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean deepEquals(final DefaultMetaTreeNode node) {
         if (node == null) {
             return false;
         }
 
         if (this == node) {
             return true;
         }
 
         if (!node.getClass().equals(this.getClass())) {
             return false;
         }
 
         if (!((userObject instanceof Node) && (node.userObject instanceof Node))) {
             return false;
         }
 
         final Node n1 = (Node)userObject;
         final Node n2 = (Node)node.userObject;
 
         return n1.deepEquals(n2);
     }
 
     /**
      * Returns the class ob object id.
      *
      * @return  DOCUMENT ME!
      */
     public abstract int getID();
 
     /**
      * Returns the class ob object id.
      *
      * @return  DOCUMENT ME!
      */
     public abstract int getClassID();
 
     /**
      * return the class or object domain (was: localserver)
      *
      * @return  DOCUMENT ME!
      */
     public abstract String getDomain();
 
     /**
      * Returns the unique key of the node's user object (class or object).
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public abstract String getKey() throws Exception;
 
     /**
      * Getter for property changed.
      *
      * @return  Value of property changed.
      */
     public boolean isChanged() {
         return this.changed;
     }
 
     /**
      * Setter for property changed.
      *
      * @param  changed  New value of property changed.
      */
     public void setChanged(final boolean changed) {
         this.changed = changed;
     }
 
     /**
      * Getter for property new_node.
      *
      * @return  Value of property new_node.
      */
     public boolean isNew() {
         return this.new_node;
     }
 
     /**
      * Setter for property new_node.
      *
      * @param  new_node  New value of property new_node.
      */
     public void setNew(final boolean new_node) {
         this.new_node = new_node;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   key  DOCUMENT ME!
      * @param   p    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public boolean isEditable(final Object key, final Permission p) throws Exception {
         return getNode().getPermissions().hasPermission(key, p);
     }
 
     /**
      * Setter for property explored.
      *
      * @param  explored  New value of property explored.
      */
     public void setExplored(final boolean explored) {
         this.explored = explored;
     }
 }
