 package com.wedlum.styleprofile.domain.survey;
 
 import java.util.List;
 
 public class Session {
 
 	private final List<String> likes;
 	private final List<String> allPhotos;
 
 	public Session(List<String> likes, List<String> allColors) {
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
