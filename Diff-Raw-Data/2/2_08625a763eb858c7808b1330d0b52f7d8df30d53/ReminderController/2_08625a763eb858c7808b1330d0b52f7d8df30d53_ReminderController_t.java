 package com.digt.web;
 
 import com.digt.util.MailUtils;
 import com.digt.util.PwdUtils;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.logging.Logger;
 
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMessage.RecipientType;
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.digt.web.beans.ReminderBean;
 import com.digt.web.beans.json.Message;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.util.logging.Level;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.servlet.ServletContext;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.rave.model.User;
 import org.apache.rave.portal.repository.UserRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.FieldError;
 import org.springframework.context.MessageSource;
 
 @Controller
 public class ReminderController {
 
     @Autowired
     private UserRepository userRepository;
     
     @Autowired
     private Session mailSession;
     
     @Autowired
     ServletContext  ctx;
     
     @Autowired
     private MessageSource messages;
     
 	private static final Logger LOG = Logger.getLogger(
 			ReminderController.class.getName());
 	
 	@RequestMapping(value = "/reminder", method = RequestMethod.GET)
 	public String showReminder(HttpServletRequest request, Model model,
 								@ModelAttribute("formBean") ReminderBean form)
 	{
 		//model.addAttribute("recaptcha", reCaptcha);
 		return "reminder";
 	}
 	
 	@Transactional
     @RequestMapping(value = "/reminder", method = RequestMethod.POST)
 	public String sendReminder(HttpServletRequest request, Model model,
 								//@RequestParam("recaptcha_challenge_field") String challengeField, 
 								//@RequestParam("recaptcha_response_field") String responseField,
 								@ModelAttribute("formBean") ReminderBean form,
 								BindingResult result) throws MessagingException, UnsupportedEncodingException, IOException
 	{
         //String remoteAddress = request.getRemoteAddr();
         //model.addAttribute("recaptcha", reCaptcha);
 
         String email = form.getEmail();
         User user = userRepository.getByUserEmail(email);
         
         if (user == null) {
             FieldError fieldError = new FieldError(
                     "formBean",
                     "email",
                     messages.getMessage("reminder.email.notexist", null, null));
                     result.addError(fieldError);
         }
         
         else
         {
             MimeMessage msg = new MimeMessage(mailSession);
             msg.setRecipient(RecipientType.TO, new InternetAddress(email));
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(ctx.getResourceAsStream("/WEB-INF/tpl/send_reminder.tpl"), MailUtils.MAIL_CHARSET));
             msg.setSubject(in.readLine(), MailUtils.MAIL_CHARSET);
             String password = RandomStringUtils.randomAlphanumeric(8);
             try {
                 user.setPassword(new String(PwdUtils.cryptPassword(password.toCharArray())));
             } catch (NoSuchAlgorithmException ex) {
                 LOG.log(Level.SEVERE, null, ex);
             }
             userRepository.save(user);
             msg.setContent(MailUtils.readTemplate(in)
                     .replaceAll("\\{\\$firstname\\}", user.getGivenName())
                     .replaceAll("\\{\\$lastname\\}", user.getFamilyName())
                     .replaceAll("\\{\\$password\\}", password)
                    .replaceAll("\\{\\$login\\}", user.getUsername()),
                     "text/html;charset="+MailUtils.MAIL_CHARSET);
             Transport.send(msg);
             model.addAttribute("message", 
                     new Message(true, user.getEmail()));
             return "remindDone";
         }
         
 		return "reminder";
 	}
 	
 }
