 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop_admin.controller;
 
 import dit126.group4.group4shop.core.Users;
 import dit126.group4.group4shop_admin.view.ContactBackingBean;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 /**
  *
  * @author Group4
  */
 @Named("emailController")
 @RequestScoped
 public class EmailController implements Serializable{
     
     @Inject 
     private ContactBackingBean contactBackingBean;
     
     @Inject
     private Group4ShopBean group4shop;
     
     private String host = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("host");
     private String port = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("port");
     private String user = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("user");
     private String pass = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("pass");
    
     private String recipient = "group4@emilbogren.se";
    
     public void sendEmail() throws Exception{
         int userCount = group4shop.getUserRegister().getCount();
         List<Users> users = group4shop.getUserRegister().getRange(0, userCount);
         Properties properties = new Properties();
         properties.put("mail.smtp.host", host);
         properties.put("mail.smtp.port", port);
         properties.put("mail.smtp.auth", "true");
         properties.put("mail.smtp.starttls.enable", "true");
         
         Authenticator auth = new Authenticator() {
             @Override
             public PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication(user, pass);
             }
         };
        
         Session session = Session.getInstance(properties, auth);
         if(users.isEmpty())
             throw new Exception("NO Users");
         for(Users u : users){
             Message msg = new MimeMessage(session);
             try {
                 System.out.println("MAIL TEST!!!!!!!!" + u+ u.getEmail()+user + ' ' + pass);
                 msg.setFrom(new InternetAddress(contactBackingBean.getFrom()));
                 InternetAddress[] toAddresses = { new InternetAddress(u.getEmail()) };
                 InternetAddress[] fromAddresses = { new InternetAddress(contactBackingBean.getFrom()) };
                 msg.setRecipients(Message.RecipientType.TO, toAddresses);
                 msg.setReplyTo(fromAddresses);
                 msg.setSubject(contactBackingBean.getSubject());
                 msg.setSentDate(new Date());
                 msg.setText(contactBackingBean.getContent());
 
                 Transport.send(msg);
             } catch (Exception e) {
                // Catch exception.
             }
         }
     }
 }
