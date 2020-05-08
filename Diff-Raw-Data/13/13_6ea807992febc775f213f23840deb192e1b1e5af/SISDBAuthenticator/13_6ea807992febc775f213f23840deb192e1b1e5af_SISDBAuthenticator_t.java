 package org.iucn.sis.server.api.application;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import org.iucn.sis.server.api.io.UserIO;
 import org.iucn.sis.server.api.persistance.SISPersistentManager;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.User;
 
 import com.solertium.db.ExecutionContext;
 import com.solertium.mail.Mailer;
 import com.solertium.util.MD5Hash;
 import com.solertium.util.restlet.authentication.DBAuthenticator;
 
 public class SISDBAuthenticator extends DBAuthenticator {
 	
 	private final HashMap<String, String> loginCache;
 
 	public SISDBAuthenticator(ExecutionContext ec) {
 		super(ec, "user", "username", "password");
 		loginCache = new HashMap<String, String>();
 	}
 	
 	@Override
 	public String putNewAccount(String login, String password) throws AccountExistsException {
 		if (doesAccountExist(login))
 			throw new AccountExistsException();
 		
 		Session session = SISPersistentManager.instance().openSession();
 		
 		UserIO io = new UserIO(session);
 		
 		User user = io.getUserFromUsername(login);
 		if (user == null) {
 			user = new User();
 			user.setUsername(login);
 			user.setEmail(login);
 			user.setFirstName("");
 			user.setLastName("");
 		} else if (user.state == User.DELETED) {
 			user.state = User.ACTIVE;
 		} else {
 			session.close();
 			throw new AccountExistsException("User already exists");
 		}
 		user.setPassword(translatePassword(login, password));
 		user.setSisUser(true);
 		user.setRapidlistUser(false);
 		
		session.beginTransaction();
		
 		try {
 			io.saveUser(user);
			session.getTransaction().commit();
 		} catch (PersistentException e) {
			session.getTransaction().rollback();
 			throw new AccountExistsException(e.getLocalizedMessage());
 		} finally {
 			session.close();
 		}
 		
		return Integer.toString(user.getId());
 	}
 	
 	@Override
 	public String translatePassword(String login, String password) {
 		return super.translatePassword(login, password);
 	}
 	
 	@Override
 	public boolean validateAccount(String login, String password) {
 		if (password == null || "".equals(password.trim())) {
 			//Password could be empty if not yet assigned; don't want them to be able to log in.
 			return false;
 		}
 		else {
 			final String translatedPW = translatePassword(login, password);
 			final String translatedUN = getSHA1Hash(login);
 			
 			if (translatedPW.equalsIgnoreCase(loginCache.get(translatedUN)))
 				return true;
 			
 			Session session = SISPersistentManager.instance().openSession();
 			
 			User user = (User)session.createCriteria(User.class)
 				.add(Restrictions.eq("username", login))
 				.add(Restrictions.eq("password", translatedPW))
 				.add(Restrictions.eq("sisUser", Boolean.TRUE))
 				.add(Restrictions.eq("state", User.ACTIVE))
 				.uniqueResult();
 			
 			boolean success = user != null;
 			
 			if (success)
 				loginCache.put(translatedUN, translatedPW);
 			
 			session.close();
 			
 			return success;
 		}
 	}
 	
 	@Override
 	public boolean deleteUser(String username) throws AccountNotFoundException {
 		Session session = SISPersistentManager.instance().openSession();
 		UserIO io = new UserIO(session);
 		User user = io.getUserFromUsername(username);
 		if (user == null) {
 			session.close();
 			throw new AccountNotFoundException("User " + username + " was not found.");
 		}
 		user.state = User.DELETED;
 		
 		try {
 			return io.saveUser(user);
 		} catch (PersistentException e) {
 			return false;
 		} finally {
 			session.close();
 		}
 	}
 	
 	
 	@Override
 	public boolean resetPassword(String username) {
 		if (!username.equalsIgnoreCase("admin") || username.equals("")) {
 			String newPassword = new MD5Hash(username + new Date().toString()).toString().substring(0, 8);
 			if (changePassword(username, newPassword)) {
 				System.out.println("Changed password to " + newPassword);
 				
 				String body = "Hello " + username + ", \r\n \r\n Your password has been "
 				+ "reset for your IUCN application.  Please log in using the new credentials:\r\n"
 				+ "  Username: " + username + "\r\n  Password: " + newPassword
 				+ "\r\n \r\n We strongly recommend that you change your password on the " +
 						"login page.";
 				Mailer mailer = SISMailer.getGMailer();
 				mailer.setTo(username);
 				mailer.setBody(body);
 
 				try {
 					mailer.background_send();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				return true;
 			} else {
 				Debug.println("Error changing password for user {0}", username);
 				return false;
 			}
 		} else {
 			Debug.println("Username requested reset is either admin or empty, so it failed: {0}", username);
 			return false;
 		}
 	}
 	
 	@Override
 	public String emailConfirmationCode(String email) {
 		String confirmationCode = getSHA1Hash(Long.toString(new Date().getTime()) + "some_more_salt");
 
 		String body = "Your confirmation code for the Species Information " + "System is " + confirmationCode
 		+ "\r\n\r\n" + "Please visit http://iucnsis.org/authn/confirm/" + confirmationCode
 		+ " to activate your account.";
 
 		String subject = "Species Information System Account Signup Confirmation";
 		
 		Mailer mailer = SISMailer.getGMailer();
 		mailer.setTo(email);
 		mailer.setSubject(subject);
 		mailer.setBody(body);
 
 		try {
 			mailer.background_send();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return confirmationCode;
 	}
 
 }
