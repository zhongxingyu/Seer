 package com.mindbadger.jaluxx;
 
 public enum GameStatus {
 	SETUP ("Setup"),
 	READY_TO_PLAY ("Finished"),
 	PLAYING ("Playing");
 	
 	private String statusText;
 	
 	private GameStatus (String statusText) {
 		this.statusText = statusText;
 	}
 	
	public String getText () {
 		return statusText;
 	}
 }
