 package at.ac.tuwien.big.ewa.ue3.validator;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.Validator;
 import javax.faces.validator.ValidatorException;
 
 public class UsernameValidator implements Validator {
 
 	public void validate(FacesContext facesContext, UIComponent uiComponent, Object value) throws ValidatorException {
 		final String enteredUsername = (String) value;
 
 		// TODO check for existing username
 
		final boolean valid = enteredUsername.equalsIgnoreCase("max");
 
 		if (!valid) {
 			final FacesMessage message = new FacesMessage();
 			message.setDetail(" A user with that name already exists.");
 			message.setSummary("Username not valid");
 			message.setSeverity(FacesMessage.SEVERITY_ERROR);
 			throw new ValidatorException(message);
 		}
 	}
 
 }
