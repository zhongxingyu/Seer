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
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsTCMS;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BlockedByBzBug;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.ConsumerCert;
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
 
 	
 	
 	
 
 	@Test(	description="subscription-manager-cli: register to a Candlepin server",
 			groups={"RegisterWithUsernameAndPassword_Test"},
 			dataProvider="getUsernameAndPasswordData")
 	@ImplementsTCMS(id="41677")
 	public void RegisterWithUsernameAndPassword_Test(String username, String password) {
 		log.info("Testing registration to a Candlepin using username="+username+" and password="+password);
 		
 		// determine this user's ability to register
 		SSHCommandResult registerResult = clienttasks.register_(username, password, null, null, null, null, null);
 			
 		// determine this user's available subscriptions
 		List<SubscriptionPool> allAvailableSubscriptionPools=null;
 		if (registerResult.getExitCode()==0) {
 			allAvailableSubscriptionPools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		}
 		
 		// determine this user's owner
 		JSONObject jsonOwner = null;
 		if (registerResult.getExitCode()==0) {
 			String consumerId = clienttasks.getCurrentConsumerId(registerResult);	// c48dc3dc-be1d-4b8d-8814-e594017d63c1 testuser1
 			try {
 				jsonOwner = CandlepinTasks.getOwnerOfConsumerId(serverHostname,serverPort,serverPrefix,clientOwnerUsername,clientOwnerPassword, consumerId);
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (Exception e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 //			try {
 //				JSONObject jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,clientOwnerUsername,clientOwnerPassword,"/consumers/"+consumerId));	
 //				JSONObject jsonOwner_ = (JSONObject) jsonConsumer.getJSONObject("owner");
 //				jsonOwner = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,clientOwnerUsername,clientOwnerPassword,jsonOwner_.getString("href")));	
 //			} catch (Exception e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 		}
 		
 		RegistrationData userData = new RegistrationData(username,password,jsonOwner,registerResult,allAvailableSubscriptionPools);
 		registrationDataList.add(userData);
 		clienttasks.unregister_();
 		
 		Assert.assertEquals(registerResult.getExitCode(), Integer.valueOf(0), "The register command was a success.");
 		Assert.assertContainsMatch(registerResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+username);
 	}
 	
 
 	
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using bogus credentials",
 			groups={},
 			dataProvider="getBogusRegistrationData")
 	@ImplementsTCMS(id="41691, 47918")
 	public void Registration_Test(String username, String password, ConsumerType type, String name, String consumerId, Boolean autosubscribe, Boolean force, String debug, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		log.info("Testing registration to a Candlepin using various options and data and asserting various expected results.");
 		
 		// assure we are unregistered
 //		clienttasks.unregister();
 		
 		// attempt the registration
 		SSHCommandResult sshCommandResult = clienttasks.register_(username, password, type, name, consumerId, autosubscribe, force);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode);
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex);
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex);
 	}
 
 	
 // FIXME DELETEME: Moved to Registration_Test
 //	@Test(	description="subscription-manager-cli: register to a Candlepin server using bogus credentials",
 ////			groups={"sm_stage1"},
 //			dataProvider="getInvalidRegistrationData")
 //	@ImplementsTCMS(id="41691, 47918")
 //	public void InvalidRegistration_Test(String username, String password, String type, String consumerId, Boolean autosubscribe, Boolean force, String debug, String stdoutRegex, String stderrRegex) {
 //		log.info("Testing registration to a Candlepin server using bogus credentials.");
 //		SSHCommandResult sshCommandResult = clienttasks.register(username, password, type, consumerId, autosubscribe, force);
 //		if (stdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), stdoutRegex);
 //		if (stderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), stderrRegex);
 //	}
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to register to a Candlepin server using bogus credentials and check for localized strings results",
 //			groups={"sm_stage1", "sprint9-script"},
 			groups={},
 			dataProvider="getInvalidRegistrationWithLocalizedStringsData")
 	public void InvalidRegistrationWithLocalizedStrings_Test(String lang, String username, String password, Integer exitCode, String stdoutRegex, String stderrRegex) {
 //		sm.unregister();
 //		this.runRHSMCallAsLang(lang,"subscription-manager-cli register --force --username="+username+" --password="+password);
 //		String stdErr = sshCommandRunner.getStderr();
 //		Assert.assertTrue(stdErr.contains(expectedMessage),
 //				"Actual localized error message from failed registration: "+stdErr+" as language "+lang+" matches: "+expectedMessage);
 		
 		log.info("Testing the registration to a candlepin server using invalid credentials and expecting results in language "+lang+". ");
 //		RemoteFileTasks.runCommandAndAssert(client, "export LANG="+lang+"; subscription-manager-cli register --force --username="+username+" --password="+password, exitCode, stdoutRegex, stderrRegex);
 		RemoteFileTasks.runCommandAndAssert(client, "LANG="+lang+"  subscription-manager-cli register --force --username="+username+" --password="+password, exitCode, stdoutRegex, stderrRegex);
 //		Assert.assertEquals(sshCommandRunner.getStderr().trim(),expectedMessage,
 //				"Testing the registration to a candlepin server using invalid credentials and expecting results in language "+lang+". ");
 	}
 	
 // FIXME DELETEME
 //	@Test(	description="subscription-manager-cli: register to a Candlepin server using a user who hasn't accepted terms and conditions, check for localized strings",
 //			dataProvider="invalidRegistrationTermsAndConditionsLocalizedTest",
 //			groups={"sm_stage1", "sprint9-script", "only-IT"})
 //	public void InvalidRegistrationTermsAndConditionsLocalized_Test(String username, String password, String lang, Integer exitCode, String stdoutRegex, String stderrRegex) {
 //		sm.unregister();
 ////		this.runRHSMCallAsLang(lang, "subscription-manager-cli register --force --username="+username+" --password="+password);
 ////		String stdErr = sshCommandRunner.getStderr();
 ////		Assert.assertTrue(stdErr.contains(expectedMessage),
 ////				"Actual localized error message from unaccepted T&Cs registration: "+stdErr+" as language "+lang+" matches: "+expectedMessage);
 //		log.info("Testing the registration to a candlepin server using bogus credentials and expecting results in language "+lang+". ");
 //		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "export LANG="+lang+"; subscription-manager-cli register --force --username="+username+" --password="+password, exitCode, stdoutRegex, stderrRegex);
 ////		Assert.assertEquals(sshCommandRunner.getStderr().trim(),expectedMessage,
 ////				"Testing the registration to a candlepin server using bogus credentials and expecting results in language "+lang+". ");
 //	}
 	
 
 // FIXME DELETEME	Replaced by Registration_Test
 //	@Test(	description="subscription-manager-cli: register to a Candlepin server",
 ////			dependsOnGroups={"sm_stage1"},
 ////			groups={"sm_stage2"},
 ////			alwaysRun=true,
 //			enabled=true)
 //	@ImplementsTCMS(id="41677")
 //	public void ValidRegistration_Test() {
 //		clienttasks.register(clientusername, clientpassword, null, null, null, Boolean.TRUE);
 //		
 //		// assert certificate files are dropped into /etc/pki/consumer
 //		Assert.assertEquals(client.runCommandAndWait("stat /etc/pki/consumer/key.pem").intValue(), 0,
 //				"/etc/pki/consumer/key.pem is present after register");
 //		Assert.assertEquals(client.runCommandAndWait("stat /etc/pki/consumer/cert.pem").intValue(),0,
 //				"/etc/pki/consumer/cert.pem is present after register");
 //	}
 	
 	
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using autosubscribe functionality",
 //			groups={"sm_stage1", "sprint9-script", "blockedByBug-602378", "blockedByBug-616137"},
 //			alwaysRun=true,
 			groups={"ValidRegistrationAutosubscribe_Test","blockedByBug-602378", "blockedByBug-616137"},
 			enabled=true)
 	public void ValidRegistrationAutosubscribe_Test() {
 		if (prodCertProduct.equals("")) throw new SkipException("This testcase requires that you specify a subscription product name for which you have a corresponding product cert installed to test autosubscribe.");
 
 		// before executing this test we need to make sure that subscription matching the product cert that we are about to test with autosubscribe is actually available
 		clienttasks.unregister();
 		clienttasks.register(clientusername, clientpassword, null, null, null, Boolean.TRUE, null);
 		SubscriptionPool pool = clienttasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName", prodCertProduct, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertTrue(pool!=null,"Subscription '"+prodCertProduct+"' is available to user '"+clientusername+"' for testing autosubscribe.");
 
 		String autoProdCert = autosubscribeProdCertFile;
 		teardownAfterValidRegistrationAutosubscribe_Test(); 	// will remove the autosubscribeProdCertFile
 		client.runCommandAndWait("wget -O "+autoProdCert+" "+prodCertLocation);
 		clienttasks.unregister();
 		clienttasks.register(clientusername, clientpassword, null, null, null, Boolean.TRUE, null);
 		// assert that the stdout from the registration includes: Bind Product  Red Hat Directory Server 75822
 		Assert.assertContainsMatch(client.getStdout(),
 				"Bind Product  "+prodCertProduct, "Stdout from the register command contains binding to the expected product.");
 		// assert the bound product is reported in the consumed product listing
 		Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().contains(new ProductSubscription(prodCertProduct, null)),
 				"Expected product "+this.prodCertProduct+" appears in list --consumed call after autosubscribe");
 //		sshCommandRunner.runCommandAndWait("rm -f /etc/pki/product/"+autoProdCert);
 	}
 
 	
 	@Test(	description="subscription-manager-cli: register with --force",
 			groups={"blockedByBug-623264"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void RegisterWithForce_Test() {
 		
 		// start fresh by unregistering
 		clienttasks.unregister();
 		
 		// make sure you are first registered
 		SSHCommandResult sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,null);
 		String firstConsumerId = clienttasks.getCurrentConsumerId();
 		
 		// subscribe to a random pool (so as to consume an entitlement)
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// attempt to register again and assert that you are warned that the system is already registered
 		sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,null);
 		Assert.assertTrue(sshCommandResult.getStdout().startsWith("This system is already registered."),"Expecting: This system is already registered.");
 		
 		// register with force
 		sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,Boolean.TRUE);
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
 	@ImplementsTCMS(id="62352")
 	public void RegisterWithName_Test() {
 		
 		// start fresh by unregistering
 		clienttasks.unregister();
 		
 		// register with a name
 		String name = "RegisterWithName_Tester";
 		SSHCommandResult sshCommandResult = clienttasks.register(clientusername,clientpassword,null,name,null,null, null);
 		
 		// assert the stdout reflects the register name
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+name,"Stdout from register with --name value of "+name);
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
 	@ImplementsTCMS(id="56327")
 	public void ReregisterBasicRegistration_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister();
 		String consumerIdBefore = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername,clientpassword,null,null,null,null, null));
 		
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
 			groups={/*"blockedByBug-624106"*/},
 			enabled=true)
 	@ImplementsTCMS(id="56328")
 	public void ReregisterWithBadIdentityCert_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister();
 		clienttasks.register(clientusername,clientpassword,null,null,null,null, null);
 		
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
 		clienttasks.register(clientusername,clientpassword,null,null,consumerCertBefore.consumerid,null, Boolean.TRUE);
 		
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
 	
 // TODO Automation Candidates for Reregister tests: 
 //		https://bugzilla.redhat.com/show_bug.cgi?id=627685
 //		https://bugzilla.redhat.com/show_bug.cgi?id=627681
 //		https://bugzilla.redhat.com/show_bug.cgi?id=627665
 	
 	
 	
 	
 	// Configuration methods ***********************************************************************
 	
 	String autosubscribeProdCertFile = null;
 	@BeforeGroups(value={"ValidRegistrationAutosubscribe_Test"},alwaysRun=true)
 	public void autosubscribeProdCertFileBeforeValidRegistrationAutosubscribe_Test() {
 		autosubscribeProdCertFile =  clienttasks.productCertDir+"/autosubscribeProdCert"+/*getRandInt()+*/".pem";
 	}
 
 	@AfterGroups (value={"ValidRegistrationAutosubscribe_Test"}, alwaysRun=true)
 	public void teardownAfterValidRegistrationAutosubscribe_Test() {
 		client.runCommandAndWait("rm -f "+autosubscribeProdCertFile);
 	}
 	
 	@BeforeGroups(value={"RegisterWithUsernameAndPassword_Test"},alwaysRun=true)
 	public void unregisterBeforeRegisterWithUsernameAndPassword_Test() {
 		clienttasks.unregister_();
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
 				if (registeredConsumer.jsonOwner==null) {
 					output.write("<tr bgcolor=#F47777>");
 				} else {output.write("<tr>");}
 				if (registeredConsumer.jsonOwner!=null) {
 					output.write("<td valign=top>"+registeredConsumer.jsonOwner.getString("key")+"</td>");
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
 		} catch (JSONException e) {
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
 		// String username, String password, String type, String consumerId, Boolean autosubscribe, Boolean force, String debug, Integer exitCode, String stdoutRegex, String stderrRegex
 		// 									username,			password,						type,	name,	consumerId,	autosubscribe,	force,			debug,	exitCode,				stdoutRegex,																	stderrRegex
 		ll.add(Arrays.asList(new Object[]{	"",					"",								null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: username and password are required to register, try register --help.",	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,		"",								null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: username and password are required to register, try register --help.",	null}));
 		ll.add(Arrays.asList(new Object[]{	"",					clientpassword,					null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: username and password are required to register, try register --help.",	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,		String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			"Invalid username or password"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername+"X",	String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			"Invalid username or password"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,		String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			"Invalid username or password"}));
 
 		// force a successful registration, and then...
 		// FIXME: https://bugzilla.redhat.com/show_bug.cgi?id=616065
 		ll.add(Arrays.asList(new Object[]{	clientusername,		clientpassword,					null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"[a-f,0-9,\\-]{36} "+clientusername,											null}));	// https://bugzilla.redhat.com/show_bug.cgi?id=616065
 
 		// ... try to register again even though the system is already registered
 		ll.add(Arrays.asList(new Object[]{	clientusername,		clientpassword,					null,	null,	null,		null,			Boolean.FALSE,	null,	Integer.valueOf(1),		"This system is already registered. Use --force to override",					null}));
 
 // DELETEME This data moved to a script arguments for clientUsernames and clientPasswords 
 //		if (isServerOnPremises) {
 //			ll.add(Arrays.asList(new Object[]{	"admin",					"admin",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"admin"+"$",						null}));
 //			ll.add(Arrays.asList(new Object[]{	"testuser1",				"password",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"testuser1"+"$",					null}));
 //			ll.add(Arrays.asList(new Object[]{	"testuser2",				"password",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"testuser2"+"$",					null}));
 //		}
 //		else {	// user data comes from https://engineering.redhat.com/trac/IntegratedMgmtQE/wiki/imanage_qe_IT_data_spec
 //			ll.add(Arrays.asList(new Object[]{	"ewayte",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"ewayte"+"$",						null}));
 //			ll.add(Arrays.asList(new Object[]{	"sehuffman",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"epgyadmin",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"epgyadmin"+"$",					null}));
 //			ll.add(Arrays.asList(new Object[]{	"onthebus",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"onthebus"+"$",					null}));
 //			ll.add(Arrays.asList(new Object[]{	"epgy_bsears",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc$"}));
 //			ll.add(Arrays.asList(new Object[]{	"Dadaless",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"emmapease",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"aaronwen",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"davidmcmath",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"davidmcmath"+"$",				null}));
 //			ll.add(Arrays.asList(new Object[]{	"cfairman2",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"cfairman2"+"$",					null}));
 //			ll.add(Arrays.asList(new Object[]{	"macfariman",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"isu-ardwin",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"isu-paras",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"isuchaos",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"isucnc",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"isu-thewags",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"isu-thewags"+"$",				null}));
 //			ll.add(Arrays.asList(new Object[]{	"isu-sukhoy",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"isu-sukhoy"+"$",					null}));
 //			ll.add(Arrays.asList(new Object[]{	"isu-debrm",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc$"}));
 //			ll.add(Arrays.asList(new Object[]{	"isu-acoster",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"isunpappas",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc$"}));
 //			ll.add(Arrays.asList(new Object[]{	"isujdwarn",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"isujdwarn"+"$",					null}));
 //			ll.add(Arrays.asList(new Object[]{	"pascal.catric@a-sis.com",	"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"pascal.catric@a-sis.com"+"$",	null}));
 //			ll.add(Arrays.asList(new Object[]{	"xeops",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"xeops"+"$",						null}));
 //			ll.add(Arrays.asList(new Object[]{	"xeops-js",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"xeop-stenjoa",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"xeop-stenjoa"+"$",				null}));
 //			ll.add(Arrays.asList(new Object[]{	"tmgedp",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"tmgedp"+"$",						null}));
 //			ll.add(Arrays.asList(new Object[]{	"jmarra",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"jmarra"+"$",						null}));
 //			ll.add(Arrays.asList(new Object[]{	"nisadmin",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"nisadmin"+"$",					null}));
 //			ll.add(Arrays.asList(new Object[]{	"darkrider1",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"darkrider1"+"$",					null}));
 //			ll.add(Arrays.asList(new Object[]{	"test5",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc$"}));
 //			ll.add(Arrays.asList(new Object[]{	"amy_redhat2",				"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,													"^The user has been disabled, if this is a mistake, please contact customer service.$"}));
 //			ll.add(Arrays.asList(new Object[]{	"test_1",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"test_1"+"$",						null}));
 //			ll.add(Arrays.asList(new Object[]{	"test2",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"test2"+"$",						null}));
 //			ll.add(Arrays.asList(new Object[]{	"test3",					"redhat",			null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"^[a-f,0-9,\\-]{36} "+"test3"+"$",						null}));
 //
 //		}
 		return ll;
 	}
 	
 // FIXME DELETEME	
 //	@DataProvider(name="invalidRegistrationTermsAndConditionsLocalizedTest")
 //	public Object[][] getInvalidRegistrationTermsAndConditionsLocalizedDataAs2dArray() {
 //		return TestNGUtils.convertListOfListsTo2dArray(getInvalidRegistrationTermsAndConditionsLocalizedDataAsListOfLists());
 //	}
 //	protected List<List<Object>> getInvalidRegistrationTermsAndConditionsLocalizedDataAsListOfLists(){
 //		List<List<Object>> ll = new ArrayList<List<Object>>();
 //		// String username, String password, String lang, String expectedMessage
 //		// String username, String password, String lang, Integer exitCode, String stdoutRegex, String stderrRegex
 //		ll.add(Arrays.asList(new Object[]{tcUnacceptedUsername, tcUnacceptedPassword, 255, null, "en_US.UTF8","You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc"}));
 //		ll.add(Arrays.asList(new Object[]{"ssalevan", "redhat", "en_US.UTF8", 255, null,"The user has been disabled, if this is a mistake, please contact customer service."}));
 //		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362",tcUnacceptedUsername, tcUnacceptedPassword, 255, null, "de_DE.UTF8","Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!")}));
 //		return ll;
 //	}
 	
 
 // FIXME DELETEME Moved to getBogusRegistrationData
 //	@DataProvider(name="getInvalidRegistrationData")
 //	public Object[][] getInvalidRegistrationDataAs2dArray() {
 //		return TestNGUtils.convertListOfListsTo2dArray(getInvalidRegistrationDataAsListOfLists());
 //	}
 //	protected List<List<Object>> getInvalidRegistrationDataAsListOfLists() {
 //		List<List<Object>> ll = new ArrayList<List<Object>>();
 //		// String username, String password, String type, String consumerId, Boolean autosubscribe, Boolean force, String debug, String stdoutRegex, String stderrRegex
 //		ll.add(Arrays.asList(new Object[]{"",					"",								null,	null,	null,	Boolean.TRUE,	null,	"Error: username and password are required to register, try register --help.",	null}));
 //		ll.add(Arrays.asList(new Object[]{clientusername,		"",								null,	null,	null,	Boolean.TRUE,	null,	"Error: username and password are required to register, try register --help.",	null}));
 //		ll.add(Arrays.asList(new Object[]{"",					clientpassword,					null,	null,	null,	Boolean.TRUE,	null,	"Error: username and password are required to register, try register --help.",	null}));
 //		ll.add(Arrays.asList(new Object[]{clientusername,		String.valueOf(getRandInt()),	null,	null,	null,	Boolean.TRUE,	null,	null,																	"Invalid username or password"}));
 //		ll.add(Arrays.asList(new Object[]{clientusername+"X",	String.valueOf(getRandInt()),	null,	null,	null,	Boolean.TRUE,	null,	null,																	"Invalid username or password"}));
 //		ll.add(Arrays.asList(new Object[]{clientusername,		String.valueOf(getRandInt()),	null,	null,	null,	Boolean.TRUE,	null,	null,																	"Invalid username or password"}));
 //
 //		// force a successful registration, and then...
 //		// FIXME: https://bugzilla.redhat.com/show_bug.cgi?id=616065
 //		ll.add(Arrays.asList(new Object[]{clientusername,		clientpassword,						null,	null,	null,	Boolean.TRUE,	null,	"[a-f,0-9,\\-]{36} "+clientusername,								null}));	// https://bugzilla.redhat.com/show_bug.cgi?id=616065
 //
 //		// ... try to register again even though the system is already registered
 //		ll.add(Arrays.asList(new Object[]{clientusername,		clientpassword,						null,	null,	null,	Boolean.FALSE,	null,	"This system is already registered. Use --force to override",		null}));
 //
 //		return ll;
 //	}
 	
 	@DataProvider(name="getInvalidRegistrationWithLocalizedStringsData")
 	public Object[][] getInvalidRegistrationWithLocalizedStringsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInvalidRegistrationWithLocalizedStringsAsListOfLists());
 	}
 	protected List<List<Object>> getInvalidRegistrationWithLocalizedStringsAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 
 		// String lang, String username, String password, Integer exitCode, String stdoutRegex, String stderrRegex
 		
 		// registration test for a user who is invalid
 		ll.add(Arrays.asList(new Object[]{"en_US.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Invalid username or password":"Invalid username or password. To create a login, please visit https://www.redhat.com/wapps/ugc/register.html"}));
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362","de_DE.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Ungültiger Benutzername oder Kennwort":"Ungültiger Benutzername oder Kennwort. So erstellen Sie ein Login, besuchen Sie bitte https://www.redhat.com/wapps/ugc")}));
 															// 642805
 		// registration test for a user who has not accepted Red Hat's Terms and conditions
 		if (!isServerOnPremises) ll.add(Arrays.asList(new Object[]{"en_US.UTF8", tcUnacceptedUsername, tcUnacceptedPassword, 255, null, "You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc"}));
 
 		// registration test for a user who has been disabled
 		if (!isServerOnPremises) ll.add(Arrays.asList(new Object[]{"en_US.UTF8", "xeops-js", "redhat", 255, null,"The user has been disabled, if this is a mistake, please contact customer service."}));
 		if (!isServerOnPremises) ll.add(Arrays.asList(new Object[]{"en_US.UTF8", "ssalevan", "redhat", 255, null,"The user has been disabled, if this is a mistake, please contact customer service."}));
 		if (!isServerOnPremises) ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362","de_DE.UTF8", tcUnacceptedUsername, tcUnacceptedPassword, 255, null, "Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!")}));
 																					// 642805
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
 }
