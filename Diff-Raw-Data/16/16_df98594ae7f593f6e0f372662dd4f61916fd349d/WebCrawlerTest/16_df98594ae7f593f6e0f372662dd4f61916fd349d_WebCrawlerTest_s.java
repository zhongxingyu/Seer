 /**
  * 
  */
 package main;
 
 import static org.junit.Assert.*;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author tomAndSimon
  *
  */
 public class WebCrawlerTest {
 	
 	private WebCrawler crawler;
 	private String urlTrue;
 	private String urlFalse;
 	private String simpleUrl;
 	private String testDb;
 
 	@Before
 	public void setUp() throws Exception {
 		crawler = new MockWebCrawler();
 		urlTrue = "http://en.wikipedia.org/wiki/Spider";
 		urlFalse = "http://en.wikipedia.org/wiki/Arachnophobia";
 		simpleUrl = "http://www.york.ac.uk/teaching/cws/wws/webpage1.html";
 		testDb = "WCTest.txt";
 	}

	@Test
	public final void testSearch() {
		assertTrue(crawler.search(urlTrue));
		assertFalse(crawler.search(urlFalse));
	}
 	
 	@After
	public void debug() throws Exception {
 		URL bbc = new URL("http://www.bbc.co.uk");
 		String rel = "www.bbc.co.uk";
 		String sci = "/science";
 		String sci2 = "science";
 		
 		URL url1 = new URL(bbc, rel);
 		URL url2 = new URL(bbc, sci);
 		URL url3 = new URL(bbc, sci2);
 		
 		System.out.println(url1.toString());
 		System.out.println(url2.toString());
 		System.out.println(url3.toString());
 	}
 	
 	@Test
 	public void testCrawlerSimplePage() {
 		crawler.crawl(simpleUrl, testDb);
 		Database myDb = crawler.getDatabase();
 		List<String> priorityZero = myDb.getLinksOfPriority(0);
 		List<String> priorityOne = myDb.getLinksOfPriority(1);
 		List<String> priorityTwo = myDb.getLinksOfPriority(2);
 		assertEquals(1, priorityZero.size());
 		assertEquals(2, priorityOne.size());
 		assertEquals(8, priorityTwo.size());
 	}
 	/*
 	//Just realised it was silly to base a test on a Wikipedia page. Very likely to change its content. Check source if test fails unexpectedly.
 	@Test
 	public final void testCrawlSimpleEmptyFile() {
 	    crawler = new MockWebCrawler(1,20);
 	    crawler.crawl("http://en.wikipedia.org/wiki/Spider","WCTest.txt");
 	    Database myDb = crawler.getDatabase();
 	    Map<Integer, LinkedList<String>> linksToCrawl = myDb.getLinksToCrawl();
 	    LinkedList<String> priorityZero = linksToCrawl.get(0);
 	    LinkedList<String> priorityOne = linksToCrawl.get(1);
 	    assertTrue(priorityZero.size()==1);
 	    assertTrue(priorityZero.get(0).equals("http://en.wikipedia.org/wiki/Spider"));
 	    assertTrue(priorityOne.size()==19);
 	    assertTrue(priorityOne.get(0).equals("http://en.wikipedia.org/wiki/Spider#mw-head"));
 	    assertTrue(priorityOne.get(1).equals("http://en.wikipedia.org/wiki/Spider#p-search"));
 	    assertEquals("http://en.wikipedia.org/wiki/Spider_(disambiguation)", priorityOne.get(2));
 	    assertTrue(priorityOne.get(5).equals("http://en.wikipedia.org/wiki/Cambrian"));
 	    assertFalse(priorityOne.get(7).equals("http://en.wikipedia.org/apple-touch-icon.png"));
 	}
 	
 	@Test
 	public final void testCrawlSimpleNoFile() {
 	    crawler = new MockWebCrawler(1,20);
 	    crawler.crawl("http://en.wikipedia.org/wiki/Spider","blah.txt");
 	    Database myDb = crawler.getDatabase();
 	    Map<Integer, LinkedList<String>> linksToCrawl = myDb.getLinksToCrawl();
 	    LinkedList<String> priorityZero = linksToCrawl.get(0);
 	    LinkedList<String> priorityOne = linksToCrawl.get(1);
 	    assertTrue(priorityZero.size()==1);
 	    assertTrue(priorityZero.get(0).equals("http://en.wikipedia.org/wiki/Spider"));
 	    assertTrue(priorityOne.size()==19);
 	    assertTrue(priorityOne.get(0).equals("http://en.wikipedia.org/wiki/Spider#mw-head"));
 	    assertTrue(priorityOne.get(1).equals("http://en.wikipedia.org/wiki/Spider#p-search"));
 	    assertTrue(priorityOne.get(2).equals("http://en.wikipedia.org/wiki/Spider_(disambiguation)"));
 	    assertTrue(priorityOne.get(5).equals("http://en.wikipedia.org/wiki/Cambrian"));
 	    assertFalse(priorityOne.get(7).equals("http://en.wikipedia.org/apple-touch-icon.png"));
 	}
 	*/
 }
