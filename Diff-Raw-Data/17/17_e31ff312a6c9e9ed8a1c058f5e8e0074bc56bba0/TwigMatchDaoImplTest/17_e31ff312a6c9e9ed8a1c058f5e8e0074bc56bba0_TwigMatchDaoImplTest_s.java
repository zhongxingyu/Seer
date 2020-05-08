 package org.seamoo.daos.twigImpl.matching;
 
 import org.seamoo.daos.matching.MatchDao;
 import org.seamoo.entities.Member;
 import org.seamoo.entities.matching.Match;
 import org.seamoo.entities.matching.MatchAnswer;
 import org.seamoo.entities.matching.MatchAnswerType;
 import org.seamoo.entities.matching.MatchCompetitor;
 import org.seamoo.entities.matching.MatchEvent;
 import org.seamoo.entities.matching.MatchEventType;
 import org.seamoo.entities.question.Question;
 import org.seamoo.persistence.test.LocalDatastoreTest;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.google.inject.internal.Lists;
 
 public class TwigMatchDaoImplTest extends LocalDatastoreTest {
 	MatchDao daoImpl;
 
 	public TwigMatchDaoImplTest() {
 	}
 
 	@BeforeMethod
 	@Override
 	public void setUp() {
 		// TODO Auto-generated method stub
 		super.setUp();
 		daoImpl = new TwigMatchDaoImpl();
 	}
 
 	@AfterMethod
 	@Override
 	public void tearDown() {
 		// TODO Auto-generated method stub
 		super.tearDown();
 	}
 
 	@Test
 	public void persistMatchWithQuestionAndEventAndCompetitorsAndAnswersShouldBeOK() {
 		Match m = new Match();
 		m.setQuestions(Lists.newArrayList(new Question[] { new Question() }));
 		m.addEvent(new MatchEvent(MatchEventType.ANSWER_QUESTION, new Member(), 1));
 		MatchCompetitor competitor = new MatchCompetitor();
 		competitor.setMember(new Member());
 		competitor.addAnswer(new MatchAnswer(MatchAnswerType.SUBMITTED, "1"));
 		competitor.addAnswer(new MatchAnswer(MatchAnswerType.IGNORED, "1"));
 		m.addCompetitor(competitor);
 		daoImpl.persist(m);
 	}
 }
