 package com.redhat.qe.sm.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.testng.SkipException;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsTCMS;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.auto.testopia.Assert;
 import com.redhat.qe.sm.abstractions.ProductSubscription;
 import com.redhat.qe.sm.abstractions.SubscriptionPool;
 import com.redhat.qe.sm.base.SubscriptionManagerTestScript;
 
 /**
  * @author jsefler
  *
  *
  *REFERENCE MATERIAL:
  * https://tcms.engineering.redhat.com/case/55702/
  * https://tcms.engineering.redhat.com/case/55718/
  * https://engineering.redhat.com/trac/IntegratedMgmtQE/wiki/RH-Personal_dev_testplan
  * 
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
 
  */
 @Test(groups={"rhelPersonal"})
 public class RHELPersonalTests extends SubscriptionManagerTestScript{
 	
 
 	 @Test(	description="subscription-manager-cli: Ensure RHEL Personal Bits are available and unlimited after a person has subscribed to RHEL Personal",
 			groups={"EnsureRHELPersonalBitsAreAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test", "RHELPersonal"/*, "blockedByBug-624816"*/},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	@ImplementsTCMS(id="55702,55718")
 	public void EnsureRHELPersonalBitsAreAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 //		if (!isServerOnPremises) throw new SkipException("Currently this test is designed only for on-premises.");	//TODO Make this work for IT too.  jsefler 8/12/2010 
 		if (client2==null) throw new SkipException("This test requires a second consumer.");
 		if (consumerUsername.equals("admin")) throw new SkipException("This test requires that the client user ("+consumerUsername+") is NOT admin.");
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=624423 - jsefler 8/16/2010
 		Boolean invokeWorkaroundWhileBugIsOpen = false;
 		try {String bugId="624423"; if (BzChecker.getInstance().isBugOpen(bugId)&&invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			servertasks.restartTomcat();
 		} // END OF WORKAROUND
 		
 		
 		SubscriptionPool pool = null;
 		
 		
 		log.info("Register client2 under username '"+consumerUsername+"' as a system and assert that '"+systemSubscriptionName+"' is NOT yet available...");
 		client2tasks.unregister();
 		client2tasks.register(consumerUsername, consumerPassword, "system", null, null, null);
 		List<SubscriptionPool> client2BeforeSubscriptionPools = client2tasks.getCurrentlyAvailableSubscriptionPools();
 		pool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2BeforeSubscriptionPools);
 		Assert.assertTrue(pool==null,systemSubscriptionName+" is NOT yet available to client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"'.");
 
 		
 		log.info("Now register client1 under username '"+consumerUsername+"' as a person and subscribe to the '"+personSubscriptionName+"' subscription pool...");
 		client1tasks.unregister();
 		client1tasks.register(consumerUsername, consumerPassword, "person", null, null, null);
 		pool = client1tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",personSubscriptionName,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertTrue(pool!=null,personSubscriptionName+" is available to user '"+consumerUsername+"' registered as a person.");
 		List<String> beforeEntitlementCertFiles = client1tasks.getCurrentEntitlementCertFiles();
 		if (isServerOnPremises) {	// needed this special case block to assert that that a new entitlement certificate is NOT dropped
 			client1tasks.subscribe(pool.poolId, null, null, null, null);
 			Assert.assertTrue(!client1tasks.getCurrentlyAvailableSubscriptionPools().contains(pool),
 				"The available subscription pools no longer contains the just subscribed to pool: "+pool);
 			List<String> afterEntitlementCertFiles = client1tasks.getCurrentEntitlementCertFiles();
 			Assert.assertTrue(afterEntitlementCertFiles.equals(beforeEntitlementCertFiles),
 				"Subscribing to subscription pool '"+personSubscriptionName+"' does NOT drop a new entitlement certificate when registered as a person.");
 		} else {
 			client1tasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		}
 		
 		log.info("Now client2 (already registered as a system under username '"+consumerUsername+"') should now have '"+systemSubscriptionName+"' available with unlimited quantity...");
 		List<SubscriptionPool> client2AfterSubscriptionPools = client2tasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2AfterSubscriptionPools);
 		Assert.assertTrue(systemSubscriptionPool!=null,systemSubscriptionName+" is now available to client2 '"+client2.getConnection().getHostname()+"' (registered as a system under username '"+consumerUsername+"')");
 		Assert.assertEquals(systemSubscriptionPool.quantity.toLowerCase(),"unlimited","An unlimited quantity of entitlements is available to "+systemSubscriptionName+".");
 		
 		
 		log.info("Verifying that the available subscription pools available to client2 has increased by only the '"+systemSubscriptionName+"' pool...");
		client2BeforeSubscriptionPools.add(systemSubscriptionPool);  // manually add the systemSubscriptionPool to the List
		Assert.assertEquals(client2AfterSubscriptionPools,client2BeforeSubscriptionPools,
 				"The list of available subscription pools seen by client2 increases only by '"+systemSubscriptionName+"' pool: "+systemSubscriptionPool);
	
 	}
 	 
 	 
 	@Test(	description="subscription-manager-cli: Ensure RHEL Personal Bits are consumable after a person has subscribed to RHEL Personal",
 			groups={"EnsureRHELPersonalBitsAreConsumableAfterRegisteredPersonSubscribesToRHELPersonal_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureRHELPersonalBitsAreAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	@ImplementsTCMS(id="55702,55718")
 	public void EnsureRHELPersonalBitsAreConsumableAfterRegisteredPersonSubscribesToRHELPersonal_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 				
 		log.info("Now client2 (already registered as a system under username '"+consumerUsername+"') can now consume '"+systemSubscriptionName+"'...");
 		SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 		client2tasks.subscribeToSubscriptionPoolUsingPoolId(systemSubscriptionPool);
 		
 		
 		log.info("Now client2 should be consuming the product '"+systemConsumedProductName+"'...");
 		ProductSubscription systemProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 		Assert.assertTrue(systemProductSubscription!=null,systemConsumedProductName+" is now consumed on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"'.");
 		
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure that availability of RHEL Personal Bits is revoked once the person unsubscribes from RHEL Personal",
 			groups={"EnsureAvailabilityOfRHELPersonalBitsIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureRHELPersonalBitsAreConsumableAfterRegisteredPersonSubscribesToRHELPersonal_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	//@ImplementsTCMS(id="55702,55718")
 	public void EnsureAvailabilityOfRHELPersonalBitsIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 		
 		log.info("Unsubscribe client2 (already registered as a system under username '"+consumerUsername+"') from all currently consumed product subscriptions...");
 		client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		
 		log.info("Unsubscribe client1 (already registered as a person under username '"+consumerUsername+"') from product subscription '"+personSubscriptionName+"'...");
 		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 
 		
 		log.info("Now verify that client2 (already registered as a system under username '"+consumerUsername+"') can no longer subscribe to the '"+systemSubscriptionName+"' pool...");
 		SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertTrue(systemSubscriptionPool==null,systemSubscriptionName+" is no longer available on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"'.");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure that the entitlement cert for RHEL Personal Bits is revoked once the person unsubscribes from RHEL Personal",
 			groups={"EnsureEntitlementCertForRHELPersonalBitsIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureAvailabilityOfRHELPersonalBitsIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	//@ImplementsTCMS(id="55702,55718")
 	public void EnsureEntitlementCertForRHELPersonalBitsIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 		log.info("Making sure the clients are not subscribed to anything...");
 		client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		log.info("Subscribe client1 (already registered as a person under username '"+consumerUsername+"') to product subscription '"+personSubscriptionName+"'...");
 		SubscriptionPool personSubscriptionPool = client1tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",personSubscriptionName,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertTrue(personSubscriptionPool!=null,personSubscriptionName+" is available to user '"+consumerUsername+"' registered as a person.");
 		client1tasks.subscribe(personSubscriptionPool.poolId, null, null, null, null);
 
 		
 		log.info("Subscribe client2 (already registered as a system under username '"+consumerUsername+"') to product subscription "+systemSubscriptionName+"...");
 		SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 		client2tasks.subscribeToSubscriptionPoolUsingPoolId(systemSubscriptionPool);
 		ProductSubscription systemProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 		Assert.assertTrue(systemProductSubscription!=null,systemConsumedProductName+" is now consumed on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"'.");
 
 		
 		log.info("Now, unsubscribe the person on client 1 from the '"+personSubscriptionName+"' pool and update the rhsmcertd frequency to 1 minute on client2.  Then assert that the '"+systemSubscriptionName+"' gets revoked from client2.");
 		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		int certFrequencyMinutes = 1;
 		client2tasks.changeCertFrequency(certFrequencyMinutes);
 		sleep(certFrequencyMinutes*60*1000);sleep(10000);	// give the rhsmcertd a chance check in with the candlepin server and update the certs
 		systemProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 		Assert.assertTrue(systemProductSubscription==null,systemConsumedProductName+" was revoked on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"' after the certFrquency of '"+certFrequencyMinutes+"' minutes since this same person has unsubscribed from the "+personSubscriptionName+" on client1");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure that the entitlement cert for RHEL Personal Bits is revoked once the person unregisters",
 			groups={"EnsureEntitlementCertForRHELPersonalBitsIsRevokedOncePersonUnregisters_Test","RHELPersonal", "blockedByBug-624063"},
 			dependsOnGroups={"EnsureAvailabilityOfRHELPersonalBitsIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	//@ImplementsTCMS(id="55702,55718")
 	public void EnsureEntitlementCertForRHELPersonalBitsIsRevokedOncePersonUnregisters_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 		log.info("Making sure the clients are not subscribed to anything...");
 		client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		log.info("Subscribe client1 (already registered as a person under username '"+consumerUsername+"') to subscription '"+personSubscriptionName+"' pool...");
 		SubscriptionPool personSubscriptionPool = client1tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",personSubscriptionName,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertTrue(personSubscriptionPool!=null,personSubscriptionName+" is available to user '"+consumerUsername+"' registered as a person.");
 		client1tasks.subscribe(personSubscriptionPool.poolId, null, null, null, null);
 
 		
 		log.info("Subscribe client2 (already registered as a system under username '"+consumerUsername+"') to subscription '"+systemSubscriptionName+"' pool...");
 		SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 		client2tasks.subscribeToSubscriptionPoolUsingPoolId(systemSubscriptionPool);
 		ProductSubscription systemProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemSubscriptionName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 		Assert.assertTrue(systemProductSubscription!=null,systemSubscriptionName+" is now consumed on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"'.");
 
 		
 		log.info("Now, unregister the person on client 1 from the "+personSubscriptionName+" pool and update the rhsmcertd frequency to 1 minute on client2.  Then assert that the '"+systemSubscriptionName+"' gets revoked from client2.");
 		client1tasks.unregister();
 		int certFrequencyMinutes = 1;
 		client2tasks.changeCertFrequency(certFrequencyMinutes);
 		sleep(certFrequencyMinutes*60*1000);sleep(10000);	// give the rhsmcertd a chance check in with the candlepin server and update the certs
 		systemProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemSubscriptionName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 		Assert.assertTrue(systemProductSubscription==null,systemSubscriptionName+" was revoked on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"' after the certFrquency of '"+certFrequencyMinutes+"' minutes since this same person has unregistered from the '"+personSubscriptionName+"' on client1");
 	}
 	
 	
 	
 	
 	
 	// Configuration Methods ***********************************************************************
 	
 	
 	@AfterGroups(groups={}, value={"RHELPersonal"}, alwaysRun=true)
 	public void teardownAfterEnsureRHELPersonalBitsAreAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test() {
 		if (client2tasks!=null) client2tasks.unregister_();
 		if (client1tasks!=null) client1tasks.unregister_();
 	}
 	
 //	@BeforeClass()
 //	public void beforeClassSetup() {
 //		if (client2==null) throw new SkipException("The RHEL Personal tests require a second consumer.");
 //	}
 	
 	
 	
 	// Protected Methods ***********************************************************************
 
 
 //	protected static String personSubscriptionName = "RHEL Personal";
 //	protected static String systemSubscriptionName = "RHEL Personal Bits";
 
 	
 	
 	// Data Providers ***********************************************************************
 
 	
 	@DataProvider(name="getRHELPersonalData")
 	public Object[][] getRHELPersonalDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRHELPersonalDataAsListOfLists());
 	}
 	
 	protected List<List<Object>> getRHELPersonalDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		//										String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName
 		if (isServerOnPremises) {
 			ll.add(Arrays.asList(new Object[]{	clientusername,				clientpassword,				"RHEL Personal",					"RHEL Personal Bits",			"RHEL Personal Bits"} ));
 		} else {
 			ll.add(Arrays.asList(new Object[]{	"test5",					"redhat",					"Red Hat Personal Edition",			"RHEL for Physical Servers",	"Red Hat Enterprise Linux Server"} ));
 		}
 		
 		return ll;
 	}
 }
