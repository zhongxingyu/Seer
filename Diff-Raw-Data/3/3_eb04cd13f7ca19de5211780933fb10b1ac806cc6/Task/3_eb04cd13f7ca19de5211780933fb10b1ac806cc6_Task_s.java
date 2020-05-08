 /* Task.java - Part of Task Mistress
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 /**
  * Class that implements a task tree node.
  * @author anonpds <anonpds@gmail.com>
  */
 class Task {
 	/** The status code for task that has been done. */
 	public static final short STATUS_DONE = 1;
 
 	/** The status code for task that has not been done. */
 	public static final short STATUS_UNDONE = 2;
 
 	/** The status code for task that doesn't have done/undone status. */
 	public static final short STATUS_DEFAULT = 3;
 
 	/** Tells whether the task has changed since last write to disk. */
 	private boolean dirty;
 	
 	/** The name of the task. */
 	private String name;
 	
 	/** The time stamp of the task creation. */
 	private long timeStamp;
 
 	/** The text of the task. */
 	private String text;
 
 	/** The status of the task; done, undone or default. */
 	private short status;
 	
 	/**
 	 * Constructs a new task object.
 	 * @param name the name of the task 
 	 * @param text the text of the task
 	 * @param timeStamp the creation time stamp of the task
 	 * @param dirty true if the task is newly created, false if it was loaded from disk
 	 */
 	public Task(String name, String text, long timeStamp, boolean dirty) {
 		this.name = name;
 		this.text = text;
 		this.timeStamp = timeStamp;
 		this.dirty = dirty;
 		this.status = STATUS_DEFAULT;
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
 	 * Returns the node creation time.
 	 * @return the creation time stamp
 	 */
 	public long getCreationTime() {
 		return this.timeStamp;
 	}
 	
 	/**
 	 * Sets the creation time of the task.
 	 * @param timeStamp the creation time
 	 */
 	public void setCreationTime(long timeStamp) {
 		this.timeStamp = timeStamp;
 	}
 
 	/**
 	 * Sets the name of the node.
 	 * @param name the new name of the node
 	 */
 	public void setName(String name) {
 		if (this.name == name) return;
 		
 		if (name == null || this.name == null || (this.name != null && name.compareTo(this.name) != 0)) {
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
 		if (this.text == text) return;
 		
 		if (text == null || this.text == null || (this.text != null && text.compareTo(this.text) != 0)) {
 			this.dirty = true;
 			this.text = text;
 		}
 	}
 	
 	/**
 	 * Returns the status of the task.
 	 * @return the status of the task
 	 */
 	public short getStatus() {
 		return this.status;
 	}
 	
 	/**
 	 * Sets the status of the task.
 	 * @param status the new status of the task
 	 */
 	public void setStatus(short status) {
 		if (status != STATUS_DONE && status != STATUS_UNDONE && status != STATUS_DEFAULT) return; /* TODO error */
 		this.status = status;
 	}
 	
 	/**
 	 * Returns the string representation of this object.
 	 * @return the string
 	 */
 	public String toString() {
 		return this.name;
 	}
 }
