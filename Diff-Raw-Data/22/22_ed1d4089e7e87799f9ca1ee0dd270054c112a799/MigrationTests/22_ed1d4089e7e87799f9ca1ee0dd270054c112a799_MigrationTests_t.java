 package com.redhat.qe.sm.cli.tests;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.bugzilla.BlockedByBzBug;
 import com.redhat.qe.auto.bugzilla.BzChecker;
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.CandlepinType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 /**
  * @author jsefler
  *
  *	References:
  *		http://documentation-stage.bne.redhat.com/docs/en-US/Red_Hat_Enterprise_Linux/5/html/Deployment_Guide/rhn-migration.html
  *		https://engineering.redhat.com/trac/PBUPM/browser/trunk/documents/Releases/RHEL6/Variants/RHEL6-Variants.rst
  *		http://linuxczar.net/articles/rhel-installation-numbers
  *		https://docspace.corp.redhat.com/docs/DOC-71135 (PRODUCT CERTS)
  *		https://engineering.redhat.com/trac/rcm/wiki/Projects/CDNBaseline
  *
  *	// OLD LOCATION
  *	git clone git://git.app.eng.bos.redhat.com/rcm/rhn-definitions.git
  *  http://git.app.eng.bos.redhat.com/?p=rcm/rhn-definitions.git;a=tree
  *  
  *  git clone git://git.app.eng.bos.redhat.com/rcm/rcm-metadata.git
  *  http://git.app.eng.bos.redhat.com/?p=rcm/rcm-metadata.git;a=tree
  *  
  *  product 150 is at
  *  http://git.app.eng.bos.redhat.com/?p=rcm/rhn-definitions.git;a=tree;f=product_ids/rhev-3.0;hb=HEAD
  *
  */
 @Test(groups={"MigrationTests"})
 public class MigrationTests extends SubscriptionManagerCLITestScript {
 
 	// Test methods ***********************************************************************
 	
 	@Test(	description="Verify that the channel-cert-mapping.txt exists",
 			groups={"debugTest","AcceptanceTests"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyChannelCertMappingFileExists_Test() {
 		Assert.assertTrue(RemoteFileTasks.testExists(client, channelCertMappingFilename),"The expected channel cert mapping file '"+channelCertMappingFilename+"' exists.");
 	}
 	
 	
 	@Test(	description="Verify that the channel-cert-mapping.txt contains a unique map of channels to product certs",
 			groups={"debugTest","AcceptanceTests"},
 			dependsOnMethods={"VerifyChannelCertMappingFileExists_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyChannelCertMapping_Test() throws FileNotFoundException, IOException {
 		Assert.assertTrue(RemoteFileTasks.testExists(client, channelCertMappingFilename),"The expected channel cert mapping file '"+channelCertMappingFilename+"' exists.");
 		
 		// Read the channelCertMappingFilename as if they were properties (Warning! this will mask non-unique mappings)
 		// [root@jsefler-onprem-5client ~]# cat /usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt
 		// rhn-tools-rhel-x86_64-server-5-beta: none
 		// rhn-tools-rhel-x86_64-server-5: Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem
 		// rhn-tools-rhel-x86_64-client-5-beta: none
 		// rhn-tools-rhel-x86_64-client-5: Client-Client-x86_64-efe91c1c-78d7-4d19-b2fb-3c88cfc2da35-68.pem
 		SSHCommandResult result = client.runCommandAndWait("cat "+channelCertMappingFilename);
 		Properties p = new Properties();
 		p.load(new ByteArrayInputStream(result.getStdout().getBytes("UTF-8")));
 		for (Object key: p.keySet()){
 			// load the channelsToProductCertFilesMap
 			channelsToProductCertFilenamesMap.put((String)key, p.getProperty((String)(key)));
 			// load the mappedProductCertFiles
 			if (!channelsToProductCertFilenamesMap.get(key).equalsIgnoreCase("none"))
 				mappedProductCertFilenames.add(channelsToProductCertFilenamesMap.get(key));
 		}
 		
 		// Read the channelCertMappingFilename line by line asserting unique mappings
 		boolean uniqueChannelsToProductCertFilenamesMap = true;
 		for (String line: result.getStdout().trim().split("\\n")){
 			if (line.trim().equals("")) continue; // skip blank lines
 			if (line.trim().startsWith("#")) continue; // skip comments
 			String channel = line.split(":")[0].trim();
 			String productCertFilename = line.split(":")[1].trim();
 			if (channelsToProductCertFilenamesMap.containsKey(channel)) {
 				if (!channelsToProductCertFilenamesMap.get(channel).equals(productCertFilename)) {
 					log.warning("RHN Channel '"+channel+"' is already mapped to productFilename '"+productCertFilename+"' while parsing "+channelCertMappingFilename+" line: "+line);
 					uniqueChannelsToProductCertFilenamesMap = false;
 				}
 			} else {
 				Assert.fail("Having trouble parsing the following channel:product map from "+channelCertMappingFilename+": "+line);
 			}
 		}
 		Assert.assertTrue(uniqueChannelsToProductCertFilenamesMap, "Each channel in "+channelCertMappingFilename+" maps to a unique product cert filename. (See above warnings for offenders.)");
 	}
 	
 	
 	@Test(	description="Verify that all product cert files mapped in channel-cert-mapping.txt exist",
 			groups={"AcceptanceTests","blockedByBug-771615"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyAllMappedProductCertFilesExists_Test() {
 
 		boolean allMappedProductCertFilesExist = true;
 		for (String mappedProductCertFilename : mappedProductCertFilenames) {
 			String mappedProductCertFile = baseProductsDir+"/"+mappedProductCertFilename;
 			if (RemoteFileTasks.testExists(client, mappedProductCertFile)) {
 				log.info("Mapped productCert file '"+mappedProductCertFile+"' exists.");		
 			} else {
 				log.warning("Mapped productCert file '"+mappedProductCertFile+"' does NOT exist.");
 				allMappedProductCertFilesExist = false;
 			}
 		}
 		Assert.assertTrue(allMappedProductCertFilesExist,"All of the productCert files mapped in '"+channelCertMappingFilename+"' exist.");
 	}
 	
 	
 	@Test(	description="Verify that all existing product cert files are mapped in channel-cert-mapping.txt",
 			groups={"AcceptanceTests","blockedByBug-799103"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyAllExistingProductCertFilesAreMapped_Test() {
 		
 		// get a list of all the existing product cert files
 		SSHCommandResult result = client.runCommandAndWait("ls "+baseProductsDir+"/*.pem");
 		List<String> existingProductCertFiles = Arrays.asList(result.getStdout().split("\\n"));
 		boolean allExitingProductCertFilesAreMapped = true;
 		for (String existingProductCertFile : existingProductCertFiles) {
 			if (mappedProductCertFilenames.contains(new File(existingProductCertFile).getName())) {
 				log.info("Existing productCert file '"+existingProductCertFile+"' is mapped in '"+channelCertMappingFilename+"'.");
 			} else {
 				log.warning("Existing productCert file '"+existingProductCertFile+"' is NOT mapped in '"+channelCertMappingFilename+"'.");
 				
 				// TEMPORARY WORKAROUND FOR BUG
 				/* Notes: http://entitlement.etherpad.corp.redhat.com/Entitlement02MAY12 
 			    /product_ids/rhel-6.3/ComputeNode-ScalableFileSystem-x86_64-21b36280d242-175.pem  is not mapped to any RHN Channels in /cdn/product-baseline.json  (SEEMS  WRONG)
 			    (dgregor) channel won't exist until 6.3 GA.  suggest we pick this up in 6.4
 			    (jsefler) TODO update automated test with pre-6.3GA work-around
 			    /product_ids/rhel-6.3/Server-HPN-ppc64-fff6dded9725-173.pem  is not mapped to  any RHN Channels in /cdn/product-baseline.json   (SEEMS WRONG)
 			    (dgregor) channel won't exist until 6.3 GA.  suggest we pick this up in 6.4
 			    (jsefler) TODO update automated test with pre-6.3GA work-around
 			    */
 				if (existingProductCertFile.endsWith("-173.pem") && clienttasks.redhatReleaseXY.equals("6.3")) {
 					log.warning("Ignoring that existing productCert file '"+existingProductCertFile+"' is NOT mapped in '"+channelCertMappingFilename+"' until release 6.4 as recommended by dgregor.");
 				} else
 				if (existingProductCertFile.endsWith("-175.pem") && clienttasks.redhatReleaseXY.equals("6.3")) {
 					log.warning("Ignoring that existing productCert file '"+existingProductCertFile+"' is NOT mapped in '"+channelCertMappingFilename+"' until release 6.4 as recommended by dgregor.");
 				} else
 				// END OF WORKAROUND
 				allExitingProductCertFilesAreMapped = false;
 				
 			}
 		}
 		Assert.assertTrue(allExitingProductCertFilesAreMapped,"All of the existing productCert files in directory '"+baseProductsDir+"' are mapped to a channel in '"+channelCertMappingFilename+"'.");
 	}
 	
 	
 	@Test(	description="Verify that the migration product certs support this system's RHEL release version",
 			groups={"AcceptanceTests","blockedByBug-782208"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=130940)
 	public void VerifyMigrationProductCertsSupportThisSystemsRhelVersion_Test() {
 		
 		// process all the migration product cert files into ProductCerts and assert their version
 		boolean verifiedVersionOfAllMigrationProductCertFiles = true;
 		for (ProductCert productCert : clienttasks.getProductCerts(baseProductsDir)) {
 			if (!productCert.productNamespace.providedTags.toLowerCase().contains("rhel")) {
 				log.warning("Migration productCert '"+productCert+"' does not provide RHEL tags.  Skipping assertion that its version matches this system's RHEL version.");
 				continue;
 			}
 			if (productCert.productNamespace.version.equals(clienttasks.redhatReleaseXY)) {
 				log.info("Migration productCert '"+productCert+"' supports this version of RHEL '"+clienttasks.redhatReleaseXY+"'.");
 
 			} else {
 				log.warning("Migration productCert '"+productCert+"' does NOT support this version of RHEL '"+clienttasks.redhatReleaseXY+"'.");
 				verifiedVersionOfAllMigrationProductCertFiles = false;
 			}
 		}
 		Assert.assertTrue(verifiedVersionOfAllMigrationProductCertFiles,"All of the migration productCerts in directory '"+baseProductsDir+"' support this version of RHEL '"+clienttasks.redhatReleaseXY+"'.");
 	}
 	
 	
 	@Test(	description="Verify that the migration product certs match those from rhn definitions",
 			groups={"AcceptanceTests","blockedByBug-799152","blockedByBug-814360"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyMigrationProductCertsMatchThoseFromRhnDefinitions_Test() {
 		
 		// process all the migration product cert files into ProductCerts and assert they match those from the RHN Definitions
 
 		// get
 		List<ProductCert> rhnDefnitionProductCerts = new ArrayList<ProductCert>();
 		for (String rhnDefinitionsProductCertsDir : sm_rhnDefinitionsProductCertsDirs) {
 			String tmpRhnDefinitionsProductCertsDir = clienttasks.rhnDefinitionsDir+rhnDefinitionsProductCertsDir;
 			Assert.assertTrue(RemoteFileTasks.testExists(client, tmpRhnDefinitionsProductCertsDir),"The rhn definitions product certs dir '"+rhnDefinitionsProductCertsDir+"' has been locally cloned to '"+tmpRhnDefinitionsProductCertsDir+"'.");
 			rhnDefnitionProductCerts.addAll(clienttasks.getProductCerts(tmpRhnDefinitionsProductCertsDir));
 		}
 		List<ProductCert> migrationProductCerts = clienttasks.getProductCerts(baseProductsDir);
 
 		// test
 		boolean verifiedMatchForAllMigrationProductCertFiles = true;
 		for (ProductCert migrationProductCert : migrationProductCerts) {
 			if (rhnDefnitionProductCerts.contains(migrationProductCert)) {
 				log.info("Migration product cert '"+migrationProductCert.file+"' was found among the current rhn-definition.git product certs for this release.");
 			} else {
 				log.warning("Migration product cert '"+migrationProductCert.file+"' was NOT found among the current product certs in [rcm/rhn-definitions.git] "+sm_rhnDefinitionsProductCertsDirs+" for this release.  It may have been re-generated by release engineering.");
 				verifiedMatchForAllMigrationProductCertFiles = false;
 			}
 		}
 		for (ProductCert rhnDefinitionProductCert : rhnDefnitionProductCerts) {
 			if (migrationProductCerts.contains(rhnDefinitionProductCert)) {
 				//log.info("Product cert [rcm/rhn-definitions.git] "+rhnDefinitionProductCert.file.getName()+" was found among the current migration product certs for this release.");
 			} else {
 				log.warning("Product cert [rcm/rhn-definitions.git] "+rhnDefinitionProductCert.file.getPath().replaceFirst(clienttasks.rhnDefinitionsDir, "")+" was NOT found among the current migration product certs for this release.  It is either a new product cert generated by release engineering, or it is not mapped to an RHN Channel in '"+sm_rhnDefinitionsProductBaselineFile+"' for this release.");
 				// FIXME? THIS WARNING MAY ACTUALLY BE A FAILURE IN /cdn/product-baseline.json, NOT A FAILURE IN MIGRATION DATA.   IF YES, THEN COMMENT OUT NEXT LINE.  It is safest to set verifiedMatchForAllMigrationProductCertFiles = false
 				verifiedMatchForAllMigrationProductCertFiles = false;
 			}
 		}
 		Assert.assertTrue(verifiedMatchForAllMigrationProductCertFiles,"All of the migration productCerts in directory '"+baseProductsDir+"' match the current rhn-definition.git product certs for this release.");
 	}
 	
 	
 	@Test(	description="Verify that all of the required RHN Channels in the ProductBaseline file are accounted for in channel-cert-mapping.txt",
 			groups={},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="RhnChannelFromProductBaselineData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyChannelCertMappingFileSupportsRhnChannelFromProductBaseline_Test(Object bugzilla, String productBaselineRhnChannel, String productBaselineProductId, String productBaselineProductName) throws JSONException {
 		
 		// does the cdn indicate that this channel maps to more than one product?
 		if (cdnProductBaselineChannelMap.get(productBaselineRhnChannel).size()>1) {
 			log.warning("According to the CDN Product Baseline, RHN Channel '"+productBaselineRhnChannel+"' maps to more than one product id: "+cdnProductBaselineChannelMap.get(productBaselineRhnChannel));
 			// handle special cases to decide what productId should be mapped (see bug https://bugzilla.redhat.com/show_bug.cgi?id=786257)
 			if (Arrays.asList("rhel-x86_64-client-supplementary-5","rhel-x86_64-client-5","rhel-i386-client-supplementary-5","rhel-i386-client-5").contains(productBaselineRhnChannel)) {
 				if (cdnProductBaselineChannelMap.get(productBaselineRhnChannel).contains("68") && !productBaselineProductId.equals("68")) {
 					throw new SkipException("According to https://bugzilla.redhat.com/show_bug.cgi?id=786257#c1, channel '"+productBaselineProductId+"' is a special case on RHEL5 and the subscription-manager-migration-data file '"+channelCertMappingFilename+"' should only map to productId 68.");
 				}
 			// placeholder for next special case
 			} else if (false) {
 				
 			} else {
 				Assert.fail("Do not know how to choose which productId is mapped to channel '"+productBaselineRhnChannel+"' in the subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			}
 		}
 		
 		// Special case for High Touch Beta productId 135  reference: https://bugzilla.redhat.com/show_bug.cgi?id=799152#c4
 		if (productBaselineProductId.equals("135")) {
 			log.warning("For product id 135 which represents '"+productBaselineProductName+"' we actually do NOT want a channel cert mapping as instructed in https://bugzilla.redhat.com/show_bug.cgi?id=799152#c4");
 			Assert.assertTrue(!channelsToProductCertFilenamesMap.containsKey(productBaselineRhnChannel),
 					"CDN Product Baseline RHN Channel '"+productBaselineRhnChannel+"' supporting productId="+productBaselineProductId+" productName="+productBaselineProductName+" was NOT found in the subscription-manager-migration-data file '"+channelCertMappingFilename+"'.  This is a special case (Bugzilla 799152#c4).");
 			return;
 		}
 		
 		// assert that the subscription-manager-migration-data file has a mapping for this RHN Channel found in the CDN Product Baseline
 		Assert.assertTrue(channelsToProductCertFilenamesMap.containsKey(productBaselineRhnChannel),
 				"CDN Product Baseline RHN Channel '"+productBaselineRhnChannel+"' supporting productId="+productBaselineProductId+" productName="+productBaselineProductName+" was found in the subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 		
 		// now assert that the subscription-manager-migration-data mapping for the RHN Channel is to the same productId as mapped in the CDN Product Baseline
 		Assert.assertEquals(getProductIdFromProductCertFilename(channelsToProductCertFilenamesMap.get(productBaselineRhnChannel)), productBaselineProductId,
 				"The subscription-manager-migration-data file '"+channelCertMappingFilename+"' maps RHN Channel '"+productBaselineRhnChannel+"' to the same productId as dictated in the CDN Product Baseline.");
 	}
 	
 	
 	@Test(	description="Verify that all of the classic RHN Channels available to a classically registered consumer are accounted for in the in the channel-cert-mapping.txt or is a known exception",
 			groups={"debugTest","AcceptanceTests"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="getRhnClassicBaseAndAvailableChildChannelsData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyChannelCertMappingFileSupportsRhnClassicBaseAndAvailableChildChannel_Test(Object bugzilla, String classicRhnChannel) {
 		
 		// SPECIAL CASES.....
 		
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-cf-ae-1' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-cf-ae-1-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-cf-ae-1-beta-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-cf-ae-1-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// (degregor 5/4/2012) CloudForms Application Engine isn't shipping anytime soon, so we decided to remove the CDN repos.  While the channels are there in RHN, no one has access to them.
 		if (classicRhnChannel.matches("rhel-.+-6-cf-ae-1(-.*|$)")) {
 			log.warning("(degregor 5/4/2012) CloudForms Application Engine isn't shipping anytime soon, so we decided to remove the CDN repos.  While the channels are there in RHN, no one has access to them.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-htb' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-htb-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.827 - WARNING: RHN Classic channel 'sam-rhel-x86_64-server-6-htb' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.828 - WARNING: RHN Classic channel 'sam-rhel-x86_64-server-6-htb-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// (degregor 5/4/2012) We intentionally exclude HTB channels from the migration script.  It's not a supported use case.
 		if (classicRhnChannel.matches(".+-htb(-.*|$)")) {
 			log.warning("(degregor 5/4/2012) We intentionally exclude HTB channels from the migration script.  It's not a supported use case.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		// 201205032049:22.819 - WARNING: RHN Classic channel 'rhel-x86_64-server-clusteredstorage-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.820 - WARNING: RHN Classic channel 'rhel-x86_64-server-ei-replication-6' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.820 - WARNING: RHN Classic channel 'rhel-x86_64-server-ei-replication-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.820 - WARNING: RHN Classic channel 'rhel-x86_64-server-ei-replication-6-beta-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.820 - WARNING: RHN Classic channel 'rhel-x86_64-server-ei-replication-6-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'. 
 		// (degregor 5/4/2012) The above channels aren't used.
 		if (classicRhnChannel.matches("rhel-.+-ei-replication-6(-.*|$)")  || classicRhnChannel.matches("rhel-.+-clusteredstorage-6(-.*|$)")) {
 			log.warning("(degregor 5/4/2012) The above channels aren't used.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		// 201205032049:22.827 - WARNING: RHN Classic channel 'rhn-tools-rhel-x86_64-server-6' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.827 - WARNING: RHN Classic channel 'rhn-tools-rhel-x86_64-server-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.827 - WARNING: RHN Classic channel 'rhn-tools-rhel-x86_64-server-6-beta-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.827 - WARNING: RHN Classic channel 'rhn-tools-rhel-x86_64-server-6-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// (degregor 5/4/2012) RHN Tools content doesn't get delivered through CDN.
 		if (classicRhnChannel.startsWith("rhn-tools-rhel-")) {
 			log.warning("(degregor 5/4/2012) RHN Tools content doesn't get delivered through CDN.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		// 201205080442:43.007 - WARNING: RHN Classic channel 'rhel-x86_64-server-highavailability-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205080442:43.008 - WARNING: RHN Classic channel 'rhel-x86_64-server-largefilesystem-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205080442:43.010 - WARNING: RHN Classic channel 'rhel-x86_64-server-loadbalance-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// (degregor 5/8/2012) These channels are not used and can be ignored.
 		if (classicRhnChannel.matches("rhel-.+-highavailability-6-beta") || classicRhnChannel.matches("rhel-.+-largefilesystem-6-beta") || classicRhnChannel.matches("rhel-.+-loadbalance-6-beta")) {
 			log.warning("(degregor 5/8/2012) These channels are not used and can be ignored.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		// 201205080556:10.326 - WARNING: RHN Classic channel 'rhel-x86_64-server-hts-6' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205080556:10.326 - WARNING: RHN Classic channel 'rhel-x86_64-server-hts-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		//	RHN Classic channel 'rhel-x86_64-server-hts-5' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt'.
 		//	RHN Classic channel 'rhel-x86_64-server-hts-5-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt'.
 		//	RHN Classic channel 'rhel-x86_64-server-hts-5-beta-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt'.
 		//	RHN Classic channel 'rhel-x86_64-server-hts-5-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt'.
 		// (degregor 5/8/2012) We're not delivering Hardware Certification (aka hts) bits through the CDN at this point.
 		if (classicRhnChannel.matches("rhel-.+-hts-"+clienttasks.redhatReleaseX+"(-.*|$)")) {
 			log.warning("(degregor 5/8/2012) We're not delivering Hardware Certification (aka hts) bits through the CDN at this point.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		
 		if (classicRhnChannel.matches("rhel-.+-server-5-mrg-.*")) {	// rhel-x86_64-server-5-mrg-grid-1 rhel-x86_64-server-5-mrg-grid-1-beta rhel-x86_64-server-5-mrg-grid-2 rhel-x86_64-server-5-mrg-grid-execute-1 rhel-x86_64-server-5-mrg-grid-execute-1-beta rhel-x86_64-server-5-mrg-grid-execute-2 etc.
 			// Bug 840102 - channels for rhel-<ARCH>-server-5-mrg-* are not yet mapped to product certs in rcm/rcm-metadata.git
			log.warning("(degregor 8/4/2012) RHEL 5 MRG isn't currently supported in CDN (outside of RHUI) - https://bugzilla.redhat.com/show_bug.cgi?id=840102#c1");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		if (classicRhnChannel.matches("rhel-.+-server-hpc-5(-.*|$)")) {	// rhel-x86_64-server-hpc-5-beta
 			// Bug 840103 - channel for rhel-x86_64-server-hpc-5-beta is not yet mapped to product cert in rcm/rcm-metadata.git
			log.warning("(degregor 8/4/2012) The RHEL 5 HPC products is not currently supported in CDN - https://bugzilla.redhat.com/show_bug.cgi?id=840103#c1");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		if (classicRhnChannel.matches("rhel-.+-server-rhev-hdk-2-5(-.+|$)")) {	// rhel-x86_64-server-rhev-hdk-2-5 rhel-x86_64-server-rhev-hdk-2-5-beta
 			// Bug 840108 - channels for rhel-<ARCH>-rhev-hdk-2-5-* are not yet mapped to product certs in rcm/rhn-definitions.git
			log.warning("(degregor 8/4/2012) RHEV H Dev Kit is not currently supported in CDN - https://bugzilla.redhat.com/show_bug.cgi?id=840108#c1");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		if (classicRhnChannel.matches("rhel-.+-server-productivity-5-beta(-.+|$)")) {	// rhel-x86_64-server-productivity-5-beta rhel-x86_64-server-productivity-5-beta-debuginfo
 			// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		if (classicRhnChannel.matches("rhel-.+-server-rhsclient-5(-.+|$)")) {	// rhel-x86_64-server-rhsclient-5 rhel-x86_64-server-rhsclient-5-debuginfo
 			// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		if (classicRhnChannel.matches("rhel-.+-server-xfs-5(-.+|$)")) {	// rhel-x86_64-server-xfs-5 rhel-x86_64-server-xfs-5-beta
 			// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		if (classicRhnChannel.matches("rhel-.+-server-5-shadow(-.+|$)")) {	// rhel-x86_64-server-5-shadow-debuginfo
 			// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		if (classicRhnChannel.startsWith("rhx-")) {	// rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5 rhx-amanda-enterprise-backup-2.6-rhel-x86_64-server-5 etcetera
 			// Bug 840111 - various rhx channels are not yet mapped to product certs in rcm/rcm-metadata.git 
			log.warning("(degregor 8/4/2012) RHX products are not currently supported in CDN - https://bugzilla.redhat.com/show_bug.cgi?id=840111#c2");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		Assert.assertTrue(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 	}
 	
 	
 	
 	// install-num-migrate-to-rhsm Test methods ***********************************************************************
 	
 	@Test(	description="Execute migration tool install-num-migrate-to-rhsm with a known instnumber and assert the expected productCerts are copied",
 			groups={"AcceptanceTests","InstallNumMigrateToRhsmWithInstNumber_Test"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="InstallNumMigrateToRhsmData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=131567)
 	//@ImplementsNitrateTest(caseId=130760)
 	//@ImplementsNitrateTest(caseId=130758)
 	public void InstallNumMigrateToRhsmWithInstNumber_Test(Object bugzilla, String instNumber) throws JSONException {
 		InstallNumMigrateToRhsmWithInstNumber(instNumber);
 	}
 	protected SSHCommandResult InstallNumMigrateToRhsmWithInstNumber(String instNumber) throws JSONException {
 		if (!clienttasks.redhatReleaseX.equals("5")) throw new SkipException("This test is applicable to RHEL5 only.");
 		String command;
 		SSHCommandResult result;
 		
 		// deleting the currently installed product certs
 		clienttasks.removeAllCerts(false, false, true);
 		clienttasks.removeAllFacts();
 		
 		// get the product cert filenames that we should expect install-num-migrate-to-rhsm to copy
 		List<String> expectedMigrationProductCertFilenames = getExpectedMappedProductCertFilenamesCorrespondingToInstnumberUsingInstnumTool(instNumber);
 
 		// test --dryrun --instnumber ................................................
 		log.info("Testing with the dryrun option...");
 		command = installNumTool+" --dryrun --instnumber="+instNumber;
 		result = RemoteFileTasks.runCommandAndAssert(client,command,0);
 		//[root@jsefler-onprem-5server ~]# install-num-migrate-to-rhsm --dryrun --instnumber 0000000e0017fc01
 		//Copying /usr/share/rhsm/product/RHEL-5/Client-Workstation-x86_64-f812997e0eda-71.pem to /etc/pki/product/71.pem
 		//Copying /usr/share/rhsm/product/RHEL-5/Client-Client-x86_64-6587edcf1c03-68.pem to /etc/pki/product/68.pem
 
 		// assert the dryrun
 		for (String expectedMigrationProductCertFilename : expectedMigrationProductCertFilenames) {
 			String pemFilename = getPemFileNameFromProductCertFilename(expectedMigrationProductCertFilename);
 			String expectedStdoutString = "Copying "+baseProductsDir+"/"+expectedMigrationProductCertFilename+" to "+clienttasks.productCertDir+"/"+pemFilename;
 			Assert.assertTrue(result.getStdout().contains(expectedStdoutString),"The dryrun output from "+installNumTool+" contains the expected message: "+expectedStdoutString);
 		}
 		int numProductCertFilenamesToBeCopied=0;
 		for (int fromIndex=0; result.getStdout().indexOf("Copying", fromIndex)>=0&&fromIndex>-1; fromIndex=result.getStdout().indexOf("Copying", fromIndex+1)) numProductCertFilenamesToBeCopied++;	
 		Assert.assertEquals(numProductCertFilenamesToBeCopied, expectedMigrationProductCertFilenames.size(),"The number of product certs to be copied.");
 		Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), 0, "A dryrun should NOT install any product certs.");
 		// TEMPORARY WORKAROUND FOR BUG
 		String bugId = "783278"; boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		String bugPkg = "subscription-manager-migration";
 		String bugVer = "subscription-manager-migration-0.98";	// RHEL58
 		try {if (clienttasks.installedPackageVersion.get(bugPkg).contains(bugVer) && !invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+" which has NOT been fixed in this installed version of "+bugVer+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")"); invokeWorkaroundWhileBugIsOpen=true;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			log.warning("Skipping the assertion of the fact '"+migrationFromFact+"' fact.");
 		} else
 		// END OF WORKAROUND
 		Assert.assertNull(clienttasks.getFactValue(migrationFromFact), "The migration fact '"+migrationFromFact+"' should NOT be set after running command: "+command);
 		Assert.assertNull(clienttasks.getFactValue(migrationSystemIdFact), "The migration fact '"+migrationSystemIdFact+"' should NOT be set after running command: "+command);
 
 		
 		// test --instnumber ................................................
 		log.info("Testing without the dryrun option...");
 		command = installNumTool+" --instnumber="+instNumber;
 		result = RemoteFileTasks.runCommandAndAssert(client,command,0);
 		//[root@jsefler-onprem-5server ~]# install-num-migrate-to-rhsm --instnumber 0000000e0017fc01
 		//Copying /usr/share/rhsm/product/RHEL-5/Client-Workstation-x86_64-f812997e0eda-71.pem to /etc/pki/product/71.pem
 		//Copying /usr/share/rhsm/product/RHEL-5/Client-Client-x86_64-6587edcf1c03-68.pem to /etc/pki/product/68.pem
 		List<ProductCert> migratedProductCerts = clienttasks.getCurrentProductCerts();
 		Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), expectedMigrationProductCertFilenames.size(), "The number of productCerts installed after running migration command: "+command);
 		for (String expectedMigrationProductCertFilename : expectedMigrationProductCertFilenames) {
 			ProductCert expectedMigrationProductCert = clienttasks.getProductCertFromProductCertFile(new File(baseProductsDir+"/"+expectedMigrationProductCertFilename));
 			Assert.assertTrue(migratedProductCerts.contains(expectedMigrationProductCert),"The newly installed product certs includes the expected migration productCert: "+expectedMigrationProductCert);
 		}
 		Assert.assertEquals(clienttasks.getFactValue(migrationFromFact), "install_number", "The migration fact '"+migrationFromFact+"' should be set after running command: "+command);
 		Assert.assertNull(clienttasks.getFactValue(migrationSystemIdFact), "The migration fact '"+migrationSystemIdFact+"' should NOT be set after running command: "+command);
 
 		return result;
 	}
 	
 	
 	@Test(	description="Execute migration tool install-num-migrate-to-rhsm with install-num used to provision this machine",
 			groups={"AcceptanceTests","InstallNumMigrateToRhsm_Test"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=130760)
 	public void InstallNumMigrateToRhsm_Test() throws JSONException {
 		if (!clienttasks.redhatReleaseX.equals("5")) throw new SkipException("This test is applicable to RHEL5 only.");
 		if (!RemoteFileTasks.testExists(client, machineInstNumberFile) &&
 			RemoteFileTasks.testExists(client, backupMachineInstNumberFile)	) {
 			log.info("Restoring backup of the rhn install-num file...");
 			client.runCommandAndWait("mv -f "+backupMachineInstNumberFile+" "+machineInstNumberFile);
 		}
 		if (!RemoteFileTasks.testExists(client, machineInstNumberFile)) throw new SkipException("This system was NOT provisioned with an install number.");
 		
 		// get the install number used to provision this machine
 		SSHCommandResult result = client.runCommandAndWait("cat "+machineInstNumberFile);
 		String installNumber = result.getStdout().trim();
 		
 		// test this install number explicitly (specifying --instnumber option)
 		SSHCommandResult explicitResult = InstallNumMigrateToRhsmWithInstNumber(installNumber);
 		
 		// now test this install number implicitly (without specifying any options)
 		clienttasks.removeAllCerts(false, false, true);
 		clienttasks.removeAllFacts();
 		SSHCommandResult implicitResult = client.runCommandAndWait(installNumTool);
 		// compare implicit to explicit results for verification
 		Assert.assertEquals(implicitResult.getStdout().trim(), explicitResult.getStdout().trim(), "Stdout from running :"+installNumTool);
 		Assert.assertEquals(implicitResult.getStderr().trim(), explicitResult.getStderr().trim(), "Stderr from running :"+installNumTool);
 		Assert.assertEquals(implicitResult.getExitCode(), explicitResult.getExitCode(), "ExitCode from running :"+installNumTool);
 		Assert.assertEquals(clienttasks.getFactValue(migrationFromFact), "install_number", "The migration fact '"+migrationFromFact+"' should be set after running command: "+installNumTool);
 		Assert.assertNull(clienttasks.getFactValue(migrationSystemIdFact), "The migration fact '"+migrationSystemIdFact+"' should NOT be set after running command: "+installNumTool);
 		
 		// assert that the migrated product certs provide (at least) the same product tags as originally installed with the install number
 		List<ProductCert> migratedProductCerts = clienttasks.getCurrentProductCerts();
 		log.info("The following productCerts were originally installed on this machine prior to this migration test:");
 		for (ProductCert originalProductCert : originallyInstalledRedHatProductCerts) log.info(originalProductCert.toString());
 		log.info("The following productCerts were migrated to the product install directory after running the migration test:");
 		for (ProductCert migratedProductCert : migratedProductCerts) log.info(migratedProductCert.toString());
 		log.info("Will now verify that all of the productTags from the originally installed productCerts are found among the providedTags of the migrated productCerts...");
 		for (ProductCert originalProductCert : originallyInstalledRedHatProductCerts) {
 			if (originalProductCert.productNamespace.providedTags==null) continue;
 			List<String> originalProvidedTags = Arrays.asList(originalProductCert.productNamespace.providedTags.trim().split(" *, *"));
 			for (ProductCert migratedProductCert : migratedProductCerts) {
 				List<String> migratedProvidedTags = Arrays.asList(migratedProductCert.productNamespace.providedTags.trim().split(" *, *"));
 				if (migratedProvidedTags.containsAll(originalProvidedTags)) {
 					Assert.assertTrue(true,"This migrated productCert provides all the same tags as one of the originally installed product certs.\nMigrated productCert: "+migratedProductCert);
 					originalProvidedTags = new ArrayList<String>();//originalProvidedTags.clear();
 					break;
 				}
 			}
 			if (!originalProvidedTags.isEmpty()) {
 				Assert.fail("Failed to find the providedTags from this originally installed productCert among the migrated productCerts.\nOriginal productCert: "+originalProductCert);
 			}
 		}
 	}
 	
 	
 	@Test(	description="Execute migration tool install-num-migrate-to-rhsm with a non-default rhsm.productcertdir configured",
 			groups={"blockedByBug-773707","InstallNumMigrateToRhsmWithNonDefaultProductCertDir_Test"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="InstallNumMigrateToRhsmData",
 			enabled=true)
 	public void InstallNumMigrateToRhsmWithNonDefaultProductCertDir_Test(Object bugzilla, String instNumber) throws JSONException {
 		if (!clienttasks.redhatReleaseX.equals("5")) throw new SkipException("This test is applicable to RHEL5 only.");
 		
 		// TEMPORARY WORKAROUND FOR BUG
 		String bugId = "773707"; boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		String bugPkg = "subscription-manager-migration";
 		String bugVer = "subscription-manager-migration-0.98";	// RHEL58
 		try {if (clienttasks.installedPackageVersion.get(bugPkg).contains(bugVer) && !invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+" which has NOT been fixed in this installed version of "+bugVer+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")"); invokeWorkaroundWhileBugIsOpen=true;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			throw new SkipException("There is no workaround for this installed version of "+bugVer+".  Blocked by Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");
 		}
 		// END OF WORKAROUND
 
 		
 		// NOTE: The configNonDefaultRhsmProductCertDir will handle the configuration setting
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm", "productCertDir"), nonDefaultProductCertDir,"A non-default rhsm.productCertDir has been configured.");
 		InstallNumMigrateToRhsmWithInstNumber_Test(bugzilla,instNumber);
 	}
 	
 	
 	@Test(	description="Execute migration tool install-num-migrate-to-rhsm with a bad length install-num (expecting 16 chars long)",
 			groups={},
 			dependsOnMethods={},
 			dataProvider="InstallNumMigrateToRhsmWithInvalidInstNumberData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=130760)
 	public void InstallNumMigrateToRhsmWithInvalidInstNumber_Test(Object bugzilla, String command, Integer expectedExitCode, String expectedStdout, String expectedStderr) {
 		if (!clienttasks.redhatReleaseX.equals("5")) throw new SkipException("This test is applicable to RHEL5 only.");
 		SSHCommandResult result = client.runCommandAndWait(command);
 		if (expectedStdout!=null) Assert.assertEquals(result.getStdout().trim(), expectedStdout, "Stdout from running :"+command);
 		if (expectedStderr!=null) Assert.assertEquals(result.getStderr().trim(), expectedStderr, "Stderr from running :"+command);
 		// TEMPORARY WORKAROUND FOR BUG
 		String bugId="783542"; boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		String bugPkg = "subscription-manager-migration";
 		String bugVer = "subscription-manager-migration-0.98";	// RHEL58
 		try {if (clienttasks.installedPackageVersion.get(bugPkg).contains(bugVer) && !invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+" which has NOT been fixed in this installed version of "+bugVer+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")"); invokeWorkaroundWhileBugIsOpen=true;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			log.warning("Skipping the exitCode assertion from running: "+command);
 		} else
 		// END OF WORKAROUND
 		if (expectedExitCode!=null) Assert.assertEquals(result.getExitCode(), expectedExitCode, "ExitCode from running :"+command);
 	}
 
 
 	@Test(	description="Execute migration tool install-num-migrate-to-rhsm with no install-num found on machine",
 			groups={},
 			dependsOnMethods={},
 			enabled=true)
 	public void InstallNumMigrateToRhsmWithMissingInstNumber_Test() {
 		if (!clienttasks.redhatReleaseX.equals("5")) throw new SkipException("This test is applicable to RHEL5 only.");
 		if (RemoteFileTasks.testExists(client, machineInstNumberFile)) {
 			log.info("Backing up the rhn install-num file...");
 			client.runCommandAndWait("mv -f "+machineInstNumberFile+" "+backupMachineInstNumberFile);
 		}
 		InstallNumMigrateToRhsmWithInvalidInstNumber_Test(null, installNumTool,1,"Could not read installation number from "+machineInstNumberFile+".  Aborting.","");
 	}
 	
 	
 	@Test(	description="Assert that install-num-migrate-to-rhsm is only installed on RHEL5",
 			groups={"blockedByBug-790205"},
 			dependsOnMethods={},
 			enabled=true)
 	public void InstallNumMigrateToRhsmShouldOnlyBeInstalledOnRHEL5_Test() {
 		// make sure subscription-manager-migration is installed on RHEL5
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client, "rpm -ql "+clienttasks.command+"-migration", 0);
 		Assert.assertEquals(result.getStdout().contains(installNumTool), clienttasks.redhatReleaseX.equals("5"), "The "+clienttasks.command+"-migration package should only provide '"+installNumTool+"' on RHEL5.");
 	}
 	
 	
 	
 	// rhn-migrate-classic-to-rhsm Test methods ***********************************************************************
 	
 	@Test(	description="With a proxy configured in rhn/up2date, register system using RHN Classic and then Execute migration tool rhn-migrate-classic-to-rhsm with options after adding RHN Channels",
 			groups={"AcceptanceTests","RhnMigrateClassicToRhsm_Test","RhnMigrateClassicToRhsmUsingProxyServer_Test","blockedbyBug-798015"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="RhnMigrateClassicToRhsmUsingProxyServerData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=130763)
 	public void RhnMigrateClassicToRhsmUsingProxyServer_Test(Object bugzilla, String rhnUsername, String rhnPassword, String rhnHostname, List<String> rhnChannelsToAdd, String options, List<String> expectedMigrationProductCertFilenames, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		if (!sm_serverType.equals(CandlepinType.hosted)) throw new SkipException("The configured candlepin server type ("+sm_serverType+") is not '"+CandlepinType.hosted+"'.  This test requires access registration access to RHN Classic.");
 		if (sm_rhnHostname.equals("")) throw new SkipException("This test requires access to RHN Classic.");
 		if (options.contains("-n")) log.info("Executing "+rhnMigrateTool+" --no-auto should effectively unregister your system from RHN Classic without registering to RHSM.");
 
 		// make sure we are NOT registered to RHSM
 		clienttasks.unregister_(null,null,null);
 		clienttasks.removeAllCerts(true, true, true);
 		clienttasks.removeAllFacts();
 		
 //		// reset all of the proxy server configuration (RHN and RHSM)
 //		removeProxyServerConfigurations();
 		// remove proxy settings from up2date
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxy", "0");		// enableProxyAuth[comment]=To use an authenticated proxy or not
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "httpProxy", "");			// httpProxy[comment]=HTTP proxy in host:port format, e.g. squid.redhat.com:3128
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth", "0");	// enableProxyAuth[comment]=To use an authenticated proxy or not
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyUser", "");			// proxyUser[comment]=The username for an authenticated proxy
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyPassword", "");		// proxyPassword[comment]=The password to use for an authenticated proxy
 		iptablesAcceptPort(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "port"));
 
 
 		// enable/set proxy settings for RHN up2date
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxy",		"1");											// enableProxyAuth[comment]=To use an authenticated proxy or not
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "httpProxy",		proxy_hostnameConfig+":"+proxy_portConfig);		// httpProxy[comment]=HTTP proxy in host:port format, e.g. squid.redhat.com:3128
 		if (proxy_userConfig.equals("") && proxy_passwordConfig.equals("")) {
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth", "0");	// enableProxyAuth[comment]=To use an authenticated proxy or not
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyUser",		"disabled-proxy-user");								// proxyUser[comment]=The username for an authenticated proxy
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyPassword",	"disabled-proxy-password");							// proxyPassword[comment]=The password to use for an authenticated proxy
 		} else {
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth", "1");	// enableProxyAuth[comment]=To use an authenticated proxy or not
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyUser",		proxy_userConfig);								// proxyUser[comment]=The username for an authenticated proxy
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyPassword",	proxy_passwordConfig);							// proxyPassword[comment]=The password to use for an authenticated proxy
 		}
 		
 		// mark the tail of proxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing RhnMigrateClassicToRhsmUsingProxyServer_Test.registerToRhnClassic from "+clienttasks.hostname+"...";
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 
 		// register to RHN Classic
 		String rhnSystemId = registerToRhnClassic(rhnUsername, rhnPassword, rhnHostname);
 		
 		// assert that traffic to RHN is went through the proxy
 		String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 		Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to RHN.");
 		
 		// subscribe to more RHN Classic channels
 		if (rhnChannelsToAdd.size()>0) addRhnClassicChannels(rhnUsername, rhnPassword, rhnChannelsToAdd);
 		
 		// get a list of the consumed RHN Classic channels
 		List<String> rhnChannelsConsumed = getCurrentRhnClassicChannels();
 		if (rhnChannelsToAdd.size()>0) Assert.assertTrue(rhnChannelsConsumed.containsAll(rhnChannelsToAdd), "All of the RHN Classic channels added appear to be consumed.");
 
 		// get the product cert filenames that we should expect rhn-migrate-classic-to-rhsm to copy (or use the ones supplied to the @Test)
 		if (expectedMigrationProductCertFilenames==null) expectedMigrationProductCertFilenames = getExpectedMappedProductCertFilenamesCorrespondingToChannels(rhnChannelsConsumed);
 		
 		// reject traffic through the server.port
 		iptablesRejectPort(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "port"));
 
 		// mark the tail of proxyLog with a message
 		proxyLogMarker = System.currentTimeMillis()+" Testing RhnMigrateClassicToRhsmUsingProxyServer_Test.executeRhnMigrateClassicToRhsmWithOptions from "+clienttasks.hostname+"...";
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 
 		// execute rhn-migrate-classic-to-rhsm with options
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsmWithOptions(rhnUsername,rhnPassword,null,options);
 		
 		// assert the exit code
 		String expectedMsg;
 		if (!areAllChannelsMapped(rhnChannelsConsumed) && !options.contains("-f")/*--force*/) {	// when not all of the rhnChannelsConsumed have been mapped to a productCert and no --force has been specified.
 			log.warning("Not all of the channels are mapped to a product cert.  Therefore, the "+rhnMigrateTool+" command should have exited with code 1.");
 			expectedMsg = "Use --force to ignore these channels and continue the migration.";
 			Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to "+rhnMigrateTool+" with "+options+" contains message: "+expectedMsg);	
 			Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "ExitCode from call to "+rhnMigrateTool+" with "+options+" when any of the channels are not mapped to a productCert.");
 			Assert.assertTrue(RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"The system id file '"+clienttasks.rhnSystemIdFile+"' indicates this system is still registered using RHN Classic when rhn-migrate-classic-to-rhsm requires --force to continue.");
 			
 			// assert that no product certs have been copied yet
 			Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), 0, "No productCerts have been migrated when "+rhnMigrateTool+" requires --force to continue.");
 
 			// assert that we are not yet registered to RHSM
 			Assert.assertNull(clienttasks.getCurrentConsumerCert(),"We should NOT be registered to RHSM when "+rhnMigrateTool+" requires --force to continue.");
 
 			return;
 		}
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(0), "ExitCode from call to "+rhnMigrateTool+" with "+options+" when all of the channels are mapped.");
 
 		// assert that traffic to RHSM went through the proxy
 		proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 		Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to RHN.");
 
 		// assert that proxy configurations from RHN up2date have been copied to RHSM rhsm.conf
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_hostname"), proxy_hostnameConfig.replace("http://", ""), "The RHN hostname component from the httpProxy configuration in "+clienttasks.rhnUp2dateFile+" has been copied to the RHSM server.proxy_hostname configuration in "+clienttasks.rhsmConfFile+" (with prefix \"http://\" removed; reference bug 798015).");
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_port"), proxy_portConfig, "The RHN port component from the httpProxy configuration in "+clienttasks.rhnUp2dateFile+" has been copied to the RHSM server.proxy_port configuration in "+clienttasks.rhsmConfFile+".");
 		if (clienttasks.getConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth").equals("0") || clienttasks.getConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth").equalsIgnoreCase("false")) {
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_user"), "", "The RHSM server.proxy_user configuration in "+clienttasks.rhsmConfFile+" is removed when RHN configuration enableProxyAuth is false.");
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_password"), "", "The RHSM server.proxy_password configuration in "+clienttasks.rhsmConfFile+" is removed when RHN configuration enableProxyAuth is false.");
 		} else {
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_user"), proxy_userConfig, "The RHN proxyUser configuration in "+clienttasks.rhnUp2dateFile+" has been copied to the RHSM server.proxy_user configuration in "+clienttasks.rhsmConfFile+" when RHN configuration enableProxyAuth is true.");
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_password"), proxy_passwordConfig, "The RHN proxyPassword configuration in "+clienttasks.rhnUp2dateFile+" has been copied to the RHSM server.proxy_password configuration in "+clienttasks.rhsmConfFile+" when RHN configuration enableProxyAuth is true.");
 		}
 		
 		// assert we are no longer registered to RHN Classic
 		expectedMsg = "System successfully unregistered from RHN Classic.";
 		Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to "+rhnMigrateTool+" with "+options+" contains message: "+expectedMsg);
 		Assert.assertTrue(!RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"The system id file '"+clienttasks.rhnSystemIdFile+"' is absent.  This indicates this system is not registered using RHN Classic.");
 
 		// assert products are copied
 		expectedMsg = String.format("Product certificates copied successfully to %s !",	clienttasks.productCertDir);
 		expectedMsg = String.format("Product certificates copied successfully to %s",	clienttasks.productCertDir);
 		Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to "+rhnMigrateTool+" with "+options+" contains message: "+expectedMsg);
 		
 		// assert that the expected product certs mapped from the consumed RHN Classic channels are now installed
 		List<ProductCert> migratedProductCerts = clienttasks.getCurrentProductCerts();
 		Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), expectedMigrationProductCertFilenames.size(), "The number of productCerts installed after running "+rhnMigrateTool+" with "+options+".");
 		for (String expectedMigrationProductCertFilename : expectedMigrationProductCertFilenames) {
 			ProductCert expectedMigrationProductCert = clienttasks.getProductCertFromProductCertFile(new File(baseProductsDir+"/"+expectedMigrationProductCertFilename));
 			Assert.assertTrue(migratedProductCerts.contains(expectedMigrationProductCert),"The newly installed product certs includes the expected migration productCert: "+expectedMigrationProductCert);
 		}
 		Assert.assertEquals(clienttasks.getFactValue(migrationFromFact), "rhn_hosted_classic", "The migration fact '"+migrationFromFact+"' should be set after running "+rhnMigrateTool+" with "+options+".");
 		Assert.assertEquals(clienttasks.getFactValue(migrationSystemIdFact), rhnSystemId, "The migration fact '"+migrationSystemIdFact+"' should be set after running "+rhnMigrateTool+" with "+options+".");
 		
 		// assert final RHSM status....
 		
 		if (options.contains("-n")) { // -n, --no-auto   Do not autosubscribe when registering with subscription-manager
 			// assert that we are NOT registered using rhsm
 			clienttasks.identity_(null, null, null, null, null, null, null);
 			Assert.assertNull(clienttasks.getCurrentConsumerCert(),"We should NOT be registered to RHSM after a call to "+rhnMigrateTool+" with "+options+".");
 	
 			// assert that we are NOT consuming any entitlements
 			Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().isEmpty(),"We should NOT be consuming any RHSM entitlements after call to "+rhnMigrateTool+" with "+options+".");
 		} else {
 			// assert that we are registered using rhsm
 			clienttasks.identity(null, null, null, null, null, null, null);
 			Assert.assertNotNull(clienttasks.getCurrentConsumerId(),"The existance of a consumer cert indicates that the system is currently registered using RHSM.");
 	
 			// assert that we are consuming some entitlements (for at least the base product cert)
 			Assert.assertTrue(!clienttasks.getCurrentlyConsumedProductSubscriptions().isEmpty(),"We should be consuming some RHSM entitlements (at least for the base RHEL product) after call to "+rhnMigrateTool+" with "+options+".");
 			
 			// assert that the migrated productCert corresponding to the base channel has been autosubscribed by checking the status on the installedProduct
 			InstalledProduct installedProduct = clienttasks.getInstalledProductCorrespondingToProductCert(clienttasks.getProductCertFromProductCertFile(new File(clienttasks.productCertDir+"/"+getPemFileNameFromProductCertFilename(channelsToProductCertFilenamesMap.get(rhnBaseChannel)))));
 			Assert.assertEquals(installedProduct.status, "Subscribed","The migrated product cert corresponding to the RHN Classic base channel '"+rhnBaseChannel+"' was autosubscribed: "+installedProduct);
 		}
 	}
 	
 	
 	@Test(	description="Register system using RHN Classic and then Execute migration tool rhn-migrate-classic-to-rhsm with options after adding RHN Channels",
 			groups={"AcceptanceTests","RhnMigrateClassicToRhsm_Test","blockedByBug-840169"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="RhnMigrateClassicToRhsmData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=130764,130762) // TODO some expected yum repo assertions are not yet automated
 	public void RhnMigrateClassicToRhsm_Test(Object bugzilla, String rhnUsername, String rhnPassword, String rhnHostname, List<String> rhnChannelsToAdd, String options, String serviceLevel, List<String> expectedMigrationProductCertFilenames) {
 		if (!sm_serverType.equals(CandlepinType.hosted)) throw new SkipException("The configured candlepin server type ("+sm_serverType+") is not '"+CandlepinType.hosted+"'.  This test requires access registration access to RHN Classic.");
 		if (sm_rhnHostname.equals("")) throw new SkipException("This test requires access to RHN Classic.");
 		if (options.contains("-n")) log.info("Executing "+rhnMigrateTool+" --no-auto should effectively unregister your system from RHN Classic without registering to RHSM.");
 		if (serviceLevel!=null) options+=" -s "+serviceLevel;
 
 		// make sure we are NOT registered to RHSM
 		clienttasks.unregister(null,null,null);
 
 		// deleting the currently installed product certs
 		clienttasks.removeAllCerts(false, false, true);
 		clienttasks.removeAllFacts();
 		
 		// register to RHN Classic
 		String rhnSystemId = registerToRhnClassic(rhnUsername, rhnPassword, rhnHostname);
 		
 		// subscribe to more RHN Classic channels
 		if (rhnChannelsToAdd.size()>0) addRhnClassicChannels(rhnUsername, rhnPassword, rhnChannelsToAdd);
 		
 		// get a list of the consumed RHN Classic channels
 		List<String> rhnChannelsConsumed = getCurrentRhnClassicChannels();
 		if (rhnChannelsToAdd.size()>0) Assert.assertTrue(rhnChannelsConsumed.containsAll(rhnChannelsToAdd), "All of the RHN Classic channels added appear to be consumed.");
 
 		// get the product cert filenames that we should expect rhn-migrate-classic-to-rhsm to copy (or use the ones supplied to the @Test)
 		if (expectedMigrationProductCertFilenames==null) expectedMigrationProductCertFilenames = getExpectedMappedProductCertFilenamesCorrespondingToChannels(rhnChannelsConsumed);
 		
 		// execute rhn-migrate-classic-to-rhsm with options
 //		String sendServiceLevel = null;
 //		List<String> rhnServiceLevelsToUpperCase = new ArrayList<String>(); for (String sl : rhnServiceLevels) rhnServiceLevelsToUpperCase.add(sl.toUpperCase());
 //		if (serviceLevel!=null && !rhnServiceLevels.contains(serviceLevel)) sendServiceLevel = String.valueOf((rhnServiceLevelsToUpperCase.indexOf(serviceLevel.toUpperCase())+1));	// attempt to guess the number in the prompting by the migration tool for a valid service level
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsmWithOptions(rhnUsername,rhnPassword,serviceLevel,options);
 		
 		// assert the exit code
 		String expectedMsg;
 		if (!areAllChannelsMapped(rhnChannelsConsumed) && !options.contains("-f")/*--force*/) {	// when not all of the rhnChannelsConsumed have been mapped to a productCert and no --force has been specified.
 			log.warning("Not all of the channels are mapped to a product cert.  Therefore, the "+rhnMigrateTool+" command should have exited with code 1.");
 			expectedMsg = "Use --force to ignore these channels and continue the migration.";
 			Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to "+rhnMigrateTool+" with "+options+" contains message: "+expectedMsg);	
 			Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "ExitCode from call to "+rhnMigrateTool+" with "+options+" when any of the channels are not mapped to a productCert.");
 			Assert.assertTrue(RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"The system id file '"+clienttasks.rhnSystemIdFile+"' exists.  This indicates this system is still registered using RHN Classic when rhn-migrate-classic-to-rhsm requires --force to continue.");
 			
 			// assert that no product certs have been copied yet
 			Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), 0, "No productCerts have been migrated when "+rhnMigrateTool+" requires --force to continue.");
 
 			// assert that we are not yet registered to RHSM
 			Assert.assertNull(clienttasks.getCurrentConsumerCert(),"We should NOT be registered to RHSM when "+rhnMigrateTool+" requires --force to continue.");
 
 			return;
 		}
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(0), "ExitCode from call to "+rhnMigrateTool+" with "+options+" when all of the channels are mapped.");
 		
 		// assert we are no longer registered to RHN Classic
 		expectedMsg = "System successfully unregistered from RHN Classic.";
 		Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to "+rhnMigrateTool+" with "+options+" contains message: "+expectedMsg);
 		Assert.assertTrue(!RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"The system id file '"+clienttasks.rhnSystemIdFile+"' is abscent.  This indicates this system is not registered using RHN Classic.");
 
 		// assert products are copied
 		expectedMsg = String.format("Product certificates copied successfully to %s !",	clienttasks.productCertDir);
 		expectedMsg = String.format("Product certificates copied successfully to %s",	clienttasks.productCertDir);
 		Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to "+rhnMigrateTool+" with "+options+" contains message: "+expectedMsg);
 		
 		// assert that the expected product certs mapped from the consumed RHN Classic channels are now installed
 		List<ProductCert> migratedProductCerts = clienttasks.getCurrentProductCerts();
 		Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), expectedMigrationProductCertFilenames.size(), "The number of productCerts installed after running "+rhnMigrateTool+" with "+options+".");
 		for (String expectedMigrationProductCertFilename : expectedMigrationProductCertFilenames) {
 			ProductCert expectedMigrationProductCert = clienttasks.getProductCertFromProductCertFile(new File(baseProductsDir+"/"+expectedMigrationProductCertFilename));
 			Assert.assertTrue(migratedProductCerts.contains(expectedMigrationProductCert),"The newly installed product certs includes the expected migration productCert: "+expectedMigrationProductCert);
 		}
 
 		//	[root@ibm-x3620m3-01 ~]# subscription-manager facts --list | grep migration
 		//	migration.classic_system_id: 1023061526
 		//	migration.migrated_from: rhn_hosted_classic
 		//	migration.migration_date: 2012-07-13T18:51:44.254543
 		Map<String,String> factMap = clienttasks.getFacts();
 		Assert.assertEquals(factMap.get(migrationFromFact), "rhn_hosted_classic", "The migration fact '"+migrationFromFact+"' should be set after running "+rhnMigrateTool+" with "+options+".");
 		Assert.assertEquals(factMap.get(migrationSystemIdFact), rhnSystemId, "The migration fact '"+migrationSystemIdFact+"' should be set after running "+rhnMigrateTool+" with "+options+".");
 		Assert.assertNotNull(factMap.get(migrationDateFact), "The migration fact '"+migrationDateFact+"' should be set after running "+rhnMigrateTool+" with "+options+".");	// TODO assert the value of the migration date is today
 		
 		// assert final RHSM status....
 		
 		if (options.contains("-n")) { // -n, --no-auto   Do not autosubscribe when registering with subscription-manager
 			// assert that we are NOT registered using rhsm
 			clienttasks.identity_(null, null, null, null, null, null, null);
 			Assert.assertNull(clienttasks.getCurrentConsumerCert(),"We should NOT be registered to RHSM after a call to "+rhnMigrateTool+" with "+options+".");
 	
 			// assert that we are NOT consuming any entitlements
 			Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().isEmpty(),"We should NOT be consuming any RHSM entitlements after call to "+rhnMigrateTool+" with "+options+".");
 		} else {
 			// assert that we are registered using rhsm
 			clienttasks.identity(null, null, null, null, null, null, null);
 			Assert.assertNotNull(clienttasks.getCurrentConsumerId(),"The existance of a consumer cert indicates that the system is currently registered using RHSM.");
 
 			// assert that the migrated productCert corresponding to the base channel has been autosubscribed by checking the status on the installedProduct
 			// FIXME This assertion is wrong when there are no available subscriptions that provide for the migrated product certs' providesTags; however since we register as qa@redhat.com, I think we have access to all base rhel subscriptions
 			InstalledProduct installedProduct = clienttasks.getInstalledProductCorrespondingToProductCert(clienttasks.getProductCertFromProductCertFile(new File(clienttasks.productCertDir+"/"+getPemFileNameFromProductCertFilename(channelsToProductCertFilenamesMap.get(rhnBaseChannel)))));
 			Assert.assertEquals(installedProduct.status, "Subscribed","The migrated product cert corresponding to the RHN Classic base channel '"+rhnBaseChannel+"' was autosubscribed: "+installedProduct);
 			
 			// assert that we are consuming some entitlements (for at least the base product cert)
 			// FIXME This assertion is wrong when there are no available subscriptions that provide for the migrated product certs' providesTags; however since we register as qa@redhat.com, I think we have access to all base rhel subscriptions
 			List<ProductSubscription> consumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 			Assert.assertTrue(!consumedProductSubscriptions.isEmpty(),"We should be consuming some RHSM entitlements (at least for the base RHEL product) after call to "+rhnMigrateTool+" with "+options+".");
 			
 			// assert the service levels being consumed match the requested serviceLevel
 			if (serviceLevel!=null) {
 				if (isInteger(serviceLevel) && Integer.valueOf(serviceLevel)<=rhnServiceLevels.size()) {
 					serviceLevel = rhnServiceLevels.get(Integer.valueOf(serviceLevel)-1);
 				}
 
 				List<String> rhnServiceLevelsToUpperCase = new ArrayList<String>(); for (String sl : rhnServiceLevels) rhnServiceLevelsToUpperCase.add(sl.toUpperCase());
 //				if (serviceLevel!=null && !rhnServiceLevels.contains(serviceLevel)) sendServiceLevel = String.valueOf((rhnServiceLevelsToUpperCase.indexOf(serviceLevel.toUpperCase())+1));	// attempt to guess the number in the prompting by the migration tool for a valid service level
 
 				expectedMsg = String.format("Service level \"%s\" is not available.",	serviceLevel);
 				if (!rhnServiceLevelsToUpperCase.contains(serviceLevel.toUpperCase())) {
 					Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to "+rhnMigrateTool+" with "+options+" contains message: "+expectedMsg);
 				} else {
 					Assert.assertTrue(!sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to "+rhnMigrateTool+" with "+options+" does not contain message: "+expectedMsg);
 				}
 				
 				expectedMsg = "Attempting to auto-subscribe to appropriate subscriptions ...";
 				Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to "+rhnMigrateTool+" with "+options+" contains message: "+expectedMsg);			
 
 				if (isInteger(serviceLevel) && Integer.valueOf(serviceLevel)==rhnServiceLevels.size()+1) {
 					// when the specified noServiceLevel preference
 				} else {
 					// when a valid the servicelevel was either specified or chosen
 					expectedMsg = String.format("Service level set to: %s",serviceLevel);
 					Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to "+rhnMigrateTool+" with "+options+" contains message: "+expectedMsg);
 	
 					for (ProductSubscription productSubscription : consumedProductSubscriptions) {
 						Assert.assertNotNull(productSubscription.serviceLevel, "When migrating from RHN Classic with a specified service level '"+serviceLevel+"', this auto consumed product subscription's service level should not be null: "+productSubscription);
 						if (sm_exemptServiceLevelsInUpperCase.contains(productSubscription.serviceLevel.toUpperCase())) {
 							log.info("Exempt service levels: "+sm_exemptServiceLevelsInUpperCase);
 							Assert.assertTrue(sm_exemptServiceLevelsInUpperCase.contains(productSubscription.serviceLevel.toUpperCase()),"This auto consumed product subscription's service level is among the exempt service levels: "+productSubscription);
 						} else {
 							Assert.assertTrue(productSubscription.serviceLevel.equalsIgnoreCase(serviceLevel),"When migrating from RHN Classic with a specified service level '"+serviceLevel+"', this auto consumed product subscription's service level should match: "+productSubscription);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	
 	@Test(	description="Execute migration tool rhn-migrate-classic-to-rhsm with a non-default rhsm.productcertdir configured",
 			groups={"RhnMigrateClassicToRhsmWithNonDefaultProductCertDir_Test"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="RhnMigrateClassicToRhsmData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=130765)
 	public void RhnMigrateClassicToRhsmWithNonDefaultProductCertDir_Test(Object bugzilla, String rhnUsername, String rhnPassword, String rhnServer, List<String> rhnChannelsToAdd, String options, String serviceLevel, List<String> expectedProductCertFilenames) {
 		// NOTE: The configNonDefaultRhsmProductCertDir will handle the configuration setting
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm", "productCertDir"), nonDefaultProductCertDir,"A non-default rhsm.productCertDir has been configured.");
 		RhnMigrateClassicToRhsm_Test(bugzilla,rhnUsername,rhnPassword,rhnServer,rhnChannelsToAdd,options,serviceLevel,expectedProductCertFilenames);
 	}
 	
 	
 	@Test(	description="migrating a RHEL5 Client - Desktop versus Workstation",
 			groups={"blockedByBug-786257","RhnMigrateClassicToRhsm_Test"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="RhnMigrateClassicToRhsm_Rhel5ClientDesktopVersusWorkstationData",
 			enabled=true)
 	public void RhnMigrateClassicToRhsm_Rhel5ClientDesktopVersusWorkstation_Test(Object bugzilla, String rhnUsername, String rhnPassword, String rhnHostname, List<String> rhnChannelsToAdd, List<String> expectedMigrationProductCertFilenames) {
 		if (!sm_serverType.equals(CandlepinType.hosted)) throw new SkipException("The configured candlepin server type ("+sm_serverType+") is not '"+CandlepinType.hosted+"'.  This test requires access registration access to RHN Classic.");
 		if (sm_rhnHostname.equals("")) throw new SkipException("This test requires access to RHN Classic.");
 
 		log.info("Red Hat Enterprise Linux Desktop (productId=68) corresponds to the base RHN Channel (rhel-ARCH-client-5) for a 5Client system where ARCH=i386,x86_64.");
 		log.info("Red Hat Enterprise Linux Workstation (productId=71) corresponds to child RHN Channel (rhel-ARCH-client-workstation-5) for a 5Client system where ARCH=i386,x86_64.");	
 		log.info("After migrating from RHN Classic to RHSM, these two product certs should not be installed at the same time.");
 
 		RhnMigrateClassicToRhsm_Test(null,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnChannelsToAdd, "--no-auto", null, expectedMigrationProductCertFilenames);		
 	}
 	
 	
 	@Test(	description="Execute migration tool rhn-migrate-classic-to-rhsm with invalid credentials",
 			groups={"blockedByBug-789008","blockedByBug-807477"},
 			dependsOnMethods={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=136404)
 	public void RhnMigrateClassicToRhsmWithInvalidCredentials_Test() {
 		clienttasks.unregister(null,null,null);
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsmWithOptions("foo","bar",null,null);
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "The expected exit code from call to "+rhnMigrateTool+" with invalid credentials.");
 		//Assert.assertContainsMatch(sshCommandResult.getStdout(), "Unable to connect to certificate server.  See "+clienttasks.rhsmLogFile+" for more details.", "The expected stdout result from call to "+rhnMigrateTool+" with invalid credentials.");		// valid prior to bug fix 789008
 		Assert.assertContainsMatch(sshCommandResult.getStdout(), "Unable to connect to certificate server: "+servertasks.invalidCredentialsMsg()+".  See "+clienttasks.rhsmLogFile+" for more details.", "The expected stdout result from call to "+rhnMigrateTool+" with invalid credentials.");
 	}
 	
 	
 	@Test(	description="Execute migration tool rhn-migrate-classic-to-rhsm without having registered to classic (no /etc/sysconfig/rhn/systemid)",
 			groups={"blockedByBug-807477","AcceptanceTests"},
 			dependsOnMethods={},
 			enabled=true)
 	public void RhnMigrateClassicToRhsmWithMissingSystemIdFile_Test() {
 		if (!sm_serverType.equals(CandlepinType.hosted)) throw new SkipException("This test requires that your candlepin server be a hosted system that accepts credentials for '"+sm_clientUsername+"' to RHN Classic.");
 	    removeProxyServerConfigurations();	// cleanup from prior tests
 	    clienttasks.unregister(null,null,null);
 		client.runCommandAndWait("rm -f "+clienttasks.rhnSystemIdFile);
 		Assert.assertTrue(!RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"This system is not registered using RHN Classic.");
 		
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsmWithOptions(sm_clientUsername,sm_clientPassword,null,null);
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "The expected exit code from call to "+rhnMigrateTool+" without having registered to RHN Classic.");
 		Assert.assertContainsMatch(sshCommandResult.getStdout(), "Unable to locate SystemId file. Is this system registered?", "The expected stdout result from call to "+rhnMigrateTool+" without having registered to RHN Classic.");
 	}
 	
 	
 	@Test(	description="Execute migration tool rhn-migrate-classic-to-rhsm while already registered to RHSM",
 			groups={"blockedByBug-807477"},
 			dependsOnMethods={},
 			enabled=true)
 	public void RhnMigrateClassicToRhsmWhileAlreadyRegisteredToRhsm_Test() {
 		client.runCommandAndWait("rm -f "+clienttasks.rhnSystemIdFile);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (List<String>)null, null, null, true, null, null, null, null);
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsmWithOptions(sm_clientUsername,sm_clientPassword,null,null);
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "The expected exit code from call to "+rhnMigrateTool+" while already registered to RHSM.");
 		Assert.assertContainsMatch(sshCommandResult.getStdout(), "This machine appears to be already registered to Certificate-based RHN.  Exiting.", "The expected stdout result from call to "+rhnMigrateTool+" while already registered to RHSM.");
 	}
 	
 
 
 	
 
 	
 	// Candidates for an automated Test:
 	// TODO Bug 789007 - Migrate with normal user (non org admin) user .
 	// TODO https://tcms.engineering.redhat.com/case/130762/?from_plan=5223
 	// TODO Bug 816377 - rhn-migrate-classic-to-rhsm throws traceback when subscription-manager-migration-data is not installed
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=816364#c6
 	
 	// Configuration methods ***********************************************************************
 	
 
 	@BeforeClass(groups="setup")
 	public void setupBeforeClass() {
 		if (clienttasks==null) return;
 		
 		// determine the full path to the channelCertMappingFile
 		baseProductsDir+="-"+clienttasks.redhatReleaseX;
 		channelCertMappingFilename = baseProductsDir+"/"+channelCertMappingFilename;
 		
 		// make sure needed rpms are installed
 		for (String pkg : new String[]{"subscription-manager-migration", "subscription-manager-migration-data", "expect"}) {
 			Assert.assertTrue(clienttasks.isPackageInstalled(pkg),"Required package '"+pkg+"' is installed for MigrationTests.");
 		}
 	}
 	
 	
 	@BeforeClass(groups="setup", dependsOnMethods={"setupBeforeClass"})
 	public void rememberOriginallyInstalledRedHatProductCertsBeforeClass() {
 		
 		// review the currently installed product certs and filter out the ones from test automation (indicated by suffix "_.pem")
 		for (File productCertFile : clienttasks.getCurrentProductCertFiles()) {
 			if (!productCertFile.getName().endsWith("_.pem")) {	// The product cert files ending in "_.pem" are not true RedHat products
 				originallyInstalledRedHatProductCerts.add(clienttasks.getProductCertFromProductCertFile(productCertFile));
 			}
 		}
 	}
 	
 	@BeforeClass(groups="setup", dependsOnMethods={"setupBeforeClass"})
 	public void backupProductCertsBeforeClass() {
 		
 		// determine the original productCertDir value
 		//productCertDirRestore = clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm", "productCertDir");
 		originalProductCertDir = clienttasks.productCertDir;
 		
 		log.info("Backing up all the currently installed product certs...");
 		client.runCommandAndWait("mkdir -p "+backupProductCertDir+"; rm -f "+backupProductCertDir+"/*.pem");
 		client.runCommandAndWait("cp "+originalProductCertDir+"/*.pem "+backupProductCertDir);
 	}
 	
 	@BeforeClass(groups="setup")
 	public void determineRhnServiceLevels() throws JSONException, Exception {
 		if (sm_rhnUsername.equals("")) return;
 		if (sm_rhnPassword.equals("")) return;
 		
 		// determine the valid service levels available to a consumer registered with rhn credentials
 		clienttasks.register_(sm_rhnUsername, sm_rhnPassword, null, null, null, null, null, null, null, null, (String)null, null, null, true, null, null, null, null);
 		String rhnOrg = clienttasks.getCurrentlyRegisteredOwnerKey();
 		clienttasks.unregister(null, null, null);
 		rhnServiceLevels = CandlepinTasks.getServiceLevelsForOrgKey(sm_rhnUsername, sm_rhnPassword, sm_serverUrl, rhnOrg);		
 	}
 	
 	@AfterClass(groups="setup")
 	public void restoreProductCertsAfterClass() {
 		if (clienttasks==null) return;
 		
 		log.info("Restoring the originally installed product certs...");
 		client.runCommandAndWait("rm -f "+originalProductCertDir+"/*.pem");
 		client.runCommandAndWait("cp "+backupProductCertDir+"/*.pem "+originalProductCertDir);
 		configOriginalRhsmProductCertDir();
 	}
 	
 	@BeforeGroups(groups="setup",value={"InstallNumMigrateToRhsmWithInstNumber_Test","InstallNumMigrateToRhsm_Test","RhnMigrateClassicToRhsm_Test"})
 	public void configOriginalRhsmProductCertDir() {
 		if (clienttasks==null) return;
 		
 		//clienttasks.config(false, false, true, new String[]{"rhsm","productcertdir",productCertDirOriginal});
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", originalProductCertDir);
 	}
 	
 	@BeforeClass(groups="setup")
 	@AfterGroups(groups="setup",value={"RhnMigrateClassicToRhsmUsingProxyServer_Test"})
 	public void removeProxyServerConfigurations() {
 		if (clienttasks==null) return;
 		
 		// remove proxy settings from rhsm.conf
 		// these will actually remove the value from the config file; don't do this
 		//clienttasks.config(false, true, false, new String[]{"server","proxy_hostname"});
 		//clienttasks.config(false, true, false, new String[]{"server","proxy_user"});
 		//clienttasks.config(false, true, false, new String[]{"server","proxy_password"});
 		//clienttasks.config(false, true, false, new String[]{"server","proxy_port"});
 		clienttasks.config(false, false, true, new String[]{"server","proxy_hostname",""});
 		clienttasks.config(false, false, true, new String[]{"server","proxy_user",""});
 		clienttasks.config(false, false, true, new String[]{"server","proxy_password",""});
 		clienttasks.config(false, false, true, new String[]{"server","proxy_port",""});
 		
 		// remove proxy settings from up2date
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxy", "0");		// enableProxyAuth[comment]=To use an authenticated proxy or not
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "httpProxy", "");			// httpProxy[comment]=HTTP proxy in host:port format, e.g. squid.redhat.com:3128
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth", "0");	// enableProxyAuth[comment]=To use an authenticated proxy or not
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyUser", "");			// proxyUser[comment]=The username for an authenticated proxy
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyPassword", "");		// proxyPassword[comment]=The password to use for an authenticated proxy
 		
 		iptablesAcceptPort(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "port"));
 	}
 	
 	@BeforeClass(groups="setup", dependsOnMethods={"setupBeforeClass","determineRhnServiceLevels"})
 	public void determineRhnClassicBaseAndAvailableChildChannels() throws IOException {
 //debugTesting if (true) return;
 		if (sm_rhnUsername.equals("")) {log.warning("Skipping determination of the base and available RHN Classic channels"); return;}
 		if (sm_rhnPassword.equals("")) {log.warning("Skipping determination of the base and available RHN Classic channels"); return;}
 		if (sm_rhnHostname.equals("")) {log.warning("Skipping determination of the base and available RHN Classic channels"); return;}
 		
 		// copy the rhn-channels.py script to the client
 		File rhnChannelsScriptFile = new File(System.getProperty("automation.dir", null)+"/scripts/rhn-channels.py");
 		if (!rhnChannelsScriptFile.exists()) Assert.fail("Failed to find expected script: "+rhnChannelsScriptFile);
 		RemoteFileTasks.putFile(client.getConnection(), rhnChannelsScriptFile.toString(), "/usr/local/bin/", "0755");
 
 		// get the base channel
 		registerToRhnClassic(sm_rhnUsername, sm_rhnPassword, sm_rhnHostname);
 		List<String> rhnChannels = getCurrentRhnClassicChannels();
 		Assert.assertEquals(rhnChannels.size(), 1, "The number of base RHN Classic base channels this system is consuming.");
 		rhnBaseChannel = getCurrentRhnClassicChannels().get(0);
 
 		// get all of the available RHN Classic channels available for consuming under this base channel
 //		String command = String.format("rhn-channels.py --username=%s --password=%s --server=%s --basechannel=%s --no-custom", sm_rhnUsername, sm_rhnPassword, sm_rhnHostname, rhnBaseChannel);
 		String command = String.format("rhn-channels.py --username=%s --password=%s --server=%s --basechannel=%s --no-custom --available", sm_rhnUsername, sm_rhnPassword, sm_rhnHostname, rhnBaseChannel);
 //debugTesting if (true) command = "echo rhel-x86_64-server-5 && echo rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5 && echo rhx-amanda-enterprise-backup-2.6-rhel-x86_64-server-5";
 
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0));
 		rhnChannels = new ArrayList<String>();
 		if (!result.getStdout().trim().equals("")) {
 			rhnChannels	= Arrays.asList(result.getStdout().trim().split("\\n"));
 		}
 		for (String rhnChannel : rhnChannels) {
 			if (!rhnChannel.equals(rhnBaseChannel)) rhnAvailableChildChannels.add(rhnChannel.trim()); 
 		}
 		Assert.assertTrue(rhnAvailableChildChannels.size()>0,"A positive number of child channels under the RHN Classic base channel '"+rhnBaseChannel+"' are available for consumption.");
 
 	}
 	
 	@BeforeGroups(groups="setup",value={"InstallNumMigrateToRhsmWithNonDefaultProductCertDir_Test","RhnMigrateClassicToRhsmWithNonDefaultProductCertDir_Test"})
 	public void configNonDefaultRhsmProductCertDir() {
 		if (clienttasks==null) return;
 		
 		//clienttasks.config(false, false, true, new String[]{"rhsm","productcertdir",productCertDirNonDefault});
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", nonDefaultProductCertDir);
 	}
 
 	
 	public static SSHCommandRunner basicAuthProxyRunner = null;
 	public static SSHCommandRunner noAuthProxyRunner = null;
 	@BeforeClass(groups={"setup"})
 	public void setupProxyRunnersBeforeClass() throws IOException {
 		basicAuthProxyRunner = new SSHCommandRunner(sm_basicauthproxyHostname, sm_sshUser, sm_sshKeyPrivate, sm_sshkeyPassphrase, null);
 		noAuthProxyRunner = new SSHCommandRunner(sm_noauthproxyHostname, sm_sshUser, sm_sshKeyPrivate, sm_sshkeyPassphrase, null);
 	}
 	
 	// Protected methods ***********************************************************************
 	protected String baseProductsDir = "/usr/share/rhsm/product/RHEL";
 	protected String channelCertMappingFilename = "channel-cert-mapping.txt";
 	protected List<String> mappedProductCertFilenames = new ArrayList<String>();	// list of all the mapped product cert file names in the mapping file (e.g. Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem)
 	protected Map<String,String> channelsToProductCertFilenamesMap = new HashMap<String,String>();	// map of all the channels to product cert file names (e.g. key=rhn-tools-rhel-x86_64-server-5 value=Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem)
 	protected Map<String,List<String>> cdnProductBaselineChannelMap = new HashMap<String,List<String>>();	// map of all the channels to product cert file names (e.g. key=rhn-tools-rhel-x86_64-server-5 value=Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem)
 	protected List<ProductCert> originallyInstalledRedHatProductCerts = new ArrayList<ProductCert>();
 	protected String migrationFromFact				= "migration.migrated_from";
 	protected String migrationSystemIdFact			= "migration.classic_system_id";
 	protected String migrationDateFact				= "migration.migration_date";
 	protected String originalProductCertDir			= null;
 	protected String backupProductCertDir			= "/tmp/backupOfProductCertDir";
 	protected String nonDefaultProductCertDir		= "/tmp/migratedProductCertDir";
 	protected String machineInstNumberFile			= "/etc/sysconfig/rhn/install-num";
 	protected String backupMachineInstNumberFile	= machineInstNumberFile+".bak";
 	protected String rhnBaseChannel = null;
 	protected List<String> rhnAvailableChildChannels = new ArrayList<String>();
 	static public String installNumTool = "install-num-migrate-to-rhsm";
 	static public String rhnMigrateTool = "rhn-migrate-classic-to-rhsm";
 	protected List<String> rhnServiceLevels = new ArrayList<String>();	// list of service levels available to the rhn user to be migrated
 
 	
 	protected List<String> getExpectedMappedProductCertFilenamesCorrespondingToChannels(List<String> channels) {
 		List<String> mappedProductCertFilenamesCorrespondingToChannels = new ArrayList<String>();
 		for (String channel : channels) {
 			String mappedProductCertFilename = channelsToProductCertFilenamesMap.get(channel);
 			if (mappedProductCertFilename==null) {
 				//log.warning("RHN Classic channel '"+channel+"' is NOT mapped in the file '"+channelCertMappingFilename+"'.");
 			} else {
 				log.info("The mapped product cert filename for RHN Classic channel '"+channel+"' is: "+mappedProductCertFilename);
 				if (!mappedProductCertFilename.equalsIgnoreCase("none")) {
 					if (!mappedProductCertFilenamesCorrespondingToChannels.contains(mappedProductCertFilename)) {	// make sure the list contains unique filenames
 						mappedProductCertFilenamesCorrespondingToChannels.add(mappedProductCertFilename);
 					}
 				}
 			}
 		}
 		return mappedProductCertFilenamesCorrespondingToChannels;
 	}
 	
 	protected boolean areAllChannelsMapped(List<String> channels) {
 		boolean allChannelsAreMapped = true;
 		for (String channel : channels) {
 			String mappedProductCertFilename = channelsToProductCertFilenamesMap.get(channel);
 			if (mappedProductCertFilename==null) {
 				allChannelsAreMapped = false;
 				log.warning("RHN Classic channel '"+channel+"' is NOT mapped in the file '"+channelCertMappingFilename+"'.");
 			}
 		}
 		return allChannelsAreMapped;
 	}
 	
 	/**
 	 * Use the python instnum.py program to determine what mapped product cert filenames from the channel-cert-mapping.txt correspond to this instnumber and should therefore be copied.
 	 * @param instnumber
 	 * @return
 	 * @throws JSONException
 	 */
 	protected List<String> getExpectedMappedProductCertFilenamesCorrespondingToInstnumberUsingInstnumTool(String instnumber) throws JSONException {
 		List<String> mappedProductCertFilenamesCorrespondingToInstnumber = new ArrayList<String>();
 
 		String command = "python /usr/lib/python2.4/site-packages/instnum.py "+instnumber;
 		//SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client,command,0);
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client,command+" | egrep \"^{.*}$\"", 0);
 		// [root@jsefler-onprem-5server ~]# python /usr/lib/python2.4/site-packages/instnum.py 0000000e0017fc01 | egrep "^{.*}$"
 		// {'Virt': 'VT', 'Workstation': 'Workstation', 'Base': 'Client'}
 		
 		// decide what product arch applies to our system
 		String arch = clienttasks.arch;	// default
 		if (clienttasks.redhatReleaseX.equals("5") && clienttasks.arch.equals("ppc64")) arch = "ppc";	// RHEL5 only supports ppc packages, but can be run on ppc64 hardware
 		if (Arrays.asList("i386","i486","i586","i686").contains(clienttasks.arch)) arch = "i386";		// RHEL supports i386 packages, but can be run on all 32-bit arch hardware
 		
 		// process result as a json object
 		JSONObject jsonResult = new JSONObject(result.getStdout());
 		String base = jsonResult.getString("Base");
 		
 		// Workstation (71.pem) is a special sub of the base Client (68.pem) - see bug 790217
 		// when the Workstation key is present on a base Client install, remove the base key - the effect will be to trump the base product cert with the Workstation product cert
 		if (jsonResult.has("Workstation")/* && base.equalsIgnoreCase("client") I DON'T THINK THIS IS NECESSARY*/) {
 			log.warning("This appears to be a Workstation install ("+instnumber+"). Therefore we will assume that the Workstation product cert trumps the Base.");
 			jsonResult.remove("Base");
 		}
 		
 		for (String mappedProductCertFilename : mappedProductCertFilenames) {
 			// example mappedProductCertFilenames:
 			// Server-Server-s390x-340665cdadee-72.pem  
 			// Server-ClusterStorage-ppc-a3fea9e1dde3-90.pem
 			// base-sub-arch-hash-id.pem
 			Iterator keys = jsonResult.keys();
 			while (keys.hasNext()) {
 				String key = (String)keys.next();
 				String sub = jsonResult.getString(key);
 				if (mappedProductCertFilename.startsWith(base+"-"+sub+"-"+arch+"-")) {
 					if (!mappedProductCertFilenamesCorrespondingToInstnumber.contains(mappedProductCertFilename)) {	// make sure the list contains unique filenames
 						mappedProductCertFilenamesCorrespondingToInstnumber.add(mappedProductCertFilename);
 					}
 				}
 			}
 		}
 		
 		return mappedProductCertFilenamesCorrespondingToInstnumber;
 	}
 	
 	
 	
 	/**
 	 * Extract the suffix pem filename from the long mapped filename.
 	 * @param productCertFilename example: Server-ClusterStorage-ppc-a3fea9e1dde3-90.pem
 	 * @return example: 90.pem
 	 */
 	protected String getPemFileNameFromProductCertFilename(String productCertFilename) {
 		// Server-ClusterStorage-ppc-a3fea9e1dde3-90.pem
 		return productCertFilename.split("-")[productCertFilename.split("-").length-1];
 	}
 	
 	/**
 	 * Extract the productId from the long mapped filename.
 	 * @param productCertFilename example: Server-ClusterStorage-ppc-a3fea9e1dde3-90.pem
 	 * @return example: 90
 	 */
 	protected String getProductIdFromProductCertFilename(String productCertFilename) {
 		// Server-ClusterStorage-ppc-a3fea9e1dde3-90.pem
 		String pemFilename = getPemFileNameFromProductCertFilename(productCertFilename);
 		return pemFilename.replace(".pem", "");
 	}
 	
 	/**
 	 * Call rhn-migrate-classic-to-rhsm without asserting results.
 	 * @param sendUsername - interactive keystrokes to enter at the prompt for username
 	 * @param sendPassword - interactive keystrokes to enter at the prompt for passwprd
 	 * @param sendServiceLevel - interactive keystrokes to enter at the prompt for servicelevel
 	 * @param options
 	 * @return
 	 */
 	protected SSHCommandResult executeRhnMigrateClassicToRhsmWithOptions(String sendUsername, String sendPassword, String sendServiceLevel, String options) {
 		// assemble an ssh command using expect to simulate an interactive supply of credentials to the rhn-migrate-classic-to-rhsm command
 		// RHN Username: 
 		String promptedUsernames=""; if (sendUsername!=null) for (String u : sendUsername.split("\\n")) {
 			promptedUsernames += "expect \\\"*Username:\\\"; send "+u+"\\\r;";	// RHN Username:
 		}
 		// Password: 
 		String promptedPasswords=""; if (sendPassword!=null) for (String p : sendPassword.split("\\n")) {
 			promptedPasswords += "expect \\\"*Password:\\\"; send "+p+"\\\r;";	// Password:
 		}
 		
 		//  Service level "stANDArD" is not available.		<== WHEN --servicelevel="stANDArD" WAS ENTERED AS A COMMAND LINE OPTION
 		//	You have entered an invalid choice.				<== WHEN "stANDArD" WAS ENTERED AT THE PROMPT
 		//	Please select a service level agreement for this system.
 		//	1. Standard
 		//	2. Layered
 		//	3. Self-support
 		//	4. None
 		//	5. Premium
 		//	6. No service level preference
 		//	? 
 		String promptedServiceLevels=""; if (sendServiceLevel!=null) for (String s : sendServiceLevel.split("\\n")) {
 			promptedServiceLevels += "expect \\\"Please select a service level agreement for this system.\\\"; send "+s+"\\\r;";	// ? 
 		}
 		if (options==null) options="";
 		// [root@jsefler-onprem-5server ~]# expect -c "spawn rhn-migrate-classic-to-rhsm --cli-only; expect \"*Username:\"; send qa@redhat.com\r; expect \"*Password:\"; send CHANGE-ME\r; interact; catch wait reason; exit [lindex \$reason 3]"
 		String command = String.format("expect -c \"spawn %s %s; %s %s %s interact; catch wait reason; exit [lindex \\$reason 3]\"", rhnMigrateTool, options, promptedUsernames, promptedPasswords, promptedServiceLevels);
 		//                                                                                      ^^^^^^^^ DO NOT USE expect eof IT WILL TRUNCATE THE --force OUTPUT MESSAGE
 		return client.runCommandAndWait(command);
 	}
 	
 	/**
 	 * Call rhnreg_ks and assert the existence of a systemid file afterwards.
 	 * @param rhnUsername
 	 * @param rhnPassword
 	 * @param rhnHostname
 	 * @return the rhn system_id value from the contents of the systemid file
 	 */
 	protected String registerToRhnClassic(String rhnUsername, String rhnPassword, String rhnHostname) {
 		
 		// register to RHN Classic
 		// [root@jsefler-onprem-5server ~]# rhnreg_ks --serverUrl=https://xmlrpc.rhn.code.stage.redhat.com/XMLRPC --username=qa@redhat.com --password=CHANGE-ME --force --norhnsd --nohardware --nopackages --novirtinfo
 		//	ERROR: refreshing remote package list for System Profile
 		String command = String.format("rhnreg_ks --serverUrl=https://xmlrpc.%s/XMLRPC --username=%s --password=%s --profilename=%s --force --norhnsd --nohardware --nopackages --novirtinfo", rhnHostname, rhnUsername, rhnPassword, "rhsm-automation."+clienttasks.hostname);
 		SSHCommandResult result = client.runCommandAndWait(command);
 		
 		// assert result
 		Assert.assertEquals(result.getExitCode(), new Integer(0),"Exitcode from attempt to register to RHN Classic.");
 		Assert.assertEquals(result.getStderr(), "","Stderr from attempt to register to RHN Classic.");
 		if (!result.getStdout().trim().equals("")) log.warning("Ignoring result: "+result.getStdout().trim()); 		// <- IGNORE ERRORS LIKE THIS: ERROR: refreshing remote package list for System Profile
 		//Assert.assertEquals(result.getStdout(), "","Stdout from attempt to register to RHN Classic.");
 		
 		// assert existance of system id file
 		Assert.assertTrue(RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"The system id file '"+clienttasks.rhnSystemIdFile+"' exists.  This indicates this system is registered using RHN Classic.");
 		
 		// get the value of the systemid
 		// [root@jsefler-onprem-5server rhn]# grep ID- /etc/sysconfig/rhn/systemid
 		// <value><string>ID-1021538137</string></value>
 		command = String.format("grep ID- %s", clienttasks.rhnSystemIdFile);
 		return client.runCommandAndWait(command).getStdout().trim().replaceAll("\\<.*?\\>", "").replaceFirst("ID-", "");		// return 1021538137
 	}
 	
 	/**
 	 * Call rhn-channel --list to get the currently consumed RHN channels.
 	 * @return
 	 */
 	protected List<String> getCurrentRhnClassicChannels() {
 		
 		// [root@jsefler-onprem-5server rhn]# rhn-channel --list
 		// rhel-x86_64-server-5
 		// rhel-x86_64-server-supplementary-5
 		// rhel-x86_64-server-supplementary-5-debuginfo
 		String command = String.format("rhn-channel --list");
 		SSHCommandResult result = client.runCommandAndWait(command);
 		
 		// assert result
 		Assert.assertEquals(result.getExitCode(), new Integer(0),"Exitcode from attempt to list currently consumed RHN Classic channels.");
 		Assert.assertEquals(result.getStderr(), "","Stderr from attempt to list currently consumed RHN Classic channels.");
 		
 		List<String> rhnChannels = new ArrayList<String>();
 		if (!result.getStdout().trim().equals("")) {
 			rhnChannels	= Arrays.asList(result.getStdout().trim().split("\\n"));
 		}
 		return rhnChannels;
 	}
 	
 	/**
 	 * Call rhn-channel --user=$rhnUsername --password=$rhnPassword --add --channel=$rhnChannel to consume RHN channels.
 	 * @param rhnUsername
 	 * @param rhnPassword
 	 * @param rhnChannels
 	 */
 	protected void addRhnClassicChannels(String rhnUsername, String rhnPassword, List<String> rhnChannels) {
 		if (false) {	// THIS APPROACH FAILS WHEN THERE IS AN OFFENDING CHANNEL OR THERE ARE TONS OF CHANNELS; I THINK THERE MIGHT BE AN INPUT LIMIT FOR PYTHON ARGS
 			// [root@jsefler-onprem-5server rhn]# rhn-channel --add --user=qa@redhat.com --password=CHANGE-ME --channel=rhel-x86_64-server-sap-5
 			String rhnChannelsAsOptions=""; for (String rhnChannel : rhnChannels) rhnChannelsAsOptions+=String.format("--channel=%s ",rhnChannel);
 			String command = String.format("rhn-channel --user=%s --password=%s --add %s",rhnUsername,rhnPassword,rhnChannelsAsOptions);
 			SSHCommandResult result = client.runCommandAndWait(command);
 			Assert.assertEquals(result.getExitCode(), new Integer(0),"Exitcode from attempt to add RHN Classic channel.");
 			Assert.assertEquals(result.getStderr(), "","Stderr from attempt to add RHN Classic channel.");
 		} else if (false) {	// THIS APPROACH DOES NOT HIDE AN OFFENDING CHANNEL, BUT IT IS SLOWER AND CONSUMES LOTS OF LOG SPACE
 			for (String rhnChannel : rhnChannels) {
 				String command = String.format("rhn-channel --user=%s --password=%s --add --channel=%s",rhnUsername,rhnPassword,rhnChannel);
 				SSHCommandResult result = client.runCommandAndWait(command);
 				Assert.assertEquals(result.getExitCode(), new Integer(0),"Exitcode from attempt to add RHN Classic channel.");
 				Assert.assertEquals(result.getStderr(), "","Stderr from attempt to add RHN Classic channel.");
 			}
 		} else {	// THIS APPROACH WORKS WELL, BUT IT HIDES OFFENDING CHANNELS
 			String command="";
 			for (String rhnChannel : rhnChannels) {
 				//command += String.format(" && rhn-channel --user=%s --password=%s --add --channel=%s",rhnUsername,rhnPassword,rhnChannel);
 				command += String.format(" && rhn-channel -u %s -p %s -a -c %s",rhnUsername,rhnPassword,rhnChannel);
 			}
 			command = command.replaceFirst("^ *&& *", "");
 			SSHCommandResult result = client.runCommandAndWait(command);
 			Assert.assertEquals(result.getExitCode(), new Integer(0),"Exitcode from attempt to add RHN Classic channels.");
 			Assert.assertEquals(result.getStderr(), "","Stderr from attempt to add RHN Classic channel.");
 		}
 	}
 
 
 
 	protected void iptablesRejectPort(String port) {
 		//	[root@jsefler-r63-server rhn]# iptables -L OUTPUT
 		//	Chain OUTPUT (policy ACCEPT)
 		//	target     prot opt source               destination         
 		//	[root@jsefler-r63-server rhn]# iptables -A OUTPUT -p tcp --dport 443 -j REJECT
 		//	[root@jsefler-r63-server rhn]# iptables -L OUTPUT
 		//	Chain OUTPUT (policy ACCEPT)
 		//	target     prot opt source               destination         
 		//	REJECT     tcp  --  anywhere             anywhere            tcp dpt:https reject-with icmp-port-unreachable 
 		RemoteFileTasks.runCommandAndAssert(client, String.format("iptables -A OUTPUT -p tcp --dport %s -j REJECT",port), 0);
 	}
 	
 	protected void iptablesAcceptPort(String port) {
 		//	[root@jsefler-r63-server rhn]# iptables -L OUTPUT
 		//	Chain OUTPUT (policy ACCEPT)
 		//	target     prot opt source               destination         
 		//	[root@jsefler-r63-server rhn]# iptables -A OUTPUT -p tcp --dport 443 -j REJECT
 		//	[root@jsefler-r63-server rhn]# iptables -A OUTPUT -p tcp --dport 443 -j REJECT
 		//	[root@jsefler-r63-server rhn]# iptables -L OUTPUT
 		//	Chain OUTPUT (policy ACCEPT)
 		//	target     prot opt source               destination         
 		//	REJECT     tcp  --  anywhere             anywhere            tcp dpt:https reject-with icmp-port-unreachable 
 		//	REJECT     tcp  --  anywhere             anywhere            tcp dpt:https reject-with icmp-port-unreachable 
 		//	[root@jsefler-r63-server rhn]# iptables -D OUTPUT -p tcp --dport 443 -j REJECT
 		//	[root@jsefler-r63-server rhn]# echo $?
 		//	0
 		//	[root@jsefler-r63-server rhn]# iptables -D OUTPUT -p tcp --dport 443 -j REJECT
 		//	[root@jsefler-r63-server rhn]# echo $?
 		//	0
 		//	[root@jsefler-r63-server rhn]# iptables -D OUTPUT -p tcp --dport 443 -j REJECT
 		//	iptables: No chain/target/match by that name.
 		//	[root@jsefler-r63-server rhn]# echo $?
 		//	1
 		//	[root@jsefler-r63-server rhn]# iptables -L OUTPUT
 		//	Chain OUTPUT (policy ACCEPT)
 		//	target     prot opt source               destination         
 		//	[root@jsefler-r63-server rhn]# 
 		do {
 			client.runCommandAndWait(String.format("iptables -D OUTPUT -p tcp --dport %s -j REJECT",port));
 		} while (client.getExitCode()==0);
 	}
 
 	// Data Providers ***********************************************************************
 
 	@DataProvider(name="InstallNumMigrateToRhsmData")
 	public Object[][] getInstallNumMigrateToRhsmDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInstallNumMigrateToRhsmDataAsListOfLists());
 	}
 	public List<List<Object>> getInstallNumMigrateToRhsmDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		// if (!clienttasks.redhatReleaseX.equals("5")) return ll;	// prone to improperly unexecuted tests
 		if (clienttasks.redhatReleaseX.equals("6")) return ll;
 		if (clienttasks.redhatReleaseX.equals("7")) return ll;
 		
 		// REFRENCE DATA FROM: http://linuxczar.net/articles/rhel-installation-numbers
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("790217"),	"0000000e0017fc01"}));	// Client
 		ll.add(Arrays.asList(new Object[]{null,							"000000990007fc02"}));	// Red Hat Global Desktop
 		ll.add(Arrays.asList(new Object[]{null,							"000000e90007fc00"}));	// Server
 		ll.add(Arrays.asList(new Object[]{null,							"00000065000bfc00"}));	// Server with Cluster
 		ll.add(Arrays.asList(new Object[]{null,							"000000ab000ffc00"}));	// Server with ClusterStorage
 		ll.add(Arrays.asList(new Object[]{null,							"000000e30013fc00"}));	// Server with HPC
 		ll.add(Arrays.asList(new Object[]{null,							"000000890017fc00"}));	// Server with Directory
 		ll.add(Arrays.asList(new Object[]{null,							"00000052001bfc00"}));	// Server with SMB
 
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("790217"),	"000000a4004ffc01"}));	// Product: RHEL Client   Options: Basic FullProd Workstation  {'Workstation': 'Workstation', 'Base': 'Client'}
 		ll.add(Arrays.asList(new Object[]{null,							"000000870003fc01"}));	// Product: RHEL Client   Options: NoSLA FullProd  {'Base': 'Client'}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="InstallNumMigrateToRhsmWithInvalidInstNumberData")
 	public Object[][] getInstallNumMigrateToRhsmWithInvalidInstNumberDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInstallNumMigrateToRhsmWithInvalidInstNumberDataAsListOfLists());
 	}
 	protected List<List<Object>> getInstallNumMigrateToRhsmWithInvalidInstNumberDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		// if (!clienttasks.redhatReleaseX.equals("5")) return ll;	// prone to improperly unexecuted tests
 		if (clienttasks.redhatReleaseX.equals("6")) return ll;
 		if (clienttasks.redhatReleaseX.equals("7")) return ll;
 		
 		// due to design changes, this is a decent place to dump old commands that have been removed
 		
 		// String command, int expectedExitCode, String expectedStdout, String expectedStderr
 		ll.add(Arrays.asList(new Object[]{null, installNumTool+" -d -i 123456789012345",			1,	"Could not parse the installation number: Unsupported string length", ""}));
 		ll.add(Arrays.asList(new Object[]{null, installNumTool+" -d -i=12345678901234567",			1,	"Could not parse the installation number: Unsupported string length", ""}));
 		ll.add(Arrays.asList(new Object[]{null, installNumTool+"    --instnum 123456789X123456",	1,	"Could not parse the installation number: Not a valid hex string", ""}));
 		ll.add(Arrays.asList(new Object[]{null, installNumTool+"    --instnum=1234567890123456",	1,	"Could not parse the installation number: Checksum verification failed", ""}));
 		
 		return ll;
 	}
 	
 	
 	@DataProvider(name="RhnMigrateClassicToRhsm_Rhel5ClientDesktopVersusWorkstationData")
 	public Object[][] getRhnMigrateClassicToRhsm_Rhel5ClientDesktopVersusWorkstationDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRhnMigrateClassicToRhsm_Rhel5ClientDesktopVersusWorkstationDataAsListOfLists());
 	}
 	public List<List<Object>> getRhnMigrateClassicToRhsm_Rhel5ClientDesktopVersusWorkstationDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		// this test is only applicable on a RHEL 5Client
 		if (!clienttasks.releasever.equals("5Client")) return ll;
 		
 		// decide what product arch applies to our system
 		String arch = clienttasks.arch;	// default
 		//if (clienttasks.redhatReleaseX.equals("5") && clienttasks.arch.equals("ppc64")) arch = "ppc";	// RHEL5 only supports ppc packages, but can be run on ppc64 hardware
 		if (Arrays.asList("i386","i486","i586","i686").contains(clienttasks.arch)) arch = "i386";		// RHEL supports i386 packages, but can be run on all 32-bit arch hardware
 		
 		if (!Arrays.asList("i386","x86_64").contains(arch)) Assert.fail("RHEL 5Client should only be available on i386 and x86_64 arches (not: "+arch+").") ;
 		
 		// Object bugzilla, String rhnUsername, String rhnPassword, String rhnServer, List<String> rhnChannelsToAdd, 
 		ll.add(Arrays.asList(new Object[]{null,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	Arrays.asList(/*"rhel-"+arch+"-client-5" base channel*/),									Arrays.asList(channelsToProductCertFilenamesMap.get("rhel-"+arch+"-client-5"))})); // 68.pem
 		ll.add(Arrays.asList(new Object[]{null,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	Arrays.asList("rhel-"+arch+"-client-supplementary-5"),										Arrays.asList(channelsToProductCertFilenamesMap.get("rhel-"+arch+"-client-5"))})); // 68.pem
 		ll.add(Arrays.asList(new Object[]{null,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	Arrays.asList("rhel-"+arch+"-client-workstation-5"),										Arrays.asList(channelsToProductCertFilenamesMap.get("rhel-"+arch+"-client-workstation-5"))})); // 71.pem
 		ll.add(Arrays.asList(new Object[]{null,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	Arrays.asList("rhel-"+arch+"-client-supplementary-5","rhel-"+arch+"-client-workstation-5"),	Arrays.asList(channelsToProductCertFilenamesMap.get("rhel-"+arch+"-client-workstation-5"))})); // 71.pem
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="RhnMigrateClassicToRhsmData")
 	public Object[][] getRhnMigrateClassicToRhsmDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRhnMigrateClassicToRhsmDataAsListOfLists());
 	}
 	public List<List<Object>> getRhnMigrateClassicToRhsmDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		List<String> rhnAvailableChildChannelsPart1 = 	rhnAvailableChildChannels.subList(0, rhnAvailableChildChannels.size()/2);
 		List<String> rhnAvailableChildChannelsPart2 = 	rhnAvailableChildChannels.subList(rhnAvailableChildChannels.size()/2,rhnAvailableChildChannels.size());
 
 		String noServiceLevel = String.valueOf(rhnServiceLevels.size()+1);	// predict the index choice for "No service level preference"
 
 		// Object bugzilla, String rhnUsername, String rhnPassword, String rhnServer, List<String> rhnChannelsToAdd, String options, String serviceLevel, List<String> expectedProductCertFilenames
 		ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"-n",		null,	null}));
 		ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels,		"-n",		null,	null}));
 		//ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("818786"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels,	"-n -f",	null,	null}));
 		ll.add(Arrays.asList(new Object[]{null /* AVOIDS BUG 818786 */,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannelsPart1,	"-n -f",	null,	null}));
 		ll.add(Arrays.asList(new Object[]{null /* AVOIDS BUG 818786 */,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannelsPart2,	"-n -f",	null,	null}));
 
 		ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"",			noServiceLevel,	null}));
 		ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels,		"",			areAllChannelsMapped(rhnAvailableChildChannels)?noServiceLevel:null,	null}));
 		//ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("818786"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels,	"-c -f",	null,	null}));
 		ll.add(Arrays.asList(new Object[]{null /* AVOIDS BUG 818786 */,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannelsPart1,	"-f",		noServiceLevel,	null}));
 		ll.add(Arrays.asList(new Object[]{null /* AVOIDS BUG 818786 */,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannelsPart2,	"-f",		noServiceLevel,	null}));
 
 		// test the service levels too
 		for (String serviceLevel : rhnServiceLevels) {
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("840169"),							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannelsPart1,	"-f", serviceLevel,	null}));	
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"840169","841961"}),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannelsPart2,	"-f", randomizeCaseOfCharactersInString(serviceLevel),	null}));	
 		}
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="RhnMigrateClassicToRhsmUsingProxyServerData")
 	public Object[][] getRhnMigrateClassicToRhsmUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRhnMigrateClassicToRhsmUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getRhnMigrateClassicToRhsmUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		List<String> rhnAvailableChildChannelsPart1 = 	rhnAvailableChildChannels.subList(0, rhnAvailableChildChannels.size()/2);
 		List<String> rhnAvailableChildChannelsPart2 = 	rhnAvailableChildChannels.subList(rhnAvailableChildChannels.size()/2,rhnAvailableChildChannels.size());
 
 		String noServiceLevel = String.valueOf(rhnServiceLevels.size()+1);	// predict the index choice for "No service level preference"
 
 		String basicauthproxyUrl = String.format("%s:%s", sm_basicauthproxyHostname,sm_basicauthproxyPort); basicauthproxyUrl = basicauthproxyUrl.replaceAll(":$", "");
 		String noauthproxyUrl = String.format("%s:%s", sm_noauthproxyHostname,sm_noauthproxyPort); noauthproxyUrl = noauthproxyUrl.replaceAll(":$", "");
 		
 		// Object bugzilla, String rhnUsername, String rhnPassword, String rhnServer, List<String> rhnChannelsToAdd, String options, List<String> expectedProductCertFilenames, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex
 		
 		// basic auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"--no-auto",			null,		sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicAuthProxyRunner,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("798015"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"--no-auto",			null,		"http://"+sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicAuthProxyRunner,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		//ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("818786"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels,		"--no-auto --force",	null,		sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicAuthProxyRunner,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{null /* AVOIDS BUG 818786 */,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannelsPart1,	"--no-auto --force",	null,		sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicAuthProxyRunner,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{null /* AVOIDS BUG 818786 */,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannelsPart2,	"--no-auto --force",	null,		sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicAuthProxyRunner,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		
 		// no auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"--no-auto",			null,		sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noAuthProxyRunner,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("798015"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"--no-auto",			null,		"http://"+sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noAuthProxyRunner,	sm_noauthproxyLog,		"Connect"}));
 		//ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("818786"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels,		"--no-auto --force",	null,		sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noAuthProxyRunner,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{null /* AVOIDS BUG 818786 */,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannelsPart1,	"--no-auto --force",	null,		sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noAuthProxyRunner,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{null /* AVOIDS BUG 818786 */,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannelsPart2,	"--no-auto --force",	null,		sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noAuthProxyRunner,	sm_noauthproxyLog,		"Connect"}));
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="RhnChannelFromProductBaselineData")
 	public Object[][] getRhnChannelFromProductBaselineDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getRhnChannelFromProductBaselineDataAsListOfLists());
 	}
 	public List<List<Object>> getRhnChannelFromProductBaselineDataAsListOfLists() throws JSONException {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		// Reference: https://engineering.redhat.com/trac/rcm/wiki/Projects/CDNBaseline
 
 		// THE JSON LOOKS LIKE THIS...
 		//	[
 		//		{
 		//			"Content Sets": [
 		//				{
 		//					"Label": "rhel-hpn-for-rhel-6-server-source-rpms", 
 		//					"Repos": [
 		//						{
 		//							"Relative URL": "/content/dist/rhel/server/6/6.1/i386/hpn/source/SRPMS"
 		//						}, 
 		//						{
 		//							"Relative URL": "/content/dist/rhel/server/6/6.2/x86_64/hpn/source/SRPMS"
 		//						}
 		//					]
 		//				}
 		//			], 
 		//			"Name": "Red Hat Enterprise Linux High Performance Networking (for RHEL Server)", 
 		//			"Product ID": "132", 
 		//			"RHN Channels": [
 		//				"rhel-x86_64-server-hpn-6", 
 		//				"rhel-x86_64-server-hpn-6-beta-debuginfo", 
 		//				"rhel-x86_64-server-hpn-6-beta", 
 		//				"rhel-x86_64-server-hpn-6-debuginfo"
 		//			]
 		//		}
 		//	]
         
 		// get the rhnProductBaselineFile from the release engineering team
 		// String sm_cdnProductBaselineUrl = "http://git.app.eng.bos.redhat.com/?p=rcm/rhn-definitions.git;a=blob_plain;f=cdn/product-baseline.json;hb=refs/heads/master";
 		//File cdnProductBaselineFile= new File("/tmp/product-baseline.json");
 		//log.info("Fetching the most current "+cdnProductBaselineFile.getName()+" file.");
 		//RemoteFileTasks.runCommandAndAssert(client,"wget -O "+cdnProductBaselineFile+" --no-check-certificate \""+sm_cdnProductBaselineUrl+"\"",Integer.valueOf(0),null,"."+cdnProductBaselineFile+". saved");
 		//client.runCommandAndWaitWithoutLogging("cat "+cdnProductBaselineFile);
 		client.runCommandAndWaitWithoutLogging("cat "+clienttasks.rhnDefinitionsDir+sm_rhnDefinitionsProductBaselineFile);
 		JSONArray jsonProducts = new JSONArray(client.getStdout());	
 		for (int p = 0; p < jsonProducts.length(); p++) {
 			JSONObject jsonProduct = (JSONObject) jsonProducts.get(p);
 			String productName = jsonProduct.getString("Name");
 			String productId = jsonProduct.getString("Product ID");
 			JSONArray jsonRhnChannels = jsonProduct.getJSONArray("RHN Channels");
 			
 			// process each of the RHN Channels
 			for (int r=0; r<jsonRhnChannels.length(); r++) {
 				String rhnChannel = jsonRhnChannels.getString(r);
 				
 				// store the rhnChannel in the cdnProductBaselineChannelMap
 				if (cdnProductBaselineChannelMap.containsKey(rhnChannel)) {
 					if (!cdnProductBaselineChannelMap.get(rhnChannel).contains(productId)) {
 						cdnProductBaselineChannelMap.get(rhnChannel).add(productId);
 					}
 				} else {
 					List<String> productIds = new ArrayList<String>(); productIds.add(productId);
 					cdnProductBaselineChannelMap.put(rhnChannel, productIds);
 				}
 		
 				// filter out all RHN Channels not associated with this release  (e.g., assume that an rhn channel containing "-5-" or ends in "-5" is only applicable to rhel5 
 				if (!(rhnChannel.contains("-"+clienttasks.redhatReleaseX+"-") || rhnChannel.endsWith("-"+clienttasks.redhatReleaseX))) continue;
 				
 				// skip on these RHN Channels that slip through this ^ filter
 				// [root@jsefler-onprem-5server tmp]# grep jboss /tmp/product-baseline.json | grep -v Label
 	            // "rhel-x86_64-server-6-rhevm-3-jboss-5", 
 	            // "rhel-x86_64-server-6-rhevm-3-jboss-5-beta", 
 	            // "rhel-x86_64-server-6-rhevm-3-jboss-5-debuginfo", 
 	            // "rhel-x86_64-server-6-rhevm-3-jboss-5-beta-debuginfo"
 				List<String> rhnChannelExceptions = Arrays.asList("rhel-x86_64-server-6-rhevm-3-jboss-5","rhel-x86_64-server-6-rhevm-3-jboss-5-beta","rhel-x86_64-server-6-rhevm-3-jboss-5-debuginfo","rhel-x86_64-server-6-rhevm-3-jboss-5-beta-debuginfo");
 				if (rhnChannelExceptions.contains(rhnChannel) && !clienttasks.redhatReleaseX.equals(/*"5"*/"6")) continue;
 				
 				// bugzillas
 				Object bugzilla = null;
 				if (rhnChannel.contains("-rhev-agent-") && clienttasks.redhatReleaseX.equals("5")/* && channelsToProductCertFilenamesMap.get(rhnChannel).equalsIgnoreCase("none")*/) { 
 					// Bug 786278 - RHN Channels for -rhev- and -vt- in the channel-cert-mapping.txt are not mapped to a productId
 					bugzilla = new BlockedByBzBug("786278");
 				}
 				if (rhnChannel.contains("-vt-")/* && channelsToProductCertFilenamesMap.get(rhnChannel).equalsIgnoreCase("none")*/) { 
 					// Bug 786278 - RHN Channels for -rhev- and -vt- in the channel-cert-mapping.txt are not mapped to a productId
 					bugzilla = new BlockedByBzBug("786278");
 				}
 				if (rhnChannel.startsWith("rhel-i386-rhev-agent-") /* && channelsToProductCertFilenamesMap.get(rhnChannel).equalsIgnoreCase("none")*/) { 
 					// Bug 816364 - channel-cert-mapping.txt is missing a mapping for product 150 "Red Hat Enterprise Virtualization" on i386
 					bugzilla = new BlockedByBzBug("816364");
 				}
 				if (rhnChannel.endsWith("-beta") && clienttasks.redhatReleaseX.equals("5")/* && channelsToProductCertFilenamesMap.get(rhnChannel).equalsIgnoreCase("none")*/) { 
 					// Bug 786203 - all RHN *beta Channels in channel-cert-mapping.txt are mapped to "none" instead of valid productId
 					bugzilla = new BlockedByBzBug("786203");
 				}			
 				if (rhnChannel.endsWith("-debuginfo") && clienttasks.redhatReleaseX.equals("5")) { 
 					// Bug 786140 - RHN Channels for "*debuginfo" are missing from the channel-cert-mapping.txt 
 					bugzilla = new BlockedByBzBug("786140");
 				}
 				if (rhnChannel.startsWith("rhel-x86_64-server-optional-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-server-sfs-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-server-ha-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-server-rs-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-rhevh") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-rhevm-3") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-rhevm-3-jboss-5") ||
 					rhnChannel.startsWith("rhel-x86_64-server-sjis-6") ||
 					rhnChannel.startsWith("rhel-x86_64-server-sap-6") ||
 					rhnChannel.startsWith("rhel-x86_64-server-lb-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-workstation-sfs-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-workstation-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-workstation-optional-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-rhev-mgmt-agent-6") ||
 					rhnChannel.startsWith("rhel-i386-server-6-cf-tools-1") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-cf-tools-1") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-cf-ae-1") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-cf-ce-1") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-cf-se-1") ||
 					rhnChannel.startsWith("sam-rhel-x86_64-server-6-htb") || rhnChannel.startsWith("sam-rhel-x86_64-server-6-beta")) { 
 					// Bug 799152 - subscription-manager-migration-data is missing some product certs for RHN Channels in product-baseline.json
 					bugzilla = new BlockedByBzBug("799152");
 				}
 				if (rhnChannel.equals("rhel-s390x-server-6") ||
 					rhnChannel.equals("rhel-s390x-server-optional-6") ||
 					rhnChannel.equals("rhel-s390x-server-supplementary-6")) { 
 					// Bug 799103 - no mapping for s390x product cert included in the subscription-manager-migration-data
 					bugzilla = new BlockedByBzBug("799103");
 				}
 				if (rhnChannel.equals("sam-rhel-x86_64-server-6") ||
 					rhnChannel.equals("sam-rhel-x86_64-server-6-debuginfo")) { 
 					// Bug 815433 - sam-rhel-x86_64-server-6-beta channel mapping needs replacement in channel-cert-mapping.txt 
 					bugzilla = new BlockedByBzBug("815433");
 				}
 				if (productId.equals("167")) {
 					// Bug 811633 - channel-cert-mapping.txt is missing a mapping for product 167 "Red Hat CloudForms"
 					bugzilla = new BlockedByBzBug("811633");
 				}
 				if (productId.equals("183") || productId.equals("184") || productId.equals("185")) {
 					// Bug 825603 - channel-cert-mapping.txt is missing a mapping for JBoss product ids 183,184,185
 					bugzilla = new BlockedByBzBug("825603");
 				}
 				if (rhnChannel.contains("-dts-")) { 
 					// Bug 820749 - channel-cert-mapping.txt is missing a mapping for product "Red Hat Developer Toolset"
 					bugzilla = new BlockedByBzBug("820749");
 				}
 				if (productId.equals("181")) {
 					// Bug 840148 - missing product cert corresponding to "Red Hat EUCJP Support (for RHEL Server)"
 					bugzilla = new BlockedByBzBug("840148");
 				}
 				
 				// Object bugzilla, String productBaselineRhnChannel, String productBaselineProductId, String productBaselineProductName
 				ll.add(Arrays.asList(new Object[]{bugzilla,	rhnChannel,	productId,	productName}));
 			}
 		}
 		
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getRhnClassicBaseAndAvailableChildChannelsData")
 	public Object[][] getRhnClassicBaseAndAvailableChildChannelsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRhnClassicBaseAndAvailableChildChannelsDataAsListOfLists());
 	}
 	protected List<List<Object>> getRhnClassicBaseAndAvailableChildChannelsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		// add the base channel
 		if (rhnBaseChannel!=null) ll.add(Arrays.asList(new Object[]{null,	rhnBaseChannel}));
 		
 		// add the child channels
 		for (String rhnAvailableChildChannel : rhnAvailableChildChannels) {
 			
 			// bugzillas
 			Object bugzilla = null;
 			if (rhnAvailableChildChannel.matches("sam-rhel-.+-server-6-beta.*")) {	// sam-rhel-x86_64-server-6-beta-debuginfo
 				// Bug 819092 - channels for sam-rhel-<ARCH>-server-6-beta-* are not yet mapped to product certs in rcm/rhn-definitions.git
 				bugzilla = new BlockedByBzBug("819092");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-rhui-2(-.*|$)")) {	// rhel-x86_64-server-6-rhui-2-debuginfo
 				// Bug 819089 - channels for rhel-<ARCH>-rhui-2-* are not yet mapped to product certs in rcm/rhn-definitions.git
 				bugzilla = new BlockedByBzBug("819089");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-6-mrg-.+")) {	// rhel-x86_64-server-6-mrg-grid-execute-2-debuginfo rhel-x86_64-server-6-mrg-messaging-2-debuginfo
 				// Bug 819088 - channels for rhel-<ARCH>-server-6-mrg-* are not yet mapped to product certs in rcm/rhn-definitions.git 
 				bugzilla = new BlockedByBzBug("819088");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-hpc-node-6-mrg-.*")) {	// rhel-x86_64-hpc-node-6-mrg-grid-execute-2  rhel-x86_64-hpc-node-6-mrg-grid-execute-2-debuginfo  rhel-x86_64-hpc-node-6-mrg-management-2  rhel-x86_64-hpc-node-6-mrg-management-2-debuginfo
 				// Bug 825608 - channels for rhel-<ARCH>-hpc-node-6-mrg-* are not yet mapped to product certs in rcm/rhn-definitions.git
 				bugzilla = new BlockedByBzBug("825608");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-v2vwin-6(-.*|$)")) {	// rhel-x86_64-server-v2vwin-6-beta-debuginfo
 				// Bug 817791 - v2vwin content does not exist in CDN
 				bugzilla = new BlockedByBzBug("817791");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-fastrack-6(-.*|$)")) {	// rhel-x86_64-server-ha-fastrack-6-debuginfo
 				// Bug 818202 - Using subscription-manager, some repositories like fastrack are not available as they are in rhn.
 				bugzilla = new BlockedByBzBug("818202");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-eucjp-6(-.+|$)")) {	// rhel-x86_64-server-eucjp-6 rhel-x86_64-server-eucjp-6-beta etc.
 				// Bug 840148 - missing product cert corresponding to "Red Hat EUCJP Support (for RHEL Server)"
 				bugzilla = new BlockedByBzBug("840148");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-fastrack-5(-.*|$)")) {	// rhel-x86_64-server-fastrack-5 rhel-x86_64-server-fastrack-5-debuginfo
 				// Bug 818202 - Using subscription-manager, some repositories like fastrack are not available as they are in rhn.
 				bugzilla = new BlockedByBzBug("818202");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-5-cf-tools-1(-beta)?-debuginfo")) {	// rhel-x86_64-server-5-cf-tools-1-beta-debuginfo, rhel-x86_64-server-5-cf-tools-1-debuginfo
 				// Bug 840099 - debug info channels for rhel-x86_64-server-5-cf-tools are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugzilla = new BlockedByBzBug("840099");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-5-mrg-.*")) {	// rhel-x86_64-server-5-mrg-grid-1 rhel-x86_64-server-5-mrg-grid-1-beta rhel-x86_64-server-5-mrg-grid-2 rhel-x86_64-server-5-mrg-grid-execute-1 rhel-x86_64-server-5-mrg-grid-execute-1-beta rhel-x86_64-server-5-mrg-grid-execute-2 etc.
 				// Bug 840102 - channels for rhel-<ARCH>-server-5-mrg-* are not yet mapped to product certs in rcm/rcm-metadata.git 
 				bugzilla = new BlockedByBzBug("840102");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-hpc-5(-.*|$)")) {	// rhel-x86_64-server-hpc-5-beta
 				// Bug 840103 - channel for rhel-x86_64-server-hpc-5-beta is not yet mapped to product cert in rcm/rcm-metadata.git
 				bugzilla = new BlockedByBzBug("840103");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-rhev-hdk-2-5(-.+|$)")) {	// rhel-x86_64-server-rhev-hdk-2-5 rhel-x86_64-server-rhev-hdk-2-5-beta
 				// Bug 840108 - channels for rhel-<ARCH>-rhev-hdk-2-5-* are not yet mapped to product certs in rcm/rhn-definitions.git
 				bugzilla = new BlockedByBzBug("840108");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-productivity-5-beta(-.+|$)")) {	// rhel-x86_64-server-productivity-5-beta rhel-x86_64-server-productivity-5-beta-debuginfo
 				// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugzilla = new BlockedByBzBug("840136");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-rhsclient-5(-.+|$)")) {	// rhel-x86_64-server-rhsclient-5 rhel-x86_64-server-rhsclient-5-debuginfo
 				// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugzilla = new BlockedByBzBug("840136");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-xfs-5(-.+|$)")) {	// rhel-x86_64-server-xfs-5 rhel-x86_64-server-xfs-5-beta
 				// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugzilla = new BlockedByBzBug("840136");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-5-shadow(-.+|$)")) {	// rhel-x86_64-server-5-shadow-debuginfo
 				// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugzilla = new BlockedByBzBug("840136");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-eucjp-5(-.+|$)")) {	// rhel-x86_64-server-eucjp-5 rhel-x86_64-server-eucjp-5-beta etc.
 				// Bug 840148 - missing product cert corresponding to "Red Hat EUCJP Support (for RHEL Server)"
 				bugzilla = new BlockedByBzBug("840148");
 			}
 			if (rhnAvailableChildChannel.startsWith("rhx-")) {	// rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5 rhx-amanda-enterprise-backup-2.6-rhel-x86_64-server-5 etcetera
 				// Bug 840111 - various rhx channels are not yet mapped to product certs in rcm/rcm-metadata.git 
 				bugzilla = new BlockedByBzBug("840111");
 			}

 			
 			ll.add(Arrays.asList(new Object[]{bugzilla,	rhnAvailableChildChannel}));
 		}
 		
 		return ll;
 	}
 }
 
 
 
 
 // Notes ***********************************************************************
 
 // EXAMPLE FOR install-num-migrate-to-rhsm TAKEN FROM THE DEPLOYMENT GUIDE http://documentation-stage.bne.redhat.com/docs/en-US/Red_Hat_Enterprise_Linux/5/html/Deployment_Guide/rhn-install-num.html
 //	[root@jsefler-onprem-5server ~]# python /usr/lib/python2.4/site-packages/instnum.py da3122afdb7edd23
 //	Product: RHEL Client
 //	Type: Installer Only
 //	Options: Eval FullProd Workstation
 //	Allowed CPU Sockets: Unlimited
 //	Allowed Virtual Instances: Unlimited
 //	Package Repositories: Client Workstation
 //
 //	key: 14299426 'da3122'
 //	checksum: 175 'af'
 //	options: 4416 'Eval FullProd Workstation'
 //	socklimit: -1 'Unlimited'
 //	virtlimit: -1 'Unlimited'
 //	type: 2 'Installer Only'
 //	product: 1 'client'
 //
 //	{'Workstation': 'Workstation', 'Base': 'Client'}
 //
 //	da31-22af-db7e-dd23
 //	[root@jsefler-onprem-5server ~]# 
 //
 //	[root@jsefler-onprem-5server ~]# install-num-migrate-to-rhsm -d -i da3122afdb7edd23
 //	Copying /usr/share/rhsm/product/RHEL-5/Client-Workstation-x86_64-efa6382a-44c4-408b-a142-37ad4be54aa6-71.pem to /etc/pki/product/71.pem
 //	Copying /usr/share/rhsm/product/RHEL-5/Client-Client-x86_64-efe91c1c-78d7-4d19-b2fb-3c88cfc2da35-68.pem to /etc/pki/product/68.pem
 //	[root@jsefler-onprem-5server ~]# 
 //	[root@jsefler-onprem-5server ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Client-Client-x86_64-efe91c1c-78d7-4d19-b2fb-3c88cfc2da35-68.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.68.1: 
 //	                . Red Hat Enterprise Linux Desktop
 //	            1.3.6.1.4.1.2312.9.1.68.2: 
 //	                ..5.7
 //	            1.3.6.1.4.1.2312.9.1.68.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.68.4: 
 //	                ..rhel-5,rhel-5-client
 //	[root@jsefler-onprem-5server ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Client-Workstation-x86_64-efa6382a-44c4-408b-a142-37ad4be54aa6-71.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.71.1: 
 //	                .$Red Hat Enterprise Linux Workstation
 //	            1.3.6.1.4.1.2312.9.1.71.2: 
 //	                ..5.7
 //	            1.3.6.1.4.1.2312.9.1.71.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.71.4: 
 //	                .,rhel-5-client-workstation,rhel-5-workstation
 //	[root@jsefler-onprem-5server ~]# 
 	
 	
 // EXAMPLE FOR install-num-migrate-to-rhsm
 //	[root@dell-pe1855-01 ~]# ls /etc/pki/product/
 //	69.pem
 //	[root@dell-pe1855-01 ~]# cat /etc/redhat-release 
 //	Red Hat Enterprise Linux Server release 5.8 Beta (Tikanga)
 //	[root@dell-pe1855-01 ~]# openssl x509 -text -in /etc/pki/product/69.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.69.1: 
 //	                ..Red Hat Enterprise Linux Server
 //	            1.3.6.1.4.1.2312.9.1.69.2: 
 //	                ..5.8 Beta
 //	            1.3.6.1.4.1.2312.9.1.69.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.69.4: 
 //	                ..rhel-5,rhel-5-server
 //	
 //	[root@dell-pe1855-01 ~]# cat /etc/sysconfig/rhn/install-num 
 //	49af89414d147589
 //	[root@dell-pe1855-01 ~]# install-num-migrate-to-rhsm -d -i 49af89414d147589
 //	Copying /usr/share/rhsm/product/RHEL-5/Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem to /etc/pki/product/69.pem
 //	Copying /usr/share/rhsm/product/RHEL-5/Server-ClusterStorage-x86_64-66e8d727-f5aa-4e37-a04b-787fbbc3430c-90.pem to /etc/pki/product/90.pem
 //	Copying /usr/share/rhsm/product/RHEL-5/Server-Cluster-x86_64-bebfe30e-22a5-4788-8611-744ea744bdc0-83.pem to /etc/pki/product/83.pem
 //	[root@dell-pe1855-01 ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.69.1: 
 //	                ..Red Hat Enterprise Linux Server
 //	            1.3.6.1.4.1.2312.9.1.69.2: 
 //	                ..5.7
 //	            1.3.6.1.4.1.2312.9.1.69.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.69.4: 
 //	                ..rhel-5,rhel-5-server
 //	
 //	[root@dell-pe1855-01 ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Server-ClusterStorage-x86_64-66e8d727-f5aa-4e37-a04b-787fbbc3430c-90.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.90.1: 
 //	                .<Red Hat Enterprise Linux Resilient Storage (for RHEL Server)
 //	            1.3.6.1.4.1.2312.9.1.90.2: 
 //	                ..5.7
 //	            1.3.6.1.4.1.2312.9.1.90.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.90.4: 
 //	                .2rhel-5-server-clusterstorage,rhel-5-clusterstorage
 //	                
 //	[root@dell-pe1855-01 ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Server-Cluster-x86_64-bebfe30e-22a5-4788-8611-744ea744bdc0-83.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.83.1: 
 //	                .<Red Hat Enterprise Linux High Availability (for RHEL Server)
 //	            1.3.6.1.4.1.2312.9.1.83.2: 
 //	                ..5.7
 //	            1.3.6.1.4.1.2312.9.1.83.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.83.4: 
 //	                .$rhel-5-server-cluster,rhel-5-cluster
 
 	
 
 	
 // 	EXAMPLE FOR rhn-migrate-classic-to-rhsm
 //	[root@jsefler-onprem-5server ~]# rhnreg_ks -v --serverUrl=https://xmlrpc.rhn.code.stage.redhat.com/XMLRPC --username=qa@redhat.com --password=CHANGE-ME --force --norhnsd --nohardware --nopackages --novirtinfo 
 //		[root@jsefler-onprem-5server ~]# rhn-migrate-classic-to-rhsm -c
 //		RHN Username: qa@redhat.com
 //		Password: 
 //
 //		Retrieving existing RHN classic subscription information ...
 //		+----------------------------------+
 //		System is currently subscribed to:
 //		+----------------------------------+
 //		rhel-x86_64-server-5
 //
 //		List of channels for which certs are being copied
 //		rhel-x86_64-server-5
 //
 //		Product Certificates copied successfully to /etc/pki/product !!
 //
 //		Preparing to unregister system from RHN classic ...
 //		System successfully unregistered from RHN Classic.
 //
 //		Attempting to register system to Certificate-based RHN ...
 //		The system has been registered with id: 78cb5e26-3a5a-459d-848c-d5b3102a864d 
 //		System 'jsefler-onprem-5server.usersys.redhat.com' successfully registered to Certificate-based RHN.
 //
 //		Attempting to auto-subscribe to appropriate subscriptions ...
 //		Installed Product Current Status:         
 //
 //		ProductName:          	Red Hat Enterprise Linux Server
 //		Status:               	Subscribed             
 //
 //
 //		Please visit https://access.redhat.com/management/consumers/78cb5e26-3a5a-459d-848c-d5b3102a864d to view the details, and to make changes if necessary.
 //		[root@jsefler-onprem-5server ~]# subscription-manager unregister
 //		System has been un-registered.
 //		[root@jsefler-onprem-5server ~]# 
 
 	
 //	EXAMPLE FOR rhn-migrate-classic-to-rhsm
 //	[root@jsefler-onprem-5server rhn]# rhnreg_ks  --serverUrl=https://xmlrpc.rhn.code.stage.redhat.com/XMLRPC --username=qa@redhat.com --password=CHANGE-ME --force --norhnsd --nohardware --nopackages --novirtinfo
 //		ERROR: refreshing remote package list for System Profile
 //		[root@jsefler-onprem-5server rhn]# rhn-channel --list
 //		rhel-x86_64-server-5
 //		[root@jsefler-onprem-5server rhn]# rhn-channel --user=qa@redhat.com --password=CHANGE-ME --add -c  rhel-x86_64-server-5-debuginfo -c rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5
 //		[root@jsefler-onprem-5server rhn]# rhn-channel --list
 //		rhel-x86_64-server-5
 //		rhel-x86_64-server-5-debuginfo
 //		rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5
 //		[root@jsefler-onprem-5server rhn]# rhn-migrate-classic-to-rhsm --no-auto
 //		RHN Username: qa@redhat.com
 //		Password: 
 //
 //		Retrieving existing RHN classic subscription information ...
 //		+----------------------------------+
 //		System is currently subscribed to:
 //		+----------------------------------+
 //		rhel-x86_64-server-5
 //		rhel-x86_64-server-5-debuginfo
 //		rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5
 //
 //		+--------------------------------------------------+
 //		Below mentioned channels are NOT available on RHSM
 //		+--------------------------------------------------+
 //		rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5
 // ^ THESE CHANNELS ARE IN THE MAP FILE MAPPED TO none
 //
 //		+---------------------------------------------------------------------------------------+ 
 //		Unrecognized channels. Channel to Product Certificate mapping missing for these channels.
 //		+---------------------------------------------------------------------------------------+
 //		rhel-x86_64-server-5-debuginfo
 // ^ THESE CHANNELS ARE NOT IN THE MAP FILE AT ALL
 //
 //		Use --force to ignore these channels and continue the migration.
 //
 //		[root@jsefler-onprem-5server rhn]# echo $?
 //		1
 //		[root@jsefler-onprem-5server rhn]# 
 
