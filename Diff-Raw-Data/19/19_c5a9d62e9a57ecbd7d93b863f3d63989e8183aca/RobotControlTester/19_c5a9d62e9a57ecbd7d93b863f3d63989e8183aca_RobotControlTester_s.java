 package edu.umn.robotcontrol.test;
 
 import java.io.*;
 import java.net.*;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.*;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class RobotControlTester {
 
   public static void main(String[] args) throws MalformedURLException,
       IOException {
     URLConnection con = new URL(
        "http://localhost:8080/RobotControlServer/control/control/hello")
         .openConnection();
     BufferedReader in = new BufferedReader(new InputStreamReader(
         con.getInputStream()));
     String inputLine;
     while ((inputLine = in.readLine()) != null) {
       System.out.println(inputLine);
     }
     in.close();
 
     HttpClient client = new DefaultHttpClient();
     HttpPost httpPost = new HttpPost(
        "http://localhost:8080/RobotControlServer/control/control/photo");
     byte[] bytes = { 0x01b };
     httpPost.setEntity(new ByteArrayEntity(bytes));
     HttpResponse response = client.execute(httpPost);
     HttpEntity entity = response.getEntity();
     InputStream inStream = entity.getContent();
     // Apache IOUtils makes this pretty easy :)
     System.out.println(IOUtils.toString(inStream));
     inStream.close();
   }
 
 }
