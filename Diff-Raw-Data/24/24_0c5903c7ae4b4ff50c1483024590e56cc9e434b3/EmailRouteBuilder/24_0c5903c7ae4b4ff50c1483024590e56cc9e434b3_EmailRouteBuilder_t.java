 package com.aciertoteam.mail.routers;
 
 import static com.aciertoteam.mail.EmailForm.ImageAttachment;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import javax.activation.DataHandler;
 import javax.mail.util.ByteArrayDataSource;
 import com.aciertoteam.common.utils.FtlReader;
 import com.aciertoteam.mail.EmailForm;
 import com.aciertoteam.mail.EmailResultCallback;
 import com.aciertoteam.mail.dto.NotificationDTO;
 import com.aciertoteam.mail.entity.MailConfiguration;
 import com.aciertoteam.mail.entity.MailTemplate;
 import com.aciertoteam.mail.exceptions.EmptyMessageBodyException;
 import com.aciertoteam.mail.services.MailConfigurationService;
 import org.apache.camel.Exchange;
 import org.apache.camel.Message;
 import org.apache.camel.Processor;
 import org.apache.camel.ProducerTemplate;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.MessageSource;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public class EmailRouteBuilder extends RouteBuilder {
 
     @Autowired
     private MailConfigurationService configurationService;
 
     private String user;
     private String password;
     private int port;
     private String host;
 
     private FtlReader ftlReader = new FtlReader("classpath:/com/aciertoteam/ftl/");
 
     @Autowired
     @Qualifier(value = "emailMessageSource")
     private MessageSource messageSource;
 
     @Value("${send.email.endpoint}")
     private String sendEmailEndpoint;
 
     @Value("${email.ssl.enabled}")
     private boolean emailSslEnabled;
 
     @Override
     public void configure() throws Exception {
 
         init();
 
         from(sendEmailEndpoint).process(new Processor() {
             @Override
             public void process(Exchange exchange) throws Exception {
                 MessageContainer messageContainer = exchange.getIn().getBody(MessageContainer.class);
                 ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();
                 sendMessage(producerTemplate, exchange, messageContainer);
             }
 
             private void sendMessage(ProducerTemplate producerTemplate, Exchange exchange,
                     MessageContainer messageContainer) {
                 try {
                     populateMessageBeforeSending(exchange.getIn(), getEmailForm(messageContainer));
 
                     producerTemplate.send(createSmtpEndpoint(), exchange);
                     messageContainer.getCallback().notifyThatMessageIsReceived();
                 } catch (Exception e) {
                     messageContainer.getCallback().notifyAboutFailedDelivery(e.getMessage());
                 }
             }
         });
     }
 
     private EmailForm getEmailForm(MessageContainer messageContainer) {
         NotificationDTO notification = messageContainer.getNotification();
         return createEmailForm(notification);
     }
 
     private void populateMessageBeforeSending(Message message, EmailForm emailForm) {
         message.setBody(emailForm.getBodyText());
         addAttachments(message, emailForm);
         addedMailProperties(message, emailForm);
     }
 
     private void addAttachments(Message message, EmailForm emailForm) {
         for (ImageAttachment attachment : emailForm.getImageAttachments()) {
             message.addAttachment(attachment.getImageTemplateId(), getDataHandler(attachment));
         }
     }
 
     private DataHandler getDataHandler(ImageAttachment attachment) {
         try {
             return new DataHandler(new ByteArrayDataSource(attachment.getInputStream(), attachment.getMimeType()));
         } catch (Exception e) {
             log.error(e.getMessage(), e);
         }
         return null;
     }
 
     private String createSmtpEndpoint() {
         return "smtps://" + host + "?username=" + user + "@gmail.com&password=" + password
                 + (emailSslEnabled ? "&sslContextParameters=#sslContextParameters" : "");
     }
 
     private EmailForm createEmailForm(NotificationDTO notification) {
        final MailTemplate mailTemplate = new MailTemplate(notification.getTemplateName(), notification.getSubjectName());
         final EmailForm emailForm = new EmailForm();
         final String body = ftlReader.read(mailTemplate.getTemplateName(), notification.getProperties(),
                 Locale.getDefault());
 
         emailForm.setTo(notification.getTo());
         emailForm.setSubject(getSubject(mailTemplate));
         emailForm.addImageAttachments(notification.getAttachments());
 
         checkEmailBody(notification.getId(), body);
 
         emailForm.setBodyText(body);
 
         return emailForm;
     }
 
     private String getSubject(MailTemplate mailTemplate) {
         return messageSource.getMessage(mailTemplate.getSubjectName(), new Object[] {}, Locale.getDefault());
     }
 
     private void checkEmailBody(Long notificationId, String body) {
         if (StringUtils.isBlank(body)) {
             String msg = String.format("The exception has been occurred. Body for notification [id = %s] is blank ",
                     notificationId);
             log.error(msg);
             throw new EmptyMessageBodyException();
         }
     }
 
     private void addedMailProperties(Message message, EmailForm emailForm) {
         Map<String, Object> map = new HashMap<String, Object>();
 
         map.put("Port", port);
         map.put("Host", host);
         map.put("Username", user);
         map.put("Password", password);
 
         map.put("From", user + "@gmail.com");
         map.put("To", emailForm.getRecipients());
         map.put("Subject", emailForm.getSubject());
         map.put("ContentType", "text/html;charset=UTF-8");
 
         message.setHeaders(map);
     }
 
     private void init() {
         user = getValue("mail.user");
         password = getValue("mail.password");
         port = getIntValue("mail.port");
         host = getValue("mail.host");
         getValue("mail.starttls.enable");
         getValue("mail.auth");
     }
 
     private Integer getIntValue(String configKey) {
         String value = getValue(configKey);
         return value == null ? 0 : Integer.valueOf(value);
     }
 
     private String getValue(String configKey) {
         MailConfiguration config = configurationService.getConfig(configKey);
         return config == null ? null : config.getValue();
     }
 
     /**
      * @author bogdan
      */
     public static class MessageContainer implements Serializable {
 
         private static final long serialVersionUID = 1L;
 
         private final NotificationDTO notification;
 
         private final EmailResultCallback callback;
 
         public MessageContainer(NotificationDTO notification, EmailResultCallback callback) {
             this.notification = notification;
             this.callback = callback;
         }
 
         public NotificationDTO getNotification() {
             return notification;
         }
 
         public EmailResultCallback getCallback() {
             return callback;
         }
     }
 }
