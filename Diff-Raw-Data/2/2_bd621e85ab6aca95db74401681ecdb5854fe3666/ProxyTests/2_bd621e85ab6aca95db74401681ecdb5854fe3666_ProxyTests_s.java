 package com.redhat.qe.sm.cli.tests;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BlockedByBzBug;
 import com.redhat.qe.auto.testng.LogMessageUtil;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 /**
  * @author jsefler
  *
  * http://gibson.usersys.redhat.com/agilo/ticket/4618
  * automate tests for subscription manager modules with basic and noauth proxy servers
  * 		register
  * 		subscribe
  * 		unsubscribe
  * 		unregister
  * 		clean (https://bugzilla.redhat.com/show_bug.cgi?id=664581)
  * 		facts
  * 		identity
  * 		refresh
  * 		list
  */
 @Test(groups={"ProxyTests"})
 public class ProxyTests extends SubscriptionManagerCLITestScript {
 
 
 	// Test methods ***********************************************************************
 	
 	// REGISTER Test methods ***********************************************************************
 	
 	@Test(	description="subscription-manager : register using a proxy server (Positive and Negative Variations)",
 			groups={"AcceptanceTests"},
 			dataProvider="getRegisterAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		String moduleTask = "register";
 		
 		SSHCommandResult attemptResult = clienttasks.register_(username, password, org, null, null, null, null, null, (String)null, null, null, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 	
 	
 	@Test(	description="subscription-manager : register using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getRegisterAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RegisterAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		String moduleTask = "register";
 
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt to register
 		SSHCommandResult attemptResult = clienttasks.register_(username, password, org, null, null, null, null, null, (String)null, null, null, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from a negative attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex (BASIC AUTH)
 		// 1292545301.350    418 10.16.120.247 TCP_MISS/200 1438 CONNECT jsefler-f12-candlepin.usersys.redhat.com:8443 redhat DIRECT/10.16.120.146 -
 		// 1292551602.625      0 10.16.120.247 TCP_DENIED/407 3840 CONNECT jsefler-f12-candlepin.usersys.redhat.com:8443 - NONE/- text/html
 
 		// assert the tail of proxyLog shows the proxyLogRegex (NO AUTH)
 		// CONNECT   Dec 17 18:56:22 [20793]: Connect (file descriptor 7):  [10.16.120.248]
 		// CONNECT   Dec 17 18:56:22 [20793]: Request (file descriptor 7): CONNECT jsefler-f12-candlepin.usersys.redhat.com:8443 HTTP/1.1
 		// INFO      Dec 17 18:56:22 [20793]: No proxy for jsefler-f12-candlepin.usersys.redhat.com
 		// CONNECT   Dec 17 18:56:22 [20793]: Established connection to host "jsefler-f12-candlepin.usersys.redhat.com" using file descriptor 8.
 		// INFO      Dec 17 18:56:22 [20793]: Not sending client headers to remote machine
 		// INFO      Dec 17 18:56:22 [20793]: Closed connection between local client (fd:7) and remote client (fd:8)
 		
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	
 	// UNREGISTER Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : unregister using a proxy server (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getUnregisterAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void UnregisterAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		// setup for test
 		String moduleTask = "unregister";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.unregister_(proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 	
 	
 	@Test(	description="subscription-manager : unregister using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getUnregisterAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void UnregisterAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "unregister";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.unregister_(proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// IDENTITY Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : identity using a proxy server (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getIdentityAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void IdentityAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		// setup for test
 		String moduleTask = "identity";
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.identity_(username, password, Boolean.TRUE, Boolean.TRUE, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : identity using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getIdentityAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void IdentityAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "identity";
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.identity_(username, password, Boolean.TRUE, Boolean.TRUE, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// ORGS Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : orgs using a proxy server (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getOrgsAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void OrgsAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		// setup for test
 		String moduleTask = "orgs";
 		
 		SSHCommandResult attemptResult = clienttasks.orgs_(username, password, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : orgs using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getOrgsAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void OrgsAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "orgs";
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.orgs_(username, password, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// ENVIRONMENTS Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : environments using a proxy server (Positive and Negative Variations)",
 			groups={"blockedByBug-728380"},
 			dataProvider="getEnvironmentsAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void EnvironmentsAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		// setup for test
 		String moduleTask = "environments";
 		
 		SSHCommandResult attemptResult = clienttasks.environments_(username, password, org, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : environments using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={"blockedByBug-728380"},
 			dataProvider="getEnvironmentsAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void EnvironmentsAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "environments";
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.environments_(username, password, org, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// LIST Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : list using a proxy server (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getListAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void ListAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		// setup for test
 		String moduleTask = "list";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.list_(null,Boolean.TRUE,null,null,null, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : list using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getListAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void ListAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "list";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.list_(null,Boolean.TRUE,null,null,null, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// REDEEM Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : redeem using a proxy server (Positive and Negative Variations)",
 			groups={"ProxyRedeemTests"},
 			dataProvider="getRedeemAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RedeemAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		// setup for test
 		String moduleTask = "redeem";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.redeem_("proxytester@redhat.com","en-us",proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : redeem using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={"ProxyRedeemTests"},
 			dataProvider="getRedeemAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RedeemAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "redeem";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.redeem_("proxytester@redhat.com","en-us",proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// FACTS Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : facts using a proxy server (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getFactsAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void FactsAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		// setup for test
 		String moduleTask = "facts";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.facts_(null,Boolean.TRUE,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : facts using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getFactsAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void FactsAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "facts";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.facts_(null,Boolean.TRUE,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// REFRESH Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : refresh using a proxy server (Positive and Negative Variations)",
 			groups={"blockedByBug-664548"},
 			dataProvider="getRefreshAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RefreshAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		// setup for test
 		String moduleTask = "refresh";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.refresh_(proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : refresh using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={"blockedByBug-664548"},
 			dataProvider="getRefreshAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RefreshAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "refresh";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.refresh_(proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// SUBSCRIBE Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : subscribe using a proxy server (Positive and Negative Variations)",
 			groups={"blockedByBug-664603"},
 			dataProvider="getSubscribeAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void SubscribeAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		// setup for test
 		String moduleTask = "subscribe";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 
 		SSHCommandResult attemptResult = clienttasks.subscribe_(null,pool.poolId,null,null,null,null, null, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : subscribe using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={"blockedByBug-664603"},
 			dataProvider="getSubscribeAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void SubscribeAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "subscribe";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 		
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.subscribe_(null,pool.poolId,null,null,null,null, null, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// UNSUBSCRIBE Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : unsubscribe using a proxy server (Positive and Negative Variations)",
 			groups={"blockedByBug-664603"},
 			dataProvider="getUnsubscribeAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void UnsubscribeAttemptsUsingProxyServer_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr) {
 		// setup for test
 		String moduleTask = "unsubscribe";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.unsubscribe_(Boolean.TRUE, null,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : subscribe using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={"blockedByBug-664603"},
 			dataProvider="getUnsubscribeAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void UnsubscribeAttemptsUsingProxyServerViaRhsmConfig_Test(Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "unsubscribe";
 		if (!username.equals(sm_clientUsername) || !password.equals(sm_clientPassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 		
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		//RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.unsubscribe_(Boolean.TRUE, null,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)	Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdout!=null)	Assert.assertEquals(attemptResult.getStdout().trim(), stdout, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderr!=null)	Assert.assertEquals(attemptResult.getStderr().trim(), stderr, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			//SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			//SSHCommandResult proxyLogResult = proxyRunner.runCommandAndWait("(LINES=''; IFS=$'\n'; for line in $(tac "+proxyLog+"); do if [[ $line = '"+proxyLogMarker+"' ]]; then break; fi; LINES=${LINES}'\n'$line; done; echo -e $LINES) | grep "+clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy simultaneously
 			//Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 			String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 			Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// More Test methods ***********************************************************************
 	
 	@Test(	description="subscription-manager :  register with proxy configurations commented out of rhsm.conf",
 			groups={},
 			dataProvider="getRegisterWithProxyConfigurationsCommentedOutOfRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RegisterWithProxyConfigurationsCommentedOutOfRhsmConfig_Test(Object meta, String[] proxyConfigs) {
 		
 		// comment out each of the config proxy parameters
 		for (String proxyConfig : proxyConfigs) clienttasks.commentConfFileParameter(clienttasks.rhsmConfFile, proxyConfig);
 		
 		log.info("Following are the current proxy parameters configured in config file: "+clienttasks.rhsmConfFile);
 		RemoteFileTasks.runCommandAndWait(client, "grep proxy_ "+clienttasks.rhsmConfFile, LogMessageUtil.action());
 		
 		log.info("Attempt to register with the above proxy config parameters configured (expecting success)...");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, null, false, null, null, null);
 	}
 	
 	
 	// Candidates for an automated Test:
	//		
 	
 	
 	// Configuration methods ***********************************************************************
 	
 	public static SSHCommandRunner basicauthproxy = null;
 	public static SSHCommandRunner noauthproxy = null;
 	public static String nErrMsg = null;
 
 	@BeforeClass(groups={"setup"})
 	public void setupBeforeClass() throws IOException {
 		basicauthproxy = new SSHCommandRunner(sm_basicauthproxyHostname, sm_sshUser, sm_sshKeyPrivate, sm_sshkeyPassphrase, null);
 		noauthproxy = new SSHCommandRunner(sm_noauthproxyHostname, sm_sshUser, sm_sshKeyPrivate, sm_sshkeyPassphrase, null);
 		nErrMsg = "Network error, unable to connect to server.\n Please see "+clienttasks.rhsmLogFile+" for more information.";
 	}
 	
 	@BeforeMethod(groups={"setup"})
 	public void cleanRhsmConfigAndUnregisterBeforeMethod() {
 		uncommentConfFileProxyParameters();
 		updateConfFileProxyParameters("","","","");
 		clienttasks.unregister(null, null, null);
 	}
 	
 	@AfterClass(groups={"setup"})
 	public void cleanRhsmConfigAfterClass() throws IOException {
 		cleanRhsmConfigAndUnregisterBeforeMethod();
 	}
 	
 	@BeforeGroups(value={"ProxyRedeemTests"}, groups={"setup"})
 	public void createMockAssetFactsFile () {
 		// create a facts file with a serialNumber that will clobber the true system facts
 		Map<String,String> facts = new HashMap<String,String>();
 		facts.put("dmi.system.manufacturer", "Dell Inc.");
 		facts.put("dmi.system.serial_number", "5ABCDEF");	// Mock Setup For: "Your subscription activation is being processed and should be available soon. You will be notified via email once it is available. If you have any questions, additional information can be found here: https://access.redhat.com/kb/docs/DOC-53864."
 		clienttasks.createFactsFileWithOverridingValues(facts);
 	}
 	
 	@AfterGroups(value={"ProxyRedeemTests"}, groups={"setup"}, alwaysRun=true)
 	public void deleteMockAssetFactsFile () {
 		clienttasks.deleteFactsFileWithOverridingValues();
 	}
 	
 	
 	
 	// Protected methods ***********************************************************************
 	
 	protected void uncommentConfFileProxyParameters() {
 		clienttasks.uncommentConfFileParameter(clienttasks.rhsmConfFile, "proxy_hostname");
 		clienttasks.uncommentConfFileParameter(clienttasks.rhsmConfFile, "proxy_port");
 		clienttasks.uncommentConfFileParameter(clienttasks.rhsmConfFile, "proxy_user");
 		clienttasks.uncommentConfFileParameter(clienttasks.rhsmConfFile, "proxy_password");
 	}
 	protected void updateConfFileProxyParameters(String proxy_hostname, String proxy_port, String proxy_user, String proxy_password) {
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "proxy_hostname", proxy_hostname);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "proxy_port", proxy_port);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "proxy_user", proxy_user);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "proxy_password", proxy_password);
 	}
 
 	
 	// Data Providers ***********************************************************************
 	
 	
 	@DataProvider(name="getRegisterWithProxyConfigurationsCommentedOutOfRhsmConfigData")
 	public Object[][] getRegisterWithProxyConfigurationsCommentedOutOfRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterWithProxyConfigurationsCommentedOutOfRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterWithProxyConfigurationsCommentedOutOfRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 
 		// String[] proxyConfigs
 
 		ll.add(Arrays.asList(new Object[] {null,	new String[]{"proxy_hostname"}  }));
 		ll.add(Arrays.asList(new Object[] {null,	new String[]{"proxy_port"}  }));
 		ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("667829"), new String[]{"proxy_hostname", "proxy_port"} }));
 		ll.add(Arrays.asList(new Object[] {null,	new String[]{"proxy_user"}  }));
 		ll.add(Arrays.asList(new Object[] {null,	new String[]{"proxy_password"}  }));
 		ll.add(Arrays.asList(new Object[] {null,	new String[]{"proxy_user", "proxy_password"}  }));
 		ll.add(Arrays.asList(new Object[] {null,	new String[]{"proxy_hostname", "proxy_port", "proxy_user", "proxy_password"}  }));
 
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getRegisterAttemptsUsingProxyServerData")
 	public Object[][] getRegisterAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		String basicauthproxyUrl = String.format("%s:%s", sm_basicauthproxyHostname,sm_basicauthproxyPort); basicauthproxyUrl = basicauthproxyUrl.replaceAll(":$", "");
 		String noauthproxyUrl = String.format("%s:%s", sm_noauthproxyHostname,sm_noauthproxyPort); noauthproxyUrl = noauthproxyUrl.replaceAll(":$", "");
 //		String nErrMsg = "Network error, unable to connect to server. Please see "+clienttasks.rhsmLogFile+" for more information.";
 		String uErrMsg = servertasks.invalidCredentialsMsg(); //"Invalid username or password";
 	
 		// Object blockedByBug, String username, String password, String org, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdout, String stderr
 
 		// basic auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	basicauthproxyUrl,		sm_basicauthproxyUsername,		sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	"bad-proxy",			sm_basicauthproxyUsername,		sm_basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	basicauthproxyUrl+"0",	sm_basicauthproxyUsername,		sm_basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	basicauthproxyUrl,		"bad-username",					sm_basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	basicauthproxyUrl,		sm_basicauthproxyUsername,		"bad-password",				Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	basicauthproxyUrl,		"bad-username",					"bad-password",				Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	basicauthproxyUrl,		null,							null,						Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	null,	"bad-username",		sm_clientPassword,	sm_clientOrg,	basicauthproxyUrl,		sm_basicauthproxyUsername,		sm_basicauthproxyPassword,	Integer.valueOf(255),	null,		uErrMsg}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	"bad-password",		sm_clientOrg,	basicauthproxyUrl,		sm_basicauthproxyUsername,		sm_basicauthproxyPassword,	Integer.valueOf(255),	null,		uErrMsg}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	"bad-org",		basicauthproxyUrl,		sm_basicauthproxyUsername,		sm_basicauthproxyPassword,	Integer.valueOf(255),	null,		/*"Organization/Owner bad-org does not exist."*/"Organization bad-org does not exist."}));
 
 		// no auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	noauthproxyUrl,			null,						null,					Integer.valueOf(0),		null,		null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	noauthproxyUrl,			"ignored-username",			"ignored-password",		Integer.valueOf(0),		null,		null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	noauthproxyUrl+"0",		null,						null,					Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	null,	"bad-username",		sm_clientPassword,	sm_clientOrg,	noauthproxyUrl,			null,						null,					Integer.valueOf(255),	null,		uErrMsg}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	"bad-password",		sm_clientOrg,	noauthproxyUrl,			null,						null,					Integer.valueOf(255),	null,		uErrMsg}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	"bad-org",		noauthproxyUrl,			null,						null,					Integer.valueOf(255),	null,		/*"Organization/Owner bad-org does not exist."*/"Organization bad-org does not exist."}));
 
 		return ll;
 	}
 
 	
 	@DataProvider(name="getRegisterAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		String basicauthproxyUrl = String.format("%s:%s", sm_basicauthproxyHostname,sm_basicauthproxyPort); basicauthproxyUrl = basicauthproxyUrl.replaceAll(":$", "");
 		String noauthproxyUrl = String.format("%s:%s", sm_noauthproxyHostname,sm_noauthproxyPort); noauthproxyUrl = noauthproxyUrl.replaceAll(":$", "");
 //		String nErrMsg = "Network error, unable to connect to server. Please see "+clienttasks.rhsmLogFile+" for more information.";
 		String uErrMsg = servertasks.invalidCredentialsMsg(); //"Invalid username or password";
 
 		// Object blockedByBug, String username, String password, Sring org, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex
 		// basic auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicauthproxy,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						"bad-proxy",				sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	sm_basicauthproxyLog,	null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_basicauthproxyHostname,	sm_basicauthproxyPort+"0",	sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	sm_basicauthproxyLog,	null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_basicauthproxyHostname,	sm_basicauthproxyPort,		"bad-username",				sm_basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	sm_basicauthproxyLog,	"TCP_DENIED"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	"bad-password",				Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	sm_basicauthproxyLog,	"TCP_DENIED"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_basicauthproxyHostname,	sm_basicauthproxyPort,		"bad-username",				"bad-password",				Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	sm_basicauthproxyLog,	"TCP_DENIED"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_basicauthproxyHostname,	sm_basicauthproxyPort,		""/*no username*/,			""/*no password*/,			Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	sm_basicauthproxyLog,	"TCP_DENIED"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	basicauthproxyUrl,	null,						null,						"bad-proxy",				sm_basicauthproxyPort+"0",	sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicauthproxy,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	basicauthproxyUrl,	sm_basicauthproxyUsername,	null,						"bad-proxy",				sm_basicauthproxyPort+"0",	"bad-username",				sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicauthproxy,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	basicauthproxyUrl,	sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	"bad-proxy",				sm_basicauthproxyPort+"0",	"bad-username",				"bad-password",				Integer.valueOf(0),		null,		null,		basicauthproxy,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicauthproxy,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	null,	"bad-username",		sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(255),	null,		uErrMsg,	basicauthproxy,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	"bad-password",		sm_clientOrg,	null,				null,						null,						sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(255),	null,		uErrMsg,	basicauthproxy,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	"bad-org",		null,				null,						null,						sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(255),	null,		/*"Organization/Owner bad-org does not exist."*/"Organization bad-org does not exist.",	basicauthproxy,	sm_basicauthproxyLog,	"TCP_MISS"}));
 
 		// no auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noauthproxy,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_noauthproxyHostname,	sm_noauthproxyPort,		"ignored-username",			"ignored-password",		Integer.valueOf(0),		null,		null,		noauthproxy,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_noauthproxyHostname,	sm_noauthproxyPort+"0",	"",							"",						Integer.valueOf(255),	nErrMsg,	null,		noauthproxy,	sm_noauthproxyLog,		null}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,				null,						null,						"bad-proxy",			sm_noauthproxyPort,		"",							"",						Integer.valueOf(255),	nErrMsg,	null,		noauthproxy,	sm_noauthproxyLog,		null}));
 		ll.add(Arrays.asList(new Object[]{	null,	"bad-username",		sm_clientPassword,	sm_clientOrg,	null,				null,						null,						sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(255),	null,		uErrMsg,	noauthproxy,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	"bad-password",		sm_clientOrg,	null,				null,						null,						sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(255),	null,		uErrMsg,	noauthproxy,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	"bad-org",		null,				null,						null,						sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(255),	null,		/*"Organization/Owner bad-org does not exist."*/"Organization bad-org does not exist.",	noauthproxy,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	noauthproxyUrl,		null,						null,						"bad-proxy",			sm_noauthproxyPort+"0",	"",							"",						Integer.valueOf(0),		null,		null,		noauthproxy,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	noauthproxyUrl,		"ignored-username",			"ignored-password",			"bad-proxy",			sm_noauthproxyPort+"0",	"bad-username",				"bad-password",			Integer.valueOf(0),		null,		null,		noauthproxy,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{	null,	sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	"bad-proxy",		null,						null,						sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(255),	nErrMsg,	null,		noauthproxy,	sm_noauthproxyLog,		null}));
 
 
 		return ll;
 	}
 	protected List<List<Object>> getValidRegisterAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only include dataProvided rows where username, password, and org are valid
 			if (l.get(1).equals(sm_clientUsername) && l.get(2).equals(sm_clientPassword) && l.get(3).equals(sm_clientOrg)) ll.add(l);
 		}
 		return ll;
 	}
 	protected List<List<Object>> getValidRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only include dataProvided rows where username, password, and org are valid
 			if (l.get(1).equals(sm_clientUsername) && l.get(2).equals(sm_clientPassword) && l.get(3).equals(sm_clientOrg)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getIdentityAttemptsUsingProxyServerData")
 	public Object[][] getIdentityAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getIdentityAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getIdentityAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only include dataProvided rows where org is valid
 			if (l.get(3).equals(sm_clientOrg)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getIdentityAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getIdentityAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getIdentityAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getIdentityAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only include dataProvided rows where org is valid
 			if (l.get(3).equals(sm_clientOrg)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getOrgsAttemptsUsingProxyServerData")
 	public Object[][] getOrgsAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getOrgsAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getOrgsAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only include dataProvided rows where org is valid
 			if (l.get(3).equals(sm_clientOrg)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getOrgsAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getOrgsAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getOrgsAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getOrgsAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only include dataProvided rows where org is valid
 			if (l.get(3).equals(sm_clientOrg)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getEnvironmentsAttemptsUsingProxyServerData")
 	public Object[][] getEnvironmentsAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getEnvironmentsAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getEnvironmentsAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only include dataProvided rows where org is valid
 			/*FIXME if (l.get(3).equals(sm_clientOrg))*/ ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getEnvironmentsAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getEnvironmentsAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getEnvironmentsAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getEnvironmentsAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only include dataProvided rows where org is valid
 			/*FIXME if (l.get(3).equals(sm_clientOrg))*/ ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getUnregisterAttemptsUsingProxyServerData")
 	public Object[][] getUnregisterAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	
 	
 	@DataProvider(name="getUnregisterAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getUnregisterAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	
 	
 	@DataProvider(name="getListAttemptsUsingProxyServerData")
 	public Object[][] getListAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	
 	
 	@DataProvider(name="getListAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getListAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	
 	
 	@DataProvider(name="getRedeemAttemptsUsingProxyServerData")
 	public Object[][] getRedeemAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRedeemAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getRedeemAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getValidRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only block rows where stdout != null with BlockedByBzBug("732499")
 			if (l.get(8)!=null) {
 				ll.add(Arrays.asList(new Object[]{	new BlockedByBzBug("732499"),	l.get(1),	l.get(2),	l.get(3),	l.get(4),	l.get(5),	l.get(6),	l.get(7),	l.get(8),	l.get(9)}));
 			} else {
 				ll.add(l);
 			}
 		}
 		return ll;
 	}
 
 	
 	@DataProvider(name="getRedeemAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getRedeemAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRedeemAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getRedeemAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getValidRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only block rows where stdout != null with BlockedByBzBug("732499")
 			if (l.get(12)!=null) {
 				ll.add(Arrays.asList(new Object[]{	new BlockedByBzBug("732499"),	l.get(1),	l.get(2),	l.get(3),	l.get(4),	l.get(5),	l.get(6),	l.get(7),	l.get(8),	l.get(9),	l.get(10),	l.get(11),	l.get(12),	l.get(13),	l.get(14),	l.get(15),	l.get(16)}));
 			} else {
 				ll.add(l);
 			}
 		}
 		return ll;
 	}
 	
 // DELETME after bug https://bugzilla.redhat.com/show_bug.cgi?id=720049 is resolved
 //	@DataProvider(name="getReposAttemptsUsingProxyServerData")
 //	public Object[][] getReposAttemptsUsingProxyServerDataAs2dArray() {
 //		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerDataAsListOfLists());
 //	}
 //	
 //	
 //	@DataProvider(name="getReposAttemptsUsingProxyServerViaRhsmConfigData")
 //	public Object[][] getReposAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 //		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 //	}
 	
 	
 	@DataProvider(name="getFactsAttemptsUsingProxyServerData")
 	public Object[][] getFactsAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getFactsAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getFactsAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getValidRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			if (l.get(8)!=null) {
 				ll.add(Arrays.asList(new Object[]{	l.get(0),	l.get(1),	l.get(2),	l.get(3),	l.get(4),	l.get(5),	l.get(6),	l.get(7),	null,	"Error updating system data, see /var/log/rhsm/rhsm.log for more details."}));
 			} else {
 				ll.add(l);
 			}
 		}
 		return ll;
 	}
 	
 	@DataProvider(name="getFactsAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getFactsAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getFactsAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getFactsAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getValidRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			if (l.get(12)!=null) {
 				ll.add(Arrays.asList(new Object[]{	l.get(0),	l.get(1),	l.get(2),	l.get(3),	l.get(4),	l.get(5),	l.get(6),	l.get(7),	l.get(8),	l.get(9),	l.get(10),	l.get(11),	null,	"Error updating system data, see /var/log/rhsm/rhsm.log for more details.",	l.get(14),	l.get(15),	l.get(16)}));
 			} else {
 				ll.add(l);
 			}
 		}
 		return ll;
 	}
 	
 	@DataProvider(name="getRefreshAttemptsUsingProxyServerData")
 	public Object[][] getRefreshAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	
 	
 	@DataProvider(name="getRefreshAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getRefreshAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	
 	
 	@DataProvider(name="getSubscribeAttemptsUsingProxyServerData")
 	public Object[][] getSubscribeAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	
 	
 	@DataProvider(name="getSubscribeAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getSubscribeAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	
 	
 	@DataProvider(name="getUnsubscribeAttemptsUsingProxyServerData")
 	public Object[][] getUnsubscribeAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	
 	
 	@DataProvider(name="getUnsubscribeAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getUnsubscribeAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getValidRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 
 }
