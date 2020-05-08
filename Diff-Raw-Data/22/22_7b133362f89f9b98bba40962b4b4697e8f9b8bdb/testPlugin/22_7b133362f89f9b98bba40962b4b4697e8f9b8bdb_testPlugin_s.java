 package com.williambl.plugins.testplugin;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class testPlugin extends JavaPlugin{
     public void onEnable(){
     	getServer().getPluginManager().registerEvents(new TestPluginListener(), this);
     }
 }
