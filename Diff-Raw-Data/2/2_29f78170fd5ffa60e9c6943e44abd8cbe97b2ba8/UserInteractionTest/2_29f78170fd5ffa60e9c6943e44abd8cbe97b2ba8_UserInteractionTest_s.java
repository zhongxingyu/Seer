 package tests;
 
 import static org.junit.Assert.*;
 
 import models.Answer;
 import models.Comment;
 import models.Question;
 import models.User;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import play.mvc.Http.Response;
 import play.test.FunctionalTest;
 import play.test.UnitTest;
 
 import tests.mocks.SessionMock;
 
 import controllers.Secured;
 import controllers.Session;
 
 @Ignore
public class UserInteractionTest extends UnitTest {
 
 	private User jack;
 	private SessionMock session;
 
 	@Before
 	public void setUp() throws Exception {
 		jack = new User("Jack","");
 		session = new SessionMock();
 		session.loginAs(jack);
 		Session.mockWith(session);
 	}
 	
 	@Test
 	public void shouldPostQuestion() {
 //		Response response = POST("/","","why?");
 //		assertStatus(302,response);
 //		Question question = null;
 //		for (Question q :Question.questions()) {
 //			if (q.owner().equals(jack))
 //				question = q;
 //		}
 		Question question = Secured.newQuestion("why?", "stupid");
 		assertNotNull(question);
 		assertEquals(question.owner(),jack);
 		assertTrue(Question.questions().contains(question));
 	}
 	
 	@Test
 	public void shouldPostAnswer() {
 		Question question = Secured.newQuestion("why?", "stupid");
 		Answer answer = Secured.newAnswer(question.id(), "nevermind");
 		assertNotNull(answer);
 		assertEquals(answer.owner(),jack);
 		assertTrue(question.answers().contains(answer));
 	}
 	
 	@Test
 	public void shouldPostComment() {
 		Question question = Secured.newQuestion("why?", "stupid");
 		Comment comment = Secured.newCommentQuestion(question.id(), "Could I specify?");
 		assertNotNull(comment);
 		assertEquals(comment.owner(),jack);
 		assertTrue(question.comments().contains(comment));
 	}
 
 	@Test
 	public void shouldPostAnswerComment() {
 		Question question = Secured.newQuestion("why?", "stupid");
 		Answer answer = Secured.newAnswer(question.id(), "nevermind");
 		Comment comment = Secured.newCommentAnswer(question.id(),answer.id(), "Good Point");
 		assertNotNull(comment);
 		assertEquals(comment.owner(),jack);
 		assertTrue(answer.comments().contains(comment));
 	}
 	
 	@Test
 	public void shouldVoteQuestion() {
 		Question question = Secured.newQuestion("why?", "stupid");
 		User jill = new User("Jill","");
 		session.loginAs(jill);
 		Secured.voteQuestionDown(question.id());
 		assertEquals(-1,question.rating());
 		Secured.voteQuestionUp(question.id());
 		assertEquals(1,question.rating());
 	}
 	
 	@Test
 	public void shouldVoteAnswer() {
 		Question question = Secured.newQuestion("why?", "stupid");
 		Answer answer = Secured.newAnswer(question.id(), "nevermind");
 		User jill = new User("Jill","");
 		session.loginAs(jill);
 		Secured.voteAnswerDown(question.id(),answer.id());
 		assertEquals(-1,answer.rating());
 		Secured.voteAnswerUp(question.id(),answer.id());
 		assertEquals(1,answer.rating());
 	}
 	
 	@Test
 	public void shouldDeleteQuestion() {
 		Question question = Secured.newQuestion("why?", "stupid");
 		Secured.deleteQuestion(question.id());
 		assertFalse(Question.questions().contains(question));
 	}
 	
 	@Test
 	public void shouldDeleteAnswer() {
 		Question question = Secured.newQuestion("why?", "stupid");
 		Answer answer = Secured.newAnswer(question.id(), "nevermind");
 		Secured.deleteQuestion(answer.id());
 		assertFalse(question.answers().contains(question));
 	}
 	
 	@Test
 	public void shouldDeleteQuestionComment() {
 		Question question = Secured.newQuestion("why?", "stupid");
 		Comment comment = Secured.newCommentQuestion(question.id(), "Could I specify?");
 		Secured.deleteCommentQuestion(comment.id(),question.id());
 		assertFalse(question.comments().contains(question));
 	}
 	
 	@Test
 	public void shouldDeleteAnswerComment() {
 		Question question = Secured.newQuestion("why?", "stupid");
 		Answer answer = Secured.newAnswer(question.id(), "nevermind");
 		Comment comment = Secured.newCommentAnswer(answer.id(),question.id(), "Could I specify?");
 		Secured.deleteCommentAnswer(comment.id(),question.id(),answer.id());
 		assertFalse(answer.comments().contains(question));
 	}
 }
