 package net.frontlinesms.plugins.learn.ui.gradebook;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.*;
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.plugins.learn.data.domain.*;
 import net.frontlinesms.plugins.learn.data.gradebook.GradebookService;
 import net.frontlinesms.plugins.learn.data.repository.*;
 import net.frontlinesms.test.spring.MockBean;
 import net.frontlinesms.test.ui.ThinletEventHandlerTest;
 import net.frontlinesms.ui.UiDestroyEvent;
 
 import static net.frontlinesms.plugins.learn.LearnTestUtils.*;
 
 import static org.mockito.Mockito.*;
 
 public class GradebookTabHandlerTest extends ThinletEventHandlerTest<GradebookTabHandler> {
 	@MockBean private GradebookService gradebookService;
 	@MockBean private GroupMembershipDao groupMembershipDao;
 	@SuppressWarnings("unused")
 	@MockBean private GroupDao groupDao;
 	@MockBean private TopicDao topicDao;
 	@MockBean private AssessmentDao assessmentDao;
 	@MockBean private EventBus eventBus;
 
 //> SETUP METHODS
 	@Override
 	protected GradebookTabHandler initHandler() {
 		return new GradebookTabHandler(ui, ctx);
 	}
 
 	@Override
 	protected Object getRootComponent() {
 		return h.getTab();
 	}
 
 //> TEST METHODS
 	public void testClassGroupSelecterInitialisedEmpty() {
 		assertEquals("", $("tfClass").getText());
 	}
 	
 	public void testClassGroupTextfieldNotEditable() {
 		assertFalse($("tfClass").isEditable());
 	}
 	
 	public void testAssessmentSelecterInitialisedBlank() {
 		assertEquals("", $("cbAssessment").getText());
 	}
 	
 	public void testAssessmentSelecterIsNotEditable() {
 		assertFalse($("cbAssessment").isEditable());
 	}
 	
 	public void testAssessmentSelecterIsDisabled() {
 		assertFalse($("cbAssessment").isEnabled());
 	}
 	
 	public void testSelectingAClassAndTopicEnablesAssessmentComboboxIfAssessmentsAvailable() {
 		// given
 		Topic t = mockTopics(topicDao, "topic1")[0];
 		initUiForTests();
 		Group g = mockGroup("test-group");
 		mockAssessments(assessmentDao, g, t, 1);
 		
 		// when
 		h.groupSelectionCompleted(g);
 		$("cbTopic").setSelected("topic1");
 		
 		// then
 		assertTrue($("cbAssessment").isEnabled());
 	}
 	
 	public void testSelectingAClassAndTopicDoesNotEnableAssessmentComboboxIfNoAssessmentsAvailable() {
 		// given
 		mockTopics(topicDao, "topic1");
 		initUiForTests();
 		Group g = mockGroup("test-group");
 		
 		// when
 		h.groupSelectionCompleted(g);
 		$("cbTopic").setSelected("topic1");
 		
 		// then
 		assertFalse($("cbAssessment").isEnabled());
 	}
 	
 	public void testSelectingAClassAndTopicSetsAssesmentComboboxTextIfNoAssessmentsAvailable() {
 		// given
 		mockTopics(topicDao, "topic1");
 		initUiForTests();
 		Group g = mockGroup("test-group");
 		
 		// when
 		h.groupSelectionCompleted(g);
 		$("cbTopic").setSelected("topic1");
 		
 		// then
 		assertEquals("plugins.learn.assessment.none", $("cbAssessment").getText());
 	}
 	
 	public void testAssessmentSelecterIsPopulatedWithRelevantAssessmentsWhenGroupAndTopicChosen() {
 		// given
 		Topic t = mockTopics(topicDao, "topic1")[0];
 		initUiForTests();
 		Group g = mockGroup("test-group");
 		mockAssessments(assessmentDao, g, t, 2);
 		
 		// when
 		h.groupSelectionCompleted(g);
 		$("cbTopic").setSelected("topic1");
 		
 		// then
 		assertEquals("assessment list",
 				array("1/1/2000 - 31/12/2000", "1/1/2001 - 31/12/2001"),
 				$("cbAssessment").getOptions());
 	}
 	
 	public void testAssessmentSelecterTextIsSetWhenGroupAndTopicSelected() {
 		// given
 		Topic t = mockTopics(topicDao, "topic1")[0];
 		initUiForTests();
 		Group g = mockGroup("test-group");
 		mockAssessments(assessmentDao, g, t, 2);
 		
 		// when
 		h.groupSelectionCompleted(g);
 		$("cbTopic").setSelected("topic1");
 		
 		// then
 		assertEquals("1/1/2000 - 31/12/2000", $("cbAssessment").getText());
 	}
 	
 	public void testTopicSelecterInitialisedToAllTopics() {
 		assertEquals("plugins.learn.topic.all", $("cbTopic").getText());
 	}
 	
 	public void testTopicSelecterDisabledByDefault() {
 		assertFalse($("cbTopic").isEnabled());
 	}
 	
 	public void testTopicSelecterNotEditable() {
 		assertFalse($("cbTopic").isEditable());
 	}
 	
 	public void testTopicSelecterOptionsCorrespondToTopics() {
 		// given
 		mockTopics(topicDao, "topic1", "topic2", "topic3");
 		
 		// when
 		initUiForTests();
 		
 		// then
 		assertEquals("topic selecter options",
 				array("plugins.learn.topic.all", "topic1", "topic2", "topic3"),
 				$("cbTopic").getOptions());
 	}
 	
 	public void testSettingGroupEnablesTopicSelecter() {
 		// when
 		Group mockGroup = mockEmptyGradebookAndClass();
 		h.groupSelectionCompleted(mockGroup);
 		
 		// then
 		assertTrue($("cbTopic").isEnabled());
 	}
 	
 	public void testChangingToAnotherGroupResetsTopicSelecter() {
 		// given
 		mockTopics(topicDao, "topic1", "topic2");
 		initUiForTests();
 		
 		Group[] groups = mockEmptyGradebooksAndClasses(2);
 		h.groupSelectionCompleted(groups[0]);
 		
 		$("cbTopic").setSelected("topic1");
 		
 		// when
 		h.groupSelectionCompleted(groups[1]);
 		
 		// then
 		assertEquals("plugins.learn.topic.all", $("cbTopic").getText());
 	}
 	
 	public void testSelectingATopicUpdatesTableColumnTitles() {
 		// given
 		Group misfits = mockTopicAndClassAndAssessments("topic1", "misfits", "Alfred", "Bernadette");
 		initUiForTests();
 		h.groupSelectionCompleted(misfits);
 		
 		// when
 		$("cbTopic").setSelected("topic1");
 		
 		// then
 		assertEquals("Grade table headers",
 				array("plugins.learn.student", "Q1", "Q2", "Q3", "plugins.learn.gradebook.score"),
 				$("tbGrades").getColumnTitles());
 	}
 	
 	public void testSelectingATopicAndThenSelectingAllTopicsUpdatesTableColumnTitles() {
 		// given
 		Group misfits = mockTopicAndClassAndAssessments("topic1", "misfits", "Alfred", "Bernadette");
 		initUiForTests();
 		h.groupSelectionCompleted(misfits);
 		$("cbTopic").setSelected("topic1");
 		
 		// when
 		$("cbTopic").setSelected("plugins.learn.topic.all");
 		
 		// then
 		assertEquals("Grade table headers",
 				array("plugins.learn.student", "topic1", "plugins.learn.gradebook.average"),
 				$("tbGrades").getColumnTitles());
 	}
 	
 	public void testReturningToAllTopicViewShouldResetAssessmentsList() {
 		// given
 		Group misfits = mockTopicAndClassAndAssessments("topic1", "misfits", "Alfred", "Bernadette");
 		initUiForTests();
 		h.groupSelectionCompleted(misfits);
 		$("cbTopic").setSelected("topic1");
 		
 		// when
 		$("cbTopic").setSelected("plugins.learn.topic.all");
 		
 		// then
 		assertFalse($("cbAssessment").isEnabled());
 		assertEquals("", $("cbAssessment").getText());
 	}
 	
 	public void testSelectingATopicAddsAverageRowAtBottomOfTable() {
 		// given
 		Group misfits = mockTopicAndClassAndAssessments("topic1", "misfits", "Alfred", "Bernadette");
 		initUiForTests();
 		h.groupSelectionCompleted(misfits);
 		
 		// when
 		$("cbTopic").setSelected("topic1");
 		
 		// then
 		assertEquals("Grade table averages",
 				array("plugins.learn.gradebook.average", "12%", "36%", "0%", "16%"),
 				$("tbGrades").getRowText(2));
 	}
 	
 	public void testSelectingATopicWithAnAssessmentUpdatesTableContentsWithStudentsAnswers() {
 		// given
 		Group misfits = mockTopicAndClassAndAssessments("topic1", "misfits", "Alfred", "Bernadette");
 		initUiForTests();
 		h.groupSelectionCompleted(misfits);
 		
 		// when
 		$("cbTopic").setSelected("topic1");
 		
 		// then
 		assertEquals("Alfred's results",
 				array("Alfred", "", "A", "B", "2%"),
 				$("tbGrades").getRowText(0));
 		assertEquals("Bernadette's results",
 				array("Bernadette", "A", "B", "C", "6%"),
 				$("tbGrades").getRowText(1));
 	}
 	
 	public void testChangingAssessmentSelectionChangesResults() {
 		// given
 		Group misfits = mockTopicAndClassAndAssessments("topic1", "misfits", "Alfred", "Bernadette");
 		initUiForTests();
 		
 		// when
 		h.groupSelectionCompleted(misfits);
 		$("cbTopic").setSelected("topic1");
 		$("cbAssessment").setSelected("1/1/2001 - 31/12/2001");
 		
 		// then
 		for(int i=0; i<$("tbGrades").getRowCount(); ++i) {
 			System.out.println(Arrays.toString($("tbGrades").getRowText(i)));
 		}
 		assertEquals(3, $("tbGrades").getRowCount());
 		assertEquals("Alfred's results",
 				array("Alfred", "", "B", "C", "12%"),
 				$("tbGrades").getRowText(0));
 		assertEquals("Bernadette's results",
 				array("Bernadette", "B", "C", "A", "16%"),
 				$("tbGrades").getRowText(1));
 		assertEquals("average results",
 				array("plugins.learn.gradebook.average", "12%", "36%", "0%", "16%"),
 				$("tbGrades").getRowText(2));
 	}
 	
 	public void testGradeTableInitialisedEmpty() {
 		assertEquals(0, $("tbGrades").getRowCount());
 	}
 	
 	public void testGradeTableColumnsInitialised() {
 		assertEquals("Grade table column names",
 				array("plugins.learn.student"),
 				$("tbGrades").getColumnTitles());
 	}
 	
 	public void testSelectClassButtonShowsGroupSelecterDialog() {
 		// when
 		$("btSelectClass").click();
 		
 		// then
 		assertTrue($("dgGroupSelecter").isVisible());
 	}
 	
 	public void testSelectingClassUpdatesClassTextfield() {
 		// given
 		Group g = mockEmptyGradebookAndClass();
 		
 		// when
 		h.groupSelectionCompleted(g);
 		
 		// then
 		assertEquals("empty class", $("tfClass").getText());
 	}
 	
 	public void testSelectingClassUpdatesGradeTableColumnNames() {
 		// given
 		Group g = mockGradeBookAndClass();
 		
 		// when
 		h.groupSelectionCompleted(g);
 		
 		// then
 		assertEquals("Grade table column names",
 				array("plugins.learn.student", "Health & Safety", "Maths + English", "Electronics", "plugins.learn.gradebook.average"),
 				$("tbGrades").getColumnTitles());
 	}
 	
 	public void testSelectingClassUpdatesGradeTableStudents() {
 		// given
 		Group g = mockGradeBookAndClass();
 		
 		// when
 		h.groupSelectionCompleted(g);
 		
 		// then
 		assertEquals("Student column values",
 				array("Angela", "Beatrix", "Clare", "Dave", "Edwina", "plugins.learn.gradebook.average"),
 				$("tbGrades").getColumnText(0));
 	}
 	
	public void testChangingClassCorrectlyUpdatesGradeTableStudents() {
		// given
		Group g1 = mockGradeBookAndClass();
		h.groupSelectionCompleted(g1);
		Group g2 = mockEmptyGradebookAndClass();
		
		// when
		h.groupSelectionCompleted(g2);
		
		// then
		assertEquals("Student column values",
				array("plugins.learn.gradebook.average"),
				$("tbGrades").getColumnText(0));
	}
	
 	public void testSelectingClassUpdatesGradeTableResults() {
 		// given
 		Group g = mockGradeBookAndClass();
 		
 		// when
 		h.groupSelectionCompleted(g);
 		
 		// then
 		assertEquals(5, $("tbGrades").getColumnCount());
 		assertEquals("Results (Health & Safety)",
 				array("100%", "47%", "82%", "100%", "4%", "67%"),
 				$("tbGrades").getColumnText(1));
 		assertEquals("Results (Maths + English)",
 				array("plugins.learn.gradebook.result.none",
 						"plugins.learn.gradebook.result.none",
 						"plugins.learn.gradebook.result.none",
 						"plugins.learn.gradebook.result.none",
 						"plugins.learn.gradebook.result.none", "0%"),
 				$("tbGrades").getColumnText(2));
 		assertEquals("Results (Electronics)",
 				array("17%", "52%", "76%", "100%", "100%", "69%"),
 				$("tbGrades").getColumnText(3));
 		assertEquals("Average results",
 				array("39%", "33%", "52%", "66%", "34%", "45%"),
 				$("tbGrades").getColumnText(4));
 	}
 	
 	public void testTopicSelecterShouldUpdateWhenANewTopicIsCreated() {
 		// given
 		mockTopics(topicDao, "topic1");
 		initUiForTests();
 		assertEquals("Topic selecter content",
 				array("plugins.learn.topic.all", "topic1"),
 				$("cbTopic").getOptions());
 		
 		// when
 		mockTopics(topicDao, "topic1", "topic2");
 		h.notify(mockEntitySavedNotification(Topic.class));
 		
 		// then
 		waitForUiEvents();
 		assertEquals("Topic selecter content",
 				array("plugins.learn.topic.all", "topic1", "topic2"),
 				$("cbTopic").getOptions());
 	}
 	
 	public void testTopicSelecterShouldUpdateWhenATopicIsDeleted() {
 		// given
 		mockTopics(topicDao, "topic1", "topic2");
 		initUiForTests();
 		assertEquals("Topic selecter content",
 				array("plugins.learn.topic.all", "topic1", "topic2"),
 				$("cbTopic").getOptions());
 		
 		// when
 		mockTopics(topicDao, "topic2");
 		h.notify(mockEntityDeletedNotification(Topic.class));
 		
 		// then
 		waitForUiEvents();
 		assertEquals("Topic selecter content",
 				array("plugins.learn.topic.all", "topic2"),
 				$("cbTopic").getOptions());
 	}
 	
 	public void testTabHandlerShouldRegisterWithEventBusOnInit() {
 		verify(eventBus).registerObserver(h);
 	}
 	
 	public void testTabHandlerShouldUnregisterWithEventBusOnUiDestroy() {
 		// when
 		h.notify(new UiDestroyEvent(ui));
 		
 		// then
 		verify(eventBus).unregisterObserver(h);
 	}
 	
 //> SETUP HELPER METHODS
 	private Group mockEmptyGradebookAndClass() {
 		return mockEmptyGradebookAndClass("empty class");
 	}
 	
 	private Group mockEmptyGradebookAndClass(String name) {
 		Group g = mockGroup(name);
 		ClassGradebook gb = mock(ClassGradebook.class);
 		when(gb.getTopicAverages()).thenReturn(new int[0]);
 		when(gradebookService.getForClass(g)).thenReturn(gb);
 		return g;
 	}
 	
 	private Group[] mockEmptyGradebooksAndClasses(int count) {
 		Group[] groups = new Group[count];
 		for (int i = 0; i < count; i++) {
 			groups[i] = mockEmptyGradebookAndClass("group-" + i);
 		}
 		return groups;
 	}
 
 	private Group mockGradeBookAndClass() {
 		Group g = mockGroup("rd");
 		Contact[] students = mockContacts("Angela", "Beatrix", "Clare", "Dave", "Edwina");
 		when(groupMembershipDao.getActiveMembers(g)).thenReturn(asList(students));
 		
 		ClassGradebook gb = mock(ClassGradebook.class);
 		Topic[] mockTopics = mockTopics("Health & Safety", "Maths + English", "Electronics");
 		when(gb.getTopics()).thenReturn(asList(mockTopics));
 		List<StudentGrades> results = asList(
 				mockStudentClassResults(students[0], 100, null, 17),
 				mockStudentClassResults(students[1], 47, null, 52),
 				mockStudentClassResults(students[2], 82, null, 76),
 				mockStudentClassResults(students[3], 100, null, 100),
 				mockStudentClassResults(students[4], 4, null, 100));
 		when(gb.getResults()).thenReturn(results);
 		when(gb.getTopicAverages()).thenReturn(array(67, 0, 69, 45));
 		when(gradebookService.getForClass(g)).thenReturn(gb);
 		
 		return g;
 	}
 	
 	private Group mockTopicAndClassAndAssessments(String topicName, String groupName, String... studentNames) {
 		Topic t = mockTopics(topicDao, topicName)[0];
 		
 		Group g = mockGroup(groupName);
 		
 		ClassGradebook classGb = mock(ClassGradebook.class);
 		when(classGb.getTopics()).thenReturn(asList(t));
 		when(classGb.getTopicAverages()).thenReturn(new int[0]);
 		when(gradebookService.getForClass(g)).thenReturn(classGb);
 		
 		// create students
 		Contact[] students = mockContacts(studentNames);
 		when(groupMembershipDao.getActiveMembers(g)).thenReturn(asList(students));
 		
 		// create assessment
 		Assessment[] assessments = mockAssessments(assessmentDao, g, t, 2);
 		
 		// create gradebook for each assessment
 		int baseIndex = 0;
 		for(Assessment a : assessments) {
 			AssessmentGradebook gb = mock(AssessmentGradebook.class);
 			when(gb.getQuestionCount()).thenReturn(3);
 			StudentTopicResult[] results = mockStudentTopicResults(baseIndex, students, 3);
 			when(gb.getAverages()).thenReturn(new int[]{12, 36, 0, 16});
 			when(gb.getResults()).thenReturn(results);
 			when(gradebookService.getForAssessment(a)).thenReturn(gb);
 			baseIndex += 10;
 		}
 		
 		return g;
 	}
 
 	private StudentTopicResult[] mockStudentTopicResults(int baseIndex, Contact[] students,
 			int questionCount) {
 		int counter = baseIndex-1;
 		ArrayList<StudentTopicResult> results = new ArrayList<StudentTopicResult>();
 		for(Contact s : students) {
 			StudentTopicResult r = mock(StudentTopicResult.class);
 			when(r.getContact()).thenReturn(s);
 			AssessmentMessageResponse[] individualResponses = new AssessmentMessageResponse[questionCount];
 			for (int i = 0; i < individualResponses.length; i++) {
 				AssessmentMessageResponse iqr = mock(AssessmentMessageResponse.class);
 				Integer value = counter < baseIndex? null: counter;
 				if(value != null) {
 					when(iqr.isCorrect()).thenReturn((counter % 1) == 0);
 					when(iqr.getAnswer()).thenReturn(value % 3);
 					individualResponses[i] = iqr;
 				}
 				++counter;
 			}
 			when(r.getResponses()).thenReturn(individualResponses);
 			when(r.getScore()).thenReturn(counter++);
 			results.add(r);
 		}
 		return results.toArray(new StudentTopicResult[results.size()]);
 	}
 	
 	private Assessment[] mockAssessments(AssessmentDao assessmentDao, Group g, Topic t, int count) {
 		ArrayList<Assessment> assessments = new ArrayList<Assessment>();
 		for(int i=0; i<count; ++i) {
 			Assessment a = mock(Assessment.class);
 			when(a.getStartDate()).thenReturn(firstOfJanuary(2000 + i));
 			when(a.getEndDate()).thenReturn(newYearsEve(2000 + i));
 			assessments.add(a);
 		}
 		when(assessmentDao.findAllByGroupAndTopic(g, t)).thenReturn(assessments);
 		return assessments.toArray(new Assessment[0]);
 	}
 
 	private Long firstOfJanuary(int year) {
 		Calendar c = Calendar.getInstance();
 		c.set(year, 0, 1);
 		return c.getTimeInMillis();
 	}
 
 	private Long newYearsEve(int year) {
 		Calendar c = Calendar.getInstance();
 		c.set(year, 11, 31);
 		return c.getTimeInMillis();
 	}
 }
