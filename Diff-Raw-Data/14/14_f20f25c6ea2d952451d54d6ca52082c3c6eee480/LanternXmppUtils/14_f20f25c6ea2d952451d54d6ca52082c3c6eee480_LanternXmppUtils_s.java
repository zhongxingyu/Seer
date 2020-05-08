 package org.lantern;
 
 import org.apache.commons.lang3.StringUtils;
 
 
 public class LanternXmppUtils {
 
    public static String jidToUserId(final String fullId) {
        if (fullId.contains("/")) {
            return fullId.split("/")[0];
        } else {
            return fullId;
        }
    }

     /** This is misspelled -- use jidToResourceId */
     @Deprecated
     public static String jidToInstanceId(final String fullId) {
         return fullId.split("/", 2)[1];
     }
 
     public static String jidToResourceId(final String fullId) {
         return fullId.split("/", 2)[1];
     }
 
     public static boolean isLanternJid(final String from) {
         // Here's the format we're looking for: "-lan-"
         if (from.contains("/"+LanternConstants.UNCENSORED_ID)) {
             return true;
         }
         return false;
     }
 
     public static String jidToEmail(final String jid) {
         if (jid.contains("/")) {
            return StringUtils.substringBefore(jid, "/");
         }
        return jid;
     }
 }
