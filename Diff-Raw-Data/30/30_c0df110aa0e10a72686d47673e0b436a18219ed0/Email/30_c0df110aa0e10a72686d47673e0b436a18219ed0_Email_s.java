 package sw.server;
import java.io.*;
import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
import javax.activation.*;
 
 import sw.server.models.Sheep;
 
import java.util.Properties;

 public class Email{
 	
 	
 	public static void send(String username, String email, Sheep sheep){		
 				
 				final String email1 = "sheepwatch@gmail.com";
 				final String password = "IT1901passord";	
 
 				Properties props = new Properties();
 				props.put("mail.smtp.auth", "true");
 				props.put("mail.smtp.starttls.enable", "true");
 				props.put("mail.smtp.host", "smtp.gmail.com");
 				props.put("mail.smtp.port", "587");
 		 
 				Session session1 = Session.getInstance(props,new javax.mail.Authenticator() {
 					protected PasswordAuthentication getPasswordAuthentication() {
 						return new PasswordAuthentication(email1, password);
 					}
 				  });
 		 
 				try {
 		 
 					Message message = new MimeMessage(session1);
 					message.setFrom(new InternetAddress(username));
 					message.setRecipients(Message.RecipientType.TO,
 						InternetAddress.parse(email));
 					message.setSubject("Sheep Watch System Alarm");
 					message.setText("Hi "+ username+ " \nYour sheep"+sheep.getName()+" with id"+sheep.getId()+" has set of an alarm." +
 							"\nGo to https://SheepWatch.com for information(Does not work, maybe we send some info in this mail;) )");
 		 
 					Transport.send(message);		 
 				} catch (MessagingException e) {
 					throw new RuntimeException(e);
 				}
 	}
 }
