 package edu.ncsu.csc.realsearch.devsurvey;
 
 public enum ProjectTasks {
 	Development("Write code"), Testing("Write and/or execute tests"), Design("Design software"), Management(
 			"Manage people"), Inspection("Inspect other peoples' code"), FixBugs("Fix defects."), AnswerCustomers(
 			"Answer questions from customers"), AnswerDevelopment("Answer technical questions from fellow developers"), Steering(
			"Steer overall the direction of the project");
 
 	private final String description;
 
 	private ProjectTasks(String description) {
 		this.description = description;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 }
