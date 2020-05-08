 package com.redhat.qe.sm.base;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.DataProvider;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.cli.tasks.SubscriptionManagerTasks;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 /**
  * @author ssalevan
  * @author jsefler
  *
  */
 public class SubscriptionManagerCLITestScript extends SubscriptionManagerBaseTestScript{
 
 	public static Connection dbConnection = null;
 	
 	protected static SubscriptionManagerTasks clienttasks	= null;
 	protected static SubscriptionManagerTasks client1tasks	= null;	// client1 subscription manager tasks
 	protected static SubscriptionManagerTasks client2tasks	= null;	// client2 subscription manager tasks
 	
 	protected Random randomGenerator = new Random(System.currentTimeMillis());
 	
 	public SubscriptionManagerCLITestScript() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 
 	
 	
 	// Configuration Methods ***********************************************************************
 	
 	@BeforeSuite(groups={"setup"},description="subscription manager set up")
 	public void setupBeforeSuite() throws IOException {
 	
 		client = new SSHCommandRunner(clienthostname, sshUser, sshKeyPrivate, sshkeyPassphrase, null);
 		clienttasks = new SubscriptionManagerTasks(client);
 		client1 = client;
 		client1tasks = clienttasks;
 		
 		// will we be connecting to the candlepin server?
 		if (!serverHostname.equals("")) {
 			server = new SSHCommandRunner(serverHostname, sshUser, sshKeyPrivate, sshkeyPassphrase, null);
 			servertasks = new com.redhat.qe.sm.cli.tasks.CandlepinTasks(server,serverInstallDir,isServerOnPremises);
 
 		} else {
 			log.info("Assuming the server is already setup and running.");
 			servertasks = new com.redhat.qe.sm.cli.tasks.CandlepinTasks(null,null,isServerOnPremises);
 
 		}
 		
 		// will we be testing multiple clients?
 		if (!(	client2hostname.equals("") /*|| client2username.equals("") || client2password.equals("")*/ )) {
 			client2 = new SSHCommandRunner(client2hostname, sshUser, sshKeyPrivate, sshkeyPassphrase, null);
 			client2tasks = new SubscriptionManagerTasks(client2);
 		} else {
 			log.info("Multi-client testing will be skipped.");
 		}
 		
 		// setup the server
 		if (server!=null && servertasks.isOnPremises) {
 			
 			// NOTE: After updating the candlepin.conf file, the server needs to be restarted, therefore this will not work against the Hosted IT server which we don't want to restart or deploy
 			//       I suggest manually setting this on hosted and asking calfanso to restart
 			servertasks.updateConfigFileParameter("pinsetter.org.fedoraproject.candlepin.pinsetter.tasks.CertificateRevocationListTask.schedule","0 0\\/2 * * * ?");  // every 2 minutes
 			servertasks.cleanOutCRL();
 			servertasks.deploy(serverHostname, serverImportDir,serverBranch);
 
 			// also connect to the candlepin server database
 			dbConnection = connectToDatabase();  // do this after the call to deploy since it will restart postgresql
 		}
 		
 		// in the event that the clients are already registered from a prior run, unregister them
 		unregisterClientsAfterSuite();
 		
 		// setup the client(s)
 		for (SubscriptionManagerTasks smt : new SubscriptionManagerTasks[]{client2tasks, client1tasks}) {
 			if (smt==null) continue;
 			smt.installSubscriptionManagerRPMs(rpmUrls,enableRepoForDeps);
 			
 			// rhsm.conf [server] configurations
 			if (!serverHostname.equals(""))				smt.updateConfFileParameter(smt.rhsmConfFile, "hostname", serverHostname);							else serverHostname = smt.getConfFileParameter(smt.rhsmConfFile, "hostname");
 			if (!serverPrefix.equals(""))				smt.updateConfFileParameter(smt.rhsmConfFile, "prefix", serverPrefix);								else serverPrefix = smt.getConfFileParameter(smt.rhsmConfFile, "prefix");
 			if (!serverPort.equals(""))					smt.updateConfFileParameter(smt.rhsmConfFile, "port", serverPort);									else serverPort = smt.getConfFileParameter(smt.rhsmConfFile, "port");
 			if (!serverInsecure.equals(""))				smt.updateConfFileParameter(smt.rhsmConfFile, "insecure", serverInsecure);							else serverInsecure = smt.getConfFileParameter(smt.rhsmConfFile, "insecure");
 			if (!serverSslVerifyDepth.equals(""))		smt.updateConfFileParameter(smt.rhsmConfFile, "ssl_verify_depth", serverSslVerifyDepth);							else serverInsecure = smt.getConfFileParameter(smt.rhsmConfFile, "insecure");
 			if (!serverCaCertDir.equals(""))			smt.updateConfFileParameter(smt.rhsmConfFile, "ca_cert_dir", serverCaCertDir);						else serverCaCertDir = smt.getConfFileParameter(smt.rhsmConfFile, "ca_cert_dir");
 
 			// rhsm.conf [rhsm] configurations
 			if (!rhsmBaseUrl.equals(""))				smt.updateConfFileParameter(smt.rhsmConfFile, "baseurl", rhsmBaseUrl);								else rhsmBaseUrl = smt.getConfFileParameter(smt.rhsmConfFile, "baseurl");
 			if (!rhsmRepoCaCert.equals(""))				smt.updateConfFileParameter(smt.rhsmConfFile, "repo_ca_cert", rhsmRepoCaCert);						else rhsmRepoCaCert = smt.getConfFileParameter(smt.rhsmConfFile, "repo_ca_cert");
 			//if (!rhsmShowIncompatiblePools.equals(""))	smt.updateConfFileParameter(smt.rhsmConfFile, "showIncompatiblePools", rhsmShowIncompatiblePools);	else rhsmShowIncompatiblePools = smt.getConfFileParameter(smt.rhsmConfFile, "showIncompatiblePools");
 			if (!rhsmProductCertDir.equals(""))			smt.updateConfFileParameter(smt.rhsmConfFile, "productCertDir", rhsmProductCertDir);				else rhsmProductCertDir = smt.getConfFileParameter(smt.rhsmConfFile, "productCertDir");
 			if (!rhsmEntitlementCertDir.equals(""))		smt.updateConfFileParameter(smt.rhsmConfFile, "entitlementCertDir", rhsmEntitlementCertDir);		else rhsmEntitlementCertDir = smt.getConfFileParameter(smt.rhsmConfFile, "entitlementCertDir");
 			if (!rhsmConsumerCertDir.equals(""))		smt.updateConfFileParameter(smt.rhsmConfFile, "consumerCertDir", rhsmConsumerCertDir);				else rhsmConsumerCertDir = smt.getConfFileParameter(smt.rhsmConfFile, "consumerCertDir");
 
 			// rhsm.conf [rhsmcertd] configurations
 			if (!rhsmcertdCertFrequency.equals(""))		smt.updateConfFileParameter(smt.rhsmConfFile, "certFrequency", rhsmcertdCertFrequency);				else rhsmcertdCertFrequency = smt.getConfFileParameter(smt.rhsmConfFile, "certFrequency");
 		
 			smt.initializeFieldsFromConfigFile();
 			smt.removeAllCerts(true,true);
 		}
 		
 		// transfer a copy of the CA Cert from the candlepin server to the clients so we can test in secure mode
 		if (server!=null && servertasks.isOnPremises) {
 			log.info("Copying Candlepin cert onto clients to enable certificate validation...");
 			File localFile = new File("/tmp/"+servertasks.candlepinCACertFile.getName());
 			RemoteFileTasks.getFile(server.getConnection(), localFile.getParent(),servertasks.candlepinCACertFile.getPath());
 
 								RemoteFileTasks.putFile(client1.getConnection(), localFile.getPath(), client1tasks.getConfFileParameter(client1tasks.rhsmConfFile,"ca_cert_dir").trim().replaceFirst("/$", "")+"/"+serverHostname.split("\\.")[0]+"-candlepin-ca.pem", "0644");
 								client1tasks.updateConfFileParameter(client1tasks.rhsmConfFile, "insecure", "0");
 			if (client2!=null)	RemoteFileTasks.putFile(client2.getConnection(), localFile.getPath(), client2tasks.getConfFileParameter(client2tasks.rhsmConfFile,"ca_cert_dir").trim().replaceFirst("/$", "")+"/"+serverHostname.split("\\.")[0]+"-candlepin-ca.pem", "0644");
 			if (client2!=null)	client2tasks.updateConfFileParameter(client2tasks.rhsmConfFile, "insecure", "0");
 		}
 		
 		// transfer a copy of the generated product certs from the candlepin server to the clients so we can test
 		if (server!=null && servertasks.isOnPremises) {
 			log.info("Copying Candlepin generated product certs onto clients to simulate installed products...");
 			SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(server, "find "+serverInstallDir+servertasks.generatedProductsDir+" -name '*.pem'", 0);
 			for (String remoteFileAsString : result.getStdout().trim().split("\\n")) {
 				File remoteFile = new File(remoteFileAsString);
 				File localFile = new File("/tmp/"+remoteFile.getName());
 				RemoteFileTasks.getFile(server.getConnection(), localFile.getParent(),remoteFile.getPath());
 				
 									RemoteFileTasks.putFile(client1.getConnection(), localFile.getPath(), client1tasks.productCertDir+"/", "0644");
 				if (client2!=null)	RemoteFileTasks.putFile(client2.getConnection(), localFile.getPath(), client2tasks.productCertDir+"/", "0644");
 			}
 		}
 		
 		
 		log.info("Installed version of candlepin...");
 		try {
 			JSONObject jsonStatus = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,"anybody","password","/status")); // seems to work no matter what credentials are passed		
 			log.info("Candlepin server '"+serverHostname+"' is running version: "+jsonStatus.get("version"));
 		} catch (Exception e) {
 			log.warning("Candlepin server '"+serverHostname+"' is running version: UNKNOWN");
 		}
 		
 		log.info("Installed version of subscription-manager...");
 		log.info("Subscription manager client '"+client1hostname+"' is running version: "+client1.runCommandAndWait("rpm -q subscription-manager").getStdout()); // subscription-manager-0.63-1.el6.i686
 		if (client2!=null) log.info("Subscription manager client '"+client2hostname+"' is running version: "+client2.runCommandAndWait("rpm -q subscription-manager").getStdout()); // subscription-manager-0.63-1.el6.i686
 
 		isSetupBeforeSuiteComplete = true;
 	}
 	protected static boolean isSetupBeforeSuiteComplete = false;
 	
 	@AfterSuite(groups={"setup", "cleanup"},description="subscription manager tear down")
 	public void unregisterClientsAfterSuite() {
 		if (client2tasks!=null) client2tasks.unregister_(null, null, null);	// release the entitlements consumed by the current registration
 		if (client1tasks!=null) client1tasks.unregister_(null, null, null);	// release the entitlements consumed by the current registration
 	}
 	
 	@AfterSuite(groups={"setup", "cleanup"},description="subscription manager tear down")
 	public void disconnectDatabaseAfterSuite() {
 		
 		// close the candlepin database connection
 		if (dbConnection!=null) {
 			try {
 				dbConnection.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	// close the connection to the database
 		}
 	}
 
 
 	
 	// Protected Methods ***********************************************************************
 	
 	protected Connection connectToDatabase() {
 		Connection dbConnection = null;
 		try { 
 			// Load the JDBC driver 
 			Class.forName(dbSqlDriver);	//	"org.postgresql.Driver" or "oracle.jdbc.driver.OracleDriver"
 			
 			// Create a connection to the database
 			String url = dbSqlDriver.contains("postgres")? 
 					"jdbc:postgresql://" + dbHostname + ":" + dbPort + "/" + dbName :
 					"jdbc:oracle:thin:@" + dbHostname + ":" + dbPort + ":" + dbName ;
 			log.info(String.format("Attempting to connect to database with url and credentials: url=%s username=%s password=%s",url,dbUsername,dbPassword));
 			dbConnection = DriverManager.getConnection(url, dbUsername, dbPassword);
 			//log.finer("default dbConnection.getAutoCommit()= "+dbConnection.getAutoCommit());
 			dbConnection.setAutoCommit(true);
 			
 			DatabaseMetaData dbmd = dbConnection.getMetaData(); //get MetaData to confirm connection
 		    log.fine("Connection to "+dbmd.getDatabaseProductName()+" "+dbmd.getDatabaseProductVersion()+" successful.\n");
 
 		} 
 		catch (ClassNotFoundException e) { 
 			log.warning("JDBC driver not found!:\n" + e.getMessage());
 		} 
 		catch (SQLException e) {
 			log.warning("Could not connect to backend database:\n" + e.getMessage());
 		}
 		return dbConnection;
 	}
 
 	/* DELETEME  OLD CODE FROM ssalevan
 	
 	public void getSalesToEngineeringProductBindings(){
 		try {
 			String products = itDBConnection.nativeSQL("select * from butt;");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			log.info("Database query for Sales-to-Engineering product bindings failed!  Traceback:\n"+e.getMessage());
 		}
 	}
 	*/
 	
 
 	public static void sleep(long milliseconds) {
 		log.info("Sleeping for "+milliseconds+" milliseconds...");
 		try {
 			Thread.sleep(milliseconds);
 		} catch (InterruptedException e) {
 			log.info("Sleep interrupted!");
 		}
 	}
 	
 	protected int getRandInt(){
 		return Math.abs(randomGenerator.nextInt());
 	}
 	
 	
 //	public void runRHSMCallAsLang(SSHCommandRunner sshCommandRunner, String lang,String rhsmCall){
 //		sshCommandRunner.runCommandAndWait("export LANG="+lang+"; " + rhsmCall);
 //	}
 //	
 //	public void setLanguage(SSHCommandRunner sshCommandRunner, String lang){
 //		sshCommandRunner.runCommandAndWait("export LANG="+lang);
 //	}
 	
 
 	// Protected Inner Data Class ***********************************************************************
 	
 	protected class RegistrationData {
 		public String username=null;
 		public String password=null;
 		public String ownerKey=null;
 		public SSHCommandResult registerResult=null;
 		public List<SubscriptionPool> allAvailableSubscriptionPools=null;/*new ArrayList<SubscriptionPool>();*/
 		public RegistrationData() {
 			super();
 		}
 		public RegistrationData(String username, String password, String ownerKey,	SSHCommandResult registerResult, List<SubscriptionPool> allAvailableSubscriptionPools) {
 			super();
 			this.username = username;
 			this.password = password;
 			this.ownerKey = ownerKey;
 			this.registerResult = registerResult;
 			this.allAvailableSubscriptionPools = allAvailableSubscriptionPools;
 		}
 		
 		public String toString() {
 			String string = "";
 			if (username != null)		string += String.format(" %s='%s'", "username",username);
 			if (password != null)		string += String.format(" %s='%s'", "password",password);
 			if (ownerKey != null)		string += String.format(" %s='%s'", "ownerKey",ownerKey);
 			if (registerResult != null)	string += String.format(" %s=[%s]", "registerResult",registerResult);
 			for (SubscriptionPool subscriptionPool : allAvailableSubscriptionPools) {
 				string += String.format(" %s=[%s]", "availableSubscriptionPool",subscriptionPool);
 			}
 			return string.trim();
 		}
 	}
 	
 	// this list will be populated by subclass ResisterTests.RegisterWithUsernameAndPassword_Test
 	protected static List<RegistrationData> registrationDataList = new ArrayList<RegistrationData>();	
 
 	/**
 	 * Useful when trying to find a username that belongs to a different owner/org than the current username you are testing with.
 	 * @param key
 	 * @return null when no match is found
 	 * @throws JSONException
 	 */
 	protected RegistrationData findRegistrationDataNotMatchingOwnerKey(String key) throws JSONException {
 		Assert.assertTrue (!registrationDataList.isEmpty(), "The RegisterWithUsernameAndPassword_Test has been executed thereby populating the registrationDataList with content for testing."); 
 		for (RegistrationData registration : registrationDataList) {
 			if (registration.ownerKey!=null) {
 				if (!registration.ownerKey.equals(key)) {
 					return registration;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Useful when trying to find a username that belongs to the same owner/org as the current username you are testing with.
 	 * @param key
 	 * @param username
 	 * @return null when no match is found
 	 * @throws JSONException
 	 */
 	protected RegistrationData findRegistrationDataMatchingOwnerKeyButNotMatchingUsername(String key, String username) throws JSONException {
 		Assert.assertTrue (!registrationDataList.isEmpty(), "The RegisterWithUsernameAndPassword_Test has been executed thereby populating the registrationDataList with content for testing."); 
 		for (RegistrationData registration : registrationDataList) {
 			if (registration.ownerKey!=null) {
 				if (registration.ownerKey.equals(key)) {
 					if (!registration.username.equals(username)) {
 						return registration;
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Useful when trying to find registration data results from a prior registration by a given username.
 	 * @param key
 	 * @param username
 	 * @return null when no match is found
 	 * @throws JSONException
 	 */
 	protected RegistrationData findRegistrationDataMatchingUsername(String username) throws JSONException {
 		Assert.assertTrue (!registrationDataList.isEmpty(), "The RegisterWithUsernameAndPassword_Test has been executed thereby populating the registrationDataList with content for testing."); 
 		for (RegistrationData registration : registrationDataList) {
 			if (registration.username.equals(username)) {
 				return registration;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * This can be called by Tests that depend on it in a BeforeClass method to insure that registrationDataList has been populated.
 	 * @throws IOException 
 	 */
 	protected void RegisterWithUsernameAndPassword_Test() throws IOException {
 		if (registrationDataList.isEmpty()) {
 			for (List<Object> UsernameAndPassword : getUsernameAndPasswordDataAsListOfLists()) {
 				com.redhat.qe.sm.cli.tests.RegisterTests registerTests = new com.redhat.qe.sm.cli.tests.RegisterTests();
 				registerTests.setupBeforeSuite();
				registerTests.RegisterWithUsernameAndPassword_Test((String)UsernameAndPassword.get(0), (String)UsernameAndPassword.get(1), null);
 			}
 		}
 	}
 	
 
 	
 	// Data Providers ***********************************************************************
 
 	@DataProvider(name="getGoodRegistrationData")
 	public Object[][] getGoodRegistrationDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getGoodRegistrationDataAsListOfLists());
 	}
 	protected List<List<Object>> getGoodRegistrationDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 //		for (List<Object> registrationDataList : getBogusRegistrationDataAsListOfLists()) {
 //			// pull out all of the valid registration data (indicated by an Integer exitCode of 0)
 //			if (registrationDataList.contains(Integer.valueOf(0))) {
 //				// String username, String password, String type, String consumerId
 //				ll.add(registrationDataList.subList(0, 4));
 //			}
 //			
 //		}
 // changing to registrationDataList to get all the valid registeredConsumer
 		
 		for (RegistrationData registeredConsumer : registrationDataList) {
 			if (registeredConsumer.registerResult.getExitCode().intValue()==0) {
 				ll.add(Arrays.asList(new Object[]{registeredConsumer.username, registeredConsumer.password}));
 				
 				// minimize the number of dataProvided rows (useful during automated testcase development)
 				if (Boolean.valueOf(getProperty("sm.debug.dataProviders.minimize","false"))) break;
 			}
 		}
 		
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getAvailableSubscriptionPoolsData")
 	public Object[][] getAvailableSubscriptionPoolsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getAvailableSubscriptionPoolsDataAsListOfLists());
 	}
 	protected List<List<Object>> getAvailableSubscriptionPoolsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		// assure we are registered
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		if (client2tasks!=null)	{
 			client2tasks.unregister(null, null, null);
 			client2tasks.register(client2username, client2password, null, null, null, null, null, null, null, null);
 		}
 		
 		// unsubscribe from all consumed product subscriptions and then assemble a list of all SubscriptionPools
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		if (client2tasks!=null)	{
 			client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		}
 
 		// populate a list of all available SubscriptionPools
 		for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			ll.add(Arrays.asList(new Object[]{pool}));
 			
 			// minimize the number of dataProvided rows (useful during automated testcase development)
 			if (Boolean.valueOf(getProperty("sm.debug.dataProviders.minimize","false"))) break;
 		}
 		
 		// manually reorder the pools so that the base "Red Hat Enterprise Linux*" pool is first in the list
 		// This is a workaround for InstallAndRemovePackageAfterSubscribingToPool_Test so as to avoid installing
 		// a package from a repo that has a package dependency from a repo that is not yet entitled.
 		int i=0;
 		for (List<Object> list : ll) {
 			if (((SubscriptionPool)(list.get(0))).subscriptionName.startsWith("Red Hat Enterprise Linux")) {
 				ll.remove(i);
 				ll.add(0, list);
 				break;
 			}
 			i++;
 		}
 		
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getSystemSubscriptionPoolProductData")
 	public Object[][] getSystemSubscriptionPoolProductDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getSystemSubscriptionPoolProductDataAsListOfLists());
 	}
 	protected List<List<Object>> getSystemSubscriptionPoolProductDataAsListOfLists() throws JSONException {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		String subscriptionPoolProductData = getProperty("sm.system.subscriptionPoolProductData", "<>");
 		subscriptionPoolProductData = subscriptionPoolProductData.replaceAll("<", "["); // hudson parameters use < instead of [
 		subscriptionPoolProductData = subscriptionPoolProductData.replaceAll(">", "]"); // hudson parameters use > instead of ]
 
 		
 		JSONArray subscriptionPoolProductDataAsJSONArray = new JSONArray(subscriptionPoolProductData);
 		
 		for (int j=0; j<subscriptionPoolProductDataAsJSONArray.length(); j++) {
 			JSONObject poolProductDataAsJSONObject = (JSONObject) subscriptionPoolProductDataAsJSONArray.get(j);
 			String systemProductId = poolProductDataAsJSONObject.getString("systemProductId");
 			JSONArray bundledProductDataAsJSONArray = poolProductDataAsJSONObject.getJSONArray("bundledProductData");
 //			List<String> bundledProductNamesAsList = new ArrayList<String>();
 //			for (int i = 0; i < bundledProductDataAsJSONArray.length(); i++) {
 //				String bundledProductName = (String) bundledProductDataAsJSONArray.get(i);
 //				bundledProductNamesAsList.add(bundledProductName);
 //			}
 //			ll.add(Arrays.asList(new Object[]{systemProductId, bundledProductNamesAsList.toArray(new String[]{})}));
 
 			// String systemProductId, JSONArray bundledProductDataAsJSONArray
 			ll.add(Arrays.asList(new Object[]{systemProductId, bundledProductDataAsJSONArray}));
 
 			// minimize the number of dataProvided rows (useful during automated testcase development)
 			if (Boolean.valueOf(getProperty("sm.debug.dataProviders.minimize","false"))) break;
 		}
 		
 		return ll;
 	}
 	
 	@DataProvider(name="getUsernameAndPasswordData")
 	public Object[][] getUsernameAndPasswordDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getUsernameAndPasswordDataAsListOfLists());
 	}
 	protected List<List<Object>> getUsernameAndPasswordDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		String[] usernames = clientUsernames.split(",");
 		String[] passwords = clientPasswords.split(",");
 		String password = passwords[0].trim();
 		for (int i = 0; i < usernames.length; i++) {
 			String username = usernames[i].trim();
 			// when there is not a 1:1 relationship between usernames and passwords, the last password is repeated
 			// this allows one to specify only one password when all the usernames share the same password
 			if (i<passwords.length) password = passwords[i].trim();
 			ll.add(Arrays.asList(new Object[]{username,password}));
 		}
 		
 		return ll;
 	}
 
 	
 	@DataProvider(name="getAllConsumedProductSubscriptionsData")
 	public Object[][] getAllConsumedProductSubscriptionsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getAllConsumedProductSubscriptionsDataAsListOfLists());
 	}
 	protected List<List<Object>> getAllConsumedProductSubscriptionsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		// first make sure we are subscribed to all pools
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername,clientpassword,null,null,null,null, null, null, null, null);
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(null);
 		
 		// then assemble a list of all consumed ProductSubscriptions
 		for (ProductSubscription productSubscription : clienttasks.getCurrentlyConsumedProductSubscriptions()) {
 			ll.add(Arrays.asList(new Object[]{productSubscription}));
 			
 			// minimize the number of dataProvided rows (useful during automated testcase development)
 			if (Boolean.valueOf(getProperty("sm.debug.dataProviders.minimize","false"))) break;
 		}
 		
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getAllEntitlementCertsData")
 	public Object[][] getAllEntitlementCertsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getAllEntitlementCertsDataAsListOfLists());
 	}
 	protected List<List<Object>> getAllEntitlementCertsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		// first make sure we are subscribed to all pools
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername,clientpassword,null,null,null,null, null, null, null, null);
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(null);
 
 		
 		// then assemble a list of all consumed ProductSubscriptions
 		for (EntitlementCert entitlementCert : clienttasks.getCurrentEntitlementCerts()) {
 			ll.add(Arrays.asList(new Object[]{entitlementCert}));
 			
 			// minimize the number of dataProvided rows (useful during automated testcase development)
 			if (Boolean.valueOf(getProperty("sm.debug.dataProviders.minimize","false"))) break;
 		}
 		
 		return ll;
 	}
 }
