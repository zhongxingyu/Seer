 /**
  * Copyright (C) 2013 Robert Munteanu (robert@lmn.ro)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ro.lmn.presos.di.emailsender.impl.spring;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import ro.lmn.presos.di.emailsender.api.RecipientFinder;
 import ro.lmn.presos.di.emailsender.api.SmtpService;
 import ro.lmn.presos.di.emailsender.api.TextFormatter;
 
 @Configuration
 public class EmailSenderApp {
     
     public static void main(String[] args) {
         ApplicationContext context = new AnnotationConfigApplicationContext(EmailSenderApp.class);
         context.getBean(EmailSender.class).sendMail("Hello there", "Buy cheap $product");
     }
 
     @Bean
     public SmtpService smtpService() {
         return new DebuggingSmtpService();
     }
     
     @Bean
     public RecipientFinder recipientFinder() {
         return new InMemoryRecipientFinder();
     }
     
     @Bean
     public TextFormatter textFormatter() {
         return new DoNothingTextFormatter();
     }
     
     @Bean
     public EmailSender emailSender() {
        
         return new EmailSender();
     }
 }
