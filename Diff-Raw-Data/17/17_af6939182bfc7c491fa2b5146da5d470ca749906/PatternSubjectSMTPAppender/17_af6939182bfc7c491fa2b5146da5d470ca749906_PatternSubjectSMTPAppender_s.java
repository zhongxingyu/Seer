 package com.amee.base.engine;
 
 import org.apache.log4j.Layout;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.helpers.LogLog;
 import org.apache.log4j.net.SMTPAppender;
 import org.apache.log4j.spi.LoggingEvent;
 
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Transport;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.internet.MimeUtility;
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 
 /**
  * This is based on code from here: http://lajosd.blogspot.com/2009/09/log4j-smtpappender-exception-info-in.html
  */
 public class PatternSubjectSMTPAppender extends SMTPAppender {
 
     @Override
     protected void sendBuffer() {
 
         // Note: this code already owns the monitor for this
         // appender. This frees us from needing to synchronize on 'cb'.
         try {
             MimeBodyPart part = new MimeBodyPart();
 
             StringBuffer sbuf = new StringBuffer();
             String t = layout.getHeader();
             if (t != null)
                 sbuf.append(t);
             int len = cb.length();
             for (int i = 0; i < len; i++) {
                 // sbuf.append(MimeUtility.encodeText(layout.format(cb.get())));
                 LoggingEvent event = cb.get();
 
                 // setting the subject
                 if (i == 0) {
                     Layout subjectLayout = new PatternLayout(getSubject());
                     msg.setSubject(MimeUtility.encodeText
                             (subjectLayout.format(event), "UTF-8", null));
                 }
 
                 sbuf.append(layout.format(event));
                 if (layout.ignoresThrowable()) {
                     String[] s = event.getThrowableStrRep();
                     if (s != null) {
                         for (int j = 0; j < s.length; j++) {
                             sbuf.append(s[j]);
                             sbuf.append(Layout.LINE_SEP);
                         }
                     }
                 }
             }
             t = layout.getFooter();
             if (t != null)
                 sbuf.append(t);
             part.setContent(sbuf.toString(), layout.getContentType());
 
             Multipart mp = new MimeMultipart();
             mp.addBodyPart(part);
             msg.setContent(mp);
 
             msg.setSentDate(new Date());
             Transport.send(msg);
 
         } catch (MessagingException e) {
             LogLog.error("sendBuffer() Caught MessagingException: " + e.getMessage(), e);
         } catch (UnsupportedEncodingException e) {
             LogLog.error("sendBuffer() Caught UnsupportedEncodingException: " + e.getMessage(), e);
         }
     }
 }
