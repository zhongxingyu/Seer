 package models;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.Date;
 
 public abstract class Votable {
 
 	protected int id;
 	protected String content;
 	protected User owner;
 	protected int score = 0;
 	protected static UserQuestionAnswerManager userQuestionAnswerManager = UserQuestionAnswerManager
 			.getInstance();
 
 	protected Calendar calendar = Calendar.getInstance();
 	protected java.sql.Timestamp currentTimestamp;
 
 	public void vote(String newVote) {
 		int vote = Integer.parseInt(newVote);
 		score = score + vote;
 	}
 
 	public String toString() {
 		return content + "\n by " + owner;
 	}
 
 	public java.sql.Timestamp getDate() {
 		return currentTimestamp;
 	}
 
 	public void setId(String id) {
 		this.id = Integer.parseInt(id);
 
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public User getOwner() {
 		return owner;
 	}
 	
	public boolean isOwner(int uid) {
 		return owner.getId() == uid;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public int getScore() {
 		return score;
 	}
 	
 	public String getTimestamp() {
         return currentTimestamp.toString();
 	}
 
 	public void setTimeStamp(String timeStamp) throws ParseException {
 		DateFormat formatter;
 		Date date;
 
 		formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.SSS");
 		date = (Date) formatter.parse(timeStamp);
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 
 		this.currentTimestamp = new java.sql.Timestamp(cal.getTime().getTime());
 	}
 
 }
