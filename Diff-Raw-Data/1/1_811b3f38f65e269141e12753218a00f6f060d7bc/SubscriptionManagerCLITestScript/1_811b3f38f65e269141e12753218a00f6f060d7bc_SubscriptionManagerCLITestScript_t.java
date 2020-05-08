 package com.redhat.qe.sm.base;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
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
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.DataProvider;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.cli.tasks.SubscriptionManagerTasks;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.Org;
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
 	public void setupBeforeSuite() throws IOException, JSONException {
 		if (isSetupBeforeSuiteComplete) return;
 		
 		// create SSHCommandRunners to connect to the subscription-manager clients
 		client = new SSHCommandRunner(sm_clientHostname, sm_sshUser, sm_sshKeyPrivate, sm_sshkeyPassphrase, null);
 		clienttasks = new SubscriptionManagerTasks(client);
 		client1 = client;
 		client1tasks = clienttasks;
 		
 		// will we be testing multiple clients?
 		if (!(	sm_client2Hostname.equals("") /*|| client2username.equals("") || client2password.equals("")*/ )) {
 			client2 = new SSHCommandRunner(sm_client2Hostname, sm_sshUser, sm_sshKeyPrivate, sm_sshkeyPassphrase, null);
 			client2tasks = new SubscriptionManagerTasks(client2);
 		} else {
 			log.info("Multi-client testing will be skipped.");
 		}
 		
 		// unregister clients in case they are still registered from prior run (DO THIS BEFORE SETTING UP A NEW CANDLEPIN)
 		unregisterClientsAfterSuite();
 		
 		
 		File serverCaCertFile = null;
 		List<File> generatedProductCertFiles = new ArrayList<File>();
 		
 		// can we create an SSHCommandRunner to connect to the candlepin server ?
 		if (!sm_serverHostname.equals("") && sm_isServerOnPremises) {
 			server = new SSHCommandRunner(sm_serverHostname, sm_sshUser, sm_sshKeyPrivate, sm_sshkeyPassphrase, null);
 			servertasks = new com.redhat.qe.sm.cli.tasks.CandlepinTasks(server,sm_serverInstallDir,sm_serverImportDir,sm_isServerOnPremises,sm_serverBranch);
 		} else {
 			log.info("Assuming the server is already setup and running.");
 			servertasks = new com.redhat.qe.sm.cli.tasks.CandlepinTasks(null,null,null,sm_isServerOnPremises,sm_serverBranch);
 		}
 		
 		// setup the candlepin server
 		if (server!=null && servertasks.isOnPremises) {
 			
 			// NOTE: After updating the candlepin.conf file, the server needs to be restarted, therefore this will not work against the Hosted IT server which we don't want to restart or deploy
 			//       I suggest manually setting this on hosted and asking calfanso to restart
 			servertasks.updateConfigFileParameter("pinsetter.org.fedoraproject.candlepin.pinsetter.tasks.CertificateRevocationListTask.schedule","0 0\\/2 * * * ?");  // every 2 minutes
 			servertasks.cleanOutCRL();
 			servertasks.deploy();
 			servertasks.reportAPI();
 			
 			// also connect to the candlepin server database
 			dbConnection = connectToDatabase();  // do this after the call to deploy since deploy will restart postgresql
 			
 			// fetch the candlepin CA Cert
 			log.info("Fetching Candlepin CA cert...");
 			serverCaCertFile = new File((getProperty("automation.dir", "/tmp")+"/tmp/"+servertasks.candlepinCACertFile.getName()).replace("tmp/tmp", "tmp"));
 			RemoteFileTasks.getFile(server.getConnection(), serverCaCertFile.getParent(), servertasks.candlepinCACertFile.getPath());
 			
 			// fetch the generated Product Certs
 			if (Boolean.valueOf(getProperty("sm.debug.fetchProductCerts","true"))) {
 			log.info("Fetching the generated product certs...");
 			//SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(server, "find "+serverInstallDir+servertasks.generatedProductsDir+" -name '*.pem'", 0);
 			SSHCommandResult result = server.runCommandAndWait("find "+sm_serverInstallDir+servertasks.generatedProductsDir+" -name '*.pem'");
 			String[] remoteFilesAsString = result.getStdout().trim().split("\\n");
 			if (remoteFilesAsString.length==1 && remoteFilesAsString[0].equals("")) remoteFilesAsString = new String[]{};
 			if (remoteFilesAsString.length==0) log.warning("No generated product certs were found on the candlpin server for use in testing.");
 			for (String remoteFileAsString : remoteFilesAsString) {
 				File remoteFile = new File(remoteFileAsString);
 				File localFile = new File((getProperty("automation.dir", "/tmp")+"/tmp/"+remoteFile.getName()).replace("tmp/tmp", "tmp"));
 				File localFileRenamed = new File(localFile.getPath().replace(".pem", "_.pem")); // rename the generated productCertFile to help distinguish it from a true RHEL productCertFiles
 				RemoteFileTasks.getFile(server.getConnection(), localFile.getParent(),remoteFile.getPath());
 				localFile.renameTo(localFileRenamed);
 				generatedProductCertFiles.add(localFileRenamed);
 			}
 			}
 		}
 		
 		// setup the client(s)
 		for (SubscriptionManagerTasks smt : new SubscriptionManagerTasks[]{client2tasks, client1tasks}) {
 			if (smt==null) continue;
 			
 			smt.installSubscriptionManagerRPMs(sm_rpmUrls,sm_yumInstallOptions);
 			
 			// rhsm.conf [server] configurations
 			if (!sm_serverHostname.equals(""))				smt.updateConfFileParameter(smt.rhsmConfFile, "hostname", sm_serverHostname);							else sm_serverHostname = smt.getConfFileParameter(smt.rhsmConfFile, "hostname");
 			if (!sm_serverPrefix.equals(""))				smt.updateConfFileParameter(smt.rhsmConfFile, "prefix", sm_serverPrefix);								else sm_serverPrefix = smt.getConfFileParameter(smt.rhsmConfFile, "prefix");
 			if (!sm_serverPort.equals(""))					smt.updateConfFileParameter(smt.rhsmConfFile, "port", sm_serverPort);									else sm_serverPort = smt.getConfFileParameter(smt.rhsmConfFile, "port");
 			if (!sm_serverInsecure.equals(""))				smt.updateConfFileParameter(smt.rhsmConfFile, "insecure", sm_serverInsecure);							else sm_serverInsecure = smt.getConfFileParameter(smt.rhsmConfFile, "insecure");
 			if (!sm_serverSslVerifyDepth.equals(""))		smt.updateConfFileParameter(smt.rhsmConfFile, "ssl_verify_depth", sm_serverSslVerifyDepth);							else sm_serverInsecure = smt.getConfFileParameter(smt.rhsmConfFile, "insecure");
 			if (!sm_serverCaCertDir.equals(""))				smt.updateConfFileParameter(smt.rhsmConfFile, "ca_cert_dir", sm_serverCaCertDir);						else sm_serverCaCertDir = smt.getConfFileParameter(smt.rhsmConfFile, "ca_cert_dir");
 
 			// rhsm.conf [rhsm] configurations
 			if (!sm_rhsmBaseUrl.equals(""))					smt.updateConfFileParameter(smt.rhsmConfFile, "baseurl", sm_rhsmBaseUrl);								else sm_rhsmBaseUrl = smt.getConfFileParameter(smt.rhsmConfFile, "baseurl");
 			if (!sm_rhsmRepoCaCert.equals(""))				smt.updateConfFileParameter(smt.rhsmConfFile, "repo_ca_cert", sm_rhsmRepoCaCert);						else sm_rhsmRepoCaCert = smt.getConfFileParameter(smt.rhsmConfFile, "repo_ca_cert");
 			//if (!rhsmShowIncompatiblePools.equals(""))	smt.updateConfFileParameter(smt.rhsmConfFile, "showIncompatiblePools", rhsmShowIncompatiblePools);	else rhsmShowIncompatiblePools = smt.getConfFileParameter(smt.rhsmConfFile, "showIncompatiblePools");
 			if (!sm_rhsmProductCertDir.equals(""))			smt.updateConfFileParameter(smt.rhsmConfFile, "productCertDir", sm_rhsmProductCertDir);				else sm_rhsmProductCertDir = smt.getConfFileParameter(smt.rhsmConfFile, "productCertDir");
 			if (!sm_rhsmEntitlementCertDir.equals(""))		smt.updateConfFileParameter(smt.rhsmConfFile, "entitlementCertDir", sm_rhsmEntitlementCertDir);		else sm_rhsmEntitlementCertDir = smt.getConfFileParameter(smt.rhsmConfFile, "entitlementCertDir");
 			if (!sm_rhsmConsumerCertDir.equals(""))			smt.updateConfFileParameter(smt.rhsmConfFile, "consumerCertDir", sm_rhsmConsumerCertDir);				else sm_rhsmConsumerCertDir = smt.getConfFileParameter(smt.rhsmConfFile, "consumerCertDir");
 
 			// rhsm.conf [rhsmcertd] configurations
 			if (!sm_rhsmcertdCertFrequency.equals(""))		smt.updateConfFileParameter(smt.rhsmConfFile, "certFrequency", sm_rhsmcertdCertFrequency);				else sm_rhsmcertdCertFrequency = smt.getConfFileParameter(smt.rhsmConfFile, "certFrequency");
 		
 			smt.initializeFieldsFromConfigFile();
 			smt.removeAllCerts(true,true);
 			smt.installRepoCaCerts(sm_repoCaCertUrls);
 			
 			// transfer a copy of the candlepin CA Cert from the candlepin server to the clients so we can test in secure mode
 			log.info("Copying Candlepin cert onto client to enable certificate validation...");
 			smt.installRepoCaCert(serverCaCertFile, sm_serverHostname.split("\\.")[0]+".pem");
 			
 			// transfer copies of all the generated product certs from the candlepin server to the clients
 			log.info("Copying Candlepin generated product certs onto client to simulate installed products...");
 			smt.installProductCerts(generatedProductCertFiles);
 		}
 		
 		// determine the server URL that will be used for candlepin API calls
		if (sm_serverUrl==null) sm_serverUrl = getProperty("sm.server.url","");
 		if (sm_serverUrl.equals("")) {
 			sm_serverUrl = getServerUrl(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile,"hostname"), clienttasks.getConfFileParameter(clienttasks.rhsmConfFile,"port"), clienttasks.getConfFileParameter(clienttasks.rhsmConfFile,"prefix"));
 		}
 		
 		log.info("Installed version of candlepin...");
 		JSONObject jsonStatus =null;
 		try {
 			//jsonStatus = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverHostname,sm_serverPort,sm_serverPrefix,"anybody","password","/status")); // seems to work no matter what credentials are passed		
 			//jsonStatus = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverHostname,sm_serverPort,sm_serverPrefix,"","","/status"));
 			//The above call works against onpremises, but causes the following againsta stage
 			//201108251644:10.040 - INFO: SSH alternative to HTTP request: curl -k  --request GET https://rubyvip.web.stage.ext.phx2.redhat.com:80/clonepin/candlepin/status (com.redhat.qe.sm.cli.tasks.CandlepinTasks.getResourceUsingRESTfulAPI)
 			//201108251644:10.049 - WARNING: Required credentials not available for BASIC <any realm>@rubyvip.web.stage.ext.phx2.redhat.com:80 (org.apache.commons.httpclient.HttpMethodDirector.authenticateHost)
 			//201108251644:10.052 - WARNING: Preemptive authentication requested but no default credentials available (org.apache.commons.httpclient.HttpMethodDirector.authenticateHost)
 			jsonStatus = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,"/status"));
 		} catch (Exception e) {
 			log.warning("Candlepin server '"+sm_serverHostname+"' is running version: UNKNOWN");
 		} finally {
 			if (jsonStatus!=null) {
 				log.info("Candlepin server '"+sm_serverHostname+"' is running release '"+jsonStatus.getString("release")+"' version '"+jsonStatus.get("version")+"'.");
 				Assert.assertEquals(jsonStatus.getBoolean("result"), true,"Candlepin status result");
 				Assert.assertTrue(jsonStatus.getString("release").matches("\\d+"), "Candlepin release matches d+");	// https://bugzilla.redhat.com/show_bug.cgi?id=703962
 				Assert.assertTrue(jsonStatus.getString("version").matches("\\d+\\.\\d+\\.\\d+"), "Candlepin version is matches d+.d+.d+");
 			}
 		}
 
 		
 		log.info("Installed version of subscription-manager...");
 		log.info("Client1 '"+sm_client1Hostname+"' is running version: "+client1.runCommandAndWait("rpm -qa | egrep ^subscription-manager").getStdout()); // subscription-manager-0.63-1.el6.i686
 		if (client2!=null) log.info("Client2 '"+sm_client2Hostname+"' is running version: "+client2.runCommandAndWait("rpm -qa | grep subscription-manager").getStdout()); // subscription-manager-0.63-1.el6.i686
 
 		log.info("Installed version of python-rhsm...");
 		log.info("Client1 '"+sm_client1Hostname+"' is running version: "+client1.runCommandAndWait("rpm -q python-rhsm").getStdout()); // python-rhsm-0.63-1.el6.i686
 		if (client2!=null) log.info("Client2 '"+sm_client2Hostname+"' is running version: "+client2.runCommandAndWait("rpm -q python-rhsm").getStdout()); // python-rhsm-0.63-1.el6.i686
 
 		log.info("Installed version of RHEL...");
 		log.info("Client1 '"+sm_client1Hostname+"' is running version: "+client1.runCommandAndWait("cat /etc/redhat-release").getStdout()); // Red Hat Enterprise Linux Server release 6.1 Beta (Santiago)
 		if (client2!=null) log.info("Client2 '"+sm_client2Hostname+"' is running version: "+client2.runCommandAndWait("cat /etc/redhat-release").getStdout()); // Red Hat Enterprise Linux Server release 6.1 Beta (Santiago)
 
 		log.info("Installed version of kernel...");
 		log.info("Client1 '"+sm_client1Hostname+"' is running version: "+client1.runCommandAndWait("uname -a").getStdout()); // Linux jsefler-onprem-server.usersys.redhat.com 2.6.32-122.el6.x86_64 #1 SMP Wed Mar 9 23:54:34 EST 2011 x86_64 x86_64 x86_64 GNU/Linux
 		if (client2!=null) log.info("Client2 '"+sm_client2Hostname+"' is running version: "+client2.runCommandAndWait("uname -a").getStdout()); // Linux jsefler-onprem-server.usersys.redhat.com 2.6.32-122.el6.x86_64 #1 SMP Wed Mar 9 23:54:34 EST 2011 x86_64 x86_64 x86_64 GNU/Linux
 
 		isSetupBeforeSuiteComplete = true;
 	}
 	protected static boolean isSetupBeforeSuiteComplete = false;
 	
 //	@BeforeSuite(groups={"gui-setup"},dependsOnMethods={"setupBeforeSuite"}, description="subscription manager gui set up")
 //	public void setupGUIBeforeSuite() throws IOException {
 //		// 201104251443:55.877 - FINE: ssh root@jsefler-onprem-workstation.usersys.redhat.com service vncserver restart (com.redhat.qe.tools.SSHCommandRunner.run)
 //		// 201104251444:02.676 - FINE: Stdout: 
 //		// Shutting down VNC server: 2:root [  OK  ]
 //		// Starting VNC server: 2:root [  OK  ]
 //		//if (client1!=null) RemoteFileTasks.runCommandAndAssert(client1, "service vncserver restart", /*Integer.valueOf(0) DON"T CHECK EXIT CODE SINCE IT RETURNS 1 WHEN STOP FAILS EVEN THOUGH START SUCCEEDS*/null, "Starting VNC server: 2:root \\[  OK  \\]", null);
 //		//if (client2!=null) RemoteFileTasks.runCommandAndAssert(client2, "service vncserver restart", /*Integer.valueOf(0) DON"T CHECK EXIT CODE SINCE IT RETURNS 1 WHEN STOP FAILS EVEN THOUGH START SUCCEEDS*/null, "Starting VNC server: 2:root \\[  OK  \\]", null);
 //		if (client1!=null) client1.runCommandAndWait("service vncserver restart");
 //		if (client2!=null) client2.runCommandAndWait("service vncserver restart");
 //
 //		// vncviewer <client1tasks.hostname>:2
 //	}
 	
 
 	
 	protected String selinuxSuiteMarker = "SM TestSuite marker";	// do not use a timestamp on the whole suite marker
 	protected String selinuxClassMarker = "SM TestClass marker "+String.valueOf(System.currentTimeMillis());	// using a timestamp on the class marker will help identify the test class during which a denial is logged
 	@BeforeSuite(groups={"setup"},dependsOnMethods={"setupBeforeSuite"}, description="Ensure SELinux is Enforcing before running the test suite.")
 	public void ensureSELinuxIsEnforcingBeforeSuite() {
 		if (client1!=null) Assert.assertEquals(client1.runCommandAndWait("getenforce").getStdout().trim(), "Enforcing", "SELinux mode is set to enforcing on client "+client1.getConnection().getHostname());
 		if (client2!=null) Assert.assertEquals(client2.runCommandAndWait("getenforce").getStdout().trim(), "Enforcing", "SELinux mode is set to enforcing on client "+client2.getConnection().getHostname());
 		if (client1!=null) RemoteFileTasks.markFile(client1, client1tasks.varLogAuditFile, selinuxSuiteMarker);
 		if (client2!=null) RemoteFileTasks.markFile(client2, client2tasks.varLogAuditFile, selinuxSuiteMarker);
 	}
 	@BeforeClass(groups={"setup"}, description="Mark the SELinux audit log before running the current class of tests so it can be searched for denials after the test class has run.")
 	public void MarkSELinuxAuditLogBeforeClass() {
 		if (client1!=null) RemoteFileTasks.markFile(client1, client1tasks.varLogAuditFile, selinuxClassMarker);
 		if (client2!=null) RemoteFileTasks.markFile(client2, client2tasks.varLogAuditFile, selinuxClassMarker);
 	}
 	@AfterClass(groups={"setup"}, description="Search the SELinux audit log for denials after running the current class of tests")
 	public void verifyNoSELinuxDenialsWereLoggedAfterClass() {
 		if (client1!=null) Assert.assertTrue(RemoteFileTasks.getTailFromMarkedFile(client1, client1tasks.varLogAuditFile, selinuxClassMarker, "denied").trim().equals(""), "No SELinux denials found in the audit log on client "+client1.getConnection().getHostname()+" while executing this test class.");
 		if (client2!=null) Assert.assertTrue(RemoteFileTasks.getTailFromMarkedFile(client2, client2tasks.varLogAuditFile, selinuxClassMarker, "denied").trim().equals(""), "No SELinux denials found in the audit log on client "+client2.getConnection().getHostname()+" while executing this test class.");
 	}
 	
 	@AfterSuite(groups={"cleanup"},description="subscription manager tear down")
 	public void unregisterClientsAfterSuite() {
 		
 		if (client2tasks!=null) client2tasks.unregister_(null, null, null);	// release the entitlements consumed by the current registration
 		if (client1tasks!=null) client1tasks.unregister_(null, null, null);	// release the entitlements consumed by the current registration
 		if (client2tasks!=null) client2tasks.clean_(null, null, null);	// in case the unregister fails, also clean the client
 		if (client1tasks!=null) client1tasks.clean_(null, null, null);	// in case the unregister fails, also clean the client
 	}
 	
 	@AfterSuite(groups={"cleanup"},description="subscription manager tear down")
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
 	
 	@AfterSuite(groups={"return2beaker"/*"cleanup"*/},description="return clients to beaker",dependsOnMethods={"disconnectDatabaseAfterSuite","unregisterClientsAfterSuite"}/*, alwaysRun=true*/)
 	public void return2beaker() {
 
 		Boolean return2beaker = Boolean.valueOf(getProperty("sm.client.return2beaker","false"));
 
 		if (return2beaker) {
 			if (client1!=null) client1.runCommandAndWait("return2beaker.sh");	// return this client back to beaker
 			if (client2!=null) client2.runCommandAndWait("return2beaker.sh");	// return this client back to beaker
 		}
 	}
 
 
 	
 	// Protected Methods ***********************************************************************
 
 	protected String getServerUrl(String hostname, String port, String prefix) {
 		// https://hostname:port/prefix
 		if (!port.equals("")) port=(":"+port).replaceFirst("^:+", ":");
 		if (!prefix.equals("")) prefix=("/"+prefix).replaceFirst("^/+", "/");
 		return "https://"+hostname+port+prefix;	
 	}
 	
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
 			Class.forName(sm_dbSqlDriver);	//	"org.postgresql.Driver" or "oracle.jdbc.driver.OracleDriver"
 			
 			// Create a connection to the database
 			String url = sm_dbSqlDriver.contains("postgres")? 
 					"jdbc:postgresql://" + sm_dbHostname + ":" + sm_dbPort + "/" + sm_dbName :
 					"jdbc:oracle:thin:@" + sm_dbHostname + ":" + sm_dbPort + ":" + sm_dbName ;
 			log.info(String.format("Attempting to connect to database with url and credentials: url=%s username=%s password=%s",url,sm_dbUsername,sm_dbPassword));
 			dbConnection = DriverManager.getConnection(url, sm_dbUsername, sm_dbPassword);
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
 	
 	// this list will be populated by subclass ResisterTests.RegisterWithCredentials_Test
 	protected static List<RegistrationData> registrationDataList = new ArrayList<RegistrationData>();	
 
 //	/**
 //	 * Useful when trying to find a username that belongs to a different owner/org than the current username you are testing with.
 //	 * @param key
 //	 * @return null when no match is found
 //	 * @throws JSONException
 //	 */
 //	protected RegistrationData findRegistrationDataNotMatchingOwnerKey(String key) throws JSONException {
 //		Assert.assertTrue (!registrationDataList.isEmpty(), "The RegisterWithCredentials_Test has been executed thereby populating the registrationDataList with content for testing."); 
 //		for (RegistrationData registration : registrationDataList) {
 //			if (registration.ownerKey!=null) {
 //				if (!registration.ownerKey.equals(key)) {
 //					return registration;
 //				}
 //			}
 //		}
 //		return null;
 //	}
 	
 	/**
 	 * Useful when trying to find registerable credentials that belongs to a different (or same) owner than the current credentials you are testing with.
 	 * @param matchingUsername
 	 * @param username
 	 * @param matchingOwnerKey
 	 * @param ownerkey
 	 * @return
 	 * @throws JSONException
 	 */
 	protected List<RegistrationData> findGoodRegistrationData(Boolean matchingUsername, String username, Boolean matchingOwnerKey, String ownerKey) throws JSONException {
 		List<RegistrationData> finalRegistrationData = new ArrayList<RegistrationData>();
 		List<RegistrationData> goodRegistrationData = new ArrayList<RegistrationData>();
 		List<String> ownersWithMatchingUsername = new ArrayList<String>();
 		List<String> usernamesWithMatchingOwnerKey = new ArrayList<String>();
 		Assert.assertTrue (!registrationDataList.isEmpty(), "The RegisterWithCredentials_Test has been executed thereby populating the registrationDataList with content for testing."); 
 		for (RegistrationData registrationDatum : registrationDataList) {
 			if (registrationDatum.registerResult.getExitCode().intValue()==0) {
 				if (registrationDatum.ownerKey.equals(ownerKey)) usernamesWithMatchingOwnerKey.add(registrationDatum.username);
 				if (registrationDatum.username.equals(username)) ownersWithMatchingUsername.add(registrationDatum.ownerKey);
 				if ( matchingUsername &&  registrationDatum.username.equals(username) &&  matchingOwnerKey &&  registrationDatum.ownerKey.equals(ownerKey)) {
 					goodRegistrationData.add(registrationDatum);
 				}
 				if ( matchingUsername &&  registrationDatum.username.equals(username) && !matchingOwnerKey && !registrationDatum.ownerKey.equals(ownerKey)) {
 					goodRegistrationData.add(registrationDatum);
 				}
 				if (!matchingUsername && !registrationDatum.username.equals(username) &&  matchingOwnerKey &&  registrationDatum.ownerKey.equals(ownerKey)) {
 					goodRegistrationData.add(registrationDatum);
 				}
 				if (!matchingUsername && !registrationDatum.username.equals(username) && !matchingOwnerKey && !registrationDatum.ownerKey.equals(ownerKey)) {
 					goodRegistrationData.add(registrationDatum);
 				}
 			}
 		}
 		for (RegistrationData registrationDatum : goodRegistrationData) {
 				if (ownerKey==null && !matchingOwnerKey &&  !matchingUsername) {
 					if (!registrationDatum.username.equals(username)) {
 						finalRegistrationData.add(registrationDatum);
 					}
 				} else if (ownerKey==null && !matchingOwnerKey && matchingUsername) {					
 					if (registrationDatum.username.equals(username)) {
 						finalRegistrationData.add(registrationDatum);
 					}
 				}
 		}
 		for (RegistrationData registrationDatum : goodRegistrationData) {
 			if ( !matchingOwnerKey &&  !matchingUsername) {
 				if (!ownersWithMatchingUsername.contains(registrationDatum.ownerKey )&& !usernamesWithMatchingOwnerKey.contains(registrationDatum.username)) {
 					finalRegistrationData.add(registrationDatum);
 				}
 			} else if ( !matchingOwnerKey && matchingUsername) {					
 				if (!ownersWithMatchingUsername.contains(registrationDatum.ownerKey) && username.equals(registrationDatum.username)) {
 					finalRegistrationData.add(registrationDatum);
 				}
 			} else if ( matchingOwnerKey && !matchingUsername ) {
 				if (ownersWithMatchingUsername.contains(registrationDatum.ownerKey) && !username.equals(registrationDatum.username)) {
 					finalRegistrationData.add(registrationDatum);
 				}
 			} else {
 				finalRegistrationData.add(registrationDatum);
 			}
 		}
 		return finalRegistrationData;
 	}
 	
 //	/**
 //	 * Useful when trying to find a username that belongs to the same owner/org as the current username you are testing with.
 //	 * @param key
 //	 * @param username
 //	 * @return null when no match is found
 //	 * @throws JSONException
 //	 */
 //	protected RegistrationData findRegistrationDataMatchingOwnerKeyButNotMatchingUsername(String key, String username) throws JSONException {
 //		Assert.assertTrue (!registrationDataList.isEmpty(), "The RegisterWithCredentials_Test has been executed thereby populating the registrationDataList with content for testing."); 
 //		for (RegistrationData registration : registrationDataList) {
 //			if (registration.ownerKey!=null) {
 //				if (registration.ownerKey.equals(key)) {
 //					if (!registration.username.equals(username)) {
 //						return registration;
 //					}
 //				}
 //			}
 //		}
 //		return null;
 //	}
 //	
 //	/**
 //	 * Useful when trying to find registration data results from a prior registration by a given username.
 //	 * @param key
 //	 * @param username
 //	 * @return null when no match is found
 //	 * @throws JSONException
 //	 */
 //	protected RegistrationData findRegistrationDataMatchingUsername(String username) throws JSONException {
 //		Assert.assertTrue (!registrationDataList.isEmpty(), "The RegisterWithCredentials_Test has been executed thereby populating the registrationDataList with content for testing."); 
 //		for (RegistrationData registration : registrationDataList) {
 //			if (registration.username.equals(username)) {
 //				return registration;
 //			}
 //		}
 //		return null;
 //	}
 	
 	/**
 	 * This can be called by Tests that depend on it in a BeforeClass method to insure that registrationDataList has been populated.
 	 * @throws Exception 
 	 */
 	protected void RegisterWithCredentials_Test() throws Exception {
 		if (registrationDataList.isEmpty()) {
 			clienttasks.unregister(null,null,null); // make sure client is unregistered
 			for (List<Object> credentials : getRegisterCredentialsDataAsListOfLists()) {
 				com.redhat.qe.sm.cli.tests.RegisterTests registerTests = new com.redhat.qe.sm.cli.tests.RegisterTests();
 				registerTests.setupBeforeSuite();
 				try {
 					registerTests.RegisterWithCredentials_Test((String)credentials.get(0), (String)credentials.get(1), (String)credentials.get(2));			
 				} catch (AssertionError e) {
 					log.warning("Ignoring a failure in RegisterWithCredentials_Test("+(String)credentials.get(0)+", "+(String)credentials.get(1)+", "+(String)credentials.get(2)+")");
 				}
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
 	
 
 	final String iso8601DateString = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"; 								//"2012-02-08T00:00:00.000+0000"
 	final DateFormat iso8601DateFormat = new SimpleDateFormat(iso8601DateString);				//"2012-02-08T00:00:00.000+0000"
 	protected Calendar parse_iso8601DateString(String dateString) {
 		try{
 			DateFormat dateFormat = new SimpleDateFormat(iso8601DateString);
 			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 			Calendar calendar = new GregorianCalendar();
 			calendar.setTimeInMillis(dateFormat.parse(dateString).getTime());
 			return calendar;
 		}
 		catch (ParseException e){
 			log.warning("Failed to parse GMT date string '"+dateString+"' with format '"+iso8601DateString+"':\n"+e.getMessage());
 			return null;
 		}
 	}
 	protected String format_iso8601DateString(Calendar date) throws UnsupportedEncodingException {
 		String iso8601FormatedDateString = iso8601DateFormat.format(date.getTime());
 		iso8601FormatedDateString = iso8601FormatedDateString.replaceFirst("(..$)", ":$1");		// "2012-02-08T00:00:00.000+00:00"	// see https://bugzilla.redhat.com/show_bug.cgi?id=720493 // http://books.xmlschemata.org/relaxng/ch19-77049.html requires a colon in the time zone for xsd:dateTime
 		return iso8601FormatedDateString;
 	}
 	protected String urlEncode(String formattedDate) throws UnsupportedEncodingException {
 		String urlEncodedDate = java.net.URLEncoder.encode(formattedDate, "UTF-8");	// "2012-02-08T00%3A00%3A00.000%2B00%3A00"	encode the string to escape the colons and plus signs so it can be passed as a parameter on an http call
 		return urlEncodedDate;
 	}
 
 
 	
 
 	
 	
 	
 	
 	// Data Providers ***********************************************************************
 
 	@DataProvider(name="getGoodRegistrationData")
 	public Object[][] getGoodRegistrationDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getGoodRegistrationDataAsListOfLists());
 	}
 	protected List<List<Object>> getGoodRegistrationDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		// parse the registrationDataList to get all the successfully registeredConsumers
 		for (RegistrationData registeredConsumer : registrationDataList) {
 			if (registeredConsumer.registerResult.getExitCode().intValue()==0) {
 				ll.add(Arrays.asList(new Object[]{registeredConsumer.username, registeredConsumer.password, registeredConsumer.ownerKey}));
 				
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
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		// assure we are registered
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, null, null, null);
 		if (client2tasks!=null)	{
 			client2tasks.unregister(null, null, null);
 			if (!sm_client2Username.equals("") && !sm_client2Password.equals(""))
 				client2tasks.register(sm_client2Username, sm_client2Password, sm_client2Org, null, null, null, null, null, (String)null, null, null, null, null);
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
 
 	
 // DELETEME
 //	@DataProvider(name="getUsernameAndPasswordData")
 //	public Object[][] getUsernameAndPasswordDataAs2dArray() {
 //		return TestNGUtils.convertListOfListsTo2dArray(getUsernameAndPasswordDataAsListOfLists());
 //	}
 //	protected List<List<Object>> getUsernameAndPasswordDataAsListOfLists() {
 //		List<List<Object>> ll = new ArrayList<List<Object>>();
 //		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users | python -mjson.tool
 //		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1 | python -mjson.tool
 //		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1/owners | python -mjson.tool
 //
 //		String[] usernames = sm_clientUsernames.split(",");
 //		String[] passwords = sm_clientPasswords.split(",");
 //		String password = passwords[0].trim();
 //		for (int i = 0; i < usernames.length; i++) {
 //			String username = usernames[i].trim();
 //			// when there is not a 1:1 relationship between usernames and passwords, the last password is repeated
 //			// this allows one to specify only one password when all the usernames share the same password
 //			if (i<passwords.length) password = passwords[i].trim();
 //			
 //			// get the orgs for this username/password
 //			List<String> orgs = clienttasks.getOrgs(username,password);
 //			if (orgs.size()==1) {orgs.clear(); orgs.add(null);}	// 
 //			
 //			// append a username and password for each org the user belongs to
 //			for (String org : orgs) {
 //				ll.add(Arrays.asList(new Object[]{username,password,org}));
 //			}
 //		}
 //		
 //		return ll;
 //	}
 
 	@DataProvider(name="getRegisterCredentialsData")
 	public Object[][] getRegisterCredentialsDataAs2dArray() throws Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterCredentialsDataAsListOfLists());
 	}
 	@DataProvider(name="getRegisterCredentialsExcludingNullOrgData")
 	public Object[][] getRegisterCredentialsExcludingNullOrgDataAs2dArray() throws Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		for (List<Object> l : getRegisterCredentialsDataAsListOfLists()) {
 			// l contains: String username, String password, String owner
 			if (l.get(2) !=null) ll.add(l);
 		}
 		return TestNGUtils.convertListOfListsTo2dArray(ll);
 	}
 	protected List<List<Object>> getRegisterCredentialsDataAsListOfLists() throws Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		
 		// when the candlepin server is not onPremises, then we usually don't have access to the candlepin api paths to quesry the users, so let's use the input parameters 
 		if (!sm_isServerOnPremises) {
 			for (String username : sm_clientUsernames) {
 				String password = sm_clientPasswordDefault;
 			
 				// get the orgs for this username/password
 				//List<Org> orgs = clienttasks.getOrgs(username,password);	// fails when: You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc
 				List<Org> orgs = Org.parse(clienttasks.orgs_(username, password, null, null, null).getStdout());
 				//if (orgs.size()==1) {orgs.clear(); orgs.add(new Org(null,null));}	// when a user belongs to only one org, then we don't really need to know the orgKey for registration
 				if (orgs.isEmpty()) orgs.add(new Org("null","Null"));	// reveals when: You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc
 			
 				// append a username and password for each org the user belongs to
 				for (Org org : orgs) {
 					ll.add(Arrays.asList(new Object[]{username,password,org.orgKey}));
 				}
 			}
 			return ll;
 		}
 		
 				
 		// Notes...
 		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users | python -mjson.tool
 		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1 | python -mjson.tool
 		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1/owners | python -mjson.tool
 
 		// get all of the candlepin users
 		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users | python -mjson.tool
 		JSONArray jsonUsers = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,"/users"));	
 		for (int i = 0; i < jsonUsers.length(); i++) {
 			JSONObject jsonUser = (JSONObject) jsonUsers.get(i);
 
 			// Candlepin Users
 			//    {
 			//        "created": "2011-09-23T14:42:25.924+0000", 
 			//        "hashedPassword": "e3e80f61a902ceca245e22005dffb4219ac1c5f7", 
 			//        "id": "8a90f8c63296bc55013296bcc4040005", 
 			//        "superAdmin": true, 
 			//        "updated": "2011-09-23T14:42:25.924+0000", 
 			//        "username": "admin"
 			//    }, 
 			
 			// Katello Users...
 			//    {
 			//        "created_at": "2011-09-24T01:29:02Z", 
 			//        "disabled": false, 
 			//        "helptips_enabled": true, 
 			//        "id": 1, 
 			//        "own_role_id": 4, 
 			//        "page_size": 25, 
 			//        "password": "07a1dacc4f283e817c0ba353bd1452de49ce5723b2b7f56f6ee2f1f400a974b360f98acb90b630c7fa411f692bdb4c5cdd0f4b916efcf3c77e7cd0453446b185TS0YtS0uRjY0UznEsx7JqGIpEM1vEfIrBSNGdnXkFdkxsDhjmyFINBJVvkCTxeC7", 
 			//        "updated_at": "2011-09-24T01:29:02Z", 
 			//        "username": "admin"
 			//    }, 
 			
 			//Boolean isSuperAdmin = jsonUser.getBoolean("superAdmin");
 			String username = jsonUser.getString("username");
 			String password = sm_clientPasswordDefault;
 			if (username.equals(sm_serverAdminUsername)) password = sm_serverAdminPassword;
 			
 			
 			// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=741961 - jsefler 9/29/2011
 			if (username.equals("anonymous")) {
 				boolean invokeWorkaroundWhileBugIsOpen = true;
 				String bugId="741961"; 
 				try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 				if (invokeWorkaroundWhileBugIsOpen) {
 					log.warning("Ignoring the presence of user '"+username+"'.  No automated testing with this user will be executed.");
 					continue;
 				}
 			}
 			// END OF WORKAROUND
 			
 			// get the user's owners
 			// curl -k -u testuser1:password https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1/owners | python -mjson.tool
 			JSONArray jsonUserOwners = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(username,password,sm_serverUrl,"/users/"+username+"/owners"));	
 			for (int j = 0; j < jsonUserOwners.length(); j++) {
 				JSONObject jsonOwner = (JSONObject) jsonUserOwners.get(j);
 				// {
 				//    "contentPrefix": null, 
 				//    "created": "2011-07-01T06:39:58.740+0000", 
 				//    "displayName": "Snow White", 
 				//    "href": "/owners/snowwhite", 
 				//    "id": "8a90f8c630e46c7e0130e46ce114000a", 
 				//    "key": "snowwhite", 
 				//    "parentOwner": null, 
 				//    "updated": "2011-07-01T06:39:58.740+0000", 
 				//    "upstreamUuid": null
 				// }
 				String owner = jsonOwner.getString("key");
 				
 				// String username, String password, String owner
 				ll.add(Arrays.asList(new Object[]{username,password,owner}));
 			}
 			
 			// don't forget that some users (for which no owners are returned) probably have READ_ONLY permission to their orgs
 			if (jsonUserOwners.length()==0) {
 				ll.add(Arrays.asList(new Object[]{username,password,null}));			
 			}
 		}
 		
 		return ll;
 	}
 	
 	@DataProvider(name="getAllConsumedProductSubscriptionsData")
 	public Object[][] getAllConsumedProductSubscriptionsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getAllConsumedProductSubscriptionsDataAsListOfLists());
 	}
 	protected List<List<Object>> getAllConsumedProductSubscriptionsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		// first make sure we are subscribed to all pools
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null, null, (String)null, null, null, null, null);
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools();
 		
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
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		// first make sure we are subscribed to all pools
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null, null, (String)null, null, null, null, null);
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools();
 
 		
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
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		List <String> productIdsAddedToSystemSubscriptionPoolProductData = new ArrayList<String>();
 
 		// get the owner key for clientusername, clientpassword
 		String consumerId = clienttasks.getCurrentConsumerId();
 		if (consumerId==null) consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, Boolean.TRUE, null, null, null));
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_clientUsername, sm_clientPassword, sm_serverUrl, consumerId);
 
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(System.currentTimeMillis());
 		
 		// process all of the subscriptions belonging to ownerKey
 		JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/owners/"+ownerKey+"/subscriptions"));	
 		for (int i = 0; i < jsonSubscriptions.length(); i++) {
 			JSONObject jsonSubscription = (JSONObject) jsonSubscriptions.get(i);
 			
 			// skip future subscriptions that are not valid today (at this time now)
 			Calendar startDate = parse_iso8601DateString(jsonSubscription.getString("startDate"));	// "startDate":"2012-02-08T00:00:00.000+0000"
 			Calendar endDate = parse_iso8601DateString(jsonSubscription.getString("endDate"));	// "endDate":"2013-02-07T00:00:00.000+0000"
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
 			List<String> productSupportedArches = new ArrayList<String>();
 			String productAttributeStackingIdValue = null;
 			for (int j = 0; j < jsonProductAttributes.length(); j++) {	// loop product attributes to find a stacking_id
 				if (((JSONObject) jsonProductAttributes.get(j)).getString("name").equals("stacking_id")) {
 					productAttributeStackingIdValue = ((JSONObject) jsonProductAttributes.get(j)).getString("value");
 					break;
 				}
 			}
 			for (int j = 0; j < jsonProductAttributes.length(); j++) {
 				JSONObject jsonProductAttribute = (JSONObject) jsonProductAttributes.get(j);
 				String attributeName = jsonProductAttribute.getString("name");
 				String attributeValue = jsonProductAttribute.getString("value");
 				if (attributeName.equals("arch")) {
 					productSupportedArches.addAll(Arrays.asList(attributeValue.trim().toUpperCase().split(" *, *")));	// Note: the arch attribute can be a comma separated list of values
 					if (productSupportedArches.contains("X86")) {productSupportedArches.addAll(Arrays.asList("I386","I486","I586","I686"));}  // Note" x86 is a general arch to cover all 32-bit intel micrprocessors 
 					if (!productSupportedArches.contains("ALL") && !productSupportedArches.contains(clienttasks.arch.toUpperCase())) {
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
 				if (attributeName.equals("multi-entitlement")) {
 
 				}
 				if (attributeName.equals("warning_period")) {
 
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
 				if (attributeName.equals("stacking_id")) {
 					// productAttributeStackingIdValue = attributeValue; // was already searched for above
 				}
 				if (attributeName.equals("sockets")) {
 					productAttributeSocketsValue = attributeValue;
 					
 					// if this subscription is stackable (indicated by the presence of a stacking_id attribute)
 					// then there is no need to check the system's sockets to see if this subscription should be available 
 					// Because this subscription is stackable, it better not be filtered out from availability based on the system's sockets.
 					if (productAttributeStackingIdValue==null) {
 					
 						// if the socket count on this client exceeds the sockets attribute, then this subscription should NOT be available to this client
 						if (Integer.valueOf(attributeValue) < Integer.valueOf(clienttasks.sockets)) {
 							if (matchSystem) productAttributesPassRulesCheck = false;
 						}
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
 						/* 6/17/2011 The availability of a Subscription depends only on its attributes and NOT the attributes of its provided products.
 						 * You will get ALL of its provided product even if they don't make arch/socket sense.
 						 * In this case you could argue that it is not subscription-manager's job to question the meaningfulness of the subscription and its provided products.
 						 * For this reason, I am commenting out all the providedProductAttributesPassRulesCheck = false; ... (except "type")
 						 */
 						if (attributeName.equals("arch")) {
 							List<String> supportedArches = new ArrayList<String>(Arrays.asList(attributeValue.trim().toUpperCase().split(" *, *")));	// Note: the arch attribute can be a comma separated list of values
 							if (supportedArches.contains("X86")) {supportedArches.addAll(Arrays.asList("I386","I486","I586","I686"));}  // Note" x86 is a general term to cover all 32-bit intel micrprocessors 
 							if (!productSupportedArches.containsAll(supportedArches)) {
 								log.warning("THE VALIDITY OF SUBSCRIPTION productName='"+productName+"' productId='"+productId+"' WITH PROVIDED PRODUCT '"+providedProductName+"' IS QUESTIONABLE.  THE PROVIDED PRODUCT '"+providedProductId+"' ARCH ATTRIBUTE '"+attributeValue+"' IS NOT A SUBSET OF THE TOP LEVEL PRODUCT '"+productId+"' ARCH ATTRIBUTE '"+productSupportedArches+"'.");
 							}
 							if (!supportedArches.contains("ALL") && !supportedArches.contains(clienttasks.arch.toUpperCase())) {
 								//providedProductAttributesPassRulesCheck = false;
 							}
 						}
 						if (attributeName.equals("variant")) {
 //								if (!attributeValue.equalsIgnoreCase("ALL") && !attributeValue.equalsIgnoreCase(clienttasks.variant)) {
 //									providedProductAttributesPassRulesCheck = false;
 //								}
 						}
 						if (attributeName.equals("type")) {
 							if (attributeValue.equals("MKT")) { // provided products of type "MKT" should not pass the rules check  e.g. providedProductName="Awesome OS Server Bundled"
 								providedProductAttributesPassRulesCheck = false;	// do not comment out!
 							}
 						}
 						if (attributeName.equals("version")) {
 //								if (!attributeValue.equalsIgnoreCase(clienttasks.version)) {
 //									providedProductAttributesPassRulesCheck = false;
 //								}
 						}
 						if (attributeName.equals("requires_consumer_type")) {
 							if (!attributeValue.equalsIgnoreCase(ConsumerType.system.toString())) {
 								//providedProductAttributesPassRulesCheck = false;
 							}
 						}
 						if (attributeName.equals("sockets")) {
 							if (!attributeValue.equals(productAttributeSocketsValue)) {
 								log.warning("THE VALIDITY OF SUBSCRIPTION productName='"+productName+"' productId='"+productId+"' WITH PROVIDED PRODUCT '"+providedProductName+"' IS QUESTIONABLE.  THE PROVIDED PRODUCT '"+providedProductId+"' SOCKETS ATTRIBUTE '"+attributeValue+"' DOES NOT MATCH THE BASE SUBSCRIPTION PRODUCT '"+productId+"' SOCKETS ATTRIBUTE '"+productAttributeSocketsValue+"'.");
 							}
 							if (!productAttributeSocketsValue.equals("") && Integer.valueOf(attributeValue) > Integer.valueOf(productAttributeSocketsValue)) {
 								//providedProductAttributesPassRulesCheck = false;
 							}
 						}
 					}
 					if (providedProductAttributesPassRulesCheck) {
 						JSONObject bundledProduct = new JSONObject(String.format("{productName:'%s', productId:'%s'}", providedProductName,providedProductId));
 
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
 	
 	@DataProvider(name="getNonAvailableSystemSubscriptionPoolProductData")
 	public Object[][] getNonAvailableSystemSubscriptionPoolProductDataAs2dArray() throws Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getNonAvailableSystemSubscriptionPoolProductDataAsListOfLists());
 	}
 	protected List<List<Object>> getNonAvailableSystemSubscriptionPoolProductDataAsListOfLists() throws Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		List <String> productIdsAddedToNonAvailableSystemSubscriptionPoolProductData = new ArrayList<String>();
 
 		// String systemProductId, JSONArray bundledProductDataAsJSONArray
 		List<List<Object>> availSystemSubscriptionPoolProductData = getSystemSubscriptionPoolProductDataAsListOfLists(true);
 		
 		// get the owner key for clientusername, clientpassword
 		String consumerId = clienttasks.getCurrentConsumerId();
 		if (consumerId==null) consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, Boolean.TRUE, null, null, null));
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_clientUsername, sm_clientPassword, sm_serverUrl, consumerId);
 
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(System.currentTimeMillis());
 		
 		// process all of the subscriptions belonging to ownerKey
 		JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/owners/"+ownerKey+"/subscriptions"));	
 		for (int i = 0; i < jsonSubscriptions.length(); i++) {
 			JSONObject jsonSubscription = (JSONObject) jsonSubscriptions.get(i);
 			
 			JSONObject jsonProduct = (JSONObject) jsonSubscription.getJSONObject("product");
 			String productId = jsonProduct.getString("id");
 			String productName = jsonProduct.getString("name");
 			
 			// skip subscriptions that have already been added to NonAvailableSystemSubscriptionPoolProductData
 			if (productIdsAddedToNonAvailableSystemSubscriptionPoolProductData.contains(productId)) continue;
 
 			boolean isAvailable = false;
 			for (List<Object> systemSubscriptionPoolProductDatum : availSystemSubscriptionPoolProductData) {
 				String availProductId = (String) systemSubscriptionPoolProductDatum.get(0);
 				JSONArray availJsonBundledProductData = (JSONArray) systemSubscriptionPoolProductDatum.get(1);
 				if (availProductId.equals(productId)) {
 					isAvailable = true;
 					break;
 				}
 			}
 			if (!isAvailable) {
 				// String systemProductId
 				ll.add(Arrays.asList(new Object[]{productId}));
 				productIdsAddedToNonAvailableSystemSubscriptionPoolProductData.add(productId);
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
 	
 	
 	@DataProvider(name="getAllJSONPoolsData")
 	public Object[][] getAllJSONPoolsDataAs2dArray() throws Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getAllJSONPoolsDataAsListOfLists());
 	}
 	protected List<List<Object>> getAllJSONPoolsDataAsListOfLists() throws Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		
 		// who is the owner of sm_clientUsername
 		String clientOrg = sm_clientOrg;
 		if (clientOrg==null) {
 			List<RegistrationData> registrationData = findGoodRegistrationData(true,sm_clientUsername,false,clientOrg);
 			if (registrationData.isEmpty() || registrationData.size()>1) throw new SkipException("Could not determine unique owner for username '"+sm_clientUsername+"'.  It is needed for a candlepin API call get pools by owner.");
 			clientOrg = registrationData.get(0).ownerKey;
 		}
 		
 		// process all of the pools belonging to ownerKey
 		JSONArray jsonPools = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/owners/"+clientOrg+"/pools?listall=true"));	
 		for (int i = 0; i < jsonPools.length(); i++) {
 			JSONObject jsonPool = (JSONObject) jsonPools.get(i);
 			String id = jsonPool.getString("id");
 			
 			ll.add(Arrays.asList(new Object[]{jsonPool}));
 		}
 		
 		return ll;
 	}
 
 	
 	@DataProvider(name="getAllFutureJSONPoolsData")
 	public Object[][] getAllFutureJSONPoolsDataAs2dArray() throws Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getAllFutureJSONPoolsDataAsListOfLists(null));
 	}
 	@DataProvider(name="getAllFutureSystemJSONPoolsData")
 	public Object[][] getAllFutureSystemJSONPoolsDataAs2dArray() throws Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getAllFutureJSONPoolsDataAsListOfLists(ConsumerType.system));
 	}
 	@DataProvider(name="getAllFutureSystemSubscriptionPoolsData")
 	public Object[][] getAllFutureSystemSubscriptionPoolsDataAs2dArray() throws Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getAllFutureSystemSubscriptionPoolsDataAsListOfLists());
 	}
 	
 	protected List<List<Object>>getAllFutureSystemSubscriptionPoolsDataAsListOfLists() throws ParseException, JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getAllFutureJSONPoolsDataAsListOfLists(ConsumerType.system)) {
 			JSONObject jsonPool = (JSONObject) l.get(0);
 			
 			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
 			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 			Calendar endDate = new GregorianCalendar();
 			endDate.setTimeInMillis(dateFormat.parse(jsonPool.getString("endDate")).getTime());
 
 			ll.add(Arrays.asList(new Object[]{new SubscriptionPool(jsonPool.getString("productName"), jsonPool.getString("productId"), jsonPool.getString("id"), jsonPool.getString("quantity"), SubscriptionPool.formatDateString(endDate))}));
 		}
 		return ll;
 	}
 	
 	protected List<List<Object>> getAllFutureJSONPoolsDataAsListOfLists(ConsumerType consumerType) throws Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		
 		// get the owner key for clientusername, clientpassword
 		String consumerId = clienttasks.getCurrentConsumerId();
 		if (consumerId==null) consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, Boolean.TRUE, null, null, null));
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_clientUsername, sm_clientPassword, sm_serverUrl, consumerId);
 
 		for (List<Object> l : getAllFutureJSONSubscriptionsDataAsListOfLists(consumerType)) {
 			JSONObject jsonSubscription = (JSONObject) l.get(0);
 			String subscriptionId = jsonSubscription.getString("id");			
 			Calendar startDate = parse_iso8601DateString(jsonSubscription.getString("startDate"));	// "startDate":"2012-02-08T00:00:00.000+0000"
 
 			// process all of the pools belonging to ownerKey that are activeon startDate
 			JSONArray jsonPools = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/owners/"+ownerKey+"/pools" +"?activeon="+urlEncode(format_iso8601DateString(startDate))));
 			for (int j = 0; j < jsonPools.length(); j++) {
 				JSONObject jsonPool = (JSONObject) jsonPools.get(j);
 				
 				// remember all the jsonPools that come from subscriptionId
 				if (jsonPool.getString("subscriptionId").equals(subscriptionId)) {
 
 					// JSONObject jsonPool
 					ll.add(Arrays.asList(new Object[]{jsonPool}));
 					
 					// minimize the number of dataProvided rows (useful during automated testcase development)
 					if (Boolean.valueOf(getProperty("sm.debug.dataProviders.minimize","false"))) break;
 				}
 			}
 		}
 		return ll;
 	}
 	
 	protected List<List<Object>> getAllFutureJSONSubscriptionsDataAsListOfLists(ConsumerType consumerType) throws Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 
 		// get the owner key for clientusername, clientpassword
 		String consumerId = clienttasks.getCurrentConsumerId();
 		if (consumerId==null) consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, Boolean.TRUE, null, null, null));
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_clientUsername, sm_clientPassword, sm_serverUrl, consumerId);
 
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(System.currentTimeMillis());
 		
 		// process all of the subscriptions belonging to ownerKey
 		JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/owners/"+ownerKey+"/subscriptions"));	
 		for (int i = 0; i < jsonSubscriptions.length(); i++) {
 			JSONObject jsonSubscription = (JSONObject) jsonSubscriptions.get(i);
 			String id = jsonSubscription.getString("id");			
 			Calendar startDate = parse_iso8601DateString(jsonSubscription.getString("startDate"));	// "startDate":"2012-02-08T00:00:00.000+0000"
 			Calendar endDate = parse_iso8601DateString(jsonSubscription.getString("endDate"));	// "endDate":"2013-02-07T00:00:00.000+0000"
 
 			// skip subscriptions to a product that doesn't satisfy the requested consumerType
 			JSONObject jsonProduct = jsonSubscription.getJSONObject("product");
 			JSONArray jsonProductAttributes = jsonProduct.getJSONArray("attributes");
 			String requires_consumer_type = null;
 			for (int j = 0; j < jsonProductAttributes.length(); j++) {
 				JSONObject jsonProductAttribute = (JSONObject) jsonProductAttributes.get(j);
 				if (jsonProductAttribute.getString("name").equals("requires_consumer_type")) {
 					requires_consumer_type = jsonProductAttribute.getString("value");
 					break;
 				}
 			}
 			if (requires_consumer_type==null) requires_consumer_type = ConsumerType.system.toString();	// the absence of a "requires_consumer_type" implies requires_consumer_type is system
 			if (!ConsumerType.valueOf(requires_consumer_type).equals(consumerType)) continue;
 			
 			// process subscriptions that are in the future
 			if (startDate.after(now)) {
 			
 				// JSONObject jsonSubscription
 				ll.add(Arrays.asList(new Object[]{jsonSubscription}));
 			}
 		}
 		return ll;
 	}
 
 	
 	
 	
 	protected List<List<Object>> getModifierSubscriptionDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>();	if (!isSetupBeforeSuiteComplete) return ll;
 		
 		// get the owner key for clientusername, clientpassword
 		String consumerId = clienttasks.getCurrentConsumerId();
 		if (consumerId==null) consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, Boolean.TRUE, null, null, null));
 		//String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_serverHostname, sm_serverPort, sm_serverPrefix, sm_clientUsername, sm_clientPassword, consumerId);
 
 		
 		List<SubscriptionPool> allAvailablePools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		
 		// iterate through all available pools looking for those that contain products with content that modify other products
 		for (SubscriptionPool modifierPool : allAvailablePools) {
 			JSONObject jsonModifierPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/pools/"+modifierPool.poolId));	
 			
 			// iterate through each of the providedProducts
 			JSONArray jsonModifierProvidedProducts = jsonModifierPool.getJSONArray("providedProducts");
 			for (int i = 0; i < jsonModifierProvidedProducts.length(); i++) {
 				JSONObject jsonModifierProvidedProduct = (JSONObject) jsonModifierProvidedProducts.get(i);
 				String modifierProvidedProductId = jsonModifierProvidedProduct.getString("productId");
 				
 				// get the productContents
 				JSONObject jsonProduct = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/products/"+modifierProvidedProductId));	
 				JSONArray jsonProductContents = jsonProduct.getJSONArray("productContent");
 				for (int j = 0; j < jsonProductContents.length(); j++) {
 					JSONObject jsonProductContent = (JSONObject) jsonProductContents.get(j);
 					JSONObject jsonContent = jsonProductContent.getJSONObject("content");
 					
 					// get the label and modifiedProductIds for each of the productContents
 					String label = jsonContent.getString("label");
 					String requiredTags = jsonContent.getString("requiredTags"); // comma separated string
 					if (requiredTags.equals("null")) requiredTags = null;
 					JSONArray jsonModifiedProductIds = jsonContent.getJSONArray("modifiedProductIds");
 					List<String> modifiedProductIds = new ArrayList<String>();
 					for (int k = 0; k < jsonModifiedProductIds.length(); k++) {
 						String modifiedProductId = (String) jsonModifiedProductIds.get(k);
 						modifiedProductIds.add(modifiedProductId);
 					}
 					
 					// does this pool contain productContents that modify other products?
 					if (modifiedProductIds.size()>0) {
 						
 						List<SubscriptionPool> providingPools = new ArrayList<SubscriptionPool>();
 						// yes, now its time to find the subscriptions that provide the modifiedProductIds
 						for (SubscriptionPool providingPool : allAvailablePools) {
 							JSONObject jsonProvidingPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/pools/"+providingPool.poolId));	
 							
 							// iterate through each of the providedProducts
 							JSONArray jsonProvidingProvidedProducts = jsonProvidingPool.getJSONArray("providedProducts");
 							for (int l = 0; l < jsonProvidingProvidedProducts.length(); l++) {
 								JSONObject jsonProvidingProvidedProduct = (JSONObject) jsonProvidingProvidedProducts.get(l);
 								String providingProvidedProductId = jsonProvidingProvidedProduct.getString("productId");
 								if (modifiedProductIds.contains(providingProvidedProductId)) {
 									
 									// NOTE: This test takes a long time to run when there are many providingPools.
 									// To reduce the execution time, let's simply limit the number of providing pools tested to 2,
 									// otherwise this block of code could be commented out for a more thorough test.
 									boolean thisPoolProductIdIsAlreadyInProvidingPools = false;
 									for (SubscriptionPool providedPool : providingPools) {
 										if (providedPool.productId.equals(providingPool.productId)) {
 											thisPoolProductIdIsAlreadyInProvidingPools=true; break;
 										}
 									}
 									if (thisPoolProductIdIsAlreadyInProvidingPools||providingPools.size()>=2) break;
 									
 									providingPools.add(providingPool); break;
 								}
 							}
 						}
 										
 						ll.add(Arrays.asList(new Object[]{modifierPool, label, modifiedProductIds, requiredTags, providingPools}));
 					}
 				}
 			}
 		}	
 		return ll;
 	}
 }
