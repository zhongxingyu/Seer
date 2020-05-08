 package com.redhat.qe.sm.cli.tests;
 
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
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerTestScript;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 
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
 Subpools are currently something that is currently pretty specific to RH Personal, but I will try and explain it generally first...
 
 In most cases, a pool is a 1-to-1 match with a subscription - so a subscription with quantity 20 for "Super Cool Linux" gives you a pool with the same quantity and product(s).  There are special cases where the act of consuming an entitlement from one pool actually spins off a new pool as a result.  A case for this might be "Developer Tools" where a subscription for a 10 person license is purchased (quantity 10), and when someone consumes an entitlement from that pool, a new sub-pool is created specifically for that user.  Each system that this user installs the product on pulls from this sub-pool, and when this user gives up his/her seat (unbinds from the original pool), the sub-pool and all of its entitlements are removed, which means that any systems that that "Developer Tools" installed by this user are no longer in compliance.
 
 We are using this same construct to model the RH Personal case, where the "person" consumes an entitlement for RHEL Personal, and any systems that want to install RHEL on are entitled off of the created sub-pool.
 
 I'm not really sure if this helps any, but here is the original design doc:  https://engineering.redhat.com/trac/Entitlement/wiki/RHPersonalDevTools
 
  - Justin
  */
 
 
 @Test(groups={"rhelPersonal"})
 public class RHELPersonalTests extends SubscriptionManagerTestScript{
 	protected List<String> consumerIds = new ArrayList<String>();
 	protected String personConsumerId = null;
 	protected int multipleSystems = 4;	// multiple (unlimited)
 	
 
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager-cli: Ensure RHEL Personal Bits are available and unlimited after a person has subscribed to RHEL Personal",
 			groups={"EnsureSubPoolIsAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test", "RHELPersonal"/*, "blockedByBug-624816"*/},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	@ImplementsTCMS(id="55702,55718")
 	public void EnsureSubPoolIsAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 //		if (!isServerOnPremises) throw new SkipException("Currently this test is designed only for on-premises.");	//TODO Make this work for IT too.  jsefler 8/12/2010 
 		if (client2==null) throw new SkipException("This test requires a second consumer.");
 		if (consumerUsername.equals("admin")) throw new SkipException("This test requires that the client user ("+consumerUsername+") is NOT admin.");
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=624423 - jsefler 8/16/2010
 		Boolean invokeWorkaroundWhileBugIsOpen = false;
		try {String bugId="624423"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			servertasks.restartTomcat();
 		} // END OF WORKAROUND
 		
 		
 		SubscriptionPool pool = null;
 		
 		
 		log.info("Register client2 under username '"+consumerUsername+"' as a system and assert that '"+systemSubscriptionName+"' is NOT yet available...");
 		client2tasks.unregister();
 		client1tasks.unregister();	// just in case client1 is still registered as the person consumer
 		client2tasks.register(consumerUsername, consumerPassword, ConsumerType.system, null, null, null);
 		List<SubscriptionPool> client2BeforeSubscriptionPools = client2tasks.getCurrentlyAvailableSubscriptionPools();
 		pool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2BeforeSubscriptionPools);
 		Assert.assertTrue(pool==null,systemSubscriptionName+" is NOT yet available to client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"'.");
 
 		
 		log.info("Now register client1 under username '"+consumerUsername+"' as a person and subscribe to the '"+personSubscriptionName+"' subscription pool...");
 		client1tasks.unregister();
 		client1tasks.register(consumerUsername, consumerPassword, ConsumerType.person, null, null, null);
 		personConsumerId = client1tasks.getCurrentConsumerId();
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
 		Assert.assertTrue(
 				client2AfterSubscriptionPools.containsAll(client2BeforeSubscriptionPools) &&
 				client2AfterSubscriptionPools.contains(systemSubscriptionPool) &&
 				client2AfterSubscriptionPools.size()==client2BeforeSubscriptionPools.size()+1,
 				"The list of available subscription pools seen by client2 increases only by '"+systemSubscriptionName+"' pool: "+systemSubscriptionPool);
 	}
 	 
 	 
 	@Test(	description="subscription-manager-cli: Ensure RHEL Personal Bits are consumable after a person has subscribed to RHEL Personal",
 			groups={"EnsureSubPoolIsConsumableAfterRegisteredPersonSubscribesToRHELPersonal_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureSubPoolIsAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	@ImplementsTCMS(id="55702,55718")
 	public void EnsureSubPoolIsConsumableAfterRegisteredPersonSubscribesToRHELPersonal_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 				
 		log.info("Now client2 (already registered as a system under username '"+consumerUsername+"') can now consume '"+systemSubscriptionName+"'...");
 		SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 		client2tasks.subscribeToSubscriptionPoolUsingPoolId(systemSubscriptionPool);
 		
 		
 		log.info("Now client2 should be consuming the product '"+systemConsumedProductName+"'...");
 		ProductSubscription systemProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 		Assert.assertTrue(systemProductSubscription!=null,systemConsumedProductName+" is now consumed on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"'.");
 		
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure that availability of RHEL Personal Bits is revoked once the person unsubscribes from RHEL Personal",
 			groups={"EnsureAvailabilityOfSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureSubPoolIsConsumableAfterRegisteredPersonSubscribesToRHELPersonal_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	//@ImplementsTCMS(id="55702,55718")
 	public void EnsureAvailabilityOfSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 		
 		log.info("Unsubscribe client2 (already registered as a system under username '"+consumerUsername+"') from all currently consumed product subscriptions...");
 		client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		
 		log.info("Unsubscribe client1 (already registered as a person under username '"+consumerUsername+"') from product subscription '"+personSubscriptionName+"'...");
 		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 
 		
 		log.info("Now verify that client2 (already registered as a system under username '"+consumerUsername+"') can no longer subscribe to the '"+systemSubscriptionName+"' pool...");
 		SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertTrue(systemSubscriptionPool==null,systemSubscriptionName+" is no longer available on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"'.");
 	}
 	
 	
 //	@Test(	description="subscription-manager-cli: Ensure that the entitlement cert for RHEL Personal Bits is revoked once the person unsubscribes from RHEL Personal",
 //			groups={"EnsureEntitlementCertForSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test","RHELPersonal"},
 //			dependsOnGroups={"EnsureAvailabilityOfSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test"},
 //			dataProvider="getRHELPersonalData",
 //			enabled=true)
 //	@ImplementsTCMS(id="58898")
 //	// 1) unsubscribe person from personal pool while systems are subscribed to subpool (scenario from calfanso@redhat.com)
 //	public void EnsureEntitlementCertForSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 //		log.info("Making sure the clients are not subscribed to anything...");
 //		client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		
 //		log.info("Subscribe client1 (already registered as a person under username '"+consumerUsername+"') to product subscription '"+personSubscriptionName+"'...");
 //		SubscriptionPool personSubscriptionPool = client1tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",personSubscriptionName,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 //		Assert.assertTrue(personSubscriptionPool!=null,personSubscriptionName+" is available to user '"+consumerUsername+"' registered as a person.");
 //		client1tasks.subscribe(personSubscriptionPool.poolId, null, null, null, null);
 //
 //		
 //		log.info("Subscribe client2 (already registered as a system under username '"+consumerUsername+"') to product subscription "+systemSubscriptionName+"...");
 //		SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 //		client2tasks.subscribeToSubscriptionPoolUsingPoolId(systemSubscriptionPool);
 //		ProductSubscription systemProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 //		Assert.assertTrue(systemProductSubscription!=null,systemConsumedProductName+" is now consumed on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"'.");
 //
 //		
 //		log.info("Now, unsubscribe the person on client 1 from the '"+personSubscriptionName+"' pool and update the rhsmcertd frequency to 1 minute on client2.  Then assert that the '"+systemSubscriptionName+"' gets revoked from client2.");
 //		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		int certFrequencyMinutes = 1;
 //		client2tasks.changeCertFrequency(certFrequencyMinutes, true);
 //		systemProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 //		Assert.assertTrue(systemProductSubscription==null,systemConsumedProductName+" was revoked on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"' after the certFrquency of '"+certFrequencyMinutes+"' minutes since this same person has unsubscribed from the "+personSubscriptionName+" on client1");
 //	}
 
 	
 	@Test(	description="subscription-manager-cli: Ensure that multiple (unlimited) systems can subscribe to subpool",
 			groups={"SubscribeMultipleSystemsToSubPool_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureAvailabilityOfSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void SubscribeMultipleSystemsToSubPool_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 		log.info("Making sure the clients are not subscribed to anything...");
 		client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		client2tasks.unregister();
 		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		log.info("Subscribe client1 (already registered as a person under username '"+consumerUsername+"') to product subscription '"+personSubscriptionName+"'...");
 		SubscriptionPool personSubscriptionPool = client1tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",personSubscriptionName,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertTrue(personSubscriptionPool!=null,personSubscriptionName+" is available to user '"+consumerUsername+"' registered as a person.");
 		client1tasks.subscribe(personSubscriptionPool.poolId, null, null, null, null);
 
 		log.info("Register "+multipleSystems+" new systems under username '"+consumerUsername+"' and subscribe to product subscription '"+systemSubscriptionName+"'...");
 		consumerIds = new ArrayList<String>();
 		for (int systemNum = 1; systemNum <=multipleSystems; systemNum++) {
 			client2.runCommandAndWait("rm -rf "+client2tasks.consumerCertDir);
 			String consumerId = client2tasks.getCurrentConsumerId(client2tasks.register(consumerUsername, consumerPassword, ConsumerType.system, null, null, Boolean.TRUE));
 			consumerIds.add(consumerId);
 			SubscriptionPool subPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 			log.info("Subscribing system '"+systemNum+"' ('"+consumerId+"' under username '"+consumerUsername+"') to product subscription '"+systemSubscriptionName+"'...");
 			client2tasks.subscribeToSubscriptionPoolUsingPoolId(subPool);
 			ProductSubscription productSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 			Assert.assertTrue(productSubscription!=null,systemConsumedProductName+" is now consumed by consumer '"+consumerId+"' (registered as a system under username '"+consumerUsername+"')");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure person consumer cannot unregister while subpools are consumed",
 			groups={"EnsurePersonCannotUnregisterWhileSubpoolsAreConsumed_Test","RHELPersonal", "blockedByBug-624063"},
 			dependsOnGroups={"SubscribeMultipleSystemsToSubPool_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void EnsurePersonCannotUnregisterWhileSubpoolsAreConsumed_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 		log.info("Assuming that multiple systems have subscribed to subpool '"+systemSubscriptionName+"' in prior testcase...");
 	
 		// REFERENCE FIX TO https://bugzilla.redhat.com/show_bug.cgi?id=624063
 
 		log.info("Now, attempt to unregister the person on client 1 from the "+personSubscriptionName+" pool and assert the unregister is blocked.");
 		SSHCommandResult result = client1tasks.unregister_();
 		Assert.assertTrue(result.getStderr().startsWith("Cannot unregister due to outstanding entitlement:"),"Attempting to unregister the person consumer is blocked when another system is register by the same consumer."); // stderr: Cannot unregister due to outstanding entitlement: 9
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure that the entitlement certs for subscribed subpool is revoked once the person unsubscribes from RHEL Personal",
 			groups={"EnsureEntitlementCertForSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test","RHELPersonal"},
 			dependsOnGroups={"EnsurePersonCannotUnregisterWhileSubpoolsAreConsumed_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	@ImplementsTCMS(id="58898")
 	// 1) unsubscribe person from personal pool while systems are subscribed to subpool (scenario from calfanso@redhat.com)
 	public void EnsureEntitlementCertForSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 		log.info("Assuming that multiple systems have subscribed to subpool '"+systemSubscriptionName+"' in prior testcase...");
 	
 		log.info("Now, unsubscribe the person on client 1 from the '"+personSubscriptionName+"' and assert that the '"+systemConsumedProductName+"' and '"+systemSubscriptionName+"' gets revoked from the system consumers.");
 		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		log.info("Now the the certs for '"+systemConsumedProductName+"' and '"+systemSubscriptionName+"' should be revoked from the system consumers...");
 		for (String consumerId : consumerIds) {
 			client2tasks.reregister(consumerUsername, consumerPassword,consumerId);
 			client2tasks.changeCertFrequency(1, true);	// give rhsmcertd a chance to download the consumer's certs
 			ProductSubscription productSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 			Assert.assertTrue(productSubscription==null,systemConsumedProductName+" is no longer consumed by '"+consumerId+"' (registered as a system under username '"+consumerUsername+"')");
 			SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 			Assert.assertTrue(systemSubscriptionPool==null,systemSubscriptionName+" is no longer available to consumer '"+consumerId+"' (registered as a system under username '"+consumerUsername+"')");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure that unsubscribing system from subpool while other systems are subscribed to subpool does not cause subpool to go away",
 			groups={"EnsureEntitlementCertForSubPoolIsNotRevokedOnceAnotherSystemUnsubscribesFromSubPool_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureEntitlementCertForSubPoolIsRevokedOncePersonUnsubscribesFromRHELPersonal_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	@ImplementsTCMS(id="58899")
 	// 2) unsubscribe system from subpool while other systems are subscribed to subpool, make sure the subpool doesn't go away (scenario from calfanso@redhat.com)
 	public void EnsureEntitlementCertForSubPoolIsNotRevokedOnceAnotherSystemUnsubscribesFromSubPool_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 		SubscribeMultipleSystemsToSubPool_Test(consumerUsername, consumerPassword, personSubscriptionName, systemSubscriptionName, systemConsumedProductName);
 		
 		log.info("Now start unsubscribing each system from the consumed product '"+systemConsumedProductName+"' and assert the sub pool '"+systemSubscriptionName+"' is still available...");
 		for (String consumerId : consumerIds) {
			client2tasks.reregister(consumerUsername, consumerPassword,consumerId);
 			client2tasks.changeCertFrequency(1, true);	// give rhsmcertd a chance to download the consumer's certs
 			ProductSubscription productSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 			client2tasks.unsubscribeFromProductSubscription(productSubscription);
 			SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 			Assert.assertTrue(systemSubscriptionPool!=null,systemSubscriptionName+" is once again available to consumer '"+consumerId+"' (registered as a system under username '"+consumerUsername+"')");
 		}
 	}
 	
 	
 	
 	@Test(	description="subscription-manager-cli: Ensure that after unsubscribing all systems from a subpool, the subpool should not get deleted",
 			groups={"EnsureSubPoolIsNotDeletedAfterAllOtherSystemsUnsubscribeFromSubPool_Test","RHELPersonal"},
 			dependsOnGroups={"EnsureEntitlementCertForSubPoolIsNotRevokedOnceAnotherSystemUnsubscribesFromSubPool_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	@ImplementsTCMS(id="58907")
 	// 3) unsubscribe system from subpool as the last system subscribed, make sure the subpool doesn't get deleted (scenario from calfanso@redhat.com)
 	public void EnsureSubPoolIsNotDeletedAfterAllOtherSystemsUnsubscribeFromSubPool_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 		log.info("After having unsubscribed all systems from product '"+systemConsumedProductName+"' in the prior testcase , we will now verify that the subpool '"+systemSubscriptionName+"' has not been deleted and that all systems can still subscribe to it ...");
 
 		for (String consumerId : consumerIds) {
 			SSHCommandResult result = client2tasks.reregister(consumerUsername, consumerPassword,consumerId);
 			client2tasks.changeCertFrequency(1, true);	// give rhsmcertd a chance to download the consumer's certs
 			ProductSubscription productSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 			Assert.assertTrue(productSubscription==null,systemConsumedProductName+" is not consumed by consumer '"+consumerId+"' (registered as a system under username '"+consumerUsername+"')");
 			SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 			Assert.assertTrue(systemSubscriptionPool!=null,systemSubscriptionName+" is still available to consumer '"+consumerId+"' (registered as a system under username '"+consumerUsername+"')");
 		}
 		
 		log.info("Now that all the subscribers of '"+systemSubscriptionName+"' have unsubscribed from '"+systemConsumedProductName+"', the person consumer should be able to unregister without being blocked due to outstanding entitlements...");
 		client1tasks.unregister();		
 	}
 	
 	
 	
 //	@Test(	description="subscription-manager-cli: Ensure that the entitlement cert for RHEL Personal Bits is revoked once the person unregisters",
 //			groups={"EnsureEntitlementCertForRHELPersonalBitsIsRevokedOncePersonUnregisters_Test","RHELPersonal", "blockedByBug-624063"},
 //			dependsOnGroups={"EnsureSubPoolIsNotDeletedAfterAllOtherSystemsUnsubscribeFromSubPool_Test"},
 //			dataProvider="getRHELPersonalData",
 //			enabled=true)
 //	//@ImplementsTCMS(id="55702,55718")
 //	public void EnsureEntitlementCertForRHELPersonalBitsIsRevokedOncePersonUnregisters_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 //		log.info("Making sure the clients are not subscribed to anything...");
 //		client2tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		client1tasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		
 //		log.info("Subscribe client1 (already registered as a person under username '"+consumerUsername+"') to subscription '"+personSubscriptionName+"' pool...");
 //		SubscriptionPool personSubscriptionPool = client1tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",personSubscriptionName,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
 //		Assert.assertTrue(personSubscriptionPool!=null,personSubscriptionName+" is available to user '"+consumerUsername+"' registered as a person.");
 //		client1tasks.subscribe(personSubscriptionPool.poolId, null, null, null, null);
 //
 //		
 //		log.info("Subscribe client2 (already registered as a system under username '"+consumerUsername+"') to subscription '"+systemSubscriptionName+"' pool...");
 //		SubscriptionPool systemSubscriptionPool = client2tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",systemSubscriptionName,client2tasks.getCurrentlyAvailableSubscriptionPools());
 //		client2tasks.subscribeToSubscriptionPoolUsingPoolId(systemSubscriptionPool);
 //		ProductSubscription systemProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemSubscriptionName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 //		Assert.assertTrue(systemProductSubscription!=null,systemSubscriptionName+" is now consumed on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"'.");
 //
 //		
 ////		log.info("Now, unregister the person on client 1 from the "+personSubscriptionName+" pool and update the rhsmcertd frequency to 1 minute on client2.  Then assert that the '"+systemSubscriptionName+"' gets revoked from client2.");
 ////		client1tasks.unregister();
 ////		int certFrequencyMinutes = 1;
 ////		client2tasks.changeCertFrequency(certFrequencyMinutes);
 ////		sleep(certFrequencyMinutes*60*1000);sleep(10000);	// give the rhsmcertd a chance check in with the candlepin server and update the certs
 ////		systemProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemSubscriptionName,client2tasks.getCurrentlyConsumedProductSubscriptions());
 ////		Assert.assertTrue(systemProductSubscription==null,systemSubscriptionName+" was revoked on client2 system '"+client2.getConnection().getHostname()+"' registered under user '"+consumerUsername+"' after the certFrquency of '"+certFrequencyMinutes+"' minutes since this same person has unregistered from the '"+personSubscriptionName+"' on client1");
 //	
 //		// REFERENCE FIX TO https://bugzilla.redhat.com/show_bug.cgi?id=624063
 //		// FIXME: should probably rename this test
 //		log.info("Now, attempt to unregister the person on client 1 from the "+personSubscriptionName+" pool and assert the unregister is blocked.");
 //		SSHCommandResult result = client1tasks.unregister_();
 //		Assert.assertTrue(result.getStderr().startsWith("Cannot unregister due to outstanding entitlement:"),"Attempting to unregister the person consumer is blocked when another system is register by the same consumer."); // stderr: Cannot unregister due to outstanding entitlement: 9
 //		client2tasks.unsubscribeFromProductSubscription(systemProductSubscription);
 //		client1tasks.unregister();
 //		client2tasks.unregister();
 //		
 //	}
 	
 	
 	@Test(	description="subscription-manager-cli: verify system autosubscribe consumes subpool RHEL Personal Bits",
 			groups={"EnsureSystemAutosubscribeConsumesSubPool_Test","RHELPersonal", "blockedByBug-637937"},
 			dependsOnGroups={"EnsureSubPoolIsNotDeletedAfterAllOtherSystemsUnsubscribeFromSubPool_Test"},
 			dataProvider="getRHELPersonalData",
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void EnsureSystemAutosubscribeConsumesSubPool_Test(String consumerUsername,	String consumerPassword,	String personSubscriptionName,		String systemSubscriptionName,	String systemConsumedProductName) {
 		log.info("Now register client1 under username '"+consumerUsername+"' as a person and subscribe to the '"+personSubscriptionName+"' subscription pool...");
 		client1tasks.unregister();
 		client1tasks.register(consumerUsername, consumerPassword, ConsumerType.person, null, null, null);
 		personConsumerId = client1tasks.getCurrentConsumerId();
 		SubscriptionPool pool = client1tasks.findSubscriptionPoolWithMatchingFieldFromList("subscriptionName",personSubscriptionName,client1tasks.getCurrentlyAllAvailableSubscriptionPools());
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
 		
 		
 		log.info("Now register client2 under username '"+consumerUsername+"' as a system with autosubscribe to assert that '"+systemConsumedProductName+"' gets consumed...");
 		client2tasks.unregister();
 		client2tasks.register(consumerUsername, consumerPassword, ConsumerType.system, null, Boolean.TRUE, null);
 		List<ProductSubscription> client2ConsumedProductSubscriptions = client2tasks.getCurrentlyConsumedProductSubscriptions();
 		
 		
 		ProductSubscription consumedProductSubscription = client2tasks.findProductSubscriptionWithMatchingFieldFromList("productName",systemConsumedProductName,client2ConsumedProductSubscriptions);
 		Assert.assertTrue(consumedProductSubscription!=null,systemConsumedProductName+" has been autosubscribed by client2 '"+client2.getConnection().getHostname()+"' (registered as a system under username '"+consumerUsername+"')");
 	}
 	
 	
 	
 	
 	// Configuration Methods ***********************************************************************
 	
 	
 	@AfterGroups(groups={}, value={"RHELPersonal"}, alwaysRun=true)
 	public void teardownAfterEnsureSubPoolIsAvailableAfterRegisteredPersonSubscribesToRHELPersonal_Test() {
 		if (client2tasks!=null) {
 			client2tasks.unsubscribe_(null);
 			client2tasks.unregister_();
 		}
 
 		if (client1tasks!=null) {
 			if (personConsumerId!=null) {
 				client1tasks.reregister_(client1username, client1password, personConsumerId);
 			}
 			client1tasks.unsubscribe_(null);
 			client1tasks.unregister_();
 		}
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
