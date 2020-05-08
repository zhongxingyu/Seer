 package com.financial.pyramid.service.impl;
 
 import com.financial.pyramid.domain.User;
 import com.financial.pyramid.service.EmailService;
 import com.financial.pyramid.service.LocalizationService;
import com.financial.pyramid.service.SettingsService;
 import com.financial.pyramid.service.validators.EmailValidator;
import com.financial.pyramid.settings.Setting;
 import org.apache.log4j.Logger;
 import org.apache.velocity.app.VelocityEngine;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.mail.MailException;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.stereotype.Service;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * User: Danil
  * Date: 06.06.13
  * Time: 20:37
  */
 @Service("emailService")
 public class EmailServiceImpl implements EmailService {
 
     private final static Logger logger = Logger.getLogger(EmailServiceImpl.class);
 
     @Autowired
     private JavaMailSender mailSender;
 
     @Autowired
     private VelocityEngine velocityEngine;
 
     @Autowired
     private LocalizationService localizationService;
 
    @Autowired
    private SettingsService settingsService;

     private EmailValidator emailValidator = new EmailValidator();
 
     private String template;
 
     @Override
     public boolean sendEmail(User user, Map model) {
         try {
             String projectName = localizationService.translate("projectName");
             MimeMessage message = mailSender.createMimeMessage();
             MimeMessageHelper helper = new MimeMessageHelper(message);
             helper.setTo(user.getEmail());
             if (this.template == null) {
                 throw new RuntimeException("Template is not set");
             }
             String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, this.template, "UTF-8", model);
             String subject = model.get("subject").toString();
             helper.setText(text, true);
             helper.setSubject(projectName + ": " + subject);
             this.mailSender.send(message);
         } catch (MailException ex) {
             logger.error(ex.getMessage());
             return false;
         } catch (MessagingException ex) {
             logger.error(ex.getMessage());
             return false;
         }
         return true;
     }
 
     public void setTemplate(String template) {
         Locale locale = LocaleContextHolder.getLocale();
         this.template = template + "-" + locale.toString() + ".vm";
     }
 
     public String getTemplate() {
         return template;
     }
 
     @Override
     public boolean sendInvitation(String uuid, String name, String email) {
         Map<String, Object> model = new HashMap<String, Object>(5);
         model.put("name", name);
         model.put("uuid", uuid);
        model.put("host", settingsService.getProperty(Setting.APPLICATION_URL));
         model.put("email", email);
         model.put("subject", localizationService.translate("invitationToBusiness"));
         setTemplate("invitation-template");
         return sendEmail(model);
     }
 
     @Override
     public boolean sendPassword(String name, String password, String email) {
         Map<String, Object> model = new HashMap<String, Object>(3);
         model.put("name", name);
         model.put("password", password);
        model.put("host", settingsService.getProperty(Setting.APPLICATION_URL));
         model.put("email", email);
         model.put("subject", localizationService.translate("passwordIsReady"));
         setTemplate("password-template");
         return sendEmail(model);
     }
 
     @Override
     public boolean checkEmail(String email) {
         if (!emailValidator.validate(email)) {
             logger.warn("Email " + email + " is not valid");
             return false;
         }
         return true;
     }
 
     private boolean sendEmail(Map<String, Object> model) {
         try {
             String projectName = localizationService.translate("projectName");
             String subject = (String) model.get("subject");
             MimeMessage message = mailSender.createMimeMessage();
             MimeMessageHelper helper = new MimeMessageHelper(message);
             helper.setTo((String) model.get("email"));
             String text = VelocityEngineUtils.mergeTemplateIntoString(
                     velocityEngine, this.getTemplate(), "UTF-8", model);
             helper.setText(text, true);
             helper.setSubject(projectName + ": " + subject);
             this.mailSender.send(message);
         } catch (MailException ex) {
             logger.error(ex.getMessage());
             return false;
         } catch (MessagingException ex) {
             logger.error(ex.getMessage());
             return false;
         }
         return true;
     }
 }
