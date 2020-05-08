 import models.Answer;
 import models.Comment;
 import models.DbManager;
 import models.Post;
 import models.Question;
 import models.Search;
 import models.User;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 public class SearchTest extends UnitTest {
 	private static DbManager manager;
 	private User admin;
 
 	@Before
 	public void setUp() {
 		manager = DbManager.getInstance();
 		admin = new User("admin", "admin@admin.ch", "admin");
 	}
 
 	@Test
 	public void shouldNotBeUpperLowerCaseSensitive() {
 		Question question3 = new Question(true, "question1", admin);
 		Answer answer3 = new Answer(true, "answer1", admin, question3);
 		question3.addTags("test");
 		Comment commentQuestion = new Comment(admin, question3, "comment");
 		Comment commentAnswer = new Comment(admin, answer3, "comment1");
 
 		Search search = new Search("QuEstion1");
 		assertEquals("question1", search.getQuestionContentResults().get(0)
 				.getContent());
 
 		Search search1 = new Search("AnSWer1");
 		assertEquals("answer1", search1.getAnswerContentResults().get(0)
 				.getContent());
 
 		Search search2 = new Search("cOMMent");
 		assertEquals("comment", search2.getCommentResults().get(0).getContent());
 		assertEquals("comment1", search2.getCommentResults().get(1)
 				.getContent());
 
 		Search search3 = new Search("TesT");
 		assertEquals("test", search3.getQuestionTagsResults().get(0).getTags()
 				.get(0));
 	}
 
 	@Test
 	public void shouldSearchQuestionTags() {
 		Question question1 = new Question(true, "question1", admin);
 		question1.addTags("test");
 
 		Search search = new Search("test");
 		assertEquals("test", search.getQuestionTagsResults().get(0).getTags()
 				.get(0));
 	}
 
 	@Test
 	public void shouldSearchQuestionContent() {
 		Post question1 = new Question(true, "question1", admin);
 		Search search = new Search("question1");
 		assertEquals("question1", search.getQuestionContentResults().get(0)
 				.getContent());
 	}
 
 	@Test
 	public void shouldSearchAnswerContent() {
 		Question question1 = new Question(true, "question1", admin);
 		Answer answer1 = new Answer(true, "answer1", admin, question1);
 
 		Search search = new Search("answer1");
 		assertEquals("answer1", search.getAnswerContentResults().get(0)
 				.getContent());
 
 	}
 
 	@Test
 	public void shouldSearchComments() {
 		Question question1 = new Question(true, "question1", admin);
 		Answer answer1 = new Answer(true, "answer1", admin, question1);
 
 		Comment commentQuestion = new Comment(admin, question1,
 				"commentToQuestion1");
 		Comment commentAnswer = new Comment(admin, answer1, "commentToAnswer1");
 
 		Search search = new Search("commentToQuestion1");
 		assertEquals("commentToQuestion1", search.getCommentResults().get(0)
 				.getContent());
 
 		Search search1 = new Search("commentToAnswer1");
 		assertEquals("commentToAnswer1", search1.getCommentResults().get(0)
 				.getContent());
 	}
 
 	@Test
 	public void shouldAddNotingWhenNoMatches() {
 		Question question1 = new Question(true, "question1", admin);
 		Answer answer1 = new Answer(true, "answer1", admin, question1);
 		question1.addTags("test");
 		Comment commentQuestion = new Comment(admin, question1,
 				"commentToQuestion1");
 		Comment commentAnswer = new Comment(admin, answer1, "commentToAnswer1");
 
 		Search search = new Search("nothing");
 
 		assertEquals(0, search.getAnswerContentResults().size());
 		assertEquals(0, search.getCommentResults().size());
 		assertEquals(0, search.getQuestionContentResults().size());
 		assertEquals(0, search.getQuestionTagsResults().size());
 	}
 
 	@Test
 	public void shouldAddQuestionOnlyOnceWhenMoreThanOneTagMatches() {
 		Question question1 = new Question(true, "question1", admin);
 		question1.addTags("testA");
		question1.addTags("testB");
 
 		Search search = new Search("test");
 
 		assertEquals(2, question1.getTags().size());
		assertEquals(1, search.getQuestionTagsResults().size());
 
 	}
 
 	@After
 	public void tearDown() {
 		manager.getUsers().clear();
 		manager.getQuestions().clear();
 		manager.getAnswers().clear();
 		manager.resetAllIdCounts();
 		manager.getTagList().clear();
 	}
 }
