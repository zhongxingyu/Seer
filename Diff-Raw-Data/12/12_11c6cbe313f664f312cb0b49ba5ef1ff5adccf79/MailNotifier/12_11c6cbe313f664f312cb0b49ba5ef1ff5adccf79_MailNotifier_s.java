 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package notifiers;
 
 import play.mvc.*;
 import models.*;
 
 /**
  *
  * @author OpenARS Server API team
  */
 public class MailNotifier extends Mailer {
 
     public static void sendAdminLink(Poll question) {
         setSubject("Your admin link");
         addRecipient(question.email);
        setFrom("OpenARS.dk <no-reply@mailer.openars.dk>");
         System.out.println("question: " + question);
         if (question != null) {
             send(question);
         }
     }
 
     public static void sendPollIDLink(Poll question) {
         setSubject("Your poll link");
         addRecipient(question.email);
        setFrom("OpenARS.dk <no-reply@mailer.openars.dk>");
         System.out.println(question);
         if (question != null) {
             send(question);
         }
     }
 
 }
