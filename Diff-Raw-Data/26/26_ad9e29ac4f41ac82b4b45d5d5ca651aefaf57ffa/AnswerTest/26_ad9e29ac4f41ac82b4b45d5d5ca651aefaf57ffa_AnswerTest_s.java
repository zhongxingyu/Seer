 import models.Answer;
 import models.DbManager;
 import models.Question;
 import models.User;
 import models.Vote;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 public class AnswerTest extends UnitTest {
 	private static DbManager manager;
 	private User admin;
 
 	@Before
 	public void setUp() {
 		manager = DbManager.getInstance();
 		admin = new User("admin", "admin@admin.ch", "admin");
 	}
 
 	@Test
 	public void shouldCheckUserAlreadyVotedAnswer() {
 		Question question = new Question(true, "content of question", "title",
 				admin);
 		Answer answer = new Answer(true, "content of answer", admin, question);
 		assertFalse(answer.checkUserVotedForPost(admin));
 		answer.userVotedForPost(admin);
 		assertTrue(answer.checkUserVotedForPost(admin));
 	}
 
 	@Test
 	public void shouldVoteAnswer() {
 		Question question = new Question(true, "content of question", "title",
 				admin);
 		Answer answer = new Answer(true, "content of answer", admin, question);
 		assertEquals(0, answer.getScore());
 		// Vote Up
 		Vote vote1 = new Vote(answer, 1, admin);
 		answer.vote(vote1);
 		assertEquals(1, answer.getScore());
 		// Vote down
 		Vote vote2 = new Vote(answer, -1, admin);
 		answer.vote(vote2);
 		assertEquals(0, answer.getScore());
 	}
 	
 	@Test
 	public void shouldAddVersionAndRestore(){
 		Question q = new Question("content", "title", admin);
		Answer a= new Answer("content of", admin, q);
 		a.addVersion("content of answer", "admin");
 		assertEquals(1, a.getOldVersions().size());
 		a.restoreOldVersion("content of", "admin");
		assertEquals(2,a.getOldVersions().size());
 		assertEquals("content of", a.getContent());
 	}
 
 	@AfterClass
 	public static void tearDown() {
 		manager.getUsers().clear();
 		manager.clearQuestionsMap();
 		manager.clearAnswerMap();
 		manager.resetAllIdCounts();
 		manager.getComments().clear();
 		manager.getTagList().clear();
 	}
 
 }
