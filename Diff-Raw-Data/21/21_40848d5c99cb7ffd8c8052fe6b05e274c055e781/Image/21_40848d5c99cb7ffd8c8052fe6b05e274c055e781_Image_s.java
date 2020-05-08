 package edu.uib.info323.model;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class Image {
 	private String imageUri;
 	private String pageUri;
 	
 	public String getImageUri() {
 		return imageUri;
 	}
 	public void setImageUri(String imageUri) {
 		this.imageUri = imageUri;
 	}
 	public String getPageUri() {
 		return pageUri;
 	}
 	public void setPageUri(String pageUri) {
 		this.pageUri = pageUri;
		
 	}
 
 }
