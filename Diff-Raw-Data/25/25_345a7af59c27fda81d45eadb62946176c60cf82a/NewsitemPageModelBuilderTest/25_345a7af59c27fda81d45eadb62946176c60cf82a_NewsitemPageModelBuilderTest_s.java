 package nz.co.searchwellington.controllers.models;
 
 import junit.framework.TestCase;
 
 import org.springframework.mock.web.MockHttpServletRequest;
 
 public class NewsitemPageModelBuilderTest extends TestCase {
 	
 	public void testIsValid() throws Exception {
		ModelBuilder builder = new NewsitemPageModelBuilder(null);
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.setPathInfo("/wellington-city-council/2010/feb/01/something-about-rates");
 		assertTrue(builder.isValid(request));
 	}
 
 }
