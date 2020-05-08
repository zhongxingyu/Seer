 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.fakebelieve.netbeans.plugin.logviewer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.prefs.Preferences;
 import org.openide.util.NbPreferences;
 
 /**
  *
  * @author mock
  */
 public class LogViewerOptions {
 
     public static final String DEFAULT_UNIX_SSH_COMMAND = "/usr/bin/ssh -l %r %h";
     public static final String DEFAULT_WINDOWS_SSH_COMMAND = "\"C:\\Program Files\\PuTTY\\plink.exe\" -l %r %h";
     public static final String DEFAULT_REMOTE_COMMAND = "tail -n %n -f %f";
     public static Preferences preferences = NbPreferences.forModule(LogViewerOptions.class);
 
 
     private static boolean isWindows() {
         return System.getProperty("os.name").toLowerCase().startsWith("windows");
     }
 
     public static String getSshCommand() {
         if (isWindows()) {
             return preferences.get("sshCommand", DEFAULT_WINDOWS_SSH_COMMAND);
         } else {
             return preferences.get("sshCommand", DEFAULT_UNIX_SSH_COMMAND);
         }
     }
 
     public static void setSshCommand(String command) {
         preferences.put("sshCommand", command);
     }
 
     public static String getRemoteCommand() {
         return preferences.get("remoteCommand", DEFAULT_REMOTE_COMMAND);
     }
 
     public static void setRemoteCommand(String command) {
         preferences.put("remoteCommand", command);
     }
 
     public static List<History> loadHistory() {
         List<History> logHistory = new ArrayList<History>();
         for (int idx = 0; idx < 15; idx++) {
             History history = new History(preferences, "history-" + idx);
             if (history.getCommand() != null && !history.getCommand().isEmpty()) {
                 logHistory.add(history);
             }
         }
         return logHistory;
     }
 
     public static void updateHistory(List<History> logHistory, History logConfig) {
         //
         // Save prefs in MRU order that way, crapshit ones will drop off the
         // list eventually.
         //
         for (int idx = 0; idx < logHistory.size(); idx++) {
             if (logHistory.get(idx).getCommand().equals(logConfig.getCommand())) {
                 logHistory.remove(idx);
                 break;
             }
         }
 
         logHistory.add(0, logConfig);
         saveHistory(logHistory);
     }
 
     public static void saveHistory(List<History> logHistory) {
         for (int idx = 0; idx < 15; idx++) {
             if (idx < logHistory.size()) {
                 logHistory.get(idx).update(preferences, "history-" + idx);
             } else {
                preferences.remove("history-" + idx);
             }
         }
     }
 }
