 package ufly.entities;
 
 
 import javax.jdo.annotations.Inheritance;
 import javax.jdo.annotations.InheritanceStrategy;
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 import javax.servlet.http.HttpSession;
 
 @PersistenceCapable
 @Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
 // TODO make User an abstract class
 public abstract class User {
 	/*------------ CONSTRUCTORS ------------*/
 	/**
 	 * Create a User object
 	 * @param emailAddr : email address of the User
 	 * @param password  : password of the user
 	 */
 	public User(String emailAddr, String password)
 	{
 		this.emailAddr = emailAddr;
 		this.setPassword(password);
 	}
 	public User(){}
 	
 	/*------------ METHODS ------------*/
 	/**
 	 * Check if one User is equal to another
 	 * @param otherUser :other user to compare against
 	 * @return true if both users are the same
 	 */
 	public boolean equals(User otherUser)
 	{
 		return this.emailAddr == otherUser.emailAddr;
 	}
 	
 	/**
 	 * Log in an already created user
 	 * @param session :session to make user logged into, usually request.getSession()
 	 * @return :this
 	 */
 	public User login(HttpSession session)
 	{
 		this.session=session;
 		session.setAttribute("loggedInUser", this.emailAddr);
 		return this;
 	}
 	
 	/**
 	 * Make the user logged out
 	 * @return void
 	 */
 	public User logout()
 	{
 		this.session.setAttribute("loggedInUser",null);
 		return this;
 	} 
 	
 	/*------------ MODIFIERS ------------*/
 	// TODO: All modifiers: need to modify datastore object, not local copy
 	/**
 	 * Change the password
 	 * @param newPw : string to make the new password
 	 */
 	public void changePw(String newPw)
 	{
 		this.setPassword(newPw);
 	}
 	
 	/**
 	 * @param name	: new first name to update to
 	 * @return
 	 */
 	
 	
 	
 	/*------------ ACCESSORS ------------*/
 	
 	
 	/**
 	 * Get email address of User
 	 * @return emailAddr
 	 */
 	public String getEmailAddr()
 	{
 		return this.emailAddr;
 	}
 	public boolean checkPassword(String pwd)
 	{
 		return this.password.Matches(pwd);
 	}
 	
 	
 	/*------------ HELPER METHODS ------------*/
 	/**
 	 * Change String into UflyPassword object
 	 * @param password	: String password to convert
 	 */
 	public void setPassword(String password)
 	{
 		this.password = new UflyPassword(password);
 	}
 	
 	
 	/*------------ VARIABLES ------------*/
 	@PrimaryKey
 	@Persistent
 	private String emailAddr;		// The User's email address. Uniquely identifies the entity.
 	
 	@Persistent(serialized="true", defaultFetchGroup="true") 
 	private UflyPassword password;	// The User's password
 	@NotPersistent
 	private HttpSession session;
 	//@NotPersistent
 	//private static PersistenceManager pm = PMF.get().getPersistenceManager(); // [Felix] I don't think we should do this. There is only one PersistenceManager and each time we use it we have to close it. We should get the singleton instance and close it only when we need it.
 }
