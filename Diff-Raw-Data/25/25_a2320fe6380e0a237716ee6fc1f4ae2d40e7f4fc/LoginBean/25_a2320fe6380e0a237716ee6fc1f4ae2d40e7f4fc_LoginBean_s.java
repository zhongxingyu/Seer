 package org.CommunityService.ManagedBeans;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 
 import org.CommunityService.EntitiesMapped.Volunteer;
 import org.CommunityService.Services.VolunteerService;
 
 @ManagedBean
 @RequestScoped
 public class LoginBean {
 
 	@ManagedProperty(value = "#{currentVolunteerBean}")
 	private CurrentVolunteerBean currentVolunteer;
 	
 	private String username;
 	private String password;
 
 	public String Login() {
 		if (currentVolunteer.getVolunteer() == null) {
 			try {
 				Volunteer v = VolunteerService.getVolunteerByName(username);
 				if (v != null && v.getVolunteerPassword().equals(password)) {
 					currentVolunteer.setVolunteer(v);
					// TODO Need to change this to somehow return the page the user was last trying to access
					return "Home";
 				} else {
 					MessageController.addError("Login credentials didn't match.");
					return "Login";
 				}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
				return "error";
 			}
 		} else {
 			MessageController.addInfo("User already logged in.");
			// TODO Need to decide where to navigate in this case, is home appropriate, or is it better keep them on the page they're on?
			return "Home";
 		}
 	}

 	public String Logout() {
		currentVolunteer.setVolunteer(null);
		return "Home";
 	}
 
 	// Getters and Setters
 	// ---------------------------------------------------------------------------------------------------
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public CurrentVolunteerBean getCurrentVolunteer() {
 		return currentVolunteer;
 	}
 
 	public void setCurrentVolunteer(CurrentVolunteerBean currentVolunteer) {
 		this.currentVolunteer = currentVolunteer;
 	}
 
 }
