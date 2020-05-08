 package cz.cvut.fel.bupro.test.integration;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONObject;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
 import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
 
 import cz.cvut.fel.bupro.dao.ProjectRepository;
 import cz.cvut.fel.bupro.dao.UserRepository;
 import cz.cvut.fel.bupro.model.Project;
 import cz.cvut.fel.bupro.model.User;
 import cz.cvut.fel.bupro.security.SecurityService;
 import cz.cvut.fel.bupro.test.configuration.AbstractControllerTest;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 public class CommentController extends AbstractControllerTest {
 	private final Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	private WebApplicationContext webApplicationContext;
 
 	private MockMvc mockMvc;
 
 	@Autowired
 	private ProjectRepository projectRepository;
 	@Autowired
 	private UserRepository userRepository;
 	@Autowired
 	private SecurityService securityService;
 
 	@Autowired
 	private RequestMappingHandlerAdapter handlerAdapter;
 
 	@Autowired
 	private RequestMappingHandlerMapping handlerMapping;
 
 	@Before
 	public void setup() {
 		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
 	}
 
 	private Project getProjectInstance() {
 		return projectRepository.findAll().iterator().next();
 	}
 
 	private User getUserInstance() {
 		return userRepository.findAll().iterator().next();
 	}
 
 	@Test
 	@Transactional
 	public void testCreateProjectComment() throws Exception {
 		log.info("testCreateProjectComment");
 		User user = securityService.getCurrentUser();
 		String title = "new title";
 		String text = "My great test comment on project";
 		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/comment");
 		request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
 		request.setParameter("type", "project");
 		request.setParameter("id", String.valueOf(getProjectInstance().getId()));
 		request.setParameter("title", title);
 		request.setParameter("text", text);
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		Object handler = handlerMapping.getHandler(request).getHandler();
 		handlerAdapter.handle(request, response, handler);
 		Assert.assertEquals(200, response.getStatus());
 		//Check JSON response
 		JSONObject json = new JSONObject(response.getContentAsString());
 		Assert.assertEquals("My great test comment on project", json.get("text"));
 		Assert.assertEquals(title, json.get("title"));
 		Assert.assertEquals(text, json.get("text"));
 		Assert.assertEquals(user.getFullName(), json.get("author"));
 		Assert.assertNotNull(json.get("creationtime"));
 	}
 
 	@Test
 	@Transactional
 	public void testCreateUserComment() throws Exception {
 		log.info("testCreateUserComment");
 		User user = securityService.getCurrentUser();
 		String title = "new title";
 		String text = "My great test comment on user";
 		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/comment");
 		request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
 		request.setParameter("type", "user");
 		request.setParameter("id", String.valueOf(getUserInstance().getId()));
 		request.setParameter("title", title);
 		request.setParameter("text", text);
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		Object handler = handlerMapping.getHandler(request).getHandler();
 		handlerAdapter.handle(request, response, handler);
 		Assert.assertEquals(200, response.getStatus());
 		//Check JSON response
 		JSONObject json = new JSONObject(response.getContentAsString());
		Assert.assertEquals("My great test comment on project", json.get("text"));
 		Assert.assertEquals(title, json.get("title"));
 		Assert.assertEquals(text, json.get("text"));
 		Assert.assertEquals(user.getFullName(), json.get("author"));
 		Assert.assertNotNull(json.get("creationtime"));
 	}
 
 }
