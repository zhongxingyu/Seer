 package com.asquera.hmac;
 
 import java.net.URISyntaxException;
 import java.net.URLDecoder;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.crypto.Mac;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.uri.NameValuePair;
 import com.uri.URI;
 
 public class WardenHMacSignerTest {
     WardenHMacSigner signer;
     RequestParams options;
     
     @Before
     public void setUp() throws NoSuchAlgorithmException {
         signer = new WardenHMacSigner();
         options = new RequestParams();
     }
     
     private Map<String, String> toMap(List<NameValuePair> list) {
         Map<String, String> result = new HashMap<String, String>();
         for (NameValuePair pair : list) {
             result.put(pair.key, pair.value);
         }
         return result;
     }
     
     @Test
     public void signUrl() throws URISyntaxException, InvalidKeyException, NoSuchAlgorithmException {
         options.setDate(2012, 06, 19, 14, 36, 10);
         
         String actual = signer.signUrl("http://www.google.com", "SHAREDSECRET", options);
        String expected = "http://www.google.com?auth%5Bdate%5D=Thu%2C%2019%20Jul%202012%2014%3A36%3A10%20GMT";
         Assert.assertTrue(actual.startsWith(expected));
     }
     
     @Test
     public void signUrlAndDecodeResult() throws Exception {
         options.setDate(2012, 06, 19, 14, 36, 10);
         
         String signedUrl  = signer.signUrl("http://www.google.com", "SHAREDSECRET", options);
         String encodedUrl = URLDecoder.decode(signedUrl, "UTF-8");
         String expected   = "http://www.google.com?auth[date]=Thu, 19 Jul 2012 14:36:10 GMT";
         
         Assert.assertTrue(encodedUrl.startsWith(expected));
     }
     
     @Test
     public void signUrlContainsUrlWithoutQuery() throws InvalidKeyException, NoSuchAlgorithmException, URISyntaxException {
         options.setDate(2012, 06, 18, 15, 42, 01);
         options.setNonce("TESTNONCE");
         String url = "http://example.org?foo=temp&bar=void";
         
         String signedUrl = signer.signUrl(url, "SHAREDSECRET", options);
         String[] parts = signedUrl.split("\\?");
         
         Assert.assertEquals("http://example.org", parts[0]);
     }
     
     @Test
     public void signUrlWithQueryParameters() throws InvalidKeyException, NoSuchAlgorithmException, URISyntaxException {
         options.setDate(2012, 06, 18, 15, 42, 01);
         options.setNonce("TESTNONCE");
         
         String signedUrl = signer.signUrl("http://example.org?foo=temp&bar=void", "SHAREDSECRET", options);
         
         URI uri = URI.parse(signedUrl).sortQuery();
         Map<String, String> queries = toMap(uri.queries());
         
         Assert.assertTrue(queries.containsKey("foo"));
         Assert.assertEquals("temp", queries.get("foo"));
         Assert.assertTrue(queries.containsKey("bar"));
         Assert.assertEquals("void", queries.get("bar"));
         
        Assert.assertTrue(queries.containsKey("auth%5Bdate%5D"));
        Assert.assertEquals("Wed%2C%2018%20Jul%202012%2015%3A42%3A01%20GMT", queries.get("auth%5Bdate%5D"));
        Assert.assertTrue(queries.containsKey("auth%5Bnonce%5D"));
        Assert.assertEquals("TESTNONCE", queries.get("auth%5Bnonce%5D"));
        Assert.assertTrue(queries.containsKey("auth%5Bdate%5D"));
     }
     
     @Test
     public void generateSignatureForRequest() throws InvalidKeyException, NoSuchAlgorithmException, URISyntaxException {
         String url = "http://example.org?foo=bar&baz=foobar";
         options.setNonce("TESTNONCE");
         options.setDate(2011, 05, 20, 12, 06, 11);
         
         Mac mac = Mac.getInstance("HmacMD5");
         WardenHMacSigner md5Signer = new WardenHMacSigner(mac);
         Request request = md5Signer.signRequest(url, "secret", options);
 
         Map<String, String> headers = request.headersAsMap();
         Assert.assertTrue(headers.containsKey("date"));
         Assert.assertTrue(headers.containsKey("X-HMAC-Nonce"));
         Assert.assertTrue(headers.containsKey("Authorization"));
         Assert.assertEquals("Mon, 20 Jun 2011 12:06:11 GMT", headers.get("date"));
         Assert.assertEquals("TESTNONCE", headers.get("X-HMAC-Nonce"));
     }
 }
 
