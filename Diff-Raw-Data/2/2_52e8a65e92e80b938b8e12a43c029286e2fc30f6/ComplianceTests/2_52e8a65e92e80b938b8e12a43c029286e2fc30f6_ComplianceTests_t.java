 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.List;
 
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.LogMessageUtil;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductNamespace;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  *
  */
 
 
 @Test(groups={"ComplianceTests","AcceptanceTests"})
 public class ComplianceTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact is False when some installed products are subscribable",
 			groups={"configureProductCertDirForSomeProductsSubscribable","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenSomeProductsAreSubscribable() {
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,(String)null,Boolean.TRUE,null, null, null);
 		List<InstalledProduct> installdProducts = clienttasks.getCurrentlyInstalledProducts();
 		Assert.assertFalse(installdProducts.isEmpty(),
 				"Products are currently installed for which the compliance of only SOME are covered by currently available subscription pools.");
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance).toLowerCase(), Boolean.FALSE.toString(),
 				"Before attempting to subscribe and become compliant for all the currently installed products, the system should be incompliant.");
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools();
 		clienttasks.listInstalledProducts();
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance).toLowerCase(), Boolean.FALSE.toString(),
 				"When a system has products installed for which only SOME are covered by available subscription pools, the system should NOT become compliant even after having subscribed to every available subscription pool.");
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports an incompliant status when some installed products are subscribable",
 			groups={"blockedbyBug-723336","blockedbyBug-691480","cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenSomeProductsAreSubscribable"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenSomeProductsAreSubscribable() {
 		String command = clienttasks.rhsmComplianceD+" -s -d";
 		RemoteFileTasks.runCommandAndWait(client, "echo 'Testing "+command+"' >> "+clienttasks.varLogMessagesFile, LogMessageUtil.action());
 
 		// verify the stdout message
 		RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenIncompliant, null);
 		
 		// also verify the /var/syslog/messages
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.varLogMessagesFile, null, rhsmComplianceDSyslogMessageWhenIncompliant, null);
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact is True when all installed products are subscribable",
 			groups={"configureProductCertDirForAllProductsSubscribable","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenAllProductsAreSubscribable() {
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,(String)null,Boolean.TRUE,null, null, null);
 		List<InstalledProduct> installedProducts = clienttasks.getCurrentlyInstalledProducts();
 		Assert.assertFalse(installedProducts.isEmpty(),
 				"Products are currently installed for which the compliance of ALL are covered by currently available subscription pools.");
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance).toLowerCase(), Boolean.FALSE.toString(),
 				"Before attempting to subscribe and become compliant for all the currently installed products, the system should be incompliant.");
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools();
 		clienttasks.listInstalledProducts();
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance).toLowerCase(), Boolean.TRUE.toString(),
 				"When a system has products installed for which ALL are covered by available subscription pools, the system should become compliant after having subscribed to every available subscription pool.");
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports a compliant status when all installed products are subscribable",
 			groups={"blockedbyBug-723336","cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenAllProductsAreSubscribable"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenAllProductsAreSubscribable() {
 		String command = clienttasks.rhsmComplianceD+" -s -d";
 
 		// verify the stdout message
 		RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenCompliant, null);
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact is False when no installed products are subscribable",
 			groups={"configureProductCertDirForNoProductsSubscribable","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenNoProductsAreSubscribable() {
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,(String)null,Boolean.TRUE,null, null, null);
 		List<InstalledProduct> installedProducts = clienttasks.getCurrentlyInstalledProducts();
 		Assert.assertFalse(installedProducts.isEmpty(),
 				"Products are currently installed for which the compliance of NONE are covered by currently available subscription pools.");
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance).toLowerCase(), Boolean.FALSE.toString(),
 				"Before attempting to subscribe and become compliant for all the currently installed products, the system should be incompliant.");
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools();
 		clienttasks.listInstalledProducts();
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance).toLowerCase(), Boolean.FALSE.toString(),
 				"When a system has products installed for which NONE are covered by available subscription pools, the system should NOT become compliant after having subscribed to every available subscription pool.");
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports an incompliant status when no installed products are subscribable",
 			groups={"blockedbyBug-723336","blockedbyBug-691480","cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenNoProductsAreSubscribable"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenNoProductsAreSubscribable() {
 		String command = clienttasks.rhsmComplianceD+" -s -d";
 		RemoteFileTasks.runCommandAndWait(client, "echo 'Testing "+command+"' >> "+clienttasks.varLogMessagesFile, LogMessageUtil.action());
 
 		// verify the stdout message
 		RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenIncompliant, null);
 		
 		// also verify the /var/syslog/messages
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.varLogMessagesFile, null, rhsmComplianceDSyslogMessageWhenIncompliant, null);
 	}
 
 	
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact is True when no products are installed",
 			groups={"configureProductCertDirForNoProductsInstalled","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenNoProductsAreInstalled() {
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,(String)null,Boolean.TRUE,null, null, null);
 		List<InstalledProduct> installedProducts = clienttasks.getCurrentlyInstalledProducts();
 		Assert.assertTrue(installedProducts.isEmpty(),
 				"No products are currently installed.");
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance).toLowerCase(), Boolean.TRUE.toString(),
 				"Because no prodycts are currently installed, the system should inherently be compliant even without subscribing to any subscription pools.");
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools();
 		clienttasks.listInstalledProducts();
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance).toLowerCase(), Boolean.TRUE.toString(),
 				"Even after subscribing to all the available subscription pools, a system with no products installed should remain compliant.");
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports a compliant status when no products are installed",
 			groups={"blockedbyBug-723336","cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenNoProductsAreInstalled"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenNoProductsAreInstalled() {
 		String command = clienttasks.rhsmComplianceD+" -s -d";
 
 		// verify the stdout message
 		RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenCompliant, null);
 	}
 	
 	
 	
 	// Candidates for an automated Test:
	// TODO Bug 649068 - Certs with entitlement start date in the future are treated as Expired
 	// TODO Bug 737553 - should not be compliant for a future subscription
 	
 	
 	
 	
 	// Protected Class Variables ***********************************************************************
 	
 	protected final String productCertDirForSomeProductsSubscribable = "/tmp/sm-someProductsSubscribable";
 	protected final String productCertDirForAllProductsSubscribable = "/tmp/sm-allProductsSubscribable";
 	protected final String productCertDirForNoProductsSubscribable = "/tmp/sm-noProductsSubscribable";
 	protected final String productCertDirForNoProductsinstalled = "/tmp/sm-noProductsInstalled";
 	protected String productCertDir = null;
 	protected final String factNameForSystemCompliance = "system.entitlements_valid"; // "system.compliant"; // changed with the removal of the word "compliance" 3/30/2011
 	protected final String rhsmComplianceDStdoutMessageWhenIncompliant = "System has one or more certificates that are not valid";
 	protected final String rhsmComplianceDStdoutMessageWhenCompliant = "System entitlements appear valid";
 	protected final String rhsmComplianceDSyslogMessageWhenIncompliant = "This system is missing one or more valid entitlement certificates. Please run subscription-manager for more information.";
 	
 	
 	// Protected Methods ***********************************************************************
 	
 	
 	
 	
 	// Configuration Methods ***********************************************************************
 
 	@BeforeClass(groups={"setup"})
 	public void setupProductCertDirsBeforeClass() {
 		
 		// clean out the productCertDirs
 		for (String productCertDir : new String[]{productCertDirForSomeProductsSubscribable,productCertDirForAllProductsSubscribable,productCertDirForNoProductsSubscribable,productCertDirForNoProductsinstalled}) {
 			RemoteFileTasks.runCommandAndAssert(client, "rm -rf "+productCertDir, 0);
 			RemoteFileTasks.runCommandAndAssert(client, "mkdir "+productCertDir, 0);
 		}
 		
 		// autosubscribe
 //		clienttasks.unregister(null,null,null);	// avoid Bug 733525 - [Errno 2] No such file or directory: '/etc/pki/entitlement'
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, true, (String)null, true, null, null, null);
 		
 //		// distribute a copy of the product certs amongst the productCertDirs
 //		List<InstalledProduct> installedProducts = clienttasks.getCurrentlyInstalledProducts();
 //		for (File productCertFile : clienttasks.getCurrentProductCertFiles()) {
 //			ProductCert productCert = clienttasks.getProductCertFromProductCertFile(productCertFile);
 //			// TODO WORKAROUND NEEDED FOR Bug 733805 - the name in the subscription-manager installed product listing is changing after a valid subscribe is performed (https://bugzilla.redhat.com/show_bug.cgi?id=733805)
 //			InstalledProduct installedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", productCert.productName, installedProducts);
 //			if (installedProduct.status.equalsIgnoreCase("Not Subscribed")) {
 //				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForNoProductsSubscribable, 0);
 //				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForSomeProductsSubscribable, 0);
 //			} else if (installedProduct.status.equalsIgnoreCase("Subscribed")) {
 //				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForAllProductsSubscribable, 0);
 //				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForSomeProductsSubscribable, 0);
 //			}
 //		}
 		
 		// distribute a copy of the product certs amongst the productCertDirs
 		List<EntitlementCert> entitlementCerts = clienttasks.getCurrentEntitlementCerts();
 		for (File productCertFile : clienttasks.getCurrentProductCertFiles()) {
 			ProductCert productCert = clienttasks.getProductCertFromProductCertFile(productCertFile);
 			
 			// WORKAROUND NEEDED FOR Bug 733805 - the name in the subscription-manager installed product listing is changing after a valid subscribe is performed (https://bugzilla.redhat.com/show_bug.cgi?id=733805)
 			List<EntitlementCert> correspondingEntitlementCerts = clienttasks.getEntitlementCertsCorrespondingToProductCert(productCert);
 			
 			if (correspondingEntitlementCerts.isEmpty()) {
 				// "Not Subscribed" case...
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForNoProductsSubscribable, 0);
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForSomeProductsSubscribable, 0);
 			} else {
 				// "Subscribed" case...
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForAllProductsSubscribable, 0);
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForSomeProductsSubscribable, 0);
 			}
 			// TODO "Partially Subscribed" case
 			//InstalledProduct installedProduct = clienttasks.getInstalledProductCorrespondingToEntitlementCert(correspondingEntitlementCert);
 		}
 		
 		this.productCertDir = clienttasks.productCertDir;
 	}
 	
 	@AfterClass(groups={"setup"},alwaysRun=true)
 	public void configureProductCertDirAfterClass() {
 		if (clienttasks==null) return;
 		if (this.productCertDir!=null) clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", this.productCertDir);
 	}
 	
 	
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForSomeProductsSubscribable")
 	protected void configureProductCertDirForSomeProductsSubscribable() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForSomeProductsSubscribable);
 		SSHCommandResult r0 = client.runCommandAndWait("ls -1 "+productCertDirForSomeProductsSubscribable+" | wc -l");
 		SSHCommandResult r1 = client.runCommandAndWait("ls -1 "+productCertDirForAllProductsSubscribable+" | wc -l");
 		SSHCommandResult r2 = client.runCommandAndWait("ls -1 "+productCertDirForNoProductsSubscribable+" | wc -l");
 		if (Integer.valueOf(r1.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are subscribable based on the currently available subscriptions.");
 		if (Integer.valueOf(r2.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are non-subscribable based on the currently available subscriptions.");
 		Assert.assertTrue(Integer.valueOf(r0.getStdout().trim())>0 && Integer.valueOf(r1.getStdout().trim())>0 && Integer.valueOf(r2.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains some subscribable products based on the currently available subscriptions.");
 	}
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForAllProductsSubscribable")
 	protected void configureProductCertDirForAllProductsSubscribable() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForAllProductsSubscribable);	
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForAllProductsSubscribable+" | wc -l");
 		if (Integer.valueOf(r.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are subscribable based on the currently available subscriptions.");
 		Assert.assertTrue(Integer.valueOf(r.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains all subscribable products based on the currently available subscriptions.");
 	}
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForNoProductsSubscribable")
 	protected void configureProductCertDirForNoProductsSubscribable() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForNoProductsSubscribable);
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForNoProductsSubscribable+" | wc -l");
 		if (Integer.valueOf(r.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are non-subscribable based on the currently available subscriptions.");
 		Assert.assertTrue(Integer.valueOf(r.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains all non-subscribable products based on the currently available subscriptions.");
 	}
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForNoProductsInstalled")
 	protected void configureProductCertDirForNoProductsInstalled() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForNoProductsinstalled);
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForNoProductsinstalled+" | wc -l");
 		Assert.assertEquals(Integer.valueOf(r.getStdout().trim()),Integer.valueOf(0),
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains no products.");
 	}
 	
 	// Data Providers ***********************************************************************
 
 	
 
 }
 
