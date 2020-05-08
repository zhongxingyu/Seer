 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
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
 			groups={"blockedByBug-584137", "blockedByBug-602852"},
 			dataProvider="getAllConsumedProductSubscriptionsData")
 	@ImplementsNitrateTest(caseId=41688)
 	public void UnsubscribeFromValidProductIDs_Test(ProductSubscription productSubscription){
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 //		sm.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.unsubscribeFromProductSubscription(productSubscription);
 	}
 	
 	
 	@Test(description="Unsubscribe product entitlement and re-subscribe",
 			groups={"blockedByBug-584137", "blockedByBug-602852"},
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
 		if (clienttasks.unsubscribeFromProductSubscription(productSubscription))
 			clienttasks.subscribeToSubscriptionPoolUsingProductId(pool);	// only re-subscribe when unsubscribe was a success
 	}
 	
 	
 	@Test(description="Malicious Test - Unsubscribe and then attempt to reuse the revoked entitlement cert.",
 			groups={"AcceptanceTests","blockedByBug-584137", "blockedByBug-602852", "blockedByBug-672122"},
 			dataProvider="getAvailableSubscriptionPoolsData")
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
 		client.runCommandAndWait("rm -rf "+randDir);
 		client.runCommandAndWait("mkdir -p "+randDir);
 		client.runCommandAndWait("cp "+entitlementCertFile.getPath()+" "+randDir);
 		
 		// unsubscribe from the pool (Note: should be the only one subscribed too
 		clienttasks.unsubscribeFromSerialNumber(clienttasks.getSerialNumberFromEntitlementCertFile(entitlementCertFile));
 		
 		// assert all of the entitlement certs are no longer reported in the "yum repolist all"
 		clienttasks.assertEntitlementCertsInYumRepolist(entitlementCerts,false);
 
 		// restart the rhsm cert deamon
 		int certFrequency = 1;
 		clienttasks.restart_rhsmcertd(certFrequency, false);
 		
 		// move the copied entitlement certificate from /tmp to location /etc/pki/entitlement/product
 		// Note: this is malicious activity
 		client.runCommandAndWait("cp -f "+randDir+"/* "+clienttasks.entitlementCertDir);
 		
 		// assert all of the entitlement certs are reported in the "yum repolist all" again
 		clienttasks.assertEntitlementCertsInYumRepolist(entitlementCerts,true);
 		
 		// assert that the rhsmcertd will clean up the malicious activity
 		log.info("Now let's wait for \"certificates updated\" by the rhsmcertd and assert that the deamon deletes the copied entitlement certificate since it was put on candlepins certificate revocation list during the unsubscribe.");
 		String marker = "Testing UnsubscribeAndAttemptToReuseTheRevokedEntitlementCert_Test..."; // https://tcms.engineering.redhat.com/case/41692/
 		RemoteFileTasks.runCommandAndAssert(client,"echo \""+marker+"\" >> "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0));
 		clienttasks.waitForRegexInRhsmcertdLog(".*certificates updated.*", certFrequency);	// https://bugzilla.redhat.com/show_bug.cgi?id=672122
 
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, entitlementCertFile.getPath())==0,"Entitlement certificate '"+entitlementCertFile+"' was deleted by the rhsm certificate deamon.");
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
 		SSHCommandResult subscribeResult = clienttasks.subscribe(null,pool.poolId,null,null,null,null,null,null,null,null);
 
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
 		// NOTE: this assertion is probably already built into the unsubscribe task above
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, entitlementCertFile.getPath()), 0,"EntitlementCert file has been removed after unsubscribing to future SubscriptionPool.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, entitlementCertKeyFile.getPath()), 0,"EntitlementCert key file has been removed after unsubscribing to future SubscriptionPool.");
 	}
 	
 	
 	// Candidates for an automated Test:
 	// TODO Bug 706889 - “-1” displayed on console while unsubscribe invalid serial
	// TODO Bug 727970 - Unsubscribe Is Not Removing Future Dated Entitlement Certificates Or Keys 
 	
 	// Data Providers ***********************************************************************
 	
 
 	
 
 }
