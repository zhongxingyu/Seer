 package com.robonobo.remote.service;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.Message.RecipientType;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class MailServiceImpl implements MailService {
 	private Session session;
 	Log log = LogFactory.getLog(getClass());
 	private boolean configured;
 
	public MailServiceImpl(String smtpServer) {
 		configured = (smtpServer != null);
 		if (configured) {
 			Properties props = System.getProperties();
 			props.put("mail.smtp.host", smtpServer);
 			session = Session.getDefaultInstance(props, null);
 		} else
 			log.error("SMTP server not configured... no mails will be sent!");
 	}
 
 	public void sendMail(String fromName, String fromEmail, String toName, String toEmail, String subject, String body)
 			throws IOException {
 		sendMail(fromName, fromEmail, toName, toEmail, null, null, subject, body);
 	}
 
 	public void sendMail(String fromName, String fromEmail, String toName, String toEmail, String replyToName,
 			String replyToEmail, String subject, String body) throws IOException {
 		if(!configured) {
 			log.error("Not sending mail to "+toEmail+" with subject '"+subject+"' - SMTP server not configured");
 			return;
 		}
 		try {
 			Message msg = new MimeMessage(session);
 			msg.setFrom(new InternetAddress(fromEmail, fromName));
 			if (replyToEmail != null)
 				msg.setReplyTo(new InternetAddress[] { new InternetAddress(replyToEmail, replyToName) });
 			msg.setRecipient(RecipientType.TO, new InternetAddress(toEmail, toName));
 			msg.setSubject(subject);
 			msg.setText(body);
 			msg.setSentDate(new Date());
 			msg.setHeader("X-Sent-By", "robonobo MIDAS");
 			if (log.isInfoEnabled()) {
 				StringBuffer sb = new StringBuffer("Sending mail: from '");
 				sb.append(fromName).append("' <");
 				sb.append(fromEmail).append(">, to: '");
 				sb.append(toName).append("' <");
 				sb.append(toEmail).append(">, subject: ");
 				sb.append(subject);
 				log.info(sb);
 			}
 			Transport.send(msg);
 		} catch (Exception e) {
 			throw new IOException(e);
 		}
 	}
 }
