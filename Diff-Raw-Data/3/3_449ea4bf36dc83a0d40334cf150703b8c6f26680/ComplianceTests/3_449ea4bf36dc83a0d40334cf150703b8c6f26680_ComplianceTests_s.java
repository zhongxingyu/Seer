 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.List;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  *
  */
 
 
 @Test(groups={"ComplianceTests"})
 public class ComplianceTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager: compliance test",
 			groups={"configureProductCertDirForSomeProductsSubscribable"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenSomeProductsAreSubscribable() {
 		clienttasks.register(clientusername,clientpassword,null,null,null,null,Boolean.TRUE,null,null,null);
 		List<InstalledProduct> installdProducts = clienttasks.getCurrentlyInstalledProducts();
 		Assert.assertFalse(installdProducts.isEmpty(),
 				"Products are currently installed for which the compliance of only SOME are covered by currently available subscription pools.");
 		Assert.assertEquals(clienttasks.getFactValue("system.compliant").toLowerCase(), Boolean.FALSE.toString(),
 				"Before attempting to subscribe and become compliant for all the currently installed products, the system should be incompliant.");
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType.system);
 		clienttasks.listInstalledProducts();
 		Assert.assertEquals(clienttasks.getFactValue("system.compliant").toLowerCase(), Boolean.FALSE.toString(),
 				"When a system has products installed for which only SOME are covered by available subscription pools, the system should NOT become compliant even after having subscribed to every available subscription pool.");
 	}
 	
 	@Test(	description="subscription-manager: compliance test",
 			groups={"configureProductCertDirForAllProductsSubscribable"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenAllProductsAreSubscribable() {
 		clienttasks.register(clientusername,clientpassword,null,null,null,null,Boolean.TRUE,null,null,null);
 		List<InstalledProduct> installdProducts = clienttasks.getCurrentlyInstalledProducts();
 		Assert.assertFalse(installdProducts.isEmpty(),
 				"Products are currently installed for which the compliance of ALL are covered by currently available subscription pools.");
 		Assert.assertEquals(clienttasks.getFactValue("system.compliant").toLowerCase(), Boolean.FALSE.toString(),
 				"Before attempting to subscribe and become compliant for all the currently installed products, the system should be incompliant.");
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType.system);
 		clienttasks.listInstalledProducts();
 		Assert.assertEquals(clienttasks.getFactValue("system.compliant").toLowerCase(), Boolean.TRUE.toString(),
 				"When a system has products installed for which ALL are covered by available subscription pools, the system should become compliant after having subscribed to every available subscription pool.");
 	}
 	
 	@Test(	description="subscription-manager: compliance test",
 			groups={"configureProductCertDirForNoProductsSubscribable"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenNoProductsAreSubscribable() {
 		clienttasks.register(clientusername,clientpassword,null,null,null,null,Boolean.TRUE,null,null,null);
 		List<InstalledProduct> installdProducts = clienttasks.getCurrentlyInstalledProducts();
 		Assert.assertFalse(installdProducts.isEmpty(),
 				"Products are currently installed for which the compliance of NONE are covered by currently available subscription pools.");
 		Assert.assertEquals(clienttasks.getFactValue("system.compliant").toLowerCase(), Boolean.FALSE.toString(),
 				"Before attempting to subscribe and become compliant for all the currently installed products, the system should be incompliant.");
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType.system);
 		clienttasks.listInstalledProducts();
 		Assert.assertEquals(clienttasks.getFactValue("system.compliant").toLowerCase(), Boolean.FALSE.toString(),
 				"When a system has products installed for which NONE are covered by available subscription pools, the system should NOT become compliant after having subscribed to every available subscription pool.");
 	}
 	
 	@Test(	description="subscription-manager: compliance test",
 			groups={"configureProductCertDirForNoProductsInstalled"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenNoProductsAreInstalled() {
 		clienttasks.register(clientusername,clientpassword,null,null,null,null,Boolean.TRUE,null,null,null);
 		List<InstalledProduct> installdProducts = clienttasks.getCurrentlyInstalledProducts();
 		Assert.assertTrue(installdProducts.isEmpty(),
 				"No products are currently installed.");
 		Assert.assertEquals(clienttasks.getFactValue("system.compliant").toLowerCase(), Boolean.TRUE.toString(),
 				"Because no prodycts are currently installed, the system should inherently be compliant even without subscribing to any subscription pools.");
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType.system);
 		clienttasks.listInstalledProducts();
 		Assert.assertEquals(clienttasks.getFactValue("system.compliant").toLowerCase(), Boolean.TRUE.toString(),
 				"Even after subscribing to all the available subscription pools, a system with no products installed should remain compliant.");
 
 	}
 	
 
 	
 	
 	
 	// TODO Candidates for an automated Test:
	
	
 	
 	
 	
 	
 	
 	// Protected Class Variables ***********************************************************************
 	
 	protected final String productCertDirForSomeProductsSubscribable = "/tmp/sm-someProductsSubscribable";
 	protected final String productCertDirForAllProductsSubscribable = "/tmp/sm-allProductsSubscribable";
 	protected final String productCertDirForNoProductsSubscribable = "/tmp/sm-noProductsSubscribable";
 	protected final String productCertDirForNoProductsinstalled = "/tmp/sm-noProductsInstalled";
 	protected String productCertDir = null;
 	
 	
 	
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
 		clienttasks.register(clientusername, clientpassword, null, null, null, Boolean.TRUE, Boolean.TRUE, null, null, null);
 		
 		// distribute a copy of the product certs amongst the productCertDirs
 		List<InstalledProduct> installedProducts = clienttasks.getCurrentlyInstalledProducts();
 		for (File productCertFile : clienttasks.getCurrentProductCertFiles()) {
 			ProductCert productCert = clienttasks.getProductCertFromProductCertFile(productCertFile);
 			InstalledProduct installedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", productCert.productName, installedProducts);
 			if (installedProduct.status.equalsIgnoreCase("Not Subscribed")) {
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForNoProductsSubscribable, 0);
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForSomeProductsSubscribable, 0);
 			} else if (installedProduct.status.equalsIgnoreCase("Subscribed")) {
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForAllProductsSubscribable, 0);
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCertFile+" "+productCertDirForSomeProductsSubscribable, 0);
 			}
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
 		Assert.assertTrue(Integer.valueOf(r0.getStdout().trim())>0 && Integer.valueOf(r1.getStdout().trim())>0 && Integer.valueOf(r2.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains some subscribable products based on the currently available subscriptions.");
 	}
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForAllProductsSubscribable")
 	protected void configureProductCertDirForAllProductsSubscribable() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForAllProductsSubscribable);	
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForAllProductsSubscribable+" | wc -l");
 		Assert.assertTrue(Integer.valueOf(r.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains all subscribable products based on the currently available subscriptions.");
 	}
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForNoProductsSubscribable")
 	protected void configureProductCertDirForNoProductsSubscribable() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForNoProductsSubscribable);
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForNoProductsSubscribable+" | wc -l");
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
 
