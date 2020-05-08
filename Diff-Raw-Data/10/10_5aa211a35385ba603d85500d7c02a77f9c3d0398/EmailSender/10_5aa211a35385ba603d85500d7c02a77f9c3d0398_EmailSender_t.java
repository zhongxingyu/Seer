 package pt.utl.ist.fenix.tools.smtp;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.SendFailedException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.Message.RecipientType;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import pt.utl.ist.fenix.tools.util.PropertiesManager;
 
 public class EmailSender {
 
     private static final int MAX_MAIL_RECIPIENTS;
 
     private static final Session session;
     static {
         try {
             Properties properties = PropertiesManager.loadProperties("/SMTPConfiguration.properties");
             session = Session.getDefaultInstance(properties, null);
             MAX_MAIL_RECIPIENTS = Integer.parseInt(properties.getProperty("mailSender.max.recipients"));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static Collection<String> send(final String fromName, final String fromAddress,
             final Collection<String> toAddresses, final Collection<String> ccAddresses,
             final Collection<String> bccAddresses, final String subject, final String body) {
 
         if (fromAddress == null) {
             throw new NullPointerException("error.from.address.cannot.be.null");
         }
 
         final Collection<String> unsentAddresses = new ArrayList<String>(0);
 
         final String from = constructFromString(fromName, fromAddress);
 
         try {
             final MimeMessage mimeMessageTo = new MimeMessage(session);
             mimeMessageTo.setFrom(new InternetAddress(from));
             mimeMessageTo.setSubject(subject);
             mimeMessageTo.setText(body);
 
             addRecipients(mimeMessageTo, Message.RecipientType.TO, toAddresses, unsentAddresses);
             addRecipients(mimeMessageTo, Message.RecipientType.CC, ccAddresses, unsentAddresses);
             Transport.send(mimeMessageTo);
 
         } catch (SendFailedException e) {
            registerInvalidAddresses(unsentAddresses, e, toAddresses, ccAddresses, null);
         } catch (MessagingException e) {
             e.printStackTrace();
         }
 
         if (bccAddresses != null) {
             final List<String> bccAddressesList = new ArrayList<String>(bccAddresses);
             for (int i = 0; i < bccAddresses.size(); i = i + MAX_MAIL_RECIPIENTS) {
                List<String> subList = null;
                 try {
                    subList = bccAddressesList.subList(i, Math.min(bccAddressesList
                             .size(), i + MAX_MAIL_RECIPIENTS));
                     final MimeMessage mimeMessageBcc = new MimeMessage(session);
                     mimeMessageBcc.setFrom(new InternetAddress(from));
                     mimeMessageBcc.setSubject(subject);
                     mimeMessageBcc.setText(body);
                     addRecipients(mimeMessageBcc, Message.RecipientType.BCC, subList, unsentAddresses);
                     Transport.send(mimeMessageBcc);
                 } catch (SendFailedException e) {
                    registerInvalidAddresses(unsentAddresses, e, null, null, subList);
                 } catch (MessagingException e) {
                     e.printStackTrace();
                 }
             }
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
                 if (toAddresses != null) {
                     unsentAddresses.addAll(toAddresses);
                 }
                 if (ccAddresses != null) {
                     unsentAddresses.addAll(ccAddresses);
                 }
                 if (bccAddresses != null) {
                     unsentAddresses.addAll(bccAddresses);
                 }
             }
         }
     }
 
     private static String constructFromString(final String fromName, String fromAddress) {
         return (fromName == null || fromName.length() == 0) ? fromAddress : "\"" + fromName + "\""
                 + " <" + fromAddress + ">";
     }
 
     private static void addRecipients(final MimeMessage mensagem, final RecipientType recipientType,
             final Collection<String> emailAddresses, Collection<String> unsentMails)
             throws MessagingException {
         if (emailAddresses != null) {
             for (final String emailAddress : emailAddresses) {
                 try {
                     if (emailAddressFormatIsValid(emailAddress)) {
                         mensagem.addRecipient(recipientType, new InternetAddress(emailAddress));
                     } else {
                         unsentMails.add(emailAddress);
                     }
                 } catch (AddressException e) {
                     unsentMails.add(emailAddress);
                 }
             }
         }
     }
 
     public static boolean emailAddressFormatIsValid(String emailAddress) {
         if ((emailAddress == null) || (emailAddress.length() == 0))
             return false;
 
         if (emailAddress.indexOf(' ') > 0)
             return false;
 
         String[] atSplit = emailAddress.split("@");
         if (atSplit.length != 2)
             return false;
 
         else if ((atSplit[0].length() == 0) || (atSplit[1].length() == 0))
             return false;
 
         String domain = new String(atSplit[1]);
 
         if (domain.lastIndexOf('.') == (domain.length() - 1))
             return false;
 
         if (domain.indexOf('.') <= 0)
             return false;
 
         return true;
 
     }
 }
