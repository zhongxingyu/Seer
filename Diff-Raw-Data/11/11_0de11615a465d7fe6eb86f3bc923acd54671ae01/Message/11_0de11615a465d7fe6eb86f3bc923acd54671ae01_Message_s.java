 package sat.radio.message;
 
 import java.util.Date;
 
 import sat.radio.RadioID;
 
 public abstract class Message implements Comparable<Message> {
 	private int priority;
 	private int posx, posy;
 	private int length;
 	private Date time;
 	
 	public Message() {
 	}
 	
 	public int getPriority() {
 		return this.priority;
 	}
 	
 	public Date getDate() {
 		return this.time;
 	}
 
 	public abstract String toString();
 	
 	public int compareTo(Message msg) {
 		if(this.priority < msg.getPriority())
 			return 1;
 		else if(this.priority > msg.getPriority())
 			return -1;
 		else {
 			if(this.getDate().compareTo(msg.getDate()) > 0)
 				return 1;
 			else if(this.getDate().compareTo(msg.getDate()) < 0)
 				return -1;
 			else
 				return 0;
 		}
 	}
 }
