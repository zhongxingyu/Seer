 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mail;
 
 import com.model.entity.Usuario;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import javax.mail.Authenticator;
 import javax.mail.Message;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 /**
  *
  * @author Usuario
  */
 public class Mail {
     public static final String SUCCESS_TEMPLATE = "<!DOCTYPE html>"
             + "<html lang=\"pt-br\">"
             + "<head>"
             + "<meta charset=\"UTF-8\">"
             + "</head>"
             + "<body>"
             + "<h1>Viagem Confirmada!</h1>"
             + "<p>Olá {{solicitante}}, sua viagem foi "
             + "confirmada.</p>"
             + "</body>"
             + "</html>";
     public static final String CANCEL_TEMPLATE = "<!DOCTYPE html>"
             + "<html lang=\"pt-br\">"
             + "<head>"
             + "<meta charset=\"UTF-8\">"
             + "</head>"
             + "<body>"
             + "<h3>Viagem Cancelada!</h3>"
             + "</body>"
             + "</html>";
     public static final String CHARLIE_VICTOR_TEMPLATE = "<!DOCTYPE html>"
             + "<html lang=\"pt-br\">"
             + "<head>"
             + "<meta charset=\"UTF-8\">"
             + "</head>"
             + "<body>"
             + "<h1>Veículo Alterado!</h1>"
             + "<p>Olá {{solicitante}}, o veículo selecionado para "
             + "a sua viagem foi alterado. Verifique o sistema "
             + "para mais detalhes.</p>"
             + "</body>"
             + "</html>";
     public static final String CHARLIE_ROMEO_TEMPLATE = "<!DOCTYPE html>"
             + "<html lang=\"pt-br\">"
             + "<head>"
             + "<meta charset=\"UTF-8\">"
             + "</head>"
             + "<body>"
             + "<h1>Veículo Alterado!</h1>"
             + "<p>Olá {{solicitante}}, a rota configurada para "
             + "a sua viagem foi alterada. Verifique o sistema "
             + "para mais detalhes.</p>"
             + "</body>"
             + "</html>";
     private Session getSession() {
         Properties props = System.getProperties();
         props.put("mail.transport.protocol", "smtp");
         props.put("mail.smtp.starttls.enable","true" );
         props.put("mail.smtp.host","smtp.gmail.com");
         props.put("mail.smtp.port", "587");
         props.put("mail.smtp.auth", "true" );
         Authenticator auth = new SMTPAuthenticator("syscaruni@gmail.com", 
                 "krug_viado");
         Session session = Session.getInstance(props, auth);
         return session;
     }
     
     
     private void sendMail(String to, Session session, String message) {
         if (to == null) {
             return;
         }
         try {
             
             Message msg = new MimeMessage(session);
             
               // -- Set the FROM and TO fields --
             msg.setFrom(new InternetAddress("syscaruni@gmail.com"));
             msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
             msg.setSubject("Notificação de Viagem - Unipampa");
             msg.setContent(message, "text/html; charset=utf-8");
               // -- Set some other header information --
             msg.setHeader("Unipampa", "Gerenciamento de Frota" );
             msg.setSentDate(new Date());
             
               // -- Send the message --
             Transport.send(msg);
             System.out.println("Message sent to"+to+" OK." );
             
             
         }
         catch (javax.mail.AuthenticationFailedException e) {
             
             e.printStackTrace();
         }
         catch (javax.mail.MessagingException m) {
             m.printStackTrace();
         }
         
         
     }
     
    public void sendmail(Usuario to, String template) {
         String html = template;
        if (!template.equals(Mail.CANCEL_TEMPLATE)) {
            html = template.replace("{{solicitante}}", to.getNome());
        }
        this.sendMail(to.getEmail(), this.getSession(), html);
     }
     
     public void sendbatch(List<Usuario> recpts, String template) {
         for (Usuario user: recpts) {
             String fix = template.replace("{{solicitante}}",
                     user.getNome());
            this.sendmail(user, fix);
         }
     }
     
     
     private class SMTPAuthenticator extends javax.mail.Authenticator {
         private String user;
         private String psswd;
         
         public SMTPAuthenticator(String user, String psswd) {
             super();
             this.user = user;
             this.psswd = psswd;
         }
         @Override
         public PasswordAuthentication getPasswordAuthentication() {                             // specify your password here
             return new PasswordAuthentication(user, psswd);
         }
   }
 }
