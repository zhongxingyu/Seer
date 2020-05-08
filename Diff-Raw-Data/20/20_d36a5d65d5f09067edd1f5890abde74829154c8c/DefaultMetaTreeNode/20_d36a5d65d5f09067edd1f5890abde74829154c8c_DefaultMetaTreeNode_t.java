 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.types.treenode;
 
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
  * Version                      :       1.0
  * Purpose                      :
  * Created                      :       01.11.1999
  * History                      :
  *
  *******************************************************************************/
 import Sirius.navigator.connection.*;
 import Sirius.navigator.connection.proxy.*;
 import Sirius.navigator.exception.*;
 
 import Sirius.server.middleware.types.*;
 import Sirius.server.newuser.permission.*;
 
 import org.apache.log4j.Logger;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.tree.*;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public abstract class DefaultMetaTreeNode extends DefaultMutableTreeNode // implements Comparable
 {
 
     //~ Static fields/initializers ---------------------------------------------
 
     // deprecated
     // #########################################################################
 
     /**
      * DOCUMENT ME!
      *
      * @deprecated  DOCUMENT ME!
      */
     // public final static short ANY_NODES = 1;
     /**
      * DOCUMENT ME!
      *
      * @deprecated  DOCUMENT ME!
      */
     // public final static short CLASS_NODES = 2;
     /**
      * DOCUMENT ME!
      *
      * @deprecated  DOCUMENT ME!
      */
     // public final static short OBJECT_NODES = 4;
     // #########################################################################
     protected static final Logger logger = Logger.getLogger(DefaultMetaTreeNode.class);
 
     //~ Instance fields --------------------------------------------------------
 
     protected boolean explored = false;
     protected boolean selected = false;
     protected boolean enabled = true;
     // protected Node[] children = null;
     /** Holds value of property changed. */
     private boolean changed;
     /** Holds value of property new_node. */
     private boolean new_node;
 
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
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  selected  DOCUMENT ME!
      */
     public void setSelected(final boolean selected) {
         this.selected = enabled & selected;
         // this.selected = selected;
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
 //yxc
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public Node[] getChildren() throws Exception {
         final Node node = this.getNode();
 //        if(node != null &&  !node.isLeaf())
 //        {
 //            if(node.getChildren() == null)
 //            {
 
        final Node[] c = SessionManager.getProxy().getChildren(node, SessionManager.getSession().getUser());
        if (node.isDynamic() && node.isSqlSort()) {
            return c;
        }

        return Sirius.navigator.tools.NodeSorter.sortNodes(c);
 //            }
 //            else
 //            {
 //                return node.getChildren();
 //            }
 //        }
 //        else
 //        {
 //            return new Node[0];
 //        }
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
      * public void setChildren(Node[] children) { this.children = children; } public abstract Node[] getChildren()
      * throws Exception; public abstract void setTreeNodeLoader(TreeNodeLoader loader);
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
      * Ueberschreibt die Funktion getAllowsChildren() in MutableTreeNode.<br>
      * Wird ueberschrieben um die Expansion 'Anfasser' ('+', 'o-') anzuzeigen, auch wenn diese Node noch gar keine
      * Children hat.
      *
      * @return  true/false
      */
     // public abstract boolean getAllowsChildren();
     /**
      * Ueberschreibt die Funktion isLeaf() in MutableTreeNode.
      *
      * @return  true/false
      */
     // public abstract boolean isLeaf();
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
             if (logger.isDebugEnabled()) {
                 logger.warn("exploring all children of node '" + this + "'"); // NOI18N
             }
             if (!this.isExplored()) {
                 this.explore();
             }
 
             final Enumeration children = this.children();
             while (children.hasMoreElements()) {
                 ((DefaultMetaTreeNode)children.nextElement()).exploreAll();
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
                             logger.warn("Fixme: thisChildNodeString is null!");
                         }
                     }
                 }
 
                 if (logger.isDebugEnabled()) {
                     logger.debug("explore(): child node '" + childNode + "' not found"); // NOI18N
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
         if (logger.isDebugEnabled()) {
             logger.debug("removing children"); // NOI18N
         }
         this.removeAllChildren();
         this.explored = false;
     }
 
     /**
      * Fuegt neue Children zu einem Knoten hinzu.
      *
      * @return    DOCUMENT ME!
      *
      * @children  Ein Array mit den Children.
      */
     /*public boolean addChildren(Node[] children) throws Exception
      * { boolean explored = true;
      *
      * if(NavigatorLogger.TREE_VERBOSE)NavigatorLogger.printMessage("[DefaultMetaTreeNode] Begin addChildren()"); //
      * WaitNode entfernen! this.removeChildren();
      *
      * if(children == null) return false;
      *
      * for (int i = 0; i < children.length; i++) { if (children[i] instanceof PureNode) { this.add(new PureTreeNode(new
      * LocalPureNode(children[i]))); explored &= children[i].isValid();
      *
      * if(NavigatorLogger.TREE_VERBOSE)NavigatorLogger.printMessage("[DefaultMetaTreeNode] PureNode Children added"); }
      * else if (children[i] instanceof ClassNode) { this.add(new ClassTreeNode(new LocalClassNode(children[i])));
      * explored &= children[i].isValid();
      *
      * if(NavigatorLogger.TREE_VERBOSE)NavigatorLogger.printMessage("[DefaultMetaTreeNode] ClassNode Children added"); }
      * else if (children[i] instanceof ObjectNode) { this.add(new ObjectTreeNode(new LocalObjectNode(children[i])));
      * explored &= children[i].isValid();
      *
      * if(NavigatorLogger.TREE_VERBOSE)NavigatorLogger.printMessage("[DefaultMetaTreeNode] ObjectNode Children added"); }
      * else { if(NavigatorLogger.DEV)NavigatorLogger.printMessage("[DefaultMetaTreeNode] Wrong Node Type: " +
      * children[i]); //_TA_throw new Exception("<TREENODE> Fehler: falscher Node-Typ: " + children[i]); throw new
      * Exception(StringLoader.getString("STL@wrongNodeType") + children[i]); }
      *
      * if(NavigatorLogger.TREE_VERBOSE)NavigatorLogger.printMessage("[DefaultMetaTreeNode] Children #" + i+1 + " added.");
      * } return explored;}*/
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
      * @return      DOCUMENT ME!
      *
      * @deprecated  , use <code>AttributeIterator</code>
      */
     // public abstract String[][] getAttributes() throws Exception;
     // icons ...................................................................
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
     // .........................................................................
 
     /**
      * DOCUMENT ME!
      *
      * @param   node  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract boolean equals(DefaultMetaTreeNode node);
 
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
 
     // deprecated
     // #########################################################################
     /**
      * Falls das UserObject dieser TreeNode eine ClassNode ist, liefert diese Funktion die zugehoerige Class.
      *
      * @return      Eine Sirius.Middleware.Types.Class oder null;
      *
      * @throws      Exception  DOCUMENT ME!
      *
      * @deprecated  use Iterator
      */
     /*public MetaClass getClass(int ofWhichNodes) throws Exception
      * { // Auf WaitNode muss immer ueberprueft werden, da diese kein // nodeObject hat. if(this.isWaitNode() ||
      * this.isRootNode()) return null; else if (this.isObjectNode() && (ofWhichNodes == ANY_NODES || ofWhichNodes ==
      * OBJECT_NODES)) return ((ObjectTreeNode)this).getMetaClass(); else if (this.isClassNode() && (ofWhichNodes ==
      * ANY_NODES || ofWhichNodes ==  CLASS_NODES)) return ((ClassTreeNode)this).getMetaClass(); else return null;}*/
     /**
      * Falls das UserObject dieser TreeNode eine ClassNode oder eine ObjectNode ist, liefert diese Funktion die ClassID
      * der Class der ClassNode oder die ClassID der Class des Objects der ObjectNode.
      *
      * @return      Die ClassID oder -1.
      *
      * @throws      Exception  DOCUMENT ME!
      *
      * @deprecated  use Iterator
      */
     /*public int getClassID(int ofWhichNodes)  throws Exception
      * { MetaClass tmpClass = this.getClass(ofWhichNodes);
      *
      * if(tmpClass != null) return tmpClass.getID(); else return -1;}*/
     /**
      * Falls das UserObject dieser TreeNode eine ObjectNode ist, liefert diese Funktion das zugehoerige Objekt.
      *
      * @return      Ein Sirius.Middleware.Types.Object oder null;
      *
      * @throws      Exception  DOCUMENT ME!
      *
      * @deprecated  use Iterator
      */
     /*public MetaObject getObject()
      * { if(this.isObjectNode()) { return ((ObjectTreeNode)this).getObject(); }
      *
      * return null;}*/
     /**
      * Falls das UserObject dieser TreeNode eine eine ObjectNode ist, liefert diese Funktion alle Attribute alle
      * Attribute der Class des Objects der ObjectNode.
      *
      * @return      Ein Array mit allen Attributen oder null.
      *
      * @throws      Exception  DOCUMENT ME!
      *
      * @deprecated  use AttributeIterator
      */
     /*public Sirius.server.localserver.attribute.Attribute[] getAttributes(int ofWhichNodes)  throws Exception
      * { if(this.isWaitNode() || this.isRootNode()) { return null; } else if ((ofWhichNodes == ANY_NODES || ofWhichNodes
      * ==  OBJECT_NODES) && this.isObjectNode()) { Sirius.server.localserver.attribute.Attribute[] attr =
      * this.getObject().getAttribs(); //if(NavigatorLogger.TREE_VERBOSE)NavigatorLogger.printMessage("<TREENODE> Object
      * getNodeAttributes(): " + attr.length); return attr; } else if (ofWhichNodes == ANY_NODES || ofWhichNodes ==
      * CLASS_NODES) { if (this.isClassNode()) { Sirius.server.localserver.attribute.Attribute[] attr =
      * ((ClassTreeNode)this).getMetaClass().getAttribs();
      * //if(NavigatorLogger.TREE_VERBOSE)NavigatorLogger.printMessage("<TREENODE> Class getNodeAttributes(): " +
      * attr.length); return attr; } else if (this.isObjectNode()) { Sirius.server.localserver.attribute.Attribute[] attr
      * = ((ObjectTreeNode)this).getObject().getAttribs();
      * //if(NavigatorLogger.TREE_VERBOSE)NavigatorLogger.printMessage("<TREENODE> Object:Class getNodeAttributes(): " +
      * attr.length); return attr; } else return null; } else return null;}*/
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
 
     /*public boolean equals(Object object)
      * { if(object instanceof DefaultMetaTreeNode) { return this.equals((DefaultMetaTreeNode)object); }
      *
      * return false;}*/
     /*{
      * return "xxx";}*/
     /**
      * returns true if the key is equal to this nodes key.
      *
      * @param   key  DOCUMENT ME!
      * @param   p    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     /*public boolean equals(String key)
      * { return this.getKey() != null ? key.equals(this.getKey()) : false;}*/
     /*public boolean equals(Object object)
      * { if(object instanceof DefaultMetaTreeNode) { logger.debug("equals DefaultMetaTreeNode :"  + object); return
      * this.equals((DefaultMetaTreeNode)object); } else if (object instanceof Node) { return this.equals((Node)object);
      * } else if (object instanceof String) { return this.equals(object.toString()); }
      *
      * return false;}*/
     // #########################################################################
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
