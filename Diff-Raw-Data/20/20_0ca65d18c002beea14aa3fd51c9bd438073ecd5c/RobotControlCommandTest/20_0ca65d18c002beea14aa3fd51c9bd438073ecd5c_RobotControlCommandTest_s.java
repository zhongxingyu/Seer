 package edu.umn.robotcontrol.test;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Properties;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.google.gson.Gson;
 
 import edu.umn.robotcontrol.domain.RobotCommand;
 
 public class RobotControlCommandTest {
   public static void main(String[] args) throws ClientProtocolException,
       IOException, URISyntaxException {
     HttpClient client = new DefaultHttpClient();
     URL propsURL = ClassLoader.getSystemResource("robotcontrol.properties");
     File propsFile = new File(propsURL.toURI());
     FileInputStream fis = new FileInputStream(propsFile);
     Properties props = new Properties();
     props.load(fis);
     fis.close();
     String baseurl =  props.getProperty("baseurl");
     HttpPost httpPost = new HttpPost(
         baseurl + "/rest/control/command");
     RobotCommand rc = new RobotCommand();
     rc.setComponent(1);
     rc.setValue(45);
     rc.setIssueTime(System.currentTimeMillis());
 
     Gson gson = new Gson();
     String json = gson.toJson(rc);
     System.out.println(json);
 
     httpPost.setEntity(new StringEntity(json));
    httpPost.setHeader("Accept", "text/html");
     httpPost.setHeader("Content-Type", "application/json");
     HttpResponse response = client.execute(httpPost);
     HttpEntity entity = response.getEntity();
     InputStream inStream = entity.getContent();
     // Apache IOUtils makes this pretty easy :)
     System.out.println(IOUtils.toString(inStream));
     inStream.close();
   }
 }
