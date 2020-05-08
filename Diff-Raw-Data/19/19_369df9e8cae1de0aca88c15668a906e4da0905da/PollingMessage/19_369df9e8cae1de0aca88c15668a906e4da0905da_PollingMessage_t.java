 /**
  * SYSC3303 - Term Project
  * The Message represents the different type of messages being sent to and from the server.
  * @author Matthew Smith - 100 827 363
  * @version 02/5/2013
  */
package pollingSystem;
 
 import java.io.Serializable;
 
 public class PollingMessage implements Serializable {
 
 	/**Generated Serial ID*/
 	private static final long serialVersionUID = 6020846097896774666L;
 	
 	private String text;
 	
 	public PollingMessage(String messageText) {
 		text = messageText;
 	}
 	
 	public boolean isCreatePollMessage() {
 		return true; //temporary fake logic
 	}
 	
 	public boolean isPausePollMessage() {
 		return true;
 	}
 	
 
 	@Override
 	public String toString() {
 		return text;
 	}
 }
