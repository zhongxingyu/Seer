 package net.frontlinesms.plugins.learn.ui.assessment;
 
 import java.util.List;
 
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.GroupDao;
 import net.frontlinesms.plugins.learn.data.domain.Assessment;
 import net.frontlinesms.plugins.learn.data.domain.AssessmentMessage;
 import net.frontlinesms.plugins.learn.data.domain.Topic;
 import net.frontlinesms.plugins.learn.data.domain.TopicItem;
 import net.frontlinesms.plugins.learn.data.repository.AssessmentDao;
 import net.frontlinesms.plugins.learn.data.repository.TopicItemDao;
 import net.frontlinesms.plugins.learn.ui.topic.TopicChoosingDialogHandlerTest;
 import net.frontlinesms.test.spring.MockBean;
 import net.frontlinesms.test.ui.ThinletComponent;
 
 import static org.mockito.Mockito.*;
 import static net.frontlinesms.plugins.learn.LearnTestUtils.*;
 
 public class EditAssessmentDialogHandlerTest extends TopicChoosingDialogHandlerTest<EditAssessmentDialogHandler> {
 	@MockBean private AssessmentDao dao;
 	@MockBean private GroupDao groupDao;
 	@MockBean private TopicItemDao topicItemDao;
 	private Assessment a;
 	private Group g;
 	private Topic t;
 	private List<AssessmentMessage> assessmentMessages;
 	private List<TopicItem> topicItems;
 	
 //> SETUP METHODS
 	@Override
 	protected void setUp() throws Exception {
 		a = new Assessment();
 		
 		t = mockTopic("the topic");
 		a.setTopic(t);
 
 		g = mockGroup("the group");
 		a.setGroup(g);
 		
 		TopicItem[] ti = mockTopicItems(5);
 		topicItems = asList(ti);
 
 		assessmentMessages = asList(
 				mockAssessmentWithTopicItem(ti[0]),
 				mockAssessmentWithTopicItem(ti[2]),
 				mockAssessmentWithTopicItem(ti[4]));
 		a.setMessages(assessmentMessages);
 		
 		super.setUp();
 		
 		when(topicItemDao.getAllByTopic(t)).thenReturn(topicItems);
 		initUiForTests();
 	}
 
 	@Override
 	protected void fillFieldsExceptTopic() {
 	}
 
 	@Override
 	protected EditAssessmentDialogHandler initHandler() {
 		return new EditAssessmentDialogHandler(ui, dao, groupDao, topicDao, topicItemDao, a);
 	}
 	
 //> TEST METHODS
 	@Override
 	public void testTitle() {
 		assertEquals("plugins.learn.assessment.edit", $().getText());
 	}
 
 	@Override
 	public void testSaveButton() {
 		// when
 		$("btSave").click();
 		
 		// then
 		verify(dao).save(a);
 		assertFalse($().isVisible());
 	}
 	
 	public void testTopicInitialisation() {
 		assertEquals("the group", $("tfGroup").getText());
 	}
 	
 	public void testGroupInitialisation() {
 		assertEquals("the topic", $("cbTopic").getText());
 	}
 	
 	public void testMessageTableInitialisation() {
 		ThinletComponent[] rows = $("tbMessages").getRows();
 		
 		assertEquals(topicItems.size(), rows.length);
 		
 		for(ThinletComponent c : rows) {
 			Object attachment = c.getAttachment();
 			if(attachment instanceof AssessmentMessage) {
 				TopicItem t = ((AssessmentMessage) attachment).getTopicItem();
 				if(!assessmentMessages.remove(attachment) ||
 						!topicItems.remove(t)) {
 					fail("Unexpected assessment message!");
 				}
 			} else if(attachment instanceof TopicItem) {
 				if(!topicItems.remove(attachment)) {
 					fail("Unexpected topic item!");
 				}
 			} else fail("Unexpected attachment: " + attachment);
 		}
 
 		assertEquals(0, topicItems.size());
 		assertEquals(0, assessmentMessages.size());
 	}
 }
