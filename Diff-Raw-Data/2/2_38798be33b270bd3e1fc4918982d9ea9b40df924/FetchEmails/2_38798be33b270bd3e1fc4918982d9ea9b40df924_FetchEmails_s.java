 package jobs;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.*;
 
 import javax.mail.*;
 import javax.mail.Flags.Flag;
 import javax.mail.internet.*;
 import javax.mail.search.*;
 
 import models.Attachment;
 import models.JobApplication;
 
 import controllers.Mails;
 
 import play.*;
 import play.db.jpa.Blob;
 import play.jobs.*;
 import play.libs.IO;
 
 @Every("10min")
 public class FetchEmails extends Job {
 
     public void doJob() throws Exception {
         
         if(Play.configuration.getProperty("mailbox.username") == null || Play.configuration.getProperty("mailbox.password") == null) {
             Logger.error("Please configure mailbox credentials in conf/credentials.conf");
             return;
         }
 
         // Connect to gmail mailbox
         Properties props = new Properties();
         props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.setProperty("mail.imap.socketFactory.port", "993");
         Session session = Session.getDefaultInstance(props);
         Store store = session.getStore("imap");
         store.connect("imap.gmail.com", Play.configuration.getProperty("mailbox.username"), Play.configuration.getProperty("mailbox.password"));
 
         // Open jobs mailbox
         Folder folder = store.getFolder("jobs");
         folder.open(Folder.READ_WRITE);
 
         // Search unstarred messages
         SearchTerm unstarred = new FlagTerm(new Flags(Flags.Flag.FLAGGED), false);
         Message[] messages = folder.search(unstarred);
 
         // Loop over messages
         for (Message message : messages) {
             
             String contentString = "";
             List<Attachment> attachments = new ArrayList<Attachment>();
             
             // Decode content
             if (message.getContent() instanceof String) {
                 contentString = (String) message.getContent();
             } else if (message.getContent() instanceof Multipart) {
                 Multipart mp = (Multipart) message.getContent();
                 for (int j = 0; j < mp.getCount(); j++) {
                     Part part = mp.getBodyPart(j);
                     String disposition = part.getDisposition();
                     if (disposition == null
                             || ((disposition != null) && (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition
                                     .equalsIgnoreCase(Part.INLINE)))) {
                         // Check if plain
                         MimeBodyPart mbp = (MimeBodyPart) part;
                         if (mbp.isMimeType("text/plain")) {
                             contentString += (String) mbp.getContent();
                         } else {
                             attachments.add(saveAttachment(part));
                         }
                     }
                 }
             }
             
             String name = ((InternetAddress) message.getFrom()[0]).getPersonal();
             String email = ((InternetAddress) message.getFrom()[0]).getAddress();
             String to = ((InternetAddress) message.getAllRecipients()[0]).getAddress();
             
             if("jobs@zenexity.com".equals(to)) {
                 
                 // Create Application
                 JobApplication application = new JobApplication(name, email, contentString, attachments);
                 application.create();
                 for(Attachment attachment : attachments) {
                     attachment.jobApplication = application;
                     attachment.create();
                 }
                 Mails.applied(application);
                 
             } else {
                 Pattern regexp = Pattern.compile("^jobs[+][^@]{5}-([0-9]+)@.*$");
                 Matcher matcher = regexp.matcher(to);
                 if(matcher.matches()) {
                     Long id = Long.parseLong(matcher.group(1));
                     JobApplication application = JobApplication.findById(id);
                     if(application == null) {
                         Logger.warn("Job application not found %s, for %s", id, to);
                     } else {
                         application.addMessage(name, email, contentString);
                         application.save();
                     }
                 } else {
                     Logger.warn("Unknow address --> %s", to);
                 }
             }
             
            if (Play.mode == Play.Mode.PROD) {
                 message.setFlag(Flag.FLAGGED, true);
             }
             
         }
 
         // Close connection
         folder.close(false);
         store.close();
     }
     
     private Attachment saveAttachment(Part part) throws Exception, MessagingException, IOException {
         Attachment attachment = new Attachment();
         attachment.name = decodeName(part.getFileName());
         attachment.content.set(part.getInputStream(), part.getContentType());
         return attachment;
     }
     
     protected String decodeName(String name) throws Exception {
         if (name == null || name.length() == 0) {
             return "unknown";
         }
         String ret = java.net.URLDecoder.decode(name, "UTF-8");
 
         // also check for a few other things in the string:
         ret = ret.replaceAll("=\\?utf-8\\?q\\?", "");
         ret = ret.replaceAll("\\?=", "");
         ret = ret.replaceAll("=20", " ");
 
         return ret;
     }
 
 }
