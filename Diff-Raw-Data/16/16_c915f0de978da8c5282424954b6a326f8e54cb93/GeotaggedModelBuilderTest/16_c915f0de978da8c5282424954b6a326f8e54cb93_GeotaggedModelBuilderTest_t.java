 package nz.co.searchwellington.controllers.models;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import nz.co.searchwellington.controllers.RelatedTagsService;
 import nz.co.searchwellington.controllers.RssUrlBuilder;
 import nz.co.searchwellington.filters.LocationParameterFilter;
 import nz.co.searchwellington.model.Geocode;
 import nz.co.searchwellington.model.frontend.FrontendResource;
 import nz.co.searchwellington.repositories.ContentRetrievalService;
 import nz.co.searchwellington.urls.UrlBuilder;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.web.servlet.ModelAndView;
 
 public class GeotaggedModelBuilderTest {
 	
 	private static final int TOTAL_GEOTAGGED_COUNT = 512;
 	private static final int LOCATION_RESULTS_COUNT = 33;
 	
 	@Mock ContentRetrievalService contentRetrievalService;
 	@Mock UrlBuilder urlBuilder;
 	@Mock RssUrlBuilder rssUrlBuilder;
 	@Mock List<FrontendResource> newsitemsNearPetoneStationFirstPage;
 	@Mock List<FrontendResource> newsitemsNearPetoneStationSecondPage;
 	
 	private MockHttpServletRequest request;
 	private Geocode validLocation;
 	@Mock Geocode invalidLocation;
 	
	private GeotaggedModelBuilder modelBuilder;
 	private RelatedTagsService relatedTagsService;
 	
 	@Before
 	public void setUp() throws Exception {
 		MockitoAnnotations.initMocks(this);
 		request = new MockHttpServletRequest();		
 		validLocation = new Geocode("Petone Station", 1.1, 2.2);
 		modelBuilder = new GeotaggedModelBuilder(contentRetrievalService, urlBuilder, rssUrlBuilder, relatedTagsService);
 	}
 	
 	@Test
 	public void testShouldBeValidForTagCommentPath() throws Exception {
 		request.setPathInfo("/geotagged");
 		assertTrue(modelBuilder.isValid(request));
 	}
 	
 	@Test
 	public void testShouldBeValidForTagCommentRssPath() throws Exception {		
 		request.setPathInfo("/geotagged/rss");
 		assertTrue(modelBuilder.isValid(request));
 	}
 	
 	@Test
 	public void testShouldBeValidForTagCommentJSONPath() throws Exception {		
 		request.setPathInfo("/geotagged/json");
 		assertTrue(modelBuilder.isValid(request));
 	}
 	
 	@Test
 	public void geotaggedNewsitemsPageShouldHavePaginationInformation() throws Exception {
 		request.setPathInfo("/geotagged");				
 		Mockito.when(contentRetrievalService.getGeotaggedCount()).thenReturn(TOTAL_GEOTAGGED_COUNT);
 		ModelAndView modelAndView = modelBuilder.populateContentModel(request);
 		
 		assertEquals(0, modelAndView.getModel().get("page"));
 		assertEquals(TOTAL_GEOTAGGED_COUNT, modelAndView.getModel().get("main_content_total"));
 	}
 	
 	@Test
 	public void locationSearchesShouldHaveNearbyNewsitemsAsTheMainContent() throws Exception {
		Mockito.when(contentRetrievalService.getNewsitemsNear(1.1, 2.2, GeotaggedModelBuilder.HOW_FAR_IS_CLOSE_IN_KILOMETERS, 0, 30)).thenReturn(newsitemsNearPetoneStationFirstPage);
 		request.setPathInfo("/geotagged");
 		request.setAttribute(LocationParameterFilter.LOCATION, validLocation);
 		
 		ModelAndView modelAndView = modelBuilder.populateContentModel(request);
 		
 		assertEquals(newsitemsNearPetoneStationFirstPage, modelAndView.getModel().get("main_content"));
 		assertEquals(validLocation, modelAndView.getModel().get("location"));
 	}
 	
 	@Test
 	public void locationSearchRadiusShouldBeTweakableFromTheRequestParameters() throws Exception {
		Mockito.when(contentRetrievalService.getNewsitemsNear(1.1, 2.2, 3.0, 0, 30)).thenReturn(newsitemsNearPetoneStationFirstPage);
 		request.setPathInfo("/geotagged");
 		request.setAttribute(LocationParameterFilter.LOCATION, validLocation);
 		request.setAttribute(LocationParameterFilter.RADIUS, 3.0);
 		
 		ModelAndView modelAndView = modelBuilder.populateContentModel(request);
 		
 		assertEquals(newsitemsNearPetoneStationFirstPage, modelAndView.getModel().get("main_content"));
 	}
 	
 	@Test
 	public void locationSearchesShouldHavePagination() throws Exception {
 		request.setPathInfo("/geotagged");
		Mockito.when(contentRetrievalService.getNewsitemsNearCount(1.1, 2.2, GeotaggedModelBuilder.HOW_FAR_IS_CLOSE_IN_KILOMETERS)).thenReturn(LOCATION_RESULTS_COUNT);
 		
 		request.setAttribute(LocationParameterFilter.LOCATION, validLocation);
 
 		ModelAndView modelAndView = modelBuilder.populateContentModel(request);
 		
 		assertEquals(0, modelAndView.getModel().get("page"));
 		assertEquals(LOCATION_RESULTS_COUNT, modelAndView.getModel().get("main_content_total"));
 	}
 	
 	@Test
 	public void locationSearchesShouldHaveCorrectContentOnSecondPaginationPage() throws Exception {
 		request.setPathInfo("/geotagged");
		Mockito.when(contentRetrievalService.getNewsitemsNearCount(1.1, 2.2, GeotaggedModelBuilder.HOW_FAR_IS_CLOSE_IN_KILOMETERS)).thenReturn(LOCATION_RESULTS_COUNT);
		Mockito.when(contentRetrievalService.getNewsitemsNear(1.1, 2.2, GeotaggedModelBuilder.HOW_FAR_IS_CLOSE_IN_KILOMETERS, 30, 30)).thenReturn(newsitemsNearPetoneStationSecondPage);
 
 		request.setAttribute(LocationParameterFilter.LOCATION, validLocation);
 		request.setAttribute("page", 2);
 		
 		ModelAndView modelAndView = modelBuilder.populateContentModel(request);
 		
 		assertEquals(2, modelAndView.getModel().get("page"));
 		assertEquals(newsitemsNearPetoneStationSecondPage, modelAndView.getModel().get("main_content"));
 	}
 	
 	@Test
 	public void locationSearchShouldNotSetMainContentIfTheLocationWasInvalid() throws Exception {
 		request.setPathInfo("/geotagged");
 		Mockito.when(invalidLocation.isValid()).thenReturn(false);
 		request.setAttribute(LocationParameterFilter.LOCATION, invalidLocation);
 				
 		ModelAndView modelAndView = modelBuilder.populateContentModel(request);
 
 		assertNull(modelAndView.getModel().get("main_content"));
 		assertNull(modelAndView.getModel().get("location"));
 	}
 	
 }
