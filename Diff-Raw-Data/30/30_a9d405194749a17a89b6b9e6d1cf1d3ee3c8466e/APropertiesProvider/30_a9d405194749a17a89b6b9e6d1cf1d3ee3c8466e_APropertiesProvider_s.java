 /*
  * Copyright (C) 2011-2012  Christian Roesch
  * 
  * This file is part of micro-debug.
  * 
  * micro-debug is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * micro-debug is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with micro-debug.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.croesch.micro_debug.properties;
 
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.github.croesch.micro_debug.annotation.NotNull;
 import com.github.croesch.micro_debug.annotation.Nullable;
 
 /**
  * Provides properties for different files.
  * 
  * @author croesch
  * @since Date: Feb 23, 2012
  */
 public abstract class APropertiesProvider {
 
   /** contains the properties stored in the map with their base file names */
   @NotNull
   private final Map<String, Properties> propertiesMap = new ConcurrentHashMap<String, Properties>();
 
   /**
    * Replaces all placeholders in the given string, with the given arguments.
    * 
    * @since Date: Feb 22, 2012
    * @param str the string to replace the placeholders in
    * @param args the arguments
    * @return the string filled with arguments
    */
   @NotNull
   public static String replacePlaceholdersInString(final String str, final Object ... args) {
     String text = str;
     if (args != null) {
       for (int i = 0; i < args.length; ++i) {
         text = replacePlaceholderInString(i, text, args[i]);
       }
     }
     return replaceEscapedPlaceholder(text);
   }
 
   /**
    * Returns the value for the given key in the given file.
    * 
    * @since Date: Feb 22, 2012
    * @param file the path of the file, <b>without</b> file extension!
    * @param key the key to fetch the value for
    * @return the value read from the xml properties,<br>
    *         or <code>null</code> if the given file path or key is <code>null</code>.
    */
   @Nullable
   public final String get(final String file, final String key) {
     if (file == null || key == null) {
       return null;
     }
    final String preparedKey = key.toLowerCase(Locale.GERMAN).replace('_', '-');
     return getProperties(file).getProperty(preparedKey);
   }
 
   /**
    * Returns the properties belonging to the given file path.
    * 
    * @since Date: Feb 22, 2012
    * @param file the path to the file of the properties
    * @return the properties belonging to the given file path.
    */
   @Nullable
   private Properties getProperties(final String file) {
     if (!this.propertiesMap.containsKey(file)) {
       this.propertiesMap.put(file, createNewProperties(file));
     }
     return this.propertiesMap.get(file);
   }
 
   /**
    * Create the properties for the given file.
    * 
    * @since Date: Feb 23, 2012
    * @param file the path of the file, <b>without</b> file extension!
    * @return {@link Properties} that contain the content of the given file
    */
   @NotNull
   protected abstract Properties createNewProperties(final String file);
 
   /**
    * Replaces the placeholder with the given number in the given string, with the given argument.
    * 
    * @since Date: Feb 22, 2012
    * @param number the number of the placeholder and argument
    * @param str the string to replace the placeholders in
    * @param arg the argument
    * @return the string with the placeholder replaced
    */
   @NotNull
   private static String replacePlaceholderInString(final int number, final String str, final Object arg) {
     final String preparedArgument = prepareArgument(arg);
     return str.replaceAll("(^|[^{])\\{" + number + "\\}", "$1" + preparedArgument);
   }
 
   /**
    * Unescapes escaped placeholders in the given string.
    * 
    * @since Date: Feb 22, 2012
    * @param str the string, maybe contains escaped placeholders
    * @return string with unescaped placeholders.
    */
   @NotNull
   private static String replaceEscapedPlaceholder(final String str) {
     return str.replaceAll("\\{\\{(\\d)\\}", "\\{$1\\}");
   }
 
   /**
    * Prepares the given argument for being a replacement.
    * 
    * @since Date: Feb 22, 2012
    * @param arg the argument to prepare
    * @return the prepared argument
    */
   @NotNull
   private static String prepareArgument(final Object arg) {
     if (arg == null || arg.toString() == null) {
       return "null";
     }
     // prevent exceptions with using $
     return arg.toString().replaceAll("\\$", "\\\\\\$");
   }
 }
