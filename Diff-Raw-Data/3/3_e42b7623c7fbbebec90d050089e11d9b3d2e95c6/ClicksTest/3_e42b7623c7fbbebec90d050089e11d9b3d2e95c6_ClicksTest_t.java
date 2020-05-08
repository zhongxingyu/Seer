 package com.rosaloves.bitlyj;
 
 import static com.rosaloves.bitlyj.Bitly.clicks;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Set;
 
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
 		ParameterMap p = Utils.paramsAsMap(clicks("t"));
 		assertTrue(p.size() == 1);
 		assertEquals("t", p.get("hash").get(0));
 	}
 	
 	@Test
 	public void buildWithMultipleHashArguments() {
 		ParameterMap p = Utils.paramsAsMap(clicks("t", "t2"));
 		assertTrue(p.size() == 1);
 		assertEquals(2, p.get("hash").size());
 	}
 	
 	@Test
 	public void buildWithSingleUrlArgument() {
 		ParameterMap p = Utils.paramsAsMap(clicks("http://tcrn.ch/a4MSUH"));
 		assertTrue(p.size() == 1);
 		assertEquals("http://tcrn.ch/a4MSUH", p.get("shortUrl").get(0));
 	}
 	
 	@Test
 	public void buildWithMultipleUrlArguments() {
 		ParameterMap p = Utils.paramsAsMap(clicks("http://foo1", "http://foo2"));
 		assertTrue(p.size() == 1);
 		assertEquals(2, p.get("shortUrl").size());
 	}
 	
 	@Test
 	public void clickResultSums() {
 		UrlClicks clicks = new UrlClicks(new Url(), 1, 2);
 		assertEquals(1, clicks.getUserClicks());
 		assertEquals(2, clicks.getGlobalClicks());
 	}
 	
 	@Test
 	public void clickResultParsing() {
 		UrlClicks clicks = clicks("http://tcrn.ch/a4MSUH").apply(doc);
 		
 		assertEquals(0, clicks.getUserClicks());
 		assertEquals(1105, clicks.getGlobalClicks());
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
