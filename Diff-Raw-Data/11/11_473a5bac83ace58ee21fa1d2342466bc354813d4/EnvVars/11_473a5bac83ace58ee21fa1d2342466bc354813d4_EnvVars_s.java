 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.core;
 
 import org.apache.commons.lang3.Validate;
 
 /**
  * Used environment variables.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public enum EnvVars {
 
     /**
      * Environment variable $STAGE.
      */
     STAGE,
     /**
      * Environment variable $HOME.
      */
     HOME;
     /**
      * Provider to get environment variables.
      */
    private static EnvVarProvider PROVIDER = new DefaultProvider();
 
     /**
      * Set the provider.
      *
      * @param p must not be {@code null}
      */
    public static void setProvider(final EnvVarProvider p) {
         Validate.notNull(p, "Provider must not be null!");
        PROVIDER = p;
     }
 
     /**
      * Retrieves the environment variable from the system.
      *
      * @return always returns string, may be an empty string but never {@code null}
      */
     public String getValue() {
        return PROVIDER.getenv(name());
     }
 
     /**
      * Interface for a provider which get environment variables.
      */
     interface EnvVarProvider {
 
         /**
          * Get the environment variable for a name.
          *
          * @param name must nit be {@code null} or empty
          * @return never {@code null}
          */
         String getenv(String name);
     }
 
     /**
      * Default implementation which utilizes {@link System#getenv(java.lang.String)}.
      */
     static class DefaultProvider implements EnvVarProvider {
 
         @Override
         public String getenv(final String name) {
             Validate.notEmpty(name);
             final String env = System.getenv(name);
             return null == env ? "" : env;
         }
     }
 }
