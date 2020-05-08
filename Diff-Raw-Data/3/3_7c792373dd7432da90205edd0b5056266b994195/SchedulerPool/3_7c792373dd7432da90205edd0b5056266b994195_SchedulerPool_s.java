 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nationsmc.chunkrefresh.scheduler;
 
 import com.nationsmc.chunkrefresh.ChunkRefresh;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 
 /**
  *
  * @author tarlach
  */
 public class SchedulerPool implements Runnable {
 
     /**
      *
      */
     List<ScheduleData> registered;
     File flatdir;
     ChunkRefresh plugin;
     
     public final void loadSchedules(String dir) throws IOException {
         
         this.flatdir = new File(dir);
         if (this.flatdir.isDirectory()) {
             for (File child : this.flatdir.listFiles()) {
                 if (child.canRead() && this.isValidFileName(child)) {
                     registered.add(new ScheduleData(child, plugin));
                     registered.get(registered.indexOf(child)).load();
                 } else {
                     throw new IOException();
                 }
                 
             }
         }
     }
     
     public SchedulerPool(String fileDir, ChunkRefresh plugin) {
         registered = new ArrayList();
         this.plugin = plugin;
         
         try {
             this.loadSchedules(fileDir);
         } catch (IOException ex) {
             Bukkit.getLogger().info("[SEVERE] IOException on SchedulePool Constructor!");
         }
         
     }
     
     public void add(Chunk chunk, long time) {
         ScheduleData dat = new ScheduleData(chunk, time);
         if (!registered.contains(dat)) {
             registered.add(dat);
         }
         try {
             this.flush();
         } catch (IOException ex) {
             Bukkit.getLogger().info("[SEVERE] Chunk Refresh Failed to flush Chunks!");
         }
     }
     
     @Override
     public void run() {
         synchronized (this) {
             for (ScheduleData dat : this.registered) {
                 if (timeBound(dat.time)) {
                    //TODO: Set update method here.
                    plugin.run(null);
                 }
                 dat.lastUpdate = System.currentTimeMillis();
                 dat.reset();
             }
         }
     }
     
     public boolean timeBound(long time) {
         long serverTime = System.currentTimeMillis();
         if (serverTime - 100000 < time && serverTime + 100000 > time) {
             return true;
         } else {
             return false;
         }
     }
     
     public boolean isValidFileName(File file) {
         String temp = file.getName();
         if (temp.matches("(\\d+).(\\d+).sch")) {
             return true;
         } else {
             return false;
         }
     }
     
     public void flush() throws IOException {
         for (ScheduleData dat : this.registered) {
             dat.save();
         }
     }
 }
