 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.fakebelieve.netbeans.plugin.logviewer;
 
 import java.util.prefs.Preferences;
 
 /**
  *
  * @author mock
  */
 public class History {
 
     private String command;
     private int refresh;
     private int lookback;
 
     public History(String command, int refresh, int lookback) {
         this.command = command;
         this.refresh = refresh;
         this.lookback = lookback;
     }
 
     public History(Preferences preferences, String prefix) {
         command = preferences.get(prefix + ".command", null);
         refresh = preferences.getInt(prefix + ".refresh", 0);
         lookback = preferences.getInt(prefix + ".lookback", 0);
     }
 
     public void update(Preferences preferences, String prefix) {
        preferences.put(prefix + ".command", command);
        preferences.putInt(prefix + ".refresh", refresh);
        preferences.putInt(prefix + ".lookback", lookback);
     }
 
    public static void remove(Preferences preferences, String prefix) {
        preferences.remove(prefix + ".command");
        preferences.remove(prefix + ".refresh");
        preferences.remove(prefix + ".lookback");
    }

     public String getCommand() {
         return command;
     }
 
     public void setCommand(String command) {
         this.command = command;
     }
 
     public int getRefresh() {
         return refresh;
     }
 
     public void setRefresh(int refresh) {
         this.refresh = refresh;
     }
 
     public int getLookback() {
         return lookback;
     }
 
     public void setLookback(int lookback) {
         this.lookback = lookback;
     }
     
 }
