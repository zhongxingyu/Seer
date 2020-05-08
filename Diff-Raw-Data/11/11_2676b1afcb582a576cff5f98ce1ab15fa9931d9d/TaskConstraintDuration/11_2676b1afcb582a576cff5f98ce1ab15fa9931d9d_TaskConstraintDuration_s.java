 package com.kangaroo.task;
 
 public class TaskConstraintDuration implements TaskConstraintInterface {
 
 	private int duration;
 	
 	
 	public TaskConstraintDuration(int duration) {
 		this.duration = duration;
 	}
 	
 	
 	public int getDuration() {
 		return duration;
 	}
 	
	
 	@Override
 	public String getType() {
 		return TaskConstraintInterface.TYPE_DURATION;
 	}
 
 }
