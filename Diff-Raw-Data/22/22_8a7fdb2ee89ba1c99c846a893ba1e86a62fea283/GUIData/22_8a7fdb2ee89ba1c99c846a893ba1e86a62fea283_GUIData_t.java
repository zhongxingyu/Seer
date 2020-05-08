 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 14, 2012
  * Author(s): NicolaiO
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.gui;
 
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Vector;
 
 import edu.dhbw.sos.course.Course;
 import edu.dhbw.sos.course.lecture.BlockType;
 import edu.dhbw.sos.course.lecture.Lecture;
 import edu.dhbw.sos.course.lecture.TimeBlock;
 
 
 /**
  * This class stores all objects, that are given to the GUI to display the corresponding data.
  * If you want to display something on the GUI, you should store an object here.
  * The GUI will receive this object (the reference!), so that it can be refreshed when calling update()
  * 
  * @author NicolaiO
  * 
  */
 public class GUIData {
 	private Vector<String>						profiles		= new Vector<String>();
 	private LinkedHashMap<String, String>	statistics	= new LinkedHashMap<String, String>();
 	private LinkedList<String>					suggestions	= new LinkedList<String>();
 	private Lecture lecture;
 	private Course course;
 	
 	
 	public GUIData() {
 		// dummy data
		lecture = new Lecture(new Date());
		lecture.getTimeBlocks().addTimeBlock(new TimeBlock(10,BlockType.theory));
		lecture.getTimeBlocks().addTimeBlock(new TimeBlock(20,BlockType.pause));
		lecture.getTimeBlocks().addTimeBlock(new TimeBlock(30,BlockType.exercise));
		lecture.getTimeBlocks().addTimeBlock(new TimeBlock(10,BlockType.pause));
		lecture.getTimeBlocks().addTimeBlock(new TimeBlock(30,BlockType.group));
 		
 		setCourse(new Course());
 		
 		profiles.add("Profile0");
 		profiles.add("Profile1");
 		
 		for (int i = 0; i < 5; i++) {
 			statistics.put("Test" + i, "" + i * 42);
 		}
 		
 		suggestions.add("Sug1");
 		suggestions.add("Sug2");
 		suggestions.add("Sug3");
 		suggestions.add("Sug4");
 	}
 	
 	
 	public Vector<String> getProfiles() {
 		return profiles;
 	}
 	
 	
 	public void setProfiles(Vector<String> profiles) {
 		this.profiles = profiles;
 	}
 	
 	
 	public LinkedHashMap<String, String> getStatistics() {
 		return statistics;
 	}
 	
 	
 	public void setStatistics(LinkedHashMap<String, String> statistics) {
 		this.statistics = statistics;
 	}
 	
 	
 	public LinkedList<String> getSuggestions() {
 		return suggestions;
 	}
 	
 	
 	public void setSuggestions(LinkedList<String> suggestions) {
 		this.suggestions = suggestions;
 	}
 
 
 	public Lecture getLecture() {
 		return lecture;
 	}
 
 
 	public void setLecture(Lecture lecture) {
 		this.lecture = lecture;
 	}
 
 
 	/**
 	 * @return the course
 	 */
 	public Course getCourse() {
 		return course;
 	}
 
 
 	/**
 	 * @param course the course to set
 	 */
 	public void setCourse(Course course) {
 		this.course = course;
 	}
 }
