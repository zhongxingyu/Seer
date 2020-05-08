 package bazik.android.sms;
 /* column names for sms provider:
 0: _id
 1: thread_id
 2: address
 3: person
 4: date
 5: protocol
 6: read   
 7: status
 8: type
 9: reply_path_present
 10: subject
 11: body
 12: service_center
 13: locked
 */
 public class Message 
 {
 	private String id;
 	private String thread_id;
 	private String address;
 	private String person;
 	private String date;
 	private String protocol;
 	private String read;
 	private String type;
 	private String reply_path_present;
 	private String subject;
 	private String body;
 	private String service_center;
 	private String locked;
 	
	public Message(String id, String thread_id,	String address,	String person,	String date, String protocol, String read, String type, 
 				   String reply_path_present, String subject, String body, String service_center, String locked)
 	{
 		this.id = id;
 		this.thread_id = thread_id;
 		this.address = address;
 		this.person = person;
 		this.date = date;
 		this.protocol = protocol;
 		this.read = read;
 		this.type = type;
 		this.reply_path_present = reply_path_present;
 		this.subject = subject;
 		this.body = body;
 		this.service_center = service_center;
 		this.locked = locked;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the thread_id
 	 */
 	public String getThread_id() {
 		return thread_id;
 	}
 
 	/**
 	 * @param thread_id the thread_id to set
 	 */
 	public void setThread_id(String thread_id) {
 		this.thread_id = thread_id;
 	}
 
 	/**
 	 * @return the address
 	 */
 	public String getAddress() {
 		return address;
 	}
 
 	/**
 	 * @param address the address to set
 	 */
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	/**
 	 * @return the person
 	 */
 	public String getPerson() {
 		return person;
 	}
 
 	/**
 	 * @param person the person to set
 	 */
 	public void setPerson(String person) {
 		this.person = person;
 	}
 
 	/**
 	 * @return the date
 	 */
 	public String getDate() {
 		return date;
 	}
 
 	/**
 	 * @param date the date to set
 	 */
 	public void setDate(String date) {
 		this.date = date;
 	}
 
 	/**
 	 * @return the protocol
 	 */
 	public String getProtocol() {
 		return protocol;
 	}
 
 	/**
 	 * @param protocol the protocol to set
 	 */
 	public void setProtocol(String protocol) {
 		this.protocol = protocol;
 	}
 
 	/**
 	 * @return the read
 	 */
 	public String getRead() {
 		return read;
 	}
 
 	/**
 	 * @param read the read to set
 	 */
 	public void setRead(String read) {
 		this.read = read;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * @param type the type to set
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	/**
 	 * @return the reply_path_present
 	 */
 	public String getReply_path_present() {
 		return reply_path_present;
 	}
 
 	/**
 	 * @param reply_path_present the reply_path_present to set
 	 */
 	public void setReply_path_present(String reply_path_present) {
 		this.reply_path_present = reply_path_present;
 	}
 
 	/**
 	 * @return the subject
 	 */
 	public String getSubject() {
 		return subject;
 	}
 
 	/**
 	 * @param subject the subject to set
 	 */
 	public void setSubject(String subject) {
 		this.subject = subject;
 	}
 
 	/**
 	 * @return the body
 	 */
 	public String getBody() {
 		return body;
 	}
 
 	/**
 	 * @param body the body to set
 	 */
 	public void setBody(String body) {
 		this.body = body;
 	}
 
 	/**
 	 * @return the service_center
 	 */
 	public String getService_center() {
 		return service_center;
 	}
 
 	/**
 	 * @param service_center the service_center to set
 	 */
 	public void setService_center(String service_center) {
 		this.service_center = service_center;
 	}
 
 	/**
 	 * @return the locked
 	 */
 	public String getLocked() {
 		return locked;
 	}
 
 	/**
 	 * @param locked the locked to set
 	 */
 	public void setLocked(String locked) {
 		this.locked = locked;
 	}
 	
 	
 	
 }
