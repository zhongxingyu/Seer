 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.facebook.automation.testcases;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 
 import com.restfb.types.Post.Likes;
 
 public class GetPhotoLikesTestCases extends FacebookTestParent {
 	
 	@SuppressWarnings("unchecked")
 	@Before
 	public void setUp() throws Exception {
 		testObjects = (HashMap<String,Object>) context.getBean("getPhotoLikesTestData");
 		
 		String profileId = getProfileId();
 		testObjects.put("profileId", profileId);
 		
 		String caption = (String) testObjects.get("caption");
 		String photoFileName = (String) testObjects.get("photoFileName");
 		
 		File photo = new File(getClass().getClassLoader().getResource(photoFileName).toURI());
 		String photoId = publishPhoto(profileId, caption, photo);
 		
 		// for "get-photo-likes"
 		testObjects.put("photo", photoId);
 		
 		like(photoId);
 	}
 	
 	@Category({RegressionTests.class})
 	@Test
 	public void testGetPhotoLikes() {
 		try {
 			MessageProcessor flow = lookupFlowConstruct("get-photo-likes");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 
 			Likes result = (Likes) response.getMessage().getPayload();
 			assertTrue(result.getData().size() == 1);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
     @After
 	public void tearDown() throws Exception {
    	String photoId = (String) testObjects.get("photoId");
     	deleteObject(photoId);
 	}
     
 }
