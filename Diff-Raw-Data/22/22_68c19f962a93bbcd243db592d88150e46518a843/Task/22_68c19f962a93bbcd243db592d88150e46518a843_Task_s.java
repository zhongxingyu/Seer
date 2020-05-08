 //TODO Pull in what we had before, including id, name, crte/updt_dttm, due date, priority
 //DONE setup DB with tomcat and eclipse (email Colin)
 
 // in MVC configuration, this class is a Business Service
 package com.kain.tom.dioe;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Task {
 
 	private int taskId;
 	private String taskName;
 	private Date dueDate;
 	private Boolean isDone;
 	private static final String dateFormat = "MM/dd/yyyy";
 
 
 	// Auto-generated getters/setters
 	public int getTaskId() {
 		return taskId;
 	}
 
 	public void setTaskId(int taskId) {
 		this.taskId = taskId;
 	}
 
 	public String getTaskName() {
 		return taskName;
 	}
 
 	public void setTaskName(String taskName) {
 		this.taskName = taskName;
 	}
 
 	public Boolean getIsDone() {
 		return isDone;
 	}
 
 	public void setIsDone(Boolean isDone) {
 		this.isDone = isDone;
 	}
 
 	public Date getDueDate() {
 		return dueDate;
 	}
 
 	public String getDueDateFormatted() {
 		System.out.println(getTaskId() + " has a due date of " + getDueDate());
 		if (dueDate != null) {
 			DateFormat dateToStringFormat = new SimpleDateFormat(dateFormat);
 			return dateToStringFormat.format(dueDate);
 		} else return "No Due Date";
 	}
 
 	public void setDueDate(Date dueDate) {
 		this.dueDate = dueDate;
 	}
 	
 	public void setDueDateString(String stringToConvert) {
 		try {
 			dueDate = new SimpleDateFormat(dateFormat).parse(stringToConvert);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Task(String taskName) {
 		isDone = false;
 		this.taskName = taskName;
 	}
 
 	public Task() {
 	}
 	
 	public static String getDateformat() {
 		return dateFormat;
 	}
 
 }// end Task
