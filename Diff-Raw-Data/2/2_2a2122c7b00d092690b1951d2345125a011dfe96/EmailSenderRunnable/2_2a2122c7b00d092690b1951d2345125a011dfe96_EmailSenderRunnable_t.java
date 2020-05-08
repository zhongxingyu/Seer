 package openagendamail;
 
 import com.sun.mail.imap.IMAPStore;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.NoSuchProviderException;
 import javax.mail.Session;
 import javax.mail.Store;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import openagendamail.file.LogFile;
 import openagendamail.util.OamTools;
 
 /**
  * This class is a general purpose email sender.  It allows you to build and send emails with variable subjects, body
  * text and attachments to the email list.
  *
  * @author adam
  * @date February 17, 2012.
  */
 public class EmailSenderRunnable implements Runnable {
 
     /** The properties required to send emails from the configured email account. */
     private Properties m_props;
 
     /** The subject of the email to be sent. */
     private String m_subject;
 
     /** The body text of the email. */
     private String m_body;
 
     /** A list of attachments (file paths) to add to the email when it is sent. */
     private List<String> m_attachments;
 
     /**
      * Constructs a new EmailSenderRunnable.
      *
      * @param subject the subject of the email to send.
      * @param body the body of the email, if any.  This value may be null.
      * @param attachments a list of Strings that are the paths to files that should be attached.  Null is a permitted
      * value.
      */
     public EmailSenderRunnable(String subject, String body, List<String> attachments){
         if (subject == null){
             throw new IllegalArgumentException("Parameter 'subject' cannot be null.");
         }
         if (body == null){
             m_body = "";
         }
 
         System.out.println("Initializing sender for :  " + subject);
 
         m_subject = subject;
         m_body = body;
         m_props = OamTools.PROPS;
 
         // Add the attachments to the collection if any were provided.
         m_attachments = new ArrayList<>();
         if (attachments != null){
             m_attachments.addAll(attachments);
         }
 
         // Add special properties for using SMTP / sending mail.
         m_props.put("mail.store.protocol", "imaps");
         m_props.put("mail.smtp.starttls.enable", "true");
         m_props.put("mail.smtp.auth", "true");
         m_props.put("mail.smtp.host", "smtp.gmail.com");
         m_props.put("mail.transport.protocol", "smtp");
         m_props.put("mail.smtp.user", m_props.get("email"));
         m_props.put("mail.smtp.password", m_props.get("password"));
     }
 
     /**
      * Builds the message to be sent.
      *
      * @param session The email session to use to build and send the messages.
      *
      * @return a fully assembled and ready to send MimeMessage.
      * @throws MessagingException if an error occurs when assembling a message.
      */
     private MimeMessage buildEmail(Session session) throws MessagingException{
         // --- Define message
         List<String> emails = OamTools.readEmails(m_props.getProperty("email.list.filename", "emails.txt"));
         LogFile.getLogFile().log("Constructing email message....");
         MimeMessage message = new MimeMessage(session);
         message.setFrom(new InternetAddress(m_props.getProperty("email")));
 
         // Add Subject, Body, and Attachments.
         message.setSubject(m_subject);
         MimeMultipart multipart = new MimeMultipart("related");
         LogFile.getLogFile().log("Adding body text to email...");
 
         MimeBodyPart bodyText = new MimeBodyPart();
         bodyText.setText(m_body);
         multipart.addBodyPart(bodyText);
 
         LogFile.getLogFile().log("Attaching attachment files...");
         for (String attachment : m_attachments){
             try {
                 MimeBodyPart att = new MimeBodyPart();
                 att.attachFile(attachment);
                 multipart.addBodyPart(att);
             } catch (IOException ex) {
                 LogFile.getLogFile().log("Error attaching '" + attachment + "' file to email.", ex);
             }
         }
         message.setContent(multipart);
 
         // Add Recipients
        LogFile.getLogFile().log("Adding " + emails.size() + " email recipients...");
         for (String email : emails){
             message.addRecipient(Message.RecipientType.BCC, new InternetAddress(email));
         }
         return message;
     }
 
     /** {@inheritDoc} */
     @Override
     public void run() {
         try {
             // --- Connect to the email account.
             LogFile.getLogFile().log("Connecting to email account...");
             Session session = Session.getDefaultInstance(m_props);
             Store store = session.getStore("imaps");
             store.connect("imap.gmail.com", m_props.getProperty("email"), m_props.getProperty("password"));
             LogFile.getLogFile().log("Connected successfully.");
 
             // Get the email message store.
             if (store instanceof IMAPStore){
                 IMAPStore imapStore = (IMAPStore)store;
                 Folder inbox = imapStore.getFolder("inbox");
                 inbox.open(Folder.READ_WRITE);
 
                 // Assemble the message to be sent.
                 MimeMessage message = buildEmail(session);
 
                 // Close the message store.
                 LogFile.getLogFile().log("Closing Message store.");
                 store.close();
 
                 // Send the message.
                 LogFile.getLogFile().log("Sending the message...");
                 Transport transport = session.getTransport();
 
                 // Uses port 587 because we're using TLS/STARTTLS, its 465 for SSL
                 transport.connect("smtp.gmail.com", 587, m_props.getProperty("email"), m_props.getProperty("password"));
                 transport.sendMessage(message, message.getAllRecipients());
                 LogFile.getLogFile().log("Message(s) sent successfully.");
 
             } else {
                 LogFile.getLogFile().log("Message store was not an IMAP Message store.  No messages retrieved.");
             }
         } catch (NoSuchProviderException ex) {
             LogFile.getLogFile().log("Couldn't find the mail provider.", ex);
         } catch (MessagingException ex) {
             LogFile.getLogFile().log("Message exception while initializing store", ex);
         }
     }
 }
