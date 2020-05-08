package agents.test.mock;
 
 import java.text.DateFormat;
 import java.util.Date;
 
 /**
  * Represents a single LoggedEvent. A LoggedEvent is a textual message, and the
  * time at which the event occured.
  * 
  * @author Sean Turner
  * 
  */
 public class LoggedEvent {
 
 	/**
 	 * Represents the date and time at which the event occurred. This is useful
 	 * when you need to order events in chronological order.
 	 */
 	private Date timestamp;
 
 	/**
 	 * This is the body of the message. This might read something like:
 	 * msgSitCustomerAtTable called with Customer Jim at table 3.
 	 */
 	private String message;
 
 	/**
 	 * @param message
 	 */
 	public LoggedEvent(String message) {
 		timestamp = new Date();
 		this.message = message;
 	}
 
 	/**
 	 * @return the timestamp
 	 */
 	public Date getTimestamp() {
 		return timestamp;
 	}
 
 	/**
 	 * @return the message
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(DateFormat.getTimeInstance().format(timestamp));
 		sb.append(": ");
 		sb.append(message);
 		return sb.toString();
 	}
 
 }
