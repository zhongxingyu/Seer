 package ch.hszt.mdp.web;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.security.Principal;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
 
 import ch.hszt.mdp.domain.Activity;
 import ch.hszt.mdp.domain.Activity.ActivityType;
 import ch.hszt.mdp.domain.User;
 import ch.hszt.mdp.service.UserService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:mdp-test-daos.xml" })
 public class StreamControllerTest {
 
 	private MockHttpServletRequest request;
 	private MockHttpServletResponse response;
 	private AnnotationMethodHandlerAdapter adapter;
 	private StreamController streamController;
 	private User user;
 
 	@Autowired
 	private UserService userService;
 
 	@Before
 	public void setup() {
 
 		request = new MockHttpServletRequest();
 		response = new MockHttpServletResponse();
 		adapter = new AnnotationMethodHandlerAdapter();
 		streamController = new StreamController(userService, new DateTime());
 		
 		Principal principal = new Principal() {
 
 			@Override
 			public String getName() {
 				return "gabathuler@gmail.com";
 			}
 		};
 		
 		request.setUserPrincipal(principal);
 		
 		createAndSaveUser();
 	}
 
 	@Test
 	public void testList() {
 		try {
 			request.setMethod("GET");
 			request.setRequestURI("/");
 			
 			ModelAndView mAv = adapter.handle(request, response, streamController);
 
 			assertEquals("stream/list", mAv.getViewName());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	private void createAndSaveUser() {
 		user = new User();
 		user.setEmail("gabathuler@gmail.com");
 		user.setPrename("Cyril");
 		user.setSurname("Gabathuler");
 		user.setPassword("123");
 		user.setRepeat("123");
 		user.setBirthdate(new DateTime(2000, 1, 1, 0, 0, 0, 0));
 		user.setCity("Baden");
 		
 		Activity activity = new Activity();
 		activity.setContent("test");
 		activity.setTime(new DateTime(2000, 1, 1, 0, 0, 0, 0));
 		activity.setTyp(ActivityType.PROFILE);
		activity.setUser(user);
 		
 		user.addActivity(activity);
 		
 		userService.saveUser(user);
 	}
 }
