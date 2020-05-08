 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static play.test.Helpers.fakeApplication;
 import static play.test.Helpers.inMemoryDatabase;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import models.Request;
 import models.Student;
 import models.TMSession;
 import models.Tutor;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.FakeApplication;
 import play.test.Helpers;
 
 
 
 public class SessionTest {
 
   private static FakeApplication app;
 
   private static final String TEST_USERNAME = "Test Username";
   private static final String TEST_EMAIL = "test@email.com";
   private static final String TEST_NAME = "Test Name";
   private static final String TEST_PASSWORD = "Test Password";
   private static final String TEST_SALT = "1";
 
   private static final String MATH_SUBJECT = "MATH";
   private static final String HISTORY_SUBJECT = "HISTORY";
   private static final String SCIENCE_SUBJECT = "SCIENCE";
   private static final double CHEAP_COST = 1.0;
   private static final double EXPENSIVE_COST = 100.0;
   private static final double LOW_RATING = 1.0;
   private static final double HIGH_RATING = 5.0;
 
   private Student testStudent;
   private Tutor testMathTutor;
   private Tutor testHistoryTutorCheapHighRated;
   private Tutor testHistoryTutorCheapLowRated;
   private Tutor testHistoryTutorExpensiveHighRated;
   private Tutor testHistoryTutorExpensiveLowRated;
   private Tutor testMultiSubjectTutor;
 
   private Request testFarFutureRequest;
   private Request testFutureRequest;
   private Request testFarPastRequest;
   private Request testPastRequest;
   private Request testPresentRequest;
 
 
   @Before
   public void setUp() {
     app = fakeApplication(inMemoryDatabase());
     Helpers.start(app);
 
     testStudent = new Student();
     testStudent.setUsername(TEST_USERNAME);
     testStudent.setEmail(TEST_EMAIL);
     testStudent.setName(TEST_NAME);
     testStudent.setSalt(TEST_SALT);
     testStudent.setPwhash(Student.encrypt(TEST_PASSWORD, TEST_SALT));
     testStudent.save();
 
     testMathTutor = new Tutor();
     testMathTutor.setSubjects(Arrays.asList(MATH_SUBJECT));
     testMathTutor.setCostUSD(EXPENSIVE_COST);
     testMathTutor.setRating(LOW_RATING);
     testMathTutor.save();
 
     testHistoryTutorCheapHighRated = new Tutor();
     testHistoryTutorCheapHighRated.setSubjects(Arrays.asList(HISTORY_SUBJECT));
     testHistoryTutorCheapHighRated.setCostUSD(CHEAP_COST);
     testHistoryTutorCheapHighRated.setRating(HIGH_RATING);
     testHistoryTutorCheapHighRated.save();
 
     testHistoryTutorCheapLowRated = new Tutor();
     testHistoryTutorCheapLowRated.setSubjects(Arrays.asList(HISTORY_SUBJECT));
     testHistoryTutorCheapLowRated.setCostUSD(CHEAP_COST);
     testHistoryTutorCheapLowRated.setRating(LOW_RATING);
     testHistoryTutorCheapLowRated.save();
 
     testHistoryTutorExpensiveHighRated = new Tutor();
     testHistoryTutorExpensiveHighRated.setSubjects(Arrays
         .asList(HISTORY_SUBJECT));
     testHistoryTutorExpensiveHighRated.setCostUSD(EXPENSIVE_COST);
     testHistoryTutorExpensiveHighRated.setRating(HIGH_RATING);
     testHistoryTutorExpensiveHighRated.save();
 
     testHistoryTutorExpensiveLowRated = new Tutor();
     testHistoryTutorExpensiveLowRated.setSubjects(Arrays
         .asList(HISTORY_SUBJECT));
     testHistoryTutorExpensiveLowRated.setCostUSD(EXPENSIVE_COST);
     testHistoryTutorExpensiveLowRated.setRating(LOW_RATING);
     testHistoryTutorExpensiveLowRated.save();
 
     testMultiSubjectTutor = new Tutor();
     testMultiSubjectTutor.setSubjects(Arrays.asList(HISTORY_SUBJECT,
         SCIENCE_SUBJECT));
     testMultiSubjectTutor.save();
 
     long oneMinute = 1000*60;
     long oneHour = oneMinute*60;
     long oneDay = oneHour*24;
     Date date = new Date();
     testFarFutureRequest = new Request(testStudent, testMathTutor, date.getTime()+30*oneDay, date.getTime()+30*oneDay+oneHour);
     testFutureRequest = new Request(testStudent, testMathTutor, date.getTime()+3*oneDay, date.getTime()+3*oneDay+oneHour);
     testFarPastRequest = new Request(testStudent, testMathTutor, date.getTime()-30*oneDay, date.getTime()-30*oneDay+oneHour);
     testPastRequest = new Request(testStudent, testMathTutor, date.getTime()-3*oneDay, date.getTime()-3*oneDay+oneHour);
     testPresentRequest = new Request(testStudent, testMathTutor, date.getTime()-oneHour, date.getTime()+oneHour);
   }
 
 	/**
 	 * Test overlapping sessions
 	 */
 	@Test
 	public void testOverlappingSession(){
 	//Should not be able to create session if overlapping session exists
		long oneMinute = 1000*60;
    long oneHour = oneMinute*60;
    long oneDay = oneHour*24;
    Date date = new Date();
     Request overlapping1 = new Request(testStudent, testMathTutor, date.getTime()+30*oneDay, date.getTime()+30*oneDay+oneHour);
     Request overlapping2 = new Request(testStudent, testHistoryTutorExpensiveHighRated, date.getTime()+30*oneDay, date.getTime()+30*oneDay+oneHour);
     TMSession testOverlapping1 = overlapping1.generateSession();
    TMSession testOverlapping2 = overlapping2.generateSession();
    assertTrue(testOverlapping2==null);
 	}
 	/**
 	 * Test creating same session again
 	 */
 	@Test
 	public void testSameSession(){
 		//Session should not be created if it already exist
   	TMSession testRepeatSession = testFarFutureRequest.generateSession();
   	TMSession testRepeatSession1 = testFarFutureRequest.generateSession();
    assertTrue(testRepeatSession1==null);
 	}
   /**
    * Test past sessions
    */
   @Test
   public void testPastSession() {
     //Should not be able to create a session in the past
     TMSession testPastSession;
     testPastSession = testPastRequest.generateSession();
    assertTrue(testPastSession==null);
     }    

 
   /**
    * Tests accepting a request
    */
   @Test
   public void testAcceptRequest() {
     // The list of requests after responding should be the same as the list
     // before without the accepted request
     // The upcoming sessions should include the request and the other sessions
     // should be unchanged
     List<Request> beforeRequests = testMathTutor.getRequests();
     List<TMSession> beforeUpcomingSessions = testMathTutor.getUpcomingSessions();
     List<TMSession> beforeCurrentSessions = testMathTutor.getCurrentSessions();
     List<TMSession> beforeCompletedSessions = testMathTutor.getCompletedSessions();
     testMathTutor.respondToRequest(testFutureRequest, true);
     List<Request> afterRequests = testMathTutor.getRequests();
     List<TMSession> afterUpcomingSessions = testMathTutor.getUpcomingSessions();
     List<TMSession> afterCurrentSessions = testMathTutor.getCurrentSessions();
     List<TMSession> afterCompletedSessions = testMathTutor.getCompletedSessions();
 
     List<Request> tempRequests = beforeRequests;
     tempRequests.remove(testFutureRequest);
     assertListEqualsUnordered(tempRequests, afterRequests);
     List<TMSession> tempSessions = beforeUpcomingSessions;
     TMSession newSession = testFutureRequest.generateSession();
     tempSessions.add(newSession);
 
     assertListEqualsUnordered(tempSessions, afterUpcomingSessions);
     assertListEqualsUnordered(beforeCurrentSessions, afterCurrentSessions);
     assertListEqualsUnordered(beforeCompletedSessions, afterCompletedSessions);
   }
 
   /**
    * Tests canceling a request
    */
   @Test
   public void testCancelRequest() {
     // The list of requests after responding should be the same as the list
     // before without the canceled request
     // The other sessions should be unchanged
     List<Request> beforeRequests = testMathTutor.getRequests();
     List<TMSession> beforeUpcomingSessions = testMathTutor.getUpcomingSessions();
     List<TMSession> beforeCurrentSessions = testMathTutor.getCurrentSessions();
     List<TMSession> beforeCompletedSessions = testMathTutor.getCompletedSessions();
     testFutureRequest.getRequestingStudent().cancelRequest(testFutureRequest);
     List<Request> afterRequests = testMathTutor.getRequests();
     List<TMSession> afterUpcomingSessions = testMathTutor.getUpcomingSessions();
     List<TMSession> afterCurrentSessions = testMathTutor.getCurrentSessions();
     List<TMSession> afterCompletedSessions = testMathTutor.getCompletedSessions();
 
     List<Request> tempRequests = beforeRequests;
     tempRequests.remove(testFutureRequest);
     assertListEqualsUnordered(tempRequests, afterRequests);
     assertListEqualsUnordered(beforeUpcomingSessions, afterUpcomingSessions);
     assertListEqualsUnordered(beforeCurrentSessions, afterCurrentSessions);
     assertListEqualsUnordered(beforeCompletedSessions, afterCompletedSessions);
   }
 
   public void assertListEqualsUnordered(List<?> expectedList, List<?> actualList) {
     assertEquals(expectedList.size(), actualList.size());
     for (Object object : expectedList) {
       assertTrue(actualList.contains(object));
     }
   }
 
   @After
   public void tearDown() {
     Helpers.stop(app);
   }
 }
