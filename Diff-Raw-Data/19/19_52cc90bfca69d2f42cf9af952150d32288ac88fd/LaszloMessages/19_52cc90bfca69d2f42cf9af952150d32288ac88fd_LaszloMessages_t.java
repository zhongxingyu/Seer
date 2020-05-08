/* *****************************************************************************
 * LaszloMessages.java
 * ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2006 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/


 package org.openlaszlo.i18n;
 
 import java.text.MessageFormat;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import org.apache.log4j.Logger;
 import org.openlaszlo.server.LPS;
 
 public class LaszloMessages {
     private static Locale LOCALE;
     private static ResourceBundle RESOURCE_BUNDLE;
     private static final String BUNDLE_NAME = "org.openlaszlo.i18n.laszlomessages";
     private static Logger mLogger = Logger.getLogger(LaszloMessages.class);
     
     /* initialize static variables */
     static {
         LaszloMessages.setLocale(LPS.getLocale());     /* call added new LPS method */
         mLogger.debug("LPS mLocale="+LOCALE.getDisplayName());
         mLogger.debug("Resource locale="+RESOURCE_BUNDLE.getLocale().getDisplayName());
     }
     
     private LaszloMessages() {
     }
     
     public static void setLocale(Locale locale){
         LOCALE = locale;
         RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, LOCALE);
     }
     
     public static String getMessage(String packageName, String key) {
         return getMessage(packageName, key, new Object[]{});
     }
     
     public static String getMessage(String packageName, String key, Object[] args) {
         return getValue(packageName+"."+key, args);
     }
     
     public static String getValue(String fullKey){
         return getValue(fullKey, new Object[]{});
     }
     
     public static String getValue(String fullKey, Object[] args){
         try {
            //mLogger.debug(BUNDLE_NAME+".key="+fullKey);
             if(args.length==0){
                //mLogger.debug("value="+RESOURCE_BUNDLE.getString(fullKey));
                 return RESOURCE_BUNDLE.getString(fullKey);
             }else{
                 String format = RESOURCE_BUNDLE.getString(fullKey);
                //mLogger.debug(MessageFormat.format(format, args));
                 return MessageFormat.format(format, args);
             }
         } catch (MissingResourceException e) {
             String mes = "can't find key="+fullKey+" of '"+BUNDLE_NAME+" "+getLocaleString()+".properties'";
             System.err.println(mes);
             return mes;
         }
     }
     
     public static String getLocaleString(){
         String lang = RESOURCE_BUNDLE.getLocale().getLanguage();
     if( lang == "" )
         return "";
     else
         return "_" + lang;
     }
 }
