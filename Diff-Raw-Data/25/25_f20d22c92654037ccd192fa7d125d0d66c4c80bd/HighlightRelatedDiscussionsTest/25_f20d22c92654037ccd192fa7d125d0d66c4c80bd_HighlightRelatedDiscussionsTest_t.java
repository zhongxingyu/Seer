 package org.computer.knauss.reqtDiscussion.ui.ctrl;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class HighlightRelatedDiscussionsTest {
 
 	private HighlightRelatedDiscussions hrw;
 
 	@Before
 	public void setUp() throws Exception {
 		this.hrw = new HighlightRelatedDiscussions();
 	}
 
 	@Test
 	public void testWithFirstIDExisting() {
		int[] wiIDs = this.hrw.getRelatedDiscussionIDs(13764);
 
 		assertEquals(3, wiIDs.length);
 		assertEquals(13764, wiIDs[0]);
 		assertEquals(40774, wiIDs[1]);
 		assertEquals(40775, wiIDs[2]);
 	}
 
 	@Test
 	public void testWithLaterIDExisting() {
		int[] wiIDs = this.hrw.getRelatedDiscussionIDs(40774);
 
 		assertEquals(3, wiIDs.length);
 		assertEquals(13764, wiIDs[0]);
 		assertEquals(40774, wiIDs[1]);
 		assertEquals(40775, wiIDs[2]);
 	}
 	
 	@Test
 	public void testNonExisting() { 
		int[] wiIDs = this.hrw.getRelatedDiscussionIDs(0123);
 		
 		assertEquals(1, wiIDs.length);
 		assertEquals(0123, wiIDs[0]);
 	}
 
 }
