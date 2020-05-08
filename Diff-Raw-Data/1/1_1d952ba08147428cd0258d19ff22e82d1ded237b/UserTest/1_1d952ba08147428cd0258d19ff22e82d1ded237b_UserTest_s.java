 import java.util.GregorianCalendar;
 
 import models.Answer;
 import models.DbManager;
 import models.Question;
 import models.User;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 public class UserTest extends UnitTest {
 	private static DbManager manager;
 	private User admin;
 
 	@Before
 	public void setUp() {
 		manager = DbManager.getInstance();
 		admin = new User("admin", "admin@admin.ch", "admin");
 	}
 
 	@Test
 	public void shouldGetRightScore() {
 		User topScorer = new User("scorer", "user@champion", "password");
 		Question question = new Question(true, "Good Question", topScorer);
 		Answer answer = new Answer(true, "Good Answer", topScorer, question);
 		question.vote(1);
 		answer.vote(2);
 		assertEquals(3, topScorer.getScore());
 	}
 
 	@Test
 	public void shouldAddUserLog() {
 		User logTester = new User("logTester", "test@log", "pw");
 		logTester.addActivity("Activity1");
 		logTester.addActivity("Activity2");
 		assertEquals(3, logTester.getActivities().size());
 		assertEquals("Activity2", logTester.getActivities().get(0));
 	}
 	
 	@Test
 	public void shouldUpdateReputation() {
		System.out.println("shouldUpdateReputation");
 		User reputatedUser = new User("reputatedUser", "rep.user@ese.ch", "1234");
 		GregorianCalendar twoDaysAgo = new GregorianCalendar();
 		twoDaysAgo.setTimeInMillis(twoDaysAgo.getTimeInMillis()-2*24*60*60*1000);
 		reputatedUser.setLastTimeOfReputation(twoDaysAgo);
 		reputatedUser.addReputation(3);
 		reputatedUser.setLastReputation(4);
 		assertEquals(2,reputatedUser.getReputations().size());
 		assertEquals(4,(int)reputatedUser.getReputations().get(0)); //yesterday
 		assertEquals(3,(int)reputatedUser.getReputations().get(1)); //the day before yesterday
 	}
 	
 	@AfterClass
 	public static void tearDown() {
 		manager.getUsers().clear();
 		manager.getQuestions().clear();
 		manager.getAnswers().clear();
 		manager.getTagList().clear();
 		manager.resetAllIdCounts();
 	}
 }
