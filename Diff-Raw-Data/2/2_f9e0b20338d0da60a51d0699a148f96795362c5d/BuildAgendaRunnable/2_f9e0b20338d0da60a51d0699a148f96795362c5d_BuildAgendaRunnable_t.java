 package openagendamail;
 
 import com.sun.mail.imap.IMAPMessage;
 import com.sun.mail.imap.IMAPStore;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
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
 import openagendamail.data.AgendaItem;
 import openagendamail.file.LogFile;
 import openagendamail.file.Pdf;
 import openagendamail.util.OpenAgendaMailTools;
 import org.apache.pdfbox.exceptions.COSVisitorException;
 
 /**
  * A runnable that build the agenda document from the emails.
  *
  * @author adam
  * Created:       Dec 30, 2012
  * Last updated:  Feb 8, 2013
  */
 public class BuildAgendaRunnable implements Runnable {
 
     /** A constant for the Content Type. */
     private static final String PLAIN_TEXT = "TEXT/PLAIN";
 
     /** Application Properties. */
     private static Properties m_props;
 
     /** True if the emails should be deleted after generating the agenda document. */
     private static boolean m_deleteEmails;
 
     /** The messages from the email account. */
     private static Message[] m_messages;
 
     /** A formatter for date objects used when generating the .doc object. */
     private static SimpleDateFormat m_dateFormat;
 
     /**
      * Constructor.  Creates a new CheckMailRunnable.
      * @param properties the application's properties.
      * @param deleteEmails true if the emails should be deleted after building the agenda, false otherwise.
      */
     public BuildAgendaRunnable(Properties properties, boolean deleteEmails) {
         if (properties == null) {
             throw new IllegalArgumentException("Parameter 'properties' cannot be null.");
         }
         m_props = properties;
         m_deleteEmails = deleteEmails;
         m_props.put("mail.store.protocol", "imaps");
        m_dateFormat = new SimpleDateFormat("MMM.dd.YYYY");
     }
 
     /** {@inheritDoc} */
     @Override
     public void run() {
         Store store = null;
         try {
             // Fetch the mail from the account.
             LogFile.getLogFile().log("Connecting to email account...");
             Session session = Session.getDefaultInstance(m_props, null);
             store = session.getStore("imaps");
             store.connect("imap.gmail.com", m_props.getProperty("email"), m_props.getProperty("password"));
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
 
         // Using the messages retrieved, build the word doc.
         LogFile.getLogFile().log("Generating Agenda document.");
 
         //generateDocxAgenda();
         generatePdfAgenda();
 
         // delete the old messages.
         LogFile.getLogFile().log("Deleting old emails...");
         deleteEmails(store);
         LogFile.getLogFile().log("Done deleting old emails.");
     }
 
     /**
      * Deletes the emails in the account and closes the message store.
      * @param store the message store to close.
      */
     private static void deleteEmails(Store store){
         try {
             if (m_deleteEmails){
                 for (Message msg : m_messages){
                     msg.setFlag(Flags.Flag.DELETED, true);
                 }
             }
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
 
 
     /** Generates the agenda document. */
     private static void generatePdfAgenda(){
         try {
             Pdf pdf = new Pdf();
 
             // Render Title of Agenda
             String title = m_props.getProperty("agenda.title", "Agenda");
             String sub = "This document generated By OpenAgendaMail " + OpenAgendaMail.VERSION + " on:  " + m_dateFormat.format(new Date());
             pdf.renderTitle(title, sub);
 
             // Render Agenda Items to PDF.
             List<AgendaItem> items = generateAgendaItems();
 
             // Render Each Agenda Item
             for (AgendaItem item : items){
                 pdf.renderAgendaItem(item);
             }
 
             // Save PDF to disk.
             try {
                 pdf.saveAs(m_props.getProperty("doc.name", "agenda.pdf"));
             } catch (COSVisitorException ex) {
                 LogFile.getLogFile().log("Error saving PDF Agenda to disk.", ex);
             }
             pdf.close();
 
         } catch (IOException ex) {
             LogFile.getLogFile().log("Error generating PDF.", ex);
         }
     }
 
     /**
      * Assembles the emails into AgendaItems.
      * @return a list of AgendaItems constructed from the emails received.
      */
     private static List<AgendaItem> generateAgendaItems(){
         List<AgendaItem> agendaItems = new ArrayList<>();
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
 
         Collections.sort(agendaItems);
         return agendaItems;
     }
 
     /**
      * Fetches the valid messages (those that are actually _from_ members of the email list) from those in the inbox.
      * @return only the valid messages from the inbox.
      */
     private static List<IMAPMessage> getValidMessages() {
         List<IMAPMessage> validMessages = new ArrayList<>();
 
         List<Message> allMessages = Arrays.asList(m_messages);
         List<String> validEmails = OpenAgendaMailTools.readEmails(m_props.getProperty("email.list.filename", "emails.txt"));
 
         // Check each message to determine its sender's authority to add items to the agenda.
         try {
             for (Message message : allMessages){
                 if (message instanceof IMAPMessage){
                     IMAPMessage msg = (IMAPMessage)message;
                     if (msg.getSender() instanceof InternetAddress){
                         InternetAddress address = (InternetAddress)msg.getSender();
 
                         // If the email is from someone on the agenda email list, add the message to the lsit of valid ones.
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
 }
