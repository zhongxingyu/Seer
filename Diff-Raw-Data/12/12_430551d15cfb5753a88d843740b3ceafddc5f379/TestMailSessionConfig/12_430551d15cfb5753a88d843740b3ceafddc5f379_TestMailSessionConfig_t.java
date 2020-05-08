 package org.mailoverlord.server.config;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import javax.mail.Session;
import java.util.Properties;
 
 /**
  * When in test mode, just return a Session pointing to the mail overlord smtp server.
  */
 @Configuration
 public class TestMailSessionConfig {
 
     private static final String HOST = "localhost";
     private static final int PORT = 2025;
 
     @Bean
     public Session session() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", HOST);
        properties.put("mail.smtp.port", PORT);
        return Session.getDefaultInstance(properties);
     }
 
 }
