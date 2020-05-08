 package org.mailoverlord.server.config;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
 
 import javax.mail.Session;
 
 /**
  * When in test mode, just return a Session pointing to the mail overlord smtp server.
  */
 @Configuration
 public class TestMailSessionConfig {
 
     private static final String HOST = "localhost";
     private static final int PORT = 2025;
 
     @Bean
     public Session session() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setPort(PORT);
        javaMailSender.setHost(HOST);
        return javaMailSender.getSession();
     }
 
 }
