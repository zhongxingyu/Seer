 package de.hpi.fgis.html;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 
 public class WebPageExtractorTest {
 	WebPageExtractor extractor;
 	
 	private String getResource(String uri) throws IOException {
 		InputStream inputStream = WebPageExtractor.class.getResource(uri).openStream();
 		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
 	    StringBuilder content = new StringBuilder();
         String line;
         while ((line = br.readLine()) != null) {
         	if(content.length()>0) {
         		content.append('\n');
         	}
         	content.append(line);
         }
         br.close();
         return content.toString();
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		extractor = new WebPageExtractor();
 	}
 
 	@Test
 	public void testTransformSuccess() throws IOException {
 		String expectedTitle = "HTTP0.9 Summary -- /DesignIssues";
 		String expectedGenerator = "HTML Tidy for Mac OS X (vers 31 October 2006 - Apple Inc. build 13), see www.w3.org";
 		String expectedContentType = "text/html; charset=iso-8859-1";
 		Object expectedText = getResource("http_0.9.txt");
 		String expectedHtml = getResource("http_0.9.html");
 		
 		
 		DBObject data = extractor.transform(new BasicDBObject("url", "http://www.w3.org/DesignIssues/HTTP0.9Summary.html"));
 
 		assertEquals("http://www.w3.org/DesignIssues/HTTP0.9Summary.html", data.get("url"));
 		assertEquals(expectedTitle, ((DBObject)data.get("meta")).get("title"));
 		assertEquals(expectedGenerator, ((DBObject)data.get("meta")).get("generator"));
 		assertEquals(expectedContentType, ((DBObject)data.get("meta")).get("Content-Type"));
 		assertEquals(expectedText, data.get("text"));
 		assertEquals(expectedHtml, data.get("html"));
 		assertNull(data.get("error"));
 	}
 
 	@Test
 	public void testTransformError() throws IOException {
 		DBObject data = extractor.transform(new BasicDBObject("url", "http://cran.r-project.org/doc/manuals/R-intro.pdf"));
 
 		assertEquals("http://cran.r-project.org/doc/manuals/R-intro.pdf", data.get("url"));
 		assertNull(data.get("meta"));
 		assertNull(data.get("text"));
 		assertNull(data.get("html"));
 		assertNotNull(data.get("error"));
 		assertEquals("org.jsoup.UnsupportedMimeTypeException", ((DBObject)data.get("error")).get("type"));
 	}
 
 
 	@Test
 	public void testExtractDataHTTPSpec() throws IOException {
 		String expectedTitle = "HTTP0.9 Summary -- /DesignIssues";
 		String expectedGenerator = "HTML Tidy for Mac OS X (vers 31 October 2006 - Apple Inc. build 13), see www.w3.org";
 		String expectedContentType = "text/html; charset=iso-8859-1";
 		Object expectedText = getResource("http_0.9.txt");
 		String expectedHtml = getResource("http_0.9.html");
 		
 		
 		DBObject data = extractor.extractData("http://www.w3.org/DesignIssues/HTTP0.9Summary.html");
 
 		assertEquals(expectedTitle, ((DBObject)data.get(WebPageExtractor.TMP_META_ATTRIBUTE_NAME)).get("title"));
 		assertEquals(expectedGenerator, ((DBObject)data.get(WebPageExtractor.TMP_META_ATTRIBUTE_NAME)).get("generator"));
 		assertEquals(expectedContentType, ((DBObject)data.get(WebPageExtractor.TMP_META_ATTRIBUTE_NAME)).get("Content-Type"));
 		assertEquals(expectedText, data.get(WebPageExtractor.TMP_TEXT_CONTENT_ATTRIBUTE_NAME));
 		assertEquals(expectedHtml, data.get(WebPageExtractor.TMP_HTML_CONTENT_ATTRIBUTE_NAME));
 	}
 	
 	@Test
 	public void testExtractDataPDF() {
 		DBObject data = extractor.extractData("http://cran.r-project.org/doc/manuals/R-intro.pdf");
 
 		assertNull(data.get(WebPageExtractor.TMP_META_ATTRIBUTE_NAME));
 		assertNull(data.get(WebPageExtractor.TMP_TEXT_CONTENT_ATTRIBUTE_NAME));
 		assertNull(data.get(WebPageExtractor.TMP_HTML_CONTENT_ATTRIBUTE_NAME));
 		assertNotNull(data.get(WebPageExtractor.TMP_ERROR_ATTRIBUTE_NAME));
 		assertEquals("org.jsoup.UnsupportedMimeTypeException", ((DBObject)data.get(WebPageExtractor.TMP_ERROR_ATTRIBUTE_NAME)).get("type"));
 	}
 	
 	@Test
 	public void testExtractDataPNG() {
 		DBObject data = extractor.extractData("http://www.google.com/intl/en_ALL/images/srpr/logo1w.png");
 
 		assertNull(data.get(WebPageExtractor.TMP_META_ATTRIBUTE_NAME));
 		assertNull(data.get(WebPageExtractor.TMP_TEXT_CONTENT_ATTRIBUTE_NAME));
 		assertNull(data.get(WebPageExtractor.TMP_HTML_CONTENT_ATTRIBUTE_NAME));
 		assertNotNull(data.get(WebPageExtractor.TMP_ERROR_ATTRIBUTE_NAME));
 		assertEquals("org.jsoup.UnsupportedMimeTypeException", ((DBObject)data.get(WebPageExtractor.TMP_ERROR_ATTRIBUTE_NAME)).get("type"));
 	}
 	
 	@Test
 	public void testExtractDataWrongURL() {
 		DBObject data = extractor.extractData("http://illegal.web.url/index.html");
 
 		assertNull(data.get(WebPageExtractor.TMP_META_ATTRIBUTE_NAME));
 		assertNull(data.get(WebPageExtractor.TMP_TEXT_CONTENT_ATTRIBUTE_NAME));
 		assertNull(data.get(WebPageExtractor.TMP_HTML_CONTENT_ATTRIBUTE_NAME));
 		assertNotNull(data.get(WebPageExtractor.TMP_ERROR_ATTRIBUTE_NAME));
 		assertEquals("java.net.UnknownHostException", ((DBObject)data.get(WebPageExtractor.TMP_ERROR_ATTRIBUTE_NAME)).get("type"));
 		System.out.println(JSON.serialize(data.get(WebPageExtractor.TMP_ERROR_ATTRIBUTE_NAME)));
 	}
 	
 	@Test(expected = IllegalStateException.class)  
 	public void testExtractDataException() {
		Logger.getLogger(WebPageExtractor.class.getName()).setLevel(Level.OFF);
 		extractor.setIgnoreErrors(false);
 		extractor.extractData("http://illegal.web.url/index.html");
 	}
 }
