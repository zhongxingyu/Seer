 package sidekick.coffeescript;
 
 import org.gjt.sp.jedit.*;
 
/** Singleton for easy access to plugin options */
public enum Options {INSTANCE;
     public static final String
     OPTION_PREFIX = CoffeeScriptSideKickPlugin.OPTION_PREFIX;
 
     public static String getLabel(String name) {
         return jEdit.getProperty(OPTION_PREFIX+name+".label");
     }
 
     public static boolean getBool(String name) {
         return jEdit.getBooleanProperty(OPTION_PREFIX+name);
     }
 
     public static void setBool(String name, boolean value) {
         jEdit.setBooleanProperty(OPTION_PREFIX+name, value);
     }
 }
