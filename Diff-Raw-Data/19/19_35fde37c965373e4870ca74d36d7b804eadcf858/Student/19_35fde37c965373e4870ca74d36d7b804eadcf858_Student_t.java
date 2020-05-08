 package edu.gatech.arktos;
 
 import java.util.ArrayList;
 
 /**
  * Student class
  * @author Pujita
  *
  */
 public class Student {
 	private String name;
 	private String gtid;
 	private String email;
 	private int attendance;	
 	private ArrayList<Assignment> assignments;
 	private int averageAssignmentGrade;
 	private ArrayList<ProjectTeam> projects;
 	
 	
 	public Student(String name, String gtid, String email, int attendance)
 	{
 		this.name = name;
 		this.gtid = gtid;
 		this.email = email;
 		this.attendance = attendance;
 		projects = new ArrayList<ProjectTeam>();
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public void setGtid(String gtid) {
 		this.gtid = gtid;
 	}
 	
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	
 	public void setAttendance(int attendance) {
 		this.attendance = attendance;
 	}
 	
 	
 	public String getName() {
 		return name;
 	}
 
 	public String getGtid() {
 		return gtid;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 
 	public int getAttendance() {
 		return attendance;
 	}
 
 	public ArrayList<Integer> getAssignmentGrades() {
 		ArrayList<Integer> assignmentGrades = new ArrayList<Integer>();
 		
		if (assignments == null) return assignmentGrades;
		
 		for (Assignment a: assignments) {
 			assignmentGrades.add( GradesDB.convertToInt(a.getGrade()));
 		}
 		
 		return assignmentGrades;
 	}
 	
 	public ArrayList<Assignment> getAssignments() {
 		return assignments;
 	}
 
 	public void setAssignments(ArrayList<Assignment> assignments) {
 		this.assignments = assignments;
 	}
 
 	public ArrayList<ProjectTeam> getProjects() {
 		return projects;
 	}
 
 	public void addProjects(ProjectTeam projects) {
 		this.projects.add(projects);
 	}
 
 	public int getAverageAssignmentGrade() {
 		return averageAssignmentGrade;
 	}
 
 	public void setAverageAssignmentGrade(int averageAssignmentGrade) {
 		this.averageAssignmentGrade = averageAssignmentGrade;
 	}
 	
 	public String toString() {
 		return getName();
 	}
 
 }
