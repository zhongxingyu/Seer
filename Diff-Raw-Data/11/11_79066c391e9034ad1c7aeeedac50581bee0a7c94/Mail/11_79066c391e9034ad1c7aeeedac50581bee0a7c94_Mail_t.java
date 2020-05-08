 package uk.ac.cam.dashboard.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.mail.Message;
 import javax.mail.Message.RecipientType;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
 import uk.ac.cam.cl.dtg.ldap.LDAPQueryManager;
 import uk.ac.cam.cl.dtg.ldap.LDAPUser;
 import uk.ac.cam.dashboard.models.Deadline;
 import uk.ac.cam.dashboard.models.DeadlineUser;
 import uk.ac.cam.dashboard.models.User;
 
 public class Mail {
 
 	private final static String SMTP_ADDRESS = "mail-serv.cl.cam.ac.uk";
 	
 	public static void sendMail(String[] recipients, String from, String contents, String subject){
 		
 		final String MAIL_ADDRESS = "ott-admin@cl.cam.ac.uk";
 		
 		Properties p = new Properties();
         p.put("mail.smtp.host", SMTP_ADDRESS);
         //p.put("mail.smtp.port", SMTP_PORT);
         p.put("mail.smtp.starttls.enable", "true");
         
         Session s = Session.getDefaultInstance(p);
         Message msg = new MimeMessage(s);
 
         InternetAddress sentBy = null;
         InternetAddress[] sender = new InternetAddress[1];
         InternetAddress[] recievers = new InternetAddress[recipients.length];
         
         try {
         	sentBy = new InternetAddress(MAIL_ADDRESS);
             sender[0]  = new InternetAddress(from);
             for(int i=0; i<recipients.length; i++){
             	recievers[i] = new InternetAddress(recipients[i]);
             }
         } catch (AddressException e) {
             // TODO return an error
         	System.out.println("Unable to parse to/from");
             e.printStackTrace();
         }
         
         try {
         	if(sentBy!=null&&sender!=null&&recievers!=null){
 	            msg.setFrom(sentBy);            
 	            msg.setReplyTo(sender);
 	            msg.setRecipients(RecipientType.TO, recievers);
 	            msg.setSubject(subject);
 	            msg.setText(contents);
 	 
 	            Transport.send(msg);
         	}
             
         } catch (MessagingException e) {
         	System.out.println("Unable to send");
             e.printStackTrace();
         }
     }
 	
 	public static void sendNotificationEmail(String subject, Set<User> users){
 		String eol = System.getProperty("line.separator"); 
 		String mailSubject = Strings.MAIL_NOTIFICATION_SUBJECT + subject;
 		String mailBody = subject + eol +
 						Strings.MAIL_NOTIFICATION_HEADER + eol +
						" http://ott.cl.cam.ac.uk/dashboard/notifications " + eol +
 						"----------------------------" + eol +
 						Strings.MAIL_NOTIFICATION_FOOTER + eol;
 				
 		List<String> recipientsList = new ArrayList<String>();
 		int i=0;
 		for(User u : users){
 			String email;
 			if(u.getSettings().isNotificationSendsEmail()){
 				try{
 					LDAPUser lU = LDAPQueryManager.getUser(u.getCrsid());
 					email = lU.getEmail();
 					if(email==null){ email = u.getCrsid()+"@cam.ac.uk"; }
 				} catch (LDAPObjectNotFoundException e){
 					email = u.getCrsid()+"@cam.ac.uk"; 
 				}
 				recipientsList.add(email);
 				i++;
 			}
 		}
 		
 		String[] recipients = new String[recipientsList.size()];
 		recipientsList.toArray(recipients);
 		
 		Mail.sendMail(recipients, "otter-admin@cl.cam.ac.uk", mailBody, mailSubject);	
 		
 	}
 	
 	public static void buildDeadlineEmail(User currentUser, Set<DeadlineUser> deadlineUsers, String body){
 		
 		String[] recipients = new String[deadlineUsers.size()];
 		int i=0;
 		for(DeadlineUser du : deadlineUsers){
 			String email;
 			try{
 				LDAPUser u = LDAPQueryManager.getUser(du.getUser().getCrsid());
 				email = u.getEmail();
 				if(email==null){ email = du.getUser().getCrsid()+"@cam.ac.uk"; }
 			} catch (LDAPObjectNotFoundException e){
 				email = du.getUser().getCrsid()+"@cam.ac.uk"; 
 			}
 			recipients[i] = email;
 			i++;
 		}
 		String subject = currentUser.getName() + " ("+currentUser.getCrsid()+")" + Strings.MAIL_SETDEADLINE_SUBJECT;				
 		String sender;
 		try{
 			LDAPUser u = LDAPQueryManager.getUser(currentUser.getCrsid());
 			sender = u.getEmail();
 			if(sender==null){ sender = currentUser.getCrsid()+"@cam.ac.uk"; }
 		} catch(LDAPObjectNotFoundException e){
 			sender = currentUser.getCrsid()+"@cam.ac.uk";
 		}
 		
 		Mail.sendMail(recipients, sender, body, subject);	
 	}
 	
 	public static void setDeadline(User currentUser, Deadline deadline, Set<DeadlineUser> deadlineUsers){
 		String eol = System.getProperty("line.separator"); 
 		String body = Strings.MAIL_SETDEADLINE_HEADER + eol +
 						"Deadline: " + deadline.getTitle() + eol +
 						"Due: " + deadline.getFormattedDate() + eol +
 						"Message: " + deadline.getMessage() + eol +
						"http://ott.cl.cam.ac.uk/dashboard/deadlines/" + eol +
 						Strings.MAIL_SETDEADLINE_FOOTER + " ("+currentUser.getCrsid()+"@cam.ac.uk)";
 		buildDeadlineEmail(currentUser, deadlineUsers, body);
 	}
 	
 	public static void remindDeadline(User currentUser, Deadline deadline, Set<DeadlineUser> deadlineUsers){
 		String eol = System.getProperty("line.separator"); 
 		String body = Strings.MAIL_REMINDDEADLINE_HEADER + eol +
 						"Deadline: " + deadline.getTitle() + eol +
 						"Due: " + deadline.getFormattedDate() + eol +
 						"Message: " + deadline.getMessage() + eol +
						"http://ott.cl.cam.ac.uk/dashboard/deadlines/" + eol +
 						Strings.MAIL_REMINDDEADLINE_FOOTER + " ("+currentUser.getCrsid()+"@cam.ac.uk)";
 		buildDeadlineEmail(currentUser, deadlineUsers, body);
 	}
 	
 	public static void updateDeadline(User currentUser, Deadline deadline, Set<DeadlineUser> deadlineUsers){
 		String eol = System.getProperty("line.separator"); 
 		String body = Strings.MAIL_UPDATEDEADLINE_HEADER + eol +
 						"Deadline: " + deadline.getTitle() + eol +
 						"Due: " + deadline.getFormattedDate() + eol +
 						"Message: " + deadline.getMessage() + eol +
						"http://ott.cl.cam.ac.uk/dashboard/deadlines/" + eol +
 						Strings.MAIL_UPDATEDEADLINE_FOOTER + " ("+currentUser.getCrsid()+"@cam.ac.uk)";
 		buildDeadlineEmail(currentUser, deadlineUsers, body);
 	}
 	
 }
