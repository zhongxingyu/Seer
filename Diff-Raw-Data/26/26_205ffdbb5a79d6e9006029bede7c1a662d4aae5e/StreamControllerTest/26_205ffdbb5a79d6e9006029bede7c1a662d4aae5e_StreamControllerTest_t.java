 package ch.hszt.mdp.web;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
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
 
import ch.hszt.mdp.service.ActivityService;

 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:mdp-test-daos.xml" })
 public class StreamControllerTest {
 	
 
 	private MockHttpServletRequest request;
 	private MockHttpServletResponse response;
 	private AnnotationMethodHandlerAdapter adapter;
 	private StreamController streamController;
	
	@Autowired
	private ActivityService activityService;
 
 	@Before
 	public void setup() {
 
 		request = new MockHttpServletRequest();
 		response = new MockHttpServletResponse();
 		adapter = new AnnotationMethodHandlerAdapter();
		streamController = new StreamController(activityService, new DateTime());
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
 
 }
