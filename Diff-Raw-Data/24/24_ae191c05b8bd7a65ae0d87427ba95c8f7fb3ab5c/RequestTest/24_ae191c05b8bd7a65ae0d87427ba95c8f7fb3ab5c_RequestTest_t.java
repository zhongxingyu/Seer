 package com.asquera.hmac;
 
 import java.net.URISyntaxException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class RequestTest {
     
     private RequestParams options;
     
     @Before
     public void setUp() {
         options = new RequestParams();
     }
     
     @Test
     public void constructorThrowsExceptionWhenMapIsNull() throws URISyntaxException {
         try {
             new Request("http://www.example.com", null);
             Assert.fail("Must throw exception when map is null");
         } catch (IllegalArgumentException e) {
         }
     }
     
     @Test
     public void constructorThrowsExceptionWhenNoUrl() throws URISyntaxException {
         try {
             new Request(null, options);
             Assert.fail("Must throw exception when url is null");
         } catch (IllegalArgumentException e) {
         }
     }
     
     @Test
     public void pathFromUrlWithoutQuery() throws URISyntaxException {
         Request info = new Request("http://www.example.com", options);
         String actualPath = info.path();
         Assert.assertEquals("/", actualPath);
     }
     
     @Test
     public void pathFromUrlWithPath() throws URISyntaxException {
         Request info = new Request("http://www.example.com/images/test.png", options);
         String actualPath = info.path();
         Assert.assertEquals("/images/test.png", actualPath);
     }
     
     @Test
     public void pathFromUrlWithQuery() throws URISyntaxException {
         Request info = new Request("http://www.test.com/blog?key=value&test=true", options);
         String actualPath = info.path();
         Assert.assertEquals("/blog", actualPath);
     }
     
     @Test
     public void dateAsDateGetsFormatted() throws Exception {
         Calendar calendar = new GregorianCalendar();
         calendar.set(2012, 06, 17, 10, 20, 30);
         Date date = calendar.getTime();
         options.setDate(date);
         
         Request request = new Request("http://example.com", options);
         String actualDate = request.dateAsString();
         Assert.assertEquals("Tue, 17 Jul 2012 10:20:30 GMT", actualDate);
     }
     
     
     @Test
     public void isNotQueryBasedWithDefaultOption() throws URISyntaxException {
         Request request = new Request("http://www.example.com", options);
         Assert.assertFalse(request.isQueryBased());
     }
     
     @Test
     public void isNotQueryBasedWhenOptionIsFalse() throws URISyntaxException {
         options.setQueryBased(false);
         Request request = new Request("http://www.example.com", options);
         Assert.assertFalse(request.isQueryBased());
     }
     
     @Test
     public void isQueryBasedWhenOptionIsTrue() throws URISyntaxException {
         options.setQueryBased(true);
         Request request = new Request("http://www.example.com", options);
         Assert.assertTrue(request.isQueryBased());
     }
     
     @Test
    public void returnUrlWithoutQueryParameters() throws URISyntaxException {
        options.setQueryBased(false);
        Request request = new Request("http://www.example.com", options);
        Assert.assertEquals("http://www.example.com", request.url());
    }
    
    @Test
     public void canonicalRepresentationOfDefaultParams() throws URISyntaxException {
         RequestParams options = new RequestParams();
         options.setDate(2012, 06, 18, 19, 14, 20);
         Request request = new Request("http://www.example.com", options);
         
         String actual = request.canonicalRepresentation();
         String expected = "GET\ndate:Wed, 18 Jul 2012 19:14:20 GMT\nnonce:\n/";
         Assert.assertEquals(expected, actual);
     }
     
     @Test
     public void canonicalRepresentationWithNonce() throws URISyntaxException {
         RequestParams options = new RequestParams();
         options.setDate(2011, 05, 20, 12, 6, 11);
         options.setNonce("TESTNONCE");
         Request request = new Request("/example", options);
         request.query().add("foo", "bar");
         request.query().add("baz", "foobared");
         
         String actual = request.canonicalRepresentation();
         String expected = "GET\ndate:Mon, 20 Jun 2011 12:06:11 GMT\nnonce:TESTNONCE\n/example?baz=foobared&foo=bar";
         Assert.assertEquals(expected, actual);
     }
     
     @Test
     public void canonicalRepresentationWithHeaders() throws URISyntaxException {
         RequestParams options = new RequestParams();
         options.setDate(2011, 05, 20, 12, 06, 11);
         options.setNonce("TESTNONCE");
         options.setQueryBased(false);
         options.addHeader("Content-Type", "application/json;charset=utf8");
         options.addHeader("Content-MD5", "d41d8cd98f00b204e9800998ecf8427e");
         
         Request request = new Request("/example", options);
         request.query().add("foo", "bar");
         request.query().add("baz", "foobared");
         
         String actual = request.canonicalRepresentation();
         String expected = 
                 "GET\n" +
                 "date:Mon, 20 Jun 2011 12:06:11 GMT\n" +
                 "nonce:TESTNONCE\n" +
                 "content-md5:d41d8cd98f00b204e9800998ecf8427e\n" +
                 "content-type:application/json;charset=utf8\n" +
                 "/example?baz=foobared&foo=bar";
         Assert.assertEquals(expected, actual);
     }
 }
 
