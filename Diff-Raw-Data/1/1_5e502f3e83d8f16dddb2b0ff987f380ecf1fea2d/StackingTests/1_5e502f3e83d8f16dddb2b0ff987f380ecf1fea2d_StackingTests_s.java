 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 
 /**
  * @author jsefler
  *
  *
  */
 @Test(groups={"StackingTests"})
 public class StackingTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 	
 	@Test(	description="subscription-manager: subscribe to each pool with the same stacking_id to achieve compliance",
 			enabled=true,
 			groups={"blockedByBug-739671", "blockedByBug-740377"},
 			dataProvider="getAvailableStackableSubscriptionPoolsData")
 	//@ImplementsNitrateTest(caseId=)
 	public void StackEachPoolToAchieveCompliance(List<SubscriptionPool> stackableSubscriptionPools) throws JSONException, Exception{
 		
 		// loop through the pools to determine the minimum socket count for which one of each stackable pool is needed to achieve compliance
 		int minimumSockets=0;
 		for (SubscriptionPool pool : stackableSubscriptionPools) {
 			String sockets = CandlepinTasks.getPoolProductAttributeValue(sm_clientUsername, sm_clientPassword, sm_serverUrl, pool.poolId, "sockets");
 			minimumSockets+=Integer.valueOf(sockets);
 		}
 		
 		// override the system facts setting the socket count to a value for which all the stackable subscriptions are needed to achieve compliance
 		Map<String,String> factsMap = new HashMap<String,String>();
 		factsMap.put("cpu.cpu_socket(s)", String.valueOf(minimumSockets));
 		//factsMap.put("lscpu.cpu_socket(s)", String.valueOf(minimumSockets));
 		clienttasks.createFactsFileWithOverridingValues(factsMap);
 		clienttasks.facts(null,true,null,null,null);
 		
 		// loop through the stackable pools until we find the first one that covers product certs that are currently installed (put that subscription at the front of the list) (remember the installed product certs)
 		List<ProductCert> installedProductCerts = new ArrayList<ProductCert>();
 		for (int i=0; i<stackableSubscriptionPools.size(); i++) {
 			SubscriptionPool pool = stackableSubscriptionPools.get(i);
 			installedProductCerts = clienttasks.getCurrentProductCertsCorrespondingToSubscriptionPool(pool);
 			if (installedProductCerts.size()>0) {
 				stackableSubscriptionPools.remove(i);
 				stackableSubscriptionPools.add(0, pool);
 				break;
 			}
 		}
 		if (installedProductCerts.size()==0) throw new SkipException("Could not find any installed products for which stacking these pools would achieve compliance.");
 
 		// reconfigure such that only these product certs are installed (copy them to a /tmp/sm-stackingProductDir)
 		for (ProductCert productCert : installedProductCerts) {
 			RemoteFileTasks.runCommandAndAssert(client, "cp "+productCert.file+" "+productCertDirForStacking, 0);
 		}
 		clienttasks.config(null,null,true,new String[]{"rhsm","productCertDir".toLowerCase(),productCertDirForStacking});
 		
 		// subscribe to each pool and assert "Partially Subscribe" status and overall incompliance until the final pool is subscribed
 		Assert.assertEquals(clienttasks.getFactValue(ComplianceTests.factNameForSystemCompliance), ComplianceTests.factValueForSystemNonCompliance,
 			"Prior to subscribing to any of the stackable subscription pools, the overall system entitlement status should NOT be valid/compliant.");
 		int s=0;
 		for (SubscriptionPool pool : stackableSubscriptionPools) {
 			File entitlementCertFile = clienttasks.subscribeToSubscriptionPool(pool);
 			if (++s < stackableSubscriptionPools.size()) {
 				
 				// assert installed products are Partially Subscribed
 				for (ProductCert installedProductCert : installedProductCerts) {
 					InstalledProduct installedProduct = clienttasks.getInstalledProductCorrespondingToProductCert(installedProductCert);
 					Assert.assertEquals(installedProduct.status, "Partially Subscribed", "After subscribing to stackable subscription pool for ProductId '"+pool.productId+"', the status of Installed Product '"+installedProduct.productName+"' should be Partially Subscribed.");
 				}
 				
 				// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=739671
 				boolean invokeWorkaroundWhileBugIsOpen = true;
 				String bugId="739671"; 
 				try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 				if (invokeWorkaroundWhileBugIsOpen) {
 					log.info("Skipping the value assertion for fact '"+ComplianceTests.factNameForSystemCompliance+"' while bug '"+bugId+"' is open.");
 				} else {
 				// END OF WORKAROUND
 				
 				// assert overall system compliance is not yet valid
 				Assert.assertEquals(clienttasks.getFactValue(ComplianceTests.factNameForSystemCompliance), ComplianceTests.factValueForSystemPartialCompliance,
 					"The overall system entitlement status should NOT be valid/compliant until we have subscribed to enough stackable subscription pools to meet coverage for the system's cpu.socket(s) '"+minimumSockets+"'.");		
 				}
 			}
 		}
 		
 		// assert installed products are fully Subscribed
 		for (ProductCert installedProductCert : installedProductCerts) {
 			InstalledProduct installedProduct = clienttasks.getInstalledProductCorrespondingToProductCert(installedProductCert);
 			Assert.assertEquals(installedProduct.status, "Subscribed", "After subscribing to enough stackable subscription pools to cover the systems sockets count ("+minimumSockets+"), the status of Installed Product '"+installedProduct.productName+"' should be fully Subscribed.");
 		}
 		// assert overall system compliance is now valid
 		Assert.assertEquals(clienttasks.getFactValue(ComplianceTests.factNameForSystemCompliance), ComplianceTests.factValueForSystemCompliance,
 			"After having subscribed to all the stackable subscription pools needed to meet coverage for the system's cpu.socket(s) '"+minimumSockets+"', the overall system entitlement status should be valid/compliant.");
 
 
 		
 	}
 	
 	// Candidates for an automated Test:
 	// TODO Bug 733327 - stacking entitlements reports as distinct entries in cli list --installed 
 	// TODO Bug 740377 - Stacking Partially Compliant / Yellow State is Broken
 	// TODO Bug 743710 - Subscription manager displays incorrect status for partially subscribed subscription
 	//      MAYBE THIS ONE BELONGS IN COMPLIANCE TESTS?
 		
 	// Configuration methods ***********************************************************************
 
 	
 	@BeforeClass(groups={"setup"})
 	public void setupBeforeClass() {
 		
 		// clean out the productCertDirs
 		RemoteFileTasks.runCommandAndAssert(client, "rm -rf "+productCertDirForStacking, 0);
 		RemoteFileTasks.runCommandAndAssert(client, "mkdir "+productCertDirForStacking, 0);
 		
 		this.productCertDir = clienttasks.productCertDir;
 	}
 	
 	@BeforeMethod(groups={"setup"})
 	@AfterClass(groups={"setup"},alwaysRun=true)
 	public void cleanupAfterClass() {
 		if (clienttasks==null) return;
 		clienttasks.deleteFactsFileWithOverridingValues();
 		if (this.productCertDir!=null) clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", this.productCertDir);
 
 	}
 	
 	// Protected methods ***********************************************************************
 
 	protected final String productCertDirForStacking = "/tmp/sm-stackingProductDir";
 	protected String productCertDir = null;
 
 	
 	// Data Providers ***********************************************************************
 	
 
 	
 	
 	// FIXME NOT BEING USED
 	@DataProvider(name="getAllStackableJSONPoolsData")
 	public Object[][] getAllStackableJSONPoolsDataAs2dArray() throws Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getAllStackableJSONPoolsDataAsListOfLists());
 	}
 	protected List<List<Object>> getAllStackableJSONPoolsDataAsListOfLists() throws Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		Map<String,List<JSONObject>> stackableJSONPoolsMap = new HashMap<String,List<JSONObject>>();
 		
 		for (List<Object> row : getAllJSONPoolsDataAsListOfLists()) {
 			JSONObject jsonPool = (JSONObject) row.get(0);
 			
 			
 			// loop through all the productAttributes looking for stacking_id
 			JSONArray jsonProductAttributes = jsonPool.getJSONArray("productAttributes");
 			for (int j = 0; j < jsonProductAttributes.length(); j++) {	// loop product attributes to find a stacking_id
 				if (((JSONObject) jsonProductAttributes.get(j)).getString("name").equals("stacking_id")) {
 					String stacking_id = ((JSONObject) jsonProductAttributes.get(j)).getString("value");
 					
 					// we found a stackable pool, let's add it to the stackableJSONPoolsMap
 					if (!stackableJSONPoolsMap.containsKey(stacking_id)) stackableJSONPoolsMap.put(stacking_id, new ArrayList<JSONObject>());
 					stackableJSONPoolsMap.get(stacking_id).add(jsonPool);
 					break;
 				}
 			}
 		}
 		
 		for (String stacking_id : stackableJSONPoolsMap.keySet()) {
 			List<JSONObject> stackableJSONPools = stackableJSONPoolsMap.get(stacking_id);
 			ll.add(Arrays.asList(new Object[]{stackableJSONPools}));
 		}
 		
 		return ll;
 	}
 
 	
 	@DataProvider(name="getAvailableStackableSubscriptionPoolsData")
 	public Object[][] getAvailableStackableSubscriptionPoolsDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getAvailableStackableSubscriptionPoolsDataAsListOfLists());
 	}
 	protected List<List<Object>> getAvailableStackableSubscriptionPoolsDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		Map<String,List<SubscriptionPool>> stackableSubscriptionPoolsMap = new HashMap<String,List<SubscriptionPool>>();
 		
 		// find all the SubscriptionPools with the same stacking_id
 		for (List<Object> l : getAvailableSubscriptionPoolsDataAsListOfLists()) {
 			SubscriptionPool pool = (SubscriptionPool)l.get(0);
 			String stacking_id = CandlepinTasks.getPoolProductAttributeValue(sm_clientUsername, sm_clientPassword, sm_serverUrl, pool.poolId, "stacking_id");
 			
 			if (stacking_id==null) continue; // this pool is not stackable
 			
 			// add this available stackable pool to the stackableSubscriptionPoolsMap
 			if (!stackableSubscriptionPoolsMap.containsKey(stacking_id)) stackableSubscriptionPoolsMap.put(stacking_id, new ArrayList<SubscriptionPool>());
 			stackableSubscriptionPoolsMap.get(stacking_id).add(pool);
 		}
 		
 		// assemble the rows of data
 		for (String stacking_id : stackableSubscriptionPoolsMap.keySet()) {
 			List<SubscriptionPool> stackableSubscriptionPools = stackableSubscriptionPoolsMap.get(stacking_id);
 			
 			// List<SubscriptionPool> stackableSubscriptionPools
 			ll.add(Arrays.asList(new Object[]{stackableSubscriptionPools}));
 		}
 		
 		return ll;
 	}
 }
