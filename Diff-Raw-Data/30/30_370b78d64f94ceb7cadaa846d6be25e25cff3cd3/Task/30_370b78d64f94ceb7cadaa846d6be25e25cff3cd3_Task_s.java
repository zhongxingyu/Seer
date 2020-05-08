/* Task Mistress - Task.java
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 /* TODO this class should only implement the bare Task features, such as name and text. All the other features should
  * be implemented by the storage classes sub-classing this class.
  */
 
 /**
  * Class that implements a task tree node.
  * @author anonpds <anonpds@gmail.com>
  */
 class Task {
 	/** Tells whether the task has changed since last write to disk. */
 	private boolean dirty;
 	
 	/** The name of the task. */
 	private String name;
 	
 	/** The time stamp of the task creation. */
 	private long timeStamp;
 
 	/** The plain name of the task, which is used when writing the task to disk. */
 	private String plainName;
 
 	/** The text of the task. */
 	private String text;
 
 	/**
 	 * Constructs a new task object.
 	 * @param name the name of the task 
 	 * @param plainName the plain name of the task
 	 * @param text the text of the task
 	 * @param timeStamp the creation time stamp of the task
 	 * @param dirty true if the task is newly created, false if it was loaded from disk
 	 */
 	public Task(String name, String plainName, String text, long timeStamp, boolean dirty) {
 		this.name = name;
 		this.plainName = plainName;
 		this.text = text;
 		this.timeStamp = timeStamp;
 		this.dirty = dirty;
 	}
 	
 	/**
 	 * Tells whether the node was modified since it was last written out.
 	 * @return true if the node has unsaved changes, false it not
 	 */
 	public boolean isDirty() {
 		return this.dirty;
 	}
 
 	/**
 	 * Sets the node to dirty or non-dirty. Only dirty nodes are saved to disk when the tree is written out.
 	 * @param dirty true to set the node dirty, false for non-dirty
 	 */
 	public void setDirty(boolean dirty) {
 		this.dirty = dirty;
 	}
 
 	/**
 	 * Sets the creation time of the node.
 	 * @param timeStamp the time of the node's creation
 	 */
 	/* TODO remove this? The creation time should be immutable
 	public void setCreationTime(long timeStamp) {
 		this.timeStamp = timeStamp;
 		this.dirty = true;
 	}
 	*/
 	
 	/**
 	 * Returns the node creation time.
 	 * @return the creation time stamp
 	 */
 	public long getCreationTime() {
 		return this.timeStamp;
 	}
 
 	/**
 	 * Sets the name of the node.
 	 * @param name the new name of the node
 	 */
 	public void setName(String name) {
 		if (name.compareTo(this.name) != 0) {
 			this.dirty = true;
 			this.name = name;
 		}
 	}
 
 	/**
 	 * Returns the name of the node.
 	 * @return the name of the node
 	 */
 	public String getName() {
 		return this.name;
 	}
 	
 	/**
 	 * Sets the plain name of the node.
 	 * @param name the file system name
 	 */
 	public void setPlainName(String name) {
 		this.plainName = name;
 	}
 
 	/**
 	 * Returns the plain name of the node.
 	 * @return the file system name of the node
 	 */
 	public String getPlainName() {
 		return this.plainName;
 	}
 
 	/**
 	 * Returns the text of the node.
 	 * @return the text of the node
 	 */
 	public String getText() {
 		return this.text;
 	}
 
 	/**
 	 * Sets the text of the node.
 	 * @param text the text to set
 	 */
 	public void setText(String text) {
 		if (text.compareTo(this.text) != 0) {
 			this.dirty = true;
 			this.text = text;
 		}
 	}
 	
 	/**
 	 * Returns the string representation of this object.
 	 * @return the string
 	 */
 	public String toString() {
 		return this.name;
 	}
 }
