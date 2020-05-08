 package edu.gatech.oad.fullhouse.findmystuff.model;
 
 import java.util.Date;
 
 import android.location.Location;
 
 public class User {
     private long id;
 	private String username;
 	private String password;
 	private String name;
 	private String location;
 	private Date dateJoined;
 	private int loginAttempts;
 	private boolean isLocked;
	private boolean admin;
 	private String emailAddress;
 	private String phoneNumber;
 	
 	public User() {
 	    
 	}
 
 	public static User newUser(String usern, String passw, String name, String loc, boolean admin, String email, String phone) {
 	    User user = new User();
 		user.username = usern;
 		user.password = passw;
 		user.name = name;
 		user.location = loc;
 		user.dateJoined = new Date();
 		user.loginAttempts = 0;
 		user.isLocked = false;
		user.admin = admin;
 		user.emailAddress = email;
 		user.phoneNumber = phone;
 		return user;
 	}
 	
 	public boolean isLocked() {
 		return isLocked;
 	}
 	
 	public boolean checkPassword(String attempt) {
 		if (password.equals(attempt)) {
 			resetLogin();
 			return true;
 		} else {
 			loginAttempts++;
 			if (loginAttempts > 2)
 				isLocked = true;
 			return false;
 		}
 	}
 	
 	public void resetLogin() {
		isLocked = false;
 		loginAttempts = 0;
 	}
 	
 	public long getId() {
         return id;
     }
 	
 	public void setId(long id) {
         this.id = id;
     }
 	
 	public String getUsername() {
 		return username;
 	}
 	
 	public void setUsername(String username) {
         this.username = username;
     }
 	
 	public void setPassword(String password) {
         this.password = password;
     }
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
         this.name = name;
     }
 
 	public String getLocation() {
 		return location;
 	}
 	
 	public void setLocation(String location) {
         this.location = location;
     }
 	
 	public Date getDateJoined() {
 		return dateJoined;
 	}
 
 	public void setDateJoined(Date dateJoined) {
         this.dateJoined = dateJoined;
     }
 
 	public String getEmail() {
 		return emailAddress;
 	}
 	
 	public void setEmail(String emailAddress) {
 		this.emailAddress = emailAddress;
 	}
 	
 	public String getPhone() {
 		return phoneNumber;
 	}
 	
 	public void setPhone(String phoneNumber) {
 		this.phoneNumber = phoneNumber;
 	}
 	
 	public boolean isAdmin() {
		return admin;
 	}
 	
	public void setAdmin(boolean admin) {
		this.admin = admin;
 	}
 }
