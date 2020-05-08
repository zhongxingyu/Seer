 package com.bics.jira.mail.model;
 
 import com.atlassian.mail.MailUtils;
 import com.atlassian.plugin.util.collect.Predicate;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import javax.mail.Address;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Part;
 import javax.mail.internet.ContentType;
 import javax.mail.internet.InternetAddress;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * JavaDoc here
  *
  * @author Victor Polischuk
  * @since 04.02.13 22:02
  */
 public class MessageAdapter {
     private static final Logger LOG = Logger.getLogger(MessageAdapter.class);
     private static final InternetAddress[] EMPTY = {};
    private static final Pattern REPLIES = Pattern.compile("(?i)^\\s*(?:re:\\s*|fw:\\s*)*");
     private static final String UNKNOWN_SUBJECT = "Unknown Subject";
     private static final String KEY_JIRA_FINGER_PRINT = "X-JIRA-FingerPrint";
     private static final String KEY_THREAD_TOPIC = "Thread-Topic";
     private static final String KEY_REPLY_SUBJECT = "In-Reply-To";
     private static final String KEY_MAIL_PRIORITY = "X-Priority";
     private static final int NORMAL_PRIORITY = 3;
 
     private final Message message;
     private final Part textBody;
     private final Part htmlBody;
     private final List<Part> attachments;
 
     public MessageAdapter(Message message) throws MessagingException {
         this.message = message;
 
         BodyPredicate textPlainPredicate = new BodyPredicate(MimeType.TEXT);
         BodyPredicate textHtmlPredicate = new BodyPredicate(MimeType.HTML);
         AttachmentPredicate attachmentPredicate = new AttachmentPredicate();
 
         apply(message, new ArrayList<Predicate<Part>>(Arrays.asList(textHtmlPredicate, textPlainPredicate, attachmentPredicate)));
 
         textBody = textPlainPredicate.body;
         htmlBody = textHtmlPredicate.body;
         attachments = attachmentPredicate.attachments;
     }
 
     public String getSubject() {
         try {
             String subject = message.getSubject();
 
             String inReplyTo = getHeader(KEY_REPLY_SUBJECT);
 
             if (StringUtils.isNotBlank(inReplyTo)) {
                 subject = inReplyTo;
             }
 
            subject = REPLIES.matcher(subject).replaceAll("");
 
             return StringUtils.abbreviate(subject, 200);
         } catch (MessagingException e) {
             LOG.warn("Cannot read subject. ", e);
         }
 
         return UNKNOWN_SUBJECT;
     }
 
     public int getPriority() {
         try {
             String header = getHeader(KEY_MAIL_PRIORITY);
 
             return header == null ? NORMAL_PRIORITY : Integer.parseInt(header.replaceFirst(".*(\\d+).*", "$1"));
         } catch (NumberFormatException e) {
             LOG.warn(e.getMessage(), e);
             return NORMAL_PRIORITY;
         }
     }
 
     public InternetAddress getAuthor() throws MessagingException {
         return getFrom()[0]; //TODO: fix it
     }
 
     public InternetAddress[] getFrom() throws MessagingException {
         Address[] from = message.getFrom();
 
         return Arrays.copyOf(from, from.length, EMPTY.getClass());
     }
 
     public InternetAddress[] getAllRecipients() throws MessagingException {
         Address[] allRecipients = message.getAllRecipients();
 
         return Arrays.copyOf(allRecipients, allRecipients.length, EMPTY.getClass());
     }
 
     public boolean isLoopMail(String instanceFingerPrint) {
         String[] headers = getHeaders(KEY_JIRA_FINGER_PRINT);
 
         return headers == null || Arrays.asList(headers).contains(instanceFingerPrint);
     }
 
     public boolean hasThreadTopic() {
         return getHeader(KEY_THREAD_TOPIC) != null;
     }
 
     public String getPlainTextBody() throws MessagingException {
         return convertText(textBody);
     }
 
     public String getHtmlTextBody() throws MessagingException {
         return convertText(htmlBody);
     }
 
     public Collection<Attachment> getAttachments() throws MessagingException {
         if (attachments == null || attachments.isEmpty()) {
             return Collections.emptyList();
         }
 
         try {
             Collection<Attachment> list = new LinkedList<Attachment>();
 
             for (Part part : attachments) {
                 File storedFile = File.createTempFile("attachment", "jira");
                 String fileName = part.getFileName();
                 ContentType contentType = new ContentType(part.getContentType());
                 InputStream content = part.getInputStream();
 
                 try {
                     OutputStream out = new BufferedOutputStream(new FileOutputStream(storedFile));
 
                     try {
                         IOUtils.copy(content, out);
                     } finally {
                         out.close();
                     }
                 } finally {
                     content.close();
                 }
 
 
                 if (fileName == null) {
                     fileName = UUID.randomUUID().toString();
                 } else {
                     fileName = MailUtils.fixMimeEncodedFilename(fileName);
                 }
 
                 list.add(new Attachment(storedFile, contentType, fileName));
             }
 
             return list;
         } catch (IOException e) {
             throw new MessagingException(e.getMessage(), e);
         }
     }
 
     private String getHeader(String key) {
         String[] headers = getHeaders(key);
 
         return headers == null ? null : headers[0];
     }
 
     private String[] getHeaders(String key) {
         try {
             String[] headers = message.getHeader(key);
 
             return headers == null || headers.length == 0 ? null : headers;
         } catch (MessagingException e) {
             LOG.warn(e.getMessage(), e);
             return null;
         }
     }
 
     private static String convertText(Part part) throws MessagingException {
         try {
             if (part == null || MailUtils.isContentEmpty(part)) {
                 return "";
             }
 
             return String.valueOf(part.getContent());
         } catch (IOException e) {
             throw new MessagingException(e.getMessage(), e);
         }
     }
 
     private static void apply(Part part, Collection<Predicate<Part>> predicates) throws MessagingException {
         if (predicates == null || predicates.isEmpty()) {
             return;
         }
 
         MimeType mimeType = MimeType.valueOf(part);
 
         if (mimeType != MimeType.MULTIPART) {
             for (Iterator<Predicate<Part>> iterator = predicates.iterator(); iterator.hasNext(); ) {
                 if (!iterator.next().evaluate(part)) {
                     iterator.remove();
                 }
             }
 
             return;
         }
 
         Multipart multipart;
 
         try {
             multipart = (Multipart) part.getContent();
         } catch (IOException e) {
             throw new MessagingException(e.getMessage(), e);
         }
 
         int count = multipart.getCount();
 
         for (int i = 0; i < count; i++) {
             Part child = multipart.getBodyPart(i);
 
             apply(child, predicates);
         }
     }
 
     private static class BodyPredicate implements Predicate<Part> {
         private final MimeType mimeType;
         private Part body;
 
         public BodyPredicate(MimeType mimeType) {
             this.mimeType = mimeType;
         }
 
         @Override
         public boolean evaluate(Part input) {
             try {
                 MimeType mimeType = MimeType.valueOf(input);
 
                 if (mimeType == this.mimeType) {
                     body = input;
                 }
 
                 return body == null;
             } catch (MessagingException e) {
                 throw new IllegalArgumentException(e);
             }
         }
     }
 
     private static class AttachmentPredicate implements Predicate<Part> {
         private List<Part> attachments = new ArrayList<Part>();
 
         @Override
         public boolean evaluate(Part input) {
             try {
                 if (MailUtils.isPartAttachment(input) || MailUtils.isPartInline(input) || MimeType.valueOf(input) == MimeType.OTHER) {
                     attachments.add(input);
                 }
 
                 return true;
             } catch (MessagingException e) {
                 throw new IllegalArgumentException(e);
             }
         }
     }
 
 }
