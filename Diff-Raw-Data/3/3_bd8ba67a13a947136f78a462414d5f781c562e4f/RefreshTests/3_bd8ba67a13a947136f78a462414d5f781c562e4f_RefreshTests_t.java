 package com.redhat.qe.sm.cli.tests;
 
 import java.util.List;
 
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 
 /**
  * @author jsefler
  *
  *
  */
 @Test(groups={"RefreshTests"})
 public class RefreshTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager-cli: refresh and verify entitlements are updated",
 			groups={"RefreshEntitlements_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")	// http://gibson.usersys.redhat.com/agilo/ticket/4022
 	public void RefreshEntitlements_Test() {
 		
 		// Start fresh by unregistering and registering...
 		log.info("Start fresh by unregistering and registering...");
 		clienttasks.unregister(null, null, null);
 		clienttasks.getCurrentConsumerId(clienttasks.register(clientusername,clientpassword,null,null,null,null, null, null, null, null));
 		
 		// make sure the certFrequency will not affect the results of this test
 		log.info("Change the certFrequency to a large value to assure the rhsmcertd does not interfere with this test.");
 		clienttasks.restart_rhsmcertd(60, false);
 		
 		// Subscribe to a randomly available pool...
 		log.info("Subscribe to a randomly available pool...");
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// remember the currently consumed product subscriptions (and entitlement certs)
 		List<ProductSubscription> consumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		List<EntitlementCert> entitlementCerts = clienttasks.getCurrentEntitlementCerts();
 
 		// remove your entitlements
 		log.info("Removing the entitlement certs...");
 		clienttasks.removeAllCerts(false,true);
 		Assert.assertEquals(clienttasks.getCurrentEntitlementCerts().size(),0,"Entitlements have been removed.");
 		Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions().size(),0,"Consumed subscription pools do NOT exist after entitlements have been removed.");
 
 		// refresh
 		log.info("Refresh...");
 		clienttasks.refresh(null, null, null);
 		
 		// Assert the entitlement certs are restored after the refresh
 		log.info("After running refresh, assert that the entitlement certs are restored...");
 		Assert.assertEquals(clienttasks.getCurrentEntitlementCerts(),entitlementCerts,"Original entitlements have been restored.");
 		Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions(),consumedProductSubscriptions,"Original consumed product subscriptions have been restored.");
 	}
 	
 	
 	
	// Candidates for an automated Test:
	// TODO Bug 665118 - Refresh pools will not notice change in provided products 
	
 	
 	
 	// Configuration methods ***********************************************************************
 
 	
 	
 	// Protected methods ***********************************************************************
 
 
 	
 	// Data Providers ***********************************************************************
 
 
 }
