 package org.mule.module.facebook.automation.testcases;
 
 	import java.util.HashMap;
 	import java.util.List;
 
 	import static org.junit.Assert.*;
 	import static org.junit.Assert.assertTrue;
 	import static org.junit.Assert.fail;
 
 	import org.junit.After;
 	import org.junit.Before;
 	import org.junit.Test;
 	import org.mule.api.MuleEvent;
 	import org.mule.api.MuleException;
 	import org.mule.api.processor.MessageProcessor;
 
 	import com.restfb.types.Note;
 	
 	
 public class GetNoteTestCase extends FacebookTestParent {
 
 		@Before
 		public void tearUp() {
 			testObjects = (HashMap<String, Object>) context
 					.getBean("publishNoteTestData");
 			String msg = testObjects.get("msg").toString();
 			String subject = testObjects.get("subject").toString();
 
 			try {
 				String profileId = getProfileId();
 				testObjects.put("profileId", profileId);
 				String noteid = publishNote(getProfileId(), msg, subject);
				testObjects.put("note", noteid);
 			} catch (Exception e) {
 
 				e.printStackTrace();
 				fail();
 			}
 		}
 
 		@Test
 		public void testGetNote() {
			String noteId = testObjects.get("note").toString();
 
 			MessageProcessor flow = lookupFlowConstruct("get-note");
 			try {
 				
 				MuleEvent response = flow.process(getTestEvent(testObjects));
 				Note note = (Note) response.getMessage().getPayload();
 
 				assertTrue(note.getMessage().contains(testObjects.get("msg").toString()));
 				assertEquals(note.getSubject(), testObjects.get("subject").toString());	
 				
 			} catch (Exception e) {
 				fail();
 				e.printStackTrace();
 			}
 		}
 
 		@After
 		public void tearDown() {
			String noteId = testObjects.get("note").toString();
 			try {
 				deleteObject(noteId);
 			} catch (Exception e) {
 				e.printStackTrace();
 				fail();
 			}
 		}
 	}
 
