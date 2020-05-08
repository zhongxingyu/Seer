 package org.mule.module.cmis.automation.testcases;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import org.apache.chemistry.opencmis.client.api.ObjectId;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.module.cmis.VersioningState;
 
 public class CheckOutTestCases extends CMISTestParent {
 
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (Map<String, Object>) context.getBean("checkOut");
 			
 			String rootFolderId = rootFolderId();
 			String filename = testObjects.get("filename").toString();
 			String mimeType = testObjects.get("mimeType").toString();
 			String content = testObjects.get("content").toString();
 			String objectType = testObjects.get("objectType").toString();
 			Map<String, Object> propertiesRef = (Map<String, Object>) testObjects.get("propertiesRef");
 			VersioningState versioningState = (VersioningState) testObjects.get("versioningState");
 			
 			ObjectId documentObjectId = createDocumentById(rootFolderId, filename, content, mimeType, versioningState, objectType, propertiesRef);
 			testObjects.put("documentObjectId", documentObjectId);
 			testObjects.put("documentId", documentObjectId.getId());
			
			String checkinComment = (String) testObjects.get("checkinComment");
			Boolean major = (Boolean) testObjects.get("major");
			Map<String, Object> properties = (Map<String, Object>) testObjects.get("properties");
			
			ObjectId checkInObjectId = checkIn(checkinComment, documentObjectId.getId(), filename, content, mimeType, major, properties);
			testObjects.put("checkInObjectId", checkInObjectId);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({SmokeTests.class, RegressionTests.class})
 	@Test
 	public void testCheckOut() {
 		try {
			ObjectId checkInObjectId = (ObjectId) testObjects.get("checkInObjectId");
			
 			MessageProcessor flow = lookupFlowConstruct("check-out");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 			
 			ObjectId pwcObjectId = (ObjectId) response.getMessage().getPayload();
			assertTrue(pwcObjectId.getId().equals(checkInObjectId.toString()));
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@After
 	public void tearDown() {
 		try {
 			String objectId = (String) testObjects.get("documentId");
 			delete(getObjectById(objectId), objectId, true);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 }
