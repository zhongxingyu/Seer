 package br.com.globalcode;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
 import org.apache.camel.component.twitter.TwitterComponent;
 
 /**
  * A Camel Java DSL Router
  */
 public class ThingsRouteBuilder extends RouteBuilder {
 
     /**
      * Let's configure the Camel routing rules using Java code...
      */
     public void configure() {
         initTwitter();
 
        // receive commands (mentions of @ThingsTwitter)
         from("twitter://timeline/mentions")
                 .process(new ThingsProcessor());
     }
 
     private void initTwitter() {
         TwitterComponent tc = new TwitterComponent();
         getContext().addComponent("twitter", tc);
 
         Properties p = new Properties();
         try (InputStream is = getClass().getResourceAsStream("/twitter.properties")) {
             p.load(is);
        } catch (IOException i) {
         }
 
        tc.setAccessToken(p.getProperty("accessToken"));
        tc.setAccessTokenSecret(p.getProperty("accessTokenSecret"));
        tc.setConsumerKey(p.getProperty("consumerKey"));
        tc.setConsumerSecret(p.getProperty("consumerSecret"));
     }
 }
