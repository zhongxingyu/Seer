 package com.gildorym.synctime;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SyncTime extends JavaPlugin {
 
 public void onEnable() {
 
 //Every 15 minutes, the server will synchronize the time of day with the calendar
 this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
 @Override
 public void run() {
 //Gets IRL time, in milliseconds, after most recent 1/5 day interval in UTC + 1
long irlTimeMillis = (currentTimeMillis() + 3600000L) % 17280000L;
 
 //Calculates desired time of day in Minecraft based on IRL time, in ticks.
 long syncedTimeOfDay = (irlTimeMillis / 17280000L) * 24000L; 
 
 //Gets current server time of day, in ticks.
 long serverTimeOfDay = SyncTime.this.getServer().getWorlds().get(0).getTime();
 
 //Calculates required shift in time to synchronize, in ticks.
 long timeShift = syncedTimeOfDay - serverTimeOfDay;
 
 //Synchronizes server time of day with time of day based on calendar
 SyncTime.this.getServer().getWorlds().get(0).setFullTime(SyncTime.this.getServer().getWorlds().get(0).getFullTime() + timeShift);
 
 }
 
 }, 18000L, 18000L);
 }
 
 }
