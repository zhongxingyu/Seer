 package com.psddev.dari.util;
 
 /** Policy for {@link Password} creation. */
 public interface PasswordPolicy extends SettingsBackedObject {
 
     /** Setting key for all password policy configurations. */
     public static final String SETTING_PREFIX = "dari/passwordPolicy";
 
     /**
      * Validates the given {@code password}.
      *
      * @param password Can be {@code null}.
      * @throws PasswordException If anything's wrong with the given
      * {@code password}.
      */
     public void validate(String password) throws PasswordException;
 
     /**
      * {@link PasswordPolicy} utility methods.
      *
      * <p>The factory method, {@link #getInstance}, uses {@link Settings}
      * to construct instances.</p>
      */
     public static final class Static {
 
        protected static final PullThroughCache<String, PasswordPolicy>
                 INSTANCES = new PullThroughCache<String, PasswordPolicy>() {
 
             @Override
             public PasswordPolicy produce(String name) {
                 return Settings.newInstance(PasswordPolicy.class, SETTING_PREFIX + "/" + name);
             }
         };
 
         private Static() {
         }
 
         /**
          * Returns the password policy associated with the given
          * {@code name}.
          *
          * @param name If blank, returns {@code null}.
          * @return May be {@code null}.
          */
         public static PasswordPolicy getInstance(String name) {
             return ObjectUtils.isBlank(name) ? null : INSTANCES.get(name);
         }
     }
 }
