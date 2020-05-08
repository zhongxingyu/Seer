 package com.rosaloves.bitlyj;
 
 import static com.rosaloves.bitlyj.Bitly.clicks;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Set;
 
import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 /**
  * ClicksTest
  * 
  * $Id$
  * 
  * @author clewis Jul 17, 2010
  *
  */
 public class ClicksTest {
 	Document doc;
 	
 	@Before
 	public void before() {
 		doc = Utils.classpathXmlIS("clicks_http_tcrn.xml");
 	}
 	
 	@Test
 	public void name() {
 		assertEquals("clicks", clicks("http://tcrn.ch/a4MSUH").getName());
 	}
 
 	@Test
 	public void buildWithSingleHashArgument() {
 		BitlyMethod<UrlClicks> url = clicks("t");
 		assertTrue(url.getParameters().size() == 1);
 		assertEquals("t", url.getParameters().get("hash").get(0));
 	}
 	
 	@Test
 	public void buildWithMultipleHashArguments() {
 		BitlyMethod<Set<UrlClicks>> url = clicks("t", "t2");
 		assertTrue(url.getParameters().size() == 1);
 		assertEquals(2, url.getParameters().get("hash").size());
 		System.out.println("ClicksTest.buildWithMultipleUrlArguments() " + Bitly.as("rosaloves", "R_63b4916408e824a93ad0da40558bb410").getUrlForCall(url));
 	}
 	
 	@Test
 	public void buildWithSingleUrlArgument() {
 		BitlyMethod<UrlClicks> url = clicks("http://tcrn.ch/a4MSUH");
 		assertTrue(url.getParameters().size() == 1);
 		assertEquals("http://tcrn.ch/a4MSUH", url.getParameters().get("shortUrl").get(0));
 	}
 	
 	@Test
 	public void buildWithMultipleUrlArguments() {
 		BitlyMethod<Set<UrlClicks>> url = clicks("http://foo1", "http://foo2");
 		
 		assertTrue(url.getParameters().size() == 1);
 		assertEquals(2, url.getParameters().get("shortUrl").size());
 	}
 	
 	@Test
 	public void clickResultSums() {
 		UrlClicks clicks = new UrlClicks(new Url(), 1, 2);
 		assertEquals(1, clicks.getUserClicks());
 		assertEquals(2, clicks.getGlobalClicks());
 		assertEquals(3, clicks.getTotalClicks());
 	}
 	
 	@Test
 	public void clickResultParsing() {
 		UrlClicks clicks = clicks("http://tcrn.ch/a4MSUH").apply(doc);
 		
 		assertEquals(0, clicks.getUserClicks());
 		assertEquals(1105, clicks.getGlobalClicks());
 		assertEquals(1105, clicks.getTotalClicks());
 	}
 	
 	@Test
 	public void urlResultParsing() {
 		Url url = clicks("http://tcrn.ch/a4MSUH").apply(doc).getUrl();
 		
 		assertEquals("http://tcrn.ch/a4MSUH", url.getShortUrl());
 		assertEquals("bWw49z", url.getGlobalHash());
 		assertEquals("a4MSUH", url.getUserHash());
 	}
	
	@Test
	public void multipleUrlResultParsing() {
		doc = Utils.classpathXmlIS("clicks_2_urls.xml");
		Set<UrlClicks> clicks = clicks("http://tcrn.ch/a4MSUH", "http://bit.ly/1YKMfY").apply(doc);
		
		assertEquals(2, clicks.size());
	}
	
	@Test
	public void multipleHashResultParsing() {
		doc = Utils.classpathXmlIS("clicks_2_urls.xml");
		Set<UrlClicks> clicks = clicks("bWw49z", "1YKMfY").apply(doc);
		
		assertEquals(2, clicks.size());
	}
 }
