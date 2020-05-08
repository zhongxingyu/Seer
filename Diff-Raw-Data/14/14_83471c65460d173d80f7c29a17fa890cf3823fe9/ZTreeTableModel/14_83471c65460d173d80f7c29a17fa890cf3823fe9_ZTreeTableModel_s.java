 /*
  * Created on 13-sep-2004
  */
 package zz.utils.treetable;
 
 import java.util.List;
 
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import zz.utils.tree.ITree;
 import zz.utils.tree.ITreeListener;
 import zz.utils.treetable.TreeTableModel;
 
 
 
 /**
  * Adapter from {@link ITree} to tree table model.
  * @author gpothier
  */
 public abstract class ZTreeTableModel<N, V> extends DefaultTreeModel 
 implements TreeTableModel
 {
 	private ITree<N, V> itsTree;
 
 	public ZTreeTableModel(ITree<N, V> aTree)
 	{
 		super(null);
 		itsTree = aTree;
 		
 		if (itsTree != null)
 		{
 			itsTree.addListener(new ITreeListener<N, V> ()
 			{
 
 				public void childAdded(ITree<N, V> aTree, N aNode,int aIndex, N aChild)
 				{
 					fireTreeStructureChanged ();
 				}
 
 				public void childRemoved(ITree<N, V> aTree, N aNode,int aIndex, N aChild)
 				{
 					fireTreeStructureChanged ();					
 				}
 
 				public void valueChanged(ITree<N, V> aTree, N aNode, V aNewValue)
 				{
 					fireTreeStructureChanged ();
 				}
 			});
 		}
 	}
 	
 	public void fireTreeStructureChanged ()
 	{
 		TreePath thePath = getTreePath(itsTree.getRoot());
 		fireTreeStructureChanged(this, thePath.getPath(), null, null);
 	}
 	
 	public Object getRoot()
 	{
 		return itsTree != null ? itsTree.getRoot() : null;
 	}
 
 	public int getChildCount(Object aParent)
 	{
 		return itsTree.getChildCount((N) aParent);
 	}
 
 	public boolean isLeaf(Object aNode)
 	{
 		return itsTree.isLeaf((N) aNode);
 	}
 
 	public Object getChild(Object aParent, int aIndex)
 	{
 		return itsTree.getChild((N) aParent, aIndex);
 	}
 
 	public int getIndexOfChild(Object aParent, Object aChild)
 	{
 		return itsTree.getIndexOfChild((N) aParent, (N) aChild);
 	}
 	
 	/**
	 * Subclasses shold override this method to provide the values for different columns
 	 */
	public Object getValueAt(Object aNode, int aColumn)
 	{
		return itsTree.getValue((N) aNode);
 	}
 	
 	public boolean isCellEditable(Object aNode, int aColumn)
 	{
 		return getColumnClass(aColumn) == TreeTableModel.class;
 	}
 	
 	public void setValueAt(Object aValue, Object aNode, int aColumn)
 	{
 		throw new UnsupportedOperationException ();
 	}
 	
     /**
      * Computes the tree path corresponding to a given node.
      */
     public TreePath getTreePath (N aNode)
     {
     	List<N> thePath = itsTree.getPath(aNode);
     	return new TreePath (thePath.toArray());
     }
 
 }
