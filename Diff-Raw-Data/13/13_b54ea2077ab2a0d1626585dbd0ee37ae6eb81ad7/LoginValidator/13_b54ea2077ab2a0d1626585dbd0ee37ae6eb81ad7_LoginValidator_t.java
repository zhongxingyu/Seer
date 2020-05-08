 package exercisesmanager.validators;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 
 import exercisesmanager.dao.implementors.UserDaoImpl;
 
 import exercisesmanager.pojos.User;
 
 public class LoginValidator implements Validator {
 
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	@Override
 	public boolean supports(Class<?> clazz) {
 		return User.class.equals(clazz);
 	}
 	
 	@Override
 	public void validate(Object object, Errors errors) {
 		User user = (User) object;
 		checkCredentials(errors, user);
 		checkRegex(errors, "username", user.getUsername(), "[^a-zA-Z_0-9_\\.]", "error.form.user.username.regex");
 		checkRegex(errors, "password", user.getPassword(), "[^a-zA-Z_0-9_\\.]", "error.form.user.password.regex");
 	}
 
 	private void checkCredentials(Errors errors, User user) {
 		UserDaoImpl userDaoImpl = new UserDaoImpl();
 		try {
 			User returnedUser = (User) userDaoImpl.selectByCredentials(user);
 			if (returnedUser == null) {
 				errors.rejectValue("password", "error.form.user.loginFailed");
 			}
 		} catch (Exception e) {
 			logger.error("checkCredential: error during login");
 		}
 	}
 
 	private void checkRegex(Errors errors, String property, String value, String regex, String errorMessage) {
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(value);
 		if (matcher.find()) {
 			errors.rejectValue(property, errorMessage);
 		}
 	}
 
 }
