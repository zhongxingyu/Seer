 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.ContentNamespace;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductNamespace;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author ssalevan
  * @author jsefler
  *
  */
 @Test(groups={"SubscribeTests"})
 public class SubscribeTests extends SubscriptionManagerCLITestScript{
 	
 	// Test methods ***********************************************************************
 	
 
 	@Test(	description="subscription-manager-cli: subscribe consumer to an expected subscription pool product id",
 			dataProvider="getSystemSubscriptionPoolProductData",
 			groups={"blockedByBug-660713"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void SubscribeToExpectedSubscriptionPoolProductId_Test(String productId, JSONArray bundledProductDataAsJSONArray) throws JSONException {
 //if (!productId.equals("awesomeos-server")) throw new SkipException("debugging");
 		
 		// begin test with a fresh register
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 
 		// assert the subscription pool with the matching productId is available
 //		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool, "Expected SubscriptionPool with ProductId '"+productId+"' is available for subscribing.");
 
 		List<ProductCert> currentlyInstalledProductCerts = clienttasks.getCurrentProductCerts();
 		List<InstalledProduct> currentlyInstalledProducts = clienttasks.getCurrentlyInstalledProducts();
 		
 		// assert the installed status of the bundled products
 		for (int j=0; j<bundledProductDataAsJSONArray.length(); j++) {
 			JSONObject bundledProductAsJSONObject = (JSONObject) bundledProductDataAsJSONArray.get(j);
 			String productName = bundledProductAsJSONObject.getString("productName");
 			
 			// assert the status of the installed product
 			ProductCert productCert = ProductCert.findFirstInstanceWithMatchingFieldFromList("productName", productName, currentlyInstalledProductCerts);
 			if (productCert!=null) {
 				InstalledProduct installedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", productName, currentlyInstalledProducts);
 				Assert.assertNotNull(installedProduct, "The status of product with ProductName '"+productName+"' is reported in the list of installed products.");
 				Assert.assertEquals(installedProduct.status, "Not Subscribed", "Before subscribing to ProductId '"+productId+"', the status of Installed Product '"+productName+"' is Not Subscribed.");
 			}
 		}
 		
 		// subscribe to the pool
 		File entitlementCertFile = clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		
 		currentlyInstalledProducts = clienttasks.getCurrentlyInstalledProducts();
 		List<ProductSubscription> currentlyConsumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 
 		// assert the expected products are consumed
 		for (int j=0; j<bundledProductDataAsJSONArray.length(); j++) {
 			JSONObject bundledProductAsJSONObject = (JSONObject) bundledProductDataAsJSONArray.get(j);
 			String productName = bundledProductAsJSONObject.getString("productName");
 			
 			ProductSubscription productSubscription = ProductSubscription.findFirstInstanceWithMatchingFieldFromList("productName", productName, currentlyConsumedProductSubscriptions);
 			Assert.assertNotNull(productSubscription, "Expected ProductSubscription with ProductName '"+productName+"' is consumed after subscribing to pool with ProductId '"+productId+"'.");
 
 			// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=660713 - jsefler 12/12/2010
 			Boolean invokeWorkaroundWhileBugIsOpen = true;
 			try {String bugId="660713"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				log.warning("The workaround while this bug is open is to skip the assertion that: Consumed ProductSubscription Expires on the same DAY as the originating subscription pool.");
 			} else {
 			// END OF WORKAROUND
 				
 				// OLD CODE THAT I THINK WAS WRONG  jsefler 12/2/2010
 //				// assert the dates match
 //				Calendar dayBeforeEndDate = (Calendar) entitlementCert.validityNotAfter.clone(); dayBeforeEndDate.add(Calendar.DATE, -1);
 ////				Calendar dayBeforeStartDate = (Calendar) entitlementCert.validityNotBefore.clone(); dayBeforeStartDate.add(Calendar.DATE, -1);
 //				//Assert.assertEquals(productSubscription.endDate, entitlementCert.validityNotAfter, "Consumed ProductSubscription Expires on the same end date as the given entitlement: "+entitlementCert);
 //				Assert.assertTrue(productSubscription.endDate.before(entitlementCert.validityNotAfter) && productSubscription.endDate.after(dayBeforeEndDate), "Consumed ProductSubscription Expires on the same end date as the new entitlement: "+entitlementCert);
 //				Assert.assertTrue(productSubscription.startDate.before(entitlementCert.validityNotBefore), "Consumed ProductSubscription Began before the validityNotBefore date of the new entitlement: "+entitlementCert);
 //				Assert.assertEquals(ProductSubscription.formatDateString(productSubscription.endDate), SubscriptionPool.formatDateString(pool.endDate), "Consumed ProductSubscription Expires on the same date as the originating subscription pool: "+pool);
 
 				// assert the dates match
 				//FIXME https://bugzilla.redhat.com/show_bug.cgi?id=660713 UNCOMMENT WHEN YOU GET AN EXPLANATION FROM DEVELOPMENT
 //				Assert.assertEquals(ProductSubscription.formatDateString(productSubscription.startDate),ProductSubscription.formatDateString(entitlementCert.startDate),
 //						"Consumed ProductSubscription Begins on the same DAY as the new entitlement.");
 //				Assert.assertEquals(ProductSubscription.formatDateString(productSubscription.endDate),ProductSubscription.formatDateString(entitlementCert.endDate),
 //						"Consumed ProductSubscription Expires on the same DAY as the new entitlement.");
 				Assert.assertEquals(ProductSubscription.formatDateString(productSubscription.endDate),ProductSubscription.formatDateString(pool.endDate),
 						"Consumed ProductSubscription Expires on the same DAY as the originating subscription pool.");
 				//FIXME		Assert.assertTrue(productSubscription.startDate.before(entitlementCert.validityNotBefore), "Consumed ProductSubscription Began before the validityNotBefore date of the new entitlement: "+entitlementCert);
 			}
 			
 			// find the corresponding productNamespace from the entitlementCert
 			ProductNamespace productNamespace = null;
 			for (ProductNamespace pn : entitlementCert.productNamespaces) {
 				if (pn.name.equals(productName)) productNamespace = pn;
 			}
 			
 			// assert the installed status of the corresponding product
 			if (entitlementCert.productNamespaces.isEmpty()) {
 				log.warning("This product '"+productId+"' ("+productName+") does not appear to grant entitlement to any client side content.  This must be a server side management add-on product. Asserting as such...");
 
 				Assert.assertEquals(entitlementCert.contentNamespaces.size(),0,
 						"When there are no productNamespaces in the entitlementCert, there should not be any contentNamespaces.");
 
 				// when there is no corresponding product, then there better not be an installed product status by the same product name
 				Assert.assertNull(InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", productName, currentlyInstalledProducts),
 						"Should not find any installed product status matching a server side management add-on productName: "+ productName);
 
 				// when there is no corresponding product, then there better not be an installed product cert by the same product name
 				Assert.assertNull(ProductCert.findFirstInstanceWithMatchingFieldFromList("productName", productName, currentlyInstalledProductCerts),
 						"Should not find any installed product certs matching a server side management add-on productName: "+ productName);
 
 			} else {
 				Assert.assertNotNull(productNamespace, "The new entitlement cert's product namespace corresponding to this expected ProductSubscription with ProductName '"+productName+"' was found.");
 
 				// assert whether or not the product is installed			
 				InstalledProduct installedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", productName, currentlyInstalledProducts);
 				Assert.assertNotNull(installedProduct, "The status of product with ProductName '"+productName+"' is reported in the list of installed products.");
 	
 				// assert the status of the installed product
 				//ProductCert productCert = ProductCert.findFirstInstanceWithMatchingFieldFromList("productName", productName, currentlyInstalledProductCerts);
				ProductCert productCert = ProductCert.findFirstInstanceWithMatchingFieldFromList("id", productNamespace.id, currentlyInstalledProductCerts);
 				if (productCert!=null) {
 					Assert.assertEquals(installedProduct.status, "Subscribed", "After subscribing to ProductId '"+productId+"', the status of Installed Product '"+productName+"' is Subscribed since a corresponding product cert was found in "+clienttasks.productCertDir);
 					Assert.assertEquals(InstalledProduct.formatDateString(installedProduct.expires), ProductSubscription.formatDateString(productSubscription.endDate), "Installed Product '"+productName+"' expires on the same date as the consumed ProductSubscription: "+productSubscription);
 					Assert.assertEquals(installedProduct.subscription, productSubscription.serialNumber, "Installed Product '"+productName+"' subscription matches the serialNumber of the consumed ProductSubscription: "+productSubscription);
 				} else {
 					Assert.assertEquals(installedProduct.status, "Not Installed", "The status of Entitled Product '"+productName+"' is Not Installed since a corresponding product cert was not found in "+clienttasks.productCertDir);
 				}
 			}
 		}
 	}
 
 	
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to an entitlement using product ID",
 			enabled=false,	// Subscribing to a Subscription Pool using --product Id has been removed in subscription-manager-0.71-1.el6.i686.
 			groups={"blockedByBug-584137"},
 			dataProvider="getAvailableSubscriptionPoolsData")
 	@ImplementsNitrateTest(caseId=41680)
 	public void SubscribeToValidSubscriptionsByProductID_Test(SubscriptionPool pool){
 //		sm.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		clienttasks.subscribeToSubscriptionPoolUsingProductId(pool);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to an entitlement using product ID",
 			groups={"blockedByBug-584137"},
 			enabled=false)
 	public void SubscribeToASingleEntitlementByProductID_Test(){
 		clienttasks.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 		SubscriptionPool MCT0696 = new SubscriptionPool("MCT0696", "696");
 		MCT0696.addProductID("Red Hat Directory Server");
 		clienttasks.subscribeToSubscriptionPoolUsingProductId(MCT0696);
 		//this.refreshSubscriptions();
 		for (ProductSubscription pid:MCT0696.associatedProductIDs){
 			Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().contains(pid),
 					"ProductID '"+pid.productName+"' consumed from Pool '"+MCT0696.subscriptionName+"'");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to an entitlement using pool ID",
 			groups={"blockedByBug-584137"},
 			enabled=true,
 			dataProvider="getAvailableSubscriptionPoolsData")
 	@ImplementsNitrateTest(caseId=41686)
 	public void SubscribeToValidSubscriptionsByPoolID_Test(SubscriptionPool pool){
 // non-dataProvided test procedure
 //		sm.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to each available subscription pool using pool ID",
 			groups={"blockedByBug-584137"},
 			dataProvider="getGoodRegistrationData")
 	@ImplementsNitrateTest(caseId=41686)
 	public void SubscribeConsumerToEachAvailableSubscriptionPoolUsingPoolId_Test(String username, String password){
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(username, password, ConsumerType.system, null, null, Boolean.FALSE, Boolean.FALSE, null, null, null);
 		clienttasks.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 	}
 	
 	
 	// TODO DELETE TEST due to https://bugzilla.redhat.com/show_bug.cgi?id=670823
 	@Test(	description="subscription-manager-cli: subscribe consumer to an entitlement using registration token",
 			groups={"blockedByBug-584137"},
 			enabled=false)
 	@ImplementsNitrateTest(caseId=41681)
 	public void SubscribeToRegToken_Test(){
 		clienttasks.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.subscribeToRegToken(regtoken);
 	}
 	
 	
 	@Test(	description="Subscribed for Already subscribed Entitlement.",
 			groups={"blockedByBug-584137"},
 			dataProvider="getAvailableSubscriptionPoolsData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41897)
 	public void AttemptToSubscribeToAnAlreadySubscribedPool_Test(SubscriptionPool pool){
 // non-dataProvided test procedure
 //		//sm.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 //		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		for(SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 //			clienttasks.subscribeToSubscriptionPoolUsingProductId(pool);
 //			clienttasks.subscribeToProduct(pool.subscriptionName);
 //		}
 		clienttasks.subscribeToSubscriptionPoolUsingProductId(pool);
 		SSHCommandResult result = clienttasks.subscribe_(null,pool.poolId,null,null,null, null, null, null, null);
 		Assert.assertEquals(result.getStdout().trim(), "This consumer is already subscribed to the product matching pool with id '"+pool.poolId+"'",
 				"subscribe command returns proper message when already subscribed to the requested pool");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to multiple/duplicate/bad pools in one call",
 			groups={"blockedByBug-622851"},
 			enabled=true)
 	public void SubscribeToMultipleDuplicateAndBadPools_Test() {
 		
 		// begin the test with a cleanly registered system
 		clienttasks.unregister(null, null, null);
 	    clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 	    
 		// assemble a list of all the available SubscriptionPool ids with duplicates and bad ids
 		List <String> poolIds = new ArrayList<String>();
 		for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			poolIds.add(pool.poolId);
 			poolIds.add(pool.poolId); // add a duplicate poolid
 		}
 		String badPoolId1 = "bad123", badPoolId2 = "bad_POOLID"; 
 		poolIds.add(0, badPoolId1); // insert a bad poolid
 		poolIds.add(badPoolId2); // append a bad poolid
 		
 		// subscribe to all pool ids
 		log.info("Attempting to subscribe to multiple pools with duplicate and bad pool ids...");
 		SSHCommandResult result = clienttasks.subscribe(poolIds, null, null, null, null, null, null, null);
 		
 		// assert the results
 		for (String poolId : poolIds) {
 			if (poolId.equals(badPoolId1)) continue; if (poolId.equals(badPoolId2)) continue;
 			Assert.assertContainsMatch(result.getStdout(),"^This consumer is already subscribed to the product matching pool with id '"+poolId+"'$","Asserting that already subscribed to pools is noted and skipped during a multiple pool binding.");
 		}
 		Assert.assertContainsMatch(result.getStdout(),"^No such entitlement pool: "+badPoolId1+"$","Asserting that an invalid pool is noted and skipped during a multiple pool binding.");
 		Assert.assertContainsMatch(result.getStdout(),"^No such entitlement pool: "+badPoolId2+"$","Asserting that an invalid pool is noted and skipped during a multiple pool binding.");
 		clienttasks.assertNoAvailableSubscriptionPoolsToList("Asserting that no available subscription pools remain after simultaneously subscribing to them all including duplicates and bad pool ids.");
 	}
 	
 	
 	@Test(	description="rhsmcertd: change certFrequency",
 			dataProvider="getCertFrequencyData",
 			groups={"blockedByBug-617703","blockedByBug-700952","blockedByBug-708512"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41692)
 	public void rhsmcertdChangeCertFrequency_Test(int minutes) {
 		String errorMsg = "Either the consumer is not registered with candlepin or the certificates are corrupted. Certificate updation using daemon failed.";
 		errorMsg = "Either the consumer is not registered or the certificates are corrupted. Certificate update using daemon failed.";
 		
 		log.info("First test with an unregistered user and verify that the rhsmcertd actually fails since it cannot self-identify itself to the candlepin server.");
 		clienttasks.unregister(null, null, null);
 		clienttasks.restart_rhsmcertd(minutes, false); sleep(10000); // allow 10sec for the initial update
 		log.info("Appending a marker in the '"+clienttasks.rhsmcertdLogFile+"' so we can assert that the certificates are being updated every '"+minutes+"' minutes");
 		String marker = "Testing rhsm.conf certFrequency="+minutes+" when unregistered..."; // https://tcms.engineering.redhat.com/case/41692/
 		RemoteFileTasks.runCommandAndAssert(client,"echo \""+marker+"\" >> "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0));
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0),marker,null);
 		sleep(minutes*60*1000);	// give the rhsmcertd a chance to check in with the candlepin server and update the certs
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0),"update failed \\(\\d+\\), retry in "+minutes+" minutes",null);
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmLogFile,Integer.valueOf(0),errorMsg,null);
 		
 		
 		log.info("Now test with a registered user whose identity is corrupt and verify that the rhsmcertd actually fails since it cannot self-identify itself to the candlepin server.");
 		String consumerid = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null));
 		log.info("Corrupting the identity cert by borking its content...");
 		RemoteFileTasks.runCommandAndAssert(client, "openssl x509 -noout -text -in "+clienttasks.consumerCertFile+" > /tmp/stdout; mv /tmp/stdout -f "+clienttasks.consumerCertFile, 0);
 		clienttasks.restart_rhsmcertd(minutes, false); sleep(10000); // allow 10sec for the initial update
 		log.info("Appending a marker in the '"+clienttasks.rhsmcertdLogFile+"' so we can assert that the certificates are being updated every '"+minutes+"' minutes");
 		marker = "Testing rhsm.conf certFrequency="+minutes+" when identity is corrupted...";
 		RemoteFileTasks.runCommandAndAssert(client,"echo \""+marker+"\" >> "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0));
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0),marker,null);
 		sleep(minutes*60*1000);	// give the rhsmcertd a chance to check in with the candlepin server and update the certs
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0),"update failed \\(\\d+\\), retry in "+minutes+" minutes",null);
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmLogFile,Integer.valueOf(0),errorMsg,null);
 
 		
 		log.info("Finally test with a registered user and verify that the rhsmcertd succeeds because he can identify himself to the candlepin server.");
 	    clienttasks.register(clientusername, clientpassword, null, null, consumerid, null, Boolean.TRUE, null, null, null);
 		clienttasks.restart_rhsmcertd(minutes, false); sleep(10000); // allow 10sec for the initial update
 		log.info("Appending a marker in the '"+clienttasks.rhsmcertdLogFile+"' so we can assert that the certificates are being updated every '"+minutes+"' minutes");
 		marker = "Testing rhsm.conf certFrequency="+minutes+" when registered..."; // https://tcms.engineering.redhat.com/case/41692/
 		RemoteFileTasks.runCommandAndAssert(client,"echo \""+marker+"\" >> "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0));
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0),marker,null);
 		sleep(minutes*60*1000);	// give the rhsmcertd a chance to check in with the candlepin server and update the certs
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0),"certificates updated",null);
 
 		/* tail -f /var/log/rhsm/rhsm.log
 		 * 2010-09-10 12:05:06,338 [ERROR] main() @certmgr.py:75 - Either the consumer is not registered with candlepin or the certificates are corrupted. Certificate updation using daemon failed.
 		 */
 		
 		/* tail -f /var/log/rhsm/rhsmcertd.log
 		 * Fri Sep 10 11:59:50 2010: started: interval = 1 minutes
 		 * Fri Sep 10 11:59:51 2010: update failed (255), retry in 1 minutes
 		 * testing rhsm.conf certFrequency=1 when unregistered.
 		 * Fri Sep 10 12:00:51 2010: update failed (255), retry in 1 minutes
 		 * Fri Sep 10 12:01:04 2010: started: interval = 1 minutes
 		 * Fri Sep 10 12:01:05 2010: certificates updated
 		 * testing rhsm.conf certFrequency=1 when registered.
 		 * Fri Sep 10 12:02:05 2010: certificates updated
 		*/
 	}
 	
 	
 	@Test(	description="rhsmcertd: ensure certificates synchronize",
 			groups={"blockedByBug-617703"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41694)
 	public void rhsmcertdEnsureCertificatesSynchronize_Test(){
 		
 		// start with a cleanly unregistered system
 		clienttasks.unregister(null, null, null);
 		
 		// register a clean user
 	    clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 	    
 	    // subscribe to all the available pools
 	    clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType.system);
 	    
 	    // get all of the current entitlement certs and remember them
 	    List<File> entitlementCertFiles = clienttasks.getCurrentEntitlementCertFiles();
 	    
 	    // delete all of the entitlement cert files
 	    client.runCommandAndWait("rm -rf "+clienttasks.entitlementCertDir+"/*");
 	    Assert.assertEquals(clienttasks.getCurrentEntitlementCertFiles().size(), 0,
 	    		"All the entitlement certs have been deleted.");
 		
 	    // restart the rhsmcertd to run every 1 minute and wait for a refresh
 		clienttasks.restart_rhsmcertd(1, true);
 		
 		// assert that rhsmcertd has refreshed the entitlement certs back to the original
 	    Assert.assertEquals(clienttasks.getCurrentEntitlementCertFiles(), entitlementCertFiles,
 	    		"All the deleted entitlement certs have been re-synchronized by rhsm cert deamon.");
 	}
 	
 	
 	@Test(	description="subscription-manager: make sure the available pools come from subscriptions that pass the hardware rules for availability.",
 			groups={},
 			dependsOnGroups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	// Note: The objective if this test is essentially the same as ListTests.EnsureHardwareMatchingSubscriptionsAreListedAsAvailable_Test() and ListTests.EnsureNonHardwareMatchingSubscriptionsAreNotListedAsAvailable_Test(), but its implementation is slightly different
 	public void VerifyAvailablePoolsPassTheHardwareRulesCheck_Test() throws Exception {
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, true, null, null, null);
 
 		List<List<Object>> subscriptionPoolProductData = getSystemSubscriptionPoolProductDataAsListOfLists();
 		List<SubscriptionPool> availableSubscriptionPoolsBeforeAutosubscribe = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		for (List<Object> subscriptionPoolProductDatum : subscriptionPoolProductData) {
 			String productId = (String)subscriptionPoolProductDatum.get(0);
 			SubscriptionPool subscriptionPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, availableSubscriptionPoolsBeforeAutosubscribe);
 			Assert.assertNotNull(subscriptionPool, "Expecting SubscriptionPool with ProductId '"+productId+"' to be available to registered user '"+clientusername+"'.");
 		}
 		for (SubscriptionPool availableSubscriptionPool : availableSubscriptionPoolsBeforeAutosubscribe) {
 			boolean productIdFound = false;
 			for (List<Object> subscriptionPoolProductDatum : subscriptionPoolProductData) {
 				if (availableSubscriptionPool.productId.equals((String)subscriptionPoolProductDatum.get(0))) {
 					productIdFound = true;
 					break;
 				}
 			}
 			Assert.assertTrue(productIdFound, "Available SubscriptionPool with ProductId '"+availableSubscriptionPool.productId+"' passes the hardware rules check.");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: autosubscribe consumer and verify expected subscription pool product id are consumed",
 			groups={"AutoSubscribeAndVerify", "blockedByBug-680399"},
 			dependsOnMethods={"VerifyAvailablePoolsPassTheHardwareRulesCheck_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void InititiateAutoSubscribe_Test() throws Exception {
 
 		// before testing, make sure all the expected subscriptionPoolProductId are available
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 		// get the expected subscriptionPoolProductIdData
 		subscriptionPoolProductData = getSystemSubscriptionPoolProductDataAsListOfLists(false);
 		
 		// autosubscribe
 		sshCommandResultFromAutosubscribe = clienttasks.subscribe(Boolean.TRUE,null,null,null,null,null,null,null,null);
 		
 		// Example Results from register --autosubscribe
 		//		[root@jsefler-onprem03 ~]# subscription-manager register --username=testuser1 --password=password --autosubscribe
 		//		9a63ba95-4f0f-47da-b645-69cc3915e2bd jsefler-onprem03.usersys.redhat.com
 		//		Installed Products:
 		//		   Multiplier Product Bits - Not Subscribed
 		//		   Load Balancing Bits - Subscribed
 		//		   Awesome OS Server Bits - Subscribed
 		//		   Management Bits - Subscribed
 		//		   Awesome OS Scalable Filesystem Bits - Subscribed
 		//		   Shared Storage Bits - Subscribed
 		//		   Large File Support Bits - Subscribed
 		//		   Awesome OS Workstation Bits - Subscribed
 		//		   Awesome OS Premium Architecture Bits - Not Subscribed
 		//		   Awesome OS for S390X Bits - Not Subscribed
 		//		   Awesome OS Developer Basic - Not Subscribed
 		//		   Clustering Bits - Subscribed
 		//		   Awesome OS Developer Bits - Not Subscribed
 		//		   Awesome OS Modifier Bits - Subscribed
 	}
 	protected List<List<Object>> subscriptionPoolProductData;
 
 	@Test(	description="subscription-manager-cli: autosubscribe consumer and verify expected subscription pool product id are consumed",
 			groups={"AutoSubscribeAndVerify","blockedByBug-672438","blockedByBug-678049"},
 			dependsOnMethods={"InititiateAutoSubscribe_Test"},
 			dataProvider="getInstalledProductCertsData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyInstalledProductCertWasAutoSubscribed_Test(ProductCert productCert) throws Exception {
 		// get the expected subscriptionPoolProductIdData
 		String sm_debug_dataProviders_minimize = getProperty("sm.debug.dataProviders.minimize","$NULL");
 		System.setProperty("sm.debug.dataProviders.minimize","false");
 		//List<List<Object>> subscriptionPoolProductData = getSystemSubscriptionPoolProductDataAsListOfLists();
 		System.setProperty("sm.debug.dataProviders.minimize",sm_debug_dataProviders_minimize);
 		
 		// search the subscriptionPoolProductData for a bundledProduct matching the productCert's productName
 		String subscriptionPoolProductId = null;
 		for (List<Object> row : subscriptionPoolProductData) {
 			JSONArray bundledProductDataAsJSONArray = (JSONArray)row.get(1);
 			
 			for (int j=0; j<bundledProductDataAsJSONArray.length(); j++) {
 				JSONObject bundledProductAsJSONObject = (JSONObject) bundledProductDataAsJSONArray.get(j);
 				String bundledProductName = bundledProductAsJSONObject.getString("productName");
 				String bundledProductId = bundledProductAsJSONObject.getString("productId");
 
 				if (bundledProductId.equals(productCert.productId)) {
 					subscriptionPoolProductId = (String)row.get(0); // found
 					break;
 				}
 			}
 			if (subscriptionPoolProductId!=null) break;
 		}
 		
 		// determine what autosubscribe results to assert for this installed productCert 
 		InstalledProduct installedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", productCert.productName, clienttasks.getCurrentlyInstalledProducts());
 
 		// when subscriptionPoolProductId!=null, then this productCert should have been autosubscribed
 		String expectedSubscribeStatus = (subscriptionPoolProductId!=null)? "Subscribed":"Not Subscribed";
 		
 		// assert the installed product status matches the expected status 
 		Assert.assertEquals(installedProduct.status,expectedSubscribeStatus,
 				"As expected, the Installed Product Status reflects that the autosubscribed ProductName '"+productCert.productName+"' is "+expectedSubscribeStatus.toLowerCase()+".");
 
 		// assert that the sshCommandResultOfAutosubscribe showed the expected Subscribe Status for this productCert
 		Assert.assertContainsMatch(sshCommandResultFromAutosubscribe.getStdout().trim(), "^\\s+"+productCert.productName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"+" - "+expectedSubscribeStatus),
 				"As expected, ProductName '"+productCert.productName+"' was reported as '"+expectedSubscribeStatus+"' in the output from register with autotosubscribe.");
 	}
 //	List<SubscriptionPool> availableSubscriptionPoolsBeforeAutosubscribe;
 	SSHCommandResult sshCommandResultFromAutosubscribe;
 	
 	
 	
 	// Candidates for an automated Test:
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=668032
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=670831
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=664847#3
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=676377 - could use dbus-monitor to assert that the dbus message is sent on the expected compliance changing events
 	
 	
 	// Configuration Methods ***********************************************************************
 	
 	
 	
 	// Protected Methods ***********************************************************************
 	
 
 	
 	// Data Providers ***********************************************************************
 
 	@DataProvider(name="getInstalledProductCertsData")
 	public Object[][] getInstalledProductCertsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInstalledProductCertsDataAsListOfLists());
 	}
 	protected List<List<Object>> getInstalledProductCertsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		for (ProductCert productCert: clienttasks.getCurrentProductCerts()) {
 			ll.add(Arrays.asList(new Object[]{productCert}));
 		}
 		
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getCertFrequencyData")
 	public Object[][] getCertFrequencyDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getCertFrequencyDataAsListOfLists());
 	}
 	protected List<List<Object>> getCertFrequencyDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		// int minutes
 		ll.add(Arrays.asList(new Object[]{2}));
 		ll.add(Arrays.asList(new Object[]{1}));
 		
 		return ll;
 	}
 }
