 /* TaskNode.java - Part of Task Mistress
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 
 /**
  * Extends the DefaultMutableTreeNode by adding a stored task in each of the nodes.
  * @author anonpds <anonpds@gmail.com>
  */
 @SuppressWarnings("serial")
 public class TaskNode extends DefaultMutableTreeNode {
 	/** The task stored in the node or null if no task in the node. */
 	private Task task;
 	
 	/** Default constructor. */
 	public TaskNode() {
 	}
 	
 	/**
 	 * Constructs a node that contains the given task.
 	 * @param task the task to store in the node
 	 */
 	public TaskNode(Task task) {
 		this.task = task;
 	}
 	
 	/**
 	 * Sets the user object of the node. This is overwritten, because it may be called by DefaultTreeModel and those
 	 * calls need to be handled.
 	 * @param object the user object to set
 	 */
 	 @Override
 	 public void setUserObject(Object object) {
 		 this.userObject = object;
 	 }
 	 
 	 /**
 	  * Gets the real userObject value.
 	  * @return the value of userObject as set by setUserObject
 	  */
 	 public Object getRealUserObject() {
 		 return this.userObject;
 	 }
 	 
 	 /**
 	  * Returns the user object associated with this node; the actual returned object is the name of the Task.
 	  * This is needed for some of the treeView code, because the Object returned by this is used to display the tree.
 	  */
 	 @Override
 	 public Object getUserObject() {
 		 if (this.task == null) return null;
 		 return this.task.getName();
 	 }
 	
 	/**
 	 * Returns the task of this node.
 	 * @return the task of this node or null if no task is associated with this node
 	 */
 	public Task getTask() {
 		return this.task;
 	}
 	
 	/**
 	 * Sets the task of this node, overwriting the task that may have been set earlier.
 	 * @param task the task to set
 	 */
 	public void setTask(Task task) {
 		this.task = task;
 	}
 	
 	/**
 	 * Returns a string the describes the object. This exists for the treeView code, which may use this function to
 	 * display nodes in the tree.
 	 * @return the node as a string (the name of the task)
 	 */
 	public String toString() {
 		if (this.task == null) return "";
 		return this.task.getName();
 	}
 }
