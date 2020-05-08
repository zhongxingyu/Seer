 package be.spiker.thread;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.mail.Message;
 import javax.mail.Multipart;
 import javax.mail.Part;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.util.ByteArrayDataSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.liferay.portal.model.User;
 import com.liferay.portal.service.UserLocalServiceUtil;
 import com.liferay.portlet.documentlibrary.model.DLFileEntry;
 import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
 
 public class MailThread implements Runnable {
 
 	private static Log sLog = LogFactory.getLog(MailThread.class);
 
 	private String sender;
 	private String subject;
 	private String body;
 	private String host;
 	private Map<String, Long> imageReferences;
 
 	private List<String> emails;
 	private Long organizationId;
 
 	public MailThread(String sender, String subject, String body, String host, Map<String, Long> imageReferences, List<String> emails, Long organizationId) {
 		this.sender = sender;
 		this.subject = subject;
 		this.body = body;
 		this.host = host;
 		this.imageReferences = imageReferences;
 		this.emails = emails;
 		this.organizationId = organizationId;
 	}
 
 	@Override
 	public void run() {
 
 		try {
 
 			if (organizationId != null && !organizationId.equals(new Long(0))) {
 
 				List<User> users = UserLocalServiceUtil.getOrganizationUsers(organizationId);
 
 				for (User user : users) {
 
 					String mail = user.getEmailAddress();
 					if (mail != null && mail.trim().length() > 0 && mail.contains("@")) {
 						emails.add(mail);
 					}
 				}
 
 			}
 
 			Properties sessionProperties = System.getProperties();
 			sessionProperties.put("mail.smtp.host", host);
 			Session session = Session.getDefaultInstance(sessionProperties, null);
 
 			Multipart mp = new MimeMultipart();
 
 			MimeBodyPart htmlPart = new MimeBodyPart();
 			htmlPart.setContent(body, "text/html");
 			mp.addBodyPart(htmlPart);
 
 			Iterator<Entry<String, Long>> itr = imageReferences.entrySet().iterator();
 
 			while (itr.hasNext()) {
 
 				Entry<String, Long> entry = itr.next();
 				DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil.getFileEntry(entry.getValue());
 
 				MimeBodyPart imagePart = new MimeBodyPart();
 				DataSource fds = new ByteArrayDataSource(dlFileEntry.getContentStream(), dlFileEntry.getMimeType());
 
 				imagePart.setDataHandler(new DataHandler(fds));
 				imagePart.setHeader("Content-ID", "<" + entry.getKey() + ">");
 				imagePart.setDisposition(Part.INLINE);
 
 				mp.addBodyPart(imagePart);
 
 			}
 
 			Message message = new MimeMessage(session);
 			message.setFrom(new InternetAddress(sender));
 			message.setSubject(subject);
 			message.setContent(mp);
 
 			for (String mail : emails) {
 				try {
					sLog.error("sending mail to : " + mail + " sender : " + sender + " subject : " + subject);
 //					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail, false));
 //					Transport.send(message);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 		} catch (Exception e) {
 			sLog.error("failed to send mail", e);
 		}
 
 	}
 
 }
