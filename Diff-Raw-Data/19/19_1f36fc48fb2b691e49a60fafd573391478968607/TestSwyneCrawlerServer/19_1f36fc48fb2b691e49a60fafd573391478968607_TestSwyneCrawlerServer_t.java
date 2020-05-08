 package edu.iit.swyne.crawler.test;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.InvalidPropertiesFormatException;
 
 import junit.framework.TestCase;
 import edu.iit.swyne.crawler.server.SwyneCrawlerServer;
 import edu.iit.swyne.crawler.server.SwyneCrawlerServer.FeedAlreadyTrackedException;
 
 public class TestSwyneCrawlerServer extends TestCase {
 	private URL feedURL;
//	private Properties testProps = new Properties();
 	private SwyneCrawlerServer server;
 	
 	public TestSwyneCrawlerServer() throws InvalidPropertiesFormatException, FileNotFoundException, IOException{
 		feedURL = new URL("http://omega.cs.iit.edu/~orawling/iproTesting/news.rss");
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
		server = new SwyneCrawlerServer();
 		server.init();
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		server.shutdown();
 	}
 	
 	public void testStartAndStopServer() throws Exception {
 		assertFalse(server.isRunning());
 		server.start();
 		assertTrue(server.isRunning());
 		server.shutdown();
 		assertFalse(server.isRunning());
 	}
 	
 	public void testAddFeedBeforeStartingServer() throws Exception {
 		// The server should start up if a request to add a feed has been made
 		assertFalse(server.isRunning());
 		server.addFeed(feedURL);
 		assertTrue(server.isRunning());
 	}
 	
 	public void testAddFeed() throws Exception {
 		server.start();
 		server.addFeed(feedURL);
 		assertEquals(1, server.numFeedsTracking());
 		assertTrue(server.isTrackingFeed(feedURL));
 	}
 	
 	public void testAddFeedTwice() throws Exception {
 		server.start();
 		server.addFeed(feedURL);
 		try {
 			server.addFeed(feedURL);
 			fail();
 		}
 		catch(FeedAlreadyTrackedException e) {
 			assertNotNull(e.getMessage());
 		}
 		assertEquals(1, server.numFeedsTracking());
 		assertTrue(server.isTrackingFeed(feedURL));
 	}
 	
 	public void testRemoveFeed() throws Exception {
 		server.start();
 		server.addFeed(feedURL);
 		assertEquals(1, server.numFeedsTracking());
 		assertTrue(server.isTrackingFeed(feedURL));
 		server.removeFeed(feedURL);
 		assertEquals(0, server.numFeedsTracking());
 		assertFalse(server.isTrackingFeed(feedURL));
 	}
 }
