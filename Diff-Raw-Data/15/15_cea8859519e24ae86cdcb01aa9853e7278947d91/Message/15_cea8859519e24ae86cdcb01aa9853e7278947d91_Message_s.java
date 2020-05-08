 package com.dumptruckman.plugintemplate.locale;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * An enum containing all messages/strings used by Multiverse.
  */
 public enum Message {
     // BEGIN CHECKSTYLE-SUPPRESSION: Javadoc
     TEST_STRING("a test-string from the enum"),
 
     // Reload Command
     RELOAD_COMPLETE("&b===[ Reload Complete! ]===");
 
     // BEGIN CHECKSTYLE-SUPPRESSION: Javadoc
 
     private final List<String> def;
 
     Message(String def, String... extra) {
         this.def = new ArrayList<String>();
         this.def.add(def);
         this.def.addAll(Arrays.asList(extra));
     }
 
     /**
      * @return This {@link Message}'s default-message
      */
     public List<String> getDefault() {
         return def;
     }
 
 }
 
