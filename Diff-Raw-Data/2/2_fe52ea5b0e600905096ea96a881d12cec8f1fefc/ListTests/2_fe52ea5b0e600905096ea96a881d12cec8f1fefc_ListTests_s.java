 package com.redhat.qe.sm.cli.tests;
 
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.testng.SkipException;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductNamespace;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  *  @author ssalevan
  *  @author jsefler
  *
  */
 @Test(groups={"list"})
 public class ListTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager-cli: list available subscriptions (when not consuming)",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=41678)
 	public void EnsureAvailableSubscriptionsListed_Test() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		String availableSubscriptionPools = clienttasks.listAvailableSubscriptionPools().getStdout();
 		Assert.assertContainsMatch(availableSubscriptionPools, "Available Subscriptions","" +
 				"Available Subscriptions are listed for '"+clientusername+"' to consume.");
 		Assert.assertContainsNoMatch(availableSubscriptionPools, "No Available subscription pools to list",
 				"Available Subscriptions are listed for '"+clientusername+"' to consume.");
 
 		log.warning("These manual TCMS instructions are not really achievable in this automated test...");
 		log.warning(" * List produced matches the known data contained on the Candlepin server");
 		log.warning(" * Confirm that the marketing names match.. see prereq link https://engineering.redhat.com/trac/IntegratedMgmtQE/wiki/sm-prerequisites");
 		log.warning(" * Match the marketing names w/ https://www.redhat.com/products/");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: list available subscriptions",
 			groups={},
 			dataProvider="getSubscriptionPoolProductIdData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41678)
 	public void EnsureAvailableSubscriptionsListed_Test(String productId, String[] bundledProductNames) {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool, "Expected SubscriptionPool with ProductId '"+productId+"' is available for subscribing: "+pool);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: list consumed entitlements (when not consuming)",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=41679)
 	public void EnsureConsumedEntitlementsListed_Test() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		String consumedProductSubscription = clienttasks.listConsumedProductSubscriptions().getStdout();
 		Assert.assertContainsMatch(consumedProductSubscription, "No Consumed subscription pools to list",
 				"No Consumed subscription pools listed for '"+clientusername+"' after registering (without autosubscribe).");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: list consumed entitlements",
 			groups={},
 			dataProvider="getSubscriptionPoolProductIdData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41679)
 	public void EnsureConsumedEntitlementsListed_Test(String productId, String[] bundledProductNames) {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool, "Expected SubscriptionPool with ProductId '"+productId+"' is available for subscribing: "+pool);
 		EntitlementCert  entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool));
 		List<ProductSubscription> consumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertTrue(!consumedProductSubscriptions.isEmpty(),"The list of Consumed Product Subscription is NOT empty after subscribing to a pool with ProductId '"+productId+"'.");
 		for (ProductSubscription productSubscription : consumedProductSubscriptions) {
 			Assert.assertEquals(productSubscription.serialNumber, entitlementCert.serialNumber,
 					"SerialNumber of Consumed Product Subscription matches the serial number from the current entitlement certificate.");
 		}	
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: list installed products",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void EnsureInstalledProductsListed_Test() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 
 		List <ProductCert> productCerts = clienttasks.getCurrentProductCerts();
 		String installedProductsAsString = clienttasks.listInstalledProducts().getStdout();
 		//List <InstalledProduct> installedProducts = clienttasks.getCurrentlyInstalledProducts();
 		List <InstalledProduct> installedProducts = InstalledProduct.parse(installedProductsAsString);
 
 		// assert some stdout
 		if (installedProducts.size()>0) {
 			Assert.assertContainsMatch(installedProductsAsString, "Installed Product Status");
 		}
 		
 		// assert the number of installed product matches the product certs installed
 		Assert.assertEquals(installedProducts.size(), productCerts.size(), "A single product is reported as installed for each product cert found in "+clienttasks.productCertDir);
 
 		// assert that each of the installed product certs are listed in installedProducts as "Not Subscribed"
 		for (InstalledProduct installedProduct : installedProducts) {
 			boolean foundInstalledProductMatchingProductCert=false;
 			for (ProductCert productCert : productCerts) {
 				if (installedProduct.productName.equals(productCert.productName)) {
 					foundInstalledProductMatchingProductCert = true;
 					break;
 				}
 			}
 			Assert.assertTrue(foundInstalledProductMatchingProductCert, "The installed product cert for '"+installedProduct.productName+"' is reported by subscription-manager as installed.");
 			Assert.assertEquals(installedProduct.status, "Not Subscribed", "A newly registered system should not be subscribed to installed product '"+installedProduct.productName+"'.");
 		}
 
 	}
 	
 	
 	@Test(	description="subscription-manager: ensure list [--installed] produce the same results",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void EnsureListAndListInstalledAreTheSame_Test() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 
 		// assert same results when no subscribed to anything...
 		log.info("assert list [--installed] produce same results when not subscribed to anything...");
 		SSHCommandResult listResult = clienttasks.list_(null, null, null, null, null, null, null);
 		SSHCommandResult listInstalledResult = clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null);
 		
 		Assert.assertEquals(listResult.getStdout(), listInstalledResult.getStdout(), "'list' and 'list --installed' produce the same stdOut results.");
 		Assert.assertEquals(listResult.getStderr(), listInstalledResult.getStderr(), "'list' and 'list --installed' produce the same stdErr results.");
 		Assert.assertEquals(listResult.getExitCode(), listInstalledResult.getExitCode(), "'list' and 'list --installed' produce the same exitCode results.");
 		
 		
 		// assert same results when subscribed to something...
 		log.info("assert list [--installed] produce same results when subscribed to something...");
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPool(pool);
 		listResult = clienttasks.list_(null, null, null, null, null, null, null);
 		listInstalledResult = clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null);
 		
 		Assert.assertEquals(listResult.getStdout(), listInstalledResult.getStdout(), "'list' and 'list --installed' produce the same stdOut results.");
 		Assert.assertEquals(listResult.getStderr(), listInstalledResult.getStderr(), "'list' and 'list --installed' produce the same stdErr results.");
 		Assert.assertEquals(listResult.getExitCode(), listInstalledResult.getExitCode(), "'list' and 'list --installed' produce the same exitCode results.");
 	}
 	
 
 	@Test(	description="subscription-manager: list of consumed entitlements should display consumed product marketing name",
 			groups={},
 			dataProvider="getAllEntitlementCertsData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=48092, fromPlan=2481)
 	public void EnsureListConsumedMatchesProductsListedInTheEntitlementCerts_Test(EntitlementCert entitlementCert) {
 
 		// assert: The list of consumed products matches the products listed in the entitlement cert
 		List<ProductSubscription> productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		List<ProductSubscription> productSubscriptionsWithMatchingSerialNumber = ProductSubscription.findAllInstancesWithMatchingFieldFromList("serialNumber", entitlementCert.serialNumber, productSubscriptions);
 		//Assert.assertTrue(productSubscriptionsWithMatchingSerialNumber.size()>0, "Found consumed product subscription(s) whose SerialNumber matches this entitlement cert: "+entitlementCert);
		Assert.assertEquals(productSubscriptionsWithMatchingSerialNumber.size(),entitlementCert.productNamespaces.size(), "Found consumed product subscription(s) for each of the bundleProducts (total of '"+entitlementCert.productNamespaces.size()+"' expected) whose SerialNumber matches this entitlement cert: "+entitlementCert);
 		int productSubscriptionsWithMatchingSerialNumberSizeExpected = entitlementCert.productNamespaces.size()==0?1:entitlementCert.productNamespaces.size(); // when there are 0 bundledProducts, we are still consuming 1 ProductSubscription
 		Assert.assertEquals(productSubscriptionsWithMatchingSerialNumber.size(),productSubscriptionsWithMatchingSerialNumberSizeExpected, "Found consumed product subscription(s) for each of the bundleProducts (total of '"+productSubscriptionsWithMatchingSerialNumberSizeExpected+"' expected) whose SerialNumber matches this entitlement cert: "+entitlementCert);
 
 		for (ProductNamespace productNamespace : entitlementCert.productNamespaces) {
 			List<ProductSubscription> matchingProductSubscriptions = ProductSubscription.findAllInstancesWithMatchingFieldFromList("productName", productNamespace.name, productSubscriptionsWithMatchingSerialNumber);
 			Assert.assertEquals(matchingProductSubscriptions.size(), 1, "Found one bundledProduct name '"+productNamespace.name+"' in the list of consumed product subscriptions whose SerialNumber matches this entitlement cert: "+entitlementCert);
 			ProductSubscription correspondingProductSubscription = matchingProductSubscriptions.get(0);
 			Assert.assertEquals(correspondingProductSubscription.productName, productNamespace.name, "productName from ProductSubscription in list --consumed matches productName from ProductNamespace in EntitlementCert.");
 			Assert.assertEquals(correspondingProductSubscription.contractNumber, entitlementCert.orderNamespace.contractNumber, "contractNumber from ProductSubscription in list --consumed matches contractNumber from OrderNamespace in EntitlementCert.");
 			Assert.assertEquals(correspondingProductSubscription.accountNumber, entitlementCert.orderNamespace.accountNumber, "accountNumber from ProductSubscription in list --consumed matches accountNumber from OrderNamespace in EntitlementCert.");
 			Assert.assertEquals(correspondingProductSubscription.serialNumber, entitlementCert.serialNumber, "serialNumber from ProductSubscription in list --consumed matches serialNumber from EntitlementCert.");
 			
 			Calendar now = Calendar.getInstance();
 			if (now.after(entitlementCert.orderNamespace.startDate) && now.before(entitlementCert.orderNamespace.endDate)) {
 				Assert.assertTrue(correspondingProductSubscription.isActive, "isActive is True when the current time ("+EntitlementCert.formatDateString(now)+") is between the start/end dates in the EntitlementCert: "+entitlementCert);
 			} else {
 				Assert.assertFalse(correspondingProductSubscription.isActive, "isActive is False when the current time ("+EntitlementCert.formatDateString(now)+") is NOT between the start/end dates in the EntitlementCert: "+entitlementCert);
 			}
 			
 			// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=660713 - jsefler 12/12/2010
 			Boolean invokeWorkaroundWhileBugIsOpen = true;
 			try {String bugId="660713"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				log.warning("The workaround while this bug is open is to skip the assertion that: startDates and endDates match");
 			} else {
 			// END OF WORKAROUND
 			Assert.assertEquals(ProductSubscription.formatDateString(correspondingProductSubscription.startDate), ProductSubscription.formatDateString(entitlementCert.orderNamespace.startDate), "startDate from ProductSubscription in list --consumed matches startDate from OrderNamespace in EntitlementCert.");
 			Assert.assertEquals(ProductSubscription.formatDateString(correspondingProductSubscription.endDate), ProductSubscription.formatDateString(entitlementCert.orderNamespace.endDate), "endDate from ProductSubscription in list --consumed matches endDate from OrderNamespace in EntitlementCert.");
 			}
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: RHEL Personal should be the only available subscription to a consumer registered as type person",
 			groups={"EnsureOnlyRHELPersonalIsAvailableToRegisteredPerson_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void EnsureOnlyRHELPersonalIsAvailableToRegisteredPerson_Test() {
 		String rhelPersonalProductId = getProperty("sm.rhpersonal.productId", "");
 		if (rhelPersonalProductId.equals("")) throw new SkipException("This testcase requires specification of a RHPERSONAL_PRODUCTID.");
 		
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, ConsumerType.person, null, null, null, null, null, null, null);
 		
 
 		// assert that RHEL Personal is available to this person consumer
 		List<SubscriptionPool> subscriptionPools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool rhelPersonalPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", rhelPersonalProductId, subscriptionPools);
 		Assert.assertNotNull(rhelPersonalPool,"RHEL Personal ProductId '"+rhelPersonalProductId+"' is available to this consumer registered as type person");
 		
 		// assert that RHEL Personal is the only available pool to this person consumer
 		for (SubscriptionPool subscriptionPool : subscriptionPools) {
 			Assert.assertEquals(subscriptionPool.productId,rhelPersonalPool.productId, "RHEL Personal ProductId '"+rhelPersonalProductId+"' is the ONLY product consumable from an available subscription pool to this consumer registered as type person");
 		}
 	}
 	@AfterGroups(groups={}, value="EnsureOnlyRHELPersonalIsAvailableToRegisteredPerson_Test", alwaysRun=true)
 	public void teardownAfterEnsureOnlyRHELPersonalIsAvailableToRegisteredPerson_Test() {
 		if (clienttasks!=null) clienttasks.unregister_(null, null, null);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: RHEL Personal should not be an available subscription to a consumer registered as type system",
 			groups={"EnsureRHELPersonalIsNotAvailableToRegisteredSystem_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void EnsureRHELPersonalIsNotAvailableToRegisteredSystem_Test() {
 		String rhelPersonalProductId = getProperty("sm.rhpersonal.productId", "");
 		if (rhelPersonalProductId.equals("")) throw new SkipException("This testcase requires specification of a RHPERSONAL_PRODUCTID.");
 
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername, clientpassword, ConsumerType.system, null, null, null, null, null, null, null);
 		SubscriptionPool rhelPersonalPool = null;
 		
 		// assert that RHEL Personal *is not* included in --available subscription pools
 		rhelPersonalPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", rhelPersonalProductId, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNull(rhelPersonalPool,"RHEL ProductId '"+rhelPersonalProductId+"' is NOT available to this consumer from any available subscription pool when registered as type system");
 		
 		// also assert that RHEL Personal *is* included in --all --available subscription pools
 		rhelPersonalPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", rhelPersonalProductId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNotNull(rhelPersonalPool,"RHEL ProductId '"+rhelPersonalProductId+"' is included in --all --available subscription pools when registered as type system");
 	}
 	@AfterGroups(groups={}, value="EnsureRHELPersonalIsNotAvailableToRegisteredSystem_Test", alwaysRun=true)
 	public void teardownAfterEnsureRHELPersonalIsNotAvailableToRegisteredSystem_Test() {
 		if (clienttasks!=null) clienttasks.unregister_(null, null, null);
 	}
 	
 
 	
 	// Data Providers ***********************************************************************
 	
 	
 }
