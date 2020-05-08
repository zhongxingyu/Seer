 package com.showrss;
 
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 public class Show {
 
 	public String showName;
 	public int showId;
 	public int hasHd = 0;
 	public int hasProper = 0;
 	
 	public Show(String showName, int showId) {
 		super();
 		this.showName = showName;
 		this.showId = showId;
 	}
 	
 	public Show(String showName) {
 		super();
 		this.showName = showName;
 		this.showId = Integer.parseInt(AllShows.showNameAsKey.get(showName));
 	}
 	
 	public Show(int showId) {
 		super();
 		this.showId = showId;
 		this.showName = AllShows.allshows.get(showId);
 	}
 	
 	@Override
 	public String toString() {
 		return "Show [showName=" + showName + ", showId=" + showId + "]";
 	}
 	
 		
 }
