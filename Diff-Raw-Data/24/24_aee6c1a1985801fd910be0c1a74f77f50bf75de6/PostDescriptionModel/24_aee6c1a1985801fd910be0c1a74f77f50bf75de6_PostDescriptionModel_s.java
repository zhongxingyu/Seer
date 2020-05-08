 package com.ramselabs.education.model;
 
 import java.util.Comparator;
 import java.util.Date;
 
 public class PostDescriptionModel implements Comparator<PostDescriptionModel>{
 	private String personName;
 	private String postDescription;
 	private String userType;
 	private Date dateOfPosting;
 	private String userImage;
 	
 	public String getUserImage() {
 		return userImage;
 	}
 	public void setUserImage(String userImage) {
 		this.userImage = userImage;
 	}
 	public String getUserType() {
 		return userType;
 	}
 	public void setUserType(String userType) {
 		this.userType = userType;
 	}
 	public Date getDateOfPosting() {
 		return dateOfPosting;
 	}
 	public void setDateOfPosting(Date dateOfPosting) {
 		this.dateOfPosting = dateOfPosting;
 	}
 	public String getPersonName() {
 		return personName;
 	}
 	public void setPersonName(String personName) {
 		this.personName = personName;
 	}
 	public String getPostDescription() {
 		return postDescription;
 	}
 	public void setPostDescription(String postDescription) {
 		this.postDescription = postDescription;
 	}
 	@Override
 	public int compare(PostDescriptionModel o1, PostDescriptionModel o2) {
 		return o2.getDateOfPosting().compareTo(o1.getDateOfPosting());
 	}
 	@Override
 	public String toString(){
 		return postDescription;
 	}
     
 }
