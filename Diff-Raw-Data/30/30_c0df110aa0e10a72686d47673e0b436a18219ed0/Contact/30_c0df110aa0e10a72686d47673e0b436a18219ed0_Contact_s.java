 package sw.server.models;
 
 import java.util.Date;
 
 public class Contact {
 
 	private long id;
 	private long userId;
 	private String name;
 	private String email;
 	private String phone;
 	private boolean emailAlert;
 	private boolean callAlert;
 	private boolean smsAlert;
 	private Date creTime;
 	private Date updTime;
 
 	public Contact(String name, String email) {
 		super();
 		this.name = name;
 		this.email = email;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public long getUserId() {
 		return userId;
 	}
 
 	public void setUserId(long userId) {
 		this.userId = userId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPhone() {
 		return phone;
 	}
 
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 
 	public boolean isEmailAlert() {
 		return emailAlert;
 	}
 
 	public void setEmailAlert(boolean emailAlert) {
 		this.emailAlert = emailAlert;
 	}
 
 	public boolean isCallAlert() {
 		return callAlert;
 	}
 
 	public void setCallAlert(boolean callAlert) {
 		this.callAlert = callAlert;
 	}
 
 	public boolean isSmsAlert() {
 		return smsAlert;
 	}
 
 	public void setSmsAlert(boolean smsAlert) {
 		this.smsAlert = smsAlert;
 	}
 
 	public Date getCreTime() {
 		return creTime;
 	}
 
 	public void setCreTime(Date creTime) {
 		this.creTime = creTime;
 	}
 
 	public Date getUpdTime() {
 		return updTime;
 	}
 
 	public void setUpdTime(Date updTime) {
 		this.updTime = updTime;
 	}
 
 }
