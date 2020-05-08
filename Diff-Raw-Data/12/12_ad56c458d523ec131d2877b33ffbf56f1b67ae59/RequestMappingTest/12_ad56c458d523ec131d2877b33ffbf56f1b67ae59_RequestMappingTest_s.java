 package net.metadata.auselit.lorestore.servlet;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
 
 
 /**
  * These test cases are to ensure that the @RequestMapping annotations in our controller
  * have been configure properly, and that the correct method will be called.
  * 
  * This may be unnecessary, spring seems to be good at taking care of it all. But it
  * shouldn't do any harm in the case of anything getting mistakenly changed.
  * 
  * Based on:
  * - http://stackoverflow.com/questions/861089/testing-spring-mvc-annotation-mapppings
  * 
  * @author uqdayers
  */
 public class RequestMappingTest {
 	private MockHttpServletRequest request;
 	private MockHttpServletResponse response;
 	private OREController controller;
 	private AnnotationMethodHandlerAdapter adapter;
 	
 	private String exampleURI;
 	private String exampleID;
 	
 	@Before
 	public void setUp() throws Exception {
 		controller = EasyMock.createNiceMock(OREController.class);
 
 		adapter = new AnnotationMethodHandlerAdapter();
 		request = new MockHttpServletRequest();
 		response = new MockHttpServletResponse();
 		
 		exampleURI = "http://example.com/";
 		exampleID = "asdn19";
 	}
 
 	@Test
 	public void plainRetrieve() throws Exception {
 		EasyMock.expect(controller.get(exampleID)).andReturn(null);
 		EasyMock.replay(controller);
 		
 		request.setRequestURI("/" + exampleID);
 		request.setMethod("GET");
 		
 		adapter.handle(request, response, controller);
 		EasyMock.verify(controller);
 	}
 	
 	@Test
 	public void post() throws Exception {
 		EasyMock.expect(controller.post(null)).andReturn(null);
 		EasyMock.replay(controller);
 		
 		request.setRequestURI("/");
 		request.setMethod("POST");
 		
 		adapter.handle(request, response, controller);
 		EasyMock.verify(controller);
 	}
 	
 	@Test
 	public void put() throws Exception {
 		EasyMock.expect(controller.put(exampleID, null)).andReturn(null);
 		EasyMock.replay(controller);
 		
 		request.setRequestURI("/" + exampleID);
 		request.setMethod("PUT");
 		
 		adapter.handle(request, response, controller);
 		EasyMock.verify(controller);
 	}
 	
 	@Test
 	public void delete() throws Exception {
		controller.delete(exampleID);
		EasyMock.expectLastCall();
 		EasyMock.replay(controller);
 		
 		request.setRequestURI("/" + exampleID);
 		request.setMethod("DELETE");
 		
 		adapter.handle(request, response, controller);
 		EasyMock.verify(controller);
 	}
 
 	@Test
 	public void refersToQuery() throws Exception {
 		EasyMock.expect(controller.refersToQuery(exampleURI)).andReturn(null);
 		EasyMock.replay(controller);
 		
 		request.setRequestURI("/");
 		request.setParameter("refersTo", exampleURI);
 		request.setMethod("GET");
 		
 		adapter.handle(request, response, controller);
 		EasyMock.verify(controller);
 	}
 
 	@Test
 	public void searchQuery() throws Exception {
 		EasyMock.expect(controller.searchQuery(exampleURI, "matchpred", "matchval")).andReturn(null);
 		EasyMock.replay(controller);
 		
 		request.setRequestURI("/");
 		request.setParameter("refersTo", exampleURI);
 		request.setParameter("matchpred", "matchpred");
 		request.setParameter("matchval", "matchval");
 		request.setMethod("GET");
 		
 		adapter.handle(request, response, controller);
 		EasyMock.verify(controller);
 	}
 
 	@Test
 	public void exploreQuery() throws Exception {
 		EasyMock.expect(controller.exploreQuery(exampleURI)).andReturn(null);
 		EasyMock.replay(controller);
 		
 		request.setRequestURI("/");
 		request.setParameter("exploreFrom", exampleURI);
 		request.setMethod("GET");
 		
 		adapter.handle(request, response, controller);
 		EasyMock.verify(controller);
 	}
 	
 	@Test
 	public void keywordSearchQuery() throws Exception {
 		EasyMock.expect(controller.keywordSearch("matchval")).andReturn(null);
 		EasyMock.replay(controller);
 		
 		request.setRequestURI("/");
 		request.setParameter("matchval", "matchval");
 		request.setMethod("GET");
 		
 		adapter.handle(request, response, controller);
 		EasyMock.verify(controller);
 	}	
 }
