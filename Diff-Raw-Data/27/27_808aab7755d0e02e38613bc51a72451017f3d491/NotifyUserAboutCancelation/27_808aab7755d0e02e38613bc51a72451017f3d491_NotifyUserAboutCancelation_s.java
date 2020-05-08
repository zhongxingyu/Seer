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
 
 import wfm.db.Course;
 
 
 public class NotifyUserAboutCancelation implements JavaDelegate {
 
 	private String add;
 	private String cName;
 	private String uName;
 	
 	@Override
 	public void execute(DelegateExecution execution) throws Exception {
 		
 		add = (String) execution.getVariable("userMail");
		//cName = (String) execution.getVariable("deletedCourseName");
		cName = (String) ((Course)execution.getVariable("courseToApprove")).getName();
 		uName = (String) execution.getVariable("userName");
 		
 		System.out.println("Sending mail...to: "+add+" with name "+uName+" about deleted course: "+cName);	
 		//-------------------- different msg for course rejection vs. cancellatiopn
		String msgType = (boolean) execution.getVariable("approved") ? "cancelled" : "rejected";
 		
 		initializeMailService(add,cName,uName, msgType);
 		System.out.println("done...");
 	}
 	
 	//set that for message
 	private void initializeMailService(String add,String cName, String uName, String msgType) throws AddressException, MessagingException {
 		
 		   // Sender's email ID needs to be mentioned
 		   String from = "sscms.sender@gmail.com";
 		   String pass = "12345pass";
 		   // Recipient's email ID needs to be mentioned.
 		   String to = add;
 
 		   String host = "smtp.gmail.com";
 
 		   // Get system properties
 		   Properties properties = System.getProperties();
 		   // Setup mail server
 		   properties.put("mail.smtp.starttls.enable", "true");
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
 		      message.setSubject("SCCMS: Course canceled!");
 
 		      // Now set the actual message
 		      // set header according to reason
 		      String header = (msgType.equals("cancelled")) ? "This Email is a notification that a course you have been subscribed to has been canceled!\n\n" : "This Email is a notification that your attempt to subscribe to a course was " + msgType + " by the trainer \n\n";
 		      message.setText("Dear "+uName+"!\n" +
 		    		header+
 		      		"The affected course is: "+cName+"\n"+
 		      		"Please feel free to select an alternative.\n\n" +
 		      		"This is an automatically generated course " +  msgType + "  notification.\n\n" +
 		      		"Best regards,\nSCCMS");	      
 		      
 		      // Send message
 		      Transport transport = session.getTransport("smtp");
 		      transport.connect(host, from, pass);
 		      transport.sendMessage(message, message.getAllRecipients());
 		      transport.close();
 		      System.out.println("Sent message successfully....");
 		   }catch (MessagingException mex) {
 		      mex.printStackTrace();
 		   }			
 		}
 }
