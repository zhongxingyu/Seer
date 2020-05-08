 package com.psddev.dari.util;
 
 /** Interface for sending a {@link MailMessage} **/
 public interface MailProvider extends SettingsBackedObject {
 
     /** Setting key for default mail provider name. */
     public static final String DEFAULT_MAIL_SETTING = "dari/defaultMailProvider";
 
     /** Setting key prefix for all mail provider configuration. */
     public static final String SETTING_PREFIX = "dari/mailProvider";
 
     /**
      * Sends mail given a {@code MailMessage}.
      *
      * @param emailMessage
      */
     public void send(MailMessage message);
 
     /**
      * {@link MailProvider} utility methods.
      *
      * <p>The factory method, {@link #getInstance}, uses {@link Settings}
      * to construct instances.</p>
      */
     public static final class Static {
 
        protected static final PullThroughCache<String, MailProvider>
                 INSTANCES = new PullThroughCache<String, MailProvider>() {
 
             @Override
             public MailProvider produce(String name) {
                 return Settings.newInstance(MailProvider.class, SETTING_PREFIX + "/" + name);
             }
         };
 
         private Static() {
         }
 
         /**
          * Returns the MailProvider instance associated with the given
          * {@code name}.
          *
          * @param name If blank, returns {@code null}.
          * @return May be {@code null}.
          */
         public static MailProvider getInstance(String name) {
             if (ObjectUtils.isBlank(name)) {
                 name = Settings.get(String.class, DEFAULT_MAIL_SETTING);
 
                 if (ObjectUtils.isBlank(name)) {
                     throw new IllegalStateException(
                         "No default mail provider setting found!");
                 }
             }
 
             return INSTANCES.get(name);
         }
 
         /**
          * Returns the default configured mail provider.
          *
          * @return
          */
         public static MailProvider getDefault() {
             return getInstance(null);
         }
     }
 }
