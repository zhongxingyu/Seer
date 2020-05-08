 package wfm.task;
 
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.activiti.engine.delegate.DelegateExecution;
 import org.activiti.engine.delegate.JavaDelegate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class NotifyUserAboutCancelation implements JavaDelegate {
 	
 	public static Logger log = LoggerFactory.getLogger(NotifyUserAboutCancelation.class);
 
 	private String add;
 	private String cName;
 	private String uName;
 	
 	@Override
 	public void execute(DelegateExecution execution) throws Exception {
 		
 		add = (String) execution.getVariable("userMail");
 		cName = (String) execution.getVariable("deletedCourseName");		
 		uName = (String) execution.getVariable("userName");
 		
		log.info("Sending mail...to: "+add+" with name "+uName+" about deleted course: "+cName);	
 		//-------------------- different msg for course rejection vs. cancellatiopn
 		String msgType = "";
 		
 		try {
 			msgType = (Boolean) execution.getVariable("approved") ? "cancelled" : "rejected";
 		}
 		catch (NullPointerException ex) {
 			msgType = "cancelled";
 			log.info("no active current user process instance registered for respective course");
 		}
 		
		initializeMailService(add,cName,uName, msgType);
 		log.info("done...");
 	}
 	
 	//set that for message
	private void initializeMailService(String add,String cName, String uName, String msgType) throws AddressException, MessagingException {
 		
 		// Sender's email ID needs to be mentioned
 		   String from = "noreply.sccms@michaelb.at";
 		   String pass = "9c5567f9";
 		   // Recipient's email ID needs to be mentioned.
 		   String to = add;
 
 		   String host = "mail.michaelb.at";
 
 		   // Get system properties
 		   Properties properties = System.getProperties();
 		   // Setup mail server
 		   properties.put("mail.smtp.starttls.enable", "true");
 		   properties.put("mail.smtp.ssl.trust", "mail.michaelb.at");
 		   properties.put("mail.smtp.host", host);
 		   properties.put("mail.smtp.user", from);
 		   properties.put("mail.smtp.password", pass);
 		   properties.put("mail.smtp.port", "587");
 		   properties.put("mail.smtp.auth", "true");
 
 		   // Get the default Session object.
 		   Session session = Session.getInstance(properties);
 
 		   try{
 		      // Create a default MimeMessage object.
 		      MimeMessage message = new MimeMessage(session);
 
 		      // Set From: header field of the header.
 		      message.setFrom(new InternetAddress(from));
 
 		      // Set To: header field of the header.
 		      message.addRecipient(Message.RecipientType.TO,
 		                               new InternetAddress(to));
 
 		      // Set Subject: header field
 		      message.setSubject("SCCMS: Course "+msgType+"!");
 
 		      // Now set the actual message
 		      // set header according to reason
		      String header = (msgType.equals("cancelled")) ? "This Email is a notification that the course '"+cName+"' you have been subscribed to has been "+msgType+"!\n\n" : "This Email is a notification that your attempt to subscribe to the course '"+cName+"' was " + msgType + " by the trainer \n\n";
 		     
 		      String text = (msgType.equals("cancelled")) ? "cancellation" : "rejection";
 			     
 		      message.setText("Dear "+uName+"!\n\n" +
 		    		header+
 		      		"\n"+
 		      		"Please feel free to select an alternative.\n\n" +
 		      		"This is an automatically generated course " +  text + " notification.\n\n" +
 		      		"Best regards,\nSCCMS");	      
 		      
 		      // Send message
 		      Transport transport = session.getTransport("smtp");
 		      transport.connect(host, from, pass);
 		      transport.sendMessage(message, message.getAllRecipients());
 		      transport.close();
 		      log.info("Sent message successfully....");
 		   }catch (MessagingException mex) {
 		      mex.printStackTrace();
 		   }			
 		}
 }
