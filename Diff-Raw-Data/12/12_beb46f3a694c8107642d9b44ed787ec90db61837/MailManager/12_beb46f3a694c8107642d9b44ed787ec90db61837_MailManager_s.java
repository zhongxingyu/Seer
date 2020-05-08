 package ki.webgame;
 
 import javax.mail.Message;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
 public class MailManager
 {
     private static final String JDBC_NAME = "java:comp/env/mail/webgame";
     
     public static void sendResetPasswordEmail(String username, String email, String code) throws Exception
     {
         Context initCtx = new InitialContext();
         Session session = (Session) initCtx.lookup(JDBC_NAME);
 
         Message message = new MimeMessage(session);
         message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(email)});
         message.setSubject("[www.dragon-rage.com] password change/reset");
         message.setContent(
             "Yourt username is: "+username+"\n\n"
             + "The confirmation code is: "+code+"\n\n"
            + "Click this link to set/reset your password: http://www.dragon-rage.com/passwordreset.html?username="+username+"&code="+code,
             "text/plain");
         Transport.send(message);
     }
 }
