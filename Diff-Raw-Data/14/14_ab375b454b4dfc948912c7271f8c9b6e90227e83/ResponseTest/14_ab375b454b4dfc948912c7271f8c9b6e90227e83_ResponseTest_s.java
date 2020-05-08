 package com.server.Responses;
 
 import com.server.HtmlGenerator;
 import com.server.Responses.Response;
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 public class ResponseTest {
     Response resp = new Response();
     String body;
 
     @Before public void init() {
         body = HtmlGenerator.echoBody("Tonarino Totoro");
     }
 
     @Test public void testWrite() throws IOException {
         Hashtable<String, Object> req = new Hashtable<String, Object>();
         Hashtable<String, Object> header = new Hashtable<String, Object>();
 
         req.put("status-line", "HTTP/1.0 200 OK");
         req.put("message-header", header);
         req.put("message-body", body.getBytes(Charset.forName("utf-8")));
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         resp.writeTo(req, out);
         assertEquals("HTTP/1.0 200 OK\r\n\r\n<html><head><title></title></head><body> Tonarino Totoro </body></html>", out.toString());
     }
 
     @Test public void writeContainsMessageHeader() throws IOException {
         Hashtable<String, Object> req = new Hashtable<String, Object>();
         Hashtable<String, Object> header = new Hashtable<String, Object>();
 
         header.put("Content-Type", "text/html");
         req.put("status-line", "HTTP/1.0 200 OK");
         req.put("message-header", header);
         req.put("message-body", body.getBytes(Charset.forName("utf-8")));
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         resp.writeTo(req, out);
         assertEquals("HTTP/1.0 200 OK\r\nContent-Type: text/html\r\n\r\n<html><head><title></title></head><body> Tonarino Totoro </body></html>", out.toString());
     }
 
     @Test public void responseHasCookie() throws IOException {
         Hashtable<String, Object> req = new Hashtable<String, Object>();
         Hashtable<String, Object> header = new Hashtable<String, Object>();
         List<String> cookies = new ArrayList<String>();
         cookies.add("hello=world; path=/");
         header.put("Set-Cookie", cookies);
 
         header.put("Content-Type", "text/html");
         req.put("status-line", "HTTP/1.0 200 OK");
         req.put("message-header", header);
         req.put("message-body", body.getBytes(Charset.forName("utf-8")));
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         resp.writeTo(req, out);
         Assert.assertTrue(out.toString().contains("Set-Cookie: "));
         Assert.assertTrue(out.toString().contains("hello=world; path=/"));
     }
 
     @Test public void responseHasMultipleSetCookies() throws IOException {
         Hashtable<String, Object> req = new Hashtable<String, Object>();
         Hashtable<String, Object> header = new Hashtable<String, Object>();
         List<String> cookies = new ArrayList<String>();
         cookies.add("hello=world; path=/");
         cookies.add("taka=goto; path=/");
         cookies.add("board=blah; path=/");
         header.put("Set-Cookie", cookies);
 
         header.put("Content-Type", "text/html");
         req.put("status-line", "HTTP/1.0 200 OK");
         req.put("message-header", header);
         req.put("message-body", body.getBytes(Charset.forName("utf-8")));
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         resp.writeTo(req, out);
        Assert.assertTrue(out.toString().contains("Set-Cookie: hello=world; path=/"));
        Assert.assertTrue(out.toString().contains("Set-Cookie: taka=goto; path=/"));
        Assert.assertTrue(out.toString().contains("Set-Cookie: board=blah; path=/"));
     }
 }
