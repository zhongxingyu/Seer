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
 
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 
 /**
  * A class that handles the storage of task trees in Task Mistress.
  * @author anonpds <anonpds@gmail.com>
  */
 public class TaskStore implements TreeModelListener {
 	/** Name of the file that contains task tree meta data. */
 	private static final String META_FILE = "meta.cfg";
 	
 	private static final String LOCK_FILE = "tree.lck";
 
 	/** The meta data configuration variable of creation time. */
 	private static final String META_CREATION = "creationTime";
 
 	/** The meta data configuration variable of task tree format. */
 	private static final String META_FORMAT = "format";
 
 	/** Format string for the default "file system" format. */
 	private static final String FORMAT_FILE_SYSTEM = "fs";
 
 	/** The tree model that contains the stored task tree. */
 	private DefaultTreeModel treeModel = new DefaultTreeModel(new Task());
 
 	/** The file system path in which the file is stored. */
 	private File path;
 
 	/** The task tree configuration. */
 	private Configuration conf;
 	
 	/**
 	 * Creates a new task store from the specified directory. The directory is created if it doesn't exist and an
 	 * empty task store is initialised.
 	 * @param path the directory that stores the task tree
 	 * @param ignoreLock set to true to ignore lock file, false not to ignore it
 	 * @throws TaskTreeLockedException if the task tree is locked (already open)
 	 * @throws Exception on any IO errors
 	 */
 	public TaskStore(File path, boolean ignoreLock) throws TaskTreeLockedException, Exception {
 		/* make sure the path exists or can at least be created */
 		if (!path.exists() && !path.mkdirs()) throw new Exception("cannot create '" + path.getPath() + "'");
 		this.path = path;
 		
 		/* make sure the lock file does not exist */
 		File lockFile = new File(path, LOCK_FILE);
 		if (!ignoreLock && lockFile.exists()) throw new TaskTreeLockedException();
 		
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
 		
 		/* add a listener to get information on changes to the tree; needed for task renames */
 		this.treeModel.addTreeModelListener(this);
 
 		/* load the task tree */
 		FileSystemTask.loadTree((Task) this.treeModel.getRoot(), path);
 		
 		/* create the lock file to indicate that the task tree is open; set the file to be deleted on exit */
 		lockFile.createNewFile();
 		lockFile.deleteOnExit();
 	}
 	
 	/**
 	 * Sets another path for the task store. Note: this will dirty all the tasks in the tree, causing all of them to
 	 * be saved to disk when the store is closed.
 	 * @param path the new path for the task store
 	 * @throws Exception if the path does not exist and cannot be created
 	 */
 	public void setPath(File path) throws Exception {
 		if (!path.exists() && !path.mkdirs()) throw new Exception("cannot create '" + path.getPath() + "'");
 		this.path = path;
 		this.getRoot().setAllDirty();
 	}
 	
 	/**
 	 * Closes the task store; writes out the configuration and any changed tasks. 
 	 * @throws Exception on error 
 	 */
 	public void close() throws Exception {
 		/* write the configuration */
 		File metaFile = new File(this.path, META_FILE);
 		this.conf.store(metaFile);
 		
 		/* write the tasks */
 		this.writeOut();
 		
 		/* remove the lock file */
 		File lockFile = new File(this.path, LOCK_FILE);
 		lockFile.delete();
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
 	 * Returns the root node of the tree.
 	 * @return the root node
 	 */
 	public Task getRoot() {
 		return (Task) this.treeModel.getRoot();
 	}
 
 	/**
	 * Adds a named node as a child of the given node.
 	 * @param parent the parent node
 	 * @param name the name of the new node to add
 	 * @return the added node
 	 */
 	public Task add(Task parent, String name) {
 		Task task = new FileSystemTask(null, name, "", System.currentTimeMillis(), true);
 		this.treeModel.insertNodeInto(task, parent, parent.getChildCount());
 		return(task);
 	}
 
 	/**
 	 * Returns the file system path of the node.
 	 * @param node the node to query
 	 * @return the file system path to the node
 	 */
 	private File getNodePath(Task node) {
 		/* start with the task tree root directory */
 		File path = this.path;
 
 		/* traverse the tree path to this node */
 		TreeNode[] treePath = node.getPath();
 		
 		/* start traversal from second path object; first is root and already accounted for */
 		for (int i = 1; i < treePath.length; i++) {
 			/* get the Task object from the tree node */
 			Task task = (Task) treePath[i];
 			
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
 	public void remove(Task node) {
 		if (node.isRoot()) return; /* never remove the root node */
 		
 		/* delete the file system path of the node and its children */
 		File path = this.getNodePath(node);
 		this.deleteDirectory(path);
 
 		/* remove the node from the tree */
 		this.treeModel.removeNodeFromParent(node);
 	}
 	
 	/**
 	 * Renames a task.
 	 * @param node the node that contains the task to rename
 	 * @param name the new name of the task
 	 */
 	public void rename(Task node, String name) {
 		/* don't rename if null name or name hasn't changed */
 		if (name == null || name.compareTo(node.getName()) == 0) return;
 		
 		/* store the old task folder and plain name */
 		File path = this.getNodePath(node);
 		String plainName = node.getPlainName();
 		
 		/* rename the task */
 		node.setName(name);
 		
 		/* no need to rename, if the plain name hasn't changed */
 		if (node.getPlainName().compareTo(plainName) == 0) return;
 		
 		/* rename the folder */
 		path.renameTo(this.getNodePath(node));
 		
 		/* update the treeModel, so the node will be repainted in the tree view */
 		this.treeModel.nodeChanged(node);
 	}
 	
 	/**
 	 * Moves a node and all its children under another node.
 	 * @param dest the destination node
 	 * @param node the node to move
 	 * @throws Exception when the move is not possible
 	 */
 	public void move(Task dest, Task node) throws Exception {
 		/* never move root node or a node unto itself or a node to its parent */
 		if (node.isRoot() || node == dest || node.getParent() == dest) return;
 
 		/* never move a parent down into itself */
 		for (Task child = dest; child != null; child = (Task) child.getParent())
 			if (child == node) throw new Exception("Cannot move node under itself!");
 
 		/* save the file system path of the old node location */
 		File oldPath = this.getNodePath(node);
 		
 		/* remove the node and add it under the destination node */
 		this.treeModel.removeNodeFromParent(node);
 		this.treeModel.insertNodeInto(node, dest, dest.getChildCount());
 		
 		/* update the file system: move the task directory */
 		oldPath.renameTo(this.getNodePath(node));
 	}
 
 	/**
 	 * Writes the tasks to disk. 
 	 * @return the number of tasks actually written to disk
 	 * @throws Exception on any error
 	 */
 	public int writeOut() throws Exception {
 		return this.writeOut(this.path);
 	}
 	
 	/**
 	 * Writes the tasks to disk to a specified directory instead of the default.
 	 * @param path the directory path to write the tasks to
 	 * @return the number of tasks actually written to disk
 	 * @throws Exception on any error
 	 */
 	public int writeOut(File path) throws Exception {
 		return FileSystemTask.saveTree(this.getRoot(), this.path);
 	}
 	
 	/**
 	 * Writes out a single task.
 	 * @param node the task node
 	 * @throws Exception on IO errors
 	 */
 	public void writeOut(Task node) throws Exception {
 		/* get the path of the node and the task */
 		File path = this.getNodePath(node);
 		if (!node.isRoot()) FileSystemTask.saveTree(node, path);
 	}
 
 	/**
 	 * Handles the event of a node changing.
 	 * @param event the TreeModelEvent
 	 */
 	@Override
 	public void treeNodesChanged(TreeModelEvent event) {
 		/* at this point the TreeModel has already called setUserObject on the node;
 		 * get the value set by that function call and rename the task to that. */
 		
 		/* there may be several changed nodes; loop through all of them */
 		for (Object obj : event.getChildren()) {
 			/* must be node */
 			if (!(obj instanceof Task)) continue;
 			
 			/* the node's userObject must be set to a String */
 			Task node = (Task) obj;
 			if (!(node.getUserObject() instanceof String)) continue; /* TODO error? */
 			
 			/* rename */
 			this.rename(node, (String)node.getUserObject());
 		}
 	}
 
 	/**
 	 * Handles the event of a nodes being inserted; currently unused.
 	 * @param event the TreeModelEvent
 	 */
 	@Override
 	public void treeNodesInserted(TreeModelEvent event) {
 	}
 
 	/**
 	 * Handles the event of a nodes being removed; currently unused.
 	 * @param event the TreeModelEvent
 	 */
 	@Override
 	public void treeNodesRemoved(TreeModelEvent event) {
 	}
 
 	/**
 	 * Handles the event of the tree structure changing; currently unused.
 	 * @param event the TreeModelEvent
 	 */
 	@Override
 	public void treeStructureChanged(TreeModelEvent event) {
 	}
 }
