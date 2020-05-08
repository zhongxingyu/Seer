 package swarm.shared.app;
 
 import swarm.shared.E_AppEnvironment;
 
 public class AppConfig
 {
 	public int libServerVersion = 11;
	public int appServerVersion = 0; // app should set this if it wants.
 	
 	public boolean verboseTransactions = false;
 	public String appId = null;
 	public String publicRecaptchaKey = null;
 }
