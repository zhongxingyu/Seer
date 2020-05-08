 package com.thezomg.zomgctf;
 
 public class Configuration {
     
     private ZomgCTF plugin;
 
     public Configuration(ZomgCTF plugin) {
         this.plugin = plugin;
     }
     
    public int furthest_drop = plugin.getConfig().getInt("furthest_drop", 0);
    public int flag_capture_score = plugin.getConfig().getInt("scores.flagcapture", 5);
    public int kill_score = plugin.getConfig().getInt("scores.kill");
 
 }
