 package controllers;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 import play.Logger;
 import play.libs.Mail;
 import play.mvc.*;
 
 
 public class Application extends Controller {
 
     @Before(unless={"index"})
     static void setPageLevel2() {
         renderArgs.put("level", 2);
     }
 
     public static void index() { render();}
     public static void sysadmin() { render(); }
     public static void webdev() { render(); }
     public static void analysis() { render();}
     public static void about() { render();}
 
     /**
      * Handles the form input from the contact us page.
      */
     public static void sendMessage() {
         String   name      = params.get("name");
         String   address   = params.get("email");
         String   message   = params.get("message");
 
         SimpleEmail email = new SimpleEmail();
         try {
             email.setFrom("dan@lightcastletech.com");
             email.addTo("tamara@lightcastletech.com");
             email.addTo("dan@lightcastletech.com");
             email.setSubject("someone filled out the form on our website.");
             email.setMsg("Email: " + address + "\n" +
                          "Name: " + name + "\n" +
                          "Message: " + message );
         } catch(EmailException e) {
             Logger.error(e, "Failed to create email!");
         }
         Mail.send(email);
     }
 
 }
