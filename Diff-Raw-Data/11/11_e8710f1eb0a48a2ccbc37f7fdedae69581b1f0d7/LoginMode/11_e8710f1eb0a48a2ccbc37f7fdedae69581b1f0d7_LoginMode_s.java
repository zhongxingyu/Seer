 package net.link.safeonline.sdk.api.auth;
 
 import java.util.EnumSet;
 
 
/**
 * Created by IntelliJ IDEA.
 * User: sgdesmet
 * Date: 22/11/11
 * Time: 12:01
 * To change this template use File | Settings | File Templates.
 */
 public enum LoginMode {
     REDIRECT, POPUP, FRAMED, FRAMED_NO_BREAKFRAME, POPUP_NO_CLOSE;
 
     public static LoginMode fromString(String text) {
 
         for (LoginMode type : EnumSet.allOf( LoginMode.class )) {
             if (type.toString().equalsIgnoreCase( text ))
                 return type;
         }
         return null;
     }
 
     public static LoginMode fromString(String text, LoginMode fallback) {
 
         for (LoginMode type : EnumSet.allOf( LoginMode.class )) {
             if (type.toString().equalsIgnoreCase( text ))
                 return type;
         }
         return fallback;
     }
 }
