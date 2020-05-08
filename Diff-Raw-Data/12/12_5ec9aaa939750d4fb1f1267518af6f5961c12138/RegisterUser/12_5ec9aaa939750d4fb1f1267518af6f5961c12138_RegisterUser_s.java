 
 package axirassa.webapp.pages.user;
 
 import org.apache.shiro.authz.annotation.RequiresGuest;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.Log;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.Secure;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.json.JSONObject;
 import org.apache.tapestry5.services.Request;
 import org.hibernate.Session;
 
 import axirassa.model.UserEntity;
 import axirassa.model.exception.NoSaltException;
 import axirassa.webapp.components.AxForm;
 import axirassa.webapp.components.AxPasswordField;
 import axirassa.webapp.components.AxTextField;
 import axirassa.webapp.pages.Index;
 
 @Secure
 @RequiresGuest
 public class RegisterUser {
 	@Inject
 	private Request request;
 
 	@Inject
 	private Session session;
 
 	@Persist
 	@Property
 	private String email;
 
 	@Persist
 	@Property
 	private String confirmemail;
 
 	@Property
 	private String password;
 
 	@Property
 	private String confirmpassword;
 
 	@Component
 	private AxTextField emailField;
 
 	@Component
 	private AxTextField confirmEmailField;
 
 	@Component
 	private AxPasswordField confirmPasswordField;
 
 	@Component
 	private AxForm form;
 
 
 	@Log
 	public JSONObject onAJAXValidateFromEmailField() {
 		String emailvalue = request.getParameter("param");
 
 		if (UserEntity.isEmailRegistered(session, emailvalue))
			return new JSONObject().put("error", "The email '" + emailvalue + "' is taken");
 
 		return new JSONObject();
 	}
 
 
 	public void onValidateFromForm() {
 		if (password != null && confirmemail != null && !password.equals(confirmpassword))
 			form.recordError(confirmPasswordField, "Passwords do not match");
 
 		if (email != null && confirmemail != null && !email.equals(confirmemail))
 			form.recordError(confirmEmailField, "E-mails do not match");
 	}
 
 
 	@CommitAfter
 	public Object onSuccessFromForm() throws NoSaltException {
 		UserEntity user = new UserEntity();
 
 		user.setEMail(email);
 		user.createPassword(password);
 
 		session.persist(user);
 
 		return Index.class;
 	}
 }
