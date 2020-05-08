 package com.redhat.qe.sm.cli.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.AccessType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  *
  */
 @Test(groups={"RolesTests"})
 public class RolesTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 	
 	@Test(	description="for the given user credentials, verify that the permission access (READ_ONLY,ALL) are obeyed by attempting to register",
 			enabled=true,
 			groups={},
 			dataProvider="getUserRoleOrgPermissionAccessData")
 	//@ImplementsNitrateTest(caseId=)
 	public void UserRoleOrgPermissionAccess_Test(String username, String password, String roleName, String orgKey, String access){
 		SSHCommandResult sshCommandResult;
 
 		// flatten all the AccessType values into a comma separated list
 		String accessTypesAsString = "";
 		boolean knownAccessType=false;
 		for (AccessType type : AccessType.values()) {
 			if (type.toString().equals(access)) knownAccessType=true;
 			accessTypesAsString+=type+",";
 		}
 		accessTypesAsString = accessTypesAsString.replaceAll(",$", "");
 		
 		// fail on unknown access types
 		if (!knownAccessType) {
 			Assert.fail("Do not know how to test this users role permission access type '"+access+"'.  Is this a new type?  Currently known types: "+accessTypesAsString);	
 		}
 		
 		// test the users ability to access the org with this role
 		switch(AccessType.valueOf(access)) {
 			case ALL:
 				// when a user has ALL access to orgKey, then the user should be able to successfully register to that org.
 				sshCommandResult = clienttasks.register_(username, password, orgKey, null, null, null, null, null, (String)null, true, null, null, null);
 				Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0), "The exit code indicates that user '"+username+"' with role '"+roleName+"' to org '"+orgKey+"' can successfully register with access '"+access+"'.");
 				Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "The system has been registered with id: [a-f,0-9,\\-]{36}");
 				break;
 			case READ_ONLY:
 				sshCommandResult = clienttasks.register_(username, password, orgKey, null, null, null, null, null, (String)null, true, null, null, null);
 				Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(255), "The exit code indicates that user '"+username+"' with role '"+roleName+"' to org '"+orgKey+"' can NOT register with access '"+access+"'.");
 				//Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), "User "+username+" cannot access organization/owner "+orgKey);	// before string translation files were committed
 				Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), "User "+username+" cannot access organization "+orgKey);
 				
 				// TODO beav suggested another test to try here
 				/*
 				<beav> jsefler-wfh: maybe update a username
 				<jsefler-wfh> hmm - I haven't attempted that before.  How would I do that?  some curl api POST call to a rest api I suspect.
 				<beav> jsefler-wfh: ya, you just post up with similar data to what you get on a get /users/username
 				*/
 				break;
 			default:
 				Assert.fail("Do not know how to test this users role permission access type '"+access+"'.  Is this a new type?  Currently known types: "+accessTypesAsString);
 				break;
 		}
 	}
 	
 	
	// Candidates for an automated Test:
	// TODO Bug 720487 - Refresh Pools w/ Auto-Create Owner Fails
	//      # curl -k -u admin:admin --request PUT https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/owners/NEWOWNER/subscriptions?auto_create_owner=true	| python -mjson.tool
 		
 		
 	// Configuration methods ***********************************************************************
 	
 	
 	
 	// Protected methods ***********************************************************************
 	
 	
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="getUserRoleOrgPermissionAccessData")
 	public Object[][] getUserRoleOrgPermissionAccessDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getUserRoleOrgPermissionAccessDataAsListOfLists());
 	}
 	protected List<List<Object>>getUserRoleOrgPermissionAccessDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		// Notes...
 		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users | python -mjson.tool
 		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1 | python -mjson.tool
 		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1/roles | python -mjson.tool
 		
 		// TODO probably simpler and more appropriate, the following API call can be used
 		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/roles | python -mjson.tool
 
 		// get all of the candlepin users
 		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users | python -mjson.tool
 		JSONArray jsonUsers = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,"/users"));	
 		for (int i = 0; i < jsonUsers.length(); i++) {
 			JSONObject jsonUser = (JSONObject) jsonUsers.get(i);
 			// {
 			//   "created": "2011-07-01T06:40:00.951+0000", 
 			//   "hashedPassword": "05557a2aaec7cb676df574d2eb080691949a6752", 
 			//   "id": "8a90f8c630e46c7e0130e46ce9b70020", 
 			//   "superAdmin": false, 
 			//   "updated": "2011-07-01T06:40:00.951+0000", 
 			//   "username": "minnie"
 			// }
 			Boolean isSuperAdmin = jsonUser.getBoolean("superAdmin");
 			String username = jsonUser.getString("username");
 			String password = sm_clientPasswordDefault;
 			if (username.equals(sm_serverAdminUsername)) password = sm_serverAdminPassword;
 			
 			// get the user's roles
 			// curl -k -u testuser1:password https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1/owners | python -mjson.tool
 			JSONArray jsonRoles = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(username,password,sm_serverUrl,"/users/"+username+"/roles"));	
 			for (int j = 0; j < jsonRoles.length(); j++) {
 				JSONObject jsonRole = (JSONObject) jsonRoles.get(j);
 				//{
 				//    "created": "2011-07-22T13:27:32.326+0000", 
 				//    "href": "/roles/8a90f8c63152071c013152078ee70022", 
 				//    "id": "8a90f8c63152071c013152078ee70022", 
 				//    "name": "admin-all", 
 				//    "permissions": [
 				//        {
 				//            "access": "ALL", 
 				//            "created": "2011-07-22T13:27:32.327+0000", 
 				//            "id": "8a90f8c63152071c013152078ee70023", 
 				//            "owner": {
 				//                "displayName": "Admin Owner", 
 				//                "href": "/owners/admin", 
 				//                "id": "8a90f8c63152071c01315207841f0006", 
 				//                "key": "admin"
 				//            }, 
 				//            "updated": "2011-07-22T13:27:32.327+0000"
 				//        }
 				//    ], 
 				//    "updated": "2011-07-22T13:27:32.326+0000", 
 				//    "users": [
 				//        {
 				//            "created": "2011-07-22T13:27:30.827+0000", 
 				//            "hashedPassword": "05557a2aaec7cb676df574d2eb080691949a6752", 
 				//            "id": "8a90f8c63152071c01315207890b0012", 
 				//            "superAdmin": false, 
 				//            "updated": "2011-07-22T13:27:30.827+0000", 
 				//            "username": "testuser1"
 				//        }
 				//    ]
 				//}
 				String roleName = jsonRole.getString("name");
 
 				JSONArray jsonPermissions =  jsonRole.getJSONArray("permissions");
 				for (int p = 0; p < jsonPermissions.length(); p++) {
 					JSONObject jsonPermission = (JSONObject) jsonPermissions.get(p);
 					//        {
 					//            "access": "ALL", 
 					//            "created": "2011-07-22T13:27:32.327+0000", 
 					//            "id": "8a90f8c63152071c013152078ee70023", 
 					//            "owner": {
 					//                "displayName": "Admin Owner", 
 					//                "href": "/owners/admin", 
 					//                "id": "8a90f8c63152071c01315207841f0006", 
 					//                "key": "admin"
 					//            }, 
 					//            "updated": "2011-07-22T13:27:32.327+0000"
 					//        }
 					String access = jsonPermission.getString("access");
 					JSONObject jsonOwner = jsonPermission.getJSONObject("owner");
 					String ownerKey = jsonOwner.getString("key");
 					
 					// String username, String password, String roleName, String orgKey, String access
 					ll.add(Arrays.asList(new Object[]{username,password,roleName,ownerKey,access}));
 				}
 			}
 		}
 		
 		return ll;
 	}
 
 }
