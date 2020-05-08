 package pl.softwaremill.jozijug.joziawsdemo.tools;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URIUtils;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: szimano
  */
 public class LoadGenerator {
     public static void main(String[] args) throws URISyntaxException, IOException {
 
         HttpClient client = new DefaultHttpClient();
 
         int i = 0;
 
         while (true) {
             String content = "Some content " + i++;
 
             List<NameValuePair> qparams = new ArrayList<NameValuePair>();
             qparams.add(new BasicNameValuePair("room", "room"));
             qparams.add(new BasicNameValuePair("content", content));
 
             System.out.println("Sending message " + content);
 
            URI uri = URIUtils.createURI("http", "localhost", 8080, "/jozi-aws-demo-1.0-SNAPSHOT/home/addMessageByGet",
                     URLEncodedUtils.format(qparams, "UTF-8"), null);
             HttpGet get = new HttpGet(uri);
 
             HttpResponse response = client.execute(get);
 
             EntityUtils.consume(response.getEntity());
 
             try {
                 Thread.sleep(500);
             } catch (InterruptedException e) {
                 //
             }
         }
     }
 }
