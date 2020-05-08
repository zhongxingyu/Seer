 package org.cloudifysource.widget.test;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * User: sagib
  * Date: 07/02/13
  * Time: 12:35
  */
 public class MailSender {
 
     public static void send(String mailHost, final String user, final String password, String title, String content, List<String> recipients) throws Exception {
 
         Properties props = new Properties();
 
         props.setProperty("mail.transport.protocol", "smtp");
         props.setProperty("mail.host", mailHost);
         props.setProperty("mail.user", user);
         props.setProperty("mail.password", password);
         props.put("mail.smtp.starttls.enable", "true");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.socketFactory.port", "587");
         props.put("mail.smtp.socketFactory.fallback", "false");
 
 
         Session mailSession = Session.getDefaultInstance(props, new Authenticator() {
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication(user, password);
             }
         });
         Transport transport = mailSession.getTransport();
 
         InternetAddress[] address = new InternetAddress[1];
         address[0] = new InternetAddress("tgrid@gigaspaces.com");
 
         MimeMessage message = new MimeMessage(mailSession);
         message.addFrom(address);
         message.setSubject(title);
         // create the message part
         MimeBodyPart messageBodyPart =
                 new MimeBodyPart();
 
         //fill message
         messageBodyPart.setContent(content, "text/html; charset=ISO-8859-1");
 
         Multipart multipart = new MimeMultipart();
         multipart.addBodyPart(messageBodyPart);
 
         String fileAttachment = "";
         for(String fileName : new File(System.getProperty("user.dir")).list()){
             if(fileName.toLowerCase().endsWith(".png")){
                 fileAttachment = fileName;
             }
         }
         // Part two is attachment
         if(!fileAttachment.equals("")){
             messageBodyPart = new MimeBodyPart();
             DataSource source =
                     new FileDataSource(fileAttachment);
             messageBodyPart.setDataHandler(
                     new DataHandler(source));
             messageBodyPart.setFileName(fileAttachment);
             multipart.addBodyPart(messageBodyPart);
         }
         message.setContent(multipart);
 
 
         InternetAddress[] recipientAddresses = new InternetAddress[recipients.size()];
         for (int i = 0; i < recipients.size(); i++) {
             recipientAddresses[i] = new InternetAddress(recipients.get(i));
         }
         message.addRecipients(Message.RecipientType.TO, recipientAddresses);
 
         transport.connect();
         transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
         transport.close();
     }
 
     private static String readFile( String file ) throws IOException {
         BufferedReader reader = new BufferedReader( new FileReader(file));
         String         line = null;
         StringBuilder  stringBuilder = new StringBuilder();
         String         ls = System.getProperty("line.separator");
 
         while( ( line = reader.readLine() ) != null ) {
             stringBuilder.append( line );
             stringBuilder.append( ls );
         }
 
         return stringBuilder.toString();
     }
 
 
     public static void main(String ... args) throws Exception{
         String host = args[0];
         String user = args[1];
         String pass = args[2];
         String recipients = args[3];
        String body = readFile("target/site/surefire-report.xml");
         send(host, user, pass, "Cloudify-Widget Test Report", body, Arrays.asList(recipients.split(",")));
     }
 }
