 package com.wedlum.styleprofile.domain.survey;
 
 import java.util.List;
 
import org.apache.commons.lang3.Validate;

 public class Session {
 
 	private final List<String> likes;
 	private final List<String> allPhotos;
 
 	public Session(List<String> likes, List<String> allColors) {
		Validate.notNull(likes, "likes is required");
		Validate.notNull(allColors, "allColors is required");

 		this.likes = likes;
 		this.allPhotos = allColors;
 	
 	}
 
 	public List<String> getLikes() {
 		return likes;
 	}
 
 	public List<String> getAllPhotos() {
 		return allPhotos;
 	}
 
 }
