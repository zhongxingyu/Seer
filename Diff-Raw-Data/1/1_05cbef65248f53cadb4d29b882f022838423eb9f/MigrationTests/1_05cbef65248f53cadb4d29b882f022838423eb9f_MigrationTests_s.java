 package com.redhat.qe.sm.cli.tests;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BlockedByBzBug;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  *	References:
  *		http://documentation-stage.bne.redhat.com/docs/en-US/Red_Hat_Enterprise_Linux/5/html/Deployment_Guide/rhn-migration.html
  *		https://engineering.redhat.com/trac/PBUPM/browser/trunk/documents/Releases/RHEL6/Variants/RHEL6-Variants.rst
  *		http://linuxczar.net/articles/rhel-installation-numbers
  */
 @Test(groups={"MigrationTests"})
 public class MigrationTests extends SubscriptionManagerCLITestScript {
 
 	// Test methods ***********************************************************************
 	
 	@Test(	description="Verify that the channel-cert-mapping.txt exists",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyChannelCertMappingFileExists_Test() throws FileNotFoundException, IOException {
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, channelCertMappingFile),1,"The expected channel cert mapping file '"+channelCertMappingFile+"' exists.");
 		
 		// [root@jsefler-onprem-5client ~]# cat /usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt
 		// rhn-tools-rhel-x86_64-server-5-beta: none
 		// rhn-tools-rhel-x86_64-server-5: Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem
 		// rhn-tools-rhel-x86_64-client-5-beta: none
 		// rhn-tools-rhel-x86_64-client-5: Client-Client-x86_64-efe91c1c-78d7-4d19-b2fb-3c88cfc2da35-68.pem
 		SSHCommandResult result = client.runCommandAndWait("cat "+channelCertMappingFile);
 		Properties p = new Properties();
 		p.load(new ByteArrayInputStream(result.getStdout().getBytes("UTF-8")));
 		for (Object key: p.keySet()){
 			// load the channelsToProductCertFilesMap
 			channelsToProductCertFilesMap.put((String)key, p.getProperty((String)(key)));
 			// load the mappedProductCertFiles
 			if (!channelsToProductCertFilesMap.get(key).equalsIgnoreCase("none"))
 				mappedProductCertFiles.add(channelsToProductCertFilesMap.get(key));
 		}
 	}
 	
 	@Test(	description="Verify that all product cert files mapped in channel-cert-mapping.txt exist",
 			groups={},
 			dependsOnMethods={"VerifyChannelCertMappingFileExists_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyAllMappedProductCertFilesExists_Test() {
 
 		boolean allMappedProductCertFilesExist = true;
 		for (String mappedProductCertFile : mappedProductCertFiles) {
 			mappedProductCertFile = baseProductsDir+"/"+mappedProductCertFile;
 			if (RemoteFileTasks.testFileExists(client, mappedProductCertFile)==1) {
 				log.info("Mapped productCert file '"+mappedProductCertFile+"' exists.");		
 			} else {
 				log.warning("Mapped productCert file '"+mappedProductCertFile+"' does NOT exist.");
 				allMappedProductCertFilesExist = false;
 			}
 		}
 		Assert.assertTrue(allMappedProductCertFilesExist,"All of the productCert files mapped in '"+channelCertMappingFile+"' exist.");
 	}
 	
 	
 	@Test(	description="Verify that all existing product cert files are mapped in channel-cert-mapping.txt",
 			groups={},
 			dependsOnMethods={"VerifyChannelCertMappingFileExists_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyAllExistingProductCertFilesAreMapped_Test() {
 		
 		// get a list of all the existing product cert files
 		SSHCommandResult result = client.runCommandAndWait("ls "+baseProductsDir+"/*.pem");
 		List<String> existingProductCertFiles = Arrays.asList(result.getStdout().split("\\n"));
 		boolean allExitingProductCertFilesAreMapped = true;
 		for (String existingProductCertFile : existingProductCertFiles) {
 			if (mappedProductCertFiles.contains(new File(existingProductCertFile).getName())) {
 				log.info("Existing productCert file '"+existingProductCertFile+"' is mapped in '"+channelCertMappingFile+"'.");
 
 			} else {
 				log.warning("Existing productCert file '"+existingProductCertFile+"' is NOT mapped in '"+channelCertMappingFile+"'.");
 				allExitingProductCertFilesAreMapped = false;
 			}
 		}
 		Assert.assertTrue(allExitingProductCertFilesAreMapped,"All of the existing productCert files in directory '"+baseProductsDir+"' are mapped to a channel in '"+channelCertMappingFile+"'.");
 	}
 	
 	
 	
 	
 	@Test(	description="execute the install-num-migrate-to-rhsm with a known instnumber and assert the expected productCerts are copied",
 			groups={},
 			dependsOnMethods={"VerifyChannelCertMappingFileExists_Test"},
 			dataProvider="InstallNumMigrateToRhsmData")
 	public void InstallNumMigrateToRhsm_Test(Object bugzilla, String instnumber, String[] expectedProductCertPems) {
 		String command;
 		SSHCommandResult result;
 		
 		// deleting the currently installed product certs
 		//client.runCommandAndWait("rm -f "+clienttasks.productCertDir+"/*.pem");
 		
 		// test dryrun instnumber
 		command = "install-num-migrate-to-rhsm --dryrun --instnumber="+instnumber;
 		result = RemoteFileTasks.runCommandAndAssert(client,command,0);
 	}
 	
 	// Candidates for an automated Test:
 	// TODO tool that explains/gives valid inst numbers   http://linuxczar.net/articles/rhel-installation-numbers
 	// TODO Bug 749948 - [Release Notes and Deployment Guide] Migration tooling from RHN Classic to Cert-based RHN for RHEL 5 (edit)
 	// TODO Bug 769856 - confusing output from rhn-migrate-to-rhsm when autosubscribe fails
 	// TODO Bug 771615 - Got Traceback with –force migration
 	
 	
 	// Configuration methods ***********************************************************************
 	
 
 	@BeforeClass(groups="setup")
 	public void setupBeforeClass() {
 		if (clienttasks==null) return;
 		
 		// determine the full path to the channelCertMappingFile
 		if (clienttasks.redhatRelease.contains("release 5")) baseProductsDir+="-5";
 		if (clienttasks.redhatRelease.contains("release 6")) baseProductsDir+="-6";
 		channelCertMappingFile = baseProductsDir+"/"+channelCertMappingFile;
 	}
 	
 	@BeforeClass(groups="setup")
 	public void rememberOriginallyInstalledRedHatProductCertsBeforeClass() {
 		if (clienttasks==null) return;
 		
 		// review the currently installed product certs and filter out the ones from test automation (indicated by suffix "_.pem")
 		for (File productCertFile : clienttasks.getCurrentProductCertFiles()) {
 			if (!productCertFile.getName().endsWith("_.pem")) {	// The product cert files ending in "_.pem" are not true RedHat products
 				originallyInstalledRedHatProductCerts.add(clienttasks.getProductCertFromProductCertFile(productCertFile));
 			}
 		}
 	}
 	
 	@BeforeClass(groups="setup")
 	public void backupProductCertsBeforeClass() {
 		if (clienttasks==null) return;
 		
 		// determine the original productCertDir value
 		//productCertDirRestore = clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm", "productCertDir");
 		productCertDirOriginal = clienttasks.productCertDir;
 		
 		log.info("Backing up all the currently installed product certs...");
 		client.runCommandAndWait("mkdir -p "+productCertDirBackup+"; rm -f "+productCertDirBackup+"/*.pem");
 		client.runCommandAndWait("cp "+productCertDirOriginal+"/*.pem "+productCertDirBackup);
 	}
 	
 	@AfterClass(groups="setup")
 	public void restoreProductCertsAfterClass() {
 		if (clienttasks==null) return;
 		
 		log.info("Restoring the originally installed product certs...");
 		client.runCommandAndWait("rm -f "+productCertDirOriginal+"/*.pem");
 		client.runCommandAndWait("cp "+productCertDirBackup+"/*.pem "+productCertDirOriginal);
 		//clienttasks.config(false, false, true, new String[]{"rhsm","productcertdir",productCertDirOriginal});
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", productCertDirOriginal);
 	}
 	
 	
 	// Protected methods ***********************************************************************
 	protected String baseProductsDir = "/usr/share/rhsm/product/RHEL";
 	protected String channelCertMappingFile = "channel-cert-mapping.txt";
 	List<String> mappedProductCertFiles = new ArrayList<String>();	// list of all the mapped product cert file names in the mapping file (e.g. Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem)
 	Map<String,String> channelsToProductCertFilesMap = new HashMap<String,String>();	// map of all the channels to product cert file names (e.g. key=rhn-tools-rhel-x86_64-server-5 value=Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem)
 	protected String productCertDirOriginal = null;
 	protected String productCertDirBackup = "/tmp/productCertDirMigrationTestsBackup";
 	List<ProductCert> originallyInstalledRedHatProductCerts = new ArrayList<ProductCert>();
 
 	
 	
 	// Data Providers ***********************************************************************
 
 	@DataProvider(name="InstallNumMigrateToRhsmData")
 	public Object[][] getInstallNumMigrateToRhsmDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInstallNumMigrateToRhsmDataAsListOfLists());
 	}
 	public List<List<Object>> getInstallNumMigrateToRhsmDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		// REFRENCE DATA FROM: http://linuxczar.net/articles/rhel-installation-numbers
 		ll.add(Arrays.asList(new Object[]{null,	"0000000e0017fc01", new String[]{"68.pem","71.pem"}}));	// Client
 		ll.add(Arrays.asList(new Object[]{null,	"000000990007fc02", new String[]{}}));					// Red Hat Global Desktop
 		ll.add(Arrays.asList(new Object[]{null,	"000000e90007fc00", new String[]{"69.pem"}}));			// Server
 		ll.add(Arrays.asList(new Object[]{null,	"00000065000bfc00", new String[]{"69.pem","83.pem"}}));	// Server with Cluster
 		ll.add(Arrays.asList(new Object[]{null,	"000000ab000ffc00", new String[]{"69.pem","90.pem","83.pem"}}));	// Server with ClusterStorage
 		ll.add(Arrays.asList(new Object[]{null,	"000000e30013fc00", new String[]{"69.pem"}}));	// Server with HPC
 		ll.add(Arrays.asList(new Object[]{null,	"000000890017fc00", new String[]{"69.pem"}}));	// Server with Directory
 		ll.add(Arrays.asList(new Object[]{null,	"00000052001bfc00", new String[]{"69.pem"}}));	// Server with SMB
 		//new BlockedByBzBug("773707")
 
 		return ll;
 	}
 	
 	
 	
 	/*
 	 * 
 	 */
 	
 	// EXAMPLE TAKEN FROM THE DEPLOYMENT GUIDE http://documentation-stage.bne.redhat.com/docs/en-US/Red_Hat_Enterprise_Linux/5/html/Deployment_Guide/rhn-install-num.html
 /*
 	[root@jsefler-onprem-5server ~]# python /usr/lib/python2.4/site-packages/instnum.py da3122afdb7edd23
 	Product: RHEL Client
 	Type: Installer Only
 	Options: Eval FullProd Workstation
 	Allowed CPU Sockets: Unlimited
 	Allowed Virtual Instances: Unlimited
 	Package Repositories: Client Workstation
 
 	key: 14299426 'da3122'
 	checksum: 175 'af'
 	options: 4416 'Eval FullProd Workstation'
 	socklimit: -1 'Unlimited'
 	virtlimit: -1 'Unlimited'
 	type: 2 'Installer Only'
 	product: 1 'client'
 
 	{'Workstation': 'Workstation', 'Base': 'Client'}
 
 	da31-22af-db7e-dd23
 	[root@jsefler-onprem-5server ~]# 
 
 	[root@jsefler-onprem-5server ~]# install-num-migrate-to-rhsm -d -i da3122afdb7edd23
 	Copying /usr/share/rhsm/product/RHEL-5/Client-Workstation-x86_64-efa6382a-44c4-408b-a142-37ad4be54aa6-71.pem to /etc/pki/product/71.pem
 	Copying /usr/share/rhsm/product/RHEL-5/Client-Client-x86_64-efe91c1c-78d7-4d19-b2fb-3c88cfc2da35-68.pem to /etc/pki/product/68.pem
 	[root@jsefler-onprem-5server ~]# 
 	[root@jsefler-onprem-5server ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Client-Client-x86_64-efe91c1c-78d7-4d19-b2fb-3c88cfc2da35-68.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 	            1.3.6.1.4.1.2312.9.1.68.1: 
 	                . Red Hat Enterprise Linux Desktop
 	            1.3.6.1.4.1.2312.9.1.68.2: 
 	                ..5.7
 	            1.3.6.1.4.1.2312.9.1.68.3: 
 	                ..x86_64
 	            1.3.6.1.4.1.2312.9.1.68.4: 
 	                ..rhel-5,rhel-5-client
 	[root@jsefler-onprem-5server ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Client-Workstation-x86_64-efa6382a-44c4-408b-a142-37ad4be54aa6-71.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 	            1.3.6.1.4.1.2312.9.1.71.1: 
 	                .$Red Hat Enterprise Linux Workstation
 	            1.3.6.1.4.1.2312.9.1.71.2: 
 	                ..5.7
 	            1.3.6.1.4.1.2312.9.1.71.3: 
 	                ..x86_64
 	            1.3.6.1.4.1.2312.9.1.71.4: 
 	                .,rhel-5-client-workstation,rhel-5-workstation
 	[root@jsefler-onprem-5server ~]# 
 	
 	
 	ANOTHER EXAMPLE
 	
 	
 [root@dell-pe1855-01 ~]# ls /etc/pki/product/
 69.pem
 [root@dell-pe1855-01 ~]# cat /etc/redhat-release 
 Red Hat Enterprise Linux Server release 5.8 Beta (Tikanga)
 [root@dell-pe1855-01 ~]# openssl x509 -text -in /etc/pki/product/69.pem | grep -A1 1.3.6.1.4.1.2312.9.1
             1.3.6.1.4.1.2312.9.1.69.1: 
                 ..Red Hat Enterprise Linux Server
             1.3.6.1.4.1.2312.9.1.69.2: 
                 ..5.8 Beta
             1.3.6.1.4.1.2312.9.1.69.3: 
                 ..x86_64
             1.3.6.1.4.1.2312.9.1.69.4: 
                 ..rhel-5,rhel-5-server
 
 [root@dell-pe1855-01 ~]# cat /etc/sysconfig/rhn/install-num 
 49af89414d147589
 [root@dell-pe1855-01 ~]# install-num-migrate-to-rhsm -d -i 49af89414d147589
 Copying /usr/share/rhsm/product/RHEL-5/Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem to /etc/pki/product/69.pem
 Copying /usr/share/rhsm/product/RHEL-5/Server-ClusterStorage-x86_64-66e8d727-f5aa-4e37-a04b-787fbbc3430c-90.pem to /etc/pki/product/90.pem
 Copying /usr/share/rhsm/product/RHEL-5/Server-Cluster-x86_64-bebfe30e-22a5-4788-8611-744ea744bdc0-83.pem to /etc/pki/product/83.pem
 [root@dell-pe1855-01 ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem | grep -A1 1.3.6.1.4.1.2312.9.1
             1.3.6.1.4.1.2312.9.1.69.1: 
                 ..Red Hat Enterprise Linux Server
             1.3.6.1.4.1.2312.9.1.69.2: 
                 ..5.7
             1.3.6.1.4.1.2312.9.1.69.3: 
                 ..x86_64
             1.3.6.1.4.1.2312.9.1.69.4: 
                 ..rhel-5,rhel-5-server
 
 [root@dell-pe1855-01 ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Server-ClusterStorage-x86_64-66e8d727-f5aa-4e37-a04b-787fbbc3430c-90.pem | grep -A1 1.3.6.1.4.1.2312.9.1
             1.3.6.1.4.1.2312.9.1.90.1: 
                 .<Red Hat Enterprise Linux Resilient Storage (for RHEL Server)
             1.3.6.1.4.1.2312.9.1.90.2: 
                 ..5.7
             1.3.6.1.4.1.2312.9.1.90.3: 
                 ..x86_64
             1.3.6.1.4.1.2312.9.1.90.4: 
                 .2rhel-5-server-clusterstorage,rhel-5-clusterstorage
                 
 [root@dell-pe1855-01 ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Server-Cluster-x86_64-bebfe30e-22a5-4788-8611-744ea744bdc0-83.pem | grep -A1 1.3.6.1.4.1.2312.9.1
             1.3.6.1.4.1.2312.9.1.83.1: 
                 .<Red Hat Enterprise Linux High Availability (for RHEL Server)
             1.3.6.1.4.1.2312.9.1.83.2: 
                 ..5.7
             1.3.6.1.4.1.2312.9.1.83.3: 
                 ..x86_64
             1.3.6.1.4.1.2312.9.1.83.4: 
                 .$rhel-5-server-cluster,rhel-5-cluster
 
 	
 */
 }
