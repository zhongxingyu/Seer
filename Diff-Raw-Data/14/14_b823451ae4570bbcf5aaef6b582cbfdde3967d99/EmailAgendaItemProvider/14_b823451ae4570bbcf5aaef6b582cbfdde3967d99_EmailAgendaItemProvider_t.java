 package openagendamail.data;
 
 import com.sun.mail.imap.IMAPMessage;
 import com.sun.mail.imap.IMAPStore;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import javax.mail.BodyPart;
 import javax.mail.Flags;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.NoSuchProviderException;
 import javax.mail.Session;
 import javax.mail.Store;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMultipart;
 import openagendamail.file.LogFile;
 import openagendamail.util.OamTools;
 
 /**
  * A class that provides AgendaItems from an email account.
  *
  * @author adam
  * @date May 4th, 2013
  */
 public class EmailAgendaItemProvider implements AgendaItemProvider {
 
     /** A constant for the Content Type. */
     private static final String PLAIN_TEXT = "TEXT/PLAIN";
 
     /** True if the emails should be deleted after generating the agenda document. */
     private static boolean m_deleteEmails;
 
     /** The messages from the email account. */
     private static Message[] m_messages;
 
 
     /**
      * Constructor.
      * @param deleteEmails true if the emails should be deleted after retrieval, false otherwise.
      */
     public EmailAgendaItemProvider(boolean deleteEmails){
         m_deleteEmails = deleteEmails;
         OamTools.PROPS.put("mail.store.protocol", "imaps");
     }
 
 
     /**
      * Fetches the {@link AgendaItem}s from the Email account configured to be used with this application.
      * {@inheritDoc}
      */
     @Override
     public Collection<AgendaItem> getItems() {
         Store store = null;
         try {
             // Fetch the mail from the account.
             LogFile.getLogFile().log("Connecting to email account...");
             Session session = Session.getInstance(OamTools.PROPS);
             store = session.getStore("imaps");
             store.connect("imap.gmail.com", OamTools.PROPS.getProperty("email"), OamTools.PROPS.getProperty("password"));
             LogFile.getLogFile().log("Successfully connected.");
 
             if (store instanceof IMAPStore){
                 IMAPStore imapStore = (IMAPStore)store;
 
                 LogFile.getLogFile().log("Retrieving emails from inbox...");
                 Folder inbox = imapStore.getFolder("inbox");
                 inbox.open(Folder.READ_WRITE);
                 m_messages = inbox.getMessages();
                 LogFile.getLogFile().log(m_messages.length + " messages successfully retrieved.\n\n");
 
             } else {
                 LogFile.getLogFile().log("Message store was not an IMAP Message store.  No messages retrieved.");
             }
         } catch (NoSuchProviderException ex) {
             LogFile.getLogFile().log("Couldn't find the mail provider.", ex);
         } catch (MessagingException ex) {
             LogFile.getLogFile().log("Message exception while initializing store", ex);
         }
 
         List<AgendaItem> items = new ArrayList<>(generateAgendaItems());
         Collections.sort(items);
 
         // delete the old messages.
         if (OamTools.PROPS.getProperty("debug", "false").equals("true")){
             LogFile.getLogFile().log("In debug mode:  Skipping email delete step.");
         } else {
            if (m_deleteEmails){
                deleteEmails(store);
            }
         }
 
         return items;
     }
 
     /**
      * Assembles the emails into AgendaItems.
      * @return a list of AgendaItems constructed from the emails received.
      */
     private static Collection<AgendaItem> generateAgendaItems(){
         Collection<AgendaItem> agendaItems = new ArrayList<>();
         try {
             // PROCESS AGENDA ITEMS
             List<IMAPMessage> messages = getValidMessages();
             for (IMAPMessage item : messages){
 
                 // Item Title
                 String title = item.getSubject();
 
                 // Item Sender
                 InternetAddress address = (InternetAddress)item.getSender();
                 String email = address.getAddress();
                 String name = "";
                 if (address.getPersonal() != null){
                     name = address.getPersonal();
                 }
 
                 // Body Text if any...
                 String body = "";
                 if (item.getContent() != null){
                     if (item.getContent() instanceof MimeMultipart){
                         MimeMultipart mmp = (MimeMultipart)item.getContent();
                         int bodyParts = mmp.getCount();
                         for (int i = 0; i < bodyParts; i++){
                             BodyPart bp = mmp.getBodyPart(i);
                             if (bp.getContentType().trim().startsWith(PLAIN_TEXT)){
                                 body = bp.getContent().toString();
                                 break;
                             }
                         }
                     }
                 }
                 agendaItems.add(new AgendaItem(email, name, title, body));
             }
         } catch (MessagingException ex) {
             LogFile.getLogFile().log("Error processing agenda items.", ex);
         } catch (IOException ioex){
             LogFile.getLogFile().log("Error fetching email body.", ioex);
         }
 
         return agendaItems;
     }
 
     /**
      * Fetches the valid messages (those that are actually _from_ members of the email list) from those in the inbox.
      * @return only the valid messages from the inbox.
      */
     private static List<IMAPMessage> getValidMessages() {
         List<IMAPMessage> validMessages = new ArrayList<>();
 
         List<Message> allMessages = Arrays.asList(m_messages);
         List<String> validEmails = OamTools.readEmails(OamTools.PROPS.getProperty("email.list.filename", "emails.txt"));
 
         // Check each message to determine its sender's authority to add items to the agenda.
         try {
             for (Message message : allMessages){
                 if (message instanceof IMAPMessage){
                     IMAPMessage msg = (IMAPMessage)message;
                     if (msg.getSender() instanceof InternetAddress){
                         InternetAddress address = (InternetAddress)msg.getSender();
 
                         // If the email is from someone on the agenda email list, add the message to the
                         // list of valid ones.
                         if (validEmails.contains(address.getAddress())){
                             validMessages.add(msg);
                         }
                     }
                 }
             }
         } catch (MessagingException ex) {
             LogFile.getLogFile().log("Error getting valid messages/agenda items.", ex);
         }
         return validMessages;
     }
 
     /**
      * Deletes the emails in the account and closes the message store.
      * @param store the message store to close.
      */
     private static void deleteEmails(Store store){
         try {
                LogFile.getLogFile().log("Deleting old emails...");
                 for (Message msg : m_messages){
                     msg.setFlag(Flags.Flag.DELETED, true);
                 }
                LogFile.getLogFile().log("Done deleting old emails.");
         } catch (MessagingException ex) {
             LogFile.getLogFile().log("Error deleting mesesages", ex);
         }
 
         // Close the store if it was initialized.
         try {
             if (store != null){
                 LogFile.getLogFile().log("Closing the connection to the email account.\n\n");
                 store.close();
             }
         } catch (MessagingException ex) {
                 LogFile.getLogFile().log("Error closing message store.", ex);
         }
     }
 }
