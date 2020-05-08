 package br.com.xisp.mail;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 import br.com.caelum.vraptor.ioc.Component;
 
 @Component
 public class MailerImpl implements Mailer {
 
 	public void sendMail(String to, String from, String subject,
 			String message) {
 
 		SimpleEmail email = new SimpleEmail();
 
 		try {
 			email.setDebug(true);
 			email.setHostName("smtp.gmail.com");
 			//Autenticar com outra conta
			email.setAuthentication("edipofederle", "luis@federle");
 			email.setSSL(true);
 			email.addTo(to);
 			email.setFrom("edipofederle@gmail.com"); 
 			email.setSubject(subject);
 			email.setMsg(message);
 			email.send();
 
 		} catch (EmailException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 }
