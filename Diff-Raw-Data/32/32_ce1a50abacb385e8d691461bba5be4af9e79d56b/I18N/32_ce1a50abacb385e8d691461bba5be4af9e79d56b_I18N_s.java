 /*
  * Copyright (C) 2012 Klaus Reimer <k@ailis.de>
  * See LICENSE.md for licensing information.
  */
 package de.ailis.jasdoc.util;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import de.ailis.jasdoc.Main;
 
 /**
  * Simple internationalization utility class.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 public final class I18N
 {
     /** The messages. */
     private static final ResourceBundle MESSAGES = ResourceBundle
         .getBundle(Main.class.getPackage().getName() + ".messages");
 
     /** The custom messages. */
     private static ResourceBundle customMessages;
 
     static
     {
         try
         {
             customMessages = ResourceBundle.getBundle("messages");
         }
         catch (final MissingResourceException e)
         {
             customMessages = null;
         }
     }
 
     /**
     * Private constructor to prevent instantiation
      */
     private I18N()
     {
         // Empty
     }
 
     /**
      * Returns the message resource with the specified key. If not found then
      * null is returned.
      *
      * @param key
      *            The message resource key
      * @return The message resource value or null if not found
      */
     private static String get(final String key)
     {
         try
         {
             if (customMessages != null && customMessages.containsKey(key))
                 return customMessages.getString(key);
             return MESSAGES.getString(key);
         }
         catch (final MissingResourceException e)
         {
             return null;
         }
     }
 
     /**
      * Returns the message resource with the specified key. If not found then a
      * special string is returned indicating the missing message resource.
      *
      * @param key
      *            The message resource key
      * @param args
      *            Message arguments
      * @return The message resource value
      */
     public static String getString(final String key, final Object... args)
     {
         final String value = get(key);
         if (value == null) return "???" + key + "???";
         return String.format(value, args);
     }
 }
