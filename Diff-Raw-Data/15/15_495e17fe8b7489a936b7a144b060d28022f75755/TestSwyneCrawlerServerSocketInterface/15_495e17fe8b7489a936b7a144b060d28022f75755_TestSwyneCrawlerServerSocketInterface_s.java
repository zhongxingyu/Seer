 package edu.iit.swyne.crawler.test;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import junit.framework.TestCase;
 import edu.iit.swyne.crawler.client.SwyneCrawlerServerClient;
 import edu.iit.swyne.crawler.server.CrawlerServer;
 import edu.iit.swyne.crawler.server.SwyneCrawlerServer;
 
 public class TestSwyneCrawlerServerSocketInterface extends TestCase {
 	
 	private static final String COLLECTION = "LATimes";
	private static final String EXTRACTOR_CLASS = "edu.iit.swyne.crawler.LATimesExtractor";
 	
 	private SwyneCrawlerServerClient client;
 	private URL feedURL;
 	private CrawlerServer server;
 	private String addDirective;
 	
 	public TestSwyneCrawlerServerSocketInterface() throws MalformedURLException {
 		feedURL = new URL("http://omega.cs.iit.edu/~orawling/iproTesting/news.rss");
 		client = new SwyneCrawlerServerClient("localhost", 6970);
 		addDirective = "add "+feedURL.toString()+" "+COLLECTION+" "+EXTRACTOR_CLASS;
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		server = new SwyneCrawlerServer();
 		server.startServer();
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		server.stopServer();
 	}
 
 	public void testAddFeed() throws Exception {
 		client.setCommand(addDirective);
 		assertTrue(server.isRunning());
 		client.run();
 		assertTrue(server.getCrawler().isTrackingFeed(feedURL));
 	}
 	
 	public void testShutdown() throws Exception {
 		client.setCommand("shutdown");
 		assertTrue(server.isRunning());
 		client.run();
 		assertFalse(server.isRunning());
 	}
 	
 	public void testRemoveFeed() throws Exception {
 		client.setCommand(addDirective);
 		assertTrue(server.isRunning());
 		client.run();
 		assertTrue(server.getCrawler().isTrackingFeed(feedURL));
 		
 		client.setCommand("remove "+feedURL.toString());
 		assertTrue(server.isRunning());
 		client.run();
 		assertFalse(server.getCrawler().isTrackingFeed(feedURL));
 	}
 }
