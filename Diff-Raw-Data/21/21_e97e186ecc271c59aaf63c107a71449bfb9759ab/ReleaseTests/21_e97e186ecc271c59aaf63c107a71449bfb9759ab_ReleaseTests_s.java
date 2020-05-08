 package com.redhat.qe.sm.cli.tests;
 
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  *
  * How to get the expected release list
  * http://cdn-internal.rcm-test.redhat.com/content/dist/rhel/server/5/listing
  * http://cdn-internal.rcm-test.redhat.com/content/dist/rhel/server/6/listing
  */
 
@Test(groups={"ReleaseTests","debugTest","AcceptanceTest"})
 public class ReleaseTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 	
 	@Test(	description="attempt to get the subscription-manager release when not registered",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptToGetReleaseWhenNotRegistered_Test() {
 		
 		// make sure we are not registered
 		clienttasks.unregister(null, null, null);
 		
 		SSHCommandResult result;
 		
 		result = clienttasks.release_(null, null, null, null, null);
 		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "ExitCode from calling release without being registered");
 		Assert.assertEquals(result.getStdout().trim(), clienttasks.msg_ConsumerNotRegistered, "Stdout from release without being registered");
 
 		result = clienttasks.release_(true, null, null, null, null);
 		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "ExitCode from calling release --list without being registered");
 		Assert.assertEquals(result.getStdout().trim(), clienttasks.msg_ConsumerNotRegistered, "Stdout from release without being registered");
 
 		result = clienttasks.release_(null, "FOO", null, null, null);
 		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "ExitCode from calling release --set without being registered");
 		Assert.assertEquals(result.getStdout().trim(), clienttasks.msg_ConsumerNotRegistered, "Stdout from release without being registered");
 	}
 	
 	@Test(	description="attempt to get the subscription-manager release when a release has not been set; should be told that the release is not set",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptToGetReleaseWhenReleaseHasNotBeenSet_Test() {
 		
 		// make sure we are newly registered
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(List<String>)null,true,null,null,null, null);		
 		SSHCommandResult result = clienttasks.release(null, null, null, null, null);
 		Assert.assertEquals(result.getStdout().trim(), "Release not set", "Stdout from release without having set it");
 	}
 	
 	@Test(	description="attempt to get the subscription-manager release list without having subscribed to any entitlements",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptToGetReleaseListWithoutHavingAnyEntitlements_Test() {
 		
 		// make sure we are newly registered
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(List<String>)null,true,null,null,null, null);		
 
 		Assert.assertTrue(clienttasks.getCurrentlyAvailableReleases().isEmpty(), "No releases should be available without having been entitled to anything.");
 	}
 	
 	@Test(	description="verify that the consumer's current subscription-manager release value matches the release value just set",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void GetTheReleaseAfterSettingTheRelease_Test() {
 		
 		// make sure we are newly registered
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(List<String>)null,true,null,null,null, null);		
 
 		clienttasks.release(null, "Foo", null, null, null);
 		Assert.assertEquals(clienttasks.getCurrentRelease(), "Foo", "The release value retrieved after setting the release.");
 	}
 
 	@Test(	description="assert that the subscription-manager release can be unset",
 			groups={"blockedByBug-807822"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void UnsetTheReleaseAfterSettingTheRelease_Test() {
 		
 		// make sure we are newly registered
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(List<String>)null,true,null,null,null, null);		
 
 		clienttasks.release(null, "Foo", null, null, null);
		SSHCommandResult result = clienttasks.release(null, null, null, null, null);
		Assert.assertEquals(result.getStdout().trim(), "Release not set", "Stdout from release after attempting to unset it.");
 		Assert.assertEquals(clienttasks.getCurrentRelease(), "", "The release value retrieved after attempting to unset it.");
 	}
 	
 
 	
 	
 //	@Test(	description="subscription-manager: service-level --show (when not registered)",
 //			groups={},
 //			enabled=true)
 //	//@ImplementsNitrateTest(caseId=)
 //	public void ServiceLevelShowWhenNotRegistered_Test() {
 //		
 //		// make sure we are not registered
 //		clienttasks.unregister(null, null, null);
 //		
 //		SSHCommandResult result;
 //		
 //		// with credentials
 //		result = clienttasks.service_level_(true, null, sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null);
 //		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "ExitCode from service-level --show without being registered");
 //		Assert.assertEquals(result.getStdout().trim(),"Error: This system is currently not registered.", "Stdout from service-level --show without being registered");
 //		
 //		// without credentials
 //		result = clienttasks.service_level_(true, null, null, null, null, null, null, null);
 //		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "ExitCode from service-level --show without being registered");
 //		Assert.assertEquals(result.getStdout().trim(),"Error: This system is currently not registered.", "Stdout from service-level --show without being registered");
 //	}
 //	
 //	@Test(	description="subscription-manager: service-level --list (when not registered)",
 //			groups={},
 //			enabled=true)
 //	//@ImplementsNitrateTest(caseId=)
 //	public void ServiceLevelListWhenNotRegistered_Test() {
 //		
 //		// make sure we are not registered
 //		clienttasks.unregister(null, null, null);
 //		
 //		SSHCommandResult result = clienttasks.service_level_(null, true, null, null, null, null, null, null);
 //		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "ExitCode from service-level --list without being registered");
 //		Assert.assertEquals(result.getStdout().trim(),"Error: you must register or specify --username and password to list service levels", "Stdout from service-level --list without being registered");
 //	}
 //	
 //	
 //	@Test(	description="subscription-manager: service-level --list (with invalid credentials)",
 //			groups={},
 //			enabled=true)
 //	//@ImplementsNitrateTest(caseId=)
 //	public void ServiceLevelListWithInvalidCredentials_Test() {
 //		String x = String.valueOf(getRandInt());
 //		SSHCommandResult result;
 //				
 //		// test while unregistered
 //		clienttasks.unregister(null, null, null);
 //		result = clienttasks.service_level_(null, true, sm_clientUsername, sm_clientPassword+x, sm_clientOrg, null, null, null);
 //		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "ExitCode from service-level --list with invalid credentials");
 //		Assert.assertEquals(result.getStderr().trim(), servertasks.invalidCredentialsMsg(), "Stderr from service-level --list with invalid credentials");
 //		Assert.assertEquals(result.getStdout().trim(), "", "Stdout from service-level --list with invalid credentials");
 //
 //		// test while registered
 //		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,(List<String>)null,null,null,null,null,null);
 //		result = clienttasks.service_level_(null, true, sm_clientUsername, sm_clientPassword+x, sm_clientOrg, null, null, null);
 //		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "ExitCode from service-level --list with invalid credentials");
 //		Assert.assertEquals(result.getStderr().trim(), servertasks.invalidCredentialsMsg(), "Stderr from service-level --list with invalid credentials");
 //		Assert.assertEquals(result.getStdout().trim(), "", "Stdout from service-level --list with invalid credentials");
 //	}
 //	
 //	
 //	@Test(	description="subscription-manager: service-level --list (with invalid org)",
 //			groups={"blockedByBug-796468"},
 //			enabled=true)
 //	//@ImplementsNitrateTest(caseId=)
 //	public void ServiceLevelListWithInvalidOrg_Test() {
 //		String x = String.valueOf(getRandInt());
 //		SSHCommandResult result;
 //				
 //		// test while unregistered
 //		clienttasks.unregister(null, null, null);
 //		result = clienttasks.service_level_(null, true, sm_clientUsername, sm_clientPassword, sm_clientOrg+x, null, null, null);
 //		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "ExitCode from service-level --list with invalid org");
 //		Assert.assertEquals(result.getStderr().trim(), String.format("Organization with id %s could not be found",sm_clientOrg+x), "Stderr from service-level --list with invalid org");
 //		Assert.assertEquals(result.getStdout().trim(), "", "Stdout from service-level --list with invalid credentials");
 //
 //		// test while registered
 //		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,(List<String>)null,null,null,null,null,null);
 //		result = clienttasks.service_level_(null, true, sm_clientUsername, sm_clientPassword, sm_clientOrg+x, null, null, null);
 //		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255), "ExitCode from service-level --list with invalid org");
 //		Assert.assertEquals(result.getStderr().trim(), String.format("Organization with id %s could not be found",sm_clientOrg+x), "Stderr from service-level --list with invalid org");
 //		Assert.assertEquals(result.getStdout().trim(), "", "Stdout from service-level --list with invalid credentials");
 //	}
 //	
 //	
 //	
 //	@Test(	description="subscription-manager: service-level --show (after registering without a service level)",
 //			groups={},
 //			enabled=false) // assertions in this test are already a subset of ServiceLevelShowAvailable_Test
 //	//@ImplementsNitrateTest(caseId=)
 //	public void ServiceLevelShowAfterRegisteringWithoutServiceLevel_Test() throws JSONException, Exception  {
 //		SSHCommandResult result;
 //				
 //		// register with no service-level
 //		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,(List<String>)null,true,null,null,null,null));
 //		
 //		// get the current consumer object and assert that the serviceLevel persisted
 //		JSONObject jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerId));
 //		Assert.assertEquals(jsonConsumer.get("serviceLevel"), JSONObject.NULL, "The call to register without a servicelevel leaves the current consumer object serviceLevel attribute value null.");
 //		
 //		result = clienttasks.service_level(true, false, null, null, null, null, null, null);
 //		Assert.assertEquals(result.getStdout().trim(), "Current service level:", "When the system has been registered without a service level, the current service level should be null.");
 //	}
 //	
 //	
 //	@Test(	description="subscription-manager: service-level --show (after registering without a service level)",
 //			groups={"AcceptanceTests"},
 //			dataProvider="getRegisterCredentialsExcludingNullOrgData",
 //			enabled=true)
 //	//@ImplementsNitrateTest(caseId=)
 //	public void ServiceLevelShowAvailable_Test(String username, String password, String org) throws JSONException, Exception  {
 //		SSHCommandResult result;
 //				
 //		// register with no service-level
 //		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(username,password,org,null,null,null,null,null,null,(List<String>)null,true,null,null,null,null));
 //		
 //		// get the current consumer object and assert that the serviceLevel is empty (value is "")
 //		JSONObject jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(username,password,sm_serverUrl,"/consumers/"+consumerId));
 //		// Assert.assertEquals(jsonConsumer.get("serviceLevel"), JSONObject.NULL, "The call to register without a servicelevel leaves the current consumer object serviceLevel attribute value null.");	// original value was null
 //		Assert.assertEquals(jsonConsumer.get("serviceLevel"), "", "The call to register without a servicelevel leaves the current consumer object serviceLevel attribute value empty.");
 //	
 //		// assert that "Current service level:" is empty
 //		Assert.assertEquals(clienttasks.getCurrentServiceLevel(), "", "When the system has been registered without a service level, the current service level value should be empty.");
 //		Assert.assertEquals(clienttasks.service_level(null,null,null,null,null,null,null,null).getStdout().trim(), "Current service level:", "When the system has been registered without a service level, the current service level value should be empty.");
 //
 //		// get all the valid service levels available to this org	
 //		List<String> serviceLevelsExpected = CandlepinTasks.getServiceLevelsForOrgKey(/*username or*/sm_serverAdminUsername, /*password or*/sm_serverAdminPassword, sm_serverUrl, org);
 //		
 //		// assert that all the valid service levels are returned by service-level --list
 //		List<String> serviceLevelsActual = clienttasks.getCurrentlyAvailableServiceLevels();		
 //		Assert.assertTrue(serviceLevelsExpected.containsAll(serviceLevelsActual)&&serviceLevelsActual.containsAll(serviceLevelsExpected), "The actual service levels available to the current consumer "+serviceLevelsActual+" match the expected list of service levels available to the org '"+org+"' "+serviceLevelsExpected+".");
 //
 //		// subscribe with each service level and assert that the current service level persists the requested service level
 //		for (String serviceLevel : serviceLevelsExpected) {
 //			clienttasks.subscribe(true, serviceLevel, (String)null, (String)null, (String)null, null, null, null, null, null, null);
 //			Assert.assertEquals(clienttasks.getCurrentServiceLevel(), serviceLevel, "When the system has auto subscribed specifying a service level, the current service level should be persisted.");
 //
 //		}
 //	}
 	
 	
 	
 
 	// Candidates for an automated Test:
 	
 	
 		
 	// Configuration methods ***********************************************************************
 
 
 	
 	
 	// Protected methods ***********************************************************************
 
 
 
 	
 	// Data Providers ***********************************************************************
 	
 
 }
