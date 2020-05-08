 package dat255.grupp06.bibbla.backend.tasks;
 
 import dat255.grupp06.bibbla.model.Credentials;
 import dat255.grupp06.bibbla.utils.CredentialsMissingException;
 import dat255.grupp06.bibbla.utils.Message;
 
 /**
  * Job classes that need the user to be logged in extend this class.
  * The constructor runs a LoginJob to ensure that every run of ancestor jobs
  * should be carried out with user logged-in.
  * @author arla
  */
 public abstract class AuthorizedJob {
 	
 	protected final boolean loggedIn;
 	protected final Credentials credentials;
 	
 	/**
 	 * Construct a job that requires being logged in.
 	 * @param loggedIn Whether the user is already logged in.
 	 * @param credentials Which credentials to use otherwise.
 	 */
 	public AuthorizedJob(boolean loggedIn, Credentials credentials) {
 		if (credentials == null);
 		this.loggedIn = loggedIn;
 		this.credentials = credentials;
 	}
 	
 	/**
 	 * Log in if needed (which is determined by the loggedIn constructor
 	 * param). Should be called in ancestor's run() before login-dependent
 	 * connections are made.
 	 */
 	public void login() throws CredentialsMissingException {
 		if (loggedIn)
 			return;
 		LoginJob loginJob = new LoginJob(credentials);
 		Message message = loginJob.run();
 		if (!message.loggedIn) {
 			// If the login failed we can at least require updated credentials 
			// TODO Replace with something semantically correct
 			throw new CredentialsMissingException("Login failed.");
 		}
 	}
 }
