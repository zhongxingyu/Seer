 
 package edu.common.dynamicextensions.ui.webui.util;
 
import java.util.ArrayList;
import java.util.List;
 
 public class TreeNodesList
 {
 
	List childNodeList = null;
 	private int length = 0;
 
 	/**
 	 */
 	public TreeNodesList()
 	{
		childNodeList = new ArrayList();
 	}
 
 	/**
 	 * Add new sub-node to list
 	 * @param node Tree node 
 	 */
 	public void add(TreeNode node)
 	{
 		childNodeList.add(node);
 		length++;
 	}
 
 	/**
 	 * Create a new tree node and add to list
 	 * @param text  : Name of tree node
 	 * @param seqno : Node sequence number
 	 */
 	public void add(String text, int seqno)
 	{
 		add(new TreeNode(text, seqno));
 	}
 
 	/**
 	 * Get node at specified location in child list
 	 * @param index : index in the child list
 	 * @return Tree node at index
 	 */
 	public TreeNode item(int index)
 	{
 		return (TreeNode) childNodeList.get(index);
 	}
 
 	/**
 	 * 
 	 * @return Length of list
 	 */
 	public int getLength()
 	{
 		return this.length;
 	}
 
 	/**
 	 * 
 	 * @param length : Length of list
 	 */
 	public void setLength(int length)
 	{
 		this.length = length;
 	}
 }
