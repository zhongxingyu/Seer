 package com.jrodeo.example;
 
 import junit.framework.Assert;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Created by IntelliJ IDEA.
  * User: brad_hlista
  */
 
 public class SingleClientIT {
 
 
     static String helloThereURL = "http://localhost:8087/brokerB/helloThere";
     static String putURL = "http://localhost:8087/brokerB/queueA";
     static String leaseURL = "http://localhost:8087/brokerB/queueA";
     static String consumeURL = "http://localhost:8087/brokerB/queueA";
 
     static String msg = "12345678901234567890:";
 
     static HttpClient httpClient = new DefaultHttpClient();
 
     private void deletePath(String path) {
         File f = new File(path);
         f.delete();
     }
 
     @Before
     public void clear() throws Exception {
 
         Properties properties = new Properties();
         properties.load(getClass().getClassLoader().getResourceAsStream("servlet.properties"));
 
         List<String> list = (List<String>) Collections.list(properties.propertyNames());
         for(String propertyName: list) {
             if(propertyName.endsWith(".fullPath")) {
                 deletePath(properties.getProperty(propertyName));
             }
         }
 
     }
 
     @Test
     public void testExpiredLeases() throws Exception {
 
         String result = helloThere();
         Assert.assertTrue(result.startsWith("hello there, time in millis:"));
 
         for (int i = 0; i < 1000; i++) {
             put(msg + i);
         }
 
         Response response;
         long t1 = System.currentTimeMillis();
         for (int i = 0; i < 1000; i++) {
             response = lease(30000, 5000);
             if (i % 2 == 0) {
                 consume(response);
             }
             Assert.assertEquals(msg + i, response.data);
         }
         long t2 = System.currentTimeMillis();
         System.out.println("doing wait: " + (t2-t1));
        Thread.sleep(20000);
         System.out.println("done waiting.");
         for (int i = 0; i < 500; i++) {
             response = lease(5000, 5000);
             consume(response);
         }
         try {
             response = lease(5000, 5000);
             Assert.fail();
         } catch (Exception e) {
             // should get an exception after request timeout, no response
         }
 
 
     }
 
 
     byte[] consumeInputStream(InputStream is) throws IOException {
         byte[] buf = new byte[8192];
         int n;
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
         while ((n = is.read(buf, 0, 8192)) != -1) {
             baos.write(buf, 0, n);
         }
 
         return baos.toByteArray();
     }
 
 
     String helloThere() throws Exception {
         HttpEntity httpEntity = null;
         try {
             HttpGet httpGet = new HttpGet(helloThereURL);
             HttpResponse httpResponse = httpClient.execute(httpGet);
             httpEntity = httpResponse.getEntity();
             InputStream is = httpEntity.getContent();
             try {
                 byte[] bytes = consumeInputStream(is);
                 return new String(bytes);
             } finally {
                 is.close();
             }
         } finally {
             EntityUtils.consume(httpEntity);
         }
     }
 
     void put(String msg) throws Exception {
         HttpEntity httpEntity = null;
         try {
             HttpPost httpPost = new HttpPost(putURL);
             StringEntity se = new StringEntity(msg);
             httpPost.setEntity(se);
             HttpResponse httpResponse = httpClient.execute(httpPost);
             httpEntity = httpResponse.getEntity();
             httpEntity.getContent().close();
         } finally {
             if (httpEntity != null) {
                 EntityUtils.consume(httpEntity);
             }
         }
     }
 
     Response lease(long requestTimeout, long leaseTime) throws Exception {
         HttpEntity httpEntity = null;
         try {
             Response response = null;
             HttpGet httpGet = new HttpGet(leaseURL + "/" + requestTimeout + "/" + leaseTime + "/brad");
             HttpResponse httpResponse = httpClient.execute(httpGet);
             httpEntity = httpResponse.getEntity();
             InputStream is = httpEntity.getContent();
             try {
                 byte[] bytes = consumeInputStream(is);
                 response = new Response(bytes, bytes.length);
                 return response;
             } finally {
                 is.close();
             }
         } finally {
             EntityUtils.consume(httpEntity);
         }
     }
 
     boolean consume(Response response) throws Exception {
         HttpEntity httpEntity = null;
         try {
             long segmentId = response.segmentId;
             long offset = response.offset;
             HttpPut httpPut = new HttpPut(consumeURL + "/" + segmentId + "/" + offset);
             HttpResponse httpResponse = httpClient.execute(httpPut);
             httpEntity = httpResponse.getEntity();
             return httpResponse.getStatusLine().getStatusCode() == 200;
         } finally {
             EntityUtils.consume(httpEntity);
         }
     }
 
 
     /**
      * 4 bytes: size == N bytes
      * 8 bytes: segmentId
      * 8 bytes: offset
      * N-16: remaining bytes
      */
 
     class Response {
 
         long segmentId;
         long offset;
         String data;
 
         public Response(byte[] src, int len) throws Exception {
             ByteBuffer bb = ByteBuffer.wrap(src, 0, len);
             int ttlSize = bb.getInt();
             segmentId = bb.getLong();
             offset = bb.getLong();
             data = decoder.decode(bb).toString();
         }
 
     }
 
 
     static CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
 
 }
