 package models;
 
 import play.Logger;
 import play.api.libs.Crypto;
 import play.db.ebean.*;
 
 import javax.persistence.*;
 
 import com.avaje.ebean.ExpressionList;
 
 import controllers.Application;
 
 import java.util.*;
 
 import play.data.Form;
 import play.data.format.*;
 import play.data.validation.*;
 import play.mvc.Controller;
 import play.mvc.Http.Request;
 import play.mvc.Http.Session;
 
 @Entity(name="users")
 @Table(name="users")
 public class User extends Model {
 
 	// ---------- Static stuff ----------
 
 	private static final long serialVersionUID = 1L;
 
 
 	public static final Long LINK_TIMEOUT = 24*3600*1000L; // TODO: make as admin setting instead
 
 
 	// ---------- Instance stuff ----------
 
 	@Id
 	@GeneratedValue(strategy=GenerationType.SEQUENCE)
 	public Long id;
 
 	@Constraints.Required
 	@Formats.NonEmpty
 	@Constraints.Email
 	public String email;
 
 	@Constraints.MinLength(1)
 	@Constraints.Pattern("[^{}\\[\\]();:'\"<>]+") // Avoid breaking JavaScript code in templates
 	public String name;
 
 	@Constraints.Required
 	@Constraints.MinLength(6)
 	public String password;
 
 	@Constraints.Required
 	public boolean admin;
 
 	@Constraints.Required
 	public boolean active; // Account is deactivated until the user sets a password
 
 	// Crypto.sign(email+passwordLinkSent.getTime()) = uid sent in link
 	public Date passwordLinkSent; // Time that the password link was sent
 
 	/**
 	 * Constructor
 	 */
 	public User(String email, String name, String password, boolean admin) {
 		this.email = email;
 		this.name = name;
 		if ("".equals(password)) {
 			this.password = "";
 			this.active = false;
 		} else {
 			this.password = Crypto.sign(password);
 			this.active = true;
 		}
 		this.admin = admin;
 	}
 	
 	/**
 	 * Try setting the password using the provided activation UID.
 	 * 
 	 * @param uid
 	 * @param password
 	 */
 	public void activate(String uid, String password) {
 		if (getActivationUid().equals(uid)) {
 			this.active = true;
 			this.password = Crypto.sign(password);
 		}
 	}
 
 	/**
 	 * Generates a new activation UID.
 	 */
 	public void makeNewActivationUid() {
 		this.passwordLinkSent = new Date();
 	}
 
 	/**
 	 * Gets the activation UID.
 	 * @return
 	 */
 	public String getActivationUid() {
 		if (this.passwordLinkSent == null || new Date(new Date().getTime() - LINK_TIMEOUT).after(this.passwordLinkSent)) {
 			return null;
 		}
 		return Crypto.sign(this.email+this.passwordLinkSent.getTime()/1000);
 	}
 
 	public String toString() {
 		return "User(" + email + ")";
 	}
 
 	/**
 	 * Encrypts and sets the password.
 	 */
 	public void setPassword(String password) {
 		this.password = Crypto.sign(password);
 	}
 	
 	public Long flashBrowserId() {
 		Long browserId = new Random().nextLong();
 		NotificationConnection.createBrowserIfAbsent(id, browserId);
 		Logger.debug("Browser: user #"+id+" opened browser window #"+browserId);
 		Controller.flash("browserId",""+browserId);
 		return browserId;
 	}
 	
 	// -- Queries
 
 	public static Model.Finder<String,User> find = new Model.Finder<String, User>(Application.datasource, String.class, User.class);
 
 	/** Retrieve all users. */
 	public static List<User> findAll() {
 		return find.all();
 	}
 
 	/** Retrieve a User from email. */
 	public static User findByEmail(String email) {
 		List<User> users = find.where().eq("email", email).findList();
 		for (int u = users.size()-1; u > 0; u--)
 			users.get(u).delete(Application.datasource);
 		if (users.size() == 0)
 			return null;
 		else
 			return users.get(0);
 	}
 
 	/** Retrieve a User from id. */
 	public static User findById(long id) {
 		List<User> users = find.where().eq("id", id).findList();
 		for (int u = users.size()-1; u > 0; u--)
 			users.get(u).delete(Application.datasource);
 		if (users.size() == 0)
 			return null;
 		else
 			return users.get(0);
 	}
 	
 	/** Authenticate a user. */
 	public static User authenticate(Request request, Session session) {
 		User user;
 		
 		Long id = models.User.parseUserId(session); // login with session variables
 		if (id == null && request.queryString().containsKey("guestid") && request.queryString().get("guestid").length > 0) {
 			try {
 				id = Long.parseLong("-"+request.queryString().get("guestid")[0]); // resume guest session
 			} catch (NumberFormatException e) {
 				// do nothing
 			}
 		}
 		
 		if (id == null) { // no userid; try automatic login
 			if ("true".equals(Setting.get("users.guest.automaticLogin")))
 				return loginAsGuest(session);
 			else
 				return null;
 			
 		} else if (id >= 0) { // normal or admin user
 			try {
 				user = find.where()
 						.eq("id", id)
 						.eq("email", session.get("email"))
 						.eq("password", session.get("password"))
 						.findUnique();
 				user.login(session);
 				return user;
 				
 			} catch (NullPointerException e) {
 				// Not found or wrong credentials
 				return null;
 			}
 			
 		} else if ("true".equals(models.Setting.get("users.guest.allowGuests"))) { // guest user
 			user = new User("", models.Setting.get("users.guest.name"), "", false);
 			user.id = id;
			user.login(session);
 			return user;
 			
 		} else {
 			// trying to log in as guest, but guest login is not allowed
 			return null;
 		}
 	}
 	
 	private static Random randomGuestUserId = new Random();
 	public static User loginAsGuest(Session session) {
 		if (!"true".equals(models.Setting.get("users.guest.allowGuests")))
 			return null;
 		
 		if ("desktop".equals(models.Setting.get("deployment"))) {
 			User admin = find.where().eq("admin", true).findList().get(0);
 			admin.login(session);
 			return admin;
 			
 		} else {
 			User guest = new User("", models.Setting.get("users.guest.name"), "", false);
 			guest.id = -1-(long)randomGuestUserId.nextInt(2147483640);
 			guest.login(session);
 			return guest;
 		}
 	}
 
 	public void login(Session session) {
 		if (id != null) {
 			session.put("userid", id+"");
 	    	session.put("name", name);
 	    	session.put("email", email);
 	    	session.put("password", password);
 	    	session.put("admin", admin+"");
 		} else {
 			session.remove("userid");
 			Logger.warn("Could not log in user '"+name+"' ('"+email+"'); userid is null.");
 		}
 	}
 
 	/** Authenticate a user with an unencrypted password */
 	public static User authenticateUnencrypted(String email, String password, Session session) {
 		try {
 			User user = find.where()
 					.eq("email", email)
 					.eq("password", Crypto.sign(password))
 					.findUnique();
 			user.login(session);
 			return user;
 			
 		} catch (NullPointerException e) {
 			// Not found or wrong credentials
 			return null;
 		}
 	}
 
 	/**
 	 * Validate a new user.
 	 * @param filledForm
 	 */
 	public static void validateNew(Form<User> filledForm) {
 		if (User.findByEmail(filledForm.field("email").valueOr("")) != null)
 			filledForm.reject("email", "That e-mail address is already taken");
 		
 		String adminString = filledForm.field("admin").valueOr("");
 		if (!adminString.equals("true") && !adminString.equals("false"))
 			filledForm.reject("admin", "The user must either *be* an admin, or *not be* an admin");
 		
 		String password = filledForm.field("password").valueOr("");
 		if ("true".equals(Setting.get("mail.enable"))) {
 			// "password" are not set by the administrator when e-mails are enabled
 			filledForm.errors().remove("password");
 			
 		} else {
 			if (0 <= password.length() && password.length() < 6) {
 				filledForm.reject("password", "The password must be at least 6 characters long");
 			}
 		}
 		
 		filledForm.errors().remove("active");
 	}
 	
 	/**
 	 * Validate changes for a user.
 	 * @param filledForm
 	 * @param user 
 	 */
 	public void validateChange(Form<User> filledForm, User user) {
 		if (!this.email.equals(filledForm.field("email").value()) && User.findByEmail(filledForm.field("email").valueOr("")) != null)
 			filledForm.reject("email", "That e-mail address is already taken");
 		
 		String password = filledForm.field("password").valueOr("");
 		if (password.length() > 0) {
 			// Trying to change the password
 			if (password.length() < 6)
 				filledForm.reject("password", "The password must be at least 6 characters long");
 			
 		} else {
 			// Not trying to change the password
 			if (!(this.admin + "").equals(filledForm.field("admin").valueOr(""))) {
 				String adminString = filledForm.field("admin").valueOr("");
 				if (!adminString.equals("true") && !adminString.equals("false"))
 					filledForm.reject("admin", "The user must either *be* an admin, or *not be* an admin");
 				
 				if (this.id.equals(user.id)) {
 					filledForm.reject("admin", "Only other admins can demote you to a normal user, you cannot do it yourself");
 					
 				} else if (user.admin) {
 					filledForm.errors().remove("password"); // dont throw "error.required" for "password" when an admin edits another user
 				}
 			}
 			
 		}
 	}
 	
 	/**
 	 * Whether the form contains changes to the user.
 	 * @param filledForm
 	 * @return
 	 */
 	public boolean hasChanges(Form<User> filledForm) {
 		if (!this.name.equals(filledForm.field("name").valueOr("")))
 			return true;
 		
 		if (!this.email.equals(filledForm.field("email").valueOr("")))
 			return true;
 		
 		if (filledForm.field("password").valueOr("").length() != 0 && !this.password.equals(Crypto.sign(filledForm.field("password").valueOr(""))))
 			return true;
 
 		if (!(this.admin + "").equals(filledForm.field("admin").valueOr("")))
 			return true;
 		
 		return false;
 	}
 	
 	public List<Job> getJobs() {
 		return Job.find.where().eq("user", id).findList();
 	}
 	
 	@Override
 	public void delete(String datasource) {
 		List<Job> jobs = getJobs();
 		for (Job job : jobs)
 			job.delete(datasource);
 		super.delete(datasource);
 	}
 	
 	@Override
 	public void save(String datasource) {
 		super.save(datasource);
 		
 		// refresh id after save
 		if (this.id == null) {
 			User user = User.findByEmail(this.email);
 			if (user != null) {
 				this.id = user.id;
 			}
 		}
 	}
 	
 	/**
 	 * Parses the userid from the session. Useful to avoid having to handle cases (especially in templates) where session("userid") is neither null nor a string representation of a Long.
 	 * @param session
 	 * @return
 	 */
 	public static Long parseUserId(Session session) {
 		try {
     		return Long.parseLong(session.get("userid"));
     	} catch(NumberFormatException e) {
     		session.remove("userid");
     		return null;
     	}
 	}
 
 }
