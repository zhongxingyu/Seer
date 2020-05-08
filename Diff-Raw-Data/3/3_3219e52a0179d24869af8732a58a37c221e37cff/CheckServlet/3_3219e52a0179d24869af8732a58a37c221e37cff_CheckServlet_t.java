 
 package se.slide.renew;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 import se.slide.renew.entity.Renew;
 import se.slide.renew.entity.Settings;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class CheckServlet extends HttpServlet {
 
     public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
         
         UserService userService = UserServiceFactory.getUserService();
         User user = userService.getCurrentUser();
         
         // Handle authentication
         if (user == null || !userService.isUserLoggedIn() || user.getUserId() == null) {
             return;
         }
         
         String userId = user.getUserId();
 
         Settings settings = ofy().load().type(Settings.class).filter("userId", userId).first().now();
         List<Renew> listOfRenew = ofy().load().type(Renew.class).filter("userId", userId).list();
 
        if (settings == null)
            settings = new Settings();
        
         StringBuilder builder = new StringBuilder();
 
         for (Renew r : listOfRenew) {
 
             if (r.expires != null && r.checked == null) {
                 Date today = new Date();
                 long now = today.getTime();
                 long expires = r.expires.getTime();
                 long subtractedExpiration = Utils.getSubtractedExpiration(r.expires, settings.reminderOption);
 
                 if (now > subtractedExpiration) {
                     Properties props = new Properties();
                     Session session = Session.getDefaultInstance(props, null);
 
                     StringBuilder msgBody = new StringBuilder();
                     msgBody.append("Hi ");
                     msgBody.append(user.getNickname());
                     msgBody.append(",");
                     msgBody.append("\r\n");
                     msgBody.append("\r\n");
                     msgBody.append("Your ");
                     msgBody.append(r.name);
                     msgBody.append(" will expire on ");
                     msgBody.append(r.expires);
                     msgBody.append(".");
                     msgBody.append("\r\n");
                     msgBody.append("\r\n");
                     msgBody.append("\r\n");
                     msgBody.append("Have a good day,");
                     msgBody.append("\r\n");
                     msgBody.append("Your friends at Renew");
 
                     try {
                         Message msg = new MimeMessage(session);
                         msg.setFrom(new InternetAddress("www.slide.se@gmail.com", "Renew"));
                         msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail(), user.getNickname()));
                         msg.setSubject("Renew notification for: " + r.name);
                         msg.setText(msgBody.toString());
                         Transport.send(msg);
                         
                         // set the date when we sent the email
                         r.checked = today;
                         ofy().save().entities(r).now();
 
                     } catch (AddressException e) {
                         mailAdmin(e);
                     } catch (MessagingException e) {
                         mailAdmin(e);
                     }
                 }
             }
 
             // builder.append(r.name);
             // builder.append("\r\n");
         }
 
         resp.setContentType("text/plain");
         resp.getWriter().println("Check completed: " + builder.toString());
 
     }
 
     private void mailAdmin(Exception ex) {
         try {
             Properties props = new Properties();
             Session session = Session.getDefaultInstance(props, null);
 
             Message msg = new MimeMessage(session);
             msg.setFrom(new InternetAddress("www.slide.se@gmail.com", "Renew Admin"));
             msg.addRecipient(Message.RecipientType.TO, new InternetAddress("admins"));
             msg.setSubject("Problem sending email");
             msg.setText(ex.getMessage());
             Transport.send(msg);
 
         } catch (AddressException e) {
             //
         } catch (MessagingException e) {
             //
         } catch (UnsupportedEncodingException e) {
             //
         }
     }
 }
