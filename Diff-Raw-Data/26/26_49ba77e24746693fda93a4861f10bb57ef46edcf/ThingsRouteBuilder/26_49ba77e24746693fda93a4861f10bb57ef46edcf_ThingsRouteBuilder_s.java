 package br.com.globalcode;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
 import org.apache.camel.builder.RouteBuilder;
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
 
        // tweet what is coming from the file (commands for Things API)
        from("file:src/data")
                .convertBodyTo(String.class)
                .to("twitter://timeline/user");
        
        // receive commands (mentions)
         from("twitter://timeline/mentions")
                 .process(new ThingsProcessor());
     }
 
     private void initTwitter() {
         TwitterComponent tc = new TwitterComponent();
         getContext().addComponent("twitter", tc);
 
         Properties p = new Properties();

         try (InputStream is = getClass().getResourceAsStream("/twitter.properties")) {
             p.load(is);
        } catch(IOException i) {
         }
 
        tc.setAccessToken("");
        tc.setAccessTokenSecret("");
        tc.setConsumerKey("");
        tc.setConsumerSecret("");
     }
 }
