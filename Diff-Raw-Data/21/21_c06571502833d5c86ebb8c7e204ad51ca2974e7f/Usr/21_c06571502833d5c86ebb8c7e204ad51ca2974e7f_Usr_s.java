 package ch.hsr.waktu.domain;
 
 import ch.hsr.waktu.services.Md5;
 
 public class Usr {
 
 	private int usrid;
 	private String username;
 	private String name;
 	private String firstname;
 	private String passwordHash;
 	private int pensum;
 	private SystemRole systemRole = SystemRole.EMPLOYEE;
 	private double holiday;
 	private boolean active = true;
 	
 
 	public Usr() {
 		
 	}
 	
 	public Usr(String username, String firstname, String lastname,
 			String password, int pensum, SystemRole role, double holiday) {
 		this.username = username;
 		this.name = lastname;
 		this.firstname = firstname;
 		this.passwordHash = password;
 
 		this.pensum = pensum;
 		this.systemRole = role;
 		this.holiday = holiday;
 
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPasswordHash() {
 		return passwordHash;
 	}
 
 	public void setPassword(String password) {
 		this.passwordHash = Md5.hash(password);
 	}
 
 	public int getPensum() {
 		return pensum;
 	}
 
 	public SystemRole getRole() {
 		return systemRole;
 	}
 
 	public void setRole(SystemRole role) {
 		this.systemRole = role;
 	}
 
 	public double getHoliday() {
 		return holiday;
 	}
 
 	public void setHoliday(double holiday) {
 		this.holiday = holiday;
 	}
 
 	public int getId() {
 		return usrid;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getFirstname() {
 		return firstname;
 	}
 
 	public void setFirstname(String firstname) {
 		this.firstname = firstname;
 	}
 	
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActiveState(boolean toActive) {
 		this.active = toActive;
 	}
 
 	public void setPensum(int pensum) {
 			this.pensum = pensum;		
 	}
 	
 	public SystemRole getSystemRole() {
 		return systemRole;
 	}
 	
 	public void setSystemRole(SystemRole systemRole) {
 			this.systemRole = systemRole;
 	}
 	
 	@Override
 	public String toString() {
 		return firstname + " " + name;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Usr) {
 			Usr usr = (Usr)obj;
 			if (usr.username.equals(username) && usr.usrid == usrid && 
 					usr.firstname.equals(firstname) && usr.name.equals(name) && 
 					usr.holiday == holiday && usr.active == active && 
 					usr.passwordHash.equals(passwordHash) && usr.pensum == pensum && 
 					usr.systemRole.equals(systemRole)) {
 				return true;
 			}
 		}
 		return super.equals(obj);
 	}
 	
	@Override
	public int hashCode() {
		return new Integer(Md5.hash(this.toString()));
	}
 
 	public void setPasswordHash(String passwordHash) {
 		this.passwordHash = passwordHash;
 	}
 }
