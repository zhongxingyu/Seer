 package alpv.calendar;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 
 public final class Event implements Serializable, Comparable<Event> {
 
 	private static final long serialVersionUID = 3084014581109727872L;
 
 	public Event(String name, String[] user, Date begin) {
 		super();
 		this.name = name;
 		this.user = user;
 		this.begin = begin;
 	}
 
 	private long id;
 
 	private String name;
 
 	private String[] user;
 
 	private Date begin;
 
 	/**
 	 * @return the id
 	 */
 	public long getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name
 	 *            the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the user
 	 */
 	public String[] getUser() {
 		return user;
 	}
 
 	/**
 	 * @param user
 	 *            the user to set
 	 */
 	public void setUser(String[] user) {
 		this.user = user;
 	}
 
 	/**
 	 * @return the begin
 	 */
 	public Date getBegin() {
 		return begin;
 	}
 
 	/**
 	 * @param begin
 	 *            the begin to set
 	 */
 	public void setBegin(Date begin) {
 		this.begin = begin;
 	}
 
 	@Override
 	public int compareTo(Event arg0) {
 		int dateComp = this.getBegin().compareTo(arg0.getBegin());
 		if (dateComp != 0) {
 			return dateComp;
 		} // if both have the same begin-time, order by id
 		else
 			return Long.valueOf(this.getId()).compareTo(arg0.getId());
 	}
 
 	public boolean isAfter(Date date) {
 		return this.getBegin().after(date);
 	}
 	
 	
 	public String toString() {
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 		String date = sdf.format(this.getBegin());
 		
 		String event = "Id: " + this.getId() + "\n"
 		+ "Date: " + date + "\n"
 		+ "Name: " + this.getName() + "\n"
		+ "Users: " + Arrays.toString(this.getUser()) + "\n";
 		return event;
 	}
 }
