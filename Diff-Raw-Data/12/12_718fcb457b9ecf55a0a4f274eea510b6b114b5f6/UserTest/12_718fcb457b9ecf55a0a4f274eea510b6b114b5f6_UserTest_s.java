 package tests;
 
 import java.text.ParseException;
 
 import models.Question;
 import models.User;
 import models.database.Database;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 public class UserTest extends UnitTest {
 
 	@Before
 	public void setUp() {
 		Database.clear();
 	}
 
 	@Test
 	public void shouldCreateUser() {
 		User user = new User("Jack", "jack");
 		assertTrue(user != null);
 	}
 
 	@Test
 	public void shouldBeCalledJack() {
 		User user = new User("Jack", "jack");
 		assertEquals(user.getName(), "Jack");
 	}
 
 	@Test
 	public void shouldCheckeMailValidation() {
 		User user = new User("John", "john");
 		assertTrue(user.checkEmail("john@gmx.com"));
 		assertTrue(user.checkEmail("john.smith@students.unibe.ch"));
 		assertFalse(user.checkEmail("john@gmx.c"));
 		assertFalse(user.checkEmail("john@info.museum"));
 		assertFalse(user.checkEmail("john@...com"));
 	}
 
 	@Test
 	public void checkMailAssertion() {
 		User user = new User("Bill", "bill");
 		user.setEmail("bill@aol.com");
 		assertEquals(user.getEmail(), "bill@aol.com");
 	}
 
 	@Test
 	public void checkPassw() {
 		User user = new User("Bill", "bill");
 		assertTrue(user.checkPW("bill"));
 		assertEquals(user.encrypt("bill"), user.getSHA1Password());
 		assertEquals(user.encrypt(""),
 				"da39a3ee5e6b4b0d3255bfef95601890afd80709"); // Source:
 		// wikipedia.org/wiki/Examples_of_SHA_digests
 		assertFalse(user.encrypt("password").equals(user.encrypt("Password")));
 	}
 
 	@Test
 	public void shouldEditProfileCorrectly() throws ParseException {
 		User user = new User("Jack", "jack");
 		user.setDateOfBirth("14.9.1987");
 		user.setBiography("I lived");
 		user.setEmail("test@test.tt");
 		user.setEmployer("TestInc");
 		user.setFullname("Test Tester");
 		user.setProfession("tester");
 		user.setWebsite("http://www.test.ch");
 
 		assertEquals(user.getAge(), 23);
 		assertTrue(user.getBiography().equalsIgnoreCase("I lived"));
 		assertTrue(user.getEmail().equals("test@test.tt"));
 		assertTrue(user.getEmployer().equals("TestInc"));
 		assertTrue(user.getFullname().equals("Test Tester"));
 		assertTrue(user.getProfession().equals("tester"));
 		assertTrue(user.getWebsite().equals("http://www.test.ch"));
 	}
 
 	@Test
 	public void checkForSpammer() {
 		User user = new User("Spammer", "spammer");
 		assertTrue(user.howManyItemsPerHour() == 0);
 		new Question(user, "Why did the chicken cross the road?");
 		assertTrue(user.howManyItemsPerHour() == 1);
 		new Question(user, "Does anybody know?");
 		assertFalse(user.howManyItemsPerHour() == 1);
 		for (int i = 0; i < 57; i++) {
 			new Question(user, "This is my " + i + ". question");
 		}
 		assertTrue(!user.isSpammer());
 		assertTrue(user.howManyItemsPerHour() == 59);
 		assertTrue(!user.isCheating());
 		new Question(user, "My last possible Post");
 		assertTrue(user.isSpammer());
 		assertTrue(user.isCheating());
 	}
 
 	@Test
 	public void checkForCheater() {
 		User user = new User("TheSupported", "supported");
 		User user2 = new User("Cheater", "cheater");
 		for (int i = 0; i < 4; i++) {
 			new Question(user, "This is my " + i + ". question").voteUp(user2);
 		}
 		assertTrue(user2.isMaybeCheater());
 		assertTrue(user2.isCheating());
 		assertTrue(!user.isMaybeCheater());
 		assertTrue(!user.isCheating());
 	}
 
 	@Test
 	public void shouldHaveOneQuestion() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		assertEquals(1, user.getQuestions().size());
 		q.unregister();
 	}
 
 	@Test
 	public void shouldHaveNoQuestion() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.unregister();
 		assertEquals(0, user.getQuestions().size());
 	}
 
 	@Test
 	public void shouldHaveOneAnswer() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.answer(user, "Because");
 		assertEquals(1, user.getAnswers().size());
 	}
 
 	@Test
 	public void shouldHaveNoAnswer() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.answer(user, "Because");
 		q.answers().get(0).unregister();
 		assertEquals(0, user.getAnswers().size());
 	}
 
 	@Test
 	public void shouldHaveOneBestAnswer() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.answer(user, "Because");
 		q.setBestAnswer(q.answers().get(0));
 		assertEquals(1, user.bestAnswers().size());
 	}
 
 	@Test
 	public void shouldHaveNoBestAnswer() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.answer(user, "Because");
 		q.setBestAnswer(q.answers().get(0));
 		q.answers().get(0).unregister();
 		assertEquals(0, user.bestAnswers().size());
 	}
 
 	@Test
 	public void shouldHaveOneHighRatedAnswer() {
 		User user = new User("Jack", "jack");
 		Question q = new Question(user, "Why?");
 		q.answer(user, "Because");
 
 		User a = new User("a", "a");
 		User b = new User("b", "b");
 		User c = new User("c", "c");
 		User d = new User("d", "d");
 		User e = new User("e", "e");
 
 		q.answers().get(0).voteUp(a);
 		q.answers().get(0).voteUp(b);
 		q.answers().get(0).voteUp(c);
 		q.answers().get(0).voteUp(d);
 		q.answers().get(0).voteUp(e);
 
 		assertEquals(1, user.highRatedAnswers().size());
 
 		a.delete();
 		b.delete();
 		c.delete();
 		d.delete();
 		e.delete();
 
 		assertEquals(0, user.highRatedAnswers().size());
 	}
 
 	@Test
 	public void shouldSuggestQuestion() {
 		User user3 = new User("User3", "user3");
 		User user4 = new User("User4", "user4");
 		User user5 = new User("User5", "user5");
 		Question m = new Question(user3, "Why?");
 		Question n = new Question(user4, "Where?");
 
 		m.setTagString("demo");
 		n.setTagString("demo demo2");
 		m.answer(user3, "Because");
 		m.answer(user4, "No idea");
 		n.answer(user5, "Therefore");
 
 		assertEquals(1, user5.getSuggestedQuestions().size());
 		assertEquals(m, user5.getSuggestedQuestions().get(0));
 
 	}
 
 	@Test
 	public void shouldSuggestThreeQuestions() {
 		User user3 = new User("User3", "user3");
 		User user4 = new User("User4", "user4");
 		User user5 = new User("User5", "user5");
 		Question m = new Question(user3, "Why?");
 		Question n = new Question(user4, "Where?");
 		Question o = new Question(user3, "Who?");
 		Question p = new Question(user4, "How old?");
 
 		m.setTagString("demo");
 		n.setTagString("demo demo2");
 		o.setTagString("demo demo3 demo4");
 		p.setTagString("demo demo3 demo4 demo5");
 		m.answer(user3, "Because");
 		m.answer(user4, "No idea");
 		n.answer(user5, "Therefore");
 
 		assertEquals(3, user5.getSuggestedQuestions().size());
 		assertEquals(m, user5.getSuggestedQuestions().get(0));
 	}
 
 	@Test
 	public void shouldSuggestQuestionsFromBestAnswersFirst() {
 		User user3 = new User("User3", "user3");
 		User user4 = new User("User4", "user4");
 		User user5 = new User("User5", "user5");
 		Question m = new Question(user3, "Why?");
 		Question n = new Question(user4, "Where?");
 		Question o = new Question(user3, "Who?");
 		Question p = new Question(user4, "How old?");
 
 		m.setTagString("demo");
 		n.setTagString("demo demo2");
 		o.setTagString("demo9 demo8");
 		p.setTagString("demo9 demo8");
 		m.answer(user3, "Because");
 		m.answer(user4, "No idea");
 		n.answer(user5, "Therefore");
 		o.answer(user5, "No");
 		o.setBestAnswer(user5.getAnswers().get(1));
 		assertEquals(2, user5.getSuggestedQuestions().size());
 		assertEquals(p, user5.getSuggestedQuestions().get(0));
 		assertEquals(m, user5.getSuggestedQuestions().get(1));
 
 	}
 
 	@Test
 	public void shouldSuggestQuestionsSortedByRatingOfAnswers() {
 		User user3 = new User("User3", "user3");
 		User user4 = new User("User4", "user4");
 		User user5 = new User("User5", "user5");
 		Question m = new Question(user3, "Why?");
 		Question n = new Question(user4, "Where?");
 		Question o = new Question(user3, "Who?");
 		Question p = new Question(user4, "How old?");
 
 		m.setTagString("demo");
 		n.setTagString("demo demo2");
 		o.setTagString("demo9 demo8");
 		p.setTagString("demo9 demo8");
 		m.answer(user3, "Because");
 		m.answer(user4, "No idea");
 		n.answer(user5, "Therefore");
 		o.answer(user5, "No");
 		user5.getAnswers().get(1).voteUp(user3);
 		user5.getAnswers().get(1).voteUp(user4);
 
		assertEquals(2, user5.getSuggestedQuestions().size());
 		assertEquals(p, user5.getSuggestedQuestions().get(0));
		assertEquals(m, user5.getSuggestedQuestions().get(1));
 
 	}
 
 	@Test
 	public void shouldNotSuggestQuestionsFromBadAnswers() {
 		User user6 = new User("User6", "user6");
 		User user7 = new User("User7", "user7");
 		User user8 = new User("User8", "user8");
 		System.out.println(user6.getName());
 		Question m = new Question(user6, "Why?");
 		Question n = new Question(user7, "Where?");
 		Question o = new Question(user6, "Who?");
 		Question p = new Question(user7, "How old?");
 		m.answer(user6, "Because");
 		m.answer(user7, "No idea");
 		p.answer(user8, "Therefore");
 		user8.getAnswers().get(0).voteDown(user6);
 
 		m.setTagString("demo");
 		n.setTagString("demo demo2");
 		o.setTagString("demo demo3 demo4");
 		p.setTagString("demo demo3 demo4 demo5");
 
 		assertEquals(0, user8.getSuggestedQuestions().size());
 	}
 
 	@Test
 	public void shouldNotSuggestOwnQuestions() {
 		User user = new User("Jack", "jack");
 		User user2 = new User("John", "john");
 		Question q = new Question(user, "Why?");
 		Question f = new Question(user2, "Where?");
 		q.setTagString("demo");
 		f.setTagString("demo");
 		q.answer(user2, "Because");
 		assertEquals(0, user2.getSuggestedQuestions().size());
 
 	}
 
 	@Test
 	public void shouldNotSuggestQuestionsWithBestAnswer() {
 		User james = new User("James", "james");
 		User john = new User("John", "john");
 		User kate = new User("Kate", "kate");
 		Question k = new Question(james, "Why?");
 		Question l = new Question(john, "Where?");
 
 		k.setTagString("demo");
 		l.setTagString("demo");
 		k.answer(james, "Because");
 		k.answer(john, "No idea");
 		james.getQuestions().get(0).setBestAnswer(k.getAnswer(1));
 		l.answer(kate, "Therefore");
 		assertEquals(0, kate.getSuggestedQuestions().size());
 
 	}
 
 }
