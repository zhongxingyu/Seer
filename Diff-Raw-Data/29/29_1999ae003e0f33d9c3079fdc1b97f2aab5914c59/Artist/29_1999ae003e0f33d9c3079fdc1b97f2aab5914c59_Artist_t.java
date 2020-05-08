 package edu.uib.info310.model;
 
 import java.util.List;
 import java.util.Map;
 
 public interface Artist {
 	
 	public String getId();
 	
 	public String getName();
 	
 	public String getSafeName();
 	
 	public String getShortDescription();
 	
 	public String getDescription();
 	
 	public String getBio();
 	
 	public String getImage();
 	
 	public List<Record> getDiscography();
 	
 	public List<Artist> getSimilar();
 	
 	public List<Event> getEvents();
 	
 	public Map<String, String> getMeta();
 	
	public List<Event> getLocatedEvents();
 }
