 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BlockedByBzBug;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
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
 			groups={"AcceptanceTests","blockedByBug-660713"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void SubscribeToExpectedSubscriptionPoolProductId_Test(String productId, JSONArray bundledProductDataAsJSONArray) throws Exception {
 //if (!productId.equals("awesomeos-server")) throw new SkipException("debugging");
 		
 		// begin test with a fresh register
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 
 		// assert the subscription pool with the matching productId is available
 //		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool, "Expected SubscriptionPool with ProductId '"+productId+"' is available for subscribing.");
 
 		List<ProductCert> currentlyInstalledProductCerts = clienttasks.getCurrentProductCerts();
 		List<InstalledProduct> currentlyInstalledProducts = clienttasks.getCurrentlyInstalledProducts();
 		
 		// assert the installed status of the bundled products
 		for (int j=0; j<bundledProductDataAsJSONArray.length(); j++) {
 			JSONObject bundledProductAsJSONObject = (JSONObject) bundledProductDataAsJSONArray.get(j);
 //			String bundledProductName = bundledProductAsJSONObject.getString("productName");
 			String bundledProductId = bundledProductAsJSONObject.getString("productId");
 			
 			// assert the status of the installed products listed
 			for (ProductCert productCert : ProductCert.findAllInstancesWithMatchingFieldFromList("productId", bundledProductId, currentlyInstalledProductCerts)) {
 				InstalledProduct installedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", productCert.productName, currentlyInstalledProducts);
 				Assert.assertNotNull(installedProduct, "The status of installed product cert with ProductName '"+productCert.productName+"' is reported in the list of installed products.");
 				Assert.assertEquals(installedProduct.status, "Not Subscribed", "Before subscribing to pool for ProductId '"+productId+"', the status of Installed Product '"+productCert.productName+"' is Not Subscribed.");
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
 			String bundledProductName = bundledProductAsJSONObject.getString("productName");
 			String bundledProductId = bundledProductAsJSONObject.getString("productId");
 			
 			ProductSubscription productSubscription = ProductSubscription.findFirstInstanceWithMatchingFieldFromList("productName", bundledProductName, currentlyConsumedProductSubscriptions);
 			Assert.assertNotNull(productSubscription, "Expected ProductSubscription with ProductName '"+bundledProductName+"' is consumed after subscribing to pool for ProductId '"+productId+"'.");
 
 			// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=660713 - jsefler 12/12/2010
 			Boolean invokeWorkaroundWhileBugIsOpen = true;
 			try {String bugId="660713"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				log.warning("The workaround while this bug is open is to skip the assertion that: Consumed ProductSubscription Expires on the same DAY as the originating subscription pool.");
 			} else {
 			// END OF WORKAROUND
 				
 			// assert the dates match
 			Assert.assertEquals(ProductSubscription.formatDateString(productSubscription.endDate),ProductSubscription.formatDateString(pool.endDate),
 					"Consumed ProductSubscription Expires on the same DAY as the originating subscription pool.");
 			//FIXME	Assert.assertTrue(productSubscription.startDate.before(entitlementCert.validityNotBefore), "Consumed ProductSubscription Began before the validityNotBefore date of the new entitlement: "+entitlementCert);
 			}
 			
 			// find the corresponding productNamespace from the entitlementCert
 			ProductNamespace productNamespace = null;
 			for (ProductNamespace pn : entitlementCert.productNamespaces) {
 				if (pn.id.equals(bundledProductId)) productNamespace = pn;
 			}
 			
 			// assert the installed status of the corresponding product
 			if (entitlementCert.productNamespaces.isEmpty()) {
 				log.warning("This product '"+productId+"' ("+bundledProductName+") does not appear to grant entitlement to any client side content.  This must be a server side management add-on product. Asserting as such...");
 
 				Assert.assertEquals(entitlementCert.contentNamespaces.size(),0,
 						"When there are no productNamespaces in the entitlementCert, there should not be any contentNamespaces.");
 
 				// when there is no corresponding product, then there better not be an installed product status by the same product name
 				Assert.assertNull(InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", bundledProductName, currentlyInstalledProducts),
 						"Should not find any installed product status matching a server side management add-on productName: "+ bundledProductName);
 
 				// when there is no corresponding product, then there better not be an installed product cert by the same product name
 				Assert.assertNull(ProductCert.findFirstInstanceWithMatchingFieldFromList("productName", bundledProductName, currentlyInstalledProductCerts),
 						"Should not find any installed product certs matching a server side management add-on productName: "+ bundledProductName);
 
 			} else {
 				Assert.assertNotNull(productNamespace, "The new entitlement cert's product namespace corresponding to this expected ProductSubscription with ProductName '"+bundledProductName+"' was found.");
 				
 				// assert the status of the installed products listed
 				List <ProductCert> productCerts = ProductCert.findAllInstancesWithMatchingFieldFromList("productId", productNamespace.id, currentlyInstalledProductCerts);  // should be a list of one or empty
 				for (ProductCert productCert : productCerts) {
 					List <InstalledProduct> installedProducts = InstalledProduct.findAllInstancesWithMatchingFieldFromList("productName", productCert.productName, currentlyInstalledProducts);
 					Assert.assertEquals(installedProducts.size(),1, "The status of installed product '"+productCert.productName+"' should only be reported once in the list of installed products.");
 					InstalledProduct installedProduct = installedProducts.get(0);
 					
 					// decide what the status should be...  "Subscribed" or "Partially Subscribed"
 					String poolProductSocketsAttribute = CandlepinTasks.getPoolProductAttributeValue(sm_clientUsername, sm_clientPassword, sm_serverUrl, pool.poolId, "sockets");
 					if (poolProductSocketsAttribute!=null && Integer.valueOf(poolProductSocketsAttribute)<Integer.valueOf(clienttasks.sockets)) {
 						Assert.assertEquals(installedProduct.status, "Partially Subscribed", "After subscribing to a pool for ProductId '"+productId+"' (covers '"+poolProductSocketsAttribute+"' sockets), the status of Installed Product '"+bundledProductName+"' should be Partially Subscribed since a corresponding product cert was found in "+clienttasks.productCertDir+" and the machine's sockets value ("+clienttasks.sockets+") is greater than what a single subscription covers.");
 					} else {
 						Assert.assertEquals(installedProduct.status, "Subscribed", "After subscribing to a pool for ProductId '"+productId+"', the status of Installed Product '"+bundledProductName+"' is Subscribed since a corresponding product cert was found in "+clienttasks.productCertDir);
 					}
 					Assert.assertEquals(InstalledProduct.formatDateString(installedProduct.endDate), ProductSubscription.formatDateString(productSubscription.endDate), "Installed Product '"+bundledProductName+"' expires on the same DAY as the consumed ProductSubscription: "+productSubscription);
 				}
 				if (productCerts.isEmpty()) {
 					Assert.assertNull(InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", bundledProductName, currentlyInstalledProducts),"There should NOT be an installed status report for '"+bundledProductName+"' since a corresponding product cert was not found in "+clienttasks.productCertDir);
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
 	public void SubscribeConsumerToEachAvailableSubscriptionPoolUsingPoolId_Test(String username, String password, String owner){
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(username, password, owner, null, ConsumerType.system, null, null, Boolean.FALSE, (String)null, Boolean.FALSE, false, null, null, null);
 		clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsIndividually();
 	}
 	
 	
 	// TODO DELETE TEST due to https://bugzilla.redhat.com/show_bug.cgi?id=670823
 	@Test(	description="subscription-manager-cli: subscribe consumer to an entitlement using registration token",
 			groups={"blockedByBug-584137"},
 			enabled=false)
 	@ImplementsNitrateTest(caseId=41681)
 	public void SubscribeToRegToken_Test(){
 		clienttasks.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.subscribeToRegToken(sm_regtoken);
 	}
 	
 	
 	@Test(	description="Subscribed for Already subscribed Entitlement.",
 			groups={"blockedByBug-584137"},
 			dataProvider="getAvailableSubscriptionPoolsData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41897)
 	public void AttemptToSubscribeToAnAlreadySubscribedPool_Test(SubscriptionPool pool) throws JSONException, Exception{
 
 		//clienttasks.subscribeToSubscriptionPoolUsingProductId(pool);
 		Assert.assertNull(CandlepinTasks.getEntitlementSerialForSubscribedPoolId(sm_clientUsername, sm_clientPassword, sm_serverUrl, sm_clientOrg, pool.poolId),"Authenticator '"+sm_clientUsername+"' has not been granted any entitlements from pool '"+pool.poolId+"' under organization '"+sm_clientOrg+"'.");
 		Assert.assertNotNull(clienttasks.subscribeToSubscriptionPool_(pool),"Authenticator '"+sm_clientUsername+"' has been granted an entitlement from pool '"+pool.poolId+"' under organization '"+sm_clientOrg+"'.");
 		SSHCommandResult result = clienttasks.subscribe_(null,pool.poolId,null,null,null, null, null, null, null, null);
 
 		if (!CandlepinTasks.isPoolProductMultiEntitlement(sm_clientUsername,sm_clientPassword,sm_serverUrl,pool.poolId)) {
 			Assert.assertEquals(result.getStdout().trim(), "This consumer is already subscribed to the product matching pool with id '"+pool.poolId+"'.",
 				"subscribe command returns proper message when the same consumer attempts to subscribe to a non-multi-entitlement pool more than once.");
 		} else {
 			Assert.assertEquals(result.getStdout().trim(), "Successfully subscribed the system to Pool "+pool.poolId+"",
 				"subscribe command allows multi-entitlement pools to be subscribed to by the same consumer more than once.");		
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to multiple/duplicate/bad pools in one call",
 			groups={"blockedByBug-622851"},
 			enabled=true)
 	public void SubscribeToMultipleDuplicateAndBadPools_Test() throws JSONException, Exception {
 		
 		// begin the test with a cleanly registered system
 		clienttasks.unregister(null, null, null);
 	    clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 	    
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
 		SSHCommandResult subscribeResult = clienttasks.subscribe_(null, poolIds, null, null, null, null, null, null, null, null);
 		
 		/*
 		No such entitlement pool: bad123
 		Successfully subscribed the system to Pool 8a90f8c63159ce55013159cfd6c40303
 		This consumer is already subscribed to the product matching pool with id '8a90f8c63159ce55013159cfd6c40303'.
 		Successfully subscribed the system to Pool 8a90f8c63159ce55013159cfea7a06ac
 		Successfully subscribed the system to Pool 8a90f8c63159ce55013159cfea7a06ac
 		No such entitlement pool: bad_POOLID
 		*/
 		
 		// assert the results...
 		Assert.assertEquals(subscribeResult.getExitCode(), Integer.valueOf(0), "The exit code from the subscribe command indicates a success.");
 		for (String poolId : poolIds) {
 			String subscribeResultMessage;
 			if (poolId.equals(badPoolId1) || poolId.equals(badPoolId2)) {
 				//subscribeResultMessage = "No such entitlement pool: "+poolId;
 				subscribeResultMessage = "Subscription pool "+poolId+" does not exist.";
 				Assert.assertTrue(subscribeResult.getStdout().contains(subscribeResultMessage),"The subscribe result for an invalid pool '"+poolId+"' contains: "+subscribeResultMessage);
 			}
 			else if (CandlepinTasks.isPoolProductMultiEntitlement(sm_clientUsername,sm_clientPassword,sm_serverUrl,poolId)) {
 				subscribeResultMessage = "Successfully subscribed the system to Pool "+poolId;
 				subscribeResultMessage += "\n"+subscribeResultMessage;
 				Assert.assertTrue(subscribeResult.getStdout().contains(subscribeResultMessage),"The duplicate subscribe result for a multi-entitlement pool '"+poolId+"' contains: "+subscribeResultMessage);
 			} else if (false) {
 				// TODO case when there are no entitlements remaining for the duplicate subscribe
 			} else {
 				subscribeResultMessage = "Successfully subscribed the system to Pool "+poolId;
 				subscribeResultMessage += "\n"+"This consumer is already subscribed to the product matching pool with id '"+poolId+"'.";
 				Assert.assertTrue(subscribeResult.getStdout().contains(subscribeResultMessage),"The duplicate subscribe result for pool '"+poolId+"' contains: "+subscribeResultMessage);			
 			}
 		}
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
 		clienttasks.restart_rhsmcertd(minutes, null, false); sleep(10000); // allow 10sec for the initial update
 		log.info("Appending a marker in the '"+clienttasks.rhsmcertdLogFile+"' so we can assert that the certificates are being updated every '"+minutes+"' minutes");
 		String marker = "Testing rhsm.conf certFrequency="+minutes+" when unregistered..."; // https://tcms.engineering.redhat.com/case/41692/
 		RemoteFileTasks.runCommandAndAssert(client,"echo \""+marker+"\" >> "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0));
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0),marker,null);
 		sleep(minutes*60*1000);	// give the rhsmcertd a chance to check in with the candlepin server and update the certs
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0),"update failed \\(\\d+\\), retry in "+minutes+" minutes",null);
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmLogFile,Integer.valueOf(0),errorMsg,null);
 		
 		
 		log.info("Now test with a registered user whose identity is corrupt and verify that the rhsmcertd actually fails since it cannot self-identify itself to the candlepin server.");
 		String consumerid = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null));
 		log.info("Corrupting the identity cert by borking its content...");
 		RemoteFileTasks.runCommandAndAssert(client, "openssl x509 -noout -text -in "+clienttasks.consumerCertFile+" > /tmp/stdout; mv /tmp/stdout -f "+clienttasks.consumerCertFile, 0);
 		clienttasks.restart_rhsmcertd(minutes, null, false); sleep(10000); // allow 10sec for the initial update
 		log.info("Appending a marker in the '"+clienttasks.rhsmcertdLogFile+"' so we can assert that the certificates are being updated every '"+minutes+"' minutes");
 		marker = "Testing rhsm.conf certFrequency="+minutes+" when identity is corrupted...";
 		RemoteFileTasks.runCommandAndAssert(client,"echo \""+marker+"\" >> "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0));
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0),marker,null);
 		sleep(minutes*60*1000);	// give the rhsmcertd a chance to check in with the candlepin server and update the certs
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmcertdLogFile,Integer.valueOf(0),"update failed \\(\\d+\\), retry in "+minutes+" minutes",null);
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.rhsmLogFile,Integer.valueOf(0),errorMsg,null);
 
 		
 		log.info("Finally test with a registered user and verify that the rhsmcertd succeeds because he can identify himself to the candlepin server.");
 	    clienttasks.register(sm_clientUsername, sm_clientPassword, null, null, null, null, consumerid, null, (String)null, Boolean.TRUE, false, null, null, null);
 		clienttasks.restart_rhsmcertd(minutes, null, false); sleep(10000); // allow 10sec for the initial update
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
 	public void rhsmcertdEnsureCertificatesSynchronize_Test() throws JSONException, Exception{
 		
 		// start with a cleanly unregistered system
 		clienttasks.unregister(null, null, null);
 		
 		// register a clean user
 	    clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 	    
 	    // subscribe to all the available pools
 	    clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively();
 	    
 	    // get all of the current entitlement certs and remember them
 	    List<File> entitlementCertFiles = clienttasks.getCurrentEntitlementCertFiles();
 	    
 	    // delete all of the entitlement cert files
 	    client.runCommandAndWait("rm -rf "+clienttasks.entitlementCertDir+"/*");
 	    Assert.assertEquals(clienttasks.getCurrentEntitlementCertFiles().size(), 0,
 	    		"All the entitlement certs have been deleted.");
 		
 	    // restart the rhsmcertd to run every 1 minute and wait for a refresh
 		clienttasks.restart_rhsmcertd(1, null, true);
 		
 		// assert that rhsmcertd has refreshed the entitlement certs back to the original
 	    Assert.assertEquals(clienttasks.getCurrentEntitlementCertFiles(), entitlementCertFiles,
 	    		"All the deleted entitlement certs have been re-synchronized by rhsm cert deamon.");
 	}
 	
 	
 	@Test(	description="subscription-manager: make sure the available pools come from subscriptions that pass the hardware rules for availability.",
 			groups={"AcceptanceTests"},
 			dependsOnGroups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	// Note: The objective if this test is essentially the same as ListTests.EnsureHardwareMatchingSubscriptionsAreListedAsAvailable_Test() and ListTests.EnsureNonHardwareMatchingSubscriptionsAreNotListedAsAvailable_Test(), but its implementation is slightly different
 	public void VerifyAvailablePoolsPassTheHardwareRulesCheck_Test() throws Exception {
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, true, false, null, null, null);
 
 		List<List<Object>> subscriptionPoolProductData = getSystemSubscriptionPoolProductDataAsListOfLists();
 		List<SubscriptionPool> availableSubscriptionPoolsBeforeAutosubscribe = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		for (List<Object> subscriptionPoolProductDatum : subscriptionPoolProductData) {
 			String productId = (String)subscriptionPoolProductDatum.get(0);
 			SubscriptionPool subscriptionPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, availableSubscriptionPoolsBeforeAutosubscribe);
 			Assert.assertNotNull(subscriptionPool, "Expecting SubscriptionPool with ProductId '"+productId+"' to be available to registered user '"+sm_clientUsername+"'.");
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
 			groups={"AcceptanceTests","AutoSubscribeAndVerify", "blockedByBug-680399", "blockedByBug-734867", "blockedByBug-740877"},
 			dependsOnMethods={"VerifyAvailablePoolsPassTheHardwareRulesCheck_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void InititiateAutoSubscribe_Test() throws Exception {
 
 		// before testing, make sure all the expected subscriptionPoolProductId are available
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		
 		// get the expected subscriptionPoolProductIdData
 		subscriptionPoolProductData = getSystemSubscriptionPoolProductDataAsListOfLists(false);
 		
 		// autosubscribe
 		sshCommandResultFromAutosubscribe = clienttasks.subscribe(Boolean.TRUE,(String)null,null,null,null,null,null,null,null, null);
 		
 		/* RHEL57 RHEL61 Example Results...
 		# subscription-manager subscribe --auto
 		Installed Products:
 		   Multiplier Product Bits - Not Subscribed
 		   Load Balancing Bits - Subscribed
 		   Awesome OS Server Bits - Subscribed
 		   Management Bits - Subscribed
 		   Awesome OS Scalable Filesystem Bits - Subscribed
 		   Shared Storage Bits - Subscribed
 		   Large File Support Bits - Subscribed
 		   Awesome OS Workstation Bits - Subscribed
 		   Awesome OS Premium Architecture Bits - Not Subscribed
 		   Awesome OS for S390X Bits - Not Subscribed
 		   Awesome OS Developer Basic - Not Subscribed
 		   Clustering Bits - Subscribed
 		   Awesome OS Developer Bits - Not Subscribed
 		   Awesome OS Modifier Bits - Subscribed
 		*/
 		
 		/* Example Results...
 		# subscription-manager subscribe --auto
 		Installed Product Current Status:
 		
 		ProductName:         	Awesome OS for x86_64/ALL Bits for ZERO sockets
 		Status:               	Subscribed               
 		
 		
 		ProductName:         	Awesome OS for x86_64/ALL Bits
 		Status:               	Subscribed               
 		
 		
 		ProductName:         	Awesome OS for ppc64 Bits
 		Status:               	Subscribed               
 		
 		
 		ProductName:         	Awesome OS for i386 Bits 
 		Status:               	Subscribed               
 		
 		
 		ProductName:         	Awesome OS for x86 Bits  
 		Status:               	Subscribed               
 		
 		
 		ProductName:         	Awesome OS for ia64 Bits 
 		Status:               	Subscribed               
 		
 		
 		ProductName:         	Awesome OS Scalable Filesystem Bits
 		Status:               	Subscribed               
 		*/
 	}
 	protected List<List<Object>> subscriptionPoolProductData;
 	
 	
 	@Test(	description="subscription-manager-cli: autosubscribe consumer and verify expected subscription pool product id are consumed",
 			groups={"AcceptanceTests","AutoSubscribeAndVerify","blockedByBug-672438","blockedByBug-678049","blockedByBug-743082"},
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
 		// RHEL57 RHEL61		Assert.assertContainsMatch(sshCommandResultFromAutosubscribe.getStdout().trim(), "^\\s+"+productCert.productName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"+" - "+expectedSubscribeStatus),
 		//		"As expected, ProductName '"+productCert.productName+"' was reported as '"+expectedSubscribeStatus+"' in the output from register with autotosubscribe.");
 		List<InstalledProduct> autosubscribedProductStatusList = InstalledProduct.parse(sshCommandResultFromAutosubscribe.getStdout());
 		InstalledProduct autosubscribedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", productCert.productName, autosubscribedProductStatusList);
 		Assert.assertEquals(autosubscribedProduct.status,expectedSubscribeStatus,
 				"As expected, ProductName '"+productCert.productName+"' was reported as '"+expectedSubscribeStatus+"' in the output from register with autotosubscribe.");
 	}
 //	List<SubscriptionPool> availableSubscriptionPoolsBeforeAutosubscribe;
 	SSHCommandResult sshCommandResultFromAutosubscribe;
 	
 	
 	@Test(	description="subscription-manager: autosubscribe consumer more than once and verify we are not duplicately subscribed",
 			groups={"blockedByBug-723044","blockedByBug-743082"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void SubscribeWithAutoMoreThanOnce_Test() throws Exception {
 
 		// before testing, make sure all the expected subscriptionPoolProductId are available
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, true, false, null, null, null);
 		
 		// get the expected subscriptionPoolProductIdData
 		subscriptionPoolProductData = getSystemSubscriptionPoolProductDataAsListOfLists(false);
 		
 		// autosubscribe once
 		SSHCommandResult result1 = clienttasks.subscribe(Boolean.TRUE,(String)null,null,null,null,null,null,null,null, null);
 		List<File> entitlementCertFiles1 = clienttasks.getCurrentEntitlementCertFiles();
 		List<InstalledProduct> autosubscribedProductStatusList1 = InstalledProduct.parse(result1.getStdout());
 		
 		// autosubscribe twice
 		SSHCommandResult result2 = clienttasks.subscribe(Boolean.TRUE,(String)null,null,null,null,null,null,null,null, null);
 		List<File> entitlementCertFiles2 = clienttasks.getCurrentEntitlementCertFiles();
 		List<InstalledProduct> autosubscribedProductStatusList2 = InstalledProduct.parse(result2.getStdout());
 		
 		// assert results
 		Assert.assertEquals(entitlementCertFiles2.size(), entitlementCertFiles1.size(), "The number of granted entitlement certs is the same after a second autosubscribe.");
 		Assert.assertEquals(autosubscribedProductStatusList2.size(), autosubscribedProductStatusList1.size(), "The stdout from autosubscribe reports the same number of installed product status entries after a second autosubscribe.");
 		Assert.assertTrue(autosubscribedProductStatusList1.containsAll(autosubscribedProductStatusList2), "The list of installed product status entries from a second autosubscribe is the same as the first.");
 	}
 	
 	
 	@Test(	description="subscription-manager: subscribe using various good and bad values for the --quantity option",
 			groups={"AcceptanceTests"},
 			dataProvider="getSubscribeWithQuantityData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void SubscribeWithQuantity_Test(Object meta, SubscriptionPool pool, String quantity, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		log.info("Testing subscription-manager subscribe using various good and bad values for the --quantity option.");
 		if(pool==null) throw new SkipException(expectedStderrRegex);	// special case in the dataProvider to identify when a test pool was not available; expectedStderrRegex contains a message for what kind of test pool was being searched for.
 	
 		// start fresh by returning all subscriptions
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		// subscribe with quantity
 		SSHCommandResult sshCommandResult = clienttasks.subscribe_(null,pool.poolId,null,null,quantity,null,null,null,null, null);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode,"ExitCode after subscribe with quantity=\""+quantity+"\" option:");
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex,"Stdout after subscribe with --quantity=\""+quantity+"\" option:");
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex,"Stderr after subscribe with --quantity=\""+quantity+"\" option:");
 		
 		// when successful, assert that the quantity is correctly reported in the list of consumed subscriptions
 		List<ProductSubscription> subscriptionsConsumed = client1tasks.getCurrentlyConsumedProductSubscriptions();
 		List<EntitlementCert> entitlementCerts = client1tasks.getCurrentEntitlementCerts();
 		if (expectedExitCode==0 && expectedStdoutRegex!=null && expectedStdoutRegex.contains("Successful")) {
 			Assert.assertEquals(entitlementCerts.size(), 1, "One EntitlementCert should have been downloaded to "+client1tasks.hostname+" when the attempt to subscribe is successful.");
 			Assert.assertEquals(entitlementCerts.get(0).orderNamespace.quantityUsed, quantity, "The quantityUsed in the OrderNamespace of the downloaded EntitlementCert should match the quantity requested when we subscribed to pool '"+pool.poolId+"'.  OrderNamespace: "+entitlementCerts.get(0).orderNamespace);
 			for (ProductSubscription productSubscription : subscriptionsConsumed) {
 				Assert.assertEquals(productSubscription.quantityUsed, Integer.valueOf(quantity), "The quantityUsed reported in each consumed ProductSubscription should match the quantity requested when we subscribed to pool '"+pool.poolId+"'.  ProductSubscription: "+productSubscription);
 			}
 		} else {
 			Assert.assertEquals(subscriptionsConsumed.size(), 0, "No subscriptions should be consumed when the attempt to subscribe is not successful.");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager: subscribe using --quantity option and assert the available quantity is properly decremented/incremeneted as multiple consumers subscribe/unsubscribe.",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void MultiConsumerSubscribeWithQuantity_Test() throws NumberFormatException, JSONException, Exception {
 		
 		// start by calling SubscribeWithQuantity_Test with the row from the dataProvider where quantity=2
 		SubscriptionPool consumer1Pool = null;
 		int consumer1Quantity=0;
 		int totalPoolQuantity=0;
 		for (List<Object> row : getSubscribeWithQuantityDataAsListOfLists()) {
 			if (((String)(row.get(2))).equals("2") && ((String)(row.get(4))).startsWith("^Successful")) {	// find the row where quantity.equals("2")
 				consumer1Pool = (SubscriptionPool) row.get(1);
 				totalPoolQuantity = Integer.valueOf(consumer1Pool.quantity);
 				consumer1Quantity = Integer.valueOf((String) row.get(2));
 				SubscribeWithQuantity_Test(row.get(0), (SubscriptionPool)row.get(1), (String)row.get(2), (Integer)row.get(3), (String)row.get(4), (String)row.get(5));
 				break;
 			}
 		}
 		if (consumer1Pool==null) Assert.fail("Failed to initiate the first consumer for this test.");
 		
 		// remember the current consumerId
 		String consumer1Id = clienttasks.getCurrentConsumerId(); systemConsumerIds.add(consumer1Id);
 		
 		// clean the client and register a second consumer
 		clienttasks.clean(null,null,null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, "SubscriptionQuantityConsumer2", null, null, (String)null, false, false, null, null, null);
 		
 		// remember the second consumerId
 		String consumer2Id = clienttasks.getCurrentConsumerId(); systemConsumerIds.add(consumer2Id);
 		
 		// find the pool among the available pools
 		SubscriptionPool consumer2Pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", consumer1Pool.poolId, clienttasks.getCurrentlyAvailableSubscriptionPools()); 
 		Assert.assertNotNull(consumer2Pool,"Consumer2 found the same pool from which consumer1 subscribed a quantity of "+consumer1Quantity);
 
 		// assert that the quantity available to consumer2 is correct
 		int consumer2Quantity = totalPoolQuantity-consumer1Quantity;
 		Assert.assertEquals(consumer2Pool.quantity, String.valueOf(consumer2Quantity),"The pool quantity available to consumer2 has been decremented by the quantity consumer1 consumed.");
 		
 		// assert that consumer2 can NOT oversubscribe
 		Assert.assertTrue(!clienttasks.subscribe(null,consumer2Pool.poolId,null,null,String.valueOf(consumer2Quantity+1),null,null,null,null, null).getStdout().startsWith("Success"),"An attempt by consumer2 to oversubscribe using the remaining pool quantity+1 should NOT succeed.");
 
 		// assert that consumer2 can successfully consume all the remaining pool quantity
 		Assert.assertTrue(clienttasks.subscribe(null,consumer2Pool.poolId,null,null,String.valueOf(consumer2Quantity),null,null,null,null, null).getStdout().startsWith("Success"),"An attempt by consumer2 to exactly consume the remaining pool quantity should succeed.");
 		
 		// start rolling back the subscribes
 		
 		// restore consumer1, unsubscribe, and assert remaining quantities
 		clienttasks.clean(null,null,null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, null, null, null, null, consumer1Id, null, (String)null, false, false, null, null, null);
 		Assert.assertNull(SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", consumer1Pool.poolId, clienttasks.getCurrentlyAvailableSubscriptionPools()),"SubscriptionPool '"+consumer1Pool.poolId+"' should NOT be available (because consumer1 is already subscribed to it).");
 		clienttasks.unsubscribe(null,clienttasks.getCurrentlyConsumedProductSubscriptions().get(0).serialNumber,null,null,null);
 		consumer1Pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", consumer1Pool.poolId, clienttasks.getCurrentlyAvailableSubscriptionPools()); 
 		Assert.assertEquals(consumer1Pool.quantity, String.valueOf(totalPoolQuantity-consumer2Quantity),"The pool quantity available to consumer1 has incremented by the quantity consumer1 consumed.");
 		
 		// restore consumer2, unsubscribe, and assert remaining quantities
 		clienttasks.register(sm_clientUsername, sm_clientPassword, null, null, null, null, consumer2Id, null, (String)null, true, false, null, null, null);
 		consumer2Pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", consumer2Pool.poolId, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		//Assert.assertNull(consumer2Pool,"SubscriptionPool '"+consumer2Pool.poolId+"' should NOT be available (because consumer2 is already subscribed to it).");
 		Assert.assertNotNull(consumer2Pool,"SubscriptionPool '"+consumer2Pool.poolId+"' should be available even though consumer2 is already subscribed to it because it is multi-entitleable.");
 		Assert.assertEquals(consumer2Pool.quantity, String.valueOf(totalPoolQuantity-consumer2Quantity),"The pool quantity available to consumer2 is still decremented by the quantity consumer2 consumed.");
 		clienttasks.unsubscribe(null,clienttasks.getCurrentlyConsumedProductSubscriptions().get(0).serialNumber,null,null,null);
 		consumer2Pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", consumer2Pool.poolId, clienttasks.getCurrentlyAvailableSubscriptionPools()); 
 		Assert.assertEquals(consumer2Pool.quantity, String.valueOf(totalPoolQuantity),"The pool quantity available to consumer2 has been restored to its original total quantity");
 	}
 	
 	
 	@Test(	description="subscription-manager: subscribe to multiple pools using --quantity that exceeds some pools and is under other pools.",
 			groups={"blockedByBug-722975"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void SubscribeWithQuantityToMultiplePools_Test() throws NumberFormatException, JSONException, Exception {
 		
 		// register
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, true, false, null, null, null);
 		
 		// get all the available pools
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		List<String> poolIds = new ArrayList<String>();
 		
 		// find the poolIds and their quantities
 		List<Integer> quantities = new ArrayList<Integer>();
 		for (SubscriptionPool pool : pools) {
 			poolIds.add(pool.poolId);
 			quantities.add(Integer.valueOf(pool.quantity));
 		}
 		Collections.sort(quantities);
 		int quantity = quantities.get(quantities.size()/2);	// choose the median as the quantity to subscribe with
 		
 		// collectively subscribe to all pools with --quantity
 		SSHCommandResult subscribeResult = clienttasks.subscribe_(null, poolIds, null, null, String.valueOf(quantity), null, null, null, null, null);
 		
 		/*
 		Multi-entitlement not supported for pool with id '8a90f8c6320e9a4401320e9be0e20480'.
 		Successfully subscribed the system to Pool 8a90f8c6320e9a4401320e9be196049e
 		No free entitlements are available for the pool with id '8a90f8c6320e9a4401320e9be1d404a8'.
 		Multi-entitlement not supported for pool with id '8a90f8c6320e9a4401320e9be24004be'.
 		Successfully subscribed the system to Pool 8a90f8c6320e9a4401320e9be2e304dd
 		No free entitlements are available for the pool with id '8a90f8c6320e9a4401320e9be30c04e8'.
 		Multi-entitlement not supported for pool with id '8a90f8c6320e9a4401320e9be3b80505'.
 		Multi-entitlement not supported for pool with id '8a90f8c6320e9a4401320e9be4660520'.
 		*/
 		
 		// assert that the expected pools were subscribed to based on quantity
 		Assert.assertEquals(subscribeResult.getExitCode(), Integer.valueOf(0), "The exit code from the subscribe command indicates a success.");
 		for (SubscriptionPool pool : pools) {
 			if (quantity>1 && !CandlepinTasks.isPoolProductMultiEntitlement(sm_clientUsername, sm_clientPassword, sm_serverUrl, pool.poolId)) {
 				Assert.assertTrue(subscribeResult.getStdout().contains("Multi-entitlement not supported for pool with id '"+pool.poolId+"'."),"Subscribe attempt to non-multi-entitlement pool '"+pool.poolId+"' was NOT successful when subscribing with --quantity greater than one.");				
 			} else if (quantity <= Integer.valueOf(pool.quantity)) {
 				//Assert.assertContainsMatch(result.getStdout(), "^Successfully subscribed the system to Pool "+pool.poolId+"$","Subscribe should be successful when subscribing with --quantity less than or equal to the pool's availability.");
 				Assert.assertTrue(subscribeResult.getStdout().contains("Successfully subscribed the system to Pool "+pool.poolId),"Subscribe to pool '"+pool.poolId+"' was successful when subscribing with --quantity less than or equal to the pool's availability.");
 			} else {
 				//Assert.assertContainsMatch(result.getStdout(), "^No free entitlements are available for the pool with id '"+pool.poolId+"'.$","Subscribe should NOT be successful when subscribing with --quantity greater than the pool's availability.");
 				Assert.assertTrue(subscribeResult.getStdout().contains("No free entitlements are available for the pool with id '"+pool.poolId+"'."),"Subscribe to pool '"+pool.poolId+"' was NOT successful when subscribing with --quantity greater than the pool's availability.");
 			}
 		}
 	}
 	
 		
 	@Test(	description="subscription-manager: subscribe to future subscription pool",
 			groups={},
 			dataProvider="getAllFutureSystemSubscriptionPoolsData",
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void SubscribeToFutureSubscriptionPool_Test(SubscriptionPool pool) throws Exception {
 		
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(System.currentTimeMillis());
 		
 		// subscribe to the future subscription pool
 		SSHCommandResult subscribeResult = clienttasks.subscribe(null,pool.poolId,null,null,null,null,null,null,null,null);
 
 		// assert that the granted entitlement cert begins in the future
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertCorrespondingToSubscribedPool(pool);
 		Assert.assertNotNull(entitlementCert,"Found the newly granted EntitlementCert on the client after subscribing to future subscription pool '"+pool.poolId+"'.");
 		Assert.assertTrue(entitlementCert.validityNotBefore.after(now), "The newly granted EntitlementCert is not valid until the future.  EntitlementCert: "+entitlementCert);
 		Assert.assertTrue(entitlementCert.orderNamespace.startDate.after(now), "The newly granted EntitlementCert's OrderNamespace starts in the future.  OrderNamespace: "+entitlementCert.orderNamespace);	
 	}
 		
 	
 	// Candidates for an automated Test:
 	// TODO Bug 668032 - rhsm not logging subscriptions and products properly
 	// TODO Bug 670831 - Entitlement Start Dates should be the Subscription Start Date
 	// TODO Bug 664847 - Autobind logic should respect the architecture attribute
 	// TODO Bug 676377 - rhsm-compliance-icon's status can be a day out of sync - could use dbus-monitor to assert that the dbus message is sent on the expected compliance changing events
 	// TODO Bug 739790 - Product "RHEL Workstation" has a valid stacking_id but its socket_limit is 0
 	// TODO Bug 707641 - CLI auto-subscribe tries to re-use basic auth credentials.
 	
 	// TODO Write an autosubscribe bug... 1. Subscribe to all avail and note the list of installed products (Subscribed, Partially, Not) 2. Unsubscribe all  3. Autosubscribe and verfy same installed product status (Subscribed, Not)
 	// TODO Bug 746035 - autosubscribe should NOT consider existing future entitlements when determining what pools and quantity should be autosubscribed 
 	// TODO Bug 747399 - if consumer does not have architecture then we should not check for it
	// TODO Bug 743704 - autosubscribe ignores socket count on non multi-entitle subscriptions
 	
 	
 	// Configuration Methods ***********************************************************************
 	
 	@AfterClass(groups={"setup"})
 	public void unregisterAllSystemConsumerIds() {
 		if (clienttasks!=null) {
 			for (String systemConsumerId : systemConsumerIds) {
 				clienttasks.register_(sm_clientUsername,sm_clientPassword,null,null,null,null,systemConsumerId, null, (String)null, Boolean.TRUE, null, null, null, null);
 				clienttasks.unsubscribe_(Boolean.TRUE, null, null, null, null);
 				clienttasks.unregister_(null, null, null);
 			}
 			systemConsumerIds.clear();
 		}
 	}
 	
 
 
 	
 	// Protected Methods ***********************************************************************
 
 	protected List<String> systemConsumerIds = new ArrayList<String>();
 	
 	
 
 	
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
 	
 	
 	@DataProvider(name="getSubscribeWithQuantityData")
 	public Object[][] getSubscribeWithQuantityDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getSubscribeWithQuantityDataAsListOfLists());
 	}
 	protected List<List<Object>>getSubscribeWithQuantityDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		
 		// register
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, "SubscriptionQuantityConsumer", null, null, (String)null, true, false, null, null, null);
 		
 //		// find a random testpool with a positive quantity
 //		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 //		SubscriptionPool testPool;
 //		int i = 1000;	// avoid an infinite loop
 //		do {
 //			testPool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 //		} while (!testPool.quantity.equalsIgnoreCase("unlimited") && Integer.valueOf(testPool.quantity)<2 && /*avoid an infinite loop*/i-->0);
 
 		
 		// find pools with a positive quantity that have a productAttribute set for "multi-entitlement"
 		SubscriptionPool poolWithMultiEntitlementNull = null;
 		SubscriptionPool poolWithMultiEntitlementYes = null;
 		SubscriptionPool poolWithMultiEntitlementNo = null;
 		for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			if (!pool.quantity.equalsIgnoreCase("unlimited") && Integer.valueOf(pool.quantity)<2) continue;	// skip pools that don't have enough quantity left to consume
 			
 			Boolean isMultiEntitlementPool = null;	// indicates that the pool's product does NOT have the "multi-entitlement" attribute
 			JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/pools/"+pool.poolId));	
 			JSONArray jsonProductAttributes = jsonPool.getJSONArray("productAttributes");
 			// loop through the productAttributes of this pool looking for the "multi-entitlement" attribute
 			for (int j = 0; j < jsonProductAttributes.length(); j++) {
 				JSONObject jsonProductAttribute = (JSONObject) jsonProductAttributes.get(j);
 				String productAttributeName = jsonProductAttribute.getString("name");
 				if (productAttributeName.equals("multi-entitlement")) {
 					//multi_entitlement = jsonProductAttribute.getBoolean("value");
 					isMultiEntitlementPool = jsonProductAttribute.getString("value").equalsIgnoreCase("yes") || jsonProductAttribute.getString("value").equals("1");
 					break;
 				}
 			}
 			
 			if (isMultiEntitlementPool == null) {
 				poolWithMultiEntitlementNull = pool;
 			} else if (isMultiEntitlementPool) {
 				poolWithMultiEntitlementYes = pool;
 			} else {
 				poolWithMultiEntitlementNo = pool;
 			}
 		}
 		SubscriptionPool pool;
 		
 		
 		// Object meta, String poolId, String quantity, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 
 		pool= poolWithMultiEntitlementYes;
 		if (pool!=null) {
 			ll.add(Arrays.asList(new Object[] {null,							pool,	"Two",												Integer.valueOf(255),	"^Error: Quantity must be a positive number.$",	null}));
 			ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("722554"),	pool,	"-1",												Integer.valueOf(255),	"^Error: Quantity must be a positive number.$",	null}));
 			ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("722554"),	pool,	"0",												Integer.valueOf(255),	"^Error: Quantity must be a positive number.$",	null}));
 			ll.add(Arrays.asList(new Object[] {null,							pool,	"1",												Integer.valueOf(0),		"^Successfully subscribed the system to Pool "+pool.poolId+"$",	null}));
 			ll.add(Arrays.asList(new Object[] {null,							pool,	"2",												Integer.valueOf(0),		"^Successfully subscribed the system to Pool "+pool.poolId+"$",	null}));
 			ll.add(Arrays.asList(new Object[] {null,							pool,	pool.quantity,										Integer.valueOf(0),		"^Successfully subscribed the system to Pool "+pool.poolId+"$",	null}));
 			ll.add(Arrays.asList(new Object[] {null,							pool,	String.valueOf(Integer.valueOf(pool.quantity)+1),	Integer.valueOf(0),		"^No free entitlements are available for the pool with id '"+pool.poolId+"'.$",	null}));
 			ll.add(Arrays.asList(new Object[] {null,							pool,	String.valueOf(Integer.valueOf(pool.quantity)*10),	Integer.valueOf(0),		"^No free entitlements are available for the pool with id '"+pool.poolId+"'.$",	null}));
 		} else {
 			ll.add(Arrays.asList(new Object[] {null,	null,	null,	null,	null,	"Could NOT find an available subscription pool with a \"multi-entitlement\" product attribute set to yes."}));
 		}
 		
 		pool= poolWithMultiEntitlementNo;
 		if (pool!=null) {
 			ll.add(Arrays.asList(new Object[] {null,							pool,	"1",												Integer.valueOf(0),		"^Successfully subscribed the system to Pool "+pool.poolId+"$",	null}));
 			ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("722975"),	pool,	"2",												Integer.valueOf(0),		"^Multi-entitlement not supported for pool with id '"+pool.poolId+"'.$",	null}));
 		} else {
 			ll.add(Arrays.asList(new Object[] {null,	null,	null,	null,	null,	"Could NOT find an available subscription pool with a \"multi-entitlement\" product attribute set to no."}));
 		}
 		
 		pool= poolWithMultiEntitlementNull;
 		if (pool!=null) {
 			ll.add(Arrays.asList(new Object[] {null,							pool,	"1",												Integer.valueOf(0),		"^Successfully subscribed the system to Pool "+pool.poolId+"$",	null}));
 			ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("722975"),	pool,	"2",												Integer.valueOf(0),		"^Multi-entitlement not supported for pool with id '"+pool.poolId+"'.$",	null}));
 		} else {
 			ll.add(Arrays.asList(new Object[] {null,	null,	null,	null,	null,	"Could NOT find an available subscription pool without a \"multi-entitlement\" product attribute."}));
 		}
 		
 		return ll;
 	}
 	
 }
