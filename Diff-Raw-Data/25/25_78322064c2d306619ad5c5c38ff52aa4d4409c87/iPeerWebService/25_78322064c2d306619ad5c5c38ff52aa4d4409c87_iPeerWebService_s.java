 package ca.ubc.ctlt.ipeerb2.webservice;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.annotation.Resource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.security.oauth.consumer.OAuthConsumerToken;
 import org.springframework.security.oauth.consumer.OAuthSecurityContextHolder;
 import org.springframework.security.oauth.consumer.OAuthSecurityContextImpl;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.RestTemplate;
 
 import ca.ubc.ctlt.ipeerb2.Configuration;
 import ca.ubc.ctlt.ipeerb2.iPeerB2ProtectedResourceDetailsService;
 import ca.ubc.ctlt.ipeerb2.domain.Course;
 import ca.ubc.ctlt.ipeerb2.domain.Department;
 import ca.ubc.ctlt.ipeerb2.domain.Event;
 import ca.ubc.ctlt.ipeerb2.domain.Grade;
 import ca.ubc.ctlt.ipeerb2.domain.Group;
 import ca.ubc.ctlt.ipeerb2.domain.User;
 
 @Component
 public class iPeerWebService {
 	@Autowired
     private MessageSource messageSource;
 	
 	@Resource(name="restTemplate")
 	private RestTemplate restTemplate;
 	
 	@Autowired
 	private Configuration configuration;
 
 	public static final String API_VERSION = "v1";
 	public static final String API_COURSE = "/" + API_VERSION + "/courses";
 	public static final String API_USER = "/" + API_VERSION + "/users";
 	public static final String API_GROUP = "/" + API_VERSION + "/courses/{course_id}/groups";
 	public static final String API_COURSE_USER = "/" + API_VERSION + "/courses/{course_id}/users";
 	public static final String API_GROUP_USER = "/" + API_VERSION + "/groups/{group_id}/users";
 	public static final String API_EVENT = "/" + API_VERSION + "/courses/{course_id}/events";
 	public static final String API_GRADE = "/" + API_VERSION + "/events/{event_id}/grades";
 	public static final String API_DEPARTMENT = "/" + API_VERSION + "/departments";
 	//private static final Logger logger = Logger.getLogger(iPeerWebService.class);
 	
 	public String getServerUrl() {
 		// Temporary hack to generate an OAuth security context with token
 		// This fixes the requests that are not covered by oauth:url patten defined in applicationContext.xml
 		if (OAuthSecurityContextHolder.getContext() == null) {
 			OAuthSecurityContextImpl context = new OAuthSecurityContextImpl();
 	
 			String key = configuration.getSetting(Configuration.TOKEN_KEY);
 			String secret = configuration.getSetting(Configuration.TOKEN_SECRET);
 			if (null == key || key.isEmpty() || null == secret || secret.isEmpty()) {
 				throw new RuntimeException("Token key or secret is empty. Did you forget to set up the building block?");
 			}
 			
 			OAuthConsumerToken token = new OAuthConsumerToken();
 			token.setAccessToken(true);
 			token.setSecret(secret);
 			token.setValue(key);
 			
 			Map<String, OAuthConsumerToken> accessTokens = new TreeMap<String, OAuthConsumerToken>();
 			accessTokens.put(iPeerB2ProtectedResourceDetailsService.IPEERB2_DETAILS_ID, token);
 					
 	
 			context.setAccessTokens(accessTokens);
 			OAuthSecurityContextHolder.setContext(context);
 		}
 		
 		return configuration.getSetting(Configuration.IPEER_URL);
 	}
 
 	/************** Course APIs ****************/
 	
 	public List<Course> getCourseList() {
 		Course[] courses = restTemplate.getForObject(getServerUrl() + API_COURSE, Course[].class);
 		List<Course> result = Arrays.asList(courses);
		
 		return result;
 	}
 
 	public Course getCourse(int id) {
 		Course result = null;
 		result = restTemplate.getForObject(getServerUrl() + API_COURSE + "/{id}", Course.class, String.valueOf(id));
 		
 		return result;
 	}
 	
 	public Course createCourse(Course course) {
 		Course result = null;
 		result = restTemplate.postForObject(getServerUrl() + API_COURSE, course, Course.class);
 
 		return result;
 	}
 	
 	public boolean deleteCourse(int id) {
 		restTemplate.delete(getServerUrl() + API_COURSE + "/{id}", String.valueOf(id));
 
 		return true;
 	}
 	
 	public boolean updateCourse(Course course) {
 		restTemplate.put(getServerUrl() + API_COURSE + "/{id}", course, String.valueOf(course.getId()));
 		
 		return true;
 	}
 	
 	/************** User APIs ****************/
 	
 	public List<User> getUserList() {
 		User[] users = restTemplate.getForObject(getServerUrl() + API_USER, User[].class);
 		List<User> result = Arrays.asList(users);
 		return result;
 	}
 	
 	public User getUser(int id) {
 		User result = null;
 		result = restTemplate.getForObject(getServerUrl() + API_USER + "/{id}", User.class, String.valueOf(id));
 		
 		return result;
 	}
 	
 	public User createUser(User user) {
 		User result = null;
 		result = restTemplate.postForObject(getServerUrl() + API_USER, user, User.class);
 		
 		return result;
 	}
 	
 	public List<User> createUsers(List<User> users) {
		User[] result = null;
 		result = restTemplate.postForObject(getServerUrl() + API_USER, users.toArray(new User[users.size()]), User[].class);
 		
 		return Arrays.asList(result);
 	}
 	
 	public boolean deleteUser(int id) {
 		restTemplate.delete(getServerUrl() + API_USER + "/{id}", String.valueOf(id));
 		
 		return true;
 	}
 	
 	public boolean updateUser(User user) {
 		restTemplate.put(getServerUrl() + API_USER + "/{id}", user, String.valueOf(user.getId()));
 		
 		return true;
 	}
 	
 	/************** Group APIs ****************/
 	
 	public List<Group> getGroupList(int courseId) {
 		Group[] groups = restTemplate.getForObject(getServerUrl() + API_GROUP, Group[].class, courseId);
 		List<Group> result = Arrays.asList(groups);
 		return result;
 	}
 	
 	public Group getGroup(int id) {
 		Group result = null;
 		result = restTemplate.getForObject(getServerUrl() + API_GROUP + "/{id}", Group.class, 0, String.valueOf(id));
 		
 		return result;
 	}
 	
 	public Group createGroup(int courseId, Group group) {
 		Group result = null;
 		result = restTemplate.postForObject(getServerUrl() + API_GROUP, group, Group.class, courseId);
 		
 		return result;
 	}
 	
 	public List<Group> createGroups(int courseId, List<Group> groups) {
 		Group[] result = null;
 		result = restTemplate.postForObject(getServerUrl() + API_GROUP, groups.toArray(new Group[groups.size()]), Group[].class, courseId);
 
 		return Arrays.asList(result);
 	}
 	
 	public boolean deleteGroup(int id) {
 		restTemplate.delete(getServerUrl() + API_GROUP + "/{id}", 0, String.valueOf(id));
 		
 		return true;
 	}
 	
 	public boolean updateGroup(Group group) {
 		restTemplate.put(getServerUrl() + API_GROUP + "/{id}", group, 0, String.valueOf(group.getId()));
 		
 		return true;
 	}
 	
 	/************** Course/User APIs ****************/
 	
 	public List<User> getUsersInCourse(int courseId) {
 		User[] users = restTemplate.getForObject(getServerUrl() + API_COURSE_USER, User[].class, courseId);
 		List<User> result = Arrays.asList(users);
 		return result;
 	}
 	
 	public List<User> enrolUsersInCourse(int courseId, List<User> users) {
 		User[] result = null;
 		result = restTemplate.postForObject(getServerUrl() + API_COURSE_USER, users.toArray(), User[].class, courseId);
 		
 		return Arrays.asList(result);
 	}
 	
 	public boolean removeUserFromCourse(int courseId, long userId) {
 		restTemplate.delete(getServerUrl() + API_COURSE_USER + "/{user_id}", courseId, userId);
 		
 		return true;	
 	}
 	
 	public boolean removeUserFromCourse(int courseId, User user) {
 		return removeUserFromCourse(courseId, user.getId());
 	}
 	
 	/************** Group/User APIs ****************/
 	
 	public List<User> getUsersInGroup(int groupId) {
 		User[] users = restTemplate.getForObject(getServerUrl() + API_GROUP_USER, User[].class, groupId);
 		List<User> result = Arrays.asList(users);
 		return result;
 	}
 	
 	public List<User> enrolUsersInGroup(int groupId, List<User> users) {
 		User[] result = null;
 		result = restTemplate.postForObject(getServerUrl() + API_GROUP_USER, users.toArray(), User[].class, groupId);
 		
 		return Arrays.asList(result);
 	}
 	
 	public boolean removeUserFromGroup(int groupId, long userId) {
 		restTemplate.delete(getServerUrl() + API_GROUP_USER + "/{user_id}", groupId, userId);
 
 		return true;	
 	}
 	
 	public boolean removeUserFromGroup(int groupId, User user) {
 		return removeUserFromGroup(groupId, user.getId());
 	}
 	
 	/************** Event APIs ****************/
 	
 	public List<Event> getEventsInCourse(int courseId) {
 		Event[] events = restTemplate.getForObject(getServerUrl() + API_EVENT, Event[].class, courseId);
 		List<Event> result = Arrays.asList(events);
 		return result;	
 	}
 	
 	public Event getEvent(int eventId) {
 		Event result = null;
 		result = restTemplate.getForObject(getServerUrl() + API_EVENT + "/{id}", Event.class, 0, eventId);
 		
 		return result;	
 	}
 	
 	public List<Event> getEventsForUser(String username) {
 		Event[] result;
 		result = restTemplate.getForObject(getServerUrl() + API_USER + "/{username}/events", Event[].class, username);
 		
 		return Arrays.asList(result);	
 	}
 	
 	public List<Event> getEventsForUserInCourse(String username, int courseId) {
 		Event[] result;
 		result = restTemplate.getForObject(getServerUrl() + API_COURSE + "/{course_id}/users/{username}/events", Event[].class, courseId, username);
 		
 		return Arrays.asList(result);	
 	}
 	
 	/************** Grade APIs ****************/
 	
 	public List<Grade> getGradesInEvent(int eventId) {
 		Grade[] grades = restTemplate.getForObject(getServerUrl() + API_GRADE, Grade[].class, eventId);
 		List<Grade> result = Arrays.asList(grades);
 		return result;	
 	}
 	
 	public Grade getGradesForUserInEvent(int userId, int eventId) {
 		Grade result;
 		result = restTemplate.getForObject(getServerUrl() + API_GRADE + "/{user_id}", Grade.class, eventId, userId);
 		
 		return result;	
 	}
 	
 	/***************** Department API ***************/
 	public List<Department> getDepartmentList() {
 		Department[] departments = restTemplate.getForObject(getServerUrl() + API_DEPARTMENT, Department[].class);
 		List<Department> result = Arrays.asList(departments);
 		return result;
 	}
 	
 	public boolean assignCourseToDepartment(int courseId, int departmentId) {
 		restTemplate.postForLocation(getServerUrl() + API_COURSE + "/{course_id}/departments/{department_id}", "", courseId, departmentId);
 		
 		return true;
 	}
 }
