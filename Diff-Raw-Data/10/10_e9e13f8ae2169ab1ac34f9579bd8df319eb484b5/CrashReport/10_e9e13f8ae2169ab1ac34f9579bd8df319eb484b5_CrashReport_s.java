 package com.haystacksoftware.crashreportserver;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.activation.DataHandler;
import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.servlet.ServletContext;
 
 public class CrashReport {
     private String appName;
     private String uuid;
     private Map<String, Object> map = new HashMap<String, Object>();
     
     public CrashReport(String theAppName, String theUUID) {
         appName = theAppName;
         uuid = theUUID;
     }
     public void setValue(String name, String value) {
         map.put(name, value);
     }
     public void setValue(String name, File value) {
         map.put(name, value);
     }
     public void sendEmail(ServletContext sc) throws IOException {
         try {
             Properties props = new Properties();
             props.put("mail.smtp.host", sc.getInitParameter("mail.smtp.host"));
             Session session = Session.getDefaultInstance(props, null);
             MimeMessage message = new MimeMessage(session);
             message.setFrom(new InternetAddress("crashreport@haystacksoftware.com"));
             message.addRecipient(Message.RecipientType.TO, new InternetAddress(sc.getInitParameter("mail.recipient")));
            message.setSubject("crash report");
             
             Multipart multipart = new MimeMultipart();
             
             MimeBodyPart textPart = new MimeBodyPart();
             multipart.addBodyPart(textPart);
             StringBuffer text = new StringBuffer("crash report\n\napp: " + appName + "\nreport uuid: " + uuid + "\n\n");
             for (String key : map.keySet()) {
                 Object value = map.get(key);
                 if (value instanceof File) {
                     File file = (File)value;
                     MimeBodyPart filePart = new MimeBodyPart();
                    DataSource source = new FileDataSource(file.getPath());
                    filePart.setDataHandler(new DataHandler(source));
                    filePart.setFileName(file.getPath());
                     multipart.addBodyPart(filePart);
                 } else {
                     text.append(key + ": " + value + "\n");
                 }
             }
             
             textPart.setText(text.toString());
             message.setContent(multipart);
             Transport.send(message);
             
         } catch(MessagingException e) {
             throw new IOException(e);
         }
     }
 }
