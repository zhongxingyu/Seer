 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  * Bug 730380 - Can not import a certificate via the cli - https://bugzilla.redhat.com/show_bug.cgi?id=730380
  * Bug 712980 - Import Certificate neglects to also import the corresponding key.pem - https://bugzilla.redhat.com/show_bug.cgi?id=712980
  * Bug 733873 - subscription-manager import --help should not use proxy options - https://bugzilla.redhat.com/show_bug.cgi?id=733873
  */
 @Test(groups={"ImportTests"})
 public class ImportTests extends SubscriptionManagerCLITestScript {
 
 	// Test methods ***********************************************************************
 	
 	
 	@Test(	description="subscription-manager: import a valid entitlement cert/key bundle and verify subscriptions are consumed",
 			groups={"blockedByBug-730380","blockedByBug-712980","AcceptanceTests"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ImportAnEntitlementCertAndKeyFromFile_Test() {
 		
 		// assemble a valid bundled cert/key certificate file
 		File importEntitlementCertFile = entitlementCertFiles.get(randomGenerator.nextInt(entitlementCertFiles.size()));
 		File importEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(importEntitlementCertFile);
 		File importCertificateFile = new File(importCertificatesDir+File.separator+importEntitlementCertFile.getName());
 		client.runCommandAndWait("cat "+importEntitlementCertFile+" "+importEntitlementKeyFile+" > "+importCertificateFile);
 		
 		// once imported, what should the entitlement cert file be?
 		File expectedEntitlementCertFile = new File (clienttasks.entitlementCertDir+File.separator+importCertificateFile.getName());
 		File expectedEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(expectedEntitlementCertFile);
 		
 		// make sure the expected entitlement files do not exist before our test and that no subscriptions are consumed
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement cert file does NOT yet exist ("+expectedEntitlementCertFile+").");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement key file does NOT yet exist ("+expectedEntitlementKeyFile+").");
 		Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions().size(), 0, "Should not be consuming any subscriptions before the import.");
 
 		// show the contents of the file about to be imported
 		log.info("Following is the contents of the certificate file about to be imported...");
 		client.runCommandAndWait("cat "+importCertificateFile);
 		
 		// attempt an entitlement cert import from a valid bundled file
 		clienttasks.importCertificate(importCertificateFile.getPath());
 		
 		// verify that the expectedEntitlementCertFile now exists
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),1,"After attempting the import, the expected destination for the entitlement cert file should now exist ("+expectedEntitlementCertFile+").");
 
 		// verify that the expectedEntitlementKeyFile also exists
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),1,"After attempting the import, the expected destination for the entitlement key file should now exist ("+expectedEntitlementKeyFile+").");
 		
 		// assert that the contents of the imported files match the originals
 		log.info("Asserting that the imported entitlement cert file contents match the original...");
 		RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementCertFile+" "+importEntitlementCertFile, 0);
 		log.info("Asserting that the imported entitlement key file contents match the original...");
 		RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementKeyFile+" "+importEntitlementKeyFile, 0);
 
 		// finally verify that we are now consuming subscriptions
 		Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().size()>0, "After importing a valid certificate, we should be consuming subscriptions.");
 	}
 	
 	
 	@Test(	description="subscription-manager: import a certificate from a file (saved as a different name) and verify subscriptions are consumed",
 			groups={"blockedByBug-734606"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ImportAnEntitlementCertAndKeyFromASavedAsFile_Test() {
 		
 		// assemble a valid bundled cert/key certificate file
 		File importEntitlementCertFile = entitlementCertFiles.get(randomGenerator.nextInt(entitlementCertFiles.size()));
 		File importEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(importEntitlementCertFile);
 		
 		// loop through the following simulations of a renamed certificate...
 		for (String filename : new String[]{"certificate.pem", "certificate", importEntitlementCertFile.getName().split("\\.")[0]}) {
 			
 			File importCertificateFile = new File(importCertificatesDir+File.separator+filename);
 			client.runCommandAndWait("cat "+importEntitlementCertFile+" "+importEntitlementKeyFile+" > "+importCertificateFile);
 			
 			// once imported, what should the entitlement cert file be?
			File expectedEntitlementCertFile = new File (clienttasks.entitlementCertDir+File.separator+importCertificateFile.getName());
 			File expectedEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(expectedEntitlementCertFile);
 			
 			// make sure the expected entitlement files do not exist before our test and that no subscriptions are consumed
 			clienttasks.removeAllCerts(false, true);
 			Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement cert file does NOT yet exist ("+expectedEntitlementCertFile+").");
 			Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement key file does NOT yet exist ("+expectedEntitlementKeyFile+").");
 			Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions().size(), 0, "Should not be consuming any subscriptions before the import.");
 
 			// show the contents of the file about to be imported
 			log.info("Following is the contents of the certificate file about to be imported...");
 			client.runCommandAndWait("cat "+importCertificateFile);
 			
 			// attempt an entitlement cert import from a valid bundled file
 			clienttasks.importCertificate(importCertificateFile.getPath());
 			
 			// verify that the expectedEntitlementCertFile now exists
 			Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),1,"After attempting the import, the expected destination for the entitlement cert file should now exist ("+expectedEntitlementCertFile+").");
 
 			// verify that the expectedEntitlementKeyFile also exists
 			Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),1,"After attempting the import, the expected destination for the entitlement key file should now exist ("+expectedEntitlementKeyFile+").");
 			
 			// assert that the contents of the imported files match the originals
 			log.info("Asserting that the imported entitlement cert file contents match the original...");
 			RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementCertFile+" "+importEntitlementCertFile, 0);
 			log.info("Asserting that the imported entitlement key file contents match the original...");
 			RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementKeyFile+" "+importEntitlementKeyFile, 0);
 
 			// finally verify that we are now consuming subscriptions
 			Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().size()>0, "After importing a valid certificate, we should be consuming subscriptions.");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager: import a valid entitlement key/cert bundle and verify subscriptions are consumed",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ImportAnEntitlementKeyAndCertFromFile_Test() {
 		
 		// assemble a valid bundled cert/key certificate file
 		File importEntitlementCertFile = entitlementCertFiles.get(randomGenerator.nextInt(entitlementCertFiles.size()));
 		File importEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(importEntitlementCertFile);
 		File importCertificateFile = new File(importCertificatesDir+File.separator+importEntitlementCertFile.getName());
 		client.runCommandAndWait("cat "+importEntitlementKeyFile+" "+importEntitlementCertFile+" > "+importCertificateFile);
 		
 		// once imported, what should the entitlement cert file be?
 		File expectedEntitlementCertFile = new File (clienttasks.entitlementCertDir+File.separator+importCertificateFile.getName());
 		File expectedEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(expectedEntitlementCertFile);
 		
 		// make sure the expected entitlement files do not exist before our test and that no subscriptions are consumed
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement cert file does NOT yet exist ("+expectedEntitlementCertFile+").");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement key file does NOT yet exist ("+expectedEntitlementKeyFile+").");
 		Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions().size(), 0, "Should not be consuming any subscriptions before the import.");
 
 		// show the contents of the file about to be imported
 		log.info("Following is the contents of the certificate file about to be imported...");
 		client.runCommandAndWait("cat "+importCertificateFile);
 		
 		// attempt an entitlement cert import from a valid bundled file
 		clienttasks.importCertificate(importCertificateFile.getPath());
 		
 		// verify that the expectedEntitlementCertFile now exists
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),1,"After attempting the import, the expected destination for the entitlement cert file should now exist ("+expectedEntitlementCertFile+").");
 
 		// verify that the expectedEntitlementKeyFile also exists
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),1,"After attempting the import, the expected destination for the entitlement key file should now exist ("+expectedEntitlementKeyFile+").");
 		
 		// assert that the contents of the imported files match the originals
 		log.info("Asserting that the imported entitlement cert file contents match the original...");
 		RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementCertFile+" "+importEntitlementCertFile, 0);
 		log.info("Asserting that the imported entitlement key file contents match the original...");
 		RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementKeyFile+" "+importEntitlementKeyFile, 0);
 
 		// finally verify that we are now consuming subscriptions
 		Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().size()>0, "After importing a valid certificate, we should be consuming subscriptions.");
 	}
 	
 	
 	@Test(	description="subscription-manager: attempt an entitlement cert import from a file in the current directory",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ImportAnEntitlementCertAndKeyFromFileInCurrentDirectory_Test() {
 		
 		// assemble a valid bundled cert/key certificate file
 		File importEntitlementCertFile = entitlementCertFiles.get(randomGenerator.nextInt(entitlementCertFiles.size()));
 		File importEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(importEntitlementCertFile);
 		File importCertificateFile = new File(importCertificatesDir+File.separator+importEntitlementCertFile.getName());
 		client.runCommandAndWait("cat "+importEntitlementCertFile+" "+importEntitlementKeyFile+" > "+importCertificateFile);
 
 		// once imported, what should the entitlement cert file be?
 		File expectedEntitlementCertFile = new File (clienttasks.entitlementCertDir+File.separator+importCertificateFile.getName());
 		File expectedEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(expectedEntitlementCertFile);
 		
 		// make sure the expected entitlement files do not exist before our test
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement cert file does NOT yet exist ("+expectedEntitlementCertFile+").");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement key file does NOT yet exist ("+expectedEntitlementKeyFile+").");
 		Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions().size(), 0, "Should not be consuming any subscriptions before the import.");
 
 		// show the contents of the file about to be imported
 		log.info("Following is the contents of the certificate file about to be imported...");
 		client.runCommandAndWait("cd "+importCertificateFile.getParent()+"; cat "+importCertificateFile.getName());
 		
 		// attempt an entitlement cert import from a valid file in the current directory
 		SSHCommandResult importResult = client.runCommandAndWait("cd "+importCertificateFile.getParent()+"; "+clienttasks.command+" import --certificate "+importCertificateFile.getName());
 		Assert.assertEquals(importResult.getStdout().trim(), "Successfully imported certificate "+importCertificateFile.getName());
 		
 		// verify that the expectedEntitlementCertFile now exists
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),1,"After attempting the import, the expected destination for the entitlement cert file should now exist ("+expectedEntitlementCertFile+").");
 
 		// verify that the expectedEntitlementKeyFile also exists
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),1,"After attempting the import, the expected destination for the entitlement key file should now exist ("+expectedEntitlementKeyFile+").");
 
 		// assert that the contents of the imported files match the originals
 		log.info("Asserting that the imported entitlement cert file contents match the original...");
 		RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementCertFile+" "+importEntitlementCertFile, 0);
 		log.info("Asserting that the imported entitlement key file contents match the original...");
 		RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementKeyFile+" "+importEntitlementKeyFile, 0);
 		
 		// finally verify that we are now consuming subscriptions
 		Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().size()>0, "After importing a valid certificate, we should be consuming subscriptions.");
 	}
 
 	
 
 	
 	
 	@Test(	description="subscription-manager: import a certificate for a future entitlement",
 			groups={},
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void ImportACertificateForAFutureEntitlement_Test() throws Exception {
 		
 		if (futureEntitlementCertFile==null) throw new SkipException("Could not generate an entitlement certificate for a future subscription.");
 		
 		// create a import file for the future entitlement
 		File importEntitlementCertFile = futureEntitlementCertFile;
 		File importEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(importEntitlementCertFile);
 		File importCertificateFile = new File(importCertificatesDir+File.separator+importEntitlementCertFile.getName());
 		client.runCommandAndWait("cat "+importEntitlementCertFile+" "+importEntitlementKeyFile+" > "+importCertificateFile);
 		
 		// once imported, what should the entitlement cert file be?
 		File expectedEntitlementCertFile = new File (clienttasks.entitlementCertDir+File.separator+importCertificateFile.getName());
 		File expectedEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(expectedEntitlementCertFile);
 		
 		// make sure the expected entitlement files do not exist before our test and that no subscriptions are consumed
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement cert file does NOT yet exist ("+expectedEntitlementCertFile+").");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement key file does NOT yet exist ("+expectedEntitlementKeyFile+").");
 		Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions().size(), 0, "Should not be consuming any subscriptions before the import.");
 
 		// show the contents of the file about to be imported
 		log.info("Following is the contents of the certificate file about to be imported...");
 		client.runCommandAndWait("cat "+importCertificateFile);
 		
 		// attempt an entitlement cert import from a valid bundled file
 		clienttasks.importCertificate(importCertificateFile.getPath());
 		
 		// verify that the expectedEntitlementCertFile now exists
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),1,"After attempting the import, the expected destination for the entitlement cert file should now exist ("+expectedEntitlementCertFile+").");
 
 		// verify that the expectedEntitlementKeyFile also exists
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),1,"After attempting the import, the expected destination for the entitlement key file should now exist ("+expectedEntitlementKeyFile+").");
 		
 		// assert that the contents of the imported files match the originals
 		log.info("Asserting that the imported entitlement cert file contents match the original...");
 		RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementCertFile+" "+importEntitlementCertFile, 0);
 		log.info("Asserting that the imported entitlement key file contents match the original...");
 		RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementKeyFile+" "+importEntitlementKeyFile, 0);
 
 		// finally verify that we are now consuming subscriptions
 		List<ProductSubscription> consumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions(); 
 		Assert.assertTrue(consumedProductSubscriptions.size()>0, "After importing a valid certificate, we should be consuming subscriptions.");
 		
 		// assert that the consumed subscriptions begin in the future
 		Calendar now = new GregorianCalendar();	now.setTimeInMillis(System.currentTimeMillis());
 		for (ProductSubscription productSubscription : consumedProductSubscriptions) {
 			Assert.assertTrue(productSubscription.startDate.after(now), "The product subscription consumed from the imported future entitlement certificate begins in the future.  ProductSubscription: "+productSubscription);
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager: attempt an entitlement cert import from a file containing the cert only (negative test)",
 			groups={"blockedByBug-735226"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptAnEntitlementImportFromACertOnlyFile_Test() {
 		
 		// choose an entitlement cert file
 		File importEntitlementCertFile = entitlementCertFiles.get(randomGenerator.nextInt(entitlementCertFiles.size()));
 
 		//TODO UNCOMMENT THIS LINE, BLOCK TEST WITH blockedByBug-735226 AND DELETE THE REST OF THIS METHOD AFTER BUG IS RESOLVED
 		attemptAnEntitlementImportFromAnInvalidFile_Test(importEntitlementCertFile);
 		if (true) return;
 		
 		File importEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(importEntitlementCertFile);
 		
 		// once imported, what should the entitlement cert file be?
 		File expectedEntitlementCertFile = new File (clienttasks.entitlementCertDir+File.separator+importEntitlementCertFile.getName());
 		File expectedEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(expectedEntitlementCertFile);
 		
 		// make sure the expected entitlement files do not exist before our test
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement cert file does NOT yet exist ("+expectedEntitlementCertFile+").");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement key file does NOT yet exist ("+expectedEntitlementKeyFile+").");
 
 		// show the contents of the file about to be imported
 		log.info("Following is the contents of the certificate file about to be imported...");
 		client.runCommandAndWait("cat "+importEntitlementCertFile);
 		
 		// attempt an entitlement cert import from a file containing the cert only
 		clienttasks.importCertificate(importEntitlementCertFile.getPath());
 		
 		// verify that the expectedEntitlementCertFile now exists
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),1,"After attempting the import, the expected destination for the entitlement cert file should now exist ("+expectedEntitlementCertFile+").");
 
 		// verify that the expectedEntitlementKeyFile does NOT exist
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),0,"After attempting the import, the expected destination for the entitlement key file should NOT exist ("+expectedEntitlementKeyFile+") since there was no key in the import file.");
 		
 		// assert that the contents of the imported files match the originals
 		log.info("Asserting that the imported entitlement cert file contents match the original...");
 		RemoteFileTasks.runCommandAndAssert(client, "diff -w "+expectedEntitlementCertFile+" "+importEntitlementCertFile, 0);
 		
 		// finally verify that we are still NOT consuming subscriptions
 		Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions().size(),0, "After importing a certificate that is missing a key, there should be no consuming subscriptions listed.");
 	}
 	
 	
 	@Test(	description="subscription-manager: attempt an entitlement cert import from a file containing only a key (negative test)",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptAnEntitlementImportFromAKeyOnlyFile_Test() {
 		File importEntitlementCertFile = entitlementCertFiles.get(randomGenerator.nextInt(entitlementCertFiles.size()));
 		File importEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(importEntitlementCertFile);
 		attemptAnEntitlementImportFromAnInvalidFile_Test(importEntitlementKeyFile);
 	}
 	
 	
 	@Test(	description="subscription-manager: attempt an entitlement cert import using an identity cert (negative test)",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptAnEntitlementImportFromAConsumerCertFile_Test() {
 		File invalidCertificate = consumerCertFile;
 		attemptAnEntitlementImportFromAnInvalidFile_Test(invalidCertificate);
 	}
 	
 	
 	@Test(	description="subscription-manager: attempt an entitlement cert import using non existent file (negative test)",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptAnEntitlementImportFromNonExistentFile_Test() {
 		String invalidCertificate = "/tmp/nonExistentFile.pem";
 		attemptAnEntitlementImportFromAnInvalidFile_Test(new File(invalidCertificate));
 	}
 	
 	
 	@Test(	description="subscription-manager: import multiple valid certificates and verify subscriptions are consumed",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ImportMultipleCertificates_Test() {
 		
 		// create a list of valid import certificate files
 		List<String> importCertificates = new ArrayList<String>();
 		for (File entitlementCertFile : entitlementCertFiles) {
 			File entitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(entitlementCertFile);
 			File importCertificateFile = new File(importCertificatesDir+File.separator+entitlementCertFile.getName());
 			client.runCommandAndWait("cat "+entitlementCertFile+" "+entitlementKeyFile+" > "+importCertificateFile);
 			importCertificates.add(importCertificateFile.getPath());
 		}
 		
 		// import all the certificates at once
 		SSHCommandResult importResult = clienttasks.importCertificate(importCertificates);
 		
 		// assert that all of the actual imported certificates were extracted
 		for (String importCertificate : importCertificates) {
 			File expectedEntitlementCertFile = new File (clienttasks.entitlementCertDir+File.separator+new File(importCertificate).getName());
 			File expectedEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(expectedEntitlementCertFile);
 
 			// verify that the expectedEntitlementCertFile now exists
 			Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),1,"After attempting multiple certificate import, the expected destination for the entitlement cert file should now exist ("+expectedEntitlementCertFile+").");
 
 			// verify that the expectedEntitlementKeyFile also exists
 			Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),1,"After attempting multiple certificate import, the expected destination for the entitlement key file should now exist ("+expectedEntitlementKeyFile+").");
 		}
 	}
 	
 	@Test(	description="subscription-manager: import multiple certificates including invalid ones",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ImportMultipleCertificatesIncludingInvalidCertificates_Test() {
 		
 		// create a list of valid import certificate files
 		List<String> importCertificates = new ArrayList<String>();
 		for (File entitlementCertFile : entitlementCertFiles) {
 			File entitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(entitlementCertFile);
 			File importCertificateFile = new File(importCertificatesDir+File.separator+entitlementCertFile.getName());
 			client.runCommandAndWait("cat "+entitlementCertFile+" "+entitlementKeyFile+" > "+importCertificateFile);
 			importCertificates.add(importCertificateFile.getPath());
 		}
 		// insert some invalid certificates to the list
 		List<String> invalidCertificates = new ArrayList<String>();
 		invalidCertificates.add("/tmp/nonExistentFile.pem");
 		invalidCertificates.add(consumerCertFile.getPath());
 		invalidCertificates.add(clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(entitlementCertFiles.get(0)).getPath());
 		importCertificates.remove(importCertificates.indexOf(new File(importCertificatesDir+File.separator+entitlementCertFiles.get(0).getName()).getPath()));
 		for (String invalidCertificate : invalidCertificates) {
 			importCertificates.add(randomGenerator.nextInt(importCertificates.size()), invalidCertificate);
 		}
 		
 		// import all the certificates at once without asserting the success
 		SSHCommandResult importResult = clienttasks.importCertificate_(importCertificates);
 		
 		// assert that all of the actual imported certificates were extracted (or not)
 		for (String importCertificate : importCertificates) {
 			File expectedEntitlementCertFile = new File (clienttasks.entitlementCertDir+File.separator+new File(importCertificate).getName());
 			File expectedEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(expectedEntitlementCertFile);
 
 			if (invalidCertificates.contains(importCertificate)) {
 				String errorMsg = new File(importCertificate).getName()+" is not a valid certificate file. Please use a valid certificate.";
 				Assert.assertTrue(importResult.getStdout()/*TODO CHANGE TO stderr after open bz fix */.contains(errorMsg),"The result from the import command contains expected message: "+errorMsg);		
 
 				// verify that the expectedEntitlementCertFile does not exist
 				Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),0,"After attempting multiple certificate import, the expected destination for the invalid entitlement cert file should NOT exist ("+expectedEntitlementCertFile+").");
 				
 				// verify that the expectedEntitlementKeyFile  does not exist
 				Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),0,"After attempting multiple certificate import, the expected destination for the invalid entitlement key file should NOT exist ("+expectedEntitlementKeyFile+").");
 
 			} else {
 				String successMsg = "Successfully imported certificate "+new File(importCertificate).getName();
 				Assert.assertTrue(importResult.getStdout().contains(successMsg),"The result from the import command contains expected message: "+successMsg);		
 				
 				// verify that the expectedEntitlementCertFile now exists
 				Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),1,"After attempting multiple certificate import, the expected destination for the entitlement cert file should now exist ("+expectedEntitlementCertFile+").");
 				
 				// verify that the expectedEntitlementKeyFile also exists
 				Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),1,"After attempting multiple certificate import, the expected destination for the entitlement key file should now exist ("+expectedEntitlementKeyFile+").");
 			}
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager: unsubscribe from an imported entitlement (while not registered)",
 			groups={"blockedByBug-735338"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ImportACertificateAndUnsubscribeWhileNotRegistered_Test() {
 		
 		// make sure we are NOT registered
 		clienttasks.unregister(null,null,null);
 		
 		// make sure we are not consuming
 		List<ProductSubscription> productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertEquals(productSubscriptions.size(), 0, "We should NOT be consuming any entitlements.");
 
 		// import from a valid certificate
 		clienttasks.importCertificate(getValidImportCertificate().getPath());
 		
 		// get one of the now consumed ProductSubscriptions
 		productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertTrue(productSubscriptions.size()>0, "We should now be consuming an entitlement.");
 
 		// attempt to unsubscribe from it
 		clienttasks.unsubscribe(null, productSubscriptions.get(0).serialNumber, null,null,null);
 		productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertEquals(productSubscriptions.size(), 0, "We should no longer be consuming the imported entitlement after unsubscribing (while not registered).");
 	}
 	
 	
 	
 	// Candidates for an automated Test:
 	
 	
 	
 	// Protected Class Variables ***********************************************************************
 		
 	protected final String importCertificatesDir = "/tmp/sm-importCertificatesDir";
 	protected final String importEntitlementsDir = "/tmp/sm-importEntitlementsDir";
 	protected String originalEntitlementCertDir = null;
 	protected List<File> entitlementCertFiles = new ArrayList<File>();	// valid entitlement cert files that can be used for import testing (contains cert only, no key)
 	protected File futureEntitlementCertFile = null;
 	protected File consumerCertFile = new File(importCertificatesDir+File.separator+"cert.pem");	// a file containing a valid consumer cert.pem and its key.pem concatenated together
 	
 	// Protected methods ***********************************************************************
 	
 	public File getValidImportCertificate() {
 		// assemble a valid bundled cert/key certificate file
 		File importEntitlementCertFile = entitlementCertFiles.get(randomGenerator.nextInt(entitlementCertFiles.size()));
 		File importEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(importEntitlementCertFile);
 		File importCertificateFile = new File(importCertificatesDir+File.separator+importEntitlementCertFile.getName());
 		client.runCommandAndWait("cat "+importEntitlementCertFile+" "+importEntitlementKeyFile+" > "+importCertificateFile);
 		return (importCertificateFile);
 	}
 
 	protected void attemptAnEntitlementImportFromAnInvalidFile_Test(File invalidCertificate) {
 		
 		// once imported, what should the entitlement cert file be?
 		File expectedEntitlementCertFile = new File (clienttasks.entitlementCertDir+"/"+invalidCertificate.getName());
 		File expectedEntitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(expectedEntitlementCertFile);
 		
 		// make sure the expected entitlement files do not exist before our test
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement cert file does NOT yet exist ("+expectedEntitlementCertFile+").");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),0,"Before attempting the import, asserting that expected destination for the entitlement key file does NOT yet exist ("+expectedEntitlementKeyFile+").");
 		
 		// show the contents of the file about to be imported
 		log.info("Following is the contents of the certificate file about to be imported...");
 		client.runCommandAndWait("cat "+invalidCertificate);
 		
 		// attempt an entitlement cert import from a file containing only a key (negative test)
 		SSHCommandResult importResult = clienttasks.importCertificate_(invalidCertificate.getPath());
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=734533 - jsefler 08/30/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="734533"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			Assert.assertEquals(importResult.getExitCode(), Integer.valueOf(0));
 			Assert.assertEquals(importResult.getStdout().trim(), invalidCertificate.getName()+" is not a valid certificate file. Please use a valid certificate.");
 
 		} else {
 		// END OF WORKAROUND
 		
 		// assert the negative results
 		Assert.assertEquals(importResult.getExitCode(), Integer.valueOf(255), "The exit code from the import command indicates a failure.");
 		
 		// {0} is not a valid certificate file. Please use a valid certificate.
 		Assert.assertEquals(importResult.getStderr().trim(), invalidCertificate.getName()+" is not a valid certificate file. Please use a valid certificate.");
 		}
 		
 		// verify that the expectedEntitlementCertFile does NOT exist
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementCertFile.getPath()),0,"After attempting the import, the expected destination for the entitlement cert file should NOT exist ("+expectedEntitlementCertFile+") since there was no entitlement in the import file.");
 
 		// verify that the expectedEntitlementKeyFile does NOT exist
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, expectedEntitlementKeyFile.getPath()),0,"After attempting the import, the expected destination for the entitlement key file should NOT exist ("+expectedEntitlementKeyFile+") since there was no key in the import file.");
 	}
 	
 	
 	
 	// Configuration methods ***********************************************************************
 	
 	@BeforeMethod(groups={"setup"})
 	public void removeAllEntitlementCertsBeforeEachTest() {
 		//clienttasks.removeAllCerts(false, true);
 		clienttasks.clean(null,null,null);
 	}
 	
 	@AfterClass(groups={"setup"}, alwaysRun=true)
 	public void cleanupAfterClass() {
 		if (clienttasks==null) return;
 		if (originalEntitlementCertDir!=null) clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "entitlementCertDir", originalEntitlementCertDir);
 		clienttasks.unregister_(null,null,null);
 		clienttasks.clean_(null,null,null);
 	}
 	
 	@BeforeClass(groups={"setup"})
 	public void setupBeforeClass() throws Exception {
 		
 		// restart rhsmcertd with a longer certFrequency
 		clienttasks.restart_rhsmcertd(240, false);
 		
 		// register
 		//clienttasks.unregister(null,null,null);	// avoid Bug 733525 - [Errno 2] No such file or directory: '/etc/pki/entitlement'
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, true, null, null, null);
 
 		// change the entitlementCertDir to a temporary location to store all of the entitlements that will be used for importing
 		originalEntitlementCertDir = clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "entitlementCertDir");
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "entitlementCertDir", importEntitlementsDir);
 		clienttasks.removeAllCerts(false, true);
 		
 		// create a directory where we can create bundled entitlement/key certtificates for import
 		RemoteFileTasks.runCommandAndAssert(client,"mkdir -p "+importCertificatesDir,Integer.valueOf(0));
 		
 		// subscribe to all available pools (so as to create valid entitlement cert/key pairs)
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools();
 		
 //		// assemble a list of entitlements that we can use for import
 //		entitlementCertFiles = clienttasks.getCurrentEntitlementCertFiles();
 				
 		// generate a future entitlement for the ImportACertificateForAFutureEntitlement_Test
 		Object[][] futureSystemSubscriptionPoolsData = getAllFutureSystemSubscriptionPoolsDataAs2dArray();
 		if (futureSystemSubscriptionPoolsData.length>0) {
 			SubscriptionPool futurePool = (SubscriptionPool) futureSystemSubscriptionPoolsData[randomGenerator.nextInt(futureSystemSubscriptionPoolsData.length)][0];
 			
 			// subscribe to the future subscription pool
 			SSHCommandResult subscribeResult = clienttasks.subscribe(null,futurePool.poolId,null,null,null,null,null,null,null,null);
 	
 			// assert that the granted entitlement cert begins in the future
 			Calendar now = new GregorianCalendar();	now.setTimeInMillis(System.currentTimeMillis());
 			EntitlementCert futureEntitlementCert = clienttasks.getEntitlementCertCorrespondingToSubscribedPool(futurePool);		
 			
 			Assert.assertNotNull(futureEntitlementCert,"Found the newly granted EntitlementCert on the client after subscribing to future subscription pool '"+futurePool.poolId+"'.");
 			Assert.assertTrue(futureEntitlementCert.validityNotBefore.after(now), "The newly granted EntitlementCert is not valid until the future.  EntitlementCert: "+futureEntitlementCert);
 			Assert.assertTrue(futureEntitlementCert.orderNamespace.startDate.after(now), "The newly granted EntitlementCert's OrderNamespace starts in the future.  OrderNamespace: "+futureEntitlementCert.orderNamespace);	
 			
 			// remember the futureEntitlementCertFile
 			futureEntitlementCertFile = clienttasks.getEntitlementCertFileFromEntitlementCert(futureEntitlementCert);
 
 		} else {
 			log.warning("Could not find a pool to a future system subscription.");
 		}
 		
 		// assemble a list of entitlements that we can use for import (excluding the future cert)
 		entitlementCertFiles = clienttasks.getCurrentEntitlementCertFiles();
 		if (futureEntitlementCertFile!=null) entitlementCertFiles.remove(entitlementCertFiles.indexOf(futureEntitlementCertFile));
 
 		// create a bundled consumer cert/key file for a negative import test
 		client.runCommandAndWait("cat "+clienttasks.consumerCertFile+" "+clienttasks.consumerKeyFile+" > "+consumerCertFile);
 
 		// restore the entitlementCertDir
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "entitlementCertDir", originalEntitlementCertDir);
 
 		// unregister client so as to test imports while not registered
 		clienttasks.unregister(null,null,null);
 		
 		// assert that we have some valid entitlement certs for import testing
 		if (entitlementCertFiles.size()<1) throw new SkipException("Could not generate valid entitlement certs for these ImportTests.");
 	}
 	
 	// Data Providers ***********************************************************************
 
 }
