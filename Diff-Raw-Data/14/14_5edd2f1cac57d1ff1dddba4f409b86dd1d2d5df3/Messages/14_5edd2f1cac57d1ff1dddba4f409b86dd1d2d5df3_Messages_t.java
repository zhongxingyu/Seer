 
 package urbanairship;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class Messages implements java.io.Serializable, java.lang.Iterable<Message>
 {
	private Integer badge;
 	private List<Message> messages;
 	
	public Integer getBadge()
 	{
 		return badge;
 	}
 	
	public void setBadge(Integer badge)
 	{
 		this.badge = badge;
 	}
 	
	public Integer getNumberOfUnreadMessages()
	{
		return this.getBadge();
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
