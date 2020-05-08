 package rhsm.cli.tests;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
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
 import rhsm.base.ConsumerType;
 import rhsm.base.SubscriptionManagerCLITestScript;
 import rhsm.cli.tasks.CandlepinTasks;
 import rhsm.data.ContentNamespace;
 import rhsm.data.EntitlementCert;
 import rhsm.data.ProductCert;
 import rhsm.data.ProductSubscription;
 import rhsm.data.SubscriptionPool;
 import rhsm.data.YumRepo;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  */
 @Test(groups={"ContentTests"})
 public class ContentTests extends SubscriptionManagerCLITestScript{
 
 	
 	// Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager Yum plugin: enable/disable",
 			groups={"EnableDisableYumRepoAndVerifyContentAvailable_Test","blockedByBug-804227","blockedByBug-871146"},
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
 			if (contentNamespace.enabled) {
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
 			if (contentNamespace.enabled) {
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
 		// repolist = clienttasks.getYumRepolist("all");	// used prior to RHEL5.9
 		/* 9/10/2012 RHEL5.9: YUM STARTED THROWING THIS M2Crypto.SSL.SSLError  THIS ERROR INSPIRED Bug 855957 - subscription-manager unsubscribe should cleanup the redhat.repo
 		ssh root@jsefler-59server.usersys.redhat.com yum repolist all --disableplugin=rhnplugin
 		Stdout:
 		Loaded plugins: product-id, security
 		No plugin match for: rhnplugin
 		Stderr:
 		Traceback (most recent call last):
 		File "/usr/bin/yum", line 29, in ?
 		yummain.user_main(sys.argv[1:], exit_code=True)
 		File "/usr/share/yum-cli/yummain.py", line 309, in user_main
 		errcode = main(args)
 		File "/usr/share/yum-cli/yummain.py", line 178, in main
 		result, resultmsgs = base.doCommands()
 		File "/usr/share/yum-cli/cli.py", line 349, in doCommands
 		return self.yum_cli_commands[self.basecmd].doCommand(self, self.basecmd, self.extcmds)
 		File "/usr/share/yum-cli/yumcommands.py", line 788, in doCommand
 		base.repos.populateSack()
 		File "/usr/lib/python2.4/site-packages/yum/repos.py", line 260, in populateSack
 		sack.populate(repo, mdtype, callback, cacheonly)
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 168, in populate
 		if self._check_db_version(repo, mydbtype):
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 226, in _check_db_version
 		return repo._check_db_version(mdtype)
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 1226, in _check_db_version
 		repoXML = self.repoXML
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 1399, in <lambda>
 		repoXML = property(fget=lambda self: self._getRepoXML(),
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 1391, in _getRepoXML
 		self._loadRepoXML(text=self)
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 1381, in _loadRepoXML
 		return self._groupLoadRepoXML(text, ["primary"])
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 1365, in _groupLoadRepoXML
 		if self._commonLoadRepoXML(text):
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 1201, in _commonLoadRepoXML
 		result = self._getFileRepoXML(local, text)
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 974, in _getFileRepoXML
 		cache=self.http_caching == 'all')
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 805, in _getFile
 		result = self.grab.urlgrab(misc.to_utf8(relative), local,
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 511, in <lambda>
 		grab = property(lambda self: self._getgrab())
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 506, in _getgrab
 		self._setupGrab()
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 474, in _setupGrab
 		ugopts = self._default_grabopts()
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 486, in _default_grabopts
 		opts = { 'keepalive': self.keepalive,
 		File "/usr/lib/python2.4/site-packages/yum/yumRepo.py", line 656, in _getSslContext
 		sslCtx.load_cert(self.sslclientcert, self.sslclientkey)
 		File "/usr/lib64/python2.4/site-packages/M2Crypto/SSL/Context.py", line 74, in load_cert
 		m2.ssl_ctx_use_cert(self.ctx, certfile)
 		M2Crypto.SSL.SSLError: No such file or directory
 		ExitCode: 1
 		*/
 		// while bug 855957 is open, replacing above call to clienttasks.getCurrentlySubscribedYumRepos() with the following call to clienttasks.getCurrentlySubscribedYumRepos()
 		repolist.clear(); for (YumRepo yumRepo : YumRepo.parse(client.runCommandAndWait("cat "+clienttasks.redhatRepoFile).getStdout())) {repolist.add(yumRepo.id);}
 		// NOTE: 9/10/2012 - The following block of behavior may change after bug 855957 is addressed
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
 			groups={"AcceptanceTests","blockedByBug-804227","blockedByBug-871146"},
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
 				if (contentNamespace.enabled) {
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
 				if (contentNamespace.enabled) {
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
 			groups={"AcceptanceTests","blockedByBug-701425","blockedByBug-871146"},
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
 	
 	
 	
 	
 	
 
 	@Test(	description="Verify that a 185 content set product subscription is always subscribable",
 			groups={"SubscribabilityOfContentSetProduct_Tests","blockedByBug-871146"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifySubscribabilityOf185ContentSetProduct_Test() {
 
 		Map<String,String> factsMap = new HashMap<String,String>();
 		File entitlementCertFile;
 		EntitlementCert entitlementCert;
 		String systemCertificateVersionFactValue;
 		
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, true, false, null, null, null);
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", skuTo185ContentSetProduct, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool,"Found an available pool to subscribe to productId '"+skuTo185ContentSetProduct+"': "+pool);
 		
 		// test that it IS subscribable when system.certificate_version: None
 		factsMap.put("system.certificate_version", null);
 		clienttasks.createFactsFileWithOverridingValues(factsMap);
 		clienttasks.facts(null, true, null, null, null);
 		Assert.assertEquals(clienttasks.getFactValue("system.certificate_version"), "None", "When the system.certificate_version fact is null, its fact value is reported as 'None'.");
 		//entitlementCertFile = clienttasks.subscribeToProductId(skuTo185ContentSetProduct);
 		//entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		clienttasks.subscribe(null, null, pool.poolId, null, null, null, null, null, null, null, null);
 		entitlementCert = clienttasks.getEntitlementCertCorrespondingToSubscribedPool(pool);
 		entitlementCertFile = clienttasks.getEntitlementCertFileFromEntitlementCert(entitlementCert);
 		Assert.assertEquals(entitlementCert.version,"1.0","When the system.certificate_version fact is null, the version of the entitlement certificate granted by candlepin is '1.0'.");
 		Assert.assertEquals(entitlementCert.contentNamespaces.size(), 185, "The number of content sets provided in the version 1.0 entitlement cert parsed using the rct cat-cert tool.");
 		entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFileUsingOpensslX509(entitlementCertFile);
 		Assert.assertEquals(entitlementCert.contentNamespaces.size(), 185, "The number of content sets provided in this version '"+entitlementCert.version+"' entitlement cert parsed using the openssl x509 tool.");
 		clienttasks.assertEntitlementCertsInYumRepolist(Arrays.asList(entitlementCert), true);
 		//clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.unsubscribeFromSerialNumber(clienttasks.getSerialNumberFromEntitlementCertFile(entitlementCertFile));
 		
 		// test that it IS subscribable when system.certificate_version: 1.0
 		factsMap.put("system.certificate_version", "1.0");
 		clienttasks.createFactsFileWithOverridingValues(factsMap);
 		clienttasks.facts(null, true, null, null, null);
 		Assert.assertEquals(clienttasks.getFactValue("system.certificate_version"), "1.0", "When the system.certificate_version fact is 1.0, its fact value is reported as '1.0'.");
 		//entitlementCertFile = clienttasks.subscribeToProductId(skuTo185ContentSetProduct);
 		//entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		clienttasks.subscribe(null, null, pool.poolId, null, null, null, null, null, null, null, null);
 		entitlementCert = clienttasks.getEntitlementCertCorrespondingToSubscribedPool(pool);
 		entitlementCertFile = clienttasks.getEntitlementCertFileFromEntitlementCert(entitlementCert);
 		Assert.assertEquals(entitlementCert.version,"1.0","When the system.certificate_version fact is 1.0, the version of the entitlement certificate granted by candlepin is '1.0'.");
 		Assert.assertEquals(entitlementCert.contentNamespaces.size(), 185, "The number of content sets provided in the version 1.0 entitlement cert parsed using the rct cat-cert tool.");
 		entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFileUsingOpensslX509(entitlementCertFile);
 		Assert.assertEquals(entitlementCert.contentNamespaces.size(), 185, "The number of content sets provided in this version '"+entitlementCert.version+"' entitlement cert parsed using the openssl x509 tool.");
 		clienttasks.assertEntitlementCertsInYumRepolist(Arrays.asList(entitlementCert), true);
 		//clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.unsubscribeFromSerialNumber(clienttasks.getSerialNumberFromEntitlementCertFile(entitlementCertFile));
 
 		// test that it IS subscribable when system.certificate_version is the system's default value (should be >=3.0)
 		clienttasks.deleteFactsFileWithOverridingValues();
 		systemCertificateVersionFactValue = clienttasks.getFactValue("system.certificate_version");
 		Assert.assertTrue(Float.valueOf(systemCertificateVersionFactValue)>=3.0, "The actual default system.certificate_version fact '"+systemCertificateVersionFactValue+"' is >= 3.0.");
 		//entitlementCertFile = clienttasks.subscribeToProductId(skuTo185ContentSetProduct);
 		//entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		clienttasks.subscribe(null, null, pool.poolId, null, null, null, null, null, null, null, null);
 		entitlementCert = clienttasks.getEntitlementCertCorrespondingToSubscribedPool(pool);
 		entitlementCertFile = clienttasks.getEntitlementCertFileFromEntitlementCert(entitlementCert);
 		Assert.assertTrue(Float.valueOf(entitlementCert.version)<=Float.valueOf(systemCertificateVersionFactValue),"The version of the entitlement certificate '"+entitlementCert.version+"' granted by candlepin is less than or equal to the system.certificate_version '"+systemCertificateVersionFactValue+"' which indicates the maximum certificate version this system knows how to handle.");
 		Assert.assertEquals(entitlementCert.contentNamespaces.size(), 185, "The number of content sets provided in this version '"+entitlementCert.version+"' entitlement cert parsed using the rct cat-cert tool.");
 		clienttasks.assertEntitlementCertsInYumRepolist(Arrays.asList(entitlementCert), true);
 		//clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.unsubscribeFromSerialNumber(clienttasks.getSerialNumberFromEntitlementCertFile(entitlementCertFile));
 	}
 	
 	
 	@Test(	description="Verify that a 186 content set product subscription is subscribable only when system.certificate_version >= 3.0",
 			groups={"SubscribabilityOfContentSetProduct_Tests","blockedByBug-871146"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifySubscribabilityOf186ContentSetProduct_Test() {
 		
 		Map<String,String> factsMap = new HashMap<String,String>();
 		File entitlementCertFile;
 		EntitlementCert entitlementCert;
 		String systemCertificateVersionFactValue;
 		SSHCommandResult sshCommandResult;
 		
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, true, false, null, null, null);
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", skuTo186ContentSetProduct, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool,"Found an available pool to subscribe to productId '"+skuTo186ContentSetProduct+"': "+pool);
 	
 		// test that it is NOT subscribable when system.certificate_version: None
 		factsMap.put("system.certificate_version", null);
 		clienttasks.createFactsFileWithOverridingValues(factsMap);
 		clienttasks.facts(null, true, null, null, null);
 		Assert.assertEquals(clienttasks.getFactValue("system.certificate_version"), "None", "When the system.certificate_version fact is null, its fact value is reported as 'None'.");
 		sshCommandResult = clienttasks.subscribe_(null, null, pool.poolId, null, null, null, null, null, null, null, null);
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(255), "Exitcode from an attempt to subscribe to '"+pool.subscriptionName+"' that provides a product with too many content sets (>185) when system.certificate_version is null");
 		Assert.assertEquals(sshCommandResult.getStderr().trim(), "Too many content sets for certificate. Please upgrade to a newer client to use subscription: "+pool.subscriptionName, "Stderr from an attempt to subscribe to '"+pool.subscriptionName+"' that provides a product with too many content sets (>185) when system.certificate_version is null");
 		Assert.assertEquals(sshCommandResult.getStdout().trim(), "", "Stdout from an attempt to subscribe to '"+pool.subscriptionName+"' that provides a product with too many content sets (>185) when system.certificate_version is null");
 		Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().isEmpty(), "No entitlements should be consumed after attempt to subscribe to '"+pool.subscriptionName+"' that provides a product with too many content sets (>185) when system.certificate_version is null");
 		
 		// test that it is NOT subscribable when system.certificate_version: 1.0
 		factsMap.put("system.certificate_version", "1.0");
 		clienttasks.createFactsFileWithOverridingValues(factsMap);
 		clienttasks.facts(null, true, null, null, null);
 		Assert.assertEquals(clienttasks.getFactValue("system.certificate_version"), "1.0", "When the system.certificate_version fact is 1.0, its fact value is reported as '1.0'.");
 		sshCommandResult = clienttasks.subscribe_(null, null, pool.poolId, null, null, null, null, null, null, null, null);
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(255), "Exitcode from an attempt to subscribe to '"+pool.subscriptionName+"' that provides a product with too many content sets (>185) when system.certificate_version is 1.0");
 		Assert.assertEquals(sshCommandResult.getStderr().trim(), "Too many content sets for certificate. Please upgrade to a newer client to use subscription: "+pool.subscriptionName, "Stderr from an attempt to subscribe to '"+pool.subscriptionName+"' that provides a product with too many content sets (>185) when system.certificate_version is 1.0");
 		Assert.assertEquals(sshCommandResult.getStdout().trim(), "", "Stdout from an attempt to subscribe to '"+pool.subscriptionName+"' that provides a product with too many content sets (>185) when system.certificate_version is 1.0");
 		Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().isEmpty(), "No entitlements should be consumed after attempt to subscribe to '"+pool.subscriptionName+"' that provides a product with too many content sets (>185) when system.certificate_version is 1.0");
 
 		// test that it is subscribable when system.certificate_version is the system's default value (should be >=3.0)
 		clienttasks.deleteFactsFileWithOverridingValues();
 		systemCertificateVersionFactValue = clienttasks.getFactValue("system.certificate_version");
 		Assert.assertTrue(Float.valueOf(systemCertificateVersionFactValue)>=3.0, "The actual default system.certificate_version fact '"+systemCertificateVersionFactValue+"' is >= 3.0.");
 		//entitlementCertFile = clienttasks.subscribeToProductId(skuTo185ContentSetProduct);
 		//entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		clienttasks.subscribe(null, null, pool.poolId, null, null, null, null, null, null, null, null);
 		entitlementCert = clienttasks.getEntitlementCertCorrespondingToSubscribedPool(pool);
 		entitlementCertFile = clienttasks.getEntitlementCertFileFromEntitlementCert(entitlementCert);
 		Assert.assertTrue(Float.valueOf(entitlementCert.version)<=Float.valueOf(systemCertificateVersionFactValue),"The version of the entitlement certificate '"+entitlementCert.version+"' granted by candlepin is less than or equal to the system.certificate_version '"+systemCertificateVersionFactValue+"' which indicates the maximum certificate version this system knows how to handle.");
 		Assert.assertEquals(entitlementCert.contentNamespaces.size(), 186, "The number of content sets provided in this version '"+entitlementCert.version+"' entitlement cert parsed using the rct cat-cert tool.");
 		clienttasks.assertEntitlementCertsInYumRepolist(Arrays.asList(entitlementCert), true);
 		//clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.unsubscribeFromSerialNumber(clienttasks.getSerialNumberFromEntitlementCertFile(entitlementCertFile));
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
 	// TODO Bug 845349 - [RFE] As a Red Hat user, I would like to use the exclude line in the yum.repos.d files and not have them blown away
	
 	
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
 			client2tasks.unsubscribe_(true,(BigInteger)null, null, null, null);
 			client2tasks.unregister_(null,null,null);
 		}
 	}
 
 
 	@AfterGroups(groups={"setup"},value="SubscribabilityOfContentSetProduct_Tests")
 	public void deleteFactsFileWithOverridingValuesAfterGroups() {
 		if (clienttasks==null) return;
 		clienttasks.deleteFactsFileWithOverridingValues();
 	}
 	
 	protected String skuTo185ContentSetProduct = "mktProductId-185";
 	protected String skuTo186ContentSetProduct = "mktProductId-186";
 	@BeforeClass(groups="setup")
 	public void createSubscriptionsWithVariationsOnContentSizes() throws JSONException, Exception {
 		String marketingProductName,engineeringProductName,marketingProductId,engineeringProductId;
 		Map<String,String> attributes = new HashMap<String,String>();
 		if (server==null) {
 			log.warning("Skipping createSubscriptionsWithVariationsOnContentSizes() when server is null.");
 			return;	
 		}
 		
 		// recreate a lot of content sets
 		String contentIdStringFormat = "777%04d";
 		for (int i = 1; i <= 200; i++) {
 			String contentName = "Content Name "+i;
 			String contentId = String.format(contentIdStringFormat,i);	// must be numeric (and unique)
 			String contentLabel = "content-label-"+i;
 			CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/content/"+contentId);
 			CandlepinTasks.createContentUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, contentName, contentId, contentLabel, "yum", "Red Hat QE, Inc.", "/content/path/to/"+contentLabel, "/gpg/path/to/"+contentLabel, "3600", null, null);
 		}
 	
 		// recreate Subscription and products providing a lot of content
 		for (int N : new ArrayList<Integer>(Arrays.asList(185,186))) {	// 185 is the maximum number of content sets tolerated in a system.certificate_version < 3.0
 			marketingProductName = String.format("Subscription for a %s ContentSet Product",N);
 			marketingProductId = "mktProductId-"+N;
 			engineeringProductName = String.format("%s ContentSet Product",N);
 			engineeringProductId = String.valueOf(N);	// must be numeric (and unique)
 			attributes.clear();
 			attributes.put("requires_consumer_type", "system");
 			//attributes.put("sockets", "0");
 			attributes.put("version", "3.0");
 			//attributes.put("variant", "server");
 			attributes.put("arch", "ALL");
 			//attributes.put("warning_period", "30");
 			// delete already existing subscription and products
 			CandlepinTasks.deleteSubscriptionsAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, marketingProductId);
 			CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+marketingProductId);
 			CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+engineeringProductId);
 			// create a new marketing product (MKT), engineering product (SVC), content for the engineering product, and a subscription to the marketing product that provides the engineering product
 			attributes.put("type", "MKT");
 			CandlepinTasks.createProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, marketingProductName, marketingProductId, 1, attributes, null);
 			attributes.put("type", "SVC");
 			CandlepinTasks.createProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, engineeringProductName, engineeringProductId, 1, attributes, null);
 			for (int i = 1; i <= N; i++) {
 				String contentId = String.format(contentIdStringFormat,i);	// must be numeric (and unique) defined above
 				CandlepinTasks.addContentToProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, engineeringProductId, contentId, /*randomGenerator.nextBoolean()*/i%3==0?true:false);	// WARNING: Be careful with the enabled flag! If the same content is enabled under one product and then disabled in another product, the tests to assert enabled or disabled will both fail due to conflict of interest.  Therefore use this flag with some pseudo-randomness 
 			}
 			CandlepinTasks.createSubscriptionAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, 20, -1*24*60/*1 day ago*/, 15*24*60/*15 days from now*/, getRandInt(), getRandInt(), marketingProductId, Arrays.asList(engineeringProductId));
 		}
 		
 		// NOTE: To get the product certs, use the CandlepinTasks REST API:
         //"url": "/products/{product_uuid}/certificate", 
         //"GET"
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
 				if (contentNamespace.enabled && clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
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
 				if (contentNamespace.enabled && clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
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
 				if (contentNamespace.enabled) {
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
