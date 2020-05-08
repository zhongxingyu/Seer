 package com.uwusoft.timesheet.model;
 
 import java.sql.Timestamp;
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 @Entity
 public class Task {
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	private Timestamp dateTime;
 	private String task;
     @ManyToOne
 	private Project project;
 	private float total=0;
 	private boolean wholeDay=false;	
 	
 	/**
 	 * JPA requires a no-arg constructor
 	 */
 	protected Task() {
 	}	
 
 	/**
 	 * @param dateTime
 	 * @param task
 	 */
 	public Task(Date dateTime, String task) {
 		this.dateTime = new Timestamp(dateTime.getTime());
 		this.task = task;
 	}
 	
 	/**
 	 * @param dateTime
 	 * @param task
 	 * @param project
 	 */
 	public Task(Date dateTime, String task, Project project) {
 		this(dateTime, task);
 		this.project = project;
 	}
 
 	/**
 	 * @param id
 	 * @param dateTime
 	 * @param task
 	 * @param project
 	 */
 	public Task(Long id, Date dateTime, String task, float total, Project project) {
 		this(dateTime, task, project);
 		this.id = id;
 		this.total = total;
 	}
 
 	/**
 	 * @param dateTime
 	 * @param task
 	 * @param total
 	 */
 	public Task(Date dateTime, String task, float total) {
 		this(dateTime, task);
 		this.total = total;
 	}
 
 	/**
 	 * @param dateTime
 	 * @param task
 	 * @param project
 	 * @param total
 	 */
 	public Task(Date dateTime, String task, Project project, float total) {
 		this(dateTime, task, project);
 		this.total = total;
 	}
 	
 	/**
 	 * @param dateTime
 	 * @param task
 	 * @param total
 	 * @param wholeDay
 	 */
 	public Task(Date dateTime, String task, float total, boolean wholeDay) {
 		this(dateTime, task, total);
 		this.wholeDay = wholeDay;
 	}	
 	
 	/**
 	 * @return the id
 	 */
 	public Long getId() {
 		return id;
 	}
 
 	public Timestamp getDateTime() {
 		return dateTime;
 	}
 
 	public void setDateTime(Timestamp dateTime) {
 		this.dateTime = dateTime;
 	}
 
 	public String getTask() {
 		return task;
 	}
 	
 	public void setTask(String task) {
 		this.task = task;
 	}
 
 	public float getTotal() {
 		return total;
 	}
 
 	public void setTotal(float total) {
 		this.total = total;
 	}
 
 	public Project getProject() {
 		return project;
 	}
 
 	public void setProject(Project project) {
 		this.project = project;
 	}
 
 	public boolean isWholeDay() {
 		return wholeDay;
 	}
 
 	@Override
 	public String toString() {
		return "Task [Date=" + dateTime + ", task=" + task + " (project: " + project.getName() + ", system: " + project.getSystem() + "), total=" + total + ", wholeDay=" + wholeDay + "]";
 	}
 }
