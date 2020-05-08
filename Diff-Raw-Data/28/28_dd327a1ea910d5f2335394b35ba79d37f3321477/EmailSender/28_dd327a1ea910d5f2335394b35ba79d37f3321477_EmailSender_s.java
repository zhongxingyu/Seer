 package pt.utl.ist.fenix.tools.smtp;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.SendFailedException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.Message.RecipientType;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 public class EmailSender {
 
 	private static final int MAX_MAIL_RECIPIENTS;
 
     private static final Session session;
     static {
         ResourceBundle resourceBundle = ResourceBundle.getBundle("SMTPConfiguration");
         final Properties properties = new Properties();
         for (final Enumeration<String> keys = resourceBundle.getKeys() ; keys.hasMoreElements(); ) {
             final String key = keys.nextElement();
             final String value = resourceBundle.getString(key);
             properties.put(key, value);
         }
         session = Session.getDefaultInstance(properties, null);
         MAX_MAIL_RECIPIENTS = Integer.parseInt(properties.getProperty("mailSender.max.recipients"));
     }
 
     public static Collection<String> send(final String fromName, final String fromAddress,
     		final Collection<String> toAddresses, final Collection<String> ccAddresses, final Collection<String> bccAddresses,
     		final String subject, final String body) {
 
         final Collection<String> unsentAddresses = new ArrayList<String>(0);
 
         try {
        	final MimeMessage mimeMessage = new MimeMessage(session);
         	final String from = constructFromString(fromName, fromAddress);
            mimeMessage.setFrom(new InternetAddress(from));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(body);
 
            addRecipients(mimeMessage, Message.RecipientType.TO, toAddresses, unsentAddresses);
            addRecipients(mimeMessage, Message.RecipientType.CC, ccAddresses, unsentAddresses);
            Transport.send(mimeMessage);
 
            mimeMessage.setRecipient(Message.RecipientType.TO, null);
            mimeMessage.setRecipient(Message.RecipientType.CC, null);
 
             final List<String> bccAddressesList = new ArrayList<String>(bccAddresses);
             for (int i = 0; i < bccAddresses.size(); i = i + MAX_MAIL_RECIPIENTS) {
             	final List<String> subList = bccAddressesList.subList(i, Math.min(bccAddressesList.size(), i + MAX_MAIL_RECIPIENTS));
            	addRecipients(mimeMessage, Message.RecipientType.BCC, subList, unsentAddresses);
            	Transport.send(mimeMessage);
             }
         } catch (SendFailedException e) {
         	registerInvalidAddresses(unsentAddresses, e, toAddresses, ccAddresses, bccAddresses);
         } catch (MessagingException e) {
         	e.printStackTrace();
         }
         return unsentAddresses;
     }
 
 	private static void registerInvalidAddresses(final Collection<String> unsentAddresses,
 			final SendFailedException e, final Collection<String> toAddresses,
 			final Collection<String> ccAddresses, final Collection<String> bccAddresses) {
         if (e.getValidUnsentAddresses() != null) {
             for (int i = 0; i < e.getValidUnsentAddresses().length; i++) {
             	unsentAddresses.add(e.getValidUnsentAddresses()[i].toString());
             }
         } else {
             if (e.getValidSentAddresses() == null || e.getValidSentAddresses().length == 0) {
             	unsentAddresses.addAll(toAddresses);
             	unsentAddresses.addAll(ccAddresses);
             	unsentAddresses.addAll(bccAddresses);
             }
         }
 	}
 
 	private static String constructFromString(final String fromName, String fromAddress) {
 		return (fromName == null || fromName.length() == 0) ?
 				"\"" + fromName + "\"" + " <" + fromAddress + ">" : fromAddress;
 	}
 
 	private static void addRecipients(final MimeMessage mensagem, final RecipientType recipientType,
 			final Collection<String> emailAddresses, Collection<String> unsentMails)
 			throws MessagingException {
 		if (emailAddresses != null) {
 			for (final String emailAddress : emailAddresses) {
 				try {
 					if (emailAddressFormatIsValid(emailAddress)) {
 						mensagem.addRecipient(recipientType, new InternetAddress(emailAddress));
 					}
 				} catch (AddressException e) {
 					unsentMails.add(emailAddress);
 				}
 			}
 		}
 	}
 
 	public static boolean emailAddressFormatIsValid(String emailAddress) {
 		if((emailAddress == null) || (emailAddress.length() == 0))
 			return false;
 						
 		if(emailAddress.indexOf(' ') > 0)
 			return false;
 		
 		String[] atSplit = emailAddress.split("@");
 		if(atSplit.length != 2)
 			return false;
 
 		else if((atSplit[0].length() == 0) || (atSplit[1].length() == 0))
 			return false;
 		
 		String domain = new String(atSplit[1]);
 
 		if(domain.lastIndexOf('.') == (domain.length() - 1))
 			return false;
 		
 		if(domain.indexOf('.') <= 0)
 			return false;
 				
 		return true;
 		
 	}
 }
