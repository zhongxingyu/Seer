 package org.CommunityService.ManagedBeans;
 
 import javax.faces.application.FacesMessage;
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
					return "Login";
 				} else {
 					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sample error message", "Login credentials didn't match.");
 					return "Login";
 				}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return "error";
 			}
 		} else {
 			new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sample error message", "User already logged in.");
 			return "Login";
 		}
 	}
 
 	public String Logout() {
 		currentVolunteer.setVolunteer(null);
 		return "Login";
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
