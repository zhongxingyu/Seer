 package services;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNull;
 import static junit.framework.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import models.Poll;
 import models.Question;
 import models.QuestionChoice;
 import models.User;
 import models.reference.PollStatus;
 
 import org.junit.Test;
 
 import services.exception.poll.NoChoiceException;
import services.exception.user.AnonymousUserAlreadyAnsweredPoll;
 import util.UserTestUtil;
 import util.security.SessionUtil;
 
 public class QuestionServiceTest extends ProtoPollTest {
 
 	private static final int CHOICE_NUMBER = 3;
 
 	@Test
 	public void testCreateQuestionAnonymousUser() {
 		// Act.
 		final Question question = createQuestion();
 
 		// Assert.
 		final Question loadedQuestion = PollService
 				.getQuestion(question.uuid());
 		assertNotNull(loadedQuestion);
 		assertNotNull(question.poll);
 		assertNull(loadedQuestion.poll.userCreator);
 		assertEquals(0, loadedQuestion.choices.size());
 	}
 
 	@Test
 	public void testCreateQuestionRegistered() {
 		// Prepare.
 		final User user = UserTestUtil.getAuthenticatedUser();
 		assertNotNull(SessionUtil.currentUser());
 
 		// Act.
 		final Question question = createQuestion();
 
 		// Assert.
 		final Question loadedQuestion = PollService
 				.getQuestion(question.uuid());
 		assertNotNull("id", loadedQuestion.id);
 		assertNotNull("poll", loadedQuestion.poll);
 		assertNotNull("user", loadedQuestion.poll.userCreator);
 		assertEquals("user id", user.id, loadedQuestion.poll.userCreator.id);
 		assertEquals("size", 0, loadedQuestion.choices.size());
 	}
 
 	@Test
 	public void testSetChoices() {
 		// Prepare.
 		final Question question = createQuestion();
 
 		final List<QuestionChoice> choices = new ArrayList<QuestionChoice>();
 
 		final QuestionChoice choice1 = new QuestionChoice();
 		choice1.label = "Choice 1";
 		choice1.sortOrder = 2;
 		choices.add(choice1);
 
 		final QuestionChoice choice2 = new QuestionChoice();
 		choice2.label = "Choice 2";
 		choice2.sortOrder = 1;
 		choices.add(choice2);
 
 		// Act.
 		QuestionService.saveChoices(question.uuid(), choices);
 
 		// Assert.
 		final Question loadedQuestion = PollService
 				.getQuestion(question.uuid());
 		assertEquals(choices.size(), loadedQuestion.choices.size());
 		assertEquals(choices.get(0).label, loadedQuestion.choices.get(1).label);
 		assertEquals(choices.get(1).label, loadedQuestion.choices.get(0).label);
 	}
 
 	@Test(expected = NoChoiceException.class)
 	public void testNoChoiceDefinedNull() {
 		// Prepare.
 		final Question question = createQuestion();
 
 		// Act.
 		QuestionService.saveChoices(question.uuid(), null);
 	}
 
 	@Test(expected = NoChoiceException.class)
 	public void testNoChoiceDefinedEmpty() {
 		// Prepare.
 		final Question question = createQuestion();
 
 		// Act.
 		QuestionService.saveChoices(question.uuid(),
 				new ArrayList<QuestionChoice>());
 	}
 
 	@Test
 	public void testAnswerPollAnonymous() {
 		// Prepare.
 		Question question = createQuestion();
 		question = addChoices(question);
 
 		Set<Long> choiceIds = new HashSet<Long>();
 		choiceIds.add(question.choices.get(0).id);
 
 		// Act.
 		PollService
 				.answerPollAnonymous("test user", question.uuid(), choiceIds);
 
 		// Assert.
 		Question reloadedQuestion = PollService.getQuestion(question.uuid());
 		assertEquals(1, reloadedQuestion.answers.size());
 		assertEquals(choiceIds.size(),
 				reloadedQuestion.answers.get(0).details.size());
 		assertTrue(choiceIds.contains(reloadedQuestion.answers.get(0).details
 				.get(0).choice.id));
 		// FIXME Missing user checks!
 	}
 
	@Test(expected = AnonymousUserAlreadyAnsweredPoll.class)
	public void testAnswerPollAnonymousAlreadyUsedLogin() {
		// Prepare.
		String userLogin = "test user";

		Question question = createQuestion();
		question = addChoices(question);

		Set<Long> choiceIds = new HashSet<Long>();
		choiceIds.add(question.choices.get(0).id);

		PollService.answerPollAnonymous(userLogin, question.uuid(), choiceIds);

		// Act.
		PollService.answerPollAnonymous(userLogin, question.uuid(), choiceIds);
	}

 	@Test
 	public void testStatusCreated() {
 		// Prepare / Act.
 		Question question = createQuestion();
 
 		// Assert.
 		assertEquals(PollStatus.DRAFT, question.poll.status);
 	}
 
 	@Test
 	public void testStatusCreatedWithChoices() {
 		// Prepare.
 		Question question = createQuestion();
 
 		// Act.
 		question = addChoices(question);
 
 		// Assert.
 		assertEquals(PollStatus.COMPLETE, question.poll.status);
 	}
 
 	private static Question createQuestion() {
 		Poll poll = Poll.initQuestion();
 		poll.title = "question title";
 		PollService.createPoll(poll);
 		return PollService.getPoll(poll.uuid).question;
 	}
 
 	private static Question addChoices(Question question) {
 		List<QuestionChoice> choices = new ArrayList<QuestionChoice>();
 		QuestionChoice choice;
 		for (int i = 0; i < CHOICE_NUMBER; i++) {
 			choice = new QuestionChoice();
 			choice.label = "Choice " + i;
 			choice.sortOrder = i;
 			choice.question = question;
 			choices.add(choice);
 		}
 		QuestionService.saveChoices(question.uuid(), choices);
 		return PollService.getQuestion(question.uuid());
 	}
 }
