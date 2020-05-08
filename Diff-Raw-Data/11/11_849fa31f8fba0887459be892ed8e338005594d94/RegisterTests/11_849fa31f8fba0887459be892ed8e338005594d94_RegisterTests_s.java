 package com.redhat.qe.sm.cli.tests;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BlockedByBzBug;
 import com.redhat.qe.auto.testng.LogMessageUtil;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.ConsumerCert;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
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
 @Test(groups={"register"})
 public class RegisterTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager-cli: register to a Candlepin server",
 			groups={"RegisterWithUsernameAndPassword_Test"},
 			dataProvider="getUsernameAndPasswordData")
 	@ImplementsNitrateTest(caseId=41677)
 	public void RegisterWithUsernameAndPassword_Test(String username, String password) {
 		log.info("Testing registration to a Candlepin using username="+username+" and password="+password);
 		
 		// determine this user's ability to register
 		SSHCommandResult registerResult = clienttasks.register_(username, password, null, null, null, null, null, null, null, null);
 			
 		// determine this user's available subscriptions
 		List<SubscriptionPool> allAvailableSubscriptionPools=null;
 		if (registerResult.getExitCode()==0) {
 			allAvailableSubscriptionPools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		}
 		
 		// determine this user's owner
 		String ownerKey = null;
 		if (registerResult.getExitCode()==0) {
 			String consumerId = clienttasks.getCurrentConsumerId(registerResult);	// c48dc3dc-be1d-4b8d-8814-e594017d63c1 testuser1
 			try {
 				ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(serverHostname,serverPort,serverPrefix,username,password, consumerId);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		RegistrationData userData = new RegistrationData(username,password,ownerKey,registerResult,allAvailableSubscriptionPools);
 		registrationDataList.add(userData);
 		clienttasks.unregister_(null, null, null);
 		
 		Assert.assertEquals(registerResult.getExitCode(), Integer.valueOf(0), "The register command was a success.");
 		Assert.assertContainsMatch(registerResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+/*username*/clienttasks.hostname);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using bogus credentials",
 			groups={},
 			dataProvider="getBogusRegistrationData")
 //	@ImplementsNitrateTest(caseId={41691, 47918})
 	@ImplementsNitrateTest(caseId=47918)
 	public void Registration_Test(String username, String password, ConsumerType type, String name, String consumerId, Boolean autosubscribe, Boolean force, String debug, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		log.info("Testing registration to a Candlepin using various options and data and asserting various expected results.");
 		
 		// ensure we are unregistered
 //DO NOT		clienttasks.unregister();
 		
 		// attempt the registration
 		SSHCommandResult sshCommandResult = clienttasks.register_(username, password, type, name, consumerId, autosubscribe, force, null, null, null);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode);
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex);
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to register to a Candlepin server using bogus credentials and check for localized strings results",
 			groups={},
 			dataProvider="getInvalidRegistrationWithLocalizedStringsData")
 	@ImplementsNitrateTest(caseId=41691)
 	public void AttemptRegistrationWithInvalidCredentials_Test(String lang, String username, String password, Integer exitCode, String stdoutRegex, String stderrRegex) {
 
 		// ensure we are unregistered
 		clienttasks.unregister(null, null, null);
 		
 		log.info("Attempting to register to a candlepin server using invalid credentials and expecting output in language "+(lang==null?"DEFAULT":lang));
 		String command = String.format("%s %s register --username=%s --password=%s", lang==null?"":"LANG="+lang, clienttasks.command, username, password);
 		RemoteFileTasks.runCommandAndAssert(client, command, exitCode, stdoutRegex, stderrRegex);
 		
 		// assert that the consumer cert and key have NOT been dropped
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client,clienttasks.consumerKeyFile),0, "Consumer key file '"+clienttasks.consumerKeyFile+"' does NOT exist after an attempt to register with invalid credentials.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client,clienttasks.consumerCertFile),0, "Consumer cert file '"+clienttasks.consumerCertFile+" does NOT exist after an attempt to register with invalid credentials.");
 	}
 	
 
 	@Test(	description="subscription-manager-cli: attempt to register a user who has unaccepted Terms and Conditions",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=48502)
 	public void AttemptRegistrationWithUnacceptedTermsAndConditions_Test() {
 		String username = usernameWithUnacceptedTC;
 		String password = passwordWithUnacceptedTC;
 		if (username.equals("")) throw new SkipException("Must specify a username who has not accepted Terms & Conditions before attempting this test.");
 		AttemptRegistrationWithInvalidCredentials_Test(null,username,password,255, null, "You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to register a user who has been disabled",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=50210)
 	public void AttemptRegistrationWithDisabledUserCredentials_Test() {
 		String username = disabledUsername;
 		String password = disabledPassword;
 		if (username.equals("")) throw new SkipException("Must specify a username who has been disabled before attempting this test.");
 		AttemptRegistrationWithInvalidCredentials_Test(null,username,password,255, null,"The user has been disabled, if this is a mistake, please contact customer service.");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using autosubscribe functionality",
 			groups={"blockedByBug-602378", "blockedByBug-616137"},
 			enabled=true)
 	public void RegisterWithAutosubscribe_Test() {
 
 		log.info("RegisterWithAutosubscribe_Test Strategy:");
 		log.info(" For DEV and QA testing purposes, we may not have valid products installed on the client, therefore we will fake an installed product by following this strategy:");
 		log.info(" 1. Register and subscribe to a randomly available pool");
 		log.info(" 2. copy the downloaded entitlement cert to the product cert directory (this will fake rhsm into believing that the same product is installed)");
 		log.info(" 3. reregister with autosubscribe and assert that the product is bound");
 
 		// Register and subscribe to a randomly available pool
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		File entitlementCertFile = clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		fakeProductCertFile = new File(clienttasks.productCertDir+File.separator+"FAKE_"+entitlementCertFile.getName());
 		
 		// copy the downloaded entitlement cert to the product cert directory (this will fake rhsm into believing that the same product is installed)
 		cleaupAfterClass(); // removes the fake product cert file
 		RemoteFileTasks.runCommandAndAssert(client, "cp "+entitlementCertFile.getPath()+" "+fakeProductCertFile, Integer.valueOf(0));
 		ProductCert fakeProductCert = clienttasks.getProductCertFromProductCertFile(fakeProductCertFile);
 		
 		// reregister with autosubscribe and assert that the product is bound
 		clienttasks.unregister(null, null, null);
 		SSHCommandResult sshCommandResult = clienttasks.register(clientusername, clientpassword, null, null, null, Boolean.TRUE, null, null, null, null);
 		
 		// assert that the sshCommandResult from register indicates the fakeProductCert was subscribed
 		/* Sample sshCommandResult.getStdout():
 		 * d67df9c8-f381-4449-9d17-56094ea58092 testuser1
 		 * Subscribed to Products:
 		 *      RHEL for Physical Servers SVC(37060)
 		 */
 		String productName = fakeProductCert.productName;
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^Subscribed to Products:", "register with autotosubscribe appears to have subscribed to something");
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^\\s+"+productName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"), "Expected ProductName '"+productName+"' was reported as autosubscribed in the output from register with autotosubscribe.");
 
 		// assert that the productName is consumed
 		ProductSubscription productSubscription = ProductSubscription.findFirstInstanceWithMatchingFieldFromList("productName", productName, clienttasks.getCurrentlyConsumedProductSubscriptions());
 		Assert.assertNotNull(productSubscription, "Expected ProductSubscription with ProductName '"+productName+"' is consumed after registering with autosubscribe.");
 
 		// assert that the productName is installed and subscribed
 		InstalledProduct installedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", productName, clienttasks.getCurrentlyInstalledProducts());
 		Assert.assertNotNull(installedProduct, "The status of expected product with ProductName '"+productName+"' is reported in the list of installed products.");
 		Assert.assertEquals(installedProduct.status, "Subscribed", "After registering with autosubscribe, the status of Installed Product '"+productName+"' is Subscribed.");
 	}
 
 	
 	@Test(	description="subscription-manager-cli: register with --force",
 			groups={"blockedByBug-623264"},
 			enabled=true)
 	public void RegisterWithForce_Test() {
 		
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// make sure you are first registered
 		SSHCommandResult sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,null, null, null, null);
 		String firstConsumerId = clienttasks.getCurrentConsumerId();
 		
 		// subscribe to a random pool (so as to consume an entitlement)
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// attempt to register again and assert that you are warned that the system is already registered
 		sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,null, null, null, null);
 		Assert.assertTrue(sshCommandResult.getStdout().startsWith("This system is already registered."),"Expecting: This system is already registered.");
 		
 		// register with force
 		sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,Boolean.TRUE, null, null, null);
 		String secondConsumerId = clienttasks.getCurrentConsumerId();
 		
 		// assert the stdout reflects a new consumer
 		Assert.assertTrue(sshCommandResult.getStdout().startsWith("The system with UUID "+firstConsumerId+" has been unregistered"),
 				"The system with UUID "+firstConsumerId+" has been unregistered");
 		Assert.assertTrue(!secondConsumerId.equals(firstConsumerId),
 				"After registering with force, a newly registered consumerid was returned.");
 
 		// assert that the new consumer is not consuming any entitlements
 		List<ProductSubscription> productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertEquals(productSubscriptions.size(),0,"After registering with force, no product subscriptions should be consumed.");
 	}
 	
 
 	@Test(	description="subscription-manager-cli: register with --name",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=62352)
 	public void RegisterWithName_Test() {
 		
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// register with a name
 		String name = "RegisterWithName_Tester";
 		SSHCommandResult sshCommandResult = clienttasks.register(clientusername,clientpassword,null,name,null,null, null, null, null, null);
 		
 		// assert the stdout reflects the register name
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+name,"Stdout from register with --name value of "+name);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: register with --name and --type",
 			dataProvider="getRegisterWithNameAndTypeData",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterWithNameAndType_Test(String name, ConsumerType type, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// register with a name
		SSHCommandResult sshCommandResult = clienttasks.register(clientusername,clientpassword,type,name,null,null, null, null, null, null);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode,"ExitCode after register with --name="+name+" --type="+type+" options:");
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex,"Stdout after register with --name="+name+" --type="+type+" options:");
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex,"Stderr after register with --name="+name+" --type="+type+" options:");
 
 	}
 	
 	
 	/**
 	 * https://tcms.engineering.redhat.com/case/56327/?from_plan=2476
 		Actions:
 
 			* register a client to candlepin
 			* subscribe to a pool
 			* list consumed
 			* reregister
 
 	    Expected Results:
 
 	 		* check the identity cert has not changed
 	        * check the consumed entitlements have not changed
 	 */
 	@Test(	description="subscription-manager-cli: reregister basic registration",
 			groups={"blockedByBug-636843"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=56327)
 	public void ReregisterBasicRegistration_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister(null, null, null);
 		String consumerIdBefore = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername,clientpassword,null,null,null,null, null, null, null, null));
 		
 		// take note of your identity cert before reregister
 		ConsumerCert consumerCertBefore = clienttasks.getCurrentConsumerCert();
 		
 		// subscribe to a random pool
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// get a list of the consumed products
 		List<ProductSubscription> consumedProductSubscriptionsBefore = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		
 		// reregister
 		//clienttasks.reregister(null,null,null);
 		clienttasks.reregisterToExistingConsumer(clientusername,clientpassword,consumerIdBefore);
 		
 		// assert that the identity cert has not changed
 		ConsumerCert consumerCertAfter = clienttasks.getCurrentConsumerCert();
 		Assert.assertEquals(consumerCertBefore, consumerCertAfter, "The consumer identity cert has not changed after reregistering with consumerid.");
 		
 		// assert that the user is still consuming the same products
 		List<ProductSubscription> consumedProductSubscriptionsAfter = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertTrue(
 				consumedProductSubscriptionsAfter.containsAll(consumedProductSubscriptionsBefore) &&
 				consumedProductSubscriptionsBefore.size()==consumedProductSubscriptionsAfter.size(),
 				"The list of consumed products after reregistering is identical.");
 	}
 	
 	
 	/**
 	 * https://tcms.engineering.redhat.com/case/56328/?from_plan=2476
 	 * 
 		Actions:
 
 	 		* register a client to candlepin (take note of the uuid returned)
 	 		* take note of your identity cert info using openssl x509
 	 		* subscribe to a pool
 	 		* list consumed
 	 		* ls /etc/pki/entitlement/products
 	 		* Now.. mess up your identity..  mv /etc/pki/consumer/cert.pem /bak
 	 		* run the "reregister" command w/ username and passwd AND w/consumerid=<uuid>
 
 		Expected Results:
 
 	 		* after running reregister you should have a new identity cert
 	 		* after registering you should still the same products consumed (list consumed)
 	 		* the entitlement serials should be the same as before the registration
 	 */
 	@Test(	description="subscription-manager-cli: bad identity cert",
 			groups={"blockedByBug-624106"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=56328)
 	public void ReregisterWithBadIdentityCert_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername,clientpassword,null,null,null,null, null, null, null, null);
 		
 		// take note of your identity cert
 		ConsumerCert consumerCertBefore = clienttasks.getCurrentConsumerCert();
 		
 		// subscribe to a random pool
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// get a list of the consumed products
 		List<ProductSubscription> consumedProductSubscriptionsBefore = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		
 		// Now.. mess up your identity..  by borking its content
 		log.info("Messing up the identity cert by borking its content...");
 		RemoteFileTasks.runCommandAndAssert(client, "openssl x509 -noout -text -in "+clienttasks.consumerCertFile+" > /tmp/stdout; mv /tmp/stdout -f "+clienttasks.consumerCertFile, 0);
 		
 		// reregister w/ username, password, and consumerid
 		//clienttasks.reregister(client1username,client1password,consumerCertBefore.consumerid);
 		log.warning("The subscription-manager-cli reregister module has been eliminated and replaced by register --consumerid (b3c728183c7259841100eeacb7754c727dc523cd)...");
 		clienttasks.register(clientusername,clientpassword,null,null,consumerCertBefore.consumerid,null, Boolean.TRUE, null, null, null);
 		
 		// assert that the identity cert has not changed
 		ConsumerCert consumerCertAfter = clienttasks.getCurrentConsumerCert();
 		Assert.assertEquals(consumerCertBefore, consumerCertAfter, "The consumer identity cert has not changed after reregistering with consumerid.");
 	
 		// assert that the user is still consuming the same products
 		List<ProductSubscription> consumedProductSubscriptionsAfter = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertTrue(
 				consumedProductSubscriptionsAfter.containsAll(consumedProductSubscriptionsBefore) &&
 				consumedProductSubscriptionsBefore.size()==consumedProductSubscriptionsAfter.size(),
 				"The list of consumed products after reregistering is identical.");
 	}
 	
 	
 	/**
 	 * https://tcms.engineering.redhat.com/case/72845/?from_plan=2476
 	 * 
 Actions:
 
     * register with username and password and remember the consumerid
     * subscribe to one or more subscriptions
     * list the consumed subscriptions and remember them
     * clean system
     * assert that there are no entitlements on the system
     * register with same username, password and existing consumerid
     * assert that originally consumed subscriptions are once again being consumed
 
 	
 Expected Results:
 
     * when registering a new system to an already existing consumer, all of the existing consumers entitlement certs should be downloaded to the new system
 
 	 */
 	@Test(	description="register with existing consumerid should automatically refresh entitlements",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=72845)
 	public void ReregisterWithConsumerIdShouldAutomaticallyRefreshEntitlements_Test() {
 		
 		// register with username and password and remember the consumerid
 		clienttasks.unregister(null, null, null);
 		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername,clientpassword,null,null,null,null, null, null, null, null));
 		
 		// subscribe to one or more subscriptions
 		//// subscribe to a random pool
 		//List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		//SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		//clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType.system);
 
 		// list the consumed subscriptions and remember them
 		List <ProductSubscription> originalConsumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		// also remember the current entitlement certs
 		List <EntitlementCert> originalEntitlementCerts= clienttasks.getCurrentEntitlementCerts();
 		
 		// clean system
 		clienttasks.clean(null, null, null);
 		
 		// assert that there are no entitlements on the system
 		//Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().isEmpty(),"There are NO consumed Product Subscriptions on this system after running clean");
 		Assert.assertTrue(clienttasks.getCurrentEntitlementCerts().isEmpty(),"There are NO Entitlement Certs on this system after running clean");
 		
 		// register with same username, password and existing consumerid
 		// Note: no need to register with force as running clean wipes system of all local registration data
 		clienttasks.register(clientusername,clientpassword,null,null,consumerId,null, null, null, null, null);
 
 		// assert that originally consumed subscriptions are once again being consumed
 		List <ProductSubscription> consumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertEquals(consumedProductSubscriptions.size(),originalConsumedProductSubscriptions.size(), "The number of consumed Product Subscriptions after registering to an existing consumerid matches his original count.");
 		for (ProductSubscription productSubscription : consumedProductSubscriptions) {
 			Assert.assertContains(originalConsumedProductSubscriptions, productSubscription);
 		}
 		// assert that original entitlement certs are once on the system
 		List <EntitlementCert> entitlementCerts = clienttasks.getCurrentEntitlementCerts();
 		Assert.assertEquals(entitlementCerts.size(),originalEntitlementCerts.size(), "The number of Entitlement Certs on the system after registering to an existing consumerid matches his original count.");
 		for (EntitlementCert entitlementCert : entitlementCerts) {
 			Assert.assertContains(originalEntitlementCerts, entitlementCert);
 		}
 		
 	}
 	
 
 	@Test(	description="register with an empty /var/lib/rhsm/facts/facts.json file",
 			groups={"blockedByBug-667953","blockedByBug-669208"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RegisterWithAnEmptyRhsmFactsJsonFile_Test() {
 		
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhsmFactsJsonFile)==1, "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' exists");
 		log.info("Emptying rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"'...");
 		client.runCommandAndWait("echo \"\" > "+clienttasks.rhsmFactsJsonFile, LogMessageUtil.action());
 		SSHCommandResult result = client.runCommandAndWait("cat "+clienttasks.rhsmFactsJsonFile, LogMessageUtil.action());
 		Assert.assertTrue(result.getStdout().trim().equals(""), "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' is empty.");
 		
 		log.info("Attempt to register with an empty rhsm facts file (expecting success)...");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, Boolean.TRUE, null, null, null);
 	}
 	
 	
 	@Test(	description="register with a missing /var/lib/rhsm/facts/facts.json file",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RegisterWithAnMissingRhsmFactsJsonFile_Test() {
 		
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhsmFactsJsonFile)==1, "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' exists");
 		log.info("Deleting rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"'...");
 		RemoteFileTasks.runCommandAndWait(client, "rm -f "+clienttasks.rhsmFactsJsonFile, LogMessageUtil.action());
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhsmFactsJsonFile)==0, "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' has been removed");
 		
 		log.info("Attempt to register with a missing rhsm facts file (expecting success)...");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, Boolean.TRUE, null, null, null);
 	}
 	
 	// TODO Candidates for an automated Test:
 	//		https://bugzilla.redhat.com/show_bug.cgi?id=627685
 	//		https://bugzilla.redhat.com/show_bug.cgi?id=627665
 	//		https://bugzilla.redhat.com/show_bug.cgi?id=668814
 	//		https://bugzilla.redhat.com/show_bug.cgi?id=669395
 
 
 
 	
 	
 	// Configuration methods ***********************************************************************
 	
 	File fakeProductCertFile = null;
 	@AfterClass (alwaysRun=true)
 	public void cleaupAfterClass() {
 		if (fakeProductCertFile!=null) {
 			client.runCommandAndWait("rm -f "+fakeProductCertFile);
 		}
 	}
 	
 	@BeforeGroups(value={"RegisterWithUsernameAndPassword_Test"},alwaysRun=true)
 	public void unregisterBeforeRegisterWithUsernameAndPassword_Test() {
 		clienttasks.unregister_(null, null, null);
 	}
 	@AfterGroups(value={"RegisterWithUsernameAndPassword_Test"},alwaysRun=true)
 	public void generateRegistrationReportTableAfterRegisterWithUsernameAndPassword_Test() {
 		
 		// now dump out the list of userData to a file
 	    File file = new File("CandlepinRegistrationReport.html"); // this will be in the workspace directory on hudson
 	    DateFormat dateFormat = new SimpleDateFormat("MMM d HH:mm:ss yyyy z");
 	    try {
 	    	Writer output = new BufferedWriter(new FileWriter(file));
 			
 			// write out the rows of the table
 			output.write("<html>\n");
 			output.write("<table border=1>\n");
 			output.write("<h2>Candlepin Registration Report</h2>");
 			//output.write("<h3>(generated on "+dateFormat.format(System.currentTimeMillis())+")</h3>");
 			output.write("Candlepin hostname= <b>"+serverHostname+"</b>\n");
 			output.write("(generated on "+dateFormat.format(System.currentTimeMillis())+")\n");
 			output.write("<tr><th>Owner</th><th>Username/<BR>Password</th><th>Registration Output</th><th>All Available Subscriptions (to system consumers)</th></tr>\n");
 			for (RegistrationData registeredConsumer : registrationDataList) {
 				if (registeredConsumer.ownerKey==null) {
 					output.write("<tr bgcolor=#F47777>");
 				} else {output.write("<tr>");}
 				if (registeredConsumer.ownerKey!=null) {
 					output.write("<td valign=top>"+registeredConsumer.ownerKey+"</td>");
 				} else {output.write("<td/>");};
 				if (registeredConsumer.username!=null) {
 					output.write("<td valign=top>"+registeredConsumer.username+"/<BR>"+registeredConsumer.password+"</td>");
 				} else {output.write("<td/>");};
 				if (registeredConsumer.registerResult!=null) {
 					output.write("<td valign=top>"+registeredConsumer.registerResult.getStdout()+registeredConsumer.registerResult.getStderr()+"</td>");
 				} else {output.write("<td/>");};
 				if (registeredConsumer.allAvailableSubscriptionPools!=null) {
 					output.write("<td valign=top><ul>");
 					for (SubscriptionPool availableSubscriptionPool : registeredConsumer.allAvailableSubscriptionPools) {
 						output.write("<li>"+availableSubscriptionPool+"</li>");
 					}
 					output.write("</ul></td>");
 				} else {output.write("<td/>");};
 				output.write("</tr>\n");
 			}
 			output.write("</table>\n");
 			output.write("</html>\n");
 		    output.close();
 		    //log.info(file.getCanonicalPath()+" exists="+file.exists()+" writable="+file.canWrite());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 
 	
 	// Protected methods ***********************************************************************
 
 	protected void checkInvalidRegistrationStrings(SSHCommandRunner sshCommandRunner, String username, String password){
 		sshCommandRunner.runCommandAndWait("subscription-manager-cli register --username="+username+this.getRandInt()+" --password="+password+this.getRandInt()+" --force");
 		Assert.assertContainsMatch(sshCommandRunner.getStdout(),
 				"Invalid username or password. To create a login, please visit https:\\/\\/www.redhat.com\\/wapps\\/ugc\\/register.html");
 	}
 	
 	
 	
 	// Data Providers ***********************************************************************
 
 	@DataProvider(name="getBogusRegistrationData")
 	public Object[][] getBogusRegistrationDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getBogusRegistrationDataAsListOfLists());
 	}
 	protected List<List<Object>> getBogusRegistrationDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg();
 
 		
 		// String username, String password, String type, String consumerId, Boolean autosubscribe, Boolean force, String debug, Integer exitCode, String stdoutRegex, String stderrRegex
 		// 									username,			password,						type,	name,	consumerId,	autosubscribe,	force,			debug,	exitCode,				stdoutRegex,																	stderrRegex
 		ll.add(Arrays.asList(new Object[]{	"",					"",								null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: username and password are required to register, try register --help.",	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,		"",								null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: password not provided. Use --password <value>",							null}));
 		ll.add(Arrays.asList(new Object[]{	"",					clientpassword,					null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: username not provided. Use --username <name>",							null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,		String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			uErrMsg}));
 		ll.add(Arrays.asList(new Object[]{	clientusername+"X",	String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			uErrMsg}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,		String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			uErrMsg}));
 
 		// force a successful registration, and then...
 		ll.add(Arrays.asList(new Object[]{	new BlockedByBzBug(new String[]{"616065","669395"},
 											clientusername,		clientpassword,					null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"[a-f,0-9,\\-]{36} "+/*clientusername*/clienttasks.hostname,					null)}));
 
 		// ... try to register again even though the system is already registered
 		ll.add(Arrays.asList(new Object[]{	clientusername,		clientpassword,					null,	null,	null,		null,			Boolean.FALSE,	null,	Integer.valueOf(1),		"This system is already registered. Use --force to override",					null}));
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getInvalidRegistrationWithLocalizedStringsData")
 	public Object[][] getInvalidRegistrationWithLocalizedStringsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInvalidRegistrationWithLocalizedStringsAsListOfLists());
 	}
 	protected List<List<Object>> getInvalidRegistrationWithLocalizedStringsAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg();
 
 		// String lang, String username, String password, Integer exitCode, String stdoutRegex, String stderrRegex
 		
 		// registration test for a user who is invalid
 		ll.add(Arrays.asList(new Object[]{"en_US.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, uErrMsg}));
 		
 		// registration test for a user who is invalid (translated)
 		if (!isServerOnPremises) ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"615362","642805"},"de_DE.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Ungültige Mandate"/*"Ungültiger Benutzername oder Kennwort"*/:"Ungültiger Benutzername oder Kennwort. So erstellen Sie ein Login, besuchen Sie bitte https://www.redhat.com/wapps/ugc")}));
 		else                     ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362",                       "de_DE.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Ungültige Mandate"/*"Ungültiger Benutzername oder Kennwort"*/:"Ungültiger Benutzername oder Kennwort. So erstellen Sie ein Login, besuchen Sie bitte https://www.redhat.com/wapps/ugc")}));
 
 		// registration test for a user who has not accepted Red Hat's Terms and conditions (translated)  Man, why did you do something?
 		if (!usernameWithUnacceptedTC.equals("")) {
 			if (!isServerOnPremises) ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"615362","642805"},"de_DE.UTF8", usernameWithUnacceptedTC, passwordWithUnacceptedTC, 255, null, "Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!")}));
 			else                     ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362",                       "de_DE.UTF8", usernameWithUnacceptedTC, passwordWithUnacceptedTC, 255, null, "Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!")}));
 		}
 		
 		// registration test for a user who has been disabled (translated)
 		if (!disabledUsername.equals("")) {
 			ll.add(Arrays.asList(new Object[]{"en_US.UTF8", disabledUsername, disabledPassword, 255, null,"The user has been disabled, if this is a mistake, please contact customer service."}));
 		}
 
 		return ll;
 	}
 	
 
 	@DataProvider(name="getRegisterWithNameAndTypeData")
 	public Object[][] getRegisterWithNameAndTypeDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterWithNameAndTypeDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterWithNameAndTypeDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		// String name, ConsumerType type, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 		// 									name,				type,					exitCode,			stdoutRegex,						stderrRegex
 		for (ConsumerType type : ConsumerType.values()) {
 			String name = type.toString()+"_NAME";
 			
 			// https://bugzilla.redhat.com/show_bug.cgi?id=661130
 			if (type.equals(ConsumerType.person)) {
 				ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("661130",	name,	type,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+clientusername,	null)}));			
 				continue;
 			}
 			
 			ll.add(Arrays.asList(new Object[]{  name,	type,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+name,	null}));			
 		}
 
 		return ll;
 	}
 }
