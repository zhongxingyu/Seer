 package beans;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 
 import models.UserModel;
 
 @ManagedBean
 @SessionScoped
 public class LoginBean {
 	private UIInput username;
 	private String name;
	private UserModel user;
 	
 	public LoginBean() {
 	}
 
 	public UIInput getUsername() {
 		return username;
 	}
 	
 	public void setUsername(UIInput username) {
 		this.username = username;
 	}
 	
 	public boolean getLoggedin() {
 		System.out.println(this.user);
 		if(this.user != null) return true;
 		return false;
 	}
 	
 	public void logout() {
 		this.user = null;
 	}
 	
 	public void login(FacesContext context, UIComponent component, Object value) throws ValidatorException {
 		if(user != null) {
 			FacesMessage message = new FacesMessage("You're already loggd in!");
 			context.addMessage(component.getClientId(context), message);
 			message.setSeverity(FacesMessage.SEVERITY_FATAL);
 			throw new ValidatorException(message);
 		}
 		
 		
 		name = (String)username.getLocalValue();
 		
 		try {
 			user = UserModel.authenticate(name, (String)value);
 		} catch(Throwable e) {
 			
 		}
 		
 		if(user == null) {
 			FacesMessage message = new FacesMessage("Provided credentials do not match!");
 			context.addMessage(component.getClientId(context), message);
 			message.setSeverity(FacesMessage.SEVERITY_FATAL);
 			throw new ValidatorException(message);
 		}
 	}
 }
