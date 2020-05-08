 /**
  * File: Message.java
  * Created: Aug 6, 2012
  */
 package quizsite.models;
 
 import java.sql.SQLException;
 import java.util.Formatter;
 import java.util.List;
 
 import quizsite.util.DatabaseConnection;
 import quizsite.util.PersistentModel;
 
 /**
  * Subclasses, note: define a format string and call setBody() with the parameters
  * corresponding to the placeholders in the format string.
  * eg.:
  * formatString = "%1$s is before %2$s which is preceded by %1$s";
  * setBody("Vighnesh", "Rege");
  */
 public abstract class Message extends PersistentModel{
 	/**
 	 * Usage:
 	 * $ Formatter fmt = new Formatter();
 	 * $ fmt.format("%1$s is before %2$s which is preceded by %1$s", "Vighnesh", "Rege"); 
 	 * */
 	protected Formatter fmt;
 	protected String formatString;	// To be set in subclass constructor
 	private String type;
 	
 	private User sender;
 	private User recipient;
 	protected String body;	// Message body - built in subclass constructors
 
 	// Meta data about the backing database table stored as static fields
 	// to avoid copies of same information in every instantiation
 	public static String TABLE_NAME = "Message";
	public static String[][] SCHEMA = {{"body", "TEXT"}, {"sender_id", "INTEGER"}, {"recipient_id", "INTEGER"}, {"type", "TINYTEXT"}};
 	public static String[][] FOREIGN_KEYS = 
 	{ {"sender_id", "User", "id"}, {"recipient_id", "User", "id"} };
 
 	public enum Type{
 		CHALLENGE("challenge"), 
 		FRIEND_REQUEST("friend_request"), 
 		NOTE("note");
 		
 		private final String repr;
 		Type(String repr) {
 			this.repr = repr;
 		}
 		@Override
 		public String toString() {
 			return getRepr();
 		}
 		private String getRepr() {
 			return this.repr;
 		}
 	};
 
 	public Message(User recipient, User sender) throws SQLException {
 		super(TABLE_NAME, SCHEMA, FOREIGN_KEYS);
 		this.setRecipient(recipient);
 		this.setSender(sender);
 		this.fmt = new Formatter();
 	}
 
 	protected String formatBody(Object...objects) {
 		// Needs a defined formatString
 		return fmt.format(formatString, objects).toString();
 	}
 
 	public void setBody(String body) {
 		this.body = body;
 	}
 	
 	/* Generic SQL queries */
 	/** Saves and set the auto-generated id */
 	@Override
 	public int save() throws SQLException {
 		setId(DatabaseConnection.create(this));
 		return getId();
 	}
 
 
 	public static List<Message> index() throws SQLException {
 		
 		return null;
 	}
 
 	@Override
 	public Object[] getFields() {
 		Object[] objs = new Object[] {getBody(), getSender().getId(), getRecipient().getId(), getType()};
 		return objs;
 	}
 
 	@Override
 	public void parse(List<String> dbEntry) throws IllegalArgumentException, SQLException {
 		super.parse(dbEntry);
 		setBody(dbEntry.get(1));
 		int senderId = Integer.parseInt(dbEntry.get(2)); 
 		int recipientId = Integer.parseInt(dbEntry.get(3));
 
 		User sender = User.get(senderId);
 		User recipient = User.get(recipientId);
 
 		setSender(sender);
 		setRecipient(recipient);
 		
 		setType(dbEntry.get(4));
 ;	}
 
 	public static Message get(int id) throws SQLException {
 
 		return null;
 	}	
 
 	/* The usual getters and setters */
 	/**
 	 * @return the body
 	 */
 	public String getBody() {
 		return body;
 	}
 
 	/**
 	 * @param recipient to set
 	 */
 	public void setRecipient(User recipient) {
 		this.recipient = recipient;
 	}
 
 	/**
 	 * @return the recipient
 	 */
 	public User getRecipient() {
 		return recipient;
 	}
 
 	/**
 	 * @param sender to set
 	 */
 	public void setSender(User sender) {
 		this.sender = sender;
 	}
 
 	/**
 	 * @return the from
 	 */
 	public User getSender() {
 		return sender;
 	}
 	
 	@Override
 	public String toString() {
 		return 	"From: " + getSender().getId() + "\n" +
 			  	"To: " + getRecipient().getId() + "\n" +
 			  	"Body: " + getBody();
 	}
 
 	/**
 	 * Use this one when reading from a database entry
 	 * @param type the type to set
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 	
 	/** Use this one in constructors */
 	public void setType(Type type) {
 		String typ = type.toString();
 		setType(typ);
 	}
 
 	/**
 	 * @return the type
 	 */
 	public String getType() {
 		return type;
 	}
 }
