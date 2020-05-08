 package com.matejdro.bukkit.monsterhunt;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 public class HuntWorldManager {
     public static MonsterHuntWorld HuntZoneWorld;
     public static HashMap<String, MonsterHuntWorld> worlds = new HashMap<String, MonsterHuntWorld>();
     
     public static MonsterHuntWorld getWorld(String name) {
         if (Settings.globals.getBoolean(Setting.HuntZoneMode.getString(), false)) {
             return HuntZoneWorld;
         } else {
             return worlds.get(name);
         }
     }
 
     public static Collection<MonsterHuntWorld> getWorlds() {
         if (Settings.globals.getBoolean(Setting.HuntZoneMode.getString(), false)) {
             ArrayList<MonsterHuntWorld> list = new ArrayList<MonsterHuntWorld>();
             list.add(HuntZoneWorld);
             return list;
         } else {
             return worlds.values();
         }
     }
 
     public static void timer() {
         MonsterHunt.instance.getServer().getScheduler().scheduleSyncRepeatingTask(MonsterHunt.instance, new Runnable() {
 
             public void run() {
                 for (MonsterHuntWorld world : getWorlds()) {
                     if (world == null || world.getWorld() == null)
                         return;
                     long time = world.getWorld().getTime();
 
                    if (world.state == 0 && time < world.settings.getInt(Setting.StartTime) && time > world.getSignUpPeriodTime() && world.getSignUpPeriodTime() > 0 && !world.manual && !world.waitday) {
                         if (world.canStart()) {
                             world.state = 1;
                             String message = world.settings.getString(Setting.MessageSignUpPeriod);
                             message = message.replace("<World>", world.name);
                             Util.Broadcast(message);
                         }
                         world.waitday = true;
 
                     } else if (world.state < 2 && time > world.settings.getInt(Setting.StartTime) && time < world.settings.getInt(Setting.EndTime) && !world.manual) {
                         if (world.state == 1) {
                             if (world.Score.size() < world.settings.getInt(Setting.MinimumPlayers) && world.settings.getBoolean(Setting.EnableSignup)) {
                                 Util.Broadcast(world.settings.getString(Setting.MessageStartNotEnoughPlayers));
                                 world.state = 0;
                                 world.Score.clear();
                                 world.waitday = true;
                                 world.skipNight();
                             } else {
 	                             world.start();
                             }
                         } else if (!world.waitday && world.settings.getInt(Setting.SignUpPeriodTime) == 0) {
                             world.waitday = true;
                             if (world.canStart())
                                 world.start();
                         }
                     } 
                     else if (world.state == 2 && (time > world.settings.getInt(Setting.EndTime) || time < world.settings.getInt(Setting.StartTime)) && !world.manual) {
                         Util.Debug("[DEBUG - NEVEREND]Stop Time");
                         world.stop();
                     } else if (world.waitday && (time > world.settings.getInt(Setting.EndTime) || time < world.settings.getInt(Setting.StartTime) - world.getSignUpPeriodTime())) {
                         world.waitday = false;
                     }
                 }
             }
         }, 200L, 40L);
     }
 }
