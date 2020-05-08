 package Main;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.IncorrectCredentialsException;
 import org.apache.shiro.authc.LockedAccountException;
 import org.apache.shiro.authc.UnknownAccountException;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.config.ConfigurationException;
 import org.apache.shiro.config.IniSecurityManagerFactory;
 import org.apache.shiro.mgt.SecurityManager;
 import org.apache.shiro.session.Session;
 import org.apache.shiro.subject.Subject;
 import org.apache.shiro.util.Factory;
 
 /**
  * 
  * @author Cole Christie Purpose: Provides simple authentication interface
  */
 public class Auth {
 	private static Logging mylog;
 	private static String psk;
 
 	/**
 	 * CONSTRUCTOR Sets up Shiro (pulls configuration from hard coded INI file
 	 * currently)
 	 * 
 	 * @param passedLog
 	 */
 	public Auth(Logging passedLog) {
 		psk = null;
 		mylog = passedLog;
 
 		// Load Shiro
 		try {
 			Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
 			SecurityManager securityManager = factory.getInstance();
 			SecurityUtils.setSecurityManager(securityManager);
 			mylog.out("INFO", "Apache Shiro activated");
 		} catch (ConfigurationException err) {
 			mylog.out("FATAL", "Failed to instantiate Apache Shiro\n" + err);
 			System.exit(0);
 		} catch (NoClassDefFoundError err) {
 			mylog.out("FATAL", "Failed to instantiate Apache Shiro\n" + err);
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Sets the private PSK to a passed value
 	 * 
 	 * @param PassedPSK
 	 */
 	public void SetPSK(String PassedPSK) {
 		if (PassedPSK.isEmpty()) {
 			mylog.out("FATAL", "PreShared Key CANNOT be set to empty/null.");
 			System.exit(0);
 		}
 		psk = PassedPSK;
 	}
 
 	/**
 	 * Establishes credentials based upon passed or provided input
 	 * 
 	 * @return
 	 */
 	public String[] GetCredential() {
 		// Cast variables
 		String user = "";
 		String pw = "";
 
 		// Create input handle
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
 		// Capture subject identity (user name)
 		System.out.println("Provide subject identity:");
 		try {
 			user = br.readLine();
 		} catch (IOException ioe) {
 			mylog.out("ERROR", "Failed to capture USERNAME input.");
 		}
 
 		// Capture password
 		System.out.println("Provide subject password:");
 		try {
 			pw = br.readLine();
 		} catch (IOException ioe) {
 			mylog.out("ERROR", "Failed to capture PASSWORD input.");
 		}
 		String[] Credentials = { user, pw }; // Cast return
 
 		// Capture PSK (pre-shared key)
 		System.out.println("Provide network pre-shared key:");
 		try {
 			psk = br.readLine();
 		} catch (IOException ioe) {
 			mylog.out("ERROR", "Failed to capture PSK input.");
 		}
 
 		return Credentials;
 	}
 
 	/**
 	 * Establishes an authenticated session based upon credentials provided
 	 * 
 	 * @param username
 	 * @param password
 	 */
 	public Subject Login(String username, String password) {
 		// Create subject identity
 		Subject currentUser = SecurityUtils.getSubject();
 
 		// Authenticate user
 		if (!currentUser.isAuthenticated()) {
 			try {
 				UsernamePasswordToken token = new UsernamePasswordToken(username, password);
 				token.setRememberMe(true); // Create token (SSO)
 				currentUser.login(token);
 			} catch (UnknownAccountException err) {
 				// Bad user name
 				failedLogin();
 			} catch (IncorrectCredentialsException err) {
 				// Bad password
 				failedLogin();
 			} catch (LockedAccountException err) {
 				// account locked
 				failedLogin();
 			} catch (AuthenticationException err) {
 				// other error
 				failedLogin();
 			}
 		}
 
 		// Log subject used
 		mylog.out("INFO", "Using [" + currentUser.getPrincipal() + "] credentials.");
 
 		// Check permissions
 		if (currentUser.hasRole("nothing")) {
 			mylog.out("WARN", "Account has NO PRIVLEGES");
 		} else {
 			if (currentUser.hasRole("secureTarget")) {
 				mylog.out("INFO", "Jobs can be RECIEVED");
 				mylog.out("INFO", "PRIVATE jobs can be calculated");
 			} else if (currentUser.hasRole("insecureTarget")) {
 				mylog.out("INFO", "Jobs can be RECIEVED");
 				mylog.out("INFO", "PUBLIC jobs can be calculated");
 			} else if (currentUser.hasRole("sourceTarget")) {
 				mylog.out("INFO", "Job classification system ENABLED");
 				mylog.out("INFO", "Jobs can be SENT");
 				mylog.out("INFO", "Workers can be BOUND (Authenticated & Authorized)");
 			} else if (currentUser.hasRole("resultTarget")) {
 				mylog.out("INFO", "Completed jobs (WORK) can be RECIEVED");
 				mylog.out("INFO", "Workers can be BOUND (Authenticated & Authorized)");
 			}
 		}
 		return currentUser;
 	}
 
 	/**
 	 * Exits the application when called
 	 */
 	private void failedLogin() {
 		mylog.out("FATAL", "Login DENIED");
 		mylog.out("FATAL", "Application terminated");
 		System.exit(0);
 	}
 
 	/**
 	 * Setups a subject session and caches purpose (for simplified access)
 	 * 
 	 * @param targetSubject
 	 * @return
 	 */
 	public Session EstablishSession(Subject targetSubject) {
 		// Setup a session
 		Session session = targetSubject.getSession();
 
 		// Determine the rough use of the session, store it for easy access
 		if (targetSubject.hasRole("nothing")) {
 			session.setAttribute("USE", "");
 			session.setAttribute("SecurityLevel", "0");
 			session.setAttribute("OS", "AuthFailed");
 		} else {
 			// Load the OS type into the client attribute list for easy recall
 			// later
 			session.setAttribute("OS", System.getProperty("os.name").toString());
 			// Create and save a small pseudo random integer that we will use to
			// uniquely identify this client (when used along with its IP
 			// address)
 			int SmallRandom = (1 + (int) (Math.random() * 65536));
 			session.setAttribute("ID", String.valueOf(SmallRandom));
 			// Authorization specific
 			if (targetSubject.hasRole("secureTarget")) {
 				session.setAttribute("USE", "private");
 				session.setAttribute("SecurityLevel", "2");
 			} else if (targetSubject.hasRole("insecureTarget")) {
 				session.setAttribute("USE", "public");
 				session.setAttribute("SecurityLevel", "1");
 			} else if (targetSubject.hasRole("sourceTarget")) {
 				session.setAttribute("USE", "server");
 				session.setAttribute("SecurityLevel", "0");
 			} else if (targetSubject.hasRole("resultTarget")) {
 				session.setAttribute("USE", "dropoff");
 				session.setAttribute("SecurityLevel", "0");
 			}
 		}
 		// Return created and annotated session
 		return session;
 	}
 
 	/**
 	 * Returns the PSK provided so it can be used for PBKDF2
 	 * 
 	 * @return
 	 */
 	public String GetPSK() {
 		return psk;
 	}
 }
