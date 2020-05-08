 package org.iplantc.de.client.utils;
 
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 
 /**
  * Utilities to facilitate logging out of the discovery environment.
  * 
  * @author Dennis Roberts
  */
 public class LogoutUtil {
     /**
      * Builds the URL used to log out of the discovery environment.
      * 
      * @return the URL as a string.
      */
     public static String buildLogoutUrl() {
        return Constants.CLIENT.logoutUrl(); //$NON-NLS-1$
     }
 
     /**
      * Builds the message to display when the user is being logged out.
      * 
      * @return the message as a string.
      */
     public static String buildLogoutMessageText() {
         return I18N.DISPLAY.logoutMessageText(buildLogoutUrl());
     }
 
 
 }
