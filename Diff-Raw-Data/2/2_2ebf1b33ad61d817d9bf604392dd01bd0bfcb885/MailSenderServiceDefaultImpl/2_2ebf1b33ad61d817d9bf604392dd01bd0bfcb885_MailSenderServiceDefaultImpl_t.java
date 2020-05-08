 package dmk.mail;
 
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.apache.commons.lang3.Validate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MailSenderServiceDefaultImpl implements MailSenderService {
 	Logger logger = LoggerFactory.getLogger(MailSenderServiceDefaultImpl.class);
 	
 	protected String emailAddr;
 	protected String pass;
 	
 	public MailSenderServiceDefaultImpl() {
 		super();
 	}
 
 	public MailSenderServiceDefaultImpl(final String emailAddr, final String pass) {
 		super();
 		this.emailAddr = emailAddr;
 		this.pass = pass;
 	}
 
 
 	public void send(final String subject, final String content, final String... recipients) {
 		Validate.notBlank(subject);
 		Validate.notBlank(content);
 		Validate.notNull(recipients);
 		Validate.notEmpty(recipients);
 		
 		logger.debug("sending message to " + recipients.toString());
 		
 		Properties props = new Properties();
 		props.put("mail.smtp.host", "smtp.gmail.com");
 		props.put("mail.smtp.socketFactory.port", "465");
 		props.put("mail.smtp.socketFactory.class",
 				"javax.net.ssl.SSLSocketFactory");
 		props.put("mail.smtp.auth", "true");
 		props.put("mail.smtp.port", "465"); //465, 587
 
 		Session session = Session.getDefaultInstance(props,
 				new javax.mail.Authenticator() {
 					protected PasswordAuthentication getPasswordAuthentication() {
 						return new PasswordAuthentication(emailAddr,
 								pass);
 					}
 				});
 
 		if(logger.isDebugEnabled()){
 			session.setDebug(true);
 			System.setProperty("javax.net.debug", "ssl");
 		}
 		try {
 
 			StringBuilder sb = new StringBuilder(64);
 			int index = 0;
 			for(String recipient: recipients){
 				if(index++ > 1){
 					sb.append(";");
 				}
 				sb.append(recipient);
 			}
 			Message message = new MimeMessage(session);
 			message.setFrom(new InternetAddress("dmknopp@no-reply"));
 			message.setRecipients(Message.RecipientType.TO,
 					InternetAddress.parse(sb.toString()));
			message.setSubject(subject);
 			message.setText(content);
 
 			Transport.send(message);
 			
 			if(logger.isDebugEnabled()){
 				logger.debug("sent message");
 			}
 		} catch (MessagingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public String getEmailAddr() {
 		return emailAddr;
 	}
 
 	public void setEmailAddr(String emailAddr) {
 		this.emailAddr = emailAddr;
 	}
 
 	public String getPass() {
 		return pass;
 	}
 
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 }
