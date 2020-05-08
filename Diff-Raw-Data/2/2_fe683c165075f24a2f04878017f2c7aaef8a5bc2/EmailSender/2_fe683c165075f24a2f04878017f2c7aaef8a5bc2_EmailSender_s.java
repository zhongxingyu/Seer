 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
  * (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.haiba.epimibaimporter.email;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.mail.javamail.MimeMessagePreparator;
 
 import dk.nsi.haiba.epimibaimporter.log.Log;
 import dk.nsi.haiba.epimibaimporter.util.AlphanumComparator;
 
 public class EmailSender {
     private static Log log = new Log(Logger.getLogger(EmailSender.class));
 
     @Value("${smtp.from}")
     private String from;
     @Value("${smtp.to_commaseparated}")
     private String to_commaseparated;
     @Value("${smtp.sendhello}")
     private boolean sendHello;
 
     @Autowired
     private JavaMailSender javaMailSender;
 
     public void send(final Collection<String> unknownBanrSet, final Collection<String> unknownAlnrSet) {
         String not_html = "After the recent import, the following unknown table entries are discovered:\n";
         ArrayList<String> alnrSet = new ArrayList<String>(unknownAlnrSet);
         Collections.sort(alnrSet, AlphanumComparator.INSTANCE);
         ArrayList<String> banrSet = new ArrayList<String>(unknownBanrSet);
         Collections.sort(banrSet, AlphanumComparator.INSTANCE);
         if (!alnrSet.isEmpty()) {
             not_html += "-----\n";
             not_html += "alnr:\n";
             String delim = "";
             for (String alnr : alnrSet) {
                 not_html += delim + alnr;
                 delim = ", ";
             }
             not_html += "\n";
         }
         if (!banrSet.isEmpty()) {
             not_html += "-----\n";
             not_html += "banr:\n";
             String delim = "";
             for (String banr : banrSet) {
                 not_html += delim + banr;
                 delim = ", ";
             }
             not_html += "\n";
         }
         sendText("EPIMIBA: Notification on unknown table entries", not_html);
     }
 
     private void sendText(final String subject, final String nonHtml) {
         MimeMessagePreparator preparator = new MimeMessagePreparator() {
             @Override
             public void prepare(MimeMessage mimeMessage) throws Exception {
                 MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
                 messageHelper.setValidateAddresses(true);
 
                 String[] split = to_commaseparated.split(",");
                 for (String emailAddress : split) {
                     emailAddress = emailAddress.trim();
                     try {
                         log.debug("adding " + emailAddress);
                         messageHelper.addTo(emailAddress);
                         log.debug("added " + emailAddress);
                     } catch (MessagingException e) {
                         log.error("unable to parse email address from " + emailAddress, e);
                     }
                 }
                 messageHelper.setFrom(from);
                 messageHelper.setSubject(subject);
                 messageHelper.setText(nonHtml, false);
             }
         };
         javaMailSender.send(preparator);
     }
 
     public String getTo() {
         return to_commaseparated;
     }
 
     public void sendHello() {
         if (sendHello) {
             SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             sendText("EPIMIBA: Import started at " + dateFormat.format(new Date()), "Have a nice day");
         }
     }
 
     public void sendDone(String error) {
         if (sendHello) {
             SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (error != null) {
                 sendText("EPIMIBA: Import done at " + dateFormat.format(new Date()), "No errors");
             } else {
                 sendText("EPIMIBA: Import done at " + dateFormat.format(new Date()), "Errors found\n:" + error);
             }
         }
     }
 }
