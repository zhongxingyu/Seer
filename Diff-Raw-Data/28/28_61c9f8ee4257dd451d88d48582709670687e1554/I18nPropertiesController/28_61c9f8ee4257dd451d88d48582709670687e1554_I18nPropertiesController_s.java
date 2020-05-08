 package oshop.web.ui;
 
 import org.springframework.context.MessageSource;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.context.support.DelegatingMessageSource;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestHeader;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import oshop.i18n.ExposedResourceBundleMessageSource;
 import oshop.web.api.rest.adapter.HttpCacheRestCallbackAdapter;
 
 import javax.annotation.Resource;
 import java.util.Map;
 
 @Controller
 @RequestMapping(value = "/i18n")
 public class I18nPropertiesController {
 
     @Resource(name = "messageSource")
     private MessageSource messageSource;
 
     @RequestMapping(
            value = "/messages",
             method = RequestMethod.GET,
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     public ResponseEntity<?> getAllMessages(
             final @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSinceHeader) throws Exception {
 
         return new HttpCacheRestCallbackAdapter<Map<String, String>>() {
 
             ExposedResourceBundleMessageSource exposedMessageSource;
 
             @Override
             protected void setModifiedTimes() throws Exception {
                 exposedMessageSource = getExposedResourceBundleMessageSource(messageSource);
 
                 long cacheSeconds = exposedMessageSource.getCacheSeconds();
                 if (cacheSeconds > 0) {
                     this.setMaxAge(cacheSeconds);
                 }
 
                 this.setLastModified(exposedMessageSource.getLastModifiedTime());
                 this.setIfModifiedSince(ifModifiedSinceHeader);
             }
 
             @Override
             protected Map<String, String> getResult() throws Exception {
                return exposedMessageSource.getAllMessages(LocaleContextHolder.getLocale());
             }
         }.invoke();
     }
 
     @RequestMapping(
            value = "/messages/{basename}",
             method = RequestMethod.GET,
             produces = {MediaType.APPLICATION_JSON_VALUE})
     @ResponseBody
     public ResponseEntity<?> getMessagesForBaseName(
             final @PathVariable String basename,
             final @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSinceHeader) throws Exception {
 
         return new HttpCacheRestCallbackAdapter<Map<String, String>>() {
 
             ExposedResourceBundleMessageSource exposedMessageSource;
 
             @Override
             protected void setModifiedTimes() throws Exception {
                 exposedMessageSource = getExposedResourceBundleMessageSource(messageSource);
 
                 long cacheSeconds = exposedMessageSource.getCacheSeconds();
                 if (cacheSeconds > 0) {
                     this.setMaxAge(cacheSeconds);
                 }
 
                 this.setLastModified(exposedMessageSource.getLastModifiedTime());
                 this.setIfModifiedSince(ifModifiedSinceHeader);
             }
 
             @Override
             protected Map<String, String> getResult() throws Exception {
                return exposedMessageSource.getAllMessagesForBaseName(basename, LocaleContextHolder.getLocale());
             }
         }.invoke();
     }
 
     private ExposedResourceBundleMessageSource getExposedResourceBundleMessageSource(MessageSource messageSource) {
         ExposedResourceBundleMessageSource exposedResourceBundleMessageSource = null;
 
         if (messageSource instanceof ExposedResourceBundleMessageSource) {
             exposedResourceBundleMessageSource = (ExposedResourceBundleMessageSource) messageSource;
         } else if (messageSource instanceof DelegatingMessageSource) {
             MessageSource parentMessageSource = ((DelegatingMessageSource) messageSource).getParentMessageSource();
 
             if (parentMessageSource instanceof ExposedResourceBundleMessageSource) {
                 exposedResourceBundleMessageSource = (ExposedResourceBundleMessageSource) parentMessageSource;
             } else {
                 throw new IllegalStateException("Can not resolve proper message bundle");
             }
         }
 
         return exposedResourceBundleMessageSource;
     }
 }
