 package com.imaginea.android.sugarcrm.restapi;
 
 import java.util.List;
 
 import junit.framework.TestCase;

import org.json.JSONObject;

 import android.test.AndroidTestCase;
 import android.test.suitebuilder.annotation.SmallTest;
 import android.util.Log;
 
 import com.imaginea.android.sugarcrm.util.RestUtil;
 
 /**
  * 
  * Extend this class for module specific unit tests.
  * 
  * Since this test doesn't need a {@link android.content.Context}, or any other
  * dependencies injected, it simply extends the standard {@link TestCase}.
  * 
  * See {@link com.imaginea.android.sugarcrm.AllTests} for documentation on
  * running all tests and individual tests in this application.
  */
 public class RestAPITest extends AndroidTestCase {
 
 	/**
 	 * used by module specific unit tests
 	 */
 	protected String url = "http://192.168.1.83/sugarcrm/service/v2/rest.php";
 
 	protected String userName = "will";
 
 	protected String password = "will";
 
 	protected String mSessionId;
 
 	@Override
 	protected void setUp() throws Exception {
 
 		super.setUp();
 		authenticate();
 	}
 
 	public void authenticate() throws Exception {
 
 		mSessionId = RestUtil.loginToSugarCRM(url, userName, password);
 		assertNotNull(mSessionId);
 	}
 
 	/*@SmallTest
 	public void testGetModules() throws Exception {
 		List<String> modules = RestUtil.getAvailableModules(url, mSessionId);
 		for (String module : modules) {
 			Log.d("RestAPITest", "\t" + module);
 		}
 	}*/
 
 	@SmallTest
     public void testGetServerInfo() throws Exception{
         String response = RestUtil.getServerInfo(url);
         System.out.println("getServerInfo : " + response);
         
         JSONObject jsonResponse = new JSONObject(response);
         assertNotNull(jsonResponse.get("flavor").toString());
         assertNotNull(jsonResponse.get("version").toString());
         assertNotNull(jsonResponse.get("gmt_time").toString());
     }
 
 }
