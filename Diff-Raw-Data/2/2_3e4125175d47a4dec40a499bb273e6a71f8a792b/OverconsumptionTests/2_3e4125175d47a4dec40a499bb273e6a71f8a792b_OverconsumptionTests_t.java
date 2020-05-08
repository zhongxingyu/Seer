 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.LogMessageUtil;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.cli.tasks.SubscriptionManagerTasks;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  *
  */
 
 
 @Test(groups={"OverconsumptionTests"})
 public class OverconsumptionTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager: Basic attempt to oversubscribe the pool quantity",
 			groups={},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void BasicAttemptToOversubscribe_Test() throws JSONException, Exception {
 	
 		// find the pool with the least positive quantity available >= 2
 		client1tasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, registereeName, null, null, Boolean.TRUE, null, null, null);
 		int quantity = 1000000;
 		for (SubscriptionPool pool: client1tasks.getCurrentlyAvailableSubscriptionPools()) {
 			if (pool.quantity.equalsIgnoreCase("unlimited")) continue;
 			int pool_quantity = Integer.valueOf(pool.quantity);
 			if (pool_quantity < quantity && pool_quantity >= 2) {
 				quantity = pool_quantity;
 				testPool = pool;
 			}
 		}
 		client1tasks.unregister(null, null, null);
 		Assert.assertNotNull(testPool, "Found an available pool with a quantity of available subscriptions >= 2.");
 		
 		// consume each quantity available as a new consumer
 		log.info("Now we will register and subscribe new consumers until we exhaust all of the available subscriptions...");
 		systemConsumerIds.clear();
 		for (int i=quantity; i>0; i--) {
 			// register a new system consumer
 			client1tasks.clean(null,null,null);
 			systemConsumerIds.add(client1tasks.getCurrentConsumerId(client1tasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, registereeName, null, null, null, null, null, null)));
 
 			// subscribe to the pool
 			client1tasks.subscribeToSubscriptionPool(testPool);
 		}
 		
 		log.info("Now we will register and subscribe the final subscriber as an attempt to oversubscribe to original pool: "+testPool);
 		client1tasks.clean(null,null,null);
 		systemConsumerIds.add(client1tasks.getCurrentConsumerId(client1tasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, registereeName, null, null, null, null, null, null)));
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 //		Assert.assertNotNull(pool, "Found the test pool amongst --all --available after having consumed all of its available entitlements.");
 		Assert.assertNull(pool, "The test pool is no longer in the --all --available list after having consumed all of its available subscriptions.");
 		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client1tasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNull(pool, "The test pool is no longer in the --available list after having consumed all of its available subscriptions.");
 		
 		// assert the consumed quantity
 		JSONObject jsonTestPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverHostname,sm_serverPort,sm_serverPrefix,sm_clientUsername,sm_clientPassword,"/pools/"+testPool.poolId));
 		Assert.assertEquals(jsonTestPool.getInt("consumed"), jsonTestPool.getInt("quantity"),
 				"Asserting the test pool's consumed attribute matches it's original total quantity after having consumed all of its available entitlements.");
 
 		// now attempt to oversubscribe
 		log.info("Now we will attempt to oversubscribe to original pool: "+testPool);
 		// No free entitlements are available for the pool with id	'8a9b90882df297d5012df31def5e00bb'
 		Assert.assertNull(client1tasks.subscribeToSubscriptionPool(testPool),"No entitlement cert is granted when the pool is already fully subscribed.");
 		// try again
		Assert.assertEquals(client1tasks.subscribe_(null, testPool.poolId, null, null, null, null, null, null, null, null).getStdout().trim(),"No free entitlements are available for the pool with id '"+testPool.poolId+"'.");
 		// assert the consumed quantity again
 		jsonTestPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverHostname,sm_serverPort,sm_serverPrefix,sm_clientUsername,sm_clientPassword,"/pools/"+testPool.poolId));
 		Assert.assertEquals(jsonTestPool.getInt("consumed"), jsonTestPool.getInt("quantity"),
 				"Asserting the test pool's consumed attribute has not overconsumed it's total quantity after attempting a basic overconsumption of its entitlements.");
 
 	}
 	
 	
 	@Test(	description="subscription-manager: Concurrent attempt to subscribe",
 			groups={},
 			dependsOnMethods={"BasicAttemptToOversubscribe_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void ConcurrentAttemptToSubscribe_Test() throws JSONException, Exception {
 		if (client1==null || client2==null) throw new SkipException("This test requires two clients.");
 
 		// reregister the first systemConsumerId and unsubscribe from the test pool
 		client1tasks.clean(null,null,null);
 		client1tasks.register(sm_clientUsername,sm_clientPassword,null,null,registereeName,systemConsumerIds.get(0), null, null, null, null, null);
 		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		// reregister the second systemConsumerId and unsubscribe from the test pool
 		client2tasks.clean(null,null,null);
 		client2tasks.register(sm_clientUsername,sm_clientPassword,null,null,registereeName,systemConsumerIds.get(1), null, null, null, null, null);
 		client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		// assert that each client has no entitlements
 		List<File> client1EntitlementCerts = client1tasks.getCurrentEntitlementCertFiles();
 		Assert.assertEquals(client1EntitlementCerts.size(), 0, "Registered client on '"+client1tasks.hostname+"' should have NO entitlements.");
 		List<File> client2EntitlementCerts = client2tasks.getCurrentEntitlementCertFiles();
 		Assert.assertEquals(client2EntitlementCerts.size(), 0, "Registered client on '"+client2tasks.hostname+"' should have NO entitlements.");
 		
 		// assert that the test pool now has a quantity of 2 available
 		SubscriptionPool pool;
 		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client1tasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool, "Found the test pool after having consumed available subscriptions.");
 		Assert.assertEquals(pool.quantity, "2", "Asserting the test pool quantity after having consumed almost all of its available subscriptions.");
 		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client2tasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool, "Found the test pool after having consumed available subscriptions.");
 		Assert.assertEquals(pool.quantity, "2", "Asserting the test pool quantity after having consumed almost all of its available subscriptions.");
 
 		// attempt to concurrently subscribe to the test pool
 		log.info("Now we will attempt to subscribe both clients concurrently to pool: "+pool);
 		String client1command = String.format("%s subscribe --pool=%s", client1tasks.command, pool.poolId);
 		String client2command = String.format("%s subscribe --pool=%s", client2tasks.command, pool.poolId);
 		client1.runCommand(client1command, LogMessageUtil.action());
 		client2.runCommand(client2command, LogMessageUtil.action());
 		client1.waitForWithTimeout(new Long(10*60*1000)); // timeout after 10 min
 		client2.waitForWithTimeout(new Long(10*60*1000)); // timeout after 10 min
 		SSHCommandResult result1 = client1.getSSHCommandResult();
 		SSHCommandResult result2 = client2.getSSHCommandResult();
 		
 		// assert the results
 		log.info("SSHCommandResult from '"+client1tasks.hostname+"': "+result1);
 //		Assert.assertEquals(result1.getStdout().trim(), "","The lack of information in stdout from the subscribe command on '"+client1tasks.hostname+"' indicates that it successfully subscribed to poolid: "+testPool.poolId);
 		Assert.assertEquals(result1.getStdout().trim(), "Successfully subscribed the system to Pool "+testPool.poolId,"On '"+client1tasks.hostname+"' we successfully subscribed to poolid: "+testPool.poolId);
 		Assert.assertEquals(result1.getStderr().trim(), "","No stderr information is expected on '"+client1tasks.hostname+"'.");
 		Assert.assertEquals(result1.getExitCode(), Integer.valueOf(0),"The exit code from the subscribe command on '"+client1tasks.hostname+"' indicates the subscribe attempt was handled gracefully.");
 		log.info("SSHCommandResult from '"+client2tasks.hostname+"': "+result2);
 //		Assert.assertEquals(result2.getStdout().trim(), "","The lack of information in stdout from the subscribe command on '"+client2tasks.hostname+"' indicates that it successfully subscribed to poolid: "+testPool.poolId);
 		Assert.assertEquals(result2.getStdout().trim(), "Successfully subscribed the system to Pool "+testPool.poolId,"On '"+client2tasks.hostname+"' we successfully subscribed to poolid: "+testPool.poolId);
 		Assert.assertEquals(result2.getStderr().trim(), "","No stderr information is expected on '"+client2tasks.hostname+"'.");
 		Assert.assertEquals(result2.getExitCode(), Integer.valueOf(0),"The exit code from the subscribe command on '"+client2tasks.hostname+"' indicates the subscribe attempt was handled gracefully.");
 		
 		// assert that the test pool has dropped by 2
 //		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 //		Assert.assertNotNull(pool, "Found the test pool amongst --all --available after having consumed all of its available entitlements.");
 //		Assert.assertEquals(pool.quantity, "0", "Asserting the test pool quantity has dropped by 2 due to the concurrent call to subscribe.");
 //		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client2tasks.getCurrentlyAllAvailableSubscriptionPools());
 //		Assert.assertNotNull(pool, "Found the test pool amongst --all --available after having consumed all of its available entitlements.");
 //		Assert.assertEquals(pool.quantity, "0", "Asserting the test pool quantity has dropped by 2 due to the concurrent call to subscribe.");
 		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNull(pool, "The test pool is no longer in the --all --available list after having consumed all of its available subscriptions.");
 		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client2tasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNull(pool, "The test pool is no longer in the --all --available list after having consumed all of its available subscriptions.");
 		JSONObject jsonTestPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverHostname,sm_serverPort,sm_serverPrefix,sm_clientUsername,sm_clientPassword,"/pools/"+testPool.poolId));
 		Assert.assertEquals(jsonTestPool.getInt("consumed"), jsonTestPool.getInt("quantity"), "Asserting the test pool's consumed attribute matches it's original total quantity after having consumed all of its available entitlements.");
 
 		// make sure both clients got individualized entitlement certs
 		client1EntitlementCerts = client1tasks.getCurrentEntitlementCertFiles();
 		Assert.assertEquals(client1EntitlementCerts.size(), 1, "Registered client on '"+client1tasks.hostname+"' should have 1 entitlement from the attempt to subscribe to poolid: "+testPool.poolId);
 		client2EntitlementCerts = client2tasks.getCurrentEntitlementCertFiles();
 		Assert.assertEquals(client2EntitlementCerts.size(), 1, "Registered client on '"+client2tasks.hostname+"' should have 1 entitlement from the attempt to subscribe to poolid: "+testPool.poolId);
 		EntitlementCert client1EntitlementCert = client1tasks.getEntitlementCertFromEntitlementCertFile(client1EntitlementCerts.get(0));
 		EntitlementCert client2EntitlementCert = client2tasks.getEntitlementCertFromEntitlementCertFile(client2EntitlementCerts.get(0));
 		Assert.assertFalse(client1EntitlementCert.serialNumber.equals(client2EntitlementCert.serialNumber), "Asserting that the entitlement serials granted to each of the concurent subscribers are unique to each other.");
 	}
 	
 	
 	@Test(	description="subscription-manager: Concurrent attempt to oversubscribe the pool quantity",
 			groups={"blockedByBug-671195"},
 			dependsOnMethods={"ConcurrentAttemptToSubscribe_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void ConcurrentAttemptToOversubscribe_Test() throws JSONException, Exception {
 		if (client1==null || client2==null) throw new SkipException("This test requires two clients.");
 		
 		// reregister the first systemConsumerId and unsubscribe from the test pool
 		client1tasks.clean(null,null,null);
 		client1tasks.register(sm_clientUsername,sm_clientPassword,null,null,registereeName,systemConsumerIds.get(0), null, null, null, null, null);
 		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 
 		// register from a second client too
 		client2tasks.clean(null,null,null);
 		systemConsumerIds.add(client2tasks.getCurrentConsumerId(client2tasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, registereeName, null, null, null, null, null, null)));
 
 		// assert that the test pool has a quantity of 1 available
 		SubscriptionPool pool;
 		pool= SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client1tasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool, "Found the test pool after having consumed available subscriptions.");
 		Assert.assertEquals(pool.quantity, "1", "Asserting the test pool quantity after having consumed almost all of its available subscriptions.");
 		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client2tasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool, "Found the test pool after having consumed available subscriptions.");
 		Assert.assertEquals(pool.quantity, "1", "Asserting the test pool quantity after having consumed almost all of its available subscriptions.");
 
 		// attempt to concurrently subscribe to the test pool
 		log.info("Now we will attempt to subscribe both clients (only one should succeed) concurrently to pool: "+pool);
 		String client1command = String.format("%s subscribe --pool=%s", client1tasks.command, pool.poolId);
 		String client2command = String.format("%s subscribe --pool=%s", client2tasks.command, pool.poolId);
 		client1.runCommand(client1command, LogMessageUtil.action());
 		client2.runCommand(client2command, LogMessageUtil.action());
 		client1.waitForWithTimeout(new Long(10*60*1000)); // timeout after 10 min
 		client2.waitForWithTimeout(new Long(10*60*1000)); // timeout after 10 min
 		SSHCommandResult result1 = client1.getSSHCommandResult();
 		SSHCommandResult result2 = client2.getSSHCommandResult();
 		
 		// assert that the test pool does NOT fall below zero
 //		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 //		Assert.assertNotNull(pool, "Found the test pool amongst --all --available after having consumed all of its available entitlements.");
 //		Assert.assertEquals(pool.quantity, "0", "Asserting the test pool quantity does not fall below zero after attempting a concurrent subscribe.");
 //		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client2tasks.getCurrentlyAllAvailableSubscriptionPools());
 //		Assert.assertNotNull(pool, "Found the test pool amongst --all --available after having consumed all of its available entitlements.");
 //		Assert.assertEquals(pool.quantity, "0", "Asserting the test pool quantity does not fall below zero after attempting a concurrent subscribe.");
 		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNull(pool, "The test pool is no longer in the --all --available list after having consumed all of its available subscriptions.");
 		pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", testPool.poolId, client2tasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNull(pool, "The test pool is no longer in the --all --available list after having consumed all of its available subscriptions.");
 		JSONObject jsonTestPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverHostname,sm_serverPort,sm_serverPrefix,sm_clientUsername,sm_clientPassword,"/pools/"+testPool.poolId));
 		Assert.assertEquals(jsonTestPool.getInt("consumed"), jsonTestPool.getInt("quantity"), "Asserting the test pool's consumed attribute matches it's original total quantity after having consumed all of its available entitlements.");
 		
 		// one of these command should have succeeded and one should have failed with "No free entitlements..."
 		// decide who was the winner and who must have been the loser
 		SSHCommandResult sshWinner, sshLoser;
 		SubscriptionManagerTasks smtWinner, smtLoser;
 //		if (result1.getStdout().equals("")) {	// client1 appears to be the winner, therefore client2 must be the loser
 		if (result1.getStdout().startsWith("Success")) {	// client1 appears to be the winner, therefore client2 must be the loser
 			sshWinner =	result1;
 			smtWinner = client1tasks;
 			sshLoser =	result2;
 			smtLoser =	client2tasks;
 		} else {	// client2 must be the winner and client1 is the loser
 			sshWinner =	result2;
 			smtWinner = client2tasks;
 			sshLoser =	result1;
 			smtLoser =	client1tasks;
 		}
 		
 		// assert the Winner and Loser
 		log.info("SSHCommandResult from '"+smtWinner.hostname+"': "+sshWinner);
 //		Assert.assertEquals(sshWinner.getStdout().trim(), "","The lack of information in stdout from the subscribe command on '"+smtWinner.hostname+"' indicates that it won the subscribe race to the subscription pool's final entitlement.");
 		Assert.assertEquals(sshWinner.getStdout().trim(), "Successfully subscribed the system to Pool "+testPool.poolId,"On '"+smtWinner.hostname+"' we successfully subscribed to poolid '"+testPool.poolId+"' indicating that it won the subscribe race to the subscription pool's final entitlement.");
 		Assert.assertEquals(sshWinner.getStderr().trim(), "","No stderr information is expected on '"+smtWinner.hostname+"'.");
 		Assert.assertEquals(sshWinner.getExitCode(), Integer.valueOf(0),"The exit code from the subscribe command on '"+smtWinner.hostname+"' indicates the subscribe attempt was handled gracefully.");
 		log.info("SSHCommandResult from '"+smtLoser.hostname+"': "+sshLoser);
 		Assert.assertEquals(sshLoser.getStdout().trim(), "No free entitlements are available for the pool with id '"+testPool.poolId+"'", "Stdout must indicate to system '"+smtLoser.hostname+"' that there are no free entitlements left from poolId '"+testPool.poolId+"'.");
 		Assert.assertEquals(sshLoser.getStderr().trim(), "","No stderr information is expected on '"+smtLoser.hostname+"'.");
 		Assert.assertEquals(sshLoser.getExitCode(), Integer.valueOf(0),"The exit code from the subscribe command on '"+smtLoser.hostname+"' indicates the subscribe attempt was handled gracefully.");
 	}
 
 	
 	
 	
 	// Candidates for an automated Test:
 	
 	
 	
 	
 	
 	
 	
 	// Protected Class Variables ***********************************************************************
 	
 	protected List<String> systemConsumerIds = new ArrayList<String>();
 	protected SubscriptionPool testPool = null;
 	protected final String registereeName = "Overconsumer";
 	
 	
 	
 	// Protected Methods ***********************************************************************
 
 	
 	
 
 	
 	// Configuration Methods ***********************************************************************
 
 	@AfterClass(groups={"setup"},alwaysRun=true)
 	public void unsubscribeAndUnregisterMultipleSystemsAfterClass() {
 		if (client2tasks!=null) {
 			client2tasks.unsubscribe_(Boolean.TRUE,null, null, null, null);
 			client2tasks.unregister_(null, null, null);
 		}
 
 		if (client1tasks!=null) {
 			
 			for (String systemConsumerId : systemConsumerIds) {
 				client1tasks.register_(sm_clientUsername,sm_clientPassword,null,null,null,systemConsumerId, null, Boolean.TRUE, null, null, null);
 				client1tasks.unsubscribe_(Boolean.TRUE, null, null, null, null);
 				client1tasks.unregister_(null, null, null);
 			}
 			systemConsumerIds.clear();
 		}
 	}
 	
 	
 	
 	// Data Providers ***********************************************************************
 
 	
 
 }
 
