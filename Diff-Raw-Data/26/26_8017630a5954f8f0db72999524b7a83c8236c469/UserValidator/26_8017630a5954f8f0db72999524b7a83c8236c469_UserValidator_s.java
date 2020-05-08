 package com.wishop.mvc.validators;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 import com.wishop.model.BaseObject;
 import com.wishop.model.User;
 import com.wishop.mvc.WebConstants;
 import com.wishop.mvc.controllers.UserController;
 import com.wishop.service.BaseService;
 import com.wishop.service.UserService;
 import com.wishop.utils.WishopSecurityContext;
 
 /**
  * <code>Validator</code> for <code>User</code> forms.
  *
  * @author Paulo Monteiro
  */
 
 @Component
 public class UserValidator extends BaseValidator<User, Long> implements Validator, WebConstants {
 
 
 	private static final String PARAM_FIRST_NAME = "firstName";
 	private static final String PARAM_LAST_NAME = "lastName";
 	private static final String PARAM_EMAIL = "email";
 	private static final String PARAM_EMAIL_CONFIRMATION = "emailConfirmation";
 	private static final String PARAM_PASSWORD = "password";
 	private static final String PARAM_PASSWORD_CONFIRMATION = "passwordConfirmation";
 	private static final String PARAM_CURRENT_PASSWORD = "currentPassword";
 	private static final String PARAM_DATE_OF_BIRTH = "dateOfBirth";
 	private static final String PARAM_TELEPHONE = "telephone";
 	private static final String PARAM_MOBILE = "mobile";
 	private static final String PARAM_FAX = "fax";
 
 	private String formStatus;
 	
 	@Autowired
 	public UserValidator(BaseService<?, BaseObject<?, Long>> userService) {
 		super(userService);
 	}
 	
 	/**
 	 * Ensures that this Validator only validates instances User class
 	 * @param clazz - supplied class
 	 * @return true if is a valid class, false otherwise
 	 */
 	@Override
 	public boolean supports(Class<?> clazz) {
 		return User.class.equals(clazz);
 	}
 	
 	/**
 	 * Method to validate the User object. <br>
 	 * Validates the given object and in case of validation errors, registers those with the given Errors object.<br>
 	 * @param user - User
 	 * @param errors - List of errors
 	 */
 	@Override
 	public void validate(Object target, Errors errors) {
 		//TODO: FIX EMAIL CONFIRMATION
 		User formUser = (User) target;
 		User sessionUser = WishopSecurityContext.getSessionUser();
 		
 		ValidationUtils.rejectIfEmpty(errors, PARAM_FIRST_NAME, FIELD_REQUIRED, "Form field required");
 		ValidationUtils.rejectIfEmpty(errors, PARAM_LAST_NAME, FIELD_REQUIRED, "Form field required");
 		
 		validateEmail(errors, formUser, sessionUser);
 		validatePassword(errors, formUser, sessionUser);
 		
 		if (!StringUtils.isEmpty(formUser.getTelephone()) && !StringUtils.isNumeric(formUser.getTelephone())) {
 			errors.rejectValue(PARAM_TELEPHONE, FIELD_REQUIRED, "Form field must be numeric");
 		}
 		
 		if (!StringUtils.isEmpty(formUser.getMobile()) && !StringUtils.isNumeric(formUser.getMobile())) {
 			errors.rejectValue(PARAM_MOBILE, FIELD_REQUIRED, "Form field must be numeric");
 		}
 		
 		if (!StringUtils.isEmpty(formUser.getFax()) && !StringUtils.isNumeric(formUser.getFax())) {
 			errors.rejectValue(PARAM_FAX, FIELD_REQUIRED, "Form field must be numeric");
 		}
 		
 		ValidationUtils.rejectIfEmpty(errors, PARAM_DATE_OF_BIRTH, FIELD_REQUIRED, "Form field required");
 		
 	}
 
 	/**
 	 * Method that validates the Password
 	 * @param errors - Error messages Collection
 	 * @param user User which needs to be validated
 	 * @param sessionUser -  current logged in user
 	 * @param oldPassword 
 	 */
 	public void validatePassword(Errors errors, User user, User sessionUser) {
 		if (UserController.FORM_STATUS_SUBMIT.equals(getFormStatus()) && user.isNew()) {
 			ValidationUtils.rejectIfEmpty(errors, PARAM_PASSWORD, FIELD_REQUIRED, "Form field required");
 			ValidationUtils.rejectIfEmpty(errors, PARAM_PASSWORD_CONFIRMATION, FIELD_REQUIRED, "Form field required");
 			if (!user.getPassword().equals(user.getPasswordConfirmation())) {
 				errors.rejectValue(PARAM_PASSWORD_CONFIRMATION, FIELD_REQUIRED, "Password is incorrect");
 			}
 			
 			if (!isValidPassword(user.getPassword())) {
 				errors.rejectValue(PARAM_PASSWORD, FIELD_INVALID, "Invalid password");
 			}
 		}
 	}
 	
 	/**
 	 * Method that validates the a Password change
 	 * @param errors
 	 * @param user
 	 * @param sessionUser
 	 */
 	public void validatePasswordChange(Errors errors, User user, User sessionUser) {
		if((sessionUser.equals(user))) {
			if(StringUtils.isEmpty(user.getCurrentPassword())) {
				errors.rejectValue(PARAM_CURRENT_PASSWORD, FIELD_REQUIRED, "Form field required");
			} 
			User dbUser = ((UserService)getBaseService()).getUserByIdAndPassword(user, user.getCurrentPassword());
			if(dbUser == null) {
				errors.rejectValue(PARAM_CURRENT_PASSWORD, FORM_OLD_PASSWORD_IS_INCORRECT, "Form field required");
			}
		} 
 		validatePassword(errors, user, sessionUser);
 	}
 
 	/**
 	 * Method that validates the Email
 	 * @param errors - Error messages Collection
 	 * @param user User which needs to be validated
 	 * @param sessionUser - current logged in user
 	 */
 
 	private void validateEmail(Errors errors, User user, User sessionUser) {
 		ValidationUtils.rejectIfEmpty(errors, PARAM_EMAIL, FIELD_REQUIRED, "Form field required");
 		if( user.getId() == null) {
 			ValidationUtils.rejectIfEmpty(errors, PARAM_EMAIL_CONFIRMATION, FIELD_REQUIRED, "Form field required");
 			if (!user.getEmail().equals(user.getEmailConfirmation())) {
 				errors.rejectValue(PARAM_EMAIL_CONFIRMATION, FIELD_REQUIRED, "Email is incorrect");
 			}
 		}
 		
 		if (!isValidEmail(user.getEmail())) {
 			errors.rejectValue(PARAM_EMAIL, FIELD_INVALID, "Invalid email");
 		}
 		
 		if(!((UserService)getBaseService()).isUniqueEmail(user)) {
 			errors.rejectValue(PARAM_EMAIL, FIELD_INVALID, "Email already exists");
 		}
 	}
 	
 	/**
 	 * Uses <b>Regex</b> to validate an user password.<br>
 	 * Should be between 6 to 20 characters. Must have at least 1 character. Must have at least 1 digit.<br>
 	 * Can contain any of the following characters ! @ # $ % ^ & <br>
 	 * @param password - user password to validate
 	 * @return boolean- <b>true</b> if is a valid password, <b>false</b> otherwise
 	 */
 	private boolean isValidPassword(String password) {
 		return (password.matches("^.*\\p{Alpha}.*$") && 
 				password.matches("^.*\\p{Digit}.*$") && 
 				password.matches("^[\\p{Alnum}!@#$%^&]{6,20}$"));
 	}
 
 	/**
 	 * Uses <b>Regex</b> to validate an user email address
 	 * @param email - user email to validate 
 	 * @return boolean- <b>true</b> if is a valid email, <b>false</b> otherwise
 	 */
 	private boolean isValidEmail(String email) {
 		Pattern p = Pattern.compile("^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
 		Matcher m = p.matcher(email);
 		return m.matches();
 	}
 
 	/**
 	 * @return the userFormStatus
 	 */
 	public String getFormStatus() {
 		return formStatus;
 	}
 
 	/**
 	 * @param userFormStatus the userFormStatus to set
 	 */
 	public void setUserFormStatus(String userFormStatus) {
 		this.formStatus = userFormStatus;
 	}
 }
