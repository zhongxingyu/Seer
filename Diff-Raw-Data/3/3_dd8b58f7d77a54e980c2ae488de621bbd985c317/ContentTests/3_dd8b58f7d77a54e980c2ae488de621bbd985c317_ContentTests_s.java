 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.ContentNamespace;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.sm.data.YumRepo;
 import com.redhat.qe.tools.RemoteFileTasks;
 
 /**
  * @author jsefler
  *
  */
 @Test(groups={"ContentTests"})
 public class ContentTests extends SubscriptionManagerCLITestScript{
 
 	
 	// Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager Yum plugin: enable/disable",
 			groups={"EnableDisableYumRepoAndVerifyContentAvailable_Test","blockedByBug-804227"},
 			//dataProvider="getAvailableSubscriptionPoolsData",	// very thorough, but takes too long to execute and rarely finds more bugs
 			dataProvider="getRandomSubsetOfAvailableSubscriptionPoolsData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41696,fromPlan=2479)
 	public void EnableDisableYumRepoAndVerifyContentAvailable_Test(SubscriptionPool pool) throws JSONException, Exception {
 
 		// get the currently installed product certs to be used when checking for conditional content tagging
 		List<ProductCert> currentProductCerts = clienttasks.getCurrentProductCerts();
 
 		log.info("Before beginning this test, we will stop the rhsmcertd so that it does not interfere with this test..");
 		clienttasks.stop_rhsmcertd();
 		
 		// Edit /etc/yum/pluginconf.d/rhsmplugin.conf and ensure that the enabled directive is set to 1
 		log.info("Making sure that the rhsm plugin conf file '"+clienttasks.rhsmPluginConfFile+"' is enabled with enabled=1..");
 		clienttasks.updateConfFileParameter(clienttasks.rhsmPluginConfFile, "enabled", "1");
 		
 		log.info("Subscribe to the pool and start testing that yum repolist reports the expected repo id/labels...");
 		File entitlementCertFile = clienttasks.subscribeToSubscriptionPool_(pool);
 		Assert.assertNotNull(entitlementCertFile, "Found the entitlement cert file that was granted after subscribing to pool: "+pool);
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		
 		// 1. Run a 'yum repolist' and get a list of all of the available repositories corresponding to your entitled products
 		// 1. Repolist contains repositories corresponding to your entitled products
 		ArrayList<String> repolist = clienttasks.getYumRepolist("enabled");
 		for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 			if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 			if (contentNamespace.enabled.equals("1")) {
 				if (clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 					Assert.assertTrue(repolist.contains(contentNamespace.label),
 						"Yum repolist enabled includes enabled repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' enabled.");
 				} else {
 					Assert.assertFalse(repolist.contains(contentNamespace.label),
 						"Yum repolist enabled excludes enabled repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' enabled because not all requiredTags ("+contentNamespace.requiredTags+") in the contentNamespace are provided by the currently installed productCerts.");
 				}
 			} else {
 				Assert.assertFalse(repolist.contains(contentNamespace.label),
 					"Yum repolist enabled excludes disabled repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' enabled.");
 			}
 		}
 		repolist = clienttasks.getYumRepolist("disabled");
 		for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 			if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 			if (contentNamespace.enabled.equals("1")) {
 				Assert.assertFalse(repolist.contains(contentNamespace.label),
 					"Yum repolist disabled excludes enabled repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' enabled.");
 			} else {
 				Assert.assertTrue(repolist.contains(contentNamespace.label),
 					"Yum repolist disabled includes disabled repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' enabled.");
 			}
 		}
 		repolist = clienttasks.getYumRepolist("all");
 		for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 			if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 			if (clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 				Assert.assertTrue(repolist.contains(contentNamespace.label),
 					"Yum repolist all includes repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' enabled.");
 			} else {
 				Assert.assertFalse(repolist.contains(contentNamespace.label),
 					"Yum repolist all excludes repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' enabled because not all requiredTags ("+contentNamespace.requiredTags+") in the contentNamespace are provided by the currently installed productCerts.");
 			}
 		}
 
 		log.info("Unsubscribe from the pool and verify that yum repolist no longer reports the expected repo id/labels...");
 		clienttasks.unsubscribeFromSerialNumber(entitlementCert.serialNumber);
 		
 		repolist = clienttasks.getYumRepolist("all");
 		for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 			if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 			Assert.assertFalse(repolist.contains(contentNamespace.label),
 				"Yum repolist all excludes repo id/label '"+contentNamespace.label+"' after having unsubscribed from Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' enabled.");
 		}
 	
 		// Edit /etc/yum/pluginconf.d/rhsmplugin.conf and ensure that the enabled directive is set to 0
 		log.info("Now we will disable the rhsm plugin conf file '"+clienttasks.rhsmPluginConfFile+"' with enabled=0..");
 		clienttasks.updateConfFileParameter(clienttasks.rhsmPluginConfFile, "enabled", "0");
 		
 		log.info("Again let's subscribe to the same pool and verify that yum repolist does NOT report any of the entitled repo id/labels since the plugin has been disabled...");
 		entitlementCertFile = clienttasks.subscribeToSubscriptionPool_(pool);
 		Assert.assertNotNull(entitlementCertFile, "Found the entitlement cert file that was granted after subscribing to pool: "+pool);
 		entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 	
 		// 2. Run a 'yum repolist' and get a list of all of the available repositories corresponding to your entitled products
 		// 2. Repolist does not contain repositories corresponding to your entitled products
 		repolist = clienttasks.getYumRepolist("all");
 		for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 			if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 			Assert.assertFalse(repolist.contains(contentNamespace.label),
 				"Yum repolist all excludes repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' disabled.");
 		}
 		
 		log.info("Now we will restart the rhsmcertd and expect the repo list to be updated");
 		int minutes = 2;
 		clienttasks.restart_rhsmcertd(minutes, null, false, true);
 		repolist = clienttasks.getYumRepolist("all");
 		for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 			if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 			if (clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 				Assert.assertTrue(repolist.contains(contentNamespace.label),
 					"Yum repolist all now includes repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' disabled and run an update with rhsmcertd.");
 			} else {
 				Assert.assertFalse(repolist.contains(contentNamespace.label),
 					"Yum repolist all still excludes repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' disabled and run an update with rhsmcertd because not all requiredTags ("+contentNamespace.requiredTags+") in the contentNamespace are provided by the currently installed productCerts.");		
 			}
 		}
 		
 		log.info("Now we will unsubscribe from the pool and verify that yum repolist continues to report the repo id/labels until the next refresh from the rhsmcertd runs...");
 		clienttasks.unsubscribeFromSerialNumber(entitlementCert.serialNumber);
 		repolist = clienttasks.getYumRepolist("all");
 		for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 			if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 			if (clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 				Assert.assertTrue(repolist.contains(contentNamespace.label),
 					"Yum repolist all still includes repo id/label '"+contentNamespace.label+"' despite having unsubscribed from Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' disabled.");
 			} else {
 				Assert.assertFalse(repolist.contains(contentNamespace.label),
 					"Yum repolist all still excludes repo id/label '"+contentNamespace.label+"' despite having unsubscribed from Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' disabled because not all requiredTags ("+contentNamespace.requiredTags+") in the contentNamespace are provided by the currently installed productCerts.");
 			}
 		}
 		log.info("Wait for the next refresh by rhsmcertd to remove the repos from the yum repo file '"+clienttasks.redhatRepoFile+"'...");
 		sleep(minutes*60*1000);
 		repolist = clienttasks.getYumRepolist("all");
 		for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 			if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 			Assert.assertFalse(repolist.contains(contentNamespace.label),
 				"Yum repolist all finally excludes repo id/label '"+contentNamespace.label+"' after having unsubscribed from Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' with the rhsmPluginConfFile '"+clienttasks.rhsmPluginConfFile+"' disabled AND waiting for the next refresh by rhsmcertd.");
 		}
 	}
 	@AfterGroups(value="EnableDisableYumRepoAndVerifyContentAvailable_Test", alwaysRun=true)
 	protected void teardownAfterEnableDisableYumRepoAndVerifyContentAvailable_Test() {
 		clienttasks.updateConfFileParameter(clienttasks.rhsmPluginConfFile, "enabled", "1");
 		clienttasks.restart_rhsmcertd(Integer.valueOf(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "certFrequency")), null, false, null);
 	}
 	
 
 	
 	@Test(	description="subscription-manager content flag : Default content flag should enable",
 			groups={"AcceptanceTests","blockedByBug-804227"},
 	        enabled=true)
 	@ImplementsNitrateTest(caseId=47578,fromPlan=2479)
 	public void VerifyYumRepoListsEnabledContent_Test() throws JSONException, Exception{
 // Original code from ssalevan
 //	    ArrayList<String> repos = this.getYumRepolist();
 //	    
 //	    for (EntitlementCert cert:clienttasks.getCurrentEntitlementCerts()){
 //	    	if(cert.enabled.contains("1"))
 //	    		Assert.assertTrue(repos.contains(cert.label),
 //	    				"Yum reports enabled content subscribed to repo: " + cert.label);
 //	    	else
 //	    		Assert.assertFalse(repos.contains(cert.label),
 //	    				"Yum reports enabled content subscribed to repo: " + cert.label);
 //	    }
 		
 		List<ProductCert> currentProductCerts = clienttasks.getCurrentProductCerts();
 		
 		clienttasks.unregister(null, null, null);
 	    clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 	    if (clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively().size()<=0)
 	    	throw new SkipException("No available subscriptions were found.  Therefore we cannot perform this test.");
 	    List<EntitlementCert> entitlementCerts = clienttasks.getCurrentEntitlementCerts();
 	    Assert.assertTrue(!entitlementCerts.isEmpty(),"After subscribing to all available subscription pools, there must be some entitlements."); // or maybe we should skip when nothing is consumed 
 		ArrayList<String> repolist = clienttasks.getYumRepolist("enabled");
 		for (EntitlementCert entitlementCert : entitlementCerts) {
 			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 				if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 				if (contentNamespace.enabled.equals("1")) {
 					if (clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 						Assert.assertTrue(repolist.contains(contentNamespace.label),
 								"Yum repolist enabled includes enabled repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"'.");
 					} else {
 						Assert.assertFalse(repolist.contains(contentNamespace.label),
 								"Yum repolist enabled excludes enabled repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"' because not all requiredTags ("+contentNamespace.requiredTags+") in the contentNamespace are provided by the currently installed productCerts.");
 					}
 				} else {
 					Assert.assertFalse(repolist.contains(contentNamespace.label),
 						"Yum repolist enabled excludes disabled repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+entitlementCert.orderNamespace.productId+"'.");
 				}
 			}
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager content flag : gpgcheck value in redhat.repo should be disabled when gpg_url is empty or null",
 			groups={"AcceptanceTests","blockedByBug-741293","blockedByBug-805690"},
 	        enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyGpgCheckValuesInYumRepos_Test() throws JSONException, Exception {
 		//	[root@jsefler-r63-server ~]# cat /etc/yum.repos.d/redhat.repo 
 		//	#
 		//	# Certificate-Based Repositories
 		//	# Managed by (rhsm) subscription-manager
 		//	#
 		//	# If this file is empty and this system is subscribed consider 
 		//	# a "yum repolist" to refresh available repos
 		//	#
 		//
 		//	[content-label]
 		//	name = content
 		//	baseurl = https://cdn.redhat.com/foo/path
 		//	enabled = 1
 		//	gpgcheck = 1
 		//	gpgkey = https://cdn.redhat.com/foo/path/gpg/
 		//	sslverify = 1
 		//	sslcacert = /etc/rhsm/ca/redhat-uep.pem
 		//	sslclientkey = /etc/pki/entitlement/5488047145460852736-key.pem
 		//	sslclientcert = /etc/pki/entitlement/5488047145460852736.pem
 		//	metadata_expire = 0
 		
 		//	1.3.6.1.4.1.2312.9.2 (Content Namespace)
 		//	1.3.6.1.4.1.2312.9.2.<content_hash> (Red Hat Enterprise Linux (core server))
 		//	  1.3.6.1.4.1.2312.9.2.<content_hash>.1 (Yum repo type))
 		//	    1.3.6.1.4.1.2312.9.2.<content_hash>.1.1 (Name) : Red Hat Enterprise Linux (core server)
 		//	    1.3.6.1.4.1.2312.9.2.<content_hash>.1.2 (Label) : rhel-server
 		//	    1.3.6.1.4.1.2312.9.2.<content_hash>.1.5 (Vendor ID): %Red_Hat_Id% or %Red_Hat_Label%
 		//	    1.3.6.1.4.1.2312.9.2.<content_hash>.1.6 (Download URL): content/rhel-server/$releasever/$basearch
 		//	    1.3.6.1.4.1.2312.9.2.<content_hash>.1.7 (GPG Key URL): file:///etc/pki/rpm-gpg/RPM-GPG-KEY-redhat-release
 		//	    1.3.6.1.4.1.2312.9.2.<content_hash>.1.8 (Enabled): 1
 		//	    1.3.6.1.4.1.2312.9.2.<content_hash>.1.9 (Metadata Expire Seconds): 604800
 		//	    1.3.6.1.4.1.2312.9.2.<content_hash>.1.10 (Required Tags): TAG1,TAG2,TAG3
 		
 		List<ProductCert> currentProductCerts = clienttasks.getCurrentProductCerts();
 		
 	    clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, true, false, null, null, null);
 	    if (clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively().size()<=0)
 	    	throw new SkipException("No available subscriptions were found.  Therefore we cannot perform this test.");
 	    List<EntitlementCert> entitlementCerts = clienttasks.getCurrentEntitlementCerts();
 	    Assert.assertTrue(!entitlementCerts.isEmpty(),"After subscribing to all available subscription pools, there must be some entitlements."); // or maybe we should skip when nothing is consumed 
 
 	    
 	    ArrayList<String> repolist = clienttasks.getYumRepolist("enabled");
 	    List<YumRepo> yumRepos = clienttasks.getCurrentlySubscribedYumRepos();
 		for (EntitlementCert entitlementCert : entitlementCerts) {
 			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 				if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 				if (contentNamespace.enabled.equals("1")) {
 					if (!clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) continue;
 					YumRepo yumRepo = YumRepo.findFirstInstanceWithMatchingFieldFromList("id"/*label*/, contentNamespace.label, yumRepos);
 					
 					// case 1: contentNamespace.gpgKeyUrl==null
 					if (contentNamespace.gpgKeyUrl==null) {
 						Assert.assertFalse(yumRepo.gpgcheck,
 								"gpgcheck is False for Yum repo '"+yumRepo.id+"' when corresponding entitlement contentNamespace has a null gpgKeyUrl: contentNamespace: "+contentNamespace);
 						Assert.assertNull(yumRepo.gpgkey,
 								"gpgkey is not set for Yum repo '"+yumRepo.id+"' when corresponding entitlement contentNamespace has a null gpgKeyUrl: contentNamespace: "+contentNamespace);
 					
 					// case 2: contentNamespace.gpgKeyUrl==""
 					} else if (contentNamespace.gpgKeyUrl.equals("")) {
 						Assert.assertFalse(yumRepo.gpgcheck,
 								"gpgcheck is False for Yum repo '"+yumRepo.id+"' when corresponding entitlement contentNamespace has an empty gpgKeyUrl: contentNamespace: "+contentNamespace);
 						Assert.assertNull(yumRepo.gpgkey,
 								"gpgkey is not set for Yum repo '"+yumRepo.id+"' when corresponding entitlement contentNamespace has an empty gpgKeyUrl: contentNamespace: "+contentNamespace);
 
 					// case 3: contentNamespace.gpgKeyUrl.startsWith("http")
 					} else if (contentNamespace.gpgKeyUrl.startsWith("http:") || contentNamespace.gpgKeyUrl.startsWith("https:")) {
 						Assert.assertTrue(yumRepo.gpgcheck,
 								"gpgcheck is True for Yum repo '"+yumRepo.id+"' when corresponding entitlement contentNamespace has a non-null/empty gpgKeyUrl: contentNamespace: "+contentNamespace);
 						Assert.assertEquals(yumRepo.gpgkey, contentNamespace.gpgKeyUrl,
 								"gpgkey is set for Yum repo '"+yumRepo.id+"' when corresponding entitlement contentNamespace has a non-null/empty gpgKeyUrl: contentNamespace: "+contentNamespace);
 
 					// case 4: contentNamespace.gpgKeyUrl.startsWith("file:")
 					} else if (contentNamespace.gpgKeyUrl.startsWith("file:")) {
 						Assert.assertTrue(yumRepo.gpgcheck,
 								"gpgcheck is True for Yum repo '"+yumRepo.id+"' when corresponding entitlement contentNamespace has a non-null/empty gpgKeyUrl: contentNamespace: "+contentNamespace);
 						Assert.assertEquals(yumRepo.gpgkey, contentNamespace.gpgKeyUrl,
 								"gpgkey is set for Yum repo '"+yumRepo.id+"' when corresponding entitlement contentNamespace has a non-null/empty gpgKeyUrl: contentNamespace: "+contentNamespace);
 
 					// case 5: contentNamespace.gpgKeyUrl is a relative path   
 					} else {
 						Assert.assertTrue(yumRepo.gpgcheck,
 								"gpgcheck is True for Yum repo '"+yumRepo.id+"' when corresponding entitlement contentNamespace has a non-null/empty gpgKeyUrl: contentNamespace: "+contentNamespace);
 						Assert.assertEquals(yumRepo.gpgkey, clienttasks.baseurl+contentNamespace.gpgKeyUrl,
 								"gpgkey is set for Yum repo '"+yumRepo.id+"' when corresponding entitlement contentNamespace has a non-null/empty gpgKeyUrl: contentNamespace: "+contentNamespace);
 					}
 				}
 			}
 		}
 		if (yumRepos.isEmpty()) throw new SkipException("Since no Red Hat repos were found in '"+clienttasks.redhatRepoFile+"', there are no gpgcheck values to verify.");
 	}
 	
 	
 	@Test(	description="subscription-manager Yum plugin: ensure content can be downloaded/installed/removed",
 			groups={"AcceptanceTests","blockedByBug-701425"},
 			dataProvider="getPackageFromEnabledRepoAndSubscriptionPoolData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41695,fromPlan=2479)
 	public void InstallAndRemovePackageFromEnabledRepoAfterSubscribingToPool_Test(String pkg, String repoLabel, SubscriptionPool pool) throws JSONException, Exception {
 		if (pkg==null) throw new SkipException("Could NOT find a unique available package from repo '"+repoLabel+"' after subscribing to SubscriptionPool: "+pool);
 		
 		// subscribe to this pool
 		File entitlementCertFile = clienttasks.subscribeToSubscriptionPool_(pool);
 		Assert.assertNotNull(entitlementCertFile, "Found the entitlement cert file that was granted after subscribing to pool: "+pool);
 
 		// install the package and assert that it is successfully installed
 		clienttasks.yumInstallPackageFromRepo(pkg, repoLabel, null); //pkgInstalled = true;
 		
 		// now remove the package
 		clienttasks.yumRemovePackage(pkg);
 	}
 	
 	
 	@Test(	description="subscription-manager Yum plugin: ensure content can be downloaded/installed/removed after subscribing to a personal subpool",
 			groups={"InstallAndRemovePackageAfterSubscribingToPersonalSubPool_Test"},
 			dataProvider="getPackageFromEnabledRepoAndSubscriptionSubPoolData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId for
 	public void InstallAndRemovePackageAfterSubscribingToPersonalSubPool_Test(String pkg, String repoLabel, SubscriptionPool pool) throws JSONException, Exception {
 		InstallAndRemovePackageFromEnabledRepoAfterSubscribingToPool_Test(pkg, repoLabel, pool);
 	}
 	
 	
 	@Test(	description="subscription-manager Yum plugin: ensure yum groups can be downloaded/installed/removed",
 			groups={},
 			dataProvider="getYumGroupFromEnabledRepoAndSubscriptionPoolData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId for
 	public void InstallAndRemoveYumGroupFromEnabledRepoAfterSubscribingToPool_Test(String availableGroup, String installedGroup, String repoLabel, SubscriptionPool pool) throws JSONException, Exception {
 		if (availableGroup==null && installedGroup==null) throw new SkipException("No yum groups corresponding to enabled repo '"+repoLabel+" were found after subscribing to pool: "+pool);
 				
 		// unsubscribe from this pool
 		if (pool.equals(lastSubscribedSubscriptionPool)) clienttasks.unsubscribeFromSerialNumber(clienttasks.getSerialNumberFromEntitlementCertFile(lastSubscribedEntitlementCertFile));
 		
 		// before subscribing to the pool, assert that the yum groupinfo does not exist
 		for (String group : new String[]{availableGroup,installedGroup}) {
 			if (group!=null) RemoteFileTasks.runCommandAndAssert(client, "yum groupinfo \""+group+"\" --disableplugin=rhnplugin", Integer.valueOf(0), null, "Warning: Group "+group+" does not exist.");
 		}
 
 		// subscribe to this pool (and remember it)
 		File entitlementCertFile = clienttasks.subscribeToSubscriptionPool_(pool);
 		Assert.assertNotNull(entitlementCertFile, "Found the entitlement cert file that was granted after subscribing to pool: "+pool);
 		lastSubscribedEntitlementCertFile = entitlementCertFile;
 		lastSubscribedSubscriptionPool = pool;
 		
 		// install and remove availableGroup
 		if (availableGroup!=null) {
 			clienttasks.yumInstallGroup(availableGroup);
 			clienttasks.yumRemoveGroup(availableGroup);
 		}
 		
 		// remove and install installedGroup
 		if (installedGroup!=null) {
 			clienttasks.yumRemoveGroup(installedGroup);
 			clienttasks.yumInstallGroup(installedGroup);
 		}
 
 		// TODO: add asserts for the products that get installed or deleted in stdout as a result of yum group install/remove: 
 		// deleting: /etc/pki/product/7.pem
 		// installing: 7.pem
 		// assert the list --installed "status" for the productNamespace name that corresponds to the ContentNamespace from where this repolabel came from.
 	}
 	protected SubscriptionPool lastSubscribedSubscriptionPool = null;
 	protected File lastSubscribedEntitlementCertFile = null;
 	
 	
 	
 	@Test(	description="verify redhat.repo file does not contain an excessive (more than two) number of successive blank lines",
 			groups={"blockedByBug-737145"},
 			enabled=false) // Disabling... this test takes too long to execute.  VerifyRedHatRepoFileIsPurgedOfBlankLinesByYumPlugin_Test effectively provides the same test coverage.
 	@Deprecated
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyRedHatRepoFileDoesNotContainExcessiveBlankLines_Test_DEPRECATED() {
 		
 		// successive blank lines in redhat.repo must not exceed N
 		int N=2; String regex = "(\\n\\s*){"+(N+2)+",}"; 	//  (\n\s*){4,}
 		String redhatRepoFileContents = "";
 	    
 	    // check for excessive blank lines after a new register
 	    clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, true, null, null, null, null);
 	    client.runCommandAndWait("yum -q repolist --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		redhatRepoFileContents = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertContainsNoMatch(redhatRepoFileContents,regex,null,"At most '"+N+"' successive blank are acceptable inside "+clienttasks.redhatRepoFile);
 
 		// check for excessive blank lines after subscribing to each pool
 	    for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
     		clienttasks.subscribe_(null,null,pool.poolId,null,null,null,null,null,null,null, null);
     		client.runCommandAndWait("yum -q repolist --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError		
 		}
 		redhatRepoFileContents = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertContainsNoMatch(redhatRepoFileContents,regex,null,"At most '"+N+"' successive blank are acceptable inside "+clienttasks.redhatRepoFile);
 
 		// check for excessive blank lines after unsubscribing from each serial
 		List<BigInteger> serialNumbers = new ArrayList<BigInteger>();
 	    for (ProductSubscription productSubscription : clienttasks.getCurrentlyConsumedProductSubscriptions()) {
 	    	if (serialNumbers.contains(productSubscription.serialNumber)) continue;	// save some time by avoiding redundant unsubscribes
     		clienttasks.unsubscribe_(null, productSubscription.serialNumber, null, null, null);
     		serialNumbers.add(productSubscription.serialNumber);
     		client.runCommandAndWait("yum -q repolist --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError		
 		}
 		redhatRepoFileContents = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertContainsNoMatch(redhatRepoFileContents,regex,null,"At most '"+N+"' successive blank are acceptable inside "+clienttasks.redhatRepoFile);
 		
 		// assert the comment heading is present
 		//Assert.assertContainsMatch(redhatRepoFileContents,"^# Red Hat Repositories$",null,"Comment heading \"Red Hat Repositories\" was found inside "+clienttasks.redhatRepoFile);
 		Assert.assertContainsMatch(redhatRepoFileContents,"^# Certificate-Based Repositories$",null,"Comment heading \"Certificate-Based Repositories\" was found inside "+clienttasks.redhatRepoFile);
 		Assert.assertContainsMatch(redhatRepoFileContents,"^# Managed by \\(rhsm\\) subscription-manager$",null,"Comment heading \"Managed by (rhsm) subscription-manager\" was found inside "+clienttasks.redhatRepoFile);		
 	}
 	
 	@Test(	description="verify redhat.repo file is purged of successive blank lines by subscription-manager yum plugin",
 			groups={"AcceptanceTests","blockedByBug-737145","blockedByBug-838113"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId for
 	public void VerifyRedHatRepoFileIsPurgedOfBlankLinesByYumPlugin_Test() {
 		
 		// successive blank lines in redhat.repo must not exceed N
 		int N=2; String regex = "(\\n\\s*){"+(N+2)+",}"; 	//  (\n\s*){4,}
 		String redhatRepoFileContents = null;
 	    
 		// adding the following call to login and yum repolist to compensate for change of behavior introduced by Bug 781510 - 'subscription-manager clean' should delete redhat.repo
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null,(List<String>)null, null, null, null, null, null, null, null);
 		clienttasks.subscribeToTheCurrentlyAllAvailableSubscriptionPoolsCollectively();
 		client.runCommandAndWait("yum -q repolist --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError			
 		redhatRepoFileContents = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertTrue(RemoteFileTasks.testExists(client, clienttasks.redhatRepoFile),"Expecting the redhat repo file '"+clienttasks.redhatRepoFile+"' to exist after unregistering.");
 		Assert.assertContainsNoMatch(redhatRepoFileContents,regex,null,"At most '"+N+"' successive blank are acceptable inside "+clienttasks.redhatRepoFile);
 	
 	    // check for excessive blank lines after unregister
 	    clienttasks.unregister(null,null,null);
 	    client.runCommandAndWait("yum -q repolist --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 	    //TODO 8/9/2012 FIGURE OUT IF THIS EXPECTED OUTPUT WAS SUPPOSE TO CHANGE: Assert.assertTrue(client.getStderr().contains("Unable to read consumer identity"),"Yum repolist should not touch redhat.repo when there is no consumer and state in stderr 'Unable to read consumer identity'.");
 	    Assert.assertEquals(client.getStderr().trim(),"","Stderr from prior command");
 	    Assert.assertTrue(RemoteFileTasks.testExists(client, clienttasks.redhatRepoFile),"Expecting the redhat repo file '"+clienttasks.redhatRepoFile+"' to exist after unregistering.");
 		redhatRepoFileContents = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertContainsNoMatch(redhatRepoFileContents,regex,null,"At most '"+N+"' successive blank are acceptable inside '"+clienttasks.redhatRepoFile+"' after unregistering.");
 
 		log.info("Inserting blank lines into the redhat.repo for testing purposes...");
 		client.runCommandAndWait("for i in `seq 1 10`; do echo \"\" >> "+clienttasks.redhatRepoFile+"; done; echo \"# test for bug 737145\" >> "+clienttasks.redhatRepoFile);
 		redhatRepoFileContents = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertContainsMatch(redhatRepoFileContents,regex,null,"File "+clienttasks.redhatRepoFile+" has been infiltrated with excessive blank lines.");
 	    client.runCommandAndWait("yum -q repolist --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 	    //TODO 8/9/2012 FIGURE OUT IF THIS EXPECTED OUTPUT WAS SUPPOSE TO CHANGE: Assert.assertTrue(client.getStderr().contains("Unable to read consumer identity"),"Yum repolist should not touch redhat.repo when there is no consumer and state in stderr 'Unable to read consumer identity'.");
 	    Assert.assertEquals(client.getStderr().trim(),"","Stderr from prior command");
 		String redhatRepoFileContents2 = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertContainsMatch(redhatRepoFileContents2,regex,null,"File "+clienttasks.redhatRepoFile+" is still infiltrated with excessive blank lines.");
 		Assert.assertEquals(redhatRepoFileContents2, redhatRepoFileContents,"File "+clienttasks.redhatRepoFile+" remains unchanged when there is no consumer.");
 
 		// trigger the yum plugin for subscription-manager (after registering again)
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null,(List<String>)null, null, null, null, null, null, null, null);
 		log.info("Triggering the yum plugin for subscription-manager which will purge the blank lines from redhat.repo...");
 	    client.runCommandAndWait("yum -q repolist --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		redhatRepoFileContents = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertContainsNoMatch(redhatRepoFileContents,regex,null,"At most '"+N+"' successive blank are acceptable inside '"+clienttasks.redhatRepoFile+"' after reregistering.");
 		
 		// assert the comment heading is present
 		//Assert.assertContainsMatch(redhatRepoFileContents,"^# Red Hat Repositories$",null,"Comment heading \"Red Hat Repositories\" was found inside "+clienttasks.redhatRepoFile);
 		Assert.assertContainsMatch(redhatRepoFileContents,"^# Certificate-Based Repositories$",null,"Comment heading \"Certificate-Based Repositories\" was found inside "+clienttasks.redhatRepoFile);
 		Assert.assertContainsMatch(redhatRepoFileContents,"^# Managed by \\(rhsm\\) subscription-manager$",null,"Comment heading \"Managed by (rhsm) subscription-manager\" was found inside "+clienttasks.redhatRepoFile);		
 	}
 	@Test(	description="verify redhat.repo file is purged of successive blank lines by subscription-manager yum plugin",
 			groups={"AcceptanceTests","blockedByBug-737145"},
 			enabled=false)	// was valid before bug fix 781510
 	@Deprecated
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId for
 	public void VerifyRedHatRepoFileIsPurgedOfBlankLinesByYumPlugin_Test_DEPRECATED() {
 		
 		// successive blank lines in redhat.repo must not exceed N
 		int N=2; String regex = "(\\n\\s*){"+(N+2)+",}"; 	//  (\n\s*){4,}
 		String redhatRepoFileContents = "";
 	    
 	    // check for excessive blank lines after unregister
 	    clienttasks.unregister(null,null,null);
 	    client.runCommandAndWait("yum -q repolist --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		redhatRepoFileContents = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertContainsNoMatch(redhatRepoFileContents,regex,null,"At most '"+N+"' successive blank are acceptable inside "+clienttasks.redhatRepoFile);
 
 		log.info("Inserting blank lines into the redhat.repo for testing purposes...");
 		client.runCommandAndWait("for i in `seq 1 10`; do echo \"\" >> "+clienttasks.redhatRepoFile+"; done; echo \"# test for bug 737145\" >> "+clienttasks.redhatRepoFile);
 		redhatRepoFileContents = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertContainsMatch(redhatRepoFileContents,regex,null,"File "+clienttasks.redhatRepoFile+" has been infiltrated with excessive blank lines.");
 
 		// trigger the yum plugin for subscription-manager
 		log.info("Triggering the yum plugin for subscription-manager which will purge the blank lines from redhat.repo...");
 	    client.runCommandAndWait("yum -q repolist --disableplugin=rhnplugin"); // --disableplugin=rhnplugin helps avoid: up2date_client.up2dateErrors.AbuseError
 		redhatRepoFileContents = client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout();
 		Assert.assertContainsNoMatch(redhatRepoFileContents,regex,null,"At most '"+N+"' successive blank are acceptable inside "+clienttasks.redhatRepoFile);
 		
 		// assert the comment heading is present
 		//Assert.assertContainsMatch(redhatRepoFileContents,"^# Red Hat Repositories$",null,"Comment heading \"Red Hat Repositories\" was found inside "+clienttasks.redhatRepoFile);
 		Assert.assertContainsMatch(redhatRepoFileContents,"^# Certificate-Based Repositories$",null,"Comment heading \"Certificate-Based Repositories\" was found inside "+clienttasks.redhatRepoFile);
 		Assert.assertContainsMatch(redhatRepoFileContents,"^# Managed by \\(rhsm\\) subscription-manager$",null,"Comment heading \"Managed by (rhsm) subscription-manager\" was found inside "+clienttasks.redhatRepoFile);		
 	}	
 	
 	
 	
 	
 	// Candidates for an automated Test:
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=654442
 	// TODO Bug 689031 - nss needs to be able to use pem files interchangeably in a single process 
 	// TODO Bug 701425 - NSS issues with more than one susbcription 
 	// TODO Bug 706265 - product cert is not getting removed after removing all the installed packages from its repo using yum
 	// TODO Bug 687970 - Currently no way to delete a content source from a product
 	// how to create content (see Bug 687970): [jsefler@jsefler ~]$ curl -u admin:admin -k --request POST https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/content --header "Content-type: application/json" -d '{"contentUrl":"/foo/path","label":"foolabel","type":"yum","gpgUrl":"/foo/path/gpg","id":"fooid","name":"fooname","vendor":"Foo Vendor"}' | python -m json.tool
 	// how to delete content (see Bug 687970): [jsefler@jsefler ~]$ curl -u admin:admin -k --request DELETE https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/content/fooid
 	// how to get content    (see Bug 687970): [jsefler@jsefler ~]$ curl -u admin:admin -k --request GET https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/content/fooid
 	// how to associate content with product   (see Bug 687970): [jsefler@jsefler ~]$ curl -u admin:admin -k --request POST https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/product/productid/content/fooid&enabled=false
 	// TODO Bug 705068 - product-id plugin displays "duration"
 	// TODO Bug 740773 - product cert lost after installing a pkg from cdn-internal.rcm-test.redhat.com
 	// TODO Bug 806457 - If yum runs with no enabled or active repo's, we delete the product cert 
 	
 	// All of these are the same...
 	// TODO Bug 818383 - Yum Plugin usability Improvements 
 	// TODO Bug 832119 - Yum Plugin usability Improvements 
 	// TODO Bug 830193 - Improve yum plugin output across RHN and Subscription Management
 	
 	// Configuration Methods ***********************************************************************
 	
 	@BeforeClass(groups={"setup"})
 	public void removeYumBeakerRepos() {
 		client.runCommandAndWait("mkdir -p /tmp/beaker.repos; mv -f /etc/yum.repos.d/beaker*.repo /tmp/beaker.repos");
 	}
 	
 	@BeforeClass(groups={"setup"})
 	public void setManageRepos() {
 		clienttasks.config(null, null, true, new String[]{"rhsm","manage_repos","1"});
 	}
 	
 	@AfterClass(groups={"setup"})
 	public void restoreYumBeakerRepos() {
 		client.runCommandAndWait("mv -f /tmp/beaker.repos/beaker*.repo /etc/yum.repos.d");
 	}
 	
 	@AfterClass(groups={"setup"})
 	@AfterGroups(groups={"setup"},value="InstallAndRemovePackageAfterSubscribingToPersonalSubPool_Test", alwaysRun=true)
 	public void unregisterAfterGroupsInstallAndRemovePackageAfterSubscribingToPersonalSubPool_Test() {
 		// first, unregister client1 since it is a personal subpool consumer
 		client1tasks.unregister_(null,null,null);
 		// second, unregister client2 since it is a personal consumer
 		if (client2tasks!=null) {
 			client2tasks.register_(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, personalConsumerId, null, null, null, (String)null, null, null, Boolean.TRUE, null, null, null, null);
 			client2tasks.unsubscribe_(Boolean.TRUE,null, null, null, null);
 			client2tasks.unregister_(null,null,null);
 		}
 	}
 
 
 	
 	// Protected Methods ***********************************************************************
 	protected String personalConsumerId = null;
 
 	
 	// Data Providers ***********************************************************************
 
 	
 	@DataProvider(name="getPackageFromEnabledRepoAndSubscriptionPoolData")
 	public Object[][] getPackageFromEnabledRepoAndSubscriptionPoolDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getPackageFromEnabledRepoAndSubscriptionPoolDataAsListOfLists());
 	}
 	protected List<List<Object>> getPackageFromEnabledRepoAndSubscriptionPoolDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		if (sm_clientUsername==null) return ll;
 		if (sm_clientPassword==null) return ll;
 		
 		// get the currently installed product certs to be used when checking for conditional content tagging
 		List<ProductCert> currentProductCerts = clienttasks.getCurrentProductCerts();
 		
 		// assure we are freshly registered and process all available subscription pools
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, ConsumerType.system, null, null, null, null, null, (String)null, null, null, Boolean.TRUE, false, null, null, null);
 		for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			
 			File entitlementCertFile = clienttasks.subscribeToSubscriptionPool_(pool);
 			Assert.assertNotNull(entitlementCertFile, "Found the entitlement cert file that was granted after subscribing to pool: "+pool);
 			EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 				if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 				if (contentNamespace.enabled.equals("1") && clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 					String repoLabel = contentNamespace.label;
 					
 					// find an available package that is uniquely provided by repo
 					String pkg = clienttasks.findUniqueAvailablePackageFromRepo(repoLabel);
 					if (pkg==null) {
 						log.warning("Could NOT find a unique available package from repo '"+repoLabel+"' after subscribing to SubscriptionPool: "+pool);
 					}
 
 					// String availableGroup, String installedGroup, String repoLabel, SubscriptionPool pool
 					ll.add(Arrays.asList(new Object[]{pkg, repoLabel, pool}));
 				}
 			}
 			clienttasks.unsubscribeFromSerialNumber(clienttasks.getSerialNumberFromEntitlementCertFile(entitlementCertFile));
 
 			// minimize the number of dataProvided rows (useful during automated testcase development)
 			if (Boolean.valueOf(getProperty("sm.debug.dataProviders.minimize","false"))) break;
 		}
 		
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getYumGroupFromEnabledRepoAndSubscriptionPoolData")
 	public Object[][] getYumGroupFromEnabledRepoAndSubscriptionPoolDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getYumGroupFromEnabledRepoAndSubscriptionPoolDataAsListOfLists());
 	}
 	protected List<List<Object>> getYumGroupFromEnabledRepoAndSubscriptionPoolDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		if (sm_clientUsername==null) return ll;
 		if (sm_clientPassword==null) return ll;
 		
 		// get the currently installed product certs to be used when checking for conditional content tagging
 		List<ProductCert> currentProductCerts = clienttasks.getCurrentProductCerts();
 		
 		// assure we are freshly registered and process all available subscription pools
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, ConsumerType.system, null, null, null, null, null, (String)null, null, null, Boolean.TRUE, false, null, null, null);
 		for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			
 			File entitlementCertFile = clienttasks.subscribeToSubscriptionPool_(pool);
 			Assert.assertNotNull(entitlementCertFile, "Found the entitlement cert file that was granted after subscribing to pool: "+pool);
 			EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 				if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 				if (contentNamespace.enabled.equals("1") && clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 					String repoLabel = contentNamespace.label;
 
 					// find first available group provided by this repo
 					String availableGroup = clienttasks.findAnAvailableGroupFromRepo(repoLabel);
 					// find first installed group provided by this repo
 					String installedGroup = clienttasks.findAnInstalledGroupFromRepo(repoLabel);
 
 					// String availableGroup, String installedGroup, String repoLabel, SubscriptionPool pool
 					ll.add(Arrays.asList(new Object[]{availableGroup, installedGroup, repoLabel, pool}));
 				}
 			}
 			clienttasks.unsubscribeFromSerialNumber(clienttasks.getSerialNumberFromEntitlementCertFile(entitlementCertFile));
 
 			// minimize the number of dataProvided rows (useful during automated testcase development)
 			if (Boolean.valueOf(getProperty("sm.debug.dataProviders.minimize","false"))) break;
 		}
 		
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getPackageFromEnabledRepoAndSubscriptionSubPoolData")
 	public Object[][] getPackageFromEnabledRepoAndSubscriptionSubPoolDataAs2dArray() throws Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getPackageFromEnabledRepoAndSubscriptionSubPoolDataAsListOfLists());
 	}
 	protected List<List<Object>> getPackageFromEnabledRepoAndSubscriptionSubPoolDataAsListOfLists() throws Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (client1tasks==null) return ll;
 		if (client2tasks==null) return ll;
 		
 		// assure we are registered (as a person on client2 and a system on client1)
 		
 		// register client1 as a system under rhpersonalUsername
 		client1tasks.register(sm_rhpersonalUsername, sm_rhpersonalPassword, sm_rhpersonalOrg, null, ConsumerType.system, null, null, null, null, null, (String)null, null, null, Boolean.TRUE, false, null, null, null);
 		
 		// register client2 as a person under rhpersonalUsername
 		client2tasks.register(sm_rhpersonalUsername, sm_rhpersonalPassword, sm_rhpersonalOrg, null, ConsumerType.person, null, null, null, null, null, (String)null, null, null, Boolean.TRUE, false, null, null, null);
 		
 		// subscribe to the personal subscription pool to unlock the subpool
 		personalConsumerId = client2tasks.getCurrentConsumerId();
 		for (int j=0; j<sm_personSubscriptionPoolProductData.length(); j++) {
 			JSONObject poolProductDataAsJSONObject = (JSONObject) sm_personSubscriptionPoolProductData.get(j);
 			String personProductId = poolProductDataAsJSONObject.getString("personProductId");
 			JSONObject subpoolProductDataAsJSONObject = poolProductDataAsJSONObject.getJSONObject("subPoolProductData");
 			String systemProductId = subpoolProductDataAsJSONObject.getString("systemProductId");
 
 			SubscriptionPool personPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",personProductId,client2tasks.getCurrentlyAvailableSubscriptionPools());
 			Assert.assertNotNull(personPool,"Personal productId '"+personProductId+"' is available to user '"+sm_rhpersonalUsername+"' registered as a person.");
 			File entitlementCertFile = client2tasks.subscribeToSubscriptionPool_(personPool);
 			Assert.assertNotNull(entitlementCertFile, "Found the entitlement cert file that was granted after subscribing to personal pool: "+personPool);
 
 			
 			// now the subpool is available to the system
 			SubscriptionPool systemPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId",systemProductId,client1tasks.getCurrentlyAvailableSubscriptionPools());
 			Assert.assertNotNull(systemPool,"Personal subPool productId'"+systemProductId+"' is available to user '"+sm_rhpersonalUsername+"' registered as a system.");
 			//client1tasks.subscribeToSubscriptionPool(systemPool);
 			
 			entitlementCertFile = client1tasks.subscribeToSubscriptionPool_(systemPool);
 			Assert.assertNotNull(entitlementCertFile, "Found the entitlement cert file that was granted after subscribing to system pool: "+systemPool);
 			EntitlementCert entitlementCert = client1tasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 				if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 				if (contentNamespace.enabled.equals("1")) {
 					String repoLabel = contentNamespace.label;
 					
 					// find an available package that is uniquely provided by repo
 					String pkg = client1tasks.findUniqueAvailablePackageFromRepo(repoLabel);
 					if (pkg==null) {
 						log.warning("Could NOT find a unique available package from repo '"+repoLabel+"' after subscribing to SubscriptionSubPool: "+systemPool);
 					}
 
 					// String availableGroup, String installedGroup, String repoLabel, SubscriptionPool pool
 					ll.add(Arrays.asList(new Object[]{pkg, repoLabel, systemPool}));
 				}
 			}
 			client1tasks.unsubscribeFromSerialNumber(client1tasks.getSerialNumberFromEntitlementCertFile(entitlementCertFile));
 		}
 		return ll;
 	}
 
 }
