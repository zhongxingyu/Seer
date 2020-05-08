 package kkckkc.jsourcepad.util;
 
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.settings.ScriptExecutionSettings;
 import kkckkc.utils.Os;
 
 public class Cygwin {
     public static String makePathForDirectUsage(String path) {
         if (! Os.isWindows()) return path;
         return path.replace('\\', '/').replaceAll("^([a-zA-Z]):", "/cygdrive/$1").replaceAll(" ", "\\\\ ");
     }
 
     public static String makePathForEnvironmentUsage(String path) {
         if (! Os.isWindows()) return path;
         return path.replace('\\', '/').replaceAll("^([a-zA-Z]):", "/cygdrive/$1");
     }
 
     public static String toFile(String path) {
         if (! Os.isWindows()) return path;
 
        if (path.startsWith("/") && ! path.startsWith("/cygdrive")) {
             ScriptExecutionSettings ses = Application.get().getSettingsManager().get(ScriptExecutionSettings.class);
             String bashCommand = ses.getShellCommandLine()[0];
             String cygwinPrefix = bashCommand.substring(0, bashCommand.indexOf("/bin/bash.exe"));
             path = cygwinPrefix + path;
         }
 
         return path.replaceAll("^/cygdrive/([A-Za-z])", "$1:");
     }
 }
