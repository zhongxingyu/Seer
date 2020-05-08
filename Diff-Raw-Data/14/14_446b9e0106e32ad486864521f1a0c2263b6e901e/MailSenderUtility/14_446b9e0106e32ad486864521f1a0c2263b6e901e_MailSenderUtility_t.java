 package it.sevenbits.conferences.utils.mail;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.stereotype.Service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 /**
  * This class provide tools for working with mail.
  */
 @Service("emailService")
 public class MailSenderUtility {
 
     private static final String SERVICE_MAILBOX = "no-reply@omskit.org";
 
     private static final String REGISTRATION_INFO_TEXT = "Чтобы подтвердить регистрацию на нашем сайте, пройдите по ссылке: ";
 
     private static final String TEMPORARY_PASSWORD_TEXT = "" +
             "Вам выслан временный пароль. Пройдите, пожалуйста, по указанной ссылке и смените его на новый."
     ;
 
     private final Logger logger = Logger.getLogger(MailSenderUtility.class);
 
    private String domain;
 
     @Autowired
     private JavaMailSender mailSender;
 
     public MailSenderUtility() {
        this.domain = getSiteDomain();
     }
 
     /**
      * Send common message.
      */
     private void sendMail(final String to, final String subject, final String msg) {
         SimpleMailMessage message = new SimpleMailMessage();
         message.setFrom(SERVICE_MAILBOX);
         message.setTo(to);
         message.setSubject(subject);
         message.setText(msg);
         mailSender.send(message);
     }
 
     /**
      * Send message with link for confirmation account.
      * @param login Users login
      * @param confirmationToken Users confirmation token
      */
     public void sendConfirmationToken(final String login, final String confirmationToken) {
         SimpleMailMessage message = new SimpleMailMessage();
         message.setFrom(SERVICE_MAILBOX);
         message.setTo(login);
         message.setSubject("Подтверждение регистрации");
        String confirmationUrl = domain + "user/confirmation?confirmation_token=" +
                 confirmationToken + "&confirmation_login=" + login
         ;
         message.setText(REGISTRATION_INFO_TEXT + confirmationUrl);
         mailSender.send(message);
     }
 
     /**
      * Send message with link for confirmation account and registration for current conference.
      * @param email Users email
      * @param confirmationToken Users confirmation token
      */
     public void sendConfirmationTokenAndConferenceStatus(final String email, final String confirmationToken) {
         SimpleMailMessage message = new SimpleMailMessage();
         message.setFrom(SERVICE_MAILBOX);
         message.setTo(email);
         message.setSubject("Подтверждение регистрации");
        String confirmationUrl = domain + "user/confirmation?confirmation_token=" +
                 confirmationToken + "&confirmation_login=" + email + "&conference_status=1"
         ;
         message.setText(REGISTRATION_INFO_TEXT + confirmationUrl + " Ждем вас в субботу");
         mailSender.send(message);
     }
 
     /**
      * Send message with link for confirmation account and consideration user as reporter.
      * @param email User's email
      * @param confirmationToken Users confirmation token
      */
     public void sendConfirmationTokenAndReportStatus(final String email, final String confirmationToken, final Long reportId) {
         SimpleMailMessage message = new SimpleMailMessage();
         message.setFrom(SERVICE_MAILBOX);
         message.setTo(email);
         message.setSubject("Подтверждение регистрации");
        String confirmationUrl = domain + "user/confirmation?confirmation_token=" +
                 confirmationToken + "&confirmation_login=" + email + "&report_status=" + reportId
         ;
         message.setText(REGISTRATION_INFO_TEXT + confirmationUrl + " Ваша заявка на доклад будет рассмотрена.");
         mailSender.send(message);
     }
 
     /**
      * Send message with new password
      * @param email User's email
      * @param password User's new password
      */
     public void sendNewPassword(final String email, final String password) {
         SimpleMailMessage message = new SimpleMailMessage();
         message.setFrom(SERVICE_MAILBOX);
         message.setTo(email);
         message.setSubject("Восстановление пароля");
         message.setText(TEMPORARY_PASSWORD_TEXT + " " + password);
         mailSender.send(message);
     }
 
     private String getSiteDomain() {
         Properties prop = new Properties();
         try {
             InputStream inStream = getClass().getClassLoader().getResourceAsStream("common.properties");
             prop.load(inStream);
             inStream.close();
         } catch (IOException e) {
             logger.error("Cannot read filesUpload.properties file", e);
         }
         return prop.getProperty("site.domain");
     }
 }
