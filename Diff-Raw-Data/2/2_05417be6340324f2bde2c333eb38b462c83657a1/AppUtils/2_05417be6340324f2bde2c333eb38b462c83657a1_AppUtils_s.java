 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app.ui;
 
 import org.apache.commons.lang.SystemUtils;
 
 /**
  * Provides a few utilities for UI applications.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class AppUtils {
 
     /**
      * The default array of Linux browsers.
      */
     private static final String[] BROWSERS =
         { "google-chrome", "firefox", "mozilla", "opera", "konqueror" };
 
     /**
      * Checks if a command is available (in a Unix environment).
      *
      * @param command        the command-line application name
      *
      * @return true if the application is available, or
      *         false otherwise
      */
     public static boolean hasCommand(String command) {
         try {
             Process proc = Runtime.getRuntime().exec("which " + command);
             return proc.getInputStream().read() != -1;
         } catch (Exception ignore) {
             return false;
         }
     }
 
     /**
      * Opens the specified URL in the user's default browser.
      *
      * @param url            the URL to open
      *
      * @throws Exception if the URL failed to open or if no browser was found
      */
     public static void openURL(String url) throws Exception {
         if (SystemUtils.IS_OS_MAC_OSX) {
             Runtime.getRuntime().exec("open " + url);
         } else if (SystemUtils.IS_OS_WINDOWS) {
            Runtime.getRuntime().exec("start " + url);
         } else if (hasCommand("xdg-open")) {
             Runtime.getRuntime().exec("xdg-open " + url);
         } else if (hasCommand("gnome-open")) {
             Runtime.getRuntime().exec("gnome-open " + url);
         } else if (hasCommand("kde-open")) {
             Runtime.getRuntime().exec("kde-open " + url);
         } else {
             for (int i = 0; i < BROWSERS.length; i++) {
                 if (hasCommand(BROWSERS[i])) {
                     Runtime.getRuntime().exec(BROWSERS[i] + " " + url);
                     return;
                 }
             }
             throw new Exception("No browser found.");
         }
     }
 }
