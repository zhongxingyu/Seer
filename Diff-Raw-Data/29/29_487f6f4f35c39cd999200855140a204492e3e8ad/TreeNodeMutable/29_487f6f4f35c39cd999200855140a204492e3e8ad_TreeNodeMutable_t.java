 package com.felipelo.etlgui.schema.model;
 
 import br.com.saxes.suite.model.TreeNode;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 /**
  *
  * @author felipe.lorenz
  */
public class TreeNodeMutable extends DefaultMutableTreeNode {
 	
	private final static int ROW_COUNT = 2;
 	
 	private int rowCount;
	
	public TreeNodeMutable(TreeNode object) {
		this(ROW_COUNT, object);
 	}
 	
	protected TreeNodeMutable(int rowCount, TreeNode object) {
		super();
 		this.rowCount = rowCount;
 		this.userObject = object;
 	}
 	
 	public int getRowCount() { return rowCount; }
 	
 }
