 package nz.co.searchwellington.controllers.models;
 
 import junit.framework.TestCase;
 
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;
import nz.co.searchwellington.widgets.TagWidgetFactory;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 public class NewsitemPageModelBuilderTest extends TestCase {
 	
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock TaggingReturnsOfficerService taggingReturnsOfficerService;
	@Mock TagWidgetFactory tagWidgetFactory;
	@Mock HandTaggingDAO handTaggingDAO;
	@Mock LoggedInUserFilter loggedInUserFilter;
		
	@Override
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
 	public void testIsValid() throws Exception {
		ModelBuilder builder = new NewsitemPageModelBuilder(contentRetrievalService, taggingReturnsOfficerService, tagWidgetFactory, handTaggingDAO, loggedInUserFilter);
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.setPathInfo("/wellington-city-council/2010/feb/01/something-about-rates");
 		assertTrue(builder.isValid(request));
 	}
 
 }
