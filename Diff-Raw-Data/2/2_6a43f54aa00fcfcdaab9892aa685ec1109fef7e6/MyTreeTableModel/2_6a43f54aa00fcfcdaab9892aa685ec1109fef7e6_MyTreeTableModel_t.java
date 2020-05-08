 package com.nyver.rctool.treetable;
 
 import com.ezware.oxbow.swingbits.table.filter.DistinctColumnItem;
 import com.nyver.rctool.model.Revision;
 import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
 import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
 import org.jdesktop.swingx.treetable.MutableTreeTableNode;
 import org.jdesktop.swingx.treetable.TreeTableNode;
 
 import javax.swing.tree.TreeNode;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Yuri Novitsky
  */
 public abstract class MyTreeTableModel<E> extends AbstractTreeTableModel
 {
     protected String[] columns;
     protected TreeTableNode root;
 
     protected Map<Integer, Collection<DistinctColumnItem>> distinctColumnItems = new HashMap<Integer, Collection<DistinctColumnItem>>();
 
     protected MyTreeTableModel()
     {
         super(null);
     }
 
     protected MyTreeTableModel(TreeTableNode root)
     {
         super(root);
         this.root = root;
     }
 
     protected MyTreeTableModel(TreeTableNode root, String[] columns)
     {
         super(root);
         this.root = root;
         this.columns = columns;
     }
 
 
     @Override
     public Object getRoot()
     {
         return filterRoot(root);
     }
 
     @Override
     public int getColumnCount()
     {
         return columns.length;
     }
 
     @Override
     public Class<?> getColumnClass(int column)
     {
         return String.class;
     }
 
     @Override
     public String getColumnName(int column)
     {
         return columns[column];
     }
 
     @Override
     public Object getChild(Object parent, int index)
     {
         if (parent instanceof MutableTreeTableNode) {
             return ((MutableTreeTableNode) parent).getChildAt(index);
         }
 
         return null;
     }
 
     @Override
     public int getChildCount(Object parent)
     {
         if (parent instanceof MutableTreeTableNode) {
             return ((MutableTreeTableNode) parent).getChildCount();
         }
         return 0;
     }
 
     @Override
     public int getIndexOfChild(Object parent, Object child)
     {
         if (parent instanceof MutableTreeTableNode) {
             return ((MutableTreeTableNode) parent).getIndex((TreeNode) child);
         }
         return 0;
     }
 
     public void setDistinctColumnItems(int i, Collection<DistinctColumnItem> items)
     {
         distinctColumnItems.put(i, items);
     }
 
     public void setColumns(String[] columns)
     {
         this.columns = columns;
     }
 
     public E getItem(int row)
     {
        MutableTreeTableNode node = (MutableTreeTableNode) getChild(filterRoot(root), row);
         if (null != node) {
             return (E) node.getUserObject();
         }
         return null;
     }
 
     protected abstract E getRootUserData();
 
     protected Object filterRoot(Object root)
     {
         DefaultMutableTreeTableNode revisions = new DefaultMutableTreeTableNode(getRootUserData());
         if (null != distinctColumnItems && distinctColumnItems.size() > 0) {
             DefaultMutableTreeTableNode rootNode = (DefaultMutableTreeTableNode) root;
             for(int count = 0; count < rootNode.getChildCount(); count++) {
                 DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) rootNode.getChildAt(count);
                 for(int i = 0; i < getColumnCount(); i++) {
                     if (distinctColumnItems.containsKey(i)) {
                         if (distinctColumnItems.get(i).contains(new DistinctColumnItem(getValueAt(node, i), 0))) {
                             revisions.add(new DefaultMutableTreeTableNode(node.getUserObject()));
                             break;
                         }
                     }
                 }
             }
         } else {
             revisions = (DefaultMutableTreeTableNode) root;
         }
 
         return revisions;
     }
 
 }
