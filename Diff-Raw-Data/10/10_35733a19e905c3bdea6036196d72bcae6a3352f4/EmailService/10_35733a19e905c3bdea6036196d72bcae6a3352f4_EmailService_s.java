 package com.open.rotile.service;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import com.open.rotile.model.Project;
 import com.open.rotile.server.model.EmailData;
 
 public class EmailService implements IEmailService {
 
    public static final String SENDER_EMAIL = "rotile.open@gmail.com";
    public static final String SENDER_NAME = "Rotile";
 
    @Override
 	public void sendCreationEmail(EmailData emailData, Project project) {
 		Properties props = new Properties();
 		Session session = Session.getDefaultInstance(props, null);
 
 		try {
 			String msgBody = buildMessageBody(emailData, project);
 			Message msg = new MimeMessage(session);
 			msg.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
 			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
 					emailData.email()));
 			msg.setSubject("Project " + project.name() + " creation");
 			msg.setText(msgBody);
 			Transport.send(msg);
 		} catch (UnsupportedEncodingException e) {
 			// Do not show this exception to the user
 			e.printStackTrace();
 		} catch (MessagingException e) {
 			// Do not show this exception to the user
 			e.printStackTrace();
 		}
 
 	}
 
 	private String buildMessageBody(EmailData emailData, Project project) {
 		final String projectUrl = emailData.serverName() + "#/project/"
 				+ project.id();
		String message = "Your project %s has been created an can be viewed at the following url:\n<a href=\"%s\">%s</a>";
 		message = String
 				.format(message, project.name(), projectUrl, projectUrl);
 		return message;
 	}
 
 }
