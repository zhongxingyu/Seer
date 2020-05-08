 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /* Notes...
 Data prep
 ==========================================================
 (in candlepin/client/ruby - assuming that the cp_product_utils product data has been imported)
 
 ./cpc create_owner john
 ./cpc create_user 10 john password   (10 happened to be the id returned from creating the john owner)
 ./cpc create_subscription 10 RH09XYU34  (defaults to a quantity of 1)
 ./cpc refresh_pools john
 
 RHSM
 ===========================================================
 # Simulating a person accepting RHEL Personal
 sudo ./subscription-manager-cli register --username=john --password=password --type=person
 sudo ./subscription-manager-cli list --available     # (Should see RHEL Personal)
 sudo ./subscription-manager-cli subscribe --pool=13  # (or whatever the pool id is for RHEL Personal)
 
 # AT THIS POINT YOU WILL NOT HAVE ANY ENTITLEMENT CERTS
 # THIS DOES NOT MATTER, AS THIS IS NOT HOW A RHEL PERSONAL CUSTOMER WILL ACTUALLY CONSUME THIS ENTITLEMENT
 
 # Stash the consumer certs - this really just simulates using a different machine
 sudo mv /etc/pki/consumer/*.pem /tmp
 
 # Now register a system consumer under john's username
 sudo ./subscription-manager-cli register --username=john --password=password
 sudo ./subscription-manager-cli list --available    # (Should see RHEL Personal Bits - this product actually should create a real entitlement cert and allow this machine access to the actual content, hence the silly name)
 
 sudo ./subscription-manager-cli subscribe --pool=14  # (RHEL Personal Bits pool)
 sudo ./subscription-manager-cli list                 # (This should actually show entitlement cert information)
 
 I don't think that we have valid content data currently baked into the certs, so I don't think that you can use this to access protected yum repos yet.
 
 
 
 EMAIL FROM dgoodwin@redhat.com:
 Sub-pools are something we refer to related to Red Hat Personal.
 
 Customers register as what we call "person consumers" with candlepin
 through this Kingpin application.
 
 They will then see a Red Hat Personal subscription and be able to bind
 (request an entitlement). This is only for "person consumers", not systems.
 
 Once granted, this entitlement results in a sub-pool being created (of
 unlimited size) which they can then subscribe their systems to. It is
 tied to an entitlement that created it.
 
 When the person consumer's entitlement is removed, that sub-pool needs
 to be cleaned up, including all outstanding entitlements it has given out.
 
 Hope that was clear, it is certainly not the simplest thing in the world. :)
 
 Devan
 
 
 EMAIL FROM jharris@redhat.com
 Subpools are currently something that is currently pretty specific to RH Personal,
 but I will try and explain it generally first...
 
 In most cases, a pool is a 1-to-1 match with a subscription - so a subscription with 
 quantity 20 for "Super Cool Linux" gives you a pool with the same quantity and product(s).  
 There are special cases where the act of consuming an entitlement from one pool actually 
 spins off a new pool as a result.  A case for this might be "Developer Tools" where a 
 subscription for a 10 person license is purchased (quantity 10), and when someone consumes 
 an entitlement from that pool, a new sub-pool is created specifically for that user.  
 Each system that this user installs the product on pulls from this sub-pool, and when this 
 user gives up his/her seat (unbinds from the original pool), the sub-pool and all of its 
 entitlements are removed, which means that any systems that that "Developer Tools" installed 
 by this user are no longer in compliance.
 
 We are using this same construct to model the RH Personal case, where the "person" consumes 
 an entitlement for RHEL Personal, and any systems that want to install RHEL on are entitled 
 off of the created sub-pool.
 
 I'm not really sure if this helps any, but here is the original design doc:  
 https://engineering.redhat.com/trac/Entitlement/wiki/RHPersonalDevTools
 
  - Justin
  */
 
 /**
  * @author jsefler
  *
  *
  *REFERENCE MATERIAL:
  * https://tcms.engineering.redhat.com/case/55702/
  * https://tcms.engineering.redhat.com/case/55718/
  * https://engineering.redhat.com/trac/IntegratedMgmtQE/wiki/RH-Personal_dev_testplan
  * https://engineering.redhat.com/trac/Entitlement/wiki/RHPersonalDevTools
  */
 @Test(groups={"rhelPersonal"})
 public class RHELPersonalTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager-cli: Ensure RHEL Personal Bits are available and unlimited after a person has subscribed to RHEL Personal",
 			groups={"EnsureSubPoolIsAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test", "RHELPersonal", "blockedByBug-624816", "blockedByBug-641155", "blockedByBug-643405"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=55702)
 //	@ImplementsNitrateTest(caseId={55702,55718})
 	public void EnsureSubPoolIsAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test() throws JSONException {
 //		if (!isServerOnPremises) throw new SkipException("Currently this test is designed only for on-premises.");	//TODO Make this work for IT too.  jsefler 8/12/2010 
 		if (client2tasks==null) throw new SkipException("These tests are designed to use a second client.");
 		if (username.equals(serverAdminUsername)) throw new SkipException("This test requires that the client user ("+username+") is NOT "+serverAdminUsername);
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=624423 - jsefler 8/16/2010
 		Boolean invokeWorkaroundWhileBugIsOpen = false;
 		try {String bugId="624423"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			servertasks.restartTomcat();
 		} // END OF WORKAROUND
 		
 		client2tasks.unregister(null, null, null);
 		client1tasks.unregister(null, null, null);	// just in case client1 is still registered as the person consumer
 		
 		for (int j=0; j<personSubscriptionPoolProductData.length(); j++) {
 			JSONObject poolProductDataAsJSONObject = (JSONObject) personSubscriptionPoolProductData.get(j);
 			String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 			JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 			String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 			
 			SubscriptionPool pool = null;
 			
 			log.info("Register client2 under username '"+username+"' as a system and assert that ProductId '"+systemProductId+"' is NOT yet available...");
 
 			client2tasks.register(username, password, ConsumerType.system, null, null, null, null, null, null, null);
 			List<SubscriptionPool> client2BeforeSubscriptionPools = client2tasks.getCurrentlyAvailableSubscriptionPools();
 			pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",systemProductId,client2BeforeSubscriptionPools);
 			Assert.assertNull(pool,"ProductId '"+systemProductId+"' is NOT yet available to client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+username+"'.");
 	
 			
 	//		log.info("Now register client1 under username '"+consumerUsername+"' as a person and subscribe to the '"+personSubscriptionName+"' subscription pool...");
 			log.info("Now register client1 under username '"+username+"' as a person and subscribe to the personal subscription pool with ProductId '"+personProductId+"'...");
 			//client1tasks.unregister(null, null, null);
 			client1tasks.register(username, password, ConsumerType.person, null, null, null, null, null, null, null);
 			personConsumerId = client1tasks.getCurrentConsumerId();
 	//		pool = client1tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",personSubscriptionName,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 			pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",personProductId,client1tasks.getCurrentlyAvailableSubscriptionPools());
 			personSubscriptionName = pool.subscriptionName;
 			Assert.assertNotNull(pool,"Personal Subscription with ProductId '"+personProductId+"' is available to user '"+username+"' registered as a person.");
 			client1tasks.subscribeToSubscriptionPool(pool);
 			
 			
 			log.info("Now client2 (already registered as a system under username '"+username+"') should now have ProductId '"+systemProductId+"' available with a quantity if '"+systemSubscriptionQuantity+"'...");
 			List<SubscriptionPool> client2AfterSubscriptionPools = client2tasks.getCurrentlyAvailableSubscriptionPools();
 			SubscriptionPool systemSubscriptionPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",systemProductId,client2AfterSubscriptionPools);
 			Assert.assertNotNull(systemSubscriptionPool,"ProductId '"+systemProductId+"' is now available to client2 '"+client2.getConnection().getHostname()+"' (registered as a system under username '"+username+"')");
 			Assert.assertEquals(systemSubscriptionPool.quantity.toLowerCase(),systemSubscriptionQuantity,"A quantity of '"+systemSubscriptionQuantity+"' entitlements is available to the subscription for "+systemProductId+".");
 			
 			
 			log.info("Verifying that the available subscription pools available to client2 has increased by only the '"+systemProductId+"' pool...");
 			Assert.assertTrue(
 					client2AfterSubscriptionPools.containsAll(client2BeforeSubscriptionPools) &&
 					client2AfterSubscriptionPools.contains(systemSubscriptionPool) &&
 					client2AfterSubscriptionPools.size()==client2BeforeSubscriptionPools.size()+1,
 					"The list of available subscription pools seen by client2 increases only by '"+systemProductId+"' pool: "+systemSubscriptionPool);
 		}
 	}
 	 
 	@Test(	description="subscription-manager-cli: Ensure RHEL Personal Bits are consumable after a person has subscribed to RHEL Personal",
 			groups={"EnsureSubPoolIsConsumableAfterRegisteredPersonSubscribesToRHELPersonal_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureSubPoolIsAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=55702)
 //	@ImplementsNitrateTest(caseId={55702,55718})
 	public void EnsureSubPoolIsConsumableAfterRegisteredPersonSubscribesToRHELPersonal_Test() throws JSONException {
 				
 		for (int j=0; j<personSubscriptionPoolProductData.length(); j++) {
 			JSONObject poolProductDataAsJSONObject = (JSONObject) personSubscriptionPoolProductData.get(j);
 			String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 			JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 			String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 			JSONArray bundledProductData = subpoolProductDataAsJSONObject.getJSONArray("bundledProductData");
 			
 			log.info("Now client2 (already registered as a system under username '"+username+"') can now consume '"+systemProductId+"'...");
 			SubscriptionPool systemSubscriptionPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",systemProductId,client2tasks.getCurrentlyAvailableSubscriptionPools());
 			client2tasks.subscribeToSubscriptionPool(systemSubscriptionPool);
 			
 			for (int k=0; k<bundledProductData.length(); k++) {
 				JSONObject bundledProductAsJSONObject = (JSONObject) bundledProductData.get(k);
 				String systemConsumedProductName = bundledProductAsJSONObject.getString("productName");
 
 				log.info("Now client2 should be consuming the product '"+systemConsumedProductName+"'...");
 				ProductSubscription systemProductSubscription = ProductSubscription.findFirstInstanceWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 				Assert.assertNotNull(systemProductSubscription,systemConsumedProductName+" is now consumed on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+username+"'.");
 			}
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure that availability of RHEL Personal Bits is revoked once the person unsubscribes from RHEL Personal",
 			groups={"EnsureAvailabilityOfSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureSubPoolIsConsumableAfterRegisteredPersonSubscribesToRHELPersonal_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void EnsureAvailabilityOfSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test() throws JSONException {
 		
 		log.info("Unsubscribe client2 (already registered as a system under username '"+username+"') from all currently consumed product subscriptions...");
 		client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		
 		log.info("Unsubscribe client1 (already registered as a person under username '"+username+"') from all currently consumed person subscriptions...");
 		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 
 		
 		for (int j=0; j<personSubscriptionPoolProductData.length(); j++) {
 			JSONObject poolProductDataAsJSONObject = (JSONObject) personSubscriptionPoolProductData.get(j);
 			String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 			JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 			String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 
 			log.info("Now verify that client2 (already registered as a system under username '"+username+"') can no longer subscribe to the '"+systemProductId+"' pool...");
 			SubscriptionPool systemSubscriptionPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",systemProductId,client2tasks.getCurrentlyAvailableSubscriptionPools());
 			Assert.assertNull(systemSubscriptionPool,"ProductId '"+systemProductId+"' is no longer available on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+username+"'.");
 		}
 	}
 	
 
 	
 	@Test(	description="subscription-manager-cli: Ensure that multiple (unlimited) systems can subscribe to subpool",
 			groups={"SubscribeMultipleSystemsToSubPool_Test","RHELPersonal"/*, "blockedByBug-661130"*/},
 			dependsOnGroups={"EnsureAvailabilityOfSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void SubscribeMultipleSystemsToSubPool_Test() throws JSONException {
 		log.info("Making sure the clients are not subscribed to anything...");
 //		client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		client2tasks.unregister();
 //		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		unsubscribeAndUnregisterMultipleSystemsAfterGroups();
 		client1tasks.register(username, password, ConsumerType.person, /*"blockedByBug-661130" "ME"*/null, null, null, null, null, null, null);
 		personConsumerId = client1tasks.getCurrentConsumerId();
 
 		for (int j=0; j<personSubscriptionPoolProductData.length(); j++) {
 			JSONObject poolProductDataAsJSONObject = (JSONObject) personSubscriptionPoolProductData.get(j);
 			String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 			JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 			String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 			JSONArray bundledProductData = subpoolProductDataAsJSONObject.getJSONArray("bundledProductData");
 			
 			log.info("Subscribe client1 (already registered as a person under username '"+username+"') to subscription pool with ProductId'"+personProductId+"'...");
 			personSubscriptionPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",personProductId,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 			Assert.assertNotNull(personSubscriptionPool,"Personal subscription with ProductId '"+personProductId+"' is available to user '"+username+"' registered as a person.");
 			//client1tasks.subscribe(personSubscriptionPool.poolId, null, null, null, null);
 			//personalEntitlementCert = client1tasks.getEntitlementCertFromEntitlementCertFile(client1tasks.subscribeToSubscriptionPool(personSubscriptionPool));
 			personEntitlementCert = client1tasks.getEntitlementCertFromEntitlementCertFile(client1tasks.subscribeToSubscriptionPool(personSubscriptionPool));
 	
 			log.info("Register "+multipleSystems+" new systems under username '"+username+"' and subscribe to sub productId '"+systemProductId+"'...");
 			systemConsumerIds = new ArrayList<String>();
 			for (int systemNum = 1; systemNum <=multipleSystems; systemNum++) {
 				// simulate a clean system
 				client2tasks.removeAllCerts(true,true);
 				
 				String consumerId = client2tasks.getCurrentConsumerId(client2tasks.register(username, password, ConsumerType.system, null, null, null, Boolean.TRUE, null, null, null));
 				systemConsumerIds.add(consumerId);
 				SubscriptionPool subPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",systemProductId,client2tasks.getCurrentlyAvailableSubscriptionPools());
 				log.info("Subscribing system '"+systemNum+"' ('"+consumerId+"' under username '"+username+"') to sub pool for productId '"+systemProductId+"'...");
 				client2tasks.subscribeToSubscriptionPoolUsingPoolId(subPool);
 				
 				for (int k=0; k<bundledProductData.length(); k++) {
 					JSONObject bundledProductAsJSONObject = (JSONObject) bundledProductData.get(k);
 					String systemConsumedProductName = bundledProductAsJSONObject.getString("productName");
 
 					ProductSubscription productSubscription = ProductSubscription.findFirstInstanceWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 					Assert.assertNotNull(productSubscription,systemConsumedProductName+" is now consumed by consumer '"+consumerId+"' (registered as a system under username '"+username+"')");
 				}
 			}
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure person consumer cannot unsubscribe while subpools are consumed",
 			groups={"EnsurePersonCannotUnsubscribeWhileSubpoolsAreConsumed_Test","RHELPersonal", "blockedByBug-624063", "blockedByBug-639434", "blockedByBug-658283", "blockedByBug-658683", "blockedByBug-675473"},
 			dependsOnGroups={"SubscribeMultipleSystemsToSubPool_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=58898)
 	// 1) unsubscribe person from personal pool while systems are subscribed to subpool (scenario from calfanso@redhat.com)
 	public void EnsurePersonCannotUnsubscribeWhileSubpoolsAreConsumed_Test() {
 		log.info("Assuming that multiple systems have subscribed to a personal subpool in prior testcase...");
 	
 		log.info("Now, attempt to unsubscribe the person on client 1 from the "+personSubscriptionName+" pool and assert the unsubscribe is blocked.");
 		SSHCommandResult result = client1tasks.unsubscribe_(Boolean.TRUE,null, null, null, null);
 
 		/*
 		-Cannot unsubscribe entitlement {0} because:
 			  system consumer {1} with id {2} has he following entitlements:
 			    Entitlement {0}:
 			        account number: {0}
 			        serial number: {0}
 			  system consumer {1} with id {2} has he following entitlements:
 			    Entitlement {0}:
 			        account number: {0}
 			        serial number: {0}
 			  system consumer {1} with id {2} has he following entitlements:
 			    Entitlement {0}:
 			        account number: {0}
 			        serial number: {0}
 			  system consumer {1} with id {2} has he following entitlements:
 			    Entitlement {0}:
 			        account number: {0}
 			        serial number: {0}
 
 			The above entitlements were derived from the pool: {0}.
 			Please unsubscribe from the above entitlements first.
 			
 			
 			// AFTER BUG FIX 658683
 			Cannot unregister person consumer testuser1 because: 
 			-Cannot unsubscribe entitlement '8a90f8b42e349f20012e34a76157020e' because:
   			  system consumer 'jsefler-onprem04.usersys.redhat.com' with id '187546b3-4d90-4d43-b689-a4e6c09fae04' has the following entitlements:
     			Entitlement '8a90f8b42e349f20012e34a872ab0214':
         			account number: '12331131231'
         			serial number: '521,297,963,578,060,232'
 
 			These consumed entitlements were derived from subscription pool: '8a90f8b42e349f20012e349f9cb40149'.
 			You must first unsubscribe these consumers from these entitlements.
 
 		*/
 		
 		Assert.assertEquals(result.getExitCode(),new Integer(255),
 				"Attempt to unsubscribe throws a failing exit code."); // exitCode: 255
 		//Assert.assertTrue(result.getStderr().startsWith("Cannot unbind due to outstanding entitlement:"),
 		//		"Attempting to unsubscribe the person consumer from all pools is blocked when another system registered by the same consumer is consuming from a subpool."); // stderr: Cannot unregister due to outstanding entitlement: 9
 		//Assert.assertContainsMatch(result.getStderr(),"Cannot unbind due to outstanding sub-pool entitlements in [a-f,0-9]{32}",
 		//		"Attempting to unsubscribe the person consumer from all pools is blocked when another system registered with the same username is consuming from a subpool."); // stderr: Cannot unbind due to outstanding sub-pool entitlements in ff8080812c9942fa012c994cf1da02a1
 		Assert.assertContainsMatch(result.getStderr(),"-Cannot unsubscribe entitlement '"+personEntitlementCert.id+"' because:",
 				"Attempting to unsubscribe the person consumer from all pools is blocked when another system registered with the same username is consuming from a subpool."); // stdout: -Cannot unsubscribe entitlement ff8080812c9942fa012c994cf1da02a1 because:
		//Assert.assertContainsMatch(result.getStderr(),"The above entitlements were derived from the pool: '"+personSubscriptionPool.poolId+"'.",
 		//		"Attempting to unsubscribe the person consumer from all pools is blocked when another system registered with the same username is consuming from a subpool.");
 		//Assert.assertContainsMatch(result.getStderr(),"Please unsubscribe from the above entitlements first.",
 		//		"Attempting to unsubscribe the person consumer from all pools is blocked when another system registered with the same username is consuming from a subpool.");
 		Assert.assertContainsMatch(result.getStderr(),"These consumed entitlements were derived from subscription pool: '"+personSubscriptionPool.poolId+"'.",
 				"Attempting to unsubscribe the person consumer from all pools is blocked when another system registered with the same username is consuming from a subpool.");
 		Assert.assertContainsMatch(result.getStderr(),"You must first unsubscribe these consumers from these entitlements.",
 				"Attempting to unsubscribe the person consumer from all pools is blocked when another system registered with the same username is consuming from a subpool.");
 
 		// TODO include loop to assert all the system consumer information in the stderr message
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure person consumer cannot unregister while subpools are consumed",
 			groups={"EnsurePersonCannotUnregisterWhileSubpoolsAreConsumed_Test","RHELPersonal", "blockedByBug-624063", "blockedByBug-639434", "blockedByBug-658683", "blockedByBug-661130"},
 			dependsOnGroups={"SubscribeMultipleSystemsToSubPool_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void EnsurePersonCannotUnregisterWhileSubpoolsAreConsumed_Test() {
 		log.info("Assuming that multiple systems have subscribed to a personal subpool in prior testcase...");
 	
 		log.info("Now, attempt to unregister the person on client 1 from the "+personSubscriptionName+" pool and assert the unregister is blocked.");
 		SSHCommandResult result = client1tasks.unregister_(null, null, null);
 
 		/*
 		Cannot unregister person consumer jsefler-qabetaperson-2 because:
 			-Cannot unsubscribe entitlement {0} because:
 			  system consumer {1} with id {2} has he following entitlements:
 			    Entitlement {0}:
 			        account number: {0}
 			        serial number: {0}
 			  system consumer {1} with id {2} has he following entitlements:
 			    Entitlement {0}:
 			        account number: {0}
 			        serial number: {0}
 			  system consumer {1} with id {2} has he following entitlements:
 			    Entitlement {0}:
 			        account number: {0}
 			        serial number: {0}
 			  system consumer {1} with id {2} has he following entitlements:
 			    Entitlement {0}:
 			        account number: {0}
 			        serial number: {0}
 
 			The above entitlements were derived from the pool: {0}.
 			Please unsubscribe from the above entitlements first.
 			
 			// AFTER BUG FIX 658683
 			Cannot unregister person consumer testuser1 because: 
 			-Cannot unsubscribe entitlement '8a90f8b42e349f20012e34a76157020e' because:
   			  system consumer 'jsefler-onprem04.usersys.redhat.com' with id '187546b3-4d90-4d43-b689-a4e6c09fae04' has the following entitlements:
     			Entitlement '8a90f8b42e349f20012e34a872ab0214':
         			account number: '12331131231'
         			serial number: '521,297,963,578,060,232'
 
 			These consumed entitlements were derived from subscription pool: '8a90f8b42e349f20012e349f9cb40149'.
 			You must first unsubscribe these consumers from these entitlements.
 
 		*/
 		
 		Assert.assertEquals(result.getExitCode(),new Integer(255),
 				"Attempt to unregister throws a failing exit code."); // exitCode: 255
 		//Assert.assertTrue(result.getStderr().startsWith("Cannot unregister due to outstanding entitlement:"),
 		//		"Attempting to unregister the person consumer is blocked when another system is register by the same consumer is consuming from a subpool."); // stderr: Cannot unregister due to outstanding entitlement: 9
 		//Assert.assertContainsMatch(result.getStdout(),"Cannot unregister due to outstanding sub-pool entitlements in [a-f,0-9]{32}",
 		//		"Attempting to unregister the person consumer is blocked when another system registered with the same username is consuming from a subpool."); // stdout: Cannot unregister due to outstanding sub-pool entitlements in ff8080812c9942fa012c994cf1da02a1
 		//Assert.assertContainsMatch(result.getStdout(),"^Cannot unregister consumer '"+/*"blockedByBug-661130" "ME" */username+"'",
 		//		"Attempting to unregister the person consumer is blocked when another system registered with the same username is consuming from a subpool."); // stdout: Cannot unregister consumer 'testuser1' because:
 		Assert.assertContainsMatch(result.getStdout(),"^Cannot unregister person consumer "+username+" because:",
 				"Attempting to unregister the person consumer is blocked when another system registered with the same username is consuming from a subpool."); // stdout: Cannot unregister person consumer jsefler-qabetaperson-2 because:
 	}
 	
 	
 // DUE TO BEHAVIOR CHNAGE, THIS TEST WAS REPLACED BY EnsurePersonCannotUnsubscribeWhileSubpoolsAreConsumed_Test
 //	@Test(	description="subscription-manager-cli: Ensure that the entitlement certs for subscribed subpool is revoked once the person unsubscribes from RHEL Personal",
 //			groups={"EnsureEntitlementCertForSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test","RHELPersonal","blockedByBug-639434"},
 //			dependsOnGroups={"SubscribeMultipleSystemsToSubPool_Test","EnsurePersonCannotUnsubscribeWhileSubpoolsAreConsumed_Test","EnsurePersonCannotUnregisterWhileSubpoolsAreConsumed_Test"},
 ////			dataProvider="getRHELPersonalData",
 //			enabled=true)
 //	@ImplementsNitrateTest(caseId=58898)
 //	// 1) unsubscribe person from personal pool while systems are subscribed to subpool (scenario from calfanso@redhat.com)
 //	public void EnsureEntitlementCertForSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test(/*String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName*/) {
 //		log.info("Assuming that multiple systems have subscribed to subpool '"+systemSubscriptionName+"' in prior testcase...");
 //	
 //		log.info("Now, unsubscribe the person on client 1 from the '"+personSubscriptionName+"' and assert that the '"+systemConsumedProductName+"' and '"+systemSubscriptionName+"' gets revoked from the system consumers.");
 //		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		
 //		log.info("Now the the certs for '"+systemConsumedProductName+"' and '"+systemSubscriptionName+"' should be revoked from the system consumers...");
 //		for (String consumerId : consumerIds) {
 //			//client2tasks.reregister(consumerUsername,consumerPassword,consumerId);
 //			client2tasks.reregisterToExistingConsumer(consumerUsername,consumerPassword,consumerId);
 //			// 10/11/2010 NOT NEEDED SINCE register --consumerid NOW REFRESHES CERTS			client2tasks.restart_rhsmcertd(1, true);	// give rhsmcertd a chance to download the consumer's certs
 //			ProductSubscription productSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 //			Assert.assertTrue(productSubscription==null,systemConsumedProductName+" is no longer consumed by '"+consumerId+"' (registered as a system under username '"+consumerUsername+"')");
 //			SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 //			Assert.assertTrue(systemSubscriptionPool==null,systemSubscriptionName+" is no longer available to consumer '"+consumerId+"' (registered as a system under username '"+consumerUsername+"')");
 //		}
 //	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure that unsubscribing system from subpool while other systems are subscribed to subpool does not cause subpool to go away",
 			groups={"EnsureEntitlementCertForSubPoolIsNotRevokedOnceAnotherSystemUnsubscribesFromSubPool_Test","RHELPersonal", "blockedByBug-643405"},
 //			dependsOnGroups={"EnsureEntitlementCertForSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test"},
 			dependsOnGroups={"SubscribeMultipleSystemsToSubPool_Test","EnsurePersonCannotUnsubscribeWhileSubpoolsAreConsumed_Test","EnsurePersonCannotUnregisterWhileSubpoolsAreConsumed_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=58899)
 	// 2) unsubscribe system from subpool while other systems are subscribed to subpool, make sure the subpool doesn't go away (scenario from calfanso@redhat.com)
 	public void EnsureEntitlementCertForSubPoolIsNotRevokedOnceAnotherSystemUnsubscribesFromSubPool_Test() throws JSONException {
 		
 		log.info("Now start unsubscribing each system from the consumed product(s) and assert the personal sub pool is still available...");
 		for (String consumerId : systemConsumerIds) {
 			//client2tasks.reregister(consumerUsername,consumerPassword,consumerId);
 			client2tasks.reregisterToExistingConsumer(username,password,consumerId);
 			// 10/11/2010 NOT NEEDED SINCE register --consumerid NOW REFRESHES CERTS			client2tasks.restart_rhsmcertd(1, true);	// give rhsmcertd a chance to download the consumer's certs
 			
 			for (int j=0; j<personSubscriptionPoolProductData.length(); j++) {
 				JSONObject poolProductDataAsJSONObject = (JSONObject) personSubscriptionPoolProductData.get(j);
 				String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 				JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 				String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 				JSONArray bundledProductData = subpoolProductDataAsJSONObject.getJSONArray("bundledProductData");
 			
 				String systemConsumedProductName = null;
 				for (int k=0; k<bundledProductData.length(); k++) {
 					JSONObject bundledProductAsJSONObject = (JSONObject) bundledProductData.get(k);
 					systemConsumedProductName = bundledProductAsJSONObject.getString("productName");
 				}
 				
 				ProductSubscription productSubscription = ProductSubscription.findFirstInstanceWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 				client2tasks.unsubscribeFromProductSubscription(productSubscription);
 				SubscriptionPool systemSubscriptionPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",systemProductId,client2tasks.getCurrentlyAvailableSubscriptionPools());
 				Assert.assertNotNull(systemSubscriptionPool,"Subscription to ProductId '"+systemProductId+"' is once again available to consumer '"+consumerId+"' (registered as a system under username '"+username+"')");
 			}
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure that after unsubscribing all systems from a subpool, the subpool should not get deleted",
 			groups={"EnsureSubPoolIsNotDeletedAfterAllOtherSystemsUnsubscribeFromSubPool_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureEntitlementCertForSubPoolIsNotRevokedOnceAnotherSystemUnsubscribesFromSubPool_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=58907)
 	// 3) unsubscribe system from subpool as the last system subscribed, make sure the subpool doesn't get deleted (scenario from calfanso@redhat.com)
 	public void EnsureSubPoolIsNotDeletedAfterAllOtherSystemsUnsubscribeFromSubPool_Test() throws JSONException {
 		log.info("After having unsubscribed all systems from product subscriptions in the prior testcase , we will now verify that the personal subpool has not been deleted and that all systems can still subscribe to it ...");
 
 		for (String consumerId : systemConsumerIds) {
 			//client2tasks.reregister(consumerUsername,consumerPassword,consumerId);
 			client2tasks.reregisterToExistingConsumer(username,password,consumerId);
 			// 10/11/2010 NOT NEEDED SINCE register --consumerid NOW REFRESHES CERTS			client2tasks.restart_rhsmcertd(1, true);	// give rhsmcertd a chance to download the consumer's certs
 			
 			for (int j=0; j<personSubscriptionPoolProductData.length(); j++) {
 				JSONObject poolProductDataAsJSONObject = (JSONObject) personSubscriptionPoolProductData.get(j);
 				String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 				JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 				String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 				JSONArray bundledProductData = subpoolProductDataAsJSONObject.getJSONArray("bundledProductData");
 			
 				for (int k=0; k<bundledProductData.length(); k++) {
 					JSONObject bundledProductAsJSONObject = (JSONObject) bundledProductData.get(k);
 					String systemConsumedProductName = bundledProductAsJSONObject.getString("productName");
 
 					ProductSubscription productSubscription = ProductSubscription.findFirstInstanceWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 					Assert.assertNull(productSubscription,systemConsumedProductName+" is not consumed by consumer '"+consumerId+"' (registered as a system under username '"+username+"')");
 				}
 				SubscriptionPool systemSubscriptionPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",systemProductId,client2tasks.getCurrentlyAvailableSubscriptionPools());
 				Assert.assertNotNull(systemSubscriptionPool,"ProductId '"+systemProductId+"' is still available to consumer '"+consumerId+"' (registered as a system under username '"+username+"')");
 			}
 		}
 		
 		log.info("Now that all the subscribers of the personal subpool products have been unsubscribed, the person consumer should be able to unregister without being blocked due to outstanding entitlements...");
 		client1tasks.unregister(null, null, null);		
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure system autosubscribe consumes subpool RHEL Personal Bits",
 			groups={"EnsureSystemAutosubscribeConsumesSubPool_Test", "blockedByBug-637937"},
 //			dependsOnGroups={"EnsureSubPoolIsNotDeletedAfterAllOtherSystemsUnsubscribeFromSubPool_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void EnsureSystemAutosubscribeConsumesSubPool_Test() throws JSONException {
 		log.info("Now register client1 under username '"+username+"' as a person and subscribe to the '"+personSubscriptionName+"' subscription pool...");
 		client1tasks.unregister(null, null, null);
 		client1tasks.register(username, password, ConsumerType.person, null, null, null, null, null, null, null);
 		personConsumerId = client1tasks.getCurrentConsumerId();
 		
 		for (int j=0; j<personSubscriptionPoolProductData.length(); j++) {
 			JSONObject poolProductDataAsJSONObject = (JSONObject) personSubscriptionPoolProductData.get(j);
 			String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 			JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 			String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 			JSONArray bundledProductData = subpoolProductDataAsJSONObject.getJSONArray("bundledProductData");
 		
 			SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",personProductId,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 			Assert.assertNotNull(pool,"Personal subscription with ProductId '"+personProductId+"' is available to user '"+username+"' registered as a person.");
 			List<File> beforeEntitlementCertFiles = client1tasks.getCurrentEntitlementCertFiles();
 
 			client1tasks.subscribeToSubscriptionPool(pool);
 		
 			log.info("Now register client2 under username '"+username+"' as a system with autosubscribe to assert that subpools bundled products gets consumed...");
 			client2tasks.unregister(null, null, null);
 			client2tasks.register(username, password, ConsumerType.system, null, null, Boolean.TRUE, null, null, null, null);
 			List<ProductSubscription> client2ConsumedProductSubscriptions = client2tasks.getCurrentlyConsumedProductSubscriptions();
 			
 			for (int k=0; k<bundledProductData.length(); k++) {
 				JSONObject bundledProductAsJSONObject = (JSONObject) bundledProductData.get(k);
 				String systemConsumedProductName = bundledProductAsJSONObject.getString("productName");
 				
 				ProductSubscription consumedProductSubscription = ProductSubscription.findFirstInstanceWithMatchingFieldFromList("productName",systemConsumedProductName,client2ConsumedProductSubscriptions);
 				Assert.assertNotNull(consumedProductSubscription,systemConsumedProductName+" has been autosubscribed by client2 '"+client2.getConnection().getHostname()+"' (registered as a system under username '"+username+"')");
 	
 			}
 			client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();	
 			client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: No consumer created by any other user in the same owner can see the sub pool",
 			groups={"EnsureUsersSubPoolIsNotAvailableToSystemsRegisterByAnotherUsernameUnderSameOwner_Test", "blockedByBug-643405"},
 //			dependsOnGroups={"EnsureSubPoolIsNotDeletedAfterAllOtherSystemsUnsubscribeFromSubPool_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=61126)
 	public void EnsureUsersSubPoolIsNotAvailableToSystemsRegisterByAnotherUsernameUnderSameOwner_Test() throws JSONException {
 		unsubscribeAndUnregisterMultipleSystemsAfterGroups();
 		
 		log.info("Register client1 under username '"+username+"' as a person and subscribe to the '"+personSubscriptionName+"' subscription pool...");
 		client1tasks.register(username, password, ConsumerType.person, null, null, null, null, null, null, null);
 		personConsumerId = client1tasks.getCurrentConsumerId();
 		
 		for (int j=0; j<personSubscriptionPoolProductData.length(); j++) {
 			JSONObject poolProductDataAsJSONObject = (JSONObject) personSubscriptionPoolProductData.get(j);
 			String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 			JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 			String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 			
 			SubscriptionPool personSubscriptionPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",personProductId,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 			Assert.assertNotNull(personSubscriptionPool,
 					"Personal subscription with ProductId '"+personProductId+"' is available to user '"+username+"' registered as a person.");
 			client1tasks.subscribe(personSubscriptionPool.poolId, null, null, null, null, null, null, null);
 	
 			log.info("Now register client2 under username '"+username+"' as a system and assert the subpool ProductId '"+systemProductId+"' is available...");
 			client2tasks.unregister(null, null, null);
 			client2tasks.register(username, password, ConsumerType.system, null, null, null, null, null, null, null);
 			Assert.assertNotNull(SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",systemProductId,client2tasks.getCurrentlyAvailableSubscriptionPools()),
 					"Personal subpool subscription with ProductId '"+systemProductId+"' is available to user '"+username+"' registered as a system.");
 	
 			log.info("Now register client2 under username '"+anotherUsername+"' as a system and assert the subpool ProductId '"+systemProductId+"' is NOT available...");
 			if (anotherUsername==null) throw new SkipException("This test requires another username under the same owner as username '"+username+"'.");
 			client2tasks.unregister(null, null, null);
 			client2tasks.register(anotherUsername, anotherPassword, ConsumerType.system, null, null, null, null, null, null, null);
 			Assert.assertNull(SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",systemProductId,client2tasks.getCurrentlyAvailableSubscriptionPools()),
 					"Personal subpool subscription with ProductId '"+systemProductId+"' is NOT available to user '"+anotherUsername+"' who is under the same owner as '"+username+"'.");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure a system cannot subscribe to a personal subscription pool",
 			groups={"EnsureSystemCannotSubscribeToPersonalPool_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void EnsureSystemCannotSubscribeToPersonalPool_Test() throws JSONException {
 		unsubscribeAndUnregisterMultipleSystemsAfterGroups();
 		
 		log.info("Register client1 under username '"+username+"' as a system and assert that personal products can NOT be subscribed to...");
 		//client1tasks.unregister();
 		client1tasks.register(username, password, ConsumerType.system, null, null, null, null, null, null, null);
 		
 		for (int j=0; j<personSubscriptionPoolProductData.length(); j++) {
 			JSONObject poolProductDataAsJSONObject = (JSONObject) personSubscriptionPoolProductData.get(j);
 			String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 			JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 			String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 		
 			SubscriptionPool personSubscriptionPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",personProductId,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 			Assert.assertNotNull(personSubscriptionPool,
 					"ProductId '"+personProductId+"' is listed as all available to user '"+username+"' registered as a system.");
 			SSHCommandResult sshComandResult = client1tasks.subscribe(personSubscriptionPool.poolId, null, null, null, null, null, null, null);
 	
 			// stdout: Consumers of this type are not allowed to subscribe to the pool with id 'ff8080812c9e72a8012c9e738ce70191'
 			Assert.assertContainsMatch(sshComandResult.getStdout().trim(), "Consumers of this type are not allowed to subscribe to the pool with id '"+personSubscriptionPool.poolId+"'",
 					"Attempting to subscribe a system consumer to a personal pool is blocked.");
 			Assert.assertEquals(client1tasks.listConsumedProductSubscriptions().getStdout().trim(),"No Consumed subscription pools to list",
 					"Because the subscribe attempt was blocked, there should still be 'No Consumed subscription pools to list'.");
 		}
 	}
 	
 	
 	// TODO Candidates for an automated Test:
 	//		https://bugzilla.redhat.com/show_bug.cgi?id=626509
 	
 	
 	
 	
 	
 	
 	
 	
 	// Configuration Methods ***********************************************************************
 	
 	
 	@BeforeClass(groups="setup")
 	public void setupBeforeClass() throws IOException, JSONException {
 		// alternative to dependsOnGroups={"RegisterWithUsernameAndPassword_Test"}
 		// This allows us to satisfy a dependency on registrationDataList making TestNG add unwanted Test results.
 		// This also allows us to individually run this Test Class on Hudson.
 		RegisterWithUsernameAndPassword_Test(); // needed to populate registrationDataList
 		
 		// find anotherConsumerUsername under the same owner as consumerUsername
 		RegistrationData registrationDataForSystemUsername = findRegistrationDataMatchingUsername(username);
 		Assert.assertNotNull(registrationDataForSystemUsername, "Found the RegistrationData for username '"+username+"': "+registrationDataForSystemUsername);
 		RegistrationData registrationDataForAnotherSystemUsername = findRegistrationDataMatchingOwnerKeyButNotMatchingUsername(registrationDataForSystemUsername.ownerKey,username);
 		if (registrationDataForAnotherSystemUsername!=null) {
 			anotherUsername = registrationDataForAnotherSystemUsername.username;
 			anotherPassword = registrationDataForAnotherSystemUsername.password;
 		}
 
 	}
 	
 	
 	@BeforeClass(groups={"setup"})
 	public void beforeClassSetup() throws JSONException {
 		if (getPersonProductIds()==null) {
 			throw new SkipException("To enable the RHEL Personal Tests, we need to know the ProductId of a Subscription containing a subpool of personal products.");
 		}
 		
 		// initialize systemSubscriptionQuantity
 		if (!systemSubscriptionQuantity.equalsIgnoreCase("unlimited")) {
 			int quantity = Integer.valueOf(systemSubscriptionQuantity);
 			Assert.assertTrue(quantity>0,"Expecting personal subpool subscription to be available with a positive quantity.");
 			if (multipleSystems>quantity) {
 				multipleSystems = quantity - 1;
 			}
 		}
 
 	}
 	
 	
 	// FIXME: It might be a good idea for a BeforeGroups methods to somehow query all the system consumers that have entitlements from the subpool and unregister them first
 	// this would avoid errors like this:
 	//	ssh root@jsefler-betaqa-1.usersys.redhat.com subscription-manager unregister
 	//	Stdout:
 	//	Cannot unregister consumer 'jsefler-qabetaperson-2' because:
 	//	-Cannot unsubscribe entitlement '8a9b90882dce731d012dd59341ed0ecb' because:
 	//	 Consumer 'jsefler-betaqa-2.usersys.redhat.com' with identity 'e56993b8-0c9d-40c9-ae8d-f51af7e6e75d' has the following entitlements:
 	//	 Entitlement '8a9b90882dce731d012dd5943f460ed0':
 	//	 account number: '1407770'
 	//	 serial number: '8129636845352976'
 	//
 	//	The above entitlements were derived from the pool: '8a9b90882dce731d012dd25e1a7804be'.
 	//	Please unsubscribe from the above entitlements first.
 	@AfterGroups(groups={"setup"}, value={"RHELPersonal","EnsureSystemAutosubscribeConsumesSubPool_Test"}, alwaysRun=true)
 	public void unsubscribeAndUnregisterMultipleSystemsAfterGroups() {
 		if (client2tasks!=null) {
 			client2tasks.unsubscribe_(Boolean.TRUE,null, null, null, null);
 			client2tasks.unregister_(null, null, null);
 		}
 
 		if (client1tasks!=null) {
 			
 			for (String systemConsumerId : systemConsumerIds) {
 				client1tasks.register_(username,password,null,null,systemConsumerId,null, Boolean.TRUE, null, null, null);
 				client1tasks.unsubscribe_(Boolean.TRUE, null, null, null, null);
 				client1tasks.unregister_(null, null, null);
 			}
 			systemConsumerIds.clear();
 			
 			if (personConsumerId!=null) {
 				//client1tasks.reregister_(client1username, client1password, personConsumerId);
 				//client1tasks.removeAllCerts(true, true);
 				client1tasks.register_(username,password,null,null,personConsumerId,null,Boolean.TRUE, null, null, null);
 			}
 			client1tasks.unsubscribe_(Boolean.TRUE,null, null, null, null);
 			client1tasks.unregister_(null, null, null);
 			personConsumerId=null;
 		}
 	}
 	
 
 
 	
 	// Protected Methods ***********************************************************************
 
 	protected List<String> systemConsumerIds = new ArrayList<String>();
 	protected String personConsumerId = null;
 	protected int multipleSystems = 4;	// multiple (unlimited)  // multipleSystems is a count of systems that will be used to subscribe to the sub-pool.  Theoretically this number should be very very large to test the unlimited quantity
 	
 	protected String username = getProperty("sm.rhpersonal.username", "");
 	protected String password = getProperty("sm.rhpersonal.password", "");
 	protected String anotherUsername = null;	// under the same ownerkey as username
 	protected String anotherPassword = null;
 	protected String personSubscriptionName = null;
 	protected EntitlementCert personEntitlementCert = null;
 	SubscriptionPool personSubscriptionPool = null;
 	protected String systemSubscriptionQuantity = getProperty("sm.rhpersonal.subproductQuantity", "unlimited");
 
 
 
 
 	
 	// Data Providers ***********************************************************************
 
 	
 
 }
 
