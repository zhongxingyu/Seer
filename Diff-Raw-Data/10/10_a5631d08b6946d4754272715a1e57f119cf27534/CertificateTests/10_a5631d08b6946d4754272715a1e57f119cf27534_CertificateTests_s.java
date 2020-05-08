 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 
 import org.testng.SkipException;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.ContentNamespace;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.OrderNamespace;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductNamespace;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  *
  */
 @Test(groups={"CertificateTests"})
 public class CertificateTests extends SubscriptionManagerCLITestScript {
 
 	// Test methods ***********************************************************************
 	
 	@Test(	description="Verify that a base product cert corresponding to the /etc/redhat-release is installed",
 			groups={"AcceptanceTests","blockedByBug-706518"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyBaseRHELProductCertIsInstalled_Test() {
 // DELETEME		OLD IMPLEMENTATION		
 //		// list the currently installed products
 //		clienttasks.listInstalledProducts();
 //		
 //		// check each of the installed product certs in search of one that matches the /etc/redhat-release
 //		log.info("Checking each installed product cert for one that matches /etc/redhat-release: "+clienttasks.redhatRelease);
 //		for (ProductCert productCert : clienttasks.getCurrentProductCerts()) {
 //			log.info("Found installed product cert: "+productCert);
 //			String productCertName = productCert.productName;
 //			
 //			// strip out numbers from the product cert name before checking for a match...  e.g name='Red Hat Enterprise Linux 6 Server' => 'Red Hat Enterprise Linux Server'
 //			// Example redhatRelease: Red Hat Enterprise Linux Server release 6.2 Beta (Santiago)
 //			// Example product cert: name="Red Hat Enterprise Linux 6 Server" version="6.2 Beta"  (69.pem)
 //			productCertName = productCertName.replaceAll("\\d", "").replaceAll(" {2,}", " ");
 //
 //			// adjust the product cert name "for Scientific Computing" => "ComputeNode"  (76.pem)
 //			productCertName = productCertName.replaceFirst("for Scientific Computing","ComputeNode");
 //
 //			// adjust the product cert name "for IBM POWER" => "Server"  (74.pem)
 //			productCertName = productCertName.replaceFirst("for IBM POWER","Server");
 //
 //			// adjust the product cert name "for IBM System z" => "Server"  (72.pem)
 //			productCertName = productCertName.replaceFirst("for IBM System z","Server");
 //
 //			// adjust the product cert name "Desktop" => "Client"  (68.pem)
 //			productCertName = productCertName.replaceFirst("Desktop","Client");
 //
 //			// adjust the product cert name "for Workstation" => "Workstation"  (71.pem)
 //			//productCertName = productCertName.replaceFirst("Workstation","Workstation");
 //
 //			if (clienttasks.redhatRelease.startsWith(String.format("%s release %s", productCertName, productCert.productNamespace.version))) {
 //				Assert.assertTrue(true,"Found the following installed product cert that appears to match the /etc/redhat-release ("+clienttasks.redhatRelease+"): "+productCert);
 //				return;
 //			}
 //			
 //			//TODO We should also verify the arch  e.g. uname.machine: s390x when on IBM System z
 //		}
 //		Assert.fail("Could NOT find an installed installed product cert that matches /etc/redhat-release ("+clienttasks.redhatRelease+").");
 
 		// list the currently installed products
 		clienttasks.listInstalledProducts();
 		
 		// list the currently installed product certs
 		log.info("Installed product certs: ");
 		List <ProductCert> productCerts = clienttasks.getCurrentProductCerts();
 		for (ProductCert productCert : productCerts) {
 			log.info(productCert.toString());
 		}
 		
 		// assert that a product cert is installed that matches our base RHEL release version
 		ProductCert productCert = null;
 		if (clienttasks.releasever.equals("5Server") || clienttasks.releasever.equals("6Server")) {
 			if (clienttasks.arch.startsWith("ppc")) {															// ppc ppc64
 				productCert = ProductCert.findFirstInstanceWithMatchingFieldFromList("productId", "74", productCerts);	// Red Hat Enterprise Linux for IBM POWER
 			} else if (clienttasks.arch.startsWith("s390")) {													// s390 s390x
 				productCert = ProductCert.findFirstInstanceWithMatchingFieldFromList("productId", "72", productCerts);	// Red Hat Enterprise Linux for IBM System z
 			} else { 																							// i386 i686 ia64 x86_64
 				productCert = ProductCert.findFirstInstanceWithMatchingFieldFromList("productId", "69", productCerts);	// Red Hat Enterprise Linux Server
 			}
 		} else if (clienttasks.releasever.equals("5Client") || clienttasks.releasever.equals("6Workstation")) {	// i386 i686 x86_64
 				productCert = ProductCert.findFirstInstanceWithMatchingFieldFromList("productId", "71", productCerts);	// Red Hat Enterprise Linux Workstation
 		} else if (clienttasks.releasever.equals("6Client")) {													// i386 i686 x86_64
 				productCert = ProductCert.findFirstInstanceWithMatchingFieldFromList("productId", "68", productCerts);	// Red Hat Enterprise Linux Desktop
 		} else if (clienttasks.releasever.equals("6ComputeNode")) {												// i386 i686 x86_64
 				productCert = ProductCert.findFirstInstanceWithMatchingFieldFromList("productId", "76", productCerts);	// Red Hat Enterprise Linux for Scientific Computing
 		}
 		
 		Assert.assertNotNull(productCert,"Found an installed product cert that matches our base RHEL product...");
 		log.info(productCert.toString());
 	}
 	
 	
 	@Test(	description="candidate product cert validity dates",
 			groups={"AcceptanceTests"},
 			dataProvider="getProductCertFilesData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=64656)
 	public void VerifyValidityPeriodInProductCerts_Test(File productCertFile) {
 		
 		ProductCert productCert = clienttasks.getProductCertFromProductCertFile(productCertFile);
 		long actualValidityDurationDays = (productCert.validityNotAfter.getTimeInMillis() - productCert.validityNotBefore.getTimeInMillis())/(24*60*60*1000);
 		
 		log.info("Verifying the validity period in product cert '"+productCertFile+"': "+productCert);
 		log.info("The validity period for this product cert is (days): "+actualValidityDurationDays);
 		Calendar now = Calendar.getInstance();
 		
 		// verify that the validity period for this product cert has already begun.
 		Assert.assertTrue(productCert.validityNotBefore.before(now),"This validity period for this product cert has already begun.");
 	
 		// verify that the validity period for this product cert has not yet ended.
 		Assert.assertTrue(productCert.validityNotAfter.after(now),"This validity period for this product cert has not yet ended.");
 
 		// verify that the validity period for this product cert is among the expected values.
 		List<Long>expectedValidityDurationDaysList=new ArrayList<Long>();
 		String productCertValidityDuration = sm_productCertValidityDuration;
 		for (String expectedValidityDurationDayOption :  Arrays.asList(productCertValidityDuration.trim().split(" *, *"))) {
 			expectedValidityDurationDaysList.add(Long.valueOf(expectedValidityDurationDayOption));
 		}
 		log.info("Asserting that the the validity period for this product certificate ("+actualValidityDurationDays+") spans one of the expected number of days ("+productCertValidityDuration+")...");
 		Assert.assertContains(expectedValidityDurationDaysList, new Long(actualValidityDurationDays));
 	}
 	
 	@Test(	description="Make sure the entitlement cert contains all expected OIDs",
 			groups={"AcceptanceTests","blockedByBug-744259","blockedByBug-754426" },
 			dataProvider="getAllAvailableSubscriptionPoolsData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyEntitlementCertContainsExpectedOIDs_Test(SubscriptionPool pool) {
 //if (!pool.productId.equals("awesomeos-virt-4")) throw new SkipException("debugging...");
 
 //		// toggle this block of code with the dataProvider to process all pools or just one random pool
 //		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (List)null, true, false, null, null, null);
 //		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 //		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		
 		// subscribe to the pool and get the EntitlementCert
 		//File entitlementCertFile = clienttasks.subscribeToSubscriptionPool(pool);
 		//EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		//^ replaced with the following to save logging/assertion time
 		clienttasks.subscribe(null, null, pool.poolId, null, null, null, null, null, null, null, null);
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertCorrespondingToSubscribedPool(pool);
 		Assert.assertNotNull(entitlementCert,"Successfully retrieved the entitlement cert granted after subscribing to pool: "+pool);
 		
 		// Commented out the following log of rawCertificate to conserve logFile size
 		//log.info("Raw entitlement certificate: \n"+entitlementCert.rawCertificate);
 		boolean allMandatoryOIDsFound = true;
 		// Reference	https://docspace.corp.redhat.com/docs/DOC-30244
 		
 		// asserting all expected OIDS in the OrderNamespace
 		//		  1.3.6.1.4.1.2312.9.4.1 (Name): Red Hat Enterprise Linux Server
 		//		  1.3.6.1.4.1.2312.9.4.2 (Order Number) : ff8080812c3a2ba8012c3a2cbe63005b  
 		//		  1.3.6.1.4.1.2312.9.4.3 (SKU) : MCT0982
 		//		  1.3.6.1.4.1.2312.9.4.4 (Subscription Number) : abcd-ef12-1234-5678   <- SHOULD ONLY EXIST IF ORIGINATED FROM A REGTOKEN
 		//		  1.3.6.1.4.1.2312.9.4.5 (Quantity) : 100
 		//		  1.3.6.1.4.1.2312.9.4.6 (Entitlement Start Date) : 2010-10-25T04:00:00Z
 		//		  1.3.6.1.4.1.2312.9.4.7 (Entitlement End Date) : 2011-11-05T00:00:00Z
 		//		  1.3.6.1.4.1.2312.9.4.8 (Virtualization Limit) : 4						<- ONLY EXISTS WHEN VIRT POOLS ARE TO BE GENERATED
 		//		  1.3.6.1.4.1.2312.9.4.9 (Socket Limit) : None							<- ONLY EXISTS WHEN THE SUBSCRIPTION IS USED TO SATISFY HARDWARE
 		//		  1.3.6.1.4.1.2312.9.4.10 (Contract Number): 152341643
 		//		  1.3.6.1.4.1.2312.9.4.11 (Quantity Used): 4
 		//		  1.3.6.1.4.1.2312.9.4.12 (Warning Period): 30
 		//		  1.3.6.1.4.1.2312.9.4.13 (Account Number): 9876543210
 		//		  1.3.6.1.4.1.2312.9.4.14 (Provides Management): 0 (boolean, 1 for true)<- NEEDINFO
 		//		  1.3.6.1.4.1.2312.9.4.15 (Support Level): Premium						<- NEEDINFO
 		//		  1.3.6.1.4.1.2312.9.4.16 (Support Type): Level 3						<- NEEDINFO
 		//		  1.3.6.1.4.1.2312.9.4.17 (Stacking Id): 23456							<- ONLY EXISTS WHEN SUBSCRIPTION IS STACKABLE
 		//		  1.3.6.1.4.1.2312.9.4.18 (Virt Only): 1								<- ONLY EXISTS WHEN POOLS ARE INTENTED FOR VIRT MACHINES ONLY
 		OrderNamespace orderNamespace = entitlementCert.orderNamespace;
 		if (orderNamespace.productName!=null)			{Assert.assertNotNull(orderNamespace.productName,			"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.1 (Name) is present with value '"+ orderNamespace.productName+"'");}							else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.1 (Name) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.orderNumber!=null)			{Assert.assertNotNull(orderNamespace.orderNumber,			"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.2 (Order Number) is present with value '"+ orderNamespace.orderNumber+"'");}					else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.2 (Order Number) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.productId!=null)				{Assert.assertNotNull(orderNamespace.productId,				"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.3 (SKU) is present with value '"+ orderNamespace.productId+"'");}							else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.3 (SKU) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.subscriptionNumber!=null)	{Assert.assertNotNull(orderNamespace.subscriptionNumber,	"Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.4 (Subscription Number) is present with value '"+ orderNamespace.subscriptionNumber+"'");}	else {log.warning("Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.4 (Subscription Number) is missing"); /*allOIDSFound = false;*/}
 		if (orderNamespace.quantity!=null)				{Assert.assertNotNull(orderNamespace.quantity,				"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.5 (Quantity) is present with value '"+ orderNamespace.quantity+"'");}						else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.5 (Quantity) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.startDate!=null)				{Assert.assertNotNull(orderNamespace.startDate,				"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.6 (Entitlement Start Date) is present with value '"+ orderNamespace.startDate+"'");}			else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.6 (Entitlement Start Date) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.endDate!=null)				{Assert.assertNotNull(orderNamespace.endDate,				"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.7 (Entitlement End Date) is present with value '"+ orderNamespace.endDate+"'");}				else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.7 (Entitlement End Date) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.virtualizationLimit!=null)	{Assert.assertNotNull(orderNamespace.virtualizationLimit,	"Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.8 (Virtualization Limit) is present with value '"+ orderNamespace.virtualizationLimit+"'");}	else {log.warning("Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.8 (Virtualization Limit) is missing"); /*allOIDSFound = false;*/}
 		if (orderNamespace.socketLimit!=null)			{Assert.assertNotNull(orderNamespace.socketLimit,			"Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.9 (Socket Limit) is present with value '"+ orderNamespace.socketLimit+"'");}					else {log.warning("Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.9 (Socket Limit) is missing"); /*allOIDSFound = false;*/}
 		if (orderNamespace.contractNumber!=null)		{Assert.assertNotNull(orderNamespace.contractNumber,		"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.10 (Contract Number) is present with value '"+ orderNamespace.contractNumber+"'");}			else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.10 (Contract Number) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.quantityUsed!=null)			{Assert.assertNotNull(orderNamespace.quantityUsed,			"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.11 (Quantity Used) is present with value '"+ orderNamespace.quantityUsed+"'");}				else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.11 (Quantity Used) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.warningPeriod!=null)			{Assert.assertNotNull(orderNamespace.warningPeriod,			"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.12 (Warning Period) is present with value '"+ orderNamespace.warningPeriod+"'");}			else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.12 (Warning Period) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.accountNumber!=null)			{Assert.assertNotNull(orderNamespace.accountNumber,			"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.13 (Account Number) is present with value '"+ orderNamespace.accountNumber+"'");}			else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.13 (Account Number) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.providesManagement!=null)	{Assert.assertNotNull(orderNamespace.providesManagement,	"Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.14 (Provides Management) is present with value '"+ orderNamespace.providesManagement+"'");}	else {log.warning("Mandatory OrderNamespace OID 1.3.6.1.4.1.2312.9.4.14 (Provides Management) is missing"); allMandatoryOIDsFound = false;}
 		if (orderNamespace.supportLevel!=null)			{Assert.assertNotNull(orderNamespace.supportLevel,			"Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.15 (Support Level) is present with value '"+ orderNamespace.supportLevel+"'");}				else {log.warning("Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.15 (Support Level) is missing"); /*allOIDSFound = false;*/}
 		if (orderNamespace.supportType!=null)			{Assert.assertNotNull(orderNamespace.supportType,			"Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.16 (Support Type) is present with value '"+ orderNamespace.supportType+"'");}					else {log.warning("Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.16 (Support Type) is missing"); /*allOIDSFound = false;*/}
 		if (orderNamespace.stackingId!=null)			{Assert.assertNotNull(orderNamespace.stackingId,			"Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.17 (Stacking Id) is present with value '"+ orderNamespace.stackingId+"'");}					else {log.warning("Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.17 (Stacking Id) is missing"); /*allOIDSFound = false;*/}
 		if (orderNamespace.virtOnly!=null)				{Assert.assertNotNull(orderNamespace.virtOnly,				"Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.18 (Virt Only) is present with value '"+ orderNamespace.virtOnly+"'");}						else {log.warning("Optional OrderNamespace OID 1.3.6.1.4.1.2312.9.4.18 (Virt Only) is missing"); /*allOIDSFound = false;*/}
 		
 		for (ProductNamespace productNamespace : entitlementCert.productNamespaces) {
 			// asserting all expected OIDS in the ProductNamespace
 			//    1.3.6.1.4.1.2312.9.1.<product_hash>.1 (Name) : Red Hat Enterprise Linux
 			//    1.3.6.1.4.1.2312.9.1.<product_hash>.2 (Version) : 6.0
 			//    1.3.6.1.4.1.2312.9.1.<product_hash>.3 (Architecture) : x86_64
 			//    1.3.6.1.4.1.2312.9.1.<product_hash>.4 (Provides) : String1, String2, String3
 			if (productNamespace.name!=null)			{Assert.assertNotNull(productNamespace.name,			"Mandatory ProductNamespace OID 1.3.6.1.4.1.2312.9.1."+productNamespace.id+".1 (Name) is present with value '"+ productNamespace.name+"'");}			else {log.warning("Mandatory ProductNamespace OID 1.3.6.1.4.1.2312.9.1."+productNamespace.id+".1 (Name) is missing"); allMandatoryOIDsFound = false;}
 			if (productNamespace.version!=null)			{Assert.assertNotNull(productNamespace.version,			"Mandatory ProductNamespace OID 1.3.6.1.4.1.2312.9.1."+productNamespace.id+".2 (Version) is present with value '"+ productNamespace.version+"'");}		else {log.warning("Mandatory ProductNamespace OID 1.3.6.1.4.1.2312.9.1."+productNamespace.id+".2 (Version) is missing"); allMandatoryOIDsFound = false;}
 			if (productNamespace.arch!=null)			{Assert.assertNotNull(productNamespace.arch,			"Mandatory ProductNamespace OID 1.3.6.1.4.1.2312.9.1."+productNamespace.id+".3 (Architecture) is present with value '"+ productNamespace.arch+"'");}	else {log.warning("Mandatory ProductNamespace OID 1.3.6.1.4.1.2312.9.1."+productNamespace.id+".3 (Architecture) is missing"); allMandatoryOIDsFound = false;}
 			if (productNamespace.providedTags!=null)	{Assert.assertNotNull(productNamespace.providedTags,	"Optional ProductNamespace OID 1.3.6.1.4.1.2312.9.1."+productNamespace.id+".4 (Provides) is present with value '"+ productNamespace.providedTags+"'");}	else {log.warning("Optional ProductNamespace OID 1.3.6.1.4.1.2312.9.1."+productNamespace.id+".4 (Provides) is missing"); /*allOIDSFound = false;*/}
 		}
 		
 		for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 			if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 
 			// asserting all expected OIDS in the ContentNamespace	
 			//  1.3.6.1.4.1.2312.9.2.<content_hash>.1.1 (Name) : Red Hat Enterprise Linux (Supplementary)
 			//  1.3.6.1.4.1.2312.9.2.<content_hash>.1.2 (Label) : rhel-server-6-supplementary
 			//  1.3.6.1.4.1.2312.9.2.<content_hash>.1.3 (Physical Entitlements): 1
 			//  1.3.6.1.4.1.2312.9.2.<content_hash>.1.4 (Flex Guest Entitlements): 0
 			//  1.3.6.1.4.1.2312.9.2.<content_hash>.1.5 (Vendor ID): %Red_Hat_Id% or %Red_Hat_Label%
 			//  1.3.6.1.4.1.2312.9.2.<content_hash>.1.6 (Download URL): content/rhel-server-6-supplementary/$releasever/$basearch
 			//  1.3.6.1.4.1.2312.9.2.<content_hash>.1.7 (GPG Key URL): file:///etc/pki/rpm-gpg/RPM-GPG-KEY-redhat-release
 			//  1.3.6.1.4.1.2312.9.2.<content_hash>.1.8 (Enabled): 1
 			//  1.3.6.1.4.1.2312.9.2.<content_hash>.1.9 (Metadata Expire Seconds): 604800
 			//  1.3.6.1.4.1.2312.9.2.<content_hash>.1.10 (Required Tags): TAG1,TAG2,TAG3
 			if (contentNamespace.name!=null)					{Assert.assertNotNull(contentNamespace.name,					"Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.1 (Name) is present with value '"+ contentNamespace.name+"'");}									else {log.warning("Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.1 (Name) is missing"); allMandatoryOIDsFound = false;}
 			if (contentNamespace.label!=null)					{Assert.assertNotNull(contentNamespace.label,					"Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.2 (Label) is present with value '"+ contentNamespace.label+"'");}									else {log.warning("Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.2 (Label) is missing"); allMandatoryOIDsFound = false;}
 			//REMOVED						//if (contentNamespace.physicalEntitlement!=null)		{Assert.assertNotNull(contentNamespace.physicalEntitlement,		"Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.3 (Physical Entitlements) is present with value '"+ contentNamespace.physicalEntitlement+"'");}	else {log.warning("Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.3 (Physical Entitlements) is missing"); allOIDSFound = false;}
 			Assert.assertNull(contentNamespace.physicalEntitlement,		"ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.3 (Physical Entitlements) has been removed from the generation of entitlements.  Its current value is '"+ contentNamespace.physicalEntitlement+"'");
 			//REMOVED blockedByBug-754426	//if (contentNamespace.flexGuestEntitlement!=null)	{Assert.assertNotNull(contentNamespace.flexGuestEntitlement,	"Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.4 (Flex Guest Entitlements) is present with value '"+ contentNamespace.flexGuestEntitlement+"'");}	else {log.warning("Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.4 (Flex Guest Entitlements) is missing"); allOIDSFound = false;}
 			Assert.assertNull(contentNamespace.flexGuestEntitlement,	"ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.4 (Flex Guest Entitlements) has been removed from the generation of entitlements.  Its current value is '"+ contentNamespace.flexGuestEntitlement+"'");
 			if (contentNamespace.vendorId!=null)				{Assert.assertNotNull(contentNamespace.vendorId,				"Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.5 (Vendor ID) is present with value '"+ contentNamespace.vendorId+"'");}							else {log.warning("Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.5 (Vendor ID) is missing"); allMandatoryOIDsFound = false;}
 			if (contentNamespace.downloadUrl!=null)				{Assert.assertNotNull(contentNamespace.downloadUrl,				"Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.6 (Download URL) is present with value '"+ contentNamespace.downloadUrl+"'");}						else {log.warning("Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.6 (Download URL) is missing"); allMandatoryOIDsFound = false;}
 			if (contentNamespace.gpgKeyUrl!=null)				{Assert.assertNotNull(contentNamespace.gpgKeyUrl,				"Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.7 (GPG Key URL) is present with value '"+ contentNamespace.gpgKeyUrl+"'");}						else {log.warning("Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.7 (GPG Key URL) is missing"); allMandatoryOIDsFound = false;}
 			if (contentNamespace.enabled!=null)					{Assert.assertNotNull(contentNamespace.enabled,					"Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.8 (Enabled) is present with value '"+ contentNamespace.enabled+"'");}								else {log.warning("Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.8 (Enabled) is missing"); allMandatoryOIDsFound = false;}
 			if (contentNamespace.metadataExpire!=null)			{Assert.assertNotNull(contentNamespace.metadataExpire,			"Optional ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.9 (Metadata Expire Seconds) is present with value '"+ contentNamespace.metadataExpire+"'");}		else {log.warning("Mandatory ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.9 (Metadata Expire Seconds) is missing"); /*allMandatoryOIDsFound = false;*/}
 			if (contentNamespace.requiredTags!=null)			{Assert.assertNotNull(contentNamespace.requiredTags,			"Optional ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.10 (Required Tags) is present with value '"+ contentNamespace.requiredTags+"'");}					else {log.warning("Optional ContentNamespace OID 1.3.6.1.4.1.2312.9.2."+contentNamespace.hash+".1.10 (Required Tags) is missing"); /*allOIDSFound = false;*/}
 		}
 		
 		if (!allMandatoryOIDsFound) Assert.fail("Could not find all mandatory entitlement cert OIDs. (see warnings above)");
 	}
 	
 	// Candidates for an automated Test:
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=659735
 	// TODO Bug 805690 - gpgcheck for repo metadata is ignored 
	
 	
 	// Configuration methods ***********************************************************************
 	
 	
 	
 	// Protected methods ***********************************************************************
 
 
 	
 	// Data Providers ***********************************************************************
 	@DataProvider(name="getProductCertFilesData")
 	public Object[][] getProductCertFilesDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getProductCertFilesDataAsListOfLists());
 	}
 	protected List<List<Object>> getProductCertFilesDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		
 		if (clienttasks!=null) {
 			
 			// get all of the installed product certs
 			List<File> productCertFiles = clienttasks.getCurrentProductCertFiles();
 			if (productCertFiles.size()==0) log.warning("No Product Cert files were found.");
 			for (File productCertFile : productCertFiles) {
 				if (productCertFile.getName().endsWith("_.pem")) {
 					log.info("Skipping the generated product cert '"+productCertFile+"' from those provided by @DataProvider name=getProductCertFilesData.");
 					continue;
 				}
 				ll.add(Arrays.asList(new Object[]{productCertFile}));
 			}
 			
 			// get all of the product certs from the subscription-manager-migration-data
 			SSHCommandResult result = client.runCommandAndWait("rpm -ql subscription-manager-migration-data | grep .pem");
 			for (String productCertFilePath : result.getStdout().split("\n")) {
 				ll.add(Arrays.asList(new Object[]{new File(productCertFilePath)}));
 			}
 		}
 		
 		return ll;
 	}
 	
 }
