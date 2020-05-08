 package org.mule.module.cmis.automation.testcases;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.ObjectId;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 
 public class DeleteTestCases extends CMISTestParent {
 	
 	@SuppressWarnings("unchecked")
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (HashMap<String, Object>) context.getBean("delete");
 			testObjects.put("parentObjectId", rootFolderId());
 			ObjectId result = createFolder((String) testObjects.get("folderName"), (String) testObjects.get("parentObjectId"));
 			
 			testObjects.put("objectId", result.getId());
 			testObjects.put("cmisObjectRef", getObjectById(result.getId()));
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	@Category({SmokeTests.class, RegressionTests.class})
 	@Test
 	public void testDelete() {
 		try {
 			Object result = delete((CmisObject) testObjects.get("cmisObjectRef"), (String) testObjects.get("objectId"), (Boolean) testObjects.get("allVersions"));
 			assertNotNull(result);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	@Category({SmokeTests.class, RegressionTests.class})
 	@Test
 	public void testDelete_null_payload() {
 		try {
 			Object result = delete(null, (String) testObjects.get("objectId"), (Boolean) testObjects.get("allVersions"));
 			assertNotNull(result);
 		} catch (Exception e) {
 			e.printStackTrace();
			fail();
 		}
 	}
 	
 	@Category({SmokeTests.class, RegressionTests.class})
 	@Test
 	public void testDelete_with_cmisObjectRef() {
 		try {
 			Object result = delete(lookupFlowConstruct("delete-with-cmis-object-ref"), testObjects, 
 					(String) testObjects.get("objectId"), (Boolean) testObjects.get("allVersions"));
 			assertNotNull(result);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 }
