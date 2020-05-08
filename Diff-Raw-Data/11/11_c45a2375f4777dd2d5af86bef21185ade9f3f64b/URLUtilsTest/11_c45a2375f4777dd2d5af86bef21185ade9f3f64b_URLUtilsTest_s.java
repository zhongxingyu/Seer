 package org.jibble.pircbot.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 import org.pircbotx.util.URLUtils;
 
 public class URLUtilsTest {
   @Test
   public void getFirstQueryParam() throws MalformedURLException {
     assertEquals("fjlewi", URLUtils.getQueryParamFirstValue(
         new URL("http://www.youtube.com/something?chipped=32&yu=fjelke&v=fjlewi"), "v"));
     assertEquals("1232", URLUtils.getQueryParamFirstValue(
         new URL("http://www.youtube.com/watch?v=1232&v=fjlewi"), "v"));
     assertEquals("fjlewi", URLUtils.getQueryParamFirstValue(
         new URL("http://www.youtube.com/watch?v=fjlewi&v=1232"), "v"));
 
     assertEquals(null, URLUtils.getQueryParamFirstValue(
         new URL("http://www.youtube.com/something?chipped=32&yu=fjelke&v=fjlewi"), "blah"));
     assertEquals(null, URLUtils.getQueryParamFirstValue(
         new URL("http://www.youtube.com/something"), "blah"));
     assertEquals(null, URLUtils.getQueryParamFirstValue(
         new URL("http://www.youtube.com/p=32"), "p"));
 
     assertEquals("", URLUtils.getQueryParamFirstValue(
         new URL("http://www.youtube.com/something?p="), "p"));
   }
 
   @Test
   public void getQueryParams() throws MalformedURLException {
     Map<String, List<String>> queryParams;
 
     queryParams = URLUtils.getQueryParams(new URL("https://www.google.fr/test"));
     assertTrue(queryParams.isEmpty());
 
     queryParams = URLUtils.getQueryParams(
         new URL("http://www.youtube.com/something?chipped=32&yu=fjelke&v=fjlewi"));
     assertFalse(queryParams.containsKey("blah"));
     assertEquals(1, queryParams.get("chipped").size());
     assertEquals("32", queryParams.get("chipped").get(0));
     assertEquals(1, queryParams.get("yu").size());
     assertEquals("fjelke", queryParams.get("yu").get(0));
     assertEquals(1, queryParams.get("v").size());
     assertEquals("fjlewi", queryParams.get("v").get(0));
 
     queryParams = URLUtils.getQueryParams(
         new URL("https://domack.com/?t=32&yu=dam&t=4fghh6"));
     assertFalse(queryParams.containsKey("blah"));
     assertEquals(1, queryParams.get("yu").size());
     assertEquals("dam", queryParams.get("yu").get(0));
     assertEquals(2, queryParams.get("t").size());
     assertEquals("32", queryParams.get("t").get(0));
     assertEquals("4fghh6", queryParams.get("t").get(1));
   }
 
   @Test
   public void toPublicHTTPURL() throws MalformedURLException, URISyntaxException {
     assertEquals(new URL("http://youtube.com"), URLUtils.toPublicHTTPURL(new URI("youtube.com")));
     assertEquals(new URL("http://youtube.com"), URLUtils.toPublicHTTPURL(new URI("http://youtube.com")));
     assertEquals(new URL("https://youtube.com"), URLUtils.toPublicHTTPURL(new URI("https://youtube.com")));
 
     assertEquals(new URL("http://youtu.be/IFOlx-55"),
         URLUtils.toPublicHTTPURL(new URI("youtu.be/IFOlx-55")));
     assertEquals(new URL("http://co.uk.damnit.co.uk/index.html"),
         URLUtils.toPublicHTTPURL(new URI("co.uk.damnit.co.uk/index.html")));
 
     assertNull(URLUtils.toPublicHTTPURL(new URI("blah")));
     assertNull(URLUtils.toPublicHTTPURL(new URI("boo.helk")));
   }
 }
