 /*
  * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
  * @version $Id: MailServiceImpl.java 4496 2006-02-08 20:27:04Z wehrens $
  */
 package org.gridsphere.services.core.mail.impl;
 
 import org.gridsphere.portlet.service.PortletServiceException;
 import org.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridsphere.services.core.mail.MailMessage;
 import org.gridsphere.services.core.mail.MailService;
 import org.gridsphere.services.core.portal.PortalConfigService;
 
 import javax.mail.Message;
import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import java.util.Date;
 import java.util.Properties;
 
 /**
  * <code>MailServiceImpl</code> is a mail utility used to send {@link org.gridsphere.services.core.mail.MailMessage}s.
 *
  */
 public class MailServiceImpl implements PortletServiceProvider, MailService {
 
     private PortalConfigService portalConfigService = null;
 
     public void init(PortletServiceConfig config) {
         portalConfigService = (PortalConfigService) PortletServiceFactory.createPortletService(PortalConfigService.class, true);
     }
 
     public void destroy() {
     }
 
     /**
      * Send an email regarding job completion
      *
      * @param msg the MailMessage containing e-mail parameters
     * @throws MessagingException if a an error occurs sending the message
      */
     public void sendMail(MailMessage msg) throws PortletServiceException {
         Properties props = System.getProperties();
         String mailServiceHost = portalConfigService.getProperty(PortalConfigService.MAIL_SERVER);
         if (mailServiceHost != null) props.put("mail.smtp.host", mailServiceHost);
         String mailServicePort = portalConfigService.getProperty("mailport");
         if (mailServicePort != null) props.put("mail.smtp.port", mailServicePort);
         Session session = Session.getDefaultInstance(props, null);
         session.setDebug(true);
         Message mimeMessage = new MimeMessage(session);
         try {
             InternetAddress from = new InternetAddress(msg.getSender());
             InternetAddress to[] = InternetAddress.parse(msg.getEmailAddress());
             mimeMessage.setFrom(from);
             mimeMessage.setRecipients(Message.RecipientType.TO, to);
             mimeMessage.setSubject(msg.getSubject());
             mimeMessage.setSentDate(new Date());
             mimeMessage.setText(msg.getBody());
             Transport.send(mimeMessage);
         } catch (Exception e) {
             throw new PortletServiceException(e);
         }
     }
 }
