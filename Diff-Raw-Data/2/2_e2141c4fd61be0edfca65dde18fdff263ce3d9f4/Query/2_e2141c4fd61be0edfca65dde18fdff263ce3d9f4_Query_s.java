 package core;
 
 import java.util.Date;
 
 /**
  * The fundamental object representing a users request from reception, until
  * response and logging. Any and all relevant information should probably be
  * stored and accessible in this class.
  */
 public class Query {
 
 	/** The time the request was received. */
 	private final Date timeReceived;
 
	/** The time it the response finished sending. */
 	private Date timeResponded;
 
 	/** The body of the received text message. */
 	private final String body;
 
 	/** The phone number the text message came from. */
 	private final String phoneNumber;
 
 	/** A system recognized keyword or null. */
 	private final String keyword;
 
 	/** The appropriate response to the message. */
 	private String response;
 
 	public Query(Date timeReceived, String body, String phoneNumber) {
 		this.timeReceived = timeReceived;
 		this.body = body;
 		this.keyword = Keywords.instance().extract(body);
 		this.phoneNumber = phoneNumber;
 		
 	}
 	
 	public String getBody() {
 		return body;
 	}
 
 	public String getKeyword() {
 		return keyword;
 	}
 
 	public String getPhoneNumber() {
 		return phoneNumber;
 	}
 
 	public String getResponse() {
 		return response;
 	}
 
 	public Date getTimeReceived() {
 		return timeReceived;
 	}
 
 	public Date getTimeResponded() {
 		return timeResponded;
 	}
 
 	public void setResponse(String response) {
 		this.response = response;
 	}
 
 	public void setTimeResponded(Date timeResponded) {
 		this.timeResponded = timeResponded;
 	}
 
 }
