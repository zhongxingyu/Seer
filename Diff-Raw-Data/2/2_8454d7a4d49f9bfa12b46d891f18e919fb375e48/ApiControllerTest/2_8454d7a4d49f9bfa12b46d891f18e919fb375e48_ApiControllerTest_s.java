 package com.dc.web.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.junit.Test;
 
 public class ApiControllerTest {
 
     private static String host = "http://192.168.1.13:9091";
 
     // private static String host = "http://zhaduir.vicp.cc:8092";
 
     // private static String host = "http://127.0.0.1:9091";
 
     public void request(String xml) throws ParseException, IOException {
         HttpClient httpclient = new DefaultHttpClient();
         HttpPost httpPost = new HttpPost(host + "/api/ipad");
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
         nameValuePairs.add(new BasicNameValuePair("psentity", xml));
         httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
         HttpResponse response = httpclient.execute(httpPost);
         System.err.println(EntityUtils.toString(response.getEntity()));
     }
 
     // @Test
     public void testLogin() throws ClientProtocolException, IOException {
         String xml =
                 "<Request action=\"Login\"><Param name=\"Username\">1234</Param><Param name=\"Password\">12312312</Param><Param name=\"MacAddr\"></Param></Request>";
         request(xml);
     }
 
     // @Test
     public void testOpenTable() throws ParseException, IOException {
         String xml =
                 "<Request action=\"OpenTable\" sid=\"997582243409166\"><Param name=\"TableId\">234</Param></Request>";
         request(xml);
     }
 
     // @Test
     public void testGetTables() throws ParseException, IOException {
         String xml = "<Request action=\"GetTables\" sid=\"4805629588930\"/>";
         request(xml);
     }
 
     @Test
     public void testGetMenuList() throws ParseException, IOException {
        String xml = "<Request action=\"GetMenuList\" sid=\"464791886918958\"/>";
         request(xml);
     }
 }
