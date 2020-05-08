 package no.bekk.digiq.channel.smtp;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import javax.mail.BodyPart;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.internet.ContentType;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.internet.ParseException;
 
 import no.bekk.digiq.Forsendelse;
 import no.bekk.digiq.channel.IncomingMessageListener;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.subethamail.smtp.MessageContext;
 import org.subethamail.smtp.MessageHandler;
 import org.subethamail.smtp.RejectException;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 
 public class DigipostMailHandler implements MessageHandler {
 
     public static final Pattern RECIPIENT_PATTERN = Pattern.compile("^[a-z.]+#[0-9A-Z]{4}$");
     private static final Logger LOG = LoggerFactory.getLogger(DigipostMailHandler.class);
 
     private List<String> recipients = new ArrayList<String>();
     private final MessageContext ctx;
     private final IncomingMessageListener listener;
 
     public DigipostMailHandler(MessageContext ctx, IncomingMessageListener listener) {
         this.ctx = ctx;
         this.listener = listener;
     }
 
     public void from(String from) throws RejectException {
         LOG.debug("Incoming mail from:" + from);
     }
 
     public void recipient(String recipient) throws RejectException {
         LOG.debug("Incoming mail recipient:" + recipient);
         recipients.add(recipient);
     }
 
     public void data(InputStream data) throws IOException {
         byte[] dataBytes = IOUtils.toByteArray(data);
 
         LOG.debug("Incoming mail data:");
         LOG.debug(new String(dataBytes));
 
         try {
             MimeMessage message = new MimeMessage(Session.getInstance(new Properties()), new ByteArrayInputStream(dataBytes));
             MimeMultipart multipart = (MimeMultipart) message.getContent();
 
             byte[] pdfAttachment = findPdfAttachment(multipart);
             if (pdfAttachment == null) {
                 LOG.warn("Could not find any pdf attachment in mail message.");
                 return;
             }
 
            String subject = message.getSubject();
             if (subject == null) {
                 LOG.warn("Could not find subject.");
                 return;
             }
 
             for (String recipient : recipients) {
 
                 if (RECIPIENT_PATTERN.matcher(recipient).matches()) {
                     listener.received(new Forsendelse(subject, recipient, null, null, null, null, null, null, null, "smtp", pdfAttachment));
                 } else {
                     LOG.warn("Invalid recipient address '{}'. Not sending.", recipient);
                 }
             }
         } catch (Exception e) {
             LOG.error("Feil.", e);
         }
     }
 
 
     private byte[] findPdfAttachment(MimeMultipart multipart) throws MessagingException, ParseException, IOException {
         for (int i = 0; i < multipart.getCount(); i++) {
             BodyPart part = multipart.getBodyPart(i);
             ContentType contentType = new ContentType(part.getContentType());
             String nameParameter = contentType.getParameter("name");
             if ((nameParameter != null && nameParameter.endsWith(".pdf")) || contentType.match("application/pdf")) {
                 LOG.debug("Found pdf attachment.");
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 IOUtils.copy(part.getInputStream(), baos);
                 return baos.toByteArray();
             }
         }
         return null;
     }
 
     public void done() {
         LOG.debug("Finished");
     }
 }
