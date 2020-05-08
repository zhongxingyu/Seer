 package cz.muni.fi.pv243.model.validation;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.Validator;
 import javax.faces.validator.ValidatorException;
 
 public class PasswordValidator implements Validator{
 
 	@Override
 	public void validate(FacesContext arg0, UIComponent arg1, Object arg2)
 			throws ValidatorException {
 		
 		String pass = (String) arg2;
 		/**
 		 * (			# Start of group
   		(?=.*\d)		#   must contains one digit from 0-9
   		(?=.*[a-z])		#   must contains one lowercase characters
   		(?=.*[A-Z])		#   must contains one uppercase characters
               	.		#     match anything with previous condition checking
                 {6,20}	#        length at least 6 characters and maximum of 20	
 					)	# End of group
 		 */
 		boolean bool = pass.matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20}");
 		
 		if(!bool) {
             FacesMessage message = new FacesMessage();
             message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary("Password is not valid.");
            message.setDetail("Password is not valid.");
             arg0.addMessage("registerForm:passwordHash", message);
             throw new ValidatorException(message);
         }
 	}
 
 }
