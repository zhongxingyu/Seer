 package com.felipelo.etlgui.schema.model;
 
 import br.com.saxes.suite.model.TreeNode;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 /**
  *
  * @author felipe.lorenz
  */
public abstract class TreeNodeMutable extends DefaultMutableTreeNode {
 	
	private final static int ROW_COUNT = 0;
 	
 	private int rowCount;

	private TreeNodeMutable() {
		super();
		rowCount = ROW_COUNT;
 	}
 	
	public TreeNodeMutable(int rowCount, TreeNode object) {
		this();
 		this.rowCount = rowCount;
 		this.userObject = object;
 	}
 	
 	public int getRowCount() { return rowCount; }
 	
 }
