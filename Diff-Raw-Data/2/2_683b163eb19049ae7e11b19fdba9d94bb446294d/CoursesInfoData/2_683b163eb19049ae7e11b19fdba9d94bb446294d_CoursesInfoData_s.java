 package com.cromiumapps.uwhub;
 
 import java.text.ParseException;
 
 import android.content.Context;
 
 public class CoursesInfoData {
 
 	//private
 	Context context;
 	
 	String dept = null;
 	String number = null;
 	String title = null;
 	String description = null;
 	
 	public CoursesInfoData (String inDept, String inNumber, String inTitle, String inDesc) {
 	    dept = inDept;
 	    number = inNumber;
 		title = inTitle;
 	    description = inDesc;
 	}
 	
 	public String getDept() {
 		return dept;
 	}
 	
 	public String getNumber() {
 		return number;
 	}
 	
 	public String getTitle() {
 		return title;
 	}
 	
 	public String getDescription() {
 		return removeOfferings(description);
 	}
 	
 	private String removeOfferings(String string) {
	    string = string.replaceAll("\\[Offered: [FWS]\\]", "");
 	    return string;
 	}
 }
