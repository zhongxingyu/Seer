 package org.zkoss.addon;
 
 import java.util.Collection;
 
 import org.zkoss.zul.DefaultTreeNode;
 
 public class SpaceTreeNode<E> extends DefaultTreeNode<E> {
 
 	private static int genId = 0;
 	private int id;
 	public SpaceTreeNode(E data, Collection<SpaceTreeNode<E>> children) {
 		super(data, children);
 		id = genId++;
 	}
 	
 	public boolean isRoot() {
 		return getParent() == null;	
 	}
 		
 	public void add(org.zkoss.zul.TreeNode<E> child) {
		if(isRoot() && getChildCount() == 1)
 			try {
 				throw new Exception("the root has one child at most");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		else
 			super.add(child);
 	};
 
 	public int getId() {
 		return id;
 	}
 
 	public boolean hasChildren() {
 		return getChildren() != null && getChildren().size() != 0;
 	}
 
 }
