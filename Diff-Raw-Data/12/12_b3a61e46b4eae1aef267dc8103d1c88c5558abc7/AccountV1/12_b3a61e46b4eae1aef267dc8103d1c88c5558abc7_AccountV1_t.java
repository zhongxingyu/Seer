 package edu.gatech.oad.rocket.findmythings.server.spi;
 
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 import com.google.appengine.api.datastore.PhoneNumber;
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import com.googlecode.objectify.Key;
 
 import edu.gatech.oad.rocket.findmythings.server.db.DatabaseService;
 import edu.gatech.oad.rocket.findmythings.server.db.model.DBMember;
 import edu.gatech.oad.rocket.findmythings.server.model.AppMember;
 import edu.gatech.oad.rocket.findmythings.server.model.MessageBean;
 import edu.gatech.oad.rocket.findmythings.server.util.Config;
 import edu.gatech.oad.rocket.findmythings.server.util.HTTP;
 import edu.gatech.oad.rocket.findmythings.server.util.Messages;
 import edu.gatech.oad.rocket.findmythings.shared.util.validation.EmailValidator;
 import edu.gatech.oad.rocket.findmythings.shared.util.validation.RegexValidator;
 
 import javax.annotation.Nullable;
 import javax.inject.Named;
 
 @Api(name = "fmthings", version = "v1")
 public class AccountV1 extends BaseEndpoint {
 
 	boolean emailIsInvalid(String email) {
 		return email == null || email.length() == 0 || !EmailValidator.getInstance().isValid(email);
 	}
 
 	private static final RegexValidator PHONE_VALIDATOR = new RegexValidator("[0-9]-([0-9]{3})-[0-9]{3}-[0-9]{4}", false);
 
 	MessageBean mailWelcomeReturnOK(String email) {
 		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(TaskOptions.Builder.withUrl("/sendWelcomeMail")
				.param(Config.USERNAME_PARAM, email));
 		return new MessageBean(HTTP.Status.OK.toInt(), Messages.Status.OK.toString());
 	}
 
 	MessageBean mailAuthenticationTokenSendOK(String email, boolean isForgot) {
 		String registrationToken = DatabaseService.ofy().createRegistrationTicket(email);
 
 		// send email with registrationToken
 		Queue queue = QueueFactory.getDefaultQueue();
 		queue.add(TaskOptions.Builder.withUrl("/sendMail")
 				.param(Config.USERNAME_PARAM, email)
 				.param(Config.FORGOT_PASSWORD_PARAM, Boolean.toString(isForgot))
 				.param(Config.TICKET_PARAM, registrationToken));
 
 		return new MessageBean(HTTP.Status.OK.toInt(), Messages.Status.OK.toString());
 	}
 	
 	private MessageBean createMember(String email, String password, String passwordAlt, 
 			String phone, String name, String address, boolean isAdmin) {
 		try {
 			if (emailIsInvalid(email)) {
 				return new MessageBean(HTTP.Status.BAD_REQUEST.toInt(), Messages.Status.FAILED.toString(), Messages.Register.BAD_EMAIL_ADDRESS.toString());
 			}
 
 			AppMember user = getMemberWithEmail(email);
 
 			if (user != null && user.isRegistered()) {
 				return new MessageBean(HTTP.Status.BAD_REQUEST.toInt(), Messages.Status.FAILED.toString(), Messages.Register.ALREADY_USER.toString());
 			}
 
 			if (password == null || password.length() < 3 || passwordAlt == null || passwordAlt.length() < 3) {
 				return new MessageBean(HTTP.Status.BAD_REQUEST.toInt(), Messages.Status.FAILED.toString(), Messages.Register.BAD_PASSWORD.toString());
 			}
 
 			if (!password.equals(passwordAlt)) {
 				return new MessageBean(HTTP.Status.BAD_REQUEST.toInt(), Messages.Status.FAILED.toString(), Messages.Register.PASSWORDS_MATCH.toString());
 			}
 
			if (phone != null && !PHONE_VALIDATOR.isValid(phone)) {
 				return new MessageBean(HTTP.Status.BAD_REQUEST.toInt(), Messages.Status.FAILED.toString(), Messages.Register.INVALID_PHONE.toString());
 			}
 
			PhoneNumber phoneNum = null;
			if (phone != null) {
				phoneNum = new PhoneNumber(phone);
			}
 			
 			DBMember newUser = (DBMember)user;
 
 			if (newUser == null) {
 				newUser = new DBMember(email, password, null, null, true);
 				newUser.setPhone(phoneNum);
 				newUser.setName(name);
 				newUser.setAddress(address);
 			} else {
 				if (phoneNum != null) newUser.setPhone(phoneNum);
 				if (name != null) newUser.setName(name);
 				if (address != null) newUser.setAddress(address);
 			}
 			
 			newUser.setAdmin(isAdmin);
 			
 			DatabaseService.ofy().save().entity(newUser);
 
 			return mailWelcomeReturnOK(email);
 		} catch (Exception e) {
 			return new MessageBean(HTTP.Status.BAD_REQUEST.toInt(), Messages.Status.FAILED.toString(), Messages.Register.INVALID_DATA.toString());
 		}
 	}
 	
 	@ApiMethod(name = "account.createAdmin", path = "register/admin")
 	public MessageBean createAdmin(@Named("email") String email, @Named("password") String password,
 			@Named("password_alt") String passwordAlt, @Named("phone") @Nullable String phone,
 			@Named("name") @Nullable String name, @Named("address") @Nullable String address) {
 		return createMember(email, password, passwordAlt, phone, name, address, true);
 	}
 
 	@ApiMethod(name = "account.register", path = "register")
 	public MessageBean createUser(@Named("email") String email, @Named("password") String password,
 			@Named("password_alt") String passwordAlt, @Named("phone") @Nullable String phone,
 			@Named("name") @Nullable String name, @Named("address") @Nullable String address) {
 		return createMember(email, password, passwordAlt, phone, name, address, false);
 	}
 
 	@ApiMethod(name = "account.forgot", path = "forgot")
 	public MessageBean memberForgotPassword(@Named("email") String email) {
 		try {
 			if (emailIsInvalid(email)) {
 				return new MessageBean(HTTP.Status.BAD_REQUEST.toInt(), Messages.Status.FAILED.toString(), Messages.Register.BAD_EMAIL_ADDRESS.toString());
 			}
 
 			if (memberExistsWithEmail(email)) {
 				return mailAuthenticationTokenSendOK(email, true);
 			} else {
 				return new MessageBean(HTTP.Status.BAD_REQUEST.toInt(), Messages.Status.FAILED.toString(), Messages.Register.NO_SUCH_MEMBER.toString());
 			}
 		} catch (Exception e) {
 			return new MessageBean(HTTP.Status.BAD_REQUEST.toInt(), Messages.Status.FAILED.toString(), Messages.Register.INVALID_DATA.toString());
 		}
 	}
 
 	@ApiMethod(name = "account.get", path = "account")
 	public AppMember getCurrentMember() {
 		return getMemberWithEmail(getCurrentMemberEmail());
 	}
 
 	@ApiMethod(name = "account.update", path = "account/update")
 	public AppMember updateMember(AppMember member) {
 		if (member.isEditable()) {
 			Key<DBMember> result = DatabaseService.ofy().save().entity((DBMember)member).now();
 			return DatabaseService.ofy().load().key(result).get();
 		}
 		return null;
 	}
 
 	@ApiMethod(name = "account.login", path = "account/login")
 	public MessageBean getLoginToken(@Named("email") String email, @Named("password") String password) {
 		// this was caught by BearerTokenAuthenticatingFilter
 		return new MessageBean(HTTP.Status.OK.toInt(), Messages.Status.OK.toString());
 	}
 
 	@ApiMethod(name = "account.logout", path = "account/logout")
 	public MessageBean deleteLoginToken() {
 		// this was caught by BearerTokenRevokeFilter
 		return new MessageBean(HTTP.Status.OK.toInt(), Messages.Status.OK.toString());
 	}
 
 }
