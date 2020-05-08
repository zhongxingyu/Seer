 package com.redhat.qe.sm.cli.tests;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
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
 @Test(groups={"proxy"})
 public class ProxyTests extends SubscriptionManagerCLITestScript {
 
 	protected String basicauthproxyHostname = getProperty("sm.basicauthproxy.hostname", "");
 	protected String basicauthproxyPort = getProperty("sm.basicauthproxy.port", "");
 	protected String basicauthproxyUsername = getProperty("sm.basicauthproxy.username", "");
 	protected String basicauthproxyPassword = getProperty("sm.basicauthproxy.password", "");
 	protected String basicauthproxyLog = getProperty("sm.basicauthproxy.log", "");
 	
 	protected String noauthproxyHostname = getProperty("sm.noauthproxy.hostname", "");
 	protected String noauthproxyPort = getProperty("sm.noauthproxy.port", "");
 	protected String noauthproxyLog = getProperty("sm.noauthproxy.log", "");
 	
 
 	// Test methods ***********************************************************************
 	
 	// REGISTER Test methods ***********************************************************************
 	
 	@Test(	description="subscription-manager : register using a proxy server (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getRegisterAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterAttemptsUsingProxyServer_Test(String username, String password, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdoutRegex, String stderrRegex) {
 		String moduleTask = "register";
 		
 		SSHCommandResult attemptResult = clienttasks.register_(username, password, null, null, null, null, null, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 	
 	
 	@Test(	description="subscription-manager : register using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getRegisterAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RegisterAttemptsUsingProxyServerViaRhsmConfig_Test(String username, String password, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdoutRegex, String stderrRegex, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		String moduleTask = "register";
 
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = "Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt to register
 		SSHCommandResult attemptResult = clienttasks.register_(username, password, null, null, null, null, null, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from a negative attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
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
 			SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	
 	// UNREGISTER Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : unregister using a proxy server (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getUnregisterAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void UnregisterAttemptsUsingProxyServer_Test(String username, String password, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdoutRegex, String stderrRegex) {
 		// setup for test
 		String moduleTask = "unregister";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.unregister_(proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 	
 	
 	@Test(	description="subscription-manager : unregister using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getUnregisterAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void UnregisterAttemptsUsingProxyServerViaRhsmConfig_Test(String username, String password, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdoutRegex, String stderrRegex, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "unregister";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = "Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.unregister_(proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// IDENTITY Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : identity using a proxy server (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getRegisterAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void IdentityAttemptsUsingProxyServer_Test(String username, String password, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdoutRegex, String stderrRegex) {
 		// setup for test
 		String moduleTask = "identity";
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.identity_(username, password, Boolean.TRUE, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : identity using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getRegisterAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void IdentityAttemptsUsingProxyServerViaRhsmConfig_Test(String username, String password, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdoutRegex, String stderrRegex, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "identity";
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = "Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.identity_(username, password, Boolean.TRUE, proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// LIST Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : list using a proxy server (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getListAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void ListAttemptsUsingProxyServer_Test(String username, String password, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdoutRegex, String stderrRegex) {
 		// setup for test
 		String moduleTask = "list";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.list_(null,Boolean.TRUE,null,null,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : list using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getListAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void ListAttemptsUsingProxyServerViaRhsmConfig_Test(String username, String password, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdoutRegex, String stderrRegex, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "list";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = "Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.list_(null,Boolean.TRUE,null,null,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// FACTS Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : facts using a proxy server (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getFactsAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void FactsAttemptsUsingProxyServer_Test(String username, String password, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdoutRegex, String stderrRegex) {
 		// setup for test
 		String moduleTask = "facts";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.facts_(null,Boolean.TRUE,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : facts using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={},
 			dataProvider="getFactsAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void FactsAttemptsUsingProxyServerViaRhsmConfig_Test(String username, String password, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdoutRegex, String stderrRegex, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "facts";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = "Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.facts_(null,Boolean.TRUE,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// REFRESH Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : refresh using a proxy server (Positive and Negative Variations)",
 			groups={"blockedByBug-664548"},
 			dataProvider="getRefreshAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RefreshAttemptsUsingProxyServer_Test(String username, String password, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdoutRegex, String stderrRegex) {
 		// setup for test
 		String moduleTask = "refresh";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.refresh_(proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : refresh using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={"blockedByBug-664548"},
 			dataProvider="getRefreshAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RefreshAttemptsUsingProxyServerViaRhsmConfig_Test(String username, String password, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdoutRegex, String stderrRegex, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "refresh";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 	
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = "Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.refresh_(proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// SUBSCRIBE Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : subscribe using a proxy server (Positive and Negative Variations)",
 			groups={"blockedByBug-664603"},
 			dataProvider="getSubscribeAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void SubscribeAttemptsUsingProxyServer_Test(String username, String password, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdoutRegex, String stderrRegex) {
 		// setup for test
 		String moduleTask = "subscribe";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 
 		SSHCommandResult attemptResult = clienttasks.subscribe_(pool.poolId,null,null,null,null,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : subscribe using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={"blockedByBug-664603"},
 			dataProvider="getSubscribeAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void SubscribeAttemptsUsingProxyServerViaRhsmConfig_Test(String username, String password, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdoutRegex, String stderrRegex, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "subscribe";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = "Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 		
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.subscribe_(pool.poolId,null,null,null,null,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
 	
 	// UNSUBSCRIBE Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager : unsubscribe using a proxy server (Positive and Negative Variations)",
 			groups={"blockedByBug-664603"},
 			dataProvider="getUnsubscribeAttemptsUsingProxyServerData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void UnsubscribeAttemptsUsingProxyServer_Test(String username, String password, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdoutRegex, String stderrRegex) {
 		// setup for test
 		String moduleTask = "unsubscribe";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 		SSHCommandResult attemptResult = clienttasks.unsubscribe_(Boolean.TRUE, null,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 	}
 
 	
 	@Test(	description="subscription-manager : subscribe using a proxy server after setting rhsm.config parameters (Positive and Negative Variations)",
 			groups={"blockedByBug-664603"},
 			dataProvider="getUnsubscribeAttemptsUsingProxyServerViaRhsmConfigData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void UnsubscribeAttemptsUsingProxyServerViaRhsmConfig_Test(String username, String password, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdoutRegex, String stderrRegex, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		// setup for test
 		String moduleTask = "unsubscribe";
 		if (!username.equals(clientusername) || !password.equals(clientpassword)) throw new SkipException("These dataProvided parameters are either superfluous or not meaningful for this test.");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 		// pad the tail of basicauthproxyLog with a message
 		String proxyLogMarker = "Testing "+moduleTask+" AttemptsUsingProxyServerViaRhsmConfig_Test from "+clienttasks.hostname+"...";
 		RemoteFileTasks.runCommandAndAssert(proxyRunner,"echo '"+proxyLogMarker+"'  >> "+proxyLog, Integer.valueOf(0));
 		
 		// set the config parameters
 		updateConfFileProxyParameters(proxy_hostnameConfig, proxy_portConfig, proxy_userConfig, proxy_passwordConfig);
 		RemoteFileTasks.runCommandAndWait(client,"grep proxy "+clienttasks.rhsmConfFile,LogMessageUtil.action());
 
 		// attempt the moduleTask with the proxy options
 		SSHCommandResult attemptResult = clienttasks.unsubscribe_(Boolean.TRUE, null,proxy, proxyuser, proxypassword);
 		if (exitCode!=null)		Assert.assertEquals(attemptResult.getExitCode(), exitCode, "The exit code from an attempt to "+moduleTask+" using a proxy server.");
 		if (stdoutRegex!=null)	Assert.assertContainsMatch(attemptResult.getStdout().trim(), stdoutRegex, "The stdout from an attempt to "+moduleTask+" using a proxy server.");
 		if (stderrRegex!=null)	Assert.assertContainsMatch(attemptResult.getStderr().trim(), stderrRegex, "The stderr from an attempt to "+moduleTask+" using a proxy server.");
 
 		// assert the tail of proxyLog shows the proxyLogRegex
 		if (proxyLogRegex!=null) {
 			SSHCommandResult proxyLogResult = RemoteFileTasks.runCommandAndAssert(proxyRunner,"tail -1 "+proxyLog, Integer.valueOf(0));
 			Assert.assertContainsMatch(proxyLogResult.getStdout(), proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to the candlepin server.");
 		}
 	}
 	
 	
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
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null, null, null, null);
 		
 	}
 	
 	
 	// TODO Candidates for an automated Test:
 	//		
 	
 	
 	// Configuration methods ***********************************************************************
 	
 	public static SSHCommandRunner basicauthproxy = null;
 	public static SSHCommandRunner noauthproxy = null;
 	@BeforeClass(groups={"setup"})
 	public void setupBeforeClass() throws IOException {
 		basicauthproxy = new SSHCommandRunner(basicauthproxyHostname, sshUser, sshKeyPrivate, sshkeyPassphrase, null);
 		noauthproxy = new SSHCommandRunner(noauthproxyHostname, sshUser, sshKeyPrivate, sshkeyPassphrase, null);
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
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		String basicauthproxyUrl = String.format("%s:%s", basicauthproxyHostname,basicauthproxyPort); basicauthproxyUrl = basicauthproxyUrl.replaceAll(":$", "");
 		String noauthproxyUrl = String.format("%s:%s", noauthproxyHostname,noauthproxyPort); noauthproxyUrl = noauthproxyUrl.replaceAll(":$", "");
 		String nErrMsg = "Network error, unable to connect to server. Please see "+clienttasks.rhsmLogFile+" for more information.";
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg(); //"Invalid username or password";
 		
 		// String username, String password, String proxy, String proxyuser, String proxypassword, Integer exitCode, String stdoutRegex, String stderrRegex
 
 		// basic auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	basicauthproxyUrl,		basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(0),		null,		null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	"bad-proxy",			basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	basicauthproxyUrl+"0",	basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	basicauthproxyUrl,		"bad-username",				basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	basicauthproxyUrl,		basicauthproxyUsername,		"bad-password",			Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	basicauthproxyUrl,		"bad-username",				"bad-password",			Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	basicauthproxyUrl,		null,						null,					Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	"bad-password",	basicauthproxyUrl,		basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(255),	null,		uErrMsg}));
 		ll.add(Arrays.asList(new Object[]{	"bad-username",	clientpassword,	basicauthproxyUrl,		basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(255),	null,		uErrMsg}));
 
 		// no auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	noauthproxyUrl,			null,						null,					Integer.valueOf(0),		null,		null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	noauthproxyUrl,			"ignored-username",			"ignored-password",		Integer.valueOf(0),		null,		null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	noauthproxyUrl+"0",		null,						null,					Integer.valueOf(255),	nErrMsg,	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	"bad-password",	noauthproxyUrl,			null,						null,					Integer.valueOf(255),	null,		uErrMsg}));
 		ll.add(Arrays.asList(new Object[]{	"bad-username",	clientpassword,	noauthproxyUrl,			null,						null,					Integer.valueOf(255),	null,		uErrMsg}));
 
 		return ll;
 	}
 
 	@DataProvider(name="getRegisterAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		String basicauthproxyUrl = String.format("%s:%s", basicauthproxyHostname,basicauthproxyPort); basicauthproxyUrl = basicauthproxyUrl.replaceAll(":$", "");
 		String noauthproxyUrl = String.format("%s:%s", noauthproxyHostname,noauthproxyPort); noauthproxyUrl = noauthproxyUrl.replaceAll(":$", "");
 		String nErrMsg = "Network error, unable to connect to server. Please see "+clienttasks.rhsmLogFile+" for more information.";
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg(); //"Invalid username or password";
 
 		// String username, String password, String proxy, String proxyuser, String proxypassword, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdoutRegex, String stderrRegex, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex
 		// basic auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					basicauthproxyHostname,	basicauthproxyPort,		basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicauthproxy,	basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					"bad-proxy",			basicauthproxyPort,		basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	basicauthproxyLog,	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					basicauthproxyHostname,	basicauthproxyPort+"0",	basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	basicauthproxyLog,	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					basicauthproxyHostname,	basicauthproxyPort,		"bad-username",				basicauthproxyPassword,	Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	basicauthproxyLog,	"TCP_DENIED"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					basicauthproxyHostname,	basicauthproxyPort,		basicauthproxyUsername,		"bad-password",			Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	basicauthproxyLog,	"TCP_DENIED"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					basicauthproxyHostname,	basicauthproxyPort,		"bad-username",				"bad-password",			Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	basicauthproxyLog,	"TCP_DENIED"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					basicauthproxyHostname,	basicauthproxyPort,		""/*no username*/,			""/*no password*/,		Integer.valueOf(255),	nErrMsg,	null,		basicauthproxy,	basicauthproxyLog,	"TCP_DENIED"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	basicauthproxyUrl,	null,					null,					"bad-proxy",			basicauthproxyPort+"0",	basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicauthproxy,	basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	basicauthproxyUrl,	basicauthproxyUsername,	null,					"bad-proxy",			basicauthproxyPort+"0",	"bad-username",				basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicauthproxy,	basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	basicauthproxyUrl,	basicauthproxyUsername,	basicauthproxyPassword,	"bad-proxy",			basicauthproxyPort+"0",	"bad-username",				"bad-password",			Integer.valueOf(0),		null,		null,		basicauthproxy,	basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					basicauthproxyHostname,	basicauthproxyPort,		basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicauthproxy,	basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	"bad-password",	null,				null,					null,					basicauthproxyHostname,	basicauthproxyPort,		basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(255),	null,		uErrMsg,	basicauthproxy,	basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{	"bad-username",	"bad-password",	null,				null,					null,					basicauthproxyHostname,	basicauthproxyPort,		basicauthproxyUsername,		basicauthproxyPassword,	Integer.valueOf(255),	null,		uErrMsg,	basicauthproxy,	basicauthproxyLog,	"TCP_MISS"}));
 
 		// no auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					noauthproxyHostname,	noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noauthproxy,	noauthproxyLog,		"Closed connection"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					noauthproxyHostname,	noauthproxyPort,		"ignored-username",			"ignored-password",		Integer.valueOf(0),		null,		null,		noauthproxy,	noauthproxyLog,		"Closed connection"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					noauthproxyHostname,	noauthproxyPort+"0",	"",							"",						Integer.valueOf(255),	nErrMsg,	null,		noauthproxy,	noauthproxyLog,		null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	null,				null,					null,					"bad-proxy",			noauthproxyPort,		"",							"",						Integer.valueOf(255),	nErrMsg,	null,		noauthproxy,	noauthproxyLog,		null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	"bad-password",	null,				null,					null,					noauthproxyHostname,	noauthproxyPort,		"",							"",						Integer.valueOf(255),	null,		uErrMsg,	noauthproxy,	noauthproxyLog,		"Closed connection"}));
 		ll.add(Arrays.asList(new Object[]{	"bad-username",	"bad-password",	null,				null,					null,					noauthproxyHostname,	noauthproxyPort,		"",							"",						Integer.valueOf(255),	null,		uErrMsg,	noauthproxy,	noauthproxyLog,		"Closed connection"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	noauthproxyUrl,		null,					null,					"bad-proxy",			noauthproxyPort+"0",	"",							"",						Integer.valueOf(0),		null,		null,		noauthproxy,	noauthproxyLog,		"Closed connection"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	noauthproxyUrl,		"ignored-username",		"ignored-password",		"bad-proxy",			noauthproxyPort+"0",	"bad-username",				"bad-password",			Integer.valueOf(0),		null,		null,		noauthproxy,	noauthproxyLog,		"Closed connection"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,	clientpassword,	"bad-proxy",		null,					null,					noauthproxyHostname,	noauthproxyPort,		"",							"",						Integer.valueOf(255),	nErrMsg,	null,		noauthproxy,	noauthproxyLog,		null}));
 
 
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getUnregisterAttemptsUsingProxyServerData")
 	public Object[][] getUnregisterAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getUnregisterAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getUnregisterAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	@DataProvider(name="getUnregisterAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getUnregisterAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getUnregisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getUnregisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getListAttemptsUsingProxyServerData")
 	public Object[][] getListAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getListAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getListAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	@DataProvider(name="getListAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getListAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getListAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getListAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getFactsAttemptsUsingProxyServerData")
 	public Object[][] getFactsAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getFactsAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getFactsAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	@DataProvider(name="getFactsAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getFactsAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getFactsAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getFactsAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getRefreshAttemptsUsingProxyServerData")
 	public Object[][] getRefreshAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRefreshAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getRefreshAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	@DataProvider(name="getRefreshAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getRefreshAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRefreshAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getRefreshAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getSubscribeAttemptsUsingProxyServerData")
 	public Object[][] getSubscribeAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getSubscribeAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getSubscribeAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	@DataProvider(name="getSubscribeAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getSubscribeAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getSubscribeAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getSubscribeAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getUnsubscribeAttemptsUsingProxyServerData")
 	public Object[][] getUnsubscribeAttemptsUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getUnsubscribeAttemptsUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getUnsubscribeAttemptsUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 	
 	@DataProvider(name="getUnsubscribeAttemptsUsingProxyServerViaRhsmConfigData")
 	public Object[][] getUnsubscribeAttemptsUsingProxyServerViaRhsmConfigDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getUnsubscribeAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists());
 	}
 	protected List<List<Object>> getUnsubscribeAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		for (List<Object> l : getRegisterAttemptsUsingProxyServerViaRhsmConfigDataAsListOfLists()) {
 			// only include dataProvided rows where username and password are valid
 			if (l.get(0).equals(clientusername) && l.get(1).equals(clientpassword)) ll.add(l);
 		}
 		return ll;
 	}
 }
