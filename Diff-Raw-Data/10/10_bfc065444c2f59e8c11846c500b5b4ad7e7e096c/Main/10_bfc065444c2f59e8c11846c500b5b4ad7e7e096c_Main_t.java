 package dst2.ejb;
 
 import java.math.BigDecimal;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import dst2.ejb.exceptions.InvalidCredentialsException;
 
 public class Main {
 
 	private static final Logger logger = LogManager.getLogger(Main.class);
 	
 	/**
 	 * @param args
 	 * @throws NamingException 
 	 */
 	public static void main(String[] args) throws NamingException {
 		
 		logger.info("Looking up TestingBean...");
 		Testing testing = (Testing) InitialContext.doLookup("java:global/dst2_1/TestingBean");
 		logger.info("Found and wired TestingBean.");
 		
 		logger.info("Adding grid for tests...");
 		testing.addGrid();
 		logger.info("Added grid for tests.");
 		
 		logger.info("Looking up GeneralManagementBean...");
 		GeneralManagement generalManagement = (GeneralManagement) InitialContext.doLookup("java:global/dst2_1/GeneralManagementBean");
 		logger.info("Found and wired GeneralManagementBean.");
 		
 		logger.info("Adding some price steps...");
 		generalManagement.addPriceStep(100, BigDecimal.valueOf(30));
 		generalManagement.addPriceStep(1000, BigDecimal.valueOf(15));
 		generalManagement.addPriceStep(5000, BigDecimal.valueOf(5));
 		logger.info("Price steps added.");
 		
 		logger.info("Looking up JobManagementBean...");
 		JobManagement jobManagement = (JobManagement) InitialContext.doLookup("java:global/dst2_1/JobManagementBean");
 		logger.info("Found and wired JobManagementBean.");
 		
 		logger.info("Logging in with unknown user...");
 		try {
 			jobManagement.login("unknownUser", "Test1");
 			bailOut("InvalidCredentialsException expected.");
 		} catch (InvalidCredentialsException e) {
 			logger.info("Login failed as expected.");
 		}
 		
 		logger.info("Logging in with invalid password...");
 		try {
 			jobManagement.login("mm", "invalidPassword");
 			bailOut("InvalidCredentialsException expected.");
 		} catch (InvalidCredentialsException e) {
 			logger.info("Login failed as expected.");
 		}
 		
 		logger.info("Logging in with correct credentials...");
 		try {
 			jobManagement.login("mm", "Test1");
 		} catch (InvalidCredentialsException e) {
 			bailOut("InvalidCredentialsException occured unexpectedly.");
 		}
 		
//		logger.info("Creating a valid job assignment...");
//		jobManagement.add(1, 4, "map-reduce", new ArrayList<String>());
//		jobManagement.add(1, 4, "map-reduce", new ArrayList<String>());
//		logger.info("Assigned " + jobManagement.get(1) + " jobs to grid 1");
 	}
 	
 	private static void bailOut(String message) {
 		logger.error(message);
 		System.exit(1);
 	}
 
 }
