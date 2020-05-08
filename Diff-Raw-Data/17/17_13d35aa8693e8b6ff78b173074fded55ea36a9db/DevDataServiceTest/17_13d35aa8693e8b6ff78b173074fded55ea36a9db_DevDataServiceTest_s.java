 package org.ssg.gui.server.devdata;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.reset;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.CharArrayWriter;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.hamcrest.CoreMatchers;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Matchers;
 import org.mockito.internal.verification.Times;
 import org.mockito.internal.verification.VerificationModeFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcOperations;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.ssg.core.domain.Module;
 import org.ssg.core.domain.Student;
 import org.ssg.core.domain.Task;
 import org.ssg.core.domain.Topic;
 import org.ssg.core.dto.TaskType;
 import org.ssg.core.service.CurriculumDao;
 import org.ssg.core.service.StudentService;
 import org.ssg.core.service.UserDao;
 import org.ssg.core.support.TstDataBuilder;
 import org.ssg.core.support.TstDataUtils;
 
@ActiveProfiles({"junit", "junit-mock-core"})
@ContextConfiguration(locations = { "classpath:/spring/dev-data.ctx.xml", "classpath:/spring/junit-mock-core-profile.ctx.xml"})
 @RunWith(SpringJUnit4ClassRunner.class)
 public class DevDataServiceTest {
 
 	@Autowired
 	private DefaultDevDataService devDataService;
 
 	private Map<Object, Object> params = new HashMap<Object, Object>();
 
 	private UserDao userDao;
 	private JdbcOperations jdbcOperations;
 	private CurriculumDao curriculumDao;
 	private StudentService studentService;
 	private PrintWriter writer;
 	private CharArrayWriter out;
 
 	@Before
 	public void setUp() {
 		userDao = devDataService.getUserDao();
 		jdbcOperations = devDataService.getJdbcOperations();
 		curriculumDao = devDataService.getCurriculumDao();
 		studentService = devDataService.getStudentService();
 		out = new CharArrayWriter();
 		writer = new PrintWriter(out);
 
 		Student st = TstDataUtils.createStudent("jd");
 		st.setId(10);
 		when(userDao.getStudentByName(eq("jd"))).thenReturn(st);
 	}
 
 	@After
 	public void tearDown() {
 		reset(userDao, jdbcOperations, curriculumDao);
 	}
 
 	@Test
 	public void verifyThatIfRequestWithUserParamThenNewStudentIsCreatedWithGivenName() {
 		// Given
 		prepareParamsForUser();
 
 		// When
 		devDataService.processRequest(params, writer);
 
 		// Then
 		ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
 		verify(userDao).saveStudent(studentCaptor.capture());
 		assertThat(studentCaptor.getValue().getName(), CoreMatchers.is("jd"));
 
 	}
 
 	private void prepareParamsForUser() {
 		params.put("user", new String[] { "jd" });
 	}
 
 	@Test
 	public void verifyThatIfRequestWithUserParamThenNewUserIsCreatedWithGivenName() {
 		// Given
 		prepareParamsForUser();
 
 		// When
 		devDataService.processRequest(params, writer);
 
 		// Then
 		verify(jdbcOperations).execute(Matchers.eq("insert into users values('jd','1',TRUE)"));
 	}
 
 	@Test
 	public void verifyThatIfRequestWithUserParamThenRoleStudentIsAssignedToNewUser() {
 		// Given
 		prepareParamsForUser();
 
 		// When
 		devDataService.processRequest(params, writer);
 
 		// Then
 		verify(jdbcOperations).execute(Matchers.eq("insert into authorities values('jd','student')"));
 	}
 
 	@Test
 	public void verifyThatIfRequestWithUserParamThenOutPutIsIdOfNewUser() {
 		// Given
 		prepareParamsForUser();
 
 		// When
 		devDataService.processRequest(params, writer);
 
 		// Then
 		assertThat(out.toString(), is("User Id: 10"));
 	}
 
 	@Test
 	public void verifyThatIfRequestWithModuleThenNewModuleIsCreated() {
 		// Given
 		params.put("module", new String[] { "withThreeTopics" });
 		when(curriculumDao.getAllModules()).thenReturn(
 		        Arrays.asList(TstDataUtils.createModule("Module withThreeTopics", 12)));
 
 		Module moduleWithTopics = TstDataUtils.createModule("Module withThreeTopics", 12);
 		when(curriculumDao.getModuleById(eq(12))).thenReturn(moduleWithTopics);
 
 		// When
 		devDataService.processRequest(params, writer);
 
 		// Then
 		ArgumentCaptor<Module> moduleCaptor = ArgumentCaptor.forClass(Module.class);
 		verify(curriculumDao).saveModule(moduleCaptor.capture());
 		assertThat(moduleCaptor.getValue().getName(), is("Module withThreeTopics"));
 
 	}
 
 	@Test
 	public void verifyThatIfRequestWithTopicsThenTopicsAreCreatedWithGivenQuantity() {
 		// Given
 		params.put("module", new String[] { "withThreeTopics" });
 		params.put("topics", new String[] { "3" });
 		Module savedModule = TstDataUtils.createModule("Module withThreeTopics", 12);
 		when(curriculumDao.getAllModules()).thenReturn(Arrays.asList(savedModule));
 
 		Module moduleWithTopics = TstDataUtils.createModule("Module withThreeTopics", 12);
 		moduleWithTopics = TstDataUtils.enrichModuleWithTopics(moduleWithTopics);
 		moduleWithTopics.getTopics().get(0).setId(1);
 		moduleWithTopics.getTopics().get(1).setId(2);
 		moduleWithTopics.getTopics().get(2).setId(3);
 		when(curriculumDao.getModuleById(eq(12))).thenReturn(moduleWithTopics);
 
 		// When
 		devDataService.processRequest(params, writer);
 
 		// Then
 		ArgumentCaptor<Topic> topicCaptor = ArgumentCaptor.forClass(Topic.class);
 		verify(curriculumDao, new Times(3)).saveTopic(topicCaptor.capture());
 		assertThat(topicCaptor.getValue().getName(), is("Topic 3"));
 		assertThat(topicCaptor.getValue().getModule(), is(savedModule));
 
 	}
 
 	@Test
 	public void verifyThatIfRequestWithModuleThenOutputIsModuleId() {
 		// Given
 		params.put("module", new String[] { "withThreeTopics" });
 
 		when(curriculumDao.getAllModules()).thenReturn(
 		        Arrays.asList(TstDataUtils.createModule("Module withThreeTopics", 12)));
 
 		Module moduleWithTopics = TstDataUtils.createModule("Module withThreeTopics", 12);
 		when(curriculumDao.getModuleById(eq(12))).thenReturn(moduleWithTopics);
 
 		// When
 		devDataService.processRequest(params, writer);
 
 		// Then
		assertThat(out.toString(), is("Module Id: 12\r\n"));
 
 	}
 
 	@Test
 	public void verifyThatIfRequestWithTopicsThenThenOutputIsModuleAndTopicIds() {
 		// Given
 		params.put("module", new String[] { "withThreeTopics" });
 		params.put("topics", new String[] { "3" });
 
 		when(curriculumDao.getAllModules()).thenReturn(
 		        Arrays.asList(TstDataUtils.createModule("Module withThreeTopics", 12)));
 
 		Module moduleWithTopics = TstDataUtils.createModule("Module withThreeTopics", 12);
 		moduleWithTopics = TstDataUtils.enrichModuleWithTopics(moduleWithTopics);
 		moduleWithTopics.getTopics().get(0).setId(1);
 		moduleWithTopics.getTopics().get(1).setId(2);
 		moduleWithTopics.getTopics().get(2).setId(3);
 		when(curriculumDao.getModuleById(eq(12))).thenReturn(moduleWithTopics);
 
 		// When
 		devDataService.processRequest(params, writer);
 
 		// Then
		assertThat(out.toString(), is("Module Id: 12\r\nTopic Id: 1\r\nTopic Id: 2\r\nTopic Id: 3\r\n"));
 
 	}
 
 	@Test
 	public void verifyThatIfRequestWithHomeworkThenNewHomeworkIsCreatedForGivenStudentAndModuleIds() {
 		// Given
 		params.put("homeworkForStudent", new String[] { "10" });
 		params.put("moduleId", new String[] { "1" });
 
 		// When
 		devDataService.processRequest(params, writer);
 
 		// Then
 		verify(studentService).giveHomework(eq(10), eq(1));
 	}
 
 	@Test
 	public void givenTaskForTopicRequestThenThreeTypesOfTasksSholdBeCreated() {
 		// Given
 		param("taskForTopic", "10");
 
 		Topic topic = TstDataBuilder.topicBuilder().id(10).name("some name").build();
 
 		when(curriculumDao.getTopic(eq(10))).thenReturn(topic);
 
 		// When
 		devDataService.processRequest(params, writer);
 
 		// Then
 		verify(curriculumDao, VerificationModeFactory.times(3)).saveTask(Matchers.isA(Task.class));
 		assertThat(topic.getTasks().size(), is(3));
 	}
 
 	private void param(String name, String value) {
 		params.put(name, new String[] { value });
 	}
 
 }
