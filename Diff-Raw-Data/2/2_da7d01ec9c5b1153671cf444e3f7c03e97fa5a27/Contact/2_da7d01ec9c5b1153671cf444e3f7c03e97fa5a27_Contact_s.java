 package controllers;
 
 import com.webube.utils.UbeHtml;
 import com.webube.utils.UbeMail;
 import org.apache.commons.codec.net.URLCodec;
 import org.apache.commons.mail.Email;
 import org.apache.commons.mail.EmailException;
 import play.Logger;
 
 import play.libs.Mail;
 import play.libs.WS;
 import play.mvc.Controller;
 
 import java.net.URL;
 
 /**
  * Created by IntelliJ IDEA.
  * User: guillaumebadin
  * Date: 12/01/12
  * Time: 20:54
  * To change this template use File | Settings | File Templates.
  */
 public class Contact extends Controller {
 
 
     public static void sendMessage( String name,
                                    String email,
                                    String message) {
 
 
         Logger.info("[SendMessage]" + UbeHtml.decode(name + " : " +  email + ":" + message) );
         
         try 
         {
        UbeMail.createNewEmail(UbeHtml.decode(name),UbeHtml.decode(message), UbeHtml.decode(email),"contact@webube.com").send();
         }
         catch (Exception e)
         {
             Logger.error(e.toString());
         }
 
 
         if (request.isAjax())
             renderText("Mail Sent");
         else
             Application.contact();
     }
 }
