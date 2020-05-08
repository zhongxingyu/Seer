 package com.redhat.qe.sm.base;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Random;
 import java.util.TimeZone;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.DataProvider;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.LogMessageUtil;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.cli.tasks.SubscriptionManagerTasks;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 import com.redhat.qe.tools.abstraction.AbstractCommandLineData;
 
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
 		if (isSetupBeforeSuiteComplete) return;
 		
 		client = new SSHCommandRunner(clienthostname, sshUser, sshKeyPrivate, sshkeyPassphrase, null);
 		clienttasks = new SubscriptionManagerTasks(client);
 		client1 = client;
 		client1tasks = clienttasks;
 		File serverCaCertFile = null;
 		List<File> generatedProductCertFiles = new ArrayList<File>();
 		
 		// will we be connecting to the candlepin server?
 		if (!serverHostname.equals("") && isServerOnPremises) {
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
 			dbConnection = connectToDatabase();  // do this after the call to deploy since deploy will restart postgresql
 			
 			// fetch the candlepin CA Cert
 			log.info("Fetching Candlepin CA cert...");
 			serverCaCertFile = new File((getProperty("automation.dir", "/tmp")+"/tmp/"+servertasks.candlepinCACertFile.getName()).replace("tmp/tmp", "tmp"));
 			RemoteFileTasks.getFile(server.getConnection(), serverCaCertFile.getParent(), servertasks.candlepinCACertFile.getPath());
 			
 			// fetch the generated Product Certs
 			log.info("Fetching the generated product certs...");
			SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(server, "find "+serverInstallDir+servertasks.generatedProductsDir+" -name '*.pem'", 0);
			for (String remoteFileAsString : result.getStdout().trim().split("\\n")) {
 				File remoteFile = new File(remoteFileAsString);
 				File localFile = new File((getProperty("automation.dir", "/tmp")+"/tmp/"+remoteFile.getName()).replace("tmp/tmp", "tmp"));
 				File localFileRenamed = new File(localFile.getPath().replace(".pem", "_.pem")); // rename the generated productCertFile to help distinguish it from a true RHEL productCertFiles
 				RemoteFileTasks.getFile(server.getConnection(), localFile.getParent(),remoteFile.getPath());
 				localFile.renameTo(localFileRenamed);
 				generatedProductCertFiles.add(localFileRenamed);
 			}
 
 		}
 		
 		// if clients are already registered from a prior run, unregister them
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
 			smt.installRepoCaCerts(repoCaCertUrls);
 			
 			// transfer a copy of the candlepin CA Cert from the candlepin server to the clients so we can test in secure mode
 			log.info("Copying Candlepin cert onto client to enable certificate validation...");
 			smt.installRepoCaCert(serverCaCertFile, serverHostname.split("\\.")[0]+".pem");
 			
 			// transfer copies of all the generated product certs from the candlepin server to the clients
 			log.info("Copying Candlepin generated product certs onto client to simulate installed products...");
 			smt.installProductCerts(generatedProductCertFiles);
 		}
 		
 //		// transfer a copy of the CA Cert from the candlepin server to the clients so we can test in secure mode
 //		if (server!=null && servertasks.isOnPremises) {
 //			log.info("Copying Candlepin cert onto clients to enable certificate validation...");
 //			File localFile = new File("/tmp/"+servertasks.candlepinCACertFile.getName());
 //			RemoteFileTasks.getFile(server.getConnection(), localFile.getParent(),servertasks.candlepinCACertFile.getPath());
 //			
 //			for (SubscriptionManagerTasks smt : new SubscriptionManagerTasks[]{client2tasks, client1tasks}) {
 //				if (smt==null) continue;
 //				smt.installRepoCaCert(localFile, serverHostname);
 //			}
 //
 //								RemoteFileTasks.putFile(client1.getConnection(), localFile.getPath(), client1tasks.getConfFileParameter(client1tasks.rhsmConfFile,"ca_cert_dir").trim().replaceFirst("/$", "")+"/"+serverHostname.split("\\.")[0]+"-candlepin-ca.pem", "0644");
 //								client1tasks.updateConfFileParameter(client1tasks.rhsmConfFile, "insecure", "0");
 //			if (client2!=null)	RemoteFileTasks.putFile(client2.getConnection(), localFile.getPath(), client2tasks.getConfFileParameter(client2tasks.rhsmConfFile,"ca_cert_dir").trim().replaceFirst("/$", "")+"/"+serverHostname.split("\\.")[0]+"-candlepin-ca.pem", "0644");
 //			if (client2!=null)	client2tasks.updateConfFileParameter(client2tasks.rhsmConfFile, "insecure", "0");
 //		}
 		
 //		// transfer a copy of the generated product certs from the candlepin server to the clients so we can test
 //		if (server!=null && servertasks.isOnPremises) {
 //			log.info("Copying Candlepin generated product certs onto clients to simulate installed products...");
 //			SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(server, "find "+serverInstallDir+servertasks.generatedProductsDir+" -name '*.pem'", 0);
 //			for (String remoteFileAsString : result.getStdout().trim().split("\\n")) {
 //				File remoteFile = new File(remoteFileAsString);
 //				File localFile = new File("/tmp/"+remoteFile.getName());
 //				RemoteFileTasks.getFile(server.getConnection(), localFile.getParent(),remoteFile.getPath());
 //				
 //									RemoteFileTasks.putFile(client1.getConnection(), localFile.getPath(), client1tasks.productCertDir+"/", "0644");
 //				if (client2!=null)	RemoteFileTasks.putFile(client2.getConnection(), localFile.getPath(), client2tasks.productCertDir+"/", "0644");
 //			}
 //		}
 		
 		
 		log.info("Installed version of candlepin...");
 		try {
 			JSONObject jsonStatus = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,"anybody","password","/status")); // seems to work no matter what credentials are passed		
 			log.info("Candlepin server '"+serverHostname+"' is running version: "+jsonStatus.get("version"));
 		} catch (Exception e) {
 			log.warning("Candlepin server '"+serverHostname+"' is running version: UNKNOWN");
 		}
 		
 		log.info("Installed version of subscription-manager...");
 		log.info("Client1 '"+client1hostname+"' is running version: "+client1.runCommandAndWait("rpm -q subscription-manager").getStdout()); // subscription-manager-0.63-1.el6.i686
 		if (client2!=null) log.info("Client2 '"+client2hostname+"' is running version: "+client2.runCommandAndWait("rpm -q subscription-manager").getStdout()); // subscription-manager-0.63-1.el6.i686
 
 		log.info("Installed version of python-rhsm...");
 		log.info("Client1 '"+client1hostname+"' is running version: "+client1.runCommandAndWait("rpm -q python-rhsm").getStdout()); // python-rhsm-0.63-1.el6.i686
 		if (client2!=null) log.info("Client2 '"+client2hostname+"' is running version: "+client2.runCommandAndWait("rpm -q python-rhsm").getStdout()); // python-rhsm-0.63-1.el6.i686
 
 		log.info("Installed version of RHEL...");
 		log.info("Client1 '"+client1hostname+"' is running version: "+client1.runCommandAndWait("cat /etc/redhat-release").getStdout()); // Red Hat Enterprise Linux Server release 6.1 Beta (Santiago)
 		if (client2!=null) log.info("Client2 '"+client2hostname+"' is running version: "+client2.runCommandAndWait("cat /etc/redhat-release").getStdout()); // Red Hat Enterprise Linux Server release 6.1 Beta (Santiago)
 
 		log.info("Installed version of kernel...");
 		log.info("Client1 '"+client1hostname+"' is running version: "+client1.runCommandAndWait("uname -a").getStdout()); // Linux jsefler-onprem-server.usersys.redhat.com 2.6.32-122.el6.x86_64 #1 SMP Wed Mar 9 23:54:34 EST 2011 x86_64 x86_64 x86_64 GNU/Linux
 		if (client2!=null) log.info("Client2 '"+client2hostname+"' is running version: "+client2.runCommandAndWait("uname -a").getStdout()); // Linux jsefler-onprem-server.usersys.redhat.com 2.6.32-122.el6.x86_64 #1 SMP Wed Mar 9 23:54:34 EST 2011 x86_64 x86_64 x86_64 GNU/Linux
 
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
 	
 	@AfterSuite(groups={"setup", "cleanup"},description="return clients to beaker",dependsOnMethods={"disconnectDatabaseAfterSuite","unregisterClientsAfterSuite"}, alwaysRun=true)
 	public void return2beaker() {
 
 		Boolean return2beaker = Boolean.valueOf(getProperty("sm.client.return2beaker","false"));
 
 		if (return2beaker) {
 			if (client1!=null) client1.runCommandAndWait("return2beaker.sh");	// return this client back to beaker
 			if (client2!=null) client2.runCommandAndWait("return2beaker.sh");	// return this client back to beaker
 		}
 	}
 
 
 	
 	// Protected Methods ***********************************************************************
 	
 	protected Connection connectToDatabase() {
 		/* Notes on setting up the db for a connection:
 		 * # yum install postgresql-server
 		 * 
 		 * # service postgresql initdb 
 		 * 
 		 * # su - postgres
 		 * $ psql
 		 * # CREATE USER candlepin WITH PASSWORD 'candlepin';
 		 * # ALTER user candlepin CREATEDB;
 		 * [Ctrl-D]
 		 * $ createdb -O candlepin candlepin
 		 * $ exit
 		 * 
 		 * # vi /var/lib/pgsql/data/pg_hba.conf
 		 * # TYPE  DATABASE    USER        CIDR-ADDRESS          METHOD
 		 * local   all         all                               trust
 		 * host    all         all         127.0.0.1/32          trust
 		 *
 		 * # vi /var/lib/pgsql/data/postgresql.conf
 		 * listen_addresses = '*'
 		 * 
 		 * # netstat -lpn | grep 5432
 		 * tcp        0      0 0.0.0.0:5432                0.0.0.0:*                   LISTEN      24935/postmaster    
 		 * tcp        0      0 :::5432                     :::*                        LISTEN      24935/postmaster    
 		 * unix  2      [ ACC ]     STREAM     LISTENING     1717127 24935/postmaster    /tmp/.s.PGSQL.5432
 		 * 
 		 */
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
 			clienttasks.unregister(null,null,null); // make sure client is unregistered
 			for (List<Object> UsernameAndPassword : getUsernameAndPasswordDataAsListOfLists()) {
 				com.redhat.qe.sm.cli.tests.RegisterTests registerTests = new com.redhat.qe.sm.cli.tests.RegisterTests();
 				registerTests.setupBeforeSuite();
 				registerTests.RegisterWithUsernameAndPassword_Test((String)UsernameAndPassword.get(0), (String)UsernameAndPassword.get(1));
 			}
 		}
 	}
 	
 	/**
 	 * On the connected candlepin server database, update the startdate and enddate in the cp_subscription table on rows where the pool id is a match.
 	 * @param pool
 	 * @param startDate
 	 * @param endDate
 	 * @throws SQLException 
 	 */
 	protected void updateSubscriptionPoolDatesOnDatabase(SubscriptionPool pool, Calendar startDate, Calendar endDate) throws SQLException {
 		//DateFormat dateFormat = new SimpleDateFormat(CandlepinAbstraction.dateFormat);
 		String updateSubscriptionPoolEndDateSql = "";
 		String updateSubscriptionPoolStartDateSql = "";
 		if (endDate!=null) {
 			updateSubscriptionPoolEndDateSql = "update cp_subscription set enddate='"+AbstractCommandLineData.formatDateString(endDate)+"' where id=(select pool.subscriptionid from cp_pool pool where pool.id='"+pool.poolId+"');";
 		}
 		if (startDate!=null) {
 			updateSubscriptionPoolStartDateSql = "update cp_subscription set startdate='"+AbstractCommandLineData.formatDateString(startDate)+"' where id=(select pool.subscriptionid from cp_pool pool where pool.id='"+pool.poolId+"');";
 		}
 		
 		Statement sql = dbConnection.createStatement();
 		if (endDate!=null) {
 			log.info("About to change the endDate in the database for this subscription pool: "+pool);
 			log.fine("Executing SQL: "+updateSubscriptionPoolEndDateSql);
 			Assert.assertEquals(sql.executeUpdate(updateSubscriptionPoolEndDateSql), 1, "Updated one row of the cp_subscription table with sql: "+updateSubscriptionPoolEndDateSql);
 		}
 		if (startDate!=null) {
 			log.info("About to change the startDate in the database for this subscription pool: "+pool);
 			log.fine("Executing SQL: "+updateSubscriptionPoolStartDateSql);
 			Assert.assertEquals(sql.executeUpdate(updateSubscriptionPoolStartDateSql), 1, "Updated one row of the cp_subscription table with sql: "+updateSubscriptionPoolStartDateSql);
 		}
 		sql.close();
 	}
 	
 	protected void updateSubscriptionDatesOnDatabase(String subscriptionId, Calendar startDate, Calendar endDate) throws SQLException {
 		//DateFormat dateFormat = new SimpleDateFormat(CandlepinAbstraction.dateFormat);
 		String updateSubscriptionEndDateSql = "";
 		String updateSubscriptionStartDateSql = "";
 		if (endDate!=null) {
 			updateSubscriptionEndDateSql = "update cp_subscription set enddate='"+AbstractCommandLineData.formatDateString(endDate)+"' where id='"+subscriptionId+"';";
 		}
 		if (startDate!=null) {
 			updateSubscriptionStartDateSql = "update cp_subscription set startdate='"+AbstractCommandLineData.formatDateString(startDate)+"' where id='"+subscriptionId+"';";
 		}
 		
 		Statement sql = dbConnection.createStatement();
 		if (endDate!=null) {
 			log.info("About to change the endDate in the database for this subscription id: "+subscriptionId);
 			log.fine("Executing SQL: "+updateSubscriptionEndDateSql);
 			Assert.assertEquals(sql.executeUpdate(updateSubscriptionEndDateSql), 1, "Updated one row of the cp_subscription table with sql: "+updateSubscriptionEndDateSql);
 		}
 		if (startDate!=null) {
 			log.info("About to change the startDate in the database for this subscription id: "+subscriptionId);
 			log.fine("Executing SQL: "+updateSubscriptionStartDateSql);
 			Assert.assertEquals(sql.executeUpdate(updateSubscriptionStartDateSql), 1, "Updated one row of the cp_subscription table with sql: "+updateSubscriptionStartDateSql);
 		}
 		sql.close();
 	}
 	
 
 	protected Calendar parseDateString(String dateString) {
 		String simpleDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"; //"2012-02-08T00:00:00.000+0000"
 		try{
 			DateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
 			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 			Calendar calendar = new GregorianCalendar();
 			calendar.setTimeInMillis(dateFormat.parse(dateString).getTime());
 			return calendar;
 		}
 		catch (ParseException e){
 			log.warning("Failed to parse GMT date string '"+dateString+"' with format '"+simpleDateFormat+"':\n"+e.getMessage());
 			return null;
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
 	
 	
 	@DataProvider(name="getSystemSubscriptionPoolProductData")
 	public Object[][] getSystemSubscriptionPoolProductDataAs2dArray() throws Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getSystemSubscriptionPoolProductDataAsListOfLists());
 	}
 	/* HARDCODED IMPLEMENTATION THAT READS FROM systemSubscriptionPoolProductData
 	protected List<List<Object>> getSystemSubscriptionPoolProductDataAsListOfLists() throws JSONException {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 				
 		for (int j=0; j<systemSubscriptionPoolProductData.length(); j++) {
 			JSONObject poolProductDataAsJSONObject = (JSONObject) systemSubscriptionPoolProductData.get(j);
 			String systemProductId = poolProductDataAsJSONObject.getString("systemProductId");
 			JSONArray bundledProductDataAsJSONArray = poolProductDataAsJSONObject.getJSONArray("bundledProductData");
 
 			// String systemProductId, JSONArray bundledProductDataAsJSONArray
 			ll.add(Arrays.asList(new Object[]{systemProductId, bundledProductDataAsJSONArray}));
 
 			// minimize the number of dataProvided rows (useful during automated testcase development)
 			if (Boolean.valueOf(getProperty("sm.debug.dataProviders.minimize","false"))) break;
 		}
 		
 		return ll;
 	}
 	*/
 	protected List<List<Object>> getSystemSubscriptionPoolProductDataAsListOfLists() throws Exception {
 		return getSystemSubscriptionPoolProductDataAsListOfLists(true);
 	}
 	protected List<List<Object>> getSystemSubscriptionPoolProductDataAsListOfLists(boolean matchSystem) throws Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		List <String> productIdsAddedToSystemSubscriptionPoolProductData = new ArrayList<String>();
 		
 		// get the owner key for clientusername, clientpassword
 		String consumerId = clienttasks.getCurrentConsumerId();
 		if (consumerId==null) consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername, clientpassword, null, null, null, null, Boolean.TRUE, null, null, null));
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(serverHostname, serverPort, serverPrefix, clientusername, clientpassword, consumerId);
 
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(System.currentTimeMillis());
 		
 		// process all of the subscriptions belonging to ownerKey
 		JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,clientusername,clientpassword,"/owners/"+ownerKey+"/subscriptions"));	
 		for (int i = 0; i < jsonSubscriptions.length(); i++) {
 			JSONObject jsonSubscription = (JSONObject) jsonSubscriptions.get(i);
 			
 			// skip subscriptions that are not valid today (at this time now)
 			Calendar startDate = parseDateString(jsonSubscription.getString("startDate"));	// "startDate":"2012-02-08T00:00:00.000+0000"
 			Calendar endDate = parseDateString(jsonSubscription.getString("endDate"));	// "endDate":"2013-02-07T00:00:00.000+0000"
 			if (!(startDate.before(now) && endDate.after(now))) continue;
 			
 			JSONObject jsonProduct = (JSONObject) jsonSubscription.getJSONObject("product");
 			String productId = jsonProduct.getString("id");
 			String productName = jsonProduct.getString("name");
 			
 			// skip subscriptions that have already been added to SystemSubscriptionPoolProductData
 			if (productIdsAddedToSystemSubscriptionPoolProductData.contains(productId)) continue;
 			
 			// process this subscription productId
 			JSONArray jsonProductAttributes = jsonProduct.getJSONArray("attributes");
 			boolean productAttributesPassRulesCheck = true; // assumed
 			String productAttributeSocketsValue = "";
 			for (int j = 0; j < jsonProductAttributes.length(); j++) {
 				JSONObject jsonProductAttribute = (JSONObject) jsonProductAttributes.get(j);
 				String attributeName = jsonProductAttribute.getString("name");
 				String attributeValue = jsonProductAttribute.getString("value");
 				if (attributeName.equals("arch")) {
 					if (!attributeValue.equalsIgnoreCase("ALL") && !attributeValue.equalsIgnoreCase(clienttasks.arch)) {
 						productAttributesPassRulesCheck = false;
 					}
 				}
 				if (attributeName.equals("variant")) {
 //					if (!attributeValue.equalsIgnoreCase("ALL") && !attributeValue.equalsIgnoreCase(clienttasks.variant)) {
 //						productAttributesPassRulesCheck = false;
 //					}
 				}
 				if (attributeName.equals("type")) {
 
 				}
 				if (attributeName.equals("version")) {
 //					if (!attributeValue.equalsIgnoreCase(clienttasks.version)) {
 //						productAttributesPassRulesCheck = false;
 //					}
 				}
 				if (attributeName.equals("requires_consumer_type")) {
 					if (!attributeValue.equalsIgnoreCase(ConsumerType.system.toString())) {
 						productAttributesPassRulesCheck = false;
 					}
 				}
 				if (attributeName.equals("sockets")) {
 					productAttributeSocketsValue = attributeValue;
 					if (Integer.valueOf(attributeValue) < Integer.valueOf(clienttasks.sockets)) {
 						if (matchSystem) productAttributesPassRulesCheck = false;
 					}
 				}
 			}
 			if (productAttributesPassRulesCheck) {
 				
 				// process this subscription's providedProducts
 				JSONArray jsonBundledProductData = new JSONArray();
 				JSONArray jsonProvidedProducts = (JSONArray) jsonSubscription.getJSONArray("providedProducts");
 				for (int k = 0; k < jsonProvidedProducts.length(); k++) {
 					JSONObject jsonProvidedProduct = (JSONObject) jsonProvidedProducts.get(k);
 					String providedProductName = jsonProvidedProduct.getString("name");
 					String providedProductId = jsonProvidedProduct.getString("id");
 
 					
 					JSONArray jsonProvidedProductAttributes = jsonProvidedProduct.getJSONArray("attributes");
 					boolean providedProductAttributesPassRulesCheck = true; // assumed
 					for (int l = 0; l < jsonProvidedProductAttributes.length(); l++) {
 						JSONObject jsonProvidedProductAttribute = (JSONObject) jsonProvidedProductAttributes.get(l);
 						String attributeName = jsonProvidedProductAttribute.getString("name");
 						String attributeValue = jsonProvidedProductAttribute.getString("value");
 						if (attributeName.equals("arch")) {
 							if (!attributeValue.equalsIgnoreCase("ALL") && !attributeValue.equalsIgnoreCase(clienttasks.arch)) {
 								providedProductAttributesPassRulesCheck = false;
 							}
 						}
 						if (attributeName.equals("variant")) {
 //								if (!attributeValue.equalsIgnoreCase("ALL") && !attributeValue.equalsIgnoreCase(clienttasks.variant)) {
 //									providedProductAttributesPassRulesCheck = false;
 //								}
 						}
 						if (attributeName.equals("type")) {
 							if (attributeValue.equals("MKT")) { // provided products of type "MKT" should not pass the rules check
 								providedProductAttributesPassRulesCheck = false;
 							}
 						}
 						if (attributeName.equals("version")) {
 //								if (!attributeValue.equalsIgnoreCase(clienttasks.version)) {
 //									providedProductAttributesPassRulesCheck = false;
 //								}
 						}
 						if (attributeName.equals("requires_consumer_type")) {
 							if (!attributeValue.equalsIgnoreCase(ConsumerType.system.toString())) {
 								providedProductAttributesPassRulesCheck = false;
 							}
 						}
 						if (attributeName.equals("sockets")) {
 //							if (Integer.valueOf(attributeValue) < Integer.valueOf(clienttasks.sockets)) {
 //								if (matchSystem) providedProductAttributesPassRulesCheck = false;
 //							}
 //							if (Integer.valueOf(attributeValue) > Integer.valueOf(clienttasks.sockets)) {
 //								providedProductAttributesPassRulesCheck = false;
 //							}
 							if (!attributeValue.equals(productAttributeSocketsValue)) {
 								log.warning("THE VALIDITY OF SUBSCRIPTION '"+productName+"' WITH PROVIDED PRODUCT '"+providedProductName+"' IS QUESTIONABLE.  THE PROVIDED PRODUCT '"+providedProductId+"' SOCKETS ATTRIBUTE '"+attributeValue+"' DOES NOT MATCH THE BASE SUBSCRIPTION PRODUCT '"+productId+"' SOCKETS ATTRIBUTE '"+productAttributeSocketsValue+"'.");
 							}
 							if (!productAttributeSocketsValue.equals("") && Integer.valueOf(attributeValue) > Integer.valueOf(productAttributeSocketsValue)) {
 								providedProductAttributesPassRulesCheck = false;
 							}
 						}
 
 					}
 					if (providedProductAttributesPassRulesCheck) {
 						JSONObject bundledProduct = new JSONObject(String.format("{productName:'%s'}", providedProductName));
 
 						jsonBundledProductData.put(bundledProduct);
 					}
 				}
 				// Example:
 				// < {systemProductId:'awesomeos-modifier', bundledProductData:<{productName:'Awesome OS Modifier Bits'}>} , {systemProductId:'awesomeos-server', bundledProductData:<{productName:'Awesome OS Server Bits'},{productName:'Clustering Bits'},{productName:'Shared Storage Bits'},{productName:'Management Bits'},{productName:'Large File Support Bits'},{productName:'Load Balancing Bits'}>} , {systemProductId:'awesomeos-server-basic', bundledProductData:<{productName:'Awesome OS Server Bits'}>} , {systemProductId:'awesomeos-workstation-basic', bundledProductData:<{productName:'Awesome OS Workstation Bits'}>} , {systemProductId:'awesomeos-server-2-socket-std', bundledProductData:<{productName:'Awesome OS Server Bits'},{productName:'Clustering Bits'},{productName:'Shared Storage Bits'},{productName:'Management Bits'},{productName:'Large File Support Bits'},{productName:'Load Balancing Bits'}>} , {systemProductId:'awesomeos-virt-4', bundledProductData:<{productName:'Awesome OS Server Bits'}>} , {systemProductId:'awesomeos-server-2-socket-prem', bundledProductData:<{productName:'Awesome OS Server Bits'},{productName:'Clustering Bits'},{productName:'Shared Storage Bits'},{productName:'Management Bits'},{productName:'Large File Support Bits'},{productName:'Load Balancing Bits'}>} , {systemProductId:'awesomeos-virt-4', bundledProductData:<{productName:'Awesome OS Server Bits'}>} , {systemProductId:'awesomeos-server-4-socket-prem',bundledProductData:<{productName:'Awesome OS Server Bits'},{productName:'Clustering Bits'},{productName:'Shared Storage Bits'},{productName:'Management Bits'},{productName:'Large File Support Bits'},{productName:'Load Balancing Bits'}>} , {systemProductId:'awesomeos-virt-4', bundledProductData:<{productName:'Awesome OS Server Bits'}>} , {systemProductId:'awesomeos-server-2-socket-bas', bundledProductData:<{productName:'Awesome OS Server Bits'},{productName:'Clustering Bits'},{productName:'Shared Storage Bits'},{productName:'Management Bits'},{productName:'Large File Support Bits'},{productName:'Load Balancing Bits'}>} , {systemProductId:'awesomeos-virt-4', bundledProductData:<{productName:'Awesome OS Server Bits'}>} , {systemProductId:'management-100', bundledProductData:<{productName:'Management Add-On'}>} , {systemProductId:'awesomeos-scalable-fs', bundledProductData:<{productName:'Awesome OS Scalable Filesystem Bits'}>}>
 
 				// String systemProductId, JSONArray bundledProductDataAsJSONArray
 				ll.add(Arrays.asList(new Object[]{productId, jsonBundledProductData}));
 				productIdsAddedToSystemSubscriptionPoolProductData.add(productId);
 			}
 		}
 		
 		return ll;
 		
 	}
 	
 	/* SUBSCRIPTION WITH BUNDLED PRODUCTS
 	
 	[root@jsefler-onprem-server ~]# curl -k -u admin:admin --request GET https://jsefler-f14-candlepin.usersys.redhat.com:8443/candlepin/subscriptions/8a90f8b42ee62404012ee624918b00a9 | json_reformat 
 		  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
 		                                 Dload  Upload   Total   Spent    Left  Speed
 		100 13941    0 13941    0     0   127k      0 --:--:-- --:--:-- --:--:--  412k
 		{
 		  "id": "8a90f8b42ee62404012ee624918b00a9",
 		  "owner": {
 		    "href": "/owners/admin",
 		    "id": "8a90f8b42ee62404012ee62448260005"
 		  },
 		  "certificate": null,
 		  "product": {
 		    "name": "Awesome OS Server Bundled (2 Sockets, Standard Support)",
 		    "id": "awesomeos-server-2-socket-std",
 		    "attributes": [
 		      {
 		        "name": "variant",
 		        "value": "ALL",
 		        "updated": "2011-03-24T04:34:39.173+0000",
 		        "created": "2011-03-24T04:34:39.173+0000"
 		      },
 		      {
 		        "name": "sockets",
 		        "value": "2",
 		        "updated": "2011-03-24T04:34:39.174+0000",
 		        "created": "2011-03-24T04:34:39.174+0000"
 		      },
 		      {
 		        "name": "arch",
 		        "value": "ALL",
 		        "updated": "2011-03-24T04:34:39.174+0000",
 		        "created": "2011-03-24T04:34:39.174+0000"
 		      },
 		      {
 		        "name": "support_level",
 		        "value": "Standard",
 		        "updated": "2011-03-24T04:34:39.174+0000",
 		        "created": "2011-03-24T04:34:39.174+0000"
 		      },
 		      {
 		        "name": "support_type",
 		        "value": "L1-L3",
 		        "updated": "2011-03-24T04:34:39.175+0000",
 		        "created": "2011-03-24T04:34:39.175+0000"
 		      },
 		      {
 		        "name": "management_enabled",
 		        "value": "1",
 		        "updated": "2011-03-24T04:34:39.175+0000",
 		        "created": "2011-03-24T04:34:39.175+0000"
 		      },
 		      {
 		        "name": "type",
 		        "value": "MKT",
 		        "updated": "2011-03-24T04:34:39.175+0000",
 		        "created": "2011-03-24T04:34:39.175+0000"
 		      },
 		      {
 		        "name": "warning_period",
 		        "value": "30",
 		        "updated": "2011-03-24T04:34:39.176+0000",
 		        "created": "2011-03-24T04:34:39.176+0000"
 		      },
 		      {
 		        "name": "version",
 		        "value": "6.1",
 		        "updated": "2011-03-24T04:34:39.176+0000",
 		        "created": "2011-03-24T04:34:39.176+0000"
 		      }
 		    ],
 		    "multiplier": 1,
 		    "productContent": [
 
 		    ],
 		    "dependentProductIds": [
 
 		    ],
 		    "href": "/products/awesomeos-server-2-socket-std",
 		    "updated": "2011-03-24T04:34:39.173+0000",
 		    "created": "2011-03-24T04:34:39.173+0000"
 		  },
 		  "providedProducts": [
 		    {
 		      "name": "Clustering Bits",
 		      "id": "37065",
 		      "attributes": [
 		        {
 		          "name": "version",
 		          "value": "1.0",
 		          "updated": "2011-03-24T04:34:26.104+0000",
 		          "created": "2011-03-24T04:34:26.104+0000"
 		        },
 		        {
 		          "name": "variant",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:26.104+0000",
 		          "created": "2011-03-24T04:34:26.104+0000"
 		        },
 		        {
 		          "name": "sockets",
 		          "value": "2",
 		          "updated": "2011-03-24T04:34:26.104+0000",
 		          "created": "2011-03-24T04:34:26.104+0000"
 		        },
 		        {
 		          "name": "arch",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:26.104+0000",
 		          "created": "2011-03-24T04:34:26.104+0000"
 		        },
 		        {
 		          "name": "type",
 		          "value": "SVC",
 		          "updated": "2011-03-24T04:34:26.104+0000",
 		          "created": "2011-03-24T04:34:26.104+0000"
 		        }
 		      ],
 		      "multiplier": 1,
 		      "productContent": [
 		        {
 		          "content": {
 		            "name": "always-enabled-content",
 		            "id": "1",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/always",
 		            "label": "always-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/always/gpg",
 		            "metadataExpire": 200,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.415+0000",
 		            "created": "2011-03-24T04:34:25.415+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": true
 		        },
 		        {
 		          "content": {
 		            "name": "never-enabled-content",
 		            "id": "0",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/never",
 		            "label": "never-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/never/gpg",
 		            "metadataExpire": 600,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.277+0000",
 		            "created": "2011-03-24T04:34:25.277+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": false
 		        }
 		      ],
 		      "dependentProductIds": [
 
 		      ],
 		      "href": "/products/37065",
 		      "updated": "2011-03-24T04:34:26.103+0000",
 		      "created": "2011-03-24T04:34:26.103+0000"
 		    },
 		    {
 		      "name": "Awesome OS Server Bundled",
 		      "id": "awesomeos-server",
 		      "attributes": [
 		        {
 		          "name": "version",
 		          "value": "1.0",
 		          "updated": "2011-03-24T04:34:35.841+0000",
 		          "created": "2011-03-24T04:34:35.841+0000"
 		        },
 		        {
 		          "name": "variant",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:35.841+0000",
 		          "created": "2011-03-24T04:34:35.841+0000"
 		        },
 		        {
 		          "name": "support_level",
 		          "value": "Premium",
 		          "updated": "2011-03-24T04:34:35.841+0000",
 		          "created": "2011-03-24T04:34:35.841+0000"
 		        },
 		        {
 		          "name": "sockets",
 		          "value": "2",
 		          "updated": "2011-03-24T04:34:35.841+0000",
 		          "created": "2011-03-24T04:34:35.841+0000"
 		        },
 		        {
 		          "name": "arch",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:35.841+0000",
 		          "created": "2011-03-24T04:34:35.841+0000"
 		        },
 		        {
 		          "name": "management_enabled",
 		          "value": "1",
 		          "updated": "2011-03-24T04:34:35.842+0000",
 		          "created": "2011-03-24T04:34:35.842+0000"
 		        },
 		        {
 		          "name": "type",
 		          "value": "MKT",
 		          "updated": "2011-03-24T04:34:35.842+0000",
 		          "created": "2011-03-24T04:34:35.842+0000"
 		        },
 		        {
 		          "name": "warning_period",
 		          "value": "30",
 		          "updated": "2011-03-24T04:34:35.842+0000",
 		          "created": "2011-03-24T04:34:35.842+0000"
 		        },
 		        {
 		          "name": "support_type",
 		          "value": "Level 3",
 		          "updated": "2011-03-24T04:34:35.842+0000",
 		          "created": "2011-03-24T04:34:35.842+0000"
 		        }
 		      ],
 		      "multiplier": 1,
 		      "productContent": [
 
 		      ],
 		      "dependentProductIds": [
 
 		      ],
 		      "href": "/products/awesomeos-server",
 		      "updated": "2011-03-24T04:34:35.841+0000",
 		      "created": "2011-03-24T04:34:35.841+0000"
 		    },
 		    {
 		      "name": "Awesome OS Server Bits",
 		      "id": "37060",
 		      "attributes": [
 		        {
 		          "name": "variant",
 		          "value": "ALL",
 		          "updated": "2011-03-24T05:28:28.464+0000",
 		          "created": "2011-03-24T05:28:28.464+0000"
 		        },
 		        {
 		          "name": "sockets",
 		          "value": "2",
 		          "updated": "2011-03-24T05:28:28.465+0000",
 		          "created": "2011-03-24T05:28:28.465+0000"
 		        },
 		        {
 		          "name": "arch",
 		          "value": "ALL",
 		          "updated": "2011-03-24T05:28:28.464+0000",
 		          "created": "2011-03-24T05:28:28.464+0000"
 		        },
 		        {
 		          "name": "type",
 		          "value": "SVC",
 		          "updated": "2011-03-24T05:28:28.465+0000",
 		          "created": "2011-03-24T05:28:28.465+0000"
 		        },
 		        {
 		          "name": "warning_period",
 		          "value": "30",
 		          "updated": "2011-03-24T05:28:28.465+0000",
 		          "created": "2011-03-24T05:28:28.465+0000"
 		        },
 		        {
 		          "name": "version",
 		          "value": "6.1",
 		          "updated": "2011-03-24T05:28:28.465+0000",
 		          "created": "2011-03-24T05:28:28.465+0000"
 		        }
 		      ],
 		      "multiplier": 1,
 		      "productContent": [
 		        {
 		          "content": {
 		            "name": "tagged-content",
 		            "id": "2",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/always",
 		            "label": "tagged-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/always/gpg",
 		            "metadataExpire": null,
 		            "requiredTags": "TAG1,TAG2",
 		            "updated": "2011-03-24T04:34:25.482+0000",
 		            "created": "2011-03-24T04:34:25.482+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": true
 		        },
 		        {
 		          "content": {
 		            "name": "always-enabled-content",
 		            "id": "1",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/always",
 		            "label": "always-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/always/gpg",
 		            "metadataExpire": 200,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.415+0000",
 		            "created": "2011-03-24T04:34:25.415+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": true
 		        },
 		        {
 		          "content": {
 		            "name": "never-enabled-content",
 		            "id": "0",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/never",
 		            "label": "never-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/never/gpg",
 		            "metadataExpire": 600,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.277+0000",
 		            "created": "2011-03-24T04:34:25.277+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": false
 		        },
 		        {
 		          "content": {
 		            "name": "content",
 		            "id": "1111",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path",
 		            "label": "content-label",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/gpg/",
 		            "metadataExpire": 0,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.559+0000",
 		            "created": "2011-03-24T04:34:25.559+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": true
 		        }
 		      ],
 		      "dependentProductIds": [
 
 		      ],
 		      "href": "/products/37060",
 		      "updated": "2011-03-24T04:34:32.608+0000",
 		      "created": "2011-03-24T04:34:32.608+0000"
 		    },
 		    {
 		      "name": "Load Balancing Bits",
 		      "id": "37070",
 		      "attributes": [
 		        {
 		          "name": "version",
 		          "value": "1.0",
 		          "updated": "2011-03-24T04:34:27.252+0000",
 		          "created": "2011-03-24T04:34:27.252+0000"
 		        },
 		        {
 		          "name": "variant",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:27.252+0000",
 		          "created": "2011-03-24T04:34:27.252+0000"
 		        },
 		        {
 		          "name": "sockets",
 		          "value": "2",
 		          "updated": "2011-03-24T04:34:27.253+0000",
 		          "created": "2011-03-24T04:34:27.253+0000"
 		        },
 		        {
 		          "name": "arch",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:27.252+0000",
 		          "created": "2011-03-24T04:34:27.252+0000"
 		        },
 		        {
 		          "name": "type",
 		          "value": "SVC",
 		          "updated": "2011-03-24T04:34:27.253+0000",
 		          "created": "2011-03-24T04:34:27.253+0000"
 		        },
 		        {
 		          "name": "warning_period",
 		          "value": "30",
 		          "updated": "2011-03-24T04:34:27.253+0000",
 		          "created": "2011-03-24T04:34:27.253+0000"
 		        }
 		      ],
 		      "multiplier": 1,
 		      "productContent": [
 		        {
 		          "content": {
 		            "name": "always-enabled-content",
 		            "id": "1",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/always",
 		            "label": "always-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/always/gpg",
 		            "metadataExpire": 200,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.415+0000",
 		            "created": "2011-03-24T04:34:25.415+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": true
 		        },
 		        {
 		          "content": {
 		            "name": "never-enabled-content",
 		            "id": "0",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/never",
 		            "label": "never-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/never/gpg",
 		            "metadataExpire": 600,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.277+0000",
 		            "created": "2011-03-24T04:34:25.277+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": false
 		        }
 		      ],
 		      "dependentProductIds": [
 
 		      ],
 		      "href": "/products/37070",
 		      "updated": "2011-03-24T04:34:27.251+0000",
 		      "created": "2011-03-24T04:34:27.251+0000"
 		    },
 		    {
 		      "name": "Large File Support Bits",
 		      "id": "37068",
 		      "attributes": [
 		        {
 		          "name": "version",
 		          "value": "1.0",
 		          "updated": "2011-03-24T04:34:30.292+0000",
 		          "created": "2011-03-24T04:34:30.292+0000"
 		        },
 		        {
 		          "name": "variant",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:30.293+0000",
 		          "created": "2011-03-24T04:34:30.293+0000"
 		        },
 		        {
 		          "name": "sockets",
 		          "value": "2",
 		          "updated": "2011-03-24T04:34:30.293+0000",
 		          "created": "2011-03-24T04:34:30.293+0000"
 		        },
 		        {
 		          "name": "arch",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:30.293+0000",
 		          "created": "2011-03-24T04:34:30.293+0000"
 		        },
 		        {
 		          "name": "type",
 		          "value": "SVC",
 		          "updated": "2011-03-24T04:34:30.293+0000",
 		          "created": "2011-03-24T04:34:30.293+0000"
 		        },
 		        {
 		          "name": "warning_period",
 		          "value": "30",
 		          "updated": "2011-03-24T04:34:30.293+0000",
 		          "created": "2011-03-24T04:34:30.293+0000"
 		        }
 		      ],
 		      "multiplier": 1,
 		      "productContent": [
 		        {
 		          "content": {
 		            "name": "always-enabled-content",
 		            "id": "1",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/always",
 		            "label": "always-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/always/gpg",
 		            "metadataExpire": 200,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.415+0000",
 		            "created": "2011-03-24T04:34:25.415+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": true
 		        },
 		        {
 		          "content": {
 		            "name": "never-enabled-content",
 		            "id": "0",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/never",
 		            "label": "never-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/never/gpg",
 		            "metadataExpire": 600,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.277+0000",
 		            "created": "2011-03-24T04:34:25.277+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": false
 		        }
 		      ],
 		      "dependentProductIds": [
 
 		      ],
 		      "href": "/products/37068",
 		      "updated": "2011-03-24T04:34:30.292+0000",
 		      "created": "2011-03-24T04:34:30.292+0000"
 		    },
 		    {
 		      "name": "Shared Storage Bits",
 		      "id": "37067",
 		      "attributes": [
 		        {
 		          "name": "version",
 		          "value": "1.0",
 		          "updated": "2011-03-24T04:34:28.860+0000",
 		          "created": "2011-03-24T04:34:28.860+0000"
 		        },
 		        {
 		          "name": "variant",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:28.861+0000",
 		          "created": "2011-03-24T04:34:28.861+0000"
 		        },
 		        {
 		          "name": "sockets",
 		          "value": "2",
 		          "updated": "2011-03-24T04:34:28.861+0000",
 		          "created": "2011-03-24T04:34:28.861+0000"
 		        },
 		        {
 		          "name": "arch",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:28.861+0000",
 		          "created": "2011-03-24T04:34:28.861+0000"
 		        },
 		        {
 		          "name": "type",
 		          "value": "SVC",
 		          "updated": "2011-03-24T04:34:28.861+0000",
 		          "created": "2011-03-24T04:34:28.861+0000"
 		        },
 		        {
 		          "name": "warning_period",
 		          "value": "30",
 		          "updated": "2011-03-24T04:34:28.861+0000",
 		          "created": "2011-03-24T04:34:28.861+0000"
 		        }
 		      ],
 		      "multiplier": 1,
 		      "productContent": [
 		        {
 		          "content": {
 		            "name": "always-enabled-content",
 		            "id": "1",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/always",
 		            "label": "always-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/always/gpg",
 		            "metadataExpire": 200,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.415+0000",
 		            "created": "2011-03-24T04:34:25.415+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": true
 		        },
 		        {
 		          "content": {
 		            "name": "never-enabled-content",
 		            "id": "0",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/never",
 		            "label": "never-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/never/gpg",
 		            "metadataExpire": 600,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.277+0000",
 		            "created": "2011-03-24T04:34:25.277+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": false
 		        }
 		      ],
 		      "dependentProductIds": [
 
 		      ],
 		      "href": "/products/37067",
 		      "updated": "2011-03-24T04:34:28.859+0000",
 		      "created": "2011-03-24T04:34:28.859+0000"
 		    },
 		    {
 		      "name": "Management Bits",
 		      "id": "37069",
 		      "attributes": [
 		        {
 		          "name": "version",
 		          "value": "1.0",
 		          "updated": "2011-03-24T04:34:31.181+0000",
 		          "created": "2011-03-24T04:34:31.181+0000"
 		        },
 		        {
 		          "name": "variant",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:31.181+0000",
 		          "created": "2011-03-24T04:34:31.181+0000"
 		        },
 		        {
 		          "name": "sockets",
 		          "value": "2",
 		          "updated": "2011-03-24T04:34:31.181+0000",
 		          "created": "2011-03-24T04:34:31.181+0000"
 		        },
 		        {
 		          "name": "arch",
 		          "value": "ALL",
 		          "updated": "2011-03-24T04:34:31.181+0000",
 		          "created": "2011-03-24T04:34:31.181+0000"
 		        },
 		        {
 		          "name": "type",
 		          "value": "SVC",
 		          "updated": "2011-03-24T04:34:31.181+0000",
 		          "created": "2011-03-24T04:34:31.181+0000"
 		        },
 		        {
 		          "name": "warning_period",
 		          "value": "30",
 		          "updated": "2011-03-24T04:34:31.181+0000",
 		          "created": "2011-03-24T04:34:31.181+0000"
 		        }
 		      ],
 		      "multiplier": 1,
 		      "productContent": [
 		        {
 		          "content": {
 		            "name": "always-enabled-content",
 		            "id": "1",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/always",
 		            "label": "always-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/always/gpg",
 		            "metadataExpire": 200,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.415+0000",
 		            "created": "2011-03-24T04:34:25.415+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": true
 		        },
 		        {
 		          "content": {
 		            "name": "never-enabled-content",
 		            "id": "0",
 		            "type": "yum",
 		            "modifiedProductIds": [
 
 		            ],
 		            "contentUrl": "/foo/path/never",
 		            "label": "never-enabled-content",
 		            "vendor": "test-vendor",
 		            "gpgUrl": "/foo/path/never/gpg",
 		            "metadataExpire": 600,
 		            "requiredTags": null,
 		            "updated": "2011-03-24T04:34:25.277+0000",
 		            "created": "2011-03-24T04:34:25.277+0000"
 		          },
 		          "flexEntitlement": 0,
 		          "physicalEntitlement": 0,
 		          "enabled": false
 		        }
 		      ],
 		      "dependentProductIds": [
 
 		      ],
 		      "href": "/products/37069",
 		      "updated": "2011-03-24T04:34:31.180+0000",
 		      "created": "2011-03-24T04:34:31.180+0000"
 		    }
 		  ],
 		  "endDate": "2013-03-13T00:00:00.000+0000",
 		  "startDate": "2012-03-13T00:00:00.000+0000",
 		  "quantity": 15,
 		  "contractNumber": "20",
 		  "accountNumber": "12331131231",
 		  "modified": null,
 		  "tokens": [
 
 		  ],
 		  "upstreamPoolId": null,
 		  "updated": "2011-03-24T04:34:39.627+0000",
 		  "created": "2011-03-24T04:34:39.627+0000"
 		}
 	*/
 	
 
 
 
 }
