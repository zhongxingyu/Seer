 package net.link.safeonline.sdk.api.auth;
 
 import java.util.EnumSet;
import org.jetbrains.annotations.Nullable;
 
 
 public enum LoginMode {
     REDIRECT, POPUP, FRAMED, FRAMED_NO_BREAKFRAME, POPUP_NO_CLOSE;
 
    @Nullable
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
