 /* FileSystemTask.java - Part of Task Mistress
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 /**
  * A sub-class of Task, which adds file system storage features to the task.
  * @author anonpds <anonpds@gmail.com>
  */
 @SuppressWarnings("serial")
 public class FileSystemTask extends Task {
 	/** Configuration variable name for the task name. */
 	private static final String CONFIG_NAME = "name";
 	
 	/** Configuration variable name for the task creation time. */
 	private static final String CONFIG_CREATION_TIME = "creation_time";
 	
 	/** Configuration variable for the task status. */
 	private static final String CONFIG_STATUS = "status";
 
 	/** The plain name of the task. */
 	private static final String CONFIG_PLAIN_NAME = "plain_name";
 
 	/** The name of the file that contains task meta data. */
 	private static final String META_FILE = "task.cfg";
 
 	/** The name of the file that contains task text. */
 	private static final String TEXT_FILE = "task.txt";
 
 	/** The file containing the index of a nodes children. */
 	private static final String INDEX_FILE = "children.ndx";
 
 	/** Constructs an empty FileSystemTask node. Useful as the root of a task tree. */
 	public FileSystemTask() {
 		super();
 	}
 	
 	/**
 	 * Default constructor; creates an empty task. 
 	 * @param parent the parent of this task node
 	 */
 	public FileSystemTask(Task parent) {
 		super(parent, null, null, 0, false);
 	}
 	
 	/**
 	 * Constructs a new Task with the given parameters.
 	 * @param parent the parent of this task node
 	 * @param name the name of the task node
 	 * @param text the text of the task node
 	 * @param timeStamp the time stamp of the task node
 	 * @param dirty the dirty status of the task node
 	 * @param plainName the plain name of the task node
 	 */
 	public FileSystemTask(Task parent, String name, String text, long timeStamp, boolean dirty) {
 		super(parent, name, text, timeStamp, dirty);
 	}
 	
 	/**
 	 * Loads a tree of tasks.
 	 * @param tree the root node under which to load the tasks
 	 * @param path the directory path to load from
 	 * @throws Exception on any IO or parse errors
 	 */
 	public static void loadTree(Task tree, File path) throws Exception {
 		/* must be a directory */
 		if (!path.isDirectory()) throw new Exception("'" + path.getPath() + "' not a directory");
 
 		File indexFile = new File(path, INDEX_FILE);
 		if (indexFile.exists()) {
 			/* the index file exists, load nodes based on the index */
 			BufferedReader reader = new BufferedReader(new FileReader(indexFile));
 			String line;
 			
 			/* the index is just a list of sub-directories, one per line */
 			while ((line = reader.readLine()) != null) {
 				if (line.length() == 0) continue;
 				File taskFile = new File(path, line);
 				Task task = FileSystemTask.load(taskFile);
				if (task != null) tree.add(task);
				
				/* add the children recursively */
				loadTree(task, taskFile);
 			}
 			reader.close();
 		} else {
 			/* the node does not exist, load nodes from all suitable sub-directories */
 			/* TODO remove support for this in the future */
 			File[] files = path.listFiles();
 			for (File file : files) {
 				if (file.isDirectory()) {
 					Task task = FileSystemTask.load(file);
					if (task != null) tree.add(task);
					
					/* recursively add the children */
					loadTree(task, file);
 				}
 			}
 			/* mark the node dirty, so the task will be saved again and an index will be written */
 			tree.setDirty(true);
 		}
 	}
 	
 	/**
 	 * Loads a task from disk.
 	 * @param path the directory path to load the task from
 	 * @return the loaded Task or null if the directory is not a task directory
 	 * @throws Exception on errors
 	 */
 	public static FileSystemTask load(File path) throws Exception {
 		/* make sure the path exists and is a directory */
 		if (!path.exists() || !path.isDirectory()) throw new Exception(path.getPath() + " does not exist.");
 
 		FileSystemTask task = new FileSystemTask();
 		
 		/* get the file system name from the path */
 		String plainName = path.getName();
 		task.setPlainName(plainName);
 
 		/* read the meta data; if the meta data file does not exist, the path does not contain a task */
 		String name = null, date = null, status;
 		File metaFile = new File(path, META_FILE);
 		if (!metaFile.exists()) return null;
 
 		/* use the fancy Configuration class to read and parse the meta data variables */
 		Configuration conf = Configuration.parse(metaFile);
 		name = conf.get(CONFIG_NAME);
		/* plain name is taken from the directory name, so no: plainNAme = conf.get(CONFIG_PLAIN_NAME); */
 		date = conf.get(CONFIG_CREATION_TIME);
 		status = conf.get(CONFIG_STATUS);
 		
 		/* validate and parse the variables */
 		if (name == null) throw new Exception("no name in metadata " + metaFile.getPath());
 		if (date == null) throw new Exception("no date in metadata " + metaFile.getPath());
 		long timeStamp = Long.parseLong(date);
 
 		/* set the name, creation date and status */
 		task.setName(name);
 		task.setCreationTime(timeStamp);
 		try { task.setStatus(Short.parseShort(status)); } catch (Exception e) {}
 		
 		/* read the task text if it exists */
 		File textFile = new File(path, TEXT_FILE);
 		if (textFile.exists()) {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), "UTF-8"));
 			String text = "", line;
 			while ((line = reader.readLine()) != null) {
 				if (text.length() > 0) text = text + "\n";
 				text = text + line;
 			}
 			task.setText(text);
 			reader.close();
 		} else task.setText(null);
 		
 		/* clear the dirty flag, since the Task was just read from disk */
 		task.setDirty(false);
 
 		return task;
 	}
 	
 	/**
 	 * Saves a tree of tasks to disk.
 	 * @param tree the root node of the tree to write out
 	 * @param path the path to write to
 	 * @return the number of tasks actually written to disk
 	 * @throws Exception on IO errors
 	 */
 	public static int saveTree(Task tree, File path) throws Exception {
 		int numSaved = 0;
 
 		/* Don't save the root node, but save an index of its children */
 		if (!tree.isRoot()) { if (((FileSystemTask)tree).save(path)) numSaved++;
 		} else saveIndex(tree, path);
 		
 		/* recurse for each of the child nodes */
 		for (int i = 0; i < tree.getChildCount(); i++) {
 			Task child = (Task) tree.getChildAt(i);
 			File newPath = new File(path, child.getPlainName());
 			numSaved += saveTree(child, newPath);
 		}
 		
 		return numSaved;
 	}
 
 	/**
 	 * Saves the Task to disk.
 	 * @param path the path to save the task to
 	 * @return true if the task was saved, false if it hasn't changed since last save and thus wasn't written out
 	 * @throws Exception on IO errors
 	 */
 	public boolean save(File path) throws Exception {
 		/* create the path if it doesn't exist */
 		if (!path.exists() && !path.mkdirs()) throw new Exception("can not create " + path);
 
 		/* write the node only if dirty */
 		if (!this.isDirty()) return false;
 		
 		/* write the meta data in new format only */
 		File metaFile = new File(path, META_FILE);
 		Configuration conf = new Configuration();
 		conf.add(CONFIG_NAME, this.getName());
 		conf.add(CONFIG_PLAIN_NAME, this.getPlainName());
 		conf.add(CONFIG_CREATION_TIME, this.getCreationTime());
 		conf.add(CONFIG_STATUS, this.getStatus());
 		conf.store(metaFile);
 		
 		/* write the task text, if any */
 		File textFile = new File(path, TEXT_FILE);
 		try {
 			PrintWriter writer = new PrintWriter(textFile, "UTF-8");
 			if (this.getText() != null) writer.print(this.getText());
 			writer.close();
 		} catch (Exception e) {
 			throw new Exception("can not write to " + textFile.getPath() + ": " + e.getMessage());
 		}
 		
 		/* save an index of the node's children */
 		saveIndex(this, path);
 		
 		/* clear the dirty flag, since the task was just saved */
 		this.setDirty(false);
 		return true;
 	}
 
 	/**
 	 * Saves an index of the tasks children.
 	 * @param task the task of which to write the index
 	 * @param path the path to write the index to
 	 * @throws Exception on IO errors
 	 */
 	public static void saveIndex(Task task, File path) throws Exception {
 		File indexFile = new File(path, INDEX_FILE);
 		PrintWriter writer = new PrintWriter(indexFile);
 		for (int i = 0; i < task.getChildCount(); i++) {
 			Task child = (Task) task.getChildAt(i);
 			writer.println(child.getPlainName());
 		}
 		writer.close();
 	}
 
 	
 	/**
 	 * Removes the task files from a directory.
 	 * @param path the directory path from which to remove the files
 	 */
 	public static void removeTaskFiles(File path) {
 		File metaFile = new File(path, META_FILE);
 		File textFile = new File(path, TEXT_FILE);
 		
 		if (metaFile.exists()) metaFile.delete();
 		if (textFile.exists()) textFile.delete();
 	}
 }
