 package calendar;
 
 import java.io.IOException;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 
 import no.ntnu.fp.model.Person;
 import server.Execute;
 import server.MessageHandler;
 import server.RoundTime;
 
 public class Message implements Comparable<Message> {
 	
 	protected long msgId;
	private Date dateSent;
 	protected String content;
 	protected String title;
 	private List<Person> receivers;
 	
 	/**
 	 * Created as Message object and saved it to the database.
 	 * @param title
 	 * @param content
 	 * @throws IOException
 	 */
 	public Message (String title, String content) throws IOException{
 		this.title=title;
 		this.content=content;
 		this.dateSent= RoundTime.roundTime(new Date(System.currentTimeMillis()));
 		receivers = new ArrayList<Person>();
 		MessageHandler.createMessage(this);
 	}
 	
 	protected Message(long id){
 		this.msgId = id;
 	}
 	
 	public static Message recreateMessage(long msgId, String title, String content, Date dateSent) throws IOException{
 		Message m = new Message(msgId);
 		m.dateSent = dateSent;
 		m.msgId = msgId;
 		m.content = content;
 		m.title = title;
 		m.receivers = new ArrayList<Person>();
 		return m;
 	}
 	
 	public Message showMessage(Person user) throws IOException {
 		MessageHandler.setMessageAsRead(msgId, user.getId());
 		return this;
 	}
 	
 	public long getId() {
 		return msgId;
 	}
 
 	public Date getDateSent() {
 		return dateSent;
 	}
 	
 	public void addReceiver(Person p) throws IOException{
 		String query = "INSERT INTO UserMessages(msgId, userId, hasBeenRead) VALUES(%d, %d, null)";
 		Execute.executeUpdate(String.format(query, msgId, p.getId()));
 		receivers.add(p);
 	}
 	
 	// TODO Lagre til databasen i batch, fremfor en og en.
 	public void setReceivers(List<Person> receivers) throws IOException{
 		String deleteQuery = String.format("DELETE FROM UserMessages WHERE msgId=%d", msgId);
 		Execute.executeUpdate(deleteQuery);
 		String query = "INSERT INTO UserMessages(msgId, userId, hasBeenRead) VALUES(%d, %d, false)";
 		for(Person p: receivers){
 			Execute.executeUpdate(String.format(query, msgId, p.getId()));
 		}
 		this.receivers = receivers;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setId(long msgId) {
 		this.msgId = msgId;
 	}
 	
 	public void setDateSent(Date dateSent){
 		this.dateSent = dateSent;
 	}
 	
 
 	
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((content == null) ? 0 : content.hashCode());
 		result = prime * result
 				+ ((dateSent == null) ? 0 : dateSent.hashCode());
 		result = prime * result + (int) (msgId ^ (msgId >>> 32));
 		result = prime * result
 				+ ((receivers == null) ? 0 : receivers.hashCode());
 		result = prime * result + ((title == null) ? 0 : title.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Message other = (Message) obj;
 		if (content == null) {
 			if (other.content != null)
 				return false;
 		} else if (!content.equals(other.content))
 			return false;
 		if (dateSent == null) {
 			if (other.dateSent != null)
 				return false;
 		} else if (!dateSent.equals(other.dateSent))
 			return false;
 		if (msgId != other.msgId)
 			return false;
 		if (receivers == null) {
 			if (other.receivers != null)
 				return false;
 		} else if (!receivers.equals(other.receivers))
 			return false;
 		if (title == null) {
 			if (other.title != null)
 				return false;
 		} else if (!title.equals(other.title))
 			return false;
 		return true;
 	}
 
 	@Override
 	public int compareTo(Message m) {
 		return dateSent.compareTo(m.dateSent);
 	}
 	
 	@Override
 	public String toString(){
 		return title;
 	}
 
 }
