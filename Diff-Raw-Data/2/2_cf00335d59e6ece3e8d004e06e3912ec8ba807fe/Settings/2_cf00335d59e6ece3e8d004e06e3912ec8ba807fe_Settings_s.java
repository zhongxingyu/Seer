 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.  
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or 
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)  
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.  
  */
 
 
 package org.webmacro.util;
 import org.webmacro.InitException;
 import java.util.*;
 import java.io.*;
 import java.net.URL;
 
 public class Settings {
 
    Properties _props;
    String _prefix;
 
    /**
      * Create an empty Settings object
      */
    public Settings() {
       _props = new Properties();
       _prefix = null;
    }
 
    /**
      * Search for the named settingsFile on the classpath 
      * and instantiate a Settings object based on its values
      */
    public Settings(String settingsFile) 
       throws InitException, IOException
    {
       this();
       load(settingsFile);
    }
 
    /**
      * Search for the named settingsFile from the supplied URL
      * and instantiate a Settings object based on its values
      */
    public Settings(URL settingsFile)
       throws InitException, IOException
    {
       this();
       load(settingsFile);
    }
 
 
    /**
      * Instantiate a new Settings object using the properties 
      * supplied as the settings values.
      */
    public Settings(Properties values) {
       _props = values;
       _prefix = null;
    }
 
    /**
      * Instantaite a new Settings object using the supplied 
      * Settings as the defaults
      */
    public Settings(Settings defaults) {
       Properties p = new Properties();
       String keys[] = defaults.keys();
       for (int i = 0; i < keys.length; i++) {
          p.setProperty(keys[i], defaults.getSetting(keys[i]));
       }
       _props = new Properties(p);
       _prefix = null;
    }
 
    /**
      * Instantiate a new Settings object using the properties 
      * supplied as the settings values. Only properties begining
      * with the supplied prefix are considered, and they are 
      * treated as though the prefix were not there.
      */
    private Settings(Properties p, String prefix) {
       _props = p;
       _prefix = prefix;
    }
 
 
    /**
      * Load settings from the supplied fileName, searching for 
      * the file along the classpath.
      */
    public void load(String fileName) throws InitException, IOException 
    {  
       ClassLoader cl = this.getClass().getClassLoader();
       URL u = cl.getResource(fileName);
       if (u == null) {
          u = ClassLoader.getSystemResource(fileName);
       }
       if (u == null) {
          StringBuffer error = new StringBuffer();
          error.append("Unable to locate the configuration file: ");
          error.append(fileName);
          error.append("\n");
          error.append("This may mean the system could not be started. The \n");
          error.append("following list should be where I looked for it:\n");
          error.append("\n");
          error.append("   my classpath:\n");
          try {
             buildPath(error, fileName, cl.getResources("."));
          } catch (Exception e) { }
          error.append("\n");
          error.append("   system classpath:\n");
          try {
             buildPath(error, fileName, ClassLoader.getSystemResources("."));
          } catch (Exception e) { }
          error.append("\n\n");
          error.append("Please create an appropriate " + fileName + " at one of the above\n");
          error.append("locations. Alternately this Settings class can be configured from\n");
          error.append("a Properties object, if you want to modify the init code.\n");
          throw new InitException(error.toString());
       }
       load(u);       
    }
 
    /**
      * Load settings from the supplied URL
      */
    public void load(URL u) throws IOException {
       InputStream in = u.openStream();
       _props.load(in);
       in.close();
    }
 
    private static
    void buildPath(StringBuffer b, String fileName, Enumeration e)
    {  
       while (e.hasMoreElements()) {
          b.append("\t");
          b.append(e.nextElement().toString());
          b.append(fileName);
          b.append("\n");
       }
    }
 
    /**
      * Prefix the key for use with the underlying Properties
      */
    private String prefix(String key) {
       return (_prefix == null) ? key : (_prefix + "." + key);
    }
 
    /**
      * Find out if a setting is defined
      */
    public boolean containsKey(String key) {
       return _props.containsKey(prefix(key));
    }
 
    /**
      * Get a setting
      */
    public String getSetting(String key) {
       return _props.getProperty(prefix(key));
    }
 
    /**
      * Get a setting with a default value in case it is not set
      */
    public String getSetting(String key, String defaultValue) {
       String ret = getSetting(key);
       return (ret != null) ? ret : defaultValue;
    }
 
    /**
      * Get a setting and convert it to an int 
      */
    public int getIntegerSetting(String key) {
       String snum = getSetting(key);
       try {
         return Integer.parseInt(key);
       } catch (Exception e) {
          return 0;
       }
    }
 
    /**
      * Get a setting with a default value in case it is not set
      */
    public int getIntegerSetting(String key, int defaultValue) {
       if (containsKey(key)) {
          return getIntegerSetting(key);
       } else {
          return defaultValue;
       }
    }
 
    /**
      * Get a setting and convert it to a boolean. The 
      * values "on", "true", and "yes" are considered to 
      * be TRUE values, everything else is FALSE.
      */
    public boolean getBooleanSetting(String key) {
       String setting = getSetting(key);
       return ((setting != null) &&
               ((setting.equalsIgnoreCase("on"))
               || (setting.equalsIgnoreCase("true"))
               || (setting.equalsIgnoreCase("yes")) ));
    }
 
    /**
      * Get a setting with a default value in case it is not set
      */
    public boolean getBooleanSetting(String key, boolean defaultValue) 
    {
       if (containsKey(key)) {
          return getBooleanSetting(key);
       } else {
          return defaultValue;
       }
    }
 
    /**
      * Get a subset of the settings in this file. The 
      * returned Settings object will be just those settings 
      * beginning with the supplied prefix, with the 
      * prefix chopped off. So if this settings file had 
      * a setting "LogLevel.foo" then the settings file
      * returned by getSubSettings("LogLevel") would contain
      * the key "foo".
      */
    public Settings getSubSettings(String prefix) {
       if (_prefix == null) {
          return new Settings(_props, prefix);
       } else {
          String subPrefix = _prefix + "." + prefix;
          return new Settings(_props, subPrefix);
       }
    }
 
    /**
      * Get the keys for this settings object as an array
      */
    public String[] keys() {
       ArrayList al = new ArrayList();
       Enumeration i = _props.keys();
       String dotPrefix = _prefix + ".";
       while (i.hasMoreElements()) {
          String key = (String) i.nextElement();
          if (_prefix == null) {
             al.add(key);
          } else {
             if (key.startsWith(dotPrefix)) {
                al.add(key.substring(dotPrefix.length()));
             }
          }
       }
       return (String[]) al.toArray(new String[0]);
    }
 
 
    /**
      * Brief test
      */
    public static void main(String arg[]) throws Exception
    {
 
       Settings s = new Settings();
       s.load("Test.properties");
 
       Settings sb = s.getSubSettings("b");
       String[] keys = sb.keys();
       for (int i = 0; i < keys.length; i++) {
          System.out.println("prop " + keys[i] + " = " + sb.getSetting(keys[i]));
       }
 
       System.out.println("LogTraceExceptions is: " + s.getBooleanSetting("LogTraceExceptions"));
 
    }
 }
 
