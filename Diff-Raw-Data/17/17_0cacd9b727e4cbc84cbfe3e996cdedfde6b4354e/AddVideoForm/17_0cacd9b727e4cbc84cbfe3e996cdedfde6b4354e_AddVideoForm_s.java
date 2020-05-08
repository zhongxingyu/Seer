 package edu.cmu.cs15437.clubwebsite.formbeans;
 
 import org.mybeans.form.FormBean;
 
 import java.util.*;
 import java.util.regex.*;
 
 public class AddVideoForm extends FormBean {
 	private String link			= null;
 	private String description	= null;
 	
 	private static Pattern pattern = Pattern.compile("v=([^&]{11})(?:&.*)?$");
 	
 	public String getLink()			{ return link;			}
 	public String getDescription()	{ return description;	}
 	
	public void setLink(String s) 		{ link = s;										}
	public void setComment(String s)	{ description = trimAndConvert(s, "<>&\'\"");	}
 	
 	public String extractVideoId() {
		System.out.println(link);
 		Matcher m = pattern.matcher(link);
 		if (m.find())
 			return m.group(1);
 		else
 			return null;
 	}
 	
 	public List< String > getValidationErrors() {
 		List< String > errors = new ArrayList< String >();
 		
 		if (link == null || link.length() == 0) {
 			errors.add("Video link cannot be empty");
 		} else if (extractVideoId() == null) {
 			errors.add("We can't process this video link at the moment. Contact us for more info.");
 		}
 		
 		return errors;
 	}
 }
