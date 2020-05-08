 /*
 * Copyright 2010-2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package griffon.plugins.i18n;
 
 import griffon.util.ApplicationHolder;
 
 import java.util.List;
 import java.util.Locale;
 
 /**
  * @author Andres Almiray
  */
 public class DelegatingMessageSource implements MessageSource {
     private final org.springframework.context.MessageSource messageSource;
     private static final Object[] EMPTY_ARGS = new Object[0];
 
     public DelegatingMessageSource(org.springframework.context.MessageSource messageSource) {
         this.messageSource = messageSource;
     }
 
     public org.springframework.context.MessageSource getMessageSource() {
         return messageSource;
     }
 
     private Locale locale() {
         return ApplicationHolder.getApplication().getLocale();
     }
 
     public String getMessage(String s, Object[] objects, String s1, Locale locale) {
         return messageSource.getMessage(s, objects, s1, locale);
     }
 
     public String getMessage(String s, Object[] objects, Locale locale) throws NoSuchMessageException {
         try {
             return messageSource.getMessage(s, objects, locale);
         } catch (org.springframework.context.NoSuchMessageException e) {
             throw new NoSuchMessageException(s, locale);
         }
     }
 
     public String getMessage(String key) throws NoSuchMessageException {
         try {
             return messageSource.getMessage(key, EMPTY_ARGS, locale());
         } catch (org.springframework.context.NoSuchMessageException e) {
             throw new NoSuchMessageException(key, locale());
         }
     }
 
     public String getMessage(String key, String defaultMessage) {
         return messageSource.getMessage(key, EMPTY_ARGS, defaultMessage, locale());
     }
 
     public String getMessage(String key, Locale locale) throws NoSuchMessageException {
         try {
             return messageSource.getMessage(key, EMPTY_ARGS, locale);
         } catch (org.springframework.context.NoSuchMessageException e) {
             throw new NoSuchMessageException(key, locale);
         }
     }
 
     public String getMessage(String key, String defaultMessage, Locale locale) {
         return messageSource.getMessage(key, EMPTY_ARGS, defaultMessage, locale);
     }
 
    public String getMessage(String key, List<Object> args) throws NoSuchMessageException {
         try {
             return messageSource.getMessage(key, args.toArray(new Object[args.size()]), locale());
         } catch (org.springframework.context.NoSuchMessageException e) {
             throw new NoSuchMessageException(key, locale());
         }
     }
 
    public String getMessage(String key, List<Object> args, String defaultMessage) {
         return messageSource.getMessage(key, args.toArray(new Object[args.size()]), defaultMessage, locale());
     }
 
    public String getMessage(String key, List<Object> args, Locale locale) throws NoSuchMessageException {
         try {
             return messageSource.getMessage(key, args.toArray(new Object[args.size()]), locale);
         } catch (org.springframework.context.NoSuchMessageException e) {
             throw new NoSuchMessageException(key, locale);
         }
     }
 
    public String getMessage(String key, List<Object> args, String defaultMessage, Locale locale) {
         return messageSource.getMessage(key, args.toArray(new Object[args.size()]), defaultMessage, locale);
     }
 
     public String getMessage(String key, Object[] args) throws NoSuchMessageException {
         try {
             return messageSource.getMessage(key, args, locale());
         } catch (org.springframework.context.NoSuchMessageException e) {
             throw new NoSuchMessageException(key, locale());
         }
     }
 
     public String getMessage(String key, Object[] args, String defaultMessage) {
         return messageSource.getMessage(key, args, defaultMessage, locale());
     }
 }
