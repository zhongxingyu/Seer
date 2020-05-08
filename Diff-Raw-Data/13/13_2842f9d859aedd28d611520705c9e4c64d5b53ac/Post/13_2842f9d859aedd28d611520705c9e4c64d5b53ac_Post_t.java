 package models;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import com.petebevin.markdown.MarkdownProcessor;
 
 /**
  * The Class Votable delivers functionality for all objects that are votable
  * (e.g. answers and questions).
  */
 public abstract class Post {
 
 	protected int id;
 	protected String content;
 	protected User owner;
 	protected int score = 0;
 
 	protected static DbManager manager = DbManager.getInstance();
 	protected Calendar calendar = Calendar.getInstance();
 	protected java.sql.Timestamp currentTimestamp;
 
 	/**
 	 * Vote for a votable.
 	 * 
 	 * @param newVote
 	 *            - The vote you want to add. This is an String-object
 	 *            containing an integer number.
 	 */
 	public void vote(String newVote) {
 		int vote = Integer.parseInt(newVote);
 		score = score + vote;
 		this.setLastChanged(new Date());
 	}
 
 	/**
 	 * @return parsed markdown string, so either plain text or HTML.
 	 */
 	// TODO do not create a new instance for every call, how?
 	public String getHtml() {
 		return new MarkdownProcessor().markdown(content);
 	}
 
 	/**
	 * Checks if the user with the id #uid is the owner of the votable. 
 	 * 
 	 * @param uid
 	 *            numeric user id as string
	 * @return true, if successful and false if either the uid isn't the owner of the post or if the uid can't be parsed to an integer value.
 	 */
 	public boolean ownerIs(String uid) {
		
		try {
			return owner.getId() == Integer.parseInt(uid);
		} catch (NumberFormatException e) {
			return false;
		}
 	}
 
 	/**
 	 * Checks if the user with the id #uid is the owner of the votable.
 	 * 
 	 * @param uid
 	 *            the uid
 	 * @return true, if successful
 	 */
 	public boolean ownerIs(int uid) {
 		return owner.getId() == uid;
 	}
 
 	public String toString() {
 		return content + "\n by " + owner;
 	}
 
 	/** Getter methods */
 	public int getId() {
 		return id;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public String getTimestamp() {
 		return currentTimestamp.toString();
 	}
 
 	public java.sql.Timestamp getDate() {
 		return currentTimestamp;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public User getOwner() {
 		return owner;
 	}
 
 	/** Setter methods */
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public void setId(int id) {
 		this.id = id;
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
 
 	/**
 	 * Invokes the method setLastChangedDate of the question the post relates to
 	 * (also itself) to set the date of the last change.
 	 * 
 	 * @param date
 	 *            - the date when the question was last changed.
 	 */
 	public void setLastChanged(Date date) {
 		if (this instanceof Question) {
 			manager.getQuestionById(this.id).setLastChangedDate(date);
 		} else if (this instanceof Answer) {
 			int questionId = manager.getAnswerById(id).getQuestionId();
 			manager.getQuestionById(questionId).setLastChangedDate(date);
 		}
 	}
 }
