 package se.diversify.webroti.data;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 public class MeetingTest {
     private Meeting testedObject;
 
     @Before
     public void before() {
        testedObject = new Meeting("000");
         testedObject.addVote(new Vote(1));
         testedObject.addVote(new Vote(5));
     }
 
     @Test
     public void testGetVotes() throws Exception {
         List<Vote> votes = testedObject.getVotes();
         assertEquals(2, votes.size());
         assertEquals(1.0, votes.get(0).getValue());
         assertEquals(5.0, votes.get(1).getValue());
     }
 
     @Test
     public void testGetId() throws Exception {
         assertNotNull(testedObject.getId());
     }
 
     @Test
     public void testAddVote() throws Exception {
         testedObject.addVote(new Vote(3));
         List<Vote> votes = testedObject.getVotes();
         assertEquals(3, votes.size());
         assertEquals(3.0, votes.get(2).getValue());
     }
 }
