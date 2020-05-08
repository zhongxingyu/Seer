 package org.geoserver.web.data.tree;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.swing.tree.TreeNode;
 
 import org.apache.wicket.model.IDetachable;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.platform.GeoServerExtensions;
 import org.geotools.util.logging.Logging;
 
 abstract class AbstractCatalogNode implements TreeNode, Serializable,
         IDetachable, Comparable<AbstractCatalogNode> {
 
     public enum SelectionState {
         SELECTED, UNSELECTED, PARTIAL
     };
 
     static final Logger LOGGER = Logging.getLogger(AbstractCatalogNode.class);
 
     String name;
 
     AbstractCatalogNode parent;
 
     transient List<AbstractCatalogNode> childNodes;
 
     transient Catalog catalog;
 
     SelectionState selectionState;
 
     public AbstractCatalogNode(String id, AbstractCatalogNode parent) {
         if (id == null)
             throw new NullPointerException("Id cannot be null");
         this.name = id;
         this.parent = parent;
     }
 
     protected Catalog getCatalog() {
         if (catalog == null) {
             catalog = (Catalog) GeoServerExtensions.bean("catalog2");
         }
         return catalog;
     }
 
     protected AbstractCatalogNode setParent(AbstractCatalogNode parent) {
         this.parent = parent;
         return this;
     }
 
     public Enumeration children() {
         final Iterator i = childNodes().iterator();
         return new Enumeration() {
 
             public boolean hasMoreElements() {
                 return i.hasNext();
             }
 
             public Object nextElement() {
                 return i.next();
             }
 
         };
     }
 
     public boolean getAllowsChildren() {
         return childNodes().size() > 0;
     }
 
     public TreeNode getChildAt(int childIndex) {
         return childNodes().get(childIndex);
     }
 
     public int getChildCount() {
         return childNodes().size();
     }
 
     public int getIndex(TreeNode node) {
         return childNodes().indexOf(node);
     }
 
     public AbstractCatalogNode getParent() {
         return parent;
     }
 
     public boolean isLeaf() {
         return getChildCount() <= 0;
     }
 
     List<AbstractCatalogNode> childNodes() {
         if (childNodes == null) {
             synchronized (this) {
                 if (childNodes == null) {
                     childNodes = buildChildNodes();
                     // sort child nodes
                     Collections.sort(childNodes);
                     // manage selection
                     if (selectionState == SelectionState.SELECTED)
                         for (AbstractCatalogNode child : childNodes) {
                             child.setSelectionState(SelectionState.SELECTED);
                         }
                     else
                         for (AbstractCatalogNode child : childNodes) {
                             child.setSelectionState(SelectionState.UNSELECTED);
                         }
                 }
             }
         }
 
         return childNodes;
     }
 
     protected abstract List<AbstractCatalogNode> buildChildNodes();
 
     protected String getNodeLabel() {
         return getModel().toString();
     }
 
     public boolean equals(Object obj) {
         if (!(obj instanceof AbstractCatalogNode)) {
             return false;
         }
 
         AbstractCatalogNode other = (AbstractCatalogNode) obj;
         return getModel().equals(other.getModel());
     }
 
     public int hashCode() {
         return getModel().hashCode();
     }
 
     public void detach() {
         // childNodes = null;
         catalog = null;
     }
 
     @Override
     public String toString() {
         return getNodeLabel();
     }
 
     protected abstract Object getModel();
 
     public void nextSelectionState() {
         if (selectionState == SelectionState.SELECTED
                 || selectionState == SelectionState.PARTIAL)
             setSelectionState(SelectionState.UNSELECTED);
         else
             setSelectionState(SelectionState.SELECTED);
     }
 
     public void setSelectionState(SelectionState state) {
         if (isSelectable()) {
             this.selectionState = state;
             if (state != SelectionState.PARTIAL && childNodes != null)
                 for (AbstractCatalogNode child : childNodes) {
                     child.setSelectionState(state);
                 }
         }
     }
 
     public SelectionState getSelectionState() {
         return selectionState;
     }
 
     public boolean isSelectable() {
         return true;
     }
 
     /**
      * Updates the partial selection state of node and recurses up to the root,
      * and returns the higher node that got updated during the process
      */
     public AbstractCatalogNode checkPartialSelection() {
         List<AbstractCatalogNode> children = childNodes;
         if (children == null || children.size() == 0)
             return this;
 
         boolean selected = false;
         boolean unselected = false;
         SelectionState result = null;
         for (AbstractCatalogNode child : children) {
             if (!child.isSelectable())
                 continue;
 
             SelectionState childState = child.getSelectionState();
             selected = selected || childState == SelectionState.SELECTED;
             unselected = unselected || childState == SelectionState.UNSELECTED;
             if ((selected && unselected)
                     || childState == SelectionState.PARTIAL) {
                 result = SelectionState.PARTIAL;
                 break;
             }
         }
         if (result == null && unselected)
             result = selectionState.UNSELECTED;
        if (result == null && selected)
            result = selectionState.PARTIAL;
         if (result != null && result != selectionState) {
             selectionState = result;
         }
         if (parent != null)
             return parent.checkPartialSelection();
         return this;
     }
 
     public int compareTo(AbstractCatalogNode other) {
         if (this instanceof AbstractPlaceholderNode
                 && !(other instanceof AbstractPlaceholderNode))
             return 1;
         else if (other instanceof AbstractPlaceholderNode
                 && !(this instanceof AbstractPlaceholderNode))
             return -1;
         String label1 = this.getNodeLabel();
         String label2 = other.getNodeLabel();
         return label1.compareTo(label2);
 
     }
 
 }
