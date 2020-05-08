 package quizsite.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A class representing an activity that has taken place
  */
 public class Activity {
 	/* Instance variables */
 	private int userId;
 	private String userName;
 	private String date;
 	private String verb;
 	private String subject;
 	
 	public Activity(int userId, String userName, String date, String verb, String subject) {
 		System.err.println("(Activity Constructor: line 18) Making an activity with userId: " + userId);
 		this.userId = userId;
 		this.userName = userName;
 		this.date = date;
 		this.verb = verb;
 		this.subject = subject;
 	}
 	
 	public int getUserId() {
 		return userId;
 	}
 	
 	public String getUser() {
 		return userName;
 	}
 	
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 	
 	public String getDate() {
 		return date;
 	}
 	
 	public String getVerb() {
 		return verb;
 	}
 	
 	public String subject() {
 		return subject;
 	}
 	
 	/**
 	 * Takes in a list of persistent models and returns a list of equivalent activities
 	 */
 	public static <T extends PersistentModel> List<Activity> toActivityList(List<T>models) {
 		List<Activity> activities = new ArrayList<Activity>();
 		for(PersistentModel model : models) {
 			activities.add(model.getActivity());
 		}
 		return activities;
 	}
 	
 	/**
 	 * Returns a print string for the given activity
 	 */
 	public String getActivityPrintString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<div class='activity'>\n");
 		sb.append("<p>\n");
		sb.append("<a href='display_user.jsp?userId="+userId+"'>"+userName+"</a>");
 		sb.append(" " + verb + " " + subject + " on " + date + ".\n");
 		sb.append("</p>\n");
 		sb.append("</div>");
 		
 		return sb.toString();
 	}
 }
