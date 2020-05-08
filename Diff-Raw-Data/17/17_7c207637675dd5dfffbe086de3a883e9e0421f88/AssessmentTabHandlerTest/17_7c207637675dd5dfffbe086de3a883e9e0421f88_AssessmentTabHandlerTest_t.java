 package net.frontlinesms.plugins.learn.ui.assessment;
 
 import java.util.List;
 
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.GroupDao;
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.plugins.learn.data.domain.Assessment;
 import net.frontlinesms.plugins.learn.data.domain.Topic;
 import net.frontlinesms.plugins.learn.data.repository.*;
 import net.frontlinesms.test.spring.MockBean;
 import net.frontlinesms.test.ui.ThinletEventHandlerTest;
 
 import static org.mockito.Mockito.*;
 
 import static net.frontlinesms.plugins.learn.LearnTestUtils.*;
 
 public class AssessmentTabHandlerTest extends ThinletEventHandlerTest<AssessmentTabHandler> {
 	@SuppressWarnings("unused")
 	@MockBean private EventBus eventBus;
 	@SuppressWarnings("unused")
 	@MockBean private GroupDao groupDao;
 	@MockBean private TopicDao topicDao;
 	@MockBean private AssessmentDao assessmentDao;
 
 //> SETUP METHODS
 	@Override
 	protected AssessmentTabHandler initHandler() {
 		mockTopics(topicDao, "Cookery", "Music", "Philately");
 		
 		return new AssessmentTabHandler(ui, ctx);
 	}
 	
 	@Override
 	protected Object getRootComponent() {
 		return h.getTab();
 	}
 	
 //> TEST METHODS
 	public void testNewAssessmentButton() {		
 		// when
 		$("btNewAssessment").click();
 		
 		// then
 		assertEquals("plugins.learn.assessment.new", $("dgEditAssessment").getText());
 	}
 	
 	public void testListByClassTableHeaders() {
 		// given
 		$("cbViewBy_class").select();
 		
 		// then
 		assertEquals("Column titles",
 				array("plugins.learn.topic",
 						"plugins.learn.assessment.start",
 						"plugins.learn.assessment.end"),
 				$("tblAssessments").getColumnTitles());
 	}
 	
 	public void testListByClassTableContents() {
 		// given
 		List<Assessment> assessments = asList(
 				mockAssessmentWithTopic("Topicana", "20/12/11", "13/12/12"),
 				mockAssessmentWithTopic("Umbongo", "3/4/12", "14/4/12"));
 		when(assessmentDao.findAllByGroup(any(Group.class))).thenReturn(assessments);
 		
 		// when
 		$("cbViewBy_class").select();
 		h.groupSelectionCompleted(mock(Group.class));
 		
 		// then
 		assertEquals("First row contents.",
 				new String[]{ "Topicana", "20/12/2011", "13/12/2012" },
 				$("tblAssessments").getRowText(0));
 		assertEquals("Second row contents.",
 				new String[]{ "Umbongo", "3/4/2012", "14/4/2012" },
 				$("tblAssessments").getRowText(1));
 	}
 	
 	public void testTopicChoiceVisibleByDefault() {
 		assertTrue($("cbTopic").isVisible());
 	}
 	
 	public void testTopicChoiceHiddenWhenViewingByClass() {
 		// when
 		$("cbViewBy_class").select();
 		// then
 		assertFalse($("cbTopic").isVisible());
 	}
 	
 	public void testTopicChoiceShowAgainWhenViewingByTopicAfterViewingByClass() {
 		// given
 		$("cbViewBy_class").select();
 		assertFalse($("cbTopic").isVisible());
 		
 		// when
 		$("cbViewBy_topic").select();
 		
 		// then
 		assertTrue($("cbTopic").isVisible());
 	}
 	
 	public void testClassSelecterInitialisedEmptyAndNotEditable() {
 		assertEquals("", $("tfClass").getText());
 		assertFalse($("tfClass").isEditable());
 	}
 	
 	public void testGroupSelecterControlsHiddenWhenViewingByTopic() {
 		// given we're viewing by topic by default
 		// then
 		assertFalse($("pnClass").isVisible());
 	}
 	
 	public void testGroupSelecterControlsShownWhenViewingByClass() {
 		// when
 		$("cbViewBy_class").select();
 		// then
 		assertTrue($("pnClass").isVisible());
 	}
 	
 	public void testClassSelecterTriggerButton() {
 		// given
 		$("cbViewBy_class").select();
 		// when
 		$("btSelectClass").click();
 		// then
 		$("dgGroupSelecter").isVisible();
 	}
 	
 	public void testListByTopicTableHeaders() {
 		// given we are already viewing by topic
 		
 		// then
 		assertEquals("Column titles",
 				array("plugins.learn.assessment.class",
 						"plugins.learn.assessment.start",
 						"plugins.learn.assessment.end"),
 				$("tblAssessments").getColumnTitles());
 	}
 	
 	public void testListByTopicTableContents() {
 		// given we are already viewing by topic
 		List<Assessment> assessments = asList(
 				mockAssessmentWithGroup("Space mutants", "20/12/11", "13/12/12"),
 				mockAssessmentWithGroup("Biker mice", "3/4/12", "14/4/12"));
 		when(assessmentDao.findAllByTopic(any(Topic.class))).thenReturn(assessments);
 		
 		// when
 		$("cbTopic").setSelected("Music");
 		
 		// then
 		assertEquals("First row contents.",
 				new String[]{ "Space mutants", "20/12/2011", "13/12/2012" },
 				$("tblAssessments").getRowText(0));
 		assertEquals("Second row contents.",
 				new String[]{ "Biker mice", "3/4/2012", "14/4/2012" },
 				$("tblAssessments").getRowText(1));
 	}
 	
 	public void testListByTopicUnscheduledAssessment() {
 		// given we are already viewing by topic
 		List<Assessment> assessments = asList(mockAssessmentWithGroup("unscheduled", null, null));
 		when(assessmentDao.findAllByTopic(any(Topic.class))).thenReturn(assessments);
 		
 		// when
 		$("cbTopic").setSelected("Music");
 		
 		// then
 		assertEquals("First row contents.",
 				new String[]{ "unscheduled", "?", "?" },
 				$("tblAssessments").getRowText(0));
 	}
 	
 	public void testListShouldEmptyWhenChangingToByTopic() {
 		// given
 		$("cbViewBy_class").select();
 		List<Assessment> assessments = asList(
 				mockAssessmentWithTopic("Current affairs", "20/12/11", "13/12/12"),
 				mockAssessmentWithTopic("Currency affairs", "3/4/12", "14/4/12"));
 		when(assessmentDao.findAllByGroup(any(Group.class))).thenReturn(assessments);
 		h.groupSelectionCompleted(mockGroup("random-group"));
 		assertEquals(2, $("tblAssessments").getRowCount());
 
 		// when
 		$("cbViewBy_topic").select();
 		
 		// then
 		assertEquals(0, $("tblAssessments").getRowCount());
 	}
 	
 	public void testListShouldEmptyWhenChangingToByClass() {
 		// given
 		populateAssessmentListForTopic();
 
 		// when
 		$("cbViewBy_class").select();
 		
 		// then
 		assertEquals(0, $("tblAssessments").getRowCount());
 	}
 	
 	public void testDoubleClickingOnAnAssessmentShouldOpenEditWindow() {
 		// given
 		populateAssessmentListForTopic();
 
 		// when
 		$("tblAssessments").getRow(0).doubleClick();
 
 		// then
 		assertTrue($("dgEditAssessment").isVisible());
 	}
 
 	public void testEditAssessmentButtonShouldBeDisabledWhenNoAssessmentIsSelected() {
 		assertFalse($("btEditAssessment").isEnabled());
 	}
 	
 	public void testEditAssessmentButtonShouldBeDisabledAfterAssessmentListIsCleared() {
 		// given
 		populateAssessmentListForTopic();
 
 		// when
 		$("tblAssessments").getRow(0).select();
 		// then
 		assertTrue($("btEditAssessment").isEnabled());
 		
 		// when
 		$("cbViewBy_class").select();
 		
 		// then
 		assertEquals(0, $("tblAssessments").getRowCount());
 		assertFalse($("btEditAssessment").isEnabled());
 	}
 	
 	public void testDeleteAssessmentButtonVisible() {
 		$("btDeleteAssessment").exists();
 	}
 	
 	public void testDeleteAssessmentButtonDisabledByDefault() {
 		assertFalse($("btDeleteAssessment").isEnabled());
 	}
 	
 	public void testDeleteAssessmentButtonEnabledWhenAssessmentSelected() {
 		// given
 		populateAssessmentListForTopic();
 
 		// when
 		$("tblAssessments").getRow(0).select();
 		
 		// then
 		assertTrue($("btDeleteAssessment").isEnabled());
 	}
 	
 	public void testDeleteAssessmentButtonShouldBeDisabledAfterAssessmentListIsCleared() {
 		// given
 		populateAssessmentListForTopic();
 
 		// when
 		$("tblAssessments").getRow(0).select();
 		// then
 		assertTrue($("btDeleteAssessment").isEnabled());
 		
 		// when
 		$("cbViewBy_class").select();
 		
 		// then
 		assertEquals(0, $("tblAssessments").getRowCount());
 		assertFalse($("btDeleteAssessment").isEnabled());
 	}
 	
 	public void testDeleteAssessmentButtonShowsConfirmationDialog() {
 		// given
 		populateAssessmentListForTopic();
 
 		// when
 		$("tblAssessments").getRow(0).select();
 		$("btDeleteAssessment").click();
 		
 		// then
 		$("confirmDialog").exists();
 		verify(assessmentDao, never()).delete(any(Assessment.class));
 	}
 	
 	public void testConfirmingDeleteAssessmentCallsDaoDeleteForCorrectAssessment() {
 		// given
 		Assessment a = populateAssessmentListForTopic().get(0);
 
 		// when
 		$("tblAssessments").getRow(0).select();
 		$("btDeleteAssessment").click();
 		$("confirmDialog").find("btContinue").click();
 		
 		// then
 		verify(assessmentDao).delete(a);
 	}
 	
 	public void testDeletedAssessmentShouldBeRemovedFromView() {
 		// given
 		populateAssessmentListForTopic().get(0);
 		assertEquals(2, $("tblAssessments").getRowCount());
 
 		// when
 		$("tblAssessments").getRow(0).select();
 		$("btDeleteAssessment").click();
 		$("confirmDialog").find("btContinue").click();
 		
 		// then
 		assertEquals(1, $("tblAssessments").getRowCount());
 	}
 	
 	public void testEditAssesmentButtonShouldOpenEditWindow() {
 		// given
 		populateAssessmentListForTopic();
 
 		// when
 		$("tblAssessments").getRow(0).select();
 		$("btEditAssessment").click();
 		
 		// then
 		assertTrue($("dgEditAssessment").isVisible());
 	}
 	
 	public void testCreationOfNewTopicShouldUpdateListOfTopics() {
 		// given
 		assertEquals("Displayed topics",
 				array("Cookery", "Music", "Philately"),
 				$("cbTopic").getOptions());
 		mockTopics(topicDao, "Cookery", "Music", "Philately", "Tomfoolery");
 		
 		// when
 		h.notify(mockEntitySavedNotification(Topic.class));
		waitForUiEvents();
 		
 		// then
 		assertEquals("Displayed topics",
 				array("Cookery", "Music", "Philately", "Tomfoolery"),
 				$("cbTopic").getOptions());
 	}
 	
 	public void testUpdateOfAssessmentShouldDisableEditAndDeleteButtons() {
 		// given
 		List<Assessment> assessments = asList(
 				mockAssessmentWithTopicAndId(99, "Topicana", "20/12/11", "13/12/12"));
 		when(assessmentDao.findAllByGroup(any(Group.class))).thenReturn(assessments);
 		$("cbViewBy_class").select();
 		h.groupSelectionCompleted(mock(Group.class));
 		assertEquals("Assessments displayed", 1, $("tblAssessments").getRowCount());
 		assertEquals("Initial first row contents.",
 				new String[]{ "Topicana", "20/12/2011", "13/12/2012" },
 				$("tblAssessments").getRowText(0));
 		$("tblAssessments").getRow(0).select();
 		assertTrue($("btEditAssessment").isEnabled());
 		assertTrue($("btDeleteAssessment").isEnabled());
 		
 		// when
 		h.notify(mockEntityUpdatedNotification(
 				mockAssessmentWithTopicAndId(99, "Umbongo", "3/4/12", "14/4/12")));
 		waitForUiEvents();
 		
 		// then
 		assertFalse($("btEditAssessment").isEnabled());
 		assertFalse($("btDeleteAssessment").isEnabled());
 	}
 	
 	public void testUpdateOfAssessmentShouldUpdateAssessmentList() {
 		// given
 		List<Assessment> assessments = asList(
 				mockAssessmentWithTopicAndId(99, "Topicana", "20/12/11", "13/12/12"));
 		when(assessmentDao.findAllByGroup(any(Group.class))).thenReturn(assessments);
 		$("cbViewBy_class").select();
 		h.groupSelectionCompleted(mock(Group.class));
 		assertEquals("Assessments displayed", 1, $("tblAssessments").getRowCount());
 		assertEquals("Initial first row contents.",
 				new String[]{ "Topicana", "20/12/2011", "13/12/2012" },
 				$("tblAssessments").getRowText(0));
 		
 		// when
 		h.notify(mockEntityUpdatedNotification(
 				mockAssessmentWithTopicAndId(99, "Umbongo", "3/4/12", "14/4/12")));
		waitForUiEvents();
 		
 		// then
 		assertEquals("Assessments displayed", 1, $("tblAssessments").getRowCount());
 		assertEquals("Final first row contents.",
 				new String[]{ "Umbongo", "3/4/2012", "14/4/2012" },
 				$("tblAssessments").getRowText(0));
 	}
 	
 	public void testSaveOfAssessmentShouldUpdateAssessmentList() {
 		// given
 		List<Assessment> assessments = asList(
 				mockAssessmentWithTopicAndId(99, "Topicana", "20/12/11", "13/12/12"));
 		when(assessmentDao.findAllByGroup(any(Group.class))).thenReturn(assessments);
 		$("cbViewBy_class").select();
 		h.groupSelectionCompleted(mock(Group.class));
 		assertEquals("Assessments displayed", 1, $("tblAssessments").getRowCount());
 		assertEquals("Initial first row contents.",
 				new String[]{ "Topicana", "20/12/2011", "13/12/2012" },
 				$("tblAssessments").getRowText(0));
 		
 		// when
 		h.notify(mockEntitySavedNotification(
 				mockAssessmentWithTopicAndId(100, "Umbongo", "3/4/12", "14/4/12")));
		waitForUiEvents();
 		
 		// then
 		assertEquals("Assessments displayed", 2, $("tblAssessments").getRowCount());
 		assertEquals("Final first row contents.",
 				new String[]{ "Topicana", "20/12/2011", "13/12/2012" },
 				$("tblAssessments").getRowText(0));
 		assertEquals("Final second row contents.",
 				new String[]{ "Umbongo", "3/4/2012", "14/4/2012" },
 				$("tblAssessments").getRowText(1));
 	}
 	
 	public void testDeleteOfAssessmentShouldDisableEditAndDeleteButtons() {
 		// given
 		List<Assessment> assessments = asList(
 				mockAssessmentWithTopicAndId(99, "Topicana", "20/12/11", "13/12/12"),
 				mockAssessmentWithTopicAndId(100, "Umbongo", "3/4/12", "14/4/12"));
 		when(assessmentDao.findAllByGroup(any(Group.class))).thenReturn(assessments);
 		$("cbViewBy_class").select();
 		h.groupSelectionCompleted(mock(Group.class));
 		$("tblAssessments").getRow(1).select();
 		assertTrue($("btEditAssessment").isEnabled());
 		assertTrue($("btDeleteAssessment").isEnabled());
 		
 		// when
 		h.notify(mockEntityDeletedNotification(
 				mockAssessmentWithId(100)));
		waitForUiEvents();
 		
 		// then
 		assertFalse($("btEditAssessment").isEnabled());
 		assertFalse($("btDeleteAssessment").isEnabled());
 	}
 	
 	public void testDeleteOfAssessmentShouldUpdateAssessmentList() {
 		// given
 		List<Assessment> assessments = asList(
 				mockAssessmentWithTopicAndId(99, "Topicana", "20/12/11", "13/12/12"),
 				mockAssessmentWithTopicAndId(100, "Umbongo", "3/4/12", "14/4/12"));
 		when(assessmentDao.findAllByGroup(any(Group.class))).thenReturn(assessments);
 		$("cbViewBy_class").select();
 		h.groupSelectionCompleted(mock(Group.class));
 		assertEquals("Assessments displayed", 2, $("tblAssessments").getRowCount());
 		assertEquals("Initial first row contents.",
 				new String[]{ "Topicana", "20/12/2011", "13/12/2012" },
 				$("tblAssessments").getRowText(0));
 		assertEquals("Initial second row contents.",
 				new String[]{ "Umbongo", "3/4/2012", "14/4/2012" },
 				$("tblAssessments").getRowText(1));
 		
 		// when
 		h.notify(mockEntityDeletedNotification(
 				mockAssessmentWithId(100)));
		waitForUiEvents();
 		
 		// then
 		assertEquals("Assessments displayed", 1, $("tblAssessments").getRowCount());
 		assertEquals("Final first row contents.",
 				new String[]{ "Topicana", "20/12/2011", "13/12/2012" },
 				$("tblAssessments").getRowText(0));
 	}
 	
 //> TEST HELPER METHODS
 	private List<Assessment> populateAssessmentListForTopic() {
 		List<Assessment> assessments = asList(
 				mockAssessmentWithGroup(1, "Space mutants", "20/12/11", "13/12/12"),
 				mockAssessmentWithGroup(2, "Biker mice", "3/4/12", "14/4/12"));
 		when(assessmentDao.findAllByTopic(any(Topic.class))).thenReturn(assessments);
 		$("cbTopic").setSelected("Music");
 		assertEquals(2, $("tblAssessments").getRowCount());
 		return assessments;
 	}
 }
