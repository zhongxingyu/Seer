 package org.accesointeligente.server;
 
 
 import java.util.Properties;
 
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 public class Emailer {
 	private Properties props;
 	private String recipient;
 	private String subject;
 	private String body;
 
 	public Emailer() {
 		props = System.getProperties();
 	}
 
 	public Emailer(String recipient, String subject, String body) {
 		props = System.getProperties();
 		this.recipient = recipient;
 		this.subject = subject;
 		this.body = body;
 	}
 
 	public String getRecipient() {
 		return recipient;
 	}
 
 	public void setRecipient(String recipient) {
 		this.recipient = recipient;
 	}
 
 	public String getSubject() {
 		return subject;
 	}
 
 	public void setSubject(String subject) {
 		this.subject = subject;
 	}
 
 	public String getBody() {
 		return body;
 	}
 
 	public void setBody(String body) {
 		this.body = body;
 	}
 
 	public Boolean connectAndSend() {
		if (ApplicationProperties.getProperty("email.server") == null || ApplicationProperties.getProperty("email.user") == null || ApplicationProperties.getProperty("email.password") == null) {
 			System.err.println("Emailer: No estan definidas las propiedades!");
 			return false;
 		}
 
 		if (getRecipient() == null || getSubject() == null || getBody() == null) {
 			System.err.println("Emailer: No estan definidas las partes del correo");
 			return false;
 		}
 
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage message = new MimeMessage(session);
 
 		try {
 			message.setFrom(new InternetAddress(ApplicationProperties.getProperty("email.user")));
 			message.addRecipient(Message.RecipientType.TO, new InternetAddress(getRecipient()));
 			message.setSubject(getSubject());
			message.setContent(getBody(), "text/html");
 			Transport.send(message);
 			return true;
 		} catch (MessagingException ex) {
 			System.err.println("No se ha podido enviar el correo: " + ex);
 		}
 
 		return false;
 	}
 }
