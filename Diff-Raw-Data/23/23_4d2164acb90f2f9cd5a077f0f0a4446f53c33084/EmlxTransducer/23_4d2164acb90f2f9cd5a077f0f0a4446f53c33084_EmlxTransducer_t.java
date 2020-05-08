 package org.deepfs.fsml.xdcr;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
import java.util.Date;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.Address;
 import javax.mail.BodyPart;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 
 public class EmlxTransducer extends Transducer {
 
 
     static final Properties PROP;
     static final Session SESS;
 
     static {
         // [MS] this is needed for emlx as
         // the text/plain body contains xml which is
         // incorrectly read by the base64 decoder.
         PROP = new Properties();
         System.setProperty("mail.mime.base64.ignoreerrors", "true");
         System.setProperty("mail.mime.applefilenames", "true");
         SESS = Session.getDefaultInstance(PROP, null);
     }
     // convert Tue Nov 09 16:06:04 CET 2010 to RFC 2822
     private final SimpleDateFormat format = new SimpleDateFormat(
             "EEE, dd MMM yyyy HH:mm:ss Z");
     /** Strip the mail address from User <user@domain.tt>. */
     final Pattern getMail = Pattern.
             compile("([\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+"
                     + "[A-Za-z]{2,4})");
 
     /**
      * Constructor.
      */
     public EmlxTransducer() {
         super("text/x-mail");
     }
 
     @Override
     public String read(final String path) {
         Message message = null;
         final StringBuilder result = new StringBuilder(128);
         result.append("<metadata>");
         try {
             InputStream in = extractEMLX(path);
             message = new MimeMessage(SESS, in);
         } catch (FileNotFoundException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         } catch (MessagingException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         if (message == null)
             return "";
 
         extractMime(message, result);
 
         result.append("</metadata>");
         return result.toString();
     }
 
     /**
      * Cuts the actual email.
      * @param path to emlx file
      * @return InputStream containing the actual mail
      * @throws IOException
      */
     private InputStream extractEMLX(final String path) throws IOException {
         final FileInputStream fi = new FileInputStream(path);
         final BufferedInputStream fis = new BufferedInputStream(fi);
         final BufferedReader br = new BufferedReader(new FileReader(path));
         String line = br.readLine();
         if(line == null) throw new IllegalStateException("Not an .emlx");
 
         int len = Integer.parseInt(line.trim()) - line.length();
         byte[] b = new byte[len];
         if(line.length() + 1 != fis.skip(line.length() + 1))
             throw new IllegalStateException("Failed to skip.");
         int read = fis.read(b, 0, len);
 
         fis.close();
         br.close();
         fi.close();
 
         if (read != len) throw new IllegalStateException("Not an .emlx");
         return new ByteArrayInputStream(b);
     }
 
     /**
      * This method extracts all available meta information from email messages.
      *
      * @param msg the email Message
      * @param result StringBuilder that is filled with information.
      */
     private void extractMime(final Message msg, final StringBuilder result) {
         Matcher m;
         try {
             for (Address a : msg.getAllRecipients()) {
                 m = getMail.matcher(a.toString());
                 if(m.find()) result.append(keyValue("To:", m.group(1)));
             }
         } catch (MessagingException e) {
             // TODO Auto-generated catch block
             System.err.println("Error parsing TO: " + e.getMessage());
         }
         try {
             for (Address g : msg.getFrom()) {
                 m = getMail.matcher(g.toString());
                 if (m.find()) result.append(keyValue("From:", m.group(1)));
             }
         } catch (MessagingException e) {
             // TODO Auto-generated catch block
             System.err.println("Error parsing FROM: " + e.getMessage());
         }
 
         try {
             result.append(keyValue("Subject:", msg.getSubject()));
         } catch (MessagingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         try {
            Date date = msg.getSentDate();
            if(date != null)
            result.append(keyValue("Date:", format.format(date)));
         } catch (MessagingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         try {
 
             final Object content = msg.getContent();
             if (content instanceof java.lang.String)
                 result.append(keyValue("Body", content.toString()));
             else if (content instanceof Multipart) {
                 extractMultipart((Multipart) content, result);
 
             }
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (MessagingException e) {
             // TODO Auto-generated catch block
             System.err.println(e.getMessage());
         }
 
     }
 
     private void extractMultipart(final Multipart content,
             final StringBuilder result) throws MessagingException, IOException {
         for (int i = 0; i < content.getCount(); i++) {
             final BodyPart bp = content.getBodyPart(i);
             final String dispo = bp.getDisposition();
             if (null == dispo && bp instanceof MimeBodyPart) {
                 // this is not an attachment
                 // see also http://java.sun.com/developer/onlineTraining/ \
                 // JavaMail/contents.html
                 MimeBodyPart mbp = (MimeBodyPart) bp;
                 if (mbp.isMimeType("text/plain"))
                     result.append(keyValue("Body", bp.getContent().toString()));
                 // [MS] *Todo* Parse HTML content (strip tags or sth?)
 //                } else if (mbp.isMimeType("text/html")) {
 //                    result.append(keyValue("HTMLBody", bp.getContent().
 //                            toString()));
 //                }
 
             }
         }
 
     }
 
     @Override
     public boolean write(final String path, final String xml) {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public String template() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public String rng() {
         // TODO Auto-generated method stub
         return null;
     }
 
 }
