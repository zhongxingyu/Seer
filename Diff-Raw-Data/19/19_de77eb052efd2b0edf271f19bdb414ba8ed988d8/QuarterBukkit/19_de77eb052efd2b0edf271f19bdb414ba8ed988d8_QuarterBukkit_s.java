 /*
  * This file is part of QuarterBukkit-Plugin.
  * Copyright (c) 2012 QuarterCode <http://www.quartercode.com/>
  *
  * QuarterBukkit-Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * QuarterBukkit-Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with QuarterBukkit-Plugin. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.quarterbukkit;
 
 import java.io.File;
 import java.io.IOException;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.quartercode.quarterbukkit.util.Config;
 import com.quartercode.quarterbukkit.util.CustomEventListener;
 import com.quartercode.quarterbukkit.util.Metrics;
 import com.quartercode.quarterbukkit.util.QuarterBukkitExceptionListener;
 import com.quartercode.quarterbukkit.util.QuarterBukkitUpdater;
 
 /**
  * The main class of the QuarterBukkit API.
  * This is for easy API loading.
  */
 public class QuarterBukkit extends JavaPlugin {
 
     private static Plugin plugin;
 
     /**
      * Returns the current {@link Plugin}.
      * 
      * @return The current {@link Plugin}.
      */
     public static Plugin getPlugin() {
 
         return plugin;
     }
 
     private Config  config;
     private Metrics metrics;
 
     /**
      * The default constructor for Bukkit.
      */
     public QuarterBukkit() {
 
         if (plugin == null) {
             plugin = this;
         } else {
             throw new IllegalStateException("Plugin already initalized");
         }
     }
 
     /**
      * This method is called when the plugin loads. It will initalize the most important functions.
      * The plugin will check for new versions and updates, if required.
      */
     @Override
     public void onLoad() {
 
         getLogger().info("Successfully loaded " + getName() + "!");
     }
 
     /**
      * This method is called when the plugin gets enabled.
      * It will initalize the main API functions.
      */
     @Override
     public void onEnable() {
 
         config = new Config(this, new File(getDataFolder(), "config.yml"));
 
         if (config.getBoolean("autoupdate")) {
             try {
                 QuarterBukkitUpdater updater = new QuarterBukkitUpdater(this);
                 if (updater.isNewVersionAvaiable(Bukkit.getConsoleSender())) {
                     getLogger().info("Updating QuarterBukkit ...");
                     updater.tryInstall();
                 }
             }
             catch (Exception e) {
                 Bukkit.getLogger().severe("An error occurred while updating QuarterBukkit (" + e + ")");
             }
         }
 
         try {
             metrics = new Metrics(this);
             metrics.start();
         }
         catch (IOException e) {
             getLogger().severe("An error occurred while enabling Metrics (" + e + ")");
         }
 
        // Internal Exceptions
        new QuarterBukkitExceptionListener(plugin);

         // Custom Events
         new CustomEventListener(plugin);
 
         getLogger().info("Successfully enabled " + getName() + "!");
     }
 
     /**
      * This method is called when the plugin gets disabled.
      * It will disable the enabled API functions and clear the space.
      */
     @Override
     public void onDisable() {
 
         getLogger().info("Successfully disabled " + getName() + "!");
     }
 
     @Override
     public Config getConfig() {
 
         return config;
     }
 
     @Override
     public String toString() {
 
         return getClass().getName() + " [metrics=" + metrics + ", getServer()=" + getServer() + ", getName()=" + getName() + "]";
     }
 
 }
