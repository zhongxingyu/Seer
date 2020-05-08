 package jkmau5.superpowers.utils;
 
 import cpw.mods.fml.common.FMLLog;
 
 import java.util.logging.Level;
 
 /**
  * Our logging handler
  *
  * @author jk-5
  */
 public class SPLogger {
 
     private static final String name = String.format("SuperPowers-%s", SideHelper.getEffectiveSide().name());
 
    public static void finest(String message, Object... data) {
         FMLLog.log(name, Level.FINEST, message, data);
     }
 
    public static void finer(String message, Object... data) {
         FMLLog.log(name, Level.FINER, message, data);
     }
 
    public static void fine(String message, Object... data) {
         FMLLog.log(name, Level.FINE, message, data);
     }
 
    public static void info(String message, Object... data) {
         FMLLog.log(name, Level.FINE, message, data);
     }
 
    public static void warn(String message, Object... data) {
         FMLLog.log(name, Level.WARNING, message, data);
     }
 
    public static void severe(String message, Object... data) {
         FMLLog.log(name, Level.SEVERE, message, data);
     }
 
    public static void warn(String message, Throwable t, Object... data) {
         FMLLog.log(name, Level.WARNING, t, message, data);
     }
 
    public static void severe(String message, Throwable t, Object... data) {
         FMLLog.log(name, Level.SEVERE, t, message, data);
     }
 }
