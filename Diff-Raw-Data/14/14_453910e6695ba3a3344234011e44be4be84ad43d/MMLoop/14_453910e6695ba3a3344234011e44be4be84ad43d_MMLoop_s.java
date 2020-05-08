 package com.cnaude.mutemanager;
 
 import java.util.ArrayList;
 
 public class MMLoop {
 
     private final MuteManager plugin;
     private int taskID;
 
     public MMLoop(MuteManager instance) {
         plugin = instance;
         plugin.logInfo("MuteManager main loop running.");
 
         taskID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
             @Override
             public void run() {
                 plugin.logDebug("Checking mute list.");
                 if (plugin.mList.isEmpty()) {
                     return;
                 }
                 ArrayList unMuteList = new ArrayList();
                 for (String pName : plugin.mList.keySet()) {
                     if (plugin.mList.containsKey(pName)) {
                         long curTime = System.currentTimeMillis();
                         long expTime = plugin.mList.get(pName);
                         if (expTime <= curTime) {
                             unMuteList.add(pName);
                         }
                     }
                 }
                 for (Object pName : unMuteList) {
                     plugin.unMutePlayer((String) pName);
                 }
                 unMuteList.clear();
             }
         }, 200, 200);
     }
 
     public void end() {
         this.plugin.getServer().getScheduler().cancelTask(taskID);
     }
 }
