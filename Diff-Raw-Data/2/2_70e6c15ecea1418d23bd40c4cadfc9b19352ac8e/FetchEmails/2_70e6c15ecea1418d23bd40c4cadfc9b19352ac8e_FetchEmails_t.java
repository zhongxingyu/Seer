 package jobs;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.Address;
 import javax.mail.Flags;
 import javax.mail.Flags.Flag;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.Multipart;
 import javax.mail.Part;
 import javax.mail.Session;
 import javax.mail.Store;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.search.FlagTerm;
 import javax.mail.search.SearchTerm;
 
 import models.Attachment;
 import models.JobApplication;
 import play.Logger;
 import play.Play;
 import play.jobs.Every;
 import play.jobs.Job;
 import play.libs.MimeTypes;
 import controllers.Mails;
 
 @Every("10min")
 public class FetchEmails extends Job {
 
     public void doJob() throws Exception {
         Logger.debug("=== Fetch email...");
 
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
             try {
                 handleMessage(message);
             } catch(Exception e) {
                 Logger.error(e, "Cannot read this message");
             }
         }
 
         // Close connection
         folder.close(false);
         store.close();
     }
 
     private static void handleMessage(Message message) throws Exception {
         String contentString = "(no content found)";
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
                     } else if(mbp.isMimeType("multipart/ALTERNATIVE")) {
                         MimeMultipart mmp = (MimeMultipart)mbp.getContent();
                         for(int k=0; k<mmp.getCount(); k++) {
                             Part p = mmp.getBodyPart(k);
                             if (((MimeBodyPart)p).isMimeType("text/plain")) {
                                 contentString = (String)p.getContent();
                             }
                         }
                     } else {
                         attachments.add(saveAttachment(part));
                     }
                 }
             }
         }
 
         String name = ((InternetAddress) message.getFrom()[0]).getPersonal();
         String email = ((InternetAddress) message.getFrom()[0]).getAddress();
         String to = ((InternetAddress) message.getAllRecipients()[0]).getAddress();
         if ((name == null || name.length() == 0) && email != null && email.contains("@")) {
             name = email.split("@")[0];
         }
         for (Address a : (Address[])message.getAllRecipients()) {
             if ("rfc822".equals(a.getType())) {
                 InternetAddress ia = (InternetAddress)a;
                 if (ia.getAddress().contains("jobs")) {
                     to = ia.getAddress();
                 }
             } else {
                 Logger.warn("Could not convert address " + a + " to an InternetAddress, type is invalid " + a.getType());
             }
         }
 
        if ("no-reply@zenexity.com".equals(email)) {
             // Ignore emails sent from jobs (= sent from Great People)
             if (Play.mode == Play.Mode.PROD) {
                 message.setFlag(Flag.FLAGGED, true);
             }
             return;
         }
         if ("jobs@zenexity.com".equals(to)) {
             Logger.debug("Found a new application: " + name);
             JobApplication application = JobApplication.createApplication(name, email, contentString, attachments);
             Mails.applied(application);
         } else {
             // Look for an application to a tagged email, e.g. jobs+t:design@zenexity.com
             Pattern tagRe = Pattern.compile("^jobs[+]t\\-(\\w+)@.*$");
             Matcher matcher = tagRe.matcher(to);
             if(matcher.matches()) {
                 String tag = matcher.group(1);
                 JobApplication application = JobApplication.createApplication(name, email, contentString, attachments);
                 application.tags = tag;
                 application.save();
                 Mails.applied(application);
                 return;
             }
 
             // Look for a reply to an existing thread
             Pattern replyRe = Pattern.compile("^jobs[+][^@]{5}-([0-9]+)@.*$");
             matcher = replyRe.matcher(to);
             if(matcher.matches()) {
                 Long id = Long.parseLong(matcher.group(1));
                 JobApplication application = JobApplication.findById(id);
                 if(application == null) {
                     Logger.warn("Job application not found %s, for %s", id, to);
                 } else {
                     application.addMessage(name, email, contentString);
                 }
                 Logger.debug("Found a follow-up from: " + name);
             } else {
                 Logger.warn("Unknow address --> %s", to);
             }
         }
 
         if (Play.mode == Play.Mode.PROD) {
             message.setFlag(Flag.FLAGGED, true);
         }
     }
 
     private static Attachment saveAttachment(Part part) throws Exception {
         Attachment attachment = new Attachment();
         attachment.name = decodeName(part.getFileName());
         Logger.debug("Found attachment name: " + attachment.name);
         String type = MimeTypes.getContentType(attachment.name, "application/binary");
         attachment.content.set(part.getInputStream(), type);
         Logger.debug("   => content-type: " + type);
         attachment.save();
         return attachment;
     }
 
     protected static String decodeName(String name) throws Exception {
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
