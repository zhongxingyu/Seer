 
 package urbanairship;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class Messages implements java.io.Serializable, java.lang.Iterable<Message>
 {
	private String badge;
 	private List<Message> messages;
 	
	public String getBadge()
 	{
 		return badge;
 	}
 	
	public void setBadge(String badge)
 	{
 		this.badge = badge;
 	}
 	
 	public List<Message> getMessages()
 	{
 		return messages;
 	}
 	
 	public void setMessages(List<Message> messages)
 	{
 		this.messages = messages;
 	}
 
 	public Iterator<Message> iterator()
 	{
 		return this.getMessages().iterator();
 	}
 
 	
 }
