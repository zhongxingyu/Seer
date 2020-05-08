 package org.notificationengine.mail;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.log4j.Logger;
 import org.notificationengine.constants.Constants;
 import org.notificationengine.spring.SpringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.MailException;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.stereotype.Component;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 
 
 @Component(value=Constants.MAILER)
 public class Mailer {
 
 	private static Logger LOGGER = Logger.getLogger(Mailer.class);
 	
 	@Autowired
 	private JavaMailSender mailSender;
 	
 	@Autowired
 	private SimpleMailMessage templateMessage;
 	
 	@Autowired
 	private Properties localSettingsProperties; 
 
 	public Boolean sendMail(
             String recipientAddress,
             String text,
             Boolean isHtmlTemplate,
             Collection<File> filesToAttach,
             Map<String, String> options) {
 
         Boolean result = Boolean.FALSE;
 
         try {
 
             MimeMessage message = this.mailSender.createMimeMessage();
 
             // use the true flag to indicate you need a multipart message
             MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
 
             helper.setTo(recipientAddress);
 
             if (options != null) {
 
                 String subject = options.get(Constants.SUBJECT);
 
                 if (StringUtils.isEmpty(subject)) {
 
 					subject = this.localSettingsProperties.getProperty(Constants.DEFAULT_SUBJECT);
                 }
                 
                 helper.setSubject(subject);
 
 
                 String from = options.get(Constants.FROM);
 
                 if (StringUtils.isEmpty(from)) {
 
                 	from = this.localSettingsProperties.getProperty(Constants.DEFAULT_FROM);
                 }
                 
                 helper.setFrom(from);
             }
 
             //TODO : test size of global attachments and send several emails if too big
 
             double totalSize = 0;
 
             Collection<File> filesToSendInOtherMails = new HashSet<>();
 
             if(filesToAttach != null) {
 
                 for(File file : filesToAttach) {
 
                     LOGGER.warn(file.getName() + ": " + file.length());
 
                     totalSize = totalSize + file.length();
 
                     if(totalSize < Constants.MAX_ATTACHMENT_SIZE) {
 
                         helper.addAttachment(file.getName(), file);
                     }
                     else {
 
                         filesToSendInOtherMails.add(file);
                     }
 
                 }
 
             }
 
             // use the true flag to indicate the text included is HTML
             helper.setText(text, isHtmlTemplate);
 
             this.mailSender.send(message);
 
             this.sendOtherAttachments(recipientAddress, filesToSendInOtherMails, options);
 
             result = Boolean.TRUE;
 
         } catch (MailException me) {
 
             LOGGER.error(ExceptionUtils.getFullStackTrace(me));
 
             LOGGER.error("Unable to send mail");
 
         } catch (MessagingException messagingExc) {
 
             LOGGER.error(ExceptionUtils.getFullStackTrace(messagingExc));
 
             LOGGER.error("Unable to send mail");
 
         }
 
         return result;
     }
 
     private void sendOtherAttachments(String recipientAddress,
                                       Collection<File> filesToSendInOtherMails,
                                       Map<String, String> options)
             throws MailException, MessagingException {
 
         Integer attempts = 0;
 
         while(filesToSendInOtherMails.size() > 0 && attempts < 5) {
 
             MimeMessage message = this.mailSender.createMimeMessage();
 
             // use the true flag to indicate you need a multipart message
             MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
 
             helper.setTo(recipientAddress);
 
             if (options != null) {
 
                 String subject = options.get(Constants.SUBJECT);
 
                 if (StringUtils.isEmpty(subject)) {
 
                     subject = this.localSettingsProperties.getProperty(Constants.DEFAULT_SUBJECT);
                 }
 
                 helper.setSubject(subject);
 
 
                 String from = options.get(Constants.FROM);
 
                 if (StringUtils.isEmpty(from)) {
 
                     from = this.localSettingsProperties.getProperty(Constants.DEFAULT_FROM);
                 }
 
                 helper.setFrom(from);
             }
 
             helper.setText(Constants.MAIL_TEXT_OTHER_ATTACHMENTS);
 
             double totalSize = 0;
 
            Collection<File> filesToSendInNextMail = new HashSet<>();

             for(File file : filesToSendInOtherMails) {
 
                 totalSize = totalSize + file.length();
 
                 if(totalSize < Constants.MAX_ATTACHMENT_SIZE) {
 
                     helper.addAttachment(file.getName(), file);
 
                 }
                 else {
                    filesToSendInNextMail.add(file);
                 }
 
             }
 
            filesToSendInOtherMails = filesToSendInNextMail;

             this.mailSender.send(message);
 
             attempts ++;
 
         }
 
     }
 	
 	public MailSender getMailSender() {
 		return mailSender;
 	}
 
 	public void setMailSender(JavaMailSender mailSender) {
 		this.mailSender = mailSender;
 	}
 
 	public SimpleMailMessage getTemplateMessage() {
 		return templateMessage;
 	}
 
 	public void setTemplateMessage(SimpleMailMessage templateMessage) {
 		this.templateMessage = templateMessage;
 	}
 	
 	
 }
