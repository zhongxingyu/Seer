 package com.redhat.qe.sm.cli.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.SubscriptionPool;
 
 
 /**
  * @author jsefler
  * </BR>
  * Design Reference: https://engineering.redhat.com/trac/Entitlement/wiki/EUSDesign
  */
 @Test(groups="ModifierTests")
 public class ModifierTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 	
 
 	@Test(	description="verify content label for modifier subscription (EUS) is only available in yum repolist when providing subscriptions are entitled",
 			groups={},
 			dependsOnGroups={},
 			dataProvider="getModifierSubscriptionData",
 			enabled=true)
 	public void VerifyContentLabelForModifierSubscriptionIsOnlyAvailableInYumRepoListWhenProvidingPoolsAreSubscribed(SubscriptionPool modifierPool, String label, List<String> modifiedProductIds, String requiredTags, List<SubscriptionPool> providingPools) {
 		
 		// make sure we are not subscribed to anything
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 
 		boolean areAllRequiredTagsProvided = clienttasks.areAllRequiredTagsProvidedByProductCerts(requiredTags, clienttasks.getCurrentProductCerts());
 
 		log.info("Before subscribing to anything, assert that the label (repo id) '"+label+"' is not available.");
 		Assert.assertFalse(clienttasks.yumRepolist("all").contains(label),
 				"Before beginning our test, yum repolist all excludes label (repo id) '"+label+"'.");
 
 		log.info("Now subscribe to the modifier pool and assert that the label (repo id) '"+label+"' is still not available.");
 		clienttasks.subscribeToSubscriptionPool(modifierPool);
 		Assert.assertFalse(clienttasks.yumRepolist("all").contains(label),
 				"After subscribing to modifier pool for productId '"+modifierPool.productId+"', yum repolist all does not include (repo id) '"+label+"' because at least one of the providing product subscription(s) being modified is not yet subscribed to.");
 
 		log.info("Now individually subscribe to each of the subscribing products being modified and assert that once both the modifier pool and product subscription being modified are both subscribed, then the modifier (repo id) '"+label+"' will become available.");
 		for (SubscriptionPool providingPool : providingPools) {
 			EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(clienttasks.subscribeToSubscriptionPool(providingPool));
 			if (areAllRequiredTagsProvided) {
 				Assert.assertTrue(clienttasks.yumRepolist("all").contains(label),
 					"Having subscribed to both the modifier pool and its providing pool for productId '"+providingPool.productId+"', now the modifier pool's (repo id) '"+label+"' is available in yum repolist all.");
 			} else {
 				Assert.assertFalse(clienttasks.yumRepolist("all").contains(label),
 						"Because not all of the requiredTags '"+requiredTags+"' for content label '"+label+"' are not 100% provided by the currently installed product certs, we are blocked from seeing the repo id label '"+label+"' in yum repolist all.");				
 			}
 			clienttasks.unsubscribeFromSerialNumber(entitlementCert.serialNumber);
 			Assert.assertFalse(clienttasks.yumRepolist("all").contains(label),
 					"After unsubscribing from the providing pool for productId '"+providingPool.productId+"', yum repolist all no longer includes (repo id) '"+label+"' from modifier productId '"+modifierPool.productId+"'.");
 		}
 		
 		log.info("Now let's subscribe to the providing pools first before subscribing to the modifier.");
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		Assert.assertFalse(clienttasks.yumRepolist("all").contains(label),
 				"Yum repolist now excludes label (repo id) '"+label+"' since we are not subscribed to anything.");
 //		for (SubscriptionPool providingPool : providingPools) {
 //			clienttasks.subscribeToSubscriptionPool(providingPool);
 //		}
 		List<String> providingPoolIds = new ArrayList<String>();
 		for (SubscriptionPool providingPool : providingPools) providingPoolIds.add(providingPool.poolId);
 		clienttasks.subscribe(providingPoolIds, null, null, null, null, null, null, null);
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(clienttasks.subscribeToSubscriptionPool(modifierPool));
 		if (areAllRequiredTagsProvided) {
 			Assert.assertTrue(clienttasks.yumRepolist("all").contains(label),
 					"Having subscribed to all of its possible providing pools and the modifier pool, the modifier pool's (repo id) '"+label+"' is immediately be available in yum repolist all.");
 		} else {
 			Assert.assertFalse(clienttasks.yumRepolist("all").contains(label),
 					"Because not all of the requiredTags '"+requiredTags+"' for content label '"+label+"' are not 100% provided by the currently installed product certs, we are blocked from seeing the repo id label '"+label+"' in yum repolist all.");
 		}
 		clienttasks.unsubscribeFromSerialNumber(entitlementCert.serialNumber);
 		Assert.assertFalse(clienttasks.yumRepolist("all").contains(label),
 				"After unsubscribing from the modifier pool, yum repolist all no longer includes (repo id) '"+label+"' from modifier productId '"+modifierPool.productId+"'.");
 	
 		if (!areAllRequiredTagsProvided) {
 			throw new SkipException("We cannot claim success on this test until 100% of the requiredTags '"+requiredTags+"' are provided by the currently install products.");
 		}
 	}
 
 	
 	
 	// Candidates for an automated Test:
 	// 
 	
 	
 	
 	
 	// Configuration methods ***********************************************************************
 		
 	@BeforeClass(groups="setup")
 	public void registerBeforeClass() throws Exception {
		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername, clientpassword, null, null, null, null, Boolean.TRUE, null, null, null));
 		ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(serverHostname, serverPort, serverPrefix, clientusername, clientpassword, consumerId);
 	}
 	
 	@AfterClass(groups="setup")
 	public void unregisterAfterClass() {
 		clienttasks.unregister(null, null, null);
 	}
 	
 	// protected methods ***********************************************************************
 	
 	protected String ownerKey = "";
 
 	
 	
 	
 	// Data Providers ***********************************************************************
 	
 	
 	@DataProvider(name="getModifierSubscriptionData")
 	public Object[][] getModifierSubscriptionDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getModifierSubscriptionDataAsListOfLists());
 	}
 	protected List<List<Object>> getModifierSubscriptionDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		List<SubscriptionPool> allAvailablePools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		
 		// iterate through all available pools looking for those that contain products with content that modify other products
 		for (SubscriptionPool modifierPool : allAvailablePools) {
 			JSONObject jsonModifierPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,clientusername,clientpassword,"/pools/"+modifierPool.poolId));	
 			
 			// iterate through each of the providedProducts
 			JSONArray jsonModifierProvidedProducts = jsonModifierPool.getJSONArray("providedProducts");
 			for (int i = 0; i < jsonModifierProvidedProducts.length(); i++) {
 				JSONObject jsonModifierProvidedProduct = (JSONObject) jsonModifierProvidedProducts.get(i);
 				String modifierProvidedProductId = jsonModifierProvidedProduct.getString("productId");
 				
 				// get the productContents
 				JSONObject jsonProduct = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,clientusername,clientpassword,"/products/"+modifierProvidedProductId));	
 				JSONArray jsonProductContents = jsonProduct.getJSONArray("productContent");
 				for (int j = 0; j < jsonProductContents.length(); j++) {
 					JSONObject jsonProductContent = (JSONObject) jsonProductContents.get(j);
 					JSONObject jsonContent = jsonProductContent.getJSONObject("content");
 					
 					// get the label and modifiedProductIds for each of the productContents
 					String label = jsonContent.getString("label");
 					String requiredTags = jsonContent.getString("requiredTags"); // comma separated string
 					if (requiredTags.equals("null")) requiredTags = null;
 					JSONArray jsonModifiedProductIds = jsonContent.getJSONArray("modifiedProductIds");
 					List<String> modifiedProductIds = new ArrayList<String>();
 					for (int k = 0; k < jsonModifiedProductIds.length(); k++) {
 						String modifiedProductId = (String) jsonModifiedProductIds.get(k);
 						modifiedProductIds.add(modifiedProductId);
 					}
 					
 					// does this pool contain productContents that modify other products?
 					if (modifiedProductIds.size()>0) {
 						
 						List<SubscriptionPool> providingPools = new ArrayList<SubscriptionPool>();
 						// yes, now its time to find the subscriptions that provide the modifiedProductIds
 						for (SubscriptionPool providingPool : allAvailablePools) {
 							JSONObject jsonProvidingPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,clientusername,clientpassword,"/pools/"+providingPool.poolId));	
 							
 							// iterate through each of the providedProducts
 							JSONArray jsonProvidingProvidedProducts = jsonProvidingPool.getJSONArray("providedProducts");
 							for (int l = 0; l < jsonProvidingProvidedProducts.length(); l++) {
 								JSONObject jsonProvidingProvidedProduct = (JSONObject) jsonProvidingProvidedProducts.get(l);
 								String providingProvidedProductId = jsonProvidingProvidedProduct.getString("productId");
 								if (modifiedProductIds.contains(providingProvidedProductId)) {
 									
 									// NOTE: This test takes a long time to run when there are many providingPools.
 									// To reduce the execution time, let's simply limit the number of providing pools tested to 2,
 									// otherwise this block of code could be commented out for a more thorough test.
 									boolean thisPoolProductIdIsAlreadyInProvidingPools = false;
 									for (SubscriptionPool providedPool : providingPools) {
 										if (providedPool.productId.equals(providingPool.productId)) {
 											thisPoolProductIdIsAlreadyInProvidingPools=true; break;
 										}
 									}
 									if (thisPoolProductIdIsAlreadyInProvidingPools||providingPools.size()>=2) break;
 									
 									providingPools.add(providingPool); break;
 								}
 							}
 						}
 										
 						ll.add(Arrays.asList(new Object[]{modifierPool, label, modifiedProductIds, requiredTags, providingPools}));
 					}
 				}
 			}
 		}
 				
 		return ll;
 	}
 	
 /*
 Example jsonPool:
 [root@jsefler-onprem04 tmp]# curl -u testuser1:password -k https://jsefler-f14-candlepin.usersys.redhat.com:8443/candlepin/pools/8a90f8b42eb9b5b8012eb9b6439f0219 | python -mjson.tool  
   % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                  Dload  Upload   Total   Spent    Left  Speed
 103   828    0   828    0     0   3883      0 --:--:-- --:--:-- --:--:--  7527
 {
     "accountNumber": "12331131231", 
     "activeSubscription": true, 
     "attributes": [], 
     "consumed": 0, 
     "contractNumber": "0", 
     "created": "2011-03-15T13:30:53.215+0000", 
     "endDate": "2012-03-14T00:00:00.000+0000", 
     "href": "/pools/8a90f8b42eb9b5b8012eb9b6439f0219", 
     "id": "8a90f8b42eb9b5b8012eb9b6439f0219", 
     "owner": {
         "href": "/owners/admin", 
         "id": "8a90f8b42eb9b5b8012eb9b5cb9a0005"
     }, 
     "productId": "awesomeos-modifier", 
     "productName": "Awesome OS Modifier", 
     "providedProducts": [
         {
             "created": "2011-03-15T13:30:53.215+0000", 
             "id": "8a90f8b42eb9b5b8012eb9b6439f021a", 
             "productId": "37080", 
             "productName": "Awesome OS Modifier Bits", 
             "updated": "2011-03-15T13:30:53.215+0000"
         }
     ], 
     "quantity": 5, 
     "restrictedToUsername": null, 
     "sourceEntitlement": null, 
     "startDate": "2011-03-15T00:00:00.000+0000", 
     "subscriptionId": "8a90f8b42eb9b5b8012eb9b6059a0050", 
     "updated": "2011-03-15T13:30:53.215+0000"
 }
 
 Example jsonProduct:
 [root@jsefler-onprem04 tmp]# curl -u testuser1:password -k https://jsefler-f14-candlepin.usersys.redhat.com:8443/candlepin/products/37080 | python -mjson.tool  
   % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                  Dload  Upload   Total   Spent    Left  Speed
 102  1130    0  1130    0     0   9955      0 --:--:-- --:--:-- --:--:-- 62777
 {
     "attributes": [
         {
             "created": "2011-03-15T13:30:36.413+0000", 
             "name": "variant", 
             "updated": "2011-03-15T13:30:36.413+0000", 
             "value": "ALL"
         }, 
         {
             "created": "2011-03-15T13:30:36.414+0000", 
             "name": "arch", 
             "updated": "2011-03-15T13:30:36.414+0000", 
             "value": "ALL"
         }, 
         {
             "created": "2011-03-15T13:30:36.414+0000", 
             "name": "type", 
             "updated": "2011-03-15T13:30:36.414+0000", 
             "value": "SVC"
         }, 
         {
             "created": "2011-03-15T13:30:36.414+0000", 
             "name": "version", 
             "updated": "2011-03-15T13:30:36.414+0000", 
             "value": "6.1"
         }
     ], 
     "created": "2011-03-15T13:30:36.413+0000", 
     "dependentProductIds": [], 
     "href": "/products/37080", 
     "id": "37080", 
     "multiplier": 1, 
     "name": "Awesome OS Modifier Bits", 
     "productContent": [
         {
             "content": {
                 "contentUrl": "http://example.com/awesomeos-modifier", 
                 "created": "2011-03-15T13:30:26.233+0000", 
                 "gpgUrl": "http://example.com/awesomeos-modifier/gpg", 
                 "id": "1112", 
                 "label": "awesomeos-modifier", 
                 "metadataExpire": null, 
                 "modifiedProductIds": [
                     "27060", 
                     "37060"
                 ], 
                 "name": "awesomeos-modifier", 
                 "requiredTags": null, 
                 "type": "yum", 
                 "updated": "2011-03-15T13:30:26.233+0000", 
                 "vendor": "test-vendor"
             }, 
             "enabled": true, 
             "flexEntitlement": 0, 
             "physicalEntitlement": 0
         }
     ], 
     "updated": "2011-03-15T13:30:36.413+0000"
 }
 
 		  
 */
 	
 	
 
 	
 	
 
 }
