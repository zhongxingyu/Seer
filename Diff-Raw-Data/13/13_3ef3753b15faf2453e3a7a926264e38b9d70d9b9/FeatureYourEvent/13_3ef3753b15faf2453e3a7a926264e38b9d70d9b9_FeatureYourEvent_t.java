 package com.chaman.svc;
 
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.restlet.data.Form;
 import org.restlet.resource.Post;
 import org.restlet.resource.ServerResource;
 
 
 public class FeatureYourEvent  extends ServerResource {
 
 	@Post("form") 
 	public Response handlePost(Form webForm) { 
 	   
 	   Response result = new Response();
 		
 		try {
 			String uid = webForm.getFirstValue("uid");
 			String email = webForm.getFirstValue("email");
 			String phone = webForm.getFirstValue("phone");
 			String purpose = webForm.getFirstValue("puropse");
 
 			Properties props = new Properties();
 	        Session session = Session.getDefaultInstance(props, null);
 
 	        String msgBody = "uid:\t" + uid + "\n" +
 	        "email:\t" + email + "\n" +
 	        "phone:\t" + phone + "\n" +
 	        "purpose:\t" + purpose;
 
         		Message msg = new MimeMessage(session);
 	            msg.setFrom(new InternetAddress("promoter@raventsvc.appspotmail.com", "promoter"));
 	            msg.addRecipient(Message.RecipientType.TO,
 	                             new InternetAddress("dimitri.rouil@gmail.com", "admin"));
 	            msg.addRecipient(Message.RecipientType.TO,
                         new InternetAddress("florian.haftman@gmail.com", "admin"));
 	            msg.setSubject("[GEMSTER] - Feature your event");
 	            msg.setText(msgBody);
 	            Transport.send(msg);
	        
 			result.setSuccess(true);
 			result.setRecords(null);
 			
 		} catch (Exception ex) {
 			
 			result.setSuccess(false);
 			result.setError(ex.toString());
 		}
 		
 		return result;
 	}
 
 }
