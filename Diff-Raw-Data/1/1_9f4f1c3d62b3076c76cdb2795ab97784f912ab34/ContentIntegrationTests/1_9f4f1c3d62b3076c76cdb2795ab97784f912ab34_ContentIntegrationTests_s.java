 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
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
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.ContentNamespace;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductNamespace;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 
 /**
  * @author jsefler
  *
  * References:
  * https://docspace.corp.redhat.com/docs/DOC-60198
  * http://gibson.usersys.redhat.com:9000/Integration-Testing-Issues
  * https://docspace.corp.redhat.com/docs/DOC-63084	Stage Env Data Setup for Content Testing (automation) - rows highlighted in yellow do not exist to date
  * https://docspace.corp.redhat.com/docs/DOC-67214	Stage Env Data Setup for Content Testing (5.7 Beta Release)
  * https://docspace.corp.redhat.com/docs/DOC-68623	RHEL 5.7 Content Re-validation Testing 2011-06-14
  * https://docspace.corp.redhat.com/docs/DOC-75443  RHEL6.2 Content Testing On Stage Env (Internal Beta)
  * 
  * https://docspace.corp.redhat.com/docs/DOC-71135	Red Hat Product Certificates 
  * https://docspace.corp.redhat.com/docs/DOC-70016#Config_sku_model
  * 
  * Where to look when a product cert does not get installed:
  * e.g.
  * repo [rhel-scalefs-for-rhel-5-server-rpms]  
  * baseurl=https://cdn.redhat.com/content/dist/rhel/server/5/$releasever/$basearch/scalablefilesystem/os
  * if the expected productid has 92 is not getting installed from this repo, browse to:
  * http://download.devel.redhat.com/cds/prod/content/dist/rhel/server/5/5Server/x86_64/scalablefilesystem/os/repodata/
  * if no productid is there, then contact rhel-eng/jgreguske/dgregor
  */
 
 @Test(groups={"ContentIntegrationTests"})
 public class ContentIntegrationTests extends SubscriptionManagerCLITestScript{
 
 	
 	// Test Methods ***********************************************************************
 
 	@Test(	description="register and subscribe to expected product subscription",
 			groups={"AcceptanceTests"},
 			dataProvider="getSubscribeData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId
 	public void RegisterAndSubscribe_Test(String username, String password, ConsumerType type, String productId, String variant, String arch, Integer sockets, String engProductId) {
 
 		// register a new consumer
 		registerConsumerWhenNotAlreadyRegistered(username, password, type, sockets);	
 
 		// assert non-availability based on sockets
 		if (sockets!=null) {
 			// set client's sockets value one higher than subscription supports
 			Map<String,String> factsMap = new HashMap<String,String>();
 			Integer moreSockets = sockets+1;
 			factsMap.put("cpu.cpu_socket(s)", String.valueOf(moreSockets));
 			//factsMap.put("lscpu.cpu_socket(s)", String.valueOf(moreSockets));
 			clienttasks.createFactsFileWithOverridingValues(factsMap);
 			clienttasks.facts(null,true,null,null,null);
 			SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAvailableSubscriptionPools());
 			Assert.assertNull(pool, "Subscription pool for product '"+productId+"' is NOT available when the client's sockets (simulated cpu.cpu_socket(s)='"+moreSockets+"') exceed '"+sockets+"'.");
 			factsMap.put("cpu.cpu_socket(s)", String.valueOf(sockets));
 			//factsMap.put("lscpu.cpu_socket(s)", String.valueOf(sockets));
 			clienttasks.createFactsFileWithOverridingValues(factsMap);
 			clienttasks.facts(null,true,null,null,null);
 		}
 
 		// get the pools available to this registered consumer
 		List<SubscriptionPool> availablePools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 
 		// assert non-availability based on variant
 		/* THIS IS NOT A FILTER
 		if (variant!=null) { 
 			List<String> variants = new ArrayList<String>(Arrays.asList(variant.trim().toUpperCase().split(" *, *")));	// Note: the variant attribute can be a comma separated list of values
 			if (!variants.contains(clienttasks.variant.toUpperCase())) {
 				SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, availablePools);
 				Assert.assertNull(pool, "Subscription pool for product '"+productId+"' is NOT available when the client arch (actual='"+clienttasks.variant+"') is not contained in '"+variants+"'.");				
 			}
 		}
 		*/
 		
 		// assert non-availability based on arch
 		if (arch!=null) { 
 			List<String> arches = new ArrayList<String>(Arrays.asList(arch.trim().toUpperCase().split(" *, *")));	// Note: the arch attribute can be a comma separated list of values
 			if (arches.contains("X86")) {arches.addAll(Arrays.asList("I386","I486","I586","I686"));}  // Note" x86 is a general term to cover all 32-bit intel micrprocessors 
 			if (!arches.contains(clienttasks.arch.toUpperCase())) {
 				SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, availablePools);
 				Assert.assertNull(pool, "Subscription pool for product '"+productId+"' is NOT available when the client arch (actual='"+clienttasks.arch+"') is not contained in '"+arches+"'.");
 
 				pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 				Assert.assertNotNull(pool, "Subscription pool for product '"+productId+"' is only listed in --all --available when the client arch (actual='"+clienttasks.arch+"') is not contained in '"+arches+"'.");
 				File entitlementCertFile = clienttasks.subscribeToSubscriptionPool(pool); currentlySubscribedProductIds.add(productId);
 				EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 				assertEngProductsAreProvidedInEntitlementCert(engProductId, entitlementCert);
 				log.warning("No need for further testing of subscription productId '"+productId+"' on this hardware since the providing pool is not normally available.");
 				return;
 			}
 		}
 		
 		// subscribe to the first available pool providing the productId
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, availablePools);
 		Assert.assertNotNull(pool,"Found first available pool to subscribe to productId '"+productId+"'.");
 		File entitlementCertFile = clienttasks.subscribeToSubscriptionPool(pool); currentlySubscribedProductIds.add(productId);
 		
 		// setup data for subsequent tests
 		// TODO MAYBE WE SHOULD ONLY DO THIS WHEN variants.contains(clienttasks.variant)) OR WHEN SUBSCRIPTION IS AVAILABLE?
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		entitlementCertData.add(Arrays.asList(new Object[]{username, password, type, productId, sockets, entitlementCert}));
 		
 		// assert that the entitlementCert contains productNamespaces for the engProductId(s)
 		if (engProductId!=null) {
 			assertEngProductsAreProvidedInEntitlementCert(engProductId, entitlementCert);
 		}
 	}
 	
 	@Test(	description="verify the CDN provides packages for the default enabled content set after subscribing to a product subscription",
 			groups={"VerifyPackagesAreAvailable"},
 			dependsOnMethods={"RegisterAndSubscribe_Test"}, alwaysRun=true,
 			dataProvider="getDefaultEnabledContentNamespaceData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId
 	public void VerifyPackagesAreAvailableForDefaultEnabledContentNamespace_Test(String username, String password, ConsumerType type, String productId, Integer sockets, ContentNamespace contentNamespace) {
 		String abled = contentNamespace.enabled.equals("1")? "enabled":"disabled";	// is this an enabled or disabled test?
 		EntitlementCert entitlementCert = recallTheEntitlementCertGrantedAfterSubscribing(username, password, type, productId, sockets);
 		Integer packageCount=null;
 
 //if(true) throw new SkipException("debugging");
 		// register a new consumer
 		registerConsumerWhenNotAlreadyRegistered(username, password, type, sockets);
 		
 		// assert that there are not yet any available packages from the default enabled/disabled repo
 		// NOT REALLY A VALID ASSERTION WHEN WE COULD ALREADY BE SUBSCRIBED (FOR EFFICIENCY SAKE).  MOREOVER, THE MORE APPROPRIATE ASSERTION COMES AFTER THE SUBSCRIBE)   
 		//packageCount = clienttasks.getYumRepolistPackageCount(contentNamespace.label);
 		//Assert.assertEquals(packageCount,Integer.valueOf(0),"Before subscribing to product subscription '"+productId+"', the number of available packages '"+packageCount+"' from the default "+abled+" repo '"+contentNamespace.label+"' is zero.");
 
 		// subscribe
 		if (!currentlySubscribedProductIds.contains(productId)) { // try to save some time by not re-subscribing
 			clienttasks.subscribeToProductId(productId); currentlySubscribedProductIds.add(productId);
 		} else {
 			log.info("Saving time by assuming that we are already subscribed to productId='"+productId+"'");
 			//clienttasks.list_(null,null,null, Boolean.TRUE, null, null, null, null);
 		}
 
 		// Assert that after subscribing, the default enabled/disabled repo is now included in yum repolist
 		ArrayList<String> repolist = clienttasks.getYumRepolist(abled);
 		if (clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 			Assert.assertTrue(repolist.contains(contentNamespace.label),
 				"Yum repolist "+abled+" includes "+abled+" repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+productId+"'.");
 		} else {
 			log.warning("Did not find all the requiredTags '"+contentNamespace.requiredTags+"' for this content namespace amongst the currently installed products.");
 			Assert.assertFalse(repolist.contains(contentNamespace.label),
 				"Yum repolist "+abled+" excludes "+abled+" repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+productId+"' because not all requiredTags '"+contentNamespace.requiredTags+"' in the contentNamespace are provided by the currently installed productCerts.");
 			throw new SkipException("This contentNamespace has requiredTags '"+contentNamespace.requiredTags+"' that were not found amongst all of the currently installed products.  Therefore we cannot verify that the CDN is providing packages for repo '"+contentNamespace.label+"'.");
 		}
 
 		// verify the yum repolist contentNamespace.label returns more than 0 packages
 		String options = contentNamespace.enabled.equals("1")? contentNamespace.label:contentNamespace.label+" --enablerepo="+contentNamespace.label;
 		packageCount = clienttasks.getYumRepolistPackageCount(options);
 		Assert.assertTrue(packageCount>0,"After subscribing to product subscription '"+productId+"', the number of available packages from the default "+abled+" repo '"+contentNamespace.label+"' is greater than zero (actual packageCount is '"+packageCount+"').");
 		
 		// TODO populate data for subsequent calls to InstallAndRemoveAnyPackageFromContentNamespace_Test 
 		contentNamespaceData.add(Arrays.asList(new Object[]{username, password, type, productId, sockets, contentNamespace, entitlementCert}));
 	}
 	
 	
 	@Test(	description="verify the CDN provides packages for the non-default enabled content set after subscribing to a product subscription",
 			groups={"VerifyPackagesAreAvailable"},
 			dependsOnMethods={"RegisterAndSubscribe_Test"}, alwaysRun=true,
 			dataProvider="getDefaultDisabledContentNamespaceData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId
 	public void VerifyPackagesAreAvailableForDefaultDisabledContentNamespace_Test(String username, String password, ConsumerType type, String productId, Integer sockets, ContentNamespace contentNamespace) {
 		Assert.assertEquals(contentNamespace.enabled,"0","Reconfirming that we are are about to test a default disabled contentNamespace.");
 		VerifyPackagesAreAvailableForDefaultEnabledContentNamespace_Test(username, password, type, productId, sockets, contentNamespace);
 	}
 	
 	
 	@Test(	description="ensure a random available package can be downloaded from the enabled repo ",
 			groups={},
 			dependsOnMethods={"RegisterAndSubscribe_Test"}, alwaysRun=true,
 			dependsOnGroups={"VerifyPackagesAreAvailable"},
 			dataProvider="getContentNamespaceData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId
 	public void DownloadRandomPackageFromContentNamespace_Test(String username, String password, ConsumerType type, String productId, Integer sockets, ContentNamespace contentNamespace) {
 		EntitlementCert entitlementCert = recallTheEntitlementCertGrantedAfterSubscribing(username, password, type, productId, sockets);
 
 		// register
 		registerConsumerWhenNotAlreadyRegistered(username, password, type, sockets);
 		
 		// subscribe
 		if (!currentlySubscribedProductIds.contains(productId)) { // try to save some time by not re-subscribing
 			clienttasks.subscribeToProductId(productId); currentlySubscribedProductIds.add(productId);
 		} else {
 			log.info("Saving time by assuming that we are already subscribed to productId='"+productId+"'");
 			//clienttasks.list_(null,null,null, Boolean.TRUE, null, null, null, null);
 		}
 		
 		
 		// make sure that the products required for this repo are installed
 		/* not needed anymore since this case is already filtered out by the dataProvider
 		if (!clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 			throw new SkipException("This contentNamespace has requiredTags '"+contentNamespace.requiredTags+"' that were not found amongst all of the currently installed products.  Therefore we cannot install and remove any package from repo '"+contentNamespace.label+"'.");
 		}
 		*/
 		
 		
 		// make sure there is a positive package count provided by this repo
 		/* not needed anymore since this case is already filtered out by the dataProvider
 		Integer packageCount = clienttasks.getYumRepolistPackageCount(contentNamespace.label+" --enablerepo="+contentNamespace.label);
 		if (packageCount==0) {
 			throw new SkipException("Cannot install a package from this repo '"+contentNamespace.label+"' since it is not providing any packages.");
 		}
 		*/
 		
 		// find a random available package provided by this repo
 		String pkg = clienttasks.findRandomAvailablePackageFromRepo(contentNamespace.label);
 		if (pkg==null) {
 			throw new SkipException("Could NOT find a random available package from this repo '"+contentNamespace.label+"' to attempt an install/remove test.");
 		}
 		
 		// download the package and assert that it is successfully downloaded
 		File pkgFile = clienttasks.yumDownloadPackageFromRepo(pkg, contentNamespace.label, "/tmp", null);
 		Assert.assertNotNull(pkgFile, "The actual downloaded package file is: "+pkgFile);
 		
 		// remove the file since it is not needed anymore
 		client.runCommandAndWait("rm -f "+pkgFile);
 
 	}
 	
 	@Test(	description="ensure a unique available package can be installed/removed from the enabled repo ",
 			groups={},
 			dependsOnMethods={"RegisterAndSubscribe_Test"}, alwaysRun=true,
 			dependsOnGroups={"VerifyPackagesAreAvailable"},
 			dataProvider="getContentNamespaceData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId
 	public void InstallAndRemoveUniquePackageFromContentNamespace_Test(String username, String password, ConsumerType type, String productId, Integer sockets, ContentNamespace contentNamespace) {
 		EntitlementCert entitlementCert = recallTheEntitlementCertGrantedAfterSubscribing(username, password, type, productId, sockets);
 
 //if (!contentNamespace.label.equals("rhel-6-server-beta-debug-rpms")) throw new SkipException("debugging");
 		// register
 		registerConsumerWhenNotAlreadyRegistered(username, password, type, sockets);
 		
 		// subscribe
 		if (!currentlySubscribedProductIds.contains(productId)) { // try to save some time by not re-subscribing
 			clienttasks.subscribeToProductId(productId); currentlySubscribedProductIds.add(productId);
 		} else {
 			log.info("Saving time by assuming that we are already subscribed to productId='"+productId+"'");
 			//clienttasks.list_(null,null,null, Boolean.TRUE, null, null, null, null);
 		}
 		
 		
 		// make sure that the products required for this repo are installed
 		/* not needed anymore since this case is already filtered out by the dataProvider
 		if (!clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 			throw new SkipException("This contentNamespace has requiredTags '"+contentNamespace.requiredTags+"' that were not found amongst all of the currently installed products.  Therefore we cannot install and remove any package from repo '"+contentNamespace.label+"'.");
 		}
 		*/
 		
 		
 		// make sure there is a positive package count provided by this repo
 		/* not needed anymore since this case is already filtered out by the dataProvider
 		Integer packageCount = clienttasks.getYumRepolistPackageCount(contentNamespace.label+" --enablerepo="+contentNamespace.label);
 		if (packageCount==0) {
 			throw new SkipException("Cannot install a package from this repo '"+contentNamespace.label+"' since it is not providing any packages.");
 		}
 		*/
 		
 		// find an available package that is uniquely provided by this repo
 		String pkg = clienttasks.findUniqueAvailablePackageFromRepo(contentNamespace.label);
 		if (pkg==null) {
 			throw new SkipException("Could NOT find a unique available package from this repo '"+contentNamespace.label+"' to attempt an install/remove test.");
 		}
 //pkg="cairo-spice-debuginfo.x86_64";
 		
 		// install the package and assert that it is successfully installed
 		clienttasks.yumInstallPackageFromRepo(pkg, contentNamespace.label, null); //pkgInstalled = true;
 
 
 		// 06/09/2011 TODO Would also like to add an assert that the productid.pem file is also installed.
 		/* To do this, we need to also include the List of ProductNamepaces from the entitlement cert as another argument to this test,
 		 * Then after the yum install, we need to make sure that at least one of the hash values in the list of product ids is
 		 * included in the installed products.  If the list of ProductNamespaces from the entitlement cert is one, then this is
 		 * a definitive test.  If the list is greater than one, then we don't know for sure if the product hash(s) that is installed is actually the 
 		 * right one.  But we do know that if none of the product hashes are installed, then the repo is missing the product ids and this test should fail.
 		 * Also note that we should probably make this assertion after removing the package so that we don't over install all the packages in the repo.
 		 */
 		// 06/10/2011 Mostly Done in the following blocks of code; jsefler
 		
 		// determine if at least one of the productids from the productNamespaces was found installed on the client after running yumInstallPackageFromRepo(...)
 		// ideally there is only one ProductNamespace in productNamespaces in which case we can definitively know that the correct product cert is installed
 		// when there are more than one ProductNamespace in productNamespaces, then we can't say for sure if the product cert installed actually corresponds to the repo under test
 		// however if none of the productNamespaces ends up installed, then the yum product-id plugin is not installing the expected product cert
 		int numberOfProductNamespacesInstalled = 0;
 		ProductCert productCertInstalled=null;
 		for (ProductCert productCert : clienttasks.getCurrentProductCerts()) {
 			for (ProductNamespace productNamespace : entitlementCert.productNamespaces) {
 				if (productNamespace.id.equals(productCert.productId)) {
 					numberOfProductNamespacesInstalled++;
 					productCertInstalled=productCert;
 				}
 			}
 		}
 
 		//FIXME check if the package was obsolete and its replacement was installed instead
 		//if (!obsoletedByPkg.isEmpty()) pkg = obsoletedByPkg;
 		
 		// now remove the package
 		clienttasks.yumRemovePackage(pkg);
 		
 		// assert that a productid.pem is/was installed that covers the product from which this package was installed
 		// Note: I am making this assertion after the yumRemovePackage call to avoid leaving packages installed in case this assert fails.
 		if (numberOfProductNamespacesInstalled>1) {
 			log.info("Found product certs installed that match the ProductNamespaces from the entitlement cert that granted the right to install package '"+pkg+"' from repo '"+contentNamespace.label+"'.");
 		} else if (numberOfProductNamespacesInstalled==1){
 			Assert.assertTrue(true,"An installed product cert (productName='"+productCertInstalled.productName+"' productId='"+productCertInstalled.productId+"') corresponding to installed package '"+pkg+"' from repo '"+contentNamespace.label+"' was found after it was installed.");
 		} else {
 			Assert.fail("After installing package '"+pkg+"' from repo '"+contentNamespace.label+"', there was no product cert installed.  Expected one of the following product certs to get installed via the yum product-id plugin: "+entitlementCert.productNamespaces);		
 		}
 	}
 	
 	
 	
 	
 //	@Test()
 //	@TestNgPriority(400)
 //	public void Test400() {}	
 //	@Test()
 //	@TestNgPriority(500)
 //	public void Test500() {}
 //	@Test()
 //	@TestNgPriority(100)
 //	public void Test100() {}
 //	@Test()
 //	@TestNgPriority(200)
 //	public void Test200() {}
 //	@Test(dependsOnMethods={"Test200"})	// adding a dependsOn* breaks the priority
 //	@TestNgPriority(300)
 //	public void Test300() {}
 
 	// Candidates for an automated Test:
 	// TODO Bug 689031 - nss needs to be able to use pem files interchangeably in a single process 
 	/* TODO
 	[Bug 756737] RC4.0 content testing: package conflict and/or content missing on RHEL Server - x86_64
 	[Bug 756752] RC4.0 content testing: package conflict and/or content missing on RHEL Server - i386
 	[Bug 756753] RC4.0 content testing: package conflict and/or content missing on RHEL Client - x86_64
 	[Bug 756744] RC4.0 content testing: package conflict and/or content missing on RHEL 6Client - i386
 	[Bug 756751] RC4.0 content testing: package conflict and/or content missing on RHEL Workstation - x86_64
 	[Bug 756760] RC4.0 content testing: package conflict and/or content missing on RHEL Workstation - i386
 	[Bug 756730] RC4.0 content testing: package conflict and/or content missing on RHEL for IBM Power - ppc64
 	[Bug 756735] RC4.0 content testing: package conflict and/or package version different on RHEL for IBM System z- s390
 	[Bug 756757] RC4.0 content testing: package conflict and/or content missing on RHEL Server for HPC Compute Node - x86_64
 	*/
 
 	/* TODO Bug 768012 - manifest import fails - 404 Resource Not Found 
 	 * On Fri, 2011-12-16 at 11:05 -0500, Keqin Hong wrote:
 > Hi Bryan and Dennis,
 > No problem we will add this check to our content testing.
 > But I have a question. pls see my comments inline.
 >
 > ----- Original Message -----
 > > From: "Dennis Gregorovic" <dgregor@redhat.com>
 > > To: "Bryan Kearney" <bkearney@redhat.com>
 > > Cc: "entitlement-team-list" <entitlement-team-list@redhat.com>, rhel5-leads-list@redhat.com
 > > Sent: Friday, December 16, 2011 12:22:47 AM
 > > Subject: Re: Request for additions to the content testing
 > >
 > > On Thu, 2011-12-15 at 09:58 -0500, Bryan Kearney wrote:
 > > > Keqin / Lawrence:
 > > >
 > > > I have a request to add to your CDN content testing suite. Today,
 > > > Katello testing (which is using the CDN) opened bug
 > > > https://bugzilla.redhat.com/show_bug.cgi?id=768012. The root cause
 > > > is
 > > > that a listing file was not pushed to the CDN. The actual file was
 > > > content/beta/rhel/server/5/5Server/listing
 > > >
 > > > The listing file tells downstream tools what versions and / or
 > > > architectures to look for. You tend to see them in the directory
 > > > which
 > > > as the versions (content/beta/rhel/server/5) and in the directories
 > > > that
 > > > have may arches (content/beta/rhel/server/5/5Server).
 > > >
 > > > Going forward, can you please add this to the items which you test
 > > > for?
 > > > Dgregor or others in RCM can give you the complete set of where
 > > > these
 > > > files should live.
 > > >
 > > > Thanks!
 > > >
 > > > -- bk
 > >
 > > They should live anywhere that there is a variable in the download
 > > URL.
 > > So, if the download URL is
 > >
 > > /content/dist/rhel/server/5/$releasever/$basearch/os
 > >
 > > then you would have the following listing files:
 > >
 > > /content/dist/rhel/server/5/5.6/listing
 > > /content/dist/rhel/server/5/5.7/listing
 > > /content/dist/rhel/server/5/5Server/listing
 > > /content/dist/rhel/server/5/listing
 >
 > 1. Does it exist on qa cdn? Currently there's no listing file under https://cdn.rcm-qa.redhat.com/content/dist/rhel/server/5/ .
 Fixed.  Thanks for catching that.
 
 >
 > It does exist on prod cdn, though.
 > # curl --cert 9175120542568818876.pem --key 9175120542568818876-key.pem -k https://cdn.redhat.com/content/beta/rhel/server/5/listing
 > 5.7
 > 5.8
 > 5Server
 >
 > Regards,
 > Keqin
 > >
 > > The contents of each listing file is the set of subdirectories at
 > > that
 > > level.  For $basearch listing files, the ordering doesn't matter.
 > >  For
 > > $releasever, the releases should be in ascending order with the
 > > default
 > > as the last.
 > >
 > > $ cat /content/dist/rhel/server/5/listing
 > > 5.6
 > > 5.7
 > > 5Server
 > >
 > > The exception to the above is RHUI.  For RHUI, the $releasever
 > > listing
 > > file only lists the default directory.
 > >
 > > $ cat /content/dist/rhel/rhui/server/5/listing
 > > 5Server
 > >
 > > Let me know if there is any other info I can provide.
 > >
 > > Cheers
 > > -- Dennis
 > >
 > >
 
 
 
 	 */
 	
 	
 	
 	// Configuration Methods ***********************************************************************
 	
 	
 	@BeforeClass(groups={"setup"})
 	public void disableAllRepos() {
 		// this is needed to disable all of the beaker-* repos that are enabled by default on Beaker provisioned hardware otherwise VERY long loops occur in clienttasks.findUniqueAvailablePackageFromRepo
 		clienttasks.yumDisableAllRepos();
 	}
 	
 	
 	@BeforeClass(groups={"setup"})
 	public void getCurrentProductCertsBeforeClass() {
 		currentProductCerts = clienttasks.getCurrentProductCerts();
 	}
 	
 	@AfterClass(groups={"setup"})
 	public void cleanupAfterClass() {
 		clienttasks.deleteFactsFileWithOverridingValues();
 	}
 	
 //	@BeforeClass(groups={"setup"})
 //	public void yumCleanAllBeforeClass() {
 //		clienttasks.yumClean("all");
 //	}
 	
 	// Protected Methods ***********************************************************************
 	
 	List<List<Object>> entitlementCertData = new ArrayList<List<Object>>();
 	List<List<Object>> contentNamespaceData = new ArrayList<List<Object>>();
 	List<ProductCert> currentProductCerts = new ArrayList<ProductCert>();
 	protected String currentRegisteredUsername = null;
 	protected List<String> currentlySubscribedProductIds = new ArrayList<String>();;
 	
 	
 	protected void registerConsumerWhenNotAlreadyRegistered(String username, String password, ConsumerType type, Integer sockets) {
 		// register a new consumer
 		if (!username.equals(currentRegisteredUsername)) { // try to save some time by not re-registering
 		
 			// set the consumer's sockets
 			clienttasks.deleteFactsFileWithOverridingValues();
 			if (sockets!=null) {
 				Map<String,String> factsMap = new HashMap<String,String>();
 				factsMap.put("cpu.cpu_socket(s)", String.valueOf(sockets));
 				//factsMap.put("lscpu.cpu_socket(s)", String.valueOf(sockets));
 				clienttasks.createFactsFileWithOverridingValues(factsMap);
 			}
 			
 			// register
 			clienttasks.register(username, password, null, null, type, null, null, null, (String)null, true, false, null, null, null);
 			currentRegisteredUsername = username;
 			currentlySubscribedProductIds.clear();
 		} else {
 			log.info("Saving time by assuming that we are already registered as username='"+username+"'");
 		}
 	}
 	
 	protected void assertEngProductsAreProvidedInEntitlementCert(String engProductId, EntitlementCert entitlementCert) {
 		// assert that the entitlementCert contains productNamespaces for the engProductId(s)
 		if (engProductId!=null) { 
 			List<String> engProductIds = new ArrayList<String>(Arrays.asList(engProductId.trim().toUpperCase().split(" *, *")));	// Note: the arch attribute can be a comma separated list of values
 			for (String id : engProductIds) {
 				boolean foundId = false;
 				for (ProductNamespace productNamespace : entitlementCert.productNamespaces) {
 					if (productNamespace.id.equals(id)) {
 						foundId = true; break;
 					}
 				}
 				//Assert.assertTrue(foundId, "After subscribing to product '"+productId+"', found the expected engineering product id '"+id+"' amongst the granted entitlement cert productNamespaces: "+entitlementCert.productNamespaces);
 				Assert.assertTrue(foundId, "Found the expected engineering product id '"+id+"' amongst the granted entitlement cert productNamespaces: "+entitlementCert.productNamespaces);
 			}
 			//Assert.assertEquals(entitlementCert.productNamespaces.size(), engProductIds.size(), "After subscribing to product '"+productId+"', the number of possible provided engineering product ids from the granted entitlement cert matches the expected list: "+engProductIds);			
 			Assert.assertEquals(entitlementCert.productNamespaces.size(), engProductIds.size(), "The number of possible provided engineering product ids from the granted entitlement cert matches the expected list: "+engProductIds);			
 		}
 	}
 	
 	protected EntitlementCert recallTheEntitlementCertGrantedAfterSubscribing(String username, String password, ConsumerType type, String productId, Integer sockets)  {
 		for (List<Object> row : entitlementCertData) {
 			if ((String)row.get(0) != username) continue;
 			if ((String)row.get(1) != password) continue;
 			if ((ConsumerType)row.get(2) != type) continue;
 			if ((String)row.get(3) != productId) continue;
 			if ((Integer)row.get(4) != sockets) continue;
 			return (EntitlementCert)row.get(5);
 		}
 		Assert.fail("Failed to recall the entitlement cert granted to: username='"+username+"' password='"+password+"' type='"+type+"' productId='"+productId+"' sockets='"+sockets+"'.");
 		return null;
 	}
 	
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="getSubscribeData")
 	public Object[][] getSubscribeDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getSubscribeDataAsListOfLists());
 	}
 	protected List<List<Object>> getSubscribeDataAsListOfLists() throws JSONException {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 /*		
 sm.content.integrationTestData:[
 	{
 		username:'stage_test_5',
 		password:'redhat',
 		type:'RHUI',						//** OPTIONAL **
 		variant:'Server,ComputeNode',		//** OPTIONAL **	** COMMA SEPARATED **
 		arch:'x86,x86_64',					//** OPTIONAL **	** COMMA SEPARATED **
 		sockets:'8',						//** OPTIONAL **
 		productIdsData:[
 			{
 		 		productId:'RH0179918',
 		 		sockets:'8',				//** OPTIONAL **
 		 		engProductId:'8',			//** OPTIONAL **	** COMMA SEPARATED **
 		 		reposData:[					//** NOT YET IMPLEMENTED **
 		 			{
 		 				repo:'label',
 		 				packages:'pkg1,pkg2'
 		 			}
 		 		]
 		 	},
 		 	{
 		 		productId:'RH1232091'
 		 	},
 		 	{
 		 		productId:'RH1151626'
 		 	},
 		 	{
 		 		productId:'RH1469292'
 		 	}
 		 ]
 	}
 ]
 */
 
 		JSONArray jsonIntegrationTestData = sm_contentIntegrationTestData;
 		for (int i = 0; i < jsonIntegrationTestData.length(); i++) {
 			JSONObject jsonIntegrationTestDatum = (JSONObject) jsonIntegrationTestData.get(i);
 			String username = jsonIntegrationTestDatum.getString("username");
 			String password = jsonIntegrationTestDatum.getString("password");
 			ConsumerType type = null;
 			if (jsonIntegrationTestDatum.has("type")) type = ConsumerType.valueOf(jsonIntegrationTestDatum.getString("type"));
 			String variant = null;	// can be comma separated
 			if (jsonIntegrationTestDatum.has("variant")) variant = jsonIntegrationTestDatum.getString("variant");
 			String arch = null;	// can be comma separated
 			if (jsonIntegrationTestDatum.has("arch")) arch = jsonIntegrationTestDatum.getString("arch");
 			
 
 		
 			JSONArray jsonProductIdsData = (JSONArray) jsonIntegrationTestDatum.getJSONArray("productIdsData");
 			for (int j = 0; j < jsonProductIdsData.length(); j++) {
 				JSONObject jsonProductIdsDatum = (JSONObject) jsonProductIdsData.get(j);
 				String productId = jsonProductIdsDatum.getString("productId");
 				Integer sockets = null;
 				if (jsonProductIdsDatum.has("sockets")) sockets = jsonProductIdsDatum.getInt("sockets");
 				String engProductId = null;	// can be comma separated
 				if (jsonProductIdsDatum.has("engProductId")) engProductId = jsonProductIdsDatum.getString("engProductId");
 				
 				// String username, String password, String productId, Integer sockets
 				ll.add(Arrays.asList(new Object[]{username, password, type, productId, variant, arch, sockets, engProductId}));
 			}
 		}
 		
 		return ll;
 	}
 	
 	@DataProvider(name="getDefaultEnabledContentNamespaceData")
 	public Object[][] getDefaultEnabledContentNamespaceDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getContentNamespaceDataAsListOfLists("1"));
 	}
 	@DataProvider(name="getDefaultDisabledContentNamespaceData")
 	public Object[][] getDefaultDisabledContentNamespaceDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getContentNamespaceDataAsListOfLists("0"));
 	}
 	protected List<List<Object>> getContentNamespaceDataAsListOfLists(String enabledValue) throws JSONException {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		for (List<Object> row : entitlementCertData) {
 			String username = (String) row.get(0);
 			String password = (String) row.get(1);
 			ConsumerType type = (ConsumerType) row.get(2);
 			String productId = (String) row.get(3);
 			Integer sockets = (Integer) row.get(4);
 			EntitlementCert entitlementCert = (EntitlementCert) row.get(5);
 			
 			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 				if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 				if (contentNamespace.enabled.equals(enabledValue)) {	// enabled="1", not enabled="0"
 					
 					//
 					ll.add(Arrays.asList(new Object[]{username, password, type, productId, sockets, contentNamespace}));
 				}
 			}
 		}
 		return ll;
 	}
 
 	@DataProvider(name="getContentNamespaceData")
 	public Object[][] getContentNamespaceDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getContentNamespaceDataAsListOfLists());
 	}
 	protected List<List<Object>> getContentNamespaceDataAsListOfLists() throws JSONException {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		for (List<Object> row : contentNamespaceData) {
 			String username = (String) row.get(0);
 			String password = (String) row.get(1);
 			ConsumerType type = (ConsumerType) row.get(2);
 			String productId = (String) row.get(3);
 			Integer sockets = (Integer) row.get(4);
 			ContentNamespace contentNamespace = (ContentNamespace) row.get(5);
 			EntitlementCert entitlementCert = (EntitlementCert) row.get(6);
 			
 			//
 			ll.add(Arrays.asList(new Object[]{username, password, type, productId, sockets, contentNamespace}));
 
 		}
 		return ll;
 	}
 }
 
 
 
 //[root@jsefler-betastage-server pki]# yum -y install cairo-spice-debuginfo.x86_64 --enablerepo=rhel-6-server-beta-debug-rpms --disableplugin=rhnplugin
 //Loaded plugins: product-id, refresh-packagekit, subscription-manager
 //No plugin match for: rhnplugin
 //Updating Red Hat repositories.
 //INFO:rhsm-app.repolib:repos updated: 63
 //rhel-6-server-beta-debug-rpms                                                                                                                   |  951 B     00:00     
 //rhel-6-server-beta-rpms                                                                                                                         | 3.7 kB     00:00     
 //rhel-6-server-rpms                                                                                                                              | 2.1 kB     00:00     
 //Setting up Install Process
 //Package cairo-spice-debuginfo is obsoleted by spice-server, trying to install spice-server-0.7.3-2.el6.x86_64 instead
 //Resolving Dependencies
 //--> Running transaction check
 //---> Package spice-server.x86_64 0:0.7.3-2.el6 will be installed
 //--> Finished Dependency Resolution
 //
 //Dependencies Resolved
 //
 //=======================================================================================================================================================================
 //Package                                Arch                             Version                               Repository                                         Size
 //=======================================================================================================================================================================
 //Installing:
 //spice-server                           x86_64                           0.7.3-2.el6                           rhel-6-server-beta-rpms                           245 k
 //
 //Transaction Summary
 //=======================================================================================================================================================================
 //Install       1 Package(s)
 //
 //Total download size: 245 k
 //Installed size: 913 k
 //Downloading Packages:
 //spice-server-0.7.3-2.el6.x86_64.rpm                                                                                                             | 245 kB     00:00     
 //Running rpm_check_debug
 //Running Transaction Test
 //Transaction Test Succeeded
 //Running Transaction
 //Installing : spice-server-0.7.3-2.el6.x86_64                                                                                                                     1/1 
 //duration: 297(ms)
 //Installed products updated.
 //
 //Installed:
 //spice-server.x86_64 0:0.7.3-2.el6                                                                                                                                    
 //
 //Complete!
 //[root@jsefler-betastage-server pki]# yum remove spice-server.x86_64
 //Loaded plugins: product-id, refresh-packagekit, subscription-manager
 //Updating Red Hat repositories.
 //INFO:rhsm-app.repolib:repos updated: 63
 //Setting up Remove Process
 //Resolving Dependencies
 //--> Running transaction check
 //---> Package spice-server.x86_64 0:0.7.3-2.el6 will be erased
 //--> Finished Dependency Resolution
 //rhel-6-server-beta-rpms                                                                                                                         | 3.7 kB     00:00     
 //rhel-6-server-rpms                                                                                                                              | 2.1 kB     00:00     
 //
 //Dependencies Resolved
 //
 //=======================================================================================================================================================================
 //Package                               Arch                            Version                                 Repository                                         Size
 //=======================================================================================================================================================================
 //Removing:
 //spice-server                          x86_64                          0.7.3-2.el6                             @rhel-6-server-beta-rpms                          913 k
 //
 //Transaction Summary
 //=======================================================================================================================================================================
 //Remove        1 Package(s)
 //
 //Installed size: 913 k
 //Is this ok [y/N]: y
 //Downloading Packages:
 //Running rpm_check_debug
 //Running Transaction Test
 //Transaction Test Succeeded
 //Running Transaction
 //Erasing    : spice-server-0.7.3-2.el6.x86_64                                                                                                                     1/1 
 //duration: 207(ms)
 //Installed products updated.
 //
 //Removed:
 //spice-server.x86_64 0:0.7.3-2.el6                                                                                                                                    
 //
 //Complete!
 //[root@jsefler-betastage-server pki]# 
