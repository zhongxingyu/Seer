 package org.CommunityService.ManagedBeans;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
@RequestScoped
@ManagedBean(name="independentMessageBean")
 public class IndependentMessageBean {
 
	public String message = "";
 	
 	public void addInfo(ActionEvent actionEvent) {
 		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sample info message", message));
 	}
 
 	public void addWarn(ActionEvent actionEvent) {
 		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Sample warn message", message));
 	}
 
 	public void addError(ActionEvent actionEvent) {
 		FacesContext.getCurrentInstance().addMessage(null,
 				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sample error message", message));
 	}
 
 	public void addFatal(ActionEvent actionEvent) {
 		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Sample fatal message", message));
 	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
 }
