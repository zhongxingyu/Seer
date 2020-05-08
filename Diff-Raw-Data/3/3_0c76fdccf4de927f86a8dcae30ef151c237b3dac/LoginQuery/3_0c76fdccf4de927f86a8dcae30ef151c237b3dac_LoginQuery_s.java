 package server.queries;
 
 import server.entities.EmailAddress;
 import server.entities.Login;
 
 /**
  * Creates/Deletes/Checks the Login and inputs them into the Database
  * 
  * @author oleg.scheltow
  * 
  */
 public class LoginQuery extends QueryResult {
 
 	public LoginQuery() {
 		super();
 	}
 
 	/**
 	 * Get the Password for the specified User
 	 * 
 	 * @param username
 	 * @return String password
 	 */
 	public String getPassword(final String username) {
 		final Login login = this.getLoginUser(username);
 		if (login != null) {
 			return login.getPassword();
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the Email address for the specified User
 	 * @param username
 	 * @return String Email
 	 */
 	public String getEmailForUser(String username){
 		final Login login = this.getLoginUser(username);
 		if (login != null) {
 			return login.getEmail().getEMailAddress();
 		}
 		return null;
 	}
 
 	/**
 	 * Create a new User with the specified Details
 	 * 
 	 * @param username
 	 * @param password
 	 * @param eMailAddress
 	 */
 	public boolean createUser(final String username, final String password,
 			final String eMailAddress) {
 		Login login = this.getLoginUser(username);
 		if (login == null) {
 			this.em.getTransaction().begin();
 
 			final EmailAddress email = new EmailAddress();
 			email.setEMailAddress(eMailAddress);
 			this.em.persist(email);
 
 			login = new Login();
 			login.setPassword(password);
 			login.setUser(username);
 			login.setEmail(email);
 			this.em.persist(login);
 			this.em.getTransaction().commit();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Removes the User Login
 	 * 
 	 * @param username
 	 * @return
 	 */
 	public boolean removeLogin(final String username) {
 		final Login login = this.getLoginUser(username);
 		return this.removeFromDB(login);
 
 	}
 
 	/**
 	 * Changes the User password
 	 * 
 	 * @param username
 	 * @param password
 	 */
 	public boolean changePassword(final String username, final String password) {
		this.em.getTransaction().begin();
 		final Login loginUser = this.getLoginUser(username);
 		if (loginUser != null) {
 			loginUser.setPassword(password);
 			this.em.persist(loginUser);
 			this.em.getTransaction().commit();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private Login getLoginUser(final String username) {
 		return (Login) this.getSingleResult(this.em.createQuery(
 				"select l from Login l where user = '" + username + "'",
 				Login.class));
 	}
 
 }
