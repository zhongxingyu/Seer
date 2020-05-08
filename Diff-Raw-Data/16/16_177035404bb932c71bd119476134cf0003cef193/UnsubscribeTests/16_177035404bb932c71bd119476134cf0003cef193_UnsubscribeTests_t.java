 package rhsm.cli.tests;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.testng.SkipException;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import rhsm.base.SubscriptionManagerCLITestScript;
 import rhsm.data.EntitlementCert;
 import rhsm.data.ProductSubscription;
 import rhsm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author ssalevan
  * @author jsefler
  *
  */
 @Test(groups={"UnsubscribeTests"})
 public class UnsubscribeTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 
 	@Test(description="subscription-manager-cli: unsubscribe consumer from an entitlement using product ID",
 			groups={"blockedByBug-584137", "blockedByBug-602852", "blockedByBug-873791"},
 			dataProvider="getAllConsumedProductSubscriptionsData")
 	@ImplementsNitrateTest(caseId=41688)
 	public void UnsubscribeFromValidProductIDs_Test(ProductSubscription productSubscription){
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 //		sm.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.unsubscribeFromProductSubscription(productSubscription);
 	}
 	
 	
 	@Test(description="Unsubscribe product entitlement and re-subscribe",
 			groups={"blockedByBug-584137", "blockedByBug-602852", "blockedByBug-873791"},
 			dataProvider="getAllConsumedProductSubscriptionsData")
 	@ImplementsNitrateTest(caseId=41898)
 	public void ResubscribeAfterUnsubscribe_Test(ProductSubscription productSubscription) throws Exception{
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 //		sm.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		
 //		// first make sure we are subscribed to all pools
 //		sm.register(username,password,null,null,null,null);
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		
 		// now loop through each consumed product subscription and unsubscribe/re-subscribe
 		SubscriptionPool pool = clienttasks.getSubscriptionPoolFromProductSubscription(productSubscription,sm_clientUsername,sm_clientPassword);
 		if (clienttasks.unsubscribeFromProductSubscription(productSubscription)) {
 			Assert.assertNotNull(pool, "Successfully determined what SubscriptionPool ProductSubscription '"+productSubscription+"' was consumed from.");
 			clienttasks.subscribeToSubscriptionPoolUsingProductId(pool);	// only re-subscribe when unsubscribe was a success
 		}
 	}
 	
 	
 	@Test(description="Malicious Test - Unsubscribe and then attempt to reuse the revoked entitlement cert.",
 			groups={"AcceptanceTests","blockedByBug-584137","blockedByBug-602852","blockedByBug-672122","blockedByBug-804227","blockedByBug-871146"},
 			//dataProvider="getAvailableSubscriptionPoolsData",	// very thorough, but takes too long to execute and rarely finds more bugs
 			dataProvider="getRandomSubsetOfAvailableSubscriptionPoolsData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41903)
 	public void UnsubscribeAndAttemptToReuseTheRevokedEntitlementCert_Test(SubscriptionPool subscriptionPool){
 		client.runCommandAndWait("killall -9 yum");
 		
 		// subscribe to a pool
 		File entitlementCertFile = clienttasks.subscribeToSubscriptionPoolUsingPoolId(subscriptionPool);
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		List <EntitlementCert> entitlementCerts = new ArrayList<EntitlementCert>();
 		entitlementCerts.add(entitlementCert);
 
 		// assert all of the entitlement certs are reported in the "yum repolist all"
 		clienttasks.assertEntitlementCertsInYumRepolist(entitlementCerts,true);
  
 		// copy entitlement certificate from location /etc/pki/entitlement/product/ to /tmp
 		log.info("Now let's copy the valid entitlement cert to the side so we can maliciously try to reuse it after its serial has been unsubscribed.");
 		String randDir = "/tmp/sm-certForSubscriptionPool-"+subscriptionPool.poolId;
 		client.runCommandAndWait("rm -rf "+randDir+"; mkdir -p "+randDir);
 		client.runCommandAndWait("cp "+entitlementCertFile+" "+randDir+"; cp "+clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(entitlementCertFile)+" "+randDir);
 		
 		// unsubscribe from the pool (Note: should be the only one subscribed too
 		clienttasks.unsubscribeFromSerialNumber(clienttasks.getSerialNumberFromEntitlementCertFile(entitlementCertFile));
 		
 		// assert all of the entitlement certs are no longer reported in the "yum repolist all"
 		clienttasks.assertEntitlementCertsInYumRepolist(entitlementCerts,false);
 
 		// restart the rhsm cert deamon
 		// Note: by passing assertCertificatesUpdate=null, we are assuming that the subsequent assertions will execute within 2 min before the next cert update and waitForRegexInRhsmcertdLog 
 		int certFrequency = 2; clienttasks.restart_rhsmcertd(certFrequency, null, false, null);
 		
 		// move the copied entitlement certificate from /tmp to location /etc/pki/entitlement/product
 		// Note: this is malicious activity (user is trying to continue using entitlement certs that have been unsubscribed)
 		client.runCommandAndWait("cp -f "+randDir+"/* "+clienttasks.entitlementCertDir);
 		
 		// assert all of the entitlement certs are reported in the "yum repolist all" again
 		clienttasks.assertEntitlementCertsInYumRepolist(entitlementCerts,true);
 		
 		// assert that the rhsmcertd will clean up the malicious activity
 		log.info("Now let's wait for \"certificates updated\" by the rhsmcertd and assert that the deamon deletes the copied entitlement certificate since it was put on candlepins certificate revocation list during the unsubscribe.");
 		String marker = "Testing UnsubscribeAndAttemptToReuseTheRevokedEntitlementCert_Test..."; // https://tcms.engineering.redhat.com/case/41692/
 		RemoteFileTasks.runCommandAndAssert(client,"echo \""+marker+"\" >> "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0));
 		clienttasks.waitForRegexInRhsmcertdLog(".*certificates updated.*", certFrequency);	// https://bugzilla.redhat.com/show_bug.cgi?id=672122
 		sleep(10000); // plus a little padding for the client to do it's thing
 
 		Assert.assertTrue(!RemoteFileTasks.testExists(client, entitlementCertFile.getPath()),"Entitlement certificate '"+entitlementCertFile+"' was deleted by the rhsm certificate deamon.");
 		clienttasks.assertEntitlementCertsInYumRepolist(entitlementCerts,false);
 		
 		// cleanup
 		client.runCommandAndWait("rm -rf "+randDir);
 	}
 
 	
 	@Test(	description="subscription-manager: subscribe and then unsubscribe from a future subscription pool",
 			groups={"blockedByBug-727970"},
 			dataProvider="getAllFutureSystemSubscriptionPoolsData",
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void UnsubscribeAfterSubscribingToFutureSubscriptionPool_Test(SubscriptionPool pool) throws Exception {
 		
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(System.currentTimeMillis());
 		
 		// subscribe to the future SubscriptionPool
 		SSHCommandResult subscribeResult = clienttasks.subscribe(null,null,pool.poolId,null,null,null,null,null,null,null, null);
 
 		// assert that the granted EntitlementCert and its corresponding key exist
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertCorrespondingToSubscribedPool(pool);
 		File entitlementCertFile = clienttasks.getEntitlementCertFileFromEntitlementCert(entitlementCert);
 		File entitlementCertKeyFile = clienttasks.getEntitlementCertKeyFileFromEntitlementCert(entitlementCert);
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, entitlementCertFile.getPath()), 1,"EntitlementCert file exists after subscribing to future SubscriptionPool.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, entitlementCertKeyFile.getPath()), 1,"EntitlementCert key file exists after subscribing to future SubscriptionPool.");
 
 		// find the consumed ProductSubscription from the future SubscriptionPool
 		ProductSubscription productSubscription =  ProductSubscription.findFirstInstanceWithMatchingFieldFromList("serialNumber",entitlementCert.serialNumber, clienttasks.getCurrentlyConsumedProductSubscriptions());
 		Assert.assertNotNull(productSubscription,"Found the newly consumed ProductSubscription after subscribing to future subscription pool '"+pool.poolId+"'.");
 		
 		// unsubscribe
 		clienttasks.unsubscribeFromProductSubscription(productSubscription);
 		
 		// assert that the EntitlementCert file and its key are removed.
 		/* NOTE: this assertion is already built into the unsubscribeFromProductSubscription task above
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, entitlementCertFile.getPath()), 0,"EntitlementCert file has been removed after unsubscribing to future SubscriptionPool.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, entitlementCertKeyFile.getPath()), 0,"EntitlementCert key file has been removed after unsubscribing to future SubscriptionPool.");
 		*/
 	}
 	
 	@Test(description="Attempt to unsubscribe when not registered",
 			groups={"blockedByBug-735338","blockedByBug-838146","blockedByBug-865590","blockedByBug-873791"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void UnsubscribeFromSerialWhenNotRegistered_Test() {
 	
 		// first make sure we are subscribed to a pool
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(List<String>)null,null,null,true, false, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(0);
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(clienttasks.subscribeToSubscriptionPool(pool));
 		
 		// now remove the consumer cert to simulate an unregister
 		clienttasks.removeAllCerts(true,false, false);
 		Assert.assertEquals(clienttasks.identity_(null,null,null,null,null,null,null).getStdout().trim(),clienttasks.msg_ConsumerNotRegistered);
 		
 		// now unsubscribe from the serial number (while not registered)
 		Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().size()>0, "We should be consuming an entitlement (even while not registered)");
 		clienttasks.unsubscribeFromSerialNumber(entitlementCert.serialNumber);
 		Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions().size(), 0, "We should not be consuming any entitlements after unsubscribing (while not registered)");
 	}
 	
 	@Test(description="Attempt to unsubscribe when from an invalid serial number",
 			groups={"blockedByBug-706889","blockedByBug-867766"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void UnsubscribeFromAnInvalidSerial_Test() {
 		SSHCommandResult result;
 		
 		BigInteger serial = BigInteger.valueOf(-123);
 		result = clienttasks.unsubscribe_(null, serial, null, null, null);
 		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "Asserting exit code when attempting to unsubscribe from an invalid serial number.");
 		//Assert.assertEquals(result.getStderr().trim(), "Error: '-123' is not a valid serial number");
 		//Assert.assertEquals(result.getStdout().trim(), "");
 		// stderr moved to stdout by Bug 867766 - [RFE] unsubscribe from multiple entitlement certificates using serial numbers 
 		Assert.assertEquals(result.getStdout().trim(), String.format("Error: '%s' is not a valid serial number",serial));
 		Assert.assertEquals(result.getStderr().trim(), "");
 		
 		List<BigInteger> serials = Arrays.asList(new BigInteger[]{BigInteger.valueOf(123),BigInteger.valueOf(-456),BigInteger.valueOf(789)});
 		result = clienttasks.unsubscribe_(null, serials, null, null, null);
 		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "Asserting exit code when attempting to unsubscribe from an invalid serial number.");
 		Assert.assertEquals(result.getStdout().trim(), String.format("Error: '%s' is not a valid serial number",serials.get(1)));
 		Assert.assertEquals(result.getStderr().trim(), "");
 	}
 	
 	
 	@Test(description="Verify the feedback after unsubscribing from all consumed subscriptions using unsubscribe --all",
 			groups={"blockedByBug-812388","blockedByBug-844455"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void UnsubscribeFromAll_Test() {
 	
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(List<String>)null,null,null,true, null, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.subscribeToTheCurrentlyAllAvailableSubscriptionPoolsCollectively();
 		
 		// unsubscribe from all and assert # subscriptions are unsubscribed
 		SSHCommandResult result = clienttasks.unsubscribe(true, (BigInteger)null, null, null, null);
 		Assert.assertEquals(result.getStdout().trim(), String.format("This machine has been unsubscribed from %s subscriptions",pools.size()),"Expected feedback when unsubscribing from all the currently consumed subscriptions.");
 		
 		// now attempt to unsubscribe from all again and assert 0 subscriptions are unsubscribed
 		result = clienttasks.unsubscribe(true, (BigInteger)null, null, null, null);
 		Assert.assertEquals(result.getStdout().trim(), String.format("This machine has been unsubscribed from %s subscriptions",0),"Expected feedback when unsubscribing from all when no subscriptions are currently consumed.");
 
 	}
 	
 	@Test(description="Verify the feedback after unsubscribing from all consumed subscriptions using unsubscribe --serial SERIAL1 --serial SERIAL2 --serial SERIAL3 etc.",
 			groups={"blockedByBug-867766"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void UnsubscribeFromAllSerials_Test() throws Exception {
 	
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(List<String>)null,null,null,true, null, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.subscribeToTheCurrentlyAllAvailableSubscriptionPoolsCollectively();
 		if (pools.isEmpty()) throw new SkipException("This test requires multiple available pools.");
 		
 		// unsubscribe from all serials in one call and assert the feedback
 		List<ProductSubscription> productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
		String expectedStdoutMsgLabel;
		expectedStdoutMsgLabel = "Successfully unsubscribed serial numbers:";
		expectedStdoutMsgLabel = "Successfully removed serial numbers:";	// changed by bug 874749
		String expectedStdoutMsg = expectedStdoutMsgLabel;
		for (ProductSubscription productSubscription : productSubscriptions) expectedStdoutMsg+="\n   "+productSubscription.serialNumber;
 		SSHCommandResult result = clienttasks.unsubscribeFromTheCurrentlyConsumedProductSubscriptionsCollectively();
		// NOTE: This expectedStdoutMsg makes a huge assumption about the order of the unsubscribed serial numbers printed to stdout
		// NOTE: TIME TO FIX THIS ASSERTION... Assert.assertEquals(result.getStdout().trim(), expectedStdoutMsg, "Stdout feedback when unsubscribing from all the currently consumed subscriptions.");
		List<String> expectedStdoutMsgAsList = new ArrayList<String>(Arrays.asList(expectedStdoutMsg.split("\n"))); expectedStdoutMsgAsList.remove(expectedStdoutMsgLabel);
		List<String> actualStdoutMsgAsList = new ArrayList<String>(Arrays.asList(result.getStdout().trim().split("\n"))); actualStdoutMsgAsList.remove(expectedStdoutMsgLabel);
		Assert.assertTrue(expectedStdoutMsgAsList.containsAll(actualStdoutMsgAsList) && actualStdoutMsgAsList.containsAll(expectedStdoutMsgAsList), "Stdout feedback when unsubscribing from all the currently consumed subscriptions contains all the expected serial numbers:"+expectedStdoutMsg.replace(expectedStdoutMsgLabel, ""));
 	}
 	
 	@Test(description="Verify the feedback after unsubscribing from all consumed subscriptions (including revoked serials) using unsubscribe --serial SERIAL1 --serial SERIAL2 --serial SERIAL3 etc.",
 			groups={"blockedByBug-867766"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void UnsubscribeFromAllSerialsIncludingRevokedSerials_Test() {
 	
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(List<String>)null,null,null,true, null, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		if (pools.isEmpty()) throw new SkipException("This test requires multiple available pools.");
 		
 		// subscribe to all of the available pools
 		List<String> poolIds = new ArrayList<String>();
 		for (SubscriptionPool pool : pools) poolIds.add(pool.poolId);
 		clienttasks.subscribe(null, null, poolIds, null, null, null, null, null, null, null, null);
 
 		// unsubscribe from all serials in one call and assert the feedback;
 		List<BigInteger> serials = new ArrayList<BigInteger>();
 		for(ProductSubscription productSubscription : clienttasks.getCurrentlyConsumedProductSubscriptions()) serials.add(productSubscription.serialNumber);
 		SSHCommandResult result = clienttasks.unsubscribe(null,serials,null,null,null);
 		String expectedStdoutMsg;
 		expectedStdoutMsg = "Successfully unsubscribed serial numbers:";
 		expectedStdoutMsg = "Successfully removed serial numbers:";	// changed by bug 874749
 		for (BigInteger serial : serials) expectedStdoutMsg+="\n   "+serial;	// NOTE: This expectedStdoutMsg makes a huge assumption about the order of the unsubscribed serial numbers printed to stdout
 		Assert.assertEquals(result.getStdout().trim(), expectedStdoutMsg, "Stdout feedback when unsubscribing from all the currently consumed subscriptions.");
 		
 		// remember the unsubscribed serials as revoked serials
 		List<BigInteger> revokedSerials = new ArrayList<BigInteger>();
 		for (BigInteger serial : serials) revokedSerials.add(serial);
 		
 		// subscribe to all the available pools again
 		clienttasks.subscribe(null, null, poolIds, null, null, null, null, null, null, null, null);
 		
 		// now attempt to unsubscribe from both the current serials AND the previously consumed serials in one call and assert the feedback
 		serials.clear();
 		List<ProductSubscription> productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		for (int i=0; i<productSubscriptions.size(); i++) {
 			serials.add(productSubscriptions.get(i).serialNumber);
 			serials.add(revokedSerials.get(i));
 		}
 		expectedStdoutMsg = "Successfully unsubscribed serial numbers:";	// added by bug 867766
 		expectedStdoutMsg = "Successfully removed serial numbers:";	// changed by bug 874749
 		for (ProductSubscription productSubscription : productSubscriptions) expectedStdoutMsg+="\n   "+productSubscription.serialNumber;	// NOTE: This expectedStdoutMsg makes a huge assumption about the order of the unsubscribed serial numbers printed to stdout
 		expectedStdoutMsg +="\n";
 		//expectedStdoutMsg += "Unsuccessfully unsubscribed serial numbers:";	// added by bug 867766
 		expectedStdoutMsg += "Unsuccessfully removed serial numbers:";	// changed by bug 874749
 		for (BigInteger revokedSerial : revokedSerials) expectedStdoutMsg+="\n   "+String.format("Entitlement Certificate with serial number %s could not be found.", revokedSerial);	// NOTE: This expectedStdoutMsg makes a huge assumption about the order of the unsubscribed serial numbers printed to stdout
 		result = clienttasks.unsubscribe(null,serials,null,null,null);
 		Assert.assertEquals(result.getStdout().trim(), expectedStdoutMsg, "Stdout feedback when unsubscribing from all the currently consumed subscriptions (including revoked serials).");
 	}
 //TOO MUCH LOGGING FROM TOO MANY ASSERTIONS;  DELETEME IF ABOVE TEST WORKS WELL
 //	public void UnsubscribeFromAllSerialsIncludingRevokedSerials_Test() {
 //		
 //		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(List<String>)null,null,null,true, null, null, null, null);
 //		List<SubscriptionPool> pools = clienttasks.subscribeToTheCurrentlyAllAvailableSubscriptionPoolsCollectively();
 //		if (pools.isEmpty()) throw new SkipException("This test requires multiple available pools.");
 //		
 //		// unsubscribe from all serials in one call and assert the feedback
 //		List<ProductSubscription> productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 //		SSHCommandResult result = clienttasks.unsubscribeFromTheCurrentlyConsumedProductSubscriptionsCollectively();
 //		String expectedStdoutMsg = "Successfully unsubscribed serial numbers:";
 //		for (ProductSubscription productSubscription : productSubscriptions) expectedStdoutMsg+="\n   "+productSubscription.serialNumber;	// NOTE: This expectedStdoutMsg makes a huge assumption about the order of the unsubscribed serial numbers printed to stdout
 //		Assert.assertEquals(result.getStdout().trim(), expectedStdoutMsg, "Stdout feedback when unsubscribing from all the currently consumed subscriptions.");
 //
 //		List<BigInteger> revokedSerials = new ArrayList<BigInteger>();
 //		for (ProductSubscription productSubscription : productSubscriptions) revokedSerials.add(productSubscription.serialNumber);
 //		
 //		// subscribe to all the available pools again
 //		clienttasks.subscribeToTheCurrentlyAllAvailableSubscriptionPoolsCollectively();
 //		
 //		// now attempt to unsubscribe from both the current serials AND the previously consumed serials in one call and assert the feedback
 //		productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 //		List<BigInteger> serials = new ArrayList<BigInteger>();
 //		for (int i=0; i<productSubscriptions.size(); i++) {
 //			serials.add(productSubscriptions.get(i).serialNumber);
 //			serials.add(revokedSerials.get(i));
 //		}
 //		expectedStdoutMsg = "Successfully unsubscribed serial numbers:";
 //		for (ProductSubscription productSubscription : productSubscriptions) expectedStdoutMsg+="\n   "+productSubscription.serialNumber;	// NOTE: This expectedStdoutMsg makes a huge assumption about the order of the unsubscribed serial numbers printed to stdout
 //		expectedStdoutMsg +="\n";
 //		expectedStdoutMsg += "Unsuccessfully unsubscribed serial numbers:";
 //		for (BigInteger revokedSerial : revokedSerials) expectedStdoutMsg+="\n   "+String.format("Entitlement Certificate with serial number %s could not be found.", revokedSerial);	// NOTE: This expectedStdoutMsg makes a huge assumption about the order of the unsubscribed serial numbers printed to stdout
 //		result = clienttasks.unsubscribe(false,serials,null,null,null);
 //		Assert.assertEquals(result.getStdout().trim(), expectedStdoutMsg, "Stdout feedback when unsubscribing from all the currently consumed subscriptions (including revoked serials).");
 //	}
 	
 	
 	@Test(	description="subscription-manager: unsubscribe and remove can be used interchangably",
 			groups={"blockedByBug-874749"},
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void RemoveDeprecatesUnsubscribe_Test() throws Exception {
 		SSHCommandResult result = client.runCommandAndWait(clienttasks.command+" --help");
 		Assert.assertContainsMatch(result.getStdout(), "^\\s*unsubscribe\\s+Deprecated, see remove$");
 		
 		SSHCommandResult unsubscribeResult;
 		SSHCommandResult removeResult;
 		
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, true, false, null, null, null);
 		unsubscribeResult = client.runCommandAndWait(clienttasks.command+" unsubscribe --serial=123");
 		removeResult = client.runCommandAndWait(clienttasks.command+" remove --serial=123");
 		Assert.assertEquals(unsubscribeResult.toString(), removeResult.toString(), "Results from 'unsubscribe' and 'remove' module commands should be identical.");
 		unsubscribeResult = client.runCommandAndWait(clienttasks.command+" unsubscribe --all");
 		removeResult = client.runCommandAndWait(clienttasks.command+" remove --all");
 		Assert.assertEquals(unsubscribeResult.toString(), removeResult.toString(), "Results from 'unsubscribe' and 'remove' module commands should be identical.");
 
 		clienttasks.unregister(null,null,null);
 		unsubscribeResult = client.runCommandAndWait(clienttasks.command+" unsubscribe --serial=123");
 		removeResult = client.runCommandAndWait(clienttasks.command+" remove --serial=123");
 		Assert.assertEquals(unsubscribeResult.toString(), removeResult.toString(), "Results from 'unsubscribe' and 'remove' module commands should be identical.");
 		unsubscribeResult = client.runCommandAndWait(clienttasks.command+" unsubscribe --all");
 		removeResult = client.runCommandAndWait(clienttasks.command+" remove --all");
 		Assert.assertEquals(unsubscribeResult.toString(), removeResult.toString(), "Results from 'unsubscribe' and 'remove' module commands should be identical.");
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	// Candidates for an automated Test:
 
 
 	
 	// Data Providers ***********************************************************************
 	
 
 	
 
 }
