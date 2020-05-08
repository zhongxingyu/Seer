 package com.thezomg.zomgctf;
 
 public class Configuration {
     
     private ZomgCTF plugin;
 
     public Configuration(ZomgCTF plugin) {
         this.plugin = plugin;
        
        furthest_drop = plugin.getConfig().getInt("furthest_drop", 0);
        flag_capture_score = plugin.getConfig().getInt("scores.flagcapture", 0);
        kill_score = plugin.getConfig().getInt("scores.kill");
     }
     
    public int furthest_drop;
    public int flag_capture_score;
    public int kill_score;
 
 }
