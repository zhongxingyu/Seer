 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server;
 
 
 import java.util.Date;
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
 		if (ApplicationProperties.getProperty("email.smtp") == null || ApplicationProperties.getProperty("email.user") == null || ApplicationProperties.getProperty("email.password") == null) {
 			System.err.println("Emailer: No estan definidas las propiedades!");
 			return false;
 		}
 
 		props.put("mail.smtp.host", ApplicationProperties.getProperty("email.smtp"));
 		props.put("mail.smtp.port", "465");
 		props.put("mail.smtp.auth", "true");
 		props.put("mail.smtp.socketFactory.port", "465");
 		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
 		props.put("mail.smtp.socketFactory.fallback", "false");
 
 		if (getRecipient() == null || getSubject() == null || getBody() == null) {
 			System.err.println("Emailer: No estan definidas las partes del correo");
 			return false;
 		}
 
		Session session = Session.getDefaultInstance(props, new Authenticator() {
 			@Override
 			protected PasswordAuthentication getPasswordAuthentication() {
 				return new PasswordAuthentication(ApplicationProperties.getProperty("email.user"), ApplicationProperties.getProperty("email.password"));
 			}
 		});
 
 		try {
 			MimeMessage message = new MimeMessage(session);
 			message.setFrom(new InternetAddress(ApplicationProperties.getProperty("email.user")));
 			message.addRecipient(Message.RecipientType.TO, new InternetAddress(getRecipient()));
 			message.setSubject(getSubject());
 			message.setContent(getBody(), "text/html;charset=UTF-8");
 			message.setSentDate(new Date());
 			Transport.send(message);
 			return true;
 		} catch (MessagingException ex) {
 			System.err.println("No se ha podido enviar el correo: " + ex);
 		}
 
 		return false;
 	}
 }
