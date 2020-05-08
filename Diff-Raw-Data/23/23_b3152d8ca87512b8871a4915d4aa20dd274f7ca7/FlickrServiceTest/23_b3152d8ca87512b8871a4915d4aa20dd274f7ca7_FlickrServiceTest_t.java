 package nz.co.searchwellington.flickr;
 
 import static org.junit.Assert.assertEquals;
 import nz.co.searchwellington.model.Tag;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 
 import uk.co.eelpieconsulting.common.caching.MemcachedCache;
 
 public class FlickrServiceTest {
 
 	private static final int POOL_PHOTO_COUNT = 107;
 	private static final int CACHED_PHOTO_COUNT = 106;
 	private static final String FLICKR_POOL_ID = "12345@N0";
 	private static final String TAG_DISPLAY_NAME = "Trolley Buses";
 
 	@Mock FlickrApi flickrApi;
 	@Mock MemcachedCache cache;
 	
 	private FlickrService flickrService;
 	private Tag tag;
 
 	@Before
 	public void setup() {
 		MockitoAnnotations.initMocks(this);
		flickrService = new FlickrService(flickrApi, cache, FLICKR_POOL_ID);
 		tag = new Tag();
 		tag.setDisplayName(TAG_DISPLAY_NAME);
 	}
 	
 	@Test
 	public void shouldRetrievePhotoCountFromFlickrApiBasedOnTagDisplayNameAndConfiguredPoolId() throws Exception {		
 		Mockito.when(flickrApi.getPoolPhotoCountForTag(TAG_DISPLAY_NAME, FLICKR_POOL_ID)).thenReturn(POOL_PHOTO_COUNT);
 		
 		int flickrPhotoCountFor = flickrService.getFlickrPhotoCountFor(tag);
 		
 		assertEquals(POOL_PHOTO_COUNT, flickrPhotoCountFor);
 	}
 	
 	@Test
 	public void shouldUseCachedCountIfAvailable() throws Exception {
 		Mockito.when(cache.get("flickrphotocount:" + FLICKR_POOL_ID + ":TrolleyBuses")).thenReturn(CACHED_PHOTO_COUNT);
 		
 		int flickrPhotoCountFor = flickrService.getFlickrPhotoCountFor(tag);
 		
 		assertEquals(CACHED_PHOTO_COUNT, flickrPhotoCountFor);
 		Mockito.verifyZeroInteractions(flickrApi);
 	}
 	
 	@Test
 	public void shouldPushFetchedCountsIntoTheCacheForTwentyFourHours() throws Exception {
 		Mockito.when(cache.get("flickrphotocount:" + FLICKR_POOL_ID + ":TrolleyBuses")).thenReturn(null);
 		Mockito.when(flickrApi.getPoolPhotoCountForTag(TAG_DISPLAY_NAME, FLICKR_POOL_ID)).thenReturn(POOL_PHOTO_COUNT);
 
 		flickrService.getFlickrPhotoCountFor(tag);
 		
 		Mockito.verify(cache).put("flickrphotocount:" + FLICKR_POOL_ID + ":TrolleyBuses", 24 * 3600, POOL_PHOTO_COUNT);
 	}
 	
 }
