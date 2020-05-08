 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 
 import org.testng.SkipException;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.ContentNamespace;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.Repo;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 
 /**
  * @author jsefler
  * 
  * Red Hat Update Infrastructure
  * Reference:
  * RHEL6 http://documentation-stage.bne.redhat.com/docs/en-US/Red_Hat_Update_Infrastructure/2.0/html/Installation_Guide/sect-Installation_Guide-Installation_Requirements-Package_Installation.html
  * RHEL5 http://docs.redhat.com/docs/en-US/Red_Hat_Update_Infrastructure/1.2/html/Installation_Guide/chap-Installation_Guide-Installation.html
  *		https://cdn.redhat.com/content/dist/rhel/rhui/server/5Server/i386/rhui/1.2/iso/rhel-5.5-rhui-1.2-i386.iso
  *		https://cdn.redhat.com/content/dist/rhel/rhui/server/5Server/x86_64/rhui/1.2/iso/rhel-5.5-rhui-1.2-x86_64.iso
  * 
  */
@Test(groups={"RHUITests","AcceptanceTests"})
 public class RHUITests extends SubscriptionManagerCLITestScript {
 
 	// Test methods ***********************************************************************
 	
 	@Test(	description="register to the stage/prod environment as a RHUI consumer type",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterRHUIConsumer_Test() {
 		if (sm_rhuiUsername.equals("")) throw new SkipException("Skipping this test when no value was given for the RHUI Username");
 		// register the RHUI consumer
 		clienttasks.register(sm_rhuiUsername,sm_rhuiPassword,sm_rhuiOrg,null,ConsumerType.RHUI,null,null,null,null,null,(String)null,true,null, null, null, null);
 		
 	}
 	
 	@Test(	description="after registering to the stage/prod environment as a RHUI consumer, subscribe to the expected RHUI product subscription",
 			groups={},
 			dependsOnMethods={"RegisterRHUIConsumer_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ConsumeRHUISubscriptionProduct_Test() {
 		
 		// assert that the RHUI ProductId is found in the all available list
 		List<SubscriptionPool> allAvailableSubscriptionPools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		Assert.assertNotNull(SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", sm_rhuiSubscriptionProductId, allAvailableSubscriptionPools), "RHUI Product ID '"+sm_rhuiSubscriptionProductId+"' is available for consumption when the client arch is ignored.");
 		
 		// assert that the RHUI ProductId is found in the available list only on x86_64,x86 arches
 		List<String> supportedArches = Arrays.asList("x86_64","x86","i386","i686");
 		List<SubscriptionPool> availableSubscriptionPools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool rhuiPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", sm_rhuiSubscriptionProductId, availableSubscriptionPools);
 		if (!supportedArches.contains(clienttasks.arch)) {
 			Assert.assertNull(rhuiPool, "RHUI Product ID '"+sm_rhuiSubscriptionProductId+"' should NOT be available for consumption on a system whose arch ("+clienttasks.arch+") is NOT among the supported arches "+supportedArches);
 			throw new SkipException("Cannot consume RHUI Product ID '"+sm_rhuiSubscriptionProductId+"' subscription on a system whose arch ("+clienttasks.arch+") is NOT among the supported arches "+supportedArches);
 		}
 		Assert.assertNotNull(rhuiPool, "RHUI Product ID '"+sm_rhuiSubscriptionProductId+"' is available for consumption on a system whose arch ("+clienttasks.arch+") is among the supported arches "+supportedArches);
 
 		
 		// Subscribe to the RHUI subscription productId
 		entitlementCertFile = clienttasks.subscribeToSubscriptionPool(rhuiPool);
 	}
 	
 	@Test(	description="download an expected RHUI iso from an expected yum repoUrl",
 			groups={},
 			dependsOnMethods={"ConsumeRHUISubscriptionProduct_Test"},
 			enabled=false)
 	//@ImplementsNitrateTest(caseId=)
 	public void DownloadRHUIISOFromYumRepo_Test() {
 		if (sm_rhuiDownloadIso.equals("")) throw new SkipException("Skipping this test when no value was given for the RHUI Download ISO");
 
 		File downloadedIsoFile = new File("/tmp/"+sm_rhuiDownloadIso);
 		RemoteFileTasks.runCommandAndAssert(client, "rm -rf "+downloadedIsoFile, 0/*, stdoutRegex, stderrRegex*/);
 	
 		// find the repo for the isos
 		Repo repoForIsos = Repo.findFirstInstanceWithMatchingFieldFromList("repoId", sm_rhuiRepoIdForIsos, clienttasks.getCurrentlySubscribedRepos());
 		Assert.assertNotNull(repoForIsos,"Found expected repoId for rhui isos after subscribe to '"+sm_rhuiSubscriptionProductId+"'.");
 		String repoUrl = repoForIsos.repoUrl;
 		repoUrl = repoUrl.replaceFirst("\\$releasever", clienttasks.releasever);
 		repoUrl = repoUrl.replaceFirst("\\$basearch", clienttasks.arch);
 
 		File entitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(entitlementCertFile);
 		// $ wget  --certificate=<Content Certificate>	https://cdn.redhat.com/content/dist/rhel/rhui/server/6/6Server/x86_64/rhui/2.0/iso/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD.iso
 		// wget --no-check-certificate --certificate=/etc/pki/entitlement/7658526340059785943.pem --private-key=/etc/pki/entitlement/7658526340059785943-key.pem --output-document=/tmp/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD.iso -- https://cdn.redhat.com/content/dist/rhel/rhui/server/6/6Server/x86_64/rhui/2.0/iso/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD.iso
 		RemoteFileTasks.runCommandAndAssert(client, "wget --no-check-certificate --certificate="+entitlementCertFile+" --private-key="+entitlementKeyFile+" --output-document="+downloadedIsoFile+" -- "+repoUrl+"/"+sm_rhuiDownloadIso, 0/*, stdoutRegex, stderrRegex*/);
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, downloadedIsoFile.getPath()), 1,"Expected RHUI Download ISO was downloaded.");
 	}
 	
 	@Test(	description="download an expected RHUI iso from an expected file repoUrl",
 			groups={},
 			dependsOnMethods={"ConsumeRHUISubscriptionProduct_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void DownloadRHUIISOFromFileRepo_Test() {
 		if (sm_rhuiDownloadIso.equals("")) throw new SkipException("Skipping this test when no value was given for the RHUI Download ISO");
 
 		File downloadedIsoFile = new File("/tmp/"+sm_rhuiDownloadIso);
 		RemoteFileTasks.runCommandAndAssert(client, "rm -rf "+downloadedIsoFile, 0/*, stdoutRegex, stderrRegex*/);
 	
 		// find the repo for the isos
 		ContentNamespace contentNamespaceForIso = null;
 		for (EntitlementCert entitlementCert : clienttasks.getCurrentEntitlementCerts()) {
 			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 				if (contentNamespace.label.equals(sm_rhuiRepoIdForIsos)) {
 					contentNamespaceForIso = contentNamespace;
 					break;
 				}
 			}
 		}
 		Assert.assertNotNull(contentNamespaceForIso,"Found expected ContentNamespace to repoId '"+sm_rhuiRepoIdForIsos+"' for rhui isos after subscribe to '"+sm_rhuiSubscriptionProductId+"'.");
 		String repoUrl = clienttasks.baseurl+contentNamespaceForIso.downloadUrl;
 		repoUrl = repoUrl.replaceFirst("\\$releasever", clienttasks.releasever);
 		repoUrl = repoUrl.replaceFirst("\\$basearch", clienttasks.arch);
 
 		File entitlementKeyFile = clienttasks.getEntitlementCertKeyFileCorrespondingToEntitlementCertFile(entitlementCertFile);
 		// $ wget  --certificate=<Content Certificate>	https://cdn.redhat.com/content/dist/rhel/rhui/server/6/6Server/x86_64/rhui/2.0/iso/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD.iso
 		// wget --no-check-certificate --certificate=/etc/pki/entitlement/7658526340059785943.pem --private-key=/etc/pki/entitlement/7658526340059785943-key.pem --output-document=/tmp/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD.iso -- https://cdn.redhat.com/content/dist/rhel/rhui/server/6/6Server/x86_64/rhui/2.0/iso/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD.iso
 		RemoteFileTasks.runCommandAndAssert(client, "wget --no-check-certificate --certificate="+entitlementCertFile+" --private-key="+entitlementKeyFile+" --output-document="+downloadedIsoFile+" -- "+repoUrl+"/"+sm_rhuiDownloadIso, 0/*, stdoutRegex, stderrRegex*/);
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, downloadedIsoFile.getPath()), 1,"Expected RHUI Download ISO was downloaded.");
 	}
 	
 	
 	@Test(	description="mount the downloaded RHUI iso and list the packages in the iso",
 			groups={},
 			dependsOnMethods={"DownloadRHUIISOFromFileRepo_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ListPackagesInMountedRHUIISO_Test() {
 
 		//	[root@jsefler-r63-server tmp]# mkdir -p /tmp/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD
 		//	[root@jsefler-r63-server tmp]# mount -o loop /tmp/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD.iso /tmp/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD
 		//	[root@jsefler-r63-server tmp]# ls /tmp/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD/Packages/
 		//	gofer-0.64-1.el6.noarch.rpm            pulp-0.0.263-18.el6.noarch.rpm                   python-gofer-0.64-1.el6.noarch.rpm
 		//	gofer-package-0.64-1.el6.noarch.rpm    pulp-admin-0.0.263-18.el6.noarch.rpm             python-httplib2-0.6.0-4.el6_0.noarch.rpm
 		//	grinder-0.0.136-1.el6.noarch.rpm       pulp-cds-0.0.263-18.el6.noarch.rpm               python-isodate-0.4.4-4.pulp.el6.noarch.rpm
 		//	js-1.70-12.el6_0.x86_64.rpm            pulp-client-lib-0.0.263-18.el6.noarch.rpm        python-oauth2-1.5.170-2.pulp.el6.noarch.rpm
 		//	libmongodb-1.8.2-2.el6.x86_64.rpm      pulp-common-0.0.263-18.el6.noarch.rpm            python-webpy-0.32-8.el6_0.noarch.rpm
 		//	libyaml-0.1.3-3.el6_1.x86_64.rpm       pulp-consumer-0.0.263-18.el6.noarch.rpm          PyYAML-3.09-14.el6_1.x86_64.rpm
 		//	m2crypto-0.21.1.pulp-7.el6.x86_64.rpm  pulp-selinux-server-0.0.263-18.el6.noarch.rpm    rh-rhua-selinux-policy-0.0.6-1.el6.noarch.rpm
 		//	mod_wsgi-3.3-2.pulp.el6.x86_64.rpm     pymongo-1.9-8.el6_1.x86_64.rpm                   rh-rhui-tools-2.0.60-1.el6.noarch.rpm
 		//	mongodb-1.8.2-2.el6.x86_64.rpm         python-BeautifulSoup-3.0.8.1-3.el6_1.noarch.rpm  ruby-gofer-0.64-1.el6.noarch.rpm
 		//	mongodb-server-1.8.2-2.el6.x86_64.rpm  python-bson-1.9-8.el6_1.x86_64.rpm               TRANS.TBL
 		//	[root@jsefler-r63-server tmp]# umount /tmp/RHEL-6.1-RHUI-2.0-LATEST-Server-x86_64-DVD
 
 		File downloadedIsoFile = new File("/tmp/"+sm_rhuiDownloadIso);
 		File mountPoint = new File(downloadedIsoFile+".mount");
 		client.runCommandAndWait("mkdir -p "+mountPoint+"; umount "+mountPoint);
 		RemoteFileTasks.runCommandAndAssert(client, "mount -o loop "+downloadedIsoFile+" "+mountPoint, 0/*, stdoutRegex, stderrRegex*/);
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, mountPoint+"/Packages"), 1, "The expected Packages directory exists after mounting the downloaded iso file.");
 		client.runCommandAndWait("ls -1 "+mountPoint+"/Packages");
 		client.runCommandAndWait("umount "+mountPoint);
 	}
 
 	
 	
 	// Candidates for an automated Test:
 	
 	
 	// Configuration methods ***********************************************************************
 	
 	
 	
 	
 	// Protected methods ***********************************************************************
 	File entitlementCertFile = null;
 
 
 	
 	// Data Providers ***********************************************************************
 
 }
