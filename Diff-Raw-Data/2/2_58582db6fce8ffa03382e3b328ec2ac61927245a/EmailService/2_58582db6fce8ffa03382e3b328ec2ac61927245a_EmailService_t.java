 package com.bia.monitor.email;
 
 /**
  *
  * @author intesar
  */
 public interface EmailService {
 
     String SMTP_HOST_NAME = "smtp.gmail.com";
     String SMTP_PORT = "465";
     String EMAIL_FROM_ADDRESS = "team@zytoon.me";
     String SEND_FROM_USERNAME = "team@zytoon.me";
    String SEND_FROM_PASSWORD = "";
     String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
     String EMAIL_CONTENT_TYPE = "text/html";
     String EMAIL_SIGNATURE = "<br/>"
             + "Thanks, <br/>"
             + "Zytoon.me Team";
 
     /**
      * 
      * @param toAddress
      * @param car
      * @param comment
      */
     void sendEmail(String toAddress, String subject, String body);
     
 }
