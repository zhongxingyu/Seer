 /* TaskStore.java - Part of Task Mistress
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 import java.io.File;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 
 /**
  * A class that handles the storage of task trees in Task Mistress.
  * @author anonpds <anonpds@gmail.com>
  */
 public class TaskStore {
 	/** Name of the file that contains task tree meta data. */
 	private static final String META_FILE = "meta.cfg";
 	
 	/** Maximum length of a plain name, that is used when saving tasks to file system. */
 	private static final int MAX_PLAIN_NAME_LEN = 12;
 
 	/** The meta data configuration variable of creation time. */
 	private static final String META_CREATION = "creationTime";
 
 	/** The meta data configuration variable of task tree format. */
 	private static final String META_FORMAT = "format";
 
 	/** Format string for the default "file system" format. */
 	private static final String FORMAT_FILE_SYSTEM = "fs";
 
 	/** The tree model that contains the stored task tree. */
 	private DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
 
 	/** The file system path in which the file is stored. */
 	private File path;
 
 	/** The task tree configuration. */
 	private Configuration conf;
 	
 	/**
 	 * Creates a new task store from the specified directory. The directory is created if it doesn't exist and an
 	 * empty task store is initialised.
 	 * @param path the directory that stores the task tree
 	 * @throws Exception on any IO errors
 	 */
 	public TaskStore(File path) throws Exception {
 		/* make sure the path exists or can at least be created */
 		if (!path.exists() && !path.mkdirs()) throw new Exception("cannot create '" + path.getPath() + "'");
 		this.path = path;
 		
 		/* read the task tree meta data */
 		File metaFile = new File(path, META_FILE);
 		
 		/* create the meta data if it doesn't exist */
 		if (!metaFile.exists()) {
 			this.conf = new Configuration();
 			this.conf.add(META_CREATION, System.currentTimeMillis());
 			this.conf.add(META_FORMAT, FORMAT_FILE_SYSTEM);
 		} else {
 			/* throw an exception on error */
 			this.conf = Configuration.parse(metaFile);
 		}
 		
 		/* add the directories and their sub-directories recursively as nodes */
 		File[] files = this.path.listFiles();
 		for (File file : files) {
 			if (file.isDirectory()) this.loadTaskDirectory((DefaultMutableTreeNode) this.treeModel.getRoot(), file);
 		}
 	}
 	
 	/**
 	 * Closes the task store; writes out the configuration and any changed tasks. 
 	 * @throws Exception on error 
 	 */
 	public void close() throws Exception {
 		/* write the configuration */
 		File metaFile = new File(path, META_FILE);
 		this.conf.store(metaFile);
 		
 		/* write the tasks */
 		this.writeOut();
 	}
 	
 	/**
 	 * Returns a configuration variable from the task tree configuration.
 	 * @param name the name of the variable to return
 	 * @return value of the variable or null if no such variable exists
 	 */
 	public String getVariable(String name) {
 		return this.conf.get(name);
 	}
 	
 	/**
 	 * Sets a configuration variable to the task tree configuration; the TaskStore variables cannot be set!
 	 * @param name the name of the variable to set
 	 * @param value the value of the variable to set
 	 */
 	public void setVariable(String name, String value) {
 		if (META_CREATION.equals(name)) return;
 		if (META_FORMAT.equals(name)) return;
 		
 		this.conf.add(name, value);
 	}
 	
 	/**
 	 * Returns the tree model used by this task store.
 	 * @return the tree model
 	 */
 	public TreeModel getTreeModel() {
 		return this.treeModel;
 	}
 
 	/**
 	 * Adds the task to the store from a directory and recursively adds all the sub-tasks in sub-directories.
 	 * @param tree the tree node to add to
 	 * @param path the directory path to add
 	 * @throws Exception on any IO or parse errors
 	 */
 	private void loadTaskDirectory(DefaultMutableTreeNode tree, File path) throws Exception {
 		/* must be a directory */
 		if (!path.isDirectory()) throw new Exception("'" + path.getPath() + "' not a directory");
 
 		/* create the task from the read data */
 		Task task = FileSystemTask.load(path);
 		
 		/* add the node to the tree */
 		if (task != null) {
 			DefaultMutableTreeNode node = new DefaultMutableTreeNode(task);
 			tree.add(node);
 
 			/* finally recurse into any potential sub-directories */
 			File[] files = path.listFiles();
 			for (File file : files) {
 				if (file.isDirectory()) this.loadTaskDirectory(node, file);
 			}
 		}
 	}
 	
 	/**
 	 * Returns the root node of the tree.
 	 * @return the root node
 	 */
 	public DefaultMutableTreeNode getRoot() {
 		return (DefaultMutableTreeNode) this.treeModel.getRoot();
 	}
 
 	/**
 	 * Adds a named node as a child of the given node.
 	 * @param parent the parent node
 	 * @param name the name of the new node to add
 	 * @return the added node
 	 */
 	public DefaultMutableTreeNode add(DefaultMutableTreeNode parent, String name) {
 		Task task = new FileSystemTask(name, "", System.currentTimeMillis(), true, null);
 		DefaultMutableTreeNode node = new DefaultMutableTreeNode(task);
 		this.treeModel.insertNodeInto(node, parent, parent.getChildCount());
 		return(node);
 	}
 
 	/**
 	 * Returns the file system path of the node.
 	 * @param node the node to query
 	 * @return the file system path to the node
 	 */
 	private File getNodePath(DefaultMutableTreeNode node) {
 		/* start with the task tree root directory */
 		File path = this.path;
 
 		/* traverse the tree path to this node */
 		TreeNode[] treePath = node.getPath();
 		
 		/* start traversal from second path object; first is root and already accounted for */
 		for (int i = 1; i < treePath.length; i++) {
 			/* get the Task object from the tree node */
 			DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) treePath[i];
 			Task task = (Task) curNode.getUserObject();
 			
 			/* get the file system (plain) name of the task; create it, if it's not set */
 			String curPath = ((FileSystemTask)task).getPlainName();
 			if (curPath == null) this.setFileSystemName(curNode);
 			
 			/* add the task directory to path */
 			path = new File(path, ((FileSystemTask)task).getPlainName());
 		}
 		
 		return path;
 	}
 	
 	/**
 	 * Deletes a directory and all its contents recursively.
 	 * @param path the directory to delete
 	 */
 	private void deleteDirectory(File path) {
 		if (!path.isDirectory()) return;
 		
 		/* recurse into sub-directories */
 		File[] files = path.listFiles();
 		for (File file : files) {
 			if (file.isDirectory()) {
 				deleteDirectory(file); /* recurse on directories */
 				file.delete();
 			}
 		}
 		
 		/* remove the task files */
 		FileSystemTask.removeTaskFiles(path);
 
 		/* attempt to delete the directory when all sub-directories are clear; this may and should fail if there are
 		 * any files left that are not related to the task storage. */
 		path.delete();
 	}
 	
 	/**
 	 * Removes a node and all its children from the tree.
 	 * @param node the node to remove
 	 */
 	public void remove(DefaultMutableTreeNode node) {
 		if (node.isRoot()) return; /* never remove the root node */
 		
 		/* delete the file system path of the node and its children */
 		File path = this.getNodePath(node);
 		this.deleteDirectory(path);
 
 		/* remove the node from the tree */
 		this.treeModel.removeNodeFromParent(node);
 	}
 	
 	/**
 	 * Moves a node and all its children under another node.
 	 * @param dest the destination node
 	 * @param node the node to move
 	 * @throws Exception when the move is not possible
 	 */
 	public void move(DefaultMutableTreeNode dest, DefaultMutableTreeNode node) throws Exception {
 		/* never move root node or a node unto itself or a node to its parent */
 		if (node.isRoot() || node == dest || node.getParent() == dest) return;
 
 		/* never move a parent down into itself */
 		for (DefaultMutableTreeNode child = dest; child != null; child = (DefaultMutableTreeNode) child.getParent())
 			if (child == node) throw new Exception("Cannot move node under itself!");
 
 		/* save the file system path of the old node location */
 		File oldPath = this.getNodePath(node);
 		
 		/* invalidate the file system name of the node */
 		Task oldTask = (Task) node.getUserObject();
 		((FileSystemTask)oldTask).setPlainName(null);
 		
 		/* remove the node and add it under the destination node */
 		this.treeModel.removeNodeFromParent(node);
 		this.treeModel.insertNodeInto(node, dest, dest.getChildCount());
 		
 		/* update the file system: set the new file system (plain) name and move the task directory */
 		this.setFileSystemName(node);
 		oldPath.renameTo(this.getNodePath(node));
 	}
 
 	/* CRITICAL implement a function that writes just a single task to the disk; important for saving the status
 	 * of tasks right after they have changed
 	 */
 	/**
 	 * Writes the tasks to disk. 
 	 * @throws Exception on any error
 	 */
 	public void writeOut() throws Exception {
 		this.writeOut(this.path);
 	}
 	
 	/**
 	 * Writes the tasks to disk to a specified directory instead of the default.
 	 * @param path the directory path to write the tasks to
 	 * @throws Exception on any error
 	 */
 	public void writeOut(File path) throws Exception {
 		this.writeOutRecurse(this.path, this.getRoot());
 	}
 	
 	/**
 	 * Writes out a single task.
 	 * @param node the task node
 	 * @throws Exception on IO errors
 	 */
 	public void writeOut(DefaultMutableTreeNode node) throws Exception {
 		/* get the path of the node and the task */
 		File path = this.getNodePath(node);
 		Task task = (Task) node.getUserObject();
		/* DEBUG */ System.out.println("Writing out " + task.getName() + " to " + path.getPath());
 		if (!node.isRoot()) ((FileSystemTask)task).save(path);
 	}
 
 	/**
 	 * Writes the tasks in given node and all its children to disk recursively.
 	 * @param path the path to write to
 	 * @param node the node to write out
 	 * @throws Exception on IO errors
 	 */
 	private void writeOutRecurse(File path, DefaultMutableTreeNode node) throws Exception {
 		/* get the user object from the node */
 		Task task = (Task) node.getUserObject();
 
 		/* Don't save the root node */
 		if (!node.isRoot())	((FileSystemTask)task).save(path);
 		
 		/* recurse for each of the child nodes */
 		for (int i = 0; i < node.getChildCount(); i++) {
 			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
 			Task childTask = (Task) child.getUserObject();
 			
 			/* make sure the file system name has been set for the node */
 			this.setFileSystemName(child);
 			File newPath = new File(path, ((FileSystemTask)childTask).getPlainName());
 			this.writeOutRecurse(newPath, child);
 		}
 	}
 
 	/**
 	 * Sets the file system name of a tree node, if it isn't set already.
	 * @param path the file system path where the node resides
 	 * @param curNode the node
 	 */
 	private void setFileSystemName(DefaultMutableTreeNode curNode) {
 		/* don't set for root */
 		if (curNode.isRoot()) return;
 		
 		/* get the user object from the tree node */
 		Task task = (Task) curNode.getUserObject();
 
 		/* don't set name if one already exists */
 		if (((FileSystemTask)task).getPlainName() != null) return;
 
 		/* the parent path under which this node will be written */
 		File path = this.getNodePath((DefaultMutableTreeNode) curNode.getParent());
 		
 		/* remove all silly characters from the node name and make everything lower-case */
 		String name = "";
 		for (int i = 0; i < task.getName().length(); i++) {
 			char ch = task.getName().charAt(i);
 			/* only accept letters or digits in the ASCII range */
 			if (ch < 128 && Character.isLetterOrDigit(ch)) name = name + Character.toLowerCase(ch);
 		}
 		
 		/* don't allow too long plain names */
 		if (name.length() > MAX_PLAIN_NAME_LEN) name = name.substring(0, MAX_PLAIN_NAME_LEN);
 		
 		/* if any characters left, try the name as is */
 		if (name.length() > 0) {
 			File newPath = new File(path, name);
 			if (!newPath.exists()) {
 				/* success */
 				((FileSystemTask)task).setPlainName(name);
 				return;
 			}
 		}
 		
 		/* append numbers at the end of the name until non-existing path is found */
 		/* TODO only 100 numbers are tried at most! Do something about this. */
 		for (int i = 0; i < 100; i++) {
 			String newName = name + i;
 			File newPath = new File(path, newName);
 			if (!newPath.exists()) {
 				/* success! */
 				((FileSystemTask)task).setPlainName(newName);
 				return;
 			}
 		}
 		
 		/* error, no suitable name found */
 		throw new RuntimeException("no suitable path found for " + task.getName() + " in " + path.getPath());
 	}
 
 	/* TODO make this into actual debug function that outputs to some debug stream, instead of System.out */
 	@SuppressWarnings("javadoc")
 	public void print() {
 		print(0, this.getRoot());
 	}
 	/* TODO make this into actual debug function that outputs to some debug stream, instead of System.out */
 	@SuppressWarnings("javadoc")
 	public void print(int depth, DefaultMutableTreeNode node) {
 		Task task = (Task) node.getUserObject();
 		for (int i = 0; i < depth; i++) System.out.print("  ");
 		if (task != null) System.out.println(task.getName() + ": " + task.getText());
 		for (int i = 0; i < node.getChildCount(); i++)
 			print(depth + 1, (DefaultMutableTreeNode) node.getChildAt(i));
 	}
 }
