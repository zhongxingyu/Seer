 package org.inigma.shared.message;
 
 import java.util.Collection;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.inigma.shared.webapp.BaseController;
 import org.inigma.shared.webapp.RestResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class MessageController extends BaseController {
     @Autowired
     private MessageDaoTemplate template;
 
     @RequestMapping(value = "/message", method = RequestMethod.DELETE)
     public RestResponse deleteMessage(Message message, HttpServletResponse response) {
         validateMessage(message);
         return template.delete(message.getCode(), message.getLocale());
     }
 
     @RequestMapping(value = "/message", method = RequestMethod.GET)
     public RestResponse getMessage(Message message, HttpServletResponse response) {
         validateMessage(message);
         return template.findById(message.getCode(), message.getLocale());
     }
 
     @RequestMapping(value = "/messages", method = RequestMethod.GET)
     public Collection<? extends RestResponse> getMessages(HttpServletResponse response) {
         return template.find();
     }
 
     @RequestMapping(value = "/message", method = { RequestMethod.POST, RequestMethod.PUT })
     public RestResponse updateMessage(Message message, HttpServletResponse response) {
         Errors errors = getErrors(message, "message");
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "value", "required");
         validateMessage(message, errors);
         template.save(message);
         return message;
     }
 
     private void validateMessage(Message message) {
         validateMessage(message, null);
     }
 
     private void validateMessage(Message message, Errors errors) {
         if (errors == null) {
             errors = getErrors(message, "message");
         }
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "code", "required");
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "locale", "required");
         if (message.getLocale() != null) {
             String[] tokens = message.getLocale().split("_", 3);
             Locale locale = new Locale(tokens[0]);
             switch (tokens.length) {
             case 1:
                 locale = new Locale(tokens[0]);
                 break;
             case 2:
                 locale = new Locale(tokens[0], tokens[1]);
                 break;
             case 3:
                 locale = new Locale(tokens[0], tokens[1], tokens[2]);
                 break;
             }
             message.setLocale(locale.toString());
         }
         stopOnErrors(errors);
     }
 }
