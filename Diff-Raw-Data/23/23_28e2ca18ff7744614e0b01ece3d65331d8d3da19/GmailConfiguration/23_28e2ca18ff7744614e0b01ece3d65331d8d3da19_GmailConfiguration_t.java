 package app.mail.configuration;
 
 import java.util.Properties;
 
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 
 public class GmailConfiguration implements MailerConfiguration {
 	
 	private Session session;
 	private static final String host = "smtp.gmail.com";
 	
 	private GmailConfiguration(Session session) {
 		this.session = session;
 	}
 	
 	public static MailerConfiguration configureSession(final String username, 
 												final String password) {
 		
 		Properties props = System.getProperties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
 		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		Authenticator authenticator = new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username,password);
			}
		};
		
		return new GmailConfiguration(Session.getDefaultInstance(props,
				authenticator));
 	}
 	
 	public Session getSession() {
 		return session;
 	}
 	
 	public final String getHost() {
 		return host;
 	}
 }
