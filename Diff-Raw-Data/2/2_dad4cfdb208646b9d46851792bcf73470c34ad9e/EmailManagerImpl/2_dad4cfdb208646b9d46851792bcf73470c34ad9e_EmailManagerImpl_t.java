 package com.solidstategroup.radar.service.impl;
 
 import com.solidstategroup.radar.model.user.PatientUser;
 import com.solidstategroup.radar.model.user.ProfessionalUser;
 import com.solidstategroup.radar.model.user.User;
 import com.solidstategroup.radar.service.EmailManager;
 import com.solidstategroup.radar.web.RadarApplication;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 
 import javax.mail.Address;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 public class EmailManagerImpl implements EmailManager {
     private String emailAddressApplication;
     private String emailAddressAdmin1;
     private String emailAddressAdmin2;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(EmailManagerImpl.class);
     private JavaMailSender javaMailSender;
     private VelocityEngine velocityEngine;
 
     private boolean debug;
 
     public EmailManagerImpl() {
         try {
             velocityEngine = new VelocityEngine();
             // This sets Velocity to use our own logging implementation so we can use SLF4J
             velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, LOGGER);
             velocityEngine.setProperty("resource.loader", "class");
             velocityEngine.setProperty("class.resource.loader.description", "Classpath Loader");
             velocityEngine.setProperty("class.resource.loader.class",
                     "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
             // Init
             velocityEngine.init();
         } catch (Exception e) {
             LOGGER.error("Could not initialise velocity engine!", e);
         }
     }
 
     public void sendPatientRegistrationEmail(PatientUser patientUser, String password) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("patientUser", patientUser);
         map.put("password", password);
         String emailBody = renderTemplate(map, "patient-registration.vm");
         sendEmail(emailAddressApplication, new String[]{patientUser.getUsername()},
                 new String[]{emailAddressAdmin1}, "Your RaDaR website registration", emailBody);
     }
 
     public void sendPatientRegistrationReminderEmail(PatientUser patientUser) throws Exception {       
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("patientUser", patientUser);
         map.put("password", patientUser.getPassword());
         String emailBody = renderTemplate(map, "patient-registration-reminder.vm");
         if (!debug) {
             sendEmail(emailAddressApplication, new String[]{patientUser.getUsername()},
                     new String[]{emailAddressAdmin1}, "Registration reminder for the RADAR website", emailBody);
         }
     }
 
     public void sendPatientRegistrationAdminNotificationEmail(PatientUser patientUser) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("patientUser", patientUser);
         String emailBody = renderTemplate(map, "patient-registration-admin-notification.vm");
         sendEmail(emailAddressApplication, new String[]{emailAddressAdmin1, emailAddressAdmin2},
                 new String[]{emailAddressAdmin1}, "New Radar patient registrant on: " +
                 new SimpleDateFormat(RadarApplication.DATE_PATTERN).format(patientUser.getDateRegistered()),
                 emailBody);
     }
 
     public void sendProfessionalRegistrationAdminNotificationEmail(ProfessionalUser professionalUser) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("user", professionalUser);
         String emailBody = renderTemplate(map, "professional-registration-admin-notification.vm");
         sendEmail(emailAddressApplication, new String[]{emailAddressAdmin1, emailAddressAdmin2},
                 new String[]{emailAddressAdmin1}, "New Radar site registrant on: " +
                 new SimpleDateFormat(RadarApplication.DATE_PATTERN).format(professionalUser.getDateRegistered()),
                 emailBody);
     }
 
     public void sendForgottenPassword(PatientUser patientUser, String password) {
         sendPassword(patientUser, password);
     }
 
     public void sendForgottenPassword(ProfessionalUser professionalUser, String password) throws Exception {
         sendPassword(professionalUser, password);
     }
 
     private void sendPassword(User user, String password) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("password", password);
         map.put("isProfessionalUser", user instanceof ProfessionalUser);
         String emailBody = renderTemplate(map, "forgotten-password.vm");
         sendEmail(emailAddressApplication, new String[]{user.getUsername(), emailAddressAdmin1},
                 new String[]{emailAddressAdmin1}, "RADAR website password", emailBody);
     }
 
 
     // methods is public to allow for testing
     public void sendEmail(String from, String[] to, String[] bcc, String subject, String body) {
         MimeMessage message = javaMailSender.createMimeMessage();
         MimeMessageHelper messageHelper = null;
 
         try {
             messageHelper = new MimeMessageHelper(message, true);
             messageHelper.setTo(to);
             Address[] bccAddresses = new Address[bcc.length];
             for (int i = 0; i < bcc.length; i++) {
                 bccAddresses[i] = new InternetAddress(bcc[i]);
             }
             message.addRecipients(Message.RecipientType.BCC, bccAddresses);
             messageHelper.setFrom(from);
             messageHelper.setSubject(subject);
             messageHelper.setText(body, true);
 
             javaMailSender.send(messageHelper.getMimeMessage());
        } catch (Exception e) {
             LOGGER.error("Could send email", e);
         }
     }
 
     // this method is public to allow for testing
     public String renderTemplate(Map<String, Object> map, String template) {
         // build our context map
         VelocityContext velocityContext = new VelocityContext();
         for (Map.Entry<String, Object> entry : map.entrySet()) {
             velocityContext.put(entry.getKey(), entry.getValue());
         }
 
         // Try the renderTemplate, log any problems
         final Writer writer = new StringWriter();
         try {
             velocityEngine.mergeTemplate(template, velocityContext, writer);
         } catch (Exception e) {
             LOGGER.error("Could not renderTemplate template {}", "email", e);
         }
 
         return writer.toString();
     }
 
     public void setJavaMailSender(JavaMailSender javaMailSender) {
         this.javaMailSender = javaMailSender;
     }
 
     public String getEmailAddressApplication() {
         return emailAddressApplication;
     }
 
     public void setEmailAddressApplication(String emailAddressApplication) {
         this.emailAddressApplication = emailAddressApplication;
     }
 
     public String getEmailAddressAdmin1() {
         return emailAddressAdmin1;
     }
 
     public void setEmailAddressAdmin1(String emailAddressAdmin1) {
         this.emailAddressAdmin1 = emailAddressAdmin1;
     }
 
     public String getEmailAddressAdmin2() {
         return emailAddressAdmin2;
     }
 
     public void setEmailAddressAdmin2(String emailAddressAdmin2) {
         this.emailAddressAdmin2 = emailAddressAdmin2;
     }
 
     public void setDebug(boolean debug) {
         this.debug = debug;
     }
 }
