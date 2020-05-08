 package edu.unsw.cse.comp9323.group1.models;
 
 public class Rating {
	private Integer studentID;
 	private String courseName;
 	private Integer rating;
 	private String category;
 	
 	
 	public String getCategory() {
 		return category;
 	}
 	public void setCategory(String category) {
 		this.category = category;
 	}
	public Integer getStudentID() {
 		return studentID;
 	}
	public void setStudentID(Integer studentID) {
 		this.studentID = studentID;
 	}
 	public String getCourseName() {
 		return courseName;
 	}
 	public void setCourseName(String courseName) {
 		this.courseName = courseName;
 	}
 	public Integer getRating() {
 		return rating;
 	}
 	public void setRating(Integer rating) {
 		this.rating = rating;
 	}
 	
 
 }
