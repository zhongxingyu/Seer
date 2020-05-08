 package org.eleventhlabs.ncomplo.business.services;
 
 import org.springframework.stereotype.Service;
 
 
 @Service
 public class EmailService {
 
     
     
     public EmailService() {
         super();
     }
     
 
     
     public void sendNewPassword(final String login, final String newPassword) {
        System.out.println(
                "The new password for \"" + login + "\" is: \"" + newPassword + "\"");
         // nothing to do yet
     }
     
     
 }
