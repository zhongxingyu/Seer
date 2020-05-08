 package org.CommunityService.ManagedBeans;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 
 import org.CommunityService.EntitiesMapped.Organization;
 import org.CommunityService.Services.OrganizationService;
 import org.CommunityService.Services.VolunteerService;
 
 @ManagedBean
 @SessionScoped
 public class OrgProfileBean {
 
 	boolean renderError;
 	boolean renderPage;
 
 	@ManagedProperty(value = "#{currentVolunteerBean}")
 	private CurrentVolunteerBean currentVolunteer;
 
 	private Organization currentOrg = null;
 
 	private String oldEmail = null;
 
 	public void init() {
 
 		renderError = true;
 		renderPage = false;
 
 		if (currentVolunteer.getVolunteer() == null) {
 			return;
 		}
 
 		//do query
 		currentOrg = OrganizationService.getOrganizationByVolunteerId(currentVolunteer.getVolunteer().getVolunteerId());
 		
 		if(currentOrg == null)
 			return;
 
//		if (currentOrg == null) {
//			// return;
//			// test
//			currentOrg = new Organization(
//					currentVolunteer.getVolunteer(),
//					"Debug Org",
//					"123 Main St, Orlando, Fl, 32738",
//					"386-123-7890",
//					"debugOrg@gmail.com");
//			currentOrg
//					.setDescription("We need your help! Plant trees on UCF campus to add green space and shade, which is much needed during the summer time. Thank you for reading this and have a fantastic day!");
//		}

 		renderError = false;
 		renderPage = true;
 	}
 
 	public void saveEmail() {
 
 		oldEmail = new String(currentVolunteer.getVolunteer().getEmailAddress());
 	}
 
 	public String updateProfile() {
 
 		try {
 
 			// if email changed, clear activation date
 			if (oldEmail != null) {
 
 				String newEmail = currentVolunteer.getVolunteer().getEmailAddress();
 
 				if (!newEmail.equals(oldEmail)) {
 
 					// there is no activation date for organization
 				}
 			}
 
 			// test
			// OrganizationService.updateOrganization(currentOrg);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "Error";
 		}
 
 		return null;
 	}
 
 	public boolean isRenderError() {
 		return renderError;
 	}
 
 	public void setRenderError(boolean renderError) {
 		this.renderError = renderError;
 	}
 
 	public boolean isRenderPage() {
 		return renderPage;
 	}
 
 	public void setRenderPage(boolean renderPage) {
 		this.renderPage = renderPage;
 	}
 
 	public CurrentVolunteerBean getCurrentVolunteer() {
 		return currentVolunteer;
 	}
 
 	public void setCurrentVolunteer(CurrentVolunteerBean currentVolunteer) {
 		this.currentVolunteer = currentVolunteer;
 	}
 
 	public Organization getCurrentOrg() {
 		return currentOrg;
 	}
 
 	public void setCurrentOrg(Organization currentOrg) {
 		this.currentOrg = currentOrg;
 	}
 
 	public String getOldEmail() {
 		return oldEmail;
 	}
 
 	public void setOldEmail(String oldEmail) {
 		this.oldEmail = oldEmail;
 	}
 }
