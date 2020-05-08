 package nz.co.searchwellington.feeds.rss;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.mockito.Mockito.verify;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 import nz.co.searchwellington.feeds.FeedNewsitemCache;
 import nz.co.searchwellington.feeds.FeedReaderRunner;
 import nz.co.searchwellington.feeds.LiveRssfeedNewsitemService;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.FeedImpl;
 import nz.co.searchwellington.repositories.ConfigDAO;
 import nz.co.searchwellington.repositories.ResourceRepository;
 
 public class RssPrefetcherTest extends TestCase {
 	
 	List<Feed> feeds;
 	
 	ResourceRepository resourceDAO = mock(ResourceRepository.class);
 	LiveRssfeedNewsitemService rssHttpFetcher = mock(LiveRssfeedNewsitemService.class);
 	FeedNewsitemCache rssCache = mock(FeedNewsitemCache.class);
 	ConfigDAO configDAO = mock(ConfigDAO.class);
 	FeedReaderRunner feedReaderRunner = mock(FeedReaderRunner.class);
 	
 	Feed firstFeed;
 	Feed secondFeed;
 	
 	RssNewsitemPrefetcher prefetcher;
 	
 	@Override
 	protected void setUp() throws Exception {
		 prefetcher = new RssNewsitemPrefetcher(resourceDAO, rssHttpFetcher, rssCache, feedReaderRunner, configDAO);			
 		 
 		 firstFeed = new FeedImpl();
 		 firstFeed.setUrl("http://testdata/rss/1");		
 		 
 		 secondFeed = new FeedImpl();
 		 secondFeed.setUrl("http://testdata/rss/2");
 	
 		 feeds = new ArrayList<Feed>();
 		 feeds.add(firstFeed);
 		 feeds.add(secondFeed);
 
 		 when(configDAO.isFeedReadingEnabled()).thenReturn(true);
 		 when(resourceDAO.getAllFeeds()).thenReturn(feeds);
 	}
 	
 	
 	public void testShouldLoadListOfAllFeedsToPrefetch() throws Exception {		
 		prefetcher.run();
 		verify(resourceDAO).getAllFeeds();
 	}
 	
 	public void testShouldFetchAndCacheAllFeeds() throws Exception {		
 		prefetcher.run();
 		verify(rssHttpFetcher).getFeedNewsitems(firstFeed);
 		verify(rssHttpFetcher).getFeedNewsitems(secondFeed);
 		// TODO doesn't verify cache put
 	}
 	
 	public void testShouldPerformFeedReadingRightAfterPrefetching() throws Exception {
 		prefetcher.run();
 		verify(feedReaderRunner).readAllFeeds(feeds);
 	}
 
 }
