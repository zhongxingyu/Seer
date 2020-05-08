 package ca.ubc.ctlt.ipeerb2.webservice;
 
 import ca.ubc.ctlt.ipeerb2.Configuration;
 import ca.ubc.ctlt.ipeerb2.IPeerB2OAuthRestTemplate;
 import ca.ubc.ctlt.ipeerb2.domain.*;
 import ca.ubc.ctlt.ipeerb2.iPeerB2ProtectedResourceDetails;
 import ca.ubc.ctlt.ipeerb2.iPeerResponseErrorHandler;
 import org.mockito.Mockito;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.http.converter.HttpMessageNotReadableException;
 import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import javax.annotation.Resource;
 import java.util.*;
 
 import static org.mockito.Mockito.doReturn;
 import static org.testng.AssertJUnit.*;
 
 @ContextConfiguration
 @ActiveProfiles({"dev", "integration"})
 public class iPeerWebServiceIntegrationTest extends AbstractTestNGSpringContextTests {
 	
 	@Autowired
 	private iPeerWebService webService;
 	
 	@Autowired
 	private Course courseToCreate;
 	
 	@Autowired
 	private Course invalidCourseToCreate;
 	
 	@Autowired
 	private Course courseToUpdate;
 	
 	@Autowired
 	private Course invalidCourseToUpdate;
 	
 	@Autowired
 	private User userToCreate;
 	
 	@Autowired
 	private User invalidUserToCreate;
 	
 	@Autowired
 	private User userToUpdate;
 	
 	@Autowired
 	private User invalidUserToUpdate;
 	
 	@Resource
 	private List<User> userList;
 	
 	@Resource
 	private List<User> usersWithoutOptionalFields;
 
     @Resource
     private List<User> usersReturnEmpty;
 
     @Resource
     private List<User> usersReturnInvalid;
 
 	@Autowired
 	private Group groupToCreate;
 	
 	@Autowired
 	private Group invalidGroupToCreate;
 	
 	@Autowired
 	private Group groupToUpdate;
 	
 	@Autowired
 	private Group invalidGroupToUpdate;
 
 	@Autowired
 	private Configuration configuration;
 	
 	@Autowired
 	private Properties config;
 	
 	/************* Course API Tests ****************/
 	
 	@BeforeMethod
 	public void setUp() throws Exception {
 		// mocking configuration
 		doReturn(config).when(configuration).getSettings();
 
 //        iPeerB2ProtectedResourceDetails details = Mockito.mock(iPeerB2ProtectedResourceDetails.class);
 //        IPeerB2OAuthRestTemplate restTemplate = new IPeerB2OAuthRestTemplate(details);
 //        MappingJacksonHttpMessageConverter msgConverter = new MappingJacksonHttpMessageConverter();
 //        msgConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
 //        restTemplate.setErrorHandler(new iPeerResponseErrorHandler());
 //        restTemplate.getMessageConverters().add(msgConverter);
 //        webService = new iPeerWebService();
 //        webService.setRestTemplate(restTemplate);
     }
 	
 	@Test
 	public final void testGetCourseList() {
 		List<Course> cl = webService.getCourseList();
 		assertTrue(cl.size() == 3);
 		Course c = cl.get(0);
 		assertTrue(c.getId() == 1);
 		assertTrue("Test Course".equals(c.getTitle()));
 		assertEquals(30, c.getClassSize());
 		
 		c = cl.get(1);
 		assertTrue(c.getId() == 2);
 		assertTrue("Test Course 2".equals(c.getTitle()));
 		
 		c = cl.get(2);
 		assertTrue(c.getId() == 3);
 		assertTrue("Test Course 3".equals(c.getTitle()));
 	}
 
 	@Test
 	public final void testGetCourse() {
 		Course course = webService.getCourse(1);
 		assertNotNull(course);
 		assertTrue(course.getId() == 1);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetInvalidCourse() {
 		webService.getCourse(999);
 		Assert.fail("No exception when requesting an invalid course!");
 	}
 	
 	@Test
 	public final void testCreateCourse() {
 		Course course = webService.createCourse(courseToCreate);
 		assertTrue(course.getId() == 1);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testCreateInvalidCourse() {
 		webService.createCourse(invalidCourseToCreate);
 		Assert.fail("No exception when creating an invalid course!");
 	}
 	
 	@Test
 	public final void testDeleteCourse() {
 		boolean result = webService.deleteCourse(1);
 		assertTrue(result);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testDeleteNonExistingCourse() {
 		webService.deleteCourse(999);
 		Assert.fail("No exception when deleting an invalid course!");
 	}
 	
 	@Test
 	public final void testUpdateCourse() {
 		boolean result = webService.updateCourse(courseToUpdate);
 		assertTrue(result);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testUpdateInvalidCourse() {
 		webService.updateCourse(invalidCourseToUpdate);
 		Assert.fail("No exception when deleting an invalid course!");
 	}
 	
 	/************* User API Tests ****************/
 	
 	@Test
 	public final void testGetUserList() {
 		List<User> cl = webService.getUserList();
 		assertTrue(cl.size() == 3);
 		User c = cl.get(0);
 		assertTrue(c.getId() == 1);
 		assertTrue("username1".equals(c.getUsername()));
 		
 		c = cl.get(1);
 		assertTrue(c.getId() == 2);
 		assertTrue("username2".equals(c.getUsername()));
 		
 		c = cl.get(2);
 		assertTrue(c.getId() == 3);
 		assertTrue("username3".equals(c.getUsername()));
 	}
 	
 	@Test
 	public final void testGetUser() {
 		User user = webService.getUser(1);
 		assertNotNull(user);
 		assertTrue(user.getId() == 1);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetInvalidUser() {
 		webService.getUser(999);
 		Assert.fail("No exception when requesting an invalid user!");
 	}
 	
 	@Test
 	public final void testCreateUser() {
 		User user = webService.createUser(userToCreate);
 		assertTrue(user.getId() == 1);
 	}
 	
 	@Test
 	public final void testCreateUserWithoutOptionalFields() {
 		List<User> cl = webService.createUsers(usersWithoutOptionalFields);
 		assertTrue(cl.size() == 3);
 		User c = cl.get(0);
 		assertTrue(c.getId() == 1);
 		assertTrue("username1".equals(c.getUsername()));
 		
 		c = cl.get(1);
 		assertTrue(c.getId() == 2);
 		assertTrue("username2".equals(c.getUsername()));
 		
 		c = cl.get(2);
 		assertTrue(c.getId() == 3);
 		assertTrue("username3".equals(c.getUsername()));
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testCreateInvalidUser() {
 		webService.createUser(invalidUserToCreate);
 		Assert.fail("No exception when creating an invalid user!");
 	}
 	
 	@Test
 	public final void testDeleteUser() {
 		boolean result = webService.deleteUser(1);
 		assertTrue(result);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testDeleteNonExistingUser() {
 		webService.deleteUser(999);
 		Assert.fail("No exception when deleting an invalid user!");
 	}
 	
 	@Test
 	public final void testUpdateUser() {
 		boolean result = webService.updateUser(userToUpdate);
 		assertTrue(result);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testUpdateInvalidUser() {
 		webService.updateUser(invalidUserToUpdate);
 		Assert.fail("No exception when deleting an invalid user!");
 	}
 
     @Test
     public final void testCreateUsersReturnEmpty() {
         // when the all users created failed, it should return empty
         List<User> ul = webService.createUsers(usersReturnEmpty);
         assertTrue("Creating User Return Empty didn't return empty.", ul.size() == 0);
     }
 
    @Test(expectedExceptions=RuntimeException.class)
     public final void testCreateUsersReturnInvalid() {
         // when the all users created failed, it should return empty
         List<User> ul = webService.createUsers(usersReturnInvalid);
         Assert.fail("No exception when handling an invalid return value!");
     }
 
 	/************* Group API Tests ****************/
 	
 	@Test
 	public final void testGetGroupList() {
 		List<Group> cl = webService.getGroupList(1);
 		assertTrue(cl.size() == 3);
 		Group c = cl.get(0);
 		assertTrue(c.getId() == 1);
 		assertTrue("groupname1".equals(c.getName()));
 		assertEquals(5, c.getSize());
 		
 		c = cl.get(1);
 		assertTrue(c.getId() == 2);
 		assertTrue("groupname2".equals(c.getName()));
 		
 		c = cl.get(2);
 		assertTrue(c.getId() == 3);
 		assertTrue("groupname3".equals(c.getName()));
 	}
 	
 	@Test
 	public final void testGetGroup() {
 		Group group = webService.getGroup(1);
 		assertNotNull(group);
 		assertTrue(group.getId() == 1);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetInvalidGroup() {
 		webService.getGroup(999);
 		Assert.fail("No exception when requesting an invalid group!");
 	}
 	
 	@Test
 	public final void testCreateGroup() {
 		Group group = webService.createGroup(1, groupToCreate);
 		assertTrue(group.getId() == 1);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testCreateInvalidGroup() {
 		webService.createGroup(1, invalidGroupToCreate);
 		Assert.fail("No exception when creating an invalid group!");
 	}
 	
 	@Test
 	public final void testDeleteGroup() {
 		boolean result = webService.deleteGroup(1);
 		assertTrue(result);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testDeleteNonExistingGroup() {
 		webService.deleteGroup(999);
 		Assert.fail("No exception when deleting an invalid group!");
 	}
 	
 	@Test
 	public final void testUpdateGroup() {
 		boolean result = webService.updateGroup(groupToUpdate);
 		assertTrue(result);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testUpdateInvalidGroup() {
 		webService.updateGroup(invalidGroupToUpdate);
 		Assert.fail("No exception when deleting an invalid group!");
 	}
 	
 	/************* Course/User API Tests ****************/
 	
 	@Test
 	public final void testGetUsersInCourse() {
 		List<User> cl = webService.getUsersInCourse(1);
 		assertTrue(cl.size() == 3);
 		User c = cl.get(0);
 		assertTrue(c.getId() == 1);
 		assertTrue("username1".equals(c.getUsername()));
 		
 		c = cl.get(1);
 		assertTrue(c.getId() == 2);
 		assertTrue("username2".equals(c.getUsername()));
 		
 		c = cl.get(2);
 		assertTrue(c.getId() == 3);
 		assertTrue("username3".equals(c.getUsername()));
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetUsersInInvalidCourse() {
 		webService.getUsersInCourse(999);
 		Assert.fail("No exception when get users from invalid course!");
 	}
 	
 	@Test
 	public final void testEnrolUserInCourse() {
 		List<User> ul = webService.enrolUsersInCourse(1, userList);
 		assertTrue(ul.size() == userList.size());
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testEnrolInvalidUserInCourse() {
 		webService.enrolUsersInCourse(1, Arrays.asList(invalidUserToUpdate));
 		Assert.fail("No exception when enroling invalid users to course!");
 	}
 	
 	@Test
 	public final void testRemoveUsersFromCourse() {
 		boolean result = webService.removeUserFromCourse(1, userToUpdate);
 		assertTrue(result);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testRemoveInvalidUsersFromCourse() {
 		webService.removeUserFromCourse(1, invalidUserToUpdate);
 		Assert.fail("No exception when removing invalid users to course!");
 	}
 	
 	/************* Group/User API Tests ****************/
 	
 	@Test
 	public final void testGetUsersInGroup() {
 		List<User> cl = webService.getUsersInGroup(1);
 		assertTrue(cl.size() == 3);
 		User c = cl.get(0);
 		assertTrue(c.getId() == 1);
 		assertTrue("username1".equals(c.getUsername()));
 		
 		c = cl.get(1);
 		assertTrue(c.getId() == 2);
 		assertTrue("username2".equals(c.getUsername()));
 		
 		c = cl.get(2);
 		assertTrue(c.getId() == 3);
 		assertTrue("username3".equals(c.getUsername()));
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetUsersInInvalidGroup() {
 		webService.getUsersInGroup(999);
 		Assert.fail("No exception when get users from invalid group!");
 	}
 	
 	@Test
 	public final void testEnrolUserInGroup() {
 		List<User> ul = webService.enrolUsersInGroup(1, userList);
 		assertTrue(ul.size() == userList.size());
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testEnrolInvalidUserInGroup() {
 		webService.enrolUsersInGroup(1, Arrays.asList(invalidUserToUpdate));
 		Assert.fail("No exception when enroling invalid users to group!");
 	}
 	
 	@Test
 	public final void testRemoveUsersFromGroup() {
 		boolean result = webService.removeUserFromGroup(1, userToUpdate);
 		assertTrue(result);
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testRemoveInvalidUsersFromGroup() {
 		webService.removeUserFromGroup(1, invalidUserToUpdate);
 		Assert.fail("No exception when removing invalid users to group!");
 	}
 	
 	/************* Event API Tests ****************/
 	
 	@Test
 	public final void testGetEventsInCourse() {
 		List<Event> el = webService.getEventsInCourse(1);
 		assertTrue(el.size() == 3);
 		Event c = el.get(0);
 		assertTrue(c.getId() == 1);
 		assertTrue("eventname1".equals(c.getTitle()));
 		
 		c = el.get(1);
 		assertTrue(c.getId() == 2);
 		assertTrue("eventname2".equals(c.getTitle()));
 		
 		c = el.get(2);
 		assertTrue(c.getId() == 3);
 		assertTrue("eventname3".equals(c.getTitle()));
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetEventsInInvalidCourse() {
 		webService.getEventsInCourse(999);
 		Assert.fail("No exception when get events from invalid group!");
 	}
 	
 	@Test
 	public final void testGetEvent() {
 		Event event = webService.getEvent(1);
 		assertTrue(1 == event.getId());
 		assertTrue("eventname1".equals(event.getTitle()));
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetInvalidEvent() {
 		webService.getEvent(999);
 		Assert.fail("No exception when getting invalid event!");
 	}
 	
 	@Test
 	public final void testGetEventsForUser() {
 		List<Event> el = webService.getEventsForUser("administrator");
 		assertTrue(el.size() == 3);
 		Event c = el.get(0);
 		assertTrue(c.getId() == 1);
 		assertTrue("eventname1".equals(c.getTitle()));
 		
 		c = el.get(1);
 		assertTrue(c.getId() == 2);
 		assertTrue("eventname2".equals(c.getTitle()));
 		
 		c = el.get(2);
 		assertTrue(c.getId() == 3);
 		assertTrue("eventname3".equals(c.getTitle()));
 	}
 	
 	@Test
 	public final void testGetEventsForUserInCourse() {
 		List<Event> el = webService.getEventsForUserInCourse("administrator",1);
 		assertTrue(el.size() == 3);
 		Event c = el.get(0);
 		assertTrue(c.getId() == 1);
 		assertTrue("eventname1".equals(c.getTitle()));
 		
 		c = el.get(1);
 		assertTrue(c.getId() == 2);
 		assertTrue("eventname2".equals(c.getTitle()));
 		
 		c = el.get(2);
 		assertTrue(c.getId() == 3);
 		assertTrue("eventname3".equals(c.getTitle()));
 		
 		el = webService.getEventsForUserInCourse("student",1);
 		assertTrue(el.size() == 4);
 		c = el.get(0);
 		assertTrue(c.getId() == 1);
 		assertTrue("Term 1 Evaluation".equals(c.getTitle()));
 		
 		c = el.get(1);
 		assertTrue(c.getId() == 2);
 		assertTrue("Term Report Evaluation".equals(c.getTitle()));
 		
 		c = el.get(2);
 		assertTrue(c.getId() == 3);
 		assertTrue("Project Evaluation".equals(c.getTitle()));
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetEventsForInvalidUser() {
 		webService.getEventsForUser("invalidname");
 		Assert.fail("No exception when getting events for invalid user!");
 	}
 	
 	/************* Grade API Tests ****************/
 	
 	@Test
 	public final void testGetGradesInCourse() {
 		List<Grade> el = webService.getGradesInEvent(1);
 		assertTrue(el.size() == 3);
 		Grade u = el.get(0);
 		assertTrue(u.getId() == 1);
 		assertTrue(10.5 == u.getGrade());
 		
 		u = el.get(1);
 		assertTrue(u.getId() == 2);
 		assertTrue(10.6 == (u.getGrade()));
 		
 		u = el.get(2);
 		assertTrue(u.getId() == 3);
 		assertTrue(10.7 == u.getGrade());
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetGradesInInvalidEvent() {
 		webService.getGradesInEvent(999);
 		Assert.fail("No exception when get grades from invalid event!");
 	}
 	
 	@Test
 	public final void testGetGradeForUserInEvent() {
 		Grade grade = webService.getGradesForUserInEvent(1, 1);
 		assertTrue(1 == grade.getId());
 		assertTrue(10.5 == grade.getGrade());
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetGradesForInvalidUserInEvent() {
 		webService.getGradesForUserInEvent(999, 1);
 		Assert.fail("No exception when enroling invalid users to group!");
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testGetGradesForUserInInvalidEvent() {
 		webService.getGradesForUserInEvent(1, 999);
 		Assert.fail("No exception when enroling invalid users to group!");
 	}
 	
 	/************** Department API Tests *******************/
 	@Test
 	public final void testGetDepartmentList() {
 		List<Department> dl = webService.getDepartmentList();
 		assertTrue(dl.size() == 3);
 		Department u = dl.get(0);
 		assertTrue(u.getId() == 1);
 		assertTrue("department1".equals(u.getName()));
 		
 		u = dl.get(1);
 		assertTrue(u.getId() == 2);
 		assertTrue("department2".equals(u.getName()));
 		
 		u = dl.get(2);
 		assertTrue(u.getId() == 3);
 		assertTrue("department3".equals(u.getName()));
 	}
 	
 	@Test
 	public final void testAssignCourseToDepartment() {
 		assertTrue(webService.assignCourseToDepartment(1,1));
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testNonExistingCourseDepartmentAssociation() {
 		webService.assignCourseToDepartment(999,1);
 		Assert.fail("No exception when associating a non-exist course to department!");
 	}
 	
 	@Test(expectedExceptions=RuntimeException.class)
 	public final void testNonExistingDepartmentWithCourseAssociation() {
 		webService.assignCourseToDepartment(1,999);
 		Assert.fail("No exception when associating a course to a non-exist department!");
 	}
 	
 	/***************** Test Invalid Responses ******************/
 	
 	@Test(expectedExceptions=HttpMessageNotReadableException.class)
 	public final void testInvalidResponses() {
 		webService.getGroup(9999);
 		Assert.fail("No exception when getting invalid group!");
 	}
 }
