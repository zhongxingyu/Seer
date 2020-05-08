 package com.redhat.qe.sm.base;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.Random;
 
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeSuite;
 
 import com.redhat.qe.auto.testopia.Assert;
 import com.redhat.qe.sm.tasks.ModuleTasks;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 public class SubscriptionManagerTestScript extends com.redhat.qe.auto.testng.TestScript{
 //	protected static final String defaultAutomationPropertiesFile=System.getenv("HOME")+"/sm-tests.properties";
 //	public static final String RHSM_LOC = "/usr/sbin/subscription-manager-cli ";
 	
 	protected String serverHostname			= System.getProperty("rhsm.server.hostname");
 	protected String serverPort 			= System.getProperty("rhsm.server.port");
 	protected String serverBaseUrl			= System.getProperty("rhsm.server.baseurl");
 	protected Boolean isServerOnPremises	= Boolean.valueOf(System.getProperty("rhsm.server.onpremises","false"));
 
 	protected String client1hostname		= System.getProperty("rhsm.client1.hostname");
 	protected String client1username		= System.getProperty("rhsm.client1.username");
 	protected String client1password		= System.getProperty("rhsm.client1.password");
 	
 	protected String client2hostname		= System.getProperty("rhsm.client2.hostname");
 	protected String client2username		= System.getProperty("rhsm.client2.username");
 	protected String client2password		= System.getProperty("rhsm.client2.password");
 
 	protected String clienthostname			= client1hostname;
 	protected String clientusername			= client1username;
 	protected String clientpassword			= client1password;
 
 	protected String tcUnacceptedUsername	= System.getProperty("rhsm.client.username.tcunaccepted");
 	protected String tcUnacceptedPassword	= System.getProperty("rhsm.client.password.tcunaccepted");
 	protected String regtoken				= System.getProperty("rhsm.client.regtoken");
 	protected String certFrequency			= System.getProperty("rhsm.client.certfrequency");
 	protected String enablerepofordeps		= System.getProperty("rhsm.client.enablerepofordeps");
 
 	protected String prodCertLocation		= System.getProperty("rhsm.prodcert.url");
 	protected String prodCertProduct		= System.getProperty("rhsm.prodcert.product");
 	
 	protected String sshUser				= System.getProperty("rhsm.ssh.user","root");
 	protected String sshKeyPrivate			= System.getProperty("rhsm.sshkey.private",".ssh/id_auto_dsa");
 	protected String sshkeyPassphrase		= System.getProperty("rhsm.sshkey.passphrase","");
 
 	
 	protected String itDBSQLDriver			= System.getProperty("rhsm.it.db.sqldriver", "oracle.jdbc.driver.OracleDriver");
 	protected String itDBHostname			= System.getProperty("rhsm.it.db.hostname");
 	protected String itDBDatabase			= System.getProperty("rhsm.it.db.database");
 	protected String itDBPort				= System.getProperty("rhsm.it.db.port", "1521");
 	protected String itDBUsername			= System.getProperty("rhsm.it.db.username");
 	protected String itDBPassword			= System.getProperty("rhsm.it.db.password");
 	
 	protected String rpmLocation			= System.getProperty("rhsm.rpm.url");
 
 	protected String defaultConfigFile		= "/etc/rhsm/rhsm.conf";
 	protected String rhsmcertdLogFile		= "/var/log/rhsm/rhsmcertd.log";
 	protected String rhsmYumRepoFile		= "/etc/yum/pluginconf.d/rhsmplugin.conf";
 	
 	public static Connection itDBConnection = null;
 	
 	public static SSHCommandRunner client	= null;
 	public static SSHCommandRunner client1	= null;	// client1
 	public static SSHCommandRunner client2	= null;	// client2
 	
 	protected static ModuleTasks clienttasks	= null;
 	protected static ModuleTasks client1tasks	= null;	// client1 subscription manager tasks
 	protected static ModuleTasks client2tasks	= null;	// client2 subscription manager tasks
 	
 	
 	public SubscriptionManagerTestScript() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	@BeforeSuite(groups={"setup"},description="subscription manager set up")
 	public void setupBeforeSuite() throws ParseException, IOException{
 		SSHCommandRunner[] sshCommandRunners;
 		
 		client = new SSHCommandRunner(clienthostname, sshUser, sshKeyPrivate, sshkeyPassphrase, null);
 		clienttasks = new com.redhat.qe.sm.tasks.ModuleTasks(client);
 		sshCommandRunners= new SSHCommandRunner[]{client};
 		
 		// will we be testing multiple clients?
		if (!client2hostname.equals("") || client2hostname.startsWith("$")) {
 			client1 = client;
 			client1tasks = clienttasks;
 			client2 = new SSHCommandRunner(client2hostname, sshUser, sshKeyPrivate, sshkeyPassphrase, null);
 			client2tasks = new com.redhat.qe.sm.tasks.ModuleTasks(client2);
 			sshCommandRunners= new SSHCommandRunner[]{client1,client2};
 		}
 		
 		// setup each client
 		for (SSHCommandRunner commandRunner : sshCommandRunners) {
 			
 			this.installLatestRPM(commandRunner);
 			this.updateSMConfigFile(commandRunner, serverHostname, serverPort);
 			this.changeCertFrequency(commandRunner, certFrequency);
 			commandRunner.runCommandAndWait("killall -9 yum");
 			
 	//setup should not be running sm commands		sm.unregister();	// unregister after updating the config file
 			this.cleanOutAllCerts(commandRunner);	// is this really needed?  shouldn't unregister do this and assert it - jsefler 7/8/2010  - yes it is needed since we should not use sm to unregister here
 
 		}
 	}
 	
 	@AfterSuite(groups={"setup"},description="subscription manager tear down")
 	public void teardownAfterSuite() {
 		if (clienttasks!=null) clienttasks.unregister();	// release the entitlements consumed by the current registration
 		if (client2tasks!=null) client2tasks.unregister();	// release the entitlements consumed by the current registration
 	}
 	
 	private void cleanOutAllCerts(SSHCommandRunner sshCommandRunner){
 		log.info("Cleaning out certs from /etc/pki/consumer, /etc/pki/entitlement/, /etc/pki/entitlement/product, and /etc/pki/product/");
 		
 		sshCommandRunner.runCommandAndWait("rm -f /etc/pki/consumer/*");
 		sshCommandRunner.runCommandAndWait("rm -rf /etc/pki/entitlement/*");
 		sshCommandRunner.runCommandAndWait("rm -rf /etc/pki/entitlement/product/*");
 		sshCommandRunner.runCommandAndWait("rm -rf /etc/pki/product/*");
 	}
 	
 	public void connectToDatabase(){
 		itDBConnection = null; 
 		try { 
 			// Load the JDBC driver 
 			String driverName = this.itDBSQLDriver;
 			Class.forName(driverName); 
 			// Create a connection to the database
 			String serverName = this.itDBHostname;
 			String portNumber = this.itDBPort;
 			String sid = this.itDBDatabase;
 			String url = "jdbc:oracle:thin:@" + serverName + ":" + portNumber + ":" + sid;
 			String username = this.itDBUsername;
 			String password = this.itDBPassword;
 			itDBConnection = DriverManager.getConnection(url, username, password); 
 			} 
 		catch (ClassNotFoundException e) { 
 			log.warning("Oracle JDBC driver not found!");
 		} 
 		catch (SQLException e) {
 			log.warning("Could not connect to backend IT database!  Traceback:\n" + e.getMessage());
 		}
 	}
 	
 	public void getSalesToEngineeringProductBindings(){
 		try {
 			String products = itDBConnection.nativeSQL("select * from butt;");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			log.info("Database query for Sales-to-Engineering product bindings failed!  Traceback:\n"+e.getMessage());
 		}
 	}
 	
 	private void installLatestRPM(SSHCommandRunner sshCommandRunner) {
 
 		// verify the subscription-manager client is a rhel 6 machine
 		log.info("Verifying prerequisite...  client hostname '"+sshCommandRunner.getConnection().getHostname()+"' is a Red Hat Enterprise Linux .* release 6 machine.");
 		Assert.assertEquals(sshCommandRunner.runCommandAndWait("cat /etc/redhat-release | grep -E \"^Red Hat Enterprise Linux .* release 6.*\""),Integer.valueOf(0),"subscription-manager-cli hostname must be RHEL 6.*");
 
 		log.info("Retrieving latest subscription-manager RPM...");
 		String sm_rpm = "/tmp/subscription-manager.rpm";
 		sshCommandRunner.runCommandAndWait("rm -f "+sm_rpm);
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"wget -O "+sm_rpm+" --no-check-certificate \""+rpmLocation+"\"",Integer.valueOf(0),null,"“"+sm_rpm+"” saved");
 
 		log.info("Uninstalling existing subscription-manager RPM...");
 		sshCommandRunner.runCommandAndWait("rpm -e subscription-manager-gnome");
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"rpm -q subscription-manager-gnome",Integer.valueOf(1),"package subscription-manager-gnome is not installed",null);
 		sshCommandRunner.runCommandAndWait("rpm -e subscription-manager");
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"rpm -q subscription-manager",Integer.valueOf(1),"package subscription-manager is not installed",null);
 		
 		log.info("Installing newest subscription-manager RPM...");
 		// using yum localinstall should enable testing on RHTS boxes right off the bat.
 		sshCommandRunner.runCommandAndWait("yum -y localinstall "+sm_rpm+" --nogpgcheck --disablerepo=* --enablerepo="+enablerepofordeps);
 
 		log.info("Installed version of subscription-manager RPM...");
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"rpm -q subscription-manager",Integer.valueOf(0),"^subscription-manager-\\d.*",null);	// subscription-manager-0.63-1.el6.i686
 	}
 	
 	private void updateSMConfigFile(SSHCommandRunner sshCommandRunner, String hostname, String port){
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, defaultConfigFile, "^hostname=.*$", "hostname="+hostname),
 				0,"Updated rhsm config hostname to point to:" + hostname);
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, defaultConfigFile, "^port=.*$", "port="+port),
 				0,"Updated rhsm config port to point to:" + port);
 		
 		// jsefler - 7/21/2010
 		// FIXME DELETEME AFTER FIX FROM <alikins> so, just talked to jsefler and nadathur, we are going to temporarily turn ca verification off, till we get a DEV ca or whatever setup, so we don't break QA at the moment
 		if (isServerOnPremises) {
 		log.warning("TEMPORARY WORKAROUND...");
 		sshCommandRunner.runCommandAndWait("echo \"candlepin_ca_file = /tmp/candlepincacerts/candlepin-ca.crt\"  >> "+defaultConfigFile);
 		}
 	}
 	
 	public void changeCertFrequency(SSHCommandRunner sshCommandRunner, String frequency){
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, defaultConfigFile, "^certFrequency=.*$", "certFrequency="+frequency),
 				0,"Updated rhsmd cert refresh frequency to "+frequency+" minutes");
 		sshCommandRunner.runCommandAndWait("mv "+rhsmcertdLogFile+" "+rhsmcertdLogFile+".bak");
 		//sshCommandRunner.runCommandAndWait("service rhsmcertd restart");
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service rhsmcertd restart",Integer.valueOf(0),"^Starting rhsmcertd "+frequency+"\\[  OK  \\]$",null);
 		Assert.assertEquals(
 				RemoteFileTasks.grepFile(sshCommandRunner,rhsmcertdLogFile, "started: interval = "+frequency),
 				0,"interval reported as "+frequency+" in "+rhsmcertdLogFile);
 	}
 
 	
 	public void sleep(long i) {
 		log.info("Sleeping for "+i+" milliseconds...");
 		try {
 			Thread.sleep(i);
 		} catch (InterruptedException e) {
 			log.info("Sleep interrupted!");
 		}
 	}
 	
 	public int getRandInt(){
 		Random gen = new Random();
 		return Math.abs(gen.nextInt());
 	}
 	
 	public void runRHSMCallAsLang(SSHCommandRunner sshCommandRunner, String lang,String rhsmCall){
 		sshCommandRunner.runCommandAndWait("export LANG="+lang+"; " + rhsmCall);
 	}
 	
 	public void setLanguage(SSHCommandRunner sshCommandRunner, String lang){
 		sshCommandRunner.runCommandAndWait("export LANG="+lang);
 	}
 	
 
 	
 
 }
