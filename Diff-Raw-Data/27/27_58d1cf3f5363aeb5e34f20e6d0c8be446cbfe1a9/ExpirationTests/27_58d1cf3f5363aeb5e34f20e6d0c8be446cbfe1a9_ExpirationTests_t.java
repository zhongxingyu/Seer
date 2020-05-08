 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.RevokedCert;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 @Test(groups="ExpirationTests")
 public class ExpirationTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 	
 	@Test(	description="subscribe to a pool that will expire soon and assert the entitlements are removed after it expires",
 			groups={"blockedByBug-660713"}, dependsOnGroups={},
 			enabled=true)
 	public void VerifyEntitlementsAreRemovedAfterSubscriptionExpires_Test() throws Exception{
 
 		// create a subscription pool that will expire 2 minutes from now
 		int endingMinutesFromNow = 2;
 		String expiringPoolId = createTestPool(-60*24,endingMinutesFromNow);
 		
 		// assert the expiring pool is currently available
 		SubscriptionPool expiringPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", expiringPoolId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNotNull(expiringPool,"The expiring SubscriptionPool is currently available for subscribing: "+expiringPool);
 
 		// subscribe
 		File expiringCertFile = clienttasks.subscribeToSubscriptionPool(expiringPool);
 		/*EntitlementCert*/ expiringCert = clienttasks.getEntitlementCertFromEntitlementCertFile(expiringCertFile);
 		List <ProductSubscription> expiringProductSubscriptions = ProductSubscription.findAllInstancesWithMatchingFieldFromList("serialNumber", expiringCert.serialNumber, clienttasks.getCurrentlyConsumedProductSubscriptions());
 		Assert.assertMore(expiringProductSubscriptions.size(),0, "Found ProductSubscriptions corresponding to the just subscribed SubscriptionPool: "+expiringPool);
 
 		// wait for pool to expire
 		sleep(endingMinutesFromNow*60*1000 + 30*1000); // plus a 30 sec buffer
 		sleep(30*1000); // wait another 30 sec for rhsmcertd insurance
 		
 		// verify that that the pool expired
 		SubscriptionPool expiredPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", expiringPoolId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNull(expiredPool,"The expired TestPool is no longer available for subscribing: "+expiringPoolId);
 		
 		//verify that the expired product subscriptions are gone
 		List <ProductSubscription> currentProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		for (ProductSubscription p : expiringProductSubscriptions) {
 			Assert.assertTrue(!currentProductSubscriptions.contains(p),"The expired ProductSubscription '"+p+"' has been removed by rhsmcertd.");
 		}
 		
 		// verify that the expired entitlement cert file is gone
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client,expiringCertFile.getPath())==0,"The expired entitlement cert file has been removed by rhsmcertd");
 	}
 	
 	
 	// TODO Review the validity of this testcase with development (My observation is that expiringCert is NOT actually added to list of RevokedCerts
 	@Test(	description="Verify expired entitlement is added to the certifiate revocation list after the subscription expires",
 			groups={}, dependsOnMethods={"VerifyEntitlementsAreRemovedAfterSubscriptionExpires_Test"},
 			enabled=false)
 	public void VerifyExpiredEntitlementIsAddedToCertificateRevocationList_Test() throws Exception{
 		if (expiringCert==null) throw new SkipException("This test requires a successful run of a prior test whose entitlement cert has expired."); 
 
 		log.info("Check the CRL list on the server and verify the expired entitlement cert serial is revoked...");
 		log.info("Waiting 2 minutes...  (Assuming this is the candlepin.conf value set for pinsetter.org.fedoraproject.candlepin.pinsetter.tasks.CertificateRevocationListTask.schedule)");
 		sleep(2*60*1000);	// give the CertificateRevocationListTask.schedule 2 minutes to update the list since that is what was set in setupBeforeSuite()
 		// NOTE: The refresh schedule should have been set with a call to servertasks.updateConfigFileParameter in the setupBeforeSuite()
 		//       Set inside /etc/candlepin/candlepin.conf
 		//       pinsetter.org.fedoraproject.candlepin.pinsetter.tasks.CertificateRevocationListTask.schedule=0 0/2 * * * ?
 		// NOTE: if not set, the default is  public static final String DEFAULT_SCHEDULE = "0 0 12 * * ?" Fire at 12pm (noon) every day
 		RevokedCert revokedCert = RevokedCert.findFirstInstanceWithMatchingFieldFromList("serialNumber",expiringCert.serialNumber,servertasks.getCurrentlyRevokedCerts());
 		Assert.assertTrue(revokedCert!=null,"Expiring entitlement certificate serial number '"+expiringCert.serialNumber+"' has been added to the Certificate Revocation List (CRL) as: "+revokedCert);
 		Assert.assertEquals(revokedCert.reasonCode, "Privilege Withdrawn","An expired entitlement certificate should be revoked with a reason code of Privilege Withdrawn.");
 	}
 	
 	
 	@Test(	description="assert that a soon to expire pool is removed from availablity after it expires",
 			groups={}, dependsOnGroups={},
 			enabled=true)
 	public void VerifyPoolIsRemovedAfterSubscriptionExpires_Test() throws Exception {
 		
 		// create a subscription pool that will expire 2 minutes from now
 		int endingMinutesFromNow = 2;
 		String expiringPoolId = createTestPool(-60*24,endingMinutesFromNow);
 		
 		// assert the expiring pool is currently available
 		SubscriptionPool expiringPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", expiringPoolId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNotNull(expiringPool,"The expiring SubscriptionPool is currently available for subscribing: "+expiringPool);
 
 		// wait for pool to expire
 		sleep(endingMinutesFromNow*60*1000 + 30*1000); // plus a 30 sec buffer;
 		
 		// assert pool is no longer available
 		SubscriptionPool expiredPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", expiringPoolId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNull(expiredPool,"The expired SubscriptionPool is no longer available for subscribing");
 	}
 	
 	
 	@Test(	description="assert that a soon to be available pool is not yet available",
 			groups={}, dependsOnGroups={},
 			enabled=true)
 	public void VerifyPoolIsNotYetAvailableUntilTheStartDate_Test() throws Exception {
 		
 		// create a subscription pool that will start 2 minutes from now
 		int startingMinutesFromNow = 3;
 		String aboutToStartPoolId = createTestPool(startingMinutesFromNow,/*60*24*/startingMinutesFromNow+2);
 	
 		// assert the starting pool is NOT currently available
 		SubscriptionPool startingPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", aboutToStartPoolId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNull(startingPool,"The about to start SubscriptionPool with id '"+aboutToStartPoolId+"' is currently NOT YET available for subscribing.");
 
 		// wait for pool to start
 		sleep((startingMinutesFromNow-1)*60*1000 + 0*1000); // plus a 30 sec buffer;
 	
 		// assert the starting pool is still NOT currently available (1 minute before its startDate)
 		startingPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", aboutToStartPoolId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNull(startingPool,"The about to start SubscriptionPool with id '"+aboutToStartPoolId+"' is still NOT YET available for subscribing (1 minute before its startDate).");
 
 		// wait one more minute for pool to start
 		sleep(1*60*1000 + 30*1000); // plus a 30 sec buffer;
 
 		// assert the starting pool is NOW currently available
 		startingPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", aboutToStartPoolId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNotNull(startingPool,"The about to start SubscriptionPool with id '"+aboutToStartPoolId+"' is now available for subscribing: "+startingPool);
 
 	}
 	
 	
 	// Candidates for an automated Test:
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=655835
 	
 	
 
 	
 	
 	
 	// Configuration methods ***********************************************************************
 	
 	
 	@BeforeClass(groups="setup")
 	public void skipIfHosted() {
 		if (!servertasks.isOnPremises) throw new SkipException("These tests are only valid for on-premises candlepin servers.");
 	}
 	
 	@BeforeClass(groups="setup", dependsOnMethods="skipIfHosted")
 	public void registerBeforeClass() throws Exception {
 		clienttasks.unregister(null, null, null);
 		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, null));
 		ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_serverHostname, sm_serverPort, sm_serverPrefix, sm_clientUsername, sm_clientPassword, consumerId);
 	}
 	
 	@BeforeClass(groups="setup", dependsOnMethods="registerBeforeClass")
 	public void findRandomAvailableProductIdBeforeClass() throws Exception {
 
 		// find a randomly available product id
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		randomAvailableProductId = pool.productId;
 	}
 	
 	@BeforeClass(groups="setup", dependsOnMethods="registerBeforeClass")
 	public void checkTimeBeforeClass() throws Exception{
 		checkTime("candlepin server", server);
 		checkTime("client", client);
 		clienttasks.restart_rhsmcertd(1, false);
 	}
 	
 //	@BeforeMethod
 //	public void createTestPoolsBeforeMethod() throws Exception{
 //		
 //		Calendar cal = new GregorianCalendar();
 //		cal.add(Calendar.MINUTE, 2); 	// pool will expire in 2 min from now
 //		Date _3min = cal.getTime();
 //		cal.add(Calendar.DATE, -21);
 //		Date _3weeksago = cal.getTime();
 //		/*List<InstalledProduct> clientProds = clienttasks.getCurrentlyInstalledProducts();
 //		String product = clientProds.get(0).productName;*/
 //		String[] providedProducts = {"37068", "37069", "37060"};
 //		String requestBody = CandlepinTasks.createPoolRequest(10, _3weeksago, _3min, "MKT-rhel-server", 123, providedProducts).toString();
 //		CandlepinTasks.postResourceUsingRESTfulAPI(serverHostname, serverPort, serverPrefix, serverAdminUsername, serverAdminPassword, "/owners/" + owner + "/subscriptions", requestBody);
 //		JSONObject jobDetail = CandlepinTasks.refreshPoolsUsingRESTfulAPI(serverHostname, serverPort, serverPrefix, serverAdminUsername, serverAdminPassword, owner);
 //		jobDetail = CandlepinTasks.waitForJobDetailStateUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,serverAdminUsername,serverAdminPassword, jobDetail, "FINISHED", 5*1000, 1);
 //
 //	}
 	
 
 	
 	
 	// protected methods ***********************************************************************
 	
 	protected String ownerKey = "";
 	protected String randomAvailableProductId = null;
 	protected EntitlementCert expiringCert = null;
 	
 	protected void checkTime(String host, SSHCommandRunner runner)throws Exception {
 		//make sure local clock and server clock are synced
 		Date localTime = Calendar.getInstance().getTime();
 		Date remoteTime; 
 		//SSHCommandRunner runner = new SSHCommandRunner("jweiss-rhel6-1.usersys.redhat.com", sshUser, sshKeyPrivate, sshkeyPassphrase, null);
 		runner.runCommandAndWait("date");
 		String serverDateStr = runner.getStdout();
 		SimpleDateFormat unixFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
 		remoteTime = unixFormat.parse(serverDateStr);
 		long timeDiffms = Math.abs(localTime.getTime() - remoteTime.getTime());
 		Assert.assertLess(timeDiffms, 60000L, "Time difference with " + host + " is less than 1 minute");
 	}
 
 	/**
 	 * @param startingMinutesFromNow
 	 * @param endingMinutesFromNow
 	 * @return poolId to the newly available SubscriptionPool
 	 * @throws JSONException
 	 * @throws Exception
 	 */
 	protected String createTestPool(int startingMinutesFromNow, int endingMinutesFromNow) throws JSONException, Exception  {
 		
 		// set the start and end dates
 		Calendar endCalendar = new GregorianCalendar();
 		endCalendar.add(Calendar.MINUTE, endingMinutesFromNow);
 		Date endDate = endCalendar.getTime();
 		Calendar startCalendar = new GregorianCalendar();
 		startCalendar.add(Calendar.MINUTE, startingMinutesFromNow);
 		Date startDate = startCalendar.getTime();
 
 		
 		// randomly choose a contract number
 		Integer contractNumber = Integer.valueOf(getRandInt());
 		
 		// randomly choose an account number
 		Integer accountNumber = Integer.valueOf(getRandInt());
 		
 		// choose a product id for the subscription
 		//String productId =  "MKT-rhel-server";  // too hard coded
 		//JSONArray jsonProducts = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,serverAdminUsername,serverAdminPassword,"/products"));	
 		//String productId = null;
 		//do {	// pick a random productId (excluding a personal productId) // too random; could pick a product that is not available to this system
 		//	productId =  ((JSONObject) jsonProducts.get(randomGenerator.nextInt(jsonProducts.length()))).getString("id");
 		//} while (getPersonProductIds().contains(productId));
 		String productId = randomAvailableProductId;
 
 		// choose providedProducts for the subscription
 		//String[] providedProducts = {"37068", "37069", "37060"};
 		//String[] providedProducts = {
 		//	((JSONObject) jsonProducts.get(randomGenerator.nextInt(jsonProducts.length()))).getString("id"),
 		//	((JSONObject) jsonProducts.get(randomGenerator.nextInt(jsonProducts.length()))).getString("id"),
 		//	((JSONObject) jsonProducts.get(randomGenerator.nextInt(jsonProducts.length()))).getString("id")
 		//};
 		String[] providedProducts = {};
 		
 		// create the subscription
 		String requestBody = CandlepinTasks.createSubscriptionRequestBody(3, startDate, endDate, productId, contractNumber, accountNumber, providedProducts).toString();
 		JSONObject jsonSubscription = new JSONObject(CandlepinTasks.postResourceUsingRESTfulAPI(sm_serverHostname, sm_serverPort, sm_serverPrefix, sm_serverAdminUsername, sm_serverAdminPassword, "/owners/" + ownerKey + "/subscriptions", requestBody));
 		
 		// refresh the pools
 		JSONObject jobDetail = CandlepinTasks.refreshPoolsUsingRESTfulAPI(sm_serverHostname, sm_serverPort, sm_serverPrefix, sm_serverAdminUsername, sm_serverAdminPassword, ownerKey);
 		jobDetail = CandlepinTasks.waitForJobDetailStateUsingRESTfulAPI(sm_serverHostname,sm_serverPort,sm_serverPrefix,sm_serverAdminUsername,sm_serverAdminPassword, jobDetail, "FINISHED", 5*1000, 1);
 		
		// assemble an activeon parameter set to the start date so we can pass it on to the REST API call to find the created pool
		DateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");				// "2012-02-08T00:00:00.000+0000"
		String iso8601FormatedDateString = iso8601DateFormat.format(startDate);
		iso8601FormatedDateString = iso8601FormatedDateString.replaceFirst("(..$)", ":$1");				// "2012-02-08T00:00:00.000+00:00"	// see https://bugzilla.redhat.com/show_bug.cgi?id=720493 // http://books.xmlschemata.org/relaxng/ch19-77049.html requires a colon in the time zone for xsd:dateTime
		String urlEncodedActiveOnDate = java.net.URLEncoder.encode(iso8601FormatedDateString, "UTF-8");	// "2012-02-08T00%3A00%3A00.000%2B00%3A00"	encode the string to escape the colons and plus signs so it can be passed as a parameter on an http call

		// loop through all pools available to owner and find the newly created poolid corresponding to the new subscription id activeon startDate
 		String poolId = null;
 		JSONArray jsonPools = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverHostname,sm_serverPort,sm_serverPrefix,sm_serverAdminUsername,sm_serverAdminPassword,"/owners/"+ownerKey+"/pools"+"?activeon="+urlEncodedActiveOnDate));	
 		for (int i = 0; i < jsonPools.length(); i++) {
 			JSONObject jsonPool = (JSONObject) jsonPools.get(i);
 			//if (contractNumber.equals(jsonPool.getInt("contractNumber"))) {
 			if (jsonPool.getString("subscriptionId").equals(jsonSubscription.getString("id"))) {
 				poolId = jsonPool.getString("id");
 				break;
 			}
 		}
 		Assert.assertNotNull(poolId,"Found newly created pool corresponding to the newly created subscription with id: "+jsonSubscription.getString("id"));
 		log.info("The newly created subscription pool with id '"+poolId+"' will start in '"+startingMinutesFromNow+"' minutes from now.");
 		log.info("The newly created subscription pool with id '"+poolId+"' will expire in '"+endingMinutesFromNow+"' minutes from now.");
 		return poolId; // return poolId to the newly available SubscriptionPool
 		
 	}
 	
 	
 //	protected Predicate<SubscriptionPool> expToday = new Predicate<SubscriptionPool>(){
 //		public boolean apply(SubscriptionPool pool){
 //			Calendar cal = pool.endDate;
 //			Calendar today = new GregorianCalendar();
 //			return (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) && (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) ;
 //		}
 //	};
 //
 //	class ByIdPredicate implements Predicate<SubscriptionPool> {
 //		String id;
 //		public ByIdPredicate(String id) {
 //			this.id=id;
 //		}
 //		public boolean apply(SubscriptionPool pool) {
 //			return this.id.equals(pool.poolId);
 //		}
 //	}
 //	
 //	protected SubscriptionPool getPool() {
 //		Collection<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 //		
 //		Collection<SubscriptionPool> expiresToday = Collections2.filter(pools, expToday);
 //		
 //		//choose first pool
 //		return  expiresToday.iterator().next();
 //	}
 	
 }
