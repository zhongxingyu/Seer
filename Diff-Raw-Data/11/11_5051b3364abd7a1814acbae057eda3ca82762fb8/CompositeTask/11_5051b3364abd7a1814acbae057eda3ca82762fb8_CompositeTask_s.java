 package org.cvut.wa2.projectcontrol.entities;
 
 import java.util.ArrayList;
 
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 
 @PersistenceCapable
 public class CompositeTask extends Task {
 	
 	@Persistent
	private String owner;
 	
 	@Persistent
 	private String responsible;
 	
 	@Persistent
 	private ArrayList<Task> subtasks;
 
 	public CompositeTask() {}
 	
	public CompositeTask(String owner, String responsible,
 			ArrayList<Task> subtasks) {
 		super();
 		this.owner = owner;
 		this.responsible = responsible;
 		this.subtasks = subtasks;
 	}
 
	public String getOwner() {
 		return owner;
 	}
 
	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	public String getResponsible() {
 		return responsible;
 	}
 
 	public void setResponsible(String responsible) {
 		this.responsible = responsible;
 	}
 
 	public ArrayList<Task> getSubtasks() {
 		return subtasks;
 	}
 
 	public void setSubtasks(ArrayList<Task> subtasks) {
 		this.subtasks = subtasks;
 	}
 	
 	
 
 }
