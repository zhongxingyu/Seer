 package com.imaginea.android.sugarcrm.restapi;
 
 import android.test.suitebuilder.annotation.LargeTest;
 import android.test.suitebuilder.annotation.SmallTest;
 import android.util.Log;
 
 import com.imaginea.android.sugarcrm.ModuleFields;
 import com.imaginea.android.sugarcrm.util.RestUtil;
 import com.imaginea.android.sugarcrm.util.SugarBean;
 
 /**
  * ContactsApiTest, tests the rest api calls
  * 
  * @author chander
  * 
  */
 public class ContactsApiTest extends RestAPITest {
 	String moduleName = "Contacts";
 
 	String[] fields = new String[] {};
 
 	String[] customFields = new String[] { "a", "b" };
 
 	String[] selectFields = { ModuleFields.FIRST_NAME, ModuleFields.LAST_NAME,
 			ModuleFields.EMAIL1 };
 	String[] linkNameToFieldsArray = new String[] {};
 
 	public final static String LOG_TAG = "ContactsApiTest";
 
 	@SmallTest
 	public void testGetAllModuleFields() throws Exception {
 
 		// RestUtil.getModuleFields(url, mSessionId, moduleName, fields);
 	}
 
 	@SmallTest
 	public void testGetCustomModuleFields() throws Exception {
 
 		// RestUtil.getModuleFields(url, mSessionId, moduleName, customFields);
 	}
 
 	@SmallTest
 	public void testContactsList() throws Exception {
 		int offset = 0;
 		int maxResults = 10;
 		// String[] selectFields = new String[] {};
 		SugarBean[] sBeans = getSugarBeans(offset, maxResults);
 		assertTrue(sBeans.length > 0);
 
 		if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
 			for (SugarBean sBean : sBeans) {
 				;
 				Log.d(LOG_TAG, sBean.getBeanId());
 				Log.d(LOG_TAG, sBean.getFieldValue(ModuleFields.EMAIL1));
 			}
 		}
 	}
 
 	@LargeTest
 	public void testEntireContactList() throws Exception {
 		int offset = 0;
 		int maxResults = 20;
 		// String[] selectFields = new String[] {};
 		SugarBean[] sBeans = getSugarBeans(offset, maxResults);
 		int totalRuns = 1;
 		while (sBeans.length > 0) {
 			offset += 20;
 			sBeans = getSugarBeans(offset, maxResults);
 			totalRuns++;
 		}
 		Log.d(LOG_TAG, "Total Runs:" + totalRuns);
 	}
 
 	/**
 	 * demonstrates the usage of RestUtil for contacts List. ModuleFields.NAME
 	 * or FULL_NAME is not returned by Sugar CRM. The fields that are not
 	 * returned by SugarCRM can be automated, but not yet generated
 	 * 
 	 * @param offset
 	 * @param maxResults
 	 * @return
 	 * @throws Exception
 	 */
 	private SugarBean[] getSugarBeans(int offset, int maxResults)
 			throws Exception {
 		String query = "", orderBy = "";
 
 		int deleted = 0;
 
 		SugarBean[] sBeans = RestUtil.getEntryList(url, mSessionId, moduleName,
				query, orderBy, offset+"", selectFields, linkNameToFieldsArray,
				maxResults+"", deleted+"");
 		return sBeans;
 	}
 
 }
