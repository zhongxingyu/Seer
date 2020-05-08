 package app.mail.configuration;
 
 import java.util.Properties;
 
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
		props.setProperty("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", username);
		props.put("mail.smtp.password", password);
		props.put("mail.smtp.port", "587");
 		props.put("mail.smtp.auth", "true");
		return new GmailConfiguration(Session.getDefaultInstance(props));
 	}
 	
 	public Session getSession() {
 		return session;
 	}
 	
 	public final String getHost() {
 		return host;
 	}
 }
