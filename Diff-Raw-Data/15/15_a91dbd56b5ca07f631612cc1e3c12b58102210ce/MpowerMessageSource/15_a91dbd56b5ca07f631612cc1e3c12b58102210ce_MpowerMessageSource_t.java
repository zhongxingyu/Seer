 package com.mpower.controller.validator;
 
 import java.text.MessageFormat;
 import java.util.Locale;
 
 import org.springframework.context.ResourceLoaderAware;
 import org.springframework.context.support.AbstractMessageSource;
 import org.springframework.core.io.ResourceLoader;
 
 import com.mpower.service.SessionServiceImpl;
 import com.mpower.service.customization.MessageService;
 import com.mpower.type.MessageResourceType;
 
 public class MpowerMessageSource extends AbstractMessageSource implements ResourceLoaderAware {
 
     private MessageService messageService;
 
     public void setMessageService(MessageService messageService) {
         this.messageService = messageService;
     }
 
     @Override
     protected MessageFormat resolveCode(String code, Locale locale) {
         String message = messageService.lookupMessage(SessionServiceImpl.lookupUserSiteName(), MessageResourceType.FIELD_VALIDATION, code, locale);
         if (message == null && (code.startsWith("fieldRequiredFailure.") || code.startsWith("fieldValidationFailure."))) {
            logger.info("message code, " + code + ", not found - use out-of-the-box error message");
             message = messageService.lookupMessage(SessionServiceImpl.lookupUserSiteName(), MessageResourceType.FIELD_VALIDATION, code.substring(0, code.indexOf('.')), locale);
         }
         return createMessageFormat(message, locale);
     }
 
     @Override
     public void setResourceLoader(ResourceLoader resourceLoader) {
     }
 }
