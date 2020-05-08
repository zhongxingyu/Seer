 package com.redhat.qe.sm.tests;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsTCMS;
 import com.redhat.qe.sm.tasks.Pool;
 import com.redhat.qe.sm.tasks.ProductID;
 import com.redhat.qe.tools.RemoteFileTasks;
 
 public class Subscribe extends Setup{
 	
 	@Test(description="subscription-manager-cli: subscribe client to an entitlement using product ID",
 			dependsOnGroups={"sm_stage3"},
 			groups={"sm_stage4", "blockedByBug-584137"})
 	@ImplementsTCMS(id="41680,41899")
 	public void SubscribeToValidSubscriptionsByProductID_Test(){
 		this.unsubscribeFromAllProductIDs();
 		this.subscribeToAllPools(false);
 	}
 	
 	@Test(description="subscription-manager-cli: subscribe client to an entitlement using product ID",
 			dependsOnGroups={"sm_stage3"},
 			groups={"sm_stage4", "blockedByBug-584137"})
 	public void SubscribeToASingleEntitlementByProductID_Test(){
 		this.unsubscribeFromAllProductIDs();
 		Pool MCT0696 = new Pool("MCT0696", "biteme");
 		MCT0696.addProductID("RH Infrastructure Solutions");
 		this.subscribeToPool(MCT0696, false);
 		//this.refreshSubscriptions();
 		for (ProductID pid:MCT0696.associatedProductIDs){
 			Assert.assertTrue(this.consumedProductIDs.contains(pid),
 					"ProductID '"+pid.productId+"' consumed from Pool '"+MCT0696.poolName+"'");
 		}
 	}
 	
 	@Test(description="subscription-manager-cli: subscribe client to an entitlement using pool ID",
 			dependsOnGroups={"sm_stage3"},
 			groups={"sm_stage4", "blockedByBug-584137"})
 	@ImplementsTCMS(id="41686,41899")
 	public void SubscribeToValidSubscriptionsByPoolID_Test(){
 		this.unsubscribeFromAllProductIDs();
 		this.subscribeToAllPools(true);
 	}
 	
 	@Test(description="subscription-manager-cli: subscribe client to an entitlement using registration token",
 			dependsOnGroups={"sm_stage8"},
 			groups={"sm_stage9", "blockedByBug-584137"})
 	@ImplementsTCMS(id="41681")
 	public void SubscribeToRegToken_Test(){
 		this.unsubscribeFromAllProductIDs();
 		this.subscribeToRegToken(regtoken);
 	}
 	
 	@Test(description="Subscribed for Already subscribed Entitlement.",
 			dependsOnGroups={"sm_stage3"},
 			groups={"sm_stage4", "blockedByBug-584137"})
 	@ImplementsTCMS(id="41897")
 	public void SubscribeAndSubscribeAgain_Test(){
 		this.unsubscribeFromAllProductIDs();
 		this.refreshSubscriptions();
 		ArrayList<Pool> availablePools = (ArrayList<Pool>)this.availPools.clone();
 		for(Pool sub:availablePools){
 			this.subscribeToPool(sub, false);
 			RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner,
 					RHSM_LOC+"subscribe --product="+sub.poolName);
 		}
 	}
 	
 	@Test(description="subscription-manager Yum plugin: enable/disable",
 			dependsOnGroups={"sm_stage5"},
 			groups={"sm_stage6"})
 	@ImplementsTCMS(id="41696")
 	public void EnableYumRepoAndVerifyContentAvailable_Test(){
 		this.subscribeToAllPools(false);
 		this.adjustRHSMYumRepo(true);
 		/*for(ProductID sub:this.consumedProductIDs){
 			ArrayList<String> repos = this.getYumRepolist();
 			Assert.assertTrue(repos.contains(sub.productId),
 					"Yum reports product subscribed to repo: " + sub.productId);
 		}*/
 	}
 	
 	@Test(description="subscription-manager Yum plugin: ensure ...",
 	        dependsOnGroups={"sm_stage6"},
 	        groups={"sm_stage7"})
 	@ImplementsTCMS(id="xxxx")
 	public void VerifyReposAvailableForEnabledContent(){
 	    
 	    sshCommandRunner.runCommandAndWait(
             "find /etc/pki/entitlement/product/ -name '*.pem' | xargs -I '{}' openssl x509 -in '{}' -noout -text"
 	    );
 	    String butt = "butt";
 	    
 	    java.util.List<String[]> contentSets = parseProductCertificates(sshCommandRunner.getStdout());
 	    
 	    ArrayList<String> repos = this.getYumRepolist();
 	    
 	    for (String[] content : contentSets) {
 	        if ("1".equals(content[1])) { 
                 Assert.assertTrue(repos.contains(content[2]), 
                         "Yum reports enabled content subscribed to repo: " + content[2]);
 	        } else {
                 Assert.assertFalse(repos.contains(content[2]), 
                         "Yum reports disabled content not subscribed to repo: " + content[2]);
 	        }
 	    }
 	}
 	
 	@Test(description="subscription-manager Yum plugin: ensure content can be downloaded/installed",
 			dependsOnGroups={"sm_stage6"},
 			groups={"sm_stage7"})
 	@ImplementsTCMS(id="41695")
 	public void InstallPackageFromRHSMYumRepo_Test(){
 		HashMap<String, String[]> pkgList = this.getPackagesCorrespondingToSubscribedRepos();
 		for(ProductID sub:this.consumedProductIDs){
 			String pkg = pkgList.get(sub.productId)[0];
 			log.info("timeout of two minutes for next three commands");
 			RemoteFileTasks.runCommandExpectingNoTracebacks(sshCommandRunner,
 					"yum repolist");
 			RemoteFileTasks.runCommandExpectingNoTracebacks(sshCommandRunner,
 					"yum install -y "+pkg);
 			RemoteFileTasks.runCommandExpectingNoTracebacks(sshCommandRunner,
 					"rpm -q "+pkg);
 		}
 	}
 	
 	@Test(description="subscription-manager Yum plugin: enable/disable",
 			dependsOnGroups={"sm_stage7"},
 			groups={"sm_stage8"})
 	@ImplementsTCMS(id="41696")
 	public void DisableYumRepoAndVerifyContentNotAvailable_Test(){
 		this.adjustRHSMYumRepo(false);
 		for(Pool sub:this.availPools)
 			for(String repo:this.getYumRepolist())
 				if(repo.contains(sub.poolName))
 					Assert.fail("After unsubscribe, Yum still has access to repo: "+repo);
 	}
 	
 	@Test(description="rhsmcertd: change certFrequency",
 			dependsOnGroups={"sm_stage3"},
 			groups={"sm_stage4"})
 	@ImplementsTCMS(id="41692")
 	public void certFrequency_Test(){
 		this.changeCertFrequency("1");
 		this.sleep(70*1000);
 		Assert.assertEquals(RemoteFileTasks.grepFile(sshCommandRunner,
 				rhsmcertdLogFile,
 				"certificates updated"),
 				0,
 				"rhsmcertd reports that certificates have been updated at new interval");
 	}
 	
 	@Test(description="rhsmcertd: ensure certificates synchronize",
 			dependsOnGroups={"sm_stage3"},
 			groups={"sm_stage4"})
 	@ImplementsTCMS(id="41694")
 	public void refreshCerts_Test(){
 		//SubscribeToASingleEntitlementByProductID_Test();
 		sshCommandRunner.runCommandAndWait("rm -f /etc/pki/entitlement/*");
 		sshCommandRunner.runCommandAndWait("rm -f /etc/pki/entitlement/product/*");
 		sshCommandRunner.runCommandAndWait("rm -f /etc/pki/product/*");
 		sshCommandRunner.runCommandAndWait("cat /dev/null > "+rhsmcertdLogFile);
 		//sshCommandRunner.runCommandAndWait("rm -f "+rhsmcertdLogFile);
 		//sshCommandRunner.runCommandAndWait("/etc/init.d/rhsmcertd restart");
 		this.sleep(70*1000);
 		
 		Assert.assertEquals(RemoteFileTasks.grepFile(sshCommandRunner,
 				rhsmcertdLogFile,
 				"certificates updated"),
 				0,
 				"rhsmcertd reports that certificates have been updated");
 		
 		//verify that PEM files are present in all certificate directories
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, 
 				"ls /etc/pki/entitlement | grep pem",
 				0,
 				"pem", 
 				null);
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, 
 				"ls /etc/pki/entitlement/product | grep pem", 
 				0,
 				"pem", 
 				null);
 		// this directory will only be populated if you upload ur own license, not while working w/ candlepin
 		/*RemoteFileTasks.runCommandAndAssert(sshCommandRunner, 
 				"ls /etc/pki/product", 
 				0,
 				"pem", 
 				null);*/
 	}
 }
