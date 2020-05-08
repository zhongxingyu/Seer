 package controllers;
 
 import play.*;
 import play.data.validation.Email;
 import play.data.validation.Required;
 import play.mvc.*;
 
 import java.util.*;
 
 public class Application extends Controller {
 
     public static void index(Boolean header,String tab) {
         if(request.headers.get("user-agent")!= null){
             String ua = request.headers.get("user-agent").toString().toLowerCase();
             Boolean isMobile = ua.contains("android");
            if(!isMobile){
                 renderTemplate("application/mobile.html");
             }
         };
         render(header,tab);
     }
     
     public static void sendMessage(@Required String firstname, @Required String lastname, @Required String email, String phone, @Required String message){
     	
         if (validation.hasErrors()) {
             validation.keep();
             params.flash();
             flash.error("Bazı alanları boş bıraktın?");
             index(true,"iletisim");            
         }   	
     	try {
 			Mails.sendFeedbackMail(firstname, lastname, email, phone, message);
 			flash.success("Mesajın gönderildi");
 			index(true,"");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}    	
     	
     }
 
 }
