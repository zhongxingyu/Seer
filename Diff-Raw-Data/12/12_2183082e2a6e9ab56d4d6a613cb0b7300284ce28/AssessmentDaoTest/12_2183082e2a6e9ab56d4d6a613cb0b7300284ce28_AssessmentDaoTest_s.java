 package net.frontlinesms.plugins.learn.data.repository;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.GroupDao;
 import net.frontlinesms.junit.HibernateTestCase;
 import net.frontlinesms.plugins.learn.data.domain.Assessment;
 import net.frontlinesms.plugins.learn.data.domain.AssessmentMessage;
 import net.frontlinesms.plugins.learn.data.domain.Frequency;
 import net.frontlinesms.plugins.learn.data.domain.Reinforcement;
 import net.frontlinesms.plugins.learn.data.domain.Topic;
 import net.frontlinesms.plugins.learn.data.domain.TopicItem;
 
 import static java.util.Arrays.asList;
 import static net.frontlinesms.plugins.learn.LearnTestUtils.*;
 
 public class AssessmentDaoTest extends HibernateTestCase {
 	/** dao under test */
 	@Autowired AssessmentDao dao;
 	@Autowired GroupDao groupDao;
 	@Autowired ReinforcementDao reinforcementDao;
 	@Autowired TopicDao topicDao;
 
 //> TEST METHODS
 	public void testSave() {
 		// given
 		Assessment a = new Assessment();
 		assertEquals(0, dao.count());
 		
 		// when
 		dao.save(a);
 		
 		// then
 		assertEquals(1, dao.count());
 	}
 	
 	public void testSaveWithNewMessages() throws Exception {
 		// given
 		Assessment a = new Assessment();
 		Topic t = createTopics("test")[0];
 		a.setMessages(asList(
 				newAssessmentMessage(createReinforcement(t)),
 				newAssessmentMessage(createReinforcement(t))));
 		a.setTopic(t);
 		a.setGroup(createGroup("test-group"));
 		// when
 		dao.save(a);
 		
 		// then
 		assertEquals(1, dao.count());
 		assertEquals(2, dao.list().get(0).getMessages().size());
 	}
 	
 	public void testDelete() {
 		// given
 		Assessment a = new Assessment();
 		assertEquals(0, dao.count());
 		dao.save(a);
 		assertEquals(1, dao.count());
 		
 		// when
 		dao.delete(a);
 		
 		// then
 		assertEquals(0, dao.count());
 	}
 	
 	public void testDeleteWithMessages() throws Exception {
 		// given
 		Assessment a = new Assessment();
 		Topic t = createTopics("test")[0];
 		a.setMessages(asList(
 				newAssessmentMessage(createReinforcement(t)),
 				newAssessmentMessage(createReinforcement(t))));
 		a.setTopic(t);
 		a.setGroup(createGroup("test-group"));
 		dao.save(a);
 		assertEquals(1, dao.count());
 		
 		// when
 		dao.delete(a);
 		
 		// then
 		assertEquals(0, dao.count());
 	}
 	
 	public void testFindAllByTopic_none() throws Exception {
 		// given
 		Topic t = createTopics("Random")[0];
 		
 		// when
 		List<Assessment> a = dao.findAllByTopic(t);
 		
 		// then
 		assertEquals(0, a.size());
 	}
 	
 	public void testFindAllByTopic() throws Exception {
 		// given
 		Topic[] topics = createTopics("Zero", "One", "Two");
 		Assessment a1 = createAssessment(topics[1]);
 		Assessment a1a = createAssessment(topics[1]);
 		Assessment a2 = createAssessment(topics[2]);
 		
 		{
 			// when
 			List<Assessment> a = dao.findAllByTopic(topics[0]);
 			// then
 			assertEquals(0, a.size());
 		}
 		
 		{
 			// when
 			List<Assessment> a = dao.findAllByTopic(topics[1]);
 			// then
 			assertEquals(2, a.size());
 			assertEquals(asList(a1, a1a), a);
 		}
 		
 		{
 			// when
 			List<Assessment> a = dao.findAllByTopic(topics[2]);
 			// then
 			assertEquals(1, a.size());
 			assertEquals(asList(a2), a);
 		}
 	}
 	
 	public void testFindAllByGroup_none() throws Exception {
 		// given
 		Group g = createGroup("TestGroup");
 		
 		// when
 		List<Assessment> a = dao.findAllByGroup(g);
 		
 		// then
 		assertEquals(0, a.size());
 	}
 	
 	public void testFindAllByGroup() throws Exception {
 		// given
 		Group[] groups = createGroups("Zero", "One", "Two");
 		Assessment a1 = createAssessment(groups[1]);
 		Assessment a1a = createAssessment(groups[1]);
 		Assessment a2 = createAssessment(groups[2]);
 		
 		{
 			// when
 			List<Assessment> a = dao.findAllByGroup(groups[0]);
 			// then
 			assertEquals(0, a.size());
 		}
 		
 		{
 			// when
 			List<Assessment> a = dao.findAllByGroup(groups[1]);
 			// then
 			assertEquals(2, a.size());
 			assertEquals(asList(a1, a1a), a);
 		}
 		
 		{
 			// when
 			List<Assessment> a = dao.findAllByGroup(groups[2]);
 			// then
 			assertEquals(1, a.size());
 			assertEquals(asList(a2), a);
 		}
 	}
 	
 	public void testMessagesEagerLoading() {
 		// given
 		AssessmentMessage m = new AssessmentMessage();
 		m.setStartDate(YESTERDAY);
 		m.setEndDate(TOMORROW);
 		m.setFrequency(Frequency.DAILY);
 		Assessment a = new Assessment();
 		a.setMessages(asList(m));
 		dao.save(a);
 		setComplete();
 		endTransaction();
 		
 		// when
 		a = dao.list().get(0);
 		
 		// then
 		assertEquals(YESTERDAY, a.getStartDate());
 		assertEquals(TOMORROW, a.getEndDate());
 	}
	
 //> SETUP HELPER METHODS
 	private Topic[] createTopics(String... names) throws Exception {
 		Topic[] topics = new Topic[names.length];
 		for (int i = 0; i < topics.length; i++) {
 			topics[i] = new Topic();
 			topics[i].setName(names[i]);
 			topicDao.save(topics[i]);
 		}
 		return topics;
 	}
 	
 	private Assessment createAssessment(Topic t) {
 		Assessment a = new Assessment();
 		a.setTopic(t);
 		dao.save(a);
 		return a;
 	}
 	
 	private Assessment createAssessment(Group g) {
 		Assessment a = new Assessment();
 		a.setGroup(g);
 		dao.save(a);
 		return a;
 	}
 	
 	private Reinforcement createReinforcement(Topic t) {
 		Reinforcement i = new Reinforcement();
 		i.setTopic(t);
 		reinforcementDao.save(i);
 		return i;
 	}
 	
 	private Group createGroup(String name) throws Exception {
 		Group g = new Group(new Group(null, null), name);
 		groupDao.saveGroup(g);
 		return g;
 	}
 	
 	private Group[] createGroups(String... names) throws Exception {
 		Group[] groups = new Group[names.length];
 		for (int i = 0; i < groups.length; i++) {
 			groups[i] = createGroup(names[i]);
 		}
 		return groups;
 	}
 	
 	private AssessmentMessage newAssessmentMessage(TopicItem t) {
 		AssessmentMessage m = new AssessmentMessage(t);
 		return m;
 	}
 }
