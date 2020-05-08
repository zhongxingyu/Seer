 package com.redhat.qe.sm.cli.tests;
 
 
 import java.util.List;
 
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.SubscriptionPool;
 
 /**
  * @author ssalevan
  *
  */
 @Test(groups={"UnregisterTests"})
 public class UnregisterTests extends SubscriptionManagerCLITestScript {
 	
 	
 	// Test Methods ***********************************************************************
 
 	@Test(description="unregister the consumer",
 			groups={"blockedByBug-589626"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=46714)
 	public void RegisterSubscribeAndUnregisterTest() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, null, null, null);
 		List<SubscriptionPool> availPoolsBeforeSubscribingToAllPools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		clienttasks.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, null, null, null);
 		for (SubscriptionPool afterPool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			SubscriptionPool originalPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", afterPool.poolId, availPoolsBeforeSubscribingToAllPools);
 			Assert.assertEquals(originalPool.quantity, afterPool.quantity,
 				"The subscription quantity count for Pool "+originalPool.poolId+" returned to its original count after subscribing to it and then unregistering from the candlepin server.");
 		}
 	}
 	
 	// Candidates for an automated Test:
 	// TODO Bug 674652 - Subscription Manager Leaves Broken Yum Repos After Unregister
 }
